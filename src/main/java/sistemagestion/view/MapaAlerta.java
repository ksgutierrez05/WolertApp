package sistemagestion.view;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.*;
import javafx.stage.*;
import javafx.util.Duration;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCenter;
import org.jxmapviewer.viewer.*;
import sistemagestion.model.*;
import sistemagestion.service.*;

import javax.swing.SwingUtilities;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import java.sql.SQLException;
import java.util.*;
import javafx.embed.swing.SwingNode;
import sistemagestion.model.Notificacion;
import sistemagestion.model.Suscripcion;
import sistemagestion.service.NotificacionService;
import sistemagestion.service.SuscripcionService;

public class MapaAlerta {

    // ── Paleta ─────────────────────────────────────────────────────────────
    private static final String BG          = "white";
    private static final String BORDER      = "#e5e7eb";
    private static final String DARK_GRAD   = "linear-gradient(to bottom right,#16283d,#1f3a56)";
    private static final String RED         = "#e53935";
    private static final String RED_DARK    = "#c62828";
    private static final String FIELD_BG    = "#f8fafc";
    private static final String FIELD_BDR   = "#e2e8f0";
    private static final String FIELD_FOCUS = "#93c5fd";
    private static final String TEXT_MAIN   = "#111827";
    private static final String TEXT_SUB    = "#6b7280";
    private static final String TEXT_FIELD  = "#374151";
    private static final String BLUE_LIGHT  = "#eff6ff";
    private static final String BLUE_BDR    = "#bfdbfe";
    private static final String GREEN       = "#16a34a";
    private static final String DARK        = "#16283d";

    // ── Tipos hardcodeados (no se duplican desde la BD) ───────────────────
    private static final Set<String> TIPOS_FIJOS = Set.of(
            "ROBO", "HOMICIDIO", "PERSONA_SOSPECHOSA",
            "INCENDIO", "RUIDO", "EMERGENCIA_MEDICA", "ACCIDENTE",
            // nombres tal como vienen de la BD para los básicos:
            "Robo / Asalto", "Homicidio", "Persona Sospechosa",
            "Sospechoso", "Incendio", "Ruido / Alteración",
            "Emergencia médica", "Emergencia Medica", "Accidente"
    );

    private static final Set<String> ARMAS_FIJAS = Set.of(
            "Arma de fuego", "Arma blanca", "Sin arma", "Explosivo",
            "Agresión física", "Agresion fisica", "Desconocido"
    );

    private static final Set<String> MEDIOS_FIJOS = Set.of(
            "Motocicleta", "Automóvil", "Automovil", "A pie", "Bus/Colectivo"
    );

    // ── Dependencias ────────────────────────────────────────────────────────
    private final Stage              owner;
    private final Usuario            usuario;
    private final AlertaService      alertaService;
    private final BarrioService      barrioService;
    private final TipoAlertaService  tipoAlertaService;
    private final TipoArmaService    tipoArmaService;
    private final MedioTransporteService medioService;

    // ── Mapa ────────────────────────────────────────────────────────────────
    private JXMapViewer        mapa;
    private GeoPosition        posicionSeleccionada;
    private Label              lblCoordFooter;
    private Label              lblInstruccion;
    private SimpleStringProperty coordProp;

    // ── Estado formulario ───────────────────────────────────────────────────
    private String tipoAlertaSel  = null;
    private String tipoArmaSel    = null;
    private String medioTranspSel = null;
    private HBox   tarjetaActiva  = null;

    // ── Subpaneles fijos ────────────────────────────────────────────────────
    private VBox subRobo, subHomicidio, subSospechoso,
                 subIncendio, subRuido, subMedica, subAccidente;
    private List<VBox> todosSubpaneles;

    // ── Controles formulario ────────────────────────────────────────────────
    private TextArea       txtDescripcion;
    private ComboBox<Barrio> cmbBarrio;
    private Label          lblFeedback;

    // ── Grid dinámico ───────────────────────────────────────────────────────
    private GridPane gridAlertas;
    private int      gridCol = 0;
    private int      gridRow = 0;

    // ── Subpanel de armas/medios extra ──────────────────────────────────────
    private VBox subExtra;           // subpanel genérico para tipo extra
    private VBox secArmaExtra;       // sección con armas extra dentro de subRobo
    private VBox secMedioExtra;      // sección con medios extra dentro de subRobo

    // ── Animación pánico ─────────────────────────────────────────────────────
    private List<ScaleTransition> scaleAnims = new ArrayList<>();
    private List<FadeTransition>  fadeAnims  = new ArrayList<>();
    private List<Circle>          waves      = new ArrayList<>();
    private boolean panicActive = false;

    // ─────────────────────────────────────────────────────────────────────────
    public MapaAlerta(Stage owner, Usuario usuario,
                      AlertaService alertaService, BarrioService barrioService) {
        this.owner        = owner;
        this.usuario      = usuario;
        this.alertaService = alertaService;
        this.barrioService = barrioService;
        this.tipoAlertaService = crearTipoAlertaService();
        this.tipoArmaService   = crearTipoArmaService();
        this.medioService      = crearMedioService();
    }

    public MapaAlerta() {
        this.owner        = null;
        this.usuario      = null;
        AlertaService as  = null;
        BarrioService bs  = null;
        try { as = new AlertaService(); }  catch (SQLException ignored) {}
        try { bs = new BarrioService(); }  catch (SQLException ignored) {}
        this.alertaService     = as;
        this.barrioService     = bs;
        this.tipoAlertaService = crearTipoAlertaService();
        this.tipoArmaService   = crearTipoArmaService();
        this.medioService      = crearMedioService();
    }

    private TipoAlertaService crearTipoAlertaService() {
        try { return new TipoAlertaService(); } catch (Exception e) { return null; }
    }
    private TipoArmaService crearTipoArmaService() {
        try { return new TipoArmaService(); } catch (Exception e) { return null; }
    }
    private MedioTransporteService crearMedioService() {
        try { return new MedioTransporteService(); } catch (Exception e) { return null; }
    }

