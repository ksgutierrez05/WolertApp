/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.view;

/**
 *
 * @author Maria Cristina
 */


import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import sistemagestion.model.*;
import sistemagestion.service.*;

/**
 * Vista: estadísticas generales del sistema.
 * Uso: root.setCenter(new EstadisticasAdminPoliciaView(services...).build());
 */
public class EstadisticasAdminPoliciaView {

    private static final String BG           = "#f4f6fb";
    private static final String WHITE        = "#ffffff";
    private static final String RED          = "#e53935";
    private static final String RED_LIGHT    = "#fff0f0";
    private static final String ORANGE       = "#fb8c00";
    private static final String ORANGE_LIGHT = "#fff3e0";
    private static final String GREEN        = "#43a047";
    private static final String GREEN_LIGHT  = "#e8f5e9";
    private static final String BLUE         = "#1565c0";
    private static final String BLUE_LIGHT   = "#e8f0fe";
    private static final String GRAY_TEXT    = "#6b7280";
    private static final String BORDER       = "#e5e7eb";

    private final AlertaService        alertaService;
    private final AsignacionUnidadService asignacionService;
    private final UnidadPolicialService  unidadService;
    private final PoliciaService         policiaService;
    private final AlarmaService          alarmaService;
    private final NotificacionService    notificacionService;

    public EstadisticasAdminPoliciaView(
            AlertaService alertaService,
            AsignacionUnidadService asignacionService,
            UnidadPolicialService unidadService,
            PoliciaService policiaService,
            AlarmaService alarmaService,
            NotificacionService notificacionService) {
        this.alertaService       = alertaService;
        this.asignacionService   = asignacionService;
        this.unidadService       = unidadService;
        this.policiaService      = policiaService;
        this.alarmaService       = alarmaService;
        this.notificacionService = notificacionService;
    }

    public ScrollPane build() {
        VBox content = new VBox(18);
        content.setPadding(new Insets(22));
        content.setStyle("-fx-background-color: " + BG + ";");

        Label title = new Label("📊 Estadísticas");
        title.setFont(Font.font("System", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#111827"));

        // Fila 1
        HBox row1 = new HBox(16);
        row1.getChildren().addAll(
                statCard(RED_LIGHT,    RED,    "Alertas activas",    contarAlertasActivas(),   "+3 vs mes anterior",          "IncidentesPin"),
                statCard(ORANGE_LIGHT, ORANGE, "Alertas pendientes", contarAsignaciones(),     "PENDIENTE / EN PROCESO",      "AlertasPendientesPin"),
                statCard(GREEN_LIGHT,  GREEN,  "Unidades activas",   contarUnidadesActivas(),  "Estado OPERATIVA",            "UnidadPin"),
                statCard(BLUE_LIGHT,   BLUE,   "Policías activos",   contarPoliciasActivos(),  "Estado DISPONIBLE",           "PoliciaPin"));

        // Fila 2
        HBox row2 = new HBox(16);
        row2.getChildren().addAll(
                statCard(ORANGE_LIGHT, ORANGE, "Alarmas activas",  contarAlarmasActivas(),    "ACTIVA / EN MANTENIMIENTO",   "AlarmaPin"),
                statCard(BLUE_LIGHT,   BLUE,   "Notificaciones",   contarNotificaciones(),    "Total enviadas",              "NotificacionPin"));

        content.getChildren().addAll(title, row1, row2);
        return wrapScroll(content);
    }

    // ── Stat card ────────────────────────────────────────────────
    private VBox statCard(String bgIcon, String accentColor, String title, long value, String sub, String iconName) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20, 22, 20, 22));
        card.setStyle("-fx-background-color:white; -fx-background-radius:18;");
        card.setPrefWidth(260);
        HBox.setHgrow(card, Priority.ALWAYS);

        StackPane iconWrap = new StackPane();
        iconWrap.setPrefSize(52, 52);
        iconWrap.setMinSize(52, 52);
        iconWrap.setMaxSize(52, 52);
        Region colorBg = new Region();
        colorBg.setPrefSize(52, 52);
        colorBg.setStyle("-fx-background-color:" + bgIcon + "; -fx-background-radius:14;");
        ImageView iv = new ImageView();
        iv.setFitWidth(28); iv.setFitHeight(28); iv.setPreserveRatio(true);
        try {
            java.io.InputStream is = getClass().getResourceAsStream("/" + iconName + ".png");
            if (is != null) {
                java.awt.image.BufferedImage original = javax.imageio.ImageIO.read(is);
                java.awt.image.BufferedImage recortada = recortarTransparencia(original);
                iv.setImage(javafx.embed.swing.SwingFXUtils.toFXImage(recortada, null));
            }
        } catch (Exception ignored) {}
        iconWrap.getChildren().addAll(colorBg, iv);

