/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.view;

/**
 *
 * @author Maria Cristina
 */
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import sistemagestion.model.Alerta;
import sistemagestion.model.EstadoAlerta;
import sistemagestion.service.AlertaService;

public class EstadisticasAdminView {

    // ── Colores ──────────────────────────────────────────────────
    private static final String WHITE = "#ffffff";
    private static final String BG = "#f4f6fb";
    private static final String BLUE = "#1565c0";
    private static final String GREEN = "#43a047";
    private static final String RED = "#e53935";
    private static final String ORANGE = "#fb8c00";
    private static final String PURPLE = "#7b1fa2";
    private static final String GRAY = "#9e9e9e";
    private static final String GRAY_TEXT = "#6b7280";
    private static final String BORDER = "#e5e7eb";

    // Colores de barras
    private static final String[] BAR_COLORS = {
        "#e53935", "#7b1fa2", "#43a047", "#fb8c00",
        "#ff7043", "#1565c0", "#00acc1", "#6d4c41"
    };
    private static final String[] COMUNA_COLORS = {
        "#1565c0", "#e53935", "#fb8c00", "#43a047", "#7b1fa2",
        "#00796b", "#ff7043", "#6d4c41"
    };

    // ── Sidebar colors (para el mismo tono de botón) ──────────────
    private static final String SIDEBAR_BG_START = "#16283d";
    private static final String SIDEBAR_BG_END = "#1f3a56";

    private AlertaService alertaService;
    private List<Alerta> alertas;

    public EstadisticasAdminView() {
        javafx.scene.text.Font.loadFont(
                getClass().getResourceAsStream("/fa-solid-900.ttf"), 20);
        try {
            alertaService = new AlertaService();
        } catch (SQLException e) {
            alertaService = null;
        }
        alertas = cargarAlertas();
    }

    // ── PUNTO DE ENTRADA ─────────────────────────────────────────
    public ScrollPane getView() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color: " + BG + ";");

