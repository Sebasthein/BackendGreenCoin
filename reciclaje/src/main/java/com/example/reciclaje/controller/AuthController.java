package com.example.reciclaje.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.AuthenticationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.reciclaje.entidades.Logro;
import com.example.reciclaje.entidades.Reciclaje;
import com.example.reciclaje.entidades.Usuario;
import com.example.reciclaje.repositorio.UsuarioLogroRepositorio;
import com.example.reciclaje.repositorio.UsuarioRepositorio;
import com.example.reciclaje.seguridad.CustomUserDetails;
import com.example.reciclaje.seguridad.JwtUtil;
import com.example.reciclaje.servicio.LogroServicio;
import com.example.reciclaje.servicio.ReciclajeServicio;
import com.example.reciclaje.servicio.UsuarioServicio;
import com.example.reciclaje.servicioDTO.ActividadDTO;
import com.example.reciclaje.servicioDTO.LoginRequest;
import com.example.reciclaje.servicioDTO.RegistroRequest;
import com.example.reciclaje.servicioDTO.UsuarioDTO;

import jakarta.validation.Valid;

@Controller
public class AuthController {

	private final JwtUtil jwtUtil; // Necesitarás actualizar el constructor o usar @RequiredArgsConstructor
	private final UsuarioServicio usuarioServicio;
	private final AuthenticationManager authenticationManager;
	private final ReciclajeServicio reciclajeServicio;
	@Autowired
	private  UsuarioLogroRepositorio usuariologroReposiotry;
	private   LogroServicio logroServicio;

	@Autowired
	public AuthController(UsuarioServicio usuarioServicio, AuthenticationManager authenticationManager, ReciclajeServicio reciclajeServicio, JwtUtil jwtUtil) {
		this.usuarioServicio = usuarioServicio;
		this.authenticationManager = authenticationManager;
		this.reciclajeServicio = reciclajeServicio;
		this.jwtUtil = jwtUtil;
		
	}
	
	// Nuevo Endpoint REST para MAUI
	
	@PostMapping("/auth/login")
	public ResponseEntity<Map<String, Object>> autenticarApi(@Valid @RequestBody LoginRequest loginRequest) {
	    Map<String, Object> response = new HashMap<>();
	    
	    System.out.println("🔐🔐🔐🔐🔐🔐🔐🔐🔐🔐🔐🔐🔐🔐🔐🔐");
	    System.out.println("🔐 RECIBIENDO PETICIÓN EN /api/auth/login");
	    System.out.println("🔐 Email: " + loginRequest.getEmail());
	    System.out.println("🔐🔐🔐🔐🔐🔐🔐🔐🔐🔐🔐🔐🔐🔐🔐🔐");
	    
	    try {
	        // 1. Autenticar usando las credenciales
	        Authentication authentication = authenticationManager.authenticate(
	                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
	        
	        SecurityContextHolder.getContext().setAuthentication(authentication);

	        // 2. Generar el Token JWT
	        String token = jwtUtil.generateToken(authentication);

	        // 3. Obtener los datos del usuario para el DTO
	        Usuario usuario = usuarioServicio.findByEmail(loginRequest.getEmail());
	        
	        // 4. Construir la respuesta JSON para MAUI
	        UsuarioDTO usuarioDto = new UsuarioDTO(usuario);
	        
	        response.put("success", true);
	        response.put("token", token);
	        response.put("usuario", usuarioDto);
	        response.put("message", "Login exitoso");
	        
	        System.out.println("✅✅✅✅✅✅✅✅✅✅✅✅✅✅✅✅");
	        System.out.println("✅ LOGIN EXITOSO - Usuario: " + usuario.getEmail());
	        System.out.println("✅ Token generado: " + token.substring(0, Math.min(20, token.length())) + "...");
	        System.out.println("✅✅✅✅✅✅✅✅✅✅✅✅✅✅✅✅");

	        return ResponseEntity.ok(response);

	    } catch (BadCredentialsException e) {
	        System.out.println("❌❌❌❌❌❌❌❌❌❌❌❌❌❌❌❌");
	        System.out.println("❌ CREDENCIALES INVÁLIDAS: " + loginRequest.getEmail());
	        System.out.println("❌❌❌❌❌❌❌❌❌❌❌❌❌❌❌❌");
	        response.put("success", false);
	        response.put("error", "Credenciales inválidas.");
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
	    } catch (Exception e) {
	        System.out.println("💥💥💥💥💥💥💥💥💥💥💥💥💥💥💥💥");
	        System.out.println("💥 ERROR EN LOGIN: " + e.getMessage());
	        e.printStackTrace();
	        System.out.println("💥💥💥💥💥💥💥💥💥💥💥💥💥💥💥💥");
	        response.put("success", false);
	        response.put("error", "Error interno: " + e.getMessage());
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	    }
	}

	// 👉 Mostrar formulario de login
	@GetMapping("/login")
	public String mostrarLogin(Model model) {
		model.addAttribute("loginRequest", new LoginRequest());
		return "login";
	}

	// 👉 Procesar inicio de sesión
	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody LoginRequest request) {
	    try {
	        Authentication authentication = authenticationManager.authenticate(
	            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
	        );
	        
	        SecurityContextHolder.getContext().setAuthentication(authentication);
	        
	        // Ahora puedes obtener CustomUserDetails
	        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
	        Usuario usuario = userDetails.getUsuario(); // Obtienes tu entidad Usuario completa
	        
	        // Retorna la respuesta con los datos del usuario
	        return ResponseEntity.ok(usuario);
	        
	    } catch (Exception e) {
	        return ResponseEntity.badRequest().body("Error en login: " + e.getMessage());
	    }
	}

