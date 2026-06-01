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
import java.util.stream.Collectors;
import sistemagestion.model.*;
import sistemagestion.service.*;

public class HistorialPoliciaView {

    private static final String BG = "#f4f6fb";
    private static final String BLUE = "#1565c0";
    private static final String BLUE_LIGHT = "#e8f0fe";
    private static final String GREEN = "#43a047";
    private static final String GREEN_LIGHT = "#e8f5e9";
    private static final String GRAY_TEXT = "#6b7280";
    private static final String BORDER = "#e5e7eb";
    private static final String WHITE = "#ffffff";

    private final Usuario usuarioActual;
    private final Policia policiaActual;
    private final PoliciaService policiaService;
    private final BorderPane root;

    public HistorialPoliciaView(Usuario usuarioActual, Policia policiaActual,
            PoliciaService policiaService, BorderPane root) {
        javafx.scene.text.Font.loadFont(
                getClass().getResourceAsStream("/fa-solid-900.ttf"), 20);
        this.usuarioActual = usuarioActual;
        this.policiaActual = policiaActual;
        this.policiaService = policiaService;
        this.root = root;
    }

    public ScrollPane build() {
        VBox content = new VBox(24);
        content.setPadding(new Insets(28, 22, 22, 22));
        content.setStyle("-fx-background-color: " + BG + ";");

        List<Policia> companeros = cargarCompaneros();

        content.getChildren().addAll(
                buildTopBar(),
                buildStatsRow(companeros),
                buildGrid(companeros)
        );

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: " + BG + "; -fx-background: " + BG + ";"
                + "-fx-border-color: transparent;");
        return scroll;
    }

    // ── Top bar ──────────────────────────────────────────────────
    private HBox buildTopBar() {
        HBox bar = new HBox(16);
        bar.setAlignment(Pos.CENTER_LEFT);

        VBox titles = new VBox(5);
        HBox.setHgrow(titles, Priority.ALWAYS);

        Label title = new Label("Mi Unidad");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#111827"));

        String unidadNom = policiaActual != null && policiaActual.getUnidadpolicial() != null
                ? policiaActual.getUnidadpolicial().getNombre()
                : "tu unidad";
        Label sub = new Label("Compañeros asignados a " + unidadNom);
        sub.setFont(Font.font("System", 13));
        sub.setTextFill(Color.web(GRAY_TEXT));

        titles.getChildren().addAll(title, sub);
        bar.getChildren().add(titles);
        return bar;
    }

    // ── Stats ────────────────────────────────────────────────────
    private HBox buildStatsRow(List<Policia> lista) {
        long total = lista.size();
        long enServicio = lista.stream()
                .filter(p -> p.getEstado() != null
                && p.getEstado().name().equalsIgnoreCase("EN_SERVICIO"))
                .count();
        long oficiales = lista.stream()
                .filter(p -> p.getRango() != null
                && (p.getRango().equalsIgnoreCase("Oficial")
                || p.getRango().equalsIgnoreCase("Teniente")
                || p.getRango().equalsIgnoreCase("Capitán")))
                .count();

        HBox row = new HBox(16);
        HBox.setHgrow(row, Priority.ALWAYS);
        row.getChildren().addAll(
                statCard(BLUE_LIGHT, BLUE, "\uf0c0", "Total de compañeros", total, "En tu unidad"),
                statCard(GREEN_LIGHT, GREEN, "\uf058", "En servicio", enServicio, "Activos ahora"),
                statCard("#fff8e1", "#b45309", "\uf505", "Oficiales / Mandos", oficiales, "Rangos superiores")
        );
        return row;
    }

    // ── Grid de tarjetas ─────────────────────────────────────────
    private VBox buildGrid(List<Policia> lista) {
        VBox wrapper = new VBox(12);

        // Encabezado de sección
        HBox secH = new HBox(8);
        secH.setAlignment(Pos.CENTER_LEFT);
        Label secFA = new Label("\uf0c0");
        secFA.setStyle("-fx-font-family: 'Font Awesome 6 Free Solid'; -fx-font-size: 14px; -fx-text-fill: " + BLUE + ";");
        Label secTxt = new Label("Integrantes de la unidad");
        secTxt.setFont(Font.font("System", FontWeight.BOLD, 15));
        secTxt.setTextFill(Color.web("#111827"));
        Label secCount = new Label("(" + lista.size() + " " + (lista.size() == 1 ? "policía" : "policías") + ")");
        secCount.setFont(Font.font("System", 12));
        secCount.setTextFill(Color.web(GRAY_TEXT));
        secH.getChildren().addAll(secFA, secTxt, secCount);
        wrapper.getChildren().add(secH);

        if (lista.isEmpty()) {
            VBox vacio = new VBox(12);
            vacio.setAlignment(Pos.CENTER);
            vacio.setPadding(new Insets(50));
            vacio.setStyle("-fx-background-color: white; -fx-background-radius: 18;");
            shadow(vacio);
            Label ico = new Label("\uf0c0");
            ico.setStyle("-fx-font-family: 'Font Awesome 6 Free Solid'; -fx-font-size: 40px; -fx-text-fill: " + GRAY_TEXT + "55;");
            Label m1 = new Label("Sin compañeros registrados");
            m1.setFont(Font.font("System", FontWeight.BOLD, 15));
            m1.setTextFill(Color.web(GRAY_TEXT));
            Label m2 = new Label("No se encontraron policías en tu unidad");
            m2.setFont(Font.font("System", 12));
            m2.setTextFill(Color.web(GRAY_TEXT));
            vacio.getChildren().addAll(ico, m1, m2);
            wrapper.getChildren().add(vacio);
            return wrapper;
        }

        // Grid de 3 columnas
        int cols = 3;
        List<HBox> filas = new java.util.ArrayList<>();
        HBox filaActual = null;
        for (int i = 0; i < lista.size(); i++) {
            if (i % cols == 0) {
                filaActual = new HBox(16);
                filaActual.setFillHeight(true);
                filas.add(filaActual);
            }
            VBox card = buildCard(lista.get(i));
            filaActual.getChildren().add(card);
        }
        // Rellenar última fila si queda incompleta
        if (filaActual != null) {
            int resto = lista.size() % cols;
            if (resto != 0) {
                for (int i = 0; i < cols - resto; i++) {
                    Region dummy = new Region();
                    dummy.setPrefWidth(250);
                    filaActual.getChildren().add(dummy);
                }
            }
        }
        wrapper.getChildren().addAll(filas);
        return wrapper;
    }

    // ── Tarjeta de policía ───────────────────────────────────────
    private VBox buildCard(Policia p) {
        boolean esYo = policiaActual != null
                && p.getId_policia() == policiaActual.getId_policia();

        VBox card = new VBox(0);
        card.setPrefWidth(280);
        card.setMinWidth(280);
        card.setMaxWidth(280);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 18;"
                + (esYo ? "-fx-border-color: " + BLUE + "; -fx-border-radius: 18; -fx-border-width: 2;" : ""));
        shadow(card);

        // ── Cabecera con avatar ──────────────────────────────────
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(18, 14, 14, 14));
        header.setStyle("-fx-background-color: " + BLUE_LIGHT + "; -fx-background-radius: 18 18 0 0;");

        // Avatar circular
        StackPane avatar = new StackPane();
        avatar.setPrefSize(52, 52);
        avatar.setMinSize(52, 52);
        avatar.setMaxSize(52, 52);

        Circle avatarCircle = new Circle(26, Color.web(esYo ? BLUE : "#dbeafe"));
        Label avatarIcon = new Label("\uf007");
        avatarIcon.setStyle(
                "-fx-font-family: 'Font Awesome 6 Free Solid';"
                + "-fx-font-size: 20px;"
                + "-fx-text-fill: " + (esYo ? WHITE : BLUE) + ";"
        );
        avatar.getChildren().addAll(avatarCircle, avatarIcon);

        // Badge "Tú" si es el policía actual
        if (esYo) {
            Label tuLbl = new Label("Tú");
            tuLbl.setFont(Font.font("System", FontWeight.BOLD, 10));
            tuLbl.setTextFill(Color.web(WHITE));
            tuLbl.setPadding(new Insets(2, 8, 2, 8));
            tuLbl.setStyle("-fx-background-color: " + BLUE + "; -fx-background-radius: 20;");
            header.getChildren().addAll(avatar, tuLbl);
        } else {
            header.getChildren().add(avatar);
        }

        // Nombre
        String nombre = (p.getPrimer_nombre() != null ? p.getPrimer_nombre() : "")
                + (p.getPrimer_apellido() != null ? " " + p.getPrimer_apellido() : "");
        Label nombreLbl = new Label(nombre.isBlank() ? "Sin nombre" : nombre.trim());
        nombreLbl.setFont(Font.font("System", FontWeight.BOLD, 14));
        nombreLbl.setTextFill(Color.web("#111827"));
        nombreLbl.setAlignment(Pos.CENTER);
        nombreLbl.setWrapText(false);
        nombreLbl.setMaxWidth(180);
        header.getChildren().add(nombreLbl);

        // Rango badge
        String rango = p.getRango() != null ? p.getRango() : "Policía";
        HBox rangoBadge = new HBox(5);
        rangoBadge.setAlignment(Pos.CENTER);
        rangoBadge.setPadding(new Insets(4, 12, 4, 10));
        rangoBadge.setStyle("-fx-background-color: white; -fx-background-radius: 20;"
                + "-fx-border-color: " + BLUE + "55; -fx-border-radius: 20; -fx-border-width: 1;");
        Label rangoFA = new Label("\uf505");
        rangoFA.setStyle("-fx-font-family: 'Font Awesome 6 Free Solid'; -fx-font-size: 10px; -fx-text-fill: " + BLUE + ";");
        Label rangoTxt = new Label(rango);
        rangoTxt.setFont(Font.font("System", FontWeight.BOLD, 10));
        rangoTxt.setTextFill(Color.web(BLUE));
        rangoBadge.getChildren().addAll(rangoFA, rangoTxt);
        header.getChildren().add(rangoBadge);

        // ── Cuerpo con detalles ──────────────────────────────────
        VBox body = new VBox(10);
        body.setPadding(new Insets(12, 14, 14, 14));

        // Placa
        if (p.getPlaca() != null && !p.getPlaca().isBlank()) {
            body.getChildren().add(detailRow("\uf2bb", "Placa", p.getPlaca(), GRAY_TEXT));
        }

        // Estado
        if (p.getEstado() != null) {
            String estadoStr = p.getEstado().name().replace("_", " ");
            String estadoColor = p.getEstado().name().equalsIgnoreCase("EN_SERVICIO") ? GREEN : GRAY_TEXT;
            body.getChildren().add(detailRow("\uf111", "Estado", estadoStr, estadoColor));
        }

        card.getChildren().addAll(header, body);
        return card;
    }

    // ── Fila de detalle ──────────────────────────────────────────
    private HBox detailRow(String iconFA, String label, String value, String valueColor) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(28, 28);
        iconBox.setMinSize(28, 28);
        iconBox.setMaxSize(28, 28);
        iconBox.setStyle("-fx-background-color: " + BLUE_LIGHT + "; -fx-background-radius: 7;");
        Label ico = new Label(iconFA);
        ico.setStyle("-fx-font-family: 'Font Awesome 6 Free Solid'; -fx-font-size: 11px; -fx-text-fill: " + BLUE + ";");
        iconBox.getChildren().add(ico);

        VBox text = new VBox(1);
        HBox.setHgrow(text, Priority.ALWAYS);
        Label labelLbl = new Label(label);
        labelLbl.setFont(Font.font("System", 10));
        labelLbl.setTextFill(Color.web(GRAY_TEXT));
        Label valueLbl = new Label(value);
        valueLbl.setFont(Font.font("System", FontWeight.BOLD, 12));
        valueLbl.setTextFill(Color.web(valueColor));
        text.getChildren().addAll(labelLbl, valueLbl);

        row.getChildren().addAll(iconBox, text);
        return row;
    }

    // ── Stat card ────────────────────────────────────────────────
    private VBox statCard(String bgIcon, String accentColor, String iconFA,
            String title, long value, String sub) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20, 22, 20, 22));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 18;");
        card.setPrefWidth(250);
        card.setMaxWidth(250);
        card.setMinWidth(250);

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
        iconLbl.setStyle("-fx-font-family: 'Font Awesome 6 Free Solid'; -fx-font-size: 22px; -fx-text-fill: " + accentColor + ";");
        iconWrap.getChildren().addAll(iconBg, iconLbl);

        Label titleLbl = new Label(title);
        titleLbl.setFont(Font.font("System", FontWeight.BOLD, 13));
        titleLbl.setTextFill(Color.web("#374151"));
        Label valLbl = new Label(String.valueOf(value));
        valLbl.setFont(Font.font("System", FontWeight.BOLD, 36));
        valLbl.setTextFill(Color.web(accentColor));
        Label subLbl = new Label(sub);
        subLbl.setFont(Font.font("System", 11));
        subLbl.setTextFill(Color.web(GRAY_TEXT));

        HBox top = new HBox(16, iconWrap, new VBox(3, titleLbl, valLbl, subLbl));
        top.setAlignment(Pos.CENTER_LEFT);
        card.getChildren().add(top);
        card.setOnMouseEntered(e -> card.setTranslateY(-3));
        card.setOnMouseExited(e -> card.setTranslateY(0));
        return card;
    }

    // ── Datos ────────────────────────────────────────────────────
    private List<Policia> cargarCompaneros() {
        if (policiaService == null || policiaActual == null
                || policiaActual.getUnidadpolicial() == null) {
            return List.of();
        }
        try {
            String miUnidad = policiaActual.getUnidadpolicial().getNombre();
            return policiaService.listar().stream()
                    .filter(p -> p.getUnidadpolicial() != null
                    && miUnidad.equalsIgnoreCase(p.getUnidadpolicial().getNombre()))
                    .sorted((a, b) -> {
                        // Yo primero
                        if (a.getId_policia() == policiaActual.getId_policia()) {
                            return -1;
                        }
                        if (b.getId_policia() == policiaActual.getId_policia()) {
                            return 1;
                        }
                        String na = a.getPrimer_nombre() != null ? a.getPrimer_nombre() : "";
                        String nb = b.getPrimer_nombre() != null ? b.getPrimer_nombre() : "";
                        return na.compareToIgnoreCase(nb);
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return List.of();
        }
    }

    private void shadow(Region node) {
        node.setEffect(new DropShadow(12, 0, 2, Color.web("#0000001a")));
    }
}
