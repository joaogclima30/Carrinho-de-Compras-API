package io.github.joaogclima30.carrinhoAPI.service.Carrinho;

import io.github.joaogclima30.carrinhoAPI.model.Carrinho;
import io.github.joaogclima30.carrinhoAPI.model.CarrinhoItem;
import io.github.joaogclima30.carrinhoAPI.model.Usuario;
import io.github.joaogclima30.carrinhoAPI.repository.CarrinhoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import io.github.joaogclima30.carrinhoAPI.exceptions.ExceptionsCarrinho.CarrinhoNaoEncontrado;

import java.math.BigDecimal;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@org.springframework.transaction.annotation.Transactional
public class CarrinhoService {

    private final CarrinhoRepository carrinhoRepository;

    public Carrinho listarCarrinho(Long id){
        Carrinho carrinhoEncontrado = carrinhoRepository.findById(id).orElseThrow(() -> new CarrinhoNaoEncontrado("Carrinho não encontrado"));
        return carrinhoEncontrado;
    }

    public void limparCarrinho(Long id){
        Carrinho carrinhoEncontrado = carrinhoRepository.buscarParaAtualizar(id)
                .orElseThrow(() -> new CarrinhoNaoEncontrado("Carrinho não encontrado"));
        carrinhoEncontrado.limpar();
    }

    public BigDecimal precoTotal(Long id){
        Carrinho carrinhoEncontrado = carrinhoRepository.findById(id).orElseThrow(() -> new CarrinhoNaoEncontrado("Carrinho não encontrado"));
        return carrinhoEncontrado.getCarrinhoItems()
                .stream()
                .map(CarrinhoItem::getPrecoTotal)
                .reduce(BigDecimal.ZERO,BigDecimal::add);

    }

    public Carrinho initializeNovoCarrinho (Usuario usuario){
        return Optional.ofNullable(getCarrinhoByUsuarioId(usuario.getId()))
                .orElseGet(() -> {
                    Carrinho carrinho = new Carrinho();
                    carrinho.setUsuario(usuario);
                    return carrinhoRepository.save(carrinho);
                });
    }

    public Carrinho getCarrinhoByUsuarioId(Long usuarioId){
        return carrinhoRepository.findByUsuarioId(usuarioId);
    }
}
