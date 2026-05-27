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

/**
 * Vista "Mis Alertas" para el usuario ciudadano.

 *
 * @author Maria Cristina
 */
public class MisAlertasView {

    // ── Colores ───────────────────────────────────────────────────
    private static final String WHITE = "#ffffff";
    private static final String BG = "#f4f6fb";
    private static final String RED = "#e53935";
    private static final String RED_LIGHT = "#fff0f0";
    private static final String ORANGE = "#fb8c00";
    private static final String GREEN = "#43a047";
    private static final String BLUE = "#1565c0";
    private static final String GRAY_TEXT = "#6b7280";
    private static final String BORDER = "#e5e7eb";

    private static final DateTimeFormatter FMT
            = DateTimeFormatter.ofPattern("dd/MM/yyyy  HH:mm");

    // ── Dependencias ──────────────────────────────────────────────
    private final Usuario usuarioActual;
    private final AlertaService alertaService;

    // ── Estado ────────────────────────────────────────────────────
    private final ObservableList<Alerta> misAlertas = FXCollections.observableArrayList();
    private final ObservableList<Alerta> alertasFiltradas = FXCollections.observableArrayList();

    private static final int FILAS_POR_PAGINA = 7;
    private int paginaActual = 1;

    // ── Controles ─────────────────────────────────────────────────
    private VBox listaContainer;
    private Label lblMostrando;
    private HBox paginacionBox;
    private TextField campoBusqueda;
    private ComboBox<String> filtroEstado;
    private ComboBox<String> filtroTipo;

    // ── Métricas ──────────────────────────────────────────────────
    private Label lblTotalVal;
    private Label lblPendientesVal;
    private Label lblEnAtencionVal;
    private Label lblResueltasVal;

