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
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import sistemagestion.model.*;
import sistemagestion.service.AlertaService;
import sistemagestion.service.BarrioService;
import sistemagestion.service.TipoAlertaService;

/**
 * Vista "Alertas" para el usuario ciudadano.
 * @author Maria Cristina
 */
public class AlertasView {

    // ── Colores ───────────────────────────────────────────────────
    private static final String WHITE = "#ffffff";
    private static final String BG = "#f4f6fb";
    private static final String RED = "#e53935";
    private static final String RED_LIGHT = "#fff0f0";
    private static final String ORANGE = "#fb8c00";
    private static final String GREEN = "#43a047";
    private static final String BLUE = "#1565c0";
    private static final String PURPLE = "#7b1fa2";
    private static final String GRAY_TEXT = "#6b7280";
    private static final String BORDER = "#e5e7eb";

    private static final DateTimeFormatter FMT
            = DateTimeFormatter.ofPattern("dd/MM/yyyy  HH:mm");

    // ── Servicios ─────────────────────────────────────────────────
    private final AlertaService alertaService;
    private final BarrioService barrioService;
    private final TipoAlertaService tipoAlertaService;

    // ── Usuario logueado ──────────────────────────────────────────
    private final Usuario usuarioActual;

    // ── Estado de datos ───────────────────────────────────────────
    private final ObservableList<Alerta> todasLasAlertas = FXCollections.observableArrayList();
    private final ObservableList<Alerta> alertasFiltradas = FXCollections.observableArrayList();

    private static final int FILAS_POR_PAGINA = 8;
    private int paginaActual = 1;

    // ── Controles ─────────────────────────────────────────────────
    private VBox listaContainer;
    private Label lblMostrando;
    private HBox paginacionBox;
    private TextField campoBusqueda;
    private ComboBox<String> filtroEstado;
    private ComboBox<String> filtroTipo;
    private ComboBox<String> filtroBarrio;

    // ── Métricas ──────────────────────────────────────────────────
    private Label lblTotalVal;
    private Label lblActivasVal;
    private Label lblEnAtencionVal;
    private Label lblResueltasVal;

    // ─────────────────────────────────────────────────────────────
    public AlertasView(Usuario usuarioActual,
            AlertaService alertaService,
            BarrioService barrioService) {
        this.usuarioActual = usuarioActual;
        this.alertaService = alertaService;
        this.barrioService = barrioService;

        TipoAlertaService tmp = null;
        try {
            tmp = new TipoAlertaService();
        } catch (SQLException e) {
            System.out.println("TipoAlertaService no disponible: " + e.getMessage());
        }
        this.tipoAlertaService = tmp;
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

        cargarAlertas();

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
        Label title = new Label("Alertas de la Comunidad");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#111827"));

        // Subtítulo personalizado con el barrio del usuario
        String barrioUsuario = obtenerBarrioUsuario();
        String subTexto = barrioUsuario != null
                ? "Alertas activas en " + barrioUsuario + " y comunidad cercana"
                : "Alertas activas reportadas en la comunidad";
        titles.getChildren().addAll(title, label(subTexto, 13, GRAY_TEXT, false));

        HBox right = new HBox(12);
        right.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(right, Priority.ALWAYS);

        Button btnRecargar = btnSecundario("↻  Actualizar");
        btnRecargar.setOnAction(e -> cargarAlertas());

        right.getChildren().add(btnRecargar);
        bar.getChildren().addAll(titles, right);
        return bar;
    }

    // ═══════════════════════════════════════════════════════════════
    // MÉTRICAS
    // ═══════════════════════════════════════════════════════════════
    private HBox buildMetrics() {
        HBox row = new HBox(16);

        lblTotalVal = boldNum("0");
        lblActivasVal = boldNum("0");
        lblEnAtencionVal = boldNum("0");
        lblResueltasVal = boldNum("0");

        row.getChildren().addAll(
                statCard("🔔", RED_LIGHT, RED, "Total alertas", lblTotalVal,
                        "En la comunidad", RED),
                statCard("⚠", "#fff8e1", ORANGE, "Activas / Nuevas", lblActivasVal,
                        "PENDIENTE · RECIBIDA", ORANGE),
                statCard("🚔", "#eff6ff", BLUE, "En atención", lblEnAtencionVal,
                        "Policía asignado", BLUE),
                statCard("✅", "#e8f5e9", GREEN, "Resueltas hoy", lblResueltasVal,
                        "Casos cerrados", GREEN)
        );
        return row;
    }

