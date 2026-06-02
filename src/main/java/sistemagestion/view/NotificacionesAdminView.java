/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.view;

/**
 *
 * @author Maria Cristina
 */
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import sistemagestion.model.Notificacion;

import sistemagestion.service.NotificacionService;

public class NotificacionesAdminView {

    private static final String WHITE = "#ffffff";
    private static final String BG = "#f4f6fb";
    private static final String BLUE = "#1565c0";
    private static final String GRAY_TEXT = "#6b7280";
    private static final String BORDER = "#e5e7eb";

    private final NotificacionService notificacionService;

    public NotificacionesAdminView(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    public ScrollPane getView() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color: " + BG + ";");

        content.getChildren().addAll(buildTopBar(), buildTabla());

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: " + BG + "; -fx-background: " + BG + ";");
        return scroll;
    }

    private HBox buildTopBar() {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Notificaciones del sistema");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#111827"));
        bar.getChildren().add(title);
        return bar;
    }

    private VBox buildTabla() {
        VBox card = new VBox(0);
        card.setStyle("-fx-background-color: " + WHITE + "; -fx-background-radius: 12;");
        card.setEffect(new DropShadow(12, 0, 2, Color.web("#0000001a")));

        HBox header = new HBox();
        header.setPadding(new Insets(14, 16, 14, 16));
        header.setStyle("-fx-border-color: transparent transparent " + BORDER
                + " transparent; -fx-border-width: 0 0 1 0;");
        header.getChildren().addAll(
                colHeader("Mensaje"),
                colHeader("Destinatario"),
                colHeader("Fecha"),
                colHeader("Estado")
        );
        card.getChildren().add(header);

        List<Notificacion> notifs = cargarNotificaciones();

        if (notifs.isEmpty()) {
            Label vacio = label("No hay notificaciones registradas.", 14, GRAY_TEXT, false);
            VBox.setMargin(vacio, new Insets(24));
            card.getChildren().add(vacio);
            return card;
        }

        boolean par = true;
        for (Notificacion n : notifs) {
            card.getChildren().add(buildFila(n, par));
            par = !par;
        }
        return card;
    }

    private HBox buildFila(Notificacion n, boolean par) {
        HBox fila = new HBox();
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.setPadding(new Insets(12, 16, 12, 16));
        fila.setStyle("-fx-background-color: " + (par ? WHITE : "#fafbfd") + ";"
                + "-fx-border-color: transparent transparent " + BORDER + " transparent;"
                + "-fx-border-width: 0 0 1 0;");

        String mensaje = n.getMensaje() != null ? n.getMensaje() : "—";
        String dest = n.getCorreodestinatario() != null ? n.getCorreodestinatario() : "—";
        String fecha = n.getFechahora() != null
                ? n.getFechahora().toLocalDate().toString() : "—";
        String estado = n.getEstado() != null ? n.getEstado().name() : "—";

        fila.getChildren().addAll(
                celda(mensaje.length() > 50 ? mensaje.substring(0, 50) + "…" : mensaje),
                celda(dest),
                celda(fecha),
                celda(estado)
        );
        return fila;
    }

    private List<Notificacion> cargarNotificaciones() {
        try {
            return notificacionService.listar();
        } catch (Exception e) {
            return List.of();
        }
    }

    private Label colHeader(String text) {
        Label l = label(text, 12, GRAY_TEXT, false);
        l.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(l, Priority.ALWAYS);
        return l;
    }

    private Label celda(String txt) {
        Label l = label(txt, 13, "#374151", false);
        l.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(l, Priority.ALWAYS);
        return l;
    }

    private Label label(String text, double size, String color, boolean bold) {
        Label lbl = new Label(text);
        lbl.setFont(bold
                ? Font.font("System", FontWeight.BOLD, size)
                : Font.font("System", size));
        lbl.setTextFill(Color.web(color));
        return lbl;
    }
}
