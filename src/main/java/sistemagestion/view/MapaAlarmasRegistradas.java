/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.view;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingNode;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCenter;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;
import sistemagestion.model.Alarma;
import sistemagestion.model.EstadoAlarma;
import sistemagestion.service.AlarmaService;

import javax.swing.SwingUtilities;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MapaAlarmasRegistradas {

    // ── Paleta (igual que MapaAlarmas para coherencia visual) ─────────────────
    private static final String C_INPUT_BG = "#f7f7f7";
    private static final String C_INPUT_TEXT = "#4b5563";
    private static final String C_LABEL = "#374151";
    private static final String C_SEPARATOR = "#e5e7eb";
    private static final String C_DARK_GRAD = "linear-gradient(to right, #16283d, #1f3a56)";

    // Colores por estado
    private static final java.awt.Color COLOR_ACTIVA = new java.awt.Color(34, 197, 94);   // verde
    private static final java.awt.Color COLOR_INACTIVA = new java.awt.Color(156, 163, 175); // gris
    private static final java.awt.Color COLOR_MANTENIMIENTO = new java.awt.Color(251, 191, 36); // amarillo

    // ── Servicios ─────────────────────────────────────────────────────────────
    private AlarmaService alarmaService;

    // ── Estado ────────────────────────────────────────────────────────────────
    private JXMapViewer mapa;
    private List<Alarma> todasLasAlarmas = new ArrayList<>();
    private List<Alarma> alarmasFiltradas = new ArrayList<>();
    private Alarma alarmaSeleccionada;

    // ── Controles ─────────────────────────────────────────────────────────────
    private ComboBox<String> cmbFiltroEstado;
    private TextField txtBusqueda;
    private Label lblContador;
    private Label lblDetalleNombre;
    private Label lblDetalleBarrio;
    private Label lblDetalleCoordenadas;
    private Label lblDetalleRadio;
    private Label lblDetalleEstado;
    private VBox panelDetalle;
    private VBox panelSugerencias;
    private ListView<Alarma> lstSugerencias;

    // ── Constructor ───────────────────────────────────────────────────────────
    public MapaAlarmasRegistradas() {
        try {
            alarmaService = new AlarmaService();
        } catch (SQLException e) {
            System.out.println("Error AlarmaService: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    public void mostrar() {
        Stage stage = new Stage();
        stage.setTitle("WolertApp — Alarmas Registradas");

        BorderPane root = new BorderPane();
        root.setTop(buildTopBar());
        root.setCenter(buildCentro());
        root.setStyle("-fx-background-color:white;");

        stage.setScene(new Scene(root, 1150, 700));
        stage.setResizable(true);

        // Cargar alarmas antes de mostrar
        cargarAlarmas();

        stage.show();
    }

    public javafx.scene.Node build() {
        BorderPane root = new BorderPane();
        root.setTop(buildTopBar());
        root.setCenter(buildCentro());
        root.setStyle("-fx-background-color:white;");
        cargarAlarmas();
        return root;
    }

    // ════════════════════════════════════════════════════════════════════════
    // BARRA SUPERIOR
    // ════════════════════════════════════════════════════════════════════════
    private HBox buildTopBar() {
        // Logo
        javafx.scene.image.ImageView logoImg = new javafx.scene.image.ImageView();
        try {
            java.awt.image.BufferedImage raw = javax.imageio.ImageIO.read(
                    getClass().getResourceAsStream("/LogoWolertApp.png"));
            logoImg.setImage(javafx.embed.swing.SwingFXUtils.toFXImage(
                    recortarTransparencia(raw), null));
        } catch (Exception ignored) {
        }
        logoImg.setFitHeight(24);
        logoImg.setFitWidth(24);
        logoImg.setPreserveRatio(true);

        StackPane logoBox = new StackPane(logoImg);
        logoBox.setStyle("-fx-background-color:" + C_DARK_GRAD
                + ";-fx-background-radius:6;-fx-padding:6;");

        Label logoText = new Label("WolertApp");
        logoText.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        logoText.setTextFill(Color.web("#111827"));

        Region sep = new Region();
        sep.setStyle("-fx-background-color:" + C_SEPARATOR + ";");
        sep.setPrefSize(1, 20);

        Label titulo = new Label("Mapa de Alarmas Registradas");
        titulo.setFont(Font.font("Arial", 13));
        titulo.setTextFill(Color.web("#6b7280"));

        // Leyenda de estados
        HBox leyenda = buildLeyenda();

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Contador
        lblContador = new Label("0 alarmas");
        lblContador.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        lblContador.setTextFill(Color.WHITE);
        lblContador.setStyle(
                "-fx-background-color:" + C_DARK_GRAD + ";"
                + "-fx-background-radius:20;-fx-padding:4 14 4 14;");

        HBox bar = new HBox(10, logoBox, logoText, sep, new Label("🗺️"), titulo,
                spacer, leyenda, lblContador);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(12, 20, 12, 20));
        bar.setStyle("-fx-background-color:white;-fx-border-color:" + C_SEPARATOR
                + ";-fx-border-width:0 0 1 0;");
        return bar;
    }

    private HBox buildLeyenda() {
        HBox leyenda = new HBox(14,
                puntito("#22c55e", "Activa"),
                puntito("#9ca3af", "Inactiva"),
                puntito("#fbbf24", "Mantenimiento"));
        leyenda.setAlignment(Pos.CENTER_LEFT);
        return leyenda;
    }

    private HBox puntito(String color, String texto) {
        javafx.scene.shape.Circle c = new javafx.scene.shape.Circle(5);
        c.setFill(Color.web(color));
        Label lbl = new Label(texto);
        lbl.setFont(Font.font("Arial", 11));
        lbl.setTextFill(Color.web("#6b7280"));
        HBox h = new HBox(5, c, lbl);
        h.setAlignment(Pos.CENTER_LEFT);
        return h;
    }

    // ════════════════════════════════════════════════════════════════════════
    // CENTRO
    // ════════════════════════════════════════════════════════════════════════
    private BorderPane buildCentro() {
        BorderPane centro = new BorderPane();
        centro.setCenter(buildMapaStack());
        centro.setRight(buildPanelDerecho());
        return centro;
    }

    // ════════════════════════════════════════════════════════════════════════
    // MAPA
    // ════════════════════════════════════════════════════════════════════════
    private StackPane buildMapaStack() {
        SwingNode swingNode = new SwingNode();
        SwingUtilities.invokeLater(() -> inicializarMapa(swingNode));

        Label lblHint = new Label("📍  Haz clic en una alarma para ver su detalle");
        lblHint.setFont(Font.font("Arial", 12));
        lblHint.setTextFill(Color.web("#374151"));
        lblHint.setStyle(
                "-fx-background-color:rgba(255,255,255,0.92);"
                + "-fx-background-radius:20;-fx-padding:6 18 6 18;"
                + "-fx-border-color:" + C_SEPARATOR + ";"
                + "-fx-border-radius:20;-fx-border-width:1;"
                + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.10),8,0,0,2);");

        StackPane stack = new StackPane(swingNode, lblHint);
        StackPane.setAlignment(lblHint, Pos.BOTTOM_CENTER);
        StackPane.setMargin(lblHint, new Insets(0, 0, 18, 0));
        return stack;
    }

    private void inicializarMapa(SwingNode swingNode) {
        mapa = new JXMapViewer();
        TileFactoryInfo info = new TileFactoryInfo(
                1, 15, 17, 256, true, true,
                "https://tile.openstreetmap.org", "x", "y", "z") {
            @Override
            public String getTileUrl(int x, int y, int zoom) {
                return baseURL + "/" + (17 - zoom) + "/" + x + "/" + y + ".png";
            }
        };
        mapa.setTileFactory(new DefaultTileFactory(info));
        mapa.setAddressLocation(new GeoPosition(10.4795, -73.2536));
        mapa.setZoom(4);

        PanMouseInputListener pan = new PanMouseInputListener(mapa);
        mapa.addMouseListener(pan);
        mapa.addMouseMotionListener(pan);
        mapa.addMouseWheelListener(new ZoomMouseWheelListenerCenter(mapa));
        mapa.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onMapaClick(e);
            }
        });
        mapa.setOverlayPainter(this::pintarOverlay);
        swingNode.setContent(mapa);
    }

    // Information Expert: click detecta alarma más cercana al punto
    private void onMapaClick(MouseEvent e) {
        if (alarmasFiltradas.isEmpty()) {
            return;
        }

        Alarma encontrada = null;
        double distMin = Double.MAX_VALUE;

        for (Alarma a : alarmasFiltradas) {
            GeoPosition gp = new GeoPosition(a.getLatitud(), a.getLongitud());
            Point2D pt = mapa.getTileFactory().geoToPixel(gp, mapa.getZoom());
            int cx = (int) pt.getX() - mapa.getViewportBounds().x;
            int cy = (int) pt.getY() - mapa.getViewportBounds().y;
            double dist = Math.hypot(e.getX() - cx, e.getY() - cy);
            if (dist < 20 && dist < distMin) {
                distMin = dist;
                encontrada = a;
            }
        }

        final Alarma seleccionada = encontrada;
        Platform.runLater(() -> mostrarDetalle(seleccionada));
        alarmaSeleccionada = encontrada;
        SwingUtilities.invokeLater(mapa::repaint);
    }

    // High Cohesion: pintura de todas las alarmas filtradas
    private void pintarOverlay(Graphics2D g, JXMapViewer map, int w, int h) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (Alarma alarma : alarmasFiltradas) {
            GeoPosition gp = new GeoPosition(alarma.getLatitud(), alarma.getLongitud());
            Point2D pt = map.getTileFactory().geoToPixel(gp, map.getZoom());
            int cx = (int) pt.getX() - map.getViewportBounds().x;
            int cy = (int) pt.getY() - map.getViewportBounds().y;

            java.awt.Color colorEstado = colorPorEstado(alarma.getEstado());

            // Radio de cobertura
            int radioM = (int) alarma.getRadio_cobertura();
            if (radioM > 0) {
                double mpp = 156543.03392
                        * Math.cos(Math.toRadians(alarma.getLatitud()))
                        / Math.pow(2, 17 - map.getZoom());
                int rp = (int) (radioM / mpp);

                // Relleno semitransparente del color del estado
                java.awt.Color relleno = new java.awt.Color(
                        colorEstado.getRed(), colorEstado.getGreen(),
                        colorEstado.getBlue(), 30);
                g.setColor(relleno);
                g.fill(new Ellipse2D.Double(cx - rp, cy - rp, rp * 2, rp * 2));

                // Borde del radio
                java.awt.Color borde = new java.awt.Color(
                        colorEstado.getRed(), colorEstado.getGreen(),
                        colorEstado.getBlue(), 160);
                g.setColor(borde);

                // Borde más grueso si está seleccionada
                float strokeW = (alarma == alarmaSeleccionada) ? 2.5f : 1.5f;
                g.setStroke(new BasicStroke(strokeW, BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND, 0, new float[]{7, 5}, 0));
                g.draw(new Ellipse2D.Double(cx - rp, cy - rp, rp * 2, rp * 2));
            }

            // Marcador PNG con sombra de selección
            if (alarma == alarmaSeleccionada) {
                g.setColor(new java.awt.Color(22, 40, 61, 60));
                g.fillOval(cx - 18, cy - 2, 36, 10);
            }
            try {
                java.io.InputStream is = getClass().getResourceAsStream("/SirenaPin.png");
                if (is != null) {
                    java.awt.image.BufferedImage img
                            = recortarTransparencia(javax.imageio.ImageIO.read(is));
                    int iw = (alarma == alarmaSeleccionada) ? 38 : 28;
                    int ih = (int) (img.getHeight() * (iw / (double) img.getWidth()));
                    g.drawImage(img, cx - iw / 2, cy - ih, iw, ih, null);
                }
            } catch (Exception ignored) {
            }

            // Punto de color del estado sobre el marcador
            g.setColor(colorEstado);
            g.setStroke(new BasicStroke(1.5f));
            int pr = 5;
            g.fillOval(cx + 6, cy - 28, pr * 2, pr * 2);
            g.setColor(java.awt.Color.WHITE);
            g.drawOval(cx + 6, cy - 28, pr * 2, pr * 2);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // PANEL DERECHO — filtros + detalle
    // ════════════════════════════════════════════════════════════════════════
    private VBox buildPanelDerecho() {
        VBox panel = new VBox(buildPanelHeader(), buildPanelFiltros(), buildPanelDetalle());
        panel.setPrefWidth(310);
        panel.setMaxWidth(310);
        panel.setStyle("-fx-background-color:white;-fx-border-color:" + C_SEPARATOR
                + ";-fx-border-width:0 0 0 1;");
        return panel;
    }

    private HBox buildPanelHeader() {
        Label titulo = new Label("Alarmas registradas");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        titulo.setTextFill(Color.web("#111827"));

        Label sub = new Label("Visualiza y filtra las alarmas");
        sub.setFont(Font.font("Arial", 11));
        sub.setTextFill(Color.web("#6b7280"));

        Label icono = new Label("📡");
        icono.setFont(Font.font(18));
        icono.setTextFill(Color.WHITE);
        icono.setStyle("-fx-background-color:" + C_DARK_GRAD
                + ";-fx-background-radius:8;-fx-padding:6 10 6 10;");

        HBox header = new HBox(12, icono, new VBox(2, titulo, sub));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(16, 20, 16, 20));
        header.setStyle("-fx-background-color:white;-fx-border-color:" + C_SEPARATOR
                + ";-fx-border-width:0 0 1 0;");
        return header;
    }

    // ── Filtros ───────────────────────────────────────────────────────────────
    private VBox buildPanelFiltros() {
        // ── Campo de búsqueda ──────────────────────────────────────────────
        txtBusqueda = new TextField();
        txtBusqueda.setPromptText("🔍  Buscar por nombre...");
        estilizarInput(txtBusqueda);

        // ── Lista de sugerencias (autocomplete) ────────────────────────────
        lstSugerencias = new ListView<>();
        lstSugerencias.setPrefHeight(0);
        lstSugerencias.setMaxHeight(0);
        lstSugerencias.setStyle(
                "-fx-background-color:white;"
                + "-fx-border-color:" + C_SEPARATOR + ";"
                + "-fx-border-width:1;-fx-border-radius:10;"
                + "-fx-background-radius:10;"
                + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.10),10,0,0,4);");

        // Celda personalizada: muestra nombre + estado con color
        lstSugerencias.setCellFactory(lv -> new ListCell<Alarma>() {
            @Override
            protected void updateItem(Alarma item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setStyle("-fx-background-color:transparent;");
                } else {
                    String nombre = item.getNombre() != null ? item.getNombre() : "—";
                    String estado = item.getEstado() != null ? item.getEstado().name() : "—";
                    String colorPunto = switch (estado) {
                        case "ACTIVA" ->
                            "#22c55e";
                        case "INACTIVA" ->
                            "#9ca3af";
                        case "MANTENIMIENTO" ->
                            "#f59e0b";
                        default ->
                            "#374151";
                    };

                    javafx.scene.shape.Circle punto = new javafx.scene.shape.Circle(5);
                    punto.setFill(Color.web(colorPunto));

                    Label lblNombre = new Label(nombre);
                    lblNombre.setFont(Font.font("Arial", FontWeight.BOLD, 13));
                    lblNombre.setTextFill(Color.web("#111827"));

                    Label lblEstado = new Label(estado);
                    lblEstado.setFont(Font.font("Arial", 10));
                    lblEstado.setTextFill(Color.web(colorPunto));

                    VBox textos = new VBox(1, lblNombre, lblEstado);
                    HBox fila = new HBox(10, punto, textos);
                    fila.setAlignment(Pos.CENTER_LEFT);
                    fila.setPadding(new Insets(6, 10, 6, 10));
                    setGraphic(fila);
                    setStyle("-fx-background-color:transparent;-fx-cursor:hand;");

                    setOnMouseEntered(e
                            -> setStyle("-fx-background-color:#f0f4ff;-fx-cursor:hand;"));
                    setOnMouseExited(e
                            -> setStyle("-fx-background-color:transparent;-fx-cursor:hand;"));
                }
            }
        });

        // Al escribir → mostrar sugerencias filtradas
        txtBusqueda.textProperty().addListener((obs, ov, nv) -> {
            actualizarSugerencias(nv);
            aplicarFiltros();
        });

        // Al perder foco → ocultar sugerencias (con pequeño delay para permitir el clic)
        txtBusqueda.focusedProperty().addListener((obs, ov, focused) -> {
            if (!focused) {
                new Thread(() -> {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException ignored) {
                    }
                    Platform.runLater(this::ocultarSugerencias);
                }).start();
            }
        });

        // Al seleccionar una sugerencia → centrar mapa y mostrar detalle
        // MousePressed garantiza que el ítem ya está seleccionado antes de que el
        // foco del TextField pueda cancelar el evento
        lstSugerencias.setOnMousePressed(e -> {
            int idx = lstSugerencias.getSelectionModel().getSelectedIndex();
            if (idx < 0) {
                return;
            }
            Alarma elegida = lstSugerencias.getItems().get(idx);
            Platform.runLater(() -> {
                ocultarSugerencias();
                txtBusqueda.setText(elegida.getNombre());
                seleccionarAlarma(elegida);
            });
        });

        panelSugerencias = new VBox(lstSugerencias);
        panelSugerencias.setVisible(false);
        panelSugerencias.setManaged(false);

        // ── Filtro de estado ───────────────────────────────────────────────
        cmbFiltroEstado = new ComboBox<>(FXCollections.observableArrayList(
                "Todos", "ACTIVA", "INACTIVA", "MANTENIMIENTO"));
        cmbFiltroEstado.setValue("Todos");
        cmbFiltroEstado.setMaxWidth(Double.MAX_VALUE);
        estilizarCombo(cmbFiltroEstado);
        cmbFiltroEstado.setOnAction(e -> aplicarFiltros());

        // Botón recargar
        Button btnRecargar = new Button("⟳  Recargar");
        btnRecargar.setMaxWidth(Double.MAX_VALUE);
        btnRecargar.setPrefHeight(36);
        btnRecargar.setFont(Font.font("Arial", 12));
        btnRecargar.setStyle(
                "-fx-background-color:white;-fx-text-fill:#374151;"
                + "-fx-border-color:#d1d5db;-fx-border-width:1;"
                + "-fx-cursor:hand;-fx-background-radius:8;-fx-border-radius:8;");
        btnRecargar.setOnAction(e -> {
            cargarAlarmas();
            alarmaSeleccionada = null;
            mostrarDetalle(null);
            ocultarSugerencias();
            txtBusqueda.clear();
        });

        VBox filtros = new VBox(6,
                campo("Buscar alarma", txtBusqueda),
                panelSugerencias,
                campo("Filtrar por estado", cmbFiltroEstado),
                btnRecargar);
        filtros.setPadding(new Insets(16, 20, 16, 20));
        filtros.setStyle("-fx-background-color:white;-fx-border-color:" + C_SEPARATOR
                + ";-fx-border-width:0 0 1 0;");
        return filtros;
    }

    // ── Actualiza la lista de sugerencias según lo escrito ─────────────────
    private void actualizarSugerencias(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            ocultarSugerencias();
            return;
        }
        String busq = texto.toLowerCase().trim();
        List<Alarma> coincidencias = todasLasAlarmas.stream()
                .filter(a -> a.getNombre() != null
                && a.getNombre().toLowerCase().contains(busq))
                .limit(6) // máximo 6 sugerencias visibles
                .collect(Collectors.toList());

        if (coincidencias.isEmpty()) {
            ocultarSugerencias();
            return;
        }

        ObservableList<Alarma> items = FXCollections.observableArrayList(coincidencias);
        lstSugerencias.setItems(items);

        // Altura dinámica: ~52px por fila, máximo 6
        double altura = Math.min(coincidencias.size(), 6) * 52.0;
        lstSugerencias.setPrefHeight(altura);
        lstSugerencias.setMaxHeight(altura);

        panelSugerencias.setVisible(true);
        panelSugerencias.setManaged(true);
    }

    private void ocultarSugerencias() {
        panelSugerencias.setVisible(false);
        panelSugerencias.setManaged(false);
        lstSugerencias.setPrefHeight(0);
        lstSugerencias.setMaxHeight(0);
    }

    // ── Selecciona una alarma, centra el mapa y muestra su detalle ──────────
    private void seleccionarAlarma(Alarma alarma) {
        if (alarma == null) {
            return;
        }
        alarmaSeleccionada = alarma;

        // Restaurar lista completa para que el pin sea visible en el mapa
        alarmasFiltradas = new ArrayList<>(todasLasAlarmas);

        // Mostrar detalle en el panel (debe correr en FX thread)
        Platform.runLater(() -> mostrarDetalle(alarma));

        // Centrar y hacer zoom en el mapa (corre en Swing thread)
        if (mapa != null) {
            GeoPosition gp = new GeoPosition(alarma.getLatitud(), alarma.getLongitud());
            SwingUtilities.invokeLater(() -> {
                mapa.setAddressLocation(gp);
                mapa.setZoom(3);
                mapa.repaint();
            });
        }

        int count = alarmasFiltradas.size();
        Platform.runLater(()
                -> lblContador.setText(count + (count == 1 ? " alarma" : " alarmas")));
    }

    // ── Panel de detalle ──────────────────────────────────────────────────────
    private ScrollPane buildPanelDetalle() {
        // Labels de detalle
        lblDetalleNombre = filaDetalle("—");
        lblDetalleBarrio = filaDetalle("—");
        lblDetalleCoordenadas = filaDetalle("—");
        lblDetalleRadio = filaDetalle("—");
        lblDetalleEstado = filaDetalle("—");

        panelDetalle = new VBox(10,
                new Label("Selecciona una alarma en el mapa") {
            {
                setFont(Font.font("Arial", 12));
                setTextFill(Color.web("#9ca3af"));
                setWrapText(true);
            }
        },
                grupoDetalle("Nombre", lblDetalleNombre),
                grupoDetalle("Barrio", lblDetalleBarrio),
                grupoDetalle("Coordenadas", lblDetalleCoordenadas),
                grupoDetalle("Radio (m)", lblDetalleRadio),
                grupoDetalle("Estado", lblDetalleEstado)
        );
        panelDetalle.setPadding(new Insets(16, 20, 16, 20));
        panelDetalle.setStyle("-fx-background-color:white;");

        // Ocultar filas de datos hasta que haya selección
        for (int i = 1; i < panelDetalle.getChildren().size(); i++) {
            panelDetalle.getChildren().get(i).setVisible(false);
        }

        ScrollPane scroll = new ScrollPane(panelDetalle);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:white;-fx-background:white;");
        VBox.setVgrow(scroll, Priority.ALWAYS);
        return scroll;
    }

    private Label filaDetalle(String txt) {
        Label lbl = new Label(txt);
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        lbl.setTextFill(Color.web("#111827"));
        lbl.setWrapText(true);
        return lbl;
    }

    private VBox grupoDetalle(String etiqueta, Label valor) {
        Label lbl = new Label(etiqueta);
        lbl.setFont(Font.font("Arial", 11));
        lbl.setTextFill(Color.web("#6b7280"));
        VBox vb = new VBox(2, lbl, valor);
        vb.setStyle("-fx-background-color:#f9fafb;-fx-background-radius:8;"
                + "-fx-padding:8 12 8 12;");
        return vb;
    }

    // ════════════════════════════════════════════════════════════════════════
    // LÓGICA DE NEGOCIO
    // ════════════════════════════════════════════════════════════════════════
    // Information Expert: cargar alarmas y pintarlas
    private void cargarAlarmas() {
        try {
            todasLasAlarmas = alarmaService.listar();
        } catch (Exception e) {
            System.out.println("Error cargando alarmas: " + e.getMessage());
            todasLasAlarmas = new ArrayList<>();
        }
        aplicarFiltros();
    }

    // Information Expert: filtrar lista y actualizar mapa + contador
    private void aplicarFiltros() {
        String busqueda = (txtBusqueda != null && txtBusqueda.getText() != null)
                ? txtBusqueda.getText().toLowerCase().trim() : "";
        String estado = (cmbFiltroEstado != null) ? cmbFiltroEstado.getValue() : "Todos";

        alarmasFiltradas = todasLasAlarmas.stream()
                .filter(a -> busqueda.isEmpty() || a.getNombre().toLowerCase().contains(busqueda))
                .filter(a -> "Todos".equals(estado) || (a.getEstado() != null
                && a.getEstado().name().equals(estado)))
                .collect(Collectors.toList());

        int count = alarmasFiltradas.size();
        Platform.runLater(()
                -> lblContador.setText(count + (count == 1 ? " alarma" : " alarmas")));

        // Si la seleccionada ya no está en el filtro, limpiarla
        if (alarmaSeleccionada != null && !alarmasFiltradas.contains(alarmaSeleccionada)) {
            alarmaSeleccionada = null;
            Platform.runLater(() -> mostrarDetalle(null));
        }

        if (mapa != null) {
            SwingUtilities.invokeLater(mapa::repaint);
        }

        // Centrar el mapa en la primera alarma si hay resultados
        if (!alarmasFiltradas.isEmpty() && mapa != null) {
            Alarma primera = alarmasFiltradas.get(0);
            SwingUtilities.invokeLater(()
                    -> mapa.setAddressLocation(
                            new GeoPosition(primera.getLatitud(), primera.getLongitud())));
        }
    }

    // Information Expert: mostrar detalle de la alarma seleccionada
    private void mostrarDetalle(Alarma a) {
        boolean haySeleccion = a != null;

        // Mostrar/ocultar filas de detalle
        for (int i = 1; i < panelDetalle.getChildren().size(); i++) {
            panelDetalle.getChildren().get(i).setVisible(haySeleccion);
        }

        // Actualizar texto del hint
        if (!haySeleccion) {
            ((Label) panelDetalle.getChildren().get(0)).setText(
                    "Selecciona una alarma en el mapa");
            return;
        }
        ((Label) panelDetalle.getChildren().get(0)).setText("Alarma seleccionada");

        lblDetalleNombre.setText(a.getNombre());
        lblDetalleBarrio.setText(a.getBarrio() != null ? a.getBarrio().getNombre() : "—");
        lblDetalleCoordenadas.setText(String.format("%.5f,  %.5f",
                a.getLatitud(), a.getLongitud()));
        lblDetalleRadio.setText((int) a.getRadio_cobertura() + " m");

        // Estado con color
        String estadoStr = a.getEstado() != null ? a.getEstado().name() : "—";
        lblDetalleEstado.setText(estadoStr);
        String colorEstado = switch (estadoStr) {
            case "ACTIVA" ->
                "#22c55e";
            case "INACTIVA" ->
                "#9ca3af";
            case "MANTENIMIENTO" ->
                "#f59e0b";
            default ->
                "#374151";
        };
        lblDetalleEstado.setTextFill(Color.web(colorEstado));
    }

    // ════════════════════════════════════════════════════════════════════════
    // HELPERS DE UI
    // ════════════════════════════════════════════════════════════════════════
    private VBox campo(String etiqueta, javafx.scene.Node control) {
        Label lbl = new Label(etiqueta);
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        lbl.setTextFill(Color.web(C_LABEL));
        return new VBox(6, lbl, control);
    }

    private void estilizarInput(TextField tf) {
        tf.setStyle(
                "-fx-background-color:" + C_INPUT_BG + ";"
                + "-fx-text-fill:" + C_INPUT_TEXT + ";"
                + "-fx-prompt-text-fill:#9ca3af;"
                + "-fx-border-color:transparent;"
                + "-fx-border-radius:30;-fx-background-radius:30;"
                + "-fx-padding:14 20 14 20;-fx-font-size:14px;");
        tf.setPrefHeight(48);
    }

    private void estilizarCombo(ComboBox<String> cb) {
        cb.setStyle(
                "-fx-background-color:" + C_INPUT_BG + ";"
                + "-fx-text-fill:" + C_INPUT_TEXT + ";"
                + "-fx-prompt-text-fill:#9ca3af;"
                + "-fx-border-color:transparent;"
                + "-fx-border-radius:30;-fx-background-radius:30;-fx-font-size:14px;");
        cb.setPrefHeight(48);

        cb.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
                setStyle("-fx-background-color:transparent;-fx-text-fill:#4b5563;"
                        + "-fx-font-size:14px;-fx-padding:8 14 8 14;");
                setOnMouseEntered(e -> setStyle(
                        "-fx-background-color:" + C_DARK_GRAD + ";"
                        + "-fx-background-radius:6;-fx-text-fill:white;"
                        + "-fx-font-size:14px;-fx-padding:8 14 8 14;"));
                setOnMouseExited(e -> setStyle(
                        "-fx-background-color:transparent;-fx-text-fill:#4b5563;"
                        + "-fx-font-size:14px;-fx-padding:8 14 8 14;"));
            }
        });

        cb.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null) {
                    setStyle("-fx-background-color:" + C_INPUT_BG + ";"
                            + "-fx-background-radius:30;-fx-text-fill:black;"
                            + "-fx-font-size:14px;-fx-padding:4 14 4 14;");
                } else {
                    setStyle("-fx-background-color:transparent;"
                            + "-fx-text-fill:#9ca3af;-fx-font-size:14px;");
                }
                setText(empty || item == null ? cb.getPromptText() : item);
            }
        });
    }

    // ── Color por estado de alarma ─────────────────────────────────────────────
    private java.awt.Color colorPorEstado(EstadoAlarma estado) {
        if (estado == null) {
            return COLOR_INACTIVA;
        }
        return switch (estado) {
            case ACTIVA ->
                COLOR_ACTIVA;
            case INACTIVA ->
                COLOR_INACTIVA;
            case EN_MANTENIMIENTO ->
                COLOR_MANTENIMIENTO;
        };
    }

    // ════════════════════════════════════════════════════════════════════════
    // UTILIDAD AWT
    // ════════════════════════════════════════════════════════════════════════
    private java.awt.image.BufferedImage recortarTransparencia(java.awt.image.BufferedImage img) {
        int w = img.getWidth(), h = img.getHeight();
        int top = h, bottom = 0, left = w, right = 0;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (((img.getRGB(x, y) >> 24) & 0xff) > 10) {
                    if (y < top) {
                        top = y;
                    }
                    if (y > bottom) {
                        bottom = y;
                    }
                    if (x < left) {
                        left = x;
                    }
                    if (x > right) {
                        right = x;
                    }
                }
            }
        }
        if (top >= bottom || left >= right) {
            return img;
        }
        return img.getSubimage(left, top, right - left + 1, bottom - top + 1);
    }
}
