/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.view;

/**
 *
 * @author Maria Cristina
 */
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import sistemagestion.model.Barrio;
import sistemagestion.model.Direccion;
import sistemagestion.model.Suscripcion;
import sistemagestion.model.Usuario;
import sistemagestion.service.BarrioService;
import sistemagestion.service.SuscripcionService;
import sistemagestion.service.UsuarioService;

/**
 * Vista principal de "Mi Cuenta".
 *
 * Responsabilidad (GRASP — Controller + Alta Cohesión): Solo orquesta los
 * paneles y delega construcción de diálogos y componentes a clases
 * especializadas.
 */
public class MiCuentaView {

    // ── Constantes de color (Information Expert: solo esta clase las expone) ──
    static final String WHITE = "#ffffff";
    static final String BG = "#f4f6fb";
    static final String RED = "#e53935";
    static final String ORANGE = "#fb8c00";
    static final String GREEN = "#43a047";
    static final String BLUE = "#1565c0";
    static final String BLUE_LIGHT = "#e8f0fe";
    static final String GRAY_TEXT = "#6b7280";
    static final String BORDER = "#e5e7eb";

    private final Usuario usuarioActual;
    private final SuscripcionService suscripcionService;
    private final BorderPane root;
    private final Runnable onVolver;

    // Servicios (Creator: MiCuentaView los instancia porque los necesita)
    private UsuarioService usuarioService;
    private BarrioService barrioService;

    public MiCuentaView(Usuario usuarioActual,
            SuscripcionService suscripcionService,
            BorderPane root,
            Runnable onVolver) {
        this.usuarioActual = usuarioActual;
        this.suscripcionService = suscripcionService;
        this.root = root;
        this.onVolver = onVolver;

        Font.loadFont(getClass().getResourceAsStream("/fa-solid-900.ttf"), 20);

        try {
            usuarioService = new UsuarioService();
        } catch (Exception e) {
            System.out.println("Error UsuarioService: " + e.getMessage());
        }

        try {
            barrioService = new BarrioService();
        } catch (Exception e) {
            System.out.println("Error BarrioService: " + e.getMessage());
        }
    }

