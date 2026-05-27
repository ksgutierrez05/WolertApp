/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.view;

import java.sql.SQLException;
import java.util.List;
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
import sistemagestion.model.Barrio;
import sistemagestion.model.Comuna;
import sistemagestion.service.BarrioService;
import sistemagestion.service.ComunaService;

public class BarrioAdminView {

    // ── Colores — idénticos a ComunaAdminView/UsuariosAdminView ──
    private static final String WHITE     = "#ffffff";
    private static final String BG        = "#f4f6fb";
    private static final String BLUE      = "#1565c0";
    private static final String GREEN     = "#43a047";
    private static final String RED       = "#e53935";
    private static final String RED_LIGHT = "#fff0f0";
    private static final String ORANGE    = "#fb8c00";
    private static final String PURPLE    = "#7b1fa2";
    private static final String GRAY_TEXT = "#6b7280";
    private static final String BORDER    = "#e5e7eb";

    private BarrioService  barrioService;
    private ComunaService  comunaService;
    private VBox           tablaContainer;
    private TextField      campoBusqueda;
    private ComboBox<String> filtroComuna;

    private static final int FILAS_POR_PAGINA = 8;
    private int paginaActual = 1;
    private java.util.List<Barrio> barrosFiltrados = new java.util.ArrayList<>();
    private Label lblMostrando;
    private HBox paginacionBox;

    public BarrioAdminView() {
        javafx.scene.text.Font.loadFont(
                getClass().getResourceAsStream("/fa-solid-900.ttf"), 20);
        try {
            barrioService = new BarrioService();
            comunaService = new ComunaService();
        } catch (SQLException e) {
            mostrarAlerta("Error de conexión", e.getMessage());
        }
    }

    public ScrollPane getView() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color: " + BG + ";");

        paginacionBox = new HBox(6);
        paginacionBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        paginacionBox.setPadding(new Insets(4, 0, 0, 0));

        content.getChildren().addAll(
                buildTopBar(),
                buildStatsRow(),
                buildToolbar(),
                buildTabla(),
                paginacionBox
        );

