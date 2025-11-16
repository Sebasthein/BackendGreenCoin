package com.example.reciclaje.seguridad;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.reciclaje.servicio.UserDetailsServiceImpl;


@Configuration
@EnableMethodSecurity
public class SecurityConfig {

	 private final UserDetailsServiceImpl userDetailsService;
	 

	    public SecurityConfig(UserDetailsServiceImpl userDetailsService) {
	        this.userDetailsService = userDetailsService;
	    }

	    @Bean
	    public DaoAuthenticationProvider authenticationProvider() {
	        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
	        authProvider.setUserDetailsService(userDetailsService);
	        authProvider.setPasswordEncoder(passwordEncoder());
	        return authProvider;
	    }

	    @Bean
	    public AuthenticationManager authenticationManager(
	            AuthenticationConfiguration authenticationConfiguration) throws Exception {
	        return authenticationConfiguration.getAuthenticationManager();
	    }
	    
	  
	    
	 
	    @Bean
	    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
	        http
	            .csrf(csrf -> csrf.disable())
	            .authorizeHttpRequests(auth -> auth
	                .requestMatchers("/api/auth/login","/login", "/registro","/api/**","/api/usuarios/registro", "/css/**", "/js/**","/img/**", "/plugins/**").permitAll() // Permitir acceso a estas rutas
	                .requestMatchers("/admin/**").hasRole("ADMIN")
	                .anyRequest().authenticated() // El resto requiere autenticación
	            )
	            .formLogin(form -> form
	                    .loginPage("/login")
	                    .loginProcessingUrl("/login") // Asegúrate que coincida con el action del formulario
	                    .usernameParameter("username") // Asegúrate que coincida con tu formulario
	                    .passwordParameter("password") // Asegúrate que coincida con tu formulario
	                    .defaultSuccessUrl("/dashboard", true)
	                    .failureUrl("/login?error=true") // Para manejar errores
	                    .permitAll()
	                )
	                .exceptionHandling(exception -> exception
	                    .accessDeniedPage("/acceso-denegado")
	                )
	                .logout(logout -> logout
	                    .logoutUrl("/logout")
	                    .logoutSuccessUrl("/login?logout=true")
	                    .permitAll()
	                );
	        
	     // Agregar el filtro JWT para validar el token en cada petición REST protegida
	        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
	        
	            return http.build();
	    }

	    @Bean
	    public PasswordEncoder passwordEncoder() {
	        return new BCryptPasswordEncoder();
	    }
	    @Bean
	    public JwtAuthenticationFilter jwtAuthenticationFilter() {
	        return new JwtAuthenticationFilter();
	    }
	    
	    
	}