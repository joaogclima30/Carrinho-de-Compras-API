package io.github.joaogclima30.carrinhoAPI.repository;

import io.github.joaogclima30.carrinhoAPI.model.Carrinho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CarrinhoRepository extends JpaRepository<Carrinho, Long > {
    Optional<Carrinho> findByUsuarioId(Long usuarioId);

    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Carrinho c where c.id = :id")
    Optional<Carrinho> buscarParaAtualizar(@org.springframework.data.repository.query.Param("id") Long id);

    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Carrinho c where c.usuario.id = :usuarioId")
    Optional<Carrinho> buscarPorUsuarioParaAtualizar(@org.springframework.data.repository.query.Param("usuarioId") Long usuarioId);
}
