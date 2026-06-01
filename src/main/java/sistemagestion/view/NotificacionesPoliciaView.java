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

import java.util.List;
import java.util.stream.Collectors;

import sistemagestion.model.*;
import sistemagestion.service.*;

public class NotificacionesPoliciaView {

    // ── Paleta ───────────────────────────────────────────────────
    private static final String BG_PAGE = "#f0f2f8";
    private static final String BG_CARD = "#ffffff";
    private static final String BORDER = "#e8eaf0";

    private static final String TEXT_PRIMARY = "#1a1f36";
    private static final String TEXT_SECONDARY = "#5a6070";

    private static final String C_LEIDA = "#2e7d32";
    private static final String C_LEIDA_BG = "#e8f5e9";
    private static final String C_ENVI = "#1565c0";
    private static final String C_ENVI_BG = "#e3f2fd";
    private static final String C_ERROR = "#c62828";
    private static final String C_ERROR_BG = "#ffebee";
    private static final String C_PEND = "#e65100";
    private static final String C_PEND_BG = "#fff3e0";

    private final Usuario usuarioActual;
    private final NotificacionService notificacionService;

    public NotificacionesPoliciaView(Usuario usuarioActual, NotificacionService notificacionService) {
        this.usuarioActual = usuarioActual;
        this.notificacionService = notificacionService;
    }

