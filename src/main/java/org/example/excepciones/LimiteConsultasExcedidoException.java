package org.example.excepciones;

public class LimiteConsultasExcedidoException extends Exception {
    public LimiteConsultasExcedidoException(String mensaje) {
        super(mensaje);
    }
}