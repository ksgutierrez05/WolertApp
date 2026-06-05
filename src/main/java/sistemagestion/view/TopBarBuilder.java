/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.view;

/**
 *
 * @author Maria Cristina
 */


import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import static sistemagestion.view.AppColors.BLUE;
import static sistemagestion.view.AppColors.GRAY_TEXT;
import static sistemagestion.view.AppColors.TEXT_PRIMARY;
import static sistemagestion.view.AppColors.TEXT_SECONDARY;
import static sistemagestion.view.MiCuentaUIFactory.label;


/**
 * Construye la barra superior (título + fecha/hora + campana) compartida
 * entre AdministradorApp y UsuarioApp.
 *
 * Patrón GRASP: Pure Fabrication — no es dominio, reduce duplicación.
 * Principio SOLID: SRP — construye solo la barra superior.
 *                  OCP — la campana se inyecta como nodo ya construido.
 */
public final class TopBarBuilder {

    private TopBarBuilder() {}

    /**
     * @param mainTitle   Título principal (ej. "Dashboard Administrativo").
     * @param subtitle    Subtítulo descriptivo.
     * @param bellNode    Nodo de la campana ya construido (puede ser null).
     * @return HBox listo para usar como barra superior.
     */
    public static HBox build(String mainTitle, String subtitle, javafx.scene.Node bellNode) {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);

        // ── Saludo / título ───────────────────────────────────────
        VBox greeting = new VBox(4);
        Label hello = new Label(mainTitle);
        hello.setFont(Font.font("System", FontWeight.BOLD, 28));
        hello.setTextFill(Color.web(TEXT_PRIMARY));
        greeting.getChildren().addAll(hello, label(subtitle, 13, GRAY_TEXT, false));

        // ── Fecha y hora ──────────────────────────────────────────
        HBox rightBox = new HBox(16);
        rightBox.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(rightBox, Priority.ALWAYS);

        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern(
                "d 'de' MMMM 'de' yyyy", new Locale("es", "CO"));
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern(
                "hh:mm:ss a", new Locale("es", "CO"));
        LocalDateTime now0 = LocalDateTime.now(ZoneId.of("America/Bogota"));

        Label calIco = faLabel("\uf073", 13, BLUE);
        Label dateLbl = label(now0.format(dateFmt), 13, TEXT_SECONDARY, false);
        Label clockIco = faLabel("\uf017", 13, GRAY_TEXT);
        Label timeLbl = label(now0.format(timeFmt), 13, GRAY_TEXT, false);

        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), ev -> {
            LocalDateTime now = LocalDateTime.now(ZoneId.of("America/Bogota"));
            dateLbl.setText(now.format(dateFmt));
            timeLbl.setText(now.format(timeFmt));
        }));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();

        HBox dateRow = new HBox(6);
        dateRow.setAlignment(Pos.CENTER_RIGHT);
        dateRow.getChildren().addAll(calIco, dateLbl);

        HBox timeRow = new HBox(6);
        timeRow.setAlignment(Pos.CENTER_RIGHT);
        timeRow.getChildren().addAll(clockIco, timeLbl);

        VBox dateBox = new VBox(4);
        dateBox.setAlignment(Pos.CENTER_RIGHT);
        dateBox.getChildren().addAll(dateRow, timeRow);

        rightBox.getChildren().add(dateBox);
        if (bellNode != null) rightBox.getChildren().add(bellNode);

        bar.getChildren().addAll(greeting, rightBox);
        return bar;
    }

    /** Helper local: Label con fuente Font Awesome. */
    private static Label faLabel(String icon, double size, String color) {
        Label lbl = new Label(icon);
        lbl.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';"
                + "-fx-font-size:" + size + "px;-fx-text-fill:" + color + ";");
        return lbl;
    }
}
