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
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import static oracle.sql.NUMBER.e;
import sistemagestion.model.Barrio;
import sistemagestion.model.Comuna;
import sistemagestion.model.Direccion;
import sistemagestion.model.EstadoSuscripcion;
import sistemagestion.model.Suscripcion;
import sistemagestion.model.TipoAlerta;
import sistemagestion.model.Usuario;
import sistemagestion.service.BarrioService;
import sistemagestion.service.SuscripcionService;
import sistemagestion.service.UsuarioService;

public class MiCuentaView {

    private static final String WHITE = "#ffffff";
    private static final String BG = "#f4f6fb";
    private static final String RED = "#e53935";
    private static final String ORANGE = "#fb8c00";
    private static final String GREEN = "#43a047";
    private static final String BLUE = "#1565c0";
    private static final String BLUE_LIGHT = "#e8f0fe";
    private static final String GRAY_TEXT = "#6b7280";
    private static final String BORDER = "#e5e7eb";
    private static final String C_DARK_GRAD = "linear-gradient(to right, #16283d, #1f3a56)";

    private final Usuario usuarioActual;
    private final SuscripcionService suscripcionService;
    private final BorderPane root;
    private final Runnable onVolver;
    private UsuarioService usuarioService;
    private BarrioService barrioService;

    public MiCuentaView(Usuario usuarioActual,
            SuscripcionService suscripcionService,
            BorderPane root,
            Runnable onVolver) {
        this.usuarioActual = usuarioActual;
        this.suscripcionService = suscripcionService;
        this.root = root;
        this.onVolver = onVolver;
        javafx.scene.text.Font.loadFont(
                getClass().getResourceAsStream("/fa-solid-900.ttf"), 20);
        try {
            usuarioService = new UsuarioService();
        } catch (Exception e) {
            System.out.println("Error UsuarioService: " + e.getMessage());
        }
        try {
            barrioService = new BarrioService();
        } catch (Exception e) {
            System.out.println("Error BarrioService: " + e.getMessage());
        }
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
                buildTarjetaPerfil(),
                buildPerfilPanel(),
                buildSuscripcionesPanel(),
                buildInfoUtilPanel()
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
        Label title = new Label("Mi Cuenta");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#111827"));
        Label sub = label("Gestiona tu perfil, suscripciones e información útil", 13, GRAY_TEXT, false);
        titles.getChildren().addAll(title, sub);
        header.getChildren().add(titles);
        return header;
    }

    // =========================================================================
    // TARJETA DE PERFIL
    // =========================================================================
    private HBox buildTarjetaPerfil() {
        HBox card = new HBox(20);
        card.setPadding(new Insets(22, 24, 22, 24));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle(
                "-fx-background-color: white;"
                + "-fx-background-radius: 16;"
                + "-fx-border-color: " + BORDER + ";"
                + "-fx-border-radius: 16;"
                + "-fx-border-width: 1;");
        shadow(card);

        StackPane avatarBox = new StackPane();
        Circle avatarCircle = new Circle(38, Color.web(BLUE));
        Label inicialesLbl = new Label(iniciales());
        inicialesLbl.setFont(Font.font("System", FontWeight.BOLD, 22));
        inicialesLbl.setTextFill(Color.WHITE);
        avatarBox.getChildren().addAll(avatarCircle, inicialesLbl);

        VBox info = new VBox(6);
        HBox.setHgrow(info, Priority.ALWAYS);

        String nombre = usuarioActual != null
                ? ((usuarioActual.getPrimer_nombre() != null ? usuarioActual.getPrimer_nombre() : "")
                        + " " + (usuarioActual.getPrimer_apellido() != null ? usuarioActual.getPrimer_apellido() : "")).trim()
                : "Usuario";
        Label nombreLbl = new Label(nombre.isEmpty() ? "Usuario" : nombre);
        nombreLbl.setFont(Font.font("System", FontWeight.BOLD, 18));
        nombreLbl.setTextFill(Color.web("#111827"));

        String rol = usuarioActual != null && usuarioActual.getRol() != null
                ? usuarioActual.getRol().getNombre() : "—";
        Label rolLbl = new Label(rol);
        rolLbl.setStyle(
                "-fx-background-color: " + BLUE_LIGHT + ";"
                + "-fx-text-fill: " + BLUE + ";"
                + "-fx-font-size: 11px; -fx-font-weight: bold;"
                + "-fx-background-radius: 20; -fx-padding: 4 12;");

        String correo = usuarioActual != null && usuarioActual.getCorreo() != null
                ? usuarioActual.getCorreo() : "—";
        Label correoLbl = label(correo, 12, GRAY_TEXT, false);

        String username = usuarioActual != null && usuarioActual.getUsername() != null
                ? "@" + usuarioActual.getUsername() : "";
        Label userLbl = label(username, 12, GRAY_TEXT, false);

        HBox statusRow = new HBox(6);
        statusRow.setAlignment(Pos.CENTER_LEFT);
        statusRow.getChildren().addAll(new Circle(4, Color.web(GREEN)), label("En línea", 11, GREEN, false));

        info.getChildren().addAll(nombreLbl, rolLbl, correoLbl, userLbl, statusRow);

        Button editBtn = new Button("Editar perfil");
        String editBase = "-fx-background-color: #1f3a56; -fx-text-fill: white;"
                + "-fx-font-size: 12px; -fx-font-weight: bold;"
                + "-fx-background-radius: 8; -fx-padding: 9 18; -fx-cursor: hand;";
        String editHover = "-fx-background-color: #16283d; -fx-text-fill: white;"
                + "-fx-font-size: 12px; -fx-font-weight: bold;"
                + "-fx-background-radius: 8; -fx-padding: 9 18; -fx-cursor: hand;";
        editBtn.setStyle(editBase);
        editBtn.setOnMouseEntered(e -> editBtn.setStyle(editHover));
        editBtn.setOnMouseExited(e -> editBtn.setStyle(editBase));
        editBtn.setOnAction(e -> abrirDialogoEditar());

        card.getChildren().addAll(avatarBox, info, editBtn);
        return card;
    }

