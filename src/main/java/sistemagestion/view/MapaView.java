/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.view;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingNode;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCenter;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;

import javax.swing.SwingUtilities;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.List;

public class MapaView {

    // ── Paleta ────────────────────────────────────────────────────────────────
    private static final String C_INPUT_BG = "#f7f7f7";
    private static final String C_INPUT_TEXT = "#4b5563";
    private static final String C_LABEL = "#374151";
    private static final String C_BLUE_ICON = "#3b82f6";
    private static final String C_COORD_BG = "#f0f4ff";
    private static final String C_COORD_BDR = "#c7d7fd";
    private static final String C_COORD_VAL = "#1e3a8a";
    private static final String C_SEPARATOR = "#e5e7eb";
    private static final String C_DARK_GRAD = "linear-gradient(to right, #16283d, #1f3a56)";

    // ── Estado ────────────────────────────────────────────────────────────────
    private JXMapViewer mapa;
    private GeoPosition posicionSeleccionada;

    // ── Controles ─────────────────────────────────────────────────────────────
    private TextField txtNombre;
    private ComboBox<String> cmbBarrio;
    private Slider sliderRadio;
    private Label lblRadioValor;
    private ComboBox<String> cmbEstado;
    private Label lblCoordenadas;
    private Label lblInstruccionHeader;
    private Label lblCoordFooter;

    private final List<String> BARRIOS = Arrays.asList(
            "Alfonso López", "La Esperanza", "El Cañaguate",
            "Los Músicos", "Novalito", "Siete de Agosto",
            "La Nevada", "El Cedral", "Simón Bolívar",
            "Popular", "Los Cortijos", "Villa Corelca",
            "Cevillar", "Caracolí", "La Castellana"
    );

    // ─────────────────────────────────────────────────────────────────────────
    public void mostrar() {
        Stage stage = new Stage();
        stage.setTitle("WolertApp — Registrar Alarma");

        // 1. BARRA SUPERIOR
        javafx.scene.image.ImageView logoImg = new javafx.scene.image.ImageView();
        try {
            java.awt.image.BufferedImage raw = javax.imageio.ImageIO.read(
                    getClass().getResourceAsStream("/LogoWolertApp.png"));
            java.awt.image.BufferedImage cropped = recortarTransparencia(raw);
            logoImg.setImage(javafx.embed.swing.SwingFXUtils.toFXImage(cropped, null));
        } catch (Exception ignored) {
        }
        logoImg.setFitHeight(24);
        logoImg.setFitWidth(24);
        logoImg.setPreserveRatio(true);

        StackPane logoBox = new StackPane(logoImg);
        logoBox.setStyle(
                "-fx-background-color: " + C_DARK_GRAD + ";"
                + "-fx-background-radius: 6;"
                + "-fx-padding: 6;"
        );

        Label logoText = new Label("WolertApp");
        logoText.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        logoText.setTextFill(Color.web("#111827"));

        Region separadorH = new Region();
        separadorH.setStyle("-fx-background-color: " + C_SEPARATOR + ";");
        separadorH.setPrefWidth(1);
        separadorH.setPrefHeight(20);

        lblInstruccionHeader = new Label("Haz clic en el mapa para ubicar la alarma");
        lblInstruccionHeader.setFont(Font.font("Arial", 13));
        lblInstruccionHeader.setTextFill(Color.web("#6b7280"));

        HBox topBar = new HBox(10, logoBox, logoText, separadorH, new Label("📍"), lblInstruccionHeader);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(12, 20, 12, 20));
        topBar.setStyle(
                "-fx-background-color: white;"
                + "-fx-border-color: " + C_SEPARATOR + ";"
                + "-fx-border-width: 0 0 1 0;"
        );

