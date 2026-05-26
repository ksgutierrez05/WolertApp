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
import sistemagestion.model.Comuna;
import sistemagestion.service.ComunaService;

/**
 * Vista de gestión de Comunas para el panel administrativo.
 * Sigue el mismo patrón visual de AdministradorApp.
 *
 * @author Maria Cristina
 */
public class ComunaAdminView {

    // ── Colores — mismos que AdministradorApp ────────────────────
    private static final String WHITE     = "#ffffff";
    private static final String BG        = "#f4f6fb";
    private static final String BLUE      = "#1565c0";
    private static final String GREEN     = "#43a047";
    private static final String RED       = "#e53935";
    private static final String ORANGE    = "#fb8c00";
    private static final String GRAY_TEXT = "#6b7280";
    private static final String BORDER    = "#e5e7eb";

    private ComunaService comunaService;
    private VBox tableBox;      // contenedor de la tabla (se recarga)
    private Label totalLbl;     // métrica superior

    public ComunaAdminView() {
        try {
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
        scroll.setStyle("-fx-background-color: " + BG + "; -fx-background: " + BG + ";");
        return scroll;
    }

    // ═══════════════════════════════════════════════════════════════
    // TOP BAR
    // ═══════════════════════════════════════════════════════════════
    private HBox buildTopBar() {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);

        VBox title = new VBox(4);
        Label h1 = new Label("Gestión de Comunas");
        h1.setFont(Font.font("System", FontWeight.BOLD, 28));
        h1.setTextFill(Color.web("#111827"));
        Label sub = label("Administra las divisiones territoriales del sistema", 13, GRAY_TEXT, false);
        title.getChildren().addAll(h1, sub);

        bar.getChildren().add(title);
        return bar;
    }

    // ═══════════════════════════════════════════════════════════════
    // MÉTRICAS
    // ═══════════════════════════════════════════════════════════════
    private HBox buildMetrics() {
        HBox row = new HBox(16);

        List<Comuna> comunas = cargarComunas();
        int total = comunas.size();

        // Tarjeta 1 — Total comunas
        VBox c1 = statCard("📍", "#e8f0fe", BLUE,
                "Total comunas", String.valueOf(total),
                "Divisiones territoriales", BLUE);

        // Tarjeta 2 — Comunas registradas (misma fuente, referencia a barrios pendiente)
        VBox c2 = statCard("🏘", "#e8f5e9", GREEN,
                "Comunas activas", String.valueOf(total),
                "En el sistema", GREEN);

        // Tarjeta 3 — placeholder: barrios (se puede conectar a BarrioService)
        VBox c3 = statCard("🗺", "#fff8e1", ORANGE,
                "Barrios asociados", "—",
                "Ver en módulo Barrios", GRAY_TEXT);

        // Tarjeta 4 — placeholder: alertas por comuna
        VBox c4 = statCard("🚨", "#fff0f0", RED,
                "Alertas activas", "—",
                "Ver en módulo Alertas", RED);

        row.getChildren().addAll(c1, c2, c3, c4);
        return row;
    }

    // ═══════════════════════════════════════════════════════════════
    // TOOLBAR — búsqueda + botón nueva
    // ═══════════════════════════════════════════════════════════════
    private HBox buildToolbar() {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);

        // Buscador
        TextField search = new TextField();
        search.setPromptText("Buscar por nombre de comuna...");
        search.setPrefHeight(38);
        search.setStyle("""
                -fx-background-color: white;
                -fx-border-color: #e5e7eb;
                -fx-border-radius: 8;
                -fx-background-radius: 8;
                -fx-font-size: 13px;
                -fx-padding: 6 12 6 12;
                """);
        HBox.setHgrow(search, Priority.ALWAYS);
        search.textProperty().addListener((obs, oldVal, newVal) -> filtrarTabla(newVal));

