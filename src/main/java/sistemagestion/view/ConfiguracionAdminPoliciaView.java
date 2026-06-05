/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.view;

/**
 *
 * @author Maria Cristina
 */
import java.sql.SQLException;
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
import sistemagestion.dao.UsuarioDAO;
import sistemagestion.model.Usuario;

public class ConfiguracionAdminPoliciaView {

    private static final String BG        = "#f4f6fb";
    private static final String WHITE     = "#ffffff";
    private static final String BLUE      = "#1565c0";
    private static final String SIDEBAR_B = "#1f3a56";
    private static final String GREEN     = "#43a047";
    private static final String RED       = "#e53935";
    private static final String GRAY_TEXT = "#6b7280";
    private static final String BORDER    = "#e5e7eb";
    private static final String FA        = "'Font Awesome 6 Free Solid'";

    private final Usuario usuarioActual;

    // Campos editables
    private TextField tfNombre, tfCorreo, tfTelefono;

    public ConfiguracionAdminPoliciaView(Usuario usuarioActual) {
        this.usuarioActual = usuarioActual;
        Font.loadFont(getClass().getResourceAsStream("/fa-solid-900.ttf"), 20);
    }

    public ScrollPane build() {
        VBox content = new VBox(16);
        content.setPadding(new Insets(22));
        content.setStyle("-fx-background-color:" + BG + ";");

        // Título
        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label titleIcon = faIcon("\uf013", 20, BLUE);
        Label titleLbl = new Label("Configuración");
        titleLbl.setFont(Font.font("System", FontWeight.BOLD, 22));
        titleLbl.setTextFill(Color.web("#111827"));
        titleRow.getChildren().addAll(titleIcon, titleLbl);

        content.getChildren().addAll(titleRow, buildPanelPerfil());

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background:" + BG + "; -fx-background-color:" + BG + ";");
        return scroll;
    }

    // ── Panel de perfil editable ──────────────────────────────────
    private VBox buildPanelPerfil() {
        VBox panel = createPanel("\uf007", "Datos del perfil");

        if (usuarioActual == null) {
            panel.getChildren().add(label("No hay información de usuario disponible.", 13, GRAY_TEXT, false));
            return panel;
        }

        String nombreCompleto = join(
                usuarioActual.getPrimer_nombre(), usuarioActual.getSegundo_nombre(),
                usuarioActual.getPrimer_apellido(), usuarioActual.getSegundo_apellido());

        // Avatar + nombre
        HBox avatarRow = new HBox(14);
        avatarRow.setAlignment(Pos.CENTER_LEFT);
        avatarRow.setPadding(new Insets(6, 0, 10, 0));

        StackPane avatarBox = new StackPane();
        Circle avatar = new Circle(28, Color.web(SIDEBAR_B));
        Label avatarIco = faIcon("\uf505", 18, WHITE);
        avatarBox.getChildren().addAll(avatar, avatarIco);

        VBox avatarInfo = new VBox(3);
        Label nombreLbl = new Label(nombreCompleto.isEmpty() ? "Administrador Policía" : nombreCompleto);
        nombreLbl.setFont(Font.font("System", FontWeight.BOLD, 14));
        nombreLbl.setTextFill(Color.web("#111827"));

        HBox statusRow = new HBox(5);
        statusRow.setAlignment(Pos.CENTER_LEFT);
        statusRow.getChildren().addAll(
                new Circle(5, Color.web(GREEN)),
                label("En servicio · Administrador Policía", 11, GRAY_TEXT, false));
        avatarInfo.getChildren().addAll(nombreLbl, statusRow);
        avatarRow.getChildren().addAll(avatarBox, avatarInfo);

        panel.getChildren().addAll(avatarRow, separator());

        // Campos editables
        tfNombre   = editField(nombreCompleto);
        tfCorreo   = editField(nvl(usuarioActual.getCorreo()));
        tfTelefono = editField(nvl(usuarioActual.getTelefono()));

        TextField tfUsername = editField(nvl(usuarioActual.getUsername()));
        tfUsername.setEditable(false);
        tfUsername.setStyle(tfUsername.getStyle() + "-fx-opacity:0.65;");

        panel.getChildren().addAll(
                editRow("\uf007", "Nombre completo",    tfNombre),   separator(),
                editRow("\uf0e0", "Correo electrónico", tfCorreo),   separator(),
                editRow("\uf095", "Teléfono",           tfTelefono), separator(),
                editRow("\uf084", "Usuario",            tfUsername), separator()
        );

        // Feedback + botones
        Label msgLbl = new Label("");
        msgLbl.setFont(Font.font("System", 12));
        msgLbl.setPadding(new Insets(4, 0, 0, 0));

        HBox btnRow = new HBox(10);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        btnRow.setPadding(new Insets(10, 0, 4, 0));

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setStyle(
                "-fx-background-color:white;-fx-text-fill:" + GRAY_TEXT + ";"
                + "-fx-font-size:12px;-fx-background-radius:8;"
                + "-fx-border-color:" + BORDER + ";-fx-border-radius:8;"
                + "-fx-padding:7 18;-fx-cursor:hand;");
        btnCancelar.setOnAction(e -> {
            tfNombre.setText(join(
                    usuarioActual.getPrimer_nombre(), usuarioActual.getSegundo_nombre(),
                    usuarioActual.getPrimer_apellido(), usuarioActual.getSegundo_apellido()));
            tfCorreo.setText(nvl(usuarioActual.getCorreo()));
            tfTelefono.setText(nvl(usuarioActual.getTelefono()));
            msgLbl.setText("");
        });

        String saveBase = "-fx-background-color:" + SIDEBAR_B + ";"
                + "-fx-text-fill:white;-fx-font-size:12px;-fx-font-weight:bold;"
                + "-fx-background-radius:8;-fx-padding:7 18;-fx-cursor:hand;";
        Button btnGuardar = new Button("\uf0c7  Guardar cambios");
        btnGuardar.setStyle(saveBase);
        btnGuardar.setOnMouseEntered(e -> btnGuardar.setStyle(saveBase.replace(SIDEBAR_B, "#16283d")));
        btnGuardar.setOnMouseExited(e  -> btnGuardar.setStyle(saveBase));
        btnGuardar.setOnAction(e -> guardarCambios(msgLbl));

        btnRow.getChildren().addAll(btnCancelar, btnGuardar);
        panel.getChildren().addAll(btnRow, msgLbl);

        return panel;
    }

