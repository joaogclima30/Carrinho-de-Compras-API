package io.github.joaogclima30.carrinhoAPI.dto.CarrinhoDTO;

import io.github.joaogclima30.carrinhoAPI.dto.ProdutoDTO.ProdutoResponseDTO;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CarrinhoItemDto {
    private Long itemId;
    private Integer quantidade;
    private BigDecimal precoUnitario;
    private ProdutoResponseDTO produto;
}
