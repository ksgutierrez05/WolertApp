/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.view;

/**
 *
 * @author Maria Cristina
 */
import java.awt.image.BufferedImage;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import javafx.scene.image.ImageView;
import sistemagestion.model.*;
import sistemagestion.service.*;

public class CentroOperacionesPoliciaView {

    private static final String WHITE = "#ffffff";
    private static final String BG = "#f4f6fb";
    private static final String RED = "#e53935";
    private static final String RED_LIGHT = "#fff0f0";
    private static final String ORANGE = "#fb8c00";
    private static final String ORANGE_LIGHT = "#fff3e0";
    private static final String GREEN = "#43a047";
    private static final String GREEN_LIGHT = "#e8f5e9";
    private static final String BLUE = "#1565c0";
    private static final String BLUE_LIGHT = "#e8f0fe";
    private static final String GRAY_TEXT = "#6b7280";
    private static final String BORDER = "#e5e7eb";

    private final Usuario usuarioActual;
    private final Policia policiaActual;
    private final AlertaService alertaService;
    private final AtencionAlertaService atencionService;
    private final AlarmaService alarmaService;
    private final NotificacionService notificacionService;
    private final BorderPane root;

    public CentroOperacionesPoliciaView(Usuario usuarioActual, Policia policiaActual,
            AlertaService alertaService, AtencionAlertaService atencionService,
            AlarmaService alarmaService, NotificacionService notificacionService,
            BorderPane root) {
        this.usuarioActual = usuarioActual;
        this.policiaActual = policiaActual;
        this.alertaService = alertaService;
        this.atencionService = atencionService;
        this.alarmaService = alarmaService;
        this.notificacionService = notificacionService;
        this.root = root;
    }

    public ScrollPane build() {
        VBox content = new VBox(18);
        content.setPadding(new Insets(22));
        content.setStyle("-fx-background-color: " + BG + ";");
        content.getChildren().addAll(
                buildTopBar(),
                buildStats(),
                buildBottomPanels());
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: " + BG + "; -fx-background: " + BG + ";");
        return scroll;
    }

    // ── Top bar ──────────────────────────────────────────────────
    private HBox buildTopBar() {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);

        String saludo = usuarioActual != null && usuarioActual.getPrimer_nombre() != null
                ? "¡Bienvenido, " + usuarioActual.getPrimer_nombre() + "!"
                : "¡Bienvenido!";
        VBox greeting = new VBox(3);
        Label hello = new Label(saludo);
        hello.setFont(Font.font("System", FontWeight.BOLD, 24));
        hello.setTextFill(Color.web("#111827"));
        String rangoSub = policiaActual != null
                ? (policiaActual.getRango() != null ? policiaActual.getRango() : "Policía")
                + (policiaActual.getUnidadpolicial() != null
                ? " — " + policiaActual.getUnidadpolicial().getNombre() : "")
                : "Portal policial — WolertApp";
        greeting.getChildren().addAll(hello, label(rangoSub, 12, GRAY_TEXT, false));

        HBox rightBox = new HBox(16);
        rightBox.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(rightBox, Priority.ALWAYS);

