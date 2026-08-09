package io.github.joaogclima30.carrinhoAPI.controller;

import io.github.joaogclima30.carrinhoAPI.model.Categoria;
import io.github.joaogclima30.carrinhoAPI.response.ApiResponse;
import io.github.joaogclima30.carrinhoAPI.service.Categoria.CategoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    @GetMapping("/todas")
    public ResponseEntity<ApiResponse> listarTodasCategorias(){
        List<Categoria> categorias = categoriaService.pesquisarTodasCategorias();
        return ResponseEntity.ok(new ApiResponse("Encontrado!", categorias));
    }

    @PostMapping("/salvar")
    public ResponseEntity<ApiResponse> salvarCategorias(@RequestBody Categoria nome){
        Categoria categoriaEncontrada = categoriaService.salvarCategoria(nome);
        return ResponseEntity.ok(new ApiResponse("Salvo com Sucesso!", categoriaEncontrada));
    }

    @PostMapping("/categoria/{id}/atualizar")
    public ResponseEntity<ApiResponse> atualizarCategoria(@PathVariable Long id, @RequestBody Categoria categoria){
        Categoria categoriaEncontrada = categoriaService.atualizarCategoria(categoria, id);
        return ResponseEntity.ok(new ApiResponse("Atualizado com Sucesso!", categoriaEncontrada));
    }

    @DeleteMapping("/categoria/{id}/delete")
    public ResponseEntity<ApiResponse> deletarCategoria(@PathVariable Long id){
        categoriaService.deletarCategoriaPorId(id);
        return ResponseEntity.ok(new ApiResponse("Deletado!", null));
    }

    @GetMapping("/categoria/{id}/categoria")
    public ResponseEntity<ApiResponse> listarCategoriaPorId(@PathVariable Long id){
        Categoria categoriaEncontrada = categoriaService.categoriaPorId(id);
        return ResponseEntity.ok(new ApiResponse("Encontrado!", categoriaEncontrada));

    }

    @GetMapping("/categoria/{nome}/categoria")
    public ResponseEntity<ApiResponse> listarCategoriaPorNome(@PathVariable String nome){
        Optional<Categoria> categoriaEncontrada = categoriaService.categoriaPorNome(nome);
        return ResponseEntity.ok(new ApiResponse("Encontrado!", categoriaEncontrada));
    }

}
