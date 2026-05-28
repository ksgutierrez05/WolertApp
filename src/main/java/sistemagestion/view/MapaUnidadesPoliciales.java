/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.view;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCenter;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;
import sistemagestion.model.Barrio;
import sistemagestion.model.Comuna;
import sistemagestion.model.EstadoUnidadPolicial;
import sistemagestion.model.UnidadPolicial;
import sistemagestion.service.BarrioService;
import sistemagestion.service.ComunaService;
import sistemagestion.service.UnidadPolicialService;

import javax.swing.SwingUtilities;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.sql.SQLException;
import java.util.List;

public class MapaUnidadesPoliciales {

    // ── Paleta ────────────────────────────────────────────────────────────────
    private static final String C_INPUT_BG   = "#f7f7f7";
    private static final String C_INPUT_TEXT = "#4b5563";
    private static final String C_LABEL      = "#374151";
    private static final String C_COORD_BG   = "#f0f4ff";
    private static final String C_COORD_BDR  = "#c7d7fd";
    private static final String C_COORD_VAL  = "#1e3a8a";
    private static final String C_SEPARATOR  = "#e5e7eb";
    private static final String C_DARK_GRAD  = "linear-gradient(to right, #16283d, #1f3a56)";
    private static final String C_GREEN      = "#16a34a";

    // ── Servicios ─────────────────────────────────────────────────────────────
    private UnidadPolicialService unidadService;
    private BarrioService         barrioService;
    private ComunaService         comunaService;

    // ── Mapa ──────────────────────────────────────────────────────────────────
    private JXMapViewer  mapa;
    private GeoPosition  posicionSeleccionada;

    // ── Controles ─────────────────────────────────────────────────────────────
    private TextField                      txtNombre;
    private ComboBox<EstadoUnidadPolicial> cmbEstado;
    private ComboBox<String>               cmbComuna;
    private ComboBox<Barrio>               cmbBarrio;
    private Label                          lblCoordenadas;
    private Label                          lblCoordFooter;
    private Label                          lblInstruccionHeader;
    private Label                          lblTituloPanel;
    private Label                          lblSubtituloPanel;

    // ── Botones ───────────────────────────────────────────────────────────────
    private Button btnAccionPrincipal;
    private Button btnEliminar;

    // ── Estado edición ────────────────────────────────────────────────────────
    private UnidadPolicial unidadEditando = null;

    // ── Constructor ───────────────────────────────────────────────────────────
    public MapaUnidadesPoliciales() {
        try { unidadService = new UnidadPolicialService(); }
        catch (SQLException e) { System.out.println("Error UnidadService: " + e.getMessage()); }
        try { barrioService = new BarrioService(); }
        catch (SQLException e) { System.out.println("Error BarrioService: " + e.getMessage()); }
        try { comunaService = new ComunaService(); }
        catch (SQLException e) { System.out.println("Error ComunaService: " + e.getMessage()); }
    }

    // ─────────────────────────────────────────────────────────────────────────
    public void mostrar() {
        Stage stage = new Stage();
        stage.setTitle("WolertApp — Unidades Policiales");

        BorderPane root = new BorderPane();
        root.setTop(buildTopBar());
        root.setCenter(buildCentro());
        root.setStyle("-fx-background-color:white;");

        stage.setScene(new Scene(root, 1200, 720));
        stage.setResizable(true);
        stage.show();
    }

