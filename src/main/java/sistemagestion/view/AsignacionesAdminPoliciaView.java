/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.view;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;
import sistemagestion.model.*;
import sistemagestion.service.AlarmaService;
import sistemagestion.service.AlertaService;
import sistemagestion.service.AsignacionUnidadService;
import sistemagestion.service.UnidadPolicialService;

/**
 * Vista de administración de asignaciones de unidades, con el mismo estilo
 * visual que UnidadesAdminPoliciaView.
 */
public class AsignacionesAdminPoliciaView {

    // ── Paleta ────────────────────────────────────────────────────
    private static final String WHITE = "#ffffff";
    private static final String BG = "#f4f6fb";
    private static final String BLUE = "#1565c0";
    private static final String BLUE_LIGHT = "#e8f0fe";
    private static final String GREEN = "#43a047";
    private static final String GREEN_LIGHT = "#e8f5e9";
    private static final String RED = "#e53935";
    private static final String RED_LIGHT = "#fff0f0";
    private static final String ORANGE = "#fb8c00";
    private static final String ORANGE_LIGHT = "#fff8e1";
    private static final String PURPLE = "#7b1fa2";
    private static final String PURPLE_LIGHT = "#f3e5f5";
    private static final String GRAY_TEXT = "#6b7280";
    private static final String BORDER = "#e5e7eb";

    private static final DateTimeFormatter FMT
            = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final AsignacionUnidadService asignacionService;
    private AlarmaService alarmaService;
    private UnidadPolicialService unidadService;

    // Estado mutable
    private VBox tablaContainer;
    private TextField campoBusqueda;
    private List<AsignacionUnidad> todasLasAsignaciones;
    private HBox statsContainer;

    public AsignacionesAdminPoliciaView(AsignacionUnidadService asignacionService,
            UnidadPolicialService unidadService, AlarmaService alarmaService) {
        this.asignacionService = asignacionService;
        this.unidadService = unidadService;
        this.alarmaService = alarmaService;
    }

    // ── Build principal ───────────────────────────────────────────
    public ScrollPane build() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color:" + BG + ";");

        try {
            todasLasAsignaciones = asignacionService.listar();
        } catch (Exception e) {
            todasLasAsignaciones = List.of();
        }

        statsContainer = buildStats();

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
        scroll.setStyle("-fx-background-color:" + BG + "; -fx-background:" + BG + ";");
        Timeline autoRefresh = new Timeline(
                new KeyFrame(Duration.seconds(30), e -> {
                    try {
                        todasLasAsignaciones = asignacionService.listar();
                        renderizarLista(todasLasAsignaciones);
                    } catch (Exception ex) {
                        System.err.println("Error al refrescar: " + ex.getMessage());
                    }
                })
        );
        autoRefresh.setCycleCount(Timeline.INDEFINITE);
        autoRefresh.play();

        // Detener el timeline cuando el ScrollPane salga de escena
        scroll.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null) {
                autoRefresh.stop();
            }
        });

        return scroll;
    }

    // ── Top bar ───────────────────────────────────────────────────
    private HBox buildTopBar() {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);

        VBox titles = new VBox(4);
        Label title = new Label("Gestión de Asignaciones");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#111827"));
        Label sub = label("Administra las asignaciones de unidades policiales", 13, GRAY_TEXT, false);
        titles.getChildren().addAll(title, sub);

       Button btnMapa = mkBtn("\uf3c5  Mapa de operaciones", "#ef5350", "#c62828");
        btnMapa.setOnAction(e -> abrirMapaOperaciones());

      Button btnNueva = mkBtn("＋  Nueva asignación", "#1f3a56", "#0d1c2b");
        btnNueva.setOnAction(e -> abrirFormularioAsignacion());  // ← conectado

        HBox right = new HBox(10);
        right.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(right, Priority.ALWAYS);
        right.getChildren().addAll(btnMapa, btnNueva);

        bar.getChildren().addAll(titles, right);
        return bar;
    }

