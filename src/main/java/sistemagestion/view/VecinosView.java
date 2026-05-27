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
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.*;
import java.util.stream.Collectors;

import sistemagestion.model.*;
import sistemagestion.service.*;

/**
 * Vista "Mis Vecinos" — muestra los usuarios que han reportado alertas
 * en el mismo barrio del usuario logueado. Solo usa datos reales de la BD.
 *
 * Datos disponibles por alerta (vw_alertas_completa):
 *   NOMBRE_USUARIO, USERNAME, CEDULA, TELEFONO,
 *   TIPO_ALERTA, BARRIO, COMUNA, FECHA, ESTADO, DESCRIPCION
 *
 * @author generado
 */
public class VecinosView {

    // ── Paleta (igual que UsuarioApp) ─────────────────────────────
    private static final String WHITE     = "#ffffff";
    private static final String BG        = "#f4f6fb";
    private static final String RED       = "#e53935";
    private static final String RED_LIGHT = "#fff0f0";
    private static final String ORANGE    = "#fb8c00";
    private static final String GREEN     = "#43a047";
    private static final String BLUE      = "#1565c0";
    private static final String BLUE_LIGHT= "#e8f0fe";
    private static final String GRAY_TEXT = "#6b7280";
    private static final String BORDER    = "#e5e7eb";

    // ── Dependencias ──────────────────────────────────────────────
    private final Usuario        usuarioActual;
    private final AlertaService  alertaService;

    // ── Estado UI ─────────────────────────────────────────────────
    /** Barrio del usuario logueado, puede ser null si no tiene dirección */
    private final String miBarrio;

    /**
     * Vecino = datos únicos por username extraídos de las alertas del barrio.
     * Usamos un record interno para agrupar la info disponible.
     */
    private record DatosVecino(
            String nombreCompleto,
            String username,
            String cedula,
            String telefono,
            long   totalAlertas,
            long   alertasActivas,
            String ultimoTipoAlerta,
            String ultimoEstado
    ) {}

    // ── Constructor ───────────────────────────────────────────────
    public VecinosView(Usuario usuarioActual, AlertaService alertaService) {
        this.usuarioActual = usuarioActual;
        this.alertaService  = alertaService;

        // Barrio del usuario logueado
        String barrio = null;
        if (usuarioActual != null
                && usuarioActual.getDireccion() != null
                && usuarioActual.getDireccion().getBarrio() != null) {
            barrio = usuarioActual.getDireccion().getBarrio().getNombre();
        }
        this.miBarrio = barrio;
    }

    // =========================================================================
    // PUNTO DE ENTRADA
    // =========================================================================
    public ScrollPane getView() {

        VBox content = new VBox(20);
        content.setPadding(new Insets(28));
        content.setStyle("-fx-background-color: " + BG + ";");

        // Cabecera
        content.getChildren().add(buildHeader());

        // Carga datos
        List<DatosVecino> vecinos = cargarVecinos();

        // Tarjeta de resumen
        content.getChildren().add(buildResumenCard(vecinos));

        // Lista de vecinos
        content.getChildren().add(buildListaVecinos(vecinos));

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: " + BG + "; -fx-background: " + BG + ";");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        return scroll;
    }

    // =========================================================================
    // CABECERA
    // =========================================================================
    private VBox buildHeader() {
        VBox header = new VBox(4);

        String barrioDisplay = miBarrio != null ? "Barrio " + miBarrio : "tu barrio";

        Label titulo = new Label("👥  Mis Vecinos");
        titulo.setFont(Font.font("System", FontWeight.BOLD, 28));
        titulo.setTextFill(Color.web("#111827"));

        Label subtitulo = new Label(
                "Usuarios que han reportado alertas en " + barrioDisplay
                        + "  ·  Solo se muestran quienes han publicado al menos una alerta"
        );
        subtitulo.setFont(Font.font("System", 13));
        subtitulo.setTextFill(Color.web(GRAY_TEXT));
        subtitulo.setWrapText(true);

        header.getChildren().addAll(titulo, subtitulo);
        return header;
    }

