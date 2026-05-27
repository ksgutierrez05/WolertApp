/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.view;

/**
 *
 * @author Maria Cristina
 */
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;
import sistemagestion.model.Alerta;
import sistemagestion.model.EstadoAlerta;
import sistemagestion.model.Notificacion;
import sistemagestion.service.AlertaService;
import sistemagestion.service.NotificacionService;
import sistemagestion.service.UsuarioService;

public class AdministradorApp {

    // ── Colores principales ───────────────────────────────────────
    private static final String WHITE = "#ffffff";
    private static final String BG = "#f8fafc";
    private static final String RED = "#e53935";
    private static final String RED_LIGHT = "#fff0f0";
    private static final String ORANGE = "#fb8c00";
    private static final String GREEN = "#43a047";
    private static final String BLUE = "#1565c0";
    private static final String GRAY_TEXT = "#6b7280";
    private static final String BORDER = "#e5e7eb";

    // ── Colores de texto ──────────────────────────────────────────
    private static final String TEXT_PRIMARY = "#111827";
    private static final String TEXT_SECONDARY = "#374151";

    // ── Colores de estado de alerta ───────────────────────────────
    private static final String COLOR_EN_ATENCION = "#7b1fa2";
    private static final String COLOR_UNIDAD_ASIGNADA = "#0288d1";

    // ── Colores del sidebar ───────────────────────────────────────
    private static final String SIDEBAR_BG_START = "#16283d";
    private static final String SIDEBAR_BG_END = "#1f3a56";
    private static final String SIDEBAR_AVATAR_BG = "#334155";
    private static final String SIDEBAR_NAV_TEXT = "#8899bb";
    private static final String SIDEBAR_HOVER = "#ffffff18";
    private static final String SIDEBAR_ACTIVE = "#ffffff22";
    private static final String SIDEBAR_LOGOUT_HOVER = "#ffffff15";

    // ── Colores de fondo de iconos en stat cards ──────────────────
    private static final String ICON_BG_BLUE = "#e8f0fe";
    private static final String ICON_BG_RED = RED_LIGHT;
    private static final String ICON_BG_AMBER = "#fff8e1";
    private static final String ICON_BG_GREEN = "#e8f5e9";

    // ── Sombra ────────────────────────────────────────────────────
    private static final String SHADOW_COLOR = "#0000001a";

    // ── Dimensiones ───────────────────────────────────────────────
    private static final double SCENE_WIDTH = 1100;
    private static final double SCENE_HEIGHT = 620;
    private static final double SIDEBAR_WIDTH = 250;
    private static final double SIDEBAR_HEIGHT = 900;
    private static final double ICON_BOX_SM = 36;
    private static final double ICON_BOX_LG = 44;
    private static final double SEPARATOR_HEIGHT = 1;
    private static final double LOGOUT_ICON_SIZE = 70;
    private static final double PLACEHOLDER_MSG_SIZE = 18;

    // ── Límites de listas ─────────────────────────────────────────
    private static final int MAX_ALERTAS_PANEL = 4;
    private static final int MAX_NOTIFS_PANEL = 2;

    // ── Servicios ─────────────────────────────────────────────────
    private BorderPane root;
    private VBox nav;

    private AlertaService alertaService;
    private NotificacionService notificacionService;
    private UsuarioService usuarioService;

    public AdministradorApp() {
        try {
            alertaService = new AlertaService();
            notificacionService = new NotificacionService();
            usuarioService = new UsuarioService();
        } catch (Exception e) {
            mostrarAlerta("Error de conexión", e.getMessage());
        }
    }

    public void show(Stage stage) {
        root = new BorderPane();
        root.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);

        ScrollPane sidebarScroll = new ScrollPane(buildSidebar());
        sidebarScroll.setFitToWidth(true);
        sidebarScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sidebarScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        sidebarScroll.setStyle("""
            -fx-background: transparent;
            -fx-background-color: transparent;
            """);

        root.setLeft(sidebarScroll);
        root.setCenter(buildMainContent());

        Scene scene = new Scene(root, SCENE_WIDTH, SCENE_HEIGHT);
        stage.setTitle("WolertApp – Panel Administrativo");
        stage.setScene(scene);

