package org.example;

import org.example.excepciones.*;
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
    public List<Producto> buscar(String termino) throws
            ProductoNoEncontradoException,
            LimiteConsultasExcedidoException,
            ApiKeyInvalidaException,
            AccesoDenegadoException,
            ErrorServidorException {

        registrarLogBusqueda(termino);
        List<Producto> listaProductos = new ArrayList<>();

        try {
            String query = URLEncoder.encode(termino, StandardCharsets.UTF_8).replace("+", "%20").toLowerCase();

            // 🛠️ Optimizamos agregando &num=20 al final de la URL
            String urlApi = "https://serpapi.com/search.json?engine=google_shopping&q=" + query
                    + "&google_domain=google.cl&gl=cl&hl=es-419&api_key=" + this.apiKey + "&num=20";

            try (HttpClient cliente = HttpClient.newHttpClient()) {
                HttpRequest peticion = HttpRequest.newBuilder().uri(URI.create(urlApi)).GET().build();
                HttpResponse<String> respuesta = cliente.send(peticion, HttpResponse.BodyHandlers.ofString());

                JSONObject jsonCompleto = new JSONObject(respuesta.body());
                int statusCode = respuesta.statusCode();

                // 1. CONTROL DE ERRORES POR CÓDIGO HTTP GLOBAL
                if (statusCode != 200) {
                    switch (statusCode) {
                        case 401:
                            throw new ApiKeyInvalidaException("La API Key ingresada no es válida o expiró.");
                        case 403:
                            throw new AccesoDenegadoException("Tu plan de SerpApi no tiene permisos para usar Google Shopping (Error 403).");
                        case 429:
                            throw new LimiteConsultasExcedidoException("Has agotado tu cuota de búsquedas permitidas (Error 429).");
                        case 500:
                        case 502:
                        case 503:
                            throw new ErrorServidorException("Los servidores de Google/SerpApi están caídos temporalmente (Error " + statusCode + ").");
                        default:
                            throw new ErrorServidorException("Error inesperado del servidor HTTP externo: Código " + statusCode);
                    }
                }

                // 2. MAPPING DE ERRORES DENTRO DEL JSON EXITOSO (CORREGIDO 🛠️)
                if (jsonCompleto.has("error")) {
                    String mensajeError = jsonCompleto.getString("error").toLowerCase();

                    // Ampliamos el espectro para capturar tanto "hasn't" como "haven't" o "no results"
                    if (mensajeError.contains("result") && (mensajeError.contains("hasn't") || mensajeError.contains("haven't") || mensajeError.contains("no"))) {
                        throw new ProductoNoEncontradoException("No se encontraron resultados en Google Shopping para: " + termino);
                    } else {
                        throw new LimiteConsultasExcedidoException("SerpApi rechazó la consulta: " + jsonCompleto.getString("error"));
                    }
                }

                if (!jsonCompleto.has("shopping_results")) {
                    throw new ProductoNoEncontradoException("No se encontraron resultados de compras para: " + termino);
                }

                JSONArray resultadosJson = jsonCompleto.getJSONArray("shopping_results");

                for (int i = 0; i < resultadosJson.length(); i++) {
                    JSONObject item = resultadosJson.getJSONObject(i);

                    String nombre = item.getString("title");
                    double precio = item.getDouble("extracted_price");
                    String tienda = item.getString("source");
                    String link = item.getString("product_link").replace(" ", "%20");
                    String imagen = item.getString("thumbnail");

                    listaProductos.add(new Producto(nombre, precio, tienda, link, imagen));
                }
            }

        } catch (ProductoNoEncontradoException | LimiteConsultasExcedidoException | ApiKeyInvalidaException | AccesoDenegadoException | ErrorServidorException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("Error inesperado en la comunicación: " + e.getMessage());
            throw new ErrorServidorException("Error crítico interno de conexión: " + e.getMessage());
        }

        return listaProductos;
    }
}