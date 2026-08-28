package com.franquicias.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.franquicias.api.domain.gateway.FranquiciaRepository;
import com.franquicias.api.usecase.FranquiciaUseCase;
import com.franquicias.api.usecase.ProductoUseCase;
import com.franquicias.api.usecase.SucursalUseCase;

@Configuration
public class UseCaseConfig {

    @Bean
    public FranquiciaUseCase franquiciaUseCase(FranquiciaRepository franquiciaRepository) {
        return new FranquiciaUseCase(franquiciaRepository);
    }

    @Bean
    public SucursalUseCase sucursalUseCase(FranquiciaRepository franquiciaRepository) {
        return new SucursalUseCase(franquiciaRepository);
    }

    @Bean
    public ProductoUseCase productoUseCase(FranquiciaRepository franquiciaRepository) {
        return new ProductoUseCase(franquiciaRepository);
    }
}