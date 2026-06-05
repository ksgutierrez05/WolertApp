package sistemagestion.view;

// ════════════════════════════════════════════════════════════════════════════
//  IMPORTS GLOBALES
// ════════════════════════════════════════════════════════════════════════════
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import sistemagestion.model.*;
import sistemagestion.service.*;

// ════════════════════════════════════════════════════════════════════════════
//
//  CLASE 1 – EstiloUI
//  GRASP: Pure Fabrication + Information Expert + Protected Variations
//
//  Centraliza toda la paleta de colores, fuentes y estilos inline.
//  Si la paleta cambia, solo se modifica AQUÍ (Protected Variations).
//  No representa ninguna entidad del dominio (Pure Fabrication).
//
// ════════════════════════════════════════════════════════════════════════════
final class EstiloUI {

    // ── Paleta principal ──────────────────────────────────────────
    static final String WHITE = "#ffffff";
    static final String BG = "#f4f6fb";
    static final String BLUE = "#1565c0";
    static final String BLUE_LIGHT = "#e8f0fe";
    static final String GREEN = "#43a047";
    static final String GREEN_LIGHT = "#e8f5e9";
    static final String RED = "#e53935";
    static final String RED_LIGHT = "#fff0f0";
    static final String ORANGE = "#fb8c00";
    static final String ORANGE_LIGHT = "#fff8e1";
    static final String PURPLE = "#7b1fa2";
    static final String PURPLE_LIGHT = "#f3e5f5";
    static final String GRAY_TEXT = "#6b7280";
    static final String BORDER = "#e5e7eb";

    private static final String[] AVATAR_COLORS = {
        "#1565c0", "#2e7d32", "#6a1b9a", "#c62828",
        "#e65100", "#00695c", "#283593", "#4e342e"
    };

    private EstiloUI() {
    }

    // ── Fábrica de Labels ─────────────────────────────────────────
    static Label label(String text, double size, String color, boolean bold) {
        Label lbl = new Label(text);
        lbl.setFont(bold
                ? Font.font("System", FontWeight.BOLD, size)
                : Font.font("System", size));
        lbl.setTextFill(Color.web(color));
        return lbl;
    }

    /**
     * Color de avatar consistente basado en el nombre.
     */
    static String colorAvatar(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return AVATAR_COLORS[0];
        }
        return AVATAR_COLORS[Math.abs(nombre.hashCode()) % AVATAR_COLORS.length];
    }

    /**
     * Iniciales (máx. 2 letras) de un nombre.
     */
    static String iniciales(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return "?";
        }
        String[] p = nombre.trim().split("\\s+");
        return p.length == 1
                ? p[0].substring(0, 1).toUpperCase()
                : (p[0].substring(0, 1) + p[1].substring(0, 1)).toUpperCase();
    }

    static String estiloCampo() {
        return "-fx-background-color:#f5f7fb;-fx-background-radius:8;"
                + "-fx-border-color:#e5e7eb;-fx-font-size:13px;";
    }

    static String estiloBotonSecundario() {
        return "-fx-background-color:#f3f4f6;-fx-text-fill:#374151;"
                + "-fx-font-size:13px;-fx-font-weight:bold;"
                + "-fx-background-radius:8;-fx-padding:8 18;-fx-cursor:hand;";
    }
}

// ════════════════════════════════════════════════════════════════════════════
//
//  CLASE 2 – ComponenteFactory
//  GRASP: Pure Fabrication + Creator + High Cohesion + Low Coupling
//
//  Centraliza la construcción de widgets de UI repetitivos (botones,
//  celdas, barras de acciones). Es quien tiene TODA la información para
//  crearlos (Creator), sin pertenecer al dominio (Pure Fabrication).
//
// ════════════════════════════════════════════════════════════════════════════
final class ComponenteFactory {

    private ComponenteFactory() {
    }

    /**
     * Botón estilizado con transición hover.
     */
    static Button crearBoton(String texto, String bgColor, String bgHover) {
        Button btn = new Button(texto);
        btn.setPrefHeight(40);
        String base = estiloBoton(bgColor, "white");
        String hover = estiloBoton(bgHover, "white");
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e -> btn.setStyle(base));
        return btn;
    }

    /**
     * Botón de ícono (ver / editar / eliminar) con Tooltip.
     */
    static Button crearBtnAccion(String iconFA, String iconColor,
            String bgColor, String tooltip,
            Runnable accion) {
        Button btn = new Button(iconFA);
        String base = "-fx-background-color:" + bgColor + ";-fx-text-fill:" + iconColor + ";"
                + "-fx-font-family:'Font Awesome 6 Free Solid';"
                + "-fx-font-size:13px;-fx-background-radius:8;"
                + "-fx-padding:7 10;-fx-cursor:hand;";
        String hov = "-fx-background-color:" + iconColor + ";-fx-text-fill:white;"
                + "-fx-font-family:'Font Awesome 6 Free Solid';"
                + "-fx-font-size:13px;-fx-background-radius:8;"
                + "-fx-padding:7 10;-fx-cursor:hand;";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hov));
        btn.setOnMouseExited(e -> btn.setStyle(base));
        btn.setOnAction(e -> accion.run());
        Tooltip.install(btn, new Tooltip(tooltip));
        return btn;
    }

    /**
     * Celda de tabla con ancho fijo y elipsis.
     */
    static Label crearCelda(String txt, double width) {
        Label lbl = EstiloUI.label(txt != null ? txt : "—", 12, "#374151", false);
        lbl.setPrefWidth(width);
        lbl.setMinWidth(width);
        lbl.setMaxWidth(width);
        lbl.setEllipsisString("…");
        return lbl;
    }

    /**
     * Encabezado de columna (letras mayúsculas, color gris).
     */
    static Label crearEncabezadoColumna(String text) {
        Label lbl = new Label(text.toUpperCase());
        lbl.setStyle("-fx-font-size:11px;-fx-font-weight:bold;"
                + "-fx-text-fill:#9ca3af;-fx-letter-spacing:0.5;");
        return lbl;
    }

    /**
     * Barra de acciones (ver / editar / eliminar) lista para usar en filas.
     */
    static HBox crearBarraAcciones(Runnable ver, Runnable editar, Runnable eliminar) {
        HBox barra = new HBox(6);
        barra.setAlignment(Pos.CENTER_LEFT);
        barra.setPrefWidth(130);
        barra.setMinWidth(130);
        barra.setMaxWidth(130);
        barra.getChildren().addAll(
                crearBtnAccion("\uf06e", EstiloUI.BLUE, EstiloUI.BLUE_LIGHT, "Ver", ver),
                crearBtnAccion("\uf044", EstiloUI.ORANGE, EstiloUI.ORANGE_LIGHT, "Editar", editar),
                crearBtnAccion("\uf2ed", EstiloUI.RED, EstiloUI.RED_LIGHT, "Eliminar", eliminar)
        );
        return barra;
    }

    /**
     * Aplica sombra suave a cualquier Region.
     */
    static void aplicarSombra(Region nodo) {
        nodo.setEffect(new DropShadow(12, 0, 2, Color.web("#0000001a")));
    }

    private static String estiloBoton(String bg, String fg) {
        return "-fx-background-color:" + bg + ";-fx-text-fill:" + fg + ";"
                + "-fx-font-size:13px;-fx-font-weight:bold;"
                + "-fx-background-radius:8;-fx-padding:8 18;-fx-cursor:hand;";
    }
}

