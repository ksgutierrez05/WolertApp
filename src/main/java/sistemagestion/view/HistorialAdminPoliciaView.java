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
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;

import sistemagestion.model.*;
import sistemagestion.service.AtencionAlertaService;

/**
 * Vista: historial de atenciones de alertas.
 * Uso: root.setCenter(new HistorialAdminPoliciaView(atencionService).build());
 */
public class HistorialAdminPoliciaView {

    // ── Paleta de colores centralizada ───────────────────────────
    private static final String BG_PAGE     = "#f0f2f8";
    private static final String BG_CARD     = "#ffffff";
    private static final String BG_HEADER   = "#1a237e";   // azul oscuro institucional

    private static final String ACCENT_BLUE = "#1565c0";
    private static final String ACCENT_TEAL = "#00838f";

    private static final String STATE_RED    = "#c62828";
    private static final String STATE_RED_BG = "#ffebee";
    private static final String STATE_ORG    = "#e65100";
    private static final String STATE_ORG_BG = "#fff3e0";
    private static final String STATE_GRN    = "#2e7d32";
    private static final String STATE_GRN_BG = "#e8f5e9";
    private static final String STATE_GRY    = "#546e7a";
    private static final String STATE_GRY_BG = "#eceff1";

    private static final String TEXT_PRIMARY   = "#1a1f36";
    private static final String TEXT_SECONDARY = "#5a6070";
    private static final String BORDER_COLOR   = "#e8eaf0";

    private final AtencionAlertaService atencionService;

    public HistorialAdminPoliciaView(AtencionAlertaService atencionService) {
        this.atencionService = atencionService;
    }

