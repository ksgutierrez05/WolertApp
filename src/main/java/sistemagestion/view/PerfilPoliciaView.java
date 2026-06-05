/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.view;


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
import sistemagestion.model.*;
import sistemagestion.service.PoliciaService;

public class PerfilPoliciaView {

    private static final String WHITE     = "#ffffff";
    private static final String BG        = "#f4f6fb";
    private static final String RED       = "#e53935";
    private static final String GREEN     = "#43a047";
    private static final String BLUE      = "#1565c0";
    private static final String BLUE_DARK = "#16283d";
    private static final String GRAY_TEXT = "#6b7280";
    private static final String BORDER    = "#e5e7eb";

    private final Usuario usuarioActual;
    private final Policia policiaActual;

    public PerfilPoliciaView(Usuario usuarioActual, Policia policiaActual) {
        this.usuarioActual = usuarioActual;
        this.policiaActual = policiaActual;
        Font.loadFont(getClass().getResourceAsStream("/fa-solid-900.ttf"), 20);
    }

    public ScrollPane build() {
        VBox content = new VBox(16);
        content.setPadding(new Insets(22));
        content.setStyle("-fx-background-color: " + BG + ";");

        // Título
        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label titleIcon = faIcon("\uf007", 20, BLUE);
        Label titleLbl  = new Label("Mi perfil");
        titleLbl.setFont(Font.font("System", FontWeight.BOLD, 22));
        titleLbl.setTextFill(Color.web("#111827"));
        titleRow.getChildren().addAll(titleIcon, titleLbl);

        content.getChildren().addAll(
                titleRow,
                buildPanelPersonal(),
                buildPanelPolicia()
        );

        return wrapScroll(content);
    }

