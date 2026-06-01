/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.view;

/**
 *
 * @author Maria Cristina
 */
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import sistemagestion.model.*;
import sistemagestion.service.*;

/**
 * Controlador principal del dashboard del Administrador Policía.
 *
 * Cada sección del menú delega en su propia clase *View:
 * AlertasAdminPoliciaView, AlarmasAdminPoliciaView,
 * AsignacionesAdminPoliciaView, HistorialAdminPoliciaView,
 * PoliciasAdminPoliciaView, UnidadesAdminPoliciaView,
 * EstadisticasAdminPoliciaView, ReportesAdminPoliciaView,
 * NotificacionesAdminPoliciaView, ConfiguracionAdminPoliciaView,
 * MapaAdminPoliciaView.
 */
public class AdministradorPoliciaApp {

    // ── Paleta ────────────────────────────────────────────────────
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

    // ── Servicios ─────────────────────────────────────────────────
    private AlertaService alertaService;
    private PoliciaService policiaService;
    private UnidadPolicialService unidadService;
    private AsignacionUnidadService asignacionService;
    private AlarmaService alarmaService;
    private NotificacionService notificacionService;
    private AtencionAlertaService atencionService;
    private AsignacionesAdminPoliciaView asignacionesView;

    // ── Usuario logueado ──────────────────────────────────────────
    private final Usuario usuarioActual;

    // ── UI ────────────────────────────────────────────────────────
    private BorderPane root;
    private VBox nav;

    private boolean mapaExpandido = false;
    private VBox mapaSubMenu = null;

    // ── Constructor ───────────────────────────────────────────────
    public AdministradorPoliciaApp(Usuario usuarioActual) {
        this.usuarioActual = usuarioActual;
        Font.loadFont(getClass().getResourceAsStream("/fa-solid-900.ttf"), 20);
        try {
            alertaService = new AlertaService();
            policiaService = new PoliciaService();
            unidadService = new UnidadPolicialService();
            asignacionService = new AsignacionUnidadService();
            alarmaService = new AlarmaService();
            notificacionService = new NotificacionService();
            atencionService = new AtencionAlertaService();
        } catch (SQLException e) {
            mostrarAlerta("Error de conexión", e.getMessage());
        }
    }

    // =========================================================================
    // SHOW
    // =========================================================================
    public void show(Stage stage) {

        root = new BorderPane();
        root.setLeft(buildSidebar());
        root.setCenter(buildMainContent());
        root.setStyle("-fx-background-color: " + BG + ";");

        Scene scene = new Scene(root, 1100, 650);
        stage.setTitle("WolertApp – Administrador Policía");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMaximized(false);
        stage.show();
    }

    // =========================================================================
    // SIDEBAR
    // =========================================================================
    private VBox buildSidebar() {
        VBox sidebar = new VBox();
        sidebar.setPrefWidth(240);
        sidebar.setMinWidth(240);
        sidebar.setMaxWidth(240);
        sidebar.setStyle("-fx-background-color: linear-gradient(to right, #16283d, #1f3a56);");
        VBox.setVgrow(sidebar, Priority.ALWAYS);
        sidebar.setMaxHeight(Double.MAX_VALUE);

        // Logo
        HBox logoBox = new HBox(10);
        logoBox.setPadding(new Insets(20, 16, 20, 16));
        logoBox.setAlignment(Pos.CENTER_LEFT);
        StackPane wolfIcon = new StackPane();
        Circle iconCircle = new Circle(22, Color.web("#2a3560"));
        wolfIcon.getChildren().addAll(iconCircle, label("🐺", 18, WHITE, false));
        VBox logoText = new VBox(2);
        logoText.getChildren().addAll(
                label("WolertApp", 15, WHITE, true),
                label("Administrador Policía", 9, "#8899bb", false));
        logoBox.getChildren().addAll(wolfIcon, logoText);

        // Perfil
        HBox profileCard = buildProfileCard();

        // Nav
        nav = new VBox(2);
        nav.setPadding(new Insets(12, 8, 12, 8));
        HBox mapaItem = buildMapaNavItem();
        nav.getChildren().addAll(
                navItem("🏠", "Centro de operaciones"),
                navItem("🚨", "Alertas"),
                navItem("🔔", "Alarmas"),
                navItem("📌", "Asignaciones"),
                navItem("👮", "Policías"),
                navItem("🚓", "Unidades"),
                navItem("📜", "Historial"),
                mapaItem,
                navItem("📊", "Estadísticas"),
                navItem("📈", "Reportes"),
                navItem("🔔", "Notificaciones"),
                navItem("⚙", "Configuración")
        );

        VBox spacer = new VBox();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Logout
        HBox logout = new HBox(10);
        logout.setPadding(new Insets(14, 16, 18, 16));
        logout.setAlignment(Pos.CENTER_LEFT);
        logout.setCursor(javafx.scene.Cursor.HAND);
        logout.setStyle("-fx-background-color: transparent;");
        logout.setOnMouseEntered(e -> logout.setStyle("-fx-background-color: rgba(229,57,53,0.15); -fx-background-radius: 8;"));
        logout.setOnMouseExited(e -> logout.setStyle("-fx-background-color: transparent;"));
        logout.setOnMouseClicked(e -> cerrarSesion());
        logout.getChildren().addAll(label("🚪", 15, RED, false), label("Cerrar sesión", 13, RED, true));

        sidebar.getChildren().addAll(logoBox, profileCard, nav, spacer, logout);
        return sidebar;
    }

