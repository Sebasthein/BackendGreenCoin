package com.example.reciclaje.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.reciclaje.entidades.Reciclaje;
import com.example.reciclaje.entidades.Usuario;
import com.example.reciclaje.repositorio.ReciclajeRepositorio;
import com.example.reciclaje.servicio.ReciclajeServicio;
import com.example.reciclaje.servicio.UsuarioServicio;
import com.example.reciclaje.servicioDTO.ActividadDTO;

@Controller
public class HistorialController {
	
	   @Autowired
	    private ReciclajeServicio reciclajeServicio;

	    @Autowired
	    private UsuarioServicio usuarioServicio;
	    
	    @Autowired
	    private ReciclajeRepositorio reciclajeRepository ;
	    

	    @GetMapping("/historial")
	    public String verHistorial(Model model, Principal principal) {
	        Usuario usuario = usuarioServicio.findByEmail(principal.getName());
	        List<ActividadDTO> historial = reciclajeServicio.obtenerHistorialDelUsuario(usuario.getId());
	        model.addAttribute("historial", historial);
	        return "historial";
	    }

}