    // ═══════════════════════════════════════════════════════════════
    // PANEL — datos personales con edición
    // ═══════════════════════════════════════════════════════════════
    private VBox buildPanelPersonal() {
        VBox panel = createPanel("\uf2bd", "Mis datos personales");

        if (usuarioActual == null) {
            panel.getChildren().add(label("No se encontraron datos del usuario.", 12, GRAY_TEXT, false));
            return panel;
        }

        String nombre = (trim(usuarioActual.getPrimer_nombre()) + " "
                + trim(usuarioActual.getSegundo_nombre()) + " "
                + trim(usuarioActual.getPrimer_apellido()) + " "
                + trim(usuarioActual.getSegundo_apellido()))
                .trim().replaceAll("  +", " ");

        // Avatar
        HBox avatarRow = new HBox(14);
        avatarRow.setAlignment(Pos.CENTER_LEFT);
        avatarRow.setPadding(new Insets(6, 0, 10, 0));

        StackPane avatarBox = new StackPane();
        Circle avatar = new Circle(28, Color.web(BLUE_DARK));
        Label avatarIco = faIcon("\uf007", 18, WHITE);
        avatarBox.getChildren().addAll(avatar, avatarIco);

        VBox avatarInfo = new VBox(3);
        Label nombreLbl = new Label(nombre.isEmpty() ? "Policía" : nombre);
        nombreLbl.setFont(Font.font("System", FontWeight.BOLD, 14));
        nombreLbl.setTextFill(Color.web("#111827"));
        String rolNombre = (usuarioActual.getRol() != null)
                ? usuarioActual.getRol().getNombre() : "Policía";
        HBox statusRow = new HBox(5);
        statusRow.setAlignment(Pos.CENTER_LEFT);
        statusRow.getChildren().addAll(
                new Circle(5, Color.web(GREEN)),
                lbl("Activo · " + rolNombre, 11, GRAY_TEXT));
        avatarInfo.getChildren().addAll(nombreLbl, statusRow);
        avatarRow.getChildren().addAll(avatarBox, avatarInfo);

        panel.getChildren().addAll(avatarRow, separator());

        // Campos editables
        TextField tfNombre   = editField(nombre);
        TextField tfCorreo   = editField(nn(usuarioActual.getCorreo()));
        TextField tfTelefono = editField(nn(usuarioActual.getTelefono()));
        TextField tfIdent    = roField(nn(usuarioActual.getIdentificacion()));
        TextField tfUser     = roField(nn(usuarioActual.getUsername()));
        TextField tfEstado   = roField(usuarioActual.getEstado() != null
                ? usuarioActual.getEstado().name() : "—");

        panel.getChildren().addAll(
                editRow("\uf007", "Nombre completo",   tfNombre),   separator(),
                editRow("\uf2bb", "Identificación",    tfIdent),    separator(),
                editRow("\uf0e0", "Correo",            tfCorreo),   separator(),
                editRow("\uf095", "Teléfono",          tfTelefono), separator(),
                editRow("\uf084", "Usuario",           tfUser),     separator(),
                editRow("\uf058", "Estado",            tfEstado),   separator()
        );

        // Feedback + botones
        Label msgLbl = new Label("");
        msgLbl.setFont(Font.font("System", 12));
        msgLbl.setPadding(new Insets(4, 0, 0, 0));

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setStyle(cancelStyle());
        btnCancelar.setOnAction(e -> {
            tfNombre.setText(nombre);
            tfCorreo.setText(nn(usuarioActual.getCorreo()));
            tfTelefono.setText(nn(usuarioActual.getTelefono()));
            msgLbl.setText("");
        });

        Button btnGuardar = new Button("💾  Guardar cambios");
        btnGuardar.setStyle(saveStyle());
        btnGuardar.setOnMouseEntered(e ->
                btnGuardar.setStyle(saveStyle().replace(BLUE_DARK, "#223f63")));
        btnGuardar.setOnMouseExited(e -> btnGuardar.setStyle(saveStyle()));
        btnGuardar.setOnAction(e -> {
            String[] ps = tfNombre.getText().trim().split("\\s+");
            try {
                UsuarioDAO dao = new UsuarioDAO();
                boolean ok = dao.actualizar(
                        usuarioActual.getUsername(),
                        ps.length > 0 ? ps[0] : "",
                        ps.length > 1 ? ps[1] : "",
                        ps.length > 2 ? ps[2] : "",
                        ps.length > 3 ? ps[3] : "",
                        tfTelefono.getText().trim(),
                        tfCorreo.getText().trim(),
                        usuarioActual.getPassword(),
                        usuarioActual.getRol() != null ? usuarioActual.getRol().getNombre() : "",
                        "", "", "", "", "", "");
                if (ok) {
                    if (ps.length > 0) usuarioActual.setPrimer_nombre(ps[0]);
                    if (ps.length > 1) usuarioActual.setSegundo_nombre(ps[1]);
                    if (ps.length > 2) usuarioActual.setPrimer_apellido(ps[2]);
                    if (ps.length > 3) usuarioActual.setSegundo_apellido(ps[3]);
                    usuarioActual.setCorreo(tfCorreo.getText().trim());
                    usuarioActual.setTelefono(tfTelefono.getText().trim());
                    msgLbl.setText("✔ Cambios guardados correctamente.");
                    msgLbl.setTextFill(Color.web(GREEN));
                } else {
                    msgLbl.setText("✘ No se pudieron guardar los cambios.");
                    msgLbl.setTextFill(Color.web(RED));
                }
            } catch (Exception ex) {
                msgLbl.setText("✘ Error: " + ex.getMessage());
                msgLbl.setTextFill(Color.web(RED));
            }
        });

        HBox btnRow = new HBox(10);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        btnRow.setPadding(new Insets(10, 0, 4, 0));
        btnRow.getChildren().addAll(btnCancelar, btnGuardar);

        panel.getChildren().addAll(btnRow, msgLbl);
        return panel;
    }

