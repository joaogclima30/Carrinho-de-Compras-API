package io.github.joaogclima30.carrinhoAPI.validator;

import io.github.joaogclima30.carrinhoAPI.exceptions.ExceptionsCategoria.CategoiraJaCriada;
import io.github.joaogclima30.carrinhoAPI.model.Categoria;
import io.github.joaogclima30.carrinhoAPI.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CategoriaValidator {

    private final CategoriaRepository categoriaRepository;

    public void validarCategoria(Categoria categoria){
        boolean duplicado = categoriaRepository.existsByNome(categoria.getNome());
        if(duplicado){
            throw new CategoiraJaCriada("Categoria já cadastrada");
        }
    }

    public boolean existeCategoriaCriada(Categoria categoria){
        if(categoria.getId() == null){
            return false;
        }
        return categoriaRepository.existsById(categoria.getId());
    }

}
