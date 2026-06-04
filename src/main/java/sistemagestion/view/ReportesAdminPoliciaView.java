package sistemagestion.view;

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

public class ReportesAdminPoliciaView {

    // ── Paleta ────────────────────────────────────────────────────
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

    // ── Font Awesome unicode ──────────────────────────────────────
    private static final String FA_BELL = "\uf0f3";
    private static final String FA_SHIELD = "\uf505";
    private static final String FA_CAR = "\uf5e4";
    private static final String FA_SIREN = "\uf46a";
    private static final String FA_CLIPBOARD = "\uf46d";
    private static final String FA_BULLHORN = "\uf0a1";
    private static final String FA_THUMBTACK = "\uf08d";
    private static final String FA_DOWNLOAD = "\uf019";
    private static final String FA_REPORT = "\uf080";
    private static final String FA_LIST = "\uf03a";
    private static final String FA_FILTER = "\uf0b0";
    private static final String FA_TIMES = "\uf00d";
    private static final String FA_SEARCH = "\uf002";
    private static final String FA_CALENDAR = "\uf073";

    // ── Servicios ─────────────────────────────────────────────────
    private final AlertaService alertaService;
    private final AsignacionUnidadService asignacionService;
    private final UnidadPolicialService unidadService;
    private final PoliciaService policiaService;
    private final AlarmaService alarmaService;
    private final NotificacionService notificacionService;
    private final AtencionAlertaService atencionService;

    // ── Paginación ────────────────────────────────────────────────
    private static final int FILAS_POR_PAGINA = 8;
    private int paginaActual = 1;
    private List<Alerta> alertasCached;        // todos los registros
    private List<Alerta> alertasFiltradas;     // resultado tras filtros
    private List<AtencionAlerta> atencionesCached;
    private VBox tbodyRef;
    private Label lblMostrandoRef;
    private HBox paginacionBoxRef;

    // ── Controles de filtro ───────────────────────────────────────
    private ComboBox<String> cbTipo;
    private ComboBox<String> cbEstado;
    private ComboBox<String> cbBarrio;
    private DatePicker dpDesde;
    private DatePicker dpHasta;
    private TextField txtBuscar;

    // ── Anchos de columnas ────────────────────────────────────────
    private static final double[] WIDTHS = {45, 140, 120, 120, 155, 180, 130, 150};

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

    // ═════════════════════════════════════════════════════════════
    // PUNTO DE ENTRADA
    // ═════════════════════════════════════════════════════════════
    public ScrollPane build() {
        Font.loadFont(getClass().getResourceAsStream("/fa-solid-900.ttf"), 16);

        // Precargar datos
        alertasCached = alertaService != null ? alertaService.listar() : List.of();
        atencionesCached = atencionService != null ? atencionService.listar() : List.of();
        alertasFiltradas = alertasCached;

        VBox content = new VBox(20);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color:" + BG + ";");

        content.getChildren().add(buildTopBar());
        content.getChildren().add(buildStatsRow());

        try {
            content.getChildren().add(buildSeccionLabel("Detalle de Actividad"));
            content.getChildren().add(buildDetailCard());
            content.getChildren().add(buildSeccionLabel("Reporte de Alertas"));
            content.getChildren().add(buildFiltrosCard());   // ← FILTROS
            content.getChildren().add(buildTablaAlertas());
            content.getChildren().add(buildPaginacion());
        } catch (Exception e) {
            content.getChildren().add(buildErrorState(e.getMessage()));
        }

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color:" + BG + "; -fx-background:" + BG + ";");
        return scroll;
    }

    // ═════════════════════════════════════════════════════════════
    // TOP BAR
    // ═════════════════════════════════════════════════════════════
    private HBox buildTopBar() {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);

        StackPane titleIcon = faIconBox(FA_REPORT, 22, C_BLU, C_BLU_BG, 44);

        VBox titles = new VBox(4);
        Label title = new Label("Reportes del Sistema");
        title.setFont(Font.font("System", FontWeight.BOLD, 26));
        title.setTextFill(Color.web("#111827"));
        Label sub = lbl("Resumen general de actividad operativa", 13, GRAY_TEXT, false);
        titles.getChildren().addAll(title, sub);

        HBox titleRow = new HBox(14, titleIcon, titles);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        HBox right = new HBox(12);
        right.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(right, Priority.ALWAYS);