	// 👉 Mostrar formulario de registro
	@GetMapping("/registro")
	public String mostrarFormularioRegistro(Model model) {
		model.addAttribute("usuario", new Usuario()); // Puedes usar un UsuarioDTO o RegistroRequest aquí si prefieres
		model.addAttribute("registroRequest", new RegistroRequest()); // Asegúrate de que este objeto esté en el modelo
		return "registro";
	}

	// 👉 Procesar registro de usuario (versión para formulario HTML)
	@PostMapping("/registro")
		public String procesarRegistro(@Validated @ModelAttribute("registroRequest") RegistroRequest registroRequest,
		BindingResult result,
		RedirectAttributes redirectAttributes) {

if (result.hasErrors()) {
 return "registro";
}

try {
Usuario usuario = new Usuario();
usuario.setNombre(registroRequest.getNombre());
usuario.setEmail(registroRequest.getEmail());
usuario.setPassword(registroRequest.getPassword());
usuario.setTelefono(registroRequest.getTelefono());
//SSusuario.setDireccion(registroRequest.getDireccion());
// NO es necesario establecer el avatarId aquí.
 // El UsuarioServicio.registrarUsuarioConRol se encargará de asignarlo.
// Si tu RegistroRequest DTO tiene un campo avatarId, puedes eliminarlo
// o simplemente no usarlo aquí, ya que el servicio lo gestionará.

UsuarioDTO nuevoUsuario = usuarioServicio.registrarUsuarioConRol(usuario);
// Autenticar al usuario después del registro
Authentication auth = authenticationManager.authenticate(
new UsernamePasswordAuthenticationToken(
registroRequest.getEmail(),
registroRequest.getPassword()

		)

		);
	 SecurityContextHolder.getContext().setAuthentication(auth);

redirectAttributes.addFlashAttribute("successMessage", "¡Registro exitoso! Bienvenido a GreenCoin");
return "redirect:/dashboard";
} catch (IllegalArgumentException ex) {
redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
// Es mejor redirigir a /registro para que el modelo se reinicialice correctamente
// y los errores de validación se muestren de nuevo si es necesario.
return "redirect:/registro"; 
 } catch (Exception ex) {
	redirectAttributes.addFlashAttribute("errorMessage", "Error en el registro. Por favor, inténtalo de nuevo.");
		 return "redirect:/registro";
		 }
		}

	@PostMapping("/api/registro")
public ResponseEntity<Map<String, Object>> registrarUsuarioApi(@Valid @RequestBody RegistroRequest registroRequest) {
Map<String, Object> response = new HashMap<>();
try {
Usuario usuario = new Usuario();
 usuario.setNombre(registroRequest.getNombre());
usuario.setEmail(registroRequest.getEmail());
 usuario.setPassword(registroRequest.getPassword());
usuario.setTelefono(registroRequest.getTelefono());
// No es necesario establecer el avatarId aquí para el registro API tampoco
// usuario.setAvatarId(registroRequest.getAvatarId()); 

 UsuarioDTO nuevoUsuario = usuarioServicio.registrarUsuarioConRol(usuario);
 response.put("success", true);
 response.put("usuario", nuevoUsuario);
 response.put("message", "Usuario registrado exitosamente");
	 return ResponseEntity.status(HttpStatus.CREATED).body(response);
	} catch (IllegalArgumentException ex) {
	 response.put("success", false);
	response.put("error", ex.getMessage());
	 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	 }
}

