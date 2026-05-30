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
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import sistemagestion.model.*;
import sistemagestion.service.PoliciaService;

public class PerfilPoliciaView {

    private static final String WHITE     = "#ffffff";
    private static final String BG        = "#f4f6fb";
    private static final String RED       = "#e53935";
    private static final String GREEN     = "#43a047";
    private static final String BLUE      = "#1565c0";
    private static final String GRAY_TEXT = "#6b7280";
    private static final String BORDER    = "#e5e7eb";

    private final Usuario usuarioActual;
    private final Policia policiaActual;

    public PerfilPoliciaView(Usuario usuarioActual, Policia policiaActual) {
        this.usuarioActual = usuarioActual;
        this.policiaActual = policiaActual;
    }

    public ScrollPane build() {
        VBox content = new VBox(16);
        content.setPadding(new Insets(22));
        content.setStyle("-fx-background-color: " + BG + ";");

        // Título con ícono FA
        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label titleIcon = faIcon("\uf007", 20, BLUE);
        Label titleLbl = new Label("Mi perfil");
        titleLbl.setFont(Font.font("System", FontWeight.BOLD, 22));
        titleLbl.setTextFill(Color.web("#111827"));
        titleRow.getChildren().addAll(titleIcon, titleLbl);
        content.getChildren().add(titleRow);

        // Datos personales
        VBox panel = createPanel("\uf2bd", "Mis datos personales");
        if (usuarioActual != null) {
            String nombre = (trim(usuarioActual.getPrimer_nombre()) + " "
                    + trim(usuarioActual.getSegundo_nombre()) + " "
                    + trim(usuarioActual.getPrimer_apellido()) + " "
                    + trim(usuarioActual.getSegundo_apellido()))
                    .trim().replaceAll("  +", " ");
            panel.getChildren().addAll(
                    listItem("\uf007", "Nombre completo",  nombre.isEmpty() ? "—" : nombre, BLUE),      separator(),
                    listItem("\uf2bb", "Identificación",   nn(usuarioActual.getIdentificacion()),        GRAY_TEXT), separator(),
                    listItem("\uf0e0", "Correo",           nn(usuarioActual.getCorreo()),                GRAY_TEXT), separator(),
                    listItem("\uf095", "Teléfono",         nn(usuarioActual.getTelefono()),              GRAY_TEXT), separator(),
                    listItem("\uf084", "Username",         nn(usuarioActual.getUsername()),              GRAY_TEXT), separator(),
                    listItem("\uf058", "Estado",
                            usuarioActual.getEstado() != null
                                    ? usuarioActual.getEstado().name() : "—",                           GRAY_TEXT), separator()
            );
        }

        // Datos de policía
        VBox panelPolicia = createPanel("\uf505", "Mis datos de policía");
        if (policiaActual != null) {
            String unidadNom = policiaActual.getUnidadpolicial() != null
                    ? policiaActual.getUnidadpolicial().getNombre() : "—";
            panelPolicia.getChildren().addAll(
                    listItem("\uf3c5", "Placa",           nn(policiaActual.getPlaca()),   BLUE), separator(),
                    listItem("\uf005", "Rango",           nn(policiaActual.getRango()),   BLUE), separator(),
                    listItem("\uf5b7", "Unidad policial", unidadNom,                      BLUE), separator()
            );

            // ── Selector de estado policial ──────────────────────
            HBox estadoRow = new HBox(12);
            estadoRow.setAlignment(Pos.CENTER_LEFT);
            estadoRow.setPadding(new Insets(6, 0, 6, 0));

            StackPane iconBox = new StackPane();
            Rectangle iconBg = new Rectangle(32, 32);
            iconBg.setArcWidth(7); iconBg.setArcHeight(7);
            iconBg.setFill(Color.web(BG));
            Label iconLbl = faIcon("\uf111", 13, GREEN);
            iconBox.getChildren().addAll(iconBg, iconLbl);

            VBox estadoText = new VBox(3);
            HBox.setHgrow(estadoText, Priority.ALWAYS);
            Label estadoTitle = new Label("Estado policial");
            estadoTitle.setFont(Font.font("System", 12));
            estadoTitle.setTextFill(Color.web("#111827"));

            ComboBox<EstadoPolicia> combo = new ComboBox<>();
            combo.getItems().addAll(EstadoPolicia.values());
            if (policiaActual.getEstadopolicial() != null) {
                combo.setValue(policiaActual.getEstadopolicial());
            }
            combo.setStyle(
                    "-fx-font-size: 12px;"
                    + "-fx-background-radius: 8;"
                    + "-fx-border-radius: 8;"
                    + "-fx-border-color: " + BORDER + ";"
                    + "-fx-border-width: 1;");

            Button btnGuardar = new Button("Guardar");
            btnGuardar.setStyle(
                    "-fx-background-color: " + BLUE + ";"
                    + "-fx-text-fill: white;"
                    + "-fx-font-size: 12px;"
                    + "-fx-background-radius: 8;"
                    + "-fx-padding: 5 14 5 14;"
                    + "-fx-cursor: hand;");

            Label msgLbl = new Label("");
            msgLbl.setFont(Font.font("System", 11));

            btnGuardar.setOnAction(e -> {
                EstadoPolicia seleccionado = combo.getValue();
                if (seleccionado == null) return;
                try {
                    PoliciaService svc = new PoliciaService();
                    policiaActual.setEstadopolicial(seleccionado);
                    svc.actualizar(policiaActual);
                    msgLbl.setText("✔ Estado actualizado");
                    msgLbl.setTextFill(Color.web(GREEN));
                } catch (Exception ex) {
                    msgLbl.setText("✘ Error: " + ex.getMessage());
                    msgLbl.setTextFill(Color.web(RED));
                }
            });

            HBox controles = new HBox(8, combo, btnGuardar);
            controles.setAlignment(Pos.CENTER_LEFT);
            estadoText.getChildren().addAll(estadoTitle, controles, msgLbl);
            estadoRow.getChildren().addAll(iconBox, estadoText);

            panelPolicia.getChildren().addAll(estadoRow, separator());

        } else {
            panelPolicia.getChildren().add(
                    label("No se encontraron datos de policía.", 12, GRAY_TEXT, false));
        }

        content.getChildren().addAll(panel, panelPolicia);
        return wrapScroll(content);
    }