    // ═══════════════════════════════════════════════════════════════
    // PANEL — datos de policía con edición de estado
    // ═══════════════════════════════════════════════════════════════
    private VBox buildPanelPolicia() {
        VBox panel = createPanel("\uf505", "Mis datos de policía");

        if (policiaActual == null) {
            panel.getChildren().add(
                    label("No se encontraron datos de policía.", 12, GRAY_TEXT, false));
            return panel;
        }

        String unidadNom = policiaActual.getUnidadpolicial() != null
                ? policiaActual.getUnidadpolicial().getNombre() : "—";

        panel.getChildren().addAll(
                editRow("\uf3c5", "Placa",           roField(nn(policiaActual.getPlaca()))),   separator(),
                editRow("\uf005", "Rango",           roField(nn(policiaActual.getRango()))),   separator(),
                editRow("\uf5b7", "Unidad policial", roField(unidadNom)),                      separator()
        );

        // Selector de estado policial
        HBox estadoRow = new HBox(10);
        estadoRow.setAlignment(Pos.CENTER_LEFT);
        estadoRow.setPadding(new Insets(6, 0, 6, 0));

        StackPane iconBox = new StackPane();
        Rectangle iconBg = new Rectangle(32, 32);
        iconBg.setArcWidth(7); iconBg.setArcHeight(7);
        iconBg.setFill(Color.web(BG));
        iconBox.getChildren().addAll(iconBg, faIcon("\uf111", 13, GREEN));

        VBox estadoText = new VBox(4);
        HBox.setHgrow(estadoText, Priority.ALWAYS);
        Label estadoLbl = new Label("Estado policial");
        estadoLbl.setStyle("-fx-font-size:12px; -fx-text-fill:" + GRAY_TEXT + ";");

        ComboBox<EstadoPolicia> combo = new ComboBox<>();
        combo.getItems().addAll(EstadoPolicia.values());
        if (policiaActual.getEstadopolicial() != null) {
            combo.setValue(policiaActual.getEstadopolicial());
        }
        combo.setMaxWidth(Double.MAX_VALUE);
        combo.setStyle(
                "-fx-font-size: 13px;"
                + "-fx-background-radius: 8;"
                + "-fx-border-radius: 8;"
                + "-fx-border-color: " + BORDER + ";"
                + "-fx-border-width: 1;"
                + "-fx-background-color: #f5f7fb;");
        HBox.setHgrow(combo, Priority.ALWAYS);

        estadoText.getChildren().addAll(estadoLbl, combo);
        estadoRow.getChildren().addAll(iconBox, estadoText);

        panel.getChildren().addAll(estadoRow, separator());

        // Feedback + botón guardar estado
        Label msgPolicia = new Label("");
        msgPolicia.setFont(Font.font("System", 12));
        msgPolicia.setPadding(new Insets(4, 0, 0, 0));

        Button btnGuardarEstado = new Button("💾  Guardar estado");
        btnGuardarEstado.setStyle(saveStyle());
        btnGuardarEstado.setOnMouseEntered(e ->
                btnGuardarEstado.setStyle(saveStyle().replace(BLUE_DARK, "#223f63")));
        btnGuardarEstado.setOnMouseExited(e -> btnGuardarEstado.setStyle(saveStyle()));
        btnGuardarEstado.setOnAction(e -> {
            EstadoPolicia seleccionado = combo.getValue();
            if (seleccionado == null) {
                msgPolicia.setText("✘ Selecciona un estado.");
                msgPolicia.setTextFill(Color.web(RED));
                return;
            }
            try {
                PoliciaService svc = new PoliciaService();
                policiaActual.setEstadopolicial(seleccionado);
                svc.actualizar(policiaActual);
                msgPolicia.setText("✔ Estado actualizado correctamente.");
                msgPolicia.setTextFill(Color.web(GREEN));
            } catch (Exception ex) {
                msgPolicia.setText("✘ Error: " + ex.getMessage());
                msgPolicia.setTextFill(Color.web(RED));
            }
        });

        HBox btnRowPolicia = new HBox(10);
        btnRowPolicia.setAlignment(Pos.CENTER_RIGHT);
        btnRowPolicia.setPadding(new Insets(10, 0, 4, 0));
        btnRowPolicia.getChildren().add(btnGuardarEstado);

        panel.getChildren().addAll(btnRowPolicia, msgPolicia);
        return panel;
    }

