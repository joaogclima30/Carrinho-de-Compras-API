package io.github.joaogclima30.carrinhoAPI.controller;

import io.github.joaogclima30.carrinhoAPI.response.ApiResponse;
import io.github.joaogclima30.carrinhoAPI.service.Carrinho.CarrinhoItemService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/carrinhoItem")
public class CarrinhoItemController {
    private final CarrinhoItemService service;

    @PostMapping("/adicionarItem")
    public ResponseEntity<ApiResponse> adicionarItem(@RequestParam(required = false) @Positive Long usuarioId,
            @RequestParam(required = false) @Positive Long carrinhoId,
            @RequestParam @Positive Long produtoId, @RequestParam @Positive Integer quantidade) {
        return ResponseEntity.ok(new ApiResponse("Item adicionado", service.adicionarItem(usuarioId, carrinhoId, produtoId, quantidade)));
    }

    @DeleteMapping("/{carrinhoId}/item/{produtoId}/remover")
    public ResponseEntity<ApiResponse> removerItem(@PathVariable @Positive Long carrinhoId, @PathVariable @Positive Long produtoId) {
        service.removerItemDoCarrinho(carrinhoId, produtoId);
        return ResponseEntity.ok(new ApiResponse("Item removido", null));
    }

    @PutMapping("/{carrinhoId}/item/{produtoId}/atualizar")
    public ResponseEntity<ApiResponse> atualizarItemQuantidade(@PathVariable @Positive Long carrinhoId,
            @PathVariable @Positive Long produtoId, @RequestParam @Positive Integer quantidade) {
        service.atualizarQuantidadeItem(carrinhoId, produtoId, quantidade);
        return ResponseEntity.ok(new ApiResponse("Item atualizado", null));
    }

    @GetMapping("/{carrinhoId}/item/{produtoId}/listarCarrinho")
    public ResponseEntity<ApiResponse> listarItensCarrinhos(@PathVariable @Positive Long carrinhoId, @PathVariable @Positive Long produtoId) {
        return ResponseEntity.ok(new ApiResponse("Item listado", service.listarItemDto(carrinhoId, produtoId)));
    }
}
