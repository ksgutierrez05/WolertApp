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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Screen;

import sistemagestion.model.*;
import sistemagestion.service.*;

/**
 * GRASP · Controller
 * Coordina la interacción del usuario con la UI.
 * No contiene lógica de negocio ni construcción de widgets;
 * delega esas responsabilidades a las clases internas especializadas.
 */
public class AdministradorPoliciaApp {

    // ── Paleta centralizada (Information Expert: quien la usa, la posee) ──────
    static final String WHITE        = "#ffffff";
    static final String BG           = "#f4f6fb";
    static final String RED          = "#e53935";
    static final String RED_LIGHT    = "#fff0f0";
    static final String ORANGE       = "#fb8c00";
    static final String ORANGE_LIGHT = "#fff3e0";
    static final String GREEN        = "#43a047";
    static final String GREEN_LIGHT  = "#e8f5e9";
    static final String BLUE         = "#1565c0";
    static final String BLUE_LIGHT   = "#e8f0fe";
    static final String GRAY_TEXT    = "#6b7280";
    static final String BORDER       = "#e5e7eb";
    static final String FA           = "'Font Awesome 6 Free Solid'";

    static final double SIDEBAR_EXPANDED  = 240;
    static final double SIDEBAR_COLLAPSED = 60;

    // ── Servicios (Creator: AdministradorPoliciaApp los instancia porque los usa) ──
    final AlertaService          alertaService;
    final PoliciaService         policiaService;
    final UnidadPolicialService  unidadService;
    final AsignacionUnidadService asignacionService;
    final AlarmaService          alarmaService;
    final NotificacionService    notificacionService;
    final AtencionAlertaService  atencionService;

    final Usuario usuarioActual;

    // ── Raíz de la escena (Controller necesita acceso para navegación) ────────
    BorderPane root;

    // ── Constructor ───────────────────────────────────────────────────────────
    public AdministradorPoliciaApp(Usuario usuarioActual) {
        this.usuarioActual = usuarioActual;
        Font.loadFont(getClass().getResourceAsStream("/fa-solid-900.ttf"), 20);

        AlertaService          as  = null;
        PoliciaService         ps  = null;
        UnidadPolicialService  us  = null;
        AsignacionUnidadService uas = null;
        AlarmaService          ams = null;
        NotificacionService    ns  = null;
        AtencionAlertaService  ats = null;

        try { as  = new AlertaService();          } catch (SQLException e) { log("AlertaService",  e); }
        try { ps  = new PoliciaService();          } catch (SQLException e) { log("PoliciaService", e); }
        try { us  = new UnidadPolicialService();   } catch (SQLException e) { log("UnidadService",  e); }
        try { uas = new AsignacionUnidadService(); } catch (SQLException e) { log("AsignService",   e); }
        try { ams = new AlarmaService();           } catch (SQLException e) { log("AlarmaService",  e); }
        try { ns  = new NotificacionService();     } catch (SQLException e) { log("NotiService",    e); }
        try { ats = new AtencionAlertaService();   } catch (SQLException e) { log("AtencionService",e); }

        alertaService     = as;
        policiaService    = ps;
        unidadService     = us;
        asignacionService = uas;
        alarmaService     = ams;
        notificacionService = ns;
        atencionService   = ats;
    }

    private void log(String nombre, Exception e) {
        System.out.println("Error " + nombre + ": " + e.getMessage());
    }

