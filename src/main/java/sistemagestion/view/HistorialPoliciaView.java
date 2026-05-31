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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import sistemagestion.model.*;
import sistemagestion.service.*;

public class HistorialPoliciaView {

    private static final String BG        = "#f4f6fb";
    private static final String BLUE      = "#1565c0";
    private static final String GREEN     = "#43a047";
    private static final String RED       = "#e53935";
    private static final String RED_LIGHT = "#fff0f0";
    private static final String GRAY_TEXT = "#6b7280";
    private static final String BORDER    = "#e5e7eb";

    private final Usuario               usuarioActual;
    private final Policia               policiaActual;
    private final AtencionAlertaService atencionService;
    private final BorderPane            root;

    public HistorialPoliciaView(Usuario usuarioActual, Policia policiaActual,
            AtencionAlertaService atencionService, BorderPane root) {
        javafx.scene.text.Font.loadFont(
                getClass().getResourceAsStream("/fa-solid-900.ttf"), 20);
        this.usuarioActual   = usuarioActual;
        this.policiaActual   = policiaActual;
        this.atencionService = atencionService;
        this.root            = root;
    }

    // ═══════════════════════════════════════════════════════════════
    // PUNTO DE ENTRADA
    // ═══════════════════════════════════════════════════════════════
    public ScrollPane build() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color: " + BG + ";");

        // Solo finalizadas y canceladas — historial cerrado
        List<AtencionAlerta> historial = cargarHistorial();

        VBox listaContainer = new VBox(16);
        renderLista(listaContainer, historial, null, "");

        content.getChildren().addAll(
                buildTopBar(),
                buildStatsRow(historial),
                buildFiltros(historial, listaContainer),
                listaContainer
        );

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
        HBox bar = new HBox(16);
        bar.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(bar, Priority.ALWAYS);

        VBox titles = new VBox(4);
        HBox.setHgrow(titles, Priority.ALWAYS);
        Label title = new Label("Mi Historial");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#111827"));
        Label sub = new Label("Atenciones finalizadas y canceladas de tu unidad");
        sub.setFont(Font.font("System", 13));
        sub.setTextFill(Color.web(GRAY_TEXT));
        titles.getChildren().addAll(title, sub);

