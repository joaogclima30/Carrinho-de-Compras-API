package io.github.joaogclima30.carrinhoAPI.validator;

import io.github.joaogclima30.carrinhoAPI.exceptions.ProdutoNaoEncontrado;
import io.github.joaogclima30.carrinhoAPI.exceptions.produtoJaCadastrado;
import io.github.joaogclima30.carrinhoAPI.model.Produto;
import io.github.joaogclima30.carrinhoAPI.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProdutoValidator {

    private final ProdutoRepository produtoRepository;

    public void validarProduto(Produto produto){

        if(existeProdutoCadastrado(produto)){
            throw new produtoJaCadastrado("Produto ja foi cadastrado");
        }

        if(!existeProdutoCadastrado(produto)){
            throw new ProdutoNaoEncontrado("Não existe produto cadastrado");
        }

    }

    public boolean existeProdutoCadastrado(Produto produto){
        Optional<Produto> produtoEncontrado = produtoRepository.findById(produto.getId());

        if(produto.getId() == null){
            return produtoEncontrado.isPresent();
        }

        return !produto.getId().equals(produtoEncontrado.get().getId()) && produtoEncontrado.isPresent();
    }

}
