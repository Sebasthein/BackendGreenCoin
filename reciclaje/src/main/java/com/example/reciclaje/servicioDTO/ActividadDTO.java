package com.example.reciclaje.servicioDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ActividadDTO {
	
	 private String descripcion; // Ejemplo: "Reciclaje de PET (3 unidades)"
	    private int puntos;         // Ejemplo: 30 puntos
	    private LocalDateTime FechaReciclaje;    // Ejemplo: 2025-05-29

	    private String nombreMaterial;
	    private int cantidad;
	    // Constructor
	    public ActividadDTO(String descripcion, int puntos, LocalDateTime fecha) {
	        this.descripcion = descripcion;
	        this.puntos = puntos;
	        this.FechaReciclaje = fecha;
	    }

	    // Getters y setters
	    public String getDescripcion() { return descripcion; }
	    public int getPuntos() { return puntos; }
	    public LocalDateTime getFecha() { return FechaReciclaje; }

}
