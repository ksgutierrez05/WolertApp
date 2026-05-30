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
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import sistemagestion.model.*;
import sistemagestion.service.*;

public class MisAlertasPoliciaView {

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
    private final AlertaService alertaService;
    private final AtencionAlertaService atencionService;
    private final BorderPane root;

    public MisAlertasPoliciaView(Usuario usuarioActual, Policia policiaActual,
            AlertaService alertaService, AtencionAlertaService atencionService,
            BorderPane root) {
        this.usuarioActual  = usuarioActual;
        this.policiaActual  = policiaActual;
        this.alertaService  = alertaService;
        this.atencionService = atencionService;
        this.root           = root;
    }

    public ScrollPane build() {
        VBox content = new VBox(16);
        content.setPadding(new Insets(22));
        content.setStyle("-fx-background-color: " + BG + ";");

        Label title = new Label("🚨 Mis alertas asignadas");
        title.setFont(Font.font("System", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#111827"));
        content.getChildren().add(title);

        try {
            List<Alerta> lista = obtenerMisAlertas();
            if (lista.isEmpty()) {
                VBox vacio = createPanel("Sin alertas");
                vacio.getChildren().add(label("No tienes alertas asignadas en este momento.", 13, GRAY_TEXT, false));
                content.getChildren().add(vacio);
            } else {
                for (Alerta a : lista) {
                    content.getChildren().add(buildAlertaCard(a));
                }
            }
        } catch (Exception e) {
            VBox err = createPanel("Error");
            err.getChildren().add(label("Error al cargar alertas: " + e.getMessage(), 12, RED, false));
            content.getChildren().add(err);
        }

        return wrapScroll(content);
    }

    private VBox buildAlertaCard(Alerta a) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: " + WHITE + "; -fx-background-radius: 12;");
        shadow(card);

        // Encabezado
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        String dotColor = estadoColor(a.getEstado());
        Circle dot = new Circle(7, Color.web(dotColor));
        String tipo   = a.getTipoalerta() != null ? a.getTipoalerta().getNombre() : "—";
        String barrio = a.getBarrio()     != null ? a.getBarrio().getNombre()     : "—";
        VBox headerTxt = new VBox(2);
        HBox.setHgrow(headerTxt, Priority.ALWAYS);
        headerTxt.getChildren().addAll(
                label("🚨 " + tipo + " — " + barrio, 14, "#111827", true),
                label("Fecha: " + formatFecha(a.getFechaHora()), 11, GRAY_TEXT, false));
        Label estadoBadge = label(
                a.getEstado() != null ? a.getEstado().name().replace("_", " ") : "—", 11, dotColor, true);
        estadoBadge.setPadding(new Insets(3, 8, 3, 8));
        estadoBadge.setStyle("-fx-background-color: " + dotColor + "22; -fx-background-radius: 6;");
        header.getChildren().addAll(dot, headerTxt, estadoBadge);

        Label descLbl = label(
                "Descripción: " + (a.getDescripcion() != null ? a.getDescripcion() : "Sin descripción"),
                12, "#374151", false);
        descLbl.setWrapText(true);

        HBox infoRow = new HBox(16);
        infoRow.getChildren().addAll(
                label("🗺 Dirección: "     + formatDireccion(a.getDireccion()),                                         11, GRAY_TEXT, false),
                label("🔫 Arma: "         + (a.getTipoarma()       != null ? a.getTipoarma().getNombre()       : "N/A"), 11, GRAY_TEXT, false),
                label("🚗 Transporte: "   + (a.getMediotransporte() != null ? a.getMediotransporte().getNombre() : "N/A"), 11, GRAY_TEXT, false)
        );

        card.getChildren().add(separator());

        // Registrar atención
        Label secTitle = label("📝 Registrar atención / describir situación", 13, BLUE, true);

        TextArea situacion = new TextArea();
        situacion.setPromptText("Describe la situación que ocurrió en la escena, acciones tomadas, resultado...");
        situacion.setPrefRowCount(3);
        situacion.setWrapText(true);
        situacion.setStyle("-fx-font-size: 12; -fx-background-radius: 8;");

        Label estadoLbl = label("Cambiar estado de la alerta:", 12, "#374151", false);
        ComboBox<String> estadoCombo = new ComboBox<>();
        estadoCombo.getItems().addAll("PENDIENTE", "RECIBIDA", "EN_ATENCION", "UNIDAD_ASIGNADA", "RESUELTA", "CANCELADA");
        if (a.getEstado() != null) estadoCombo.setValue(a.getEstado().name());
        estadoCombo.setPrefWidth(200);

        Label atencionEstadoLbl = label("Estado de la atención a registrar:", 12, "#374151", false);
        ComboBox<String> atencionCombo = new ComboBox<>();
        atencionCombo.getItems().addAll("PENDIENTE", "EN_PROCESO", "FINALIZADA", "CANCELADA");
        atencionCombo.setValue("EN_PROCESO");
        atencionCombo.setPrefWidth(200);

        HBox combosRow = new HBox(16);
        combosRow.setAlignment(Pos.CENTER_LEFT);
        combosRow.getChildren().addAll(
                new VBox(4, estadoLbl, estadoCombo),
                new VBox(4, atencionEstadoLbl, atencionCombo));

        Button guardar = new Button("💾  Guardar atención");
        guardar.setStyle("-fx-background-color: " + BLUE + "; -fx-text-fill: white; -fx-font-size: 12;"
                + " -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 7 18 7 18;");
        guardar.setOnAction(ev -> {
            String desc        = situacion.getText().trim();
            String nuevoEst    = estadoCombo.getValue();
            String estAtencion = atencionCombo.getValue();

            if (desc.isEmpty()) {
                mostrarAlerta("Campo requerido", "Debes describir la situación antes de guardar.");
                return;
            }
            try {
                if (nuevoEst != null && alertaService != null) {
                    alertaService.actualizarEstado(a.getId_alerta(), nuevoEst);
                }
            } catch (Exception ex) {
                mostrarAlerta("Error", "No se pudo actualizar el estado de la alerta: " + ex.getMessage());
            }
            try {
                if (atencionService != null) {
                    AtencionAlerta at = new AtencionAlerta();
                    at.setAlerta(a);
                    at.setDescripcion(desc);
                    at.setEstado(EstadoAtencionAlerta.valueOf(estAtencion));
                    at.setObservacion("Registrado por: "
                            + (usuarioActual != null ? usuarioActual.getUsername() : "—"));
                    if (policiaActual != null && policiaActual.getUnidadpolicial() != null) {
                        at.setUnidad(policiaActual.getUnidadpolicial());
                    } else {
                        UnidadPolicial u = new UnidadPolicial();
                        u.setNombre("Sin unidad");
                        at.setUnidad(u);
                    }
                    boolean ok = atencionService.insertar(at);
                    if (ok) {
                        mostrarInfo("¡Guardado!", "La atención fue registrada correctamente.");
                        situacion.clear();
                        root.setCenter(new MisAlertasPoliciaView(
                                usuarioActual, policiaActual, alertaService, atencionService, root).build());
                    } else {
                        mostrarAlerta("Error", "No se pudo registrar la atención.");
                    }
                }
            } catch (Exception ex) {
                mostrarAlerta("Error al guardar", ex.getMessage());
            }
        });

        card.getChildren().addAll(header, descLbl, infoRow, separator(), secTitle, situacion, combosRow, guardar);
        return card;
    }

    // ── Datos ────────────────────────────────────────────────────
    private List<Alerta> obtenerMisAlertas() throws Exception {
        if (alertaService == null) return List.of();
        List<Alerta> todas = alertaService.listar();
        if (usuarioActual == null) return todas;
        List<Alerta> mias = todas.stream()
                .filter(a -> a.getUsuario() != null
                        && usuarioActual.getUsername() != null
                        && usuarioActual.getUsername().equals(a.getUsuario().getUsername()))
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

    private String formatDireccion(Direccion d) {
        if (d == null) return "—";
        StringBuilder sb = new StringBuilder();
        if (d.getCalle()   != null && !d.getCalle().isBlank())   sb.append("Calle ").append(d.getCalle()).append(" ");
        if (d.getCarrera() != null && !d.getCarrera().isBlank()) sb.append("# ").append(d.getCarrera());
        if (d.getCasa()    != null && !d.getCasa().isBlank())    sb.append(" Casa ").append(d.getCasa());
        return sb.isEmpty() ? "Sin dirección" : sb.toString().trim();
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