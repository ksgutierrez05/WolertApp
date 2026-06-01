/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.view;

import javafx.application.Platform;
import javafx.collections.FXCollections;
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
import sistemagestion.model.Alerta;
import sistemagestion.model.Barrio;
import sistemagestion.service.AlertaService;

import javax.swing.SwingUtilities;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class MapaZonasPeligrosas {

    private static final String C_DARK_GRAD = "linear-gradient(to right, #16283d, #1f3a56)";
    private static final String C_SEPARATOR = "#e5e7eb";
    private static final String C_INPUT_BG  = "#f7f7f7";
    private static final String C_LABEL     = "#374151";

    // ── Servicios ─────────────────────────────────────────────────────────────
    private AlertaService alertaService;

    // ── Modelo de zona: agrupa alertas de un mismo barrio ─────────────────────
    /**
     * Contiene todo lo que necesitamos para pintar y describir
     * un barrio en el mapa, calculado a partir de sus alertas.
     */
    private static class ZonaBarrio {
        final String  nombre;
        final int     totalAlertas;
        final double  centroideLat;   // promedio de latitudes de las alertas
        final double  centroideLng;   // promedio de longitudes de las alertas
        final Map<String, Long> porTipo;  // tipo → cantidad

        ZonaBarrio(String nombre, List<Alerta> alertas) {
            this.nombre       = nombre;
            this.totalAlertas = alertas.size();
            this.centroideLat = alertas.stream()
                    .mapToDouble(Alerta::getLatitud).average().orElse(0);
            this.centroideLng = alertas.stream()
                    .mapToDouble(Alerta::getLongitud).average().orElse(0);
            this.porTipo = alertas.stream()
                    .filter(a -> a.getTipoalerta() != null)
                    .collect(Collectors.groupingBy(
                            a -> a.getTipoalerta().getNombre(), Collectors.counting()));
        }

        /** true si el centroide tiene coordenadas válidas (no 0,0) */
        boolean tieneCoordenadas() {
            return centroideLat != 0 || centroideLng != 0;
        }
    }

    // ── Estado ────────────────────────────────────────────────────────────────
    private JXMapViewer         mapa;
    private List<ZonaBarrio>    zonas        = new ArrayList<>();   // barrios con alertas
    private int                 maxAlertas   = 1;
    private ZonaBarrio          zonaSeleccionada = null;

    // ── Controles ─────────────────────────────────────────────────────────────
    private Label             lblContador;
    private Label             lblDetalleNombre;
    private Label             lblDetalleConteo;
    private Label             lblDetallePeligro;
    private Label             lblDetalleTop;
    private VBox              panelDetalle;
    private ComboBox<String>  cmbFiltroTipo;

    // ─────────────────────────────────────────────────────────────────────────
    public MapaZonasPeligrosas() {
        try { alertaService = new AlertaService(); } catch (SQLException ignored) {}
    }

    public void mostrar() {
        Stage stage = new Stage();
        stage.setTitle("WolertApp — Zonas Peligrosas");

        BorderPane root = new BorderPane();
        root.setTop(buildTopBar());
        root.setCenter(buildCentro());
        root.setStyle("-fx-background-color:white;");

        stage.setScene(new Scene(root, 1150, 700));
        stage.setResizable(true);
        cargarDatos();
        stage.show();
    }

    // ════════════════════════════════════════════════════════════════════════
    // TOP BAR
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

        Label logoTxt = new Label("WolertApp");
        logoTxt.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        logoTxt.setTextFill(Color.web("#111827"));

        Region sep = new Region();
        sep.setStyle("-fx-background-color:" + C_SEPARATOR + ";");
        sep.setPrefSize(1, 20);

        Label titulo = new Label("Zonas Peligrosas por Barrio");
        titulo.setFont(Font.font("Arial", 13));
        titulo.setTextFill(Color.web("#6b7280"));

        HBox leyenda = new HBox(14,
                puntito("#22c55e", "Bajo"),
                puntito("#facc15", "Moderado"),
                puntito("#f97316", "Alto"),
                puntito("#ef4444", "Crítico"),
                puntito("#7f1d1d", "Extremo"));
        leyenda.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        lblContador = new Label("0 zonas");
        lblContador.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        lblContador.setTextFill(Color.WHITE);
        lblContador.setStyle("-fx-background-color:" + C_DARK_GRAD
                + ";-fx-background-radius:20;-fx-padding:4 14 4 14;");

        HBox bar = new HBox(10, logoBox, logoTxt, sep, titulo,
                spacer, leyenda, lblContador);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(12, 20, 12, 20));
        bar.setStyle("-fx-background-color:white;-fx-border-color:" + C_SEPARATOR
                + ";-fx-border-width:0 0 1 0;");
        return bar;
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

        Label hint = new Label("🔥  Haz clic en una zona para ver el detalle");
        hint.setFont(Font.font("Arial", 12));
        hint.setTextFill(Color.web("#374151"));
        hint.setStyle(
                "-fx-background-color:rgba(255,255,255,0.92);"
                + "-fx-background-radius:20;-fx-padding:6 18 6 18;"
                + "-fx-border-color:" + C_SEPARATOR + ";"
                + "-fx-border-radius:20;-fx-border-width:1;"
                + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.10),8,0,0,2);");

        StackPane stack = new StackPane(swingNode, hint);
        StackPane.setAlignment(hint, Pos.BOTTOM_CENTER);
        StackPane.setMargin(hint, new Insets(0, 0, 18, 0));
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
        mapa.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                onMapaClick(e);
            }
        });
        mapa.setOverlayPainter(this::pintarOverlay);
        swingNode.setContent(mapa);
    }

    // ── Detecta la zona más cercana al punto clicado ──────────────────────────
    private void onMapaClick(java.awt.event.MouseEvent e) {
        ZonaBarrio encontrada = null;
        double distMin = Double.MAX_VALUE;

        for (ZonaBarrio zona : zonas) {
            if (!zona.tieneCoordenadas()) continue;
            Point2D pt = mapa.getTileFactory().geoToPixel(
                    new GeoPosition(zona.centroideLat, zona.centroideLng), mapa.getZoom());
            int cx = (int) pt.getX() - mapa.getViewportBounds().x;
            int cy = (int) pt.getY() - mapa.getViewportBounds().y;
            int radio = radioVisual(zona.totalAlertas);
            double dist = Math.hypot(e.getX() - cx, e.getY() - cy);
            if (dist < radio + 10 && dist < distMin) {
                distMin    = dist;
                encontrada = zona;
            }
        }

        final ZonaBarrio sel = encontrada;
        zonaSeleccionada = sel;
        Platform.runLater(() -> mostrarDetalle(sel));
        SwingUtilities.invokeLater(mapa::repaint);
    }

    // ── Overlay: círculo de calor centrado en el centroide de cada barrio ─────
    private void pintarOverlay(Graphics2D g, JXMapViewer map, int w, int h) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING,    RenderingHints.VALUE_RENDER_QUALITY);

        for (ZonaBarrio zona : zonas) {
            if (!zona.tieneCoordenadas()) continue;

            Point2D pt = map.getTileFactory().geoToPixel(
                    new GeoPosition(zona.centroideLat, zona.centroideLng), map.getZoom());
            int cx = (int) pt.getX() - map.getViewportBounds().x;
            int cy = (int) pt.getY() - map.getViewportBounds().y;

            float ratio = Math.min(1f, (float) zona.totalAlertas / maxAlertas);
            java.awt.Color colorZona = colorPeligro(ratio);
            int radio = radioVisual(zona.totalAlertas);
            boolean seleccionado = zona == zonaSeleccionada;

            // Halo exterior (muy transparente)
            int haloR = (int)(radio * 1.6);
            g.setColor(new java.awt.Color(
                    colorZona.getRed(), colorZona.getGreen(), colorZona.getBlue(), 18));
            g.fill(new Ellipse2D.Double(cx - haloR, cy - haloR, haloR * 2, haloR * 2));

            // Círculo intermedio
            int medioR = (int)(radio * 1.2);
            g.setColor(new java.awt.Color(
                    colorZona.getRed(), colorZona.getGreen(), colorZona.getBlue(), 38));
            g.fill(new Ellipse2D.Double(cx - medioR, cy - medioR, medioR * 2, medioR * 2));

            // Círculo principal
            g.setColor(new java.awt.Color(
                    colorZona.getRed(), colorZona.getGreen(), colorZona.getBlue(),
                    seleccionado ? 160 : 110));
            g.fill(new Ellipse2D.Double(cx - radio, cy - radio, radio * 2, radio * 2));

            // Borde
            g.setColor(new java.awt.Color(
                    colorZona.getRed(), colorZona.getGreen(), colorZona.getBlue(),
                    seleccionado ? 255 : 200));
            g.setStroke(new BasicStroke(seleccionado ? 3f : 1.8f,
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(new Ellipse2D.Double(cx - radio, cy - radio, radio * 2, radio * 2));

            // Número de alertas en el centro
            g.setColor(java.awt.Color.WHITE);
            java.awt.Font fnt = new java.awt.Font("Arial",
                    java.awt.Font.BOLD, seleccionado ? 14 : 12);
            g.setFont(fnt);
            java.awt.FontMetrics fm = g.getFontMetrics();
            String num = String.valueOf(zona.totalAlertas);
            g.drawString(num, cx - fm.stringWidth(num) / 2, cy + fm.getAscent() / 2 - 1);

            // Nombre del barrio debajo del círculo
            java.awt.Font fntBarrio = new java.awt.Font("Arial",
                    java.awt.Font.BOLD, seleccionado ? 11 : 9);
            g.setFont(fntBarrio);
            fm = g.getFontMetrics();
            String nombre = zona.nombre.length() > 14
                    ? zona.nombre.substring(0, 13) + "…" : zona.nombre;
            int tw = fm.stringWidth(nombre);
            // fondo semitransparente para legibilidad
            g.setColor(new java.awt.Color(0, 0, 0, 120));
            g.fillRoundRect(cx - tw / 2 - 4, cy + radio + 3,
                    tw + 8, fm.getHeight() + 2, 6, 6);
            g.setColor(java.awt.Color.WHITE);
            g.drawString(nombre, cx - tw / 2, cy + radio + 3 + fm.getAscent());
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // PANEL DERECHO
    // ════════════════════════════════════════════════════════════════════════
    private VBox buildPanelDerecho() {
        VBox panel = new VBox(buildPanelHeader(), buildPanelFiltros(), buildPanelDetalle());
        panel.setPrefWidth(300);
        panel.setMaxWidth(300);
        panel.setStyle("-fx-background-color:white;-fx-border-color:" + C_SEPARATOR
                + ";-fx-border-width:0 0 0 1;");
        return panel;
    }

    private HBox buildPanelHeader() {
        Label titulo = new Label("Zonas peligrosas");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        titulo.setTextFill(Color.web("#111827"));
        Label sub = new Label("Barrios con más alertas activas");
        sub.setFont(Font.font("Arial", 11));
        sub.setTextFill(Color.web("#6b7280"));
        Label icono = new Label("🔥");
        icono.setFont(Font.font(18));
        icono.setStyle("-fx-background-color:" + C_DARK_GRAD
                + ";-fx-background-radius:8;-fx-padding:6 10 6 10;");
        HBox header = new HBox(12, icono, new VBox(2, titulo, sub));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(16, 20, 16, 20));
        header.setStyle("-fx-background-color:white;-fx-border-color:" + C_SEPARATOR
                + ";-fx-border-width:0 0 1 0;");
        return header;
    }

    private VBox buildPanelFiltros() {
        cmbFiltroTipo = new ComboBox<>(FXCollections.observableArrayList(
                "Todos los tipos", "HOMICIDIO", "ROBO", "ACCIDENTE",
                "INCENDIO", "EMERGENCIA_MEDICA", "RUIDO", "PERSONA_SOSPECHOSA"));
        cmbFiltroTipo.setValue("Todos los tipos");
        cmbFiltroTipo.setMaxWidth(Double.MAX_VALUE);
        cmbFiltroTipo.setPrefHeight(44);
        cmbFiltroTipo.setStyle(
                "-fx-background-color:" + C_INPUT_BG + ";"
                + "-fx-border-color:transparent;"
                + "-fx-background-radius:10;-fx-font-size:13px;");
        cmbFiltroTipo.setOnAction(e -> cargarDatos());

        Button btnRecargar = new Button("⟳  Recargar");
        btnRecargar.setMaxWidth(Double.MAX_VALUE);
        btnRecargar.setPrefHeight(36);
        btnRecargar.setFont(Font.font("Arial", 12));
        btnRecargar.setStyle(
                "-fx-background-color:white;-fx-text-fill:#374151;"
                + "-fx-border-color:#d1d5db;-fx-border-width:1;"
                + "-fx-cursor:hand;-fx-background-radius:8;-fx-border-radius:8;");
        btnRecargar.setOnAction(e -> {
            cargarDatos();
            zonaSeleccionada = null;
            mostrarDetalle(null);
        });

        Label lblFiltro = new Label("Filtrar por tipo de alerta");
        lblFiltro.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        lblFiltro.setTextFill(Color.web(C_LABEL));

        VBox filtros = new VBox(8, lblFiltro, cmbFiltroTipo, btnRecargar);
        filtros.setPadding(new Insets(16, 20, 16, 20));
        filtros.setStyle("-fx-background-color:white;-fx-border-color:" + C_SEPARATOR
                + ";-fx-border-width:0 0 1 0;");
        return filtros;
    }

    private ScrollPane buildPanelDetalle() {
        lblDetalleNombre  = detalleLbl("—");
        lblDetalleConteo  = detalleLbl("—");
        lblDetallePeligro = detalleLbl("—");
        lblDetalleTop     = detalleLbl("—");
        lblDetalleTop.setWrapText(true);

        panelDetalle = new VBox(10,
                new Label("Haz clic en una zona del mapa") {{
                    setFont(Font.font("Arial", 12));
                    setTextFill(Color.web("#9ca3af"));
                    setWrapText(true);
                }},
                grupoDetalle("Barrio",           lblDetalleNombre),
                grupoDetalle("Total alertas",    lblDetalleConteo),
                grupoDetalle("Nivel peligro",    lblDetallePeligro),
                grupoDetalle("Tipos frecuentes", lblDetalleTop)
        );
        panelDetalle.setPadding(new Insets(16, 20, 16, 20));
        panelDetalle.setStyle("-fx-background-color:white;");

        for (int i = 1; i < panelDetalle.getChildren().size(); i++)
            panelDetalle.getChildren().get(i).setVisible(false);

        ScrollPane scroll = new ScrollPane(panelDetalle);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:white;-fx-background:white;");
        VBox.setVgrow(scroll, Priority.ALWAYS);
        return scroll;
    }

    private Label detalleLbl(String txt) {
        Label l = new Label(txt);
        l.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        l.setTextFill(Color.web("#111827"));
        l.setWrapText(true);
        return l;
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

    /**
     * Carga todas las alertas, las agrupa por barrio y calcula el centroide
     * de cada grupo a partir de las coordenadas de las propias alertas.
     * Barrios sin coordenadas (lat == 0 && lng == 0 en TODAS sus alertas)
     * no se pintan en el mapa.
     */
    private void cargarDatos() {
        try {
            List<Alerta> alertas = alertaService.listar();

            // 1. Filtrar por tipo si aplica
            String filtroTipo = cmbFiltroTipo != null
                    ? cmbFiltroTipo.getValue() : "Todos los tipos";
            if (!"Todos los tipos".equals(filtroTipo)) {
                alertas = alertas.stream()
                        .filter(a -> a.getTipoalerta() != null
                                && filtroTipo.equalsIgnoreCase(
                                        a.getTipoalerta().getNombre()))
                        .collect(Collectors.toList());
            }

            // 2. Descartar alertas sin barrio asignado
            alertas = alertas.stream()
                    .filter(a -> a.getBarrio() != null)
                    .collect(Collectors.toList());

            // 3. Agrupar por nombre de barrio y construir ZonaBarrio
            Map<String, List<Alerta>> porBarrio = alertas.stream()
                    .collect(Collectors.groupingBy(a -> a.getBarrio().getNombre()));

            zonas = porBarrio.entrySet().stream()
                    .map(e -> new ZonaBarrio(e.getKey(), e.getValue()))
                    // Solo zonas donde al menos UNA alerta tiene coordenadas reales
                    .filter(ZonaBarrio::tieneCoordenadas)
                    // Ordenar de más a menos peligroso
                    .sorted(Comparator.comparingInt(
                            (ZonaBarrio z) -> z.totalAlertas).reversed())
                    .collect(Collectors.toList());

            maxAlertas = zonas.stream()
                    .mapToInt(z -> z.totalAlertas).max().orElse(1);

            int total = zonas.size();
            Platform.runLater(() ->
                    lblContador.setText(total + (total == 1 ? " zona" : " zonas")));

            if (mapa != null) SwingUtilities.invokeLater(mapa::repaint);

            // Centrar el mapa en la zona más peligrosa
            if (!zonas.isEmpty()) {
                ZonaBarrio top = zonas.get(0);
                SwingUtilities.invokeLater(() -> {
                    mapa.setAddressLocation(
                            new GeoPosition(top.centroideLat, top.centroideLng));
                    mapa.setZoom(4);
                    mapa.repaint();
                });
            }

        } catch (Exception e) {
            System.out.println("Error cargando zonas: " + e.getMessage());
        }
    }

    private void mostrarDetalle(ZonaBarrio zona) {
        boolean hay = zona != null;
        for (int i = 1; i < panelDetalle.getChildren().size(); i++)
            panelDetalle.getChildren().get(i).setVisible(hay);

        Label hint = (Label) panelDetalle.getChildren().get(0);
        if (!hay) {
            hint.setText("Haz clic en una zona del mapa");
            return;
        }
        hint.setText("Zona seleccionada");

        float ratio = Math.min(1f, (float) zona.totalAlertas / maxAlertas);

        lblDetalleNombre.setText(zona.nombre);
        lblDetalleConteo.setText(zona.totalAlertas
                + " alerta" + (zona.totalAlertas != 1 ? "s" : ""));

        // Nivel de peligro
        String nivel; String colorNivel;
        if      (ratio < 0.25f) { nivel = "🟢 BAJO";     colorNivel = "#22c55e"; }
        else if (ratio < 0.5f)  { nivel = "🟡 MODERADO"; colorNivel = "#ca8a04"; }
        else if (ratio < 0.75f) { nivel = "🟠 ALTO";     colorNivel = "#ea580c"; }
        else if (ratio < 0.9f)  { nivel = "🔴 CRÍTICO";  colorNivel = "#dc2626"; }
        else                    { nivel = "💀 EXTREMO";  colorNivel = "#7f1d1d"; }
        lblDetallePeligro.setText(nivel);
        lblDetallePeligro.setTextFill(Color.web(colorNivel));

        // Top 3 tipos de alerta
        String topTipos = zona.porTipo.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(3)
                .map(e -> e.getKey() + " (" + e.getValue() + ")")
                .collect(Collectors.joining("\n"));
        lblDetalleTop.setText(topTipos.isEmpty() ? "—" : topTipos);
    }

    // ════════════════════════════════════════════════════════════════════════
    // HELPERS VISUALES
    // ════════════════════════════════════════════════════════════════════════

    /** Degradado: verde → amarillo → naranja → rojo → rojo oscuro */
    private java.awt.Color colorPeligro(float ratio) {
        if (ratio < 0.25f)
            return blend(new java.awt.Color(34, 197, 94),  new java.awt.Color(250, 204, 21), ratio / 0.25f);
        if (ratio < 0.5f)
            return blend(new java.awt.Color(250, 204, 21), new java.awt.Color(249, 115, 22), (ratio - 0.25f) / 0.25f);
        if (ratio < 0.75f)
            return blend(new java.awt.Color(249, 115, 22), new java.awt.Color(239, 68, 68),  (ratio - 0.5f)  / 0.25f);
        return     blend(new java.awt.Color(239, 68, 68),  new java.awt.Color(127, 29, 29),  (ratio - 0.75f) / 0.25f);
    }

    private java.awt.Color blend(java.awt.Color a, java.awt.Color b, float t) {
        return new java.awt.Color(
                (int)(a.getRed()   + (b.getRed()   - a.getRed())   * t),
                (int)(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                (int)(a.getBlue()  + (b.getBlue()  - a.getBlue())  * t));
    }

    /** Radio visual proporcional al conteo (mín 28 px, máx 70 px) */
    private int radioVisual(int conteo) {
        if (maxAlertas <= 0) return 28;
        float ratio = Math.min(1f, (float) conteo / maxAlertas);
        return (int)(28 + ratio * 42);
    }

    private java.awt.image.BufferedImage recortarTransparencia(
            java.awt.image.BufferedImage img) {
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