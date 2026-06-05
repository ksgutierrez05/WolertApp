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
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.sql.SQLException;
import java.util.List;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import static javafx.scene.layout.Region.USE_COMPUTED_SIZE;
import sistemagestion.model.*;
import sistemagestion.service.*;

public class PoliciaApp {

    private static final String WHITE = "#ffffff";
    private static final String BG = "#f4f6fb";
    private static final String RED = "#e53935";
    private static final String GREEN = "#43a047";
    private static final String BLUE = "#1565c0";
    private static final String GRAY_TEXT = "#6b7280";

    private AlertaService alertaService;
    private AtencionAlertaService atencionService;
    private AlarmaService alarmaService;
    private NotificacionService notificacionService;
    private AsignacionUnidadService asignacionService;
    private PoliciaService policiaService;
    private UnidadPolicialService unidadService;
    private TipoArmaService tipoArmaService;
    private MedioTransporteService medioTransporteService;
    private static final double SIDEBAR_EXPANDED = 240;
    private static final double SIDEBAR_COLLAPSED = 60;
    private boolean sidebarExpanded = false;   // empieza colapsado
    private VBox sidebar;
    private VBox logoTextBox;

    private final Usuario usuarioActual;
    private Policia policiaActual;
    private BorderPane root;
    private VBox nav;

