package com.musicplay.musicplay.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.musicplay.musicplay.services.ArchivoStorageService;

@Configuration
public class ArchivosConfig implements WebMvcConfigurer {

    private final ArchivoStorageService storageService;

    public ArchivosConfig(ArchivoStorageService storageService) {
        this.storageService = storageService;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(storageService.getUploadRoot().toUri().toString());
    }
}
