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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import sistemagestion.model.Usuario;

public class ChatBotView {

    // ── Paleta — mismos colores que el sidebar ─────────────────────
    private static final String WHITE = "#ffffff";
    private static final String BG = "#f4f6fb";
    private static final String SIDEBAR_BG = "#16283d";       // fondo oscuro sidebar
    private static final String SIDEBAR_MID = "#1f3a56";       // gradiente sidebar
    private static final String BLUE = "#1565c0";
    private static final String BLUE_DARK = "#0d47a1";
    private static final String GRAY_TEXT = "#6b7280";
    private static final String BORDER = "#e5e7eb";
    private static final String BOT_BG = "#f0f4ff";
    private static final String USER_BG = "#1f3a56";        // mismo que sidebar_mid
    private static final String ACCENT = "#8899bb";        // color subtítulos sidebar

    private final Usuario usuarioActual;
    private VBox messagesBox;
    private ScrollPane messagesScroll;
    private final List<JSONObject> historial = new ArrayList<>();

    private static final String API_KEY = ""; // reemplaza con tu clave real

    public ChatBotView(Usuario usuarioActual) {
        this.usuarioActual = usuarioActual;
    }

    public Popup buildPopup(Stage owner) {
        Popup popup = new Popup();
        popup.setAutoHide(false);

        VBox container = new VBox(0);
        container.setPrefWidth(370);
        container.setPrefHeight(520);
        container.setMaxWidth(370);
        container.setMaxHeight(520);
        container.setStyle(
                "-fx-background-color: " + WHITE + ";"
                + "-fx-background-radius: 20;"
                + "-fx-border-radius: 20;"
                + "-fx-border-color: " + BORDER + ";"
                + "-fx-border-width: 1;");
        DropShadow shadow = new DropShadow(30, 0, 8, Color.web("#00000025"));
        container.setEffect(shadow);

        container.getChildren().addAll(
                buildHeader(popup),
                buildMessages(),
                buildInputArea());

        popup.getContent().add(container);

        // Mensaje de bienvenida
        addBotMessage("¡Hola" + (usuarioActual != null
                ? ", " + usuarioActual.getPrimer_nombre() : "") + "! 👋\n"
                + "Soy el asistente de WolertApp. Puedo ayudarte con:\n"
                + "• Cómo reportar una alerta\n"
                + "• Información sobre tu barrio\n"
                + "• Dudas sobre el sistema\n"
                + "¿En qué te puedo ayudar?");

        return popup;
    }

    // ── Header — gradiente del sidebar ───────────────────────────
    private HBox buildHeader(Popup popup) {
        HBox header = new HBox(10);
        header.setPadding(new Insets(16, 16, 14, 16));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle(
                "-fx-background-color: linear-gradient(to right, " + SIDEBAR_BG + ", " + SIDEBAR_MID + ");"
                + "-fx-background-radius: 20 20 0 0;");

        // Avatar bot
        Circle av = new Circle(18, Color.web("#ffffff22"));
        Label avIco = faIcon("\uf544", 14, WHITE);
        StackPane avBox = new StackPane(av, avIco);

        VBox info = new VBox(1);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label name = label("Mayita", 13, WHITE, true);
        HBox statusRow = new HBox(5);
        statusRow.setAlignment(Pos.CENTER_LEFT);
        Circle dot = new Circle(4, Color.web("#69f0ae"));
        Label statusLbl = label("En línea", 10, ACCENT, false);
        statusRow.getChildren().addAll(dot, statusLbl);
        info.getChildren().addAll(name, statusRow);

        // Botón cerrar
        Button closeBtn = new Button("✕");
        closeBtn.setStyle(
                "-fx-background-color: rgba(255,255,255,0.12);"
                + "-fx-text-fill: " + ACCENT + ";"
                + "-fx-font-size: 12px;"
                + "-fx-background-radius: 20;"
                + "-fx-padding: 4 8;"
                + "-fx-cursor: hand;");
        closeBtn.setOnMouseEntered(e -> closeBtn.setStyle(
                "-fx-background-color: rgba(255,255,255,0.22);"
                + "-fx-text-fill: white;"
                + "-fx-font-size: 12px;"
                + "-fx-background-radius: 20;"
                + "-fx-padding: 4 8;"
                + "-fx-cursor: hand;"));
        closeBtn.setOnMouseExited(e -> closeBtn.setStyle(
                "-fx-background-color: rgba(255,255,255,0.12);"
                + "-fx-text-fill: " + ACCENT + ";"
                + "-fx-font-size: 12px;"
                + "-fx-background-radius: 20;"
                + "-fx-padding: 4 8;"
                + "-fx-cursor: hand;"));
        closeBtn.setOnAction(e -> popup.hide());

        header.getChildren().addAll(avBox, info, closeBtn);
        return header;
    }

