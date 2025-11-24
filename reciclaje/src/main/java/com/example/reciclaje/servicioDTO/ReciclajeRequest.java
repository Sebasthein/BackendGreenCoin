package com.example.reciclaje.servicioDTO;

import org.springframework.web.multipart.MultipartFile;

public class ReciclajeRequest {
	
	 private Long materialId;
	    private Double cantidad;
	    private MultipartFile imagen;
	    
	    // Getters y Setters
	    public Long getMaterialId() { return materialId; }
	    public void setMaterialId(Long materialId) { this.materialId = materialId; }
	    
	    public Double getCantidad() { return cantidad; }
	    public void setCantidad(Double cantidad) { this.cantidad = cantidad; }
	    
	    public MultipartFile getImagen() { return imagen; }
	    public void setImagen(MultipartFile imagen) { this.imagen = imagen; }

}
