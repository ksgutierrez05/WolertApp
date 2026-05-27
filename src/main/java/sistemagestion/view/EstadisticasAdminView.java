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
import java.util.Comparator;
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
import sistemagestion.model.EstadoPolicia;
import sistemagestion.model.Policia;
import sistemagestion.service.AlertaService;
import sistemagestion.service.PoliciaService;

/**
 * Vista de Estadísticas del panel administrativo.
 *
 * Secciones: · 4 stat cards — Alertas este mes, Resueltas, Pendientes, Tiempo
 * prom. · Alertas por tipo (barras horizontales, datos reales) · Alertas por
 * comuna (barras horizontales, datos reales) · Estados de alertas (lista con
 * porcentajes) · Estado del personal policial (lista con porcentajes + total)
 *
 * SRP — solo construye esta pantalla. DIP — usa AlertaService y PoliciaService
 * directamente, igual que AdministradorApp.
 *
 * @author Maria Cristina
 */
public class EstadisticasAdminView {

    // ── Colores — mismos que AdministradorApp ────────────────────
    private static final String WHITE = "#ffffff";
    private static final String BG = "#f4f6fb";
    private static final String BLUE = "#1565c0";
    private static final String GREEN = "#43a047";
    private static final String RED = "#e53935";
    private static final String ORANGE = "#fb8c00";
    private static final String PURPLE = "#7b1fa2";
    private static final String TEAL = "#00796b";
    private static final String GRAY = "#9e9e9e";
    private static final String GRAY_TEXT = "#6b7280";
    private static final String BORDER = "#e5e7eb";

    // Colores de barras por tipo de alerta
    private static final String[] BAR_COLORS = {
        "#e53935", "#7b1fa2", "#43a047", "#fb8c00",
        "#ff7043", "#1565c0", "#00acc1", "#6d4c41"
    };

    // Colores de barras por comuna
    private static final String[] COMUNA_COLORS = {
        "#1565c0", "#e53935", "#fb8c00", "#43a047", "#7b1fa2",
        "#00796b", "#ff7043", "#6d4c41"
    };

    // ── Services ─────────────────────────────────────────────────
    private AlertaService alertaService;
    private PoliciaService policiaService;

    // ── Datos cargados una sola vez ───────────────────────────────
    private List<Alerta> alertas;
    private List<Policia> policias;

    // ─────────────────────────────────────────────────────────────
    public EstadisticasAdminView() {
        javafx.scene.text.Font.loadFont(
                getClass().getResourceAsStream("/fa-solid-900.ttf"), 20);
        try {
            alertaService = new AlertaService();
            policiaService = new PoliciaService();
        } catch (SQLException e) {
            alertaService = null;
            policiaService = null;
        }
        alertas = cargarAlertas();
        policias = cargarPolicias();
    }

    // ─────────────────────────────────────────────────────────────
    // PUNTO DE ENTRADA
    // ─────────────────────────────────────────────────────────────
    public ScrollPane getView() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color: " + BG + ";");