// ── Formulario de asignación ──────────────────────────────────────────────
    private void abrirFormularioAsignacion() {
        Stage dlg = new Stage();
        dlg.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dlg.setTitle("Asignación manual");
        dlg.setResizable(false);

        VBox root = new VBox(0);
        root.setPrefWidth(460);
        root.setStyle("-fx-background-color:" + BG + ";");

        // Header
        HBox header = new HBox(12);
        header.setPadding(new Insets(20, 24, 18, 24));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color:" + BLUE + ";");
        Label titleLbl = new Label("Asignación manual de unidad");
        titleLbl.setFont(Font.font("System", FontWeight.BOLD, 18));
        titleLbl.setTextFill(Color.web("#ffffff"));
        Label subLbl = label("Usa esto solo si la asignación automática falló.", 12, "#bbdefb", false);
        VBox headerText = new VBox(4, titleLbl, subLbl);
        header.getChildren().add(headerText);

        // Form
        VBox form = new VBox(14);
        form.setPadding(new Insets(24));
        form.setStyle("-fx-background-color:white;");

        // Selector alerta
        Label lblAlerta = label("Alerta sin unidad asignada *", 12, "#374151", true);
        ComboBox<Alerta> cmbAlerta = new ComboBox<>();
        cmbAlerta.setPromptText("Selecciona la alerta");
        cmbAlerta.setMaxWidth(Double.MAX_VALUE);
        cmbAlerta.setPrefHeight(40);
        cmbAlerta.setStyle("-fx-background-color:#f5f7fb;-fx-background-radius:8;"
                + "-fx-border-color:#e5e7eb;-fx-font-size:13px;");
        try {
            cmbAlerta.getItems().setAll(cargarAlertasPendientes());
        } catch (Exception ignored) {
        }
        cmbAlerta.setCellFactory(lv -> new javafx.scene.control.ListCell<Alerta>() {
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
        });
        cmbAlerta.setButtonCell(new javafx.scene.control.ListCell<Alerta>() {
            @Override
            protected void updateItem(Alerta item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Selecciona la alerta");
                    return;
                }
                String tipo = item.getTipoalerta() != null ? item.getTipoalerta().getNombre() : "Alerta";
                String barrio = item.getBarrio() != null ? item.getBarrio().getNombre() : "—";
                setText("#" + item.getId_alerta() + " — " + tipo + " en " + barrio);
            }
        });

        // Selector unidad
        Label lblUnidad = label("Unidad policial *", 12, "#374151", true);
        ComboBox<UnidadPolicial> cmbUnidad = new ComboBox<>();
        cmbUnidad.setPromptText("Selecciona la unidad");
        cmbUnidad.setMaxWidth(Double.MAX_VALUE);
        cmbUnidad.setPrefHeight(40);
        cmbUnidad.setStyle("-fx-background-color:#f5f7fb;-fx-background-radius:8;"
                + "-fx-border-color:#e5e7eb;-fx-font-size:13px;");
        try {
            List<UnidadPolicial> todasUnidades = unidadService.listar();
            List<AsignacionUnidad> asignacionesActivas = asignacionService.listar()
                    .stream()
                    .filter(a -> a.getAlerta() != null
                    && a.getAlerta().getEstado() == sistemagestion.model.EstadoAlerta.UNIDAD_ASIGNADA)
                    .toList();

            Set<Integer> unidadesOcupadas = asignacionesActivas.stream()
                    .filter(a -> a.getUnidadpolicial() != null)
                    .map(a -> a.getUnidadpolicial().getId_unidad())
                    .collect(java.util.stream.Collectors.toSet());

            List<UnidadPolicial> unidadesLibres = todasUnidades.stream()
                    .filter(u -> u.getEstado().equals("OPERATIVA"))
                    .filter(u -> !unidadesOcupadas.contains(u.getId_unidad()))
                    .toList();

            cmbUnidad.getItems().setAll(unidadesLibres);
        } catch (Exception ignored) {

        }
        cmbUnidad.setCellFactory(lv -> new javafx.scene.control.ListCell<UnidadPolicial>() {
            @Override
            protected void updateItem(UnidadPolicial item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNombre() + " — " + item.getEstado());
            }
        });
        cmbUnidad.setButtonCell(new javafx.scene.control.ListCell<UnidadPolicial>() {
            @Override
            protected void updateItem(UnidadPolicial item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Selecciona la unidad" : item.getNombre());
            }
        });

        // Observación
        Label lblObs = label("Observación", 12, "#374151", true);
        TextField txtObs = new TextField();
        txtObs.setPromptText("Motivo de la asignación manual...");
        txtObs.setPrefHeight(40);
        txtObs.setMaxWidth(Double.MAX_VALUE);
        txtObs.setStyle("-fx-background-color:#f5f7fb;-fx-background-radius:8;"
                + "-fx-border-color:#e5e7eb;-fx-font-size:13px;");

        Label errLbl = label("", 12, RED, false);
        errLbl.setWrapText(true);

        form.getChildren().addAll(
                new VBox(4, lblAlerta, cmbAlerta),
                new VBox(4, lblUnidad, cmbUnidad),
                new VBox(4, lblObs, txtObs),
                errLbl
        );

        // Botones
        HBox btnBar = new HBox(10);
        btnBar.setPadding(new Insets(16, 24, 20, 24));
        btnBar.setAlignment(Pos.CENTER_RIGHT);
        btnBar.setStyle("-fx-background-color:white;-fx-border-color:#e5e7eb "
                + "transparent transparent;-fx-border-width:1 0 0 0;");

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setPrefHeight(40);
        btnCancelar.setStyle("-fx-background-color:#f3f4f6;-fx-text-fill:#374151;"
                + "-fx-font-size:13px;-fx-font-weight:bold;"
                + "-fx-background-radius:8;-fx-padding:8 18;-fx-cursor:hand;");
        btnCancelar.setOnAction(e -> dlg.close());

        Button btnGuardar = mkBtn("Asignar manualmente", GREEN, "#2e7d32");
        btnGuardar.setOnAction(e -> {
            errLbl.setText("");
            Alerta alertaSel = cmbAlerta.getValue();
            UnidadPolicial unidadSel = cmbUnidad.getValue();

            if (alertaSel == null) {
                errLbl.setText("Selecciona una alerta.");
                return;
            }
            if (unidadSel == null) {
                errLbl.setText("Selecciona una unidad.");
                return;
            }

            try {
                AsignacionUnidad nueva = new AsignacionUnidad();
                Alerta a = new Alerta();
                a.setId_alerta(alertaSel.getId_alerta());
                nueva.setAlerta(a);
                nueva.setUnidadpolicial(unidadSel);
                nueva.setObservacion(txtObs.getText().isBlank()
                        ? "Asignación manual" : txtObs.getText().trim());
                nueva.setFechahoraasignacion(LocalDateTime.now());

                boolean ok = asignacionService.insertar(nueva);
                if (ok) {
                    todasLasAsignaciones = asignacionService.listar();
                    renderizarLista(todasLasAsignaciones);
                    dlg.close();
                    mostrarInfo("Asignación creada", "Unidad asignada manualmente.");
                } else {
                    errLbl.setText("No se pudo guardar la asignación.");
                }
            } catch (Exception ex) {
                errLbl.setText("Error: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        btnBar.getChildren().addAll(btnCancelar, btnGuardar);
        root.getChildren().addAll(header, form, btnBar);
        dlg.setScene(new javafx.scene.Scene(root));
        dlg.showAndWait();
    }
// ── Cargar alertas pendientes o sin unidad asignada ──────────────────────

    private List<Alerta> cargarAlertasPendientes() {
        try {
            AlertaService alertaService = new AlertaService();

            Set<Integer> alertasConAsignacion = asignacionService.listar()
                    .stream()
                    .filter(a -> a.getAlerta() != null)
                    .map(a -> a.getAlerta().getId_alerta())
                    .collect(java.util.stream.Collectors.toSet());

            return alertaService.listar().stream()
                    .filter(a -> a.getEstado() == sistemagestion.model.EstadoAlerta.PENDIENTE
                    || a.getEstado() == sistemagestion.model.EstadoAlerta.RECIBIDA)
                    .filter(a -> !alertasConAsignacion.contains(a.getId_alerta()))
                    .toList();
        } catch (Exception e) {
            return List.of();
        }
    }

    // ── Mapa de operaciones ───────────────────────────────────────
    private void abrirMapaOperaciones() {
        MapaOperaciones mapa = (unidadService != null)
                ? new MapaOperaciones(asignacionService, unidadService)
                : new MapaOperaciones(asignacionService);
        Stage stage = new Stage();
        stage.setTitle("Mapa de operaciones");
        stage.setScene(new javafx.scene.Scene(mapa.build(), 1100, 680));
        stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        stage.show();
    }

    private Button mkBtn(String texto, String bgColor, String bgHover) {
        Button b = new Button(texto);
        b.setPrefHeight(40);
        String base = "-fx-background-color:" + bgColor + ";-fx-text-fill:white;"
                + "-fx-font-size:13px;-fx-font-weight:bold;"
                + "-fx-background-radius:8;-fx-padding:8 18;-fx-cursor:hand;";
        String hover = "-fx-background-color:" + bgHover + ";-fx-text-fill:white;"
                + "-fx-font-size:13px;-fx-font-weight:bold;"
                + "-fx-background-radius:8;-fx-padding:8 18;-fx-cursor:hand;";
        b.setStyle(base);
        b.setOnMouseEntered(e -> b.setStyle(hover));
        b.setOnMouseExited(e -> b.setStyle(base));
        return b;
    }

    // ── Stats ─────────────────────────────────────────────────────
    private HBox buildStats() {
        HBox row = new HBox(16);

        long total = todasLasAsignaciones.size();

        long conUnidad = todasLasAsignaciones.stream()
                .filter(a -> a.getUnidadpolicial() != null).count();

        long hoy = todasLasAsignaciones.stream()
                .filter(a -> {
                    if (a.getFechahoraasignacion() == null) {
                        return false;
                    }
                    return a.getFechahoraasignacion().toLocalDate()
                            .equals(java.time.LocalDate.now());
                }).count();

        long sinObservacion = todasLasAsignaciones.stream()
                .filter(a -> a.getObservacion() == null
                || a.getObservacion().isBlank()).count();

        row.getChildren().addAll(
                statCard(BLUE_LIGHT, BLUE, "\uf02d", "Total asignaciones",
                        boldNum(String.valueOf(total), BLUE),
                        "Registradas en el sistema"),
                statCard(GREEN_LIGHT, GREEN, "\uf058", "Con unidad asignada",
                        boldNum(String.valueOf(conUnidad), GREEN),
                        "Tienen unidad activa"),
                statCard(ORANGE_LIGHT, ORANGE, "\uf073", "Hoy",
                        boldNum(String.valueOf(hoy), ORANGE),
                        "Asignaciones del día"),
                statCard(PURPLE_LIGHT, PURPLE, "\uf249", "Sin observación",
                        boldNum(String.valueOf(sinObservacion), PURPLE),
                        "Sin nota registrada")
        );
        return row;
    }

    private Label boldNum(String val, String color) {
        Label l = new Label(val);
        l.setStyle("-fx-font-size:36px;-fx-font-weight:bold;-fx-text-fill:" + color + ";");
        return l;
    }

    private VBox statCard(String bgIcon, String accentColor, String iconFA,
            String title, Label valueLabel, String sub) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20, 22, 20, 22));
        card.setStyle("-fx-background-color:white;-fx-background-radius:18;");
        HBox.setHgrow(card, Priority.ALWAYS);
        shadow(card);

        StackPane iconWrap = new StackPane();
        iconWrap.setPrefSize(52, 52);
        iconWrap.setMinSize(52, 52);
        iconWrap.setMaxSize(52, 52);

        javafx.scene.shape.Rectangle iconBg = new javafx.scene.shape.Rectangle(52, 52);
        iconBg.setArcWidth(16);
        iconBg.setArcHeight(16);
        iconBg.setFill(Color.web(bgIcon));

        Label iconLbl = new Label(iconFA);
        iconLbl.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';"
                + "-fx-font-size:22px;-fx-text-fill:" + accentColor + ";");
        iconWrap.getChildren().addAll(iconBg, iconLbl);

        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#374151;");
        Label subLbl = label(sub, 11, GRAY_TEXT, false);

        VBox textBox = new VBox(3, titleLbl, valueLabel, subLbl);
        HBox top = new HBox(16);
        top.setAlignment(Pos.CENTER_LEFT);
        top.getChildren().addAll(iconWrap, textBox);
        card.getChildren().add(top);

        card.setOnMouseEntered(e -> card.setTranslateY(-3));
        card.setOnMouseExited(e -> card.setTranslateY(0));
        return card;
    }

    // ── Toolbar ───────────────────────────────────────────────────
    private HBox buildToolbar() {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(14, 20, 14, 20));
        bar.setStyle("-fx-background-color:white;-fx-background-radius:12;");
        shadow(bar);

        // Buscador
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
        campoBusqueda.setPromptText("Buscar por unidad u observación...");
        campoBusqueda.setStyle("-fx-background-color:transparent;"
                + "-fx-border-color:transparent;"
                + "-fx-font-size:13px;-fx-text-fill:#111827;");
        campoBusqueda.setPrefHeight(42);
        HBox.setHgrow(campoBusqueda, Priority.ALWAYS);
        campoBusqueda.textProperty().addListener((obs, o, n) -> filtrarYMostrar());

        ComboBox<String> filtroUnidad = new ComboBox<>();
        filtroUnidad.getItems().addAll("Todos", "Con unidad", "Sin unidad");
        filtroUnidad.setValue("Todos");
        filtroUnidad.setPrefHeight(42);
        filtroUnidad.setStyle("-fx-background-color:#f5f7fb;"
                + "-fx-border-color:transparent;"
                + "-fx-background-radius:10;-fx-font-size:13px;");
        filtroUnidad.setOnAction(e -> {
            String val = filtroUnidad.getValue();
            List<AsignacionUnidad> filtradas = switch (val) {
                case "Con unidad" ->
                    todasLasAsignaciones.stream()
                    .filter(a -> a.getUnidadpolicial() != null).toList();
                case "Sin unidad" ->
                    todasLasAsignaciones.stream()
                    .filter(a -> a.getUnidadpolicial() == null).toList();
                case "Unidades libres" -> {
                    Set<Integer> ocupadas = todasLasAsignaciones.stream()
                            .filter(a -> a.getAlerta() != null
                            && "UNIDAD_ASIGNADA".equals(
                                    a.getAlerta().getEstado() != null
                                    ? a.getAlerta().getEstado().toString() : ""))
                            .filter(a -> a.getUnidadpolicial() != null)
                            .map(a -> a.getUnidadpolicial().getId_unidad())
                            .collect(java.util.stream.Collectors.toSet());
                    yield todasLasAsignaciones.stream()
                    .filter(a -> a.getUnidadpolicial() != null
                    && !ocupadas.contains(a.getUnidadpolicial().getId_unidad()))
                    .toList();
                }
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
        shadow(card);

        // Header de columnas
        HBox header = new HBox(0);
        header.setPadding(new Insets(12, 16, 12, 16));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color:#f8fafc;-fx-background-radius:12 12 0 0;"
                + "-fx-border-color:transparent transparent " + BORDER + " transparent;"
                + "-fx-border-width:0 0 1 0;");

        HBox unidadWrap = new HBox();
        HBox.setHgrow(unidadWrap, Priority.ALWAYS);
        unidadWrap.getChildren().add(colH("Unidad Policial", true));

        header.getChildren().addAll(
                unidadWrap,
                colHFixed("Observación", 220),
                colHFixed("Fecha / Hora", 160),
                colHFixed("Acciones", 130)
        );
        card.getChildren().add(header);

        tablaContainer = new VBox(0);
        card.getChildren().add(tablaContainer);

        // Footer con contador
        HBox footer = new HBox();
        footer.setPadding(new Insets(10, 16, 10, 16));
        footer.setStyle("-fx-background-color:#f8fafc;-fx-background-radius:0 0 12 12;"
                + "-fx-border-color:" + BORDER + " transparent transparent transparent;"
                + "-fx-border-width:1 0 0 0;");
        footer.getChildren().add(label("Cargando...", 12, GRAY_TEXT, false));
        card.getChildren().add(footer);

        return card;
    }

    private Label colH(String text, boolean grow) {
        Label l = new Label(text.toUpperCase());
        l.setStyle("-fx-font-size:11px;-fx-font-weight:bold;"
                + "-fx-text-fill:#9ca3af;-fx-letter-spacing:0.5;");
        if (grow) {
            HBox.setHgrow(l, Priority.ALWAYS);
        }
        return l;
    }

    private Label colHFixed(String text, double width) {
        Label l = colH(text, false);
        l.setPrefWidth(width);
        l.setMinWidth(width);
        l.setMaxWidth(width);
        return l;
    }

    // ── Filtrar ───────────────────────────────────────────────────
    private void filtrarYMostrar() {
        String txt = campoBusqueda.getText().toLowerCase().trim();
        if (txt.isEmpty()) {
            renderizarLista(todasLasAsignaciones);
            return;
        }
        List<AsignacionUnidad> filtradas = todasLasAsignaciones.stream()
                .filter(a
                        -> (a.getUnidadpolicial() != null
                && a.getUnidadpolicial().getNombre() != null
                && a.getUnidadpolicial().getNombre().toLowerCase().contains(txt))
                || (a.getObservacion() != null
                && a.getObservacion().toLowerCase().contains(txt))
                ).toList();
        renderizarLista(filtradas);
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
            Label msg = label("No se encontraron asignaciones", 14, GRAY_TEXT, false);
            vacio.getChildren().addAll(icn, msg);
            tablaContainer.getChildren().add(vacio);
        } else {
            for (int i = 0; i < lista.size(); i++) {
                tablaContainer.getChildren().add(buildFila(lista.get(i), i % 2 == 0));
            }
        }

        // Actualizar footer
        VBox card = (VBox) tablaContainer.getParent();
        if (card != null && card.getChildren().size() >= 3) {
            HBox footer = (HBox) card.getChildren().get(2);
            if (!footer.getChildren().isEmpty()
                    && footer.getChildren().get(0) instanceof Label lbl) {
                lbl.setText("Mostrando " + lista.size() + " asignación"
                        + (lista.size() != 1 ? "es" : ""));
            }
        }

        actualizarStats();
    }

    // ── Fila ──────────────────────────────────────────────────────
    private HBox buildFila(AsignacionUnidad a, boolean par) {
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
                + "-fx-border-width:0 0 1 0; -fx-cursor:hand;"));
        fila.setOnMouseExited(e -> fila.setStyle(bgN));

        String nombreUnidad = (a.getUnidadpolicial() != null
                && a.getUnidadpolicial().getNombre() != null)
                ? a.getUnidadpolicial().getNombre() : "Sin unidad";

        HBox celdaUnidad = new HBox(10);
        celdaUnidad.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(celdaUnidad, Priority.ALWAYS);

        StackPane avatarBox = new StackPane();
        Circle avatar = new Circle(18, Color.web(colorAvatar(nombreUnidad)));
        Label avatarLbl = label(iniciales(nombreUnidad), 11, WHITE, true);
        avatarBox.getChildren().addAll(avatar, avatarLbl);

        Label nombreLbl = new Label(nombreUnidad);
        nombreLbl.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#111827;");
        celdaUnidad.getChildren().addAll(avatarBox, nombreLbl);

        // ── Observación  ───────────────────────────────────
        String obs = (a.getObservacion() != null && !a.getObservacion().isBlank())
                ? a.getObservacion() : "—";
        Label obsLbl = celda(obs, 220);

        // ── Fecha / hora (160px) ──────────────────────────────────
        String fecha = a.getFechahoraasignacion() != null
                ? a.getFechahoraasignacion().format(FMT) : "—";
        Label fechaLbl = celda(fecha, 160);

        // ── Acciones (130px) ──────────────────────────────────────
        HBox acciones = new HBox(6);
        acciones.setAlignment(Pos.CENTER_LEFT);
        acciones.setPrefWidth(130);
        acciones.setMinWidth(130);
        acciones.setMaxWidth(130);
        acciones.getChildren().addAll(
                btnAccion("\uf06e", BLUE, BLUE_LIGHT, "Ver", () -> verAsignacion(a)),
                btnAccion("\uf044", ORANGE, ORANGE_LIGHT, "Editar", () -> editarAsignacion(a)),
                btnAccion("\uf2ed", RED, RED_LIGHT, "Eliminar", () -> eliminarAsignacion(a))
        );

        fila.getChildren().addAll(celdaUnidad, obsLbl, fechaLbl, acciones);
        return fila;
    }

    // ── Acciones ─────────────────────────────────────────────────
    private void verAsignacion(AsignacionUnidad a) {
        Stage dlg = new Stage();
        dlg.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dlg.setTitle("Detalle de asignación");
        dlg.setResizable(false);

        VBox root = new VBox(0);
        root.setPrefWidth(420);
        root.setStyle("-fx-background-color:" + BG + ";");

        // Header azul
        HBox header = new HBox();
        header.setPadding(new Insets(20, 24, 18, 24));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color:#1f3a56;");
        Label titleLbl = new Label("Detalle de asignación");
        titleLbl.setFont(Font.font("System", FontWeight.BOLD, 18));
        titleLbl.setTextFill(Color.web(WHITE));
        header.getChildren().add(titleLbl);

        // Cuerpo con filas de datos
        VBox body = new VBox(0);
        body.setStyle("-fx-background-color:white;");
        body.setPadding(new Insets(8, 0, 8, 0));

        String unidad = a.getUnidadpolicial() != null ? a.getUnidadpolicial().getNombre() : "Sin unidad";
        String obs = (a.getObservacion() != null && !a.getObservacion().isBlank()) ? a.getObservacion() : "—";
        String fecha = a.getFechahoraasignacion() != null ? a.getFechahoraasignacion().format(FMT) : "—";

        // Alerta
        String alertaTxt = "—";
        if (a.getAlerta() != null) {
            String tipo = a.getAlerta().getTipoalerta() != null ? a.getAlerta().getTipoalerta().getNombre() : "Alerta";
            String barrio = a.getAlerta().getBarrio() != null ? a.getAlerta().getBarrio().getNombre() : "Sin barrio";
            String estado = a.getAlerta().getEstado() != null ? a.getAlerta().getEstado().toString() : "—";
            alertaTxt = "#" + a.getAlerta().getId_alerta() + " — " + tipo + " en " + barrio + "  [" + estado + "]";
        }

        body.getChildren().addAll(
                filaDetalle("\uf0f3", "Alerta", alertaTxt, BLUE),
                separador(),
                filaDetalle("\uf505", "Unidad", unidad, GREEN),
                separador(),
                filaDetalle("\uf249", "Observación", obs, ORANGE),
                separador(),
                filaDetalle("\uf073", "Fecha / Hora", fecha, PURPLE)
        );

        // Botón cerrar
        HBox btnBar = new HBox();
        btnBar.setPadding(new Insets(16, 24, 20, 24));
        btnBar.setAlignment(Pos.CENTER_RIGHT);
        btnBar.setStyle("-fx-background-color:white;-fx-border-color:#e5e7eb "
                + "transparent transparent;-fx-border-width:1 0 0 0;");
        Button btnCerrar = mkBtn("Cerrar", BLUE, "#0d47a1");
        btnCerrar.setOnAction(e -> dlg.close());
        btnBar.getChildren().add(btnCerrar);

        root.getChildren().addAll(header, body, btnBar);
        dlg.setScene(new javafx.scene.Scene(root));
        dlg.showAndWait();
    }

