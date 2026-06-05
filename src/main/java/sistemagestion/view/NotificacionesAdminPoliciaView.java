package sistemagestion.view;

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

import java.util.List;

import sistemagestion.model.*;
import sistemagestion.service.NotificacionService;

public class NotificacionesAdminPoliciaView {

    // ── Paleta consistente con la app ─────────────────────────────
    private static final String BG = "#f4f6fb";
    private static final String WHITE = "#ffffff";
    private static final String BLUE = "#1565c0";
    private static final String BLUE_LIGHT = "#e8f0fe";
    private static final String GREEN = "#43a047";
    private static final String GREEN_LIGHT = "#e8f5e9";
    private static final String RED = "#ef5350";
    private static final String RED_LIGHT = "#fff0f0";
    private static final String ORANGE = "#fb8c00";
    private static final String ORANGE_LIGHT = "#fff8e1";
    private static final String GRAY = "#6b7280";
    private static final String BORDER = "#e5e7eb";
    private static final String TEXT_DARK = "#111827";
    private static final String FA = "'Font Awesome 6 Free Solid'";

    private final NotificacionService notificacionService;

    // Estado para filtrado
    private VBox listaContainer;
    private List<Notificacion> todasLasNotis;

    public NotificacionesAdminPoliciaView(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    // ── Build principal ──────────────────────────────────────────
    public ScrollPane build() {
        todasLasNotis = cargarLista();

        VBox content = new VBox(24);
        content.setPadding(new Insets(28));
        content.setStyle("-fx-background-color:" + BG + ";");

        // Solo título simple
        Label title = new Label("Notificaciones");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#111827"));
        Label sub = label("Registro de notificaciones enviadas al sistema", 13, GRAY, false);
        VBox titleBox = new VBox(4, title, sub);

        content.getChildren().addAll(
                titleBox,
                buildListaCard()
        );

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setStyle("-fx-background:" + BG + "; -fx-background-color:" + BG + ";");
        return scroll;
    }

    // ── Top bar ──────────────────────────────────────────────────
    private HBox buildTopBar() {
        HBox bar = new HBox(0);
        bar.setPadding(new Insets(24, 28, 24, 28));
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle("-fx-background-color: linear-gradient(to right, #e8f3fa, #e6f4fc);"
                + "-fx-background-radius: 14;");
        bar.setEffect(new DropShadow(12, 0, 4, Color.web("#1a237e30")));

        // Ícono
        StackPane iconWrap = new StackPane();
        iconWrap.setPrefSize(56, 56);
        iconWrap.setMinSize(56, 56);
        Circle iconCircle = new Circle(28, Color.web("#ffffff80"));
        Label iconLbl = new Label("\uf0f3");
        iconLbl.setStyle("-fx-font-family:" + FA + ";-fx-font-size:22px;-fx-text-fill:" + BLUE + ";");
        iconWrap.getChildren().addAll(iconCircle, iconLbl);

        // Textos
        VBox textBox = new VBox(5);
        textBox.setPadding(new Insets(0, 0, 0, 16));
        HBox.setHgrow(textBox, Priority.ALWAYS);
        Label title = new Label("Notificaciones");
        title.setFont(Font.font("System", FontWeight.BOLD, 26));
        title.setTextFill(Color.web("#1a237e"));
        Label subtitle = label(
                "Registro de notificaciones enviadas a usuarios y unidades del sistema",
                12, GRAY, false);
        subtitle.setWrapText(true);
        textBox.getChildren().addAll(title, subtitle);

        // Contador total
        VBox counter = new VBox(2);
        counter.setAlignment(Pos.CENTER_RIGHT);
        Label num = new Label(String.valueOf(todasLasNotis.size()));
        num.setStyle("-fx-font-size:40px;-fx-font-weight:bold;-fx-text-fill:" + BLUE + ";");
        Label lbl = label("notificaciones", 11, GRAY, false);
        counter.getChildren().addAll(num, lbl);

        bar.getChildren().addAll(iconWrap, textBox, counter);
        return bar;
    }

    // ── Strip de estadísticas ─────────────────────────────────────
    private HBox buildStatStrip() {
        long leidas = count(EstadoNotificacion.LEIDA);
        long enviadas = count(EstadoNotificacion.ENVIADA);
        long errores = count(EstadoNotificacion.ERROR);
        long otras = todasLasNotis.size() - leidas - enviadas - errores;

        HBox strip = new HBox(14);
        strip.getChildren().addAll(
                miniStat("\uf058", "Leídas", leidas, GREEN, GREEN_LIGHT),
                miniStat("\uf1d8", "Enviadas", enviadas, BLUE, BLUE_LIGHT),
                miniStat("\uf057", "Errores", errores, RED, RED_LIGHT),
                miniStat("\uf017", "Otras", otras, ORANGE, ORANGE_LIGHT)
        );
        return strip;
    }

    private VBox miniStat(String icon, String lbl, long val,
            String color, String bg) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(16, 20, 16, 20));
        card.setStyle("-fx-background-color:white;-fx-background-radius:14;");
        card.setEffect(new DropShadow(10, 0, 2, Color.web("#0000001a")));
        HBox.setHgrow(card, Priority.ALWAYS);