    // ── Mensajes ─────────────────────────────────────────────────
    private ScrollPane buildMessages() {
        messagesBox = new VBox(10);
        messagesBox.setPadding(new Insets(14, 12, 14, 12));
        messagesBox.setStyle("-fx-background-color: " + BG + ";");

        messagesScroll = new ScrollPane(messagesBox);
        messagesScroll.setFitToWidth(true);
        messagesScroll.setPrefHeight(370);
        messagesScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        messagesScroll.setStyle(
                "-fx-background: " + BG + ";"
                + "-fx-background-color: " + BG + ";");
        VBox.setVgrow(messagesScroll, Priority.ALWAYS);
        return messagesScroll;
    }

    // ── Input ─────────────────────────────────────────────────────
    private HBox buildInputArea() {
        HBox input = new HBox(8);
        input.setPadding(new Insets(12, 12, 14, 12));
        input.setAlignment(Pos.CENTER);
        input.setStyle(
                "-fx-background-color: " + WHITE + ";"
                + "-fx-border-color: " + BORDER + ";"
                + "-fx-border-width: 1 0 0 0;");

        TextField field = new TextField();
        field.setPromptText("Escribe tu mensaje...");
        field.setStyle(
                "-fx-font-size: 13px;"
                + "-fx-background-color: " + BG + ";"
                + "-fx-background-radius: 20;"
                + "-fx-border-color: " + BORDER + ";"
                + "-fx-border-radius: 20;"
                + "-fx-border-width: 1;"
                + "-fx-padding: 8 14;");
        HBox.setHgrow(field, Priority.ALWAYS);

        Button sendBtn = new Button();
        Label sendIco = faIcon("\uf1d8", 13, "#F5F7FA");
        sendBtn.setGraphic(sendIco);
        sendBtn.setStyle(
                "-fx-background-color: " + SIDEBAR_MID + ";"
                + "-fx-background-radius: 20;"
                + "-fx-padding: 8 14;"
                + "-fx-cursor: hand;");
        sendBtn.setOnMouseEntered(e -> sendBtn.setStyle(
                "-fx-background-color: " + SIDEBAR_BG + ";"
                + "-fx-background-radius: 20;"
                + "-fx-padding: 8 14;"
                + "-fx-cursor: hand;"));
        sendBtn.setOnMouseExited(e -> sendBtn.setStyle(
                "-fx-background-color: " + SIDEBAR_MID + ";"
                + "-fx-background-radius: 20;"
                + "-fx-padding: 8 14;"
                + "-fx-cursor: hand;"));

        Runnable enviar = () -> {
            String texto = field.getText().trim();
            if (texto.isEmpty()) {
                return;
            }
            field.clear();
            addUserMessage(texto);
            addTypingIndicator();
            enviarMensaje(texto);
        };

        sendBtn.setOnAction(e -> enviar.run());
        field.setOnAction(e -> enviar.run());

        input.getChildren().addAll(field, sendBtn);
        return input;
    }

