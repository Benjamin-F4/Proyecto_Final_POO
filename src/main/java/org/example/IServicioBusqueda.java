package org.example;

import org.example.excepciones.ApiKeyInvalidaException;
import org.example.excepciones.ProductoNoEncontradoException;
import org.example.excepciones.LimiteConsultasExcedidoException;
import java.util.List;

public interface IServicioBusqueda {
    List<Producto> buscar(String termino) throws ProductoNoEncontradoException, LimiteConsultasExcedidoException, ApiKeyInvalidaException;
}