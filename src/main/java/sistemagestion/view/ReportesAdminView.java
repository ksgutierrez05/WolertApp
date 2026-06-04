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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javafx.stage.FileChooser;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import sistemagestion.model.Alerta;
import sistemagestion.model.EstadoUsuario;
import sistemagestion.model.Usuario;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import sistemagestion.service.*;

public class ReportesAdminView {

    // ── Paleta ─────────────────────────────────────────────────────
    private static final String WHITE     = "#ffffff";
    private static final String BG        = "#f4f6fb";
    private static final String BORDER    = "#e5e7eb";
    private static final String GRAY_TEXT = "#6b7280";

    private static final String C_RED     = "#e53935";
    private static final String C_RED_BG  = "#fff0f0";
    private static final String C_ORG     = "#fb8c00";
    private static final String C_ORG_BG  = "#fff8e1";
    private static final String C_GRN     = "#43a047";
    private static final String C_GRN_BG  = "#e8f5e9";
    private static final String C_BLU     = "#1565c0";
    private static final String C_BLU_BG  = "#e8f0fe";
    private static final String C_PUR     = "#7b1fa2";
    private static final String C_PUR_BG  = "#f3e5f5";
    private static final String C_TEA     = "#00695c";
    private static final String C_TEA_BG  = "#e0f2f1";
    private static final String C_AMB     = "#f9a825";
    private static final String C_AMB_BG  = "#fffde7";

    // ── Font Awesome unicode ────────────────────────────────────────
    private static final String FA_USER        = "\uf007";
    private static final String FA_USERS       = "\uf0c0";
    private static final String FA_USER_CHECK  = "\uf4fc";
    private static final String FA_USER_SLASH  = "\uf506";
    private static final String FA_BELL        = "\uf0f3";
    private static final String FA_CHART       = "\uf080";
    private static final String FA_LIST        = "\uf03a";
    private static final String FA_FILTER      = "\uf0b0";
    private static final String FA_TIMES       = "\uf00d";
    private static final String FA_SEARCH      = "\uf002";
    private static final String FA_DOWNLOAD    = "\uf019";
    private static final String FA_SHIELD      = "\uf505";
    private static final String FA_CLOCK       = "\uf017";
    private static final String FA_TOGGLE_ON   = "\uf205";
    private static final String FA_TOGGLE_OFF  = "\uf204";
    private static final String FA_BAN         = "\uf05e";

    // ── Servicios ──────────────────────────────────────────────────
    private final UsuarioService usuarioService;
    private final AlertaService  alertaService;

    // ── Paginación ─────────────────────────────────────────────────
    private static final int FILAS_POR_PAGINA = 8;
    private int paginaActual = 1;

    private List<Usuario> usuariosCached;
    private List<Usuario> usuariosFiltrados;
    private List<Alerta>  alertasCached;

    private VBox  tbodyRef;
    private Label lblMostrandoRef;
    private HBox  paginacionBoxRef;

    // ── Controles de filtro ────────────────────────────────────────
    private ComboBox<String> cbEstado;
    private ComboBox<String> cbRol;
    private TextField        txtBuscar;

    // ── Anchos de columnas ─────────────────────────────────────────
    // #  | Nombre | Username | Correo | Teléfono | Estado | Rol | #Alertas
    private static final double[] WIDTHS = {45, 180, 130, 185, 115, 100, 100, 85};

    // ══════════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ══════════════════════════════════════════════════════════════
    public ReportesAdminView(UsuarioService usuarioService,
                             AlertaService  alertaService) {
        this.usuarioService = usuarioService;
        this.alertaService  = alertaService;
    }

