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
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class ConfiguracionAdminView {

    // ── Paleta ────────────────────────────────────────────────────
    private static final String BG          = "#f0f4f8";
    private static final String WHITE       = "#ffffff";
    private static final String BLUE        = "#1565c0";
    private static final String BLUE_LIGHT  = "#e8f0fe";
    private static final String BLUE_MID    = "#1976d2";
    private static final String TEXT_PRI    = "#111827";
    private static final String TEXT_SEC    = "#374151";
    private static final String GRAY        = "#6b7280";
    private static final String BORDER      = "#e5e7eb";
    private static final String GREEN       = "#43a047";
    private static final String GREEN_LIGHT = "#e8f5e9";
    private static final String AMBER       = "#fb8c00";
    private static final String AMBER_LIGHT = "#fff8e1";

    // ── getView ───────────────────────────────────────────────────
    public ScrollPane getView() {
        VBox content = new VBox(24);
        content.setPadding(new Insets(32, 36, 40, 36));
        content.setStyle("-fx-background-color:" + BG + ";");
        content.setFillWidth(true);

        // Título de página
        Label pageTitle = new Label("Configuración del sistema");
        pageTitle.setFont(Font.font("System", FontWeight.BOLD, 26));
        pageTitle.setTextFill(Color.web(TEXT_PRI));

        Label pageSub = new Label("Información general del sistema y datos del administrador");
        pageSub.setStyle("-fx-font-size:13px;-fx-text-fill:" + GRAY + ";");

        VBox header = new VBox(4, pageTitle, pageSub);

        content.getChildren().addAll(header, buildContent());

        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setStyle("-fx-background:" + BG + ";-fx-background-color:" + BG + ";");
        return sp;
    }

    // ═══════════════ CONTENIDO PRINCIPAL ══════════════════════════
    private VBox buildContent() {
        VBox sec = new VBox(20);
        sec.setFillWidth(true);

        // Tarjetas de estado rápido
        HBox metrics = new HBox(16);
        metrics.setFillHeight(true);
        metrics.getChildren().addAll(
                metricCard("🖥", "Servidor",    "Activo",  GREEN, GREEN_LIGHT),
                metricCard("📶", "Red",         "Estable", BLUE,  BLUE_LIGHT),
                metricCard("⚠",  "Errores hoy", "3",       AMBER, AMBER_LIGHT)
        );

        // Información de la aplicación
        VBox cSistema = card("Información de la aplicación", "⚙", BLUE, BLUE_LIGHT,
                infoRow("Nombre",       "WolertApp"),
                div(),
                infoRow("Versión",      "v2.1.0"),
                div(),
                infoRow("Zona horaria", "America/Bogota (UTC-5)"),
                div(),
                infoRow("Idioma",       "Español (Colombia)"),
                div(),
                infoRow("Descripción",  "Sistema de gestión de alertas comunitarias")
        );

        // Perfil del administrador
        VBox cAdmin = card("Perfil del administrador", "👤", BLUE, BLUE_LIGHT,
                buildPerfilHeader(),
                div(),
                fieldRow("Nombre completo", tf("Administrador Principal")),
                div(),
                fieldRow("Correo",          tf("admin@wolertapp.co")),
                div(),
                fieldRow("Teléfono",        tf("+57 300 000 0000")),
                div(),
                fieldRow("Username",        tf("admin"))
        );

        sec.getChildren().addAll(cAdmin, cSistema, metrics, actionButtons());
        return sec;
    }

    // ── Avatar + estado dentro de la card de perfil ───────────────
    private HBox buildPerfilHeader() {
        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 0, 14, 0));

        StackPane avatarBox = new StackPane();
        Circle avatar   = new Circle(30, Color.web("#16283d"));
        Label avatarLbl = new Label("👨‍💼");
        avatarLbl.setStyle("-fx-font-size:20px;");
        avatarBox.getChildren().addAll(avatar, avatarLbl);

        VBox info = new VBox(3);
        Label nombre = new Label("Administrador Principal");
        nombre.setFont(Font.font("System", FontWeight.BOLD, 15));
        nombre.setTextFill(Color.web(TEXT_PRI));

        HBox statusRow = new HBox(5);
        statusRow.setAlignment(Pos.CENTER_LEFT);
        statusRow.getChildren().addAll(
                new Circle(5, Color.web(GREEN)),
                lbl("Sistema activo · Último acceso hoy 08:42", 11, GRAY)
        );
        info.getChildren().addAll(nombre, statusRow);
        row.getChildren().addAll(avatarBox, info);
        return row;
    }

    // ═══════════════ COMPONENTES ══════════════════════════════════
    private VBox card(String title, String icon, String accent, String bgIcon, Node... body) {
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
        l.setStyle("-fx-font-size:13px;-fx-text-fill:" + TEXT_SEC + ";-fx-font-weight:bold;");
        l.setMinWidth(220);
        l.setWrapText(true);
        HBox.setHgrow(control, Priority.ALWAYS);
        if (control instanceof Region r) {
            r.setMaxWidth(Double.MAX_VALUE);
        }
        row.getChildren().addAll(l, control);
        return row;
    }

    private HBox infoRow(String labelText, String value) {
        HBox row = new HBox();
        row.setPadding(new Insets(12, 0, 12, 0));
        row.setAlignment(Pos.CENTER_LEFT);
        Label l = new Label(labelText);
        l.setStyle("-fx-font-size:13px;-fx-text-fill:" + GRAY + ";");
        l.setMinWidth(200);
        Label v = new Label(value);
        v.setStyle("-fx-font-size:13px;-fx-text-fill:" + TEXT_PRI + ";-fx-font-weight:bold;");
        HBox.setHgrow(v, Priority.ALWAYS);
        row.getChildren().addAll(l, v);
        return row;
    }

    private VBox metricCard(String icon, String label, String value,
                            String accent, String bgC) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(16, 18, 16, 18));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color:" + WHITE + ";-fx-background-radius:12;");
        card.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(card, Priority.ALWAYS);
        shadow(card);
        StackPane ib = new StackPane();
        ib.setPrefSize(36, 36);
        ib.setMinSize(36, 36);
        ib.setMaxSize(36, 36);
        Rectangle bg = new Rectangle(36, 36);
        bg.setArcWidth(10);
        bg.setArcHeight(10);
        bg.setFill(Color.web(bgC));
        Label ic = new Label(icon);
        ic.setStyle("-fx-font-size:16px;");
        ib.getChildren().addAll(bg, ic);
        Label la = new Label(label);
        la.setStyle("-fx-font-size:11px;-fx-text-fill:" + GRAY + ";");
        Label va = new Label(value);
        va.setStyle("-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:" + accent + ";");
        card.getChildren().addAll(ib, la, va);
        return card;
    }

    private HBox actionButtons() {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_RIGHT);
        row.setPadding(new Insets(4, 0, 0, 0));
        Button cancel = new Button("Cancelar");
        cancel.setStyle("-fx-background-color:" + WHITE + ";-fx-text-fill:" + GRAY
                + ";-fx-font-size:13px;-fx-background-radius:10;"
                + "-fx-border-color:" + BORDER + ";-fx-border-radius:10;"
                + "-fx-padding:10 22 10 22;-fx-cursor:hand;");
        Button save = new Button("💾  Guardar cambios");
        String base = "-fx-background-color:" + BLUE + ";-fx-text-fill:white;"
                + "-fx-font-size:13px;-fx-font-weight:bold;"
                + "-fx-background-radius:10;-fx-padding:10 22 10 22;-fx-cursor:hand;";
        save.setStyle(base);
        save.setOnMouseEntered(e -> save.setStyle(base.replace(BLUE, BLUE_MID)));
        save.setOnMouseExited(e -> save.setStyle(base));
        row.getChildren().addAll(cancel, save);
        return row;
    }

    // ── Inputs ────────────────────────────────────────────────────
    private TextField tf(String val) {
        TextField f = new TextField(val);
        f.setStyle("-fx-background-color:" + BG + ";-fx-border-color:" + BORDER
                + ";-fx-border-radius:8;-fx-background-radius:8;"
                + "-fx-font-size:13px;-fx-text-fill:" + TEXT_PRI + ";-fx-padding:8 12 8 12;");
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
}