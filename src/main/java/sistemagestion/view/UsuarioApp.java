/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.view;

/**
 *
 * @author Maria Cristina
 */
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

import sistemagestion.model.*;
import sistemagestion.service.*;

/**
 *
 *
 *
 * @author Maria Cristina
 */
public class UsuarioApp {

    // ── Paleta ────────────────────────────────────────────────────
    private static final String WHITE = "#ffffff";
    private static final String BG = "#f4f6fb";
    private static final String RED = "#e53935";
    private static final String RED_LIGHT = "#fff0f0";
    private static final String ORANGE = "#fb8c00";
    private static final String GREEN = "#43a047";
    private static final String BLUE = "#1565c0";
    private static final String BLUE_LIGHT = "#e8f0fe";
    private static final String GRAY_TEXT = "#6b7280";
    private static final String BORDER = "#e5e7eb";

    // ── Servicios ─────────────────────────────────────────────────
    private AlertaService alertaService;
    private NotificacionService notificacionService;
    private SuscripcionService suscripcionService;
    private BarrioService barrioService;

    // ── Usuario logueado ──────────────────────────────────────────
    private final Usuario usuarioActual;

    // ── UI ────────────────────────────────────────────────────────
    private BorderPane root;
    private VBox nav;
    private VBox mapaSubMenu = new VBox(5);
    private boolean mapaExpandido = false;

