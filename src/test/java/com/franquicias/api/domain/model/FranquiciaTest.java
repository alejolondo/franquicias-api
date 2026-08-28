package com.franquicias.api.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.franquicias.api.domain.exception.DuplicateResourceException;
import com.franquicias.api.domain.exception.SucursalNotFoundException;

@DisplayName("Franquicia")
class FranquiciaTest {

    private Franquicia franquicia;

    @BeforeEach
    void prepararFranquicia() {
        franquicia = Franquicia.crear("Mi Franquicia");
    }

    @Test
    @DisplayName("se crea con id generado y sin sucursales")
    void creaFranquiciaVacia() {
        assertThat(franquicia.getId()).isNotBlank();
        assertThat(franquicia.getNombre()).isEqualTo("Mi Franquicia");
        assertThat(franquicia.getSucursales()).isEmpty();
    }

    @Nested
    @DisplayName("al agregar sucursales")
    class AgregarSucursal {

        @Test
        @DisplayName("incorpora la sucursal a la lista")
        void agregaSucursal() {
            Franquicia resultado = franquicia.agregarSucursal(Sucursal.crear("Centro"));

            assertThat(resultado.getSucursales())
                    .hasSize(1)
                    .extracting(Sucursal::getNombre)
                    .containsExactly("Centro");
        }

        @Test
        @DisplayName("rechaza nombres duplicados ignorando mayusculas")
        void rechazaDuplicada() {
            Franquicia conCentro = franquicia.agregarSucursal(Sucursal.crear("Centro"));

            assertThatThrownBy(() -> conCentro.agregarSucursal(Sucursal.crear("CENTRO")))
                    .isInstanceOf(DuplicateResourceException.class);
        }
    }

    @Nested
    @DisplayName("al reemplazar sucursales")
    class ReemplazarSucursal {

        @Test
        @DisplayName("sustituye la sucursal indicada")
        void reemplazaSucursal() {
            Sucursal centro = Sucursal.crear("Centro");
            Franquicia conCentro = franquicia.agregarSucursal(centro);

            Franquicia resultado = conCentro.reemplazarSucursal(centro.conNombre("Norte"));

            assertThat(resultado.getSucursales())
                    .extracting(Sucursal::getNombre)
                    .containsExactly("Norte");
        }

        @Test
        @DisplayName("falla si la sucursal no existe")
        void fallaSiNoExiste() {
            assertThatThrownBy(() -> franquicia.reemplazarSucursal(Sucursal.crear("Ajena")))
                    .isInstanceOf(SucursalNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("al consultar el producto con mayor stock por sucursal")
    class ProductosConMayorStock {

        @Test
        @DisplayName("devuelve lista vacia si no hay sucursales")
        void listaVaciaSinSucursales() {
            assertThat(franquicia.productosConMayorStockPorSucursal()).isEmpty();
        }

        @Test
        @DisplayName("omite las sucursales que no tienen productos")
        void omiteSucursalesSinProductos() {
            Franquicia conSucursalVacia = franquicia.agregarSucursal(Sucursal.crear("Centro"));

            assertThat(conSucursalVacia.productosConMayorStockPorSucursal()).isEmpty();
        }

        @Test
        @DisplayName("devuelve un producto por cada sucursal con productos")
        void unProductoPorSucursal() {
            Sucursal centro = Sucursal.crear("Centro")
                    .agregarProducto(Producto.crear("Cafe", 50))
                    .agregarProducto(Producto.crear("Te", 120));

            Sucursal norte = Sucursal.crear("Norte")
                    .agregarProducto(Producto.crear("Agua", 300))
                    .agregarProducto(Producto.crear("Jugo", 90));

            Franquicia completa = franquicia
                    .agregarSucursal(centro)
                    .agregarSucursal(norte);

            List<ProductoTopStock> resultado = completa.productosConMayorStockPorSucursal();

            assertThat(resultado).hasSize(2);
            assertThat(resultado)
                    .extracting(ProductoTopStock::getProductoNombre)
                    .containsExactly("Te", "Agua");
            assertThat(resultado)
                    .extracting(ProductoTopStock::getStock)
                    .containsExactly(120, 300);
        }

        @Test
        @DisplayName("cada resultado indica a que sucursal pertenece")
        void indicaLaSucursal() {
            Sucursal centro = Sucursal.crear("Centro")
                    .agregarProducto(Producto.crear("Cafe", 50));
            Franquicia completa = franquicia.agregarSucursal(centro);

            List<ProductoTopStock> resultado = completa.productosConMayorStockPorSucursal();

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getSucursalId()).isEqualTo(centro.getId());
            assertThat(resultado.get(0).getSucursalNombre()).isEqualTo("Centro");
        }

        @Test
        @DisplayName("mezcla sucursales con y sin productos correctamente")
        void mezclaSucursales() {
            Sucursal conProductos = Sucursal.crear("Centro")
                    .agregarProducto(Producto.crear("Cafe", 50));
            Sucursal vacia = Sucursal.crear("Norte");

            Franquicia completa = franquicia
                    .agregarSucursal(conProductos)
                    .agregarSucursal(vacia);

            assertThat(completa.productosConMayorStockPorSucursal())
                    .hasSize(1)
                    .extracting(ProductoTopStock::getSucursalNombre)
                    .containsExactly("Centro");
        }
    }

    @Test
    @DisplayName("conNombre cambia el nombre y conserva las sucursales")
    void conNombreConservaSucursales() {
        Franquicia conSucursal = franquicia.agregarSucursal(Sucursal.crear("Centro"));

        Franquicia resultado = conSucursal.conNombre("Franquicia Renombrada");

        assertThat(resultado.getNombre()).isEqualTo("Franquicia Renombrada");
        assertThat(resultado.getSucursales()).hasSize(1);
        assertThat(resultado.getId()).isEqualTo(conSucursal.getId());
    }
}