package io.github.joaogclima30.carrinhoAPI.service.Carrinho;

import io.github.joaogclima30.carrinhoAPI.exceptions.DadosInvalidosException;
import io.github.joaogclima30.carrinhoAPI.exceptions.ConflitoException;
import io.github.joaogclima30.carrinhoAPI.exceptions.ExceptionsCarrinho.CarrinhoNaoEncontrado;
import io.github.joaogclima30.carrinhoAPI.exceptions.ExceptionsProdutos.ProdutoNaoEncontrado;
import io.github.joaogclima30.carrinhoAPI.model.*;
import io.github.joaogclima30.carrinhoAPI.repository.CarrinhoRepository;
import io.github.joaogclima30.carrinhoAPI.service.Produto.ProdutoService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Transactional
public class CarrinhoItemService {
    private final CarrinhoRepository carrinhoRepository;
    private final ProdutoService produtoService;
    private final CarrinhoService carrinhoService;
    private final io.github.joaogclima30.carrinhoAPI.mapper.DtoMapper mapper;
    private final jakarta.persistence.EntityManager entityManager;

    public io.github.joaogclima30.carrinhoAPI.dto.CarrinhoDTO.CarrinhoDto adicionarItem(
            Long usuarioId, Long carrinhoId, Long produtoId, int quantidade) {
        validarQuantidade(quantidade);
        Carrinho carrinho = carrinhoService.prepararCarrinho(usuarioId, carrinhoId);
        salvarItem(carrinho.getId(), produtoId, quantidade);
        entityManager.flush();
        return mapper.carrinho(carrinho);
    }

    @Transactional(readOnly = true)
    public io.github.joaogclima30.carrinhoAPI.dto.CarrinhoDTO.CarrinhoItemDto listarItemDto(Long carrinhoId, Long produtoId) {
        return mapper.item(listarCarrinhoItem(carrinhoId, produtoId));
    }

    public void salvarItem(Long carrinhoId, Long produtoId, int quantidade) {
        validarQuantidade(quantidade);
        Carrinho carrinho = buscarParaAtualizar(carrinhoId);
        Produto produto = produtoService.obterPorId(produtoId);
        CarrinhoItem item = carrinho.getCarrinhoItems().stream()
                .filter(i -> i.getProduto().getId().equals(produtoId)).findFirst().orElse(null);
        long novaQuantidade = (long) quantidade + (item == null ? 0 : item.getQuantidade());
        if (novaQuantidade > produto.getEstoque()) {
            throw new ConflitoException("Quantidade solicitada superior ao estoque disponível");
        }
        if (item == null) {
            item = new CarrinhoItem();
            item.setProduto(produto);
            item.setPrecoUnico(produto.getPrice());
        }
        item.setQuantidade((int) novaQuantidade);
        item.setTotalPrice();
        carrinho.adicionarItem(item);
    }

    public void removerItemDoCarrinho(Long carrinhoId, Long produtoId) {
        Carrinho carrinho = buscarParaAtualizar(carrinhoId);
        carrinho.removerItem(encontrarItem(carrinho, produtoId));
    }

    public void atualizarQuantidadeItem(Long carrinhoId, Long produtoId, int quantidade) {
        validarQuantidade(quantidade);
        Carrinho carrinho = buscarParaAtualizar(carrinhoId);
        CarrinhoItem item = encontrarItem(carrinho, produtoId);
        if (quantidade > item.getProduto().getEstoque()) {
            throw new ConflitoException("Quantidade solicitada superior ao estoque disponível");
        }
        item.setQuantidade(quantidade);
        item.setPrecoUnico(item.getProduto().getPrice());
        item.setTotalPrice();
        carrinho.atualizarQuantidadeTotal();
    }

    public CarrinhoItem listarCarrinhoItem(Long carrinhoId, Long produtoId) {
        Carrinho carrinho = carrinhoRepository.findById(carrinhoId)
                .orElseThrow(() -> new CarrinhoNaoEncontrado("Carrinho não encontrado"));
        return encontrarItem(carrinho, produtoId);
    }

    private Carrinho buscarParaAtualizar(Long id) {
        return carrinhoRepository.buscarParaAtualizar(id)
                .orElseThrow(() -> new CarrinhoNaoEncontrado("Carrinho não encontrado"));
    }

    private CarrinhoItem encontrarItem(Carrinho carrinho, Long produtoId) {
        return carrinho.getCarrinhoItems().stream().filter(i -> i.getProduto().getId().equals(produtoId))
                .findFirst().orElseThrow(() -> new ProdutoNaoEncontrado("Item não encontrado"));
    }

    private void validarQuantidade(int quantidade) {
        if (quantidade <= 0) throw new DadosInvalidosException("Quantidade deve ser maior que zero");
    }
}