        StackPane icoWrap = new StackPane();
        icoWrap.setPrefSize(40, 40);
        icoWrap.setMinSize(40, 40);
        icoWrap.setMaxSize(40, 40);
        Rectangle icoBg = new Rectangle(40, 40);
        icoBg.setArcWidth(12);
        icoBg.setArcHeight(12);
        icoBg.setFill(Color.web(bg));
        Label icoLbl = new Label(icon);
        icoLbl.setStyle("-fx-font-family:" + FA + ";-fx-font-size:16px;-fx-text-fill:" + color + ";");
        icoWrap.getChildren().addAll(icoBg, icoLbl);

        Label valLbl = new Label(String.valueOf(val));
        valLbl.setStyle("-fx-font-size:30px;-fx-font-weight:bold;-fx-text-fill:" + color + ";");

        card.getChildren().addAll(icoWrap, valLbl, label(lbl, 11, GRAY, false));
        card.setOnMouseEntered(e -> card.setTranslateY(-3));
        card.setOnMouseExited(e -> card.setTranslateY(0));
        return card;
    }

    // ── Filtros por estado ────────────────────────────────────────
    private HBox buildFiltros() {
        HBox bar = new HBox(10);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(14, 18, 14, 18));
        bar.setStyle("-fx-background-color:white;-fx-background-radius:12;");
        bar.setEffect(new DropShadow(10, 0, 2, Color.web("#0000001a")));

        HBox searchBox = new HBox(8);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setStyle("-fx-background-color:#f5f7fb;-fx-background-radius:10;-fx-padding:0 14;");
        searchBox.setPrefHeight(40);
        HBox.setHgrow(searchBox, Priority.ALWAYS);

        Label searchIcon = new Label("\uf002");
        searchIcon.setStyle("-fx-font-family:" + FA + ";-fx-font-size:13px;-fx-text-fill:#9ca3af;");
        TextField tf = new TextField();
        tf.setPromptText("Buscar por destinatario o mensaje...");
        tf.setStyle("-fx-background-color:transparent;-fx-border-color:transparent;"
                + "-fx-font-size:13px;-fx-text-fill:" + TEXT_DARK + ";");
        tf.setPrefHeight(40);
        HBox.setHgrow(tf, Priority.ALWAYS);
        tf.textProperty().addListener((obs, o, n) -> filtrar(n, "Todos"));
        searchBox.getChildren().addAll(searchIcon, tf);

        bar.getChildren().add(searchBox);
        return bar;
    }

    private void filtrar(String texto, String estado) {
        String txt = texto == null ? "" : texto.toLowerCase().trim();
        List<Notificacion> resultado = todasLasNotis.stream()
                .filter(n -> {
                    if (!"Todos".equals(estado)) {
                        if (n.getEstado() == null) {
                            return false;
                        }
                        if (!n.getEstado().name().equals(estado)) {
                            return false;
                        }
                    }
                    if (!txt.isEmpty()) {
                        String dest = n.getCorreodestinatario() != null
                                ? n.getCorreodestinatario().toLowerCase() : "";
                        String msg = n.getMensaje() != null
                                ? n.getMensaje().toLowerCase() : "";
                        return dest.contains(txt) || msg.contains(txt);
                    }
                    return true;
                }).toList();
        renderLista(resultado);
    }

    // ── Card principal de lista ───────────────────────────────────
    private VBox buildListaCard() {
        VBox card = new VBox(0);
        card.setStyle("-fx-background-color:white;-fx-background-radius:18;");
        card.setEffect(new DropShadow(14, 0, 3, Color.web("#0000001a")));

        // Cabecera
        HBox cardHeader = new HBox(10);
        cardHeader.setPadding(new Insets(18, 24, 16, 24));
        cardHeader.setAlignment(Pos.CENTER_LEFT);
        cardHeader.setStyle("-fx-border-color:transparent transparent " + BORDER
                + " transparent;-fx-border-width:0 0 1 0;");

        Rectangle accentBar = new Rectangle(4, 20);
        accentBar.setArcWidth(4);
        accentBar.setArcHeight(4);
        accentBar.setFill(Color.web(BLUE));

        Label cardTitle = label("Listado de notificaciones", 15, TEXT_DARK, true);
        cardHeader.getChildren().addAll(accentBar, pad(8), cardTitle);
        card.getChildren().add(cardHeader);

        listaContainer = new VBox(0);
        card.getChildren().add(listaContainer);

        // Footer contador
        HBox footer = new HBox();
        footer.setPadding(new Insets(10, 24, 12, 24));
        footer.setStyle("-fx-background-color:#f8fafc;-fx-background-radius:0 0 18 18;"
                + "-fx-border-color:" + BORDER + " transparent transparent transparent;"
                + "-fx-border-width:1 0 0 0;");
        Label footerLbl = label("", 12, GRAY, false);
        footer.getChildren().add(footerLbl);
        card.getChildren().add(footer);

        // Guardar referencia al footer label para actualizar
        card.setUserData(footerLbl);

        renderLista(todasLasNotis);
        return card;
    }

    // ── Render de la lista ────────────────────────────────────────
    private void renderLista(List<Notificacion> lista) {
        listaContainer.getChildren().clear();

        if (lista.isEmpty()) {
            listaContainer.getChildren().add(buildEmptyState());
        } else {
            for (int i = 0; i < lista.size(); i++) {
                listaContainer.getChildren().add(buildRow(lista.get(i), i % 2 == 0));
                if (i < lista.size() - 1) {
                    Region sep = new Region();
                    sep.setPrefHeight(1);
                    sep.setStyle("-fx-background-color:" + BORDER + ";");
                    listaContainer.getChildren().add(sep);
                }
            }
        }

        // Actualizar footer
        if (listaContainer.getParent() instanceof VBox card) {
            Object ud = card.getUserData();
            if (ud instanceof Label lbl) {
                lbl.setText("Mostrando " + lista.size() + " notificación"
                        + (lista.size() != 1 ? "es" : ""));
            }
        }
    }

    // ── Fila individual ───────────────────────────────────────────
    private HBox buildRow(Notificacion n, boolean par) {
        String dest = n.getCorreodestinatario() != null
                ? n.getCorreodestinatario() : "Sin destinatario";
        String msg = (n.getMensaje() != null && !n.getMensaje().isBlank())
                ? n.getMensaje() : "Sin mensaje";

        String[] sc = stateColors(n.getEstado());
        String color = sc[0];
        String bgColor = sc[1];
        String faIcon = sc[2];
        String estadoTxt = n.getEstado() != null
                ? n.getEstado().name().replace("_", " ") : "PENDIENTE";

        String rowBg = par ? WHITE : "#fafbfd";
        String rowBgH = "#EEF2FF";

        HBox row = new HBox(14);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(14, 24, 14, 24));
        row.setStyle("-fx-background-color:" + rowBg + ";");
        row.setOnMouseEntered(e -> row.setStyle(
                "-fx-background-color:" + rowBgH + ";-fx-cursor:hand;"));
        row.setOnMouseExited(e -> row.setStyle(
                "-fx-background-color:" + rowBg + ";"));

        // Ícono de estado
        StackPane stateBox = new StackPane();
        stateBox.setPrefSize(42, 42);
        stateBox.setMinSize(42, 42);
        stateBox.setMaxSize(42, 42);
        Rectangle stateBgRect = new Rectangle(42, 42);
        stateBgRect.setArcWidth(12);
        stateBgRect.setArcHeight(12);
        stateBgRect.setFill(Color.web(bgColor));
        Label stateIco = new Label(faIcon);
        stateIco.setStyle("-fx-font-family:" + FA + ";-fx-font-size:16px;-fx-text-fill:" + color + ";");
        stateBox.getChildren().addAll(stateBgRect, stateIco);

        // Textos
        VBox textBox = new VBox(4);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        // Destinatario con avatar inicial
        HBox destRow = new HBox(8);
        destRow.setAlignment(Pos.CENTER_LEFT);

        String inicial = dest.substring(0, 1).toUpperCase();
        StackPane av = new StackPane();
        av.setPrefSize(24, 24);
        av.setMinSize(24, 24);
        av.setMaxSize(24, 24);
        Circle avC = new Circle(12, Color.web(avatarColor(dest)));
        Label avL = label(inicial, 9, WHITE, true);
        av.getChildren().addAll(avC, avL);

        Label destLbl = label(dest, 13, TEXT_DARK, true);
        destRow.getChildren().addAll(av, destLbl);

        // Mensaje truncado
        String msgTrunc = msg.length() > 90 ? msg.substring(0, 87) + "…" : msg;
        Label msgLbl = label(msgTrunc, 12, GRAY, false);
        msgLbl.setWrapText(false);

        textBox.getChildren().addAll(destRow, msgLbl);

        // Badge estado + ícono correo
        HBox right = new HBox(8);
        right.setAlignment(Pos.CENTER_RIGHT);

        Label mailIco = new Label("\uf0e0");
        mailIco.setStyle("-fx-font-family:" + FA + ";-fx-font-size:11px;-fx-text-fill:#d1d5db;");

        Label badge = new Label(estadoTxt);
        badge.setPadding(new Insets(3, 10, 3, 10));
        badge.setStyle("-fx-background-color:" + bgColor + ";"
                + "-fx-background-radius:20;"
                + "-fx-text-fill:" + color + ";"
                + "-fx-font-weight:bold;-fx-font-size:10px;");

        right.getChildren().addAll(mailIco, badge);

        row.getChildren().addAll(stateBox, textBox, right);
        return row;
    }

    // ── Estado vacío ─────────────────────────────────────────────
    private VBox buildEmptyState() {
        VBox box = new VBox(12);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(60, 0, 60, 0));

        StackPane icoWrap = new StackPane();
        icoWrap.setPrefSize(72, 72);
        icoWrap.setMinSize(72, 72);
        Circle bg = new Circle(36, Color.web(BLUE_LIGHT));
        Label ico = new Label("\uf0f3");
        ico.setStyle("-fx-font-family:" + FA + ";-fx-font-size:28px;-fx-text-fill:" + BLUE + ";");
        icoWrap.getChildren().addAll(bg, ico);

        Label title = label("Sin notificaciones registradas", 15, TEXT_DARK, true);
        Label sub = label("Las notificaciones enviadas aparecerán aquí.", 12, GRAY, false);

        box.getChildren().addAll(icoWrap, title, sub);
        return box;
    }

    // ── Helpers ───────────────────────────────────────────────────
    private String[] stateColors(EstadoNotificacion e) {
        if (e == null) {
            return new String[]{ORANGE, ORANGE_LIGHT, "\uf017"};
        }
        return switch (e) {
            case LEIDA ->
                new String[]{GREEN, GREEN_LIGHT, "\uf058"};
            case ENVIADA ->
                new String[]{BLUE, BLUE_LIGHT, "\uf1d8"};
            case ERROR ->
                new String[]{RED, RED_LIGHT, "\uf057"};
            default ->
                new String[]{ORANGE, ORANGE_LIGHT, "\uf017"};
        };
    }

    private long count(EstadoNotificacion e) {
        return todasLasNotis.stream()
                .filter(n -> n.getEstado() == e).count();
    }

    private List<Notificacion> cargarLista() {
        try {
            return notificacionService.listar();
        } catch (Exception e) {
            return List.of();
        }
    }

    private static final String[] AVATAR_COLORS = {
        "#1565c0", "#2e7d32", "#6a1b9a", "#c62828",
        "#e65100", "#00695c", "#283593", "#4e342e"
    };

    private String avatarColor(String s) {
        if (s == null || s.isBlank()) {
            return AVATAR_COLORS[0];
        }
        return AVATAR_COLORS[Math.abs(s.hashCode()) % AVATAR_COLORS.length];
    }

    private Region pad(double w) {
        Region r = new Region();
        r.setPrefWidth(w);
        r.setMinWidth(w);
        return r;
    }

    private Label label(String text, double size, String color, boolean bold) {
        Label l = new Label(text);
        l.setFont(bold ? Font.font("System", FontWeight.BOLD, size) : Font.font("System", size));
        try {
            l.setTextFill(Color.web(color));
        } catch (Exception ignored) {
            l.setTextFill(Color.web(GRAY));
        }
        return l;
    }
}
