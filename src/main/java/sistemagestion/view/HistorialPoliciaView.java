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
import java.util.stream.Collectors;
import sistemagestion.model.*;
import sistemagestion.service.*;

public class HistorialPoliciaView {

    private static final String WHITE     = "#ffffff";
    private static final String BG        = "#f4f6fb";
    private static final String RED       = "#e53935";
    private static final String GREEN     = "#43a047";
    private static final String BLUE      = "#1565c0";
    private static final String GRAY_TEXT = "#6b7280";
    private static final String BORDER    = "#e5e7eb";

    private final Usuario usuarioActual;
    private final Policia policiaActual;
    private final AtencionAlertaService atencionService;
    private final BorderPane root;

    public HistorialPoliciaView(Usuario usuarioActual, Policia policiaActual,
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

        Label title = new Label("📜 Historial de atenciones");
        title.setFont(Font.font("System", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#111827"));
        content.getChildren().add(title);

        VBox panel = createPanel("Atenciones finalizadas y canceladas");
        try {
            List<AtencionAlerta> lista = obtenerMisAtenciones().stream()
                    .filter(at -> at.getEstado() == EstadoAtencionAlerta.FINALIZADA
                               || at.getEstado() == EstadoAtencionAlerta.CANCELADA)
                    .collect(Collectors.toList());
            if (lista.isEmpty()) {
                panel.getChildren().add(
                        label("No hay atenciones finalizadas o canceladas.", 13, GRAY_TEXT, false));
            } else {
                for (AtencionAlerta at : lista) {
                    String estadoStr = at.getEstado().name().replace("_", " ");
                    String color     = at.getEstado() == EstadoAtencionAlerta.FINALIZADA ? GREEN : GRAY_TEXT;
                    String desc      = at.getDescripcion() != null ? at.getDescripcion() : "—";
                    String fecha     = formatFechaAt(at.getFechaatencion());
                    panel.getChildren().addAll(
                            listItem("📋",
                                    "#" + at.getId_atencion() + " — " + estadoStr + " · " + fecha,
                                    desc, color),
                            separator());
                }
            }
        } catch (Exception e) {
            panel.getChildren().add(label("Error: " + e.getMessage(), 12, RED, false));
        }
        content.getChildren().add(panel);
        return wrapScroll(content);
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
                .collect(Collectors.toList());
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

    private String formatFechaAt(LocalDateTime dt) {
        if (dt == null) return "—";
        return dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
}