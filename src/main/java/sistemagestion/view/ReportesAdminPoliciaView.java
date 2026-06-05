package sistemagestion.view;

/*
 * ══════════════════════════════════════════════════════════════════════════════
 *  ReportesAdminPoliciaView  —  Principios GRASP aplicados
 * ══════════════════════════════════════════════════════════════════════════════
 *
 *  1. INFORMATION EXPERT   → EstadoCeldaPresentation, TipoCeldaPresentation,
 *                             ReporteStatsCalculator, AlertaFiltroEngine
 *  2. CREATOR              → ExcelReporteBuilder crea el XSSFWorkbook
 *  3. CONTROLLER           → ReportesController recibe eventos de UI
 *  4. LOW COUPLING         → La vista solo conoce ReportesController
 *  5. HIGH COHESION        → Cada clase tiene una responsabilidad
 *  6. PURE FABRICATION     → ReportesStyleKit, ExcelReporteBuilder
 *  7. POLYMORPHISM         → EstadoCeldaPresentation, TipoCeldaPresentation (enums)
 *  8. INDIRECTION          → ReportesController intermediario Vista ↔ Servicios
 *
 * ══════════════════════════════════════════════════════════════════════════════
 */

import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
import sistemagestion.service.*;

// ══════════════════════════════════════════════════════════════════════════════
//  GRASP: PURE FABRICATION — ReportesStyleKit
//  Centraliza TODOS los literales CSS. No pertenece al dominio de negocio.
// ══════════════════════════════════════════════════════════════════════════════
class ReportesStyleKit {

    private ReportesStyleKit() {}

    static final String WHITE    = "#ffffff";
    static final String BG       = "#f4f6fb";
    static final String BORDER   = "#e5e7eb";
    static final String GRAY     = "#6b7280";

    static final String C_RED    = "#e53935";
    static final String C_RED_BG = "#fff0f0";
    static final String C_ORG    = "#fb8c00";
    static final String C_ORG_BG = "#fff8e1";
    static final String C_GRN    = "#43a047";
    static final String C_GRN_BG = "#e8f5e9";
    static final String C_BLU    = "#1565c0";
    static final String C_BLU_BG = "#e8f0fe";
    static final String C_PUR    = "#7b1fa2";
    static final String C_PUR_BG = "#f3e5f5";
    static final String C_TEA    = "#00695c";
    static final String C_TEA_BG = "#e0f2f1";
    static final String C_AMB    = "#f9a825";
    static final String C_AMB_BG = "#fffde7";

    // ── Font Awesome codepoints ───────────────────────────────────
    static final String FA_BELL      = "\uf0f3";
    static final String FA_SHIELD    = "\uf505";
    static final String FA_CAR       = "\uf5e4";
    static final String FA_SIREN     = "\uf46a";
    static final String FA_CLIPBOARD = "\uf46d";
    static final String FA_BULLHORN  = "\uf0a1";
    static final String FA_THUMBTACK = "\uf08d";
    static final String FA_DOWNLOAD  = "\uf019";
    static final String FA_REPORT    = "\uf080";
    static final String FA_LIST      = "\uf03a";
    static final String FA_FILTER    = "\uf0b0";
    static final String FA_TIMES     = "\uf00d";
    static final String FA_SEARCH    = "\uf002";
    static final String FA_CLOCK     = "\uf017";
    static final String WHITE_STR    = "#ffffff"; // alias para claridad

    static DropShadow shadow() {
        return new DropShadow(12, 0, 2, Color.web("#0000001a"));
    }

    static String inputBase() {
        return "-fx-background-color:white;"
                + "-fx-border-color:" + BORDER + ";"
                + "-fx-border-radius:8;"
                + "-fx-background-radius:8;"
                + "-fx-padding:6 10;"
                + "-fx-font-size:12px;";
    }

    static String btnExportarBase() {
        return "-fx-background-color:linear-gradient(to right,#16283d,#1f3a56);"
                + "-fx-background-radius:8;-fx-padding:10 18;-fx-cursor:hand;";
    }

