package io.github.joaogclima30.carrinhoAPI.repository;

import io.github.joaogclima30.carrinhoAPI.model.Usuario;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    @Query("select count(u) > 0 from Usuario u where lower(trim(u.email)) = lower(trim(:email))")
    boolean existsByEmail(@Param("email") String email);

    @Query("select u from Usuario u where lower(trim(u.email)) = lower(trim(:email))")
    Optional<Usuario> findByEmail(@Param("email") String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from Usuario u where u.id = :id")
    Optional<Usuario> buscarParaAtualizar(@Param("id") Long id);
}