        // Botón nueva comuna
        Button btnNueva = new Button("+ Nueva comuna");
        btnNueva.setPrefHeight(38);
        btnNueva.setStyle("""
                -fx-background-color: #1565c0;
                -fx-text-fill: white;
                -fx-font-size: 13px;
                -fx-font-weight: bold;
                -fx-background-radius: 8;
                -fx-padding: 6 18 6 18;
                -fx-cursor: hand;
                """);
        btnNueva.setOnMouseEntered(e -> btnNueva.setStyle("""
                -fx-background-color: #1251a3;
                -fx-text-fill: white;
                -fx-font-size: 13px;
                -fx-font-weight: bold;
                -fx-background-radius: 8;
                -fx-padding: 6 18 6 18;
                -fx-cursor: hand;
                """));
        btnNueva.setOnMouseExited(e -> btnNueva.setStyle("""
                -fx-background-color: #1565c0;
                -fx-text-fill: white;
                -fx-font-size: 13px;
                -fx-font-weight: bold;
                -fx-background-radius: 8;
                -fx-padding: 6 18 6 18;
                -fx-cursor: hand;
                """));
        btnNueva.setOnAction(e -> abrirFormulario(null));

        bar.getChildren().addAll(search, btnNueva);
        return bar;
    }

    // ═══════════════════════════════════════════════════════════════
    // TABLA
    // ═══════════════════════════════════════════════════════════════
    private VBox buildTable() {
        tableBox = new VBox();
        tableBox.setStyle("""
                -fx-background-color: white;
                -fx-background-radius: 12;
                """);
        tableBox.setEffect(new DropShadow(12, 0, 2, Color.web("#0000001a")));

        renderTabla(cargarComunas());
        return tableBox;
    }

    private void renderTabla(List<Comuna> lista) {
        tableBox.getChildren().clear();

        // Encabezados
        HBox header = new HBox();
        header.setPadding(new Insets(12, 16, 12, 16));
        header.setStyle("-fx-background-color: #f8f9fc; -fx-background-radius: 12 12 0 0;");
        header.getChildren().addAll(
                colHeader("ID",        60),
                colHeader("Nombre",    250),
                colHeader("Acciones",  180)
        );
        tableBox.getChildren().add(header);
        tableBox.getChildren().add(separadorH());

        if (lista.isEmpty()) {
            Label empty = label("No hay comunas registradas.", 13, GRAY_TEXT, false);
            empty.setPadding(new Insets(24, 16, 24, 16));
            tableBox.getChildren().add(empty);
            return;
        }

        boolean alterno = false;
        for (Comuna c : lista) {
            tableBox.getChildren().add(buildFila(c, alterno));
            tableBox.getChildren().add(separadorH());
            alterno = !alterno;
        }

        // Total al pie
        HBox footer = new HBox();
        footer.setPadding(new Insets(10, 16, 10, 16));
        footer.setStyle("-fx-background-color: #f8f9fc; -fx-background-radius: 0 0 12 12;");
        footer.getChildren().add(
                label("Total: " + lista.size() + " comunas", 12, GRAY_TEXT, false)
        );
        tableBox.getChildren().add(footer);
    }

    private HBox buildFila(Comuna c, boolean alterno) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 16, 12, 16));
        row.setStyle("-fx-background-color: " + (alterno ? "#fafbfc" : WHITE) + ";");

        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #f0f4ff;"));
        row.setOnMouseExited(e  -> row.setStyle("-fx-background-color: " + (alterno ? "#fafbfc" : WHITE) + ";"));

        // ID
        Label idLbl = label(String.valueOf(c.getId_comuna()), 13, GRAY_TEXT, false);
        idLbl.setPrefWidth(60);

        // Nombre con badge
        HBox nombreBox = new HBox(8);
        nombreBox.setAlignment(Pos.CENTER_LEFT);
        nombreBox.setPrefWidth(250);

        Circle dot = new Circle(5, Color.web(BLUE));

        Label nombreLbl = label(c.getNombre(), 13, "#111827", true);
        nombreBox.getChildren().addAll(dot, nombreLbl);

        // Acciones
        HBox acciones = new HBox(8);
        acciones.setAlignment(Pos.CENTER_LEFT);
        acciones.setPrefWidth(180);

        Button btnEditar  = accionBtn("✏ Editar",   "#fff8e1", "#b45309");
        Button btnEliminar = accionBtn("🗑 Eliminar", "#fff0f0", "#b91c1c");

        btnEditar.setOnAction(e -> abrirFormulario(c));
        btnEliminar.setOnAction(e -> confirmarEliminar(c));

        acciones.getChildren().addAll(btnEditar, btnEliminar);
        row.getChildren().addAll(idLbl, nombreBox, acciones);
        return row;
    }

    // ═══════════════════════════════════════════════════════════════
    // FORMULARIO — insertar / editar
    // ═══════════════════════════════════════════════════════════════
    private void abrirFormulario(Comuna comunaExistente) {
        boolean esEdicion = comunaExistente != null;

        // Panel del formulario reemplaza el centro temporalmente
        VBox form = new VBox(20);
        form.setPadding(new Insets(32));
        form.setStyle("-fx-background-color: " + BG + ";");
        form.setMaxWidth(500);

        Label titulo = new Label(esEdicion ? "Editar Comuna" : "Nueva Comuna");
        titulo.setFont(Font.font("System", FontWeight.BOLD, 24));
        titulo.setTextFill(Color.web("#111827"));

        Label subTitulo = label(
                esEdicion ? "Modifica los datos de la comuna" : "Registra una nueva división territorial",
                13, GRAY_TEXT, false);

        // Campo nombre
        Label lblNombre = label("Nombre de la comuna", 13, "#374151", true);
        TextField txtNombre = new TextField();
        txtNombre.setPromptText("Ej: Comuna 1, Zona Norte...");
        txtNombre.setPrefHeight(40);
        txtNombre.setStyle("""
                -fx-background-color: white;
                -fx-border-color: #e5e7eb;
                -fx-border-radius: 8;
                -fx-background-radius: 8;
                -fx-font-size: 13px;
                -fx-padding: 6 12 6 12;
                """);

        if (esEdicion) {
            txtNombre.setText(comunaExistente.getNombre());
        }

        // Mensaje de error
        Label errorLbl = label("", 12, RED, false);
        errorLbl.setVisible(false);

        // Botones
        HBox botones = new HBox(12);
        botones.setAlignment(Pos.CENTER_LEFT);

        Button btnGuardar = new Button(esEdicion ? "💾 Guardar cambios" : "✅ Crear comuna");
        btnGuardar.setPrefHeight(40);
        btnGuardar.setStyle("""
                -fx-background-color: #1565c0;
                -fx-text-fill: white;
                -fx-font-size: 13px;
                -fx-font-weight: bold;
                -fx-background-radius: 8;
                -fx-padding: 6 20 6 20;
                -fx-cursor: hand;
                """);

        Button btnCancelar = new Button("✕ Cancelar");
        btnCancelar.setPrefHeight(40);
        btnCancelar.setStyle("""
                -fx-background-color: white;
                -fx-text-fill: #374151;
                -fx-font-size: 13px;
                -fx-border-color: #e5e7eb;
                -fx-border-radius: 8;
                -fx-background-radius: 8;
                -fx-padding: 6 20 6 20;
                -fx-cursor: hand;
                """);

        // Acción guardar
        btnGuardar.setOnAction(e -> {
            String nombre = txtNombre.getText().trim();

            if (nombre.isEmpty()) {
                errorLbl.setText("El nombre de la comuna no puede estar vacío.");
                errorLbl.setVisible(true);
                return;
            }

            try {
                if (esEdicion) {
                    comunaExistente.setNombre(nombre);
                    comunaService.actualizar(comunaExistente);
                    mostrarExito("Comuna actualizada correctamente.");
                } else {
                    Comuna nueva = new Comuna();
                    nueva.setNombre(nombre);
                    comunaService.insertar(nueva);
                    mostrarExito("Comuna creada correctamente.");
                }
                recargarTabla();
                volverATabla(form);
            } catch (IllegalArgumentException ex) {
                errorLbl.setText("Nombre inválido. Verifica los datos.");
                errorLbl.setVisible(true);
            } catch (Exception ex) {
                errorLbl.setText("Error: " + ex.getMessage());
                errorLbl.setVisible(true);
            }
        });

        // Acción cancelar
        btnCancelar.setOnAction(e -> volverATabla(form));

        botones.getChildren().addAll(btnGuardar, btnCancelar);

        VBox card = new VBox(16);
        card.setPadding(new Insets(28));
        card.setStyle("""
                -fx-background-color: white;
                -fx-background-radius: 14;
                """);
        card.setEffect(new DropShadow(12, 0, 2, Color.web("#0000001a")));
        card.getChildren().addAll(
                titulo, subTitulo,
                separadorH(),
                lblNombre, txtNombre,
                errorLbl,
                separadorH(),
                botones
        );

        form.getChildren().add(card);

        // Mostrar en lugar de la tabla (scroll)
        ScrollPane scrollForm = new ScrollPane(form);
        scrollForm.setFitToWidth(true);
        scrollForm.setStyle("-fx-background-color: " + BG + "; -fx-background: " + BG + ";");

        // Reemplaza el center del BorderPane padre
        if (tableBox.getScene() != null) {
            BorderPane bp = (BorderPane) tableBox.getScene().getRoot();
            bp.setCenter(scrollForm);

            // El botón cancelar ya hace el regreso
            btnCancelar.setOnAction(e -> bp.setCenter(getView()));
            btnGuardar.setOnAction(e -> {
                String nombre = txtNombre.getText().trim();
                if (nombre.isEmpty()) {
                    errorLbl.setText("El nombre no puede estar vacío.");
                    errorLbl.setVisible(true);
                    return;
                }
                try {
                    if (esEdicion) {
                        comunaExistente.setNombre(nombre);
                        comunaService.actualizar(comunaExistente);
                    } else {
                        Comuna nueva = new Comuna();
                        nueva.setNombre(nombre);
                        comunaService.insertar(nueva);
                    }
                    mostrarExito(esEdicion ? "Comuna actualizada." : "Comuna creada.");
                    bp.setCenter(getView());
                } catch (IllegalArgumentException ex) {
                    errorLbl.setText("Datos inválidos.");
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
    private void confirmarEliminar(Comuna c) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Eliminar comuna");
        confirm.setHeaderText("¿Eliminar \"" + c.getNombre() + "\"?");
        confirm.setContentText(
                "Esta acción no se puede deshacer.\n"
                + "Los barrios asociados quedarán sin comuna.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    comunaService.eliminar(c.getNombre());
                    mostrarExito("Comuna eliminada correctamente.");
                    recargarTabla();
                } catch (Exception e) {
                    mostrarAlerta("Error al eliminar", e.getMessage());
                }
            }
        });
    }

    // ═══════════════════════════════════════════════════════════════
    // FILTRO DE BÚSQUEDA
    // ═══════════════════════════════════════════════════════════════
    private void filtrarTabla(String texto) {
        List<Comuna> todas = cargarComunas();
        if (texto == null || texto.isBlank()) {
            renderTabla(todas);
            return;
        }
        String filtro = texto.toLowerCase();
        List<Comuna> filtradas = todas.stream()
                .filter(c -> c.getNombre() != null
                        && c.getNombre().toLowerCase().contains(filtro))
                .toList();
        renderTabla(filtradas);
    }

    // ═══════════════════════════════════════════════════════════════
    // HELPERS DE DATOS
    // ═══════════════════════════════════════════════════════════════
    private List<Comuna> cargarComunas() {
        if (comunaService == null) return List.of();
        try {
            return comunaService.listar();
        } catch (Exception e) {
            return List.of();
        }
    }

    private void recargarTabla() {
        renderTabla(cargarComunas());
    }

    private void volverATabla(VBox form) {
        // no-op: el regreso lo maneja el BorderPane en abrirFormulario
    }

    // ═══════════════════════════════════════════════════════════════
    // HELPERS UI
    // ═══════════════════════════════════════════════════════════════
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
        iconBg.setArcWidth(10);
        iconBg.setArcHeight(10);
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
        String base = String.format("""
                -fx-background-color: %s;
                -fx-text-fill: %s;
                -fx-font-size: 11px;
                -fx-background-radius: 6;
                -fx-padding: 4 10 4 10;
                -fx-cursor: hand;
                """, bg, color);
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setOpacity(0.85));
        btn.setOnMouseExited(e  -> btn.setOpacity(1.0));
        return btn;
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
}