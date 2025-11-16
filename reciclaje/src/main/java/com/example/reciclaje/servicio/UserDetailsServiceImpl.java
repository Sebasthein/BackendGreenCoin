package com.example.reciclaje.servicio;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.reciclaje.entidades.Rol;
import com.example.reciclaje.entidades.Usuario;
import com.example.reciclaje.entidades.UsuarioRol;
import com.example.reciclaje.repositorio.UsuarioRepositorio;
import com.example.reciclaje.seguridad.CustomUserDetails;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

	 private final UsuarioRepositorio usuarioRepository;

	 @Override
	 public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
	     Usuario usuario = usuarioRepository.findByEmail(email)
	         .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));

	     // Obtener los authorities (roles)
	     Collection<? extends GrantedAuthority> authorities = usuario.getUsuarioRoles().stream()
	         .map(usuarioRol -> new SimpleGrantedAuthority("ROLE_" + usuarioRol.geRol().getNombre()))
	         .collect(Collectors.toList());

	     // Retornar CustomUserDetails con el usuario completo y authorities
	     return new CustomUserDetails(usuario, authorities);
	 }
	

    private Collection<? extends GrantedAuthority> getAuthorities(Set<Rol> roles) {
        return roles.stream()
            .map(rol -> new SimpleGrantedAuthority("ROLE_" + rol.getNombre()))
            .collect(Collectors.toList());
    }
}
