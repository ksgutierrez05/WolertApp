/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.view;

/**
 *
 * @author Maria Cristina
 */


import java.time.format.DateTimeFormatter;
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
import sistemagestion.model.Notificacion;
import sistemagestion.model.Usuario;
import sistemagestion.service.NotificacionService;

public class NotificacionesView {

    private static final String WHITE     = "#ffffff";
    private static final String BG        = "#f4f6fb";
    private static final String RED       = "#e53935";
    private static final String ORANGE    = "#fb8c00";
    private static final String GREEN     = "#43a047";
    private static final String BLUE      = "#1565c0";
    private static final String GRAY_TEXT = "#6b7280";
    private static final String BORDER    = "#e5e7eb";

    private final Usuario             usuarioActual;
    private final NotificacionService notificacionService;
    private final BorderPane          root;  // para el botón "volver"

    public NotificacionesView(Usuario usuarioActual,
                              NotificacionService notificacionService,
                              BorderPane root) {
        this.usuarioActual       = usuarioActual;
        this.notificacionService = notificacionService;
        this.root                = root;
    }

    // =========================================================================
    // PUNTO DE ENTRADA
    // =========================================================================
    public ScrollPane getView() {
        VBox content = new VBox(16);
        content.setPadding(new Insets(22));
        content.setStyle("-fx-background-color: " + BG + ";");

        content.getChildren().addAll(
                buildHeader(),
                buildStatsRow(),
                buildPanel()
        );

        // Botón volver
        Button volver = new Button("← Volver al Dashboard");
        volver.setStyle(
                "-fx-background-color:" + BLUE + ";-fx-text-fill:white;"
                + "-fx-font-size:13px;-fx-background-radius:8;"
                + "-fx-padding:9 18;-fx-cursor:hand;");
        volver.setOnMouseEntered(e -> volver.setStyle(
                "-fx-background-color:#0d47a1;-fx-text-fill:white;"
                + "-fx-font-size:13px;-fx-background-radius:8;"
                + "-fx-padding:9 18;-fx-cursor:hand;"));
        volver.setOnMouseExited(e -> volver.setStyle(
                "-fx-background-color:" + BLUE + ";-fx-text-fill:white;"
                + "-fx-font-size:13px;-fx-background-radius:8;"
                + "-fx-padding:9 18;-fx-cursor:hand;"));
        volver.setOnAction(e -> {
            if (root != null) {
                // Vuelve al dashboard del usuario
                UsuarioApp app = new UsuarioApp(usuarioActual);
                // Solo recarga el centro — no abre nueva ventana
                root.setCenter(buildPlaceholderVolver());
            }
        });
        content.getChildren().add(volver);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background: " + BG + "; -fx-background-color: " + BG + ";");
        return scroll;
    }

    // =========================================================================
    // HEADER
    // =========================================================================
    private HBox buildHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titles = new VBox(4);
        Label title = new Label("🔔 Mis notificaciones");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#111827"));
        Label sub = label("Todas las notificaciones recibidas en tu cuenta", 13, GRAY_TEXT, false);
        titles.getChildren().addAll(title, sub);

