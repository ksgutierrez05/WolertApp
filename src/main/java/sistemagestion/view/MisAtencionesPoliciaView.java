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
        this.usuarioActual   = usuarioActual;
        this.policiaActual   = policiaActual;
        this.atencionService = atencionService;
        this.root            = root;
    }

    public ScrollPane build() {
        VBox content = new VBox(18);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color: " + BG + ";");

        // ── Título ────────────────────────────────────────────────
        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        StackPane titleIcon = new StackPane();
        Rectangle titleBg = new Rectangle(42, 42);
        titleBg.setArcWidth(12); titleBg.setArcHeight(12);
        titleBg.setFill(Color.web("#e8f0fe"));
        Label titleIcoLbl = faIcon("\uf46d", 18, BLUE);
        titleIcon.getChildren().addAll(titleBg, titleIcoLbl);

        VBox titleText = new VBox(2);
        Label titleLbl = new Label("Mis atenciones");
        titleLbl.setFont(Font.font("System", FontWeight.BOLD, 22));
        titleLbl.setTextFill(Color.web("#111827"));
        Label subLbl = label("Registro y seguimiento de tus atenciones", 12, GRAY_TEXT, false);
        titleText.getChildren().addAll(titleLbl, subLbl);

        titleRow.getChildren().addAll(titleIcon, titleText);
        content.getChildren().add(titleRow);

        try {
            List<AtencionAlerta> lista = obtenerMisAtenciones();
            if (lista.isEmpty()) {
                VBox vacio = new VBox(14);
                vacio.setAlignment(Pos.CENTER);
                vacio.setPadding(new Insets(40));
                vacio.setStyle("-fx-background-color: " + WHITE + "; -fx-background-radius: 16;");
                shadow(vacio);
                Label icoVacio = faIcon("\uf46d", 40, "#d1d5db");
                Label txtVacio = label("No tienes atenciones registradas.", 14, GRAY_TEXT, false);
                vacio.getChildren().addAll(icoVacio, txtVacio);
                content.getChildren().add(vacio);
            } else {
                for (AtencionAlerta at : lista) {
                    content.getChildren().add(buildAtencionCard(at));
                }
            }
        } catch (Exception e) {
            VBox err = new VBox(8);
            err.setPadding(new Insets(16));
            err.setStyle("-fx-background-color: #fff0f0; -fx-background-radius: 12;");
            err.getChildren().add(label("Error: " + e.getMessage(), 12, RED, false));
            content.getChildren().add(err);
        }

        return wrapScroll(content);
    }

    private VBox buildAtencionCard(AtencionAlerta at) {
        VBox card = new VBox(0);
        card.setStyle("-fx-background-color: " + WHITE + "; -fx-background-radius: 16;");
        shadow(card);

        String estadoStr = at.getEstado() != null ? at.getEstado().name().replace("_", " ") : "—";
        String colorEst  = switch (at.getEstado() != null ? at.getEstado() : EstadoAtencionAlerta.PENDIENTE) {
            case FINALIZADA -> GREEN;
            case EN_PROCESO -> ORANGE;
            case CANCELADA  -> GRAY_TEXT;
            default         -> RED;
        };

        // ── Header coloreado ──────────────────────────────────────
        HBox header = new HBox(12);
        header.setPadding(new Insets(16, 18, 16, 18));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: " + colorEst + "18;"
                + "-fx-background-radius: 16 16 0 0;");

        StackPane dotBox = new StackPane();
        Rectangle dotBg = new Rectangle(38, 38);
        dotBg.setArcWidth(10); dotBg.setArcHeight(10);
        dotBg.setFill(Color.web(colorEst + "33"));
        Label dotIco = faIcon("\uf46d", 15, colorEst);
        dotBox.getChildren().addAll(dotBg, dotIco);

        String unidadNom = at.getUnidad() != null ? at.getUnidad().getNombre() : "—";
        VBox hTxt = new VBox(3);
        HBox.setHgrow(hTxt, Priority.ALWAYS);
        hTxt.getChildren().addAll(
                label("Atención #" + at.getId_atencion() + "  —  " + unidadNom, 14, "#111827", true),
                label("Fecha: " + formatFechaAt(at.getFechaatencion()), 11, GRAY_TEXT, false));

        Label badge = new Label(estadoStr);
        badge.setFont(Font.font("System", FontWeight.BOLD, 10));
        badge.setTextFill(Color.web(colorEst));
        badge.setPadding(new Insets(4, 10, 4, 10));
        badge.setStyle("-fx-background-color: " + colorEst + "22;"
                + "-fx-background-radius: 20;"
                + "-fx-border-color: " + colorEst + "55;"
                + "-fx-border-radius: 20;"
                + "-fx-border-width: 1;");

        header.getChildren().addAll(dotBox, hTxt, badge);

        // ── Cuerpo ────────────────────────────────────────────────
        VBox body = new VBox(12);
        body.setPadding(new Insets(16, 18, 0, 18));

        Label descLbl = label("Situación", 11, GRAY_TEXT, false);
        Label descVal = label(at.getDescripcion() != null ? at.getDescripcion() : "—", 13, "#111827", false);
        descVal.setWrapText(true);

        Label obsLbl = label("Observación", 11, GRAY_TEXT, false);
        Label obsVal = label(at.getObservacion() != null ? at.getObservacion() : "—", 12, "#374151", false);
        obsVal.setWrapText(true);

        body.getChildren().addAll(
                infoBlock(descLbl, descVal),
                infoBlock(obsLbl, obsVal));

        // ── Panel de actualización (casi transparente) ────────────
        VBox updatePanel = new VBox(10);
        updatePanel.setPadding(new Insets(14, 18, 18, 18));
        updatePanel.setStyle(
                "-fx-background-color: rgba(21,101,192,0.04);"
                + "-fx-border-color: rgba(21,101,192,0.10);"
                + "-fx-border-width: 1 0 0 0;");

        HBox updateHeader = new HBox(6);
        updateHeader.setAlignment(Pos.CENTER_LEFT);
        updateHeader.getChildren().addAll(
                faIcon("\uf304", 12, BLUE),
                label("Actualizar atención", 12, BLUE, true));

        TextArea nuevaSituacion = new TextArea();
        nuevaSituacion.setPromptText("Actualiza la descripción de la situación...");
        nuevaSituacion.setText(at.getDescripcion() != null ? at.getDescripcion() : "");
        nuevaSituacion.setPrefRowCount(2);
        nuevaSituacion.setWrapText(true);
        nuevaSituacion.setStyle(
                "-fx-font-size: 12;"
                + "-fx-background-color: rgba(255,255,255,0.7);"
                + "-fx-background-radius: 8;"
                + "-fx-border-color: " + BORDER + ";"
                + "-fx-border-radius: 8;"
                + "-fx-border-width: 1;");

        // ComboBox casi transparente
        ComboBox<String> nuevoEstCombo = new ComboBox<>();
        nuevoEstCombo.getItems().addAll("PENDIENTE", "EN_PROCESO", "FINALIZADA", "CANCELADA");
        if (at.getEstado() != null) nuevoEstCombo.setValue(at.getEstado().name());
        nuevoEstCombo.setPrefWidth(200);
        nuevoEstCombo.setStyle(
                "-fx-font-size: 12px;"
                + "-fx-background-color: rgba(255,255,255,0.5);"
                + "-fx-background-radius: 8;"
                + "-fx-border-color: rgba(21,101,192,0.25);"
                + "-fx-border-radius: 8;"
                + "-fx-border-width: 1;"
                + "-fx-text-fill: #374151;");

        Label nuevoEstLbl = label("Nuevo estado", 11, GRAY_TEXT, false);

        Button btnActualizar = new Button();
        Label btnIco = faIcon("\uf021", 12, WHITE);
        Label btnTxt = label("  Actualizar", 12, WHITE, true);
        HBox btnContent = new HBox(4, btnIco, btnTxt);
        btnContent.setAlignment(Pos.CENTER);
        btnActualizar.setGraphic(btnContent);
        btnActualizar.setStyle(
                "-fx-background-color: " + BLUE + ";"
                + "-fx-background-radius: 8;"
                + "-fx-cursor: hand;"
                + "-fx-padding: 7 18 7 18;");

        Label msgLbl = new Label("");
        msgLbl.setFont(Font.font("System", 11));

        btnActualizar.setOnAction(ev -> {
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
                    msgLbl.setText("✔  Atención actualizada correctamente");
                    msgLbl.setTextFill(Color.web(GREEN));
                    root.setCenter(new MisAtencionesPoliciaView(
                            usuarioActual, policiaActual, atencionService, root).build());
                } else {
                    msgLbl.setText("✘  No se pudo actualizar");
                    msgLbl.setTextFill(Color.web(RED));
                }
            } catch (Exception ex) {
                msgLbl.setText("✘  " + ex.getMessage());
                msgLbl.setTextFill(Color.web(RED));
            }
        });

        HBox bottomRow = new HBox(10);
        bottomRow.setAlignment(Pos.CENTER_LEFT);
        VBox estadoBox = new VBox(4, nuevoEstLbl, nuevoEstCombo);
        HBox.setHgrow(estadoBox, Priority.ALWAYS);
        bottomRow.getChildren().addAll(estadoBox, btnActualizar);

        updatePanel.getChildren().addAll(updateHeader, nuevaSituacion, bottomRow, msgLbl);

        card.getChildren().addAll(header, body, updatePanel);
        return card;
    }

    // ── Info block ───────────────────────────────────────────────
    private VBox infoBlock(Label lbl, Label val) {
        VBox box = new VBox(2, lbl, val);
        box.setPadding(new Insets(0, 0, 4, 0));
        return box;
    }

    // ── FA icon ──────────────────────────────────────────────────
    private Label faIcon(String code, double size, String color) {
        Label lbl = new Label(code);
        lbl.setStyle(
                "-fx-font-family: 'Font Awesome 6 Free Solid';"
                + "-fx-font-size: " + size + "px;"
                + "-fx-text-fill: " + color + ";");
        return lbl;
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
        node.setEffect(new DropShadow(12, 0, 3, Color.web("#0000001a")));
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
}