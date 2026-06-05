/* Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.view;

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
import javafx.stage.Screen;

import sistemagestion.model.*;
import sistemagestion.service.*;

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
    private static final double SIDEBAR_EXPANDED = 240;
    private static final double SIDEBAR_COLLAPSED = 60;
    private static final String FA = "'Font Awesome 6 Free Solid'";

    private VBox sidebarBox;
    private VBox logoTextAdmin;
    private Label logoutTextAdmin;

    // ── Servicios ─────────────────────────────────────────────────
    private AlertaService alertaService;
    private PoliciaService policiaService;
    private UnidadPolicialService unidadService;
    private AsignacionUnidadService asignacionService;
    private AlarmaService alarmaService;
    private NotificacionService notificacionService;
    private AtencionAlertaService atencionService;

    // ── Usuario logueado ──────────────────────────────────────────
    private final Usuario usuarioActual;

    // ── UI ────────────────────────────────────────────────────────
    private BorderPane root;
    private VBox nav;

    private boolean mapaExpandido = false;
    private VBox mapaSubMenu = null;

    // ── Panel de notificaciones flotante ──────────────────────────
    private boolean panelNotiVisible = false;
    private VBox panelNoti = null;
    private StackPane bellStack = null;

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
        root.setStyle("-fx-background-color:" + BG + ";");

        Screen screen = Screen.getPrimary();
        double w = screen.getVisualBounds().getWidth();
        double h = screen.getVisualBounds().getHeight();

        Scene scene = new Scene(root, w * 0.85, h * 0.85);
        stage.setTitle("WolertApp – Administrador Policía");
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
        sidebarBox = new VBox();
        sidebarBox.setPrefWidth(SIDEBAR_COLLAPSED);
        sidebarBox.setMinWidth(SIDEBAR_COLLAPSED);
        sidebarBox.setMaxWidth(SIDEBAR_COLLAPSED);
        sidebarBox.setMaxHeight(Double.MAX_VALUE);
        sidebarBox.setFillWidth(true);
        VBox.setVgrow(sidebarBox, Priority.ALWAYS);
        sidebarBox.setStyle("-fx-background-color: linear-gradient(to right, #16283d, #1f3a56);");

        // ── Logo ──────────────────────────────────────────────────
        HBox logoBox = new HBox(10);
        logoBox.setPadding(new Insets(20, 8, 20, 8));
        logoBox.setAlignment(Pos.CENTER_LEFT);
        javafx.scene.shape.Rectangle logoClip = new javafx.scene.shape.Rectangle(SIDEBAR_EXPANDED, 80);
        logoBox.setClip(logoClip);

        ImageView logoImg = new ImageView(
                new Image(getClass().getResourceAsStream("/LogoWolertAPP.png")));
        logoImg.setFitWidth(44);
        logoImg.setFitHeight(44);
        logoImg.setPreserveRatio(true);

        logoTextAdmin = new VBox(2);
        logoTextAdmin.getChildren().addAll(
                label("WolertApp", 15, WHITE, true),
                label("Administrador Policía", 9, "#8899bb", false));
        logoTextAdmin.setVisible(false);
        logoTextAdmin.setManaged(false);
        logoBox.getChildren().addAll(new StackPane(logoImg), logoTextAdmin);

        // ── Perfil ────────────────────────────────────────────────
        HBox profileCard = buildProfileCard();

        // ── Nav ───────────────────────────────────────────────────
        nav = new VBox(2);
        nav.setPadding(new Insets(12, 4, 12, 4));
        HBox mapaItem = buildMapaNavItem();
        nav.getChildren().addAll(
                navItem("\uf015", "Centro de operaciones"),
                navItem("\uf0f3", "Alertas"),
                navItem("\uf0a1", "Alarmas"),
                navItem("\uf14a", "Asignaciones"),
                navItem("\uf505", "Policías"),
                navItem("\uf1b9", "Unidades"),
                navItem("\uf1da", "Historial"),
                mapaItem,
                navItem("\uf080", "Estadísticas"),
                navItem("\uf201", "Reportes"),
                navItem("\uf1f6", "Notificaciones"),
                navItem("\uf013", "Configuración")
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
        logoutIcon.setStyle("-fx-font-family:" + FA + ";"
                + "-fx-font-size:14px; -fx-text-fill:" + RED + ";");
        logoutIcon.setMinWidth(28);
        logoutIcon.setAlignment(Pos.CENTER);

        logoutTextAdmin = label("Cerrar sesión", 13, WHITE, true);
        logoutTextAdmin.setVisible(false);
        logoutTextAdmin.setManaged(false);
        logout.getChildren().addAll(logoutIcon, logoutTextAdmin);

        sidebarBox.getChildren().addAll(logoBox, profileCard, nav, spacer, logout);

        // ── Hover expand/collapse ─────────────────────────────────
        sidebarBox.setOnMouseEntered(e -> setSidebarAdminPolExpanded(true));
        sidebarBox.setOnMouseExited(e -> setSidebarAdminPolExpanded(false));

        ScrollPane scroll = new ScrollPane(sidebarBox);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.setPannable(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;"
                + "-fx-border-color: transparent; -fx-padding: 0;");
        return scroll;
    }

    private void setSidebarAdminPolExpanded(boolean expand) {
        double target = expand ? SIDEBAR_EXPANDED : SIDEBAR_COLLAPSED;
        javafx.animation.Timeline tl = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(180),
                        new javafx.animation.KeyValue(sidebarBox.prefWidthProperty(), target,
                                javafx.animation.Interpolator.EASE_BOTH),
                        new javafx.animation.KeyValue(sidebarBox.minWidthProperty(), target,
                                javafx.animation.Interpolator.EASE_BOTH),
                        new javafx.animation.KeyValue(sidebarBox.maxWidthProperty(), target,
                                javafx.animation.Interpolator.EASE_BOTH)));
        tl.play();

        if (logoTextAdmin != null) {
            logoTextAdmin.setVisible(expand);
            logoTextAdmin.setManaged(expand);
        }
        if (logoutTextAdmin != null) {
            logoutTextAdmin.setVisible(expand);
            logoutTextAdmin.setManaged(expand);
        }
        nav.getChildren().forEach(node -> {
            if (node instanceof HBox hbox) {
                hbox.getChildren().forEach(child -> {
                    if (child instanceof Label lbl && !lbl.getStyle().contains("Font Awesome")) {
                        lbl.setVisible(expand);
                        lbl.setManaged(expand);
                    }
                });
            }
        });
        sidebarBox.getChildren().forEach(node -> {
            if (node instanceof HBox hbox) {
                hbox.getChildren().forEach(child -> {
                    if (child instanceof VBox vb) {
                        vb.getChildren().forEach(c -> {
                            if (c instanceof Label lbl && !lbl.getStyle().contains("Font Awesome")) {
                                lbl.setVisible(expand);
                                lbl.setManaged(expand);
                            }
                            if (c instanceof HBox row) {
                                row.getChildren().forEach(rc -> {
                                    if (rc instanceof Label rl && !rl.getStyle().contains("Font Awesome")) {
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

    private HBox buildProfileCard() {
        HBox card = new HBox(10);
        card.setPadding(new Insets(10, 16, 10, 16));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 12;");

        Circle av = new Circle(20, Color.web("#1f3a56"));
        Label icon = new Label("\uf505");
        icon.setStyle("-fx-font-family:" + FA + ";-fx-font-size:15px;-fx-text-fill:#a8c0dd;");
        StackPane avBox = new StackPane(av, icon);
        avBox.setMinWidth(36);
        avBox.setMaxWidth(36);

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

    // ── Nav item genérico ─────────────────────────────────────────
    /**
     * FIX: íconos alineados usando StackPane de ancho fijo (28px) para el ícono
     * FA, así todos los textos empiezan en la misma posición horizontal.
     */
    private HBox navItem(String icon, String text) {
        HBox item = new HBox(10);
        item.setPadding(new Insets(9, 12, 9, 12));
        item.setAlignment(Pos.CENTER_LEFT);
        item.setCursor(javafx.scene.Cursor.HAND);
        item.setMaxWidth(Double.MAX_VALUE);
        item.setStyle("-fx-background-radius: 8;");

        // Ícono FA en StackPane de ancho fijo — alineación perfecta
        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-family:" + FA + ";-fx-font-size:14px;-fx-text-fill:#8899bb;");
        StackPane iconBox = new StackPane(iconLbl);
        iconBox.setPrefWidth(24);
        iconBox.setMinWidth(24);
        iconBox.setMaxWidth(24);
        iconBox.setAlignment(Pos.CENTER);

        Label textLbl = label(text, 13, "#f8fafc", false);
        textLbl.setVisible(false);
        textLbl.setManaged(false);

        item.getChildren().addAll(iconBox, textLbl);

        item.setOnMouseEntered(e -> item.setStyle(
                "-fx-background-color: rgba(255,255,255,0.15); -fx-background-radius: 8;"));
        item.setOnMouseExited(e -> item.setStyle("-fx-background-radius: 8;"));

        item.setOnMouseClicked(e -> {
            resetNavStyles();
            item.setStyle("-fx-background-color: rgba(255,255,255,0.20); -fx-background-radius: 8;");
            iconLbl.setStyle("-fx-font-family:" + FA + ";-fx-font-size:14px;-fx-text-fill:white;");
            textLbl.setTextFill(Color.WHITE);

            root.setCenter(switch (text) {
                case "Centro de operaciones" ->
                    buildMainContent();
                case "Alertas" ->
                    new AlertasAdminPoliciaView(alertaService).build();
                case "Alarmas" ->
                    new AlarmasAdminPoliciaView(alarmaService).build();
                case "Asignaciones" ->
                    new AsignacionesAdminPoliciaView(asignacionService, unidadService, alarmaService).build();
                case "Historial" ->
                    new HistorialAdminPoliciaView(atencionService).build();
                case "Policías" ->
                    new PoliciasAdminPoliciaView(policiaService, unidadService).build();
                case "Unidades" ->
                    new UnidadesAdminPoliciaView(unidadService).build();
                case "Estadísticas" ->
                    new EstadisticasAdminPoliciaView(alertaService, asignacionService, unidadService, policiaService, alarmaService, notificacionService).build();
                case "Reportes" ->
                    new ReportesAdminPoliciaView(alertaService, asignacionService, unidadService, policiaService, alarmaService, notificacionService, atencionService).build();
                case "Notificaciones" ->
                    new NotificacionesAdminPoliciaView(notificacionService).build();
                case "Configuración" ->
                    new ConfiguracionAdminPoliciaView(usuarioActual).build();
                default ->
                    buildMainContent();
            });
        });
        return item;
    }

    private void resetNavStyles() {
        nav.getChildren().forEach(node -> {
            if (node instanceof HBox hbox) {
                hbox.setStyle("-fx-background-radius: 8;");
                hbox.getChildren().forEach(child -> {
                    if (child instanceof StackPane sp) {
                        sp.getChildren().forEach(c -> {
                            if (c instanceof Label lbl && lbl.getStyle().contains("Font Awesome")) {
                                lbl.setStyle("-fx-font-family:" + FA + ";-fx-font-size:14px;-fx-text-fill:#8899bb;");
                            }
                        });
                    }
                    if (child instanceof Label lbl && !lbl.getStyle().contains("Font Awesome")) {
                        lbl.setTextFill(Color.web("#f8fafc"));
                    }
                });
            }
        });
    }

    // ── Nav item "Mapas" con submenú ──────────────────────────────
    private HBox buildMapaNavItem() {
        HBox item = new HBox(10);
        item.setPadding(new Insets(9, 12, 9, 12));
        item.setAlignment(Pos.CENTER_LEFT);
        item.setCursor(javafx.scene.Cursor.HAND);
        item.setMaxWidth(Double.MAX_VALUE);
        item.setStyle("-fx-background-radius: 8;");

        Label arrowLbl = label("▶", 10, WHITE, false);

        Label mapaIcon = new Label("\uf3c5");
        mapaIcon.setStyle("-fx-font-family:" + FA + ";-fx-font-size:14px;-fx-text-fill:#8899bb;");
        StackPane iconBox = new StackPane(mapaIcon);
        iconBox.setPrefWidth(24);
        iconBox.setMinWidth(24);
        iconBox.setMaxWidth(24);
        iconBox.setAlignment(Pos.CENTER);

        Label mapaText = label("Mapas", 13, "#f8fafc", false);
        mapaText.setVisible(false);
        mapaText.setManaged(false);

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        item.getChildren().addAll(iconBox, mapaText, sp, arrowLbl);
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
                    subNavItem("\uf279", "Mapa de alertas"),
                    subNavItem("\uf06d", "Zonas peligrosas"),
                    subNavItem("\uf0c0", "Mapa Operaciones"));
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

        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-family:" + FA + ";-fx-font-size:12px;-fx-text-fill:#8899bb;");
        StackPane iconBox = new StackPane(iconLbl);
        iconBox.setPrefWidth(20);
        iconBox.setMinWidth(20);
        iconBox.setAlignment(Pos.CENTER);

        item.getChildren().addAll(iconBox, label(text, 11, WHITE, true));

        item.setOnMouseClicked(e -> root.setCenter(switch (text) {
            case "Zonas peligrosas" ->
                new MapaZonasPeligrosas(alertaService).build();
            case "Mapa de alertas" ->
                new MapaAlertas(alertaService).build();
            case "Mapa Operaciones" ->
                new MapaOperaciones(asignacionService, unidadService).build();
            default ->
                new MapaAdminPoliciaView(alertaService, text).build();
        }));
        return item;
    }

    // =========================================================================
    // MAIN CONTENT
    // =========================================================================
    private ScrollPane buildMainContent() {
        VBox content = new VBox(18);
        content.setPadding(new Insets(22));
        content.setStyle("-fx-background-color:" + BG + ";");
        content.getChildren().addAll(
                buildTopBar(),
                buildStats(),
                buildCenterPanels(),
                buildBottomPanels());

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setPannable(true);
        scroll.setFocusTraversable(false);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setStyle("-fx-background-color:" + BG + "; -fx-background:" + BG + ";");
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
        greeting.getChildren().addAll(hello,
                label("Centro de operaciones policial — WolertApp", 12, GRAY_TEXT, false));

        HBox rightBox = new HBox(16);
        rightBox.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(rightBox, Priority.ALWAYS);

        // Fecha / hora
        VBox dateBox = new VBox(2);
        dateBox.setAlignment(Pos.CENTER_RIGHT);
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", new Locale("es", "CO"));
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("hh:mm:ss a", new Locale("es", "CO"));
        LocalDateTime now0 = LocalDateTime.now(ZoneId.of("America/Bogota"));
        Label calIcon = new Label("\uf073");
        calIcon.setStyle("-fx-font-family:" + FA + ";-fx-font-size:12px;-fx-text-fill:#374151;");
        Label dateLbl = label(now0.format(dateFmt), 12, "#374151", false);
        Label timeLbl = label(now0.format(timeFmt), 12, GRAY_TEXT, false);
        HBox dateRow = new HBox(5, calIcon, dateLbl);
        dateRow.setAlignment(Pos.CENTER_RIGHT);
        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), ev -> {
            LocalDateTime now = LocalDateTime.now(ZoneId.of("America/Bogota"));
            dateLbl.setText(now.format(dateFmt));
            timeLbl.setText(now.format(timeFmt));
        }));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();
        dateBox.getChildren().addAll(dateRow, timeLbl);

        // ── Campana con panel de notificaciones ───────────────────
        // ── Campana estilo Admin ──────────────────────────────────
        List<Alerta> alertasActivas = new java.util.ArrayList<>();
        try {
            if (alertaService != null) {
                alertaService.listar().stream()
                        .filter(a -> a.getEstado() == EstadoAlerta.PENDIENTE
                        || a.getEstado() == EstadoAlerta.EN_ATENCION
                        || a.getEstado() == EstadoAlerta.UNIDAD_ASIGNADA
                        || a.getEstado() == EstadoAlerta.RECIBIDA)
                        .sorted((a, b) -> b.getFechaHora() != null && a.getFechaHora() != null
                        ? b.getFechaHora().compareTo(a.getFechaHora()) : 0)
                        .limit(4)
                        .forEach(alertasActivas::add);
            }
        } catch (Exception ignored) {
        }

        List<Notificacion> ultimasNotis = new java.util.ArrayList<>();
        try {
            if (notificacionService != null) {
                notificacionService.listar().stream()
                        .limit(3)
                        .forEach(ultimasNotis::add);
            }
        } catch (Exception ignored) {
        }

        int totalBadge = alertasActivas.size() + ultimasNotis.size();

        bellStack = new StackPane();
        bellStack.setCursor(javafx.scene.Cursor.HAND);

        Region bellBg = new Region();
        bellBg.setPrefSize(40, 40);
        bellBg.setStyle("-fx-background-color:white;-fx-background-radius:50%;"
                + "-fx-border-color:#e5e7eb;-fx-border-radius:50%;-fx-border-width:1.5;");

        Label bellIcon = new Label("\uf0a2");
        bellIcon.setStyle("-fx-font-family:" + FA + ";-fx-font-size:16px;-fx-text-fill:#374151;");

        Label badgeNum = new Label(totalBadge > 9 ? "9+" : String.valueOf(totalBadge));
        badgeNum.setStyle("-fx-background-color:#e53935;-fx-text-fill:white;"
                + "-fx-font-size:9px;-fx-font-weight:bold;"
                + "-fx-background-radius:20;-fx-padding:1 4;");
        badgeNum.setTranslateX(10);
        badgeNum.setTranslateY(-10);
        badgeNum.setVisible(totalBadge > 0);
        bellStack.getChildren().addAll(bellBg, bellIcon, badgeNum);

        bellStack.setOnMouseEntered(e -> bellBg.setStyle(
                "-fx-background-color:#f0f7ff;-fx-background-radius:50%;"
                + "-fx-border-color:" + BLUE + ";-fx-border-radius:50%;-fx-border-width:1.5;"));
        bellStack.setOnMouseExited(e -> bellBg.setStyle(
                "-fx-background-color:white;-fx-background-radius:50%;"
                + "-fx-border-color:#e5e7eb;-fx-border-radius:50%;-fx-border-width:1.5;"));

