/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.view;

/**
 *
 * @author Maria Cristina
 */


import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import sistemagestion.model.Alerta;
import sistemagestion.model.EstadoAlerta;
import sistemagestion.model.Usuario;
import sistemagestion.service.AdminDashboardStatsService;
import sistemagestion.service.AlertaService;
import sistemagestion.service.NotificacionService;
import sistemagestion.service.UsuarioService;
import static sistemagestion.view.AppColors.BG;
import static sistemagestion.view.AppColors.BLUE;
import static sistemagestion.view.AppColors.GRAY_TEXT;
import static sistemagestion.view.AppColors.RED_LIGHT;
import static sistemagestion.view.AppColors.ORANGE;
import static sistemagestion.view.AppColors.GREEN;
import static sistemagestion.view.AppColors.RED;
import static sistemagestion.view.AppColors.TEXT_PRIMARY;
import static sistemagestion.view.AppDimensions.ICON_BOX_SM;
import static sistemagestion.view.AppDimensions.MAX_ALERTAS_PANEL;
import static sistemagestion.view.UIFactory.label;
import static sistemagestion.view.UIFactory.separator;
import static sistemagestion.view.UIFactory.shadow;




/**
 * Vista principal del administrador.
 *
 * Principio SOLID:
 *  SRP — solo orquesta el dashboard admin; delega construcción a builders.
 *  DIP — depende de IDashboardStatsService, no de implementaciones concretas.
 *  OCP — la navegación se extiende en NavigationRegistry sin tocar este archivo.
 *
 * Patrón GRASP:
 *  Controller — coordina interacción entre servicios y vistas.
 *  Creator — crea sus dependencias y las inyecta en el registry.
 */
public class AdministradorApp {

    // ── Dependencias ──────────────────────────────────────────────
    private final Usuario                  usuarioLogueado;
    private final AlertaService            alertaService;
    private final UsuarioService           usuarioService;
    private final NotificacionService      notificacionService;
    private final AdminDashboardStatsService statsService;

    // ── UI principal ──────────────────────────────────────────────
    private BorderPane root;

    // ── Registro de navegación (OCP) ──────────────────────────────
    private NavigationRegistry navRegistry;

    public AdministradorApp(Usuario usuario) {
        this.usuarioLogueado = usuario;
        AlertaService         tmpAlerta = null;
        UsuarioService        tmpUsuario = null;
        NotificacionService   tmpNotif  = null;

        try { tmpAlerta  = new AlertaService();       } catch (Exception e) { showError(e); }
        try { tmpUsuario = new UsuarioService();       } catch (Exception e) { showError(e); }
        try { tmpNotif   = new NotificacionService(); } catch (Exception e) { showError(e); }

        this.alertaService       = tmpAlerta;
        this.usuarioService      = tmpUsuario;
        this.notificacionService = tmpNotif;
        this.statsService        = new AdminDashboardStatsService(alertaService, usuarioService);
    }

    // ── Constructor con inyección de dependencias (DIP) ───────────
    public AdministradorApp(Usuario usuario,
                             AlertaService alertaService,
                             UsuarioService usuarioService,
                             NotificacionService notificacionService) {
        this.usuarioLogueado    = usuario;
        this.alertaService      = alertaService;
        this.usuarioService     = usuarioService;
        this.notificacionService = notificacionService;
        this.statsService       = new AdminDashboardStatsService(alertaService, usuarioService);
    }

    public void show(Stage stage) {
        Font.loadFont(getClass().getResourceAsStream("/fa-solid-900.ttf"), 20);

        root = new BorderPane();
        root.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);

        // ── Registro de rutas (OCP: agregar sección = 1 línea aquí) ──
        navRegistry = new NavigationRegistry()
                .register("Dashboard",      this::buildMainContent)
                .register("Usuarios",       () -> new UsuariosAdminView().getView())
                .register("Alertas",        () -> new AlertasAdminView(alertaService).getView())
                .register("Alarmas",        () -> { new MapaAlarmasRegistradas().mostrar(); return root.getCenter(); })
                .register("Comunas",        () -> new ComunaAdminView().getView())
                .register("Barrios",        () -> new BarrioAdminView().getView())
                .register("Tipos",          () -> new TiposAdminView().getView())
                .register("Notificaciones", () -> new NotificacionesAdminView(notificacionService).getView())
                .register("Reportes",       this::buildReportesView)
                .register("Estadísticas",   () -> new EstadisticasAdminView().getView())
                .register("Configuración",  () -> new ConfiguracionAdminView(usuarioLogueado).getView());

