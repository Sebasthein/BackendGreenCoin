package com.example.reciclaje.repositorio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.reciclaje.entidades.Nivel;
import com.example.reciclaje.entidades.Rol;

public interface NivelRepositorio extends JpaRepository<Nivel, Long> {

	// Obtener el nivel más alto que el usuario puede alcanzar con sus puntos
  /*  @Query("SELECT n FROM Nivel n WHERE n.puntosMinimos <= :puntos ORDER BY n.puntosMinimos DESC LIMIT 1")
    Optional<Nivel> findNivelByPuntos(@Param("puntos") int puntos);*/

    /**
     * Busca un nivel por su cantidad de puntos mínimos requeridos
     * @param puntosMinimos Puntos mínimos del nivel a buscar
     * @return Nivel correspondiente
     */
    //Optional<Nivel> findByPuntosMinimos(int puntosMinimos);
    Optional<Nivel> findTopByPuntosRequeridosLessThanEqualOrderByPuntosRequeridosDesc(int puntos);

	List<Nivel> findAllByOrderByPuntosRequeridosAsc( );
}
