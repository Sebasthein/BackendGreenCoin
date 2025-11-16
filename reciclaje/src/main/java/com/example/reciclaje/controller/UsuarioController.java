package com.example.reciclaje.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.reciclaje.entidades.Rol;
import com.example.reciclaje.entidades.Usuario;
import com.example.reciclaje.entidades.UsuarioRol;
import com.example.reciclaje.repositorio.RolRepositorio;
import com.example.reciclaje.repositorio.UsuarioRepositorio;
import com.example.reciclaje.seguridad.CustomUserDetails;
import com.example.reciclaje.servicio.UsuarioServicio;
import com.example.reciclaje.servicioDTO.RegistroRequest;
import com.example.reciclaje.servicioDTO.UsuarioDTO;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

	private final PasswordEncoder passwordEncoder;
  //  private final SecurityConfig securityConfig;
	 private final UsuarioServicio usuarioService;
	 private final RolRepositorio rolRepository;
	 private final UsuarioRepositorio usuarioRepository;
	 
	 @PostMapping("/api/registro")
	 @Transactional
	 public ResponseEntity<Map<String, Object>> registrarUsuarioApi(@Valid @RequestBody RegistroRequest registroRequest) {
	     Map<String, Object> response = new HashMap<>();
	     
	    
	     try {
	    	 
	         // Verificar si el email ya existe
	         if (usuarioRepository.existsByEmail(registroRequest.getEmail())) {
	             throw new IllegalArgumentException("El email ya está registrado");
	         }

	      // Debug: Verificar datos recibidos
	         System.out.println("Datos recibidos para registro:");
	            System.out.println("Nombre: " + registroRequest.getNombre());
	            System.out.println("Email: " + registroRequest.getEmail());
	            System.out.println("Dirección: " +registroRequest.getDireccion());
	            System.out.println("Teléfono: " + registroRequest.getTelefono());
	         // Crear el nuevo usuario
	         Usuario usuario = new Usuario();
	         usuario.setNombre(registroRequest.getNombre());
	         usuario.setEmail(registroRequest.getEmail());
	         usuario.setPassword(passwordEncoder.encode(registroRequest.getPassword()));
	         usuario.setTelefono(registroRequest.getTelefono());
	         usuario.setDireccion(registroRequest.getDireccion());
	         usuario.setAvatarId(registroRequest.getAvatarId());
	         usuario.setPuntos(0); // Puntos iniciales
	         
	         // Buscar el nivel inicial (si es necesario)s
	         // nivelRepository.findByPuntosMinimos(0).ifPresent(usuario::setNivel);
	         
	         // Buscar el rol USER
	         Rol rolUser = rolRepository.findByNombre("USER")
	             .orElseThrow(() -> new IllegalArgumentException("El rol USER no existe"));
	         
	         // Crear la relación UsuarioRol
	         UsuarioRol usuarioRol = new UsuarioRol(usuario, rolUser);
	         usuario.getUsuarioRoles().add(usuarioRol);
	         rolUser.getUsuarioRoles().add(usuarioRol); // Opcional pero recomendable para bidireccionalidad

	         
	         // Guardar el usuario (esto debería persistir también la relación)
	         Usuario usuarioGuardado = usuarioRepository.save(usuario);
	         
	         UsuarioDTO nuevoUsuario = new UsuarioDTO(usuarioGuardado);
	         nuevoUsuario.setAvatarId(usuarioGuardado.getAvatarId().toString()); 
	         
	         response.put("success", true);
	         response.put("usuario", nuevoUsuario);
	         response.put("message", "Usuario registrado exitosamente");
	         return ResponseEntity.status(HttpStatus.CREATED).body(response);
	     } catch (IllegalArgumentException ex) {
	         response.put("success", false);
	         response.put("error", ex.getMessage());
	         return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	     } catch (DataIntegrityViolationException ex) {
	         response.put("success", false);
	         response.put("error", "Error de integridad de datos: " + ex.getMostSpecificCause().getMessage());
	         return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	     }
	 }

	// Método auxiliar para listar avatars disponibles
	 private List<String> listarAvatarsDisponibles() {
	     try {
	         Resource[] resources = new PathMatchingResourcePatternResolver()
	             .getResources("classpath:static/img/*.*");
	         
	         return Arrays.stream(resources)
	             .map(Resource::getFilename)
	             .collect(Collectors.toList());
	     } catch (IOException e) {
	         return List.of("default.jpeg"); // Avatar por defecto
	     }
	 }


	    /**
	     * Obtener el perfil de un usuario por su ID.
	     */
	    @GetMapping("/{id:\\d+}")
	    public ResponseEntity<Usuario> obtenerPerfilPorId(@PathVariable Long id) {
	        Usuario usuario = usuarioService.obtenerPerfil(id);
	        return ResponseEntity.ok(usuario);
	    }

	    /**
	     * Obtener el perfil del usuario autenticado desde la sesión.
	     */
	    public ResponseEntity<?> getPerfil() {
	        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	        
	        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
	            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
	            Usuario usuario = userDetails.getUsuario();
	            
	            // Trabajar con tu entidad Usuario
	            return ResponseEntity.ok(usuario);
	        }
	        
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	    }
	    
	}
