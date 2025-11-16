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

import com.example.reciclaje.seguridad.CustomUserDetails;
import com.example.reciclaje.servicio.UsuarioServicio;

import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class DashboardController {

	 private final UsuarioServicio usuarioServicio;

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