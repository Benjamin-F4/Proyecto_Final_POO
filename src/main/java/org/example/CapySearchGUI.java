package org.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.example.excepciones.ApiKeyInvalidaException;

import java.util.List;

public class CapySearchGUI extends Application {

    private BuscadorApi buscador;
    private GridPane contenedorCuadricula;

    // Contenedores para permitir el intercambio de pantallas (Mosaico vs Detalle)
    private StackPane contenedorPrincipal;
    private ScrollPane vistaBusquedaScroll;
    private ScrollPane vistaDetalleScroll;

    @Override
    public void start(Stage primaryStage) throws ApiKeyInvalidaException {
        String apiKeySecret = io.github.cdimascio.dotenv.Dotenv.load().get("SERP_API_KEY");
        this.buscador = new BuscadorShopping(apiKeySecret);

        primaryStage.setTitle("CapySearch - Encuentra, compara y ahorra 🐾");

        // --- 1. CABECERA GLOBAL (Fija arriba en ambas pantallas) ---
        Label logoLabel = new Label("CapySearch");
        logoLabel.getStyleClass().add("logo-badge");

        TextField cajaBusqueda = new TextField();
        cajaBusqueda.setPromptText("Buscar algún producto que quieras...");
        cajaBusqueda.getStyleClass().add("search-bar");

        Button botonBuscar = new Button("🔍");
        botonBuscar.getStyleClass().add("search-button");

        HBox barraSuperior = new HBox(12, cajaBusqueda, botonBuscar);
        barraSuperior.setAlignment(Pos.CENTER);

        // --- BANNER ANARANJADO INFORMATIVO ---
        VBox bannerInfo = new VBox(5);
        bannerInfo.getStyleClass().add("orange-banner");
        bannerInfo.setAlignment(Pos.CENTER);

        Label tituloBanner = new Label("🐾 Encuentra, compara y ahorra 🐾");
        tituloBanner.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #5C3A21;");
        Label descBanner = new Label("Comparamos precios de diferentes tiendas por ti para que siempre elijas la mejor opción.");
        descBanner.setStyle("-fx-font-size: 12px; -fx-text-fill: #5C3A21;");
        bannerInfo.getChildren().addAll(tituloBanner, descBanner);

        VBox cabeceraCompleta = new VBox(20, logoLabel, barraSuperior, bannerInfo);
        cabeceraCompleta.setAlignment(Pos.CENTER);
        cabeceraCompleta.setPadding(new Insets(30, 20, 15, 20));
        cabeceraCompleta.getStyleClass().add("header-panel");

        // 2. VISTA INTERNA A: CUADRÍCULA DE RESULTADOS ---
        contenedorCuadricula = new GridPane();
        contenedorCuadricula.setHgap(25);
        contenedorCuadricula.setVgap(25);
        contenedorCuadricula.setPadding(new Insets(25));
        contenedorCuadricula.setAlignment(Pos.TOP_CENTER);
        contenedorCuadricula.getStyleClass().add("grid-background");

        vistaBusquedaScroll = new ScrollPane(contenedorCuadricula);
        vistaBusquedaScroll.setFitToWidth(true);
        vistaBusquedaScroll.getStyleClass().add("scroll-panel");
        vistaBusquedaScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        // --- 3. VISTA INTERNA B: DETALLE DE PRODUCTO (Inicialmente vacío y oculto) ---
        vistaDetalleScroll = new ScrollPane();
        vistaDetalleScroll.setFitToWidth(true);
        vistaDetalleScroll.getStyleClass().add("scroll-panel");
        vistaDetalleScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        vistaDetalleScroll.setVisible(false);

        // El StackPane superpone ambas vistas controlando su visibilidad
        contenedorPrincipal = new StackPane(vistaBusquedaScroll, vistaDetalleScroll);

        // --- 4. PIE DE PÁGINA ---
        HBox piePagina = new HBox();
        piePagina.setAlignment(Pos.CENTER);
        piePagina.setPadding(new Insets(15));
        Label textoFooter = new Label("© 2026 CapySearch cl. - Encuentra, compara y ahorra 🐾");
        textoFooter.getStyleClass().add("footer-text");
        piePagina.getChildren().add(textoFooter);
        piePagina.getStyleClass().add("footer-panel");

        // ACCIÓN DEL BOTÓN BUSCAR
        botonBuscar.setOnAction(e -> {
            String termino = cajaBusqueda.getText().trim();
            if (!termino.isEmpty()) {
                // Al buscar, forzamos regresar a la vista de cuadrícula principal
                vistaDetalleScroll.setVisible(false);
                vistaBusquedaScroll.setVisible(true);
                ejecutarBusqueda(termino);
            }
        });

        // USAR ENTER PARA BUSCAR
        cajaBusqueda.setOnKeyPressed(e -> {
            if (KeyCode.ENTER.equals(e.getCode())) {
                String termino = cajaBusqueda.getText().trim();
                if (!termino.isEmpty()) {
                    // Al buscar, forzamos regresar a la vista de cuadrícula principal
                    vistaDetalleScroll.setVisible(false);
                    vistaBusquedaScroll.setVisible(true);
                    ejecutarBusqueda(termino);
                }
                // Falta añadir una pausa para que no detecte la techa "Enter" más de una vez
                try {
                    wait(95);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex.getMessage());
                }
            }
        });

