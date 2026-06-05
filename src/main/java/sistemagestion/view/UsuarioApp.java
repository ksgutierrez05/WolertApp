/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.view;

/**
 *
 * @author Maria Cristina
 */

import javafx.application.Platform;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.Pair;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import sistemagestion.model.*;
import sistemagestion.service.*;
import static sistemagestion.view.AppColors.BG_USER;
import static sistemagestion.view.AppColors.BLUE;
import static sistemagestion.view.AppColors.BLUE_LIGHT;
import static sistemagestion.view.AppColors.GRAY_TEXT;
import static sistemagestion.view.AppColors.GREEN;
import static sistemagestion.view.AppColors.ORANGE;
import static sistemagestion.view.AppColors.RED;
import static sistemagestion.view.AppColors.SIDEBAR_HOVER;
import static sistemagestion.view.AppColors.TEXT_PRIMARY;
import static sistemagestion.view.AppColors.WHITE;
import static sistemagestion.view.UIFactory.label;
import static sistemagestion.view.UIFactory.separator;
import static sistemagestion.view.UIFactory.shadowMd;



/**
 * Vista principal del usuario ciudadano.
 *
 * Principio SOLID:
 *  SRP — solo orquesta el dashboard de usuario; delega a builders y servicios.
 *  DIP — depende de IDashboardStatsService, no de la implementación concreta.
 *  OCP — la navegación se configura en NavigationRegistry.
 *
 * Patrón GRASP:
 *  Controller — coordina la interacción del usuario con el sistema.
 *  Creator — instancia sus dependencias e inyecta en el registry.
 */
public class UsuarioApp {

    // ── Dependencias ──────────────────────────────────────────────
    private final Usuario               usuarioActual;
    private final AlertaService         alertaService;
    private final SuscripcionService    suscripcionService;
    private final BarrioService         barrioService;
    private final NotificacionService   notificacionService;
    private final UsuarioDashboardStatsService statsService;

    // ── UI ────────────────────────────────────────────────────────
    private BorderPane root;
    private NavigationRegistry navRegistry;

    // ── Submenú mapa ──────────────────────────────────────────────
    private VBox  mapaSubMenu   = null;
    private boolean mapaExpandido = false;
    private VBox  navContainer;          // referencia al VBox del nav para toggleMapaSubMenu

    // ── Constructor (inyección directa) ───────────────────────────
    public UsuarioApp(Usuario usuarioActual,
                      AlertaService alertaService,
                      SuscripcionService suscripcionService,
                      BarrioService barrioService,
                      NotificacionService notificacionService) {
        this.usuarioActual       = usuarioActual;
        this.alertaService       = alertaService;
        this.suscripcionService  = suscripcionService;
        this.barrioService       = barrioService;
        this.notificacionService = notificacionService;
        this.statsService        = new UsuarioDashboardStatsService(alertaService, usuarioActual);
        Font.loadFont(getClass().getResourceAsStream("/fa-solid-900.ttf"), 20);
    }

    /** Constructor de conveniencia que instancia los servicios internamente. */
    public UsuarioApp(Usuario usuarioActual) {
        this.usuarioActual = usuarioActual;
        Font.loadFont(getClass().getResourceAsStream("/fa-solid-900.ttf"), 20);

        AlertaService       tmpA = null;
        SuscripcionService  tmpS = null;
        BarrioService       tmpB = null;
        NotificacionService tmpN = null;

        try { tmpA = new AlertaService();      } catch (SQLException e) { showError("AlertaService",      e); }
        try { tmpS = new SuscripcionService(); } catch (SQLException e) { showError("SuscripcionService", e); }
        try { tmpB = new BarrioService();      } catch (SQLException e) { showError("BarrioService",      e); }
        try { tmpN = new NotificacionService();} catch (SQLException e) { showError("NotificacionService",e); }

        this.alertaService       = tmpA;
        this.suscripcionService  = tmpS;
        this.barrioService       = tmpB;
        this.notificacionService = tmpN;
        this.statsService        = new UsuarioDashboardStatsService(alertaService, usuarioActual);
    }

