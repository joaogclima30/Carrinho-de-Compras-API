package io.github.joaogclima30.carrinhoAPI.controller;

import io.github.joaogclima30.carrinhoAPI.dto.CategoriaDTO.CategoriaRequestDTO;
import io.github.joaogclima30.carrinhoAPI.response.ApiResponse;
import io.github.joaogclima30.carrinhoAPI.service.Categoria.CategoriaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/categorias")
public class CategoriaController {
    private final CategoriaService service;
    @GetMapping("/todas")
    public ResponseEntity<ApiResponse> listarTodasCategorias() { return ok(service.listarTodasDto()); }
    @PostMapping("/salvar")
    public ResponseEntity<ApiResponse> salvarCategorias(@Valid @RequestBody CategoriaRequestDTO dto) { return ok(service.salvarCategoriaDto(dto)); }
    @PutMapping("/categoria/{id}/atualizar")
    public ResponseEntity<ApiResponse> atualizarCategoria(@PathVariable @Positive Long id, @Valid @RequestBody CategoriaRequestDTO dto) { return ok(service.atualizarCategoriaDto(dto, id)); }
    @DeleteMapping("/categoria/{id}/delete")
    public ResponseEntity<ApiResponse> deletarCategoria(@PathVariable @Positive Long id) { service.deletarCategoriaPorId(id); return ok(null); }
    @GetMapping("/categoria/{id}")
    public ResponseEntity<ApiResponse> listarCategoriaPorId(@PathVariable @Positive Long id) { return ok(service.listarPorIdDto(id)); }
    @GetMapping("/categoria/{nome}/procurar")
    public ResponseEntity<ApiResponse> listarCategoriaPorNome(@PathVariable String nome) { return ok(service.listarPorNomeDto(nome)); }
    private ResponseEntity<ApiResponse> ok(Object data) { return ResponseEntity.ok(new ApiResponse("Sucesso", data)); }
}
