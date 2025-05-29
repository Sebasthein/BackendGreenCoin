package com.example.reciclaje.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
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

import com.example.reciclaje.entidades.Reciclaje;
import com.example.reciclaje.entidades.Usuario;
import com.example.reciclaje.servicio.LogroServicio;
import com.example.reciclaje.servicio.ReciclajeServicio;
import com.example.reciclaje.servicio.UsuarioServicio;
import com.example.reciclaje.servicioDTO.LoginRequest;
import com.example.reciclaje.servicioDTO.RegistroRequest;
import com.example.reciclaje.servicioDTO.UsuarioDTO;

import jakarta.validation.Valid;

@Controller
public class AuthController {

	private final UsuarioServicio usuarioServicio;
	private final AuthenticationManager authenticationManager;
	private final ReciclajeServicio reciclajeServicio;
	private  LogroServicio logroServicio;

	@Autowired
	public AuthController(UsuarioServicio usuarioServicio, AuthenticationManager authenticationManager, ReciclajeServicio reciclajeServicio) {
		this.usuarioServicio = usuarioServicio;
		this.authenticationManager = authenticationManager;
		this.reciclajeServicio = reciclajeServicio;
	}

	// 👉 Mostrar formulario de login
	@GetMapping("/login")
	public String mostrarLogin(Model model) {
		model.addAttribute("loginRequest", new LoginRequest());
		return "login";
	}

	// 👉 Procesar inicio de sesión
	@PostMapping("/login")
	public String procesarLogin(@ModelAttribute("loginRequest") LoginRequest loginRequest, Model model) {
		try {
			Authentication auth = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
			SecurityContextHolder.getContext().setAuthentication(auth);
			return "redirect:/dashboard";
		} catch (Exception e) {
			model.addAttribute("error", "Credenciales inválidas");
			return "login";
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
	 Usuario usuario = usuarioServicio.findByEmail(principal.getName());
	 
	 int logros = usuario.getUsuarioLogros().size();
	 
	 long totalReciclajes = reciclajeServicio.obtenerReciclajesPorUsuario(usuario.getId()).size();
	 long diasActivos = reciclajeServicio.contarDiasActivosPorUsuario(usuario.getId());
	 
	 Usuario usuarioActualizado = usuarioServicio.actualizarNivelUsuario(usuario);
	 //Actualizar Puntos
	 List<Reciclaje> reciclajes = reciclajeServicio.obtenerReciclajesPorUsuario(usuario.getId()); 
     int puntos = reciclajes.stream()
             .mapToInt(Reciclaje::getPuntosGanados)
             .sum();
	 
	 usuarioServicio.agregarPuntos(usuario.getId(),puntos);
	 //System.out.println(usuario.toString());
	 // Convertir a DTO si es necesario o pasar la entidad directamente
	 
	model.addAttribute("usuario", usuario);
	model.addAttribute("logros",logros);
	model.addAttribute("totalReciclajes", reciclajes.size());
	model.addAttribute("diasActivos", diasActivos);
	model.addAttribute("usuario", usuarioActualizado);

	
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