    static String btnExportarHover() {
        return "-fx-background-color:linear-gradient(to right,#0f1e30,#16283d);"
                + "-fx-background-radius:8;-fx-padding:10 18;-fx-cursor:hand;";
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  GRASP: POLYMORPHISM + INFORMATION EXPERT — EstadoCeldaPresentation
//  Cada constante sabe su icono, color de texto y fondo de badge.
//  Reemplaza estadoColor(), estadoBg(), estadoIcon() dispersos en la vista.
// ══════════════════════════════════════════════════════════════════════════════
enum EstadoCeldaPresentation {

    ACTIVO      ("\uf058", ReportesStyleKit.C_GRN,  ReportesStyleKit.C_GRN_BG),
    ACTIVA      ("\uf058", ReportesStyleKit.C_GRN,  ReportesStyleKit.C_GRN_BG),
    PENDIENTE   ("\uf017", ReportesStyleKit.C_AMB,  ReportesStyleKit.C_AMB_BG),
    ATENDIDA    ("\uf00c", ReportesStyleKit.C_BLU,  ReportesStyleKit.C_BLU_BG),
    RESUELTA    ("\uf00c", ReportesStyleKit.C_BLU,  ReportesStyleKit.C_BLU_BG),
    CANCELADA   ("\uf057", ReportesStyleKit.C_RED,  ReportesStyleKit.C_RED_BG),
    RECHAZADA   ("\uf057", ReportesStyleKit.C_RED,  ReportesStyleKit.C_RED_BG),
    DESCONOCIDO ("\uf111", ReportesStyleKit.GRAY,   "#f3f4f6");

    final String icono;
    final String colorTexto;
    final String fondoBadge;

    EstadoCeldaPresentation(String icono, String colorTexto, String fondoBadge) {
        this.icono      = icono;
        this.colorTexto = colorTexto;
        this.fondoBadge = fondoBadge;
    }

    /** INFORMATION EXPERT: el enum sabe cómo resolverse desde un String. */
    static EstadoCeldaPresentation desde(String nombre) {
        if (nombre == null) return DESCONOCIDO;
        try {
            return valueOf(nombre.toUpperCase());
        } catch (IllegalArgumentException e) {
            return DESCONOCIDO;
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  GRASP: POLYMORPHISM + INFORMATION EXPERT — TipoCeldaPresentation
//  Reemplaza tipoIcon() con su switch/case.
// ══════════════════════════════════════════════════════════════════════════════
enum TipoCeldaPresentation {

    ROBO      (n -> n.contains("ROB") || n.contains("HURTO"), "\uf505"),
    ACCIDENTE (n -> n.contains("ACCID"),                      "\uf071"),
    INCENDIO  (n -> n.contains("INCEND"),                     "\uf46a"),
    ANIMAL    (n -> n.contains("ANIMAL"),                     "\uf6d3"),
    VANDALISMO(n -> n.contains("VANDAL"),                     "\uf6e3"),
    SOSPECHA  (n -> n.contains("SOSPECH") || n.contains("PERSON"), "\uf007"),
    GENERAL   (n -> true,                                     ReportesStyleKit.FA_BELL);

    private final java.util.function.Predicate<String> regla;
    final String icono;

    TipoCeldaPresentation(java.util.function.Predicate<String> regla, String icono) {
        this.regla = regla;
        this.icono = icono;
    }

    /** INFORMATION EXPERT: resuelve el icono según nombre del tipo. */
    static String iconoPara(String nombreTipo) {
        if (nombreTipo == null) return GENERAL.icono;
        String n = nombreTipo.toUpperCase();
        for (TipoCeldaPresentation t : values()) {
            if (t.regla.test(n)) return t.icono;
        }
        return GENERAL.icono;
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  GRASP: INFORMATION EXPERT — ReporteStatsCalculator
//  Conoce las listas y sabe calcular cada total.
//
//  ✅ CORREGIDO: Agregar 'throws Exception' para que el compilador acepte
//     las llamadas a servicios que podrían lanzar excepciones.
// ══════════════════════════════════════════════════════════════════════════════
class ReporteStatsCalculator {

    record Stats(long alertas, long policias, long unidades,
                 long asignaciones, long alarmas, long atenciones) {}

    private ReporteStatsCalculator() {}

    static Stats calcular(List<Alerta> alertas,
                          List<AtencionAlerta> atenciones,
                          PoliciaService policiaService,
                          UnidadPolicialService unidadService,
                          AsignacionUnidadService asignacionService,
                          AlarmaService alarmaService) throws Exception {

        // ── CORRECCIÓN: convertir int a long explícitamente ────────
        long totalPolicias  = safe(policiaService    != null ? (long) policiaService.listar().size()    : 0L);
        long totalUnidades  = safe(unidadService     != null ? (long) unidadService.listar().size()     : 0L);
        long totalAsig      = safe(asignacionService != null ? (long) asignacionService.listar().size()  : 0L);
        long totalAlarmas   = safe(alarmaService     != null ? (long) alarmaService.listar().size()      : 0L);

        return new Stats(
                alertas.size(),
                totalPolicias,
                totalUnidades,
                totalAsig,
                totalAlarmas,
                atenciones.size());
    }

    /** Recibe long: evita la ambigüedad entre Math.max(int,int) y Math.max(long,long). */
    private static long safe(long v) {
        return Math.max(0L, v);
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  GRASP: INFORMATION EXPERT — AlertaFiltroEngine
//  Encapsula TODA la lógica de filtrado y paginación.
// ══════════════════════════════════════════════════════════════════════════════
class AlertaFiltroEngine {

    static final int FILAS_POR_PAGINA = 8;

    private AlertaFiltroEngine() {}

    static List<Alerta> filtrar(List<Alerta> todas,
                                 String tipo,
                                 String estado,
                                 String barrio,
                                 String buscar,
                                 LocalDate desde,
                                 LocalDate hasta) {
        String q = (buscar != null) ? buscar.trim().toLowerCase() : "";
        return todas.stream()
                .filter(a -> tipo   == null || (a.getTipoalerta() != null && tipo.equals(a.getTipoalerta().getNombre())))
                .filter(a -> estado == null || (a.getEstado()     != null && estado.equals(a.getEstado().name())))
                .filter(a -> barrio == null || (a.getBarrio()     != null && barrio.equals(a.getBarrio().getNombre())))
                .filter(a -> q.isEmpty()    || (a.getDescripcion() != null
                                                && a.getDescripcion().toLowerCase().contains(q)))
                .filter(a -> {
                    if (desde == null && hasta == null) return true;
                    LocalDateTime fh = a.getFechaHora();
                    if (fh == null) return false;
                    LocalDate fecha = fh.toLocalDate();
                    if (desde != null && fecha.isBefore(desde)) return false;
                    if (hasta != null && fecha.isAfter(hasta))  return false;
                    return true;
                })
                .collect(Collectors.toList());
    }

    static int[] rangoPagina(int pagina, int totalRegistros) {
        int desde = (pagina - 1) * FILAS_POR_PAGINA;
        int hasta = Math.min(desde + FILAS_POR_PAGINA, totalRegistros);
        return new int[]{desde, hasta};
    }

    static int totalPaginas(int totalRegistros) {
        return (int) Math.ceil((double) totalRegistros / FILAS_POR_PAGINA);
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  GRASP: CREATOR + PURE FABRICATION — ExcelReporteBuilder
// ══════════════════════════════════════════════════════════════════════════════
class ExcelReporteBuilder {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private ExcelReporteBuilder() {}

    static void exportar(List<Alerta> alertas,
                         List<AtencionAlerta> atenciones,
                         File destino) throws Exception {

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Alertas");

            CellStyle hStyle = crearEstiloHeader(wb);
            CellStyle nStyle = crearEstiloNormal(wb);

            String[] headers = {"ID", "Tipo Alerta", "Barrio", "Estado",
                    "Fecha y Hora", "Descripción", "Latitud", "Longitud",
                    "Unidad Asignada", "Atendida por"};

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(hStyle);
            }

            int rowNum = 1;
            for (Alerta al : alertas) {
                final int idAl = al.getId_alerta();
                AtencionAlerta aten = atenciones.stream()
                        .filter(a -> a.getAlerta() != null
                                && a.getAlerta().getId_alerta() == idAl)
                        .findFirst().orElse(null);

                String polNombre = "";
                if (aten != null && aten.getUnidad() != null
                        && aten.getUnidad().getPolicias() != null
                        && !aten.getUnidad().getPolicias().isEmpty()) {
                    Policia p = aten.getUnidad().getPolicias().get(0);
                    polNombre = p.getPrimer_nombre() + " " + p.getPrimer_apellido();
                }

                String[] vals = {
                    String.valueOf(al.getId_alerta()),
                    al.getTipoalerta() != null ? al.getTipoalerta().getNombre() : "",
                    al.getBarrio()     != null ? al.getBarrio().getNombre()     : "",
                    al.getEstado()     != null ? al.getEstado().name()          : "",
                    al.getFechaHora()  != null ? al.getFechaHora().format(FMT)  : "",
                    al.getDescripcion() != null ? al.getDescripcion()           : "",
                    String.valueOf(al.getLatitud()),
                    String.valueOf(al.getLongitud()),
                    aten != null && aten.getUnidad() != null ? aten.getUnidad().getNombre() : "",
                    polNombre
                };

                Row row = sheet.createRow(rowNum++);
                for (int i = 0; i < vals.length; i++) {
                    Cell cell = row.createCell(i);
                    cell.setCellValue(vals[i]);
                    cell.setCellStyle(nStyle);
                }
            }

            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);

            try (FileOutputStream fos = new FileOutputStream(destino)) {
                wb.write(fos);
            }
        }
    }

    private static CellStyle crearEstiloHeader(XSSFWorkbook wb) {
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

    private static CellStyle crearEstiloNormal(XSSFWorkbook wb) {
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
}

// ══════════════════════════════════════════════════════════════════════════════
//  GRASP: CONTROLLER + INDIRECTION — ReportesController
// ══════════════════════════════════════════════════════════════════════════════
class ReportesController {

    record ResultadoExport(boolean exito, String mensaje) {}

    private final AlertaService           alertaService;
    private final AsignacionUnidadService  asignacionService;
    private final UnidadPolicialService    unidadService;
    private final PoliciaService           policiaService;
    private final AlarmaService            alarmaService;
    private final NotificacionService      notificacionService;
    private final AtencionAlertaService    atencionService;

    private List<Alerta>         alertasCached;
    private List<AtencionAlerta> atencionesCached;
    private List<Alerta>         alertasFiltradas;

    ReportesController(AlertaService alertaService,
                       AsignacionUnidadService asignacionService,
                       UnidadPolicialService unidadService,
                       PoliciaService policiaService,
                       AlarmaService alarmaService,
                       NotificacionService notificacionService,
                       AtencionAlertaService atencionService) {
        this.alertaService       = alertaService;
        this.asignacionService   = asignacionService;
        this.unidadService       = unidadService;
        this.policiaService      = policiaService;
        this.alarmaService       = alarmaService;
        this.notificacionService = notificacionService;
        this.atencionService     = atencionService;
    }

    void inicializar() {
        alertasCached    = alertaService   != null ? alertaService.listar()   : List.of();
        atencionesCached = atencionService != null ? atencionService.listar() : List.of();
        alertasFiltradas = alertasCached;
    }

    List<Alerta>         getAlertasCached()    { return alertasCached; }
    List<AtencionAlerta> getAtencionesCached() { return atencionesCached; }
    List<Alerta>         getAlertasFiltradas() { return alertasFiltradas; }

    ReporteStatsCalculator.Stats calcularStats() throws Exception {
        return ReporteStatsCalculator.calcular(
                alertasCached, atencionesCached,
                policiaService, unidadService, asignacionService, alarmaService);
    }

    long totalNotificaciones() {
        return notificacionService != null ? (long) notificacionService.listar().size() : 0L;
    }

    long totalAsignaciones() {
        return asignacionService != null ? (long) asignacionService.listar().size() : 0L;
    }

    List<AsignacionUnidad> getAsignaciones() {
        return asignacionService != null ? asignacionService.listar() : List.of();
    }

    List<Alerta> aplicarFiltros(String tipo, String estado, String barrio,
                                 String buscar, LocalDate desde, LocalDate hasta) {
        alertasFiltradas = AlertaFiltroEngine.filtrar(
                alertasCached, tipo, estado, barrio, buscar, desde, hasta);
        return alertasFiltradas;
    }

    List<Alerta> limpiarFiltros() {
        alertasFiltradas = alertasCached;
        return alertasFiltradas;
    }

    List<String> opcionesTipo() {
        return alertasCached.stream()
                .filter(a -> a.getTipoalerta() != null)
                .map(a -> a.getTipoalerta().getNombre())
                .distinct().sorted().collect(Collectors.toList());
    }

    List<String> opcionesEstado() {
        return alertasCached.stream()
                .filter(a -> a.getEstado() != null)
                .map(a -> a.getEstado().name())
                .distinct().sorted().collect(Collectors.toList());
    }

    List<String> opcionesBarrio() {
        return alertasCached.stream()
                .filter(a -> a.getBarrio() != null)
                .map(a -> a.getBarrio().getNombre())
                .distinct().sorted().collect(Collectors.toList());
    }

    ResultadoExport exportar(File destino) {
        try {
            ExcelReporteBuilder.exportar(alertasFiltradas, atencionesCached, destino);
            return new ResultadoExport(true, "Reporte guardado en:\n" + destino.getAbsolutePath());
        } catch (Exception ex) {
            return new ResultadoExport(false, "Error al exportar: " + ex.getMessage());
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  VISTA PRINCIPAL — ReportesAdminPoliciaView
// ══════════════════════════════════════════════════════════════════════════════
public class ReportesAdminPoliciaView {

    private static final double[] WIDTHS = {45, 140, 120, 120, 155, 180, 130, 150};

    private final ReportesController controller;

    private int   paginaActual = 1;
    private VBox  tbodyRef;
    private Label lblMostrandoRef;
    private HBox  paginacionBoxRef;

    private ComboBox<String> cbTipo;
    private ComboBox<String> cbEstado;
    private ComboBox<String> cbBarrio;
    private DatePicker       dpDesde;
    private DatePicker       dpHasta;
    private TextField        txtBuscar;

    // ── Constructores ─────────────────────────────────────────────

    public ReportesAdminPoliciaView(ReportesController controller) {
        this.controller = controller;
    }

    public ReportesAdminPoliciaView(AlertaService alertaService,
                                     AsignacionUnidadService asignacionService,
                                     UnidadPolicialService unidadService,
                                     PoliciaService policiaService,
                                     AlarmaService alarmaService,
                                     NotificacionService notificacionService,
                                     AtencionAlertaService atencionService) {
        this(new ReportesController(alertaService, asignacionService, unidadService,
                                     policiaService, alarmaService, notificacionService,
                                     atencionService));
    }

    // ══════════════════════════════════════════════════════════════
    //  PUNTO DE ENTRADA
    // ══════════════════════════════════════════════════════════════
    public ScrollPane build() {
        Font.loadFont(getClass().getResourceAsStream("/fa-solid-900.ttf"), 16);
        controller.inicializar();

        VBox content = new VBox(20);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color:" + ReportesStyleKit.BG + ";");

        content.getChildren().add(buildTopBar());
        
        try {
            content.getChildren().add(buildStatsRow(controller.calcularStats()));
        } catch (Exception e) {
            content.getChildren().add(buildErrorState("No se pudieron cargar las estadísticas: " + e.getMessage()));
        }

        try {
            content.getChildren().add(seccionLabel("Detalle de Actividad"));
            content.getChildren().add(buildDetailCard());
            content.getChildren().add(seccionLabel("Reporte de Alertas"));
            content.getChildren().add(buildFiltrosCard());
            content.getChildren().add(buildTablaAlertas());
            content.getChildren().add(buildPaginacion());
        } catch (Exception e) {
            content.getChildren().add(buildErrorState(e.getMessage()));
        }

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color:" + ReportesStyleKit.BG
                + "; -fx-background:" + ReportesStyleKit.BG + ";");
        return scroll;
    }

    // ══════════════════════════════════════════════════════════════
    //  TOP BAR
    // ══════════════════════════════════════════════════════════════
    private HBox buildTopBar() {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);

        StackPane titleIcon = faIconBox(ReportesStyleKit.FA_REPORT, 22,
                ReportesStyleKit.C_BLU, ReportesStyleKit.C_BLU_BG, 44);

        Label title = lbl("Reportes del Sistema", 26, "#111827", true);
        Label sub   = lbl("Resumen general de actividad operativa", 13, ReportesStyleKit.GRAY, false);
        HBox titleRow = new HBox(14, titleIcon, new VBox(4, title, sub));
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Button btnExportar = buildBtnExportar();

        HBox right = new HBox(12, btnExportar);
        right.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(right, Priority.ALWAYS);

        bar.getChildren().addAll(titleRow, right);
        return bar;
    }

    private Button buildBtnExportar() {
        Label dlIco = faLabel(ReportesStyleKit.FA_DOWNLOAD, 14, ReportesStyleKit.WHITE);
        Label dlTxt = new Label("  Exportar Excel");
        dlTxt.setStyle("-fx-text-fill:white;-fx-font-size:13px;-fx-font-weight:bold;");
        HBox c = new HBox(4, dlIco, dlTxt);
        c.setAlignment(Pos.CENTER);
        Button btn = new Button();
        btn.setGraphic(c);
        btn.setStyle(ReportesStyleKit.btnExportarBase());
        btn.setOnMouseEntered(e -> btn.setStyle(ReportesStyleKit.btnExportarHover()));
        btn.setOnMouseExited(e  -> btn.setStyle(ReportesStyleKit.btnExportarBase()));
        btn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Guardar reporte de alertas");
            fc.setInitialFileName("Reporte_Alertas_WolertApp.xlsx");
            fc.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Excel", "*.xlsx"));
            File file = fc.showSaveDialog(btn.getScene().getWindow());
            if (file == null) return;
            ReportesController.ResultadoExport res = controller.exportar(file);
            Alert alert = new Alert(res.exito()
                    ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
            alert.setTitle(res.exito() ? "Exportado" : "Error");
            alert.setHeaderText(null);
            alert.setContentText((res.exito() ? "✅ " : "") + res.mensaje());
            alert.showAndWait();
        });
        return btn;
    }

    // ══════════════════════════════════════════════════════════════
    //  STATS ROW
    // ══════════════════════════════════════════════════════════════
    private HBox buildStatsRow(ReporteStatsCalculator.Stats s) {
        HBox row = new HBox(16);
        HBox.setHgrow(row, Priority.ALWAYS);
        try {
            row.getChildren().addAll(
                    statCard(ReportesStyleKit.C_RED_BG, ReportesStyleKit.C_RED,
                             ReportesStyleKit.FA_BELL,      "Alertas",      s.alertas(),      "Registradas en el sistema"),
                    statCard(ReportesStyleKit.C_BLU_BG, ReportesStyleKit.C_BLU,
                             ReportesStyleKit.FA_SHIELD,    "Policías",     s.policias(),     "Personal activo"),
                    statCard(ReportesStyleKit.C_GRN_BG, ReportesStyleKit.C_GRN,
                             ReportesStyleKit.FA_CAR,       "Unidades",     s.unidades(),     "Unidades configuradas"),
                    statCard(ReportesStyleKit.C_ORG_BG, ReportesStyleKit.C_ORG,
                             ReportesStyleKit.FA_THUMBTACK, "Asignaciones", s.asignaciones(), "Despachos realizados"),
                    statCard(ReportesStyleKit.C_AMB_BG, ReportesStyleKit.C_AMB,
                             ReportesStyleKit.FA_SIREN,     "Alarmas",      s.alarmas(),      "Eventos registrados"),
                    statCard(ReportesStyleKit.C_TEA_BG, ReportesStyleKit.C_TEA,
                             ReportesStyleKit.FA_CLIPBOARD, "Atenciones",   s.atenciones(),   "Alertas atendidas")
            );
        } catch (Exception e) {
            row.getChildren().add(buildErrorState(e.getMessage()));
        }
        return row;
    }

    private VBox statCard(String bgIcon, String accent, String faIcon,
                          String title, long value, String sub) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(18, 20, 18, 20));
        card.setStyle("-fx-background-color:white;-fx-background-radius:18;");
        HBox.setHgrow(card, Priority.ALWAYS);
        card.setEffect(ReportesStyleKit.shadow());

        StackPane iconWrap = faIconBox(faIcon, 20, accent, bgIcon, 48);

        Label valueLbl = new Label(String.valueOf(value));
        valueLbl.setFont(Font.font("System", FontWeight.BOLD, 32));
        valueLbl.setTextFill(Color.web(accent));

        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#374151;");
        Label subLbl = new Label(sub);
        subLbl.setStyle("-fx-font-size:11px;-fx-text-fill:" + ReportesStyleKit.GRAY + ";");

        Rectangle bar = new Rectangle();
        bar.setHeight(3); bar.setArcWidth(3); bar.setArcHeight(3);
        bar.setFill(Color.web(accent));
        bar.widthProperty().bind(card.widthProperty().subtract(40));

        VBox textBox = new VBox(2, titleLbl, valueLbl, subLbl);
        HBox top     = new HBox(14, iconWrap, textBox);
        top.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(top, bar);
        card.setOnMouseEntered(e -> card.setTranslateY(-3));
        card.setOnMouseExited(e  -> card.setTranslateY(0));
        return card;
    }

    // ══════════════════════════════════════════════════════════════
    //  DETALLE DE ACTIVIDAD
    // ══════════════════════════════════════════════════════════════
    private VBox buildDetailCard() throws Exception {
        long totalNoti = controller.totalNotificaciones();
        long totalAsig = controller.totalAsignaciones();

        VBox card = new VBox(0);
        card.setStyle("-fx-background-color:white;-fx-background-radius:14;");
        card.setEffect(ReportesStyleKit.shadow());
        card.getChildren().add(cardHeader("Estadísticas adicionales",
                ReportesStyleKit.C_PUR, ReportesStyleKit.FA_BULLHORN));

        String[][] items = {
            {ReportesStyleKit.FA_BULLHORN,  "Notificaciones enviadas", String.valueOf(totalNoti),
             ReportesStyleKit.C_PUR, ReportesStyleKit.C_PUR_BG},
            {ReportesStyleKit.FA_THUMBTACK, "Total de asignaciones",   String.valueOf(totalAsig),
             ReportesStyleKit.C_ORG, ReportesStyleKit.C_ORG_BG},
        };
        for (int i = 0; i < items.length; i++) {
            card.getChildren().add(buildDetailRow(
                    items[i][0], items[i][1], items[i][2], items[i][3], items[i][4]));
            if (i < items.length - 1) card.getChildren().add(divider());
        }
        return card;
    }

    private HBox buildDetailRow(String faIcon, String labelText, String value,
                                 String color, String bgColor) {
        HBox row = new HBox(14);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(14, 20, 14, 20));
        row.setStyle("-fx-background-color:transparent;");
        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color:#f8f9fd;"));
        row.setOnMouseExited(e  -> row.setStyle("-fx-background-color:transparent;"));

        StackPane iconBox = faIconBox(faIcon, 17, color, bgColor, 38);
        Label labelLbl    = new Label(labelText);
        labelLbl.setFont(Font.font("System", 13));
        labelLbl.setTextFill(Color.web("#374151"));
        HBox.setHgrow(labelLbl, Priority.ALWAYS);

        Label valLbl = new Label(value);
        valLbl.setFont(Font.font("System", FontWeight.BOLD, 15));
        valLbl.setTextFill(Color.web(color));
        valLbl.setPadding(new Insets(4, 14, 4, 14));
        valLbl.setStyle("-fx-background-color:" + bgColor + ";-fx-background-radius:20;");

        row.getChildren().addAll(iconBox, labelLbl, valLbl);
        return row;
    }

    // ══════════════════════════════════════════════════════════════
    //  PANEL DE FILTROS
    // ══════════════════════════════════════════════════════════════
    private VBox buildFiltrosCard() {
        VBox card = new VBox(0);
        card.setStyle("-fx-background-color:white;-fx-background-radius:14;");
        card.setEffect(ReportesStyleKit.shadow());
        card.getChildren().add(cardHeader("Filtrar alertas",
                ReportesStyleKit.C_BLU, ReportesStyleKit.FA_FILTER));

        cbTipo   = comboFiltro("Todos los tipos");
        cbEstado = comboFiltro("Todos los estados");
        cbBarrio = comboFiltro("Todos los barrios");

        controller.opcionesTipo().forEach(t   -> cbTipo.getItems().add(t));
        controller.opcionesEstado().forEach(e  -> cbEstado.getItems().add(e));
        controller.opcionesBarrio().forEach(b  -> cbBarrio.getItems().add(b));

        txtBuscar = new TextField();
        txtBuscar.setPromptText("Buscar en descripción…");
        txtBuscar.setPrefWidth(200);
        txtBuscar.setStyle(ReportesStyleKit.inputBase());
        HBox.setHgrow(txtBuscar, Priority.ALWAYS);

        HBox fila1 = new HBox(16,
                controlConLabel(filtroLbl("Tipo:"),   cbTipo,   130),
                controlConLabel(filtroLbl("Estado:"), cbEstado, 130),
                controlConLabel(filtroLbl("Barrio:"), cbBarrio, 150),
                controlConLabel(filtroLbl("Buscar:"), txtBuscar, -1)
        );
        fila1.setAlignment(Pos.CENTER_LEFT);
        fila1.setPadding(new Insets(14, 20, 10, 20));

        dpDesde = datePicker("Desde…");
        dpHasta = datePicker("Hasta…");

        Button btnAplicar = accionBtn("Aplicar filtros", ReportesStyleKit.FA_FILTER,
                ReportesStyleKit.C_BLU,
                "-fx-background-color:" + ReportesStyleKit.C_BLU + ";"
                + "-fx-text-fill:white;-fx-background-radius:8;"
                + "-fx-padding:8 18;-fx-cursor:hand;-fx-font-weight:bold;");
        btnAplicar.setOnAction(e -> {
            controller.aplicarFiltros(
                    cbTipo.getValue(), cbEstado.getValue(), cbBarrio.getValue(),
                    txtBuscar.getText(), dpDesde.getValue(), dpHasta.getValue());
            paginaActual = 1;
            renderizarPagina();
        });

        Button btnLimpiar = accionBtn("Limpiar", ReportesStyleKit.FA_TIMES,
                ReportesStyleKit.C_RED,
                "-fx-background-color:" + ReportesStyleKit.C_RED_BG + ";"
                + "-fx-text-fill:" + ReportesStyleKit.C_RED + ";"
                + "-fx-background-radius:8;-fx-padding:8 18;"
                + "-fx-cursor:hand;-fx-font-weight:bold;");
        btnLimpiar.setOnAction(e -> {
            cbTipo.setValue(null);   cbEstado.setValue(null);
            cbBarrio.setValue(null); txtBuscar.clear();
            dpDesde.setValue(null);  dpHasta.setValue(null);
            controller.limpiarFiltros();
            paginaActual = 1;
            renderizarPagina();
        });

        HBox fila2 = new HBox(16,
                controlConLabel(filtroLbl("Desde:"), dpDesde, 160),
                controlConLabel(filtroLbl("Hasta:"), dpHasta, 160),
                btnAplicar, btnLimpiar
        );
        fila2.setAlignment(Pos.CENTER_LEFT);
        fila2.setPadding(new Insets(0, 20, 14, 20));

        card.getChildren().addAll(fila1, fila2);
        return card;
    }

    // ══════════════════════════════════════════════════════════════
    //  TABLA DE ALERTAS
    // ══════════════════════════════════════════════════════════════
    private VBox buildTablaAlertas() {
        VBox card = new VBox(0);
        card.setStyle("-fx-background-color:white;-fx-background-radius:14;");
        card.setEffect(ReportesStyleKit.shadow());
        card.getChildren().add(cardHeader("Listado de Alertas",
                ReportesStyleKit.C_GRN, ReportesStyleKit.FA_LIST));

        String[] cols = {"#","Tipo","Barrio","Estado","Fecha y Hora","Descripción","Unidad","Atendida por"};
        HBox thead = new HBox(0);
        thead.setPadding(new Insets(11, 16, 11, 16));
        thead.setStyle("-fx-background-color:#f8fafc;"
                + "-fx-border-color:transparent transparent "
                + ReportesStyleKit.BORDER + " transparent;-fx-border-width:0 0 1 0;");
        for (int i = 0; i < cols.length; i++) {
            Label h = new Label(cols[i].toUpperCase());
            h.setStyle("-fx-font-size:10px;-fx-font-weight:bold;-fx-text-fill:#9ca3af;");
            h.setPrefWidth(WIDTHS[i]);
            h.setMinWidth(WIDTHS[i]);
            thead.getChildren().add(h);
        }

        tbodyRef = new VBox(0);
        renderizarPagina();

        lblMostrandoRef = lbl("", 12, ReportesStyleKit.GRAY, false);
        HBox footer = new HBox(lblMostrandoRef);
        footer.setPadding(new Insets(11, 16, 11, 16));
        footer.setStyle("-fx-border-color:" + ReportesStyleKit.BORDER
                + " transparent transparent transparent;-fx-border-width:1 0 0 0;");

        card.getChildren().addAll(thead, tbodyRef, footer);
        return card;
    }

    private void renderizarPagina() {
        if (tbodyRef == null) return;
        tbodyRef.getChildren().clear();

        List<Alerta>           filtradas    = controller.getAlertasFiltradas();
        List<AtencionAlerta>   atenciones   = controller.getAtencionesCached();
        List<AsignacionUnidad> asignaciones = controller.getAsignaciones();

        int total   = filtradas.size();
        int[] rango = AlertaFiltroEngine.rangoPagina(paginaActual, total);
        int desde   = rango[0];
        int hasta   = rango[1];

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        if (total == 0) {
            VBox vacio = new VBox(10);
            vacio.setAlignment(Pos.CENTER);
            vacio.setPadding(new Insets(40));
            vacio.getChildren().addAll(
                    faLabel(ReportesStyleKit.FA_SEARCH, 32, ReportesStyleKit.GRAY),
                    lbl("No hay alertas que coincidan con los filtros", 14, ReportesStyleKit.GRAY, false)
            );
            tbodyRef.getChildren().add(vacio);
            actualizarMostrando(total, 0, 0);
            actualizarPaginacion();
            return;
        }

        for (int i = desde; i < hasta; i++) {
            Alerta al      = filtradas.get(i);
            boolean par    = i % 2 == 0;
            final int idAl = al.getId_alerta();

            AtencionAlerta aten = atenciones.stream()
                    .filter(a -> a.getAlerta() != null
                            && a.getAlerta().getId_alerta() == idAl)
                    .findFirst().orElse(null);

            String[] unidadArr  = {"—"};
            String[] policiaArr = {"—"};

            if (aten != null && aten.getUnidad() != null) {
                unidadArr[0] = aten.getUnidad().getNombre();
                if (aten.getUnidad().getPolicias() != null
                        && !aten.getUnidad().getPolicias().isEmpty()) {
                    Policia p = aten.getUnidad().getPolicias().get(0);
                    policiaArr[0] = p.getPrimer_nombre() + " " + p.getPrimer_apellido();
                }
            } else {
                asignaciones.stream()
                        .filter(asig -> asig.getAlerta() != null
                                && asig.getAlerta().getId_alerta() == idAl
                                && asig.getUnidadpolicial() != null)
                        .findFirst()
                        .ifPresent(asig -> unidadArr[0] = asig.getUnidadpolicial().getNombre());
            }

            String tipo   = al.getTipoalerta() != null ? al.getTipoalerta().getNombre() : "—";
            String barrio = al.getBarrio()      != null ? al.getBarrio().getNombre()     : "—";
            String estado = al.getEstado()      != null ? al.getEstado().name()          : "—";
            String fecha  = al.getFechaHora()   != null ? al.getFechaHora().format(fmt)  : "—";
            String desc   = al.getDescripcion() != null ? al.getDescripcion()            : "—";

            HBox fila = buildFila(al.getId_alerta(), tipo, barrio, estado,
                    fecha, desc, unidadArr[0], policiaArr[0], par);
            tbodyRef.getChildren().add(fila);
        }

        actualizarMostrando(total, desde, hasta);
        actualizarPaginacion();
    }

    private HBox buildFila(int id, String tipo, String barrio, String estado,
                            String fecha, String desc, String unidad,
                            String policia, boolean par) {
        HBox fila = new HBox(0);
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.setPadding(new Insets(10, 16, 10, 16));
        String bgN = "-fx-background-color:" + (par ? ReportesStyleKit.WHITE : "#fafbfd") + ";"
                + "-fx-border-color:transparent transparent "
                + ReportesStyleKit.BORDER + " transparent;-fx-border-width:0 0 1 0;";
        fila.setStyle(bgN);
        fila.setOnMouseEntered(e -> fila.setStyle("-fx-background-color:#EEF2FF;"
                + "-fx-border-color:transparent transparent "
                + ReportesStyleKit.BORDER + " transparent;-fx-border-width:0 0 1 0;-fx-cursor:hand;"));
        fila.setOnMouseExited(e  -> fila.setStyle(bgN));

        // Col 1: ID badge circular
        StackPane idBadge = new StackPane();
        idBadge.getChildren().addAll(
                new Circle(16, Color.web(ReportesStyleKit.C_BLU_BG)),
                lbl(String.valueOf(id), 11, ReportesStyleKit.C_BLU, true));
        HBox idBox = wrapCell(idBadge, WIDTHS[0]);

        // Col 2: Tipo — POLYMORPHISM: TipoCeldaPresentation.iconoPara()
        String tipoIcono = TipoCeldaPresentation.iconoPara(tipo);
        HBox tipoBadge   = badgeBox(tipoIcono, tipo, ReportesStyleKit.C_RED, ReportesStyleKit.C_RED_BG);
        HBox tipoBox     = wrapCell(tipoBadge, WIDTHS[1]);

        // Col 3: Barrio
        Label barrioLbl = celdaFija(barrio, WIDTHS[2]);

        // Col 4: Estado — POLYMORPHISM: EstadoCeldaPresentation.desde()
        EstadoCeldaPresentation ep = EstadoCeldaPresentation.desde(estado);
        HBox estadoBadge = badgeBox(ep.icono, estado, ep.colorTexto, ep.fondoBadge);
        HBox estadoBox   = wrapCell(estadoBadge, WIDTHS[3]);

        // Col 5: Fecha con ícono reloj
        HBox fechaHBox = new HBox(3,
                faLabel(ReportesStyleKit.FA_CLOCK, 11, ReportesStyleKit.GRAY),
                lbl(" " + fecha, 11, "#374151", false));
        fechaHBox.setAlignment(Pos.CENTER_LEFT);
        HBox fechaBox = wrapCell(fechaHBox, WIDTHS[4]);

        fila.getChildren().addAll(
                idBox, tipoBox, barrioLbl, estadoBox, fechaBox,
                celdaFija(desc,    WIDTHS[5]),
                celdaFija(unidad,  WIDTHS[6]),
                celdaFija(policia, WIDTHS[7]));
        return fila;
    }

    // ══════════════════════════════════════════════════════════════
    //  PAGINACIÓN
    // ══════════════════════════════════════════════════════════════
    private HBox buildPaginacion() {
        paginacionBoxRef = new HBox(6);
        paginacionBoxRef.setAlignment(Pos.CENTER_RIGHT);
        paginacionBoxRef.setPadding(new Insets(4, 0, 0, 0));
        return paginacionBoxRef;
    }

    private void actualizarPaginacion() {
        if (paginacionBoxRef == null) return;
        paginacionBoxRef.getChildren().clear();
        int total     = controller.getAlertasFiltradas() != null
                ? controller.getAlertasFiltradas().size() : 0;
        int totalPags = AlertaFiltroEngine.totalPaginas(total);
        if (totalPags <= 1) return;

        paginacionBoxRef.getChildren().add(btnNav("\uf104", paginaActual > 1, () -> {
            paginaActual--; renderizarPagina();
        }));

        int ini = Math.max(1, paginaActual - 2);
        int fin = Math.min(totalPags, paginaActual + 2);

        if (ini > 1) {
            paginacionBoxRef.getChildren().add(btnPagNum(1));
            if (ini > 2) paginacionBoxRef.getChildren().add(puntosSuspensivos());
        }
        for (int i = ini; i <= fin; i++) paginacionBoxRef.getChildren().add(btnPagNum(i));
        if (fin < totalPags) {
            if (fin < totalPags - 1) paginacionBoxRef.getChildren().add(puntosSuspensivos());
            paginacionBoxRef.getChildren().add(btnPagNum(totalPags));
        }

        paginacionBoxRef.getChildren().add(btnNav("\uf105", paginaActual < totalPags, () -> {
            paginaActual++; renderizarPagina();
        }));
    }

    private Button btnPagNum(int pg) {
        boolean esActual = pg == paginaActual;
        Button b = new Button(String.valueOf(pg));
        b.setPrefSize(34, 34); b.setMinSize(34, 34); b.setMaxSize(34, 34);
        b.setFont(Font.font("System",
                esActual ? FontWeight.BOLD : FontWeight.NORMAL, 13));
        String styleBase = esActual
                ? "-fx-background-color:" + ReportesStyleKit.C_BLU
                  + ";-fx-text-fill:white;-fx-background-radius:8;"
                  + "-fx-cursor:hand;-fx-border-color:" + ReportesStyleKit.C_BLU
                  + ";-fx-border-radius:8;-fx-border-width:1.5;"
                : "-fx-background-color:" + ReportesStyleKit.WHITE
                  + ";-fx-text-fill:#374151;-fx-background-radius:8;"
                  + "-fx-cursor:hand;-fx-border-color:" + ReportesStyleKit.BORDER
                  + ";-fx-border-radius:8;-fx-border-width:1.5;";
        String styleHover = "-fx-background-color:" + ReportesStyleKit.C_BLU_BG
                + ";-fx-text-fill:" + ReportesStyleKit.C_BLU
                + ";-fx-background-radius:8;-fx-cursor:hand;-fx-border-color:"
                + ReportesStyleKit.C_BLU + ";-fx-border-radius:8;-fx-border-width:1.5;";
        b.setStyle(styleBase);
        if (!esActual) {
            b.setOnMouseEntered(e -> b.setStyle(styleHover));
            b.setOnMouseExited(e  -> b.setStyle(styleBase));
        }
        b.setOnAction(e -> { paginaActual = pg; renderizarPagina(); });
        return b;
    }

    private Button btnNav(String fa, boolean enabled, Runnable accion) {
        Label ico = new Label(fa);
        ico.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';-fx-font-size:13px;"
                + "-fx-text-fill:" + (enabled ? "#374151" : "#d1d5db") + ";");
        Button b = new Button();
        b.setGraphic(ico);
        b.setPrefSize(34, 34); b.setMinSize(34, 34); b.setMaxSize(34, 34);
        b.setDisable(!enabled);
        String base  = "-fx-background-color:" + ReportesStyleKit.WHITE
                + ";-fx-background-radius:8;-fx-cursor:" + (enabled ? "hand" : "default")
                + ";-fx-border-color:" + ReportesStyleKit.BORDER
                + ";-fx-border-radius:8;-fx-border-width:1.5;";
        String hover = "-fx-background-color:" + ReportesStyleKit.C_BLU_BG
                + ";-fx-background-radius:8;-fx-cursor:hand;-fx-border-color:"
                + ReportesStyleKit.C_BLU + ";-fx-border-radius:8;-fx-border-width:1.5;";
        b.setStyle(base);
        if (enabled) {
            b.setOnMouseEntered(e -> b.setStyle(hover));
            b.setOnMouseExited(e  -> b.setStyle(base));
        }
        b.setOnAction(e -> accion.run());
        return b;
    }

    private Label puntosSuspensivos() {
        Label l = new Label("…");
        l.setStyle("-fx-font-size:14px;-fx-text-fill:" + ReportesStyleKit.GRAY + ";");
        l.setPadding(new Insets(0, 4, 0, 4));
        return l;
    }

    private void actualizarMostrando(int total, int desde, int hasta) {
        if (lblMostrandoRef == null) return;
        int cached = controller.getAlertasCached().size();
        lblMostrandoRef.setText("Mostrando "
                + (total == 0 ? 0 : desde + 1) + " – " + hasta + " de " + total + " alertas"
                + (total < cached ? " (filtradas de " + cached + " totales)" : ""));
    }

    // ══════════════════════════════════════════════════════════════
    //  HELPERS UI
    // ══════════════════════════════════════════════════════════════

    private Label faLabel(String unicode, double size, String color) {
        Label l = new Label(unicode);
        l.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';"
                + "-fx-font-size:" + size + "px;"
                + "-fx-text-fill:" + color + ";");
        return l;
    }

    private StackPane faIconBox(String unicode, double iconSize,
                                 String iconColor, String bgColor, double boxSize) {
        StackPane box = new StackPane();
        box.setPrefSize(boxSize, boxSize);
        box.setMinSize(boxSize, boxSize);
        box.setMaxSize(boxSize, boxSize);
        Rectangle bg = new Rectangle(boxSize, boxSize);
        bg.setArcWidth(14); bg.setArcHeight(14);
        bg.setFill(Color.web(bgColor));
        box.getChildren().addAll(bg, faLabel(unicode, iconSize, iconColor));
        return box;
    }

    private HBox cardHeader(String title, String accentColor, String faIcon) {
        HBox h = new HBox(10);
        h.setAlignment(Pos.CENTER_LEFT);
        h.setPadding(new Insets(14, 20, 12, 20));
        h.setStyle("-fx-border-color:transparent transparent "
                + ReportesStyleKit.BORDER + " transparent;-fx-border-width:0 0 1 0;");
        Rectangle accent = new Rectangle(4, 20);
        accent.setArcWidth(4); accent.setArcHeight(4);
        accent.setFill(Color.web(accentColor));
        Label titleLbl = new Label(title);
        titleLbl.setFont(Font.font("System", FontWeight.BOLD, 14));
        titleLbl.setTextFill(Color.web("#111827"));
        h.getChildren().addAll(accent, faLabel(faIcon, 15, accentColor), titleLbl);
        return h;
    }

    private Label seccionLabel(String text) {
        Label l = new Label(text.toUpperCase());
        l.setFont(Font.font("System", FontWeight.BOLD, 10));
        l.setTextFill(Color.web(ReportesStyleKit.GRAY));
        l.setPadding(new Insets(6, 0, 0, 2));
        return l;
    }

    private HBox buildErrorState(String msg) {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(14, 20, 14, 20));
        box.setStyle("-fx-background-color:" + ReportesStyleKit.C_RED_BG + ";-fx-background-radius:10;");
        Label message = new Label("Error al cargar datos: " + (msg != null ? msg : "desconocido"));
        message.setFont(Font.font("System", 12));
        message.setTextFill(Color.web(ReportesStyleKit.C_RED));
        message.setWrapText(true);
        box.getChildren().addAll(faLabel("\uf071", 16, ReportesStyleKit.C_RED), message);
        return box;
    }

    private Label celdaFija(String txt, double width) {
        Label l = new Label(txt != null ? txt : "—");
        l.setStyle("-fx-font-size:12px;-fx-text-fill:#374151;");
        l.setPrefWidth(width); l.setMinWidth(width); l.setMaxWidth(width);
        l.setTextOverrun(OverrunStyle.ELLIPSIS);
        return l;
    }

    private HBox wrapCell(javafx.scene.Node node, double width) {
        HBox box = new HBox(node);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPrefWidth(width); box.setMinWidth(width); box.setMaxWidth(width);
        return box;
    }

    private HBox badgeBox(String icon, String texto, String color, String bgColor) {
        Label ico = faLabel(icon, 11, color);
        Label txt = new Label(" " + texto);
        txt.setStyle("-fx-font-size:11px;-fx-font-weight:bold;-fx-text-fill:" + color + ";");
        HBox box = new HBox(2, ico, txt);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(3, 9, 3, 9));
        box.setStyle("-fx-background-color:" + bgColor + ";-fx-background-radius:20;");
        return box;
    }

    private Label lbl(String text, double size, String color, boolean bold) {
        Label l = new Label(text);
        l.setFont(bold ? Font.font("System", FontWeight.BOLD, size) : Font.font("System", size));
        l.setTextFill(Color.web(color));
        return l;
    }

    private Region divider() {
        Region r = new Region();
        r.setPrefHeight(1);
        r.setStyle("-fx-background-color:" + ReportesStyleKit.BORDER + ";");
        return r;
    }

    private Label filtroLbl(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size:11px;-fx-text-fill:" + ReportesStyleKit.GRAY
                + ";-fx-font-weight:bold;");
        return l;
    }

    private VBox controlConLabel(Label label, javafx.scene.Node control, double prefW) {
        VBox vb = new VBox(4, label, control);
        vb.setAlignment(Pos.TOP_LEFT);
        if (prefW > 0 && control instanceof Region r) {
            r.setPrefWidth(prefW); r.setMinWidth(prefW);
        }
        return vb;
    }

    private ComboBox<String> comboFiltro(String placeholder) {
        ComboBox<String> cb = new ComboBox<>();
        cb.setPromptText(placeholder);
        cb.getItems().add(null);
        cb.setStyle(ReportesStyleKit.inputBase());
        cb.setMaxWidth(Double.MAX_VALUE);
        return cb;
    }

    private DatePicker datePicker(String prompt) {
        DatePicker dp = new DatePicker();
        dp.setPromptText(prompt);
        String style = "-fx-background-color:white;-fx-background-radius:10;"
                + "-fx-border-radius:10;-fx-border-color:#d1d5db;"
                + "-fx-border-width:1;-fx-padding:6 10;-fx-font-size:12px;";
        dp.setStyle(style);
        dp.getEditor().setStyle("-fx-background-color:transparent;"
                + "-fx-text-fill:#111827;-fx-font-size:12px;");
        dp.setOnMouseEntered(e -> dp.setStyle(style + "-fx-border-color:#1565c0;"));
        dp.setOnMouseExited(e  -> dp.setStyle(style));
        dp.setPrefHeight(36);
        dp.setMaxWidth(Double.MAX_VALUE);
        return dp;
    }

    private Button accionBtn(String texto, String icon, String color, String style) {
        Label ico = faLabel(icon, 12,
                color.equals(ReportesStyleKit.C_BLU) ? ReportesStyleKit.WHITE : color);
        Label txt = new Label("  " + texto);
        txt.setStyle("-fx-text-fill:"
                + (color.equals(ReportesStyleKit.C_BLU) ? "white" : color)
                + ";-fx-font-size:12px;-fx-font-weight:bold;");
        HBox content = new HBox(4, ico, txt);
        content.setAlignment(Pos.CENTER);
        Button b = new Button();
        b.setGraphic(content);
        b.setStyle(style);
        VBox.setMargin(b, new Insets(18, 0, 0, 0));
        return b;
    }
}