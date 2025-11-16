package com.example.reciclaje.repositorio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.reciclaje.entidades.UsuarioLogro;

public interface UsuarioLogroRepositorio extends JpaRepository<UsuarioLogro, Long> {
    List<UsuarioLogro> findByUsuarioId(Long usuarioId);
    
    @Query("SELECT ul FROM UsuarioLogro ul WHERE ul.usuario.id = :usuarioId AND ul.logro.id = :logroId")
    Optional<UsuarioLogro> findByUsuarioAndLogro(@Param("usuarioId") Long usuarioId, @Param("logroId") Long logroId);
    
    boolean existsByUsuarioIdAndLogroId(Long usuarioId, Long logroId);
    
    @Query("SELECT ul FROM UsuarioLogro ul JOIN FETCH ul.logro WHERE ul.usuario.id = :usuarioId")
    List<UsuarioLogro> findFullByUsuarioId(@Param("usuarioId") Long usuarioId);
    
    Long countByUsuarioId(Long usuarioId);
}