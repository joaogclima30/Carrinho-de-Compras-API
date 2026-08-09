package io.github.joaogclima30.carrinhoAPI.controller;

import io.github.joaogclima30.carrinhoAPI.dto.ProdutoDTO.ProdutoRequestDTO;
import io.github.joaogclima30.carrinhoAPI.model.Produto;
import io.github.joaogclima30.carrinhoAPI.response.ApiResponse;
import io.github.joaogclima30.carrinhoAPI.service.Produto.ProdutoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/produtos")
public class ProdutoController {

    private final ProdutoService produtoService;

    @PostMapping("/salvar")
    public ResponseEntity<ApiResponse> salvarProduto(@RequestBody ProdutoRequestDTO produtoRequestDTO){
        Produto produtoSalvo = produtoService.salvarProduto(produtoRequestDTO);

    }
    @GetMapping("/all")
    public ResponseEntity<ApiResponse> listarTodosProdutos(){
        List<Produto> produto = produtoService.listarTodosProdutos();
        return ResponseEntity.ok(new ApiResponse("Sucesso", produto));
    }

    @GetMapping("/produto/{produtoId}/produto")
    public ResponseEntity<ApiResponse> listarProdutoPorId(@PathVariable Long produtoId){
        Optional<Produto> produto = produtoService.obterPorId(produtoId);
        return ResponseEntity.ok(new ApiResponse("Sucesso", produto));
    }

}
