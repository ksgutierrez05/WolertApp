package sistemagestion.view;

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
import sistemagestion.model.*;
import sistemagestion.service.AlarmaService;

public class AlarmasPoliciaView {

    private static final String WHITE        = "#ffffff";
    private static final String BG           = "#f4f6fb";
    private static final String BLUE         = "#1565c0";
    private static final String BLUE_LIGHT   = "#e8f0fe";
    private static final String GREEN        = "#43a047";
    private static final String GREEN_LIGHT  = "#e8f5e9";
    private static final String RED          = "#e53935";
    private static final String RED_LIGHT    = "#fff0f0";
    private static final String ORANGE       = "#fb8c00";
    private static final String ORANGE_LIGHT = "#fff8e1";
    private static final String GRAY_TEXT    = "#6b7280";
    private static final String BORDER       = "#e5e7eb";
    private static final String DARK         = "#111827";

    private final AlarmaService alarmaService;
    private final Policia       policiaActual;

    private VBox      listaContainer;
    private TextField campoBusqueda;
    private List<Alarma> todasActivas;

    public AlarmasPoliciaView(AlarmaService alarmaService, Policia policiaActual) {
        javafx.scene.text.Font.loadFont(
                getClass().getResourceAsStream("/fa-solid-900.ttf"), 20);
        this.alarmaService = alarmaService;
        this.policiaActual = policiaActual;
    }

    // ═══════════════════════════════════════════════════════════════
    // PUNTO DE ENTRADA
    // ═══════════════════════════════════════════════════════════════
    public ScrollPane build() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color:" + BG + ";");

        try {
            todasActivas = alarmaService.listar().stream()
                    .filter(a -> a.getEstado() == EstadoAlarma.ACTIVA)
                    .toList();
        } catch (Exception e) {
            todasActivas = List.of();
        }

        listaContainer = new VBox(14);

        content.getChildren().addAll(
                buildTopBar(),
                buildBanner(),
                buildStats(),
                buildToolbar(),
                buildSeccionHeader(),
                listaContainer
        );

