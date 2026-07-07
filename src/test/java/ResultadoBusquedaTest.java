import org.example.BuscadorLocal;
import org.example.Producto;
import org.example.ResultadoBusqueda;
import org.example.excepciones.ApiKeyInvalidaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class ResultadoBusquedaTest {

    private List<Producto> productosPrueba;

    @BeforeEach
    void setUp() {
        productosPrueba = new ArrayList<>();
        productosPrueba.add(new Producto("iPhone 15", 900000, "Lider", "http://link1", "http://img1"));
        productosPrueba.add(new Producto("iPhone 15 Pro", 1200000, "Paris.cl", "http://link2", "http://img2"));
        productosPrueba.add(new Producto("iPhone 14", 750000, "Lider", "http://link3", "http://img3"));
    }

    // Funcionalidad 1: Filtrar correctamente elementos por una tienda específica
    @Test
    void testFiltrarPorTiendaExistente() {
        ResultadoBusqueda analisis = new ResultadoBusqueda("iPhone", productosPrueba);
        List<Producto> filtrados = analisis.filtrarPorTienda("Lider");
        assertEquals(2, filtrados.size());
    }

    // Funcionalidad 2: El filtro por tienda no debe ser afectado por mayúsculas o minúsculas
    @Test
    void testFiltrarPorTiendaCaseInsensitive() {
        ResultadoBusqueda analisis = new ResultadoBusqueda("iPhone", productosPrueba);
        List<Producto> filtrados = analisis.filtrarPorTienda("pArIs.Cl");
        assertEquals(1, filtrados.size());
    }

    // Funcionalidad 3: Retornar lista vacía si la tienda filtrada no existe
    @Test
    void testFiltrarPorTiendaNoExistente() {
        ResultadoBusqueda analisis = new ResultadoBusqueda("iPhone", productosPrueba);
        List<Producto> filtrados = analisis.filtrarPorTienda("Amazon");
        assertTrue(filtrados.isEmpty());
    }

    // Funcionalidad 4: Calcular de forma correcta el promedio matemático de precios
    @Test
    void testCalcularPrecioPromedio() {
        ResultadoBusqueda analisis = new ResultadoBusqueda("iPhone", productosPrueba);
        assertEquals(950000.0, analisis.obtenerPrecioPromedio());
    }

    // Funcionalidad 5: El promedio de una lista vacía debe ser 0.0
    @Test
    void testCalcularPrecioPromedioListaVacia() {
        ResultadoBusqueda analisis = new ResultadoBusqueda("iPhone", new ArrayList<>());
        assertEquals(0.0, analisis.obtenerPrecioPromedio());
    }

    // Funcionalidad 6: El constructor de BuscadorApi debe fallar si la clave es nula
    @Test
    void testBuscadorApiKeyNullLanzaExcepcion() {
        assertThrows(ApiKeyInvalidaException.class, () -> new BuscadorLocal(null));
    }

    // Funcionalidad 7: El constructor de BuscadorApi debe fallar si la clave está vacía
    @Test
    void testBuscadorApiKeyVaciaLanzaExcepcion() {
        assertThrows(ApiKeyInvalidaException.class, () -> new BuscadorLocal(""));
    }

    // Funcionalidad 8: Validación de integridad de datos en getters de Producto
    @Test
    void testGettersProducto() {
        Producto p = new Producto("Tele", 300000, "Ripley", "http://compra", "http://foto");
        assertAll("Verificación de propiedades del objeto producto",
                () -> assertEquals("Tele", p.getNombre()),
                () -> assertEquals(300000, p.getPrecio()),
                () -> assertEquals("Ripley", p.getTienda())
        );
    }

    // Funcionalidad 9: Formatear correctamente el valor monetario a la divisa chilena (CLP)
    @Test
    void testPrecioFormateadoChile() {
        Producto p = new Producto("Consola", 450990, "Falabella", "http://link", "http://img");
        assertEquals("$450.990 CLP", p.getPrecioFormateado());
    }

    // Funcionalidad 10: Validar que el buscador registre correctamente la bitácora de auditoría en consola
    @Test
    void testRegistrarLogBusquedaImpresion() throws ApiKeyInvalidaException {
        BuscadorLocal buscadorPrueba = new BuscadorLocal("fake_key");
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        buscadorPrueba.registrarLogBusqueda("Nintendo Switch");
        String outputEsperado = "Log: Buscando 'Nintendo Switch' en los servidores de SerpApi...";
        assertTrue(outContent.toString().contains(outputEsperado));

        System.setOut(System.out); // Restaurar salida estándar del sistema
    }
}