    // =========================================================================
    // TARJETA DE RESUMEN
    // =========================================================================
    private HBox buildResumenCard(List<DatosVecino> vecinos) {
        HBox row = new HBox(16);

        long totalVecinos   = vecinos.size();
        long conAlertaActiva = vecinos.stream()
                .filter(v -> v.alertasActivas() > 0)
                .count();
        long totalAlertas   = vecinos.stream()
                .mapToLong(DatosVecino::totalAlertas)
                .sum();

        row.getChildren().addAll(
                resumenItem("👥", "#e8f0fe", BLUE,
                        "Vecinos activos",  totalVecinos,
                        miBarrio != null ? "En " + miBarrio : "En tu barrio"),
                resumenItem("🔔", RED_LIGHT, RED,
                        "Con alerta activa", conAlertaActiva,
                        "PENDIENTE o EN ATENCIÓN"),
                resumenItem("📋", "#e8f5e9", GREEN,
                        "Alertas totales",   totalAlertas,
                        "Reportadas por vecinos")
        );
        return row;
    }

    private VBox resumenItem(String icon, String bgIcon, String iconColor,
                             String titulo, long valor, String sub) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(14));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 18;");
        card.setMinHeight(100);
        card.setMaxHeight(100);
        HBox.setHgrow(card, Priority.ALWAYS);
        shadow(card);

        StackPane iconBox = new StackPane();
        Rectangle bg = new Rectangle(48, 48);
        bg.setArcWidth(14); bg.setArcHeight(14);
        bg.setFill(Color.web(bgIcon));
        iconBox.getChildren().addAll(bg, lbl(icon, 20, iconColor, false));

        Label valLbl = new Label(String.valueOf(valor));
        valLbl.setFont(Font.font("System", FontWeight.BOLD, 28));
        valLbl.setTextFill(Color.web("#111827"));

        VBox texto = new VBox(3);
        texto.getChildren().addAll(lbl(titulo, 13, "#374151", true), valLbl, lbl(sub, 11, GRAY_TEXT, false));

        HBox top = new HBox(14);
        top.setAlignment(Pos.CENTER_LEFT);
        top.getChildren().addAll(iconBox, texto);
        card.getChildren().add(top);

        card.setOnMouseEntered(e -> card.setTranslateY(-2));
        card.setOnMouseExited(e  -> card.setTranslateY(0));
        return card;
    }

    // =========================================================================
    // LISTA DE VECINOS
    // =========================================================================
    private VBox buildListaVecinos(List<DatosVecino> vecinos) {
        VBox card = new VBox(0);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 14;");
        card.setPadding(new Insets(20));
        shadow(card);

        // — Título de sección —
        HBox sectionHeader = new HBox(8);
        sectionHeader.setAlignment(Pos.CENTER_LEFT);
        sectionHeader.setPadding(new Insets(0, 0, 14, 0));
        sectionHeader.getChildren().addAll(
                lbl("📋", 16, BLUE, false),
                lbl("Directorio de vecinos", 15, "#111827", true)
        );

        // Badge cantidad
        Label badge = new Label(String.valueOf(vecinos.size()));
        badge.setFont(Font.font("System", FontWeight.BOLD, 11));
        badge.setTextFill(Color.web(WHITE));
        badge.setPadding(new Insets(2, 8, 2, 8));
        badge.setStyle("-fx-background-color: " + BLUE + "; -fx-background-radius: 20;");
        sectionHeader.getChildren().add(badge);

        card.getChildren().addAll(sectionHeader, separator());

        if (vecinos.isEmpty()) {
            VBox empty = new VBox(12);
            empty.setAlignment(Pos.CENTER);
            empty.setPadding(new Insets(40));
            Label emptyIcon = new Label("🏘");
            emptyIcon.setFont(Font.font(48));
            String msg = miBarrio != null
                    ? "Ningún vecino de " + miBarrio + " ha reportado alertas aún."
                    : "No tienes barrio asignado o no hay vecinos con alertas.";
            empty.getChildren().addAll(emptyIcon, lbl(msg, 14, GRAY_TEXT, false));
            card.getChildren().add(empty);
            return card;
        }

        // Encabezado de tabla
        HBox thead = buildFilaEncabezado();
        card.getChildren().addAll(thead, separator());

        // Filas de datos
        for (int i = 0; i < vecinos.size(); i++) {
            DatosVecino v = vecinos.get(i);
            HBox fila = buildFilaVecino(v, i);
            card.getChildren().add(fila);
            if (i < vecinos.size() - 1) {
                card.getChildren().add(separator());
            }
        }

        return card;
    }

    // ── Encabezado de tabla ───────────────────────────────────────
    private HBox buildFilaEncabezado() {
        HBox row = new HBox();
        row.setPadding(new Insets(10, 0, 10, 0));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #f8fafc;");

        row.getChildren().addAll(
                celdaHead("#",           50),
                celdaHead("Vecino",     200),
                celdaHead("Usuario",    140),
                celdaHead("Teléfono",   130),
                celdaHead("Alertas",     80),
                celdaHead("Activas",     80),
                celdaHead("Último tipo",150),
                celdaHead("Estado",     120)
        );
        return row;
    }

    private Label celdaHead(String texto, double ancho) {
        Label l = lbl(texto, 11, GRAY_TEXT, true);
        l.setPrefWidth(ancho);
        l.setMinWidth(ancho);
        l.setPadding(new Insets(0, 8, 0, 8));
        return l;
    }

    // ── Fila de un vecino ─────────────────────────────────────────
    private HBox buildFilaVecino(DatosVecino v, int index) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(14, 0, 14, 0));
        row.setCursor(javafx.scene.Cursor.HAND);

        String normalBg = index % 2 == 0 ? "transparent" : "#fafbfc";
        row.setStyle("-fx-background-color: " + normalBg + ";");
        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: " + BLUE_LIGHT + ";"));
        row.setOnMouseExited(e  -> row.setStyle("-fx-background-color: " + normalBg + ";"));

        // Avatar con inicial
        String inicial = v.nombreCompleto() != null && !v.nombreCompleto().isBlank()
                ? String.valueOf(v.nombreCompleto().charAt(0)).toUpperCase()
                : "?";
        String[] avatarColors = {"#1565c0","#43a047","#e53935","#fb8c00","#6a1b9a","#00838f"};
        String avatarColor = avatarColors[Math.abs(v.username().hashCode()) % avatarColors.length];

        Circle avatarCircle = new Circle(18, Color.web(avatarColor));
        Label avatarLbl = lbl(inicial, 14, WHITE, true);
        StackPane avatar = new StackPane(avatarCircle, avatarLbl);
        avatar.setMinWidth(36); avatar.setMaxWidth(36);

        // Número
        Label numLbl = lbl(String.valueOf(index + 1), 12, GRAY_TEXT, false);
        numLbl.setPrefWidth(50); numLbl.setMinWidth(50);
        numLbl.setPadding(new Insets(0, 8, 0, 8));

        // Nombre
        VBox nombreBox = new VBox(2);
        nombreBox.setPrefWidth(200); nombreBox.setMinWidth(200);
        nombreBox.setPadding(new Insets(0, 8, 0, 4));
        String nombreDisplay = v.nombreCompleto() != null && !v.nombreCompleto().isBlank()
                ? v.nombreCompleto() : "Sin nombre";
        nombreBox.getChildren().addAll(
                lbl(nombreDisplay, 13, "#111827", true),
                lbl("C.C. " + (v.cedula() != null ? v.cedula() : "—"), 10, GRAY_TEXT, false)
        );

        // Username
        Label userLbl = lbl("@" + (v.username() != null ? v.username() : "—"), 12, BLUE, false);
        userLbl.setPrefWidth(140); userLbl.setMinWidth(140);
        userLbl.setPadding(new Insets(0, 8, 0, 8));

        // Teléfono
        Label telLbl = lbl(v.telefono() != null ? v.telefono() : "—", 12, "#374151", false);
        telLbl.setPrefWidth(130); telLbl.setMinWidth(130);
        telLbl.setPadding(new Insets(0, 8, 0, 8));

        // Total alertas
        Label totalLbl = lbl(String.valueOf(v.totalAlertas()), 13, "#111827", true);
        totalLbl.setPrefWidth(80); totalLbl.setMinWidth(80);
        totalLbl.setPadding(new Insets(0, 8, 0, 8));
        totalLbl.setAlignment(Pos.CENTER);

        // Alertas activas
        Label activasLbl = lbl(String.valueOf(v.alertasActivas()), 13,
                v.alertasActivas() > 0 ? RED : GREEN, true);
        activasLbl.setPrefWidth(80); activasLbl.setMinWidth(80);
        activasLbl.setPadding(new Insets(0, 8, 0, 8));

        // Último tipo alerta
        Label tipoLbl = lbl(
                v.ultimoTipoAlerta() != null ? v.ultimoTipoAlerta() : "—",
                11, "#374151", false
        );
        tipoLbl.setPrefWidth(150); tipoLbl.setMinWidth(150);
        tipoLbl.setPadding(new Insets(0, 8, 0, 8));
        tipoLbl.setWrapText(true);

        // Estado badge
        Label estadoBadge = buildEstadoBadge(v.ultimoEstado());

        row.getChildren().addAll(
                numLbl,
                new StackPane(avatar), // wrap para que respete padding
                nombreBox,
                userLbl,
                telLbl,
                totalLbl,
                activasLbl,
                tipoLbl,
                estadoBadge
        );

        return row;
    }

    private Label buildEstadoBadge(String estado) {
        String texto   = estado != null ? estado.replace("_", " ") : "—";
        String fgColor;
        String bgColor;

        if (estado == null) {
            fgColor = GRAY_TEXT; bgColor = "#f3f4f6";
        } else switch (estado) {
            case "PENDIENTE"      -> { fgColor = RED;    bgColor = RED_LIGHT; }
            case "EN_ATENCION"    -> { fgColor = ORANGE; bgColor = "#fff8e1"; }
            case "UNIDAD_ASIGNADA"-> { fgColor = BLUE;   bgColor = BLUE_LIGHT; }
            case "RESUELTA"       -> { fgColor = GREEN;  bgColor = "#e8f5e9"; }
            case "CANCELADA"      -> { fgColor = GRAY_TEXT; bgColor = "#f3f4f6"; }
            default               -> { fgColor = GRAY_TEXT; bgColor = "#f3f4f6"; }
        }

        Label badge = new Label(texto);
        badge.setFont(Font.font("System", FontWeight.BOLD, 10));
        badge.setTextFill(Color.web(fgColor));
        badge.setPadding(new Insets(3, 10, 3, 10));
        badge.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 20;");
        badge.setPrefWidth(120);
        badge.setMinWidth(120);
        badge.setAlignment(Pos.CENTER);
        return badge;
    }

    // =========================================================================
    // CARGA DE DATOS REALES
    // =========================================================================
    /**
     * Obtiene todas las alertas del barrio del usuario logueado,
     * agrupa por username y construye un DatosVecino por persona.
     * Excluye al propio usuario logueado.
     */
    private List<DatosVecino> cargarVecinos() {
        if (alertaService == null || miBarrio == null) {
            return Collections.emptyList();
        }

        List<Alerta> todasAlertas;
        try {
            todasAlertas = alertaService.listar();
        } catch (Exception e) {
            return Collections.emptyList();
        }

        // Filtrar por barrio y excluir al propio usuario
        String miUsername = usuarioActual != null ? usuarioActual.getUsername() : null;

        List<Alerta> alertasBarrio = todasAlertas.stream()
                .filter(a -> a.getBarrio() != null
                        && miBarrio.equalsIgnoreCase(a.getBarrio().getNombre()))
                .filter(a -> a.getUsuario() != null
                        && a.getUsuario().getUsername() != null
                        && !a.getUsuario().getUsername().equalsIgnoreCase(miUsername))
                .collect(Collectors.toList());

        // Agrupar por username
        Map<String, List<Alerta>> porUsuario = alertasBarrio.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getUsuario().getUsername()
                ));

        // Construir DatosVecino por cada username único
        List<DatosVecino> vecinos = new ArrayList<>();

        for (Map.Entry<String, List<Alerta>> entry : porUsuario.entrySet()) {
            String username    = entry.getKey();
            List<Alerta> suyas = entry.getValue();

            // Tomamos los datos personales de la primera alerta (son constantes por usuario)
            Alerta primera = suyas.get(0);
            Usuario u = primera.getUsuario();

            String nombreCompleto = (u.getPrimer_nombre() != null ? u.getPrimer_nombre() : "")
                    + " " + (u.getIdentificacion() != null ? "" : ""); // nombre ya viene en NOMBRE_USUARIO
            // En la vista el campo mapea NOMBRE_USUARIO → primer_nombre (ver AlertaDAO.mapear)
            // así que getPrimer_nombre() contiene el nombre completo tal como viene de la BD
            String nombre   = u.getPrimer_nombre() != null ? u.getPrimer_nombre().trim() : username;
            String cedula   = u.getIdentificacion();
            String telefono = u.getTelefono();

            long totalAlertas = suyas.size();

            // Alertas activas = estados que indican que aún no está resuelta/cancelada
            long activas = suyas.stream()
                    .filter(a -> a.getEstado() != null)
                    .filter(a -> a.getEstado() == EstadoAlerta.PENDIENTE
                            || a.getEstado() == EstadoAlerta.EN_ATENCION
                            || a.getEstado() == EstadoAlerta.UNIDAD_ASIGNADA
                            || a.getEstado() == EstadoAlerta.RECIBIDA)
                    .count();

            // Última alerta (la más reciente por fecha)
            Alerta ultima = suyas.stream()
                    .filter(a -> a.getFechaHora() != null)
                    .max(Comparator.comparing(Alerta::getFechaHora))
                    .orElse(primera);

            String ultimoTipo   = ultima.getTipoalerta() != null
                    ? ultima.getTipoalerta().getNombre() : null;
            String ultimoEstado = ultima.getEstado() != null
                    ? ultima.getEstado().name() : null;

            vecinos.add(new DatosVecino(
                    nombre, username, cedula, telefono,
                    totalAlertas, activas, ultimoTipo, ultimoEstado
            ));
        }

        // Ordenar: más alertas activas primero, luego más alertas totales
        vecinos.sort(Comparator
                .comparingLong(DatosVecino::alertasActivas).reversed()
                .thenComparingLong(DatosVecino::totalAlertas).reversed()
        );

        return vecinos;
    }

    // =========================================================================
    // HELPERS UI
    // =========================================================================
    private Label lbl(String texto, double size, String color, boolean bold) {
        Label l = new Label(texto);
        l.setFont(bold
                ? Font.font("System", FontWeight.BOLD, size)
                : Font.font("System", size));
        l.setTextFill(Color.web(color));
        return l;
    }

    private Region separator() {
        Region sep = new Region();
        sep.setPrefHeight(1);
        sep.setStyle("-fx-background-color: " + BORDER + ";");
        return sep;
    }

    private void shadow(Region node) {
        DropShadow ds = new DropShadow();
        ds.setRadius(18);
        ds.setOffsetY(4);
        ds.setColor(Color.rgb(15, 23, 42, 0.10));
        node.setEffect(ds);
    }
}