// ── Popup ─────────────────────────────────────────────────
        javafx.stage.Popup popup = new javafx.stage.Popup();
        popup.setAutoHide(true);

        VBox popupBox = new VBox(0);
        popupBox.setPrefWidth(340);
        popupBox.setStyle("-fx-background-color:white;-fx-background-radius:14;"
                + "-fx-border-color:#e5e7eb;-fx-border-radius:14;-fx-border-width:1;"
                + "-fx-effect:dropshadow(gaussian,rgba(15,23,42,0.18),24,0,0,8);");

// Header popup
        HBox popHeader = new HBox(8);
        popHeader.setPadding(new Insets(14, 16, 12, 16));
        popHeader.setAlignment(Pos.CENTER_LEFT);
        popHeader.setStyle("-fx-background-color:#f8fafc;-fx-background-radius:14 14 0 0;"
                + "-fx-border-color:transparent transparent #e5e7eb transparent;"
                + "-fx-border-width:0 0 1 0;");
        Label popTitle = new Label("Notificaciones del sistema");
        popTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        popTitle.setTextFill(Color.web("#111827"));
        HBox.setHgrow(popTitle, Priority.ALWAYS);
        Label popBadge = new Label(totalBadge + " nuevas");
        popBadge.setStyle("-fx-background-color:#e5393522;-fx-text-fill:#e53935;"
                + "-fx-font-size:11px;-fx-font-weight:bold;"
                + "-fx-background-radius:20;-fx-padding:3 8;");
        popHeader.getChildren().addAll(popTitle, popBadge);
        popupBox.getChildren().add(popHeader);

