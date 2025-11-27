package com.example.reciclaje.seguridad;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.example.reciclaje.servicio.UserDetailsServiceImpl;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	// Asumiendo que tu UserDetailsService está implementado (ej. CustomUserDetailsService)
    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    // Este es el método principal que se ejecuta en cada petición
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
            HttpServletResponse response, 
            FilterChain filterChain)
throws ServletException, IOException {

try {
// 🔥 NO procesar JWT para endpoints públicos
String path = request.getServletPath();
if (path.startsWith("/api/auth/") ||
path.startsWith("/auth/") ||
path.equals("/login") ||
path.equals("/registro") ||
path.startsWith("/css/") ||
path.startsWith("/js/") ||
path.startsWith("/img/") ||
path.startsWith("/plugins/") ||
path.startsWith("/uploads/") ||
path.startsWith("/api/reciclajes/imagen/")) {
filterChain.doFilter(request, response);
return;
}

String token = getTokenFromRequest(request);

if (token == null) {
   sendJsonError(response, "Token JWT no proporcionado");
   return;
}

if (!jwtUtil.validateToken(token)) {
   sendJsonError(response, "Token JWT inválido");
   return;
}

String username = jwtUtil.getUsernameFromToken(token);
UserDetails userDetails = userDetailsService.loadUserByUsername(username);

UsernamePasswordAuthenticationToken authentication =
  new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

SecurityContextHolder.getContext().setAuthentication(authentication);
} catch (Exception e) {
logger.error("Cannot set user authentication: {}", e);
sendJsonError(response, "Error de autenticación");
}

filterChain.doFilter(request, response);
}

private String getTokenFromRequest(HttpServletRequest request) {
String bearerToken = request.getHeader("Authorization");
if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
return bearerToken.substring(7);
}
return null;
}

    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        // Excluir la ruta de registro del filtro de validación de token
        return request.getRequestURI().equals("/api/registro") || 
               request.getRequestURI().equals("/api/auth/login");
    }

    // Método auxiliar para extraer el token del encabezado "Authorization: Bearer <token>"
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Quita "Bearer "
        }
        return null;
    }

    private void sendJsonError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", message);
        errorResponse.put("status", 401);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

}
