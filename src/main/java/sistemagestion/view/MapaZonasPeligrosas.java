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
import javafx.scene.Node;
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
import sistemagestion.service.AlertaService;

import javax.swing.SwingUtilities;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class MapaZonasPeligrosas {

    private static final String C_DARK_GRAD = "linear-gradient(to right, #16283d, #1f3a56)";
    private static final String C_SEPARATOR = "#e5e7eb";
    private static final String C_INPUT_BG = "#f7f7f7";
    private static final String C_LABEL = "#374151";

    private AlertaService alertaService;

    // ── Modelo interno ────────────────────────────────────────────────────────
    private static class ZonaBarrio {

        final String nombre;
        final int totalAlertas;
        final double centroideLat;
        final double centroideLng;
        final Map<String, Long> porTipo;

        ZonaBarrio(String nombre, List<Alerta> alertas) {
            this.nombre = nombre;
            this.totalAlertas = alertas.size();
            this.centroideLat = alertas.stream().mapToDouble(Alerta::getLatitud).average().orElse(0);
            this.centroideLng = alertas.stream().mapToDouble(Alerta::getLongitud).average().orElse(0);
            this.porTipo = alertas.stream()
                    .filter(a -> a.getTipoalerta() != null)
                    .collect(Collectors.groupingBy(a -> a.getTipoalerta().getNombre(), Collectors.counting()));
        }

        boolean tieneCoordenadas() {
            return centroideLat != 0 || centroideLng != 0;
        }
    }

    private JXMapViewer mapa;
    private List<ZonaBarrio> zonas = new ArrayList<>();
    private int maxAlertas = 1;
    private ZonaBarrio zonaSeleccionada = null;

    private Label lblContador;
    private Label lblDetalleNombre;
    private Label lblDetalleConteo;
    private Label lblDetallePeligro;
    private Label lblDetalleTop;
    private VBox panelDetalle;
    private ComboBox<String> cmbFiltroTipo;

    public MapaZonasPeligrosas() {
        try {
            alertaService = new AlertaService();
        } catch (SQLException ignored) {
        }
    }

    public MapaZonasPeligrosas(AlertaService alertaService) {
        this.alertaService = alertaService;
    }
    
    

    public Node build() {
        BorderPane root = new BorderPane();
        root.setTop(buildTopBar());
        root.setCenter(buildCentro());
        root.setStyle("-fx-background-color:white;");
        cargarDatos();
        return root;
    }

    public void mostrar() {
        Stage stage = new Stage();
        stage.setTitle("WolertApp — Zonas Peligrosas");
        stage.setScene(new Scene((BorderPane) build(), 1150, 700));
        stage.setResizable(true);
        stage.show();
    }

    // ── Top bar ───────────────────────────────────────────────────────────────
    private HBox buildTopBar() {
        javafx.scene.image.ImageView logoImg = new javafx.scene.image.ImageView();
        try {
            java.awt.image.BufferedImage raw = javax.imageio.ImageIO.read(
                    getClass().getResourceAsStream("/LogoWolertApp.png"));
            logoImg.setImage(javafx.embed.swing.SwingFXUtils.toFXImage(recortarTransparencia(raw), null));
        } catch (Exception ignored) {
        }
        logoImg.setFitHeight(24);
        logoImg.setFitWidth(24);
        logoImg.setPreserveRatio(true);

        StackPane logoBox = new StackPane(logoImg);
        logoBox.setStyle("-fx-background-color:" + C_DARK_GRAD + ";-fx-background-radius:6;-fx-padding:6;");

        Label logoTxt = new Label("WolertApp");
        logoTxt.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        logoTxt.setTextFill(Color.web("#111827"));

        Region sep = new Region();
        sep.setStyle("-fx-background-color:" + C_SEPARATOR + ";");
        sep.setPrefSize(1, 20);

        Label titulo = new Label("Zonas Peligrosas por Barrio");
        titulo.setFont(Font.font("Arial", 13));
        titulo.setTextFill(Color.web("#6b7280"));

        HBox leyenda = new HBox(16,
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

        HBox bar = new HBox(10, logoBox, logoTxt, sep, titulo, spacer, leyenda, lblContador);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(12, 20, 12, 20));
        bar.setStyle("-fx-background-color:white;-fx-border-color:" + C_SEPARATOR
                + ";-fx-border-width:0 0 1 0;");
        return bar;
    }

    private HBox puntito(String color, String texto) {
        javafx.scene.shape.Circle c = new javafx.scene.shape.Circle(4);
        c.setFill(Color.web(color));
        Label lbl = new Label(texto);
        lbl.setFont(Font.font("Arial", 11));
        lbl.setTextFill(Color.web("#6b7280"));
        HBox h = new HBox(5, c, lbl);
        h.setAlignment(Pos.CENTER_LEFT);
        return h;
    }

    // ── Centro ────────────────────────────────────────────────────────────────
    private BorderPane buildCentro() {
        BorderPane centro = new BorderPane();
        centro.setCenter(buildMapaStack());
        centro.setRight(buildPanelDerecho());
        return centro;
    }

    private StackPane buildMapaStack() {
        SwingNode swingNode = new SwingNode();
        SwingUtilities.invokeLater(() -> inicializarMapa(swingNode));

        Label hint = new Label("Haz clic en una zona para ver el detalle");
        hint.setFont(Font.font("Arial", 11));
        hint.setTextFill(Color.web("#6b7280"));
        hint.setStyle(
                "-fx-background-color:rgba(255,255,255,0.95);"
                + "-fx-background-radius:20;-fx-padding:5 16 5 16;"
                + "-fx-border-color:#e5e7eb;-fx-border-radius:20;-fx-border-width:1;"
                + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.08),6,0,0,2);");

        StackPane stack = new StackPane(swingNode, hint);
        StackPane.setAlignment(hint, Pos.BOTTOM_CENTER);
        StackPane.setMargin(hint, new Insets(0, 0, 16, 0));
        return stack;
    }

    private void inicializarMapa(SwingNode swingNode) {
        mapa = new JXMapViewer();
        TileFactoryInfo info = new TileFactoryInfo(1, 15, 17, 256, true, true,
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
        mapa.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                onMapaClick(e);
            }
        });
        mapa.setOverlayPainter(this::pintarOverlay);
        swingNode.setContent(mapa);
    }

    private void onMapaClick(java.awt.event.MouseEvent e) {
        ZonaBarrio encontrada = null;
        double distMin = Double.MAX_VALUE;
        for (ZonaBarrio zona : zonas) {
            if (!zona.tieneCoordenadas()) {
                continue;
            }
            Point2D pt = mapa.getTileFactory().geoToPixel(
                    new GeoPosition(zona.centroideLat, zona.centroideLng), mapa.getZoom());
            int cx = (int) pt.getX() - mapa.getViewportBounds().x;
            int cy = (int) pt.getY() - mapa.getViewportBounds().y;
            double dist = Math.hypot(e.getX() - cx, e.getY() - cy);
            if (dist < radioVisual(zona.totalAlertas) + 12 && dist < distMin) {
                distMin = dist;
                encontrada = zona;
            }
        }
        final ZonaBarrio sel = encontrada;
        zonaSeleccionada = sel;
        Platform.runLater(() -> mostrarDetalle(sel));
        SwingUtilities.invokeLater(mapa::repaint);
    }

    // ── Overlay minimalista ───────────────────────────────────────────────────
    private void pintarOverlay(Graphics2D g, JXMapViewer map, int w, int h) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        for (ZonaBarrio zona : zonas) {
            if (!zona.tieneCoordenadas()) {
                continue;
            }

            Point2D pt = map.getTileFactory().geoToPixel(
                    new GeoPosition(zona.centroideLat, zona.centroideLng), map.getZoom());
            int cx = (int) pt.getX() - map.getViewportBounds().x;
            int cy = (int) pt.getY() - map.getViewportBounds().y;

            float ratio = Math.min(1f, (float) zona.totalAlertas / maxAlertas);
            java.awt.Color col = colorPeligro(ratio);
            int radio = radioVisual(zona.totalAlertas);
            boolean sel = zona == zonaSeleccionada;

            // ── 1. Relleno translúcido
            g.setColor(new java.awt.Color(col.getRed(), col.getGreen(), col.getBlue(), sel ? 70 : 50));
            g.fill(new Ellipse2D.Double(cx - radio, cy - radio, radio * 2, radio * 2));

            // ── 2. Borde del círculo
            g.setColor(new java.awt.Color(col.getRed(), col.getGreen(), col.getBlue(), sel ? 255 : 210));
            g.setStroke(new BasicStroke(sel ? 3f : 2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(new Ellipse2D.Double(cx - radio, cy - radio, radio * 2, radio * 2));

            // ── 3. Punto central: aro blanco + relleno color
            int pr = 6;
            g.setColor(java.awt.Color.WHITE);
            g.fill(new Ellipse2D.Double(cx - pr - 2, cy - pr - 2, (pr + 2) * 2, (pr + 2) * 2));
            g.setColor(new java.awt.Color(col.getRed(), col.getGreen(), col.getBlue(), 230));
            g.fill(new Ellipse2D.Double(cx - pr, cy - pr, pr * 2, pr * 2));

            // ── 4. Textos dentro del círculo
            java.awt.Font fntNombre = new java.awt.Font("Arial", java.awt.Font.BOLD, sel ? 13 : 11);
            java.awt.Font fntRiesgo = new java.awt.Font("Arial", java.awt.Font.BOLD, sel ? 12 : 10);

            g.setFont(fntNombre);
            java.awt.FontMetrics fmN = g.getFontMetrics();
            String nombre = zona.nombre.length() > 14
                    ? zona.nombre.substring(0, 13) + "…" : zona.nombre;

            g.setFont(fntRiesgo);
            java.awt.FontMetrics fmR = g.getFontMetrics();
            String riesgoTxt = "Riesgo: " + zona.totalAlertas;

            // Posición centrada verticalmente: nombre arriba del punto, riesgo debajo
            int lineGap = 3;
            int totalTextH = fmN.getHeight() + lineGap + fmR.getHeight();
            int startY = cy - totalTextH / 2;

            // Sombra del nombre
            g.setFont(fntNombre);
            int tx1 = cx - fmN.stringWidth(nombre) / 2;
            int ty1 = startY + fmN.getAscent();
            g.setColor(new java.awt.Color(0, 0, 0, 80));
            g.drawString(nombre, tx1 + 1, ty1 + 1);
            // Nombre en blanco
            g.setColor(java.awt.Color.BLACK);
            g.drawString(nombre, tx1, ty1);

            // Sombra del riesgo
            g.setFont(fntRiesgo);
            int tx2 = cx - fmR.stringWidth(riesgoTxt) / 2;
            int ty2 = ty1 + fmN.getDescent() + lineGap + fmR.getAscent();
            g.setColor(new java.awt.Color(0, 0, 0, 80));
            g.drawString(riesgoTxt, tx2 + 1, ty2 + 1);
            // "Riesgo: X" en blanco
            g.setColor(java.awt.Color.BLACK);
            g.drawString(riesgoTxt, tx2, ty2);
        }
    }

    // ── Panel derecho ─────────────────────────────────────────────────────────
    private VBox buildPanelDerecho() {
        VBox panel = new VBox(buildPanelHeader(), buildPanelFiltros(), buildPanelDetalle());
        panel.setPrefWidth(290);
        panel.setMaxWidth(290);
        panel.setStyle("-fx-background-color:#fafafa;-fx-border-color:" + C_SEPARATOR
                + ";-fx-border-width:0 0 0 1;");
        return panel;
    }

    private HBox buildPanelHeader() {
        Label titulo = new Label("Zonas peligrosas");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        titulo.setTextFill(Color.web("#111827"));
        Label sub = new Label("Barrios con más alertas activas");
        sub.setFont(Font.font("Arial", 11));
        sub.setTextFill(Color.web("#9ca3af"));
        Label icono = new Label("🔥");
        icono.setFont(Font.font(16));
        icono.setStyle("-fx-background-color:" + C_DARK_GRAD
                + ";-fx-background-radius:8;-fx-padding:7 10 7 10;");
        HBox header = new HBox(12, icono, new VBox(2, titulo, sub));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(16, 18, 14, 18));
        header.setStyle("-fx-background-color:#fafafa;-fx-border-color:" + C_SEPARATOR
                + ";-fx-border-width:0 0 1 0;");
        return header;
    }

    private VBox buildPanelFiltros() {
        cmbFiltroTipo = new ComboBox<>(FXCollections.observableArrayList(
                "Todos los tipos", "HOMICIDIO", "ROBO", "ACCIDENTE",
                "INCENDIO", "EMERGENCIA_MEDICA", "RUIDO", "PERSONA_SOSPECHOSA"));
        cmbFiltroTipo.setValue("Todos los tipos");
        cmbFiltroTipo.setMaxWidth(Double.MAX_VALUE);
        cmbFiltroTipo.setPrefHeight(38);
        cmbFiltroTipo.setStyle(
                "-fx-background-color:white;-fx-border-color:#e5e7eb;"
                + "-fx-border-width:1;-fx-border-radius:8;-fx-background-radius:8;"
                + "-fx-font-size:12px;");
        cmbFiltroTipo.setOnAction(e -> cargarDatos());

        Button btnRecargar = new Button("⟳  Recargar");
        btnRecargar.setMaxWidth(Double.MAX_VALUE);
        btnRecargar.setPrefHeight(34);
        btnRecargar.setFont(Font.font("Arial", 12));
        btnRecargar.setStyle(
                "-fx-background-color:white;-fx-text-fill:#6b7280;"
                + "-fx-border-color:#e5e7eb;-fx-border-width:1;"
                + "-fx-cursor:hand;-fx-background-radius:8;-fx-border-radius:8;");
        btnRecargar.setOnMouseEntered(e -> btnRecargar.setStyle(
                "-fx-background-color:#f3f4f6;-fx-text-fill:#374151;"
                + "-fx-border-color:#d1d5db;-fx-border-width:1;"
                + "-fx-cursor:hand;-fx-background-radius:8;-fx-border-radius:8;"));
        btnRecargar.setOnMouseExited(e -> btnRecargar.setStyle(
                "-fx-background-color:white;-fx-text-fill:#6b7280;"
                + "-fx-border-color:#e5e7eb;-fx-border-width:1;"
                + "-fx-cursor:hand;-fx-background-radius:8;-fx-border-radius:8;"));
        btnRecargar.setOnAction(e -> {
            cargarDatos();
            zonaSeleccionada = null;
            mostrarDetalle(null);
        });

        Label lblFiltro = new Label("Tipo de alerta");
        lblFiltro.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        lblFiltro.setTextFill(Color.web("#9ca3af"));

        VBox filtros = new VBox(8, lblFiltro, cmbFiltroTipo, btnRecargar);
        filtros.setPadding(new Insets(14, 18, 14, 18));
        filtros.setStyle("-fx-background-color:#fafafa;-fx-border-color:" + C_SEPARATOR
                + ";-fx-border-width:0 0 1 0;");
        return filtros;
    }

    private ScrollPane buildPanelDetalle() {
        lblDetalleNombre = detalleLbl("—");
        lblDetalleConteo = detalleLbl("—");
        lblDetallePeligro = detalleLbl("—");
        lblDetalleTop = detalleLbl("—");
        lblDetalleTop.setWrapText(true);

        Label hint = new Label("Selecciona una zona en el mapa");
        hint.setFont(Font.font("Arial", 12));
        hint.setTextFill(Color.web("#9ca3af"));
        hint.setWrapText(true);

        panelDetalle = new VBox(10, hint,
                grupoDetalle("Barrio", lblDetalleNombre),
                grupoDetalle("Total alertas", lblDetalleConteo),
                grupoDetalle("Nivel de peligro", lblDetallePeligro),
                grupoDetalle("Tipos frecuentes", lblDetalleTop));
        panelDetalle.setPadding(new Insets(14, 18, 14, 18));
        panelDetalle.setStyle("-fx-background-color:#fafafa;");

        for (int i = 1; i < panelDetalle.getChildren().size(); i++) {
            panelDetalle.getChildren().get(i).setVisible(false);
        }

        ScrollPane scroll = new ScrollPane(panelDetalle);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:#fafafa;-fx-background:#fafafa;");
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
        lbl.setFont(Font.font("Arial", 10));
        lbl.setTextFill(Color.web("#9ca3af"));
        VBox vb = new VBox(3, lbl, valor);
        vb.setStyle("-fx-background-color:white;-fx-background-radius:10;"
                + "-fx-padding:10 12 10 12;"
                + "-fx-border-color:#f3f4f6;-fx-border-width:1;-fx-border-radius:10;");
        return vb;
    }

    // ── Lógica ────────────────────────────────────────────────────────────────
    private void cargarDatos() {
        try {
            List<Alerta> alertas = alertaService.listar();

            String filtroTipo = cmbFiltroTipo != null ? cmbFiltroTipo.getValue() : "Todos los tipos";
            if (!"Todos los tipos".equals(filtroTipo)) {
                alertas = alertas.stream()
                        .filter(a -> a.getTipoalerta() != null
                        && filtroTipo.equalsIgnoreCase(a.getTipoalerta().getNombre()))
                        .collect(Collectors.toList());
            }

            alertas = alertas.stream().filter(a -> a.getBarrio() != null).collect(Collectors.toList());

            Map<String, List<Alerta>> porBarrio = alertas.stream()
                    .collect(Collectors.groupingBy(a -> a.getBarrio().getNombre()));

            zonas = porBarrio.entrySet().stream()
                    .map(e -> new ZonaBarrio(e.getKey(), e.getValue()))
                    .filter(ZonaBarrio::tieneCoordenadas)
                    .sorted(Comparator.comparingInt((ZonaBarrio z) -> z.totalAlertas).reversed())
                    .collect(Collectors.toList());

            maxAlertas = zonas.stream().mapToInt(z -> z.totalAlertas).max().orElse(1);

            int total = zonas.size();
            Platform.runLater(() -> lblContador.setText(total + (total == 1 ? " zona" : " zonas")));
            if (mapa != null) {
                SwingUtilities.invokeLater(mapa::repaint);
            }

            if (!zonas.isEmpty()) {
                ZonaBarrio top = zonas.get(0);
                SwingUtilities.invokeLater(() -> {
                    mapa.setAddressLocation(new GeoPosition(top.centroideLat, top.centroideLng));
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
        for (int i = 1; i < panelDetalle.getChildren().size(); i++) {
            panelDetalle.getChildren().get(i).setVisible(hay);
        }

        Label hint = (Label) panelDetalle.getChildren().get(0);
        if (!hay) {
            hint.setText("Selecciona una zona en el mapa");
            return;
        }
        hint.setText("Zona seleccionada");

        float ratio = Math.min(1f, (float) zona.totalAlertas / maxAlertas);
        lblDetalleNombre.setText(zona.nombre);
        lblDetalleConteo.setText(zona.totalAlertas + " alerta" + (zona.totalAlertas != 1 ? "s" : ""));

        String nivel;
        String colorNivel;
        if (ratio < 0.25f) {
            nivel = "● BAJO";
            colorNivel = "#16a34a";
        } else if (ratio < 0.5f) {
            nivel = "● MODERADO";
            colorNivel = "#ca8a04";
        } else if (ratio < 0.75f) {
            nivel = "● ALTO";
            colorNivel = "#ea580c";
        } else if (ratio < 0.9f) {
            nivel = "● CRÍTICO";
            colorNivel = "#dc2626";
        } else {
            nivel = "● EXTREMO";
            colorNivel = "#7f1d1d";
        }
        lblDetallePeligro.setText(nivel);
        lblDetallePeligro.setTextFill(Color.web(colorNivel));

        String topTipos = zona.porTipo.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(3)
                .map(e -> e.getKey() + " (" + e.getValue() + ")")
                .collect(Collectors.joining("\n"));
        lblDetalleTop.setText(topTipos.isEmpty() ? "—" : topTipos);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private java.awt.Color colorPeligro(float ratio) {
        if (ratio < 0.25f) {
            return blend(new java.awt.Color(34, 197, 94), new java.awt.Color(250, 204, 21), ratio / 0.25f);
        }
        if (ratio < 0.5f) {
            return blend(new java.awt.Color(250, 204, 21), new java.awt.Color(249, 115, 22), (ratio - 0.25f) / 0.25f);
        }
        if (ratio < 0.75f) {
            return blend(new java.awt.Color(249, 115, 22), new java.awt.Color(239, 68, 68), (ratio - 0.5f) / 0.25f);
        }
        return blend(new java.awt.Color(239, 68, 68), new java.awt.Color(127, 29, 29), (ratio - 0.75f) / 0.25f);
    }

    private java.awt.Color blend(java.awt.Color a, java.awt.Color b, float t) {
        return new java.awt.Color(
                (int) (a.getRed() + (b.getRed() - a.getRed()) * t),
                (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t));
    }

   private int radioVisual(int conteo) {
    if (maxAlertas <= 0) return 42;
    return (int)(42 + Math.min(1f, (float) conteo / maxAlertas) * 38);
}

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
