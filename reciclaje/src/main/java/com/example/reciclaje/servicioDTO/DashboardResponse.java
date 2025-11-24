package com.example.reciclaje.servicioDTO;

import java.util.List;

import com.example.reciclaje.entidades.Nivel;

import lombok.Data;

@Data // Genera getters y setters automáticamente
public class DashboardResponse {
    private String usuarioNombre;
    private String nivelActual;
    private String avatarUrl;
    private String direccion;
    private int puntosTotales;
    private int ranking;
    private long totalReciclajes;
    private long logrosDesbloqueados;
    private long diasActivos;
    private List<ActividadDTO> actividadesRecientes;
}
