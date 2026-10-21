package io.github.joaogclima30.carrinhoAPI.controller;

import io.github.joaogclima30.carrinhoAPI.dto.UsuarioDTO.*;
import io.github.joaogclima30.carrinhoAPI.response.ApiResponse;
import io.github.joaogclima30.carrinhoAPI.service.Usuario.UsuarioService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/usuarios")
public class UsuarioController {
    private final UsuarioService service;

    @PostMapping
    public ResponseEntity<ApiResponse> criarUsuario(@Valid @RequestBody UsuarioCriarDTO dto) {
        return ResponseEntity.ok(new ApiResponse("Usuário criado com sucesso", service.criarUsuarioDto(dto)));
    }

    @GetMapping("/{usuarioId}/listarUsuario")
    public ResponseEntity<ApiResponse> listarUsuario(@PathVariable @Positive Long usuarioId) {
        return ResponseEntity.ok(new ApiResponse("Sucesso", service.listarUsuarioDto(usuarioId)));
    }

    @PutMapping("/{usuarioId}/atualizar")
    public ResponseEntity<ApiResponse> atualizarUsuario(@PathVariable @Positive Long usuarioId, @Valid @RequestBody UsuarioAtualizarDTO dto) {
        return ResponseEntity.ok(new ApiResponse("Usuário atualizado", service.atualizarUsuarioDto(dto, usuarioId)));
    }

    @DeleteMapping("/{usuarioId}/deletar")
    public ResponseEntity<ApiResponse> deletarUsuario(@PathVariable @Positive Long usuarioId) {
        service.deletarUsuario(usuarioId);
        return ResponseEntity.ok(new ApiResponse("Usuário excluído", null));
    }
}
