package io.github.joaogclima30.carrinhoAPI.repository;

import io.github.joaogclima30.carrinhoAPI.dto.PedidoDTO.PedidoResponseDTO;
import io.github.joaogclima30.carrinhoAPI.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByUsuarioId(Long usuarioId);

    boolean existsByUsuarioId(Long usuarioId);
}
