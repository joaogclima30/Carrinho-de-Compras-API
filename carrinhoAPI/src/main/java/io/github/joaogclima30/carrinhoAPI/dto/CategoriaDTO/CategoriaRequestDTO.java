package io.github.joaogclima30.carrinhoAPI.dto.CategoriaDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoriaRequestDTO {

    @NotBlank(message = "Nome da Categoria Obrigatorio")
    @Size(max = 255)
    private String nome;
}
