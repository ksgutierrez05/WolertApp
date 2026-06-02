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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import sistemagestion.model.Usuario;

/**
 * Vista: información del usuario y configuración. Uso: root.setCenter(new
 * ConfiguracionAdminPoliciaView(usuarioActual).build());
 */
public class ConfiguracionAdminPoliciaView {

    private static final String BG = "#f4f6fb";
    private static final String WHITE = "#ffffff";
    private static final String BLUE = "#1565c0";
    private static final String GRAY_TEXT = "#6b7280";
    private static final String BORDER = "#e5e7eb";

    private final Usuario usuarioActual;

    public ConfiguracionAdminPoliciaView(Usuario usuarioActual) {
        this.usuarioActual = usuarioActual;
    }

    public ScrollPane build() {

        VBox content = new VBox(20);
        content.setPadding(new Insets(25));
        content.setStyle("-fx-background-color:" + BG + ";");

        Label title = new Label("⚙ Configuración");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#111827"));

        content.getChildren().add(title);

        VBox perfilCard = new VBox(20);
        perfilCard.setPadding(new Insets(25));
        perfilCard.setStyle(
                "-fx-background-color:white;"
                + "-fx-background-radius:16;"
                + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.08),15,0,0,4);"
        );

        if (usuarioActual != null) {

            String nombre = ((usuarioActual.getPrimer_nombre() != null
                    ? usuarioActual.getPrimer_nombre() : "")
                    + " "
                    + (usuarioActual.getPrimer_apellido() != null
                    ? usuarioActual.getPrimer_apellido() : "")).trim();

            // ENCABEZADO
            StackPane avatar = new StackPane();

            Rectangle fondoAvatar = new Rectangle(70, 70);
            fondoAvatar.setArcWidth(20);
            fondoAvatar.setArcHeight(20);
            fondoAvatar.setFill(Color.web(BLUE));

            Label inicial = new Label(
                    nombre.isEmpty()
                    ? "U"
                    : nombre.substring(0, 1).toUpperCase()
            );

            inicial.setStyle(
                    "-fx-font-size:28px;"
                    + "-fx-font-weight:bold;"
                    + "-fx-text-fill:white;"
            );

            avatar.getChildren().addAll(fondoAvatar, inicial);

            Label nombreLbl = new Label(
                    nombre.isEmpty() ? "Usuario" : nombre
            );

            nombreLbl.setFont(
                    Font.font("System", FontWeight.BOLD, 22)
            );
            nombreLbl.setTextFill(Color.web("#111827"));

            Label rolLbl = new Label("Administrador Policía");
            rolLbl.setTextFill(Color.web("#6b7280"));
            rolLbl.setFont(Font.font(13));

            VBox datosPerfil = new VBox(4,
                    nombreLbl,
                    rolLbl
            );

            HBox encabezado = new HBox(18,
                    avatar,
                    datosPerfil
            );

            encabezado.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            perfilCard.getChildren().add(encabezado);

            perfilCard.getChildren().add(separator());

            // DATOS
            perfilCard.getChildren().addAll(
                    listItem(
                            "🪪",
                            "Identificación",
                            usuarioActual.getIdentificacion() != null
                            ? usuarioActual.getIdentificacion()
                            : "—",
                            BLUE
                    ),
                    listItem(
                            "✉",
                            "Correo electrónico",
                            usuarioActual.getCorreo() != null
                            ? usuarioActual.getCorreo()
                            : "—",
                            BLUE
                    ),
                    listItem(
                            "📱",
                            "Teléfono",
                            usuarioActual.getTelefono() != null
                            ? usuarioActual.getTelefono()
                            : "—",
                            BLUE
                    ),
                    listItem(
                            "🔑",
                            "Nombre de usuario",
                            usuarioActual.getUsername() != null
                            ? usuarioActual.getUsername()
                            : "—",
                            BLUE
                    )
            );

        } else {

            Label vacio = new Label(
                    "No hay información de usuario disponible."
            );

            vacio.setTextFill(Color.web(GRAY_TEXT));

            perfilCard.getChildren().add(vacio);
        }

        content.getChildren().add(perfilCard);

        return wrapScroll(content);
    }

    // ── Helpers ──────────────────────────────────────────────────
    private VBox createPanel(String iconFA, String title) {

        VBox panel = new VBox(10);
        panel.setPadding(new Insets(16));

        panel.setStyle(
                "-fx-background-color:" + WHITE + ";"
                + "-fx-background-radius:12;"
        );

        shadow(panel);

        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);

        header.getChildren().addAll(
                faIcon(iconFA, 14, BLUE),
                label(title, 14, "#111827", true)
        );

        panel.getChildren().addAll(
                header,
                separator()
        );

        return panel;
    }

    private HBox listItem(
            String iconFA,
            String title,
            String sub,
            String subColor) {

        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(6, 0, 6, 0));

        StackPane iconBox = new StackPane();

        Rectangle bg = new Rectangle(32, 32);
        bg.setArcWidth(7);
        bg.setArcHeight(7);
        bg.setFill(Color.web(BG));

        iconBox.getChildren().addAll(
                bg,
                faIcon(iconFA, 13, BLUE)
        );

        VBox text = new VBox(1);

        text.getChildren().addAll(
                label(title, 12, "#111827", false),
                label(sub, 10, subColor, false)
        );

        row.getChildren().addAll(iconBox, text);

        return row;
    }

    private Label faIcon(String code, double size, String color) {

        Label lbl = new Label(code);

        lbl.setStyle(
                "-fx-font-family:'Font Awesome 6 Free Solid';"
                + "-fx-font-size:" + size + "px;"
                + "-fx-text-fill:" + color + ";"
        );

        return lbl;
    }

    private void shadow(Region node) {
        node.setEffect(
                new DropShadow(
                        10,
                        0,
                        2,
                        Color.web("#0000001a")
                )
        );
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