// Sección alertas
        if (!alertasActivas.isEmpty()) {
            HBox secBox = new HBox(6);
            secBox.setPadding(new Insets(8, 16, 6, 16));
            secBox.setStyle("-fx-background-color:#f9fafb;");
            Label secIco = new Label("\uf0f3");
            secIco.setStyle("-fx-font-family:" + FA + ";-fx-font-size:11px;-fx-text-fill:" + RED + ";");
            Label secTxt = new Label("Alertas activas");
            secTxt.setFont(Font.font("System", FontWeight.BOLD, 11));
            secTxt.setTextFill(Color.web(GRAY_TEXT));
            secBox.getChildren().addAll(secIco, secTxt);
            popupBox.getChildren().add(secBox);

            for (Alerta a : alertasActivas) {
                String tipo = a.getTipoalerta() != null ? a.getTipoalerta().getNombre() : "Alerta";
                String barrio = a.getBarrio() != null ? a.getBarrio().getNombre() : "—";
                String fecha = a.getFechaHora() != null
                        ? a.getFechaHora().format(DateTimeFormatter.ofPattern("dd/MM HH:mm")) : "—";
                String estadoStr = a.getEstado() != null
                        ? a.getEstado().name().replace("_", " ") : "—";
                String dotColor = estadoColor(a.getEstado());

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
                    root.setCenter(new AlertasAdminPoliciaView(alertaService).build());
                });

                Circle dot = new Circle(5, Color.web(dotColor));
                VBox info = new VBox(2);
                HBox.setHgrow(info, Priority.ALWAYS);
                Label tLbl = new Label(tipo + " — " + barrio);
                tLbl.setFont(Font.font("System", FontWeight.BOLD, 12));
                tLbl.setTextFill(Color.web("#111827"));
                Label sLbl = new Label(estadoStr + "  ·  " + fecha);
                sLbl.setFont(Font.font("System", 11));
                sLbl.setTextFill(Color.web(GRAY_TEXT));
                info.getChildren().addAll(tLbl, sLbl);
                fila.getChildren().addAll(dot, info);
                popupBox.getChildren().add(fila);
            }
        }

