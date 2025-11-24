package com.example.reciclaje.servicio;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.reciclaje.entidades.Material;
import com.example.reciclaje.entidades.Usuario;
import com.example.reciclaje.repositorio.MaterialRepositorio;
import com.example.reciclaje.repositorio.UsuarioRepositorio;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;

@Service
public class MaterialServicio {

	  private final MaterialRepositorio materialRepository;
	    private final UsuarioRepositorio usuarioRepositorio;
	    private final ObjectMapper objectMapper;

	    // Modificar constructor para incluir UsuarioRepositorio
	    public MaterialServicio(MaterialRepositorio materialRepository, 
	                          UsuarioRepositorio usuarioRepositorio,
	                          ObjectMapper objectMapper) {
	        this.materialRepository = materialRepository;
	        this.usuarioRepositorio = usuarioRepositorio;
	        this.objectMapper = objectMapper;
	    }

	    @PostConstruct
	    public void inicializarMateriales() {
	        if (materialRepository.count() == 0) {
	            List<Material> materiales = Arrays.asList(
	                    crearMaterialPorDefecto("Botella PET", "Plástico", 10, "00001", "botella-pet.jpg"),
	                    crearMaterialPorDefecto("Latas Aluminio", "Metal", 15, "00002", "latas.jpg"),
	                    crearMaterialPorDefecto("Cartón Corrugado", "Papel", 8, "00003", "carton.jpg"),
	                    crearMaterialPorDefecto("Vidrio Transparente", "Vidrio", 12, "00004", "vidrio.jpg"));
	            materialRepository.saveAll(materiales);
	        }
	    }

	    private Material crearMaterialPorDefecto(String nombre, String categoria, int puntos, String codigo, String imagenUrl) {
	        return Material.builder()
	                .nombre(nombre)
	                .categoria(categoria)
	                .puntosPorUnidad(puntos)
	                .codigoBarra(codigo)
	                .reciclable(true)
	                .descripcion("Material para reciclaje de " + categoria)
	                .build();
	    }

	    @Transactional(readOnly = true)
	    public Optional<Material> buscarPorId(Long id) {
	        return materialRepository.findById(id);
	    }

	    @Transactional(readOnly = true)
	    public List<Material> buscarPorTipo(String tipo) {
	        return materialRepository.findByCategoriaContainingIgnoreCase(tipo);
	    }

	    @Transactional
	    public Material crearMaterial(Material material) {
	        if (material.getNombre() == null || material.getNombre().trim().isEmpty()) {
	            throw new IllegalArgumentException("El nombre del material no puede estar vacío");
	        }

	        // Buscar por codigoBarra antes de guardar para evitar duplicados
	        List<Material> materialesExistentes = materialRepository.findByCodigoBarra(material.getCodigoBarra());
	        if (!materialesExistentes.isEmpty()) {
	            throw new DataIntegrityViolationException("Código de barras ya registrado: " + material.getCodigoBarra());
	        }

	        // Asegurar valores por defecto si no vienen en el request
	        if (material.getPuntosPorUnidad() == null) {
	            material.setPuntosPorUnidad(10);
	        }
	        if (material.getReciclable() == null) {
	            material.setReciclable(true);
	        }
	        if (material.getDescripcion() == null || material.getDescripcion().trim().isEmpty()) {
	            material.setDescripcion("Material para reciclaje de " + material.getCategoria());
	        }

	        return materialRepository.save(material);
	    }

	    @Transactional
	    public Material actualizarMaterial(Long id, Material material) {
	        Material existente = materialRepository.findById(id)
	                .orElseThrow(() -> new IllegalArgumentException("Material no encontrado con ID: " + id));

	        existente.setNombre(material.getNombre());
	        existente.setDescripcion(material.getDescripcion());
	        existente.setCategoria(material.getCategoria());
	        existente.setPuntosPorUnidad(material.getPuntosPorUnidad());
	        existente.setReciclable(material.getReciclable());

	        return materialRepository.save(existente);
	    }

	    @Transactional(readOnly = true)
	    public List<Material> obtenerTodos() {
	        return materialRepository.findAll();
	    }
	    
