/*
 * ReportesPoliciaView.java
 * Refactorizado aplicando principios GRASP:
 *  - Information Expert  : ReporteEstadisticasHelper calcula métricas con sus propios datos
 *  - Creator             : ExcelExportHelper crea libros Excel; DialogHelper crea ventanas
 *  - Low Coupling        : FiltroAtenciones y FiltroAlertas encapsulan filtrado independientemente de la UI
 *  - High Cohesion       : cada clase interna tiene una sola responsabilidad
 *  - Controller          : PaginadorHelper gestiona el estado de paginación y la re-renderización
 *  - Pure Fabrication    : UiFactory agrupa utilidades de construcción de nodos JavaFX
 *  - Polymorphism        : BadgeFactory centraliza la variación de colores/iconos por estado/tipo
 */
package sistemagestion.view;

import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javafx.stage.FileChooser;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import sistemagestion.model.Alerta;
import sistemagestion.model.AtencionAlerta;
import sistemagestion.model.AsignacionUnidad;
import sistemagestion.model.Policia;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import sistemagestion.model.EstadoAtencionAlerta;
import sistemagestion.service.*;

public class ReportesPoliciaView {

    // ══════════════════════════════════════════════════════════════
    // PALETA — constantes de color compartidas por todas las clases internas
    // ══════════════════════════════════════════════════════════════
    static final String WHITE = "#ffffff";
    static final String BG = "#f4f6fb";
    static final String BORDER = "#e5e7eb";
    static final String GRAY_TEXT = "#6b7280";
    static final String C_RED = "#e53935";
    static final String C_RED_BG = "#fff0f0";
    static final String C_ORG = "#fb8c00";
    static final String C_ORG_BG = "#fff8e1";
    static final String C_GRN = "#43a047";
    static final String C_GRN_BG = "#e8f5e9";
    static final String C_BLU = "#1565c0";
    static final String C_BLU_BG = "#e8f0fe";
    static final String C_PUR = "#7b1fa2";
    static final String C_PUR_BG = "#f3e5f5";
    static final String C_TEA = "#00695c";
    static final String C_TEA_BG = "#e0f2f1";
    static final String C_AMB = "#f9a825";
    static final String C_AMB_BG = "#fffde7";

    // Font Awesome unicode
    static final String FA_SHIELD = "\uf505";
    static final String FA_CLIPBOARD = "\uf46d";
    static final String FA_BELL = "\uf0f3";
    static final String FA_CHECK = "\uf00c";
    static final String FA_CLOCK = "\uf017";
    static final String FA_FILTER = "\uf0b0";
    static final String FA_TIMES = "\uf00d";
    static final String FA_SEARCH = "\uf002";
    static final String FA_DOWNLOAD = "\uf019";
    static final String FA_LIST = "\uf03a";
    static final String FA_CHART = "\uf080";
    static final String FA_STAR = "\uf005";
    static final String FA_BAN = "\uf05e";
    static final String FA_SPINNER = "\uf110";
    static final String FA_MAP = "\uf279";
    static final String FA_THUMBTACK = "\uf08d";
    static final String FA_USER = "\uf007";

    // Anchos de columnas
    static final double[] W_ATENC = {45, 85, 130, 155, 200, 130, 190};
    static final double[] W_ALERT = {45, 140, 120, 120, 155, 180, 130, 150};

    private static final int FILAS_POR_PAGINA = 8;

    // ── Servicios ─────────────────────────────────────────────────
    private final AtencionAlertaService atencionService;
    private final AsignacionUnidadService asignacionService;
    private final AlertaService alertaService;
    private final Policia policiaLogueado;

    // ── Datos cacheados ───────────────────────────────────────────
    private List<AtencionAlerta> atencionesCached;
    private List<AsignacionUnidad> asignacionesCached;
    private List<Alerta> alertasCached;

    // ── Helpers GRASP ─────────────────────────────────────────────
    private ReporteEstadisticasHelper estadisticasHelper; // Information Expert
    private FiltroAtenciones filtroAtenc;        // Low Coupling
    private FiltroAlertas filtroAlert;        // Low Coupling
    private PaginadorHelper pagAtenc;           // Controller
    private PaginadorHelper pagAlert;           // Controller

    // ── Referencias a nodos que se actualizan ─────────────────────
    private VBox tbodyAtencRef;
    private VBox tbodyAlertasRef;
    private Label lblMostrandoAtencRef;
    private Label lblMostrandoAlertasRef;
    private HBox paginacionAtencRef;
    private HBox paginacionAlertasRef;

    // ══════════════════════════════════════════════════════════════
    // CONSTRUCTORES
    // ══════════════════════════════════════════════════════════════
    public ReportesPoliciaView(
            Policia policiaLogueado,
            AtencionAlertaService atencionService,
            AsignacionUnidadService asignacionService,
            AlertaService alertaService,
            NotificacionService notificacionService,
            AlarmaService alarmaService) {
        this.policiaLogueado = policiaLogueado;
        this.atencionService = atencionService;
        this.asignacionService = asignacionService;
        this.alertaService = alertaService;
    }

    public ReportesPoliciaView(
            Policia policiaLogueado,
            AtencionAlertaService atencionService,
            AsignacionUnidadService asignacionService,
            AlertaService alertaService) {
        this(policiaLogueado, atencionService, asignacionService, alertaService, null, null);
    }

    // ══════════════════════════════════════════════════════════════
    // PUNTO DE ENTRADA
    // ══════════════════════════════════════════════════════════════
    public ScrollPane build() {
        Font.loadFont(getClass().getResourceAsStream("/fa-solid-900.ttf"), 16);

        cargarDatos();

        // Information Expert: el helper recibe los datos que necesita para calcular
        estadisticasHelper = new ReporteEstadisticasHelper(atencionesCached, asignacionesCached, alertasCached);

        // Low Coupling: los filtros sólo conocen sus listas y criterios
        filtroAtenc = new FiltroAtenciones(atencionesCached);
        filtroAlert = new FiltroAlertas(alertasCached);

        // Controller: paginadores controlan estado de página y disparan re-render
        pagAtenc = new PaginadorHelper(FILAS_POR_PAGINA, () -> renderizarAtenciones(W_ATENC));
        pagAlert = new PaginadorHelper(FILAS_POR_PAGINA, () -> renderizarAlertasAsignadas(W_ALERT));

        VBox content = new VBox(20);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color:" + BG + ";");

        content.getChildren().add(buildTopBar());
        content.getChildren().add(buildStatsRow());

        try {
            content.getChildren().add(UiFactory.seccionLabel("Resumen de Actividad"));
            content.getChildren().add(buildResumenCard());
            content.getChildren().add(UiFactory.seccionLabel("Mis Atenciones de Alertas"));
            content.getChildren().add(buildFiltrosAtencCard());
            content.getChildren().add(buildTablaAtenciones());
            content.getChildren().add(buildPaginacionAtenc());
            content.getChildren().add(UiFactory.seccionLabel("Alertas Asignadas a mi Unidad"));
            content.getChildren().add(buildFiltrosAlertasCard());
            content.getChildren().add(buildTablaAlertasAsignadas());
            content.getChildren().add(buildPaginacionAlertas());
        } catch (Exception e) {
            content.getChildren().add(buildErrorState(e.getMessage()));
        }

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color:" + BG + "; -fx-background:" + BG + ";");
        return scroll;
    }

    // ── Carga y filtrado inicial de datos ─────────────────────────
    private void cargarDatos() {
        String unidadNombre = (policiaLogueado != null && policiaLogueado.getUnidadpolicial() != null)
                ? policiaLogueado.getUnidadpolicial().getNombre() : null;

        List<AtencionAlerta> todas = atencionService != null ? atencionService.listar() : List.of();
        List<AsignacionUnidad> todasAsig = asignacionService != null ? asignacionService.listar() : List.of();
        List<Alerta> todasAlertas = alertaService != null ? alertaService.listar() : List.of();

        if (unidadNombre != null) {
            atencionesCached = filtrarPorUnidad(todas, unidadNombre);
            asignacionesCached = filtrarAsignacionesPorUnidad(todasAsig, unidadNombre);
            List<Integer> idsAsignadas = asignacionesCached.stream()
                    .filter(a -> a.getAlerta() != null)
                    .map(a -> a.getAlerta().getId_alerta())
                    .distinct().collect(Collectors.toList());
            alertasCached = todasAlertas.stream()
                    .filter(a -> idsAsignadas.contains(a.getId_alerta()))
                    .collect(Collectors.toList());
        } else {
            atencionesCached = List.of();
            asignacionesCached = List.of();
            alertasCached = List.of();
        }
    }

    private List<AtencionAlerta> filtrarPorUnidad(List<AtencionAlerta> lista, String nombre) {
        return lista.stream()
                .filter(a -> a.getUnidad() != null && nombre.equalsIgnoreCase(a.getUnidad().getNombre()))
                .collect(Collectors.toList());
    }

    private List<AsignacionUnidad> filtrarAsignacionesPorUnidad(List<AsignacionUnidad> lista, String nombre) {
        return lista.stream()
                .filter(a -> a.getUnidadpolicial() != null && nombre.equalsIgnoreCase(a.getUnidadpolicial().getNombre()))
                .collect(Collectors.toList());
    }

    // ══════════════════════════════════════════════════════════════
    // TOP BAR
    // ══════════════════════════════════════════════════════════════
    private HBox buildTopBar() {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);