    // =========================================================================
    // PANEL: INFORMACIÓN PERSONAL
    // =========================================================================
    private VBox buildPerfilPanel() {

        VBox panel = createPanel("Información personal");
        if (usuarioActual == null) {
            panel.getChildren().add(label("No hay información disponible.", 13, GRAY_TEXT, false));
            return panel;
        }
        Direccion dir = usuarioActual.getDireccion();

        // Construir dirección legible
        StringBuilder dirTexto = new StringBuilder();
        if (dir != null) {
            if (dir.getCalle() != null && !dir.getCalle().isBlank()) {
                dirTexto.append("Calle ").append(dir.getCalle()).append("  ");
            }
            if (dir.getCarrera() != null && !dir.getCarrera().isBlank()) {
                dirTexto.append("Cra ").append(dir.getCarrera()).append("  ");
            }
            if (dir.getEtapa() != null && !dir.getEtapa().isBlank()) {
                dirTexto.append("Etapa ").append(dir.getEtapa()).append("  ");
            }
            if (dir.getManzana() != null && !dir.getManzana().isBlank()) {
                dirTexto.append("Manzana ").append(dir.getManzana()).append("  ");
            }
            if (dir.getCasa() != null && !dir.getCasa().isBlank()) {
                dirTexto.append("Casa ").append(dir.getCasa());
            }
        }
        String dirFinal = dirTexto.toString().trim();
        if (dirFinal.isEmpty()) {
            dirFinal = "—";
        }

        String barrio = dir != null && dir.getBarrio() != null ? dir.getBarrio().getNombre() : "—";

        panel.getChildren().addAll(
                infoRow("Identificación", val(usuarioActual.getIdentificacion())), sep(),
                infoRow("Teléfono", val(usuarioActual.getTelefono())), sep(),
                infoRow("Correo", val(usuarioActual.getCorreo())), sep(),
                infoRow("Username", val(usuarioActual.getUsername())), sep(),
                infoRow("Barrio", barrio), sep(),
                infoRow("Dirección", dirFinal)
        );
        return panel;
    }

    // =========================================================================
    // PANEL: MIS SUSCRIPCIONES
    // =========================================================================
    private VBox buildSuscripcionesPanel() {
        VBox panel = createPanel("Mis suscripciones");

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
                vacio.setPadding(new Insets(12, 0, 12, 0));
                vacio.getChildren().add(label("No tienes suscripciones activas.", 13, GRAY_TEXT, false));
                panel.getChildren().add(vacio);
            } else {
                for (Suscripcion s : lista) {
                    String zona = s.getBarrio() != null
                            ? "Barrio: " + s.getBarrio().getNombre()
                            : s.getComuna() != null
                            ? "Comuna: " + s.getComuna().getNombre()
                            : "General (toda la ciudad)";
                    String estadoStr = s.getEstado() != null ? s.getEstado().name() : "—";
                    String badgeColor = switch (estadoStr) {
                        case "ACTIVA" ->
                            GREEN;
                        case "PAUSADA" ->
                            ORANGE;
                        case "CANCELADA" ->
                            RED;
                        default ->
                            GRAY_TEXT;
                    };

                    // Botón eliminar
                    Button btnEliminar = new Button("✕ Eliminar");
                    btnEliminar.setStyle(
                            "-fx-background-color: #fee2e2; -fx-text-fill: #e53935;"
                            + "-fx-font-size: 11px; -fx-font-weight: bold;"
                            + "-fx-background-radius: 6; -fx-padding: 4 10; -fx-cursor: hand;");
                    btnEliminar.setOnMouseEntered(e -> btnEliminar.setStyle(
                            "-fx-background-color: #e53935; -fx-text-fill: white;"
                            + "-fx-font-size: 11px; -fx-font-weight: bold;"
                            + "-fx-background-radius: 6; -fx-padding: 4 10; -fx-cursor: hand;"));
                    btnEliminar.setOnMouseExited(e -> btnEliminar.setStyle(
                            "-fx-background-color: #fee2e2; -fx-text-fill: #e53935;"
                            + "-fx-font-size: 11px; -fx-font-weight: bold;"
                            + "-fx-background-radius: 6; -fx-padding: 4 10; -fx-cursor: hand;"));

                    final int idSusc = s.getId_suscripcion();
                    btnEliminar.setOnAction(e -> {
                        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                        confirm.setTitle("Eliminar suscripción");
                        confirm.setHeaderText(null);
                        confirm.setContentText("¿Seguro que quieres eliminar esta suscripción?\n" + zona);
                        confirm.showAndWait().ifPresent(resp -> {
                            if (resp == ButtonType.OK) {
                                boolean ok = suscripcionService.eliminar(idSusc);
                                if (ok) {
                                    System.out.println("✅ Suscripción eliminada: " + idSusc);
                                } else {
                                    System.out.println("❌ No se pudo eliminar: " + idSusc);
                                }
                                // Refrescar la vista
                                if (root != null) {
                                    root.setCenter(getView());
                                }
                            }
                        });
                    });

                    HBox fila = new HBox(10);
                    fila.setAlignment(Pos.CENTER_LEFT);
                    HBox infoSusc = infoRowBadge("Suscripción", zona, estadoStr, badgeColor);
                    HBox.setHgrow(infoSusc, Priority.ALWAYS);
                    fila.getChildren().addAll(infoSusc, btnEliminar);

                    panel.getChildren().addAll(fila, sep());
                }
            }

