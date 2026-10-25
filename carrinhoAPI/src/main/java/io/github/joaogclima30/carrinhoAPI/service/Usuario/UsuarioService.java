package io.github.joaogclima30.carrinhoAPI.service.Usuario;

import io.github.joaogclima30.carrinhoAPI.dto.UsuarioDTO.*;
import io.github.joaogclima30.carrinhoAPI.exceptions.ConflitoException;
import io.github.joaogclima30.carrinhoAPI.exceptions.ExceptionsUsuario.UsuarioNaoEncontrado;
import io.github.joaogclima30.carrinhoAPI.mapper.DtoMapper;
import io.github.joaogclima30.carrinhoAPI.model.Usuario;
import io.github.joaogclima30.carrinhoAPI.repository.*;
import jakarta.persistence.EntityExistsException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Validated
@Transactional(readOnly = true)
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final PedidoRepository pedidoRepository;
    private final DtoMapper mapper;
    private final PasswordEncoder passwordEncoder;

    public Usuario listarUsuarioPorId(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new UsuarioNaoEncontrado("Usuário não encontrado"));
    }

    public Usuario getUserById(Long usuarioId) {
        return listarUsuarioPorId(usuarioId);
    }

    @Transactional
    public Usuario criarUsuario(@Valid UsuarioCriarDTO dto) {
        return Optional.of(dto)
                .filter(usuarioCriarDTO -> !usuarioRepository.existsByEmail(dto.getEmail()))
                .map(req -> {
                    Usuario usuario = new Usuario ();
                    usuario.setEmail(dto.getEmail());
                    usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
                    usuario.setPrimeiroNome(dto.getPrimeiroNome());
                    usuario.setSobrenome(dto.getSobrenome());
                    return usuarioRepository.save(usuario);
                }).orElseThrow(() -> new EntityExistsException("Oops!" + dto.getEmail() + "Ja existe"));

    }

    @Transactional
    public UsuarioResponseDTO criarUsuarioDto(@Valid UsuarioCriarDTO dto) {
        return mapper.usuario(criarUsuario(dto));
    }

    public UsuarioResponseDTO listarUsuarioDto(Long id) {
        return mapper.usuario(listarUsuarioPorId(id));
    }

    @Transactional
    public Usuario atualizarUsuario(@Valid UsuarioAtualizarDTO dto, Long id) {
        Usuario usuario = listarUsuarioPorId(id);
        usuario.setPrimeiroNome(dto.getPrimeiroNome());
        usuario.setSobrenome(dto.getSobrenome());
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public UsuarioResponseDTO atualizarUsuarioDto(@Valid UsuarioAtualizarDTO dto, Long id) {
        return mapper.usuario(atualizarUsuario(dto, id));
    }

    @Transactional
    public void deletarUsuario(Long id) {
        Usuario usuario = usuarioRepository.buscarParaAtualizar(id)
                .orElseThrow(() -> new UsuarioNaoEncontrado("Usuário não encontrado"));
        if (pedidoRepository.existsByUsuarioId(id)) {
            throw new ConflitoException("Usuário possui pedidos e não pode ser excluído");
        }
        usuarioRepository.delete(usuario);
    }

    public Usuario getAuthenticatedUsuario(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return usuarioRepository.findByEmail(email);

    }
}