	    // MÉTODO ACTUALIZADO - Con usuarioId
	    @Transactional
	    public Material crearMaterialDesdeQR(String qrData, Long usuarioId) {
	        try {
	            // Obtener usuario
	            Optional<Usuario> usuario = usuarioRepositorio.findById(usuarioId);
	            if (usuario.isEmpty()) {
	                throw new IllegalArgumentException("Usuario no encontrado");
	            }
	            
	            // Parsear el JSON del QR
	            JsonNode jsonNode = objectMapper.readTree(qrData);
	            
	            // Validar campos obligatorios
	            if (!jsonNode.hasNonNull("nombre") || !jsonNode.hasNonNull("categoria") || !jsonNode.hasNonNull("puntosPorUnidad")) {
	                throw new IllegalArgumentException("El QR no contiene todos los campos obligatorios: nombre, categoria, puntosPorUnidad.");
	            }
	            
	            // Crear el material
	            Material material = new Material();
	            material.setNombre(jsonNode.get("nombre").asText());
	            material.setCategoria(jsonNode.get("categoria").asText());
	            material.setPuntosPorUnidad(jsonNode.get("puntosPorUnidad").asInt());
	            
	            // Campos opcionales
	            if (jsonNode.hasNonNull("descripcion")) {
	                material.setDescripcion(jsonNode.get("descripcion").asText());
	            } else {
	                material.setDescripcion("Material para reciclaje de " + material.getCategoria());
	            }
	            
	            if (jsonNode.hasNonNull("codigoBarra")) {
	                material.setCodigoBarra(jsonNode.get("codigoBarra").asText());
	            }
	            
	            if (jsonNode.hasNonNull("reciclable")) {
	                material.setReciclable(jsonNode.get("reciclable").asBoolean());
	            } else {
	                material.setReciclable(true);
	            }
	            
	            // ASOCIAR CON EL USUARIO - IMPORTANTE PARA MAUI
	            material.setUsuarioCreador(usuario.get());
	            
	            return materialRepository.save(material);
	            
	        } catch (JsonProcessingException e) {
	            throw new IllegalArgumentException("Formato de QR inválido o malformado: " + e.getMessage(), e);
	        }
	    }

	    // MÉTODO ACTUALIZADO - Con usuarioId
	    public Material buscarPorCodigo(String codigoBarra, Long usuarioId) {
	        if (codigoBarra == null || codigoBarra.isBlank()) {
	            throw new IllegalArgumentException("El código de barras no puede estar vacío");
	        }

	        List<Material> materiales = materialRepository.findByCodigoBarra(codigoBarra);
	        
	        if (materiales.isEmpty()) {
	            // Crear material automáticamente si no existe
	            return crearMaterialDesdeCodigo(codigoBarra, usuarioId);
	        }
	        
	        if (materiales.size() > 1) {
	            throw new IllegalStateException("Existen múltiples materiales con el código " + codigoBarra);
	        }
	        
	        return materiales.get(0);
	    }
	    
	    // NUEVO MÉTODO - Crear material desde código
	    private Material crearMaterialDesdeCodigo(String codigoBarra, Long usuarioId) {
	        Optional<Usuario> usuario = usuarioRepositorio.findById(usuarioId);
	        if (usuario.isEmpty()) {
	            throw new IllegalArgumentException("Usuario no encontrado");
	        }
	        
	        Material nuevoMaterial = new Material();
	        nuevoMaterial.setCodigoBarra(codigoBarra);
	        nuevoMaterial.setNombre("Material desde código: " + codigoBarra);
	        nuevoMaterial.setCategoria("GENERICO");
	        nuevoMaterial.setPuntosPorUnidad(10);
	        nuevoMaterial.setDescripcion("Material creado automáticamente desde código de barras");
	        nuevoMaterial.setReciclable(true);
	        nuevoMaterial.setUsuarioCreador(usuario.get());
	        
	        return materialRepository.save(nuevoMaterial);
	    }
	    
	    // MÉTODO OBSOLETO - Eliminar o marcar como deprecated
	    /*
	    @Transactional
	    public Material crearMaterialDesdeQR(String qrData) {
	        // Este método ya no se usa, se reemplaza por el que tiene usuarioId
	        throw new UnsupportedOperationException("Usar el método con usuarioId: crearMaterialDesdeQR(String qrData, Long usuarioId)");
	    }
	    */
	}