        // ── Sidebar (reutilizando SidebarBuilder) ─────────────────
        List<SidebarConfig.NavEntry> entries = List.of(
                new SidebarConfig.NavEntry("\uf3fd", "Dashboard",      () -> navigate("Dashboard")),
                new SidebarConfig.NavEntry("\uf0c0", "Usuarios",       () -> navigate("Usuarios")),
                new SidebarConfig.NavEntry("\uf0f3", "Alertas",        () -> navigate("Alertas")),
                new SidebarConfig.NavEntry("\uf0a1", "Alarmas",        () -> navigate("Alarmas")),
                new SidebarConfig.NavEntry("\uf041", "Comunas",        () -> navigate("Comunas")),
                new SidebarConfig.NavEntry("\uf015", "Barrios",        () -> navigate("Barrios")),
                new SidebarConfig.NavEntry("\uf0cb", "Tipos",          () -> navigate("Tipos")),
                new SidebarConfig.NavEntry("\uf1f6", "Notificaciones", () -> navigate("Notificaciones")),
                new SidebarConfig.NavEntry("\uf080", "Reportes",       () -> navigate("Reportes")),
                new SidebarConfig.NavEntry("\uf201", "Estadísticas",   () -> navigate("Estadísticas")),
                new SidebarConfig.NavEntry("\uf013", "Configuración",  () -> navigate("Configuración"))
        );

        SidebarConfig sidebarConfig = new SidebarConfig(
                "Panel Administrativo",
                "Administrador",
                "Sistema activo",
                entries,
                () -> ((Stage) root.getScene().getWindow()).close()
        );

        root.setLeft(new SidebarBuilder().build(sidebarConfig, getClass()));
        root.setCenter(buildMainContent());

