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
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Popup;

import java.util.List;
import static sistemagestion.view.AppColors.BLUE;
import static sistemagestion.view.AppColors.GRAY_TEXT;
import static sistemagestion.view.AppColors.TEXT_PRIMARY;



/**
 * Construye el botón-campana con popup de notificaciones.
 *
 * Patrón GRASP: Pure Fabrication — widget compartido sin relación con el dominio.
 *               Information Expert — maneja su propio estado (badge count).
 * Principio SOLID: SRP — construye solo la campana y su popup.
 *                  OCP — las secciones del popup se pasan como lista de PopupSection.
 */
public final class NotificationBellBuilder {

    private NotificationBellBuilder() {}

    /**
     * Sección del popup (título, ícono, color y lista de filas).
     */
    public record PopupSection(
            String sectionIcon,
            String sectionColor,
            String sectionTitle,
            List<PopupRow> rows
    ) {}

    /**
     * Fila individual dentro de una sección del popup.
     */
    public record PopupRow(
            String dotColor,
            String title,
            String subtitle,
            Runnable onClick
    ) {}

    /**
     * Construye el StackPane de la campana con badge y popup configurable.
     *
     * @param badgeCount    Número inicial en el badge (0 = oculto).
     * @param sections      Secciones a mostrar en el popup.
     * @param footerActions Acciones del footer del popup (label + acción).
     */
    public static StackPane build(
            int badgeCount,
            List<PopupSection> sections,
            List<javafx.util.Pair<String, Runnable>> footerActions
    ) {
        StackPane bell = new StackPane();
        bell.setCursor(javafx.scene.Cursor.HAND);

        Region bellBg = new Region();
        bellBg.setPrefSize(40, 40);
        bellBg.setStyle(
                "-fx-background-color:white;-fx-background-radius:50%;"
                + "-fx-border-color:#e5e7eb;-fx-border-radius:50%;-fx-border-width:1.5;");

        Label bellIco = new Label("\uf0f3");
        bellIco.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';"
                + "-fx-font-size:16px;-fx-text-fill:#374151;");

        Label badge = new Label(badgeCount > 9 ? "9+" : String.valueOf(badgeCount));
        badge.setStyle("-fx-background-color:#e53935;-fx-text-fill:white;"
                + "-fx-font-size:9px;-fx-font-weight:bold;"
                + "-fx-background-radius:20;-fx-padding:1 4;");
        badge.setTranslateX(10);
        badge.setTranslateY(-10);
        badge.setVisible(badgeCount > 0);

        bell.getChildren().addAll(bellBg, bellIco, badge);

        bell.setOnMouseEntered(e -> bellBg.setStyle(
                "-fx-background-color:#f0f7ff;-fx-background-radius:50%;"
                + "-fx-border-color:#1565c0;-fx-border-radius:50%;-fx-border-width:1.5;"));
        bell.setOnMouseExited(e -> bellBg.setStyle(
                "-fx-background-color:white;-fx-background-radius:50%;"
                + "-fx-border-color:#e5e7eb;-fx-border-radius:50%;-fx-border-width:1.5;"));

        // ── Popup ─────────────────────────────────────────────────
        Popup popup = new Popup();
        popup.setAutoHide(true);

        VBox popupBox = buildPopupBox(badgeCount, sections, footerActions, popup);
        popup.getContent().add(popupBox);

        bell.setOnMouseClicked(e -> {
            if (popup.isShowing()) {
                popup.hide();
            } else {
                badge.setVisible(false);
                javafx.geometry.Bounds b = bell.localToScreen(bell.getBoundsInLocal());
                popup.show(bell, b.getMaxX() - 330, b.getMaxY() + 8);
            }
        });

