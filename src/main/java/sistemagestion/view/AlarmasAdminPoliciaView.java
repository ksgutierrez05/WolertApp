/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.view;

import java.util.List;
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
import sistemagestion.service.AlarmaService;

public class AlarmasAdminPoliciaView {

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

    private final AlarmaService alarmaService;
    private VBox tablaContainer;
    private TextField campoBusqueda;
    private List<Alarma> todasLasAlarmas;

    public AlarmasAdminPoliciaView(AlarmaService alarmaService) {
        this.alarmaService = alarmaService;
    }

    public ScrollPane build() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color:" + BG + ";");

        try {
            todasLasAlarmas = alarmaService.listar();
        } catch (Exception e) {
            todasLasAlarmas = List.of();
        }

        content.getChildren().addAll(
                buildTopBar(),
                buildStats(),
                buildToolbar(),
                buildTabla()
        );

        renderizarLista(todasLasAlarmas);

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
        Label title = new Label("Gestión de Alarmas");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#111827"));
        Label sub = label("Administra las alarmas registradas en el sistema", 13, GRAY_TEXT, false);
        titles.getChildren().addAll(title, sub);

        Button btnNueva = new Button("＋  Nueva alarma");
        btnNueva.setPrefHeight(40);
        String base = "-fx-background-color:" + BLUE + ";-fx-text-fill:white;"
                + "-fx-font-size:13px;-fx-font-weight:bold;"
                + "-fx-background-radius:8;-fx-padding:8 18;-fx-cursor:hand;";
        String hover = "-fx-background-color:#0d47a1;-fx-text-fill:white;"
                + "-fx-font-size:13px;-fx-font-weight:bold;"
                + "-fx-background-radius:8;-fx-padding:8 18;-fx-cursor:hand;";
        btnNueva.setStyle(base);
        btnNueva.setOnMouseEntered(e -> btnNueva.setStyle(hover));
        btnNueva.setOnMouseExited(e -> btnNueva.setStyle(base));
        btnNueva.setOnAction(e -> {
            MapaAlarmas mapa = new MapaAlarmas();
            mapa.mostrar();
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

        long total = todasLasAlarmas.size();
        long activas = todasLasAlarmas.stream()
                .filter(a -> a.getEstado() == EstadoAlarma.ACTIVA).count();
        long inactivas = todasLasAlarmas.stream()
                .filter(a -> a.getEstado() == EstadoAlarma.INACTIVA).count();
        long mantenimiento = todasLasAlarmas.stream()
                .filter(a -> a.getEstado() == EstadoAlarma.EN_MANTENIMIENTO).count();

        row.getChildren().addAll(
                statCard(BLUE_LIGHT, BLUE, "\uf0f3", "Total alarmas",
                        boldNum(String.valueOf(total), BLUE), "Registradas en el sistema"),
                statCard(GREEN_LIGHT, GREEN, "\uf058", "Activas",
                        boldNum(String.valueOf(activas), GREEN), "Funcionando correctamente"),
                statCard("#f3f4f6", GRAY_TEXT, "\uf057", "Inactivas",
                        boldNum(String.valueOf(inactivas), GRAY_TEXT), "Fuera de servicio"),
                statCard(ORANGE_LIGHT, ORANGE, "\uf0ad", "En mantenimiento",
                        boldNum(String.valueOf(mantenimiento), ORANGE), "En revisión técnica")
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
        iconLbl.setStyle(
                "-fx-font-family:'Font Awesome 6 Free Solid';"
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
        searchBox.setStyle("-fx-background-color:#f5f7fb;-fx-background-radius:10;-fx-padding:0 14;");
        searchBox.setPrefHeight(42);
        HBox.setHgrow(searchBox, Priority.ALWAYS);

        Label searchIcon = new Label("\uf002");
        searchIcon.setStyle(
                "-fx-font-family:'Font Awesome 6 Free Solid';"
                + "-fx-font-size:14px;-fx-text-fill:#9ca3af;");

        campoBusqueda = new TextField();
        campoBusqueda.setPromptText("Buscar por nombre o barrio...");
        campoBusqueda.setStyle(
                "-fx-background-color:transparent;-fx-border-color:transparent;"
                + "-fx-font-size:13px;-fx-text-fill:#111827;");
        campoBusqueda.setPrefHeight(42);
        HBox.setHgrow(campoBusqueda, Priority.ALWAYS);
        campoBusqueda.textProperty().addListener((obs, o, n) -> filtrarYMostrar());

        // Filtro por estado
        ComboBox<String> filtroEstado = new ComboBox<>();
        filtroEstado.getItems().addAll("Todos", "ACTIVA", "INACTIVA", "EN_MANTENIMIENTO");
        filtroEstado.setValue("Todos");
        filtroEstado.setPrefHeight(42);
        filtroEstado.setStyle(
                "-fx-background-color:#f5f7fb;-fx-border-color:transparent;"
                + "-fx-background-radius:10;-fx-font-size:13px;");
        filtroEstado.setOnAction(e -> {
            String val = filtroEstado.getValue();
            List<Alarma> filtradas = "Todos".equals(val)
                    ? todasLasAlarmas
                    : todasLasAlarmas.stream()
                            .filter(a -> a.getEstado() != null
                            && a.getEstado().name().equals(val))
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

        // Cabecera de columnas
        HBox header = new HBox(0);
        header.setPadding(new Insets(12, 16, 12, 16));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle(
                "-fx-background-color:#f8fafc;-fx-background-radius:12 12 0 0;"
                + "-fx-border-color:transparent transparent " + BORDER + " transparent;"
                + "-fx-border-width:0 0 1 0;");

        
        HBox nombreWrap = new HBox();
        HBox.setHgrow(nombreWrap, Priority.ALWAYS);
        nombreWrap.getChildren().add(colH("Nombre", true));

        header.getChildren().addAll(
                nombreWrap,
                colHFixed("Barrio", 160),
                colHFixed("Estado", 130),
                colHFixed("Radio (m)", 100),
                colHFixed("Coordenadas", 170),
                colHFixed("Acciones", 190)
        );
        card.getChildren().add(header);

        tablaContainer = new VBox(0);
        card.getChildren().add(tablaContainer);

        // Footer
        HBox footer = new HBox();
        footer.setPadding(new Insets(10, 16, 10, 16));
        footer.setStyle(
                "-fx-background-color:#f8fafc;-fx-background-radius:0 0 12 12;"
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
            renderizarLista(todasLasAlarmas);
            return;
        }
        List<Alarma> filtradas = todasLasAlarmas.stream()
                .filter(a -> (a.getNombre() != null && a.getNombre().toLowerCase().contains(txt))
                || (a.getBarrio() != null && a.getBarrio().getNombre().toLowerCase().contains(txt)))
                .toList();
        renderizarLista(filtradas);
    }

    // ── Renderizar ────────────────────────────────────────────────
    private void renderizarLista(List<Alarma> lista) {
        tablaContainer.getChildren().clear();

        if (lista.isEmpty()) {
            VBox vacio = new VBox(10);
            vacio.setAlignment(Pos.CENTER);
            vacio.setPadding(new Insets(40));
            Label icn = new Label("\uf0f3");
            icn.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';"
                    + "-fx-font-size:48px;-fx-text-fill:#d1d5db;");
            Label msg = label("No se encontraron alarmas", 14, GRAY_TEXT, false);
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
                lbl.setText("Mostrando " + lista.size() + " alarma"
                        + (lista.size() != 1 ? "s" : ""));
            }
        }
    }

    // ── Fila ──────────────────────────────────────────────────────
    private HBox buildFila(Alarma a, boolean par) {
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
        Circle avatar = new Circle(18, Color.web(colorAvatar(a.getNombre())));
        Label avatarLbl = label(iniciales(a.getNombre()), 11, WHITE, true);
        avatarBox.getChildren().addAll(avatar, avatarLbl);

        VBox nombreBox = new VBox(2);
        Label nombreLbl = new Label(a.getNombre() != null ? a.getNombre() : "—");
        nombreLbl.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#111827;");
        Label idLbl = label("ID: " + a.getId_alarma(), 10, GRAY_TEXT, false);
        nombreBox.getChildren().addAll(nombreLbl, idLbl);
        celdaNombre.getChildren().addAll(avatarBox, nombreBox);

        // ── Barrio (160px) ────────────────────────────────────────
        Label barrioLbl = celda(
                a.getBarrio() != null ? a.getBarrio().getNombre() : "—", 160);

        // ── Estado badge (130px) ──────────────────────────────────
        String estNom = a.getEstado() != null
                ? a.getEstado().name().replace("_", " ") : "—";
        String colorEst = colorEstado(a.getEstado());
        String bgEst = bgEstado(a.getEstado());
        Label estBadge = badge(estNom, bgEst, colorEst);
        HBox estBox = new HBox(estBadge);
        estBox.setAlignment(Pos.CENTER_LEFT);
        estBox.setPrefWidth(130);
        estBox.setMinWidth(130);
        estBox.setMaxWidth(130);

        // ── Radio (100px) ─────────────────────────────────────────
        Label radioLbl = celda((int) a.getRadio_cobertura() + " m", 100);

        // ── Coordenadas (170px) ───────────────────────────────────
        String coords = (a.getLatitud() != 0 || a.getLongitud() != 0)
                ? String.format("%.4f,%.4f", a.getLatitud(), a.getLongitud()) : "—";
        Label coordLbl = celda(coords, 170);

        // ── Acciones (190px) ─────────────────────────────────────
        HBox acciones = new HBox(6);
        acciones.setAlignment(Pos.CENTER_LEFT);
        acciones.setPrefWidth(190);
        acciones.setMinWidth(190);
        acciones.setMaxWidth(190);
        acciones.getChildren().addAll(
                btnAccion("\uf06e", BLUE, BLUE_LIGHT, "Ver", () -> verAlarma(a)),
                btnAccion("\uf044", ORANGE, ORANGE_LIGHT, "Editar", () -> editarAlarma(a)),
                btnAccion("\uf2ed", RED, RED_LIGHT, "Eliminar", () -> eliminarAlarma(a))
        );

        fila.getChildren().addAll(celdaNombre, barrioLbl, estBox, radioLbl, coordLbl, acciones);
        return fila;
    }

    // ── Ver detalle ───────────────────────────────────────────────
    private void verAlarma(Alarma a) {
        Dialog<Void> dlg = new Dialog<>();
        dlg.setTitle("Detalle de alarma");
        dlg.setHeaderText(null);

        VBox content = new VBox(12);
        content.setPadding(new Insets(20));
        content.setPrefWidth(400);

        Circle av = new Circle(32, Color.web(colorAvatar(a.getNombre())));
        Label avLbl = label(iniciales(a.getNombre()), 18, WHITE, true);
        StackPane avBox = new StackPane(av, avLbl);
        Label nomDlg = new Label(a.getNombre() != null ? a.getNombre() : "—");
        nomDlg.setFont(Font.font("System", FontWeight.BOLD, 17));
        nomDlg.setTextFill(Color.web("#111827"));

        // Badge estado
        Label estBadge = badge(
                a.getEstado() != null ? a.getEstado().name().replace("_", " ") : "—",
                bgEstado(a.getEstado()), colorEstado(a.getEstado()));
        estBadge.setFont(Font.font("System", FontWeight.BOLD, 13));

        VBox header = new VBox(6);
        header.setAlignment(Pos.CENTER);
        header.getChildren().addAll(avBox, nomDlg, estBadge);

        content.getChildren().addAll(
                header, new Separator(),
                detRow("🔔 Nombre", a.getNombre() != null ? a.getNombre() : "—"),
                detRow("🏘 Barrio", a.getBarrio() != null ? a.getBarrio().getNombre() : "—"),
                detRow("📡 Radio", (int) a.getRadio_cobertura() + " metros"),
                detRow("📍 Latitud", String.format("%.6f", a.getLatitud())),
                detRow("📍 Longitud", String.format("%.6f", a.getLongitud()))
        );

        // Botón ver en mapa
        Button btnMapa = new Button("\uf3c5  Ver en mapa");
        btnMapa.setStyle("-fx-font-family:'Font Awesome 6 Free Solid',System;"
                + "-fx-background-color:" + BLUE_LIGHT + ";-fx-text-fill:" + BLUE + ";"
                + "-fx-font-weight:bold;-fx-background-radius:8;"
                + "-fx-padding:8 16;-fx-cursor:hand;");
        btnMapa.setOnAction(e -> {
            dlg.close();
            visualizacionAlarmas(a);
        });
        content.getChildren().addAll(new Separator(), btnMapa);

        dlg.getDialogPane().setContent(content);
        dlg.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dlg.showAndWait();
    }

    // ── Editar ────────────────────────────────────────────────────
    private void editarAlarma(Alarma a) {
        abrirEnMapaEdicion(a);
    }

    private void visualizacionAlarmas(Alarma a) {
        MapaAlarmasRegistradas mapa = new MapaAlarmasRegistradas();
        mapa.mostrar();
    }

    private void abrirEnMapaEdicion(Alarma a) {
        MapaAlarmasRegistada mapa = new MapaAlarmasRegistada();
        Stage stage = mapa.mostrar(); // ← mostrar() debe retornar Stage
        javafx.application.Platform.runLater(() -> mapa.cargarAlarmaPublico(a));

        stage.setOnHidden(e -> {
            try {
                todasLasAlarmas = alarmaService.listar();
                renderizarLista(todasLasAlarmas);
            } catch (Exception ex) {
                mostrarAlerta("Error", "No se pudo recargar: " + ex.getMessage());
            }
        });

    }

    // ── Eliminar ──────────────────────────────────────────────────
    private void eliminarAlarma(Alarma a) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Eliminar alarma");
        confirm.setHeaderText("¿Eliminar \"" + a.getNombre() + "\"?");
        confirm.setContentText("Esta acción no se puede deshacer.");

        ButtonType btnSi = new ButtonType("Sí, eliminar", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnNo = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirm.getButtonTypes().setAll(btnSi, btnNo);

        confirm.showAndWait().ifPresent(resp -> {
            if (resp == btnSi) {
                boolean ok = alarmaService.eliminar(a.getId_alarma());
                if (ok) {
                    try {
                        todasLasAlarmas = alarmaService.listar();
                    } catch (Exception ex) {
                        todasLasAlarmas = List.of();
                    }
                    renderizarLista(todasLasAlarmas);
                    mostrarInfo("Eliminada", "Alarma eliminada correctamente.");
                } else {
                    mostrarAlerta("Error", "No se pudo eliminar la alarma.");
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
                + "-fx-font-size:13px;-fx-background-radius:8;-fx-padding:7 10;-fx-cursor:hand;";
        String hov = "-fx-background-color:" + iconColor + ";-fx-text-fill:white;"
                + "-fx-font-family:'Font Awesome 6 Free Solid';"
                + "-fx-font-size:13px;-fx-background-radius:8;-fx-padding:7 10;-fx-cursor:hand;";
        b.setStyle(base);
        b.setOnMouseEntered(e -> b.setStyle(hov));
        b.setOnMouseExited(e -> b.setStyle(base));
        b.setOnAction(e -> accion.run());
        Tooltip.install(b, new Tooltip(tooltip));
        return b;
    }

    private HBox detRow(String campo, String valor) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        Label k = label(campo, 13, GRAY_TEXT, false);
        k.setMinWidth(130);
        row.getChildren().addAll(k, label(valor != null ? valor : "—", 13, "#111827", false));
        return row;
    }

    private String colorEstado(EstadoAlarma e) {
        if (e == null) {
            return GRAY_TEXT;
        }
        return switch (e) {
            case ACTIVA ->
                GREEN;
            case EN_MANTENIMIENTO ->
                ORANGE;
            case INACTIVA ->
                GRAY_TEXT;
        };
    }

    private String bgEstado(EstadoAlarma e) {
        if (e == null) {
            return "#f3f4f6";
        }
        return switch (e) {
            case ACTIVA ->
                GREEN_LIGHT;
            case EN_MANTENIMIENTO ->
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
}
