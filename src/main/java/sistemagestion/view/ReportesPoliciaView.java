/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.view;

/**
 *
 * @author Maria Cristina
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
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import sistemagestion.model.Alerta;
import sistemagestion.model.AtencionAlerta;
import sistemagestion.model.AsignacionUnidad;
import sistemagestion.model.EstadoAtencionAlerta;
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

public class ReportesPoliciaView {

    // ── Paleta ─────────────────────────────────────────────────────
    private static final String WHITE = "#ffffff";
    private static final String BG = "#f4f6fb";
    private static final String BORDER = "#e5e7eb";
    private static final String GRAY_TEXT = "#6b7280";

    private static final String C_RED = "#e53935";
    private static final String C_RED_BG = "#fff0f0";
    private static final String C_ORG = "#fb8c00";
    private static final String C_ORG_BG = "#fff8e1";
    private static final String C_GRN = "#43a047";
    private static final String C_GRN_BG = "#e8f5e9";
    private static final String C_BLU = "#1565c0";
    private static final String C_BLU_BG = "#e8f0fe";
    private static final String C_PUR = "#7b1fa2";
    private static final String C_PUR_BG = "#f3e5f5";
    private static final String C_TEA = "#00695c";
    private static final String C_TEA_BG = "#e0f2f1";
    private static final String C_AMB = "#f9a825";
    private static final String C_AMB_BG = "#fffde7";

    // ── Font Awesome unicode ────────────────────────────────────────
    private static final String FA_SHIELD = "\uf505";
    private static final String FA_CLIPBOARD = "\uf46d";
    private static final String FA_BELL = "\uf0f3";
    private static final String FA_CHECK = "\uf00c";
    private static final String FA_CLOCK = "\uf017";
    private static final String FA_FILTER = "\uf0b0";
    private static final String FA_TIMES = "\uf00d";
    private static final String FA_SEARCH = "\uf002";
    private static final String FA_DOWNLOAD = "\uf019";
    private static final String FA_LIST = "\uf03a";
    private static final String FA_CHART = "\uf080";
    private static final String FA_CAR = "\uf5e4";
    private static final String FA_STAR = "\uf005";
    private static final String FA_BAN = "\uf05e";
    private static final String FA_SPINNER = "\uf110";
    private static final String FA_MAP = "\uf279";
    private static final String FA_THUMBTACK = "\uf08d";
    private static final String FA_BULLHORN = "\uf0a1";
    private static final String FA_CALENDAR = "\uf073";
    private static final String FA_MAP_PIN = "\uf3c5";
    private static final String FA_REPORT = "\uf080";
    private static final String FA_USER = "\uf007";

    // ── Servicios ──────────────────────────────────────────────────
    private final AtencionAlertaService atencionService;
    private final AsignacionUnidadService asignacionService;
    private final AlertaService alertaService;
    private final NotificacionService notificacionService;
    private final AlarmaService alarmaService;

    // ── Policía logueado ───────────────────────────────────────────
    private final Policia policiaLogueado;

    // ── Paginación ─────────────────────────────────────────────────
    private static final int FILAS_POR_PAGINA = 8;
    private int paginaAtenc = 1;
    private int paginaAlertas = 1;

    // ── Datos cacheados ────────────────────────────────────────────
    private List<AtencionAlerta> atencionesCached;
    private List<AtencionAlerta> atencionesFiltradas;
    private List<AsignacionUnidad> asignacionesCached;
    private List<Alerta> alertasCached;
    private List<Alerta> alertasFiltradas;

    // ── Refs tabla atenciones ──────────────────────────────────────
    private VBox tbodyAtencRef;
    private Label lblMostrandoAtencRef;
    private HBox paginacionAtencRef;

    // ── Refs tabla alertas asignadas ───────────────────────────────
    private VBox tbodyAlertasRef;
    private Label lblMostrandoAlertasRef;
    private HBox paginacionAlertasRef;

    // ── Filtros atenciones ─────────────────────────────────────────
    private ComboBox<String> cbEstadoAtenc;
    private DatePicker dpDesdeAtenc;
    private DatePicker dpHastaAtenc;
    private TextField txtBuscarAtenc;

    // ── Filtros alertas ────────────────────────────────────────────
    private ComboBox<String> cbEstadoAlerta;
    private ComboBox<String> cbTipoAlerta;
    private ComboBox<String> cbBarrioAlerta;
    private DatePicker dpDesdeAlerta;
    private DatePicker dpHastaAlerta;
    private TextField txtBuscarAlerta;

    // ── Anchos columnas ────────────────────────────────────────────
    // Atenciones: # | Alerta ID | Estado | Fecha Atención | Descripción | Tipo Arma | Observación
    private static final double[] W_ATENC = {45, 85, 130, 155, 200, 130, 190};
    // Alertas:    # | Tipo | Barrio | Estado | Fecha y Hora | Descripción | Unidad | Atendida por
    private static final double[] W_ALERT = {45, 140, 120, 120, 155, 180, 130, 150};

    // ══════════════════════════════════════════════════════════════
    // CONSTRUCTOR
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
        this.notificacionService = notificacionService;
        this.alarmaService = alarmaService;
    }

    /**
     * Constructor de compatibilidad (sin notificaciones ni alarmas)
     */
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

        String unidadNombre = (policiaLogueado != null
                && policiaLogueado.getUnidadpolicial() != null)
                ? policiaLogueado.getUnidadpolicial().getNombre() : null;

        List<AtencionAlerta> todasAtenciones = atencionService != null ? atencionService.listar() : List.of();
        List<AsignacionUnidad> todasAsignaciones = asignacionService != null ? asignacionService.listar() : List.of();
        List<Alerta> todasAlertas = alertaService != null ? alertaService.listar() : List.of();

        if (unidadNombre != null) {
            atencionesCached = todasAtenciones.stream()
                    .filter(a -> a.getUnidad() != null
                    && unidadNombre.equalsIgnoreCase(a.getUnidad().getNombre()))
                    .collect(Collectors.toList());

            asignacionesCached = todasAsignaciones.stream()
                    .filter(a -> a.getUnidadpolicial() != null
                    && unidadNombre.equalsIgnoreCase(a.getUnidadpolicial().getNombre()))
                    .collect(Collectors.toList());

            List<Integer> idsAsignadas = asignacionesCached.stream()
                    .filter(a -> a.getAlerta() != null)
                    .map(a -> a.getAlerta().getId_alerta())
                    .distinct()
                    .collect(Collectors.toList());
            alertasCached = todasAlertas.stream()
                    .filter(a -> idsAsignadas.contains(a.getId_alerta()))
                    .collect(Collectors.toList());
        } else {
            atencionesCached = List.of();
            asignacionesCached = List.of();
            alertasCached = List.of();
        }

        atencionesFiltradas = atencionesCached;
        alertasFiltradas = alertasCached;

        VBox content = new VBox(20);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color:" + BG + ";");

        content.getChildren().add(buildTopBar());
        content.getChildren().add(buildStatsRow());

        try {
            content.getChildren().add(buildSeccionLabel("Resumen de Actividad"));
            content.getChildren().add(buildResumenCard());

            content.getChildren().add(buildSeccionLabel("Mis Atenciones de Alertas"));
            content.getChildren().add(buildFiltrosAtencCard());
            content.getChildren().add(buildTablaAtenciones());
            content.getChildren().add(buildPaginacionAtenc());

            content.getChildren().add(buildSeccionLabel("Alertas Asignadas a mi Unidad"));
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

    // ══════════════════════════════════════════════════════════════
    // TOP BAR
    // ══════════════════════════════════════════════════════════════
    private HBox buildTopBar() {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);

        StackPane titleIcon = faIconBox(FA_SHIELD, 22, C_BLU, C_BLU_BG, 44);

        String nombrePolicia = policiaLogueado != null
                ? policiaLogueado.getPrimer_nombre() + " " + policiaLogueado.getPrimer_apellido()
                : "Policía";
        String unidad = (policiaLogueado != null
                && policiaLogueado.getUnidadpolicial() != null)
                ? policiaLogueado.getUnidadpolicial().getNombre() : "Sin unidad";

        VBox titles = new VBox(4);
        Label title = new Label("Mis Reportes de Actividad");
        title.setFont(Font.font("System", FontWeight.BOLD, 26));
        title.setTextFill(Color.web("#111827"));
        Label sub = lbl("Oficial: " + nombrePolicia + "  ·  Unidad: " + unidad, 13, GRAY_TEXT, false);
        titles.getChildren().addAll(title, sub);

        HBox titleRow = new HBox(14, titleIcon, titles);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        HBox right = new HBox(12);
        right.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(right, Priority.ALWAYS);

        Button btnExportAtenc = buildExportBtn("Exportar Atenciones");
        Button btnExportAlertas = buildExportBtn("Exportar Alertas");
        btnExportAtenc.setOnAction(e -> exportarAtenciones(btnExportAtenc));
        btnExportAlertas.setOnAction(e -> exportarAlertasAsignadas(btnExportAlertas));

        right.getChildren().addAll(btnExportAtenc, btnExportAlertas);
        bar.getChildren().addAll(titleRow, right);
        return bar;
    }

    private Button buildExportBtn(String texto) {
        Button b = new Button();
        Label ico = faLabel(FA_DOWNLOAD, 13, WHITE);
        Label txt = new Label("  " + texto);
        txt.setStyle("-fx-text-fill:white;-fx-font-size:12px;-fx-font-weight:bold;");
        HBox content = new HBox(4, ico, txt);
        content.setAlignment(Pos.CENTER);
        b.setGraphic(content);
        String base = "-fx-background-color:linear-gradient(to right,#16283d,#1f3a56);"
                + "-fx-background-radius:8;-fx-padding:10 18;-fx-cursor:hand;";
        String hover = "-fx-background-color:linear-gradient(to right,#0f1e30,#16283d);"
                + "-fx-background-radius:8;-fx-padding:10 18;-fx-cursor:hand;";
        b.setStyle(base);
        b.setOnMouseEntered(e -> b.setStyle(hover));
        b.setOnMouseExited(e -> b.setStyle(base));
        return b;
    }

    // ══════════════════════════════════════════════════════════════
    // STATS ROW
    // ══════════════════════════════════════════════════════════════
    private HBox buildStatsRow() {
        HBox row = new HBox(16);
        HBox.setHgrow(row, Priority.ALWAYS);

        long totalAtenc = atencionesCached.size();
        long finalizadas = atencionesCached.stream()
                .filter(a -> a.getEstado() == EstadoAtencionAlerta.FINALIZADA).count();
        long enProceso = atencionesCached.stream()
                .filter(a -> a.getEstado() == EstadoAtencionAlerta.EN_PROCESO).count();
        long canceladas = atencionesCached.stream()
                .filter(a -> a.getEstado() == EstadoAtencionAlerta.CANCELADA).count();
        long totalAlertas = alertasCached.size();
        long totalAsig = asignacionesCached.size();

        row.getChildren().addAll(
                statCard(C_BLU_BG, C_BLU, FA_CLIPBOARD, "Mis Atenciones", totalAtenc, "Total de atenciones registradas"),
                statCard(C_GRN_BG, C_GRN, FA_CHECK, "Finalizadas", finalizadas, "Atenciones resueltas"),
                statCard(C_ORG_BG, C_ORG, FA_SPINNER, "En Proceso", enProceso, "Atenciones activas"),
                statCard(C_RED_BG, C_RED, FA_BAN, "Canceladas", canceladas, "Atenciones canceladas"),
                statCard(C_PUR_BG, C_PUR, FA_BELL, "Alertas Asignadas", totalAlertas, "De mi unidad"),
                statCard(C_TEA_BG, C_TEA, FA_MAP, "Despachos", totalAsig, "Asignaciones de unidad")
        );
        return row;
    }

    private VBox statCard(String bgIcon, String accent, String faIcon,
            String title, long value, String sub) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(18, 20, 18, 20));
        card.setStyle("-fx-background-color:white;-fx-background-radius:18;");
        HBox.setHgrow(card, Priority.ALWAYS);
        shadow(card);

        StackPane iconWrap = faIconBox(faIcon, 20, accent, bgIcon, 48);

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
    // TARJETA RESUMEN
    // ══════════════════════════════════════════════════════════════
    private VBox buildResumenCard() {
        long finalizadas = atencionesCached.stream()
                .filter(a -> a.getEstado() == EstadoAtencionAlerta.FINALIZADA).count();
        long pendientes = atencionesCached.stream()
                .filter(a -> a.getEstado() == EstadoAtencionAlerta.PENDIENTE).count();
        long enProceso = atencionesCached.stream()
                .filter(a -> a.getEstado() == EstadoAtencionAlerta.EN_PROCESO).count();
        long canceladas = atencionesCached.stream()
                .filter(a -> a.getEstado() == EstadoAtencionAlerta.CANCELADA).count();
        long totalAsig = asignacionesCached.size();

        String tipoFrecuente = alertasCached.stream()
                .filter(a -> a.getTipoalerta() != null)
                .collect(Collectors.groupingBy(
                        a -> a.getTipoalerta().getNombre(), Collectors.counting()))
                .entrySet().stream()
                .max(java.util.Map.Entry.comparingByValue())
                .map(java.util.Map.Entry::getKey)
                .orElse("—");

        VBox card = new VBox(0);
        card.setStyle("-fx-background-color:white;-fx-background-radius:14;");
        shadow(card);
        card.getChildren().add(cardHeaderBox("Desglose de mis atenciones", C_BLU, FA_CHART));

        String[][] items = {
            {FA_CHECK, "Atenciones finalizadas", String.valueOf(finalizadas), C_GRN, C_GRN_BG},
            {FA_SPINNER, "Atenciones en proceso", String.valueOf(enProceso), C_ORG, C_ORG_BG},
            {FA_CLOCK, "Atenciones pendientes", String.valueOf(pendientes), C_AMB, C_AMB_BG},
            {FA_BAN, "Atenciones canceladas", String.valueOf(canceladas), C_RED, C_RED_BG},
            {FA_THUMBTACK, "Total despachos de mi unidad", String.valueOf(totalAsig), C_TEA, C_TEA_BG},
            {FA_STAR, "Tipo de alerta más frecuente", tipoFrecuente, C_PUR, C_PUR_BG},};

        for (int i = 0; i < items.length; i++) {
            card.getChildren().add(
                    buildDetailRow(items[i][0], items[i][1], items[i][2], items[i][3], items[i][4]));
            if (i < items.length - 1) {
                card.getChildren().add(divider());
            }
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
        row.setOnMouseExited(e -> row.setStyle("-fx-background-color:transparent;"));

        StackPane iconBox = faIconBox(faIcon, 17, color, bgColor, 38);
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
    // FILTROS ATENCIONES
    // ══════════════════════════════════════════════════════════════
    private VBox buildFiltrosAtencCard() {
        VBox card = new VBox(0);
        card.setStyle("-fx-background-color:white;-fx-background-radius:14;");
        shadow(card);
        card.getChildren().add(cardHeaderBox("Filtrar mis atenciones", C_BLU, FA_FILTER));

        cbEstadoAtenc = comboFiltro("Todos los estados");
        cbEstadoAtenc.getItems().addAll("PENDIENTE", "EN_PROCESO", "FINALIZADA", "CANCELADA");

        txtBuscarAtenc = new TextField();
        txtBuscarAtenc.setPromptText("Buscar en descripción u observación…");
        txtBuscarAtenc.setStyle(estiloInput());
        HBox.setHgrow(txtBuscarAtenc, Priority.ALWAYS);

        dpDesdeAtenc = datePicker("Desde…");
        dpHastaAtenc = datePicker("Hasta…");

        Button btnAplicar = accionBtn("Aplicar filtros", FA_FILTER, C_BLU,
                "-fx-background-color:" + C_BLU + ";-fx-text-fill:white;"
                + "-fx-background-radius:8;-fx-padding:8 18;-fx-cursor:hand;-fx-font-weight:bold;");
        btnAplicar.setOnAction(e -> aplicarFiltrosAtenc());

        Button btnLimpiar = accionBtn("Limpiar", FA_TIMES, C_RED,
                "-fx-background-color:" + C_RED_BG + ";-fx-text-fill:" + C_RED + ";"
                + "-fx-background-radius:8;-fx-padding:8 18;-fx-cursor:hand;-fx-font-weight:bold;");
        btnLimpiar.setOnAction(e -> limpiarFiltrosAtenc());

        HBox fila1 = new HBox(16,
                labeledControl(labelFiltro("Estado:"), cbEstadoAtenc, 140),
                labeledControl(labelFiltro("Buscar:"), txtBuscarAtenc, -1)
        );
        fila1.setAlignment(Pos.CENTER_LEFT);
        fila1.setPadding(new Insets(14, 20, 10, 20));

        HBox fila2 = new HBox(16,
                labeledControl(labelFiltro("Desde:"), dpDesdeAtenc, 160),
                labeledControl(labelFiltro("Hasta:"), dpHastaAtenc, 160),
                btnAplicar, btnLimpiar
        );
        fila2.setAlignment(Pos.CENTER_LEFT);
        fila2.setPadding(new Insets(0, 20, 14, 20));

        card.getChildren().addAll(fila1, fila2);
        return card;
    }

    private void aplicarFiltrosAtenc() {
        String estado = cbEstadoAtenc.getValue();
        String buscar = txtBuscarAtenc.getText() != null
                ? txtBuscarAtenc.getText().trim().toLowerCase() : "";
        LocalDate desde = dpDesdeAtenc.getValue();
        LocalDate hasta = dpHastaAtenc.getValue();

        atencionesFiltradas = atencionesCached.stream()
                .filter(a -> estado == null
                || (a.getEstado() != null && estado.equals(a.getEstado().name())))
                .filter(a -> buscar.isEmpty()
                || (a.getDescripcion() != null && a.getDescripcion().toLowerCase().contains(buscar))
                || (a.getObservacion() != null && a.getObservacion().toLowerCase().contains(buscar)))
                .filter(a -> {
                    if (desde == null && hasta == null) {
                        return true;
                    }
                    LocalDateTime fh = a.getFechaatencion();
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
                })
                .collect(Collectors.toList());

        paginaAtenc = 1;
        renderizarAtenciones(W_ATENC);
    }

    private void limpiarFiltrosAtenc() {
        cbEstadoAtenc.setValue(null);
        txtBuscarAtenc.clear();
        dpDesdeAtenc.setValue(null);
        dpHastaAtenc.setValue(null);
        atencionesFiltradas = atencionesCached;
        paginaAtenc = 1;
        renderizarAtenciones(W_ATENC);
    }

    // ══════════════════════════════════════════════════════════════
    // TABLA ATENCIONES
    // ══════════════════════════════════════════════════════════════
    private VBox buildTablaAtenciones() {
        VBox card = new VBox(0);
        card.setStyle("-fx-background-color:white;-fx-background-radius:14;");
        shadow(card);
        card.getChildren().add(cardHeaderBox("Listado de mis Atenciones", C_BLU, FA_CLIPBOARD));

        String[] cols = {"#", "Alerta", "Estado", "Fecha Atención", "Descripción", "Tipo Arma", "Observación"};
        card.getChildren().add(buildThead(cols, W_ATENC));

        tbodyAtencRef = new VBox(0);
        renderizarAtenciones(W_ATENC);

        lblMostrandoAtencRef = lbl("", 12, GRAY_TEXT, false);
        card.getChildren().addAll(tbodyAtencRef, buildFooter(lblMostrandoAtencRef));
        actualizarLblAtenc();
        return card;
    }

    private void renderizarAtenciones(double[] w) {
        tbodyAtencRef.getChildren().clear();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        int total = atencionesFiltradas.size();
        int desde = (paginaAtenc - 1) * FILAS_POR_PAGINA;
        int hasta = Math.min(desde + FILAS_POR_PAGINA, total);

        if (total == 0) {
            tbodyAtencRef.getChildren().add(buildVacioState("No hay atenciones que coincidan con los filtros"));
            actualizarLblAtenc();
            actualizarPaginacionAtenc(w);
            return;
        }

        for (int i = desde; i < hasta; i++) {
            AtencionAlerta at = atencionesFiltradas.get(i);
            boolean par = i % 2 == 0;

            String alertaId = at.getAlerta() != null ? "Alerta #" + at.getAlerta().getId_alerta() : "—";
            String estado = at.getEstado() != null ? at.getEstado().name() : "—";
            String fecha = at.getFechaatencion() != null ? at.getFechaatencion().format(fmt) : "—";
            String desc = at.getDescripcion() != null ? at.getDescripcion() : "—";
            String tipoArma = at.getTipoarma() != null ? at.getTipoarma().getNombre() : "—";
            String obs = at.getObservacion() != null ? at.getObservacion() : "—";

            HBox fila = buildFilaBase(par);
            fila.setOnMouseEntered(e -> fila.setStyle(filaBgHover(C_BLU_BG)));
            fila.setOnMouseExited(e -> fila.setStyle(filaBgNormal(par)));

            fila.getChildren().add(buildNumBadge(i + 1, C_BLU_BG, C_BLU, w[0]));
            fila.getChildren().add(celdaFija(alertaId, w[1]));
            fila.getChildren().add(hboxCell(buildEstadoAtencBadge(estado), w[2]));
            fila.getChildren().add(hboxCell(buildFechaBox(fecha), w[3]));
            fila.getChildren().add(celdaFija(desc, w[4]));
            fila.getChildren().add(celdaFija(tipoArma, w[5]));
            fila.getChildren().add(celdaFija(obs, w[6]));

            tbodyAtencRef.getChildren().add(fila);
        }

        actualizarLblAtenc();
        actualizarPaginacionAtenc(w);
    }

    private void actualizarLblAtenc() {
        if (lblMostrandoAtencRef == null) {
            return;
        }
        int total = atencionesFiltradas != null ? atencionesFiltradas.size() : 0;
        int desde = (paginaAtenc - 1) * FILAS_POR_PAGINA;
        int hasta = Math.min(desde + FILAS_POR_PAGINA, total);
        lblMostrandoAtencRef.setText("Mostrando " + (total == 0 ? 0 : desde + 1)
                + " – " + hasta + " de " + total + " atenciones"
                + (total < atencionesCached.size()
                ? " (filtradas de " + atencionesCached.size() + " totales)" : ""));
    }

    // ══════════════════════════════════════════════════════════════
    // FILTROS ALERTAS ASIGNADAS
    // ══════════════════════════════════════════════════════════════
    private VBox buildFiltrosAlertasCard() {
        VBox card = new VBox(0);
        card.setStyle("-fx-background-color:white;-fx-background-radius:14;");
        shadow(card);
        card.getChildren().add(cardHeaderBox("Filtrar alertas asignadas", C_PUR, FA_FILTER));

        cbTipoAlerta = comboFiltro("Todos los tipos");
        alertasCached.stream()
                .filter(a -> a.getTipoalerta() != null)
                .map(a -> a.getTipoalerta().getNombre())
                .distinct().sorted()
                .forEach(t -> cbTipoAlerta.getItems().add(t));

        cbEstadoAlerta = comboFiltro("Todos los estados");
        alertasCached.stream()
                .filter(a -> a.getEstado() != null)
                .map(a -> a.getEstado().name())
                .distinct().sorted()
                .forEach(s -> cbEstadoAlerta.getItems().add(s));

        cbBarrioAlerta = comboFiltro("Todos los barrios");
        alertasCached.stream()
                .filter(a -> a.getBarrio() != null)
                .map(a -> a.getBarrio().getNombre())
                .distinct().sorted()
                .forEach(b -> cbBarrioAlerta.getItems().add(b));

        txtBuscarAlerta = new TextField();
        txtBuscarAlerta.setPromptText("Buscar en descripción…");
        txtBuscarAlerta.setStyle(estiloInput());
        HBox.setHgrow(txtBuscarAlerta, Priority.ALWAYS);

        dpDesdeAlerta = datePicker("Desde…");
        dpHastaAlerta = datePicker("Hasta…");

        Button btnAplicar = accionBtn("Aplicar filtros", FA_FILTER, C_PUR,
                "-fx-background-color:" + C_PUR + ";-fx-text-fill:white;"
                + "-fx-background-radius:8;-fx-padding:8 18;-fx-cursor:hand;-fx-font-weight:bold;");
        btnAplicar.setOnAction(e -> aplicarFiltrosAlertas());

        Button btnLimpiar = accionBtn("Limpiar", FA_TIMES, C_RED,
                "-fx-background-color:" + C_RED_BG + ";-fx-text-fill:" + C_RED + ";"
                + "-fx-background-radius:8;-fx-padding:8 18;-fx-cursor:hand;-fx-font-weight:bold;");
        btnLimpiar.setOnAction(e -> limpiarFiltrosAlertas());

        HBox fila1 = new HBox(16,
                labeledControl(labelFiltro("Tipo:"), cbTipoAlerta, 130),
                labeledControl(labelFiltro("Estado:"), cbEstadoAlerta, 130),
                labeledControl(labelFiltro("Barrio:"), cbBarrioAlerta, 150),
                labeledControl(labelFiltro("Buscar:"), txtBuscarAlerta, -1)
        );
        fila1.setAlignment(Pos.CENTER_LEFT);
        fila1.setPadding(new Insets(14, 20, 10, 20));

        HBox fila2 = new HBox(16,
                labeledControl(labelFiltro("Desde:"), dpDesdeAlerta, 160),
                labeledControl(labelFiltro("Hasta:"), dpHastaAlerta, 160),
                btnAplicar, btnLimpiar
        );
        fila2.setAlignment(Pos.CENTER_LEFT);
        fila2.setPadding(new Insets(0, 20, 14, 20));

        card.getChildren().addAll(fila1, fila2);
        return card;
    }

    private void aplicarFiltrosAlertas() {
        String tipo = cbTipoAlerta.getValue();
        String estado = cbEstadoAlerta.getValue();
        String barrio = cbBarrioAlerta.getValue();
        String buscar = txtBuscarAlerta.getText() != null
                ? txtBuscarAlerta.getText().trim().toLowerCase() : "";
        LocalDate desde = dpDesdeAlerta.getValue();
        LocalDate hasta = dpHastaAlerta.getValue();

        alertasFiltradas = alertasCached.stream()
                .filter(a -> tipo == null || (a.getTipoalerta() != null && tipo.equals(a.getTipoalerta().getNombre())))
                .filter(a -> estado == null || (a.getEstado() != null && estado.equals(a.getEstado().name())))
                .filter(a -> barrio == null || (a.getBarrio() != null && barrio.equals(a.getBarrio().getNombre())))
                .filter(a -> buscar.isEmpty() || (a.getDescripcion() != null
                && a.getDescripcion().toLowerCase().contains(buscar)))
                .filter(a -> {
                    if (desde == null && hasta == null) {
                        return true;
                    }
                    LocalDateTime fh = a.getFechaHora();
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
                })
                .collect(Collectors.toList());

        paginaAlertas = 1;
        renderizarAlertasAsignadas(W_ALERT);
    }

    private void limpiarFiltrosAlertas() {
        cbTipoAlerta.setValue(null);
        cbEstadoAlerta.setValue(null);
        cbBarrioAlerta.setValue(null);
        txtBuscarAlerta.clear();
        dpDesdeAlerta.setValue(null);
        dpHastaAlerta.setValue(null);
        alertasFiltradas = alertasCached;
        paginaAlertas = 1;
        renderizarAlertasAsignadas(W_ALERT);
    }

    // ══════════════════════════════════════════════════════════════
    // TABLA ALERTAS ASIGNADAS
    // ══════════════════════════════════════════════════════════════
    private VBox buildTablaAlertasAsignadas() {
        VBox card = new VBox(0);
        card.setStyle("-fx-background-color:white;-fx-background-radius:14;");
        shadow(card);
        card.getChildren().add(cardHeaderBox("Alertas Asignadas a mi Unidad", C_PUR, FA_LIST));

        String[] cols = {"#", "Tipo", "Barrio", "Estado", "Fecha y Hora", "Descripción", "Unidad", "Atendida por"};
        card.getChildren().add(buildThead(cols, W_ALERT));

        tbodyAlertasRef = new VBox(0);
        renderizarAlertasAsignadas(W_ALERT);

        lblMostrandoAlertasRef = lbl("", 12, GRAY_TEXT, false);
        card.getChildren().addAll(tbodyAlertasRef, buildFooter(lblMostrandoAlertasRef));
        actualizarLblAlertas();
        return card;
    }

    private void renderizarAlertasAsignadas(double[] w) {
        tbodyAlertasRef.getChildren().clear();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        int total = alertasFiltradas.size();
        int desde = (paginaAlertas - 1) * FILAS_POR_PAGINA;
        int hasta = Math.min(desde + FILAS_POR_PAGINA, total);

        if (total == 0) {
            tbodyAlertasRef.getChildren().add(buildVacioState("No hay alertas asignadas que coincidan con los filtros"));
            actualizarLblAlertas();
            actualizarPaginacionAlertas(w);
            return;
        }

        for (int i = desde; i < hasta; i++) {
            Alerta al = alertasFiltradas.get(i);
            boolean par = i % 2 == 0;

            String tipo = al.getTipoalerta() != null ? al.getTipoalerta().getNombre() : "—";
            String barrio = al.getBarrio() != null ? al.getBarrio().getNombre() : "—";
            String estado = al.getEstado() != null ? al.getEstado().name() : "—";
            String fecha = al.getFechaHora() != null ? al.getFechaHora().format(fmt) : "—";
            String desc = al.getDescripcion() != null ? al.getDescripcion() : "—";
            String unidad = (policiaLogueado != null && policiaLogueado.getUnidadpolicial() != null)
                    ? policiaLogueado.getUnidadpolicial().getNombre() : "—";

            // ── FIX: usar aten.getPolicia() directamente ──────────
            final int idAl = al.getId_alerta();
            AtencionAlerta aten = atencionesCached.stream()
                    .filter(a -> a.getAlerta() != null && a.getAlerta().getId_alerta() == idAl)
                    .findFirst().orElse(null);
            String nombreAtendio = "Sin atender";
            if (aten != null && aten.getPolicia() != null) {
                Policia p = aten.getPolicia();
                nombreAtendio = p.getPrimer_nombre() + " " + p.getPrimer_apellido();
            }

            HBox fila = buildFilaBase(par);
            fila.setOnMouseEntered(e -> fila.setStyle(filaBgHover(C_PUR_BG)));
            fila.setOnMouseExited(e -> fila.setStyle(filaBgNormal(par)));

            // Col 1: ID badge
            StackPane idBadge = new StackPane();
            Circle idCircle = new Circle(16, Color.web(C_PUR_BG));
            Label idLbl = lbl(String.valueOf(al.getId_alerta()), 11, C_PUR, true);
            idBadge.getChildren().addAll(idCircle, idLbl);
            HBox idBox = new HBox(idBadge);
            idBox.setAlignment(Pos.CENTER_LEFT);
            idBox.setPrefWidth(w[0]);
            idBox.setMinWidth(w[0]);

            // Col 2: Tipo badge
            Label tipoIco = faLabel(tipoIcon(tipo), 11, C_RED);
            Label tipoTxt = new Label(" " + tipo);
            tipoTxt.setStyle("-fx-font-size:11px;-fx-font-weight:bold;-fx-text-fill:" + C_RED + ";");
            HBox tipoBadgeBox = new HBox(2, tipoIco, tipoTxt);
            tipoBadgeBox.setAlignment(Pos.CENTER_LEFT);
            tipoBadgeBox.setPadding(new Insets(3, 9, 3, 9));
            tipoBadgeBox.setStyle("-fx-background-color:" + C_RED_BG + ";-fx-background-radius:20;");
            HBox tipoBox = hboxCell(tipoBadgeBox, w[1]);

            // Col 3: Barrio
            Label barrioLbl = celdaFija(barrio, w[2]);

            // Col 4: Estado badge
            Label estadoIco = faLabel(estadoIcon(estado), 11, estadoColor(estado));
            Label estadoTxt = new Label(" " + estado);
            estadoTxt.setStyle("-fx-font-size:11px;-fx-font-weight:bold;-fx-text-fill:" + estadoColor(estado) + ";");
            HBox estadoBadgeBox = new HBox(2, estadoIco, estadoTxt);
            estadoBadgeBox.setAlignment(Pos.CENTER_LEFT);
            estadoBadgeBox.setPadding(new Insets(3, 9, 3, 9));
            estadoBadgeBox.setStyle("-fx-background-color:" + estadoBg(estado) + ";-fx-background-radius:20;");
            HBox estadoBox = hboxCell(estadoBadgeBox, w[3]);

            // Col 5: Fecha con ícono reloj
            Label fechaIco = faLabel("\uf017", 11, GRAY_TEXT);
            Label fechaTxt = new Label(" " + fecha);
            fechaTxt.setStyle("-fx-font-size:11px;-fx-text-fill:#374151;");
            HBox fechaInner = new HBox(3, fechaIco, fechaTxt);
            fechaInner.setAlignment(Pos.CENTER_LEFT);
            HBox fechaBox = hboxCell(fechaInner, w[4]);

            // Col 8: Atendida por — badge con ícono de persona
            HBox atendidaBox = buildAtendidaBadge(nombreAtendio, w[7]);

            fila.getChildren().addAll(
                    idBox, tipoBox, barrioLbl, estadoBox, fechaBox,
                    celdaFija(desc, w[5]), celdaFija(unidad, w[6]), atendidaBox
            );

            tbodyAlertasRef.getChildren().add(fila);
        }

        actualizarLblAlertas();
        actualizarPaginacionAlertas(w);
    }

    // ── Badge "Atendida por" ──────────────────────────────────────
    private HBox buildAtendidaBadge(String nombre, double width) {
        boolean sinAtender = "Sin atender".equals(nombre);
        String color = sinAtender ? GRAY_TEXT : C_TEA;
        String bg = sinAtender ? "#f3f4f6" : C_TEA_BG;
        String icon = sinAtender ? FA_CLOCK : FA_USER;

        Label ico = faLabel(icon, 10, color);
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
        int total = alertasFiltradas != null ? alertasFiltradas.size() : 0;
        int desde = (paginaAlertas - 1) * FILAS_POR_PAGINA;
        int hasta = Math.min(desde + FILAS_POR_PAGINA, total);
        lblMostrandoAlertasRef.setText("Mostrando " + (total == 0 ? 0 : desde + 1)
                + " – " + hasta + " de " + total + " alertas asignadas"
                + (total < alertasCached.size()
                ? " (filtradas de " + alertasCached.size() + " totales)" : ""));
    }

    // ══════════════════════════════════════════════════════════════
    // PAGINACIÓN ATENCIONES
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
        int total = atencionesFiltradas != null ? atencionesFiltradas.size() : 0;
        int totalPags = (int) Math.ceil((double) total / FILAS_POR_PAGINA);
        if (totalPags <= 1) {
            return;
        }

        paginacionAtencRef.getChildren().add(btnPagNav("\uf104", paginaAtenc > 1, () -> {
            paginaAtenc--;
            renderizarAtenciones(w);
        }, C_BLU));

        int ini = Math.max(1, paginaAtenc - 2);
        int fin = Math.min(totalPags, paginaAtenc + 2);
        if (ini > 1) {
            paginacionAtencRef.getChildren().add(btnPagNum(1, w, true));
            if (ini > 2) {
                paginacionAtencRef.getChildren().add(puntos());
            }
        }
        for (int i = ini; i <= fin; i++) {
            paginacionAtencRef.getChildren().add(btnPagNum(i, w, true));
        }
        if (fin < totalPags) {
            if (fin < totalPags - 1) {
                paginacionAtencRef.getChildren().add(puntos());
            }
            paginacionAtencRef.getChildren().add(btnPagNum(totalPags, w, true));
        }
        paginacionAtencRef.getChildren().add(btnPagNav("\uf105", paginaAtenc < totalPags, () -> {
            paginaAtenc++;
            renderizarAtenciones(w);
        }, C_BLU));
    }

    // ══════════════════════════════════════════════════════════════
    // PAGINACIÓN ALERTAS
    // ══════════════════════════════════════════════════════════════
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
        int total = alertasFiltradas != null ? alertasFiltradas.size() : 0;
        int totalPags = (int) Math.ceil((double) total / FILAS_POR_PAGINA);
        if (totalPags <= 1) {
            return;
        }

        paginacionAlertasRef.getChildren().add(btnPagNav("\uf104", paginaAlertas > 1, () -> {
            paginaAlertas--;
            renderizarAlertasAsignadas(w);
        }, C_PUR));

        int ini = Math.max(1, paginaAlertas - 2);
        int fin = Math.min(totalPags, paginaAlertas + 2);
        if (ini > 1) {
            paginacionAlertasRef.getChildren().add(btnPagNum(1, w, false));
            if (ini > 2) {
                paginacionAlertasRef.getChildren().add(puntos());
            }
        }
        for (int i = ini; i <= fin; i++) {
            paginacionAlertasRef.getChildren().add(btnPagNum(i, w, false));
        }
        if (fin < totalPags) {
            if (fin < totalPags - 1) {
                paginacionAlertasRef.getChildren().add(puntos());
            }
            paginacionAlertasRef.getChildren().add(btnPagNum(totalPags, w, false));
        }
        paginacionAlertasRef.getChildren().add(btnPagNav("\uf105", paginaAlertas < totalPags, () -> {
            paginaAlertas++;
            renderizarAlertasAsignadas(w);
        }, C_PUR));
    }

    private Button btnPagNum(int pg, double[] w, boolean esAtenc) {
        boolean esActual = esAtenc ? pg == paginaAtenc : pg == paginaAlertas;
        String accent = esAtenc ? C_BLU : C_PUR;
        String accentBg = esAtenc ? C_BLU_BG : C_PUR_BG;
        Button b = new Button(String.valueOf(pg));
        b.setPrefSize(34, 34);
        b.setMinSize(34, 34);
        b.setMaxSize(34, 34);
        b.setFont(Font.font("System", esActual ? FontWeight.BOLD : FontWeight.NORMAL, 13));
        String base = esActual
                ? "-fx-background-color:" + accent + ";-fx-text-fill:white;-fx-background-radius:8;"
                + "-fx-cursor:hand;-fx-border-color:" + accent + ";-fx-border-radius:8;-fx-border-width:1.5;"
                : "-fx-background-color:" + WHITE + ";-fx-text-fill:#374151;-fx-background-radius:8;"
                + "-fx-cursor:hand;-fx-border-color:" + BORDER + ";-fx-border-radius:8;-fx-border-width:1.5;";
        String hover = "-fx-background-color:" + accentBg + ";-fx-text-fill:" + accent + ";"
                + "-fx-background-radius:8;-fx-cursor:hand;-fx-border-color:" + accent + ";"
                + "-fx-border-radius:8;-fx-border-width:1.5;";
        b.setStyle(base);
        if (!esActual) {
            b.setOnMouseEntered(e -> b.setStyle(hover));
            b.setOnMouseExited(e -> b.setStyle(base));
        }
        b.setOnAction(e -> {
            if (esAtenc) {
                paginaAtenc = pg;
                renderizarAtenciones(w);
            } else {
                paginaAlertas = pg;
                renderizarAlertasAsignadas(w);
            }
        });
        return b;
    }

    private Button btnPagNav(String ico, boolean enabled, Runnable accion, String accent) {
        Label l = new Label(ico);
        l.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';-fx-font-size:13px;"
                + "-fx-text-fill:" + (enabled ? "#374151" : "#d1d5db") + ";");
        Button b = new Button();
        b.setGraphic(l);
        b.setPrefSize(34, 34);
        b.setMinSize(34, 34);
        b.setMaxSize(34, 34);
        b.setDisable(!enabled);
        String accentBg = accent.equals(C_BLU) ? C_BLU_BG : C_PUR_BG;
        String base = "-fx-background-color:" + WHITE + ";-fx-background-radius:8;"
                + "-fx-border-color:" + BORDER + ";-fx-border-radius:8;-fx-border-width:1.5;";
        String hover = "-fx-background-color:" + accentBg + ";-fx-background-radius:8;"
                + "-fx-border-color:" + accent + ";-fx-border-radius:8;-fx-border-width:1.5;";
        b.setStyle(base);
        if (enabled) {
            b.setOnMouseEntered(e -> b.setStyle(hover));
            b.setOnMouseExited(e -> b.setStyle(base));
        }
        b.setOnAction(e -> accion.run());
        return b;
    }

    private Label puntos() {
        Label l = new Label("…");
        l.setStyle("-fx-font-size:14px;-fx-text-fill:" + GRAY_TEXT + ";");
        l.setPadding(new Insets(0, 4, 0, 4));
        return l;
    }

    // ══════════════════════════════════════════════════════════════
    // EXPORTAR ATENCIONES
    // ══════════════════════════════════════════════════════════════
    private void exportarAtenciones(javafx.scene.Node owner) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Guardar reporte de atenciones");
        fc.setInitialFileName("Reporte_MisAtenciones_WolertApp.xlsx");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel", "*.xlsx"));
        File file = fc.showSaveDialog(owner.getScene().getWindow());
        if (file == null) {
            return;
        }

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("MisAtenciones");
            CellStyle hStyle = excelHeaderStyle(wb);
            CellStyle nStyle = excelNormalStyle(wb);

            String[] headers = {"#", "ID Alerta", "Estado", "Fecha Atención",
                "Descripción", "Tipo Arma", "Medio Transporte", "Observación", "Unidad"};
            Row hr = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                var c = hr.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(hStyle);
            }

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            int rn = 1;
            for (AtencionAlerta at : atencionesFiltradas) {
                String[] vals = {
                    String.valueOf(rn),
                    at.getAlerta() != null ? String.valueOf(at.getAlerta().getId_alerta()) : "",
                    at.getEstado() != null ? at.getEstado().name() : "",
                    at.getFechaatencion() != null ? at.getFechaatencion().format(fmt) : "",
                    nvl(at.getDescripcion()),
                    at.getTipoarma() != null ? at.getTipoarma().getNombre() : "",
                    at.getMediotransporte() != null ? at.getMediotransporte().getNombre() : "",
                    nvl(at.getObservacion()),
                    at.getUnidad() != null ? at.getUnidad().getNombre() : ""
                };
                Row row = sheet.createRow(rn++);
                for (int i = 0; i < vals.length; i++) {
                    var c = row.createCell(i);
                    c.setCellValue(vals[i]);
                    c.setCellStyle(nStyle);
                }
            }
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            try (FileOutputStream fos = new FileOutputStream(file)) {
                wb.write(fos);
            }
            showExportSuccess("Reporte de Atenciones exportado", file.getName(), file.getAbsolutePath());
        } catch (Exception ex) {
            showExportError(ex.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════
    // EXPORTAR ALERTAS ASIGNADAS
    // ══════════════════════════════════════════════════════════════
    private void exportarAlertasAsignadas(javafx.scene.Node owner) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Guardar reporte de alertas asignadas");
        fc.setInitialFileName("Reporte_MisAlertasAsignadas_WolertApp.xlsx");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel", "*.xlsx"));
        File file = fc.showSaveDialog(owner.getScene().getWindow());
        if (file == null) {
            return;
        }

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("AlertasAsignadas");
            CellStyle hStyle = excelHeaderStyle(wb);
            CellStyle nStyle = excelNormalStyle(wb);

            String[] headers = {"#", "ID Alerta", "Tipo", "Barrio", "Estado",
                "Fecha y Hora", "Descripción", "Latitud", "Longitud",
                "Unidad Asignada", "Atendida por"};
            Row hr = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                var c = hr.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(hStyle);
            }

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            String unidadNom = (policiaLogueado != null && policiaLogueado.getUnidadpolicial() != null)
                    ? policiaLogueado.getUnidadpolicial().getNombre() : "";
            int rn = 1;
            for (Alerta al : alertasFiltradas) {
                final int idAl = al.getId_alerta();
                AtencionAlerta aten = atencionesCached.stream()
                        .filter(a -> a.getAlerta() != null && a.getAlerta().getId_alerta() == idAl)
                        .findFirst().orElse(null);
                // ── FIX exportación: usar getPolicia() directamente ──
                String polNombre = "Sin atender";
                if (aten != null && aten.getPolicia() != null) {
                    Policia p = aten.getPolicia();
                    polNombre = p.getPrimer_nombre() + " " + p.getPrimer_apellido();
                }
                String[] vals = {
                    String.valueOf(rn),
                    String.valueOf(al.getId_alerta()),
                    al.getTipoalerta() != null ? al.getTipoalerta().getNombre() : "",
                    al.getBarrio() != null ? al.getBarrio().getNombre() : "",
                    al.getEstado() != null ? al.getEstado().name() : "",
                    al.getFechaHora() != null ? al.getFechaHora().format(fmt) : "",
                    nvl(al.getDescripcion()),
                    String.valueOf(al.getLatitud()),
                    String.valueOf(al.getLongitud()),
                    unidadNom,
                    polNombre
                };
                Row row = sheet.createRow(rn++);
                for (int i = 0; i < vals.length; i++) {
                    var c = row.createCell(i);
                    c.setCellValue(vals[i]);
                    c.setCellStyle(nStyle);
                }
            }
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            try (FileOutputStream fos = new FileOutputStream(file)) {
                wb.write(fos);
            }
            showExportSuccess("Reporte de Alertas Asignadas exportado", file.getName(), file.getAbsolutePath());
        } catch (Exception ex) {
            showExportError(ex.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════
    // DIÁLOGOS DE EXPORTACIÓN MEJORADOS
    // ══════════════════════════════════════════════════════════════
    /**
     * Ventana de éxito al exportar — diseño tipo card con icono, nombre de
     * archivo y ruta.
     */
    private void showExportSuccess(String titulo, String nombreArchivo, String rutaCompleta) {
        javafx.stage.Stage dialog = new javafx.stage.Stage();
        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialog.setTitle("Exportación exitosa");
        dialog.setResizable(false);

        // ── Icono animado (círculo verde + check) ─────────────────
        Circle circulo = new Circle(34);
        circulo.setFill(Color.web(C_GRN_BG));
        circulo.setStroke(Color.web(C_GRN));
        circulo.setStrokeWidth(2.5);
        Label checkIco = faLabel(FA_CHECK, 24, C_GRN);
        StackPane iconoCheck = new StackPane(circulo, checkIco);

        // ── Título ───────────────────────────────────────────────
        Label lblTitulo = new Label(titulo);
        lblTitulo.setFont(Font.font("System", FontWeight.BOLD, 17));
        lblTitulo.setTextFill(Color.web("#111827"));

        Label lblSub = new Label("El archivo fue guardado correctamente.");
        lblSub.setStyle("-fx-font-size:12px;-fx-text-fill:" + GRAY_TEXT + ";");

        // ── Card con nombre de archivo ────────────────────────────
        Label excelIco = faLabel(FA_DOWNLOAD, 16, C_GRN);
        Label lblNombre = new Label(nombreArchivo);
        lblNombre.setFont(Font.font("System", FontWeight.BOLD, 13));
        lblNombre.setTextFill(Color.web("#111827"));

        Label lblRuta = new Label(rutaCompleta);
        lblRuta.setStyle("-fx-font-size:10px;-fx-text-fill:" + GRAY_TEXT + ";");
        lblRuta.setWrapText(true);
        lblRuta.setMaxWidth(360);

        VBox archivoInfo = new VBox(3, lblNombre, lblRuta);
        HBox archivoCard = new HBox(12, excelIco, archivoInfo);
        archivoCard.setAlignment(Pos.CENTER_LEFT);
        archivoCard.setPadding(new Insets(12, 16, 12, 16));
        archivoCard.setStyle("-fx-background-color:" + C_GRN_BG + ";"
                + "-fx-background-radius:10;"
                + "-fx-border-color:" + C_GRN + ";"
                + "-fx-border-radius:10;"
                + "-fx-border-width:1;");
        archivoCard.setMaxWidth(400);

        // ── Botón cerrar ─────────────────────────────────────────
        Button btnCerrar = new Button("Listo");
        btnCerrar.setPrefWidth(120);
        btnCerrar.setFont(Font.font("System", FontWeight.BOLD, 13));
        String baseBtn = "-fx-background-color:" + C_GRN + ";-fx-text-fill:white;"
                + "-fx-background-radius:8;-fx-padding:9 24;-fx-cursor:hand;";
        String hoverBtn = "-fx-background-color:#388e3c;-fx-text-fill:white;"
                + "-fx-background-radius:8;-fx-padding:9 24;-fx-cursor:hand;";
        btnCerrar.setStyle(baseBtn);
        btnCerrar.setOnMouseEntered(e -> btnCerrar.setStyle(hoverBtn));
        btnCerrar.setOnMouseExited(e -> btnCerrar.setStyle(baseBtn));
        btnCerrar.setOnAction(e -> dialog.close());

        // ── Layout ───────────────────────────────────────────────
        VBox body = new VBox(16, iconoCheck, lblTitulo, lblSub, archivoCard, btnCerrar);
        body.setAlignment(Pos.CENTER);
        body.setPadding(new Insets(32, 36, 28, 36));
        body.setStyle("-fx-background-color:white;-fx-background-radius:16;");
        body.setMaxWidth(460);

        // Sombra exterior
        body.setEffect(new DropShadow(20, 0, 4, Color.web("#0000002a")));

        StackPane root = new StackPane(body);
        root.setStyle("-fx-background-color:" + BG + ";");
        root.setPadding(new Insets(20));

        javafx.scene.Scene scene = new javafx.scene.Scene(root, 480, 340);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    /**
     * Ventana de error al exportar — diseño tipo card con icono rojo y mensaje.
     */
    private void showExportError(String mensaje) {
        javafx.stage.Stage dialog = new javafx.stage.Stage();
        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialog.setTitle("Error al exportar");
        dialog.setResizable(false);

        Circle circulo = new Circle(34);
        circulo.setFill(Color.web(C_RED_BG));
        circulo.setStroke(Color.web(C_RED));
        circulo.setStrokeWidth(2.5);
        Label errIco = faLabel(FA_TIMES, 22, C_RED);
        StackPane iconoErr = new StackPane(circulo, errIco);

        Label lblTitulo = new Label("No se pudo exportar el archivo");
        lblTitulo.setFont(Font.font("System", FontWeight.BOLD, 17));
        lblTitulo.setTextFill(Color.web("#111827"));

        Label lblMsg = new Label(mensaje != null ? mensaje : "Error desconocido.");
        lblMsg.setStyle("-fx-font-size:12px;-fx-text-fill:" + C_RED + ";");
        lblMsg.setWrapText(true);
        lblMsg.setMaxWidth(360);
        lblMsg.setPadding(new Insets(10, 16, 10, 16));
        lblMsg.setStyle("-fx-background-color:" + C_RED_BG + ";-fx-background-radius:8;"
                + "-fx-font-size:12px;-fx-text-fill:" + C_RED + ";");

        Button btnCerrar = new Button("Cerrar");
        btnCerrar.setPrefWidth(120);
        btnCerrar.setFont(Font.font("System", FontWeight.BOLD, 13));
        String baseBtn = "-fx-background-color:" + C_RED + ";-fx-text-fill:white;"
                + "-fx-background-radius:8;-fx-padding:9 24;-fx-cursor:hand;";
        String hoverBtn = "-fx-background-color:#c62828;-fx-text-fill:white;"
                + "-fx-background-radius:8;-fx-padding:9 24;-fx-cursor:hand;";
        btnCerrar.setStyle(baseBtn);
        btnCerrar.setOnMouseEntered(e -> btnCerrar.setStyle(hoverBtn));
        btnCerrar.setOnMouseExited(e -> btnCerrar.setStyle(baseBtn));
        btnCerrar.setOnAction(e -> dialog.close());

        VBox body = new VBox(16, iconoErr, lblTitulo, lblMsg, btnCerrar);
        body.setAlignment(Pos.CENTER);
        body.setPadding(new Insets(32, 36, 28, 36));
        body.setStyle("-fx-background-color:white;-fx-background-radius:16;");
        body.setMaxWidth(440);
        body.setEffect(new DropShadow(20, 0, 4, Color.web("#0000002a")));

        StackPane root = new StackPane(body);
        root.setStyle("-fx-background-color:" + BG + ";");
        root.setPadding(new Insets(20));

        javafx.scene.Scene scene = new javafx.scene.Scene(root, 460, 300);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    // ══════════════════════════════════════════════════════════════
    // HELPERS — BADGES VISUALES
    // ══════════════════════════════════════════════════════════════
    private HBox buildEstadoAtencBadge(String estado) {
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
        Label ico = faLabel(icon, 11, color);
        Label txt = new Label(" " + estado.replace("_", " "));
        txt.setStyle("-fx-font-size:11px;-fx-font-weight:bold;-fx-text-fill:" + color + ";");
        HBox box = new HBox(2, ico, txt);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(3, 9, 3, 9));
        box.setStyle("-fx-background-color:" + bg + ";-fx-background-radius:20;");
        return box;
    }

    private HBox buildFechaBox(String fecha) {
        Label ico = faLabel(FA_CLOCK, 11, GRAY_TEXT);
        Label txt = new Label(" " + fecha);
        txt.setStyle("-fx-font-size:11px;-fx-text-fill:#374151;");
        HBox box = new HBox(3, ico, txt);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private HBox buildNumBadge(int num, String bg, String fg, double width) {
        StackPane sp = new StackPane();
        Circle c = new Circle(16, Color.web(bg));
        Label l = lbl(String.valueOf(num), 11, fg, true);
        sp.getChildren().addAll(c, l);
        HBox box = new HBox(sp);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPrefWidth(width);
        box.setMinWidth(width);
        return box;
    }

    private String tipoIcon(String tipo) {
        if (tipo == null) {
            return FA_BELL;
        }
        return switch (tipo.toUpperCase()) {
            case "ROBO", "HURTO" ->
                "\uf505";
            case "ACCIDENTE" ->
                "\uf071";
            case "INCENDIO" ->
                "\uf46a";
            case "ANIMAL" ->
                "\uf6d3";
            case "VANDALISMO" ->
                "\uf6e3";
            case "SOSPECHOSO", "PERSONA" ->
                "\uf007";
            default ->
                FA_BELL;
        };
    }

    private String estadoIcon(String estado) {
        if (estado == null) {
            return "\uf111";
        }
        return switch (estado.toUpperCase()) {
            case "ACTIVO", "ACTIVA" ->
                "\uf058";
            case "PENDIENTE" ->
                "\uf017";
            case "ATENDIDA", "RESUELTA" ->
                "\uf00c";
            case "CANCELADA", "RECHAZADA" ->
                "\uf057";
            default ->
                "\uf111";
        };
    }

    private String estadoColor(String estado) {
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

    private String estadoBg(String estado) {
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

    // ══════════════════════════════════════════════════════════════
    // HELPERS ESTRUCTURALES
    // ══════════════════════════════════════════════════════════════
    private HBox buildThead(String[] cols, double[] widths) {
        HBox thead = new HBox(0);
        thead.setPadding(new Insets(11, 16, 11, 16));
        thead.setStyle("-fx-background-color:#f8fafc;"
                + "-fx-border-color:transparent transparent " + BORDER + " transparent;"
                + "-fx-border-width:0 0 1 0;");
        for (int i = 0; i < cols.length; i++) {
            Label h = new Label(cols[i].toUpperCase());
            h.setStyle("-fx-font-size:10px;-fx-font-weight:bold;-fx-text-fill:#9ca3af;");
            h.setPrefWidth(widths[i]);
            h.setMinWidth(widths[i]);
            thead.getChildren().add(h);
        }
        return thead;
    }

    private HBox buildFooter(Label lblMostrando) {
        HBox footer = new HBox();
        footer.setPadding(new Insets(11, 16, 11, 16));
        footer.setStyle("-fx-border-color:" + BORDER + " transparent transparent transparent;"
                + "-fx-border-width:1 0 0 0;");
        footer.getChildren().add(lblMostrando);
        return footer;
    }

    private HBox buildFilaBase(boolean par) {
        HBox fila = new HBox(0);
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.setPadding(new Insets(10, 16, 10, 16));
        fila.setStyle(filaBgNormal(par));
        return fila;
    }

    private String filaBgNormal(boolean par) {
        return "-fx-background-color:" + (par ? WHITE : "#fafbfd") + ";"
                + "-fx-border-color:transparent transparent " + BORDER + " transparent;"
                + "-fx-border-width:0 0 1 0;";
    }

    private String filaBgHover(String accentBg) {
        return "-fx-background-color:" + accentBg + ";"
                + "-fx-border-color:transparent transparent " + BORDER + " transparent;"
                + "-fx-border-width:0 0 1 0;-fx-cursor:hand;";
    }

    private VBox buildVacioState(String mensaje) {
        VBox vacio = new VBox(10);
        vacio.setAlignment(Pos.CENTER);
        vacio.setPadding(new Insets(40));
        Label ico = faLabel(FA_SEARCH, 32, GRAY_TEXT);
        Label msg = lbl(mensaje, 14, GRAY_TEXT, false);
        vacio.getChildren().addAll(ico, msg);
        return vacio;
    }

    private VBox labeledControl(Label labelNode, javafx.scene.Node control, double prefW) {
        VBox vb = new VBox(4, labelNode, control);
        vb.setAlignment(Pos.TOP_LEFT);
        if (prefW > 0 && control instanceof Region r) {
            r.setPrefWidth(prefW);
            r.setMinWidth(prefW);
        }
        return vb;
    }

    private Label labelFiltro(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size:11px;-fx-text-fill:" + GRAY_TEXT + ";-fx-font-weight:bold;");
        return l;
    }

    private ComboBox<String> comboFiltro(String placeholder) {
        ComboBox<String> cb = new ComboBox<>();
        cb.setPromptText(placeholder);
        cb.getItems().add(null);
        cb.setStyle(estiloInput());
        cb.setMaxWidth(Double.MAX_VALUE);
        return cb;
    }

    private DatePicker datePicker(String prompt) {
        DatePicker dp = new DatePicker();
        dp.setPromptText(prompt);
        String style = "-fx-background-color:white;-fx-background-radius:10;"
                + "-fx-border-radius:10;-fx-border-color:#d1d5db;-fx-border-width:1;"
                + "-fx-padding:6 10;-fx-font-size:12px;";
        dp.setStyle(style);
        dp.getEditor().setStyle("-fx-background-color:transparent;-fx-text-fill:#111827;-fx-font-size:12px;");
        dp.setOnMouseEntered(e -> dp.setStyle(style + "-fx-border-color:" + C_BLU + ";"));
        dp.setOnMouseExited(e -> dp.setStyle(style));
        dp.setPrefHeight(36);
        dp.setMaxWidth(Double.MAX_VALUE);
        return dp;
    }

    private Button accionBtn(String texto, String icon, String color, String style) {
        Button b = new Button();
        boolean dark = color.equals(C_BLU) || color.equals(C_PUR);
        Label ico = faLabel(icon, 12, dark ? WHITE : color);
        Label txt = new Label("  " + texto);
        txt.setStyle("-fx-text-fill:" + (dark ? "white" : color) + ";-fx-font-size:12px;-fx-font-weight:bold;");
        HBox content = new HBox(4, ico, txt);
        content.setAlignment(Pos.CENTER);
        b.setGraphic(content);
        b.setStyle(style);
        VBox.setMargin(b, new Insets(18, 0, 0, 0));
        return b;
    }

    private String estiloInput() {
        return "-fx-background-color:white;-fx-border-color:" + BORDER + ";"
                + "-fx-border-radius:8;-fx-background-radius:8;-fx-padding:6 10;-fx-font-size:12px;";
    }

    // ══════════════════════════════════════════════════════════════
    // HELPERS EXCEL
    // ══════════════════════════════════════════════════════════════
    private CellStyle excelHeaderStyle(XSSFWorkbook wb) {
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

    private CellStyle excelNormalStyle(XSSFWorkbook wb) {
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

    // ══════════════════════════════════════════════════════════════
    // HELPERS UI BASE
    // ══════════════════════════════════════════════════════════════
    private Label faLabel(String unicode, double size, String color) {
        Label l = new Label(unicode);
        l.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';-fx-font-size:" + size
                + "px;-fx-text-fill:" + color + ";");
        return l;
    }

    private StackPane faIconBox(String unicode, double iconSize,
            String iconColor, String bgColor, double boxSize) {
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

    private HBox cardHeaderBox(String title, String accentColor, String faIcon) {
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(14, 20, 12, 20));
        header.setStyle("-fx-border-color:transparent transparent " + BORDER
                + " transparent;-fx-border-width:0 0 1 0;");
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

    private Label buildSeccionLabel(String text) {
        Label l = new Label(text.toUpperCase());
        l.setFont(Font.font("System", FontWeight.BOLD, 10));
        l.setTextFill(Color.web(GRAY_TEXT));
        l.setPadding(new Insets(6, 0, 0, 2));
        return l;
    }

    private HBox buildErrorState(String message) {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(14, 20, 14, 20));
        box.setStyle("-fx-background-color:" + C_RED_BG + ";-fx-background-radius:10;");
        Label icon = faLabel("\uf071", 16, C_RED);
        Label msg = new Label("Error al cargar datos: " + message);
        msg.setFont(Font.font("System", 12));
        msg.setTextFill(Color.web(C_RED));
        msg.setWrapText(true);
        box.getChildren().addAll(icon, msg);
        return box;
    }

    private Label celdaFija(String txt, double width) {
        Label l = new Label(txt != null ? txt : "—");
        l.setStyle("-fx-font-size:12px;-fx-text-fill:#374151;");
        l.setPrefWidth(width);
        l.setMinWidth(width);
        l.setMaxWidth(width);
        l.setTextOverrun(javafx.scene.control.OverrunStyle.ELLIPSIS);
        return l;
    }

    private HBox hboxCell(javafx.scene.Node node, double width) {
        HBox box = new HBox(node);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPrefWidth(width);
        box.setMinWidth(width);
        box.setMaxWidth(width);
        return box;
    }

    private Label lbl(String text, double size, String color, boolean bold) {
        Label l = new Label(text);
        l.setFont(bold ? Font.font("System", FontWeight.BOLD, size) : Font.font("System", size));
        l.setTextFill(Color.web(color));
        return l;
    }

    private void shadow(Region node) {
        node.setEffect(new DropShadow(12, 0, 2, Color.web("#0000001a")));
    }

    private Region divider() {
        Region r = new Region();
        r.setPrefHeight(1);
        r.setStyle("-fx-background-color:" + BORDER + ";");
        return r;
    }

    private String nvl(String s) {
        return s != null ? s : "";
    }
}
