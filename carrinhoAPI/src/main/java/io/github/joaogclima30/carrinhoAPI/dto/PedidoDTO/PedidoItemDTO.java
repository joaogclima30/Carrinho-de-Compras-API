package io.github.joaogclima30.carrinhoAPI.dto.PedidoDTO;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PedidoItemDTO {
    private Long produtoId;
    private String produtoNome;
    private int quantidade;
    private BigDecimal price;
}
