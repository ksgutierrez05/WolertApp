/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.view;

/**
 *
 * @author Maria Cristina
 */
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import java.util.List;
import javafx.animation.KeyFrame;

import sistemagestion.model.*;
import sistemagestion.service.AlertaService;
import sistemagestion.service.BarrioService;

/**
 * Diálogo de emergencia. Al confirmar, llama a AlertaService.insertar() para
 * guardar la alerta en BD.
 *
 * @author Maria Cristina
 */
public class EmergencyDialog {

    // ── Estado del subpanel activo ────────────────────────────────
    private static VBox panelActivo = null;

    // ── Selecciones del usuario ───────────────────────────────────
    private static String tipoAlertaSeleccionado = null;
    private static String tipoArmaSeleccionada = null;
    private static String medioTranspSeleccionado = null;

    /**
     * Abre el diálogo de emergencia.
     *
     * @param owner Stage padre
     * @param usuario Usuario logueado (para username y datos)
     * @param alertaService Servicio para insertar la alerta
     * @param barrioService Servicio para cargar barrios disponibles
     */
    public static void show(
            Stage owner,
            Usuario usuario,
            AlertaService alertaService,
            BarrioService barrioService
    ) {
        // Reset selecciones
        tipoAlertaSeleccionado = null;
        tipoArmaSeleccionada = null;
        medioTranspSeleccionado = null;
        panelActivo = null;

        // ── Root transparente ─────────────────────────────────────
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: rgba(0,0,0,0.45);");

        // ── Modal principal ───────────────────────────────────────
        VBox modal = new VBox(20);
        modal.setAlignment(Pos.TOP_CENTER);
        modal.setPadding(new Insets(25));
        modal.setPrefWidth(460);
        modal.setMinHeight(600);
        modal.setStyle("""
                -fx-background-color: white;
                -fx-background-radius: 20;
                -fx-border-radius: 20;
                """);

        // ── Botón cerrar ──────────────────────────────────────────
        HBox topBar = new HBox();
        topBar.setAlignment(Pos.TOP_RIGHT);
        Button close = new Button("✕");
        close.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 16px; -fx-text-fill: #1a2340;");
        topBar.getChildren().add(close);

        // ── Título ────────────────────────────────────────────────
        Label title = new Label("¿Qué tipo de emergencia tienes?");
        title.setFont(Font.font("System", FontWeight.BOLD, 20));
        title.setTextFill(Color.web("#111827"));

        // ── Botón pánico animado ──────────────────────────────────
        Button panicButton = new Button();
        panicButton.setStyle("-fx-background-color: #ff1f2d; -fx-background-radius: 100; -fx-cursor: hand; -fx-padding: 25;");
        panicButton.setMinSize(120, 120);

        StackPane panicCenter = new StackPane();
        try {
            ImageView shield = new ImageView(
                    new Image(EmergencyDialog.class.getResource("/shield-Photoroom.png").toExternalForm())
            );
            shield.setFitWidth(70);
            shield.setFitHeight(70);
            shield.setPreserveRatio(true);
            panicButton.setGraphic(shield);
        } catch (Exception ignored) {
            Label shieldLbl = new Label("🛡");
            shieldLbl.setFont(Font.font(40));
            panicButton.setGraphic(shieldLbl);
        }

        for (int i = 0; i < 3; i++) {
            Circle wave = new Circle(60);
            wave.setFill(Color.web("#ff1f2d", 0.3));
            wave.setStroke(null);
            panicCenter.getChildren().add(0, wave);
            ScaleTransition scale = new ScaleTransition(Duration.seconds(2), wave);
            scale.setFromX(1);
            scale.setFromY(1);
            scale.setToX(2.5);
            scale.setToY(2.5);
            scale.setCycleCount(Timeline.INDEFINITE);
            scale.setDelay(Duration.seconds(i * 0.6));
            scale.play();
            FadeTransition fade = new FadeTransition(Duration.seconds(2), wave);
            fade.setFromValue(0.6);
            fade.setToValue(0);
            fade.setCycleCount(Timeline.INDEFINITE);
            fade.setDelay(Duration.seconds(i * 0.6));
            fade.play();
        }
        panicCenter.getChildren().add(panicButton);

     
                // ── ComboBox barrio ───────────────────────────────────────
        ComboBox<Barrio> cmbBarrio = new ComboBox<>();
        cmbBarrio.setPromptText("Selecciona el barrio del incidente");
        cmbBarrio.setMaxWidth(Double.MAX_VALUE);
        cmbBarrio.setPrefHeight(40);
        cmbBarrio.setStyle("-fx-background-radius: 8; -fx-font-size: 13px;");
        cmbBarrio.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Barrio item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNombre());
            }
        });
        cmbBarrio.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Barrio item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNombre());
            }
        });
        // Pre-cargar barrio del usuario si lo tiene
        if (barrioService != null) {
            try {
                List<Barrio> barrios = barrioService.listar();
                cmbBarrio.getItems().setAll(barrios);
                if (usuario != null && usuario.getDireccion() != null && usuario.getDireccion().getBarrio() != null) {
                    String miBarrio = usuario.getDireccion().getBarrio().getNombre();
                    barrios.stream().filter(b -> b.getNombre().equalsIgnoreCase(miBarrio)).findFirst()
                            .ifPresent(cmbBarrio::setValue);
                }
            } catch (Exception ignored) {
            }
        }

     
       

        // ── Subpaneles de detalle ─────────────────────────────────
        // ROBO / ASALTO — selecciona tipo arma y medio transporte
        VBox subRobo = crearSubpanel("Detalles del incidente",
                List.of(
                        new OpcionCard("fas-crosshairs", "#ef4444", "#fff1f2", "Arma de fuego", "ARMA"),
                        new OpcionCard("fas-user-slash", "#f97316", "#fff7ed", "Arma blanca", "ARMA"),
                        new OpcionCard("mdi2m-motorbike", "#3b82f6", "#eff6ff", "Motocicleta", "TRANSPORTE"),
                        new OpcionCard("fas-car", "#22c55e", "#ecfdf3", "Automóvil", "TRANSPORTE")
                ), null);

        // PERSONA SOSPECHOSA — descripción libre
        TextArea descSospechoso = new TextArea();
        descSospechoso.setPromptText("Ej: camiseta negra, gorra roja, aprox. 1.80m...");
        descSospechoso.setWrapText(true);
        descSospechoso.setPrefHeight(100);
        descSospechoso.setStyle("-fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: #dbe3ea; -fx-font-size: 13px;");
      
        VBox subSospechoso = crearSubpanel("Descripción del sospechoso", List.of(), descSospechoso);

        // INCENDIO
        CheckBox chkAtrapados = new CheckBox("Hay personas atrapadas");
        chkAtrapados.setStyle("-fx-font-size: 13px;");
       
        VBox subIncendio = crearSubpanel("Tipo de incendio",
                List.of(
                        new OpcionCard("fas-home", "#ef4444", "#fff1f2", "Vivienda", "TIPO"),
                        new OpcionCard("fas-car", "#f97316", "#fff7ed", "Vehículo", "TIPO"),
                        new OpcionCard("fas-tree", "#ca8a04", "#fef9c3", "Zona verde", "TIPO"),
                        new OpcionCard("fas-industry", "#ef4444", "#fff1f2", "Local comercial", "TIPO")
                ), chkAtrapados);

        // RUIDO / ALTERACIÓN
        VBox subRuido = crearSubpanel("Tipo de alteración",
                List.of(
                        new OpcionCard("fas-music", "#d97706", "#fffbeb", "Música / fiesta", "TIPO"),
                        new OpcionCard("fas-fist-raised", "#ef4444", "#fff1f2", "Riña / pelea", "TIPO"),
                        new OpcionCard("fas-dog", "#3b82f6", "#eff6ff", "Animal agresivo", "TIPO"),
                        new OpcionCard("fas-hard-hat", "#16a34a", "#f0fdf4", "Obra / construcción", "TIPO"),
                        new OpcionCard("fas-bullhorn", "#9333ea", "#faf5ff", "Escándalo público", "TIPO"),
                        new OpcionCard("fas-bomb", "#f97316", "#fff7ed", "Detonación / disparo", "TIPO")
                ), null);

        // EMERGENCIA MÉDICA
        VBox subMedica = crearSubpanel("Tipo de emergencia médica",
                List.of(
                        new OpcionCard("fas-heartbeat", "#ef4444", "#fff1f2", "Paro cardíaco", "TIPO"),
                        new OpcionCard("fas-lungs", "#3b82f6", "#eff6ff", "Dific. respiratoria", "TIPO"),
                        new OpcionCard("fas-user-injured", "#d97706", "#fffbeb", "Persona herida", "TIPO"),
                        new OpcionCard("fas-head-side-mask", "#9333ea", "#faf5ff", "Desmayo / convulsión", "TIPO"),
                        new OpcionCard("fas-baby", "#16a34a", "#f0fdf4", "Parto de emergencia", "TIPO"),
                        new OpcionCard("fas-flask", "#f97316", "#fff7ed", "Intoxicación", "TIPO")
                ), null);

        // ACCIDENTE
        CheckBox chkHeridos = new CheckBox("Hay personas heridas");
        chkHeridos.setStyle("-fx-font-size: 13px;");
        chkHeridos.selectedProperty().addListener((obs, o, n) -> {
           
        });
        VBox subAccidente = crearSubpanel("Tipo de accidente",
                List.of(
                        new OpcionCard("fas-car-crash", "#3b82f6", "#eff6ff", "Choque de vehículos", "TIPO"),
                        new OpcionCard("fas-walking", "#ef4444", "#fff1f2", "Atropello", "TIPO"),
                        new OpcionCard("fas-bolt", "#d97706", "#fffbeb", "Cable eléctrico", "TIPO"),
                        new OpcionCard("fas-water", "#16a34a", "#f0fdf4", "Inundación", "TIPO"),
                        new OpcionCard("fas-building", "#9333ea", "#faf5ff", "Derrumbamiento", "TIPO"),
                        new OpcionCard("fas-road", "#16a34a", "#f0fdf4", "Vía obstruida", "TIPO")
                ), chkHeridos);

        List<VBox> todosPaneles = List.of(subRobo, subSospechoso, subIncendio, subRuido, subMedica, subAccidente);

        // ── Tarjetas principales ──────────────────────────────────
        HBox roboCard = createOption("fas-user-slash", "#ff2d2d", "#ffefef", "Robo / Asalto");
        HBox sospechosoCard = createOption("fas-user-secret", "#16a34a", "#ecfdf3", "Sospechoso");
        HBox ruidoCard = createOption("fas-volume-up", "#f59e0b", "#fff7ed", "Ruido / Alteración");
        HBox incendioCard = createOption("fas-fire", "#ef4444", "#fff1f2", "Incendio");
        HBox medicaCard = createOption("fas-plus", "#22c55e", "#ecfdf3", "Emergencia médica");
        HBox accidenteCard = createOption("fas-exclamation-triangle", "#3b82f6", "#eff6ff", "Accidente");

        // Al hacer clic: fija el tipo de alerta y muestra subpanel
        roboCard.setOnMouseClicked(e -> {
            tipoAlertaSeleccionado = "ROBO";
            mostrarSubpanel(subRobo, todosPaneles);
        });
        sospechosoCard.setOnMouseClicked(e -> {
            tipoAlertaSeleccionado = "PERSONA_SOSPECHOSA";
            mostrarSubpanel(subSospechoso, todosPaneles);
        });
        incendioCard.setOnMouseClicked(e -> {
            tipoAlertaSeleccionado = "INCENDIO";
            mostrarSubpanel(subIncendio, todosPaneles);
        });
        ruidoCard.setOnMouseClicked(e -> {
            tipoAlertaSeleccionado = "RUIDO";
            mostrarSubpanel(subRuido, todosPaneles);
        });
        medicaCard.setOnMouseClicked(e -> {
            tipoAlertaSeleccionado = "EMERGENCIA_MEDICA";
            mostrarSubpanel(subMedica, todosPaneles);
        });
        accidenteCard.setOnMouseClicked(e -> {
            tipoAlertaSeleccionado = "ACCIDENTE";
            mostrarSubpanel(subAccidente, todosPaneles);
        });

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(roboCard, 0, 0);
        grid.add(sospechosoCard, 1, 0);
        grid.add(ruidoCard, 0, 1);
        grid.add(incendioCard, 1, 1);
        grid.add(medicaCard, 0, 2);
        grid.add(accidenteCard, 1, 2);

        // Separador visual
        Region sep = new Region();
        sep.setPrefHeight(1);
        sep.setStyle("-fx-background-color: #e5e7eb;");

        modal.getChildren().addAll(
                topBar,
                title,
                panicCenter,
                grid,
                subRobo,
                subSospechoso,
                subIncendio,
                subRuido,
                subMedica,
                subAccidente,
                sep,
                new Label("Ubicación del incidente:") {
            {
                setFont(Font.font("System", FontWeight.BOLD, 13));
                setTextFill(Color.web("#374151"));
            }
        },
                cmbBarrio
               
        );

        ScrollPane scroll = new ScrollPane(modal);
        scroll.setFitToWidth(true);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background: white; -fx-background-color: white;");
        scroll.setPannable(true);

        root.getChildren().add(scroll);

        Scene scene = new Scene(root, 560, 560);
        scene.setFill(Color.TRANSPARENT);

        Stage stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setScene(scene);

        owner.getScene().getRoot().setEffect(new BoxBlur(8, 8, 3));
        stage.setOnHidden(e -> owner.getScene().getRoot().setEffect(null));
        close.setOnAction(e -> stage.close());
        stage.show();
    }

    // ── Toggle subpanel ───────────────────────────────────────────
    private static void mostrarSubpanel(VBox nuevo, List<VBox> todos) {
        for (VBox p : todos) {
            p.setVisible(false);
            p.setManaged(false);
        }
        if (panelActivo == nuevo) {
            panelActivo = null;
        } else {
            nuevo.setVisible(true);
            nuevo.setManaged(true);
            panelActivo = nuevo;
        }
    }

    // ── Constructor de subpanel ───────────────────────────────────
    private static VBox crearSubpanel(String tituloTexto, List<OpcionCard> opciones, javafx.scene.Node extraNodo) {
        VBox panel = new VBox(12);
        panel.setVisible(false);
        panel.setManaged(false);
        panel.setPadding(new Insets(10, 0, 0, 0));

        Label titulo = new Label(tituloTexto);
        titulo.setFont(Font.font("System", FontWeight.BOLD, 15));
        titulo.setTextFill(Color.web("#111827"));
        panel.getChildren().add(titulo);

        if (!opciones.isEmpty()) {
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            int col = 0, row = 0;
            for (OpcionCard op : opciones) {
                HBox card = createOption(op.iconCode, op.iconColor, op.bg, op.label);
                // Al hacer clic guarda el valor según tipo
                final String val = op.label;
                final String tipo = op.tipo;
                card.setOnMouseClicked(e -> {
                    if ("ARMA".equals(tipo)) {
                        tipoArmaSeleccionada = val;
                    } else if ("TRANSPORTE".equals(tipo)) {
                        medioTranspSeleccionado = val;
                    }
                    // Para TIPO simplemente queda como detalle en descripción
                    resaltarSeleccion(card, grid);
                });
                grid.add(card, col, row);
                col++;
                if (col == 2) {
                    col = 0;
                    row++;
                }
            }
            panel.getChildren().add(grid);
        }
        if (extraNodo != null) {
            panel.getChildren().add(extraNodo);
        }
        return panel;
    }

    // ── Resalta la tarjeta seleccionada ───────────────────────────
    private static void resaltarSeleccion(HBox seleccionada, GridPane grid) {
        grid.getChildren().forEach(n -> {
            if (n instanceof HBox h) {
                h.setStyle(cardStyle(false));
            }
        });
        seleccionada.setStyle(cardStyle(true));
    }

    // ── Tarjeta con emoji (sin dependencia ikonli) ────────────────
    private static HBox createOption(String iconCode, String iconColor, String bg, String text) {
        HBox card = new HBox(14);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(10));
        card.setPrefSize(200, 65);
        card.setStyle(cardStyle(false));

        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(38, 38);
        iconBox.setStyle("-fx-background-radius: 14; -fx-background-color: " + bg + ";");

        // Mapeo FontAwesome code → emoji
        String emoji = iconToEmoji(iconCode);
        Label iconLbl = new Label(emoji);
        iconLbl.setFont(Font.font(18));
        iconBox.getChildren().add(iconLbl);

        Label txt = new Label(text);
        txt.setFont(Font.font("System", FontWeight.BOLD, 13));
        txt.setTextFill(Color.web("#111827"));
        txt.setWrapText(true);
        txt.setMaxWidth(130);
        card.getChildren().addAll(iconBox, txt);
        card.setOnMouseEntered(e -> card.setStyle(cardStyle(true)));
        card.setOnMouseExited(e -> card.setStyle(cardStyle(false)));
        return card;
    }

    /**
     * Convierte códigos FontAwesome usados en este diálogo a emojis
     * equivalentes.
     */
    private static String iconToEmoji(String code) {
        return switch (code) {
            case "fas-user-slash" ->
                "🚫";
            case "fas-user-secret" ->
                "🕵";
            case "fas-crosshairs" ->
                "🎯";
            case "fas-volume-up" ->
                "📢";
            case "fas-fire" ->
                "🔥";
            case "fas-plus" ->
                "➕";
            case "fas-exclamation-triangle" ->
                "⚠";
            case "fas-home" ->
                "🏠";
            case "fas-car" ->
                "🚗";
            case "fas-car-crash" ->
                "💥";
            case "fas-tree" ->
                "🌳";
            case "fas-industry" ->
                "🏭";
            case "fas-music" ->
                "🎵";
            case "fas-fist-raised" ->
                "✊";
            case "fas-dog" ->
                "🐕";
            case "fas-hard-hat" ->
                "⛑";
            case "fas-bullhorn" ->
                "📣";
            case "fas-bomb" ->
                "💣";
            case "fas-heartbeat" ->
                "❤";
            case "fas-lungs" ->
                "🫁";
            case "fas-user-injured" ->
                "🤕";
            case "fas-head-side-mask" ->
                "😷";
            case "fas-baby" ->
                "👶";
            case "fas-flask" ->
                "⚗";
            case "fas-walking" ->
                "🚶";
            case "fas-bolt" ->
                "⚡";
            case "fas-water" ->
                "🌊";
            case "fas-building" ->
                "🏗";
            case "fas-road" ->
                "🛣";
            case "mdi2m-motorbike" ->
                "🏍";
            default ->
                "🔴";
        };
    }

    private static String cardStyle(boolean hover) {
        return "-fx-background-color: " + (hover ? "#f9fafb" : "white") + ";"
                + "-fx-background-radius: 16; -fx-border-color: " + (hover ? "#cfd8e3" : "#dbe3ea") + ";"
                + "-fx-border-radius: 16; -fx-cursor: hand;";
    }

    // ── Record auxiliar ───────────────────────────────────────────
    private record OpcionCard(String iconCode, String iconColor, String bg, String label, String tipo) {

    }
}