    // ── Build principal ──────────────────────────────────────────
    public ScrollPane build() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(28, 28, 36, 28));
        content.setStyle("-fx-background-color: " + BG_PAGE + ";");

        content.getChildren().addAll(buildHeader(), buildListCard());

        return wrapScroll(content);
    }

    // ── Header con degradado ─────────────────────────────────────
    private HBox buildHeader() {
        HBox header = new HBox(14);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 24, 20, 24));
        header.setStyle(
            "-fx-background-color: linear-gradient(to right, #e8f3fa, #e6f4fc);" +
            "-fx-background-radius: 14;"
        );

        // Ícono en círculo
        StackPane iconCircle = new StackPane();
        Circle circle = new Circle(24);
        circle.setFill(Color.web("#ffffff", 0.15));
        Label iconLbl = new Label("📜");
        iconLbl.setFont(Font.font("System", 20));
        iconCircle.getChildren().addAll(circle, iconLbl);

        // Textos
        VBox textBox = new VBox(3);
        Label title = new Label("Historial de Atenciones");
        title.setFont(Font.font("System", FontWeight.BOLD, 20));
        title.setTextFill(Color.WHITE);

        Label subtitle = new Label("Registro completo de alertas atendidas");
        subtitle.setFont(Font.font("System", 12));
        subtitle.setTextFill(Color.web("#90caf9"));

        textBox.getChildren().addAll(title, subtitle);

        // Sombra al header
        DropShadow shadow = new DropShadow(12, 0, 4, Color.web("#1a237e", 0.30));
        header.setEffect(shadow);

        header.getChildren().addAll(iconCircle, textBox);
        return header;
    }

    // ── Tarjeta principal con la lista ───────────────────────────
    private VBox buildListCard() {
        VBox card = new VBox(0);
        card.setStyle(
            "-fx-background-color: " + BG_CARD + ";" +
            "-fx-background-radius: 14;"
        );
        DropShadow cardShadow = new DropShadow(14, 0, 3, Color.web("#000000", 0.08));
        card.setEffect(cardShadow);

        // Sub-cabecera de la tarjeta
        HBox cardHeader = new HBox(8);
        cardHeader.setAlignment(Pos.CENTER_LEFT);
        cardHeader.setPadding(new Insets(16, 20, 14, 20));
        cardHeader.setStyle(
            "-fx-border-color: " + BORDER_COLOR + ";" +
            "-fx-border-width: 0 0 1 0;"
        );

        Rectangle accent = new Rectangle(4, 18);
        accent.setArcWidth(4); accent.setArcHeight(4);
        accent.setFill(Color.web(ACCENT_BLUE));

        Label cardTitle = new Label("Atenciones registradas");
        cardTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        cardTitle.setTextFill(Color.web(TEXT_PRIMARY));

        cardHeader.getChildren().addAll(accent, cardTitle);
        card.getChildren().add(cardHeader);

        // Contenido dinámico
        VBox listBox = new VBox(0);
        listBox.setPadding(new Insets(8, 0, 8, 0));

        try {
            List<AtencionAlerta> lista = atencionService.listar();
            if (lista.isEmpty()) {
                listBox.getChildren().add(buildEmptyState());
            } else {
                for (int i = 0; i < lista.size(); i++) {
                    AtencionAlerta a = lista.get(i);
                    listBox.getChildren().add(buildRow(a));
                    if (i < lista.size() - 1) {
                        listBox.getChildren().add(separator());
                    }
                }
            }
        } catch (Exception e) {
            listBox.getChildren().add(buildErrorState(e.getMessage()));
        }

        card.getChildren().add(listBox);
        return card;
    }

    // ── Fila individual de atención ──────────────────────────────
    private HBox buildRow(AtencionAlerta a) {
        String estadoStr = a.getEstado() != null
                ? a.getEstado().name().replace("_", " ") : "PENDIENTE";
        String unidad    = a.getUnidad()  != null ? a.getUnidad().getNombre() : "Sin asignar";
        String desc      = a.getDescripcion() != null && !a.getDescripcion().isBlank()
                ? a.getDescripcion() : "Sin descripción";

        EstadoAtencionAlerta estado = a.getEstado() != null
                ? a.getEstado() : EstadoAtencionAlerta.PENDIENTE;

        String[] colors = switch (estado) {
            case FINALIZADA -> new String[]{ STATE_GRN, STATE_GRN_BG, "✔" };
            case EN_PROCESO -> new String[]{ STATE_ORG, STATE_ORG_BG, "⏳" };
            case CANCELADA  -> new String[]{ STATE_GRY, STATE_GRY_BG, "✖" };
            default         -> new String[]{ STATE_RED, STATE_RED_BG, "⚠" };
        };
        String stateColor = colors[0];
        String stateBg    = colors[1];
        String stateIcon  = colors[2];

        HBox row = new HBox(14);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(13, 20, 13, 20));
        row.setStyle("-fx-background-color: transparent;");

        // Hover effect
        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #f8f9fd; -fx-cursor: hand;"));
        row.setOnMouseExited(e  -> row.setStyle("-fx-background-color: transparent;"));

        // Ícono de estado
        StackPane stateCircle = new StackPane();
        stateCircle.setMinSize(38, 38);
        stateCircle.setMaxSize(38, 38);
        Rectangle stateBgRect = new Rectangle(38, 38);
        stateBgRect.setArcWidth(10); stateBgRect.setArcHeight(10);
        stateBgRect.setFill(Color.web(stateBg));
        Label stateIconLbl = new Label(stateIcon);
        stateIconLbl.setFont(Font.font("System", FontWeight.BOLD, 14));
        stateIconLbl.setTextFill(Color.web(stateColor));
        stateCircle.getChildren().addAll(stateBgRect, stateIconLbl);

        // Textos
        VBox textBox = new VBox(4);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        Label unitLbl = new Label("🚔  " + unidad);
        unitLbl.setFont(Font.font("System", FontWeight.BOLD, 13));
        unitLbl.setTextFill(Color.web(TEXT_PRIMARY));

        Label descLbl = new Label(desc);
        descLbl.setFont(Font.font("System", 12));
        descLbl.setTextFill(Color.web(TEXT_SECONDARY));
        descLbl.setWrapText(true);

        textBox.getChildren().addAll(unitLbl, descLbl);

        // Badge de estado
        Label badge = new Label(estadoStr);
        badge.setFont(Font.font("System", FontWeight.BOLD, 10));
        badge.setTextFill(Color.web(stateColor));
        badge.setPadding(new Insets(3, 10, 3, 10));
        badge.setStyle(
            "-fx-background-color: " + stateBg + ";" +
            "-fx-background-radius: 20;"
        );

        row.getChildren().addAll(stateCircle, textBox, badge);
        return row;
    }

    // ── Estado vacío ─────────────────────────────────────────────
    private VBox buildEmptyState() {
        VBox box = new VBox(8);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(40, 0, 40, 0));

        Label icon = new Label("📭");
        icon.setFont(Font.font("System", 36));

        Label msg = new Label("No hay atenciones registradas");
        msg.setFont(Font.font("System", FontWeight.BOLD, 14));
        msg.setTextFill(Color.web(TEXT_SECONDARY));

        box.getChildren().addAll(icon, msg);
        return box;
    }

    // ── Estado de error ──────────────────────────────────────────
    private HBox buildErrorState(String message) {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(14, 20, 14, 20));
        box.setStyle(
            "-fx-background-color: " + STATE_RED_BG + ";" +
            "-fx-background-radius: 8;"
        );

        Label icon = new Label("⚠");
        icon.setFont(Font.font("System", FontWeight.BOLD, 16));
        icon.setTextFill(Color.web(STATE_RED));

        Label msg = new Label("Error al cargar: " + message);
        msg.setFont(Font.font("System", 12));
        msg.setTextFill(Color.web(STATE_RED));
        msg.setWrapText(true);

        box.getChildren().addAll(icon, msg);
        return box;
    }

    // ── Helpers básicos ──────────────────────────────────────────
    private Region separator() {
        Region sep = new Region();
        sep.setPrefHeight(1);
        sep.setStyle("-fx-background-color: " + BORDER_COLOR + ";");
        HBox.setHgrow(sep, Priority.ALWAYS);
        return sep;
    }

    private ScrollPane wrapScroll(VBox content) {
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setStyle(
            "-fx-background: " + BG_PAGE + ";" +
            "-fx-background-color: " + BG_PAGE + ";"
        );
        return scroll;
    }
}