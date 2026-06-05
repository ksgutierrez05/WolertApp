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
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Popup;
import javafx.stage.Screen;

import sistemagestion.model.*;
import sistemagestion.service.*;
import sistemagestion.service.NotificacionService;

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

    private static final double SIDEBAR_EXPANDED_U = 250;
    private static final double SIDEBAR_COLLAPSED_U = 60;
    private VBox sidebarVBoxU;
    private VBox logoTextUsuario;
    private Label logoutTextUsuario;

    // ── Servicios ─────────────────────────────────────────────────
    private AlertaService alertaService;
    private SuscripcionService suscripcionService;
    private BarrioService barrioService;
    private NotificacionService notificacionService;

    // ── Usuario logueado ──────────────────────────────────────────
    private final Usuario usuarioActual;

    // ── UI ────────────────────────────────────────────────────────
    private BorderPane root;
    private VBox nav;
    // FIX 1: mapaSubMenu inicializado como null para controlar su estado correctamente
    private VBox mapaSubMenu = null;
    private boolean mapaExpandido = false;

    // ── Constructor ───────────────────────────────────────────────
    public UsuarioApp(Usuario usuarioActual) {
        this.usuarioActual = usuarioActual;
        Font.loadFont(getClass().getResourceAsStream("/fa-solid-900.ttf"), 20);
        try {
            alertaService = new AlertaService();
        } catch (SQLException e) {
            mostrarAlerta("Error", e.getMessage());
        }
        try {
            suscripcionService = new SuscripcionService();
        } catch (SQLException e) {
            mostrarAlerta("Error", e.getMessage());
        }
        try {
            barrioService = new BarrioService();
        } catch (SQLException e) {
            mostrarAlerta("Error", e.getMessage());
        }
        try {
            notificacionService = new NotificacionService();
        } catch (SQLException e) {
            mostrarAlerta("Error", e.getMessage());
        }
    }

    // =========================================================================
    // SHOW
    // =========================================================================
    public void show(Stage stage) {
        // FIX 2: se elimina la carga duplicada de la fuente (ya se hace en el constructor)
        root = new BorderPane();
        root.setLeft(buildSidebar());
        root.setCenter(buildMainContent());
        root.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
        root.setStyle("-fx-background-color: " + BG + "; -fx-padding: 0;");

        // ── Botón flotante del chatbot ────────────────────────────────
        ChatBotView chatbotView = new ChatBotView(usuarioActual);
        Popup chatPopup = chatbotView.buildPopup(stage);

        Button chatBtn = new Button();
        Label chatIco = new Label("\uf544");
        chatIco.setStyle(
                "-fx-font-family: 'Font Awesome 6 Free Solid';"
                + "-fx-font-size: 22px;"
                + "-fx-text-fill: white;");
        chatBtn.setGraphic(chatIco);

        String chatBase = "-fx-background-color:#1f3a56;-fx-text-fill:#F5F7FA;-fx-background-radius:50%;"
                + "-fx-min-width:56px;-fx-min-height:56px;-fx-max-width:56px;-fx-max-height:56px;"
                + "-fx-cursor:hand;-fx-effect:dropshadow(gaussian,rgba(31,58,86,0.55),18,0,0,5);";
        String chatHover = "-fx-background-color:#16283d;-fx-text-fill:#F5F7FA;-fx-background-radius:50%;"
                + "-fx-min-width:56px;-fx-min-height:56px;-fx-max-width:56px;-fx-max-height:56px;"
                + "-fx-cursor:hand;-fx-effect:dropshadow(gaussian,rgba(22,40,61,0.70),22,0,0,7);";

        chatBtn.setStyle(chatBase);
        chatBtn.setOnMouseEntered(e -> chatBtn.setStyle(chatHover));
        chatBtn.setOnMouseExited(e -> chatBtn.setStyle(chatBase));
        chatBtn.setOnAction(e -> {
            if (chatPopup.isShowing()) {
                chatPopup.hide();
            } else {
                javafx.geometry.Bounds b = chatBtn.localToScreen(chatBtn.getBoundsInLocal());
                chatPopup.show(stage, b.getMaxX() - 370, b.getMinY() - 530);
            }
        });

        StackPane overlay = new StackPane(root, chatBtn);
        StackPane.setAlignment(chatBtn, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(chatBtn, new Insets(0, 24, 24, 0));
        overlay.setStyle("-fx-padding: 0;");

        Screen screen = Screen.getPrimary();
        double w = screen.getVisualBounds().getWidth();
        double h = screen.getVisualBounds().getHeight();

        Scene scene = new Scene(overlay, w * 0.85, h * 0.85);
        stage.setTitle("WolertApp – Sistema de Alertas Comunitarias");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMinWidth(900);
        stage.setMinHeight(580);
        stage.show();
    }

    // =========================================================================
    // SIDEBAR
    // =========================================================================
    private ScrollPane buildSidebar() {
        sidebarVBoxU = new VBox();
        sidebarVBoxU.setPrefWidth(SIDEBAR_COLLAPSED_U);
        sidebarVBoxU.setMinWidth(SIDEBAR_COLLAPSED_U);
        sidebarVBoxU.setMaxWidth(SIDEBAR_COLLAPSED_U);
        sidebarVBoxU.setMaxHeight(Double.MAX_VALUE);
        sidebarVBoxU.setFillWidth(true);
        VBox.setVgrow(sidebarVBoxU, Priority.ALWAYS);
        sidebarVBoxU.setStyle(
                "-fx-background-color: linear-gradient(to right, #16283d, #1f3a56); -fx-padding: 0;");

        // ── Logo ──────────────────────────────────────────────────
        HBox logoBox = new HBox(10);
        logoBox.setPadding(new Insets(16, 8, 16, 8));
        logoBox.setAlignment(Pos.CENTER_LEFT);
        javafx.scene.shape.Rectangle logoClip
                = new javafx.scene.shape.Rectangle(SIDEBAR_EXPANDED_U, 80);
        logoBox.setClip(logoClip);

        ImageView logoImg = new ImageView(
                new Image(getClass().getResourceAsStream("/LogoWolertAPP.png"),
                        128, 128, true, true));
        logoImg.setFitHeight(44);
        logoImg.setFitWidth(44);
        logoImg.setPreserveRatio(true);
        logoImg.setSmooth(true);

        StackPane logoWrap = new StackPane(logoImg);
        logoWrap.setPrefSize(44, 44);
        logoWrap.setMinSize(44, 44);
        logoWrap.setMaxSize(44, 44);

        logoTextUsuario = new VBox(1);
        logoTextUsuario.setAlignment(Pos.CENTER_LEFT);
        logoTextUsuario.getChildren().addAll(
                label("WolertApp", 15, WHITE, true),
                label("Sistema de Alertas", 9, "#8899bb", false));
        logoTextUsuario.setVisible(false);
        logoTextUsuario.setManaged(false);
        logoBox.getChildren().addAll(logoWrap, logoTextUsuario);

        // ── User card ─────────────────────────────────────────────
        HBox userCard = new HBox(12);
        userCard.setPadding(new Insets(10, 8, 10, 14));
        userCard.setAlignment(Pos.CENTER_LEFT);
        userCard.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 12;");

        Circle avatar = new Circle(18, Color.web("#334155"));
        Label avatarLbl = new Label("\uf007");
        avatarLbl.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';"
                + "-fx-font-size:14px; -fx-text-fill:#a8c0dd;");
        StackPane avatarBox = new StackPane(avatar, avatarLbl);
        avatarBox.setPrefSize(36, 36);
        avatarBox.setMinSize(36, 36);
        avatarBox.setMaxSize(36, 36);

        VBox userInfo = new VBox(2);
        String nombreCompleto = usuarioActual != null
                ? usuarioActual.getPrimer_nombre() + " " + usuarioActual.getPrimer_apellido()
                : "Usuario";
        String barrioNombre = usuarioActual != null
                && usuarioActual.getDireccion() != null
                && usuarioActual.getDireccion().getBarrio() != null
                ? "Barrio " + usuarioActual.getDireccion().getBarrio().getNombre()
                : "Sin barrio asignado";

        HBox statusRow = new HBox(4);
        statusRow.setAlignment(Pos.CENTER_LEFT);
        statusRow.getChildren().addAll(
                new Circle(4, Color.web(GREEN)),
                label("En línea", 10, GREEN, false));
        userInfo.getChildren().addAll(
                label(nombreCompleto, 12, WHITE, true),
                label(barrioNombre, 9, "#8899bb", false),
                statusRow);
        userCard.getChildren().addAll(avatarBox, userInfo);

        // ── Nav ───────────────────────────────────────────────────
        nav = new VBox(2);
        nav.setPadding(new Insets(12, 4, 12, 4));
        nav.getChildren().addAll(
                navItem("\uf015", "Dashboard"),
                navItem("\uf0f3", "Alertas"),
                navItem("\uf279", "Mapa"),
                navItem("\uf500", "Vecinos"),
                navItem("\uf46d", "Mis Alertas"),
                navItem("\uf1f6", "Notificaciones"),
                navItem("\uf007", "Mi Cuenta")
        );

        VBox spacer = new VBox();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // ── Logout ────────────────────────────────────────────────
        HBox logout = new HBox(10);
        logout.setPadding(new Insets(14, 8, 18, 14));
        logout.setAlignment(Pos.CENTER_LEFT);
        logout.setCursor(javafx.scene.Cursor.HAND);
        logout.setStyle("-fx-background-color: transparent;");
        logout.setOnMouseEntered(e -> logout.setStyle(
                "-fx-background-color: rgba(229,57,53,0.15); -fx-background-radius: 8;"));
        logout.setOnMouseExited(e -> logout.setStyle("-fx-background-color: transparent;"));
        logout.setOnMouseClicked(e -> cerrarSesion());

        Label logoutIcon = new Label("\uf2f5");
        logoutIcon.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';"
                + "-fx-font-size:14px; -fx-text-fill:" + RED + ";");
        logoutIcon.setMinWidth(28);
        logoutIcon.setAlignment(Pos.CENTER);

        logoutTextUsuario = label("Cerrar sesión", 13, RED, false);
        logoutTextUsuario.setVisible(false);
        logoutTextUsuario.setManaged(false);
        logout.getChildren().addAll(logoutIcon, logoutTextUsuario);

        sidebarVBoxU.getChildren().addAll(logoBox, userCard, nav, spacer, logout);

        // ── Hover expand/collapse ─────────────────────────────────
        sidebarVBoxU.setOnMouseEntered(e -> setSidebarUsuarioExpanded(true));
        sidebarVBoxU.setOnMouseExited(e -> setSidebarUsuarioExpanded(false));

        ScrollPane scroll = new ScrollPane(sidebarVBoxU);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;"
                + "-fx-border-color: transparent; -fx-padding: 0;");
        return scroll;
    }

    private void setSidebarUsuarioExpanded(boolean expand) {
        double target = expand ? SIDEBAR_EXPANDED_U : SIDEBAR_COLLAPSED_U;
        javafx.animation.Timeline tl = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(180),
                        new javafx.animation.KeyValue(sidebarVBoxU.prefWidthProperty(), target,
                                javafx.animation.Interpolator.EASE_BOTH),
                        new javafx.animation.KeyValue(sidebarVBoxU.minWidthProperty(), target,
                                javafx.animation.Interpolator.EASE_BOTH),
                        new javafx.animation.KeyValue(sidebarVBoxU.maxWidthProperty(), target,
                                javafx.animation.Interpolator.EASE_BOTH)));
        tl.play();

        // Logo y logout
        if (logoTextUsuario != null) {
            logoTextUsuario.setVisible(expand);
            logoTextUsuario.setManaged(expand);
        }
        if (logoutTextUsuario != null) {
            logoutTextUsuario.setVisible(expand);
            logoutTextUsuario.setManaged(expand);
        }

        // Nav items
        nav.getChildren().forEach(node -> {
            if (node instanceof HBox hbox) {
                hbox.getChildren().forEach(child -> {
                    if (child instanceof Label lbl
                            && !lbl.getStyle().contains("Font Awesome")) {
                        lbl.setVisible(expand);
                        lbl.setManaged(expand);
                    }
                });
            }
        });

        // User card info
        sidebarVBoxU.getChildren().forEach(node -> {
            if (node instanceof HBox hbox) {
                hbox.getChildren().forEach(child -> {
                    if (child instanceof VBox vb) {
                        vb.getChildren().forEach(c -> {
                            if (c instanceof Label lbl
                                    && !lbl.getStyle().contains("Font Awesome")) {
                                lbl.setVisible(expand);
                                lbl.setManaged(expand);
                            }
                            if (c instanceof HBox row) {
                                row.getChildren().forEach(rc -> {
                                    if (rc instanceof Label rl
                                            && !rl.getStyle().contains("Font Awesome")) {
                                        rl.setVisible(expand);
                                        rl.setManaged(expand);
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    }

    private HBox navItem(String icon, String text) {
        HBox item = new HBox(12);
        item.setPadding(new Insets(10, 12, 10, 12));
        item.setAlignment(Pos.CENTER_LEFT);
        item.setCursor(javafx.scene.Cursor.HAND);
        item.setMaxWidth(Double.MAX_VALUE);

        String normalStyle = "-fx-background-radius:8;-fx-background-color:transparent;"
                + "-fx-focus-color:transparent;-fx-faint-focus-color:transparent;";
        String hoverStyle = "-fx-background-color:#ffffff18;-fx-background-radius:8;"
                + "-fx-focus-color:transparent;-fx-faint-focus-color:transparent;";
        String activeStyle = "-fx-background-color:rgba(255,255,255,0.20);-fx-background-radius:8;"
                + "-fx-focus-color:transparent;-fx-faint-focus-color:transparent;";

        item.setStyle(normalStyle);

        Label iconLabel = new Label(icon);
        iconLabel.setStyle(
                "-fx-font-family: 'Font Awesome 6 Free Solid';"
                + "-fx-font-size: 15px;"
                + "-fx-text-fill: #8899bb;");
        Label textLabel = label(text, 13, "#8899bb", false);
        iconLabel.setMinWidth(28);
        iconLabel.setAlignment(Pos.CENTER);
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
            // Resetear TODOS los ítems del nav
            nav.getChildren().forEach(node -> {
                if (node instanceof HBox hbox) {
                    hbox.setStyle(normalStyle);
                    hbox.getChildren().forEach(child -> {
                        if (child instanceof Label lbl) {
                            // Restaurar estilo según si es icono FA o texto
                            if (lbl.getStyle().contains("Font Awesome")) {
                                lbl.setStyle(
                                        "-fx-font-family: 'Font Awesome 6 Free Solid';"
                                        + "-fx-font-size: 15px;"
                                        + "-fx-text-fill: #8899bb;");
                            } else {
                                lbl.setTextFill(Color.web("#8899bb"));
                            }
                        }
                    });
                }
            });

            // Activar el ítem clickeado
            item.setStyle(activeStyle);
            iconLabel.setStyle(
                    "-fx-font-family: 'Font Awesome 6 Free Solid';"
                    + "-fx-font-size: 15px;"
                    + "-fx-text-fill: white;");
            textLabel.setTextFill(Color.WHITE);

            switch (text) {
                case "Dashboard" ->
                    root.setCenter(buildMainContent());
                case "Alertas" ->
                    root.setCenter(new AlertasView(usuarioActual, alertaService, barrioService).getView());
                case "Mis Alertas" ->
                    root.setCenter(new MisAlertasView(usuarioActual, alertaService, barrioService, root).getView());
                case "Mapa" ->
                    toggleMapaSubMenu(item);
                case "Vecinos" ->
                    root.setCenter(new VecinosView(usuarioActual, alertaService).getView());
                case "Mi Cuenta" ->
                    root.setCenter(new MiCuentaView(usuarioActual, suscripcionService, root,
                            () -> root.setCenter(buildMainContent())).getView());
                case "Notificaciones" ->
                    root.setCenter(new NotificacionesView(usuarioActual, notificacionService, root).getView());
                default ->
                    root.setCenter(buildPlaceholder(text));
            }
        });

        return item;
    }

    // FIX 3: toggleMapaSubMenu corregido para evitar duplicados al hacer clic varias veces
    private void toggleMapaSubMenu(HBox mapaItem) {
        int index = nav.getChildren().indexOf(mapaItem);
        if (!mapaExpandido) {
            mapaSubMenu = buildMapaSubMenu();
            nav.getChildren().add(index + 1, mapaSubMenu);
            mapaExpandido = true;
        } else {
            if (mapaSubMenu != null && nav.getChildren().contains(mapaSubMenu)) {
                nav.getChildren().remove(mapaSubMenu);
            }
            mapaSubMenu = null;
            mapaExpandido = false;
        }
    }

    private VBox buildMapaSubMenu() {
        VBox sub = new VBox(5);
        sub.setPadding(new Insets(0, 0, 0, 25));
        sub.getChildren().addAll(
                subItem("\uf3c5", "Mapa de alertas"),
                subItem("\uf071", "Zonas peligrosas"),
                subItem("\uf500", "Alertas comunitarias"));
        return sub;
    }

    private HBox subItem(String icon, String text) {
        HBox item = new HBox(8);
        item.setPadding(new Insets(8, 10, 8, 10));
        item.setAlignment(Pos.CENTER_LEFT);
        item.setCursor(javafx.scene.Cursor.HAND);

        Label iconLbl = new Label(icon);
        iconLbl.setStyle(
                "-fx-font-family: 'Font Awesome 6 Free Solid';"
                + "-fx-font-size: 13px;"
                + "-fx-text-fill: #8899bb;");

        Label textLbl = new Label(text);
        textLbl.setTextFill(Color.web("#cbd5e1"));
        textLbl.setFont(Font.font(12));

        item.getChildren().addAll(iconLbl, textLbl);
        item.setOnMouseEntered(e -> item.setStyle(
                "-fx-background-color:#ffffff15;-fx-background-radius:6;"
                + "-fx-focus-color:transparent;-fx-faint-focus-color:transparent;"));
        item.setOnMouseExited(e -> item.setStyle(
                "-fx-background-color:transparent;"
                + "-fx-focus-color:transparent;-fx-faint-focus-color:transparent;"));

        item.setOnMouseClicked(e -> {
            switch (text) {
                case "Mapa de alertas" ->
                    root.setCenter(new MapaAlertas(alertaService).build());
                case "Zonas peligrosas" ->
                    root.setCenter(new MapaZonasPeligrosas(alertaService).build());
                case "Alertas comunitarias" ->
                    root.setCenter(new MapaAlarmasRegistradas().build());
                default ->
                    root.setCenter(buildPlaceholder(text));
            }
        });
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
                buildFooter());
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setPannable(true);
        scroll.setFocusTraversable(false);
        scroll.setStyle("-fx-background-color: " + BG + "; -fx-background: " + BG + ";");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        return scroll;
    }

    // ── Top bar ───────────────────────────────────────────────────
    // ── Top bar ───────────────────────────────────────────────────
    private HBox buildTopBar() {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);

        VBox greeting = new VBox(4);
        String saludo = usuarioActual != null ? "¡Hola, " + usuarioActual.getPrimer_nombre() + "!" : "¡Hola!";
        Label hello = new Label(saludo);
        hello.setFont(Font.font("System", FontWeight.BOLD, 28));
        hello.setTextFill(Color.web("#111827"));
        greeting.getChildren().addAll(hello,
                label("Tu seguridad es importante. Estamos aquí para ayudarte.", 13, GRAY_TEXT, false));

        HBox rightBox = new HBox(16);
        rightBox.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(rightBox, Priority.ALWAYS);

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

        VBox dateBox = new VBox(2);
        dateBox.setAlignment(Pos.CENTER_RIGHT);
        dateBox.getChildren().addAll(dateLbl, timeLbl);

        // ── Campanita ─────────────────────────────────────────────────
        List<Notificacion> misNotifs = List.of();
        if (notificacionService != null && usuarioActual != null) {
            try {
                misNotifs = notificacionService.listar().stream()
                        .filter(n -> usuarioActual.getCorreo() != null
                        && usuarioActual.getCorreo().equalsIgnoreCase(n.getCorreodestinatario()))
                        .sorted((a, b) -> b.getFechahora() != null && a.getFechahora() != null
                        ? b.getFechahora().compareTo(a.getFechahora()) : 0)
                        .collect(java.util.stream.Collectors.toList());
            } catch (Exception ignored) {
            }
        }

        int totalBadge = misNotifs.size();

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
        Popup popup = new Popup();
        popup.setAutoHide(true);

        VBox popupBox = new VBox(0);
        popupBox.setPrefWidth(320);
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

        Label popTitle = new Label("Mis notificaciones");
        popTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        popTitle.setTextFill(Color.web("#111827"));
        HBox.setHgrow(popTitle, Priority.ALWAYS);

        Label popBadge = new Label(totalBadge + (totalBadge == 1 ? " nueva" : " nuevas"));
        popBadge.setStyle(
                "-fx-background-color:#1565c022;"
                + "-fx-text-fill:#1565c0;"
                + "-fx-font-size:11px;-fx-font-weight:bold;"
                + "-fx-background-radius:20;-fx-padding:3 8;");
        popHeader.getChildren().addAll(popTitle, popBadge);
        popupBox.getChildren().add(popHeader);

        // Lista de notificaciones
        final List<Notificacion> misNotifsF = misNotifs;
        if (misNotifsF.isEmpty()) {
            VBox vacio = new VBox(8);
            vacio.setAlignment(Pos.CENTER);
            vacio.setPadding(new Insets(28));
            Label icoVacio = new Label("🔕");
            icoVacio.setFont(Font.font(36));
            Label msgVacio = new Label("Sin notificaciones");
            msgVacio.setStyle("-fx-font-size:13px;-fx-text-fill:" + GRAY_TEXT + ";");
            vacio.getChildren().addAll(icoVacio, msgVacio);
            popupBox.getChildren().add(vacio);
        } else {
            misNotifsF.stream().limit(5).forEach(n -> {
                String estadoStr = n.getEstado() != null ? n.getEstado().name() : "";
                String iconColor = switch (estadoStr) {
                    case "LEIDA" ->
                        GREEN;
                    case "ENVIADA" ->
                        BLUE;
                    case "ERROR" ->
                        RED;
                    default ->
                        ORANGE;
                };

                HBox fila = new HBox(10);
                fila.setPadding(new Insets(11, 16, 11, 16));
                fila.setAlignment(Pos.CENTER_LEFT);
                fila.setStyle("-fx-border-color:transparent transparent #f3f4f6 transparent;"
                        + "-fx-border-width:0 0 1 0;");
                fila.setOnMouseEntered(e -> fila.setStyle(
                        "-fx-background-color:#f0f7ff;"
                        + "-fx-border-color:transparent transparent #f3f4f6 transparent;"
                        + "-fx-border-width:0 0 1 0;-fx-cursor:hand;"));
                fila.setOnMouseExited(e -> fila.setStyle(
                        "-fx-border-color:transparent transparent #f3f4f6 transparent;"
                        + "-fx-border-width:0 0 1 0;"));
                fila.setOnMouseClicked(e -> {
                    popup.hide();
                    root.setCenter(new NotificacionesView(usuarioActual, notificacionService, root).getView());
                });

                // Ícono circular
                StackPane iconWrap = new StackPane();
                iconWrap.setPrefSize(34, 34);
                iconWrap.setMinSize(34, 34);
                iconWrap.setMaxSize(34, 34);
                Region iconBg = new Region();
                iconBg.setPrefSize(34, 34);
                iconBg.setStyle("-fx-background-color:" + iconColor + "22;"
                        + "-fx-background-radius:50%;");
                Label iconLbl = new Label("\uf0f3");
                iconLbl.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';"
                        + "-fx-font-size:13px;-fx-text-fill:" + iconColor + ";");
                iconWrap.getChildren().addAll(iconBg, iconLbl);

                // Texto
                VBox texto = new VBox(3);
                HBox.setHgrow(texto, Priority.ALWAYS);

                String msg = n.getMensaje() != null
                        ? (n.getMensaje().length() > 42
                        ? n.getMensaje().substring(0, 42) + "…"
                        : n.getMensaje())
                        : "Sin mensaje";
                Label msgLbl = new Label(msg);
                msgLbl.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:#111827;");
                msgLbl.setWrapText(true);

                String fecha = n.getFechahora() != null
                        ? n.getFechahora().format(DateTimeFormatter.ofPattern("dd/MM HH:mm")) : "";
                Label subLbl = new Label(fecha);
                subLbl.setStyle("-fx-font-size:10px;-fx-text-fill:" + GRAY_TEXT + ";");

                Label stateBadge = new Label(estadoStr);
                stateBadge.setStyle(
                        "-fx-background-color:" + iconColor + "22;"
                        + "-fx-text-fill:" + iconColor + ";"
                        + "-fx-font-size:9px;-fx-font-weight:bold;"
                        + "-fx-background-radius:20;-fx-padding:2 6;");

                texto.getChildren().addAll(msgLbl, subLbl, stateBadge);
                fila.getChildren().addAll(iconWrap, texto);
                popupBox.getChildren().add(fila);
            });
        }

        // Footer popup
        HBox popFooter = new HBox();
        popFooter.setPadding(new Insets(10, 16, 12, 16));
        popFooter.setAlignment(Pos.CENTER);
        popFooter.setStyle(
                "-fx-background-color:#f8fafc;"
                + "-fx-background-radius:0 0 14 14;"
                + "-fx-border-color:#e5e7eb transparent transparent transparent;"
                + "-fx-border-width:1 0 0 0;-fx-cursor:hand;");
        Label verTodas = new Label("Ver todas las notificaciones  →");
        verTodas.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:" + BLUE + ";");
        popFooter.getChildren().add(verTodas);
        popFooter.setOnMouseEntered(e -> popFooter.setStyle(
                "-fx-background-color:#e8f0fe;"
                + "-fx-background-radius:0 0 14 14;"
                + "-fx-border-color:#e5e7eb transparent transparent transparent;"
                + "-fx-border-width:1 0 0 0;-fx-cursor:hand;"));
        popFooter.setOnMouseExited(e -> popFooter.setStyle(
                "-fx-background-color:#f8fafc;"
                + "-fx-background-radius:0 0 14 14;"
                + "-fx-border-color:#e5e7eb transparent transparent transparent;"
                + "-fx-border-width:1 0 0 0;-fx-cursor:hand;"));
        popFooter.setOnMouseClicked(e -> {
            popup.hide();
            root.setCenter(new NotificacionesView(usuarioActual, notificacionService, root).getView());
        });

        popupBox.getChildren().add(popFooter);
        popup.getContent().add(popupBox);

        // Abrir / cerrar popup
        bell.setOnMouseClicked(e -> {
            if (popup.isShowing()) {
                popup.hide();
            } else {
                javafx.geometry.Bounds b = bell.localToScreen(bell.getBoundsInLocal());
                popup.show(bell, b.getMaxX() - 320, b.getMaxY() + 8);
            }
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
        bellLbl.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';-fx-font-size:22px;-fx-text-fill:white;");
        iconBox.getChildren().addAll(iconBg, bellLbl);

        // FIX 4: uso correcto de ScaleTransition (importado al inicio)
        ScaleTransition pulse = new ScaleTransition(Duration.seconds(1.1), iconBox);
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
        String base = "-fx-background-color:" + RED + ";-fx-text-fill:white;-fx-font-size:15px;"
                + "-fx-font-weight:bold;-fx-background-radius:30;-fx-padding:13 32;-fx-cursor:hand;"
                + "-fx-effect:dropshadow(gaussian,rgba(229,57,53,0.42),18,0,0,5);";
        String hover = "-fx-background-color:#c62828;-fx-text-fill:white;-fx-font-size:15px;"
                + "-fx-font-weight:bold;-fx-background-radius:30;-fx-padding:13 32;-fx-cursor:hand;"
                + "-fx-effect:dropshadow(gaussian,rgba(198,40,40,0.58),22,0,0,7);";
        panicBtn.setStyle(base);
        panicBtn.setOnMouseEntered(e -> panicBtn.setStyle(hover));
        panicBtn.setOnMouseExited(e -> panicBtn.setStyle(base));
        panicBtn.setOnAction(e -> {
            Stage stage = (Stage) panicBtn.getScene().getWindow();
            new MapaAlerta(stage, usuarioActual, alertaService, barrioService).mostrar();
        });

        banner.getChildren().addAll(iconBox, textBox, panicBtn);
        return banner;
    }

    // ── Stat cards ────────────────────────────────────────────────
    private HBox buildStatCards() {
        HBox row = new HBox(16);

        // Tarjetas con valores en 0 inicialmente
        VBox cardIncidentes = statCard("#fff0f0", "#e53935", "Incidentes este mes", 0, "+3 vs mes anterior");
        VBox cardPendientes = statCard("#fff8e1", "#fb8c00", "Alertas pendientes", 0, "PENDIENTE / EN PROCESO");
        VBox cardVecinos = statCard("#e8f5e9", "#43a047", "Vecinos del barrio", 0, "Reportaron en tu barrio");

        row.getChildren().addAll(cardIncidentes, cardPendientes, cardVecinos);

        // Carga en hilo separado para no bloquear la UI
        new Thread(() -> {
            long incidentesMes = 0, alertasPendientes = 0, vecinosActivos = 0;
            try {
                if (alertaService != null && usuarioActual != null) {
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

                    if (usuarioActual.getDireccion() != null
                            && usuarioActual.getDireccion().getBarrio() != null) {
                        String miBarrio = usuarioActual.getDireccion().getBarrio().getNombre();
                        vecinosActivos = alertaService.listar().stream()
                                .filter(a -> a.getBarrio() != null
                                && miBarrio.equalsIgnoreCase(a.getBarrio().getNombre()))
                                .map(a -> a.getUsuario() != null ? a.getUsuario().getUsername() : "")
                                .distinct().count();
                    }
                }
            } catch (Exception ignored) {
            }

            final long fi = incidentesMes, fp = alertasPendientes, fv = vecinosActivos;
            javafx.application.Platform.runLater(() -> {
                actualizarValorCard(cardIncidentes, fi, "#e53935");
                actualizarValorCard(cardPendientes, fp, "#fb8c00");
                actualizarValorCard(cardVecinos, fv, "#43a047");
            });
        }, "stats-loader").start();

        return row;
    }

// Helper para actualizar solo el label del valor sin reconstruir la tarjeta
    private void actualizarValorCard(VBox card, long valor, String color) {
        try {
            HBox topRow = (HBox) card.getChildren().get(0);
            VBox infoBox = (VBox) topRow.getChildren().get(1);
            Label valLbl = (Label) infoBox.getChildren().get(1);
            valLbl.setText(String.valueOf(valor));
        } catch (Exception ignored) {
        }
    }

    // Reemplaza el método statCard completo
    private VBox statCard(String bgIcon, String accentColor, String title, long value, String sub) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20, 22, 20, 22));
        card.setStyle("-fx-background-color:white;-fx-background-radius:18;");
        HBox.setHgrow(card, Priority.ALWAYS);
        shadow(card);

        // Icono FA según color de acento
        String faIcon = accentColor.equals("#e53935") ? "\uf071" // triangle-exclamation
                : accentColor.equals("#fb8c00") ? "\uf0f3" // bell
                : "\uf500";                                       // people-group

        StackPane iconWrap = new StackPane();
        iconWrap.setPrefSize(58, 58);
        iconWrap.setMinSize(58, 58);
        iconWrap.setMaxSize(58, 58);

        Region colorBg = new Region();
        colorBg.setPrefSize(58, 58);
        colorBg.setStyle("-fx-background-color:" + bgIcon + ";-fx-background-radius:16;");

        Label iconLbl = new Label(faIcon);
        iconLbl.setStyle(
                "-fx-font-family: 'Font Awesome 6 Free Solid';"
                + "-fx-font-size: 26px;"
                + "-fx-text-fill: " + accentColor + ";");

        iconWrap.getChildren().addAll(colorBg, iconLbl);

        Label valLbl = new Label(String.valueOf(value));
        valLbl.setStyle("-fx-font-size:36px;-fx-font-weight:bold;-fx-text-fill:" + accentColor + ";");
        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#374151;");
        Label subLbl = new Label(sub);
        subLbl.setStyle("-fx-font-size:11px;-fx-text-fill:" + GRAY_TEXT + ";");

        HBox topRow = new HBox(16, iconWrap, new VBox(3, titleLbl, valLbl, subLbl));
        topRow.setAlignment(Pos.CENTER_LEFT);
        card.getChildren().add(topRow);
        card.setOnMouseEntered(e -> card.setTranslateY(-3));
        card.setOnMouseExited(e -> card.setTranslateY(0));
        return card;
    }

    // ── Bottom row ────────────────────────────────────────────────
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

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        HBox titleRow = new HBox(8);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(32, 32);
        iconBox.setMinSize(32, 32);
        iconBox.setMaxSize(32, 32);
        iconBox.setStyle("-fx-background-color:#e8f0fe;-fx-background-radius:8;");
        Label iconLbl = new Label("\uf0f3");
        iconLbl.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';-fx-font-size:16px;-fx-text-fill:#1565c0;");
        iconBox.getChildren().add(iconLbl);

        titleRow.getChildren().addAll(iconBox, label("Alertas recientes", 15, "#111827", true));
        HBox.setHgrow(titleRow, Priority.ALWAYS);

        Label verTodas = label("Ver todas  >", 12, BLUE, false);
        verTodas.setCursor(javafx.scene.Cursor.HAND);
        verTodas.setOnMouseClicked(e -> root.setCenter(
                new AlertasView(usuarioActual, alertaService, barrioService).getView()));

        header.getChildren().addAll(titleRow, verTodas);
        card.getChildren().addAll(header, separator());

        // Spinner mientras carga
        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setPrefSize(32, 32);
        spinner.setStyle("-fx-progress-color:#1565c0;");
        HBox spinnerBox = new HBox(spinner);
        spinnerBox.setAlignment(Pos.CENTER);
        spinnerBox.setPadding(new Insets(16));
        card.getChildren().add(spinnerBox);

        new Thread(() -> {
            List<Alerta> alertas = new ArrayList<>();
            String errorMsg = null;
            try {
                if (alertaService != null) {
                    alertas = alertaService.listar();
                }
            } catch (Exception e) {
                errorMsg = "Error al cargar alertas";
            }

            final List<Alerta> resultado = alertas;
            final String error = errorMsg;
            javafx.application.Platform.runLater(() -> {
                card.getChildren().remove(spinnerBox);
                if (error != null) {
                    card.getChildren().add(label(error, 13, RED, false));
                } else if (resultado.isEmpty()) {
                    card.getChildren().add(label("No hay alertas registradas", 13, GRAY_TEXT, false));
                } else {
                    resultado.stream().limit(4).forEach(a
                            -> card.getChildren().addAll(alertaItem(a), separator()));
                }
            });
        }, "alertas-loader").start();

        return card;
    }

    private HBox alertaItem(Alerta a) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 4, 10, 4));
        row.setCursor(javafx.scene.Cursor.HAND);
        row.setStyle("-fx-background-color:transparent;-fx-background-radius:8;");
        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color:#f8f9fb;-fx-background-radius:8;"));
        row.setOnMouseExited(e -> row.setStyle("-fx-background-color:transparent;-fx-background-radius:8;"));

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

        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(40, 40);
        iconBox.setMinSize(40, 40);
        iconBox.setMaxSize(40, 40);

        String tipo = a.getTipoalerta() != null ? a.getTipoalerta().getNombre().toUpperCase() : "";
        String circleBg = switch (tipo) {
            case "ROBO", "HURTO", "SOSPECHOSO", "PERSONA" ->
                "#fce4ec";
            case "ANIMAL" ->
                "#fff8e1";
            case "ACCIDENTE" ->
                "#fff3e0";
            case "VANDALISMO" ->
                "#e8eaf6";
            default ->
                "#e3f2fd";
        };
        String faIcon = switch (tipo) {
            case "ROBO", "HURTO" ->
                "\uf505";
            case "ACCIDENTE" ->
                "\uf071";
            case "INCENDIO" ->
                "\uf46a";
            case "ANIMAL" ->
                "\uf6d3";
            case "VANDALISMO" ->
                "\uf6e3";
            case "SOSPECHOSO", "PERSONA" ->
                "\uf007";
            default ->
                "\uf0f3";
        };

        Region circleBgRegion = new Region();
        circleBgRegion.setPrefSize(40, 40);
        circleBgRegion.setStyle("-fx-background-color:" + circleBg + ";-fx-background-radius:50%;");
        Label faLbl = new Label(faIcon);
        faLbl.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';-fx-font-size:16px;-fx-text-fill:" + dotColor + ";");
        iconBox.getChildren().addAll(circleBgRegion, faLbl);

        VBox text = new VBox(3);
        HBox.setHgrow(text, Priority.ALWAYS);

        String tipoNombre = a.getTipoalerta() != null ? a.getTipoalerta().getNombre() : "Alerta";
        String barrio = a.getBarrio() != null ? " en " + a.getBarrio().getNombre() : "";

        String tiempo = "Hace un momento";
        if (a.getFechaHora() != null) {
            long mins = java.time.Duration.between(a.getFechaHora(), LocalDateTime.now()).toMinutes();
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

        String reportado = "";
        if (a.getUsuario() != null) {
            String nombre = a.getUsuario().getPrimer_nombre() != null ? a.getUsuario().getPrimer_nombre() : "";
            String apellido = a.getUsuario().getPrimer_apellido() != null
                    ? " " + a.getUsuario().getPrimer_apellido().charAt(0) + "." : "";
            if (!nombre.isBlank() || !apellido.isBlank()) {
                reportado = " · Reportado por " + nombre + apellido;
            }
        }

        String titleColor = switch (a.getEstado() != null ? a.getEstado().name() : "") {
            case "PENDIENTE" ->
                RED;
            case "EN_ATENCION" ->
                ORANGE;
            case "RESUELTA" ->
                GREEN;
            case "RECIBIDA" ->
                BLUE;
            default ->
                "#1e293b";
        };
        Label titleLbl = new Label(desc != null && !desc.isBlank() ? desc : tipoNombre + barrio);
        titleLbl.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:" + titleColor + ";");
        titleLbl.setWrapText(true);
        Label subLbl = new Label(tiempo + reportado);
        subLbl.setStyle("-fx-font-size:11px;-fx-text-fill:" + GRAY_TEXT + ";");
        text.getChildren().addAll(titleLbl, subLbl);

        Label chevron = new Label("›");
        chevron.setStyle("-fx-font-size:18px;-fx-text-fill:#cbd5e1;-fx-font-weight:bold;");

        row.getChildren().addAll(dot, iconBox, text, chevron);
        return row;
    }

    private VBox buildMapPanel() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.setPrefWidth(380);
        card.setStyle("-fx-background-color: " + WHITE + "; -fx-background-radius: 12;");
        shadow(card);

        // ── Header ────────────────────────────────────────────────────
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        StackPane pinBox = new StackPane();
        pinBox.setPrefSize(32, 32);
        pinBox.setMinSize(32, 32);
        pinBox.setMaxSize(32, 32);
        pinBox.setStyle("-fx-background-color:#e8f0fe;-fx-background-radius:50%;");
        Label pinLbl = new Label("\uf3c5");
        pinLbl.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';-fx-font-size:15px;-fx-text-fill:#1565c0;");
        pinBox.getChildren().add(pinLbl);

        String barrioDisplay = usuarioActual != null
                && usuarioActual.getDireccion() != null
                && usuarioActual.getDireccion().getBarrio() != null
                ? usuarioActual.getDireccion().getBarrio().getNombre() : "Tu barrio";

        HBox.setHgrow(new Region(), Priority.ALWAYS);
        Label abrirLbl = label("Expandir  ↗", 11, BLUE, false);
        abrirLbl.setCursor(javafx.scene.Cursor.HAND);
        abrirLbl.setOnMouseClicked(e -> root.setCenter(new MapaAlertas(alertaService).build()));

        Region spacerH = new Region();
        HBox.setHgrow(spacerH, Priority.ALWAYS);
        header.getChildren().addAll(pinBox, label("Alertas en " + barrioDisplay, 14, "#111827", true), spacerH, abrirLbl);

        // ── Contenedor del mapa embebido ──────────────────────────────
        javafx.scene.layout.StackPane mapaContainer = new javafx.scene.layout.StackPane();
        mapaContainer.setPrefHeight(210);
        mapaContainer.setMinHeight(210);
        mapaContainer.setMaxHeight(210);
        mapaContainer.setStyle("-fx-background-radius:10;-fx-background-color:#e8f0fe;");
        VBox.setVgrow(mapaContainer, Priority.NEVER);

        // Spinner mientras inicializa el mapa Swing
        ProgressIndicator mapaSpinner = new ProgressIndicator();
        mapaSpinner.setPrefSize(36, 36);
        mapaSpinner.setStyle("-fx-progress-color:#1565c0;");
        mapaContainer.getChildren().add(mapaSpinner);

        // Inicializar SwingNode en hilo Swing, luego agregarlo al FX thread
        javax.swing.SwingUtilities.invokeLater(() -> {
            org.jxmapviewer.JXMapViewer miniMapa = new org.jxmapviewer.JXMapViewer();

            org.jxmapviewer.viewer.TileFactoryInfo info = new org.jxmapviewer.viewer.TileFactoryInfo(
                    1, 15, 17, 256, true, true,
                    "https://tile.openstreetmap.org", "x", "y", "z") {
                @Override
                public String getTileUrl(int x, int y, int zoom) {
                    return baseURL + "/" + (17 - zoom) + "/" + x + "/" + y + ".png";
                }
            };
            miniMapa.setTileFactory(new org.jxmapviewer.viewer.DefaultTileFactory(info));
            miniMapa.setAddressLocation(new org.jxmapviewer.viewer.GeoPosition(10.4795, -73.2536));
            miniMapa.setZoom(5);

            // Pan con mouse
            org.jxmapviewer.input.PanMouseInputListener pan
                    = new org.jxmapviewer.input.PanMouseInputListener(miniMapa);
            miniMapa.addMouseListener(pan);
            miniMapa.addMouseMotionListener(pan);
            miniMapa.addMouseWheelListener(
                    new org.jxmapviewer.input.ZoomMouseWheelListenerCenter(miniMapa));

            // Pintar alertas del barrio del usuario
            miniMapa.setOverlayPainter((g, map, w, h) -> {
                g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                try {
                    if (alertaService == null) {
                        return;
                    }
                    String miBarrio = (usuarioActual != null
                            && usuarioActual.getDireccion() != null
                            && usuarioActual.getDireccion().getBarrio() != null)
                            ? usuarioActual.getDireccion().getBarrio().getNombre() : null;

                    List<Alerta> alertas = alertaService.listar().stream()
                            .filter(a -> miBarrio == null || (a.getBarrio() != null
                            && miBarrio.equalsIgnoreCase(a.getBarrio().getNombre())))
                            .filter(a -> a.getLatitud() != 0 || a.getLongitud() != 0)
                            .collect(java.util.stream.Collectors.toList());

                    for (Alerta a : alertas) {
                        java.awt.geom.Point2D pt = map.getTileFactory().geoToPixel(
                                new org.jxmapviewer.viewer.GeoPosition(
                                        a.getLatitud(), a.getLongitud()), map.getZoom());
                        int cx = (int) pt.getX() - map.getViewportBounds().x;
                        int cy = (int) pt.getY() - map.getViewportBounds().y;

                        String estado = a.getEstado() != null ? a.getEstado().name() : "";
                        java.awt.Color col = switch (estado) {
                            case "PENDIENTE" ->
                                new java.awt.Color(229, 57, 53);
                            case "EN_ATENCION" ->
                                new java.awt.Color(251, 140, 0);
                            case "RESUELTA" ->
                                new java.awt.Color(67, 160, 71);
                            default ->
                                new java.awt.Color(107, 114, 128);
                        };

                        // Pin pequeño
                        int r = 7;
                        g.setColor(col);
                        g.fillOval(cx - r, cy - r, r * 2, r * 2);
                        g.setColor(java.awt.Color.WHITE);
                        g.setStroke(new java.awt.BasicStroke(1.5f));
                        g.drawOval(cx - r, cy - r, r * 2, r * 2);
                    }
                } catch (Exception ignored) {
                }
            });

            javafx.embed.swing.SwingNode swingNode = new javafx.embed.swing.SwingNode();
            swingNode.setContent(miniMapa);

            // Doble clic → abrir mapa completo
            swingNode.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2) {
                    root.setCenter(new MapaAlertas(alertaService).build());
                }
            });

            javafx.application.Platform.runLater(() -> {
                mapaContainer.getChildren().setAll(swingNode);
            });
        });

        // ── Leyenda ───────────────────────────────────────────────────
        HBox legend = new HBox(16);
        legend.setAlignment(Pos.CENTER);
        legend.getChildren().addAll(
                legendItem(RED, "Pendiente"),
                legendItem(ORANGE, "En revisión"),
                legendItem(GREEN, "Resuelto"));

        Label hint = label("Doble clic para abrir mapa completo", 10, GRAY_TEXT, false);
        hint.setAlignment(Pos.CENTER);

        card.getChildren().addAll(header, mapaContainer, legend, hint);
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

        StackPane lockBox = new StackPane();
        lockBox.setPrefSize(60, 60);
        lockBox.setMinSize(60, 60);
        lockBox.setMaxSize(60, 60);
        lockBox.setStyle("-fx-background-color:#dbeafe;-fx-background-radius:50%;");
        Label lockLbl = new Label("\uf023");
        lockLbl.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';-fx-font-size:25px;-fx-text-fill:#1565c0;");
        lockBox.getChildren().add(lockLbl);

        HBox left = new HBox(10);
        left.setAlignment(Pos.CENTER_LEFT);
        VBox leftText = new VBox(2);
        leftText.getChildren().addAll(
                label("Tu información está protegida", 16, BLUE, true),
                label("Todas tus alertas son anónimas y confidenciales.", 13, GRAY_TEXT, false));
        left.getChildren().addAll(lockBox, leftText);
        HBox.setHgrow(left, Priority.ALWAYS);

        StackPane shieldBox = new StackPane();
        shieldBox.setPrefSize(50, 50);
        shieldBox.setMinSize(50, 50);
        shieldBox.setMaxSize(50, 50);
        shieldBox.setStyle("-fx-background-color:#dbeafe;-fx-background-radius:50%;");
        Label shieldLbl = new Label("\uf132");
        shieldLbl.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';-fx-font-size:25px;-fx-text-fill:#1565c0;");
        shieldBox.getChildren().add(shieldLbl);

        HBox right = new HBox(10);
        right.setAlignment(Pos.CENTER_RIGHT);
        VBox rightText = new VBox(2);
        rightText.setAlignment(Pos.CENTER_RIGHT);
        rightText.getChildren().addAll(
                label("100% Anónimo", 16, BLUE, true),
                label("Tu identidad no será revelada.", 13, GRAY_TEXT, false));
        right.getChildren().addAll(rightText, shieldBox);

        footer.getChildren().addAll(left, right);
        return footer;
    }

    // =========================================================================
    // PLACEHOLDER — FIX 7: método faltante implementado
    // =========================================================================
    private VBox buildPlaceholder(String texto) {
        VBox box = new VBox(16);
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: " + BG + ";");
        box.setPadding(new Insets(60));

        Label icon = new Label("🚧");
        icon.setFont(Font.font(64));

        Label title = new Label(texto);
        title.setFont(Font.font("System", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#111827"));

        Label sub = new Label("Esta sección estará disponible próximamente.");
        sub.setFont(Font.font(14));
        sub.setTextFill(Color.web(GRAY_TEXT));

        box.getChildren().addAll(icon, title, sub);
        return box;
    }

    // =========================================================================
    // CERRAR SESIÓN
    // =========================================================================
    private void cerrarSesion() {
        Stage stage = (Stage) root.getScene().getWindow();
        stage.close();
    }

    // =========================================================================
    // HELPERS UI
    // =========================================================================
    private HBox legendItem(String color, String text) {
        HBox item = new HBox(6);
        item.setAlignment(Pos.CENTER_LEFT);
        item.getChildren().addAll(new Circle(5, Color.web(color)), label(text, 11, GRAY_TEXT, false));
        return item;
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
}