    // ════════════════════════════════════════════════════════════════════════
    // BARRA SUPERIOR
    // ════════════════════════════════════════════════════════════════════════
    private HBox buildTopBar() {
        javafx.scene.image.ImageView logoImg = new javafx.scene.image.ImageView();
        try {
            java.awt.image.BufferedImage raw = javax.imageio.ImageIO.read(
                    getClass().getResourceAsStream("/LogoWolertApp.png"));
            logoImg.setImage(javafx.embed.swing.SwingFXUtils.toFXImage(
                    recortarTransparencia(raw), null));
        } catch (Exception ignored) {}
        logoImg.setFitHeight(24); logoImg.setFitWidth(24); logoImg.setPreserveRatio(true);

        StackPane logoBox = new StackPane(logoImg);
        logoBox.setStyle("-fx-background-color:" + C_DARK_GRAD
                + ";-fx-background-radius:6;-fx-padding:6;");

        Label logoText = new Label("WolertApp");
        logoText.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        logoText.setTextFill(Color.web("#111827"));

        Region sep = new Region();
        sep.setStyle("-fx-background-color:" + C_SEPARATOR + ";");
        sep.setPrefSize(1, 20);

        lblInstruccionHeader = new Label("Haz clic en el mapa para ubicar la unidad policial");
        lblInstruccionHeader.setFont(Font.font("Arial", 13));
        lblInstruccionHeader.setTextFill(Color.web("#6b7280"));

        Button btnVerUnidades = new Button("🔍  Ver unidades");
        btnVerUnidades.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        String estNormal = "-fx-background-color:white;-fx-text-fill:#16283d;"
                + "-fx-border-color:#c7d7fd;-fx-border-width:1.5;"
                + "-fx-border-radius:8;-fx-background-radius:8;"
                + "-fx-cursor:hand;-fx-padding:7 16 7 16;";
        String estHover = "-fx-background-color:#f0f4ff;-fx-text-fill:#16283d;"
                + "-fx-border-color:#93c5fd;-fx-border-width:1.5;"
                + "-fx-border-radius:8;-fx-background-radius:8;"
                + "-fx-cursor:hand;-fx-padding:7 16 7 16;";
        btnVerUnidades.setStyle(estNormal);
        btnVerUnidades.setOnMouseEntered(e -> btnVerUnidades.setStyle(estHover));
        btnVerUnidades.setOnMouseExited(e  -> btnVerUnidades.setStyle(estNormal));
        btnVerUnidades.setOnAction(e -> abrirListaUnidades());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox bar = new HBox(10, logoBox, logoText, sep,
                lblInstruccionHeader, spacer, btnVerUnidades);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(12, 20, 12, 20));
        bar.setStyle("-fx-background-color:white;-fx-border-color:" + C_SEPARATOR
                + ";-fx-border-width:0 0 1 0;");
        return bar;
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

        lblCoordFooter = new Label("📍  —,  —");
        lblCoordFooter.setFont(Font.font("Arial", 12));
        lblCoordFooter.setTextFill(Color.web("#374151"));
        lblCoordFooter.setStyle(
                "-fx-background-color:rgba(255,255,255,0.92);"
                + "-fx-background-radius:20;-fx-padding:6 18 6 18;"
                + "-fx-border-color:" + C_SEPARATOR + ";"
                + "-fx-border-radius:20;-fx-border-width:1;"
                + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.10),8,0,0,2);");

        StackPane stack = new StackPane(swingNode, lblCoordFooter);
        StackPane.setAlignment(lblCoordFooter, Pos.BOTTOM_CENTER);
        StackPane.setMargin(lblCoordFooter, new Insets(0, 0, 18, 0));
        return stack;
    }

    private void inicializarMapa(SwingNode swingNode) {
        mapa = new JXMapViewer();
        TileFactoryInfo info = new TileFactoryInfo(
                1, 15, 17, 256, true, true,
                "https://tile.openstreetmap.org", "x", "y", "z") {
            @Override public String getTileUrl(int x, int y, int zoom) {
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
            @Override public void mouseClicked(MouseEvent e) { onMapaClick(e); }
        });
        mapa.setOverlayPainter(this::pintarOverlay);
        swingNode.setContent(mapa);
    }

    private void onMapaClick(MouseEvent e) {
        posicionSeleccionada = mapa.convertPointToGeoPosition(e.getPoint());
        mapa.repaint();
        String coord = String.format("%.5f,  %.5f",
                posicionSeleccionada.getLatitude(), posicionSeleccionada.getLongitude());
        Platform.runLater(() -> {
            lblCoordenadas.setText(coord);
            lblCoordFooter.setText("📍  " + coord);
            lblInstruccionHeader.setText("Ubicación seleccionada");
            lblInstruccionHeader.setTextFill(Color.web(C_GREEN));
        });
    }

