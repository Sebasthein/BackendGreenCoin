package com.example.reciclaje.servicio;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

public class FileStorageService {
	
	 private final Path fileStorageLocation;
	    
	    @Autowired
	    public FileStorageService() {
	        this.fileStorageLocation = Paths.get("./uploads")
	                .toAbsolutePath().normalize();
	        
	        try {
	            Files.createDirectories(this.fileStorageLocation);
	        } catch (Exception ex) {
	            throw new RuntimeException("No se pudo crear el directorio para guardar archivos.", ex);
	        }
	    }
	    
	    public String storeFile(MultipartFile file) {
	        try {
	            // Validar archivo
	            if (file.isEmpty()) {
	                throw new RuntimeException("El archivo está vacío.");
	            }
	            
	            // Validar tipo de archivo
	            String contentType = file.getContentType();
	            if (contentType == null || !contentType.startsWith("image/")) {
	                throw new RuntimeException("Solo se permiten archivos de imagen.");
	            }
	            
	            // Generar nombre único
	            String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
	            String fileExtension = "";
	            if (originalFileName.contains(".")) {
	                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
	            }
	            
	            String fileName = "reciclaje_" + System.currentTimeMillis() + fileExtension;
	            
	            // Copiar archivo
	            Path targetLocation = this.fileStorageLocation.resolve(fileName);
	            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
	            
	            return fileName;
	        } catch (IOException ex) {
	            throw new RuntimeException("Error al guardar el archivo: " + ex.getMessage(), ex);
	        }
	    }
	    
	    public Resource loadFileAsResource(String fileName) {
	        try {
	            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
	            Resource resource = new UrlResource(filePath.toUri());
	            
	            if (resource.exists()) {
	                return resource;
	            } else {
	                throw new RuntimeException("Archivo no encontrado: " + fileName);
	            }
	        } catch (MalformedURLException ex) {
	            throw new RuntimeException("Archivo no encontrado: " + fileName, ex);
	        }
	    }
	}
