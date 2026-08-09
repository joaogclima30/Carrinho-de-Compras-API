package io.github.joaogclima30.carrinhoAPI.dto.ProdutoDTO;

import io.github.joaogclima30.carrinhoAPI.model.Categoria;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProdutoRequestDTO {
    String nome;
    String marca;
    BigDecimal preco;
    int estoque;
    String descricao;
    Categoria categoria;
}
