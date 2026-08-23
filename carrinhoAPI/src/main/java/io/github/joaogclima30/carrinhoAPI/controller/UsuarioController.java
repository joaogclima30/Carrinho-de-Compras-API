package io.github.joaogclima30.carrinhoAPI.controller;

import io.github.joaogclima30.carrinhoAPI.dto.UsuarioDTO.UsuarioAtualizarDTO;
import io.github.joaogclima30.carrinhoAPI.dto.UsuarioDTO.UsuarioCriarDTO;
import io.github.joaogclima30.carrinhoAPI.model.Usuario;
import io.github.joaogclima30.carrinhoAPI.response.ApiResponse;
import io.github.joaogclima30.carrinhoAPI.service.Usuario.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<ApiResponse> criarUsuario(@Valid @RequestBody UsuarioCriarDTO dto){
        Usuario usuario = usuarioService.criarUsuario(dto);
        return ResponseEntity.ok(new ApiResponse("Usuário criado com sucesso", usuario));
    }

    @GetMapping("/{usuarioId}/listarUsuario")
    public ResponseEntity<ApiResponse> listarUsuario(@PathVariable Long usuarioId){
        Usuario usuario = usuarioService.listarUsuarioPorId(usuarioId);
        return ResponseEntity.ok(new ApiResponse("Sucesso", usuario));
    }

    @PutMapping("/{usuarioId}/atualizar")
    public ResponseEntity<ApiResponse> atualizarUsuario(@PathVariable Long usuarioId, @Valid @RequestBody UsuarioAtualizarDTO dto){
        Usuario usuario = usuarioService.atualizarUsuario(dto, usuarioId);
        return ResponseEntity.ok(new ApiResponse("Usuário atualizado", usuario));
    }

    @DeleteMapping("/{usuarioId}/deletar")
    public ResponseEntity<ApiResponse> deletarUsuario(@PathVariable Long usuarioId){
        usuarioService.deletarUsuario(usuarioId);
        return ResponseEntity.ok(new ApiResponse("Usuário deletado", null));
    }
}