    // =========================================================================
    // GRASP · Controller — punto de entrada, orquesta el montaje de la UI
    // =========================================================================
    public void show(Stage stage) {
        root = new BorderPane();

        // Creator: cada builder recibe solo lo que necesita (Low Coupling)
        SidebarBuilder  sidebar  = new SidebarBuilder(this);
        DashboardBuilder dashboard = new DashboardBuilder(this);

        root.setLeft(sidebar.build());
        root.setCenter(dashboard.build());
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

    // ── Navegación central (Controller) ──────────────────────────────────────
    void navegar(String destino) {
        root.setCenter(switch (destino) {
            case "Centro de operaciones" -> new DashboardBuilder(this).build();
            case "Alertas"        -> new AlertasAdminPoliciaView(alertaService).build();
            case "Alarmas"        -> new AlarmasAdminPoliciaView(alarmaService).build();
            case "Asignaciones"   -> new AsignacionesAdminPoliciaView(asignacionService, unidadService, alarmaService).build();
            case "Historial"      -> new HistorialAdminPoliciaView(atencionService).build();
            case "Policías"       -> new PoliciasAdminPoliciaView(policiaService, unidadService).build();
            case "Unidades"       -> new UnidadesAdminPoliciaView(unidadService).build();
            case "Estadísticas"   -> new EstadisticasAdminPoliciaView(alertaService, asignacionService, unidadService, policiaService, alarmaService, notificacionService).build();
            case "Reportes"       -> new ReportesAdminPoliciaView(alertaService, asignacionService, unidadService, policiaService, alarmaService, notificacionService, atencionService).build();
            case "Notificaciones" -> new NotificacionesAdminPoliciaView(notificacionService).build();
            case "Configuración"  -> new ConfiguracionAdminPoliciaView(usuarioActual).build();
            case "Mapa de alertas"   -> new MapaAlertas(alertaService).build();
            case "Zonas peligrosas"  -> new MapaZonasPeligrosas(alertaService).build();
            case "Mapa Operaciones"  -> new MapaOperaciones(asignacionService, unidadService).build();
            default -> new DashboardBuilder(this).build();
        });
    }

    // =========================================================================
    // GRASP · Information Expert — quien conoce los datos, calcula los conteos
    // =========================================================================
    static class ContadorService {

        private final AdministradorPoliciaApp app;

        ContadorService(AdministradorPoliciaApp app) { this.app = app; }

        long alertasActivas() {
            try {
                return app.alertaService == null ? 0 :
                    app.alertaService.listar().stream()
                        .filter(a -> a.getEstado() == EstadoAlerta.PENDIENTE
                                  || a.getEstado() == EstadoAlerta.EN_ATENCION
                                  || a.getEstado() == EstadoAlerta.UNIDAD_ASIGNADA
                                  || a.getEstado() == EstadoAlerta.RECIBIDA)
                        .count();
            } catch (Exception e) { return 0; }
        }

        long policiasActivos() {
            try {
                return app.policiaService == null ? 0 :
                    app.policiaService.listar().stream()
                        .filter(p -> p.getEstadopolicial() == EstadoPolicia.DISPONIBLE
                                  || p.getEstadopolicial() == EstadoPolicia.EN_SERVICIO)
                        .count();
            } catch (Exception e) { return 0; }
        }

        long unidadesActivas() {
            try {
                return app.unidadService == null ? 0 :
                    app.unidadService.listar().stream()
                        .filter(u -> u.getEstado() == EstadoUnidadPolicial.OPERATIVA)
                        .count();
            } catch (Exception e) { return 0; }
        }

        long asignaciones() {
            try {
                return app.asignacionService == null ? 0 :
                    app.asignacionService.listar().size();
            } catch (Exception e) { return 0; }
        }

        List<Alerta> alertasRecientes(int limite) {
            List<Alerta> result = new ArrayList<>();
            try {
                if (app.alertaService != null) {
                    app.alertaService.listar().stream()
                        .filter(a -> a.getEstado() == EstadoAlerta.PENDIENTE
                                  || a.getEstado() == EstadoAlerta.EN_ATENCION
                                  || a.getEstado() == EstadoAlerta.UNIDAD_ASIGNADA
                                  || a.getEstado() == EstadoAlerta.RECIBIDA)
                        .sorted((a, b) -> b.getFechaHora() != null && a.getFechaHora() != null
                                ? b.getFechaHora().compareTo(a.getFechaHora()) : 0)
                        .limit(limite)
                        .forEach(result::add);
                }
            } catch (Exception ignored) {}
            return result;
        }

        List<Notificacion> notificacionesRecientes(int limite) {
            List<Notificacion> result = new ArrayList<>();
            try {
                if (app.notificacionService != null) {
                    app.notificacionService.listar().stream()
                        .limit(limite)
                        .forEach(result::add);
                }
            } catch (Exception ignored) {}
            return result;
        }
    }

    // =========================================================================
    // GRASP · Information Expert — quien conoce los colores de estado, los da
    // =========================================================================
    static class EstadoColorResolver {

        static String colorAlerta(EstadoAlerta estado) {
            if (estado == null) return GRAY_TEXT;
            return switch (estado) {
                case PENDIENTE, EN_ATENCION -> RED;
                case UNIDAD_ASIGNADA        -> ORANGE;
                case RESUELTA               -> GREEN;
                default                     -> GRAY_TEXT;
            };
        }

        static String bgEstadoNotificacion(EstadoNotificacion e) {
            if (e == null) return "#fff8e1";
            return switch (e) {
                case LEIDA   -> GREEN_LIGHT;
                case ENVIADA -> BLUE_LIGHT;
                case ERROR   -> RED_LIGHT;
                default      -> "#fff8e1";
            };
        }

        static String colorNotificacion(EstadoNotificacion e) {
            if (e == null) return ORANGE;
            return switch (e) {
                case LEIDA   -> GREEN;
                case ENVIADA -> BLUE;
                case ERROR   -> RED;
                default      -> ORANGE;
            };
        }

        static String iconoNotificacion(EstadoNotificacion e) {
            if (e == null) return "\uf017";
            return switch (e) {
                case LEIDA   -> "\uf058";
                case ENVIADA -> "\uf1d8";
                case ERROR   -> "\uf057";
                default      -> "\uf017";
            };
        }

        static String formatFecha(LocalDateTime dt) {
            if (dt == null) return "—";
            long mins = java.time.Duration.between(dt, LocalDateTime.now()).toMinutes();
            if (mins < 1)    return "Hace un momento";
            if (mins < 60)   return "Hace " + mins + " min";
            if (mins < 1440) return "Hace " + (mins / 60) + " h";
            return dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
    }

    // =========================================================================
    // GRASP · High Cohesion — UIFactory agrupa SOLO la creación de widgets
    // comunes reutilizados en toda la app. No tiene lógica de negocio.
    // =========================================================================
    static class UIFactory {

        static Label label(String text, double size, String color, boolean bold) {
            Label lbl = new Label(text);
            lbl.setFont(bold
                    ? Font.font("System", FontWeight.BOLD, size)
                    : Font.font("System", size));
            lbl.setTextFill(Color.web(color));
            return lbl;
        }

        static Region separator() {
            Region sep = new Region();
            sep.setPrefHeight(1);
            sep.setStyle("-fx-background-color:" + BORDER + ";");
            return sep;
        }

        static void shadow(Region node) {
            node.setEffect(new DropShadow(10, 0, 2, Color.web("#0000001a")));
        }

        static VBox createPanel(String faIcon, String title,
                                String iconColor, String bgColor) {
            VBox panel = new VBox(10);
            panel.setPadding(new Insets(16));
            panel.setStyle("-fx-background-color:" + WHITE + ";-fx-background-radius:12;");
            shadow(panel);

            StackPane iconBox = new StackPane();
            iconBox.setPrefSize(28, 28); iconBox.setMinSize(28, 28);
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

        static VBox statCard(String bgIcon, String accentColor, String title,
                             long value, String sub, String faIcon) {
            VBox card = new VBox(10);
            card.setPadding(new Insets(20, 22, 20, 22));
            card.setStyle("-fx-background-color:white;-fx-background-radius:18;");
            HBox.setHgrow(card, Priority.ALWAYS);
            shadow(card);

            StackPane iconWrap = new StackPane();
            iconWrap.setPrefSize(52, 52); iconWrap.setMinSize(52, 52); iconWrap.setMaxSize(52, 52);
            Region iconBg = new Region();
            iconBg.setPrefSize(52, 52);
            iconBg.setStyle("-fx-background-color:" + bgIcon + ";"
                    + "-fx-background-radius:14;-fx-border-color:" + accentColor + ";"
                    + "-fx-border-radius:14;-fx-border-width:1.5;");
            Label iconLbl = new Label(faIcon);
            iconLbl.setStyle("-fx-font-family:" + FA + ";-fx-font-size:22px;-fx-text-fill:" + accentColor + ";");
            iconWrap.getChildren().addAll(iconBg, iconLbl);

            Label titleLbl = label(title, 13, "#374151", true);
            Label valLbl   = new Label(String.valueOf(value));
            valLbl.setStyle("-fx-font-size:36px;-fx-font-weight:bold;-fx-text-fill:" + accentColor + ";");
            Label subLbl   = label(sub, 11, GRAY_TEXT, false);

            HBox topRow = new HBox(16, iconWrap, new VBox(3, titleLbl, valLbl, subLbl));
            topRow.setAlignment(Pos.CENTER_LEFT);
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

        static VBox actionBtn(String faIcon, String text,
                              String iconColor, String bgColor, Runnable accion) {
            StackPane iconWrap = new StackPane();
            iconWrap.setPrefSize(34, 34); iconWrap.setMinSize(34, 34);
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

            String base  = "-fx-background-color:white;-fx-background-radius:12;-fx-border-color:" + BORDER + ";-fx-border-radius:12;-fx-border-width:1;";
            String hover = "-fx-background-color:" + bgColor + ";-fx-background-radius:12;-fx-border-color:" + iconColor + ";-fx-border-radius:12;-fx-border-width:1.5;";
            btn.setStyle(base);
            btn.setOnMouseEntered(e -> btn.setStyle(hover));
            btn.setOnMouseExited(e  -> btn.setStyle(base));
            btn.setOnMouseClicked(e -> accion.run());
            return btn;
        }
    }

    // =========================================================================
    // GRASP · High Cohesion — SidebarBuilder construye SOLO el sidebar.
    // GRASP · Low Coupling — recibe AdministradorPoliciaApp solo para navegar.
    // =========================================================================
    static class SidebarBuilder {

        private final AdministradorPoliciaApp app;
        private VBox   sidebarBox;
        private VBox   logoTextBox;
        private Label  logoutText;
        private VBox   nav;
        private boolean mapaExpandido = false;
        private VBox    mapaSubMenu   = null;

        SidebarBuilder(AdministradorPoliciaApp app) { this.app = app; }

        ScrollPane build() {
            sidebarBox = new VBox();
            sidebarBox.setPrefWidth(SIDEBAR_COLLAPSED);
            sidebarBox.setMinWidth(SIDEBAR_COLLAPSED);
            sidebarBox.setMaxWidth(SIDEBAR_COLLAPSED);
            sidebarBox.setMaxHeight(Double.MAX_VALUE);
            sidebarBox.setFillWidth(true);
            VBox.setVgrow(sidebarBox, Priority.ALWAYS);
            sidebarBox.setStyle("-fx-background-color: linear-gradient(to right, #16283d, #1f3a56);");

            sidebarBox.getChildren().addAll(
                    buildLogo(),
                    buildProfileCard(),
                    buildNav(),
                    spacer(),
                    buildLogout()
            );

            sidebarBox.setOnMouseEntered(e -> setExpanded(true));
            sidebarBox.setOnMouseExited(e  -> setExpanded(false));

            ScrollPane scroll = new ScrollPane(sidebarBox);
            scroll.setFitToWidth(true); scroll.setFitToHeight(true);
            scroll.setPannable(true);
            scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scroll.setStyle("-fx-background:transparent;-fx-background-color:transparent;"
                    + "-fx-border-color:transparent;-fx-padding:0;");
            return scroll;
        }

        // ── Logo ──────────────────────────────────────────────────
        private HBox buildLogo() {
            HBox box = new HBox(10);
            box.setPadding(new Insets(20, 8, 20, 8));
            box.setAlignment(Pos.CENTER_LEFT);

            ImageView logoImg = new ImageView(
                    new Image(app.getClass().getResourceAsStream("/LogoWolertAPP.png")));
            logoImg.setFitWidth(44); logoImg.setFitHeight(44); logoImg.setPreserveRatio(true);

            logoTextBox = new VBox(2);
            logoTextBox.getChildren().addAll(
                    UIFactory.label("WolertApp", 15, WHITE, true),
                    UIFactory.label("Administrador Policía", 9, "#8899bb", false));
            logoTextBox.setVisible(false);
            logoTextBox.setManaged(false);

            box.getChildren().addAll(new StackPane(logoImg), logoTextBox);
            return box;
        }

        // ── Perfil ────────────────────────────────────────────────
        private HBox buildProfileCard() {
            HBox card = new HBox(10);
            card.setPadding(new Insets(10, 16, 10, 16));
            card.setAlignment(Pos.CENTER_LEFT);
            card.setStyle("-fx-background-color:rgba(255,255,255,0.08);-fx-background-radius:12;");

            Circle av = new Circle(20, Color.web("#1f3a56"));
            Label icon = new Label("\uf505");
            icon.setStyle("-fx-font-family:" + FA + ";-fx-font-size:15px;-fx-text-fill:#a8c0dd;");
            StackPane avBox = new StackPane(av, icon);
            avBox.setMinWidth(36); avBox.setMaxWidth(36);

            String nombre = app.usuarioActual != null
                    ? ((app.usuarioActual.getPrimer_nombre()   != null ? app.usuarioActual.getPrimer_nombre()   : "")
                     + " " + (app.usuarioActual.getPrimer_apellido() != null ? app.usuarioActual.getPrimer_apellido() : "")).trim()
                    : "Administrador";

            HBox statusRow = new HBox(4);
            statusRow.setAlignment(Pos.CENTER_LEFT);
            statusRow.getChildren().addAll(
                    new Circle(4, Color.web(GREEN)),
                    UIFactory.label("En servicio", 10, GREEN, false));

            VBox info = new VBox(2,
                    UIFactory.label(nombre, 12, WHITE, true),
                    UIFactory.label("Administrador Policía", 9, "#8899bb", false),
                    statusRow);

            card.getChildren().addAll(avBox, info);
            return card;
        }

        // ── Navegación ────────────────────────────────────────────
        private VBox buildNav() {
            nav = new VBox(2);
            nav.setPadding(new Insets(12, 4, 12, 4));
            nav.getChildren().addAll(
                    navItem("\uf015", "Centro de operaciones"),
                    navItem("\uf0f3", "Alertas"),
                    navItem("\uf0a1", "Alarmas"),
                    navItem("\uf14a", "Asignaciones"),
                    navItem("\uf505", "Policías"),
                    navItem("\uf1b9", "Unidades"),
                    navItem("\uf1da", "Historial"),
                    buildMapaNavItem(),
                    navItem("\uf080", "Estadísticas"),
                    navItem("\uf201", "Reportes"),
                    navItem("\uf1f6", "Notificaciones"),
                    navItem("\uf013", "Configuración")
            );
            return nav;
        }

        private HBox navItem(String icon, String text) {
            HBox item = new HBox(10);
            item.setPadding(new Insets(9, 12, 9, 12));
            item.setAlignment(Pos.CENTER_LEFT);
            item.setCursor(javafx.scene.Cursor.HAND);
            item.setMaxWidth(Double.MAX_VALUE);
            item.setStyle("-fx-background-radius:8;");

            Label iconLbl = new Label(icon);
            iconLbl.setStyle("-fx-font-family:" + FA + ";-fx-font-size:14px;-fx-text-fill:#8899bb;");
            StackPane iconBox = new StackPane(iconLbl);
            iconBox.setPrefWidth(24); iconBox.setMinWidth(24); iconBox.setMaxWidth(24);
            iconBox.setAlignment(Pos.CENTER);

            Label textLbl = UIFactory.label(text, 13, "#f8fafc", false);
            textLbl.setVisible(false); textLbl.setManaged(false);
            item.getChildren().addAll(iconBox, textLbl);

            item.setOnMouseEntered(e -> item.setStyle("-fx-background-color:rgba(255,255,255,0.15);-fx-background-radius:8;"));
            item.setOnMouseExited(e  -> item.setStyle("-fx-background-radius:8;"));
            item.setOnMouseClicked(e -> {
                resetNavStyles();
                item.setStyle("-fx-background-color:rgba(255,255,255,0.20);-fx-background-radius:8;");
                iconLbl.setStyle("-fx-font-family:" + FA + ";-fx-font-size:14px;-fx-text-fill:white;");
                textLbl.setTextFill(Color.WHITE);
                app.navegar(text);
            });
            return item;
        }

        private HBox buildMapaNavItem() {
            Label arrowLbl = UIFactory.label("▶", 10, WHITE, false);
            Label mapaIcon = new Label("\uf3c5");
            mapaIcon.setStyle("-fx-font-family:" + FA + ";-fx-font-size:14px;-fx-text-fill:#8899bb;");
            StackPane iconBox = new StackPane(mapaIcon);
            iconBox.setPrefWidth(24); iconBox.setMinWidth(24); iconBox.setMaxWidth(24);
            iconBox.setAlignment(Pos.CENTER);

            Label mapaText = UIFactory.label("Mapas", 13, "#f8fafc", false);
            mapaText.setVisible(false); mapaText.setManaged(false);

            Region sp = new Region();
            HBox.setHgrow(sp, Priority.ALWAYS);

            HBox item = new HBox(10, iconBox, mapaText, sp, arrowLbl);
            item.setPadding(new Insets(9, 12, 9, 12));
            item.setAlignment(Pos.CENTER_LEFT);
            item.setCursor(javafx.scene.Cursor.HAND);
            item.setMaxWidth(Double.MAX_VALUE);
            item.setStyle("-fx-background-radius:8;");

            item.setOnMouseEntered(e -> item.setStyle("-fx-background-color:rgba(255,255,255,0.15);-fx-background-radius:8;"));
            item.setOnMouseExited(e  -> item.setStyle("-fx-background-radius:8;"));
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
            item.setStyle("-fx-background-radius:7;");
            item.setOnMouseEntered(e -> item.setStyle("-fx-background-color:#ffffff12;-fx-background-radius:7;"));
            item.setOnMouseExited(e  -> item.setStyle("-fx-background-radius:7;"));

            Label iconLbl = new Label(icon);
            iconLbl.setStyle("-fx-font-family:" + FA + ";-fx-font-size:12px;-fx-text-fill:#8899bb;");
            StackPane iconBox = new StackPane(iconLbl);
            iconBox.setPrefWidth(20); iconBox.setMinWidth(20); iconBox.setAlignment(Pos.CENTER);

            item.getChildren().addAll(iconBox, UIFactory.label(text, 11, WHITE, true));
            item.setOnMouseClicked(e -> app.navegar(text));
            return item;
        }

        // ── Logout ────────────────────────────────────────────────
        private HBox buildLogout() {
            Label logoutIcon = new Label("\uf2f5");
            logoutIcon.setStyle("-fx-font-family:" + FA + ";-fx-font-size:14px;-fx-text-fill:" + RED + ";");
            logoutIcon.setMinWidth(28); logoutIcon.setAlignment(Pos.CENTER);

            logoutText = UIFactory.label("Cerrar sesión", 13, WHITE, true);
            logoutText.setVisible(false); logoutText.setManaged(false);

            HBox logout = new HBox(10, logoutIcon, logoutText);
            logout.setPadding(new Insets(14, 8, 18, 14));
            logout.setAlignment(Pos.CENTER_LEFT);
            logout.setCursor(javafx.scene.Cursor.HAND);
            logout.setStyle("-fx-background-color:transparent;");
            logout.setOnMouseEntered(e -> logout.setStyle("-fx-background-color:rgba(229,57,53,0.15);-fx-background-radius:8;"));
            logout.setOnMouseExited(e  -> logout.setStyle("-fx-background-color:transparent;"));
            logout.setOnMouseClicked(e -> ((Stage) app.root.getScene().getWindow()).close());
            return logout;
        }

        // ── Expand / collapse ─────────────────────────────────────
        private void setExpanded(boolean expand) {
            double target = expand ? SIDEBAR_EXPANDED : SIDEBAR_COLLAPSED;
            javafx.animation.Timeline tl = new javafx.animation.Timeline(
                    new javafx.animation.KeyFrame(Duration.millis(180),
                            new javafx.animation.KeyValue(sidebarBox.prefWidthProperty(), target, javafx.animation.Interpolator.EASE_BOTH),
                            new javafx.animation.KeyValue(sidebarBox.minWidthProperty(),  target, javafx.animation.Interpolator.EASE_BOTH),
                            new javafx.animation.KeyValue(sidebarBox.maxWidthProperty(),  target, javafx.animation.Interpolator.EASE_BOTH)));
            tl.play();

            if (logoTextBox != null) { logoTextBox.setVisible(expand); logoTextBox.setManaged(expand); }
            if (logoutText  != null) { logoutText.setVisible(expand);  logoutText.setManaged(expand); }

            nav.getChildren().forEach(node -> {
                if (node instanceof HBox hbox) {
                    hbox.getChildren().forEach(child -> {
                        if (child instanceof Label lbl && !lbl.getStyle().contains("Font Awesome")) {
                            lbl.setVisible(expand); lbl.setManaged(expand);
                        }
                    });
                }
            });
        }

        private void resetNavStyles() {
            nav.getChildren().forEach(node -> {
                if (node instanceof HBox hbox) {
                    hbox.setStyle("-fx-background-radius:8;");
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

        private VBox spacer() {
            VBox s = new VBox();
            VBox.setVgrow(s, Priority.ALWAYS);
            return s;
        }
    }

    // =========================================================================
    // GRASP · High Cohesion — DashboardBuilder construye SOLO el contenido
    // principal del dashboard. Usa ContadorService y EstadoColorResolver.
    // =========================================================================
    static class DashboardBuilder {

        private final AdministradorPoliciaApp app;
        private final ContadorService         contador;

        DashboardBuilder(AdministradorPoliciaApp app) {
            this.app      = app;
            this.contador = new ContadorService(app);
        }

        ScrollPane build() {
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
            scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scroll.setStyle("-fx-background-color:" + BG + ";-fx-background:" + BG + ";");
            return scroll;
        }

        // ── Top bar con reloj y campana ───────────────────────────
        private HBox buildTopBar() {
            HBox bar = new HBox();
            bar.setAlignment(Pos.CENTER_LEFT);

            String saludo = app.usuarioActual != null && app.usuarioActual.getPrimer_nombre() != null
                    ? "¡Bienvenido, " + app.usuarioActual.getPrimer_nombre() + "!"
                    : "¡Bienvenido!";
            VBox greeting = new VBox(3,
                    UIFactory.label(saludo, 24, "#111827", true),
                    UIFactory.label("Centro de operaciones policial — WolertApp", 12, GRAY_TEXT, false));

            HBox rightBox = new HBox(16);
            rightBox.setAlignment(Pos.CENTER_RIGHT);
            HBox.setHgrow(rightBox, Priority.ALWAYS);
            rightBox.getChildren().addAll(buildReloj(), buildCampana());

            bar.getChildren().addAll(greeting, rightBox);
            return bar;
        }

        private VBox buildReloj() {
            DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", new Locale("es", "CO"));
            DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("hh:mm:ss a",             new Locale("es", "CO"));
            LocalDateTime now = LocalDateTime.now(ZoneId.of("America/Bogota"));

            Label calIcon = new Label("\uf073");
            calIcon.setStyle("-fx-font-family:" + FA + ";-fx-font-size:12px;-fx-text-fill:#374151;");
            Label dateLbl = UIFactory.label(now.format(dateFmt), 12, "#374151", false);
            Label timeLbl = UIFactory.label(now.format(timeFmt), 12, GRAY_TEXT, false);

            Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), ev -> {
                LocalDateTime n = LocalDateTime.now(ZoneId.of("America/Bogota"));
                dateLbl.setText(n.format(dateFmt));
                timeLbl.setText(n.format(timeFmt));
            }));
            clock.setCycleCount(Timeline.INDEFINITE);
            clock.play();

            HBox dateRow = new HBox(5, calIcon, dateLbl);
            dateRow.setAlignment(Pos.CENTER_RIGHT);
            VBox box = new VBox(2, dateRow, timeLbl);
            box.setAlignment(Pos.CENTER_RIGHT);
            return box;
        }

        // ── Campana con popup (Information Expert: conoce las notis) ─
        private StackPane buildCampana() {
            List<Alerta>       alertasRec = contador.alertasRecientes(4);
            List<Notificacion> notisRec   = contador.notificacionesRecientes(3);
            int total = alertasRec.size() + notisRec.size();

            Region bellBg = new Region();
            bellBg.setPrefSize(40, 40);
            bellBg.setStyle("-fx-background-color:white;-fx-background-radius:50%;"
                    + "-fx-border-color:#e5e7eb;-fx-border-radius:50%;-fx-border-width:1.5;");

            Label bellIcon = new Label("\uf0a2");
            bellIcon.setStyle("-fx-font-family:" + FA + ";-fx-font-size:16px;-fx-text-fill:#374151;");

            Label badgeNum = new Label(total > 9 ? "9+" : String.valueOf(total));
            badgeNum.setStyle("-fx-background-color:#e53935;-fx-text-fill:white;"
                    + "-fx-font-size:9px;-fx-font-weight:bold;"
                    + "-fx-background-radius:20;-fx-padding:1 4;");
            badgeNum.setTranslateX(10); badgeNum.setTranslateY(-10);
            badgeNum.setVisible(total > 0);

            StackPane bellStack = new StackPane(bellBg, bellIcon, badgeNum);
            bellStack.setCursor(javafx.scene.Cursor.HAND);
            bellStack.setOnMouseEntered(e -> bellBg.setStyle(
                    "-fx-background-color:#f0f7ff;-fx-background-radius:50%;"
                    + "-fx-border-color:" + BLUE + ";-fx-border-radius:50%;-fx-border-width:1.5;"));
            bellStack.setOnMouseExited(e -> bellBg.setStyle(
                    "-fx-background-color:white;-fx-background-radius:50%;"
                    + "-fx-border-color:#e5e7eb;-fx-border-radius:50%;-fx-border-width:1.5;"));

            // Creator: PopupBuilder construye el popup porque tiene todos los datos
            javafx.stage.Popup popup = new PopupBuilder(app, alertasRec, notisRec, total).build();
            bellStack.setOnMouseClicked(e -> {
                if (popup.isShowing()) {
                    popup.hide();
                } else {
                    badgeNum.setVisible(false);
                    javafx.geometry.Bounds b = bellStack.localToScreen(bellStack.getBoundsInLocal());
                    popup.show(bellStack, b.getMaxX() - 340, b.getMaxY() + 8);
                }
            });
            return bellStack;
        }

        // ── Stats ─────────────────────────────────────────────────
        private HBox buildStats() {
            return new HBox(16,
                    UIFactory.statCard(RED_LIGHT,    RED,       "Alertas activas",  contador.alertasActivas(),  "PENDIENTE / EN ATENCIÓN", "\uf0f3"),
                    UIFactory.statCard(ORANGE_LIGHT,  ORANGE,    "Asignaciones",     contador.asignaciones(),    "Total registradas",        "\uf14a"),
                    UIFactory.statCard(GREEN_LIGHT,   GREEN,     "Unidades activas", contador.unidadesActivas(), "Estado OPERATIVA",          "\uf1b9"),
                    UIFactory.statCard(BLUE_LIGHT,    BLUE,      "Policías activos", contador.policiasActivos(), "Estado DISPONIBLE",         "\uf505")
            );
        }

        // ── Paneles centrales ─────────────────────────────────────
        private HBox buildCenterPanels() {
            return new HBox(16, buildAlertsPanel(), buildMapPanel());
        }

        private VBox buildAlertsPanel() {
            VBox card = UIFactory.createPanel("\uf0f3", "Alertas recientes", RED, RED_LIGHT);
            HBox.setHgrow(card, Priority.ALWAYS);
            try {
                List<Alerta> alertas = app.alertaService != null ? app.alertaService.listar() : List.of();
                if (alertas.isEmpty()) {
                    card.getChildren().add(UIFactory.label("No hay alertas registradas", 13, GRAY_TEXT, false));
                } else {
                    alertas.stream()
                        .filter(a -> a.getFechaHora() != null)
                        .sorted((a, b) -> b.getFechaHora().compareTo(a.getFechaHora()))
                        .limit(5)
                        .forEach(a -> {
                            String dotColor = EstadoColorResolver.colorAlerta(a.getEstado());
                            String tipo     = a.getTipoalerta() != null ? a.getTipoalerta().getNombre() : "Alerta";
                            String barrio   = a.getBarrio() != null ? " — " + a.getBarrio().getNombre() : "";
                            String sub      = EstadoColorResolver.formatFecha(a.getFechaHora())
                                    + (a.getEstado() != null ? " · " + a.getEstado().name().replace("_", " ") : "");

                            HBox item = alertItem(tipo + barrio, sub, dotColor);
                            item.setOnMouseClicked(e -> app.navegar("Alertas"));
                            item.setOnMouseEntered(e -> item.setStyle("-fx-background-color:#f0f4ff;-fx-cursor:hand;-fx-background-radius:8;"));
                            item.setOnMouseExited(e  -> item.setStyle("-fx-background-color:transparent;"));
                            card.getChildren().addAll(item, UIFactory.separator());
                        });
                }
            } catch (Exception e) {
                card.getChildren().add(UIFactory.label("Error al cargar alertas", 12, RED, false));
            }
            return card;
        }

        private HBox alertItem(String title, String sub, String dotColor) {
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(7, 0, 7, 0));
            row.setCursor(javafx.scene.Cursor.HAND);

            Circle dot = new Circle(5, Color.web(dotColor));
            StackPane iconBox = new StackPane();
            iconBox.setPrefSize(34, 34); iconBox.setMinSize(34, 34); iconBox.setMaxSize(34, 34);
            Region iconBg = new Region();
            iconBg.setPrefSize(34, 34);
            iconBg.setStyle("-fx-background-color:#e8f0fe;-fx-background-radius:10;");
            Label faLbl = new Label("\uf0f3");
            faLbl.setStyle("-fx-font-family:" + FA + ";-fx-font-size:14px;-fx-text-fill:" + BLUE + ";");
            iconBox.getChildren().addAll(iconBg, faLbl);

            VBox text = new VBox(2,
                    UIFactory.label(title, 12, "#111827", false),
                    UIFactory.label(sub,   10, GRAY_TEXT, false));
            HBox.setHgrow(text, Priority.ALWAYS);

            row.getChildren().addAll(dot, iconBox, text, UIFactory.label(">", 13, GRAY_TEXT, false));
            return row;
        }

        private VBox buildMapPanel() {
            VBox card = UIFactory.createPanel("\uf3c5", "Mapa policial en tiempo real", BLUE, BLUE_LIGHT);
            card.setPrefWidth(340);
            try {
                MapaOperaciones mapaOps  = new MapaOperaciones(app.asignacionService, app.unidadService);
                BorderPane      mapaComp = mapaOps.build();
                mapaComp.setTop(null); mapaComp.setRight(null);

                if (mapaComp.getCenter() instanceof HBox hb && !hb.getChildren().isEmpty()) {
                    javafx.scene.Node soloMapa = hb.getChildren().get(0);
                    hb.getChildren().remove(soloMapa);

                    StackPane wrapper = new StackPane(soloMapa);
                    wrapper.setPrefHeight(200); wrapper.setMinHeight(200); wrapper.setMaxHeight(200);
                    wrapper.setStyle("-fx-background-radius:10;");

                    Label btnAbrir = UIFactory.label("\uf065  Ver mapa completo", 11, WHITE, true);
                    btnAbrir.setStyle("-fx-background-color:rgba(31,58,86,0.85);-fx-text-fill:white;"
                            + "-fx-font-size:11px;-fx-font-weight:bold;"
                            + "-fx-background-radius:8;-fx-padding:6 12;-fx-cursor:hand;");
                    StackPane.setAlignment(btnAbrir, Pos.BOTTOM_RIGHT);
                    StackPane.setMargin(btnAbrir, new Insets(0, 10, 10, 0));
                    wrapper.getChildren().add(btnAbrir);
                    btnAbrir.setOnMouseClicked(e -> app.navegar("Mapa Operaciones"));
                    card.getChildren().add(wrapper);
                } else {
                    card.getChildren().add(buildMapaFallback());
                }
            } catch (Exception ex) {
                card.getChildren().add(buildMapaFallback());
            }
            card.setCursor(javafx.scene.Cursor.HAND);
            card.setOnMouseClicked(e -> app.navegar("Mapa Operaciones"));
            return card;
        }

        private StackPane buildMapaFallback() {
            StackPane area = new StackPane();
            area.setPrefHeight(200);
            Rectangle bg = new Rectangle();
            bg.setFill(Color.web("#d1e8d1"));
            bg.widthProperty().bind(area.widthProperty());
            bg.heightProperty().bind(area.heightProperty());
            bg.setArcWidth(10); bg.setArcHeight(10);

            VBox popup = new VBox(4,
                    UIFactory.label("Mapa interactivo", 11, "#374151", true),
                    UIFactory.label("Clic para abrir mapa completo →", 10, BLUE, false));
            popup.setPadding(new Insets(8, 12, 8, 12));
            popup.setStyle("-fx-background-color:white;-fx-background-radius:8;");
            popup.setEffect(new DropShadow(6, Color.web("#0000001a")));
            popup.setTranslateX(15); popup.setTranslateY(8);
            area.getChildren().addAll(bg, popup);
            return area;
        }

        // ── Paneles inferiores ────────────────────────────────────
        private HBox buildBottomPanels() {
            VBox unidades = UIFactory.createPanel("\uf1b9", "Unidades disponibles", GREEN, GREEN_LIGHT);
            VBox policias = UIFactory.createPanel("\uf505", "Policías en servicio",  BLUE,  BLUE_LIGHT);
            VBox acciones = UIFactory.createPanel("\uf0e7", "Acciones rápidas",      ORANGE, ORANGE_LIGHT);
            HBox.setHgrow(unidades, Priority.ALWAYS);
            HBox.setHgrow(policias, Priority.ALWAYS);
            HBox.setHgrow(acciones, Priority.ALWAYS);

            if (app.unidadService != null) {
                try {
                    app.unidadService.listar().stream().limit(5).forEach(u -> {
                        String est   = u.getEstado() != null ? u.getEstado().name().replace("_", " ") : "—";
                        String color = u.getEstado() == EstadoUnidadPolicial.OPERATIVA ? GREEN
                                     : u.getEstado() == EstadoUnidadPolicial.ACTIVA    ? ORANGE : GRAY_TEXT;
                        HBox item = listItem("\uf1b9", u.getNombre(), est, color);
                        item.setOnMouseClicked(e -> app.navegar("Unidades"));
                        item.setOnMouseEntered(e -> item.setStyle("-fx-background-color:#f0f4ff;-fx-cursor:hand;-fx-background-radius:8;"));
                        item.setOnMouseExited(e  -> item.setStyle("-fx-background-color:transparent;"));
                        unidades.getChildren().addAll(item, UIFactory.separator());
                    });
                } catch (Exception e) {
                    unidades.getChildren().add(UIFactory.label("Error al cargar unidades", 12, RED, false));
                }
            }

            if (app.policiaService != null) {
                try {
                    app.policiaService.listar().stream().limit(5).forEach(p -> {
                        String nombre = ((p.getPrimer_nombre()   != null ? p.getPrimer_nombre()   : "")
                                      + " " + (p.getPrimer_apellido() != null ? p.getPrimer_apellido() : "")).trim();
                        String est    = p.getEstadopolicial() != null ? p.getEstadopolicial().name().replace("_", " ") : "—";
                        String color  = switch (p.getEstadopolicial() != null
                                ? p.getEstadopolicial() : EstadoPolicia.FUERA_DE_SERVICIO) {
                            case DISPONIBLE  -> GREEN;
                            case EN_SERVICIO -> BLUE;
                            case OCUPADO     -> ORANGE;
                            default          -> GRAY_TEXT;
                        };
                        HBox item = listItem("\uf505", nombre, est, color);
                        item.setOnMouseClicked(e -> app.navegar("Policías"));
                        item.setOnMouseEntered(e -> item.setStyle("-fx-background-color:#f0f4ff;-fx-cursor:hand;-fx-background-radius:8;"));
                        item.setOnMouseExited(e  -> item.setStyle("-fx-background-color:transparent;"));
                        policias.getChildren().addAll(item, UIFactory.separator());
                    });
                } catch (Exception e) {
                    policias.getChildren().add(UIFactory.label("Error al cargar policías", 12, RED, false));
                }
            }

            GridPane grid = new GridPane();
            grid.setHgap(10); grid.setVgap(10); grid.setMaxWidth(Double.MAX_VALUE);
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(50);
            grid.getColumnConstraints().addAll(col, new ColumnConstraints() {{ setPercentWidth(50); }});

            grid.add(UIFactory.actionBtn("\uf14a", "Asignar",      BLUE,    BLUE_LIGHT,    () -> app.navegar("Asignaciones")), 0, 0);
            grid.add(UIFactory.actionBtn("\uf0f3", "Nueva alerta", RED,     RED_LIGHT,     () -> {
                MapaAlerta mapa = new MapaAlerta((Stage) app.root.getScene().getWindow(), app.usuarioActual, app.alertaService, null);
                mapa.mostrar();
            }), 1, 0);
            grid.add(UIFactory.actionBtn("\uf0a1", "Notificar",    GREEN,   GREEN_LIGHT,   () -> app.navegar("Notificaciones")), 0, 1);
            grid.add(UIFactory.actionBtn("\uf080", "Reportes",     ORANGE,  ORANGE_LIGHT,  () -> app.navegar("Reportes")), 1, 1);
            grid.add(UIFactory.actionBtn("\uf3c5", "Mapa ops.",    "#7b1fa2", "#f3e5f5",   () -> app.navegar("Mapa Operaciones")), 0, 2);
            grid.add(UIFactory.actionBtn("\uf201", "Estadísticas", "#0097a7", "#e0f7fa",   () -> app.navegar("Estadísticas")), 1, 2);

            acciones.getChildren().add(grid);
            return new HBox(16, unidades, policias, acciones);
        }

        private HBox listItem(String faIcon, String title, String sub, String subColor) {
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(6, 4, 6, 4));

            StackPane iconBox = new StackPane();
            iconBox.setPrefSize(34, 34); iconBox.setMinSize(34, 34); iconBox.setMaxSize(34, 34);
            Region iconBg = new Region();
            iconBg.setPrefSize(34, 34);
            iconBg.setStyle("-fx-background-color:#e8f0fe;-fx-background-radius:10;");
            Label faLbl = new Label(faIcon);
            faLbl.setStyle("-fx-font-family:" + FA + ";-fx-font-size:14px;-fx-text-fill:" + BLUE + ";");
            iconBox.getChildren().addAll(iconBg, faLbl);

            VBox text = new VBox(1,
                    UIFactory.label(title, 12, "#111827", true),
                    UIFactory.label(sub,   10, subColor,  false));
            HBox.setHgrow(text, Priority.ALWAYS);
            row.getChildren().addAll(iconBox, text);
            return row;
        }
    }

    // =========================================================================
    // GRASP · Creator — PopupBuilder crea el popup porque tiene todos sus datos
    // GRASP · High Cohesion — solo construye el popup de notificaciones
    // =========================================================================
    static class PopupBuilder {

        private final AdministradorPoliciaApp app;
        private final List<Alerta>            alertas;
        private final List<Notificacion>      notis;
        private final int                     total;

        PopupBuilder(AdministradorPoliciaApp app,
                     List<Alerta> alertas, List<Notificacion> notis, int total) {
            this.app     = app;
            this.alertas = alertas;
            this.notis   = notis;
            this.total   = total;
        }

        javafx.stage.Popup build() {
            javafx.stage.Popup popup = new javafx.stage.Popup();
            popup.setAutoHide(true);

            VBox box = new VBox(0);
            box.setPrefWidth(340);
            box.setStyle("-fx-background-color:white;-fx-background-radius:14;"
                    + "-fx-border-color:#e5e7eb;-fx-border-radius:14;-fx-border-width:1;"
                    + "-fx-effect:dropshadow(gaussian,rgba(15,23,42,0.18),24,0,0,8);");

            box.getChildren().add(buildHeader());
            if (!alertas.isEmpty()) box.getChildren().addAll(buildSeccion("\uf0f3", "Alertas activas", RED),   buildFilasAlertas(popup));
            if (!notis.isEmpty())   box.getChildren().addAll(buildSeccion("\uf1f6", "Notificaciones recientes", BLUE), buildFilasNotis(popup));
            if (alertas.isEmpty() && notis.isEmpty()) box.getChildren().add(buildVacio());
            box.getChildren().add(buildFooter(popup));

            popup.getContent().add(box);
            return popup;
        }

        private HBox buildHeader() {
            HBox h = new HBox(8);
            h.setPadding(new Insets(14, 16, 12, 16));
            h.setAlignment(Pos.CENTER_LEFT);
            h.setStyle("-fx-background-color:#f8fafc;-fx-background-radius:14 14 0 0;"
                    + "-fx-border-color:transparent transparent #e5e7eb transparent;-fx-border-width:0 0 1 0;");
            Label title = UIFactory.label("Notificaciones del sistema", 14, "#111827", true);
            HBox.setHgrow(title, Priority.ALWAYS);
            Label badge = new Label(total + " nuevas");
            badge.setStyle("-fx-background-color:#e5393522;-fx-text-fill:#e53935;"
                    + "-fx-font-size:11px;-fx-font-weight:bold;-fx-background-radius:20;-fx-padding:3 8;");
            h.getChildren().addAll(title, badge);
            return h;
        }

        private HBox buildSeccion(String ico, String texto, String color) {
            HBox h = new HBox(6);
            h.setPadding(new Insets(8, 16, 6, 16));
            h.setStyle("-fx-background-color:#f9fafb;");
            Label icLbl = new Label(ico);
            icLbl.setStyle("-fx-font-family:" + FA + ";-fx-font-size:11px;-fx-text-fill:" + color + ";");
            Label tx = UIFactory.label(texto, 11, GRAY_TEXT, true);
            h.getChildren().addAll(icLbl, tx);
            return h;
        }

        private VBox buildFilasAlertas(javafx.stage.Popup popup) {
            VBox v = new VBox(0);
            for (Alerta a : alertas) {
                String tipo   = a.getTipoalerta() != null ? a.getTipoalerta().getNombre() : "Alerta";
                String barrio = a.getBarrio()     != null ? a.getBarrio().getNombre()      : "—";
                String fecha  = a.getFechaHora()  != null
                        ? a.getFechaHora().format(DateTimeFormatter.ofPattern("dd/MM HH:mm")) : "—";
                String est    = a.getEstado()     != null ? a.getEstado().name().replace("_", " ") : "—";

                HBox fila = buildFila(
                        EstadoColorResolver.colorAlerta(a.getEstado()),
                        tipo + " — " + barrio,
                        est + "  ·  " + fecha,
                        () -> { popup.hide(); app.navegar("Alertas"); });
                v.getChildren().add(fila);
            }
            return v;
        }

        private VBox buildFilasNotis(javafx.stage.Popup popup) {
            VBox v = new VBox(0);
            for (Notificacion n : notis) {
                String msg  = n.getMensaje() != null
                        ? (n.getMensaje().length() > 40 ? n.getMensaje().substring(0, 40) + "…" : n.getMensaje()) : "—";
                String dest = n.getCorreodestinatario() != null ? n.getCorreodestinatario() : "—";
                HBox fila   = buildFila(BLUE, msg, dest,
                        () -> { popup.hide(); app.navegar("Notificaciones"); });
                v.getChildren().add(fila);
            }
            return v;
        }

        private HBox buildFila(String dotColor, String titulo, String sub, Runnable onClick) {
            HBox fila = new HBox(10);
            fila.setPadding(new Insets(10, 16, 10, 16));
            fila.setAlignment(Pos.CENTER_LEFT);
            fila.setStyle("-fx-border-color:transparent transparent #f3f4f6 transparent;-fx-border-width:0 0 1 0;");
            fila.setOnMouseEntered(e -> fila.setStyle("-fx-background-color:#f0f4ff;"
                    + "-fx-border-color:transparent transparent #f3f4f6 transparent;-fx-border-width:0 0 1 0;-fx-cursor:hand;"));
            fila.setOnMouseExited(e  -> fila.setStyle("-fx-border-color:transparent transparent #f3f4f6 transparent;-fx-border-width:0 0 1 0;"));
            fila.setOnMouseClicked(e -> onClick.run());

            VBox info = new VBox(2,
                    UIFactory.label(titulo, 12, "#111827", true),
                    UIFactory.label(sub,    11, GRAY_TEXT, false));
            HBox.setHgrow(info, Priority.ALWAYS);
            fila.getChildren().addAll(new Circle(5, Color.web(dotColor)), info);
            return fila;
        }

        private VBox buildVacio() {
            VBox v = new VBox(8);
            v.setAlignment(Pos.CENTER);
            v.setPadding(new Insets(28));
            Label ico = new Label("\uf1f6");
            ico.setStyle("-fx-font-family:" + FA + ";-fx-font-size:28px;-fx-text-fill:#94a3b8;");
            v.getChildren().addAll(ico, UIFactory.label("Sin notificaciones nuevas", 13, GRAY_TEXT, false));
            return v;
        }

        private HBox buildFooter(javafx.stage.Popup popup) {
            HBox footer = new HBox(24);
            footer.setPadding(new Insets(10, 16, 12, 16));
            footer.setAlignment(Pos.CENTER);
            footer.setStyle("-fx-background-color:#f8fafc;-fx-background-radius:0 0 14 14;"
                    + "-fx-border-color:#e5e7eb transparent transparent;-fx-border-width:1 0 0 0;");

            Label verAlertas = UIFactory.label("Ver alertas →", 12, RED, true);
            verAlertas.setCursor(javafx.scene.Cursor.HAND);
            verAlertas.setOnMouseClicked(e -> { popup.hide(); app.navegar("Alertas"); });

            Label verNotifs = UIFactory.label("Ver notificaciones →", 12, BLUE, true);
            verNotifs.setCursor(javafx.scene.Cursor.HAND);
            verNotifs.setOnMouseClicked(e -> { popup.hide(); app.navegar("Notificaciones"); });

            footer.getChildren().addAll(verAlertas, verNotifs);
            return footer;
        }
    }
}