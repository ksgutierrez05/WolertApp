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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Popup;

import sistemagestion.model.*;
import sistemagestion.service.*;

/**
 *
 *
 *
 * @author Maria Cristina
 */
public class UsuarioApp {

    // ── Paleta ────────────────────────────────────────────────────
    private static final String WHITE = "#ffffff";
    private static final String BG = "#f4f6fb";
    private static final String RED = "#e53935";
    private static final String RED_LIGHT = "#fff0f0";
    private static final String ORANGE = "#fb8c00";
    private static final String GREEN = "#43a047";
    private static final String BLUE = "#1565c0";
    private static final String BLUE_LIGHT = "#F2F9FF";
    private static final String GRAY_TEXT = "#6b7280";
    private static final String BORDER = "#e5e7eb";

    // ── Servicios ─────────────────────────────────────────────────
    private AlertaService alertaService;
    private NotificacionService notificacionService;
    private SuscripcionService suscripcionService;
    private BarrioService barrioService;

    // ── Usuario logueado ──────────────────────────────────────────
    private final Usuario usuarioActual;

    // ── UI ────────────────────────────────────────────────────────
    private BorderPane root;
    private VBox nav;
    private VBox mapaSubMenu = new VBox(5);
    private boolean mapaExpandido = false;

    // ── Constructor ───────────────────────────────────────────────
    public UsuarioApp(Usuario usuarioActual) {
        this.usuarioActual = usuarioActual;
        Font.loadFont(getClass().getResourceAsStream("/fa-solid-900.ttf"), 20);
        try {
            alertaService = new AlertaService();
            notificacionService = new NotificacionService();
            suscripcionService = new SuscripcionService();
            barrioService = new BarrioService();
        } catch (SQLException e) {
            mostrarAlerta("Error de conexión", e.getMessage());
        }
    }