    // ═══════════════════════════════════════════════════════════════
    // TOOLBAR
    // ═══════════════════════════════════════════════════════════════
    private HBox buildToolbar() {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(16));
        bar.setStyle("-fx-background-color:" + WHITE + "; -fx-background-radius:12;");
        shadow(bar);

        // Búsqueda
        campoBusqueda = new TextField();
        campoBusqueda.setPromptText("🔍  Buscar por descripción, barrio o tipo...");
        campoBusqueda.setPrefHeight(40);
        campoBusqueda.setMaxWidth(Double.MAX_VALUE);
        campoBusqueda.setStyle(fieldStyle());
        HBox.setHgrow(campoBusqueda, Priority.ALWAYS);
        campoBusqueda.textProperty().addListener((obs, o, n) -> filtrarYMostrar());

        // Filtro estado
        filtroEstado = new ComboBox<>();
        filtroEstado.getItems().add("Estado: Todos");
        for (EstadoAlerta e : EstadoAlerta.values()) {
            filtroEstado.getItems().add(e.name());
        }
        filtroEstado.setValue("Estado: Todos");
        filtroEstado.setPrefHeight(40);
        filtroEstado.setStyle(fieldStyle());
        filtroEstado.setOnAction(e -> filtrarYMostrar());

        // Filtro tipo — cargado dinámicamente desde BD
        filtroTipo = new ComboBox<>();
        filtroTipo.getItems().add("Tipo: Todos");
        filtroTipo.setValue("Tipo: Todos");
        filtroTipo.setPrefHeight(40);
        filtroTipo.setStyle(fieldStyle());
        filtroTipo.setOnAction(e -> filtrarYMostrar());
        cargarTiposEnFiltro();

        // Filtro barrio — cargado desde BD
        filtroBarrio = new ComboBox<>();
        filtroBarrio.getItems().add("Barrio: Todos");
        filtroBarrio.setValue("Barrio: Todos");
        filtroBarrio.setPrefHeight(40);
        filtroBarrio.setStyle(fieldStyle());
        filtroBarrio.setOnAction(e -> filtrarYMostrar());
        cargarBarriosEnFiltro();