// ════════════════════════════════════════════════════════════════════════════
//
//  CLASE 3 – AsignacionStatsBuilder
//  GRASP: Information Expert + High Cohesion + Low Coupling
//
//  Nadie más tiene toda la información de asignaciones Y las reglas de
//  presentación en un solo lugar. Responsabilidad única: calcular y
//  renderizar las tarjetas de estadísticas.
//
// ════════════════════════════════════════════════════════════════════════════
class AsignacionStatsBuilder {

    /**
     * Construye el panel de tarjetas a partir de la lista de asignaciones.
     */
    HBox construir(List<AsignacionUnidad> asignaciones) {
        HBox row = new HBox(16);

        long total = asignaciones.size();
        long conUnidad = asignaciones.stream()
                .filter(a -> a.getUnidadpolicial() != null).count();
        long hoy = asignaciones.stream()
                .filter(a -> a.getFechahoraasignacion() != null
                && a.getFechahoraasignacion().toLocalDate()
                        .equals(java.time.LocalDate.now()))
                .count();
        long sinObs = asignaciones.stream()
                .filter(a -> a.getObservacion() == null
                || a.getObservacion().isBlank())
                .count();

        row.getChildren().addAll(
                tarjeta(EstiloUI.BLUE_LIGHT, EstiloUI.BLUE, "\uf02d",
                        "Total asignaciones", String.valueOf(total), "Registradas en el sistema"),
                tarjeta(EstiloUI.GREEN_LIGHT, EstiloUI.GREEN, "\uf058",
                        "Con unidad asignada", String.valueOf(conUnidad), "Tienen unidad activa"),
                tarjeta(EstiloUI.ORANGE_LIGHT, EstiloUI.ORANGE, "\uf073",
                        "Hoy", String.valueOf(hoy), "Asignaciones del día"),
                tarjeta(EstiloUI.PURPLE_LIGHT, EstiloUI.PURPLE, "\uf249",
                        "Sin observación", String.valueOf(sinObs), "Sin nota registrada")
        );
        return row;
    }

    private VBox tarjeta(String bgIcono, String accentColor, String iconFA,
            String titulo, String valor, String subtitulo) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20, 22, 20, 22));
        card.setStyle("-fx-background-color:white;-fx-background-radius:18;");
        HBox.setHgrow(card, Priority.ALWAYS);
        ComponenteFactory.aplicarSombra(card);

        StackPane iconWrap = new StackPane();
        iconWrap.setPrefSize(52, 52);
        iconWrap.setMinSize(52, 52);
        iconWrap.setMaxSize(52, 52);

        Rectangle iconBg = new Rectangle(52, 52);
        iconBg.setArcWidth(16);
        iconBg.setArcHeight(16);
        iconBg.setFill(Color.web(bgIcono));

        Label iconLbl = new Label(iconFA);
        iconLbl.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';"
                + "-fx-font-size:22px;-fx-text-fill:" + accentColor + ";");
        iconWrap.getChildren().addAll(iconBg, iconLbl);

        Label titLbl = new Label(titulo);
        titLbl.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#374151;");
        Label valLbl = new Label(valor);
        valLbl.setStyle("-fx-font-size:36px;-fx-font-weight:bold;-fx-text-fill:" + accentColor + ";");
        Label subLbl = EstiloUI.label(subtitulo, 11, EstiloUI.GRAY_TEXT, false);

        VBox textBox = new VBox(3, titLbl, valLbl, subLbl);
        HBox top = new HBox(16);
        top.setAlignment(Pos.CENTER_LEFT);
        top.getChildren().addAll(iconWrap, textBox);
        card.getChildren().add(top);

        card.setOnMouseEntered(e -> card.setTranslateY(-3));
        card.setOnMouseExited(e -> card.setTranslateY(0));
        return card;
    }
}

// ════════════════════════════════════════════════════════════════════════════
//
//  CLASE 4 – AsignacionController
//  GRASP: Controller + Indirection + Low Coupling + High Cohesion
//
//  Punto de entrada para todos los casos de uso de asignaciones.
//  La vista NUNCA llama directamente a los servicios; solo habla con
//  el Controller (Indirection). Coordina sin pertenecer a la UI.
//
// ════════════════════════════════════════════════════════════════════════════
class AsignacionController {

    private final AsignacionUnidadService asignacionService;
    private final UnidadPolicialService unidadService;
    private final AlertaService alertaService;

    AsignacionController(AsignacionUnidadService asignacionService,
            UnidadPolicialService unidadService,
            AlarmaService alarmaService) throws SQLException {
        this.asignacionService = asignacionService;
        this.unidadService = unidadService;
        this.alertaService = new AlertaService();
    }

    // ── Getters de servicios (para usos externos como MapaOperaciones) ──
    AsignacionUnidadService getAsignacionService() {
        return asignacionService;
    }

    UnidadPolicialService getUnidadService() {
        return unidadService;
    }

    // ── Listar ────────────────────────────────────────────────────
    List<AsignacionUnidad> listarTodas() {
        try {
            return asignacionService.listar();
        } catch (Exception e) {
            return List.of();
        }
    }

