package io.github.joaogclima30.carrinhoAPI.service.Usuario;

import io.github.joaogclima30.carrinhoAPI.dto.UsuarioDTO.UsuarioAtualizarDTO;
import io.github.joaogclima30.carrinhoAPI.dto.UsuarioDTO.UsuarioCriarDTO;
import io.github.joaogclima30.carrinhoAPI.exceptions.ExceptionsProdutos.ProdutoNaoEncontrado;
import io.github.joaogclima30.carrinhoAPI.exceptions.ExceptionsUsuario.UsuarioNaoEncontrado;
import io.github.joaogclima30.carrinhoAPI.model.Produto;
import io.github.joaogclima30.carrinhoAPI.model.Usuario;
import io.github.joaogclima30.carrinhoAPI.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final ModelMapper modelMapper;

    public Usuario listarUsuarioPorId(Long usuarioId){
        return usuarioRepository.findById(usuarioId).orElseThrow(() -> new UsuarioNaoEncontrado("Usuario não Existente"));
    }

    public Usuario criarUsuario(UsuarioCriarDTO usuarioCriarDTO) {
        Usuario usuario = new Usuario(
                usuarioCriarDTO.getPrimeiroNome(),
                usuarioCriarDTO.getSobrenome(),
                usuarioCriarDTO.getEmail(),
                usuarioCriarDTO.getSenha()
        );
        return usuarioRepository.save(usuario);
    }

    public Usuario atualizarUsuario(UsuarioAtualizarDTO usuarioAtualizarDTO, Long usuarioId){
        return usuarioRepository.findById(usuarioId)
                .map(existeUsuario -> {
                    existeUsuario.setPrimeiroNome(usuarioAtualizarDTO.getPrimeiroNome());
                    existeUsuario.setSobrenome(usuarioAtualizarDTO.getSobrenome());
                    return usuarioRepository.save(existeUsuario);
                }).orElseThrow(() -> new UsuarioNaoEncontrado("Não existe usuario"));
    }

    public void deletarUsuario(Long usuarioId){
        usuarioRepository.deleteById(usuarioId);
    }

    public UsuarioCriarDTO converterParaDto(Usuario usuario){
        UsuarioCriarDTO usuarioCriarDTO = modelMapper.map(usuario,UsuarioCriarDTO.class);
        return usuarioCriarDTO;
    }

    public UsuarioAtualizarDTO converterParaDtoAtualizar(Usuario usuario){
        UsuarioAtualizarDTO usuarioAtualizarDTO = modelMapper.map(usuario,UsuarioAtualizarDTO.class);
        return usuarioAtualizarDTO;
    }
}
