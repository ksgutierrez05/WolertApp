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
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.format.DateTimeFormatter;
import java.util.List;

import sistemagestion.model.*;
import sistemagestion.service.AtencionAlertaService;

public class HistorialAdminPoliciaView {

    // ── Paleta consistente con la app ─────────────────────────────
    private static final String BG = "#f4f6fb";
    private static final String WHITE = "#ffffff";
    private static final String BLUE = "#1565c0";
    private static final String BLUE_LIGHT = "#e8f0fe";
    private static final String GREEN = "#43a047";
    private static final String GREEN_LIGHT = "#e8f5e9";
    private static final String RED = "#ef5350";
    private static final String RED_LIGHT = "#fff0f0";
    private static final String ORANGE = "#fb8c00";
    private static final String ORANGE_LIGHT = "#fff8e1";
    private static final String GRAY = "#6b7280";
    private static final String GRAY_LIGHT = "#f3f4f6";
    private static final String BORDER = "#e5e7eb";
    private static final String TEXT_DARK = "#111827";
    private static final String FA = "'Font Awesome 6 Free Solid'";

    private static final DateTimeFormatter FMT
            = DateTimeFormatter.ofPattern("dd/MM/yyyy  HH:mm");

    private final AtencionAlertaService atencionService;

    public HistorialAdminPoliciaView(AtencionAlertaService atencionService) {
        this.atencionService = atencionService;
    }

    // ── Build principal ──────────────────────────────────────────
    public ScrollPane build() {
        VBox content = new VBox(24);
        content.setPadding(new Insets(28));
        content.setStyle("-fx-background-color:" + BG + ";");

        List<AtencionAlerta> lista = cargarLista();

        content.getChildren().addAll(
                buildTopBar(lista.size()),
                buildStatStrip(lista),
                buildTimeline(lista)
        );

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setStyle("-fx-background:" + BG + "; -fx-background-color:" + BG + ";");
        return scroll;
    }

    // ── Top bar ──────────────────────────────────────────────────
    private HBox buildTopBar(int total) {
        HBox bar = new HBox(0);
        bar.setPadding(new Insets(24, 28, 24, 28));
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle("-fx-background-color: linear-gradient(to right, #e8f3fa, #e6f4fc);"
                + "-fx-background-radius: 14;");
        bar.setEffect(new DropShadow(12, 0, 4, Color.web("#1a237e30")));

        // Ícono principal
        StackPane iconWrap = new StackPane();
        iconWrap.setPrefSize(56, 56);
        iconWrap.setMinSize(56, 56);
        Circle iconCircle = new Circle(28, Color.web("#ffffff80"));
        Label iconLbl = new Label("\uf1da");
        iconLbl.setStyle("-fx-font-family:" + FA + ";-fx-font-size:22px;-fx-text-fill:" + BLUE + ";");
        iconWrap.getChildren().addAll(iconCircle, iconLbl);

        // Textos
        VBox textBox = new VBox(5);
        textBox.setPadding(new Insets(0, 0, 0, 16));
        Label title = new Label("Historial de Atenciones");
        title.setFont(Font.font("System", FontWeight.BOLD, 26));
        title.setTextFill(Color.web("#1a237e"));
        Label subtitle = label("Registro completo de alertas atendidas por las unidades policiales",
                12, GRAY, false);
        subtitle.setWrapText(true);
        textBox.getChildren().addAll(title, subtitle);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        // Contador total
        VBox counterBox = new VBox(2);
        counterBox.setAlignment(Pos.CENTER_RIGHT);
        Label countNum = new Label(String.valueOf(total));
        countNum.setStyle("-fx-font-size:40px;-fx-font-weight:bold;-fx-text-fill:" + BLUE + ";");
        Label countLbl = label("atenciones", 11, GRAY, false);
        countLbl.setAlignment(Pos.CENTER_RIGHT);
        counterBox.getChildren().addAll(countNum, countLbl);

        bar.getChildren().addAll(iconWrap, textBox, counterBox);
        return bar;
    }

