/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.view;

/**
 *
 * @author Maria Cristina
 */


import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import sistemagestion.model.*;
import sistemagestion.service.AlertaService;
import sistemagestion.service.BarrioService;

public class MisAlertasView {

    // ── Colores (mismos que AlertasView) ──────────────────────────
    private static final String WHITE      = "#ffffff";
    private static final String BG        = "#f4f6fb";
    private static final String RED       = "#e53935";
    private static final String RED_LIGHT = "#fff0f0";
    private static final String ORANGE    = "#fb8c00";
    private static final String GREEN     = "#43a047";
    private static final String BLUE      = "#1565c0";
    private static final String PURPLE    = "#7b1fa2";
    private static final String GRAY_TEXT = "#6b7280";
    private static final String BORDER    = "#e5e7eb";

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy  HH:mm");

    // ── Dependencias ──────────────────────────────────────────────
    private final Usuario       usuarioActual;
    private final AlertaService alertaService;

    // ── Estado ────────────────────────────────────────────────────
    private final ObservableList<Alerta> misAlertas       = FXCollections.observableArrayList();
    private final ObservableList<Alerta> alertasFiltradas = FXCollections.observableArrayList();

    private static final int FILAS_POR_PAGINA = 8;
    private int paginaActual = 1;

    // ── Controles ─────────────────────────────────────────────────
    private VBox        listaContainer;
    private Label       lblMostrando;
    private HBox        paginacionBox;
    private TextField   campoBusqueda;
    private ComboBox<String> filtroEstado;
    private ComboBox<String> filtroTipo;

    // ── Métricas ──────────────────────────────────────────────────
    private Label lblTotalVal;
    private Label lblPendientesVal;
    private Label lblEnAtencionVal;
    private Label lblResueltasVal;

    // ─────────────────────────────────────────────────────────────
    public MisAlertasView(Usuario usuarioActual,
                          AlertaService alertaService,
                          BarrioService barrioService,
                          BorderPane rootPane) {
        Font.loadFont(getClass().getResourceAsStream("/fa-solid-900.ttf"), 20);
        this.usuarioActual = usuarioActual;
        this.alertaService = alertaService;
    }

    // ── Punto de entrada ─────────────────────────────────────────
    public ScrollPane getView() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color:" + BG + ";");

        content.getChildren().addAll(
                buildTopBar(),
                buildMetrics(),
                buildToolbar(),
                buildTabla(),
                buildPaginacion()
        );

