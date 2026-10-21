package io.github.joaogclima30.carrinhoAPI.controller;

import io.github.joaogclima30.carrinhoAPI.dto.UsuarioDTO.UsuarioAtualizarDTO;
import io.github.joaogclima30.carrinhoAPI.dto.UsuarioDTO.UsuarioCriarDTO;
import io.github.joaogclima30.carrinhoAPI.model.Usuario;
import io.github.joaogclima30.carrinhoAPI.response.ApiResponse;
import io.github.joaogclima30.carrinhoAPI.service.Usuario.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.github.joaogclima30.carrinhoAPI.mapper.DtoMapper;
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
        var usuarioCriarDTO = DtoMapper.usuario(usuario);
        return ResponseEntity.ok(new ApiResponse("Usuário criado com sucesso", usuarioCriarDTO));
    }

    @GetMapping("/{usuarioId}/listarUsuario")
    public ResponseEntity<ApiResponse> listarUsuario(@PathVariable Long usuarioId){
        Usuario usuario = usuarioService.listarUsuarioPorId(usuarioId);
        var usuarioCriarDTO = DtoMapper.usuario(usuario);
        return ResponseEntity.ok(new ApiResponse("Sucesso", usuarioCriarDTO));
    }

    @PutMapping("/{usuarioId}/atualizar")
    public ResponseEntity<ApiResponse> atualizarUsuario(@PathVariable Long usuarioId, @Valid @RequestBody UsuarioAtualizarDTO dto){
        Usuario usuario = usuarioService.atualizarUsuario(dto, usuarioId);
        UsuarioAtualizarDTO usuarioAtualizar = usuarioService.converterParaDtoAtualizar(usuario);
        return ResponseEntity.ok(new ApiResponse("Usuário atualizado", usuarioAtualizar));
    }

    @DeleteMapping("/{usuarioId}/deletar")
    public ResponseEntity<ApiResponse> deletarUsuario(@PathVariable Long usuarioId){
        usuarioService.deletarUsuario(usuarioId);
        return ResponseEntity.ok(new ApiResponse("Usuário deletado", null));
    }
}
