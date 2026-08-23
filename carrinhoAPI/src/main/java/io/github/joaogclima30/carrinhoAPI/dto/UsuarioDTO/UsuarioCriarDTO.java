package io.github.joaogclima30.carrinhoAPI.dto.UsuarioDTO;

import lombok.Data;

@Data
public class UsuarioCriarDTO {
    private String primeiroNome;
    private String sobrenome;
    private String email;
    private String senha;
}
