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
import sistemagestion.model.Barrio;
import sistemagestion.model.Comuna;
import sistemagestion.service.BarrioService;
import sistemagestion.service.ComunaService;

/**
 * Vista de gestión de Barrios para el panel administrativo.
 * Sigue el mismo patrón visual de ComunaAdminView / AdministradorApp.
 * Sin datos hardcodeados — todos los datos vienen de BarrioService.
 *
 * @author Maria Cristina
 */
public class BarrioAdminView {

    // ── Colores — idénticos a ComunaAdminView ────────────────────
    private static final String WHITE     = "#ffffff";
    private static final String BG        = "#f4f6fb";
    private static final String BLUE      = "#1565c0";
    private static final String GREEN     = "#43a047";
    private static final String RED       = "#e53935";
    private static final String ORANGE    = "#fb8c00";
    private static final String GRAY_TEXT = "#6b7280";
    private static final String BORDER    = "#e5e7eb";

    private BarrioService  barrioService;
    private ComunaService  comunaService;
    private VBox           tableBox;   // contenedor de la tabla (se recarga)
    private BorderPane     parentRoot; // referencia al BorderPane raíz para navegar

    // ── Constructor ──────────────────────────────────────────────
    public BarrioAdminView() {
        try {
            barrioService = new BarrioService();
            comunaService = new ComunaService();
        } catch (SQLException e) {
            mostrarAlerta("Error de conexión", e.getMessage());
        }
    }

    // ── Punto de entrada ─────────────────────────────────────────
    public ScrollPane getView() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color: " + BG + ";");

        content.getChildren().addAll(
                buildTopBar(),
                buildMetrics(),
                buildToolbar(),
                buildTable()
        );

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
        Label h1 = new Label("Gestión de Barrios");
        h1.setFont(Font.font("System", FontWeight.BOLD, 28));
        h1.setTextFill(Color.web("#111827"));
        Label sub = label(
                "Administra los barrios y su relación con comunas",
                13, GRAY_TEXT, false);
        title.getChildren().addAll(h1, sub);

