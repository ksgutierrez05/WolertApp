/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.view;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.embed.swing.SwingNode;
import javafx.util.Duration;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCenter;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;
import sistemagestion.model.*;
import sistemagestion.service.AlarmaService;
import sistemagestion.service.AlertaService;
import sistemagestion.service.AsignacionUnidadService;
import sistemagestion.service.UnidadPolicialService;

import javax.swing.SwingUtilities;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author Lenovo
 */
public class MapaOperaciones {

    // ── Paleta ────────────────────────────────────────────────────
    private static final String BG = "#f0f2f5";
    private static final String CARD = "rgba(255,255,255,0.92)";
    private static final String BORDER = "#e5e7eb";
    private static final String DARK = "#16283d";
    private static final String DARK_GRAD = "linear-gradient(to bottom right,#16283d,#1f3a56)";
    private static final String TEXT_MAIN = "#111827";
    private static final String GRAY_TEXT = "#6b7280";

    // Colores de capa
    private static final Color FX_ALARMA = Color.web("#ffc107");
    private static final Color FX_UNIDAD = Color.web("#2f568a");

    private static final Color FX_ALERTA = Color.web("#e53935");

    private static final java.awt.Color AWT_ALARMA = new java.awt.Color(251, 140, 0);
    private static final java.awt.Color AWT_ALERTA = new java.awt.Color(229, 57, 53);

    // ── Servicios ─────────────────────────────────────────────────
    private final AlarmaService alarmaService;
    private final UnidadPolicialService unidadService;
    private final AlertaService alertaService;
    private final AsignacionUnidadService asignacionService;

    // ── Datos ─────────────────────────────────────────────────────
    private List<Alarma> todasAlarmas = new ArrayList<>();
    private List<UnidadPolicial> todasUnidades = new ArrayList<>();
    private List<Alerta> todasAlertas = new ArrayList<>();

    private List<Alarma> alarmas = new ArrayList<>();
    private List<UnidadPolicial> unidades = new ArrayList<>();
    private List<Alerta> alertas = new ArrayList<>();

    // ── Visibilidad capas ─────────────────────────────────────────
    private boolean mostrarAlarmas = true;
    private boolean mostrarUnidades = true;
    private boolean mostrarAlertas = true;

    // ── Filtros ───────────────────────────────────────────────────
    private String filtroAlarma = "Todos";
    private String filtroUnidad = "Todos";
    private String filtroAlerta = "Todos";

    // ── Mapa ──────────────────────────────────────────────────────
    private JXMapViewer mapa;

    // ── PNG cache ─────────────────────────────────────────────────
    private java.awt.image.BufferedImage imgSirena;
    private java.awt.image.BufferedImage imgPolicia;

    // ── UI componentes clave ──────────────────────────────────────
    private StackPane rootStack;
    private Label lblContador;
    private Label lblCoordFooter;

    private static class Flotante {

        VBox card;
        GeoPosition geo;

        Flotante(VBox card, GeoPosition geo) {
            this.card = card;
            this.geo = geo;
        }
    }
    private final List<Flotante> flotantes = new ArrayList<>();

// ── Font Awesome ──────────────────────────────────────────────
    private String FA_FAMILY = "Font Awesome 6 Free Solid";

    // Acordeones
    private VBox acordeonAlarmasRef;
    private VBox acordeonUnidadesRef;
    private VBox acordeonAlertasRef;

    // Panel lateral derecho
    private VBox panelDerecho;

    // Seleccionado
    private GeoPosition seleccionado = null;

    // ═══════════════════════════════════════════════════════════════
    // Constructores
    // ═══════════════════════════════════════════════════════════════
    public MapaOperaciones(AsignacionUnidadService asignacionService,
            UnidadPolicialService unidadService) {
        this.asignacionService = asignacionService;
        this.unidadService = unidadService;
        this.alarmaService = tryS(AlarmaService.class);
        this.alertaService = tryS(AlertaService.class);
        cargarImagenes();
        cargarDatos();
    }

    public MapaOperaciones(AsignacionUnidadService asignacionService) {
        this(asignacionService, tryUnidad());
    }

    @SuppressWarnings("unchecked")
    private <T> T tryS(Class<T> cls) {
        try {
            if (cls == AlarmaService.class) {
                return (T) new AlarmaService();
            }
            if (cls == AlertaService.class) {
                return (T) new AlertaService();
            }
        } catch (SQLException ignored) {
        }
        return null;
    }

    private static UnidadPolicialService tryUnidad() {
        try {
            return new UnidadPolicialService();
        } catch (SQLException e) {
            return null;
        }
    }

    // ── PNG ───────────────────────────────────────────────────────
    private void cargarImagenes() {
        // DEBUG — imprime qué encuentra
        var urlSirena = getClass().getResource("/SirenaPin.png");
        var urlPolicia = getClass().getResource("/PinPolicia.png");
        System.out.println("SirenaPin URL: " + urlSirena);
        System.out.println("PinPolicia URL: " + urlPolicia);

        imgSirena = loadPng("/SirenaPin.png");
        imgPolicia = loadPng("/PinPolicia.png");

        System.out.println("imgSirena cargada: " + (imgSirena != null
                ? imgSirena.getWidth() + "x" + imgSirena.getHeight() : "NULL"));
        System.out.println("imgPolicia cargada: " + (imgPolicia != null
                ? imgPolicia.getWidth() + "x" + imgPolicia.getHeight() : "NULL"));
    }

