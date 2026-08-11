package io.github.joaogclima30.carrinhoAPI.repository;

import io.github.joaogclima30.carrinhoAPI.model.Imagem;
import io.github.joaogclima30.carrinhoAPI.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImagemRepository extends JpaRepository<Imagem, Long> {

    Optional<Imagem> findById(Long id);

    List<Imagem> findByProdutoId(long id);
}