        StackPane titleIcon = UiFactory.faIconBox(FA_SHIELD, 22, C_BLU, C_BLU_BG, 44);

        String nombrePolicia = policiaLogueado != null
                ? policiaLogueado.getPrimer_nombre() + " " + policiaLogueado.getPrimer_apellido() : "Policía";
        String unidad = (policiaLogueado != null && policiaLogueado.getUnidadpolicial() != null)
                ? policiaLogueado.getUnidadpolicial().getNombre() : "Sin unidad";

        VBox titles = new VBox(4);
        Label title = new Label("Mis Reportes de Actividad");
        title.setFont(Font.font("System", FontWeight.BOLD, 26));
        title.setTextFill(Color.web("#111827"));
        Label sub = UiFactory.lbl("Oficial: " + nombrePolicia + "  ·  Unidad: " + unidad, 13, GRAY_TEXT, false);
        titles.getChildren().addAll(title, sub);

        HBox titleRow = new HBox(14, titleIcon, titles);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        HBox right = new HBox(12);
        right.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(right, Priority.ALWAYS);

        // Creator: ExcelExportHelper es quien sabe crear los archivos Excel
        Button btnExportAtenc = buildExportBtn("Exportar Atenciones");
        Button btnExportAlertas = buildExportBtn("Exportar Alertas");
        btnExportAtenc.setOnAction(e
                -> new ExcelExportHelper(policiaLogueado, atencionesCached)
                        .exportarAtenciones(filtroAtenc.getResultados(), btnExportAtenc));
        btnExportAlertas.setOnAction(e
                -> new ExcelExportHelper(policiaLogueado, atencionesCached)
                        .exportarAlertasAsignadas(filtroAlert.getResultados(), btnExportAlertas));

