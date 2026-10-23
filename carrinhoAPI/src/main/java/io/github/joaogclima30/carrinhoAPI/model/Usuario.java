package io.github.joaogclima30.carrinhoAPI.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
@Entity
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String primeiroNome;
    private String sobrenome;

    @Email
    @Column(nullable = false, unique = true)
    private String email;

    private String senha;

    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private Carrinho carrinho;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pedido> pedidos;

    public void setEmail(String email) {
        this.email = email == null ? null : email.strip().toLowerCase(java.util.Locale.ROOT);
    }

    @PrePersist
    @PreUpdate
    private void normalizarEmail() {
        setEmail(email);
    }

    public Usuario(String primeiroNome, String sobrenome, String email, String senha) {
        this.primeiroNome = primeiroNome;
        this.sobrenome = sobrenome;
        setEmail(email);
        this.senha = senha;
    }

        @ManyToMany(fetch = FetchType.EAGER, cascade =
                {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
        @JoinTable(name = "usuario_roles",  joinColumns = @JoinColumn(name = "usuario_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id")
        )
    private Collection<Role> roles = new HashSet<>();
}
