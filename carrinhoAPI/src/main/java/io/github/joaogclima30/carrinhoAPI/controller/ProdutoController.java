package io.github.joaogclima30.carrinhoAPI.controller;

import io.github.joaogclima30.carrinhoAPI.dto.ProdutoDTO.ProdutoRequestDTO;
import io.github.joaogclima30.carrinhoAPI.model.Categoria;
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
        return ResponseEntity.ok(new ApiResponse("Produto salvo com sucesso!", produtoSalvo));
    }

    @PutMapping("/{produtoId}/atualizar")
    public ResponseEntity<ApiResponse> atualizarProduto(@RequestBody ProdutoRequestDTO requestDTO, @PathVariable Long produtoId){
        Produto produto = produtoService.atualizarProduto(requestDTO, produtoId);
        return ResponseEntity.ok(new ApiResponse("Atualizado com Sucesso!", produto));
    }

    @DeleteMapping("/{produtoId}/deletar")
    public ResponseEntity<ApiResponse> deletarProduto(@PathVariable Long produtoId){
        produtoService.deletarProduto(produtoId);
        return ResponseEntity.ok(new ApiResponse("Deletado com sucesso", null));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse> listarTodosProdutos(){
        List<Produto> produtos = produtoService.listarTodosProdutos();
        List<ProdutoRequestDTO> produtosConvertidos = produtoService.listarProdutosConvertidos(produtos);
        return ResponseEntity.ok(new ApiResponse("Sucesso!", produtosConvertidos));
    }

    @GetMapping("/{produtoId}/listarPorId")
    public ResponseEntity<ApiResponse> listarProdutoPorId(@PathVariable Long produtoId){
        Produto produto = produtoService.obterPorId(produtoId);
        var produtoDto = produtoService.converterParaDto(produto);
        return ResponseEntity.ok(new ApiResponse("Sucesso!", produtoDto));
    }

    @GetMapping("/by/marca-and-nomeProduto")
    public ResponseEntity<ApiResponse> listarProdutosByMarcaAndNome(@RequestParam String marca, @RequestParam String nomeProduto){
        List<Produto> produtos = produtoService.listarProdutoPorMarcaAndNome(marca,nomeProduto);
        List<ProdutoRequestDTO> produtosConvertidos = produtoService.listarProdutosConvertidos(produtos);
        return ResponseEntity.ok(new ApiResponse("Sucesso", produtosConvertidos));
    }

    @GetMapping("/by/categoria-and-marca")
    public ResponseEntity<ApiResponse> listarProdutosByCategoriaAndMarca(@RequestParam Categoria categoria, @RequestParam String marca){
        List<Produto> produtos = produtoService.listarProdutoPorCategoriaAndMarca(categoria, marca);
        return ResponseEntity.ok(new ApiResponse("Sucesso", produtos));
    }

    @GetMapping("/{nome}/produtos")
    public ResponseEntity<ApiResponse> listarProdutosByNome(@PathVariable String nome){
        List<Produto> produtos = produtoService.listarProdutoPorNome(nome);
        return ResponseEntity.ok(new ApiResponse("Sucesso", produtos));
    }

    @GetMapping("/produto/by-marca")
    public ResponseEntity<ApiResponse> listarProdutoByMarca(@RequestParam String marca){
        List<Produto> produtos = produtoService.listarProdutosPorMarca(marca);
        return ResponseEntity.ok(new ApiResponse("Sucesso", produtos));
    }

    @GetMapping("/produto/{categoria}/all/produtos")
    public ResponseEntity<ApiResponse> listarProdutoByCategoria(@PathVariable String categoria){
        List<Produto> produtos = produtoService.listarProdutosPorCategoria(categoria);
        return ResponseEntity.ok(new ApiResponse("Sucesso", produtos));
    }

    @GetMapping("/produto/contador/by-brand/and-name")
    public ResponseEntity<ApiResponse> contadorProdutosByMarcaAndNome(@RequestParam String marca, @RequestParam String nome){
        var contadorProduto = produtoService.contadorProdutosPorMarcaAndNome(marca, nome);
        return ResponseEntity.ok(new ApiResponse("Produto contado", contadorProduto));
    }
}