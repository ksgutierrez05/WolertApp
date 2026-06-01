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
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import sistemagestion.service.*;

/**
 * Vista: resumen general de actividad (reportes). Uso: root.setCenter(new
 * ReportesAdminPoliciaView(services...).build());
 */
public class ReportesAdminPoliciaView {

    // ── Paleta ───────────────────────────────────────────────────
    private static final String BG_PAGE = "#f0f2f8";
    private static final String BG_CARD = "#ffffff";
    private static final String BORDER = "#e8eaf0";

    private static final String TEXT_PRIMARY = "#1a1f36";
    private static final String TEXT_SECONDARY = "#5a6070";

    // Colores por categoría
    private static final String C_RED = "#c62828";
    private static final String C_RED_BG = "#ffebee";
    private static final String C_ORG = "#e65100";
    private static final String C_ORG_BG = "#fff3e0";
    private static final String C_GRN = "#2e7d32";
    private static final String C_GRN_BG = "#e8f5e9";
    private static final String C_BLU = "#1565c0";
    private static final String C_BLU_BG = "#e3f2fd";
    private static final String C_PUR = "#6a1b9a";
    private static final String C_PUR_BG = "#f3e5f5";
    private static final String C_TEA = "#00695c";
    private static final String C_TEA_BG = "#e0f2f1";
    private static final String C_AMB = "#f57f17";
    private static final String C_AMB_BG = "#fffde7";

    // ── Servicios ────────────────────────────────────────────────
    private final AlertaService alertaService;
    private final AsignacionUnidadService asignacionService;
    private final UnidadPolicialService unidadService;
    private final PoliciaService policiaService;
    private final AlarmaService alarmaService;
    private final NotificacionService notificacionService;
    private final AtencionAlertaService atencionService;

    public ReportesAdminPoliciaView(
            AlertaService alertaService,
            AsignacionUnidadService asignacionService,
            UnidadPolicialService unidadService,
            PoliciaService policiaService,
            AlarmaService alarmaService,
            NotificacionService notificacionService,
            AtencionAlertaService atencionService) {
        this.alertaService = alertaService;
        this.asignacionService = asignacionService;
        this.unidadService = unidadService;
        this.policiaService = policiaService;
        this.alarmaService = alarmaService;
        this.notificacionService = notificacionService;
        this.atencionService = atencionService;
    }