    // ── Strip de estadísticas ─────────────────────────────────────
    private HBox buildStatStrip(List<AtencionAlerta> lista) {
        long finalizadas = lista.stream()
                .filter(a -> a.getEstado() == EstadoAtencionAlerta.FINALIZADA).count();
        long enProceso = lista.stream()
                .filter(a -> a.getEstado() == EstadoAtencionAlerta.EN_PROCESO).count();
        long pendientes = lista.stream()
                .filter(a -> a.getEstado() == EstadoAtencionAlerta.PENDIENTE
                || a.getEstado() == null).count();
        long canceladas = lista.stream()
                .filter(a -> a.getEstado() == EstadoAtencionAlerta.CANCELADA).count();

        HBox strip = new HBox(14);
        strip.getChildren().addAll(
                miniStatCard("\uf058", "Finalizadas", finalizadas, GREEN, GREEN_LIGHT),
                miniStatCard("\uf017", "En proceso", enProceso, ORANGE, ORANGE_LIGHT),
                miniStatCard("\uf071", "Pendientes", pendientes, RED, RED_LIGHT),
                miniStatCard("\uf057", "Canceladas", canceladas, GRAY, GRAY_LIGHT)
        );
        return strip;
    }

    private VBox miniStatCard(String faIcon, String label, long value,
            String color, String bgColor) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(16, 20, 16, 20));
        card.setStyle("-fx-background-color:white;-fx-background-radius:14;");
        card.setEffect(new DropShadow(10, 0, 2, Color.web("#0000001a")));
        HBox.setHgrow(card, Priority.ALWAYS);

        // Ícono
        StackPane iconWrap = new StackPane();
        iconWrap.setPrefSize(40, 40);
        iconWrap.setMinSize(40, 40);
        iconWrap.setMaxSize(40, 40);
        Rectangle iconBg = new Rectangle(40, 40);
        iconBg.setArcWidth(12);
        iconBg.setArcHeight(12);
        iconBg.setFill(Color.web(bgColor));
        Label iconLbl = new Label(faIcon);
        iconLbl.setStyle("-fx-font-family:" + FA + ";-fx-font-size:16px;-fx-text-fill:" + color + ";");
        iconWrap.getChildren().addAll(iconBg, iconLbl);

        Label valLbl = new Label(String.valueOf(value));
        valLbl.setStyle("-fx-font-size:30px;-fx-font-weight:bold;-fx-text-fill:" + color + ";");
        Label nameLbl = label(label, 11, GRAY, false);

        card.getChildren().addAll(iconWrap, valLbl, nameLbl);
        card.setOnMouseEntered(e -> card.setTranslateY(-3));
        card.setOnMouseExited(e -> card.setTranslateY(0));
        return card;
    }

    // ── Timeline ─────────────────────────────────────────────────
    private VBox buildTimeline(List<AtencionAlerta> lista) {
        VBox wrapper = new VBox(0);
        wrapper.setStyle("-fx-background-color:white;-fx-background-radius:18;");
        wrapper.setEffect(new DropShadow(14, 0, 3, Color.web("#0000001a")));

        // Cabecera de la card
        HBox cardHeader = new HBox(10);
        cardHeader.setPadding(new Insets(18, 24, 16, 24));
        cardHeader.setAlignment(Pos.CENTER_LEFT);
        cardHeader.setStyle("-fx-border-color:transparent transparent " + BORDER
                + " transparent;-fx-border-width:0 0 1 0;");

        Rectangle accentBar = new Rectangle(4, 20);
        accentBar.setArcWidth(4);
        accentBar.setArcHeight(4);
        accentBar.setFill(Color.web(BLUE));

        Label cardTitle = label("Línea de tiempo", 15, TEXT_DARK, true);
        Label cardSub = label("  — más recientes primero", 12, GRAY, false);

        HBox titleRow = new HBox(0, accentBar, pad(8), cardTitle, cardSub);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        cardHeader.getChildren().add(titleRow);
        wrapper.getChildren().add(cardHeader);

        if (lista.isEmpty()) {
            wrapper.getChildren().add(buildEmptyState());
            return wrapper;
        }

        // Lista con línea vertical tipo timeline
        VBox timelineBody = new VBox(0);
        timelineBody.setPadding(new Insets(16, 24, 20, 24));

        for (int i = 0; i < lista.size(); i++) {
            timelineBody.getChildren().add(
                    buildTimelineRow(lista.get(i), i, lista.size()));
        }

        wrapper.getChildren().add(timelineBody);
        return wrapper;
    }

    // ── Fila de timeline ──────────────────────────────────────────
    private HBox buildTimelineRow(AtencionAlerta a, int idx, int total) {
        EstadoAtencionAlerta estado = a.getEstado() != null
                ? a.getEstado() : EstadoAtencionAlerta.PENDIENTE;

        String[] sc = stateColors(estado);
        String color = sc[0];
        String bgColor = sc[1];
        String faIcon = sc[2];
        String estadoTxt = estado.name().replace("_", " ");

        HBox row = new HBox(0);
        row.setAlignment(Pos.TOP_LEFT);
        row.setPadding(new Insets(0, 0, 0, 0));

        // ── Columna izquierda: línea + nodo ─────────────────────
        VBox lineCol = new VBox(0);
        lineCol.setAlignment(Pos.TOP_CENTER);
        lineCol.setPrefWidth(52);
        lineCol.setMinWidth(52);

        // Nodo del timeline
        StackPane node = new StackPane();
        node.setPrefSize(40, 40);
        node.setMinSize(40, 40);
        node.setMaxSize(40, 40);

        Circle nodeCircle = new Circle(20, Color.web(bgColor));
        nodeCircle.setStroke(Color.web(color));
        nodeCircle.setStrokeWidth(2);

        Label nodeIcon = new Label(faIcon);
        nodeIcon.setStyle("-fx-font-family:" + FA + ";-fx-font-size:14px;-fx-text-fill:" + color + ";");
        node.getChildren().addAll(nodeCircle, nodeIcon);

        // Línea vertical debajo (excepto último)
        Region vertLine = new Region();
        vertLine.setPrefWidth(2);
        vertLine.setMinWidth(2);
        vertLine.setMaxWidth(2);
        VBox.setVgrow(vertLine, Priority.ALWAYS);
        if (idx < total - 1) {
            vertLine.setStyle("-fx-background-color:" + BORDER + ";");
            vertLine.setPrefHeight(40);
        } else {
            vertLine.setStyle("-fx-background-color:transparent;");
            vertLine.setPrefHeight(10);
        }

        lineCol.getChildren().addAll(node, vertLine);

        // ── Columna derecha: contenido ───────────────────────────
        VBox content = new VBox(0);
        content.setPadding(new Insets(0, 0, idx < total - 1 ? 28 : 8, 16));
        HBox.setHgrow(content, Priority.ALWAYS);

        // Card del item
        VBox itemCard = new VBox(10);
        itemCard.setPadding(new Insets(14, 16, 14, 16));
        itemCard.setStyle("-fx-background-color:" + BG + ";"
                + "-fx-background-radius:12;"
                + "-fx-border-color:" + BORDER + ";"
                + "-fx-border-width:1;"
                + "-fx-border-radius:12;");

        // Fila superior: unidad + badge
        HBox topRow = new HBox(10);
        topRow.setAlignment(Pos.CENTER_LEFT);

        // Avatar unidad
        String unidadNombre = a.getUnidad() != null ? a.getUnidad().getNombre() : "Sin asignar";
        StackPane avatar = new StackPane();
        avatar.setPrefSize(32, 32);
        avatar.setMinSize(32, 32);
        avatar.setMaxSize(32, 32);
        Circle avCircle = new Circle(16, Color.web(avatarColor(unidadNombre)));
        Label avLbl = label(iniciales(unidadNombre), 10, WHITE, true);
        avatar.getChildren().addAll(avCircle, avLbl);

        Label unidadLbl = label(unidadNombre, 13, TEXT_DARK, true);
        HBox.setHgrow(unidadLbl, Priority.ALWAYS);

        // Badge estado
        Label badge = new Label(estadoTxt);
        badge.setPadding(new Insets(3, 10, 3, 10));
        badge.setStyle("-fx-background-color:" + bgColor + ";"
                + "-fx-background-radius:20;"
                + "-fx-text-fill:" + color + ";"
                + "-fx-font-weight:bold;-fx-font-size:10px;");

        topRow.getChildren().addAll(avatar, unidadLbl, badge);

        // Descripción
        String desc = (a.getDescripcion() != null && !a.getDescripcion().isBlank())
                ? a.getDescripcion() : "Sin descripción registrada.";
        Label descLbl = label(desc, 12, GRAY, false);
        descLbl.setWrapText(true);
        descLbl.setMaxWidth(Double.MAX_VALUE);

        // Fila inferior: fecha + alerta ID
        HBox bottomRow = new HBox(16);
        bottomRow.setAlignment(Pos.CENTER_LEFT);
        bottomRow.setPadding(new Insets(6, 0, 0, 0));

        // Fecha
        HBox fechaBox = new HBox(5);
        fechaBox.setAlignment(Pos.CENTER_LEFT);
        Label calIcon = new Label("\uf073");
        calIcon.setStyle("-fx-font-family:" + FA + ";-fx-font-size:10px;-fx-text-fill:" + GRAY + ";");
        String fechaTxt = a.getFechaatencion() != null
                ? a.getFechaatencion().format(FMT) : "Fecha no registrada";
        Label fechaLbl = label(fechaTxt, 11, GRAY, false);
        fechaBox.getChildren().addAll(calIcon, fechaLbl);

        // ID alerta
        if (a.getAlerta() != null) {
            HBox alertBox = new HBox(5);
            alertBox.setAlignment(Pos.CENTER_LEFT);
            Label alertIcon = new Label("\uf0f3");
            alertIcon.setStyle("-fx-font-family:" + FA + ";-fx-font-size:10px;-fx-text-fill:" + BLUE + ";");
            Label alertLbl = label("Alerta #" + a.getAlerta().getId_alerta(), 11, BLUE, false);
            alertBox.getChildren().addAll(alertIcon, alertLbl);
            bottomRow.getChildren().addAll(fechaBox, dotSep(), alertBox);
        } else {
            bottomRow.getChildren().add(fechaBox);
        }

        // Quién atendió
        if (a.getPolicia() != null) {
            String nombrePolicia = ((a.getPolicia().getPrimer_nombre() != null ? a.getPolicia().getPrimer_nombre() : "")
                    + " " + (a.getPolicia().getPrimer_apellido() != null ? a.getPolicia().getPrimer_apellido() : "")).trim();
            if (!nombrePolicia.isBlank()) {
                HBox policiaBox = new HBox(5);
                policiaBox.setAlignment(Pos.CENTER_LEFT);
                Label policiaIcon = new Label("\uf505");
                policiaIcon.setStyle("-fx-font-family:" + FA + ";-fx-font-size:10px;-fx-text-fill:" + GREEN + ";");
                Label policiaLbl = label("Atendido por: " + nombrePolicia, 11, GREEN, false);
                policiaBox.getChildren().addAll(policiaIcon, policiaLbl);
                bottomRow.getChildren().addAll(dotSep(), policiaBox);
            }
        }

        itemCard.getChildren().addAll(topRow, descLbl, bottomRow);

        // Hover en la card
        itemCard.setOnMouseEntered(e -> itemCard.setStyle(
                "-fx-background-color:white;"
                + "-fx-background-radius:12;"
                + "-fx-border-color:" + color + ";"
                + "-fx-border-width:1.5;"
                + "-fx-border-radius:12;"
                + "-fx-cursor:hand;"));
        itemCard.setOnMouseExited(e -> itemCard.setStyle(
                "-fx-background-color:" + BG + ";"
                + "-fx-background-radius:12;"
                + "-fx-border-color:" + BORDER + ";"
                + "-fx-border-width:1;"
                + "-fx-border-radius:12;"));

        content.getChildren().add(itemCard);
        row.getChildren().addAll(lineCol, content);
        return row;
    }

    // ── Estado vacío ─────────────────────────────────────────────
    private VBox buildEmptyState() {
        VBox box = new VBox(12);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(60, 0, 60, 0));

        StackPane iconWrap = new StackPane();
        iconWrap.setPrefSize(72, 72);
        iconWrap.setMinSize(72, 72);
        Circle bg = new Circle(36, Color.web(BLUE_LIGHT));
        Label icon = new Label("\uf1da");
        icon.setStyle("-fx-font-family:" + FA + ";-fx-font-size:28px;-fx-text-fill:" + BLUE + ";");
        iconWrap.getChildren().addAll(bg, icon);

        Label title = label("Sin atenciones registradas", 15, TEXT_DARK, true);
        Label sub = label("Las atenciones aparecerán aquí una vez registradas.", 12, GRAY, false);
        sub.setWrapText(true);
        sub.setAlignment(Pos.CENTER);

        box.getChildren().addAll(iconWrap, title, sub);
        return box;
    }

    // ── Helpers ───────────────────────────────────────────────────
    private String[] stateColors(EstadoAtencionAlerta e) {
        // [color, bgColor, faIcon]
        return switch (e) {
            case FINALIZADA ->
                new String[]{GREEN, GREEN_LIGHT, "\uf058"};
            case EN_PROCESO ->
                new String[]{ORANGE, ORANGE_LIGHT, "\uf017"};
            case CANCELADA ->
                new String[]{GRAY, GRAY_LIGHT, "\uf057"};
            default ->
                new String[]{RED, RED_LIGHT, "\uf071"};
        };
    }

    private List<AtencionAlerta> cargarLista() {
        try {
            return atencionService.listar();
        } catch (Exception e) {
            return List.of();
        }
    }

    private Label dotSep() {
        Label l = label("·", 12, GRAY, false);
        l.setPadding(new Insets(0, 2, 0, 2));
        return l;
    }

    private Region pad(double width) {
        Region r = new Region();
        r.setPrefWidth(width);
        r.setMinWidth(width);
        return r;
    }

    private static final String[] AVATAR_COLORS = {
        "#1565c0", "#2e7d32", "#6a1b9a", "#c62828",
        "#e65100", "#00695c", "#283593", "#4e342e"
    };

    private String avatarColor(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return AVATAR_COLORS[0];
        }
        return AVATAR_COLORS[Math.abs(nombre.hashCode()) % AVATAR_COLORS.length];
    }

    private String iniciales(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return "?";
        }
        String[] p = nombre.trim().split("\\s+");
        return p.length == 1
                ? p[0].substring(0, 1).toUpperCase()
                : (p[0].substring(0, 1) + p[1].substring(0, 1)).toUpperCase();
    }

    private Label label(String text, double size, String color, boolean bold) {
        Label lbl = new Label(text);
        lbl.setFont(bold ? Font.font("System", FontWeight.BOLD, size) : Font.font("System", size));
        try {
            lbl.setTextFill(Color.web(color));
        } catch (Exception ignored) {
            lbl.setTextFill(Color.web(GRAY));
        }
        return lbl;
    }
}
