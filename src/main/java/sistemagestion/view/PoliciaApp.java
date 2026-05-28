/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.view;

/**
 *
 * @author Maria Cristina
 */
import java.awt.image.BufferedImage;
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
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import javafx.scene.image.ImageView;

import sistemagestion.model.*;
import sistemagestion.service.*;

/**
 * Dashboard principal para el rol Policía. Misma paleta y estructura que
 * AdministradorPoliciaApp.
 *
 * Funcionalidades: - Centro de operaciones: métricas propias + alertas
 * asignadas - Mis Alertas: alertas que le pertenecen, con cambio de estado y
 * descripción de situación - Mis Atenciones: atenciones registradas por él,
 * cambiar estado y describir situación - Alarmas: listado de alarmas, puede
 * cambiar estado - Historial: atenciones finalizadas o canceladas - Mapa por
 * zonas: simulación visual de zonas con alertas - Perfil / Configuración: datos
 * personales
 *
 * @author generado para WolertApp
 */
public class PoliciaApp {

    // ── Paleta ────────────────────────────────────────────────────
    private static final String WHITE = "#ffffff";
    private static final String BG = "#f4f6fb";
    private static final String RED = "#e53935";
    private static final String RED_LIGHT = "#fff0f0";
    private static final String ORANGE = "#fb8c00";
    private static final String ORANGE_LIGHT = "#fff3e0";
    private static final String GREEN = "#43a047";
    private static final String GREEN_LIGHT = "#e8f5e9";
    private static final String BLUE = "#1565c0";
    private static final String BLUE_LIGHT = "#e8f0fe";
    private static final String GRAY_TEXT = "#6b7280";
    private static final String BORDER = "#e5e7eb";

    // ── Servicios ─────────────────────────────────────────────────
    private AlertaService alertaService;
    private AtencionAlertaService atencionService;
    private AlarmaService alarmaService;
    private NotificacionService notificacionService;
    private AsignacionUnidadService asignacionService;
    private PoliciaService policiaService;

    // ── Usuario logueado ──────────────────────────────────────────
    private final Usuario usuarioActual;
    private Policia policiaActual;

    // ── UI ────────────────────────────────────────────────────────
    private BorderPane root;
    private VBox nav;

    // ── Constructor ───────────────────────────────────────────────
    public PoliciaApp(Usuario usuarioActual) {
        this.usuarioActual = usuarioActual;
        try {
            alertaService = new AlertaService();
            atencionService = new AtencionAlertaService();
            alarmaService = new AlarmaService();
            notificacionService = new NotificacionService();
            asignacionService = new AsignacionUnidadService();
            policiaService = new PoliciaService();

            // Buscar datos completos del policía
            if (usuarioActual != null) {
                List<Policia> todos = policiaService.listar();
                for (Policia p : todos) {
                    if (usuarioActual.getIdentificacion() != null
                            && usuarioActual.getIdentificacion().equals(p.getIdentificacion())) {
                        policiaActual = p;
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            mostrarAlerta("Error de conexión", e.getMessage());
        }
    }

    // =========================================================================
    // SHOW
    // =========================================================================
    public void show(Stage stage) {
        root = new BorderPane();
        root.setLeft(buildSidebar());
        root.setCenter(buildMainContent());
        root.setStyle("-fx-background-color: " + BG + ";");
        Scene scene = new Scene(root, 1100, 650);
        stage.setTitle("WolertApp – Policía");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();
    }

    // =========================================================================
    // SIDEBAR
    // =========================================================================
    private VBox buildSidebar() {
        VBox sidebar = new VBox();
        sidebar.setPrefWidth(240);
        sidebar.setMinWidth(240);
        sidebar.setMaxWidth(240);
        sidebar.setStyle("-fx-background-color: linear-gradient(to right, #16283d, #1f3a56);");
        VBox.setVgrow(sidebar, Priority.ALWAYS);
        sidebar.setMaxHeight(Double.MAX_VALUE);

        // Logo
        HBox logoBox = new HBox(10);
        logoBox.setPadding(new Insets(20, 16, 20, 16));
        logoBox.setAlignment(Pos.CENTER_LEFT);
        StackPane wolfIcon = new StackPane();
        Circle iconCircle = new Circle(22, Color.web("#2a3560"));
        wolfIcon.getChildren().addAll(iconCircle, label("🐺", 18, WHITE, false));
        VBox logoText = new VBox(2);
        logoText.getChildren().addAll(
                label("WolertApp", 15, WHITE, true),
                label("Portal Policía", 9, "#8899bb", false));
        logoBox.getChildren().addAll(wolfIcon, logoText);

        // Tarjeta de perfil
        HBox profileCard = buildProfileCard();

        // Nav
        nav = new VBox(2);
        nav.setPadding(new Insets(12, 8, 12, 8));
        nav.getChildren().addAll(
                navItem("🏠", "Centro de operaciones"),
                navItem("🚨", "Mis alertas"),
                navItem("📋", "Mis atenciones"),
                navItem("🔔", "Alarmas"),
                navItem("📜", "Historial"),
                navItem("🗺", "Mapa por zonas"),
                navItem("📢", "Notificaciones"),
                navItem("⚙", "Mi perfil")
        );

        VBox spacer = new VBox();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Logout
        HBox logout = new HBox(10);
        logout.setPadding(new Insets(14, 16, 18, 16));
        logout.setAlignment(Pos.CENTER_LEFT);
        logout.setCursor(javafx.scene.Cursor.HAND);
        logout.setStyle("-fx-background-color: transparent;");
        logout.setOnMouseEntered(e -> logout.setStyle("-fx-background-color: rgba(229,57,53,0.15); -fx-background-radius: 8;"));
        logout.setOnMouseExited(e -> logout.setStyle("-fx-background-color: transparent;"));
        logout.setOnMouseClicked(e -> cerrarSesion());
        logout.getChildren().addAll(
                label("🚪", 15, RED, false),
                label("Cerrar sesión", 13, RED, true));

        sidebar.getChildren().addAll(logoBox, profileCard, nav, spacer, logout);
        return sidebar;
    }

    private HBox buildProfileCard() {
        HBox card = new HBox(10);
        card.setPadding(new Insets(10, 16, 10, 16));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 12;");

        Circle av = new Circle(20, Color.web("#334155"));
        StackPane avBox = new StackPane(av, label("👮", 15, WHITE, false));

        VBox info = new VBox(2);
        String nombre = usuarioActual != null
                ? trim(usuarioActual.getPrimer_nombre()) + " " + trim(usuarioActual.getPrimer_apellido())
                : "Policía";
        String rango = policiaActual != null && policiaActual.getRango() != null
                ? policiaActual.getRango() : "Oficial";
        String placa = policiaActual != null && policiaActual.getPlaca() != null
                ? "Placa: " + policiaActual.getPlaca() : "";

        // ── Unidad: muestra nombre o "Unidad no asignada" ──
        String unidad = policiaActual != null && policiaActual.getUnidadpolicial() != null
                && policiaActual.getUnidadpolicial().getNombre() != null
                ? "🚓 " + policiaActual.getUnidadpolicial().getNombre()
                : "🚓 Unidad no asignada";
        String colorUnidad = policiaActual != null && policiaActual.getUnidadpolicial() != null
                ? "#60a5fa" : ORANGE;

        HBox statusRow = new HBox(4);
        statusRow.setAlignment(Pos.CENTER_LEFT);
        statusRow.getChildren().addAll(new Circle(4, Color.web(GREEN)), label("En servicio", 10, GREEN, false));

        info.getChildren().addAll(
                label(nombre.trim(), 12, WHITE, true),
                label(rango, 9, "#8899bb", false),
                label(placa, 9, "#6a8cbb", false),
                label(unidad, 9, colorUnidad, true), // ← unidad aquí
                statusRow);
        card.getChildren().addAll(avBox, info);
        return card;
    }

    // ── Nav item ─────────────────────────────────────────────────
    private HBox navItem(String icon, String text) {
        HBox item = new HBox(10);
        item.setPadding(new Insets(9, 12, 9, 12));
        item.setAlignment(Pos.CENTER_LEFT);
        item.setCursor(javafx.scene.Cursor.HAND);
        item.setMaxWidth(Double.MAX_VALUE);
        item.setStyle("-fx-background-radius: 8;");
        item.setOnMouseEntered(e -> item.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-background-radius: 8;"));
        item.setOnMouseExited(e -> item.setStyle("-fx-background-radius: 8;"));
        item.getChildren().addAll(label(icon, 14, WHITE, false), label(text, 13, "#f8fafc", true));
        item.setOnMouseClicked(e -> {
            switch (text) {
                case "Centro de operaciones" ->
                    root.setCenter(buildMainContent());
                case "Mis alertas" ->
                    root.setCenter(buildMisAlertasView());
                case "Mis atenciones" ->
                    root.setCenter(buildMisAtencionesView());
                case "Alarmas" ->
                    root.setCenter(buildAlarmasView());
                case "Historial" ->
                    root.setCenter(buildHistorialView());
                case "Mapa por zonas" ->
                    root.setCenter(buildMapaZonasView());
                case "Notificaciones" ->
                    root.setCenter(buildNotificacionesView());
                case "Mi perfil" ->
                    root.setCenter(buildPerfilView());
                default ->
                    root.setCenter(buildMainContent());
            }
        });
        return item;
    }

    // =========================================================================
    // MAIN CONTENT (Centro de operaciones)
    // =========================================================================
    private ScrollPane buildMainContent() {
        VBox content = new VBox(18);
        content.setPadding(new Insets(22));
        content.setStyle("-fx-background-color: " + BG + ";");
        content.getChildren().addAll(
                buildTopBar(),
                buildStats(),
                buildCenterPanels(),
                buildBottomPanels(),
                buildCompanerosPanel());
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: " + BG + "; -fx-background: " + BG + ";");
        return scroll;
    }

    private HBox buildTopBar() {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);

        String saludo = usuarioActual != null && usuarioActual.getPrimer_nombre() != null
                ? "¡Bienvenido, " + usuarioActual.getPrimer_nombre() + "!"
                : "¡Bienvenido!";
        VBox greeting = new VBox(3);
        Label hello = new Label(saludo);
        hello.setFont(Font.font("System", FontWeight.BOLD, 24));
        hello.setTextFill(Color.web("#111827"));
        String rangoSub = policiaActual != null
                ? (policiaActual.getRango() != null ? policiaActual.getRango() : "Policía")
                + (policiaActual.getUnidadpolicial() != null
                ? " — " + policiaActual.getUnidadpolicial().getNombre() : "")
                : "Portal policial — WolertApp";
        greeting.getChildren().addAll(hello, label(rangoSub, 12, GRAY_TEXT, false));

        HBox rightBox = new HBox(16);
        rightBox.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(rightBox, Priority.ALWAYS);

        VBox dateBox = new VBox(2);
        dateBox.setAlignment(Pos.CENTER_RIGHT);
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", new Locale("es", "CO"));
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("hh:mm:ss a", new Locale("es", "CO"));
        LocalDateTime now0 = LocalDateTime.now(ZoneId.of("America/Bogota"));
        Label dateLbl = label("📅  " + now0.format(dateFmt), 12, "#374151", false);
        Label timeLbl = label(now0.format(timeFmt), 12, GRAY_TEXT, false);
        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), ev -> {
            LocalDateTime now = LocalDateTime.now(ZoneId.of("America/Bogota"));
            dateLbl.setText("📅  " + now.format(dateFmt));
            timeLbl.setText(now.format(timeFmt));
        }));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();
        dateBox.getChildren().addAll(dateLbl, timeLbl);

        int notiCount = contarNotificaciones();
        StackPane bell = new StackPane();
        Label bellIcon = label("🔔", 18, "#374151", false);
        if (notiCount > 0) {
            Circle badge = new Circle(7, Color.web(RED));
            badge.setTranslateX(9);
            badge.setTranslateY(-9);
            Label badgeNum = label(String.valueOf(notiCount), 8, WHITE, true);
            badgeNum.setTranslateX(9);
            badgeNum.setTranslateY(-9);
            bell.getChildren().addAll(bellIcon, badge, badgeNum);
        } else {
            bell.getChildren().add(bellIcon);
        }

        rightBox.getChildren().addAll(dateBox, bell);
        bar.getChildren().addAll(greeting, rightBox);
        return bar;
    }