    // ── Build principal ──────────────────────────────────────────
    public ScrollPane build() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(28, 28, 36, 28));
        content.setStyle("-fx-background-color: " + BG_PAGE + ";");

        content.getChildren().add(buildHeader());
        content.getChildren().add(buildListCard());

        return wrapScroll(content);
    }

    // ── Header ───────────────────────────────────────────────────
    private HBox buildHeader() {
        HBox header = new HBox(14);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 24, 20, 24));
        header.setStyle(
               "-fx-background-color: linear-gradient(to right, #e8f3fa, #e6f4fc);" +
                "-fx-background-radius: 14;"
        );

        StackPane icoWrap = new StackPane();
        icoWrap.setPrefSize(54, 54);
        icoWrap.setMinSize(54, 54);
        icoWrap.setMaxSize(54, 54);
        Circle icoBg = new Circle(27, Color.web("#ffffff80"));
        Label icoLbl = new Label("\uf0f3");
        icoLbl.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';"
                + "-fx-font-size:24px;-fx-text-fill:#1565c0");
        icoWrap.getChildren().addAll(icoBg, icoLbl);

        VBox textBox = new VBox(3);
        Label title = new Label("Mis Notificaciones");
        title.setFont(Font.font("System", FontWeight.BOLD, 20));
        title.setTextFill(Color.WHITE);

        String nombreUsuario = (usuarioActual != null && usuarioActual.getUsername() != null)
                ? usuarioActual.getUsername() : "Oficial";
        Label subtitle = new Label("Notificaciones recibidas para " + nombreUsuario);
        subtitle.setFont(Font.font("System", 12));
        subtitle.setTextFill(Color.web("#90caf9"));

        textBox.getChildren().addAll(title, subtitle);

        DropShadow shadow = new DropShadow(12, 0, 4, Color.web("#1a237e", 0.30));
        header.setEffect(shadow);

        header.getChildren().addAll(icoWrap, textBox);
        return header;
    }

    // ── Tarjeta principal con la lista ───────────────────────────
    private VBox buildListCard() {
        VBox card = new VBox(0);
        card.setStyle(
                "-fx-background-color: " + BG_CARD + ";"
                + "-fx-background-radius: 14;"
        );
        DropShadow shadow = new DropShadow(14, 0, 3, Color.web("#000000", 0.08));
        card.setEffect(shadow);

        // Sub-cabecera
        HBox cardHeader = new HBox(8);
        cardHeader.setAlignment(Pos.CENTER_LEFT);
        cardHeader.setPadding(new Insets(16, 20, 14, 20));
        cardHeader.setStyle("-fx-border-color: " + BORDER + "; -fx-border-width: 0 0 1 0;");

        Rectangle accent = new Rectangle(4, 18);
        accent.setArcWidth(4);
        accent.setArcHeight(4);
        accent.setFill(Color.web(C_ENVI));

        Label cardTitle = new Label("Mis notificaciones");
        cardTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        cardTitle.setTextFill(Color.web(TEXT_PRIMARY));

        cardHeader.getChildren().addAll(accent, cardTitle);
        card.getChildren().add(cardHeader);

        // Contenido dinámico
        VBox listBox = new VBox(0);
        listBox.setPadding(new Insets(6, 0, 6, 0));

        try {
            List<Notificacion> lista = notificacionService.listar();

            // Filtrar por usuario actual
            List<Notificacion> mias = lista.stream()
                    .filter(n -> n.getUsuario() != null && usuarioActual != null
                    && (usuarioActual.getUsername() != null
                    && usuarioActual.getUsername().equals(n.getUsuario().getUsername())
                    || usuarioActual.getCorreo() != null
                    && usuarioActual.getCorreo().equals(n.getCorreodestinatario())))
                    .collect(Collectors.toList());

            List<Notificacion> mostrar = mias.isEmpty()
                    ? lista.subList(0, Math.min(lista.size(), 20))
                    : mias;

            if (mostrar.isEmpty()) {
                listBox.getChildren().add(buildEmptyState());
            } else {
                boolean propias = !mias.isEmpty();
                for (int i = 0; i < mostrar.size(); i++) {
                    listBox.getChildren().add(buildRow(mostrar.get(i), propias));
                    if (i < mostrar.size() - 1) {
                        listBox.getChildren().add(separator());
                    }
                }
            }
        } catch (Exception e) {
            listBox.getChildren().add(buildErrorState(e.getMessage()));
        }

        card.getChildren().add(listBox);
        return card;
    }

    // ── Fila individual ──────────────────────────────────────────
    private HBox buildRow(Notificacion n, boolean propias) {
        String dest = n.getCorreodestinatario() != null ? n.getCorreodestinatario() : "Sin destinatario";
        String msg = n.getMensaje() != null && !n.getMensaje().isBlank()
                ? n.getMensaje() : "Sin mensaje";
        String estadoStr = n.getEstado() != null
                ? n.getEstado().name().replace("_", " ") : "PENDIENTE";

        String[] colors;
        if (n.getEstado() == EstadoNotificacion.LEIDA) {
            colors = new String[]{C_LEIDA, C_LEIDA_BG, "✔"};
        } else if (n.getEstado() == EstadoNotificacion.ENVIADA) {
            colors = new String[]{C_ENVI, C_ENVI_BG, "📤"};
        } else if (n.getEstado() == EstadoNotificacion.ERROR) {
            colors = new String[]{C_ERROR, C_ERROR_BG, "✖"};
        } else {
            colors = new String[]{C_PEND, C_PEND_BG, "⏳"};
        }

        String stateColor = colors[0];
        String stateBg = colors[1];
        String stateIcon = colors[2];

        HBox row = new HBox(14);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(13, 20, 13, 20));
        row.setStyle("-fx-background-color: transparent;");

        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #f8f9fd; -fx-cursor: hand;"));
        row.setOnMouseExited(e -> row.setStyle("-fx-background-color: transparent;"));

        // Ícono de estado
        StackPane stateBox = new StackPane();
        stateBox.setMinSize(38, 38);
        stateBox.setMaxSize(38, 38);
        Rectangle stateBgRect = new Rectangle(38, 38);
        stateBgRect.setArcWidth(10);
        stateBgRect.setArcHeight(10);
        stateBgRect.setFill(Color.web(stateBg));
        Label stateIconLbl = new Label(stateIcon);
        stateIconLbl.setFont(Font.font("System", FontWeight.BOLD, 14));
        stateIconLbl.setTextFill(Color.web(stateColor));
        stateBox.getChildren().addAll(stateBgRect, stateIconLbl);

        // Textos
        VBox textBox = new VBox(4);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        // Si son propias solo mostramos estado; si son de todos mostramos destinatario
        String lineaPrincipal = propias ? estadoStr : "✉  " + dest;
        Label destLbl = new Label(lineaPrincipal);
        destLbl.setFont(Font.font("System", FontWeight.BOLD, 13));
        destLbl.setTextFill(Color.web(TEXT_PRIMARY));

        String msgTrunc = msg.length() > 80 ? msg.substring(0, 77) + "..." : msg;
        Label msgLbl = new Label(msgTrunc);
        msgLbl.setFont(Font.font("System", 12));
        msgLbl.setTextFill(Color.web(TEXT_SECONDARY));
        msgLbl.setWrapText(false);

        textBox.getChildren().addAll(destLbl, msgLbl);

        // Badge de estado
        Label badge = new Label(estadoStr);
        badge.setFont(Font.font("System", FontWeight.BOLD, 10));
        badge.setTextFill(Color.web(stateColor));
        badge.setPadding(new Insets(3, 10, 3, 10));
        badge.setStyle(
                "-fx-background-color: " + stateBg + ";"
                + "-fx-background-radius: 20;"
        );

        row.getChildren().addAll(stateBox, textBox, badge);
        return row;
    }

    // ── Estado vacío ─────────────────────────────────────────────
    private VBox buildEmptyState() {
        VBox box = new VBox(8);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(40, 0, 40, 0));

        Label icon = new Label("📭");
        icon.setFont(Font.font("System", 36));

        Label msg = new Label("No tienes notificaciones");
        msg.setFont(Font.font("System", FontWeight.BOLD, 14));
        msg.setTextFill(Color.web(TEXT_SECONDARY));

        box.getChildren().addAll(icon, msg);
        return box;
    }

    // ── Estado de error ──────────────────────────────────────────
    private HBox buildErrorState(String message) {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(14, 20, 14, 20));
        box.setStyle(
                "-fx-background-color: " + C_ERROR_BG + ";"
                + "-fx-background-radius: 8;"
        );
        Label icon = new Label("⚠");
        icon.setFont(Font.font("System", FontWeight.BOLD, 16));
        icon.setTextFill(Color.web(C_ERROR));
        Label msg = new Label("Error al cargar: " + message);
        msg.setFont(Font.font("System", 12));
        msg.setTextFill(Color.web(C_ERROR));
        msg.setWrapText(true);
        box.getChildren().addAll(icon, msg);
        return box;
    }

    // ── Helpers básicos ──────────────────────────────────────────
    private Region separator() {
        Region sep = new Region();
        sep.setPrefHeight(1);
        sep.setStyle("-fx-background-color: " + BORDER + ";");
        return sep;
    }

    private ScrollPane wrapScroll(VBox content) {
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setStyle(
                "-fx-background: " + BG_PAGE + ";"
                + "-fx-background-color: " + BG_PAGE + ";"
        );
        return scroll;
    }
}
