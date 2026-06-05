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
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Fábrica de componentes UI reutilizables de MiCuenta.
 *
 * Principio GRASP aplicado — Pure Fabrication:
 *   No representa ningún concepto del dominio; existe solo para
 *   centralizar la creación de widgets y evitar duplicación de código
 *   (estilos, sombras, layouts de fila) entre la vista y los diálogos.
 *
 *   Sin esta clase, cada diálogo y panel repetiría los mismos ~30 líneas
 *   de construcción de Label, Button, infoRow, etc.
 */
public final class MiCuentaUIFactory {

    private MiCuentaUIFactory() { /* utilidad estática */ }

    // ── Labels ────────────────────────────────────────────────────────────────

    public static Label label(String text, double size, String color, boolean bold) {
        Label lbl = new Label(text);
        lbl.setFont(bold ? Font.font("System", FontWeight.BOLD, size) : Font.font("System", size));
        lbl.setTextFill(Color.web(color));
        return lbl;
    }

    public static Label groupLabel(String txt) {
        Label l = new Label(txt.toUpperCase());
        l.setFont(Font.font("System", FontWeight.BOLD, 10));
        l.setTextFill(Color.web("#94a3b8"));
        VBox.setMargin(l, new Insets(10, 0, 2, 0));
        return l;
    }

    // ── Botones ───────────────────────────────────────────────────────────────

    public static Button darkButton(String texto) {
        Button btn = new Button(texto);
        String base  = "-fx-background-color:#1f3a56;-fx-text-fill:white;"
                     + "-fx-font-size:12px;-fx-font-weight:bold;"
                     + "-fx-background-radius:8;-fx-padding:9 18;-fx-cursor:hand;";
        String hover = "-fx-background-color:#16283d;-fx-text-fill:white;"
                     + "-fx-font-size:12px;-fx-font-weight:bold;"
                     + "-fx-background-radius:8;-fx-padding:9 18;-fx-cursor:hand;";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e  -> btn.setStyle(base));
        return btn;
    }

    public static Button lightButton(String texto, String bgColor, String textColor) {
        Button btn = new Button(texto);
        String base  = "-fx-background-color:" + bgColor + ";-fx-text-fill:" + textColor + ";"
                     + "-fx-font-size:12px;-fx-font-weight:bold;"
                     + "-fx-background-radius:8;-fx-padding:8 16;-fx-cursor:hand;";
        String hover = "-fx-background-color:" + textColor + ";-fx-text-fill:white;"
                     + "-fx-font-size:12px;-fx-font-weight:bold;"
                     + "-fx-background-radius:8;-fx-padding:8 16;-fx-cursor:hand;";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e  -> btn.setStyle(base));
        return btn;
    }

    public static Button deleteButton(String texto) {
        Button btn = new Button(texto);
        String base  = "-fx-background-color:#fee2e2;-fx-text-fill:#e53935;"
                     + "-fx-font-size:11px;-fx-font-weight:bold;"
                     + "-fx-background-radius:6;-fx-padding:4 10;-fx-cursor:hand;";
        String hover = "-fx-background-color:#e53935;-fx-text-fill:white;"
                     + "-fx-font-size:11px;-fx-font-weight:bold;"
                     + "-fx-background-radius:6;-fx-padding:4 10;-fx-cursor:hand;";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e  -> btn.setStyle(base));
        return btn;
    }

    // ── Paneles ───────────────────────────────────────────────────────────────

    public static VBox createPanel(String title, String accentColor) {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(16));
        panel.setStyle("-fx-background-color:white;-fx-background-radius:12;");
        shadow(panel);

        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Rectangle bar = new Rectangle(4, 20);
        bar.setFill(Color.web(accentColor));
        bar.setArcWidth(4);
        bar.setArcHeight(4);
        Label t = label(title, 14, "#111827", true);
        titleRow.getChildren().addAll(bar, t);
        panel.getChildren().addAll(titleRow, sep("#e5e7eb"));
        return panel;
    }

    // ── Filas de información ──────────────────────────────────────────────────

    public static HBox infoRow(String campo, String valor) {
        HBox row = new HBox(0);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(9, 0, 9, 0));
        Label key = label(campo, 12, "#6b7280", false);
        key.setMinWidth(160);
        key.setMaxWidth(160);
        Label val = label(valor, 13, "#111827", false);
        val.setWrapText(true);
        HBox.setHgrow(val, Priority.ALWAYS);
        row.getChildren().addAll(key, val);
        return row;
    }

    public static HBox infoRowBadge(String campo, String valor, String badge, String badgeColor) {
        HBox row = new HBox(0);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(9, 0, 9, 0));
        Label key = label(campo, 12, "#6b7280", false);
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

    public static HBox infoRowFA(String faIcon, String iconColor, String campo, String valor) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 0, 10, 0));

        StackPane iconWrap = new StackPane();
        iconWrap.setPrefSize(34, 34);
        iconWrap.setMinSize(34, 34);
        iconWrap.setMaxSize(34, 34);
        Region iconBg = new Region();
        iconBg.setPrefSize(34, 34);
        iconBg.setStyle("-fx-background-color:" + iconColor + "22;-fx-background-radius:50%;");
        Label iconLbl = new Label(faIcon);
        iconLbl.setStyle(
                "-fx-font-family:'Font Awesome 6 Free Solid';"
                + "-fx-font-size:14px;-fx-text-fill:" + iconColor + ";");
        iconWrap.getChildren().addAll(iconBg, iconLbl);

        VBox texto = new VBox(2);
        HBox.setHgrow(texto, Priority.ALWAYS);
        texto.getChildren().addAll(
                label(campo, 11, "#6b7280", false),
                label(valor, 13, "#111827", true));

        row.getChildren().addAll(iconWrap, texto);
        return row;
    }

    // ── Utilidades ────────────────────────────────────────────────────────────

    public static Region sep(String borderColor) {
        Region r = new Region();
        r.setPrefHeight(1);
        r.setStyle("-fx-background-color:" + borderColor + ";");
        return r;
    }

    public static void shadow(Region node) {
        node.setEffect(new DropShadow(10, 0, 2, Color.web("#0000001a")));
    }

    public static String val(String v) {
        return v != null && !v.isBlank() ? v : "—";
    }
}