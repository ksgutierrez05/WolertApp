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
import java.util.Map;
import java.util.stream.Collectors;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;
import sistemagestion.model.Alerta;
import sistemagestion.model.EstadoAlerta;
import sistemagestion.model.Notificacion;
import sistemagestion.model.Usuario;
import sistemagestion.service.AlertaService;
import sistemagestion.service.NotificacionService;
import sistemagestion.service.UsuarioService;

public class AdministradorApp {

    private static final String WHITE = "#ffffff";
    private static final String BG = "#f4f6fb";
    private static final String RED = "#e53935";
    private static final String RED_LIGHT = "#fff0f0";
    private static final String ORANGE = "#fb8c00";
    private static final String GREEN = "#43a047";
    private static final String BLUE = "#1565c0";
    private static final String GRAY_TEXT = "#6b7280";
    private static final String BORDER = "#e5e7eb";

    private BorderPane root;
    private VBox nav;

    // ── Servicios ────────────────────────────────────────────────
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

        Scene scene = new Scene(root, 1100, 620);
        stage.setTitle("WolertApp – Panel Administrativo");
        stage.setScene(scene);
        stage.show();
    }

    // ── Alias para compatibilidad ─────────────────────────────────
    public void start(Stage stage) {
        show(stage);
    }

    // ═══════════════════════════════════════════════════════════════
    // SIDEBAR
    // ═══════════════════════════════════════════════════════════════
    private VBox buildSidebar() {
        VBox sidebar = new VBox();
        sidebar.setPrefWidth(240);
        sidebar.setStyle("-fx-background-color: linear-gradient(to right, #16283d, #1f3a56);");

        HBox logoBox = new HBox(10);
        logoBox.setPadding(new Insets(20, 16, 20, 16));
        logoBox.setAlignment(Pos.CENTER_LEFT);

        ImageView logoImg = new ImageView(
                new Image(getClass().getResourceAsStream("/LogoWolertAPP.png"))
        );

        logoImg.setFitWidth(80);
        logoImg.setFitHeight(80);
        logoImg.setPreserveRatio(true);

        StackPane wolfIcon = new StackPane(logoImg);

        VBox logoText = new VBox(2);
        Label appName = label("WolertApp", 16, WHITE, true);
        Label appSub = label("Panel Administrativo", 9, "#8899bb", false);
        logoText.getChildren().addAll(appName, appSub);
        logoBox.getChildren().addAll(wolfIcon, logoText);

        HBox adminCard = new HBox(10);
        adminCard.setPadding(new Insets(12, 16, 12, 16));
        adminCard.setAlignment(Pos.CENTER_LEFT);
        adminCard.setStyle("""
            -fx-background-color: rgba(255,255,255,0.08);
            -fx-background-radius: 12;
            """);

        Circle avatar = new Circle(20, Color.web("#334155"));
        Label avatarLbl = label("👨‍💼", 15, WHITE, false);
        StackPane avatarBox = new StackPane(avatar, avatarLbl);

        VBox adminInfo = new VBox(2);
        Label adminName = label("Administrador", 13, WHITE, true);
        HBox statusRow = new HBox(4);
        statusRow.setAlignment(Pos.CENTER_LEFT);
        Circle onlineDot = new Circle(4, Color.web(GREEN));
        Label statusLbl = label("Sistema activo", 10, GREEN, false);
        statusRow.getChildren().addAll(onlineDot, statusLbl);
        adminInfo.getChildren().addAll(adminName, statusRow);
        adminCard.getChildren().addAll(avatarBox, adminInfo);

        nav = new VBox(2);
        nav.setPadding(new Insets(16, 8, 16, 8));
        nav.getChildren().addAll(
                navItem("🏠", "Dashboard"),
                navItem("👥", "Usuarios"),
                navItem("🚨", "Alertas"),
                navItem("📍", "Comunas"),
                navItem("🏘", "Barrios"),
                navItem("📋", "Tipos"),
                navItem("📊", "Reportes"),
                navItem("📈", "Estadísticas"),
                navItem("🔔", "Notificaciones"),
                navItem("⚙", "Configuración")
        );

        HBox logout = new HBox(10);
        logout.setPadding(new Insets(10, 16, 10, 16));
        logout.setAlignment(Pos.CENTER_LEFT);
        logout.setCursor(javafx.scene.Cursor.HAND);
        logout.setOnMouseEntered(e -> logout.setStyle(
                "-fx-background-color: #ffffff15; -fx-background-radius: 8;"));
        logout.setOnMouseExited(e -> logout.setStyle(
                "-fx-background-color: transparent;"));
        logout.setOnMouseClicked(e -> {
            VBox logoutView = new VBox(20);
            logoutView.setAlignment(Pos.CENTER);
            logoutView.setStyle("-fx-background-color: #f4f6fb;");
            Label icon = new Label("👋");
            icon.setFont(Font.font(70));
            Label title = new Label("Sesión cerrada");
            title.setFont(Font.font("System", FontWeight.BOLD, 30));
            Label msg = new Label("Cerrando aplicación...");
            msg.setTextFill(Color.GRAY);
            logoutView.getChildren().addAll(icon, title, msg);
            root.setCenter(logoutView);
            new Timeline(new KeyFrame(Duration.seconds(2),
                    ev -> ((Stage) root.getScene().getWindow()).close())).play();
        });
        Label logoutIcon = label("🚪", 14, RED, false);
        Label logoutLbl = label("Cerrar sesión", 13, RED, false);
        logout.getChildren().addAll(logoutIcon, logoutLbl);

        VBox spacer = new VBox();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        sidebar.getChildren().addAll(logoBox, adminCard, nav, spacer, logout);
        sidebar.setPrefHeight(900);
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
                "-fx-background-color: #ffffff18; -fx-background-radius: 8;"));
        item.setOnMouseExited(e -> item.setStyle("-fx-background-radius: 8;"));

        Label iconLbl = label(icon, 14, "#8899bb", false);
        Label textLbl = label(text, 13, "#8899bb", false);
        item.getChildren().addAll(iconLbl, textLbl);

        item.setOnMouseClicked(e -> {
            nav.getChildren().forEach(node -> node.setStyle("-fx-background-radius: 8;"));
            item.setStyle("-fx-background-color: #ffffff22; -fx-background-radius: 8;");
            iconLbl.setTextFill(Color.WHITE);
            textLbl.setTextFill(Color.WHITE);

            switch (text) {
                case "Dashboard" ->
                    root.setCenter(buildMainContent());
                case "Usuarios" ->
                    root.setCenter(new UsuariosAdminView().getView());
                case "Alertas" ->
                    root.setCenter(new AlertasAdminView(alertaService).getView());
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
    // MAIN CONTENT
    // ═══════════════════════════════════════════════════════════════
    private ScrollPane buildMainContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color: " + BG + ";");

        content.getChildren().addAll(
                buildTopBar(),
                buildStats(),
                buildCenterPanels(),
                buildBottomPanels()
        );

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: " + BG + "; -fx-background: " + BG + ";");
        return scroll;
    }

    // ── Top bar ───────────────────────────────────────────────────
    private HBox buildTopBar() {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);

        VBox greeting = new VBox(4);
        Label hello = new Label("Dashboard Administrativo");
        hello.setFont(Font.font("System", FontWeight.BOLD, 28));
        hello.setTextFill(Color.web("#111827"));
        Label sub = label("Panel de control y monitoreo del sistema", 13, GRAY_TEXT, false);
        greeting.getChildren().addAll(hello, sub);

        HBox rightBox = new HBox(16);
        rightBox.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(rightBox, Priority.ALWAYS);

        VBox dateBox = new VBox(2);
        dateBox.setAlignment(Pos.CENTER_RIGHT);

        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern(
                "d 'de' MMMM 'de' yyyy", new Locale("es", "CO"));
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern(
                "hh:mm:ss a", new Locale("es", "CO"));

        LocalDateTime now0 = LocalDateTime.now(ZoneId.of("America/Bogota"));
        Label dateLbl = label("📅  " + now0.format(dateFmt), 13, "#374151", false);
        Label timeLbl = label(now0.format(timeFmt), 13, GRAY_TEXT, false);

        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), ev -> {
            LocalDateTime now = LocalDateTime.now(ZoneId.of("America/Bogota"));
            dateLbl.setText("📅  " + now.format(dateFmt));
            timeLbl.setText(now.format(timeFmt));
        }));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();

        dateBox.getChildren().addAll(dateLbl, timeLbl);

        // Campana — badge con total de alertas activas reales
        long totalActivas = contarAlertasActivas();
        StackPane bell = new StackPane();
        Label bellIcon = label("🔔", 20, "#374151", false);
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

    // ── Stats — datos reales desde servicios ─────────────────────
    private HBox buildStats() {
        HBox row = new HBox(16);

        long totalUsuarios = contarUsuarios();
        long alertasActivas = contarAlertasActivas();
        long incidentes = contarIncidentes();
        long resueltas = contarAlertasResueltas();

        row.getChildren().addAll(
                statCard("👥", "#e8f0fe", BLUE, "Usuarios registrados",
                        String.valueOf(totalUsuarios), "Total en el sistema", BLUE),
                statCard("🚨", RED_LIGHT, RED, "Alertas activas",
                        String.valueOf(alertasActivas), "En este momento", RED),
                statCard("📈", "#fff8e1", ORANGE, "Incidentes",
                        String.valueOf(incidentes), "Pendientes de atención", GRAY_TEXT),
                statCard("✅", "#e8f5e9", GREEN, "Resueltos",
                        String.valueOf(resueltas), "Total histórico", GREEN)
        );
        return row;
    }

    // ── Paneles centrales ─────────────────────────────────────────
    private HBox buildCenterPanels() {
        HBox row = new HBox(16);
        row.getChildren().addAll(
                buildAlertsPanel(),
                buildChartPanel(),
                buildMapPanel()
        );
        return row;
    }

    // Alertas recientes — datos reales
    private VBox buildAlertsPanel() {
        VBox card = createPanel("🚨 Alertas recientes");

        List<Alerta> alertas = cargarAlertas();

        if (alertas.isEmpty()) {
            card.getChildren().add(label("Sin alertas registradas.", 13, GRAY_TEXT, false));
            return card;
        }

        // Mostrar las últimas 4
        List<Alerta> ultimas = alertas.stream()
                .sorted((a, b) -> {
                    if (a.getFechaHora() == null || b.getFechaHora() == null) {
                        return 0;
                    }
                    return b.getFechaHora().compareTo(a.getFechaHora());
                })
                .limit(4)
                .collect(Collectors.toList());

        boolean primero = true;
        for (Alerta a : ultimas) {
            if (!primero) {
                card.getChildren().add(separator());
            }
            primero = false;

            String dot = dotColor(a.getEstado());
            String icon = iconAlerta(a);
            String titulo = a.getTipoalerta() != null
                    ? a.getTipoalerta().getNombre() : "Alerta";
            String barrio = a.getBarrio() != null
                    ? a.getBarrio().getNombre() : "—";
            String usuario = a.getUsuario() != null
                    ? a.getUsuario().getPrimer_nombre() : "—";
            String colorDot = colorEstado(a.getEstado());

            card.getChildren().add(
                    alertItem(dot, icon, titulo + " en " + barrio,
                            "Reportado por " + usuario, colorDot)
            );
        }
        return card;
    }

    // Gráfico por tipo de alerta — datos reales
    private VBox buildChartPanel() {
        VBox card = createPanel("📊 Alertas por categoría");

        List<Alerta> alertas = cargarAlertas();

        PieChart chart = new PieChart();

        if (alertas.isEmpty()) {
            chart.getData().add(new PieChart.Data("Sin datos", 1));
        } else {
            Map<String, Long> porTipo = alertas.stream()
                    .filter(a -> a.getTipoalerta() != null)
                    .collect(Collectors.groupingBy(
                            a -> a.getTipoalerta().getNombre(),
                            Collectors.counting()
                    ));

            porTipo.forEach((tipo, cantidad)
                    -> chart.getData().add(new PieChart.Data(tipo, cantidad)));
        }

        chart.setPrefHeight(220);
        chart.setLegendVisible(true);
        card.getChildren().add(chart);
        return card;
    }

    // Mapa — estructura visual fija, los puntos sí son reales
    private VBox buildMapPanel() {
        VBox card = createPanel("📍 Mapa de alertas");

        StackPane mapArea = new StackPane();
        mapArea.setPrefHeight(220);

        Rectangle mapBg = new Rectangle();
        mapBg.setFill(Color.web("#d1e8d1"));
        mapBg.widthProperty().bind(mapArea.widthProperty());
        mapBg.heightProperty().bind(mapArea.heightProperty());
        mapBg.setArcWidth(10);
        mapBg.setArcHeight(10);

        Pane streets = new Pane();
        streets.setPrefSize(320, 200);
        for (int i = 0; i < 4; i++) {
            Rectangle h = new Rectangle(320, 3);
            h.setFill(Color.web("#b8d4b8"));
            h.setY(40 + i * 45);
            streets.getChildren().add(h);
        }
        for (int i = 0; i < 5; i++) {
            Rectangle v = new Rectangle(3, 200);
            v.setFill(Color.web("#b8d4b8"));
            v.setX(40 + i * 60);
            streets.getChildren().add(v);
        }

        // Puntos basados en alertas reales (distribuidos en la grilla visual)
        List<Alerta> alertas = cargarAlertas();
        int total = alertas.size();
        for (int i = 0; i < Math.min(total, 5); i++) {
            Alerta a = alertas.get(i);
            double x = 60 + (i % 5) * 55.0;
            double y = 50 + (i % 3) * 50.0;
            streets.getChildren().add(
                    mapDot(x, y, colorEstado(a.getEstado()))
            );
        }

        VBox popup = new VBox(6);
        popup.setPadding(new Insets(10, 14, 10, 14));
        popup.setStyle("-fx-background-color: white; -fx-background-radius: 8;");
        popup.setEffect(new DropShadow(8, Color.web("#0000001a")));
        popup.setTranslateX(20);
        popup.setTranslateY(10);
        Label mapTitle = label("Mapa interactivo", 12, "#374151", true);
        Label mapSub = label("Zona Centro", 11, BLUE, false);
        Button openMap = new Button("Ver alertas ↗");
        openMap.setStyle(
                "-fx-background-color: " + BLUE + ";"
                + "-fx-text-fill: white; -fx-font-size: 11px;"
                + "-fx-background-radius: 6; -fx-padding: 6 12 6 12; -fx-cursor: hand;");
        openMap.setOnAction(e -> root.setCenter(new AlertasAdminView(alertaService).getView()));
        popup.getChildren().addAll(mapTitle, mapSub, openMap);

        mapArea.getChildren().addAll(mapBg, streets, popup);

        HBox legend = new HBox(16);
        legend.setAlignment(Pos.CENTER);
        legend.getChildren().addAll(
                legendItem(RED, "Activo"),
                legendItem(ORANGE, "En revisión"),
                legendItem(GREEN, "Resuelto")
        );

        card.getChildren().addAll(mapArea, legend);
        return card;
    }

    // ── Paneles inferiores ────────────────────────────────────────
    private HBox buildBottomPanels() {
        HBox bottom = new HBox(16);

        VBox activity = createPanel("📋 Actividad reciente");
        VBox notifications = createPanel("🔔 Notificaciones del sistema");

        // Actividad — últimas alertas como feed
        List<Alerta> alertas = cargarAlertas();
        List<Alerta> recientes = alertas.stream()
                .sorted((a, b) -> {
                    if (a.getFechaHora() == null || b.getFechaHora() == null) {
                        return 0;
                    }
                    return b.getFechaHora().compareTo(a.getFechaHora());
                })
                .limit(4)
                .collect(Collectors.toList());

        boolean primerA = true;
        for (Alerta a : recientes) {
            if (!primerA) {
                activity.getChildren().add(separator());
            }
            primerA = false;
            String tipo = a.getTipoalerta() != null
                    ? a.getTipoalerta().getNombre() : "Alerta";
            String barrio = a.getBarrio() != null
                    ? a.getBarrio().getNombre() : "—";
            String fecha = a.getFechaHora() != null
                    ? a.getFechaHora().toLocalDate().toString() : "—";
            activity.getChildren().add(
                    listItem("🚨", tipo + " — " + barrio, fecha, colorEstado(a.getEstado()))
            );
        }
        if (recientes.isEmpty()) {
            activity.getChildren().add(label("Sin actividad reciente.", 13, GRAY_TEXT, false));
        }

        // Notificaciones reales
        List<Notificacion> notifs = cargarNotificaciones();
        List<Notificacion> ultimasNotifs = notifs.stream().limit(4).collect(Collectors.toList());

        boolean primerN = true;
        for (Notificacion n : ultimasNotifs) {
            if (!primerN) {
                notifications.getChildren().add(separator());
            }
            primerN = false;
            String mensaje = n.getMensaje() != null ? n.getMensaje() : "—";
            String dest = n.getCorreodestinatario() != null
                    ? n.getCorreodestinatario() : "—";
            String fecha = n.getFechahora() != null
                    ? n.getFechahora().toLocalDate().toString() : "—";
            notifications.getChildren().add(
                    listItem("🔔", mensaje.length() > 40
                            ? mensaje.substring(0, 40) + "…" : mensaje,
                            dest + " · " + fecha, BLUE)
            );
        }
        if (ultimasNotifs.isEmpty()) {
            notifications.getChildren().add(
                    label("Sin notificaciones.", 13, GRAY_TEXT, false));
        }

        HBox.setHgrow(activity, Priority.ALWAYS);
        HBox.setHgrow(notifications, Priority.ALWAYS);
        bottom.getChildren().addAll(activity, notifications);
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
                "#7b1fa2";
            case UNIDAD_ASIGNADA ->
                "#0288d1";
            case RESUELTA ->
                GREEN;
            case CANCELADA ->
                GRAY_TEXT;
        };
    }

    private String dotColor(EstadoAlerta estado) {
        if (estado == null) {
            return "⚪";
        }
        return switch (estado) {
            case PENDIENTE ->
                "🟠";
            case RECIBIDA ->
                "🔵";
            case EN_ATENCION ->
                "🟣";
            case UNIDAD_ASIGNADA ->
                "🔵";
            case RESUELTA ->
                "🟢";
            case CANCELADA ->
                "⚪";
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
    // HELPERS UI — reutilizados de la versión original
    // ═══════════════════════════════════════════════════════════════
    private HBox alertItem(String dot, String icon, String title,
            String sub, String dotColor) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 0, 8, 0));
        row.setCursor(javafx.scene.Cursor.HAND);

        Circle dotCircle = new Circle(5, Color.web(dotColor));

        StackPane iconBox = new StackPane();
        Rectangle bg = new Rectangle(36, 36);
        bg.setArcWidth(8);
        bg.setArcHeight(8);
        bg.setFill(Color.web(BG));
        Label iconLbl = label(icon, 16, dotColor, false);
        iconBox.getChildren().addAll(bg, iconLbl);

        VBox text = new VBox(2);
        HBox.setHgrow(text, Priority.ALWAYS);
        Label titleLbl = label(title, 13, "#111827", false);
        Label subLbl = label(sub, 11, GRAY_TEXT, false);
        text.getChildren().addAll(titleLbl, subLbl);

        Label arrow = label(">", 14, GRAY_TEXT, false);
        row.getChildren().addAll(dotCircle, iconBox, text, arrow);
        return row;
    }

    private HBox listItem(String icon, String title, String sub, String iconColor) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 0, 8, 0));

        StackPane iconBox = new StackPane();
        Rectangle bg = new Rectangle(36, 36);
        bg.setArcWidth(8);
        bg.setArcHeight(8);
        bg.setFill(Color.web(BG));
        Label iconLbl = label(icon, 16, iconColor, false);
        iconBox.getChildren().addAll(bg, iconLbl);

        VBox text = new VBox(2);
        HBox.setHgrow(text, Priority.ALWAYS);
        text.getChildren().addAll(
                label(title, 13, "#111827", false),
                label(sub, 11, GRAY_TEXT, false)
        );

        row.getChildren().addAll(iconBox, text);
        return row;
    }

    private Circle mapDot(double x, double y, String color) {
        Circle c = new Circle(7, Color.web(color));
        c.setCenterX(x);
        c.setCenterY(y);
        c.setStroke(Color.WHITE);
        c.setStrokeWidth(2);
        return c;
    }

    private HBox legendItem(String color, String text) {
        HBox item = new HBox(6);
        item.setAlignment(Pos.CENTER_LEFT);
        Circle dot = new Circle(5, Color.web(color));
        item.getChildren().addAll(dot, label(text, 11, GRAY_TEXT, false));
        return item;
    }

    private VBox statCard(String icon, String bgIcon, String iconColor,
            String title, String value, String sub, String subColor) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: " + WHITE + "; -fx-background-radius: 12;");
        HBox.setHgrow(card, Priority.ALWAYS);
        shadow(card);

        HBox top = new HBox(12);
        top.setAlignment(Pos.CENTER_LEFT);

        StackPane iconBox = new StackPane();
        Rectangle iconBg = new Rectangle(44, 44);
        iconBg.setArcWidth(10);
        iconBg.setArcHeight(10);
        iconBg.setFill(Color.web(bgIcon));
        Label iconLbl = label(icon, 20, iconColor, false);
        iconBox.getChildren().addAll(iconBg, iconLbl);
        top.getChildren().addAll(iconBox, label(title, 13, GRAY_TEXT, false));

        Label valueLbl = new Label(value);
        valueLbl.setFont(Font.font("System", FontWeight.BOLD, 34));
        valueLbl.setTextFill(Color.web("#111827"));

        card.getChildren().addAll(top, valueLbl, label(sub, 12, subColor, false));
        return card;
    }

    private VBox createPanel(String title) {
        VBox panel = new VBox(12);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: " + WHITE + "; -fx-background-radius: 12;");
        HBox.setHgrow(panel, Priority.ALWAYS);
        shadow(panel);

        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getChildren().add(label(title, 15, "#111827", true));
        panel.getChildren().addAll(header, separator());
        return panel;
    }

    private ScrollPane buildPlaceholderView(String nombre) {
        VBox box = new VBox(20);
        box.setPadding(new Insets(40));
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: #f4f6fb;");

        Label icon = new Label("🚧");
        icon.setFont(Font.font(70));
        Label title = new Label(nombre);
        title.setFont(Font.font("System", FontWeight.BOLD, 32));
        title.setTextFill(Color.web("#111827"));
        Label msg = new Label("Pantalla en construcción");
        msg.setFont(Font.font(18));
        msg.setTextFill(Color.GRAY);

        Button volver = new Button("Volver al Dashboard");
        volver.setStyle("""
            -fx-background-color: #1565c0; -fx-text-fill: white;
            -fx-font-size: 14px; -fx-background-radius: 10;
            -fx-padding: 10 20 10 20; -fx-cursor: hand;
            """);
        volver.setOnAction(e -> root.setCenter(buildMainContent()));
        box.getChildren().addAll(icon, title, msg, volver);

        ScrollPane scroll = new ScrollPane(box);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #f4f6fb; -fx-background-color: #f4f6fb;");
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
        sep.setPrefHeight(1);
        sep.setStyle("-fx-background-color: " + BORDER + ";");
        return sep;
    }

    private void shadow(Region node) {
        node.setEffect(new DropShadow(12, 0, 2, Color.web("#0000001a")));
    }

    private void mostrarAlerta(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