// Sección notificaciones
        if (!ultimasNotis.isEmpty()) {
            HBox secBox = new HBox(6);
            secBox.setPadding(new Insets(8, 16, 6, 16));
            secBox.setStyle("-fx-background-color:#f9fafb;");
            Label secIco = new Label("\uf1f6");
            secIco.setStyle("-fx-font-family:" + FA + ";-fx-font-size:11px;-fx-text-fill:" + BLUE + ";");
            Label secTxt = new Label("Notificaciones recientes");
            secTxt.setFont(Font.font("System", FontWeight.BOLD, 11));
            secTxt.setTextFill(Color.web(GRAY_TEXT));
            secBox.getChildren().addAll(secIco, secTxt);
            popupBox.getChildren().add(secBox);

            for (Notificacion n : ultimasNotis) {
                String msg = n.getMensaje() != null
                        ? (n.getMensaje().length() > 40 ? n.getMensaje().substring(0, 40) + "…" : n.getMensaje()) : "—";
                String dest = n.getCorreodestinatario() != null ? n.getCorreodestinatario() : "—";

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
                    root.setCenter(new NotificacionesAdminPoliciaView(notificacionService).build());
                });

                Circle dot = new Circle(5, Color.web(BLUE));
                VBox info = new VBox(2);
                HBox.setHgrow(info, Priority.ALWAYS);
                Label tLbl = new Label(msg);
                tLbl.setFont(Font.font("System", FontWeight.BOLD, 12));
                tLbl.setTextFill(Color.web("#111827"));
                Label sLbl = new Label(dest);
                sLbl.setFont(Font.font("System", 11));
                sLbl.setTextFill(Color.web(GRAY_TEXT));
                info.getChildren().addAll(tLbl, sLbl);
                fila.getChildren().addAll(dot, info);
                popupBox.getChildren().add(fila);
            }
        }