        stage.setResizable(true);
        stage.setMaximized(true);
        stage.show();
    }

    public void start(Stage stage) {

        show(stage);
    }

    // ═══════════════════════════════════════════════════════════════
    // SIDEBAR
    // ═══════════════════════════════════════════════════════════════
    private VBox buildSidebar() {
        VBox sidebar = new VBox();
        sidebar.setPrefWidth(SIDEBAR_WIDTH);
        sidebar.setStyle("-fx-background-color: linear-gradient(to right, "
                + SIDEBAR_BG_START + ", " + SIDEBAR_BG_END + ");");

        // Logo
        HBox logoBox = new HBox(10);
        logoBox.setPadding(new Insets(20, 16, 20, 16));
        logoBox.setAlignment(Pos.CENTER_LEFT);

        ImageView logoImg = new ImageView(
                new Image(getClass().getResourceAsStream("/LogoWolertAPP.png")));
        logoImg.setFitWidth(65);
        logoImg.setFitHeight(65);
        logoImg.setPreserveRatio(true);
        logoImg.setTranslateY(-2);

        VBox logoText = new VBox(1);
        logoText.setAlignment(Pos.CENTER_LEFT);

        Label appName = label("WolertApp", 1, WHITE, true);
        Label appSub = label("Panel Administrativo", 9, SIDEBAR_NAV_TEXT, false);

        logoText.getChildren().addAll(appName, appSub);
        logoBox.getChildren().addAll(new StackPane(logoImg), logoText);

        // Tarjeta de admin
        HBox adminCard = new HBox(12);
        adminCard.setPadding(new Insets(12, 16, 12, 16));
        adminCard.setAlignment(Pos.CENTER_LEFT);
        adminCard.setStyle("-fx-background-color: rgba(255,255,255,0.08);"
                + "-fx-background-radius: 12;");

        Circle avatar = new Circle(20, Color.web(SIDEBAR_AVATAR_BG));
        Label avatarLbl = label("👨‍💼", 15, WHITE, false);
        StackPane avatarBox = new StackPane(avatar, avatarLbl);

        VBox adminInfo = new VBox(2);
        Label adminName = label("Administrador", 13, WHITE, true);
        HBox statusRow = new HBox(4);
        statusRow.setAlignment(Pos.CENTER_LEFT);
        statusRow.getChildren().addAll(
                new Circle(4, Color.web(GREEN)),
                label("Sistema activo", 10, GREEN, false));
        adminInfo.getChildren().addAll(adminName, statusRow);
        adminCard.getChildren().addAll(avatarBox, adminInfo);

        // Navegación
        nav = new VBox(2);
        nav.setPadding(new Insets(16, 8, 16, 8));
        nav.getChildren().addAll(
                navItem("🏠", "Dashboard"),
                navItem("👥", "Usuarios"),
                navItem("🚨", "Alertas"),
                navItem("🔔", "Alarmas"),
                navItem("📍", "Comunas"),
                navItem("🏘", "Barrios"),
                navItem("📋", "Tipos"),
                navItem("📊", "Reportes"),
                navItem("📈", "Estadísticas"),
                navItem("🔔", "Notificaciones"),
                navItem("⚙", "Configuración")
        );

        // Cerrar sesión
        HBox logout = new HBox(10);
        logout.setPadding(new Insets(10, 16, 10, 16));
        logout.setAlignment(Pos.CENTER_LEFT);
        logout.setCursor(javafx.scene.Cursor.HAND);
        logout.setOnMouseEntered(e -> logout.setStyle(
                "-fx-background-color: " + SIDEBAR_LOGOUT_HOVER
                + "; -fx-background-radius: 8;"));
        logout.setOnMouseExited(e -> logout.setStyle(
                "-fx-background-color: transparent;"));
        logout.setOnMouseClicked(e -> {
            VBox logoutView = new VBox(20);
            logoutView.setAlignment(Pos.CENTER);
            logoutView.setStyle("-fx-background-color: " + BG + ";");
            Label icon = new Label("👋");
            icon.setFont(Font.font(LOGOUT_ICON_SIZE));
            Label title = new Label("Sesión cerrada");
            title.setFont(Font.font("System", FontWeight.BOLD, 30));
            Label msg = new Label("Cerrando aplicación...");
            msg.setTextFill(Color.GRAY);
            logoutView.getChildren().addAll(icon, title, msg);
            root.setCenter(logoutView);
            new Timeline(new KeyFrame(Duration.seconds(2),
                    ev -> ((Stage) root.getScene().getWindow()).close())).play();
        });
        logout.getChildren().addAll(
                label("🚪", 14, RED, false),
                label("Cerrar sesión", 13, RED, false));

        VBox spacer = new VBox();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        sidebar.getChildren().addAll(logoBox, adminCard, nav, spacer, logout);
        sidebar.setFillWidth(true);

        VBox.setVgrow(sidebar, Priority.ALWAYS);

        sidebar.setMaxHeight(Double.MAX_VALUE);
        return sidebar;
    }

    private HBox navItem(String icon, String text) {
        HBox item = new HBox(10);
        item.setPadding(new Insets(10, 12, 10, 12));
        item.setAlignment(Pos.CENTER_LEFT);
        item.setCursor(javafx.scene.Cursor.HAND);
        item.setMaxWidth(Double.MAX_VALUE);
        item.setStyle("-fx-background-radius: 8;");

        item.setOnMouseEntered(e -> item.setStyle(
                "-fx-background-color: " + SIDEBAR_HOVER + "; -fx-background-radius: 8;"));
        item.setOnMouseExited(e -> item.setStyle("-fx-background-radius: 8;"));

        Label iconLbl = label(icon, 14, SIDEBAR_NAV_TEXT, false);
        Label textLbl = label(text, 13, SIDEBAR_NAV_TEXT, false);
        item.getChildren().addAll(iconLbl, textLbl);

        item.setOnMouseClicked(e -> {
            nav.getChildren().forEach(node -> node.setStyle("-fx-background-radius: 8;"));
            item.setStyle("-fx-background-color: " + SIDEBAR_ACTIVE
                    + "; -fx-background-radius: 8;");
            iconLbl.setTextFill(Color.WHITE);
            textLbl.setTextFill(Color.WHITE);

            switch (text) {
                case "Dashboard" ->
                    root.setCenter(buildMainContent());
                case "Usuarios" ->
                    root.setCenter(new UsuariosAdminView().getView());
                case "Alertas" ->
                    root.setCenter(new AlertasAdminView(alertaService).getView());
                case "Alarmas" ->
                    root.setCenter(new AlarmaAdminView().getView());
                case "Comunas" ->
                    root.setCenter(new ComunaAdminView().getView());
                case "Barrios" ->
                    root.setCenter(new BarrioAdminView().getView());
                case "Tipos" ->
                    root.setCenter(new TiposAdminView().getView());
                case "Estadísticas" ->
                    root.setCenter(new EstadisticasAdminView().getView());
                case "Notificaciones" ->
                    root.setCenter(new NotificacionesAdminView(notificacionService).getView());
                default ->
                    root.setCenter(buildPlaceholderView(text));
            }
        });

        return item;
    }

    // ═══════════════════════════════════════════════════════════════
    // MAIN CONTENT — dashboard inicio
    // ═══════════════════════════════════════════════════════════════
    private ScrollPane buildMainContent() {

        VBox content = new VBox(20);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color: " + BG + ";");

        content.setFillWidth(true);
        VBox.setVgrow(content, Priority.ALWAYS);

        content.getChildren().addAll(
                buildTopBar(),
                buildStats(),
                buildBottomPanels()
        );

        ScrollPane scroll = new ScrollPane(content);

        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true); // ← importante

        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        scroll.setStyle(
                "-fx-background-color: " + BG + ";"
                + "-fx-background: " + BG + ";"
        );

        return scroll;
    }

    // ── Top bar ───────────────────────────────────────────────────
    private HBox buildTopBar() {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);

        VBox greeting = new VBox(4);
        Label hello = new Label("Dashboard Administrativo");
        hello.setFont(Font.font("System", FontWeight.BOLD, 28));
        hello.setTextFill(Color.web(TEXT_PRIMARY));
        greeting.getChildren().addAll(
                hello,
                label("Panel de control y monitoreo del sistema", 13, GRAY_TEXT, false));

        HBox rightBox = new HBox(16);
        rightBox.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(rightBox, Priority.ALWAYS);

        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern(
                "d 'de' MMMM 'de' yyyy", new Locale("es", "CO"));
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern(
                "hh:mm:ss a", new Locale("es", "CO"));

        LocalDateTime now0 = LocalDateTime.now(ZoneId.of("America/Bogota"));
        Label dateLbl = label("📅  " + now0.format(dateFmt), 13, TEXT_SECONDARY, false);
        Label timeLbl = label(now0.format(timeFmt), 13, GRAY_TEXT, false);

        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), ev -> {
            LocalDateTime now = LocalDateTime.now(ZoneId.of("America/Bogota"));
            dateLbl.setText("📅  " + now.format(dateFmt));
            timeLbl.setText(now.format(timeFmt));
        }));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();

        VBox dateBox = new VBox(2);
        dateBox.setAlignment(Pos.CENTER_RIGHT);
        dateBox.getChildren().addAll(dateLbl, timeLbl);

        long totalActivas = contarAlertasActivas();
        StackPane bell = new StackPane();
        Label bellIcon = label("🔔", 20, TEXT_SECONDARY, false);
        Circle badge = new Circle(8, Color.web(RED));
        badge.setTranslateX(10);
        badge.setTranslateY(-10);
        Label badgeNum = label(String.valueOf(totalActivas), 8, WHITE, true);
        badgeNum.setTranslateX(10);
        badgeNum.setTranslateY(-10);
        bell.getChildren().addAll(bellIcon, badge, badgeNum);

        rightBox.getChildren().addAll(dateBox, bell);
        bar.getChildren().addAll(greeting, rightBox);
        return bar;
    }

    // ── Stats ─────────────────────────────────────────────────────
    private HBox buildStats() {
        HBox row = new HBox(20);
        row.setFillHeight(true);

        HBox.setHgrow(row, Priority.ALWAYS);

        long totalUsuarios = contarUsuarios();
        long alertasActivas = contarAlertasActivas();
        long incidentes = contarIncidentes();
        long resueltas = contarAlertasResueltas();

        row.getChildren().addAll(
                statCard("👥", ICON_BG_BLUE, BLUE, "Usuarios registrados",
                        String.valueOf(totalUsuarios), "Total en el sistema", BLUE),
                statCard("🚨", ICON_BG_RED, RED, "Alertas activas",
                        String.valueOf(alertasActivas), "En este momento", RED),
                statCard("📈", ICON_BG_AMBER, ORANGE, "Incidentes",
                        String.valueOf(incidentes), "Pendientes de atención", GRAY_TEXT),
                statCard("✅", ICON_BG_GREEN, GREEN, "Resueltos",
                        String.valueOf(resueltas), "Total histórico", GREEN)
        );
        return row;
    }

    // ── Paneles: alertas recientes + actividad reciente ───────────
    private HBox buildBottomPanels() {
        HBox bottom = new HBox(20);
        HBox.setHgrow(bottom, Priority.ALWAYS);

        bottom.setFillHeight(true);

        VBox alertsPanel = createPanel("🚨 Alertas recientes");
        VBox notifPanel = createPanel("🔔 Actividad reciente");

        List<Alerta> alertas = cargarAlertas();

        // Panel izquierdo — últimas alertas
        List<Alerta> ultimas = alertas.stream()
                .sorted((a, b) -> {
                    if (a.getFechaHora() == null || b.getFechaHora() == null) {
                        return 0;
                    }
                    return b.getFechaHora().compareTo(a.getFechaHora());
                })
                .limit(MAX_ALERTAS_PANEL)
                .collect(Collectors.toList());

        if (ultimas.isEmpty()) {
            alertsPanel.getChildren().add(
                    label("Sin alertas registradas.", 13, GRAY_TEXT, false));
        } else {
            boolean primero = true;
            for (Alerta a : ultimas) {
                if (!primero) {
                    alertsPanel.getChildren().add(separator());
                }
                primero = false;
                String titulo = a.getTipoalerta() != null
                        ? a.getTipoalerta().getNombre() : "Alerta";
                String barrio = a.getBarrio() != null
                        ? a.getBarrio().getNombre() : "—";
                String usuario = a.getUsuario() != null
                        ? a.getUsuario().getPrimer_nombre() : "—";
                alertsPanel.getChildren().add(
                        alertItem(iconAlerta(a),
                                titulo + " en " + barrio,
                                "Reportado por " + usuario,
                                colorEstado(a.getEstado())));
            }
        }

        // Panel derecho — actividad reciente + notificaciones
        List<Alerta> recientes = alertas.stream()
                .sorted((a, b) -> {
                    if (a.getFechaHora() == null || b.getFechaHora() == null) {
                        return 0;
                    }
                    return b.getFechaHora().compareTo(a.getFechaHora());
                })
                .limit(MAX_ALERTAS_PANEL)
                .collect(Collectors.toList());

        List<Notificacion> notifs = cargarNotificaciones();

        if (recientes.isEmpty() && notifs.isEmpty()) {
            notifPanel.getChildren().add(
                    label("Sin actividad reciente.", 13, GRAY_TEXT, false));
        } else {
            boolean primeroA = true;
            for (Alerta a : recientes) {
                if (!primeroA) {
                    notifPanel.getChildren().add(separator());
                }
                primeroA = false;
                String tipo = a.getTipoalerta() != null
                        ? a.getTipoalerta().getNombre() : "Alerta";
                String barrio = a.getBarrio() != null
                        ? a.getBarrio().getNombre() : "—";
                String fecha = a.getFechaHora() != null
                        ? a.getFechaHora().toLocalDate().toString() : "—";
                notifPanel.getChildren().add(
                        listItem("🚨", tipo + " — " + barrio, fecha,
                                colorEstado(a.getEstado())));
            }

            List<Notificacion> ultimasNotifs = notifs.stream()
                    .limit(MAX_NOTIFS_PANEL)
                    .collect(Collectors.toList());

            for (Notificacion n : ultimasNotifs) {
                notifPanel.getChildren().add(separator());
                String mensaje = n.getMensaje() != null ? n.getMensaje() : "—";
                String dest = n.getCorreodestinatario() != null
                        ? n.getCorreodestinatario() : "—";
                String fecha = n.getFechahora() != null
                        ? n.getFechahora().toLocalDate().toString() : "—";
                notifPanel.getChildren().add(
                        listItem("🔔",
                                mensaje.length() > 40
                                ? mensaje.substring(0, 40) + "…" : mensaje,
                                dest + " · " + fecha,
                                BLUE));
            }
        }

        HBox.setHgrow(alertsPanel, Priority.ALWAYS);
        HBox.setHgrow(notifPanel, Priority.ALWAYS);
        bottom.getChildren().addAll(alertsPanel, notifPanel);
        return bottom;
    }

    // ═══════════════════════════════════════════════════════════════
    // HELPERS — consultas a servicios
    // ═══════════════════════════════════════════════════════════════
    private List<Alerta> cargarAlertas() {
        if (alertaService == null) {
            return List.of();
        }
        try {
            return alertaService.listar();
        } catch (Exception e) {
            return List.of();
        }
    }

    private List<Notificacion> cargarNotificaciones() {
        if (notificacionService == null) {
            return List.of();
        }
        try {
            return notificacionService.listar();
        } catch (Exception e) {
            return List.of();
        }
    }

    private long contarUsuarios() {
        if (usuarioService == null) {
            return 0;
        }
        try {
            return usuarioService.listar().size();
        } catch (Exception e) {
            return 0;
        }
    }

    private long contarAlertasActivas() {
        return cargarAlertas().stream()
                .filter(a -> a.getEstado() == EstadoAlerta.PENDIENTE
                || a.getEstado() == EstadoAlerta.RECIBIDA
                || a.getEstado() == EstadoAlerta.EN_ATENCION
                || a.getEstado() == EstadoAlerta.UNIDAD_ASIGNADA)
                .count();
    }

    private long contarIncidentes() {
        return cargarAlertas().stream()
                .filter(a -> a.getEstado() == EstadoAlerta.EN_ATENCION
                || a.getEstado() == EstadoAlerta.UNIDAD_ASIGNADA)
                .count();
    }

    private long contarAlertasResueltas() {
        return cargarAlertas().stream()
                .filter(a -> a.getEstado() == EstadoAlerta.RESUELTA)
                .count();
    }

    // ── Helpers de color/estado ───────────────────────────────────
    private String colorEstado(EstadoAlerta estado) {
        if (estado == null) {
            return GRAY_TEXT;
        }
        return switch (estado) {
            case PENDIENTE ->
                ORANGE;
            case RECIBIDA ->
                BLUE;
            case EN_ATENCION ->
                COLOR_EN_ATENCION;
            case UNIDAD_ASIGNADA ->
                COLOR_UNIDAD_ASIGNADA;
            case RESUELTA ->
                GREEN;
            case CANCELADA ->
                GRAY_TEXT;
        };
    }

    private String iconAlerta(Alerta a) {
        if (a.getTipoalerta() == null) {
            return "🚨";
        }
        String tipo = a.getTipoalerta().getNombre().toUpperCase();
        if (tipo.contains("ROB")) {
            return "🦹";
        }
        if (tipo.contains("PELEA") || tipo.contains("VIOLENCIA")) {
            return "⚔";
        }
        if (tipo.contains("ANIMAL")) {
            return "🐕";
        }
        if (tipo.contains("LUZ") || tipo.contains("INFRA")) {
            return "💡";
        }
        return "🚨";
    }

    // ═══════════════════════════════════════════════════════════════
    // HELPERS UI
    // ═══════════════════════════════════════════════════════════════
    private HBox alertItem(String icon, String title, String sub, String dotColor) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 0, 8, 0));
        row.setCursor(javafx.scene.Cursor.HAND);

        Circle dotCircle = new Circle(5, Color.web(dotColor));

        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(ICON_BOX_SM, ICON_BOX_SM);
        iconBox.setMinSize(ICON_BOX_SM, ICON_BOX_SM);
        iconBox.setMaxSize(ICON_BOX_SM, ICON_BOX_SM);
        iconBox.setStyle("-fx-background-color: " + BG + "; -fx-background-radius: 8;");
        iconBox.getChildren().add(label(icon, 16, dotColor, false));

        VBox text = new VBox(2);
        HBox.setHgrow(text, Priority.ALWAYS);
        text.getChildren().addAll(
                label(title, 13, TEXT_PRIMARY, false),
                label(sub, 11, GRAY_TEXT, false));

        row.getChildren().addAll(dotCircle, iconBox, text, label(">", 14, GRAY_TEXT, false));
        return row;
    }

    private HBox listItem(String icon, String title, String sub, String iconColor) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 0, 8, 0));

        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(ICON_BOX_SM, ICON_BOX_SM);
        iconBox.setMinSize(ICON_BOX_SM, ICON_BOX_SM);
        iconBox.setMaxSize(ICON_BOX_SM, ICON_BOX_SM);
        iconBox.setStyle("-fx-background-color: " + BG + "; -fx-background-radius: 8;");
        iconBox.getChildren().add(label(icon, 16, iconColor, false));

        VBox text = new VBox(2);
        HBox.setHgrow(text, Priority.ALWAYS);
        text.getChildren().addAll(
                label(title, 13, TEXT_PRIMARY, false),
                label(sub, 11, GRAY_TEXT, false));

        row.getChildren().addAll(iconBox, text);
        return row;
    }

    private VBox statCard(
            String icon,
            String bgIcon,
            String iconColor,
            String title,
            String value,
            String sub,
            String subColor) {

        VBox card = new VBox(10);

        card.setPadding(new Insets(18));

        card.setStyle("""
    -fx-background-color: white;
    -fx-background-radius: 16;
    -fx-border-radius: 16;
    """);

        card.setMinHeight(100);
        card.setPrefHeight(100);

        card.setMaxWidth(Double.MAX_VALUE);

        HBox.setHgrow(card, Priority.ALWAYS);

        shadow(card);

        StackPane iconBox = new StackPane();

        iconBox.setPrefSize(44, 44);

        iconBox.setMinSize(44, 44);

        iconBox.setMaxSize(44, 44);

        iconBox.setStyle("""
    -fx-background-color: %s;
    -fx-background-radius: 14;
    """.formatted(bgIcon));

        Label iconLbl = label(icon, 20, iconColor, false);

        iconBox.getChildren().add(iconLbl);

        // Título
        Label titleLbl = label(title, 13, GRAY_TEXT, false);

        // Valor grande
        Label valueLbl = new Label(value);

        valueLbl.setFont(Font.font("System", FontWeight.BOLD, 20));

        valueLbl.setTextFill(Color.web(TEXT_PRIMARY));

        // Texto inferior
        Label subLbl = label(sub, 12, subColor, false);

        VBox textBox = new VBox(6);

        textBox.getChildren().addAll(
                titleLbl,
                valueLbl,
                subLbl
        );

        HBox top = new HBox(14);

        top.setAlignment(Pos.CENTER_LEFT);

        top.getChildren().addAll(iconBox, textBox);

        card.getChildren().add(top);

        // Hover
        card.setOnMouseEntered(e -> {
            card.setTranslateY(-3);
        });

        card.setOnMouseExited(e -> {
            card.setTranslateY(0);
        });

        return card;
    }

    private VBox createPanel(String title) {
        VBox panel = new VBox(12);
        panel.setPadding(new Insets(20));
        panel.setStyle("""
    -fx-background-color: white;
    -fx-background-radius: 16;
    """);
        HBox.setHgrow(panel, Priority.ALWAYS);
        panel.setMaxWidth(Double.MAX_VALUE);

        panel.setMaxHeight(Double.MAX_VALUE);
        shadow(panel);

        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getChildren().add(label(title, 15, TEXT_PRIMARY, true));
        panel.getChildren().addAll(header, separator());
        return panel;
    }

    private ScrollPane buildPlaceholderView(String nombre) {
        VBox box = new VBox(20);
        box.setPadding(new Insets(40));
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: " + BG + ";");

        Label icon = new Label("🚧");
        icon.setFont(Font.font(LOGOUT_ICON_SIZE));
        Label title = new Label(nombre);
        title.setFont(Font.font("System", FontWeight.BOLD, 32));
        title.setTextFill(Color.web(TEXT_PRIMARY));
        Label msg = new Label("Pantalla en construcción");
        msg.setFont(Font.font(PLACEHOLDER_MSG_SIZE));
        msg.setTextFill(Color.GRAY);

        Button volver = new Button("Volver al Dashboard");
        volver.setStyle("-fx-background-color: " + BLUE + "; -fx-text-fill: " + WHITE + ";"
                + "-fx-font-size: 14px; -fx-background-radius: 10;"
                + "-fx-padding: 10 20 10 20; -fx-cursor: hand;");
        volver.setOnAction(e -> root.setCenter(buildMainContent()));
        box.getChildren().addAll(icon, title, msg, volver);

        ScrollPane scroll = new ScrollPane(box);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: " + BG + "; -fx-background-color: " + BG + ";");
        return scroll;
    }

    private Label label(String text, double size, String color, boolean bold) {
        Label lbl = new Label(text);
        lbl.setFont(bold
                ? Font.font("System", FontWeight.BOLD, size)
                : Font.font("System", size));
        lbl.setTextFill(Color.web(color));
        return lbl;
    }

    private Region separator() {
        Region sep = new Region();
        sep.setPrefHeight(SEPARATOR_HEIGHT);
        sep.setStyle("-fx-background-color: " + BORDER + ";");
        return sep;
    }

    private void shadow(Region node) {

        DropShadow shadow = new DropShadow();

        shadow.setRadius(15);

        shadow.setOffsetY(3);

        shadow.setColor(Color.rgb(15, 23, 42, 0.08));

        node.setEffect(shadow);
    }

    private void mostrarAlerta(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
