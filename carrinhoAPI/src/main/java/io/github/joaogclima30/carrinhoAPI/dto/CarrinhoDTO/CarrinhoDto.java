package io.github.joaogclima30.carrinhoAPI.dto.CarrinhoDTO;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Data
public class CarrinhoDto {
    private Long carrinhoId;
    private Set<CarrinhoItemDto> itens;
    private BigDecimal precoTotal;
}
