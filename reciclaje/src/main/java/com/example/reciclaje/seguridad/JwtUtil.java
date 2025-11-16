package com.example.reciclaje.seguridad;

import java.util.Date;

import javax.crypto.SecretKey;

import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.security.core.Authentication;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {
	
	private final SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);
	    
	    @Value("${jwt.expiration:36000000}") // 10 horas por defecto
	    private long expiration;
	    
	    public String generateToken(Authentication authentication) {
	        String username = authentication.getName();

	        Date now = new Date();
	        Date expiryDate = new Date(now.getTime() + expiration);

	        return Jwts.builder()
	                .setSubject(username)
	                .setIssuedAt(now)
	                .setExpiration(expiryDate)
	                .signWith(secretKey) // ✅ No necesita especificar algoritmo
	                .compact();
	    }

/**
 * 1. Valida el Token JWT: Verifica la firma y si no ha expirado.
 * @param authToken El token JWT
 * @return true si el token es válido y no expirado, false en caso contrario.
 */
	    public boolean validateToken(String authToken) {
	        try {
	            Jwts.parserBuilder()
	                .setSigningKey(secretKey) // ✅ CORREGIDO: usa secretKey directamente
	                .build()
	                .parseClaimsJws(authToken);
	            return true;
	        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
	            System.err.println("Token JWT inválido: " + e.getMessage());
	        } catch (ExpiredJwtException e) {
	            System.err.println("Token JWT ha expirado: " + e.getMessage());
	        } catch (UnsupportedJwtException e) {
	            System.err.println("Token JWT no soportado: " + e.getMessage());
	        } catch (IllegalArgumentException e) {
	            System.err.println("Cadena de claims JWT vacía: " + e.getMessage());
	        }
	        return false;
	    }

/**
 * 2. Obtiene el nombre de usuario (subject) del Token.
 * @param token El token JWT
 * @return El email del usuario (subject)
 */
	    public String getUsernameFromToken(String token) {
	        return Jwts.parserBuilder()
	            .setSigningKey(secretKey) // ✅ CORREGIDO: usa secretKey directamente
	            .build()
	            .parseClaimsJws(token)
	            .getBody()
	            .getSubject();
	    }
}
