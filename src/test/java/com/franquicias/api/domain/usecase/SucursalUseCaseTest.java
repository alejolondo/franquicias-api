package com.franquicias.api.domain.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.franquicias.api.domain.exception.DuplicateResourceException;
import com.franquicias.api.domain.exception.FranquiciaNotFoundException;
import com.franquicias.api.domain.exception.SucursalNotFoundException;
import com.franquicias.api.domain.gateway.FranquiciaRepository;
import com.franquicias.api.domain.model.Franquicia;
import com.franquicias.api.domain.model.Sucursal;
import com.franquicias.api.domain.usercase.SucursalUseCase;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@DisplayName("SucursalUseCase")
class SucursalUseCaseTest {

    @Mock
    private FranquiciaRepository franquiciaRepository;

    @InjectMocks
    private SucursalUseCase sucursalUseCase;

    private Franquicia franquicia;
    private Sucursal sucursal;

    @BeforeEach
    void prepararDatos() {
        sucursal = Sucursal.crear("Centro");
        franquicia = Franquicia.crear("Mi Franquicia").agregarSucursal(sucursal);
    }

    private void simularGuardadoExitoso() {
        when(franquiciaRepository.guardar(any(Franquicia.class)))
                .thenAnswer(invocacion -> Mono.just(invocacion.getArgument(0)));
    }

    @Test
    @DisplayName("agrega una sucursal nueva a la franquicia")
    void agregaSucursal() {
        when(franquiciaRepository.buscarPorId(franquicia.getId()))
                .thenReturn(Mono.just(franquicia));
        simularGuardadoExitoso();

        StepVerifier.create(sucursalUseCase.agregar(franquicia.getId(), "Norte"))
                .assertNext(resultado -> {
                    assertThat(resultado.getSucursales()).hasSize(2);
                    assertThat(resultado.getSucursales())
                            .extracting(Sucursal::getNombre)
                            .containsExactly("Centro", "Norte");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("falla si la franquicia no existe")
    void fallaSiLaFranquiciaNoExiste() {
        when(franquiciaRepository.buscarPorId(anyString())).thenReturn(Mono.empty());

        StepVerifier.create(sucursalUseCase.agregar("inexistente", "Norte"))
                .expectError(FranquiciaNotFoundException.class)
                .verify();

        verify(franquiciaRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("falla si la sucursal ya existe con ese nombre")
    void fallaSiLaSucursalEstaDuplicada() {
        when(franquiciaRepository.buscarPorId(franquicia.getId()))
                .thenReturn(Mono.just(franquicia));

        StepVerifier.create(sucursalUseCase.agregar(franquicia.getId(), "Centro"))
                .expectError(DuplicateResourceException.class)
                .verify();

        verify(franquiciaRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("actualiza el nombre de la sucursal conservando su id")
    void actualizaNombreSucursal() {
        when(franquiciaRepository.buscarPorId(franquicia.getId()))
                .thenReturn(Mono.just(franquicia));
        simularGuardadoExitoso();

        StepVerifier.create(sucursalUseCase.actualizarNombre(
                        franquicia.getId(), sucursal.getId(), "Centro Renovado"))
                .assertNext(resultado -> {
                    Sucursal actualizada = resultado.getSucursales().get(0);
                    assertThat(actualizada.getNombre()).isEqualTo("Centro Renovado");
                    assertThat(actualizada.getId()).isEqualTo(sucursal.getId());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("falla si la sucursal no existe")
    void fallaSiLaSucursalNoExiste() {
        when(franquiciaRepository.buscarPorId(franquicia.getId()))
                .thenReturn(Mono.just(franquicia));

        StepVerifier.create(sucursalUseCase.actualizarNombre(
                        franquicia.getId(), "sucursal-inexistente", "Nombre"))
                .expectError(SucursalNotFoundException.class)
                .verify();

        verify(franquiciaRepository, never()).guardar(any());
    }
}