    // =========================================================================
    // SHOW
    // =========================================================================
    public void show(Stage stage) {
        Font.loadFont(getClass().getResourceAsStream("/fa-solid-900.ttf"), 20);
        root = new BorderPane();
        root.setLeft(buildSidebar());
        root.setCenter(buildMainContent());
        root.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
        root.setStyle("-fx-background-color: " + BG + ";");

        // ── Botón flotante del chatbot ────────────────────────────────
        ChatBotView chatbotView = new ChatBotView(usuarioActual);
        Popup chatPopup = chatbotView.buildPopup(stage);

        Button chatBtn = new Button();
        Label chatIco = new Label("\uf544"); // robot FA
        chatIco.setStyle(
                "-fx-font-family: 'Font Awesome 6 Free Solid';"
                + "-fx-font-size: 22px;"
                + "-fx-text-fill: white;");
        chatBtn.setGraphic(chatIco);
        chatBtn.setStyle(
                "-fx-background-color: #1565c0;"
                + "-fx-background-radius: 50%;"
                + "-fx-min-width: 56px;"
                + "-fx-min-height: 56px;"
                + "-fx-max-width: 56px;"
                + "-fx-max-height: 56px;"
                + "-fx-cursor: hand;"
                + "-fx-effect: dropshadow(gaussian, rgba(21,101,192,0.45), 18, 0, 0, 5);");
        chatBtn.setOnMouseEntered(e -> chatBtn.setStyle(
                "-fx-background-color: #0d47a1;"
                + "-fx-background-radius: 50%;"
                + "-fx-min-width: 56px; -fx-min-height: 56px;"
                + "-fx-max-width: 56px; -fx-max-height: 56px;"
                + "-fx-cursor: hand;"
                + "-fx-effect: dropshadow(gaussian, rgba(21,101,192,0.55), 22, 0, 0, 7);"));
        chatBtn.setOnMouseExited(e -> chatBtn.setStyle(
                "-fx-background-color: #1565c0;"
                + "-fx-background-radius: 50%;"
                + "-fx-min-width: 56px; -fx-min-height: 56px;"
                + "-fx-max-width: 56px; -fx-max-height: 56px;"
                + "-fx-cursor: hand;"
                + "-fx-effect: dropshadow(gaussian, rgba(21,101,192,0.45), 18, 0, 0, 5);"));

        chatBtn.setOnAction(e -> {
            if (chatPopup.isShowing()) {
                chatPopup.hide();
            } else {
                javafx.geometry.Bounds b = chatBtn.localToScreen(chatBtn.getBoundsInLocal());
                chatPopup.show(stage,
                        b.getMaxX() - 370,
                        b.getMinY() - 530);
            }
        });

        StackPane overlay = new StackPane(root, chatBtn);
        StackPane.setAlignment(chatBtn, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(chatBtn, new Insets(0, 24, 24, 0));

        Scene scene = new Scene(overlay, 1200, 620);
        stage.setTitle("WolertApp – Sistema de Alertas Comunitarias");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMaximized(false);
        stage.show();
    }

    // =========================================================================
    // SIDEBAR
    // =========================================================================
    private ScrollPane buildSidebar() {
        VBox sidebar = new VBox();
        sidebar.setPrefWidth(250);
        sidebar.setStyle("-fx-background-color: linear-gradient(to right, #16283d, #1f3a56);");

        // Logo
        HBox logoBox = new HBox(15);
        logoBox.setPadding(new Insets(25, 16, 25, 16));
        logoBox.setAlignment(Pos.CENTER_LEFT);
        ImageView logoView = new ImageView(
                new Image(getClass().getResourceAsStream("/LogoWolertAPP.png"))
        );

        logoView.setFitWidth(70);
        logoView.setFitHeight(70);
        logoView.setPreserveRatio(true);
        logoView.setTranslateX(-2);

        StackPane wolfIcon = new StackPane(logoView);
        VBox logoText = new VBox(2);
        logoText.setTranslateX(-5);
        logoText.getChildren().addAll(
                label("WolertApp", 22, WHITE, true),
                label("Sistema de Alertas Comunitarias", 9, "#8899bb", false)
        );
        logoBox.getChildren().addAll(wolfIcon, logoText);

        // User card con datos reales
        HBox userCard = new HBox(10);
        userCard.setPadding(new Insets(12, 16, 12, 16));
        userCard.setAlignment(Pos.CENTER_LEFT);
        userCard.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 12;");

        Circle avatar = new Circle(20, Color.web("#3a4a70"));
        Label avatarLbl = label("👤", 16, WHITE, false);
        StackPane avatarStack = new StackPane(avatar, avatarLbl);

        VBox userInfo = new VBox(2);
        String nombreCompleto = usuarioActual != null
                ? usuarioActual.getPrimer_nombre() + " " + usuarioActual.getPrimer_apellido()
                : "Usuario";
        String barrioNombre = usuarioActual != null
                && usuarioActual.getDireccion() != null
                && usuarioActual.getDireccion().getBarrio() != null
                ? "Barrio " + usuarioActual.getDireccion().getBarrio().getNombre()
                : "Sin barrio asignado";
        userInfo.getChildren().addAll(
                label(nombreCompleto, 13, WHITE, true),
                label(barrioNombre, 10, "#8899bb", false),
                label("● En línea", 10, GREEN, false)
        );
        userCard.getChildren().addAll(avatarStack, userInfo);

        // Nav — todos los ítems van a placeholder excepto Dashboard
        nav = new VBox(2);
        nav.setPadding(new Insets(16, 8, 16, 8));
        nav.getChildren().addAll(
                navItem("🏠", "Dashboard"),
                navItem("🔔", "Alertas"),
                navItem("🗺️", "Mapa"),
                navItem("👥", "Vecinos"),
                navItem("🔔", "Mis Alertas"),
                navItem("📋", "Mis Suscripciones"),
                navItem("💬", "Notificaciones"),
                navItem("📄", "Mis Reportes"),
                navItem("ℹ️", "Información Útil"),
                navItem("👤", "Perfil"),
                navItem("⚙️", "Configuración")
        );

        // Logout
        String styleLogout
                = "-fx-background-color: white; "
                + "-fx-border-color: white; "
                + "-fx-border-width: 2; "
                + "-fx-text-fill: #16283d; "
                + "-fx-font-size: 13px; "
                + "-fx-font-weight: bold; "
                + "-fx-background-radius: 30; "
                + "-fx-border-radius: 30; "
                + "-fx-padding: 10 35; "
                + "-fx-cursor: hand;";
        String styleLogoutHover
                = "-fx-background-color: #f0f4ff; "
                + "-fx-border-color: white; "
                + "-fx-border-width: 2; "
                + "-fx-text-fill: #16283d; "
                + "-fx-font-size: 13px; "
                + "-fx-font-weight: bold; "
                + "-fx-background-radius: 30; "
                + "-fx-border-radius: 30; "
                + "-fx-padding: 10 35; "
                + "-fx-cursor: hand;";

        Button logoutBtn = new Button("🚪  Cerrar sesión");
        logoutBtn.setStyle(styleLogout);
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setOnMouseEntered(e -> logoutBtn.setStyle(styleLogoutHover));
        logoutBtn.setOnMouseExited(e -> logoutBtn.setStyle(styleLogout));
        logoutBtn.setOnAction(e -> cerrarSesion());

        VBox logoutBox = new VBox(logoutBtn);
        logoutBox.setPadding(new Insets(12, 16, 18, 16));
        logoutBox.setAlignment(Pos.CENTER);

        VBox spacer = new VBox();

        VBox.setVgrow(spacer, Priority.ALWAYS);

        sidebar.getChildren().addAll(
                logoBox,
                userCard,
                nav,
                spacer,
                logoutBox
        );

        sidebar.setMinHeight(Region.USE_PREF_SIZE);

        ScrollPane scroll = new ScrollPane(sidebar);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        scroll.setStyle(
                "-fx-background: transparent;"
                + "-fx-background-color: transparent;"
                + "-fx-border-color: transparent;"
        );

        scroll.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            scroll.lookupAll(".scroll-bar").forEach(node
                    -> node.setStyle(
                            "-fx-opacity: 0.05;"
                            + "-fx-pref-width: 3;"
                    )
            );
        });

