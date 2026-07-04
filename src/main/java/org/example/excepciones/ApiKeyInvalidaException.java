package org.example.excepciones;

public class ApiKeyInvalidaException extends Exception {
    public ApiKeyInvalidaException(String mensaje) {
        super(mensaje);
    }
}