        // 2. MAPA
        SwingNode swingNode = new SwingNode();
        SwingUtilities.invokeLater(() -> {
            mapa = new JXMapViewer();
            TileFactoryInfo info = new TileFactoryInfo(
                    1, 15, 17, 256, true, true,
                    "https://tile.openstreetmap.org", "x", "y", "z"
            ) {
                @Override
                public String getTileUrl(int x, int y, int zoom) {
                    return baseURL + "/" + (17 - zoom) + "/" + x + "/" + y + ".png";
                }
            };
            mapa.setTileFactory(new DefaultTileFactory(info));
            mapa.setAddressLocation(new GeoPosition(10.4795, -73.2536));
            mapa.setZoom(4);

            PanMouseInputListener pan = new PanMouseInputListener(mapa);
            mapa.addMouseListener(pan);
            mapa.addMouseMotionListener(pan);
            mapa.addMouseWheelListener(new ZoomMouseWheelListenerCenter(mapa));

            mapa.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    posicionSeleccionada = mapa.convertPointToGeoPosition(e.getPoint());
                    mapa.repaint();
                    String coord = String.format("%.5f,  %.5f",
                            posicionSeleccionada.getLatitude(),
                            posicionSeleccionada.getLongitude());
                    Platform.runLater(() -> {
                        lblCoordenadas.setText(coord);
                        lblCoordFooter.setText("📍  " + coord);
                        lblInstruccionHeader.setText("Ubicación seleccionada");
                    });
                }
            });

            mapa.setOverlayPainter((Graphics2D g, JXMapViewer map, int w, int h) -> {
                if (posicionSeleccionada == null) {
                    return;
                }
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Point2D pt = map.getTileFactory().geoToPixel(posicionSeleccionada, map.getZoom());
                int cx = (int) pt.getX() - map.getViewportBounds().x;
                int cy = (int) pt.getY() - map.getViewportBounds().y;

                // Círculo de cobertura
                int radioM = (int) sliderRadio.getValue();
                if (radioM > 0) {
                    double mpp = 156543.03392
                            * Math.cos(Math.toRadians(posicionSeleccionada.getLatitude()))
                            / Math.pow(2, 17 - map.getZoom());
                    int rp = (int) (radioM / mpp);
                    g.setColor(new java.awt.Color(255, 255, 255, 60));
                    g.fill(new Ellipse2D.Double(cx - rp, cy - rp, rp * 2, rp * 2));
                    g.setColor(new java.awt.Color(59, 130, 246, 200));
                    g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND,
                            BasicStroke.JOIN_ROUND, 0, new float[]{7, 5}, 0));
                    g.draw(new Ellipse2D.Double(cx - rp, cy - rp, rp * 2, rp * 2));
                }

                // Marcador PNG
                try {
                    java.io.InputStream is = getClass().getResourceAsStream("/SirenaPin.png");
                    if (is != null) {
                        java.awt.image.BufferedImage img = recortarTransparencia(
                                javax.imageio.ImageIO.read(is));
                        int iw = 32;
                        int ih = (int) (img.getHeight() * (32.0 / img.getWidth()));
                        g.drawImage(img, cx - iw / 2, cy - ih, iw, ih, null);
                    }
                } catch (Exception ignored) {
                }
            });

            swingNode.setContent(mapa);
        });

        // Flotante de coordenadas
        lblCoordFooter = new Label("📍  —,  —");
        lblCoordFooter.setFont(Font.font("Arial", 12));
        lblCoordFooter.setTextFill(Color.web("#374151"));
        lblCoordFooter.setStyle(
                "-fx-background-color: rgba(255,255,255,0.92);"
                + "-fx-background-radius: 20;"
                + "-fx-padding: 6 18 6 18;"
                + "-fx-border-color: " + C_SEPARATOR + ";"
                + "-fx-border-radius: 20;"
                + "-fx-border-width: 1;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 8, 0, 0, 2);"
        );

        StackPane mapaStack = new StackPane(swingNode, lblCoordFooter);
        StackPane.setAlignment(lblCoordFooter, Pos.BOTTOM_CENTER);
        StackPane.setMargin(lblCoordFooter, new Insets(0, 0, 18, 0));

      
        // 3. PANEL DERECHO