    // ── Constructor ───────────────────────────────────────────────
    public UsuarioApp(Usuario usuarioActual) {
        this.usuarioActual = usuarioActual;
        try {
            alertaService = new AlertaService();
            notificacionService = new NotificacionService();
            suscripcionService = new SuscripcionService();
            barrioService = new BarrioService();
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
        root.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
        BorderPane.setAlignment(root.getLeft(), Pos.TOP_LEFT);
        root.setStyle("-fx-background-color: " + BG + ";");

        Scene scene = new Scene(root, 1200, 620);
        stage.setTitle("WolertApp – Sistema de Alertas Comunitarias");
        stage.setScene(scene);
        stage.setResizable(true);   // permite redimensionar
        stage.setMaximized(false);  // inicia normal
        stage.show();
    }

    // =========================================================================
    // SIDEBAR
    // =========================================================================
    private VBox buildSidebar() {
        VBox sidebar = new VBox();
        sidebar.setPrefWidth(250);
        sidebar.setStyle("-fx-background-color: linear-gradient(to right, #16283d, #1f3a56);");

        // Logo
        HBox logoBox = new HBox(15);
        logoBox.setPadding(new Insets(25, 16, 25, 16));
        logoBox.setAlignment(Pos.CENTER_LEFT);
        ImageView logoView = new ImageView(
                new Image(getClass().getResourceAsStream("/LogoWolertAPP.png"))
        );

        logoView.setFitWidth(70);
        logoView.setFitHeight(70);
        logoView.setPreserveRatio(true);
        logoView.setTranslateX(-2);

        StackPane wolfIcon = new StackPane(logoView);
        VBox logoText = new VBox(2);
        logoText.setTranslateX(-5);
        logoText.getChildren().addAll(
                label("WolertApp", 22, WHITE, true),
                label("Sistema de Alertas Comunitarias", 9, "#8899bb", false)
        );
        logoBox.getChildren().addAll(wolfIcon, logoText);

        // User card con datos reales
        HBox userCard = new HBox(10);
        userCard.setPadding(new Insets(12, 16, 12, 16));
        userCard.setAlignment(Pos.CENTER_LEFT);
        userCard.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 12;");

        Circle avatar = new Circle(20, Color.web("#3a4a70"));
        Label avatarLbl = label("👤", 16, WHITE, false);
        StackPane avatarStack = new StackPane(avatar, avatarLbl);

        VBox userInfo = new VBox(2);
        String nombreCompleto = usuarioActual != null
                ? usuarioActual.getPrimer_nombre() + " " + usuarioActual.getPrimer_apellido()
                : "Usuario";
        String barrioNombre = usuarioActual != null
                && usuarioActual.getDireccion() != null
                && usuarioActual.getDireccion().getBarrio() != null
                ? "Barrio " + usuarioActual.getDireccion().getBarrio().getNombre()
                : "Sin barrio asignado";
        userInfo.getChildren().addAll(
                label(nombreCompleto, 13, WHITE, true),
                label(barrioNombre, 10, "#8899bb", false),
                label("● En línea", 10, GREEN, false)
        );
        userCard.getChildren().addAll(avatarStack, userInfo);

        // Nav — todos los ítems van a placeholder excepto Dashboard
      nav = new VBox(2);
        nav.setPadding(new Insets(16, 8, 16, 8));
        nav.getChildren().addAll(
                navItem("🏠", "Dashboard"),
                navItem("🔔", "Alertas"),
                navItem("🗺️", "Mapa"),
                navItem("👥", "Vecinos"),
                navItem("🔔", "Mis Alertas"),
                navItem("📋", "Mis Suscripciones"),
                navItem("💬", "Notificaciones"),
                navItem("📄", "Mis Reportes"),
                navItem("ℹ️", "Información Útil"),
                navItem("👤", "Perfil"),
                navItem("⚙️", "Configuración")
        );

        // Logout
        String styleLogout
                = "-fx-background-color: white; "
                + "-fx-border-color: white; "
                + "-fx-border-width: 2; "
                + "-fx-text-fill: #16283d; "
                + "-fx-font-size: 13px; "
                + "-fx-font-weight: bold; "
                + "-fx-background-radius: 30; "
                + "-fx-border-radius: 30; "
                + "-fx-padding: 10 35; "
                + "-fx-cursor: hand;";
        String styleLogoutHover
                = "-fx-background-color: #f0f4ff; "
                + "-fx-border-color: white; "
                + "-fx-border-width: 2; "
                + "-fx-text-fill: #16283d; "
                + "-fx-font-size: 13px; "
                + "-fx-font-weight: bold; "
                + "-fx-background-radius: 30; "
                + "-fx-border-radius: 30; "
                + "-fx-padding: 10 35; "
                + "-fx-cursor: hand;";

        Button logoutBtn = new Button("🚪  Cerrar sesión");
        logoutBtn.setStyle(styleLogout);
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setOnMouseEntered(e -> logoutBtn.setStyle(styleLogoutHover));
        logoutBtn.setOnMouseExited(e -> logoutBtn.setStyle(styleLogout));
        logoutBtn.setOnAction(e -> cerrarSesion());

        VBox logoutBox = new VBox(logoutBtn);
        logoutBox.setPadding(new Insets(12, 16, 18, 16));
        logoutBox.setAlignment(Pos.CENTER);

        VBox spacer = new VBox();

        VBox.setVgrow(spacer, Priority.ALWAYS);

        sidebar.getChildren().addAll(
                logoBox,
                userCard,
                nav,
                spacer,
                logoutBox
        );

        sidebar.setPrefHeight(Double.MAX_VALUE);
        sidebar.setMaxHeight(Double.MAX_VALUE);

        return sidebar;
    }

    private HBox navItem(String icon, String text) {

        HBox item = new HBox(12);
        item.setPadding(new Insets(12, 16, 12, 16));
        item.setAlignment(Pos.CENTER_LEFT);
        item.setCursor(javafx.scene.Cursor.HAND);
        item.setMaxWidth(Double.MAX_VALUE);

        String normalStyle = """
    -fx-background-radius: 10;
    -fx-background-color: transparent;
    -fx-focus-color: transparent;
    -fx-faint-focus-color: transparent;
""";

        String hoverStyle = """
    -fx-background-color: #ffffff18;
    -fx-background-radius: 10;
    -fx-focus-color: transparent;
    -fx-faint-focus-color: transparent;
""";

        String activeStyle = """
    -fx-background-color: #2563eb;
    -fx-background-radius: 10;
    -fx-focus-color: transparent;
    -fx-faint-focus-color: transparent;
""";

        item.setStyle(normalStyle);

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("""
        -fx-font-size: 18px;
        -fx-font-weight: bold;
        -fx-text-fill: white;
    """);

        Label textLabel = new Label(text);
        textLabel.setStyle("""
        -fx-font-size: 14px;
        -fx-font-weight: bold;
        -fx-text-fill: white;
    """);

        item.getChildren().addAll(iconLabel, textLabel);

        item.setOnMouseEntered(e -> {
            if (!item.getStyle().equals(activeStyle)) {
                item.setStyle(hoverStyle);
            }
        });

        item.setOnMouseExited(e -> {
            if (!item.getStyle().equals(activeStyle)) {
                item.setStyle(normalStyle);
            }
        });

        item.setOnMouseClicked((MouseEvent e) -> {

            item.setStyle(activeStyle);

            switch (text) {

                case "Dashboard" ->
                    root.setCenter(buildMainContent());

                case "Alertas" -> {
                    AlertasView alertasView = new AlertasView(
                            usuarioActual,
                            alertaService,
                            barrioService
                    );
                    root.setCenter(alertasView.getView());
                }

                case "Mis Alertas" -> {
                    MisAlertasView misAlertasView = new MisAlertasView(
                            usuarioActual,
                            alertaService,
                            barrioService,
                            root
                    );
                    root.setCenter(misAlertasView.getView());
                }

                case "Mapa" -> {
                    toggleMapaSubMenu(item);
                }
                case "Vecinos" -> {
                    VecinosView vecinosView = new VecinosView(usuarioActual, alertaService);
                    root.setCenter(vecinosView.getView());
                }

                default ->
                    root.setCenter(buildPlaceholder(text));
            }
        });

        return item;
    }

    private void toggleMapaSubMenu(HBox mapaItem) {
        int index = nav.getChildren().indexOf(mapaItem);
        if (!mapaExpandido) {
            mapaSubMenu = buildMapaSubMenu();
            nav.getChildren().add(index + 1, mapaSubMenu);
            mapaSubMenu.setVisible(true);
            mapaExpandido = true;
        } else {
            nav.getChildren().remove(mapaSubMenu);
            mapaExpandido = false;
        }
    }

    private VBox buildMapaSubMenu() {
        VBox sub = new VBox(5);
        sub.setPadding(new Insets(0, 0, 0, 25));
        sub.getChildren().addAll(subItem("📍 Mapa de alertas"), subItem("⚠ Zonas peligrosas"), subItem("👥 Alertas comunitarias"));
        return sub;
    }

    private HBox subItem(String text) {
        HBox item = new HBox();
        item.setPadding(new Insets(8, 10, 8, 10));
        item.setAlignment(Pos.CENTER_LEFT);
        item.setCursor(javafx.scene.Cursor.HAND);
        Label lbl = new Label(text);
        lbl.setTextFill(Color.web("#cbd5e1"));
        lbl.setFont(Font.font(12));
        item.getChildren().add(lbl);
        item.setOnMouseEntered(e -> item.setStyle(
                "-fx-background-color: #ffffff15; -fx-background-radius: 6; -fx-focus-color: transparent; -fx-faint-focus-color: transparent;"));
        item.setOnMouseExited(e -> item.setStyle(
                "-fx-background-color: transparent; -fx-focus-color: transparent; -fx-faint-focus-color: transparent;"));
        item.setOnMouseClicked(e -> root.setCenter(buildPlaceholder(text)));
        return item;
    }

    // =========================================================================
    // MAIN CONTENT — Dashboard
    // =========================================================================
    private ScrollPane buildMainContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color: " + BG + ";");
        content.getChildren().addAll(
                buildTopBar(),
                buildEmergencyBanner(),
                buildStatCards(),
                buildBottomRow(),
                buildFooter()
        );
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: " + BG + "; -fx-background: " + BG + ";");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        return scroll;
    }