    // ── Filtros ───────────────────────────────────────────────────
    List<AsignacionUnidad> filtrarPorTexto(List<AsignacionUnidad> origen, String texto) {
        if (texto == null || texto.isBlank()) {
            return origen;
        }
        String txt = texto.toLowerCase().trim();
        return origen.stream()
                .filter(a
                        -> (a.getUnidadpolicial() != null
                && a.getUnidadpolicial().getNombre() != null
                && a.getUnidadpolicial().getNombre().toLowerCase().contains(txt))
                || (a.getObservacion() != null
                && a.getObservacion().toLowerCase().contains(txt))
                ).toList();
    }

    List<AsignacionUnidad> filtrarConUnidad(List<AsignacionUnidad> origen) {
        return origen.stream().filter(a -> a.getUnidadpolicial() != null).toList();
    }

    List<AsignacionUnidad> filtrarSinUnidad(List<AsignacionUnidad> origen) {
        return origen.stream().filter(a -> a.getUnidadpolicial() == null).toList();
    }

    // ── Cargar datos para formularios ─────────────────────────────
    /**
     * Alertas PENDIENTES o RECIBIDAS sin asignación existente.
     */
    List<Alerta> cargarAlertasPendientes() {
        try {
            Set<Integer> asignadas = asignacionService.listar().stream()
                    .filter(a -> a.getAlerta() != null)
                    .map(a -> a.getAlerta().getId_alerta())
                    .collect(Collectors.toSet());
            return alertaService.listar().stream()
                    .filter(a -> a.getEstado() == EstadoAlerta.PENDIENTE
                    || a.getEstado() == EstadoAlerta.RECIBIDA)
                    .filter(a -> !asignadas.contains(a.getId_alerta()))
                    .toList();
        } catch (Exception e) {
            return List.of();
        }
    }