    private HBox buildStats() {
        HBox row = new HBox(16);
        row.getChildren().addAll(
                statCard(RED_LIGHT, RED, "Mis alertas activas", contarMisAlertasActivas(), "PENDIENTE / EN ATENCIÓN", "IncidentesPin"),
                statCard(ORANGE_LIGHT, ORANGE, "Mis atenciones", contarMisAtenciones(), "Total registradas", "AlertasPendientesPin"),
                statCard(GREEN_LIGHT, GREEN, "Alarmas activas", contarAlarmasActivas(), "ACTIVA / EN MANTENIMIENTO", "AlarmaPin"),
                statCard(BLUE_LIGHT, BLUE, "Notificaciones", contarNotificaciones(), "Total recibidas", "NotificacionPin")
        );
        return row;
    }

    private HBox buildCenterPanels() {
        HBox row = new HBox(16);
        row.getChildren().addAll(buildMisAlertasPanel(), buildMapaMiniPanel());
        return row;
    }

    private VBox buildMisAlertasPanel() {
        VBox card = createPanel("🚨 Mis alertas recientes");
        HBox.setHgrow(card, Priority.ALWAYS);
        try {
            List<Alerta> alertas = obtenerMisAlertas();
            if (alertas.isEmpty()) {
                card.getChildren().add(label("No tienes alertas asignadas", 13, GRAY_TEXT, false));
            } else {
                alertas.stream()
                        .filter(a -> a.getFechaHora() != null)
                        .sorted((a, b) -> b.getFechaHora().compareTo(a.getFechaHora()))
                        .limit(5)
                        .forEach(a -> {
                            String dotColor = estadoColor(a.getEstado());
                            String tipo = a.getTipoalerta() != null ? a.getTipoalerta().getNombre() : "Alerta";
                            String barr = a.getBarrio() != null ? " — " + a.getBarrio().getNombre() : "";
                            String sub = formatFecha(a.getFechaHora())
                                    + (a.getEstado() != null ? " · " + a.getEstado().name().replace("_", " ") : "");
                            card.getChildren().addAll(alertItem("🔔", tipo + barr, sub, dotColor), separator());
                        });
            }
        } catch (Exception e) {
            card.getChildren().add(label("Error: " + e.getMessage(), 12, RED, false));
        }
        return card;
    }

    private VBox buildMapaMiniPanel() {
        VBox card = createPanel("🗺 Mapa de mi zona");
        card.setPrefWidth(340);
        StackPane mapArea = new StackPane();
        mapArea.setPrefHeight(200);
        Rectangle mapBg = new Rectangle();
        mapBg.setFill(Color.web("#d1e8d1"));
        mapBg.widthProperty().bind(mapArea.widthProperty());
        mapBg.heightProperty().bind(mapArea.heightProperty());
        mapBg.setArcWidth(10);
        mapBg.setArcHeight(10);

        javafx.scene.layout.Pane streets = new javafx.scene.layout.Pane();
        streets.setPrefSize(300, 180);
        for (int i = 0; i < 3; i++) {
            Rectangle h = new Rectangle(300, 2);
            h.setFill(Color.web("#b8d4b8"));
            h.setY(40 + i * 50);
            streets.getChildren().add(h);
        }
        for (int i = 0; i < 4; i++) {
            Rectangle v = new Rectangle(2, 180);
            v.setFill(Color.web("#b8d4b8"));
            v.setX(40 + i * 65);
            streets.getChildren().add(v);
        }
        streets.getChildren().addAll(
                mapDot(70, 50, RED), mapDot(180, 35, ORANGE),
                mapDot(260, 50, GREEN), mapDot(55, 130, GREEN),
                mapDot(270, 140, RED));

        VBox popup = new VBox(4);
        popup.setPadding(new Insets(8, 12, 8, 12));
        popup.setStyle("-fx-background-color: white; -fx-background-radius: 8;");
        popup.setEffect(new DropShadow(6, Color.web("#0000001a")));
        popup.setTranslateX(15);
        popup.setTranslateY(8);
        popup.getChildren().addAll(
                label("Mi zona de patrullaje", 11, "#374151", true),
                label("Ver zonas activas →", 10, BLUE, false));
        popup.setCursor(javafx.scene.Cursor.HAND);
        popup.setOnMouseClicked(e -> root.setCenter(buildMapaZonasView()));
        mapArea.getChildren().addAll(mapBg, streets, popup);

        HBox legend = new HBox(14);
        legend.setAlignment(Pos.CENTER);
        legend.getChildren().addAll(
                legendItem(RED, "Con alerta"),
                legendItem(ORANGE, "En revisión"),
                legendItem(GREEN, "Zona segura"));
        card.getChildren().addAll(mapArea, legend);
        return card;
    }

