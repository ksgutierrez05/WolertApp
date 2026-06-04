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
import sistemagestion.model.MedioTransporte;
import sistemagestion.model.TipoAlerta;
import sistemagestion.model.TipoArma;
import sistemagestion.service.MedioTransporteService;
import sistemagestion.service.TipoAlertaService;
import sistemagestion.service.TipoArmaService;

public class TiposAdminView {

    // ── Colores — idénticos a ComunaAdminView/BarrioAdminView ────
    private static final String WHITE = "#ffffff";
    private static final String BG = "#f4f6fb";
    private static final String BLUE = "#1565c0";
    private static final String GREEN = "#43a047";
    private static final String RED = "#e53935";
    private static final String RED_LIGHT = "#fff0f0";
    private static final String ORANGE = "#fb8c00";
    private static final String PURPLE = "#7b1fa2";
    private static final String TEAL = "#00796b";
    private static final String GRAY_TEXT = "#6b7280";
    private static final String BORDER = "#e5e7eb";

    // ── Services ─────────────────────────────────────────────────
    private TipoAlertaService tipoAlertaService;
    private TipoArmaService tipoArmaService;
    private MedioTransporteService medioTransporteService;

    // ── Contenedores recargables ──────────────────────────────────
    private VBox tablaContainerAlerta;
    private VBox tablaContainerArma;
    private VBox tablaContainerMedio;
    private Label lblMostrandoAlerta;
    private Label lblMostrandoArma;
    private Label lblMostrandoMedio;

    // ── Pestaña activa (0 = alerta, 1 = arma, 2 = medio) ─────────
    private int pestanaActiva = 0;

    // ── Búsqueda activa por pestaña ───────────────────────────────
    private TextField campoBusquedaAlerta;
    private TextField campoBusquedaArma;
    private TextField campoBusquedaMedio;