        bar.getChildren().add(title);
        return bar;
    }

    // ═══════════════════════════════════════════════════════════════
    // MÉTRICAS — calculadas desde el servicio
    // ═══════════════════════════════════════════════════════════════
    private HBox buildMetrics() {
        HBox row = new HBox(16);

        List<Barrio>  barrios = cargarBarrios();
        List<Comuna>  comunas = cargarComunas();

        int totalBarrios = barrios.size();
        int totalComunas = comunas.size();

        // Comunas distintas que tienen al menos un barrio
        long comunasConBarrios = barrios.stream()
                .filter(b -> b.getComuna() != null)
                .map(b -> b.getComuna().getId_comuna())
                .distinct()
                .count();

        // Barrios sin comuna asignada
        long sinComuna = barrios.stream()
                .filter(b -> b.getComuna() == null)
                .count();

        VBox c1 = statCard("🏠", "#e8f0fe", BLUE,
                "Total barrios",
                String.valueOf(totalBarrios),
                "Divisiones registradas", BLUE);

        VBox c2 = statCard("📍", "#e8f5e9", GREEN,
                "Comunas",
                String.valueOf(totalComunas),
                "Comunas con barrios: " + comunasConBarrios, GREEN);

        VBox c3 = statCard("🗺", "#fff8e1", ORANGE,
                "Cobertura",
                comunasConBarrios + "/" + totalComunas,
                "Comunas cubiertas", ORANGE);

        VBox c4 = statCard("⚠", "#fff0f0", RED,
                "Sin comuna",
                String.valueOf(sinComuna),
                "Barrios sin asignar", sinComuna > 0 ? RED : GRAY_TEXT);

        row.getChildren().addAll(c1, c2, c3, c4);
        return row;
    }

    // ═══════════════════════════════════════════════════════════════
    // TOOLBAR — búsqueda + filtro de comuna + botón nuevo
    // ═══════════════════════════════════════════════════════════════
    private HBox buildToolbar() {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);

        // Campo de búsqueda
        TextField search = new TextField();
        search.setPromptText("Buscar barrio por nombre...");
        search.setPrefHeight(38);
        search.setStyle(fieldStyle());
        HBox.setHgrow(search, Priority.ALWAYS);

        // Filtro de comuna
        ComboBox<String> filtroComuna = new ComboBox<>();
        filtroComuna.getItems().add("Todas las comunas");
        cargarComunas().forEach(c -> filtroComuna.getItems().add(c.getNombre()));
        filtroComuna.setValue("Todas las comunas");
        filtroComuna.setPrefHeight(38);
        filtroComuna.setPrefWidth(200);
        filtroComuna.setStyle(fieldStyle());

        // Escucha combinada
        search.textProperty().addListener(
                (obs, o, n) -> filtrarTabla(n, filtroComuna.getValue()));
        filtroComuna.valueProperty().addListener(
                (obs, o, n) -> filtrarTabla(search.getText(), n));

        // Botón nueva
        Button btnNuevo = new Button("+ Nuevo barrio");
        btnNuevo.setPrefHeight(38);
        btnNuevo.setStyle(btnPrimaryStyle());
        btnNuevo.setOnMouseEntered(e -> btnNuevo.setStyle(btnPrimaryHoverStyle()));
        btnNuevo.setOnMouseExited(e  -> btnNuevo.setStyle(btnPrimaryStyle()));
        btnNuevo.setOnAction(e -> abrirFormulario(null));

        bar.getChildren().addAll(search, filtroComuna, btnNuevo);
        return bar;
    }

    // ═══════════════════════════════════════════════════════════════
    // TABLA
    // ═══════════════════════════════════════════════════════════════
    private VBox buildTable() {
        tableBox = new VBox();
        tableBox.setStyle(
                "-fx-background-color: white; -fx-background-radius: 12;");
        tableBox.setEffect(
                new DropShadow(12, 0, 2, Color.web("#0000001a")));

        renderTabla(cargarBarrios());
        return tableBox;
    }

    private void renderTabla(List<Barrio> lista) {
        tableBox.getChildren().clear();

        // Encabezados
        HBox header = new HBox();
        header.setPadding(new Insets(12, 16, 12, 16));
        header.setStyle(
                "-fx-background-color: #f8f9fc; -fx-background-radius: 12 12 0 0;");
        header.getChildren().addAll(
                colHeader("ID",              55),
                colHeader("Nombre barrio",  220),
                colHeader("Comuna",         180),
                colHeader("Lat. centro",    120),
                colHeader("Lng. centro",    120),
                colHeader("Acciones",       180)
        );
        tableBox.getChildren().add(header);
        tableBox.getChildren().add(separadorH());

        if (lista.isEmpty()) {
            Label empty = label(
                    "No hay barrios registrados.", 13, GRAY_TEXT, false);
            empty.setPadding(new Insets(24, 16, 24, 16));
            tableBox.getChildren().add(empty);
        } else {
            boolean alt = false;
            for (Barrio b : lista) {
                tableBox.getChildren().add(buildFila(b, alt));
                tableBox.getChildren().add(separadorH());
                alt = !alt;
            }
        }

        // Pie con total
        HBox footer = new HBox();
        footer.setPadding(new Insets(10, 16, 10, 16));
        footer.setStyle(
                "-fx-background-color: #f8f9fc; -fx-background-radius: 0 0 12 12;");
        footer.getChildren().add(
                label("Total: " + lista.size() + " barrios", 12, GRAY_TEXT, false));
        tableBox.getChildren().add(footer);
    }

    private HBox buildFila(Barrio b, boolean alterno) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 16, 12, 16));
        String bgNormal = alterno ? "#fafbfc" : WHITE;
        row.setStyle("-fx-background-color: " + bgNormal + ";");
        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #f0f4ff;"));
        row.setOnMouseExited(e  -> row.setStyle("-fx-background-color: " + bgNormal + ";"));

        // ID
        Label idLbl = label(String.valueOf(b.getId_barrio()), 13, GRAY_TEXT, false);
        idLbl.setPrefWidth(55);

        // Nombre
        HBox nombreBox = new HBox(8);
        nombreBox.setAlignment(Pos.CENTER_LEFT);
        nombreBox.setPrefWidth(220);
        Circle dot = new Circle(5, Color.web(BLUE));
        Label nomLbl = label(b.getNombre() != null ? b.getNombre() : "—",
                13, "#111827", true);
        nombreBox.getChildren().addAll(dot, nomLbl);

        // Comuna (badge de color)
        HBox comunaBox = new HBox();
        comunaBox.setPrefWidth(180);
        comunaBox.setAlignment(Pos.CENTER_LEFT);
        if (b.getComuna() != null && b.getComuna().getNombre() != null) {
            String[] badge = comunaBadge(b.getComuna().getId_comuna());
            Label cLbl = label(b.getComuna().getNombre(), 11, badge[1], true);
            cLbl.setStyle(
                    "-fx-background-color: " + badge[0] + ";"
                    + "-fx-background-radius: 8;"
                    + "-fx-padding: 3 10 3 10;"
                    + "-fx-text-fill: " + badge[1] + ";"
                    + "-fx-font-size: 11px;"
                    + "-fx-font-weight: bold;");
            comunaBox.getChildren().add(cLbl);
        } else {
            Label sinC = label("Sin comuna", 11, RED, false);
            sinC.setStyle(
                    "-fx-background-color: #fff0f0; -fx-background-radius: 8;"
                    + "-fx-padding: 3 10 3 10; -fx-text-fill: " + RED + ";");
            comunaBox.getChildren().add(sinC);
        }

        // Latitud
        Label latLbl = label(
                String.valueOf(b.getLatitudcentro()), 12, GRAY_TEXT, false);
        latLbl.setPrefWidth(120);

        // Longitud
        Label lngLbl = label(
                String.valueOf(b.getLongitudcentro()), 12, GRAY_TEXT, false);
        lngLbl.setPrefWidth(120);

        // Acciones
        HBox acciones = new HBox(8);
        acciones.setAlignment(Pos.CENTER_LEFT);
        acciones.setPrefWidth(180);

        Button btnEditar   = accionBtn("✏ Editar",    "#fff8e1", "#b45309");
        Button btnEliminar = accionBtn("🗑 Eliminar", "#fff0f0", "#b91c1c");

        btnEditar.setOnAction(e   -> abrirFormulario(b));
        btnEliminar.setOnAction(e -> confirmarEliminar(b));

        acciones.getChildren().addAll(btnEditar, btnEliminar);
        row.getChildren().addAll(idLbl, nombreBox, comunaBox, latLbl, lngLbl, acciones);
        return row;
    }

    // ═══════════════════════════════════════════════════════════════
    // FORMULARIO — insertar / editar
    // ═══════════════════════════════════════════════════════════════
    private void abrirFormulario(Barrio barrioExistente) {
        boolean esEdicion = barrioExistente != null;

        // ── Campos ──────────────────────────────────────────────
        Label lblNombre = label("Nombre del barrio *", 13, "#374151", true);
        TextField txtNombre = styledField("Ej: El Prado, Centro...");
        if (esEdicion && barrioExistente.getNombre() != null) {
            txtNombre.setText(barrioExistente.getNombre());
        }

        Label lblComuna = label("Comuna *", 13, "#374151", true);
        ComboBox<String> cmbComuna = new ComboBox<>();
        cmbComuna.setPromptText("Seleccionar comuna...");
        cmbComuna.setPrefHeight(40);
        cmbComuna.setMaxWidth(Double.MAX_VALUE);
        cmbComuna.setStyle(fieldStyle());

        List<Comuna> comunas = cargarComunas();
        comunas.forEach(c -> cmbComuna.getItems().add(c.getNombre()));
        if (esEdicion && barrioExistente.getComuna() != null) {
            cmbComuna.setValue(barrioExistente.getComuna().getNombre());
        }

        Label lblLat = label("Latitud centro", 13, "#374151", true);
        TextField txtLat = styledField("Ej: 10.4631");
        if (esEdicion) txtLat.setText(String.valueOf(barrioExistente.getLatitudcentro()));

        Label lblLng = label("Longitud centro", 13, "#374151", true);
        TextField txtLng = styledField("Ej: -73.2532");
        if (esEdicion) txtLng.setText(String.valueOf(barrioExistente.getLongitudcentro()));

        Label errorLbl = label("", 12, RED, false);
        errorLbl.setVisible(false);

        // ── Botones ──────────────────────────────────────────────
        Button btnGuardar = new Button(esEdicion ? "💾 Guardar cambios" : "✅ Crear barrio");
        btnGuardar.setPrefHeight(40);
        btnGuardar.setStyle(btnPrimaryStyle());
        btnGuardar.setOnMouseEntered(e -> btnGuardar.setStyle(btnPrimaryHoverStyle()));
        btnGuardar.setOnMouseExited(e  -> btnGuardar.setStyle(btnPrimaryStyle()));

        Button btnCancelar = new Button("✕ Cancelar");
        btnCancelar.setPrefHeight(40);
        btnCancelar.setStyle(btnSecondaryStyle());

        HBox botones = new HBox(12, btnGuardar, btnCancelar);
        botones.setAlignment(Pos.CENTER_LEFT);

        // ── Card del formulario ───────────────────────────────────
        Label titulo = new Label(esEdicion ? "Editar Barrio" : "Nuevo Barrio");
        titulo.setFont(Font.font("System", FontWeight.BOLD, 24));
        titulo.setTextFill(Color.web("#111827"));

        Label subTitulo = label(
                esEdicion ? "Modifica los datos del barrio"
                          : "Registra un nuevo barrio en el sistema",
                13, GRAY_TEXT, false);

        VBox formFields = new VBox(12,
                lblNombre, txtNombre,
                lblComuna, cmbComuna,
                lblLat,    txtLat,
                lblLng,    txtLng
        );

        VBox card = new VBox(16,
                titulo, subTitulo,
                separadorH(),
                formFields,
                errorLbl,
                separadorH(),
                botones
        );
        card.setPadding(new Insets(28));
        card.setMaxWidth(520);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 14;");
        card.setEffect(new DropShadow(12, 0, 2, Color.web("#0000001a")));

        VBox wrap = new VBox(card);
        wrap.setPadding(new Insets(32));
        wrap.setStyle("-fx-background-color: " + BG + ";");

        ScrollPane scrollForm = new ScrollPane(wrap);
        scrollForm.setFitToWidth(true);
        scrollForm.setStyle("-fx-background-color: " + BG + "; -fx-background: " + BG + ";");

        // ── Navegar al formulario ─────────────────────────────────
        if (tableBox.getScene() != null) {
            BorderPane bp = (BorderPane) tableBox.getScene().getRoot();
            bp.setCenter(scrollForm);

            btnCancelar.setOnAction(e -> bp.setCenter(getView()));

            btnGuardar.setOnAction(e -> {
                String nombre = txtNombre.getText().trim();
                String comunaNombre = cmbComuna.getValue();

                // Validaciones
                if (nombre.isEmpty()) {
                    errorLbl.setText("El nombre del barrio no puede estar vacío.");
                    errorLbl.setVisible(true); return;
                }
                if (comunaNombre == null || comunaNombre.isBlank()) {
                    errorLbl.setText("Debe seleccionar una comuna.");
                    errorLbl.setVisible(true); return;
                }

                // Buscar objeto Comuna
                Comuna comunaObj = comunas.stream()
                        .filter(c -> c.getNombre().equals(comunaNombre))
                        .findFirst().orElse(null);
                if (comunaObj == null) {
                    errorLbl.setText("Comuna no encontrada.");
                    errorLbl.setVisible(true); return;
                }

                // Parsear coordenadas (opcionales)
                double lat = 0.0, lng = 0.0;
                try {
                    if (!txtLat.getText().trim().isEmpty())
                        lat = Double.parseDouble(txtLat.getText().trim());
                    if (!txtLng.getText().trim().isEmpty())
                        lng = Double.parseDouble(txtLng.getText().trim());
                } catch (NumberFormatException ex) {
                    errorLbl.setText("Latitud y longitud deben ser valores numéricos.");
                    errorLbl.setVisible(true); return;
                }

                try {
                    if (esEdicion) {
                        barrioExistente.setNombre(nombre);
                        barrioExistente.setComuna(comunaObj);
                        barrioExistente.setLatitudcentro(lat);
                        barrioExistente.setLongitudcentro(lng);
                        barrioService.actualizar(barrioExistente);
                        mostrarExito("Barrio actualizado correctamente.");
                    } else {
                        Barrio nuevo = new Barrio();
                        nuevo.setNombre(nombre);
                        nuevo.setComuna(comunaObj);
                        nuevo.setLatitudcentro(lat);
                        nuevo.setLongitudcentro(lng);
                        barrioService.insertar(nuevo);
                        mostrarExito("Barrio creado correctamente.");
                    }
                    bp.setCenter(getView());
                } catch (IllegalArgumentException ex) {
                    errorLbl.setText("Datos inválidos: " + ex.getMessage());
                    errorLbl.setVisible(true);
                } catch (Exception ex) {
                    errorLbl.setText("Error: " + ex.getMessage());
                    errorLbl.setVisible(true);
                }
            });
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // ELIMINAR
    // ═══════════════════════════════════════════════════════════════
    private void confirmarEliminar(Barrio b) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Eliminar barrio");
        confirm.setHeaderText("¿Eliminar \"" + b.getNombre() + "\"?");
        confirm.setContentText(
                "Esta acción no se puede deshacer.\n"
                + "Los datos asociados a este barrio podrían verse afectados.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    barrioService.eliminar(b.getNombre());
                    mostrarExito("Barrio eliminado correctamente.");
                    recargarTabla();
                } catch (Exception e) {
                    mostrarAlerta("Error al eliminar", e.getMessage());
                }
            }
        });
    }

    // ═══════════════════════════════════════════════════════════════
    // FILTRO
    // ═══════════════════════════════════════════════════════════════
    private void filtrarTabla(String texto, String comunaFiltro) {
        List<Barrio> todos = cargarBarrios();

        List<Barrio> filtrados = todos.stream()
                .filter(b -> {
                    boolean matchNombre = texto == null || texto.isBlank()
                            || (b.getNombre() != null
                                && b.getNombre().toLowerCase()
                                   .contains(texto.toLowerCase().trim()));

                    boolean matchComuna = comunaFiltro == null
                            || comunaFiltro.equals("Todas las comunas")
                            || (b.getComuna() != null
                                && comunaFiltro.equals(b.getComuna().getNombre()));

                    return matchNombre && matchComuna;
                })
                .toList();

        renderTabla(filtrados);
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

    private void recargarTabla() {
        renderTabla(cargarBarrios());
    }

    // ═══════════════════════════════════════════════════════════════
    // HELPERS UI
    // ═══════════════════════════════════════════════════════════════

    /** Paleta de colores de badge para las comunas (cíclico por ID) */
    private static final String[][] COMUNA_PALETA = {
        {"#e3f2fd", "#1565c0"},  // azul
        {"#fce4ec", "#c62828"},  // rojo
        {"#e8f5e9", "#2e7d32"},  // verde
        {"#fff3e0", "#e65100"},  // naranja
        {"#f3e5f5", "#6a1b9a"},  // morado
        {"#e0f7fa", "#00695c"},  // teal
    };

    private String[] comunaBadge(int idComuna) {
        int idx = Math.abs(idComuna) % COMUNA_PALETA.length;
        return COMUNA_PALETA[idx];
    }

    private VBox statCard(String icon, String bgIcon, String iconColor,
            String title, String value, String sub, String subColor) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: " + WHITE + "; -fx-background-radius: 12;");
        HBox.setHgrow(card, Priority.ALWAYS);
        card.setEffect(new DropShadow(12, 0, 2, Color.web("#0000001a")));

        HBox top = new HBox(12);
        top.setAlignment(Pos.CENTER_LEFT);

        StackPane iconBox = new StackPane();
        Rectangle iconBg = new Rectangle(44, 44);
        iconBg.setArcWidth(10); iconBg.setArcHeight(10);
        iconBg.setFill(Color.web(bgIcon));
        Label iconLbl = label(icon, 20, iconColor, false);
        iconBox.getChildren().addAll(iconBg, iconLbl);
        top.getChildren().addAll(iconBox, label(title, 13, GRAY_TEXT, false));

        Label valueLbl = new Label(value);
        valueLbl.setFont(Font.font("System", FontWeight.BOLD, 34));
        valueLbl.setTextFill(Color.web("#111827"));

        card.getChildren().addAll(top, valueLbl, label(sub, 12, subColor, false));
        return card;
    }

    private Label colHeader(String text, double width) {
        Label lbl = label(text, 11, GRAY_TEXT, true);
        lbl.setPrefWidth(width);
        return lbl;
    }

    private Button accionBtn(String texto, String bg, String color) {
        Button btn = new Button(texto);
        String base = String.format(
                "-fx-background-color: %s; -fx-text-fill: %s;"
                + "-fx-font-size: 11px; -fx-background-radius: 6;"
                + "-fx-padding: 4 10 4 10; -fx-cursor: hand;",
                bg, color);
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setOpacity(0.82));
        btn.setOnMouseExited(e  -> btn.setOpacity(1.0));
        return btn;
    }

    private TextField styledField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setPrefHeight(40);
        tf.setMaxWidth(Double.MAX_VALUE);
        tf.setStyle(fieldStyle());
        return tf;
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

    // Estilos inline reutilizables
    private String fieldStyle() {
        return "-fx-background-color: white; -fx-border-color: #e5e7eb;"
                + "-fx-border-radius: 8; -fx-background-radius: 8;"
                + "-fx-font-size: 13px; -fx-padding: 6 12 6 12;";
    }

    private String btnPrimaryStyle() {
        return "-fx-background-color: #1565c0; -fx-text-fill: white;"
                + "-fx-font-size: 13px; -fx-font-weight: bold;"
                + "-fx-background-radius: 8; -fx-padding: 6 18 6 18; -fx-cursor: hand;";
    }

    private String btnPrimaryHoverStyle() {
        return "-fx-background-color: #1251a3; -fx-text-fill: white;"
                + "-fx-font-size: 13px; -fx-font-weight: bold;"
                + "-fx-background-radius: 8; -fx-padding: 6 18 6 18; -fx-cursor: hand;";
    }

    private String btnSecondaryStyle() {
        return "-fx-background-color: white; -fx-text-fill: #374151;"
                + "-fx-font-size: 13px; -fx-border-color: #e5e7eb;"
                + "-fx-border-radius: 8; -fx-background-radius: 8;"
                + "-fx-padding: 6 18 6 18; -fx-cursor: hand;";
    }

    private void mostrarAlerta(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(titulo); a.setHeaderText(null); a.setContentText(msg);
        a.showAndWait();
    }

    private void mostrarExito(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Éxito"); a.setHeaderText(null); a.setContentText(msg);
        a.showAndWait();
    }
}