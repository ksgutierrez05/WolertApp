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

        Label title = new Label("⚙ Mi perfil");
        title.setFont(Font.font("System", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#111827"));
        content.getChildren().add(title);

        // Datos personales
        VBox panel = createPanel("Mis datos personales");
        if (usuarioActual != null) {
            String nombre = (trim(usuarioActual.getPrimer_nombre()) + " "
                    + trim(usuarioActual.getSegundo_nombre()) + " "
                    + trim(usuarioActual.getPrimer_apellido()) + " "
                    + trim(usuarioActual.getSegundo_apellido()))
                    .trim().replaceAll("  +", " ");
            panel.getChildren().addAll(
                    listItem("👤", "Nombre completo",  nombre.isEmpty() ? "—" : nombre,                          BLUE),      separator(),
                    listItem("🪪", "Identificación",   nn(usuarioActual.getIdentificacion()),                     GRAY_TEXT), separator(),
                    listItem("✉",  "Correo",           nn(usuarioActual.getCorreo()),                             GRAY_TEXT), separator(),
                    listItem("📱", "Teléfono",         nn(usuarioActual.getTelefono()),                           GRAY_TEXT), separator(),
                    listItem("🔑", "Username",         nn(usuarioActual.getUsername()),                           GRAY_TEXT), separator(),
                    listItem("👔", "Estado",
                            usuarioActual.getEstado() != null ? usuarioActual.getEstado().name() : "—",          GRAY_TEXT), separator()
            );
        }

        // Datos de policía
        VBox panelPolicia = createPanel("Mis datos de policía");
        if (policiaActual != null) {
            String unidadNom = policiaActual.getUnidadpolicial() != null
                    ? policiaActual.getUnidadpolicial().getNombre() : "—";
            panelPolicia.getChildren().addAll(
                    listItem("🏅", "Placa",           nn(policiaActual.getPlaca()),                               BLUE),  separator(),
                    listItem("⭐", "Rango",           nn(policiaActual.getRango()),                               BLUE),  separator(),
                    listItem("🚓", "Unidad policial", unidadNom,                                                   BLUE),  separator(),
                    listItem("📊", "Estado policial",
                            policiaActual.getEstadopolicial() != null
                                    ? policiaActual.getEstadopolicial().name().replace("_", " ") : "—",          GREEN), separator()
            );
        } else {
            panelPolicia.getChildren().add(
                    label("No se encontraron datos de policía.", 12, GRAY_TEXT, false));
        }

        content.getChildren().addAll(panel, panelPolicia);
        return wrapScroll(content);
    }

    // ── Helpers UI ───────────────────────────────────────────────
    private VBox createPanel(String title) {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(16));
        panel.setStyle("-fx-background-color: " + WHITE + "; -fx-background-radius: 12;");
        shadow(panel);
        panel.getChildren().addAll(label(title, 14, "#111827", true), separator());
        return panel;
    }

    private HBox listItem(String icon, String title, String sub, String subColor) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(6, 0, 6, 0));
        StackPane iconBox = new StackPane();
        Rectangle bg = new Rectangle(32, 32);
        bg.setArcWidth(7); bg.setArcHeight(7); bg.setFill(Color.web(BG));
        iconBox.getChildren().addAll(bg, label(icon, 14, BLUE, false));
        VBox text = new VBox(1);
        HBox.setHgrow(text, Priority.ALWAYS);
        text.getChildren().addAll(
                label(title, 12, "#111827", false),
                label(sub, 10, subColor, false));
        row.getChildren().addAll(iconBox, text);
        return row;
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