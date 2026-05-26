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

/**
 * Vista de gestión de Tipos / Catálogos para el panel administrativo. Tres
 * pestañas: Tipos de alerta · Tipos de arma · Medios de transporte. Mismo
 * patrón visual que ComunaAdminView.
 *
 * SRP — solo construye y gestiona esta pantalla. DIP — recibe los services
 * desde el constructor o los crea localmente, igual que el resto de vistas
 * Admin.
 *
 * @author Maria Cristina
 */
public class TiposAdminView {

    // ── Colores — mismos que AdministradorApp ────────────────────
    private static final String WHITE = "#ffffff";
    private static final String BG = "#f4f6fb";
    private static final String BLUE = "#1565c0";
    private static final String GREEN = "#43a047";
    private static final String RED = "#e53935";
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
    private VBox tableBoxAlerta;
    private VBox tableBoxArma;
    private VBox tableBoxMedio;

    // ── Pestaña activa (0 = alerta, 1 = arma, 2 = medio) ─────────
    private int pestanaActiva = 0;

    // ─────────────────────────────────────────────────────────────
    public TiposAdminView() {
        try {
            tipoAlertaService = new TipoAlertaService();
            tipoArmaService = new TipoArmaService();
            medioTransporteService = new MedioTransporteService();
        } catch (SQLException e) {
            mostrarAlerta("Error de conexión", e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // PUNTO DE ENTRADA
    // ─────────────────────────────────────────────────────────────
    public ScrollPane getView() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color: " + BG + ";");

        // Área dinámica — se reemplaza al cambiar pestaña
        StackPane bodyArea = new StackPane();
        VBox.setVgrow(bodyArea, Priority.ALWAYS);

        content.getChildren().addAll(
                buildTopBar(),
                buildMetrics(),
                buildTabBar(bodyArea),
                bodyArea
        );

        // Carga la primera pestaña
        bodyArea.getChildren().setAll(buildPanelAlerta());

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle(
                "-fx-background-color: " + BG + "; -fx-background: " + BG + ";");
        return scroll;
    }

    // ═══════════════════════════════════════════════════════════════
    // TOP BAR
    // ═══════════════════════════════════════════════════════════════
    private HBox buildTopBar() {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);

        VBox title = new VBox(4);
        Label h1 = new Label("Tipos / Catálogos");
        h1.setFont(Font.font("System", FontWeight.BOLD, 28));
        h1.setTextFill(Color.web("#111827"));
        Label sub = label(
                "Administra tipos de alerta, armas y medios de transporte",
                13, GRAY_TEXT, false);
        title.getChildren().addAll(h1, sub);

        bar.getChildren().add(title);
        return bar;
    }

    // ═══════════════════════════════════════════════════════════════
    // MÉTRICAS
    // ═══════════════════════════════════════════════════════════════
    private HBox buildMetrics() {
        HBox row = new HBox(16);

        int totalAlertas = cargarTiposAlerta().size();
        int totalArmas = cargarTiposArma().size();
        int totalMedios = cargarMedios().size();

        row.getChildren().addAll(
                statCard("🔔", "#fff0f0", RED,
                        "Tipos de alerta",
                        String.valueOf(totalAlertas),
                        "Categorías activas", RED),
                statCard("🔫", "#f3e8ff", PURPLE,
                        "Tipos de arma",
                        String.valueOf(totalArmas),
                        "Registradas en el sistema", PURPLE),
                statCard("🚗", "#e0f2fe", TEAL,
                        "Medios de transporte",
                        String.valueOf(totalMedios),
                        "Disponibles", TEAL),
                statCard("📦", "#e8f5e9", GREEN,
                        "Total catálogos",
                        String.valueOf(totalAlertas + totalArmas + totalMedios),
                        "Ítems en el sistema", GREEN)
        );
        return row;
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
            tabBtn("🔔  Tipos de alerta", 0),
            tabBtn("🔫  Tipos de arma", 1),
            tabBtn("🚗  Medios de transporte", 2)
        };

        // Resaltar pestaña activa al inicio
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
                ? "-fx-background-color: " + WHITE + ";"
                + "-fx-text-fill: " + BLUE + ";"
                + "-fx-font-size: 13px; -fx-font-weight: bold;"
                + "-fx-border-color: transparent transparent " + BLUE + " transparent;"
                + "-fx-border-width: 0 0 2 0;"
                + "-fx-background-radius: 0; -fx-cursor: hand;"
                : "-fx-background-color: " + WHITE + ";"
                + "-fx-text-fill: " + GRAY_TEXT + ";"
                + "-fx-font-size: 13px;"
                + "-fx-border-color: transparent;"
                + "-fx-background-radius: 0; -fx-cursor: hand;");
    }

    // ═══════════════════════════════════════════════════════════════
    // PANEL — TIPOS DE ALERTA
    // ═══════════════════════════════════════════════════════════════
    private VBox buildPanelAlerta() {
        VBox panel = new VBox(12);
        panel.setStyle(
                "-fx-background-color: " + WHITE + "; -fx-background-radius: 0 0 12 12;");
        panel.setEffect(new DropShadow(12, 0, 2, Color.web("#0000001a")));

        panel.getChildren().addAll(
                buildToolbar("Buscar tipo de alerta...",
                        "+ Nuevo tipo de alerta",
                        () -> abrirFormularioAlerta(null, panel)),
                buildTablaAlerta()
        );
        return panel;
    }

    private VBox buildTablaAlerta() {
        tableBoxAlerta = new VBox();
        renderTablaAlerta(cargarTiposAlerta());
        return tableBoxAlerta;
    }

    private void renderTablaAlerta(List<TipoAlerta> lista) {
        tableBoxAlerta.getChildren().clear();

        // Cabecera
        HBox header = buildHeaderRow(
                col("ID", 60), col("Nombre", 300),
                col("Alertas registradas", 180), col("Última alerta", 180),
                col("Acciones", 120));
        tableBoxAlerta.getChildren().addAll(header, separadorH());

        if (lista.isEmpty()) {
            Label empty = label("No hay tipos de alerta registrados.", 13, GRAY_TEXT, false);
            empty.setPadding(new Insets(24, 16, 24, 16));
            tableBoxAlerta.getChildren().add(empty);
        } else {
            boolean alt = false;
            for (TipoAlerta t : lista) {
                tableBoxAlerta.getChildren().addAll(buildFilaAlerta(t, alt), separadorH());
                alt = !alt;
            }
        }

        tableBoxAlerta.getChildren().add(buildFooter(lista.size(), "tipos de alerta"));
    }

    private HBox buildFilaAlerta(TipoAlerta t, boolean alt) {
        HBox row = buildFilaBase(alt);

        Label idLbl = label(String.valueOf(t.getId_tipoalerta()), 13, GRAY_TEXT, false);
        idLbl.setPrefWidth(60);

        // Badge de nombre con color según tipo
        HBox nombreBox = new HBox(8);
        nombreBox.setAlignment(Pos.CENTER_LEFT);
        nombreBox.setPrefWidth(300);
        String[] badge = badgeAlerta(t.getNombre());
        Label badgeLbl = new Label(badge[0] + " " + t.getNombre());
        badgeLbl.setStyle(
                "-fx-background-color: " + badge[1] + ";"
                + "-fx-text-fill: " + badge[2] + ";"
                + "-fx-background-radius: 12;"
                + "-fx-padding: 3 10 3 10;"
                + "-fx-font-size: 12px;");
        nombreBox.getChildren().add(badgeLbl);

        // Columnas informativas — en una integración real vendrían de alertaService
        Label alertasLbl = label("—", 13, GRAY_TEXT, false);
        alertasLbl.setPrefWidth(180);
        Label ultimaLbl = label("—", 13, GRAY_TEXT, false);
        ultimaLbl.setPrefWidth(180);

        HBox acciones = accionesBox(
                () -> abrirFormularioAlerta(t, null),
                () -> confirmarEliminarAlerta(t)
        );

        row.getChildren().addAll(idLbl, nombreBox, alertasLbl, ultimaLbl, acciones);
        return row;
    }

    // ═══════════════════════════════════════════════════════════════
    // PANEL — TIPOS DE ARMA
    // ═══════════════════════════════════════════════════════════════
    private VBox buildPanelArma() {
        VBox panel = new VBox(12);
        panel.setStyle(
                "-fx-background-color: " + WHITE + "; -fx-background-radius: 0 0 12 12;");
        panel.setEffect(new DropShadow(12, 0, 2, Color.web("#0000001a")));

        panel.getChildren().addAll(
                buildToolbar("Buscar tipo de arma...",
                        "+ Nuevo tipo de arma",
                        () -> abrirFormularioArma(null, panel)),
                buildTablaArma()
        );
        return panel;
    }

    private VBox buildTablaArma() {
        tableBoxArma = new VBox();
        renderTablaArma(cargarTiposArma());
        return tableBoxArma;
    }

    private void renderTablaArma(List<TipoArma> lista) {
        tableBoxArma.getChildren().clear();

        HBox header = buildHeaderRow(
                col("ID", 60), col("Nombre", 280),
                col("Descripción", 360), col("Acciones", 120));
        tableBoxArma.getChildren().addAll(header, separadorH());

        if (lista.isEmpty()) {
            Label empty = label("No hay tipos de arma registrados.", 13, GRAY_TEXT, false);
            empty.setPadding(new Insets(24, 16, 24, 16));
            tableBoxArma.getChildren().add(empty);
        } else {
            boolean alt = false;
            for (TipoArma t : lista) {
                tableBoxArma.getChildren().addAll(buildFilaArma(t, alt), separadorH());
                alt = !alt;
            }
        }

        tableBoxArma.getChildren().add(buildFooter(lista.size(), "tipos de arma"));
    }

    private HBox buildFilaArma(TipoArma t, boolean alt) {
        HBox row = buildFilaBase(alt);

        Label idLbl = label(String.valueOf(t.getId_tipoarma()), 13, GRAY_TEXT, false);
        idLbl.setPrefWidth(60);

        HBox nombreBox = new HBox(8);
        nombreBox.setAlignment(Pos.CENTER_LEFT);
        nombreBox.setPrefWidth(280);
        Circle dot = new Circle(5, Color.web(PURPLE));
        nombreBox.getChildren().addAll(dot, label(t.getNombre(), 13, "#111827", true));

        Label descLbl = label(
                t.getDescripcion() != null ? t.getDescripcion() : "—",
                13, GRAY_TEXT, false);
        descLbl.setPrefWidth(360);
        descLbl.setWrapText(true);

        HBox acciones = accionesBox(
                () -> abrirFormularioArma(t, null),
                () -> confirmarEliminarArma(t)
        );

        row.getChildren().addAll(idLbl, nombreBox, descLbl, acciones);
        return row;
    }

    // ═══════════════════════════════════════════════════════════════
    // PANEL — MEDIOS DE TRANSPORTE
    // ═══════════════════════════════════════════════════════════════
    private VBox buildPanelMedio() {
        VBox panel = new VBox(12);
        panel.setStyle(
                "-fx-background-color: " + WHITE + "; -fx-background-radius: 0 0 12 12;");
        panel.setEffect(new DropShadow(12, 0, 2, Color.web("#0000001a")));

        panel.getChildren().addAll(
                buildToolbar("Buscar medio de transporte...",
                        "+ Nuevo medio",
                        () -> abrirFormularioMedio(null, panel)),
                buildTablaMedio()
        );
        return panel;
    }

    private VBox buildTablaMedio() {
        tableBoxMedio = new VBox();
        renderTablaMedio(cargarMedios());
        return tableBoxMedio;
    }

    private void renderTablaMedio(List<MedioTransporte> lista) {
        tableBoxMedio.getChildren().clear();

        HBox header = buildHeaderRow(
                col("ID", 60), col("Nombre", 400), col("Acciones", 120));
        tableBoxMedio.getChildren().addAll(header, separadorH());

        if (lista.isEmpty()) {
            Label empty = label("No hay medios de transporte registrados.",
                    13, GRAY_TEXT, false);
            empty.setPadding(new Insets(24, 16, 24, 16));
            tableBoxMedio.getChildren().add(empty);
        } else {
            boolean alt = false;
            for (MedioTransporte m : lista) {
                tableBoxMedio.getChildren().addAll(buildFilaMedio(m, alt), separadorH());
                alt = !alt;
            }
        }

        tableBoxMedio.getChildren().add(buildFooter(lista.size(), "medios de transporte"));
    }

    private HBox buildFilaMedio(MedioTransporte m, boolean alt) {
        HBox row = buildFilaBase(alt);

        Label idLbl = label(String.valueOf(m.getId_mediotransporte()), 13, GRAY_TEXT, false);
        idLbl.setPrefWidth(60);

        HBox nombreBox = new HBox(8);
        nombreBox.setAlignment(Pos.CENTER_LEFT);
        nombreBox.setPrefWidth(400);
        HBox.setHgrow(nombreBox, Priority.ALWAYS);
        Circle dot = new Circle(5, Color.web(TEAL));
        nombreBox.getChildren().addAll(dot, label(m.getNombre(), 13, "#111827", true));

        HBox acciones = accionesBox(
                () -> abrirFormularioMedio(m, null),
                () -> confirmarEliminarMedio(m)
        );

        row.getChildren().addAll(idLbl, nombreBox, acciones);
        return row;
    }

    // ═══════════════════════════════════════════════════════════════
    // FORMULARIOS — TIPO ALERTA
    // ═══════════════════════════════════════════════════════════════
    private void abrirFormularioAlerta(TipoAlerta existente, VBox panelOrigen) {
        boolean esEdicion = existente != null;
        Label errorLbl = label("", 12, RED, false);
        errorLbl.setVisible(false);

        TextField txtNombre = campoTexto("Ej: Robo / Asalto, Incendio...");
        if (esEdicion) {
            txtNombre.setText(existente.getNombre());
        }

        mostrarFormularioEnRoot(
                esEdicion ? "Editar tipo de alerta" : "Nuevo tipo de alerta",
                esEdicion ? "Modifica el nombre del tipo" : "Registra una nueva categoría de alerta",
                List.of(
                        new CampoFormulario("Nombre del tipo de alerta", txtNombre)
                ),
                errorLbl,
                esEdicion ? "💾 Guardar cambios" : "✅ Crear tipo",
                () -> {
                    try {
                        if (esEdicion) {
                            existente.setNombre(txtNombre.getText().trim());
                            tipoAlertaService.actualizar(existente);
                            mostrarExito("Tipo de alerta actualizado.");
                        } else {
                            TipoAlerta nuevo = new TipoAlerta();
                            nuevo.setNombre(txtNombre.getText().trim());
                            tipoAlertaService.insertar(nuevo);
                            mostrarExito("Tipo de alerta creado.");
                        }
                        return true;
                    } catch (IllegalArgumentException ex) {
                        errorLbl.setText(ex.getMessage() != null
                                ? ex.getMessage() : "Nombre inválido.");
                        errorLbl.setVisible(true);
                        return false;
                    }
                }
        );
    }

    // ═══════════════════════════════════════════════════════════════
    // FORMULARIOS — TIPO ARMA
    // ═══════════════════════════════════════════════════════════════
    private void abrirFormularioArma(TipoArma existente, VBox panelOrigen) {

        // ✅ Variables finales — el lambda puede capturarlas sin problema
        final boolean esEdicion = existente != null;
        final TipoArma ref = existente;

        Label errorLbl = label("", 12, RED, false);
        errorLbl.setVisible(false);

        TextField txtNombre = campoTexto("Ej: Arma de fuego, Arma blanca...");
        TextField txtDesc = campoTexto("Descripción opcional...");

        if (esEdicion) {
            txtNombre.setText(ref.getNombre());
            if (ref.getDescripcion() != null) {
                txtDesc.setText(ref.getDescripcion());
            }
        }

        mostrarFormularioEnRoot(
                esEdicion ? "Editar tipo de arma" : "Nuevo tipo de arma",
                esEdicion ? "Modifica los datos del tipo de arma"
                        : "Registra un nuevo tipo de arma",
                List.of(
                        new CampoFormulario("Nombre", txtNombre),
                        new CampoFormulario("Descripción (opcional)", txtDesc)
                ),
                errorLbl,
                esEdicion ? "💾 Guardar cambios" : "✅ Crear tipo",
                () -> {
                    try {
                        if (esEdicion) {
                            ref.setNombre(txtNombre.getText().trim());
                            ref.setDescripcion(txtDesc.getText().trim());
                            try {
                                tipoArmaService.actualizar(ref);
                            } catch (SQLException ex) {
                                System.getLogger(TiposAdminView.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                            }
                            mostrarExito("Tipo de arma actualizado.");
                        } else {
                            TipoArma nuevo = new TipoArma();
                            nuevo.setNombre(txtNombre.getText().trim());
                            nuevo.setDescripcion(txtDesc.getText().trim());
                            try {
                                tipoArmaService.insertar(nuevo);
                            } catch (SQLException ex) {
                                System.getLogger(TiposAdminView.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                            }
                            mostrarExito("Tipo de arma creado.");
                        }
                        return true;
                    } catch (IllegalArgumentException ex) {
                        errorLbl.setText(ex.getMessage() != null
                                ? ex.getMessage() : "Datos inválidos.");
                        errorLbl.setVisible(true);
                        return false;
                    }
                }
        );
    }

    // ═══════════════════════════════════════════════════════════════
    // FORMULARIOS — MEDIO DE TRANSPORTE
    // ═══════════════════════════════════════════════════════════════
    private void abrirFormularioMedio(MedioTransporte existente, VBox panelOrigen) {
        boolean esEdicion = existente != null;
        Label errorLbl = label("", 12, RED, false);
        errorLbl.setVisible(false);

        TextField txtNombre = campoTexto("Ej: Motocicleta, Automóvil, A pie...");
        if (esEdicion) {
            txtNombre.setText(existente.getNombre());
        }

        mostrarFormularioEnRoot(
                esEdicion ? "Editar medio de transporte" : "Nuevo medio de transporte",
                esEdicion ? "Modifica el nombre del medio"
                        : "Registra un nuevo medio de transporte",
                List.of(new CampoFormulario("Nombre del medio", txtNombre)),
                errorLbl,
                esEdicion ? "💾 Guardar cambios" : "✅ Crear medio",
                () -> {
                    try {
                        if (esEdicion) {
                            existente.setNombre(txtNombre.getText().trim());
                            medioTransporteService.actualizar(existente);
                            mostrarExito("Medio de transporte actualizado.");
                        } else {
                            MedioTransporte nuevo = new MedioTransporte();
                            nuevo.setNombre(txtNombre.getText().trim());
                            medioTransporteService.insertar(nuevo);
                            mostrarExito("Medio de transporte creado.");
                        }
                        return true;
                    } catch (IllegalArgumentException ex) {
                        errorLbl.setText(ex.getMessage() != null
                                ? ex.getMessage() : "Nombre inválido.");
                        errorLbl.setVisible(true);
                        return false;
                    }
                }
        );
    }

    // ═══════════════════════════════════════════════════════════════
    // FORMULARIO GENÉRICO — reutilizado por los tres tipos
    // ═══════════════════════════════════════════════════════════════
    /**
     * Interfaz funcional para la acción de guardar de cada formulario.
     */
    @FunctionalInterface
    private interface AccionGuardar {

        /**
         * @return true si guardó con éxito, false si hubo error de validación
         */
        boolean ejecutar();
    }

    /**
     * Par etiqueta + control para construir formularios genéricos.
     */
    private record CampoFormulario(String etiqueta, javafx.scene.control.Control control) {

    }

    /**
     * Muestra un formulario card en el centro del BorderPane padre. Al guardar
     * con éxito o cancelar, vuelve a getView().
     */
    private void mostrarFormularioEnRoot(
            String titulo,
            String subtitulo,
            List<CampoFormulario> campos,
            Label errorLbl,
            String textoBtnGuardar,
            AccionGuardar accionGuardar) {

        VBox wrapper = new VBox(20);
        wrapper.setPadding(new Insets(32));
        wrapper.setStyle("-fx-background-color: " + BG + ";");
        wrapper.setMaxWidth(520);

        Label h1 = new Label(titulo);
        h1.setFont(Font.font("System", FontWeight.BOLD, 24));
        h1.setTextFill(Color.web("#111827"));
        Label sub = label(subtitulo, 13, GRAY_TEXT, false);

        VBox card = new VBox(16);
        card.setPadding(new Insets(28));
        card.setStyle("-fx-background-color: " + WHITE + "; -fx-background-radius: 14;");
        card.setEffect(new DropShadow(12, 0, 2, Color.web("#0000001a")));
        card.getChildren().addAll(h1, sub, separadorH());

        for (CampoFormulario cf : campos) {
            Label lbl = label(cf.etiqueta(), 13, "#374151", true);
            cf.control().setPrefHeight(40);
            cf.control().setMaxWidth(Double.MAX_VALUE);
            card.getChildren().addAll(lbl, cf.control());
        }

        card.getChildren().addAll(errorLbl, separadorH());

        Button btnGuardar = btnPrimario(textoBtnGuardar);
        Button btnCancelar = btnSecundario("✕ Cancelar");

        HBox botones = new HBox(12, btnGuardar, btnCancelar);
        botones.setAlignment(Pos.CENTER_LEFT);
        card.getChildren().add(botones);
        wrapper.getChildren().add(card);

        ScrollPane scrollForm = new ScrollPane(wrapper);
        scrollForm.setFitToWidth(true);
        scrollForm.setStyle(
                "-fx-background-color: " + BG + "; -fx-background: " + BG + ";");

        // Navegar al formulario en el BorderPane raíz
        if (tableBoxAlerta != null && tableBoxAlerta.getScene() != null) {
            BorderPane bp = (BorderPane) tableBoxAlerta.getScene().getRoot();

            btnCancelar.setOnAction(e -> bp.setCenter(getView()));
            btnGuardar.setOnAction(e -> {
                boolean ok = accionGuardar.ejecutar();
                if (ok) {
                    bp.setCenter(getView());
                }
            });

            bp.setCenter(scrollForm);
        }
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
            mostrarExito("Tipo de alerta eliminado.");
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
            mostrarExito("Tipo de arma eliminado.");
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
            mostrarExito("Medio de transporte eliminado.");
            renderTablaMedio(cargarMedios());
        } catch (Exception e) {
            mostrarAlerta("Error al eliminar", e.getMessage());
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
    // HELPERS UI
    // ═══════════════════════════════════════════════════════════════
    /**
     * Toolbar reutilizable: buscador + botón nuevo.
     */
    private HBox buildToolbar(String promptBuscar, String textoBtnNuevo,
            Runnable accionNuevo) {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(16, 16, 8, 16));

        TextField search = new TextField();
        search.setPromptText(promptBuscar);
        search.setPrefHeight(38);
        search.setStyle(
                "-fx-background-color: " + BG + ";"
                + "-fx-border-color: " + BORDER + ";"
                + "-fx-border-radius: 8; -fx-background-radius: 8;"
                + "-fx-font-size: 13px; -fx-padding: 6 12 6 12;");
        HBox.setHgrow(search, Priority.ALWAYS);

        // Filtro en tiempo real según pestaña activa
        search.textProperty().addListener((obs, ov, nv) -> filtrar(nv));

        Button btnNuevo = btnPrimario(textoBtnNuevo);
        btnNuevo.setOnAction(e -> accionNuevo.run());

        bar.getChildren().addAll(search, btnNuevo);
        return bar;
    }

    private void filtrar(String texto) {
        String t = (texto == null) ? "" : texto.toLowerCase();
        switch (pestanaActiva) {
            case 0 ->
                renderTablaAlerta(cargarTiposAlerta().stream()
                        .filter(x -> x.getNombre() != null
                        && x.getNombre().toLowerCase().contains(t))
                        .toList());
            case 1 ->
                renderTablaArma(cargarTiposArma().stream()
                        .filter(x -> x.getNombre() != null
                        && x.getNombre().toLowerCase().contains(t))
                        .toList());
            case 2 ->
                renderTablaMedio(cargarMedios().stream()
                        .filter(x -> x.getNombre() != null
                        && x.getNombre().toLowerCase().contains(t))
                        .toList());
        }
    }

    private HBox buildHeaderRow(Label... cols) {
        HBox header = new HBox();
        header.setPadding(new Insets(12, 16, 12, 16));
        header.setStyle(
                "-fx-background-color: #f8f9fc;");
        header.getChildren().addAll(cols);
        return header;
    }

    private Label col(String text, double width) {
        Label lbl = label(text, 11, GRAY_TEXT, true);
        lbl.setPrefWidth(width);
        return lbl;
    }

    private HBox buildFilaBase(boolean alt) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 16, 12, 16));
        String bg = alt ? "#fafbfc" : WHITE;
        row.setStyle("-fx-background-color: " + bg + ";");
        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #f0f4ff;"));
        row.setOnMouseExited(e -> row.setStyle("-fx-background-color: " + bg + ";"));
        return row;
    }

    private HBox buildFooter(int total, String entidad) {
        HBox footer = new HBox();
        footer.setPadding(new Insets(10, 16, 10, 16));
        footer.setStyle(
                "-fx-background-color: #f8f9fc; -fx-background-radius: 0 0 12 12;");
        footer.getChildren().add(
                label("Total: " + total + " " + entidad, 12, GRAY_TEXT, false));
        return footer;
    }

    private HBox accionesBox(Runnable onEditar, Runnable onEliminar) {
        HBox box = new HBox(8);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPrefWidth(120);

        Button btnEditar = accionBtn("✏", "#fff8e1", "#b45309");
        Button btnEliminar = accionBtn("🗑", "#fff0f0", "#b91c1c");

        btnEditar.setOnAction(e -> onEditar.run());
        btnEliminar.setOnAction(e -> onEliminar.run());

        box.getChildren().addAll(btnEditar, btnEliminar);
        return box;
    }

    private Button accionBtn(String texto, String bg, String color) {
        Button btn = new Button(texto);
        String base = "-fx-background-color: " + bg + "; -fx-text-fill: " + color + ";"
                + "-fx-font-size: 13px; -fx-background-radius: 6;"
                + "-fx-padding: 5 10 5 10; -fx-cursor: hand;";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setOpacity(0.8));
        btn.setOnMouseExited(e -> btn.setOpacity(1.0));
        return btn;
    }

    private Button btnPrimario(String texto) {
        Button btn = new Button(texto);
        btn.setPrefHeight(38);
        String base = "-fx-background-color: " + BLUE + "; -fx-text-fill: white;"
                + "-fx-font-size: 13px; -fx-font-weight: bold;"
                + "-fx-background-radius: 8; -fx-padding: 6 18 6 18; -fx-cursor: hand;";
        String hover = "-fx-background-color: #1251a3; -fx-text-fill: white;"
                + "-fx-font-size: 13px; -fx-font-weight: bold;"
                + "-fx-background-radius: 8; -fx-padding: 6 18 6 18; -fx-cursor: hand;";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e -> btn.setStyle(base));
        return btn;
    }

    private Button btnSecundario(String texto) {
        Button btn = new Button(texto);
        btn.setPrefHeight(38);
        btn.setStyle("-fx-background-color: white; -fx-text-fill: #374151;"
                + "-fx-font-size: 13px; -fx-border-color: " + BORDER + ";"
                + "-fx-border-radius: 8; -fx-background-radius: 8;"
                + "-fx-padding: 6 18 6 18; -fx-cursor: hand;");
        return btn;
    }

    private TextField campoTexto(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setPrefHeight(40);
        tf.setStyle("-fx-background-color: white; -fx-border-color: " + BORDER + ";"
                + "-fx-border-radius: 8; -fx-background-radius: 8;"
                + "-fx-font-size: 13px; -fx-padding: 6 12 6 12;");
        return tf;
    }

    /**
     * Badge de color para nombre de TipoAlerta según palabras clave.
     */
    private String[] badgeAlerta(String nombre) {
        if (nombre == null) {
            return new String[]{"🔔", "#fff0f0", RED};
        }
        String n = nombre.toUpperCase();
        if (n.contains("ROB") || n.contains("ASALT")) {
            return new String[]{"🦹", "#fff0f0", "#b91c1c"};
        }
        if (n.contains("SOSPECH")) {
            return new String[]{"👤", "#fef9c3", "#92400e"};
        }
        if (n.contains("ANIMAL")) {
            return new String[]{"🐕", "#ecfdf5", "#065f46"};
        }
        if (n.contains("INCEND")) {
            return new String[]{"🔥", "#fff7ed", "#c2410c"};
        }
        if (n.contains("RUIDO") || n.contains("ALTER")) {
            return new String[]{"📢", "#fffbeb", "#b45309"};
        }
        if (n.contains("MÉDI") || n.contains("MEDIC")) {
            return new String[]{"➕", "#f0fdf4", "#15803d"};
        }
        if (n.contains("ACCID")) {
            return new String[]{"⚠", "#eff6ff", "#1d4ed8"};
        }
        return new String[]{"🔔", "#f3f4f6", "#374151"};
    }

    private VBox statCard(String icon, String bgIcon, String iconColor,
            String title, String value, String sub, String subColor) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: " + WHITE + "; -fx-background-radius: 12;");
        HBox.setHgrow(card, Priority.ALWAYS);
        card.setEffect(new DropShadow(12, 0, 2, Color.web("#0000001a")));

        StackPane iconBox = new StackPane();
        Rectangle iconBg = new Rectangle(44, 44);
        iconBg.setArcWidth(10);
        iconBg.setArcHeight(10);
        iconBg.setFill(Color.web(bgIcon));
        Label iconLbl = label(icon, 20, iconColor, false);
        iconBox.getChildren().addAll(iconBg, iconLbl);

        HBox top = new HBox(12);
        top.setAlignment(Pos.CENTER_LEFT);
        top.getChildren().addAll(iconBox, label(title, 13, GRAY_TEXT, false));

        Label valueLbl = new Label(value);
        valueLbl.setFont(Font.font("System", FontWeight.BOLD, 34));
        valueLbl.setTextFill(Color.web("#111827"));

        card.getChildren().addAll(top, valueLbl, label(sub, 12, subColor, false));
        return card;
    }

    private Region separadorH() {
        Region sep = new Region();
        sep.setPrefHeight(1);
        sep.setStyle("-fx-background-color: " + BORDER + ";");
        return sep;
    }

    private Label label(String text, double size, String color, boolean bold) {
        Label lbl = new Label(text);
        lbl.setFont(bold
                ? Font.font("System", FontWeight.BOLD, size)
                : Font.font("System", size));
        lbl.setTextFill(Color.web(color));
        return lbl;
    }

    private boolean confirmar(String header, String content) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Confirmar");
        a.setHeaderText(header);
        a.setContentText(content);
        return a.showAndWait()
                .filter(r -> r == ButtonType.OK)
                .isPresent();
    }

    private void mostrarExito(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Éxito");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void mostrarAlerta(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