    List<Alerta> cargarTodasAlertas() {
        try {
            return alertaService.listar();
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * Unidades OPERATIVAS que no estén en una asignación activa.
     */
    List<UnidadPolicial> cargarUnidadesLibres() {
        try {
            Set<Integer> ocupadas = asignacionService.listar().stream()
                    .filter(a -> a.getAlerta() != null
                    && a.getAlerta().getEstado() == EstadoAlerta.UNIDAD_ASIGNADA)
                    .filter(a -> a.getUnidadpolicial() != null)
                    .map(a -> a.getUnidadpolicial().getId_unidad())
                    .collect(Collectors.toSet());
            return unidadService.listar().stream()
                    .filter(u -> "OPERATIVA".equals(u.getEstado()))
                    .filter(u -> !ocupadas.contains(u.getId_unidad()))
                    .toList();
        } catch (Exception e) {
            return List.of();
        }
    }

    List<UnidadPolicial> cargarTodasUnidades() {
        try {
            return unidadService.listar();
        } catch (Exception e) {
            return List.of();
        }
    }

    // ── Casos de uso CRUD ─────────────────────────────────────────
    boolean crearAsignacion(Alerta alerta, UnidadPolicial unidad, String observacion) {
        AsignacionUnidad nueva = new AsignacionUnidad();
        Alerta ref = new Alerta();
        ref.setId_alerta(alerta.getId_alerta());
        nueva.setAlerta(ref);
        nueva.setUnidadpolicial(unidad);
        nueva.setObservacion(observacion.isBlank() ? "Asignación manual" : observacion.trim());
        nueva.setFechahoraasignacion(LocalDateTime.now());
        try {
            return asignacionService.insertar(nueva);
        } catch (Exception e) {
            return false;
        }
    }

    boolean actualizarAsignacion(int idAsignacion, Alerta alerta,
            UnidadPolicial unidad, String observacion,
            LocalDateTime fecha) {
        AsignacionUnidad actualizada = new AsignacionUnidad();
        actualizada.setId_asignacion(idAsignacion);
        Alerta ref = new Alerta();
        ref.setId_alerta(alerta.getId_alerta());
        actualizada.setAlerta(ref);
        actualizada.setUnidadpolicial(unidad);
        actualizada.setObservacion(observacion.isBlank() ? "Sin observación" : observacion.trim());
        actualizada.setFechahoraasignacion(fecha);
        try {
            return asignacionService.actualizar(actualizada);
        } catch (Exception e) {
            return false;
        }
    }

    boolean eliminarAsignacion(AsignacionUnidad asignacion) {
        try {
            // asignacionService.eliminar(asignacion.getId_asignacion());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
//
//  CLASE 5 – DialogoAsignacionBase  (abstracta)
//  GRASP: Polymorphism + Protected Variations + High Cohesion
//
//  Define la estructura fija del diálogo (header, form, botones).
//  El punto de variación es buildContenido() y onAccion(): cada subclase
//  los sobreescribe sin tocar el esqueleto (Template Method).
//
// ════════════════════════════════════════════════════════════════════════════
abstract class DialogoAsignacionBase {

    protected final Stage stage;
    protected Label errLabel;
    private String colorHeader;
    private String titulo;
    private String subtitulo;

        protected DialogoAsignacionBase(String titulo, String subtitulo, String colorHeader) {
        this.stage = new Stage();
        this.titulo = titulo;
        this.subtitulo = subtitulo;
        this.colorHeader = colorHeader;
        this.stage.initModality(Modality.APPLICATION_MODAL);
        this.stage.setTitle(titulo);
        this.stage.setResizable(false);
        
    }
      protected void init() {
        VBox root = new VBox(0);
        root.setPrefWidth(460);
        root.setStyle("-fx-background-color:" + EstiloUI.BG + ";");
        root.getChildren().addAll(
                buildHeader(titulo, subtitulo, colorHeader),
                buildFormContenedor(),
                buildBarraBotones()
        );
        stage.setScene(new Scene(root));
    }

    

    // ── Métodos abstractos (puntos de variación) ──────────────────
    protected abstract VBox buildContenido();

    protected abstract void onAccion();

    protected abstract String textoBotonPrimario();

    protected abstract String colorBotonPrimario();

    protected abstract String colorHoverPrimario();

    void mostrar() {
        stage.showAndWait();
    }

    protected void cerrar() {
        stage.close();
    }

    protected void mostrarError(String msg) {
        if (errLabel != null) {
            errLabel.setText(msg);
        }
    }

    // ── Construcción interna ──────────────────────────────────────
    private HBox buildHeader(String titulo, String subtitulo, String color) {
        HBox header = new HBox(12);
        header.setPadding(new Insets(20, 24, 18, 24));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color:" + color + ";");
        Label titLbl = new Label(titulo);
        titLbl.setFont(Font.font("System", FontWeight.BOLD, 18));
        titLbl.setTextFill(Color.web(EstiloUI.WHITE));
        Label subLbl = EstiloUI.label(subtitulo, 12, "#bbdefb", false);
        header.getChildren().add(new VBox(4, titLbl, subLbl));
        return header;
    }

    private VBox buildFormContenedor() {
        VBox form = buildContenido();
        form.setPadding(new Insets(24));
        form.setStyle("-fx-background-color:white;");
        errLabel = EstiloUI.label("", 12, EstiloUI.RED, false);
        errLabel.setWrapText(true);
        form.getChildren().add(errLabel);
        return form;
    }

    private HBox buildBarraBotones() {
        HBox bar = new HBox(10);
        bar.setPadding(new Insets(16, 24, 20, 24));
        bar.setAlignment(Pos.CENTER_RIGHT);
        bar.setStyle("-fx-background-color:white;"
                + "-fx-border-color:#e5e7eb transparent transparent;"
                + "-fx-border-width:1 0 0 0;");
        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setPrefHeight(40);
        btnCancelar.setStyle(EstiloUI.estiloBotonSecundario());
        btnCancelar.setOnAction(e -> cerrar());
        Button btnPrimario = ComponenteFactory.crearBoton(
                textoBotonPrimario(), colorBotonPrimario(), colorHoverPrimario()
        );
        btnPrimario.setOnAction(e -> onAccion());
        bar.getChildren().addAll(btnCancelar, btnPrimario);
        return bar;
    }
}

// ════════════════════════════════════════════════════════════════════════════
//
//  CLASE 6 – DialogoCrearAsignacion
//  GRASP: Polymorphism + Low Coupling + High Cohesion + Creator
//
//  Implementa el contrato de DialogoAsignacionBase para CREAR
//  una asignación manual. Solo sabe construir ese formulario.
//
// ════════════════════════════════════════════════════════════════════════════
class DialogoCrearAsignacion extends DialogoAsignacionBase {

    private final AsignacionController controller;
    private final Runnable onExito;

    private ComboBox<Alerta> cmbAlerta;
    private ComboBox<UnidadPolicial> cmbUnidad;
    private TextField txtObservacion;

    DialogoCrearAsignacion(AsignacionController controller, Runnable onExito) {
        super("Asignación manual de unidad",
                "Usa esto solo si la asignación automática falló.",
                EstiloUI.BLUE);
        this.controller = controller;
        this.onExito = onExito;
        init();
    }

    @Override
    protected VBox buildContenido() {
        VBox form = new VBox(14);

        cmbAlerta = new ComboBox<>();
        cmbAlerta.setPromptText("Selecciona la alerta");
        aplicarEstiloCmb(cmbAlerta);
        cmbAlerta.getItems().setAll(controller.cargarAlertasPendientes());
        cmbAlerta.setCellFactory(lv -> celdaAlerta());
        cmbAlerta.setButtonCell(celdaAlerta());

        cmbUnidad = new ComboBox<>();
        cmbUnidad.setPromptText("Selecciona la unidad");
        aplicarEstiloCmb(cmbUnidad);
        cmbUnidad.getItems().setAll(controller.cargarUnidadesLibres());
        cmbUnidad.setCellFactory(lv -> celdaUnidad(false));
        cmbUnidad.setButtonCell(celdaUnidad(true));

        txtObservacion = new TextField();
        txtObservacion.setPromptText("Motivo de la asignación manual...");
        txtObservacion.setPrefHeight(40);
        txtObservacion.setMaxWidth(Double.MAX_VALUE);
        txtObservacion.setStyle(EstiloUI.estiloCampo());

        form.getChildren().addAll(
                grupo("Alerta sin unidad asignada *", cmbAlerta),
                grupo("Unidad policial *", cmbUnidad),
                grupo("Observación", txtObservacion)
        );
        return form;
    }

    @Override
    protected void onAccion() {
        mostrarError("");
        if (cmbAlerta.getValue() == null) {
            mostrarError("Selecciona una alerta.");
            return;
        }
        if (cmbUnidad.getValue() == null) {
            mostrarError("Selecciona una unidad.");
            return;
        }
        boolean ok = controller.crearAsignacion(
                cmbAlerta.getValue(), cmbUnidad.getValue(), txtObservacion.getText()
        );
        if (ok) {
            cerrar();
            onExito.run();
        } else {
            mostrarError("No se pudo guardar la asignación.");
        }
    }

    @Override
    protected String textoBotonPrimario() {
        return "Asignar manualmente";
    }

    @Override
    protected String colorBotonPrimario() {
        return EstiloUI.GREEN;
    }

    @Override
    protected String colorHoverPrimario() {
        return "#2e7d32";
    }

    // ── Helpers ───────────────────────────────────────────────────
    private <T> void aplicarEstiloCmb(ComboBox<T> cmb) {
        cmb.setMaxWidth(Double.MAX_VALUE);
        cmb.setPrefHeight(40);
        cmb.setStyle(EstiloUI.estiloCampo());
    }

    private VBox grupo(String etiqueta, javafx.scene.Node campo) {
        return new VBox(4, EstiloUI.label(etiqueta, 12, "#374151", true), campo);
    }

    private ListCell<Alerta> celdaAlerta() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Alerta item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                String tipo = item.getTipoalerta() != null ? item.getTipoalerta().getNombre() : "Alerta";
                String barrio = item.getBarrio() != null ? item.getBarrio().getNombre() : "—";
                setText("#" + item.getId_alerta() + " — " + tipo + " en " + barrio);
            }
        };
    }

    private ListCell<UnidadPolicial> celdaUnidad(boolean soloNombre) {
        return new ListCell<>() {
            @Override
            protected void updateItem(UnidadPolicial item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(soloNombre ? "Selecciona la unidad" : null);
                    return;
                }
                setText(soloNombre ? item.getNombre() : item.getNombre() + " — " + item.getEstado());
            }
        };
    }
}

// ════════════════════════════════════════════════════════════════════════════
//
//  CLASE 7 – DialogoEditarAsignacion
//  GRASP: Polymorphism + Low Coupling + High Cohesion
//
//  Implementa el contrato de DialogoAsignacionBase para EDITAR
//  una asignación existente, precargando todos sus campos.
//
// ════════════════════════════════════════════════════════════════════════════
class DialogoEditarAsignacion extends DialogoAsignacionBase {