    private HBox buildProfileCard() {
        HBox card = new HBox(10);
        card.setPadding(new Insets(10, 16, 10, 16));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 12;");

        Circle av = new Circle(20, Color.web("#334155"));
        StackPane avBox = new StackPane(av, label("👮", 15, WHITE, false));

        VBox info = new VBox(2);
        String nombreDisplay = usuarioActual != null
                ? (usuarioActual.getPrimer_nombre() != null ? usuarioActual.getPrimer_nombre() : "")
                + " " + (usuarioActual.getPrimer_apellido() != null ? usuarioActual.getPrimer_apellido() : "")
                : "Administrador";
        String rangoDisplay = "Administrador Policía";
        if (usuarioActual != null && usuarioActual.getIdentificacion() != null) {
            try {
                for (Policia p : policiaService.listar()) {
                    if (usuarioActual.getIdentificacion().equals(p.getIdentificacion())
                            && p.getRango() != null && !p.getRango().isBlank()) {
                        rangoDisplay = p.getRango();
                        break;
                    }
                }
            } catch (Exception ignored) {
            }
        }

        HBox statusRow = new HBox(4);
        statusRow.setAlignment(Pos.CENTER_LEFT);
        statusRow.getChildren().addAll(new Circle(4, Color.web(GREEN)), label("En servicio", 10, GREEN, false));
        info.getChildren().addAll(
                label(nombreDisplay.trim(), 12, WHITE, true),
                label(rangoDisplay, 9, "#8899bb", false),
                statusRow);
        card.getChildren().addAll(avBox, info);
        return card;
    }

