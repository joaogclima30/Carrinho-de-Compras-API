package io.github.joaogclima30.carrinhoAPI.dto.UsuarioDTO;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.Locale;

@Data
public class UsuarioCriarDTO {
    @NotBlank @Size(max = 255)
    private String primeiroNome;
    @NotBlank @Size(max = 255)
    private String sobrenome;
    @NotBlank @Email @Size(max = 255)
    private String email;
    @NotBlank @Size(min = 8, max = 255)
    private String senha;

    public void setEmail(String email) {
        this.email = email == null ? null : email.strip().toLowerCase(Locale.ROOT);
    }
}
