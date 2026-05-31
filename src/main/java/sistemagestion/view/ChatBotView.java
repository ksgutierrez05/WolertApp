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
import javafx.stage.Popup;
import javafx.stage.Stage;
import java.net.URI;
import java.net.http.*;
import java.util.ArrayList;
import java.util.List;
import org.json.*;
import sistemagestion.model.Usuario;

public class ChatBotView {

 private static final String  API_KEY= "";

    private static final String WHITE = "#ffffff";
    private static final String BG = "#f4f6fb";
    private static final String BLUE = "#1565c0";
    private static final String BLUE_DARK = "#0d47a1";
    private static final String GRAY_TEXT = "#6b7280";
    private static final String BORDER = "#e5e7eb";
    private static final String BOT_BG = "#f0f4ff";
    private static final String USER_BG = "#1565c0";

    private final Usuario usuarioActual;
    private VBox messagesBox;
    private ScrollPane messagesScroll;
    private final List<JSONObject> historial = new ArrayList<>();

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

    // ── Header ───────────────────────────────────────────────────
    private HBox buildHeader(Popup popup) {
        HBox header = new HBox(10);
        header.setPadding(new Insets(16, 16, 14, 16));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle(
                "-fx-background-color: " + BLUE + ";"
                + "-fx-background-radius: 20 20 0 0;");

        // Avatar bot
        Circle av = new Circle(18, Color.web("#ffffff22"));
        Label avIco = faIcon("\uf544", 14, WHITE); // robot FA
        StackPane avBox = new StackPane(av, avIco);

        VBox info = new VBox(1);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label name = label("Mayita", 13, WHITE, true);
        HBox statusRow = new HBox(5);
        statusRow.setAlignment(Pos.CENTER_LEFT);
        Circle dot = new Circle(4, Color.web("#69f0ae"));
        Label statusLbl = label("En línea", 10, "#c8e6c9", false);
        statusRow.getChildren().addAll(dot, statusLbl);
        info.getChildren().addAll(name, statusRow);

        // Botón cerrar
        Button closeBtn = new Button("✕");
        closeBtn.setStyle(
                "-fx-background-color: rgba(255,255,255,0.15);"
                + "-fx-text-fill: white;"
                + "-fx-font-size: 12px;"
                + "-fx-background-radius: 20;"
                + "-fx-padding: 4 8;"
                + "-fx-cursor: hand;");
        closeBtn.setOnMouseEntered(e -> closeBtn.setStyle(
                "-fx-background-color: rgba(255,255,255,0.25);"
                + "-fx-text-fill: white;"
                + "-fx-font-size: 12px;"
                + "-fx-background-radius: 20;"
                + "-fx-padding: 4 8;"
                + "-fx-cursor: hand;"));
        closeBtn.setOnMouseExited(e -> closeBtn.setStyle(
                "-fx-background-color: rgba(255,255,255,0.15);"
                + "-fx-text-fill: white;"
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
        Label sendIco = faIcon("\uf1d8", 13, WHITE);
        sendBtn.setGraphic(sendIco);
        sendBtn.setStyle(
                "-fx-background-color: " + BLUE + ";"
                + "-fx-background-radius: 20;"
                + "-fx-padding: 8 14;"
                + "-fx-cursor: hand;");
        sendBtn.setOnMouseEntered(e -> sendBtn.setStyle(
                "-fx-background-color: " + BLUE_DARK + ";"
                + "-fx-background-radius: 20;"
                + "-fx-padding: 8 14;"
                + "-fx-cursor: hand;"));
        sendBtn.setOnMouseExited(e -> sendBtn.setStyle(
                "-fx-background-color: " + BLUE + ";"
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

        Circle av = new Circle(14, Color.web(BLUE));
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

    // ── Burbuja usuario ───────────────────────────────────────────
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
                "-fx-background-color: " + USER_BG + ";"
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

        Circle av = new Circle(14, Color.web(BLUE));
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

    // ── Llamada a  API ──────────────────────────────────────
    private void enviarMensaje(String userText) {

        String systemPrompt = """
        Eres Mayita de WolertApp, una app colombiana de alertas comunitarias.
        Ayudas a ciudadanos con:
        - Cómo reportar alertas (robos, peleas, animales, infraestructura)
        - Información sobre barrios, comunas y zonas de Valledupar
        - Cómo usar las funciones de la app (mis alertas, suscripciones, notificaciones)
        - Consejos de seguridad comunitaria
        Responde siempre en español, de forma amable, concisa y clara.
        Máximo 3 párrafos cortos por respuesta.
        """;

        new Thread(() -> {
            try {

                JSONObject body = new JSONObject()
                        .put("contents", new JSONArray()
                                .put(new JSONObject()
                                        .put("parts", new JSONArray()
                                                .put(new JSONObject()
                                                        .put("text",
                                                                systemPrompt
                                                                + "\n\nUsuario: " + userText)
                                                )
                                        )
                                )
                        );

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(
                                "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key="
                                + API_KEY))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                        .build();

                HttpResponse<String> response
                        = HttpClient.newHttpClient()
                                .send(request, HttpResponse.BodyHandlers.ofString());

                System.out.println("Código: " + response.statusCode());
                System.out.println(response.body());

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
                    addBotMessage(
                            "Lo siento, hubo un error al conectarme. Por favor intenta de nuevo."
                    );
                });
            }
        }).start();
    }

    private void scrollAbajo() {
        javafx.application.Platform.runLater(()
                -> messagesScroll.setVvalue(1.0));
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
        lbl.setFont(bold ? Font.font("System", FontWeight.BOLD, size)
                : Font.font("System", size));
        lbl.setTextFill(Color.web(color));
        return lbl;
    }
}