    // ── Burbuja bot ───────────────────────────────────────────────
    private void addBotMessage(String texto) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.TOP_LEFT);

        Circle av = new Circle(14, Color.web(SIDEBAR_MID));
        Label avIco = faIcon("\uf544", 10, WHITE);
        StackPane avBox = new StackPane(av, avIco);
        avBox.setMinSize(28, 28);
        avBox.setMaxSize(28, 28);

        Label msg = new Label(texto);
        msg.setWrapText(true);
        msg.setMaxWidth(260);
        msg.setFont(Font.font("System", 12));
        msg.setTextFill(Color.web("#111827"));
        msg.setPadding(new Insets(10, 14, 10, 14));
        msg.setStyle(
                "-fx-background-color: " + BOT_BG + ";"
                + "-fx-background-radius: 4 16 16 16;"
                + "-fx-border-color: " + BORDER + ";"
                + "-fx-border-width: 1;"
                + "-fx-border-radius: 4 16 16 16;");

        row.getChildren().addAll(avBox, msg);
        messagesBox.getChildren().add(row);
        scrollAbajo();
    }

    // ── Burbuja usuario — fondo sidebar ──────────────────────────
    private void addUserMessage(String texto) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_RIGHT);

        Label msg = new Label(texto);
        msg.setWrapText(true);
        msg.setMaxWidth(260);
        msg.setFont(Font.font("System", 12));
        msg.setTextFill(Color.WHITE);
        msg.setPadding(new Insets(10, 14, 10, 14));
        msg.setStyle(
                "-fx-background-color: #1f3a56;"
                + "-fx-text-fill: white;"
                + "-fx-background-radius: 16 4 16 16;");

        row.getChildren().add(msg);
        messagesBox.getChildren().add(row);
        scrollAbajo();
    }

    // ── Indicador escribiendo ─────────────────────────────────────
    private HBox typingRow;

    private void addTypingIndicator() {
        typingRow = new HBox(8);
        typingRow.setAlignment(Pos.TOP_LEFT);

        Circle av = new Circle(14, Color.web(SIDEBAR_MID));
        Label avIco = faIcon("\uf544", 10, WHITE);
        StackPane avBox = new StackPane(av, avIco);

        Label dots = new Label("● ● ●");
        dots.setStyle(
                "-fx-background-color: " + BOT_BG + ";"
                + "-fx-background-radius: 4 16 16 16;"
                + "-fx-border-color: " + BORDER + ";"
                + "-fx-border-width: 1;"
                + "-fx-border-radius: 4 16 16 16;"
                + "-fx-padding: 10 14;"
                + "-fx-font-size: 10;"
                + "-fx-text-fill: " + GRAY_TEXT + ";");

        typingRow.getChildren().addAll(avBox, dots);
        messagesBox.getChildren().add(typingRow);
        scrollAbajo();
    }

    private void removeTypingIndicator() {
        if (typingRow != null) {
            messagesBox.getChildren().remove(typingRow);
            typingRow = null;
        }
    }

    // ── Llamada a API ─────────────────────────────────────────────
    private void enviarMensaje(String userText) {

        String systemPrompt = """
    Eres Mayita, la asistente virtual de WolertApp, una aplicación colombiana de alertas comunitarias para la ciudad de Valledupar, Cesar.
    Tu personalidad es amable, cercana, clara y profesional. Usas un tono colombiano natural, sin ser informal en exceso.
    Responde siempre en español. Máximo 3 párrafos cortos por respuesta. Si no sabes algo, dilo honestamente.

    === SOBRE WOLERTAPP ===
    WolertApp es un sistema de alertas comunitarias que permite a los ciudadanos de Valledupar reportar incidentes de seguridad,
    recibir notificaciones de su barrio y mantenerse conectados con su comunidad.

    === CÓMO REPORTAR UNA ALERTA ===
    1. En el Dashboard principal usa el BOTÓN DE PÁNICO (rojo).
    2. Se abre un mapa interactivo — selecciona la ubicación del incidente.
    3. Completa tipo de alerta, arma (si aplica), transporte (si aplica), descripción y barrio.
    4. Al enviar, la alerta queda PENDIENTE → EN ATENCIÓN → RESUELTA.

    === OTRAS FUNCIONES ===
    - MIS ALERTAS: historial de tus alertas y su estado.
    - NOTIFICACIONES: alertas enviadas a tu correo.
    - MI CUENTA: datos personales, barrio y suscripciones.
    - MAPA: alertas activas y zonas peligrosas.
    - VECINOS: actividad de tu barrio.

    === EMERGENCIAS EN VALLEDUPAR ===
    Policía 123 · Bomberos 119 · Cruz Roja 132 · Línea Mujer 155
    """;

        new Thread(() -> {
            try {
                JSONObject body = new JSONObject()
                        .put("contents", new JSONArray()
                                .put(new JSONObject()
                                        .put("parts", new JSONArray()
                                                .put(new JSONObject()
                                                        .put("text", systemPrompt + "\n\nUsuario: " + userText)))));

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(
                                "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key="
                                + API_KEY))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                        .build();

                HttpResponse<String> response
                        = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

                JSONObject json = new JSONObject(response.body());
                String reply = json
                        .getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text");

                javafx.application.Platform.runLater(() -> {
                    removeTypingIndicator();
                    addBotMessage(reply);
                });

            } catch (Exception ex) {
                ex.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    removeTypingIndicator();
                    addBotMessage("Lo siento, hubo un error al conectarme. Por favor intenta de nuevo.");
                });
            }
        }).start();
    }

    private void scrollAbajo() {
        javafx.application.Platform.runLater(() -> messagesScroll.setVvalue(1.0));
    }

    // ── Helpers ───────────────────────────────────────────────────
    private Label faIcon(String code, double size, String color) {
        Label lbl = new Label(code);
        lbl.setStyle(
                "-fx-font-family: 'Font Awesome 6 Free Solid';"
                + "-fx-font-size: " + size + "px;"
                + "-fx-text-fill: " + color + ";");
        return lbl;
    }

    private Label label(String text, double size, String color, boolean bold) {
        Label lbl = new Label(text);
        lbl.setFont(bold ? Font.font("System", FontWeight.BOLD, size) : Font.font("System", size));
        lbl.setTextFill(Color.web(color));
        return lbl;
    }
}