    // ═══════════════════════════════════════════════════════════════
    // HELPERS UI
    // ═══════════════════════════════════════════════════════════════
    private VBox createPanel(String iconFA, String title) {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(16));
        panel.setStyle("-fx-background-color: " + WHITE + "; -fx-background-radius: 12;");
        shadow(panel);

        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getChildren().addAll(
                faIcon(iconFA, 14, BLUE),
                label(title, 14, "#111827", true));
        panel.getChildren().addAll(header, separator());
        return panel;
    }

    private HBox editRow(String iconFA, String title, TextField field) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(6, 0, 6, 0));

        StackPane iconBox = new StackPane();
        Rectangle bg = new Rectangle(32, 32);
        bg.setArcWidth(7); bg.setArcHeight(7);
        bg.setFill(Color.web(BG));
        iconBox.getChildren().addAll(bg, faIcon(iconFA, 13, BLUE));

        VBox textBox = new VBox(4);
        HBox.setHgrow(textBox, Priority.ALWAYS);
        Label lbl = new Label(title);
        lbl.setStyle("-fx-font-size:12px; -fx-text-fill:" + GRAY_TEXT + ";");
        field.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(field, Priority.ALWAYS);
        textBox.getChildren().addAll(lbl, field);

        row.getChildren().addAll(iconBox, textBox);
        return row;
    }

    private TextField editField(String value) {
        TextField f = new TextField(value);
        f.setStyle(
                "-fx-background-color: #f5f7fb;"
                + "-fx-border-color: " + BORDER + ";"
                + "-fx-border-radius: 8; -fx-background-radius: 8;"
                + "-fx-font-size: 13px; -fx-text-fill: #111827;"
                + "-fx-padding: 7 12 7 12;");
        f.setMaxWidth(Double.MAX_VALUE);
        return f;
    }

    private TextField roField(String value) {
        TextField f = editField(value);
        f.setEditable(false);
        f.setStyle(f.getStyle() + "-fx-opacity:0.65;");
        return f;
    }

    private Label faIcon(String code, double size, String color) {
        Label lbl = new Label(code);
        lbl.setStyle(
                "-fx-font-family: 'Font Awesome 6 Free Solid';"
                + "-fx-font-size: " + size + "px;"
                + "-fx-text-fill: " + color + ";");
        return lbl;
    }

    private Region separator() {
        Region sep = new Region();
        sep.setPrefHeight(1);
        sep.setStyle("-fx-background-color: " + BORDER + ";");
        return sep;
    }

    private Label label(String text, double size, String color, boolean bold) {
        Label lbl = new Label(text);
        lbl.setFont(bold
                ? Font.font("System", FontWeight.BOLD, size)
                : Font.font("System", size));
        lbl.setTextFill(Color.web(color));
        return lbl;
    }

    private Label lbl(String text, double size, String color) {
        return label(text, size, color, false);
    }

    private void shadow(Region node) {
        node.setEffect(new DropShadow(10, 0, 2, Color.web("#0000001a")));
    }

    private ScrollPane wrapScroll(VBox content) {
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background: " + BG + "; -fx-background-color: " + BG + ";");
        return scroll;
    }

    private String cancelStyle() {
        return "-fx-background-color: white; -fx-text-fill: " + GRAY_TEXT + ";"
                + "-fx-font-size: 12px; -fx-background-radius: 8;"
                + "-fx-border-color: " + BORDER + "; -fx-border-radius: 8;"
                + "-fx-padding: 7 18 7 18; -fx-cursor: hand;";
    }

    private String saveStyle() {
        return "-fx-background-color: " + BLUE_DARK + "; -fx-text-fill: white;"
                + "-fx-font-size: 12px; -fx-font-weight: bold;"
                + "-fx-background-radius: 8; -fx-padding: 7 18 7 18; -fx-cursor: hand;";
    }

    private String trim(String s) { return s != null ? s.trim() : ""; }
    private String nn(String s)   { return s != null && !s.isBlank() ? s : "—"; }
}