package com.example.reciclaje.entidades;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "reciclajes")
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Reciclaje {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

	 @ManyToOne(fetch = FetchType.EAGER)
	    @JoinColumn(name = "usuario_id", nullable = false)
	    private Usuario usuario;

	    @ManyToOne(fetch = FetchType.LAZY)
	    @JoinColumn(name = "material_id", nullable = false)
	    private Material material;
	    
	    @Column(nullable = false)
	    private int cantidad;

	    @Column(name = "fecha_reciclaje") // Nombre de columna para la DB si usas snake_case
	    private LocalDateTime fechaReciclaje; // ¡Consistencia con el servicio!

	    @Column(name = "puntos_ganados") // Nombre de columna para la DB si usas snake_case
	    private int puntosGanados; // ¡Consistencia con el servicio!

	    @Column(nullable = false)
	    private boolean validado = false;

	    private LocalDateTime fechaValidacion;
	    
	    @Column(name = "imagen_url")
	    private String imagenUrl;
	
	    public String getImagenUrl() {
	        if (imagenUrl != null && imagenUrl.startsWith("uploads/reciclajes/")) {
	            return "/api/reciclajes/imagen/" + imagenUrl.substring("uploads/reciclajes/".length());
	        }
	        return imagenUrl;
	    }
	    
	    @Enumerated(EnumType.STRING)
	    @Column(name = "estado", nullable = false, columnDefinition = "varchar(255) default 'PENDIENTE'")
	    private EstadoReciclaje estado = EstadoReciclaje.PENDIENTE;
	    
	    
	    @ManyToOne(fetch = FetchType.LAZY)
	    @JoinColumn(name = "usuario_validador_id")
	    private Usuario usuarioValidador;
	    
	    @Column(name = "motivo_rechazo", length = 500)
	    private String motivoRechazo;
	    
	 
		

	    // Y sus respectivos getter y setter (si usas @Data de Lombok se crean solos)
	    
	    


	 
}
