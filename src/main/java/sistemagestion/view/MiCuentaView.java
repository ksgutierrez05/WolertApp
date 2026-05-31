/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.view;

/**
 *
 * @author Maria Cristina
 */


import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import sistemagestion.model.Suscripcion;
import sistemagestion.model.Usuario;
import sistemagestion.service.SuscripcionService;

public class MiCuentaView {

    private static final String WHITE     = "#ffffff";
    private static final String BG        = "#f4f6fb";
    private static final String RED       = "#e53935";
    private static final String ORANGE    = "#fb8c00";
    private static final String GREEN     = "#43a047";
    private static final String BLUE      = "#1565c0";
    private static final String GRAY_TEXT = "#6b7280";
    private static final String BORDER    = "#e5e7eb";

    private final Usuario            usuarioActual;
    private final SuscripcionService suscripcionService;
    private final BorderPane         root;

    public MiCuentaView(Usuario usuarioActual,
                        SuscripcionService suscripcionService,
                        BorderPane root) {
        this.usuarioActual      = usuarioActual;
        this.suscripcionService = suscripcionService;
        this.root               = root;
    }

    // =========================================================================
    // PUNTO DE ENTRADA
    // =========================================================================
    public ScrollPane getView() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(22));
        content.setStyle("-fx-background-color: " + BG + ";");

        content.getChildren().addAll(
                buildHeader(),
                buildPerfilPanel(),
                buildSuscripcionesPanel(),
                buildInfoUtilPanel(),
                buildVolverBtn()
        );

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
        Label title = new Label("👤 Mi Cuenta");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#111827"));
        Label sub = label("Gestiona tu perfil, suscripciones e información útil", 13, GRAY_TEXT, false);
        titles.getChildren().addAll(title, sub);

        header.getChildren().add(titles);
        return header;
    }

    // =========================================================================
    // PANEL: INFORMACIÓN PERSONAL
    // =========================================================================
    private VBox buildPerfilPanel() {
        VBox panel = createPanel("👤 Información personal");

        if (usuarioActual == null) {
            panel.getChildren().add(label("No hay información disponible.", 13, GRAY_TEXT, false));
            return panel;
        }

        String nombre = ((usuarioActual.getPrimer_nombre()   != null ? usuarioActual.getPrimer_nombre()   : "")
                       + " " + (usuarioActual.getPrimer_apellido() != null ? usuarioActual.getPrimer_apellido() : "")).trim();
        String barrio = usuarioActual.getDireccion() != null
                && usuarioActual.getDireccion().getBarrio() != null
                ? usuarioActual.getDireccion().getBarrio().getNombre() : "—";
        String rol    = usuarioActual.getRol() != null ? usuarioActual.getRol().getNombre() : "—";

        panel.getChildren().addAll(
                infoRow("👤", "Nombre",          nombre.isEmpty() ? "—" : nombre),
                sep(),
                infoRow("🪪", "Identificación",  val(usuarioActual.getIdentificacion())),
                sep(),
                infoRow("✉",  "Correo",           val(usuarioActual.getCorreo())),
                sep(),
                infoRow("📱", "Teléfono",         val(usuarioActual.getTelefono())),
                sep(),
                infoRow("🔑", "Username",         val(usuarioActual.getUsername())),
                sep(),
                infoRow("📍", "Barrio",           barrio),
                sep(),
                infoRow("🔐", "Rol",              rol)
        );
        return panel;
    }

    // =========================================================================
    // PANEL: MIS SUSCRIPCIONES
    // =========================================================================
    private VBox buildSuscripcionesPanel() {
        VBox panel = createPanel("📋 Mis suscripciones");

        if (suscripcionService == null || usuarioActual == null) {
            panel.getChildren().add(label("Servicio no disponible.", 13, GRAY_TEXT, false));
            return panel;
        }

        try {
            List<Suscripcion> lista = suscripcionService.listar().stream()
                    .filter(s -> s.getUsuario() != null
                            && usuarioActual.getIdentificacion() != null
                            && usuarioActual.getIdentificacion()
                                   .equals(s.getUsuario().getIdentificacion()))
                    .toList();

            if (lista.isEmpty()) {
                VBox vacio = new VBox(8);
                vacio.setAlignment(Pos.CENTER);
                vacio.setPadding(new Insets(20));
                Label ico = new Label("📋");
                ico.setFont(Font.font(30));
                Label msg = label("No tienes suscripciones activas.", 13, GRAY_TEXT, false);
                vacio.getChildren().addAll(ico, msg);
                panel.getChildren().add(vacio);
            } else {
                for (Suscripcion s : lista) {
                    String tipo   = s.getTipoalerta() != null ? s.getTipoalerta().getNombre() : "—";
                    String zona   = s.getBarrio()     != null ? s.getBarrio().getNombre()
                                  : s.getComuna()     != null ? s.getComuna().getNombre() : "—";
                    String estado = s.getEstado()     != null
                            ? s.getEstado().name().replace("_", " ") : "—";
                    String badgeColor = switch (s.getEstado() != null ? s.getEstado().name() : "") {
                        case "ACTIVA"    -> GREEN;
                        case "PAUSADA"   -> ORANGE;
                        case "CANCELADA" -> RED;
                        default          -> GRAY_TEXT;
                    };
                    panel.getChildren().addAll(
                            infoRowBadge("📋", tipo, zona, estado, badgeColor),
                            sep());
                }
            }
        } catch (Exception e) {
            panel.getChildren().add(label("Error: " + e.getMessage(), 12, RED, false));
        }

        return panel;
    }

    // =========================================================================
    // PANEL: INFORMACIÓN ÚTIL
    // =========================================================================
    private VBox buildInfoUtilPanel() {
        VBox panel = createPanel("ℹ️ Información útil");

        panel.getChildren().addAll(
                infoRow("🚨", "Línea de emergencias",     "123"),
                sep(),
                infoRow("🚔", "Policía Nacional",          "112"),
                sep(),
                infoRow("🚒", "Bomberos",                  "119"),
                sep(),
                infoRow("🚑", "Cruz Roja",                 "132"),
                sep(),
                infoRow("📞", "Línea de denuncia",         "018000910600"),
                sep(),
                infoRow("🐺", "Soporte WolertApp",         "wolertapp.notificaciones@gmail.com")
        );
        return panel;
    }

    // =========================================================================
    // BOTÓN VOLVER
    // =========================================================================
    private Button buildVolverBtn() {
        Button btn = new Button("← Volver al Dashboard");
        btn.setStyle(
                "-fx-background-color:" + BLUE + ";-fx-text-fill:white;"
                + "-fx-font-size:13px;-fx-background-radius:8;"
                + "-fx-padding:9 18;-fx-cursor:hand;");
        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color:#0d47a1;-fx-text-fill:white;"
                + "-fx-font-size:13px;-fx-background-radius:8;"
                + "-fx-padding:9 18;-fx-cursor:hand;"));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color:" + BLUE + ";-fx-text-fill:white;"
                + "-fx-font-size:13px;-fx-background-radius:8;"
                + "-fx-padding:9 18;-fx-cursor:hand;"));
        btn.setOnAction(e -> {
            if (root != null) root.setCenter(null); // el UsuarioApp recargará el dashboard
        });
        return btn;
    }

    // =========================================================================
    // HELPERS
    // =========================================================================
    private VBox createPanel(String title) {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(16));
        panel.setStyle("-fx-background-color: white; -fx-background-radius: 12;");
        panel.setEffect(new DropShadow(10, 0, 2, Color.web("#0000001a")));
        Label t = new Label(title);
        t.setFont(Font.font("System", FontWeight.BOLD, 14));
        t.setTextFill(Color.web("#111827"));
        panel.getChildren().addAll(t, sep());
        return panel;
    }

    private HBox infoRow(String icon, String campo, String valor) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(7, 0, 7, 0));
        Label ico = label(icon, 16, BLUE, false);
        ico.setMinWidth(26);
        Label key = label(campo, 12, GRAY_TEXT, false);
        key.setMinWidth(140);
        Label val = label(valor, 13, "#111827", false);
        row.getChildren().addAll(ico, key, val);
        return row;
    }

    private HBox infoRowBadge(String icon, String campo, String valor,
                               String badge, String badgeColor) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(7, 0, 7, 0));
        Label ico = label(icon, 16, BLUE, false);
        ico.setMinWidth(26);
        Label key = label(campo, 12, GRAY_TEXT, false);
        key.setMinWidth(140);
        Label val = label(valor, 13, "#111827", false);
        HBox.setHgrow(val, Priority.ALWAYS);
        Label badgeLbl = new Label(badge);
        badgeLbl.setStyle(
                "-fx-background-color:" + badgeColor + "22;"
                + "-fx-text-fill:" + badgeColor + ";"
                + "-fx-font-size:11px;-fx-font-weight:bold;"
                + "-fx-background-radius:20;-fx-padding:4 10;");
        row.getChildren().addAll(ico, key, val, badgeLbl);
        return row;
    }

    private String val(String v) {
        return v != null && !v.isBlank() ? v : "—";
    }

    private Region sep() {
        Region r = new Region();
        r.setPrefHeight(1);
        r.setStyle("-fx-background-color: " + BORDER + ";");
        return r;
    }

    private Label label(String text, double size, String color, boolean bold) {
        Label lbl = new Label(text);
        lbl.setFont(bold
                ? Font.font("System", FontWeight.BOLD, size)
                : Font.font("System", size));
        lbl.setTextFill(Color.web(color));
        return lbl;
    }
}