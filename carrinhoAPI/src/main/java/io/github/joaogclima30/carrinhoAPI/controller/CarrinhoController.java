package io.github.joaogclima30.carrinhoAPI.controller;

import io.github.joaogclima30.carrinhoAPI.model.Carrinho;
import io.github.joaogclima30.carrinhoAPI.response.ApiResponse;
import io.github.joaogclima30.carrinhoAPI.service.Carrinho.CarrinhoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/carrinhos")
public class CarrinhoController {

    private final CarrinhoService carrinhoService;

    @GetMapping("/{carrinhoId}/listarCarrinho")
    public ResponseEntity<ApiResponse> listarCarrinho(@PathVariable Long carrinhoId){
        Carrinho carrinho = carrinhoService.listarCarrinho(carrinhoId);
        return ResponseEntity.ok(new ApiResponse("Sucesso", carrinho));
    }

    @DeleteMapping("/{carrinhoId}/limparCarrinho")
    public ResponseEntity<ApiResponse> limparCarrinho(@PathVariable Long carrinhoId){
        carrinhoService.limparCarrinho(carrinhoId);
        return ResponseEntity.ok(new ApiResponse("Carrinho limpo com Sucesso",null));
    }

    @GetMapping("/{carrinhoId}/total")
    public ResponseEntity<ApiResponse> listarQuantidadeTotal(@PathVariable Long carrinhoId){
        BigDecimal precoTotal = carrinhoService.precoTotal(carrinhoId);
        return ResponseEntity.ok(new ApiResponse("Preço total ",precoTotal));
    }
}
