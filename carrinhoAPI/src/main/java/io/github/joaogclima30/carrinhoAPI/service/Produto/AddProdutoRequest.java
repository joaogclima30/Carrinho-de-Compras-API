package io.github.joaogclima30.carrinhoAPI.service.Produto;

import io.github.joaogclima30.carrinhoAPI.model.Categoria;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AddProdutoRequest {

    private Long id;
    private String nome;
    private String marca;
    private BigDecimal preco;
    private int estoque;
    private String descricao;
    private Categoria categoria;
}
