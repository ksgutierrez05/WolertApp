package sistemagestion.view;

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
import sistemagestion.model.*;
import sistemagestion.service.*;

public class CentroOperacionesPoliciaView {

    private static final String WHITE    = "#ffffff";
    private static final String BG       = "#f4f6fb";
    private static final String RED      = "#e53935";
    private static final String RED_LIGHT   = "#fff0f0";
    private static final String ORANGE   = "#fb8c00";
    private static final String ORANGE_LIGHT = "#fff3e0";
    private static final String GREEN    = "#43a047";
    private static final String GREEN_LIGHT  = "#e8f5e9";
    private static final String BLUE     = "#1565c0";
    private static final String BLUE_LIGHT   = "#e8f0fe";
    private static final String GRAY_TEXT = "#6b7280";
    private static final String BORDER   = "#e5e7eb";

    private final Usuario              usuarioActual;
    private final Policia              policiaActual;
    private final AlertaService        alertaService;
    private final AtencionAlertaService atencionService;
    private final AlarmaService        alarmaService;
    private final NotificacionService  notificacionService;
    private final BorderPane           root;

    public CentroOperacionesPoliciaView(Usuario usuarioActual, Policia policiaActual,
            AlertaService alertaService, AtencionAlertaService atencionService,
            AlarmaService alarmaService, NotificacionService notificacionService,
            BorderPane root) {
        this.usuarioActual      = usuarioActual;
        this.policiaActual      = policiaActual;
        this.alertaService      = alertaService;
        this.atencionService    = atencionService;
        this.alarmaService      = alarmaService;
        this.notificacionService = notificacionService;
        this.root               = root;
    }