// Vacío
        if (alertasActivas.isEmpty() && ultimasNotis.isEmpty()) {
            VBox vacio = new VBox(8);
            vacio.setAlignment(Pos.CENTER);
            vacio.setPadding(new Insets(28));
            Label icoV = new Label("\uf1f6");
            icoV.setStyle("-fx-font-family:" + FA + ";-fx-font-size:28px;-fx-text-fill:#94a3b8;");
            Label msgV = new Label("Sin notificaciones nuevas");
            msgV.setStyle("-fx-font-size:13px;-fx-text-fill:" + GRAY_TEXT + ";");
            vacio.getChildren().addAll(icoV, msgV);
            popupBox.getChildren().add(vacio);
        }

        // Footer popup
        HBox popFooter = new HBox(24);
        popFooter.setPadding(new Insets(10, 16, 12, 16));
        popFooter.setAlignment(Pos.CENTER);
        popFooter.setStyle("-fx-background-color:#f8fafc;-fx-background-radius:0 0 14 14;"
                + "-fx-border-color:#e5e7eb transparent transparent transparent;"
                + "-fx-border-width:1 0 0 0;");
        Label verAlertas = new Label("Ver alertas →");
        verAlertas.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:" + RED + ";");
        verAlertas.setCursor(javafx.scene.Cursor.HAND);
        verAlertas.setOnMouseClicked(e -> {
            popup.hide();
            root.setCenter(new AlertasAdminPoliciaView(alertaService).build());
        });
        Label verNotifs = new Label("Ver notificaciones →");
        verNotifs.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:" + BLUE + ";");
        verNotifs.setCursor(javafx.scene.Cursor.HAND);
        verNotifs.setOnMouseClicked(e -> {
            popup.hide();
            root.setCenter(new NotificacionesAdminPoliciaView(notificacionService).build());
        });
        popFooter.getChildren().addAll(verAlertas, verNotifs);
        popupBox.getChildren().add(popFooter);
        popup.getContent().add(popupBox);

        bellStack.setOnMouseClicked(e -> {
            if (popup.isShowing()) {
                popup.hide();
            } else {
                badgeNum.setVisible(false);
                javafx.geometry.Bounds b = bellStack.localToScreen(bellStack.getBoundsInLocal());
                popup.show(bellStack, b.getMaxX() - 340, b.getMaxY() + 8);
            }
        });

        rightBox.getChildren().addAll(dateBox, bellStack);
        bar.getChildren().addAll(greeting, rightBox);
        return bar;
    }

    // ── Panel de notificaciones flotante ─────────────────────────
    /**
     * Abre/cierra un panel flotante de notificaciones sobre el contenido
     * principal. Al hacer clic en "Ver mapa operacional" navega al
     * MapaOperaciones.
     */
    private void togglePanelNotificaciones() {
        if (panelNotiVisible && panelNoti != null) {
            // Cerrar
            if (root.getCenter() instanceof StackPane sp) {
                sp.getChildren().remove(panelNoti);
            }
            panelNotiVisible = false;
            panelNoti = null;
            return;
        }

        // Construir panel
        panelNoti = buildPanelNotificaciones();
        panelNotiVisible = true;

        // Envolver el centro actual en un StackPane si no lo es ya
        if (!(root.getCenter() instanceof StackPane)) {
            javafx.scene.Node centro = root.getCenter();
            StackPane sp = new StackPane(centro);
            root.setCenter(sp);
        }

        StackPane centroStack = (StackPane) root.getCenter();
        StackPane.setAlignment(panelNoti, Pos.TOP_RIGHT);
        StackPane.setMargin(panelNoti, new Insets(8, 16, 0, 0));
        centroStack.getChildren().add(panelNoti);
    }

    private VBox buildPanelNotificaciones() {
        VBox panel = new VBox(0);
        panel.setPrefWidth(360);
        panel.setMaxWidth(360);
        panel.setStyle(
                "-fx-background-color: white;"
                + "-fx-background-radius: 16;"
                + "-fx-border-radius: 16;"
                + "-fx-border-color: #e5e7eb;"
                + "-fx-border-width: 1;"
                + "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.18),18,0,0,6);");

        // Header
        HBox header = new HBox(10);
        header.setPadding(new Insets(14, 16, 12, 16));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle(
                "-fx-background-color: linear-gradient(to right, #1565c0, #1976d2);"
                + "-fx-background-radius: 16 16 0 0;");

        Label bellLbl = new Label("\uf0a2");
        bellLbl.setStyle("-fx-font-family:" + FA + ";-fx-font-size:16px;-fx-text-fill:white;");
        Label titleLbl = label("Notificaciones", 14, WHITE, true);
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        // Botón "Ver mapa" → MapaOperaciones
        Button btnMapa = new Button("\uf3c5  Mapa");
        btnMapa.setStyle(
                "-fx-background-color: rgba(255,255,255,0.20);"
                + "-fx-text-fill: white;"
                + "-fx-font-family: 'Font Awesome 6 Free Solid', System;"
                + "-fx-font-size: 11px;"
                + "-fx-background-radius: 8;"
                + "-fx-padding: 5 10;"
                + "-fx-cursor: hand;");
        btnMapa.setOnMouseEntered(e -> btnMapa.setStyle(
                "-fx-background-color: rgba(255,255,255,0.35);"
                + "-fx-text-fill: white;"
                + "-fx-font-size: 11px;"
                + "-fx-background-radius: 8;"
                + "-fx-padding: 5 10;"
                + "-fx-cursor: hand;"));
        btnMapa.setOnMouseExited(e -> btnMapa.setStyle(
                "-fx-background-color: rgba(255,255,255,0.20);"
                + "-fx-text-fill: white;"
                + "-fx-font-size: 11px;"
                + "-fx-background-radius: 8;"
                + "-fx-padding: 5 10;"
                + "-fx-cursor: hand;"));
        btnMapa.setOnAction(e -> {
            // Cerrar panel y navegar al mapa de operaciones
            togglePanelNotificaciones();
            // Quitar StackPane wrapper si existe
            if (root.getCenter() instanceof StackPane sp2 && sp2.getChildren().size() == 1) {
                javafx.scene.Node inner = sp2.getChildren().get(0);
                root.setCenter(inner);
            }
            javafx.scene.Node mapaNode = new MapaOperaciones(asignacionService, unidadService).build();
            root.setCenter(mapaNode);
        });

        // Botón cerrar
        Label closeLbl = new Label("\uf00d");
        closeLbl.setStyle("-fx-font-family:" + FA + ";-fx-font-size:12px;-fx-text-fill:rgba(255,255,255,0.8);-fx-cursor:hand;");
        closeLbl.setOnMouseClicked(e -> togglePanelNotificaciones());

        header.getChildren().addAll(bellLbl, titleLbl, sp, btnMapa, closeLbl);

        // Cuerpo con scroll
        VBox body = new VBox(0);
        try {
            List<Notificacion> lista = notificacionService != null
                    ? notificacionService.listar() : List.of();
            if (lista.isEmpty()) {
                VBox empty = new VBox(8);
                empty.setAlignment(Pos.CENTER);
                empty.setPadding(new Insets(30, 0, 30, 0));
                Label emptyIco = new Label("\uf0f3");
                emptyIco.setStyle("-fx-font-family:" + FA + ";-fx-font-size:32px;-fx-text-fill:#d1d5db;");
                Label emptyMsg = label("Sin notificaciones", 13, GRAY_TEXT, false);
                empty.getChildren().addAll(emptyIco, emptyMsg);
                body.getChildren().add(empty);
            } else {
                lista.stream().limit(8).forEach(n -> {
                    body.getChildren().add(buildNotiRow(n));
                    Region sep = new Region();
                    sep.setPrefHeight(1);
                    sep.setStyle("-fx-background-color:" + BORDER + ";");
                    body.getChildren().add(sep);
                });
            }
        } catch (Exception ex) {
            body.getChildren().add(label("Error al cargar notificaciones", 12, RED, false));
        }

        ScrollPane scroll = new ScrollPane(body);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setPrefViewportHeight(280);
        scroll.setStyle("-fx-background: white; -fx-background-color: white; -fx-border-color: transparent;");

        // Footer — ver todas
        HBox footer = new HBox();
        footer.setPadding(new Insets(10, 16, 12, 16));
        footer.setAlignment(Pos.CENTER);
        footer.setStyle("-fx-border-color:#e5e7eb transparent transparent;-fx-border-width:1 0 0 0;");
        Label verTodas = label("Ver todas las notificaciones →", 12, BLUE, true);
        verTodas.setCursor(javafx.scene.Cursor.HAND);
        verTodas.setOnMouseClicked(e -> {
            togglePanelNotificaciones();
            if (root.getCenter() instanceof StackPane sp2 && sp2.getChildren().size() == 1) {
                root.setCenter(sp2.getChildren().get(0));
            }
            root.setCenter(new NotificacionesAdminPoliciaView(notificacionService).build());
        });
        footer.getChildren().add(verTodas);

        panel.getChildren().addAll(header, scroll, footer);
        return panel;
    }

    private HBox buildNotiRow(Notificacion n) {
        HBox row = new HBox(12);
        row.setPadding(new Insets(10, 16, 10, 16));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: transparent;");
        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #f8f9fd; -fx-cursor: hand;"));
        row.setOnMouseExited(e -> row.setStyle("-fx-background-color: transparent;"));

        // Ícono según estado
        String[] sc = estadoNotiColors(n.getEstado());
        StackPane iconBox = new StackPane();
        iconBox.setMinSize(36, 36);
        iconBox.setMaxSize(36, 36);
        Rectangle iconBg = new Rectangle(36, 36);
        iconBg.setArcWidth(10);
        iconBg.setArcHeight(10);
        iconBg.setFill(Color.web(sc[1]));
        Label iconLbl = new Label(sc[2]);
        iconLbl.setStyle("-fx-font-family:" + FA + ";-fx-font-size:14px;-fx-text-fill:" + sc[0] + ";");
        iconBox.getChildren().addAll(iconBg, iconLbl);

        // Textos
        VBox texts = new VBox(2);
        HBox.setHgrow(texts, Priority.ALWAYS);
        String dest = n.getCorreodestinatario() != null ? n.getCorreodestinatario() : "—";
        String msg = n.getMensaje() != null && !n.getMensaje().isBlank()
                ? (n.getMensaje().length() > 50 ? n.getMensaje().substring(0, 48) + "…" : n.getMensaje())
                : "Sin mensaje";
        texts.getChildren().addAll(
                label(dest, 12, "#111827", true),
                label(msg, 11, GRAY_TEXT, false));

        // Badge estado
        Label badge = label(n.getEstado() != null ? n.getEstado().name() : "—", 9, sc[0], true);
        badge.setPadding(new Insets(2, 7, 2, 7));
        badge.setStyle("-fx-background-color:" + sc[1] + ";-fx-background-radius:12;"
                + "-fx-text-fill:" + sc[0] + ";-fx-font-weight:bold;-fx-font-size:9px;");

        row.getChildren().addAll(iconBox, texts, badge);
        return row;
    }

    private String[] estadoNotiColors(EstadoNotificacion e) {
        // [color, bgColor, faIcon]
        if (e == null) {
            return new String[]{ORANGE, "#fff8e1", "\uf017"};
        }
        return switch (e) {
            case LEIDA ->
                new String[]{GREEN, GREEN_LIGHT, "\uf058"};
            case ENVIADA ->
                new String[]{BLUE, BLUE_LIGHT, "\uf1d8"};
            case ERROR ->
                new String[]{RED, RED_LIGHT, "\uf057"};
            default ->
                new String[]{ORANGE, "#fff8e1", "\uf017"};
        };
    }

    // ── Stat cards ────────────────────────────────────────────────
    private HBox buildStats() {
        HBox row = new HBox(16);
        row.getChildren().addAll(
                statCard(RED_LIGHT, RED, "Alertas activas", contarAlertasActivas(), "PENDIENTE / EN ATENCIÓN", "\uf0f3", RED),
                statCard(ORANGE_LIGHT, ORANGE, "Asignaciones", contarAsignaciones(), "Total registradas", "\uf14a", ORANGE),
                statCard(GREEN_LIGHT, GREEN, "Unidades activas", contarUnidadesActivas(), "Estado OPERATIVA", "\uf1b9", GREEN),
                statCard(BLUE_LIGHT, BLUE, "Policías activos", contarPoliciasActivos(), "Estado DISPONIBLE", "\uf505", BLUE));
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
        colorBg.setStyle("-fx-background-color:" + bgIcon + ";"
                + "-fx-background-radius:14;-fx-border-color:" + accentColor + ";"
                + "-fx-border-radius:14;-fx-border-width:1.5;");
        Label faLbl = new Label(faIcon);
        faLbl.setStyle("-fx-font-family:" + FA + ";-fx-font-size:22px;-fx-text-fill:" + iconColor + ";");
        iconWrap.getChildren().addAll(colorBg, faLbl);

        Label titleLbl = label(title, 13, "#374151", true);
        Label valLbl = new Label(String.valueOf(value));
        valLbl.setStyle("-fx-font-size:36px;-fx-font-weight:bold;-fx-text-fill:" + accentColor + ";");
        Label subLbl = label(sub, 11, GRAY_TEXT, false);

        VBox textBlock = new VBox(3);
        textBlock.getChildren().addAll(titleLbl, valLbl, subLbl);

        HBox topRow = new HBox(16);
        topRow.setAlignment(Pos.CENTER_LEFT);
        topRow.getChildren().addAll(iconWrap, textBlock);
        card.getChildren().add(topRow);

        card.setOnMouseEntered(e -> {
            card.setTranslateY(-3);
            card.setStyle("-fx-background-color:white;-fx-background-radius:18;"
                    + "-fx-border-color:" + accentColor + ";-fx-border-width:1.5;-fx-border-radius:18;");
        });
        card.setOnMouseExited(e -> {
            card.setTranslateY(0);
            card.setStyle("-fx-background-color:white;-fx-background-radius:18;");
        });
        return card;
    }

    // ── Paneles centrales ─────────────────────────────────────────
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
                            item.setOnMouseClicked(e
                                    -> root.setCenter(new AlertasAdminPoliciaView(alertaService).build()));
                            item.setOnMouseEntered(e
                                    -> item.setStyle("-fx-background-color:#f0f4ff;-fx-cursor:hand;-fx-background-radius:8;"));
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

        // ── Mapa real embebido ────────────────────────────────────────
        try {
            MapaOperaciones mapaOps = new MapaOperaciones(asignacionService, unidadService);
            BorderPane mapaCompleto = mapaOps.build();

            // Ocultar el top bar y el panel derecho — solo queremos el mapa
            mapaCompleto.setTop(null);
            mapaCompleto.setRight(null);

            // Si el centro es un HBox (mapa + panel derecho), extraer solo el mapa
            if (mapaCompleto.getCenter() instanceof HBox hb && !hb.getChildren().isEmpty()) {
                javafx.scene.Node soloMapa = hb.getChildren().get(0);
                hb.getChildren().remove(soloMapa);

                StackPane mapaWrapper = new StackPane(soloMapa);
                mapaWrapper.setPrefHeight(200);
                mapaWrapper.setMinHeight(200);
                mapaWrapper.setMaxHeight(200);
                mapaWrapper.setStyle("-fx-background-radius:10;-fx-background-color:#d1e8d1;");

                // Botón "Abrir mapa completo"
                Label btnAbrir = new Label("\uf065  Ver mapa completo");
                btnAbrir.setStyle(
                        "-fx-background-color:rgba(31,58,86,0.85);-fx-text-fill:white;"
                        + "-fx-font-size:11px;-fx-font-weight:bold;"
                        + "-fx-background-radius:8;-fx-padding:6 12;-fx-cursor:hand;");
                btnAbrir.setFont(Font.font("System", FontWeight.BOLD, 11));
                StackPane.setAlignment(btnAbrir, Pos.BOTTOM_RIGHT);
                StackPane.setMargin(btnAbrir, new Insets(0, 10, 10, 0));
                mapaWrapper.getChildren().add(btnAbrir);

                btnAbrir.setOnMouseClicked(e
                        -> root.setCenter(new MapaOperaciones(asignacionService, unidadService).build()));

                card.getChildren().add(mapaWrapper);
            } else {
                // Fallback: abrir en modal
                card.getChildren().add(buildMapaSimulado());
            }
        } catch (Exception ex) {
            System.err.println("Error embebiendo mapa: " + ex.getMessage());
            card.getChildren().add(buildMapaSimulado());
        }

        // Clic en toda la card también abre el mapa completo
        card.setCursor(javafx.scene.Cursor.HAND);
        card.setOnMouseClicked(e -> root.setCenter(
                new MapaOperaciones(asignacionService, unidadService).build()));

        return card;
    }

