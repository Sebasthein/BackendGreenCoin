package com.example.reciclaje.controller;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.reciclaje.entidades.Reciclaje;
import com.example.reciclaje.entidades.Usuario;
import com.example.reciclaje.repositorio.MaterialRepositorio;
import com.example.reciclaje.repositorio.UsuarioRepositorio;
import com.example.reciclaje.seguridad.CustomUserDetails;
import com.example.reciclaje.servicio.ReciclajeServicio;

import com.example.reciclaje.servicio.UsuarioServicio;

import com.example.reciclaje.servicioDTO.MaterialScanResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reciclajes")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class ReciclajeController {

	 private final ReciclajeServicio reciclajeService;
	    private final MaterialRepositorio materialRepositorio;
	    private final UsuarioRepositorio usuarioRepositorio;

	    private final UsuarioServicio usuarioServicio;



	    @PostMapping("/registrar")
	    public ResponseEntity<?> registrarReciclaje(
	            @RequestParam Long materialId,
	            @RequestParam(defaultValue = "1") int cantidad,
	            Authentication authentication) {
	        try {
	            String username = authentication.getName();
	            Optional<Usuario> optionalUsuario = usuarioRepositorio.findByEmail(username);
	            if (optionalUsuario.isEmpty()) {
	                return new ResponseEntity<>("Usuario no encontrado o no autorizado.", HttpStatus.UNAUTHORIZED);
	            }
	            Long usuarioId = optionalUsuario.get().getId();
	            Reciclaje reciclaje = reciclajeService.registrarReciclaje(usuarioId, materialId, cantidad, false);
 
	            Usuario user = usuarioServicio.agregarPuntos(usuarioId, reciclaje.getPuntosGanados());


	            return ResponseEntity.status(HttpStatus.CREATED).body(reciclaje);
	        } catch (Exception e) {
	            System.err.println("Error al registrar el reciclaje: " + e.getMessage());
	            e.printStackTrace();
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
	                    .body(Map.of(
	                        "error", "Error al registrar el reciclaje",
	                        "details", e.getMessage()
	                    ));
	        }
	    }

	    @GetMapping("/usuario/{usuarioId}")
	    public ResponseEntity<?> reciclajesPorUsuario(@PathVariable Long usuarioId) {
	        try {
	            List<Reciclaje> lista = reciclajeService.obtenerReciclajesPorUsuario(usuarioId);
	            return ResponseEntity.ok(lista);
	        } catch (Exception e) {
	            System.err.println("Error al obtener reciclajes para el usuario: " + e.getMessage());
	            e.printStackTrace();
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                    .body(Map.of("error", "Error al obtener reciclajes para el usuario", "details", e.getMessage()));
	        }
	    }

	    @GetMapping("/mis-reciclajes")
	    public ResponseEntity<?> misReciclajes(Authentication authentication) {
	        try {
	            String username = authentication.getName();
	            Optional<Usuario> optionalUsuario = usuarioRepositorio.findByEmail(username);
	            if (optionalUsuario.isEmpty()) {
	                return new ResponseEntity<>("Usuario no encontrado o no autorizado.", HttpStatus.UNAUTHORIZED);
	            }
	            Long usuarioId = optionalUsuario.get().getId();
	            List<Reciclaje> lista = reciclajeService.obtenerReciclajesPorUsuario(usuarioId);
	            return ResponseEntity.ok(lista);
	        } catch (Exception e) {
	            System.err.println("Error al obtener tus reciclajes: " + e.getMessage());
	            e.printStackTrace();
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                    .body(Map.of("error", "Error al obtener tus reciclajes", "details", e.getMessage()));
	        }
	    }

	    @PreAuthorize("hasAnyRole('ADMIN', 'MODERADOR')")
	    @PutMapping("/validar/{id}")
	    public ResponseEntity<?> validarReciclaje(@PathVariable Long id, Authentication authentication) {
	        try {
	            String emailValidador = authentication.getName();
	            reciclajeService.validarReciclaje(id, emailValidador);
	            return ResponseEntity.ok(Map.of("message", "Reciclaje validado exitosamente"));
	        } catch (Exception e) {
	            System.err.println("Error al validar el reciclaje: " + e.getMessage());
	            e.printStackTrace();
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
	                    .body(Map.of(
	                        "error", "Error al validar el reciclaje",
	                        "details", e.getMessage()
	                    ));
	        }
	    }

	    @PreAuthorize("hasAnyRole('ADMIN', 'MODERADOR')")
	    @PutMapping("/rechazar/{id}")
	    public ResponseEntity<?> rechazarReciclaje(@PathVariable Long id, @RequestBody Map<String, String> body, Authentication authentication) {
	        try {
	            String emailValidador = authentication.getName();
	            String motivoRechazo = body.get("motivoRechazo");
	            if (motivoRechazo == null || motivoRechazo.trim().isEmpty()) {
	                return ResponseEntity.badRequest().body(Map.of("error", "El motivo de rechazo es requerido"));
	            }
	            reciclajeService.rechazarReciclaje(id, emailValidador, motivoRechazo);
	            return ResponseEntity.ok(Map.of("message", "Reciclaje rechazado exitosamente"));
	        } catch (Exception e) {
	            System.err.println("Error al rechazar el reciclaje: " + e.getMessage());
	            e.printStackTrace();
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
	                    .body(Map.of(
	                        "error", "Error al rechazar el reciclaje",
	                        "details", e.getMessage()
	                    ));
	        }
	    }

	    @PostMapping("/crear-desde-qr")
	    public ResponseEntity<?> crearDesdeQR(
	            @RequestBody MaterialScanResponse request,
	            Authentication authentication) {
	        try {
	            String qrData = request.getQrData();
	            int quantity = request.getQuantity();
	            if (qrData == null || qrData.isBlank()) {
	                return ResponseEntity.badRequest().body(Map.of("error", "Datos QR no válidos"));
	            }
	            if (quantity <= 0) {
	                return ResponseEntity.badRequest().body(Map.of("error", "La cantidad debe ser al menos 1"));
	            }
	            String username = authentication.getName();
	            Optional<Usuario> optionalUsuario = usuarioRepositorio.findByEmail(username);
	            if (optionalUsuario.isEmpty()) {
	                return new ResponseEntity<>("Usuario no encontrado o no autorizado.", HttpStatus.UNAUTHORIZED);
	            }
	            Long usuarioId = optionalUsuario.get().getId();
	            MaterialScanResponse response = reciclajeService.registrarReciclajeDesdeQR(usuarioId, qrData, quantity);
	            return ResponseEntity.status(HttpStatus.CREATED).body(response);
	        } catch (Exception e) {
	            System.err.println("Error al registrar desde QR: " + e.getMessage());
	            e.printStackTrace();
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
	                    .body(Map.of(
	                        "error", "Error al registrar desde QR",
	                        "details", e.getMessage()
	                    ));
	        }
	    }

	 /*   @PostMapping("/scan-barcode")
	    public ResponseEntity<?> registrarDesdeCodigoBarras(
	            @RequestBody Map<String, String> payload,
	            Authentication authentication) {
	        try {
	            String codigo = payload.get("codigo");
	            if (codigo == null || codigo.isBlank()) {
	                return ResponseEntity.badRequest().body(Map.of("error", "Código de barras no válido"));
	            }
	            String username = authentication.getName();
	            Optional<Usuario> optionalUsuario = usuarioRepositorio.findByEmail(username);
	            if (optionalUsuario.isEmpty()) {
	                return new ResponseEntity<>("Usuario no encontrado o no autorizado.", HttpStatus.UNAUTHORIZED);
	            }
	            Long usuarioId = optionalUsuario.get().getId();
	            Reciclaje reciclaje = reciclajeService.registrarDesdeCodigoOpenFoodFacts(codigo, usuarioId);
	            return ResponseEntity.ok(Map.of(
	                "message", "Reciclaje registrado pendiente de validación",
	                "reciclaje", reciclaje
	            ));
	        } catch (Exception e) {
	            System.err.println("Error al registrar desde código de barras: " + e.getMessage());
	            e.printStackTrace();
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
	                    .body(Map.of(
	                        "error", "Error al registrar desde código de barras",
	                        "details", e.getMessage()
	                    ));
	        }
	    }*/
	    
	    @PostMapping(value = "/registrar-con-foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	    public ResponseEntity<?> registrarReciclajeConFoto(
	            @RequestParam("materialId") Long materialId,
	            @RequestParam(value = "cantidad", defaultValue = "1.0") Double cantidad, // ✅ Cambiar a Double
	            @RequestParam("foto") MultipartFile foto,
	            Authentication authentication) {
	        
	        try {
	            System.out.println("📸 --- INICIO REGISTRO CON FOTO ---");
	            System.out.println("📦 Datos recibidos:");
	            System.out.println("   - Material ID: " + materialId);
	            System.out.println("   - Cantidad: " + cantidad);
	            System.out.println("   - Foto: " + (foto != null ? foto.getOriginalFilename() : "null"));
	            System.out.println("   - Tamaño: " + (foto != null ? foto.getSize() : 0) + " bytes");
	            
	            // ✅ VALIDACIONES CRÍTICAS
	            if (materialId == null) {
	                System.out.println("❌ ERROR: materialId es NULL");
	                return ResponseEntity.badRequest().body(Map.of(
	                    "error", "El materialId es requerido"
	                ));
	            }
	            
	            if (foto == null || foto.isEmpty()) {
	                System.out.println("❌ ERROR: La foto es requerida");
	                return ResponseEntity.badRequest().body(Map.of(
	                    "error", "La foto es requerida"
	                ));
	            }
	            
	            String username = authentication.getName();
	            Optional<Usuario> optionalUsuario = usuarioRepositorio.findByEmail(username);
	            
	            if (optionalUsuario.isEmpty()) {
	                System.out.println("❌ ERROR: Usuario no encontrado: " + username);
	                return new ResponseEntity<>("Usuario no encontrado.", HttpStatus.UNAUTHORIZED);
	            }
	            
	            Usuario usuario = optionalUsuario.get();
	            System.out.println("👤 Usuario ID: " + usuario.getId());

	            // ✅ Llamar al servicio con los parámetros correctos
	            Reciclaje reciclaje = reciclajeService.registrarReciclajeConFoto(usuario, materialId, cantidad, foto);
	            
	            System.out.println("✅ Reciclaje guardado - ID: " + reciclaje.getId());
	            
	            // ✅ Agregar puntos (si aplica)
	            usuarioServicio.agregarPuntos(usuario.getId(), reciclaje.getPuntosGanados());

	            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
	                "success", true,
	                "message", "Reciclaje registrado exitosamente",
	                "reciclajeId", reciclaje.getId(),
	                "puntosGanados", reciclaje.getPuntosGanados()
	            ));

	        } catch (Exception e) {
	            System.out.println("💥 ERROR en registrarReciclajeConFoto: " + e.getMessage());
	            e.printStackTrace();
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                    .body(Map.of("error", "Error al subir el reciclaje", "details", e.getMessage()));
	        }
	    }
	    

	    @PreAuthorize("hasRole('ADMIN')")
	    @GetMapping("/todos")
	    public ResponseEntity<?> obtenerTodos() {
	        try {
	            List<Reciclaje> reciclajes = reciclajeService.obtenerTodos();
	            return ResponseEntity.ok(reciclajes);
	        } catch (Exception e) {
	            System.err.println("Error al obtener todos los reciclajes: " + e.getMessage());
	            e.printStackTrace();
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                    .body(Map.of("error", "Error al obtener todos los reciclajes", "details", e.getMessage()));
	        }
	    }

	    @PreAuthorize("hasRole('ADMIN')")
	    @GetMapping("/pendientes")
	    public ResponseEntity<?> obtenerPendientes() {
	        try {
	            List<Reciclaje> pendientes = reciclajeService.obtenerReciclajesPendientes();
	            return ResponseEntity.ok(pendientes);
	        } catch (Exception e) {
	            System.err.println("Error al obtener reciclajes pendientes: " + e.getMessage());
	            e.printStackTrace();
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                    .body(Map.of("error", "Error al obtener reciclajes pendientes", "details", e.getMessage()));
	        }
	    }

	    @GetMapping("/imagen/{filename}")
	    @PreAuthorize("permitAll()")
	    public ResponseEntity<Resource> obtenerImagen(@PathVariable String filename) {
	        try {
	            Path filePath = Paths.get(System.getProperty("user.dir"), "uploads", "reciclajes", filename);
	            Resource resource = new FileSystemResource(filePath);

	            if (resource.exists() && resource.isReadable()) {
	                return ResponseEntity.ok()
	                        .contentType(MediaType.IMAGE_JPEG) // o determinar dinámicamente
	                        .body(resource);
	            } else {
	                return ResponseEntity.notFound().build();
	            }
	        } catch (Exception e) {
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	        }
	    }
}