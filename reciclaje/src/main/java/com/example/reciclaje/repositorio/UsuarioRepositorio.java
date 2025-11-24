package com.example.reciclaje.repositorio;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.reciclaje.entidades.Usuario;

public interface UsuarioRepositorio extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);
	boolean existsByNombre(String nombre);
	
	long countByPuntosGreaterThan(int puntos);
	
}
