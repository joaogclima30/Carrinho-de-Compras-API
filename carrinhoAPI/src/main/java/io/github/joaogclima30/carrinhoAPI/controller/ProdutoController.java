package io.github.joaogclima30.carrinhoAPI.controller;

import io.github.joaogclima30.carrinhoAPI.dto.ProdutoDTO.ProdutoRequestDTO;
import io.github.joaogclima30.carrinhoAPI.response.ApiResponse;
import io.github.joaogclima30.carrinhoAPI.service.Produto.ProdutoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/produtos")
public class ProdutoController {
    private final ProdutoService service;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/salvar")
    public ResponseEntity<ApiResponse> salvarProduto(@Valid @RequestBody ProdutoRequestDTO dto) {
        return ok("Produto salvo com sucesso", service.salvarProdutoDto(dto));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{produtoId}/atualizar")
    public ResponseEntity<ApiResponse> atualizarProduto(@Valid @RequestBody ProdutoRequestDTO dto, @PathVariable @Positive Long produtoId) {
        return ok("Produto atualizado", service.atualizarProdutoDto(dto, produtoId));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{produtoId}/deletar")
    public ResponseEntity<ApiResponse> deletarProduto(@PathVariable @Positive Long produtoId) {
        service.deletarProduto(produtoId);
        return ok("Produto excluído", null);
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse> listarTodosProdutos() {
        return ok("Sucesso", service.listarTodosDto()); }


    @GetMapping("/{produtoId}/listarPorId")
    public ResponseEntity<ApiResponse> listarProdutoPorId(@PathVariable @Positive Long produtoId) {
        return ok("Sucesso", service.listarPorIdDto(produtoId)); }


    @GetMapping("/by/marca-and-nomeProduto")
    public ResponseEntity<ApiResponse> listarProdutosByMarcaAndNome(@RequestParam String marca, @RequestParam String nomeProduto) {
        return ok("Sucesso", service.listarPorMarcaENomeDto(marca, nomeProduto)); }


    @GetMapping("/by/categoria-and-marca")
    public ResponseEntity<ApiResponse> listarProdutosByCategoriaAndMarca(@RequestParam String categoria, @RequestParam String marca) {
        return ok("Sucesso", service.listarPorCategoriaEMarcaDto(categoria, marca)); }


    @GetMapping("/{nome}/produtos")
    public ResponseEntity<ApiResponse> listarProdutosByNome(@PathVariable String nome) {
        return ok("Sucesso", service.listarPorNomeDto(nome)); }


    @GetMapping("/produto/by-marca")
    public ResponseEntity<ApiResponse> listarProdutoByMarca(@RequestParam String marca) {
        return ok("Sucesso", service.listarPorMarcaDto(marca)); }


    @GetMapping("/produto/{categoria}/all/produtos")
    public ResponseEntity<ApiResponse> listarProdutoByCategoria(@PathVariable String categoria) {
        return ok("Sucesso", service.listarPorCategoriaDto(categoria)); }


    @GetMapping("/produto/contador/by-brand/and-nome")
    public ResponseEntity<ApiResponse> contadorProdutosByMarcaAndNome(@RequestParam String marca, @RequestParam String nome) {
        return ok("Sucesso", service.contadorProdutosPorMarcaAndNome(marca, nome)); }


    private ResponseEntity<ApiResponse> ok(String mensagem, Object data) {
        return ResponseEntity.ok(new ApiResponse(mensagem, data)); }
}
