package io.github.joaogclima30.carrinhoAPI.controller;

import io.github.joaogclima30.carrinhoAPI.dto.AuthDTO.JwtResponse;
import io.github.joaogclima30.carrinhoAPI.dto.AuthDTO.LoginDTO;
import io.github.joaogclima30.carrinhoAPI.response.ApiResponse;
import io.github.joaogclima30.carrinhoAPI.security.jwt.JwtUtils;
import io.github.joaogclima30.carrinhoAPI.security.user.UsuarioLojaDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login (@Valid @RequestBody LoginDTO loginDTO) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getSenha()));
        String jwt = jwtUtils.generateTokenForUser(authentication);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UsuarioLojaDto usuarioLojaDto = (UsuarioLojaDto) authentication.getPrincipal();
        JwtResponse jwtResponse = new JwtResponse(usuarioLojaDto.getId(), jwt);

        return ResponseEntity.ok(new ApiResponse("Login feito com Sucesso", jwtResponse));
    }
}