        bar.getChildren().addAll(campoBusqueda, filtroEstado, filtroTipo, filtroBarrio);
        return bar;
    }

    // ── Carga tipos desde TipoAlertaService ──────────────────────
    private void cargarTiposEnFiltro() {
        if (tipoAlertaService == null) {
            return;
        }
        try {
            tipoAlertaService.listar().forEach(t -> {
                if (t.getNombre() != null) {
                    filtroTipo.getItems().add(t.getNombre());
                }
            });
        } catch (Exception e) {
            System.out.println("Error cargando tipos: " + e.getMessage());
        }
    }

    // ── Carga barrios desde BarrioService ─────────────────────────
    private void cargarBarriosEnFiltro() {
        if (barrioService == null) {
            return;
        }
        try {
            barrioService.listar().forEach(b -> {
                if (b.getNombre() != null) {
                    filtroBarrio.getItems().add(b.getNombre());
                }
            });
        } catch (Exception e) {
            System.out.println("Error cargando barrios: " + e.getMessage());
        }
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
                colH("Estado", 120),
                colH("Tipo", 155),
                colH("Descripción", 240),
                colH("Reportado por", 130),
                colH("Barrio", 120),
                colH("Fecha", 145),
                colH("Alarma", 120),
                colH("Acciones", 80)
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
    // CARGA DE DATOS — AlertaService.listar() completo
    // ═══════════════════════════════════════════════════════════════
    private void cargarAlertas() {
        if (alertaService == null) {
            return;
        }
        try {
            List<Alerta> todas = alertaService.listar();
            todasLasAlertas.setAll(todas);
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
        String busq = campoBusqueda != null ? campoBusqueda.getText().toLowerCase().trim() : "";
        String estado = filtroEstado != null ? filtroEstado.getValue() : "Estado: Todos";
        String tipo = filtroTipo != null ? filtroTipo.getValue() : "Tipo: Todos";
        String barrio = filtroBarrio != null ? filtroBarrio.getValue() : "Barrio: Todos";

        List<Alerta> filtradas = todasLasAlertas.stream()
                .filter(a -> {
                    boolean matchB = busq.isEmpty()
                            || (a.getDescripcion() != null
                            && a.getDescripcion().toLowerCase().contains(busq))
                            || (a.getBarrio() != null
                            && a.getBarrio().getNombre().toLowerCase().contains(busq))
                            || (a.getTipoalerta() != null
                            && a.getTipoalerta().getNombre().toLowerCase().contains(busq))
                            || (a.getUsuario() != null
                            && nombreUsuario(a.getUsuario()).toLowerCase().contains(busq));

                    boolean matchE = estado == null || estado.startsWith("Estado")
                            || (a.getEstado() != null
                            && a.getEstado().name().equalsIgnoreCase(estado));

                    boolean matchT = tipo == null || tipo.startsWith("Tipo")
                            || (a.getTipoalerta() != null
                            && a.getTipoalerta().getNombre().equalsIgnoreCase(tipo));

                    boolean matchBar = barrio == null || barrio.startsWith("Barrio")
                            || (a.getBarrio() != null
                            && a.getBarrio().getNombre().equalsIgnoreCase(barrio));

                    return matchB && matchE && matchT && matchBar;
                })
                .collect(Collectors.toList());

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
            Label msg = label("No hay alertas activas en este momento.", 15, GRAY_TEXT, false);
            Label hint = label("¡Buenas noticias! Tu comunidad está tranquila.", 12, GREEN, false);
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
        HBox fila = new HBox();
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
        Label estLbl = badge(
                a.getEstado() != null ? a.getEstado().name() : "—",
                estadoBg(a.getEstado()), colorEstado(a.getEstado()));
        HBox estBox = celdaBox(estLbl, 120);

        // ── Tipo ─────────────────────────────────────────────────
        String tipoNom = a.getTipoalerta() != null ? a.getTipoalerta().getNombre() : "—";
        Label tipoLbl = badge(emojiTipo(tipoNom) + "  " + tipoNom,
                tipoBg(tipoNom), tipoColor(tipoNom));
        tipoLbl.setPrefWidth(155);
        HBox tipoBox = celdaBox(tipoLbl, 155);

        // ── Descripción ──────────────────────────────────────────
        String desc = a.getDescripcion() != null ? a.getDescripcion() : "—";
        String descCrt = desc.length() > 36 ? desc.substring(0, 36) + "…" : desc;
        Label descLbl = celda(descCrt, 240);
        descLbl.setTooltip(new Tooltip(desc));

        // ── Reportado por ────────────────────────────────────────
        String reportadoPor = a.getUsuario() != null ? nombreUsuario(a.getUsuario()) : "—";
        Label reportadoLbl = celda(reportadoPor, 130);

        // ── Barrio ───────────────────────────────────────────────
        String barrioNom = a.getBarrio() != null ? a.getBarrio().getNombre() : "—";
        Label barrioLbl = celda(barrioNom, 120);

        // ── Fecha ────────────────────────────────────────────────
        String fechaStr = a.getFechaHora() != null ? a.getFechaHora().format(FMT) : "—";
        Label fechaLbl = celda(fechaStr, 145);

        // ── Alarma asignada ──────────────────────────────────────
        String alarmaNom = a.getAlarma() != null ? a.getAlarma().getNombre() : "Sin alarma";
        Label alarmaLbl;
        if (a.getAlarma() != null) {
            alarmaLbl = badge("🚨  " + alarmaNom, RED_LIGHT, RED);
        } else {
            alarmaLbl = badge("—", "#f3f4f6", GRAY_TEXT);
        }
        alarmaLbl.setPrefWidth(120);
        HBox alarmaBox = celdaBox(alarmaLbl, 120);

        // ── Acciones — solo ver ──────────────────────────────────
        HBox acciones = new HBox(6);
        acciones.setAlignment(Pos.CENTER_LEFT);
        acciones.setPrefWidth(80);
        acciones.getChildren().add(btnIcono("👁", "Ver detalle", () -> abrirDetalle(a)));

        fila.getChildren().addAll(
                estBox, tipoBox, descLbl, reportadoLbl,
                barrioLbl, fechaLbl, alarmaBox, acciones
        );
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
                a.getEstado() != null ? a.getEstado().name() : "—",
                estadoBg(a.getEstado()), colorEstado(a.getEstado()));
        estBadge.setFont(Font.font("System", FontWeight.BOLD, 14));
        estBadge.setPadding(new Insets(5, 14, 5, 14));
        HBox estRow = new HBox(estBadge);
        estRow.setAlignment(Pos.CENTER);
        content.getChildren().addAll(estRow, new Separator());

        // Sección: Datos de la alerta
        content.getChildren().add(seccionHeader("📋 INFORMACIÓN DE LA ALERTA"));
        content.getChildren().addAll(
                detRow("🆔 ID", "#" + a.getId_alerta()),
                detRow("🔔 Tipo", tipoNvl(a.getTipoalerta())),
                detRow("📋 Descripción", nvl(a.getDescripcion())),
                detRow("📅 Fecha / Hora", a.getFechaHora() != null
                        ? a.getFechaHora().format(FMT) : "—")
        );

        content.getChildren().add(new Separator());

        // Sección: Ubicación
        content.getChildren().add(seccionHeader("📍 UBICACIÓN"));
        content.getChildren().addAll(
                detRow("🏘 Barrio", a.getBarrio() != null ? a.getBarrio().getNombre() : "—"),
                detRow("🏙 Comuna", a.getBarrio() != null && a.getBarrio().getComuna() != null
                        ? a.getBarrio().getComuna().getNombre() : "—")
        );
        if (a.getDireccion() != null) {
            Direccion d = a.getDireccion();
            content.getChildren().addAll(
                    detRow("🏠 Calle", nvl(d.getCalle())),
                    detRow("📌 Carrera", nvl(d.getCarrera())),
                    detRow("📍 Referencia", nvl(d.getReferencia())),
                    detRow("🏗 Etapa", nvl(d.getEtapa())),
                    detRow("🏡 Manzana", nvl(d.getManzana())),
                    detRow("🚪 Casa", nvl(d.getCasa()))
            );
        }

        content.getChildren().add(new Separator());

        // Sección: Detalles del incidente
        content.getChildren().add(seccionHeader("⚠ DETALLES DEL INCIDENTE"));
        content.getChildren().addAll(
                detRow("🔫 Tipo de arma",
                        a.getTipoarma() != null ? a.getTipoarma().getNombre() : "Sin arma reportada"),
                detRow("🚗 Medio de transporte",
                        a.getMediotransporte() != null ? a.getMediotransporte().getNombre()
                        : "Sin medio registrado")
        );

        // Alarma asignada
        if (a.getAlarma() != null) {
            content.getChildren().add(new Separator());
            content.getChildren().add(seccionHeader("🚨 ALARMA ASIGNADA"));
            content.getChildren().addAll(
                    detRow("📛 Nombre alarma", nvl(a.getAlarma().getNombre())),
                    detRow("⚡ Estado alarma", a.getAlarma().getEstado() != null
                            ? a.getAlarma().getEstado().name() : "—")
            );
        }

        content.getChildren().add(new Separator());

        // Sección: Reportado por
        content.getChildren().add(seccionHeader("👤 REPORTADO POR"));
        if (a.getUsuario() != null) {
            Usuario u = a.getUsuario();
            // Mostrar solo nombre, nunca datos sensibles completos al ciudadano
            content.getChildren().addAll(
                    detRow("👤 Nombre", nvl(u.getPrimer_nombre())),
                    detRow("📞 Teléfono", nvl(u.getTelefono()))
            );
        } else {
            content.getChildren().add(label("Información no disponible", 12, GRAY_TEXT, false));
        }

        ScrollPane scrollContent = new ScrollPane(content);
        scrollContent.setFitToWidth(true);
        scrollContent.setPrefHeight(500);
        scrollContent.setStyle("-fx-background:white; -fx-background-color:white;");

        dlg.getDialogPane().setContent(scrollContent);
        dlg.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dlg.getDialogPane().setPrefWidth(500);
        dlg.showAndWait();
    }

    // ═══════════════════════════════════════════════════════════════
    // MÉTRICAS
    // ═══════════════════════════════════════════════════════════════
    private void actualizarMetricas() {
        long total = todasLasAlertas.size();
        long activas = todasLasAlertas.stream()
                .filter(a -> a.getEstado() == EstadoAlerta.PENDIENTE
                || a.getEstado() == EstadoAlerta.RECIBIDA).count();
        long enAtencion = todasLasAlertas.stream()
                .filter(a -> a.getEstado() == EstadoAlerta.EN_ATENCION
                || a.getEstado() == EstadoAlerta.UNIDAD_ASIGNADA).count();

        // Resueltas del día de hoy
        long resueltasHoy = todasLasAlertas.stream()
                .filter(a -> a.getEstado() == EstadoAlerta.RESUELTA
                && a.getFechaHora() != null
                && a.getFechaHora().toLocalDate()
                        .equals(java.time.LocalDate.now()))
                .count();

        lblTotalVal.setText(String.valueOf(total));
        lblActivasVal.setText(String.valueOf(activas));
        lblEnAtencionVal.setText(String.valueOf(enAtencion));
        lblResueltasVal.setText(String.valueOf(resueltasHoy));
    }

    // ═══════════════════════════════════════════════════════════════
    // PAGINACIÓN
    // ═══════════════════════════════════════════════════════════════
    private void actualizarPaginacion() {
        if (paginacionBox == null) {
            return;
        }
        paginacionBox.getChildren().clear();

        int total = alertasFiltradas.size();
        int totalPags = (int) Math.ceil((double) total / FILAS_POR_PAGINA);
        if (totalPags <= 1) {
            return;
        }

        paginacionBox.getChildren().add(btnPag("‹", paginaActual > 1,
                () -> {
                    paginaActual--;
                    renderizarPagina();
                }));

        int ini = Math.max(1, paginaActual - 2);
        int fin = Math.min(totalPags, paginaActual + 2);

        if (ini > 1) {
            paginacionBox.getChildren().addAll(
                    btnPag("1", true, () -> {
                        paginaActual = 1;
                        renderizarPagina();
                    }),
                    label("…", 13, GRAY_TEXT, false));
        }

        for (int i = ini; i <= fin; i++) {
            final int pg = i;
            paginacionBox.getChildren().add(btnPag(String.valueOf(i), true,
                    () -> {
                        paginaActual = pg;
                        renderizarPagina();
                    }));
        }

        if (fin < totalPags) {
            paginacionBox.getChildren().addAll(
                    label("…", 13, GRAY_TEXT, false),
                    btnPag(String.valueOf(totalPags), true,
                            () -> {
                                paginaActual = totalPags;
                                renderizarPagina();
                            }));
        }

        paginacionBox.getChildren().add(btnPag("›", paginaActual < totalPags,
                () -> {
                    paginaActual++;
                    renderizarPagina();
                }));
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
    // HELPERS UI
    // ═══════════════════════════════════════════════════════════════
    private VBox statCard(String icon, String bgIcon, String iconColor,
            String title, Label valueLabel, String sub, String subColor) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(18));
        card.setStyle("-fx-background-color:" + WHITE + "; -fx-background-radius:14;");
        HBox.setHgrow(card, Priority.ALWAYS);
        shadow(card);

        StackPane iconBox = new StackPane();
        Rectangle iconBg = new Rectangle(44, 44);
        iconBg.setArcWidth(10);
        iconBg.setArcHeight(10);
        iconBg.setFill(Color.web(bgIcon));
        Label iconLbl = label(icon, 20, iconColor, false);
        iconBox.getChildren().addAll(iconBg, iconLbl);

        HBox top = new HBox(12);
        top.setAlignment(Pos.CENTER_LEFT);
        top.getChildren().addAll(iconBox, label(title, 12, GRAY_TEXT, false));

        card.getChildren().addAll(top, valueLabel, label(sub, 11, subColor, false));
        card.setOnMouseEntered(e -> card.setTranslateY(-2));
        card.setOnMouseExited(e -> card.setTranslateY(0));
        return card;
    }

    private Label boldNum(String val) {
        Label l = new Label(val);
        l.setFont(Font.font("System", FontWeight.BOLD, 32));
        l.setTextFill(Color.web("#111827"));
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
        l.setStyle("-fx-background-color:" + bg + "; -fx-background-radius:20;");
        return l;
    }

    private Button btnIcono(String icon, String tooltip, Runnable accion) {
        Button b = new Button(icon);
        b.setTooltip(new Tooltip(tooltip));
        b.setStyle("-fx-background-color:transparent; -fx-font-size:14px;"
                + "-fx-cursor:hand; -fx-padding:4 6;");
        b.setOnMouseEntered(e -> b.setStyle("-fx-background-color:#f0f0f0; -fx-font-size:14px;"
                + "-fx-cursor:hand; -fx-background-radius:6; -fx-padding:4 6;"));
        b.setOnMouseExited(e -> b.setStyle("-fx-background-color:transparent; -fx-font-size:14px;"
                + "-fx-cursor:hand; -fx-padding:4 6;"));
        b.setOnAction(e -> accion.run());
        return b;
    }

    private Button btnSecundario(String texto) {
        Button btn = new Button(texto);
        btn.setPrefHeight(38);
        btn.setStyle("-fx-background-color:white; -fx-text-fill:#374151;"
                + "-fx-font-size:13px; -fx-border-color:" + BORDER + ";"
                + "-fx-border-radius:8; -fx-background-radius:8;"
                + "-fx-padding:6 14; -fx-cursor:hand;");
        return btn;
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

    private String fieldStyle() {
        return "-fx-background-color:white; -fx-border-color:" + BORDER + ";"
                + "-fx-border-radius:8; -fx-background-radius:8;"
                + "-fx-font-size:13px; -fx-padding:6 12;";
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

    private Label label(String text, double size, String color, boolean bold) {
        Label lbl = new Label(text);
        lbl.setFont(bold ? Font.font("System", FontWeight.BOLD, size)
                : Font.font("System", size));
        lbl.setTextFill(Color.web(color));
        return lbl;
    }

    // ── Utilidades de texto ───────────────────────────────────────
    private String nvl(String val) {
        return val != null ? val : "—";
    }

    private String tipoNvl(TipoAlerta t) {
        return t != null && t.getNombre() != null ? t.getNombre() : "—";
    }

    private String nombreUsuario(Usuario u) {
        if (u == null) {
            return "—";
        }
        String n = u.getPrimer_nombre() != null ? u.getPrimer_nombre() : "";
        // Solo primer nombre para privacidad del ciudadano
        return n.isBlank() ? nvl(u.getUsername()) : n;
    }

    private String obtenerBarrioUsuario() {
        if (usuarioActual == null) {
            return null;
        }
        if (usuarioActual.getDireccion() == null) {
            return null;
        }
        if (usuarioActual.getDireccion().getBarrio() == null) {
            return null;
        }
        return usuarioActual.getDireccion().getBarrio().getNombre();
    }

    // ── Colores por EstadoAlerta ──────────────────────────────────
    private String colorEstado(EstadoAlerta e) {
        if (e == null) {
            return GRAY_TEXT;
        }
        return switch (e) {
            case PENDIENTE ->
                ORANGE;
            case RECIBIDA ->
                BLUE;
            case EN_ATENCION ->
                PURPLE;
            case UNIDAD_ASIGNADA ->
                "#0288d1";
            case RESUELTA ->
                GREEN;
            case CANCELADA ->
                GRAY_TEXT;
        };
    }

    private String estadoBg(EstadoAlerta e) {
        if (e == null) {
            return "#f3f4f6";
        }
        return switch (e) {
            case PENDIENTE ->
                "#fff8e1";
            case RECIBIDA ->
                "#eff6ff";
            case EN_ATENCION ->
                "#f3e5f5";
            case UNIDAD_ASIGNADA ->
                "#e1f5fe";
            case RESUELTA ->
                "#e8f5e9";
            case CANCELADA ->
                "#f3f4f6";
        };
    }

    // ── Emoji / colores por TipoAlerta ────────────────────────────
    private String emojiTipo(String nombre) {
        if (nombre == null) {
            return "🚨";
        }
        String n = nombre.toUpperCase();
        if (n.contains("ROB") || n.contains("ASALT")) {
            return "🦹";
        }
        if (n.contains("SOSPECH")) {
            return "🕵";
        }
        if (n.contains("INCEND")) {
            return "🔥";
        }
        if (n.contains("RUIDO") || n.contains("ALTER")) {
            return "📢";
        }
        if (n.contains("MÉDI") || n.contains("MEDIC")) {
            return "➕";
        }
        if (n.contains("ACCID")) {
            return "⚠";
        }
        if (n.contains("ANIMAL")) {
            return "🐕";
        }
        return "🚨";
    }

    private String tipoColor(String nombre) {
        if (nombre == null) {
            return GRAY_TEXT;
        }
        String n = nombre.toUpperCase();
        if (n.contains("ROB") || n.contains("ASALT")) {
            return "#b91c1c";
        }
        if (n.contains("INCEND")) {
            return "#c2410c";
        }
        if (n.contains("MÉDI") || n.contains("MEDIC")) {
            return "#15803d";
        }
        if (n.contains("ACCID")) {
            return "#1d4ed8";
        }
        return "#374151";
    }

    private String tipoBg(String nombre) {
        if (nombre == null) {
            return "#f3f4f6";
        }
        String n = nombre.toUpperCase();
        if (n.contains("ROB") || n.contains("ASALT")) {
            return "#fff1f2";
        }
        if (n.contains("INCEND")) {
            return "#fff7ed";
        }
        if (n.contains("MÉDI") || n.contains("MEDIC")) {
            return "#f0fdf4";
        }
        if (n.contains("ACCID")) {
            return "#eff6ff";
        }
        return "#f3f4f6";
    }
}
