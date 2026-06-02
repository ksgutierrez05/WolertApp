/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.view;

import java.sql.SQLException;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
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

public class ConfiguracionAdminView {

    // ── Paleta ────────────────────────────────────────────────────
    private static final String BG = "#f0f4f8";
    private static final String WHITE = "#ffffff";
    private static final String BLUE = "#1565c0";
    private static final String BLUE_LIGHT = "#e8f0fe";
    private static final String BLUE_MID = "#1976d2";
    private static final String TEXT_PRI = "#111827";
    private static final String TEXT_SEC = "#374151";
    private static final String GRAY = "#6b7280";
    private static final String BORDER = "#e5e7eb";
    private static final String GREEN = "#43a047";

    // ── Estado ────────────────────────────────────────────────────
    private final Usuario admin;
    private TextField tfNombre, tfCorreo, tfTelefono, tfUsername;

    // ── Constructor ───────────────────────────────────────────────
    public ConfiguracionAdminView(Usuario admin) {
        this.admin = admin;
    }

    // ── getView ───────────────────────────────────────────────────
    public ScrollPane getView() {
        VBox content = new VBox(24);
        content.setPadding(new Insets(32, 36, 40, 36));
        content.setStyle("-fx-background-color:" + BG + ";");
        content.setFillWidth(true);

        Label pageTitle = new Label("Configuración del sistema");
        pageTitle.setFont(Font.font("System", FontWeight.BOLD, 26));
        pageTitle.setTextFill(Color.web(TEXT_PRI));

        Label pageSub = new Label("Información general del sistema y datos del administrador");
        pageSub.setStyle("-fx-font-size:13px;-fx-text-fill:" + GRAY + ";");

        content.getChildren().addAll(new VBox(4, pageTitle, pageSub), buildContent());

        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setStyle("-fx-background:" + BG + ";-fx-background-color:" + BG + ";");
        return sp;
    }

    // ═══════════════ CONTENIDO PRINCIPAL ══════════════════════════
    private VBox buildContent() {
        // Nombre completo
        String nombreCompleto = join(
                admin.getPrimer_nombre(),
                admin.getSegundo_nombre(),
                admin.getPrimer_apellido(),
                admin.getSegundo_apellido()
        );

        tfNombre = tf(nombreCompleto);
        tfCorreo = tf(nvl(admin.getCorreo()));
        tfTelefono = tf(nvl(admin.getTelefono()));
        tfUsername = tf(nvl(admin.getUsername()));
        tfUsername.setEditable(false); // el username no se cambia
        tfUsername.setStyle(tfUsername.getStyle()
                + "-fx-opacity:0.7;");

        VBox cAdmin = card(
                "Perfil del administrador", "👤", BLUE, BLUE_LIGHT,
                buildPerfilHeader(nombreCompleto),
                div(),
                fieldRow("Nombre completo", tfNombre),
                div(),
                fieldRow("Correo electrónico", tfCorreo),
                div(),
                fieldRow("Teléfono", tfTelefono),
                div(),
                fieldRow("Usuario", tfUsername)
        );

        VBox sec = new VBox(20);
        sec.setFillWidth(true);
        sec.getChildren().addAll(cAdmin, actionButtons());
        return sec;
    }

