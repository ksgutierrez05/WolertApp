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
import sistemagestion.model.Suscripcion;
import sistemagestion.model.Usuario;
import sistemagestion.service.BarrioService;
import sistemagestion.service.SuscripcionService;
import sistemagestion.service.UsuarioService;

public class MiCuentaView {

    private static final String WHITE = "#ffffff";
    private static final String BG = "#f4f6fb";
    private static final String RED = "#e53935";
    private static final String ORANGE = "#fb8c00";
    private static final String GREEN = "#43a047";
    private static final String BLUE = "#1565c0";
    private static final String BLUE_LIGHT = "#e8f0fe";
    private static final String GRAY_TEXT = "#6b7280";
    private static final String BORDER = "#e5e7eb";

    private final Usuario usuarioActual;
    private final SuscripcionService suscripcionService;
    private final BorderPane root;
    private final Runnable onVolver;
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
    // PUNTO DE ENTRADA
    // =========================================================================
    public ScrollPane getView() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(22));
        content.setStyle("-fx-background-color: " + BG + ";");
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
        scroll.setStyle("-fx-background: " + BG + "; -fx-background-color: " + BG + ";");
        return scroll;
    }

    // =========================================================================
    // HEADER
    // =========================================================================
    private HBox buildHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        VBox titles = new VBox(4);
        Label title = new Label("Mi Cuenta");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#111827"));
        Label sub = label("Gestiona tu perfil, suscripciones e información útil", 13, GRAY_TEXT, false);
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
                "-fx-background-color: white;"
                + "-fx-background-radius: 16;"
                + "-fx-border-color: " + BORDER + ";"
                + "-fx-border-radius: 16;"
                + "-fx-border-width: 1;");
        shadow(card);

        // Avatar
        StackPane avatarBox = new StackPane();
        Circle avatarCircle = new Circle(38, Color.web(BLUE));
        Label inicialesLbl = new Label(iniciales());
        inicialesLbl.setFont(Font.font("System", FontWeight.BOLD, 22));
        inicialesLbl.setTextFill(Color.WHITE);
        avatarBox.getChildren().addAll(avatarCircle, inicialesLbl);

        // Info
        VBox info = new VBox(6);
        HBox.setHgrow(info, Priority.ALWAYS);

        String nombre = usuarioActual != null
                ? ((usuarioActual.getPrimer_nombre() != null ? usuarioActual.getPrimer_nombre() : "")
                        + " " + (usuarioActual.getPrimer_apellido() != null ? usuarioActual.getPrimer_apellido() : "")).trim()
                : "Usuario";
        Label nombreLbl = new Label(nombre.isEmpty() ? "Usuario" : nombre);
        nombreLbl.setFont(Font.font("System", FontWeight.BOLD, 18));
        nombreLbl.setTextFill(Color.web("#111827"));

        String rol = usuarioActual != null && usuarioActual.getRol() != null
                ? usuarioActual.getRol().getNombre() : "—";
        Label rolLbl = new Label(rol);
        rolLbl.setStyle(
                "-fx-background-color: " + BLUE_LIGHT + ";"
                + "-fx-text-fill: " + BLUE + ";"
                + "-fx-font-size: 11px; -fx-font-weight: bold;"
                + "-fx-background-radius: 20; -fx-padding: 4 12;");

        String correo = usuarioActual != null && usuarioActual.getCorreo() != null
                ? usuarioActual.getCorreo() : "—";
        Label correoLbl = label(correo, 12, GRAY_TEXT, false);

        String username = usuarioActual != null && usuarioActual.getUsername() != null
                ? "@" + usuarioActual.getUsername() : "";
        Label userLbl = label(username, 12, GRAY_TEXT, false);

        HBox statusRow = new HBox(6);
        statusRow.setAlignment(Pos.CENTER_LEFT);
        Circle dot = new Circle(4, Color.web(GREEN));
        Label statusLbl = label("En línea", 11, GREEN, false);
        statusRow.getChildren().addAll(dot, statusLbl);

        info.getChildren().addAll(nombreLbl, rolLbl, correoLbl, userLbl, statusRow);

        // Botón editar
        Button editBtn = new Button("Editar perfil");
        // ── DESPUÉS ──
        String editBase = "-fx-background-color: #1f3a56;"
                + "-fx-text-fill: white;"
                + "-fx-font-size: 12px; -fx-font-weight: bold;"
                + "-fx-background-radius: 8; -fx-padding: 9 18; -fx-cursor: hand;";
        String editHover = "-fx-background-color: #16283d;"
                + "-fx-text-fill: white;"
                + "-fx-font-size: 12px; -fx-font-weight: bold;"
                + "-fx-background-radius: 8; -fx-padding: 9 18; -fx-cursor: hand;";
        editBtn.setStyle(editBase);
        editBtn.setOnMouseEntered(e -> editBtn.setStyle(editHover));
        editBtn.setOnMouseExited(e -> editBtn.setStyle(editBase));
        editBtn.setOnAction(e -> abrirDialogoEditar());

        card.getChildren().addAll(avatarBox, info, editBtn);
        return card;
    }

    // =========================================================================
    // PANEL: INFORMACIÓN PERSONAL
    // =========================================================================
    private VBox buildPerfilPanel() {
        VBox panel = createPanel("Información personal");

        if (usuarioActual == null) {
            panel.getChildren().add(label("No hay información disponible.", 13, GRAY_TEXT, false));
            return panel;
        }

        String barrio = usuarioActual.getDireccion() != null
                && usuarioActual.getDireccion().getBarrio() != null
                ? usuarioActual.getDireccion().getBarrio().getNombre() : "—";
        String calle = usuarioActual.getDireccion() != null
                && usuarioActual.getDireccion().getCalle() != null
                ? usuarioActual.getDireccion().getCalle() : "—";
        String carrera = usuarioActual.getDireccion() != null
                && usuarioActual.getDireccion().getCarrera() != null
                ? usuarioActual.getDireccion().getCarrera() : "—";

        panel.getChildren().addAll(
                infoRow("Identificación", val(usuarioActual.getIdentificacion()), BLUE),
                sep(),
                infoRow("Teléfono", val(usuarioActual.getTelefono()), GRAY_TEXT),
                sep(),
                infoRow("Correo", val(usuarioActual.getCorreo()), GRAY_TEXT),
                sep(),
                infoRow("Username", val(usuarioActual.getUsername()), GRAY_TEXT),
                sep(),
                infoRow("Barrio", barrio, GRAY_TEXT),
                sep(),
                infoRow("Dirección", "Calle " + calle + "  Cra " + carrera, GRAY_TEXT)
        );
        return panel;
    }

    // =========================================================================
    // PANEL: MIS SUSCRIPCIONES
    // =========================================================================
    private VBox buildSuscripcionesPanel() {
        VBox panel = createPanel("Mis suscripciones");

        if (suscripcionService == null || usuarioActual == null) {
            panel.getChildren().add(label("Servicio no disponible.", 13, GRAY_TEXT, false));
            return panel;
        }

        try {
            List<Suscripcion> lista = suscripcionService.listar().stream()
                    .filter(s -> s.getUsuario() != null
                    && usuarioActual.getIdentificacion() != null
                    && usuarioActual.getIdentificacion()
                            .equals(s.getUsuario().getIdentificacion()))
                    .toList();

            if (lista.isEmpty()) {
                VBox vacio = new VBox(8);
                vacio.setAlignment(Pos.CENTER);
                vacio.setPadding(new Insets(20));
                vacio.getChildren().add(label("No tienes suscripciones activas.", 13, GRAY_TEXT, false));
                panel.getChildren().add(vacio);
            } else {
                for (Suscripcion s : lista) {
                    String tipo = s.getTipoalerta() != null ? s.getTipoalerta().getNombre() : "—";
                    String zona = s.getBarrio() != null ? s.getBarrio().getNombre()
                            : s.getComuna() != null ? s.getComuna().getNombre() : "—";
                    String estado = s.getEstado() != null
                            ? s.getEstado().name().replace("_", " ") : "—";
                    String badgeColor = switch (s.getEstado() != null ? s.getEstado().name() : "") {
                        case "ACTIVA" ->
                            GREEN;
                        case "PAUSADA" ->
                            ORANGE;
                        case "CANCELADA" ->
                            RED;
                        default ->
                            GRAY_TEXT;
                    };
                    panel.getChildren().addAll(infoRowBadge(tipo, zona, estado, badgeColor), sep());
                }
            }
        } catch (Exception e) {
            panel.getChildren().add(label("Error: " + e.getMessage(), 12, RED, false));
        }
        return panel;
    }

    // =========================================================================
    // PANEL: INFORMACIÓN ÚTIL
    // =========================================================================
    private VBox buildInfoUtilPanel() {
        VBox panel = createPanel("Información útil");
        panel.getChildren().addAll(
                infoRow("Línea de emergencias", "123", RED),
                sep(),
                infoRow("Policía Nacional", "112", BLUE),
                sep(),
                infoRow("Bomberos", "119", ORANGE),
                sep(),
                infoRow("Cruz Roja", "132", RED),
                sep(),
                infoRow("Línea de denuncia", "018000910600", GRAY_TEXT),
                sep(),
                infoRow("Soporte WolertApp", "wolertapp.notificaciones@gmail.com", BLUE)
        );
        return panel;
    }

    // =========================================================================
    // DIÁLOGO EDITAR PERFIL
    // — Solo: nombre, apellido, teléfono, correo, barrio
    // =========================================================================
    private void abrirDialogoEditar() {
        if (usuarioActual == null) {
            return;
        }

        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle("Editar perfil");
        dlg.setHeaderText(null);

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setPrefSize(440, 420);
        scroll.setStyle("-fx-background:white;-fx-background-color:white;");

        VBox form = new VBox(12);
        form.setPadding(new Insets(20));
        form.setStyle("-fx-background-color:white;");

        TextField fNombre = dlgField("Primer nombre", val(usuarioActual.getPrimer_nombre()));
        TextField fApellido = dlgField("Primer apellido", val(usuarioActual.getPrimer_apellido()));
        TextField fTelefono = dlgField("Teléfono", val(usuarioActual.getTelefono()));
        TextField fCorreo = dlgField("Correo", val(usuarioActual.getCorreo()));

        // Dirección
        String calleActual = (usuarioActual.getDireccion() != null && usuarioActual.getDireccion().getCalle() != null) ? usuarioActual.getDireccion().getCalle() : "";
        String carreraActual = (usuarioActual.getDireccion() != null && usuarioActual.getDireccion().getCarrera() != null) ? usuarioActual.getDireccion().getCarrera() : "";
        TextField fCalle = dlgField("Calle", calleActual);
        TextField fCarrera = dlgField("Carrera", carreraActual);

        // Selector de barrio
        ComboBox<String> cbBarrio = new ComboBox<>();
        cbBarrio.setPromptText("Selecciona tu barrio");
        cbBarrio.setMaxWidth(Double.MAX_VALUE);
        cbBarrio.setPrefHeight(40);
        cbBarrio.setStyle("-fx-background-color:#f5f7fb;-fx-background-radius:8;"
                + "-fx-border-color:transparent;-fx-font-size:13px;");

        List<Barrio> barrios = List.of();
        try {
            if (barrioService != null) {
                barrios = barrioService.listar();
            }
        } catch (Exception ignored) {
        }

        for (Barrio b : barrios) {
            cbBarrio.getItems().add(b.getNombre());
        }

        // Preseleccionar barrio actual — con null check
        try {
            if (usuarioActual.getDireccion() != null
                    && usuarioActual.getDireccion().getBarrio() != null
                    && usuarioActual.getDireccion().getBarrio().getNombre() != null) {
                cbBarrio.setValue(usuarioActual.getDireccion().getBarrio().getNombre());
            }
        } catch (Exception ignored) {
        }

        Label lblError = label("", 12, RED, false);
        lblError.setWrapText(true);

        form.getChildren().addAll(
                seccion("NOMBRE"),
                fNombre, fApellido,
                seccion("DATOS DE CONTACTO"),
                fTelefono, fCorreo,
                seccion("DIRECCIÓN"),
                fCalle, fCarrera,
                seccion("BARRIO"),
                cbBarrio,
                lblError
        );

        scroll.setContent(form);
        dlg.getDialogPane().setContent(scroll);
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button btnOk = (Button) dlg.getDialogPane().lookupButton(ButtonType.OK);
        btnOk.setText("Guardar cambios");
        btnOk.setStyle("-fx-background-color:#1f3a56;-fx-text-fill:white;"
                + "-fx-font-weight:bold;-fx-background-radius:8;-fx-padding:8 16;");

        final List<Barrio> barriosFinal = barrios;

        btnOk.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            lblError.setText("");
            try {
                if (!fNombre.getText().isBlank()) {
                    usuarioActual.setPrimer_nombre(fNombre.getText().trim());
                }
                if (!fApellido.getText().isBlank()) {
                    usuarioActual.setPrimer_apellido(fApellido.getText().trim());
                }
                if (!fTelefono.getText().isBlank()) {
                    usuarioActual.setTelefono(fTelefono.getText().trim());
                }
                if (!fCorreo.getText().isBlank()) {
                    usuarioActual.setCorreo(fCorreo.getText().trim());
                }

                // Actualizar dirección — con null check
                if (usuarioActual.getDireccion() != null) {
                    if (!fCalle.getText().isBlank()) {
                        usuarioActual.getDireccion().setCalle(fCalle.getText().trim());
                    }
                    if (!fCarrera.getText().isBlank()) {
                        usuarioActual.getDireccion().setCarrera(fCarrera.getText().trim());
                    }

                    String barrioSel = cbBarrio.getValue();
                    if (barrioSel != null) {
                        barriosFinal.stream()
                                .filter(b -> b.getNombre().equals(barrioSel))
                                .findFirst()
                                .ifPresent(b -> usuarioActual.getDireccion().setBarrio(b));
                    }
                }

                if (usuarioService != null) {
                    usuarioService.actualizar(usuarioActual);
                    if (root != null) {
                        root.setCenter(getView());
                    }
                }
            } catch (Exception ex) {
                lblError.setText("Error: " + ex.getMessage());
                ev.consume();
            }
        });

        dlg.showAndWait();
    }

    // =========================================================================
    // HELPERS UI
    // =========================================================================
    private VBox createPanel(String title) {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(16));
        panel.setStyle("-fx-background-color: white; -fx-background-radius: 12;");
        shadow(panel);

        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Rectangle accentBar = new Rectangle(4, 20);
        accentBar.setFill(Color.web(BLUE));
        accentBar.setArcWidth(4);
        accentBar.setArcHeight(4);
        Label t = new Label(title);
        t.setFont(Font.font("System", FontWeight.BOLD, 14));
        t.setTextFill(Color.web("#111827"));
        titleRow.getChildren().addAll(accentBar, t);

        panel.getChildren().addAll(titleRow, sep());
        return panel;
    }

    private HBox infoRow(String campo, String valor, String valorColor) {
        HBox row = new HBox(0);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(9, 0, 9, 0));
        Label key = label(campo, 12, GRAY_TEXT, false);
        key.setMinWidth(160);
        key.setMaxWidth(160);
        Label val = label(valor, 13, "#111827", false);
        HBox.setHgrow(val, Priority.ALWAYS);
        Circle dot = new Circle(4, Color.web(valorColor));
        row.getChildren().addAll(key, val, dot);
        return row;
    }

    private HBox infoRowBadge(String campo, String valor, String badge, String badgeColor) {
        HBox row = new HBox(0);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(9, 0, 9, 0));
        Label key = label(campo, 12, GRAY_TEXT, false);
        key.setMinWidth(160);
        key.setMaxWidth(160);
        Label val = label(valor, 13, "#111827", false);
        HBox.setHgrow(val, Priority.ALWAYS);
        Label badgeLbl = new Label(badge);
        badgeLbl.setStyle(
                "-fx-background-color:" + badgeColor + "22;"
                + "-fx-text-fill:" + badgeColor + ";"
                + "-fx-font-size:11px;-fx-font-weight:bold;"
                + "-fx-background-radius:20;-fx-padding:4 10;");
        row.getChildren().addAll(key, val, badgeLbl);
        return row;
    }

    private TextField dlgField(String prompt, String val) {
        TextField f = new TextField(val);
        f.setPromptText(prompt);
        f.setPrefHeight(40);
        f.setMaxWidth(Double.MAX_VALUE);
        f.setStyle("-fx-background-color:#f5f7fb;-fx-background-radius:8;"
                + "-fx-border-color:transparent;-fx-padding:0 14;-fx-font-size:13px;");
        return f;
    }

    private Label seccion(String txt) {
        Label l = label(txt, 10, "#9ca3af", true);
        VBox.setMargin(l, new Insets(8, 0, 2, 0));
        return l;
    }

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

    private String val(String v) {
        return v != null && !v.isBlank() ? v : "—";
    }

    private Region sep() {
        Region r = new Region();
        r.setPrefHeight(1);
        r.setStyle("-fx-background-color: " + BORDER + ";");
        return r;
    }

    private void shadow(Region node) {
        node.setEffect(new DropShadow(10, 0, 2, Color.web("#0000001a")));
    }

    private Label label(String text, double size, String color, boolean bold) {
        Label lbl = new Label(text);
        lbl.setFont(bold ? Font.font("System", FontWeight.BOLD, size) : Font.font("System", size));
        lbl.setTextFill(Color.web(color));
        return lbl;
    }
}
