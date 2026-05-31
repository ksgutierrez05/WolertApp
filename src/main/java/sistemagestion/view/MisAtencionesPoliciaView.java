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

public class MisAtencionesPoliciaView {

    private static final String BG        = "#f4f6fb";
    private static final String BLUE      = "#1565c0";
    private static final String GREEN     = "#43a047";
    private static final String RED       = "#e53935";
    private static final String RED_LIGHT = "#fff0f0";
    private static final String ORANGE    = "#fb8c00";
    private static final String GRAY_TEXT = "#6b7280";
    private static final String BORDER    = "#e5e7eb";

    private final Usuario               usuarioActual;
    private final Policia               policiaActual;
    private final AtencionAlertaService atencionService;
    private final BorderPane            root;

    public MisAtencionesPoliciaView(Usuario usuarioActual, Policia policiaActual,
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

        List<AtencionAlerta> lista = cargarAtenciones();

        VBox timelineContainer = new VBox(0);
        renderTimeline(timelineContainer, lista, null, "");

        content.getChildren().addAll(
                buildTopBar(),
                buildStatsRow(lista),
                buildFiltros(lista, timelineContainer),
                timelineContainer
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
        Label title = new Label("Historial de Atenciones");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#111827"));
        Label sub = new Label("Registro completo de atenciones realizadas por tu unidad");
        sub.setFont(Font.font("System", 13));
        sub.setTextFill(Color.web(GRAY_TEXT));
        titles.getChildren().addAll(title, sub);

        if (policiaActual != null && policiaActual.getUnidadpolicial() != null) {
            HBox chip = new HBox(10);
            chip.setAlignment(Pos.CENTER);
            chip.setPadding(new Insets(10, 18, 10, 14));
            chip.setStyle("-fx-background-color: white; -fx-background-radius: 14;"
                    + "-fx-border-color: " + BORDER + "; -fx-border-radius: 14; -fx-border-width: 1;");
            shadow(chip);

            // Avatar con iniciales
            StackPane av = new StackPane();
            av.setPrefSize(38, 38); av.setMinSize(38, 38); av.setMaxSize(38, 38);
            Circle avCircle = new Circle(19, Color.web("#dbeafe"));
            Label avFA = new Label("\uf505");
            avFA.setStyle("-fx-font-family: 'Font Awesome 6 Free Solid'; -fx-font-size: 15px; -fx-text-fill: " + BLUE + ";");
            av.getChildren().addAll(avCircle, avFA);

            VBox info = new VBox(2);
            String nombre = policiaActual.getPrimer_nombre() != null
                    ? policiaActual.getPrimer_nombre() : (usuarioActual != null ? usuarioActual.getUsername() : "Oficial");
            Label nombreLbl = new Label(nombre);
            nombreLbl.setFont(Font.font("System", FontWeight.BOLD, 13));
            nombreLbl.setTextFill(Color.web("#111827"));
            Label unidadLbl = new Label(policiaActual.getUnidadpolicial().getNombre());
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
    // STATS ROW
    // ═══════════════════════════════════════════════════════════════
    private HBox buildStatsRow(List<AtencionAlerta> lista) {
        long pendientes  = lista.stream().filter(a -> a.getEstado() == EstadoAtencionAlerta.PENDIENTE).count();
        long enProceso   = lista.stream().filter(a -> a.getEstado() == EstadoAtencionAlerta.EN_PROCESO).count();
        long finalizadas = lista.stream().filter(a -> a.getEstado() == EstadoAtencionAlerta.FINALIZADA).count();
        long total       = lista.size();

        HBox row = new HBox(16);
        HBox.setHgrow(row, Priority.ALWAYS);
        row.getChildren().addAll(
                statCard(RED_LIGHT,  RED,    "\uf017", "Pendientes",  boldNum(String.valueOf(pendientes),  RED),    "Sin iniciar"),
                statCard("#fff8e1",  ORANGE, "\uf0e7", "En proceso",  boldNum(String.valueOf(enProceso),   ORANGE), "En ejecución"),
                statCard("#e8f5e9",  GREEN,  "\uf058", "Finalizadas", boldNum(String.valueOf(finalizadas), GREEN),  "Completadas"),
                statCard("#e3f2fd",  BLUE,   "\uf46d", "Total",       boldNum(String.valueOf(total),       BLUE),   "Atenciones registradas")
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
    private VBox buildFiltros(List<AtencionAlerta> todas, VBox timelineContainer) {
        VBox panel = new VBox(12);
        panel.setPadding(new Insets(16, 20, 16, 20));
        panel.setStyle("-fx-background-color: white; -fx-background-radius: 16;");
        shadow(panel);

        HBox titulo = new HBox(8);
        titulo.setAlignment(Pos.CENTER_LEFT);
        Label filtroFA = new Label("\uf0b0");
        filtroFA.setStyle("-fx-font-family: 'Font Awesome 6 Free Solid'; -fx-font-size: 12px; -fx-text-fill: " + BLUE + ";");
        Label filtroTxt = new Label("Filtrar atenciones");
        filtroTxt.setFont(Font.font("System", FontWeight.BOLD, 13));
        filtroTxt.setTextFill(Color.web(BLUE));
        titulo.getChildren().addAll(filtroFA, filtroTxt);

        // ── Campo de búsqueda ─────────────────────────────────────
        TextField busqueda = new TextField();
        busqueda.setPromptText("\uf002  Buscar por descripción, barrio, tipo, observación...");
        busqueda.setPrefWidth(280);
        busqueda.setPrefHeight(38);
        String bBase = "-fx-background-color: #f8fafc; -fx-background-radius: 10;"
                + "-fx-border-color: " + BORDER + "; -fx-border-radius: 10; -fx-border-width: 1.5;"
                + "-fx-font-size: 13px; -fx-text-fill: #1E293B; -fx-prompt-text-fill: #94A3B8;"
                + "-fx-padding: 0 14; -fx-focus-color: transparent; -fx-faint-focus-color: transparent;";
        busqueda.setStyle(bBase);
        busqueda.focusedProperty().addListener((o, ov, f) ->
                busqueda.setStyle(f ? bBase.replace(BORDER, "#93c5fd") : bBase));

        // ── Pills de estado ───────────────────────────────────────
        record Pill(String label, String estado, String icono, String color, String bg) {}
        List<Pill> pills = List.of(
            new Pill("Todos",       null,          "\uf0c9", BLUE,      "#e3f2fd"),
            new Pill("Pendientes",  "PENDIENTE",   "\uf017", RED,       RED_LIGHT),
            new Pill("En proceso",  "EN_PROCESO",  "\uf0e7", ORANGE,    "#fff8e1"),
            new Pill("Finalizadas", "FINALIZADA",  "\uf058", GREEN,     "#e8f5e9"),
            new Pill("Canceladas",  "CANCELADA",   "\uf057", GRAY_TEXT, "#f3f4f6")
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
                renderTimeline(timelineContainer, todas, estadoActivo[0], busqueda.getText().trim());
            });
            pillRow.getChildren().add(btn);
        }

        busqueda.textProperty().addListener((o, ov, nv) ->
                renderTimeline(timelineContainer, todas, estadoActivo[0], nv.trim()));

        // Botón limpiar
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
                    0,0,0,0,javafx.scene.input.MouseButton.PRIMARY,1,false,false,false,false,false,false,false,false,false,false,null)); });