            Button btnGestionar = new Button("+ Gestionar suscripción");
            String btnBase = "-fx-background-color: " + BLUE_LIGHT + "; -fx-text-fill: " + BLUE + ";"
                    + "-fx-font-size: 12px; -fx-font-weight: bold;"
                    + "-fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand;";
            String btnHover = "-fx-background-color: " + BLUE + "; -fx-text-fill: white;"
                    + "-fx-font-size: 12px; -fx-font-weight: bold;"
                    + "-fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand;";
            btnGestionar.setStyle(btnBase);
            btnGestionar.setOnMouseEntered(e -> btnGestionar.setStyle(btnHover));
            btnGestionar.setOnMouseExited(e -> btnGestionar.setStyle(btnBase));
            btnGestionar.setOnAction(e -> {
                List<Suscripcion> listaFresca = suscripcionService.listar().stream()
                        .filter(s -> s.getUsuario() != null
                        && usuarioActual.getIdentificacion() != null
                        && usuarioActual.getIdentificacion().equals(s.getUsuario().getIdentificacion()))
                        .toList();
                abrirDialogoSuscripcion(listaFresca);
            });
            panel.getChildren().add(btnGestionar);

        } catch (Exception e) {
            panel.getChildren().add(label("Error: " + e.getMessage(), 12, RED, false));
        }
        return panel;
    }

    // =========================================================================
    // PANEL: INFORMACIÓN ÚTIL
    private VBox buildInfoUtilPanel() {
        VBox panel = createPanel("Información útil");
        panel.getChildren().addAll(
                infoRowFA("\uf46a", "#e53935", "Línea de emergencias", "123"), sep(),
                infoRowFA("\uf505", "#1565c0", "Policía Nacional", "112"), sep(),
                infoRowFA("\uf46a", "#fb8c00", "Bomberos", "119"), sep(),
                infoRowFA("\uf0f1", "#43a047", "Cruz Roja", "132"), sep(),
                infoRowFA("\uf4a4", "#7b1fa2", "Línea de denuncia", "018000910600"), sep(),
                infoRowFA("\uf086", "#0288d1", "Soporte WolertApp", "wolertapp.notificaciones@gmail.com")
        );
        return panel;
    }

    // =========================================================================
    // DIÁLOGO: GESTIONAR SUSCRIPCIÓN
    // =========================================================================
    private void abrirDialogoSuscripcion(List<Suscripcion> actuales) {
        Stage stage = new Stage();
        stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        stage.initStyle(javafx.stage.StageStyle.UNDECORATED);

        // ── Header ────────────────────────────────────────────────────────────
        HBox header = new HBox(12);
        header.setPadding(new Insets(20, 24, 16, 24));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color:white;-fx-border-color:#f1f5f9;-fx-border-width:0 0 1 0;");
        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(38, 38);
        iconBox.setStyle("-fx-background-color:linear-gradient(to bottom right,#16283d,#1f3a56);-fx-background-radius:10;");
        Label iconLbl = new Label("\uf0f3");
        iconLbl.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';-fx-font-size:16px;-fx-text-fill:white;");
        iconBox.getChildren().add(iconLbl);
        VBox titleBox = new VBox(2);
        Label titleLbl = new Label("Gestionar suscripción");
        titleLbl.setFont(Font.font("System", FontWeight.BOLD, 16));
        titleLbl.setTextFill(Color.web("#0f172a"));
        Label subLbl = new Label("Recibirás alertas de la zona elegida");
        subLbl.setFont(Font.font("System", 11));
        subLbl.setTextFill(Color.web("#94a3b8"));
        titleBox.getChildren().addAll(titleLbl, subLbl);
        Button btnX = new Button("✕");
        btnX.setStyle("-fx-background-color:transparent;-fx-text-fill:#94a3b8;-fx-font-size:16px;-fx-cursor:hand;-fx-padding:0;");
        btnX.setOnMouseEntered(e -> btnX.setStyle("-fx-background-color:transparent;-fx-text-fill:#e53935;-fx-font-size:16px;-fx-cursor:hand;-fx-padding:0;"));
        btnX.setOnMouseExited(e -> btnX.setStyle("-fx-background-color:transparent;-fx-text-fill:#94a3b8;-fx-font-size:16px;-fx-cursor:hand;-fx-padding:0;"));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(iconBox, titleBox, spacer, btnX);

        // ── RadioButtons de zona ──────────────────────────────────────────────
        ToggleGroup tgZona = new ToggleGroup();
        RadioButton rbBarrio = styledRadio("Por barrio", tgZona, true);
        RadioButton rbComuna = styledRadio("Por comuna", tgZona, false);
        RadioButton rbGeneral = styledRadio("General (toda la ciudad)", tgZona, false);
        VBox zonaBox = new VBox(6, rbBarrio, rbComuna, rbGeneral);
        zonaBox.setPadding(new Insets(4, 0, 4, 0));

        // ── Buscador estilo "Mis Alertas" ─────────────────────────────────────
        javafx.collections.ObservableList<Barrio> todosBarrios
                = javafx.collections.FXCollections.observableArrayList();

        // SearchBox contenedor (igual que en MisAlertasView)
        HBox searchBox = new HBox(8);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setStyle(
                "-fx-background-color:#f5f7fb;"
                + "-fx-background-radius:10;"
                + "-fx-padding:0 14;");
        searchBox.setPrefHeight(42);
        searchBox.setMaxWidth(Double.MAX_VALUE);

        Label searchIcon = new Label("\uf002");
        searchIcon.setStyle(
                "-fx-font-family:'Font Awesome 6 Free Solid';"
                + "-fx-font-size:14px;"
                + "-fx-text-fill:#9ca3af;");

        TextField editorBarrio = new TextField();
        editorBarrio.setPromptText("Buscar tu barrio...");
        editorBarrio.setStyle(
                "-fx-background-color:transparent;"
                + "-fx-border-color:transparent;"
                + "-fx-font-size:13px;"
                + "-fx-text-fill:#111827;");
        editorBarrio.setPrefHeight(42);
        HBox.setHgrow(editorBarrio, Priority.ALWAYS);
        searchBox.getChildren().addAll(searchIcon, editorBarrio);

        // ── Popup con lista de barrios ────────────────────────────────────────
        ListView<Barrio> listaBarrios = new ListView<>();
        listaBarrios.setPrefHeight(160);
        listaBarrios.setStyle(
                "-fx-background-color:white;-fx-border-color:#e2e8f0;"
                + "-fx-border-radius:12;-fx-background-radius:12;-fx-font-size:13px;");
        listaBarrios.setCellFactory(lv -> new ListCell<Barrio>() {
            @Override
            protected void updateItem(Barrio item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNombre());
                if (!empty && item != null) {
                    setStyle("-fx-background-color:transparent;-fx-text-fill:#374151;-fx-padding:9 20;");
                    setOnMouseEntered(e -> setStyle(
                            "-fx-background-color:linear-gradient(to right,#16283d,#1f3a56);"
                            + "-fx-text-fill:white;-fx-padding:9 20;-fx-background-radius:6;"));
                    setOnMouseExited(e -> setStyle(
                            "-fx-background-color:transparent;-fx-text-fill:#374151;-fx-padding:9 20;"));
                }
            }
        });

        javafx.stage.Popup popupBarrio = new javafx.stage.Popup();
        popupBarrio.setAutoHide(true);
        popupBarrio.setHideOnEscape(true);
        VBox popupContent = new VBox(listaBarrios);
        popupContent.setStyle(
                "-fx-background-color:white;-fx-background-radius:12;"
                + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.15),12,0,0,4);");
        popupBarrio.getContent().add(popupContent);

        final Barrio[] barrioSelSusc = {null};

        try {
            if (barrioService != null) {
                todosBarrios.setAll(barrioService.listar());
                if (usuarioActual.getDireccion() != null
                        && usuarioActual.getDireccion().getBarrio() != null) {
                    String miBarrio = usuarioActual.getDireccion().getBarrio().getNombre();
                    todosBarrios.stream()
                            .filter(b -> b.getNombre().equalsIgnoreCase(miBarrio))
                            .findFirst().ifPresent(b -> {
                                barrioSelSusc[0] = b;
                                editorBarrio.setText(b.getNombre());
                            });
                }
            }
        } catch (Exception ignored) {
        }

        editorBarrio.textProperty().addListener((obs, oldVal, newVal) -> {
            if (barrioSelSusc[0] != null && barrioSelSusc[0].getNombre().equals(newVal)) {
                return;
            }
            barrioSelSusc[0] = null;
            String busq = newVal == null ? "" : newVal.toLowerCase();
            List<Barrio> filtrados = todosBarrios.stream()
                    .filter(b -> busq.isBlank() || b.getNombre().toLowerCase().contains(busq))
                    .toList();
            listaBarrios.getItems().setAll(filtrados);
            if (!filtrados.isEmpty() && !busq.isBlank() && searchBox.getScene() != null) {
                javafx.geometry.Bounds bounds = searchBox.localToScreen(searchBox.getBoundsInLocal());
                if (bounds != null) {
                    popupBarrio.show(searchBox, bounds.getMinX(), bounds.getMaxY() + 2);
                    popupContent.setPrefWidth(bounds.getWidth());
                    listaBarrios.setPrefWidth(bounds.getWidth());
                }
            } else {
                popupBarrio.hide();
            }
        });

        listaBarrios.setOnMouseClicked(e -> {
            Barrio sel = listaBarrios.getSelectionModel().getSelectedItem();
            if (sel != null) {
                barrioSelSusc[0] = sel;
                editorBarrio.setText(sel.getNombre());
                popupBarrio.hide();
            }
        });

        editorBarrio.setOnKeyReleased(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                popupBarrio.hide();
            }
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER && !listaBarrios.getItems().isEmpty()) {
                Barrio sel = listaBarrios.getItems().get(0);
                barrioSelSusc[0] = sel;
                editorBarrio.setText(sel.getNombre());
                popupBarrio.hide();
            }
        });

        // ── ComboBox comuna ───────────────────────────────────────────────────
        ComboBox<String> cmbComuna = new ComboBox<>();
        cmbComuna.setPromptText("Selecciona la comuna");
        cmbComuna.setMaxWidth(Double.MAX_VALUE);
        cmbComuna.setPrefHeight(50);
        cmbComuna.setStyle(
                "-fx-background-color:#f5f7fb;-fx-border-color:transparent;"
                + "-fx-border-radius:25;-fx-background-radius:25;-fx-font-size:13px;");
        cmbComuna.setVisible(false);
        cmbComuna.setManaged(false);
        cmbComuna.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(item);
                boolean sel = getListView().getSelectionModel().getSelectedItem() != null
                        && getListView().getSelectionModel().getSelectedItem().equals(item);
                setStyle(sel
                        ? "-fx-background-color:linear-gradient(to right,#16283d,#1f3a56);"
                        + "-fx-text-fill:white;-fx-font-size:13px;-fx-padding:10 20;-fx-background-radius:25;"
                        : "-fx-background-color:transparent;-fx-text-fill:#374151;"
                        + "-fx-font-size:13px;-fx-padding:10 20;");
                setOnMouseEntered(e -> {
                    if (!item.equals(getListView().getSelectionModel().getSelectedItem())) {
                        setStyle("-fx-background-color:#f0f4ff;-fx-text-fill:#16283d;"
                                + "-fx-font-size:13px;-fx-padding:10 20;");
                    }
                });
                setOnMouseExited(e -> {
                    boolean s2 = item.equals(getListView().getSelectionModel().getSelectedItem());
                    setStyle(s2
                            ? "-fx-background-color:linear-gradient(to right,#16283d,#1f3a56);"
                            + "-fx-text-fill:white;-fx-font-size:13px;-fx-padding:10 20;"
                            : "-fx-background-color:transparent;-fx-text-fill:#374151;"
                            + "-fx-font-size:13px;-fx-padding:10 20;");
                });
            }
        });
        cmbComuna.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Selecciona la comuna" : item);
                setStyle("-fx-background-color:transparent;-fx-text-fill:"
                        + (item != null ? "#374151" : "#9ca3af") + ";-fx-font-size:13px;-fx-padding:0 6;");
            }
        });

        try {
            if (barrioService != null) {
                todosBarrios.stream()
                        .filter(b -> b.getComuna() != null && b.getComuna().getNombre() != null)
                        .map(b -> b.getComuna().getNombre()).distinct().sorted()
                        .forEach(cmbComuna.getItems()::add);
            }
        } catch (Exception ignored) {
        }

        // ── Visibilidad según selección ───────────────────────────────────────
        tgZona.selectedToggleProperty().addListener((obs, o, n) -> {
            boolean esBarrio = n == rbBarrio;
            boolean esComuna = n == rbComuna;

            // SearchBox barrio — deshabilitado si NO es "Por barrio"
            searchBox.setDisable(!esBarrio);
            searchBox.setOpacity(esBarrio ? 1.0 : 0.4);
            editorBarrio.setStyle(
                    "-fx-background-color:transparent;"
                    + "-fx-border-color:transparent;"
                    + "-fx-font-size:13px;"
                    + "-fx-text-fill:" + (esBarrio ? "#111827" : "#9ca3af") + ";");

            // ComboBox comuna — deshabilitado si NO es "Por comuna"
            cmbComuna.setVisible(true);
            cmbComuna.setManaged(true);
            cmbComuna.setDisable(!esComuna);
            cmbComuna.setOpacity(esComuna ? 1.0 : 0.4);

            if (!esBarrio) {
                popupBarrio.hide();
            }
        });

        Label lblRes = new Label("");
        lblRes.setFont(Font.font("System", FontWeight.BOLD, 12));
        lblRes.setWrapText(true);

        // ── Grupo con label como en "Editar perfil" ───────────────────────────
        Label lblBarrioGroup = groupLabel("ZONA DE SUSCRIPCIÓN");
        Label lblBuscadorGroup = groupLabel("BARRIO");

        VBox contenido = new VBox(6);
        contenido.setPadding(new Insets(16, 24, 8, 24));
        contenido.setStyle("-fx-background-color:white;");
        contenido.getChildren().addAll(lblBarrioGroup, zonaBox, lblBuscadorGroup, searchBox, cmbComuna, lblRes);

        ScrollPane scroll = new ScrollPane(contenido);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(340);
        scroll.setStyle("-fx-background:white;-fx-background-color:white;-fx-border-color:transparent;");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        // ── Footer ────────────────────────────────────────────────────────────
        HBox footer = new HBox(10);
        footer.setPadding(new Insets(14, 24, 18, 24));
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setStyle("-fx-background-color:white;-fx-border-color:#f1f5f9;-fx-border-width:1 0 0 0;");

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setPrefHeight(40);
        btnCancelar.setPrefWidth(110);
        btnCancelar.setStyle("-fx-background-color:#f1f5f9;-fx-text-fill:#64748b;-fx-font-size:13px;-fx-font-weight:bold;-fx-background-radius:10;-fx-cursor:hand;");
        btnCancelar.setOnMouseEntered(e -> btnCancelar.setStyle("-fx-background-color:#e2e8f0;-fx-text-fill:#475569;-fx-font-size:13px;-fx-font-weight:bold;-fx-background-radius:10;-fx-cursor:hand;"));
        btnCancelar.setOnMouseExited(e -> btnCancelar.setStyle("-fx-background-color:#f1f5f9;-fx-text-fill:#64748b;-fx-font-size:13px;-fx-font-weight:bold;-fx-background-radius:10;-fx-cursor:hand;"));

        Button btnGuardar = new Button("Guardar suscripción");
        btnGuardar.setPrefHeight(40);
        btnGuardar.setPrefWidth(180);
        btnGuardar.setStyle("-fx-background-color:linear-gradient(to bottom right,#16283d,#1f3a56);-fx-text-fill:white;-fx-font-size:13px;-fx-font-weight:bold;-fx-background-radius:10;-fx-cursor:hand;-fx-effect:dropshadow(gaussian,rgba(22,40,61,0.35),10,0,0,3);");
        btnGuardar.setOnMouseEntered(e -> btnGuardar.setStyle("-fx-background-color:linear-gradient(to bottom right,#1f3a56,#2a4f72);-fx-text-fill:white;-fx-font-size:13px;-fx-font-weight:bold;-fx-background-radius:10;-fx-cursor:hand;-fx-effect:dropshadow(gaussian,rgba(22,40,61,0.5),14,0,0,5);"));
        btnGuardar.setOnMouseExited(e -> btnGuardar.setStyle("-fx-background-color:linear-gradient(to bottom right,#16283d,#1f3a56);-fx-text-fill:white;-fx-font-size:13px;-fx-font-weight:bold;-fx-background-radius:10;-fx-cursor:hand;-fx-effect:dropshadow(gaussian,rgba(22,40,61,0.35),10,0,0,3);"));

        footer.getChildren().addAll(btnCancelar, btnGuardar);

        VBox root2 = new VBox(header, scroll, footer);
        root2.setStyle("-fx-background-color:white;");
        StackPane wrapper = new StackPane(root2);
        wrapper.setStyle("-fx-background-color:white;-fx-background-radius:16;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.20),30,0,0,8);");

        // ── Mismo tamaño que "Editar perfil" ──────────────────────────────────
        Scene scene = new Scene(wrapper, 480, 520);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);

        btnX.setOnAction(e -> {
            popupBarrio.hide();
            stage.close();
        });
        btnCancelar.setOnAction(e -> {
            popupBarrio.hide();
            stage.close();
        });

        btnGuardar.setOnAction(ev -> {
            try {
                Barrio barrioSel = null;
                String comunaNom = null;

                if (rbBarrio.isSelected()) {
                    if (barrioSelSusc[0] == null) {
                        lblRes.setText("⚠️ Selecciona un barrio de la lista.");
                        lblRes.setTextFill(Color.web(ORANGE));
                        return;
                    }
                    barrioSel = barrioSelSusc[0];
                } else if (rbComuna.isSelected()) {
                    if (cmbComuna.getValue() == null) {
                        lblRes.setText("⚠️ Selecciona una comuna.");
                        lblRes.setTextFill(Color.web(ORANGE));
                        return;
                    }
                    comunaNom = cmbComuna.getValue();
                }

                Suscripcion suscripcion = new Suscripcion();
                suscripcion.setUsuario(usuarioActual);
                TipoAlerta ta = new TipoAlerta();
                ta.setNombre("GENERAL");
                suscripcion.setTipoalerta(ta);
                if (barrioSel != null) {
                    suscripcion.setBarrio(barrioSel);
                }
                if (comunaNom != null) {
                    final String cn = comunaNom;
                    try {
                        barrioService.listar().stream()
                                .filter(b -> b.getComuna() != null && cn.equals(b.getComuna().getNombre()))
                                .map(Barrio::getComuna).findFirst().ifPresent(suscripcion::setComuna);
                    } catch (Exception ignored) {
                    }
                }
                suscripcion.setEstado(EstadoSuscripcion.ACTIVA);
                java.util.Optional<Suscripcion> existente = actuales.stream().findFirst();
                boolean ok;
                if (existente.isPresent()) {
                    suscripcion.setId_suscripcion(existente.get().getId_suscripcion());
                    ok = suscripcionService.actualizar(suscripcion);
                } else {
                    ok = suscripcionService.insertar(suscripcion);
                }
                if (ok) {
                    popupBarrio.hide();
                    stage.close();
                    javafx.application.Platform.runLater(() -> {
                        if (this.root != null) {
                            this.root.setCenter(getView());
                        }
                    });
                } else {
                    lblRes.setText("✘ No se pudo guardar.");
                    lblRes.setTextFill(Color.web(RED));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                lblRes.setText("✘ Error: " + ex.getMessage());
                lblRes.setTextFill(Color.web(RED));
            }
        });

        stage.show();
    }

    // =========================================================================
    // DIÁLOGO: EDITAR PERFIL
    // =========================================================================
    private void abrirDialogoEditar() {
        if (usuarioActual == null) {
            return;
        }

        Stage stage = new Stage();
        stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        stage.initStyle(javafx.stage.StageStyle.UNDECORATED);
        stage.setTitle("Editar perfil");

        // ── Campos de texto ───────────────────────────────────────────────────
        TextField fNombre = modernField("Primer nombre", val(usuarioActual.getPrimer_nombre()));
        TextField fApellido = modernField("Primer apellido", val(usuarioActual.getPrimer_apellido()));
        TextField fTelefono = modernField("Teléfono", val(usuarioActual.getTelefono()));
        TextField fCorreo = modernField("Correo", val(usuarioActual.getCorreo()));

        Direccion dirActual = usuarioActual.getDireccion();
        TextField fCalle = modernField("Calle", dirActual != null && dirActual.getCalle() != null ? dirActual.getCalle() : "");
        TextField fCarrera = modernField("Carrera", dirActual != null && dirActual.getCarrera() != null ? dirActual.getCarrera() : "");
        TextField fEtapa = modernField("Etapa", dirActual != null && dirActual.getEtapa() != null ? dirActual.getEtapa() : "");
        TextField fManzana = modernField("Manzana", dirActual != null && dirActual.getManzana() != null ? dirActual.getManzana() : "");
        TextField fCasa = modernField("Casa", dirActual != null && dirActual.getCasa() != null ? dirActual.getCasa() : "");

        // ── Buscador de barrio estilo searchBox ───────────────────────────────
        List<Barrio> barrios;
        try {
            barrios = barrioService != null ? barrioService.listar() : List.of();
        } catch (Exception ignored) {
            barrios = List.of();
        }

        HBox searchBoxBarrio = new HBox(8);
        searchBoxBarrio.setAlignment(Pos.CENTER_LEFT);
        searchBoxBarrio.setStyle(
                "-fx-background-color:#f5f7fb;"
                + "-fx-background-radius:10;"
                + "-fx-padding:0 14;");
        searchBoxBarrio.setPrefHeight(42);
        searchBoxBarrio.setMaxWidth(Double.MAX_VALUE);

        Label searchIconBarrio = new Label("\uf002");
        searchIconBarrio.setStyle(
                "-fx-font-family:'Font Awesome 6 Free Solid';"
                + "-fx-font-size:14px;"
                + "-fx-text-fill:#9ca3af;");

        TextField fBarrio = new TextField();
        fBarrio.setPromptText("Buscar tu barrio...");
        fBarrio.setStyle(
                "-fx-background-color:transparent;"
                + "-fx-border-color:transparent;"
                + "-fx-font-size:13px;"
                + "-fx-text-fill:#111827;");
        fBarrio.setPrefHeight(42);
        HBox.setHgrow(fBarrio, Priority.ALWAYS);
        searchBoxBarrio.getChildren().addAll(searchIconBarrio, fBarrio);

        // Precargar barrio actual
        final Barrio[] barrioSelEdit = {null};
        if (dirActual != null && dirActual.getBarrio() != null) {
            String nombreActual = dirActual.getBarrio().getNombre();
            barrios.stream()
                    .filter(b -> b.getNombre().equalsIgnoreCase(nombreActual))
                    .findFirst().ifPresent(b -> {
                        barrioSelEdit[0] = b;
                        fBarrio.setText(b.getNombre());
                    });
        }

        // Popup lista barrios
        ListView<Barrio> listaBarriosEdit = new ListView<>();
        listaBarriosEdit.setPrefHeight(160);
        listaBarriosEdit.setStyle(
                "-fx-background-color:white;-fx-border-color:#e2e8f0;"
                + "-fx-border-radius:12;-fx-background-radius:12;-fx-font-size:13px;");
        listaBarriosEdit.setCellFactory(lv -> new ListCell<Barrio>() {
            @Override
            protected void updateItem(Barrio item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNombre());
                if (!empty && item != null) {
                    setStyle("-fx-background-color:transparent;-fx-text-fill:#374151;-fx-padding:9 20;");
                    setOnMouseEntered(e -> setStyle(
                            "-fx-background-color:linear-gradient(to right,#16283d,#1f3a56);"
                            + "-fx-text-fill:white;-fx-padding:9 20;-fx-background-radius:6;"));
                    setOnMouseExited(e -> setStyle(
                            "-fx-background-color:transparent;-fx-text-fill:#374151;-fx-padding:9 20;"));
                }
            }
        });

        javafx.stage.Popup popupBarrioEdit = new javafx.stage.Popup();
        popupBarrioEdit.setAutoHide(true);
        popupBarrioEdit.setHideOnEscape(true);
        VBox popupContentEdit = new VBox(listaBarriosEdit);
        popupContentEdit.setStyle(
                "-fx-background-color:white;-fx-background-radius:12;"
                + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.15),12,0,0,4);");
        popupBarrioEdit.getContent().add(popupContentEdit);

        final List<Barrio> barriosFinal = barrios;

        fBarrio.textProperty().addListener((obs, oldVal, newVal) -> {
            if (barrioSelEdit[0] != null && barrioSelEdit[0].getNombre().equals(newVal)) {
                return;
            }
            barrioSelEdit[0] = null;
            String busq = newVal == null ? "" : newVal.toLowerCase();
            List<Barrio> filtrados = barriosFinal.stream()
                    .filter(b -> busq.isBlank() || b.getNombre().toLowerCase().contains(busq))
                    .toList();
            listaBarriosEdit.getItems().setAll(filtrados);
            if (!filtrados.isEmpty() && !busq.isBlank() && searchBoxBarrio.getScene() != null) {
                javafx.geometry.Bounds bounds = searchBoxBarrio.localToScreen(searchBoxBarrio.getBoundsInLocal());
                if (bounds != null) {
                    popupBarrioEdit.show(searchBoxBarrio, bounds.getMinX(), bounds.getMaxY() + 2);
                    popupContentEdit.setPrefWidth(bounds.getWidth());
                    listaBarriosEdit.setPrefWidth(bounds.getWidth());
                }
            } else {
                popupBarrioEdit.hide();
            }
        });

        listaBarriosEdit.setOnMouseClicked(e -> {
            Barrio sel = listaBarriosEdit.getSelectionModel().getSelectedItem();
            if (sel != null) {
                barrioSelEdit[0] = sel;
                fBarrio.setText(sel.getNombre());
                popupBarrioEdit.hide();
            }
        });

        fBarrio.setOnKeyReleased(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                popupBarrioEdit.hide();
            }
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER && !listaBarriosEdit.getItems().isEmpty()) {
                Barrio sel = listaBarriosEdit.getItems().get(0);
                barrioSelEdit[0] = sel;
                fBarrio.setText(sel.getNombre());
                popupBarrioEdit.hide();
            }
        });

        // ── Error label ───────────────────────────────────────────────────────
        Label lblError = new Label("");
        lblError.setFont(Font.font("System", 12));
        lblError.setTextFill(Color.web(RED));
        lblError.setWrapText(true);

        // ── Header del diálogo ────────────────────────────────────────────────
        HBox header = new HBox(12);
        header.setPadding(new Insets(20, 24, 16, 24));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color:white;-fx-border-color:#f1f5f9;-fx-border-width:0 0 1 0;");
        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(38, 38);
        iconBox.setStyle("-fx-background-color:linear-gradient(to bottom right,#16283d,#1f3a56);-fx-background-radius:10;");
        Label iconLbl = new Label("\uf304");
        iconLbl.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';-fx-font-size:16px;-fx-text-fill:white;");
        iconBox.getChildren().add(iconLbl);
        VBox titleBox = new VBox(2);
        Label titleLbl = new Label("Editar perfil");
        titleLbl.setFont(Font.font("System", FontWeight.BOLD, 16));
        titleLbl.setTextFill(Color.web("#0f172a"));
        Label subLbl = new Label("Actualiza tu información personal");
        subLbl.setFont(Font.font("System", 11));
        subLbl.setTextFill(Color.web("#94a3b8"));
        titleBox.getChildren().addAll(titleLbl, subLbl);
        Button btnX = new Button("✕");
        btnX.setStyle("-fx-background-color:transparent;-fx-text-fill:#94a3b8;-fx-font-size:16px;-fx-cursor:hand;-fx-padding:0;");
        btnX.setOnMouseEntered(e -> btnX.setStyle("-fx-background-color:transparent;-fx-text-fill:#e53935;-fx-font-size:16px;-fx-cursor:hand;-fx-padding:0;"));
        btnX.setOnMouseExited(e -> btnX.setStyle("-fx-background-color:transparent;-fx-text-fill:#94a3b8;-fx-font-size:16px;-fx-cursor:hand;-fx-padding:0;"));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(iconBox, titleBox, spacer, btnX);

        // ── Contenido con scroll ──────────────────────────────────────────────
        VBox contenido = new VBox(6);
        contenido.setPadding(new Insets(16, 24, 20, 24));
        contenido.setStyle("-fx-background-color:white;");
        contenido.getChildren().addAll(
                groupLabel("NOMBRE"),
                fNombre,
                fApellido,
                groupLabel("CONTACTO"),
                fTelefono,
                fCorreo,
                groupLabel("DIRECCIÓN"),
                fCalle,
                fCarrera,
                fEtapa,
                fManzana,
                fCasa,
                groupLabel("BARRIO"),
                searchBoxBarrio,
                lblError
        );

        ScrollPane scroll = new ScrollPane(contenido);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(370);
        scroll.setStyle("-fx-background:white;-fx-background-color:white;-fx-border-color:transparent;");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        // ── Footer ────────────────────────────────────────────────────────────
        HBox footer = new HBox(10);
        footer.setPadding(new Insets(14, 24, 18, 24));
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setStyle("-fx-background-color:white;-fx-border-color:#f1f5f9;-fx-border-width:1 0 0 0;");

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setPrefHeight(40);
        btnCancelar.setPrefWidth(110);
        btnCancelar.setStyle("-fx-background-color:#f1f5f9;-fx-text-fill:#64748b;-fx-font-size:13px;-fx-font-weight:bold;-fx-background-radius:10;-fx-cursor:hand;");
        btnCancelar.setOnMouseEntered(e -> btnCancelar.setStyle("-fx-background-color:#e2e8f0;-fx-text-fill:#475569;-fx-font-size:13px;-fx-font-weight:bold;-fx-background-radius:10;-fx-cursor:hand;"));
        btnCancelar.setOnMouseExited(e -> btnCancelar.setStyle("-fx-background-color:#f1f5f9;-fx-text-fill:#64748b;-fx-font-size:13px;-fx-font-weight:bold;-fx-background-radius:10;-fx-cursor:hand;"));

        Button btnGuardar = new Button("Guardar cambios");
        btnGuardar.setPrefHeight(40);
        btnGuardar.setPrefWidth(160);
        btnGuardar.setStyle("-fx-background-color:linear-gradient(to bottom right,#16283d,#1f3a56);-fx-text-fill:white;-fx-font-size:13px;-fx-font-weight:bold;-fx-background-radius:10;-fx-cursor:hand;-fx-effect:dropshadow(gaussian,rgba(22,40,61,0.35),10,0,0,3);");
        btnGuardar.setOnMouseEntered(e -> btnGuardar.setStyle("-fx-background-color:linear-gradient(to bottom right,#1f3a56,#2a4f72);-fx-text-fill:white;-fx-font-size:13px;-fx-font-weight:bold;-fx-background-radius:10;-fx-cursor:hand;-fx-effect:dropshadow(gaussian,rgba(22,40,61,0.5),14,0,0,5);"));
        btnGuardar.setOnMouseExited(e -> btnGuardar.setStyle("-fx-background-color:linear-gradient(to bottom right,#16283d,#1f3a56);-fx-text-fill:white;-fx-font-size:13px;-fx-font-weight:bold;-fx-background-radius:10;-fx-cursor:hand;-fx-effect:dropshadow(gaussian,rgba(22,40,61,0.35),10,0,0,3);"));

        footer.getChildren().addAll(btnCancelar, btnGuardar);

        // ── Wrapper con sombra ────────────────────────────────────────────────
        VBox form = new VBox(header, scroll, footer);
        form.setStyle("-fx-background-color:white;");
        StackPane wrapper = new StackPane(form);
        wrapper.setStyle("-fx-background-color:white;-fx-background-radius:16;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.20),30,0,0,8);");

        Scene scene = new Scene(wrapper, 480, 520);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);

        // ── Acciones ──────────────────────────────────────────────────────────
        btnX.setOnAction(e -> {
            popupBarrioEdit.hide();
            stage.close();
        });
        btnCancelar.setOnAction(e -> {
            popupBarrioEdit.hide();
            stage.close();
        });

        btnGuardar.setOnAction(ev -> {
            lblError.setText("");
            try {
                if (!fNombre.getText().isBlank()) {
                    usuarioActual.setPrimer_nombre(fNombre.getText().trim());
                }
                if (!fApellido.getText().isBlank()) {
                    usuarioActual.setPrimer_apellido(fApellido.getText().trim());
                }
                if (!fTelefono.getText().isBlank()) {
                    usuarioActual.setTelefono(fTelefono.getText().trim());
                }
                if (!fCorreo.getText().isBlank()) {
                    usuarioActual.setCorreo(fCorreo.getText().trim());
                }

                Direccion dir = usuarioActual.getDireccion() != null ? usuarioActual.getDireccion() : new Direccion();
                if (!fCalle.getText().isBlank()) {
                    dir.setCalle(fCalle.getText().trim());
                }
                if (!fCarrera.getText().isBlank()) {
                    dir.setCarrera(fCarrera.getText().trim());
                }
                if (!fEtapa.getText().isBlank()) {
                    dir.setEtapa(fEtapa.getText().trim());
                }
                if (!fManzana.getText().isBlank()) {
                    dir.setManzana(fManzana.getText().trim());
                }
                if (!fCasa.getText().isBlank()) {
                    dir.setCasa(fCasa.getText().trim());
                }

                if (barrioSelEdit[0] == null) {
                    lblError.setText("⚠️ Selecciona un barrio de la lista.");
                    return;
                }
                dir.setBarrio(barrioSelEdit[0]);
                usuarioActual.setDireccion(dir);

                if (usuarioService != null) {
                    usuarioService.actualizar(usuarioActual);
                    popupBarrioEdit.hide();
                    stage.close();
                    if (root != null) {
                        javafx.application.Platform.runLater(() -> root.setCenter(getView()));
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                lblError.setText("Error: " + ex.getMessage());
            }
        });

        stage.show();
    }

    // =========================================================================
    // HELPERS UI
    // =========================================================================
    private HBox infoRowFA(String faIcon, String iconColor, String campo, String valor) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 0, 10, 0));

        // Icono FA en círculo de color
        StackPane iconWrap = new StackPane();
        iconWrap.setPrefSize(34, 34);
        iconWrap.setMinSize(34, 34);
        iconWrap.setMaxSize(34, 34);
        Region iconBg = new Region();
        iconBg.setPrefSize(34, 34);
        iconBg.setStyle("-fx-background-color:" + iconColor + "22;-fx-background-radius:50%;");
        Label iconLbl = new Label(faIcon);
        iconLbl.setStyle(
                "-fx-font-family: 'Font Awesome 6 Free Solid';"
                + "-fx-font-size: 14px;"
                + "-fx-text-fill: " + iconColor + ";");
        iconWrap.getChildren().addAll(iconBg, iconLbl);

        // Texto
        VBox texto = new VBox(2);
        HBox.setHgrow(texto, Priority.ALWAYS);
        Label campoLbl = label(campo, 11, GRAY_TEXT, false);
        Label valorLbl = label(valor, 13, "#111827", true);
        texto.getChildren().addAll(campoLbl, valorLbl);

        row.getChildren().addAll(iconWrap, texto);
        return row;
    }

    private VBox createPanel(String title) {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(16));
        panel.setStyle("-fx-background-color: white; -fx-background-radius: 12;");
        shadow(panel);
        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Rectangle accentBar = new Rectangle(4, 20);
        accentBar.setFill(Color.web(BLUE));
        accentBar.setArcWidth(4);
        accentBar.setArcHeight(4);
        Label t = new Label(title);
        t.setFont(Font.font("System", FontWeight.BOLD, 14));
        t.setTextFill(Color.web("#111827"));
        titleRow.getChildren().addAll(accentBar, t);
        panel.getChildren().addAll(titleRow, sep());
        return panel;
    }

    private HBox infoRow(String campo, String valor) {
        HBox row = new HBox(0);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(9, 0, 9, 0));
        Label key = label(campo, 12, GRAY_TEXT, false);
        key.setMinWidth(160);
        key.setMaxWidth(160);
        Label val = label(valor, 13, "#111827", false);
        val.setWrapText(true);
        HBox.setHgrow(val, Priority.ALWAYS);
        row.getChildren().addAll(key, val);
        return row;
    }

    private HBox infoRowBadge(String campo, String valor, String badge, String badgeColor) {
        HBox row = new HBox(0);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(9, 0, 9, 0));
        Label key = label(campo, 12, GRAY_TEXT, false);
        key.setMinWidth(160);
        key.setMaxWidth(160);
        Label val = label(valor, 13, "#111827", false);
        HBox.setHgrow(val, Priority.ALWAYS);
        Label badgeLbl = new Label(badge);
        badgeLbl.setStyle(
                "-fx-background-color:" + badgeColor + "22;"
                + "-fx-text-fill:" + badgeColor + ";"
                + "-fx-font-size:11px;-fx-font-weight:bold;"
                + "-fx-background-radius:20;-fx-padding:4 10;");
        row.getChildren().addAll(key, val, badgeLbl);
        return row;
    }

    private TextField dlgField(String prompt, String val) {
        TextField f = new TextField(val);
        f.setPromptText(prompt);
        f.setPrefHeight(40);
        f.setMaxWidth(Double.MAX_VALUE);
        f.setStyle("-fx-background-color:#f5f7fb;-fx-background-radius:8;"
                + "-fx-border-color:transparent;-fx-padding:0 14;-fx-font-size:13px;");
        return f;
    }

    private Label seccion(String txt) {
        Label l = label(txt, 10, "#9ca3af", true);
        VBox.setMargin(l, new Insets(8, 0, 2, 0));
        return l;
    }

    private String iniciales() {
        if (usuarioActual == null) {
            return "?";
        }
        String n = usuarioActual.getPrimer_nombre() != null && !usuarioActual.getPrimer_nombre().isBlank()
                ? usuarioActual.getPrimer_nombre().substring(0, 1).toUpperCase() : "?";
        String a = usuarioActual.getPrimer_apellido() != null && !usuarioActual.getPrimer_apellido().isBlank()
                ? usuarioActual.getPrimer_apellido().substring(0, 1).toUpperCase() : "";
        return n + a;
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

    private void shadow(Region node) {
        node.setEffect(new DropShadow(10, 0, 2, Color.web("#0000001a")));
    }

    private Label label(String text, double size, String color, boolean bold) {
        Label lbl = new Label(text);
        lbl.setFont(bold ? Font.font("System", FontWeight.BOLD, size) : Font.font("System", size));
        lbl.setTextFill(Color.web(color));
        return lbl;
    }

    private TextField modernField(String prompt, String value) {
        TextField f = new TextField(value);
        f.setPromptText(prompt);
        f.setPrefHeight(50);
        f.setMaxWidth(Double.MAX_VALUE);
        String base = "-fx-background-color:#f5f7fb;-fx-border-color:transparent;"
                + "-fx-border-radius:25;-fx-background-radius:25;"
                + "-fx-padding:0 20;-fx-font-size:13px;-fx-text-fill:#374151;";
        String focused = "-fx-background-color:#f0f4ff;-fx-border-color:#1f3a56;"
                + "-fx-border-radius:25;-fx-background-radius:25;-fx-border-width:1.5;"
                + "-fx-padding:0 20;-fx-font-size:13px;-fx-text-fill:#374151;";
        f.setStyle(base);
        f.focusedProperty().addListener((obs, o, n) -> f.setStyle(n ? focused : base));
        return f;
    }

    private HBox rowDos(javafx.scene.Node a, javafx.scene.Node b) {
        HBox row = new HBox(10, a, b);
        HBox.setHgrow(a, Priority.ALWAYS);
        HBox.setHgrow(b, Priority.ALWAYS);
        ((TextField) a).setMaxWidth(Double.MAX_VALUE);
        ((TextField) b).setMaxWidth(Double.MAX_VALUE);
        return row;
    }

    private HBox rowTres(javafx.scene.Node a, javafx.scene.Node b, javafx.scene.Node c) {
        HBox row = new HBox(10, a, b, c);
        HBox.setHgrow(a, Priority.ALWAYS);
        HBox.setHgrow(b, Priority.ALWAYS);
        HBox.setHgrow(c, Priority.ALWAYS);
        return row;
    }

    private Label groupLabel(String txt) {
        Label l = new Label(txt.toUpperCase());
        l.setFont(Font.font("System", FontWeight.BOLD, 10));
        l.setTextFill(Color.web("#94a3b8"));
        VBox.setMargin(l, new Insets(10, 0, 2, 0));
        return l;
    }

    private RadioButton styledRadio(String texto, ToggleGroup tg, boolean selected) {
        RadioButton rb = new RadioButton(texto);
        rb.setToggleGroup(tg);
        rb.setSelected(selected);
        rb.setFont(Font.font("System", FontWeight.BOLD, 13));
        rb.setTextFill(Color.web(selected ? "#1f3a56" : "#6b7280"));
        rb.setPadding(new Insets(10, 20, 10, 20));
        rb.setMaxWidth(Double.MAX_VALUE);
        rb.setPrefHeight(46);
        rb.setGraphic(null);

        String base = "-fx-background-color:#f5f7fb;-fx-background-radius:25;"
                + "-fx-border-color:transparent;-fx-border-radius:25;"
                + "-fx-radio-button-gap:0;";
        String activo = "-fx-background-color:#eef2ff;-fx-background-radius:25;"
                + "-fx-border-color:#1f3a56;-fx-border-radius:25;-fx-border-width:1.5;"
                + "-fx-radio-button-gap:0;";

        rb.setStyle(selected ? activo : base);

        // Ocultar el círculo nativo completamente
        rb.skinProperty().addListener((obs, o, n) -> {
            if (n == null) {
                return;
            }
            rb.lookupAll(".radio").forEach(node -> node.setStyle("-fx-padding:0;-fx-background-color:transparent;-fx-border-color:transparent;"));
            rb.lookupAll(".dot").forEach(node -> node.setStyle("-fx-background-color:transparent;"));
        });

        rb.selectedProperty().addListener((obs, o, n) -> {
            rb.setStyle(n ? activo : base);
            rb.setTextFill(Color.web(n ? "#1f3a56" : "#6b7280"));
            rb.lookupAll(".radio").forEach(node -> node.setStyle("-fx-padding:0;-fx-background-color:transparent;-fx-border-color:transparent;"));
            rb.lookupAll(".dot").forEach(node -> node.setStyle("-fx-background-color:transparent;"));
        });

        rb.setOnMouseEntered(e -> {
            if (!rb.isSelected()) {
                rb.setStyle("-fx-background-color:#eef2ff;-fx-background-radius:25;"
                        + "-fx-border-color:transparent;-fx-border-radius:25;");
            }
        });
        rb.setOnMouseExited(e -> {
            if (!rb.isSelected()) {
                rb.setStyle(base);
            }
        });

        return rb;
    }
}