    // ── Nav item genérico ────────────────────────────────────────
    private HBox navItem(String icon, String text) {
        HBox item = new HBox(10);
        item.setPadding(new Insets(9, 12, 9, 12));
        item.setAlignment(Pos.CENTER_LEFT);
        item.setCursor(javafx.scene.Cursor.HAND);
        item.setMaxWidth(Double.MAX_VALUE);
        item.setStyle("-fx-background-radius: 8;");
        item.setOnMouseEntered(e -> item.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-background-radius: 8;"));
        item.setOnMouseExited(e -> item.setStyle("-fx-background-radius: 8;"));
        item.getChildren().addAll(label(icon, 14, WHITE, false), label(text, 13, "#f8fafc", true));

        // ── Delegación a las clases View ─────────────────────────
        item.setOnMouseClicked(e -> root.setCenter(switch (text) {
            case "Centro de operaciones" ->
                buildMainContent();
            case "Alertas" ->
                new AlertasAdminPoliciaView(alertaService).build();
            case "Alarmas" ->
                new AlarmasAdminPoliciaView(alarmaService).build();
            case "Asignaciones" ->
                new AsignacionesAdminPoliciaView(
                asignacionService, unidadService, alarmaService).build();
            case "Historial" ->
                new HistorialAdminPoliciaView(atencionService).build();
            case "Policías" ->
                new PoliciasAdminPoliciaView(policiaService, unidadService).build();
            case "Unidades" ->
                new UnidadesAdminPoliciaView(unidadService).build();
            case "Estadísticas" ->
                new EstadisticasAdminPoliciaView(
                alertaService, asignacionService, unidadService,
                policiaService, alarmaService, notificacionService).build();
            case "Reportes" ->
                new ReportesAdminPoliciaView(
                alertaService, asignacionService, unidadService,
                policiaService, alarmaService, notificacionService, atencionService).build();
            case "Notificaciones" ->
                new NotificacionesAdminPoliciaView(notificacionService).build();
            case "Configuración" ->
                new ConfiguracionAdminPoliciaView(usuarioActual).build();
            default ->
                buildMainContent();
        }));
        return item;
    }

    // ── Nav item "Mapa tiempo real" con submenú ──────────────────
    private HBox buildMapaNavItem() {
        HBox item = new HBox(10);
        item.setPadding(new Insets(9, 12, 9, 12));
        item.setAlignment(Pos.CENTER_LEFT);
        item.setCursor(javafx.scene.Cursor.HAND);
        item.setMaxWidth(Double.MAX_VALUE);
        item.setStyle("-fx-background-radius: 8;");

        Label arrowLbl = label("▶", 10, WHITE, false);
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        item.getChildren().addAll(label("📍", 14, WHITE, false), label("Mapas", 13, "#f8fafc", true), sp, arrowLbl);
        item.setOnMouseEntered(e -> item.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-background-radius: 8;"));
        item.setOnMouseExited(e -> item.setStyle("-fx-background-radius: 8;"));
        item.setOnMouseClicked(e -> toggleMapaSubMenu(item, arrowLbl));
        return item;
    }

    private void toggleMapaSubMenu(HBox item, Label arrowLbl) {
        int idx = nav.getChildren().indexOf(item);
        if (!mapaExpandido) {
            mapaSubMenu = new VBox(2);
            mapaSubMenu.setPadding(new Insets(2, 0, 4, 22));
            mapaSubMenu.getChildren().addAll(
                    subNavItem("🗺", "Mapa de alertas"),
                    subNavItem("🔥", "Zonas peligrosas"),
                    subNavItem("👥", "Mapa Operaciones"));
            nav.getChildren().add(idx + 1, mapaSubMenu);
            arrowLbl.setText("▼");
            mapaExpandido = true;
        } else {
            nav.getChildren().remove(mapaSubMenu);
            arrowLbl.setText("▶");
            mapaExpandido = false;
        }
    }

    private HBox subNavItem(String icon, String text) {
        HBox item = new HBox(8);
        item.setPadding(new Insets(7, 10, 7, 10));
        item.setAlignment(Pos.CENTER_LEFT);
        item.setCursor(javafx.scene.Cursor.HAND);
        item.setMaxWidth(Double.MAX_VALUE);
        item.setStyle("-fx-background-radius: 7;");
        item.setOnMouseEntered(e -> item.setStyle("-fx-background-color: #ffffff12; -fx-background-radius: 7;"));
        item.setOnMouseExited(e -> item.setStyle("-fx-background-radius: 7;"));
        item.getChildren().addAll(label(icon, 11, "#e2e8f0", false), label(text, 11, WHITE, true));
        // Delega en MapaAdminPoliciaView
        item.setOnMouseClicked(e -> root.setCenter(switch (text) {
            case "Zonas peligrosas" ->
                new MapaZonasPeligrosas(alertaService).build();
            case "Mapa de alertas" ->
                new MapaAlertas(alertaService).build(); // o el que tengas
            case "Mapa Operaciones" ->
                new MapaOperaciones(asignacionService, unidadService).build();
            default ->
                new MapaAdminPoliciaView(alertaService, text).build();
        }));
        return item;
    }

    // =========================================================================
    // MAIN CONTENT (centro de operaciones — lógica propia del dashboard)
    // =========================================================================
    private ScrollPane buildMainContent() {
        VBox content = new VBox(18);
        content.setPadding(new Insets(22));
        content.setStyle("-fx-background-color: " + BG + ";");
        content.getChildren().addAll(
                buildTopBar(),
                buildStats(),
                buildCenterPanels(),
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
        greeting.getChildren().addAll(hello, label("Centro de operaciones policial — WolertApp", 12, GRAY_TEXT, false));

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

    // ── Stat cards ────────────────────────────────────────────────────────────────────────────
    private HBox buildStats() {
        HBox row = new HBox(16);
        row.getChildren().addAll(
                statCard(RED_LIGHT, RED_LIGHT, "Alertas activas", contarAlertasActivas(), "PENDIENTE / EN ATENCIÓN", "\uf0f3", RED),
                statCard(ORANGE_LIGHT, ORANGE_LIGHT, "Asignaciones", contarAsignaciones(), "Total registradas", "\uf14a", ORANGE),
                statCard(GREEN_LIGHT, GREEN_LIGHT, "Unidades activas", contarUnidadesActivas(), "Estado OPERATIVA", "\uf1b9", GREEN),
                statCard(BLUE_LIGHT, BLUE_LIGHT, "Policías activos", contarPoliciasActivos(), "Estado DISPONIBLE", "\uf505", BLUE));
        return row;
    }

    private VBox statCard(String bgIcon, String accentColor, String title,
            long value, String sub, String faIcon, String iconColor) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20, 22, 20, 22));
        card.setStyle("-fx-background-color:white; -fx-background-radius:18;");
        card.setPrefWidth(260);
        HBox.setHgrow(card, Priority.ALWAYS);
        shadow(card);

        StackPane iconWrap = new StackPane();
        iconWrap.setPrefSize(52, 52);
        iconWrap.setMinSize(52, 52);
        iconWrap.setMaxSize(52, 52);

        Region colorBg = new Region();
        colorBg.setPrefSize(52, 52);
        colorBg.setStyle(
                "-fx-background-color:" + bgIcon + ";"
                + "-fx-background-radius:14;"
                + "-fx-border-color:" + accentColor + ";"
                + "-fx-border-radius:14;"
                + "-fx-border-width:1.5;");

        Label faLbl = new Label(faIcon);
        faLbl.setStyle(
                "-fx-font-family: 'Font Awesome 6 Free Solid';"
                + "-fx-font-size: 22px;"
                + "-fx-text-fill: " + iconColor + ";");

        iconWrap.getChildren().addAll(colorBg, faLbl);

        Label titleLbl = label(title, 13, "#374151", true);

        Label valLbl = new Label(String.valueOf(value));
        valLbl.setStyle("-fx-font-size:36px; -fx-font-weight:bold; -fx-text-fill:" + accentColor + ";");

        Label subLbl = label(sub, 11, GRAY_TEXT, false);

        VBox textBlock = new VBox(3);
        textBlock.getChildren().addAll(titleLbl, valLbl, subLbl);

        HBox topRow = new HBox(16);
        topRow.setAlignment(Pos.CENTER_LEFT);
        topRow.getChildren().addAll(iconWrap, textBlock);
        card.getChildren().add(topRow);

        card.setOnMouseEntered(e -> {
            card.setTranslateY(-3);
            card.setStyle("-fx-background-color:white; -fx-background-radius:18;"
                    + "-fx-border-color:" + accentColor + ";"
                    + "-fx-border-width:1.5; -fx-border-radius:18;");
        });
        card.setOnMouseExited(e -> {
            card.setTranslateY(0);
            card.setStyle("-fx-background-color:white; -fx-background-radius:18;");
        });
        return card;
    }

    // ── Paneles centrales ────────────────────────────────────────
    private HBox buildCenterPanels() {
        HBox row = new HBox(16);
        row.getChildren().addAll(buildAlertsPanel(), buildMapPanel());
        return row;
    }

    private VBox buildAlertsPanel() {
        VBox card = createPanel("\uf0f3", "Alertas recientes", RED, RED_LIGHT);
        HBox.setHgrow(card, Priority.ALWAYS);
        if (alertaService == null) {
            card.getChildren().add(label("Sin conexión al servicio de alertas", 13, GRAY_TEXT, false));
            return card;
        }
        try {
            List<Alerta> alertas = alertaService.listar();
            if (alertas.isEmpty()) {
                card.getChildren().add(label("No hay alertas registradas", 13, GRAY_TEXT, false));
            } else {
                alertas.stream()
                        .filter(a -> a.getFechaHora() != null)
                        .sorted((a, b) -> b.getFechaHora().compareTo(a.getFechaHora()))
                        .limit(5)
                        .forEach(a -> {
                            String dotColor = estadoColor(a.getEstado());
                            String tipo = a.getTipoalerta() != null ? a.getTipoalerta().getNombre() : "Alerta";
                            String barrio = a.getBarrio() != null ? " — " + a.getBarrio().getNombre() : "";
                            String titulo = tipo + barrio;
                            String sub = formatFecha(a.getFechaHora())
                                    + (a.getEstado() != null ? " · " + a.getEstado().name().replace("_", " ") : "");

                            HBox item = alertItem("\uf0f3", titulo, sub, dotColor);

                            // ── Click → navega al panel de Alertas ──
                            item.setOnMouseClicked(e
                                    -> root.setCenter(new AlertasAdminPoliciaView(alertaService).build()));
                            item.setOnMouseEntered(e
                                    -> item.setStyle("-fx-background-color:#f0f4ff; -fx-cursor:hand; -fx-background-radius:8;"));
                            item.setOnMouseExited(e
                                    -> item.setStyle("-fx-background-color:transparent;"));

                            card.getChildren().addAll(item, separator());
                        });
            }
        } catch (Exception e) {
            card.getChildren().add(label("Error al cargar alertas: " + e.getMessage(), 12, RED, false));
        }
        return card;
    }

    private VBox buildMapPanel() {
        VBox card = createPanel("\uf3c5", "Mapa policial en tiempo real", BLUE, BLUE_LIGHT);
        card.setPrefWidth(340);

        StackPane mapArea = new StackPane();
        mapArea.setPrefHeight(200);
        Rectangle mapBg = new Rectangle();
        mapBg.setFill(Color.web("#d1e8d1"));
        mapBg.widthProperty().bind(mapArea.widthProperty());
        mapBg.heightProperty().bind(mapArea.heightProperty());
        mapBg.setArcWidth(10);
        mapBg.setArcHeight(10);

        javafx.scene.layout.Pane streets = new javafx.scene.layout.Pane();
        streets.setPrefSize(300, 180);
        for (int i = 0; i < 3; i++) {
            Rectangle h = new Rectangle(300, 2);
            h.setFill(Color.web("#b8d4b8"));
            h.setY(40 + i * 50);
            streets.getChildren().add(h);
        }
        for (int i = 0; i < 4; i++) {
            Rectangle v = new Rectangle(2, 180);
            v.setFill(Color.web("#b8d4b8"));
            v.setX(40 + i * 65);
            streets.getChildren().add(v);
        }
        streets.getChildren().addAll(mapDot(70, 50, RED), mapDot(180, 35, ORANGE), mapDot(260, 50, GREEN), mapDot(55, 130, GREEN), mapDot(270, 140, RED));

        VBox popup = new VBox(4);
        popup.setPadding(new Insets(8, 12, 8, 12));
        popup.setStyle("-fx-background-color: white; -fx-background-radius: 8;");
        popup.setEffect(new DropShadow(6, Color.web("#0000001a")));
        popup.setTranslateX(15);
        popup.setTranslateY(8);
        popup.getChildren().addAll(label("Mapa interactivo", 11, "#374151", true), label("Ver alertas en tiempo real", 10, BLUE, false));
        mapArea.getChildren().addAll(mapBg, streets, popup);

        HBox legend = new HBox(14);
        legend.setAlignment(Pos.CENTER);
        legend.getChildren().addAll(legendItem(RED, "Activo"), legendItem(ORANGE, "En revisión"), legendItem(GREEN, "Disponible"));
        card.getChildren().addAll(mapArea, legend);
        return card;
    }

    // ── Paneles inferiores ────────────────────────────────────────
    private HBox buildBottomPanels() {
        HBox row = new HBox(16);
        VBox unidades = createPanel("\uf1b9", "Unidades disponibles", GREEN, GREEN_LIGHT);
        VBox policias = createPanel("\uf505", "Policías en servicio", BLUE, BLUE_LIGHT);
        VBox acciones = createPanel("\uf0e7", "Acciones rápidas", ORANGE, ORANGE_LIGHT);
        HBox.setHgrow(unidades, Priority.ALWAYS);
        HBox.setHgrow(policias, Priority.ALWAYS);
        HBox.setHgrow(acciones, Priority.ALWAYS);

        if (unidadService != null) {
            try {
                List<UnidadPolicial> lista = unidadService.listar();
                if (lista.isEmpty()) {
                    unidades.getChildren().add(label("No hay unidades registradas", 12, GRAY_TEXT, false));
                } else {
                    lista.stream().limit(5).forEach(u -> {
                        String estadoStr = u.getEstado() != null ? u.getEstado().name().replace("_", " ") : "—";
                        String color = u.getEstado() == EstadoUnidadPolicial.OPERATIVA ? GREEN
                                : u.getEstado() == EstadoUnidadPolicial.ACTIVA ? ORANGE : GRAY_TEXT;
                        HBox item = listItem("\uf1b9", u.getNombre(), estadoStr, color);
                        item.setOnMouseClicked(e
                                -> root.setCenter(new UnidadesAdminPoliciaView(unidadService).build()));
                        item.setOnMouseEntered(e
                                -> item.setStyle("-fx-background-color:#f0f4ff; -fx-cursor:hand; -fx-background-radius:8;"));
                        item.setOnMouseExited(e
                                -> item.setStyle("-fx-background-color:transparent;"));
                        unidades.getChildren().addAll(item, separator());
                    });
                }
            } catch (Exception e) {
                unidades.getChildren().add(label("Error al cargar unidades", 12, RED, false));
            }
        }

        if (policiaService != null) {
            try {
                List<Policia> lista = policiaService.listar();
                if (lista.isEmpty()) {
                    policias.getChildren().add(label("No hay policías registrados", 12, GRAY_TEXT, false));
                } else {
                    lista.stream().limit(5).forEach(p -> {
                        String nombreP = ((p.getPrimer_nombre() != null ? p.getPrimer_nombre() : "")
                                + " " + (p.getPrimer_apellido() != null ? p.getPrimer_apellido() : "")).trim();
                        String estadoStr = p.getEstadopolicial() != null
                                ? p.getEstadopolicial().name().replace("_", " ") : "—";
                        String color = switch (p.getEstadopolicial() != null
                                ? p.getEstadopolicial() : EstadoPolicia.FUERA_DE_SERVICIO) {
                            case DISPONIBLE ->
                                GREEN;
                            case EN_SERVICIO ->
                                BLUE;
                            case OCUPADO ->
                                ORANGE;
                            default ->
                                GRAY_TEXT;
                        };
                        HBox item = listItem("\uf505", nombreP, estadoStr, color);
                        item.setOnMouseClicked(e
                                -> root.setCenter(new PoliciasAdminPoliciaView(policiaService, unidadService).build()));
                        item.setOnMouseEntered(e
                                -> item.setStyle("-fx-background-color:#f0f4ff; -fx-cursor:hand; -fx-background-radius:8;"));
                        item.setOnMouseExited(e
                                -> item.setStyle("-fx-background-color:transparent;"));
                        policias.getChildren().addAll(item, separator());
                    });
                }
            } catch (Exception e) {
                policias.getChildren().add(label("Error al cargar policías", 12, RED, false));
            }
        }
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setMaxWidth(Double.MAX_VALUE);
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        grid.getColumnConstraints().addAll(col1, col2);

        grid.add(actionBtn("\uf14a", "Asignar", BLUE, BLUE_LIGHT, () -> root.setCenter(
                new AsignacionesAdminPoliciaView(asignacionService, unidadService, alarmaService).build())), 0, 0);

        grid.add(actionBtn("\uf0f3", "Nueva alerta", RED, RED_LIGHT, () -> {
            MapaAlerta mapa = new MapaAlerta(
                    (Stage) root.getScene().getWindow(), usuarioActual, alertaService, null);
            mapa.mostrar();
        }), 1, 0);

        grid.add(actionBtn("\uf0a1", "Notificar", GREEN, GREEN_LIGHT, () -> root.setCenter(
                new NotificacionesAdminPoliciaView(notificacionService).build())), 0, 1);

        grid.add(actionBtn("\uf080", "Reportes", ORANGE, ORANGE_LIGHT, () -> root.setCenter(
                new ReportesAdminPoliciaView(alertaService, asignacionService, unidadService,
                        policiaService, alarmaService, notificacionService, atencionService).build())), 1, 1);

        grid.add(actionBtn("\uf3c5", "Mapa ops.", "#7b1fa2", "#f3e5f5", () -> {
            MapaOperaciones mapa = new MapaOperaciones(asignacionService, unidadService);
            Stage stage = new Stage();
            stage.setTitle("Mapa de operaciones");
            stage.setScene(new javafx.scene.Scene(mapa.build(), 1100, 680));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.show();
        }), 0, 2);

        grid.add(actionBtn("\uf201", "Estadísticas", "#0097a7", "#e0f7fa", () -> root.setCenter(
                new EstadisticasAdminPoliciaView(alertaService, asignacionService, unidadService,
                        policiaService, alarmaService, notificacionService).build())), 1, 2);

        acciones.getChildren().add(grid);

        row.getChildren().addAll(unidades, policias, acciones);
        return row;
    }

    private VBox actionBtn(String faIcon, String text,
            String iconColor, String bgColor, Runnable accion) {
        StackPane iconWrap = new StackPane();
        iconWrap.setPrefSize(34, 34);
        iconWrap.setMinSize(34, 34);
        Region iconBg = new Region();
        iconBg.setPrefSize(34, 34);
        iconBg.setStyle("-fx-background-color:" + bgColor + "; -fx-background-radius: 9;");
        Label faLbl = new Label(faIcon);
        faLbl.setStyle(
                "-fx-font-family: 'Font Awesome 6 Free Solid';"
                + "-fx-font-size: 14px;"
                + "-fx-text-fill: " + iconColor + ";");
        iconWrap.getChildren().addAll(iconBg, faLbl);

        Label txt = label(text, 11, "#374151", true);
        txt.setAlignment(Pos.CENTER);
        txt.setMaxWidth(Double.MAX_VALUE);

        VBox btn = new VBox(7, iconWrap, txt);
        btn.setAlignment(Pos.CENTER);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPrefHeight(72);
        btn.setPadding(new Insets(10));
        btn.setCursor(javafx.scene.Cursor.HAND);

        String base = "-fx-background-color:white; -fx-background-radius:12;"
                + "-fx-border-color:" + BORDER + "; -fx-border-radius:12; -fx-border-width:1;";
        String hover = "-fx-background-color:" + bgColor + "; -fx-background-radius:12;"
                + "-fx-border-color:" + iconColor + "; -fx-border-radius:12; -fx-border-width:1.5;";

        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e -> btn.setStyle(base));
        btn.setOnMouseClicked(e -> accion.run());
        return btn;
    }

