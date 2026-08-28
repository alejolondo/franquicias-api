package com.franquicias.api.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.franquicias.api.domain.exception.InvalidStockException;

@DisplayName("Producto")
class ProductoTest {

    @Test
    @DisplayName("crear un producto con id")
    void creaProductoConIdGenerado() {
    
        Producto producto = Producto.crear("Cafe", 50);


        assertThat(producto.getId()).isNotBlank();
        assertThat(producto.getNombre()).isEqualTo("Cafe");
        assertThat(producto.getStock()).isEqualTo(50);
    }

    @Test
    @DisplayName("Crear mas de un producto")
    void generaIdsDistintos() {
        Producto uno = Producto.crear("Cafe", 10);
        Producto otro = Producto.crear("Te", 20);

        assertThat(uno.getId()).isNotEqualTo(otro.getId());
    }

    @Test
    @DisplayName("producto con stock negativo")
    void rechazaStockNegativo() {
        assertThatThrownBy(() -> Producto.crear("Cafe", -1))
                .isInstanceOf(InvalidStockException.class)
                .hasMessageContaining("no puede ser negativo");
    }

    @Test
    @DisplayName("producto sin stock")
    void rechazaStockNulo() {
        assertThatThrownBy(() -> Producto.crear("Cafe", null))
                .isInstanceOf(InvalidStockException.class);
    }

    @Test
    @DisplayName("conStock devuelve una instancia nueva sin mutar la original")
    void conStockNoMutaElOriginal() {
        Producto original = Producto.crear("Cafe", 50);

        Producto modificado = original.conStock(80);

        assertThat(modificado.getStock()).isEqualTo(80);
        assertThat(original.getStock()).isEqualTo(50);
        assertThat(modificado.getId()).isEqualTo(original.getId());
        assertThat(modificado).isNotSameAs(original);
    }

    @Test
    @DisplayName("conNombre conserva id y stock")
    void conNombreConservaLoDemas() {
        Producto original = Producto.crear("Cafe", 50);

        Producto modificado = original.conNombre("Cafe Premium");

        assertThat(modificado.getNombre()).isEqualTo("Cafe Premium");
        assertThat(modificado.getId()).isEqualTo(original.getId());
        assertThat(modificado.getStock()).isEqualTo(50);
    }

    @Test
    @DisplayName("conStock rechaza valores negativos")
    void conStockRechazaNegativo() {
        Producto producto = Producto.crear("Cafe", 50);

        assertThatThrownBy(() -> producto.conStock(-5))
                .isInstanceOf(InvalidStockException.class);
    }
}