// ── Mapa simulado como fallback ───────────────────────────────
    private StackPane buildMapaSimulado() {
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
        streets.getChildren().addAll(
                mapDot(70, 50, RED), mapDot(180, 35, ORANGE),
                mapDot(260, 50, GREEN), mapDot(55, 130, GREEN), mapDot(270, 140, RED));

        VBox popup = new VBox(4);
        popup.setPadding(new Insets(8, 12, 8, 12));
        popup.setStyle("-fx-background-color: white; -fx-background-radius: 8;");
        popup.setEffect(new DropShadow(6, Color.web("#0000001a")));
        popup.setTranslateX(15);
        popup.setTranslateY(8);
        popup.getChildren().addAll(
                label("Mapa interactivo", 11, "#374151", true),
                label("Clic para abrir mapa completo →", 10, BLUE, false));
        mapArea.getChildren().addAll(mapBg, streets, popup);
        return mapArea;
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
                        item.setOnMouseClicked(e -> root.setCenter(new UnidadesAdminPoliciaView(unidadService).build()));
                        item.setOnMouseEntered(e -> item.setStyle("-fx-background-color:#f0f4ff;-fx-cursor:hand;-fx-background-radius:8;"));
                        item.setOnMouseExited(e -> item.setStyle("-fx-background-color:transparent;"));
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
                        item.setOnMouseClicked(e -> root.setCenter(new PoliciasAdminPoliciaView(policiaService, unidadService).build()));
                        item.setOnMouseEntered(e -> item.setStyle("-fx-background-color:#f0f4ff;-fx-cursor:hand;-fx-background-radius:8;"));
                        item.setOnMouseExited(e -> item.setStyle("-fx-background-color:transparent;"));
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

        grid.add(actionBtn("\uf14a", "Asignar", BLUE, BLUE_LIGHT, () -> root.setCenter(new AsignacionesAdminPoliciaView(asignacionService, unidadService, alarmaService).build())), 0, 0);
        grid.add(actionBtn("\uf0f3", "Nueva alerta", RED, RED_LIGHT, () -> {
            MapaAlerta mapa = new MapaAlerta((Stage) root.getScene().getWindow(), usuarioActual, alertaService, null);
            mapa.mostrar();
        }), 1, 0);
        grid.add(actionBtn("\uf0a1", "Notificar", GREEN, GREEN_LIGHT, () -> root.setCenter(new NotificacionesAdminPoliciaView(notificacionService).build())), 0, 1);
        grid.add(actionBtn("\uf080", "Reportes", ORANGE, ORANGE_LIGHT, () -> root.setCenter(new ReportesAdminPoliciaView(alertaService, asignacionService, unidadService, policiaService, alarmaService, notificacionService, atencionService).build())), 1, 1);
        grid.add(actionBtn("\uf3c5", "Mapa ops.", "#7b1fa2", "#f3e5f5", () -> {
            MapaOperaciones mapa = new MapaOperaciones(asignacionService, unidadService);
            Stage stage = new Stage();
            stage.setTitle("Mapa de operaciones");
            stage.setScene(new javafx.scene.Scene(mapa.build(), 1100, 680));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.show();
        }), 0, 2);
        grid.add(actionBtn("\uf201", "Estadísticas", "#0097a7", "#e0f7fa", () -> root.setCenter(new EstadisticasAdminPoliciaView(alertaService, asignacionService, unidadService, policiaService, alarmaService, notificacionService).build())), 1, 2);

        acciones.getChildren().add(grid);
        row.getChildren().addAll(unidades, policias, acciones);
        return row;
    }

    private VBox actionBtn(String faIcon, String text, String iconColor, String bgColor, Runnable accion) {
        StackPane iconWrap = new StackPane();
        iconWrap.setPrefSize(34, 34);
        iconWrap.setMinSize(34, 34);
        Region iconBg = new Region();
        iconBg.setPrefSize(34, 34);
        iconBg.setStyle("-fx-background-color:" + bgColor + ";-fx-background-radius:9;");
        Label faLbl = new Label(faIcon);
        faLbl.setStyle("-fx-font-family:" + FA + ";-fx-font-size:14px;-fx-text-fill:" + iconColor + ";");
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

        String base = "-fx-background-color:white;-fx-background-radius:12;-fx-border-color:" + BORDER + ";-fx-border-radius:12;-fx-border-width:1;";
        String hover = "-fx-background-color:" + bgColor + ";-fx-background-radius:12;-fx-border-color:" + iconColor + ";-fx-border-radius:12;-fx-border-width:1.5;";

        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e -> btn.setStyle(base));
        btn.setOnMouseClicked(e -> accion.run());
        return btn;
    }

    // =========================================================================
    // CERRAR SESIÓN
    private void cerrarSesion() {
        ((Stage) root.getScene().getWindow()).close();
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
    // HELPERS
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
        panel.setStyle("-fx-background-color:" + WHITE + ";-fx-background-radius:12;");
        shadow(panel);

        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(28, 28);
        iconBox.setMinSize(28, 28);
        Region iconBg = new Region();
        iconBg.setPrefSize(28, 28);
        iconBg.setStyle("-fx-background-color:" + bgColor + ";-fx-background-radius:7;");
        Label faLbl = new Label(faIcon);
        faLbl.setStyle("-fx-font-family:" + FA + ";-fx-font-size:13px;-fx-text-fill:" + iconColor + ";");
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
        iconBg.setStyle("-fx-background-color:#e8f0fe;-fx-background-radius:10;");
        Label faLbl = new Label(faIcon);
        faLbl.setStyle("-fx-font-family:" + FA + ";-fx-font-size:14px;-fx-text-fill:" + BLUE + ";");
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

        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(34, 34);
        iconBox.setMinSize(34, 34);
        iconBox.setMaxSize(34, 34);
        Region iconBg = new Region();
        iconBg.setPrefSize(34, 34);
        iconBg.setStyle("-fx-background-color:#e8f0fe;-fx-background-radius:10;");
        Label faLbl = new Label("\uf0f3");
        faLbl.setStyle("-fx-font-family:" + FA + ";-fx-font-size:14px;-fx-text-fill:" + BLUE + ";");
        iconBox.getChildren().addAll(iconBg, faLbl);

        VBox text = new VBox(2);
        HBox.setHgrow(text, Priority.ALWAYS);
        text.getChildren().addAll(
                label(title, 12, "#111827", false),
                label(sub, 10, GRAY_TEXT, false));

        row.getChildren().addAll(dotCircle, iconBox, text, label(">", 13, GRAY_TEXT, false));
        return row;
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
        sep.setStyle("-fx-background-color:" + BORDER + ";");
        return sep;
    }

    private void shadow(Region node) {
        node.setEffect(new DropShadow(10, 0, 2, Color.web("#0000001a")));
    }

    private void mostrarAlerta(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
