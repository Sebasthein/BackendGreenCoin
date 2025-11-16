package com.example.reciclaje.servicio;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.reciclaje.entidades.Logro;
import com.example.reciclaje.entidades.Usuario;
import com.example.reciclaje.entidades.UsuarioLogro;
import com.example.reciclaje.repositorio.LogroRepositorio;
import com.example.reciclaje.repositorio.UsuarioLogroRepositorio;
import com.example.reciclaje.repositorio.UsuarioRepositorio;
import com.example.reciclaje.servicioDTO.LogroDTO;

import jakarta.transaction.Transactional;

@Service
public class LogroServicio {
	
	 private final LogroRepositorio logroRepositorio;
	    private final UsuarioRepositorio usuarioRepositorio;
	    private final UsuarioLogroRepositorio usuarioLogroRepositorio;

	    @Autowired
	    public LogroServicio(LogroRepositorio logroRepositorio, UsuarioRepositorio usuarioRepositorio, UsuarioLogroRepositorio usuarioLogroRepositorio) {
	        this.logroRepositorio = logroRepositorio;
	        this.usuarioRepositorio = usuarioRepositorio;
	        this.usuarioLogroRepositorio = usuarioLogroRepositorio;
	    }


	    public Page<Logro> obtenerTodosLogros(Pageable pageable) {
	        return logroRepositorio.findAll(pageable);
	    }
	    
	    public Logro obtenerLogroPorId(Long id) {
	        return logroRepositorio.findById(id)
	                .orElseThrow(() -> new RuntimeException("logro no encontrado"));
	    }

	    

	    public Logro crearLogro(Logro logro) {
	        return logroRepositorio.save(logro);
	    }
	    
	  
	    @Transactional
	    public void desbloquearLogro(Long usuarioId, Long logroId) {
	        if (!usuarioLogroRepositorio.existsByUsuarioIdAndLogroId(usuarioId, logroId)) {
	            Usuario usuario = usuarioRepositorio.findById(usuarioId)
	                                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
	            Logro logro = obtenerLogroPorId(logroId);
	            
	            UsuarioLogro usuarioLogro = new UsuarioLogro();
	            usuarioLogro.setUsuario(usuario);
	            usuarioLogro.setLogro(logro);
	            usuarioLogro.setFechaObtencion(LocalDateTime.now());
	            
	            usuarioLogroRepositorio.save(usuarioLogro);
	        }
	    }
	    
	    
	    
	    public List<Logro> obtenerLogrosDesbloqueados(Long usuarioId) {
	        return usuarioLogroRepositorio.findByUsuarioId(usuarioId).stream()
	                .map(UsuarioLogro::getLogro)
	                .collect(Collectors.toList());
	    }
	    
	    

	    public Logro actualizarLogro(Long id, Logro logroActualizado) {
	        return logroRepositorio.findById(id)
	                .map(logro -> {
	                    logro.setNombre(logroActualizado.getNombre());
	                    logro.setDescripcion(logroActualizado.getDescripcion());
	                    logro.setImagenTrofeo(logroActualizado.getImagenTrofeo());
	                    return logroRepositorio.save(logro);
	                })
	                .orElseGet(() -> {
	                    logroActualizado.setId(id);
	                    return logroRepositorio.save(logroActualizado);
	                });
	    }

	    public void eliminarLogro(Long id) {
	        logroRepositorio.deleteById(id);
	    }

	    

	    public List<Logro> obtenerLogrosPorUsuario(Long usuarioId) {
	        List<UsuarioLogro> usuarioLogros = usuarioLogroRepositorio.findByUsuarioId(usuarioId);
	        return usuarioLogros.stream()
	                            .map(UsuarioLogro::getLogro)
	                            .collect(Collectors.toList());
	    }

	    
	    
	    
	    
	    
	   
}