        Label titleLbl = label(title, 13, "#374151", true);
        Label valLbl   = new Label(String.valueOf(value));
        valLbl.setStyle("-fx-font-size:36px; -fx-font-weight:bold; -fx-text-fill:" + accentColor + ";");
        Label subLbl   = label(sub, 11, GRAY_TEXT, false);

        VBox textBlock = new VBox(3);
        textBlock.getChildren().addAll(titleLbl, valLbl, subLbl);

        HBox topRow = new HBox(16);
        topRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        topRow.getChildren().addAll(iconWrap, textBlock);
        card.getChildren().add(topRow);

        card.setOnMouseEntered(e -> {
            card.setTranslateY(-3);
            card.setStyle("-fx-background-color:white; -fx-background-radius:18;"
                    + "-fx-border-color:" + accentColor + "; -fx-border-width:1.5; -fx-border-radius:18;");
        });
        card.setOnMouseExited(e -> {
            card.setTranslateY(0);
            card.setStyle("-fx-background-color:white; -fx-background-radius:18;");
        });
        return card;
    }

    // ── Contadores ───────────────────────────────────────────────
    private long contarAlertasActivas() {
        try {
            return alertaService.listar().stream()
                    .filter(a -> a.getEstado() == EstadoAlerta.PENDIENTE
                              || a.getEstado() == EstadoAlerta.EN_ATENCION
                              || a.getEstado() == EstadoAlerta.UNIDAD_ASIGNADA
                              || a.getEstado() == EstadoAlerta.RECIBIDA)
                    .count();
        } catch (Exception e) { return 0; }
    }

    private long contarAsignaciones() {
        try { return asignacionService == null ? 0 : asignacionService.listar().size(); }
        catch (Exception e) { return 0; }
    }

    private long contarUnidadesActivas() {
        try {
            return unidadService.listar().stream()
                    .filter(u -> u.getEstado() == EstadoUnidadPolicial.OPERATIVA).count();
        } catch (Exception e) { return 0; }
    }

    private long contarPoliciasActivos() {
        try {
            return policiaService.listar().stream()
                    .filter(p -> p.getEstadopolicial() == EstadoPolicia.DISPONIBLE
                              || p.getEstadopolicial() == EstadoPolicia.EN_SERVICIO)
                    .count();
        } catch (Exception e) { return 0; }
    }

    private long contarAlarmasActivas() {
        try {
            return alarmaService.listar().stream()
                    .filter(a -> a.getEstado() == EstadoAlarma.ACTIVA
                              || a.getEstado() == EstadoAlarma.EN_MANTENIMIENTO)
                    .count();
        } catch (Exception e) { return 0; }
    }

    private long contarNotificaciones() {
        try { return notificacionService == null ? 0 : notificacionService.listar().size(); }
        catch (Exception e) { return 0; }
    }

    // ── Helpers ──────────────────────────────────────────────────
    private Label label(String text, double size, String color, boolean bold) {
        Label lbl = new Label(text);
        lbl.setFont(bold ? Font.font("System", FontWeight.BOLD, size) : Font.font("System", size));
        lbl.setTextFill(Color.web(color));
        return lbl;
    }

    private ScrollPane wrapScroll(VBox content) {
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background: " + BG + "; -fx-background-color: " + BG + ";");
        return scroll;
    }

    private java.awt.image.BufferedImage recortarTransparencia(java.awt.image.BufferedImage image) {
        int minX = image.getWidth(), minY = image.getHeight(), maxX = 0, maxY = 0;
        for (int y = 0; y < image.getHeight(); y++)
            for (int x = 0; x < image.getWidth(); x++) {
                int alpha = (image.getRGB(x, y) >> 24) & 0xff;
                if (alpha > 0) { minX = Math.min(minX, x); minY = Math.min(minY, y); maxX = Math.max(maxX, x); maxY = Math.max(maxY, y); }
            }
        return image.getSubimage(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }
}
  