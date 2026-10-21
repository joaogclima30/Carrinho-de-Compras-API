package io.github.joaogclima30.carrinhoAPI.controller;

import io.github.joaogclima30.carrinhoAPI.dto.CategoriaDTO.CategoriaRequestDTO;
import io.github.joaogclima30.carrinhoAPI.model.Categoria;
import io.github.joaogclima30.carrinhoAPI.response.ApiResponse;
import io.github.joaogclima30.carrinhoAPI.service.Categoria.CategoriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.github.joaogclima30.carrinhoAPI.mapper.DtoMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    @GetMapping("/todas")
    public ResponseEntity<ApiResponse> listarTodasCategorias(){
        List<Categoria> categorias = categoriaService.pesquisarTodasCategorias();
        return ResponseEntity.ok(new ApiResponse("Encontrado!", categorias.stream().map(DtoMapper::categoria).toList()));
    }

    @PostMapping("/salvar")
    public ResponseEntity<ApiResponse> salvarCategorias(@Valid @RequestBody CategoriaRequestDTO dto){
        Categoria categoriaSalva = categoriaService.salvarCategoria(new Categoria(dto.getNome()));
        return ResponseEntity.ok(new ApiResponse("Salvo com Sucesso!", DtoMapper.categoria(categoriaSalva)));
    }

    @PutMapping("/categoria/{id}/atualizar")
    public ResponseEntity<ApiResponse> atualizarCategoria(@PathVariable Long id, @Valid @RequestBody CategoriaRequestDTO categoria){
        Categoria categoriaEncontrada = categoriaService.atualizarCategoria(categoria, id);
        return ResponseEntity.ok(new ApiResponse("Atualizado com Sucesso!", DtoMapper.categoria(categoriaEncontrada)));
    }

    @DeleteMapping("/categoria/{id}/delete")
    public ResponseEntity<ApiResponse> deletarCategoria(@PathVariable Long id){
        categoriaService.deletarCategoriaPorId(id);
        return ResponseEntity.ok(new ApiResponse("Deletado!", null));
    }

    @GetMapping("/categoria/{id}")
    public ResponseEntity<ApiResponse> listarCategoriaPorId(@PathVariable Long id){
        Categoria categoriaEncontrada = categoriaService.categoriaPorId(id);
        return ResponseEntity.ok(new ApiResponse("Encontrado!", DtoMapper.categoria(categoriaEncontrada)));

    }

    @GetMapping("/categoria/{nome}/procurar")
    public ResponseEntity<ApiResponse> listarCategoriaPorNome(@PathVariable String nome){
        Categoria categoriaEncontrada = categoriaService.categoriaPorNome(nome).orElseThrow(() -> new io.github.joaogclima30.carrinhoAPI.exceptions.ExceptionsCategoria.CategoriaNaoEncontrada("Não existe essa categoria"));
        return ResponseEntity.ok(new ApiResponse("Encontrado!", DtoMapper.categoria(categoriaEncontrada)));
    }

}
