/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.view;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
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
import org.jxmapviewer.viewer.*;
import sistemagestion.model.Alerta;
import sistemagestion.service.AlertaService;

import javax.swing.SwingUtilities;

import java.awt.geom.*;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import javafx.scene.shape.Circle;

public class MapaAlertas {

    // ── Paleta ────────────────────────────────────────────────────────────────
    private static final String DARK_GRAD = "linear-gradient(to right,#16283d,#1f3a56)";
    private static final String BORDER = "#e5e7eb";
    private static final String GRAY_TEXT = "#6b7280";
    private static final String BG_PANEL = "#fafafa";

    // ── Servicios y datos ─────────────────────────────────────────────────────
    private AlertaService alertaService;
    private List<Alerta> todasAlertas = new ArrayList<>();
    private List<Alerta> alertasFiltradas = new ArrayList<>();
    private Alerta alertaSel = null;

    // ── Mapa ──────────────────────────────────────────────────────────────────
    private JXMapViewer mapa;

    // ── Filtros ───────────────────────────────────────────────────────────────
    private ComboBox<String> cmbTipo;
    private ComboBox<String> cmbEstado;
    private ComboBox<String> cmbBarrio;

    // ── Panel detalle ─────────────────────────────────────────────────────────
    private Label lblContador;
    private Label lblDetTipo, lblDetEstado, lblDetBarrio,
            lblDetDesc, lblDetFecha, lblDetUsuario;
    private VBox panelDetalle;

    // ─────────────────────────────────────────────────────────────────────────
    public MapaAlertas() {
        try {
            alertaService = new AlertaService();
        } catch (SQLException ignored) {
        }
    }

    public MapaAlertas(AlertaService alertaService) {
        this.alertaService = alertaService;
    }

    // ─────────────────────────────────────────────────────────────────────────
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
        stage.setTitle("WolertApp — Mapa de Alertas");
        stage.setScene(new Scene((BorderPane) build(), 1150, 700));
        stage.setResizable(true);
        stage.show();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // TOP BAR
    // ══════════════════════════════════════════════════════════════════════════
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
        logoBox.setStyle("-fx-background-color:" + DARK_GRAD + ";-fx-background-radius:6;-fx-padding:6;");

        Label logoTxt = new Label("WolertApp");
        logoTxt.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        logoTxt.setTextFill(Color.web("#111827"));

        Region sep = new Region();
        sep.setStyle("-fx-background-color:" + BORDER + ";");
        sep.setPrefSize(1, 20);

        Label titulo = new Label("Mapa de Alertas");
        titulo.setFont(Font.font("Arial", 13));
        titulo.setTextFill(Color.web(GRAY_TEXT));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        lblContador = new Label("0 alertas");
        lblContador.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        lblContador.setTextFill(Color.WHITE);
        lblContador.setStyle("-fx-background-color:" + DARK_GRAD
                + ";-fx-background-radius:20;-fx-padding:4 14 4 14;");

