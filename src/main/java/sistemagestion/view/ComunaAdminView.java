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
import sistemagestion.model.Comuna;
import sistemagestion.service.ComunaService;

public class ComunaAdminView {

    // ── Colores — idénticos a UsuariosAdminView ──────────────────
    private static final String WHITE     = "#ffffff";
    private static final String BG        = "#f4f6fb";
    private static final String BLUE      = "#1565c0";
    private static final String GREEN     = "#43a047";
    private static final String RED       = "#e53935";
    private static final String RED_LIGHT = "#fff0f0";
    private static final String ORANGE    = "#fb8c00";
    private static final String GRAY_TEXT = "#6b7280";
    private static final String BORDER    = "#e5e7eb";

    private ComunaService comunaService;
    private VBox tablaContainer;
    private TextField campoBusqueda;

    public ComunaAdminView() {
        javafx.scene.text.Font.loadFont(
                getClass().getResourceAsStream("/fa-solid-900.ttf"), 20);
        try {
            comunaService = new ComunaService();
        } catch (SQLException e) {
            mostrarAlerta("Error de conexión", e.getMessage());
        }
    }

    public ScrollPane getView() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color: " + BG + ";");

        content.getChildren().addAll(
                buildTopBar(),
                buildStatsRow(),
                buildToolbar(),
                buildTabla()
        );

        cargarYRenderizar();

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

        VBox titles = new VBox(4);
        Label title = new Label("Gestión de Comunas");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#111827"));
        Label sub = label("Administra las divisiones territoriales del sistema", 13, GRAY_TEXT, false);
        titles.getChildren().addAll(title, sub);

        HBox right = new HBox(12);
        right.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(right, Priority.ALWAYS);

        Button btnNueva = new Button("+ Nueva comuna");
        btnNueva.setStyle("""
                -fx-background-color: #1565c0;
                -fx-text-fill: white; -fx-font-size: 13px;
                -fx-font-weight: bold; -fx-background-radius: 8;
                -fx-padding: 10 18; -fx-cursor: hand;
                """);
        btnNueva.setOnMouseEntered(e -> btnNueva.setStyle("""
                -fx-background-color: #0d47a1;
                -fx-text-fill: white; -fx-font-size: 13px;
                -fx-font-weight: bold; -fx-background-radius: 8;
                -fx-padding: 10 18; -fx-cursor: hand;
                """));
        btnNueva.setOnMouseExited(e -> btnNueva.setStyle("""
                -fx-background-color: #1565c0;
                -fx-text-fill: white; -fx-font-size: 13px;
                -fx-font-weight: bold; -fx-background-radius: 8;
                -fx-padding: 10 18; -fx-cursor: hand;
                """));
        btnNueva.setOnAction(e -> abrirFormulario(null));