    // ─────────────────────────────────────────────────────────────────────────
    public void mostrar() {
        Font.loadFont(getClass().getResourceAsStream("/fa-solid-900.ttf"), 20);
        Stage stage = new Stage();
        if (owner != null) {
            stage.initOwner(owner);
            stage.initModality(Modality.APPLICATION_MODAL);
        }
        stage.initStyle(StageStyle.DECORATED);
        stage.setTitle("WolertApp — Reportar Emergencia");
        coordProp = new SimpleStringProperty("—");

        BorderPane root = new BorderPane();
        root.setTop(buildTopBar());
        root.setStyle("-fx-background-color:" + BG + ";");

        StackPane mapaPane = buildMapaPane();
        VBox      formPane = buildFormPane();
        HBox.setHgrow(mapaPane, Priority.ALWAYS);

        HBox body = new HBox(mapaPane, formPane);
        root.setCenter(body);

        if (owner != null && owner.getScene() != null) {
            owner.getScene().getRoot().setEffect(new BoxBlur(8, 8, 3));
            stage.setOnHidden(e -> owner.getScene().getRoot().setEffect(null));
        }

        Scene scene = new Scene(root, 1220, 740);
        stage.setMinWidth(920);
        stage.setMinHeight(620);
        String css = ".text-area{-fx-background-color:white;-fx-background-insets:0;}"
                + ".text-area .scroll-pane{-fx-background-color:white;-fx-background-insets:0;}"
                + ".text-area .scroll-pane .viewport{-fx-background-color:white;}"
                + ".text-area .scroll-pane .content{-fx-background-color:white;-fx-background-insets:0;}";
        scene.getStylesheets().add(
                "data:text/css," + java.net.URLEncoder.encode(css, java.nio.charset.StandardCharsets.UTF_8)
                        .replace("+", "%20"));
        stage.setScene(scene);
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
        logoImg.setFitHeight(22); logoImg.setFitWidth(22); logoImg.setPreserveRatio(true);

        StackPane logoBox = new StackPane(logoImg);
        logoBox.setStyle("-fx-background-color:" + DARK_GRAD + ";-fx-background-radius:8;-fx-padding:6;");

        Label logoTxt = new Label("WolertApp");
        logoTxt.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        logoTxt.setTextFill(Color.web(TEXT_MAIN));

        Region div = new Region();
        div.setStyle("-fx-background-color:" + BORDER + ";");
        div.setPrefSize(1, 22);

        lblInstruccion = new Label("📍  Haz clic en el mapa para marcar la ubicación del incidente");
        lblInstruccion.setFont(Font.font("Arial", 13));
        lblInstruccion.setTextFill(Color.web(TEXT_SUB));

        HBox bar = new HBox(12, logoBox, logoTxt, div, lblInstruccion);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(12, 20, 12, 20));
        bar.setStyle("-fx-background-color:" + BG + ";-fx-border-color:" + BORDER + ";-fx-border-width:0 0 1 0;");
        return bar;
    }

    // ════════════════════════════════════════════════════════════════════════
    // MAPA
    // ════════════════════════════════════════════════════════════════════════
    private StackPane buildMapaPane() {
        SwingNode swingNode = new SwingNode();
        SwingUtilities.invokeLater(() -> inicializarMapa(swingNode));

        lblCoordFooter = new Label("📍  —,  —");
        lblCoordFooter.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        lblCoordFooter.setTextFill(Color.web(TEXT_FIELD));
        lblCoordFooter.setStyle(
                "-fx-background-color:rgba(255,255,255,0.95);-fx-background-radius:20;"
                + "-fx-padding:7 18 7 18;-fx-border-color:" + BORDER
                + ";-fx-border-radius:20;-fx-border-width:1;"
                + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.12),10,0,0,3);");

        Label chipAviso = new Label("⚠  Sin ubicación — haz clic en el mapa");
        chipAviso.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        chipAviso.setTextFill(Color.web(RED));
        chipAviso.setStyle(
                "-fx-background-color:rgba(255,255,255,0.97);-fx-background-radius:20;"
                + "-fx-padding:7 16 7 16;-fx-border-color:#fca5a5;"
                + "-fx-border-radius:20;-fx-border-width:1;"
                + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.10),8,0,0,2);");

        coordProp.addListener((obs, o, n) -> {
            if (!n.equals("—")) Platform.runLater(() -> {
                chipAviso.setVisible(false);
                chipAviso.setManaged(false);
            });
        });

        StackPane stack = new StackPane(swingNode, lblCoordFooter, chipAviso);
        StackPane.setAlignment(lblCoordFooter, Pos.BOTTOM_CENTER);
        StackPane.setMargin(lblCoordFooter, new Insets(0, 0, 18, 0));
        StackPane.setAlignment(chipAviso, Pos.TOP_LEFT);
        StackPane.setMargin(chipAviso, new Insets(14, 0, 0, 14));
        return stack;
    }

    private void inicializarMapa(SwingNode sn) {
        mapa = new JXMapViewer();
        TileFactoryInfo info = new TileFactoryInfo(1, 15, 17, 256, true, true,
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
        mapa.setOverlayPainter(this::pintarPin);
        sn.setContent(mapa);
    }

    private void onMapaClick(MouseEvent e) {
        posicionSeleccionada = mapa.convertPointToGeoPosition(e.getPoint());
        mapa.repaint();
        String coord = String.format("%.5f,  %.5f",
                posicionSeleccionada.getLatitude(), posicionSeleccionada.getLongitude());
        Platform.runLater(() -> {
            lblCoordFooter.setText("📍  " + coord);
            coordProp.set(coord);
            lblInstruccion.setText("Ubicación marcada — completa el formulario y pulsa el botón de pánico");
            lblInstruccion.setTextFill(Color.web(GREEN));
        });
    }

    private void pintarPin(Graphics2D g, JXMapViewer map, int w, int h) {
        if (posicionSeleccionada == null) return;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Point2D pt = map.getTileFactory().geoToPixel(posicionSeleccionada, map.getZoom());
        int x = (int) pt.getX() - map.getViewportBounds().x;
        int y = (int) pt.getY() - map.getViewportBounds().y;
        java.awt.Color color = getColorAlerta();

        g.setColor(color);
        g.fillOval(x - 12, y - 30, 24, 24);

        int[] xs = {x - 8, x + 8, x};
        int[] ys = {y - 12, y - 12, y + 5};
        g.fillPolygon(xs, ys, 3);


        g.setColor(java.awt.Color.WHITE);
        g.fillOval(x - 5, y - 23, 10, 10);
    }

    // ════════════════════════════════════════════════════════════════════════
    // PANEL FORMULARIO
    // ════════════════════════════════════════════════════════════════════════
    private VBox buildFormPane() {
        HBox header   = buildFormHeader();
        VBox contenido = buildContenido();
        contenido.setPadding(new Insets(20, 22, 24, 22));
        contenido.setStyle("-fx-background-color:" + BG + ";");

        ScrollPane scroll = new ScrollPane(contenido);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:" + BG + ";-fx-background:" + BG + ";");
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        VBox panel = new VBox(header, scroll);
        panel.setPrefWidth(410); panel.setMinWidth(370); panel.setMaxWidth(440);
        panel.setStyle("-fx-background-color:" + BG
                + ";-fx-border-color:" + BORDER + ";-fx-border-width:0 0 0 1;");
        return panel;
    }

    private HBox buildFormHeader() {
        Label ico = new Label("\uf0f3");
        ico.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';-fx-font-size:18px;-fx-text-fill:white;"
                + "-fx-background-color:" + DARK_GRAD + ";-fx-background-radius:10;-fx-padding:7 12 7 12;");
        Label tit = new Label("Reportar Emergencia");
        tit.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        tit.setTextFill(Color.web(TEXT_MAIN));
        Label sub = new Label("Marca el mapa, elige el tipo y pulsa el botón de pánico");
        sub.setFont(Font.font("Arial", 11));
        sub.setTextFill(Color.web(TEXT_SUB));
        HBox hdr = new HBox(14, ico, new VBox(3, tit, sub));
        hdr.setAlignment(Pos.CENTER_LEFT);
        hdr.setPadding(new Insets(16, 22, 16, 22));
        hdr.setStyle("-fx-background-color:" + BG + ";-fx-border-color:" + BORDER + ";-fx-border-width:0 0 1 0;");
        return hdr;
    }

    // ════════════════════════════════════════════════════════════════════════
    // CONTENIDO PRINCIPAL
    // ════════════════════════════════════════════════════════════════════════
    private VBox buildContenido() {
        VBox root = new VBox(18);

        // ── Título + botón pánico ────────────────────────────────────────────
        Label titulo = new Label("¿Qué tipo de emergencia tienes?");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 17));
        titulo.setTextFill(Color.web(TEXT_MAIN));
        titulo.setAlignment(Pos.CENTER);
        titulo.setMaxWidth(Double.MAX_VALUE);

        StackPane panicStack = buildPanicStack();
        VBox panicBlock = new VBox(14, titulo, panicStack);
        panicBlock.setAlignment(Pos.CENTER);
        panicBlock.setPadding(new Insets(4, 0, 6, 0));

        // ── Tarjetas fijas ───────────────────────────────────────────────────
        HBox c_robo        = categoryCard("\uf6de", "#E67E22", "Robo / Asalto");
        HBox c_homicidio   = categoryCard("\uf714", "#1A2332", "Homicidio");
        HBox c_sospechoso  = categoryCard("\uf21b", "#0EA5E9", "Sospechoso");
        HBox c_incendio    = categoryCard("\uf06d", "#E53935", "Incendio");
        HBox c_medica      = categoryCard("\uf0f9", "#1D4ED8", "Emergencia médica");
        HBox c_accidente   = categoryCard("\uf071", "#7C3AED", "Accidente");
        HBox c_ruido       = categoryCard("\uf028", "#16A34A", "Ruido / Alteración");

        // ── Subpaneles fijos ─────────────────────────────────────────────────
        // Cargar armas y medios extras de la BD
        List<Opc> armasExtra  = cargarArmasExtras();
        List<Opc> mediosExtra = cargarMediosExtras();

        VBox secArma = buildSeccion("Tipo de arma", List.of(
                new Opc("\uf05b", "#E53935", "Arma de fuego", "ARMA"),
                new Opc("\ue08f", "#F97316", "Arma blanca",   "ARMA"),
                new Opc("\uf255", "#7C3AED", "Sin arma",      "ARMA"),
                new Opc("\uf1e2", "#1A2332", "Explosivo",     "ARMA")
        ));
        VBox secTransp = buildSeccion("Medio de transporte", List.of(
                new Opc("\uf21c", "#1D4ED8", "Motocicleta",   "TRANSPORTE"),
                new Opc("\uf1b9", "#0EA5E9", "Automóvil",     "TRANSPORTE"),
                new Opc("\uf554", "#16A34A", "A pie",         "TRANSPORTE"),
                new Opc("\uf207", "#D97706", "Bus/Colectivo", "TRANSPORTE")
        ));

        subRobo = containerSubpanel(secArma, dividerLine(), secTransp);

        // Agregar armas extras al subRobo
        if (!armasExtra.isEmpty()) {
            subRobo.getChildren().add(dividerLine());
            subRobo.getChildren().add(buildSeccion("Otros tipos de arma", armasExtra));
        }
        // Agregar medios extras al subRobo
        if (!mediosExtra.isEmpty()) {
            subRobo.getChildren().add(dividerLine());
            subRobo.getChildren().add(buildSeccion("Otros medios de transporte", mediosExtra));
        }

        subHomicidio = buildSubpanelSimple("Tipo de arma", List.of(
                new Opc("\uf05b", "#E53935", "Arma de fuego",  "ARMA"),
                new Opc("\ue08f", "#F97316", "Arma blanca",    "ARMA"),
                new Opc("\uf255", "#7C3AED", "Agresión física","ARMA"),
                new Opc("\uf128", "#475569", "Desconocido",    "ARMA")
        ));

        if (!armasExtra.isEmpty()) {
            subHomicidio.getChildren().add(dividerLine());
            subHomicidio.getChildren().add(buildSeccion("Otros tipos de arma", armasExtra));
        }

        TextArea descSosp = styledTextArea("Ej: camiseta negra, gorra roja...", 3);

        descSosp.textProperty().addListener((obs, o, n) -> {
            if (!n.isBlank() && txtDescripcion != null)
                txtDescripcion.setText("Persona sospechosa: " + n);
        });
        subSospechoso = buildSubpanelConNodo("Descripción del sospechoso", descSosp);

        subIncendio = buildSubpanelSimple("Tipo de incendio", List.of(
                new Opc("\uf015", "#E53935", "Vivienda",        "TIPO"),
                new Opc("\uf1b9", "#F97316", "Vehículo",        "TIPO"),
                new Opc("\uf1bb", "#16A34A", "Zona verde",      "TIPO"),
                new Opc("\uf54f", "#D97706", "Local comercial", "TIPO")
        ));
        subIncendio.getChildren().add(styledToggle("Hay personas atrapadas", activo -> {
            if (activo && txtDescripcion != null) txtDescripcion.appendText(" · Personas atrapadas");
        }));

        subRuido = buildSubpanelSimple("Tipo de alteración", List.of(
                new Opc("\uf001", "#D97706", "Música / fiesta",   "TIPO"),
                new Opc("\uf6de", "#E53935", "Riña / pelea",      "TIPO"),
                new Opc("\uf6d3", "#F97316", "Animal agresivo",   "TIPO"),
                new Opc("\uf0a1", "#7C3AED", "Escándalo público", "TIPO"),
                new Opc("\uf1e2", "#1A2332", "Detonación",        "TIPO"),
                new Opc("\uf7d9", "#0EA5E9", "Obra/construcción", "TIPO")
        ));

        subMedica = buildSubpanelSimple("Tipo de emergencia médica", List.of(
                new Opc("\uf21e", "#E53935", "Paro cardíaco",       "TIPO"),
                new Opc("\uf481", "#0EA5E9", "Dific. respiratoria", "TIPO"),
                new Opc("\uf462", "#F97316", "Persona herida",      "TIPO"),
                new Opc("\uf119", "#7C3AED", "Desmayo/convulsión",  "TIPO"),
                new Opc("\uf77d", "#16A34A", "Parto emergencia",    "TIPO"),
                new Opc("\uf490", "#D97706", "Intoxicación",        "TIPO")
        ));

        subAccidente = buildSubpanelSimple("Tipo de accidente", List.of(
                new Opc("\uf5e4", "#1D4ED8", "Choque vehículos",  "TIPO"),
                new Opc("\uf554", "#E53935", "Atropello",         "TIPO"),
                new Opc("\uf0e7", "#D97706", "Cable eléctrico",   "TIPO"),
                new Opc("\uf773", "#0EA5E9", "Inundación",        "TIPO"),
                new Opc("\uf6ff", "#475569", "Derrumbamiento",    "TIPO"),
                new Opc("\uf018", "#16A34A", "Vía obstruida",     "TIPO")
        ));
        subAccidente.getChildren().add(styledToggle("Hay personas heridas", activo -> {
            if (activo && txtDescripcion != null) txtDescripcion.appendText(" · Personas heridas");
        }));

        todosSubpaneles = new ArrayList<>(List.of(
                subRobo, subHomicidio, subSospechoso,
                subIncendio, subRuido, subMedica, subAccidente));

        // ── Acciones tarjetas fijas ──────────────────────────────────────────
        c_robo.setOnMouseClicked(e       -> activarCategoria(c_robo,       "ROBO",               subRobo,       "#E53935"));
        c_homicidio.setOnMouseClicked(e  -> activarCategoria(c_homicidio,  "HOMICIDIO",          subHomicidio,  "#1E293B"));
        c_sospechoso.setOnMouseClicked(e -> activarCategoria(c_sospechoso, "PERSONA_SOSPECHOSA", subSospechoso, "#0EA5E9"));
        c_incendio.setOnMouseClicked(e   -> activarCategoria(c_incendio,   "INCENDIO",           subIncendio,   "#F97316"));
        c_medica.setOnMouseClicked(e     -> activarCategoria(c_medica,     "EMERGENCIA_MEDICA",  subMedica,     "#1D4ED8"));
        c_accidente.setOnMouseClicked(e  -> activarCategoria(c_accidente,  "ACCIDENTE",          subAccidente,  "#7C3AED"));
        c_ruido.setOnMouseClicked(e      -> activarCategoria(c_ruido,      "RUIDO",              subRuido,      "#D97706"));

        // ── Grid: columnas fijas ─────────────────────────────────────────────
        gridAlertas = new GridPane();
        gridAlertas.setHgap(8);
        gridAlertas.setVgap(8);
        ColumnConstraints cc1 = new ColumnConstraints(); cc1.setPercentWidth(50);
        ColumnConstraints cc2 = new ColumnConstraints(); cc2.setPercentWidth(50);
        gridAlertas.getColumnConstraints().addAll(cc1, cc2);

        // Posición inicial después de los 7 fijos
        gridAlertas.add(c_robo,       0, 0);
        gridAlertas.add(c_homicidio,  1, 0);
        gridAlertas.add(c_sospechoso, 0, 1);
        gridAlertas.add(c_incendio,   1, 1);
        gridAlertas.add(c_medica,     0, 2);
        gridAlertas.add(c_accidente,  1, 2);
        gridAlertas.add(c_ruido,      0, 3);
        gridCol = 1; gridRow = 3;   // siguiente posición libre

        // ── Agregar tipos extras de alerta desde la BD ───────────────────────
        agregarTiposAlertaExtras();

        // ── Separador + descripción + barrio ─────────────────────────────────
        Region sep = new Region();
        sep.setPrefHeight(1);
        sep.setStyle("-fx-background-color:" + BORDER + ";");

        txtDescripcion = styledTextArea("Describe brevemente la emergencia...", 4);
        cmbBarrio      = buildComboBarrio();
        VBox coordChip = buildCoordChip();
        lblFeedback    = new Label();
        lblFeedback.setWrapText(true);
        lblFeedback.setMaxWidth(Double.MAX_VALUE);
        lblFeedback.setFont(Font.font("Arial", 12));

        // Agregar todos los subpaneles extras al root también
        root.getChildren().add(panicBlock);
        root.getChildren().add(gridAlertas);
        root.getChildren().addAll(todosSubpaneles);
        root.getChildren().addAll(sep, buildDescripcionBox(),
                sectionLabel("Barrio del incidente"), cmbBarrio,
                coordChip, lblFeedback);

        return root;
    }

    // ════════════════════════════════════════════════════════════════════════
    // CARGA DINÁMICA DESDE BD
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Agrega al grid los tipos de alerta que vienen de la BD
     * y no están en los fijos hardcodeados.
     */
    private void agregarTiposAlertaExtras() {
        if (tipoAlertaService == null) return;
        try {
            List<TipoAlerta> todos = tipoAlertaService.listar();
            for (TipoAlerta ta : todos) {
                String nombre = ta.getNombre();
                if (nombre == null) continue;
                // Verificar si ya está en los fijos (comparación flexible)
                boolean esFijo = TIPOS_FIJOS.stream()
                        .anyMatch(f -> f.equalsIgnoreCase(nombre)
                                || nombre.toUpperCase().contains(f.toUpperCase()));
                if (esFijo) continue;

                // Crear subpanel genérico para este tipo
                VBox subGenerico = containerSubpanel(
                        buildSubpanelConNodo("Información adicional",
                                styledTextArea("Describe los detalles del incidente...", 3))
                );
                todosSubpaneles.add(subGenerico);

                // Tarjeta genérica
                HBox tarjeta = categoryCard("\uf0a0", "#64748b", nombre);
                final String tipoKey = nombre.toUpperCase().replace(" ", "_");
                tarjeta.setOnMouseClicked(e ->
                        activarCategoria(tarjeta, tipoKey, subGenerico, "#64748b"));

                // Agregar al grid
                gridAlertas.add(tarjeta, gridCol, gridRow);
                gridCol++;
                if (gridCol == 2) { gridCol = 0; gridRow++; }
            }
        } catch (Exception ignored) {}
    }

    /**
     * Retorna lista de Opc con armas que vienen de la BD y no están en las fijas.
     */
    private List<Opc> cargarArmasExtras() {
        if (tipoArmaService == null) return List.of();
        List<Opc> extras = new ArrayList<>();
        try {
            for (TipoArma ta : tipoArmaService.listar()) {
                String nombre = ta.getNombre();
                if (nombre == null) continue;
                boolean esFijo = ARMAS_FIJAS.stream()
                        .anyMatch(f -> f.equalsIgnoreCase(nombre));
                if (!esFijo) {
                    extras.add(new Opc("\uf6ff", "#475569", nombre, "ARMA"));
                }
            }
        } catch (Exception ignored) {}
        return extras;
    }

    /**
     * Retorna lista de Opc con medios que vienen de la BD y no están en los fijos.
     */
    private List<Opc> cargarMediosExtras() {
        if (medioService == null) return List.of();
        List<Opc> extras = new ArrayList<>();
        try {
            for (MedioTransporte mt : medioService.listar()) {
                String nombre = mt.getNombre();
                if (nombre == null) continue;
                boolean esFijo = MEDIOS_FIJOS.stream()
                        .anyMatch(f -> f.equalsIgnoreCase(nombre));
                if (!esFijo) {
                    extras.add(new Opc("\uf1b9", "#64748b", nombre, "TRANSPORTE"));
                }
            }
        } catch (Exception ignored) {}
        return extras;
    }

    // ════════════════════════════════════════════════════════════════════════
    // BOTÓN PÁNICO
    // ════════════════════════════════════════════════════════════════════════
    private StackPane buildPanicStack() {
        StackPane stack = new StackPane();
        stack.setPrefSize(180, 180);
        stack.setMaxSize(180, 180);

        for (int i = 0; i < 3; i++) {
            Circle wave = new Circle(62);
            wave.setFill(Color.web("#e53935", 0.18 - i * 0.04));
            wave.setStroke(null);
            waves.add(wave);
            stack.getChildren().add(0, wave);
            ScaleTransition sc = new ScaleTransition(Duration.seconds(2.2), wave);
            sc.setFromX(1); sc.setFromY(1); sc.setToX(2.0); sc.setToY(2.0);
            sc.setCycleCount(Animation.INDEFINITE);
            sc.setDelay(Duration.seconds(i * 0.7));
            sc.play(); scaleAnims.add(sc);
            FadeTransition ft = new FadeTransition(Duration.seconds(2.2), wave);
            ft.setFromValue(0.7); ft.setToValue(0);
            ft.setCycleCount(Animation.INDEFINITE);
            ft.setDelay(Duration.seconds(i * 0.7));
            ft.play(); fadeAnims.add(ft);
        }

        Button panicBtn = new Button();
        panicBtn.setPrefSize(120, 120); panicBtn.setMinSize(120, 120); panicBtn.setMaxSize(120, 120);
        panicBtn.setStyle(estiloBotonPanico(false));

        try {
            javafx.scene.image.ImageView shield = new javafx.scene.image.ImageView(
                    new javafx.scene.image.Image(
                            getClass().getResource("/shield-Photoroom.png").toExternalForm()));
            shield.setFitWidth(58); shield.setFitHeight(58); shield.setPreserveRatio(true);
            panicBtn.setGraphic(shield);
        } catch (Exception ignored) {
            Label lbl = new Label("🛡"); lbl.setFont(Font.font(36));
            panicBtn.setGraphic(lbl);
        }

        panicBtn.setOnMouseEntered(e -> panicBtn.setStyle(estiloBotonPanico(true)));
        panicBtn.setOnMouseExited(e -> { if (!panicActive) panicBtn.setStyle(estiloBotonPanico(false)); });
        panicBtn.setOnAction(e -> { panicActive = true; panicBtn.setStyle(estiloBotonPanico(true)); enviarAlerta(panicBtn); });

        stack.getChildren().add(panicBtn);
        return stack;
    }

    private static String estiloBotonPanico(boolean hover) {
        return "-fx-background-color:" + (hover ? "#c62828" : "#e53935") + ";"
                + "-fx-background-radius:100;-fx-cursor:hand;"
                + "-fx-effect:dropshadow(gaussian,rgba(229,57,53," + (hover ? "0.55" : "0.38") + "),"
                + (hover ? "24" : "18") + ",0,0," + (hover ? "7" : "5") + ");";
    }

    // ════════════════════════════════════════════════════════════════════════
    // NORMALIZAR
    // ════════════════════════════════════════════════════════════════════════
    private String normalizarNombre(String nombre) {
        if (nombre == null) return null;
        return switch (nombre) {
            case "Agresión física" -> "Agresion fisica";
            case "Automóvil"      -> "Automovil";
            default               -> nombre;

        };
    }

    // ════════════════════════════════════════════════════════════════════════
    // LÓGICA DE NEGOCIO
    // ════════════════════════════════════════════════════════════════════════

    private void enviarAlerta(Button panicBtn) {
        lblFeedback.setText("");

        if (tipoAlertaSel == null) { error("Selecciona el tipo de emergencia antes de enviar."); panicActive=false; panicBtn.setStyle(estiloBotonPanico(false)); return; }
        if (cmbBarrio.getValue() == null) { error("Selecciona el barrio del incidente."); panicActive=false; panicBtn.setStyle(estiloBotonPanico(false)); return; }
        if (posicionSeleccionada == null) { error("Marca la ubicación en el mapa."); panicActive=false; panicBtn.setStyle(estiloBotonPanico(false)); return; }
        if (usuario == null || usuario.getUsername() == null) { error("Usuario no identificado."); panicActive=false; return; }
        if (alertaService == null) { error("Sin conexión al servicio de alertas."); panicActive=false; return; }

        try {
            Alerta alerta = new Alerta();
            alerta.setDescripcion(txtDescripcion.getText().isBlank() ? null : txtDescripcion.getText().trim());
            alerta.setUsuario(usuario);
            TipoAlerta ta = new TipoAlerta(); ta.setNombre(tipoAlertaSel); alerta.setTipoalerta(ta);
            alerta.setBarrio(cmbBarrio.getValue());
            if (tipoArmaSel != null) { TipoArma arma = new TipoArma(); arma.setNombre(normalizarNombre(tipoArmaSel)); alerta.setTipoarma(arma); }
            if (medioTranspSel != null) { MedioTransporte medio = new MedioTransporte(); medio.setNombre(normalizarNombre(medioTranspSel)); alerta.setMediotransporte(medio); }
            alerta.setLatitud(posicionSeleccionada.getLatitude());
            alerta.setLongitud(posicionSeleccionada.getLongitude());
            Direccion dir = usuario.getDireccion() != null ? usuario.getDireccion() : new Direccion();
            dir.setLatitud(posicionSeleccionada.getLatitude());
            dir.setLongitud(posicionSeleccionada.getLongitude());
            alerta.setDireccion(dir);

            int idGenerado = alertaService.insertar(alerta);
            if (idGenerado > 0) {
                alerta.setId_alerta(idGenerado);
                panicBtn.setStyle("-fx-background-color:#16a34a;-fx-background-radius:100;-fx-cursor:hand;-fx-effect:dropshadow(gaussian,rgba(22,163,74,0.45),20,0,0,6);");

                lblFeedback.setStyle("-fx-text-fill:#16a34a;");
                lblFeedback.setText("✅ Alerta enviada. Las autoridades han sido notificadas.");
                panicBtn.setDisable(true);

                final Alerta alertaFinal = alerta;

                final String tipoFinal   = tipoAlertaSel;

                new Thread(() -> {
                    try {
                        SuscripcionService suscSvc = new SuscripcionService();
                        NotificacionService notiSvc = new NotificacionService();

                        String barrioNom = alertaFinal.getBarrio().getNombre();
                        String comunaNom = alertaFinal.getBarrio().getComuna() != null ? alertaFinal.getBarrio().getComuna().getNombre() : null;
                        List<Suscripcion> aNotificar = new ArrayList<>(suscSvc.listarPorBarrio(barrioNom));
                        if (comunaNom != null) {
                            for (Suscripcion s : suscSvc.listarPorComuna(comunaNom)) {
                                if (aNotificar.stream().noneMatch(x -> x.getUsuario().getIdentificacion().equals(s.getUsuario().getIdentificacion()))) aNotificar.add(s);
                            }
                        }
                        for (Suscripcion s : suscSvc.listarGenerales()) {
                            if (aNotificar.stream().noneMatch(x -> x.getUsuario().getIdentificacion().equals(s.getUsuario().getIdentificacion()))) aNotificar.add(s);
                        }
                        for (Suscripcion s : aNotificar) {
                            try {
                                String alcance = s.getBarrio() != null ? "en el barrio " + s.getBarrio().getNombre()
                                        : s.getComuna() != null ? "en la comuna " + s.getComuna().getNombre() : "en tu ciudad";
                                Notificacion n = new Notificacion();
                                n.setUsuario(s.getUsuario()); n.setAlerta(alertaFinal);
                                n.setMensaje("🚨 Nueva alerta de " + tipoFinal.replace("_", " ") + " reportada " + alcance + "."
                                        + (alertaFinal.getDescripcion() != null ? "\n" + alertaFinal.getDescripcion() : ""));
                                n.setCorreodestinatario(s.getUsuario().getCorreo());
                                notiSvc.insertar(n);
                            } catch (Exception ex) { System.err.println("Error notificando: " + ex.getMessage()); }
                        }
                    } catch (Exception ex) { ex.printStackTrace(); }

                }, "hilo-notificaciones").start();

                new Timeline(new KeyFrame(Duration.seconds(2.5), ev -> {
                    Stage s = (Stage) panicBtn.getScene().getWindow();

                    if (owner != null && owner.getScene() != null) owner.getScene().getRoot().setEffect(null);

                    s.close();
                })).play();

            } else {
                error("Error al enviar la alerta. Intenta nuevamente.");
                panicActive = false; panicBtn.setStyle(estiloBotonPanico(false));
            }

        } catch (Exception ex) {
            error("Error inesperado: " + ex.getMessage());

            ex.printStackTrace(); panicActive = false; panicBtn.setStyle(estiloBotonPanico(false));
        }
    }

    private HBox categoryCard(String unicodeFA, String bg, String texto) {
        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(44, 44); iconBox.setMinSize(44, 44);
        iconBox.setStyle("-fx-background-radius:13;-fx-background-color:" + bg + ";");
        Label emo = new Label(unicodeFA);
        emo.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';-fx-font-size:20px;-fx-text-fill:white;");
        iconBox.getChildren().add(emo);
        Label txt = new Label(texto);
        txt.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        txt.setTextFill(Color.web("#1E293B"));
        txt.setWrapText(true); txt.setMaxWidth(118);
        HBox card = new HBox(10, iconBox, txt);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(12, 10, 12, 12));
        card.setStyle(cardNormal());
        card.setOnMouseEntered(e -> { if (!card.equals(tarjetaActiva)) card.setStyle(cardHover()); });
        card.setOnMouseExited(e  -> { if (!card.equals(tarjetaActiva)) card.setStyle(cardNormal()); });
        return card;
    }

    private void activarCategoria(HBox tarjeta, String tipo, VBox subpanel, String colorActivo) {
        tipoAlertaSel = tipo;
        if (tarjetaActiva != null) tarjetaActiva.setStyle(cardNormal());
        tarjeta.setStyle(cardActivo(colorActivo));
        tarjetaActiva = tarjeta;
        for (VBox p : todosSubpaneles) { p.setVisible(false); p.setManaged(false); }
        subpanel.setVisible(true); subpanel.setManaged(true);
    }

    private static String cardActivo(String color) {
        return "-fx-background-color:white;-fx-background-radius:14;-fx-border-radius:14;"
                + "-fx-border-color:" + color + ";-fx-border-width:2.5;-fx-cursor:hand;";
    }

    private VBox containerSubpanel(javafx.scene.Node... nodos) {
        VBox panel = new VBox(12);
        panel.setVisible(false); panel.setManaged(false);
        panel.setStyle("-fx-background-color:#f8fafc;-fx-background-radius:14;"
                + "-fx-border-color:#e2e8f0;-fx-border-radius:14;-fx-border-width:1;-fx-padding:14;");
        panel.getChildren().addAll(nodos);
        return panel;
    }

    private VBox buildSeccion(String titulo, List<Opc> opciones) {
        Label tit = new Label(titulo);
        tit.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        tit.setTextFill(Color.web(DARK));
        return new VBox(8, tit, buildMiniGrid(opciones));
    }

    private VBox buildSubpanelSimple(String titulo, List<Opc> opciones) {
        VBox panel = new VBox(10);
        panel.setVisible(false); panel.setManaged(false);
        panel.setStyle("-fx-background-color:#f8fafc;-fx-background-radius:14;"
                + "-fx-border-color:#e2e8f0;-fx-border-radius:14;-fx-border-width:1;-fx-padding:14;");
        Label tit = new Label(titulo);
        tit.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        tit.setTextFill(Color.web(DARK));
        panel.getChildren().addAll(tit, buildMiniGrid(opciones));
        return panel;
    }

    private VBox buildSubpanelConNodo(String titulo, javafx.scene.Node extra) {
        VBox panel = new VBox(10);
        panel.setVisible(false); panel.setManaged(false);
        panel.setStyle("-fx-background-color:#f8fafc;-fx-background-radius:14;"
                + "-fx-border-color:#e2e8f0;-fx-border-radius:14;-fx-border-width:1;-fx-padding:14;");
        Label tit = new Label(titulo);
        tit.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        tit.setTextFill(Color.web(DARK));
        panel.getChildren().addAll(tit, extra);
        return panel;
    }

    private GridPane buildMiniGrid(List<Opc> opciones) {
        GridPane g = new GridPane();
        g.setHgap(7); g.setVgap(7);
        ColumnConstraints c1 = new ColumnConstraints(); c1.setPercentWidth(50);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setPercentWidth(50);
        g.getColumnConstraints().addAll(c1, c2);
        List<HBox> tarjetas = new ArrayList<>();
        int col = 0, row = 0;
        for (Opc op : opciones) {
            HBox card = miniCard(op.emoji, op.bg, op.label);
            final String tipo = op.tipo, val = op.label;
            card.setOnMouseClicked(e -> {
                tarjetas.forEach(h -> h.setStyle(miniNormal()));
                card.setStyle(miniActivo());
                if ("ARMA".equals(tipo))       tipoArmaSel    = val;
                else if ("TRANSPORTE".equals(tipo)) medioTranspSel = val;
            });
            tarjetas.add(card);
            g.add(card, col, row);
            col++; if (col == 2) { col = 0; row++; }
        }
        return g;
    }

    private HBox miniCard(String emoji, String bg, String texto) {
        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(32, 32); iconBox.setMinSize(32, 32);
        iconBox.setStyle("-fx-background-radius:9;-fx-background-color:" + bg + ";");
        Label emo = new Label(emoji);
        emo.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';-fx-font-size:14px;-fx-text-fill:white;");
        iconBox.getChildren().add(emo);
        Label txt = new Label(texto);
        txt.setFont(Font.font("Arial", 12)); txt.setTextFill(Color.web(TEXT_MAIN));
        txt.setWrapText(true); txt.setMaxWidth(108);
        HBox card = new HBox(8, iconBox, txt);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(8, 10, 8, 10));
        card.setStyle(miniNormal());
        card.setOnMouseEntered(e -> { if (!card.getStyle().contains("#2563eb")) card.setStyle(miniHover()); });
        card.setOnMouseExited(e  -> { if (!card.getStyle().contains("#2563eb")) card.setStyle(miniNormal()); });
        return card;
    }

    private Region dividerLine() {
        Region r = new Region(); r.setPrefHeight(1);
        r.setStyle("-fx-background-color:#e2e8f0;");
        return r;
    }

    private ComboBox<Barrio> buildComboBarrio() {
        ComboBox<Barrio> cb = new ComboBox<>();
        cb.setPromptText("Selecciona el barrio del incidente");
        cb.setMaxWidth(Double.MAX_VALUE); cb.setPrefHeight(48);
        cb.setStyle("-fx-background-color:" + FIELD_BG + ";-fx-text-fill:" + TEXT_FIELD
                + ";-fx-prompt-text-fill:#9ca3af;-fx-border-color:transparent;"
                + "-fx-border-radius:30;-fx-background-radius:30;-fx-font-size:13px;");
        cb.setCellFactory(lv -> new ListCell<Barrio>() {
            @Override protected void updateItem(Barrio item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNombre());
                if (!empty && item != null) {
                    setStyle("-fx-background-color:transparent;-fx-text-fill:#4b5563;-fx-font-size:13px;-fx-padding:9 14 9 14;");
                    setOnMouseEntered(e -> setStyle("-fx-background-color:" + DARK_GRAD + ";-fx-background-radius:6;-fx-text-fill:white;-fx-font-size:13px;-fx-padding:9 14 9 14;"));
                    setOnMouseExited(e  -> setStyle("-fx-background-color:transparent;-fx-text-fill:#4b5563;-fx-font-size:13px;-fx-padding:9 14 9 14;"));
                }
            }
        });
        cb.setButtonCell(new ListCell<Barrio>() {
            @Override protected void updateItem(Barrio item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null) { setText(item.getNombre()); setStyle("-fx-background-color:" + FIELD_BG + ";-fx-background-radius:30;-fx-text-fill:#111827;-fx-font-size:13px;-fx-padding:4 14 4 14;"); }
                else { setText("Selecciona el barrio del incidente"); setStyle("-fx-background-color:transparent;-fx-text-fill:#9ca3af;-fx-font-size:13px;"); }
            }
        });
        if (barrioService != null) {
            try {
                List<Barrio> barrios = barrioService.listar();
                cb.getItems().setAll(barrios);
                if (usuario != null && usuario.getDireccion() != null && usuario.getDireccion().getBarrio() != null) {
                    String nombre = usuario.getDireccion().getBarrio().getNombre();
                    barrios.stream().filter(b -> b.getNombre().equalsIgnoreCase(nombre)).findFirst().ifPresent(cb::setValue);
                }
            } catch (Exception ignored) {}
        }
        return cb;
    }

    private VBox buildCoordChip() {
        Label chipTitulo = new Label("Coordenadas del incidente");
        chipTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        chipTitulo.setTextFill(Color.web(TEXT_FIELD));
        Label valCoord = new Label();
        valCoord.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        valCoord.setTextFill(Color.web(TEXT_SUB));
        valCoord.textProperty().bind(coordProp);
        coordProp.addListener((obs, o, n) -> { if (!n.equals("—")) { valCoord.setTextFill(Color.web(GREEN)); chipTitulo.setTextFill(Color.web(GREEN)); } });
        HBox inner = new HBox(10,
                new Label("📍") {{ setFont(Font.font(14)); }},
                new VBox(3, chipTitulo, valCoord));
        inner.setAlignment(Pos.CENTER_LEFT);
        inner.setPadding(new Insets(12, 16, 12, 16));
        inner.setStyle("-fx-background-color:" + BLUE_LIGHT + ";-fx-border-color:" + BLUE_BDR
                + ";-fx-border-width:1;-fx-border-radius:12;-fx-background-radius:12;");
        return new VBox(inner);
    }

    // ── Estilos ───────────────────────────────────────────────────────────────
    private static String cardNormal() { return "-fx-background-color:white;-fx-background-radius:14;-fx-border-radius:14;-fx-border-color:#E2E8F0;-fx-border-width:1.5;-fx-cursor:hand;"; }
    private static String cardHover()  { return "-fx-background-color:#F8FAFC;-fx-background-radius:14;-fx-border-radius:14;-fx-border-color:#94A3B8;-fx-border-width:1.5;-fx-cursor:hand;"; }
    private static String cardActivo() { return "-fx-background-color:#EFF6FF;-fx-background-radius:14;-fx-border-radius:14;-fx-border-color:#1D4ED8;-fx-border-width:2.5;-fx-cursor:hand;"; }
    private static String miniNormal() { return "-fx-background-color:white;-fx-background-radius:10;-fx-border-radius:10;-fx-border-color:#e2e8f0;-fx-border-width:1;-fx-cursor:hand;"; }
    private static String miniHover()  { return "-fx-background-color:#f0f9ff;-fx-background-radius:10;-fx-border-radius:10;-fx-border-color:#bae6fd;-fx-border-width:1;-fx-cursor:hand;"; }
    private static String miniActivo() { return "-fx-background-color:#dbeafe;-fx-background-radius:10;-fx-border-radius:10;-fx-border-color:#2563eb;-fx-border-width:2;-fx-cursor:hand;-fx-effect:dropshadow(gaussian,rgba(37,99,235,0.15),6,0,0,1);"; }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private Label sectionLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        l.setTextFill(Color.web(TEXT_FIELD));
        return l;
    }

    private TextArea styledTextArea(String prompt, int rows) {
        TextArea ta = new TextArea();
        ta.setPromptText(prompt); ta.setWrapText(true); ta.setPrefRowCount(rows);
        String base = "-fx-background-color:white;-fx-background-insets:0;-fx-background-radius:12;-fx-border-radius:12;-fx-border-color:#CBD5E1;-fx-border-width:1.5;-fx-font-size:13px;-fx-font-family:'Arial';-fx-text-fill:#1E293B;-fx-prompt-text-fill:#94A3B8;-fx-padding:12 14 12 14;";
        String focused = "-fx-background-color:white;-fx-background-insets:0;-fx-background-radius:12;-fx-border-radius:12;-fx-border-color:#3B82F6;-fx-border-width:2;-fx-font-size:13px;-fx-font-family:'Arial';-fx-text-fill:#1E293B;-fx-prompt-text-fill:#94A3B8;-fx-padding:12 14 12 14;";
        ta.setStyle(base);

        ta.focusedProperty().addListener((obs, o, isFocused) -> ta.setStyle(isFocused ? focused : base));

        ta.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            if (newSkin != null) {
                javafx.scene.Node sp = ta.lookup(".scroll-pane"); if (sp != null) sp.setStyle("-fx-background-color:white;-fx-background-insets:0;");
                javafx.scene.Node vp = ta.lookup(".scroll-pane .viewport"); if (vp != null) vp.setStyle("-fx-background-color:white;");
                javafx.scene.Node ct = ta.lookup(".content"); if (ct != null) ct.setStyle("-fx-background-color:white;");
            }
        });
        return ta;
    }


    private HBox styledToggle(String texto, java.util.function.Consumer<Boolean> onChange) {
        StackPane track = new StackPane();
        track.setPrefSize(42, 24); track.setMinSize(42, 24);
        track.setStyle("-fx-background-radius:20;-fx-background-color:#e2e8f0;-fx-cursor:hand;");
        javafx.scene.shape.Circle thumb = new javafx.scene.shape.Circle(9);
        thumb.setFill(Color.WHITE);
        thumb.setEffect(new DropShadow(4, 0, 1, Color.rgb(0, 0, 0, 0.2)));
        StackPane.setAlignment(thumb, Pos.CENTER_LEFT);
        StackPane.setMargin(thumb, new Insets(0, 0, 0, 3));
        track.getChildren().add(thumb);
        Label lbl = new Label(texto);
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        lbl.setTextFill(Color.web(TEXT_SUB));
        HBox box = new HBox(10, track, lbl);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(8, 0, 4, 0));

        final boolean[] activo = {false};
        track.setOnMouseClicked(e -> {
            activo[0] = !activo[0];
            if (activo[0]) { track.setStyle("-fx-background-radius:20;-fx-background-color:#16a34a;-fx-cursor:hand;"); StackPane.setAlignment(thumb, Pos.CENTER_RIGHT); StackPane.setMargin(thumb, new Insets(0,3,0,0)); lbl.setTextFill(Color.web(GREEN)); }
            else           { track.setStyle("-fx-background-radius:20;-fx-background-color:#e2e8f0;-fx-cursor:hand;"); StackPane.setAlignment(thumb, Pos.CENTER_LEFT);  StackPane.setMargin(thumb, new Insets(0,0,0,3)); lbl.setTextFill(Color.web(TEXT_SUB)); }
            onChange.accept(activo[0]);
        });
        return box;
    }

    private VBox buildDescripcionBox() {
        Label icoFA = new Label("\uf303");
        icoFA.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';-fx-font-size:13px;-fx-text-fill:#6B7280;");
        Label lblTit = new Label("Descripción del incidente");
        lblTit.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        lblTit.setTextFill(Color.web("#374151"));
        HBox header = new HBox(6, icoFA, lblTit);
        header.setAlignment(Pos.CENTER_LEFT);

        TextArea taFuncional = styledTextArea("Describe brevemente la emergencia...", 4);
        txtDescripcion = taFuncional;
        return new VBox(8, header, taFuncional);
    }

    private void error(String msg) {
        lblFeedback.setStyle("-fx-text-fill:" + RED + ";");
        lblFeedback.setText(msg);
    }

    private java.awt.Color getColorAlerta() {
        if (tipoAlertaSel == null) return new java.awt.Color(229, 57, 53);
        return switch (tipoAlertaSel) {
            case "ROBO"              -> new java.awt.Color(230, 126,  34);
            case "HOMICIDIO"         -> new java.awt.Color( 26,  35,  50);
            case "PERSONA_SOSPECHOSA"-> new java.awt.Color( 14, 165, 233);
            case "INCENDIO"          -> new java.awt.Color(229,  57,  53);
            case "EMERGENCIA_MEDICA" -> new java.awt.Color( 29,  78, 216);
            case "ACCIDENTE"         -> new java.awt.Color(124,  58, 237);
            case "RUIDO"             -> new java.awt.Color( 22, 163,  74);
            default                  -> new java.awt.Color(100, 116, 139); // gris para extras
        };

    }

    private java.awt.image.BufferedImage recortarTransparencia(java.awt.image.BufferedImage img) {
        int w = img.getWidth(), h = img.getHeight();
        int top = h, bottom = 0, left = w, right = 0;
        for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) {
            if (((img.getRGB(x, y) >> 24) & 0xff) > 10) {
                if (y < top) top = y; if (y > bottom) bottom = y;
                if (x < left) left = x; if (x > right) right = x;
            }
        }
        if (top >= bottom || left >= right) return img;
        return img.getSubimage(left, top, right - left + 1, bottom - top + 1);
    }

    private record Opc(String emoji, String bg, String label, String tipo) {}
}
