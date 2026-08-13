package io.github.joaogclima30.carrinhoAPI.repository;

import io.github.joaogclima30.carrinhoAPI.dto.CategoriaDTO.CategoriaRequestDTO;
import io.github.joaogclima30.carrinhoAPI.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    Optional<Categoria> findByNome(String name);

    CategoriaRequestDTO save(CategoriaRequestDTO categoriaRequestDTO);

    boolean existsByNome(String nome);

}