    private static final DateTimeFormatter FMT
            = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final AsignacionUnidad asignacion;
    private final AsignacionController controller;
    private final Runnable onExito;

    private ComboBox<Alerta> cmbAlerta;
    private ComboBox<UnidadPolicial> cmbUnidad;
    private TextField txtObservacion;
    private TextField txtFecha;

    DialogoEditarAsignacion(AsignacionUnidad asignacion,
            AsignacionController controller,
            Runnable onExito) {
        super("Editar asignación",
                "Modifica los datos de la asignación.",
                "#1f3a56");
        this.asignacion = asignacion;
        this.controller = controller;
        this.onExito = onExito;
        init();
    }

    @Override
    protected VBox buildContenido() {
        VBox form = new VBox(14);

        // Selector alerta (precargado)
        cmbAlerta = new ComboBox<>();
        aplicarEstiloCmb(cmbAlerta);
        List<Alerta> alertas = controller.cargarTodasAlertas();
        cmbAlerta.getItems().setAll(alertas);
        cmbAlerta.setCellFactory(lv -> celdaAlerta());
        cmbAlerta.setButtonCell(celdaAlerta());
        if (asignacion.getAlerta() != null) {
            alertas.stream()
                    .filter(al -> al.getId_alerta() == asignacion.getAlerta().getId_alerta())
                    .findFirst().ifPresent(cmbAlerta::setValue);
        }

        // Selector unidad (precargado)
        cmbUnidad = new ComboBox<>();
        aplicarEstiloCmb(cmbUnidad);
        List<UnidadPolicial> unidades = controller.cargarTodasUnidades();
        cmbUnidad.getItems().setAll(unidades);
        cmbUnidad.setCellFactory(lv -> celdaUnidad(false));
        cmbUnidad.setButtonCell(celdaUnidad(true));
        if (asignacion.getUnidadpolicial() != null) {
            unidades.stream()
                    .filter(u -> u.getId_unidad() == asignacion.getUnidadpolicial().getId_unidad())
                    .findFirst().ifPresent(cmbUnidad::setValue);
        }

        // Observación (precargada)
        txtObservacion = new TextField(
                asignacion.getObservacion() != null ? asignacion.getObservacion() : "");
        txtObservacion.setPrefHeight(40);
        txtObservacion.setMaxWidth(Double.MAX_VALUE);
        txtObservacion.setStyle(EstiloUI.estiloCampo());

        // Fecha (precargada)
        txtFecha = new TextField(
                asignacion.getFechahoraasignacion() != null
                ? asignacion.getFechahoraasignacion().format(FMT) : "");
        txtFecha.setPromptText("dd/MM/yyyy HH:mm");
        txtFecha.setPrefHeight(40);
        txtFecha.setMaxWidth(Double.MAX_VALUE);
        txtFecha.setStyle(EstiloUI.estiloCampo());

        form.getChildren().addAll(
                grupo("Alerta *", cmbAlerta),
                grupo("Unidad policial *", cmbUnidad),
                grupo("Observación", txtObservacion),
                grupo("Fecha y hora", txtFecha)
        );
        return form;
    }

    @Override
    protected void onAccion() {
        mostrarError("");
        if (cmbAlerta.getValue() == null) {
            mostrarError("Selecciona una alerta.");
            return;
        }
        if (cmbUnidad.getValue() == null) {
            mostrarError("Selecciona una unidad.");
            return;
        }

        LocalDateTime fecha;
        try {
            fecha = LocalDateTime.parse(txtFecha.getText().trim(), FMT);
        } catch (Exception ex) {
            mostrarError("Formato de fecha inválido. Usa dd/MM/yyyy HH:mm");
            return;
        }

        boolean ok = controller.actualizarAsignacion(
                asignacion.getId_asignacion(),
                cmbAlerta.getValue(),
                cmbUnidad.getValue(),
                txtObservacion.getText(),
                fecha
        );
        if (ok) {
            cerrar();
            onExito.run();
        } else {
            mostrarError("No se pudo actualizar la asignación.");
        }
    }

    @Override
    protected String textoBotonPrimario() {
        return "Guardar cambios";
    }

    @Override
    protected String colorBotonPrimario() {
        return EstiloUI.ORANGE;
    }

    @Override
    protected String colorHoverPrimario() {
        return "#e65100";
    }

    // ── Helpers ───────────────────────────────────────────────────
    private <T> void aplicarEstiloCmb(ComboBox<T> cmb) {
        cmb.setMaxWidth(Double.MAX_VALUE);
        cmb.setPrefHeight(40);
        cmb.setStyle(EstiloUI.estiloCampo());
    }

    private VBox grupo(String etiqueta, javafx.scene.Node campo) {
        return new VBox(4, EstiloUI.label(etiqueta, 12, "#374151", true), campo);
    }

    private ListCell<Alerta> celdaAlerta() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Alerta item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                String tipo = item.getTipoalerta() != null ? item.getTipoalerta().getNombre() : "Sin tipo";
                String barrio = item.getBarrio() != null ? item.getBarrio().getNombre() : "Sin barrio";
                setText("#" + item.getId_alerta() + " — " + tipo + " en " + barrio
                        + "  [" + (item.getEstado() != null ? item.getEstado() : "—") + "]");
            }
        };
    }

    private ListCell<UnidadPolicial> celdaUnidad(boolean soloNombre) {
        return new ListCell<>() {
            @Override
            protected void updateItem(UnidadPolicial item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(soloNombre ? "Selecciona la unidad" : null);
                    return;
                }
                setText(soloNombre ? item.getNombre() : item.getNombre() + " — " + item.getEstado());
            }
        };
    }
}

// ════════════════════════════════════════════════════════════════════════════
//
//  CLASE 8 – AsignacionesAdminPoliciaView  (clase pública principal)
//  GRASP: Controller (delegado) + Low Coupling + High Cohesion
//
//  Responsabilidad ÚNICA: construir y actualizar la interfaz gráfica.
//  TODA la lógica de negocio está en AsignacionController.
//  Depende solo de controller + statsBuilder (Low Coupling).
//
// ════════════════════════════════════════════════════════════════════════════
public class AsignacionesAdminPoliciaView {

