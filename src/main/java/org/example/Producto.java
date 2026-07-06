package org.example;

public class Producto {
    private final String nombre;
    private final double precio;
    private final String tienda;
    private final String linkCompra;
    private final String imagenUrl;

    public Producto(String nombre, double precio, String tienda, String linkCompra, String imagenUrl) {
        this.nombre = nombre;
        this.precio = precio;
        this.tienda = tienda;
        this.linkCompra = linkCompra;
        this.imagenUrl = imagenUrl;
    }

    public String getNombre() { return nombre; }
    public double getPrecio() { return precio; }
    public String getTienda() { return tienda; }
    public String getLinkCompra() { return linkCompra; }
    public String getImagenUrl() { return imagenUrl; }

    @Override
    public String toString() {
        return "📦 " + nombre + "\n   💵 Precio: $" + precio + " | 🏬 Tienda: " + tienda +
                "\n   🔗 Enlace: " + linkCompra +
                "\n   🖼️ Imagen (URL): " + imagenUrl;
    }
}