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
@RequiredArgsConstructor
public class CategoriaService {

    public final CategoriaRepository categoriaRepository;
    public final CategoriaValidator categoriaValidator;


    public Categoria salvarCategoria(Categoria categoria){
        categoriaValidator.validarCategoria(categoria);
        return categoriaRepository.save(categoria);
    }

    public Categoria atualizarCategoria(CategoriaRequestDTO dto, Long id){
        Categoria categoria = categoriaPorId(id);
        categoria.setNome(dto.getNome());
        return categoriaRepository.save(categoria);
    }

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
}