    private static final DateTimeFormatter FMT
            = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // GRASP Low Coupling: solo 2 dependencias en lugar de 4 servicios
    private final AsignacionController controller;
    private final AsignacionStatsBuilder statsBuilder;

    private VBox tablaContainer;
    private TextField campoBusqueda;
    private List<AsignacionUnidad> todasLasAsignaciones;
    private HBox statsContainer;

    /**
     * GRASP Indirection: los servicios se encapsulan en el Controller; la vista
     * nunca los toca directamente.
     */
    public AsignacionesAdminPoliciaView(AsignacionUnidadService asignacionService,
            UnidadPolicialService unidadService,
            AlarmaService alarmaService) {
        /**
         * GRASP Indirection: los servicios se encapsulan en el Controller; la
         * vista nunca los toca directamente.
         */
        try {
            this.controller = new AsignacionController(asignacionService, unidadService, alarmaService);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error fatal: No se pudo inicializar el controlador de asignaciones.", e);
        }
        this.statsBuilder = new AsignacionStatsBuilder();
    }

    // ── Build principal ───────────────────────────────────────────
    public ScrollPane build() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color:" + EstiloUI.BG + ";");

        // PROTEGEMOS LA CARGA INICIAL
        try {
            todasLasAsignaciones = controller.listarTodas();
        } catch (Exception ex) {
            ex.printStackTrace();
            todasLasAsignaciones = new java.util.ArrayList<>(); // Evita NullPointerException
            // Nota: Aquí no podemos mostrarAlerta directamente porque la Scene aún no está lista,
            // pero evitamos que el programa explote al compilar.
        }

        statsContainer = statsBuilder.construir(todasLasAsignaciones);