    private java.awt.image.BufferedImage loadPng(String res) {
        try {
            var is = getClass().getResourceAsStream(res);
            if (is == null) {
                System.err.println("Recurso no encontrado: " + res);
                return null;
            }
            java.awt.image.BufferedImage raw = javax.imageio.ImageIO.read(is);
            if (raw == null) {
                System.err.println("ImageIO no pudo leer: " + res);
                return null;
            }
            System.out.println("Raw cargado " + res + ": "
                    + raw.getWidth() + "x" + raw.getHeight()
                    + " type=" + raw.getType()
                    + " hasAlpha=" + raw.getColorModel().hasAlpha());

            java.awt.image.BufferedImage argb = new java.awt.image.BufferedImage(
                    raw.getWidth(), raw.getHeight(),
                    java.awt.image.BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D g2 = argb.createGraphics();
            g2.drawImage(raw, 0, 0, null);
            g2.dispose();

            java.awt.image.BufferedImage recortada = recortarTransparencia(argb);

            if (recortada == argb || (recortada.getWidth() > 0 && recortada.getHeight() > 0)) {
                return recortada;
            }
            return argb;  // fallback: imagen completa convertida a ARGB
        } catch (Exception e) {
            System.err.println("Error cargando " + res + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // ── Datos ─────────────────────────────────────────────────────
    private void cargarDatos() {
        try {
            if (alarmaService != null) {
                todasAlarmas = alarmaService.listar();
            }
        } catch (Exception ignored) {
        }
        try {
            if (unidadService != null) {
                todasUnidades = unidadService.listar();
            }
        } catch (Exception ignored) {
        }
        try {
            if (alertaService != null) {
                todasAlertas = alertaService.listar();
            }
        } catch (Exception ignored) {
        }
        aplicarFiltros();
        System.out.println("Alertas cargadas: " + todasAlertas.size());
        for (Alerta al : todasAlertas) {
            System.out.println("  → " + al.getEstado() + " lat=" + al.getLatitud()
                    + " lng=" + al.getLongitud());
        }

    }

    private void aplicarFiltros() {
        alarmas = todasAlarmas.stream()
                .filter(a -> "Todos".equals(filtroAlarma)
                || (a.getEstado() != null && a.getEstado().name().equals(filtroAlarma)))
                .collect(Collectors.toList());

        unidades = todasUnidades.stream()
                .filter(u -> "Todos".equals(filtroUnidad)
                || (u.getEstado() != null && u.getEstado().name().equals(filtroUnidad)))
                .collect(Collectors.toList());

        alertas = todasAlertas.stream()
                .filter(al -> "Todos".equals(filtroAlerta)
                || (al.getEstado() != null && al.getEstado().name().equals(filtroAlerta)))
                .collect(Collectors.toList());

        Platform.runLater(this::actualizarContadores);
    }

    // ═══════════════════════════════════════════════════════════════
    // BUILD PRINCIPAL
    // ═══════════════════════════════════════════════════════════════
    public BorderPane build() {
        var font = Font.loadFont(getClass().getResourceAsStream("/fa-solid-900.ttf"), 20);
        System.out.println("FA cargado: " + (font != null ? font.getName() : "NULL"));
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:" + BG + ";");
        root.setTop(buildTopBar());

        // Centro = mapa + overlays flotantes
        rootStack = new StackPane();
        SwingNode swingNode = new SwingNode();

        Platform.runLater(()
                -> SwingUtilities.invokeLater(()
                        -> inicializarMapa(swingNode)
                )
        );

        // Coord footer
        lblCoordFooter = new Label("📍  —,  —");
        lblCoordFooter.setFont(Font.font("Arial", 12));
        lblCoordFooter.setTextFill(Color.web("#374151"));
        lblCoordFooter.setStyle(glassStyle() + "-fx-padding:6 18;");

        // Detalle flotante (esquina inf-derecha)
        VBox leyendaFlotante = buildLeyendaFlotante();
        rootStack.getChildren().addAll(swingNode, leyendaFlotante, lblCoordFooter);
        leyendaFlotante.setMaxHeight(Region.USE_PREF_SIZE);  // ← antes de añadirlo al StackPane
        StackPane.setAlignment(leyendaFlotante, Pos.BOTTOM_LEFT);
        StackPane.setMargin(leyendaFlotante, new Insets(0, 0, 50, 12));
        StackPane.setAlignment(lblCoordFooter, Pos.BOTTOM_CENTER);
        StackPane.setMargin(lblCoordFooter, new Insets(0, 0, 18, 0));

        // Panel lateral con toggle
        panelDerecho = buildPanelDerecho();

        HBox centro = new HBox(rootStack, panelDerecho);
        HBox.setHgrow(rootStack, Priority.ALWAYS);
        root.setCenter(centro);

        return root;

    }

    // ═══════════════════════════════════════════════════════════════
    // TOP BAR MODERNA
    // ═══════════════════════════════════════════════════════════════
    private HBox buildTopBar() {
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
        logoBox.setStyle("-fx-background-color:" + DARK_GRAD
                + ";-fx-background-radius:6;-fx-padding:6;");

        Label logoText = new Label("WolertApp");
        logoText.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        logoText.setTextFill(Color.web(TEXT_MAIN));

        Region sep = new Region();
        sep.setStyle("-fx-background-color:" + BORDER + ";");
        sep.setPrefSize(1, 20);

        Label instruccion = new Label("Haz clic en un elemento para ver detalles");
        instruccion.setFont(Font.font("Arial", 13));
        instruccion.setTextFill(Color.web(GRAY_TEXT));

        // Leyenda de capas 
        HBox leyendaCapas = new HBox(14,
                puntito(toHex(FX_ALARMA), "Alarmas"),
                puntito(toHex(FX_UNIDAD), "Unidades"),
                puntito(toHex(FX_ALERTA), "Alertas"));
        leyendaCapas.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Contador
        Label cntTopBar = new Label("0 elementos");
        cntTopBar.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        cntTopBar.setTextFill(Color.WHITE);
        cntTopBar.setStyle("-fx-background-color:" + DARK_GRAD
                + ";-fx-background-radius:20;-fx-padding:4 14 4 14;");

        HBox bar = new HBox(10, logoBox, logoText, sep,
                instruccion, spacer, leyendaCapas);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(12, 20, 12, 20));
        bar.setStyle("-fx-background-color:white;"
                + "-fx-border-color:" + BORDER + ";-fx-border-width:0 0 1 0;");
        return bar;
    }

    private HBox puntito(String color, String texto) {
        javafx.scene.shape.Circle c = new javafx.scene.shape.Circle(5);
        c.setFill(Color.web(color));
        Label lbl = new Label(texto);
        lbl.setFont(Font.font("Arial", 11));
        lbl.setTextFill(Color.web(GRAY_TEXT));
        HBox h = new HBox(5, c, lbl);
        h.setAlignment(Pos.CENTER_LEFT);
        return h;
    }

    private VBox buildPillColapsable(String ico, String titulo, int total,
            Color color, String bgLight,
            java.util.function.Supplier<VBox> buildContenido) {

        String stClosed = "-fx-background-color:white;"
                + "-fx-border-color:" + BORDER + ";-fx-border-width:0 0 1 0;";
        String stOpen = "-fx-background-color:white;"
                + "-fx-border-color:" + BORDER + ";-fx-border-width:0 0 1 0;";

        // Header
        Label icoLbl = new Label(ico);
        icoLbl.setStyle("-fx-font-family:'" + FA_FAMILY + "';"
                + "-fx-font-size:13px;"
                + "-fx-font-weight:900;");
// El color del ícono del acordeón sigue el color del texto del panel
        icoLbl.setTextFill(Color.web(GRAY_TEXT));

        Label ttlLbl = new Label(titulo + " (" + total + ")");
        ttlLbl.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        ttlLbl.setTextFill(Color.web(TEXT_MAIN));

        Label arrow = new Label("▸");
        arrow.setFont(Font.font("Arial", 11));
        arrow.setTextFill(Color.web(GRAY_TEXT));

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        HBox head = new HBox(8, icoLbl, ttlLbl, sp, arrow);
        head.setAlignment(Pos.CENTER_LEFT);
        head.setPadding(new Insets(10, 16, 10, 16));
        head.setCursor(javafx.scene.Cursor.HAND);
        head.setStyle("-fx-background-color:white;");
        head.setOnMouseEntered(e -> head.setStyle("-fx-background-color:#f8fafc;"));
        head.setOnMouseExited(e -> head.setStyle("-fx-background-color:white;"));

        Region sepLine = new Region();
        sepLine.setPrefHeight(1);
        sepLine.setStyle("-fx-background-color:" + BORDER + ";");
        sepLine.setVisible(false);
        sepLine.setManaged(false);

        VBox[] listaRef = {null};
        ScrollPane[] scrollRef = {null};
        final boolean[] abierto = {false};

        VBox wrapper = new VBox(head, sepLine);
        wrapper.setMaxWidth(Double.MAX_VALUE);
        wrapper.setStyle(stClosed);

        head.setOnMouseClicked(e -> {
            abierto[0] = !abierto[0];
            if (abierto[0]) {
                listaRef[0] = buildContenido.get();
                listaRef[0].setStyle("-fx-background-color:white;");
                scrollRef[0] = new ScrollPane(listaRef[0]);
                scrollRef[0].setFitToWidth(true);
                scrollRef[0].setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                scrollRef[0].setPrefViewportHeight(
                        Math.min(listaRef[0].getChildren().size() * 44, 200));
                scrollRef[0].setStyle("-fx-background:white;-fx-background-color:white;"
                        + "-fx-border-color:transparent;");
                wrapper.getChildren().add(scrollRef[0]);
                sepLine.setVisible(true);
                sepLine.setManaged(true);
                arrow.setText("▾");
                FadeTransition ft = new FadeTransition(Duration.millis(150), scrollRef[0]);
                ft.setFromValue(0);
                ft.setToValue(1);
                ft.play();
            } else {
                if (scrollRef[0] != null) {
                    FadeTransition ft = new FadeTransition(Duration.millis(100), scrollRef[0]);
                    ft.setFromValue(1);
                    ft.setToValue(0);
                    ft.setOnFinished(ev -> {
                        wrapper.getChildren().remove(scrollRef[0]);
                        scrollRef[0] = null;
                        listaRef[0] = null;
                    });
                    ft.play();
                }
                sepLine.setVisible(false);
                sepLine.setManaged(false);
                arrow.setText("▸");
            }
        });
        return wrapper;
    }

    private VBox buildListaAlarmas() {
        VBox v = new VBox(4);
        for (Alarma a : alarmas) {
            HBox row = acordeonItem(a.getNombre(),
                    a.getEstado() != null ? a.getEstado().name() : "—",
                    FX_ALARMA,
                    () -> centrarEn(a.getLatitud(), a.getLongitud()));
            v.getChildren().add(row);
        }
        if (v.getChildren().isEmpty()) {
            v.getChildren().add(mkEmpty("Sin alarmas"));
        }
        return v;
    }

    private VBox buildListaUnidades() {
        VBox v = new VBox(4);
        for (UnidadPolicial u : unidades) {
            Color c = fxColorUnidad(u.getEstado());
            HBox row = acordeonItem(u.getNombre(),
                    u.getEstado() != null ? u.getEstado().name() : "—",
                    c,
                    () -> centrarEn(u.getLatitud(), u.getLongitud()));
            v.getChildren().add(row);
        }
        if (v.getChildren().isEmpty()) {
            v.getChildren().add(mkEmpty("Sin unidades"));
        }
        return v;
    }

    private VBox buildListaAlertas() {
        VBox v = new VBox(4);
        for (Alerta al : alertas) {
            String tipo = al.getTipoalerta() != null ? al.getTipoalerta().getNombre() : "—";
            Color c = fxColorAlerta(al.getEstado());

            // ← CAMBIO: leer coords desde Direccion
            double alLat = al.getLatitud();
            double alLng = al.getLongitud();

            HBox row = acordeonItem(tipo,
                    al.getEstado() != null ? al.getEstado().name() : "—",
                    c,
                    () -> centrarEn(alLat, alLng));
            v.getChildren().add(row);
        }
        if (v.getChildren().isEmpty()) {
            v.getChildren().add(mkEmpty("Sin alertas"));
        }
        return v;
    }

    private HBox acordeonItem(String nombre, String sub, Color accent, Runnable onClick) {
        javafx.scene.shape.Circle dot = new javafx.scene.shape.Circle(5, accent);

        Label n = new Label(nombre != null ? nombre : "—");
        n.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        n.setTextFill(Color.web(TEXT_MAIN));
        n.setMaxWidth(200);
        n.setEllipsisString("…");

        Label s = new Label(sub);
        s.setFont(Font.font("Arial", 10));
        s.setTextFill(Color.web(GRAY_TEXT));

        VBox txt = new VBox(2, n, s);
        HBox.setHgrow(txt, Priority.ALWAYS);

        Label flecha = new Label("›");
        flecha.setFont(Font.font("Arial", 16));
        flecha.setTextFill(Color.web("#d1d5db"));

        HBox row = new HBox(12, dot, txt, flecha);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 16, 8, 20));
        row.setCursor(javafx.scene.Cursor.HAND);
        String stN = "-fx-background-color:white;-fx-border-color:transparent transparent "
                + BORDER + " transparent;-fx-border-width:0 0 1 0;";
        String stH = "-fx-background-color:#f8fafc;-fx-border-color:transparent transparent "
                + BORDER + " transparent;-fx-border-width:0 0 1 0;";
        row.setStyle(stN);
        row.setOnMouseEntered(e -> row.setStyle(stH));
        row.setOnMouseExited(e -> row.setStyle(stN));
        row.setOnMouseClicked(e -> onClick.run());
        return row;
    }

    private Label mkEmpty(String t) {
        Label l = new Label(t);
        l.setFont(Font.font("Arial", 11));
        l.setTextFill(Color.web(GRAY_TEXT));
        l.setPadding(new Insets(4, 8, 4, 8));
        return l;
    }

    /**
     * Leyenda compacta de estados de alerta, flotante.
     */
    private VBox buildLeyendaFlotante() {
        String[][] items = {
            {"#fb8c00", "PENDIENTE"},
            {"#1565c0", "RECIBIDA"},
            {"#7b1fa2", "EN_ATENCION"},
            {"#0288d1", "UNID_ASIGNADA"},
            {"#43a047", "RESUELTA"},
            {"#9e9e9e", "CANCELADA"}
        };

        VBox lista = new VBox(4);

        for (String[] item : items) {
            javafx.scene.shape.Circle dot = new javafx.scene.shape.Circle(4);
            dot.setFill(Color.web(item[0]));

            Label lbl = new Label(item[1]);
            lbl.setFont(Font.font("Arial", 10));
            lbl.setTextFill(Color.web(GRAY_TEXT));

            HBox cell = new HBox(6, dot, lbl);
            cell.setAlignment(Pos.CENTER_LEFT);
            lista.getChildren().add(cell);
        }

        Label tit = new Label("ESTADOS ALERTA");
        tit.setFont(Font.font("Arial", FontWeight.BOLD, 9));
        tit.setTextFill(Color.web(GRAY_TEXT));

        VBox box = new VBox(5, tit, lista);
        box.setPadding(new Insets(8, 10, 8, 10));
        box.setMinWidth(130);
        box.setMaxWidth(Region.USE_PREF_SIZE);
        box.setStyle(
                "-fx-background-color:rgba(255,255,255,0.88);"
                + "-fx-background-radius:10;"
                + "-fx-border-radius:10;"
                + "-fx-border-color:rgba(229,57,53,0.25);"
                + "-fx-border-width:1;"
                + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.10),10,0,0,2);"
        );
        return box;
    }

    // ═══════════════════════════════════════════════════════════════
    // DETALLE FLOTANTE (inf-derecha)
    // ═══════════════════════════════════════════════════════════════
    private void mostrarDetalleEn(String nombre, String tipo, String info,
            String accentColor, double screenX, double screenY,
            double geoLat, double geoLng) {

        Label lblNombre = new Label(nombre);
        lblNombre.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        lblNombre.setTextFill(Color.web(TEXT_MAIN));

        Label lblTipo = new Label(tipo);
        lblTipo.setFont(Font.font("Arial", 10));
        lblTipo.setTextFill(Color.web(accentColor));

        Region sepLine = new Region();
        sepLine.setPrefHeight(1);
        sepLine.setPrefWidth(200);
        sepLine.setStyle("-fx-background-color:" + BORDER + ";");

        VBox filas = new VBox(2);
        for (String linea : info.split("\n")) {
            if (linea.isBlank()) {
                continue;
            }
            String[] p = linea.split(":", 2);
            HBox fila = new HBox(4);
            fila.setAlignment(Pos.CENTER_LEFT);
            Label clave = new Label((p.length == 2 ? p[0].trim() + ":" : linea.trim()));
            clave.setFont(Font.font("Arial", FontWeight.BOLD, 10));
            clave.setTextFill(Color.web(GRAY_TEXT));
            fila.getChildren().add(clave);
            if (p.length == 2) {
                Label val = new Label(p[1].trim());
                val.setFont(Font.font("Arial", 10));
                val.setTextFill(Color.web(TEXT_MAIN));
                fila.getChildren().add(val);
            }
            filas.getChildren().add(fila);
        }

        Button btnCerrar = new Button("✕");
        btnCerrar.setStyle("-fx-background-color:transparent;-fx-text-fill:#9ca3af;"
                + "-fx-cursor:hand;-fx-font-size:10px;-fx-padding:0;");

        Region esp = new Region();
        HBox.setHgrow(esp, Priority.ALWAYS);
        HBox headRow = new HBox(4, lblNombre, esp, btnCerrar);
        headRow.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(4, headRow, lblTipo, sepLine, filas);
        card.setPadding(new Insets(8, 12, 8, 12));
        card.setStyle("-fx-background-color:rgba(255,255,255,0.60);"
                + "-fx-background-radius:10;-fx-border-radius:10;"
                + "-fx-border-color:" + accentColor + ";-fx-border-width:2;"
                + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.15),8,0,0,2);");

        // ── CLAVE: forzar tamaño al contenido real ──────────────
        card.setMinWidth(Region.USE_PREF_SIZE);
        card.setMaxWidth(Region.USE_PREF_SIZE);
        card.setMinHeight(Region.USE_PREF_SIZE);
        card.setMaxHeight(Region.USE_PREF_SIZE);

        double marTop = Math.max(4, screenY - 40);
        double marLeft = Math.max(4, screenX + 16);

        StackPane.setAlignment(card, Pos.TOP_LEFT);
        StackPane.setMargin(card, new Insets(marTop, 0, 0, marLeft));
        rootStack.getChildren().add(card);
        flotantes.add(new Flotante(card, new GeoPosition(geoLat, geoLng)));

        btnCerrar.setOnAction(ev -> {
            rootStack.getChildren().remove(card);
            flotantes.removeIf(f -> f.card == card);
        });
        FadeTransition ft = new FadeTransition(Duration.millis(150), card);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    private void cerrarTodosFlotantes() {
        new ArrayList<>(flotantes).forEach(f
                -> rootStack.getChildren().remove(f.card));
        flotantes.clear();
    }

    // ═══════════════════════════════════════════════════════════════
    // PANEL LATERAL DERECHO (colapsable)
    // ═══════════════════════════════════════════════════════════════
    private VBox buildPanelDerecho() {
        Label titulo = new Label("Operaciones");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        titulo.setTextFill(Color.web(TEXT_MAIN));

        Label sub = new Label("Filtros y elementos activos");
        sub.setFont(Font.font("Arial", 11));
        sub.setTextFill(Color.web(GRAY_TEXT));

        javafx.scene.image.ImageView iconoImg = new javafx.scene.image.ImageView();
        try {
            java.awt.image.BufferedImage raw = javax.imageio.ImageIO.read(
                    getClass().getResourceAsStream("/PinOperaciones3.png"));
            iconoImg.setImage(javafx.embed.swing.SwingFXUtils.toFXImage(
                    recortarTransparencia(raw), null));
        } catch (Exception ignored) {
        }
        iconoImg.setFitHeight(22);
        iconoImg.setFitWidth(22);
        iconoImg.setPreserveRatio(true);

        StackPane icono = new StackPane(iconoImg);
        icono.setStyle("-fx-background-color:" + DARK_GRAD
                + ";-fx-background-radius:8;-fx-padding:6 10 6 10;");

        HBox header = new HBox(12, icono, new VBox(2, titulo, sub));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(16, 20, 16, 20));
        header.setStyle("-fx-background-color:white;"
                + "-fx-border-color:" + BORDER + ";-fx-border-width:0 0 1 0;");

        // Filtros
        VBox filtros = buildFiltros();

        // Contador
        lblContador = new Label();
        actualizarContadores();
        lblContador.setFont(Font.font("Arial", 11));
        lblContador.setTextFill(Color.web(GRAY_TEXT));
        lblContador.setPadding(new Insets(8, 18, 4, 18));

        // Reemplazar la sección de acordeones en buildPanelDerecho()
        VBox acordeonAlarmas = buildPillColapsable(
                null, "Alarmas", alarmas.size(),
                FX_ALARMA, toHexLight(FX_ALARMA), this::buildListaAlarmas);

        VBox acordeonUnidades = buildPillColapsable(
                null, "Unidades", unidades.size(),
                FX_UNIDAD, toHexLight(FX_UNIDAD), this::buildListaUnidades);

        VBox acordeonAlertas = buildPillColapsable(
                null, "Alertas", alertas.size(),
                FX_ALERTA, toHexLight(FX_ALERTA), this::buildListaAlertas);

        acordeonAlarmasRef = acordeonAlarmas;
        acordeonUnidadesRef = acordeonUnidades;
        acordeonAlertasRef = acordeonAlertas;

        VBox listaCompleta = new VBox(0, acordeonAlarmas, acordeonUnidades, acordeonAlertas);
        listaCompleta.setStyle("-fx-background-color:white;");

        ScrollPane scroll = new ScrollPane(listaCompleta);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background:white;-fx-background-color:white;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        VBox panel = new VBox(header, filtros, lblContador, scroll);
        panel.setPrefWidth(290);
        panel.setMinWidth(0);
        panel.setStyle("-fx-background-color:white;"
                + "-fx-border-color:" + BORDER + ";-fx-border-width:0 0 0 1;");
        return panel;
    }

    private VBox buildFiltros() {
        // Toggle chips de capas (pequeños, dentro del panel)
        HBox chipA = buildToggleChipSmall(" \uf0f3", "", FX_ALARMA, () -> {
            mostrarAlarmas = !mostrarAlarmas;
            repintar();
        });
        HBox chipU = buildToggleChipSmall(" \uf3ed", "", FX_UNIDAD, () -> {
            mostrarUnidades = !mostrarUnidades;
            repintar();
        });
        HBox chipAl = buildToggleChipSmall(" \uf071", "", FX_ALERTA, () -> {
            mostrarAlertas = !mostrarAlertas;
            repintar();
        });

        HBox chips = new HBox(8, chipA, chipU, chipAl);
        chips.setAlignment(Pos.CENTER_LEFT);

        // Filtros con estilo MapaAlarmasRegistradas
        ComboBox<String> cmbA = mkComboEstilizado("Todos", "ACTIVA", "INACTIVA", "EN_MANTENIMIENTO");
        cmbA.setOnAction(e -> {
            filtroAlarma = cmbA.getValue();
            aplicarFiltros();
            repintar();
        });

        ComboBox<String> cmbU = mkComboEstilizado("Todos", "OPERATIVA", "ACTIVA", "INACTIVA");
        cmbU.setOnAction(e -> {
            filtroUnidad = cmbU.getValue();
            aplicarFiltros();
            repintar();
        });

        ComboBox<String> cmbAl = mkComboEstilizado("Todos", "PENDIENTE", "RECIBIDA",
                "EN_ATENCION", "UNIDAD_ASIGNADA", "RESUELTA", "CANCELADA");
        cmbAl.setOnAction(e -> {
            filtroAlerta = cmbAl.getValue();
            aplicarFiltros();
            repintar();
        });

        Button btnRecargar = new Button("⟳  Recargar");
        btnRecargar.setMaxWidth(Double.MAX_VALUE);
        btnRecargar.setPrefHeight(36);
        btnRecargar.setFont(Font.font("Arial", 12));
        btnRecargar.setStyle("-fx-background-color:white;-fx-text-fill:#374151;"
                + "-fx-border-color:#d1d5db;-fx-border-width:1;"
                + "-fx-cursor:hand;-fx-background-radius:8;-fx-border-radius:8;");
        btnRecargar.setOnAction(e -> {
            cargarDatos();
            repintar();
        });

        VBox box = new VBox(12,
                campoFiltro("Mostrar capas", chips),
                campoFiltro("Estado alarma", cmbA),
                campoFiltro("Estado unidad", cmbU),
                campoFiltro("Estado alerta", cmbAl),
                btnRecargar
        );
        box.setPadding(new Insets(16, 20, 16, 20));
        box.setStyle("-fx-background-color:white;"
                + "-fx-border-color:" + BORDER + ";-fx-border-width:0 0 1 0;");
        return box;
    }

    private VBox campoFiltro(String etiqueta, javafx.scene.Node control) {
        Label lbl = new Label(etiqueta);
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        lbl.setTextFill(Color.web("#374151"));
        return new VBox(6, lbl, control);
    }

    private ComboBox<String> mkComboEstilizado(String... items) {
        ComboBox<String> cb = new ComboBox<>(FXCollections.observableArrayList(items));
        cb.setValue("Todos");
        cb.setMaxWidth(Double.MAX_VALUE);
        cb.setPrefHeight(48);
        cb.setStyle(
                "-fx-background-color:#f7f7f7;"
                + "-fx-text-fill:#4b5563;"
                + "-fx-prompt-text-fill:#9ca3af;"
                + "-fx-border-color:transparent;"
                + "-fx-border-radius:30;-fx-background-radius:30;"
                + "-fx-font-size:14px;");

        cb.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
                setStyle("-fx-background-color:transparent;-fx-text-fill:#4b5563;"
                        + "-fx-font-size:14px;-fx-padding:8 14 8 14;");
                setOnMouseEntered(e -> setStyle(
                        "-fx-background-color:" + DARK_GRAD + ";"
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
                setText(empty || item == null ? cb.getPromptText() : item);
                setStyle(item != null
                        ? "-fx-background-color:#f7f7f7;-fx-text-fill:black;"
                        + "-fx-background-radius:30;-fx-font-size:14px;-fx-padding:4 14 4 14;"
                        : "-fx-background-color:transparent;-fx-text-fill:#9ca3af;-fx-font-size:14px;");
            }
        });
        return cb;
    }

    private HBox buildToggleChipSmall(String ico, String txt, Color color, Runnable onToggle) {
        final boolean[] on = {true};
        String hex = toHex(color);

        // ON: fondo sólido del color, borde mismo color → ícono blanco
        String stOn = "-fx-background-color:" + hex + ";"
                + "-fx-border-color:" + hex + ";"
                + "-fx-border-width:1.5;-fx-border-radius:20;-fx-background-radius:20;"
                + "-fx-cursor:hand;";
        // OFF: gris apagado
        String stOff = "-fx-background-color:#e5e7eb;-fx-border-color:#d1d5db"
                + ";-fx-border-width:1.5;-fx-border-radius:20;-fx-background-radius:20;"
                + "-fx-cursor:hand;";

        // Quitar el espacio delante del unicode — rompe el glyph FA
        String icoClean = ico.trim();

        Label lbl = new Label(icoClean);
        // CRÍTICO: font-weight:900 activa el variant Solid de FA6
        lbl.setStyle("-fx-font-family:'" + FA_FAMILY + "';"
                + "-fx-font-size:13px;"
                + "-fx-font-weight:900;");
        lbl.setTextFill(Color.WHITE);                // blanco siempre en ON

        HBox chip = new HBox(lbl);
        chip.setAlignment(Pos.CENTER);
        chip.setPadding(new Insets(6, 14, 6, 14));
        chip.setStyle(stOn);

        chip.setOnMouseClicked(e -> {
            on[0] = !on[0];
            chip.setStyle(on[0] ? stOn : stOff);
            // OFF → ícono gris oscuro visible sobre fondo gris claro
            lbl.setTextFill(on[0] ? Color.WHITE : Color.web("#9ca3af"));
            onToggle.run();
        });
        return chip;
    }

    // ═══════════════════════════════════════════════════════════════
    // MAPA E INICIALIZACIÓN
    // ═══════════════════════════════════════════════════════════════
    private void inicializarMapa(SwingNode sn) {
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

            @Override
            public void mouseMoved(MouseEvent e) {
                GeoPosition pos = mapa.convertPointToGeoPosition(e.getPoint());
                String c = String.format("%.5f,  %.5f", pos.getLatitude(), pos.getLongitude());
                Platform.runLater(() -> lblCoordFooter.setText("📍  " + c));
            }
        });

        mapa.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                Platform.runLater(MapaOperaciones.this::cerrarTodosFlotantes);
            }
        });
        mapa.setOverlayPainter(this::pintarOverlay);
        sn.setContent(mapa);

        mapa.addMouseWheelListener(ev
                -> Platform.runLater(MapaOperaciones.this::cerrarTodosFlotantes));
        sn.setContent(mapa);
        SwingUtilities.invokeLater(() -> {
            mapa.recenterToAddressLocation();
            mapa.repaint();
        });

        System.out.println("Mapa size = "
                + mapa.getWidth() + " x "
                + mapa.getHeight());

    }

    // ── Click ─────────────────────────────────────────────────────
    private void onMapaClick(MouseEvent e) {
        final int TOL = 22;
        if (mostrarAlarmas) {
            for (Alarma a : alarmas) {
                Point2D pt = toScreen(mapa, a.getLatitud(), a.getLongitud());
                if (Math.hypot(e.getX() - pt.getX(), e.getY() - (pt.getY() - 16)) < TOL) {
                    seleccionado = new GeoPosition(a.getLatitud(), a.getLongitud());
                    String bar = a.getBarrio() != null ? a.getBarrio().getNombre() : "—";
                    String est = a.getEstado() != null ? a.getEstado().name() : "—";
                    double _lat = a.getLatitud(), _lng = a.getLongitud();
                    double _px = e.getX(), _py = e.getY();
                    Platform.runLater(() -> mostrarDetalleEn(
                            a.getNombre(), "Alarma",
                            "Estado: " + est + "\nBarrio: " + bar + "\nRadio: " + (int) a.getRadio_cobertura() + " m",
                            "#fb8c00", _px, _py, _lat, _lng));
                    repintarMapa();
                    return;
                }
            }
        }
        if (mostrarUnidades) {
            for (UnidadPolicial u : unidades) {
                Point2D pt = toScreen(mapa, u.getLatitud(), u.getLongitud());
                if (Math.hypot(e.getX() - pt.getX(), e.getY() - (pt.getY() - 16)) < TOL) {
                    seleccionado = new GeoPosition(u.getLatitud(), u.getLongitud());
                    String bar = u.getBarrio() != null ? u.getBarrio().getNombre() : "—";
                    String est = u.getEstado() != null ? u.getEstado().name() : "—";
                    double _lat2 = u.getLatitud(), _lng2 = u.getLongitud();
                    double _px2 = e.getX(), _py2 = e.getY();
                    Platform.runLater(() -> mostrarDetalleEn(
                            u.getNombre(), "Unidad Policial",
                            "Estado: " + est + "\nBarrio: " + bar,
                            toHex(fxColorUnidad(u.getEstado())), _px2, _py2, _lat2, _lng2));
                    repintarMapa();
                    return;
                }
            }
        }
        if (mostrarAlertas) {
            for (Alerta al : alertas) {
                // ← CAMBIO: leer coords desde Direccion
                double alLat = al.getLatitud();
                double alLng = al.getLongitud();

                if (alLat == 0 && alLng == 0) {
                    continue;
                }

                Point2D pt = toScreen(mapa, alLat, alLng);
                if (Math.hypot(e.getX() - pt.getX(), e.getY() - (pt.getY() - 16)) < TOL) {
                    seleccionado = new GeoPosition(alLat, alLng);
                    String tipo = al.getTipoalerta() != null ? al.getTipoalerta().getNombre() : "—";
                    String bar = al.getBarrio() != null ? al.getBarrio().getNombre() : "—";
                    String est = al.getEstado() != null ? al.getEstado().name() : "—";
                    String desc = al.getDescripcion() != null ? al.getDescripcion() : "";
                    double _px3 = e.getX(), _py3 = e.getY();
                    Platform.runLater(() -> mostrarDetalleEn(
                            tipo, "Alerta — " + est,
                            "Barrio: " + bar + (desc.isBlank() ? "" : "\nDescripción: " + desc),
                            toHex(fxColorAlerta(al.getEstado())), _px3, _py3, alLat, alLng));
                    repintarMapa();
                    return;
                }
            }
        }
        seleccionado = null;
        Platform.runLater(this::ocultarDetalle);
        repintarMapa();
    }

    private void reposicionarFlotantes() {
        if (mapa == null || flotantes.isEmpty()) {
            return;
        }
        for (Flotante f : flotantes) {
            Point2D pt = toScreen(mapa, f.geo.getLatitude(), f.geo.getLongitude());
            double marTop = Math.max(4, pt.getY() - 40);
            double marLeft = Math.max(4, pt.getX() + 16);
            Platform.runLater(()
                    -> StackPane.setMargin(f.card, new Insets(marTop, 0, 0, marLeft)));
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // OVERLAY AWT
    // ═══════════════════════════════════════════════════════════════
    private void pintarOverlay(Graphics2D g, JXMapViewer map, int w, int h) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        // ── ALARMAS → SirenaPin.png ────────────────────────────────
        if (mostrarAlarmas) {
            for (Alarma a : alarmas) {
                if (a.getLatitud() == 0 && a.getLongitud() == 0) {
                    continue;
                }
                Point2D pt = toScreen(map, a.getLatitud(), a.getLongitud());
                int cx = (int) pt.getX(), cy = (int) pt.getY();
                boolean sel = esSel(a.getLatitud(), a.getLongitud());

                // Radio
                if (a.getRadio_cobertura() > 0) {
                    double mpp = mpp(map, a.getLatitud());
                    int rp = (int) (a.getRadio_cobertura() / mpp);
                    g.setColor(new java.awt.Color(251, 140, 0, 30));
                    g.fill(new Ellipse2D.Double(cx - rp, cy - rp, rp * 2, rp * 2));
                    g.setColor(new java.awt.Color(251, 140, 0, 140));
                    g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND,
                            BasicStroke.JOIN_ROUND, 0, new float[]{6, 4}, 0));
                    g.draw(new Ellipse2D.Double(cx - rp, cy - rp, rp * 2, rp * 2));
                }
                pintarPng(g, imgSirena, cx, cy, sel, AWT_ALARMA);
                // Badge estado
                if (a.getEstado() != null) {
                    pintarBadge(g, cx, cy, a.getEstado().name(), AWT_ALARMA);
                }
            }
        }

        // ── UNIDADES → PinPolicia.png ──────────────
        if (mostrarUnidades) {
            for (UnidadPolicial u : unidades) {
                if (u.getLatitud() == 0 && u.getLongitud() == 0) {
                    continue;
                }
                Point2D pt = toScreen(map, u.getLatitud(), u.getLongitud());
                int cx = (int) pt.getX(), cy = (int) pt.getY();
                boolean sel = esSel(u.getLatitud(), u.getLongitud());
                java.awt.Color dotColor = awtColorUnidad(u.getEstado());

                pintarPng(g, imgPolicia, cx, cy, sel, dotColor);

                // ── Dot de estado (círculo de color encima del PNG) ─
                int iw = sel ? 38 : 30;
                int ih = (imgPolicia != null)
                        ? (int) (imgPolicia.getHeight() * (iw / (double) imgPolicia.getWidth()))
                        : iw;
                int dotR = sel ? 7 : 5;
                int dotX = cx + iw / 2 - dotR - 1;
                int dotY = cy - ih + dotR + 1;
                // Borde blanco
                g.setColor(java.awt.Color.WHITE);
                g.fillOval(dotX - 2, dotY - 2, dotR * 2 + 4, dotR * 2 + 4);
                // Color estado
                g.setColor(dotColor);
                g.fillOval(dotX, dotY, dotR * 2, dotR * 2);
            }
        }

        // ── ALERTAS  ────────────────────────
        if (mostrarAlertas) {
            for (Alerta al : alertas) {
                double alLat = al.getLatitud();
                double alLng = al.getLongitud();

                if (alLat == 0 && alLng == 0) {
                    continue;
                }

                Point2D pt = toScreen(map, alLat, alLng);
                int cx = (int) pt.getX(), cy = (int) pt.getY();
                boolean sel = esSel(alLat, alLng);

                // Pin rojo fijo (igual que unidades usa su color fijo)
                pintarPinAlerta(g, cx, cy, AWT_ALERTA, sel);

                // Dot de estado encima (igual que unidades)
                java.awt.Color estadoColor = awtColorAlerta(al.getEstado());
                int r = sel ? 15 : 11;
                int dotR = sel ? 6 : 4;
                int dotX = cx + r - dotR;
                int dotY = cy - r * 2 + dotR;
                g.setColor(java.awt.Color.WHITE);
                g.fillOval(dotX - 2, dotY - 2, dotR * 2 + 4, dotR * 2 + 4);
                g.setColor(estadoColor);
                g.fillOval(dotX, dotY, dotR * 2, dotR * 2);

                // Badge arriba con tipo de alerta (no el estado)
                String tipo = al.getTipoalerta() != null
                        ? al.getTipoalerta().getNombre() : "ALERTA";
                pintarBadge(g, cx, cy, tipo, AWT_ALERTA);
            }
        }
    }

    private void pintarPng(Graphics2D g, java.awt.image.BufferedImage img,
            int cx, int cy, boolean sel,
            java.awt.Color haloColor) {
        int iw = sel ? 38 : 30;
        if (img == null) {

            pintarPinGenerico(g, cx, cy, haloColor, sel);
            return;
        }
        int ih = (int) (img.getHeight() * (iw / (double) img.getWidth()));

        g.setColor(new java.awt.Color(0, 0, 0, 40));
        g.fillOval(cx - iw / 2 + 2, cy - 5, iw - 4, 10);

        g.drawImage(img, cx - iw / 2, cy - ih, iw, ih, null);

        if (sel) {
            g.setColor(new java.awt.Color(haloColor.getRed(), haloColor.getGreen(),
                    haloColor.getBlue(), 100));
            g.setStroke(new BasicStroke(3f));
            g.drawRoundRect(cx - iw / 2 - 4, cy - ih - 4, iw + 8, ih + 8, 10, 10);
        }
    }

    private void pintarPinGenerico(Graphics2D g, int cx, int cy,
            java.awt.Color color, boolean sel) {
        int r = sel ? 13 : 10;
        g.setColor(new java.awt.Color(0, 0, 0, 50));
        g.fillOval(cx - r + 2, cy - r * 2 + 2, r * 2, r * 2);
        g.setColor(color);
        g.fillOval(cx - r, cy - r * 2, r * 2, r * 2);
        int[] xs = {cx - r / 2, cx + r / 2, cx}, ys = {cy - r, cy - r, cy + 4};
        g.fillPolygon(xs, ys, 3);
        g.setColor(java.awt.Color.WHITE);
        g.setStroke(new BasicStroke(sel ? 2f : 1.5f));
        g.drawOval(cx - r, cy - r * 2, r * 2, r * 2);
    }

    private void pintarPinAlerta(Graphics2D g, int cx, int cy,
            java.awt.Color color, boolean sel) {
        int r = sel ? 15 : 11;

        // ── Pulso exterior ────────────────────────────────────────
        g.setColor(new java.awt.Color(color.getRed(), color.getGreen(),
                color.getBlue(), 35));
        int pr = r + 10;
        g.fill(new Ellipse2D.Double(cx - pr, cy - pr * 2, pr * 2, pr * 2));

        // ── Sombra ────────────────────────────────────────────────
        g.setColor(new java.awt.Color(0, 0, 0, 45));
        g.translate(2, 3);
        pintarTeardrop(g, cx, cy, r);
        g.translate(-2, -3);

        // ── Cuerpo teardrop ───────────────────────────────────────
        g.setColor(color);
        pintarTeardrop(g, cx, cy, r);

        // ── Borde blanco ──────────────────────────────────────────
        g.setColor(java.awt.Color.WHITE);
        g.setStroke(new BasicStroke(sel ? 2f : 1.5f,
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        pintarTeardropBorde(g, cx, cy, r);

        // ── Círculo blanco interior ───────────────────────────────
        int ir = r - 4;
        g.setColor(new java.awt.Color(255, 255, 255, 220));
        g.fillOval(cx - ir, cy - r * 2 + (r - ir), ir * 2, ir * 2);

        // ── Símbolo "!" interior ──────────────────────────────────
        g.setColor(color);
        g.setStroke(new BasicStroke(sel ? 2.2f : 1.8f,
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int centroY = cy - r * 2 + (r - ir) + ir; // centro del círculo interior
        int mitad = cy - r * 2 + (r - ir);       // tope del círculo interior
        g.drawLine(cx, mitad + 2, cx, centroY - 3);
        g.fillOval(cx - 2, centroY, 4, 4);
    }

// ── Helpers de forma teardrop ─────────────────────────────────
    private void pintarTeardrop(Graphics2D g, int cx, int cy, int r) {
        GeneralPath p = buildTeardrop(cx, cy, r);
        g.fill(p);
    }

    private void pintarTeardropBorde(Graphics2D g, int cx, int cy, int r) {
        GeneralPath p = buildTeardrop(cx, cy, r);
        g.draw(p);
    }

    private GeneralPath buildTeardrop(int cx, int cy, int r) {
        GeneralPath p = new GeneralPath();
        // Círculo superior
        p.append(new Arc2D.Double(cx - r, cy - r * 2, r * 2, r * 2,
                0, 180, Arc2D.OPEN), true);
        // Lado derecho → punta
        p.curveTo(cx + r, cy - r / 2.0,
                cx + r / 3.0, cy - 1,
                cx, cy + 5);
        // Punta → lado izquierdo
        p.curveTo(cx - r / 3.0, cy - 1,
                cx - r, cy - r / 2.0,
                cx - r, cy - r);
        p.closePath();
        return p;
    }

    private void pintarBadge(Graphics2D g, int cx, int cy,
            String texto, java.awt.Color color) {
        java.awt.Font fnt = new java.awt.Font("Arial", java.awt.Font.BOLD, 8);
        g.setFont(fnt);
        java.awt.FontMetrics fm = g.getFontMetrics();
        int tw = fm.stringWidth(texto);
        int bw = tw + 8, bh = 12;
        int bx = cx - bw / 2, by = cy - 46;
        g.setColor(new java.awt.Color(color.getRed(), color.getGreen(), color.getBlue(), 210));
        g.fillRoundRect(bx, by, bw, bh, 6, 6);
        g.setColor(java.awt.Color.WHITE);
        g.drawString(texto, bx + 4, by + bh - 2);
    }

    // ═══════════════════════════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════════════════════════
    private boolean esSel(double lat, double lng) {
        return seleccionado != null
                && seleccionado.getLatitude() == lat
                && seleccionado.getLongitude() == lng;
    }

    private Point2D toScreen(JXMapViewer map, double lat, double lng) {
        Point2D pt = map.getTileFactory().geoToPixel(new GeoPosition(lat, lng), map.getZoom());
        return new Point2D.Double(
                pt.getX() - map.getViewportBounds().x,
                pt.getY() - map.getViewportBounds().y);
    }

    private double mpp(JXMapViewer map, double lat) {
        return 156543.03392 * Math.cos(Math.toRadians(lat))
                / Math.pow(2, 17 - map.getZoom());
    }

    private void ocultarDetalle() {
        seleccionado = null;
        Platform.runLater(this::cerrarTodosFlotantes);
        repintarMapa();

    }

    private void repintar() {
        actualizarContadores();
        repintarMapa();

    }

    private void repintarMapa() {
        if (mapa != null) {
            SwingUtilities.invokeLater(mapa::repaint);
        }
    }

    private void actualizarContadores() {
        if (lblContador == null) {
            return;
        }
        int t = (mostrarAlarmas ? alarmas.size() : 0)
                + (mostrarUnidades ? unidades.size() : 0)
                + (mostrarAlertas ? alertas.size() : 0);
        lblContador.setText(" Mostrando " + t + " elemento" + (t != 1 ? "s" : ""));
    }

    private void centrarEn(double lat, double lng) {
        if (lat == 0 && lng == 0) {
            return;
        }
        SwingUtilities.invokeLater(() -> {
            mapa.setAddressLocation(new GeoPosition(lat, lng));
            mapa.setZoom(2);
            mapa.repaint();
        });
    }

    // Colores JavaFX
    private Color fxColorUnidad(EstadoUnidadPolicial e) {
        if (e == null) {
            return FX_UNIDAD;
        }
        return switch (e) {
            case OPERATIVA ->
                Color.web("#43a047");
            case ACTIVA ->
                Color.web("#fb8c00");
            case INACTIVA ->
                Color.web("#9e9e9e");
        };
    }

    private Color fxColorAlerta(EstadoAlerta e) {
        if (e == null) {
            return FX_ALERTA;
        }
        return switch (e) {
            case PENDIENTE ->
                Color.web("#fb8c00");
            case RECIBIDA ->
                Color.web("#1565c0");
            case EN_ATENCION ->
                Color.web("#7b1fa2");
            case UNIDAD_ASIGNADA ->
                Color.web("#0288d1");
            case RESUELTA ->
                Color.web("#43a047");
            case CANCELADA ->
                Color.web("#9e9e9e");
        };
    }

    // Colores AWT
    private java.awt.Color awtColorUnidad(EstadoUnidadPolicial e) {
        if (e == null) {
            return new java.awt.Color(21, 101, 192);
        }
        return switch (e) {
            case OPERATIVA ->
                new java.awt.Color(67, 160, 71);
            case ACTIVA ->
                new java.awt.Color(251, 140, 0);
            case INACTIVA ->
                new java.awt.Color(158, 158, 158);
        };
    }

    private java.awt.Color awtColorAlerta(EstadoAlerta e) {
        if (e == null) {
            return AWT_ALERTA;
        }
        return switch (e) {
            case PENDIENTE ->
                new java.awt.Color(251, 140, 0);
            case RECIBIDA ->
                new java.awt.Color(21, 101, 192);
            case EN_ATENCION ->
                new java.awt.Color(123, 31, 162);
            case UNIDAD_ASIGNADA ->
                new java.awt.Color(2, 136, 209);
            case RESUELTA ->
                new java.awt.Color(67, 160, 71);
            case CANCELADA ->
                new java.awt.Color(158, 158, 158);
        };
    }

    // Estilo vidrio
    private String glassStyle() {
        return "-fx-background-color:rgba(255,255,255,0.88);"
                + "-fx-background-radius:12;"
                + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.10),12,0,0,3);";
    }

    private String toHex(Color c) {
        return String.format("#%02x%02x%02x",
                (int) (c.getRed() * 255), (int) (c.getGreen() * 255), (int) (c.getBlue() * 255));
    }

    private String toHexLight(Color c) {
        double f = 0.85;
        int r = (int) (c.getRed() * 255 * (1 - f) + 255 * f);
        int gr = (int) (c.getGreen() * 255 * (1 - f) + 255 * f);
        int b = (int) (c.getBlue() * 255 * (1 - f) + 255 * f);
        return String.format("#%02x%02x%02x", r, gr, b);
    }

    private java.awt.image.BufferedImage recortarTransparencia(java.awt.image.BufferedImage img) {
        if (img == null) {
            return null;
        }

        if (!img.getColorModel().hasAlpha()) {
            return img;
        }

        int w = img.getWidth(), h = img.getHeight();
        int top = h, bottom = -1, left = w, right = -1;
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

        if (bottom < 0 || right < 0 || top > bottom || left > right) {
            System.out.println("Recorte: sin píxeles opacos, devolviendo imagen completa");
            return img;
        }
        return img.getSubimage(left, top, right - left + 1, bottom - top + 1);
    }
}