        // Chip del policía
        if (policiaActual != null) {
            HBox chip = new HBox(10);
            chip.setAlignment(Pos.CENTER);
            chip.setPadding(new Insets(10, 18, 10, 14));
            chip.setStyle("-fx-background-color: white; -fx-background-radius: 14;"
                    + "-fx-border-color: " + BORDER + "; -fx-border-radius: 14; -fx-border-width: 1;");
            shadow(chip);

            StackPane av = new StackPane();
            av.setPrefSize(38, 38); av.setMinSize(38, 38); av.setMaxSize(38, 38);
            Circle avCircle = new Circle(19, Color.web("#dbeafe"));
            Label avFA = new Label("\uf007");
            avFA.setStyle("-fx-font-family: 'Font Awesome 6 Free Solid'; -fx-font-size: 15px; -fx-text-fill: " + BLUE + ";");
            av.getChildren().addAll(avCircle, avFA);

            VBox info = new VBox(2);
            String nombre = policiaActual.getPrimer_nombre() != null
                    ? policiaActual.getPrimer_nombre()
                    : (usuarioActual != null ? usuarioActual.getUsername() : "Oficial");
            Label nombreLbl = new Label(nombre);
            nombreLbl.setFont(Font.font("System", FontWeight.BOLD, 13));
            nombreLbl.setTextFill(Color.web("#111827"));

            String unidadStr = (policiaActual.getUnidadpolicial() != null)
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
    // STATS ROW  (solo FINALIZADA y CANCELADA)
    // ═══════════════════════════════════════════════════════════════
    private HBox buildStatsRow(List<AtencionAlerta> lista) {
        long finalizadas = lista.stream()
                .filter(a -> a.getEstado() == EstadoAtencionAlerta.FINALIZADA).count();
        long canceladas  = lista.stream()
                .filter(a -> a.getEstado() == EstadoAtencionAlerta.CANCELADA).count();
        long total       = lista.size();

        // Calcular mes actual
        long esteMes = lista.stream().filter(a -> {
            if (a.getFechaatencion() == null) return false;
            LocalDateTime now = LocalDateTime.now();
            return a.getFechaatencion().getMonth() == now.getMonth()
                    && a.getFechaatencion().getYear() == now.getYear();
        }).count();

        HBox row = new HBox(16);
        HBox.setHgrow(row, Priority.ALWAYS);
        row.getChildren().addAll(
                statCard("#e8f5e9",  GREEN,     "\uf058", "Finalizadas",  boldNum(String.valueOf(finalizadas), GREEN),    "Completadas"),
                statCard("#f3f4f6",  GRAY_TEXT, "\uf057", "Canceladas",   boldNum(String.valueOf(canceladas),  GRAY_TEXT),"Cerradas sin atender"),
                statCard("#e3f2fd",  BLUE,      "\uf073", "Este mes",     boldNum(String.valueOf(esteMes),     BLUE),     "Atenciones del mes"),
                statCard("#fef9c3",  "#92400e", "\uf1da", "Total",        boldNum(String.valueOf(total),       "#92400e"),"En tu historial")
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
        iconWrap.setPrefSize(52, 52); iconWrap.setMinSize(52, 52); iconWrap.setMaxSize(52, 52);
        Rectangle iconBg = new Rectangle(52, 52);
        iconBg.setArcWidth(16); iconBg.setArcHeight(16);
        iconBg.setFill(Color.web(bgIcon));
        Label iconLbl = new Label(iconFA);
        iconLbl.setStyle("-fx-font-family: 'Font Awesome 6 Free Solid'; -fx-font-size: 22px; -fx-text-fill: " + accentColor + ";");
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
        card.setOnMouseExited(e  -> card.setTranslateY(0));
        return card;
    }

    // ═══════════════════════════════════════════════════════════════
    // BARRA DE FILTROS
    // ═══════════════════════════════════════════════════════════════
    private VBox buildFiltros(List<AtencionAlerta> todas, VBox listaContainer) {
        VBox panel = new VBox(12);
        panel.setPadding(new Insets(16, 20, 16, 20));
        panel.setStyle("-fx-background-color: white; -fx-background-radius: 16;");
        shadow(panel);

        HBox titulo = new HBox(8);
        titulo.setAlignment(Pos.CENTER_LEFT);
        Label fFA = new Label("\uf0b0");
        fFA.setStyle("-fx-font-family: 'Font Awesome 6 Free Solid'; -fx-font-size: 12px; -fx-text-fill: " + BLUE + ";");
        Label fTxt = new Label("Filtrar historial");
        fTxt.setFont(Font.font("System", FontWeight.BOLD, 13));
        fTxt.setTextFill(Color.web(BLUE));
        titulo.getChildren().addAll(fFA, fTxt);

        // Búsqueda
        TextField busqueda = new TextField();
        busqueda.setPromptText("\uf002  Buscar por descripción, barrio, tipo, observación...");
        busqueda.setPrefWidth(280); busqueda.setPrefHeight(38);
        String bBase = "-fx-background-color: #f8fafc; -fx-background-radius: 10;"
                + "-fx-border-color: " + BORDER + "; -fx-border-radius: 10; -fx-border-width: 1.5;"
                + "-fx-font-size: 13px; -fx-text-fill: #1E293B; -fx-prompt-text-fill: #94A3B8;"
                + "-fx-padding: 0 14; -fx-focus-color: transparent; -fx-faint-focus-color: transparent;";
        busqueda.setStyle(bBase);
        busqueda.focusedProperty().addListener((o, ov, f) ->
                busqueda.setStyle(f ? bBase.replace(BORDER, "#93c5fd") : bBase));

        // Pills — solo los estados del historial
        record Pill(String label, String estado, String icono, String color, String bg) {}
        List<Pill> pills = List.of(
            new Pill("Todos",       null,        "\uf0c9", BLUE,      "#e3f2fd"),
            new Pill("Finalizadas", "FINALIZADA", "\uf058", GREEN,     "#e8f5e9"),
            new Pill("Canceladas",  "CANCELADA",  "\uf057", GRAY_TEXT, "#f3f4f6")
        );

        HBox pillRow = new HBox(8);
        pillRow.setAlignment(Pos.CENTER_LEFT);
        final String[] estadoActivo = {null};
        final Button[] activoBtn    = {null};

        for (Pill p : pills) {
            Button btn = new Button();
            HBox bc = new HBox(5); bc.setAlignment(Pos.CENTER);
            Label bFA  = new Label(p.icono());
            bFA.setStyle("-fx-font-family: 'Font Awesome 6 Free Solid'; -fx-font-size: 10px;");
            Label bTxt = new Label(p.label());
            bTxt.setFont(Font.font("System", FontWeight.BOLD, 11));
            bc.getChildren().addAll(bFA, bTxt);
            btn.setGraphic(bc); btn.setPrefHeight(34); btn.setCursor(javafx.scene.Cursor.HAND);

            Runnable inactive = () -> {
                btn.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 20;"
                        + "-fx-border-color: " + BORDER + "; -fx-border-radius: 20; -fx-border-width: 1;");
                bFA.setStyle("-fx-font-family: 'Font Awesome 6 Free Solid'; -fx-font-size: 10px; -fx-text-fill: " + GRAY_TEXT + ";");
                bTxt.setStyle("-fx-text-fill: " + GRAY_TEXT + ";");
            };
            Runnable active = () -> {
                btn.setStyle("-fx-background-color: " + p.bg() + "; -fx-background-radius: 20;"
                        + "-fx-border-color: " + p.color() + "; -fx-border-radius: 20; -fx-border-width: 1.5;");
                bFA.setStyle("-fx-font-family: 'Font Awesome 6 Free Solid'; -fx-font-size: 10px; -fx-text-fill: " + p.color() + ";");
                bTxt.setStyle("-fx-text-fill: " + p.color() + "; -fx-font-weight: bold;");
            };

            if (p.estado() == null) { active.run(); activoBtn[0] = btn; }
            else inactive.run();

            btn.setOnMouseEntered(e -> { if (activoBtn[0] != btn) btn.setStyle(
                    "-fx-background-color: " + p.bg() + "; -fx-background-radius: 20;"
                    + "-fx-border-color: " + p.color() + "88; -fx-border-radius: 20; -fx-border-width: 1;"); });
            btn.setOnMouseExited(e -> { if (activoBtn[0] != btn) inactive.run(); });
            btn.setOnAction(e -> {
                if (activoBtn[0] != null) {
                    Button prev = activoBtn[0];
                    prev.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 20;"
                            + "-fx-border-color: " + BORDER + "; -fx-border-radius: 20; -fx-border-width: 1;");
                    if (prev.getGraphic() instanceof HBox ph)
                        ph.getChildren().forEach(n -> { if (n instanceof Label ll) {
                            if (ll.getStyle().contains("Font Awesome"))
                                ll.setStyle("-fx-font-family: 'Font Awesome 6 Free Solid'; -fx-font-size: 10px; -fx-text-fill: " + GRAY_TEXT + ";");
                            else ll.setStyle("-fx-text-fill: " + GRAY_TEXT + ";");
                        }});
                }
                activoBtn[0] = btn; estadoActivo[0] = p.estado(); active.run();
                renderLista(listaContainer, todas, estadoActivo[0], busqueda.getText().trim());
            });
            pillRow.getChildren().add(btn);
        }

        busqueda.textProperty().addListener((o, ov, nv) ->
                renderLista(listaContainer, todas, estadoActivo[0], nv.trim()));

        // Limpiar
        Button limpiar = new Button();
        HBox lc = new HBox(5); lc.setAlignment(Pos.CENTER);
        Label lFA  = new Label("\uf2ed");
        lFA.setStyle("-fx-font-family: 'Font Awesome 6 Free Solid'; -fx-font-size: 11px; -fx-text-fill: " + GRAY_TEXT + ";");
        Label lTxt = new Label("Limpiar");
        lTxt.setFont(Font.font("System", 12)); lTxt.setStyle("-fx-text-fill: " + GRAY_TEXT + ";");
        lc.getChildren().addAll(lFA, lTxt);
        limpiar.setGraphic(lc); limpiar.setPrefHeight(34); limpiar.setCursor(javafx.scene.Cursor.HAND);
        limpiar.setStyle("-fx-background-color: transparent; -fx-border-color: " + BORDER
                + "; -fx-border-radius: 20; -fx-border-width: 1; -fx-background-radius: 20;");
        limpiar.setOnAction(e -> { busqueda.clear(); pillRow.getChildren().get(0).fireEvent(
                new javafx.scene.input.MouseEvent(javafx.scene.input.MouseEvent.MOUSE_CLICKED,
                    0,0,0,0,javafx.scene.input.MouseButton.PRIMARY,1,
                    false,false,false,false,false,false,false,false,false,false,null)); });

        HBox spacer = new HBox(); HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox controles = new HBox(12);
        controles.setAlignment(Pos.CENTER_LEFT);
        controles.getChildren().addAll(busqueda, pillRow, spacer, limpiar);
        panel.getChildren().addAll(titulo, controles);
        return panel;
    }

