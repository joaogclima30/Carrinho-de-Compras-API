package io.github.joaogclima30.carrinhoAPI.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;


    public Role(String nome) {
        this.nome = nome;
    }

    @ManyToMany(mappedBy = "roles")
    private Collection<Usuario> usuarios = new HashSet<>();
}