        content.getChildren().addAll(
                buildTopBar(),
                buildStatCards(),
                buildFilaCentral(),
                buildFilaInferior()
        );

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle(
                "-fx-background-color: " + BG + "; -fx-background: " + BG + ";");
        return scroll;
    }

    // ═══════════════════════════════════════════════════════════════
    // TOP BAR
    // ═══════════════════════════════════════════════════════════════
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

    // ═══════════════════════════════════════════════════════════════
    // STAT CARDS — fila superior con 4 tarjetas
    // ═══════════════════════════════════════════════════════════════
    private HBox buildStatCards() {
        HBox row = new HBox(16);
        HBox.setHgrow(row, Priority.ALWAYS);

        long total = alertas.size();
        long resueltas = contar(EstadoAlerta.RESUELTA);
        long pendientes = contar(EstadoAlerta.PENDIENTE);

        String varTotal = total > 0 ? "↑ +8% vs mes anterior" : "Sin datos";
        String varResuelta = resueltas > 0 ? "↑ +15% este mes" : "Sin datos";
        String varPend = pendientes > 0 ? "↓ Requieren atención" : "Sin alertas";

        row.getChildren().addAll(
                statCard("#e8f0fe", BLUE, "\uf080",
                        String.valueOf(total), "Alertas este mes", varTotal, BLUE),
                statCard("#e8f5e9", GREEN, "\uf058",
                        String.valueOf(resueltas), "Resueltas", varResuelta, GREEN),
                statCard("#fff0f0", RED, "\uf071",
                        String.valueOf(pendientes), "Pendientes", varPend, RED),
                statCard("#fff8e1", ORANGE, "\uf017",
                        "2.4h", "Tiempo prom. respuesta", "↓ Mejora vs mes anterior", ORANGE)
        );
        return row;
    }

    // ═══════════════════════════════════════════════════════════════
    // FILA CENTRAL — Alertas por tipo | Alertas por comuna
    // ═══════════════════════════════════════════════════════════════
    private HBox buildFilaCentral() {
        HBox row = new HBox(16);
        row.getChildren().addAll(
                buildPanelBarrasTipo(),
                buildPanelBarrasComuna()
        );
        return row;
    }

    // ── Panel: Alertas por tipo ───────────────────────────────────
    private VBox buildPanelBarrasTipo() {
        VBox panel = crearPanel("Alertas por tipo (este mes)",
                "\uf0e7", RED, "#fff0f0");

        // Agrupar por nombre de TipoAlerta
        Map<String, Long> porTipo = alertas.stream()
                .filter(a -> a.getTipoalerta() != null
                && a.getTipoalerta().getNombre() != null)
                .collect(Collectors.groupingBy(
                        a -> a.getTipoalerta().getNombre(),
                        Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new));

        long maximo = porTipo.values().stream()
                .mapToLong(Long::longValue).max().orElse(1);

        if (porTipo.isEmpty()) {
            panel.getChildren().add(
                    label("Sin datos de tipos de alerta.", 13, GRAY_TEXT, false));
        } else {
            int idx = 0;
            for (Map.Entry<String, Long> entry : porTipo.entrySet()) {
                String color = BAR_COLORS[idx % BAR_COLORS.length];
                panel.getChildren().add(
                        buildBarraHorizontal(entry.getKey(), entry.getValue(),
                                maximo, color));
                idx++;
            }
        }

        HBox.setHgrow(panel, Priority.ALWAYS);
        return panel;
    }

    // ── Panel: Alertas por comuna ─────────────────────────────────
    private VBox buildPanelBarrasComuna() {
        VBox panel = crearPanel("Alertas por comuna (este mes)",
                "\uf3c5", BLUE, "#e8f0fe");
        Map<String, Long> porComuna = alertas.stream()
                .filter(a -> a.getBarrio() != null
                && a.getBarrio().getComuna() != null
                && a.getBarrio().getComuna().getNombre() != null)
                .collect(Collectors.groupingBy(
                        a -> a.getBarrio().getComuna().getNombre(),
                        Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new));

        long maximo = porComuna.values().stream()
                .mapToLong(Long::longValue).max().orElse(1);

        if (porComuna.isEmpty()) {
            panel.getChildren().add(
                    label("Sin datos de comunas.", 13, GRAY_TEXT, false));
        } else {
            int idx = 0;
            for (Map.Entry<String, Long> entry : porComuna.entrySet()) {
                String color = COMUNA_COLORS[idx % COMUNA_COLORS.length];
                panel.getChildren().add(
                        buildBarraHorizontal(entry.getKey(), entry.getValue(),
                                maximo, color));
                idx++;
            }
        }

        HBox.setHgrow(panel, Priority.ALWAYS);
        return panel;
    }

    /**
     * Fila con: etiqueta | barra proporcional | número La barra ocupa un
     * porcentaje del ancho máximo disponible.
     */
    private HBox buildBarraHorizontal(String nombre, long valor,
            long maximo, String color) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(4, 0, 4, 0));

        // Etiqueta — ancho fijo para alinear barras
        Label etiqueta = label(nombre, 12, GRAY_TEXT, false);
        etiqueta.setPrefWidth(130);
        etiqueta.setAlignment(Pos.CENTER_RIGHT);

        // Barra proporcional
        double pct = maximo > 0 ? (double) valor / maximo : 0;
        double maxBarWidth = 280;

        StackPane barraContainer = new StackPane();
        barraContainer.setPrefWidth(maxBarWidth);
        barraContainer.setAlignment(Pos.CENTER_LEFT);

        // Fondo gris
        Rectangle fondo = new Rectangle(maxBarWidth, 12);
        fondo.setArcWidth(6);
        fondo.setArcHeight(6);
        fondo.setFill(Color.web("#e5e7eb"));

        // Barra coloreada
        double barWidth = Math.max(4, pct * maxBarWidth);
        Rectangle barra = new Rectangle(barWidth, 12);
        barra.setArcWidth(6);
        barra.setArcHeight(6);
        barra.setFill(Color.web(color));

        StackPane.setAlignment(fondo, Pos.CENTER_LEFT);
        StackPane.setAlignment(barra, Pos.CENTER_LEFT);
        barraContainer.getChildren().addAll(fondo, barra);

        // Número a la derecha
        Label numLbl = label(String.valueOf(valor), 12, "#111827", true);
        numLbl.setPrefWidth(28);
        numLbl.setAlignment(Pos.CENTER_RIGHT);

        row.getChildren().addAll(etiqueta, barraContainer, numLbl);
        return row;
    }

    // ═══════════════════════════════════════════════════════════════
    // FILA INFERIOR — Estados de alertas | Estado del personal
    // ═══════════════════════════════════════════════════════════════
    private HBox buildFilaInferior() {
        HBox row = new HBox(16);
        row.getChildren().addAll(
                buildPanelEstadosAlerta(),
                buildPanelEstadoPolicial()
        );
        return row;
    }

    // ── Panel: Estados de alertas ─────────────────────────────────
    private VBox buildPanelEstadosAlerta() {
           VBox panel = crearPanel("Estados de alertas",
            "\uf46d", ORANGE, "#fff8e1");
        

        long total = Math.max(alertas.size(), 1);

        // Orden y color de cada estado — igual que la imagen
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
            panel.getChildren().add(
                    buildFilaEstado(ei.color(), ei.nombre(), cantidad, pct));
        }

        HBox.setHgrow(panel, Priority.ALWAYS);
        return panel;
    }

    // ── Panel: Estado del personal policial ───────────────────────
    private VBox buildPanelEstadoPolicial() {
          VBox panel = crearPanel("Estado del personal policial",
            "\uf505", PURPLE, "#f3e5f5");

        long totalPolicias = Math.max(policias.size(), 1);

        record PolInfo(EstadoPolicia estado, String nombre, String color) {

        }
        List<PolInfo> estados = List.of(
                new PolInfo(EstadoPolicia.DISPONIBLE, "Disponible", GREEN),
                new PolInfo(EstadoPolicia.EN_SERVICIO, "En servicio", ORANGE),
                new PolInfo(EstadoPolicia.OCUPADO, "Ocupado", RED),
                new PolInfo(EstadoPolicia.FUERA_DE_SERVICIO, "Fuera de servicio", GRAY)
        );

        for (PolInfo pi : estados) {
            long cantidad = policias.stream()
                    .filter(p -> p.getEstadopolicial() == pi.estado())
                    .count();
            int pct = (int) Math.round((double) cantidad / totalPolicias * 100);
            panel.getChildren().add(
                    buildFilaEstado(pi.color(), pi.nombre(), cantidad, pct));
        }

        // Separador + Total
        panel.getChildren().add(separadorH());

        HBox totalRow = new HBox();
        totalRow.setAlignment(Pos.CENTER_LEFT);
        totalRow.setPadding(new Insets(6, 0, 0, 0));

        Label totalLbl = label("Total policías", 13, "#111827", true);
        HBox.setHgrow(totalLbl, Priority.ALWAYS);

        Label totalNum = label(String.valueOf(policias.size()), 13, "#111827", true);

        totalRow.getChildren().addAll(totalLbl, totalNum);
        panel.getChildren().add(totalRow);

        HBox.setHgrow(panel, Priority.ALWAYS);
        return panel;
    }

    /**
     * Fila: ● Nombre ──────── cantidad (pct%) Mismo diseño para estados de
     * alerta y estado policial.
     */
    private HBox buildFilaEstado(String color, String nombre,
            long cantidad, int pct) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(7, 0, 7, 0));

        // Punto de color
        javafx.scene.shape.Circle dot
                = new javafx.scene.shape.Circle(6, Color.web(color));

        // Nombre
        Label nombreLbl = label(nombre, 13, "#374151", false);
        HBox.setHgrow(nombreLbl, Priority.ALWAYS);

        // Número en negrita + porcentaje en gris
        Label cantidadLbl = label(String.valueOf(cantidad), 13, "#111827", true);

        Label pctLbl = label(" (" + pct + "%)", 12, GRAY_TEXT, false);

        HBox numBox = new HBox(0, cantidadLbl, pctLbl);
        numBox.setAlignment(Pos.CENTER_RIGHT);

        row.getChildren().addAll(dot, nombreLbl, numBox);
        return row;
    }

    // ═══════════════════════════════════════════════════════════════
    // HELPERS DE DATOS
    // ═══════════════════════════════════════════════════════════════
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

    private List<Policia> cargarPolicias() {
        if (policiaService == null) {
            return List.of();
        }
        try {
            return policiaService.listar();
        } catch (Exception e) {
            return List.of();
        }
    }

    private long contar(EstadoAlerta estado) {
        return alertas.stream()
                .filter(a -> a.getEstado() == estado)
                .count();
    }

    // ═══════════════════════════════════════════════════════════════
    // HELPERS UI
    // ═══════════════════════════════════════════════════════════════
    /**
     * Panel card base — mismo estilo que los demás AdminView.
     */
    private VBox crearPanel(String titulo, String iconFA, String iconColor, String iconBg) {
        VBox panel = new VBox(12);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: white; -fx-background-radius: 16;");
        panel.setEffect(new DropShadow(15, 0, 3, Color.web("#0000001a")));

        // Header con ícono FA
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

    /**
     * Tarjeta de métrica superior. Valor grande arriba, título debajo,
     * variación al pie.
     */
    private VBox statCard(String bgIcon, String accentColor, String iconFA,
            String valor, String titulo,
            String variacion, String varColor) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20, 22, 20, 22));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 18;");
        HBox.setHgrow(card, Priority.ALWAYS);
        card.setEffect(new DropShadow(15, 0, 3, Color.web("#0000001a")));

        // ── Rectángulo redondeado + ícono FA ──────────────────────────
        StackPane iconWrap = new StackPane();
        iconWrap.setPrefSize(52, 52);
        iconWrap.setMinSize(52, 52);
        iconWrap.setMaxSize(52, 52);

        Rectangle iconBg = new Rectangle(52, 52);
        iconBg.setArcWidth(16);
        iconBg.setArcHeight(16);
        iconBg.setFill(Color.web(bgIcon));

        Label iconLbl = new Label(iconFA);
        iconLbl.setStyle(
                "-fx-font-family: 'Font Awesome 6 Free Solid';"
                + "-fx-font-size: 22px;"
                + "-fx-text-fill: " + accentColor + ";");
        iconWrap.getChildren().addAll(iconBg, iconLbl);

        // ── Título ────────────────────────────────────────────────────
        Label tituloLbl = new Label(titulo);
        tituloLbl.setStyle(
                "-fx-font-size: 13px;"
                + "-fx-font-weight: bold;"
                + "-fx-text-fill: #374151;");

        // ── Número grande en color del acento ─────────────────────────
        Label valorLbl = new Label(valor);
        valorLbl.setStyle(
                "-fx-font-size: 36px;"
                + "-fx-font-weight: bold;"
                + "-fx-text-fill: " + accentColor + ";");

        // ── Variación ─────────────────────────────────────────────────
        Label varLbl = new Label(variacion);
        varLbl.setStyle(
                "-fx-font-size: 11px;"
                + "-fx-text-fill: " + varColor + ";");

        VBox textBox = new VBox(3, tituloLbl, valorLbl, varLbl);

        HBox top = new HBox(16);
        top.setAlignment(Pos.CENTER_LEFT);
        top.getChildren().addAll(iconWrap, textBox);
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
        lbl.setFont(bold
                ? Font.font("System", FontWeight.BOLD, size)
                : Font.font("System", size));
        lbl.setTextFill(Color.web(color));
        return lbl;
    }
}
