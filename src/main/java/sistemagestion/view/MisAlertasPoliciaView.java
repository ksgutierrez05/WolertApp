package sistemagestion.view;

import javafx.animation.*;
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
import javafx.util.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import sistemagestion.model.*;
import sistemagestion.service.*;

public class MisAlertasPoliciaView {

    private static final String WHITE = "#ffffff";
    private static final String BG = "#f4f6fb";
    private static final String BLUE = "#1565c0";
    private static final String GREEN = "#43a047";
    private static final String RED = "#e53935";
    private static final String RED_LIGHT = "#fff0f0";
    private static final String ORANGE = "#fb8c00";
    private static final String YELLOW_BG = "#fffde7";
    private static final String PURPLE = "#7b1fa2";
    private static final String TEAL = "#00796b";
    private static final String GRAY_TEXT = "#6b7280";
    private static final String BORDER = "#e5e7eb";
    private static final String C_DARK_GRAD = "linear-gradient(to right, #16283d, #1f3a56)";

    // ── Opciones de combos ─────────────────────────────────────────
    private static final List<String> TIPOS_ARMA = List.of(
            "— Sin arma —", "Pistola", "Revólver", "Escopeta", "Rifle",
            "Cuchillo", "Machete", "Arma blanca", "Arma de fuego artesanal",
            "Granada", "Explosivo", "Otro");

    private static final List<String> MEDIOS_TRANSPORTE = List.of(
            "— Sin transporte —", "A pie", "Motocicleta", "Automóvil",
            "Camioneta", "Bicicleta", "Mototaxi", "Bus", "Camión", "Otro");

    private final Usuario usuarioActual;
    private final Policia policiaActual;
    private final AlertaService alertaService;
    private final AtencionAlertaService atencionService;
    private final BorderPane root;

    public MisAlertasPoliciaView(Usuario usuarioActual, Policia policiaActual,
            AlertaService alertaService, AtencionAlertaService atencionService,
            BorderPane root) {
        javafx.scene.text.Font.loadFont(
                getClass().getResourceAsStream("/fa-solid-900.ttf"), 20);
        this.usuarioActual = usuarioActual;
        this.policiaActual = policiaActual;
        this.alertaService = alertaService;
        this.atencionService = atencionService;
        this.root = root;
    }

    // ═══════════════════════════════════════════════════════════════
    // PUNTO DE ENTRADA
    // ═══════════════════════════════════════════════════════════════
    public ScrollPane build() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color: " + BG + ";");

        List<Alerta> todasAlertas = cargarAlertas();

        // Contenedor dinámico de la lista (se repinta al filtrar)
        VBox listaContainer = new VBox(16);
        renderAlertas(listaContainer, todasAlertas, null, "");

        content.getChildren().addAll(
                buildTopBar(),
                buildStatsRow(),
                buildFiltros(todasAlertas, listaContainer),
                listaContainer
        );
        Timeline autoRefresh = new Timeline(
                new KeyFrame(Duration.seconds(30), e -> {
                    try {
                        List<Alerta> nuevasAlertas = cargarAlertas();

                        listaContainer.getChildren().clear();

                        renderAlertas(
                                listaContainer,
                                nuevasAlertas,
                                null,
                                ""
                        );

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                })
        );

        autoRefresh.setCycleCount(Timeline.INDEFINITE);
        autoRefresh.play();

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: " + BG + "; -fx-background: " + BG + ";"
                + "-fx-border-color: transparent; -fx-border-width: 0;");

