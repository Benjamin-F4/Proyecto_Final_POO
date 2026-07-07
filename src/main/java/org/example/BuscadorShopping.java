package org.example;

import org.example.excepciones.ApiKeyInvalidaException;
import org.example.excepciones.LimiteConsultasExcedidoException;
import org.example.excepciones.ProductoNoEncontradoException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class BuscadorShopping extends BuscadorApi {

    public BuscadorShopping(String apiKey) throws ApiKeyInvalidaException {
        super(apiKey);
    }

    @Override
    public List<Producto> buscar(String termino) throws ProductoNoEncontradoException, LimiteConsultasExcedidoException, ApiKeyInvalidaException {
        // Ejecuta el metodo heredado de BuscadorApi para registrar la consulta
        registrarLogBusqueda(termino);

        List<Producto> listaProductos = new ArrayList<>();

        try {
            // Codificamos de forma segura el término (ej: "Mario Kart World" -> "Mario+Kart+World")
            String query = URLEncoder.encode(termino, StandardCharsets.UTF_8);

            // Construcción de la URL inyectando la query ya formateada para internet
            String urlApi = "https://serpapi.com/search.json?engine=google_shopping&q=" + query
                    + "&google_domain=google.cl&gl=cl&hl=es-419&api_key=" + this.apiKey;

            try (HttpClient cliente = HttpClient.newHttpClient()) {

                HttpRequest peticion = HttpRequest.newBuilder().uri(URI.create(urlApi)).GET().build();

                // Envia solicitud al servidor de SerpApi
                HttpResponse<String> respuesta = cliente.send(peticion, HttpResponse.BodyHandlers.ofString());

                // Convertimos la respuesta de texto plano a un Objeto JSON ejecutable
                JSONObject jsonCompleto = new JSONObject(respuesta.body());

                // Control de Errores de la API según códigos HTTP o respuestas de SerpApi
                if (respuesta.statusCode() == 401 || jsonCompleto.has("error")) {
                    throw new ApiKeyInvalidaException("La API Key ingresada no es válida o expiró.");
                }
                if (respuesta.statusCode() == 429) {
                    throw new LimiteConsultasExcedidoException("Se ha alcanzado el límite de consultas permitidas.");
                }

                // Verificar si la llave 'shopping_results' existe en el JSON
                if (!jsonCompleto.has("shopping_results")) {
                    throw new ProductoNoEncontradoException("No se encontraron resultados para: " + termino);
                }

                // Extraer el arreglo de resultados que compartiste en tu JSON
                JSONArray resultadosJson = jsonCompleto.getJSONArray("shopping_results");

                // Iterar sobre los productos devueltos por SerpApi
                for (int i = 0; i < resultadosJson.length(); i++) {
                    JSONObject item = resultadosJson.getJSONObject(i);

                    // Mapeo exacto de las llaves del JSON a variables de Java
                    String nombre = item.getString("title");
                    double precio = item.getDouble("extracted_price"); // Extrae el número limpio sin letras ni comas
                    String tienda = item.getString("source");          // Ej: "Paris.cl", "Lider", "Entel"

                    // Reemplaza espacios en blanco automáticos
                    String link = item.getString("product_link").replace(" ", "%20");

                    String imagen = item.getString("thumbnail");       // URL de la imagen en miniatura

                    // Instanciamos el objeto Producto y lo añadimos a nuestra lista de resultados
                    listaProductos.add(new Producto(nombre, precio, tienda, link, imagen));
                }
            }

        } catch (ProductoNoEncontradoException | LimiteConsultasExcedidoException | ApiKeyInvalidaException e) {
            // Dejamos que estas excepciones fluyan hacia afuera de forma limpia y directa al Main
            throw e;
        } catch (Exception e) {
            // Aquí capturamos cualquier OTRO error imprevisto (como fallos de internet o JSON mal formado)
            System.err.println("Error inesperado en la comunicación: " + e.getMessage());
            throw new LimiteConsultasExcedidoException("Error crítico de conexión con SerpApi: " + e.getMessage());
        }

        return listaProductos;
    }
}