        cargarYRenderizar();

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: " + BG + "; -fx-background: " + BG + ";");
        return scroll;
    }

    // ═══════════════════════════════════════════════════════════════
    // TOP BAR
    // ═══════════════════════════════════════════════════════════════
    private HBox buildTopBar() {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);

        VBox titles = new VBox(4);
        Label title = new Label("Gestión de Barrios");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#111827"));
        Label sub = label("Administra los barrios y su relación con comunas", 13, GRAY_TEXT, false);
        titles.getChildren().addAll(title, sub);

        HBox right = new HBox(12);
        right.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(right, Priority.ALWAYS);

        Button btnNuevo = new Button("+ Nuevo barrio");
        btnNuevo.setStyle(btnPrimaryStyle());
        btnNuevo.setOnMouseEntered(e -> btnNuevo.setStyle(btnPrimaryHoverStyle()));
        btnNuevo.setOnMouseExited(e  -> btnNuevo.setStyle(btnPrimaryStyle()));
        btnNuevo.setOnAction(e -> abrirFormulario(null));

        right.getChildren().add(btnNuevo);
        bar.getChildren().addAll(titles, right);
        return bar;
    }

    // ═══════════════════════════════════════════════════════════════
    // STATS ROW — Font Awesome + boldNum con color
    // ═══════════════════════════════════════════════════════════════
    private HBox buildStatsRow() {
        HBox row = new HBox(16);
        HBox.setHgrow(row, Priority.ALWAYS);

        List<Barrio> barrios = cargarBarrios();
        List<Comuna> comunas = cargarComunas();

        int totalBarrios = barrios.size();
        int totalComunas = comunas.size();
        long comunasConBarrios = barrios.stream()
                .filter(b -> b.getComuna() != null)
                .map(b -> b.getComuna().getId_comuna())
                .distinct().count();
        long sinComuna = barrios.stream()
                .filter(b -> b.getComuna() == null).count();

        Label lblBarriosVal   = boldNum(String.valueOf(totalBarrios),  BLUE);
        Label lblComunasVal   = boldNum(String.valueOf(totalComunas),  GREEN);
        Label lblCoberVal     = boldNum(comunasConBarrios + "/" + totalComunas, ORANGE);
        Label lblSinComunaVal = boldNum(String.valueOf(sinComuna),     sinComuna > 0 ? RED : GRAY_TEXT);

        row.getChildren().addAll(
                statCard("#e8f0fe", BLUE,   "\uf015", "Total barrios",       lblBarriosVal,   "Divisiones registradas"),
                statCard("#e8f5e9", GREEN,  "\uf5a0", "Comunas",             lblComunasVal,   "Con barrios: " + comunasConBarrios),
                statCard("#fff8e1", ORANGE, "\uf279", "Cobertura",           lblCoberVal,     "Comunas cubiertas"),
                statCard(RED_LIGHT, RED,    "\uf071", "Sin comuna",          lblSinComunaVal, "Barrios sin asignar")
        );
        return row;
    }

    private Label boldNum(String val, String color) {
        Label l = new Label(val);
        l.setStyle(
                "-fx-font-size: 36px;"
                + "-fx-font-weight: bold;"
                + "-fx-text-fill: " + color + ";");
        return l;
    }

    private VBox statCard(String bgIcon, String accentColor, String iconFA,
                          String title, Label valueLabel, String sub) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20, 22, 20, 22));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 18;");
        HBox.setHgrow(card, Priority.ALWAYS);
        shadow(card);

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

        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #374151;");

        Label subLbl = new Label(sub);
        subLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: " + GRAY_TEXT + ";");

        VBox textBox = new VBox(3, titleLbl, valueLabel, subLbl);

        HBox top = new HBox(16);
        top.setAlignment(Pos.CENTER_LEFT);
        top.getChildren().addAll(iconWrap, textBox);
        card.getChildren().add(top);

        card.setOnMouseEntered(e -> card.setTranslateY(-3));
        card.setOnMouseExited(e  -> card.setTranslateY(0));
        return card;
    }

    // ═══════════════════════════════════════════════════════════════
    // TOOLBAR
    // ═══════════════════════════════════════════════════════════════
    private HBox buildToolbar() {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(16, 20, 16, 20));
        bar.setStyle("-fx-background-color: white; -fx-background-radius: 12;");
        shadow(bar);

        // Buscador con ícono FA lupa
        HBox searchBox = new HBox(8);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setStyle(
                "-fx-background-color: #f5f7fb;"
                + "-fx-background-radius: 10;"
                + "-fx-padding: 0 14;");
        searchBox.setPrefHeight(42);
        HBox.setHgrow(searchBox, Priority.ALWAYS);

        Label searchIcon = new Label("\uf002");
        searchIcon.setStyle(
                "-fx-font-family: 'Font Awesome 6 Free Solid';"
                + "-fx-font-size: 14px; -fx-text-fill: #9ca3af;");

        campoBusqueda = new TextField();
        campoBusqueda.setPromptText("Buscar barrio por nombre...");
        campoBusqueda.setStyle(
                "-fx-background-color: transparent; -fx-border-color: transparent;"
                + "-fx-font-size: 13px; -fx-text-fill: #111827;");
        campoBusqueda.setPrefHeight(42);
        HBox.setHgrow(campoBusqueda, Priority.ALWAYS);
        campoBusqueda.textProperty().addListener((obs, o, n) -> filtrarYMostrar());
        searchBox.getChildren().addAll(searchIcon, campoBusqueda);

        // Filtro de comuna
        filtroComuna = new ComboBox<>();
        filtroComuna.setPromptText("Todas las comunas");
        filtroComuna.setPrefHeight(42);
        filtroComuna.setPrefWidth(180);
        filtroComuna.setStyle(
                "-fx-background-color: #f5f7fb;"
                + "-fx-background-radius: 10;"
                + "-fx-border-color: transparent;"
                + "-fx-font-size: 13px; -fx-cursor: hand;");
        filtroComuna.getItems().add("Todas las comunas");
        cargarComunas().forEach(c -> filtroComuna.getItems().add(c.getNombre()));
        filtroComuna.setValue("Todas las comunas");
        filtroComuna.setOnAction(e -> filtrarYMostrar());

        bar.getChildren().addAll(searchBox, filtroComuna);
        return bar;
    }

    // ═══════════════════════════════════════════════════════════════
    // TABLA
    // ═══════════════════════════════════════════════════════════════
    private VBox buildTabla() {
        VBox card = new VBox(0);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12;");
        shadow(card);

        // Header
        HBox header = new HBox(0);
        header.setPadding(new Insets(12, 16, 12, 16));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle(
                "-fx-background-color: #f8fafc;"
                + "-fx-background-radius: 12 12 0 0;"
                + "-fx-border-color: transparent transparent " + BORDER + " transparent;"
                + "-fx-border-width: 0 0 1 0;");
        HBox.setHgrow(header, Priority.ALWAYS);

        // Nombre — crece (wrapper HBox para que HGrow funcione)
        HBox hNombreWrap = new HBox();
        HBox.setHgrow(hNombreWrap, Priority.ALWAYS);
        hNombreWrap.getChildren().add(colHeader("Nombre barrio", 0));

        header.getChildren().addAll(
                hNombreWrap,
                colHeaderFixed("Comuna",      180),
                colHeaderFixed("Coordenadas", 180),
                colHeaderFixed("Acciones",    160)
        );
        card.getChildren().add(header);

        tablaContainer = new VBox(0);
        card.getChildren().add(tablaContainer);

        HBox footer = new HBox();
        footer.setPadding(new Insets(12, 16, 12, 16));
        footer.setStyle(
                "-fx-border-color: " + BORDER + " transparent transparent transparent;"
                + "-fx-border-width: 1 0 0 0;");
        lblMostrando = label("Cargando barrios...", 12, GRAY_TEXT, false);
        footer.getChildren().add(lblMostrando);
        card.getChildren().add(footer);

        return card;
    }

    private Label colHeader(String text, double width) {
        Label l = new Label(text.toUpperCase());
        l.setStyle(
                "-fx-font-size: 11px; -fx-font-weight: bold;"
                + "-fx-text-fill: #9ca3af; -fx-letter-spacing: 0.5px;");
        if (width > 0) {
            l.setPrefWidth(width);
            l.setMinWidth(width);
            l.setMaxWidth(width);
        }
        return l;
    }

    private Label colHeaderFixed(String text, double width) {
        Label l = colHeader(text, 0);
        l.setPrefWidth(width);
        l.setMinWidth(width);
        l.setMaxWidth(width);
        return l;
    }

    // ═══════════════════════════════════════════════════════════════
    // CARGA Y FILTRADO
    // ═══════════════════════════════════════════════════════════════
    private void cargarYRenderizar() {
        barrosFiltrados = new java.util.ArrayList<>(cargarBarrios());
        paginaActual = 1;
        renderizarPagina();
    }

    private void filtrarYMostrar() {
        String texto  = campoBusqueda.getText().toLowerCase().trim();
        String comuna = filtroComuna.getValue();

        barrosFiltrados = cargarBarrios().stream()
                .filter(b -> {
                    boolean matchNombre = texto.isEmpty()
                            || (b.getNombre() != null
                                && b.getNombre().toLowerCase().contains(texto));
                    boolean matchComuna = comuna == null
                            || comuna.equals("Todas las comunas")
                            || (b.getComuna() != null
                                && comuna.equals(b.getComuna().getNombre()));
                    return matchNombre && matchComuna;
                })
                .collect(java.util.stream.Collectors.toList());

        paginaActual = 1;
        renderizarPagina();
    }

    private void renderizarPagina() {
        tablaContainer.getChildren().clear();

        int total = barrosFiltrados.size();
        int desde = (paginaActual - 1) * FILAS_POR_PAGINA;
        int hasta = Math.min(desde + FILAS_POR_PAGINA, total);

        if (total == 0) {
            Label vacio = label("No se encontraron barrios", 14, GRAY_TEXT, false);
            VBox.setMargin(vacio, new Insets(30, 16, 30, 16));
            tablaContainer.getChildren().add(vacio);
        } else {
            for (int i = desde; i < hasta; i++) {
                tablaContainer.getChildren().add(buildFila(barrosFiltrados.get(i), i % 2 == 0));
            }
        }

        // Actualizar footer
        if (lblMostrando != null) {
            lblMostrando.setText("Mostrando " + (total == 0 ? 0 : desde + 1)
                    + " – " + hasta + " de " + total + " barrios");
        }

        actualizarPaginacion();
    }

    private void actualizarPaginacion() {
        if (paginacionBox == null) return;
        paginacionBox.getChildren().clear();

        int total = barrosFiltrados.size();
        int totalPaginas = (int) Math.ceil((double) total / FILAS_POR_PAGINA);
        if (totalPaginas <= 1) return;

        paginacionBox.getChildren().add(btnPag("‹", paginaActual > 1, () -> {
            paginaActual--; renderizarPagina();
        }));

        int inicio = Math.max(1, paginaActual - 2);
        int fin    = Math.min(totalPaginas, paginaActual + 2);

        if (inicio > 1) {
            paginacionBox.getChildren().addAll(
                    btnPag("1", true, () -> { paginaActual = 1; renderizarPagina(); }),
                    label("...", 13, GRAY_TEXT, false));
        }
        for (int i = inicio; i <= fin; i++) {
            final int pg = i;
            paginacionBox.getChildren().add(
                    btnPag(String.valueOf(i), true, () -> { paginaActual = pg; renderizarPagina(); }));
        }
        if (fin < totalPaginas) {
            paginacionBox.getChildren().addAll(
                    label("...", 13, GRAY_TEXT, false),
                    btnPag(String.valueOf(totalPaginas), true,
                            () -> { paginaActual = totalPaginas; renderizarPagina(); }));
        }

        paginacionBox.getChildren().add(btnPag("›", paginaActual < totalPaginas, () -> {
            paginaActual++; renderizarPagina();
        }));
    }

    private Button btnPag(String txt, boolean enabled, Runnable accion) {
        Button b = new Button(txt);
        b.setDisable(!enabled);
        boolean esActual = txt.equals(String.valueOf(paginaActual));
        b.setStyle("-fx-background-color: " + (esActual ? BLUE : WHITE) + ";"
                + "-fx-text-fill: "         + (esActual ? WHITE : "#374151") + ";"
                + "-fx-background-radius: 6; -fx-padding: 6 11; -fx-cursor: hand;"
                + "-fx-font-size: 13px; -fx-border-color: " + BORDER + "; -fx-border-radius: 6;");
        b.setOnAction(e -> accion.run());
        return b;
    }

    // ═══════════════════════════════════════════════════════════════
    // FILA DE TABLA
    // ═══════════════════════════════════════════════════════════════
    private HBox buildFila(Barrio b, boolean par) {
        HBox fila = new HBox(0);
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.setPadding(new Insets(10, 16, 10, 16));

        String bgNormal = "-fx-background-color: " + (par ? WHITE : "#fafbfd") + ";"
                + "-fx-border-color: transparent transparent " + BORDER + " transparent;"
                + "-fx-border-width: 0 0 1 0;";
        fila.setStyle(bgNormal);
        fila.setOnMouseEntered(e -> fila.setStyle(
                "-fx-background-color: #EEF2FF;"
                + "-fx-border-color: transparent transparent #e5e7eb transparent;"
                + "-fx-border-width: 0 0 1 0; -fx-cursor: hand;"));
        fila.setOnMouseExited(e -> fila.setStyle(bgNormal));

        // ── Col 1: Nombre con avatar (crece) ─────────────────────────
        HBox celdaNombre = new HBox(10);
        celdaNombre.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(celdaNombre, Priority.ALWAYS);

        StackPane avatarBox = new StackPane();
        Circle avatar = new Circle(20, Color.web(colorAvatar(b.getNombre())));
        Label avatarLbl = label(iniciales(b.getNombre()), 12, WHITE, true);
        avatarBox.getChildren().addAll(avatar, avatarLbl);

        VBox nombreBox = new VBox(2);
        Label nombreLbl = new Label(b.getNombre() != null ? b.getNombre() : "—");
        nombreLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        Label subLbl = label("Barrio registrado", 11, GRAY_TEXT, false);
        nombreBox.getChildren().addAll(nombreLbl, subLbl);
        celdaNombre.getChildren().addAll(avatarBox, nombreBox);

        // ── Col 2: Badge de comuna (180px) ───────────────────────────
        HBox comunaBox = new HBox();
        comunaBox.setAlignment(Pos.CENTER_LEFT);
        comunaBox.setPrefWidth(180);
        comunaBox.setMinWidth(180);
        comunaBox.setMaxWidth(180);

        if (b.getComuna() != null && b.getComuna().getNombre() != null) {
            String[] badge = comunaBadge(b.getComuna().getId_comuna());
            Label cLbl = new Label(b.getComuna().getNombre());
            cLbl.setStyle(
                    "-fx-background-color: " + badge[0] + ";"
                    + "-fx-text-fill: " + badge[1] + ";"
                    + "-fx-font-size: 11px; -fx-font-weight: bold;"
                    + "-fx-background-radius: 20; -fx-padding: 4 10 4 10;");
            comunaBox.getChildren().add(cLbl);
        } else {
            Label sinC = new Label("Sin comuna");
            sinC.setStyle(
                    "-fx-background-color: " + RED_LIGHT + ";"
                    + "-fx-text-fill: " + RED + ";"
                    + "-fx-font-size: 11px; -fx-font-weight: bold;"
                    + "-fx-background-radius: 20; -fx-padding: 4 10 4 10;");
            comunaBox.getChildren().add(sinC);
        }

        // ── Col 3: Coordenadas (180px) ────────────────────────────────
        VBox coordBox = new VBox(2);
        coordBox.setAlignment(Pos.CENTER_LEFT);
        coordBox.setPrefWidth(180);
        coordBox.setMinWidth(180);
        coordBox.setMaxWidth(180);
        Label latLbl = label("Lat: " + b.getLatitudcentro(),  11, GRAY_TEXT, false);
        Label lngLbl = label("Lng: " + b.getLongitudcentro(), 11, GRAY_TEXT, false);
        coordBox.getChildren().addAll(latLbl, lngLbl);

        // ── Col 4: Acciones (160px) — botones FA ─────────────────────
        HBox acciones = new HBox(6);
        acciones.setAlignment(Pos.CENTER_LEFT);
        acciones.setPrefWidth(160);
        acciones.setMinWidth(160);
        acciones.setMaxWidth(160);
        acciones.getChildren().addAll(
                btnAccion("\uf06e", BLUE,   "#e8f0fe", "Ver",      () -> abrirDialogoVer(b)),
                btnAccion("\uf044", ORANGE, "#fff8e1", "Editar",   () -> abrirFormulario(b)),
                btnAccion("\uf2ed", RED,    RED_LIGHT, "Eliminar", () -> confirmarEliminar(b))
        );

        fila.getChildren().addAll(celdaNombre, comunaBox, coordBox, acciones);
        return fila;
    }

    // ═══════════════════════════════════════════════════════════════
    // DIÁLOGO VER
    // ═══════════════════════════════════════════════════════════════
    private void abrirDialogoVer(Barrio b) {
        Dialog<Void> dlg = new Dialog<>();
        dlg.setTitle("Detalle de Barrio");
        dlg.setHeaderText(null);

        VBox content = new VBox(14);
        content.setPadding(new Insets(20));
        content.setPrefWidth(380);

        Circle avatar = new Circle(35, Color.web(colorAvatar(b.getNombre())));
        Label avatarLbl = label(iniciales(b.getNombre()), 22, WHITE, true);
        StackPane avatarBox = new StackPane(avatar, avatarLbl);

        Label nombre = new Label(b.getNombre() != null ? b.getNombre() : "—");
        nombre.setFont(Font.font("System", FontWeight.BOLD, 18));
        nombre.setTextFill(Color.web("#111827"));

        VBox header = new VBox(6);
        header.setAlignment(Pos.CENTER);
        header.getChildren().addAll(avatarBox, nombre);

        String comunaNombre = b.getComuna() != null ? b.getComuna().getNombre() : "Sin comuna";
        content.getChildren().addAll(
                header,
                new Separator(),
                detalleRow("📍 Nombre",    b.getNombre() != null ? b.getNombre() : "—"),
                detalleRow("🏘 Comuna",    comunaNombre),
                detalleRow("🌐 Latitud",   String.valueOf(b.getLatitudcentro())),
                detalleRow("🌐 Longitud",  String.valueOf(b.getLongitudcentro()))
        );

        dlg.getDialogPane().setContent(content);
        dlg.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dlg.showAndWait();
    }

    // ═══════════════════════════════════════════════════════════════
    // FORMULARIO — insertar / editar
    // ═══════════════════════════════════════════════════════════════
    private void abrirFormulario(Barrio barrioExistente) {
        boolean esEdicion = barrioExistente != null;

        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle(esEdicion ? "Editar Barrio" : "Nuevo Barrio");
        dlg.setHeaderText(null);

        VBox form = new VBox(12);
        form.setPadding(new Insets(24));
        form.setPrefWidth(440);
        form.setStyle("-fx-background-color: white;");

        Label lblTitulo = new Label(esEdicion ? "Editar Barrio" : "Nuevo Barrio");
        lblTitulo.setFont(Font.font("System", FontWeight.BOLD, 18));
        lblTitulo.setTextFill(Color.web("#111827"));

        Label lblSub = label(
                esEdicion ? "Modifica los datos del barrio"
                          : "Registra un nuevo barrio en el sistema",
                13, GRAY_TEXT, false);

        // Nombre
        TextField txtNombre = dlgField("Ej: El Prado, Centro...",
                esEdicion && barrioExistente.getNombre() != null
                        ? barrioExistente.getNombre() : "");

        // Comuna
        List<Comuna> comunas = cargarComunas();
        ComboBox<String> cmbComuna = new ComboBox<>();
        cmbComuna.setPromptText("Seleccionar comuna *");
        cmbComuna.setPrefHeight(40);
        cmbComuna.setMaxWidth(Double.MAX_VALUE);
        cmbComuna.setStyle("-fx-background-radius: 8; -fx-font-size: 13px;");
        comunas.forEach(c -> cmbComuna.getItems().add(c.getNombre()));
        if (esEdicion && barrioExistente.getComuna() != null) {
            cmbComuna.setValue(barrioExistente.getComuna().getNombre());
        }

        // Coordenadas
        HBox coordRow = new HBox(10);
        TextField txtLat = dlgField("Latitud  Ej: 10.4631",
                esEdicion ? String.valueOf(barrioExistente.getLatitudcentro()) : "");
        TextField txtLng = dlgField("Longitud  Ej: -73.2532",
                esEdicion ? String.valueOf(barrioExistente.getLongitudcentro()) : "");
        HBox.setHgrow(txtLat, Priority.ALWAYS);
        HBox.setHgrow(txtLng, Priority.ALWAYS);
        coordRow.getChildren().addAll(txtLat, txtLng);

        Label lblError = label("", 12, RED, false);
        lblError.setWrapText(true);

        form.getChildren().addAll(
                lblTitulo, lblSub,
                new Separator(),
                label("Nombre del barrio *", 12, GRAY_TEXT, false), txtNombre,
                label("Comuna *",            12, GRAY_TEXT, false), cmbComuna,
                label("Coordenadas (opcional)", 12, GRAY_TEXT, false), coordRow,
                lblError
        );

        dlg.getDialogPane().setContent(form);
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button btnOk = (Button) dlg.getDialogPane().lookupButton(ButtonType.OK);
        btnOk.setText(esEdicion ? "Guardar cambios" : "Crear barrio");
        btnOk.setStyle("-fx-background-color: #1565c0; -fx-text-fill: white; "
                + "-fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 16;");

        btnOk.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            lblError.setText("");
            String nombre = txtNombre.getText().trim();
            String comunaNombre = cmbComuna.getValue();

            if (nombre.isEmpty()) {
                lblError.setText("El nombre del barrio no puede estar vacío.");
                ev.consume(); return;
            }
            if (comunaNombre == null || comunaNombre.isBlank()) {
                lblError.setText("Debe seleccionar una comuna.");
                ev.consume(); return;
            }

            Comuna comunaObj = comunas.stream()
                    .filter(c -> c.getNombre().equals(comunaNombre))
                    .findFirst().orElse(null);
            if (comunaObj == null) {
                lblError.setText("Comuna no encontrada.");
                ev.consume(); return;
            }

            double lat = 0.0, lng = 0.0;
            try {
                if (!txtLat.getText().trim().isEmpty())
                    lat = Double.parseDouble(txtLat.getText().trim());
                if (!txtLng.getText().trim().isEmpty())
                    lng = Double.parseDouble(txtLng.getText().trim());
            } catch (NumberFormatException ex) {
                lblError.setText("Latitud y longitud deben ser valores numéricos.");
                ev.consume(); return;
            }

            try {
                if (esEdicion) {
                    barrioExistente.setNombre(nombre);
                    barrioExistente.setComuna(comunaObj);
                    barrioExistente.setLatitudcentro(lat);
                    barrioExistente.setLongitudcentro(lng);
                    barrioService.actualizar(barrioExistente);
                } else {
                    Barrio nuevo = new Barrio();
                    nuevo.setNombre(nombre);
                    nuevo.setComuna(comunaObj);
                    nuevo.setLatitudcentro(lat);
                    nuevo.setLongitudcentro(lng);
                    barrioService.insertar(nuevo);
                }
                cargarYRenderizar();
            } catch (IllegalArgumentException ex) {
                lblError.setText("Error de validación: " + ex.getMessage());
                ev.consume();
            } catch (Exception ex) {
                lblError.setText("Error BD: " + ex.getMessage());
                ev.consume();
            }
        });

        dlg.showAndWait();
    }

    // ═══════════════════════════════════════════════════════════════
    // CONFIRMAR ELIMINAR
    // ═══════════════════════════════════════════════════════════════
    private void confirmarEliminar(Barrio b) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Eliminar barrio");
        confirm.setHeaderText("¿Eliminar \"" + b.getNombre() + "\"?");
        confirm.setContentText(
                "Esta acción no se puede deshacer.\n"
                + "Los datos asociados a este barrio podrían verse afectados.");
        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) {
                try {
                    barrioService.eliminar(b.getNombre());
                    cargarYRenderizar();
                } catch (Exception ex) {
                    mostrarAlerta("Error al eliminar", ex.getMessage());
                }
            }
        });
    }

    // ═══════════════════════════════════════════════════════════════
    // HELPERS DE DATOS
    // ═══════════════════════════════════════════════════════════════
    private List<Barrio> cargarBarrios() {
        if (barrioService == null) return List.of();
        try { return barrioService.listar(); }
        catch (Exception e) { return List.of(); }
    }

    private List<Comuna> cargarComunas() {
        if (comunaService == null) return List.of();
        try { return comunaService.listar(); }
        catch (Exception e) { return List.of(); }
    }

    // ═══════════════════════════════════════════════════════════════
    // HELPERS UI
    // ═══════════════════════════════════════════════════════════════
    private Button btnAccion(String iconFA, String iconColor,
                              String bgColor, String tooltip, Runnable accion) {
        Button b = new Button(iconFA);
        String base = "-fx-background-color: " + bgColor + ";"
                + "-fx-text-fill: " + iconColor + ";"
                + "-fx-font-family: 'Font Awesome 6 Free Solid';"
                + "-fx-font-size: 13px; -fx-background-radius: 8;"
                + "-fx-padding: 7 10; -fx-cursor: hand;";
        String hover = "-fx-background-color: " + iconColor + ";"
                + "-fx-text-fill: white;"
                + "-fx-font-family: 'Font Awesome 6 Free Solid';"
                + "-fx-font-size: 13px; -fx-background-radius: 8;"
                + "-fx-padding: 7 10; -fx-cursor: hand;";
        b.setStyle(base);
        b.setOnMouseEntered(e -> b.setStyle(hover));
        b.setOnMouseExited(e  -> b.setStyle(base));
        b.setOnAction(e -> accion.run());
        Tooltip.install(b, new Tooltip(tooltip));
        return b;
    }

    private HBox detalleRow(String campo, String valor) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        Label k = label(campo, 13, GRAY_TEXT, false);
        k.setMinWidth(130);
        row.getChildren().addAll(k, label(valor != null ? valor : "—", 13, "#111827", false));
        return row;
    }

    private TextField dlgField(String prompt, String val) {
        TextField f = new TextField(val);
        f.setPromptText(prompt);
        f.setPrefHeight(40);
        f.setMaxWidth(Double.MAX_VALUE);
        f.setStyle("-fx-background-color: #f5f7fb; -fx-background-radius: 8; "
                + "-fx-border-radius: 8; -fx-border-color: transparent; "
                + "-fx-padding: 0 14; -fx-font-size: 13px;");
        return f;
    }

    private Label label(String text, double size, String color, boolean bold) {
        Label lbl = new Label(text);
        lbl.setFont(bold
                ? Font.font("System", FontWeight.BOLD, size)
                : Font.font("System", size));
        lbl.setTextFill(Color.web(color));
        return lbl;
    }

    private void shadow(Region node) {
        node.setEffect(new DropShadow(12, 0, 2, Color.web("#0000001a")));
    }

    private void mostrarAlerta(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(titulo); a.setHeaderText(null); a.setContentText(msg);
        a.showAndWait();
    }

    // ── Colores de avatar ─────────────────────────────────────────
    private static final String[] AVATAR_COLORS = {
        "#1565c0", "#2e7d32", "#6a1b9a", "#c62828", "#e65100",
        "#00695c", "#283593", "#4e342e", "#37474f", "#558b2f"
    };

    private String colorAvatar(String nombre) {
        if (nombre == null || nombre.isBlank()) return AVATAR_COLORS[0];
        return AVATAR_COLORS[Math.abs(nombre.hashCode()) % AVATAR_COLORS.length];
    }

    private String iniciales(String nombre) {
        if (nombre == null || nombre.isBlank()) return "?";
        String[] p = nombre.trim().split("\\s+");
        if (p.length == 1) return p[0].substring(0, 1).toUpperCase();
        return (p[0].substring(0, 1) + p[1].substring(0, 1)).toUpperCase();
    }

    // ── Paleta de badges para comunas (cíclico por ID) ───────────
    private static final String[][] COMUNA_PALETA = {
        {"#e3f2fd", "#1565c0"},
        {"#fce4ec", "#c62828"},
        {"#e8f5e9", "#2e7d32"},
        {"#fff3e0", "#e65100"},
        {"#f3e5f5", "#6a1b9a"},
        {"#e0f7fa", "#00695c"},
    };

    private String[] comunaBadge(int idComuna) {
        return COMUNA_PALETA[Math.abs(idComuna) % COMUNA_PALETA.length];
    }

    // ── Estilos de botón primario ─────────────────────────────────
    private String btnPrimaryStyle() {
        return "-fx-background-color: #1565c0; -fx-text-fill: white;"
                + "-fx-font-size: 13px; -fx-font-weight: bold;"
                + "-fx-background-radius: 8; -fx-padding: 10 18; -fx-cursor: hand;";
    }

    private String btnPrimaryHoverStyle() {
        return "-fx-background-color: #0d47a1; -fx-text-fill: white;"
                + "-fx-font-size: 13px; -fx-font-weight: bold;"
                + "-fx-background-radius: 8; -fx-padding: 10 18; -fx-cursor: hand;";
    }
}