    // =========================================================================
    // PUNTO DE ENTRADA — Controller: coordina sin construir detalles
    // =========================================================================
    public ScrollPane getView() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(22));
        content.setStyle("-fx-background-color:" + BG + ";");
        content.getChildren().addAll(
                buildHeader(),
                buildTarjetaPerfil(),
                buildPerfilPanel(),
                buildSuscripcionesPanel(),
                buildInfoUtilPanel()
        );
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background:" + BG + "; -fx-background-color:" + BG + ";");
        return scroll;
    }

    // =========================================================================
    // HEADER
    // =========================================================================
    private HBox buildHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        VBox titles = new VBox(4);
        Label title = MiCuentaUIFactory.label("Mi Cuenta", 24, "#111827", true);
        Label sub = MiCuentaUIFactory.label("Gestiona tu perfil, suscripciones e información útil", 13, GRAY_TEXT, false);
        titles.getChildren().addAll(title, sub);
        header.getChildren().add(titles);
        return header;
    }

    // =========================================================================
    // TARJETA DE PERFIL
    // =========================================================================
    private HBox buildTarjetaPerfil() {
        HBox card = new HBox(20);
        card.setPadding(new Insets(22, 24, 22, 24));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle(
                "-fx-background-color:white;"
                + "-fx-background-radius:16;"
                + "-fx-border-color:" + BORDER + ";"
                + "-fx-border-radius:16;"
                + "-fx-border-width:1;");
        MiCuentaUIFactory.shadow(card);

        // Avatar con iniciales
        StackPane avatarBox = new StackPane();
        Circle avatarCircle = new Circle(38, Color.web(BLUE));
        Label inicialesLbl = MiCuentaUIFactory.label(iniciales(), 22, WHITE, true);
        avatarBox.getChildren().addAll(avatarCircle, inicialesLbl);

        // Info del usuario
        VBox info = new VBox(6);
        HBox.setHgrow(info, Priority.ALWAYS);

        String nombre = usuarioActual != null
                ? ((usuarioActual.getPrimer_nombre() != null ? usuarioActual.getPrimer_nombre() : "")
                        + " "
                        + (usuarioActual.getPrimer_apellido() != null ? usuarioActual.getPrimer_apellido() : "")).trim()
                : "Usuario";
        Label nombreLbl = MiCuentaUIFactory.label(nombre.isEmpty() ? "Usuario" : nombre, 18, "#111827", true);

        String rol = usuarioActual != null && usuarioActual.getRol() != null
                ? usuarioActual.getRol().getNombre() : "—";
        Label rolLbl = new Label(rol);
        rolLbl.setStyle(
                "-fx-background-color:" + BLUE_LIGHT + ";"
                + "-fx-text-fill:" + BLUE + ";"
                + "-fx-font-size:11px;-fx-font-weight:bold;"
                + "-fx-background-radius:20;-fx-padding:4 12;");

        String correo = usuarioActual != null && usuarioActual.getCorreo() != null ? usuarioActual.getCorreo() : "—";
        String username = usuarioActual != null && usuarioActual.getUsername() != null ? "@" + usuarioActual.getUsername() : "";

        HBox statusRow = new HBox(6);
        statusRow.setAlignment(Pos.CENTER_LEFT);
        statusRow.getChildren().addAll(
                new Circle(4, Color.web(GREEN)),
                MiCuentaUIFactory.label("En línea", 11, GREEN, false));

        info.getChildren().addAll(
                nombreLbl, rolLbl,
                MiCuentaUIFactory.label(correo, 12, GRAY_TEXT, false),
                MiCuentaUIFactory.label(username, 12, GRAY_TEXT, false),
                statusRow);

        // Botón editar — delega apertura al diálogo especializado
        Button editBtn = MiCuentaUIFactory.darkButton("Editar perfil");
        editBtn.setOnAction(e -> abrirDialogoEditar());

        card.getChildren().addAll(avatarBox, info, editBtn);
        return card;
    }

    // =========================================================================
    // PANEL: INFORMACIÓN PERSONAL
    // =========================================================================
    private VBox buildPerfilPanel() {
        VBox panel = MiCuentaUIFactory.createPanel("Información personal", BLUE);

        if (usuarioActual == null) {
            panel.getChildren().add(MiCuentaUIFactory.label("No hay información disponible.", 13, GRAY_TEXT, false));
            return panel;
        }

        Direccion dir = usuarioActual.getDireccion();
        String dirFinal = buildDireccionTexto(dir);
        String barrio = dir != null && dir.getBarrio() != null ? dir.getBarrio().getNombre() : "—";

        panel.getChildren().addAll(
                MiCuentaUIFactory.infoRow("Identificación", MiCuentaUIFactory.val(usuarioActual.getIdentificacion())), MiCuentaUIFactory.sep(BORDER),
                MiCuentaUIFactory.infoRow("Teléfono", MiCuentaUIFactory.val(usuarioActual.getTelefono())), MiCuentaUIFactory.sep(BORDER),
                MiCuentaUIFactory.infoRow("Correo", MiCuentaUIFactory.val(usuarioActual.getCorreo())), MiCuentaUIFactory.sep(BORDER),
                MiCuentaUIFactory.infoRow("Username", MiCuentaUIFactory.val(usuarioActual.getUsername())), MiCuentaUIFactory.sep(BORDER),
                MiCuentaUIFactory.infoRow("Barrio", barrio), MiCuentaUIFactory.sep(BORDER),
                MiCuentaUIFactory.infoRow("Dirección", dirFinal)
        );
        return panel;
    }

    /**
     * Information Expert: quien conoce la estructura de Dirección sabe cómo
     * construir su representación textual.
     */
    private String buildDireccionTexto(Direccion dir) {
        if (dir == null) {
            return "—";
        }
        StringBuilder sb = new StringBuilder();
        if (dir.getCalle() != null && !dir.getCalle().isBlank()) {
            sb.append("Calle ").append(dir.getCalle()).append("  ");
        }
        if (dir.getCarrera() != null && !dir.getCarrera().isBlank()) {
            sb.append("Cra ").append(dir.getCarrera()).append("  ");
        }
        if (dir.getEtapa() != null && !dir.getEtapa().isBlank()) {
            sb.append("Etapa ").append(dir.getEtapa()).append("  ");
        }
        if (dir.getManzana() != null && !dir.getManzana().isBlank()) {
            sb.append("Manzana ").append(dir.getManzana()).append("  ");
        }
        if (dir.getCasa() != null && !dir.getCasa().isBlank()) {
            sb.append("Casa ").append(dir.getCasa());
        }
        String result = sb.toString().trim();
        return result.isEmpty() ? "—" : result;
    }

    // =========================================================================
    // PANEL: MIS SUSCRIPCIONES
    // =========================================================================
    private VBox buildSuscripcionesPanel() {
        VBox panel = MiCuentaUIFactory.createPanel("Mis suscripciones", BLUE);

        if (suscripcionService == null || usuarioActual == null) {
            panel.getChildren().add(MiCuentaUIFactory.label("Servicio no disponible.", 13, GRAY_TEXT, false));
            return panel;
        }

        try {
            List<Suscripcion> lista = filtrarSuscripcionesDelUsuario(suscripcionService.listar());

            if (lista.isEmpty()) {
                VBox vacio = new VBox(8);
                vacio.setAlignment(Pos.CENTER);
                vacio.setPadding(new Insets(12, 0, 12, 0));
                vacio.getChildren().add(MiCuentaUIFactory.label("No tienes suscripciones activas.", 13, GRAY_TEXT, false));
                panel.getChildren().add(vacio);
            } else {
                for (Suscripcion s : lista) {
                    panel.getChildren().addAll(buildFilaSuscripcion(s), MiCuentaUIFactory.sep(BORDER));
                }
            }

            Button btnGestionar = MiCuentaUIFactory.lightButton("+ Gestionar suscripción", BLUE_LIGHT, BLUE);
            btnGestionar.setOnAction(e -> abrirDialogoSuscripcion(
                    filtrarSuscripcionesDelUsuario(suscripcionService.listar())));
            panel.getChildren().add(btnGestionar);

        } catch (Exception e) {
            panel.getChildren().add(MiCuentaUIFactory.label("Error: " + e.getMessage(), 12, RED, false));
        }
        return panel;
    }

    /**
     * Information Expert: la lógica de filtrado por usuario vive aquí, porque
     * MiCuentaView conoce tanto el usuario como las suscripciones.
     */
    private List<Suscripcion> filtrarSuscripcionesDelUsuario(List<Suscripcion> todas) {
        return todas.stream()
                .filter(s -> s.getUsuario() != null
                && usuarioActual.getIdentificacion() != null
                && usuarioActual.getIdentificacion().equals(s.getUsuario().getIdentificacion()))
                .toList();
    }

    private HBox buildFilaSuscripcion(Suscripcion s) {
        String zona = s.getBarrio() != null
                ? "Barrio: " + s.getBarrio().getNombre()
                : s.getComuna() != null
                ? "Comuna: " + s.getComuna().getNombre()
                : "General (toda la ciudad)";

        String estadoStr = s.getEstado() != null ? s.getEstado().name() : "—";
        String badgeColor = switch (estadoStr) {
            case "ACTIVA" ->
                GREEN;
            case "PAUSADA" ->
                ORANGE;
            case "CANCELADA" ->
                RED;
            default ->
                GRAY_TEXT;
        };

        Button btnEliminar = MiCuentaUIFactory.deleteButton("✕ Eliminar");
        final int idSusc = s.getId_suscripcion();
        btnEliminar.setOnAction(e -> confirmarEliminarSuscripcion(idSusc, zona));

        HBox fila = new HBox(10);
        fila.setAlignment(Pos.CENTER_LEFT);
        HBox infoSusc = MiCuentaUIFactory.infoRowBadge("Suscripción", zona, estadoStr, badgeColor);
        HBox.setHgrow(infoSusc, Priority.ALWAYS);
        fila.getChildren().addAll(infoSusc, btnEliminar);
        return fila;
    }

    /**
     * Controller: coordina la confirmación y refresca la vista al terminar.
     */
    private void confirmarEliminarSuscripcion(int idSusc, String zona) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Eliminar suscripción");
        confirm.setHeaderText(null);
        confirm.setContentText("¿Seguro que quieres eliminar esta suscripción?\n" + zona);
        confirm.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.OK) {
                boolean ok = suscripcionService.eliminar(idSusc);
                System.out.println(ok ? "✅ Suscripción eliminada: " + idSusc
                        : "❌ No se pudo eliminar: " + idSusc);
                if (root != null) {
                    root.setCenter(getView());
                }
            }
        });
    }

    // =========================================================================
    // PANEL: INFORMACIÓN ÚTIL
    // =========================================================================
    private VBox buildInfoUtilPanel() {
        VBox panel = MiCuentaUIFactory.createPanel("Información útil", BLUE);
        panel.getChildren().addAll(
                MiCuentaUIFactory.infoRowFA("\uf46a", "#e53935", "Línea de emergencias", "123"), MiCuentaUIFactory.sep(BORDER),
                MiCuentaUIFactory.infoRowFA("\uf505", "#1565c0", "Policía Nacional", "112"), MiCuentaUIFactory.sep(BORDER),
                MiCuentaUIFactory.infoRowFA("\uf46a", "#fb8c00", "Bomberos", "119"), MiCuentaUIFactory.sep(BORDER),
                MiCuentaUIFactory.infoRowFA("\uf0f1", "#43a047", "Cruz Roja", "132"), MiCuentaUIFactory.sep(BORDER),
                MiCuentaUIFactory.infoRowFA("\uf4a4", "#7b1fa2", "Línea de denuncia", "018000910600"), MiCuentaUIFactory.sep(BORDER),
                MiCuentaUIFactory.infoRowFA("\uf086", "#0288d1", "Soporte WolertApp", "wolertapp.notificaciones@gmail.com")
        );
        return panel;
    }

    // =========================================================================
    // APERTURA DE DIÁLOGOS — Creator: MiCuentaView crea los diálogos
    // porque los conoce y les pasa sus dependencias
    // =========================================================================
    private void abrirDialogoEditar() {
        new EditarPerfilDialog(usuarioActual, barrioService, usuarioService, () -> {
            if (root != null) {
                javafx.application.Platform.runLater(() -> root.setCenter(getView()));
            }
        }).show();
    }

    private void abrirDialogoSuscripcion(List<Suscripcion> actuales) {
        new GestionarSuscripcionDialog(usuarioActual, actuales, suscripcionService, barrioService, () -> {
            if (root != null) {
                javafx.application.Platform.runLater(() -> root.setCenter(getView()));
            }
        }).show();
    }

    // =========================================================================
    // HELPERS PRIVADOS
    // =========================================================================
    private String iniciales() {
        if (usuarioActual == null) {
            return "?";
        }
        String n = usuarioActual.getPrimer_nombre() != null && !usuarioActual.getPrimer_nombre().isBlank()
                ? usuarioActual.getPrimer_nombre().substring(0, 1).toUpperCase() : "?";
        String a = usuarioActual.getPrimer_apellido() != null && !usuarioActual.getPrimer_apellido().isBlank()
                ? usuarioActual.getPrimer_apellido().substring(0, 1).toUpperCase() : "";
        return n + a;
    }
}