    // ══════════════════════════════════════════════════════════════
    // PUNTO DE ENTRADA
    // ══════════════════════════════════════════════════════════════
    public ScrollPane build() {
        Font.loadFont(getClass().getResourceAsStream("/fa-solid-900.ttf"), 16);

        try {
            usuariosCached = usuarioService != null ? usuarioService.listar() : List.of();
        } catch (Exception e) {
            usuariosCached = List.of();
        }
        try {
            alertasCached = alertaService != null ? alertaService.listar() : List.of();
        } catch (Exception e) {
            alertasCached = List.of();
        }
        usuariosFiltrados = usuariosCached;

        VBox content = new VBox(20);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color:" + BG + ";");

        content.getChildren().add(buildTopBar());
        content.getChildren().add(buildStatsRow());

        try {
            content.getChildren().add(buildSeccionLabel("Distribución de Usuarios"));
            content.getChildren().add(buildDetailCard());
            content.getChildren().add(buildSeccionLabel("Reporte de Usuarios"));
            content.getChildren().add(buildFiltrosCard());
            content.getChildren().add(buildTablaUsuarios());
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

    // ══════════════════════════════════════════════════════════════
    // HELPERS DE ESTADO — usan EstadoUsuario enum real
    // ══════════════════════════════════════════════════════════════
    /** Devuelve true si el usuario está ACTIVO según EstadoUsuario */
    private boolean esActivo(Usuario u) {
        return u.getEstado() == EstadoUsuario.ACTIVO;
    }

    /** Texto legible del estado */
    private String textoEstado(Usuario u) {
        if (u.getEstado() == null) return "—";
        return switch (u.getEstado()) {
            case ACTIVO    -> "Activo";
            case INACTIVO  -> "Inactivo";
            case SUSPENDIDO -> "Suspendido";
        };
    }

    private String colorEstado(Usuario u) {
        if (u.getEstado() == null) return GRAY_TEXT;
        return switch (u.getEstado()) {
            case ACTIVO    -> C_GRN;
            case INACTIVO  -> C_RED;
            case SUSPENDIDO -> C_AMB;
        };
    }

    private String bgEstado(Usuario u) {
        if (u.getEstado() == null) return "#f3f4f6";
        return switch (u.getEstado()) {
            case ACTIVO    -> C_GRN_BG;
            case INACTIVO  -> C_RED_BG;
            case SUSPENDIDO -> C_AMB_BG;
        };
    }

    private String iconEstado(Usuario u) {
        if (u.getEstado() == null) return FA_USER;
        return switch (u.getEstado()) {
            case ACTIVO    -> FA_TOGGLE_ON;
            case INACTIVO  -> FA_TOGGLE_OFF;
            case SUSPENDIDO -> FA_BAN;
        };
    }

    /**
     * Cuenta alertas de un usuario comparando por username (cedula en Alerta
     * solo mapea "CEDULA" del cursor, y username sí se mapea en AlertaDAO).
     */
    private long contarAlertasUsuario(Usuario u) {
        if (u.getUsername() == null) return 0;
        return alertasCached.stream()
                .filter(a -> a.getUsuario() != null
                          && u.getUsername().equals(a.getUsuario().getUsername()))
                .count();
    }

    // ══════════════════════════════════════════════════════════════
    // TOP BAR
    // ══════════════════════════════════════════════════════════════
    private HBox buildTopBar() {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);

        StackPane titleIcon = faIconBox(FA_USERS, 22, C_PUR, C_PUR_BG, 44);

        VBox titles = new VBox(4);
        Label title = new Label("Reportes de Usuarios");
        title.setFont(Font.font("System", FontWeight.BOLD, 26));
        title.setTextFill(Color.web("#111827"));
        Label sub = lbl("Gestión y actividad de usuarios registrados en el sistema", 13, GRAY_TEXT, false);
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

        String base  = "-fx-background-color:linear-gradient(to right,#16283d,#1f3a56);"
                     + "-fx-background-radius:8;-fx-padding:10 18;-fx-cursor:hand;";
        String hover = "-fx-background-color:linear-gradient(to right,#0f1e30,#16283d);"
                     + "-fx-background-radius:8;-fx-padding:10 18;-fx-cursor:hand;";
        btnExportar.setStyle(base);
        btnExportar.setOnMouseEntered(e -> btnExportar.setStyle(hover));
        btnExportar.setOnMouseExited (e -> btnExportar.setStyle(base));
        btnExportar.setOnAction(e -> {
            if (usuariosFiltrados != null)
                exportarExcel(usuariosFiltrados, alertasCached, btnExportar);
        });
        right.getChildren().add(btnExportar);

        bar.getChildren().addAll(titleRow, right);
        return bar;
    }

    // ══════════════════════════════════════════════════════════════
    // STATS ROW
    // ══════════════════════════════════════════════════════════════
    private HBox buildStatsRow() {
        HBox row = new HBox(16);
        HBox.setHgrow(row, Priority.ALWAYS);
        try {
            long totalUsuarios  = usuariosCached.size();
            long activos        = usuariosCached.stream()
                                    .filter(u -> u.getEstado() == EstadoUsuario.ACTIVO).count();
            long inactivos      = usuariosCached.stream()
                                    .filter(u -> u.getEstado() == EstadoUsuario.INACTIVO).count();
            long suspendidos    = usuariosCached.stream()
                                    .filter(u -> u.getEstado() == EstadoUsuario.SUSPENDIDO).count();
            long totalAlertas   = alertasCached.size();
            long conAlertas     = usuariosCached.stream()
                                    .filter(u -> contarAlertasUsuario(u) > 0).count();

            row.getChildren().addAll(
                    statCard(C_PUR_BG, C_PUR, FA_USERS,      "Total Usuarios",    totalUsuarios,  "Registrados en el sistema"),
                    statCard(C_GRN_BG, C_GRN, FA_USER_CHECK, "Activos",           activos,        "Acceso habilitado"),
                    statCard(C_RED_BG, C_RED, FA_USER_SLASH,  "Inactivos",         inactivos,      "Acceso deshabilitado"),
                    statCard(C_AMB_BG, C_AMB, FA_BAN,         "Suspendidos",       suspendidos,    "Cuenta suspendida"),
                    statCard(C_BLU_BG, C_BLU, FA_BELL,        "Total Alertas",     totalAlertas,   "Generadas por usuarios"),
                    statCard(C_TEA_BG, C_TEA, FA_SHIELD,      "Con Alertas",       conAlertas,     "Usuarios con ≥1 alerta")
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
        bar.setHeight(3); bar.setArcWidth(3); bar.setArcHeight(3);
        bar.setFill(Color.web(accent));
        bar.widthProperty().bind(card.widthProperty().subtract(40));

        VBox textBox = new VBox(2, titleLbl, valueLbl, subLbl);
        HBox top = new HBox(14, iconWrap, textBox);
        top.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(top, bar);
        card.setOnMouseEntered(e -> card.setTranslateY(-3));
        card.setOnMouseExited (e -> card.setTranslateY(0));
        return card;
    }

    // ══════════════════════════════════════════════════════════════
    // TARJETA DETALLE
    // ══════════════════════════════════════════════════════════════
    private VBox buildDetailCard() {
        long activos    = usuariosCached.stream().filter(u -> u.getEstado() == EstadoUsuario.ACTIVO).count();
        long inactivos  = usuariosCached.stream().filter(u -> u.getEstado() == EstadoUsuario.INACTIVO).count();
        long suspendidos= usuariosCached.stream().filter(u -> u.getEstado() == EstadoUsuario.SUSPENDIDO).count();

        // Top 3 usuarios con más alertas (comparando por username)
        Map<String, Long> alertasPorUsername = alertasCached.stream()
                .filter(a -> a.getUsuario() != null && a.getUsuario().getUsername() != null)
                .collect(Collectors.groupingBy(
                        a -> a.getUsuario().getUsername(), Collectors.counting()));

        VBox card = new VBox(0);
        card.setStyle("-fx-background-color:white;-fx-background-radius:14;");
        shadow(card);
        card.getChildren().add(cardHeaderBox("Distribución de actividad", C_PUR, FA_CHART));

        String[][] items = {
            {FA_USER_CHECK, "Usuarios con acceso activo",    String.valueOf(activos),    C_GRN, C_GRN_BG},
            {FA_USER_SLASH, "Usuarios inactivos",             String.valueOf(inactivos),  C_RED, C_RED_BG},
            {FA_BAN,        "Usuarios suspendidos",           String.valueOf(suspendidos),C_AMB, C_AMB_BG},
            {FA_BELL,       "Alertas totales generadas",
                String.valueOf(alertasCached.size()), C_BLU, C_BLU_BG},
        };

        for (int i = 0; i < items.length; i++) {
            card.getChildren().add(
                buildDetailRow(items[i][0], items[i][1], items[i][2], items[i][3], items[i][4]));
            if (i < items.length - 1) card.getChildren().add(divider());
        }

        // Top usuarios con más alertas
        card.getChildren().add(divider());
        card.getChildren().add(buildTopUsuariosRow(alertasPorUsername));
        return card;
    }

    private HBox buildTopUsuariosRow(Map<String, Long> alertasPorUsername) {
        HBox row = new HBox(14);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(14, 20, 14, 20));

        StackPane iconBox = faIconBox(FA_CHART, 17, C_ORG, C_ORG_BG, 38);
        Label lbl = new Label("Top usuarios con más alertas:");
        lbl.setFont(Font.font("System", 13));
        lbl.setTextFill(Color.web("#374151"));
        HBox.setHgrow(lbl, Priority.ALWAYS);

        HBox badges = new HBox(8);
        badges.setAlignment(Pos.CENTER_RIGHT);

        alertasPorUsername.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(3)
                .forEach(entry -> {
                    // Buscar el nombre completo del usuario por username
                    String display = usuariosCached.stream()
                            .filter(u -> entry.getKey().equals(u.getUsername()))
                            .findFirst()
                            .map(u -> nombreCompleto(u))
                            .orElse(entry.getKey());

                    Label badge = new Label(display + " (" + entry.getValue() + ")");
                    badge.setStyle("-fx-background-color:" + C_ORG_BG
                            + ";-fx-text-fill:" + C_ORG
                            + ";-fx-background-radius:20;"
                            + "-fx-padding:3 10;-fx-font-size:11px;-fx-font-weight:bold;");
                    badges.getChildren().add(badge);
                });

        if (badges.getChildren().isEmpty()) {
            Label ninguno = new Label("Sin alertas registradas");
            ninguno.setStyle("-fx-font-size:12px;-fx-text-fill:" + GRAY_TEXT + ";");
            badges.getChildren().add(ninguno);
        }

        row.getChildren().addAll(iconBox, lbl, badges);
        return row;
    }

    private HBox buildDetailRow(String faIcon, String labelText, String value,
                                String color, String bgColor) {
        HBox row = new HBox(14);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(14, 20, 14, 20));
        row.setStyle("-fx-background-color:transparent;");
        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color:#f8f9fd;"));
        row.setOnMouseExited (e -> row.setStyle("-fx-background-color:transparent;"));

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
    // PANEL DE FILTROS
    // ══════════════════════════════════════════════════════════════
    private VBox buildFiltrosCard() {
        VBox card = new VBox(0);
        card.setStyle("-fx-background-color:white;-fx-background-radius:14;");
        shadow(card);
        card.getChildren().add(cardHeaderBox("Filtrar usuarios", C_PUR, FA_FILTER));

        // Estado según EstadoUsuario real
        cbEstado = comboFiltro("Todos los estados");
        cbEstado.getItems().addAll("Activo", "Inactivo", "Suspendido");

        // Roles únicos desde los datos
        cbRol = comboFiltro("Todos los roles");
        usuariosCached.stream()
                .filter(u -> u.getRol() != null && u.getRol().getNombre() != null)
                .map(u -> u.getRol().getNombre())
                .distinct().sorted()
                .forEach(r -> cbRol.getItems().add(r));

        txtBuscar = new TextField();
        txtBuscar.setPromptText("Buscar por nombre, username o correo…");
        txtBuscar.setPrefWidth(250);
        txtBuscar.setStyle(estiloInput());
        HBox.setHgrow(txtBuscar, Priority.ALWAYS);

        HBox fila1 = new HBox(16,
                labeledControl(labelFiltro("Estado:"),  cbEstado, 130),
                labeledControl(labelFiltro("Rol:"),     cbRol,    130),
                labeledControl(labelFiltro("Buscar:"),  txtBuscar, -1)
        );
        fila1.setAlignment(Pos.CENTER_LEFT);
        fila1.setPadding(new Insets(14, 20, 10, 20));

        Button btnAplicar = accionBtn("Aplicar filtros", FA_FILTER, C_PUR,
                "-fx-background-color:" + C_PUR + ";-fx-text-fill:white;"
                + "-fx-background-radius:8;-fx-padding:8 18;-fx-cursor:hand;-fx-font-weight:bold;");
        btnAplicar.setOnAction(e -> aplicarFiltros());

        Button btnLimpiar = accionBtn("Limpiar", FA_TIMES, C_RED,
                "-fx-background-color:" + C_RED_BG + ";-fx-text-fill:" + C_RED + ";"
                + "-fx-background-radius:8;-fx-padding:8 18;-fx-cursor:hand;-fx-font-weight:bold;");
        btnLimpiar.setOnAction(e -> limpiarFiltros());

        HBox fila2 = new HBox(16, btnAplicar, btnLimpiar);
        fila2.setAlignment(Pos.CENTER_LEFT);
        fila2.setPadding(new Insets(0, 20, 14, 20));

        card.getChildren().addAll(fila1, fila2);
        return card;
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

    private Button accionBtn(String texto, String icon, String color, String style) {
        Button b = new Button();
        boolean isDark = color.equals(C_PUR) || color.equals(C_BLU);
        Label ico = faLabel(icon, 12, isDark ? WHITE : color);
        Label txt = new Label("  " + texto);
        txt.setStyle("-fx-text-fill:" + (isDark ? "white" : color) + ";"
                + "-fx-font-size:12px;-fx-font-weight:bold;");
        HBox content = new HBox(4, ico, txt);
        content.setAlignment(Pos.CENTER);
        b.setGraphic(content);
        b.setStyle(style);
        return b;
    }

    private String estiloInput() {
        return "-fx-background-color:white;-fx-border-color:" + BORDER + ";"
                + "-fx-border-radius:8;-fx-background-radius:8;-fx-padding:6 10;-fx-font-size:12px;";
    }

    // ── Lógica de filtrado ────────────────────────────────────────
    private void aplicarFiltros() {
        String estadoSel = cbEstado.getValue();
        String rol       = cbRol.getValue();
        String buscar    = txtBuscar.getText() != null
                         ? txtBuscar.getText().trim().toLowerCase() : "";

        usuariosFiltrados = usuariosCached.stream()
                .filter(u -> {
                    if (estadoSel == null) return true;
                    return textoEstado(u).equals(estadoSel);
                })
                .filter(u -> rol == null
                        || (u.getRol() != null && rol.equals(u.getRol().getNombre())))
                .filter(u -> buscar.isEmpty()
                        || nombreCompleto(u).toLowerCase().contains(buscar)
                        || (u.getUsername() != null && u.getUsername().toLowerCase().contains(buscar))
                        || (u.getCorreo() != null && u.getCorreo().toLowerCase().contains(buscar)))
                .collect(Collectors.toList());

        paginaActual = 1;
        renderizarPagina(WIDTHS);
    }

    private void limpiarFiltros() {
        cbEstado.setValue(null);
        cbRol.setValue(null);
        txtBuscar.clear();
        usuariosFiltrados = usuariosCached;
        paginaActual = 1;
        renderizarPagina(WIDTHS);
    }

    // ══════════════════════════════════════════════════════════════
    // TABLA DE USUARIOS
    // ══════════════════════════════════════════════════════════════
    private VBox buildTablaUsuarios() {
        usuariosFiltrados = usuariosCached;

        VBox card = new VBox(0);
        card.setStyle("-fx-background-color:white;-fx-background-radius:14;");
        shadow(card);
        card.getChildren().add(cardHeaderBox("Listado de Usuarios", C_PUR, FA_LIST));

        String[] cols = {"#", "Nombre Completo", "Username", "Correo Electrónico",
                         "Teléfono", "Estado", "Rol", "Alertas"};
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

        int total = usuariosFiltrados.size();
        int desde = (paginaActual - 1) * FILAS_POR_PAGINA;
        int hasta = Math.min(desde + FILAS_POR_PAGINA, total);

        if (total == 0) {
            VBox vacio = new VBox(10);
            vacio.setAlignment(Pos.CENTER);
            vacio.setPadding(new Insets(40));
            Label faVacio = faLabel(FA_SEARCH, 32, GRAY_TEXT);
            Label msg = lbl("No hay usuarios que coincidan con los filtros", 14, GRAY_TEXT, false);
            vacio.getChildren().addAll(faVacio, msg);
            tbodyRef.getChildren().add(vacio);
            actualizarLblMostrando();
            actualizarPaginacion(widths);
            return;
        }

        for (int i = desde; i < hasta; i++) {
            Usuario u   = usuariosFiltrados.get(i);
            boolean par = i % 2 == 0;

            long   numAlertas = contarAlertasUsuario(u);
            String nombre     = nombreCompleto(u);
            String username   = u.getUsername()  != null ? u.getUsername()  : "—";
            String correo     = u.getCorreo()    != null ? u.getCorreo()    : "—";
            String telefono   = u.getTelefono()  != null ? u.getTelefono()  : "—";
            String rol        = u.getRol()       != null ? u.getRol().getNombre() : "—";

            HBox fila = new HBox(0);
            fila.setAlignment(Pos.CENTER_LEFT);
            fila.setPadding(new Insets(10, 16, 10, 16));
            String bgN = "-fx-background-color:" + (par ? WHITE : "#fafbfd") + ";"
                       + "-fx-border-color:transparent transparent " + BORDER + " transparent;"
                       + "-fx-border-width:0 0 1 0;";
            fila.setStyle(bgN);
            fila.setOnMouseEntered(e -> fila.setStyle(
                    "-fx-background-color:#F5F3FF;"
                    + "-fx-border-color:transparent transparent " + BORDER + " transparent;"
                    + "-fx-border-width:0 0 1 0;-fx-cursor:hand;"));
            fila.setOnMouseExited(e -> fila.setStyle(bgN));

            // Col 1: número de fila
            StackPane idBadge  = new StackPane();
            Circle    idCircle = new Circle(16, Color.web(C_PUR_BG));
            Label     idLbl    = lbl(String.valueOf(i + 1), 11, C_PUR, true);
            idBadge.getChildren().addAll(idCircle, idLbl);
            HBox idBox = new HBox(idBadge);
            idBox.setAlignment(Pos.CENTER_LEFT);
            idBox.setPrefWidth(widths[0]);
            idBox.setMinWidth(widths[0]);

            // Col 2: Nombre con avatar inicial
            String inicial = nombre.isEmpty() ? "?" : String.valueOf(nombre.charAt(0)).toUpperCase();
            StackPane avatar = new StackPane();
            Circle    av     = new Circle(15, Color.web(esActivo(u) ? C_PUR_BG : "#f3f4f6"));
            Label     avLbl  = lbl(inicial, 12, esActivo(u) ? C_PUR : GRAY_TEXT, true);
            avatar.getChildren().addAll(av, avLbl);
            Label nombreLbl = new Label(nombre);
            nombreLbl.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:#111827;");
            nombreLbl.setMaxWidth(widths[1] - 40);
            nombreLbl.setTextOverrun(javafx.scene.control.OverrunStyle.ELLIPSIS);
            HBox nombreBox  = new HBox(6, avatar, nombreLbl);
            nombreBox.setAlignment(Pos.CENTER_LEFT);
            HBox nombreCell = hboxCell(nombreBox, widths[1]);

            // Col 3: Username
            Label userLbl = celdaFija(username, widths[2]);

            // Col 4: Correo
            Label correoLbl = celdaFija(correo, widths[3]);

            // Col 5: Teléfono
            Label telLbl = celdaFija(telefono, widths[4]);

            // Col 6: Estado (badge usando EstadoUsuario real)
            String estColor = colorEstado(u);
            String estBg    = bgEstado(u);
            String estIcoU  = iconEstado(u);
            String estTxt   = textoEstado(u);

            Label  estIco   = faLabel(estIcoU, 11, estColor);
            Label  estLbl   = new Label(" " + estTxt);
            estLbl.setStyle("-fx-font-size:11px;-fx-font-weight:bold;-fx-text-fill:" + estColor + ";");
            HBox estBadge = new HBox(2, estIco, estLbl);
            estBadge.setAlignment(Pos.CENTER_LEFT);
            estBadge.setPadding(new Insets(3, 9, 3, 9));
            estBadge.setStyle("-fx-background-color:" + estBg + ";-fx-background-radius:20;");
            HBox estCell = hboxCell(estBadge, widths[5]);

            // Col 7: Rol
            Label rolLbl = celdaFija(rol, widths[6]);

            // Col 8: Alertas (badge numérico con color por cantidad)
            String alertaColor = numAlertas == 0 ? GRAY_TEXT : (numAlertas > 5 ? C_RED : C_ORG);
            String alertaBg    = numAlertas == 0 ? "#f3f4f6"  : (numAlertas > 5 ? C_RED_BG : C_ORG_BG);
            Label alertaBadge  = new Label(String.valueOf(numAlertas));
            alertaBadge.setStyle("-fx-background-color:" + alertaBg + ";-fx-text-fill:" + alertaColor + ";"
                    + "-fx-background-radius:20;-fx-padding:3 12;-fx-font-size:12px;-fx-font-weight:bold;");
            HBox alertaCell = hboxCell(alertaBadge, widths[7]);

            fila.getChildren().addAll(idBox, nombreCell, userLbl, correoLbl,
                    telLbl, estCell, rolLbl, alertaCell);
            tbodyRef.getChildren().add(fila);
        }

        actualizarLblMostrando();
        actualizarPaginacion(widths);
    }

    private void actualizarLblMostrando() {
        if (lblMostrandoRef == null) return;
        int total = usuariosFiltrados != null ? usuariosFiltrados.size() : 0;
        int desde = (paginaActual - 1) * FILAS_POR_PAGINA;
        int hasta = Math.min(desde + FILAS_POR_PAGINA, total);
        lblMostrandoRef.setText("Mostrando " + (total == 0 ? 0 : desde + 1)
                + " – " + hasta + " de " + total + " usuarios"
                + (total < usuariosCached.size()
                    ? " (filtrados de " + usuariosCached.size() + " totales)" : ""));
    }

    // ══════════════════════════════════════════════════════════════
    // PAGINACIÓN
    // ══════════════════════════════════════════════════════════════
    private HBox buildPaginacion() {
        paginacionBoxRef = new HBox(6);
        paginacionBoxRef.setAlignment(Pos.CENTER_RIGHT);
        paginacionBoxRef.setPadding(new Insets(4, 0, 0, 0));
        return paginacionBoxRef;
    }

    private void actualizarPaginacion(double[] widths) {
        if (paginacionBoxRef == null) return;
        paginacionBoxRef.getChildren().clear();

        int total     = usuariosFiltrados != null ? usuariosFiltrados.size() : 0;
        int totalPags = (int) Math.ceil((double) total / FILAS_POR_PAGINA);
        if (totalPags <= 1) return;

        paginacionBoxRef.getChildren().add(btnPagNav("\uf104", paginaActual > 1, () -> {
            paginaActual--;
            renderizarPagina(widths);
        }));

        int ini = Math.max(1, paginaActual - 2);
        int fin = Math.min(totalPags, paginaActual + 2);
        if (ini > 1) {
            paginacionBoxRef.getChildren().add(btnPagNum(1, widths));
            if (ini > 2) paginacionBoxRef.getChildren().add(puntosSuspensivos());
        }
        for (int i = ini; i <= fin; i++)
            paginacionBoxRef.getChildren().add(btnPagNum(i, widths));
        if (fin < totalPags) {
            if (fin < totalPags - 1) paginacionBoxRef.getChildren().add(puntosSuspensivos());
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
        b.setPrefSize(34, 34); b.setMinSize(34, 34); b.setMaxSize(34, 34);
        b.setFont(Font.font("System", esActual ? FontWeight.BOLD : FontWeight.NORMAL, 13));
        String styleBase = esActual
                ? "-fx-background-color:" + C_PUR + ";-fx-text-fill:white;-fx-background-radius:8;"
                + "-fx-cursor:hand;-fx-border-color:" + C_PUR + ";-fx-border-radius:8;-fx-border-width:1.5;"
                : "-fx-background-color:" + WHITE + ";-fx-text-fill:#374151;-fx-background-radius:8;"
                + "-fx-cursor:hand;-fx-border-color:" + BORDER + ";-fx-border-radius:8;-fx-border-width:1.5;";
        String styleHover = "-fx-background-color:" + C_PUR_BG + ";-fx-text-fill:" + C_PUR + ";"
                + "-fx-background-radius:8;-fx-cursor:hand;-fx-border-color:" + C_PUR + ";"
                + "-fx-border-radius:8;-fx-border-width:1.5;";
        b.setStyle(styleBase);
        if (!esActual) {
            b.setOnMouseEntered(e -> b.setStyle(styleHover));
            b.setOnMouseExited (e -> b.setStyle(styleBase));
        }
        b.setOnAction(e -> { paginaActual = pg; renderizarPagina(widths); });
        return b;
    }

    private Button btnPagNav(String faUnicode, boolean enabled, Runnable accion) {
        Label ico = new Label(faUnicode);
        ico.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';-fx-font-size:13px;"
                + "-fx-text-fill:" + (enabled ? "#374151" : "#d1d5db") + ";");
        Button b = new Button();
        b.setGraphic(ico);
        b.setPrefSize(34, 34); b.setMinSize(34, 34); b.setMaxSize(34, 34);
        b.setDisable(!enabled);
        String styleBase  = "-fx-background-color:" + WHITE + ";-fx-background-radius:8;"
                + "-fx-cursor:" + (enabled ? "hand" : "default") + ";"
                + "-fx-border-color:" + BORDER + ";-fx-border-radius:8;-fx-border-width:1.5;";
        String styleHover = "-fx-background-color:" + C_PUR_BG + ";-fx-background-radius:8;"
                + "-fx-cursor:hand;-fx-border-color:" + C_PUR + ";-fx-border-radius:8;-fx-border-width:1.5;";
        b.setStyle(styleBase);
        if (enabled) {
            b.setOnMouseEntered(e -> b.setStyle(styleHover));
            b.setOnMouseExited (e -> b.setStyle(styleBase));
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

    // ══════════════════════════════════════════════════════════════
    // EXPORTAR EXCEL
    // ══════════════════════════════════════════════════════════════
    private void exportarExcel(List<Usuario> usuarios, List<Alerta> alertas,
                               javafx.scene.Node owner) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Guardar reporte de usuarios");
        fc.setInitialFileName("Reporte_Usuarios_WolertApp.xlsx");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel", "*.xlsx"));
        File file = fc.showSaveDialog(owner.getScene().getWindow());
        if (file == null) return;

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Usuarios");

            // Estilo encabezado
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

            // Estilo filas normales
            CellStyle nStyle = wb.createCellStyle();
            org.apache.poi.ss.usermodel.Font nFont = wb.createFont();
            nFont.setFontName("Arial");
            nFont.setFontHeightInPoints((short) 10);
            nStyle.setFont(nFont);
            nStyle.setBorderBottom(BorderStyle.THIN);
            nStyle.setBorderLeft(BorderStyle.THIN);
            nStyle.setBorderRight(BorderStyle.THIN);

            // Estilos por estado
            CellStyle activoStyle = wb.createCellStyle();
            activoStyle.cloneStyleFrom(nStyle);
            activoStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            activoStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle inactivoStyle = wb.createCellStyle();
            inactivoStyle.cloneStyleFrom(nStyle);
            inactivoStyle.setFillForegroundColor(IndexedColors.ROSE.getIndex());
            inactivoStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle suspendidoStyle = wb.createCellStyle();
            suspendidoStyle.cloneStyleFrom(nStyle);
            suspendidoStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
            suspendidoStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            String[] headers = {"#", "Primer Nombre", "Segundo Nombre",
                    "Primer Apellido", "Segundo Apellido",
                    "Username", "Correo", "Teléfono", "Cédula",
                    "Rol", "Estado", "Nº Alertas"};

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(hStyle);
            }

            int rowNum = 1;
            for (Usuario u : usuarios) {
                long numAlertas = contarAlertasUsuario(u);

                CellStyle estadoCellStyle = switch (u.getEstado() != null ? u.getEstado() : EstadoUsuario.INACTIVO) {
                    case ACTIVO     -> activoStyle;
                    case INACTIVO   -> inactivoStyle;
                    case SUSPENDIDO -> suspendidoStyle;
                };

                String[] vals = {
                    String.valueOf(rowNum),
                    nvl(u.getPrimer_nombre()),
                    nvl(u.getSegundo_nombre()),
                    nvl(u.getPrimer_apellido()),
                    nvl(u.getSegundo_apellido()),
                    nvl(u.getUsername()),
                    nvl(u.getCorreo()),
                    nvl(u.getTelefono()),
                    nvl(u.getIdentificacion()),
                    u.getRol() != null ? nvl(u.getRol().getNombre()) : "",
                    textoEstado(u),
                    String.valueOf(numAlertas)
                };

                Row row = sheet.createRow(rowNum++);
                for (int i = 0; i < vals.length; i++) {
                    org.apache.poi.ss.usermodel.Cell cell = row.createCell(i);
                    cell.setCellValue(vals[i]);
                    // Pintar solo la columna Estado con el color correspondiente
                    cell.setCellStyle(i == 10 ? estadoCellStyle : nStyle);
                }
            }

            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);

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

    // ══════════════════════════════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════════════════════════════
    private String nombreCompleto(Usuario u) {
        StringBuilder sb = new StringBuilder();
        if (u.getPrimer_nombre()    != null) sb.append(u.getPrimer_nombre()).append(" ");
        if (u.getSegundo_nombre()   != null) sb.append(u.getSegundo_nombre()).append(" ");
        if (u.getPrimer_apellido()  != null) sb.append(u.getPrimer_apellido()).append(" ");
        if (u.getSegundo_apellido() != null) sb.append(u.getSegundo_apellido());
        return sb.toString().trim();
    }

    private String nvl(String s) { return s != null ? s : ""; }

    private Label faLabel(String unicode, double size, String color) {
        Label l = new Label(unicode);
        l.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';"
                + "-fx-font-size:" + size + "px;-fx-text-fill:" + color + ";");
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
        Label ico = faLabel(unicode, iconSize, iconColor);
        box.getChildren().addAll(bg, ico);
        return box;
    }

    private HBox cardHeaderBox(String title, String accentColor, String faIcon) {
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(14, 20, 12, 20));
        header.setStyle("-fx-border-color:transparent transparent " + BORDER + " transparent;"
                + "-fx-border-width:0 0 1 0;");
        Rectangle accent = new Rectangle(4, 20);
        accent.setArcWidth(4); accent.setArcHeight(4);
        accent.setFill(Color.web(accentColor));
        Label ico      = faLabel(faIcon, 15, accentColor);
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
        Label msg  = new Label("Error al cargar datos: " + message);
        msg.setFont(Font.font("System", 12));
        msg.setTextFill(Color.web(C_RED));
        msg.setWrapText(true);
        box.getChildren().addAll(icon, msg);
        return box;
    }

    private Label celdaFija(String txt, double width) {
        Label l = new Label(txt != null ? txt : "—");
        l.setStyle("-fx-font-size:12px;-fx-text-fill:#374151;");
        l.setPrefWidth(width); l.setMinWidth(width); l.setMaxWidth(width);
        l.setTextOverrun(javafx.scene.control.OverrunStyle.ELLIPSIS);
        return l;
    }

    private HBox hboxCell(javafx.scene.Node node, double width) {
        HBox box = new HBox(node);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPrefWidth(width); box.setMinWidth(width); box.setMaxWidth(width);
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
}