        Screen screen = Screen.getPrimary();
        Scene scene = new Scene(root,
                screen.getVisualBounds().getWidth()  * 0.85,
                screen.getVisualBounds().getHeight() * 0.85);
        stage.setTitle("WolertApp – Panel Administrativo");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMinWidth(900);
        stage.setMinHeight(580);
        stage.show();
    }

    public void start(Stage stage) { show(stage); }

    // ── Navegación centralizada (Controller) ─────────────────────
    private void navigate(String label) {
        Node view = navRegistry.navigate(label);
        if (view != null) root.setCenter(view);
    }

    // ═══════════════════════════════════════════════════════════════
    // DASHBOARD PRINCIPAL
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
        scroll.setStyle("-fx-background-color:" + BG + ";-fx-background:" + BG + ";");
        return scroll;
    }

    // ── Top bar con campana ───────────────────────────────────────
    private HBox buildTopBar() {
        // Alertas activas para el popup
        List<Alerta> alertasActivas = statsService.cargarAlertas().stream()
                .filter(a -> a.getEstado() == EstadoAlerta.PENDIENTE
                        || a.getEstado() == EstadoAlerta.RECIBIDA
                        || a.getEstado() == EstadoAlerta.EN_ATENCION
                        || a.getEstado() == EstadoAlerta.UNIDAD_ASIGNADA)
                .sorted((a, b) -> b.getFechaHora() != null && a.getFechaHora() != null
                        ? b.getFechaHora().compareTo(a.getFechaHora()) : 0)
                .limit(MAX_ALERTAS_PANEL)
                .collect(Collectors.toList());

        // Notificaciones para el popup
        List<sistemagestion.model.Notificacion> ultimasNotifs = List.of();
        if (notificacionService != null) {
            try {
                ultimasNotifs = notificacionService.listar().stream()
                        .sorted((a, b) -> b.getFechahora() != null && a.getFechahora() != null
                                ? b.getFechahora().compareTo(a.getFechahora()) : 0)
                        .limit(3).collect(Collectors.toList());
            } catch (Exception ignored) {}
        }

        // Construir secciones del popup (usando NotificationBellBuilder)
        List<NotificationBellBuilder.PopupRow> alertRows = alertasActivas.stream()
                .map(a -> new NotificationBellBuilder.PopupRow(
                        AlertaColorResolver.getColor(a.getEstado()),
                        (a.getTipoalerta() != null ? a.getTipoalerta().getNombre() : "Alerta")
                                + " — " + (a.getBarrio() != null ? a.getBarrio().getNombre() : "—"),
                        (a.getEstado() != null ? a.getEstado().name().replace("_", " ") : "—")
                                + "  ·  " + (a.getFechaHora() != null
                                ? a.getFechaHora().format(DateTimeFormatter.ofPattern("dd/MM HH:mm")) : "—"),
                        () -> root.setCenter(new AlertasAdminView(alertaService).getView())
                )).collect(Collectors.toList());

        final List<sistemagestion.model.Notificacion> notifsFinal = ultimasNotifs;
        List<NotificationBellBuilder.PopupRow> notifRows = notifsFinal.stream()
                .map(n -> {
                    String msg = n.getMensaje() != null
                            ? (n.getMensaje().length() > 40 ? n.getMensaje().substring(0, 40) + "…" : n.getMensaje())
                            : "—";
                    String fecha = n.getFechahora() != null
                            ? n.getFechahora().format(DateTimeFormatter.ofPattern("dd/MM HH:mm")) : "—";
                    return new NotificationBellBuilder.PopupRow(
                            BLUE, msg,
                            (n.getCorreodestinatario() != null ? n.getCorreodestinatario() : "—") + "  ·  " + fecha,
                            () -> root.setCenter(new NotificacionesAdminView(notificacionService).getView())
                    );
                }).collect(Collectors.toList());

        List<NotificationBellBuilder.PopupSection> sections = List.of(
                new NotificationBellBuilder.PopupSection("\uf0f3", RED,  "Alertas activas",             alertRows),
                new NotificationBellBuilder.PopupSection("\uf1f6", BLUE, "Notificaciones de usuarios",  notifRows)
        );

        List<Pair<String, Runnable>> footerActions = List.of(
                new Pair<>("Ver alertas",        () -> root.setCenter(new AlertasAdminView(alertaService).getView())),
                new Pair<>("Ver notificaciones",  () -> root.setCenter(new NotificacionesAdminView(notificacionService).getView()))
        );

        int badge = alertasActivas.size() + notifsFinal.size();
        javafx.scene.layout.StackPane bell = NotificationBellBuilder.build(badge, sections, footerActions);

        return TopBarBuilder.build(
                "Dashboard Administrativo",
                "Panel de control y monitoreo del sistema",
                bell);
    }

    // ── Stats ─────────────────────────────────────────────────────
    private HBox buildStats() {
        HBox row = new HBox(20);
        row.setFillHeight(true);
        HBox.setHgrow(row, Priority.ALWAYS);

        row.getChildren().addAll(
                statCard("#e8f0fe", BLUE,   "\uf0c0", "Usuarios registrados",
                        String.valueOf(statsService.getTotalUsuarios()), "Total en el sistema"),
                statCard(RED_LIGHT, RED,    "\uf0f3", "Alertas activas",
                        String.valueOf(statsService.getAlertasActivas()), "En este momento"),
                statCard("#fff8e1", ORANGE, "\uf201", "Incidentes",
                        String.valueOf(statsService.getIncidentes()), "Pendientes de atención"),
                statCard("#e8f5e9", GREEN,  "\uf058", "Resueltos",
                        String.valueOf(statsService.getAlertasResueltas()), "Total histórico")
        );
        return row;
    }

    // ── Paneles inferiores ────────────────────────────────────────
    private HBox buildBottomPanels() {
        HBox bottom = new HBox(20);
        HBox.setHgrow(bottom, Priority.ALWAYS);
        bottom.setFillHeight(true);
        bottom.getChildren().addAll(buildAlertasPanel(), buildActividadPanel());
        return bottom;
    }

    private VBox buildAlertasPanel() {
        VBox panel = new VBox(12);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color:white;-fx-background-radius:16;");
        HBox.setHgrow(panel, Priority.ALWAYS);
        shadow(panel);

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        HBox titleRow = new HBox(8);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        StackPane iconBox = iconBox(RED_LIGHT, "\uf0f3", RED, 32);
        titleRow.getChildren().addAll(iconBox, label("Alertas recientes", 15, TEXT_PRIMARY, true));
        HBox.setHgrow(titleRow, Priority.ALWAYS);

        Label verTodas = label("Ver todas  >", 12, BLUE, false);
        verTodas.setCursor(javafx.scene.Cursor.HAND);
        verTodas.setOnMouseClicked(e -> root.setCenter(new AlertasAdminView(alertaService).getView()));

        header.getChildren().addAll(titleRow, verTodas);
        panel.getChildren().addAll(header, separator());

        List<Alerta> ultimas = statsService.cargarAlertas().stream()
                .sorted((a, b) -> a.getFechaHora() != null && b.getFechaHora() != null
                        ? b.getFechaHora().compareTo(a.getFechaHora()) : 0)
                .limit(MAX_ALERTAS_PANEL).collect(Collectors.toList());

        if (ultimas.isEmpty()) {
            panel.getChildren().add(label("Sin alertas registradas.", 13, GRAY_TEXT, false));
        } else {
            boolean first = true;
            for (Alerta a : ultimas) {
                if (!first) panel.getChildren().add(separator());
                first = false;
                String titulo  = (a.getTipoalerta() != null ? a.getTipoalerta().getNombre() : "Alerta")
                        + " en " + (a.getBarrio() != null ? a.getBarrio().getNombre() : "—");
                String sub     = "Reportado por " + (a.getUsuario() != null ? a.getUsuario().getPrimer_nombre() : "—");
                String color   = AlertaColorResolver.getColor(a.getEstado());
                panel.getChildren().add(alertItem(AlertaColorResolver.getIcon(a), titulo, sub, color));
            }
        }
        return panel;
    }

    private VBox buildActividadPanel() {
        VBox panel = new VBox(12);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color:white;-fx-background-radius:16;");
        HBox.setHgrow(panel, Priority.ALWAYS);
        shadow(panel);

        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        StackPane iconBox = iconBox("#e8f0fe", "\uf017", BLUE, 32);
        header.getChildren().addAll(iconBox, label("Actividad reciente", 15, TEXT_PRIMARY, true));
        panel.getChildren().addAll(header, separator());

        List<Alerta> recientes = statsService.cargarAlertas().stream()
                .sorted((a, b) -> a.getFechaHora() != null && b.getFechaHora() != null
                        ? b.getFechaHora().compareTo(a.getFechaHora()) : 0)
                .limit(MAX_ALERTAS_PANEL).collect(Collectors.toList());

        if (recientes.isEmpty()) {
            panel.getChildren().add(label("Sin actividad reciente.", 13, GRAY_TEXT, false));
        } else {
            boolean first = true;
            for (Alerta a : recientes) {
                if (!first) panel.getChildren().add(separator());
                first = false;
                String tipo   = (a.getTipoalerta() != null ? a.getTipoalerta().getNombre() : "Alerta")
                        + " — " + (a.getBarrio() != null ? a.getBarrio().getNombre() : "—");
                String fecha  = a.getFechaHora() != null ? a.getFechaHora().toLocalDate().toString() : "—";
                panel.getChildren().add(listItem("\uf0f3", tipo, fecha, AlertaColorResolver.getColor(a.getEstado())));
            }
        }
        return panel;
    }

    // ── Vista de reportes con manejo de error ─────────────────────
    private Node buildReportesView() {
        try {
            return new ReportesAdminView(new UsuarioService(), new AlertaService()).build();
        } catch (Exception ex) {
            return buildPlaceholderView("Reportes — Error: " + ex.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // HELPERS UI LOCALES
    // ═══════════════════════════════════════════════════════════════
    private StackPane iconBox(String bg, String faIcon, String color, double size) {
        StackPane box = new StackPane();
        box.setPrefSize(size, size); box.setMinSize(size, size); box.setMaxSize(size, size);
        box.setStyle("-fx-background-color:" + bg + ";-fx-background-radius:8;");
        Label lbl = new Label(faIcon);
        lbl.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';"
                + "-fx-font-size:16px;-fx-text-fill:" + color + ";");
        box.getChildren().add(lbl);
        return box;
    }

    private HBox alertItem(String icon, String title, String sub, String dotColor) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 0, 8, 0));
        row.setCursor(javafx.scene.Cursor.HAND);

        Circle dot = new Circle(5, Color.web(dotColor));
        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(ICON_BOX_SM, ICON_BOX_SM);
        iconBox.setMinSize(ICON_BOX_SM, ICON_BOX_SM);
        iconBox.setMaxSize(ICON_BOX_SM, ICON_BOX_SM);
        iconBox.setStyle("-fx-background-color:" + BG + ";-fx-background-radius:8;");
        iconBox.getChildren().add(label(icon, 16, dotColor, false));

        VBox text = new VBox(2);
        HBox.setHgrow(text, Priority.ALWAYS);
        text.getChildren().addAll(label(title, 13, TEXT_PRIMARY, false), label(sub, 11, GRAY_TEXT, false));
        row.getChildren().addAll(dot, iconBox, text, label(">", 14, GRAY_TEXT, false));
        return row;
    }

    private HBox listItem(String faIcon, String title, String sub, String iconColor) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 0, 8, 0));

        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(ICON_BOX_SM, ICON_BOX_SM);
        iconBox.setMinSize(ICON_BOX_SM, ICON_BOX_SM);
        iconBox.setMaxSize(ICON_BOX_SM, ICON_BOX_SM);
        iconBox.setStyle("-fx-background-color:" + BG + ";-fx-background-radius:8;");
        Label ico = new Label(faIcon);
        ico.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';"
                + "-fx-font-size:16px;-fx-text-fill:" + iconColor + ";");
        iconBox.getChildren().add(ico);

        VBox text = new VBox(2);
        HBox.setHgrow(text, Priority.ALWAYS);
        text.getChildren().addAll(label(title, 13, TEXT_PRIMARY, false), label(sub, 11, GRAY_TEXT, false));
        row.getChildren().addAll(iconBox, text);
        return row;
    }

    private VBox statCard(String bgIcon, String accent, String faIcon,
                          String title, String value, String sub) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20, 22, 20, 22));
        card.setStyle("-fx-background-color:white;-fx-background-radius:18;");
        card.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(card, Priority.ALWAYS);
        shadow(card);

        StackPane iconWrap = new StackPane();
        iconWrap.setPrefSize(52, 52); iconWrap.setMinSize(52, 52); iconWrap.setMaxSize(52, 52);
        Rectangle iconBg = new Rectangle(52, 52);
        iconBg.setArcWidth(16); iconBg.setArcHeight(16);
        iconBg.setFill(Color.web(bgIcon));
        Label ico = new Label(faIcon);
        ico.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';"
                + "-fx-font-size:22px;-fx-text-fill:" + accent + ";");
        iconWrap.getChildren().addAll(iconBg, ico);

        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#374151;");
        Label valueLbl = new Label(value);
        valueLbl.setStyle("-fx-font-size:36px;-fx-font-weight:bold;-fx-text-fill:" + accent + ";");
        Label subLbl   = new Label(sub);
        subLbl.setStyle("-fx-font-size:11px;-fx-text-fill:" + GRAY_TEXT + ";");

        HBox top = new HBox(16);
        top.setAlignment(Pos.CENTER_LEFT);
        top.getChildren().addAll(iconWrap, new VBox(3, titleLbl, valueLbl, subLbl));
        card.getChildren().add(top);
        card.setOnMouseEntered(e -> card.setTranslateY(-3));
        card.setOnMouseExited(e  -> card.setTranslateY(0));
        return card;
    }

    private ScrollPane buildPlaceholderView(String nombre) {
        VBox box = new VBox(20);
        box.setPadding(new Insets(40));
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color:" + BG + ";");
        Label icon  = new Label("🚧"); icon.setFont(Font.font(70));
        Label title = new Label(nombre);
        title.setFont(Font.font("System", FontWeight.BOLD, 32));
        title.setTextFill(Color.web(TEXT_PRIMARY));
        Label msg = new Label("Pantalla en construcción");
        msg.setFont(Font.font(18)); msg.setTextFill(Color.GRAY);
        Button volver = new Button("Volver al Dashboard");
        volver.setStyle("-fx-background-color:" + BLUE + ";-fx-text-fill:white;"
                + "-fx-font-size:14px;-fx-background-radius:10;-fx-padding:10 20;-fx-cursor:hand;");
        volver.setOnAction(e -> root.setCenter(buildMainContent()));
        box.getChildren().addAll(icon, title, msg, volver);
        ScrollPane scroll = new ScrollPane(box);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background:" + BG + ";-fx-background-color:" + BG + ";");
        return scroll;
    }

    private void showError(Exception e) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error de conexión");
        a.setHeaderText(null);
        a.setContentText(e.getMessage());
        a.showAndWait();
    }
}