    public TiposAdminView() {
        javafx.scene.text.Font.loadFont(
                getClass().getResourceAsStream("/fa-solid-900.ttf"), 20);
        try {
            tipoAlertaService = new TipoAlertaService();
            tipoArmaService = new TipoArmaService();
            medioTransporteService = new MedioTransporteService();
        } catch (SQLException e) {
            mostrarAlerta("Error de conexión", e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // PUNTO DE ENTRADA
    // ═══════════════════════════════════════════════════════════════
    public ScrollPane getView() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color: " + BG + ";");

        StackPane bodyArea = new StackPane();
        VBox.setVgrow(bodyArea, Priority.ALWAYS);

        content.getChildren().addAll(
                buildTopBar(bodyArea),
                buildStatsRow(),
                buildTabBar(bodyArea),
                bodyArea
        );

        bodyArea.getChildren().setAll(buildPanelAlerta());

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: " + BG + "; -fx-background: " + BG + ";");
        return scroll;
    }

    // ═══════════════════════════════════════════════════════════════
    // TOP BAR — título + botón dinámico según pestaña
    // ═══════════════════════════════════════════════════════════════
    private HBox buildTopBar(StackPane bodyArea) {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);

        VBox titles = new VBox(4);
        Label title = new Label("Tipos / Catálogos");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#111827"));
        Label sub = label("Administra tipos de alerta, armas y medios de transporte",
                13, GRAY_TEXT, false);
        titles.getChildren().addAll(title, sub);

        HBox right = new HBox(12);
        right.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(right, Priority.ALWAYS);

        Button btnNuevo = new Button("+ Nuevo tipo");
        btnNuevo.setStyle(btnPrimaryStyle());
        btnNuevo.setOnMouseEntered(e -> btnNuevo.setStyle(btnPrimaryHoverStyle()));
        btnNuevo.setOnMouseExited(e -> btnNuevo.setStyle(btnPrimaryStyle()));
        btnNuevo.setOnAction(e -> {
            switch (pestanaActiva) {
                case 0 ->
                    abrirFormularioAlerta(null);
                case 1 ->
                    abrirFormularioArma(null);
                case 2 ->
                    abrirFormularioMedio(null);
            }
        });

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

        int totalAlertas = cargarTiposAlerta().size();
        int totalArmas = cargarTiposArma().size();
        int totalMedios = cargarMedios().size();
        int totalCatalog = totalAlertas + totalArmas + totalMedios;

        Label lblAlertasVal = boldNum(String.valueOf(totalAlertas), RED);
        Label lblArmasVal = boldNum(String.valueOf(totalArmas), PURPLE);
        Label lblMediosVal = boldNum(String.valueOf(totalMedios), TEAL);
        Label lblCatalogVal = boldNum(String.valueOf(totalCatalog), GREEN);

        row.getChildren().addAll(
                statCard(RED_LIGHT, RED, "\uf0f3", "Tipos de alerta", lblAlertasVal, "Categorías activas"),
                statCard("#f3e8ff", PURPLE, "\uf6ff", "Tipos de arma", lblArmasVal, "Registradas en el sistema"),
                statCard("#e0f2fe", TEAL, "\uf1b9", "Medios de transporte", lblMediosVal, "Disponibles"),
                statCard("#e8f5e9", GREEN, "\uf466", "Total catálogos", lblCatalogVal, "Ítems en el sistema")
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
                + "-fx-font-size: 22px; -fx-text-fill: " + accentColor + ";");
        iconWrap.getChildren().addAll(iconBg, iconLbl);

        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #374151;");
        Label subLbl = new Label(sub);
        subLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: " + GRAY_TEXT + ";");

        HBox top = new HBox(16);
        top.setAlignment(Pos.CENTER_LEFT);
        top.getChildren().addAll(iconWrap, new VBox(3, titleLbl, valueLabel, subLbl));
        card.getChildren().add(top);

        card.setOnMouseEntered(e -> card.setTranslateY(-3));
        card.setOnMouseExited(e -> card.setTranslateY(0));
        return card;
    }

    // ═══════════════════════════════════════════════════════════════
    // BARRA DE PESTAÑAS
    // ═══════════════════════════════════════════════════════════════
    private HBox buildTabBar(StackPane bodyArea) {
        HBox bar = new HBox(0);
        bar.setStyle(
                "-fx-background-color: " + WHITE + ";"
                + "-fx-background-radius: 12 12 0 0;"
                + "-fx-border-color: " + BORDER + ";"
                + "-fx-border-width: 0 0 1 0;");

        Button[] tabs = {
            tabBtn("🔔   Tipos de alerta", 0),
            tabBtn("🔫   Tipos de arma", 1),
            tabBtn("🚗   Medios de transporte", 2)
        };

        aplicarEstiloTab(tabs[0], true);
        aplicarEstiloTab(tabs[1], false);
        aplicarEstiloTab(tabs[2], false);

        tabs[0].setOnAction(e -> {
            pestanaActiva = 0;
            actualizarTabs(tabs, 0);
            bodyArea.getChildren().setAll(buildPanelAlerta());
        });
        tabs[1].setOnAction(e -> {
            pestanaActiva = 1;
            actualizarTabs(tabs, 1);
            bodyArea.getChildren().setAll(buildPanelArma());
        });
        tabs[2].setOnAction(e -> {
            pestanaActiva = 2;
            actualizarTabs(tabs, 2);
            bodyArea.getChildren().setAll(buildPanelMedio());
        });

        bar.getChildren().addAll(tabs);
        return bar;
    }

    private Button tabBtn(String texto, int index) {
        Button btn = new Button(texto);
        btn.setPrefHeight(44);
        btn.setPadding(new Insets(0, 24, 0, 24));
        btn.setStyle(
                "-fx-font-family: 'Font Awesome 6 Free Solid', 'System';"
                + "-fx-font-size: 13px;");
        btn.setCursor(javafx.scene.Cursor.HAND);
        return btn;
    }

    private void actualizarTabs(Button[] tabs, int activo) {
        for (int i = 0; i < tabs.length; i++) {
            aplicarEstiloTab(tabs[i], i == activo);
        }
    }

    private void aplicarEstiloTab(Button btn, boolean activo) {
        btn.setStyle(activo
                ? "-fx-background-color: " + WHITE + "; -fx-text-fill: " + BLUE + ";"
                + "-fx-font-size: 13px; -fx-font-weight: bold;"
                + "-fx-border-color: transparent transparent " + BLUE + " transparent;"
                + "-fx-border-width: 0 0 2 0; -fx-background-radius: 0; -fx-cursor: hand;"
                : "-fx-background-color: " + WHITE + "; -fx-text-fill: " + GRAY_TEXT + ";"
                + "-fx-font-size: 13px; -fx-border-color: transparent;"
                + "-fx-background-radius: 0; -fx-cursor: hand;");
    }

    // ═══════════════════════════════════════════════════════════════
    // PANEL — TIPOS DE ALERTA
    // ═══════════════════════════════════════════════════════════════
    private VBox buildPanelAlerta() {
        VBox panel = new VBox(0);
        panel.setStyle("-fx-background-color: " + WHITE + "; -fx-background-radius: 0 0 12 12;");
        shadow(panel);

        panel.getChildren().addAll(
                buildToolbarPanel("Buscar tipo de alerta...", campoBusquedaAlerta,
                        tf -> {
                            campoBusquedaAlerta = tf;
                            tf.textProperty().addListener((o, ov, nv) -> filtrar(nv));
                        }),
                buildTablaAlerta()
        );
        return panel;
    }

    private VBox buildTablaAlerta() {
        VBox card = new VBox(0);

        HBox header = buildHeader();
        HBox hNombreWrap = new HBox();
        HBox.setHgrow(hNombreWrap, Priority.ALWAYS);
        hNombreWrap.getChildren().add(colHeader("Nombre / tipo", 0));
        header.getChildren().addAll(hNombreWrap, colHeaderFixed("Acciones", 120));
        card.getChildren().add(header);

        tablaContainerAlerta = new VBox(0);
        card.getChildren().add(tablaContainerAlerta);

        HBox footer = buildFooterBox();
        lblMostrandoAlerta = label("Cargando...", 12, GRAY_TEXT, false);
        footer.getChildren().add(lblMostrandoAlerta);
        card.getChildren().add(footer);

        renderTablaAlerta(cargarTiposAlerta());
        return card;
    }

    private void renderTablaAlerta(List<TipoAlerta> lista) {
        if (tablaContainerAlerta == null) {
            return;
        }
        tablaContainerAlerta.getChildren().clear();
        if (lista.isEmpty()) {
            Label v = label("No hay tipos de alerta registrados.", 14, GRAY_TEXT, false);
            VBox.setMargin(v, new Insets(30, 16, 30, 16));
            tablaContainerAlerta.getChildren().add(v);
        } else {
            for (int i = 0; i < lista.size(); i++) {
                tablaContainerAlerta.getChildren().add(buildFilaAlerta(lista.get(i), i % 2 == 0));
            }
        }
        if (lblMostrandoAlerta != null) {
            lblMostrandoAlerta.setText("Total: " + lista.size() + " tipos de alerta");
        }
    }

    private HBox buildFilaAlerta(TipoAlerta t, boolean par) {
        HBox fila = buildFilaBase(par);

        // Nombre crece
        HBox celdaNombre = new HBox(10);
        celdaNombre.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(celdaNombre, Priority.ALWAYS);

        String[] badge = badgeAlerta(t.getNombre());
        StackPane avatarBox = new StackPane();
        Circle avatar = new Circle(20, Color.web(badge[3]));
        Label avatarLbl = label(badge[0], 14, WHITE, false);
        avatarBox.getChildren().addAll(avatar, avatarLbl);

        VBox nombreBox = new VBox(2);
        Label nombreLbl = new Label(t.getNombre());
        nombreLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        Label badgeLbl = new Label(badge[1]);
        badgeLbl.setStyle(
                "-fx-background-color: " + badge[2] + ";"
                + "-fx-text-fill: " + badge[3] + ";"
                + "-fx-font-size: 10px; -fx-font-weight: bold;"
                + "-fx-background-radius: 20; -fx-padding: 2 8 2 8;");
        nombreBox.getChildren().addAll(nombreLbl, badgeLbl);
        celdaNombre.getChildren().addAll(avatarBox, nombreBox);

        HBox acciones = accionesBox(
                () -> abrirFormularioAlerta(t),
                () -> confirmarEliminarAlerta(t));

        fila.getChildren().addAll(celdaNombre, acciones);
        return fila;
    }

    // ═══════════════════════════════════════════════════════════════
    // PANEL — TIPOS DE ARMA
    // ═══════════════════════════════════════════════════════════════
    private VBox buildPanelArma() {
        VBox panel = new VBox(0);
        panel.setStyle("-fx-background-color: " + WHITE + "; -fx-background-radius: 0 0 12 12;");
        shadow(panel);

        panel.getChildren().addAll(
                buildToolbarPanel("Buscar tipo de arma...", campoBusquedaArma,
                        tf -> {
                            campoBusquedaArma = tf;
                            tf.textProperty().addListener((o, ov, nv) -> filtrar(nv));
                        }),
                buildTablaArma()
        );
        return panel;
    }

    private VBox buildTablaArma() {
        VBox card = new VBox(0);

        HBox header = buildHeader();
        HBox hNombreWrap = new HBox();
        HBox.setHgrow(hNombreWrap, Priority.ALWAYS);
        hNombreWrap.getChildren().add(colHeader("Nombre", 0));
        header.getChildren().addAll(
                hNombreWrap,
                colHeaderFixed("Descripción", 280),
                colHeaderFixed("Acciones", 120));
        card.getChildren().add(header);

        tablaContainerArma = new VBox(0);
        card.getChildren().add(tablaContainerArma);

        HBox footer = buildFooterBox();
        lblMostrandoArma = label("Cargando...", 12, GRAY_TEXT, false);
        footer.getChildren().add(lblMostrandoArma);
        card.getChildren().add(footer);

        renderTablaArma(cargarTiposArma());
        return card;
    }

    private void renderTablaArma(List<TipoArma> lista) {
        if (tablaContainerArma == null) {
            return;
        }
        tablaContainerArma.getChildren().clear();
        if (lista.isEmpty()) {
            Label v = label("No hay tipos de arma registrados.", 14, GRAY_TEXT, false);
            VBox.setMargin(v, new Insets(30, 16, 30, 16));
            tablaContainerArma.getChildren().add(v);
        } else {
            for (int i = 0; i < lista.size(); i++) {
                tablaContainerArma.getChildren().add(buildFilaArma(lista.get(i), i % 2 == 0));
            }
        }
        if (lblMostrandoArma != null) {
            lblMostrandoArma.setText("Total: " + lista.size() + " tipos de arma");
        }
    }

    private HBox buildFilaArma(TipoArma t, boolean par) {
        HBox fila = buildFilaBase(par);

        // Nombre crece
        HBox celdaNombre = new HBox(10);
        celdaNombre.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(celdaNombre, Priority.ALWAYS);

        StackPane avatarBox = new StackPane();
        Circle avatar = new Circle(20, Color.web(PURPLE));
        Label avatarLbl = label("\uf6ff", 12, WHITE, false);
        avatarLbl.setStyle("-fx-font-family: 'Font Awesome 6 Free Solid'; -fx-font-size: 12px; -fx-text-fill: white;");
        avatarBox.getChildren().addAll(avatar, avatarLbl);

        VBox nombreBox = new VBox(2);
        Label nombreLbl = new Label(t.getNombre());
        nombreLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        Label subLbl = label("Tipo de arma", 11, GRAY_TEXT, false);
        nombreBox.getChildren().addAll(nombreLbl, subLbl);
        celdaNombre.getChildren().addAll(avatarBox, nombreBox);

        // Descripción — ancho fijo
        Label descLbl = label(
                t.getDescripcion() != null && !t.getDescripcion().isBlank()
                ? t.getDescripcion() : "—",
                12, GRAY_TEXT, false);
        descLbl.setPrefWidth(280);
        descLbl.setMinWidth(280);
        descLbl.setMaxWidth(280);
        descLbl.setWrapText(false);
        descLbl.setEllipsisString("…");

        HBox acciones = accionesBox(
                () -> abrirFormularioArma(t),
                () -> confirmarEliminarArma(t));

        fila.getChildren().addAll(celdaNombre, descLbl, acciones);
        return fila;
    }

    // ═══════════════════════════════════════════════════════════════
    // PANEL — MEDIOS DE TRANSPORTE
    // ═══════════════════════════════════════════════════════════════
    private VBox buildPanelMedio() {
        VBox panel = new VBox(0);
        panel.setStyle("-fx-background-color: " + WHITE + "; -fx-background-radius: 0 0 12 12;");
        shadow(panel);

        panel.getChildren().addAll(
                buildToolbarPanel("Buscar medio de transporte...", campoBusquedaMedio,
                        tf -> {
                            campoBusquedaMedio = tf;
                            tf.textProperty().addListener((o, ov, nv) -> filtrar(nv));
                        }),
                buildTablaMedio()
        );
        return panel;
    }

    private VBox buildTablaMedio() {
        VBox card = new VBox(0);

        HBox header = buildHeader();
        HBox hNombreWrap = new HBox();
        HBox.setHgrow(hNombreWrap, Priority.ALWAYS);
        hNombreWrap.getChildren().add(colHeader("Nombre", 0));
        header.getChildren().addAll(hNombreWrap, colHeaderFixed("Acciones", 120));
        card.getChildren().add(header);

        tablaContainerMedio = new VBox(0);
        card.getChildren().add(tablaContainerMedio);

        HBox footer = buildFooterBox();
        lblMostrandoMedio = label("Cargando...", 12, GRAY_TEXT, false);
        footer.getChildren().add(lblMostrandoMedio);
        card.getChildren().add(footer);

        renderTablaMedio(cargarMedios());
        return card;
    }

    private void renderTablaMedio(List<MedioTransporte> lista) {
        if (tablaContainerMedio == null) {
            return;
        }
        tablaContainerMedio.getChildren().clear();
        if (lista.isEmpty()) {
            Label v = label("No hay medios de transporte registrados.", 14, GRAY_TEXT, false);
            VBox.setMargin(v, new Insets(30, 16, 30, 16));
            tablaContainerMedio.getChildren().add(v);
        } else {
            for (int i = 0; i < lista.size(); i++) {
                tablaContainerMedio.getChildren().add(buildFilaMedio(lista.get(i), i % 2 == 0));
            }
        }
        if (lblMostrandoMedio != null) {
            lblMostrandoMedio.setText("Total: " + lista.size() + " medios de transporte");
        }
    }

    private HBox buildFilaMedio(MedioTransporte m, boolean par) {
        HBox fila = buildFilaBase(par);

        HBox celdaNombre = new HBox(10);
        celdaNombre.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(celdaNombre, Priority.ALWAYS);

        StackPane avatarBox = new StackPane();
        Circle avatar = new Circle(20, Color.web(TEAL));
        Label avatarLbl = new Label("\uf1b9");
        avatarLbl.setStyle("-fx-font-family: 'Font Awesome 6 Free Solid'; -fx-font-size: 12px; -fx-text-fill: white;");
        avatarBox.getChildren().addAll(avatar, avatarLbl);

        VBox nombreBox = new VBox(2);
        Label nombreLbl = new Label(m.getNombre() != null ? m.getNombre() : "—");
        nombreLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        Label subLbl = label("Medio de transporte", 11, GRAY_TEXT, false);
        nombreBox.getChildren().addAll(nombreLbl, subLbl);
        celdaNombre.getChildren().addAll(avatarBox, nombreBox);

        HBox acciones = accionesBox(
                () -> abrirFormularioMedio(m),
                () -> confirmarEliminarMedio(m));

        fila.getChildren().addAll(celdaNombre, acciones);
        return fila;
    }

    // ═══════════════════════════════════════════════════════════════
    // FORMULARIOS — como Dialog (igual que ComunaAdminView)
    // ═══════════════════════════════════════════════════════════════
    private void abrirFormularioAlerta(TipoAlerta existente) {
        boolean esEdicion = existente != null;

        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle(esEdicion ? "Editar tipo de alerta" : "Nuevo tipo de alerta");
        dlg.setHeaderText(null);

        VBox form = buildFormBase(
                esEdicion ? "Editar tipo de alerta" : "Nuevo tipo de alerta",
                esEdicion ? "Modifica el nombre del tipo" : "Registra una nueva categoría de alerta");

        TextField txtNombre = dlgField("Ej: Robo / Asalto, Incendio...",
                esEdicion ? existente.getNombre() : "");

        // ← NUEVO: ComboBox de prioridad
        ComboBox<String> cmbPrioridad = new ComboBox<>();
        cmbPrioridad.setItems(javafx.collections.FXCollections.observableArrayList(
                "ALTA", "MEDIA", "BAJA"
        ));
        cmbPrioridad.setPromptText("Seleccione prioridad");
        cmbPrioridad.setMaxWidth(Double.MAX_VALUE);
        cmbPrioridad.setPrefHeight(40);
        cmbPrioridad.setStyle(
                "-fx-background-color: #f5f7fb; -fx-background-radius: 8;"
                + "-fx-border-radius: 8; -fx-border-color: transparent;"
                + "-fx-font-size: 13px;");

        // Si es edición, cargar la prioridad actual
        if (esEdicion && existente.getPrioridad() != null) {
            cmbPrioridad.setValue(existente.getPrioridad());
        }

        Label lblError = label("", 12, RED, false);
        lblError.setWrapText(true);

        form.getChildren().addAll(
                label("Nombre del tipo de alerta *", 12, GRAY_TEXT, false),
                txtNombre,
                label("Prioridad *", 12, GRAY_TEXT, false),
                cmbPrioridad,
                lblError);

        dlg.getDialogPane().setContent(form);
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        styleDlgBtn(dlg, ButtonType.OK, esEdicion ? "Guardar cambios" : "Crear tipo");

        Button btnOk = (Button) dlg.getDialogPane().lookupButton(ButtonType.OK);
        btnOk.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            lblError.setText("");
            String nombre = txtNombre.getText().trim();
            if (nombre.isEmpty()) {
                lblError.setText("El nombre no puede estar vacío.");
                ev.consume();
                return;
            }
            if (cmbPrioridad.getValue() == null) {
                lblError.setText("Seleccione la prioridad.");
                ev.consume();
                return;
            }
            try {
                if (esEdicion) {
                    existente.setNombre(nombre);
                    existente.setPrioridad(cmbPrioridad.getValue());
                    tipoAlertaService.actualizar(existente);
                } else {
                    TipoAlerta n = new TipoAlerta();
                    n.setNombre(nombre);
                    n.setPrioridad(cmbPrioridad.getValue());
                    tipoAlertaService.insertar(n);
                }
                renderTablaAlerta(cargarTiposAlerta());
            } catch (Exception ex) {
                lblError.setText("Error: " + ex.getMessage());
                ev.consume();
            }
        });
        dlg.showAndWait();
    }

    private void abrirFormularioArma(TipoArma existente) {
        boolean esEdicion = existente != null;

        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle(esEdicion ? "Editar tipo de arma" : "Nuevo tipo de arma");
        dlg.setHeaderText(null);

        VBox form = buildFormBase(
                esEdicion ? "Editar tipo de arma" : "Nuevo tipo de arma",
                esEdicion ? "Modifica los datos del tipo" : "Registra un nuevo tipo de arma");

        TextField txtNombre = dlgField("Ej: Arma de fuego, Arma blanca...",
                esEdicion ? existente.getNombre() : "");
        TextField txtDesc = dlgField("Descripción opcional...",
                esEdicion && existente.getDescripcion() != null ? existente.getDescripcion() : "");
        Label lblError = label("", 12, RED, false);
        lblError.setWrapText(true);

        form.getChildren().addAll(
                label("Nombre *", 12, GRAY_TEXT, false), txtNombre,
                label("Descripción (opcional)", 12, GRAY_TEXT, false), txtDesc,
                lblError);

        dlg.getDialogPane().setContent(form);
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        styleDlgBtn(dlg, ButtonType.OK, esEdicion ? "Guardar cambios" : "Crear tipo");

        Button btnOk = (Button) dlg.getDialogPane().lookupButton(ButtonType.OK);
        btnOk.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            lblError.setText("");
            String nombre = txtNombre.getText().trim();
            if (nombre.isEmpty()) {
                lblError.setText("El nombre no puede estar vacío.");
                ev.consume();
                return;
            }
            try {
                if (esEdicion) {
                    existente.setNombre(nombre);
                    existente.setDescripcion(txtDesc.getText().trim());
                    tipoArmaService.actualizar(existente);
                } else {
                    TipoArma n = new TipoArma();
                    n.setNombre(nombre);
                    n.setDescripcion(txtDesc.getText().trim());
                    tipoArmaService.insertar(n);
                }
                renderTablaArma(cargarTiposArma());
            } catch (Exception ex) {
                lblError.setText("Error: " + ex.getMessage());
                ev.consume();
            }
        });
        dlg.showAndWait();
    }

    private void abrirFormularioMedio(MedioTransporte existente) {
        boolean esEdicion = existente != null;

        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle(esEdicion ? "Editar medio de transporte" : "Nuevo medio de transporte");
        dlg.setHeaderText(null);

        VBox form = buildFormBase(
                esEdicion ? "Editar medio de transporte" : "Nuevo medio de transporte",
                esEdicion ? "Modifica el nombre del medio" : "Registra un nuevo medio de transporte");

        TextField txtNombre = dlgField("Ej: Motocicleta, Automóvil, A pie...",
                esEdicion ? existente.getNombre() : "");
        Label lblError = label("", 12, RED, false);
        lblError.setWrapText(true);

        form.getChildren().addAll(
                label("Nombre del medio *", 12, GRAY_TEXT, false),
                txtNombre, lblError);

        dlg.getDialogPane().setContent(form);
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        styleDlgBtn(dlg, ButtonType.OK, esEdicion ? "Guardar cambios" : "Crear medio");

        Button btnOk = (Button) dlg.getDialogPane().lookupButton(ButtonType.OK);
        btnOk.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            lblError.setText("");
            String nombre = txtNombre.getText().trim();
            if (nombre.isEmpty()) {
                lblError.setText("El nombre no puede estar vacío.");
                ev.consume();
                return;
            }
            try {
                if (esEdicion) {
                    existente.setNombre(nombre);
                    medioTransporteService.actualizar(existente);
                } else {
                    MedioTransporte n = new MedioTransporte();
                    n.setNombre(nombre);
                    medioTransporteService.insertar(n);
                }
                renderTablaMedio(cargarMedios());
            } catch (Exception ex) {
                lblError.setText("Error: " + ex.getMessage());
                ev.consume();
            }
        });
        dlg.showAndWait();
    }

    // ═══════════════════════════════════════════════════════════════
    // ELIMINAR
    // ═══════════════════════════════════════════════════════════════
    private void confirmarEliminarAlerta(TipoAlerta t) {
        if (!confirmar("¿Eliminar tipo de alerta \"" + t.getNombre() + "\"?",
                "Las alertas de este tipo quedarán sin categoría.")) {
            return;
        }
        try {
            tipoAlertaService.eliminar(t.getNombre());
            renderTablaAlerta(cargarTiposAlerta());
        } catch (Exception e) {
            mostrarAlerta("Error al eliminar", e.getMessage());
        }
    }

    private void confirmarEliminarArma(TipoArma t) {
        if (!confirmar("¿Eliminar tipo de arma \"" + t.getNombre() + "\"?",
                "Esta acción no se puede deshacer.")) {
            return;
        }
        try {
            tipoArmaService.eliminar(t.getNombre());
            renderTablaArma(cargarTiposArma());
        } catch (Exception e) {
            mostrarAlerta("Error al eliminar", e.getMessage());
        }
    }

    private void confirmarEliminarMedio(MedioTransporte m) {
        if (!confirmar("¿Eliminar medio \"" + m.getNombre() + "\"?",
                "Esta acción no se puede deshacer.")) {
            return;
        }
        try {
            medioTransporteService.eliminar(m.getNombre());
            renderTablaMedio(cargarMedios());
        } catch (Exception e) {
            mostrarAlerta("Error al eliminar", e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // FILTRO
    // ═══════════════════════════════════════════════════════════════
    private void filtrar(String texto) {
        String t = texto == null ? "" : texto.toLowerCase().trim();
        switch (pestanaActiva) {
            case 0 ->
                renderTablaAlerta(cargarTiposAlerta().stream()
                        .filter(x -> x.getNombre() != null && x.getNombre().toLowerCase().contains(t))
                        .toList());
            case 1 ->
                renderTablaArma(cargarTiposArma().stream()
                        .filter(x -> x.getNombre() != null && x.getNombre().toLowerCase().contains(t))
                        .toList());
            case 2 ->
                renderTablaMedio(cargarMedios().stream()
                        .filter(x -> x.getNombre() != null && x.getNombre().toLowerCase().contains(t))
                        .toList());
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // HELPERS DE DATOS
    // ═══════════════════════════════════════════════════════════════
    private List<TipoAlerta> cargarTiposAlerta() {
        if (tipoAlertaService == null) {
            return List.of();
        }
        try {
            return tipoAlertaService.listar();
        } catch (Exception e) {
            return List.of();
        }
    }

    private List<TipoArma> cargarTiposArma() {
        if (tipoArmaService == null) {
            return List.of();
        }
        try {
            return tipoArmaService.listar();
        } catch (Exception e) {
            return List.of();
        }
    }

    private List<MedioTransporte> cargarMedios() {
        if (medioTransporteService == null) {
            return List.of();
        }
        try {
            return medioTransporteService.listar();
        } catch (Exception e) {
            return List.of();
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // HELPERS UI — mismos patrones que ComunaAdminView
    // ═══════════════════════════════════════════════════════════════
    /**
     * Toolbar con ícono FA lupa + campo de búsqueda
     */
    private HBox buildToolbarPanel(String prompt, TextField existing,
            java.util.function.Consumer<TextField> registro) {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(16, 20, 16, 20));
        bar.setStyle("-fx-background-color: white;");

        HBox searchBox = new HBox(8);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setStyle(
                "-fx-background-color: #f5f7fb;"
                + "-fx-background-radius: 10; -fx-padding: 0 14;");
        searchBox.setPrefHeight(42);
        HBox.setHgrow(searchBox, Priority.ALWAYS);

        Label searchIcon = new Label("\uf002");
        searchIcon.setStyle(
                "-fx-font-family: 'Font Awesome 6 Free Solid';"
                + "-fx-font-size: 14px; -fx-text-fill: #9ca3af;");

        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;"
                + "-fx-font-size: 13px; -fx-text-fill: #111827;");
        tf.setPrefHeight(42);
        HBox.setHgrow(tf, Priority.ALWAYS);

        registro.accept(tf);
        searchBox.getChildren().addAll(searchIcon, tf);
        bar.getChildren().add(searchBox);
        return bar;
    }

    private HBox buildHeader() {
        HBox header = new HBox(0);
        header.setPadding(new Insets(12, 16, 12, 16));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle(
                "-fx-background-color: #f8fafc;"
                + "-fx-border-color: transparent transparent " + BORDER + " transparent;"
                + "-fx-border-width: 0 0 1 0;");
        HBox.setHgrow(header, Priority.ALWAYS);
        return header;
    }

    private HBox buildFooterBox() {
        HBox footer = new HBox();
        footer.setPadding(new Insets(12, 16, 12, 16));
        footer.setStyle(
                "-fx-border-color: " + BORDER + " transparent transparent transparent;"
                + "-fx-border-width: 1 0 0 0;");
        return footer;
    }

    private Label colHeader(String text, double width) {
        Label l = new Label(text.toUpperCase());
        l.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;"
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

    private HBox buildFilaBase(boolean par) {
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
        return fila;
    }

    private HBox accionesBox(Runnable onEditar, Runnable onEliminar) {
        HBox box = new HBox(6);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPrefWidth(120);
        box.setMinWidth(120);
        box.setMaxWidth(120);
        box.getChildren().addAll(
                btnAccion("\uf044", ORANGE, "#fff8e1", "Editar", onEditar),
                btnAccion("\uf2ed", RED, RED_LIGHT, "Eliminar", onEliminar)
        );
        return box;
    }

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
        b.setOnMouseExited(e -> b.setStyle(base));
        b.setOnAction(e -> accion.run());
        Tooltip.install(b, new Tooltip(tooltip));
        return b;
    }

    private VBox buildFormBase(String titulo, String subtitulo) {
        VBox form = new VBox(12);
        form.setPadding(new Insets(24));
        form.setPrefWidth(400);
        form.setStyle("-fx-background-color: white;");

        Label lblTitulo = new Label(titulo);
        lblTitulo.setFont(Font.font("System", FontWeight.BOLD, 18));
        lblTitulo.setTextFill(Color.web("#111827"));

        form.getChildren().addAll(lblTitulo,
                label(subtitulo, 13, GRAY_TEXT, false),
                new Separator());
        return form;
    }

    private TextField dlgField(String prompt, String val) {
        TextField f = new TextField(val);
        f.setPromptText(prompt);
        f.setPrefHeight(40);
        f.setMaxWidth(Double.MAX_VALUE);
        f.setStyle("-fx-background-color: #f5f7fb; -fx-background-radius: 8;"
                + "-fx-border-radius: 8; -fx-border-color: transparent;"
                + "-fx-padding: 0 14; -fx-font-size: 13px;");
        return f;
    }

    private void styleDlgBtn(Dialog<?> dlg, ButtonType type, String texto) {
        Button btn = (Button) dlg.getDialogPane().lookupButton(type);
        btn.setText(texto);
        btn.setStyle("-fx-background-color: #1565c0; -fx-text-fill: white;"
                + "-fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 16;");
    }

    /**
     * Badge de color + ícono + categoría para TipoAlerta. Retorna: [0]=emoji,
     * [1]=etiqueta, [2]=bgColor, [3]=accentColor
     */
    private String[] badgeAlerta(String nombre) {
        if (nombre == null) {
            return new String[]{"?", "General", "#f3f4f6", GRAY_TEXT};
        }
        String n = nombre.toUpperCase();
        if (n.contains("ROB") || n.contains("ASALT")) {
            return new String[]{"!", "Delito", RED_LIGHT, RED};
        }
        if (n.contains("SOSPECH")) {
            return new String[]{"?", "Vigilancia", "#fef9c3", "#92400e"};
        }
        if (n.contains("ANIMAL")) {
            return new String[]{"A", "Fauna", "#ecfdf5", "#065f46"};
        }
        if (n.contains("INCEND")) {
            return new String[]{"F", "Incendio", "#fff7ed", "#c2410c"};
        }
        if (n.contains("RUIDO") || n.contains("ALTER")) {
            return new String[]{"R", "Alteración", "#fffbeb", "#b45309"};
        }
        if (n.contains("MÉDI") || n.contains("MEDIC")) {
            return new String[]{"M", "Médica", "#f0fdf4", "#15803d"};
        }
        if (n.contains("ACCID")) {
            return new String[]{"!", "Accidente", "#eff6ff", "#1d4ed8"};
        }
        return new String[]{"·", "General", "#f3f4f6", GRAY_TEXT};
    }

    private void shadow(Region node) {
        node.setEffect(new DropShadow(12, 0, 2, Color.web("#0000001a")));
    }

    private Label label(String text, double size, String color, boolean bold) {
        Label lbl = new Label(text);
        lbl.setFont(bold ? Font.font("System", FontWeight.BOLD, size) : Font.font("System", size));
        lbl.setTextFill(Color.web(color));
        return lbl;
    }

    private boolean confirmar(String header, String content) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Confirmar");
        a.setHeaderText(header);
        a.setContentText(content);
        return a.showAndWait().filter(r -> r == ButtonType.OK).isPresent();
    }

    private void mostrarAlerta(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private String btnPrimaryStyle() {
        return "-fx-background-color: linear-gradient(to right, #16283d, #1f3a56);"
                + "-fx-text-fill: white;"
                + "-fx-font-size: 13px; -fx-font-weight: bold;"
                + "-fx-background-radius: 8; -fx-padding: 10 18; -fx-cursor: hand;";
    }

    private String btnPrimaryHoverStyle() {
        return "-fx-background-color: linear-gradient(to right, #0f1e30, #16283d);"
                + "-fx-text-fill: white;"
                + "-fx-font-size: 13px; -fx-font-weight: bold;"
                + "-fx-background-radius: 8; -fx-padding: 10 18; -fx-cursor: hand;";
    }

}