        renderizarLista(todasActivas);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color:" + BG + "; -fx-background:" + BG + ";"
                + "-fx-border-color:transparent;");
        return scroll;
    }

    // ═══════════════════════════════════════════════════════════════
    // TOP BAR
    // ═══════════════════════════════════════════════════════════════
    private HBox buildTopBar() {
        HBox bar = new HBox(16);
        bar.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(bar, Priority.ALWAYS);

        VBox titles = new VBox(4);
        HBox.setHgrow(titles, Priority.ALWAYS);
        Label title = new Label("Alarmas Activas");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.web(DARK));
        Label sub = new Label("Alarmas operativas en tu zona de patrullaje");
        sub.setFont(Font.font("System", 13));
        sub.setTextFill(Color.web(GRAY_TEXT));
        titles.getChildren().addAll(title, sub);

        // Chip del policía
        if (policiaActual != null) {
            HBox chip = new HBox(10);
            chip.setAlignment(Pos.CENTER);
            chip.setPadding(new Insets(10, 18, 10, 14));
            chip.setStyle("-fx-background-color:white;-fx-background-radius:14;"
                    + "-fx-border-color:" + BORDER + ";-fx-border-radius:14;-fx-border-width:1;");
            shadow(chip);

            StackPane av = new StackPane();
            av.setPrefSize(38, 38); av.setMinSize(38, 38); av.setMaxSize(38, 38);
            Circle avCircle = new Circle(19, Color.web("#dbeafe"));
            Label avFA = new Label("\uf505");
            avFA.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';"
                    + "-fx-font-size:15px;-fx-text-fill:" + BLUE + ";");
            av.getChildren().addAll(avCircle, avFA);

            VBox info = new VBox(2);
            String nombre = policiaActual.getPrimer_nombre() != null
                    ? policiaActual.getPrimer_nombre() : "Oficial";
            Label nombreLbl = new Label(nombre);
            nombreLbl.setFont(Font.font("System", FontWeight.BOLD, 13));
            nombreLbl.setTextFill(Color.web(DARK));
            String unidadStr = policiaActual.getUnidadpolicial() != null
                    ? policiaActual.getUnidadpolicial().getNombre() : "Sin unidad";
            Label unidadLbl = new Label(unidadStr);
            unidadLbl.setFont(Font.font("System", 11));
            unidadLbl.setTextFill(Color.web(GRAY_TEXT));
            info.getChildren().addAll(nombreLbl, unidadLbl);
            chip.getChildren().addAll(av, info);
            bar.getChildren().addAll(titles, chip);
        } else {
            bar.getChildren().add(titles);
        }
        return bar;
    }

    // ═══════════════════════════════════════════════════════════════
    // BANNER INFORMATIVO
    // ═══════════════════════════════════════════════════════════════
    private HBox buildBanner() {
        HBox banner = new HBox(20);
        banner.setAlignment(Pos.CENTER_LEFT);
        banner.setPadding(new Insets(18, 24, 18, 24));
        banner.setStyle(
                "-fx-background-color:linear-gradient(to right, #dfebf5, #f0f8ff);"
                + "-fx-background-radius:16;");
        shadow(banner);

        // ── Ícono grande a la izquierda ────────────────────────────
        StackPane icoWrap = new StackPane();
        icoWrap.setPrefSize(54, 54); icoWrap.setMinSize(54, 54); icoWrap.setMaxSize(54, 54);
        Circle icoBg = new Circle(27, Color.web("#ffffff80"));
        Label icoLbl = new Label("\uf0f3");
        icoLbl.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';"
                + "-fx-font-size:24px;-fx-text-fill:#1565c0");
        icoWrap.getChildren().addAll(icoBg, icoLbl);

        // ── Texto central ──────────────────────────────────────────
        VBox centro = new VBox(5);
        HBox.setHgrow(centro, Priority.ALWAYS);

        Label t1 = new Label("Vista operativa · Solo lectura");
        t1.setFont(Font.font("System", FontWeight.BOLD, 15));
        t1.setTextFill(Color.WHITE);

        Label t2 = new Label(
                "Únicamente se muestran las alarmas activas en el sistema.\n"
                + "Ante cualquier activación, comunícate de inmediato con tu central.");
        t2.setFont(Font.font("System", 12));
        t2.setTextFill(Color.web("#e3f2fd"));
        t2.setWrapText(true);

        // Mini chips informativos
        HBox chips = new HBox(8);
        chips.setAlignment(Pos.CENTER_LEFT);
        chips.getChildren().addAll(
                bannerChip("\uf05a", "Sin permisos de edición"),
                bannerChip("\uf017", "Actualizado al cargar"),
                bannerChip("\uf3ed", "Solo alarmas ACTIVAS")
        );

        centro.getChildren().addAll(t1, t2, chips);

        // ── Contador a la derecha ──────────────────────────────────
        VBox contador = new VBox(4);
        contador.setAlignment(Pos.CENTER);
        contador.setPadding(new Insets(10, 18, 10, 18));
        contador.setStyle(
                "-fx-background-color:#ffffff100;"
                + "-fx-background-radius:14;");
        contador.setMinWidth(90);

        // Punto verde pulsante
        StackPane pulse = new StackPane();
        Circle outer = new Circle(10, Color.web("#ffffff33"));
        Circle inner = new Circle(6, Color.web(GREEN));
        pulse.getChildren().addAll(outer, inner);
        pulse.setAlignment(Pos.CENTER);

        Label numLbl = new Label(String.valueOf(todasActivas.size()));
        numLbl.setFont(Font.font("System", FontWeight.BOLD, 34));
        numLbl.setTextFill(Color.WHITE);
        numLbl.setAlignment(Pos.CENTER);

        Label subLbl = new Label("alarmas activas");
        subLbl.setFont(Font.font("System", FontWeight.BOLD, 10));
        subLbl.setTextFill(Color.web("#e3f2fd"));
        subLbl.setAlignment(Pos.CENTER);

        contador.getChildren().addAll(pulse, numLbl, subLbl);

        banner.getChildren().addAll(icoWrap, centro, contador);
        return banner;
    }

    private HBox bannerChip(String iconFA, String texto) {
        HBox chip = new HBox(5);
        chip.setAlignment(Pos.CENTER);
        chip.setPadding(new Insets(3, 10, 3, 10));
        chip.setStyle("-fx-background-color:#ffffff22;-fx-background-radius:20;");
        Label ico = new Label(iconFA);
        ico.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';"
                + "-fx-font-size:10px;-fx-text-fill:#1565c0");
        Label txt = new Label(texto);
        txt.setFont(Font.font("System", FontWeight.BOLD, 10));
        txt.setTextFill(Color.WHITE);
        chip.getChildren().addAll(ico, txt);
        return chip;
    }

    // ═══════════════════════════════════════════════════════════════
    // STATS
    // ═══════════════════════════════════════════════════════════════
    private HBox buildStats() {
        long total    = todasActivas.size();

        // Barrios únicos cubiertos
        long barrios  = todasActivas.stream()
                .filter(a -> a.getBarrio() != null)
                .map(a -> a.getBarrio().getNombre())
                .distinct().count();

        // Radio promedio
        double radioPromedio = todasActivas.stream()
                .mapToDouble(Alarma::getRadio_cobertura)
                .average().orElse(0);

        // Alarmas con coordenadas registradas
        long conUbicacion = todasActivas.stream()
                .filter(a -> a.getLatitud() != 0 || a.getLongitud() != 0).count();

        HBox row = new HBox(16);
        HBox.setHgrow(row, Priority.ALWAYS);
        row.getChildren().addAll(
                statCard(GREEN_LIGHT,  GREEN,   "\uf058", "Alarmas activas",
                        boldNum(String.valueOf(total),           GREEN),   "Operativas ahora"),
                statCard(BLUE_LIGHT,   BLUE,    "\uf3c5", "Barrios cubiertos",
                        boldNum(String.valueOf(barrios),         BLUE),    "Con cobertura"),
                statCard(ORANGE_LIGHT, ORANGE,  "\uf1db", "Radio promedio",
                        boldNum((int) radioPromedio + " m",      ORANGE),  "De cobertura"),
                statCard("#f5f3ff",    "#7c3aed","\uf124","Con ubicación GPS",
                        boldNum(String.valueOf(conUbicacion),   "#7c3aed"),"Coordenadas disponibles")
        );
        return row;
    }

    private Label boldNum(String val, String color) {
        Label l = new Label(val);
        l.setFont(Font.font("System", FontWeight.BOLD, 32));
        l.setTextFill(Color.web(color));
        return l;
    }

    private VBox statCard(String bgIcon, String accent, String iconFA,
                          String title, Label valueLabel, String sub) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(18, 20, 18, 20));
        card.setStyle("-fx-background-color:white;-fx-background-radius:18;");
        HBox.setHgrow(card, Priority.ALWAYS);
        shadow(card);

        StackPane iconWrap = new StackPane();
        iconWrap.setPrefSize(48, 48); iconWrap.setMinSize(48, 48); iconWrap.setMaxSize(48, 48);
        Rectangle iconBg = new Rectangle(48, 48);
        iconBg.setArcWidth(14); iconBg.setArcHeight(14);
        iconBg.setFill(Color.web(bgIcon));
        Label iconLbl = new Label(iconFA);
        iconLbl.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';"
                + "-fx-font-size:20px;-fx-text-fill:" + accent + ";");
        iconWrap.getChildren().addAll(iconBg, iconLbl);

        Label titleLbl = new Label(title);
        titleLbl.setFont(Font.font("System", FontWeight.BOLD, 12));
        titleLbl.setTextFill(Color.web("#374151"));
        Label subLbl = new Label(sub);
        subLbl.setFont(Font.font("System", 11));
        subLbl.setTextFill(Color.web(GRAY_TEXT));

        HBox top = new HBox(14);
        top.setAlignment(Pos.CENTER_LEFT);
        top.getChildren().addAll(iconWrap, new VBox(3, titleLbl, valueLabel, subLbl));
        card.getChildren().add(top);
        card.setOnMouseEntered(e -> card.setTranslateY(-3));
        card.setOnMouseExited(e  -> card.setTranslateY(0));
        return card;
    }

    // ═══════════════════════════════════════════════════════════════
    // TOOLBAR DE BÚSQUEDA
    // ═══════════════════════════════════════════════════════════════
    private HBox buildToolbar() {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(14, 20, 14, 20));
        bar.setStyle("-fx-background-color:white;-fx-background-radius:12;");
        shadow(bar);

        // Campo búsqueda
        HBox searchBox = new HBox(8);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setStyle("-fx-background-color:#f5f7fb;-fx-background-radius:10;-fx-padding:0 14;");
        searchBox.setPrefHeight(42);
        HBox.setHgrow(searchBox, Priority.ALWAYS);

        Label searchIcon = new Label("\uf002");
        searchIcon.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';"
                + "-fx-font-size:14px;-fx-text-fill:#9ca3af;");

        campoBusqueda = new TextField();
        campoBusqueda.setPromptText("Buscar por nombre o barrio...");
        campoBusqueda.setStyle("-fx-background-color:transparent;-fx-border-color:transparent;"
                + "-fx-font-size:13px;-fx-text-fill:" + DARK + ";");
        campoBusqueda.setPrefHeight(42);
        HBox.setHgrow(campoBusqueda, Priority.ALWAYS);
        campoBusqueda.textProperty().addListener((obs, o, n) -> filtrarYMostrar());
        searchBox.getChildren().addAll(searchIcon, campoBusqueda);

        // Chip "Solo activas" decorativo
        HBox chipActivas = new HBox(6);
        chipActivas.setAlignment(Pos.CENTER);
        chipActivas.setPadding(new Insets(6, 14, 6, 14));
        chipActivas.setStyle("-fx-background-color:" + GREEN_LIGHT + ";"
                + "-fx-background-radius:20;-fx-border-color:" + GREEN + "55;"
                + "-fx-border-radius:20;-fx-border-width:1;");
        Label chipIco = new Label("\uf058");
        chipIco.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';"
                + "-fx-font-size:11px;-fx-text-fill:" + GREEN + ";");
        Label chipTxt = new Label("Solo activas");
        chipTxt.setFont(Font.font("System", FontWeight.BOLD, 11));
        chipTxt.setTextFill(Color.web(GREEN));
        chipActivas.getChildren().addAll(chipIco, chipTxt);

        bar.getChildren().addAll(searchBox, chipActivas);
        return bar;
    }

    // ═══════════════════════════════════════════════════════════════
    // ENCABEZADO DE SECCIÓN
    // ═══════════════════════════════════════════════════════════════
    private HBox buildSeccionHeader() {
        HBox h = new HBox(8);
        h.setAlignment(Pos.CENTER_LEFT);
        Label ico = new Label("\uf0f3");
        ico.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';"
                + "-fx-font-size:14px;-fx-text-fill:" + GREEN + ";");
        Label txt = new Label("Alarmas operativas");
        txt.setFont(Font.font("System", FontWeight.BOLD, 15));
        txt.setTextFill(Color.web(DARK));
        Label count = new Label("(" + todasActivas.size() + " registradas)");
        count.setFont(Font.font("System", 12));
        count.setTextFill(Color.web(GRAY_TEXT));
        h.getChildren().addAll(ico, txt, count);
        return h;
    }

    // ═══════════════════════════════════════════════════════════════
    // FILTRAR
    // ═══════════════════════════════════════════════════════════════
    private void filtrarYMostrar() {
        String txt = campoBusqueda.getText().toLowerCase().trim();
        if (txt.isEmpty()) { renderizarLista(todasActivas); return; }
        List<Alarma> filtradas = todasActivas.stream()
                .filter(a -> (a.getNombre() != null && a.getNombre().toLowerCase().contains(txt))
                        || (a.getBarrio() != null && a.getBarrio().getNombre().toLowerCase().contains(txt)))
                .toList();
        renderizarLista(filtradas);
    }

    // ═══════════════════════════════════════════════════════════════
    // RENDERIZAR LISTA
    // ═══════════════════════════════════════════════════════════════
    private void renderizarLista(List<Alarma> lista) {
        listaContainer.getChildren().clear();

        if (lista.isEmpty()) {
            VBox vacio = new VBox(12);
            vacio.setAlignment(Pos.CENTER);
            vacio.setPadding(new Insets(50));
            vacio.setStyle("-fx-background-color:white;-fx-background-radius:18;");
            shadow(vacio);
            Label icn = new Label("\uf0f3");
            icn.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';"
                    + "-fx-font-size:48px;-fx-text-fill:#d1d5db;");
            Label m1 = new Label("No hay alarmas activas");
            m1.setFont(Font.font("System", FontWeight.BOLD, 15));
            m1.setTextFill(Color.web(GRAY_TEXT));
            Label m2 = new Label("Las alarmas operativas aparecerán aquí");
            m2.setFont(Font.font("System", 12));
            m2.setTextFill(Color.web(GRAY_TEXT));
            vacio.getChildren().addAll(icn, m1, m2);
            listaContainer.getChildren().add(vacio);
            return;
        }

        for (Alarma a : lista) {
            listaContainer.getChildren().add(buildCard(a));
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // TARJETA DE ALARMA 
    // ═══════════════════════════════════════════════════════════════
    private VBox buildCard(Alarma a) {
        VBox card = new VBox(0);
        card.setStyle("-fx-background-color:white;-fx-background-radius:18;");
        shadow(card);

        // ── Cabecera ───────────────────────────────────────────────
        HBox header = new HBox(14);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(16, 20, 14, 20));
        header.setStyle("-fx-background-color:" + GREEN_LIGHT + ";"
                + "-fx-background-radius:18 18 0 0;"
                + "-fx-border-color:transparent transparent " + BORDER + " transparent;"
                + "-fx-border-width:0 0 1 0;");

        // Avatar
        StackPane av = new StackPane();
        av.setPrefSize(46, 46); av.setMinSize(46, 46); av.setMaxSize(46, 46);
        Circle avCircle = new Circle(23, Color.web(colorAvatar(a.getNombre())));
        Label avLbl = new Label(iniciales(a.getNombre()));
        avLbl.setFont(Font.font("System", FontWeight.BOLD, 15));
        avLbl.setTextFill(Color.WHITE);
        av.getChildren().addAll(avCircle, avLbl);

        // Info principal
        VBox infoBox = new VBox(4);
        HBox.setHgrow(infoBox, Priority.ALWAYS);
        Label nomLbl = new Label(a.getNombre() != null ? a.getNombre() : "—");
        nomLbl.setFont(Font.font("System", FontWeight.BOLD, 15));
        nomLbl.setTextFill(Color.web(DARK));

        HBox subRow = new HBox(8);
        subRow.setAlignment(Pos.CENTER_LEFT);
        if (a.getBarrio() != null) {
            subRow.getChildren().add(miniChip("\uf3c5", a.getBarrio().getNombre(), "#f0fdf4", GREEN));
        }
        subRow.getChildren().add(miniChip("\uf0f3", "ID " + a.getId_alarma(), "#f0fdf4", GREEN));
        infoBox.getChildren().addAll(nomLbl, subRow);

        // Badge ACTIVA
        HBox badgeBox = new HBox(5);
        badgeBox.setAlignment(Pos.CENTER);
        badgeBox.setPadding(new Insets(5, 14, 5, 12));
        badgeBox.setStyle("-fx-background-color:" + GREEN_LIGHT + ";"
                + "-fx-background-radius:20;-fx-border-color:" + GREEN + "55;"
                + "-fx-border-radius:20;-fx-border-width:1;");
        Label bIco = new Label("\uf058");
        bIco.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';"
                + "-fx-font-size:11px;-fx-text-fill:" + GREEN + ";");
        Label bTxt = new Label("ACTIVA");
        bTxt.setFont(Font.font("System", FontWeight.BOLD, 11));
        bTxt.setTextFill(Color.web(GREEN));
        badgeBox.getChildren().addAll(bIco, bTxt);

        header.getChildren().addAll(av, infoBox, badgeBox);

        // ── Cuerpo ─────────────────────────────────────────────────
        VBox body = new VBox(14);
        body.setPadding(new Insets(16, 20, 18, 20));

        // Fila de datos clave
        HBox datosRow = new HBox(16);
        datosRow.setAlignment(Pos.CENTER_LEFT);
        datosRow.getChildren().addAll(
                datoBloque("\uf1db", "Radio de cobertura", (int) a.getRadio_cobertura() + " metros"),
                datoBloque("\uf3c5", "Barrio",
                        a.getBarrio() != null ? a.getBarrio().getNombre() : "—"),
                datoBloque("\uf124", "Coordenadas",
                        (a.getLatitud() != 0 || a.getLongitud() != 0)
                        ? String.format("%.4f, %.4f", a.getLatitud(), a.getLongitud()) : "No disponible")
        );

        // Separador
        Region sep = new Region();
        sep.setPrefHeight(1);
        sep.setStyle("-fx-background-color:" + BORDER + ";");

        // Chips informativos
        FlowPane chips = new FlowPane();
        chips.setHgap(8); chips.setVgap(8);

        chips.getChildren().add(chipFA("\uf1db",
                "Radio: " + (int) a.getRadio_cobertura() + " m", ORANGE_LIGHT, ORANGE));

        if (a.getBarrio() != null) {
            chips.getChildren().add(chipFA("\uf3c5",
                    a.getBarrio().getNombre(), BLUE_LIGHT, BLUE));
        }
        if (a.getLatitud() != 0 || a.getLongitud() != 0) {
            chips.getChildren().add(chipFA("\uf124",
                    "GPS disponible", "#f0fdf4", GREEN));
        } else {
            chips.getChildren().add(chipFA("\uf05e",
                    "Sin GPS", "#f3f4f6", GRAY_TEXT));
        }
        chips.getChildren().add(chipFA("\uf0f3",
                "Alarma operativa", GREEN_LIGHT, GREEN));

        // Nota de solo lectura
        HBox nota = new HBox(8);
        nota.setAlignment(Pos.CENTER_LEFT);
        nota.setPadding(new Insets(10, 14, 10, 14));
        nota.setStyle("-fx-background-color:#f8fafc;-fx-background-radius:10;"
                + "-fx-border-color:" + BORDER + ";-fx-border-radius:10;-fx-border-width:1;");
        Label notaIco = new Label("\uf05a");
        notaIco.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';"
                + "-fx-font-size:12px;-fx-text-fill:" + BLUE + ";");
        Label notaTxt = new Label(
                "Esta alarma está activa en tu zona. Ante cualquier activación, reporta a tu central.");
        notaTxt.setFont(Font.font("System", 11));
        notaTxt.setTextFill(Color.web(GRAY_TEXT));
        notaTxt.setWrapText(true);
        HBox.setHgrow(notaTxt, Priority.ALWAYS);
        nota.getChildren().addAll(notaIco, notaTxt);

        body.getChildren().addAll(datosRow, sep, chips, nota);
        card.getChildren().addAll(header, body);

        // Hover sutil
        card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color:white;-fx-background-radius:18;"
                + "-fx-border-color:" + GREEN + "55;-fx-border-radius:18;-fx-border-width:1.5;"));
        card.setOnMouseExited(e -> card.setStyle(
                "-fx-background-color:white;-fx-background-radius:18;"));

        return card;
    }

    // ═══════════════════════════════════════════════════════════════
    // HELPERS UI
    // ═══════════════════════════════════════════════════════════════
    private VBox datoBloque(String iconFA, String label, String valor) {
        VBox bloque = new VBox(4);
        HBox.setHgrow(bloque, Priority.ALWAYS);
        HBox labelRow = new HBox(5);
        labelRow.setAlignment(Pos.CENTER_LEFT);
        Label ico = new Label(iconFA);
        ico.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';"
                + "-fx-font-size:10px;-fx-text-fill:" + GRAY_TEXT + ";");
        Label lbl = new Label(label);
        lbl.setFont(Font.font("System", FontWeight.BOLD, 10));
        lbl.setTextFill(Color.web(GRAY_TEXT));
        labelRow.getChildren().addAll(ico, lbl);
        Label val = new Label(valor);
        val.setFont(Font.font("System", FontWeight.BOLD, 13));
        val.setTextFill(Color.web(DARK));
        bloque.getChildren().addAll(labelRow, val);
        return bloque;
    }

    private HBox chipFA(String iconFA, String texto, String bg, String fg) {
        HBox chip = new HBox(6);
        chip.setAlignment(Pos.CENTER_LEFT);
        chip.setPadding(new Insets(5, 11, 5, 11));
        chip.setStyle("-fx-background-color:" + bg + ";-fx-background-radius:20;"
                + "-fx-border-color:" + fg + "44;-fx-border-radius:20;-fx-border-width:1;");
        Label ico = new Label(iconFA);
        ico.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';"
                + "-fx-font-size:11px;-fx-text-fill:" + fg + ";");
        Label txt = new Label(texto);
        txt.setFont(Font.font("System", FontWeight.BOLD, 11));
        txt.setTextFill(Color.web(fg));
        chip.getChildren().addAll(ico, txt);
        return chip;
    }

    private HBox miniChip(String iconFA, String texto, String bg, String fg) {
        HBox chip = new HBox(5);
        chip.setAlignment(Pos.CENTER_LEFT);
        chip.setPadding(new Insets(3, 9, 3, 9));
        chip.setStyle("-fx-background-color:" + bg + ";-fx-background-radius:14;"
                + "-fx-border-color:" + fg + "55;-fx-border-radius:14;-fx-border-width:1;");
        Label ico = new Label(iconFA);
        ico.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';"
                + "-fx-font-size:10px;-fx-text-fill:" + fg + ";");
        Label txt = new Label(texto);
        txt.setFont(Font.font("System", FontWeight.BOLD, 10));
        txt.setTextFill(Color.web(fg));
        chip.getChildren().addAll(ico, txt);
        return chip;
    }

    private void shadow(Region node) {
        node.setEffect(new DropShadow(12, 0, 2, Color.web("#0000001a")));
    }

    // ═══════════════════════════════════════════════════════════════
    // AVATAR
    // ═══════════════════════════════════════════════════════════════
    private static final String[] AVATAR_COLORS = {
        "#1565c0", "#2e7d32", "#6a1b9a", "#c62828",
        "#e65100", "#00695c", "#283593", "#4e342e"
    };

    private String colorAvatar(String nombre) {
        if (nombre == null || nombre.isBlank()) return AVATAR_COLORS[0];
        return AVATAR_COLORS[Math.abs(nombre.hashCode()) % AVATAR_COLORS.length];
    }

    private String iniciales(String nombre) {
        if (nombre == null || nombre.isBlank()) return "?";
        String[] p = nombre.trim().split("\\s+");
        return p.length == 1
                ? p[0].substring(0, 1).toUpperCase()
                : (p[0].substring(0, 1) + p[1].substring(0, 1)).toUpperCase();
    }
}