    // =========================================================================
    // CERRAR SESIÓN
    // =========================================================================
    private void cerrarSesion() {
        VBox bye = new VBox(20);
        bye.setAlignment(Pos.CENTER);
        bye.setStyle("-fx-background-color: " + BG + ";");
        Label icon = new Label("👋");
        icon.setFont(Font.font(70));
        Label title = new Label("Sesión cerrada");
        title.setFont(Font.font("System", FontWeight.BOLD, 30));
        title.setTextFill(Color.web("#111827"));
        Label msg = new Label("Cerrando aplicación...");
        msg.setTextFill(Color.GRAY);
        bye.getChildren().addAll(icon, title, msg);
        root.setCenter(bye);
        new Timeline(new KeyFrame(Duration.seconds(2),
                ev -> ((Stage) root.getScene().getWindow()).close())).play();
    }

    // =========================================================================
    // CONTADORES
    // =========================================================================
    private long contarAlertasActivas() {
        try {
            return alertaService == null ? 0 : alertaService.listar().stream()
                    .filter(a -> a.getEstado() == EstadoAlerta.PENDIENTE
                    || a.getEstado() == EstadoAlerta.EN_ATENCION
                    || a.getEstado() == EstadoAlerta.UNIDAD_ASIGNADA
                    || a.getEstado() == EstadoAlerta.RECIBIDA).count();
        } catch (Exception e) {
            return 0;
        }
    }

