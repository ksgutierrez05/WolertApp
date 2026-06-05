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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import sistemagestion.model.*;
import sistemagestion.service.AlertaService;
import sistemagestion.service.BarrioService;
import sistemagestion.service.TipoAlertaService;

/**
 * Vista "Alertas" para el usuario ciudadano.
 *
 * GRASP — Controller:
 *   Recibe los eventos de la interfaz (búsqueda, filtros, paginación,
 *   botón ver detalle) y coordina la respuesta adecuada delegando la
 *   construcción de widgets en AlertaUIFactory y los datos en los servicios.
 *   No mezcla lógica de negocio con construcción de nodos.
 *
 * GRASP — Low Coupling:
 *   Depende de AlertaUIFactory y los servicios inyectados; no accede
 *   directamente a la BD ni contiene lógica de presentación de bajo nivel.
 *
 * SOLID — SRP (Single Responsibility Principle):
 *   Su única razón de cambio es el comportamiento de la pantalla de alertas
 *   (flujo de navegación, reglas de filtrado, paginación, reacción a eventos).
 *
 * SOLID — DIP (Dependency Inversion Principle):
 *   AlertaService, BarrioService y TipoAlertaService se inyectan por
 *   constructor; la vista no instancia servicios de acceso a datos.
 */
public class AlertasView {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy  HH:mm");
    private static final int FILAS_POR_PAGINA = 8;

    // ── Dependencias inyectadas ───────────────────────────────────
    private final AlertaService      alertaService;
    private final BarrioService      barrioService;
    private final TipoAlertaService  tipoAlertaService;
    private final Usuario            usuarioActual;
    private final AlertaUIFactory    ui;               // Creator delegado

    // ── Estado de la pantalla ─────────────────────────────────────
    private final ObservableList<Alerta> todasLasAlertas  = FXCollections.observableArrayList();
    private final ObservableList<Alerta> alertasFiltradas = FXCollections.observableArrayList();
    private int paginaActual = 1;

    // ── Controles ─────────────────────────────────────────────────
    private VBox      listaContainer;
    private Label     lblMostrando;
    private HBox      paginacionBox;
    private TextField campoBusqueda;
    private ComboBox<String> filtroEstado;
    private ComboBox<String> filtroTipo;
    private ComboBox<String> filtroBarrio;

    // ── Labels de métricas (se actualizan con los datos) ──────────
    private Label lblTotalVal;
    private Label lblActivasVal;
    private Label lblEnAtencionVal;
    private Label lblResueltasVal;

    // ── Constructor ───────────────────────────────────────────────
    /**
     * @param usuarioActual  Usuario logueado (puede ser null).
     * @param alertaService  Servicio de alertas inyectado (DIP).
     * @param barrioService  Servicio de barrios inyectado (DIP).
     */
    public AlertasView(Usuario usuarioActual,
                       AlertaService alertaService,
                       BarrioService barrioService) {
        Font.loadFont(getClass().getResourceAsStream("/fa-solid-900.ttf"), 20);
        this.usuarioActual = usuarioActual;
        this.alertaService = alertaService;
        this.barrioService = barrioService;
        this.ui            = new AlertaUIFactory();

        TipoAlertaService tmp = null;
        try {
            tmp = new TipoAlertaService();
        } catch (SQLException e) {
            System.out.println("TipoAlertaService no disponible: " + e.getMessage());
        }
        this.tipoAlertaService = tmp;
    }

    // ── Punto de entrada ──────────────────────────────────────────
    public ScrollPane getView() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color:" + AppColors.BG_USER + ";");

        content.getChildren().addAll(
                buildTopBar(),
                buildMetrics(),
                buildToolbar(),
                buildTabla(),
                buildPaginacion()
        );