        HBox spacer = new HBox(); HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox controles = new HBox(12);
        controles.setAlignment(Pos.CENTER_LEFT);
        controles.getChildren().addAll(busqueda, pillRow, spacer, limpiar);
        panel.getChildren().addAll(titulo, controles);
        return panel;
    }

    // ═══════════════════════════════════════════════════════════════
    // RENDER DINÁMICO DEL TIMELINE
    // ═══════════════════════════════════════════════════════════════
    private void renderTimeline(VBox container, List<AtencionAlerta> todas,
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
                if (!m && at.getUnidad()      != null && at.getUnidad().getNombre() != null
                        && at.getUnidad().getNombre().toLowerCase().contains(q)) m = true;
                if (!m && at.getTipoarma()    != null && at.getTipoarma().getNombre() != null
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
            Label ico = new Label("\uf0b0");
            ico.setStyle("-fx-font-family: 'Font Awesome 6 Free Solid'; -fx-font-size: 40px; -fx-text-fill: " + GRAY_TEXT + ";");
            Label m1 = new Label("Sin resultados");
            m1.setFont(Font.font("System", FontWeight.BOLD, 15));
            m1.setTextFill(Color.web(GRAY_TEXT));
            Label m2 = new Label("Prueba con otro filtro o limpia la búsqueda");
            m2.setFont(Font.font("System", 12));
            m2.setTextFill(Color.web(GRAY_TEXT));
            vacio.getChildren().addAll(ico, m1, m2);
            container.getChildren().add(vacio);
            return;
        }

        // Encabezado del timeline
        HBox secHeader = new HBox(8);
        secHeader.setAlignment(Pos.CENTER_LEFT);
        secHeader.setPadding(new Insets(0, 0, 12, 0));
        Label secFA = new Label("\uf1da");
        secFA.setStyle("-fx-font-family: 'Font Awesome 6 Free Solid'; -fx-font-size: 14px; -fx-text-fill: " + BLUE + ";");
        Label secTxt = new Label("Historial cronológico");
        secTxt.setFont(Font.font("System", FontWeight.BOLD, 15));
        secTxt.setTextFill(Color.web("#111827"));
        Label countLbl = new Label("(" + filtradas.size() + " registro" + (filtradas.size() != 1 ? "s" : "") + ")");
        countLbl.setFont(Font.font("System", 12));
        countLbl.setTextFill(Color.web(GRAY_TEXT));
        secHeader.getChildren().addAll(secFA, secTxt, countLbl);
        container.getChildren().add(secHeader);

        for (int i = 0; i < filtradas.size(); i++) {
            container.getChildren().add(buildTimelineItem(filtradas.get(i), i, filtradas.size()));
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // ITEM DEL TIMELINE  (sin conector lateral)
    // ═══════════════════════════════════════════════════════════════
    private HBox buildTimelineItem(AtencionAlerta at, int index, int total) {
        String[] est = badgeEstado(at.getEstado());
        String accentColor = est[2];
        String bgLight     = est[1];

        HBox itemRow = new HBox(0);
        itemRow.setAlignment(Pos.TOP_LEFT);
        itemRow.setPadding(new Insets(0, 0, index < total - 1 ? 16 : 0, 0));

        // ── Tarjeta (sin columna lateral) ─────────────────────────
        VBox cardWrapper = new VBox(0);
        HBox.setHgrow(cardWrapper, Priority.ALWAYS);

        VBox card = new VBox(0);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 18;");
        shadow(card);

        // ── Cabecera de la tarjeta ─────────────────────────────────
        HBox cardHeader = new HBox(14);
        cardHeader.setAlignment(Pos.CENTER_LEFT);
        cardHeader.setPadding(new Insets(16, 20, 14, 20));
        cardHeader.setStyle("-fx-background-color: " + bgLight + ";"
                + "-fx-background-radius: 18 18 0 0;"
                + "-fx-border-color: transparent transparent " + BORDER + " transparent;"
                + "-fx-border-width: 0 0 1 0;");

        // ── ID: número grande con barra de color izquierda ─────────
        HBox idBadge = new HBox(0);
        idBadge.setAlignment(Pos.CENTER);
        idBadge.setPrefWidth(52); idBadge.setMinWidth(52); idBadge.setMaxWidth(52);
        idBadge.setPadding(new Insets(6, 10, 6, 0));

        // Barra vertical de color
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

        // Info central: tipo alerta + barrio
        VBox headerInfo = new VBox(3);
        HBox.setHgrow(headerInfo, Priority.ALWAYS);

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

        // Sub-fila: quién la registró + unidad
        HBox registradoPor = new HBox(12);
        registradoPor.setAlignment(Pos.CENTER_LEFT);

        String unidadNom = at.getUnidad() != null ? at.getUnidad().getNombre() : "Sin unidad";
        HBox unidadChip = miniChip("\uf505", unidadNom, "#e3f2fd", BLUE);

        // Registrado por: buscar en observación o usar usuarioActual como fallback
        String registradoNom = extraerRegistradoPor(at);
        HBox registradoChip  = miniChip("\uf007", "Por: " + registradoNom, "#f0fdf4", GREEN);

        registradoPor.getChildren().addAll(unidadChip, registradoChip);
        headerInfo.getChildren().addAll(tipoLbl, registradoPor);

        // Lado derecho: estado + fecha
        VBox rightSide = new VBox(6);
        rightSide.setAlignment(Pos.CENTER_RIGHT);

        HBox badgeBox = new HBox(5);
        badgeBox.setAlignment(Pos.CENTER);
        badgeBox.setPadding(new Insets(4, 12, 4, 10));
        badgeBox.setStyle("-fx-background-color: " + bgLight + "; -fx-background-radius: 20;"
                + "-fx-border-color: " + accentColor + "55; -fx-border-radius: 20; -fx-border-width: 1;");
        Label badgeFA  = new Label(est[0]);
        badgeFA.setStyle("-fx-font-family: 'Font Awesome 6 Free Solid'; -fx-font-size: 11px; -fx-text-fill: " + accentColor + ";");
        Label badgeTxt = new Label(at.getEstado() != null ? at.getEstado().name().replace("_", " ") : "—");
        badgeTxt.setFont(Font.font("System", FontWeight.BOLD, 11));
        badgeTxt.setTextFill(Color.web(accentColor));
        badgeBox.getChildren().addAll(badgeFA, badgeTxt);

        HBox fechaRow = new HBox(5);
        fechaRow.setAlignment(Pos.CENTER_RIGHT);
        Label calFA   = new Label("\uf073");
        calFA.setStyle("-fx-font-family: 'Font Awesome 6 Free Solid'; -fx-font-size: 10px; -fx-text-fill: " + GRAY_TEXT + ";");
        Label fechaTxt = new Label(formatFecha(at.getFechaatencion()));
        fechaTxt.setFont(Font.font("System", 11));
        fechaTxt.setTextFill(Color.web(GRAY_TEXT));
        fechaRow.getChildren().addAll(calFA, fechaTxt);

        rightSide.getChildren().addAll(badgeBox, fechaRow);
        cardHeader.getChildren().addAll(idBadge, headerInfo, rightSide);

        // ── Cuerpo ─────────────────────────────────────────────────
        VBox cardBody = new VBox(14);
        cardBody.setPadding(new Insets(16, 18, 18, 18));

        // Situación registrada
        cardBody.getChildren().add(infoBlock("\uf15c", "Situación registrada", at.getDescripcion()));

        // Observación
        if (at.getObservacion() != null && !at.getObservacion().isBlank()) {
            cardBody.getChildren().add(infoBlock("\uf249", "Observación", at.getObservacion()));
        }

        // Separador visual antes de chips
        Region sep = new Region();
        sep.setPrefHeight(1);
        sep.setStyle("-fx-background-color: " + BORDER + ";");

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
            if (at.getAlerta().getTipoalerta() != null) {
                chips.getChildren().add(chipFA("\uf0f3", at.getAlerta().getTipoalerta().getNombre(), RED_LIGHT, RED));
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
            cardBody.getChildren().addAll(sep, chips);
        }

        card.getChildren().addAll(cardHeader, cardBody);
        cardWrapper.getChildren().add(card);
        itemRow.getChildren().add(cardWrapper);
        return itemRow;
    }

    // ═══════════════════════════════════════════════════════════════
    // HELPER: extraer quién registró la atención
    // ═══════════════════════════════════════════════════════════════
    /**
     * Intenta extraer el nombre del registrador desde el campo observación
     * (patrón "Registrado por: X"). Si no, usa el nombre del policía actual.
     */
    private String extraerRegistradoPor(AtencionAlerta at) {
        if (at.getObservacion() != null) {
            String obs = at.getObservacion();
            int idx = obs.indexOf("Registrado por:");
            if (idx >= 0) {
                String raw = obs.substring(idx + "Registrado por:".length()).trim();
                // Tomar hasta el primer salto de línea o coma
                int end = raw.indexOf('\n');
                if (end < 0) end = raw.indexOf(',');
                return end > 0 ? raw.substring(0, end).trim() : raw.trim();
            }
        }
        if (policiaActual != null && policiaActual.getPrimer_nombre() != null)
            return policiaActual.getPrimer_nombre();
        if (usuarioActual != null && usuarioActual.getUsername() != null)
            return usuarioActual.getUsername();
        return "Oficial";
    }

    // ═══════════════════════════════════════════════════════════════
    // BADGES
    // ═══════════════════════════════════════════════════════════════
    private String[] badgeEstado(EstadoAtencionAlerta estado) {
        if (estado == null) return new String[]{"\uf128", "#f3f4f6", GRAY_TEXT};
        return switch (estado) {
            case PENDIENTE  -> new String[]{"\uf017", RED_LIGHT,  RED};
            case EN_PROCESO -> new String[]{"\uf0e7", "#fff8e1",  ORANGE};
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
    // DATOS
    // ═══════════════════════════════════════════════════════════════
    private List<AtencionAlerta> cargarAtenciones() {
        if (atencionService == null) return List.of();
        try {
            List<AtencionAlerta> todas = atencionService.listar();
            if (policiaActual == null || policiaActual.getUnidadpolicial() == null) return todas;
            String miUnidad = policiaActual.getUnidadpolicial().getNombre();
            List<AtencionAlerta> mias = todas.stream()
                    .filter(at -> at.getUnidad() != null
                            && miUnidad.equalsIgnoreCase(at.getUnidad().getNombre()))
                    .collect(Collectors.toList());
            return mias.isEmpty() ? todas : mias;
        } catch (Exception e) { return List.of(); }
    }

    // ═══════════════════════════════════════════════════════════════
    // FORMATEO
    // ═══════════════════════════════════════════════════════════════
    private String formatFecha(LocalDateTime dt) {
        if (dt == null) return "—";
        long mins = java.time.Duration.between(dt, LocalDateTime.now()).toMinutes();
        if (mins < 1)    return "Hace un momento";
        if (mins < 60)   return "Hace " + mins + " min";
        if (mins < 1440) return "Hace " + (mins / 60) + " h";
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