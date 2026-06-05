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
import sistemagestion.model.Notificacion;

import sistemagestion.service.NotificacionService;

public class NotificacionesAdminView {

    private static final String BG = "#f4f6fb";
    private static final String GRAY_TEXT = "#6b7280";
    private final NotificacionService notificacionService;

    public NotificacionesAdminView(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    public ScrollPane getView() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color: " + BG + ";");

        content.getChildren().addAll(buildTopBar(), buildTabla());

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: " + BG + "; -fx-background: " + BG + ";");
        return scroll;
    }

    private HBox buildTopBar() {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Notificaciones del sistema");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#111827"));
        bar.getChildren().add(title);
        return bar;
    }

    private VBox buildTabla() {
        VBox card = new VBox();
        card.setStyle(
                "-fx-background-color: white;"
                + "-fx-background-radius: 12;");
        card.setEffect(new DropShadow(12, Color.rgb(0, 0, 0, 0.10)));

        List<Notificacion> notifs = cargarNotificaciones();

        if (notifs.isEmpty()) {
            VBox empty = new VBox(8);
            empty.setAlignment(Pos.CENTER);
            empty.setPadding(new Insets(48));
            Label ico = new Label("🔔");
            ico.setFont(Font.font(36));
            Label msg = label("No hay notificaciones registradas.", 14, GRAY_TEXT, false);
            empty.getChildren().addAll(ico, msg);
            card.getChildren().add(empty);
            return card;
        }

        TableView<Notificacion> tabla = new TableView<>();
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.setStyle(
                "-fx-background-color: white;"
                + "-fx-background-radius: 12;"
                + "-fx-border-color: transparent;"
                + "-fx-table-cell-border-color: #f3f4f6;");
        tabla.setFixedCellSize(48);
        tabla.prefHeightProperty().bind(
                tabla.fixedCellSizeProperty()
                        .multiply(javafx.beans.binding.Bindings.size(tabla.getItems()).add(1.12)));
        tabla.setMinHeight(0);
        tabla.setMaxHeight(Double.MAX_VALUE);

        // Columna Mensaje
        TableColumn<Notificacion, String> colMensaje = new TableColumn<>("Mensaje");
        colMensaje.setCellValueFactory(c -> {
            String m = c.getValue().getMensaje();
            return new javafx.beans.property.SimpleStringProperty(m != null ? m : "—");
        });
        colMensaje.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                setText(item.length() > 60 ? item.substring(0, 60) + "…" : item);
                setStyle("-fx-font-size:13px; -fx-text-fill:#111827; -fx-padding:0 12;");
            }
        });
        colMensaje.setMaxWidth(1f * Integer.MAX_VALUE * 45);  // 45% ancho

        // Columna Destinatario
        TableColumn<Notificacion, String> colDest = new TableColumn<>("Destinatario");
        colDest.setCellValueFactory(c -> {
            String d = c.getValue().getCorreodestinatario();
            return new javafx.beans.property.SimpleStringProperty(d != null ? d : "—");
        });
        colDest.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                setText(item);
                setStyle("-fx-font-size:13px; -fx-text-fill:#374151; -fx-padding:0 12;");
            }
        });
        colDest.setMaxWidth(1f * Integer.MAX_VALUE * 28);  // 28%

        // Columna Fecha
        TableColumn<Notificacion, String> colFecha = new TableColumn<>("Fecha");
        colFecha.setCellValueFactory(c -> {
            String f = c.getValue().getFechahora() != null
                    ? c.getValue().getFechahora()
                            .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                    : "—";
            return new javafx.beans.property.SimpleStringProperty(f);
        });
        colFecha.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                setText(item);
                setStyle("-fx-font-size:13px; -fx-text-fill:#6b7280; -fx-padding:0 12;");
            }
        });
        colFecha.setMaxWidth(1f * Integer.MAX_VALUE * 15);  // 15%

        // Columna Estado (con badge de color)
        TableColumn<Notificacion, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(c -> {
            String e = c.getValue().getEstado() != null
                    ? c.getValue().getEstado().name() : "—";
            return new javafx.beans.property.SimpleStringProperty(e);
        });
        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }
                Label badge = new Label(item.replace("_", " "));
                String bg, fg;
                switch (item) {
                    case "ENVIADA" -> {
                        bg = "#e8f5e9";
                        fg = "#2e7d32";
                    }
                    case "PENDIENTE" -> {
                        bg = "#fff8e1";
                        fg = "#f57f17";
                    }
                    case "FALLIDA" -> {
                        bg = "#ffebee";
                        fg = "#c62828";
                    }
                    default -> {
                        bg = "#f1f5f9";
                        fg = "#475569";
                    }
                }
                badge.setStyle(
                        "-fx-background-color:" + bg + ";"
                        + "-fx-text-fill:" + fg + ";"
                        + "-fx-font-size:11px;"
                        + "-fx-font-weight:bold;"
                        + "-fx-background-radius:20;"
                        + "-fx-padding:4 10;");
                setGraphic(badge);
                setStyle("-fx-padding:0 12; -fx-alignment:center-left;");
            }
        });
        colEstado.setMaxWidth(1f * Integer.MAX_VALUE * 12);  // 12%

        tabla.getColumns().addAll(colMensaje, colDest, colFecha, colEstado);
        tabla.getItems().addAll(notifs);

        // Estilo del header y filas alternas vía CSS inline
        tabla.getStylesheets().add(
                "data:text/css,"
                + ".table-view .column-header { -fx-background-color:#f8fafc; -fx-border-color:transparent transparent #e5e7eb transparent; -fx-border-width:0 0 1 0; }"
                + ".table-view .column-header .label { -fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:#6b7280; -fx-padding:0 12; }"
                + ".table-view .table-row-cell:even { -fx-background-color:white; }"
                + ".table-view .table-row-cell:odd  { -fx-background-color:#fafbfd; }"
                + ".table-view .table-row-cell:hover { -fx-background-color:#f0f4ff; }"
                + ".table-view .table-row-cell:selected { -fx-background-color:#e8f0fe; }"
                + ".table-view .scroll-bar { -fx-opacity:0; }"
                + ".table-view .corner { -fx-background-color:#f8fafc; }"
        );

        card.getChildren().add(tabla);
        return card;
    }

   

    private List<Notificacion> cargarNotificaciones() {
        try {
            return notificacionService.listar();
        } catch (Exception e) {
            return List.of();
        }
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