private void pintarOverlay(Graphics2D g, JXMapViewer map, int w, int h) {
    if (posicionSeleccionada == null) return;
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    Point2D pt = map.getTileFactory().geoToPixel(posicionSeleccionada, map.getZoom());
    int cx = (int) pt.getX() - map.getViewportBounds().x;
    int cy = (int) pt.getY() - map.getViewportBounds().y;

    try {
        java.io.InputStream is = getClass().getResourceAsStream("/PinPolicia.png");
        if (is != null) {
            java.awt.image.BufferedImage img =
                    recortarTransparencia(javax.imageio.ImageIO.read(is));
            int iw = 32, ih = (int) (img.getHeight() * (32.0 / img.getWidth()));
            g.drawImage(img, cx - iw / 2, cy - ih, iw, ih, null);
        }
    } catch (Exception ignored) {}
}

    // ════════════════════════════════════════════════════════════════════════
    // PANEL DERECHO
    // ════════════════════════════════════════════════════════════════════════
    private VBox buildPanelDerecho() {
        VBox panel = new VBox(buildPanelHeader(), buildScroll());
        panel.setPrefWidth(320);
        panel.setMaxWidth(320);
        panel.setStyle("-fx-background-color:white;-fx-border-color:" + C_SEPARATOR
                + ";-fx-border-width:0 0 0 1;");
        VBox.setVgrow(panel.getChildren().get(1), Priority.ALWAYS);
        return panel;
    }

    private HBox buildPanelHeader() {
        lblTituloPanel = new Label("Registrar unidad");
        lblTituloPanel.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        lblTituloPanel.setTextFill(Color.web("#111827"));

        lblSubtituloPanel = new Label("Completa los datos y ubica en el mapa");
        lblSubtituloPanel.setFont(Font.font("Arial", 11));
        lblSubtituloPanel.setTextFill(Color.web("#6b7280"));

        Label icono = new Label("\uf505");
        icono.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';"
                + "-fx-font-size:16px;-fx-text-fill:white;"
                + "-fx-background-color:" + C_DARK_GRAD
                + ";-fx-background-radius:8;-fx-padding:7 10 7 10;");

        HBox header = new HBox(12, icono, new VBox(2, lblTituloPanel, lblSubtituloPanel));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(16, 20, 16, 20));
        header.setStyle("-fx-background-color:white;-fx-border-color:" + C_SEPARATOR
                + ";-fx-border-width:0 0 1 0;");
        return header;
    }

    private ScrollPane buildScroll() {
        VBox form = buildFormulario();
        form.setPadding(new Insets(18, 20, 18, 20));
        form.setStyle("-fx-background-color:white;");

        ScrollPane scroll = new ScrollPane(form);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:white;-fx-background:white;");
        return scroll;
    }

    // ════════════════════════════════════════════════════════════════════════
    // FORMULARIO
    // ════════════════════════════════════════════════════════════════════════
    private VBox buildFormulario() {
        txtNombre = new TextField();
        txtNombre.setPromptText("Ej: CAI Centro");
        estilizarInput(txtNombre);

        cmbEstado = new ComboBox<>(FXCollections.observableArrayList(EstadoUnidadPolicial.values()));
        cmbEstado.setPromptText("Seleccione estado");
        cmbEstado.setMaxWidth(Double.MAX_VALUE);
        estilizarComboEnum(cmbEstado);

        // ── Comuna ────────────────────────────────────────────────────────────
        cmbComuna = new ComboBox<>();
        cmbComuna.setPromptText("Seleccione una comuna");
        cmbComuna.setMaxWidth(Double.MAX_VALUE);
        estilizarComboString(cmbComuna);
        cargarComunas();

        // ── Barrio (filtrado por comuna) ───────────────────────────────────────
        cmbBarrio = new ComboBox<>();
        cmbBarrio.setPromptText("Seleccione un barrio");
        cmbBarrio.setMaxWidth(Double.MAX_VALUE);
        cmbBarrio.setDisable(true);
        estilizarComboBarrio(cmbBarrio);

        cmbComuna.setOnAction(e -> filtrarBarrios());

        lblCoordenadas = new Label("—,  —");
        lblCoordenadas.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        lblCoordenadas.setTextFill(Color.web(C_COORD_VAL));

        Label lblCoordTitulo = new Label("Ubicación seleccionada");
        lblCoordTitulo.setFont(Font.font("Arial", 11));
        lblCoordTitulo.setTextFill(Color.web("#6b7280"));

        HBox coordBox = new HBox(10, new Label("📍"),
                new VBox(2, lblCoordTitulo, lblCoordenadas));
        coordBox.setAlignment(Pos.CENTER_LEFT);
        coordBox.setPadding(new Insets(12, 14, 12, 14));
        coordBox.setStyle("-fx-background-color:" + C_COORD_BG
                + ";-fx-border-color:" + C_COORD_BDR
                + ";-fx-border-width:1;-fx-border-radius:10;-fx-background-radius:10;");

        // Botón principal dinámico (Guardar / Actualizar)
        btnAccionPrincipal = buildBotonPrimario("Guardar unidad");
        btnAccionPrincipal.setOnAction(e -> {
            if (unidadEditando == null) guardarUnidad();
            else actualizarUnidad();
        });

        // Botón Eliminar (rojo, oculto en modo registrar)
        btnEliminar = new Button("🗑  Eliminar unidad");
        btnEliminar.setMaxWidth(Double.MAX_VALUE);
        btnEliminar.setPrefHeight(38);
        btnEliminar.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        String estElimN = "-fx-background-color:#fef2f2;-fx-text-fill:#dc2626;"
                + "-fx-border-color:#fecaca;-fx-border-width:1;"
                + "-fx-cursor:hand;-fx-background-radius:8;-fx-border-radius:8;";
        String estElimH = "-fx-background-color:#fee2e2;-fx-text-fill:#b91c1c;"
                + "-fx-border-color:#fca5a5;-fx-border-width:1;"
                + "-fx-cursor:hand;-fx-background-radius:8;-fx-border-radius:8;";
        btnEliminar.setStyle(estElimN);
        btnEliminar.setOnMouseEntered(e -> btnEliminar.setStyle(estElimH));
        btnEliminar.setOnMouseExited(e  -> btnEliminar.setStyle(estElimN));
        btnEliminar.setOnAction(e -> eliminarUnidad());
        btnEliminar.setVisible(false);
        btnEliminar.setManaged(false);

        Button btnSecundario = buildBotonSecundario("Limpiar");
        btnSecundario.setOnAction(e -> salirModoEdicion());

        return new VBox(14,
                campo("Nombre de la unidad", txtNombre),
                campo("Comuna",              cmbComuna),
                campo("Barrio",              cmbBarrio),
                campo("Estado",              cmbEstado),
                coordBox,
                btnAccionPrincipal,
                btnEliminar,
                btnSecundario
        );
    }

    // ════════════════════════════════════════════════════════════════════════
    // VENTANA MODAL — lista de unidades
    // ════════════════════════════════════════════════════════════════════════
    private void abrirListaUnidades() {
        List<UnidadPolicial> lista;
        try {
            lista = unidadService.listar();
        } catch (Exception e) {
            info("Error cargando unidades: " + e.getMessage());
            return;
        }
        if (lista == null || lista.isEmpty()) {
            info("No hay unidades registradas aún.");
            return;
        }

        Stage ventana = new Stage();
        ventana.initModality(Modality.APPLICATION_MODAL);
        ventana.setTitle("WolertApp — Unidades registradas");

        TableView<UnidadPolicial> tabla = new TableView<>();
        tabla.setStyle("-fx-background-color:white;-fx-border-color:" + C_SEPARATOR + ";");
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<UnidadPolicial, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getNombre()));

        TableColumn<UnidadPolicial, String> colBarrio = new TableColumn<>("Barrio");
        colBarrio.setCellValueFactory(c -> {
            Barrio b = c.getValue().getBarrio();
            return new SimpleStringProperty(b != null ? b.getNombre() : "—");
        });

        TableColumn<UnidadPolicial, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(c -> {
            EstadoUnidadPolicial est = c.getValue().getEstado();
            return new SimpleStringProperty(est != null ? labelEstado(est) : "—");
        });
        colEstado.setCellFactory(col -> new TableCell<UnidadPolicial, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label badge = new Label(item);
                badge.setFont(Font.font("Arial", FontWeight.BOLD, 10));
                String bg, fg;
                switch (item) {
                    case "Activa":    bg = "#dcfce7"; fg = "#14532d"; break;
                    case "Operativa": bg = "#dbeafe"; fg = "#1e3a8a"; break;
                    default:          bg = "#f1f5f9"; fg = "#475569"; break;
                }
                badge.setStyle("-fx-background-color:" + bg + ";-fx-text-fill:" + fg
                        + ";-fx-background-radius:20;-fx-padding:3 10 3 10;");
                setGraphic(badge);
            }
        });

        TableColumn<UnidadPolicial, Void> colAccion = new TableColumn<>("");
        colAccion.setPrefWidth(90);
        colAccion.setSortable(false);
        colAccion.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("✏  Editar");
            {
                btn.setFont(Font.font("Arial", FontWeight.BOLD, 11));
                btn.setStyle(
                        "-fx-background-color:#f0f4ff;-fx-text-fill:#16283d;"
                        + "-fx-border-color:#c7d7fd;-fx-border-width:1;"
                        + "-fx-border-radius:6;-fx-background-radius:6;"
                        + "-fx-cursor:hand;-fx-padding:4 10 4 10;");
                btn.setOnAction(e -> {
                    UnidadPolicial sel = getTableView().getItems().get(getIndex());
                    cargarUnidadEnFormulario(sel);
                    ventana.close();
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        tabla.getColumns().addAll(colNombre, colBarrio, colEstado, colAccion);
        tabla.setItems(FXCollections.observableArrayList(lista));

        Label titulo = new Label("Unidades registradas");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        titulo.setTextFill(Color.web("#111827"));

        Label sub = new Label("Selecciona una unidad para editarla");
        sub.setFont(Font.font("Arial", 11));
        sub.setTextFill(Color.web("#6b7280"));

        Label icono = new Label("\uf505");
        icono.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';"
                + "-fx-font-size:16px;-fx-text-fill:white;"
                + "-fx-background-color:" + C_DARK_GRAD
                + ";-fx-background-radius:8;-fx-padding:7 10 7 10;");

        HBox header = new HBox(12, icono, new VBox(2, titulo, sub));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(16, 20, 16, 20));
        header.setStyle("-fx-background-color:white;-fx-border-color:" + C_SEPARATOR
                + ";-fx-border-width:0 0 1 0;");

        Button btnCerrar = buildBotonSecundario("Cerrar");
        btnCerrar.setOnAction(e -> ventana.close());
        btnCerrar.setMaxWidth(Double.MAX_VALUE);

        VBox footerBox = new VBox(btnCerrar);
        footerBox.setPadding(new Insets(14, 20, 14, 20));
        footerBox.setStyle("-fx-background-color:white;");

        VBox layout = new VBox(header, tabla, footerBox);
        layout.setStyle("-fx-background-color:white;");
        VBox.setVgrow(tabla, Priority.ALWAYS);

        ventana.setScene(new Scene(layout, 700, 440));
        ventana.show();
    }

    // ════════════════════════════════════════════════════════════════════════
    // CARGAR EN FORMULARIO — modo edición
    // ════════════════════════════════════════════════════════════════════════
    private void cargarUnidadEnFormulario(UnidadPolicial u) {
        unidadEditando = u;

        lblTituloPanel.setText("Editar unidad");
        lblSubtituloPanel.setText("Modifica los datos y guarda los cambios");

        txtNombre.setText(u.getNombre());
        cmbEstado.setValue(u.getEstado());

        // Preseleccionar comuna → filtrar barrios → preseleccionar barrio
        if (u.getBarrio() != null) {
            if (u.getBarrio().getComuna() != null) {
                // Seleccionar la comuna primero; filtrarBarrios() se dispara por setOnAction
                cmbComuna.setValue(u.getBarrio().getComuna().getNombre());
                // Luego seleccionar el barrio una vez que la lista esté poblada
                Platform.runLater(() ->
                    cmbBarrio.getItems().stream()
                        .filter(b -> b.getId_barrio() == u.getBarrio().getId_barrio())
                        .findFirst()
                        .ifPresent(cmbBarrio::setValue)
                );
            } else {
                // Sin comuna anidada: intentar seleccionar el barrio directamente
                Platform.runLater(() ->
                    cmbBarrio.getItems().stream()
                        .filter(b -> b.getId_barrio() == u.getBarrio().getId_barrio())
                        .findFirst()
                        .ifPresent(cmbBarrio::setValue)
                );
            }
        }

        if (u.getLatitud() != 0 || u.getLongitud() != 0) {
            posicionSeleccionada = new GeoPosition(u.getLatitud(), u.getLongitud());
            String coord = String.format("%.5f,  %.5f", u.getLatitud(), u.getLongitud());
            lblCoordenadas.setText(coord);
            lblCoordFooter.setText("📍  " + coord);
            SwingUtilities.invokeLater(() -> {
                mapa.setAddressLocation(posicionSeleccionada);
                mapa.repaint();
            });
        }

        btnAccionPrincipal.setText("💾  Actualizar unidad");
        btnEliminar.setVisible(true);
        btnEliminar.setManaged(true);
        lblInstruccionHeader.setText("Editando: " + u.getNombre());
        lblInstruccionHeader.setTextFill(Color.web("#F97316"));
    }

    // ════════════════════════════════════════════════════════════════════════
    // GUARDAR — nueva unidad
    // ════════════════════════════════════════════════════════════════════════
    private void guardarUnidad() {
        if (!validar()) return;
        UnidadPolicial u = new UnidadPolicial();
        u.setNombre(txtNombre.getText().trim());
        u.setEstado(cmbEstado.getValue());
        u.setBarrio(cmbBarrio.getValue());
        u.setLatitud(posicionSeleccionada.getLatitude());
        u.setLongitud(posicionSeleccionada.getLongitude());
        try {
            unidadService.insertar(u);
            mostrarExito("Unidad registrada", "La unidad fue guardada correctamente.");
            salirModoEdicion();
        } catch (SQLException e) {
            alerta("Error al guardar: " + e.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // ACTUALIZAR — unidad existente
    // ════════════════════════════════════════════════════════════════════════
    private void actualizarUnidad() {
        if (!validar()) return;

        Barrio barrioSel = cmbBarrio.getValue();
        if (barrioSel == null) { alerta("Seleccione un barrio."); return; }

        unidadEditando.setNombre(txtNombre.getText().trim());
        unidadEditando.setEstado(cmbEstado.getValue());
        unidadEditando.setBarrio(barrioSel);
        unidadEditando.setLatitud(posicionSeleccionada.getLatitude());
        unidadEditando.setLongitud(posicionSeleccionada.getLongitude());

        try {
            unidadService.actualizar(unidadEditando);
            mostrarExito("Unidad actualizada", "Los cambios fueron guardados correctamente.");
            salirModoEdicion();
        } catch (SQLException e) {
            alerta("Error al actualizar: " + e.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // ELIMINAR
    // ════════════════════════════════════════════════════════════════════════
    private void eliminarUnidad() {
        if (unidadEditando == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar eliminación");
        confirm.setHeaderText("¿Eliminar la unidad?");
        confirm.setContentText("Vas a eliminar: \"" + unidadEditando.getNombre()
                + "\"\nEsta acción no se puede deshacer.");

        ButtonType btnSi = new ButtonType("Sí, eliminar", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnNo = new ButtonType("Cancelar",     ButtonBar.ButtonData.CANCEL_CLOSE);
        confirm.getButtonTypes().setAll(btnSi, btnNo);

        confirm.showAndWait().ifPresent(resp -> {
            if (resp == btnSi) {
                try {
                    unidadService.eliminar(unidadEditando.getNombre());
                    mostrarExito("🗑  Unidad eliminada", "La unidad fue eliminada correctamente.");
                    salirModoEdicion();
                } catch (SQLException e) {
                    alerta("Error al eliminar: " + e.getMessage());
                }
            }
        });
    }

    // ════════════════════════════════════════════════════════════════════════
    // SALIR MODO EDICIÓN
    // ════════════════════════════════════════════════════════════════════════
    private void salirModoEdicion() {
        unidadEditando = null;
        lblTituloPanel.setText("Registrar unidad");
        lblSubtituloPanel.setText("Completa los datos y ubica en el mapa");
        btnAccionPrincipal.setText("Guardar unidad");
        btnEliminar.setVisible(false);
        btnEliminar.setManaged(false);
        txtNombre.clear();
        cmbComuna.setValue(null);
        cmbBarrio.getItems().clear();
        cmbBarrio.setValue(null);
        cmbBarrio.setDisable(true);
        cmbEstado.setValue(null);
        posicionSeleccionada = null;
        lblCoordenadas.setText("—,  —");
        lblCoordFooter.setText("📍  —,  —");
        lblInstruccionHeader.setText("Haz clic en el mapa para ubicar la unidad policial");
        lblInstruccionHeader.setTextFill(Color.web("#6b7280"));
        if (mapa != null) SwingUtilities.invokeLater(mapa::repaint);
    }

    // ════════════════════════════════════════════════════════════════════════
    // VALIDACIÓN
    // ════════════════════════════════════════════════════════════════════════
    private boolean validar() {
        if (txtNombre.getText().trim().isEmpty()) { alerta("Ingrese el nombre.");            return false; }
        if (cmbComuna.getValue() == null)          { alerta("Seleccione una comuna.");        return false; }
        if (cmbBarrio.getValue() == null)          { alerta("Seleccione un barrio.");         return false; }
        if (cmbEstado.getValue() == null)          { alerta("Seleccione el estado.");         return false; }
        if (posicionSeleccionada == null)           { alerta("Seleccione ubicación en mapa."); return false; }
        return true;
    }

    // ════════════════════════════════════════════════════════════════════════
    // DATOS
    // ════════════════════════════════════════════════════════════════════════
    private void cargarComunas() {
        if (comunaService == null) return;
        try {
            List<Comuna> comunas = comunaService.listar();
            ObservableList<String> nombres = FXCollections.observableArrayList();
            for (Comuna c : comunas) nombres.add(c.getNombre());
            cmbComuna.setItems(nombres);
        } catch (Exception e) {
            System.out.println("Error cargando comunas: " + e.getMessage());
        }
    }

    private void filtrarBarrios() {
        String comunaSel = cmbComuna.getValue();
        cmbBarrio.getItems().clear();
        cmbBarrio.setValue(null);
        cmbBarrio.setDisable(true);
        if (comunaSel == null) return;
        try {
            ObservableList<Barrio> filtrados = FXCollections.observableArrayList();
            for (Barrio b : barrioService.listar()) {
                if (b.getComuna() != null && comunaSel.equals(b.getComuna().getNombre()))
                    filtrados.add(b);
            }
            cmbBarrio.setItems(filtrados);
            cmbBarrio.setDisable(filtrados.isEmpty());
        } catch (Exception e) {
            System.out.println("Error filtrando barrios: " + e.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // HELPERS UI
    // ════════════════════════════════════════════════════════════════════════
    private VBox campo(String etiqueta, javafx.scene.Node control) {
        Label lbl = new Label(etiqueta);
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        lbl.setTextFill(Color.web(C_LABEL));
        return new VBox(6, lbl, control);
    }

    private Button buildBotonPrimario(String texto) {
        Button btn = new Button(texto);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPrefHeight(44);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        String normal = "-fx-background-color:" + C_DARK_GRAD
                + ";-fx-text-fill:white;-fx-cursor:hand;-fx-background-radius:8;";
        String hover  = "-fx-background-color:#1f3a56"
                + ";-fx-text-fill:white;-fx-cursor:hand;-fx-background-radius:8;";
        btn.setStyle(normal);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e  -> btn.setStyle(normal));
        return btn;
    }

    private Button buildBotonSecundario(String texto) {
        Button btn = new Button(texto);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPrefHeight(38);
        btn.setFont(Font.font("Arial", 12));
        btn.setStyle("-fx-background-color:white;-fx-text-fill:#6b7280;"
                + "-fx-border-color:#d1d5db;-fx-border-width:1;"
                + "-fx-cursor:hand;-fx-background-radius:8;-fx-border-radius:8;");
        return btn;
    }

    private void estilizarInput(TextField tf) {
        tf.setStyle("-fx-background-color:" + C_INPUT_BG + ";"
                + "-fx-text-fill:" + C_INPUT_TEXT + ";"
                + "-fx-prompt-text-fill:#9ca3af;"
                + "-fx-border-color:transparent;"
                + "-fx-border-radius:30;-fx-background-radius:30;"
                + "-fx-padding:14 20 14 20;-fx-font-size:14px;");
        tf.setPrefHeight(48);
    }

    private void estilizarComboString(ComboBox<String> cb) {
        cb.setStyle("-fx-background-color:" + C_INPUT_BG + ";"
                + "-fx-text-fill:" + C_INPUT_TEXT + ";"
                + "-fx-prompt-text-fill:#9ca3af;"
                + "-fx-border-color:transparent;"
                + "-fx-border-radius:30;-fx-background-radius:30;-fx-font-size:14px;");
        cb.setPrefHeight(48);
        cb.setCellFactory(lv -> new ListCell<String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
                setStyle("-fx-background-color:transparent;-fx-text-fill:#4b5563;"
                        + "-fx-font-size:14px;-fx-padding:8 14 8 14;");
                setOnMouseEntered(e -> setStyle("-fx-background-color:" + C_DARK_GRAD
                        + ";-fx-background-radius:6;-fx-text-fill:white;"
                        + "-fx-font-size:14px;-fx-padding:8 14 8 14;"));
                setOnMouseExited(e  -> setStyle(
                        "-fx-background-color:transparent;-fx-text-fill:#4b5563;"
                        + "-fx-font-size:14px;-fx-padding:8 14 8 14;"));
            }
        });
        cb.setButtonCell(new ListCell<String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? cb.getPromptText() : item);
                setStyle(item != null
                        ? "-fx-background-color:" + C_INPUT_BG + ";-fx-text-fill:black;-fx-font-size:14px;"
                        : "-fx-background-color:transparent;-fx-text-fill:#9ca3af;-fx-font-size:14px;");
            }
        });
    }

    private void estilizarComboEnum(ComboBox<EstadoUnidadPolicial> cb) {
        cb.setStyle("-fx-background-color:" + C_INPUT_BG + ";"
                + "-fx-text-fill:" + C_INPUT_TEXT + ";"
                + "-fx-prompt-text-fill:#9ca3af;"
                + "-fx-border-color:transparent;"
                + "-fx-border-radius:30;-fx-background-radius:30;-fx-font-size:14px;");
        cb.setPrefHeight(48);
        cb.setCellFactory(lv -> new ListCell<EstadoUnidadPolicial>() {
            @Override protected void updateItem(EstadoUnidadPolicial item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                setText(labelEstado(item));
                setStyle("-fx-background-color:transparent;-fx-text-fill:#4b5563;"
                        + "-fx-font-size:14px;-fx-padding:8 14 8 14;");
                setOnMouseEntered(e -> setStyle("-fx-background-color:" + C_DARK_GRAD
                        + ";-fx-background-radius:6;-fx-text-fill:white;"
                        + "-fx-font-size:14px;-fx-padding:8 14 8 14;"));
                setOnMouseExited(e  -> setStyle(
                        "-fx-background-color:transparent;-fx-text-fill:#4b5563;"
                        + "-fx-font-size:14px;-fx-padding:8 14 8 14;"));
            }
        });
        cb.setButtonCell(new ListCell<EstadoUnidadPolicial>() {
            @Override protected void updateItem(EstadoUnidadPolicial item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? cb.getPromptText() : labelEstado(item));
                setStyle(item != null
                        ? "-fx-background-color:" + C_INPUT_BG + ";-fx-text-fill:black;-fx-font-size:14px;"
                        : "-fx-background-color:transparent;-fx-text-fill:#9ca3af;-fx-font-size:14px;");
            }
        });
    }

    private void estilizarComboBarrio(ComboBox<Barrio> cb) {
        cb.setStyle("-fx-background-color:" + C_INPUT_BG + ";"
                + "-fx-text-fill:" + C_INPUT_TEXT + ";"
                + "-fx-prompt-text-fill:#9ca3af;"
                + "-fx-border-color:transparent;"
                + "-fx-border-radius:30;-fx-background-radius:30;-fx-font-size:14px;");
        cb.setPrefHeight(48);
        cb.setCellFactory(lv -> new ListCell<Barrio>() {
            @Override protected void updateItem(Barrio item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNombre());
                setStyle("-fx-background-color:transparent;-fx-text-fill:#4b5563;"
                        + "-fx-font-size:14px;-fx-padding:8 14 8 14;");
                setOnMouseEntered(e -> setStyle("-fx-background-color:" + C_DARK_GRAD
                        + ";-fx-background-radius:6;-fx-text-fill:white;"
                        + "-fx-font-size:14px;-fx-padding:8 14 8 14;"));
                setOnMouseExited(e  -> setStyle(
                        "-fx-background-color:transparent;-fx-text-fill:#4b5563;"
                        + "-fx-font-size:14px;-fx-padding:8 14 8 14;"));
            }
        });
        cb.setButtonCell(new ListCell<Barrio>() {
            @Override protected void updateItem(Barrio item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? cb.getPromptText() : item.getNombre());
                setStyle(item != null
                        ? "-fx-background-color:" + C_INPUT_BG + ";-fx-text-fill:black;-fx-font-size:14px;"
                        : "-fx-background-color:transparent;-fx-text-fill:#9ca3af;-fx-font-size:14px;");
            }
        });
    }

    private String labelEstado(EstadoUnidadPolicial est) {
        switch (est) {
            case ACTIVA:    return "ACTIVA";
            case INACTIVA:  return "INACTIVA";
            case OPERATIVA: return "OPERATIVA";
            default:        return est.name();
        }
    }

    private void alerta(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle("Validación"); a.setHeaderText(null);
        a.setContentText(msg); a.showAndWait();
    }

    private void info(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("WolertApp"); a.setHeaderText(null);
        a.setContentText(msg); a.showAndWait();
    }

    private void mostrarExito(String titulo, String mensaje) {
        Stage ventana = new Stage();
        ventana.initModality(Modality.APPLICATION_MODAL);
        ventana.setTitle("WolertApp");
        ventana.setResizable(false);

        Label lblIcono  = new Label(titulo.contains("eliminada") ? "🗑" : "✅");
        lblIcono.setFont(Font.font(32));
        Label lblTitulo = new Label(titulo);
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        lblTitulo.setTextFill(Color.web("#111827"));
        Label lblMsg = new Label(mensaje);
        lblMsg.setFont(Font.font("Arial", 13));
        lblMsg.setTextFill(Color.web("#6b7280"));
        lblMsg.setWrapText(true);

        Button btnOk = buildBotonPrimario("Aceptar");
        btnOk.setPrefWidth(160); btnOk.setMaxWidth(160);
        btnOk.setOnAction(e -> ventana.close());

        VBox contenido = new VBox(12, lblIcono, lblTitulo, lblMsg, btnOk);
        contenido.setAlignment(Pos.CENTER);
        contenido.setPadding(new Insets(30, 40, 30, 40));
        contenido.setStyle("-fx-background-color:white;");

        ventana.setScene(new Scene(contenido, 360, 220));
        ventana.showAndWait();
    }

    // ════════════════════════════════════════════════════════════════════════
    // UTILIDAD AWT
    // ════════════════════════════════════════════════════════════════════════
    private java.awt.image.BufferedImage recortarTransparencia(java.awt.image.BufferedImage img) {
        int w = img.getWidth(), h = img.getHeight();
        int top = h, bottom = 0, left = w, right = 0;
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++)
                if (((img.getRGB(x, y) >> 24) & 0xff) > 10) {
                    if (y < top)    top    = y;
                    if (y > bottom) bottom = y;
                    if (x < left)   left   = x;
                    if (x > right)  right  = x;
                }
        if (top >= bottom || left >= right) return img;
        return img.getSubimage(left, top, right - left + 1, bottom - top + 1);
    }
}