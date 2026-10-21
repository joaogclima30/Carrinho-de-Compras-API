package io.github.joaogclima30.carrinhoAPI.dto.PedidoDTO;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PedidoResponseDTO {
    private Long pedidoId;
    private Long usuarioId;
    private LocalDateTime pedidoHorario;
    private BigDecimal totalPedido;
    private String status;
    private List<PedidoItemDTO> item;

}
