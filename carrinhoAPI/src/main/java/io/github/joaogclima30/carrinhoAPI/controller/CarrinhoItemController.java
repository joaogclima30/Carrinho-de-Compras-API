package io.github.joaogclima30.carrinhoAPI.controller;

import io.github.joaogclima30.carrinhoAPI.model.Carrinho;
import io.github.joaogclima30.carrinhoAPI.model.Usuario;
import io.github.joaogclima30.carrinhoAPI.response.ApiResponse;
import io.github.joaogclima30.carrinhoAPI.service.Carrinho.CarrinhoItemService;
import io.github.joaogclima30.carrinhoAPI.service.Carrinho.CarrinhoService;
import io.github.joaogclima30.carrinhoAPI.service.Usuario.UsuarioService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/carrinhoItem")
public class CarrinhoItemController {
    private final CarrinhoItemService service;
    private final UsuarioService usuarioService;
    private final CarrinhoService carrinhoService;

    @PostMapping("/adicionarItem")
    public ResponseEntity<ApiResponse> adicionarItem(@RequestParam(required = false) @Positive Long usuarioId,
            @RequestParam(required = false) @Positive Long carrinhoId,
            @RequestParam @Positive Long produtoId, @RequestParam @Positive Integer quantidade) {

        Usuario usuario = usuarioService.getAuthenticatedUsuario();
        Carrinho carrinho = carrinhoService.initializeNovoCarrinho(usuario);
        return ResponseEntity.ok(new ApiResponse("Item adicionado",
                service.adicionarItem(usuarioId, carrinhoId, produtoId, quantidade)));
    }

    @DeleteMapping("/{carrinhoId}/item/{produtoId}/remover")
    public ResponseEntity<ApiResponse> removerItem(@PathVariable @Positive Long carrinhoId,
                                                   @PathVariable @Positive Long produtoId) {
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
    public ResponseEntity<ApiResponse> listarItensCarrinhos(@PathVariable @Positive Long carrinhoId,
                                                            @PathVariable @Positive Long produtoId) {
        return ResponseEntity.ok(new ApiResponse("Item listado", service.listarItemDto(carrinhoId, produtoId)));
    }
}
