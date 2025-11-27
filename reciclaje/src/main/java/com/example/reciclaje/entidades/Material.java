package com.example.reciclaje.entidades;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "materiales") // Asegúrate de que el nombre de la tabla en la DB sea 'materiales'
@Data // Reemplaza @Getter y @Setter para ser más conciso
@Builder // Para poder construir objetos Material usando Material.builder()...build()
@NoArgsConstructor // Necesario para JPA
@AllArgsConstructor // Genera un constructor con todos los campos
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Material {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    @JsonIgnore
    private Usuario usuarioCreador;
    
    @Column(name = "descripcion") // Mapea 'descripcion' en la DB a 'descripcion' en la entidad
    private String descripcion;   // ¡Cambio de 'description' a 'descripcion' para consistencia!

    private String categoria;

    @Column(name = "puntos_por_unidad") // Mapea 'puntos_por_unidad' en la DB a 'puntosPorUnidad'
    private Integer puntosPorUnidad;

    // Puedes descomentar si realmente necesitas almacenar URLs de imagen.
    // private String imagenUrl;

    private Boolean reciclable;

    @Column(name = "codigo_barra", unique = true) // Mapea 'codigo_barras' en la DB a 'codigoBarra'
    private String codigoBarra;

    // Relación OneToMany con Reciclaje
    @JsonIgnore
    @OneToMany(mappedBy = "material", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reciclaje> reciclajes;

	
}