package sistemagestion.view;

import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.io.File;
import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

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

    // ── Paleta (igual que UsuariosAdminView) ─────────────────────
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

    // ── Servicios ────────────────────────────────────────────────
    private final AlertaService alertaService;
    private final AsignacionUnidadService asignacionService;
    private final UnidadPolicialService unidadService;
    private final PoliciaService policiaService;
    private final AlarmaService alarmaService;
    private final NotificacionService notificacionService;
    private final AtencionAlertaService atencionService;

    // ── Paginación ───────────────────────────────────────────────
    private static final int FILAS_POR_PAGINA = 8;
    private int paginaActual = 1;
    private List<Alerta> alertasCached;
    private List<AtencionAlerta> atencionesCached;
    private VBox tbodyRef;
    private Label lblMostrandoRef;
    private HBox paginacionBoxRef;

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
        VBox content = new VBox(20);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color:" + BG + ";");

        content.getChildren().add(buildTopBar());
        content.getChildren().add(buildStatsRow());

        try {
            content.getChildren().add(buildSeccionLabel("Detalle de Actividad"));
            content.getChildren().add(buildDetailCard());
            content.getChildren().add(buildSeccionLabel("Reporte de Alertas"));
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
    // TOP BAR  (gradiente oscuro igual que UsuariosAdminView)
    // ═════════════════════════════════════════════════════════════
    private HBox buildTopBar() {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);

        VBox titles = new VBox(4);
        Label title = new Label("Reportes del Sistema");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#111827"));
        Label sub = lbl("Resumen general de actividad operativa", 13, GRAY_TEXT, false);
        titles.getChildren().addAll(title, sub);

        HBox right = new HBox(12);
        right.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(right, Priority.ALWAYS);

        Button btnExportar = new Button("⬇  Exportar Excel");
        String base = "-fx-background-color: linear-gradient(to right, #16283d, #1f3a56);"
                + "-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;"
                + "-fx-background-radius: 8; -fx-padding: 10 18; -fx-cursor: hand;";
        String hover = "-fx-background-color: linear-gradient(to right, #0f1e30, #16283d);"
                + "-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;"
                + "-fx-background-radius: 8; -fx-padding: 10 18; -fx-cursor: hand;";
        btnExportar.setStyle(base);
        btnExportar.setOnMouseEntered(e -> btnExportar.setStyle(hover));
        btnExportar.setOnMouseExited(e -> btnExportar.setStyle(base));
        btnExportar.setOnAction(e -> {
            if (alertasCached != null) {
                exportarExcel(alertasCached, atencionesCached, btnExportar);
            }
        });
        right.getChildren().add(btnExportar);

        bar.getChildren().addAll(titles, right);
        return bar;
    }

    // ═════════════════════════════════════════════════════════════
    // STATS ROW  (6 tarjetas, mismo diseño que UsuariosAdminView)
    // ═════════════════════════════════════════════════════════════
    private HBox buildStatsRow() {
        HBox row = new HBox(16);
        HBox.setHgrow(row, Priority.ALWAYS);

        try {
            long totalAlertas = alertaService != null ? alertaService.listar().size() : 0;
            long totalPolicias = policiaService != null ? policiaService.listar().size() : 0;
            long totalUnidades = unidadService != null ? unidadService.listar().size() : 0;
            long totalAsig = asignacionService != null ? asignacionService.listar().size() : 0;
            long totalAlarmas = alarmaService != null ? alarmaService.listar().size() : 0;
            long totalAtenc = atencionService != null ? atencionService.listar().size() : 0;

            row.getChildren().addAll(
                    statCard(C_RED_BG, C_RED, "🚨", "Alertas", totalAlertas, "Registradas en el sistema"),
                    statCard(C_BLU_BG, C_BLU, "👮", "Policías", totalPolicias, "Personal activo"),
                    statCard(C_GRN_BG, C_GRN, "🚓", "Unidades", totalUnidades, "Unidades configuradas"),
                    statCard(C_ORG_BG, C_ORG, "📌", "Asignaciones", totalAsig, "Despachos realizados"),
                    statCard(C_AMB_BG, C_AMB, "🔔", "Alarmas", totalAlarmas, "Eventos registrados"),
                    statCard(C_TEA_BG, C_TEA, "📋", "Atenciones", totalAtenc, "Alertas atendidas")
            );
        } catch (Exception e) {
            row.getChildren().add(buildErrorState(e.getMessage()));
        }
        return row;
    }

    private VBox statCard(String bgIcon, String accent, String icon,
            String title, long value, String sub) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(18, 20, 18, 20));
        card.setStyle("-fx-background-color:white;-fx-background-radius:18;");
        HBox.setHgrow(card, Priority.ALWAYS);
        shadow(card);

        // Icono con fondo redondeado
        StackPane iconWrap = new StackPane();
        iconWrap.setPrefSize(48, 48);
        iconWrap.setMinSize(48, 48);
        iconWrap.setMaxSize(48, 48);
        Rectangle iconBg = new Rectangle(48, 48);
        iconBg.setArcWidth(14);
        iconBg.setArcHeight(14);
        iconBg.setFill(Color.web(bgIcon));
        Label iconLbl = new Label(icon);
        iconLbl.setFont(Font.font("System", 20));
        iconWrap.getChildren().addAll(iconBg, iconLbl);

        Label valueLbl = new Label(String.valueOf(value));
        valueLbl.setFont(Font.font("System", FontWeight.BOLD, 32));
        valueLbl.setTextFill(Color.web(accent));

        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#374151;");

        Label subLbl = new Label(sub);
        subLbl.setStyle("-fx-font-size:11px;-fx-text-fill:" + GRAY_TEXT + ";");

        // Barra de color inferior
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
    // TARJETA DETALLE  (Notificaciones + fila extra)
    // ═════════════════════════════════════════════════════════════
    private VBox buildDetailCard() throws Exception {
        long totalNoti = notificacionService != null ? notificacionService.listar().size() : 0;
        long totalAsig = asignacionService != null ? asignacionService.listar().size() : 0;

        VBox card = new VBox(0);
        card.setStyle("-fx-background-color:white;-fx-background-radius:14;");
        shadow(card);

        // Cabecera tarjeta
        HBox cardHeader = cardHeaderBox("Estadísticas adicionales", C_PUR);
        card.getChildren().add(cardHeader);

        String[][] items = {
            {"📢", "Notificaciones enviadas", String.valueOf(totalNoti), C_PUR, C_PUR_BG},
            {"📌", "Total de asignaciones", String.valueOf(totalAsig), C_ORG, C_ORG_BG},};

        for (int i = 0; i < items.length; i++) {
            card.getChildren().add(buildDetailRow(
                    items[i][0], items[i][1], items[i][2], items[i][3], items[i][4]));
            if (i < items.length - 1) {
                card.getChildren().add(divider());
            }
        }
        return card;
    }

    private HBox buildDetailRow(String icon, String labelText, String value,
            String color, String bgColor) {
        HBox row = new HBox(14);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(14, 20, 14, 20));
        row.setStyle("-fx-background-color:transparent;");
        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color:#f8f9fd;"));
        row.setOnMouseExited(e -> row.setStyle("-fx-background-color:transparent;"));

        StackPane iconBox = new StackPane();
        Rectangle iconBg = new Rectangle(38, 38);
        iconBg.setArcWidth(10);
        iconBg.setArcHeight(10);
        iconBg.setFill(Color.web(bgColor));
        Label iconLbl = new Label(icon);
        iconLbl.setFont(Font.font("System", 17));
        iconBox.getChildren().addAll(iconBg, iconLbl);

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
    // TABLA DE ALERTAS  (mismo estilo UsuariosAdminView)
    // ═════════════════════════════════════════════════════════════
    private VBox buildTablaAlertas() {
        alertasCached = alertaService != null ? alertaService.listar() : List.of();
        atencionesCached = atencionService != null ? atencionService.listar() : List.of();

        VBox card = new VBox(0);
        card.setStyle("-fx-background-color:white;-fx-background-radius:14;");
        shadow(card);

        // Cabecera tarjeta
        HBox cardHeader = cardHeaderBox("Listado de Alertas", C_GRN);
        card.getChildren().add(cardHeader);

        // Encabezado columnas
        String[] cols = {"#", "Tipo", "Barrio", "Estado", "Fecha", "Descripción", "Unidad", "Atendida por"};
        double[] widths = {45, 140, 120, 120, 145, 190, 140, 150};

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

        tbodyRef = new VBox(0);
        renderizarPagina(widths);

        // Footer mostrando rango
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

        int total = alertasCached.size();
        int desde = (paginaActual - 1) * FILAS_POR_PAGINA;
        int hasta = Math.min(desde + FILAS_POR_PAGINA, total);

        if (total == 0) {
            VBox vacio = new VBox(10);
            vacio.setAlignment(Pos.CENTER);
            vacio.setPadding(new Insets(40));
            vacio.getChildren().add(lbl("No hay alertas registradas", 14, GRAY_TEXT, false));
            tbodyRef.getChildren().add(vacio);
            return;
        }

        for (int i = desde; i < hasta; i++) {
            Alerta al = alertasCached.get(i);
            boolean par = i % 2 == 0;

            final int idAlerta = al.getId_alerta();
            AtencionAlerta aten = atencionesCached.stream()
                    .filter(a -> a.getAlerta() != null && a.getAlerta().getId_alerta() == idAlerta)
                    .findFirst().orElse(null);

            String unidad = aten != null && aten.getUnidad() != null ? aten.getUnidad().getNombre() : "—";
            String policia = "—";
            if (aten != null && aten.getUnidad() != null
                    && aten.getUnidad().getPolicias() != null
                    && !aten.getUnidad().getPolicias().isEmpty()) {
                Policia p = aten.getUnidad().getPolicias().get(0);
                policia = p.getPrimer_nombre() + " " + p.getPrimer_apellido();
            }

            String tipo = al.getTipoalerta() != null ? al.getTipoalerta().getNombre() : "—";
            String barrio = al.getBarrio() != null ? al.getBarrio().getNombre() : "—";
            String estado = al.getEstado() != null ? al.getEstado().name() : "—";
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

            // Col 1: ID con círculo de color
            StackPane idBadge = new StackPane();
            Circle idCircle = new Circle(16, Color.web(C_BLU_BG));
            Label idLbl = lbl(String.valueOf(al.getId_alerta()), 11, C_BLU, true);
            idBadge.getChildren().addAll(idCircle, idLbl);
            HBox idBox = new HBox(idBadge);
            idBox.setAlignment(Pos.CENTER_LEFT);
            idBox.setPrefWidth(widths[0]);
            idBox.setMinWidth(widths[0]);

            // Col 2: Tipo con badge de color
            Label tipoBadge = new Label(tipo);
            tipoBadge.setStyle("-fx-background-color:" + C_RED_BG + ";-fx-text-fill:" + C_RED + ";"
                    + "-fx-font-size:11px;-fx-font-weight:bold;"
                    + "-fx-background-radius:20;-fx-padding:3 9;");
            HBox tipoBox = hboxCell(tipoBadge, widths[1]);

            // Col 3: Barrio
            Label barrioLbl = celdaFija(barrio, widths[2]);

            // Col 4: Estado con badge
            Label estadoBadge = new Label(estado);
            estadoBadge.setStyle("-fx-background-color:" + estadoBg(estado) + ";"
                    + "-fx-text-fill:" + estadoColor(estado) + ";"
                    + "-fx-font-size:11px;-fx-font-weight:bold;"
                    + "-fx-background-radius:20;-fx-padding:3 9;");
            HBox estadoBox = hboxCell(estadoBadge, widths[3]);

            // Col 5–8: texto simple
            Label fechaLbl = celdaFija(fecha, widths[4]);
            Label descLbl = celdaFija(desc, widths[5]);
            Label unidadLbl = celdaFija(unidad, widths[6]);
            Label polLbl = celdaFija(policia, widths[7]);

            fila.getChildren().addAll(idBox, tipoBox, barrioLbl, estadoBox,
                    fechaLbl, descLbl, unidadLbl, polLbl);
            tbodyRef.getChildren().add(fila);
        }

        actualizarLblMostrando();
        actualizarPaginacion(widths);
    }

    private void actualizarLblMostrando() {
        if (lblMostrandoRef == null) {
            return;
        }
        int total = alertasCached != null ? alertasCached.size() : 0;
        int desde = (paginaActual - 1) * FILAS_POR_PAGINA;
        int hasta = Math.min(desde + FILAS_POR_PAGINA, total);
        lblMostrandoRef.setText("Mostrando " + (total == 0 ? 0 : desde + 1)
                + " – " + hasta + " de " + total + " alertas");
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

        int total = alertasCached != null ? alertasCached.size() : 0;
        int totalPags = (int) Math.ceil((double) total / FILAS_POR_PAGINA);
        if (totalPags <= 1) {
            return;
        }

        paginacionBoxRef.getChildren().add(btnPag("‹", paginaActual > 1, () -> {
            paginaActual--;
            renderizarPagina(widths);
        }));

        int ini = Math.max(1, paginaActual - 2);
        int fin = Math.min(totalPags, paginaActual + 2);

        if (ini > 1) {
            paginacionBoxRef.getChildren().addAll(
                    btnPag("1", true, () -> {
                        paginaActual = 1;
                        renderizarPagina(widths);
                    }),
                    lbl("…", 13, GRAY_TEXT, false));
        }
        for (int i = ini; i <= fin; i++) {
            final int pg = i;
            paginacionBoxRef.getChildren().add(
                    btnPag(String.valueOf(i), true, () -> {
                        paginaActual = pg;
                        renderizarPagina(widths);
                    }));
        }
        if (fin < totalPags) {
            paginacionBoxRef.getChildren().addAll(
                    lbl("…", 13, GRAY_TEXT, false),
                    btnPag(String.valueOf(totalPags), true, () -> {
                        paginaActual = totalPags;
                        renderizarPagina(widths);
                    }));
        }
        paginacionBoxRef.getChildren().add(btnPag("›", paginaActual < totalPags, () -> {
            paginaActual++;
            renderizarPagina(widths);
        }));
    }

    private Button btnPag(String txt, boolean enabled, Runnable accion) {
        Button b = new Button(txt);
        b.setDisable(!enabled);
        boolean esActual = txt.equals(String.valueOf(paginaActual));
        b.setStyle("-fx-background-color:" + (esActual ? C_BLU : WHITE) + ";"
                + "-fx-text-fill:" + (esActual ? WHITE : "#374151") + ";"
                + "-fx-background-radius:6;-fx-padding:6 11;-fx-cursor:hand;"
                + "-fx-font-size:13px;-fx-border-color:" + BORDER + ";-fx-border-radius:6;");
        b.setOnAction(e -> accion.run());
        return b;
    }

    // ═════════════════════════════════════════════════════════════
    // EXPORTAR EXCEL  (sin cambios funcionales)
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

            String[] headers = {"ID", "Tipo Alerta", "Barrio", "Estado", "Fecha",
                "Descripción", "Latitud", "Longitud", "Unidad Asignada", "Atendida por"};
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
    // HELPERS UI
    // ═════════════════════════════════════════════════════════════
    private HBox cardHeaderBox(String title, String accentColor) {
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(14, 20, 12, 20));
        header.setStyle("-fx-border-color:transparent transparent " + BORDER + " transparent;"
                + "-fx-border-width:0 0 1 0;");

        Rectangle accent = new Rectangle(4, 20);
        accent.setArcWidth(4);
        accent.setArcHeight(4);
        accent.setFill(Color.web(accentColor));

        Label titleLbl = new Label(title);
        titleLbl.setFont(Font.font("System", FontWeight.BOLD, 14));
        titleLbl.setTextFill(Color.web("#111827"));

        header.getChildren().addAll(accent, titleLbl);
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

    // ── Colores de estado para alertas ───────────────────────────
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