        cargarMisAlertas();

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color:" + BG + "; -fx-background:" + BG + ";");
        return scroll;
    }

    // ═══════════════════════════════════════════════════════════════
    // TOP BAR
    // ═══════════════════════════════════════════════════════════════
    private HBox buildTopBar() {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);

        VBox titles = new VBox(4);
        Label title = new Label("Mis Alertas");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#111827"));

        String nombre = usuarioActual != null
                ? usuarioActual.getPrimer_nombre() + " " + usuarioActual.getPrimer_apellido()
                : "Usuario";
        Label sub = label("Historial de reportes de " + nombre, 13, GRAY_TEXT, false);
        titles.getChildren().addAll(title, sub);

        bar.getChildren().add(titles);
        return bar;
    }

    // ═══════════════════════════════════════════════════════════════
    // MÉTRICAS
    // ═══════════════════════════════════════════════════════════════
    private HBox buildMetrics() {
        HBox row = new HBox(16);
        HBox.setHgrow(row, Priority.ALWAYS);

        lblTotalVal      = boldNum("0", RED);
        lblPendientesVal = boldNum("0", ORANGE);
        lblEnAtencionVal = boldNum("0", BLUE);
        lblResueltasVal  = boldNum("0", GREEN);

        row.getChildren().addAll(
                statCard(RED_LIGHT,   RED,    "\uf46d", "Total reportadas",  lblTotalVal,      "Mis alertas",      RED),
                statCard("#fff8e1",   ORANGE, "\uf252", "Pendientes",        lblPendientesVal, "Sin atender",      ORANGE),
                statCard("#eff6ff",   BLUE,   "\uf111", "En atención",       lblEnAtencionVal, "En proceso",       BLUE),
                statCard("#e8f5e9",   GREEN,  "\uf058", "Resueltas",         lblResueltasVal,  "Casos cerrados",   GREEN)
        );
        return row;
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

        // Campo de búsqueda
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
                + "-fx-font-size: 14px;"
                + "-fx-text-fill: #9ca3af;");

        campoBusqueda = new TextField();
        campoBusqueda.setPromptText("Buscar por descripción, barrio...");
        campoBusqueda.setStyle(
                "-fx-background-color: transparent;"
                + "-fx-border-color: transparent;"
                + "-fx-font-size: 13px;"
                + "-fx-text-fill: #111827;");
        campoBusqueda.setPrefHeight(42);
        HBox.setHgrow(campoBusqueda, Priority.ALWAYS);
        campoBusqueda.textProperty().addListener((obs, o, n) -> filtrarYMostrar());
        searchBox.getChildren().addAll(searchIcon, campoBusqueda);

        // Filtros
        filtroEstado = styledCombo();
        filtroEstado.getItems().add("Estado: Todos");
        for (EstadoAlerta e : EstadoAlerta.values()) {
            filtroEstado.getItems().add(e.name());
        }
        filtroEstado.setValue("Estado: Todos");
        filtroEstado.setOnAction(e -> filtrarYMostrar());

        filtroTipo = styledCombo();
        filtroTipo.getItems().add("Tipo: Todos");
        filtroTipo.setValue("Tipo: Todos");
        filtroTipo.setOnAction(e -> filtrarYMostrar());

        bar.getChildren().addAll(searchBox, filtroEstado, filtroTipo);
        return bar;
    }

    private ComboBox<String> styledCombo() {
        ComboBox<String> combo = new ComboBox<>();
        combo.setPrefHeight(42);
        combo.setPrefWidth(160);
        combo.setStyle(
                "-fx-background-color: #f5f7fb;"
                + "-fx-background-radius: 10;"
                + "-fx-border-color: transparent;"
                + "-fx-border-radius: 10;"
                + "-fx-font-size: 13px;"
                + "-fx-text-fill: #374151;"
                + "-fx-cursor: hand;");
        return combo;
    }

    // ═══════════════════════════════════════════════════════════════
    // TABLA
    // ═══════════════════════════════════════════════════════════════
    private VBox buildTabla() {
        VBox card = new VBox(0);
        card.setStyle("-fx-background-color:" + WHITE + "; -fx-background-radius:12;");
        shadow(card);

        // Cabecera
        HBox header = new HBox();
        header.setPadding(new Insets(13, 18, 13, 18));
        header.setStyle("-fx-background-color:#f8f9fc; -fx-background-radius:12 12 0 0;"
                + "-fx-border-color:transparent transparent " + BORDER + " transparent;"
                + "-fx-border-width:0 0 1 0;");
        header.getChildren().addAll(
                colH("Estado",      120),
                colH("Tipo",        155),
                colH("Descripción", 240),
                colH("Barrio",      120),
                colH("Fecha",       145),
                colH("Acciones",     80)
        );
        card.getChildren().add(header);

        listaContainer = new VBox(0);
        card.getChildren().add(listaContainer);

        lblMostrando = label("Mostrando 0 – 0 de 0 alertas", 12, GRAY_TEXT, false);
        HBox footer = new HBox();
        footer.setPadding(new Insets(10, 18, 10, 18));
        footer.setStyle("-fx-background-color:#f8f9fc; -fx-background-radius:0 0 12 12;"
                + "-fx-border-color:" + BORDER + " transparent transparent transparent;"
                + "-fx-border-width:1 0 0 0;");
        footer.getChildren().add(lblMostrando);
        card.getChildren().add(footer);

        return card;
    }

    private Label colH(String text, double width) {
        Label l = label(text, 11, GRAY_TEXT, true);
        l.setPrefWidth(width);
        return l;
    }

    private HBox buildPaginacion() {
        paginacionBox = new HBox(6);
        paginacionBox.setAlignment(Pos.CENTER_RIGHT);
        return paginacionBox;
    }

    // ═══════════════════════════════════════════════════════════════
    // CARGA DE DATOS
    // ═══════════════════════════════════════════════════════════════
    private void cargarMisAlertas() {
        if (alertaService == null || usuarioActual == null) return;
        try {
            String usernameActual = usuarioActual.getUsername();
            List<Alerta> mias = alertaService.listar().stream()
                    .filter(a -> a.getUsuario() != null
                            && usernameActual != null
                            && usernameActual.equalsIgnoreCase(a.getUsuario().getUsername()))
                    .collect(Collectors.toList());

            misAlertas.setAll(mias);

            // Tipos dinámicos
            filtroTipo.getItems().clear();
            filtroTipo.getItems().add("Tipo: Todos");
            mias.stream()
                    .filter(a -> a.getTipoalerta() != null && a.getTipoalerta().getNombre() != null)
                    .map(a -> a.getTipoalerta().getNombre())
                    .distinct().sorted()
                    .forEach(t -> filtroTipo.getItems().add(t));
            filtroTipo.setValue("Tipo: Todos");

            filtrarYMostrar();
            actualizarMetricas();
        } catch (Exception e) {
            mostrarAlerta("Error al cargar alertas", e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // FILTRADO
    // ═══════════════════════════════════════════════════════════════
    private void filtrarYMostrar() {
        String busq   = campoBusqueda != null ? campoBusqueda.getText().toLowerCase().trim() : "";
        String estado = filtroEstado  != null ? filtroEstado.getValue() : "Estado: Todos";
        String tipo   = filtroTipo    != null ? filtroTipo.getValue()   : "Tipo: Todos";

        List<Alerta> filtradas = misAlertas.stream().filter(a -> {
            boolean matchB = busq.isEmpty()
                    || (a.getDescripcion() != null && a.getDescripcion().toLowerCase().contains(busq))
                    || (a.getBarrio() != null && a.getBarrio().getNombre().toLowerCase().contains(busq));
            boolean matchE = estado == null || estado.startsWith("Estado")
                    || (a.getEstado() != null && a.getEstado().name().equalsIgnoreCase(estado));
            boolean matchT = tipo == null || tipo.startsWith("Tipo")
                    || (a.getTipoalerta() != null && a.getTipoalerta().getNombre().equalsIgnoreCase(tipo));
            return matchB && matchE && matchT;
        }).collect(Collectors.toList());

        alertasFiltradas.setAll(filtradas);
        paginaActual = 1;
        renderizarPagina();
    }

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
            Label msg  = label("No tienes alertas que coincidan con los filtros.", 15, GRAY_TEXT, false);
            Label hint = label("Usa el botón ⚠ PÁNICO en el Dashboard para reportar una emergencia.", 12, GREEN, false);
            hint.setWrapText(true);
            hint.setMaxWidth(400);
            hint.setAlignment(Pos.CENTER);
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

    // ═══════════════════════════════════════════════════════════════
    // FILA
    // ═══════════════════════════════════════════════════════════════
    private HBox buildFila(Alerta a, boolean par) {
        HBox fila = new HBox(0);
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.setPadding(new Insets(11, 18, 11, 18));

        String bgN = "-fx-background-color:" + (par ? WHITE : "#fafbfd") + ";"
                + "-fx-border-color:transparent transparent " + BORDER + " transparent;"
                + "-fx-border-width:0 0 1 0;";
        fila.setStyle(bgN);
        fila.setOnMouseEntered(e -> fila.setStyle(
                "-fx-background-color:#f0f4ff;"
                + "-fx-border-color:transparent transparent " + BORDER + " transparent;"
                + "-fx-border-width:0 0 1 0;"));
        fila.setOnMouseExited(e -> fila.setStyle(bgN));

        // ── Estado ───────────────────────────────────────────────
        String estLabel = (a.getEstado() != null ? a.getEstado().name() : "—").replace("_", " ");
        Label estLbl = badge("● " + estLabel, estadoBg(a.getEstado()), colorEstado(a.getEstado()));
        HBox estBox = celdaBox(estLbl, 120);

        // ── Tipo ─────────────────────────────────────────────────
        String tipoNom = a.getTipoalerta() != null ? a.getTipoalerta().getNombre() : "—";
        Label tipoLbl = badge(emojiTipo(tipoNom) + "  " + tipoNom, tipoBg(tipoNom), tipoColor(tipoNom));
        tipoLbl.setPrefWidth(155);
        HBox tipoBox = celdaBox(tipoLbl, 155);

        // ── Descripción ──────────────────────────────────────────
        String desc = a.getDescripcion() != null ? a.getDescripcion() : "—";
        String descCrt = desc.length() > 36 ? desc.substring(0, 36) + "…" : desc;
        Label descLbl = celda(descCrt, 240);
        descLbl.setTooltip(new Tooltip(desc));

        // ── Barrio ───────────────────────────────────────────────
        String barrioNom = a.getBarrio() != null ? a.getBarrio().getNombre() : "—";
        Label barrioLbl = celda(barrioNom, 120);

        // ── Fecha ────────────────────────────────────────────────
        String fechaStr = a.getFechaHora() != null ? a.getFechaHora().format(FMT) : "—";
        Label fechaLbl = celda(fechaStr, 145);

        // ── Acciones ─────────────────────────────────────────────
        HBox acciones = new HBox(6);
        acciones.setAlignment(Pos.CENTER);
        acciones.setPrefWidth(80);

        acciones.getChildren().add(
                btnIcono("\uf06e", "Ver detalle", "#1565c0", "#e8f0fe", () -> abrirDetalle(a)));

        boolean puedeCancelar = a.getEstado() == EstadoAlerta.PENDIENTE
                || a.getEstado() == EstadoAlerta.RECIBIDA;
        Button btnCancelar = btnIcono("\uf00d", "Cancelar alerta", RED, RED_LIGHT,
                () -> cancelarAlerta(a));
        btnCancelar.setDisable(!puedeCancelar);
        btnCancelar.setOpacity(puedeCancelar ? 1.0 : 0.3);
        acciones.getChildren().add(btnCancelar);

        fila.getChildren().addAll(estBox, tipoBox, descLbl, barrioLbl, fechaLbl, acciones);
        return fila;
    }

    // ═══════════════════════════════════════════════════════════════
    // DETALLE
    // ═══════════════════════════════════════════════════════════════
    private void abrirDetalle(Alerta a) {
        Dialog<Void> dlg = new Dialog<>();
        dlg.setTitle("Detalle de alerta #" + a.getId_alerta());
        dlg.setHeaderText(null);

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.setPrefWidth(470);

        // Badge estado centrado
        Label estBadge = badge(
                a.getEstado() != null ? a.getEstado().name().replace("_", " ") : "—",
                estadoBg(a.getEstado()), colorEstado(a.getEstado()));
        estBadge.setFont(Font.font("System", FontWeight.BOLD, 14));
        estBadge.setPadding(new Insets(5, 14, 5, 14));
        HBox estRow = new HBox(estBadge);
        estRow.setAlignment(Pos.CENTER);
        content.getChildren().addAll(estRow, new Separator());

        // Información de la alerta
        content.getChildren().add(seccionHeader("📋 INFORMACIÓN DE LA ALERTA"));
        content.getChildren().addAll(
                detRow("🆔 ID",            "#" + a.getId_alerta()),
                detRow("🔔 Tipo",          a.getTipoalerta() != null ? a.getTipoalerta().getNombre() : "—"),
                detRow("📋 Descripción",   nvl(a.getDescripcion())),
                detRow("📅 Fecha / Hora",  a.getFechaHora() != null ? a.getFechaHora().format(FMT) : "—")
        );

        content.getChildren().add(new Separator());

        // Ubicación
        content.getChildren().add(seccionHeader("📍 UBICACIÓN"));
        content.getChildren().addAll(
                detRow("🏘 Barrio", a.getBarrio() != null ? a.getBarrio().getNombre() : "—")
        );
        if (a.getDireccion() != null) {
            Direccion d = a.getDireccion();
            content.getChildren().addAll(
                    detRow("🏠 Calle",       nvl(d.getCalle())),
                    detRow("📌 Carrera",     nvl(d.getCarrera())),
                    detRow("📍 Referencia",  nvl(d.getReferencia()))
            );
        }

        content.getChildren().add(new Separator());

        // Detalles del incidente
        content.getChildren().add(seccionHeader("⚠ DETALLES DEL INCIDENTE"));
        content.getChildren().addAll(
                detRow("🔫 Tipo de arma",
                        a.getTipoarma() != null ? a.getTipoarma().getNombre() : "Sin arma reportada"),
                detRow("🚗 Medio de transporte",
                        a.getMediotransporte() != null ? a.getMediotransporte().getNombre() : "Sin medio registrado")
        );

        // Botón cancelar si aplica
        if (a.getEstado() == EstadoAlerta.PENDIENTE || a.getEstado() == EstadoAlerta.RECIBIDA) {
            content.getChildren().add(new Separator());
            Button btnCancelDetalle = new Button("Cancelar esta alerta");
            btnCancelDetalle.setStyle(
                    "-fx-background-color:" + RED_LIGHT + "; -fx-text-fill:" + RED + ";"
                    + "-fx-font-size:13px; -fx-font-weight:bold; -fx-background-radius:8;"
                    + "-fx-border-color:" + RED + "; -fx-border-radius:8;"
                    + "-fx-padding:9 20; -fx-cursor:hand;");
            HBox btnRow = new HBox(btnCancelDetalle);
            btnRow.setPadding(new Insets(4, 0, 0, 0));
            btnCancelDetalle.setOnAction(e -> { dlg.close(); cancelarAlerta(a); });
            content.getChildren().add(btnRow);
        }

        ScrollPane scrollContent = new ScrollPane(content);
        scrollContent.setFitToWidth(true);
        scrollContent.setPrefHeight(460);
        scrollContent.setStyle("-fx-background:white; -fx-background-color:white;");

        dlg.getDialogPane().setContent(scrollContent);
        dlg.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dlg.getDialogPane().setPrefWidth(500);
        dlg.showAndWait();
    }

    // ═══════════════════════════════════════════════════════════════
    // CANCELAR
    // ═══════════════════════════════════════════════════════════════
    private void cancelarAlerta(Alerta a) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancelar alerta");
        confirm.setHeaderText("¿Cancelar la alerta #" + a.getId_alerta() + "?");
        confirm.setContentText("La alerta pasará al estado CANCELADA. Esta acción no se puede deshacer.");

        Button btnSi = (Button) confirm.getDialogPane().lookupButton(ButtonType.OK);
        btnSi.setText("Sí, cancelar");
        btnSi.setStyle("-fx-background-color:" + RED + "; -fx-text-fill:white;"
                + "-fx-font-weight:bold; -fx-background-radius:8; -fx-padding:8 16;");

        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) {
                try {
                    boolean ok = alertaService.actualizarEstado(
                            a.getId_alerta(), EstadoAlerta.CANCELADA.name());
                    if (ok) {
                        mostrarInfo("Alerta #" + a.getId_alerta() + " cancelada.");
                        cargarMisAlertas();
                    } else {
                        mostrarAlerta("Error", "No se pudo cancelar la alerta.");
                    }
                } catch (Exception ex) {
                    mostrarAlerta("Error al cancelar", ex.getMessage());
                }
            }
        });
    }

    // ═══════════════════════════════════════════════════════════════
    // MÉTRICAS
    // ═══════════════════════════════════════════════════════════════
    private void actualizarMetricas() {
        lblTotalVal.setText(String.valueOf(misAlertas.size()));
        lblTotalVal.setStyle("-fx-font-size:36px;-fx-font-weight:bold;-fx-text-fill:" + RED + ";");

        long pendientes = misAlertas.stream()
                .filter(a -> a.getEstado() == EstadoAlerta.PENDIENTE
                          || a.getEstado() == EstadoAlerta.RECIBIDA).count();
        lblPendientesVal.setText(String.valueOf(pendientes));
        lblPendientesVal.setStyle("-fx-font-size:36px;-fx-font-weight:bold;-fx-text-fill:" + ORANGE + ";");

        long enAtencion = misAlertas.stream()
                .filter(a -> a.getEstado() == EstadoAlerta.EN_ATENCION
                          || a.getEstado() == EstadoAlerta.UNIDAD_ASIGNADA).count();
        lblEnAtencionVal.setText(String.valueOf(enAtencion));
        lblEnAtencionVal.setStyle("-fx-font-size:36px;-fx-font-weight:bold;-fx-text-fill:" + BLUE + ";");

        long resueltas = misAlertas.stream()
                .filter(a -> a.getEstado() == EstadoAlerta.RESUELTA).count();
        lblResueltasVal.setText(String.valueOf(resueltas));
        lblResueltasVal.setStyle("-fx-font-size:36px;-fx-font-weight:bold;-fx-text-fill:" + GREEN + ";");
    }

    // ═══════════════════════════════════════════════════════════════
    // PAGINACIÓN
    // ═══════════════════════════════════════════════════════════════
    private void actualizarPaginacion() {
        if (paginacionBox == null) return;
        paginacionBox.getChildren().clear();

        int total     = alertasFiltradas.size();
        int totalPags = (int) Math.ceil((double) total / FILAS_POR_PAGINA);
        if (totalPags <= 1) return;

        paginacionBox.getChildren().add(btnPag("‹", paginaActual > 1,
                () -> { paginaActual--; renderizarPagina(); }));

        int ini = Math.max(1, paginaActual - 2);
        int fin = Math.min(totalPags, paginaActual + 2);

        if (ini > 1) {
            paginacionBox.getChildren().addAll(
                    btnPag("1", true, () -> { paginaActual = 1; renderizarPagina(); }),
                    label("…", 13, GRAY_TEXT, false));
        }
        for (int i = ini; i <= fin; i++) {
            final int pg = i;
            paginacionBox.getChildren().add(btnPag(String.valueOf(i), true,
                    () -> { paginaActual = pg; renderizarPagina(); }));
        }
        if (fin < totalPags) {
            paginacionBox.getChildren().addAll(
                    label("…", 13, GRAY_TEXT, false),
                    btnPag(String.valueOf(totalPags), true,
                            () -> { paginaActual = totalPags; renderizarPagina(); }));
        }
        paginacionBox.getChildren().add(btnPag("›", paginaActual < totalPags,
                () -> { paginaActual++; renderizarPagina(); }));
    }

    private Button btnPag(String txt, boolean enabled, Runnable accion) {
        Button b = new Button(txt);
        b.setDisable(!enabled);
        boolean esActual = txt.equals(String.valueOf(paginaActual));
        b.setStyle("-fx-background-color:" + (esActual ? BLUE : WHITE) + ";"
                + "-fx-text-fill:" + (esActual ? WHITE : "#374151") + ";"
                + "-fx-background-radius:6; -fx-padding:6 11; -fx-cursor:hand;"
                + "-fx-font-size:13px; -fx-border-color:" + BORDER + "; -fx-border-radius:6;");
        b.setOnAction(e -> accion.run());
        return b;
    }

    // ═══════════════════════════════════════════════════════════════
    // HELPERS UI — idénticos a AlertasView
    // ═══════════════════════════════════════════════════════════════
    private VBox statCard(String bgIcon, String accentColor, String iconFA,
                          String title, Label valueLabel, String sub, String subColor) {
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
        subLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: " + subColor + ";");

        VBox textBox = new VBox(3, titleLbl, valueLabel, subLbl);

        HBox top = new HBox(16);
        top.setAlignment(Pos.CENTER_LEFT);
        top.getChildren().addAll(iconWrap, textBox);
        card.getChildren().add(top);

        card.setOnMouseEntered(e -> card.setTranslateY(-3));
        card.setOnMouseExited(e  -> card.setTranslateY(0));
        return card;
    }

    private Label boldNum(String val, String color) {
        Label l = new Label(val);
        l.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        return l;
    }

    private HBox celdaBox(Label lbl, double width) {
        HBox box = new HBox(lbl);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPrefWidth(width);
        return box;
    }

    private Label celda(String txt, double width) {
        Label l = label(txt != null ? txt : "—", 12, "#374151", false);
        l.setPrefWidth(width);
        l.setMaxWidth(width);
        l.setEllipsisString("…");
        return l;
    }

    private Label badge(String texto, String bg, String color) {
        Label l = label(texto, 11, color, true);
        l.setPadding(new Insets(3, 9, 3, 9));
        l.setStyle("-fx-background-color:" + bg + "; -fx-background-radius:20;"
                + "-fx-text-fill:" + color + "; -fx-font-size:11px; -fx-font-weight:bold;");
        return l;
    }

    private Button btnIcono(String iconFA, String tooltip,
                            String iconColor, String bgColor, Runnable accion) {
        Button b = new Button(iconFA);
        b.setTooltip(new Tooltip(tooltip));
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
        return b;
    }

    private HBox detRow(String campo, String valor) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        Label k = label(campo, 13, GRAY_TEXT, false);
        k.setMinWidth(170);
        row.getChildren().addAll(k, label(valor != null ? valor : "—", 13, "#111827", false));
        return row;
    }

    private Label seccionHeader(String texto) {
        Label l = label(texto, 11, "#888", true);
        VBox.setMargin(l, new Insets(8, 0, 2, 0));
        return l;
    }

    private void shadow(Region node) {
        DropShadow s = new DropShadow();
        s.setRadius(18);
        s.setOffsetY(4);
        s.setColor(Color.rgb(15, 23, 42, 0.08));
        node.setEffect(s);
    }

    private void mostrarAlerta(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void mostrarInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Éxito");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private Label label(String text, double size, String color, boolean bold) {
        Label lbl = new Label(text);
        lbl.setFont(bold ? Font.font("System", FontWeight.BOLD, size)
                         : Font.font("System", size));
        lbl.setTextFill(Color.web(color));
        return lbl;
    }

    private String nvl(String val) { return val != null ? val : "—"; }

    // ── Colores por EstadoAlerta ──────────────────────────────────
    private String colorEstado(EstadoAlerta e) {
        if (e == null) return GRAY_TEXT;
        return switch (e) {
            case PENDIENTE       -> ORANGE;
            case RECIBIDA        -> BLUE;
            case EN_ATENCION     -> PURPLE;
            case UNIDAD_ASIGNADA -> "#0288d1";
            case RESUELTA        -> GREEN;
            case CANCELADA       -> GRAY_TEXT;
        };
    }

    private String estadoBg(EstadoAlerta e) {
        if (e == null) return "#f3f4f6";
        return switch (e) {
            case PENDIENTE       -> "#fff8e1";
            case RECIBIDA        -> "#eff6ff";
            case EN_ATENCION     -> "#f3e5f5";
            case UNIDAD_ASIGNADA -> "#e1f5fe";
            case RESUELTA        -> "#e8f5e9";
            case CANCELADA       -> "#f3f4f6";
        };
    }

    // ── Emoji / colores por TipoAlerta ────────────────────────────
    private String emojiTipo(String nombre) {
        if (nombre == null) return "🚨";
        String n = nombre.toUpperCase();
        if (n.contains("ROB") || n.contains("ASALT")) return "🦹";
        if (n.contains("SOSPECH"))                    return "🕵";
        if (n.contains("INCEND"))                     return "🔥";
        if (n.contains("RUIDO") || n.contains("ALTER")) return "📢";
        if (n.contains("MÉDI") || n.contains("MEDIC")) return "➕";
        if (n.contains("ACCID"))                      return "⚠";
        if (n.contains("ANIMAL"))                     return "🐕";
        return "🚨";
    }

    private String tipoColor(String nombre) {
        if (nombre == null) return GRAY_TEXT;
        String n = nombre.toUpperCase();
        if (n.contains("ROB") || n.contains("ASALT")) return "#b91c1c";
        if (n.contains("INCEND"))                      return "#c2410c";
        if (n.contains("MÉDI") || n.contains("MEDIC")) return "#15803d";
        if (n.contains("ACCID"))                       return "#1d4ed8";
        return "#374151";
    }

    private String tipoBg(String nombre) {
        if (nombre == null) return "#f3f4f6";
        String n = nombre.toUpperCase();
        if (n.contains("ROB") || n.contains("ASALT")) return "#fff1f2";
        if (n.contains("INCEND"))                      return "#fff7ed";
        if (n.contains("MÉDI") || n.contains("MEDIC")) return "#f0fdf4";
        if (n.contains("ACCID"))                       return "#eff6ff";
        return "#f3f4f6";
    }
}