        return bell;
    }

    // ── Construcción del popup ─────────────────────────────────────
    private static VBox buildPopupBox(
            int badgeCount,
            List<PopupSection> sections,
            List<javafx.util.Pair<String, Runnable>> footerActions,
            Popup popup
    ) {
        VBox box = new VBox(0);
        box.setPrefWidth(330);
        box.setStyle("-fx-background-color:white;-fx-background-radius:14;"
                + "-fx-border-color:#e5e7eb;-fx-border-radius:14;-fx-border-width:1;"
                + "-fx-effect:dropshadow(gaussian,rgba(15,23,42,0.18),24,0,0,8);");

        // Header
        HBox header = new HBox(8);
        header.setPadding(new Insets(14, 16, 12, 16));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color:#f8fafc;-fx-background-radius:14 14 0 0;"
                + "-fx-border-color:transparent transparent #e5e7eb transparent;"
                + "-fx-border-width:0 0 1 0;");

        Label title = new Label("Notificaciones del sistema");
        title.setFont(Font.font("System", FontWeight.BOLD, 14));
        title.setTextFill(Color.web(TEXT_PRIMARY));
        HBox.setHgrow(title, Priority.ALWAYS);

        Label badgeLbl = new Label(badgeCount + " nuevas");
        badgeLbl.setStyle("-fx-background-color:#e5393522;-fx-text-fill:#e53935;"
                + "-fx-font-size:11px;-fx-font-weight:bold;"
                + "-fx-background-radius:20;-fx-padding:3 8;");
        header.getChildren().addAll(title, badgeLbl);
        box.getChildren().add(header);

        // Secciones
        boolean hasSections = false;
        for (PopupSection section : sections) {
            if (section.rows().isEmpty()) continue;
            hasSections = true;
            box.getChildren().add(buildSectionHeader(section));
            section.rows().forEach(row -> box.getChildren().add(buildRow(row, popup)));
        }

        // Vacío
        if (!hasSections) box.getChildren().add(buildEmpty());

        // Footer
        if (!footerActions.isEmpty()) box.getChildren().add(buildFooter(footerActions, popup));

        return box;
    }

    private static HBox buildSectionHeader(PopupSection section) {
        HBox sh = new HBox(6);
        sh.setPadding(new Insets(8, 16, 6, 16));
        sh.setAlignment(Pos.CENTER_LEFT);
        sh.setMaxWidth(Double.MAX_VALUE);
        sh.setStyle("-fx-background-color:#f9fafb;");

        Label ico = new Label(section.sectionIcon());
        ico.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';"
                + "-fx-font-size:11px;-fx-text-fill:" + section.sectionColor() + ";");

        Label txt = new Label(section.sectionTitle());
        txt.setFont(Font.font("System", FontWeight.BOLD, 11));
        txt.setTextFill(Color.web(GRAY_TEXT));

        sh.getChildren().addAll(ico, txt);
        return sh;
    }

    private static HBox buildRow(PopupRow row, Popup popup) {
        HBox fila = new HBox(10);
        fila.setPadding(new Insets(10, 16, 10, 16));
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.setStyle("-fx-border-color:transparent transparent #f3f4f6 transparent;"
                + "-fx-border-width:0 0 1 0;");
        fila.setOnMouseEntered(e -> fila.setStyle(
                "-fx-background-color:#f0f4ff;"
                + "-fx-border-color:transparent transparent #f3f4f6 transparent;"
                + "-fx-border-width:0 0 1 0;-fx-cursor:hand;"));
        fila.setOnMouseExited(e -> fila.setStyle(
                "-fx-border-color:transparent transparent #f3f4f6 transparent;"
                + "-fx-border-width:0 0 1 0;"));
        fila.setOnMouseClicked(e -> {
            popup.hide();
            row.onClick().run();
        });

        javafx.scene.shape.Circle dot = new javafx.scene.shape.Circle(5, Color.web(row.dotColor()));

        VBox info = new VBox(2);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label titleLbl = new Label(row.title());
        titleLbl.setFont(Font.font("System", FontWeight.BOLD, 12));
        titleLbl.setTextFill(Color.web(TEXT_PRIMARY));

        Label subLbl = new Label(row.subtitle());
        subLbl.setFont(Font.font("System", 11));
        subLbl.setTextFill(Color.web(GRAY_TEXT));

        info.getChildren().addAll(titleLbl, subLbl);
        fila.getChildren().addAll(dot, info);
        return fila;
    }

    private static VBox buildEmpty() {
        VBox vacio = new VBox(8);
        vacio.setAlignment(Pos.CENTER);
        vacio.setPadding(new Insets(28));

        StackPane icoWrap = new StackPane();
        icoWrap.setPrefSize(56, 56);
        Region icoBg = new Region();
        icoBg.setPrefSize(56, 56);
        icoBg.setStyle("-fx-background-color:#f1f5f9;-fx-background-radius:50%;");
        Label ico = new Label("\uf1f6");
        ico.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';"
                + "-fx-font-size:24px;-fx-text-fill:#94a3b8;");
        icoWrap.getChildren().addAll(icoBg, ico);

        Label msg = new Label("Sin notificaciones nuevas");
        msg.setStyle("-fx-font-size:13px;-fx-text-fill:" + GRAY_TEXT + ";");
        vacio.getChildren().addAll(icoWrap, msg);
        return vacio;
    }

    private static HBox buildFooter(
            List<javafx.util.Pair<String, Runnable>> actions, Popup popup) {
        HBox footer = new HBox(24);
        footer.setPadding(new Insets(10, 16, 12, 16));
        footer.setAlignment(Pos.CENTER);
        footer.setStyle("-fx-background-color:#f8fafc;-fx-background-radius:0 0 14 14;"
                + "-fx-border-color:#e5e7eb transparent transparent transparent;"
                + "-fx-border-width:1 0 0 0;");

        actions.forEach(pair -> {
            Label lbl = new Label(pair.getKey() + "  →");
            lbl.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:" + BLUE + ";");
            lbl.setCursor(javafx.scene.Cursor.HAND);
            lbl.setOnMouseClicked(e -> { popup.hide(); pair.getValue().run(); });
            footer.getChildren().add(lbl);
        });

        return footer;
    }
}