    // ── Avatar + estado ───────────────────────────────────────────
    private HBox buildPerfilHeader(String nombreCompleto) {
        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 0, 14, 0));

        StackPane avatarBox = new StackPane();
        Circle avatar = new Circle(30, Color.web("#16283d"));
        Label avatarLbl = new Label("👨‍💼");
        avatarLbl.setStyle("-fx-font-size:20px;");
        avatarBox.getChildren().addAll(avatar, avatarLbl);

        VBox info = new VBox(3);
        Label nombre = new Label(nombreCompleto);
        nombre.setFont(Font.font("System", FontWeight.BOLD, 15));
        nombre.setTextFill(Color.web(TEXT_PRI));

        String rolNombre = (admin.getRol() != null) ? admin.getRol().getNombre() : "Administrador";

        HBox statusRow = new HBox(5);
        statusRow.setAlignment(Pos.CENTER_LEFT);
        statusRow.getChildren().addAll(
                new Circle(5, Color.web(GREEN)),
                lbl("Sistema activo · " + rolNombre, 11, GRAY)
        );
        info.getChildren().addAll(nombre, statusRow);
        row.getChildren().addAll(avatarBox, info);
        return row;
    }

    // ═══════════════ BOTONES ══════════════════════════════════════
    private HBox actionButtons() {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_RIGHT);
        row.setPadding(new Insets(4, 0, 0, 0));

        Button cancel = new Button("Cancelar");
        cancel.setStyle("-fx-background-color:" + WHITE + ";-fx-text-fill:" + GRAY
                + ";-fx-font-size:13px;-fx-background-radius:10;"
                + "-fx-border-color:" + BORDER + ";-fx-border-radius:10;"
                + "-fx-padding:10 22 10 22;-fx-cursor:hand;");

        // Restaura los valores originales
        cancel.setOnAction(e -> {
            tfNombre.setText(join(
                    admin.getPrimer_nombre(), admin.getSegundo_nombre(),
                    admin.getPrimer_apellido(), admin.getSegundo_apellido()));
            tfCorreo.setText(nvl(admin.getCorreo()));
            tfTelefono.setText(nvl(admin.getTelefono()));
        });

        Button save = new Button("💾  Guardar cambios");
        String base = "-fx-background-color:#16283d;-fx-text-fill:white;"
                + "-fx-font-size:13px;-fx-font-weight:bold;"
                + "-fx-background-radius:10;-fx-padding:10 22 10 22;-fx-cursor:hand;";
        save.setStyle(base);
        save.setOnMouseEntered(e -> save.setStyle(base.replace("#16283d", "#223f63")));
        save.setOnMouseExited(e -> save.setStyle(base));
        save.setOnMouseEntered(e -> save.setStyle(base.replace(BLUE, BLUE_MID)));
        

        save.setOnAction(e -> guardarCambios());

        row.getChildren().addAll(cancel, save);
        return row;
    }

    // ── Lógica de guardado ────────────────────────────────────────
    private void guardarCambios() {
        // Separar nombre completo en partes (primer nombre y primer apellido mínimo)
        String[] partes = tfNombre.getText().trim().split("\\s+");
        String p1 = partes.length > 0 ? partes[0] : "";
        String p2 = partes.length > 1 ? partes[1] : "";
        String a1 = partes.length > 2 ? partes[2] : "";
        String a2 = partes.length > 3 ? partes[3] : "";

        try {
            UsuarioDAO dao = new UsuarioDAO();
            boolean ok = dao.actualizar(
                    admin.getUsername(), // username (llave, no cambia)
                    p1, p2, a1, a2,
                    tfTelefono.getText().trim(),
                    tfCorreo.getText().trim(),
                    admin.getPassword(), // password sin cambio
                    admin.getRol() != null ? admin.getRol().getNombre() : "",
                    // dirección: se conservan los valores actuales del admin
                    "", // nombreBarrio  — ajusta si tu modelo los tiene
                    "", // calle
                    "", // carrera
                    "", // etapa
                    "", // manzana
                    "" // casa
            );

            if (ok) {
                // Actualizar el objeto en memoria
                admin.setPrimer_nombre(p1);
                admin.setSegundo_nombre(p2);
                admin.setPrimer_apellido(a1);
                admin.setSegundo_apellido(a2);
                admin.setTelefono(tfTelefono.getText().trim());
                admin.setCorreo(tfCorreo.getText().trim());

                alerta(Alert.AlertType.INFORMATION,
                        "Guardado", "Los cambios se guardaron correctamente.");
            } else {
                alerta(Alert.AlertType.ERROR,
                        "Error", "No se pudieron guardar los cambios.");
            }
        } catch (SQLException ex) {
            alerta(Alert.AlertType.ERROR,
                    "Error de base de datos", ex.getMessage());
        }
    }

    // ═══════════════ HELPERS ══════════════════════════════════════
    private VBox card(String title, String icon, String accent,
            String bgIcon, Node... body) {
        VBox card = new VBox(0);
        card.setStyle("-fx-background-color:" + WHITE + ";-fx-background-radius:16;");
        card.setMaxWidth(Double.MAX_VALUE);
        shadow(card);

        HBox header = new HBox(12);
        header.setPadding(new Insets(18, 20, 16, 20));
        header.setAlignment(Pos.CENTER_LEFT);

        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(36, 36);
        iconBox.setMinSize(36, 36);
        iconBox.setMaxSize(36, 36);
        Rectangle iconBg = new Rectangle(36, 36);
        iconBg.setArcWidth(10);
        iconBg.setArcHeight(10);
        iconBg.setFill(Color.web(bgIcon));
        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-size:16px;");
        iconBox.getChildren().addAll(iconBg, iconLbl);

        Label titleLbl = new Label(title);
        titleLbl.setFont(Font.font("System", FontWeight.BOLD, 14));
        titleLbl.setTextFill(Color.web(TEXT_PRI));
        header.getChildren().addAll(iconBox, titleLbl);

        Region divH = new Region();
        divH.setPrefHeight(1);
        divH.setStyle("-fx-background-color:" + BORDER + ";");

        VBox bodyBox = new VBox(0);
        bodyBox.setPadding(new Insets(4, 20, 16, 20));
        bodyBox.getChildren().addAll(body);

        card.getChildren().addAll(header, divH, bodyBox);
        return card;
    }

    private HBox fieldRow(String labelText, Node control) {
        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(14, 0, 14, 0));
        Label l = new Label(labelText);
        l.setStyle("-fx-font-size:13px;-fx-text-fill:" + TEXT_SEC
                + ";-fx-font-weight:bold;");
        l.setMinWidth(220);
        l.setWrapText(true);
        HBox.setHgrow(control, Priority.ALWAYS);
        if (control instanceof Region r) {
            r.setMaxWidth(Double.MAX_VALUE);
        }
        row.getChildren().addAll(l, control);
        return row;
    }

    private TextField tf(String val) {
        TextField f = new TextField(val);
        f.setStyle("-fx-background-color:" + BG + ";-fx-border-color:" + BORDER
                + ";-fx-border-radius:8;-fx-background-radius:8;"
                + "-fx-font-size:13px;-fx-text-fill:" + TEXT_PRI
                + ";-fx-padding:8 12 8 12;");
        f.setMaxWidth(Double.MAX_VALUE);
        return f;
    }

    private Region div() {
        Region r = new Region();
        r.setPrefHeight(1);
        r.setStyle("-fx-background-color:" + BORDER + ";");
        return r;
    }

    private Label lbl(String text, double size, String color) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size:" + size + "px;-fx-text-fill:" + color + ";");
        return l;
    }

    private void shadow(Region n) {
        DropShadow s = new DropShadow();
        s.setRadius(12);
        s.setOffsetY(2);
        s.setColor(Color.rgb(15, 23, 42, 0.07));
        n.setEffect(s);
    }

    private void alerta(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    /**
     * Une partes de nombre ignorando nulls/vacíos
     */
    private String join(String... partes) {
        StringBuilder sb = new StringBuilder();
        for (String p : partes) {
            if (p != null && !p.isBlank()) {
                if (sb.length() > 0) {
                    sb.append(" ");
                }
                sb.append(p.trim());
            }
        }
        return sb.toString();
    }

    /**
     * Null-safe
     */
    private String nvl(String s) {
        return s != null ? s : "";
    }
}
