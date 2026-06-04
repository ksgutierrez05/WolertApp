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
                alarmaService, notificacionService, root).build());
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

        ImageView logoImg = new ImageView(
                new Image(getClass().getResourceAsStream("/LogoWolertAPP.png")));
        logoImg.setFitWidth(65);
        logoImg.setFitHeight(65);
        logoImg.setPreserveRatio(true);
        logoImg.setTranslateY(-2);

        VBox logoText = new VBox(2);
        logoText.getChildren().addAll(
                label("WolertApp", 15, WHITE, true),
                label("Portal Policía", 9, "#8899bb", false));

        logoBox.getChildren().addAll(new StackPane(logoImg), logoText);

        // Nav
        VBox nav = new VBox(2);
        nav.setPadding(new Insets(12, 8, 12, 8));
        nav.getChildren().addAll(
                navItem("\uf015", "Centro de operaciones"),
                navItem("\uf0f3", "Mis alertas"),
                navItem("\uf46d", "Mis atenciones"),
                navItem("\uf0f3", "Alarmas"),
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
        logout.setPadding(new Insets(14, 16, 18, 16));
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
        logout.getChildren().addAll(logoutIcon, label("Cerrar sesión", 13, WHITE, true));

        sidebar.getChildren().addAll(logoBox, buildProfileCard(), nav, spacer, logout);
        return sidebar;
    }

    // ── Profile card ─────────────────────────────────────────────
    private HBox buildProfileCard() {
        HBox card = new HBox(10);
        card.setPadding(new Insets(10, 16, 10, 16));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 12;");

        Circle av = new Circle(20, Color.web("#334155"));
        StackPane avBox = new StackPane(av, label("👮", 15, WHITE, false));

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
        item.setPadding(new Insets(9, 12, 9, 12));
        item.setAlignment(Pos.CENTER_LEFT);
        item.setCursor(javafx.scene.Cursor.HAND);
        item.setMaxWidth(Double.MAX_VALUE);
        item.setStyle("-fx-background-radius: 8;");

        Label iconLbl = new Label(icon);
        iconLbl.setStyle(
                "-fx-font-family: 'Font Awesome 6 Free Solid';"
                + "-fx-font-size: 14px;"
                + "-fx-text-fill: #8899bb;");
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
                            alarmaService, notificacionService, root).build());
                case "Mis alertas" ->
                    root.setCenter(new MisAlertasPoliciaView(
                            usuarioActual, policiaActual,
                            alertaService, atencionService, root).build());
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
                    root.setCenter(pantallaEnConstruccion("\uf279", "Mapas"));
                case "Notificaciones" ->
                    root.setCenter(new NotificacionesPoliciaView(
                            usuarioActual, notificacionService).build());
                case "Reportes" -> {
                    try {
                        root.setCenter(
                                new ReportesAdminView(
                                        new sistemagestion.service.UsuarioService(),
                                        new sistemagestion.service.AlertaService()
                                ).build()
                        );
                    } catch (Exception ex) {
                        mostrarAlerta("Error en Reportes", ex.getMessage());
                    }
                }
                case "Mi perfil" ->
                    root.setCenter(new PerfilPoliciaView(
                            usuarioActual, policiaActual).build());
            }
        });
        return item;
    }

    // =========================================================================
    // PANTALLA EN CONSTRUCCIÓN
    // =========================================================================
    private VBox pantallaEnConstruccion(String icono, String nombreVista) {
        VBox pane = new VBox(20);
        pane.setAlignment(Pos.CENTER);
        pane.setStyle("-fx-background-color: " + BG + ";");

        Label iconLbl = new Label(icono);
        iconLbl.setStyle(
                "-fx-font-family: 'Font Awesome 6 Free Solid';"
                + "-fx-font-size: 64px;"
                + "-fx-text-fill: #fb8c00;");

        Label titleLbl = new Label(nombreVista);
        titleLbl.setFont(Font.font("System", FontWeight.BOLD, 26));
        titleLbl.setTextFill(Color.web("#111827"));

        Label msgLbl = new Label("🚧  Esta sección está en construcción");
        msgLbl.setFont(Font.font("System", FontWeight.BOLD, 16));
        msgLbl.setTextFill(Color.web("#fb8c00"));
        msgLbl.setPadding(new Insets(10, 24, 10, 24));
        msgLbl.setStyle("-fx-background-color: #fff3e0; -fx-background-radius: 10;");

        Label subLbl = new Label("Próximamente disponible. Por ahora puedes usar las demás secciones.");
        subLbl.setFont(Font.font("System", 13));
        subLbl.setTextFill(Color.web(GRAY_TEXT));

        pane.getChildren().addAll(iconLbl, titleLbl, msgLbl, subLbl);
        return pane;
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
