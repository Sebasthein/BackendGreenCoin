package com.example.reciclaje.repositorio;

import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.reciclaje.entidades.EstadoReciclaje;
import com.example.reciclaje.entidades.Reciclaje;
import com.example.reciclaje.entidades.Usuario;

public interface ReciclajeRepositorio extends JpaRepository<Reciclaje, Long> {
	List<Reciclaje> findByUsuario(Usuario usuario); // Usado en servicio
    // List<Reciclaje> findByFecha(LocalDate fecha); // Eliminar si no se usa
    List<Reciclaje> findByUsuarioId(Long usuarioId); // Usado en servicio
    int countByUsuarioIdAndValidadoTrue(Long usuarioId); // Usado en servicio
    List<Reciclaje> findByValidadoFalse(); // Usado en servicio
    List<Reciclaje> findByEstadoOrderByFechaReciclajeAsc(EstadoReciclaje estado);
    // Encontrar reciclajes por usuario
    List<Reciclaje> findByUsuarioIdOrderByFechaReciclajeDesc(Long usuarioId);
    
    // Contar reciclajes por estado
    Long countByEstado(EstadoReciclaje estado);
    
    // Encontrar reciclajes por usuario y estado
    List<Reciclaje> findByUsuarioIdAndEstadoOrderByFechaReciclajeDesc(Long usuarioId, EstadoReciclaje estado);
    
    // Encontrar todos los reciclajes con paginación
    Page<Reciclaje> findAll(Pageable pageable);
    // Método para encontrar reciclajes por estado
    List<Reciclaje> findByEstado(String estado);
    
    // Método paginado por estado
    Page<Reciclaje> findByEstado(String estado, Pageable pageable);
    
    // Método para reciclajes pendientes
    @Query("SELECT r FROM Reciclaje r WHERE r.estado = 'PENDIENTE'")
    List<Reciclaje> findPendientes();
    
    
    @Query("SELECT COALESCE(SUM(r.puntosGanados), 0) FROM Reciclaje r WHERE r.usuario.id = :usuarioId")
    int sumPuntosByUsuarioId(@Param("usuarioId") Long usuarioId);
    
    @Query("SELECT r FROM Reciclaje r WHERE r.estado = 'PENDIENTE'")
    Page<Reciclaje> findPendientes(Pageable pageable);
    
    
}
