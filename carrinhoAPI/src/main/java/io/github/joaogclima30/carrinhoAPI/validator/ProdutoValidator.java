package io.github.joaogclima30.carrinhoAPI.validator;

import io.github.joaogclima30.carrinhoAPI.dto.ProdutoDTO.ProdutoRequestDTO;
import io.github.joaogclima30.carrinhoAPI.exceptions.ExceptionsProdutos.produtoJaCadastrado;
import io.github.joaogclima30.carrinhoAPI.model.Produto;
import io.github.joaogclima30.carrinhoAPI.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProdutoValidator {

    private final ProdutoRepository produtoRepository;

    public void validarProduto(ProdutoRequestDTO produtoRequestDTO){
        boolean duplicado = produtoRepository.existsByNomeAndMarca(produtoRequestDTO.getNome(), produtoRequestDTO.getMarca());
        if(duplicado){
            throw new produtoJaCadastrado("Produto já foi cadastrado");
        }
    }

    public boolean existeProdutoCadastrado(Produto produto){
        if(produto.getId() == null){
            return false;
        }
        return produtoRepository.existsById(produto.getId());
    }
}

