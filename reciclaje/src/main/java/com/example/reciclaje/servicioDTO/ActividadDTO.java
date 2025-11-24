package com.example.reciclaje.servicioDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

public class ActividadDTO {
	
	private String descripcion;
    private int puntos;
    
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss") // Formato fecha para JSON
    private LocalDateTime fecha; 

    public ActividadDTO(String descripcion, int puntos, LocalDateTime fecha) {
        this.descripcion = descripcion;
        this.puntos = puntos;
        this.fecha = fecha;
    }

    // Getters
    public String getDescripcion() { return descripcion; }
    public int getPuntos() { return puntos; }
    public LocalDateTime getFecha() { return fecha; }

}
