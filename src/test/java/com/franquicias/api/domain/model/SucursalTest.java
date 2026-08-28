package com.franquicias.api.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.franquicias.api.domain.exception.DuplicateResourceException;
import com.franquicias.api.domain.exception.ProductoNotFoundException;

@DisplayName("Sucursal")
class SucursalTest {

    private Sucursal sucursal;

    @BeforeEach
    void prepararSucursal() {
        sucursal = Sucursal.crear("Sucursal Centro");
    }

    @Test
    @DisplayName("Sucrusal sin productos")
    void creaSucursalVacia() {
        assertThat(sucursal.getId()).isNotBlank();
        assertThat(sucursal.getNombre()).isEqualTo("Sucursal Centro");
        assertThat(sucursal.getProductos()).isEmpty();
    }

    @Nested
    @DisplayName("al agregar productos")
    class AgregarProducto {

        @Test
        @DisplayName("incorpora el producto a la lista")
        void agregaProducto() {
            Producto cafe = Producto.crear("Cafe", 50);

            Sucursal resultado = sucursal.agregarProducto(cafe);

            assertThat(resultado.getProductos())
                    .hasSize(1)
                    .extracting(Producto::getNombre)
                    .containsExactly("Cafe");
        }

        @Test
        @DisplayName("no modifica la sucursal original")
        void noMutaLaOriginal() {
            sucursal.agregarProducto(Producto.crear("Cafe", 50));

            assertThat(sucursal.getProductos()).isEmpty();
        }

        @Test
        @DisplayName("rechaza un nombre duplicado sin importar mayusculas")
        void rechazaDuplicado() {
            Sucursal conCafe = sucursal.agregarProducto(Producto.crear("Cafe", 50));

            assertThatThrownBy(() -> conCafe.agregarProducto(Producto.crear("CAFE", 10)))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("CAFE");
        }
    }

    @Nested
    @DisplayName("al eliminar productos")
    class EliminarProducto {

        @Test
        @DisplayName("quita el producto indicado y conserva los demas")
        void eliminaProducto() {
            Producto cafe = Producto.crear("Cafe", 50);
            Producto te = Producto.crear("Te", 120);
            Sucursal conProductos = sucursal
                    .agregarProducto(cafe)
                    .agregarProducto(te);

            Sucursal resultado = conProductos.eliminarProducto(cafe.getId());

            assertThat(resultado.getProductos())
                    .hasSize(1)
                    .extracting(Producto::getId)
                    .containsExactly(te.getId());
        }

        @Test
        @DisplayName("falla si el producto no existe")
        void fallaSiNoExiste() {
            assertThatThrownBy(() -> sucursal.eliminarProducto("id-inexistente"))
                    .isInstanceOf(ProductoNotFoundException.class)
                    .hasMessageContaining("id-inexistente");
        }
    }

    @Nested
    @DisplayName("al reemplazar productos")
    class ReemplazarProducto {

        @Test
        @DisplayName("sustituye el producto conservando la posicion")
        void reemplazaProducto() {
            Producto cafe = Producto.crear("Cafe", 50);
            Sucursal conCafe = sucursal.agregarProducto(cafe);

            Sucursal resultado = conCafe.reemplazarProducto(cafe.conStock(99));

            assertThat(resultado.getProductos()).hasSize(1);
            assertThat(resultado.getProductos().get(0).getStock()).isEqualTo(99);
        }

        @Test
        @DisplayName("falla si el producto no existe")
        void fallaSiNoExiste() {
            Producto ajeno = Producto.crear("Ajeno", 5);

            assertThatThrownBy(() -> sucursal.reemplazarProducto(ajeno))
                    .isInstanceOf(ProductoNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("al calcular el producto con mayor stock")
    class ProductoConMayorStock {

        @Test
        @DisplayName("devuelve vacio si no hay productos")
        void devuelveVacioSinProductos() {
            assertThat(sucursal.productoConMayorStock()).isEmpty();
        }

        @Test
        @DisplayName("devuelve el unico producto cuando solo hay uno")
        void devuelveElUnico() {
            Sucursal conCafe = sucursal.agregarProducto(Producto.crear("Cafe", 50));

            Optional<Producto> resultado = conCafe.productoConMayorStock();

            assertThat(resultado).isPresent();
            assertThat(resultado.get().getNombre()).isEqualTo("Cafe");
        }

        @Test
        @DisplayName("devuelve el de mayor stock entre varios")
        void devuelveElMayor() {
            Sucursal conProductos = sucursal
                    .agregarProducto(Producto.crear("Cafe", 50))
                    .agregarProducto(Producto.crear("Te", 120))
                    .agregarProducto(Producto.crear("Agua", 80));

            Optional<Producto> resultado = conProductos.productoConMayorStock();

            assertThat(resultado).isPresent();
            assertThat(resultado.get().getNombre()).isEqualTo("Te");
            assertThat(resultado.get().getStock()).isEqualTo(120);
        }
    }

    @Test
    @DisplayName("buscarProducto devuelve vacio si el id no existe")
    void buscarProductoInexistente() {
        assertThat(sucursal.buscarProducto("no-existe")).isEmpty();
    }

    @Test
    @DisplayName("conNombre cambia el nombre y conserva los productos")
    void conNombreConservaProductos() {
        Sucursal conCafe = sucursal.agregarProducto(Producto.crear("Cafe", 50));

        Sucursal resultado = conCafe.conNombre("Sucursal Norte");

        assertThat(resultado.getNombre()).isEqualTo("Sucursal Norte");
        assertThat(resultado.getProductos()).hasSize(1);
        assertThat(resultado.getId()).isEqualTo(conCafe.getId());
    }
}