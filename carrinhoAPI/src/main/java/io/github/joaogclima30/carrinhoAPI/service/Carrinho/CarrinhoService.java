package io.github.joaogclima30.carrinhoAPI.service.Carrinho;

import io.github.joaogclima30.carrinhoAPI.model.Carrinho;
import io.github.joaogclima30.carrinhoAPI.model.CarrinhoItem;
import io.github.joaogclima30.carrinhoAPI.repository.CarrinhoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import io.github.joaogclima30.carrinhoAPI.exceptions.ExceptionsCarrinho.CarrinhoNaoEncontrado;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Service
public class CarrinhoService {

    private final CarrinhoRepository carrinhoRepository;

    public Carrinho listarCarrinho(Long id){
        Carrinho carrinhoEncontrado = carrinhoRepository.findById(id).orElseThrow(() -> new CarrinhoNaoEncontrado("Carrinho não encontrado"));
        BigDecimal total = carrinhoEncontrado.getQuantidadeTotal();
        carrinhoEncontrado.setQuantidadeTotal(total);
        return carrinhoRepository.save(carrinhoEncontrado);
    }

    public void limparCarrinho(Long id){
        Carrinho carrinhoEncontrado = carrinhoRepository.findById(id).orElseThrow(() -> new CarrinhoNaoEncontrado("Carrinho não encontrado"));
        carrinhoEncontrado.getCarrinhoItems().clear();
        carrinhoRepository.save(carrinhoEncontrado);
    }

    public BigDecimal precoTotal(Long id){
        Carrinho carrinhoEncontrado = carrinhoRepository.findById(id).orElseThrow(() -> new CarrinhoNaoEncontrado("Carrinho não encontrado"));
        return carrinhoEncontrado.getCarrinhoItems()
                .stream()
                .map(CarrinhoItem::getPrecoTotal)
                .reduce(BigDecimal.ZERO,BigDecimal::add);

    }

    public Long initializeNovoCarrinho (){
        Carrinho carrinho = new Carrinho();
        return carrinhoRepository.save(carrinho).getId();
    }
}
