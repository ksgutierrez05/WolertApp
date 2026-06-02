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

        VBox card = new VBox();
        card.setStyle(
                "-fx-background-color: white;"
                + "-fx-background-radius: 12;"
        );

        card.setEffect(new DropShadow(
                12,
                Color.rgb(0, 0, 0, 0.12)
        ));

        GridPane tabla = new GridPane();
        tabla.setMaxWidth(Double.MAX_VALUE);

        ColumnConstraints c1 = new ColumnConstraints();
        c1.setPercentWidth(45);

        ColumnConstraints c2 = new ColumnConstraints();
        c2.setPercentWidth(25);

        ColumnConstraints c3 = new ColumnConstraints();
        c3.setPercentWidth(15);

        ColumnConstraints c4 = new ColumnConstraints();
        c4.setPercentWidth(15);

        tabla.getColumnConstraints().addAll(c1, c2, c3, c4);

        agregarHeader(tabla, 0, "Mensaje");
        agregarHeader(tabla, 1, "Destinatario");
        agregarHeader(tabla, 2, "Fecha");
        agregarHeader(tabla, 3, "Estado");

        List<Notificacion> notifs = cargarNotificaciones();

        if (notifs.isEmpty()) {

            Label vacio = label(
                    "No hay notificaciones registradas.",
                    14,
                    GRAY_TEXT,
                    false
            );

            VBox.setMargin(vacio, new Insets(25));

            card.getChildren().add(vacio);
            return card;
        }

        int fila = 1;

        for (Notificacion n : notifs) {

            String mensaje = n.getMensaje() != null
                    ? n.getMensaje()
                    : "—";

            String destinatario = n.getCorreodestinatario() != null
                    ? n.getCorreodestinatario()
                    : "—";

            String fecha = n.getFechahora() != null
                    ? n.getFechahora().toLocalDate().toString()
                    : "—";

            String estado = n.getEstado() != null
                    ? n.getEstado().name()
                    : "—";

            agregarCelda(tabla, fila, 0, mensaje);
            agregarCelda(tabla, fila, 1, destinatario);
            agregarCelda(tabla, fila, 2, fecha);
            agregarCelda(tabla, fila, 3, estado);

            fila++;
        }

        card.getChildren().add(tabla);

        return card;
    }

    private void agregarHeader(GridPane tabla, int columna, String texto) {

        Label lbl = new Label(texto);

        lbl.setFont(Font.font("System", FontWeight.BOLD, 13));

        lbl.setTextFill(Color.web("#6b7280"));

        lbl.setMaxWidth(Double.MAX_VALUE);

        lbl.setPadding(new Insets(15));

        lbl.setStyle(
                "-fx-background-color:#f8fafc;"
                + "-fx-border-color:#e5e7eb;"
                + "-fx-border-width:0 0 1 0;"
        );

        tabla.add(lbl, columna, 0);
    }

    private void agregarCelda(
            GridPane tabla,
            int fila,
            int columna,
            String texto) {

        Label lbl = new Label(texto);

        lbl.setWrapText(true);

        lbl.setMaxWidth(Double.MAX_VALUE);

        lbl.setPadding(new Insets(15));

        lbl.setTextFill(Color.web("#374151"));

        lbl.setStyle(
                "-fx-border-color:#e5e7eb;"
                + "-fx-border-width:0 0 1 0;"
        );

        tabla.add(lbl, columna, fila);
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
