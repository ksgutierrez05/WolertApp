/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.view;

import java.util.List;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import sistemagestion.model.*;
import sistemagestion.service.UnidadPolicialService;

public class UnidadesAdminPoliciaView {

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

    private final UnidadPolicialService unidadService;
    private VBox tablaContainer;
    private TextField campoBusqueda;
    private List<UnidadPolicial> todasLasUnidades;
    private HBox statsContainer;

    public UnidadesAdminPoliciaView(UnidadPolicialService unidadService) {
        this.unidadService = unidadService;
    }

    public ScrollPane build() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color:" + BG + ";");

        try {
            todasLasUnidades = unidadService.listar();
        } catch (Exception e) {
            todasLasUnidades = List.of();
        }
        statsContainer = buildStats();
        content.getChildren().addAll(
                buildTopBar(),
                statsContainer,
                buildToolbar(),
                buildTabla()
        );

        renderizarLista(todasLasUnidades);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color:" + BG + "; -fx-background:" + BG + ";");
        return scroll;
    }

    // ── Top bar ───────────────────────────────────────────────────
    private HBox buildTopBar() {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);

        VBox titles = new VBox(4);
        Label title = new Label("Gestión de Unidades Policiales");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#111827"));
        Label sub = label("Administra las unidades policiales del sistema", 13, GRAY_TEXT, false);
        titles.getChildren().addAll(title, sub);

        Button btnNueva = new Button("＋  Nueva unidad");
        btnNueva.setPrefHeight(40);
        String base = "-fx-background-color: linear-gradient(to right, #16283d, #1f3a56);"
                + "-fx-text-fill:white;"
                + "-fx-font-size:13px;-fx-font-weight:bold;"
                + "-fx-background-radius:8;-fx-padding:8 18;-fx-cursor:hand;";
        String hover = "-fx-background-color: linear-gradient(to right, #0d1c2b, #162d45);"
                + "-fx-text-fill:white;"
                + "-fx-font-size:13px;-fx-font-weight:bold;"
                + "-fx-background-radius:8;-fx-padding:8 18;-fx-cursor:hand;";
        btnNueva.setStyle(base);
        btnNueva.setOnMouseEntered(e -> btnNueva.setStyle(hover));
        btnNueva.setOnMouseExited(e -> btnNueva.setStyle(base));
        btnNueva.setOnAction(e -> {
            MapaUnidadesPoliciales mapa = new MapaUnidadesPoliciales();
            Stage stage = mapa.mostrar();
            stage.setOnHidden(ev -> {
                try {
                    todasLasUnidades = unidadService.listar();
                    renderizarLista(todasLasUnidades);
                } catch (Exception ex) {
                    mostrarAlerta("Error", "No se pudo recargar: " + ex.getMessage());
                }
            });
        });

        HBox right = new HBox();
        right.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(right, Priority.ALWAYS);
        right.getChildren().add(btnNueva);

        bar.getChildren().addAll(titles, right);
        return bar;
    }

    // ── Stats ─────────────────────────────────────────────────────
    private HBox buildStats() {
        HBox row = new HBox(16);

        long total = todasLasUnidades.size();
        long operativas = todasLasUnidades.stream()
                .filter(u -> u.getEstado() == EstadoUnidadPolicial.OPERATIVA).count();
        long activas = todasLasUnidades.stream()
                .filter(u -> u.getEstado() == EstadoUnidadPolicial.ACTIVA).count();
        long inactivas = todasLasUnidades.stream()
                .filter(u -> u.getEstado() == EstadoUnidadPolicial.INACTIVA).count();

        row.getChildren().addAll(
                statCard(BLUE_LIGHT, BLUE, "\uf505", "Total unidades",
                        boldNum(String.valueOf(total), BLUE), "Registradas en el sistema"),
                statCard(GREEN_LIGHT, GREEN, "\uf058", "Operativas",
                        boldNum(String.valueOf(operativas), GREEN), "Listas para asignación"),
                statCard(ORANGE_LIGHT, ORANGE, "\uf017", "Activas",
                        boldNum(String.valueOf(activas), ORANGE), "Atendiendo alertas"),
                statCard("#f3f4f6", GRAY_TEXT, "\uf057", "Inactivas",
                        boldNum(String.valueOf(inactivas), GRAY_TEXT), "Fuera de servicio")
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
        campoBusqueda.setPromptText("Buscar por nombre o barrio...");
        campoBusqueda.setStyle("-fx-background-color:transparent;"
                + "-fx-border-color:transparent;"
                + "-fx-font-size:13px;-fx-text-fill:#111827;");
        campoBusqueda.setPrefHeight(42);
        HBox.setHgrow(campoBusqueda, Priority.ALWAYS);
        campoBusqueda.textProperty().addListener((obs, o, n) -> filtrarYMostrar());

        // Filtro por estado
        ComboBox<String> filtroEstado = new ComboBox<>();
        filtroEstado.getItems().addAll("Todos", "OPERATIVA", "ACTIVA", "INACTIVA");
        filtroEstado.setValue("Todos");
        filtroEstado.setPrefHeight(42);
        filtroEstado.setStyle("-fx-background-color:#f5f7fb;"
                + "-fx-border-color:transparent;"
                + "-fx-background-radius:10;-fx-font-size:13px;");
        filtroEstado.setOnAction(e -> {
            String val = filtroEstado.getValue();
            List<UnidadPolicial> filtradas = "Todos".equals(val)
                    ? todasLasUnidades
                    : todasLasUnidades.stream()
                            .filter(u -> u.getEstado() != null
                            && u.getEstado().name().equals(val))
                            .toList();
            renderizarLista(filtradas);
        });

        searchBox.getChildren().addAll(searchIcon, campoBusqueda);
        bar.getChildren().addAll(searchBox, filtroEstado);
        return bar;
    }

    // ── Tabla ─────────────────────────────────────────────────────
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

        // Nombre crece
        HBox nombreWrap = new HBox();
        HBox.setHgrow(nombreWrap, Priority.ALWAYS);
        nombreWrap.getChildren().add(colH("Nombre", true));

        header.getChildren().addAll(
                nombreWrap,
                colHFixed("Barrio", 160),
                colHFixed("Estado", 130),
                colHFixed("Lat / Lng", 180),
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
            renderizarLista(todasLasUnidades);
            return;
        }
        List<UnidadPolicial> filtradas = todasLasUnidades.stream()
                .filter(u -> (u.getNombre() != null
                && u.getNombre().toLowerCase().contains(txt))
                || (u.getBarrio() != null
                && u.getBarrio().getNombre().toLowerCase().contains(txt)))
                .toList();
        renderizarLista(filtradas);
    }

    // ── Renderizar ────────────────────────────────────────────────
    private void renderizarLista(List<UnidadPolicial> lista) {
        tablaContainer.getChildren().clear();

        if (lista.isEmpty()) {
            VBox vacio = new VBox(10);
            vacio.setAlignment(Pos.CENTER);
            vacio.setPadding(new Insets(40));
            Label icn = new Label("\uf505");
            icn.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';"
                    + "-fx-font-size:48px;-fx-text-fill:#d1d5db;");
            Label msg = label("No se encontraron unidades", 14, GRAY_TEXT, false);
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
                lbl.setText("Mostrando " + lista.size() + " unidad"
                        + (lista.size() != 1 ? "es" : ""));
            }
        }

        actualizarStats();
    }

    // ── Fila ──────────────────────────────────────────────────────
    private HBox buildFila(UnidadPolicial u, boolean par) {
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

        // ── Nombre con avatar (crece) ──────────────────────────────
        HBox celdaNombre = new HBox(10);
        celdaNombre.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(celdaNombre, Priority.ALWAYS);

        StackPane avatarBox = new StackPane();
        Circle avatar = new Circle(18, Color.web(colorAvatar(u.getNombre())));
        Label avatarLbl = label(iniciales(u.getNombre()), 11, WHITE, true);
        avatarBox.getChildren().addAll(avatar, avatarLbl);

        VBox nombreBox = new VBox(2);
        Label nombreLbl = new Label(u.getNombre() != null ? u.getNombre() : "—");
        nombreLbl.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#111827;");
        nombreBox.getChildren().addAll(nombreLbl);
        celdaNombre.getChildren().addAll(avatarBox, nombreBox);

        // ── Barrio (160px) ────────────────────────────────────────
        Label barrioLbl = celda(
                u.getBarrio() != null ? u.getBarrio().getNombre() : "—", 160);

        // ── Estado badge (130px) ──────────────────────────────────
        String estNom = u.getEstado() != null
                ? u.getEstado().name() : "—";
        String colorEst = colorEstado(u.getEstado());
        String bgEst = bgEstado(u.getEstado());
        Label estBadge = badge(estNom, bgEst, colorEst);
        HBox estBox = new HBox(estBadge);
        estBox.setAlignment(Pos.CENTER_LEFT);
        estBox.setPrefWidth(130);
        estBox.setMinWidth(130);
        estBox.setMaxWidth(130);

        // ── Coordenadas (180px) ───────────────────────────────────
        String coords = (u.getLatitud() != 0 || u.getLongitud() != 0)
                ? String.format("%.4f,%.4f", u.getLatitud(), u.getLongitud()) : "—";
        Label coordLbl = celda(coords, 180);

        // ── Acciones (160px) ─────────────────────────────────────
        HBox acciones = new HBox(6);
        acciones.setAlignment(Pos.CENTER_LEFT);
        acciones.setPrefWidth(160);
        acciones.setMinWidth(160);
        acciones.setMaxWidth(160);
        acciones.getChildren().addAll(
                btnAccion("\uf06e", BLUE, BLUE_LIGHT, "Ver", () -> verUnidad(u)),
                btnAccion("\uf044", ORANGE, ORANGE_LIGHT, "Editar", () -> editarUnidad(u)),
                btnAccion("\uf2ed", RED, RED_LIGHT, "Eliminar", () -> eliminarUnidad(u))
        );

        fila.getChildren().addAll(celdaNombre, barrioLbl, estBox, coordLbl, acciones);
        return fila;
    }

    // ── Ver ───────────────────────────────────────────────────────
    private void verUnidad(UnidadPolicial u) {
        MapaUnidadesPoliciales mapa = new MapaUnidadesPoliciales();
        mapa.mostrarSoloLectura(u);
    }

    // ── Editar ────────────────────────────────────────────────────
    private void editarUnidad(UnidadPolicial u) {
        MapaUnidadesPoliciales mapa = new MapaUnidadesPoliciales();
        Stage stage = mapa.mostrar();

        // Platform.runLater garantiza que corre DESPUÉS de que el Stage ya está montado
        Platform.runLater(() -> mapa.cargarUnidadPublico(u));

        stage.setOnHidden(e -> {
            try {
                todasLasUnidades = unidadService.listar();
                renderizarLista(todasLasUnidades);
            } catch (Exception ex) {
                mostrarAlerta("Error", "No se pudo recargar la lista: " + ex.getMessage());
            }
        });
    }

    // ── Eliminar ──────────────────────────────────────────────────
    private void eliminarUnidad(UnidadPolicial u) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Eliminar unidad");
        confirm.setHeaderText("¿Eliminar \"" + u.getNombre() + "\"?");
        confirm.setContentText("Esta acción no se puede deshacer.");

        ButtonType btnSi = new ButtonType("Sí, eliminar", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnNo = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirm.getButtonTypes().setAll(btnSi, btnNo);

        confirm.showAndWait().ifPresent(resp -> {
            if (resp == btnSi) {
                try {
                    unidadService.eliminar(u.getNombre());
                    todasLasUnidades = unidadService.listar();
                    renderizarLista(todasLasUnidades);
                    mostrarInfo("Eliminada", "Unidad eliminada correctamente.");
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

    private Label badge(String texto, String bg, String color) {
        Label l = label(texto, 11, color, true);
        l.setPadding(new Insets(3, 9, 3, 9));
        l.setStyle("-fx-background-color:" + bg + ";-fx-background-radius:20;"
                + "-fx-text-fill:" + color + ";-fx-font-weight:bold;-fx-font-size:11px;");
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

    private String colorEstado(EstadoUnidadPolicial e) {
        if (e == null) {
            return GRAY_TEXT;
        }
        return switch (e) {
            case OPERATIVA ->
                GREEN;
            case ACTIVA ->
                ORANGE;
            case INACTIVA ->
                GRAY_TEXT;
        };
    }

    private String bgEstado(EstadoUnidadPolicial e) {
        if (e == null) {
            return "#f3f4f6";
        }
        return switch (e) {
            case OPERATIVA ->
                GREEN_LIGHT;
            case ACTIVA ->
                ORANGE_LIGHT;
            case INACTIVA ->
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
