package com.example.reciclaje.servicioDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class LogroDTO {
	private String nombre;
    private String descripcion;
    private String imagenTrofeo; // si aún la usas para algo adicional
    private boolean desbloqueado;
    private int progresoActual;
    private int objetivo;
    private int porcentajeCompletado;
    private LocalDateTime FechaObtencion;


}
