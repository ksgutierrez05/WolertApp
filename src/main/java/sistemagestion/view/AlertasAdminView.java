/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.view;

/**
 *
 * @author Maria Cristina
 */
// view/AlertasAdminView.java
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import sistemagestion.model.Alerta;
import static sistemagestion.model.EstadoAlarma.ACTIVA;
import sistemagestion.model.EstadoAlerta;
import static sistemagestion.model.EstadoAtencionAlerta.EN_PROCESO;
import sistemagestion.service.AlertaService;

public class AlertasAdminView {

    private static final String WHITE = "#ffffff";
    private static final String BG = "#f4f6fb";
    private static final String RED = "#e53935";
    private static final String RED_LIGHT = "#fff0f0";
    private static final String ORANGE = "#fb8c00";
    private static final String GREEN = "#43a047";
    private static final String BLUE = "#1565c0";
    private static final String GRAY_TEXT = "#6b7280";
    private static final String BORDER = "#e5e7eb";

    private final AlertaService alertaService;

    public AlertasAdminView(AlertaService alertaService) {
        this.alertaService = alertaService;
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
        Label title = new Label("Gestión de Alertas");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#111827"));
        bar.getChildren().add(title);
        return bar;
    }

    private VBox buildTabla() {
        VBox card = new VBox(0);
        card.setStyle("-fx-background-color: " + WHITE + "; -fx-background-radius: 12;");
        card.setEffect(new DropShadow(12, 0, 2, Color.web("#0000001a")));

        // Encabezado
        HBox header = new HBox();
        header.setPadding(new Insets(14, 16, 14, 16));
        header.setStyle("-fx-border-color: transparent transparent " + BORDER
                + " transparent; -fx-border-width: 0 0 1 0;");
        header.getChildren().addAll(
                colHeader("Tipo"),
                colHeader("Barrio"),
                colHeader("Usuario"),
                colHeader("Estado"),
                colHeader("Fecha"),
                colHeader("Descripción")
        );
        card.getChildren().add(header);

        List<Alerta> alertas = cargarAlertas();

        if (alertas.isEmpty()) {
            Label vacio = label("No hay alertas registradas.", 14, GRAY_TEXT, false);
            VBox.setMargin(vacio, new Insets(24));
            card.getChildren().add(vacio);
            return card;
        }

        boolean par = true;
        for (Alerta a : alertas) {
            card.getChildren().add(buildFila(a, par));
            par = !par;
        }
        return card;
    }

    private HBox buildFila(Alerta a, boolean par) {
        HBox fila = new HBox();
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.setPadding(new Insets(12, 16, 12, 16));
        fila.setStyle("-fx-background-color: " + (par ? WHITE : "#fafbfd") + ";"
                + "-fx-border-color: transparent transparent " + BORDER + " transparent;"
                + "-fx-border-width: 0 0 1 0;");
        fila.setOnMouseEntered(e -> fila.setStyle(
                "-fx-background-color: #EEF2FF; -fx-cursor: hand;"
                + "-fx-border-color: transparent transparent " + BORDER + " transparent;"
                + "-fx-border-width: 0 0 1 0;"));
        fila.setOnMouseExited(e -> fila.setStyle(
                "-fx-background-color: " + (par ? WHITE : "#fafbfd") + ";"
                + "-fx-border-color: transparent transparent " + BORDER + " transparent;"
                + "-fx-border-width: 0 0 1 0;"));

        String tipo = a.getTipoalerta() != null ? a.getTipoalerta().getNombre() : "—";
        String barrio = a.getBarrio() != null ? a.getBarrio().getNombre() : "—";
        String usuario = a.getUsuario() != null ? a.getUsuario().getPrimer_nombre() : "—";
        String estado = a.getEstado() != null ? a.getEstado().name() : "—";
        String fecha = a.getFechaHora() != null ? a.getFechaHora().toLocalDate().toString() : "—";
        String desc = a.getDescripcion() != null ? a.getDescripcion() : "—";

        Label estadoLbl = label(estado, 11, estadoColor(a.getEstado()), true);
        estadoLbl.setPadding(new Insets(3, 8, 3, 8));
        estadoLbl.setStyle("-fx-background-color: " + estadoBg(a.getEstado())
                + "; -fx-background-radius: 20;");
        HBox estadoBox = new HBox(estadoLbl);
        HBox.setHgrow(estadoBox, Priority.ALWAYS);

        fila.getChildren().addAll(
                celda(tipo), celda(barrio), celda(usuario),
                estadoBox,
                celda(fecha),
                celda(desc.length() > 40 ? desc.substring(0, 40) + "…" : desc)
        );
        return fila;
    }

    private List<Alerta> cargarAlertas() {
        try {
            return alertaService.listar();
        } catch (Exception e) {
            return List.of();
        }
    }

    private String estadoColor(EstadoAlerta e) {
        if (e == null) {
            return GRAY_TEXT;
        }
        return switch (e) {
            case PENDIENTE ->
                ORANGE;
            case RECIBIDA ->
                BLUE;
            case EN_ATENCION ->
                "#7b1fa2";
            case UNIDAD_ASIGNADA ->
                "#0288d1";
            case RESUELTA ->
                GREEN;
            case CANCELADA ->
                GRAY_TEXT;
        };
    }

    private String estadoBg(EstadoAlerta e) {
        if (e == null) {
            return "#f3f4f6";
        }
        return switch (e) {
            case PENDIENTE ->
                "#fff8e1";
            case RECIBIDA ->
                "#e8f0fe";
            case EN_ATENCION ->
                "#f3e5f5";
            case UNIDAD_ASIGNADA ->
                "#e1f5fe";
            case RESUELTA ->
                "#e8f5e9";
            case CANCELADA ->
                "#f3f4f6";
        };
    }

    private Label colHeader(String text) {
        Label l = label(text, 12, GRAY_TEXT, false);
        l.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(l, Priority.ALWAYS);
        return l;
    }

    private Label celda(String txt) {
        Label l = label(txt != null ? txt : "—", 13, "#374151", false);
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