        content.getChildren().addAll(
                buildTopBar(),
                buildStatCards(),
                buildFilaCentral(),
                buildPanelEstadosAlerta() // solo este panel inferior
        );

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: " + BG + "; -fx-background: " + BG + ";");
        return scroll;
    }

    // ── TOP BAR ──────────────────────────────────────────────────
    private HBox buildTopBar() {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);
        VBox title = new VBox(4);
        Label h1 = new Label("Estadísticas");
        h1.setFont(Font.font("System", FontWeight.BOLD, 28));
        h1.setTextFill(Color.web("#111827"));
        title.getChildren().addAll(h1,
                label("Análisis y métricas del sistema de alertas", 13, GRAY_TEXT, false));
        bar.getChildren().add(title);
        return bar;
    }

    // ── STAT CARDS ───────────────────────────────────────────────
    private HBox buildStatCards() {
        HBox row = new HBox(16);
        HBox.setHgrow(row, Priority.ALWAYS);

        long total = alertas.size();
        long resueltas = contar(EstadoAlerta.RESUELTA);
        long pendientes = contar(EstadoAlerta.PENDIENTE);

        row.getChildren().addAll(
                statCard("#e8f0fe", BLUE, "\uf080",
                        String.valueOf(total), "Alertas totales", "Historial completo", BLUE),
                statCard("#e8f5e9", GREEN, "\uf058",
                        String.valueOf(resueltas), "Resueltas", "↑ +15% este mes", GREEN),
                statCard("#fff0f0", RED, "\uf071",
                        String.valueOf(pendientes), "Pendientes", "↓ Requieren atención", RED),
                statCard("#fff8e1", ORANGE, "\uf017",
                        "2.4h", "Tiempo prom. respuesta", "↓ Mejora vs mes anterior", ORANGE)
        );
        return row;
    }

    // ── FILA CENTRAL — por tipo | por comuna ─────────────────────
    private HBox buildFilaCentral() {
        HBox row = new HBox(16);
        row.getChildren().addAll(
                buildPanelBarrasTipo(),
                buildPanelBarrasComuna());
        return row;
    }

    private VBox buildPanelBarrasTipo() {
        VBox panel = crearPanel("Alertas por tipo", "\uf0e7", RED, "#fff0f0");

        Map<String, Long> porTipo = alertas.stream()
                .filter(a -> a.getTipoalerta() != null && a.getTipoalerta().getNombre() != null)
                .collect(Collectors.groupingBy(a -> a.getTipoalerta().getNombre(), Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new));

        long maximo = porTipo.values().stream().mapToLong(Long::longValue).max().orElse(1);

        if (porTipo.isEmpty()) {
            panel.getChildren().add(label("Sin datos de tipos de alerta.", 13, GRAY_TEXT, false));
        } else {
            int idx = 0;
            for (Map.Entry<String, Long> entry : porTipo.entrySet()) {
                panel.getChildren().add(
                        buildBarraHorizontal(entry.getKey(), entry.getValue(),
                                maximo, BAR_COLORS[idx % BAR_COLORS.length]));
                idx++;
            }
        }
        HBox.setHgrow(panel, Priority.ALWAYS);
        return panel;
    }

    private VBox buildPanelBarrasComuna() {
        VBox panel = crearPanel("Alertas por comuna", "\uf3c5", BLUE, "#e8f0fe");

        Map<String, Long> porComuna = alertas.stream()
                .filter(a -> a.getBarrio() != null
                && a.getBarrio().getComuna() != null
                && a.getBarrio().getComuna().getNombre() != null)
                .collect(Collectors.groupingBy(a -> a.getBarrio().getComuna().getNombre(),
                        Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new));

        long maximo = porComuna.values().stream().mapToLong(Long::longValue).max().orElse(1);

        if (porComuna.isEmpty()) {
            panel.getChildren().add(label("Sin datos de comunas.", 13, GRAY_TEXT, false));
        } else {
            int idx = 0;
            for (Map.Entry<String, Long> entry : porComuna.entrySet()) {
                panel.getChildren().add(
                        buildBarraHorizontal(entry.getKey(), entry.getValue(),
                                maximo, COMUNA_COLORS[idx % COMUNA_COLORS.length]));
                idx++;
            }
        }
        HBox.setHgrow(panel, Priority.ALWAYS);
        return panel;
    }

    private HBox buildBarraHorizontal(String nombre, long valor, long maximo, String color) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(4, 0, 4, 0));

        Label etiqueta = label(nombre, 12, GRAY_TEXT, false);
        etiqueta.setPrefWidth(130);
        etiqueta.setAlignment(Pos.CENTER_RIGHT);

        double pct = maximo > 0 ? (double) valor / maximo : 0;
        double maxBarWidth = 280;

        StackPane barraContainer = new StackPane();
        barraContainer.setPrefWidth(maxBarWidth);
        barraContainer.setAlignment(Pos.CENTER_LEFT);

        Rectangle fondo = new Rectangle(maxBarWidth, 12);
        fondo.setArcWidth(6);
        fondo.setArcHeight(6);
        fondo.setFill(Color.web("#e5e7eb"));

        Rectangle barra = new Rectangle(Math.max(4, pct * maxBarWidth), 12);
        barra.setArcWidth(6);
        barra.setArcHeight(6);
        barra.setFill(Color.web(color));

        StackPane.setAlignment(fondo, Pos.CENTER_LEFT);
        StackPane.setAlignment(barra, Pos.CENTER_LEFT);
        barraContainer.getChildren().addAll(fondo, barra);

        Label numLbl = label(String.valueOf(valor), 12, "#111827", true);
        numLbl.setPrefWidth(28);
        numLbl.setAlignment(Pos.CENTER_RIGHT);

        row.getChildren().addAll(etiqueta, barraContainer, numLbl);
        return row;
    }

    // ── PANEL ÚNICO INFERIOR — Estados de alertas ─────────────────
    private VBox buildPanelEstadosAlerta() {
        VBox panel = crearPanel("Estados de alertas", "\uf46d", ORANGE, "#fff8e1");

        long total = Math.max(alertas.size(), 1);

        record EstadoInfo(EstadoAlerta estado, String nombre, String color) {

        }
        List<EstadoInfo> estados = List.of(
                new EstadoInfo(EstadoAlerta.PENDIENTE, "Pendiente", RED),
                new EstadoInfo(EstadoAlerta.RECIBIDA, "Recibida", ORANGE),
                new EstadoInfo(EstadoAlerta.EN_ATENCION, "En atención", BLUE),
                new EstadoInfo(EstadoAlerta.UNIDAD_ASIGNADA, "Unidad asignada", PURPLE),
                new EstadoInfo(EstadoAlerta.RESUELTA, "Resuelta", GREEN),
                new EstadoInfo(EstadoAlerta.CANCELADA, "Cancelada", GRAY)
        );

        for (EstadoInfo ei : estados) {
            long cantidad = contar(ei.estado());
            int pct = (int) Math.round((double) cantidad / total * 100);
            panel.getChildren().add(buildFilaEstado(ei.color(), ei.nombre(), cantidad, pct));
        }

        // El panel ocupa el ancho completo (no comparte fila con nada)
        panel.setMaxWidth(Double.MAX_VALUE);
        return panel;
    }

    private HBox buildFilaEstado(String color, String nombre, long cantidad, int pct) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(7, 0, 7, 0));

        javafx.scene.shape.Circle dot = new javafx.scene.shape.Circle(6, Color.web(color));

        Label nombreLbl = label(nombre, 13, "#374151", false);
        HBox.setHgrow(nombreLbl, Priority.ALWAYS);

        Label cantidadLbl = label(String.valueOf(cantidad), 13, "#111827", true);
        Label pctLbl = label(" (" + pct + "%)", 12, GRAY_TEXT, false);

        HBox numBox = new HBox(0, cantidadLbl, pctLbl);
        numBox.setAlignment(Pos.CENTER_RIGHT);

        row.getChildren().addAll(dot, nombreLbl, numBox);
        return row;
    }

    // ── HELPERS DE DATOS ─────────────────────────────────────────
    private List<Alerta> cargarAlertas() {
        if (alertaService == null) {
            return List.of();
        }
        try {
            return alertaService.listar();
        } catch (Exception e) {
            return List.of();
        }
    }

    private long contar(EstadoAlerta estado) {
        return alertas.stream().filter(a -> a.getEstado() == estado).count();
    }

    // ── HELPERS UI ───────────────────────────────────────────────
    private VBox crearPanel(String titulo, String iconFA, String iconColor, String iconBg) {
        VBox panel = new VBox(12);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: white; -fx-background-radius: 16;");
        panel.setEffect(new DropShadow(15, 0, 3, Color.web("#0000001a")));

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        StackPane iconWrap = new StackPane();
        iconWrap.setPrefSize(34, 34);
        iconWrap.setMinSize(34, 34);
        iconWrap.setMaxSize(34, 34);

        Rectangle bg = new Rectangle(34, 34);
        bg.setArcWidth(10);
        bg.setArcHeight(10);
        bg.setFill(Color.web(iconBg));

        Label iconLbl = new Label(iconFA);
        iconLbl.setStyle(
                "-fx-font-family: 'Font Awesome 6 Free Solid';"
                + "-fx-font-size: 15px;"
                + "-fx-text-fill: " + iconColor + ";");
        iconWrap.getChildren().addAll(bg, iconLbl);

        Label tituloLbl = new Label(titulo);
        tituloLbl.setStyle(
                "-fx-font-size: 14px;"
                + "-fx-font-weight: bold;"
                + "-fx-text-fill: #111827;");

        header.getChildren().addAll(iconWrap, tituloLbl);
        panel.getChildren().addAll(header, separadorH());
        return panel;
    }

    private VBox statCard(String bgIcon, String accentColor, String iconFA,
            String valor, String titulo, String variacion, String varColor) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20, 22, 20, 22));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 18;");
        HBox.setHgrow(card, Priority.ALWAYS);
        card.setEffect(new DropShadow(15, 0, 3, Color.web("#0000001a")));

        StackPane iconWrap = new StackPane();
        iconWrap.setPrefSize(52, 52);
        iconWrap.setMinSize(52, 52);
        iconWrap.setMaxSize(52, 52);
        Rectangle iconBg = new Rectangle(52, 52);
        iconBg.setArcWidth(16);
        iconBg.setArcHeight(16);
        iconBg.setFill(Color.web(bgIcon));
        Label iconLbl = new Label(iconFA);
        iconLbl.setStyle("-fx-font-family: 'Font Awesome 6 Free Solid';"
                + "-fx-font-size: 22px; -fx-text-fill: " + accentColor + ";");
        iconWrap.getChildren().addAll(iconBg, iconLbl);

        Label tituloLbl = new Label(titulo);
        tituloLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #374151;");
        Label valorLbl = new Label(valor);
        valorLbl.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: " + accentColor + ";");
        Label varLbl = new Label(variacion);
        varLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: " + varColor + ";");

        HBox top = new HBox(16, iconWrap, new VBox(3, tituloLbl, valorLbl, varLbl));
        top.setAlignment(Pos.CENTER_LEFT);
        card.getChildren().add(top);
        card.setOnMouseEntered(e -> card.setTranslateY(-3));
        card.setOnMouseExited(e -> card.setTranslateY(0));
        return card;
    }

    private Region separadorH() {
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
}
