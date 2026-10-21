package io.github.joaogclima30.carrinhoAPI.controller;

import io.github.joaogclima30.carrinhoAPI.model.Carrinho;
import io.github.joaogclima30.carrinhoAPI.model.CarrinhoItem;
import io.github.joaogclima30.carrinhoAPI.model.Usuario;
import io.github.joaogclima30.carrinhoAPI.response.ApiResponse;
import io.github.joaogclima30.carrinhoAPI.service.Carrinho.CarrinhoItemService;
import io.github.joaogclima30.carrinhoAPI.service.Carrinho.CarrinhoService;
import io.github.joaogclima30.carrinhoAPI.service.Usuario.UsuarioService;
import lombok.RequiredArgsConstructor;
import io.github.joaogclima30.carrinhoAPI.mapper.DtoMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/carrinhoItem")
public class CarrinhoItemController {

    private final CarrinhoItemService carrinhoItemService;
    private final CarrinhoService carrinhoService;
    private final UsuarioService usuarioService;

    @PostMapping("/adicionarItem")
    public ResponseEntity<ApiResponse> adicionarItem(@RequestParam(required = false) Long carrinhoId,
                                                     @RequestParam Long produtoId,
                                                     @RequestParam Integer quantidade){
        Usuario usuario = usuarioService.getUserById(1);
        Carrinho carrinho= carrinhoService.initializeNovoCarrinho(usuario);


        carrinhoItemService.salvarItem(carrinho.getId(), produtoId, quantidade);
        return ResponseEntity.ok(new ApiResponse("Item adicionado", null));
    }

    @DeleteMapping("/{carrinhoId}/item/{produtoId}/remover")
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

    @GetMapping("/{carrinhoId}/item/{produtoId}/listarCarrinho")
    public ResponseEntity<ApiResponse> listarItensCarrinhos(@PathVariable Long carrinhoId,
                                                            @PathVariable Long produtoId){
       CarrinhoItem carrinhoEncontrado = carrinhoItemService.listarCarrinhoItem(carrinhoId,produtoId);
       return ResponseEntity.ok(new ApiResponse("Carrinho Listado", DtoMapper.item(carrinhoEncontrado)));
    }
}