        right.getChildren().addAll(btnExportAtenc, btnExportAlertas);
        bar.getChildren().addAll(titleRow, right);
        return bar;
    }

    private Button buildExportBtn(String texto) {
        Button b = new Button();
        Label ico = UiFactory.faLabel(FA_DOWNLOAD, 13, WHITE);
        Label txt = new Label("  " + texto);
        txt.setStyle("-fx-text-fill:white;-fx-font-size:12px;-fx-font-weight:bold;");
        HBox content = new HBox(4, ico, txt);
        content.setAlignment(Pos.CENTER);
        b.setGraphic(content);
        String base = "-fx-background-color:linear-gradient(to right,#16283d,#1f3a56);-fx-background-radius:8;-fx-padding:10 18;-fx-cursor:hand;";
        String hover = "-fx-background-color:linear-gradient(to right,#0f1e30,#16283d);-fx-background-radius:8;-fx-padding:10 18;-fx-cursor:hand;";
        b.setStyle(base);
        b.setOnMouseEntered(e -> b.setStyle(hover));
        b.setOnMouseExited(e -> b.setStyle(base));
        return b;
    }

    // ══════════════════════════════════════════════════════════════
    // STATS ROW  — delega cálculos al Information Expert
    // ══════════════════════════════════════════════════════════════
    private HBox buildStatsRow() {
        HBox row = new HBox(16);
        HBox.setHgrow(row, Priority.ALWAYS);
        row.getChildren().addAll(
                statCard(C_BLU_BG, C_BLU, FA_CLIPBOARD, "Mis Atenciones", estadisticasHelper.totalAtenciones(), "Total de atenciones registradas"),
                statCard(C_GRN_BG, C_GRN, FA_CHECK, "Finalizadas", estadisticasHelper.finalizadas(), "Atenciones resueltas"),
                statCard(C_ORG_BG, C_ORG, FA_SPINNER, "En Proceso", estadisticasHelper.enProceso(), "Atenciones activas"),
                statCard(C_RED_BG, C_RED, FA_BAN, "Canceladas", estadisticasHelper.canceladas(), "Atenciones canceladas"),
                statCard(C_PUR_BG, C_PUR, FA_BELL, "Alertas Asignadas", estadisticasHelper.totalAlertas(), "De mi unidad"),
                statCard(C_TEA_BG, C_TEA, FA_MAP, "Despachos", estadisticasHelper.totalAsignaciones(), "Asignaciones de unidad")
        );
        return row;
    }

    private VBox statCard(String bgIcon, String accent, String faIcon,
            String title, long value, String sub) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(18, 20, 18, 20));
        card.setStyle("-fx-background-color:white;-fx-background-radius:18;");
        HBox.setHgrow(card, Priority.ALWAYS);
        UiFactory.shadow(card);

        StackPane iconWrap = UiFactory.faIconBox(faIcon, 20, accent, bgIcon, 48);
        Label valueLbl = new Label(String.valueOf(value));
        valueLbl.setFont(Font.font("System", FontWeight.BOLD, 32));
        valueLbl.setTextFill(Color.web(accent));
        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#374151;");
        Label subLbl = new Label(sub);
        subLbl.setStyle("-fx-font-size:11px;-fx-text-fill:" + GRAY_TEXT + ";");

        Rectangle bar = new Rectangle();
        bar.setHeight(3);
        bar.setArcWidth(3);
        bar.setArcHeight(3);
        bar.setFill(Color.web(accent));
        bar.widthProperty().bind(card.widthProperty().subtract(40));

        VBox textBox = new VBox(2, titleLbl, valueLbl, subLbl);
        HBox top = new HBox(14, iconWrap, textBox);
        top.setAlignment(Pos.CENTER_LEFT);
        card.getChildren().addAll(top, bar);
        card.setOnMouseEntered(e -> card.setTranslateY(-3));
        card.setOnMouseExited(e -> card.setTranslateY(0));
        return card;
    }

    // ══════════════════════════════════════════════════════════════
    // RESUMEN  — Information Expert provee los valores
    // ══════════════════════════════════════════════════════════════
    private VBox buildResumenCard() {
        VBox card = new VBox(0);
        card.setStyle("-fx-background-color:white;-fx-background-radius:14;");
        UiFactory.shadow(card);
        card.getChildren().add(UiFactory.cardHeaderBox("Desglose de mis atenciones", C_BLU, FA_CHART));

        String[][] items = {
            {FA_CHECK, "Atenciones finalizadas", String.valueOf(estadisticasHelper.finalizadas()), C_GRN, C_GRN_BG},
            {FA_SPINNER, "Atenciones en proceso", String.valueOf(estadisticasHelper.enProceso()), C_ORG, C_ORG_BG},
            {FA_CLOCK, "Atenciones pendientes", String.valueOf(estadisticasHelper.pendientes()), C_AMB, C_AMB_BG},
            {FA_BAN, "Atenciones canceladas", String.valueOf(estadisticasHelper.canceladas()), C_RED, C_RED_BG},
            {FA_THUMBTACK, "Total despachos de mi unidad", String.valueOf(estadisticasHelper.totalAsignaciones()), C_TEA, C_TEA_BG},
            {FA_STAR, "Tipo de alerta más frecuente", estadisticasHelper.tipoAlertaMasFrecuente(), C_PUR, C_PUR_BG},};

        for (int i = 0; i < items.length; i++) {
            card.getChildren().add(buildDetailRow(items[i][0], items[i][1], items[i][2], items[i][3], items[i][4]));
            if (i < items.length - 1) {
                card.getChildren().add(UiFactory.divider());
            }
        }
        return card;
    }

    private HBox buildDetailRow(String faIcon, String labelText, String value, String color, String bgColor) {
        HBox row = new HBox(14);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(14, 20, 14, 20));
        row.setStyle("-fx-background-color:transparent;");
        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color:#f8f9fd;"));
        row.setOnMouseExited(e -> row.setStyle("-fx-background-color:transparent;"));

        StackPane iconBox = UiFactory.faIconBox(faIcon, 17, color, bgColor, 38);
        Label lbl = new Label(labelText);
        lbl.setFont(Font.font("System", 13));
        lbl.setTextFill(Color.web("#374151"));
        HBox.setHgrow(lbl, Priority.ALWAYS);

        Label valLbl = new Label(value);
        valLbl.setFont(Font.font("System", FontWeight.BOLD, 15));
        valLbl.setTextFill(Color.web(color));
        valLbl.setPadding(new Insets(4, 14, 4, 14));
        valLbl.setStyle("-fx-background-color:" + bgColor + ";-fx-background-radius:20;");

        row.getChildren().addAll(iconBox, lbl, valLbl);
        return row;
    }

    // ══════════════════════════════════════════════════════════════
    // FILTROS ATENCIONES  — Low Coupling: FiltroAtenciones guarda los controles
    // ══════════════════════════════════════════════════════════════
    private VBox buildFiltrosAtencCard() {
        VBox card = new VBox(0);
        card.setStyle("-fx-background-color:white;-fx-background-radius:14;");
        UiFactory.shadow(card);
        card.getChildren().add(UiFactory.cardHeaderBox("Filtrar mis atenciones", C_BLU, FA_FILTER));

        filtroAtenc.buildControles();

        Button btnAplicar = accionBtn("Aplicar filtros", FA_FILTER, C_BLU,
                "-fx-background-color:" + C_BLU + ";-fx-text-fill:white;-fx-background-radius:8;-fx-padding:8 18;-fx-cursor:hand;-fx-font-weight:bold;");
        btnAplicar.setOnAction(e -> {
            filtroAtenc.aplicar();
            pagAtenc.reiniciar();
            renderizarAtenciones(W_ATENC);
        });

        Button btnLimpiar = accionBtn("Limpiar", FA_TIMES, C_RED,
                "-fx-background-color:" + C_RED_BG + ";-fx-text-fill:" + C_RED + ";-fx-background-radius:8;-fx-padding:8 18;-fx-cursor:hand;-fx-font-weight:bold;");
        btnLimpiar.setOnAction(e -> {
            filtroAtenc.limpiar();
            pagAtenc.reiniciar();
            renderizarAtenciones(W_ATENC);
        });

        HBox fila1 = new HBox(16,
                UiFactory.labeledControl(UiFactory.labelFiltro("Estado:"), filtroAtenc.cbEstado, 140),
                UiFactory.labeledControl(UiFactory.labelFiltro("Buscar:"), filtroAtenc.txtBuscar, -1));
        fila1.setAlignment(Pos.CENTER_LEFT);
        fila1.setPadding(new Insets(14, 20, 10, 20));

        HBox fila2 = new HBox(16,
                UiFactory.labeledControl(UiFactory.labelFiltro("Desde:"), filtroAtenc.dpDesde, 160),
                UiFactory.labeledControl(UiFactory.labelFiltro("Hasta:"), filtroAtenc.dpHasta, 160),
                btnAplicar, btnLimpiar);
        fila2.setAlignment(Pos.CENTER_LEFT);
        fila2.setPadding(new Insets(0, 20, 14, 20));

        card.getChildren().addAll(fila1, fila2);
        return card;
    }

    // ══════════════════════════════════════════════════════════════
    // TABLA ATENCIONES
    // ══════════════════════════════════════════════════════════════
    private VBox buildTablaAtenciones() {
        VBox card = new VBox(0);
        card.setStyle("-fx-background-color:white;-fx-background-radius:14;");
        UiFactory.shadow(card);
        card.getChildren().add(UiFactory.cardHeaderBox("Listado de mis Atenciones", C_BLU, FA_CLIPBOARD));
        card.getChildren().add(UiFactory.buildThead(
                new String[]{"#", "Alerta", "Estado", "Fecha Atención", "Descripción", "Tipo Arma", "Observación"}, W_ATENC));

        tbodyAtencRef = new VBox(0);
        lblMostrandoAtencRef = UiFactory.lbl("", 12, GRAY_TEXT, false);
        renderizarAtenciones(W_ATENC);

        card.getChildren().addAll(tbodyAtencRef, UiFactory.buildFooter(lblMostrandoAtencRef));
        return card;
    }

    private void renderizarAtenciones(double[] w) {
        tbodyAtencRef.getChildren().clear();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        List<AtencionAlerta> lista = filtroAtenc.getResultados();
        int total = lista.size();
        int desde = pagAtenc.inicio(total);
        int hasta = pagAtenc.fin(total);

        if (total == 0) {
            tbodyAtencRef.getChildren().add(UiFactory.buildVacioState("No hay atenciones que coincidan con los filtros"));
            actualizarLblAtenc();
            actualizarPaginacionAtenc(w);
            return;
        }
        for (int i = desde; i < hasta; i++) {
            AtencionAlerta at = lista.get(i);
            boolean par = i % 2 == 0;

            HBox fila = UiFactory.buildFilaBase(par);
            fila.setOnMouseEntered(e -> fila.setStyle(UiFactory.filaBgHover(C_BLU_BG)));
            fila.setOnMouseExited(e -> fila.setStyle(UiFactory.filaBgNormal(par)));

            String alertaId = at.getAlerta() != null ? "Alerta #" + at.getAlerta().getId_alerta() : "—";
            String estado = at.getEstado() != null ? at.getEstado().name() : "—";
            String fecha = at.getFechaatencion() != null ? at.getFechaatencion().format(fmt) : "—";
            String tipoArma = at.getTipoarma() != null ? at.getTipoarma().getNombre() : "—";

            fila.getChildren().add(UiFactory.buildNumBadge(i + 1, C_BLU_BG, C_BLU, w[0]));
            fila.getChildren().add(UiFactory.celdaFija(alertaId, w[1]));
            fila.getChildren().add(UiFactory.hboxCell(BadgeFactory.estadoAtencBadge(estado), w[2]));
            fila.getChildren().add(UiFactory.hboxCell(UiFactory.fechaBox(fecha), w[3]));
            fila.getChildren().add(UiFactory.celdaFija(at.getDescripcion(), w[4]));
            fila.getChildren().add(UiFactory.celdaFija(tipoArma, w[5]));
            fila.getChildren().add(UiFactory.celdaFija(at.getObservacion(), w[6]));

            tbodyAtencRef.getChildren().add(fila);
        }
        actualizarLblAtenc();
        actualizarPaginacionAtenc(w);
    }

    private void actualizarLblAtenc() {
        if (lblMostrandoAtencRef == null) {
            return;
        }
        List<AtencionAlerta> lista = filtroAtenc.getResultados();
        int total = lista.size();
        int desde = pagAtenc.inicio(total);
        int hasta = pagAtenc.fin(total);
        lblMostrandoAtencRef.setText("Mostrando " + (total == 0 ? 0 : desde + 1) + " – " + hasta
                + " de " + total + " atenciones"
                + (total < atencionesCached.size() ? " (filtradas de " + atencionesCached.size() + " totales)" : ""));
    }

    // ══════════════════════════════════════════════════════════════
    // FILTROS ALERTAS  — Low Coupling
    // ══════════════════════════════════════════════════════════════
    private VBox buildFiltrosAlertasCard() {
        VBox card = new VBox(0);
        card.setStyle("-fx-background-color:white;-fx-background-radius:14;");
        UiFactory.shadow(card);
        card.getChildren().add(UiFactory.cardHeaderBox("Filtrar alertas asignadas", C_PUR, FA_FILTER));

        filtroAlert.buildControles();

        Button btnAplicar = accionBtn("Aplicar filtros", FA_FILTER, C_PUR,
                "-fx-background-color:" + C_PUR + ";-fx-text-fill:white;-fx-background-radius:8;-fx-padding:8 18;-fx-cursor:hand;-fx-font-weight:bold;");
        btnAplicar.setOnAction(e -> {
            filtroAlert.aplicar();
            pagAlert.reiniciar();
            renderizarAlertasAsignadas(W_ALERT);
        });

        Button btnLimpiar = accionBtn("Limpiar", FA_TIMES, C_RED,
                "-fx-background-color:" + C_RED_BG + ";-fx-text-fill:" + C_RED + ";-fx-background-radius:8;-fx-padding:8 18;-fx-cursor:hand;-fx-font-weight:bold;");
        btnLimpiar.setOnAction(e -> {
            filtroAlert.limpiar();
            pagAlert.reiniciar();
            renderizarAlertasAsignadas(W_ALERT);
        });

        HBox fila1 = new HBox(16,
                UiFactory.labeledControl(UiFactory.labelFiltro("Tipo:"), filtroAlert.cbTipo, 130),
                UiFactory.labeledControl(UiFactory.labelFiltro("Estado:"), filtroAlert.cbEstado, 130),
                UiFactory.labeledControl(UiFactory.labelFiltro("Barrio:"), filtroAlert.cbBarrio, 150),
                UiFactory.labeledControl(UiFactory.labelFiltro("Buscar:"), filtroAlert.txtBuscar, -1));
        fila1.setAlignment(Pos.CENTER_LEFT);
        fila1.setPadding(new Insets(14, 20, 10, 20));

        HBox fila2 = new HBox(16,
                UiFactory.labeledControl(UiFactory.labelFiltro("Desde:"), filtroAlert.dpDesde, 160),
                UiFactory.labeledControl(UiFactory.labelFiltro("Hasta:"), filtroAlert.dpHasta, 160),
                btnAplicar, btnLimpiar);
        fila2.setAlignment(Pos.CENTER_LEFT);
        fila2.setPadding(new Insets(0, 20, 14, 20));

        card.getChildren().addAll(fila1, fila2);
        return card;
    }

    // ══════════════════════════════════════════════════════════════
    // TABLA ALERTAS ASIGNADAS
    // ══════════════════════════════════════════════════════════════
    private VBox buildTablaAlertasAsignadas() {
        VBox card = new VBox(0);
        card.setStyle("-fx-background-color:white;-fx-background-radius:14;");
        UiFactory.shadow(card);
        card.getChildren().add(UiFactory.cardHeaderBox("Alertas Asignadas a mi Unidad", C_PUR, FA_LIST));
        card.getChildren().add(UiFactory.buildThead(
                new String[]{"#", "Tipo", "Barrio", "Estado", "Fecha y Hora", "Descripción", "Unidad", "Atendida por"}, W_ALERT));

        tbodyAlertasRef = new VBox(0);
        lblMostrandoAlertasRef = UiFactory.lbl("", 12, GRAY_TEXT, false);
        renderizarAlertasAsignadas(W_ALERT);

        card.getChildren().addAll(tbodyAlertasRef, UiFactory.buildFooter(lblMostrandoAlertasRef));
        return card;
    }

    private void renderizarAlertasAsignadas(double[] w) {
        tbodyAlertasRef.getChildren().clear();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        List<Alerta> lista = filtroAlert.getResultados();
        int total = lista.size();
        int desde = pagAlert.inicio(total);
        int hasta = pagAlert.fin(total);

        if (total == 0) {
            tbodyAlertasRef.getChildren().add(UiFactory.buildVacioState("No hay alertas asignadas que coincidan con los filtros"));
            actualizarLblAlertas();
            actualizarPaginacionAlertas(w);
            return;
        }

        String unidadNom = (policiaLogueado != null && policiaLogueado.getUnidadpolicial() != null)
                ? policiaLogueado.getUnidadpolicial().getNombre() : "—";

        for (int i = desde; i < hasta; i++) {
            Alerta al = lista.get(i);
            boolean par = i % 2 == 0;

            String tipo = al.getTipoalerta() != null ? al.getTipoalerta().getNombre() : "—";
            String barrio = al.getBarrio() != null ? al.getBarrio().getNombre() : "—";
            String estado = al.getEstado() != null ? al.getEstado().name() : "—";
            String fecha = al.getFechaHora() != null ? al.getFechaHora().format(fmt) : "—";

            final int idAl = al.getId_alerta();
            AtencionAlerta aten = atencionesCached.stream()
                    .filter(a -> a.getAlerta() != null && a.getAlerta().getId_alerta() == idAl)
                    .findFirst().orElse(null);
            String nombreAtendio = "Sin atender";
            if (aten != null && aten.getPolicia() != null) {
                Policia p = aten.getPolicia();
                nombreAtendio = p.getPrimer_nombre() + " " + p.getPrimer_apellido();
            }

            HBox fila = UiFactory.buildFilaBase(par);
            fila.setOnMouseEntered(e -> fila.setStyle(UiFactory.filaBgHover(C_PUR_BG)));
            fila.setOnMouseExited(e -> fila.setStyle(UiFactory.filaBgNormal(par)));

            // Col 1: ID badge circular
            StackPane idBadge = new StackPane();
            idBadge.getChildren().addAll(new Circle(16, Color.web(C_PUR_BG)), UiFactory.lbl(String.valueOf(al.getId_alerta()), 11, C_PUR, true));
            HBox idBox = new HBox(idBadge);
            idBox.setAlignment(Pos.CENTER_LEFT);
            idBox.setPrefWidth(w[0]);
            idBox.setMinWidth(w[0]);

            fila.getChildren().addAll(
                    idBox,
                    UiFactory.hboxCell(BadgeFactory.tipoBadge(tipo), w[1]),
                    UiFactory.celdaFija(barrio, w[2]),
                    UiFactory.hboxCell(BadgeFactory.estadoAlertaBadge(estado), w[3]),
                    UiFactory.hboxCell(UiFactory.fechaBox(fecha), w[4]),
                    UiFactory.celdaFija(al.getDescripcion(), w[5]),
                    UiFactory.celdaFija(unidadNom, w[6]),
                    buildAtendidaBadge(nombreAtendio, w[7])
            );
            tbodyAlertasRef.getChildren().add(fila);
        }
        actualizarLblAlertas();
        actualizarPaginacionAlertas(w);
    }

    private HBox buildAtendidaBadge(String nombre, double width) {
        boolean sinAtender = "Sin atender".equals(nombre);
        String color = sinAtender ? GRAY_TEXT : C_TEA;
        String bg = sinAtender ? "#f3f4f6" : C_TEA_BG;
        String icon = sinAtender ? FA_CLOCK : FA_USER;

        Label ico = UiFactory.faLabel(icon, 10, color);
        Label txt = new Label(" " + nombre);
        txt.setStyle("-fx-font-size:11px;-fx-font-weight:bold;-fx-text-fill:" + color + ";");
        txt.setTextOverrun(javafx.scene.control.OverrunStyle.ELLIPSIS);
        txt.setMaxWidth(width - 30);

        HBox badge = new HBox(3, ico, txt);
        badge.setAlignment(Pos.CENTER_LEFT);
        badge.setPadding(new Insets(3, 9, 3, 9));
        badge.setStyle("-fx-background-color:" + bg + ";-fx-background-radius:20;");

        HBox box = new HBox(badge);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPrefWidth(width);
        box.setMinWidth(width);
        box.setMaxWidth(width);
        return box;
    }

    private void actualizarLblAlertas() {
        if (lblMostrandoAlertasRef == null) {
            return;
        }
        List<Alerta> lista = filtroAlert.getResultados();
        int total = lista.size();
        int desde = pagAlert.inicio(total);
        int hasta = pagAlert.fin(total);
        lblMostrandoAlertasRef.setText("Mostrando " + (total == 0 ? 0 : desde + 1) + " – " + hasta
                + " de " + total + " alertas asignadas"
                + (total < alertasCached.size() ? " (filtradas de " + alertasCached.size() + " totales)" : ""));
    }

    // ══════════════════════════════════════════════════════════════
    // PAGINACIÓN — Controller (PaginadorHelper) + UI local
    // ══════════════════════════════════════════════════════════════
    private HBox buildPaginacionAtenc() {
        paginacionAtencRef = new HBox(6);
        paginacionAtencRef.setAlignment(Pos.CENTER_RIGHT);
        paginacionAtencRef.setPadding(new Insets(4, 0, 0, 0));
        return paginacionAtencRef;
    }

    private void actualizarPaginacionAtenc(double[] w) {
        if (paginacionAtencRef == null) {
            return;
        }
        paginacionAtencRef.getChildren().clear();
        int totalPags = pagAtenc.totalPaginas(filtroAtenc.getResultados().size());
        if (totalPags <= 1) {
            return;
        }

        paginacionAtencRef.getChildren().add(btnPagNav("\uf104", pagAtenc.getPagina() > 1, () -> {
            pagAtenc.retroceder();
            renderizarAtenciones(w);
        }, C_BLU));

        agregarBotonesPag(paginacionAtencRef, pagAtenc.getPagina(), totalPags, w, true);

        paginacionAtencRef.getChildren().add(btnPagNav("\uf105", pagAtenc.getPagina() < totalPags, () -> {
            pagAtenc.avanzar();
            renderizarAtenciones(w);
        }, C_BLU));
    }

    private HBox buildPaginacionAlertas() {
        paginacionAlertasRef = new HBox(6);
        paginacionAlertasRef.setAlignment(Pos.CENTER_RIGHT);
        paginacionAlertasRef.setPadding(new Insets(4, 0, 0, 0));
        return paginacionAlertasRef;
    }

    private void actualizarPaginacionAlertas(double[] w) {
        if (paginacionAlertasRef == null) {
            return;
        }
        paginacionAlertasRef.getChildren().clear();
        int totalPags = pagAlert.totalPaginas(filtroAlert.getResultados().size());
        if (totalPags <= 1) {
            return;
        }

        paginacionAlertasRef.getChildren().add(btnPagNav("\uf104", pagAlert.getPagina() > 1, () -> {
            pagAlert.retroceder();
            renderizarAlertasAsignadas(w);
        }, C_PUR));

        agregarBotonesPag(paginacionAlertasRef, pagAlert.getPagina(), totalPags, w, false);

        paginacionAlertasRef.getChildren().add(btnPagNav("\uf105", pagAlert.getPagina() < totalPags, () -> {
            pagAlert.avanzar();
            renderizarAlertasAsignadas(w);
        }, C_PUR));
    }

    private void agregarBotonesPag(HBox container, int actual, int totalPags, double[] w, boolean esAtenc) {
        int ini = Math.max(1, actual - 2);
        int fin = Math.min(totalPags, actual + 2);
        if (ini > 1) {
            container.getChildren().add(btnPagNum(1, w, esAtenc));
            if (ini > 2) {
                container.getChildren().add(UiFactory.puntos());
            }
        }
        for (int i = ini; i <= fin; i++) {
            container.getChildren().add(btnPagNum(i, w, esAtenc));
        }
        if (fin < totalPags) {
            if (fin < totalPags - 1) {
                container.getChildren().add(UiFactory.puntos());
            }
            container.getChildren().add(btnPagNum(totalPags, w, esAtenc));
        }
    }

    private Button btnPagNum(int pg, double[] w, boolean esAtenc) {
        PaginadorHelper pag = esAtenc ? pagAtenc : pagAlert;
        boolean esActual = pg == pag.getPagina();
        String accent = esAtenc ? C_BLU : C_PUR;
        String accentBg = esAtenc ? C_BLU_BG : C_PUR_BG;
        Button b = new Button(String.valueOf(pg));
        b.setPrefSize(34, 34);
        b.setMinSize(34, 34);
        b.setMaxSize(34, 34);
        b.setFont(Font.font("System", esActual ? FontWeight.BOLD : FontWeight.NORMAL, 13));
        String base = esActual
                ? "-fx-background-color:" + accent + ";-fx-text-fill:white;-fx-background-radius:8;-fx-cursor:hand;-fx-border-color:" + accent + ";-fx-border-radius:8;-fx-border-width:1.5;"
                : "-fx-background-color:" + WHITE + ";-fx-text-fill:#374151;-fx-background-radius:8;-fx-cursor:hand;-fx-border-color:" + BORDER + ";-fx-border-radius:8;-fx-border-width:1.5;";
        String hover = "-fx-background-color:" + accentBg + ";-fx-text-fill:" + accent + ";-fx-background-radius:8;-fx-cursor:hand;-fx-border-color:" + accent + ";-fx-border-radius:8;-fx-border-width:1.5;";
        b.setStyle(base);
        if (!esActual) {
            b.setOnMouseEntered(e -> b.setStyle(hover));
            b.setOnMouseExited(e -> b.setStyle(base));
        }
        b.setOnAction(e -> {
            pag.irA(pg);
            if (esAtenc) {
                renderizarAtenciones(w);
            } else {
                renderizarAlertasAsignadas(w);
            }
        });
        return b;
    }

    private Button btnPagNav(String ico, boolean enabled, Runnable accion, String accent) {
        Label l = new Label(ico);
        l.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';-fx-font-size:13px;-fx-text-fill:" + (enabled ? "#374151" : "#d1d5db") + ";");
        Button b = new Button();
        b.setGraphic(l);
        b.setPrefSize(34, 34);
        b.setMinSize(34, 34);
        b.setMaxSize(34, 34);
        b.setDisable(!enabled);
        String accentBg = accent.equals(C_BLU) ? C_BLU_BG : C_PUR_BG;
        String base = "-fx-background-color:" + WHITE + ";-fx-background-radius:8;-fx-border-color:" + BORDER + ";-fx-border-radius:8;-fx-border-width:1.5;";
        String hover = "-fx-background-color:" + accentBg + ";-fx-background-radius:8;-fx-border-color:" + accent + ";-fx-border-radius:8;-fx-border-width:1.5;";
        b.setStyle(base);
        if (enabled) {
            b.setOnMouseEntered(e -> b.setStyle(hover));
            b.setOnMouseExited(e -> b.setStyle(base));
        }
        b.setOnAction(e -> accion.run());
        return b;
    }

    // ══════════════════════════════════════════════════════════════
    // HELPERS AUXILIARES DE LA VISTA
    // ══════════════════════════════════════════════════════════════
    private Button accionBtn(String texto, String icon, String color, String style) {
        Button b = new Button();
        boolean dark = color.equals(C_BLU) || color.equals(C_PUR);
        Label ico = UiFactory.faLabel(icon, 12, dark ? WHITE : color);
        Label txt = new Label("  " + texto);
        txt.setStyle("-fx-text-fill:" + (dark ? "white" : color) + ";-fx-font-size:12px;-fx-font-weight:bold;");
        HBox content = new HBox(4, ico, txt);
        content.setAlignment(Pos.CENTER);
        b.setGraphic(content);
        b.setStyle(style);
        VBox.setMargin(b, new Insets(18, 0, 0, 0));
        return b;
    }

    private HBox buildErrorState(String message) {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(14, 20, 14, 20));
        box.setStyle("-fx-background-color:" + C_RED_BG + ";-fx-background-radius:10;");
        Label icon = UiFactory.faLabel("\uf071", 16, C_RED);
        Label msg = new Label("Error al cargar datos: " + message);
        msg.setFont(Font.font("System", 12));
        msg.setTextFill(Color.web(C_RED));
        msg.setWrapText(true);
        box.getChildren().addAll(icon, msg);
        return box;
    }

    // ══════════════════════════════════════════════════════════════
    // ═══════════  CLASES INTERNAS GRASP  ═══════════════════════════
    // ══════════════════════════════════════════════════════════════
    // ─────────────────────────────────────────────────────────────
    // INFORMATION EXPERT
    // Conoce las listas y calcula todas las métricas derivadas de ellas.
    // ─────────────────────────────────────────────────────────────
    static class ReporteEstadisticasHelper {

        private final List<AtencionAlerta> atenciones;
        private final List<AsignacionUnidad> asignaciones;
        private final List<Alerta> alertas;

        ReporteEstadisticasHelper(List<AtencionAlerta> atenciones,
                List<AsignacionUnidad> asignaciones,
                List<Alerta> alertas) {
            this.atenciones = atenciones;
            this.asignaciones = asignaciones;
            this.alertas = alertas;
        }

        long totalAtenciones() {
            return atenciones.size();
        }

        long totalAlertas() {
            return alertas.size();
        }

        long totalAsignaciones() {
            return asignaciones.size();
        }

        long finalizadas() {
            return contar(EstadoAtencionAlerta.FINALIZADA);
        }

        long enProceso() {
            return contar(EstadoAtencionAlerta.EN_PROCESO);
        }

        long pendientes() {
            return contar(EstadoAtencionAlerta.PENDIENTE);
        }

        long canceladas() {
            return contar(EstadoAtencionAlerta.CANCELADA);
        }

        private long contar(EstadoAtencionAlerta estado) {
            return atenciones.stream().filter(a -> a.getEstado() == estado).count();
        }

        String tipoAlertaMasFrecuente() {
            return alertas.stream()
                    .filter(a -> a.getTipoalerta() != null)
                    .collect(Collectors.groupingBy(a -> a.getTipoalerta().getNombre(), Collectors.counting()))
                    .entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("—");
        }
    }

    // ─────────────────────────────────────────────────────────────
    // CONTROLLER  (también Pure Fabrication para la paginación)
    // Encapsula el estado de la página actual y las operaciones sobre ella.
    // ─────────────────────────────────────────────────────────────
    static class PaginadorHelper {

        private final int filasPorPagina;
        private final Runnable onCambio;
        private int pagina = 1;

        PaginadorHelper(int filasPorPagina, Runnable onCambio) {
            this.filasPorPagina = filasPorPagina;
            this.onCambio = onCambio;
        }

        int getPagina() {
            return pagina;
        }

        void reiniciar() {
            pagina = 1;
        }

        void irA(int p) {
            pagina = p;
        }

        void avanzar() {
            pagina++;
            onCambio.run();
        }

        void retroceder() {
            pagina--;
            onCambio.run();
        }

        int totalPaginas(int total) {
            return (int) Math.ceil((double) total / filasPorPagina);
        }

        int inicio(int total) {
            return Math.max(0, (pagina - 1) * filasPorPagina);
        }

        int fin(int total) {
            return Math.min(inicio(total) + filasPorPagina, total);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // LOW COUPLING / HIGH COHESION
    // FiltroAtenciones: sólo conoce la lista de atenciones y sus criterios de filtrado.
    // ─────────────────────────────────────────────────────────────
    static class FiltroAtenciones {

        private final List<AtencionAlerta> original;
        private List<AtencionAlerta> resultados;

        ComboBox<String> cbEstado;
        TextField txtBuscar;
        DatePicker dpDesde;
        DatePicker dpHasta;

        FiltroAtenciones(List<AtencionAlerta> original) {
            this.original = original;
            this.resultados = original;
        }

        void buildControles() {
            cbEstado = UiFactory.comboFiltro("Todos los estados");
            cbEstado.getItems().addAll("PENDIENTE", "EN_PROCESO", "FINALIZADA", "CANCELADA");
            txtBuscar = new TextField();
            txtBuscar.setPromptText("Buscar en descripción u observación…");
            txtBuscar.setStyle(UiFactory.estiloInput());
            HBox.setHgrow(txtBuscar, Priority.ALWAYS);
            dpDesde = UiFactory.datePicker("Desde…");
            dpHasta = UiFactory.datePicker("Hasta…");
        }

        void aplicar() {
            String estado = cbEstado.getValue();
            String buscar = txtBuscar.getText() != null ? txtBuscar.getText().trim().toLowerCase() : "";
            LocalDate desde = dpDesde.getValue();
            LocalDate hasta = dpHasta.getValue();

            resultados = original.stream()
                    .filter(a -> estado == null || (a.getEstado() != null && estado.equals(a.getEstado().name())))
                    .filter(a -> buscar.isEmpty()
                    || (a.getDescripcion() != null && a.getDescripcion().toLowerCase().contains(buscar))
                    || (a.getObservacion() != null && a.getObservacion().toLowerCase().contains(buscar)))
                    .filter(a -> enRango(a.getFechaatencion(), desde, hasta))
                    .collect(Collectors.toList());
        }

        void limpiar() {
            cbEstado.setValue(null);
            txtBuscar.clear();
            dpDesde.setValue(null);
            dpHasta.setValue(null);
            resultados = original;
        }

        List<AtencionAlerta> getResultados() {
            return resultados;
        }

        private boolean enRango(LocalDateTime fh, LocalDate desde, LocalDate hasta) {
            if (desde == null && hasta == null) {
                return true;
            }
            if (fh == null) {
                return false;
            }
            LocalDate fecha = fh.toLocalDate();
            if (desde != null && fecha.isBefore(desde)) {
                return false;
            }
            if (hasta != null && fecha.isAfter(hasta)) {
                return false;
            }
            return true;
        }
    }

    // ─────────────────────────────────────────────────────────────
    // LOW COUPLING / HIGH COHESION
    // FiltroAlertas: sólo conoce la lista de alertas y sus criterios de filtrado.
    // ─────────────────────────────────────────────────────────────
    static class FiltroAlertas {

        private final List<Alerta> original;
        private List<Alerta> resultados;

        ComboBox<String> cbTipo;
        ComboBox<String> cbEstado;
        ComboBox<String> cbBarrio;
        TextField txtBuscar;
        DatePicker dpDesde;
        DatePicker dpHasta;

        FiltroAlertas(List<Alerta> original) {
            this.original = original;
            this.resultados = original;
        }

        void buildControles() {
            cbTipo = UiFactory.comboFiltro("Todos los tipos");
            cbEstado = UiFactory.comboFiltro("Todos los estados");
            cbBarrio = UiFactory.comboFiltro("Todos los barrios");

           
            original.stream()
                    .filter(a -> a.getTipoalerta() != null)
                    .map(a -> a.getTipoalerta().getNombre())
                    .distinct().sorted()
                    .forEach(t -> cbTipo.getItems().add(t));

            original.stream()
                    .filter(a -> a.getEstado() != null)
                    .map(a -> a.getEstado().name())
                    .distinct().sorted()
                    .forEach(s -> cbEstado.getItems().add(s));

            original.stream()
                    .filter(a -> a.getBarrio() != null)
                    .map(a -> a.getBarrio().getNombre())
                    .distinct().sorted()
                    .forEach(b -> cbBarrio.getItems().add(b));

            txtBuscar = new TextField();
            txtBuscar.setPromptText("Buscar en descripción…");
            txtBuscar.setStyle(UiFactory.estiloInput());
            HBox.setHgrow(txtBuscar, Priority.ALWAYS);
            dpDesde = UiFactory.datePicker("Desde…");
            dpHasta = UiFactory.datePicker("Hasta…");
        }

        void aplicar() {
            String tipo = cbTipo.getValue();
            String estado = cbEstado.getValue();
            String barrio = cbBarrio.getValue();
            String buscar = txtBuscar.getText() != null ? txtBuscar.getText().trim().toLowerCase() : "";
            LocalDate desde = dpDesde.getValue();
            LocalDate hasta = dpHasta.getValue();

            resultados = original.stream()
                    .filter(a -> tipo == null || (a.getTipoalerta() != null && tipo.equals(a.getTipoalerta().getNombre())))
                    .filter(a -> estado == null || (a.getEstado() != null && estado.equals(a.getEstado().name())))
                    .filter(a -> barrio == null || (a.getBarrio() != null && barrio.equals(a.getBarrio().getNombre())))
                    .filter(a -> buscar.isEmpty() || (a.getDescripcion() != null && a.getDescripcion().toLowerCase().contains(buscar)))
                    .filter(a -> enRango(a.getFechaHora(), desde, hasta))
                    .collect(Collectors.toList());
        }

        void limpiar() {
            cbTipo.setValue(null);
            cbEstado.setValue(null);
            cbBarrio.setValue(null);
            txtBuscar.clear();
            dpDesde.setValue(null);
            dpHasta.setValue(null);
            resultados = original;
        }

        List<Alerta> getResultados() {
            return resultados;
        }

        private boolean enRango(LocalDateTime fh, LocalDate desde, LocalDate hasta) {
            if (desde == null && hasta == null) {
                return true;
            }
            if (fh == null) {
                return false;
            }
            LocalDate fecha = fh.toLocalDate();
            if (desde != null && fecha.isBefore(desde)) {
                return false;
            }
            if (hasta != null && fecha.isAfter(hasta)) {
                return false;
            }
            return true;
        }
    }

    // ─────────────────────────────────────────────────────────────
    // PURE FABRICATION
    // UiFactory: clase inventada sin correspondencia en el dominio,
    // agrupa utilidades de construcción de nodos JavaFX con alta cohesión.
    // ─────────────────────────────────────────────────────────────
    static class UiFactory {

        static Label faLabel(String unicode, double size, String color) {
            Label l = new Label(unicode);
            l.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';-fx-font-size:" + size + "px;-fx-text-fill:" + color + ";");
            return l;
        }

        static StackPane faIconBox(String unicode, double iconSize, String iconColor, String bgColor, double boxSize) {
            StackPane box = new StackPane();
            box.setPrefSize(boxSize, boxSize);
            box.setMinSize(boxSize, boxSize);
            box.setMaxSize(boxSize, boxSize);
            Rectangle bg = new Rectangle(boxSize, boxSize);
            bg.setArcWidth(14);
            bg.setArcHeight(14);
            bg.setFill(Color.web(bgColor));
            box.getChildren().addAll(bg, faLabel(unicode, iconSize, iconColor));
            return box;
        }

        static HBox cardHeaderBox(String title, String accentColor, String faIcon) {
            HBox header = new HBox(10);
            header.setAlignment(Pos.CENTER_LEFT);
            header.setPadding(new Insets(14, 20, 12, 20));
            header.setStyle("-fx-border-color:transparent transparent " + BORDER + " transparent;-fx-border-width:0 0 1 0;");
            Rectangle accent = new Rectangle(4, 20);
            accent.setArcWidth(4);
            accent.setArcHeight(4);
            accent.setFill(Color.web(accentColor));
            Label titleLbl = new Label(title);
            titleLbl.setFont(Font.font("System", FontWeight.BOLD, 14));
            titleLbl.setTextFill(Color.web("#111827"));
            header.getChildren().addAll(accent, faLabel(faIcon, 15, accentColor), titleLbl);
            return header;
        }

        static Label seccionLabel(String text) {
            Label l = new Label(text.toUpperCase());
            l.setFont(Font.font("System", FontWeight.BOLD, 10));
            l.setTextFill(Color.web(GRAY_TEXT));
            l.setPadding(new Insets(6, 0, 0, 2));
            return l;
        }

        static HBox buildThead(String[] cols, double[] widths) {
            HBox thead = new HBox(0);
            thead.setPadding(new Insets(11, 16, 11, 16));
            thead.setStyle("-fx-background-color:#f8fafc;-fx-border-color:transparent transparent " + BORDER + " transparent;-fx-border-width:0 0 1 0;");
            for (int i = 0; i < cols.length; i++) {
                Label h = new Label(cols[i].toUpperCase());
                h.setStyle("-fx-font-size:10px;-fx-font-weight:bold;-fx-text-fill:#9ca3af;");
                h.setPrefWidth(widths[i]);
                h.setMinWidth(widths[i]);
                thead.getChildren().add(h);
            }
            return thead;
        }

        static HBox buildFooter(Label lblMostrando) {
            HBox footer = new HBox();
            footer.setPadding(new Insets(11, 16, 11, 16));
            footer.setStyle("-fx-border-color:" + BORDER + " transparent transparent transparent;-fx-border-width:1 0 0 0;");
            footer.getChildren().add(lblMostrando);
            return footer;
        }

        static HBox buildFilaBase(boolean par) {
            HBox fila = new HBox(0);
            fila.setAlignment(Pos.CENTER_LEFT);
            fila.setPadding(new Insets(10, 16, 10, 16));
            fila.setStyle(filaBgNormal(par));
            return fila;
        }

        static String filaBgNormal(boolean par) {
            return "-fx-background-color:" + (par ? WHITE : "#fafbfd") + ";-fx-border-color:transparent transparent " + BORDER + " transparent;-fx-border-width:0 0 1 0;";
        }

        static String filaBgHover(String accentBg) {
            return "-fx-background-color:" + accentBg + ";-fx-border-color:transparent transparent " + BORDER + " transparent;-fx-border-width:0 0 1 0;-fx-cursor:hand;";
        }

        static VBox buildVacioState(String mensaje) {
            VBox vacio = new VBox(10);
            vacio.setAlignment(Pos.CENTER);
            vacio.setPadding(new Insets(40));
            vacio.getChildren().addAll(faLabel(FA_SEARCH, 32, GRAY_TEXT), lbl(mensaje, 14, GRAY_TEXT, false));
            return vacio;
        }

        static Label celdaFija(String txt, double width) {
            Label l = new Label(txt != null ? txt : "—");
            l.setStyle("-fx-font-size:12px;-fx-text-fill:#374151;");
            l.setPrefWidth(width);
            l.setMinWidth(width);
            l.setMaxWidth(width);
            l.setTextOverrun(javafx.scene.control.OverrunStyle.ELLIPSIS);
            return l;
        }

        static HBox hboxCell(javafx.scene.Node node, double width) {
            HBox box = new HBox(node);
            box.setAlignment(Pos.CENTER_LEFT);
            box.setPrefWidth(width);
            box.setMinWidth(width);
            box.setMaxWidth(width);
            return box;
        }

        static HBox buildNumBadge(int num, String bg, String fg, double width) {
            StackPane sp = new StackPane();
            sp.getChildren().addAll(new Circle(16, Color.web(bg)), lbl(String.valueOf(num), 11, fg, true));
            HBox box = new HBox(sp);
            box.setAlignment(Pos.CENTER_LEFT);
            box.setPrefWidth(width);
            box.setMinWidth(width);
            return box;
        }

        static HBox fechaBox(String fecha) {
            Label ico = faLabel(FA_CLOCK, 11, GRAY_TEXT);
            Label txt = new Label(" " + fecha);
            txt.setStyle("-fx-font-size:11px;-fx-text-fill:#374151;");
            HBox box = new HBox(3, ico, txt);
            box.setAlignment(Pos.CENTER_LEFT);
            return box;
        }

        static Label lbl(String text, double size, String color, boolean bold) {
            Label l = new Label(text);
            l.setFont(bold ? Font.font("System", FontWeight.BOLD, size) : Font.font("System", size));
            l.setTextFill(Color.web(color));
            return l;
        }

        static void shadow(Region node) {
            node.setEffect(new DropShadow(12, 0, 2, Color.web("#0000001a")));
        }

        static Region divider() {
            Region r = new Region();
            r.setPrefHeight(1);
            r.setStyle("-fx-background-color:" + BORDER + ";");
            return r;
        }

        static Label puntos() {
            Label l = new Label("…");
            l.setStyle("-fx-font-size:14px;-fx-text-fill:" + GRAY_TEXT + ";");
            l.setPadding(new Insets(0, 4, 0, 4));
            return l;
        }

        static VBox labeledControl(Label labelNode, javafx.scene.Node control, double prefW) {
            VBox vb = new VBox(4, labelNode, control);
            vb.setAlignment(Pos.TOP_LEFT);
            if (prefW > 0 && control instanceof Region r) {
                r.setPrefWidth(prefW);
                r.setMinWidth(prefW);
            }
            return vb;
        }

        static Label labelFiltro(String text) {
            Label l = new Label(text);
            l.setStyle("-fx-font-size:11px;-fx-text-fill:" + GRAY_TEXT + ";-fx-font-weight:bold;");
            return l;
        }

        static ComboBox<String> comboFiltro(String placeholder) {
            ComboBox<String> cb = new ComboBox<>();
            cb.setPromptText(placeholder);
            cb.getItems().add(null);
            cb.setStyle(estiloInput());
            cb.setMaxWidth(Double.MAX_VALUE);
            return cb;
        }

        static DatePicker datePicker(String prompt) {
            DatePicker dp = new DatePicker();
            dp.setPromptText(prompt);
            String style = "-fx-background-color:white;-fx-background-radius:10;-fx-border-radius:10;-fx-border-color:#d1d5db;-fx-border-width:1;-fx-padding:6 10;-fx-font-size:12px;";
            dp.setStyle(style);
            dp.getEditor().setStyle("-fx-background-color:transparent;-fx-text-fill:#111827;-fx-font-size:12px;");
            dp.setOnMouseEntered(e -> dp.setStyle(style + "-fx-border-color:" + C_BLU + ";"));
            dp.setOnMouseExited(e -> dp.setStyle(style));
            dp.setPrefHeight(36);
            dp.setMaxWidth(Double.MAX_VALUE);
            return dp;
        }

        static String estiloInput() {
            return "-fx-background-color:white;-fx-border-color:" + BORDER + ";-fx-border-radius:8;-fx-background-radius:8;-fx-padding:6 10;-fx-font-size:12px;";
        }
    }

    // ─────────────────────────────────────────────────────────────
    // POLYMORPHISM
    // BadgeFactory: centraliza la variación de colores/iconos por tipo o estado,
    // eliminando los switch/case duplicados en la vista principal.
    // ─────────────────────────────────────────────────────────────
    static class BadgeFactory {

        static HBox estadoAtencBadge(String estado) {
            String color, bg, icon;
            switch (estado.toUpperCase()) {
                case "FINALIZADA" -> {
                    color = C_GRN;
                    bg = C_GRN_BG;
                    icon = FA_CHECK;
                }
                case "EN_PROCESO" -> {
                    color = C_ORG;
                    bg = C_ORG_BG;
                    icon = FA_SPINNER;
                }
                case "CANCELADA" -> {
                    color = C_RED;
                    bg = C_RED_BG;
                    icon = FA_BAN;
                }
                default -> {
                    color = C_AMB;
                    bg = C_AMB_BG;
                    icon = FA_CLOCK;
                }
            }
            return badge(icon, estado.replace("_", " "), color, bg);
        }

        static HBox estadoAlertaBadge(String estado) {
            String color = colorForEstadoAlerta(estado);
            String bg = bgForEstadoAlerta(estado);
            String icon = iconForEstadoAlerta(estado);
            return badge(icon, estado, color, bg);
        }

        static HBox tipoBadge(String tipo) {
            String icon = iconForTipo(tipo);
            return badge(icon, tipo, C_RED, C_RED_BG);
        }

        private static HBox badge(String faIcon, String texto, String color, String bg) {
            Label ico = UiFactory.faLabel(faIcon, 11, color);
            Label txt = new Label(" " + texto);
            txt.setStyle("-fx-font-size:11px;-fx-font-weight:bold;-fx-text-fill:" + color + ";");
            HBox box = new HBox(2, ico, txt);
            box.setAlignment(Pos.CENTER_LEFT);
            box.setPadding(new Insets(3, 9, 3, 9));
            box.setStyle("-fx-background-color:" + bg + ";-fx-background-radius:20;");
            return box;
        }

        private static String colorForEstadoAlerta(String estado) {
            if (estado == null) {
                return GRAY_TEXT;
            }
            return switch (estado.toUpperCase()) {
                case "ACTIVO", "ACTIVA" ->
                    C_GRN;
                case "PENDIENTE" ->
                    C_AMB;
                case "ATENDIDA", "RESUELTA" ->
                    C_BLU;
                case "CANCELADA", "RECHAZADA" ->
                    C_RED;
                default ->
                    GRAY_TEXT;
            };
        }

        private static String bgForEstadoAlerta(String estado) {
            if (estado == null) {
                return "#f3f4f6";
            }
            return switch (estado.toUpperCase()) {
                case "ACTIVO", "ACTIVA" ->
                    C_GRN_BG;
                case "PENDIENTE" ->
                    C_AMB_BG;
                case "ATENDIDA", "RESUELTA" ->
                    C_BLU_BG;
                case "CANCELADA", "RECHAZADA" ->
                    C_RED_BG;
                default ->
                    "#f3f4f6";
            };
        }

        private static String iconForEstadoAlerta(String estado) {
            if (estado == null) {
                return "\uf111";
            }
            return switch (estado.toUpperCase()) {
                case "ACTIVO", "ACTIVA" ->
                    "\uf058";
                case "PENDIENTE" ->
                    FA_CLOCK;
                case "ATENDIDA", "RESUELTA" ->
                    FA_CHECK;
                case "CANCELADA", "RECHAZADA" ->
                    "\uf057";
                default ->
                    "\uf111";
            };
        }

        private static String iconForTipo(String tipo) {
            if (tipo == null) {
                return FA_BELL;
            }
            return switch (tipo.toUpperCase()) {
                case "ROBO", "HURTO" ->
                    FA_SHIELD;
                case "ACCIDENTE" ->
                    "\uf071";
                case "INCENDIO" ->
                    "\uf46a";
                case "ANIMAL" ->
                    "\uf6d3";
                case "VANDALISMO" ->
                    "\uf6e3";
                case "SOSPECHOSO", "PERSONA" ->
                    FA_USER;
                default ->
                    FA_BELL;
            };
        }
    }

    // ─────────────────────────────────────────────────────────────
    // CREATOR
    // ExcelExportHelper es quien tiene la información necesaria para
    // crear archivos Excel de atenciones y alertas.
    // También delega los diálogos a DialogHelper (Creator de ventanas).
    // ─────────────────────────────────────────────────────────────
    static class ExcelExportHelper {

        private final Policia policiaLogueado;
        private final List<AtencionAlerta> atencionesCached;

        ExcelExportHelper(Policia policiaLogueado, List<AtencionAlerta> atencionesCached) {
            this.policiaLogueado = policiaLogueado;
            this.atencionesCached = atencionesCached;
        }

        void exportarAtenciones(List<AtencionAlerta> filtradas, javafx.scene.Node owner) {
            File file = elegirArchivo(owner, "Guardar reporte de atenciones", "Reporte_MisAtenciones_WolertApp.xlsx");
            if (file == null) {
                return;
            }
            try (XSSFWorkbook wb = new XSSFWorkbook()) {
                Sheet sheet = wb.createSheet("MisAtenciones");
                CellStyle hStyle = headerStyle(wb);
                CellStyle nStyle = normalStyle(wb);

                String[] headers = {"#", "ID Alerta", "Estado", "Fecha Atención",
                    "Descripción", "Tipo Arma", "Medio Transporte", "Observación", "Unidad"};
                writeRow(sheet.createRow(0), headers, hStyle);

                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                int rn = 1;
                for (AtencionAlerta at : filtradas) {
                    writeRow(sheet.createRow(rn++), new String[]{
                        String.valueOf(rn - 1),
                        at.getAlerta() != null ? String.valueOf(at.getAlerta().getId_alerta()) : "",
                        at.getEstado() != null ? at.getEstado().name() : "",
                        at.getFechaatencion() != null ? at.getFechaatencion().format(fmt) : "",
                        nvl(at.getDescripcion()),
                        at.getTipoarma() != null ? at.getTipoarma().getNombre() : "",
                        at.getMediotransporte() != null ? at.getMediotransporte().getNombre() : "",
                        nvl(at.getObservacion()),
                        at.getUnidad() != null ? at.getUnidad().getNombre() : ""
                    }, nStyle);
                }
                autoSize(sheet, headers.length);
                write(wb, file);
                DialogHelper.exito("Reporte de Atenciones exportado", file.getName(), file.getAbsolutePath());
            } catch (Exception ex) {
                DialogHelper.error(ex.getMessage());
            }
        }

        void exportarAlertasAsignadas(List<Alerta> filtradas, javafx.scene.Node owner) {
            File file = elegirArchivo(owner, "Guardar reporte de alertas asignadas", "Reporte_MisAlertasAsignadas_WolertApp.xlsx");
            if (file == null) {
                return;
            }
            try (XSSFWorkbook wb = new XSSFWorkbook()) {
                Sheet sheet = wb.createSheet("AlertasAsignadas");
                CellStyle hStyle = headerStyle(wb);
                CellStyle nStyle = normalStyle(wb);

                String[] headers = {"#", "ID Alerta", "Tipo", "Barrio", "Estado",
                    "Fecha y Hora", "Descripción", "Latitud", "Longitud", "Unidad Asignada", "Atendida por"};
                writeRow(sheet.createRow(0), headers, hStyle);

                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                String unidadNom = (policiaLogueado != null && policiaLogueado.getUnidadpolicial() != null)
                        ? policiaLogueado.getUnidadpolicial().getNombre() : "";
                int rn = 1;
                for (Alerta al : filtradas) {
                    final int idAl = al.getId_alerta();
                    AtencionAlerta aten = atencionesCached.stream()
                            .filter(a -> a.getAlerta() != null && a.getAlerta().getId_alerta() == idAl)
                            .findFirst().orElse(null);
                    String polNombre = "Sin atender";
                    if (aten != null && aten.getPolicia() != null) {
                        Policia p = aten.getPolicia();
                        polNombre = p.getPrimer_nombre() + " " + p.getPrimer_apellido();
                    }
                    writeRow(sheet.createRow(rn++), new String[]{
                        String.valueOf(rn - 1),
                        String.valueOf(al.getId_alerta()),
                        al.getTipoalerta() != null ? al.getTipoalerta().getNombre() : "",
                        al.getBarrio() != null ? al.getBarrio().getNombre() : "",
                        al.getEstado() != null ? al.getEstado().name() : "",
                        al.getFechaHora() != null ? al.getFechaHora().format(fmt) : "",
                        nvl(al.getDescripcion()),
                        String.valueOf(al.getLatitud()),
                        String.valueOf(al.getLongitud()),
                        unidadNom, polNombre
                    }, nStyle);
                }
                autoSize(sheet, headers.length);
                write(wb, file);
                DialogHelper.exito("Reporte de Alertas Asignadas exportado", file.getName(), file.getAbsolutePath());
            } catch (Exception ex) {
                DialogHelper.error(ex.getMessage());
            }
        }

        private File elegirArchivo(javafx.scene.Node owner, String titulo, String nombreDefault) {
            FileChooser fc = new FileChooser();
            fc.setTitle(titulo);
            fc.setInitialFileName(nombreDefault);
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel", "*.xlsx"));
            return fc.showSaveDialog(owner.getScene().getWindow());
        }

        private void writeRow(Row row, String[] vals, CellStyle style) {
            for (int i = 0; i < vals.length; i++) {
                Cell c = row.createCell(i);
                c.setCellValue(vals[i]);
                c.setCellStyle(style);
            }
        }

        private void autoSize(Sheet sheet, int cols) {
            for (int i = 0; i < cols; i++) {
                sheet.autoSizeColumn(i);
            }
        }

        private void write(XSSFWorkbook wb, File file) throws Exception {
            try (FileOutputStream fos = new FileOutputStream(file)) {
                wb.write(fos);
            }
        }

        private CellStyle headerStyle(XSSFWorkbook wb) {
            CellStyle s = wb.createCellStyle();
            s.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            s.setBorderBottom(BorderStyle.THIN);
            s.setAlignment(HorizontalAlignment.CENTER);
            org.apache.poi.ss.usermodel.Font f = wb.createFont();
            f.setColor(IndexedColors.WHITE.getIndex());
            f.setBold(true);
            f.setFontName("Arial");
            f.setFontHeightInPoints((short) 11);
            s.setFont(f);
            return s;
        }

        private CellStyle normalStyle(XSSFWorkbook wb) {
            CellStyle s = wb.createCellStyle();
            s.setBorderBottom(BorderStyle.THIN);
            s.setBorderLeft(BorderStyle.THIN);
            s.setBorderRight(BorderStyle.THIN);
            org.apache.poi.ss.usermodel.Font f = wb.createFont();
            f.setFontName("Arial");
            f.setFontHeightInPoints((short) 10);
            s.setFont(f);
            return s;
        }

        private String nvl(String s) {
            return s != null ? s : "";
        }
    }

    // ─────────────────────────────────────────────────────────────
    // CREATOR  (Pure Fabrication)
    // DialogHelper: sabe crear y mostrar ventanas modales de éxito/error.
    // ─────────────────────────────────────────────────────────────
    static class DialogHelper {

        static void exito(String titulo, String nombreArchivo, String rutaCompleta) {
            javafx.stage.Stage dialog = new javafx.stage.Stage();
            dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dialog.setTitle("Exportación exitosa");
            dialog.setResizable(false);

            Circle circulo = new Circle(34);
            circulo.setFill(Color.web(C_GRN_BG));
            circulo.setStroke(Color.web(C_GRN));
            circulo.setStrokeWidth(2.5);
            StackPane iconoCheck = new StackPane(circulo, UiFactory.faLabel(FA_CHECK, 24, C_GRN));

            Label lblTitulo = new Label(titulo);
            lblTitulo.setFont(Font.font("System", FontWeight.BOLD, 17));
            lblTitulo.setTextFill(Color.web("#111827"));
            Label lblSub = UiFactory.lbl("El archivo fue guardado correctamente.", 12, GRAY_TEXT, false);

            Label lblNombre = new Label(nombreArchivo);
            lblNombre.setFont(Font.font("System", FontWeight.BOLD, 13));
            lblNombre.setTextFill(Color.web("#111827"));
            Label lblRuta = new Label(rutaCompleta);
            lblRuta.setStyle("-fx-font-size:10px;-fx-text-fill:" + GRAY_TEXT + ";");
            lblRuta.setWrapText(true);
            lblRuta.setMaxWidth(360);

            HBox archivoCard = new HBox(12, UiFactory.faLabel(FA_DOWNLOAD, 16, C_GRN), new VBox(3, lblNombre, lblRuta));
            archivoCard.setAlignment(Pos.CENTER_LEFT);
            archivoCard.setPadding(new Insets(12, 16, 12, 16));
            archivoCard.setStyle("-fx-background-color:" + C_GRN_BG + ";-fx-background-radius:10;-fx-border-color:" + C_GRN + ";-fx-border-radius:10;-fx-border-width:1;");
            archivoCard.setMaxWidth(400);

            Button btnCerrar = buildDialogBtn("Listo", C_GRN, "#388e3c");
            btnCerrar.setOnAction(e -> dialog.close());

            VBox body = buildDialogBody(iconoCheck, lblTitulo, lblSub, archivoCard, btnCerrar);
            javafx.scene.Scene scene = new javafx.scene.Scene(wrapDialog(body), 480, 340);
            dialog.setScene(scene);
            dialog.showAndWait();
        }

        static void error(String mensaje) {
            javafx.stage.Stage dialog = new javafx.stage.Stage();
            dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dialog.setTitle("Error al exportar");
            dialog.setResizable(false);

            Circle circulo = new Circle(34);
            circulo.setFill(Color.web(C_RED_BG));
            circulo.setStroke(Color.web(C_RED));
            circulo.setStrokeWidth(2.5);
            StackPane iconoErr = new StackPane(circulo, UiFactory.faLabel(FA_TIMES, 22, C_RED));

            Label lblTitulo = new Label("No se pudo exportar el archivo");
            lblTitulo.setFont(Font.font("System", FontWeight.BOLD, 17));
            lblTitulo.setTextFill(Color.web("#111827"));

            Label lblMsg = new Label(mensaje != null ? mensaje : "Error desconocido.");
            lblMsg.setWrapText(true);
            lblMsg.setMaxWidth(360);
            lblMsg.setPadding(new Insets(10, 16, 10, 16));
            lblMsg.setStyle("-fx-background-color:" + C_RED_BG + ";-fx-background-radius:8;-fx-font-size:12px;-fx-text-fill:" + C_RED + ";");

            Button btnCerrar = buildDialogBtn("Cerrar", C_RED, "#c62828");
            btnCerrar.setOnAction(e -> dialog.close());

            VBox body = buildDialogBody(iconoErr, lblTitulo, lblMsg, btnCerrar);
            javafx.scene.Scene scene = new javafx.scene.Scene(wrapDialog(body), 460, 300);
            dialog.setScene(scene);
            dialog.showAndWait();
        }

        private static Button buildDialogBtn(String texto, String color, String hoverColor) {
            Button b = new Button(texto);
            b.setPrefWidth(120);
            b.setFont(Font.font("System", FontWeight.BOLD, 13));
            String base = "-fx-background-color:" + color + ";-fx-text-fill:white;-fx-background-radius:8;-fx-padding:9 24;-fx-cursor:hand;";
            String hover = "-fx-background-color:" + hoverColor + ";-fx-text-fill:white;-fx-background-radius:8;-fx-padding:9 24;-fx-cursor:hand;";
            b.setStyle(base);
            b.setOnMouseEntered(e -> b.setStyle(hover));
            b.setOnMouseExited(e -> b.setStyle(base));
            return b;
        }

        private static VBox buildDialogBody(javafx.scene.Node... children) {
            VBox body = new VBox(16, children);
            body.setAlignment(Pos.CENTER);
            body.setPadding(new Insets(32, 36, 28, 36));
            body.setStyle("-fx-background-color:white;-fx-background-radius:16;");
            body.setMaxWidth(460);
            body.setEffect(new DropShadow(20, 0, 4, Color.web("#0000002a")));
            return body;
        }

        private static StackPane wrapDialog(VBox body) {
            StackPane root = new StackPane(body);
            root.setStyle("-fx-background-color:" + BG + ";");
            root.setPadding(new Insets(20));
            return root;
        }
    }
}
