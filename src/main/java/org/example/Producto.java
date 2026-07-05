package org.example;

public class Producto {
    private final String nombre;
    private final double precio;
    private final String tienda;
    private final String linkCompra;

    // Constructor
    public Producto(String nombre, double precio, String tienda, String linkCompra) {
        this.nombre = nombre;
        this.precio = precio;
        this.tienda = tienda;
        this.linkCompra = linkCompra;
    }

    // Getters
    public String getNombre() { return nombre; }
    public double getPrecio() { return precio; }
    public String getTienda() { return tienda; }
    public String getLinkCompra() { return linkCompra; }

    @Override
    public String toString() {
        return "[" + tienda + "] " + nombre + " - $" + precio;
    }
}