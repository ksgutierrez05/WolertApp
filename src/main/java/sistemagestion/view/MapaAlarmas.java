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
import sistemagestion.model.Barrio;
import sistemagestion.model.Comuna;
import sistemagestion.model.EstadoAlarma;
import sistemagestion.service.AlarmaService;
import sistemagestion.service.BarrioService;
import sistemagestion.service.ComunaService;

import javax.swing.SwingUtilities;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.sql.SQLException;
import java.util.List;

public class MapaAlarmas {

    // ── Paleta ────────────────────────────────────────────────────────────────
    private static final String C_INPUT_BG   = "#f7f7f7";
    private static final String C_INPUT_TEXT = "#4b5563";
    private static final String C_LABEL      = "#374151";
    private static final String C_COORD_BG   = "#f0f4ff";
    private static final String C_COORD_BDR  = "#c7d7fd";
    private static final String C_COORD_VAL  = "#1e3a8a";
    private static final String C_SEPARATOR  = "#e5e7eb";
    private static final String C_DARK_GRAD  = "linear-gradient(to right, #16283d, #1f3a56)";

    // ── Servicios ─────────────────────────────────────────────────────────────
    private AlarmaService alarmaService;
    private ComunaService comunaService;
    private BarrioService barrioService;

    // ── Estado del mapa ─────────────────────────────────────────────────────── a
    private JXMapViewer mapa;
    private GeoPosition posicionSeleccionada;

    // ── Controles ─────────────────────────────────────────────────────────────
    private TextField           txtNombre;
    private ComboBox<String>    cmbComuna;
    private ComboBox<String>    cmbBarrio;
    private Slider              sliderRadio;
    private TextField           txtRadioManual;
    private ComboBox<EstadoAlarma> cmbEstado;   // ← ahora tipado con el enum
    private Label               lblCoordenadas;
    private Label               lblCoordFooter;
    private Label               lblInstruccionHeader;

    // ── Constructor ───────────────────────────────────────────────────────────
    public MapaAlarmas() {
        try { alarmaService = new AlarmaService(); }
        catch (SQLException e) { System.out.println("Error AlarmaService: " + e.getMessage()); }
        try { comunaService = new ComunaService(); }
        catch (SQLException e) { System.out.println("Error ComunaService: " + e.getMessage()); }
        try { barrioService = new BarrioService(); }
        catch (SQLException e) { System.out.println("Error BarrioService: " + e.getMessage()); }
    }