    // ── Build principal ──────────────────────────────────────────
    public ScrollPane build() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(28, 28, 36, 28));
        content.setStyle("-fx-background-color: " + BG_PAGE + ";");

        content.getChildren().add(buildHeader());

        try {
            long totalAlertas = alertaService != null ? alertaService.listar().size() : 0;
            long totalAsig = asignacionService != null ? asignacionService.listar().size() : 0;
            long totalUnidades = unidadService != null ? unidadService.listar().size() : 0;
            long totalPolicias = policiaService != null ? policiaService.listar().size() : 0;
            long totalAlarmas = alarmaService != null ? alarmaService.listar().size() : 0;
            long totalNoti = notificacionService != null ? notificacionService.listar().size() : 0;
            long totalAtenc = atencionService != null ? atencionService.listar().size() : 0;

            // ── Fila de métricas destacadas (top 3) ──────────────
            Label secLabel1 = sectionLabel("Métricas principales");
            content.getChildren().add(secLabel1);

            HBox topRow = new HBox(14);
            topRow.getChildren().addAll(
                    buildMetricCard("🚨", "Alertas", totalAlertas, C_RED, C_RED_BG),
                    buildMetricCard("👮", "Policías", totalPolicias, C_BLU, C_BLU_BG),
                    buildMetricCard("🚓", "Unidades", totalUnidades, C_GRN, C_GRN_BG)
            );
            content.getChildren().add(topRow);

            // ── Lista detallada ───────────────────────────────────
            Label secLabel2 = sectionLabel("Detalle completo");
            content.getChildren().add(secLabel2);

            VBox detailCard = buildDetailCard(new String[][]{
                {"📌", "Asignaciones activas", String.valueOf(totalAsig), C_ORG, C_ORG_BG},
                {"🔔", "Alarmas", String.valueOf(totalAlarmas), C_AMB, C_AMB_BG},
                {"📢", "Notificaciones", String.valueOf(totalNoti), C_PUR, C_PUR_BG},
                {"📋", "Atenciones", String.valueOf(totalAtenc), C_TEA, C_TEA_BG},});
            content.getChildren().add(detailCard);

        } catch (Exception e) {
            content.getChildren().add(buildErrorState(e.getMessage()));
        }

        return wrapScroll(content);
    }

    // ── Header ───────────────────────────────────────────────────
    private HBox buildHeader() {
        HBox header = new HBox(14);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 24, 20, 24));
        header.setStyle(
                "-fx-background-color: linear-gradient(to right, #e8f3fa, #e6f4fc);"
                + "-fx-background-radius: 14;"
        );

        StackPane icoWrap = new StackPane();
        icoWrap.setPrefSize(54, 54);
        icoWrap.setMinSize(54, 54);
        icoWrap.setMaxSize(54, 54);
        Rectangle icoBg = new Rectangle(54, 54);
        icoBg.setFill(Color.web("#ffffff80"));

        icoBg.setArcWidth(20);
        icoBg.setArcHeight(20);

        Label icoLbl = new Label("\uf201");
        icoLbl.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';"
                + "-fx-font-size:24px;-fx-text-fill:#1565c0");
        icoWrap.getChildren().addAll(icoBg, icoLbl);

        VBox textBox = new VBox(3);
        Label title = new Label("Reportes del Sistema");
        title.setFont(Font.font("System", FontWeight.BOLD, 20));
        title.setTextFill(Color.WHITE);

        Label subtitle = new Label("Resumen general de actividad operativa");
        subtitle.setFont(Font.font("System", 12));
        subtitle.setTextFill(Color.web("#90caf9"));

        textBox.getChildren().addAll(title, subtitle);

        DropShadow shadow = new DropShadow(12, 0, 4, Color.web("#1a237e", 0.30));
        header.setEffect(shadow);

        header.getChildren().addAll(icoWrap, textBox);
        return header;
    }

    // ── Tarjeta de métrica grande ─────────────────────────────────
    private VBox buildMetricCard(String icon, String label, long value, String color, String bgColor) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20, 16, 20, 16));
        card.setStyle(
                "-fx-background-color: " + BG_CARD + ";"
                + "-fx-background-radius: 14;"
        );
        HBox.setHgrow(card, Priority.ALWAYS);

        DropShadow shadow = new DropShadow(10, 0, 3, Color.web("#000000", 0.07));
        card.setEffect(shadow);

        // Ícono con fondo de color
        StackPane iconBox = new StackPane();
        Rectangle iconBg = new Rectangle(46, 46);
        iconBg.setArcWidth(12);
        iconBg.setArcHeight(12);
        iconBg.setFill(Color.web(bgColor));
        Label iconLbl = new Label(icon);
        iconLbl.setFont(Font.font("System", 22));
        iconBox.getChildren().addAll(iconBg, iconLbl);

        // Número grande
        Label valueLbl = new Label(String.valueOf(value));
        valueLbl.setFont(Font.font("System", FontWeight.BOLD, 30));
        valueLbl.setTextFill(Color.web(color));

        // Etiqueta
        Label labelLbl = new Label(label);
        labelLbl.setFont(Font.font("System", 12));
        labelLbl.setTextFill(Color.web(TEXT_SECONDARY));

        // Barra de color inferior
        Rectangle bar = new Rectangle();
        bar.setWidth(60);
        bar.setHeight(3);
        bar.setArcWidth(3);
        bar.setArcHeight(3);
        bar.setFill(Color.web(color));

        card.getChildren().addAll(iconBox, valueLbl, labelLbl, bar);
        return card;
    }

    // ── Tarjeta de detalle con filas ─────────────────────────────
    private VBox buildDetailCard(String[][] items) {
        VBox card = new VBox(0);
        card.setStyle(
                "-fx-background-color: " + BG_CARD + ";"
                + "-fx-background-radius: 14;"
        );
        DropShadow shadow = new DropShadow(12, 0, 3, Color.web("#000000", 0.07));
        card.setEffect(shadow);

        // Cabecera
        HBox cardHeader = new HBox(8);
        cardHeader.setAlignment(Pos.CENTER_LEFT);
        cardHeader.setPadding(new Insets(14, 20, 12, 20));
        cardHeader.setStyle("-fx-border-color: " + BORDER + "; -fx-border-width: 0 0 1 0;");

        Rectangle accent = new Rectangle(4, 18);
        accent.setArcWidth(4);
        accent.setArcHeight(4);
        accent.setFill(Color.web(C_BLU));

        Label cardTitle = new Label("Otras estadísticas");
        cardTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        cardTitle.setTextFill(Color.web(TEXT_PRIMARY));

        cardHeader.getChildren().addAll(accent, cardTitle);
        card.getChildren().add(cardHeader);

        for (int i = 0; i < items.length; i++) {
            card.getChildren().add(buildDetailRow(
                    items[i][0], items[i][1], items[i][2], items[i][3], items[i][4]
            ));
            if (i < items.length - 1) {
                card.getChildren().add(separator());
            }
        }
        return card;
    }

    // ── Fila de detalle ──────────────────────────────────────────
    private HBox buildDetailRow(String icon, String labelText, String value,
            String color, String bgColor) {
        HBox row = new HBox(14);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(13, 20, 13, 20));
        row.setStyle("-fx-background-color: transparent;");

        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #f8f9fd;"));
        row.setOnMouseExited(e -> row.setStyle("-fx-background-color: transparent;"));

        // Ícono
        StackPane iconBox = new StackPane();
        Rectangle iconBg = new Rectangle(36, 36);
        iconBg.setArcWidth(9);
        iconBg.setArcHeight(9);
        iconBg.setFill(Color.web(bgColor));
        Label iconLbl = new Label(icon);
        iconLbl.setFont(Font.font("System", 16));
        iconBox.getChildren().addAll(iconBg, iconLbl);

        // Etiqueta
        Label lbl = new Label(labelText);
        lbl.setFont(Font.font("System", 13));
        lbl.setTextFill(Color.web(TEXT_PRIMARY));
        HBox.setHgrow(lbl, Priority.ALWAYS);

        // Valor con badge
        Label valLbl = new Label(value);
        valLbl.setFont(Font.font("System", FontWeight.BOLD, 15));
        valLbl.setTextFill(Color.web(color));
        valLbl.setPadding(new Insets(4, 14, 4, 14));
        valLbl.setStyle(
                "-fx-background-color: " + bgColor + ";"
                + "-fx-background-radius: 20;"
        );

        row.getChildren().addAll(iconBox, lbl, valLbl);
        return row;
    }

    // ── Estado de error ──────────────────────────────────────────
    private HBox buildErrorState(String message) {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(14, 20, 14, 20));
        box.setStyle(
                "-fx-background-color: " + C_RED_BG + ";"
                + "-fx-background-radius: 10;"
        );
        Label icon = new Label("⚠");
        icon.setFont(Font.font("System", FontWeight.BOLD, 16));
        icon.setTextFill(Color.web(C_RED));
        Label msg = new Label("Error al cargar datos: " + message);
        msg.setFont(Font.font("System", 12));
        msg.setTextFill(Color.web(C_RED));
        msg.setWrapText(true);
        box.getChildren().addAll(icon, msg);
        return box;
    }

    // ── Etiqueta de sección ──────────────────────────────────────
    private Label sectionLabel(String text) {
        Label lbl = new Label(text.toUpperCase());
        lbl.setFont(Font.font("System", FontWeight.BOLD, 10));
        lbl.setTextFill(Color.web(TEXT_SECONDARY));
        lbl.setPadding(new Insets(4, 0, 0, 2));
        return lbl;
    }

    // ── Helpers básicos ──────────────────────────────────────────
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
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setStyle(
                "-fx-background: " + BG_PAGE + ";"
                + "-fx-background-color: " + BG_PAGE + ";"
        );
        return scroll;
    }
}