        HBox bar = new HBox(10, logoBox, logoTxt, sep, titulo, spacer, lblContador);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(12, 20, 12, 20));
        bar.setStyle("-fx-background-color:white;-fx-border-color:" + BORDER
                + ";-fx-border-width:0 0 1 0;");
        return bar;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // CENTRO: mapa + panel derecho
    // ══════════════════════════════════════════════════════════════════════════
    private BorderPane buildCentro() {
        BorderPane centro = new BorderPane();
        centro.setCenter(buildMapaStack());
        centro.setRight(buildPanelDerecho());
        return centro;
    }

    private StackPane buildMapaStack() {
        SwingNode swingNode = new SwingNode();
        SwingUtilities.invokeLater(() -> inicializarMapa(swingNode));

        // ── Leyenda flotante abajo-izquierda ──────────────────────────
        VBox leyenda = buildLeyendaTipos();

        Label hint = new Label("Haz clic en un pin para ver el detalle");
        hint.setFont(Font.font("Arial", 11));
        hint.setTextFill(Color.web(GRAY_TEXT));
        hint.setStyle(
                "-fx-background-color:rgba(255,255,255,0.95);"
                + "-fx-background-radius:20;-fx-padding:5 16 5 16;"
                + "-fx-border-color:#e5e7eb;-fx-border-radius:20;-fx-border-width:1;");

        StackPane stack = new StackPane(swingNode, leyenda, hint);

        StackPane.setAlignment(leyenda, Pos.BOTTOM_LEFT);
        StackPane.setMargin(leyenda, new Insets(0, 0, 50, 12));

        StackPane.setAlignment(hint, Pos.BOTTOM_CENTER);
        StackPane.setMargin(hint, new Insets(0, 0, 16, 0));
        return stack;
    }

    private VBox buildLeyendaTipos() {
        String[][] items = {
            {"#E67E22", "Robo / Asalto"},
            {"#1A2332", "Homicidio"},
            {"#0EA5E9", "Sospechoso"},
            {"#E53935", "Incendio"},
            {"#1D4ED8", "Emergencia médica"},
            {"#7C3AED", "Accidente"},
            {"#16A34A", "Ruido / Alteración"}
        };

        VBox lista = new VBox(4);
        for (String[] item : items) {
            Circle dot = new Circle(4);
            dot.setFill(Color.web(item[0]));
            Label lbl = new Label(item[1]);
            lbl.setFont(Font.font("Arial", 10));
            lbl.setTextFill(Color.web(GRAY_TEXT));
            HBox cell = new HBox(6, dot, lbl);
            cell.setAlignment(Pos.CENTER_LEFT);
            lista.getChildren().add(cell);
        }

        Label tit = new Label("TIPOS DE ALERTA");
        tit.setFont(Font.font("Arial", FontWeight.BOLD, 9));
        tit.setTextFill(Color.web(GRAY_TEXT));

        VBox box = new VBox(5, tit, lista);
        box.setPadding(new Insets(8, 10, 8, 10));
        // ── CLAVE: tamaño al contenido, no se expande ─────────────────
        box.setMinWidth(130);
        box.setMaxWidth(Region.USE_PREF_SIZE);
        box.setMaxHeight(Region.USE_PREF_SIZE);
        box.setStyle(
                "-fx-background-color:rgba(255,255,255,0.88);"
                + "-fx-background-radius:10;"
                + "-fx-border-radius:10;"
                + "-fx-border-color:rgba(229,57,53,0.20);"
                + "-fx-border-width:1;"
                + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.10),10,0,0,2);");
        return box;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // MAPA
    // ══════════════════════════════════════════════════════════════════════════
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
        mapa.setOverlayPainter(this::pintarPines);
        swingNode.setContent(mapa);
    }

    private void onMapaClick(java.awt.event.MouseEvent e) {
        Alerta encontrada = null;
        double distMin = Double.MAX_VALUE;

        for (Alerta a : alertasFiltradas) {
            if (a.getLatitud() == 0 && a.getLongitud() == 0) {
                continue;
            }
            Point2D pt = mapa.getTileFactory().geoToPixel(
                    new GeoPosition(a.getLatitud(), a.getLongitud()), mapa.getZoom());
            int px = (int) pt.getX() - mapa.getViewportBounds().x;
            int py = (int) pt.getY() - mapa.getViewportBounds().y;
            double dist = Math.hypot(e.getX() - px, e.getY() - py);
            if (dist < 22 && dist < distMin) {
                distMin = dist;
                encontrada = a;
            }
        }

        final Alerta sel = encontrada;
        alertaSel = sel;
        Platform.runLater(() -> mostrarDetalle(sel));
        SwingUtilities.invokeLater(mapa::repaint);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // OVERLAY — pines individuales por alerta
    // ══════════════════════════════════════════════════════════════════════════
    private void pintarPines(Graphics2D g, JXMapViewer map, int w, int h) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        for (Alerta a : alertasFiltradas) {
            if (a.getLatitud() == 0 && a.getLongitud() == 0) {
                continue;
            }

            Point2D pt = map.getTileFactory().geoToPixel(
                    new GeoPosition(a.getLatitud(), a.getLongitud()), map.getZoom());
            int cx = (int) pt.getX() - map.getViewportBounds().x;
            int cy = (int) pt.getY() - map.getViewportBounds().y;

            java.awt.Color col = colorParaTipo(a);
            boolean sel = a == alertaSel;

            // ── Sombra del pin ────────────────────────────────────────────
            g.setColor(new java.awt.Color(0, 0, 0, sel ? 50 : 30));
            g.fill(new Ellipse2D.Double(cx - 10, cy + 2, 20, 7));

            // ── Cuerpo del pin (teardrop) ──────────────────────────────────
            int pinR = sel ? 14 : 11;   // radio cabeza
            int pinH = sel ? 22 : 17;   // altura punta

            // Cabeza circular
            g.setColor(col);
            g.fill(new Ellipse2D.Double(cx - pinR, cy - pinH - pinR, pinR * 2, pinR * 2));

            // Punta triangular
            int[] xs = {cx - pinR / 2, cx + pinR / 2, cx};
            int[] ys = {cy - pinH + 4, cy - pinH + 4, cy};
            g.fillPolygon(xs, ys, 3);

            // ── Borde del pin ─────────────────────────────────────────────
            java.awt.Color borde = sel
                    ? new java.awt.Color(255, 255, 255, 220)
                    : new java.awt.Color(0, 0, 0, 40);
            g.setColor(borde);
            g.setStroke(new BasicStroke(sel ? 2f : 1f));
            g.draw(new Ellipse2D.Double(cx - pinR, cy - pinH - pinR, pinR * 2, pinR * 2));

            // ── Punto blanco central ──────────────────────────────────────
            int pr = sel ? 5 : 4;
            g.setColor(java.awt.Color.WHITE);
            g.fill(new Ellipse2D.Double(
                    cx - pr, cy - pinH - pinR + (pinR - pr), pr * 2, pr * 2));

            // ── Etiqueta tipo (solo si seleccionado o zoom cercano) ────────
            if (sel) {
                String tipo = a.getTipoalerta() != null
                        ? abrevTipo(a.getTipoalerta().getNombre()) : "?";
                java.awt.Font fnt = new java.awt.Font("Arial", java.awt.Font.BOLD, 10);
                g.setFont(fnt);
                java.awt.FontMetrics fm = g.getFontMetrics();
                int tw = fm.stringWidth(tipo);
                int padX = 7, padY = 4;
                int bw = tw + padX * 2, bh = fm.getHeight() + padY * 2;
                int bx = cx - bw / 2;
                int by = cy - pinH - pinR * 2 - bh - 4;

                // Sombra chip
                g.setColor(new java.awt.Color(0, 0, 0, 20));
                g.fillRoundRect(bx + 1, by + 2, bw, bh, 8, 8);

                // Fondo chip blanco
                g.setColor(new java.awt.Color(255, 255, 255, 240));
                g.fillRoundRect(bx, by, bw, bh, 8, 8);

                // Borde chip color tipo
                g.setColor(new java.awt.Color(col.getRed(), col.getGreen(), col.getBlue(), 200));
                g.setStroke(new BasicStroke(1.5f));
                g.drawRoundRect(bx, by, bw, bh, 8, 8);

                // Texto chip
                g.setColor(new java.awt.Color(20, 20, 20, 230));
                g.drawString(tipo, bx + padX, by + padY + fm.getAscent());
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // PANEL DERECHO
    // ══════════════════════════════════════════════════════════════════════════
    private VBox buildPanelDerecho() {
        VBox panel = new VBox(buildPanelHeader(), buildPanelFiltros(), buildPanelDetalle());
        panel.setPrefWidth(300);
        panel.setMaxWidth(300);
        panel.setStyle("-fx-background-color:" + BG_PANEL
                + ";-fx-border-color:" + BORDER + ";-fx-border-width:0 0 0 1;");
        return panel;
    }

    private HBox buildPanelHeader() {
        Label titulo = new Label("Alertas registradas");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        titulo.setTextFill(Color.web("#111827"));
        Label sub = new Label("Todas las alertas del sistema");
        sub.setFont(Font.font("Arial", 11));
        sub.setTextFill(Color.web("#9ca3af"));
        javafx.scene.image.ImageView iconoImg = new javafx.scene.image.ImageView();
        try {
            java.awt.image.BufferedImage raw = javax.imageio.ImageIO.read(
                    getClass().getResourceAsStream("/UbicacionPin.png"));
            iconoImg.setImage(javafx.embed.swing.SwingFXUtils.toFXImage(
                    recortarTransparencia(raw), null));
        } catch (Exception ignored) {
        }

        StackPane icono = new StackPane(iconoImg);
        icono.setStyle("-fx-background-color:" + DARK_GRAD
                + ";-fx-background-radius:8;-fx-padding:6 10 6 10;");
        iconoImg.setFitHeight(22);
        iconoImg.setFitWidth(22);
        iconoImg.setPreserveRatio(true);
        HBox h = new HBox(12, icono, new VBox(2, titulo, sub));
        h.setAlignment(Pos.CENTER_LEFT);
        h.setPadding(new Insets(16, 18, 14, 18));
        h.setStyle("-fx-background-color:" + BG_PANEL
                + ";-fx-border-color:" + BORDER + ";-fx-border-width:0 0 1 0;");
        return h;
    }

    private VBox buildPanelFiltros() {
        cmbTipo = mkComboFiltro("Todos los tipos",
                "Todos los tipos", "ROBO", "HOMICIDIO", "PERSONA_SOSPECHOSA",
                "INCENDIO", "EMERGENCIA_MEDICA", "ACCIDENTE", "RUIDO");

        cmbEstado = mkComboFiltro("Todos los estados",
                "Todos los estados", "ACTIVA", "RESUELTA", "FALSA", "EN_PROCESO");

        cmbBarrio = mkComboFiltro("Todos los barrios"); // se llena dinámicamente

        cmbTipo.setOnAction(e -> aplicarFiltros());
        cmbEstado.setOnAction(e -> aplicarFiltros());
        cmbBarrio.setOnAction(e -> aplicarFiltros());

        Button btnRecargar = new Button("⟳  Recargar");
        btnRecargar.setMaxWidth(Double.MAX_VALUE);
        btnRecargar.setPrefHeight(36);
        btnRecargar.setFont(Font.font("Arial", 12));
        String bBase = "-fx-background-color:white;-fx-text-fill:#374151;"
                + "-fx-border-color:#d1d5db;-fx-border-width:1;"
                + "-fx-cursor:hand;-fx-background-radius:8;-fx-border-radius:8;";
        String bHov = "-fx-background-color:#f3f4f6;-fx-text-fill:#111827;"
                + "-fx-border-color:#9ca3af;-fx-border-width:1;"
                + "-fx-cursor:hand;-fx-background-radius:8;-fx-border-radius:8;";
        btnRecargar.setStyle(bBase);
        btnRecargar.setOnMouseEntered(e -> btnRecargar.setStyle(bHov));
        btnRecargar.setOnMouseExited(e -> btnRecargar.setStyle(bBase));
        btnRecargar.setOnAction(e -> {
            alertaSel = null;
            cargarDatos();
            mostrarDetalle(null);
        });

        VBox f = new VBox(10,
                campoFiltro("Tipo de alerta", cmbTipo),
                campoFiltro("Estado", cmbEstado),
                campoFiltro("Barrio", cmbBarrio),
                btnRecargar);
        f.setPadding(new Insets(14, 18, 14, 18));
        f.setStyle("-fx-background-color:" + BG_PANEL
                + ";-fx-border-color:" + BORDER + ";-fx-border-width:0 0 1 0;");
        return f;
    }

    private ComboBox<String> mkComboFiltro(String prompt, String... items) {
        ComboBox<String> cb = new ComboBox<>(
                FXCollections.observableArrayList(items));
        cb.setValue(prompt);
        cb.setMaxWidth(Double.MAX_VALUE);
        cb.setPrefHeight(48);
        cb.setStyle(
                "-fx-background-color:#f7f7f7;"
                + "-fx-text-fill:#4b5563;"
                + "-fx-prompt-text-fill:#9ca3af;"
                + "-fx-border-color:transparent;"
                + "-fx-border-radius:30;-fx-background-radius:30;"
                + "-fx-font-size:13px;");

        cb.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
                setStyle("-fx-background-color:transparent;-fx-text-fill:#4b5563;"
                        + "-fx-font-size:13px;-fx-padding:8 14 8 14;");
                setOnMouseEntered(e -> setStyle(
                        "-fx-background-color:" + DARK_GRAD + ";"
                        + "-fx-background-radius:6;-fx-text-fill:white;"
                        + "-fx-font-size:13px;-fx-padding:8 14 8 14;"));
                setOnMouseExited(e -> setStyle(
                        "-fx-background-color:transparent;-fx-text-fill:#4b5563;"
                        + "-fx-font-size:13px;-fx-padding:8 14 8 14;"));
            }
        });

        cb.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? prompt : item);
                setStyle(item != null
                        ? "-fx-background-color:#f7f7f7;-fx-text-fill:#111827;"
                        + "-fx-background-radius:30;-fx-font-size:13px;-fx-padding:4 14 4 14;"
                        : "-fx-background-color:transparent;-fx-text-fill:#9ca3af;-fx-font-size:13px;");
            }
        });
        return cb;
    }

    private ScrollPane buildPanelDetalle() {
        // Labels
        lblDetTipo = detalleVal("—");
        lblDetEstado = detalleVal("—");
        lblDetBarrio = detalleVal("—");
        lblDetDesc = detalleVal("—");
        lblDetDesc.setWrapText(true);
        lblDetFecha = detalleVal("—");
        lblDetUsuario = detalleVal("—");

        Label hint = new Label("Selecciona un pin en el mapa");
        hint.setFont(Font.font("Arial", 12));
        hint.setTextFill(Color.web("#9ca3af"));
        hint.setWrapText(true);

        panelDetalle = new VBox(10, hint,
                grupoDetalle("Tipo de alerta", lblDetTipo),
                grupoDetalle("Estado", lblDetEstado),
                grupoDetalle("Barrio", lblDetBarrio),
                grupoDetalle("Descripción", lblDetDesc),
                grupoDetalle("Fecha / hora", lblDetFecha),
                grupoDetalle("Reportado por", lblDetUsuario));
        panelDetalle.setPadding(new Insets(14, 18, 14, 18));
        panelDetalle.setStyle("-fx-background-color:" + BG_PANEL + ";");

        // Ocultar grupos hasta que haya selección
        for (int i = 1; i < panelDetalle.getChildren().size(); i++) {
            panelDetalle.getChildren().get(i).setVisible(false);
        }

        ScrollPane scroll = new ScrollPane(panelDetalle);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:" + BG_PANEL + ";-fx-background:" + BG_PANEL + ";");
        VBox.setVgrow(scroll, Priority.ALWAYS);
        return scroll;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // LÓGICA
    // ══════════════════════════════════════════════════════════════════════════
    private void cargarDatos() {
        try {
            todasAlertas = alertaService != null ? alertaService.listar() : new ArrayList<>();

            // Llenar combo barrios
            List<String> barrios = todasAlertas.stream()
                    .filter(a -> a.getBarrio() != null && a.getBarrio().getNombre() != null)
                    .map(a -> a.getBarrio().getNombre())
                    .distinct().sorted().collect(Collectors.toList());
            barrios.add(0, "Todos los barrios");

            Platform.runLater(() -> {
                cmbBarrio.getItems().setAll(barrios);
                cmbBarrio.setValue("Todos los barrios");
            });

            aplicarFiltros();

        } catch (Exception e) {
            System.out.println("Error cargando alertas: " + e.getMessage());
        }
    }

    private void aplicarFiltros() {
        String tipo = cmbTipo != null ? cmbTipo.getValue() : "Todos los tipos";
        String estado = cmbEstado != null ? cmbEstado.getValue() : "Todos los estados";
        String barrio = cmbBarrio != null ? cmbBarrio.getValue() : "Todos los barrios";

        alertasFiltradas = todasAlertas.stream()
                .filter(a -> "Todos los tipos".equals(tipo)
                || (a.getTipoalerta() != null && tipo.equalsIgnoreCase(a.getTipoalerta().getNombre())))
                .filter(a -> "Todos los estados".equals(estado)
                || (a.getEstado() != null && estado.equalsIgnoreCase(a.getEstado().toString())))
                .filter(a -> "Todos los barrios".equals(barrio)
                || (a.getBarrio() != null && barrio.equalsIgnoreCase(a.getBarrio().getNombre())))
                .collect(Collectors.toList());

        int total = alertasFiltradas.size();
        Platform.runLater(()
                -> lblContador.setText(total + (total == 1 ? " alerta" : " alertas")));

        if (mapa != null) {
            SwingUtilities.invokeLater(mapa::repaint);
        }
    }

    private void mostrarDetalle(Alerta a) {
        boolean hay = a != null;
        for (int i = 1; i < panelDetalle.getChildren().size(); i++) {
            panelDetalle.getChildren().get(i).setVisible(hay);
        }

        Label hint = (Label) panelDetalle.getChildren().get(0);
        if (!hay) {
            hint.setText("Selecciona un pin en el mapa");
            return;
        }
        hint.setText("Alerta seleccionada");

        // Tipo
        String tipoNom = a.getTipoalerta() != null ? a.getTipoalerta().getNombre() : "—";
        lblDetTipo.setText(tipoNom);
        lblDetTipo.setTextFill(Color.web(hexParaTipo(tipoNom)));

        // Estado
        String est = a.getEstado() != null ? a.getEstado().toString() : "—";
        lblDetEstado.setText(est);
        lblDetEstado.setStyle("-fx-background-color:" + badgeBg(est)
                + ";-fx-text-fill:" + badgeFg(est)
                + ";-fx-background-radius:20;-fx-padding:3 10;-fx-font-size:11px;-fx-font-weight:bold;");

        // Barrio
        lblDetBarrio.setText(a.getBarrio() != null ? a.getBarrio().getNombre() : "—");
        lblDetBarrio.setTextFill(Color.web("#111827"));

        // Descripción
        String desc = a.getDescripcion() != null && !a.getDescripcion().isBlank()
                ? a.getDescripcion() : "Sin descripción";
        lblDetDesc.setText(desc);
        lblDetDesc.setTextFill(Color.web("#374151"));

        // Fecha
        String fecha = a.getFechaHora() != null ? a.getFechaHora().toString() : "—";
        lblDetFecha.setText(fecha);
        lblDetFecha.setTextFill(Color.web("#6b7280"));

        // Usuario
        String user = a.getUsuario() != null
                ? (a.getUsuario().getUsername() != null
                ? "@" + a.getUsuario().getUsername()
                : a.getUsuario().getIdentificacion() != null
                ? a.getUsuario().getIdentificacion().toString() : "—")
                : "—";
        lblDetUsuario.setText(user);
        lblDetUsuario.setTextFill(Color.web("#6b7280"));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // HELPERS UI
    // ══════════════════════════════════════════════════════════════════════════
    private HBox puntito(String color, String texto) {
        javafx.scene.shape.Circle c = new javafx.scene.shape.Circle(4);
        c.setFill(Color.web(color));
        Label l = new Label(texto);
        l.setFont(Font.font("Arial", 11));
        l.setTextFill(Color.web(GRAY_TEXT));
        HBox h = new HBox(5, c, l);
        h.setAlignment(Pos.CENTER_LEFT);
        return h;
    }

    private VBox campoFiltro(String etiqueta, javafx.scene.Node control) {
        Label lbl = new Label(etiqueta);
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        lbl.setTextFill(Color.web("#374151"));
        return new VBox(5, lbl, control);
    }

    private Label filtroLabel(String txt) {
        Label l = new Label(txt);
        l.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        l.setTextFill(Color.web("#9ca3af"));
        return l;
    }

    private void estiloCombo(ComboBox<String> c) {
        c.setMaxWidth(Double.MAX_VALUE);
        c.setPrefHeight(38);
        c.setStyle("-fx-background-color:white;-fx-border-color:#e5e7eb;"
                + "-fx-border-width:1;-fx-border-radius:8;-fx-background-radius:8;-fx-font-size:12px;");
    }

    private Label detalleVal(String txt) {
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
                + "-fx-padding:10 12;-fx-border-color:#f3f4f6;"
                + "-fx-border-width:1;-fx-border-radius:10;");
        return vb;
    }

    // ── Colores por tipo ──────────────────────────────────────────────────────
    private java.awt.Color colorParaTipo(Alerta a) {
        if (a.getTipoalerta() == null) {
            return new java.awt.Color(107, 114, 128);
        }
        return switch (a.getTipoalerta().getNombre().toUpperCase()) {
            case "ROBO" ->
                new java.awt.Color(230, 126, 34);
            case "HOMICIDIO" ->
                new java.awt.Color(26, 35, 50);
            case "PERSONA_SOSPECHOSA" ->
                new java.awt.Color(14, 165, 233);
            case "INCENDIO" ->
                new java.awt.Color(229, 57, 53);
            case "EMERGENCIA_MEDICA" ->
                new java.awt.Color(29, 78, 216);
            case "ACCIDENTE" ->
                new java.awt.Color(124, 58, 237);
            case "RUIDO" ->
                new java.awt.Color(22, 163, 74);
            default ->
                new java.awt.Color(107, 114, 128);
        };
    }

    private String hexParaTipo(String tipo) {
        if (tipo == null) {
            return "#6b7280";
        }
        return switch (tipo.toUpperCase()) {
            case "ROBO" ->
                "#E67E22";
            case "HOMICIDIO" ->
                "#1A2332";
            case "PERSONA_SOSPECHOSA" ->
                "#0EA5E9";
            case "INCENDIO" ->
                "#E53935";
            case "EMERGENCIA_MEDICA" ->
                "#1D4ED8";
            case "ACCIDENTE" ->
                "#7C3AED";
            case "RUIDO" ->
                "#16A34A";
            default ->
                "#6b7280";
        };
    }

    private String abrevTipo(String tipo) {
        if (tipo == null) {
            return "?";
        }
        return switch (tipo.toUpperCase()) {
            case "ROBO" ->
                "ROBO";
            case "HOMICIDIO" ->
                "HOMIC.";
            case "PERSONA_SOSPECHOSA" ->
                "SOSP.";
            case "INCENDIO" ->
                "INCEN.";
            case "EMERGENCIA_MEDICA" ->
                "MÉDICA";
            case "ACCIDENTE" ->
                "ACCID.";
            case "RUIDO" ->
                "RUIDO";
            default ->
                tipo.substring(0, Math.min(6, tipo.length()));
        };
    }

    // ── Colores de badge por estado ───────────────────────────────────────────
    private String badgeBg(String est) {
        return switch (est.toUpperCase()) {
            case "ACTIVA" ->
                "#fef2f2";
            case "RESUELTA" ->
                "#f0fdf4";
            case "EN_PROCESO" ->
                "#eff6ff";
            case "FALSA" ->
                "#f9fafb";
            default ->
                "#f3f4f6";
        };
    }

    private String badgeFg(String est) {
        return switch (est.toUpperCase()) {
            case "ACTIVA" ->
                "#dc2626";
            case "RESUELTA" ->
                "#16a34a";
            case "EN_PROCESO" ->
                "#1d4ed8";
            case "FALSA" ->
                "#6b7280";
            default ->
                "#374151";
        };
    }

    // ── Utilidad imagen ───────────────────────────────────────────────────────
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
