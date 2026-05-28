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
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import sistemagestion.model.*;
import sistemagestion.service.AlertaService;

/**
 * Vista: subvistas del mapa (Mapa de alertas, Zonas peligrosas, Alertas comunitarias).
 * Uso: root.setCenter(new MapaAdminPoliciaView(alertaService, "Mapa de alertas").build());
 */
public class MapaAdminPoliciaView {

    private static final String BG        = "#f4f6fb";
    private static final String WHITE     = "#ffffff";
    private static final String RED       = "#e53935";
    private static final String ORANGE    = "#fb8c00";
    private static final String GREEN     = "#43a047";
    private static final String GRAY_TEXT = "#6b7280";
    private static final String BORDER    = "#e5e7eb";

    private final AlertaService alertaService;
    /** Nombre de la subvista: "Mapa de alertas", "Zonas peligrosas" o "Alertas comunitarias" */
    private final String nombre;

    public MapaAdminPoliciaView(AlertaService alertaService, String nombre) {
        this.alertaService = alertaService;
        this.nombre        = nombre;
    }

    public ScrollPane build() {
        VBox content = new VBox(16);
        content.setPadding(new Insets(22));
        content.setStyle("-fx-background-color: " + BG + ";");

        Label title = new Label("📍 " + nombre);
        title.setFont(Font.font("System", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#111827"));

        VBox panel = createPanel("Alertas relacionadas");
        try {
            List<Alerta> alertas = alertaService.listar();
            if (alertas.isEmpty()) {
                panel.getChildren().add(label("No hay alertas registradas.", 13, GRAY_TEXT, false));
            } else {
                alertas.stream().limit(10).forEach(a -> {
                    String dotColor = estadoColor(a.getEstado());
                    String tipo    = a.getTipoalerta() != null ? a.getTipoalerta().getNombre() : "—";
                    String barrio  = a.getBarrio()     != null ? a.getBarrio().getNombre()     : "—";
                    String estado  = a.getEstado()     != null ? a.getEstado().name().replace("_", " ") : "—";
                    panel.getChildren().addAll(
                            alertItem("🔔", tipo + " — " + barrio,
                                    formatFecha(a.getFechaHora()) + " · " + estado, dotColor),
                            separator());
                });
            }
        } catch (Exception e) {
            panel.getChildren().add(label("Error: " + e.getMessage(), 12, RED, false));
        }
        content.getChildren().addAll(title, panel);
        return wrapScroll(content);
    }

    // ── Helpers ──────────────────────────────────────────────────
    private String estadoColor(EstadoAlerta estado) {
        if (estado == null) return GRAY_TEXT;
        return switch (estado) {
            case PENDIENTE, EN_ATENCION -> RED;
            case UNIDAD_ASIGNADA        -> ORANGE;
            case RESUELTA               -> GREEN;
            default                     -> GRAY_TEXT;
        };
    }

    private String formatFecha(LocalDateTime dt) {
        if (dt == null) return "—";
        long mins = java.time.Duration.between(dt, LocalDateTime.now()).toMinutes();
        if (mins < 1)    return "Hace un momento";
        if (mins < 60)   return "Hace " + mins + " min";
        if (mins < 1440) return "Hace " + (mins / 60) + " h";
        return dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    private VBox createPanel(String title) {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(16));
        panel.setStyle("-fx-background-color: " + WHITE + "; -fx-background-radius: 12;");
        panel.getChildren().addAll(label(title, 14, "#111827", true), separator());
        return panel;
    }

    private HBox alertItem(String icon, String title, String sub, String dotColor) {
        HBox row = new HBox(10);
        row.setPadding(new Insets(7, 0, 7, 0));
        row.setCursor(javafx.scene.Cursor.HAND);
        Circle dot = new Circle(5, Color.web(dotColor));
        StackPane iconBox = new StackPane();
        Rectangle bg = new Rectangle(33, 33);
        bg.setArcWidth(7); bg.setArcHeight(7);
        bg.setFill(Color.web(BG));
        iconBox.getChildren().addAll(bg, label(icon, 14, dotColor, false));
        VBox text = new VBox(2);
        HBox.setHgrow(text, Priority.ALWAYS);
        text.getChildren().addAll(label(title, 12, "#111827", false), label(sub, 10, GRAY_TEXT, false));
        row.getChildren().addAll(dot, iconBox, text, label(">", 13, GRAY_TEXT, false));
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
