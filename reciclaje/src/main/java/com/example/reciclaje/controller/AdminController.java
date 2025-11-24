package com.example.reciclaje.controller;


import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map; // ✅ ESTE ES EL QUE FALTA
import java.util.HashMap;

import com.example.reciclaje.entidades.EstadoReciclaje;
import com.example.reciclaje.entidades.Reciclaje;
import com.example.reciclaje.entidades.Usuario;
import com.example.reciclaje.entidades.UsuarioRol;
import com.example.reciclaje.repositorio.ReciclajeRepositorio;
import com.example.reciclaje.servicio.ReciclajeServicio;
import com.example.reciclaje.servicio.UsuarioServicio;

import org.springframework.ui.Model;

@Controller
@RequestMapping("/admin")
public class AdminController {

  
	@Autowired
    private ReciclajeServicio reciclajeServicio;
    
    @Autowired
    private UsuarioServicio usuarioServicio;
    
    @Autowired
    private ReciclajeRepositorio reciclajeRepositorio;
    
    @GetMapping("/reciclajes/pendientes")
    public List<Reciclaje> getReciclajesPendientes(Principal principal) {
        // Misma lógica de verificación de admin
        if (!esAdministrador(principal)) {
            throw new AccessDeniedException("No tiene permisos de administrador");
        }
        return reciclajeServicio.obtenerReciclajesPendientes();
    }
    

  
    
    // 🔐 Verificar si el usuario es administrador
    private boolean esAdministrador(Principal principal) {
        if (principal == null) return false;
        
        try {
            Usuario usuario = usuarioServicio.findByEmail(principal.getName());
            if (usuario == null) return false;
            
            // Verificar roles de manera segura
            for (UsuarioRol usuarioRol : usuario.getUsuarioRoles()) {
                if (usuarioRol.getActivo() != null && usuarioRol.getActivo() &&
                    usuarioRol.geRol() != null && 
                    "ADMIN".equals(usuarioRol.geRol().getNombre())) {
                    return true;
                }
            }
            return false;
            
        } catch (Exception e) {
            System.err.println("Error verificando rol de administrador: " + e.getMessage());
            return false;
        }
    }
    
    // 🏠 Dashboard del administrador
    @GetMapping("/dashboard")
    public String dashboardAdmin(Model model, Principal principal) {
        if (!esAdministrador(principal)) {
            return "redirect:/access-denied";
        }
        
        Map<String, Object> estadisticas = reciclajeServicio.obtenerEstadisticasAdmin();
        model.addAttribute("estadisticas", estadisticas);
        model.addAttribute("admin", usuarioServicio.findByEmail(principal.getName()));
        
        return "admin/dashboard";
    }
    
    // 📋 Validaciones pendientes
    @GetMapping("/validaciones")
    public String validacionesPendientes(Model model, Principal principal) {
        if (!esAdministrador(principal)) {
            return "redirect:/access-denied";
        }
        
        List<Reciclaje> pendientes = reciclajeServicio.obtenerReciclajesPendientes();
        model.addAttribute("reciclajesPendientes", pendientes);
        model.addAttribute("admin", usuarioServicio.findByEmail(principal.getName()));
        
        // Debug para ver qué reciclajes se están cargando
        System.out.println("📸 Reciclajes pendientes encontrados: " + pendientes.size());
        for (Reciclaje r : pendientes) {
            System.out.println("ID: " + r.getId() + ", Imagen URL: " + r.getImagenUrl());
        }
        
        return "admin/validaciones";
    }
    
    // ✅ Validar (aprobar) reciclaje
    @PostMapping("/reciclajes/{id}/validar")
    public String validarReciclaje(@PathVariable Long id, Principal principal) {
        if (!esAdministrador(principal)) {
            return "redirect:/access-denied";
        }
        
        reciclajeServicio.validarReciclaje(id, principal.getName());
        return "redirect:/admin/validaciones?success=validado";
    }
    
    
    // ❌ Rechazar reciclaje
    @PostMapping("/reciclajes/{id}/rechazar")
    public String rechazarReciclaje(@PathVariable Long id, 
            @RequestParam String motivo, 
            Principal principal) {
if (!esAdministrador(principal)) {
return "redirect:/access-denied";
}

reciclajeServicio.rechazarReciclaje(id, principal.getName(), motivo);
return "redirect:/admin/validaciones?success=rechazado";
}
    
    // 📊 Historial de reciclajes
    @GetMapping("/reciclajes")
    public String historialReciclajes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String estado,
            Model model, Principal principal) {
        
        if (!esAdministrador(principal)) {
            return "redirect:/access-denied";
        }
        
        Page<Reciclaje> paginaReciclajes;
        if (estado != null && !estado.isEmpty()) {
            EstadoReciclaje estadoEnum = EstadoReciclaje.valueOf(estado.toUpperCase());
            List<Reciclaje> reciclajes = reciclajeServicio.obtenerReciclajesPorEstado(estadoEnum);
            model.addAttribute("reciclajes", reciclajes);
        } else {
            Pageable pageable = PageRequest.of(page, size, Sort.by("fechaReciclaje").descending());
            paginaReciclajes = reciclajeRepositorio.findAll(pageable);
            model.addAttribute("reciclajes", paginaReciclajes.getContent());
            model.addAttribute("paginaActual", page);
            model.addAttribute("totalPaginas", paginaReciclajes.getTotalPages());
        }
        
        model.addAttribute("admin", usuarioServicio.findByEmail(principal.getName()));
        model.addAttribute("estados", EstadoReciclaje.values());
        model.addAttribute("estadoFiltro", estado);
        
        return "admin/reciclajes";
    }
    
    // 👥 Gestión de usuarios (opcional)
    @GetMapping("/usuarios")
    public String gestionUsuarios(Model model, Principal principal) {
        if (!esAdministrador(principal)) {
            return "redirect:/access-denied";
        }
        
        List<Usuario> usuarios = usuarioServicio.obtenerTodosUsuarios();
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("admin", usuarioServicio.findByEmail(principal.getName()));
        
        return "admin/usuarios";
    }
}
