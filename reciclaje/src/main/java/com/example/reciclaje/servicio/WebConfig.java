package com.example.reciclaje.servicio;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Esto hace que lo que esté en la carpeta "uploads" sea accesible por URL
        // Asegúrate de poner la ruta absoluta correcta de tu carpeta uploads en tu PC
        // Ejemplo: "file:C:/Users/Jahir/Documents/Proyectos/Reciclaje/uploads/"
        
        // Opción más genérica (ruta relativa al proyecto):
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }

}