    private HBox buildBottomPanels() {
        HBox row = new HBox(16);

        // Atenciones recientes
        VBox atenciones = createPanel("📋 Mis atenciones recientes");
        HBox.setHgrow(atenciones, Priority.ALWAYS);
        try {
            List<AtencionAlerta> lista = obtenerMisAtenciones();
            if (lista.isEmpty()) {
                atenciones.getChildren().add(label("Sin atenciones registradas", 12, GRAY_TEXT, false));
            } else {
                lista.stream().limit(4).forEach(a -> {
                    String estadoStr = a.getEstado() != null ? a.getEstado().name().replace("_", " ") : "—";
                    String desc = a.getDescripcion() != null ? a.getDescripcion() : "—";
                    String color = switch (a.getEstado() != null ? a.getEstado() : EstadoAtencionAlerta.PENDIENTE) {
                        case FINALIZADA ->
                            GREEN;
                        case EN_PROCESO ->
                            ORANGE;
                        case CANCELADA ->
                            GRAY_TEXT;
                        default ->
                            RED;
                    };
                    atenciones.getChildren().addAll(listItem("📋", estadoStr, desc, color), separator());
                });
            }
        } catch (Exception e) {
            atenciones.getChildren().add(label("Error: " + e.getMessage(), 12, RED, false));
        }

        // Alarmas
        VBox alarmas = createPanel("🔔 Alarmas en mi barrio");
        HBox.setHgrow(alarmas, Priority.ALWAYS);
        try {
            List<Alarma> lista = alarmaService.listar();
            if (lista.isEmpty()) {
                alarmas.getChildren().add(label("Sin alarmas registradas", 12, GRAY_TEXT, false));
            } else {
                lista.stream().limit(4).forEach(a -> {
                    String estadoStr = a.getEstado() != null ? a.getEstado().name().replace("_", " ") : "—";
                    String color = a.getEstado() == EstadoAlarma.ACTIVA ? GREEN
                            : a.getEstado() == EstadoAlarma.EN_MANTENIMIENTO ? ORANGE : GRAY_TEXT;
                    alarmas.getChildren().addAll(listItem("🔔", a.getNombre(), estadoStr, color), separator());
                });
            }
        } catch (Exception e) {
            alarmas.getChildren().add(label("Error: " + e.getMessage(), 12, RED, false));
        }

        // Acciones rápidas
        VBox acciones = createPanel("⚡ Acciones rápidas");
        HBox.setHgrow(acciones, Priority.ALWAYS);
        HBox row1 = new HBox(10);
        HBox row2 = new HBox(10);
        Button btnAlerta = actionBtn("🚨", "Ver alertas");
        Button btnAtencion = actionBtn("📋", "Nueva atención");
        Button btnMapa = actionBtn("🗺", "Ver mapa");
        Button btnHistorial = actionBtn("📜", "Historial");
        btnAlerta.setOnAction(e -> root.setCenter(buildMisAlertasView()));
        btnAtencion.setOnAction(e -> root.setCenter(buildMisAtencionesView()));
        btnMapa.setOnAction(e -> root.setCenter(buildMapaZonasView()));
        btnHistorial.setOnAction(e -> root.setCenter(buildHistorialView()));
        row1.getChildren().addAll(btnAlerta, btnAtencion);
        row2.getChildren().addAll(btnMapa, btnHistorial);
        acciones.getChildren().addAll(row1, row2);

        VBox companeros = buildCompanerosPanel();
        HBox.setHgrow(companeros, Priority.ALWAYS);
        row.getChildren().addAll(atenciones, alarmas, acciones, companeros);
        return row;
    }

