package com.example.reciclaje.servicio;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.reciclaje.entidades.Nivel;
import com.example.reciclaje.entidades.Usuario;
import com.example.reciclaje.repositorio.NivelRepositorio;
import com.example.reciclaje.repositorio.UsuarioRepositorio;

@Service
public class NivelServicio {

	 @Autowired
	    private NivelRepositorio nivelRepository;
	
	 @Autowired
	    private UsuarioRepositorio usuarioRepository;
	 
	 
	public Nivel obtenerNivelPorPuntos(int puntos) {
	    return nivelRepository.findTopByPuntosRequeridosLessThanEqualOrderByPuntosRequeridosDesc(puntos)
	                          .orElseThrow(() -> new RuntimeException("No se encontró nivel para los puntos"));
	}

	public void verificarNivelUsuario(Usuario usuario) {
	    try {
	        // Obtener el nuevo nivel basado en los puntos actuales del usuario
	        Nivel nuevoNivel = obtenerNivelPorPuntos(usuario.getPuntos());

	        // Si el nivel actual es diferente al nuevo, actualizar
	        if (!nuevoNivel.equals(usuario.getNivel())) {
	            usuario.setNivel(nuevoNivel);
	            usuarioRepository.save(usuario);

	            // Aquí podrías añadir una notificación o desbloqueo de logro
	            // notificarNuevoNivel(usuario, nuevoNivel);
	        }

	    } catch (RuntimeException e) {
	        // Manejar la excepción si no se encuentra nivel adecuado
	        System.err.println("Error al verificar nivel del usuario: " + e.getMessage());
	        // También podrías lanzar una excepción personalizada
	    }
	}


	public List<Nivel> obtenerTodos() {
        return nivelRepository.findAll();
    }
}
