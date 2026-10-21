package io.github.joaogclima30.carrinhoAPI.service.Categoria;

import io.github.joaogclima30.carrinhoAPI.dto.CategoriaDTO.CategoriaRequestDTO;
import io.github.joaogclima30.carrinhoAPI.exceptions.ExceptionsCategoria.CategoriaNaoEncontrada;
import io.github.joaogclima30.carrinhoAPI.model.Categoria;
import io.github.joaogclima30.carrinhoAPI.repository.CategoriaRepository;
import io.github.joaogclima30.carrinhoAPI.validator.CategoriaValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@org.springframework.transaction.annotation.Transactional(readOnly = true)
@RequiredArgsConstructor
@org.springframework.validation.annotation.Validated
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final io.github.joaogclima30.carrinhoAPI.mapper.DtoMapper mapper;
    private final CategoriaValidator categoriaValidator;


    @org.springframework.transaction.annotation.Transactional
    public Categoria salvarCategoria(@jakarta.validation.Valid Categoria categoria){
        categoriaValidator.validarCategoria(categoria);
        return categoriaRepository.save(categoria);
    }

    @org.springframework.transaction.annotation.Transactional
    public Categoria atualizarCategoria(@jakarta.validation.Valid CategoriaRequestDTO dto, Long id){
        Categoria categoria = categoriaPorId(id);
        categoriaValidator.validarAtualizacao(dto.getNome(), id);
        categoria.setNome(dto.getNome());
        return categoriaRepository.save(categoria);
    }

    @org.springframework.transaction.annotation.Transactional
    public void deletarCategoriaPorId(Long id){
        categoriaRepository.delete(categoriaPorId(id));
    }

    public Categoria categoriaPorId(Long id){
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new CategoriaNaoEncontrada("Não existe essa categoria"));
    }

    public Optional<Categoria> categoriaPorNome(String nome){
        return categoriaRepository.findByNome(nome);
    }

    public List<Categoria> pesquisarTodasCategorias(){
        return categoriaRepository.findAll();
    }

    @org.springframework.transaction.annotation.Transactional
    public io.github.joaogclima30.carrinhoAPI.dto.CategoriaDTO.CategoriaResponseDTO salvarCategoriaDto(@jakarta.validation.Valid CategoriaRequestDTO dto) {
        return mapper.categoria(salvarCategoria(new Categoria(dto.getNome())));
    }
    @org.springframework.transaction.annotation.Transactional
    public io.github.joaogclima30.carrinhoAPI.dto.CategoriaDTO.CategoriaResponseDTO atualizarCategoriaDto(@jakarta.validation.Valid CategoriaRequestDTO dto, Long id) {
        return mapper.categoria(atualizarCategoria(dto, id));
    }
    public io.github.joaogclima30.carrinhoAPI.dto.CategoriaDTO.CategoriaResponseDTO listarPorIdDto(Long id) { return mapper.categoria(categoriaPorId(id)); }
    public io.github.joaogclima30.carrinhoAPI.dto.CategoriaDTO.CategoriaResponseDTO listarPorNomeDto(String nome) {
        return mapper.categoria(categoriaPorNome(nome).orElseThrow(() -> new CategoriaNaoEncontrada("Categoria não encontrada")));
    }
    public List<io.github.joaogclima30.carrinhoAPI.dto.CategoriaDTO.CategoriaResponseDTO> listarTodasDto() { return pesquisarTodasCategorias().stream().map(mapper::categoria).toList(); }
}
