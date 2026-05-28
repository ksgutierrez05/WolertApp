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
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import sistemagestion.model.Usuario;

/**
 * Vista: información del usuario y configuración.
 * Uso: root.setCenter(new ConfiguracionAdminPoliciaView(usuarioActual).build());
 */
public class ConfiguracionAdminPoliciaView {

    private static final String BG        = "#f4f6fb";
    private static final String WHITE     = "#ffffff";
    private static final String BLUE      = "#1565c0";
    private static final String GRAY_TEXT = "#6b7280";
    private static final String BORDER    = "#e5e7eb";

    private final Usuario usuarioActual;

    public ConfiguracionAdminPoliciaView(Usuario usuarioActual) {
        this.usuarioActual = usuarioActual;
    }

    public ScrollPane build() {
        VBox content = new VBox(16);
        content.setPadding(new Insets(22));
        content.setStyle("-fx-background-color: " + BG + ";");

        Label title = new Label("⚙ Configuración");
        title.setFont(Font.font("System", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#111827"));
        content.getChildren().add(title);

        VBox panel = createPanel("Información del usuario actual");
        if (usuarioActual != null) {
            String nombre = ((usuarioActual.getPrimer_nombre()    != null ? usuarioActual.getPrimer_nombre()    : "")
                           + " " + (usuarioActual.getPrimer_apellido() != null ? usuarioActual.getPrimer_apellido() : "")).trim();
            panel.getChildren().addAll(
                    listItem("👤", "Usuario",        nombre.isEmpty()                                         ? "—" : nombre,                        BLUE),      separator(),
                    listItem("🪪", "Identificación", usuarioActual.getIdentificacion() != null ? usuarioActual.getIdentificacion() : "—", GRAY_TEXT), separator(),
                    listItem("✉",  "Correo",         usuarioActual.getCorreo()         != null ? usuarioActual.getCorreo()         : "—", GRAY_TEXT), separator(),
                    listItem("📱", "Teléfono",       usuarioActual.getTelefono()       != null ? usuarioActual.getTelefono()       : "—", GRAY_TEXT), separator(),
                    listItem("🔑", "Username",       usuarioActual.getUsername()       != null ? usuarioActual.getUsername()       : "—", GRAY_TEXT), separator()
            );
        } else {
            panel.getChildren().add(label("No hay información de usuario disponible.", 13, GRAY_TEXT, false));
        }
        content.getChildren().add(panel);
        return wrapScroll(content);
    }

    // ── Helpers ──────────────────────────────────────────────────
    private VBox createPanel(String title) {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(16));
        panel.setStyle("-fx-background-color: " + WHITE + "; -fx-background-radius: 12;");
        panel.getChildren().addAll(label(title, 14, "#111827", true), separator());
        return panel;
    }

    private HBox listItem(String icon, String title, String sub, String subColor) {
        HBox row = new HBox(10);
        row.setPadding(new Insets(6, 0, 6, 0));
        StackPane iconBox = new StackPane();
        Rectangle bg = new Rectangle(32, 32);
        bg.setArcWidth(7); bg.setArcHeight(7);
        bg.setFill(Color.web(BG));
        iconBox.getChildren().addAll(bg, label(icon, 14, BLUE, false));
        VBox text = new VBox(1);
        HBox.setHgrow(text, Priority.ALWAYS);
        text.getChildren().addAll(label(title, 12, "#111827", false), label(sub, 10, subColor, false));
        row.getChildren().addAll(iconBox, text);
        return row;
    }

    private Label label(String text, double size, String color, boolean bold) {
        Label lbl = new Label(text);
        lbl.setFont(bold ? Font.font("System", FontWeight.BOLD, size) : Font.font("System", size));
        lbl.setTextFill(Color.web(color));
        return lbl;
    }

    private Region separator() {
        Region sep = new Region();
        sep.setPrefHeight(1);
        sep.setStyle("-fx-background-color: " + BORDER + ";");
        return sep;
    }

    private ScrollPane wrapScroll(VBox content) {
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background: " + BG + "; -fx-background-color: " + BG + ";");
        return scroll;
    }
}