    private long contarPoliciasActivos() {
        try {
            return policiaService == null ? 0 : policiaService.listar().stream()
                    .filter(p -> p.getEstadopolicial() == EstadoPolicia.DISPONIBLE
                    || p.getEstadopolicial() == EstadoPolicia.EN_SERVICIO).count();
        } catch (Exception e) {
            return 0;
        }
    }

    private long contarUnidadesActivas() {
        try {
            return unidadService == null ? 0 : unidadService.listar().stream()
                    .filter(u -> u.getEstado() == EstadoUnidadPolicial.OPERATIVA).count();
        } catch (Exception e) {
            return 0;
        }
    }

    private long contarAsignaciones() {
        try {
            return asignacionService == null ? 0 : asignacionService.listar().size();
        } catch (Exception e) {
            return 0;
        }
    }

    private int contarNotificaciones() {
        try {
            return notificacionService == null ? 0 : notificacionService.listar().size();
        } catch (Exception e) {
            return 0;
        }
    }

    // =========================================================================
    // HELPERS COMPARTIDOS
    // =========================================================================
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

    private VBox createPanel(String faIcon, String title, String iconColor, String bgColor) {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(16));
        panel.setStyle("-fx-background-color: " + WHITE + "; -fx-background-radius: 12;");
        shadow(panel);

