package io.github.joaogclima30.carrinhoAPI.dto.UsuarioDTO;

import lombok.Data;

@Data
public class UsuarioAtualizarDTO {
    @jakarta.validation.constraints.NotBlank
    @jakarta.validation.constraints.Size(max = 255)
    private String primeiroNome;
    @jakarta.validation.constraints.NotBlank
    @jakarta.validation.constraints.Size(max = 255)
    private String sobrenome;
}