        return scroll;
    }

    // ═══════════════════════════════════════════════════════════════
    // TOP BAR
    // ═══════════════════════════════════════════════════════════════
    private HBox buildTopBar() {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);

        VBox titles = new VBox(4);
        Label title = new Label("Mis Alertas Asignadas");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#111827"));
        Label sub = new Label("Gestiona y responde a las alertas de tu zona");
        sub.setFont(Font.font("System", 13));
        sub.setTextFill(Color.web(GRAY_TEXT));
        titles.getChildren().addAll(title, sub);
        bar.getChildren().add(titles);
        return bar;
    }

    // ═══════════════════════════════════════════════════════════════
    // STATS ROW
    // ═══════════════════════════════════════════════════════════════
    private HBox buildStatsRow() {
        List<Alerta> lista = cargarAlertas();

        long pendientes = lista.stream().filter(a -> a.getEstado() == EstadoAlerta.PENDIENTE).count();
        long enAtencion = lista.stream().filter(a
                -> a.getEstado() == EstadoAlerta.EN_ATENCION
                || a.getEstado() == EstadoAlerta.UNIDAD_ASIGNADA).count();
        long resueltas = lista.stream().filter(a -> a.getEstado() == EstadoAlerta.RESUELTA).count();
        long total = lista.size();

        HBox row = new HBox(16);
        HBox.setHgrow(row, Priority.ALWAYS);
        row.getChildren().addAll(
                statCard(RED_LIGHT, RED, "\uf0f3", "Pendientes", boldNum(String.valueOf(pendientes), RED), "Sin atender"),
                statCard("#fff8e1", ORANGE, "\uf017", "En atención", boldNum(String.valueOf(enAtencion), ORANGE), "En proceso"),
                statCard("#e8f5e9", GREEN, "\uf058", "Resueltas", boldNum(String.valueOf(resueltas), GREEN), "Completadas"),
                statCard("#e3f2fd", BLUE, "\uf0c9", "Total", boldNum(String.valueOf(total), BLUE), "Alertas asignadas")
        );
        return row;
    }

    private Label boldNum(String val, String color) {
        Label l = new Label(val);
        l.setFont(Font.font("System", FontWeight.BOLD, 36));
        l.setTextFill(Color.web(color));
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
        iconLbl.setStyle("-fx-font-family: 'Font Awesome 6 Free Solid';"
                + "-fx-font-size: 22px; -fx-text-fill: " + accentColor + ";");
        iconWrap.getChildren().addAll(iconBg, iconLbl);

        Label titleLbl = new Label(title);
        titleLbl.setFont(Font.font("System", FontWeight.BOLD, 13));
        titleLbl.setTextFill(Color.web("#374151"));
        Label subLbl = new Label(sub);
        subLbl.setFont(Font.font("System", 11));
        subLbl.setTextFill(Color.web(GRAY_TEXT));

        HBox top = new HBox(16);
        top.setAlignment(Pos.CENTER_LEFT);
        top.getChildren().addAll(iconWrap, new VBox(3, titleLbl, valueLabel, subLbl));
        card.getChildren().add(top);

        card.setOnMouseEntered(e -> card.setTranslateY(-3));
        card.setOnMouseExited(e -> card.setTranslateY(0));
        return card;
    }

    // ═══════════════════════════════════════════════════════════════
    // BARRA DE FILTROS
    // ═══════════════════════════════════════════════════════════════
    private VBox buildFiltros(List<Alerta> todas, VBox listaContainer) {
        VBox panel = new VBox(12);
        panel.setPadding(new Insets(16, 20, 16, 20));
        panel.setStyle("-fx-background-color: white; -fx-background-radius: 16;");
        shadow(panel);

        // ── Título de la sección ──────────────────────────────────
        HBox titulo = new HBox(8);
        titulo.setAlignment(Pos.CENTER_LEFT);
        Label filtroFA = new Label("\uf0b0");
        filtroFA.setStyle("-fx-font-family: 'Font Awesome 6 Free Solid'; -fx-font-size: 12px; -fx-text-fill: " + C_DARK_GRAD + ";");
        Label filtroTxt = new Label("Filtrar alertas");
        filtroTxt.setFont(Font.font("System", FontWeight.BOLD, 13));
        filtroTxt.setTextFill(Color.web(BLUE));
        titulo.getChildren().addAll(filtroFA, filtroTxt);

        // ── Fila de controles ─────────────────────────────────────
        HBox controles = new HBox(12);
        controles.setAlignment(Pos.CENTER_LEFT);

        // Búsqueda de texto
        TextField busqueda = new TextField();
        busqueda.setPromptText("\uf002  Buscar por descripción, barrio, tipo...");
        busqueda.setPrefWidth(260);
        busqueda.setPrefHeight(38);
        String bBase = "-fx-background-color: #f8fafc; -fx-background-radius: 10;"
                + "-fx-border-color: " + BORDER + "; -fx-border-radius: 10; -fx-border-width: 1.5;"
                + "-fx-font-size: 13px; -fx-text-fill: #1E293B; -fx-prompt-text-fill: #94A3B8;"
                + "-fx-padding: 0 14; -fx-focus-color: transparent; -fx-faint-focus-color: transparent;";
        String bFocus = bBase.replace(BORDER, C_DARK_GRAD);
        busqueda.setStyle(bBase);
        busqueda.focusedProperty().addListener((o, ov, focused)
                -> busqueda.setStyle(focused ? bFocus : bBase));

        // Botones de estado (tipo pill toggle)
        record FiltroBtn(String label, String estado, String icono, String color, String bgActive) {

        }
        List<FiltroBtn> filtros = List.of(
                new FiltroBtn("Todos", null, "\uf0c9", C_DARK_GRAD, WHITE),
                new FiltroBtn("Pendientes", "PENDIENTE", "\uf017", RED, RED_LIGHT),
                new FiltroBtn("Recibidas", "RECIBIDA", "\uf058", GREEN, "#e8f5e9"),
                new FiltroBtn("En atención", "EN_ATENCION", "\uf0e7", ORANGE, YELLOW_BG),
                new FiltroBtn("Unidad asig.", "UNIDAD_ASIGNADA", "\uf505", BLUE, "#e3f2fd"),
                new FiltroBtn("Resueltas", "RESUELTA", "\uf058", GREEN, "#e8f5e9"),
                new FiltroBtn("Canceladas", "CANCELADA", "\uf057", GRAY_TEXT, "#f3f4f6")
        );

        HBox pillRow = new HBox(8);
        pillRow.setAlignment(Pos.CENTER_LEFT);
        // Estado activo guardado en un array para mutabilidad desde lambdas
        final String[] estadoActivo = {null};
        final Button[] activoBtn = {null};

        for (FiltroBtn fb : filtros) {
            Button btn = new Button();
            HBox btnContent = new HBox(5);
            btnContent.setAlignment(Pos.CENTER);
            Label bFA = new Label(fb.icono());
            bFA.setStyle("-fx-font-family: 'Font Awesome 6 Free Solid'; -fx-font-size: 10px;");
            Label bTxt = new Label(fb.label());
            bTxt.setFont(Font.font("System", FontWeight.BOLD, 11));
            btnContent.getChildren().addAll(bFA, bTxt);
            btn.setGraphic(btnContent);
            btn.setPrefHeight(34);
            btn.setCursor(javafx.scene.Cursor.HAND);

            Runnable applyInactive = () -> {
                btn.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 20;"
                        + "-fx-border-color: " + BORDER + "; -fx-border-radius: 20; -fx-border-width: 1;");
                bFA.setStyle("-fx-font-family: 'Font Awesome 6 Free Solid'; -fx-font-size: 10px; -fx-text-fill: " + GRAY_TEXT + ";");
                bTxt.setStyle("-fx-text-fill: " + GRAY_TEXT + ";");
            };
            Runnable applyActive = () -> {
                btn.setStyle("-fx-background-color: " + fb.bgActive() + "; -fx-background-radius: 20;"
                        + "-fx-border-color: " + fb.color() + "; -fx-border-radius: 20; -fx-border-width: 1.5;");
                bFA.setStyle("-fx-font-family: 'Font Awesome 6 Free Solid'; -fx-font-size: 10px; -fx-text-fill: " + fb.color() + ";");
                bTxt.setStyle("-fx-text-fill: " + fb.color() + "; -fx-font-weight: bold;");
            };

            // "Todos" arranca activo
            if (fb.estado() == null) {
                applyActive.run();
                activoBtn[0] = btn;
            } else {
                applyInactive.run();
            }

            btn.setOnMouseEntered(e -> {
                if (activoBtn[0] != btn) {
                    btn.setStyle(
                            "-fx-background-color: " + fb.bgActive() + "; -fx-background-radius: 20;"
                            + "-fx-border-color: " + fb.color() + "88; -fx-border-radius: 20; -fx-border-width: 1;");
                }
            });
            btn.setOnMouseExited(e -> {
                if (activoBtn[0] != btn) {
                    applyInactive.run();
                }
            });

            btn.setOnAction(e -> {
                if (activoBtn[0] != null) {
                    // desactivar el anterior
                    final Button prev = activoBtn[0];
                    // buscar su runnable — re-aplicar inactivo directamente
                    prev.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 20;"
                            + "-fx-border-color: " + BORDER + "; -fx-border-radius: 20; -fx-border-width: 1;");
                    // reset texto/icono del anterior via lookup
                    if (prev.getGraphic() instanceof HBox ph) {
                        ph.getChildren().forEach(n -> {
                            if (n instanceof Label ll) {
                                if (ll.getStyle().contains("Font Awesome")) {
                                    ll.setStyle("-fx-font-family: 'Font Awesome 6 Free Solid'; -fx-font-size: 10px; -fx-text-fill: " + GRAY_TEXT + ";");
                                } else {
                                    ll.setStyle("-fx-text-fill: " + GRAY_TEXT + ";");
                                }
                            }
                        });
                    }
                }
                activoBtn[0] = btn;
                estadoActivo[0] = fb.estado();
                applyActive.run();
                renderAlertas(listaContainer, todas, estadoActivo[0], busqueda.getText().trim());
            });

            pillRow.getChildren().add(btn);
        }

        // Listener en el campo de búsqueda
        busqueda.textProperty().addListener((o, ov, nv)
                -> renderAlertas(listaContainer, todas, estadoActivo[0], nv.trim()));

        // Botón limpiar
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        controles.getChildren().addAll(busqueda, pillRow, spacer);

        panel.getChildren().addAll(titulo, controles);
        return panel;
    }

    // ═══════════════════════════════════════════════════════════════
    // RENDER DINÁMICO DE LA LISTA (aplica filtros)
    // ═══════════════════════════════════════════════════════════════
    private void renderAlertas(VBox container, List<Alerta> todas,
            String estadoFiltro, String textoBusqueda) {
        container.getChildren().clear();

        List<Alerta> filtradas = todas.stream().filter(a -> {
            // filtro por estado
            if (estadoFiltro != null) {
                if (a.getEstado() == null) {
                    return false;
                }
                if (!a.getEstado().name().equals(estadoFiltro)) {
                    return false;
                }
            }
            // filtro por texto
            if (!textoBusqueda.isEmpty()) {
                String q = textoBusqueda.toLowerCase();
                boolean match = false;
                if (a.getDescripcion() != null && a.getDescripcion().toLowerCase().contains(q)) {
                    match = true;
                }
                if (!match && a.getBarrio() != null && a.getBarrio().getNombre() != null
                        && a.getBarrio().getNombre().toLowerCase().contains(q)) {
                    match = true;
                }
                if (!match && a.getTipoalerta() != null && a.getTipoalerta().getNombre() != null
                        && a.getTipoalerta().getNombre().toLowerCase().contains(q)) {
                    match = true;
                }
                if (!match && a.getTipoarma() != null && a.getTipoarma().getNombre() != null
                        && a.getTipoarma().getNombre().toLowerCase().contains(q)) {
                    match = true;
                }
                if (!match && a.getMediotransporte() != null && a.getMediotransporte().getNombre() != null
                        && a.getMediotransporte().getNombre().toLowerCase().contains(q)) {
                    match = true;
                }
                if (!match) {
                    return false;
                }
            }
            return true;
        }).collect(java.util.stream.Collectors.toList());

        if (filtradas.isEmpty()) {
            VBox vacio = new VBox(12);
            vacio.setAlignment(Pos.CENTER);
            vacio.setPadding(new Insets(50));
            vacio.setStyle("-fx-background-color: white; -fx-background-radius: 18;");
            shadow(vacio);
            Label ico = new Label("\uf0b0");
            ico.setStyle("-fx-font-family: 'Font Awesome 6 Free Solid'; -fx-font-size: 40px; -fx-text-fill: " + GRAY_TEXT + ";");
            Label msg1 = new Label("Sin resultados para este filtro");
            msg1.setFont(Font.font("System", FontWeight.BOLD, 15));
            msg1.setTextFill(Color.web(GRAY_TEXT));
            Label msg2 = new Label("Prueba con otro estado o limpia la búsqueda");
            msg2.setFont(Font.font("System", 12));
            msg2.setTextFill(Color.web(GRAY_TEXT));
            vacio.getChildren().addAll(ico, msg1, msg2);
            container.getChildren().add(vacio);
        } else {
            for (Alerta a : filtradas) {
                container.getChildren().add(buildAlertaCard(a));
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // TARJETA DE ALERTA
    // ═══════════════════════════════════════════════════════════════
    private VBox buildAlertaCard(Alerta a) {
        String[] badge = badgeAlerta(a.getTipoalerta() != null
                ? a.getTipoalerta().getNombre() : null);

        boolean resuelta = a.getEstado() == EstadoAlerta.RESUELTA;
        boolean enAtencion = a.getEstado() == EstadoAlerta.EN_ATENCION
                || a.getEstado() == EstadoAlerta.UNIDAD_ASIGNADA;

        String accentColor, bgLight, iconAvatar;
        if (resuelta) {
            accentColor = GREEN;
            bgLight = "#e8f5e9";
            iconAvatar = "\uf058";
        } else if (enAtencion) {
            accentColor = ORANGE;
            bgLight = YELLOW_BG;
            iconAvatar = "\uf0e7";
        } else {
            accentColor = badge[3];
            bgLight = badge[2];
            iconAvatar = badge[0];
        }

        String tipo = a.getTipoalerta() != null ? a.getTipoalerta().getNombre() : "Alerta";
        String barrio = a.getBarrio() != null ? a.getBarrio().getNombre() : "—";
        String estado = a.getEstado() != null ? a.getEstado().name().replace("_", " ") : "—";

        VBox card = new VBox(0);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 18;");
        shadow(card);

        // ── Cabecera ───────────────────────────────────────────────
        HBox cardHeader = new HBox(14);
        cardHeader.setAlignment(Pos.CENTER_LEFT);
        cardHeader.setPadding(new Insets(18, 20, 14, 20));
        cardHeader.setStyle(
                "-fx-background-color: " + bgLight + ";"
                + "-fx-background-radius: 18 18 0 0;"
                + "-fx-border-color: transparent transparent " + BORDER + " transparent;"
                + "-fx-border-width: 0 0 1 0;");

        StackPane avatarBox = new StackPane();
        Circle avatar = new Circle(24, Color.web(accentColor));
        Label avatarFA = new Label(iconAvatar);
        avatarFA.setStyle("-fx-font-family: 'Font Awesome 6 Free Solid';"
                + "-fx-font-size: 18px; -fx-text-fill: white;");
        avatarBox.getChildren().addAll(avatar, avatarFA);

        HBox tipoRow = new HBox(8);
        tipoRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(tipoRow, Priority.ALWAYS);
        Label tipoFA = new Label(iconAvatar);
        tipoFA.setStyle("-fx-font-family: 'Font Awesome 6 Free Solid';"
                + "-fx-font-size: 13px; -fx-text-fill: " + accentColor + ";");
        Label tipoLbl = new Label(tipo + " en " + barrio);
        tipoLbl.setFont(Font.font("System", FontWeight.BOLD, 15));
        tipoLbl.setTextFill(Color.web("#111827"));
        tipoRow.getChildren().addAll(tipoFA, tipoLbl);

        HBox fechaRow = new HBox(8);
        fechaRow.setAlignment(Pos.CENTER_LEFT);
        Label calFA = new Label("\uf073");
        calFA.setStyle("-fx-font-family: 'Font Awesome 6 Free Solid'; -fx-font-size: 11px; -fx-text-fill: " + GRAY_TEXT + ";");
        Label fechaTxt = new Label(formatFecha(a.getFechaHora()));
        fechaTxt.setFont(Font.font("System", 11));
        fechaTxt.setTextFill(Color.web(GRAY_TEXT));
        Label pinFA = new Label("\uf3c5");
        pinFA.setStyle("-fx-font-family: 'Font Awesome 6 Free Solid'; -fx-font-size: 11px; -fx-text-fill: " + GRAY_TEXT + ";");
        Label dirTxt = new Label(formatDireccion(a.getDireccion()));
        dirTxt.setFont(Font.font("System", 11));
        dirTxt.setTextFill(Color.web(GRAY_TEXT));
        fechaRow.getChildren().addAll(calFA, fechaTxt, pinFA, dirTxt);

        VBox headerInfo = new VBox(4, tipoRow, fechaRow);
        HBox.setHgrow(headerInfo, Priority.ALWAYS);

        String[] eb = badgeEstado(a.getEstado());
        HBox estadoBox = new HBox(5);
        estadoBox.setAlignment(Pos.CENTER);
        estadoBox.setPadding(new Insets(4, 12, 4, 12));
        estadoBox.setStyle("-fx-background-color: " + eb[1] + "; -fx-background-radius: 20;");
        Label estadoFA = new Label(eb[0]);
        estadoFA.setStyle("-fx-font-family: 'Font Awesome 6 Free Solid'; -fx-font-size: 11px; -fx-text-fill: " + eb[2] + ";");
        Label estadoTxt = new Label(estado);
        estadoTxt.setFont(Font.font("System", FontWeight.BOLD, 11));
        estadoTxt.setTextFill(Color.web(eb[2]));
        estadoBox.getChildren().addAll(estadoFA, estadoTxt);

        cardHeader.getChildren().addAll(avatarBox, headerInfo, estadoBox);

        // ── Cuerpo ─────────────────────────────────────────────────
        VBox body = new VBox(12);
        body.setPadding(new Insets(16, 20, 8, 20));

        Label descTitleLbl = new Label("Descripción");
        descTitleLbl.setFont(Font.font("System", FontWeight.BOLD, 11));
        descTitleLbl.setTextFill(Color.web(GRAY_TEXT));
        Label descLbl = new Label(a.getDescripcion() != null ? a.getDescripcion() : "Sin descripción");
        descLbl.setFont(Font.font("System", 13));
        descLbl.setTextFill(Color.web("#334155"));
        descLbl.setWrapText(true);

        HBox chips = new HBox(10);
        chips.setAlignment(Pos.CENTER_LEFT);
        if (a.getTipoarma() != null) {
            chips.getChildren().add(chipImagen("/PistolaPin.png",
                    a.getTipoarma().getNombre(), "#fef2f2", RED));
        }
        if (a.getMediotransporte() != null) {
            chips.getChildren().add(chipFA("\uf1b9", a.getMediotransporte().getNombre(), "#e3f2fd", BLUE));
        }
        if (a.getBarrio() != null) {
            chips.getChildren().add(chipFA("\uf3c5", a.getBarrio().getNombre(), "#e8f5e9", GREEN));
        }

        body.getChildren().add(new VBox(4, descTitleLbl, descLbl));
        if (!chips.getChildren().isEmpty()) {
            body.getChildren().add(chips);
        }

        // ── Acordeón de atención ───────────────────────────────────
        card.getChildren().addAll(cardHeader, body, buildAtencionAcordeon(a));
        return card;
    }

    // ═══════════════════════════════════════════════════════════════
    // ACORDEÓN "REGISTRAR ATENCIÓN"
    // ═══════════════════════════════════════════════════════════════
    private VBox buildAtencionAcordeon(Alerta a) {
        // ── Contenedor raíz del acordeón ──────────────────────────
        VBox acordeon = new VBox(0);
        acordeon.setStyle(
                "-fx-border-color: " + BORDER + " transparent transparent transparent;"
                + "-fx-border-width: 1 0 0 0;");

        // ── Cabecera del acordeón (siempre visible) ────────────────
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(12, 20, 12, 20));
        header.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 0 0 18 18; -fx-cursor: hand;");

        // Ícono acordeón
        Label arrowFA = new Label("\uf078");   // chevron-down
        arrowFA.setStyle("-fx-font-family: 'Font Awesome 6 Free Solid'; -fx-font-size: 11px; -fx-text-fill: " + BLUE + ";");

        Label editFA = new Label("\uf303");
        editFA.setStyle("-fx-font-family: 'Font Awesome 6 Free Solid'; -fx-font-size: 13px; -fx-text-fill: " + BLUE + ";");
        Label headerTxt = new Label("Registrar atención");
        headerTxt.setFont(Font.font("System", FontWeight.BOLD, 13));
        headerTxt.setTextFill(Color.web(BLUE));

        // Mini-chips resumen del ciudadano (arma / transporte)
        HBox resumenChips = new HBox(8);
        resumenChips.setAlignment(Pos.CENTER_LEFT);
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        String armaResumen = a.getTipoarma() != null ? a.getTipoarma().getNombre() : null;
        String transResumen = a.getMediotransporte() != null ? a.getMediotransporte().getNombre() : null;

        if (armaResumen != null) {
            resumenChips.getChildren().add(chipImagen("/PistolaPin.png",
                    "Arma: " + armaResumen, "#fef2f2", RED));
        }
        if (transResumen != null) {
            resumenChips.getChildren().add(miniChip("\uf1b9", "Transporte: " + transResumen, "#e3f2fd", BLUE));
        }
        if (armaResumen == null && transResumen == null) {
            Label noInfo = new Label("Sin datos previos del ciudadano");
            noInfo.setFont(Font.font("System", 11));
            noInfo.setTextFill(Color.web(GRAY_TEXT));
            resumenChips.getChildren().add(noInfo);
        }

        header.getChildren().addAll(editFA, headerTxt, spacer, resumenChips, arrowFA);

        // ── Cuerpo del formulario (colapsable) ─────────────────────
        VBox formBody = buildAtencionFormBody(a);
        formBody.setVisible(false);
        formBody.setManaged(false);

        // ── Toggle lógica ──────────────────────────────────────────
        final boolean[] abierto = {false};
        header.setOnMouseClicked(e -> {
            abierto[0] = !abierto[0];
            formBody.setVisible(abierto[0]);
            formBody.setManaged(abierto[0]);
            // Rotar flecha
            arrowFA.setText(abierto[0] ? "\uf077" : "\uf078");  // up / down
            // Redondear esquinas cuando está cerrado
            header.setStyle(abierto[0]
                    ? "-fx-background-color: #f0f7ff; -fx-background-radius: 0; -fx-cursor: hand;"
                    : "-fx-background-color: #f8fafc; -fx-background-radius: 0 0 18 18; -fx-cursor: hand;");
        });
        header.setOnMouseEntered(e -> header.setStyle(
                "-fx-background-color: #e8f0fe; -fx-background-radius: "
                + (abierto[0] ? "0" : "0 0 18 18") + "; -fx-cursor: hand;"));
        header.setOnMouseExited(e -> header.setStyle(
                "-fx-background-color: " + (abierto[0] ? "#f0f7ff" : "#f8fafc")
                + "; -fx-background-radius: " + (abierto[0] ? "0" : "0 0 18 18")
                + "; -fx-cursor: hand;"));

        acordeon.getChildren().addAll(header, formBody);
        return acordeon;
    }

    // ═══════════════════════════════════════════════════════════════
    // FORMULARIO DE ATENCIÓN (dentro del acordeón)
    // ═══════════════════════════════════════════════════════════════
    private VBox buildAtencionFormBody(Alerta a) {
        VBox box = new VBox(16);
        box.setPadding(new Insets(18, 20, 24, 20));
        box.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 0 0 18 18;");

        // ── Banner informativo: datos del ciudadano ────────────────
        String armaOriginal = a.getTipoarma() != null ? a.getTipoarma().getNombre() : null;
        String transOriginal = a.getMediotransporte() != null ? a.getMediotransporte().getNombre() : null;

        if (armaOriginal != null || transOriginal != null) {
            HBox banner = new HBox(10);
            banner.setAlignment(Pos.CENTER_LEFT);
            banner.setPadding(new Insets(10, 14, 10, 14));
            banner.setStyle("-fx-background-color: #fffbeb; -fx-background-radius: 10;"
                    + "-fx-border-color: #fcd34d; -fx-border-radius: 10; -fx-border-width: 1;");
            Label infoFA = new Label("\uf05a");
            infoFA.setStyle("-fx-font-family: 'Font Awesome 6 Free Solid'; -fx-font-size: 13px; -fx-text-fill: #b45309;");
            Label infoTxt = new Label("El ciudadano reportó: "
                    + (armaOriginal != null ? "arma (" + armaOriginal + ") " : "")
                    + (transOriginal != null ? "· transporte (" + transOriginal + ")" : ""));
            infoTxt.setFont(Font.font("System", FontWeight.BOLD, 11));
            infoTxt.setTextFill(Color.web("#92400e"));
            infoTxt.setWrapText(true);
            banner.getChildren().addAll(infoFA, infoTxt);
            box.getChildren().add(banner);
        }

        // ── Fila: Estado alerta + Estado atención ──────────────────
        HBox combosRow = new HBox(16);
        combosRow.setAlignment(Pos.CENTER_LEFT);

        VBox estadoAlertaBox = new VBox(6);
        estadoAlertaBox.getChildren().addAll(
                labelCampo("\uf0f3", "Estado de la alerta"),
                styledCombo(
                        List.of("PENDIENTE", "RECIBIDA", "EN_ATENCION", "UNIDAD_ASIGNADA", "RESUELTA", "CANCELADA"),
                        a.getEstado() != null ? a.getEstado().name() : "PENDIENTE"));

        VBox estadoAtencionBox = new VBox(6);
        ComboBox<String> estadoAtencionCombo = styledCombo(
                List.of("PENDIENTE", "EN_PROCESO", "FINALIZADA", "CANCELADA"), "EN_PROCESO");
        estadoAtencionBox.getChildren().addAll(
                labelCampo("\uf46d", "Estado de la atención"),
                estadoAtencionCombo);

        combosRow.getChildren().addAll(estadoAlertaBox, estadoAtencionBox);

        // recuperar referencia al combo de estado alerta para el guardado
        ComboBox<String> estadoAlertaCombo = (ComboBox<String>) estadoAlertaBox.getChildren().get(1);

        // ── Descripción de la situación ────────────────────────────
        VBox situacionBox = new VBox(6);
        TextArea situacion = new TextArea();
        situacion.setPromptText("Describe la situación, acciones tomadas y resultado...");
        situacion.setPrefRowCount(3);
        situacion.setWrapText(true);
        applyTextAreaStyle(situacion);
        situacionBox.getChildren().addAll(labelCampo("\uf15c", "Descripción de la situación"), situacion);

        // ── Fila: Tipo de arma + Medio de transporte (COMBOS) ─────
        HBox extrasRow = new HBox(16);
        extrasRow.setAlignment(Pos.CENTER_LEFT);

        // Tipo de arma — combo con valor precargado del ciudadano
        VBox armaBox = new VBox(6);
        HBox.setHgrow(armaBox, Priority.ALWAYS);
        ComboBox<String> armaCombo = styledComboFull(TIPOS_ARMA,
                resolverValorCombo(armaOriginal, TIPOS_ARMA));
        armaBox.getChildren().addAll(labelCampo("\uf6de", "Tipo de arma"), armaCombo);

        // Medio de transporte — combo con valor precargado del ciudadano
        VBox transBox = new VBox(6);
        HBox.setHgrow(transBox, Priority.ALWAYS);
        ComboBox<String> transCombo = styledComboFull(MEDIOS_TRANSPORTE,
                resolverValorCombo(transOriginal, MEDIOS_TRANSPORTE));
        transBox.getChildren().addAll(labelCampo("\uf1b9", "Medio de transporte"), transCombo);

        extrasRow.getChildren().addAll(armaBox, transBox);

        // ── Observación ────────────────────────────────────────────
        VBox obsBox = new VBox(6);
        TextArea obsArea = new TextArea();
        obsArea.setPromptText("Notas adicionales, testigos, coordenadas...");
        obsArea.setPrefRowCount(2);
        obsArea.setWrapText(true);
        applyTextAreaStyle(obsArea);
        obsBox.getChildren().addAll(labelCampo("\uf249", "Observación adicional"), obsArea);

        // ── Feedback + botón ───────────────────────────────────────
        Label feedback = new Label("");
        feedback.setFont(Font.font("System", 12));
        feedback.setWrapText(true);

        HBox guardarContent = new HBox(8);
        guardarContent.setAlignment(Pos.CENTER);
        Label guardFA = new Label("\uf0c7");
        guardFA.setStyle("-fx-font-family: 'Font Awesome 6 Free Solid'; -fx-font-size: 13px; -fx-text-fill: white;");
        Label guardTxt = new Label("Guardar atención");
        guardTxt.setFont(Font.font("System", FontWeight.BOLD, 13));
        guardTxt.setStyle("-fx-text-fill: white;");
        guardarContent.getChildren().addAll(guardFA, guardTxt);

        Button guardar = new Button();
        guardar.setGraphic(guardarContent);
        guardar.setPrefHeight(42);
        guardar.setStyle(btnPrimaryStyle());
        guardar.setOnMouseEntered(e -> guardar.setStyle(btnPrimaryHoverStyle()));
        guardar.setOnMouseExited(e -> guardar.setStyle(btnPrimaryStyle()));

        guardar.setOnAction(ev -> {
            String desc = situacion.getText().trim();
            String nuevoEst = estadoAlertaCombo.getValue();
            String estAtencion = estadoAtencionCombo.getValue();

            if (desc.isEmpty()) {
                feedback.setTextFill(Color.web(RED));
                feedback.setText("Debes describir la situación antes de guardar.");
                return;
            }
            try {
                if (nuevoEst != null && alertaService != null) {
                    alertaService.actualizarEstado(a.getId_alerta(), nuevoEst);
                }
            } catch (Exception ex) {
                feedback.setTextFill(Color.web(RED));
                feedback.setText("Error al actualizar estado: " + ex.getMessage());
                return;
            }
            try {
                if (atencionService != null) {
                    AtencionAlerta at = new AtencionAlerta();
                    at.setAlerta(a);
                    at.setDescripcion(desc);
                    at.setEstado(EstadoAtencionAlerta.valueOf(estAtencion));
                    String obs = obsArea.getText().trim();
                    at.setObservacion(obs.isEmpty()
                            ? "Registrado por: " + (usuarioActual != null ? usuarioActual.getUsername() : "—")
                            : obs);
                    UnidadPolicial u = (policiaActual != null && policiaActual.getUnidadpolicial() != null)
                            ? policiaActual.getUnidadpolicial() : new UnidadPolicial();
                    if (u.getNombre() == null) {
                        u.setNombre("Sin unidad");
                    }
                    at.setUnidad(u);

                    // Tipo de arma desde combo
                    String armaSeleccionada = armaCombo.getValue();
                    if (armaSeleccionada != null && !armaSeleccionada.startsWith("—")) {
                        TipoArma ta = new TipoArma();
                        ta.setNombre(armaSeleccionada);
                        at.setTipoarma(ta);
                    } else {
                        at.setTipoarma(a.getTipoarma());
                    }

                    // Medio de transporte desde combo
                    String transSeleccionado = transCombo.getValue();
                    if (transSeleccionado != null && !transSeleccionado.startsWith("—")) {
                        MedioTransporte mt = new MedioTransporte();
                        mt.setNombre(transSeleccionado);
                        at.setMediotransporte(mt);
                    } else {
                        at.setMediotransporte(a.getMediotransporte());
                    }

                    boolean ok = atencionService.insertar(at);
                    if (ok) {
                        feedback.setTextFill(Color.web(GREEN));
                        feedback.setText("Atención registrada correctamente.");
                        situacion.clear();
                        Timeline autoRefresh = new Timeline(
                                new KeyFrame(Duration.seconds(30), e -> {
                                    root.setCenter(
                                            new MisAlertasPoliciaView(
                                                    usuarioActual,
                                                    policiaActual,
                                                    alertaService,
                                                    atencionService,
                                                    root
                                            ).build()
                                    );
                                })
                        );

                        autoRefresh.setCycleCount(Timeline.INDEFINITE);
                        autoRefresh.play();
                    } else {
                        feedback.setTextFill(Color.web(RED));
                        feedback.setText("No se pudo registrar la atención.");
                    }
                }
            } catch (Exception ex) {
                feedback.setTextFill(Color.web(RED));
                feedback.setText("Error: " + ex.getMessage());
            }
        });

        HBox btnRow = new HBox(12, guardar, feedback);
        btnRow.setAlignment(Pos.CENTER_LEFT);

        box.getChildren().addAll(combosRow, situacionBox, extrasRow, obsBox, btnRow);
        return box;
    }

    // ═══════════════════════════════════════════════════════════════
    // HELPER: resolver valor inicial del combo buscando coincidencia
    // ═══════════════════════════════════════════════════════════════
    /**
     * Busca en {@code opciones} la primera que contenga (case-insensitive) el
     * texto del ciudadano. Si no hay coincidencia devuelve el primer elemento
     * (normalmente "— Sin X —").
     */
    private String resolverValorCombo(String valorCiudadano, List<String> opciones) {
        if (valorCiudadano == null || valorCiudadano.isBlank()) {
            return opciones.get(0);
        }
        String v = valorCiudadano.trim().toLowerCase();
        return opciones.stream()
                .filter(o -> o.toLowerCase().contains(v) || v.contains(o.toLowerCase()))
                .findFirst()
                .orElse(opciones.get(0));
    }

    // ═══════════════════════════════════════════════════════════════
    // BADGES
    // ═══════════════════════════════════════════════════════════════
    private String[] badgeAlerta(String nombre) {
        if (nombre == null) {
            return new String[]{"\uf0f3", "General", "#f3f4f6", GRAY_TEXT};
        }
        String n = nombre.toUpperCase();
        if (n.contains("ROB") || n.contains("ASALT")) {
            return new String[]{"\uf505", "Delito", RED_LIGHT, RED};
        }
        if (n.contains("HOMICID")) {
            return new String[]{"\uf071", "Homicidio", "#fff0f0", RED};
        }
        if (n.contains("SOSPECH")) {
            return new String[]{"\uf441", "Vigilancia", "#fef9c3", "#92400e"};
        }
        if (n.contains("ANIMAL")) {
            return new String[]{"\uf6d3", "Fauna", "#ecfdf5", "#065f46"};
        }
        if (n.contains("INCEND")) {
            return new String[]{"\uf06d", "Incendio", "#fff7ed", "#c2410c"};
        }
        if (n.contains("RUIDO") || n.contains("ALTER")) {
            return new String[]{"\uf028", "Alteración", "#fffbeb", "#b45309"};
        }
        if (n.contains("MÉDI") || n.contains("MEDIC")) {
            return new String[]{"\uf0fa", "Médica", "#f0fdf4", "#15803d"};
        }
        if (n.contains("ACCID")) {
            return new String[]{"\uf5e4", "Accidente", "#eff6ff", BLUE};
        }
        return new String[]{"\uf0f3", "General", "#f3f4f6", GRAY_TEXT};
    }

    private String[] badgeEstado(EstadoAlerta estado) {
        if (estado == null) {
            return new String[]{"\uf128", "#f3f4f6", GRAY_TEXT};
        }
        return switch (estado) {
            case PENDIENTE ->
                new String[]{"\uf017", RED_LIGHT, RED};
            case RECIBIDA ->
                new String[]{"\uf058", "#e8f5e9", GREEN};
            case EN_ATENCION ->
                new String[]{"\uf0e7", YELLOW_BG, ORANGE};
            case UNIDAD_ASIGNADA ->
                new String[]{"\uf505", "#e3f2fd", BLUE};
            case RESUELTA ->
                new String[]{"\uf058", "#e8f5e9", GREEN};
            case CANCELADA ->
                new String[]{"\uf057", "#f3f4f6", GRAY_TEXT};
            default ->
                new String[]{"\uf128", "#f3f4f6", GRAY_TEXT};
        };
    }

    // ═══════════════════════════════════════════════════════════════
    // HELPERS UI
    // ═══════════════════════════════════════════════════════════════
    private void applyTextAreaStyle(TextArea ta) {
        String taBase = "-fx-background-color: white; -fx-background-insets: 0;"
                + "-fx-background-radius: 12; -fx-border-radius: 12;"
                + "-fx-border-color: #CBD5E1; -fx-border-width: 1.5;"
                + "-fx-font-size: 13px; -fx-text-fill: #1E293B;"
                + "-fx-prompt-text-fill: #94A3B8; -fx-padding: 12 14 12 14;"
                + "-fx-focus-color: transparent; -fx-faint-focus-color: transparent;";
        String taFocus = "-fx-background-color: white; -fx-background-insets: 0;"
                + "-fx-background-radius: 12; -fx-border-radius: 12;"
                + "-fx-border-color: #93c5fd; -fx-border-width: 1.5;"
                + "-fx-font-size: 13px; -fx-text-fill: #1E293B;"
                + "-fx-prompt-text-fill: #94A3B8; -fx-padding: 12 14 12 14;"
                + "-fx-focus-color: transparent; -fx-faint-focus-color: transparent;";
        ta.setStyle(taBase);
        ta.focusedProperty().addListener((obs, o, focused)
                -> ta.setStyle(focused ? taFocus : taBase));
        ta.skinProperty().addListener((obs, o, skin) -> {
            if (skin == null) {
                return;
            }
            javafx.scene.Node sp = ta.lookup(".scroll-pane");
            if (sp != null) {
                sp.setStyle("-fx-background-color: white; -fx-background-insets: 0;");
            }
            javafx.scene.Node vp = ta.lookup(".scroll-pane .viewport");
            if (vp != null) {
                vp.setStyle("-fx-background-color: white;");
            }
            javafx.scene.Node ct = ta.lookup(".content");
            if (ct != null) {
                ct.setStyle("-fx-background-color: white;");
            }
        });
    }

    private HBox chipFA(String iconFA, String texto, String bg, String fg) {
        HBox chip = new HBox(6);
        chip.setAlignment(Pos.CENTER_LEFT);
        chip.setPadding(new Insets(4, 10, 4, 10));
        chip.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 20;"
                + "-fx-border-color: " + fg + "44; -fx-border-radius: 20; -fx-border-width: 1;");
        Label ico = new Label(iconFA);
        ico.setStyle("-fx-font-family: 'Font Awesome 6 Free Solid'; -fx-font-size: 11px; -fx-text-fill: " + fg + ";");
        Label txt = new Label(texto);
        txt.setFont(Font.font("System", FontWeight.BOLD, 11));
        txt.setTextFill(Color.web(fg));
        chip.getChildren().addAll(ico, txt);
        return chip;
    }

    /**
     * Mini-chip más compacto para el header del acordeón
     */
    private HBox miniChip(String iconFA, String texto, String bg, String fg) {
        HBox chip = new HBox(5);
        chip.setAlignment(Pos.CENTER_LEFT);
        chip.setPadding(new Insets(3, 8, 3, 8));
        chip.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 14;"
                + "-fx-border-color: " + fg + "55; -fx-border-radius: 14; -fx-border-width: 1;");
        Label ico = new Label(iconFA);
        ico.setStyle("-fx-font-family: 'Font Awesome 6 Free Solid'; -fx-font-size: 10px; -fx-text-fill: " + fg + ";");
        Label txt = new Label(texto);
        txt.setFont(Font.font("System", FontWeight.BOLD, 10));
        txt.setTextFill(Color.web(fg));
        chip.getChildren().addAll(ico, txt);
        return chip;
    }

    private HBox chipImagen(String rutaImagen, String texto, String bg, String fg) {
        HBox chip = new HBox(6);
        chip.setAlignment(Pos.CENTER_LEFT);
        chip.setPadding(new Insets(4, 10, 4, 10));
        chip.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 20;"
                + "-fx-border-color: " + fg + "44; -fx-border-radius: 20; -fx-border-width: 1;");
        try {
            java.awt.image.BufferedImage raw = javax.imageio.ImageIO.read(
                    getClass().getResourceAsStream(rutaImagen));
            java.awt.image.BufferedImage recortada = recortarTransparencia(raw);
            javafx.scene.image.ImageView ico = new javafx.scene.image.ImageView(
                    javafx.embed.swing.SwingFXUtils.toFXImage(recortada, null));
            ico.setFitWidth(14);
            ico.setFitHeight(14);
            ico.setPreserveRatio(true);
            chip.getChildren().add(ico);
        } catch (Exception e) {
            Label fallback = new Label("⚠");
            fallback.setFont(Font.font("System", 12));
            chip.getChildren().add(fallback);
        }
        Label txt = new Label(texto);
        txt.setFont(Font.font("System", FontWeight.BOLD, 11));
        txt.setTextFill(Color.web(fg));
        chip.getChildren().add(txt);
        return chip;
    }

    private HBox labelCampo(String iconFA, String texto) {
        HBox row = new HBox(5);
        row.setAlignment(Pos.CENTER_LEFT);
        Label ico = new Label(iconFA);
        ico.setStyle("-fx-font-family: 'Font Awesome 6 Free Solid'; -fx-font-size: 11px; -fx-text-fill: #64748b;");
        Label txt = new Label(texto);
        txt.setFont(Font.font("System", FontWeight.BOLD, 11));
        txt.setTextFill(Color.web("#64748b"));
        row.getChildren().addAll(ico, txt);
        return row;
    }

    private void shadow(Region node) {
        node.setEffect(new DropShadow(12, 0, 2, Color.web("#0000001a")));
    }

    /**
     * Combo estándar con ancho fijo 220 (para combos de estado)
     */
    private ComboBox<String> styledCombo(List<String> opciones, String valorInicial) {
        ComboBox<String> cb = buildBaseCombo(opciones, valorInicial);
        cb.setPrefWidth(220);
        return cb;
    }

    /**
     * Combo que se expande al máximo disponible (para arma y transporte)
     */
    private ComboBox<String> styledComboFull(List<String> opciones, String valorInicial) {
        ComboBox<String> cb = buildBaseCombo(opciones, valorInicial);
        cb.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(cb, Priority.ALWAYS);
        return cb;
    }

    private ComboBox<String> buildBaseCombo(List<String> opciones, String valorInicial) {
        ComboBox<String> cb = new ComboBox<>();
        cb.getItems().addAll(opciones);
        cb.setValue(valorInicial);
        cb.setPrefHeight(44);
        cb.setStyle("-fx-background-color: white; -fx-background-radius: 10;"
                + "-fx-border-color: " + BORDER + "; -fx-border-radius: 10;"
                + "-fx-border-width: 1.5; -fx-font-size: 13px; -fx-cursor: hand;");
        cb.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                setText(item.startsWith("—") ? item : item.replace("_", " "));
                setStyle("-fx-background-color: transparent; -fx-text-fill: #374151;"
                        + "-fx-font-size: 13px; -fx-padding: 9 14 9 14;");
                setOnMouseEntered(e -> setStyle("-fx-background-color: " + BLUE
                        + "; -fx-background-radius: 6; -fx-text-fill: white;"
                        + "-fx-font-size: 13px; -fx-padding: 9 14 9 14;"));
                setOnMouseExited(e -> setStyle("-fx-background-color: transparent;"
                        + "-fx-text-fill: #374151; -fx-font-size: 13px; -fx-padding: 9 14 9 14;"));
            }
        });
        cb.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.replace("_", " "));
                setStyle("-fx-background-color: transparent; -fx-text-fill: #111827;"
                        + "-fx-font-size: 13px; -fx-padding: 0 14;");
            }
        });
        return cb;
    }

    private String btnPrimaryStyle() {
        return "-fx-background-color: " + C_DARK_GRAD + "; -fx-background-radius: 10; -fx-padding: 0 22; -fx-cursor: hand;";
    }

    private String btnPrimaryHoverStyle() {
        return "-fx-background-color: #0d47a1; -fx-background-radius: 10; -fx-padding: 0 22; -fx-cursor: hand;";
    }

    // ═══════════════════════════════════════════════════════════════
    // DATOS
    // ═══════════════════════════════════════════════════════════════
    private List<Alerta> cargarAlertas() {
        if (alertaService == null) {
            return List.of();
        }
        try {
            List<Alerta> todas = alertaService.listar();
            if (usuarioActual == null) {
                return todas;
            }
            List<Alerta> mias = todas.stream()
                    .filter(a -> a.getUsuario() != null
                    && usuarioActual.getUsername() != null
                    && usuarioActual.getUsername().equals(a.getUsuario().getUsername()))
                    .collect(java.util.stream.Collectors.toList());
            return mias.isEmpty() ? todas : mias;
        } catch (Exception e) {
            return List.of();
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // FORMATEO
    // ═══════════════════════════════════════════════════════════════
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

    private java.awt.image.BufferedImage recortarTransparencia(java.awt.image.BufferedImage img) {
        if (img == null) {
            return null;
        }
        if (!img.getColorModel().hasAlpha()) {
            return img;
        }
        int w = img.getWidth(), h = img.getHeight();
        int top = h, bottom = -1, left = w, right = -1;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (((img.getRGB(x, y) >> 24) & 0xff) > 10) {
                    if (y < top) {
                        top = y;
                    }
                    if (y > bottom) {
                        bottom = y;
                    }
                    if (x < left) {
                        left = x;
                    }
                    if (x > right) {
                        right = x;
                    }
                }
            }
        }
        if (bottom < 0 || right < 0) {
            return img;
        }
        return img.getSubimage(left, top, right - left + 1, bottom - top + 1);
    }

}
