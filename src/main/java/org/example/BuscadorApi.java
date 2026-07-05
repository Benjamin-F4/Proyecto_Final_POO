package org.example;

import org.example.excepciones.ApiKeyInvalidaException;

public abstract class BuscadorApi implements IServicioBusqueda {
    protected final String apiKey;

    public BuscadorApi(String apiKey) throws ApiKeyInvalidaException {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new ApiKeyInvalidaException("Error: La API Key no puede estar vacía o ser nula.");
        }
        this.apiKey = apiKey;
    }

    public void registrarLogBusqueda(String termino) {
        System.out.println("Log: Buscando '" + termino + "' en los servidores de SerpApi...");
    }
}