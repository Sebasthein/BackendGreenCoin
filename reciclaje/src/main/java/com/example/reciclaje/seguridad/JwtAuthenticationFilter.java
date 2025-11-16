package com.example.reciclaje.seguridad;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.reciclaje.servicio.UserDetailsServiceImpl;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
	// Asumiendo que tu UserDetailsService está implementado (ej. CustomUserDetailsService)
    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    // Este es el método principal que se ejecuta en cada petición
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        try {
            // 1. Obtener el Token JWT del encabezado
            String jwt = getJwtFromRequest(request);

            // 2. Validar el token y obtener el usuario
            if (StringUtils.hasText(jwt) && jwtUtil.validateToken(jwt)) {
                String username = jwtUtil.getUsernameFromToken(jwt);

                // 3. Cargar los detalles del usuario
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                // 4. Crear el objeto de Autenticación
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 5. Establecer la Autenticación en el contexto de seguridad
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            logger.error("No se pudo establecer la autenticación del usuario", ex);
        }

        filterChain.doFilter(request, response);
    }

    // Método auxiliar para extraer el token del encabezado "Authorization: Bearer <token>"
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Quita "Bearer "
        }
        return null;
    }

}
