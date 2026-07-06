package org.example;

import org.example.excepciones.ApiKeyInvalidaException;
import org.example.excepciones.LimiteConsultasExcedidoException;
import org.example.excepciones.ProductoNoEncontradoException;

import java.util.ArrayList;
import java.util.List;

public class BuscadorLocal extends BuscadorApi {

    public BuscadorLocal(String apiKey) throws ApiKeyInvalidaException {
        super(apiKey);
    }

    @Override
    public List<Producto> buscar(String termino) throws ProductoNoEncontradoException, LimiteConsultasExcedidoException {
        registrarLogBusqueda(termino);
        return new ArrayList<Producto>();
    }
}