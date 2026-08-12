package io.github.joaogclima30.carrinhoAPI.repository;

import io.github.joaogclima30.carrinhoAPI.model.Categoria;
import io.github.joaogclima30.carrinhoAPI.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    Optional<Produto> findById(Long id);

    List<Produto> findByCategoriaNome(String categoria);

    List<Produto> findByMarca(String marca);

    List<Produto> findByCategoriaAndMarca(String categoria, String marca);

    List<Produto> findByNome(String nome);

    List<Produto> findByMarcaAndNome(String marca, String nome);

    Long countByMarcaAndNome(String marca, String nome);

    boolean existsByNomeAndMarca (String nome, String marca);

}