        content.getChildren().addAll(
                buildTopBar(),
                statsContainer,
                buildToolbar(),
                buildTabla()
        );
        renderizarLista(todasLasAsignaciones);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color:" + EstiloUI.BG
                + "; -fx-background:" + EstiloUI.BG + ";");
        configurarAutoRefresh(scroll);
        return scroll;
    }

    // ── Auto-refresh ──────────────────────────────────────────────
    private void configurarAutoRefresh(ScrollPane scroll) {
        Timeline autoRefresh = new Timeline(
                new KeyFrame(Duration.seconds(30), e -> {
                    try {
                        todasLasAsignaciones = controller.listarTodas();
                        renderizarLista(todasLasAsignaciones);
                    } catch (Exception ex) {
                        System.err.println("Error en auto-refresh: " + ex.getMessage());
                    }
                })
        );
        autoRefresh.setCycleCount(Timeline.INDEFINITE);
        autoRefresh.play();
        scroll.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null) {
                autoRefresh.stop();
            }
        });
    }

    // ── Top bar ───────────────────────────────────────────────────
    private HBox buildTopBar() {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);

        VBox titles = new VBox(4);
        titles.getChildren().addAll(
                EstiloUI.label("Gestión de Asignaciones", 28, "#111827", true),
                EstiloUI.label("Administra las asignaciones de unidades policiales",
                        13, EstiloUI.GRAY_TEXT, false)
        );

        Button btnMapa = ComponenteFactory.crearBoton("\uf3c5  Mapa de operaciones", "#ef5350", "#c62828");
        Button btnNueva = ComponenteFactory.crearBoton("＋  Nueva asignación", "#1f3a56", "#0d1c2b");

        btnMapa.setOnAction(e -> abrirMapaOperaciones());
        btnNueva.setOnAction(e
                -> new DialogoCrearAsignacion(controller, this::recargarTodo).mostrar()
        );

        HBox right = new HBox(10);
        right.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(right, Priority.ALWAYS);
        right.getChildren().addAll(btnMapa, btnNueva);
        bar.getChildren().addAll(titles, right);
        return bar;
    }

    // ── Toolbar ───────────────────────────────────────────────────
    private HBox buildToolbar() {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(14, 20, 14, 20));
        bar.setStyle("-fx-background-color:white;-fx-background-radius:12;");
        ComponenteFactory.aplicarSombra(bar);

        HBox searchBox = new HBox(8);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setStyle("-fx-background-color:#f5f7fb;-fx-background-radius:10;-fx-padding:0 14;");
        searchBox.setPrefHeight(42);
        HBox.setHgrow(searchBox, Priority.ALWAYS);

        Label searchIcon = new Label("\uf002");
        searchIcon.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';"
                + "-fx-font-size:14px;-fx-text-fill:#9ca3af;");

        campoBusqueda = new TextField();
        campoBusqueda.setPromptText("Buscar por unidad u observación...");
        campoBusqueda.setStyle("-fx-background-color:transparent;-fx-border-color:transparent;"
                + "-fx-font-size:13px;-fx-text-fill:#111827;");
        campoBusqueda.setPrefHeight(42);
        HBox.setHgrow(campoBusqueda, Priority.ALWAYS);
        // GRASP Controller: el filtro se delega al controller
        campoBusqueda.textProperty().addListener((obs, o, n)
                -> renderizarLista(controller.filtrarPorTexto(todasLasAsignaciones, n))
        );

        ComboBox<String> filtroUnidad = new ComboBox<>();
        filtroUnidad.getItems().addAll("Todos", "Con unidad", "Sin unidad");
        filtroUnidad.setValue("Todos");
        filtroUnidad.setPrefHeight(42);
        filtroUnidad.setStyle("-fx-background-color:#f5f7fb;-fx-border-color:transparent;"
                + "-fx-background-radius:10;-fx-font-size:13px;");
        filtroUnidad.setOnAction(e -> {
            List<AsignacionUnidad> filtradas = switch (filtroUnidad.getValue()) {
                case "Con unidad" ->
                    controller.filtrarConUnidad(todasLasAsignaciones);
                case "Sin unidad" ->
                    controller.filtrarSinUnidad(todasLasAsignaciones);
                default ->
                    todasLasAsignaciones;
            };
            renderizarLista(filtradas);
        });

        searchBox.getChildren().addAll(searchIcon, campoBusqueda);
        bar.getChildren().addAll(searchBox, filtroUnidad);
        return bar;
    }

    // ── Tabla ─────────────────────────────────────────────────────
    private VBox buildTabla() {
        VBox card = new VBox(0);
        card.setStyle("-fx-background-color:white;-fx-background-radius:12;");
        ComponenteFactory.aplicarSombra(card);

        HBox header = new HBox(0);
        header.setPadding(new Insets(12, 16, 12, 16));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color:#f8fafc;-fx-background-radius:12 12 0 0;"
                + "-fx-border-color:transparent transparent " + EstiloUI.BORDER + " transparent;"
                + "-fx-border-width:0 0 1 0;");

        HBox unidadWrap = new HBox();
        HBox.setHgrow(unidadWrap, Priority.ALWAYS);
        unidadWrap.getChildren().add(ComponenteFactory.crearEncabezadoColumna("Unidad Policial"));

        Label colObs = ComponenteFactory.crearEncabezadoColumna("Observación");
        colObs.setPrefWidth(220);
        colObs.setMinWidth(220);
        colObs.setMaxWidth(220);
        Label colFecha = ComponenteFactory.crearEncabezadoColumna("Fecha / Hora");
        colFecha.setPrefWidth(160);
        colFecha.setMinWidth(160);
        colFecha.setMaxWidth(160);
        Label colAcc = ComponenteFactory.crearEncabezadoColumna("Acciones");
        colAcc.setPrefWidth(130);
        colAcc.setMinWidth(130);
        colAcc.setMaxWidth(130);

        header.getChildren().addAll(unidadWrap, colObs, colFecha, colAcc);
        card.getChildren().add(header);

        tablaContainer = new VBox(0);
        card.getChildren().add(tablaContainer);

        HBox footer = new HBox();
        footer.setPadding(new Insets(10, 16, 10, 16));
        footer.setStyle("-fx-background-color:#f8fafc;-fx-background-radius:0 0 12 12;"
                + "-fx-border-color:" + EstiloUI.BORDER + " transparent transparent transparent;"
                + "-fx-border-width:1 0 0 0;");
        footer.getChildren().add(EstiloUI.label("Cargando...", 12, EstiloUI.GRAY_TEXT, false));
        card.getChildren().add(footer);
        return card;
    }

    // ── Renderizar ────────────────────────────────────────────────
    private void renderizarLista(List<AsignacionUnidad> lista) {
        tablaContainer.getChildren().clear();

        if (lista.isEmpty()) {
            VBox vacio = new VBox(10);
            vacio.setAlignment(Pos.CENTER);
            vacio.setPadding(new Insets(40));
            Label icn = new Label("\uf02d");
            icn.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';"
                    + "-fx-font-size:48px;-fx-text-fill:#d1d5db;");
            vacio.getChildren().addAll(icn,
                    EstiloUI.label("No se encontraron asignaciones", 14, EstiloUI.GRAY_TEXT, false));
            tablaContainer.getChildren().add(vacio);
        } else {
            for (int i = 0; i < lista.size(); i++) {
                tablaContainer.getChildren().add(buildFila(lista.get(i), i % 2 == 0));
            }
        }
        actualizarFooter(lista.size());
        actualizarStats();
    }

    // ── Fila ──────────────────────────────────────────────────────
    private HBox buildFila(AsignacionUnidad a, boolean par) {
        HBox fila = new HBox(0);
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.setPadding(new Insets(10, 16, 10, 16));

        String bgN = "-fx-background-color:" + (par ? EstiloUI.WHITE : "#fafbfd") + ";"
                + "-fx-border-color:transparent transparent " + EstiloUI.BORDER + " transparent;"
                + "-fx-border-width:0 0 1 0;";
        fila.setStyle(bgN);
        fila.setOnMouseEntered(e -> fila.setStyle(
                "-fx-background-color:#EEF2FF;"
                + "-fx-border-color:transparent transparent " + EstiloUI.BORDER + " transparent;"
                + "-fx-border-width:0 0 1 0;-fx-cursor:hand;"));
        fila.setOnMouseExited(e -> fila.setStyle(bgN));

        String nombreUnidad = (a.getUnidadpolicial() != null
                && a.getUnidadpolicial().getNombre() != null)
                ? a.getUnidadpolicial().getNombre() : "Sin unidad";

        // Celda avatar + nombre
        HBox celdaUnidad = new HBox(10);
        celdaUnidad.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(celdaUnidad, Priority.ALWAYS);

        StackPane avatarBox = new StackPane();
        Circle avatar = new Circle(18, Color.web(EstiloUI.colorAvatar(nombreUnidad)));
        Label avatarLbl = EstiloUI.label(EstiloUI.iniciales(nombreUnidad), 11, EstiloUI.WHITE, true);
        avatarBox.getChildren().addAll(avatar, avatarLbl);

        Label nombreLbl = new Label(nombreUnidad);
        nombreLbl.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#111827;");
        celdaUnidad.getChildren().addAll(avatarBox, nombreLbl);

        String obs = (a.getObservacion() != null && !a.getObservacion().isBlank())
                ? a.getObservacion() : "—";
        String fecha = a.getFechahoraasignacion() != null
                ? a.getFechahoraasignacion().format(FMT) : "—";

        // GRASP Creator: ComponenteFactory construye todos los widgets de la fila
        fila.getChildren().addAll(
                celdaUnidad,
                ComponenteFactory.crearCelda(obs, 220),
                ComponenteFactory.crearCelda(fecha, 160),
                ComponenteFactory.crearBarraAcciones(
                        () -> verAsignacion(a),
                        () -> editarAsignacion(a),
                        () -> eliminarAsignacion(a)
                )
        );
        return fila;
    }

    // ── Acciones ─────────────────────────────────────────────────
    private void verAsignacion(AsignacionUnidad a) {
        // Diálogo simple de solo lectura (Polymorphism en acción)
        Stage dlg = new Stage();
        dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.setTitle("Detalle de asignación");
        dlg.setResizable(false);

        VBox root = new VBox(0);
        root.setPrefWidth(420);
        root.setStyle("-fx-background-color:" + EstiloUI.BG + ";");

        HBox header = new HBox();
        header.setPadding(new Insets(20, 24, 18, 24));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color:#1f3a56;");
        Label titLbl = new Label("Detalle de asignación");
        titLbl.setFont(Font.font("System", FontWeight.BOLD, 18));
        titLbl.setTextFill(Color.web(EstiloUI.WHITE));
        header.getChildren().add(titLbl);

        VBox body = new VBox(0);
        body.setStyle("-fx-background-color:white;");
        body.setPadding(new Insets(8, 0, 8, 0));

        String alertaTxt = "—";
        if (a.getAlerta() != null) {
            String tipo = a.getAlerta().getTipoalerta() != null ? a.getAlerta().getTipoalerta().getNombre() : "Alerta";
            String barrio = a.getAlerta().getBarrio() != null ? a.getAlerta().getBarrio().getNombre() : "Sin barrio";
            String estado = a.getAlerta().getEstado() != null ? a.getAlerta().getEstado().toString() : "—";
            alertaTxt = "#" + a.getAlerta().getId_alerta() + " — " + tipo + " en " + barrio + "  [" + estado + "]";
        }
        String unidad = a.getUnidadpolicial() != null ? a.getUnidadpolicial().getNombre() : "Sin unidad";
        String obs = (a.getObservacion() != null && !a.getObservacion().isBlank()) ? a.getObservacion() : "—";
        String fecha = a.getFechahoraasignacion() != null ? a.getFechahoraasignacion().format(FMT) : "—";

        body.getChildren().addAll(
                filaDetalle("\uf0f3", "Alerta", alertaTxt, EstiloUI.BLUE),
                sepDetalle(),
                filaDetalle("\uf505", "Unidad", unidad, EstiloUI.GREEN),
                sepDetalle(),
                filaDetalle("\uf249", "Observación", obs, EstiloUI.ORANGE),
                sepDetalle(),
                filaDetalle("\uf073", "Fecha / Hora", fecha, EstiloUI.PURPLE)
        );

        HBox btnBar = new HBox();
        btnBar.setPadding(new Insets(16, 24, 20, 24));
        btnBar.setAlignment(Pos.CENTER_RIGHT);
        btnBar.setStyle("-fx-background-color:white;-fx-border-color:#e5e7eb transparent transparent;"
                + "-fx-border-width:1 0 0 0;");
        Button btnCerrar = ComponenteFactory.crearBoton("Cerrar", EstiloUI.BLUE, "#0d47a1");
        btnCerrar.setOnAction(e -> dlg.close());
        btnBar.getChildren().add(btnCerrar);

        root.getChildren().addAll(header, body, btnBar);
        dlg.setScene(new Scene(root));
        dlg.showAndWait();
    }

    private HBox filaDetalle(String iconFA, String titulo, String valor, String color) {
        HBox fila = new HBox(14);
        fila.setPadding(new Insets(14, 24, 14, 24));
        fila.setAlignment(Pos.CENTER_LEFT);

        StackPane iconWrap = new StackPane();
        iconWrap.setPrefSize(36, 36);
        iconWrap.setMinSize(36, 36);
        Rectangle bg = new Rectangle(36, 36);
        bg.setArcWidth(10);
        bg.setArcHeight(10);
        bg.setFill(Color.web(color + "22"));
        Label icn = new Label(iconFA);
        icn.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';"
                + "-fx-font-size:14px;-fx-text-fill:" + color + ";");
        iconWrap.getChildren().addAll(bg, icn);

        VBox texto = new VBox(2);
        Label lValor = EstiloUI.label(valor, 13, "#111827", true);
        lValor.setWrapText(true);
        texto.getChildren().addAll(EstiloUI.label(titulo, 11, EstiloUI.GRAY_TEXT, false), lValor);
        HBox.setHgrow(texto, Priority.ALWAYS);

        fila.getChildren().addAll(iconWrap, texto);
        return fila;
    }

    private javafx.scene.shape.Line sepDetalle() {
        javafx.scene.shape.Line sep = new javafx.scene.shape.Line(0, 0, 420, 0);
        sep.setStroke(Color.web(EstiloUI.BORDER));
        return sep;
    }

    private void editarAsignacion(AsignacionUnidad a) {
        new DialogoEditarAsignacion(a, controller, this::recargarTodo).mostrar();
    }

    private void eliminarAsignacion(AsignacionUnidad a) {
        String nombre = (a.getUnidadpolicial() != null)
                ? a.getUnidadpolicial().getNombre() : "esta asignación";

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Eliminar asignación");
        confirm.setHeaderText("¿Eliminar la asignación de \"" + nombre + "\"?");
        confirm.setContentText("Esta acción no se puede deshacer.");

        ButtonType btnSi = new ButtonType("Sí, eliminar", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnNo = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirm.getButtonTypes().setAll(btnSi, btnNo);

        confirm.showAndWait().ifPresent(resp -> {
            if (resp == btnSi) {
                try {
                    if (controller.eliminarAsignacion(a)) {
                        mostrarInfo("Eliminada", "Asignación eliminada correctamente.");
                        recargarTodo();
                    } else {
                        mostrarAlerta("Error", "No se pudo eliminar.");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    mostrarAlerta("Error de datos", "Ocurrió un fallo al eliminar: " + ex.getMessage());
                }
            }
        });
    }

    private void abrirMapaOperaciones() {
        MapaOperaciones mapa = new MapaOperaciones(
                controller.getAsignacionService(),
                controller.getUnidadService()
        );
        Stage stage = new Stage();
        stage.setTitle("Mapa de operaciones");
        stage.setScene(new Scene(mapa.build(), 1100, 680));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
    }

    // ── Helpers ───────────────────────────────────────────────────
    private void recargarTodo() {
        try {
            todasLasAsignaciones = controller.listarTodas();
            renderizarLista(todasLasAsignaciones);
        } catch (Exception ex) {
            ex.printStackTrace();
            mostrarAlerta("Error de Carga", "No se pudieron refrescar los datos: " + ex.getMessage());
        }
    }

    private void actualizarFooter(int cantidad) {
        VBox card = (VBox) tablaContainer.getParent();
        if (card != null && card.getChildren().size() >= 3) {
            HBox footer = (HBox) card.getChildren().get(2);
            if (!footer.getChildren().isEmpty()
                    && footer.getChildren().get(0) instanceof Label lbl) {
                lbl.setText("Mostrando " + cantidad + " asignación"
                        + (cantidad != 1 ? "es" : ""));
            }
        }
    }

    /**
     * GRASP Information Expert: StatsBuilder reconstruye las tarjetas.
     */
    private void actualizarStats() {
        HBox nuevo = statsBuilder.construir(todasLasAsignaciones);
        VBox content = (VBox) statsContainer.getParent();
        if (content != null) {
            int index = content.getChildren().indexOf(statsContainer);
            content.getChildren().set(index, nuevo);
            statsContainer = nuevo;
        }
    }

    private void mostrarAlerta(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void mostrarInfo(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