    // ═══════════════════════════════════════════════════════════════
    // RENDER DINÁMICO
    // ═══════════════════════════════════════════════════════════════
    private void renderLista(VBox container, List<AtencionAlerta> todas,
                              String estadoFiltro, String texto) {
        container.getChildren().clear();

        List<AtencionAlerta> filtradas = todas.stream().filter(at -> {
            if (estadoFiltro != null) {
                if (at.getEstado() == null) return false;
                if (!at.getEstado().name().equals(estadoFiltro)) return false;
            }
            if (!texto.isEmpty()) {
                String q = texto.toLowerCase();
                boolean m = false;
                if (at.getDescripcion()  != null && at.getDescripcion().toLowerCase().contains(q))  m = true;
                if (!m && at.getObservacion() != null && at.getObservacion().toLowerCase().contains(q)) m = true;
                if (!m && at.getUnidad() != null && at.getUnidad().getNombre() != null
                        && at.getUnidad().getNombre().toLowerCase().contains(q)) m = true;
                if (!m && at.getTipoarma() != null && at.getTipoarma().getNombre() != null
                        && at.getTipoarma().getNombre().toLowerCase().contains(q)) m = true;
                if (!m && at.getMediotransporte() != null && at.getMediotransporte().getNombre() != null
                        && at.getMediotransporte().getNombre().toLowerCase().contains(q)) m = true;
                if (!m && at.getAlerta() != null) {
                    Alerta a = at.getAlerta();
                    if (a.getTipoalerta() != null && a.getTipoalerta().getNombre() != null
                            && a.getTipoalerta().getNombre().toLowerCase().contains(q)) m = true;
                    if (!m && a.getBarrio() != null && a.getBarrio().getNombre() != null
                            && a.getBarrio().getNombre().toLowerCase().contains(q)) m = true;
                }
                if (!m) return false;
            }
            return true;
        }).sorted(Comparator.comparing(
                at -> at.getFechaatencion() != null ? at.getFechaatencion() : LocalDateTime.MIN,
                Comparator.reverseOrder()))
        .collect(Collectors.toList());

        if (filtradas.isEmpty()) {
            VBox vacio = new VBox(12);
            vacio.setAlignment(Pos.CENTER);
            vacio.setPadding(new Insets(50));
            vacio.setStyle("-fx-background-color: white; -fx-background-radius: 18;");
            shadow(vacio);
            Label ico = new Label("\uf1da");
            ico.setStyle("-fx-font-family: 'Font Awesome 6 Free Solid'; -fx-font-size: 40px; -fx-text-fill: " + GRAY_TEXT + ";");
            Label m1 = new Label("Sin registros");
            m1.setFont(Font.font("System", FontWeight.BOLD, 15));
            m1.setTextFill(Color.web(GRAY_TEXT));
            Label m2 = new Label("No se encontraron atenciones con ese filtro");
            m2.setFont(Font.font("System", 12));
            m2.setTextFill(Color.web(GRAY_TEXT));
            vacio.getChildren().addAll(ico, m1, m2);
            container.getChildren().add(vacio);
            return;
        }

        // Encabezado
        HBox secH = new HBox(8);
        secH.setAlignment(Pos.CENTER_LEFT);
        secH.setPadding(new Insets(0, 0, 10, 0));
        Label sFA = new Label("\uf1da");
        sFA.setStyle("-fx-font-family: 'Font Awesome 6 Free Solid'; -fx-font-size: 14px; -fx-text-fill: " + BLUE + ";");
        Label sTxt = new Label("Historial de atenciones");
        sTxt.setFont(Font.font("System", FontWeight.BOLD, 15));
        sTxt.setTextFill(Color.web("#111827"));
        Label sCount = new Label("(" + filtradas.size() + " registro" + (filtradas.size() != 1 ? "s" : "") + ")");
        sCount.setFont(Font.font("System", 12));
        sCount.setTextFill(Color.web(GRAY_TEXT));
        secH.getChildren().addAll(sFA, sTxt, sCount);
        container.getChildren().add(secH);

        for (AtencionAlerta at : filtradas) {
            container.getChildren().add(buildCard(at));
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // TARJETA DE ATENCIÓN
    // ═══════════════════════════════════════════════════════════════
    private VBox buildCard(AtencionAlerta at) {
        String[] est = badgeEstado(at.getEstado());
        String accentColor = est[2];
        String bgLight     = est[1];

        VBox card = new VBox(0);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 18;");
        shadow(card);

        // ── Cabecera ───────────────────────────────────────────────
        HBox cardHeader = new HBox(14);
        cardHeader.setAlignment(Pos.CENTER_LEFT);
        cardHeader.setPadding(new Insets(16, 20, 14, 20));
        cardHeader.setStyle("-fx-background-color: " + bgLight + ";"
                + "-fx-background-radius: 18 18 0 0;"
                + "-fx-border-color: transparent transparent " + BORDER + " transparent;"
                + "-fx-border-width: 0 0 1 0;");

        // ID con barra de color
        HBox idBadge = new HBox(0);
        idBadge.setAlignment(Pos.CENTER_LEFT);
        idBadge.setPrefWidth(52); idBadge.setMinWidth(52); idBadge.setMaxWidth(52);

        Region colorBar = new Region();
        colorBar.setPrefWidth(4); colorBar.setMinWidth(4);
        colorBar.setPrefHeight(44); colorBar.setMinHeight(44);
        colorBar.setStyle("-fx-background-color: " + accentColor + "; -fx-background-radius: 4;");

        VBox idTextBox = new VBox(0);
        idTextBox.setAlignment(Pos.CENTER_LEFT);
        idTextBox.setPadding(new Insets(0, 0, 0, 8));
        Label idNumLbl = new Label(String.valueOf(at.getId_atencion()));
        idNumLbl.setFont(Font.font("System", FontWeight.BOLD, 26));
        idNumLbl.setTextFill(Color.web(accentColor));
        Label idTagLbl = new Label("ID");
        idTagLbl.setFont(Font.font("System", FontWeight.BOLD, 9));
        idTagLbl.setTextFill(Color.web(accentColor + "99"));
        idTextBox.getChildren().addAll(idNumLbl, idTagLbl);
        idBadge.getChildren().addAll(colorBar, idTextBox);

        // Info central
        VBox headerInfo = new VBox(4);
        HBox.setHgrow(headerInfo, Priority.ALWAYS);

        // Tipo de alerta + barrio
        String tipoAlerta = "Atención";
        String barrioStr  = "";
        if (at.getAlerta() != null) {
            if (at.getAlerta().getTipoalerta() != null)
                tipoAlerta = at.getAlerta().getTipoalerta().getNombre();
            if (at.getAlerta().getBarrio() != null)
                barrioStr = " · " + at.getAlerta().getBarrio().getNombre();
        }
        Label tipoLbl = new Label(tipoAlerta + barrioStr);
        tipoLbl.setFont(Font.font("System", FontWeight.BOLD, 14));
        tipoLbl.setTextFill(Color.web("#111827"));

        // Chips unidad + registrado por
        HBox subRow = new HBox(10);
        subRow.setAlignment(Pos.CENTER_LEFT);
        String unidadNom = at.getUnidad() != null ? at.getUnidad().getNombre() : "Sin unidad";
        subRow.getChildren().add(miniChip("\uf505", unidadNom, "#e3f2fd", BLUE));
        String regPor = extraerRegistradoPor(at);
        subRow.getChildren().add(miniChip("\uf007", "Por: " + regPor, "#f0fdf4", GREEN));
        headerInfo.getChildren().addAll(tipoLbl, subRow);

        // Derecha: estado + fecha
        VBox rightSide = new VBox(6);
        rightSide.setAlignment(Pos.CENTER_RIGHT);

        HBox badgeBox = new HBox(5);
        badgeBox.setAlignment(Pos.CENTER);
        badgeBox.setPadding(new Insets(4, 12, 4, 10));
        badgeBox.setStyle("-fx-background-color: " + bgLight + "; -fx-background-radius: 20;"
                + "-fx-border-color: " + accentColor + "55; -fx-border-radius: 20; -fx-border-width: 1;");
        Label bFA  = new Label(est[0]);
        bFA.setStyle("-fx-font-family: 'Font Awesome 6 Free Solid'; -fx-font-size: 11px; -fx-text-fill: " + accentColor + ";");
        Label bTxt = new Label(at.getEstado() != null ? at.getEstado().name().replace("_", " ") : "—");
        bTxt.setFont(Font.font("System", FontWeight.BOLD, 11));
        bTxt.setTextFill(Color.web(accentColor));
        badgeBox.getChildren().addAll(bFA, bTxt);

        HBox fechaRow = new HBox(5);
        fechaRow.setAlignment(Pos.CENTER_RIGHT);
        Label calFA = new Label("\uf073");
        calFA.setStyle("-fx-font-family: 'Font Awesome 6 Free Solid'; -fx-font-size: 10px; -fx-text-fill: " + GRAY_TEXT + ";");
        Label fTxt = new Label(formatFecha(at.getFechaatencion()));
        fTxt.setFont(Font.font("System", 11));
        fTxt.setTextFill(Color.web(GRAY_TEXT));
        fechaRow.getChildren().addAll(calFA, fTxt);

        rightSide.getChildren().addAll(badgeBox, fechaRow);
        cardHeader.getChildren().addAll(idBadge, headerInfo, rightSide);

        // ── Cuerpo ─────────────────────────────────────────────────
        VBox cardBody = new VBox(14);
        cardBody.setPadding(new Insets(16, 20, 18, 20));

        cardBody.getChildren().add(infoBlock("\uf15c", "Situación registrada", at.getDescripcion()));

        if (at.getObservacion() != null && !at.getObservacion().isBlank()) {
            cardBody.getChildren().add(infoBlock("\uf249", "Observación", at.getObservacion()));
        }

        // Chips de detalles
        FlowPane chips = new FlowPane();
        chips.setHgap(8); chips.setVgap(8);
        chips.setAlignment(Pos.CENTER_LEFT);
        boolean hayChips = false;
        if (at.getTipoarma() != null) {
            chips.getChildren().add(chipFA("\uf6ff", at.getTipoarma().getNombre(), "#fef2f2", RED));
            hayChips = true;
        }
        if (at.getMediotransporte() != null) {
            chips.getChildren().add(chipFA("\uf1b9", at.getMediotransporte().getNombre(), "#e3f2fd", BLUE));
            hayChips = true;
        }
        if (at.getAlerta() != null) {
            if (at.getAlerta().getBarrio() != null) {
                chips.getChildren().add(chipFA("\uf3c5", at.getAlerta().getBarrio().getNombre(), "#e8f5e9", GREEN));
                hayChips = true;
            }
            if (at.getAlerta().getDireccion() != null) {
                String dir = formatDireccion(at.getAlerta().getDireccion());
                if (!dir.equals("—")) {
                    chips.getChildren().add(chipFA("\uf5a0", dir, "#f5f3ff", "#7c3aed"));
                    hayChips = true;
                }
            }
        }
        if (hayChips) {
            Region sep = new Region();
            sep.setPrefHeight(1);
            sep.setStyle("-fx-background-color: " + BORDER + ";");
            cardBody.getChildren().addAll(sep, chips);
        }

        card.getChildren().addAll(cardHeader, cardBody);
        return card;
    }

    // ═══════════════════════════════════════════════════════════════
    // BADGES
    // ═══════════════════════════════════════════════════════════════
    private String[] badgeEstado(EstadoAtencionAlerta estado) {
        if (estado == null) return new String[]{"\uf128", "#f3f4f6", GRAY_TEXT};
        return switch (estado) {
            case FINALIZADA -> new String[]{"\uf058", "#e8f5e9",  GREEN};
            case CANCELADA  -> new String[]{"\uf057", "#f3f4f6",  GRAY_TEXT};
            default         -> new String[]{"\uf128", "#f3f4f6",  GRAY_TEXT};
        };
    }

    // ═══════════════════════════════════════════════════════════════
    // HELPERS UI
    // ═══════════════════════════════════════════════════════════════
    private VBox infoBlock(String iconFA, String label, String value) {
        HBox labelRow = new HBox(6);
        labelRow.setAlignment(Pos.CENTER_LEFT);
        Label ico = new Label(iconFA);
        ico.setStyle("-fx-font-family: 'Font Awesome 6 Free Solid'; -fx-font-size: 11px; -fx-text-fill: " + GRAY_TEXT + ";");
        Label lbl = new Label(label);
        lbl.setFont(Font.font("System", FontWeight.BOLD, 11));
        lbl.setTextFill(Color.web(GRAY_TEXT));
        labelRow.getChildren().addAll(ico, lbl);
        Label val = new Label(value != null && !value.isBlank() ? value : "—");
        val.setFont(Font.font("System", 13));
        val.setTextFill(Color.web("#111827"));
        val.setWrapText(true);
        return new VBox(4, labelRow, val);
    }

    private HBox chipFA(String iconFA, String texto, String bg, String fg) {
        HBox chip = new HBox(6);
        chip.setAlignment(Pos.CENTER_LEFT);
        chip.setPadding(new Insets(5, 11, 5, 11));
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

    private HBox miniChip(String iconFA, String texto, String bg, String fg) {
        HBox chip = new HBox(5);
        chip.setAlignment(Pos.CENTER_LEFT);
        chip.setPadding(new Insets(3, 9, 3, 9));
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

    private void shadow(Region node) {
        node.setEffect(new DropShadow(12, 0, 2, Color.web("#0000001a")));
    }

    // ═══════════════════════════════════════════════════════════════
    // DATOS  — solo FINALIZADA y CANCELADA
    // ═══════════════════════════════════════════════════════════════
    private List<AtencionAlerta> cargarHistorial() {
        if (atencionService == null) return List.of();
        try {
            List<AtencionAlerta> todas = atencionService.listar();

            // Filtrar por unidad del policía
            List<AtencionAlerta> deUnidad;
            if (policiaActual != null && policiaActual.getUnidadpolicial() != null) {
                String miUnidad = policiaActual.getUnidadpolicial().getNombre();
                List<AtencionAlerta> mias = todas.stream()
                        .filter(at -> at.getUnidad() != null
                                && miUnidad.equalsIgnoreCase(at.getUnidad().getNombre()))
                        .collect(Collectors.toList());
                deUnidad = mias.isEmpty() ? todas : mias;
            } else {
                deUnidad = todas;
            }

            // Solo historial cerrado
            return deUnidad.stream()
                    .filter(at -> at.getEstado() == EstadoAtencionAlerta.FINALIZADA
                               || at.getEstado() == EstadoAtencionAlerta.CANCELADA)
                    .collect(Collectors.toList());
        } catch (Exception e) { return List.of(); }
    }

    // ═══════════════════════════════════════════════════════════════
    // HELPERS DE DATOS
    // ═══════════════════════════════════════════════════════════════
    private String extraerRegistradoPor(AtencionAlerta at) {
        if (at.getObservacion() != null) {
            String obs = at.getObservacion();
            int idx = obs.indexOf("Registrado por:");
            if (idx >= 0) {
                String raw = obs.substring(idx + "Registrado por:".length()).trim();
                int end = raw.indexOf('\n');
                if (end < 0) end = raw.indexOf(',');
                return end > 0 ? raw.substring(0, end).trim() : raw.trim();
            }
        }
        if (policiaActual != null && policiaActual.getPrimer_nombre() != null) return policiaActual.getPrimer_nombre();
        if (usuarioActual != null && usuarioActual.getUsername() != null) return usuarioActual.getUsername();
        return "Oficial";
    }

    private String formatFecha(LocalDateTime dt) {
        if (dt == null) return "—";
        return dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    private String formatDireccion(Direccion d) {
        if (d == null) return "—";
        StringBuilder sb = new StringBuilder();
        if (d.getCalle()   != null && !d.getCalle().isBlank())   sb.append("Calle ").append(d.getCalle()).append(" ");
        if (d.getCarrera() != null && !d.getCarrera().isBlank()) sb.append("# ").append(d.getCarrera());
        if (d.getCasa()    != null && !d.getCasa().isBlank())    sb.append(" Casa ").append(d.getCasa());
        return sb.isEmpty() ? "—" : sb.toString().trim();
    }
}