    // ─────────────────────────────────────────────────────────────────────────
    public void mostrar() {
        Stage stage = new Stage();
        stage.setTitle("WolertApp — Registrar Alarma");

        BorderPane root = new BorderPane();
        root.setTop(buildTopBar());
        root.setCenter(buildCentro());
        root.setStyle("-fx-background-color: white;");

        stage.setScene(new Scene(root, 1100, 680));
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

        lblInstruccionHeader = new Label("Haz clic en el mapa para ubicar la alarma");
        lblInstruccionHeader.setFont(Font.font("Arial", 13));
        lblInstruccionHeader.setTextFill(Color.web("#6b7280"));

        HBox bar = new HBox(10, logoBox, logoText, sep, new Label("📍"), lblInstruccionHeader);
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
                posicionSeleccionada.getLatitude(),
                posicionSeleccionada.getLongitude());
        Platform.runLater(() -> {
            lblCoordenadas.setText(coord);
            lblCoordFooter.setText("📍  " + coord);
            lblInstruccionHeader.setText("Ubicación seleccionada");
        });
    }

    private void pintarOverlay(Graphics2D g, JXMapViewer map, int w, int h) {
        if (posicionSeleccionada == null) return;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Point2D pt = map.getTileFactory().geoToPixel(posicionSeleccionada, map.getZoom());
        int cx = (int) pt.getX() - map.getViewportBounds().x;
        int cy = (int) pt.getY() - map.getViewportBounds().y;

        int radioM = (int) sliderRadio.getValue();
        if (radioM > 0) {
            double mpp = 156543.03392
                    * Math.cos(Math.toRadians(posicionSeleccionada.getLatitude()))
                    / Math.pow(2, 17 - map.getZoom());
            int rp = (int) (radioM / mpp);
            g.setColor(new java.awt.Color(255, 255, 255, 60));
            g.fill(new Ellipse2D.Double(cx - rp, cy - rp, rp * 2, rp * 2));
            g.setColor(new java.awt.Color(59, 130, 246, 200));
            g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND, 0, new float[]{7, 5}, 0));
            g.draw(new Ellipse2D.Double(cx - rp, cy - rp, rp * 2, rp * 2));
        }

        try {
            java.io.InputStream is = getClass().getResourceAsStream("/SirenaPin.png");
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
        Label titulo = new Label("Registrar alarma");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        titulo.setTextFill(Color.web("#111827"));

        Label sub = new Label("Completa los datos y ubica en el mapa");
        sub.setFont(Font.font("Arial", 11));
        sub.setTextFill(Color.web("#6b7280"));

        Label icono = new Label("🔔");
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

    private ScrollPane buildScroll() {
        VBox form = buildFormulario();
        form.setPadding(new Insets(18, 20, 18, 20));
        form.setStyle("-fx-background-color:white;");

        ScrollPane scroll = new ScrollPane(form);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:white;-fx-background:white;");
        return scroll;
    }

    private VBox buildFormulario() {
        txtNombre = new TextField();
        txtNombre.setPromptText("Ej: Alarma Sector Norte");
        estilizarInput(txtNombre);

        cmbComuna = new ComboBox<>();
        cmbComuna.setPromptText("Seleccione una comuna");
        cmbComuna.setMaxWidth(Double.MAX_VALUE);
        estilizarCombo(cmbComuna);
        cargarComunas();

        cmbBarrio = new ComboBox<>();
        cmbBarrio.setPromptText("Seleccione un barrio");
        cmbBarrio.setMaxWidth(Double.MAX_VALUE);
        cmbBarrio.setDisable(true);
        estilizarCombo(cmbBarrio);

        cmbComuna.setOnAction(e -> filtrarBarrios());

        sliderRadio = new Slider(50, 2000, 500);
        sliderRadio.setBlockIncrement(50);
        sliderRadio.setStyle(
                "-fx-accent:#16283d;-fx-color:#16283d;"
                + "-fx-control-inner-background:#dde3ea;");
        sliderRadio.skinProperty().addListener((obs, ov, nv) -> estilizarSlider());

        txtRadioManual = new TextField("500");
        txtRadioManual.setPrefWidth(72);
        txtRadioManual.setPrefHeight(28);
        txtRadioManual.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        txtRadioManual.setStyle(
                "-fx-background-color:#f1f5f9;-fx-text-fill:#374151;"
                + "-fx-border-color:#e2e8f0;-fx-border-width:1;"
                + "-fx-border-radius:6;-fx-background-radius:6;"
                + "-fx-padding:4 10 4 10;-fx-alignment:center;");

        sliderRadio.valueProperty().addListener((obs, ov, nv) -> {
            txtRadioManual.setText(String.valueOf(nv.intValue()));
            if (mapa != null) SwingUtilities.invokeLater(mapa::repaint);
        });

        javafx.event.EventHandler<javafx.event.ActionEvent> aplicarManual = e -> {
            try {
                int v = Math.max(50, Math.min(2000,
                        Integer.parseInt(txtRadioManual.getText().trim())));
                sliderRadio.setValue(v);
            } catch (NumberFormatException ignored) {
                txtRadioManual.setText(String.valueOf((int) sliderRadio.getValue()));
            }
        };
        txtRadioManual.setOnAction(aplicarManual);
        txtRadioManual.focusedProperty().addListener((obs, ov, focused) -> {
            if (!focused) aplicarManual.handle(null);
        });

        Label lblM = new Label("m");
        lblM.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        lblM.setTextFill(Color.web("#374151"));

        HBox radioRow = new HBox(10, sliderRadio, txtRadioManual, lblM);
        radioRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(sliderRadio, Priority.ALWAYS);

        // ── Estado: ComboBox<EstadoAlarma> con textos bonitos ─────────────────
        cmbEstado = new ComboBox<>();
        cmbEstado.setPromptText("Seleccione estado");
        cmbEstado.setMaxWidth(Double.MAX_VALUE);
        cmbEstado.setItems(FXCollections.observableArrayList(EstadoAlarma.values()));
        estilizarComboEnum(cmbEstado);

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
        coordBox.setStyle(
                "-fx-background-color:" + C_COORD_BG + ";"
                + "-fx-border-color:" + C_COORD_BDR + ";"
                + "-fx-border-width:1;-fx-border-radius:10;-fx-background-radius:10;");

        Button btnGuardar = buildBotonPrimario("Guardar alarma");
        btnGuardar.setOnAction(e -> guardarAlarma());

        Button btnLimpiar = buildBotonSecundario("Limpiar");
        btnLimpiar.setOnAction(e -> limpiarFormulario());

        return new VBox(14,
                campo("Nombre de la alarma",    txtNombre),
                campo("Comuna",                 cmbComuna),
                campo("Barrio",                 cmbBarrio),
                campo("Radio de cobertura (m)", radioRow),
                campo("Estado",                 cmbEstado),
                coordBox, btnGuardar, btnLimpiar
        );
    }

    // ════════════════════════════════════════════════════════════════════════
    // LÓGICA DE NEGOCIO
    // ════════════════════════════════════════════════════════════════════════
    private void cargarComunas() {
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
        String comunaSeleccionada = cmbComuna.getValue();
        cmbBarrio.getItems().clear();
        cmbBarrio.setValue(null);
        cmbBarrio.setDisable(true);
        if (comunaSeleccionada == null) return;
        try {
            List<Barrio> barrios = barrioService.listar();
            ObservableList<String> nombres = FXCollections.observableArrayList();
            for (Barrio b : barrios) {
                if (b.getComuna() != null
                        && comunaSeleccionada.equals(b.getComuna().getNombre())) {
                    nombres.add(b.getNombre());
                }
            }
            cmbBarrio.setItems(nombres);
            cmbBarrio.setDisable(nombres.isEmpty());
        } catch (Exception e) {
            System.out.println("Error filtrando barrios: " + e.getMessage());
        }
    }

    private void guardarAlarma() {
        if (txtNombre.getText().trim().isEmpty()) { alerta("Ingrese el nombre.");               return; }
        if (cmbBarrio.getValue() == null)          { alerta("Seleccione un barrio.");            return; }
        if (posicionSeleccionada == null)           { alerta("Seleccione ubicación en el mapa."); return; }
        if (cmbEstado.getValue() == null)           { alerta("Seleccione el estado.");            return; }

        Alarma a = new Alarma();
        a.setNombre(txtNombre.getText().trim());
        Barrio b = new Barrio();
        b.setNombre(cmbBarrio.getValue());
        a.setBarrio(b);
        a.setLatitud(posicionSeleccionada.getLatitude());
        a.setLongitud(posicionSeleccionada.getLongitude());
        a.setRadio_cobertura(sliderRadio.getValue());
        a.setEstado(cmbEstado.getValue());   // ← directo, sin valueOf

        boolean ok = alarmaService.insertar(a);
        if (ok) { info("Alarma registrada correctamente."); limpiarFormulario(); }
        else    { alerta("Error al guardar — verifica los datos."); }
    }

    private void limpiarFormulario() {
        txtNombre.clear();
        cmbComuna.setValue(null);
        cmbBarrio.getItems().clear();
        cmbBarrio.setValue(null);
        cmbBarrio.setDisable(true);
        sliderRadio.setValue(500);
        txtRadioManual.setText("500");
        cmbEstado.setValue(null);
        posicionSeleccionada = null;
        lblCoordenadas.setText("—,  —");
        lblCoordFooter.setText("📍  —,  —");
        lblInstruccionHeader.setText("Haz clic en el mapa para ubicar la alarma");
        if (mapa != null) SwingUtilities.invokeLater(mapa::repaint);
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
            @Override protected void updateItem(String item, boolean empty) {
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
            @Override protected void updateItem(String item, boolean empty) {
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

    // ── Combo especial para el enum EstadoAlarma con textos legibles ──────────
    private void estilizarComboEnum(ComboBox<EstadoAlarma> cb) {
        cb.setStyle(
                "-fx-background-color:" + C_INPUT_BG + ";"
                + "-fx-text-fill:" + C_INPUT_TEXT + ";"
                + "-fx-prompt-text-fill:#9ca3af;"
                + "-fx-border-color:transparent;"
                + "-fx-border-radius:30;-fx-background-radius:30;-fx-font-size:14px;");
        cb.setPrefHeight(48);

        cb.setCellFactory(lv -> new ListCell<EstadoAlarma>() {
            @Override protected void updateItem(EstadoAlarma item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                setText(labelEstado(item));
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

        cb.setButtonCell(new ListCell<EstadoAlarma>() {
            @Override protected void updateItem(EstadoAlarma item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null) {
                    setText(labelEstado(item));
                    setStyle("-fx-background-color:" + C_INPUT_BG + ";"
                            + "-fx-background-radius:30;-fx-text-fill:black;"
                            + "-fx-font-size:14px;-fx-padding:4 14 4 14;");
                } else {
                    setText("Seleccione estado");
                    setStyle("-fx-background-color:transparent;"
                            + "-fx-text-fill:#9ca3af;-fx-font-size:14px;");
                }
            }
        });
    }

    // ── Centraliza el texto legible de cada estado ────────────────────────────
    private String labelEstado(EstadoAlarma est) {
        switch (est) {
            case ACTIVA:           return "Activa";
            case INACTIVA:         return "Inactiva";
            case EN_MANTENIMIENTO: return "En mantenimiento";
            default:               return est.name();
        }
    }

    private void estilizarSlider() {
        javafx.scene.layout.StackPane track =
                (javafx.scene.layout.StackPane) sliderRadio.lookup(".track");
        if (track != null) {
            track.setMaxHeight(3); track.setPrefHeight(3);
            track.setStyle("-fx-background-color:" + C_DARK_GRAD + ";-fx-background-radius:3;");
        }
        javafx.scene.layout.StackPane thumb =
                (javafx.scene.layout.StackPane) sliderRadio.lookup(".thumb");
        if (thumb != null) {
            thumb.setStyle("-fx-background-color:" + C_DARK_GRAD
                    + ";-fx-background-radius:50%;"
                    + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.25),4,0,0,1);");
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

    // ════════════════════════════════════════════════════════════════════════
    // UTILIDAD AWT
    // ════════════════════════════════════════════════════════════════════════
    private java.awt.image.BufferedImage recortarTransparencia(java.awt.image.BufferedImage img) {
        int w = img.getWidth(), h = img.getHeight();
        int top = h, bottom = 0, left = w, right = 0;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (((img.getRGB(x, y) >> 24) & 0xff) > 10) {
                    if (y < top)    top    = y;
                    if (y > bottom) bottom = y;
                    if (x < left)   left   = x;
                    if (x > right)  right  = x;
                }
            }
        }
        if (top >= bottom || left >= right) return img;
        return img.getSubimage(left, top, right - left + 1, bottom - top + 1);
    }
}