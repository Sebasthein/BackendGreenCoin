package com.example.reciclaje.entidades;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "usuario_roles")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UsuarioRol {

	 @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    @ManyToOne
	    @JoinColumn(name = "usuario_id", nullable = false)
	    private Usuario usuario;

	    @ManyToOne
	    @JoinColumn(name = "rol_id", nullable = false)
	    private Rol rol;

	    @Column(nullable = false, columnDefinition = "boolean default true")
	    private Boolean activo = true; // Valor por defecto
	    
	    private LocalDate fechaAsignacion = LocalDate.now();

	    
	    
	 // Constructor para fácil creación
		public UsuarioRol(Usuario usuario, Rol rol) {
			super();
			this.usuario = usuario;
			this.rol = rol;
		}

		 public Boolean getActivo() {
		        return this.activo;
		    }
		 
		
		  
		public UsuarioRol(Rol rol) {
			super();
			this.rol = rol;
		}

		public Rol geRol() {
			// TODO Auto-generated method stub
			return this.rol;
		}
		
		public void setUsuario(Usuario usuario) {
	        this.usuario = usuario;
	    }

	    public void setRol(Rol rol) {
	        this.rol = rol;
	    }

	    public void setActivo(boolean activo) {
	        this.activo = activo;
	    }
		

}