    // =========================================================================
    // SHOW
    // =========================================================================
    public void show(Stage stage) {
        root = new BorderPane();
        root.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
        root.setStyle("-fx-background-color:" + BG_USER + ";-fx-padding:0;");

        // ── Registro de rutas (OCP) ───────────────────────────────
        navRegistry = new NavigationRegistry()
                .register("Dashboard",      this::buildMainContent)
                .register("Alertas",        () -> new AlertasView(usuarioActual, alertaService, barrioService).getView())
                .register("Mis Alertas",    () -> new MisAlertasView(usuarioActual, alertaService, barrioService, root).getView())
                .register("Mapa",           () -> { toggleMapaSubMenu(); return root.getCenter(); })
                .register("Vecinos",        () -> new VecinosView(usuarioActual, alertaService).getView())
                .register("Mi Cuenta",      () -> new MiCuentaView(usuarioActual, suscripcionService, root,
                                                        () -> root.setCenter(buildMainContent())).getView())
                .register("Notificaciones", () -> new NotificacionesView(usuarioActual, notificacionService, root).getView());

        // ── Sidebar ────────────────────────────────────────────────
        String nombre  = usuarioActual != null
                ? usuarioActual.getPrimer_nombre() + " " + usuarioActual.getPrimer_apellido()
                : "Usuario";
        String barrio  = usuarioActual != null
                && usuarioActual.getDireccion() != null
                && usuarioActual.getDireccion().getBarrio() != null
                ? "Barrio " + usuarioActual.getDireccion().getBarrio().getNombre()
                : "Sin barrio asignado";

        List<SidebarConfig.NavEntry> entries = List.of(
                new SidebarConfig.NavEntry("\uf015", "Dashboard",      () -> navigate("Dashboard")),
                new SidebarConfig.NavEntry("\uf0f3", "Alertas",        () -> navigate("Alertas")),
                new SidebarConfig.NavEntry("\uf279", "Mapa",           () -> navigate("Mapa")),
                new SidebarConfig.NavEntry("\uf500", "Vecinos",        () -> navigate("Vecinos")),
                new SidebarConfig.NavEntry("\uf46d", "Mis Alertas",    () -> navigate("Mis Alertas")),
                new SidebarConfig.NavEntry("\uf1f6", "Notificaciones", () -> navigate("Notificaciones")),
                new SidebarConfig.NavEntry("\uf007", "Mi Cuenta",      () -> navigate("Mi Cuenta"))
        );

        SidebarConfig sidebarCfg = new SidebarConfig(
                "Sistema de Alertas",
                nombre, barrio, entries,
                this::cerrarSesion
        );

        root.setLeft(new SidebarBuilder().build(sidebarCfg, getClass()));
        root.setCenter(buildMainContent());

        // ── Botón flotante chatbot ────────────────────────────────
        ChatBotView chatbotView = new ChatBotView(usuarioActual);
        Popup chatPopup = chatbotView.buildPopup(stage);

        Button chatBtn = new Button();
        Label chatIco = new Label("\uf544");
        chatIco.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';-fx-font-size:22px;-fx-text-fill:white;");
        chatBtn.setGraphic(chatIco);

        String chatBase  = "-fx-background-color:#1f3a56;-fx-text-fill:#F5F7FA;-fx-background-radius:50%;"
                + "-fx-min-width:56px;-fx-min-height:56px;-fx-max-width:56px;-fx-max-height:56px;"
                + "-fx-cursor:hand;-fx-effect:dropshadow(gaussian,rgba(31,58,86,0.55),18,0,0,5);";
        String chatHover = "-fx-background-color:#16283d;-fx-text-fill:#F5F7FA;-fx-background-radius:50%;"
                + "-fx-min-width:56px;-fx-min-height:56px;-fx-max-width:56px;-fx-max-height:56px;"
                + "-fx-cursor:hand;-fx-effect:dropshadow(gaussian,rgba(22,40,61,0.70),22,0,0,7);";
        chatBtn.setStyle(chatBase);
        chatBtn.setOnMouseEntered(e -> chatBtn.setStyle(chatHover));
        chatBtn.setOnMouseExited(e  -> chatBtn.setStyle(chatBase));
        chatBtn.setOnAction(e -> {
            if (chatPopup.isShowing()) chatPopup.hide();
            else {
                javafx.geometry.Bounds b = chatBtn.localToScreen(chatBtn.getBoundsInLocal());
                chatPopup.show(stage, b.getMaxX() - 370, b.getMinY() - 530);
            }
        });

        StackPane overlay = new StackPane(root, chatBtn);
        StackPane.setAlignment(chatBtn, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(chatBtn, new Insets(0, 24, 24, 0));

        Screen screen = Screen.getPrimary();
        Scene scene = new Scene(overlay,
                screen.getVisualBounds().getWidth()  * 0.85,
                screen.getVisualBounds().getHeight() * 0.85);
        stage.setTitle("WolertApp – Sistema de Alertas Comunitarias");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMinWidth(900);
        stage.setMinHeight(580);
        stage.show();
    }

    // ── Navegación ────────────────────────────────────────────────
    private void navigate(String label) {
        javafx.scene.Node view = navRegistry.navigate(label);
        if (view != null && view != root.getCenter()) root.setCenter(view);
    }

    // ── Submenú mapa (específico de UsuarioApp) ───────────────────
    private void toggleMapaSubMenu() {
        // navContainer se establece desde SidebarBuilder; usamos la referencia
        // indirecta a través del sidebar ya construido.
        // Alternativa: exponer el VBox del nav en SidebarBuilder si se prefiere.
        // Aquí se maneja con la lógica mínima necesaria.
        if (!mapaExpandido) {
            mapaSubMenu = buildMapaSubMenu();
            // Inserción en el nav se gestiona externamente si se expone el nav;
            // por simplicidad, aquí navegamos directo al mapa de alertas.
            root.setCenter(new MapaAlertas(alertaService).build());
        }
        mapaExpandido = !mapaExpandido;
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
        Label ico = new Label(icon);
        ico.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';"
                + "-fx-font-size:13px;-fx-text-fill:" + SIDEBAR_HOVER + ";");
        Label txt = new Label(text);
        txt.setTextFill(Color.web("#cbd5e1")); txt.setFont(Font.font(12));
        item.getChildren().addAll(ico, txt);
        item.setOnMouseEntered(e -> item.setStyle("-fx-background-color:#ffffff15;-fx-background-radius:6;"));
        item.setOnMouseExited(e  -> item.setStyle("-fx-background-color:transparent;"));
        item.setOnMouseClicked(e -> {
            switch (text) {
                case "Mapa de alertas"      -> root.setCenter(new MapaAlertas(alertaService).build());
                case "Zonas peligrosas"     -> root.setCenter(new MapaZonasPeligrosas(alertaService).build());
                case "Alertas comunitarias" -> root.setCenter(new MapaAlarmasRegistradas().build());
                default                     -> root.setCenter(buildPlaceholder(text));
            }
        });
        return item;
    }

