package io.github.joaogclima30.carrinhoAPI.controller;

import io.github.joaogclima30.carrinhoAPI.dto.PedidoDTO.PedidoResponseDTO;
import io.github.joaogclima30.carrinhoAPI.exceptions.ExceptionsPedidos.PedidoNaoEncontrado;
import io.github.joaogclima30.carrinhoAPI.model.Pedido;
import io.github.joaogclima30.carrinhoAPI.response.ApiResponse;
import io.github.joaogclima30.carrinhoAPI.service.Pedido.PedidoService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    @PostMapping("/{usuarioId}")
    public ResponseEntity<ApiResponse> fazerPedido(@PathVariable Long usuarioId){
        return ResponseEntity.ok(new ApiResponse("Pedido realizado com sucesso", pedidoService.fazerPedidoDto(usuarioId)));
    }

    @GetMapping("/{pedidoId}")
    public ResponseEntity<ApiResponse> listarPedido(@PathVariable Long pedidoId){
        PedidoResponseDTO pedido = pedidoService.listarPedido(pedidoId);
        return ResponseEntity.ok(new ApiResponse("Sucesso", pedido));
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<ApiResponse> listarPedidosPorUsuario(@PathVariable Long usuarioId){
        List<PedidoResponseDTO> pedidos = pedidoService.listarPedidosPorUsuario(usuarioId);
        return ResponseEntity.ok(new ApiResponse("Sucesso", pedidos));
    }
}
