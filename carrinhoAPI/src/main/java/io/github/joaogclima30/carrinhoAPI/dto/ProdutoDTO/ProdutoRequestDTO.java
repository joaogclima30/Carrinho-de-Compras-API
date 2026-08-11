package io.github.joaogclima30.carrinhoAPI.dto.ProdutoDTO;

import io.github.joaogclima30.carrinhoAPI.dto.ImagemDTO.ImagemDTO;
import io.github.joaogclima30.carrinhoAPI.model.Categoria;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProdutoRequestDTO {
    private String nome;
    private String marca;
    private BigDecimal preco;
    private int estoque;
    private String descricao;
    private Categoria categoria;
    private List<ImagemDTO> imagensDto;
}