        // ── Título con ícono FA colorido ──
        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(28, 28);
        iconBox.setMinSize(28, 28);
        Region iconBg = new Region();
        iconBg.setPrefSize(28, 28);
        iconBg.setStyle("-fx-background-color:" + bgColor + "; -fx-background-radius: 7;");
        Label faLbl = new Label(faIcon);
        faLbl.setStyle(
                "-fx-font-family: 'Font Awesome 6 Free Solid';"
                + "-fx-font-size: 13px;"
                + "-fx-text-fill: " + iconColor + ";");
        iconBox.getChildren().addAll(iconBg, faLbl);

        Label titleLbl = label(title, 14, "#111827", true);

        HBox header = new HBox(8, iconBox, titleLbl);
        header.setAlignment(Pos.CENTER_LEFT);

        panel.getChildren().addAll(header, separator());
        return panel;
    }

    private Circle mapDot(double x, double y, String color) {
        Circle c = new Circle(6, Color.web(color));
        c.setCenterX(x);
        c.setCenterY(y);
        c.setStroke(Color.WHITE);
        c.setStrokeWidth(2);
        return c;
    }

    private HBox legendItem(String color, String text) {
        HBox item = new HBox(5);
        item.setAlignment(Pos.CENTER_LEFT);
        item.getChildren().addAll(new Circle(4, Color.web(color)), label(text, 10, GRAY_TEXT, false));
        return item;
    }

    private HBox listItem(String faIcon, String title, String sub, String subColor) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(6, 4, 6, 4));

        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(34, 34);
        iconBox.setMinSize(34, 34);
        iconBox.setMaxSize(34, 34);
        Region iconBg = new Region();
        iconBg.setPrefSize(34, 34);
        iconBg.setStyle("-fx-background-color:#e8f0fe; -fx-background-radius: 10;");

        Label faLbl = new Label(faIcon);
        faLbl.setStyle(
                "-fx-font-family: 'Font Awesome 6 Free Solid';"
                + "-fx-font-size: 14px;"
                + "-fx-text-fill: " + BLUE + ";");
        iconBox.getChildren().addAll(iconBg, faLbl);

        VBox text = new VBox(1);
        HBox.setHgrow(text, Priority.ALWAYS);
        text.getChildren().addAll(
                label(title, 12, "#111827", true),
                label(sub, 10, subColor, false));

        row.getChildren().addAll(iconBox, text);
        return row;
    }

    private HBox alertItem(String icon, String title, String sub, String dotColor) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(7, 0, 7, 0));
        row.setCursor(javafx.scene.Cursor.HAND);

        Circle dotCircle = new Circle(5, Color.web(dotColor));

        // ── Ícono FA con fondo azul clarito, sin borde ──
        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(34, 34);
        iconBox.setMinSize(34, 34);
        iconBox.setMaxSize(34, 34);
        Region iconBg = new Region();
        iconBg.setPrefSize(34, 34);
        iconBg.setStyle(
                "-fx-background-color: #e8f0fe;"
                + "-fx-background-radius: 10;");          // cuadrado redondeado azul clarito
        Label faLbl = new Label("\uf0f3");          // campana FA
        faLbl.setStyle(
                "-fx-font-family: 'Font Awesome 6 Free Solid';"
                + "-fx-font-size: 14px;"
                + "-fx-text-fill: " + BLUE + ";");
        iconBox.getChildren().addAll(iconBg, faLbl);

        VBox text = new VBox(2);
        HBox.setHgrow(text, Priority.ALWAYS);
        text.getChildren().addAll(
                label(title, 12, "#111827", false),
                label(sub, 10, GRAY_TEXT, false));

        row.getChildren().addAll(dotCircle, iconBox, text, label(">", 13, GRAY_TEXT, false));
        return row;
    }

    private Button actionBtn(String icon, String text) {
        Button btn = new Button(icon + "\n" + text);
        btn.setPrefSize(100, 70);
        btn.setWrapText(true);
        btn.setFont(Font.font("System", 12));
        String base = "-fx-background-color: " + BG + "; -fx-text-fill: #111827; -fx-background-radius: 10; -fx-border-color: " + BORDER + "; -fx-border-radius: 10; -fx-cursor: hand;";
        String hover = "-fx-background-color: " + BLUE_LIGHT + "; -fx-text-fill: " + BLUE + "; -fx-background-radius: 10; -fx-border-color: " + BLUE + "; -fx-border-radius: 10; -fx-cursor: hand;";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e -> btn.setStyle(base));
        return btn;
    }

    private Label label(String text, double size, String color, boolean bold) {
        Label lbl = new Label(text);
        lbl.setFont(bold ? Font.font("System", FontWeight.BOLD, size) : Font.font("System", size));
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
        node.setEffect(new DropShadow(10, 0, 2, Color.web("#0000001a")));
    }

    private java.awt.image.BufferedImage recortarTransparencia(java.awt.image.BufferedImage image) {
        int minX = image.getWidth(), minY = image.getHeight(), maxX = 0, maxY = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int alpha = (image.getRGB(x, y) >> 24) & 0xff;
                if (alpha > 0) {
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                }
            }
        }
        return image.getSubimage(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }

    private void mostrarAlerta(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

}
