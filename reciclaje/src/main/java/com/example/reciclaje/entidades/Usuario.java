package com.example.reciclaje.entidades;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    private String nombre;
    
    @EqualsAndHashCode.Include
    private String email;
    private String password;
    private int puntos;
    
    
    
    @Column(name = "direccion")
    private String direccion;

    @Column(name = "telefono")
    private String telefono;

    @Column(name = "avatar_id")
    private String avatarId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "nivel_id")
    private Nivel nivel;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL , fetch = FetchType.EAGER)
    private List<Reciclaje> reciclajes = new ArrayList<>();

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true,fetch = FetchType.EAGER)
    private Set<UsuarioRol> usuarioRoles = new HashSet<>();
    
    

    // Relación con Logros - Versión corregida

    @OneToMany(mappedBy = "usuario")
    private Set<UsuarioLogro> usuarioLogros = new HashSet<>();
    
   

    

    // Métodos de UserDetails
    
 // Método para actualizar los puntos
    public void actualizarPuntos() {
        this.puntos = this.reciclajes.stream()
                .mapToInt(Reciclaje::getPuntosGanados)
                .sum();
    }
    
    // Método getter para la vista
    public int getPuntos() {
        return this.puntos;
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();
        for (UsuarioRol usuarioRol : usuarioRoles) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + usuarioRol.geRol().getNombre()));
        }
        return authorities;
    }

    @Override
    public String getUsername() {
        return email;
    }

    public void setPuntos(int puntos){
    	
    	this.puntos = puntos;
    	actualizarPuntos();
    	
    }


    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }


   

}