        Button btnExportar = new Button();
        Label dlIco = faLabel(FA_DOWNLOAD, 14, WHITE);
        Label dlTxt = new Label("  Exportar Excel");
        dlTxt.setStyle("-fx-text-fill:white;-fx-font-size:13px;-fx-font-weight:bold;");
        HBox btnContent = new HBox(4, dlIco, dlTxt);
        btnContent.setAlignment(Pos.CENTER);
        btnExportar.setGraphic(btnContent);

        String base = "-fx-background-color:linear-gradient(to right,#16283d,#1f3a56);"
                + "-fx-background-radius:8;-fx-padding:10 18;-fx-cursor:hand;";
        String hover = "-fx-background-color:linear-gradient(to right,#0f1e30,#16283d);"
                + "-fx-background-radius:8;-fx-padding:10 18;-fx-cursor:hand;";
        btnExportar.setStyle(base);
        btnExportar.setOnMouseEntered(e -> btnExportar.setStyle(hover));
        btnExportar.setOnMouseExited(e -> btnExportar.setStyle(base));
        btnExportar.setOnAction(e -> {
            if (alertasFiltradas != null) {
                exportarExcel(alertasFiltradas, atencionesCached, btnExportar);
            }
        });
        right.getChildren().add(btnExportar);

        bar.getChildren().addAll(titleRow, right);
        return bar;
    }

    // ═════════════════════════════════════════════════════════════
    // STATS ROW
    // ═════════════════════════════════════════════════════════════
    private HBox buildStatsRow() {
        HBox row = new HBox(16);
        HBox.setHgrow(row, Priority.ALWAYS);
        try {
            long totalAlertas = alertasCached.size();
            long totalPolicias = policiaService != null ? policiaService.listar().size() : 0;
            long totalUnidades = unidadService != null ? unidadService.listar().size() : 0;
            long totalAsig = asignacionService != null ? asignacionService.listar().size() : 0;
            long totalAlarmas = alarmaService != null ? alarmaService.listar().size() : 0;
            long totalAtenc = atencionesCached.size();

            row.getChildren().addAll(
                    statCard(C_RED_BG, C_RED, FA_BELL, "Alertas", totalAlertas, "Registradas en el sistema"),
                    statCard(C_BLU_BG, C_BLU, FA_SHIELD, "Policías", totalPolicias, "Personal activo"),
                    statCard(C_GRN_BG, C_GRN, FA_CAR, "Unidades", totalUnidades, "Unidades configuradas"),
                    statCard(C_ORG_BG, C_ORG, FA_THUMBTACK, "Asignaciones", totalAsig, "Despachos realizados"),
                    statCard(C_AMB_BG, C_AMB, FA_SIREN, "Alarmas", totalAlarmas, "Eventos registrados"),
                    statCard(C_TEA_BG, C_TEA, FA_CLIPBOARD, "Atenciones", totalAtenc, "Alertas atendidas")
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

    // ═════════════════════════════════════════════════════════════
    // TARJETA DETALLE
    // ═════════════════════════════════════════════════════════════
    private VBox buildDetailCard() throws Exception {
        long totalNoti = notificacionService != null ? notificacionService.listar().size() : 0;
        long totalAsig = asignacionService != null ? asignacionService.listar().size() : 0;

        VBox card = new VBox(0);
        card.setStyle("-fx-background-color:white;-fx-background-radius:14;");
        shadow(card);
        card.getChildren().add(cardHeaderBox("Estadísticas adicionales", C_PUR, FA_BULLHORN));

        String[][] items = {
            {FA_BULLHORN, "Notificaciones enviadas", String.valueOf(totalNoti), C_PUR, C_PUR_BG},
            {FA_THUMBTACK, "Total de asignaciones", String.valueOf(totalAsig), C_ORG, C_ORG_BG},};
        for (int i = 0; i < items.length; i++) {
            card.getChildren().add(buildDetailRow(items[i][0], items[i][1], items[i][2], items[i][3], items[i][4]));
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

    // ═════════════════════════════════════════════════════════════
    // PANEL DE FILTROS  ← NUEVO
    // ═════════════════════════════════════════════════════════════
    private VBox buildFiltrosCard() {
        VBox card = new VBox(0);
        card.setStyle("-fx-background-color:white;-fx-background-radius:14;");
        shadow(card);
        card.getChildren().add(cardHeaderBox("Filtrar alertas", C_BLU, FA_FILTER));

        // ── Primera fila: Tipo | Estado | Barrio | Buscar ─────────
        cbTipo = comboFiltro("Todos los tipos");
        cbEstado = comboFiltro("Todos los estados");
        cbBarrio = comboFiltro("Todos los barrios");

        // Poblar opciones únicas desde los datos cacheados
        alertasCached.stream()
                .filter(a -> a.getTipoalerta() != null)
                .map(a -> a.getTipoalerta().getNombre())
                .distinct().sorted()
                .forEach(t -> cbTipo.getItems().add(t));

        alertasCached.stream()
                .filter(a -> a.getEstado() != null)
                .map(a -> a.getEstado().name())
                .distinct().sorted()
                .forEach(e -> cbEstado.getItems().add(e));

        alertasCached.stream()
                .filter(a -> a.getBarrio() != null)
                .map(a -> a.getBarrio().getNombre())
                .distinct().sorted()
                .forEach(b -> cbBarrio.getItems().add(b));

        txtBuscar = new TextField();
        txtBuscar.setPromptText("Buscar en descripción…");
        txtBuscar.setPrefWidth(200);
        txtBuscar.setStyle(estiloInput());
        HBox.setHgrow(txtBuscar, Priority.ALWAYS);

        Label icoTipo = filtroLabel(FA_FILTER, "Tipo:");
        Label icoEstado = filtroLabel(FA_FILTER, "Estado:");
        Label icoBarrio = filtroLabel(FA_MAP_PIN_SMALL, "Barrio:");
        Label icoBuscar = filtroLabel(FA_SEARCH, "Buscar:");

        HBox fila1 = new HBox(16,
                labeledControl(icoTipo, cbTipo, 130),
                labeledControl(icoEstado, cbEstado, 130),
                labeledControl(icoBarrio, cbBarrio, 150),
                labeledControl(icoBuscar, txtBuscar, -1)
        );
        fila1.setAlignment(Pos.CENTER_LEFT);
        fila1.setPadding(new Insets(14, 20, 10, 20));

        // ── Segunda fila: Fecha desde | Fecha hasta | Aplicar | Limpiar
        dpDesde = datePicker("Desde…");
        dpHasta = datePicker("Hasta…");

        Button btnAplicar = accionBtn("Aplicar filtros", FA_FILTER, C_BLU,
                "-fx-background-color:" + C_BLU + ";-fx-text-fill:white;"
                + "-fx-background-radius:8;-fx-padding:8 18;-fx-cursor:hand;-fx-font-weight:bold;");
        btnAplicar.setOnAction(e -> aplicarFiltros());

        Button btnLimpiar = accionBtn("Limpiar", FA_TIMES, C_RED,
                "-fx-background-color:" + C_RED_BG + ";-fx-text-fill:" + C_RED + ";"
                + "-fx-background-radius:8;-fx-padding:8 18;-fx-cursor:hand;-fx-font-weight:bold;");
        btnLimpiar.setOnAction(e -> limpiarFiltros());

        Label icoDesde = filtroLabel(FA_CALENDAR, "Desde:");
        Label icoHasta = filtroLabel(FA_CALENDAR, "Hasta:");

        HBox fila2 = new HBox(16,
                labeledControl(icoDesde, dpDesde, 160),
                labeledControl(icoHasta, dpHasta, 160),
                btnAplicar,
                btnLimpiar
        );
        fila2.setAlignment(Pos.CENTER_LEFT);
        fila2.setPadding(new Insets(0, 20, 14, 20));

        card.getChildren().addAll(fila1, fila2);
        return card;
    }

    // Pequeño helper para icono + label + control apilados verticalmente
    private VBox labeledControl(Label labelNode, javafx.scene.Node control, double prefW) {
        VBox vb = new VBox(4, labelNode, control);
        vb.setAlignment(Pos.TOP_LEFT);
        if (prefW > 0 && control instanceof Region r) {
            r.setPrefWidth(prefW);
            r.setMinWidth(prefW);
        }
        return vb;
    }

    private Label filtroLabel(String icon, String text) {
        Label l = new Label(icon + "  " + text);
        l.setStyle("-fx-font-family:'Font Awesome 6 Free Solid',-fx-system-font;"
                + "-fx-font-size:11px;-fx-text-fill:" + GRAY_TEXT + ";-fx-font-weight:bold;");
        // Para el texto sin FA usamos Label compuesto
        return labelFiltroTexto(text);
    }

    private Label labelFiltroTexto(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size:11px;-fx-text-fill:" + GRAY_TEXT + ";-fx-font-weight:bold;");
        return l;
    }

    // Constante pequeña para el ícono de mapa
    private static final String FA_MAP_PIN_SMALL = "\uf3c5";

    private ComboBox<String> comboFiltro(String placeholder) {
        ComboBox<String> cb = new ComboBox<>();
        cb.setPromptText(placeholder);
        cb.getItems().add(null); // opción "todos"
        cb.setStyle(estiloInput());
        cb.setMaxWidth(Double.MAX_VALUE);
        return cb;
    }

    private DatePicker datePicker(String prompt) {
        DatePicker dp = new DatePicker();
        dp.setPromptText(prompt);

        String style
                = "-fx-background-color: white;"
                + "-fx-background-radius: 10;"
                + "-fx-border-radius: 10;"
                + "-fx-border-color: #d1d5db;"
                + // un poco más fuerte (menos gris muerto)
                "-fx-border-width: 1;"
                + "-fx-padding: 6 10;"
                + "-fx-font-size: 12px;";

        dp.setStyle(style);

        dp.getEditor().setStyle(
                "-fx-background-color: transparent;"
                + "-fx-text-fill: #111827;"
                + // texto más oscuro (más contraste)
                "-fx-font-size: 12px;"
        );

        dp.setOnMouseEntered(e
                -> dp.setStyle(style + "-fx-border-color: #1565c0;")
        );

        dp.setOnMouseExited(e
                -> dp.setStyle(style)
        );

        dp.setPrefHeight(36);
        dp.setMaxWidth(Double.MAX_VALUE);

        return dp;
    }

    private Button accionBtn(String texto, String icon, String color, String style) {
        Button b = new Button();
        Label ico = faLabel(icon, 12, color.equals(C_BLU) ? WHITE : color);
        Label txt = new Label("  " + texto);
        txt.setStyle("-fx-text-fill:" + (color.equals(C_BLU) ? "white" : color) + ";"
                + "-fx-font-size:12px;-fx-font-weight:bold;");
        HBox content = new HBox(4, ico, txt);
        content.setAlignment(Pos.CENTER);
        b.setGraphic(content);
        b.setStyle(style);
        b.setAlignment(Pos.BOTTOM_LEFT);
        // margen superior para alinear con los pickers
        VBox.setMargin(b, new Insets(18, 0, 0, 0));
        return b;
    }

    private String estiloInput() {
        return "-fx-background-color:white;"
                + "-fx-border-color:" + BORDER + ";"
                + "-fx-border-radius:8;"
                + "-fx-background-radius:8;"
                + "-fx-padding:6 10;"
                + "-fx-font-size:12px;";
    }

    // ─── Lógica de filtrado ───────────────────────────────────────
    private void aplicarFiltros() {
        String tipo = cbTipo.getValue();
        String estado = cbEstado.getValue();
        String barrio = cbBarrio.getValue();
        String buscar = txtBuscar.getText() != null ? txtBuscar.getText().trim().toLowerCase() : "";
        LocalDate desde = dpDesde.getValue();
        LocalDate hasta = dpHasta.getValue();

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

        paginaActual = 1;
        renderizarPagina(WIDTHS);
    }

    private void limpiarFiltros() {
        cbTipo.setValue(null);
        cbEstado.setValue(null);
        cbBarrio.setValue(null);
        txtBuscar.clear();
        dpDesde.setValue(null);
        dpHasta.setValue(null);
        alertasFiltradas = alertasCached;
        paginaActual = 1;
        renderizarPagina(WIDTHS);
    }

    // ═════════════════════════════════════════════════════════════
    // TABLA DE ALERTAS
    // ═════════════════════════════════════════════════════════════
    private VBox buildTablaAlertas() {
        // alertasCached y atencionesCached ya fueron cargados en build()
        alertasFiltradas = alertasCached;

        VBox card = new VBox(0);
        card.setStyle("-fx-background-color:white;-fx-background-radius:14;");
        shadow(card);
        card.getChildren().add(cardHeaderBox("Listado de Alertas", C_GRN, FA_LIST));

        String[] cols = {"#", "Tipo", "Barrio", "Estado", "Fecha y Hora", "Descripción", "Unidad", "Atendida por"};
        HBox thead = new HBox(0);
        thead.setPadding(new Insets(11, 16, 11, 16));
        thead.setStyle("-fx-background-color:#f8fafc;"
                + "-fx-border-color:transparent transparent " + BORDER + " transparent;"
                + "-fx-border-width:0 0 1 0;");
        for (int i = 0; i < cols.length; i++) {
            Label h = new Label(cols[i].toUpperCase());
            h.setStyle("-fx-font-size:10px;-fx-font-weight:bold;-fx-text-fill:#9ca3af;");
            h.setPrefWidth(WIDTHS[i]);
            h.setMinWidth(WIDTHS[i]);
            thead.getChildren().add(h);
        }

        tbodyRef = new VBox(0);
        renderizarPagina(WIDTHS);

        lblMostrandoRef = lbl("", 12, GRAY_TEXT, false);
        HBox footer = new HBox();
        footer.setPadding(new Insets(11, 16, 11, 16));
        footer.setStyle("-fx-border-color:" + BORDER + " transparent transparent transparent;"
                + "-fx-border-width:1 0 0 0;");
        footer.getChildren().add(lblMostrandoRef);
        actualizarLblMostrando();

        card.getChildren().addAll(thead, tbodyRef, footer);
        return card;
    }

    private void renderizarPagina(double[] widths) {
        tbodyRef.getChildren().clear();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        int total = alertasFiltradas.size();
        int desde = (paginaActual - 1) * FILAS_POR_PAGINA;
        int hasta = Math.min(desde + FILAS_POR_PAGINA, total);
        
        List<sistemagestion.model.AsignacionUnidad> asignaciones =
            asignacionService != null ? asignacionService.listar() : List.of();

        if (total == 0) {
            VBox vacio = new VBox(10);
            vacio.setAlignment(Pos.CENTER);
            vacio.setPadding(new Insets(40));
            Label faVacio = faLabel(FA_SEARCH, 32, GRAY_TEXT);
            Label msg = lbl("No hay alertas que coincidan con los filtros", 14, GRAY_TEXT, false);
            vacio.getChildren().addAll(faVacio, msg);
            tbodyRef.getChildren().add(vacio);
            actualizarLblMostrando();
            actualizarPaginacion(widths);
            return;
        }

        for (int i = desde; i < hasta; i++) {
            Alerta al = alertasFiltradas.get(i);
            boolean par = i % 2 == 0;

            final int idAlerta = al.getId_alerta();
            // ✅ Después
            AtencionAlerta aten = atencionesCached.stream()
                    .filter(a -> a.getAlerta() != null && a.getAlerta().getId_alerta() == idAlerta)
                    .findFirst().orElse(null);

            String[] unidad = {"—"};
            String[] policia = {"—"};

            if (aten != null && aten.getUnidad() != null) {
                unidad[0] = aten.getUnidad().getNombre();
                if (aten.getUnidad().getPolicias() != null
                        && !aten.getUnidad().getPolicias().isEmpty()) {
                    Policia p = aten.getUnidad().getPolicias().get(0);
                    policia[0] = p.getPrimer_nombre() + " " + p.getPrimer_apellido();
                }
            } else {
            
                       
                asignaciones.stream()
                        .filter(asig -> asig.getAlerta() != null
                        && asig.getAlerta().getId_alerta() == idAlerta
                        && asig.getUnidadpolicial() != null)
                        .findFirst()
                        .ifPresent(asig -> unidad[0] = asig.getUnidadpolicial().getNombre());
            }

            String tipo = al.getTipoalerta() != null ? al.getTipoalerta().getNombre() : "—";
            String barrio = al.getBarrio() != null ? al.getBarrio().getNombre() : "—";
            String estado = al.getEstado() != null ? al.getEstado().name() : "—";
            // ← fecha Y hora completa
            String fecha = al.getFechaHora() != null ? al.getFechaHora().format(fmt) : "—";
            String desc = al.getDescripcion() != null ? al.getDescripcion() : "—";

            HBox fila = new HBox(0);
            fila.setAlignment(Pos.CENTER_LEFT);
            fila.setPadding(new Insets(10, 16, 10, 16));
            String bgN = "-fx-background-color:" + (par ? WHITE : "#fafbfd") + ";"
                    + "-fx-border-color:transparent transparent " + BORDER + " transparent;"
                    + "-fx-border-width:0 0 1 0;";
            fila.setStyle(bgN);
            fila.setOnMouseEntered(e -> fila.setStyle(
                    "-fx-background-color:#EEF2FF;"
                    + "-fx-border-color:transparent transparent " + BORDER + " transparent;"
                    + "-fx-border-width:0 0 1 0;-fx-cursor:hand;"));
            fila.setOnMouseExited(e -> fila.setStyle(bgN));

            // Col 1: ID badge circular
            StackPane idBadge = new StackPane();
            Circle idCircle = new Circle(16, Color.web(C_BLU_BG));
            Label idLbl = lbl(String.valueOf(al.getId_alerta()), 11, C_BLU, true);
            idBadge.getChildren().addAll(idCircle, idLbl);
            HBox idBox = new HBox(idBadge);
            idBox.setAlignment(Pos.CENTER_LEFT);
            idBox.setPrefWidth(widths[0]);
            idBox.setMinWidth(widths[0]);

            // Col 2: Tipo
            Label tipoIco = faLabel(tipoIcon(tipo), 11, C_RED);
            Label tipoTxt = new Label(" " + tipo);
            tipoTxt.setStyle("-fx-font-size:11px;-fx-font-weight:bold;-fx-text-fill:" + C_RED + ";");
            HBox tipoBadgeBox = new HBox(2, tipoIco, tipoTxt);
            tipoBadgeBox.setAlignment(Pos.CENTER_LEFT);
            tipoBadgeBox.setPadding(new Insets(3, 9, 3, 9));
            tipoBadgeBox.setStyle("-fx-background-color:" + C_RED_BG + ";-fx-background-radius:20;");
            HBox tipoBox = hboxCell(tipoBadgeBox, widths[1]);

            // Col 3: Barrio
            Label barrioLbl = celdaFija(barrio, widths[2]);

            // Col 4: Estado
            Label estadoIco = faLabel(estadoIcon(estado), 11, estadoColor(estado));
            Label estadoTxt = new Label(" " + estado);
            estadoTxt.setStyle("-fx-font-size:11px;-fx-font-weight:bold;-fx-text-fill:" + estadoColor(estado) + ";");
            HBox estadoBadgeBox = new HBox(2, estadoIco, estadoTxt);
            estadoBadgeBox.setAlignment(Pos.CENTER_LEFT);
            estadoBadgeBox.setPadding(new Insets(3, 9, 3, 9));
            estadoBadgeBox.setStyle("-fx-background-color:" + estadoBg(estado) + ";-fx-background-radius:20;");
            HBox estadoBox = hboxCell(estadoBadgeBox, widths[3]);

            // Col 5: Fecha y Hora (con ícono de reloj)
            Label fechaIco = faLabel("\uf017", 11, GRAY_TEXT); // clock
            Label fechaTxt = new Label(" " + fecha);
            fechaTxt.setStyle("-fx-font-size:11px;-fx-text-fill:#374151;");
            HBox fechaBox2 = new HBox(3, fechaIco, fechaTxt);
            fechaBox2.setAlignment(Pos.CENTER_LEFT);
            HBox fechaBox = hboxCell(fechaBox2, widths[4]);

            // Cols 6–8
            Label descLbl = celdaFija(desc, widths[5]);
            Label unidadLbl = celdaFija(unidad[0], widths[6]);
            Label polLbl = celdaFija(policia[0], widths[7]);
            fila.getChildren().addAll(idBox, tipoBox, barrioLbl, estadoBox,
                    fechaBox, descLbl, unidadLbl, polLbl);
            tbodyRef.getChildren().add(fila);
        }

        actualizarLblMostrando();
        actualizarPaginacion(widths);
    }

    private void actualizarLblMostrando() {
        if (lblMostrandoRef == null) {
            return;
        }
        int total = alertasFiltradas != null ? alertasFiltradas.size() : 0;
        int desde = (paginaActual - 1) * FILAS_POR_PAGINA;
        int hasta = Math.min(desde + FILAS_POR_PAGINA, total);
        lblMostrandoRef.setText("Mostrando " + (total == 0 ? 0 : desde + 1)
                + " – " + hasta + " de " + total + " alertas"
                + (total < alertasCached.size() ? " (filtradas de " + alertasCached.size() + " totales)" : ""));
    }

    // ═════════════════════════════════════════════════════════════
    // PAGINACIÓN
    // ═════════════════════════════════════════════════════════════
    private HBox buildPaginacion() {
        paginacionBoxRef = new HBox(6);
        paginacionBoxRef.setAlignment(Pos.CENTER_RIGHT);
        paginacionBoxRef.setPadding(new Insets(4, 0, 0, 0));
        return paginacionBoxRef;
    }

    private void actualizarPaginacion(double[] widths) {
        if (paginacionBoxRef == null) {
            return;
        }
        paginacionBoxRef.getChildren().clear();

        int total = alertasFiltradas != null ? alertasFiltradas.size() : 0;
        int totalPags = (int) Math.ceil((double) total / FILAS_POR_PAGINA);
        if (totalPags <= 1) {
            return;
        }

        paginacionBoxRef.getChildren().add(btnPagNav("\uf104", paginaActual > 1, () -> {
            paginaActual--;
            renderizarPagina(widths);
        }));

        int ini = Math.max(1, paginaActual - 2);
        int fin = Math.min(totalPags, paginaActual + 2);

        if (ini > 1) {
            paginacionBoxRef.getChildren().add(btnPagNum(1, widths));
            if (ini > 2) {
                paginacionBoxRef.getChildren().add(puntosSuspensivos());
            }
        }
        for (int i = ini; i <= fin; i++) {
            paginacionBoxRef.getChildren().add(btnPagNum(i, widths));
        }
        if (fin < totalPags) {
            if (fin < totalPags - 1) {
                paginacionBoxRef.getChildren().add(puntosSuspensivos());
            }
            paginacionBoxRef.getChildren().add(btnPagNum(totalPags, widths));
        }

        paginacionBoxRef.getChildren().add(btnPagNav("\uf105", paginaActual < totalPags, () -> {
            paginaActual++;
            renderizarPagina(widths);
        }));
    }

    private Button btnPagNum(int pg, double[] widths) {
        boolean esActual = pg == paginaActual;
        Button b = new Button(String.valueOf(pg));
        b.setPrefSize(34, 34);
        b.setMinSize(34, 34);
        b.setMaxSize(34, 34);
        b.setFont(Font.font("System", esActual ? FontWeight.BOLD : FontWeight.NORMAL, 13));
        String styleBase = esActual
                ? "-fx-background-color:" + C_BLU + ";-fx-text-fill:white;-fx-background-radius:8;"
                + "-fx-cursor:hand;-fx-border-color:" + C_BLU + ";-fx-border-radius:8;-fx-border-width:1.5;"
                : "-fx-background-color:" + WHITE + ";-fx-text-fill:#374151;-fx-background-radius:8;"
                + "-fx-cursor:hand;-fx-border-color:" + BORDER + ";-fx-border-radius:8;-fx-border-width:1.5;";
        String styleHover = "-fx-background-color:" + C_BLU_BG + ";-fx-text-fill:" + C_BLU + ";"
                + "-fx-background-radius:8;-fx-cursor:hand;-fx-border-color:" + C_BLU + ";"
                + "-fx-border-radius:8;-fx-border-width:1.5;";
        b.setStyle(styleBase);
        if (!esActual) {
            b.setOnMouseEntered(e -> b.setStyle(styleHover));
            b.setOnMouseExited(e -> b.setStyle(styleBase));
        }
        b.setOnAction(e -> {
            paginaActual = pg;
            renderizarPagina(widths);
        });
        return b;
    }

    private Button btnPagNav(String faUnicode, boolean enabled, Runnable accion) {
        Label ico = new Label(faUnicode);
        ico.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';-fx-font-size:13px;"
                + "-fx-text-fill:" + (enabled ? "#374151" : "#d1d5db") + ";");
        Button b = new Button();
        b.setGraphic(ico);
        b.setPrefSize(34, 34);
        b.setMinSize(34, 34);
        b.setMaxSize(34, 34);
        b.setDisable(!enabled);
        String styleBase = "-fx-background-color:" + WHITE + ";-fx-background-radius:8;"
                + "-fx-cursor:" + (enabled ? "hand" : "default") + ";"
                + "-fx-border-color:" + BORDER + ";-fx-border-radius:8;-fx-border-width:1.5;";
        String styleHover = "-fx-background-color:" + C_BLU_BG + ";-fx-background-radius:8;"
                + "-fx-cursor:hand;-fx-border-color:" + C_BLU + ";-fx-border-radius:8;-fx-border-width:1.5;";
        b.setStyle(styleBase);
        if (enabled) {
            b.setOnMouseEntered(e -> b.setStyle(styleHover));
            b.setOnMouseExited(e -> b.setStyle(styleBase));
        }
        b.setOnAction(e -> accion.run());
        return b;
    }

    private Label puntosSuspensivos() {
        Label l = new Label("…");
        l.setStyle("-fx-font-size:14px;-fx-text-fill:" + GRAY_TEXT + ";");
        l.setPadding(new Insets(0, 4, 0, 4));
        return l;
    }

    // ═════════════════════════════════════════════════════════════
    // EXPORTAR EXCEL  (exporta el resultado filtrado)
    // ═════════════════════════════════════════════════════════════
    private void exportarExcel(List<Alerta> alertas, List<AtencionAlerta> atenciones,
            javafx.scene.Node owner) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Guardar reporte de alertas");
        fc.setInitialFileName("Reporte_Alertas_WolertApp.xlsx");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel", "*.xlsx"));
        File file = fc.showSaveDialog(owner.getScene().getWindow());
        if (file == null) {
            return;
        }

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Alertas");

            CellStyle hStyle = wb.createCellStyle();
            hStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            hStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            hStyle.setBorderBottom(BorderStyle.THIN);
            org.apache.poi.ss.usermodel.Font hFont = wb.createFont();
            hFont.setColor(IndexedColors.WHITE.getIndex());
            hFont.setBold(true);
            hFont.setFontName("Arial");
            hFont.setFontHeightInPoints((short) 11);
            hStyle.setFont(hFont);
            hStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle nStyle = wb.createCellStyle();
            org.apache.poi.ss.usermodel.Font nFont = wb.createFont();
            nFont.setFontName("Arial");
            nFont.setFontHeightInPoints((short) 10);
            nStyle.setFont(nFont);
            nStyle.setBorderBottom(BorderStyle.THIN);
            nStyle.setBorderLeft(BorderStyle.THIN);
            nStyle.setBorderRight(BorderStyle.THIN);

            String[] headers = {"ID", "Tipo Alerta", "Barrio", "Estado",
                "Fecha y Hora", "Descripción", "Latitud", "Longitud",
                "Unidad Asignada", "Atendida por"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(hStyle);
            }

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            int rowNum = 1;
            for (Alerta al : alertas) {
                final int idAl = al.getId_alerta();
                AtencionAlerta aten = atenciones.stream()
                        .filter(a -> a.getAlerta() != null && a.getAlerta().getId_alerta() == idAl)
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
                    al.getBarrio() != null ? al.getBarrio().getNombre() : "",
                    al.getEstado() != null ? al.getEstado().name() : "",
                    al.getFechaHora() != null ? al.getFechaHora().format(fmt) : "",
                    al.getDescripcion() != null ? al.getDescripcion() : "",
                    String.valueOf(al.getLatitud()),
                    String.valueOf(al.getLongitud()),
                    aten != null && aten.getUnidad() != null ? aten.getUnidad().getNombre() : "",
                    polNombre
                };
                Row row = sheet.createRow(rowNum++);
                for (int i = 0; i < vals.length; i++) {
                    org.apache.poi.ss.usermodel.Cell cell = row.createCell(i);
                    cell.setCellValue(vals[i]);
                    cell.setCellStyle(nStyle);
                }
            }
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fos = new FileOutputStream(file)) {
                wb.write(fos);
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Exportado");
            alert.setHeaderText(null);
            alert.setContentText("✅ Reporte guardado en:\n" + file.getAbsolutePath());
            alert.showAndWait();

        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Error al exportar: " + ex.getMessage());
            alert.showAndWait();
        }
    }

    // ═════════════════════════════════════════════════════════════
    // HELPERS Font Awesome
    // ═════════════════════════════════════════════════════════════
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
        bg.setArcWidth(14);
        bg.setArcHeight(14);
        bg.setFill(Color.web(bgColor));
        Label ico = faLabel(unicode, iconSize, iconColor);
        box.getChildren().addAll(bg, ico);
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

    // ═════════════════════════════════════════════════════════════
    // HELPERS UI
    // ═════════════════════════════════════════════════════════════
    private HBox cardHeaderBox(String title, String accentColor, String faIcon) {
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(14, 20, 12, 20));
        header.setStyle("-fx-border-color:transparent transparent " + BORDER + " transparent;"
                + "-fx-border-width:0 0 1 0;");
        Rectangle accent = new Rectangle(4, 20);
        accent.setArcWidth(4);
        accent.setArcHeight(4);
        accent.setFill(Color.web(accentColor));
        Label ico = faLabel(faIcon, 15, accentColor);
        Label titleLbl = new Label(title);
        titleLbl.setFont(Font.font("System", FontWeight.BOLD, 14));
        titleLbl.setTextFill(Color.web("#111827"));
        header.getChildren().addAll(accent, ico, titleLbl);
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
}