	// 👉 Página principal después del login
	@GetMapping("/dashboard")
	public String dashboard(Model model, Principal principal) {
	    // 1. Obtener usuario actual
	    Usuario usuario = usuarioServicio.findByEmail(principal.getName());

	    // 2. Obtener reciclajes
	    List<Reciclaje> reciclajes = reciclajeServicio.obtenerReciclajesPorUsuario(usuario.getId());

	    // 3. Actualizar Nivel (solo verificación, no sumar puntos aquí)
	    Usuario usuarioActualizado = usuarioServicio.actualizarNivelUsuario(usuario);
	    
	    // ⚠️ COMENTADO PARA EVITAR ERRORES: No sumes puntos al ver el dashboard. 
	    // Los puntos se suman en el método de registrar reciclaje.
	    // usuarioServicio.agregarPuntos(usuario.getId(), puntos); 

	    // 4. Calcular Métricas
	    long totalReciclajes = reciclajes.size();
	    long diasActivos = reciclajeServicio.contarDiasActivosPorUsuario(usuario.getId());
	    Long cantidadLogros = usuariologroReposiotry.countByUsuarioId(usuario.getId());

	    // 5. NUEVO: Calcular Posición en Ranking
	    // Necesitas implementar este método en tu servicio (ver paso 2 abajo)
	    int posicionRanking = usuarioServicio.obtenerPosicionRanking(usuario.getPuntos());

	    // 6. Actividades Recientes (Tu código original estaba bien aquí)
	    List<ActividadDTO> actividadesRecientes = reciclajes.stream()
	            .map(r -> new ActividadDTO(
	                "Reciclaje de " + r.getMaterial().getNombre() + " (" + r.getCantidad() + " unidades)",
	                r.getPuntosGanados(),
	                r.getFechaReciclaje()
	            ))
	            .sorted((a, b) -> b.getFecha().compareTo(a.getFecha()))
	            .limit(5) // Limitamos a las ultimas 5 para no saturar
	            .toList();

	    // 7. Enviar datos a la vista (Thymeleaf)
	    model.addAttribute("usuario", usuarioActualizado); // Contiene nombre, nivel, puntos acumulados
	    model.addAttribute("totalReciclajes", totalReciclajes);
	    model.addAttribute("diasActivos", diasActivos);
	    model.addAttribute("cantidadLogros", cantidadLogros);
	    model.addAttribute("posicionRanking", posicionRanking); // Nueva variable
	    model.addAttribute("actividadesRecientes", actividadesRecientes);

	    return "dashboard";
	}

	// 👉 Página de acceso denegado
	@GetMapping("/access-denied")
	public String accesoDenegado() {
		return "acceso-denegado";
	}
	
	//Actualizacion de perfil
	@PostMapping("/perfil/actualizar")
	public String actualizarPerfil(
	        @RequestParam String nombre,
	        @RequestParam String email,
	        @RequestParam(required = false) String telefono,
	        @RequestParam(required = false) String direccion,
	        @RequestParam(required = false) String currentPassword,
	        @RequestParam(required = false) String newPassword,
	        @RequestParam(required = false) String confirmPassword,
	        Principal principal,
	        RedirectAttributes redirectAttributes) {
	    
	    try {
	        // Obtener el usuario actual desde la base de datos
	        Usuario usuarioActual = usuarioServicio.findByEmail(principal.getName());
	        
	        // Actualizar solo los campos permitidos
	        usuarioActual.setNombre(nombre);
	        usuarioActual.setTelefono(telefono);
	        usuarioActual.setDireccion(direccion);
	        
	        // Manejar cambio de contraseña si se proporciona
	        if (currentPassword != null && !currentPassword.isEmpty() &&
	            newPassword != null && !newPassword.isEmpty()) {
	            
	            // Verificar que la contraseña actual sea correcta
	            if (!usuarioServicio.validarPassword(usuarioActual, currentPassword)) {
	                redirectAttributes.addFlashAttribute("error", "La contraseña actual no es correcta");
	                return "redirect:/dashboard";
	            }
	            
	            // Verificar que las nuevas contraseñas coincidan
	            if (!newPassword.equals(confirmPassword)) {
	                redirectAttributes.addFlashAttribute("error", "Las nuevas contraseñas no coinciden");
	                return "redirect:/dashboard";
	            }
	            
	            // Actualizar la contraseña
	            usuarioServicio.cambiarPassword(usuarioActual, newPassword);
	        }
	        
	        // Guardar los cambios
	        usuarioServicio.actualizarUsuario(usuarioActual);
	        
	        redirectAttributes.addFlashAttribute("success", "Perfil actualizado correctamente");
	    } catch (Exception e) {
	        redirectAttributes.addFlashAttribute("error", "Error al actualizar: " + e.getMessage());
	    }

	    return "redirect:/dashboard";
    }
	
	 @GetMapping("/reciclaje")
	    public String vistaReciclaje() {
	        return "reciclaje"; // corresponde a templates/reciclaje.html
	    }

}