    // ── Top bar ───────────────────────────────────────────────────
    private HBox buildTopBar() {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);
        VBox greeting = new VBox(4);
        String saludo = usuarioActual != null ? "¡Hola, " + usuarioActual.getPrimer_nombre() + "!" : "¡Hola!";
        Label hello = new Label(saludo);
        hello.setFont(Font.font("System", FontWeight.BOLD, 28));
        hello.setTextFill(Color.web("#111827"));
        greeting.getChildren().addAll(hello, label("Tu seguridad es importante. Estamos aquí para ayudarte.", 13, GRAY_TEXT, false));

        HBox rightBox = new HBox(16);
        rightBox.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(rightBox, Priority.ALWAYS);
        VBox dateBox = new VBox(2);
        dateBox.setAlignment(Pos.CENTER_RIGHT);
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", new Locale("es", "CO"));
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("hh:mm:ss a", new Locale("es", "CO"));
        LocalDateTime now0 = LocalDateTime.now(ZoneId.of("America/Bogota"));
        Label dateLbl = label("📅  " + now0.format(dateFmt), 13, "#374151", false);
        Label timeLbl = label(now0.format(timeFmt), 13, GRAY_TEXT, false);
        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), ev -> {
            LocalDateTime now = LocalDateTime.now(ZoneId.of("America/Bogota"));
            dateLbl.setText("📅  " + now.format(dateFmt));
            timeLbl.setText(now.format(timeFmt));
        }));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();
        dateBox.getChildren().addAll(dateLbl, timeLbl);

        // Badge notificaciones en tiempo real
        int notiCount = contarNotificaciones();
        StackPane bell = new StackPane();
        Label bellIcon = label("🔔", 20, "#374151", false);
        if (notiCount > 0) {
            Circle badge = new Circle(8, Color.web(RED));
            badge.setTranslateX(10);
            badge.setTranslateY(-10);
            Label badgeNum = label(String.valueOf(notiCount), 8, WHITE, true);
            badgeNum.setTranslateX(10);
            badgeNum.setTranslateY(-10);
            bell.getChildren().addAll(bellIcon, badge, badgeNum);
        } else {
            bell.getChildren().add(bellIcon);
        }

        rightBox.getChildren().addAll(dateBox, bell);
        bar.getChildren().addAll(greeting, rightBox);
        return bar;
    }

    // ── Banner de emergencia ──────────────────────────────────────
    private HBox buildEmergencyBanner() {
        HBox banner = new HBox();
        banner.setAlignment(Pos.CENTER_LEFT);
        banner.setPadding(new Insets(16, 20, 16, 20));
        banner.setStyle("-fx-background-color: " + WHITE + "; -fx-background-radius: 12; -fx-border-color: " + BORDER + "; -fx-border-radius: 12;");
        shadow(banner);

        StackPane iconBox = new StackPane();
        Circle iconBg = new Circle(24, Color.web(RED_LIGHT));
        iconBox.getChildren().addAll(iconBg, label("🔔", 20, RED, false));

        VBox textBox = new VBox(4);
        textBox.setPadding(new Insets(0, 0, 0, 12));
        textBox.getChildren().addAll(
                label("Botón de emergencia", 15, RED, true),
                label("Envía alerta inmediata a vecinos y autoridades", 12, GRAY_TEXT, false)
        );
        HBox.setHgrow(textBox, Priority.ALWAYS);

        Button panicBtn = new Button("⚠  PÁNICO");
        panicBtn.setStyle("-fx-background-color: " + RED + "; -fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold; -fx-background-radius: 30; -fx-padding: 12 28; -fx-cursor: hand;");
        panicBtn.setOnMouseEntered(e -> panicBtn.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold; -fx-background-radius: 30; -fx-padding: 12 28; -fx-cursor: hand;"));
        panicBtn.setOnMouseExited(e -> panicBtn.setStyle("-fx-background-color: " + RED + "; -fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold; -fx-background-radius: 30; -fx-padding: 12 28; -fx-cursor: hand;"));
        panicBtn.setOnAction(e -> {
            Stage stage = (Stage) panicBtn.getScene().getWindow();
            EmergencyDialog.show(stage, usuarioActual, alertaService, barrioService);
        });

        banner.getChildren().addAll(iconBox, textBox, panicBtn);
        return banner;
    }

    // ── Stat cards — datos reales ─────────────────────────────────
    private HBox buildStatCards() {
        HBox row = new HBox(16);

        long incidentesMes = 0, alertasPendientes = 0, vecinosActivos = 0;

        if (alertaService != null && usuarioActual != null) {
            try {
                List<Alerta> misAlertas = alertaService.listar().stream()
                        .filter(a -> usuarioActual.getUsername().equals(
                        a.getUsuario() != null ? a.getUsuario().getUsername() : ""))
                        .toList();
                incidentesMes = misAlertas.stream()
                        .filter(a -> a.getFechaHora() != null
                        && a.getFechaHora().getMonth() == LocalDateTime.now().getMonth()
                        && a.getFechaHora().getYear() == LocalDateTime.now().getYear())
                        .count();
                alertasPendientes = misAlertas.stream()
                        .filter(a -> a.getEstado() == EstadoAlerta.PENDIENTE
                        || a.getEstado() == EstadoAlerta.EN_ATENCION)
                        .count();
            } catch (Exception ignored) {
            }
        }

        if (alertaService != null && usuarioActual != null
                && usuarioActual.getDireccion() != null
                && usuarioActual.getDireccion().getBarrio() != null) {
            try {
                String miBarrio = usuarioActual.getDireccion().getBarrio().getNombre();
                vecinosActivos = alertaService.listar().stream()
                        .filter(a -> a.getBarrio() != null && miBarrio.equalsIgnoreCase(a.getBarrio().getNombre()))
                        .map(a -> a.getUsuario() != null ? a.getUsuario().getUsername() : "")
                        .distinct().count();
            } catch (Exception ignored) {
            }
        }

        row.getChildren().addAll(
                statCard("📈", RED_LIGHT, RED, "Incidentes este mes", incidentesMes, "Mis alertas reportadas", RED),
                statCard("🕐", "#fff8e1", ORANGE, "Alertas pendientes", alertasPendientes, "PENDIENTE / EN PROCESO", GRAY_TEXT),
                statCard("👥", "#e8f5e9", GREEN, "Vecinos del barrio", vecinosActivos, "Reportaron en tu barrio", GREEN)
        );
        return row;
    }

    private VBox statCard(String icon, String bgIcon, String iconColor,
            String title, long value, String sub, String subColor) {

        VBox card = new VBox(6);

        card.setPadding(new Insets(14));

        card.setStyle("""
        -fx-background-color: white;
        -fx-background-radius: 18;
        -fx-border-radius: 18;
    """);

        card.setPrefWidth(210);

        card.setMinHeight(100);
        card.setMaxHeight(100);

        HBox.setHgrow(card, Priority.ALWAYS);

        shadow(card);

        
        // ICONO sin rectángulo
        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(48, 48);
        iconBox.setMaxSize(48, 48);
        iconBox.setStyle("-fx-background-color: " + bgIcon + ";"
                + "-fx-background-radius: 14;");

        Label iconLbl = label(icon, 20, iconColor, false);
        iconBox.getChildren().add(iconLbl);

        // TITULO
        Label titleLbl = label(title, 13, "#374151", true);

        // NUMERO
        Label val = new Label(String.valueOf(value));

        val.setFont(Font.font("System", FontWeight.BOLD, 28));

        val.setTextFill(Color.web("#111827"));

        // SUBTEXTO
        Label subLbl = label(sub, 11, subColor, false);

        VBox textBox = new VBox(3);

        textBox.getChildren().addAll(
                titleLbl,
                val,
                subLbl
        );

        HBox top = new HBox(14);

        top.setAlignment(Pos.CENTER_LEFT);

        top.getChildren().addAll(iconBox, textBox);

        card.getChildren().add(top);

        // Hover
        card.setOnMouseEntered(e -> card.setTranslateY(-2));

        card.setOnMouseExited(e -> card.setTranslateY(0));

        return card;
    }

    // ── Bottom row: lista alertas + mapa ─────────────────────────
    private HBox buildBottomRow() {
        HBox row = new HBox(16);
        row.getChildren().addAll(buildAlertsList(), buildMapPanel());
        return row;
    }

    private VBox buildAlertsList() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.setPrefWidth(480);
        card.setStyle("-fx-background-color: " + WHITE + "; -fx-background-radius: 12;");
        HBox.setHgrow(card, Priority.ALWAYS);
        shadow(card);

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        HBox titleRow = new HBox(8);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        titleRow.getChildren().addAll(label("📋", 16, BLUE, false), label("Alertas recientes", 15, "#111827", true));
        HBox.setHgrow(titleRow, Priority.ALWAYS);
        Label verTodas = label("Ver todas  >", 12, BLUE, false);
        verTodas.setCursor(javafx.scene.Cursor.HAND);
        verTodas.setOnMouseClicked(e -> root.setCenter(buildPlaceholder("Mis Alertas")));
        header.getChildren().addAll(titleRow, verTodas);
        card.getChildren().addAll(header, separator());

        if (alertaService != null) {
            try {
                List<Alerta> alertas = alertaService.listar();
                String miBarrio = usuarioActual != null
                        && usuarioActual.getDireccion() != null
                        && usuarioActual.getDireccion().getBarrio() != null
                        ? usuarioActual.getDireccion().getBarrio().getNombre() : null;

                List<Alerta> filtradas = miBarrio != null
                        ? alertas.stream().filter(a -> a.getBarrio() != null
                        && miBarrio.equalsIgnoreCase(a.getBarrio().getNombre())).toList()
                        : alertas;

                if (filtradas.isEmpty()) {
                    card.getChildren().add(label("No hay alertas en tu barrio", 13, GRAY_TEXT, false));
                } else {
                    filtradas.stream().limit(4).forEach(a -> {
                        card.getChildren().addAll(alertaItem(a), separator());
                    });
                }
            } catch (Exception e) {
                card.getChildren().add(label("Error al cargar alertas", 13, RED, false));
            }
        } else {
            card.getChildren().add(label("Sin conexión al servicio de alertas", 13, GRAY_TEXT, false));
        }
        return card;
    }

    private HBox alertaItem(Alerta a) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 0, 8, 0));
        row.setCursor(javafx.scene.Cursor.HAND);

        String estado = a.getEstado() != null ? a.getEstado().name() : "—";
        String dotColor = switch (estado) {
            case "PENDIENTE" ->
                RED;
            case "EN_PROCESO" ->
                ORANGE;
            case "RESUELTA" ->
                GREEN;
            default ->
                GRAY_TEXT;
        };
        Circle dotCircle = new Circle(5, Color.web(dotColor));

        StackPane iconBox = new StackPane();
        Rectangle bg = new Rectangle(36, 36);
        bg.setArcWidth(8);
        bg.setArcHeight(8);
        bg.setFill(Color.web(BG));
        String tipo = a.getTipoalerta() != null ? a.getTipoalerta().getNombre() : "Alerta";
        iconBox.getChildren().addAll(bg, label("🔔", 16, dotColor, false));

        VBox text = new VBox(2);
        HBox.setHgrow(text, Priority.ALWAYS);
        String desc = a.getDescripcion() != null && a.getDescripcion().length() > 45
                ? a.getDescripcion().substring(0, 45) + "…" : a.getDescripcion();
        String barrio = a.getBarrio() != null ? " · " + a.getBarrio().getNombre() : "";
        String reportadoPor = a.getUsuario() != null ? " · " + a.getUsuario().getPrimer_nombre() : "";
        text.getChildren().addAll(
                label(tipo + barrio, 13, "#111827", false),
                label((desc != null ? desc : "—") + reportadoPor, 11, GRAY_TEXT, false)
        );
        row.getChildren().addAll(dotCircle, iconBox, text, label(">", 14, GRAY_TEXT, false));
        return row;
    }

    private VBox buildMapPanel() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.setPrefWidth(380);
        card.setStyle("-fx-background-color: " + WHITE + "; -fx-background-radius: 12;");
        shadow(card);

        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getChildren().addAll(label("📍", 16, BLUE, false), label("Mapa del barrio", 15, "#111827", true));

        StackPane mapArea = new StackPane();
        mapArea.setPrefHeight(220);
        Rectangle mapBg = new Rectangle();
        mapBg.setFill(Color.web("#d1e8d1"));
        mapBg.widthProperty().bind(mapArea.widthProperty());
        mapBg.heightProperty().bind(mapArea.heightProperty());
        mapBg.setArcWidth(10);
        mapBg.setArcHeight(10);

        Pane streets = new Pane();
        streets.setPrefSize(340, 200);
        for (int i = 0; i < 4; i++) {
            Rectangle h = new Rectangle(340, 3);
            h.setFill(Color.web("#b8d4b8"));
            h.setY(40 + i * 45);
            streets.getChildren().add(h);
        }
        for (int i = 0; i < 5; i++) {
            Rectangle v = new Rectangle(3, 200);
            v.setFill(Color.web("#b8d4b8"));
            v.setX(40 + i * 65);
            streets.getChildren().add(v);
        }
        streets.getChildren().addAll(mapDot(80, 60, RED), mapDot(200, 40, ORANGE), mapDot(300, 55, GREEN), mapDot(60, 140, GREEN), mapDot(310, 150, RED));

        VBox popup = new VBox(6);
        popup.setPadding(new Insets(10, 14, 10, 14));
        popup.setStyle("-fx-background-color: white; -fx-background-radius: 8;");
        popup.setEffect(new DropShadow(8, Color.web("#0000001a")));
        popup.setTranslateX(30);
        popup.setTranslateY(10);
        String barrioDisplay = usuarioActual != null && usuarioActual.getDireccion() != null
                && usuarioActual.getDireccion().getBarrio() != null
                ? usuarioActual.getDireccion().getBarrio().getNombre() : "Tu barrio";
        popup.getChildren().addAll(label("Mapa interactivo", 12, "#374151", true), label(barrioDisplay, 11, BLUE, false));
        mapArea.getChildren().addAll(mapBg, streets, popup);

        HBox legend = new HBox(16);
        legend.setAlignment(Pos.CENTER);
        legend.getChildren().addAll(legendItem(RED, "Activo"), legendItem(ORANGE, "En revisión"), legendItem(GREEN, "Resuelto"));
        card.getChildren().addAll(header, mapArea, legend);
        return card;
    }

    // =========================================================================
    // FOOTER
    // =========================================================================
    private HBox buildFooter() {
        HBox footer = new HBox();
        footer.setPadding(new Insets(12, 20, 12, 20));
        footer.setStyle("-fx-background-color: " + BLUE_LIGHT + "; -fx-background-radius: 10;");
        HBox left = new HBox(10);
        left.setAlignment(Pos.CENTER_LEFT);
        VBox leftText = new VBox(2);
        leftText.getChildren().addAll(label("Tu información está protegida", 13, BLUE, true), label("Todas tus alertas son anónimas y confidenciales.", 11, GRAY_TEXT, false));
        left.getChildren().addAll(label("🔒", 20, BLUE, false), leftText);
        HBox.setHgrow(left, Priority.ALWAYS);
        HBox right = new HBox(10);
        right.setAlignment(Pos.CENTER_RIGHT);
        VBox rightText = new VBox(2);
        rightText.setAlignment(Pos.CENTER_RIGHT);
        rightText.getChildren().addAll(label("100% Anónimo", 13, BLUE, true), label("Tu identidad no será revelada.", 11, GRAY_TEXT, false));
        right.getChildren().addAll(rightText, label("🛡", 20, BLUE, false));
        footer.getChildren().addAll(left, right);
        return footer;
    }

    // =========================================================================
    // CERRAR SESIÓN
    // =========================================================================
    private void cerrarSesion() {
        VBox bye = new VBox(20);
        bye.setAlignment(Pos.CENTER);
        bye.setStyle("-fx-background-color: " + BG + ";");
        Label icon = new Label("👋");
        icon.setFont(Font.font(70));
        Label title = new Label("Sesión cerrada");
        title.setFont(Font.font("System", FontWeight.BOLD, 30));
        title.setTextFill(Color.web("#111827"));
        Label msg = new Label("Cerrando aplicación...");
        msg.setTextFill(Color.GRAY);
        bye.getChildren().addAll(icon, title, msg);
        root.setCenter(bye);
        new Timeline(new KeyFrame(Duration.seconds(2),
                ev -> ((Stage) root.getScene().getWindow()).close())).play();
    }

    // =========================================================================
    // CONTADORES EN VIVO desde BD
    // =========================================================================
    private int contarAlertasActivas() {
        if (alertaService == null) {
            return 0;
        }
        try {
            return (int) alertaService.listar().stream()
                    .filter(a -> a.getEstado() == EstadoAlerta.PENDIENTE
                    || a.getEstado() == EstadoAlerta.EN_ATENCION)
                    .count();
        } catch (Exception e) {
            return 0;
        }
    }

    private int contarNotificaciones() {
        if (notificacionService == null || usuarioActual == null) {
            return 0;
        }
        try {
            return (int) notificacionService.listar().stream()
                    .filter(n -> usuarioActual.getCorreo() != null
                    && usuarioActual.getCorreo().equalsIgnoreCase(n.getCorreodestinatario()))
                    .count();
        } catch (Exception e) {
            return 0;
        }
    }

    // =========================================================================
    // PLACEHOLDER
    // =========================================================================
    private ScrollPane buildPlaceholder(String nombre) {
        VBox box = new VBox(20);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(40));
        box.setStyle("-fx-background-color: " + BG + ";");
        Label icon = new Label("🚧");
        icon.setFont(Font.font(70));
        Label title = new Label(nombre);
        title.setFont(Font.font("System", FontWeight.BOLD, 32));
        title.setTextFill(Color.web("#111827"));
        Label msg = new Label("Pantalla en construcción");
        msg.setFont(Font.font(18));
        msg.setTextFill(Color.GRAY);
        Button volver = new Button("Volver al Dashboard");
        volver.setStyle("-fx-background-color: " + BLUE + "; -fx-text-fill: white; -fx-font-size: 14px; -fx-background-radius: 10; -fx-padding: 10 20; -fx-cursor: hand;");
        volver.setOnAction(e -> root.setCenter(buildMainContent()));
        box.getChildren().addAll(icon, title, msg, volver);
        ScrollPane scroll = new ScrollPane(box);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: " + BG + "; -fx-background-color: " + BG + ";");
        return scroll;
    }

    // =========================================================================
    // HELPERS UI
    // =========================================================================
    private VBox tituloVista(String titulo, String subtitulo) {
        VBox v = new VBox(4);
        Label t = new Label(titulo);
        t.setFont(Font.font("System", FontWeight.BOLD, 26));
        t.setTextFill(Color.web("#111827"));
        v.getChildren().addAll(t, label(subtitulo, 13, GRAY_TEXT, false));
        return v;
    }

    private Circle mapDot(double x, double y, String color) {
        Circle c = new Circle(7, Color.web(color));
        c.setCenterX(x);
        c.setCenterY(y);
        c.setStroke(Color.WHITE);
        c.setStrokeWidth(2);
        return c;
    }

    private HBox legendItem(String color, String text) {
        HBox item = new HBox(6);
        item.setAlignment(Pos.CENTER_LEFT);
        item.getChildren().addAll(new Circle(5, Color.web(color)), label(text, 11, GRAY_TEXT, false));
        return item;
    }

    private String estadoBg(String estado) {
        return switch (estado) {
            case "PENDIENTE" ->
                RED_LIGHT;
            case "EN_PROCESO" ->
                "#fff8e1";
            case "RESUELTA" ->
                "#e8f5e9";
            default ->
                "#f3f4f6";
        };
    }

    private Label label(String text, double size, String color, boolean bold) {
        Label lbl = new Label(text);
        lbl.setFont(bold ? Font.font("System", FontWeight.BOLD, size) : Font.font("System", size));
        lbl.setTextFill(Color.web(color));
        return lbl;
    }

    private Region separator() {
        Region sep = new Region();
        sep.setPrefHeight(1);
        sep.setStyle("-fx-background-color: " + BORDER + ";");
        return sep;
    }

    private void shadow(Region node) {

        DropShadow shadow = new DropShadow();

        shadow.setRadius(18);

        shadow.setOffsetY(4);

        shadow.setColor(Color.rgb(15, 23, 42, 0.10));

        node.setEffect(shadow);
    }

    private void mostrarAlerta(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