// ── Helper filaDetalle ────────────────────────────────────────
    private HBox filaDetalle(String iconFA, String titulo, String valor, String color) {
        HBox fila = new HBox(14);
        fila.setPadding(new Insets(14, 24, 14, 24));
        fila.setAlignment(Pos.CENTER_LEFT);

        StackPane iconWrap = new StackPane();
        iconWrap.setPrefSize(36, 36);
        iconWrap.setMinSize(36, 36);
        javafx.scene.shape.Rectangle bg = new javafx.scene.shape.Rectangle(36, 36);
        bg.setArcWidth(10);
        bg.setArcHeight(10);
        bg.setFill(Color.web(color + "22")); // color suave de fondo
        Label icn = new Label(iconFA);
        icn.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';"
                + "-fx-font-size:14px;-fx-text-fill:" + color + ";");
        iconWrap.getChildren().addAll(bg, icn);

        VBox texto = new VBox(2);
        Label lTitulo = label(titulo, 11, GRAY_TEXT, false);
        Label lValor = label(valor, 13, "#111827", true);
        lValor.setWrapText(true);
        texto.getChildren().addAll(lTitulo, lValor);
        HBox.setHgrow(texto, Priority.ALWAYS);

        fila.getChildren().addAll(iconWrap, texto);
        return fila;
    }

    private javafx.scene.shape.Rectangle separador() {
        javafx.scene.shape.Rectangle sep = new javafx.scene.shape.Rectangle();
        sep.setHeight(1);
        sep.setFill(Color.web(BORDER));
        sep.widthProperty().bind(
                javafx.beans.binding.Bindings.createDoubleBinding(() -> 420.0)
        );
        return sep;
    }

    private void editarAsignacion(AsignacionUnidad a) {
        Stage dlg = new Stage();
        dlg.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dlg.setTitle("Editar asignación");
        dlg.setResizable(false);

        VBox root = new VBox(0);
        root.setPrefWidth(460);
        root.setStyle("-fx-background-color:" + BG + ";");

        // Header
        HBox header = new HBox(12);
        header.setPadding(new Insets(20, 24, 18, 24));
        header.setAlignment(Pos.CENTER_LEFT);
      header.setStyle("-fx-background-color:#1f3a56;");
        Label titleLbl = new Label("Editar asignación");
        titleLbl.setFont(Font.font("System", FontWeight.BOLD, 18));
        titleLbl.setTextFill(Color.web(WHITE));
      Label subLbl = label("Modifica los datos de la asignación.", 12, "#a8c0dd", false);
        VBox headerText = new VBox(4, titleLbl, subLbl);
        header.getChildren().add(headerText);

        VBox form = new VBox(14);
        form.setPadding(new Insets(24));
        form.setStyle("-fx-background-color:white;");

        // ── Alerta (precargada) ──────────────────────────────────
        Label lblAlerta = label("Alerta *", 12, "#374151", true);
        ComboBox<Alerta> cmbAlerta = new ComboBox<>();
        cmbAlerta.setMaxWidth(Double.MAX_VALUE);
        cmbAlerta.setPrefHeight(40);
        cmbAlerta.setStyle("-fx-background-color:#f5f7fb;-fx-background-radius:8;"
                + "-fx-border-color:#e5e7eb;-fx-font-size:13px;");
        try {
            List<Alerta> alertas = new AlertaService().listar(); // todas, no solo pendientes
            cmbAlerta.getItems().setAll(alertas);
            if (a.getAlerta() != null) {
                alertas.stream()
                        .filter(al -> al.getId_alerta() == a.getAlerta().getId_alerta())
                        .findFirst()
                        .ifPresent(cmbAlerta::setValue); // ← precarga la alerta actual
            }
        } catch (Exception ignored) {
        }

        cmbAlerta.setCellFactory(lv -> new javafx.scene.control.ListCell<>() {
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
        });
        cmbAlerta.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Alerta item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Selecciona la alerta");
                    return;
                }
                String tipo = item.getTipoalerta() != null ? item.getTipoalerta().getNombre() : "Sin tipo";
                String barrio = item.getBarrio() != null ? item.getBarrio().getNombre() : "Sin barrio";
                setText("#" + item.getId_alerta() + " — " + tipo + " en " + barrio);
            }
        });

        // ── Unidad (precargada) ──────────────────────────────────
        Label lblUnidad = label("Unidad policial *", 12, "#374151", true);
        ComboBox<UnidadPolicial> cmbUnidad = new ComboBox<>();
        cmbUnidad.setMaxWidth(Double.MAX_VALUE);
        cmbUnidad.setPrefHeight(40);
        cmbUnidad.setStyle("-fx-background-color:#f5f7fb;-fx-background-radius:8;"
                + "-fx-border-color:#e5e7eb;-fx-font-size:13px;");
        try {
            List<UnidadPolicial> unidades = unidadService.listar(); // todas
            cmbUnidad.getItems().setAll(unidades);
            if (a.getUnidadpolicial() != null) {
                unidades.stream()
                        .filter(u -> u.getId_unidad() == a.getUnidadpolicial().getId_unidad())
                        .findFirst()
                        .ifPresent(cmbUnidad::setValue); // ← precarga la unidad actual
            }
        } catch (Exception ignored) {
        }

        cmbUnidad.setCellFactory(lv -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(UnidadPolicial item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNombre() + " — " + item.getEstado());
            }
        });
        cmbUnidad.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(UnidadPolicial item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Selecciona la unidad" : item.getNombre());
            }
        });

        // ── Observación (precargada) ─────────────────────────────
        Label lblObs = label("Observación", 12, "#374151", true);
        TextField txtObs = new TextField(a.getObservacion() != null ? a.getObservacion() : "");
        txtObs.setPrefHeight(40);
        txtObs.setMaxWidth(Double.MAX_VALUE);
        txtObs.setStyle("-fx-background-color:#f5f7fb;-fx-background-radius:8;"
                + "-fx-border-color:#e5e7eb;-fx-font-size:13px;");

        // ── Fecha (precargada) ───────────────────────────────────
        Label lblFecha = label("Fecha y hora", 12, "#374151", true);
        TextField txtFecha = new TextField(
                a.getFechahoraasignacion() != null ? a.getFechahoraasignacion().format(FMT) : "");
        txtFecha.setPromptText("dd/MM/yyyy HH:mm");
        txtFecha.setPrefHeight(40);
        txtFecha.setMaxWidth(Double.MAX_VALUE);
        txtFecha.setStyle("-fx-background-color:#f5f7fb;-fx-background-radius:8;"
                + "-fx-border-color:#e5e7eb;-fx-font-size:13px;");

        Label errLbl = label("", 12, RED, false);
        errLbl.setWrapText(true);

        form.getChildren().addAll(
                new VBox(4, lblAlerta, cmbAlerta),
                new VBox(4, lblUnidad, cmbUnidad),
                new VBox(4, lblObs, txtObs),
                new VBox(4, lblFecha, txtFecha),
                errLbl
        );

        // Botones
        HBox btnBar = new HBox(10);
        btnBar.setPadding(new Insets(16, 24, 20, 24));
        btnBar.setAlignment(Pos.CENTER_RIGHT);
        btnBar.setStyle("-fx-background-color:white;-fx-border-color:#e5e7eb "
                + "transparent transparent;-fx-border-width:1 0 0 0;");

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setPrefHeight(40);
        btnCancelar.setStyle("-fx-background-color:#f3f4f6;-fx-text-fill:#374151;"
                + "-fx-font-size:13px;-fx-font-weight:bold;"
                + "-fx-background-radius:8;-fx-padding:8 18;-fx-cursor:hand;");
        btnCancelar.setOnAction(e -> dlg.close());

        Button btnGuardar = mkBtn("Guardar cambios", ORANGE, "#e65100");
        btnGuardar.setOnAction(e -> {
            errLbl.setText("");
            Alerta alertaSel = cmbAlerta.getValue();
            UnidadPolicial uni = cmbUnidad.getValue();

            if (alertaSel == null) {
                errLbl.setText("Selecciona una alerta.");
                return;
            }
            if (uni == null) {
                errLbl.setText("Selecciona una unidad.");
                return;
            }

            LocalDateTime fecha;
            try {
                fecha = LocalDateTime.parse(txtFecha.getText().trim(), FMT);
            } catch (Exception ex) {
                errLbl.setText("Formato de fecha inválido. Usa dd/MM/yyyy HH:mm");
                return;
            }

            try {
                AsignacionUnidad actualizada = new AsignacionUnidad();
                actualizada.setId_asignacion(a.getId_asignacion());
                Alerta al = new Alerta();
                al.setId_alerta(alertaSel.getId_alerta());
                actualizada.setAlerta(al);
                actualizada.setUnidadpolicial(uni);
                actualizada.setObservacion(txtObs.getText().isBlank()
                        ? "Sin observación" : txtObs.getText().trim());
                actualizada.setFechahoraasignacion(fecha);

                boolean ok = asignacionService.actualizar(actualizada);
                if (ok) {
                    todasLasAsignaciones = asignacionService.listar();
                    renderizarLista(todasLasAsignaciones);
                    dlg.close();
                    mostrarInfo("Actualizada", "Asignación actualizada correctamente.");
                } else {
                    errLbl.setText("No se pudo actualizar la asignación.");
                }
            } catch (Exception ex) {
                errLbl.setText("Error: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        btnBar.getChildren().addAll(btnCancelar, btnGuardar);
        root.getChildren().addAll(header, form, btnBar);
        dlg.setScene(new javafx.scene.Scene(root));
        dlg.showAndWait();
    }

    private void eliminarAsignacion(AsignacionUnidad a) {
        String nombreUnidad = (a.getUnidadpolicial() != null
                && a.getUnidadpolicial().getNombre() != null)
                ? a.getUnidadpolicial().getNombre() : "esta asignación";

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Eliminar asignación");
        confirm.setHeaderText("¿Eliminar la asignación de \"" + nombreUnidad + "\"?");
        confirm.setContentText("Esta acción no se puede deshacer.");

        ButtonType btnSi = new ButtonType("Sí, eliminar", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnNo = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirm.getButtonTypes().setAll(btnSi, btnNo);

        confirm.showAndWait().ifPresent(resp -> {
            if (resp == btnSi) {
                try {
                    // asignacionService.eliminar(a.getId()); // adaptar según tu API
                    todasLasAsignaciones = asignacionService.listar();
                    renderizarLista(todasLasAsignaciones);
                    mostrarInfo("Eliminada", "Asignación eliminada correctamente.");
                } catch (Exception ex) {
                    mostrarAlerta("Error", "No se pudo eliminar: " + ex.getMessage());
                }
            }
        });
    }

    // ── Helpers UI ────────────────────────────────────────────────
    private Label celda(String txt, double width) {
        Label l = label(txt != null ? txt : "—", 12, "#374151", false);
        l.setPrefWidth(width);
        l.setMinWidth(width);
        l.setMaxWidth(width);
        l.setEllipsisString("…");
        return l;
    }

    private Button btnAccion(String iconFA, String iconColor,
            String bgColor, String tooltip, Runnable accion) {
        Button b = new Button(iconFA);
        String base = "-fx-background-color:" + bgColor + ";-fx-text-fill:" + iconColor + ";"
                + "-fx-font-family:'Font Awesome 6 Free Solid';"
                + "-fx-font-size:13px;-fx-background-radius:8;"
                + "-fx-padding:7 10;-fx-cursor:hand;";
        String hov = "-fx-background-color:" + iconColor + ";-fx-text-fill:white;"
                + "-fx-font-family:'Font Awesome 6 Free Solid';"
                + "-fx-font-size:13px;-fx-background-radius:8;"
                + "-fx-padding:7 10;-fx-cursor:hand;";
        b.setStyle(base);
        b.setOnMouseEntered(e -> b.setStyle(hov));
        b.setOnMouseExited(e -> b.setStyle(base));
        b.setOnAction(e -> accion.run());
        Tooltip.install(b, new Tooltip(tooltip));
        return b;
    }

    private static final String[] AVATAR_COLORS = {
        "#1565c0", "#2e7d32", "#6a1b9a", "#c62828",
        "#e65100", "#00695c", "#283593", "#4e342e"
    };

    private String colorAvatar(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return AVATAR_COLORS[0];
        }
        return AVATAR_COLORS[Math.abs(nombre.hashCode()) % AVATAR_COLORS.length];
    }

    private String iniciales(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return "?";
        }
        String[] p = nombre.trim().split("\\s+");
        return p.length == 1
                ? p[0].substring(0, 1).toUpperCase()
                : (p[0].substring(0, 1) + p[1].substring(0, 1)).toUpperCase();
    }

    private Label label(String text, double size, String color, boolean bold) {
        Label lbl = new Label(text);
        lbl.setFont(bold ? Font.font("System", FontWeight.BOLD, size)
                : Font.font("System", size));
        lbl.setTextFill(Color.web(color));
        return lbl;
    }

    private void shadow(Region node) {
        node.setEffect(new DropShadow(12, 0, 2, Color.web("#0000001a")));
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

    private void actualizarStats() {
        HBox nuevo = buildStats();
        VBox content = (VBox) statsContainer.getParent();
        if (content != null) {
            int index = content.getChildren().indexOf(statsContainer);
            content.getChildren().set(index, nuevo);
            statsContainer = nuevo;
        }
    }

}