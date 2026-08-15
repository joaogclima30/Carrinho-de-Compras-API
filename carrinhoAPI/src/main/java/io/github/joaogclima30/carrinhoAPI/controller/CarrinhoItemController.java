package io.github.joaogclima30.carrinhoAPI.controller;

import io.github.joaogclima30.carrinhoAPI.response.ApiResponse;
import io.github.joaogclima30.carrinhoAPI.service.Carrinho.CarrinhoItemService;
import io.github.joaogclima30.carrinhoAPI.service.Carrinho.CarrinhoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/carrinhoItem")
public class CarrinhoItemController {

    private final CarrinhoItemService carrinhoItemService;
    private final CarrinhoService carrinhoService;

    @PostMapping("/adicionarItem")
    public ResponseEntity<ApiResponse> adicionarItem(@RequestParam(required = false) Long carrinhoId,
                                                     @RequestParam Long produtoId,
                                                     @RequestParam Integer quantidade){
        Long idFinal = (carrinhoId != null) ? carrinhoId : carrinhoService.initializeNovoCarrinho();
        carrinhoItemService.salvarItem(idFinal, produtoId, quantidade);
        return ResponseEntity.ok(new ApiResponse("Item adicionado", null));
    }

    @GetMapping("/{carrinhoId}/item/{produtoId}/remover")
    public ResponseEntity<ApiResponse> removerItem(@PathVariable Long carrinhoId,
                                                   @PathVariable Long produtoId){
        carrinhoItemService.removerItemDoCarrinho(carrinhoId,produtoId);
        return ResponseEntity.ok(new ApiResponse("Item removido", null));
    }

    @PutMapping("/{carrinhoId}/item/{produtoId}/atualizar")
    public ResponseEntity<ApiResponse> atualizarItemQuantidade(@PathVariable Long carrinhoId,
                                                               @PathVariable Long produtoId,
                                                               @RequestParam Integer quantidade){
        carrinhoItemService.atualizarQuantidadeItem(carrinhoId,produtoId, quantidade);
        return ResponseEntity.ok(new ApiResponse("Item atualizado", null));
    }

}
