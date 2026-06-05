/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.view;

/**
 *
 * @author Maria Cristina
 */


import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import sistemagestion.model.EstadoAlerta;

/**
 * Fábrica de componentes visuales JavaFX para la pantalla de alertas.
 *
 * GRASP — Creator:
 *   Crea y ensambla los nodos JavaFX (Labels, Buttons, HBox, VBox).
 *   Centraliza la construcción para que AlertasView no duplique código
 *   de presentación.
 *
 * GRASP — Pure Fabrication:
 *   No representa ningún concepto del dominio (Alerta, Barrio, etc.).
 *   Existe únicamente para mejorar la cohesión y reducir el tamaño de
 *   la clase coordinadora.
 *
 * SOLID — SRP (Single Responsibility Principle):
 *   Su única razón de cambio es una decisión de presentación visual.
 *
 * SOLID — OCP (Open/Closed Principle):
 *   Agregar un nuevo tipo de badge o tarjeta no requiere modificar
 *   AlertasView; basta con añadir un método aquí.
 */
public class AlertaUIFactory {

    // ── Tarjeta de métrica ────────────────────────────────────────
    /**
     * Construye una tarjeta de resumen numérico para el panel de métricas.
     *
     * @param bgIcon      Color de fondo del icono (hex).
     * @param accentColor Color del icono (hex).
     * @param iconFA      Carácter FontAwesome.
     * @param title       Título de la tarjeta.
     * @param valueLabel  Label ya preparado con el número principal.
     * @param sub         Subtexto descriptivo.
     * @param subColor    Color del subtexto (hex).
     */
    public VBox statCard(String bgIcon, String accentColor, String iconFA,
                         String title, Label valueLabel, String sub, String subColor) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20, 22, 20, 22));
        card.setStyle("-fx-background-color:white;-fx-background-radius:18;");
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
        iconLbl.setStyle(
                "-fx-font-family:'Font Awesome 6 Free Solid';"
                + "-fx-font-size:22px;-fx-text-fill:" + accentColor + ";");
        iconWrap.getChildren().addAll(iconBg, iconLbl);

        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#374151;");

        Label subLbl = new Label(sub);
        subLbl.setStyle("-fx-font-size:11px;-fx-text-fill:" + subColor + ";");

        VBox textBox = new VBox(3, titleLbl, valueLabel, subLbl);
        HBox top = new HBox(16);
        top.setAlignment(Pos.CENTER_LEFT);
        top.getChildren().addAll(iconWrap, textBox);
        card.getChildren().add(top);

        card.setOnMouseEntered(e -> card.setTranslateY(-3));
        card.setOnMouseExited(e -> card.setTranslateY(0));
        return card;
    }

    /**
     * Label con número grande, listo para usar en statCard.
     */
    public Label boldNum(String val, String color) {
        Label l = new Label(val);
        l.setStyle("-fx-font-size:36px;-fx-font-weight:bold;-fx-text-fill:" + color + ";");
        return l;
    }

    // ── Badge ─────────────────────────────────────────────────────
    /**
     * Pastilla con fondo y texto coloreados.
     */
    public Label badge(String texto, String bg, String color) {
        Label l = label(texto, 11, color, true);
        l.setPadding(new Insets(3, 9, 3, 9));
        l.setStyle("-fx-background-color:" + bg + ";-fx-background-radius:20;");
        return l;
    }

    // ── Botón icónico ─────────────────────────────────────────────
    /**
     * Botón con ícono FontAwesome y efecto hover invertido.
     */
    public Button btnIcono(String iconFA, String tooltip,
                            String iconColor, String bgColor, Runnable accion) {
        Button b = new Button(iconFA);
        b.setTooltip(new Tooltip(tooltip));
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
        return b;
    }

    // ── Botón secundario ─────────────────────────────────────────
    /**
     * Botón de contorno (sin color de fondo fuerte) para acciones secundarias.
     */
    public Button btnSecundario(String texto) {
        Button btn = new Button(texto);
        btn.setPrefHeight(38);
        btn.setStyle("-fx-background-color:white;-fx-text-fill:" + AppColors.TEXT_SECONDARY + ";"
                + "-fx-font-size:13px;-fx-border-color:" + AppColors.BORDER + ";"
                + "-fx-border-radius:8;-fx-background-radius:8;"
                + "-fx-padding:6 14;-fx-cursor:hand;");
        return btn;
    }

    // ── Celda de tabla ────────────────────────────────────────────
    /**
     * Label de ancho fijo con elipsis para columnas de la tabla.
     */
    public Label celda(String txt, double width) {
        Label l = label(txt != null ? txt : "—", 12, AppColors.TEXT_SECONDARY, false);
        l.setPrefWidth(width);
        l.setMaxWidth(width);
        l.setEllipsisString("…");
        return l;
    }

    /**
     * Wrapper HBox de ancho fijo para alinear badges en columnas.
     */
    public HBox celdaBox(Label lbl, double width) {
        HBox box = new HBox(lbl);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPrefWidth(width);
        return box;
    }

    // ── Cabecera de columna ───────────────────────────────────────
    /**
     * Label de encabezado de tabla con ancho fijo.
     */
    public Label colH(String text, double width) {
        Label l = label(text, 11, AppColors.GRAY_TEXT, true);
        l.setPrefWidth(width);
        return l;
    }

    // ── Fila de detalle (diálogo) ─────────────────────────────────
    /**
     * Par campo–valor para los diálogos de detalle.
     *
     * @param minWidth Ancho mínimo de la celda clave (varía por diálogo).
     */
    public HBox detRow(String campo, String valor, double minWidth) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        Label k = label(campo, 13, AppColors.GRAY_TEXT, false);
        k.setMinWidth(minWidth);
        row.getChildren().addAll(k, label(valor != null ? valor : "—", 13, AppColors.TEXT_PRIMARY, false));
        return row;
    }

    // ── Encabezado de sección (diálogo) ──────────────────────────
    /**
     * Texto pequeño en mayúsculas que actúa como separador de sección.
     */
    public Label seccionHeader(String texto) {
        Label l = label(texto, 11, "#888888", true);
        VBox.setMargin(l, new Insets(8, 0, 2, 0));
        return l;
    }

    // ── ComboBox estilizado ───────────────────────────────────────
    /**
     * ComboBox con el estilo estándar de los filtros de la toolbar.
     */
    public ComboBox<String> styledCombo() {
        ComboBox<String> combo = new ComboBox<>();
        combo.setPrefHeight(42);
        combo.setPrefWidth(160);
        combo.setStyle("-fx-background-color:#f5f7fb;-fx-background-radius:10;"
                + "-fx-border-color:transparent;-fx-border-radius:10;"
                + "-fx-font-size:13px;-fx-text-fill:" + AppColors.TEXT_SECONDARY + ";-fx-cursor:hand;");
        return combo;
    }

    // ── Label genérico ────────────────────────────────────────────
    /**
     * Label con fuente, tamaño y color configurables.
     */
    public Label label(String text, double size, String color, boolean bold) {
        Label lbl = new Label(text);
        lbl.setFont(bold
                ? Font.font("System", FontWeight.BOLD, size)
                : Font.font("System", size));
        lbl.setTextFill(Color.web(color));
        return lbl;
    }

    // ── Helpers de color por EstadoAlerta ─────────────────────────
    /**
     * Color de texto del badge según el estado de la alerta.
     * Information Expert: conoce la paleta (vía AppColors) y la semántica
     * de los estados.
     */
    public String colorEstado(EstadoAlerta e) {
        if (e == null) return AppColors.GRAY_TEXT;
        return switch (e) {
            case PENDIENTE        -> AppColors.ORANGE;
            case RECIBIDA         -> AppColors.BLUE;
            case EN_ATENCION      -> AppColors.COLOR_EN_ATENCION;
            case UNIDAD_ASIGNADA  -> AppColors.COLOR_UNIDAD_ASIGNADA;
            case RESUELTA         -> AppColors.GREEN;
            case CANCELADA        -> AppColors.GRAY_TEXT;
        };
    }

    /**
     * Color de fondo del badge según el estado de la alerta.
     */
    public String estadoBg(EstadoAlerta e) {
        if (e == null) return AppColors.TIPO_DEFAULT_BG;
        return switch (e) {
            case PENDIENTE        -> AppColors.ORANGE_LIGHT;
            case RECIBIDA         -> AppColors.BLUE_SOFT;
            case EN_ATENCION      -> AppColors.PURPLE_LIGHT;
            case UNIDAD_ASIGNADA  -> AppColors.CYAN_LIGHT;
            case RESUELTA         -> AppColors.GREEN_LIGHT;
            case CANCELADA        -> AppColors.TIPO_DEFAULT_BG;
        };
    }

    // ── Helpers de color / emoji por TipoAlerta ───────────────────
    /**
     * Emoji representativo para el tipo de alerta.
     * Information Expert: conoce el mapeo nombre → emoji.
     */
    public String emojiTipo(String nombre) {
        if (nombre == null) return "🚨";
        String n = nombre.toUpperCase();
        if (n.contains("ROB")  || n.contains("ASALT")) return "🦹";
        if (n.contains("SOSPECH"))                      return "🕵";
        if (n.contains("INCEND"))                       return "🔥";
        if (n.contains("RUIDO") || n.contains("ALTER")) return "📢";
        if (n.contains("MÉDI")  || n.contains("MEDIC")) return "➕";
        if (n.contains("ACCID"))                        return "⚠";
        if (n.contains("ANIMAL"))                       return "🐕";
        return "🚨";
    }

    /**
     * Color de texto del badge según el nombre del tipo de alerta.
     */
    public String tipoColor(String nombre) {
        if (nombre == null) return AppColors.GRAY_TEXT;
        String n = nombre.toUpperCase();
        if (n.contains("ROB")   || n.contains("ASALT")) return AppColors.TIPO_ROBO_TEXT;
        if (n.contains("INCEND"))                        return AppColors.TIPO_INCENDIO_TEXT;
        if (n.contains("MÉDI")  || n.contains("MEDIC")) return AppColors.TIPO_MEDICO_TEXT;
        if (n.contains("ACCID"))                         return AppColors.TIPO_ACCIDENTE_TEXT;
        return AppColors.TEXT_SECONDARY;
    }

    /**
     * Color de fondo del badge según el nombre del tipo de alerta.
     */
    public String tipoBg(String nombre) {
        if (nombre == null) return AppColors.TIPO_DEFAULT_BG;
        String n = nombre.toUpperCase();
        if (n.contains("ROB")   || n.contains("ASALT")) return AppColors.TIPO_ROBO_BG;
        if (n.contains("INCEND"))                        return AppColors.TIPO_INCENDIO_BG;
        if (n.contains("MÉDI")  || n.contains("MEDIC")) return AppColors.TIPO_MEDICO_BG;
        if (n.contains("ACCID"))                         return AppColors.TIPO_ACCIDENTE_BG;
        return AppColors.TIPO_DEFAULT_BG;
    }

    // ── Sombra ────────────────────────────────────────────────────
    /**
     * Aplica sombra suave a cualquier Region de JavaFX.
     */
    public void shadow(Region node) {
        DropShadow s = new DropShadow();
        s.setRadius(18);
        s.setOffsetY(4);
        s.setColor(Color.rgb(15, 23, 42, 0.08));
        node.setEffect(s);
    }
}