    public ScrollPane build() {
        VBox content = new VBox(28);
        content.setPadding(new Insets(36,22,22,22));
        content.setStyle("-fx-background-color: " + BG + ";");
        content.getChildren().addAll(
                buildTopBar(),
                buildStats(),
                buildBottomPanels(),
                buildFooterBanner()
        );
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
        VBox greeting = new VBox(6);
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
                statCard(RED_LIGHT,    RED,    "\uf0f3", "Mis alertas activas",  contarMisAlertasActivas(), "PENDIENTE / EN ATENCIÓN"),
                statCard(ORANGE_LIGHT, ORANGE, "\uf046", "Mis atenciones",       contarMisAtenciones(),     "Total registradas"),
                statCard(GREEN_LIGHT,  GREEN,  "\uf0f3", "Alarmas activas",      contarAlarmasActivas(),    "ACTIVA / EN MANTENIMIENTO"),
                statCard(BLUE_LIGHT,   BLUE,   "\uf0e0", "Notificaciones",       contarNotificaciones(),    "Total recibidas")
        );
        return row;
    }

    // ── Tres paneles: alertas, atenciones, alarmas ───────────────
    private HBox buildBottomPanels() {
        HBox row = new HBox(16);
        HBox.setHgrow(row, Priority.ALWAYS);
        row.setFillHeight(true);

        // ── Panel 1: Mis alertas recientes ─────────────────────
        VBox alertas = new VBox(0);
        alertas.setPadding(new Insets(18));
        alertas.setStyle("-fx-background-color: white; -fx-background-radius: 16;");
        HBox.setHgrow(alertas, Priority.ALWAYS);
        alertas.setMaxWidth(Double.MAX_VALUE);
        shadow(alertas);

        HBox hAlerta = new HBox(8);
        hAlerta.setAlignment(Pos.CENTER_LEFT);
        hAlerta.setPadding(new Insets(0, 0, 12, 0));

        StackPane iconAlertaBox = new StackPane();
        iconAlertaBox.setPrefSize(32, 32);
        iconAlertaBox.setMinSize(32, 32);
        iconAlertaBox.setMaxSize(32, 32);
        iconAlertaBox.setStyle("-fx-background-color: #fff0f0; -fx-background-radius: 9;");
        Label iconAlertaLbl = new Label("\uf0f3");
        iconAlertaLbl.setStyle("-fx-font-family: 'Font Awesome 6 Free Solid'; -fx-font-size: 15px; -fx-text-fill: #e53935;");
        iconAlertaBox.getChildren().add(iconAlertaLbl);

        Label tituloAlertas = label("Mis alertas recientes", 14, "#111827", true);
        HBox.setHgrow(tituloAlertas, Priority.ALWAYS);

        Label verTodasBtn = label("Ver todas  ›", 11, "#1565c0", true);
        verTodasBtn.setCursor(javafx.scene.Cursor.HAND);
        verTodasBtn.setOnMouseEntered(e -> verTodasBtn.setStyle("-fx-font-size: 11px; -fx-text-fill: #0d47a1; -fx-underline: true;"));
        verTodasBtn.setOnMouseExited(e  -> verTodasBtn.setStyle("-fx-font-size: 11px; -fx-text-fill: #1565c0; -fx-underline: false;"));
        verTodasBtn.setOnMouseClicked(e ->
                root.setCenter(new MisAlertasPoliciaView(
                        usuarioActual, policiaActual,
                        alertaService, atencionService, root).build()));
        hAlerta.getChildren().addAll(iconAlertaBox, tituloAlertas, verTodasBtn);
        alertas.getChildren().addAll(hAlerta, separator());

        try {
            List<Alerta> lista = obtenerMisAlertas();
            if (lista.isEmpty()) {
                alertas.getChildren().add(placeholderVacio("Sin alertas asignadas", "\uf058", "#43a047"));
            } else {
                lista.stream()
                        .filter(a -> a.getFechaHora() != null)
                        .sorted((a, b) -> b.getFechaHora().compareTo(a.getFechaHora()))
                        .limit(5)
                        .forEach(a -> {
                            String tipo  = a.getTipoalerta() != null ? a.getTipoalerta().getNombre() : "Alerta";
                            String barr  = a.getBarrio()    != null ? " — " + a.getBarrio().getNombre() : "";
                            String sub   = formatFecha(a.getFechaHora());
                            String dot   = estadoColor(a.getEstado());
                            String estadoNombre = a.getEstado() != null
                                    ? a.getEstado().name().replace("_", " ") : "—";
                            HBox fila = alertItemMejorado(iconoTipoAlerta(a), tipo + barr, sub, estadoNombre, dot);
                            fila.setOnMouseClicked(e ->
                                    root.setCenter(new MisAlertasPoliciaView(
                                            usuarioActual, policiaActual,
                                            alertaService, atencionService, root).build()));
                            alertas.getChildren().addAll(fila, separator());
                        });
            }
        } catch (Exception e) {
            alertas.getChildren().add(label("Error: " + e.getMessage(), 11, RED, false));
        }

        // ── Panel 2: Mis atenciones recientes ──────────────────
        VBox atenciones = new VBox(0);
        atenciones.setPadding(new Insets(18));
        atenciones.setStyle("-fx-background-color: white; -fx-background-radius: 16;");
        HBox.setHgrow(atenciones, Priority.ALWAYS);
        atenciones.setMaxWidth(Double.MAX_VALUE);
        shadow(atenciones);

        HBox hAtencion = new HBox(8);
        hAtencion.setAlignment(Pos.CENTER_LEFT);
        hAtencion.setPadding(new Insets(0, 0, 12, 0));

        StackPane iconAtenBox = new StackPane();
        iconAtenBox.setPrefSize(32, 32);
        iconAtenBox.setMinSize(32, 32);
        iconAtenBox.setMaxSize(32, 32);
        iconAtenBox.setStyle("-fx-background-color: #fff8e1; -fx-background-radius: 9;");
        Label iconAtenLbl = new Label("\uf46d");
        iconAtenLbl.setStyle("-fx-font-family: 'Font Awesome 6 Free Solid'; -fx-font-size: 15px; -fx-text-fill: #fb8c00;");
        iconAtenBox.getChildren().add(iconAtenLbl);

        hAtencion.getChildren().addAll(iconAtenBox, label("Mis atenciones recientes", 14, "#111827", true));
        atenciones.getChildren().addAll(hAtencion, separator());

        try {
            List<AtencionAlerta> lista = obtenerMisAtenciones();
            if (lista.isEmpty()) {
                atenciones.getChildren().add(placeholderVacio("Sin atenciones registradas", "\uf46d", "#fb8c00"));
            } else {
                lista.stream().limit(4).forEach(a -> {
                    String estadoStr = a.getEstado() != null
                            ? a.getEstado().name().replace("_", " ") : "—";
                    String desc = a.getDescripcion() != null
                            ? (a.getDescripcion().length() > 45
                               ? a.getDescripcion().substring(0, 45) + "…"
                               : a.getDescripcion())
                            : "Sin descripción";
                    String colorEstado = switch (a.getEstado() != null
                            ? a.getEstado() : EstadoAtencionAlerta.PENDIENTE) {
                        case FINALIZADA  -> GREEN;
                        case EN_PROCESO  -> ORANGE;
                        case CANCELADA   -> GRAY_TEXT;
                        default          -> RED;
                    };
                    String iconFA = switch (a.getEstado() != null
                            ? a.getEstado() : EstadoAtencionAlerta.PENDIENTE) {
                        case FINALIZADA  -> "\uf058";
                        case EN_PROCESO  -> "\uf017";
                        case CANCELADA   -> "\uf057";
                        default          -> "\uf071";
                    };
                    atenciones.getChildren().addAll(
                            atencionItemMejorado(iconFA, colorEstado, estadoStr, desc),
                            separator());
                });
            }
        } catch (Exception e) {
            atenciones.getChildren().add(label("Error: " + e.getMessage(), 11, RED, false));
        }

        // ── Panel 3: Alarmas en mi barrio ──────────────────────
        VBox alarmas = new VBox(0);
        alarmas.setPadding(new Insets(18));
        alarmas.setStyle("-fx-background-color: white; -fx-background-radius: 16;");
        HBox.setHgrow(alarmas, Priority.ALWAYS);
        alarmas.setMaxWidth(Double.MAX_VALUE);
        shadow(alarmas);

        HBox hAlarma = new HBox(8);
        hAlarma.setAlignment(Pos.CENTER_LEFT);
        hAlarma.setPadding(new Insets(0, 0, 12, 0));

        StackPane iconAlarmBox = new StackPane();
        iconAlarmBox.setPrefSize(32, 32);
        iconAlarmBox.setMinSize(32, 32);
        iconAlarmBox.setMaxSize(32, 32);
        iconAlarmBox.setStyle("-fx-background-color: #e8f5e9; -fx-background-radius: 9;");
        Label iconAlarmLbl = new Label("\uf0f3");
        iconAlarmLbl.setStyle("-fx-font-family: 'Font Awesome 6 Free Solid'; -fx-font-size: 15px; -fx-text-fill: #43a047;");
        iconAlarmBox.getChildren().add(iconAlarmLbl);

        Label tituloAlarmas = label("Alarmas en mi barrio", 14, "#111827", true);
        HBox.setHgrow(tituloAlarmas, Priority.ALWAYS);

        Label verAlarmasBtn = label("Ver todas  ›", 11, "#1565c0", true);
        verAlarmasBtn.setCursor(javafx.scene.Cursor.HAND);
        verAlarmasBtn.setOnMouseEntered(e -> verAlarmasBtn.setStyle("-fx-font-size: 11px; -fx-text-fill: #0d47a1; -fx-underline: true;"));
        verAlarmasBtn.setOnMouseExited(e  -> verAlarmasBtn.setStyle("-fx-font-size: 11px; -fx-text-fill: #1565c0; -fx-underline: false;"));
        verAlarmasBtn.setOnMouseClicked(e ->
                root.setCenter(new AlarmasPoliciaView(alarmaService, policiaActual).build()));
        hAlarma.getChildren().addAll(iconAlarmBox, tituloAlarmas, verAlarmasBtn);
        alarmas.getChildren().addAll(hAlarma, separator());

        try {
            List<Alarma> lista = alarmaService != null ? alarmaService.listar() : List.of();
            if (lista.isEmpty()) {
                alarmas.getChildren().add(placeholderVacio("Sin alarmas registradas", "\uf0f3", "#43a047"));
            } else {
                lista.stream().limit(5).forEach(a -> {
                    String estadoStr   = a.getEstado() != null
                            ? a.getEstado().name().replace("_", " ") : "—";
                    String colorEstado = a.getEstado() == EstadoAlarma.ACTIVA         ? GREEN
                                       : a.getEstado() == EstadoAlarma.EN_MANTENIMIENTO ? ORANGE
                                       : GRAY_TEXT;
                    String iconFA      = a.getEstado() == EstadoAlarma.ACTIVA         ? "\uf0f3"
                                       : a.getEstado() == EstadoAlarma.EN_MANTENIMIENTO ? "\uf7d9"
                                       : "\uf1e6";
                    alarmas.getChildren().addAll(
                            alarmaItemMejorado(iconFA, colorEstado, a.getNombre(), estadoStr),
                            separator());
                });
            }
        } catch (Exception e) {
            alarmas.getChildren().add(label("Error: " + e.getMessage(), 11, RED, false));
        }

        row.getChildren().addAll(alertas, atenciones, alarmas);
        return row;
    }

    // ── Banner inferior (equivalente al footer de UsuarioApp) ────
    private HBox buildFooterBanner() {
        HBox banner = new HBox();
        banner.setPadding(new Insets(22, 32, 22, 32));
        banner.setPrefHeight(110);
        banner.setStyle(
                "-fx-background-color: #eef2fb;"
                + "-fx-background-radius: 16;"
                + "-fx-border-color: #dbe4f5;"
                + "-fx-border-radius: 16;"
                + "-fx-border-width: 1.2;");
        shadow(banner);

        // ── Bloque izquierdo: escudo ──────────────────────────
        StackPane shieldBox = new StackPane();
        shieldBox.setPrefSize(56, 56);
        shieldBox.setMinSize(56, 56);
        shieldBox.setMaxSize(56, 56);
        shieldBox.setStyle("-fx-background-color: #dbeafe; -fx-background-radius: 50%;");
        Label shieldLbl = new Label("\uf505");   // fa-shield-halved
        shieldLbl.setStyle(
                "-fx-font-family: 'Font Awesome 6 Free Solid';"
                + "-fx-font-size: 24px;"
                + "-fx-text-fill: #1565c0;");
        shieldBox.getChildren().add(shieldLbl);

        VBox leftText = new VBox(3);
        leftText.setPadding(new Insets(0, 0, 0, 14));
        leftText.getChildren().addAll(
                label("Unidad operativa activa", 15, BLUE, true),
                label("Tu unidad está registrada y lista para recibir asignaciones.", 12, GRAY_TEXT, false)
        );

        HBox left = new HBox();
        left.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(left, Priority.ALWAYS);
        left.getChildren().addAll(shieldBox, leftText);

        // ── Bloque derecho: candado confidencialidad ──────────
        StackPane lockBox = new StackPane();
        lockBox.setPrefSize(56, 56);
        lockBox.setMinSize(56, 56);
        lockBox.setMaxSize(56, 56);
        lockBox.setStyle("-fx-background-color: #dbeafe; -fx-background-radius: 50%;");
        Label lockLbl = new Label("\uf023");     // fa-lock
        lockLbl.setStyle(
                "-fx-font-family: 'Font Awesome 6 Free Solid';"
                + "-fx-font-size: 24px;"
                + "-fx-text-fill: #1565c0;");
        lockBox.getChildren().add(lockLbl);

        VBox rightText = new VBox(3);
        rightText.setAlignment(Pos.CENTER_RIGHT);
        rightText.getChildren().addAll(
                label("Información confidencial", 15, BLUE, true),
                label("Los datos de operación son de uso exclusivo policial.", 12, GRAY_TEXT, false)
        );

        HBox right = new HBox(14);
        right.setAlignment(Pos.CENTER_RIGHT);
        right.getChildren().addAll(rightText, lockBox);

        banner.getChildren().addAll(left, right);
        return banner;
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
        } catch (Exception e) { return 0; }
    }

    private long contarMisAtenciones() {
        try { return obtenerMisAtenciones().size(); }
        catch (Exception e) { return 0; }
    }

    private long contarAlarmasActivas() {
        try {
            if (alarmaService == null) return 0;
            return alarmaService.listar().stream()
                    .filter(a -> a.getEstado() == EstadoAlarma.ACTIVA
                              || a.getEstado() == EstadoAlarma.EN_MANTENIMIENTO)
                    .count();
        } catch (Exception e) { return 0; }
    }

    private int contarNotificaciones() {
        try { return notificacionService != null ? notificacionService.listar().size() : 0; }
        catch (Exception e) { return 0; }
    }

    // ── Datos ────────────────────────────────────────────────────
    private List<Alerta> obtenerMisAlertas() throws Exception {
        if (alertaService == null) return List.of();
        List<Alerta> todas = alertaService.listar();
        if (usuarioActual == null) return todas;
        List<Alerta> mias = todas.stream()
                .filter(a -> a.getUsuario() != null
                          && usuarioActual.getUsername() != null
                          && usuarioActual.getUsername().equals(a.getUsuario().getUsername()))
                .collect(java.util.stream.Collectors.toList());
        return mias.isEmpty() ? todas : mias;
    }

    private List<AtencionAlerta> obtenerMisAtenciones() throws Exception {
        if (atencionService == null) return List.of();
        List<AtencionAlerta> todas = atencionService.listar();
        if (policiaActual == null || policiaActual.getUnidadpolicial() == null) return todas;
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
        card.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(card, Priority.ALWAYS);
        shadow(card);

        StackPane iconWrap = new StackPane();
        iconWrap.setPrefSize(52, 52);
        iconWrap.setMinSize(52, 52);
        iconWrap.setMaxSize(52, 52);
        Rectangle iconBg = new Rectangle(52, 52);
        iconBg.setArcWidth(16); iconBg.setArcHeight(16);
        iconBg.setFill(Color.web(bgIcon));
        Label iconLbl = new Label(iconFA);
        iconLbl.setStyle("-fx-font-family: 'Font Awesome 6 Free Solid'; -fx-font-size: 22px; -fx-text-fill: " + accentColor + ";");
        iconWrap.getChildren().addAll(iconBg, iconLbl);

        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #374151;");
        Label valLbl = new Label(String.valueOf(value));
        valLbl.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: " + accentColor + ";");
        Label subLbl = new Label(sub);
        subLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: " + GRAY_TEXT + ";");

        HBox topRow = new HBox(16, iconWrap, new VBox(3, titleLbl, valLbl, subLbl));
        topRow.setAlignment(Pos.CENTER_LEFT);
        card.getChildren().add(topRow);
        card.setOnMouseEntered(e -> card.setTranslateY(-3));
        card.setOnMouseExited(e  -> card.setTranslateY(0));
        return card;
    }

    private HBox alertItemMejorado(String iconFA, String titulo, String fecha,
            String estadoNombre, String colorEstado) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 0, 10, 0));
        row.setCursor(javafx.scene.Cursor.HAND);
        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 8;"));
        row.setOnMouseExited(e  -> row.setStyle("-fx-background-color: transparent;"));

        Circle dot = new Circle(5, Color.web(colorEstado));

        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(36, 36); iconBox.setMinSize(36, 36); iconBox.setMaxSize(36, 36);
        iconBox.setStyle("-fx-background-color: " + colorEstado + "22; -fx-background-radius: 9;");
        Label iconLbl = new Label(iconFA);
        iconLbl.setStyle("-fx-font-family: 'Font Awesome 6 Free Solid'; -fx-font-size: 15px; -fx-text-fill: " + colorEstado + ";");
        iconBox.getChildren().add(iconLbl);

        VBox texto = new VBox(3);
        HBox.setHgrow(texto, Priority.ALWAYS);
        texto.getChildren().add(label(titulo, 13, "#111827", false));

        HBox metaRow = new HBox(8);
        metaRow.setAlignment(Pos.CENTER_LEFT);
        Label badge = new Label(estadoNombre);
        badge.setStyle("-fx-font-size: 9px; -fx-font-weight: bold; -fx-text-fill: " + colorEstado
                + "; -fx-background-color: " + colorEstado + "22;"
                + "-fx-background-radius: 20; -fx-padding: 2 8 2 8;");
        metaRow.getChildren().addAll(label(fecha, 10, "#9ca3af", false), badge);
        texto.getChildren().add(metaRow);

        row.getChildren().addAll(dot, iconBox, texto, label("›", 15, "#d1d5db", false));
        return row;
    }

    private HBox atencionItemMejorado(String iconFA, String color, String estado, String descripcion) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 0, 10, 0));

        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(36, 36); iconBox.setMinSize(36, 36); iconBox.setMaxSize(36, 36);
        iconBox.setStyle("-fx-background-color: " + color + "22; -fx-background-radius: 9;");
        Label iconLbl = new Label(iconFA);
        iconLbl.setStyle("-fx-font-family: 'Font Awesome 6 Free Solid'; -fx-font-size: 15px; -fx-text-fill: " + color + ";");
        iconBox.getChildren().add(iconLbl);

        VBox texto = new VBox(4);
        HBox.setHgrow(texto, Priority.ALWAYS);
        Label badge = new Label(estado);
        badge.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: " + color
                + "; -fx-background-color: " + color + "22;"
                + "-fx-background-radius: 20; -fx-padding: 2 10 2 10;");
        Label descLbl = label(descripcion, 11, "#6b7280", false);
        descLbl.setWrapText(true);
        texto.getChildren().addAll(badge, descLbl);
        row.getChildren().addAll(iconBox, texto);
        return row;
    }

    private HBox alarmaItemMejorado(String iconFA, String color, String nombre, String estado) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 0, 10, 0));

        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(36, 36); iconBox.setMinSize(36, 36); iconBox.setMaxSize(36, 36);
        iconBox.setStyle("-fx-background-color: " + color + "22; -fx-background-radius: 9;");
        Label iconLbl = new Label(iconFA);
        iconLbl.setStyle("-fx-font-family: 'Font Awesome 6 Free Solid'; -fx-font-size: 15px; -fx-text-fill: " + color + ";");
        iconBox.getChildren().add(iconLbl);

        VBox texto = new VBox(3);
        HBox.setHgrow(texto, Priority.ALWAYS);
        texto.getChildren().add(label(nombre, 13, "#111827", false));
        Label badge = new Label(estado);
        badge.setStyle("-fx-font-size: 9px; -fx-font-weight: bold; -fx-text-fill: " + color
                + "; -fx-background-color: " + color + "22;"
                + "-fx-background-radius: 20; -fx-padding: 2 8 2 8;");
        texto.getChildren().add(badge);
        row.getChildren().addAll(iconBox, texto);
        return row;
    }

    private HBox placeholderVacio(String mensaje, String iconFA, String color) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER);
        row.setPadding(new Insets(20, 0, 12, 0));
        Label iconLbl = new Label(iconFA);
        iconLbl.setStyle("-fx-font-family: 'Font Awesome 6 Free Solid'; -fx-font-size: 18px; -fx-text-fill: " + color + "44;");
        row.getChildren().addAll(iconLbl, label(mensaje, 12, "#9ca3af", false));
        return row;
    }

    private String iconoTipoAlerta(Alerta a) { return "\uf071"; }

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
        if (estado == null) return GRAY_TEXT;
        return switch (estado) {
            case PENDIENTE, EN_ATENCION -> RED;
            case UNIDAD_ASIGNADA        -> ORANGE;
            case RESUELTA               -> GREEN;
            default                     -> GRAY_TEXT;
        };
    }

    private String formatFecha(LocalDateTime dt) {
        if (dt == null) return "—";
        long mins = java.time.Duration.between(dt, LocalDateTime.now()).toMinutes();
        if (mins < 1)    return "Hace un momento";
        if (mins < 60)   return "Hace " + mins + " min";
        if (mins < 1440) return "Hace " + (mins / 60) + " h";
        return dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
}