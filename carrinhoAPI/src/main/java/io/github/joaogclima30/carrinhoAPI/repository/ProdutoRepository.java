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

    boolean existsByNomeAndMarcaAndIdNot(String nome, String marca, Long id);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select p from Produto p where p.id in :ids order by p.id")
    List<Produto> buscarParaAtualizarEstoque(@org.springframework.data.repository.query.Param("ids") List<Long> ids);

    List<Produto> findByCategoriaNome(String categoria);

    List<Produto> findByMarca(String marca);

    List<Produto> findByCategoriaNomeAndMarca(String categoriaNome, String marca);

    List<Produto> findByNome(String nome);

    List<Produto> findByMarcaAndNome(String marca, String nome);

    Long countByMarcaAndNome(String marca, String nome);

    boolean existsByNomeAndMarca (String nome, String marca);

}