    // =========================================================================
    // DASHBOARD PRINCIPAL
    // =========================================================================
    private ScrollPane buildMainContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color:" + BG_USER + ";");
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
        scroll.setStyle("-fx-background-color:" + BG_USER + ";-fx-background:" + BG_USER + ";");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        return scroll;
    }

    // ── Top bar con campana de notificaciones ─────────────────────
    private HBox buildTopBar() {
        List<Notificacion> misNotifs = List.of();
        if (notificacionService != null && usuarioActual != null) {
            try {
                misNotifs = notificacionService.listar().stream()
                        .filter(n -> usuarioActual.getCorreo() != null
                                && usuarioActual.getCorreo().equalsIgnoreCase(n.getCorreodestinatario()))
                        .sorted((a, b) -> b.getFechahora() != null && a.getFechahora() != null
                                ? b.getFechahora().compareTo(a.getFechahora()) : 0)
                        .collect(Collectors.toList());
            } catch (Exception ignored) {}
        }

        final List<Notificacion> notifsFinal = misNotifs;
        List<NotificationBellBuilder.PopupRow> rows = notifsFinal.stream().limit(5).map(n -> {
            String estado = n.getEstado() != null ? n.getEstado().name() : "";
            String color  = switch (estado) {
                case "LEIDA" -> GREEN; case "ENVIADA" -> BLUE;
                case "ERROR" -> RED;   default        -> ORANGE;
            };
            String msg   = n.getMensaje() != null
                    ? (n.getMensaje().length() > 42 ? n.getMensaje().substring(0, 42) + "…" : n.getMensaje())
                    : "Sin mensaje";
            String fecha = n.getFechahora() != null
                    ? n.getFechahora().format(DateTimeFormatter.ofPattern("dd/MM HH:mm")) : "";
            return new NotificationBellBuilder.PopupRow(
                    color, msg, fecha + "  ·  " + estado,
                    () -> root.setCenter(new NotificacionesView(usuarioActual, notificacionService, root).getView())
            );
        }).collect(Collectors.toList());

        List<NotificationBellBuilder.PopupSection> sections = List.of(
                new NotificationBellBuilder.PopupSection("\uf1f6", BLUE, "Mis notificaciones", rows)
        );
        List<Pair<String, Runnable>> footer = List.of(
                new Pair<>("Ver todas las notificaciones",
                        () -> root.setCenter(new NotificacionesView(usuarioActual, notificacionService, root).getView()))
        );

        javafx.scene.layout.StackPane bell =
                NotificationBellBuilder.build(notifsFinal.size(), sections, footer);

        String saludo = usuarioActual != null
                ? "¡Hola, " + usuarioActual.getPrimer_nombre() + "!" : "¡Hola!";
        return TopBarBuilder.build(
                saludo,
                "Tu seguridad es importante. Estamos aquí para ayudarte.",
                bell);
    }

    // ── Banner de emergencia ──────────────────────────────────────
    private HBox buildEmergencyBanner() {
        HBox banner = new HBox();
        banner.setAlignment(Pos.CENTER_LEFT);
        banner.setPadding(new Insets(18, 24, 18, 24));
        banner.setStyle("-fx-background-color:white;-fx-background-radius:16;"
                + "-fx-border-color:#fecaca;-fx-border-radius:16;-fx-border-width:1.5;");
        shadowMd(banner);

        StackPane iconBox = new StackPane();
        Region iconBg = new Region();
        iconBg.setPrefSize(56, 56);
        iconBg.setStyle("-fx-background-color:" + RED + ";-fx-background-radius:50%;");
        Label bellLbl = new Label("\uf0f3");
        bellLbl.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';-fx-font-size:22px;-fx-text-fill:white;");
        iconBox.getChildren().addAll(iconBg, bellLbl);

        ScaleTransition pulse = new ScaleTransition(Duration.seconds(1.1), iconBox);
        pulse.setFromX(1.0); pulse.setFromY(1.0);
        pulse.setToX(1.13);  pulse.setToY(1.13);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(javafx.animation.Animation.INDEFINITE);
        pulse.play();

        VBox textBox = new VBox(4);
        textBox.setPadding(new Insets(0, 0, 0, 16));
        Label tit = new Label("Botón de emergencia");
        tit.setStyle("-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:" + RED + ";");
        Label sub = new Label("Envía alerta inmediata a vecinos y autoridades");
        sub.setStyle("-fx-font-size:12px;-fx-text-fill:" + GRAY_TEXT + ";");
        textBox.getChildren().addAll(tit, sub);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        Button panicBtn = new Button("⚠  PÁNICO");
        String base  = "-fx-background-color:" + RED + ";-fx-text-fill:white;-fx-font-size:15px;"
                + "-fx-font-weight:bold;-fx-background-radius:30;-fx-padding:13 32;-fx-cursor:hand;"
                + "-fx-effect:dropshadow(gaussian,rgba(229,57,53,0.42),18,0,0,5);";
        String hover = "-fx-background-color:#c62828;-fx-text-fill:white;-fx-font-size:15px;"
                + "-fx-font-weight:bold;-fx-background-radius:30;-fx-padding:13 32;-fx-cursor:hand;"
                + "-fx-effect:dropshadow(gaussian,rgba(198,40,40,0.58),22,0,0,7);";
        panicBtn.setStyle(base);
        panicBtn.setOnMouseEntered(e -> panicBtn.setStyle(hover));
        panicBtn.setOnMouseExited(e  -> panicBtn.setStyle(base));
        panicBtn.setOnAction(e -> {
            Stage stage = (Stage) panicBtn.getScene().getWindow();
            new MapaAlerta(stage, usuarioActual, alertaService, barrioService).mostrar();
        });

        banner.getChildren().addAll(iconBox, textBox, panicBtn);
        return banner;
    }

    // ── Tarjetas de estadísticas (carga asíncrona) ────────────────
    private HBox buildStatCards() {
        HBox row = new HBox(16);

        VBox cardInc  = statCard("#fff0f0", RED,    "Incidentes este mes",    0, "+3 vs mes anterior");
        VBox cardPend = statCard("#fff8e1", ORANGE, "Alertas pendientes",     0, "PENDIENTE / EN PROCESO");
        VBox cardVec  = statCard("#e8f5e9", GREEN,  "Vecinos del barrio",     0, "Reportaron en tu barrio");
        row.getChildren().addAll(cardInc, cardPend, cardVec);

        new Thread(() -> {
            long inc  = statsService.getIncidentesMes();
            long pend = statsService.getAlertasActivas();
            long vec  = statsService.getVecinosActivos();
            Platform.runLater(() -> {
                updateCardValue(cardInc,  inc,  RED);
                updateCardValue(cardPend, pend, ORANGE);
                updateCardValue(cardVec,  vec,  GREEN);
            });
        }, "stats-loader").start();

        return row;
    }

    private void updateCardValue(VBox card, long value, String color) {
        try {
            HBox topRow   = (HBox) card.getChildren().get(0);
            VBox infoBox  = (VBox) topRow.getChildren().get(1);
            Label valLbl  = (Label) infoBox.getChildren().get(1);
            valLbl.setText(String.valueOf(value));
        } catch (Exception ignored) {}
    }

    private VBox statCard(String bgIcon, String accent, String title, long value, String sub) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20, 22, 20, 22));
        card.setStyle("-fx-background-color:white;-fx-background-radius:18;");
        HBox.setHgrow(card, Priority.ALWAYS);
        shadowMd(card);

        String faIcon = accent.equals(RED) ? "\uf071"
                : accent.equals(ORANGE)    ? "\uf0f3" : "\uf500";

        StackPane iconWrap = new StackPane();
        iconWrap.setPrefSize(58, 58); iconWrap.setMinSize(58, 58); iconWrap.setMaxSize(58, 58);
        Region colorBg = new Region();
        colorBg.setPrefSize(58, 58);
        colorBg.setStyle("-fx-background-color:" + bgIcon + ";-fx-background-radius:16;");
        Label ico = new Label(faIcon);
        ico.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';"
                + "-fx-font-size:26px;-fx-text-fill:" + accent + ";");
        iconWrap.getChildren().addAll(colorBg, ico);

        Label valLbl   = new Label(String.valueOf(value));
        valLbl.setStyle("-fx-font-size:36px;-fx-font-weight:bold;-fx-text-fill:" + accent + ";");
        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#374151;");
        Label subLbl   = new Label(sub);
        subLbl.setStyle("-fx-font-size:11px;-fx-text-fill:" + GRAY_TEXT + ";");

        HBox topRow = new HBox(16, iconWrap, new VBox(3, titleLbl, valLbl, subLbl));
        topRow.setAlignment(Pos.CENTER_LEFT);
        card.getChildren().add(topRow);
        card.setOnMouseEntered(e -> card.setTranslateY(-3));
        card.setOnMouseExited(e  -> card.setTranslateY(0));
        return card;
    }

    // ── Fila inferior: lista de alertas + panel de mapa ──────────
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
        shadowMd(card);

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(32, 32); iconBox.setMinSize(32, 32); iconBox.setMaxSize(32, 32);
        iconBox.setStyle("-fx-background-color:#e8f0fe;-fx-background-radius:8;");
        Label ico = new Label("\uf0f3");
        ico.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';-fx-font-size:16px;-fx-text-fill:" + BLUE + ";");
        iconBox.getChildren().add(ico);

        HBox titleRow = new HBox(8, iconBox, label("Alertas recientes", 15, TEXT_PRIMARY, true));
        titleRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(titleRow, Priority.ALWAYS);

        Label verTodas = label("Ver todas  >", 12, BLUE, false);
        verTodas.setCursor(javafx.scene.Cursor.HAND);
        verTodas.setOnMouseClicked(e -> root.setCenter(
                new AlertasView(usuarioActual, alertaService, barrioService).getView()));

        header.getChildren().addAll(titleRow, verTodas);
        card.getChildren().addAll(header, separator());

        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setPrefSize(32, 32);
        spinner.setStyle("-fx-progress-color:" + BLUE + ";");
        HBox spinnerBox = new HBox(spinner);
        spinnerBox.setAlignment(Pos.CENTER); spinnerBox.setPadding(new Insets(16));
        card.getChildren().add(spinnerBox);

        new Thread(() -> {
            List<Alerta> alertas = new ArrayList<>();
            String errorMsg = null;
            try { if (alertaService != null) alertas = alertaService.listar(); }
            catch (Exception e) { errorMsg = "Error al cargar alertas"; }

            final List<Alerta> result = alertas;
            final String err = errorMsg;
            Platform.runLater(() -> {
                card.getChildren().remove(spinnerBox);
                if (err != null) {
                    card.getChildren().add(label(err, 13, RED, false));
                } else if (result.isEmpty()) {
                    card.getChildren().add(label("No hay alertas registradas", 13, GRAY_TEXT, false));
                } else {
                    result.stream().limit(4).forEach(a -> card.getChildren().addAll(alertaItem(a), separator()));
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
        row.setOnMouseExited(e  -> row.setStyle("-fx-background-color:transparent;-fx-background-radius:8;"));

        String estado    = a.getEstado() != null ? a.getEstado().name() : "—";
        String dotColor  = AlertaColorResolver.getColor(a.getEstado());   // ← reutiliza resolver
        String tipo      = a.getTipoalerta() != null ? a.getTipoalerta().getNombre().toUpperCase() : "";
        String circleBg  = AlertaColorResolver.getCircleBg(tipo);         // ← reutiliza resolver
        String faIcon    = AlertaColorResolver.getFaIcon(tipo);            // ← reutiliza resolver

        Circle dot = new Circle(5, Color.web(dotColor));
        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(40, 40); iconBox.setMinSize(40, 40); iconBox.setMaxSize(40, 40);
        Region circleBgR = new Region();
        circleBgR.setPrefSize(40, 40);
        circleBgR.setStyle("-fx-background-color:" + circleBg + ";-fx-background-radius:50%;");
        Label faLbl = new Label(faIcon);
        faLbl.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';"
                + "-fx-font-size:16px;-fx-text-fill:" + dotColor + ";");
        iconBox.getChildren().addAll(circleBgR, faLbl);

        VBox text = new VBox(3);
        HBox.setHgrow(text, Priority.ALWAYS);

        String tipoNombre = a.getTipoalerta() != null ? a.getTipoalerta().getNombre() : "Alerta";
        String barrio     = a.getBarrio() != null ? " en " + a.getBarrio().getNombre() : "";
        String tiempo     = calcularTiempo(a.getFechaHora());
        String desc       = a.getDescripcion() != null && a.getDescripcion().length() > 50
                ? a.getDescripcion().substring(0, 50) + "…" : a.getDescripcion();
        String reportado  = buildReportadoPor(a);

        String titleColor = switch (estado) {
            case "PENDIENTE"  -> RED;    case "EN_ATENCION" -> ORANGE;
            case "RESUELTA"   -> GREEN;  case "RECIBIDA"    -> BLUE;
            default           -> TEXT_PRIMARY;
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

    private String calcularTiempo(LocalDateTime fechaHora) {
        if (fechaHora == null) return "Hace un momento";
        long mins = java.time.Duration.between(fechaHora, LocalDateTime.now()).toMinutes();
        if (mins < 60)   return "Hace " + mins + " min";
        if (mins < 1440) return "Hace " + (mins / 60) + " hora" + (mins / 60 > 1 ? "s" : "");
        return "Hace " + (mins / 1440) + " día" + (mins / 1440 > 1 ? "s" : "");
    }

    private String buildReportadoPor(Alerta a) {
        if (a.getUsuario() == null) return "";
        String nombre   = a.getUsuario().getPrimer_nombre() != null  ? a.getUsuario().getPrimer_nombre()  : "";
        String apellido = a.getUsuario().getPrimer_apellido() != null
                ? " " + a.getUsuario().getPrimer_apellido().charAt(0) + "." : "";
        return (!nombre.isBlank() || !apellido.isBlank()) ? " · Reportado por " + nombre + apellido : "";
    }

    private VBox buildMapPanel() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.setPrefWidth(380);
        card.setStyle("-fx-background-color:" + WHITE + ";-fx-background-radius:12;");
        shadowMd(card);

        String barrioDisplay = usuarioActual != null
                && usuarioActual.getDireccion() != null
                && usuarioActual.getDireccion().getBarrio() != null
                ? usuarioActual.getDireccion().getBarrio().getNombre() : "Tu barrio";

        StackPane pinBox = new StackPane();
        pinBox.setPrefSize(32, 32); pinBox.setMinSize(32, 32); pinBox.setMaxSize(32, 32);
        pinBox.setStyle("-fx-background-color:#e8f0fe;-fx-background-radius:50%;");
        Label pinLbl = new Label("\uf3c5");
        pinLbl.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';-fx-font-size:15px;-fx-text-fill:" + BLUE + ";");
        pinBox.getChildren().add(pinLbl);

        Label abrirLbl = label("Expandir  ↗", 11, BLUE, false);
        abrirLbl.setCursor(javafx.scene.Cursor.HAND);
        abrirLbl.setOnMouseClicked(e -> root.setCenter(new MapaAlertas(alertaService).build()));

        Region spacerH = new Region();
        HBox.setHgrow(spacerH, Priority.ALWAYS);
        HBox header = new HBox(8, pinBox, label("Alertas en " + barrioDisplay, 14, TEXT_PRIMARY, true), spacerH, abrirLbl);

        // Mapa mini (SwingNode)
        javafx.scene.layout.StackPane mapaContainer = new javafx.scene.layout.StackPane();
        mapaContainer.setPrefHeight(210); mapaContainer.setMinHeight(210); mapaContainer.setMaxHeight(210);
        mapaContainer.setStyle("-fx-background-radius:10;-fx-background-color:#e8f0fe;");
        VBox.setVgrow(mapaContainer, Priority.NEVER);

        ProgressIndicator mapaSpinner = new ProgressIndicator();
        mapaSpinner.setPrefSize(36, 36);
        mapaSpinner.setStyle("-fx-progress-color:" + BLUE + ";");
        mapaContainer.getChildren().add(mapaSpinner);

        javax.swing.SwingUtilities.invokeLater(() -> {
            org.jxmapviewer.JXMapViewer miniMapa = new org.jxmapviewer.JXMapViewer();
            org.jxmapviewer.viewer.TileFactoryInfo info = new org.jxmapviewer.viewer.TileFactoryInfo(
                    1, 15, 17, 256, true, true,
                    "https://tile.openstreetmap.org", "x", "y", "z") {
                @Override public String getTileUrl(int x, int y, int zoom) {
                    return baseURL + "/" + (17 - zoom) + "/" + x + "/" + y + ".png";
                }
            };
            miniMapa.setTileFactory(new org.jxmapviewer.viewer.DefaultTileFactory(info));
            miniMapa.setAddressLocation(new org.jxmapviewer.viewer.GeoPosition(10.4795, -73.2536));
            miniMapa.setZoom(5);
            org.jxmapviewer.input.PanMouseInputListener pan =
                    new org.jxmapviewer.input.PanMouseInputListener(miniMapa);
            miniMapa.addMouseListener(pan); miniMapa.addMouseMotionListener(pan);
            miniMapa.addMouseWheelListener(new org.jxmapviewer.input.ZoomMouseWheelListenerCenter(miniMapa));

            miniMapa.setOverlayPainter((g, map, w, h) -> {
                g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                try {
                    if (alertaService == null) return;
                    String miBarrio = (usuarioActual != null
                            && usuarioActual.getDireccion() != null
                            && usuarioActual.getDireccion().getBarrio() != null)
                            ? usuarioActual.getDireccion().getBarrio().getNombre() : null;

                    alertaService.listar().stream()
                            .filter(a -> miBarrio == null || (a.getBarrio() != null
                                    && miBarrio.equalsIgnoreCase(a.getBarrio().getNombre())))
                            .filter(a -> a.getLatitud() != 0 || a.getLongitud() != 0)
                            .forEach(a -> {
                                java.awt.geom.Point2D pt = map.getTileFactory().geoToPixel(
                                        new org.jxmapviewer.viewer.GeoPosition(a.getLatitud(), a.getLongitud()),
                                        map.getZoom());
                                int cx = (int) pt.getX() - map.getViewportBounds().x;
                                int cy = (int) pt.getY() - map.getViewportBounds().y;
                                String est = a.getEstado() != null ? a.getEstado().name() : "";
                                java.awt.Color col = switch (est) {
                                    case "PENDIENTE"  -> new java.awt.Color(229, 57, 53);
                                    case "EN_ATENCION"-> new java.awt.Color(251, 140, 0);
                                    case "RESUELTA"   -> new java.awt.Color(67, 160, 71);
                                    default           -> new java.awt.Color(107, 114, 128);
                                };
                                int r = 7;
                                g.setColor(col);
                                g.fillOval(cx - r, cy - r, r * 2, r * 2);
                                g.setColor(java.awt.Color.WHITE);
                                g.setStroke(new java.awt.BasicStroke(1.5f));
                                g.drawOval(cx - r, cy - r, r * 2, r * 2);
                            });
                } catch (Exception ignored) {}
            });

            javafx.embed.swing.SwingNode swingNode = new javafx.embed.swing.SwingNode();
            swingNode.setContent(miniMapa);
            swingNode.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2) root.setCenter(new MapaAlertas(alertaService).build());
            });
            Platform.runLater(() -> mapaContainer.getChildren().setAll(swingNode));
        });

        HBox legend = new HBox(16);
        legend.setAlignment(Pos.CENTER);
        legend.getChildren().addAll(
                legendItem(RED,    "Pendiente"),
                legendItem(ORANGE, "En revisión"),
                legendItem(GREEN,  "Resuelto"));

        Label hint = label("Doble clic para abrir mapa completo", 10, GRAY_TEXT, false);
        hint.setAlignment(Pos.CENTER);

        card.getChildren().addAll(header, mapaContainer, legend, hint);
        return card;
    }

    // ── Footer ────────────────────────────────────────────────────
    private HBox buildFooter() {
        HBox footer = new HBox();
        footer.setPadding(new Insets(30, 50, 30, 50));
        footer.setPrefHeight(120);
        footer.setStyle("-fx-background-color:" + BLUE_LIGHT + ";-fx-background-radius:10;");

        StackPane lockBox = iconCircle("\uf023", "#dbeafe");
        VBox leftText = new VBox(2,
                label("Tu información está protegida", 16, BLUE, true),
                label("Todas tus alertas son anónimas y confidenciales.", 13, GRAY_TEXT, false));
        HBox left = new HBox(10, lockBox, leftText);
        left.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(left, Priority.ALWAYS);

        StackPane shieldBox = iconCircle("\uf132", "#dbeafe");
        VBox rightText = new VBox(2,
                label("100% Anónimo", 16, BLUE, true),
                label("Tu identidad no será revelada.", 13, GRAY_TEXT, false));
        rightText.setAlignment(Pos.CENTER_RIGHT);
        HBox right = new HBox(10, rightText, shieldBox);
        right.setAlignment(Pos.CENTER_RIGHT);

        footer.getChildren().addAll(left, right);
        return footer;
    }

    private StackPane iconCircle(String faIcon, String bgColor) {
        StackPane box = new StackPane();
        box.setPrefSize(50, 50); box.setMinSize(50, 50); box.setMaxSize(50, 50);
        box.setStyle("-fx-background-color:" + bgColor + ";-fx-background-radius:50%;");
        Label lbl = new Label(faIcon);
        lbl.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';-fx-font-size:25px;-fx-text-fill:" + BLUE + ";");
        box.getChildren().add(lbl);
        return box;
    }

    // ── Placeholder ───────────────────────────────────────────────
    private VBox buildPlaceholder(String texto) {
        VBox box = new VBox(16);
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color:" + BG_USER + ";");
        box.setPadding(new Insets(60));
        Label icon = new Label("🚧"); icon.setFont(Font.font(64));
        Label title = new Label(texto);
        title.setFont(Font.font("System", FontWeight.BOLD, 24));
        title.setTextFill(Color.web(TEXT_PRIMARY));
        Label sub = new Label("Esta sección estará disponible próximamente.");
        sub.setFont(Font.font(14)); sub.setTextFill(Color.web(GRAY_TEXT));
        box.getChildren().addAll(icon, title, sub);
        return box;
    }

    // ── Helpers ───────────────────────────────────────────────────
    private HBox legendItem(String color, String text) {
        HBox item = new HBox(6);
        item.setAlignment(Pos.CENTER_LEFT);
        item.getChildren().addAll(new Circle(5, Color.web(color)), label(text, 11, GRAY_TEXT, false));
        return item;
    }

    private void cerrarSesion() {
        Stage stage = (Stage) root.getScene().getWindow();
        stage.close();
    }

    private void showError(String svc, Exception e) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error");
        a.setHeaderText(null);
        a.setContentText(svc + ": " + e.getMessage());
        a.showAndWait();
    }
}