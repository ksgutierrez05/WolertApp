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
import java.util.List;
import java.util.stream.Collectors;
import sistemagestion.model.*;
import sistemagestion.service.*;

public class NotificacionesPoliciaView {

    private static final String WHITE     = "#ffffff";
    private static final String BG        = "#f4f6fb";
    private static final String RED       = "#e53935";
    private static final String ORANGE    = "#fb8c00";
    private static final String GREEN     = "#43a047";
    private static final String BLUE      = "#1565c0";
    private static final String GRAY_TEXT = "#6b7280";
    private static final String BORDER    = "#e5e7eb";

    private final Usuario usuarioActual;
    private final NotificacionService notificacionService;

    public NotificacionesPoliciaView(Usuario usuarioActual, NotificacionService notificacionService) {
        this.usuarioActual        = usuarioActual;
        this.notificacionService  = notificacionService;
    }

    public ScrollPane build() {
        VBox content = new VBox(16);
        content.setPadding(new Insets(22));
        content.setStyle("-fx-background-color: " + BG + ";");

        Label title = new Label("📢 Notificaciones");
        title.setFont(Font.font("System", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#111827"));
        content.getChildren().add(title);

        VBox panel = createPanel("Mis notificaciones");
        try {
            List<Notificacion> lista = notificacionService.listar();

            // Filtrar por usuario actual
            List<Notificacion> mias = lista.stream()
                    .filter(n -> n.getUsuario() != null && usuarioActual != null
                            && (usuarioActual.getUsername() != null
                                && usuarioActual.getUsername().equals(n.getUsuario().getUsername())
                                || usuarioActual.getCorreo() != null
                                && usuarioActual.getCorreo().equals(n.getCorreodestinatario())))
                    .collect(Collectors.toList());

            List<Notificacion> mostrar = mias.isEmpty()
                    ? lista.subList(0, Math.min(lista.size(), 20))
                    : mias;

            if (mostrar.isEmpty()) {
                panel.getChildren().add(label("No hay notificaciones.", 13, GRAY_TEXT, false));
            } else {
                for (Notificacion n : mostrar) {
                    String dest   = n.getCorreodestinatario() != null ? n.getCorreodestinatario() : "—";
                    String msg    = n.getMensaje()             != null ? n.getMensaje()             : "—";
                    String estStr = n.getEstado()              != null
                            ? n.getEstado().name().replace("_", " ") : "—";
                    String color  = n.getEstado() == EstadoNotificacion.LEIDA  ? GREEN
                            : n.getEstado() == EstadoNotificacion.ENVIADA ? BLUE
                            : n.getEstado() == EstadoNotificacion.ERROR   ? RED : ORANGE;
                    // Si son propias solo mostramos estado+msg, si son de todos mostramos dest también
                    String tituloItem = mias.isEmpty() ? dest + " — " + estStr : estStr;
                    panel.getChildren().addAll(
                            listItem("📢", tituloItem, msg, color), separator());
                }
            }
        } catch (Exception e) {
            panel.getChildren().add(label("Error: " + e.getMessage(), 12, RED, false));
        }
        content.getChildren().add(panel);
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
}