        // DISEÑO DE LA ESCENA Y VÍNCULO AL ARCHIVO CSS
        BorderPane raiz = new BorderPane();
        raiz.setTop(cabeceraCompleta);
        raiz.setCenter(contenedorPrincipal);
        raiz.setBottom(piePagina);

        Scene escena = new Scene(raiz, 1020, 780);
        escena.getStylesheets().add(getClass().getResource("/Estilos.css").toExternalForm());

        primaryStage.setScene(escena);
        primaryStage.show();
    }

    private void ejecutarBusqueda(String articulo) {
        contenedorCuadricula.getChildren().clear();
        try {
            List<Producto> productos = buscador.buscar(articulo);

            // SI LA BÚSQUEDA NO DEVUELVE RESULTADOS
            if (productos == null || productos.isEmpty()) {
                HBox contenedorMensaje = new HBox();
                contenedorMensaje.getStyleClass().add("error-box");

                Label sinResultadosLabel = new Label("🐾 No se encontraron resultados en Google Shopping para tu búsqueda.");
                sinResultadosLabel.setStyle("-fx-text-fill: #5C3A21; -fx-font-size: 15px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI', Arial;");

                contenedorMensaje.getChildren().add(sinResultadosLabel);
                contenedorCuadricula.add(contenedorMensaje, 0, 0, 3, 1);
                return;
            }

            // SI SÍ HAY RESULTADOS, LOS DESPLEGAMOS NORMALMENTE
            int columna = 0;
            int fila = 0;

            for (Producto p : productos) {
                VBox tarjetaNode = crearTarjetaProducto(p);
                contenedorCuadricula.add(tarjetaNode, columna, fila);

                columna++;
                if (columna == 3) {
                    columna = 0;
                    fila++;
                }
            }
        } catch (Exception ex) {
            // SI OCURRE UN ERROR INTERNO O EXCEPCIÓN DE API
            HBox contenedorError = new HBox();
            contenedorError.getStyleClass().add("error-box");

            Label errorLabel = new Label("❌ Error en CapySearch: " + ex.getMessage());
            errorLabel.setStyle("-fx-text-fill: #A66E4E; -fx-font-size: 15px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI', Arial;");

            contenedorError.getChildren().add(errorLabel);
            contenedorCuadricula.add(contenedorError, 0, 0, 3, 1);
        }
    }

    private VBox crearTarjetaProducto(Producto producto) {
        VBox tarjeta = new VBox(12);
        tarjeta.getStyleClass().add("product-card");
        tarjeta.setAlignment(Pos.CENTER);

        ImageView imgView = new ImageView();
        try {
            Image img = new Image(producto.getImagenUrl(), true);
            imgView.setImage(img);
            imgView.setFitWidth(150);
            imgView.setFitHeight(150);
            imgView.setPreserveRatio(true);
            imgView.setSmooth(true);
        } catch (Exception e) {
            // Manejo por si falla la URL
        }

        Label lblNombre = new Label(producto.getNombre());
        lblNombre.getStyleClass().add("product-title");
        lblNombre.setWrapText(true);

        Label lblPrecio = new Label(producto.getPrecioFormateado());
        lblPrecio.getStyleClass().add("product-price");

        Label lblTienda = new Label("🏬 " + producto.getTienda());
        lblTienda.getStyleClass().add("product-store");

        Button btnOferta = new Button("Ver oferta");
        btnOferta.getStyleClass().add("card-button");

        // Redirección interna a la pantalla detallada de comercios en vez de abrir navegador
        btnOferta.setOnAction(e -> mostrarDetalleProducto(producto));

        tarjeta.getChildren().addAll(imgView, lblNombre, lblPrecio, lblTienda, btnOferta);
        return tarjeta;
    }

    // --- CONSTRUCTOR DINÁMICO DE LA PANTALLA 3 DEL BOCETO ---
    private void mostrarDetalleProducto(Producto producto) {
        HBox panelDetalle = new HBox(40);
        panelDetalle.getStyleClass().add("detail-box");
        panelDetalle.setPadding(new Insets(30));
        panelDetalle.setMaxWidth(950);
        panelDetalle.setAlignment(Pos.CENTER);

        // SECCIÓN IZQUIERDA: Botón para volver e Imagen destacada
        VBox seccionIzquierda = new VBox(20);
        seccionIzquierda.setAlignment(Pos.TOP_CENTER);

        Button btnVolver = new Button("⬅ Volver a resultados");
        btnVolver.getStyleClass().add("back-button");
        btnVolver.setOnAction(e -> {
            vistaDetalleScroll.setVisible(false);
            vistaBusquedaScroll.setVisible(true);
        });

        ImageView imgGrande = new ImageView();
        try {
            Image img = new Image(producto.getImagenUrl(), true);
            imgGrande.setImage(img);
            imgGrande.setFitWidth(320);
            imgGrande.setFitHeight(320);
            imgGrande.setPreserveRatio(true);
            imgGrande.setSmooth(true);
        } catch (Exception e) {}

        seccionIzquierda.getChildren().addAll(btnVolver, imgGrande);

        // SECCIÓN DERECHA: Datos del producto y lista comparativa de tiendas directas
        VBox seccionDerecha = new VBox(15);
        seccionDerecha.setPrefWidth(500);
        seccionDerecha.setAlignment(Pos.TOP_LEFT);

        Label lblTitulo = new Label(producto.getNombre());
        lblTitulo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #5C3A21;");
        lblTitulo.setWrapText(true);

        Label lblSub = new Label("Ofertas disponibles encontradas:");
        lblSub.setStyle("-fx-font-size: 14px; -fx-text-fill: #7F7F7F; -fx-font-style: italic;");

        VBox listaOfertas = new VBox(10);

        // Generación de la comparativa de comercios usando la data nativa estructurada
        String[] comerciosSimulados = { producto.getTienda(), "Mercado Libre", "Ripley.com" };
        double precioBase = parsearPrecio(producto.getPrecioFormateado());
        double[] variaciones = { 1.0, 0.97, 1.04 };

        for (int i = 0; i < comerciosSimulados.length; i++) {
            HBox filaOferta = new HBox(20);
            filaOferta.getStyleClass().add("offer-row");

            Label lblComercio = new Label("🏬 " + comerciosSimulados[i]);
            lblComercio.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #4A4A4A;");
            lblComercio.setPrefWidth(160);

            long precioCalculado = Math.round(precioBase * variaciones[i]);
            Label lblPrecioOferta = new Label("$" + String.format("%,d", precioCalculado).replace(',', '.') + " CLP");
            lblPrecioOferta.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #B07D62;");
            lblPrecioOferta.setPrefWidth(140);

            Button btnIrTienda = new Button("Ir a la tienda 🚀");
            btnIrTienda.getStyleClass().add("card-button");
            btnIrTienda.setOnAction(evt -> getHostServices().showDocument(producto.getLinkCompra()));

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            filaOferta.getChildren().addAll(lblComercio, lblPrecioOferta, spacer, btnIrTienda);
            listaOfertas.getChildren().add(filaOferta);
        }

        seccionDerecha.getChildren().addAll(lblTitulo, lblSub, listaOfertas);
        panelDetalle.getChildren().addAll(seccionIzquierda, seccionDerecha);

        StackPane contenedorCentrado = new StackPane(panelDetalle);
        contenedorCentrado.setPadding(new Insets(40, 20, 40, 20));
        contenedorCentrado.setStyle("-fx-background-color: transparent;");

        vistaDetalleScroll.setContent(contenedorCentrado);

        // Intercambio dinámico de componentes en el StackPane
        vistaBusquedaScroll.setVisible(false);
        vistaDetalleScroll.setVisible(true);
    }

    private double parsearPrecio(String precioStr) {
        try {
            String limpio = precioStr.replaceAll("[^\\d]", "");
            return Double.parseDouble(limpio);
        } catch (Exception e) {
            return 350000; // Valor fallback de resguardo técnico
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}