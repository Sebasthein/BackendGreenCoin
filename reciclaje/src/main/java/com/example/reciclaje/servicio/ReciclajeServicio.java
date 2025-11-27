package com.example.reciclaje.servicio;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.reciclaje.entidades.*;
import com.example.reciclaje.repositorio.*;
import com.example.reciclaje.servicioDTO.ActividadDTO;
import com.example.reciclaje.servicioDTO.MaterialScanResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReciclajeServicio {

	 // Asegúrate de que no haya duplicados si usas el mismo servicio para propósitos similares
    // private final OpenFoodFactsService openFoodFactsService_1; // Este parece ser un duplicado
    private final OpenFoodFactsService openFoodFactsService; // Mantén solo uno
    private final String uploadDir = "uploads/reciclajes/";
    private final ReciclajeRepositorio reciclajeRepository;
    private final UsuarioServicio usuarioService;
    private final MaterialRepositorio materialRepository; // Usado para buscar/guardar materiales
    private final NivelRepositorio nivelRepository;
    private final UsuarioRepositorio usuarioRepository;
   // private final LogService logService;
    private final LogroServicio logroServicio;
    private final NivelServicio nivelService;
    private final MaterialServicio materialService; // Usado para buscar/crear materiales por tipo/nombre

    private final ObjectMapper objectMapper; // Inyecta ObjectMapper
    
    private FileStorageService fileStorageService;

    /**
     * Registra un reciclaje usando un ID de material existente.
     * Si autoValidar es true, los puntos se actualizan inmediatamente.
     * Si es false, los puntos se actualizarán cuando se llame a validarReciclaje().
     */
    @Transactional
    public Reciclaje registrarReciclaje(Long usuarioId, Long materialId, int cantidad, boolean autoValidar) {
        // Validaciones y obtención de entidades
        Usuario usuario = usuarioService.obtenerUsuario(usuarioId);
        Material material = materialRepository.findById(materialId)
            .orElseThrow(() -> new IllegalArgumentException("Material no encontrado"));

        if (!material.getReciclable()) {
            throw new IllegalArgumentException("Este material no es reciclable.");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser al menos 1.");
        }

        // Creación del reciclaje
        Reciclaje reciclaje = new Reciclaje();
        reciclaje.setUsuario(usuario);
        reciclaje.setMaterial(material);
        reciclaje.setCantidad(cantidad);
        reciclaje.setFechaReciclaje(LocalDateTime.now());
        reciclaje.setPuntosGanados(material.getPuntosPorUnidad() * cantidad);
        reciclaje.setValidado(autoValidar);
        
        if (autoValidar) {
            reciclaje.setFechaValidacion(LocalDateTime.now());
        }

        // Guardar el reciclaje
        reciclaje = reciclajeRepository.save(reciclaje);

        // Actualizar puntos si se validó automáticamente
        if (autoValidar) {
            actualizarPuntosUsuario(usuario, reciclaje.getPuntosGanados());
        }

        return reciclaje;
    }
    
    /**
     * Actualiza los puntos del usuario y verifica logros/niveles
     */
    private void actualizarPuntosUsuario(Usuario usuario, int puntosGanados) {
        // Actualizar puntos del usuario
        usuario.setPuntos(usuario.getPuntos() + puntosGanados);
        
        // Verificar si subió de nivel
        nivelService.verificarNivelUsuario(usuario);
        
        // Verificar logros desbloqueados
        //logroServicio.verificarLogrosUsuario(usuario);
        
        // Guardar cambios
        usuarioRepository.save(usuario);

    }
    
    
    /**
     * Calcula los puntos acumulados por un usuario (suma de reciclajes validados)
     */
    public int getPuntosAcumulados(Long usuarioId) {
        Usuario usuario = usuarioService.obtenerUsuario(usuarioId);
        return usuario.getReciclajes().stream()
                .filter(Reciclaje::isValidado)
                .mapToInt(Reciclaje::getPuntosGanados)
                .sum();
    }

    /**
     * Registra un reciclaje obteniendo información del producto desde Open Food Facts
     * y creando/buscando el Material si es necesario.
     * Este método se usaría para la integración con Open Food Facts, no directamente con el frontend del escáner.
     */
    @Transactional
   /* public Reciclaje registrarDesdeCodigoOpenFoodFacts(String codigoBarras, Long usuarioId) {
        // Paso 1: Buscar el producto en Open Food Facts
        JsonNode productData = openFoodFactsService.getProductByBarcode(codigoBarras);
        
        if (productData == null || productData.get("status").asInt() != 1) {
            throw new RuntimeException("Producto no encontrado en Open Food Facts para el código: " + codigoBarras);
        }

        JsonNode product = productData.get("product");
        
        // Paso 2: Extraer datos relevantes
        String nombreProducto = product.has("product_name") ? product.get("product_name").asText() : "Producto Desconocido";
        String categoriaProducto = product.has("categories") && !product.get("categories").asText().isEmpty() ? product.get("categories").asText().split(",")[0] : "General";
        // String imagenUrl = product.has("image_url") ? product.get("image_url").asText() : null; // Si usas imagenUrl

        // Paso 3: Determinar el material (ej: PET, vidrio, etc.)
        String packagingInfo = product.has("packaging") ? product.get("packaging").asText() : "";
        String materialTipo = determinarMaterialDesdePackaging(packagingInfo);
        
        // Paso 4: Buscar o crear el material en tu BD por codigoBarra o por nombre/tipo
        // Intentar encontrar por codigoBarra primero para materiales que ya deberían estar registrados
        List<Material> materialPorCodigoBarra = materialRepository.findByCodigoBarra(codigoBarras);
        Material material;

        if (materialPorCodigoBarra.isPresent()) {
            material = materialPorCodigoBarra.get();
        } else {
            // Si no se encuentra por código de barras, intentar buscar un material genérico por tipo
            List<Material> materialesGenericos = materialService.buscarPorTipo(materialTipo);
            if (!materialesGenericos.isEmpty()) {
                material = materialesGenericos.get(0); // Usar el primer material genérico encontrado
            } else {
                // Crear un nuevo material si no se encuentra ninguno existente
                material = Material.builder()
                    .nombre(nombreProducto)
                    .descripcion("Envase de " + materialTipo + ". Marca: " + (product.has("brands") ? product.get("brands").asText() : "Desconocida"))
                    .categoria(categoriaProducto)
                    // Asigna un valor por defecto o busca uno si es un nuevo material
                    .puntosPorUnidad(10) // Valor por defecto, considera una lógica para esto
                    .reciclable(true)
                    .codigoBarra(codigoBarras) // Asignar el código de barras aquí
                    .build();
                material = materialRepository.save(material); // Guardar el nuevo material
            }
        }
        
        // Paso 5: Registrar el reciclaje
        return registrarReciclaje(usuarioId, material.getId(), 1); // Asume 1 unidad por defecto
    }*/

    /**
     * Método auxiliar para determinar el tipo de material del packaging.
     */
    private String determinarMaterialDesdePackaging(String packaging) {
        packaging = packaging.toLowerCase();
        if (packaging.contains("pet") || packaging.contains("plástico") || packaging.contains("plastic")) {
            return "Plástico";
        } else if (packaging.contains("vidrio") || packaging.contains("glass")) {
            return "Vidrio";
        } else if (packaging.contains("aluminio") || packaging.contains("aluminum")) {
            return "Metal";
        } else if (packaging.contains("cartón") || packaging.contains("carton") || packaging.contains("paper")) {
            return "Papel";
        } else if (packaging.contains("tetra pak") || packaging.contains("tetrapack") || packaging.contains("brik")) {
            return "Tetra Pak";
        }
        return "Otros"; // Categoría por defecto
    }

    /**
     * NUEVO MÉTODO: Registra un reciclaje a partir de los datos JSON del escáner (QR/código de barras).
     * Este método es el que usará tu controlador para el flujo de escaneo del frontend.
     */
    @Transactional
    public MaterialScanResponse registrarReciclajeDesdeQR(Long usuarioId, String qrDataJson, int cantidad) throws Exception {
        // 1. Parsear el JSON del QR/código de barras a un objeto Material
        // ObjectMapper convierte el string JSON (qrDataJson) a un objeto Material temporal
        Material scannedMaterialProps = objectMapper.readValue(qrDataJson, Material.class);

        // 2. Buscar el material en la base de datos por su codigoBarra
        List<Material> existingMaterial = materialRepository.findByCodigoBarra(scannedMaterialProps.getCodigoBarra());
        
        Material materialToRecycle;
        if (existingMaterial.isEmpty()) {
            materialToRecycle = existingMaterial.get(0);
            // Opcional: Actualizar propiedades del material existente si el QR trae información más reciente
            // materialToRecycle.setNombre(scannedMaterialProps.getNombre());
            // materialToRecycle.setDescripcion(scannedMaterialProps.getDescripcion());
            // materialRepository.save(materialToRecycle); 
        } else {
            // Si el material no existe, lo creamos con los datos del QR/código de barras
            materialToRecycle = new Material();
            materialToRecycle.setNombre(scannedMaterialProps.getNombre());
            materialToRecycle.setCategoria(scannedMaterialProps.getCategoria());
            materialToRecycle.setPuntosPorUnidad(scannedMaterialProps.getPuntosPorUnidad());
            materialToRecycle.setReciclable(scannedMaterialProps.getReciclable()); // Usar getReciclable()
            materialToRecycle.setCodigoBarra(scannedMaterialProps.getCodigoBarra());
            materialToRecycle.setDescripcion(scannedMaterialProps.getDescripcion());
            
            materialToRecycle = materialRepository.save(materialToRecycle); // Guardar el nuevo material
        }

        // 3. Registrar el Evento de Reciclaje usando el material encontrado o creado
        Usuario usuario = usuarioService.obtenerUsuario(usuarioId); // Usa usuarioService.obtenerUsuario()

        if (!materialToRecycle.getReciclable()) {
            throw new IllegalArgumentException("Este material no es reciclable.");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser al menos 1.");
        }

        Reciclaje reciclaje = new Reciclaje();
        reciclaje.setUsuario(usuario);
        reciclaje.setMaterial(materialToRecycle);
        reciclaje.setCantidad(cantidad);
        reciclaje.setFechaReciclaje(LocalDateTime.now()); // Usar fechaReciclaje
        reciclaje.setPuntosGanados(materialToRecycle.getPuntosPorUnidad() * cantidad); // Usar puntosGanados
        reciclaje.setValidado(false); // Los reciclajes del escáner inician no validados

        reciclaje = reciclajeRepository.save(reciclaje); // Guardar el registro de reciclaje

        // Devolver una respuesta estructurada para el frontend
        return MaterialScanResponse.builder()
            .material(materialToRecycle)
            .pointsEarned(reciclaje.getPuntosGanados())
            .message("Material registrado con éxito. Pendiente de validación.")
            .build();
    }

    /**
     * Valida un reciclaje y actualiza los puntos del usuario, nivel y logros.
     */
    @Transactional
    public Reciclaje validarReciclaje(Long id) {
        Reciclaje reciclaje = reciclajeRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Reciclaje no encontrado"));

        if (reciclaje.isValidado()) { // Asegúrate de que este también esté corregido a isValidado()
            throw new IllegalStateException("El reciclaje ya fue validado");
        }

        Usuario usuario = reciclaje.getUsuario();
        // CAMBIO AQUÍ: Usar getPuntos() y setPuntos() si el campo en Usuario se llama 'puntos'
        usuario.setPuntos(usuario.getPuntos() + reciclaje.getPuntosGanados()); // <-- CORREGIDO

        reciclaje.setValidado(true);
        reciclaje.setFechaValidacion(LocalDateTime.now());

        actualizarNivelYLogros(usuario); // Llama a este método después de actualizar los puntos del usuario

        usuarioRepository.save(usuario); // Guardar el usuario actualizado
        return reciclajeRepository.save(reciclaje); // Guardar el reciclaje validado
    }
    /**
     * Actualiza el nivel y verifica logros del usuario.
     */
    private void actualizarNivelYLogros(Usuario usuario) {
        // Asegúrate de que el método obtenerNivelPorPuntos de nivelService use el campo de puntos correcto del usuario
        // CAMBIO AQUÍ: Usar getPuntos()
        Nivel nuevoNivel = nivelService.obtenerNivelPorPuntos(usuario.getPuntos()); // <-- CORREGIDO

        if (nuevoNivel != null && (usuario.getNivel() == null || !nuevoNivel.getId().equals(usuario.getNivel().getId()))) {
            usuario.setNivel(nuevoNivel);
        }
    }

    public Reciclaje registrarReciclajeConFoto(Usuario usuario, Long materialId, Double cantidad, MultipartFile foto) {
        try {
            System.out.println("🎯 Iniciando registro de reciclaje en servicio:");
            System.out.println("   - Usuario: " + usuario.getEmail());
            System.out.println("   - Material ID: " + materialId);
            System.out.println("   - Cantidad: " + cantidad);
            
            // ✅ VALIDAR QUE EL MATERIAL EXISTA
            Optional<Material> materialOpt = materialRepository.findById(materialId);
            if (materialOpt.isEmpty()) {
                System.out.println("❌ ERROR: Material no encontrado con ID: " + materialId);
                throw new RuntimeException("Material no encontrado con ID: " + materialId);
            }
            
            Material material = materialOpt.get();
            System.out.println("✅ Material encontrado: " + material.getNombre());
            
            // 1. Guardar la imagen
            String nombreArchivo = System.currentTimeMillis() + "_" + 
                (foto.getOriginalFilename() != null ? foto.getOriginalFilename() : "reciclaje.jpg");
            
            Path rutaArchivo = Paths.get(uploadDir).resolve(nombreArchivo).normalize();
            Files.createDirectories(rutaArchivo.getParent());
            Files.copy(foto.getInputStream(), rutaArchivo, StandardCopyOption.REPLACE_EXISTING);
            
            String rutaRelativa = "/api/reciclajes/imagen/" + nombreArchivo;
            System.out.println("✅ Imagen guardada: " + rutaRelativa);

            // 2. Crear el objeto Reciclaje
            Reciclaje reciclaje = new Reciclaje();
            reciclaje.setUsuario(usuario);
            reciclaje.setMaterial(material); // ✅ ESTO ES LO MÁS IMPORTANTE
            reciclaje.setCantidad(cantidad.intValue()); // o
            reciclaje.setCantidad((int) Math.round(cantidad));
            reciclaje.setImagenUrl(rutaRelativa);
            reciclaje.setFechaReciclaje(LocalDateTime.now());
            reciclaje.setEstado(EstadoReciclaje.PENDIENTE);
            reciclaje.setValidado(false);
            
            // ✅ Calcular puntos ganados
            int puntosGanados = calcularPuntos(material, cantidad);
            reciclaje.setPuntosGanados(puntosGanados);
            
            System.out.println("💾 Guardando reciclaje en BD...");
            System.out.println("   - Material: " + reciclaje.getMaterial().getNombre());
            System.out.println("   - Material ID: " + reciclaje.getMaterial().getId());
            System.out.println("   - Puntos: " + puntosGanados);
            
            Reciclaje reciclajeGuardado = reciclajeRepository.save(reciclaje);
            
            System.out.println("✅ Reciclaje guardado exitosamente - ID: " + reciclajeGuardado.getId());
            return reciclajeGuardado;
            
        } catch (Exception e) {
            System.out.println("💥 Error en servicio registrarReciclajeConFoto: " + e.getMessage());
            throw new RuntimeException("Error al registrar reciclaje: " + e.getMessage(), e);
        }
    }
    
    private int calcularPuntos(Material material, Double cantidad) {
        // Lógica para calcular puntos basado en el material y cantidad
        if (material.getPuntosPorUnidad() != null && cantidad != null) {
            return (int) (material.getPuntosPorUnidad() * cantidad);
        }
        return 0;
    }
  
    
    public long contarDiasActivosPorUsuario(Long usuarioId) {
        List<Reciclaje> reciclajes = reciclajeRepository.findByUsuarioId(usuarioId);
        
        return reciclajes.stream()
                .map(r -> r.getFechaReciclaje().toLocalDate()) // Solo la parte de la fecha
                .distinct()
                .count();
    }

    public List<ActividadDTO> obtenerHistorialDelUsuario(Long usuarioId) {
        List<Reciclaje> reciclajes = reciclajeRepository.findByUsuarioId(usuarioId);
        
        return reciclajes.stream().map(r -> new ActividadDTO(
                r.getMaterial().getNombre(),
                r.getCantidad(),
                r.getFechaReciclaje()
        )).toList();
    }
    public List<Reciclaje> obtenerReciclajesPorUsuario(Long usuarioId) {
        return reciclajeRepository.findByUsuarioId(usuarioId);
    }

    public List<Reciclaje> obtenerTodos() {
        return reciclajeRepository.findAll();
    }

    public List<Reciclaje> obtenerReciclajesPendientesList() {
        return reciclajeRepository.findByValidadoFalse();
    }
    
    public List<Reciclaje> obtenerReciclajesPendientes() {
        return reciclajeRepository.findByEstadoOrderByFechaReciclajeAsc(EstadoReciclaje.PENDIENTE);
    }
    
    // Obtener reciclajes por estado
    public List<Reciclaje> obtenerReciclajesPorEstado(EstadoReciclaje estado) {
        return reciclajeRepository.findByEstadoOrderByFechaReciclajeAsc(estado);
    }
    
    // Validar (aprobar) reciclaje
    public void validarReciclaje(Long reciclajeId, String emailValidador) {
        Reciclaje reciclaje = reciclajeRepository.findById(reciclajeId)
                .orElseThrow(() -> new RuntimeException("Reciclaje no encontrado"));
        
        Usuario validador = usuarioService.findByEmail(emailValidador);
        
        reciclaje.setEstado(EstadoReciclaje.VALIDADO);
        reciclaje.setFechaValidacion(LocalDateTime.now());
        reciclaje.setUsuarioValidador(validador);
        reciclaje.setMotivoRechazo(null); // Limpiar motivo si existía
        
        // Asignar puntos al usuario
        usuarioService.agregarPuntos(reciclaje.getUsuario().getId(), reciclaje.getPuntosGanados());
        
        reciclajeRepository.save(reciclaje);
    }
    
    // Rechazar reciclaje
    public void rechazarReciclaje(Long reciclajeId, String emailValidador, String motivoRechazo) {
        Reciclaje reciclaje = reciclajeRepository.findById(reciclajeId)
                .orElseThrow(() -> new RuntimeException("Reciclaje no encontrado"));
        
        Usuario validador = usuarioService.findByEmail(emailValidador);
        
        reciclaje.setEstado(EstadoReciclaje.RECHAZADO);
        reciclaje.setFechaValidacion(LocalDateTime.now());
        reciclaje.setUsuarioValidador(validador);
        reciclaje.setMotivoRechazo(motivoRechazo);
        
        reciclajeRepository.save(reciclaje);
    }
    
    // Obtener estadísticas para admin
    public Map<String, Object> obtenerEstadisticasAdmin() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("pendientes", reciclajeRepository.countByEstado(EstadoReciclaje.PENDIENTE));
        stats.put("validados", reciclajeRepository.countByEstado(EstadoReciclaje.VALIDADO));
        stats.put("rechazados", reciclajeRepository.countByEstado(EstadoReciclaje.RECHAZADO));
        stats.put("total", reciclajeRepository.count());
        return stats;
    }
    
    // Guardar reciclaje con foto
    public Reciclaje guardarReciclajeConFoto(Reciclaje reciclaje, MultipartFile foto) {
        if (foto != null && !foto.isEmpty()) {
            String nombreArchivo = fileStorageService.storeFile(foto);
            reciclaje.setImagenUrl(nombreArchivo);
        }
        return reciclajeRepository.save(reciclaje);
    }
}