        VBox dateBox = new VBox(2);
        dateBox.setAlignment(Pos.CENTER_RIGHT);
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", new Locale("es", "CO"));
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("hh:mm:ss a", new Locale("es", "CO"));
        LocalDateTime now0 = LocalDateTime.now(ZoneId.of("America/Bogota"));
        Label dateLbl = label("📅  " + now0.format(dateFmt), 12, "#374151", false);
        Label timeLbl = label(now0.format(timeFmt), 12, GRAY_TEXT, false);
        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), ev -> {
            LocalDateTime now = LocalDateTime.now(ZoneId.of("America/Bogota"));
            dateLbl.setText("📅  " + now.format(dateFmt));
            timeLbl.setText(now.format(timeFmt));
        }));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();
        dateBox.getChildren().addAll(dateLbl, timeLbl);

        int notiCount = contarNotificaciones();
        StackPane bell = new StackPane();
        Label bellIcon = label("🔔", 18, "#374151", false);
        if (notiCount > 0) {
            Circle badge = new Circle(7, Color.web(RED));
            badge.setTranslateX(9);
            badge.setTranslateY(-9);
            Label badgeNum = label(String.valueOf(notiCount), 8, WHITE, true);
            badgeNum.setTranslateX(9);
            badgeNum.setTranslateY(-9);
            bell.getChildren().addAll(bellIcon, badge, badgeNum);
        } else {
            bell.getChildren().add(bellIcon);
        }

        rightBox.getChildren().addAll(dateBox, bell);
        bar.getChildren().addAll(greeting, rightBox);
        return bar;
    }

    // ── Stats ────────────────────────────────────────────────────
   private HBox buildStats() {
    HBox row = new HBox(16);
    row.getChildren().addAll(
            statCard(RED_LIGHT,    RED,    "\uf0f3", "Mis alertas activas", contarMisAlertasActivas(), "PENDIENTE / EN ATENCIÓN"),
            statCard(ORANGE_LIGHT, ORANGE, "\uf046", "Mis atenciones",      contarMisAtenciones(),     "Total registradas"),
            statCard(GREEN_LIGHT,  GREEN,  "\uf0f3", "Alarmas activas",     contarAlarmasActivas(),    "ACTIVA / EN MANTENIMIENTO"),
            statCard(BLUE_LIGHT,   BLUE,   "\uf0e0", "Notificaciones",      contarNotificaciones(),    "Total recibidas")
    );
    return row;
}

    // ── Bottom panels (solo alertas recientes y alarmas) ─────────
    private HBox buildBottomPanels() {
        HBox row = new HBox(16);

        // Alertas recientes
        VBox alertas = createPanel("🚨 Mis alertas recientes");
        HBox.setHgrow(alertas, Priority.ALWAYS);
        try {
            List<Alerta> lista = obtenerMisAlertas();
            if (lista.isEmpty()) {
                alertas.getChildren().add(label("No tienes alertas asignadas", 13, GRAY_TEXT, false));
            } else {
                lista.stream()
                        .filter(a -> a.getFechaHora() != null)
                        .sorted((a, b) -> b.getFechaHora().compareTo(a.getFechaHora()))
                        .limit(5)
                        .forEach(a -> {
                            String dotColor = estadoColor(a.getEstado());
                            String tipo = a.getTipoalerta() != null ? a.getTipoalerta().getNombre() : "Alerta";
                            String barr = a.getBarrio() != null ? " — " + a.getBarrio().getNombre() : "";
                            String sub = formatFecha(a.getFechaHora())
                                    + (a.getEstado() != null ? " · " + a.getEstado().name().replace("_", " ") : "");
                            alertas.getChildren().addAll(alertItem("🔔", tipo + barr, sub, dotColor), separator());
                        });
            }
        } catch (Exception e) {
            alertas.getChildren().add(label("Error: " + e.getMessage(), 12, RED, false));
        }

        // Atenciones recientes
        VBox atenciones = createPanel("📋 Mis atenciones recientes");
        HBox.setHgrow(atenciones, Priority.ALWAYS);
        try {
            List<AtencionAlerta> lista = obtenerMisAtenciones();
            if (lista.isEmpty()) {
                atenciones.getChildren().add(label("Sin atenciones registradas", 12, GRAY_TEXT, false));
            } else {
                lista.stream().limit(4).forEach(a -> {
                    String estadoStr = a.getEstado() != null ? a.getEstado().name().replace("_", " ") : "—";
                    String desc = a.getDescripcion() != null ? a.getDescripcion() : "—";
                    String color = switch (a.getEstado() != null ? a.getEstado() : EstadoAtencionAlerta.PENDIENTE) {
                        case FINALIZADA ->
                            GREEN;
                        case EN_PROCESO ->
                            ORANGE;
                        case CANCELADA ->
                            GRAY_TEXT;
                        default ->
                            RED;
                    };
                    atenciones.getChildren().addAll(listItem("📋", estadoStr, desc, color), separator());
                });
            }
        } catch (Exception e) {
            atenciones.getChildren().add(label("Error: " + e.getMessage(), 12, RED, false));
        }

        // Alarmas
        VBox alarmas = createPanel("🔔 Alarmas en mi barrio");
        HBox.setHgrow(alarmas, Priority.ALWAYS);
        try {
            List<Alarma> lista = alarmaService.listar();
            if (lista.isEmpty()) {
                alarmas.getChildren().add(label("Sin alarmas registradas", 12, GRAY_TEXT, false));
            } else {
                lista.stream().limit(4).forEach(a -> {
                    String estadoStr = a.getEstado() != null ? a.getEstado().name().replace("_", " ") : "—";
                    String color = a.getEstado() == EstadoAlarma.ACTIVA ? GREEN
                            : a.getEstado() == EstadoAlarma.EN_MANTENIMIENTO ? ORANGE : GRAY_TEXT;
                    alarmas.getChildren().addAll(listItem("🔔", a.getNombre(), estadoStr, color), separator());
                });
            }
        } catch (Exception e) {
            alarmas.getChildren().add(label("Error: " + e.getMessage(), 12, RED, false));
        }

        row.getChildren().addAll(alertas, atenciones, alarmas);
        return row;
    }

    // ── Contadores ───────────────────────────────────────────────
    private long contarMisAlertasActivas() {
        try {
            return obtenerMisAlertas().stream()
                    .filter(a -> a.getEstado() == EstadoAlerta.PENDIENTE
                    || a.getEstado() == EstadoAlerta.EN_ATENCION
                    || a.getEstado() == EstadoAlerta.UNIDAD_ASIGNADA
                    || a.getEstado() == EstadoAlerta.RECIBIDA)
                    .count();
        } catch (Exception e) {
            return 0;
        }
    }

    private long contarMisAtenciones() {
        try {
            return obtenerMisAtenciones().size();
        } catch (Exception e) {
            return 0;
        }
    }

    private long contarAlarmasActivas() {
        try {
            if (alarmaService == null) {
                return 0;
            }
            return alarmaService.listar().stream()
                    .filter(a -> a.getEstado() == EstadoAlarma.ACTIVA
                    || a.getEstado() == EstadoAlarma.EN_MANTENIMIENTO)
                    .count();
        } catch (Exception e) {
            return 0;
        }
    }

    private int contarNotificaciones() {
        try {
            return notificacionService != null ? notificacionService.listar().size() : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    // ── Datos ────────────────────────────────────────────────────
    private List<Alerta> obtenerMisAlertas() throws Exception {
        if (alertaService == null) {
            return List.of();
        }
        List<Alerta> todas = alertaService.listar();
        if (usuarioActual == null) {
            return todas;
        }
        List<Alerta> mias = todas.stream()
                .filter(a -> a.getUsuario() != null
                && usuarioActual.getUsername() != null
                && usuarioActual.getUsername().equals(a.getUsuario().getUsername()))
                .collect(java.util.stream.Collectors.toList());
        return mias.isEmpty() ? todas : mias;
    }

    private List<AtencionAlerta> obtenerMisAtenciones() throws Exception {
        if (atencionService == null) {
            return List.of();
        }
        List<AtencionAlerta> todas = atencionService.listar();
        if (policiaActual == null || policiaActual.getUnidadpolicial() == null) {
            return todas;
        }
        String miUnidad = policiaActual.getUnidadpolicial().getNombre();
        List<AtencionAlerta> mias = todas.stream()
                .filter(at -> at.getUnidad() != null
                && miUnidad.equalsIgnoreCase(at.getUnidad().getNombre()))
                .collect(java.util.stream.Collectors.toList());
        return mias.isEmpty() ? todas : mias;
    }

    // ── Helpers UI ───────────────────────────────────────────────
    private VBox statCard(String bgIcon, String accentColor, String iconFA,
            String title, long value, String sub) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20, 22, 20, 22));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 18;");
        card.setPrefWidth(260);
        HBox.setHgrow(card, Priority.ALWAYS);
        shadow(card);

        // Ícono FA en rectángulo redondeado
        StackPane iconWrap = new StackPane();
        iconWrap.setPrefSize(52, 52);
        iconWrap.setMinSize(52, 52);
        iconWrap.setMaxSize(52, 52);

        Rectangle iconBg = new Rectangle(52, 52);
        iconBg.setArcWidth(16);
        iconBg.setArcHeight(16);
        iconBg.setFill(Color.web(bgIcon));

        Label iconLbl = new Label(iconFA);
        iconLbl.setStyle(
                "-fx-font-family: 'Font Awesome 6 Free Solid';"
                + "-fx-font-size: 22px;"
                + "-fx-text-fill: " + accentColor + ";");

        iconWrap.getChildren().addAll(iconBg, iconLbl);

        // Textos
        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #374151;");

        Label valLbl = new Label(String.valueOf(value));
        valLbl.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: " + accentColor + ";");

        Label subLbl = new Label(sub);
        subLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: " + GRAY_TEXT + ";");

        VBox textBlock = new VBox(3, titleLbl, valLbl, subLbl);

        HBox topRow = new HBox(16, iconWrap, textBlock);
        topRow.setAlignment(Pos.CENTER_LEFT);
        card.getChildren().add(topRow);

        card.setOnMouseEntered(e -> {
            card.setTranslateY(-3);
            card.setStyle("-fx-background-color: white; -fx-background-radius: 18;"
                    + "-fx-border-color: " + accentColor + "; -fx-border-width: 1.5; -fx-border-radius: 18;");
        });
        card.setOnMouseExited(e -> {
            card.setTranslateY(0);
            card.setStyle("-fx-background-color: white; -fx-background-radius: 18;");
        });

        return card;
    }

    private VBox createPanel(String title) {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(16));
        panel.setStyle("-fx-background-color: " + WHITE + "; -fx-background-radius: 12;");
        shadow(panel);
        panel.getChildren().addAll(label(title, 14, "#111827", true), separator());
        return panel;
    }

    private HBox listItem(String icon, String title, String sub, String subColor) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(6, 0, 6, 0));
        StackPane iconBox = new StackPane();
        Rectangle bg = new Rectangle(32, 32);
        bg.setArcWidth(7);
        bg.setArcHeight(7);
        bg.setFill(Color.web(BG));
        iconBox.getChildren().addAll(bg, label(icon, 14, BLUE, false));
        VBox text = new VBox(1);
        HBox.setHgrow(text, Priority.ALWAYS);
        text.getChildren().addAll(label(title, 12, "#111827", false), label(sub, 10, subColor, false));
        row.getChildren().addAll(iconBox, text);
        return row;
    }

    private HBox alertItem(String icon, String title, String sub, String dotColor) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(7, 0, 7, 0));
        row.setCursor(javafx.scene.Cursor.HAND);
        Circle dotCircle = new Circle(5, Color.web(dotColor));
        StackPane iconBox = new StackPane();
        Rectangle bg = new Rectangle(33, 33);
        bg.setArcWidth(7);
        bg.setArcHeight(7);
        bg.setFill(Color.web(BG));
        iconBox.getChildren().addAll(bg, label(icon, 14, dotColor, false));
        VBox text = new VBox(2);
        HBox.setHgrow(text, Priority.ALWAYS);
        text.getChildren().addAll(label(title, 12, "#111827", false), label(sub, 10, GRAY_TEXT, false));
        row.getChildren().addAll(dotCircle, iconBox, text, label(">", 13, GRAY_TEXT, false));
        return row;
    }

    private Region separator() {
        Region sep = new Region();
        sep.setPrefHeight(1);
        sep.setStyle("-fx-background-color: " + BORDER + ";");
        return sep;
    }

    private Label label(String text, double size, String color, boolean bold) {
        Label lbl = new Label(text);
        lbl.setFont(bold ? Font.font("System", FontWeight.BOLD, size) : Font.font("System", size));
        lbl.setTextFill(Color.web(color));
        return lbl;
    }

    private void shadow(Region node) {
        node.setEffect(new DropShadow(10, 0, 2, Color.web("#0000001a")));
    }

    private String estadoColor(EstadoAlerta estado) {
        if (estado == null) {
            return GRAY_TEXT;
        }
        return switch (estado) {
            case PENDIENTE, EN_ATENCION ->
                RED;
            case UNIDAD_ASIGNADA ->
                ORANGE;
            case RESUELTA ->
                GREEN;
            default ->
                GRAY_TEXT;
        };
    }

    private String formatFecha(LocalDateTime dt) {
        if (dt == null) {
            return "—";
        }
        long mins = java.time.Duration.between(dt, LocalDateTime.now()).toMinutes();
        if (mins < 1) {
            return "Hace un momento";
        }
        if (mins < 60) {
            return "Hace " + mins + " min";
        }
        if (mins < 1440) {
            return "Hace " + (mins / 60) + " h";
        }
        return dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    private BufferedImage recortarTransparencia(BufferedImage image) {
        int minX = image.getWidth(), minY = image.getHeight(), maxX = 0, maxY = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int alpha = (image.getRGB(x, y) >> 24) & 0xff;
                if (alpha > 0) {
                    if (x < minX) {
                        minX = x;
                    }
                    if (y < minY) {
                        minY = y;
                    }
                    if (x > maxX) {
                        maxX = x;
                    }
                    if (y > maxY) {
                        maxY = y;
                    }
                }
            }
        }
        return image.getSubimage(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }
}
