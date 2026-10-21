package io.github.joaogclima30.carrinhoAPI.dto.UsuarioDTO;

import io.github.joaogclima30.carrinhoAPI.dto.CarrinhoDTO.CarrinhoDto;
import io.github.joaogclima30.carrinhoAPI.dto.PedidoDTO.PedidoResponseDTO;
import lombok.Data;

import java.util.List;

@Data
public class UsuarioResponseDTO {

    private Long id;
    private String primeiroNome;
    private String sobrenome;
    private String email;
    private List<PedidoResponseDTO> pedidos;
    private CarrinhoDto carrinho;
}
