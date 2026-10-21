package io.github.joaogclima30.carrinhoAPI.dto.ProdutoDTO;

import io.github.joaogclima30.carrinhoAPI.dto.CategoriaDTO.CategoriaResponseDTO;
import io.github.joaogclima30.carrinhoAPI.dto.ImagemDTO.ImagemDTO;
import java.math.BigDecimal;
import java.util.List;

public record ProdutoResponseDTO(Long id, String nome, String marca, BigDecimal preco,
        int estoque, String descricao, CategoriaResponseDTO categoria, List<ImagemDTO> imagensDto) {}
