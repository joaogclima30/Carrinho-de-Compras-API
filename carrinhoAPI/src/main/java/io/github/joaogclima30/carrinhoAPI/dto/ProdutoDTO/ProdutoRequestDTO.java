package io.github.joaogclima30.carrinhoAPI.dto.ProdutoDTO;

import io.github.joaogclima30.carrinhoAPI.dto.CategoriaDTO.CategoriaRequestDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProdutoRequestDTO {
    @NotBlank @Size(max = 255)
    private String nome;
    @NotBlank @Size(max = 255)
    private String marca;
    @NotNull @Positive @Digits(integer = 17, fraction = 2)
    private BigDecimal preco;
    @NotNull @PositiveOrZero
    private Integer estoque;
    @Size(max = 255)
    private String descricao;
    @NotNull @Valid
    private CategoriaRequestDTO categoria;
}
