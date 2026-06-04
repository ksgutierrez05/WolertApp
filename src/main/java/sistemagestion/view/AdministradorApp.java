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
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import sistemagestion.model.Alerta;
import sistemagestion.model.EstadoAlerta;
import sistemagestion.model.Usuario;
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

    // ── Sombra ────────────────────────────────────────────────────
    private static final String SHADOW_COLOR = "#0000001a";

    // ── Dimensiones ───────────────────────────────────────────────
    private static final double SIDEBAR_WIDTH = 250;
    private static final double ICON_BOX_SM = 36;
    private static final double LOGOUT_ICON_SIZE = 70;
    private static final double PLACEHOLDER_MSG_SIZE = 18;
    private static final double SEPARATOR_HEIGHT = 1;

    // ── Límites de listas ─────────────────────────────────────────
    private static final int MAX_ALERTAS_PANEL = 4;

    // ── Servicios ─────────────────────────────────────────────────
    private BorderPane root;
    private VBox nav;
    private Usuario usuarioLogueado;

    private AlertaService alertaService;
    private UsuarioService usuarioService;
    private NotificacionService notificacionService;

    public AdministradorApp(Usuario usuario) {
        this.usuarioLogueado = usuario;
        try {
            alertaService = new AlertaService();
            usuarioService = new UsuarioService();
            notificacionService = new NotificacionService();
        } catch (Exception e) {
            mostrarAlerta("Error de conexión", e.getMessage());
        }
    }

    public void show(Stage stage) {
        Font.loadFont(getClass().getResourceAsStream("/fa-solid-900.ttf"), 20);
        root = new BorderPane();
        root.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);

        VBox sidebar = buildSidebar();

        ScrollPane sidebarScroll = new ScrollPane(sidebar);
        sidebar.prefHeightProperty().bind(sidebarScroll.heightProperty());
        sidebarScroll.setFitToWidth(true);
        sidebarScroll.setFitToHeight(true);
        sidebarScroll.setStyle("""
                -fx-background: #16283d;
                -fx-background-color: #16283d;
                """);

        root.setLeft(sidebarScroll);
        root.setCenter(buildMainContent());

        Screen screen = Screen.getPrimary();
        double w = screen.getVisualBounds().getWidth();
        double h = screen.getVisualBounds().getHeight();

        Scene scene = new Scene(root, w * 0.85, h * 0.85);
        stage.setTitle("WolertApp – Panel Administrativo");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMinWidth(900);
        stage.setMinHeight(580);
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
        logoText.getChildren().addAll(
                label("WolertApp", 18, WHITE, true),
                label("Panel Administrativo", 9, SIDEBAR_NAV_TEXT, false));
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
        HBox statusRow = new HBox(4);
        statusRow.setAlignment(Pos.CENTER_LEFT);
        statusRow.getChildren().addAll(
                new Circle(4, Color.web(GREEN)),
                label("Sistema activo", 10, GREEN, false));
        adminInfo.getChildren().addAll(label("Administrador", 13, WHITE, true), statusRow);
        adminCard.getChildren().addAll(avatarBox, adminInfo);

        // Navegación
        nav = new VBox(2);
        nav.setPadding(new Insets(16, 8, 16, 8));
        nav.getChildren().addAll(
                navItem("\uf3fd", "Dashboard"), // house-chimney
                navItem("\uf0c0", "Usuarios"), // users
                navItem("\uf0f3", "Alertas"), // bell
                navItem("\uf0f3", "Alarmas"), // bell (con badge)
                navItem("\uf041", "Comunas"), // map-marker
                navItem("\uf015", "Barrios"), // home
                navItem("\uf0cb", "Tipos"), // list-ol
                navItem("\uf1f6", "Notificaciones"), // bell-slash
                navItem("\uf080", "Reportes"), // bar-chart
                navItem("\uf201", "Estadísticas"), // line-chart
                navItem("\uf013", "Configuración") // cog

        );

        // Cerrar sesión
        HBox logout = new HBox(10);
        logout.setPadding(new Insets(10, 16, 10, 16));
        logout.setAlignment(Pos.CENTER_LEFT);
        logout.setCursor(javafx.scene.Cursor.HAND);
        logout.setStyle("-fx-background-color: transparent;");

        logout.setOnMouseEntered(e -> logout.setStyle(
                "-fx-background-color: rgba(255,255,255,0.15); -fx-background-radius: 8;"));

        logout.setOnMouseExited(e -> logout.setStyle(
                "-fx-background-color: transparent;"));

        logout.setOnMouseClicked(e -> {
            Stage stage = (Stage) root.getScene().getWindow();
            stage.close();
        });

        Label logoutIcon = new Label("\uf2f5");  // fa-right-from-bracket
        logoutIcon.setStyle(
                "-fx-font-family: 'Font Awesome 6 Free Solid';"
                + "-fx-font-size: 14px;"
                + "-fx-text-fill: " + RED + ";");

        logout.getChildren().addAll(logoutIcon, label("Cerrar sesión", 13, WHITE, true));

        VBox spacer = new VBox();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        VBox.setVgrow(sidebar, Priority.ALWAYS);

        sidebar.getChildren().addAll(logoBox, adminCard, nav, spacer, logout);
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

        // Variables nombradas para poder referenciarlas en el click
        Label iconLbl = new Label(icon);
        iconLbl.setStyle(
                "-fx-font-family: 'Font Awesome 6 Free Solid';"
                + "-fx-font-size: 15px;"
                + "-fx-text-fill: " + SIDEBAR_NAV_TEXT + ";");
        Label textLbl = label(text, 13, SIDEBAR_NAV_TEXT, false);
        item.getChildren().addAll(iconLbl, textLbl);

        item.setOnMouseClicked(e -> {
            // Resetear todos los ítems
            nav.getChildren().forEach(node -> {
                if (node instanceof HBox hbox) {
                    hbox.setStyle("-fx-background-radius: 8;");
                    hbox.getChildren().forEach(child -> {
                        if (child instanceof Label lbl) {
                            if (lbl.getStyle().contains("Font Awesome")) {
                                lbl.setStyle(
                                        "-fx-font-family: 'Font Awesome 6 Free Solid';"
                                        + "-fx-font-size: 15px;"
                                        + "-fx-text-fill: " + SIDEBAR_NAV_TEXT + ";");
                            } else {
                                lbl.setTextFill(Color.web(SIDEBAR_NAV_TEXT));
                            }
                        }
                    });
                }
            });

            // Activar ítem clickeado
            item.setStyle("-fx-background-color: rgba(255,255,255,0.20); -fx-background-radius: 8;");
            iconLbl.setStyle(
                    "-fx-font-family: 'Font Awesome 6 Free Solid';"
                    + "-fx-font-size: 15px;"
                    + "-fx-text-fill: white;");
            textLbl.setTextFill(Color.WHITE);

            switch (text) {
                case "Dashboard" ->
                    root.setCenter(buildMainContent());
                case "Usuarios" ->
                    root.setCenter(new UsuariosAdminView().getView());
                case "Alertas" ->
                    root.setCenter(new AlertasAdminView(alertaService).getView());
                case "Alarmas" -> {
                    new MapaAlarmasRegistradas().mostrar();
                }
                case "Comunas" ->
                    root.setCenter(new ComunaAdminView().getView());
                case "Barrios" ->
                    root.setCenter(new BarrioAdminView().getView());
                case "Tipos" ->
                    root.setCenter(new TiposAdminView().getView());
                case "Notificaciones" ->
                    root.setCenter(new NotificacionesAdminView(notificacionService).getView());
                case "Reportes" -> {
                    try {
                        root.setCenter(
                                new ReportesAdminView(
                                        new sistemagestion.service.UsuarioService(),
                                        new sistemagestion.service.AlertaService()
                                ).build()
                        );
                    } catch (Exception ex) {
                        root.setCenter(buildPlaceholderView("Reportes — Error: " + ex.getMessage()));
                    }
                }
                case "Estadísticas" ->
                    root.setCenter(new EstadisticasAdminView().getView());
                case "Configuración" ->
                    root.setCenter(new ConfiguracionAdminView(usuarioLogueado).getView());
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
                buildBottomPanels());

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
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

        // Icono calendario FA
        Label calIco = new Label("\uf073");
        calIco.setStyle(
                "-fx-font-family:'Font Awesome 6 Free Solid';"
                + "-fx-font-size:13px;-fx-text-fill:" + BLUE + ";");
        Label dateLbl = label(now0.format(dateFmt), 13, TEXT_SECONDARY, false);

        // Icono reloj FA
        Label clockIco = new Label("\uf017");
        clockIco.setStyle(
                "-fx-font-family:'Font Awesome 6 Free Solid';"
                + "-fx-font-size:13px;-fx-text-fill:" + GRAY_TEXT + ";");
        Label timeLbl = label(now0.format(timeFmt), 13, GRAY_TEXT, false);

        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), ev -> {
            LocalDateTime now = LocalDateTime.now(ZoneId.of("America/Bogota"));
            dateLbl.setText(now.format(dateFmt));
            timeLbl.setText(now.format(timeFmt));
        }));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();

        HBox dateRow = new HBox(6);
        dateRow.setAlignment(Pos.CENTER_RIGHT);
        dateRow.getChildren().addAll(calIco, dateLbl);

        HBox timeRow = new HBox(6);
        timeRow.setAlignment(Pos.CENTER_RIGHT);
        timeRow.getChildren().addAll(clockIco, timeLbl);

        VBox dateBox = new VBox(4);
        dateBox.setAlignment(Pos.CENTER_RIGHT);
        dateBox.getChildren().addAll(dateRow, timeRow);

        // ── Campanita ─────────────────────────────────────────────────
        List<Alerta> alertasActivas = cargarAlertas().stream()
                .filter(a -> a.getEstado() == EstadoAlerta.PENDIENTE
                || a.getEstado() == EstadoAlerta.RECIBIDA
                || a.getEstado() == EstadoAlerta.EN_ATENCION
                || a.getEstado() == EstadoAlerta.UNIDAD_ASIGNADA)
                .sorted((a, b) -> b.getFechaHora() != null && a.getFechaHora() != null
                ? b.getFechaHora().compareTo(a.getFechaHora()) : 0)
                .limit(4)
                .collect(Collectors.toList());

        List<sistemagestion.model.Notificacion> ultimasNotifs = List.of();
        if (notificacionService != null) {
            try {
                ultimasNotifs = notificacionService.listar().stream()
                        .sorted((a, b) -> b.getFechahora() != null && a.getFechahora() != null
                        ? b.getFechahora().compareTo(a.getFechahora()) : 0)
                        .limit(3)
                        .collect(Collectors.toList());
            } catch (Exception ignored) {
            }
        }

        int totalBadge = (int) contarAlertasActivas() + ultimasNotifs.size();

        // Botón campana
        StackPane bell = new StackPane();
        bell.setCursor(javafx.scene.Cursor.HAND);

        Region bellBg = new Region();
        bellBg.setPrefSize(40, 40);
        bellBg.setStyle(
                "-fx-background-color:white;"
                + "-fx-background-radius:50%;"
                + "-fx-border-color:#e5e7eb;"
                + "-fx-border-radius:50%;"
                + "-fx-border-width:1.5;");

        Label bellIco = new Label("\uf0f3");
        bellIco.setStyle(
                "-fx-font-family:'Font Awesome 6 Free Solid';"
                + "-fx-font-size:16px;-fx-text-fill:#374151;");

        Label badge = new Label(totalBadge > 9 ? "9+" : String.valueOf(totalBadge));
        badge.setStyle(
                "-fx-background-color:#e53935;-fx-text-fill:white;"
                + "-fx-font-size:9px;-fx-font-weight:bold;"
                + "-fx-background-radius:20;-fx-padding:1 4;");
        badge.setTranslateX(10);
        badge.setTranslateY(-10);
        badge.setVisible(totalBadge > 0);

        bell.getChildren().addAll(bellBg, bellIco, badge);

        bell.setOnMouseEntered(e -> bellBg.setStyle(
                "-fx-background-color:#f0f7ff;"
                + "-fx-background-radius:50%;"
                + "-fx-border-color:#1565c0;"
                + "-fx-border-radius:50%;"
                + "-fx-border-width:1.5;"));
        bell.setOnMouseExited(e -> bellBg.setStyle(
                "-fx-background-color:white;"
                + "-fx-background-radius:50%;"
                + "-fx-border-color:#e5e7eb;"
                + "-fx-border-radius:50%;"
                + "-fx-border-width:1.5;"));

        // Popup
        javafx.stage.Popup popup = new javafx.stage.Popup();
        popup.setAutoHide(true);

        VBox popupBox = new VBox(0);
        popupBox.setPrefWidth(330);
        popupBox.setStyle(
                "-fx-background-color:white;"
                + "-fx-background-radius:14;"
                + "-fx-border-color:#e5e7eb;"
                + "-fx-border-radius:14;"
                + "-fx-border-width:1;"
                + "-fx-effect:dropshadow(gaussian,rgba(15,23,42,0.18),24,0,0,8);");

        // Header popup
        HBox popHeader = new HBox(8);
        popHeader.setPadding(new Insets(14, 16, 12, 16));
        popHeader.setAlignment(Pos.CENTER_LEFT);
        popHeader.setStyle(
                "-fx-background-color:#f8fafc;"
                + "-fx-background-radius:14 14 0 0;"
                + "-fx-border-color:transparent transparent #e5e7eb transparent;"
                + "-fx-border-width:0 0 1 0;");

        Label popTitle = new Label("Notificaciones del sistema");
        popTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        popTitle.setTextFill(Color.web(TEXT_PRIMARY));
        HBox.setHgrow(popTitle, Priority.ALWAYS);

        Label popBadge = new Label(totalBadge + " nuevas");
        popBadge.setStyle(
                "-fx-background-color:#e5393522;"
                + "-fx-text-fill:#e53935;"
                + "-fx-font-size:11px;-fx-font-weight:bold;"
                + "-fx-background-radius:20;-fx-padding:3 8;");
        popHeader.getChildren().addAll(popTitle, popBadge);
        popupBox.getChildren().add(popHeader);

        // Sección alertas activas
        if (!alertasActivas.isEmpty()) {
            HBox secAlertasBox = new HBox(6);
            secAlertasBox.setPadding(new Insets(8, 16, 6, 16));
            secAlertasBox.setAlignment(Pos.CENTER_LEFT);
            secAlertasBox.setMaxWidth(Double.MAX_VALUE);
            secAlertasBox.setStyle("-fx-background-color:#f9fafb;");
            Label secAlertaIco = new Label("\uf0f3");
            secAlertaIco.setStyle(
                    "-fx-font-family:'Font Awesome 6 Free Solid';"
                    + "-fx-font-size:11px;-fx-text-fill:" + RED + ";");
            Label secAlertaTxt = new Label("Alertas activas");
            secAlertaTxt.setFont(Font.font("System", FontWeight.BOLD, 11));
            secAlertaTxt.setTextFill(Color.web(GRAY_TEXT));
            secAlertasBox.getChildren().addAll(secAlertaIco, secAlertaTxt);
            popupBox.getChildren().add(secAlertasBox);

            for (Alerta a : alertasActivas) {
                String tipo = a.getTipoalerta() != null ? a.getTipoalerta().getNombre() : "Alerta";
                String barrio = a.getBarrio() != null ? a.getBarrio().getNombre() : "—";
                String fecha = a.getFechaHora() != null
                        ? a.getFechaHora().format(DateTimeFormatter.ofPattern("dd/MM HH:mm")) : "—";
                String estadoStr = a.getEstado() != null
                        ? a.getEstado().name().replace("_", " ") : "—";
                String dotColor = colorEstado(a.getEstado());

                HBox fila = new HBox(10);
                fila.setPadding(new Insets(10, 16, 10, 16));
                fila.setAlignment(Pos.CENTER_LEFT);
                fila.setStyle("-fx-border-color:transparent transparent #f3f4f6 transparent;"
                        + "-fx-border-width:0 0 1 0;");
                fila.setOnMouseEntered(e -> fila.setStyle(
                        "-fx-background-color:#f0f4ff;"
                        + "-fx-border-color:transparent transparent #f3f4f6 transparent;"
                        + "-fx-border-width:0 0 1 0;-fx-cursor:hand;"));
                fila.setOnMouseExited(e -> fila.setStyle(
                        "-fx-border-color:transparent transparent #f3f4f6 transparent;"
                        + "-fx-border-width:0 0 1 0;"));
                fila.setOnMouseClicked(e -> {
                    popup.hide();
                    root.setCenter(new AlertasAdminView(alertaService).getView());
                });

                Circle dot = new Circle(5, Color.web(dotColor));

                VBox info = new VBox(2);
                HBox.setHgrow(info, Priority.ALWAYS);

                Label tituloLbl = new Label(tipo + " — " + barrio);
                tituloLbl.setFont(Font.font("System", FontWeight.BOLD, 12));
                tituloLbl.setTextFill(Color.web(TEXT_PRIMARY));

                Label subLbl = new Label(estadoStr + "  ·  " + fecha);
                subLbl.setFont(Font.font("System", 11));
                subLbl.setTextFill(Color.web(GRAY_TEXT));

                info.getChildren().addAll(tituloLbl, subLbl);
                fila.getChildren().addAll(dot, info);
                popupBox.getChildren().add(fila);
            }
        }

        // Sección notificaciones de usuarios
        final List<sistemagestion.model.Notificacion> ultimasNotifsF = ultimasNotifs;
        if (!ultimasNotifsF.isEmpty()) {
            HBox secNotifBox = new HBox(6);
            secNotifBox.setPadding(new Insets(8, 16, 6, 16));
            secNotifBox.setAlignment(Pos.CENTER_LEFT);
            secNotifBox.setMaxWidth(Double.MAX_VALUE);
            secNotifBox.setStyle("-fx-background-color:#f9fafb;");
            Label secNotifIco = new Label("\uf1f6");
            secNotifIco.setStyle(
                    "-fx-font-family:'Font Awesome 6 Free Solid';"
                    + "-fx-font-size:11px;-fx-text-fill:" + BLUE + ";");
            Label secNotifTxt = new Label("Notificaciones de usuarios");
            secNotifTxt.setFont(Font.font("System", FontWeight.BOLD, 11));
            secNotifTxt.setTextFill(Color.web(GRAY_TEXT));
            secNotifBox.getChildren().addAll(secNotifIco, secNotifTxt);
            popupBox.getChildren().add(secNotifBox);

            for (sistemagestion.model.Notificacion n : ultimasNotifsF) {
                String mensaje = n.getMensaje() != null
                        ? (n.getMensaje().length() > 40
                        ? n.getMensaje().substring(0, 40) + "…" : n.getMensaje()) : "—";
                String dest = n.getCorreodestinatario() != null ? n.getCorreodestinatario() : "—";
                String fecha = n.getFechahora() != null
                        ? n.getFechahora().format(DateTimeFormatter.ofPattern("dd/MM HH:mm")) : "—";

                HBox fila = new HBox(10);
                fila.setPadding(new Insets(10, 16, 10, 16));
                fila.setAlignment(Pos.CENTER_LEFT);
                fila.setStyle("-fx-border-color:transparent transparent #f3f4f6 transparent;"
                        + "-fx-border-width:0 0 1 0;");
                fila.setOnMouseEntered(e -> fila.setStyle(
                        "-fx-background-color:#f0f4ff;"
                        + "-fx-border-color:transparent transparent #f3f4f6 transparent;"
                        + "-fx-border-width:0 0 1 0;-fx-cursor:hand;"));
                fila.setOnMouseExited(e -> fila.setStyle(
                        "-fx-border-color:transparent transparent #f3f4f6 transparent;"
                        + "-fx-border-width:0 0 1 0;"));
                fila.setOnMouseClicked(e -> {
                    popup.hide();
                    root.setCenter(new NotificacionesAdminView(notificacionService).getView());
                });

                Circle dot = new Circle(5, Color.web(BLUE));

                VBox info = new VBox(2);
                HBox.setHgrow(info, Priority.ALWAYS);

                Label tituloLbl = new Label(mensaje);
                tituloLbl.setFont(Font.font("System", FontWeight.BOLD, 12));
                tituloLbl.setTextFill(Color.web(TEXT_PRIMARY));

                Label subLbl = new Label(dest + "  ·  " + fecha);
                subLbl.setFont(Font.font("System", 11));
                subLbl.setTextFill(Color.web(GRAY_TEXT));

                info.getChildren().addAll(tituloLbl, subLbl);
                fila.getChildren().addAll(dot, info);
                popupBox.getChildren().add(fila);
            }
        }

        // Vacío
        if (alertasActivas.isEmpty() && ultimasNotifs.isEmpty()) {
            VBox vacio = new VBox(8);
            vacio.setAlignment(Pos.CENTER);
            vacio.setPadding(new Insets(28));

            StackPane icoVacioWrap = new StackPane();
            icoVacioWrap.setPrefSize(56, 56);
            Region icoVacioBg = new Region();
            icoVacioBg.setPrefSize(56, 56);
            icoVacioBg.setStyle("-fx-background-color:#f1f5f9;-fx-background-radius:50%;");
            Label icoVacio = new Label("\uf1f6");
            icoVacio.setStyle(
                    "-fx-font-family:'Font Awesome 6 Free Solid';"
                    + "-fx-font-size:24px;-fx-text-fill:#94a3b8;");
            icoVacioWrap.getChildren().addAll(icoVacioBg, icoVacio);

            Label msgVacio = new Label("Sin notificaciones nuevas");
            msgVacio.setStyle("-fx-font-size:13px;-fx-text-fill:" + GRAY_TEXT + ";");
            vacio.getChildren().addAll(icoVacioWrap, msgVacio);
            popupBox.getChildren().add(vacio);
        }

        // Footer popup
        HBox popFooter = new HBox(24);
        popFooter.setPadding(new Insets(10, 16, 12, 16));
        popFooter.setAlignment(Pos.CENTER);
        popFooter.setStyle(
                "-fx-background-color:#f8fafc;"
                + "-fx-background-radius:0 0 14 14;"
                + "-fx-border-color:#e5e7eb transparent transparent transparent;"
                + "-fx-border-width:1 0 0 0;");

        Label verAlertas = new Label("Ver alertas  →");
        verAlertas.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:" + RED + ";");
        verAlertas.setCursor(javafx.scene.Cursor.HAND);
        verAlertas.setOnMouseClicked(e -> {
            popup.hide();
            root.setCenter(new AlertasAdminView(alertaService).getView());
        });

        Label verNotifs = new Label("Ver notificaciones  →");
        verNotifs.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:" + BLUE + ";");
        verNotifs.setCursor(javafx.scene.Cursor.HAND);
        verNotifs.setOnMouseClicked(e -> {
            popup.hide();
            root.setCenter(new NotificacionesAdminView(notificacionService).getView());
        });

        popFooter.getChildren().addAll(verAlertas, verNotifs);
        popupBox.getChildren().add(popFooter);
        popup.getContent().add(popupBox);

        // Abrir / cerrar popup
        bell.setOnMouseClicked(e -> {
            if (popup.isShowing()) {
                popup.hide();
            } else {
                javafx.geometry.Bounds b = bell.localToScreen(bell.getBoundsInLocal());
                popup.show(bell, b.getMaxX() - 330, b.getMaxY() + 8);
            }
        });

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
                statCard("#e8f0fe", "#1565c0", "\uf0c0",
                        "Usuarios registrados", String.valueOf(totalUsuarios), "Total en el sistema"),
                statCard("#fff0f0", "#e53935", "\uf0f3",
                        "Alertas activas", String.valueOf(alertasActivas), "En este momento"),
                statCard("#fff8e1", "#fb8c00", "\uf201",
                        "Incidentes", String.valueOf(incidentes), "Pendientes de atención"),
                statCard("#e8f5e9", "#43a047", "\uf058",
                        "Resueltos", String.valueOf(resueltas), "Total histórico"));
        return row;
    }

    // ── Paneles: alertas recientes + actividad reciente ───────────
    private HBox buildBottomPanels() {
        HBox bottom = new HBox(20);
        HBox.setHgrow(bottom, Priority.ALWAYS);
        bottom.setFillHeight(true);

        // ── Panel izquierdo — Alertas recientes ───────────────────────
        VBox alertsPanel = new VBox(12);
        alertsPanel.setPadding(new Insets(20));
        alertsPanel.setStyle("-fx-background-color: white; -fx-background-radius: 16;");
        HBox.setHgrow(alertsPanel, Priority.ALWAYS);
        alertsPanel.setMaxWidth(Double.MAX_VALUE);
        alertsPanel.setMaxHeight(Double.MAX_VALUE);
        shadow(alertsPanel);

        HBox headerAlertas = new HBox();
        headerAlertas.setAlignment(Pos.CENTER_LEFT);

        HBox titleRowAlertas = new HBox(8);
        titleRowAlertas.setAlignment(Pos.CENTER_LEFT);

        StackPane iconBoxAlertas = new StackPane();
        iconBoxAlertas.setPrefSize(32, 32);
        iconBoxAlertas.setMinSize(32, 32);
        iconBoxAlertas.setMaxSize(32, 32);
        iconBoxAlertas.setStyle("-fx-background-color: #fff0f0; -fx-background-radius: 8;");
        Label iconLblAlertas = new Label("\uf0f3");
        iconLblAlertas.setStyle(
                "-fx-font-family: 'Font Awesome 6 Free Solid';"
                + "-fx-font-size: 16px; -fx-text-fill: #e53935;");
        iconBoxAlertas.getChildren().add(iconLblAlertas);

        titleRowAlertas.getChildren().addAll(
                iconBoxAlertas,
                label("Alertas recientes", 15, TEXT_PRIMARY, true));
        HBox.setHgrow(titleRowAlertas, Priority.ALWAYS);

        Label verTodas = label("Ver todas  >", 12, BLUE, false);
        verTodas.setCursor(javafx.scene.Cursor.HAND);
        verTodas.setOnMouseClicked(e
                -> root.setCenter(new AlertasAdminView(alertaService).getView()));

        headerAlertas.getChildren().addAll(titleRowAlertas, verTodas);
        alertsPanel.getChildren().addAll(headerAlertas, separator());

        List<Alerta> alertas = cargarAlertas();
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
            alertsPanel.getChildren().add(label("Sin alertas registradas.", 13, GRAY_TEXT, false));
        } else {
            boolean primero = true;
            for (Alerta a : ultimas) {
                if (!primero) {
                    alertsPanel.getChildren().add(separator());
                }
                primero = false;
                String titulo = a.getTipoalerta() != null ? a.getTipoalerta().getNombre() : "Alerta";
                String barrio = a.getBarrio() != null ? a.getBarrio().getNombre() : "—";
                String usuario = a.getUsuario() != null ? a.getUsuario().getPrimer_nombre() : "—";
                alertsPanel.getChildren().add(
                        alertItem(iconAlerta(a), titulo + " en " + barrio,
                                "Reportado por " + usuario, colorEstado(a.getEstado())));
            }
        }

        // ── Panel derecho — Actividad reciente ────────────────────────
        VBox actividadPanel = new VBox(12);
        actividadPanel.setPadding(new Insets(20));
        actividadPanel.setStyle("-fx-background-color: white; -fx-background-radius: 16;");
        HBox.setHgrow(actividadPanel, Priority.ALWAYS);
        actividadPanel.setMaxWidth(Double.MAX_VALUE);
        actividadPanel.setMaxHeight(Double.MAX_VALUE);
        shadow(actividadPanel);

        HBox headerActividad = new HBox(8);
        headerActividad.setAlignment(Pos.CENTER_LEFT);

        StackPane iconBoxAct = new StackPane();
        iconBoxAct.setPrefSize(32, 32);
        iconBoxAct.setMinSize(32, 32);
        iconBoxAct.setMaxSize(32, 32);
        iconBoxAct.setStyle("-fx-background-color: #e8f0fe; -fx-background-radius: 8;");
        Label iconLblAct = new Label("\uf017");
        iconLblAct.setStyle(
                "-fx-font-family: 'Font Awesome 6 Free Solid';"
                + "-fx-font-size: 16px; -fx-text-fill: #1565c0;");
        iconBoxAct.getChildren().add(iconLblAct);

        headerActividad.getChildren().addAll(
                iconBoxAct,
                label("Actividad reciente", 15, TEXT_PRIMARY, true));
        actividadPanel.getChildren().addAll(headerActividad, separator());

        List<Alerta> recientes = alertas.stream()
                .sorted((a, b) -> {
                    if (a.getFechaHora() == null || b.getFechaHora() == null) {
                        return 0;
                    }
                    return b.getFechaHora().compareTo(a.getFechaHora());
                })
                .limit(MAX_ALERTAS_PANEL)
                .collect(Collectors.toList());

        if (recientes.isEmpty()) {
            actividadPanel.getChildren().add(label("Sin actividad reciente.", 13, GRAY_TEXT, false));
        } else {
            boolean primero = true;
            for (Alerta a : recientes) {
                if (!primero) {
                    actividadPanel.getChildren().add(separator());
                }
                primero = false;
                String tipo = a.getTipoalerta() != null ? a.getTipoalerta().getNombre() : "Alerta";
                String barrio = a.getBarrio() != null ? a.getBarrio().getNombre() : "—";
                String fecha = a.getFechaHora() != null ? a.getFechaHora().toLocalDate().toString() : "—";
                actividadPanel.getChildren().add(
                        listItem("\uf0f3", tipo + " — " + barrio, fecha, colorEstado(a.getEstado())));
            }
        }

        bottom.getChildren().addAll(alertsPanel, actividadPanel);
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

    private HBox listItem(String iconFA, String title, String sub, String iconColor) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 0, 8, 0));

        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(ICON_BOX_SM, ICON_BOX_SM);
        iconBox.setMinSize(ICON_BOX_SM, ICON_BOX_SM);
        iconBox.setMaxSize(ICON_BOX_SM, ICON_BOX_SM);
        iconBox.setStyle("-fx-background-color: " + BG + "; -fx-background-radius: 8;");

        Label iconLbl = new Label(iconFA);
        iconLbl.setStyle(
                "-fx-font-family: 'Font Awesome 6 Free Solid';"
                + "-fx-font-size: 16px; -fx-text-fill: " + iconColor + ";");
        iconBox.getChildren().add(iconLbl);

        VBox text = new VBox(2);
        HBox.setHgrow(text, Priority.ALWAYS);
        text.getChildren().addAll(
                label(title, 13, TEXT_PRIMARY, false),
                label(sub, 11, GRAY_TEXT, false));

        row.getChildren().addAll(iconBox, text);
        return row;
    }

    private VBox statCard(String bgIcon, String accentColor, String iconFA,
            String title, String value, String sub) {
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
        iconBg.setArcWidth(16);
        iconBg.setArcHeight(16);
        iconBg.setFill(Color.web(bgIcon));

        Label iconLbl = new Label(iconFA);
        iconLbl.setStyle(
                "-fx-font-family: 'Font Awesome 6 Free Solid';"
                + "-fx-font-size: 22px; -fx-text-fill: " + accentColor + ";");
        iconWrap.getChildren().addAll(iconBg, iconLbl);

        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #374151;");

        Label valueLbl = new Label(value);
        valueLbl.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: " + accentColor + ";");

        Label subLbl = new Label(sub);
        subLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: " + GRAY_TEXT + ";");

        HBox top = new HBox(16);
        top.setAlignment(Pos.CENTER_LEFT);
        top.getChildren().addAll(iconWrap, new VBox(3, titleLbl, valueLbl, subLbl));
        card.getChildren().add(top);

        card.setOnMouseEntered(e -> card.setTranslateY(-3));
        card.setOnMouseExited(e -> card.setTranslateY(0));
        return card;
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
        lbl.setFont(bold ? Font.font("System", FontWeight.BOLD, size) : Font.font("System", size));
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
