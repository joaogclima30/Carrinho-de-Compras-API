package io.github.joaogclima30.carrinhoAPI.service.Carrinho;

import io.github.joaogclima30.carrinhoAPI.exceptions.ExceptionsProdutos.ProdutoNaoEncontrado;
import io.github.joaogclima30.carrinhoAPI.model.Carrinho;
import io.github.joaogclima30.carrinhoAPI.model.CarrinhoItem;
import io.github.joaogclima30.carrinhoAPI.model.Produto;
import io.github.joaogclima30.carrinhoAPI.repository.CarrinhoItemRepository;
import io.github.joaogclima30.carrinhoAPI.repository.CarrinhoRepository;
import io.github.joaogclima30.carrinhoAPI.service.Produto.ProdutoService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Service
public class CarrinhoItemService {

    private final CarrinhoItemRepository carrinhoItemRepository;
    private final CarrinhoRepository carrinhoRepository;
    private final ProdutoService produtoService;

    @Transactional
    public void salvarItem(Long carrinhoId, Long produtoId, int quantidade) {

        Carrinho carrinhoEncontrado = carrinhoRepository
                .findById(carrinhoId)
                .orElseThrow(() -> new RuntimeException("Carrinho não encontrado"));

        Produto produto = produtoService.obterPorId(produtoId);

        CarrinhoItem carrinhoItem = carrinhoEncontrado.getCarrinhoItems()
                .stream()
                .filter(item -> item.getProduto().getId().equals(produtoId))
                .findFirst()
                .orElse(new CarrinhoItem());

        if (carrinhoItem.getId() == null) {
            carrinhoItem.setProduto(produto);
            carrinhoItem.setQuantidade(quantidade);
            carrinhoItem.setCarrinho(carrinhoEncontrado);
            carrinhoItem.setPrecoUnico(produto.getPrice());
        } else {
            carrinhoItem.setQuantidade(
                    carrinhoItem.getQuantidade() + quantidade
            );
        }

        carrinhoItem.setTotalPrice();

        carrinhoEncontrado.adicionarItem(carrinhoItem);
    }

    @Transactional
    public void removerItemDoCarrinho(Long carrinhoId, Long produtoId){
        Carrinho carrinhoEncontrado = carrinhoRepository
                .findById(carrinhoId)
                .orElseThrow(() -> new RuntimeException("Carrinho não encontrado"));
        CarrinhoItem itemParaRemover = listarCarrinhoItem(carrinhoId,produtoId);
        carrinhoEncontrado.removerItem(itemParaRemover);
        carrinhoRepository.save(carrinhoEncontrado);
    }

    @Transactional
    public void atualizarQuantidadeItem(Long carrinhoId, Long produtoId, int quantidade) {

        Carrinho carrinhoEncontrado = carrinhoRepository
                .findById(carrinhoId)
                .orElseThrow(() -> new RuntimeException("Carrinho não encontrado"));

        carrinhoEncontrado.getCarrinhoItems()
                .stream()
                .filter(item -> item.getProduto().getId().equals(produtoId))
                .findFirst()
                .ifPresent(item -> {
                    item.setQuantidade(quantidade);
                    item.setPrecoUnico(item.getProduto().getPrice());
                    item.setTotalPrice();
                });

        carrinhoEncontrado.atualizarQuantidadeTotal();
    }

    public CarrinhoItem listarCarrinhoItem(Long carrinhoId,Long produtoId){
        Carrinho carrinhoEncontrado = carrinhoRepository
                .findById(carrinhoId)
                .orElseThrow(() -> new RuntimeException("Carrinho não encontrado"));
        return carrinhoEncontrado.getCarrinhoItems()
                .stream()
                .filter(item -> item.getProduto().getId().equals(produtoId))
                .findFirst().orElseThrow(()-> new ProdutoNaoEncontrado("Item não Encontrado"));
    }
}
