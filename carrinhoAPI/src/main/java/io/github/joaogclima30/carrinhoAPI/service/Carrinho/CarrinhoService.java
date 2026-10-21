package io.github.joaogclima30.carrinhoAPI.service.Carrinho;

import io.github.joaogclima30.carrinhoAPI.dto.CarrinhoDTO.CarrinhoDto;
import io.github.joaogclima30.carrinhoAPI.exceptions.*;
import io.github.joaogclima30.carrinhoAPI.exceptions.ExceptionsCarrinho.CarrinhoNaoEncontrado;
import io.github.joaogclima30.carrinhoAPI.exceptions.ExceptionsUsuario.UsuarioNaoEncontrado;
import io.github.joaogclima30.carrinhoAPI.mapper.DtoMapper;
import io.github.joaogclima30.carrinhoAPI.model.*;
import io.github.joaogclima30.carrinhoAPI.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CarrinhoService {
    private final CarrinhoRepository carrinhoRepository;
    private final UsuarioRepository usuarioRepository;
    private final DtoMapper mapper;

    public Carrinho listarCarrinho(Long id) {
        return carrinhoRepository.findById(id)
                .orElseThrow(() -> new CarrinhoNaoEncontrado("Carrinho não encontrado"));
    }

    public CarrinhoDto listarCarrinhoDto(Long id) {
        return mapper.carrinho(listarCarrinho(id));
    }

    @Transactional
    public void limparCarrinho(Long id) {
        carrinhoRepository.buscarParaAtualizar(id)
                .orElseThrow(() -> new CarrinhoNaoEncontrado("Carrinho não encontrado")).limpar();
    }

    public BigDecimal precoTotal(Long id) {
        return listarCarrinho(id).getCarrinhoItems().stream().map(CarrinhoItem::getPrecoTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional
    public Carrinho prepararCarrinho(Long usuarioId, Long carrinhoId) {
        Carrinho informado = carrinhoId == null ? null : listarCarrinho(carrinhoId);
        if (informado != null) {
            if (informado.getUsuario() == null) throw new ConflitoException("Carrinho não possui usuário vinculado");
            Long donoId = informado.getUsuario().getId();
            if (usuarioId != null && !usuarioId.equals(donoId)) {
                throw new ConflitoException("O carrinho informado não pertence ao usuário informado");
            }
            usuarioId = donoId;
        }
        if (usuarioId == null || usuarioId <= 0) {
            throw new DadosInvalidosException("Informe usuarioId ou um carrinhoId válido");
        }
        // Serializa a primeira criação por usuário, inclusive quando ainda não existe carrinho.
        Usuario usuario = usuarioRepository.buscarParaAtualizar(usuarioId)
                .orElseThrow(() -> new UsuarioNaoEncontrado("Usuário não encontrado"));
        Carrinho carrinho = carrinhoRepository.findByUsuarioId(usuario.getId()).orElseGet(() -> {
            Carrinho novo = new Carrinho();
            novo.setUsuario(usuario);
            return carrinhoRepository.save(novo);
        });
        if (carrinhoId != null && carrinho.getId() != carrinhoId.longValue()) {
            throw new ConflitoException("O carrinho informado não é o carrinho do usuário");
        }
        usuario.setCarrinho(carrinho);
        return carrinho;
    }

    @Transactional
    public Carrinho initializeNovoCarrinho(Usuario usuario) {
        if (usuario == null) throw new DadosInvalidosException("Usuário obrigatório");
        Carrinho carrinho = prepararCarrinho(usuario.getId(), null);
        usuario.setCarrinho(carrinho);
        return carrinho;
    }

    public Carrinho getCarrinhoByUsuarioId(Long id) {
        return carrinhoRepository.findByUsuarioId(id).orElse(null);
    }
}