    public PoliciaApp(Usuario usuarioActual) {
        this.usuarioActual = usuarioActual;
        try {
            alertaService = new AlertaService();
            atencionService = new AtencionAlertaService();
            alarmaService = new AlarmaService();
            notificacionService = new NotificacionService();
            asignacionService = new AsignacionUnidadService();
            policiaService = new PoliciaService();

            unidadService = new UnidadPolicialService();
            tipoArmaService= new TipoArmaService();
            medioTransporteService=new MedioTransporteService();



            if (usuarioActual != null) {
                List<Policia> todos = policiaService.listar();
                for (Policia p : todos) {
                    if (usuarioActual.getIdentificacion() != null
                            && usuarioActual.getIdentificacion().equals(p.getIdentificacion())) {
                        policiaActual = p;
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            mostrarAlerta("Error de conexión", e.getMessage());
        }
    }

    // =========================================================================
    // SHOW
    // =========================================================================
    public void show(Stage stage) {
        Font.loadFont(getClass().getResourceAsStream("/fa-solid-900.ttf"), 14);
        root = new BorderPane();
        root.setLeft(buildSidebar());
        root.setCenter(new CentroOperacionesPoliciaView(
                usuarioActual, policiaActual,
                alertaService, atencionService,
                alarmaService, notificacionService, tipoArmaService, medioTransporteService, root).build());
        root.setStyle("-fx-background-color: " + BG + ";");
        Scene scene = new Scene(root, 1100, 650);
        stage.setTitle("WolertApp – Policía");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMaximized(true);
        stage.show();
    }

    // =========================================================================
    // SIDEBAR
    // =========================================================================
    private VBox buildSidebar() {
        sidebar = new VBox();
        sidebar.setPrefWidth(SIDEBAR_COLLAPSED);
        sidebar.setMinWidth(SIDEBAR_COLLAPSED);
        sidebar.setMaxWidth(SIDEBAR_COLLAPSED);
        sidebar.setStyle("-fx-background-color: linear-gradient(to right, #16283d, #1f3a56);");
        VBox.setVgrow(sidebar, Priority.ALWAYS);
        sidebar.setMaxHeight(Double.MAX_VALUE);

        // Logo
        HBox logoBox = new HBox(10);
        logoBox.setPadding(new Insets(16, 8, 16, 8));
        logoBox.setAlignment(Pos.CENTER_LEFT);
        logoBox.setMinWidth(USE_COMPUTED_SIZE);
        javafx.scene.shape.Rectangle logoClip = new javafx.scene.shape.Rectangle(SIDEBAR_EXPANDED, 80);
        logoBox.setClip(logoClip);

        ImageView logoImg = new ImageView(
                new Image(getClass().getResourceAsStream("/LogoWolertAPP.png")));

        logoImg.setFitHeight(48);
        logoImg.setFitWidth(0);
        logoImg.setPreserveRatio(true);
        logoImg.setSmooth(true);
        logoImg.setCache(true);

        VBox logoText = new VBox(2);
        logoText.getChildren().addAll(
                label("WolertApp", 15, WHITE, true),
                label("Portal Policía", 9, "#8899bb", false));
        logoText.setVisible(false);
        logoText.setManaged(false);
        this.logoTextBox = logoText;

        StackPane logoWrap = new StackPane(logoImg);
        logoWrap.setPrefSize(48, 48);
        logoWrap.setMinSize(48, 48);
        logoWrap.setMaxSize(48, 48);
        logoBox.getChildren().addAll(logoWrap, logoText);

        // Nav
        nav = new VBox(2);

        nav.setPadding(new Insets(12, 8, 12, 8));

        nav.getChildren().addAll(
                navItem("\uf015", "Centro de operaciones"),
                navItem("\uf0f3", "Mis alertas"),
                navItem("\uf46d", "Mis atenciones"),
                navItem("\uf0a1", "Alarmas"),
                navItem("\uf1da", "Historial"),
                navItem("\uf279", "Mapas"),
                navItem("\uf0e0", "Notificaciones"),
                navItem("\uf080", "Reportes"),
                navItem("\uf007", "Mi perfil")
        );

        VBox spacer = new VBox();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Logout
        HBox logout = new HBox(10);
        logout.setPadding(new Insets(14, 8, 18, 14));
        logout.setAlignment(Pos.CENTER_LEFT);
        logout.setCursor(javafx.scene.Cursor.HAND);
        logout.setStyle("-fx-background-color: transparent;");
        logout.setOnMouseEntered(e -> logout.setStyle(
                "-fx-background-color: rgba(229,57,53,0.15); -fx-background-radius: 8;"));
        logout.setOnMouseExited(e -> logout.setStyle(
                "-fx-background-color: transparent;"));
        logout.setOnMouseClicked(e -> cerrarSesion());

        Label logoutIcon = new Label("\uf2f5");
        logoutIcon.setStyle(
                "-fx-font-family: 'Font Awesome 6 Free Solid';"
                + "-fx-font-size: 14px;"
                + "-fx-text-fill: " + RED + ";");
        Label logoutText = label("Cerrar sesión", 13, WHITE, true);
        logoutText.setVisible(false);
        logoutText.setManaged(false);
        logout.getChildren().addAll(logoutIcon, logoutText);

        HBox profileCard = buildProfileCard();

        sidebar.getChildren().addAll(logoBox, profileCard, nav, spacer, logout);

        // ── Hover: expandir / colapsar ────────────────────────────
        sidebar.setOnMouseEntered(e -> setSidebarExpanded(true));
        sidebar.setOnMouseExited(e -> setSidebarExpanded(false));

        return sidebar;
    }

// ── Animación de expansión/colapso ───────────────────────────
    private void setSidebarExpanded(boolean expand) {
        sidebarExpanded = expand;
        if (logoTextBox != null) {
            logoTextBox.setVisible(expand);
            logoTextBox.setManaged(expand);
        }

        double targetWidth = expand ? SIDEBAR_EXPANDED : SIDEBAR_COLLAPSED;

        // Animación de ancho con Timeline
        javafx.animation.Timeline tl = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(180),
                        new javafx.animation.KeyValue(
                                sidebar.prefWidthProperty(), targetWidth,
                                javafx.animation.Interpolator.EASE_BOTH),
                        new javafx.animation.KeyValue(
                                sidebar.minWidthProperty(), targetWidth,
                                javafx.animation.Interpolator.EASE_BOTH),
                        new javafx.animation.KeyValue(
                                sidebar.maxWidthProperty(), targetWidth,
                                javafx.animation.Interpolator.EASE_BOTH)
                )
        );
        tl.play();

        // Mostrar/ocultar textos de todos los nodos del nav
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

        // Mostrar/ocultar textos en logoBox, profileCard, logout
        toggleTextChildren(sidebar, expand);
    }

// Recorre todos los HBox/VBox del sidebar y oculta Labels de texto
    private void toggleTextChildren(javafx.scene.Parent parent, boolean show) {
        parent.getChildrenUnmodifiable().forEach(node -> {
            if (node instanceof Label lbl) {
                // Solo labels de texto (no los de iconos FA)
                if (!lbl.getStyle().contains("Font Awesome")) {
                    lbl.setVisible(show);
                    lbl.setManaged(show);
                }
            } else if (node instanceof javafx.scene.Parent p
                    && !(node instanceof VBox vb && vb == nav)) {
                // Recursivo, pero no entra al nav (ya se manejó arriba)
                toggleTextChildren(p, show);
            }
        });
    }

    // ── Profile card ─────────────────────────────────────────────
    private HBox buildProfileCard() {
        HBox card = new HBox(10);

        card.setPadding(new Insets(10, 8, 10, 14));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 12;");
        Circle av = new Circle(18, Color.web("#1f3a56"));
        Label icon = new Label("\uf505");
        icon.setStyle(
                "-fx-font-family: 'Font Awesome 6 Free Solid';"
                + "-fx-font-size: 15px;"
                + "-fx-text-fill: #a8c0dd;");
        StackPane avBox = new StackPane(av, icon);
        avBox.setPrefSize(36, 36);   // ← tamaño explícito
        avBox.setMinSize(36, 36);
        avBox.setMaxSize(36, 36);
        VBox info = new VBox(2);
        String nombre = usuarioActual != null
                ? trim(usuarioActual.getPrimer_nombre()) + " " + trim(usuarioActual.getPrimer_apellido())
                : "Policía";
        String rango = policiaActual != null && policiaActual.getRango() != null
                ? policiaActual.getRango() : "Oficial";
        String placa = policiaActual != null && policiaActual.getPlaca() != null
                ? "Placa: " + policiaActual.getPlaca() : "";

        HBox statusRow = new HBox(4);
        statusRow.setAlignment(Pos.CENTER_LEFT);
        statusRow.getChildren().addAll(
                new Circle(4, Color.web(GREEN)),
                label("En servicio", 10, GREEN, false));

        info.getChildren().addAll(
                label(nombre.trim(), 12, WHITE, true),
                label(rango, 9, "#8899bb", false),
                label(placa, 9, "#6a8cbb", false),
                statusRow);
        card.getChildren().addAll(avBox, info);
        return card;
    }

    // ── Nav item ─────────────────────────────────────────────────
    private HBox navItem(String icon, String text) {
        HBox item = new HBox(10);
        item.setPadding(new Insets(9, 8, 9, 14));
        item.setAlignment(Pos.CENTER_LEFT);
        item.setCursor(javafx.scene.Cursor.HAND);
        item.setMaxWidth(Double.MAX_VALUE);
        item.setStyle("-fx-background-radius: 8;");
        Label iconLbl = new Label(icon);
        iconLbl.setStyle(
                "-fx-font-family: 'Font Awesome 6 Free Solid';"
                + "-fx-font-size: 14px;"
                + "-fx-text-fill: #8899bb;");
        iconLbl.setMinWidth(28);
        iconLbl.setAlignment(Pos.CENTER);
        Label textLbl = label(text, 13, "#f8fafc", false);

        item.getChildren().addAll(iconLbl, textLbl);

        item.setOnMouseEntered(e -> item.setStyle(
                "-fx-background-color: rgba(255,255,255,0.15); -fx-background-radius: 8;"));
        item.setOnMouseExited(e -> item.setStyle("-fx-background-radius: 8;"));

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
                                        + "-fx-font-size: 14px;"
                                        + "-fx-text-fill: #8899bb;");
                            } else {
                                lbl.setTextFill(Color.web("#f8fafc"));
                            }
                        }
                    });
                }
            });

            // Activar ítem clickeado
            item.setStyle("-fx-background-color: rgba(255,255,255,0.20); -fx-background-radius: 8;");
            iconLbl.setStyle(
                    "-fx-font-family: 'Font Awesome 6 Free Solid';"
                    + "-fx-font-size: 14px;"
                    + "-fx-text-fill: white;");
            textLbl.setTextFill(Color.WHITE);

            switch (text) {
                case "Centro de operaciones" ->
                    root.setCenter(new CentroOperacionesPoliciaView(
                            usuarioActual, policiaActual,
                            alertaService, atencionService,
                            alarmaService, notificacionService, tipoArmaService, medioTransporteService, root).build());
                case "Mis alertas" ->
                    root.setCenter(new MisAlertasPoliciaView(
                            usuarioActual, policiaActual,
                            alertaService, atencionService, tipoArmaService, medioTransporteService, root).build());
                case "Mis atenciones" ->
                    root.setCenter(new MisAtencionesPoliciaView(
                            usuarioActual, policiaActual,
                            atencionService, root).build());
                case "Alarmas" ->
                    root.setCenter(new AlarmasPoliciaView(alarmaService, policiaActual).build());
                case "Historial" ->
                    root.setCenter(new HistorialPoliciaView(
                            usuarioActual, policiaActual,
                            policiaService, root).build());
                case "Mapas" ->
                    root.setCenter(new MapaOperaciones(asignacionService, unidadService).build());

                case "Notificaciones" ->
                    root.setCenter(new NotificacionesPoliciaView(
                            usuarioActual, policiaActual, notificacionService).build());
                case "Reportes" ->
                    root.setCenter(new ReportesPoliciaView(
                            policiaActual,
                            atencionService,
                            asignacionService,
                            alertaService,
                            notificacionService,
                            alarmaService
                    ).build());


                case "Mi perfil" ->
                    root.setCenter(new PerfilPoliciaView(
                            usuarioActual, policiaActual).build());
            }
        });
        return item;
    }

    // =========================================================================
    // CERRAR SESIÓN
    // =========================================================================
    private void cerrarSesion() {
        ((Stage) root.getScene().getWindow()).close();
    }

    // ── Helpers ──────────────────────────────────────────────────
    private Label label(String text, double size, String color, boolean bold) {
        Label lbl = new Label(text);
        lbl.setFont(bold
                ? Font.font("System", FontWeight.BOLD, size)
                : Font.font("System", size));
        lbl.setTextFill(Color.web(color));
        return lbl;
    }

    private String trim(String s) {
        return s != null ? s.trim() : "";
    }

    private void mostrarAlerta(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
