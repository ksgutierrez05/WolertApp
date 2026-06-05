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
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import sistemagestion.model.*;
import sistemagestion.service.AlertaService;

/**
 * Vista: listado completo de alertas.
 * Al presionar una alerta se muestra un diálogo con su descripción completa.
 */
public class AlertasAdminPoliciaView {

    private static final String BG        = "#f4f6fb";
    private static final String WHITE     = "#ffffff";
    private static final String RED       = "#e53935";
    private static final String RED_LIGHT = "#fff0f0";
    private static final String ORANGE    = "#fb8c00";
    private static final String GREEN     = "#43a047";
    private static final String BLUE      = "#1565c0";
    private static final String BLUE_LIGHT = "#e8f0fe";
    private static final String GRAY_TEXT = "#6b7280";
    private static final String BORDER    = "#e5e7eb";
    private static final String FA        = "'Font Awesome 6 Free Solid'";

    private final AlertaService alertaService;

    public AlertasAdminPoliciaView(AlertaService alertaService) {
        this.alertaService = alertaService;
    }

    public ScrollPane build() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color:" + BG + ";");

        // Top bar
        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER_LEFT);
        VBox titles = new VBox(4);
        Label title = new Label("Gestión de Alertas");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#111827"));
        Label sub = label("Listado completo · Haz clic en una alerta para ver su descripción", 13, GRAY_TEXT, false);
        titles.getChildren().addAll(title, sub);
        topBar.getChildren().add(titles);

        content.getChildren().add(topBar);
        content.getChildren().add(buildStatsRow());
        content.getChildren().add(buildListaCard());

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background:" + BG + ";-fx-background-color:" + BG + ";");
        return scroll;
    }

    // ── Stats rápidas ─────────────────────────────────────────────
    private HBox buildStatsRow() {
        HBox row = new HBox(16);
        HBox.setHgrow(row, Priority.ALWAYS);
        try {
            List<Alerta> lista = alertaService.listar();
            long total     = lista.size();
            long pendiente = lista.stream().filter(a -> a.getEstado() == EstadoAlerta.PENDIENTE).count();
            long enAtencion= lista.stream().filter(a -> a.getEstado() == EstadoAlerta.EN_ATENCION
                                                      || a.getEstado() == EstadoAlerta.UNIDAD_ASIGNADA).count();
            long resuelta  = lista.stream().filter(a -> a.getEstado() == EstadoAlerta.RESUELTA).count();

            row.getChildren().addAll(
                    miniStat(RED_LIGHT, RED, "\uf0f3", "Total", total),
                    miniStat("#fff8e1", ORANGE, "\uf017", "Pendientes", pendiente),
                    miniStat(BLUE_LIGHT, BLUE, "\uf14a", "En atención", enAtencion),
                    miniStat("#e8f5e9", GREEN, "\uf058", "Resueltas", resuelta));
        } catch (Exception e) {
            row.getChildren().add(label("No se pudieron cargar las estadísticas", 12, GRAY_TEXT, false));
        }
        return row;
    }

    private VBox miniStat(String bgColor, String accent, String icon, String titulo, long val) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(16, 20, 16, 20));
        card.setStyle("-fx-background-color:white;-fx-background-radius:14;");
        HBox.setHgrow(card, Priority.ALWAYS);
        shadow(card);

        StackPane iconWrap = new StackPane();
        iconWrap.setPrefSize(38, 38);
        iconWrap.setMinSize(38, 38);
        Rectangle iconBg = new Rectangle(38, 38);
        iconBg.setArcWidth(10);
        iconBg.setArcHeight(10);
        iconBg.setFill(Color.web(bgColor));
        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-family:" + FA + ";-fx-font-size:16px;-fx-text-fill:" + accent + ";");
        iconWrap.getChildren().addAll(iconBg, iconLbl);

        Label valLbl   = new Label(String.valueOf(val));
        valLbl.setStyle("-fx-font-size:28px;-fx-font-weight:bold;-fx-text-fill:" + accent + ";");
        Label titLbl   = label(titulo, 12, GRAY_TEXT, false);

        HBox top = new HBox(12, iconWrap, new VBox(2, label(titulo, 11, "#374151", true), valLbl, titLbl));
        top.setAlignment(Pos.CENTER_LEFT);
        card.getChildren().add(top);
        card.setOnMouseEntered(e -> card.setTranslateY(-2));
        card.setOnMouseExited(e  -> card.setTranslateY(0));
        return card;
    }

    // ── Tarjeta lista ──────────────────────────────────────────────
    private VBox buildListaCard() {
        VBox card = new VBox(0);
        card.setStyle("-fx-background-color:white;-fx-background-radius:14;");
        shadow(card);

        // Cabecera
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(14, 20, 12, 20));
        header.setStyle("-fx-border-color:transparent transparent " + BORDER + " transparent;-fx-border-width:0 0 1 0;");
        Rectangle accent = new Rectangle(4, 20);
        accent.setArcWidth(4);
        accent.setArcHeight(4);
        accent.setFill(Color.web(RED));
        Label faIco = new Label("\uf0f3");
        faIco.setStyle("-fx-font-family:" + FA + ";-fx-font-size:14px;-fx-text-fill:" + RED + ";");
        Label titleCard = label("Todas las alertas", 14, "#111827", true);
        header.getChildren().addAll(accent, faIco, titleCard);
        card.getChildren().add(header);

        // Columnas
        HBox thead = new HBox(0);
        thead.setPadding(new Insets(10, 16, 10, 16));
        thead.setAlignment(Pos.CENTER_LEFT);
        thead.setStyle("-fx-background-color:#f8fafc;"
                + "-fx-border-color:transparent transparent " + BORDER + " transparent;-fx-border-width:0 0 1 0;");
        String[] cols = {"Tipo / Barrio", "Estado", "Fecha", "Descripción"};
        double[] ws   = {-1, 140, 160, 180};
        for (int i = 0; i < cols.length; i++) {
            Label h = new Label(cols[i].toUpperCase());
            h.setStyle("-fx-font-size:10px;-fx-font-weight:bold;-fx-text-fill:#9ca3af;");
            if (ws[i] < 0) {
                HBox.setHgrow(h, Priority.ALWAYS);
            } else {
                h.setPrefWidth(ws[i]);
                h.setMinWidth(ws[i]);
            }
            thead.getChildren().add(h);
        }
        card.getChildren().add(thead);

        // Filas
        VBox tbody = new VBox(0);
        try {
            List<Alerta> lista = alertaService.listar();
            if (lista.isEmpty()) {
                VBox empty = new VBox(8);
                empty.setAlignment(Pos.CENTER);
                empty.setPadding(new Insets(40));
                Label emptyIco = new Label("\uf0f3");
                emptyIco.setStyle("-fx-font-family:" + FA + ";-fx-font-size:40px;-fx-text-fill:#d1d5db;");
                empty.getChildren().addAll(emptyIco, label("No hay alertas registradas", 14, GRAY_TEXT, false));
                tbody.getChildren().add(empty);
            } else {
                List<Alerta> ordenadas = lista.stream()
                        .filter(a -> a.getFechaHora() != null)
                        .sorted((a, b) -> b.getFechaHora().compareTo(a.getFechaHora()))
                        .toList();
                for (int i = 0; i < ordenadas.size(); i++) {
                    tbody.getChildren().add(buildFila(ordenadas.get(i), i % 2 == 0, ws));
                }
            }
        } catch (Exception e) {
            VBox err = new VBox(8);
            err.setAlignment(Pos.CENTER_LEFT);
            err.setPadding(new Insets(16, 20, 16, 20));
            err.setStyle("-fx-background-color:" + RED_LIGHT + ";-fx-background-radius:8;");
            err.getChildren().add(label("Error al cargar alertas: " + e.getMessage(), 12, RED, false));
            tbody.getChildren().add(err);
        }
        card.getChildren().add(tbody);
        return card;
    }

    // ── Fila individual ────────────────────────────────────────────
    private HBox buildFila(Alerta a, boolean par, double[] ws) {
        String dotColor = estadoColor(a.getEstado());
        String tipo   = a.getTipoalerta() != null ? a.getTipoalerta().getNombre() : "—";
        String barrio = a.getBarrio()     != null ? a.getBarrio().getNombre()     : "—";
        String estado = a.getEstado()     != null ? a.getEstado().name().replace("_", " ") : "—";
        String fecha  = formatFecha(a.getFechaHora());
        String desc   = a.getDescripcion() != null && !a.getDescripcion().isBlank()
                ? (a.getDescripcion().length() > 35
                        ? a.getDescripcion().substring(0, 33) + "…"
                        : a.getDescripcion())
                : "Sin descripción";

        HBox fila = new HBox(0);
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.setPadding(new Insets(10, 16, 10, 16));
        fila.setCursor(javafx.scene.Cursor.HAND);

        String bgN = "-fx-background-color:" + (par ? WHITE : "#fafbfd") + ";"
                + "-fx-border-color:transparent transparent " + BORDER + " transparent;"
                + "-fx-border-width:0 0 1 0;";
        fila.setStyle(bgN);
        fila.setOnMouseEntered(e -> fila.setStyle(
                "-fx-background-color:#EEF2FF;"
                + "-fx-border-color:transparent transparent " + BORDER + " transparent;"
                + "-fx-border-width:0 0 1 0;-fx-cursor:hand;"));
        fila.setOnMouseExited(e -> fila.setStyle(bgN));

        // Al hacer clic → diálogo con descripción completa
        fila.setOnMouseClicked(e -> mostrarDetalle(a));

        // Col 1: tipo + barrio con dot
        HBox col1 = new HBox(8);
        col1.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(col1, Priority.ALWAYS);
        Circle dot = new Circle(5, Color.web(dotColor));
        StackPane iconBox = new StackPane();
        iconBox.setMinSize(30, 30);
        iconBox.setMaxSize(30, 30);
        Rectangle iconBg = new Rectangle(30, 30);
        iconBg.setArcWidth(8);
        iconBg.setArcHeight(8);
        iconBg.setFill(Color.web(RED_LIGHT));
        Label faLbl = new Label("\uf0f3");
        faLbl.setStyle("-fx-font-family:" + FA + ";-fx-font-size:12px;-fx-text-fill:" + RED + ";");
        iconBox.getChildren().addAll(iconBg, faLbl);
        VBox tipoBox = new VBox(1);
        Label tipoLbl   = label(tipo, 12, "#111827", true);
        Label barrioLbl = label(barrio, 11, GRAY_TEXT, false);
        tipoBox.getChildren().addAll(tipoLbl, barrioLbl);
        col1.getChildren().addAll(dot, iconBox, tipoBox);

        // Col 2: estado badge
        String bgEst  = estadoBg(a.getEstado());
        Label estLbl  = label(estado, 10, dotColor, true);
        estLbl.setPadding(new Insets(3, 10, 3, 10));
        estLbl.setStyle("-fx-background-color:" + bgEst + ";-fx-background-radius:20;"
                + "-fx-text-fill:" + dotColor + ";-fx-font-weight:bold;-fx-font-size:10px;");
        HBox estBox = new HBox(estLbl);
        estBox.setAlignment(Pos.CENTER_LEFT);
        estBox.setPrefWidth(ws[1]);
        estBox.setMinWidth(ws[1]);
        estBox.setMaxWidth(ws[1]);

        // Col 3: fecha
        Label fechaLbl = celdaFija(fecha, ws[2]);

        // Col 4: descripción truncada
        Label descLbl = celdaFija(desc, ws[3]);

        fila.getChildren().addAll(col1, estBox, fechaLbl, descLbl);
        return fila;
    }

    // ── Diálogo con descripción completa ──────────────────────────
    private void mostrarDetalle(Alerta a) {
        Stage dlg = new Stage();
        dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.setTitle("Detalle de alerta");
        dlg.setResizable(false);

        VBox root = new VBox(0);
        root.setPrefWidth(460);
        root.setStyle("-fx-background-color:" + BG + ";");

        // Header según estado
        String headerColor = estadoColor(a.getEstado());
        HBox header = new HBox(12);
        header.setPadding(new Insets(20, 24, 18, 24));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color:" + headerColor + ";");

        Label titleLbl = new Label("Detalle de alerta");
        titleLbl.setFont(Font.font("System", FontWeight.BOLD, 18));
        titleLbl.setTextFill(Color.WHITE);
        String estadoStr = a.getEstado() != null ? a.getEstado().name().replace("_", " ") : "—";
        Label subLbl = label(estadoStr, 12, "rgba(255,255,255,0.80)", false);
        header.getChildren().add(new VBox(4, titleLbl, subLbl));

        // Cuerpo
        VBox body = new VBox(0);
        body.setStyle("-fx-background-color:white;");
        body.setPadding(new Insets(8, 0, 8, 0));

        String tipo   = a.getTipoalerta() != null ? a.getTipoalerta().getNombre() : "—";
        String barrio = a.getBarrio()     != null ? a.getBarrio().getNombre()     : "—";
        String fecha  = a.getFechaHora()  != null
                ? a.getFechaHora().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "—";
        String desc   = a.getDescripcion() != null && !a.getDescripcion().isBlank()
                ? a.getDescripcion() : "Sin descripción registrada";
        String coords = (a.getLatitud() != 0 || a.getLongitud() != 0)
                ? String.format("%.5f, %.5f", a.getLatitud(), a.getLongitud()) : "No disponible";

        body.getChildren().addAll(
                filaDetalle("\uf0f3", "Tipo de alerta", tipo,   RED),
                sep(),
                filaDetalle("\uf3c5", "Barrio",          barrio, BLUE),
                sep(),
                filaDetalle("\uf073", "Fecha / Hora",    fecha,  GREEN),
                sep(),
                filaDetalle("\uf041", "Coordenadas",     coords, ORANGE),
                sep());

        // Descripción completa en bloque aparte
        VBox descBlock = new VBox(6);
        descBlock.setPadding(new Insets(14, 24, 14, 24));
        Label descTit = label("Descripción completa", 11, GRAY_TEXT, false);
        Label descVal = new Label(desc);
        descVal.setWrapText(true);
        descVal.setFont(Font.font("System", 13));
        descVal.setTextFill(Color.web("#111827"));
        descVal.setStyle("-fx-background-color:#f8f9fd;-fx-background-radius:8;-fx-padding:10 12 10 12;");
        descVal.setMaxWidth(Double.MAX_VALUE);
        descBlock.getChildren().addAll(descTit, descVal);
        body.getChildren().add(descBlock);

        // Footer
        HBox footer = new HBox();
        footer.setPadding(new Insets(14, 24, 18, 24));
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setStyle("-fx-background-color:white;-fx-border-color:#e5e7eb "
                + "transparent transparent;-fx-border-width:1 0 0 0;");
        Button btnCerrar = new Button("Cerrar");
        btnCerrar.setPrefHeight(38);
        String btnBase  = "-fx-background-color:" + headerColor + ";-fx-text-fill:white;"
                + "-fx-font-size:13px;-fx-font-weight:bold;-fx-background-radius:8;-fx-padding:8 20;-fx-cursor:hand;";
        btnCerrar.setStyle(btnBase);
        btnCerrar.setOnAction(e -> dlg.close());
        footer.getChildren().add(btnCerrar);

        root.getChildren().addAll(header, body, footer);
        dlg.setScene(new Scene(root));
        dlg.showAndWait();
    }

    private HBox filaDetalle(String iconFA, String titulo, String valor, String color) {
        HBox fila = new HBox(14);
        fila.setPadding(new Insets(12, 24, 12, 24));
        fila.setAlignment(Pos.CENTER_LEFT);

        StackPane iconWrap = new StackPane();
        iconWrap.setPrefSize(34, 34);
        iconWrap.setMinSize(34, 34);
        Rectangle bg = new Rectangle(34, 34);
        bg.setArcWidth(9);
        bg.setArcHeight(9);
        bg.setFill(Color.web(color + "22"));
        Label icn = new Label(iconFA);
        icn.setStyle("-fx-font-family:" + FA + ";-fx-font-size:13px;-fx-text-fill:" + color + ";");
        iconWrap.getChildren().addAll(bg, icn);

        VBox texto = new VBox(2);
        HBox.setHgrow(texto, Priority.ALWAYS);
        texto.getChildren().addAll(
                label(titulo, 10, GRAY_TEXT, false),
                label(valor,  13, "#111827",  true));

        fila.getChildren().addAll(iconWrap, texto);
        return fila;
    }

    private Rectangle sep() {
        Rectangle r = new Rectangle();
        r.setHeight(1);
        r.setFill(Color.web(BORDER));
        r.widthProperty().bind(javafx.beans.binding.Bindings.createDoubleBinding(() -> 460.0));
        return r;
    }

    // ── Helpers ───────────────────────────────────────────────────
    private String estadoColor(EstadoAlerta estado) {
        if (estado == null) return GRAY_TEXT;
        return switch (estado) {
            case PENDIENTE, EN_ATENCION -> RED;
            case UNIDAD_ASIGNADA        -> ORANGE;
            case RESUELTA               -> GREEN;
            default                     -> GRAY_TEXT;
        };
    }

    private String estadoBg(EstadoAlerta estado) {
        if (estado == null) return "#f3f4f6";
        return switch (estado) {
            case PENDIENTE, EN_ATENCION -> RED_LIGHT;
            case UNIDAD_ASIGNADA        -> "#fff8e1";
            case RESUELTA               -> "#e8f5e9";
            default                     -> "#f3f4f6";
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

    private Label celdaFija(String txt, double width) {
        Label l = label(txt != null ? txt : "—", 12, "#374151", false);
        l.setPrefWidth(width);
        l.setMinWidth(width);
        l.setMaxWidth(width);
        return l;
    }

    private Label label(String text, double size, String color, boolean bold) {
        Label lbl = new Label(text);
        lbl.setFont(bold ? Font.font("System", FontWeight.BOLD, size) : Font.font("System", size));
        lbl.setTextFill(Color.web(color));
        return lbl;
    }

    private void shadow(Region node) {
        node.setEffect(new DropShadow(12, 0, 2, Color.web("#0000001a")));
    }
}