        cargarAlertas();

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color:" + AppColors.BG_USER
                + "; -fx-background:" + AppColors.BG_USER + ";");
        return scroll;
    }

    // ── Barra superior ────────────────────────────────────────────
    private HBox buildTopBar() {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);

        VBox titles = new VBox(4);
        Label title = new Label("Alertas de la Comunidad");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.web(AppColors.TEXT_PRIMARY));

        String barrioUsuario = obtenerBarrioUsuario();
        String subTexto = barrioUsuario != null
                ? "Alertas activas en " + barrioUsuario + " y comunidad cercana"
                : "Alertas activas reportadas en la comunidad";
        titles.getChildren().addAll(title, ui.label(subTexto, 13, AppColors.GRAY_TEXT, false));

        Button btnRecargar = ui.btnSecundario("↻  Actualizar");
        btnRecargar.setOnAction(e -> cargarAlertas());

        HBox right = new HBox(12);
        right.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(right, Priority.ALWAYS);
        right.getChildren().add(btnRecargar);

        bar.getChildren().addAll(titles, right);
        return bar;
    }

    // ── Métricas ──────────────────────────────────────────────────
    private HBox buildMetrics() {
        lblTotalVal      = ui.boldNum("0", AppColors.RED);
        lblActivasVal    = ui.boldNum("0", AppColors.ORANGE);
        lblEnAtencionVal = ui.boldNum("0", AppColors.BLUE);
        lblResueltasVal  = ui.boldNum("0", AppColors.GREEN);

        HBox row = new HBox(16);
        HBox.setHgrow(row, Priority.ALWAYS);
        row.getChildren().addAll(
                ui.statCard("#fff0f0",  AppColors.RED,    "\uf0f3",
                        "Total alertas",    lblTotalVal,      "En la comunidad",     AppColors.RED),
                ui.statCard("#fff8e1",  AppColors.ORANGE, "\uf071",
                        "Activas / Nuevas", lblActivasVal,    "PENDIENTE · RECIBIDA", AppColors.ORANGE),
                ui.statCard("#eff6ff",  AppColors.BLUE,   "\uf505",
                        "En atención",      lblEnAtencionVal, "Policía asignado",    AppColors.BLUE),
                ui.statCard("#e8f5e9",  AppColors.GREEN,  "\uf058",
                        "Resueltas hoy",    lblResueltasVal,  "Casos cerrados",      AppColors.GREEN)
        );
        return row;
    }

    // ── Toolbar ───────────────────────────────────────────────────
    private HBox buildToolbar() {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(16, 20, 16, 20));
        bar.setStyle("-fx-background-color:white;-fx-background-radius:12;");
        ui.shadow(bar);

        // Campo de búsqueda
        HBox searchBox = new HBox(8);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setStyle("-fx-background-color:#f5f7fb;"
                + "-fx-background-radius:10;-fx-padding:0 14;");
        searchBox.setPrefHeight(42);
        HBox.setHgrow(searchBox, Priority.ALWAYS);

        Label searchIcon = new Label("\uf002");
        searchIcon.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';"
                + "-fx-font-size:14px;-fx-text-fill:#9ca3af;");

        campoBusqueda = new TextField();
        campoBusqueda.setPromptText("Buscar por descripción, barrio o tipo...");
        campoBusqueda.setStyle("-fx-background-color:transparent;-fx-border-color:transparent;"
                + "-fx-font-size:13px;-fx-text-fill:" + AppColors.TEXT_PRIMARY + ";");
        campoBusqueda.setPrefHeight(42);
        HBox.setHgrow(campoBusqueda, Priority.ALWAYS);
        campoBusqueda.textProperty().addListener((obs, o, n) -> filtrarYMostrar());
        searchBox.getChildren().addAll(searchIcon, campoBusqueda);

        // Filtros
        filtroEstado = ui.styledCombo();
        filtroEstado.getItems().add("Estado: Todos");
        for (EstadoAlerta e : EstadoAlerta.values()) filtroEstado.getItems().add(e.name());
        filtroEstado.setValue("Estado: Todos");
        filtroEstado.setOnAction(e -> filtrarYMostrar());

        filtroTipo = ui.styledCombo();
        filtroTipo.getItems().add("Tipo: Todos");
        filtroTipo.setValue("Tipo: Todos");
        filtroTipo.setOnAction(e -> filtrarYMostrar());
        cargarTiposEnFiltro();

        filtroBarrio = ui.styledCombo();
        filtroBarrio.getItems().add("Barrio: Todos");
        filtroBarrio.setValue("Barrio: Todos");
        filtroBarrio.setOnAction(e -> filtrarYMostrar());
        cargarBarriosEnFiltro();

        bar.getChildren().addAll(searchBox, filtroEstado, filtroTipo, filtroBarrio);
        return bar;
    }

    // ── Tabla ─────────────────────────────────────────────────────
    private VBox buildTabla() {
        VBox card = new VBox(0);
        card.setStyle("-fx-background-color:white;-fx-background-radius:12;");
        ui.shadow(card);

        HBox header = new HBox();
        header.setPadding(new Insets(13, 18, 13, 18));
        header.setStyle("-fx-background-color:#f8f9fc;-fx-background-radius:12 12 0 0;"
                + "-fx-border-color:transparent transparent " + AppColors.BORDER + " transparent;"
                + "-fx-border-width:0 0 1 0;");
        header.getChildren().addAll(
                ui.colH("Estado",       120),
                ui.colH("Tipo",         155),
                ui.colH("Descripción",  240),
                ui.colH("Reportado por",130),
                ui.colH("Barrio",       120),
                ui.colH("Fecha",        145),
                ui.colH("Alarma",       120),
                ui.colH("Acciones",      80)
        );
        card.getChildren().add(header);

        listaContainer = new VBox(0);
        card.getChildren().add(listaContainer);

        lblMostrando = ui.label("Mostrando 0 – 0 de 0 alertas", 12, AppColors.GRAY_TEXT, false);
        HBox footer = new HBox();
        footer.setPadding(new Insets(10, 18, 10, 18));
        footer.setStyle("-fx-background-color:#f8f9fc;-fx-background-radius:0 0 12 12;"
                + "-fx-border-color:" + AppColors.BORDER + " transparent transparent transparent;"
                + "-fx-border-width:1 0 0 0;");
        footer.getChildren().add(lblMostrando);
        card.getChildren().add(footer);

        return card;
    }

    private HBox buildPaginacion() {
        paginacionBox = new HBox(6);
        paginacionBox.setAlignment(Pos.CENTER_RIGHT);
        return paginacionBox;
    }

    // ── Carga de datos ────────────────────────────────────────────
    private void cargarAlertas() {
        if (alertaService == null) return;
        new Thread(() -> {
            try {
                List<Alerta> todas = alertaService.listar();
                javafx.application.Platform.runLater(() -> {
                    todasLasAlertas.setAll(todas);
                    filtrarYMostrar();
                    actualizarMetricas();
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() ->
                        mostrarAlerta("Error al cargar alertas", e.getMessage()));
            }
        }, "hilo-carga-alertas").start();
    }

    private void cargarTiposEnFiltro() {
        if (tipoAlertaService == null) return;
        try {
            tipoAlertaService.listar().forEach(t -> {
                if (t.getNombre() != null) filtroTipo.getItems().add(t.getNombre());
            });
        } catch (Exception e) {
            System.out.println("Error cargando tipos: " + e.getMessage());
        }
    }

    private void cargarBarriosEnFiltro() {
        if (barrioService == null) return;
        try {
            barrioService.listar().forEach(b -> {
                if (b.getNombre() != null) filtroBarrio.getItems().add(b.getNombre());
            });
        } catch (Exception e) {
            System.out.println("Error cargando barrios: " + e.getMessage());
        }
    }

    // ── Filtrado ──────────────────────────────────────────────────
    private void filtrarYMostrar() {
        String busq   = campoBusqueda  != null ? campoBusqueda.getText().toLowerCase().trim() : "";
        String estado = filtroEstado   != null ? filtroEstado.getValue()  : "Estado: Todos";
        String tipo   = filtroTipo     != null ? filtroTipo.getValue()    : "Tipo: Todos";
        String barrio = filtroBarrio   != null ? filtroBarrio.getValue()  : "Barrio: Todos";

        List<Alerta> filtradas = todasLasAlertas.stream()
                .filter(a -> matchBusqueda(a, busq))
                .filter(a -> matchEstado(a, estado))
                .filter(a -> matchTipo(a, tipo))
                .filter(a -> matchBarrio(a, barrio))
                .collect(Collectors.toList());

        alertasFiltradas.setAll(filtradas);
        paginaActual = 1;
        renderizarPagina();
    }

    /** Comprueba si la alerta coincide con el texto de búsqueda libre. */
    private boolean matchBusqueda(Alerta a, String busq) {
        if (busq.isEmpty()) return true;
        return (a.getDescripcion() != null && a.getDescripcion().toLowerCase().contains(busq))
            || (a.getBarrio()     != null && a.getBarrio().getNombre().toLowerCase().contains(busq))
            || (a.getTipoalerta() != null && a.getTipoalerta().getNombre().toLowerCase().contains(busq))
            || (a.getUsuario()    != null && nombreUsuario(a.getUsuario()).toLowerCase().contains(busq));
    }

    private boolean matchEstado(Alerta a, String estado) {
        return estado == null || estado.startsWith("Estado")
            || (a.getEstado() != null && a.getEstado().name().equalsIgnoreCase(estado));
    }

    private boolean matchTipo(Alerta a, String tipo) {
        return tipo == null || tipo.startsWith("Tipo")
            || (a.getTipoalerta() != null && a.getTipoalerta().getNombre().equalsIgnoreCase(tipo));
    }

    private boolean matchBarrio(Alerta a, String barrio) {
        return barrio == null || barrio.startsWith("Barrio")
            || (a.getBarrio() != null && a.getBarrio().getNombre().equalsIgnoreCase(barrio));
    }

    // ── Renderizado de página ─────────────────────────────────────
    private void renderizarPagina() {
        listaContainer.getChildren().clear();

        int total = alertasFiltradas.size();
        int desde = (paginaActual - 1) * FILAS_POR_PAGINA;
        int hasta = Math.min(desde + FILAS_POR_PAGINA, total);

        if (total == 0) {
            VBox vacio = new VBox(12);
            vacio.setAlignment(Pos.CENTER);
            vacio.setPadding(new Insets(48));
            Label icn = new Label("🔕");
            icn.setFont(Font.font(56));
            Label msg  = ui.label("No hay alertas activas en este momento.", 15, AppColors.GRAY_TEXT, false);
            Label hint = ui.label("¡Buenas noticias! Tu comunidad está tranquila.", 12, AppColors.GREEN, false);
            vacio.getChildren().addAll(icn, msg, hint);
            listaContainer.getChildren().add(vacio);
        } else {
            for (int i = desde; i < hasta; i++) {
                listaContainer.getChildren().add(buildFila(alertasFiltradas.get(i), i % 2 == 0));
            }
        }

        lblMostrando.setText("Mostrando " + (total == 0 ? 0 : desde + 1)
                + " – " + hasta + " de " + total + " alertas");
        actualizarPaginacion();
    }

    // ── Fila de tabla ─────────────────────────────────────────────
    private HBox buildFila(Alerta a, boolean par) {
        HBox fila = new HBox();
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.setPadding(new Insets(11, 18, 11, 18));

        String bgN = "-fx-background-color:" + (par ? AppColors.WHITE : "#fafbfd") + ";"
                + "-fx-border-color:transparent transparent " + AppColors.BORDER + " transparent;"
                + "-fx-border-width:0 0 1 0;";
        fila.setStyle(bgN);
        fila.setOnMouseEntered(e -> fila.setStyle(
                "-fx-background-color:#f0f4ff;"
                + "-fx-border-color:transparent transparent " + AppColors.BORDER + " transparent;"
                + "-fx-border-width:0 0 1 0;"));
        fila.setOnMouseExited(e -> fila.setStyle(bgN));

        // Estado
        Label estLbl = ui.badge(
                a.getEstado() != null ? a.getEstado().name() : "—",
                ui.estadoBg(a.getEstado()), ui.colorEstado(a.getEstado()));
        HBox estBox = ui.celdaBox(estLbl, 120);

        // Tipo
        String tipoNom = a.getTipoalerta() != null ? a.getTipoalerta().getNombre() : "—";
        Label tipoLbl = ui.badge(ui.emojiTipo(tipoNom) + "  " + tipoNom,
                ui.tipoBg(tipoNom), ui.tipoColor(tipoNom));
        tipoLbl.setPrefWidth(155);
        HBox tipoBox = ui.celdaBox(tipoLbl, 155);

        // Descripción
        String desc = a.getDescripcion() != null ? a.getDescripcion() : "—";
        String descCrt = desc.length() > 36 ? desc.substring(0, 36) + "…" : desc;
        Label descLbl = ui.celda(descCrt, 240);
        descLbl.setTooltip(new Tooltip(desc));

        // Reportado por
        Label reportadoLbl = ui.celda(
                a.getUsuario() != null ? nombreUsuario(a.getUsuario()) : "—", 130);

        // Barrio
        Label barrioLbl = ui.celda(
                a.getBarrio() != null ? a.getBarrio().getNombre() : "—", 120);

        // Fecha
        Label fechaLbl = ui.celda(
                a.getFechaHora() != null ? a.getFechaHora().format(FMT) : "—", 145);

        // Alarma asignada
        Label alarmaLbl = a.getAlarma() != null
                ? ui.badge("🚨  " + a.getAlarma().getNombre(), AppColors.RED_LIGHT, AppColors.RED)
                : ui.badge("—", AppColors.TIPO_DEFAULT_BG, AppColors.GRAY_TEXT);
        alarmaLbl.setPrefWidth(120);
        HBox alarmaBox = ui.celdaBox(alarmaLbl, 120);

        // Acciones
        HBox acciones = new HBox(6);
        acciones.setAlignment(Pos.CENTER_LEFT);
        acciones.setPrefWidth(80);
        acciones.getChildren().add(
                ui.btnIcono("\uf06e", "Ver detalle",
                        AppColors.BLUE, AppColors.BLUE_LIGHT, () -> abrirDetalle(a)));

        fila.getChildren().addAll(
                estBox, tipoBox, descLbl, reportadoLbl,
                barrioLbl, fechaLbl, alarmaBox, acciones);
        return fila;
    }

    // ── Diálogo de detalle ────────────────────────────────────────
    private void abrirDetalle(Alerta a) {
        Dialog<Void> dlg = new Dialog<>();
        dlg.setTitle("Detalle de alerta #" + a.getId_alerta());
        dlg.setHeaderText(null);

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.setPrefWidth(470);

        // Badge de estado centrado
        Label estBadge = ui.badge(
                a.getEstado() != null ? a.getEstado().name() : "—",
                ui.estadoBg(a.getEstado()), ui.colorEstado(a.getEstado()));
        estBadge.setFont(Font.font("System", FontWeight.BOLD, 14));
        estBadge.setPadding(new Insets(5, 14, 5, 14));
        HBox estRow = new HBox(estBadge);
        estRow.setAlignment(Pos.CENTER);
        content.getChildren().addAll(estRow, new Separator());

        // Información de la alerta
        content.getChildren().add(ui.seccionHeader("📋 INFORMACIÓN DE LA ALERTA"));
        content.getChildren().addAll(
                ui.detRow("🆔 ID",          "#" + a.getId_alerta(), 170),
                ui.detRow("🔔 Tipo",        tipoNvl(a.getTipoalerta()), 170),
                ui.detRow("📋 Descripción", nvl(a.getDescripcion()), 170),
                ui.detRow("📅 Fecha / Hora",
                        a.getFechaHora() != null ? a.getFechaHora().format(FMT) : "—", 170)
        );
        content.getChildren().add(new Separator());

        // Ubicación
        content.getChildren().add(ui.seccionHeader("📍 UBICACIÓN"));
        content.getChildren().addAll(
                ui.detRow("🏘 Barrio", a.getBarrio() != null ? a.getBarrio().getNombre() : "—", 170),
                ui.detRow("🏙 Comuna",
                        a.getBarrio() != null && a.getBarrio().getComuna() != null
                                ? a.getBarrio().getComuna().getNombre() : "—", 170)
        );
        if (a.getDireccion() != null) {
            Direccion d = a.getDireccion();
            content.getChildren().addAll(
                    ui.detRow("🏠 Calle",       nvl(d.getCalle()),      170),
                    ui.detRow("📌 Carrera",      nvl(d.getCarrera()),    170),
                    ui.detRow("📍 Referencia",   nvl(d.getReferencia()), 170),
                    ui.detRow("🏗 Etapa",        nvl(d.getEtapa()),      170),
                    ui.detRow("🏡 Manzana",      nvl(d.getManzana()),    170),
                    ui.detRow("🚪 Casa",         nvl(d.getCasa()),       170)
            );
        }
        content.getChildren().add(new Separator());

        // Detalles del incidente
        content.getChildren().add(ui.seccionHeader("⚠ DETALLES DEL INCIDENTE"));
        content.getChildren().addAll(
                ui.detRow("🔫 Tipo de arma",
                        a.getTipoarma() != null ? a.getTipoarma().getNombre() : "Sin arma reportada", 170),
                ui.detRow("🚗 Medio de transporte",
                        a.getMediotransporte() != null ? a.getMediotransporte().getNombre()
                                : "Sin medio registrado", 170)
        );

        // Alarma asignada (opcional)
        if (a.getAlarma() != null) {
            content.getChildren().add(new Separator());
            content.getChildren().add(ui.seccionHeader("🚨 ALARMA ASIGNADA"));
            content.getChildren().addAll(
                    ui.detRow("📛 Nombre alarma", nvl(a.getAlarma().getNombre()), 170),
                    ui.detRow("⚡ Estado alarma",
                            a.getAlarma().getEstado() != null ? a.getAlarma().getEstado().name() : "—", 170)
            );
        }
        content.getChildren().add(new Separator());

        // Reportado por
        content.getChildren().add(ui.seccionHeader("👤 REPORTADO POR"));
        if (a.getUsuario() != null) {
            Usuario u = a.getUsuario();
            content.getChildren().addAll(
                    ui.detRow("👤 Nombre",   nvl(u.getPrimer_nombre()), 170),
                    ui.detRow("📞 Teléfono", nvl(u.getTelefono()),      170)
            );
        } else {
            content.getChildren().add(ui.label("Información no disponible", 12, AppColors.GRAY_TEXT, false));
        }

        ScrollPane scrollContent = new ScrollPane(content);
        scrollContent.setFitToWidth(true);
        scrollContent.setPrefHeight(500);
        scrollContent.setStyle("-fx-background:white;-fx-background-color:white;");

        dlg.getDialogPane().setContent(scrollContent);
        dlg.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dlg.getDialogPane().setPrefWidth(500);
        dlg.showAndWait();
    }

    // ── Métricas ──────────────────────────────────────────────────
    private void actualizarMetricas() {
        long total = todasLasAlertas.size();
        long activas = contarEstados(EstadoAlerta.PENDIENTE, EstadoAlerta.RECIBIDA);
        long enAtencion = contarEstados(EstadoAlerta.EN_ATENCION, EstadoAlerta.UNIDAD_ASIGNADA);
        long resueltasHoy = todasLasAlertas.stream()
                .filter(a -> a.getEstado() == EstadoAlerta.RESUELTA
                        && a.getFechaHora() != null
                        && a.getFechaHora().toLocalDate().equals(java.time.LocalDate.now()))
                .count();

        actualizarBoldNum(lblTotalVal,      total,       AppColors.RED);
        actualizarBoldNum(lblActivasVal,    activas,     AppColors.ORANGE);
        actualizarBoldNum(lblEnAtencionVal, enAtencion,  AppColors.BLUE);
        actualizarBoldNum(lblResueltasVal,  resueltasHoy,AppColors.GREEN);
    }

    /** Cuenta alertas que coincidan con cualquiera de los estados dados. */
    private long contarEstados(EstadoAlerta... estados) {
        return todasLasAlertas.stream()
                .filter(a -> {
                    for (EstadoAlerta e : estados) {
                        if (a.getEstado() == e) return true;
                    }
                    return false;
                }).count();
    }

    /** Actualiza texto y color de un label de métrica. */
    private void actualizarBoldNum(Label lbl, long valor, String color) {
        lbl.setText(String.valueOf(valor));
        lbl.setStyle("-fx-font-size:36px;-fx-font-weight:bold;-fx-text-fill:" + color + ";");
    }

    // ── Paginación ────────────────────────────────────────────────
    private void actualizarPaginacion() {
        if (paginacionBox == null) return;
        paginacionBox.getChildren().clear();

        int total     = alertasFiltradas.size();
        int totalPags = (int) Math.ceil((double) total / FILAS_POR_PAGINA);
        if (totalPags <= 1) return;

        paginacionBox.getChildren().add(
                btnPag("‹", paginaActual > 1, () -> { paginaActual--; renderizarPagina(); }));

        int ini = Math.max(1, paginaActual - 2);
        int fin = Math.min(totalPags, paginaActual + 2);

        if (ini > 1) {
            paginacionBox.getChildren().addAll(
                    btnPag("1", true, () -> { paginaActual = 1; renderizarPagina(); }),
                    ui.label("…", 13, AppColors.GRAY_TEXT, false));
        }

        for (int i = ini; i <= fin; i++) {
            final int pg = i;
            paginacionBox.getChildren().add(
                    btnPag(String.valueOf(i), true, () -> { paginaActual = pg; renderizarPagina(); }));
        }

        if (fin < totalPags) {
            paginacionBox.getChildren().addAll(
                    ui.label("…", 13, AppColors.GRAY_TEXT, false),
                    btnPag(String.valueOf(totalPags), true,
                            () -> { paginaActual = totalPags; renderizarPagina(); }));
        }

        paginacionBox.getChildren().add(
                btnPag("›", paginaActual < totalPags, () -> { paginaActual++; renderizarPagina(); }));
    }

    private Button btnPag(String txt, boolean enabled, Runnable accion) {
        Button b = new Button(txt);
        b.setDisable(!enabled);
        boolean esActual = txt.equals(String.valueOf(paginaActual));
        b.setStyle("-fx-background-color:" + (esActual ? AppColors.BLUE : AppColors.WHITE) + ";"
                + "-fx-text-fill:" + (esActual ? AppColors.WHITE : AppColors.TEXT_SECONDARY) + ";"
                + "-fx-background-radius:6;-fx-padding:6 11;-fx-cursor:hand;"
                + "-fx-font-size:13px;-fx-border-color:" + AppColors.BORDER + ";-fx-border-radius:6;");
        b.setOnAction(e -> accion.run());
        return b;
    }

    // ── Utilidades de texto ───────────────────────────────────────
    private String nvl(String val)          { return val != null ? val : "—"; }
    private String tipoNvl(TipoAlerta t)    { return t != null && t.getNombre() != null ? t.getNombre() : "—"; }

    private String nombreUsuario(Usuario u) {
        if (u == null) return "—";
        String n = u.getPrimer_nombre() != null ? u.getPrimer_nombre() : "";
        return n.isBlank() ? nvl(u.getUsername()) : n;
    }

    private String obtenerBarrioUsuario() {
        if (usuarioActual == null) return null;
        if (usuarioActual.getDireccion() == null) return null;
        if (usuarioActual.getDireccion().getBarrio() == null) return null;
        return usuarioActual.getDireccion().getBarrio().getNombre();
    }

    private void mostrarAlerta(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}