// Header del panel
        Label panelIconBg = new Label("🔔");
        panelIconBg.setFont(Font.font(18));
        panelIconBg.setTextFill(Color.WHITE);
        panelIconBg.setStyle(
                "-fx-background-color: " + C_DARK_GRAD + ";"
                + "-fx-background-radius: 8;"
                + "-fx-padding: 6 10 6 10;"
        );
        Label panelTitulo = new Label("Registrar alarma");
        panelTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        panelTitulo.setTextFill(Color.web("#111827"));

        Label panelSub = new Label("Completa los datos y ubica en el mapa");
        panelSub.setFont(Font.font("Arial", 11));
        panelSub.setTextFill(Color.web("#6b7280"));

        HBox panelHeader = new HBox(12, panelIconBg, new VBox(2, panelTitulo, panelSub));
        panelHeader.setAlignment(Pos.CENTER_LEFT);
        panelHeader.setPadding(new Insets(16, 20, 16, 20));
        panelHeader.setStyle(
                "-fx-background-color: white;"
                + "-fx-border-color: " + C_SEPARATOR + ";"
                + "-fx-border-width: 0 0 1 0;"
        );

        // Campos
        txtNombre = new TextField();
        txtNombre.setPromptText("Ej: Alarma Sector Norte");
        estilizarInput(txtNombre);

        cmbBarrio = new ComboBox<>(FXCollections.observableArrayList(BARRIOS));
        cmbBarrio.setPromptText("Seleccione un barrio");
        cmbBarrio.setMaxWidth(Double.MAX_VALUE);
        estilizarCombo(cmbBarrio);

        sliderRadio = new Slider(50, 2000, 500);
        sliderRadio.setBlockIncrement(50);
        sliderRadio.setStyle(
                "-fx-accent: #16283d;"
                + "-fx-color: #16283d;"
                + "-fx-control-inner-background: #dde3ea;"
                + "-fx-background-color: transparent;"
        );
       
        sliderRadio.skinProperty().addListener((obs, ov, nv) -> {
            javafx.scene.layout.StackPane track
                    = (javafx.scene.layout.StackPane) sliderRadio.lookup(".track");
            if (track != null) {
                track.setMaxHeight(3);
                track.setPrefHeight(3);
                track.setStyle(
                        "-fx-background-color: linear-gradient(to right, #16283d, #1f3a56);"
                        + "-fx-background-radius: 3;"
                );
            }
            javafx.scene.layout.StackPane thumb
                    = (javafx.scene.layout.StackPane) sliderRadio.lookup(".thumb");
            if (thumb != null) {
                thumb.setStyle(
                        "-fx-background-color: linear-gradient(to bottom, #1f3a56, #16283d);"
                        + "-fx-background-radius: 50%;"
                        + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 4, 0, 0, 1);"
                );
            }
        });

        TextField txtRadioManual = new TextField("500");
        txtRadioManual.setPrefWidth(72);
        txtRadioManual.setPrefHeight(28);
        txtRadioManual.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        txtRadioManual.setStyle(
                "-fx-background-color: #f1f5f9;"
                + "-fx-text-fill: #374151;"
                + "-fx-border-color: #e2e8f0;"
                + "-fx-border-width: 1;"
                + "-fx-border-radius: 6;"
                + "-fx-background-radius: 6;"
                + "-fx-padding: 4 10 4 10;"
                + "-fx-alignment: center;"
        );

        Label lblM = new Label("m");
        lblM.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        lblM.setTextFill(Color.web("#374151"));
        
        // Slider
        sliderRadio.valueProperty().addListener((obs, ov, nv) -> {
            int v = nv.intValue();
            txtRadioManual.setText(String.valueOf(v));
            if (mapa != null) {
                SwingUtilities.invokeLater(() -> mapa.repaint());
            }
        });

        // Input 
        javafx.event.EventHandler<javafx.event.ActionEvent> aplicarManual = e -> {
            try {
                int v = Integer.parseInt(txtRadioManual.getText().trim());
                v = Math.max(50, Math.min(2000, v));
                sliderRadio.setValue(v);
                txtRadioManual.setText(String.valueOf(v));
            } catch (NumberFormatException ignored) {
                txtRadioManual.setText(String.valueOf((int) sliderRadio.getValue()));
            }
        };
        txtRadioManual.setOnAction(aplicarManual);
        txtRadioManual.focusedProperty().addListener((obs, ov, focused) -> {
            if (!focused) {
                aplicarManual.handle(null);
            }
        });

        // Referencia para limpiarFormulario
        lblRadioValor = new Label(); 

        HBox radioRow = new HBox(10, sliderRadio, txtRadioManual, lblM);
        radioRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(sliderRadio, Priority.ALWAYS);

        cmbEstado = new ComboBox<>(FXCollections.observableArrayList(
                "activo", "inactivo", "mantenimiento"));
        cmbEstado.setPromptText("Seleccione estado");
        cmbEstado.setMaxWidth(Double.MAX_VALUE);
        estilizarCombo(cmbEstado);

        // Caja coordenadas
        lblCoordenadas = new Label("—,  —");
        lblCoordenadas.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        lblCoordenadas.setTextFill(Color.web(C_COORD_VAL));

        Label lblCoordTitulo = new Label("Ubicación seleccionada");
        lblCoordTitulo.setFont(Font.font("Arial", 11));
        lblCoordTitulo.setTextFill(Color.web("#6b7280"));

        HBox coordBox = new HBox(10, new Label("📍"), new VBox(2, lblCoordTitulo, lblCoordenadas));
        coordBox.setAlignment(Pos.CENTER_LEFT);
        coordBox.setPadding(new Insets(12, 14, 12, 14));
        coordBox.setStyle(
                "-fx-background-color: " + C_COORD_BG + ";"
                + "-fx-border-color: " + C_COORD_BDR + ";"
                + "-fx-border-width: 1;"
                + "-fx-border-radius: 10;"
                + "-fx-background-radius: 10;"
        );

        // Botones
        Button btnGuardar = new Button("Guardar alarma");
        btnGuardar.setMaxWidth(Double.MAX_VALUE);
        btnGuardar.setPrefHeight(44);
        btnGuardar.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        btnGuardar.setStyle(
                "-fx-background-color: " + C_DARK_GRAD + ";"
                + "-fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 8;");
        btnGuardar.setOnMouseEntered(e -> btnGuardar.setStyle(
                "-fx-background-color: #1f3a56;"
                + "-fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 8;"));
        btnGuardar.setOnMouseExited(e -> btnGuardar.setStyle(
                "-fx-background-color: " + C_DARK_GRAD + ";"
                + "-fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 8;"));
        btnGuardar.setOnAction(e -> guardarAlarma());

        Button btnLimpiar = new Button("Limpiar");
        btnLimpiar.setMaxWidth(Double.MAX_VALUE);
        btnLimpiar.setPrefHeight(38);
        btnLimpiar.setFont(Font.font("Arial", 12));
        btnLimpiar.setStyle(
                "-fx-background-color: white;"
                + "-fx-text-fill: #6b7280;"
                + "-fx-border-color: #d1d5db;"
                + "-fx-border-width: 1;"
                + "-fx-cursor: hand;"
                + "-fx-background-radius: 8;"
                + "-fx-border-radius: 8;"
        );
        btnLimpiar.setOnAction(e -> limpiarFormulario());

        VBox form = new VBox(14,
                campo("Nombre de la alarma", txtNombre),
                campo("Barrio", cmbBarrio),
                campo("Radio de cobertura (m)", radioRow),
                campo("Estado", cmbEstado),
                coordBox,
                btnGuardar,
                btnLimpiar
        );
        form.setPadding(new Insets(18, 20, 18, 20));
        form.setStyle("-fx-background-color: white;");

        ScrollPane scroll = new ScrollPane(form);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: white; -fx-background: white;");

        VBox panelDerecho = new VBox(panelHeader, scroll);
        panelDerecho.setPrefWidth(320);
        panelDerecho.setMaxWidth(320);
        panelDerecho.setStyle(
                "-fx-background-color: white;"
                + "-fx-border-color: " + C_SEPARATOR + ";"
                + "-fx-border-width: 0 0 0 1;"
        );
        VBox.setVgrow(scroll, Priority.ALWAYS);

        // 4. LAYOUT RAÍZ
        BorderPane center = new BorderPane();
        center.setCenter(mapaStack);
        center.setRight(panelDerecho);

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(center);
        root.setStyle("-fx-background-color: white;");

        Scene scene = new Scene(root, 1100, 680);
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();
    }


    private VBox campo(String etiqueta, javafx.scene.Node control) {
        Label lbl = new Label(etiqueta);
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        lbl.setTextFill(Color.web(C_LABEL));
        return new VBox(6, lbl, control);
    }

    private void estilizarInput(TextField tf) {
        tf.setStyle(
                "-fx-background-color: " + C_INPUT_BG + ";"
                + "-fx-text-fill: " + C_INPUT_TEXT + ";"
                + "-fx-prompt-text-fill: #9ca3af;"
                + "-fx-border-color: transparent;"
                + "-fx-border-radius: 30;"
                + "-fx-background-radius: 30;"
                + "-fx-padding: 14 20 14 20;"
                + "-fx-font-size: 14px;"
        );
        tf.setPrefHeight(48);
    }

    private void estilizarCombo(ComboBox<String> cb) {
        cb.setStyle(
                "-fx-background-color: " + C_INPUT_BG + ";"
                + "-fx-text-fill: " + C_INPUT_TEXT + ";"
                + "-fx-prompt-text-fill: #9ca3af;"
                + "-fx-border-color: transparent;"
                + "-fx-border-radius: 30;"
                + "-fx-background-radius: 30;"
                + "-fx-font-size: 14px;"
        );
        cb.setPrefHeight(48);

        // Hover de las celdas del dropdown
        cb.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
                setStyle(
                        "-fx-background-color: transparent;"
                        + "-fx-text-fill: #4b5563;"
                        + "-fx-font-size: 14px;"
                        + "-fx-padding: 8 14 8 14;"
                );
                setOnMouseEntered(e -> setStyle(
                        "-fx-background-color: linear-gradient(to right, #16283d, #1f3a56);"
                        + "-fx-background-radius: 6;"
                        + "-fx-text-fill: white;"
                        + "-fx-font-size: 14px;"
                        + "-fx-padding: 8 14 8 14;"
                ));
                setOnMouseExited(e -> setStyle(
                        "-fx-background-color: transparent;"
                        + "-fx-text-fill: #4b5563;"
                        + "-fx-font-size: 14px;"
                        + "-fx-padding: 8 14 8 14;"
                ));
            }
        });

        cb.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null) {
                    setStyle(
                            "-fx-background-color: " + C_INPUT_BG + ";"
                            + "-fx-background-radius: 30;"
                            + "-fx-text-fill: black;"
                            + "-fx-font-size: 14px;"
                            + "-fx-padding: 4 14 4 14;"
                    );
                } else {
                    setStyle(
                            "-fx-background-color: transparent;"
                            + "-fx-text-fill: #9ca3af;"
                            + "-fx-font-size: 14px;"
                    );
                }
                setText(empty || item == null ? cb.getPromptText() : item);
            }
        });
    }

    // ── Lógica de negocio ─────────────────────────────────────────────────────
    private void guardarAlarma() {
        if (txtNombre.getText().trim().isEmpty()) {
            alerta("Ingrese el nombre.");
            return;
        }
        if (cmbBarrio.getValue() == null) {
            alerta("Seleccione un barrio.");
            return;
        }
        if (posicionSeleccionada == null) {
            alerta("Seleccione ubicación en el mapa.");
            return;
        }
        if (cmbEstado.getValue() == null) {
            alerta("Seleccione el estado.");
            return;
        }

        System.out.println("=== ALARMA ===");
        System.out.println("Nombre : " + txtNombre.getText());
        System.out.println("Barrio : " + cmbBarrio.getValue());
        System.out.println("Lat    : " + posicionSeleccionada.getLatitude());
        System.out.println("Lng    : " + posicionSeleccionada.getLongitude());
        System.out.println("Radio  : " + (int) sliderRadio.getValue() + " m");
        System.out.println("Estado : " + cmbEstado.getValue());

        Alert ok = new Alert(Alert.AlertType.INFORMATION);
        ok.setTitle("WolertApp");
        ok.setHeaderText(null);
        ok.setContentText("Alarma lista para guardar en BD.");
        ok.showAndWait();
    }

    private void limpiarFormulario() {
        txtNombre.clear();
        cmbBarrio.setValue(null);
        sliderRadio.setValue(500);
        cmbEstado.setValue(null);
        posicionSeleccionada = null;
        lblCoordenadas.setText("—,  —");
        lblCoordFooter.setText("📍  —,  —");
        lblInstruccionHeader.setText("Haz clic en el mapa para ubicar la alarma");
        if (mapa != null) {
            SwingUtilities.invokeLater(() -> mapa.repaint());
        }
    }

    private void alerta(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle("Validación");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    // ── Utilidad AWT ──────────────────────────────────────────────────────────
    private java.awt.image.BufferedImage recortarTransparencia(java.awt.image.BufferedImage img) {
        int w = img.getWidth(), h = img.getHeight();
        int top = h, bottom = 0, left = w, right = 0;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (((img.getRGB(x, y) >> 24) & 0xff) > 10) {
                    if (y < top) {
                        top = y;
                    }
                    if (y > bottom) {
                        bottom = y;
                    }
                    if (x < left) {
                        left = x;
                    }
                    if (x > right) {
                        right = x;
                    }
                }
            }
        }
        if (top >= bottom || left >= right) {
            return img;
        }
        return img.getSubimage(left, top, right - left + 1, bottom - top + 1);
    }
}