    // ── Lógica de guardado ────────────────────────────────────────
    private void guardarCambios(Label msgLbl) {
        String[] partes = tfNombre.getText().trim().split("\\s+");
        String p1 = partes.length > 0 ? partes[0] : "";
        String p2 = partes.length > 1 ? partes[1] : "";
        String a1 = partes.length > 2 ? partes[2] : "";
        String a2 = partes.length > 3 ? partes[3] : "";

        try {
            UsuarioDAO dao = new UsuarioDAO();
            boolean ok = dao.actualizar(
                    usuarioActual.getUsername(),
                    p1, p2, a1, a2,
                    tfTelefono.getText().trim(),
                    tfCorreo.getText().trim(),
                    usuarioActual.getPassword(),
                    usuarioActual.getRol() != null ? usuarioActual.getRol().getNombre() : "",
                    "", "", "", "", "", "");

            if (ok) {
                usuarioActual.setPrimer_nombre(p1);
                usuarioActual.setSegundo_nombre(p2);
                usuarioActual.setPrimer_apellido(a1);
                usuarioActual.setSegundo_apellido(a2);
                usuarioActual.setTelefono(tfTelefono.getText().trim());
                usuarioActual.setCorreo(tfCorreo.getText().trim());
                msgLbl.setText("✔ Cambios guardados correctamente.");
                msgLbl.setTextFill(Color.web(GREEN));
            } else {
                msgLbl.setText("✘ No se pudieron guardar los cambios.");
                msgLbl.setTextFill(Color.web(RED));
            }
        } catch (SQLException ex) {
            msgLbl.setText("✘ Error: " + ex.getMessage());
            msgLbl.setTextFill(Color.web(RED));
        }
    }

    // ── Helpers ───────────────────────────────────────────────────
    private VBox createPanel(String iconFA, String title) {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(16));
        panel.setStyle("-fx-background-color:" + WHITE + ";-fx-background-radius:12;");
        shadow(panel);

        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getChildren().addAll(faIcon(iconFA, 14, BLUE), label(title, 14, "#111827", true));
        panel.getChildren().addAll(header, separator());
        return panel;
    }

    private HBox editRow(String iconFA, String title, TextField field) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(6, 0, 6, 0));

        StackPane iconBox = new StackPane();
        Rectangle bg = new Rectangle(32, 32);
        bg.setArcWidth(7);
        bg.setArcHeight(7);
        bg.setFill(Color.web(BG));
        iconBox.getChildren().addAll(bg, faIcon(iconFA, 13, BLUE));

        VBox textBox = new VBox(4);
        HBox.setHgrow(textBox, Priority.ALWAYS);
        Label lbl = new Label(title);
        lbl.setStyle("-fx-font-size:12px;-fx-text-fill:" + GRAY_TEXT + ";");
        field.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(field, Priority.ALWAYS);
        textBox.getChildren().addAll(lbl, field);

        row.getChildren().addAll(iconBox, textBox);
        return row;
    }

    private TextField editField(String value) {
        TextField f = new TextField(value);
        f.setStyle(
                "-fx-background-color:#f5f7fb;"
                + "-fx-border-color:" + BORDER + ";"
                + "-fx-border-radius:8;-fx-background-radius:8;"
                + "-fx-font-size:13px;-fx-text-fill:#111827;"
                + "-fx-padding:7 12;");
        f.setMaxWidth(Double.MAX_VALUE);
        return f;
    }

    private Label faIcon(String code, double size, String color) {
        Label lbl = new Label(code);
        lbl.setStyle("-fx-font-family:" + FA + ";-fx-font-size:" + size + "px;-fx-text-fill:" + color + ";");
        return lbl;
    }

    private Region separator() {
        Region sep = new Region();
        sep.setPrefHeight(1);
        sep.setStyle("-fx-background-color:" + BORDER + ";");
        return sep;
    }

    private Label label(String text, double size, String color, boolean bold) {
        Label lbl = new Label(text);
        lbl.setFont(bold ? Font.font("System", FontWeight.BOLD, size) : Font.font("System", size));
        lbl.setTextFill(Color.web(color));
        return lbl;
    }

    private void shadow(Region node) {
        node.setEffect(new DropShadow(10, 0, 2, Color.web("#0000001a")));
    }

    private String join(String... partes) {
        StringBuilder sb = new StringBuilder();
        for (String p : partes) {
            if (p != null && !p.isBlank()) {
                if (sb.length() > 0) sb.append(" ");
                sb.append(p.trim());
            }
        }
        return sb.toString();
    }

    private String nvl(String s) { return s != null ? s : ""; }
}