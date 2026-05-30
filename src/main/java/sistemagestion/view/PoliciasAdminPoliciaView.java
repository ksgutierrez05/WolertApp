/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.view;

import java.util.List;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import sistemagestion.model.*;
import sistemagestion.service.PoliciaService;
import sistemagestion.service.UnidadPolicialService;

public class PoliciasAdminPoliciaView {

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
    private static final String GRAY_TEXT = "#6b7280";
    private static final String BORDER = "#e5e7eb";

    private final PoliciaService policiaService;
    private final UnidadPolicialService unidadService;

    private VBox tablaContainer;
    private TextField campoBusqueda;
    private List<Policia> todasLasPolicias;
    private HBox statsContainer;

    public PoliciasAdminPoliciaView(PoliciaService policiaService,
            UnidadPolicialService unidadService) {
        this.policiaService = policiaService;
        this.unidadService = unidadService;
    }

    // ── Build ────────────────────────────────────────────────────
    public ScrollPane build() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color:" + BG + ";");

        try {
            todasLasPolicias = policiaService.listar();
        } catch (Exception e) {
            todasLasPolicias = List.of();
            System.err.println("ERROR cargando policías: " + e.getMessage());
            e.printStackTrace();
        }

        statsContainer = buildStats();
        content.getChildren().addAll(
                buildTopBar(),
                statsContainer,
                buildToolbar(),
                buildTabla()
        );
        renderizarLista(todasLasPolicias);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color:" + BG + "; -fx-background:" + BG + ";");
        return scroll;
    }

    // ── Top bar ──────────────────────────────────────────────────
    private HBox buildTopBar() {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);

        VBox titles = new VBox(4);
        Label title = new Label("Gestión de Policías");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#111827"));
        Label sub = lbl("Administra el personal policial del sistema", 13, GRAY_TEXT, false);
        titles.getChildren().addAll(title, sub);

        Button btnNuevo = styledBtn("＋  Nuevo policía", BLUE, "#0d47a1");
        btnNuevo.setOnAction(e -> abrirFormulario(null));

        HBox right = new HBox();
        right.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(right, Priority.ALWAYS);
        right.getChildren().add(btnNuevo);

        bar.getChildren().addAll(titles, right);
        return bar;
    }

    // ── Stats ────────────────────────────────────────────────────
    private HBox buildStats() {
        HBox row = new HBox(16);
        long total = todasLasPolicias.size();
        long disp = todasLasPolicias.stream()
                .filter(p -> p.getEstadopolicial() == EstadoPolicia.DISPONIBLE).count();
        long enServicio = todasLasPolicias.stream()
                .filter(p -> p.getEstadopolicial() == EstadoPolicia.EN_SERVICIO).count();
        long ocupados = todasLasPolicias.stream()
                .filter(p -> p.getEstadopolicial() == EstadoPolicia.OCUPADO).count();

        row.getChildren().addAll(
                statCard(BLUE_LIGHT, BLUE, "Total policías", boldNum(String.valueOf(total), BLUE), "Registrados en el sistema"),
                statCard(GREEN_LIGHT, GREEN, "Disponibles", boldNum(String.valueOf(disp), GREEN), "Listos para asignación"),
                statCard(ORANGE_LIGHT, ORANGE, "En servicio", boldNum(String.valueOf(enServicio), ORANGE), "Atendiendo alertas"),
                statCard(RED_LIGHT, RED, "Ocupados", boldNum(String.valueOf(ocupados), RED), "No disponibles")
        );
        return row;
    }

    private Label boldNum(String val, String color) {
        Label l = new Label(val);
        l.setStyle("-fx-font-size:36px;-fx-font-weight:bold;-fx-text-fill:" + color + ";");
        return l;
    }

    private VBox statCard(String bgColor, String accent, String title,
            Label valueLabel, String sub) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20, 22, 20, 22));
        card.setStyle("-fx-background-color:white;-fx-background-radius:18;");
        HBox.setHgrow(card, Priority.ALWAYS);
        shadow(card);

        javafx.scene.shape.Rectangle iconBg = new javafx.scene.shape.Rectangle(52, 52);
        iconBg.setArcWidth(16);
        iconBg.setArcHeight(16);
        iconBg.setFill(Color.web(bgColor));

        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#374151;");
        Label subLbl = lbl(sub, 11, GRAY_TEXT, false);

        VBox textBox = new VBox(3, titleLbl, valueLabel, subLbl);
        card.getChildren().add(textBox);
        card.setOnMouseEntered(e -> card.setTranslateY(-3));
        card.setOnMouseExited(e -> card.setTranslateY(0));
        return card;
    }

    // ── Toolbar ──────────────────────────────────────────────────
    private HBox buildToolbar() {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(14, 20, 14, 20));
        bar.setStyle("-fx-background-color:white;-fx-background-radius:12;");
        shadow(bar);

        HBox searchBox = new HBox(8);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setStyle("-fx-background-color:#f5f7fb;-fx-background-radius:10;-fx-padding:0 14;");
        searchBox.setPrefHeight(42);
        HBox.setHgrow(searchBox, Priority.ALWAYS);

        campoBusqueda = new TextField();
        campoBusqueda.setPromptText("Buscar por nombre, placa o rango...");
        campoBusqueda.setStyle("-fx-background-color:transparent;-fx-border-color:transparent;"
                + "-fx-font-size:13px;-fx-text-fill:#111827;");
        campoBusqueda.setPrefHeight(42);
        HBox.setHgrow(campoBusqueda, Priority.ALWAYS);

        // Filtro estado
        ComboBox<String> filtroEstado = new ComboBox<>();
        filtroEstado.getItems().addAll("Todos los estados", "DISPONIBLE", "EN_SERVICIO", "OCUPADO", "FUERA_DE_SERVICIO");
        filtroEstado.setValue("Todos los estados");
        filtroEstado.setPrefHeight(42);
        filtroEstado.setStyle("-fx-background-color:#f5f7fb;-fx-border-color:transparent;"
                + "-fx-background-radius:10;-fx-font-size:13px;");

        // Filtro unidad
        ComboBox<String> filtroUnidad = new ComboBox<>();
        filtroUnidad.getItems().add("Todas las unidades");
        try {
            unidadService.listar().forEach(u -> filtroUnidad.getItems().add(u.getNombre()));
        } catch (Exception ex) {
            /* sin unidades */ }
        filtroUnidad.setValue("Todas las unidades");
        filtroUnidad.setPrefHeight(42);
        filtroUnidad.setStyle("-fx-background-color:#f5f7fb;-fx-border-color:transparent;"
                + "-fx-background-radius:10;-fx-font-size:13px;");

        // Listener búsqueda + filtros combinados
        Runnable aplicarFiltros = () -> {
            String txt = campoBusqueda.getText().toLowerCase().trim();
            String estado = filtroEstado.getValue();
            String unidad = filtroUnidad.getValue();

            List<Policia> filtradas = todasLasPolicias.stream()
                    .filter(p -> txt.isEmpty()
                    || nombreCompleto(p).toLowerCase().contains(txt)
                    || (p.getPlaca() != null && p.getPlaca().toLowerCase().contains(txt))
                    || (p.getRango() != null && p.getRango().toLowerCase().contains(txt)))
                    .filter(p -> "Todos los estados".equals(estado)
                    || (p.getEstadopolicial() != null && p.getEstadopolicial().name().equals(estado)))
                    .filter(p -> "Todas las unidades".equals(unidad)
                    || (p.getUnidadpolicial() != null && unidad.equals(p.getUnidadpolicial().getNombre())))
                    .toList();
            renderizarLista(filtradas);
        };

        campoBusqueda.textProperty().addListener((obs, o, n) -> aplicarFiltros.run());
        filtroEstado.setOnAction(e -> aplicarFiltros.run());
        filtroUnidad.setOnAction(e -> aplicarFiltros.run());

        searchBox.getChildren().add(campoBusqueda);
        bar.getChildren().addAll(searchBox, filtroEstado, filtroUnidad);
        return bar;
    }

    // ── Tabla ────────────────────────────────────────────────────
    private VBox buildTabla() {
        VBox card = new VBox(0);
        card.setStyle("-fx-background-color:white;-fx-background-radius:12;");
        shadow(card);

        HBox header = new HBox(0);
        header.setPadding(new Insets(12, 16, 12, 16));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color:#f8fafc;-fx-background-radius:12 12 0 0;"
                + "-fx-border-color:transparent transparent " + BORDER + " transparent;"
                + "-fx-border-width:0 0 1 0;");

        HBox nombreWrap = new HBox();
        HBox.setHgrow(nombreWrap, Priority.ALWAYS);
        nombreWrap.getChildren().add(colH("Nombre", true));

        header.getChildren().addAll(
                nombreWrap,
                colHFixed("Placa", 120),
                colHFixed("Rango", 130),
                colHFixed("Unidad", 170),
                colHFixed("Estado", 130),
                colHFixed("Acciones", 160)
        );
        card.getChildren().add(header);

        tablaContainer = new VBox(0);
        card.getChildren().add(tablaContainer);

        HBox footer = new HBox();
        footer.setPadding(new Insets(10, 16, 10, 16));
        footer.setStyle("-fx-background-color:#f8fafc;-fx-background-radius:0 0 12 12;"
                + "-fx-border-color:" + BORDER + " transparent transparent transparent;"
                + "-fx-border-width:1 0 0 0;");
        footer.getChildren().add(lbl("Cargando...", 12, GRAY_TEXT, false));
        card.getChildren().add(footer);
        return card;
    }

    private Label colH(String text, boolean grow) {
        Label l = new Label(text.toUpperCase());
        l.setStyle("-fx-font-size:11px;-fx-font-weight:bold;-fx-text-fill:#9ca3af;");
        if (grow) {
            HBox.setHgrow(l, Priority.ALWAYS);
        }
        return l;
    }

    private Label colHFixed(String text, double w) {
        Label l = colH(text, false);
        l.setPrefWidth(w);
        l.setMinWidth(w);
        l.setMaxWidth(w);
        return l;
    }


    // ── Renderizar ───────────────────────────────────────────────
    private void renderizarLista(List<Policia> lista) {
        tablaContainer.getChildren().clear();

        if (lista.isEmpty()) {
            VBox vacio = new VBox(10);
            vacio.setAlignment(Pos.CENTER);
            vacio.setPadding(new Insets(40));
            Label msg = lbl("No se encontraron policías", 14, GRAY_TEXT, false);
            vacio.getChildren().add(msg);
            tablaContainer.getChildren().add(vacio);
        } else {
            for (int i = 0; i < lista.size(); i++) {
                tablaContainer.getChildren().add(buildFila(lista.get(i), i % 2 == 0));
            }
        }

        // Actualiza footer
        VBox card = (VBox) tablaContainer.getParent();
        if (card != null && card.getChildren().size() >= 3) {
            HBox footer = (HBox) card.getChildren().get(2);
            if (!footer.getChildren().isEmpty()
                    && footer.getChildren().get(0) instanceof Label lbl) {
                lbl.setText("Mostrando " + lista.size() + " policía"
                        + (lista.size() != 1 ? "s" : ""));
            }
        }
        actualizarStats();
    }

    // ── Fila ─────────────────────────────────────────────────────
    private HBox buildFila(Policia p, boolean par) {
        HBox fila = new HBox(0);
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.setPadding(new Insets(10, 16, 10, 16));

        String bgN = "-fx-background-color:" + (par ? WHITE : "#fafbfd") + ";"
                + "-fx-border-color:transparent transparent " + BORDER + " transparent;"
                + "-fx-border-width:0 0 1 0;";
        fila.setStyle(bgN);
        fila.setOnMouseEntered(e -> fila.setStyle(
                "-fx-background-color:#EEF2FF;-fx-border-color:transparent transparent "
                + BORDER + " transparent;-fx-border-width:0 0 1 0;-fx-cursor:hand;"));
        fila.setOnMouseExited(e -> fila.setStyle(bgN));

        // Nombre con avatar
        HBox celdaNombre = new HBox(10);
        celdaNombre.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(celdaNombre, Priority.ALWAYS);

        String nombre = nombreCompleto(p);
        StackPane avatarBox = new StackPane();
        Circle avatar = new Circle(18, Color.web(colorAvatar(nombre)));
        Label avatarLbl = lbl(iniciales(nombre), 11, WHITE, true);
        avatarBox.getChildren().addAll(avatar, avatarLbl);

        VBox nombreBox = new VBox(2);
        Label nombreLbl = new Label(nombre.isEmpty() ? "—" : nombre);
        nombreLbl.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#111827;");
        Label cedLbl = lbl(p.getIdentificacion() != null ? p.getIdentificacion() : "—", 11, GRAY_TEXT, false);
        nombreBox.getChildren().addAll(nombreLbl, cedLbl);
        celdaNombre.getChildren().addAll(avatarBox, nombreBox);

        Label placaLbl = celda(p.getPlaca(), 120);
        Label rangoLbl = celda(p.getRango(), 130);
        String unidadNom = p.getUnidadpolicial() != null ? p.getUnidadpolicial().getNombre() : "—";
        Label unidadLbl = celda(unidadNom, 170);

        String estNom = p.getEstadopolicial() != null
                ? p.getEstadopolicial().name().replace("_", " ") : "—";
        Label estBadge = badge(estNom, bgEstado(p.getEstadopolicial()), colorEstado(p.getEstadopolicial()));
        HBox estBox = new HBox(estBadge);
        estBox.setAlignment(Pos.CENTER_LEFT);
        estBox.setPrefWidth(130);
        estBox.setMinWidth(130);
        estBox.setMaxWidth(130);

        HBox acciones = new HBox(6);
        acciones.setAlignment(Pos.CENTER_LEFT);
        acciones.setPrefWidth(160);
        acciones.setMinWidth(160);
        acciones.setMaxWidth(160);
        acciones.getChildren().addAll(
                btnAccion("Ver", BLUE, BLUE_LIGHT, () -> verPolicia(p)),
                btnAccion("Editar", ORANGE, ORANGE_LIGHT, () -> abrirFormulario(p)),
                btnAccion("Borrar", RED, RED_LIGHT, () -> eliminarPolicia(p))
        );

        fila.getChildren().addAll(celdaNombre, placaLbl, rangoLbl, unidadLbl, estBox, acciones);
        return fila;
    }

    // ── Ver ──────────────────────────────────────────────────────
    private void verPolicia(Policia p) {
        Stage dlg = new Stage();
        dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.setTitle("Detalle del policía");
        dlg.setResizable(false);

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color:" + BG + ";");
        root.setPrefWidth(420);

        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(30, 24, 24, 24));
        header.setStyle("-fx-background-color:" + BLUE + ";");

        String nombre = nombreCompleto(p);
        StackPane avBox = new StackPane();
        Circle av = new Circle(36, Color.web(colorAvatar(nombre)));
        av.setStroke(Color.WHITE);
        av.setStrokeWidth(3);
        Label avLbl = lbl(iniciales(nombre), 18, WHITE, true);
        avBox.getChildren().addAll(av, avLbl);

        Label nomLbl = new Label(nombre.isEmpty() ? "—" : nombre);
        nomLbl.setFont(Font.font("System", FontWeight.BOLD, 20));
        nomLbl.setTextFill(Color.WHITE);

        String estStr = p.getEstadopolicial() != null
                ? p.getEstadopolicial().name().replace("_", " ") : "—";
        Label estLbl = lbl(estStr, 12, "#bbdefb", false);
        header.getChildren().addAll(avBox, nomLbl, estLbl);

        VBox details = new VBox(0);
        details.setPadding(new Insets(20, 24, 24, 24));
        details.setStyle("-fx-background-color:white;");
        details.getChildren().addAll(
                detailRow("Cédula", p.getIdentificacion()),
                detailRow("Placa", p.getPlaca()),
                detailRow("Rango", p.getRango()),
                detailRow("Unidad policial", p.getUnidadpolicial() != null ? p.getUnidadpolicial().getNombre() : "—"),
                detailRow("Rol", p.getRol() != null ? p.getRol().getNombre() : "—"),
                detailRow("Estado", estStr)
        );

        Button btnCerrar = styledBtn("Cerrar", BLUE, "#0d47a1");
        btnCerrar.setMaxWidth(Double.MAX_VALUE);
        btnCerrar.setOnAction(e -> dlg.close());

        VBox footer = new VBox();
        footer.setPadding(new Insets(16, 24, 20, 24));
        footer.setStyle("-fx-background-color:white;");
        footer.getChildren().add(btnCerrar);

        root.getChildren().addAll(header, details, footer);
        dlg.setScene(new Scene(root));
        dlg.showAndWait();
    }

    private HBox detailRow(String key, String value) {
        HBox row = new HBox(12);
        row.setPadding(new Insets(12, 0, 12, 0));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-border-color:transparent transparent " + BORDER + " transparent;"
                + "-fx-border-width:0 0 1 0;");
        VBox textBox = new VBox(2);
        Label keyLbl = lbl(key, 11, GRAY_TEXT, false);
        Label valLbl = lbl(value != null && !value.isEmpty() ? value : "—", 13, "#111827", true);
        textBox.getChildren().addAll(keyLbl, valLbl);
        row.getChildren().add(textBox);
        return row;
    }

    // ── Formulario Agregar / Editar ───────────────────────────────
    private void abrirFormulario(Policia policiaExistente) {
        boolean esNuevo = (policiaExistente == null);

        Stage dlg = new Stage();
        dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.setTitle(esNuevo ? "Nuevo policía" : "Editar policía");
        dlg.setResizable(false);

        VBox root = new VBox(0);
        root.setPrefWidth(460);
        root.setStyle("-fx-background-color:" + BG + ";");

        HBox header = new HBox(12);
        header.setPadding(new Insets(20, 24, 18, 24));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color:" + BLUE + ";");
        Label titleLbl = new Label(esNuevo ? "Nuevo policia" : "Editar policia");
        titleLbl.setFont(Font.font("System", FontWeight.BOLD, 18));
        titleLbl.setTextFill(Color.WHITE);
        header.getChildren().add(titleLbl);

        VBox form = new VBox(14);
        form.setPadding(new Insets(24));
        form.setStyle("-fx-background-color:white;");

        TextField fldCedula = formField("Cédula *", esNuevo ? "" : nvl(policiaExistente.getIdentificacion()));
        TextField fldNombre1 = formField("Primer nombre *", esNuevo ? "" : nvl(policiaExistente.getPrimer_nombre()));
        TextField fldNombre2 = formField("Segundo nombre", esNuevo ? "" : nvl(policiaExistente.getSegundo_nombre()));
        TextField fldApellido1 = formField("Primer apellido *", esNuevo ? "" : nvl(policiaExistente.getPrimer_apellido()));
        TextField fldApellido2 = formField("Segundo apellido", esNuevo ? "" : nvl(policiaExistente.getSegundo_apellido()));
        TextField fldPlaca = formField("Placa *", esNuevo ? "" : nvl(policiaExistente.getPlaca()));
        TextField fldRango = formField("Rango *", esNuevo ? "" : nvl(policiaExistente.getRango()));
        TextField fldUsername = formField("Username *", esNuevo ? "" : nvl(policiaExistente.getUsername()));
        TextField fldPassword = formField("Contraseña *", "");
        TextField fldTelefono = formField("Teléfono", esNuevo ? "" : nvl(policiaExistente.getTelefono()));
        TextField fldEmail = formField("Email", esNuevo ? "" : nvl(policiaExistente.getCorreo()));
        // ComboBox Unidad
        Label lblUnidad = lbl("Unidad policial *", 12, "#374151", true);
        ComboBox<String> cbUnidad = styledCombo();
        cbUnidad.setPromptText("Selecciona una unidad");
        try {
            List<UnidadPolicial> unidades = unidadService.listar();
            unidades.forEach(u -> cbUnidad.getItems().add(u.getNombre()));
        } catch (Exception ex) {
            /* sin unidades */ }
        if (!esNuevo && policiaExistente.getUnidadpolicial() != null) {
            cbUnidad.setValue(policiaExistente.getUnidadpolicial().getNombre());
        }

        // ComboBox Estado
        Label lblEstado = lbl("Estado *", 12, "#374151", true);
        ComboBox<String> cbEstado = styledCombo();
        cbEstado.setPromptText("Selecciona estado");
        for (EstadoPolicia e : EstadoPolicia.values()) {
            cbEstado.getItems().add(e.name());
        }
        if (!esNuevo && policiaExistente.getEstadopolicial() != null) {
            cbEstado.setValue(policiaExistente.getEstadopolicial().name());
        }

        Label errLbl = lbl("", 12, RED, false);
        errLbl.setWrapText(true);

        form.getChildren().addAll(
                fldPair(fldCedula),
                fldPair(fldNombre1), fldPair(fldNombre2),
                fldPair(fldApellido1), fldPair(fldApellido2),
                fldPair(fldPlaca), fldPair(fldRango),
                new VBox(4, lblUnidad, cbUnidad),
                new VBox(4, lblEstado, cbEstado),
                errLbl
        );

        ScrollPane formScroll = new ScrollPane(form);
        formScroll.setFitToWidth(true);
        formScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        formScroll.setStyle("-fx-background:white;-fx-background-color:white;");
        formScroll.setPrefViewportHeight(420);

        HBox btnBar = new HBox(10);
        btnBar.setPadding(new Insets(16, 24, 20, 24));
        btnBar.setAlignment(Pos.CENTER_RIGHT);
        btnBar.setStyle("-fx-background-color:white;-fx-border-color:" + BORDER
                + " transparent transparent;-fx-border-width:1 0 0 0;");

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setPrefHeight(40);
        btnCancelar.setStyle("-fx-background-color:#f3f4f6;-fx-text-fill:#374151;"
                + "-fx-font-size:13px;-fx-font-weight:bold;"
                + "-fx-background-radius:8;-fx-padding:8 18;-fx-cursor:hand;");
        btnCancelar.setOnAction(e -> dlg.close());

        Button btnGuardar = styledBtn(esNuevo ? "Guardar" : "Actualizar", GREEN, "#2e7d32");
        btnGuardar.setOnAction(e -> {
            errLbl.setText("");
            String cedula = fldCedula.getText().trim();
            String nombre1 = fldNombre1.getText().trim();
            String apellido1 = fldApellido1.getText().trim();
            String placa = fldPlaca.getText().trim();
            String rango = fldRango.getText().trim();
            String username = fldUsername.getText().trim();
            String password = fldPassword.getText().trim();
            String estadoNom = cbEstado.getValue();
            String unidadNom = cbUnidad.getValue();
            String rolNom = "POLICIA";

            // Validación
            if (cedula.isEmpty() || nombre1.isEmpty() || apellido1.isEmpty()
                    || placa.isEmpty() || rango.isEmpty()
                    || estadoNom == null
                    || (esNuevo && (username.isEmpty() || password.isEmpty()))) {
                errLbl.setText("Completa todos los campos obligatorios (*).");
                return;
            }

            try {
                Policia pol = esNuevo ? new Policia() : policiaExistente;
                pol.setIdentificacion(cedula);
                pol.setPrimer_nombre(nombre1);
                pol.setSegundo_nombre(fldNombre2.getText().trim());
                pol.setPrimer_apellido(apellido1);
                pol.setSegundo_apellido(fldApellido2.getText().trim());
                pol.setTelefono(fldTelefono.getText().trim());
                pol.setCorreo(fldEmail.getText().trim());
                pol.setPlaca(placa);
                pol.setRango(rango);
                pol.setEstadopolicial(EstadoPolicia.valueOf(estadoNom));

                if (unidadNom != null && !unidadNom.isBlank()) {
                    UnidadPolicial u = new UnidadPolicial();
                    u.setNombre(unidadNom);
                    pol.setUnidadpolicial(u);
                }

                boolean ok;
                if (esNuevo) {
                    ok = policiaService.insertarCompleto(pol, username, password, rolNom);
                } else {
                    ok = policiaService.actualizar(pol);
                }

                if (ok) {
                    todasLasPolicias = policiaService.listar();
                    renderizarLista(todasLasPolicias);
                    dlg.close();
                    mostrarInfo(esNuevo ? "Policía registrado" : "Policía actualizado",
                            "Operación completada correctamente.");
                } else {
                    errLbl.setText("No se pudo guardar. Verifica que la cédula no esté ya registrada.");
                }
            } catch (Exception ex) {
                System.out.println(">>> EXCEPCION EN VISTA: " + ex.getClass().getName() + ": " + ex.getMessage());
                ex.printStackTrace();
                errLbl.setText("Error: " + (ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName()));
            }
        });

        btnBar.getChildren().addAll(btnCancelar, btnGuardar);
        root.getChildren().addAll(header, formScroll, btnBar);
        dlg.setScene(new Scene(root));
        dlg.showAndWait();
    }

    // ── Eliminar ─────────────────────────────────────────────────
    private void eliminarPolicia(Policia p) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Eliminar policía");
        confirm.setHeaderText("Eliminar a \"" + nombreCompleto(p) + "\"?");
        confirm.setContentText("Esta acción no se puede deshacer.");

        ButtonType btnSi = new ButtonType("Sí, eliminar", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnNo = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirm.getButtonTypes().setAll(btnSi, btnNo);

        confirm.showAndWait().ifPresent(resp -> {
            if (resp == btnSi) {
                try {
                    policiaService.eliminar(p.getIdentificacion());
                    todasLasPolicias = policiaService.listar();
                    renderizarLista(todasLasPolicias);
                    mostrarInfo("Eliminado", "Policía eliminado correctamente.");
                } catch (Exception ex) {
                    mostrarAlerta("Error", "No se pudo eliminar: " + ex.getMessage());
                }
            }
        });
    }

    // ── Helpers UI ────────────────────────────────────────────────
    private TextField formField(String labelTxt, String value) {
        TextField tf = new TextField(value);
        tf.setUserData(labelTxt);
        tf.setPromptText(labelTxt.replace(" *", ""));
        tf.setPrefHeight(40);
        tf.setStyle("-fx-background-color:#f5f7fb;-fx-border-color:" + BORDER + ";"
                + "-fx-border-radius:8;-fx-background-radius:8;-fx-font-size:13px;");
        return tf;
    }

    private ComboBox<String> styledCombo() {
        ComboBox<String> cb = new ComboBox<>();
        cb.setMaxWidth(Double.MAX_VALUE);
        cb.setPrefHeight(40);
        cb.setStyle("-fx-background-color:#f5f7fb;-fx-border-color:" + BORDER + ";"
                + "-fx-border-radius:8;-fx-background-radius:8;-fx-font-size:13px;");
        return cb;
    }

    private VBox fldPair(TextField tf) {
        String labelTxt = tf.getUserData() != null ? tf.getUserData().toString() : "";
        return new VBox(4, lbl(labelTxt, 12, "#374151", true), tf);
    }

    private Label celda(String txt, double width) {
        Label l = lbl(txt != null ? txt : "—", 12, "#374151", false);
        l.setPrefWidth(width);
        l.setMinWidth(width);
        l.setMaxWidth(width);
        l.setEllipsisString("…");
        return l;
    }

    private Label badge(String texto, String bg, String color) {
        Label l = lbl(texto, 11, color, true);
        l.setPadding(new Insets(3, 9, 3, 9));
        l.setStyle("-fx-background-color:" + bg + ";-fx-background-radius:20;"
                + "-fx-text-fill:" + color + ";-fx-font-weight:bold;-fx-font-size:11px;");
        return l;
    }

    private Button btnAccion(String text, String iconColor, String bgColor, Runnable accion) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color:" + bgColor + ";-fx-text-fill:" + iconColor + ";"
                + "-fx-font-size:11px;-fx-font-weight:bold;-fx-background-radius:8;"
                + "-fx-padding:6 10;-fx-cursor:hand;");
        String hov = "-fx-background-color:" + iconColor + ";-fx-text-fill:white;"
                + "-fx-font-size:11px;-fx-font-weight:bold;-fx-background-radius:8;"
                + "-fx-padding:6 10;-fx-cursor:hand;";
        b.setOnMouseEntered(e -> b.setStyle(hov));
        b.setOnMouseExited(e -> b.setStyle("-fx-background-color:" + bgColor + ";-fx-text-fill:" + iconColor + ";"
                + "-fx-font-size:11px;-fx-font-weight:bold;-fx-background-radius:8;"
                + "-fx-padding:6 10;-fx-cursor:hand;"));
        b.setOnAction(e -> accion.run());
        return b;
    }

    private Button styledBtn(String text, String color, String hover) {
        Button b = new Button(text);
        b.setPrefHeight(40);
        String base = "-fx-background-color:" + color + ";-fx-text-fill:white;"
                + "-fx-font-size:13px;-fx-font-weight:bold;"
                + "-fx-background-radius:8;-fx-padding:8 18;-fx-cursor:hand;";
        String hov = "-fx-background-color:" + hover + ";-fx-text-fill:white;"
                + "-fx-font-size:13px;-fx-font-weight:bold;"
                + "-fx-background-radius:8;-fx-padding:8 18;-fx-cursor:hand;";
        b.setStyle(base);
        b.setOnMouseEntered(e -> b.setStyle(hov));
        b.setOnMouseExited(e -> b.setStyle(base));
        return b;
    }

    private String colorEstado(EstadoPolicia e) {
        if (e == null) {
            return GRAY_TEXT;
        }
        return switch (e) {
            case DISPONIBLE ->
                GREEN;
            case EN_SERVICIO ->
                BLUE;
            case OCUPADO ->
                ORANGE;
            default ->
                GRAY_TEXT;
        };
    }

    private String bgEstado(EstadoPolicia e) {
        if (e == null) {
            return "#f3f4f6";
        }
        return switch (e) {
            case DISPONIBLE ->
                GREEN_LIGHT;
            case EN_SERVICIO ->
                BLUE_LIGHT;
            case OCUPADO ->
                ORANGE_LIGHT;
            default ->
                "#f3f4f6";
        };
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
        String[] parts = nombre.trim().split("\\s+");
        return parts.length == 1
                ? parts[0].substring(0, 1).toUpperCase()
                : (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
    }

    private String nombreCompleto(Policia p) {
        return ((p.getPrimer_nombre() != null ? p.getPrimer_nombre() : "")
                + " " + (p.getPrimer_apellido() != null ? p.getPrimer_apellido() : "")).trim();
    }

    private String nvl(String s) {
        return s != null ? s : "";
    }

    /**
     * Label normal — nunca fuerza fuente en texto con emojis
     */
    private Label lbl(String text, double size, String color, boolean bold) {
        Label l = new Label(text);
        l.setFont(bold ? Font.font("System", FontWeight.BOLD, size)
                : Font.font("System", size));
        l.setTextFill(Color.web(color));
        return l;
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
