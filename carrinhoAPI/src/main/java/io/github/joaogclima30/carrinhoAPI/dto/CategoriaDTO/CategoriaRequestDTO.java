package io.github.joaogclima30.carrinhoAPI.dto.CategoriaDTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoriaRequestDTO {

    @NotBlank(message = "Nome da Categoria Obrigatorio")
    private String nome;
}