        header.getChildren().add(titles);
        return header;
    }

    // =========================================================================
    // STATS
    // =========================================================================
    private HBox buildStatsRow() {
        HBox row = new HBox(14);

        long total    = 0, leidas = 0, pendientes = 0, errores = 0;
        try {
            List<Notificacion> lista = cargarNotificaciones();
            total      = lista.size();
            leidas     = lista.stream().filter(n -> n.getEstado() != null
                            && n.getEstado().name().equals("LEIDA")).count();
            pendientes = lista.stream().filter(n -> n.getEstado() != null
                            && n.getEstado().name().equals("PENDIENTE")).count();
            errores    = lista.stream().filter(n -> n.getEstado() != null
                            && n.getEstado().name().equals("ERROR")).count();
        } catch (Exception ignored) {}

        row.getChildren().addAll(
                statCard("#e8f0fe", BLUE,   "🔔", "Total",      total,      "Recibidas"),
                statCard("#e8f5e9", GREEN,  "✅", "Leídas",     leidas,     "Ya revisadas"),
                statCard("#fff3e0", ORANGE, "🕐", "Pendientes", pendientes, "Sin leer"),
                statCard("#fff0f0", RED,    "❌", "Errores",    errores,    "No entregadas")
        );
        return row;
    }

    private VBox statCard(String bg, String color, String icon,
                          String title, long value, String sub) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(16, 18, 14, 18));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 14;");
        HBox.setHgrow(card, Priority.ALWAYS);
        shadow(card);

        Label iconLbl  = label(icon, 22, color, false);
        Label titleLbl = label(title, 12, GRAY_TEXT, false);

        Label valLbl = new Label(String.valueOf(value));
        valLbl.setFont(Font.font("System", FontWeight.BOLD, 32));
        valLbl.setTextFill(Color.web("#111827"));

        Label subLbl = label(sub, 10, GRAY_TEXT, false);
        card.getChildren().addAll(iconLbl, titleLbl, valLbl, subLbl);
        card.setOnMouseEntered(e -> card.setTranslateY(-2));
        card.setOnMouseExited(e  -> card.setTranslateY(0));
        return card;
    }

    // =========================================================================
    // PANEL PRINCIPAL
    // =========================================================================
    private VBox buildPanel() {
        VBox panel = new VBox(0);
        panel.setStyle("-fx-background-color: white; -fx-background-radius: 12;");
        shadow(panel);

        // Header del panel
        HBox panelHeader = new HBox();
        panelHeader.setPadding(new Insets(14, 16, 14, 16));
        panelHeader.setAlignment(Pos.CENTER_LEFT);
        panelHeader.setStyle(
                "-fx-background-color:#f8fafc;-fx-background-radius:12 12 0 0;"
                + "-fx-border-color:transparent transparent " + BORDER + " transparent;"
                + "-fx-border-width:0 0 1 0;");
        Label panelTitle = label("Listado de notificaciones", 14, "#111827", true);
        panelHeader.getChildren().add(panelTitle);
        panel.getChildren().add(panelHeader);

        try {
            List<Notificacion> lista = cargarNotificaciones();

            if (lista.isEmpty()) {
                VBox vacio = new VBox(12);
                vacio.setAlignment(Pos.CENTER);
                vacio.setPadding(new Insets(50));
                Label ico = new Label("🔔");
                ico.setFont(Font.font(50));
                Label msg = label("No tienes notificaciones", 15, GRAY_TEXT, false);
                Label sub = label("Aquí aparecerán las notificaciones de tus alertas", 12, GRAY_TEXT, false);
                vacio.getChildren().addAll(ico, msg, sub);
                panel.getChildren().add(vacio);
            } else {
                for (int i = 0; i < lista.size(); i++) {
                    panel.getChildren().add(buildFila(lista.get(i), i));
                }
            }

        } catch (Exception e) {
            VBox error = new VBox(10);
            error.setPadding(new Insets(20));
            error.getChildren().add(
                    label("Error al cargar notificaciones: " + e.getMessage(), 12, RED, false));
            panel.getChildren().add(error);
        }

        return panel;
    }

    // =========================================================================
    // FILA
    // =========================================================================
    private HBox buildFila(Notificacion n, int index) {
        HBox fila = new HBox(12);
        fila.setPadding(new Insets(14, 16, 14, 16));
        fila.setAlignment(Pos.CENTER_LEFT);

        String bgColor = index % 2 == 0 ? WHITE : "#fafbfd";
        String baseStyle = "-fx-background-color:" + bgColor + ";"
                + "-fx-border-color:transparent transparent " + BORDER + " transparent;"
                + "-fx-border-width:0 0 1 0;";
        fila.setStyle(baseStyle);
        fila.setOnMouseEntered(e -> fila.setStyle(
                "-fx-background-color:#EEF2FF;"
                + "-fx-border-color:transparent transparent " + BORDER + " transparent;"
                + "-fx-border-width:0 0 1 0;-fx-cursor:hand;"));
        fila.setOnMouseExited(e -> fila.setStyle(baseStyle));

        // Ícono circular
        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(42, 42);
        iconBox.setMinSize(42, 42);
        iconBox.setMaxSize(42, 42);
        iconBox.setStyle("-fx-background-color:#e8f0fe;-fx-background-radius:50%;");
        Label ico = new Label("🔔");
        ico.setFont(Font.font(18));
        iconBox.getChildren().add(ico);

        // Texto
        VBox texto = new VBox(4);
        HBox.setHgrow(texto, Priority.ALWAYS);

        String msg = n.getMensaje() != null ? n.getMensaje() : "—";
        Label msgLbl = new Label(msg);
        msgLbl.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#111827;");
        msgLbl.setWrapText(true);

        // Destinatario + fecha
        String dest  = n.getCorreodestinatario() != null ? n.getCorreodestinatario() : "—";
        String fecha = n.getFechahora() != null
                ? n.getFechahora().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                : "—";
        Label subLbl = label("Para: " + dest + "  ·  " + fecha, 11, GRAY_TEXT, false);

        // Alerta asociada
        String alertaDesc = n.getAlerta() != null && n.getAlerta().getDescripcion() != null
                ? n.getAlerta().getDescripcion() : null;
        texto.getChildren().addAll(msgLbl, subLbl);
        if (alertaDesc != null) {
            texto.getChildren().add(label("Alerta: " + alertaDesc, 11, GRAY_TEXT, false));
        }

        // Badge de estado
        String estadoStr  = n.getEstado() != null ? n.getEstado().name() : "—";
        String badgeColor = switch (estadoStr) {
            case "LEIDA"     -> GREEN;
            case "ENVIADA"   -> BLUE;
            case "ERROR"     -> RED;
            default          -> ORANGE;
        };
        Label badge = new Label(estadoStr);
        badge.setStyle(
                "-fx-background-color:" + badgeColor + "22;"
                + "-fx-text-fill:" + badgeColor + ";"
                + "-fx-font-size:11px;-fx-font-weight:bold;"
                + "-fx-background-radius:20;-fx-padding:5 12;");

        // Punto de color
        Circle dot = new Circle(5, Color.web(badgeColor));

        fila.getChildren().addAll(dot, iconBox, texto, badge);
        return fila;
    }

    // =========================================================================
    // CARGAR NOTIFICACIONES — filtradas por el correo del usuario actual
    // =========================================================================
    private List<Notificacion> cargarNotificaciones() {
        if (notificacionService == null || usuarioActual == null) return List.of();
        try {
            return notificacionService.listar().stream()
                    .filter(n -> usuarioActual.getCorreo() != null
                            && usuarioActual.getCorreo()
                                   .equalsIgnoreCase(n.getCorreodestinatario()))
                    .sorted((a, b) -> {
                        if (a.getFechahora() == null || b.getFechahora() == null) return 0;
                        return b.getFechahora().compareTo(a.getFechahora());
                    })
                    .toList();
        } catch (Exception e) {
            return List.of();
        }
    }

    // =========================================================================
    // HELPERS
    // =========================================================================
    private ScrollPane buildPlaceholderVolver() {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(40));
        box.setStyle("-fx-background-color: " + BG + ";");
        box.getChildren().add(label("Volviendo...", 16, GRAY_TEXT, false));
        ScrollPane s = new ScrollPane(box);
        s.setFitToWidth(true);
        s.setStyle("-fx-background: " + BG + "; -fx-background-color: " + BG + ";");
        return s;
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
        DropShadow ds = new DropShadow(10, 0, 2, Color.web("#0000001a"));
        node.setEffect(ds);
    }
}