    // ─────────────────────────────────────────────────────────────
    /**
     * @param usuarioActual Usuario logueado (obtenido del login).
     * @param alertaService Servicio de alertas.
     * @param barrioService Requerido por compatibilidad (no se usa aquí
     * directamente).
     * @param rootPane Referencia al BorderPane raíz (no se usa — ya no hay
     * botón nueva alerta).
     */
    public MisAlertasView(Usuario usuarioActual,
            AlertaService alertaService,
            BarrioService barrioService, // mantenido por compatibilidad
            BorderPane rootPane) {         // mantenido por compatibilidad
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
                buildLista(),
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
    // TOP BAR — sin botón "Nueva alerta"
    // ═══════════════════════════════════════════════════════════════
    private HBox buildTopBar() {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);

        VBox titles = new VBox(4);
        Label title = new Label("Mis Alertas");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#111827"));

        String nombreUsuario = usuarioActual != null
                ? usuarioActual.getPrimer_nombre() + " " + usuarioActual.getPrimer_apellido()
                : "Usuario";
        Label sub = label("Historial de alertas reportadas por " + nombreUsuario, 13, GRAY_TEXT, false);
        titles.getChildren().addAll(title, sub);

        // Solo botón recargar — NO botón "nueva alerta"
        Button btnRecargar = btnSecundario("↻  Recargar");
        btnRecargar.setOnAction(e -> cargarMisAlertas());

        HBox right = new HBox(10);
        right.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(right, Priority.ALWAYS);
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
        lblPendientesVal = boldNum("0");
        lblEnAtencionVal = boldNum("0");
        lblResueltasVal = boldNum("0");

        row.getChildren().addAll(
                statCard("📋", RED_LIGHT, RED, "Mis alertas", lblTotalVal,
                        "Total reportadas", RED),
                statCard("⏳", "#fff8e1", ORANGE, "Pendientes", lblPendientesVal,
                        "Sin atender todavía", ORANGE),
                statCard("🔵", "#eff6ff", BLUE, "En proceso", lblEnAtencionVal,
                        "En atención / asignada", BLUE),
                statCard("✅", "#e8f5e9", GREEN, "Resueltas", lblResueltasVal,
                        "Cerradas correctamente", GREEN)
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

        campoBusqueda = new TextField();
        campoBusqueda.setPromptText("🔍  Buscar por descripción o barrio...");
        campoBusqueda.setPrefHeight(40);
        campoBusqueda.setMaxWidth(Double.MAX_VALUE);
        campoBusqueda.setStyle(fieldStyle());
        HBox.setHgrow(campoBusqueda, Priority.ALWAYS);
        campoBusqueda.textProperty().addListener((obs, o, n) -> filtrarYMostrar());

        filtroEstado = new ComboBox<>();
        filtroEstado.getItems().add("Estado: Todos");
        for (EstadoAlerta e : EstadoAlerta.values()) {
            filtroEstado.getItems().add(e.name());
        }
        filtroEstado.setValue("Estado: Todos");
        filtroEstado.setPrefHeight(40);
        filtroEstado.setStyle(fieldStyle());
        filtroEstado.setOnAction(e -> filtrarYMostrar());

        filtroTipo = new ComboBox<>();
        filtroTipo.getItems().add("Tipo: Todos");
        filtroTipo.setValue("Tipo: Todos");
        filtroTipo.setPrefHeight(40);
        filtroTipo.setStyle(fieldStyle());
        filtroTipo.setOnAction(e -> filtrarYMostrar());

        bar.getChildren().addAll(campoBusqueda, filtroEstado, filtroTipo);
        return bar;
    }

    // ═══════════════════════════════════════════════════════════════
    // TABLA / LISTA
    // ═══════════════════════════════════════════════════════════════
    private VBox buildLista() {
        VBox card = new VBox(0);
        card.setStyle("-fx-background-color:" + WHITE + "; -fx-background-radius:12;");
        shadow(card);

        // Cabecera de columnas
        HBox header = new HBox();
        header.setPadding(new Insets(12, 18, 12, 18));
        header.setStyle("-fx-background-color:#f8f9fc; -fx-background-radius:12 12 0 0;"
                + "-fx-border-color:transparent transparent " + BORDER + " transparent;"
                + "-fx-border-width:0 0 1 0;");
        header.getChildren().addAll(
                colH("Estado", 120),
                colH("Tipo", 160),
                colH("Descripción", 260),
                colH("Barrio", 130),
                colH("Fecha", 150),
                colH("Acciones", 110)
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
    // CARGA DE DATOS — filtrada por username del usuario logueado
    // ═══════════════════════════════════════════════════════════════
    private void cargarMisAlertas() {
        if (alertaService == null || usuarioActual == null) {
            return;
        }

        try {
            List<Alerta> todas = alertaService.listar();

            // ── FILTRO CLAVE: solo las alertas del usuario logueado ──
            String usernameActual = usuarioActual.getUsername();
            List<Alerta> mias = todas.stream()
                    .filter(a -> a.getUsuario() != null
                    && usernameActual != null
                    && usernameActual.equalsIgnoreCase(a.getUsuario().getUsername()))
                    .collect(Collectors.toList());

            misAlertas.setAll(mias);

            // Cargar tipos dinámicos solo de sus alertas
            filtroTipo.getItems().clear();
            filtroTipo.getItems().add("Tipo: Todos");
            mias.stream()
                    .filter(a -> a.getTipoalerta() != null && a.getTipoalerta().getNombre() != null)
                    .map(a -> a.getTipoalerta().getNombre())
                    .distinct()
                    .sorted()
                    .forEach(t -> filtroTipo.getItems().add(t));
            filtroTipo.setValue("Tipo: Todos");

            filtrarYMostrar();
            actualizarMetricas();

        } catch (Exception e) {
            mostrarAlerta("Error al cargar alertas", e.getMessage());
        }
    }

    private void filtrarYMostrar() {
        String busq = campoBusqueda != null ? campoBusqueda.getText().toLowerCase().trim() : "";
        String estado = filtroEstado != null ? filtroEstado.getValue() : "Estado: Todos";
        String tipo = filtroTipo != null ? filtroTipo.getValue() : "Tipo: Todos";

        List<Alerta> filtradas = misAlertas.stream()
                .filter(a -> {
                    boolean matchB = busq.isEmpty()
                            || (a.getDescripcion() != null
                            && a.getDescripcion().toLowerCase().contains(busq))
                            || (a.getBarrio() != null
                            && a.getBarrio().getNombre().toLowerCase().contains(busq));

                    boolean matchE = estado == null || estado.startsWith("Estado")
                            || (a.getEstado() != null
                            && a.getEstado().name().equalsIgnoreCase(estado));

                    boolean matchT = tipo == null || tipo.startsWith("Tipo")
                            || (a.getTipoalerta() != null
                            && a.getTipoalerta().getNombre().equalsIgnoreCase(tipo));

                    return matchB && matchE && matchT;
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
            // Estado vacío — sin botón nueva alerta
            VBox vacio = new VBox(12);
            vacio.setAlignment(Pos.CENTER);
            vacio.setPadding(new Insets(48));
            Label icn = new Label("📭");
            icn.setFont(Font.font(56));
            Label msg = label("No tienes alertas reportadas todavía.", 14, GRAY_TEXT, false);
            Label hint = label("Usa el botón ⚠ PÁNICO en el Dashboard para reportar una emergencia.", 12, GRAY_TEXT, false);
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
        fila.setPadding(new Insets(12, 18, 12, 18));
        String bgN = "-fx-background-color:" + (par ? WHITE : "#fafbfd") + ";"
                + "-fx-border-color:transparent transparent " + BORDER + " transparent;"
                + "-fx-border-width:0 0 1 0;";
        fila.setStyle(bgN);
        fila.setOnMouseEntered(e -> fila.setStyle("-fx-background-color:#f0f4ff;"
                + "-fx-border-color:transparent transparent " + BORDER + " transparent;"
                + "-fx-border-width:0 0 1 0;"));
        fila.setOnMouseExited(e -> fila.setStyle(bgN));

        // Estado badge
        Label estLbl = badge(
                a.getEstado() != null ? a.getEstado().name() : "—",
                estadoBg(a.getEstado()),
                colorEstado(a.getEstado())
        );
        HBox estBox = new HBox(estLbl);
        estBox.setAlignment(Pos.CENTER_LEFT);
        estBox.setPrefWidth(120);

        // Tipo badge
        String tipoNom = a.getTipoalerta() != null ? a.getTipoalerta().getNombre() : "—";
        Label tipoLbl = badge(emojiTipo(tipoNom) + "  " + tipoNom,
                tipoBg(tipoNom), tipoColor(tipoNom));
        tipoLbl.setPrefWidth(160);

        // Descripción
        String desc = a.getDescripcion() != null ? a.getDescripcion() : "—";
        Label descLbl = celda(desc.length() > 40 ? desc.substring(0, 40) + "…" : desc, 260);
        descLbl.setTooltip(new Tooltip(desc));

        // Barrio
        Label barrioLbl = celda(a.getBarrio() != null ? a.getBarrio().getNombre() : "—", 130);

        // Fecha
        String fechaStr = a.getFechaHora() != null ? a.getFechaHora().format(FMT) : "—";
        Label fechaLbl = celda(fechaStr, 150);

        // Acciones
        HBox acciones = buildAcciones(a);

        fila.getChildren().addAll(estBox, tipoLbl, descLbl, barrioLbl, fechaLbl, acciones);
        return fila;
    }

    // ═══════════════════════════════════════════════════════════════
    // ACCIONES — solo ver detalle y cancelar (si aplica)
    // ═══════════════════════════════════════════════════════════════
    private HBox buildAcciones(Alerta a) {
        HBox box = new HBox(6);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPrefWidth(110);

        // Ver detalle — siempre disponible
        box.getChildren().add(btnIcono("👁", "Ver detalle", () -> abrirDetalle(a)));

        // Cancelar — solo si está PENDIENTE o RECIBIDA
        boolean puedeCancelar = a.getEstado() == EstadoAlerta.PENDIENTE
                || a.getEstado() == EstadoAlerta.RECIBIDA;
        Button btnCancelar = btnIcono("✕", "Cancelar alerta", () -> cancelarAlerta(a));
        btnCancelar.setDisable(!puedeCancelar);
        btnCancelar.setOpacity(puedeCancelar ? 1.0 : 0.3);
        box.getChildren().add(btnCancelar);

        return box;
    }

    // ═══════════════════════════════════════════════════════════════
    // DETALLE
    // ═══════════════════════════════════════════════════════════════
    private void abrirDetalle(Alerta a) {
        Alert dlg = new Alert(Alert.AlertType.INFORMATION);
        dlg.setTitle("Detalle de alerta #" + a.getId_alerta());
        dlg.setHeaderText(null);

        VBox content = new VBox(10);
        content.setPadding(new Insets(16));
        content.setPrefWidth(440);

        // Badge estado grande centrado
        Label estBadge = badge(
                a.getEstado() != null ? a.getEstado().name() : "—",
                estadoBg(a.getEstado()), colorEstado(a.getEstado()));
        estBadge.setFont(Font.font("System", FontWeight.BOLD, 14));
        HBox estRow = new HBox(estBadge);
        estRow.setAlignment(Pos.CENTER);
        content.getChildren().addAll(estRow, new Separator());

        content.getChildren().addAll(
                detRow("🆔 ID alerta", "#" + a.getId_alerta()),
                detRow("🔔 Tipo", a.getTipoalerta() != null ? a.getTipoalerta().getNombre() : "—"),
                detRow("🏘 Barrio", a.getBarrio() != null ? a.getBarrio().getNombre() : "—"),
                detRow("📋 Descripción", a.getDescripcion() != null ? a.getDescripcion() : "—"),
                detRow("🔫 Tipo de arma", a.getTipoarma() != null ? a.getTipoarma().getNombre() : "Sin arma"),
                detRow("🚗 Transporte", a.getMediotransporte() != null
                        ? a.getMediotransporte().getNombre() : "Sin medio"),
                detRow("📅 Fecha / Hora", a.getFechaHora() != null ? a.getFechaHora().format(FMT) : "—")
        );

        if (a.getDireccion() != null) {
            content.getChildren().add(new Separator());
            content.getChildren().addAll(
                    detRow("🏠 Calle", nvl(a.getDireccion().getCalle())),
                    detRow("📌 Carrera", nvl(a.getDireccion().getCarrera())),
                    detRow("📍 Referencia", nvl(a.getDireccion().getReferencia()))
            );
        }

        // Botón cancelar dentro del detalle si aplica
        if (a.getEstado() == EstadoAlerta.PENDIENTE
                || a.getEstado() == EstadoAlerta.RECIBIDA) {
            content.getChildren().add(new Separator());
            Button btnCancelDetalle = new Button("✕  Cancelar esta alerta");
            btnCancelDetalle.setStyle("-fx-background-color:" + RED_LIGHT
                    + "; -fx-text-fill:" + RED + ";"
                    + "-fx-font-size:13px; -fx-font-weight:bold; -fx-background-radius:8;"
                    + "-fx-border-color:" + RED + "; -fx-border-radius:8;"
                    + "-fx-padding:8 16; -fx-cursor:hand;");
            btnCancelDetalle.setOnAction(e -> {
                dlg.close();
                cancelarAlerta(a);
            });
            content.getChildren().add(btnCancelDetalle);
        }

        dlg.getDialogPane().setContent(content);
        dlg.getDialogPane().getButtonTypes().setAll(ButtonType.CLOSE);
        dlg.showAndWait();
    }

    // ═══════════════════════════════════════════════════════════════
    // CANCELAR ALERTA
    // ═══════════════════════════════════════════════════════════════
    private void cancelarAlerta(Alerta a) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancelar alerta");
        confirm.setHeaderText("¿Deseas cancelar la alerta #" + a.getId_alerta() + "?");
        confirm.setContentText("La alerta pasará al estado CANCELADA. "
                + "Esta acción no se puede deshacer.");

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
                        mostrarExito("Alerta #" + a.getId_alerta() + " cancelada correctamente.");
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
        long total = misAlertas.size();
        long pendientes = misAlertas.stream()
                .filter(a -> a.getEstado() == EstadoAlerta.PENDIENTE
                || a.getEstado() == EstadoAlerta.RECIBIDA).count();
        long enAtencion = misAlertas.stream()
                .filter(a -> a.getEstado() == EstadoAlerta.EN_ATENCION
                || a.getEstado() == EstadoAlerta.UNIDAD_ASIGNADA).count();
        long resueltas = misAlertas.stream()
                .filter(a -> a.getEstado() == EstadoAlerta.RESUELTA).count();

        lblTotalVal.setText(String.valueOf(total));
        lblPendientesVal.setText(String.valueOf(pendientes));
        lblEnAtencionVal.setText(String.valueOf(enAtencion));
        lblResueltasVal.setText(String.valueOf(resueltas));
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
        k.setMinWidth(150);
        row.getChildren().addAll(k, label(valor != null ? valor : "—", 13, "#111827", false));
        return row;
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

    private void mostrarExito(String msg) {
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

    private String nvl(String val) {
        return val != null ? val : "—";
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
                "#7b1fa2";
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