    // ── Helpers UI ───────────────────────────────────────────────
    private VBox createPanel(String iconFA, String title) {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(16));
        panel.setStyle("-fx-background-color: " + WHITE + "; -fx-background-radius: 12;");
        shadow(panel);

        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getChildren().addAll(faIcon(iconFA, 14, BLUE), label(title, 14, "#111827", true));

        panel.getChildren().addAll(header, separator());
        return panel;
    }

    private HBox listItem(String iconFA, String title, String sub, String subColor) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(6, 0, 6, 0));

        StackPane iconBox = new StackPane();
        Rectangle bg = new Rectangle(32, 32);
        bg.setArcWidth(7); bg.setArcHeight(7);
        bg.setFill(Color.web(BG));
        iconBox.getChildren().addAll(bg, faIcon(iconFA, 13, BLUE));

        VBox text = new VBox(1);
        HBox.setHgrow(text, Priority.ALWAYS);
        text.getChildren().addAll(
                label(title, 12, "#111827", false),
                label(sub, 10, subColor, false));
        row.getChildren().addAll(iconBox, text);
        return row;
    }

    // ── FA icon helper ───────────────────────────────────────────
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
        lbl.setFont(bold ? Font.font("System", FontWeight.BOLD, size) : Font.font("System", size));
        lbl.setTextFill(Color.web(color));
        return lbl;
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

    private String trim(String s) { return s != null ? s.trim() : ""; }
    private String nn(String s)   { return s != null && !s.isBlank() ? s : "—"; }
}