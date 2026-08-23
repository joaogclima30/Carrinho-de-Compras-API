package io.github.joaogclima30.carrinhoAPI.dto.UsuarioDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UsuarioCriarDTO {
    @NotBlank(message = "Primeiro nome é obrigatório")
    private String primeiroNome;

    @NotBlank(message = "Sobrenome é obrigatório")
    private String sobrenome;

    @NotBlank @Email(message = "Email inválido")
    private String email;

    @NotBlank @Size(min = 8, message = "Senha deve ter no mínimo 8 caracteres")
    private String senha;
}