    // =========================================================================
    // VISTA: MIS ALERTAS
    // =========================================================================
    private ScrollPane buildMisAlertasView() {
        VBox content = new VBox(16);
        content.setPadding(new Insets(22));
        content.setStyle("-fx-background-color: " + BG + ";");

        Label title = new Label("🚨 Mis alertas asignadas");
        title.setFont(Font.font("System", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#111827"));
        content.getChildren().add(title);

        try {
            List<Alerta> lista = obtenerMisAlertas();
            if (lista.isEmpty()) {
                VBox vacio = createPanel("Sin alertas");
                vacio.getChildren().add(label("No tienes alertas asignadas en este momento.", 13, GRAY_TEXT, false));
                content.getChildren().add(vacio);
            } else {
                for (Alerta a : lista) {
                    content.getChildren().add(buildAlertaCard(a));
                }
            }
        } catch (Exception e) {
            VBox err = createPanel("Error");
            err.getChildren().add(label("Error al cargar alertas: " + e.getMessage(), 12, RED, false));
            content.getChildren().add(err);
        }

        return wrapScroll(content);
    }

    /**
     * Tarjeta detallada de una alerta con cambio de estado y descripción de
     * situación
     */
    private VBox buildAlertaCard(Alerta a) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: " + WHITE + "; -fx-background-radius: 12;");
        shadow(card);

        // Encabezado
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        String dotColor = estadoColor(a.getEstado());
        Circle dot = new Circle(7, Color.web(dotColor));
        String tipo = a.getTipoalerta() != null ? a.getTipoalerta().getNombre() : "—";
        String barrio = a.getBarrio() != null ? a.getBarrio().getNombre() : "—";
        VBox headerTxt = new VBox(2);
        HBox.setHgrow(headerTxt, Priority.ALWAYS);
        headerTxt.getChildren().addAll(
                label("🚨 " + tipo + " — " + barrio, 14, "#111827", true),
                label("Fecha: " + formatFecha(a.getFechaHora()), 11, GRAY_TEXT, false));
        Label estadoBadge = label(a.getEstado() != null ? a.getEstado().name().replace("_", " ") : "—", 11, dotColor, true);
        estadoBadge.setPadding(new Insets(3, 8, 3, 8));
        estadoBadge.setStyle("-fx-background-color: " + dotColor + "22; -fx-background-radius: 6;");
        header.getChildren().addAll(dot, headerTxt, estadoBadge);

        // Descripción de la alerta
        Label descLbl = label("Descripción: " + (a.getDescripcion() != null ? a.getDescripcion() : "Sin descripción"), 12, "#374151", false);
        descLbl.setWrapText(true);

        // Info adicional
        HBox infoRow = new HBox(16);
        infoRow.getChildren().addAll(
                label("🗺 Dirección: " + formatDireccion(a.getDireccion()), 11, GRAY_TEXT, false),
                label("🔫 Arma: " + (a.getTipoarma() != null ? a.getTipoarma().getNombre() : "N/A"), 11, GRAY_TEXT, false),
                label("🚗 Transporte: " + (a.getMediotransporte() != null ? a.getMediotransporte().getNombre() : "N/A"), 11, GRAY_TEXT, false)
        );

        separator2(card);

        // ── Sección: Describir situación ──
        Label secTitle = label("📝 Registrar atención / describir situación", 13, BLUE, true);

        TextArea situacion = new TextArea();
        situacion.setPromptText("Describe la situación que ocurrió en la escena, acciones tomadas, resultado...");
        situacion.setPrefRowCount(3);
        situacion.setWrapText(true);
        situacion.setStyle("-fx-font-size: 12; -fx-background-radius: 8;");

        // Combo estado final de la ALERTA
        Label estadoLbl = label("Cambiar estado de la alerta:", 12, "#374151", false);
        ComboBox<String> estadoCombo = new ComboBox<>();
        estadoCombo.getItems().addAll("PENDIENTE", "RECIBIDA", "EN_ATENCION", "UNIDAD_ASIGNADA", "RESUELTA", "CANCELADA");
        if (a.getEstado() != null) {
            estadoCombo.setValue(a.getEstado().name());
        }
        estadoCombo.setPrefWidth(200);

        // Combo estado de ATENCIÓN que se va a insertar
        Label atencionEstadoLbl = label("Estado de la atención a registrar:", 12, "#374151", false);
        ComboBox<String> atencionCombo = new ComboBox<>();
        atencionCombo.getItems().addAll("PENDIENTE", "EN_PROCESO", "FINALIZADA", "CANCELADA");
        atencionCombo.setValue("EN_PROCESO");
        atencionCombo.setPrefWidth(200);

        HBox combosRow = new HBox(16);
        combosRow.setAlignment(Pos.CENTER_LEFT);
        VBox estadoBox = new VBox(4, estadoLbl, estadoCombo);
        VBox atencionBox = new VBox(4, atencionEstadoLbl, atencionCombo);
        combosRow.getChildren().addAll(estadoBox, atencionBox);

        Button guardar = new Button("💾  Guardar atención");
        guardar.setStyle("-fx-background-color: " + BLUE + "; -fx-text-fill: white; -fx-font-size: 12; "
                + "-fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 7 18 7 18;");
        guardar.setOnAction(ev -> {
            String desc = situacion.getText().trim();
            String nuevoEst = estadoCombo.getValue();
            String estAtencion = atencionCombo.getValue();

            if (desc.isEmpty()) {
                mostrarAlerta("Campo requerido", "Debes describir la situación antes de guardar.");
                return;
            }

            // 1. Actualizar estado de la alerta
            try {
                if (nuevoEst != null && alertaService != null) {
                    alertaService.actualizarEstado(a.getId_alerta(), nuevoEst);
                }
            } catch (Exception ex) {
                mostrarAlerta("Error", "No se pudo actualizar el estado de la alerta: " + ex.getMessage());
            }

            // 2. Insertar atención
            try {
                if (atencionService != null) {
                    AtencionAlerta at = new AtencionAlerta();
                    at.setAlerta(a);
                    at.setDescripcion(desc);
                    at.setEstado(EstadoAtencionAlerta.valueOf(estAtencion));
                    at.setObservacion("Registrado por: " + (usuarioActual != null ? usuarioActual.getUsername() : "—"));
                    // Unidad del policía
                    if (policiaActual != null && policiaActual.getUnidadpolicial() != null) {
                        at.setUnidad(policiaActual.getUnidadpolicial());
                    } else {
                        UnidadPolicial u = new UnidadPolicial();
                        u.setNombre("Sin unidad");
                        at.setUnidad(u);
                    }
                    boolean ok = atencionService.insertar(at);
                    if (ok) {
                        mostrarInfo("¡Guardado!", "La atención fue registrada correctamente.");
                        situacion.clear();
                        root.setCenter(buildMisAlertasView());
                    } else {
                        mostrarAlerta("Error", "No se pudo registrar la atención.");
                    }
                }
            } catch (Exception ex) {
                mostrarAlerta("Error al guardar", ex.getMessage());
            }
        });

        card.getChildren().addAll(header, descLbl, infoRow, separator(), secTitle, situacion, combosRow, guardar);
        return card;
    }

    // =========================================================================
    // VISTA: MIS ATENCIONES
    // =========================================================================
    private ScrollPane buildMisAtencionesView() {
        VBox content = new VBox(16);
        content.setPadding(new Insets(22));
        content.setStyle("-fx-background-color: " + BG + ";");

        Label title = new Label("📋 Mis atenciones");
        title.setFont(Font.font("System", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#111827"));
        content.getChildren().add(title);

        try {
            List<AtencionAlerta> lista = obtenerMisAtenciones();
            if (lista.isEmpty()) {
                VBox vacio = createPanel("Sin atenciones");
                vacio.getChildren().add(label("No tienes atenciones registradas.", 13, GRAY_TEXT, false));
                content.getChildren().add(vacio);
            } else {
                for (AtencionAlerta at : lista) {
                    content.getChildren().add(buildAtencionCard(at));
                }
            }
        } catch (Exception e) {
            VBox err = createPanel("Error");
            err.getChildren().add(label("Error: " + e.getMessage(), 12, RED, false));
            content.getChildren().add(err);
        }

        return wrapScroll(content);
    }

    /**
     * Tarjeta de atención con cambio de estado y descripción de situación
     */
    private VBox buildAtencionCard(AtencionAlerta at) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: " + WHITE + "; -fx-background-radius: 12;");
        shadow(card);

        // Encabezado
        String estadoStr = at.getEstado() != null ? at.getEstado().name().replace("_", " ") : "—";
        String colorEst = switch (at.getEstado() != null ? at.getEstado() : EstadoAtencionAlerta.PENDIENTE) {
            case FINALIZADA ->
                GREEN;
            case EN_PROCESO ->
                ORANGE;
            case CANCELADA ->
                GRAY_TEXT;
            default ->
                RED;
        };
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Circle dot = new Circle(7, Color.web(colorEst));
        String unidadNom = at.getUnidad() != null ? at.getUnidad().getNombre() : "—";
        VBox hTxt = new VBox(2);
        HBox.setHgrow(hTxt, Priority.ALWAYS);
        hTxt.getChildren().addAll(
                label("📋 Atención #" + at.getId_atencion() + " — " + unidadNom, 14, "#111827", true),
                label("Fecha: " + formatFechaAt(at.getFechaatencion()), 11, GRAY_TEXT, false));
        Label badge = label(estadoStr, 11, colorEst, true);
        badge.setPadding(new Insets(3, 8, 3, 8));
        badge.setStyle("-fx-background-color: " + colorEst + "22; -fx-background-radius: 6;");
        header.getChildren().addAll(dot, hTxt, badge);

        // Descripción actual
        Label descActual = label("Situación: " + (at.getDescripcion() != null ? at.getDescripcion() : "—"), 12, "#374151", false);
        descActual.setWrapText(true);
        Label obsActual = label("Observación: " + (at.getObservacion() != null ? at.getObservacion() : "—"), 11, GRAY_TEXT, false);
        obsActual.setWrapText(true);

        separator2(card);

        // ── Actualizar situación ──
        Label editTitle = label("✏ Actualizar situación / estado", 13, BLUE, true);

        TextArea nuevaSituacion = new TextArea();
        nuevaSituacion.setPromptText("Agrega más detalles de la situación o actualiza la descripción...");
        nuevaSituacion.setText(at.getDescripcion() != null ? at.getDescripcion() : "");
        nuevaSituacion.setPrefRowCount(3);
        nuevaSituacion.setWrapText(true);
        nuevaSituacion.setStyle("-fx-font-size: 12; -fx-background-radius: 8;");

        Label nuevoEstLbl = label("Nuevo estado:", 12, "#374151", false);
        ComboBox<String> nuevoEstCombo = new ComboBox<>();
        nuevoEstCombo.getItems().addAll("PENDIENTE", "EN_PROCESO", "FINALIZADA", "CANCELADA");
        if (at.getEstado() != null) {
            nuevoEstCombo.setValue(at.getEstado().name());
        }
        nuevoEstCombo.setPrefWidth(200);

        Button actualizar = new Button("🔄  Actualizar atención");
        actualizar.setStyle("-fx-background-color: " + ORANGE + "; -fx-text-fill: white; -fx-font-size: 12; "
                + "-fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 7 18 7 18;");
        actualizar.setOnAction(ev -> {
            String desc = nuevaSituacion.getText().trim();
            String estado = nuevoEstCombo.getValue();
            if (desc.isEmpty()) {
                mostrarAlerta("Campo requerido", "La descripción no puede quedar vacía.");
                return;
            }
            try {
                AtencionAlerta upd = new AtencionAlerta();
                upd.setId_atencion(at.getId_atencion());
                upd.setAlerta(at.getAlerta());
                upd.setUnidad(at.getUnidad());
                upd.setDescripcion(desc);
                upd.setEstado(EstadoAtencionAlerta.valueOf(estado));
                upd.setTipoarma(at.getTipoarma());
                upd.setMediotransporte(at.getMediotransporte());
                upd.setObservacion(at.getObservacion());
                boolean ok = atencionService.actualizar(upd);
                if (ok) {
                    mostrarInfo("¡Actualizado!", "La atención fue actualizada correctamente.");
                    root.setCenter(buildMisAtencionesView());
                } else {
                    mostrarAlerta("Error", "No se pudo actualizar la atención.");
                }
            } catch (Exception ex) {
                mostrarAlerta("Error", ex.getMessage());
            }
        });

        card.getChildren().addAll(header, descActual, obsActual, separator(), editTitle, nuevaSituacion,
                new VBox(4, nuevoEstLbl, nuevoEstCombo), actualizar);
        return card;
    }

    // =========================================================================
    // VISTA: ALARMAS (con cambio de estado)
    // =========================================================================
    private ScrollPane buildAlarmasView() {
        VBox content = new VBox(16);
        content.setPadding(new Insets(22));
        content.setStyle("-fx-background-color: " + BG + ";");

        Label title = new Label("🔔 Alarmas");
        title.setFont(Font.font("System", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#111827"));
        content.getChildren().add(title);

        try {
            List<Alarma> lista = alarmaService.listar();
            if (lista.isEmpty()) {
                VBox vacio = createPanel("Sin alarmas");
                vacio.getChildren().add(label("No hay alarmas registradas.", 13, GRAY_TEXT, false));
                content.getChildren().add(vacio);
            } else {
                for (Alarma al : lista) {
                    content.getChildren().add(buildAlarmaCard(al));
                }
            }
        } catch (Exception e) {
            VBox err = createPanel("Error");
            err.getChildren().add(label("Error: " + e.getMessage(), 12, RED, false));
            content.getChildren().add(err);
        }

        return wrapScroll(content);
    }

    /**
     * Tarjeta de alarma con cambio de estado
     */
    private VBox buildAlarmaCard(Alarma al) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: " + WHITE + "; -fx-background-radius: 12;");
        shadow(card);

        String estadoStr = al.getEstado() != null ? al.getEstado().name().replace("_", " ") : "—";
        String colorEst = al.getEstado() == EstadoAlarma.ACTIVA ? GREEN
                : al.getEstado() == EstadoAlarma.EN_MANTENIMIENTO ? ORANGE : GRAY_TEXT;

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Circle dot = new Circle(7, Color.web(colorEst));
        VBox hTxt = new VBox(2);
        HBox.setHgrow(hTxt, Priority.ALWAYS);
        String barrioNom = al.getBarrio() != null ? al.getBarrio().getNombre() : "—";
        hTxt.getChildren().addAll(
                label("🔔 " + al.getNombre() + " — " + barrioNom, 14, "#111827", true),
                label("Lat: " + al.getLatitud() + "  Lng: " + al.getLongitud()
                        + "  Radio: " + al.getRadio_cobertura() + " m", 11, GRAY_TEXT, false));
        Label badge = label(estadoStr, 11, colorEst, true);
        badge.setPadding(new Insets(3, 8, 3, 8));
        badge.setStyle("-fx-background-color: " + colorEst + "22; -fx-background-radius: 6;");
        header.getChildren().addAll(dot, hTxt, badge);

        // Cambio de estado
        Label ceLbl = label("Cambiar estado de alarma:", 12, "#374151", false);
        ComboBox<String> ceCombo = new ComboBox<>();
        ceCombo.getItems().addAll("INACTIVA", "ACTIVA", "EN_MANTENIMIENTO");
        if (al.getEstado() != null) {
            ceCombo.setValue(al.getEstado().name());
        }
        ceCombo.setPrefWidth(220);

        Button guardar = new Button("💾  Actualizar estado");
        guardar.setStyle("-fx-background-color: " + BLUE + "; -fx-text-fill: white; -fx-font-size: 12; "
                + "-fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 7 18 7 18;");
        guardar.setOnAction(ev -> {
            String nuevoEst = ceCombo.getValue();
            try {
                Alarma upd = new Alarma();
                upd.setId_alarma(al.getId_alarma());
                upd.setNombre(al.getNombre());
                upd.setBarrio(al.getBarrio());
                upd.setLatitud(al.getLatitud());
                upd.setLongitud(al.getLongitud());
                upd.setRadio_cobertura(al.getRadio_cobertura());
                upd.setEstado(EstadoAlarma.valueOf(nuevoEst));
                boolean ok = alarmaService.actualizar(upd);
                if (ok) {
                    mostrarInfo("¡Actualizado!", "Estado de alarma actualizado.");
                    root.setCenter(buildAlarmasView());
                } else {
                    mostrarAlerta("Error", "No se pudo actualizar la alarma.");
                }
            } catch (Exception ex) {
                mostrarAlerta("Error", ex.getMessage());
            }
        });

        HBox ceRow = new HBox(12, new VBox(4, ceLbl, ceCombo), guardar);
        ceRow.setAlignment(Pos.BOTTOM_LEFT);

        card.getChildren().addAll(header, separator(), ceRow);
        return card;
    }

    // =========================================================================
    // VISTA: HISTORIAL (atenciones finalizadas / canceladas)
    // =========================================================================
    private ScrollPane buildHistorialView() {
        VBox content = new VBox(16);
        content.setPadding(new Insets(22));
        content.setStyle("-fx-background-color: " + BG + ";");

        Label title = new Label("📜 Historial de atenciones");
        title.setFont(Font.font("System", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#111827"));
        content.getChildren().add(title);

        VBox panel = createPanel("Atenciones finalizadas y canceladas");
        try {
            List<AtencionAlerta> lista = obtenerMisAtenciones().stream()
                    .filter(at -> at.getEstado() == EstadoAtencionAlerta.FINALIZADA
                    || at.getEstado() == EstadoAtencionAlerta.CANCELADA)
                    .collect(Collectors.toList());
            if (lista.isEmpty()) {
                panel.getChildren().add(label("No hay atenciones finalizadas o canceladas.", 13, GRAY_TEXT, false));
            } else {
                for (AtencionAlerta at : lista) {
                    String estadoStr = at.getEstado().name().replace("_", " ");
                    String color = at.getEstado() == EstadoAtencionAlerta.FINALIZADA ? GREEN : GRAY_TEXT;
                    String desc = at.getDescripcion() != null ? at.getDescripcion() : "—";
                    String fecha = formatFechaAt(at.getFechaatencion());
                    panel.getChildren().addAll(
                            listItem("📋",
                                    "#" + at.getId_atencion() + " — " + estadoStr + " · " + fecha,
                                    desc, color),
                            separator());
                }
            }
        } catch (Exception e) {
            panel.getChildren().add(label("Error: " + e.getMessage(), 12, RED, false));
        }
        content.getChildren().add(panel);
        return wrapScroll(content);
    }

    // =========================================================================
    // VISTA: MAPA POR ZONAS
    // =========================================================================
    private ScrollPane buildMapaZonasView() {
        VBox content = new VBox(18);
        content.setPadding(new Insets(22));
        content.setStyle("-fx-background-color: " + BG + ";");

        Label title = new Label("🗺 Mapa por zonas");
        title.setFont(Font.font("System", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#111827"));
        content.getChildren().add(title);

        // Mapa principal
        VBox mapCard = createPanel("Distribución de alertas por zona");
        StackPane mapArea = new StackPane();
        mapArea.setPrefHeight(340);

        Rectangle mapBg = new Rectangle();
        mapBg.setFill(Color.web("#c8e6c9"));
        mapBg.widthProperty().bind(mapArea.widthProperty());
        mapBg.heightProperty().bind(mapArea.heightProperty());
        mapBg.setArcWidth(10);
        mapBg.setArcHeight(10);

        javafx.scene.layout.Pane overlay = new javafx.scene.layout.Pane();
        overlay.prefWidthProperty().bind(mapArea.widthProperty());
        overlay.prefHeightProperty().bind(mapArea.heightProperty());

        // Calles horizontales
        for (int i = 0; i < 5; i++) {
            Rectangle h = new Rectangle(800, 2);
            h.setFill(Color.web("#a5d6a7"));
            h.setY(50 + i * 55);
            overlay.getChildren().add(h);
        }
        // Calles verticales
        for (int i = 0; i < 6; i++) {
            Rectangle v = new Rectangle(2, 340);
            v.setFill(Color.web("#a5d6a7"));
            v.setX(60 + i * 100);
            overlay.getChildren().add(v);
        }

        // Zonas coloreadas
        overlay.getChildren().addAll(
                zona(10, 10, 200, 120, RED, "Zona Norte\n3 alertas activas"),
                zona(220, 10, 200, 120, ORANGE, "Zona Centro\n1 alerta revisión"),
                zona(430, 10, 200, 120, GREEN, "Zona Sur\nSin alertas"),
                zona(10, 150, 200, 120, GREEN, "Zona Occidente\nSin alertas"),
                zona(220, 150, 200, 120, RED, "Zona Oriente\n2 alertas activas"),
                zona(430, 150, 200, 120, ORANGE, "Zona Centro-Sur\n1 alerta")
        );

        // Dots de alertas reales si hay
        try {
            List<Alerta> alertas = obtenerMisAlertas();
            double[] xs = {80, 280, 450, 120, 380};
            double[] ys = {80, 60, 90, 200, 190};
            int i = 0;
            for (Alerta al : alertas) {
                if (i >= xs.length) {
                    break;
                }
                Circle c = mapDot(xs[i], ys[i], estadoColor(al.getEstado()));
                c.setRadius(9);
                Tooltip tp = new Tooltip(
                        (al.getTipoalerta() != null ? al.getTipoalerta().getNombre() : "Alerta")
                        + "\n" + (al.getBarrio() != null ? al.getBarrio().getNombre() : "")
                        + "\n" + (al.getEstado() != null ? al.getEstado().name() : ""));
                Tooltip.install(c, tp);
                overlay.getChildren().add(c);
                i++;
            }
        } catch (Exception ignored) {
        }

        mapArea.getChildren().addAll(mapBg, overlay);

        // Leyenda
        HBox legend = new HBox(20);
        legend.setAlignment(Pos.CENTER);
        legend.setPadding(new Insets(10, 0, 0, 0));
        legend.getChildren().addAll(
                legendItem(RED, "Zona con alertas activas"),
                legendItem(ORANGE, "Zona en revisión"),
                legendItem(GREEN, "Zona segura"));

        mapCard.getChildren().addAll(mapArea, legend);

        // Resumen por zona
        VBox resumenCard = createPanel("Resumen de alertas por estado");
        try {
            List<Alerta> todas = alertaService.listar();
            long pendientes = todas.stream().filter(a -> a.getEstado() == EstadoAlerta.PENDIENTE).count();
            long enAtencion = todas.stream().filter(a -> a.getEstado() == EstadoAlerta.EN_ATENCION).count();
            long asignadas = todas.stream().filter(a -> a.getEstado() == EstadoAlerta.UNIDAD_ASIGNADA).count();
            long resueltas = todas.stream().filter(a -> a.getEstado() == EstadoAlerta.RESUELTA).count();
            long canceladas = todas.stream().filter(a -> a.getEstado() == EstadoAlerta.CANCELADA).count();
            resumenCard.getChildren().addAll(
                    listItem("🔴", "Pendientes", String.valueOf(pendientes), RED), separator(),
                    listItem("🟠", "En atención", String.valueOf(enAtencion), ORANGE), separator(),
                    listItem("🔵", "Unidad asignada", String.valueOf(asignadas), BLUE), separator(),
                    listItem("🟢", "Resueltas", String.valueOf(resueltas), GREEN), separator(),
                    listItem("⚫", "Canceladas", String.valueOf(canceladas), GRAY_TEXT)
            );
        } catch (Exception e) {
            resumenCard.getChildren().add(label("Error: " + e.getMessage(), 12, RED, false));
        }

        content.getChildren().addAll(mapCard, resumenCard);
        return wrapScroll(content);
    }

    /**
     * Rectángulo de zona para el mapa
     */
    private Rectangle zona(double x, double y, double w, double h, String color, String tooltip) {
        Rectangle r = new Rectangle(x, y, w, h);
        r.setFill(Color.web(color + "44"));
        r.setStroke(Color.web(color));
        r.setStrokeWidth(1.5);
        r.setArcWidth(8);
        r.setArcHeight(8);
        Tooltip tp = new Tooltip(tooltip);
        Tooltip.install(r, tp);
        return r;
    }

    // =========================================================================
    // VISTA: NOTIFICACIONES
    // =========================================================================
    private ScrollPane buildNotificacionesView() {
        VBox content = new VBox(16);
        content.setPadding(new Insets(22));
        content.setStyle("-fx-background-color: " + BG + ";");

        Label title = new Label("📢 Notificaciones");
        title.setFont(Font.font("System", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#111827"));
        content.getChildren().add(title);

        VBox panel = createPanel("Mis notificaciones");
        try {
            List<Notificacion> lista = notificacionService.listar();
            // Filtrar las del usuario actual
            List<Notificacion> mias = lista.stream()
                    .filter(n -> n.getUsuario() != null && usuarioActual != null
                    && (usuarioActual.getUsername() != null
                    && usuarioActual.getUsername().equals(n.getUsuario().getUsername())
                    || usuarioActual.getCorreo() != null
                    && usuarioActual.getCorreo().equals(n.getCorreodestinatario())))
                    .collect(Collectors.toList());
            if (mias.isEmpty()) {
                // Mostrar todas si no hay filtro
                lista = lista.isEmpty() ? lista : lista.subList(0, Math.min(lista.size(), 20));
                if (lista.isEmpty()) {
                    panel.getChildren().add(label("No hay notificaciones.", 13, GRAY_TEXT, false));
                } else {
                    for (Notificacion n : lista) {
                        String dest = n.getCorreodestinatario() != null ? n.getCorreodestinatario() : "—";
                        String msg = n.getMensaje() != null ? n.getMensaje() : "—";
                        String estStr = n.getEstado() != null ? n.getEstado().name().replace("_", " ") : "—";
                        String color = n.getEstado() == EstadoNotificacion.LEIDA ? GREEN
                                : n.getEstado() == EstadoNotificacion.ENVIADA ? BLUE
                                : n.getEstado() == EstadoNotificacion.ERROR ? RED : ORANGE;
                        panel.getChildren().addAll(
                                listItem("📢", dest + " — " + estStr, msg, color), separator());
                    }
                }
            } else {
                for (Notificacion n : mias) {
                    String msg = n.getMensaje() != null ? n.getMensaje() : "—";
                    String estStr = n.getEstado() != null ? n.getEstado().name().replace("_", " ") : "—";
                    String color = n.getEstado() == EstadoNotificacion.LEIDA ? GREEN
                            : n.getEstado() == EstadoNotificacion.ENVIADA ? BLUE
                            : n.getEstado() == EstadoNotificacion.ERROR ? RED : ORANGE;
                    panel.getChildren().addAll(
                            listItem("📢", estStr, msg, color), separator());
                }
            }
        } catch (Exception e) {
            panel.getChildren().add(label("Error: " + e.getMessage(), 12, RED, false));
        }
        content.getChildren().add(panel);
        return wrapScroll(content);
    }

    // =========================================================================
    // VISTA: MI PERFIL
    // =========================================================================
    private ScrollPane buildPerfilView() {
        VBox content = new VBox(16);
        content.setPadding(new Insets(22));
        content.setStyle("-fx-background-color: " + BG + ";");

        Label title = new Label("⚙ Mi perfil");
        title.setFont(Font.font("System", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#111827"));
        content.getChildren().add(title);

        VBox panel = createPanel("Mis datos personales");
        if (usuarioActual != null) {
            String nombre = (trim(usuarioActual.getPrimer_nombre()) + " "
                    + trim(usuarioActual.getSegundo_nombre()) + " "
                    + trim(usuarioActual.getPrimer_apellido()) + " "
                    + trim(usuarioActual.getSegundo_apellido())).trim().replaceAll("  +", " ");
            panel.getChildren().addAll(
                    listItem("👤", "Nombre completo", nombre.isEmpty() ? "—" : nombre, BLUE), separator(),
                    listItem("🪪", "Identificación", nn(usuarioActual.getIdentificacion()), GRAY_TEXT), separator(),
                    listItem("✉", "Correo", nn(usuarioActual.getCorreo()), GRAY_TEXT), separator(),
                    listItem("📱", "Teléfono", nn(usuarioActual.getTelefono()), GRAY_TEXT), separator(),
                    listItem("🔑", "Username", nn(usuarioActual.getUsername()), GRAY_TEXT), separator(),
                    listItem("👔", "Estado",
                            usuarioActual.getEstado() != null ? usuarioActual.getEstado().name() : "—", GRAY_TEXT),
                    separator()
            );
        }

        VBox panelPolicia = createPanel("Mis datos de policía");
        if (policiaActual != null) {
            String unidadNom = policiaActual.getUnidadpolicial() != null
                    ? policiaActual.getUnidadpolicial().getNombre() : "—";
            panelPolicia.getChildren().addAll(
                    listItem("🏅", "Placa", nn(policiaActual.getPlaca()), BLUE), separator(),
                    listItem("⭐", "Rango", nn(policiaActual.getRango()), BLUE), separator(),
                    listItem("🚓", "Unidad policial", unidadNom, BLUE), separator(),
                    listItem("📊", "Estado policial",
                            policiaActual.getEstadopolicial() != null
                            ? policiaActual.getEstadopolicial().name().replace("_", " ") : "—",
                            GREEN), separator()
            );
        } else {
            panelPolicia.getChildren().add(label("No se encontraron datos de policía.", 12, GRAY_TEXT, false));
        }

        content.getChildren().addAll(panel, panelPolicia);
        return wrapScroll(content);
    }

    // =========================================================================
    // COMPAÑEROS DE UNIDAD
    // =========================================================================
    private VBox buildCompanerosPanel() {
        VBox card = createPanel("👮 Compañeros de unidad");

        // Sin unidad asignada
        if (policiaActual == null || policiaActual.getUnidadpolicial() == null) {
            card.getChildren().add(label("No tienes unidad asignada aún.", 12, GRAY_TEXT, false));
            return card;
        }

        int miUnidadId = policiaActual.getUnidadpolicial().getId_unidad();
        String miUnidadNombre = policiaActual.getUnidadpolicial().getNombre();

        // Header de la unidad
        HBox unidadHeader = new HBox(8);
        unidadHeader.setAlignment(Pos.CENTER_LEFT);
        unidadHeader.setPadding(new Insets(6, 10, 6, 10));
        unidadHeader.setStyle("-fx-background-color:" + BLUE_LIGHT + "; -fx-background-radius:8;");
        unidadHeader.getChildren().addAll(
                label("🚓", 14, BLUE, false),
                label("Unidad: " + miUnidadNombre, 12, BLUE, true));
        card.getChildren().add(unidadHeader);

        try {
            List<Policia> todos = policiaService.listar();

            List<Policia> companeros = todos.stream()
                    .filter(p -> p.getUnidadpolicial() != null
                    && p.getUnidadpolicial().getId_unidad() == miUnidadId
                    && !p.getIdentificacion().equals(
                            usuarioActual != null ? usuarioActual.getIdentificacion() : ""))
                    .collect(Collectors.toList());

            if (companeros.isEmpty()) {
                card.getChildren().add(label("No hay otros policías en tu unidad.", 12, GRAY_TEXT, false));
            } else {
                for (Policia p : companeros) {
                    String nombreP = (trim(p.getPrimer_nombre()) + " " + trim(p.getPrimer_apellido())).trim();
                    String rangoP = p.getRango() != null ? p.getRango() : "—";
                    String placaP = p.getPlaca() != null ? "Placa: " + p.getPlaca() : "Sin placa";

                    String estadoP = p.getEstadopolicial() != null
                            ? p.getEstadopolicial().name().replace("_", " ") : "—";
                    String colorEstado = p.getEstadopolicial() != null
                            && p.getEstadopolicial().name().contains("ACTIVO") ? GREEN
                            : p.getEstadopolicial() != null
                            && p.getEstadopolicial().name().contains("PERMISO") ? ORANGE
                            : GRAY_TEXT;

                    HBox fila = new HBox(10);
                    fila.setAlignment(Pos.CENTER_LEFT);
                    fila.setPadding(new Insets(8, 4, 8, 4));

                    // Avatar
                    Circle av2 = new Circle(18, Color.web("#334155"));
                    StackPane avBox2 = new StackPane(av2, label("👮", 12, WHITE, false));

                    // Info
                    VBox infoP = new VBox(2);
                    HBox.setHgrow(infoP, Priority.ALWAYS);
                    infoP.getChildren().addAll(
                            label(nombreP.isEmpty() ? "—" : nombreP, 12, "#111827", true),
                            label(rangoP + " · " + placaP, 10, GRAY_TEXT, false));

                    // Badge estado
                    Label badgeEstado = label(estadoP, 10, colorEstado, true);
                    badgeEstado.setPadding(new Insets(2, 7, 2, 7));
                    badgeEstado.setStyle("-fx-background-color:" + colorEstado + "22;"
                            + "-fx-background-radius:6;");

                    fila.getChildren().addAll(avBox2, infoP, badgeEstado);
                    card.getChildren().addAll(fila, separator());
                }
            }
        } catch (Exception e) {
            card.getChildren().add(label("Error al cargar compañeros: " + e.getMessage(), 12, RED, false));
        }

        return card;
    }

    // =========================================================================
    // CERRAR SESIÓN
    // =========================================================================
    private void cerrarSesion() {
        VBox bye = new VBox(20);
        bye.setAlignment(Pos.CENTER);
        bye.setStyle("-fx-background-color: " + BG + ";");
        bye.getChildren().addAll(
                label("👋", 70, "#111827", false),
                label("Sesión cerrada", 30, "#111827", true),
                label("Cerrando aplicación...", 13, GRAY_TEXT, false));
        root.setCenter(bye);
        new Timeline(new KeyFrame(Duration.seconds(2),
                ev -> ((Stage) root.getScene().getWindow()).close())).play();
    }

    // =========================================================================
    // CONTADORES PROPIOS
    // =========================================================================
    private long contarMisAlertasActivas() {
        try {
            return obtenerMisAlertas().stream()
                    .filter(a -> a.getEstado() == EstadoAlerta.PENDIENTE
                    || a.getEstado() == EstadoAlerta.EN_ATENCION
                    || a.getEstado() == EstadoAlerta.UNIDAD_ASIGNADA
                    || a.getEstado() == EstadoAlerta.RECIBIDA)
                    .count();
        } catch (Exception e) {
            return 0;
        }
    }

    private long contarMisAtenciones() {
        try {
            return obtenerMisAtenciones().size();
        } catch (Exception e) {
            return 0;
        }
    }

    private long contarAlarmasActivas() {
        try {
            if (alarmaService == null) {
                return 0;
            }
            return alarmaService.listar().stream()
                    .filter(a -> a.getEstado() == EstadoAlarma.ACTIVA
                    || a.getEstado() == EstadoAlarma.EN_MANTENIMIENTO)
                    .count();
        } catch (Exception e) {
            return 0;
        }
    }

    private int contarNotificaciones() {
        try {
            return notificacionService != null ? notificacionService.listar().size() : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    // =========================================================================
    // FILTROS DE DATOS PROPIOS
    // =========================================================================
    /**
     * Devuelve alertas "propias" del policía: - Filtra por username del
     * usuarioActual si AlertaDAO tiene listarPorUsuario - Fallback: las 20 más
     * recientes de todas
     */
    private List<Alerta> obtenerMisAlertas() throws Exception {
        if (alertaService == null) {
            return List.of();
        }
        List<Alerta> todas = alertaService.listar();
        if (usuarioActual == null) {
            return todas;
        }
        List<Alerta> mias = todas.stream()
                .filter(a -> a.getUsuario() != null
                && usuarioActual.getUsername() != null
                && usuarioActual.getUsername().equals(a.getUsuario().getUsername()))
                .collect(Collectors.toList());
        return mias.isEmpty() ? todas : mias;
    }

    /**
     * Devuelve atenciones propias filtrando por unidad del policía.
     */
    private List<AtencionAlerta> obtenerMisAtenciones() throws Exception {
        if (atencionService == null) {
            return List.of();
        }
        List<AtencionAlerta> todas = atencionService.listar();
        if (policiaActual == null || policiaActual.getUnidadpolicial() == null) {
            return todas;
        }
        String miUnidad = policiaActual.getUnidadpolicial().getNombre();
        List<AtencionAlerta> mias = todas.stream()
                .filter(at -> at.getUnidad() != null
                && miUnidad.equalsIgnoreCase(at.getUnidad().getNombre()))
                .collect(Collectors.toList());
        return mias.isEmpty() ? todas : mias;
    }

    // =========================================================================
    // HELPERS UI (mismos que AdministradorPoliciaApp)
    // =========================================================================
    private VBox statCard(String bgIcon, String accentColor, String title, long value, String sub, String iconName) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20, 22, 20, 22));
        card.setStyle("-fx-background-color:white; -fx-background-radius:18;");
        card.setPrefWidth(260);
        HBox.setHgrow(card, Priority.ALWAYS);
        shadow(card);

        StackPane iconWrap = new StackPane();
        iconWrap.setPrefSize(52, 52);
        iconWrap.setMinSize(52, 52);
        iconWrap.setMaxSize(52, 52);
        Region colorBg = new Region();
        colorBg.setPrefSize(52, 52);
        colorBg.setStyle("-fx-background-color:" + bgIcon + "; -fx-background-radius:14;");
        ImageView iv = new ImageView();
        iv.setFitWidth(28);
        iv.setFitHeight(28);
        iv.setPreserveRatio(true);
        try {
            java.io.InputStream is = getClass().getResourceAsStream("/" + iconName + ".png");
            if (is != null) {
                BufferedImage original = javax.imageio.ImageIO.read(is);
                BufferedImage recortada = recortarTransparencia(original);
                javafx.scene.image.WritableImage fxImg = javafx.embed.swing.SwingFXUtils.toFXImage(recortada, null);
                iv.setImage(fxImg);
            }
        } catch (Exception ignored) {
        }
        iconWrap.getChildren().addAll(colorBg, iv);

        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:#374151;");
        Label valLbl = new Label(String.valueOf(value));
        valLbl.setStyle("-fx-font-size:36px; -fx-font-weight:bold; -fx-text-fill:" + accentColor + ";");
        Label subLbl = new Label(sub);
        subLbl.setStyle("-fx-font-size:11px; -fx-text-fill:" + GRAY_TEXT + ";");

        VBox textBlock = new VBox(3, titleLbl, valLbl, subLbl);
        HBox topRow = new HBox(16, iconWrap, textBlock);
        topRow.setAlignment(Pos.CENTER_LEFT);
        card.getChildren().add(topRow);

        card.setOnMouseEntered(e -> {
            card.setTranslateY(-3);
            card.setStyle("-fx-background-color:white; -fx-background-radius:18; "
                    + "-fx-border-color:" + accentColor + "; -fx-border-width:1.5; -fx-border-radius:18;");
        });
        card.setOnMouseExited(e -> {
            card.setTranslateY(0);
            card.setStyle("-fx-background-color:white; -fx-background-radius:18;");
        });
        return card;
    }

    private VBox createPanel(String title) {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(16));
        panel.setStyle("-fx-background-color: " + WHITE + "; -fx-background-radius: 12;");
        shadow(panel);
        panel.getChildren().addAll(label(title, 14, "#111827", true), separator());
        return panel;
    }

    private HBox listItem(String icon, String title, String sub, String subColor) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(6, 0, 6, 0));
        StackPane iconBox = new StackPane();
        Rectangle bg = new Rectangle(32, 32);
        bg.setArcWidth(7);
        bg.setArcHeight(7);
        bg.setFill(Color.web(BG));
        iconBox.getChildren().addAll(bg, label(icon, 14, BLUE, false));
        VBox text = new VBox(1);
        HBox.setHgrow(text, Priority.ALWAYS);
        text.getChildren().addAll(label(title, 12, "#111827", false), label(sub, 10, subColor, false));
        row.getChildren().addAll(iconBox, text);
        return row;
    }

    private HBox alertItem(String icon, String title, String sub, String dotColor) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(7, 0, 7, 0));
        row.setCursor(javafx.scene.Cursor.HAND);
        Circle dotCircle = new Circle(5, Color.web(dotColor));
        StackPane iconBox = new StackPane();
        Rectangle bg = new Rectangle(33, 33);
        bg.setArcWidth(7);
        bg.setArcHeight(7);
        bg.setFill(Color.web(BG));
        iconBox.getChildren().addAll(bg, label(icon, 14, dotColor, false));
        VBox text = new VBox(2);
        HBox.setHgrow(text, Priority.ALWAYS);
        text.getChildren().addAll(label(title, 12, "#111827", false), label(sub, 10, GRAY_TEXT, false));
        row.getChildren().addAll(dotCircle, iconBox, text, label(">", 13, GRAY_TEXT, false));
        return row;
    }

    private Button actionBtn(String icon, String text) {
        Button btn = new Button(icon + "\n" + text);
        btn.setPrefSize(100, 70);
        btn.setWrapText(true);
        btn.setFont(Font.font("System", 12));
        String base = "-fx-background-color: " + BG + "; -fx-text-fill: #111827; -fx-background-radius: 10; -fx-border-color: " + BORDER + "; -fx-border-radius: 10; -fx-cursor: hand;";
        String hover = "-fx-background-color: " + BLUE_LIGHT + "; -fx-text-fill: " + BLUE + "; -fx-background-radius: 10; -fx-border-color: " + BLUE + "; -fx-border-radius: 10; -fx-cursor: hand;";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e -> btn.setStyle(base));
        return btn;
    }

    private Circle mapDot(double x, double y, String color) {
        Circle c = new Circle(6, Color.web(color));
        c.setCenterX(x);
        c.setCenterY(y);
        c.setStroke(Color.WHITE);
        c.setStrokeWidth(2);
        return c;
    }

    private HBox legendItem(String color, String text) {
        HBox item = new HBox(5);
        item.setAlignment(Pos.CENTER_LEFT);
        item.getChildren().addAll(new Circle(4, Color.web(color)), label(text, 10, GRAY_TEXT, false));
        return item;
    }

    private ScrollPane wrapScroll(VBox content) {
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background: " + BG + "; -fx-background-color: " + BG + ";");
        return scroll;
    }

    private Region separator() {
        Region sep = new Region();
        sep.setPrefHeight(1);
        sep.setStyle("-fx-background-color: " + BORDER + ";");
        return sep;
    }

    /**
     * Agrega un separador directamente al VBox (helper para las tarjetas).
     */
    private void separator2(VBox card) {
        Region sep = new Region();
        sep.setPrefHeight(1);
        sep.setStyle("-fx-background-color: " + BORDER + ";");
        card.getChildren().add(sep);
    }

    private Label label(String text, double size, String color, boolean bold) {
        Label lbl = new Label(text);
        lbl.setFont(bold ? Font.font("System", FontWeight.BOLD, size) : Font.font("System", size));
        lbl.setTextFill(Color.web(color));
        return lbl;
    }

    private void shadow(Region node) {
        node.setEffect(new DropShadow(10, 0, 2, Color.web("#0000001a")));
    }

    // ── String helpers ────────────────────────────────────────────
    private String trim(String s) {
        return s != null ? s.trim() : "";
    }

    private String nn(String s) {
        return s != null && !s.isBlank() ? s : "—";
    }

    private String formatDireccion(Direccion d) {
        if (d == null) {
            return "—";
        }
        StringBuilder sb = new StringBuilder();
        if (d.getCalle() != null && !d.getCalle().isBlank()) {
            sb.append("Calle ").append(d.getCalle()).append(" ");
        }
        if (d.getCarrera() != null && !d.getCarrera().isBlank()) {
            sb.append("# ").append(d.getCarrera());
        }
        if (d.getCasa() != null && !d.getCasa().isBlank()) {
            sb.append(" Casa ").append(d.getCasa());
        }
        return sb.isEmpty() ? "Sin dirección" : sb.toString().trim();
    }

    private String estadoColor(EstadoAlerta estado) {
        if (estado == null) {
            return GRAY_TEXT;
        }
        return switch (estado) {
            case PENDIENTE, EN_ATENCION ->
                RED;
            case UNIDAD_ASIGNADA ->
                ORANGE;
            case RESUELTA ->
                GREEN;
            default ->
                GRAY_TEXT;
        };
    }

    private String formatFecha(LocalDateTime dt) {
        if (dt == null) {
            return "—";
        }
        long mins = java.time.Duration.between(dt, LocalDateTime.now()).toMinutes();
        if (mins < 1) {
            return "Hace un momento";
        }
        if (mins < 60) {
            return "Hace " + mins + " min";
        }
        if (mins < 1440) {
            return "Hace " + (mins / 60) + " h";
        }
        return dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    private String formatFechaAt(LocalDateTime dt) {
        if (dt == null) {
            return "—";
        }
        return dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    private BufferedImage recortarTransparencia(BufferedImage image) {
        int minX = image.getWidth(), minY = image.getHeight(), maxX = 0, maxY = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int alpha = (image.getRGB(x, y) >> 24) & 0xff;
                if (alpha > 0) {
                    if (x < minX) {
                        minX = x;
                    }
                    if (y < minY) {
                        minY = y;
                    }
                    if (x > maxX) {
                        maxX = x;
                    }
                    if (y > maxY) {
                        maxY = y;
                    }
                }
            }
        }
        return image.getSubimage(minX, minY, maxX - minX + 1, maxY - minY + 1);
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
