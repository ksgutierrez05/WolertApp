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
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import sistemagestion.model.*;
import sistemagestion.service.*;

public class MisAtencionesPoliciaView {

    private static final String WHITE     = "#ffffff";
    private static final String BG        = "#f4f6fb";
    private static final String RED       = "#e53935";
    private static final String ORANGE    = "#fb8c00";
    private static final String GREEN     = "#43a047";
    private static final String BLUE      = "#1565c0";
    private static final String GRAY_TEXT = "#6b7280";
    private static final String BORDER    = "#e5e7eb";

    private final Usuario usuarioActual;
    private final Policia policiaActual;
    private final AtencionAlertaService atencionService;
    private final BorderPane root;

    public MisAtencionesPoliciaView(Usuario usuarioActual, Policia policiaActual,
            AtencionAlertaService atencionService, BorderPane root) {
        this.usuarioActual  = usuarioActual;
        this.policiaActual  = policiaActual;
        this.atencionService = atencionService;
        this.root           = root;
    }

    public ScrollPane build() {
        VBox content = new VBox(16);
        content.setPadding(new Insets(22));
        content.setStyle("-fx-background-color: " + BG + ";");

        Label title = new Label("📋 Mis atenciones");
        title.setFont(Font.font("System", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#111827"));
        content.getChildren().add(title);

        try {
            List<AtencionAlerta> lista = obtenerMisAtenciones();
            if (lista.isEmpty()) {
                VBox vacio = createPanel("Sin atenciones");
                vacio.getChildren().add(label("No tienes atenciones registradas.", 13, GRAY_TEXT, false));
                content.getChildren().add(vacio);
            } else {
                for (AtencionAlerta at : lista) {
                    content.getChildren().add(buildAtencionCard(at));
                }
            }
        } catch (Exception e) {
            VBox err = createPanel("Error");
            err.getChildren().add(label("Error: " + e.getMessage(), 12, RED, false));
            content.getChildren().add(err);
        }

        return wrapScroll(content);
    }

    private VBox buildAtencionCard(AtencionAlerta at) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: " + WHITE + "; -fx-background-radius: 12;");
        shadow(card);

        String estadoStr = at.getEstado() != null ? at.getEstado().name().replace("_", " ") : "—";
        String colorEst  = switch (at.getEstado() != null ? at.getEstado() : EstadoAtencionAlerta.PENDIENTE) {
            case FINALIZADA -> GREEN;
            case EN_PROCESO -> ORANGE;
            case CANCELADA  -> GRAY_TEXT;
            default         -> RED;
        };

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Circle dot = new Circle(7, Color.web(colorEst));
        String unidadNom = at.getUnidad() != null ? at.getUnidad().getNombre() : "—";
        VBox hTxt = new VBox(2);
        HBox.setHgrow(hTxt, Priority.ALWAYS);
        hTxt.getChildren().addAll(
                label("📋 Atención #" + at.getId_atencion() + " — " + unidadNom, 14, "#111827", true),
                label("Fecha: " + formatFechaAt(at.getFechaatencion()), 11, GRAY_TEXT, false));
        Label badge = label(estadoStr, 11, colorEst, true);
        badge.setPadding(new Insets(3, 8, 3, 8));
        badge.setStyle("-fx-background-color: " + colorEst + "22; -fx-background-radius: 6;");
        header.getChildren().addAll(dot, hTxt, badge);

        Label descActual = label(
                "Situación: " + (at.getDescripcion() != null ? at.getDescripcion() : "—"), 12, "#374151", false);
        descActual.setWrapText(true);
        Label obsActual = label(
                "Observación: " + (at.getObservacion() != null ? at.getObservacion() : "—"), 11, GRAY_TEXT, false);
        obsActual.setWrapText(true);

        card.getChildren().add(separator());

        Label editTitle = label("✏ Actualizar situación / estado", 13, BLUE, true);

        TextArea nuevaSituacion = new TextArea();
        nuevaSituacion.setPromptText("Agrega más detalles o actualiza la descripción...");
        nuevaSituacion.setText(at.getDescripcion() != null ? at.getDescripcion() : "");
        nuevaSituacion.setPrefRowCount(3);
        nuevaSituacion.setWrapText(true);
        nuevaSituacion.setStyle("-fx-font-size: 12; -fx-background-radius: 8;");

        Label nuevoEstLbl = label("Nuevo estado:", 12, "#374151", false);
        ComboBox<String> nuevoEstCombo = new ComboBox<>();
        nuevoEstCombo.getItems().addAll("PENDIENTE", "EN_PROCESO", "FINALIZADA", "CANCELADA");
        if (at.getEstado() != null) nuevoEstCombo.setValue(at.getEstado().name());
        nuevoEstCombo.setPrefWidth(200);

        Button actualizar = new Button("🔄  Actualizar atención");
        actualizar.setStyle("-fx-background-color: " + ORANGE + "; -fx-text-fill: white; -fx-font-size: 12;"
                + " -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 7 18 7 18;");
        actualizar.setOnAction(ev -> {
            String desc   = nuevaSituacion.getText().trim();
            String estado = nuevoEstCombo.getValue();
            if (desc.isEmpty()) {
                mostrarAlerta("Campo requerido", "La descripción no puede quedar vacía.");
                return;
            }
            try {
                AtencionAlerta upd = new AtencionAlerta();
                upd.setId_atencion(at.getId_atencion());
                upd.setAlerta(at.getAlerta());
                upd.setUnidad(at.getUnidad());
                upd.setDescripcion(desc);
                upd.setEstado(EstadoAtencionAlerta.valueOf(estado));
                upd.setTipoarma(at.getTipoarma());
                upd.setMediotransporte(at.getMediotransporte());
                upd.setObservacion(at.getObservacion());
                boolean ok = atencionService.actualizar(upd);
                if (ok) {
                    mostrarInfo("¡Actualizado!", "La atención fue actualizada correctamente.");
                    root.setCenter(new MisAtencionesPoliciaView(
                            usuarioActual, policiaActual, atencionService, root).build());
                } else {
                    mostrarAlerta("Error", "No se pudo actualizar la atención.");
                }
            } catch (Exception ex) {
                mostrarAlerta("Error", ex.getMessage());
            }
        });

        card.getChildren().addAll(header, descActual, obsActual, separator(),
                editTitle, nuevaSituacion, new VBox(4, nuevoEstLbl, nuevoEstCombo), actualizar);
        return card;
    }

    // ── Datos ────────────────────────────────────────────────────
    private List<AtencionAlerta> obtenerMisAtenciones() throws Exception {
        if (atencionService == null) return List.of();
        List<AtencionAlerta> todas = atencionService.listar();
        if (policiaActual == null || policiaActual.getUnidadpolicial() == null) return todas;
        String miUnidad = policiaActual.getUnidadpolicial().getNombre();
        List<AtencionAlerta> mias = todas.stream()
                .filter(at -> at.getUnidad() != null
                        && miUnidad.equalsIgnoreCase(at.getUnidad().getNombre()))
                .collect(java.util.stream.Collectors.toList());
        return mias.isEmpty() ? todas : mias;
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

    private String formatFechaAt(LocalDateTime dt) {
        if (dt == null) return "—";
        return dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    private void mostrarAlerta(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(titulo); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    private void mostrarInfo(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(titulo); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}
