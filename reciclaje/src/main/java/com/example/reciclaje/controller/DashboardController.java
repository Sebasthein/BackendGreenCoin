package com.example.reciclaje.controller;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.reciclaje.entidades.Reciclaje;
import com.example.reciclaje.entidades.Usuario;
import com.example.reciclaje.repositorio.UsuarioLogroRepositorio;
import com.example.reciclaje.seguridad.CustomUserDetails;
import com.example.reciclaje.servicio.ReciclajeServicio;
import com.example.reciclaje.servicio.UsuarioServicio;
import com.example.reciclaje.servicioDTO.ActividadDTO;
import com.example.reciclaje.servicioDTO.DashboardResponse;

import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class DashboardController {

	 private final UsuarioServicio usuarioServicio;
	    private final ReciclajeServicio reciclajeServicio; // Faltaba esto
	    private final UsuarioLogroRepositorio usuarioLogroRepositorio;
	 

	    /**
	     * Muestra el dashboard del usuario autenticado.
	     */
	    @GetMapping("auth/dashboard")
	    public String mostrarDashboard(Authentication authentication, Model model) {
	        if (authentication == null || !authentication.isAuthenticated()) {
	            return "redirect:/login";
	        }

	        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

	        // Aquí puedes obtener más datos reales del usuario
	        String email = userDetails.getUsername();
	        int puntos = usuarioServicio.obtenerPuntosPorEmail(email); // Método sugerido

	        model.addAttribute("email", email);
	        model.addAttribute("points", puntos);
	    

	        return "dashboard"; // Vista: src/main/resources/templates/dashboard.html
	    }
	    
	    
	    //maui
	    
	    @GetMapping("/api/dashboard/datos")
	    public ResponseEntity<?> obtenerDatosDashboard(Authentication authentication) {
	        try {
	            String email = authentication.getName();
	            Usuario usuario = usuarioServicio.findByEmail(email);

	            // 1. Actualizar Nivel
	            Usuario usuarioActualizado = usuarioServicio.actualizarNivelUsuario(usuario);
	            
	            // 2. Obtener Reciclajes
	            List<Reciclaje> reciclajes = reciclajeServicio.obtenerReciclajesPorUsuario(usuario.getId());
	            
	            // 3. Calcular Estadísticas
	            long totalReciclajes = reciclajes.size();
	            long diasActivos = reciclajeServicio.contarDiasActivosPorUsuario(usuario.getId());
	            Long cantidadLogros = usuarioLogroRepositorio.countByUsuarioId(usuario.getId());
	            int ranking = usuarioServicio.obtenerPosicionRanking(usuario.getPuntos());

	            // 4. Convertir Reciclajes a ActividadDTO (Solo las ultimas 5)
	            List<ActividadDTO> actividades = reciclajes.stream()
	                .sorted((a, b) -> b.getFechaReciclaje().compareTo(a.getFechaReciclaje()))
	                .limit(5)
	                .map(r -> new ActividadDTO(
	                    "Reciclaje de " + r.getMaterial().getNombre() + " (" + r.getCantidad() + ")",
	                    r.getPuntosGanados(),
	                    r.getFechaReciclaje()
	                ))
	                .toList();

	            // 5. Llenar el Response
	            DashboardResponse response = new DashboardResponse();
	            response.setUsuarioNombre(usuario.getNombre());
	            
	            // Corrección Nivel: Obtenemos el nombre, no el objeto
	            response.setNivelActual(usuario.getNivel() != null ? usuario.getNivel().getNombre() : "Semilla"); 
	            
	            // Lógica de Avatar
	            String avatar = usuario.getAvatarId();
	            if (avatar == null || avatar.isEmpty()) {
	                 // Avatar por defecto si no tiene
	                 avatar = "https://api.dicebear.com/7.x/bottts/png?seed=" + usuario.getEmail();
	            } else if (!avatar.startsWith("http")) {
	                 // Si es local, le agregamos la ruta
	                 avatar = "/uploads/" + avatar;
	            }
	            response.setAvatarUrl(avatar);
	            
	            response.setDireccion(usuario.getDireccion());
	            response.setPuntosTotales(usuario.getPuntos());
	            response.setRanking(ranking);
	            response.setTotalReciclajes(totalReciclajes);
	            response.setLogrosDesbloqueados(cantidadLogros);
	            response.setDiasActivos(diasActivos);
	            response.setActividadesRecientes(actividades);

	            return ResponseEntity.ok(response);

	        } catch (Exception e) {
	            e.printStackTrace();
	            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
	        }
	    }
	    
	    //--------------------
	    
	    @GetMapping("/api/dashboard")
	    public ResponseEntity<Map<String, Object>> getDashboardData(Authentication authentication) {
	        Map<String, Object> response = new HashMap<>();
	        
	        System.out.println("=== DASHBOARD API CALLED ===");
	        System.out.println("Authentication: " + authentication);
	        System.out.println("Is Authenticated: " + (authentication != null && authentication.isAuthenticated()));
	        
	        if (authentication == null || !authentication.isAuthenticated()) {
	            response.put("success", false);
	            response.put("error", "No autenticado");
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
	        }
	        
	        // Tu lógica del dashboard aquí...
	        response.put("success", true);
	        response.put("message", "Dashboard data");
	        
	        return ResponseEntity.ok(response);
	    }
	    
	    
}