        return scroll;

    }

    private HBox navItem(String icon, String text) {

        HBox item = new HBox(12);

        item.setPadding(new Insets(12, 16, 12, 16));
        item.setAlignment(Pos.CENTER_LEFT);
        item.setCursor(javafx.scene.Cursor.HAND);
        item.setMaxWidth(Double.MAX_VALUE);

        String normalStyle = """
    -fx-background-radius: 10;
    -fx-background-color: transparent;
    -fx-focus-color: transparent;
    -fx-faint-focus-color: transparent;
""";

        String hoverStyle = """
    -fx-background-color: #ffffff18;
    -fx-background-radius: 10;
    -fx-focus-color: transparent;
    -fx-faint-focus-color: transparent;
""";
        String activeStyle = """
    -fx-background-color: rgba(255,255,255,0.05);
    -fx-background-radius: 10;
    -fx-focus-color: transparent;
    -fx-faint-focus-color: transparent;
""";

        item.setStyle(normalStyle);

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("""
        -fx-font-size: 18px;
        -fx-font-weight: bold;
        -fx-text-fill: white;
    """);

        Label textLabel = new Label(text);
        textLabel.setStyle("""
        -fx-font-size: 14px;
        -fx-font-weight: bold;
        -fx-text-fill: white;
    """);

        item.getChildren().addAll(iconLabel, textLabel);

        item.setOnMouseEntered(e -> {
            if (!item.getStyle().equals(activeStyle)) {
                item.setStyle(hoverStyle);
            }
        });

        item.setOnMouseExited(e -> {
            if (!item.getStyle().equals(activeStyle)) {
                item.setStyle(normalStyle);
            }
        });

        item.setOnMouseClicked((MouseEvent e) -> {

            for (javafx.scene.Node node : nav.getChildren()) {
                if (node instanceof HBox hbox) {
                    hbox.setStyle(normalStyle);
                }
            }

            switch (text) {

                case "Dashboard" ->
                    root.setCenter(buildMainContent());

                case "Alertas" -> {
                    AlertasView alertasView = new AlertasView(
                            usuarioActual,
                            alertaService,
                            barrioService
                    );
                    root.setCenter(alertasView.getView());
                }

                case "Mis Alertas" -> {
                    MisAlertasView misAlertasView = new MisAlertasView(
                            usuarioActual,
                            alertaService,
                            barrioService,
                            root
                    );
                    root.setCenter(misAlertasView.getView());
                }

                case "Mapa" -> {
                    toggleMapaSubMenu(item);
                }
                case "Vecinos" -> {
                    VecinosView vecinosView = new VecinosView(usuarioActual, alertaService);
                    root.setCenter(vecinosView.getView());
                }
                case "Notificaciones" -> {
                    NotificacionesView view = new NotificacionesView(
                            usuarioActual,
                            notificacionService,
                            root
                    );
                    root.setCenter(view.getView());
                }

                default ->
                    root.setCenter(buildPlaceholder(text));
            }
        });

        return item;
    }

    private void toggleMapaSubMenu(HBox mapaItem) {
        int index = nav.getChildren().indexOf(mapaItem);
        if (!mapaExpandido) {
            mapaSubMenu = buildMapaSubMenu();
            nav.getChildren().add(index + 1, mapaSubMenu);
            mapaSubMenu.setVisible(true);
            mapaExpandido = true;
        } else {
            nav.getChildren().remove(mapaSubMenu);
            mapaExpandido = false;
        }
    }

    private VBox buildMapaSubMenu() {
        VBox sub = new VBox(5);
        sub.setPadding(new Insets(0, 0, 0, 25));
        sub.getChildren().addAll(subItem("📍 Mapa de alertas"), subItem("⚠ Zonas peligrosas"), subItem("👥 Alertas comunitarias"));
        return sub;
    }

    private HBox subItem(String text) {
        HBox item = new HBox();
        item.setPadding(new Insets(8, 10, 8, 10));
        item.setAlignment(Pos.CENTER_LEFT);
        item.setCursor(javafx.scene.Cursor.HAND);
        Label lbl = new Label(text);
        lbl.setTextFill(Color.web("#cbd5e1"));
        lbl.setFont(Font.font(12));
        item.getChildren().add(lbl);
        item.setOnMouseEntered(e -> item.setStyle(
                "-fx-background-color: #ffffff15; -fx-background-radius: 6; -fx-focus-color: transparent; -fx-faint-focus-color: transparent;"));
        item.setOnMouseExited(e -> item.setStyle(
                "-fx-background-color: transparent; -fx-focus-color: transparent; -fx-faint-focus-color: transparent;"));
        item.setOnMouseClicked(e -> root.setCenter(buildPlaceholder(text)));
        return item;
    }

    // =========================================================================
    // MAIN CONTENT — Dashboard
    // =========================================================================
    private ScrollPane buildMainContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color: " + BG + ";");
        content.getChildren().addAll(
                buildTopBar(),
                buildEmergencyBanner(),
                buildStatCards(),
                buildBottomRow(),
                buildFooter()
        );
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setPannable(true); // permite desplazamiento más fluido
        scroll.setFocusTraversable(false);

        scroll.setStyle(
                "-fx-background-color: " + BG + ";"
                + "-fx-background: " + BG + ";"
        );

        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        return scroll;

    }

    // ── Top bar ───────────────────────────────────────────────────
    private HBox buildTopBar() {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);
        VBox greeting = new VBox(4);
        String saludo = usuarioActual != null ? "¡Hola, " + usuarioActual.getPrimer_nombre() + "!" : "¡Hola!";
        Label hello = new Label(saludo);
        hello.setFont(Font.font("System", FontWeight.BOLD, 28));
        hello.setTextFill(Color.web("#111827"));
        greeting.getChildren().addAll(hello, label("Tu seguridad es importante. Estamos aquí para ayudarte.", 13, GRAY_TEXT, false));

        HBox rightBox = new HBox(16);
        rightBox.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(rightBox, Priority.ALWAYS);
        VBox dateBox = new VBox(2);
        dateBox.setAlignment(Pos.CENTER_RIGHT);
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", new Locale("es", "CO"));
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("hh:mm:ss a", new Locale("es", "CO"));
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

        // Badge notificaciones en tiempo real
        int notiCount = contarNotificaciones();
        StackPane bell = new StackPane();
        Label bellIcon = label("🔔", 20, "#374151", false);
        if (notiCount > 0) {
            Circle badge = new Circle(8, Color.web(RED));
            badge.setTranslateX(10);
            badge.setTranslateY(-10);
            Label badgeNum = label(String.valueOf(notiCount), 8, WHITE, true);
            badgeNum.setTranslateX(10);
            badgeNum.setTranslateY(-10);
            bell.getChildren().addAll(bellIcon, badge, badgeNum);
        } else {
            bell.getChildren().add(bellIcon);
        }

        // Al hacer clic en la campana → ir a notificaciones
        bell.setCursor(javafx.scene.Cursor.HAND);
        bell.setOnMouseClicked(e -> {
            NotificacionesView view = new NotificacionesView(
                    usuarioActual,
                    notificacionService,
                    root
            );
            root.setCenter(view.getView());
        });

        rightBox.getChildren().addAll(dateBox, bell);
        bar.getChildren().addAll(greeting, rightBox);
        return bar;
    }

    // ── Banner de emergencia ──────────────────────────────────────
    private HBox buildEmergencyBanner() {
        HBox banner = new HBox();
        banner.setAlignment(Pos.CENTER_LEFT);
        banner.setPadding(new Insets(18, 24, 18, 24));
        banner.setStyle(
                "-fx-background-color:white;"
                + "-fx-background-radius:16;"
                + "-fx-border-color:#fecaca;"
                + "-fx-border-radius:16;"
                + "-fx-border-width:1.5;");
        shadow(banner);

        StackPane iconBox = new StackPane();
        Region iconBg = new Region();
        iconBg.setPrefSize(56, 56);
        iconBg.setStyle("-fx-background-color:" + RED + ";-fx-background-radius:50%;");

        Label bellLbl = new Label("\uf0f3");
        bellLbl.setStyle(
                "-fx-font-family:'Font Awesome 6 Free Solid';"
                + "-fx-font-size:22px;"
                + "-fx-text-fill:white;");
        iconBox.getChildren().addAll(iconBg, bellLbl);

        javafx.animation.ScaleTransition pulse
                = new javafx.animation.ScaleTransition(javafx.util.Duration.seconds(1.1), iconBox);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.13);
        pulse.setToY(1.13);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(javafx.animation.Animation.INDEFINITE);
        pulse.play();

        VBox textBox = new VBox(4);
        textBox.setPadding(new Insets(0, 0, 0, 16));
        Label titulo = new Label("Botón de emergencia");
        titulo.setStyle("-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:" + RED + ";");
        Label sub = new Label("Envía alerta inmediata a vecinos y autoridades");
        sub.setStyle("-fx-font-size:12px;-fx-text-fill:" + GRAY_TEXT + ";");
        textBox.getChildren().addAll(titulo, sub);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        Button panicBtn = new Button("⚠  PÁNICO");
        String base = "-fx-background-color:" + RED + ";-fx-text-fill:white;"
                + "-fx-font-size:15px;-fx-font-weight:bold;"
                + "-fx-background-radius:30;-fx-padding:13 32;-fx-cursor:hand;"
                + "-fx-effect:dropshadow(gaussian,rgba(229,57,53,0.42),18,0,0,5);";
        String hover = "-fx-background-color:#c62828;-fx-text-fill:white;"
                + "-fx-font-size:15px;-fx-font-weight:bold;"
                + "-fx-background-radius:30;-fx-padding:13 32;-fx-cursor:hand;"
                + "-fx-effect:dropshadow(gaussian,rgba(198,40,40,0.58),22,0,0,7);";
        panicBtn.setStyle(base);
        panicBtn.setOnMouseEntered(e -> panicBtn.setStyle(hover));
        panicBtn.setOnMouseExited(e -> panicBtn.setStyle(base));
        panicBtn.setOnAction(e -> {
            Stage stage = (Stage) panicBtn.getScene().getWindow();
            MapaAlerta mapaAlerta = new MapaAlerta(stage, usuarioActual, alertaService, barrioService);
            mapaAlerta.mostrar();
        });

        banner.getChildren().addAll(iconBox, textBox, panicBtn);
        return banner;
    }

    // ── Stat cards — datos reales ─────────────────────────────────
    private HBox buildStatCards() {
        HBox row = new HBox(16);

        long incidentesMes = 0, alertasPendientes = 0, vecinosActivos = 0;
        if (alertaService != null && usuarioActual != null) {
            try {
                List<Alerta> misAlertas = alertaService.listar().stream()
                        .filter(a -> usuarioActual.getUsername().equals(
                        a.getUsuario() != null ? a.getUsuario().getUsername() : ""))
                        .toList();
                incidentesMes = misAlertas.stream()
                        .filter(a -> a.getFechaHora() != null
                        && a.getFechaHora().getMonth() == LocalDateTime.now().getMonth()
                        && a.getFechaHora().getYear() == LocalDateTime.now().getYear())
                        .count();
                alertasPendientes = misAlertas.stream()
                        .filter(a -> a.getEstado() == EstadoAlerta.PENDIENTE
                        || a.getEstado() == EstadoAlerta.EN_ATENCION)
                        .count();
            } catch (Exception ignored) {
            }
        }
        if (alertaService != null && usuarioActual != null
                && usuarioActual.getDireccion() != null
                && usuarioActual.getDireccion().getBarrio() != null) {
            try {
                String miBarrio = usuarioActual.getDireccion().getBarrio().getNombre();
                vecinosActivos = alertaService.listar().stream()
                        .filter(a -> a.getBarrio() != null
                        && miBarrio.equalsIgnoreCase(a.getBarrio().getNombre()))
                        .map(a -> a.getUsuario() != null ? a.getUsuario().getUsername() : "")
                        .distinct().count();
            } catch (Exception ignored) {
            }
        }

        row.getChildren().addAll(
                statCard("#fff0f0", "#e53935", "Incidentes este mes",
                        incidentesMes, "+3 vs mes anterior"),
                statCard("#fff8e1", "#fb8c00", "Alertas pendientes",
                        alertasPendientes, "PENDIENTE / EN PROCESO"),
                statCard("#e8f5e9", "#43a047", "Vecinos del barrio",
                        vecinosActivos, "Reportaron en tu barrio")
        );
        return row;
    }

    private VBox statCard(String bgIcon, String accentColor,
            String title, long value, String sub) {

        VBox card = new VBox(10);
        card.setPadding(new Insets(20, 22, 20, 22));
        card.setStyle("-fx-background-color:white;-fx-background-radius:18;");
        HBox.setHgrow(card, Priority.ALWAYS);
        shadow(card);

        // ── Nombre del PNG según color ─────────────────────────────────
        String iconName = accentColor.equals("#e53935") ? "IncidentesPin"
                : accentColor.equals("#fb8c00") ? "AlertasPendientesPin"
                : "VecinoPin";

        // ── Fondo + icono PNG ──────────────────────────────────────────
        StackPane iconWrap = new StackPane();
        iconWrap.setPrefSize(52, 52);
        iconWrap.setMinSize(52, 52);
        iconWrap.setMaxSize(52, 52);

        Region colorBg = new Region();
        colorBg.setPrefSize(52, 52);
        colorBg.setStyle(
                "-fx-background-color:" + bgIcon + ";"
                + "-fx-background-radius:14;");

        ImageView iv = new ImageView();
        iv.setFitWidth(28);
        iv.setFitHeight(28);
        iv.setPreserveRatio(true);

        try {

            java.io.InputStream is = getClass()
                    .getResourceAsStream("/" + iconName + ".png");

            if (is != null) {

                java.awt.image.BufferedImage original
                        = javax.imageio.ImageIO.read(is);

                java.awt.image.BufferedImage recortada
                        = recortarTransparencia(original);

                javafx.scene.image.WritableImage fxImage
                        = javafx.embed.swing.SwingFXUtils
                                .toFXImage(recortada, null);

                iv.setImage(fxImage);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        iconWrap.getChildren().addAll(colorBg, iv);

        // ── Número ─────────────────────────────────────────────────────
        Label valLbl = new Label(String.valueOf(value));
        valLbl.setStyle(
                "-fx-font-size:36px;"
                + "-fx-font-weight:bold;"
                + "-fx-text-fill:" + accentColor + ";");

        // ── Título ─────────────────────────────────────────────────────
        Label titleLbl = new Label(title);
        titleLbl.setStyle(
                "-fx-font-size:13px;"
                + "-fx-font-weight:bold;"
                + "-fx-text-fill:#374151;");

        // ── Subtexto ───────────────────────────────────────────────────
        Label subLbl = new Label(sub);
        subLbl.setStyle(
                "-fx-font-size:11px;"
                + "-fx-text-fill:" + GRAY_TEXT + ";");

        VBox textBlock = new VBox(3, titleLbl, valLbl, subLbl);

        HBox topRow = new HBox(16, iconWrap, textBlock);
        topRow.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().add(topRow);

        card.setOnMouseEntered(e -> card.setTranslateY(-3));
        card.setOnMouseExited(e -> card.setTranslateY(0));

        return card;
    }

    // ── Bottom row: lista alertas + mapa ─────────────────────────
    private HBox buildBottomRow() {
        HBox row = new HBox(16);
        row.getChildren().addAll(buildAlertsList(), buildMapPanel());
        return row;
    }

    private VBox buildAlertsList() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.setPrefWidth(480);
        card.setStyle("-fx-background-color:" + WHITE + ";-fx-background-radius:12;");
        HBox.setHgrow(card, Priority.ALWAYS);
        shadow(card);

        // ── Header ────────────────────────────────────────────────────
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        HBox titleRow = new HBox(8);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(32, 32);
        iconBox.setMinSize(32, 32);
        iconBox.setMaxSize(32, 32);
        iconBox.setStyle(
                "-fx-background-color:#e8f0fe;"
                + "-fx-background-radius:8;");
        Label iconLbl = new Label("\uf133");
        iconLbl.setStyle(
                "-fx-font-family:'Font Awesome 6 Free Solid';"
                + "-fx-font-size:16px;"
                + "-fx-text-fill:#1565c0;");
        iconBox.getChildren().add(iconLbl);

        titleRow.getChildren().addAll(iconBox, label("Alertas recientes", 15, "#111827", true));
        HBox.setHgrow(titleRow, Priority.ALWAYS);

        Label verTodas = label("Ver todas  >", 12, BLUE, false);
        verTodas.setCursor(javafx.scene.Cursor.HAND);

        verTodas.setOnMouseClicked(e -> {
            AlertasView alertasView = new AlertasView(
                    usuarioActual,
                    alertaService,
                    barrioService
            );
            root.setCenter(alertasView.getView());
        });

        header.getChildren().addAll(titleRow, verTodas);
        card.getChildren().addAll(header, separator());

        // ── Contenido ─────────────────────────────────────────────────
        if (alertaService != null) {
            try {
                List<Alerta> alertas = alertaService.listar();
                String miBarrio = usuarioActual != null
                        && usuarioActual.getDireccion() != null
                        && usuarioActual.getDireccion().getBarrio() != null
                        ? usuarioActual.getDireccion().getBarrio().getNombre() : null;

                List<Alerta> filtradas = miBarrio != null
                        ? alertas.stream()
                                .filter(a -> a.getBarrio() != null
                                && miBarrio.equalsIgnoreCase(a.getBarrio().getNombre()))
                                .toList()
                        : alertas;

                if (filtradas.isEmpty()) {
                    card.getChildren().add(
                            label("No hay alertas en tu barrio", 13, GRAY_TEXT, false));
                } else {
                    filtradas.stream().limit(4).forEach(a -> {
                        card.getChildren().addAll(alertaItem(a), separator());
                    });
                }
            } catch (Exception e) {
                card.getChildren().add(label("Error al cargar alertas", 13, RED, false));
            }
        } else {
            card.getChildren().add(
                    label("Sin conexión al servicio de alertas", 13, GRAY_TEXT, false));
        }

        return card;
    }

    private HBox alertaItem(Alerta a) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 4, 10, 4));
        row.setCursor(javafx.scene.Cursor.HAND);
        row.setStyle("-fx-background-color:transparent;-fx-background-radius:8;");
        row.setOnMouseEntered(e -> row.setStyle(
                "-fx-background-color:#f8f9fb;-fx-background-radius:8;"));
        row.setOnMouseExited(e -> row.setStyle(
                "-fx-background-color:transparent;-fx-background-radius:8;"));

        // ── Punto de estado (pequeño, a la izquierda) ──────────────────
        String estado = a.getEstado() != null ? a.getEstado().name() : "—";
        String dotColor = switch (estado) {
            case "PENDIENTE" ->
                RED;
            case "EN_ATENCION" ->
                ORANGE;
            case "RESUELTA" ->
                GREEN;
            default ->
                GRAY_TEXT;
        };
        Circle dot = new Circle(5, Color.web(dotColor));

        // ── Ícono circular con fondo de color según tipo ───────────────
        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(40, 40);
        iconBox.setMinSize(40, 40);
        iconBox.setMaxSize(40, 40);

        String tipo = a.getTipoalerta() != null
                ? a.getTipoalerta().getNombre().toUpperCase() : "";

        // Color de fondo del círculo según tipo de alerta
        String circleBg = switch (tipo) {
            case "ROBO", "HURTO" ->
                "#fce4ec"; // rosa
            case "SOSPECHOSO", "PERSONA" ->
                "#fce4ec";
            case "ANIMAL" ->
                "#fff8e1"; // amarillo
            case "ACCIDENTE" ->
                "#fff3e0"; // naranja
            case "VANDALISMO" ->
                "#e8eaf6"; // morado suave
            default ->
                "#e3f2fd"; // azul suave
        };

        // Ícono CSS (forma geométrica) según tipo — sin emojis
        Region iconShape = new Region();
        iconShape.setPrefSize(20, 20);

        String iconStyle = switch (tipo) {
            case "ROBO", "HURTO" ->
                // Candado: rectángulo + arco simulado
                "-fx-background-color:" + dotColor + ";"
                + "-fx-background-radius:3 3 2 2;"
                + "-fx-pref-width:14px;-fx-pref-height:11px;";
            case "ANIMAL" ->
                // Rombo
                "-fx-background-color:" + dotColor + ";"
                + "-fx-background-radius:3;"
                + "-fx-pref-width:14px;-fx-pref-height:14px;"
                + "-fx-rotate:45;";
            default ->
                // Círculo sólido genérico
                "-fx-background-color:" + dotColor + ";"
                + "-fx-background-radius:50%;"
                + "-fx-pref-width:14px;-fx-pref-height:14px;";
        };
        iconShape.setStyle(iconStyle);
        iconShape.setPrefSize(14, 14);
        iconShape.setMinSize(14, 14);
        iconShape.setMaxSize(14, 14);

        Region circleBgRegion = new Region();
        circleBgRegion.setPrefSize(40, 40);
        circleBgRegion.setStyle(
                "-fx-background-color:" + circleBg + ";"
                + "-fx-background-radius:50%;");

        iconBox.getChildren().addAll(circleBgRegion, iconShape);

        // ── Texto ──────────────────────────────────────────────────────
        VBox text = new VBox(3);
        HBox.setHgrow(text, Priority.ALWAYS);

        String tipoNombre = a.getTipoalerta() != null ? a.getTipoalerta().getNombre() : "Alerta";
        String barrio = a.getBarrio() != null ? " en " + a.getBarrio().getNombre() : "";

        // Tiempo transcurrido aproximado
        String tiempo = "Hace un momento";
        if (a.getFechaHora() != null) {
            long mins = java.time.Duration.between(
                    a.getFechaHora(), LocalDateTime.now()).toMinutes();
            if (mins < 60) {
                tiempo = "Hace " + mins + " min";
            } else if (mins < 1440) {
                tiempo = "Hace " + (mins / 60) + " hora" + (mins / 60 > 1 ? "s" : "");
            } else {
                tiempo = "Hace " + (mins / 1440) + " día" + (mins / 1440 > 1 ? "s" : "");
            }
        }

        String desc = a.getDescripcion() != null && a.getDescripcion().length() > 50
                ? a.getDescripcion().substring(0, 50) + "…" : a.getDescripcion();
        String reportado = a.getUsuario() != null
                ? " · Reportado por " + a.getUsuario().getPrimer_nombre()
                + " " + a.getUsuario().getPrimer_apellido().charAt(0) + "." : "";

        Label titleLbl = new Label(
                (desc != null && !desc.isBlank() ? desc : tipoNombre + barrio));
        titleLbl.setStyle(
                "-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#1e293b;");
        titleLbl.setWrapText(true);

        Label subLbl = new Label(tiempo + reportado);
        subLbl.setStyle("-fx-font-size:11px;-fx-text-fill:" + GRAY_TEXT + ";");

        text.getChildren().addAll(titleLbl, subLbl);

        // ── Chevron ────────────────────────────────────────────────────
        Label chevron = new Label("›");
        chevron.setStyle(
                "-fx-font-size:18px;-fx-text-fill:#cbd5e1;-fx-font-weight:bold;");

        row.getChildren().addAll(dot, iconBox, text, chevron);
        return row;
    }

    private VBox buildMapPanel() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.setPrefWidth(380);
        card.setStyle("-fx-background-color: " + WHITE + "; -fx-background-radius: 12;");
        shadow(card);

        // ── Header con círculo azul claro + emoji 📍 ──────────────────
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);

        StackPane pinBox = new StackPane();
        pinBox.setPrefSize(32, 32);
        pinBox.setMinSize(32, 32);
        pinBox.setMaxSize(32, 32);
        pinBox.setStyle(
                "-fx-background-color:#e8f0fe;"
                + "-fx-background-radius:50%;");
        Label pinLbl = new Label("\uf3c5");
        pinLbl.setStyle(
                "-fx-font-family:'Font Awesome 6 Free Solid';"
                + "-fx-font-size:15px;"
                + "-fx-text-fill:#1565c0");
        pinBox.getChildren().add(pinLbl);

        header.getChildren().addAll(pinBox, label("Mapa del barrio", 15, "#111827", true));

        // ── Área del mapa ─────────────────────────────────────────────
        StackPane mapArea = new StackPane();
        mapArea.setPrefHeight(220);

        Rectangle mapBg = new Rectangle();
        mapBg.setFill(Color.web("#d1e8d1"));
        mapBg.widthProperty().bind(mapArea.widthProperty());
        mapBg.heightProperty().bind(mapArea.heightProperty());
        mapBg.setArcWidth(10);
        mapBg.setArcHeight(10);

        Pane streets = new Pane();
        streets.setPrefSize(340, 200);
        for (int i = 0; i < 4; i++) {
            Rectangle h = new Rectangle(340, 3);
            h.setFill(Color.web("#b8d4b8"));
            h.setY(40 + i * 45);
            streets.getChildren().add(h);
        }
        for (int i = 0; i < 5; i++) {
            Rectangle v = new Rectangle(3, 200);
            v.setFill(Color.web("#b8d4b8"));
            v.setX(40 + i * 65);
            streets.getChildren().add(v);
        }
        streets.getChildren().addAll(
                mapDot(80, 60, RED),
                mapDot(200, 40, ORANGE),
                mapDot(300, 55, GREEN),
                mapDot(60, 140, GREEN),
                mapDot(310, 150, RED)
        );

        // ── Popup con botón "Abrir mapa" ──────────────────────────────
        VBox popup = new VBox(6);
        popup.setPadding(new Insets(10, 14, 10, 14));
        popup.setStyle(
                "-fx-background-color:white;"
                + "-fx-background-radius:8;");
        popup.setEffect(new DropShadow(8, Color.web("#0000001a")));
        popup.setTranslateX(30);
        popup.setTranslateY(10);
        popup.setAlignment(Pos.CENTER_LEFT);

        String barrioDisplay = usuarioActual != null
                && usuarioActual.getDireccion() != null
                && usuarioActual.getDireccion().getBarrio() != null
                ? usuarioActual.getDireccion().getBarrio().getNombre()
                : "Tu barrio";

        Button abrirMapaBtn = new Button("Abrir mapa  ↗");
        abrirMapaBtn.setStyle(
                "-fx-background-color:" + BLUE + ";"
                + "-fx-text-fill:white;"
                + "-fx-font-size:12px;"
                + "-fx-font-weight:bold;"
                + "-fx-background-radius:6;"
                + "-fx-padding:7 16;"
                + "-fx-cursor:hand;");
        abrirMapaBtn.setOnMouseEntered(e -> abrirMapaBtn.setStyle(
                "-fx-background-color:#0d47a1;"
                + "-fx-text-fill:white;"
                + "-fx-font-size:12px;"
                + "-fx-font-weight:bold;"
                + "-fx-background-radius:6;"
                + "-fx-padding:7 16;"
                + "-fx-cursor:hand;"));
        abrirMapaBtn.setOnMouseExited(e -> abrirMapaBtn.setStyle(
                "-fx-background-color:" + BLUE + ";"
                + "-fx-text-fill:white;"
                + "-fx-font-size:12px;"
                + "-fx-font-weight:bold;"
                + "-fx-background-radius:6;"
                + "-fx-padding:7 16;"
                + "-fx-cursor:hand;"));
        abrirMapaBtn.setOnAction(e -> root.setCenter(buildPlaceholder("Mapa")));

        popup.getChildren().addAll(
                label("Mapa interactivo", 12, "#374151", true),
                label(barrioDisplay, 11, BLUE, false),
                abrirMapaBtn
        );

        mapArea.getChildren().addAll(mapBg, streets, popup);

        // ── Leyenda ───────────────────────────────────────────────────
        HBox legend = new HBox(16);
        legend.setAlignment(Pos.CENTER);
        legend.getChildren().addAll(
                legendItem(RED, "Activo"),
                legendItem(ORANGE, "En revisión"),
                legendItem(GREEN, "Resuelto")
        );

        card.getChildren().addAll(header, mapArea, legend);
        return card;
    }

    // =========================================================================
    // FOOTER
    // =========================================================================
    private HBox buildFooter() {
        HBox footer = new HBox();
        footer.setPadding(new Insets(30, 50, 30, 50));
        footer.setPrefHeight(120);
        footer.setStyle("-fx-background-color:" + BLUE_LIGHT + ";-fx-background-radius:10;");

        // Círculo azul claro con 🔒
        StackPane lockBox = new StackPane();
        lockBox.setPrefSize(60, 60);
        lockBox.setMinSize(60, 60);
        lockBox.setMaxSize(60, 60);
        lockBox.setStyle(
                "-fx-background-color:#dbeafe;"
                + "-fx-background-radius:50%;");
        Label lockLbl = new Label("\uf023");  // lock FA
        lockLbl.setStyle(
                "-fx-font-family:'Font Awesome 6 Free Solid';"
                + "-fx-font-size:25px;"
                + "-fx-text-fill:#1565c0;");
        lockBox.getChildren().add(lockLbl);

        HBox left = new HBox(10);
        left.setAlignment(Pos.CENTER_LEFT);
        VBox leftText = new VBox(2);
        leftText.getChildren().addAll(
                label("Tu información está protegida", 16, BLUE, true),
                label("Todas tus alertas son anónimas y confidenciales.", 13, GRAY_TEXT, false)
        );
        left.getChildren().addAll(lockBox, leftText);
        HBox.setHgrow(left, Priority.ALWAYS);

        // Círculo azul claro con 🛡
        StackPane shieldBox = new StackPane();
        shieldBox.setPrefSize(50, 50);
        shieldBox.setMinSize(50, 50);
        shieldBox.setMaxSize(50, 50);
        shieldBox.setStyle(
                "-fx-background-color:#dbeafe;"
                + "-fx-background-radius:50%;");
        Label shieldLbl = new Label("\uf132");  // shield FA
        shieldLbl.setStyle(
                "-fx-font-family:'Font Awesome 6 Free Solid';"
                + "-fx-font-size:25px;"
                + "-fx-text-fill:#1565c0;");
        shieldBox.getChildren().add(shieldLbl);

        HBox right = new HBox(10);
        right.setAlignment(Pos.CENTER_RIGHT);
        VBox rightText = new VBox(2);
        rightText.setAlignment(Pos.CENTER_RIGHT);
        rightText.getChildren().addAll(
                label("100% Anónimo", 16, BLUE, true),
                label("Tu identidad no será revelada.", 13, GRAY_TEXT, false)
        );
        right.getChildren().addAll(rightText, shieldBox);

        footer.getChildren().addAll(left, right);
        return footer;
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
    // CONTADORES EN VIVO desde BD
    // =========================================================================
    private int contarAlertasActivas() {
        if (alertaService == null) {
            return 0;
        }
        try {
            return (int) alertaService.listar().stream()
                    .filter(a -> a.getEstado() == EstadoAlerta.PENDIENTE
                    || a.getEstado() == EstadoAlerta.EN_ATENCION)
                    .count();
        } catch (Exception e) {
            return 0;
        }
    }

    private int contarNotificaciones() {
        if (notificacionService == null || usuarioActual == null) {
            return 0;
        }
        try {
            return (int) notificacionService.listar().stream()
                    .filter(n -> usuarioActual.getCorreo() != null
                    && usuarioActual.getCorreo().equalsIgnoreCase(n.getCorreodestinatario()))
                    .count();
        } catch (Exception e) {
            return 0;
        }
    }

    // =========================================================================
    // PLACEHOLDER
    // =========================================================================
    private ScrollPane buildPlaceholder(String nombre) {
        VBox box = new VBox(20);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(40));
        box.setStyle("-fx-background-color: " + BG + ";");
        Label icon = new Label("🚧");
        icon.setFont(Font.font(70));
        Label title = new Label(nombre);
        title.setFont(Font.font("System", FontWeight.BOLD, 32));
        title.setTextFill(Color.web("#111827"));
        Label msg = new Label("Pantalla en construcción");
        msg.setFont(Font.font(18));
        msg.setTextFill(Color.GRAY);
        Button volver = new Button("Volver al Dashboard");
        volver.setStyle("-fx-background-color: " + BLUE + "; -fx-text-fill: white; -fx-font-size: 14px; -fx-background-radius: 10; -fx-padding: 10 20; -fx-cursor: hand;");
        volver.setOnAction(e -> root.setCenter(buildMainContent()));
        box.getChildren().addAll(icon, title, msg, volver);
        ScrollPane scroll = new ScrollPane(box);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: " + BG + "; -fx-background-color: " + BG + ";");
        return scroll;
    }

    // =========================================================================
    // HELPERS UI
    // =========================================================================
    private VBox tituloVista(String titulo, String subtitulo) {
        VBox v = new VBox(4);
        Label t = new Label(titulo);
        t.setFont(Font.font("System", FontWeight.BOLD, 26));
        t.setTextFill(Color.web("#111827"));
        v.getChildren().addAll(t, label(subtitulo, 13, GRAY_TEXT, false));
        return v;
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
        item.getChildren().addAll(new Circle(5, Color.web(color)), label(text, 11, GRAY_TEXT, false));
        return item;
    }

    private String estadoBg(String estado) {
        return switch (estado) {
            case "PENDIENTE" ->
                RED_LIGHT;
            case "EN_PROCESO" ->
                "#fff8e1";
            case "RESUELTA" ->
                "#e8f5e9";
            default ->
                "#f3f4f6";
        };
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

        DropShadow shadow = new DropShadow();

        shadow.setRadius(18);

        shadow.setOffsetY(4);

        shadow.setColor(Color.rgb(15, 23, 42, 0.10));

        node.setEffect(shadow);
    }

    private void mostrarAlerta(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    // ── Utilidad AWT ──────────────────────────────────────────────────────────
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