        right.getChildren().add(btnNueva);
        bar.getChildren().addAll(titles, right);
        return bar;
    }

    // ═══════════════════════════════════════════════════════════════
    // STATS ROW — mismo patrón que UsuariosAdminView (Font Awesome + número en color)
    // ═══════════════════════════════════════════════════════════════
    private HBox buildStatsRow() {
        HBox row = new HBox(16);
        HBox.setHgrow(row, Priority.ALWAYS);

        int total = cargarComunas().size();

        Label lblTotalVal   = boldNum(String.valueOf(total), BLUE);
        Label lblActivosVal = boldNum(String.valueOf(total), GREEN);
        Label lblBarriosVal = boldNum("—",                  ORANGE);
        Label lblAlertasVal = boldNum("—",                  RED);

        row.getChildren().addAll(
                statCard("#e8f0fe", BLUE,   "\uf5a0", "Total comunas",       lblTotalVal,   "Divisiones registradas"),
                statCard("#e8f5e9", GREEN,  "\uf058", "Comunas activas",     lblActivosVal, "En el sistema"),
                statCard("#fff8e1", ORANGE, "\uf279", "Barrios asociados",   lblBarriosVal, "Ver módulo Barrios"),
                statCard(RED_LIGHT, RED,    "\uf071", "Alertas por zona",    lblAlertasVal, "Ver módulo Alertas")
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
                + "-fx-font-size: 22px;"
                + "-fx-text-fill: " + accentColor + ";");
        iconWrap.getChildren().addAll(iconBg, iconLbl);

        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #374151;");

        Label subLbl = new Label(sub);
        subLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: " + GRAY_TEXT + ";");

        VBox textBox = new VBox(3, titleLbl, valueLabel, subLbl);

        HBox top = new HBox(16);
        top.setAlignment(Pos.CENTER_LEFT);
        top.getChildren().addAll(iconWrap, textBox);
        card.getChildren().add(top);

        card.setOnMouseEntered(e -> card.setTranslateY(-3));
        card.setOnMouseExited(e  -> card.setTranslateY(0));
        return card;
    }

    // ═══════════════════════════════════════════════════════════════
    // TOOLBAR — búsqueda (mismo estilo que UsuariosAdminView)
    // ═══════════════════════════════════════════════════════════════
    private HBox buildToolbar() {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(16, 20, 16, 20));
        bar.setStyle("-fx-background-color: white; -fx-background-radius: 12;");
        shadow(bar);

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
        campoBusqueda.setPromptText("Buscar por nombre de comuna...");
        campoBusqueda.setStyle(
                "-fx-background-color: transparent;"
                + "-fx-border-color: transparent;"
                + "-fx-font-size: 13px;"
                + "-fx-text-fill: #111827;");
        campoBusqueda.setPrefHeight(42);
        HBox.setHgrow(campoBusqueda, Priority.ALWAYS);
        campoBusqueda.textProperty().addListener((obs, o, n) -> filtrarYMostrar());
        searchBox.getChildren().addAll(searchIcon, campoBusqueda);

        bar.getChildren().add(searchBox);
        return bar;
    }

    // ═══════════════════════════════════════════════════════════════
    // TABLA — mismo patrón de header/filas/footer que UsuariosAdminView
    // ═══════════════════════════════════════════════════════════════
    private VBox buildTabla() {
        VBox card = new VBox(0);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12;");
        shadow(card);

        // Header
        HBox header = new HBox(0);
        header.setPadding(new Insets(12, 16, 12, 16));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle(
                "-fx-background-color: #f8fafc;"
                + "-fx-background-radius: 12 12 0 0;"
                + "-fx-border-color: transparent transparent " + BORDER + " transparent;"
                + "-fx-border-width: 0 0 1 0;");

        HBox.setHgrow(header, Priority.ALWAYS);

        // Wrapper para Nombre — HGrow solo funciona en Region/Pane, no en Label
        HBox hNombreWrap = new HBox();
        HBox.setHgrow(hNombreWrap, Priority.ALWAYS);
        Label hNombre = colHeader("Nombre", 0);
        hNombreWrap.getChildren().add(hNombre);

        // Barrios — ancho fijo (debe coincidir exactamente con barrioBox en filas)
        Label hBarrios = colHeaderFixed("Barrios asociados", 180);

        // Acciones — ancho fijo (debe coincidir con acciones en filas)
        Label hAcciones = colHeaderFixed("Acciones", 160);

        header.getChildren().addAll(hNombreWrap, hBarrios, hAcciones);
        card.getChildren().add(header);

        tablaContainer = new VBox(0);
        card.getChildren().add(tablaContainer);

        HBox footer = new HBox();
        footer.setPadding(new Insets(12, 16, 12, 16));
        footer.setStyle(
                "-fx-border-color: " + BORDER + " transparent transparent transparent;"
                + "-fx-border-width: 1 0 0 0;");
        Label footerLbl = label("Cargando comunas...", 12, GRAY_TEXT, false);
        footer.getChildren().add(footerLbl);
        card.getChildren().add(footer);

        return card;
    }

    private Label colHeader(String text, double width) {
        Label l = new Label(text.toUpperCase());
        l.setStyle(
                "-fx-font-size: 11px;"
                + "-fx-font-weight: bold;"
                + "-fx-text-fill: #9ca3af;"
                + "-fx-letter-spacing: 0.5px;");
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

    // ═══════════════════════════════════════════════════════════════
    // CARGA Y FILTRADO
    // ═══════════════════════════════════════════════════════════════
    private void cargarYRenderizar() {
        renderizarLista(cargarComunas());
    }

    private void filtrarYMostrar() {
        String texto = campoBusqueda.getText().toLowerCase().trim();
        List<Comuna> todas = cargarComunas();
        if (texto.isEmpty()) {
            renderizarLista(todas);
            return;
        }
        List<Comuna> filtradas = todas.stream()
                .filter(c -> c.getNombre() != null
                          && c.getNombre().toLowerCase().contains(texto))
                .toList();
        renderizarLista(filtradas);
    }

    private void renderizarLista(List<Comuna> lista) {
        tablaContainer.getChildren().clear();

        if (lista.isEmpty()) {
            Label vacio = label("No se encontraron comunas", 14, GRAY_TEXT, false);
            VBox.setMargin(vacio, new Insets(30, 16, 30, 16));
            tablaContainer.getChildren().add(vacio);
            return;
        }

        for (int i = 0; i < lista.size(); i++) {
            tablaContainer.getChildren().add(buildFila(lista.get(i), i % 2 == 0));
        }

        // Actualizar footer
        VBox card = (VBox) tablaContainer.getParent();
        if (card != null && card.getChildren().size() >= 3) {
            HBox footer = (HBox) card.getChildren().get(2);
            if (!footer.getChildren().isEmpty() && footer.getChildren().get(0) instanceof Label) {
                ((Label) footer.getChildren().get(0))
                        .setText("Mostrando " + lista.size() + " comunas");
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // FILA DE TABLA — mismo diseño que UsuariosAdminView
    // ═══════════════════════════════════════════════════════════════
    private HBox buildFila(Comuna c, boolean par) {
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

        // ── Col 1: Nombre con avatar de color (crece) ───────────────
        HBox celdaNombre = new HBox(10);
        celdaNombre.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(celdaNombre, Priority.ALWAYS);

        StackPane avatarBox = new StackPane();
        Circle avatar = new Circle(20, Color.web(colorAvatar(c.getNombre())));
        Label avatarLbl = label(iniciales(c.getNombre()), 12, WHITE, true);
        avatarBox.getChildren().addAll(avatar, avatarLbl);

        VBox nombreBox = new VBox(2);
        Label nombreLbl = new Label(c.getNombre());
        nombreLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        Label subLbl = label("División territorial", 11, GRAY_TEXT, false);
        nombreBox.getChildren().addAll(nombreLbl, subLbl);
        celdaNombre.getChildren().addAll(avatarBox, nombreBox);

        // ── Col 3: Badge barrios (180px) — placeholder ────────────────
        Label barrioBadge = new Label("Ver barrios");
        barrioBadge.setStyle(
                "-fx-background-color: #e8f0fe;"
                + "-fx-text-fill: " + BLUE + ";"
                + "-fx-font-size: 11px; -fx-font-weight: bold;"
                + "-fx-background-radius: 20; -fx-padding: 4 10 4 10;");
        HBox barrioBox = new HBox(barrioBadge);
        barrioBox.setAlignment(Pos.CENTER_LEFT);
        barrioBox.setPrefWidth(180);
        barrioBox.setMinWidth(180);
        barrioBox.setMaxWidth(180);

        // ── Col 4: Acciones (150px) — mismo estilo con Font Awesome ───
        HBox acciones = new HBox(6);
        acciones.setAlignment(Pos.CENTER_LEFT);
        acciones.setPrefWidth(160);
        acciones.setMinWidth(160);
        acciones.setMaxWidth(160);
        acciones.getChildren().addAll(
                btnAccion("\uf06e", BLUE,    "#e8f0fe", "Ver",     () -> abrirDialogoVer(c)),
                btnAccion("\uf044", ORANGE,  "#fff8e1", "Editar",  () -> abrirFormulario(c)),
                btnAccion("\uf2ed", RED,     RED_LIGHT, "Eliminar",() -> confirmarEliminar(c))
        );

        fila.getChildren().addAll(celdaNombre, barrioBox, acciones);
        return fila;
    }

    // ═══════════════════════════════════════════════════════════════
    // DIÁLOGO VER (detalle)
    // ═══════════════════════════════════════════════════════════════
    private void abrirDialogoVer(Comuna c) {
        Dialog<Void> dlg = new Dialog<>();
        dlg.setTitle("Detalle de Comuna");
        dlg.setHeaderText(null);

        VBox content = new VBox(14);
        content.setPadding(new Insets(20));
        content.setPrefWidth(360);

        Circle avatar = new Circle(35, Color.web(colorAvatar(c.getNombre())));
        Label avatarLbl = label(iniciales(c.getNombre()), 22, WHITE, true);
        StackPane avatarBox = new StackPane(avatar, avatarLbl);

        Label nombre = new Label(c.getNombre());
        nombre.setFont(Font.font("System", FontWeight.BOLD, 18));
        nombre.setTextFill(Color.web("#111827"));

        VBox header = new VBox(6);
        header.setAlignment(Pos.CENTER);
        header.getChildren().addAll(avatarBox, nombre);

        content.getChildren().addAll(
                header,
                new Separator(),
                detalleRow("🆔 ID",      String.valueOf(c.getId_comuna())),
                detalleRow("📍 Nombre",  c.getNombre())
        );

        dlg.getDialogPane().setContent(content);
        dlg.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dlg.showAndWait();
    }

    // ═══════════════════════════════════════════════════════════════
    // FORMULARIO — insertar / editar (Dialog inline)
    // ═══════════════════════════════════════════════════════════════
    private void abrirFormulario(Comuna comunaExistente) {
        boolean esEdicion = comunaExistente != null;

        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle(esEdicion ? "Editar Comuna" : "Nueva Comuna");
        dlg.setHeaderText(null);

        VBox form = new VBox(12);
        form.setPadding(new Insets(24));
        form.setPrefWidth(380);
        form.setStyle("-fx-background-color: white;");

        Label lblTitulo = new Label(esEdicion ? "Editar Comuna" : "Nueva Comuna");
        lblTitulo.setFont(Font.font("System", FontWeight.BOLD, 18));
        lblTitulo.setTextFill(Color.web("#111827"));

        Label lblSub = label(
                esEdicion ? "Modifica los datos de la comuna"
                          : "Registra una nueva división territorial",
                13, GRAY_TEXT, false);

        Label lblNombreCampo = label("Nombre de la comuna *", 12, GRAY_TEXT, false);
        TextField txtNombre = dlgField("Ej: Comuna 1, Zona Norte...",
                esEdicion ? comunaExistente.getNombre() : "");

        Label lblError = label("", 12, RED, false);
        lblError.setWrapText(true);

        form.getChildren().addAll(
                lblTitulo, lblSub,
                new Separator(),
                lblNombreCampo, txtNombre,
                lblError
        );

        dlg.getDialogPane().setContent(form);
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button btnOk = (Button) dlg.getDialogPane().lookupButton(ButtonType.OK);
        btnOk.setText(esEdicion ? "Guardar cambios" : "Crear comuna");
        btnOk.setStyle("-fx-background-color: #1565c0; -fx-text-fill: white; "
                + "-fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 16;");

        btnOk.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            lblError.setText("");
            String nombre = txtNombre.getText().trim();
            if (nombre.isEmpty()) {
                lblError.setText("El nombre de la comuna no puede estar vacío.");
                ev.consume();
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
                cargarYRenderizar();
            } catch (IllegalArgumentException ex) {
                lblError.setText("Error de validación: " + ex.getMessage());
                ev.consume();
            } catch (Exception ex) {
                lblError.setText("Error BD: " + ex.getMessage());
                ev.consume();
            }
        });

        dlg.showAndWait();
    }

    // ═══════════════════════════════════════════════════════════════
    // CONFIRMAR ELIMINAR
    // ═══════════════════════════════════════════════════════════════
    private void confirmarEliminar(Comuna c) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Eliminar comuna");
        confirm.setHeaderText("¿Eliminar \"" + c.getNombre() + "\"?");
        confirm.setContentText(
                "Esta acción no se puede deshacer.\n"
                + "Los barrios asociados quedarán sin comuna.");
        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) {
                try {
                    comunaService.eliminar(c.getNombre());
                    cargarYRenderizar();
                } catch (Exception ex) {
                    mostrarAlerta("Error al eliminar", ex.getMessage());
                }
            }
        });
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

    // ═══════════════════════════════════════════════════════════════
    // HELPERS UI — idénticos a UsuariosAdminView
    // ═══════════════════════════════════════════════════════════════
    private Label celdaFija(String txt, double width) {
        Label l = new Label(txt != null ? txt : "—");
        l.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151;");
        l.setPrefWidth(width);
        l.setMinWidth(width);
        l.setMaxWidth(width);
        l.setEllipsisString("…");
        return l;
    }

    private Button btnAccion(String iconFA, String iconColor,
                              String bgColor, String tooltip, Runnable accion) {
        Button b = new Button(iconFA);
        String base = "-fx-background-color: " + bgColor + ";"
                + "-fx-text-fill: " + iconColor + ";"
                + "-fx-font-family: 'Font Awesome 6 Free Solid';"
                + "-fx-font-size: 13px;"
                + "-fx-background-radius: 8;"
                + "-fx-padding: 7 10;"
                + "-fx-cursor: hand;";
        String hover = "-fx-background-color: " + iconColor + ";"
                + "-fx-text-fill: white;"
                + "-fx-font-family: 'Font Awesome 6 Free Solid';"
                + "-fx-font-size: 13px;"
                + "-fx-background-radius: 8;"
                + "-fx-padding: 7 10;"
                + "-fx-cursor: hand;";
        b.setStyle(base);
        b.setOnMouseEntered(e -> b.setStyle(hover));
        b.setOnMouseExited(e  -> b.setStyle(base));
        b.setOnAction(e -> accion.run());
        Tooltip.install(b, new Tooltip(tooltip));
        return b;
    }

    private HBox detalleRow(String campo, String valor) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        Label k = label(campo, 13, GRAY_TEXT, false);
        k.setMinWidth(130);
        row.getChildren().addAll(k, label(valor != null ? valor : "—", 13, "#111827", false));
        return row;
    }

    private TextField dlgField(String prompt, String val) {
        TextField f = new TextField(val);
        f.setPromptText(prompt);
        f.setPrefHeight(40);
        f.setMaxWidth(Double.MAX_VALUE);
        f.setStyle("-fx-background-color: #f5f7fb; -fx-background-radius: 8; "
                + "-fx-border-radius: 8; -fx-border-color: transparent; "
                + "-fx-padding: 0 14; -fx-font-size: 13px;");
        return f;
    }

    private Label label(String text, double size, String color, boolean bold) {
        Label lbl = new Label(text);
        lbl.setFont(bold
                ? Font.font("System", FontWeight.BOLD, size)
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

    // ── Colores de avatar generados desde el nombre ───────────────
    private static final String[] AVATAR_COLORS = {
        "#1565c0", "#2e7d32", "#6a1b9a", "#c62828", "#e65100",
        "#00695c", "#283593", "#4e342e", "#37474f", "#558b2f"
    };

    private String colorAvatar(String nombre) {
        if (nombre == null || nombre.isBlank()) return AVATAR_COLORS[0];
        return AVATAR_COLORS[Math.abs(nombre.hashCode()) % AVATAR_COLORS.length];
    }

    private String iniciales(String nombre) {
        if (nombre == null || nombre.isBlank()) return "?";
        String[] partes = nombre.trim().split("\\s+");
        if (partes.length == 1) return partes[0].substring(0, 1).toUpperCase();
        return (partes[0].substring(0, 1) + partes[1].substring(0, 1)).toUpperCase();
    }
}