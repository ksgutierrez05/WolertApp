package sistemagestion.view;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

import sistemagestion.model.*;
import sistemagestion.service.*;

public class EstadisticasAdminPoliciaView {

    private static final String BG = "#f4f6fb";
    private static final String RED = "#e53935";
    private static final String ORANGE = "#fb8c00";
    private static final String GREEN = "#43a047";
    private static final String BLUE = "#1565c0";
    private static final String PURPLE = "#7b1fa2";
    private static final String TEAL = "#00796b";
    private static final String GRAY = "#9e9e9e";
    private static final String GRAY_TEXT = "#6b7280";
    private static final String BORDER = "#e5e7eb";

    private static final String[] BAR_COLORS_ESTADO = {
        "#fb8c00", "#1565c0", "#7b1fa2", "#0288d1", "#43a047", "#9e9e9e"
    };
    private static final String[] BAR_COLORS_POLICIA = {
        "#43a047", "#1565c0", "#fb8c00", "#9e9e9e"
    };
    private static final String[] BAR_COLORS_ALARMA = {
        "#43a047", "#fb8c00", "#9e9e9e"
    };

    private final AlertaService alertaService;
    private final AsignacionUnidadService asignacionService;
    private final UnidadPolicialService unidadService;
    private final PoliciaService policiaService;
    private final AlarmaService alarmaService;
    private final NotificacionService notificacionService;

    private List<Alerta> alertas;
    private List<Policia> policias;
    private List<UnidadPolicial> unidades;
    private List<Alarma> alarmas;

    public EstadisticasAdminPoliciaView(
            AlertaService alertaService,
            AsignacionUnidadService asignacionService,
            UnidadPolicialService unidadService,
            PoliciaService policiaService,
            AlarmaService alarmaService,
            NotificacionService notificacionService) {
        Font.loadFont(getClass().getResourceAsStream("/fa-solid-900.ttf"), 20);
        this.alertaService = alertaService;
        this.asignacionService = asignacionService;
        this.unidadService = unidadService;
        this.policiaService = policiaService;
        this.alarmaService = alarmaService;
        this.notificacionService = notificacionService;
        cargarDatos();
    }

    // ── PUNTO DE ENTRADA ─────────────────────────────────────────
    public ScrollPane build() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color: " + BG + ";");

        content.getChildren().addAll(
                buildTopBar(),
                buildStatCards(),
                buildFilaCentral(),
                buildPanelEstadosAlarma()
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
                label("Métricas operacionales del sistema policial", 13, GRAY_TEXT, false));
        bar.getChildren().add(title);
        return bar;
    }

    // ── STAT CARDS ───────────────────────────────────────────────
    private HBox buildStatCards() {
        HBox row = new HBox(16);
        HBox.setHgrow(row, Priority.ALWAYS);

        long totalAlertas = alertas.size();
        long alertasActivas = alertas.stream()
                .filter(a -> a.getEstado() == EstadoAlerta.PENDIENTE
                || a.getEstado() == EstadoAlerta.RECIBIDA
                || a.getEstado() == EstadoAlerta.EN_ATENCION
                || a.getEstado() == EstadoAlerta.UNIDAD_ASIGNADA)
                .count();
        long policiasDisp = policias.stream()
                .filter(p -> p.getEstadopolicial() == EstadoPolicia.DISPONIBLE
                || p.getEstadopolicial() == EstadoPolicia.EN_SERVICIO)
                .count();
        long unidadesOper = unidades.stream()
                .filter(u -> u.getEstado() == EstadoUnidadPolicial.OPERATIVA)
                .count();

        row.getChildren().addAll(
                statCard("#e8f0fe", BLUE, "\uf0f3",
                        String.valueOf(totalAlertas), "Alertas totales", "Historial completo"),
                statCard("#fff0f0", RED, "\uf071",
                        String.valueOf(alertasActivas), "Alertas activas", "↑ Requieren atención"),
                statCard("#e8f5e9", GREEN, "\uf1b9",
                        String.valueOf(unidadesOper), "Unidades operativas", "Estado OPERATIVA"),
                statCard("#fff8e1", ORANGE, "\uf505",
                        String.valueOf(policiasDisp), "Policías activos", "DISPONIBLE · EN SERVICIO")
        );
        return row;
    }

    // ── FILA CENTRAL — por estado alerta | por estado policía ────
    private HBox buildFilaCentral() {
        HBox row = new HBox(16);
        row.getChildren().addAll(
                buildPanelBarrasEstadoAlerta(),
                buildPanelBarrasEstadoPolicia());
        return row;
    }

    private VBox buildPanelBarrasEstadoAlerta() {
        VBox panel = crearPanel("Alertas por estado", "", ORANGE, "#fff8e1");

        Map<String, Long> porEstado = new LinkedHashMap<>();
        porEstado.put("PENDIENTE", contarAlerta(EstadoAlerta.PENDIENTE));
        porEstado.put("RECIBIDA", contarAlerta(EstadoAlerta.RECIBIDA));
        porEstado.put("EN ATENCIÓN", contarAlerta(EstadoAlerta.EN_ATENCION));
        porEstado.put("UNIDAD ASIGNADA", contarAlerta(EstadoAlerta.UNIDAD_ASIGNADA));
        porEstado.put("RESUELTA", contarAlerta(EstadoAlerta.RESUELTA));
        porEstado.put("CANCELADA", contarAlerta(EstadoAlerta.CANCELADA));

        long maximo = porEstado.values().stream().mapToLong(Long::longValue).max().orElse(1);
        int idx = 0;
        for (Map.Entry<String, Long> entry : porEstado.entrySet()) {
            panel.getChildren().add(buildBarraHorizontal(
                    entry.getKey(), entry.getValue(), maximo,
                    BAR_COLORS_ESTADO[idx % BAR_COLORS_ESTADO.length]));
            idx++;
        }
        HBox.setHgrow(panel, Priority.ALWAYS);
        return panel;
    }

    private VBox buildPanelBarrasEstadoPolicia() {
        VBox panel = crearPanel("Policías por estado", "", BLUE, "#e8f0fe");

        Map<String, Long> porEstado = new LinkedHashMap<>();
        porEstado.put("DISPONIBLE", contarPolicia(EstadoPolicia.DISPONIBLE));
        porEstado.put("EN SERVICIO", contarPolicia(EstadoPolicia.EN_SERVICIO));
        porEstado.put("OCUPADO", contarPolicia(EstadoPolicia.OCUPADO));
        porEstado.put("FUERA DE SERVICIO", policias.stream()
                .filter(p -> p.getEstadopolicial() != EstadoPolicia.DISPONIBLE
                && p.getEstadopolicial() != EstadoPolicia.EN_SERVICIO
                && p.getEstadopolicial() != EstadoPolicia.OCUPADO).count());

        long maximo = porEstado.values().stream().mapToLong(Long::longValue).max().orElse(1);
        int idx = 0;
        for (Map.Entry<String, Long> entry : porEstado.entrySet()) {
            panel.getChildren().add(buildBarraHorizontal(
                    entry.getKey(), entry.getValue(), maximo,
                    BAR_COLORS_POLICIA[idx % BAR_COLORS_POLICIA.length]));
            idx++;
        }
        HBox.setHgrow(panel, Priority.ALWAYS);
        return panel;
    }

    private HBox buildBarraHorizontal(String nombre, long valor, long maximo, String color) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(4, 0, 4, 0));

        Label etiqueta = label(nombre, 12, GRAY_TEXT, false);
        etiqueta.setPrefWidth(140);
        etiqueta.setAlignment(Pos.CENTER_RIGHT);

        double pct = maximo > 0 ? (double) valor / maximo : 0;
        double maxBarWidth = 260;

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

    // ── PANEL INFERIOR — Estado de alarmas ───────────────────────
    private VBox buildPanelEstadosAlarma() {
        VBox panel = crearPanel("Estado de alarmas y unidades", "", RED, "#fff0f0");

        long totalAlarmas = Math.max(alarmas.size(), 1);
        record Info(String nombre, long cantidad, String color) {

        }

        List<Info> filas = List.of(
                new Info("Alarmas activas", contarAlarma(EstadoAlarma.ACTIVA), GREEN),
                new Info("Alarmas en mantenimiento", contarAlarma(EstadoAlarma.EN_MANTENIMIENTO), ORANGE),
                new Info("Alarmas inactivas", contarAlarma(EstadoAlarma.INACTIVA), GRAY),
                new Info("Unidades operativas", contarUnidad(EstadoUnidadPolicial.OPERATIVA), GREEN),
                new Info("Unidades activas", contarUnidad(EstadoUnidadPolicial.ACTIVA), ORANGE),
                new Info("Unidades inactivas", contarUnidad(EstadoUnidadPolicial.INACTIVA), GRAY)
        );

        for (Info info : filas) {
            long total = info.nombre().startsWith("Alarma")
                    ? totalAlarmas
                    : Math.max(unidades.size(), 1);
            int pct = (int) Math.round((double) info.cantidad() / total * 100);
            panel.getChildren().add(buildFilaEstado(info.color(), info.nombre(), info.cantidad(), pct));
        }

        panel.setMaxWidth(Double.MAX_VALUE);
        return panel;
    }

    private HBox buildFilaEstado(String color, String nombre, long cantidad, int pct) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(7, 0, 7, 0));

        Circle dot = new Circle(6, Color.web(color));

        Label nombreLbl = label(nombre, 13, "#374151", false);
        HBox.setHgrow(nombreLbl, Priority.ALWAYS);

        Label cantidadLbl = label(String.valueOf(cantidad), 13, "#111827", true);
        Label pctLbl = label(" (" + pct + "%)", 12, GRAY_TEXT, false);

        HBox numBox = new HBox(0, cantidadLbl, pctLbl);
        numBox.setAlignment(Pos.CENTER_RIGHT);

        row.getChildren().addAll(dot, nombreLbl, numBox);
        return row;
    }

    // ── CARGA DE DATOS ───────────────────────────────────────────
    private void cargarDatos() {
        try {
            alertas = alertaService.listar();
        } catch (Exception e) {
            alertas = List.of();
        }
        try {
            policias = policiaService.listar();
        } catch (Exception e) {
            policias = List.of();
        }
        try {
            unidades = unidadService.listar();
        } catch (Exception e) {
            unidades = List.of();
        }
        try {
            alarmas = alarmaService.listar();
        } catch (Exception e) {
            alarmas = List.of();
        }
    }

    private long contarAlerta(EstadoAlerta e) {
        return alertas.stream().filter(a -> a.getEstado() == e).count();
    }

    private long contarPolicia(EstadoPolicia e) {
        return policias.stream().filter(p -> p.getEstadopolicial() == e).count();
    }

    private long contarUnidad(EstadoUnidadPolicial e) {
        return unidades.stream().filter(u -> u.getEstado() == e).count();
    }

    private long contarAlarma(EstadoAlarma e) {
        return alarmas.stream().filter(a -> a.getEstado() == e).count();
    }

    // ── HELPERS UI ───────────────────────────────────────────────
    private VBox crearPanel(String titulo, String iconFA, String iconColor, String iconBg) {
        VBox panel = new VBox(12);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: white; -fx-background-radius: 16;");
        panel.setEffect(new DropShadow(15, 0, 3, Color.web("#0000001a")));

        StackPane iconWrap = new StackPane();
        iconWrap.setPrefSize(34, 34);
        iconWrap.setMinSize(34, 34);
        iconWrap.setMaxSize(34, 34);

        Rectangle bg = new Rectangle(34, 34);
        bg.setArcWidth(10);
        bg.setArcHeight(10);
        bg.setFill(Color.web(iconBg));

        Label iconLbl = new Label(iconFA);
        iconLbl.setStyle("-fx-font-family: 'Font Awesome 6 Free Solid';"
                + "-fx-font-size: 15px; -fx-text-fill: " + iconColor + ";");
        iconWrap.getChildren().addAll(bg, iconLbl);

        Label tituloLbl = new Label(titulo);
        tituloLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        HBox header = new HBox(10, iconWrap, tituloLbl);
        header.setAlignment(Pos.CENTER_LEFT);
        panel.getChildren().addAll(header, separadorH());
        return panel;
    }

    private VBox statCard(String bgIcon, String accentColor, String iconFA,
            String valor, String titulo, String variacion) {
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
        varLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: " + accentColor + ";");

        HBox top = new HBox(16, iconWrap, new VBox(3, tituloLbl, valorLbl, varLbl));
        top.setAlignment(Pos.CENTER_LEFT);
        card.getChildren().add(top);
        card.setOnMouseEntered(e -> card.setTranslateY(-3));
        card.setOnMouseExited(e -> card.setTranslateY(0));
        return card;
    }

    // CORRECCIÓN 3: wrapScroll movido dentro de la clase (era código muerto fuera de la clase)
    //              Se conserva por si se necesita en el futuro
    private ScrollPane wrapScroll(VBox content) {
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background: " + BG + "; -fx-background-color: " + BG + ";");
        return scroll;
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

    // CORRECCIÓN 4: recortarTransparencia movido dentro de la clase.
    //              Se reescribió usando javafx.scene.image.Image para evitar
    //              la mezcla AWT/JavaFX problemática en entornos modulares.
    //              Si realmente necesitas BufferedImage, añade
    //              requires java.desktop; en tu module-info.java.
    private javafx.scene.image.WritableImage recortarTransparencia(javafx.scene.image.Image image) {
        int w = (int) image.getWidth();
        int h = (int) image.getHeight();
        javafx.scene.image.PixelReader pr = image.getPixelReader();

        int minX = w, minY = h, maxX = 0, maxY = 0;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (pr.getColor(x, y).getOpacity() > 0) {
                    if (x < minX) {
                        minX = x;
                    }
                    if (y < minY) {
                        minY = y;
                    }
                    if (x > maxX) {
                        maxX = x;
                    }
                    if (y > maxY) {
                        maxY = y;
                    }
                }
            }
        }

        if (maxX < minX || maxY < minY) {
            return new javafx.scene.image.WritableImage(1, 1);
        }

        int cropW = maxX - minX + 1;
        int cropH = maxY - minY + 1;
        javafx.scene.image.WritableImage result = new javafx.scene.image.WritableImage(cropW, cropH);
        result.getPixelWriter().setPixels(
                0, 0, cropW, cropH,
                pr,
                minX, minY);
        return result;
    }

} // fin de la clase

