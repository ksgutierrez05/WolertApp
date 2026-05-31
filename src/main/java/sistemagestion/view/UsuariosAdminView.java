package sistemagestion.view;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import sistemagestion.model.EstadoUsuario;
import sistemagestion.model.RolUsuario;
import sistemagestion.model.Usuario;
import sistemagestion.service.BarrioService;
import sistemagestion.service.RolUsuarioService;
import sistemagestion.service.UsuarioService;
import sistemagestion.service.EmailService;

public class UsuariosAdminView {

    private static final String WHITE = "#ffffff";
    private static final String BG = "#f4f6fb";
    private static final String RED = "#e53935";
    private static final String RED_LIGHT = "#fff0f0";
    private static final String ORANGE = "#fb8c00";
    private static final String GREEN = "#43a047";
    private static final String BLUE = "#1565c0";
    private static final String GRAY_TEXT = "#6b7280";
    private static final String BORDER = "#e5e7eb";

    private UsuarioService usuarioService;
    private RolUsuarioService rolService;
    private BarrioService barrioService;

    private final ObservableList<Usuario> todosLosUsuarios = FXCollections.observableArrayList();
    private final ObservableList<Usuario> usuariosFiltrados = FXCollections.observableArrayList();

    private static final int FILAS_POR_PAGINA = 8;
    private int paginaActual = 1;

    private VBox tablaContainer;
    private Label lblMostrando;
    private HBox paginacionBox;
    private TextField campoBusqueda;

    private ComboBox<String> filtroEstado;
    private ComboBox<String> filtroRol;
    private ComboBox<String> filtroBarrio;

    private Label lblTotalVal;
    private Label lblActivosVal;
    private Label lblInactivosVal;
    private Label lblRolesVal;

    public UsuariosAdminView() {
        javafx.scene.text.Font.loadFont(
                getClass().getResourceAsStream("/fa-solid-900.ttf"), 20);
        try {
            usuarioService = new UsuarioService();
            rolService = new RolUsuarioService();
            barrioService = new BarrioService();
        } catch (SQLException e) {
            mostrarAlerta("Error de conexión", e.getMessage());
        }
    }

    // =========================================================================
    // PUNTO DE ENTRADA
    // =========================================================================
    public ScrollPane getView() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color:" + BG + ";");

        content.getChildren().addAll(
                buildTopBar(),
                buildStatsRow(),
                buildToolbar(),
                buildTabla(),
                buildPaginacion()
        );

        cargarUsuarios();

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color:" + BG + "; -fx-background:" + BG + ";");
        return scroll;
    }

    // =========================================================================
    // TOP BAR
    // =========================================================================
    private HBox buildTopBar() {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);

        VBox titles = new VBox(4);
        Label title = new Label("Gestión de Usuarios");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#111827"));
        Label sub = label("Administra los usuarios registrados en el sistema", 13, GRAY_TEXT, false);
        titles.getChildren().addAll(title, sub);

        HBox right = new HBox(12);
        right.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(right, Priority.ALWAYS);

        Button btnNuevo = styledBtn("+ Nuevo usuario", BLUE, "#0d47a1");
        btnNuevo.setOnAction(e -> abrirDialogoCrear());
        right.getChildren().add(btnNuevo);

        bar.getChildren().addAll(titles, right);
        return bar;
    }

    // =========================================================================
    // STATS ROW
    // =========================================================================
    private HBox buildStatsRow() {
        HBox row = new HBox(16);
        HBox.setHgrow(row, Priority.ALWAYS);

        lblTotalVal = boldNum("0", BLUE);
        lblActivosVal = boldNum("0", GREEN);
        lblInactivosVal = boldNum("0", RED);
        lblRolesVal = boldNum("0", "#7b1fa2");

        row.getChildren().addAll(
                statCard("#e8f0fe", BLUE, "\uf0c0", "Total registrados", lblTotalVal, "Total en el sistema"),
                statCard("#e8f5e9", GREEN, "\uf058", "Usuarios activos", lblActivosVal, "+18 este mes"),
                statCard("#fff0f0", RED, "\uf071", "Inactivos / Suspendidos", lblInactivosVal, "Requieren revisión"),
                statCard("#f3e5f5", "#7b1fa2", "\uf505", "Roles configurados", lblRolesVal, "Admin · Policía · Usuario")
        );
        return row;
    }

    private Label boldNum(String val, String color) {
        Label l = new Label(val);
        l.setFont(Font.font("System", FontWeight.BOLD, 36));
        l.setTextFill(Color.web(color));
        return l;
    }

    private VBox statCard(String bgIcon, String accentColor, String iconFA,
            String title, Label valueLabel, String sub) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20, 22, 20, 22));
        card.setStyle("-fx-background-color:white; -fx-background-radius:18;");
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
                + "-fx-font-size:22px;"
                + "-fx-text-fill:" + accentColor + ";");
        iconWrap.getChildren().addAll(iconBg, iconLbl);

        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#374151;");
        Label subLbl = new Label(sub);
        subLbl.setStyle("-fx-font-size:11px;-fx-text-fill:" + GRAY_TEXT + ";");

        HBox top = new HBox(16, iconWrap, new VBox(3, titleLbl, valueLabel, subLbl));
        top.setAlignment(Pos.CENTER_LEFT);
        card.getChildren().add(top);
        card.setOnMouseEntered(e -> card.setTranslateY(-3));
        card.setOnMouseExited(e -> card.setTranslateY(0));
        return card;
    }

    // =========================================================================
    // TOOLBAR
    // =========================================================================
    private HBox buildToolbar() {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(16, 20, 16, 20));
        bar.setStyle("-fx-background-color:white; -fx-background-radius:12;");
        shadow(bar);

        HBox searchBox = new HBox(8);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setStyle("-fx-background-color:#f5f7fb;-fx-background-radius:10;-fx-padding:0 14;");
        searchBox.setPrefHeight(42);
        HBox.setHgrow(searchBox, Priority.ALWAYS);

        Label searchIcon = new Label("\uf002");
        searchIcon.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';-fx-font-size:14px;-fx-text-fill:#9ca3af;");

        campoBusqueda = new TextField();
        campoBusqueda.setPromptText("Buscar por nombre, cédula o username...");
        campoBusqueda.setStyle("-fx-background-color:transparent;-fx-border-color:transparent;"
                + "-fx-font-size:13px;-fx-text-fill:#111827;");
        campoBusqueda.setPrefHeight(42);
        HBox.setHgrow(campoBusqueda, Priority.ALWAYS);
        campoBusqueda.textProperty().addListener((obs, o, n) -> filtrarYMostrar());
        searchBox.getChildren().addAll(searchIcon, campoBusqueda);

        filtroEstado = styledCombo("Estado");
        cargarEstados();

        filtroRol = styledCombo("Rol");
        cargarRoles();

        filtroBarrio = styledCombo("Barrio");
        cargarBarrios(filtroBarrio);

        bar.getChildren().addAll(searchBox, filtroEstado, filtroRol, filtroBarrio);
        return bar;
    }

    private <T> ComboBox<T> styledCombo(String placeholder) {
        ComboBox<T> combo = new ComboBox<>();
        combo.setPromptText(placeholder);
        combo.setPrefHeight(42);
        combo.setPrefWidth(160);
        combo.setStyle(
                "-fx-background-color:#f5f7fb;-fx-background-radius:10;"
                + "-fx-border-color:transparent;-fx-border-radius:10;"
                + "-fx-font-size:13px;-fx-text-fill:#374151;-fx-cursor:hand;");
        combo.setOnAction(e -> filtrarYMostrar());
        return (ComboBox<T>) combo;
    }

    private void cargarRoles() {
        try {
            filtroRol.getItems().clear();
            filtroRol.getItems().add("Rol: Todos");
            for (RolUsuario r : rolService.listar()) {
                if (r != null && r.getNombre() != null) {
                    filtroRol.getItems().add(r.getNombre());
                }
            }
            filtroRol.setValue("Rol: Todos");
        } catch (Exception e) {
            mostrarAlerta("Error roles", e.getMessage());
        }
    }

    private void cargarEstados() {
        filtroEstado.getItems().clear();
        filtroEstado.getItems().add("Estado: Todos");
        for (EstadoUsuario e : EstadoUsuario.values()) {
            filtroEstado.getItems().add(e.name());
        }
        filtroEstado.setValue("Estado: Todos");
    }

    private void cargarBarrios(ComboBox<String> combo) {
        try {
            combo.getItems().clear();
            combo.getItems().add("Barrio: Todos");
            barrioService.listar().forEach(b -> combo.getItems().add(b.getNombre()));
            combo.setValue("Barrio: Todos");
        } catch (Exception e) {
            mostrarAlerta("Error barrios", e.getMessage());
        }
    }

    // =========================================================================
    // TABLA
    // =========================================================================
    private VBox buildTabla() {
        VBox card = new VBox(0);
        card.setStyle("-fx-background-color:white;-fx-background-radius:12;");
        shadow(card);

        HBox header = new HBox(0);
        header.setPadding(new Insets(12, 16, 12, 16));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color:#f8fafc;-fx-background-radius:12 12 0 0;"
                + "-fx-border-color:transparent transparent " + BORDER + " transparent;"
                + "-fx-border-width:0 0 1 0;");
        header.getChildren().addAll(
                colHeader("Usuario", 240),
                colHeader("Identificación", 130),
                colHeader("Correo electrónico", 200),
                colHeader("Teléfono", 130),
                colHeader("Rol", 140),
                colHeader("Estado", 130),
                colHeader("Acciones", 120)
        );
        card.getChildren().add(header);

        tablaContainer = new VBox(0);
        card.getChildren().add(tablaContainer);

        lblMostrando = label("Mostrando 0 – 0 de 0 usuarios", 12, GRAY_TEXT, false);
        HBox footer = new HBox();
        footer.setPadding(new Insets(12, 16, 12, 16));
        footer.setStyle("-fx-border-color:" + BORDER + " transparent transparent transparent;"
                + "-fx-border-width:1 0 0 0;");
        footer.getChildren().add(lblMostrando);
        card.getChildren().add(footer);
        return card;
    }

    private Label colHeader(String text, double width) {
        Label l = new Label(text.toUpperCase());
        l.setStyle("-fx-font-size:11px;-fx-font-weight:bold;-fx-text-fill:#9ca3af;");
        l.setPrefWidth(width);
        l.setMinWidth(width);
        l.setMaxWidth(width);
        return l;
    }

    // =========================================================================
    // PAGINACIÓN
    // =========================================================================
    private HBox buildPaginacion() {
        paginacionBox = new HBox(6);
        paginacionBox.setAlignment(Pos.CENTER_RIGHT);
        return paginacionBox;
    }

    // =========================================================================
    // CARGA Y FILTRADO
    // =========================================================================
    private void cargarUsuarios() {
        if (usuarioService == null) {
            return;
        }
        new Thread(() -> {
            try {
                List<Usuario> lista = usuarioService.listar();
                javafx.application.Platform.runLater(() -> {
                    todosLosUsuarios.setAll(lista);
                    filtrarYMostrar();
                    actualizarStats();
                });
            } catch (SQLException e) {
                javafx.application.Platform.runLater(()
                        -> mostrarAlerta("Error al cargar usuarios", e.getMessage()));
            }
        }, "hilo-carga-usuarios").start();
    }

    private void filtrarYMostrar() {
        String busqueda = campoBusqueda != null ? campoBusqueda.getText().toLowerCase().trim() : "";
        String estado = filtroEstado != null ? filtroEstado.getValue() : "Estado: Todos";
        String rol = filtroRol != null ? filtroRol.getValue() : "Rol: Todos";
        String barrio = filtroBarrio != null ? filtroBarrio.getValue() : "Barrio: Todos";

        List<Usuario> filtrados = todosLosUsuarios.stream()
                .filter(u -> {
                    boolean matchB = busqueda.isEmpty()
                            || nombreCompleto(u).toLowerCase().contains(busqueda)
                            || (u.getIdentificacion() != null && u.getIdentificacion().toLowerCase().contains(busqueda))
                            || (u.getUsername() != null && u.getUsername().toLowerCase().contains(busqueda));

                    boolean matchE = estado == null || estado.startsWith("Estado")
                            || (u.getEstado() != null && u.getEstado().name().equalsIgnoreCase(estado));

                    boolean matchR = rol == null || rol.startsWith("Rol")
                            || (u.getRol() != null && u.getRol().getNombre().equalsIgnoreCase(rol));

                    boolean matchBa = barrio == null || barrio.startsWith("Barrio")
                            || (u.getDireccion() != null
                            && u.getDireccion().getBarrio() != null
                            && u.getDireccion().getBarrio().getNombre().equalsIgnoreCase(barrio));

                    return matchB && matchE && matchR && matchBa;
                })
                .collect(Collectors.toList());

        usuariosFiltrados.setAll(filtrados);
        paginaActual = 1;
        renderizarPagina();
    }

    private void renderizarPagina() {
        tablaContainer.getChildren().clear();

        int total = usuariosFiltrados.size();
        int desde = (paginaActual - 1) * FILAS_POR_PAGINA;
        int hasta = Math.min(desde + FILAS_POR_PAGINA, total);

        if (total == 0) {
            VBox vacio = new VBox(10);
            vacio.setAlignment(Pos.CENTER);
            vacio.setPadding(new Insets(40));
            vacio.getChildren().add(label("No se encontraron usuarios", 14, GRAY_TEXT, false));
            tablaContainer.getChildren().add(vacio);
        } else {
            for (int i = desde; i < hasta; i++) {
                tablaContainer.getChildren().add(buildFila(usuariosFiltrados.get(i), i % 2 == 0));
            }
        }

        lblMostrando.setText("Mostrando " + (total == 0 ? 0 : desde + 1)
                + " – " + hasta + " de " + total + " usuarios");
        actualizarPaginacion();
    }

    // =========================================================================
    // FILA
    // =========================================================================
    private HBox buildFila(Usuario u, boolean par) {
        HBox fila = new HBox(0);
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.setPadding(new Insets(10, 16, 10, 16));
        String bgN = "-fx-background-color:" + (par ? WHITE : "#fafbfd") + ";"
                + "-fx-border-color:transparent transparent " + BORDER + " transparent;"
                + "-fx-border-width:0 0 1 0;";
        fila.setStyle(bgN);
        fila.setOnMouseEntered(e -> fila.setStyle(
                "-fx-background-color:#EEF2FF;"
                + "-fx-border-color:transparent transparent " + BORDER + " transparent;"
                + "-fx-border-width:0 0 1 0;-fx-cursor:hand;"));
        fila.setOnMouseExited(e -> fila.setStyle(bgN));

        // Col 1: Avatar + nombre
        HBox celdaUsuario = new HBox(10);
        celdaUsuario.setAlignment(Pos.CENTER_LEFT);
        celdaUsuario.setPrefWidth(240);
        celdaUsuario.setMinWidth(240);
        celdaUsuario.setMaxWidth(240);
        StackPane avatarBox = new StackPane();
        Circle avatar = new Circle(20, Color.web(colorAvatar(u.getPrimer_nombre())));
        Label avatarLbl = label(iniciales(u), 12, WHITE, true);
        avatarBox.getChildren().addAll(avatar, avatarLbl);
        VBox nombreBox = new VBox(2);
        Label nombreLbl = new Label(nombreCompleto(u) + " " + apellidoCompleto(u));
        nombreLbl.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#111827;");
        nombreLbl.setMaxWidth(170);
        nombreLbl.setEllipsisString("…");
        Label usernameSub = label("@" + (u.getUsername() != null ? u.getUsername() : "—"), 11, GRAY_TEXT, false);
        nombreBox.getChildren().addAll(nombreLbl, usernameSub);
        celdaUsuario.getChildren().addAll(avatarBox, nombreBox);

        Label cedula = celdaFija(u.getIdentificacion(), 130);
        Label correo = celdaFija(u.getCorreo() != null ? u.getCorreo() : "—", 200);
        Label telefono = celdaFija(u.getTelefono() != null ? u.getTelefono() : "—", 130);

        // Col 5: Rol badge
        String rolNombre = u.getRol() != null ? u.getRol().getNombre() : "—";
        Label rolLbl = new Label(rolNombre);
        rolLbl.setStyle("-fx-background-color:" + rolBg(rolNombre) + ";"
                + "-fx-text-fill:" + rolColor(rolNombre) + ";"
                + "-fx-font-size:11px;-fx-font-weight:bold;"
                + "-fx-background-radius:20;-fx-padding:4 10 4 10;");
        HBox rolBox = new HBox(rolLbl);
        rolBox.setAlignment(Pos.CENTER_LEFT);
        rolBox.setPrefWidth(140);
        rolBox.setMinWidth(140);
        rolBox.setMaxWidth(140);

        // Col 6: Estado badge
        HBox estadoBox = new HBox(5);
        estadoBox.setAlignment(Pos.CENTER_LEFT);
        estadoBox.setPrefWidth(130);
        estadoBox.setMinWidth(130);
        estadoBox.setMaxWidth(130);
        Circle estadoDot = new Circle(4, Color.web(estadoColor(u.getEstado())));
        Label estadoLbl = new Label(u.getEstado() != null ? u.getEstado().name() : "—");
        estadoLbl.setStyle("-fx-background-color:" + estadoBg(u.getEstado()) + ";"
                + "-fx-text-fill:" + estadoColor(u.getEstado()) + ";"
                + "-fx-font-size:11px;-fx-font-weight:bold;"
                + "-fx-background-radius:20;-fx-padding:4 10 4 10;");
        estadoBox.getChildren().addAll(estadoDot, estadoLbl);

        // Col 7: Acciones
        HBox acciones = new HBox(6);
        acciones.setAlignment(Pos.CENTER_LEFT);
        acciones.setPrefWidth(120);
        acciones.setMinWidth(120);
        acciones.setMaxWidth(120);
        acciones.getChildren().addAll(
                btnAccion("\uf06e", "#1565c0", "#e8f0fe", "Ver", () -> abrirDialogoVer(u)),
                btnAccion("\uf044", "#fb8c00", "#fff8e1", "Editar", () -> abrirDialogoCambiarEstado(u)),
                btnAccion("\uf2ed", "#e53935", "#fff0f0", "Borrar", () -> confirmarEliminar(u))
        );

        fila.getChildren().addAll(celdaUsuario, cedula, correo, telefono, rolBox, estadoBox, acciones);
        return fila;
    }

    private Label celdaFija(String txt, double width) {
        Label l = new Label(txt != null ? txt : "—");
        l.setStyle("-fx-font-size:13px;-fx-text-fill:#374151;");
        l.setPrefWidth(width);
        l.setMinWidth(width);
        l.setMaxWidth(width);
        l.setEllipsisString("…");
        return l;
    }

    // =========================================================================
    // DIÁLOGO VER
    // =========================================================================
    private void abrirDialogoVer(Usuario u) {
        Dialog<Void> dlg = new Dialog<>();
        dlg.setTitle("Detalle de Usuario");
        dlg.setHeaderText(null);

        VBox content = new VBox(14);
        content.setPadding(new Insets(20));
        content.setPrefWidth(420);

        StackPane avatarBox = new StackPane(
                new Circle(35, Color.web(colorAvatar(u.getPrimer_nombre()))),
                label(iniciales(u), 22, WHITE, true));

        Label nombre = new Label(nombreCompleto(u) + " " + apellidoCompleto(u));
        nombre.setFont(Font.font("System", FontWeight.BOLD, 18));
        nombre.setTextFill(Color.web("#111827"));

        VBox header = new VBox(6, avatarBox, nombre);
        header.setAlignment(Pos.CENTER);

        content.getChildren().addAll(
                header, new Separator(),
                detalleRow("📋 Cédula", u.getIdentificacion()),
                detalleRow("📞 Teléfono", u.getTelefono() != null ? u.getTelefono() : "—"),
                detalleRow("📧 Correo", u.getCorreo() != null ? u.getCorreo() : "—"),
                detalleRow("👤 Username", u.getUsername()),
                detalleRow("🔐 Rol", u.getRol() != null ? u.getRol().getNombre() : "—"),
                detalleRow("📍 Dirección", direccionTexto(u)),
                detalleRow("🔘 Estado", u.getEstado() != null ? u.getEstado().name() : "—")
        );

        dlg.getDialogPane().setContent(content);
        dlg.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dlg.showAndWait();
    }

    // =========================================================================
    // DIÁLOGO CAMBIAR ESTADO
    // =========================================================================
    private void abrirDialogoCambiarEstado(Usuario u) {
        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle("Cambiar estado — " + u.getUsername());
        dlg.setHeaderText(null);

        VBox content = new VBox(16);
        content.setPadding(new Insets(24));
        content.setPrefWidth(360);

        Label lblNombre = new Label(nombreCompleto(u) + " " + apellidoCompleto(u));
        lblNombre.setFont(Font.font("System", FontWeight.BOLD, 15));
        lblNombre.setTextFill(Color.web("#111827"));
        Label lblCedula = label("Cédula: " + u.getIdentificacion(), 12, GRAY_TEXT, false);
        Label lblRol = label("Rol: " + (u.getRol() != null ? u.getRol().getNombre() : "—"), 12, GRAY_TEXT, false);

        Label lblTitulo = label("Nuevo estado *", 12, GRAY_TEXT, false);
        ComboBox<String> cmbEstado = new ComboBox<>();
        for (EstadoUsuario e : EstadoUsuario.values()) {
            cmbEstado.getItems().add(e.name());
        }
        if (u.getEstado() != null) {
            cmbEstado.setValue(u.getEstado().name());
        }
        cmbEstado.setPrefHeight(40);
        cmbEstado.setMaxWidth(Double.MAX_VALUE);
        cmbEstado.setStyle("-fx-background-radius:8;-fx-font-size:13px;");

        Label lblError = label("", 12, RED, false);
        lblError.setWrapText(true);

        content.getChildren().addAll(
                lblNombre, lblCedula, lblRol,
                new Separator(),
                lblTitulo, cmbEstado,
                lblError);

        dlg.getDialogPane().setContent(content);
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button btnOk = (Button) dlg.getDialogPane().lookupButton(ButtonType.OK);
        btnOk.setText("Guardar estado");
        btnOk.setStyle("-fx-background-color:#1565c0;-fx-text-fill:white;"
                + "-fx-font-weight:bold;-fx-background-radius:8;-fx-padding:8 16;");

        btnOk.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            lblError.setText("");
            if (cmbEstado.getValue() == null) {
                lblError.setText("Selecciona un estado.");
                ev.consume();
                return;
            }
            if (u.getEstado() != null && u.getEstado().name().equals(cmbEstado.getValue())) {
                return; // sin cambios
            }
            try {
                EstadoUsuario nuevoEstado = EstadoUsuario.valueOf(cmbEstado.getValue());
                u.setEstado(nuevoEstado);

                // ── Usar suspender/activar según el nuevo estado ──────────
                if (nuevoEstado == EstadoUsuario.SUSPENDIDO) {
                    usuarioService.suspender(u.getIdentificacion());

                } else if (nuevoEstado == EstadoUsuario.ACTIVO) {
                    usuarioService.activar(u.getIdentificacion());

                } else {
                    // INACTIVO u otros — actualiza normalmente sin correo
                    usuarioService.actualizar(u);
                }

                cargarUsuarios();

            } catch (IllegalArgumentException ex) {
                lblError.setText("Error de validación: " + ex.getMessage());
                ev.consume();
            } catch (SQLException ex) {
                lblError.setText("Error BD: " + ex.getMessage());
                ev.consume();
            }
        });

        dlg.showAndWait();
    }

    // =========================================================================
    // DIÁLOGO CREAR  (excluye POLICIA — ese se crea desde PoliciasAdminPoliciaView)
    // =========================================================================
    private void abrirDialogoCrear() {
        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle("Nuevo Usuario");
        dlg.setHeaderText(null);

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setPrefSize(480, 520);
        scroll.setStyle("-fx-background:white;-fx-background-color:white;");

        VBox form = new VBox(12);
        form.setPadding(new Insets(20));
        form.setStyle("-fx-background-color:white;");

        TextField fPrimerNombre = dlgField("Primer Nombre *", "");
        TextField fSegundoNombre = dlgField("Segundo Nombre", "");
        TextField fPrimerApellido = dlgField("Primer Apellido *", "");
        TextField fSegundoApellido = dlgField("Segundo Apellido", "");
        TextField fCedula = dlgField("Cédula *", "");
        TextField fTelefono = dlgField("Teléfono", "");
        TextField fCorreo = dlgField("Correo", "");
        TextField fUsername = dlgField("Username *", "");
        PasswordField fPassword = dlgPassword("Contraseña *");

        // Combo roles: excluye POLICIA
        ComboBox<String> cmbRol = new ComboBox<>();
        cmbRol.setPromptText("Seleccionar Rol *");
        cmbRol.setPrefHeight(40);
        cmbRol.setMaxWidth(Double.MAX_VALUE);
        cmbRol.setStyle(
                "-fx-background-color:#f5f7fb;-fx-background-radius:10;"
                + "-fx-border-color:transparent;-fx-border-radius:10;"
                + "-fx-font-size:13px;-fx-cursor:hand;");

        if (rolService != null) {
            try {
                for (RolUsuario r : rolService.listar()) {
                    if (r != null && r.getNombre() != null) {
                        // Excluye POLICIA — ese se crea desde PoliciasAdminPoliciaView
                        if (!r.getNombre().toUpperCase().equals("POLICIA")) {
                            cmbRol.getItems().add(r.getNombre());
                        }
                    }
                }
            } catch (Exception e) {
                mostrarAlerta("Error roles", e.getMessage());
            }
        }

        Label lblError = label("", 12, RED, false);
        lblError.setWrapText(true);

        form.getChildren().addAll(
                seccion("DATOS PERSONALES"),
                fila2(fPrimerNombre, fSegundoNombre),
                fila2(fPrimerApellido, fSegundoApellido),
                fila2(fCedula, fTelefono),
                fCorreo,
                seccion("CUENTA"),
                fUsername,
                fPassword,
                seccion("ROL"),
                cmbRol,
                lblError
        );

        scroll.setContent(form);
        dlg.getDialogPane().setContent(scroll);
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button btnOk = (Button) dlg.getDialogPane().lookupButton(ButtonType.OK);
        btnOk.setText("Crear Usuario");
        btnOk.setStyle("-fx-background-color:#1565c0;-fx-text-fill:white;"
                + "-fx-font-weight:bold;-fx-background-radius:8;-fx-padding:8 16;");

        btnOk.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            lblError.setText("");

            // Validación campos obligatorios
            if (fPrimerNombre.getText().isBlank()
                    || fPrimerApellido.getText().isBlank()
                    || fCedula.getText().isBlank()
                    || fUsername.getText().isBlank()
                    || fPassword.getText().isBlank()
                    || cmbRol.getValue() == null) {
                lblError.setText("Completa los campos obligatorios (*).");
                ev.consume();
                return;
            }

            try {
                Usuario nuevo = new Usuario();
                nuevo.setPrimer_nombre(fPrimerNombre.getText().trim());
                nuevo.setSegundo_nombre(fSegundoNombre.getText().trim());
                nuevo.setPrimer_apellido(fPrimerApellido.getText().trim());
                nuevo.setSegundo_apellido(fSegundoApellido.getText().trim());
                nuevo.setIdentificacion(fCedula.getText().trim());
                nuevo.setTelefono(fTelefono.getText().trim());
                nuevo.setCorreo(fCorreo.getText().trim());
                nuevo.setUsername(fUsername.getText().trim());
                nuevo.setPassword(fPassword.getText());
                nuevo.setEstado(EstadoUsuario.ACTIVO);

                RolUsuario rolSel = rolService.listar().stream()
                        .filter(r -> r.getNombre().equalsIgnoreCase(cmbRol.getValue()))
                        .findFirst().orElse(null);
                nuevo.setRol(rolSel);

                // Dirección mínima requerida por el procedimiento
                sistemagestion.model.Direccion dir = new sistemagestion.model.Direccion();
                sistemagestion.model.Barrio barrioVacio = new sistemagestion.model.Barrio();
                barrioVacio.setNombre("");
                dir.setBarrio(barrioVacio);
                dir.setCalle("");
                dir.setCarrera("");
                dir.setEtapa("");
                dir.setManzana("");
                dir.setCasa("");
                nuevo.setDireccion(dir);

                usuarioService.insertar(nuevo);
                cargarUsuarios();

            } catch (IllegalArgumentException ex) {
                lblError.setText("Error: " + ex.getMessage());
                ev.consume();
            } catch (SQLException ex) {
                lblError.setText("Error BD: " + ex.getMessage());
                ev.consume();
            }
        });

        dlg.showAndWait();
    }

    // =========================================================================
    // CONFIRMAR ELIMINAR
    // =========================================================================
    private void confirmarEliminar(Usuario u) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Eliminar usuario");
        confirm.setHeaderText("¿Eliminar a " + nombreCompleto(u) + "?");
        confirm.setContentText("Esta acción no se puede deshacer.");
        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) {
                try {
                    usuarioService.eliminar(u.getIdentificacion());
                    cargarUsuarios();
                } catch (IllegalArgumentException | SQLException ex) {
                    mostrarAlerta("Error al eliminar", ex.getMessage());
                }
            }
        });
    }

    // =========================================================================
    // PAGINACIÓN
    // =========================================================================
    private void actualizarPaginacion() {
        if (paginacionBox == null) {
            return;
        }
        paginacionBox.getChildren().clear();

        int total = usuariosFiltrados.size();
        int totalPags = (int) Math.ceil((double) total / FILAS_POR_PAGINA);
        if (totalPags <= 1) {
            return;
        }

        paginacionBox.getChildren().add(btnPag("‹", paginaActual > 1, () -> {
            paginaActual--;
            renderizarPagina();
        }));

        int ini = Math.max(1, paginaActual - 2);
        int fin = Math.min(totalPags, paginaActual + 2);

        if (ini > 1) {
            paginacionBox.getChildren().addAll(
                    btnPag("1", true, () -> {
                        paginaActual = 1;
                        renderizarPagina();
                    }),
                    label("…", 13, GRAY_TEXT, false));
        }
        for (int i = ini; i <= fin; i++) {
            final int pg = i;
            paginacionBox.getChildren().add(btnPag(String.valueOf(i), true, () -> {
                paginaActual = pg;
                renderizarPagina();
            }));
        }
        if (fin < totalPags) {
            paginacionBox.getChildren().addAll(
                    label("…", 13, GRAY_TEXT, false),
                    btnPag(String.valueOf(totalPags), true, () -> {
                        paginaActual = totalPags;
                        renderizarPagina();
                    }));
        }

        paginacionBox.getChildren().add(btnPag("›", paginaActual < totalPags, () -> {
            paginaActual++;
            renderizarPagina();
        }));
    }

    private Button btnPag(String txt, boolean enabled, Runnable accion) {
        Button b = new Button(txt);
        b.setDisable(!enabled);
        boolean esActual = txt.equals(String.valueOf(paginaActual));
        b.setStyle("-fx-background-color:" + (esActual ? BLUE : WHITE) + ";"
                + "-fx-text-fill:" + (esActual ? WHITE : "#374151") + ";"
                + "-fx-background-radius:6;-fx-padding:6 11;-fx-cursor:hand;"
                + "-fx-font-size:13px;-fx-border-color:" + BORDER + ";-fx-border-radius:6;");
        b.setOnAction(e -> accion.run());
        return b;
    }

    // =========================================================================
    // STATS
    // =========================================================================
    private void actualizarStats() {
        long total = todosLosUsuarios.size();
        long activos = todosLosUsuarios.stream()
                .filter(u -> u.getEstado() != null && "ACTIVO".equalsIgnoreCase(u.getEstado().name()))
                .count();
        long inactivos = total - activos;

        lblTotalVal.setText(String.valueOf(total));
        lblTotalVal.setStyle("-fx-font-size:36px;-fx-font-weight:bold;-fx-text-fill:" + BLUE + ";");
        lblActivosVal.setText(String.valueOf(activos));
        lblActivosVal.setStyle("-fx-font-size:36px;-fx-font-weight:bold;-fx-text-fill:" + GREEN + ";");
        lblInactivosVal.setText(String.valueOf(inactivos));
        lblInactivosVal.setStyle("-fx-font-size:36px;-fx-font-weight:bold;-fx-text-fill:" + RED + ";");

        if (rolService != null) {
            try {
                lblRolesVal.setText(String.valueOf(rolService.listar().size()));
                lblRolesVal.setStyle("-fx-font-size:36px;-fx-font-weight:bold;-fx-text-fill:#7b1fa2;");
            } catch (Exception e) {
                lblRolesVal.setText("0");
            }
        }
    }

    // =========================================================================
    // HELPERS UI
    // =========================================================================
    private Button styledBtn(String text, String color, String hover) {
        Button b = new Button(text);
        b.setPrefHeight(40);
        String base = "-fx-background-color:" + color + ";-fx-text-fill:white;"
                + "-fx-font-size:13px;-fx-font-weight:bold;"
                + "-fx-background-radius:8;-fx-padding:8 18;-fx-cursor:hand;";
        String hov = "-fx-background-color:" + hover + ";-fx-text-fill:white;"
                + "-fx-font-size:13px;-fx-font-weight:bold;"
                + "-fx-background-radius:8;-fx-padding:8 18;-fx-cursor:hand;";
        b.setStyle(base);
        b.setOnMouseEntered(e -> b.setStyle(hov));
        b.setOnMouseExited(e -> b.setStyle(base));
        return b;
    }

    private Button btnAccion(String iconFA, String iconColor,
            String bgColor, String tooltip, Runnable accion) {
        Button b = new Button(iconFA);
        String base = "-fx-background-color:" + bgColor + ";-fx-text-fill:" + iconColor + ";"
                + "-fx-font-family:'Font Awesome 6 Free Solid';-fx-font-size:13px;"
                + "-fx-background-radius:8;-fx-padding:7 10;-fx-cursor:hand;";
        String hov = "-fx-background-color:" + iconColor + ";-fx-text-fill:white;"
                + "-fx-font-family:'Font Awesome 6 Free Solid';-fx-font-size:13px;"
                + "-fx-background-radius:8;-fx-padding:7 10;-fx-cursor:hand;";
        b.setStyle(base);
        b.setOnMouseEntered(e -> b.setStyle(hov));
        b.setOnMouseExited(e -> b.setStyle(base));
        b.setOnAction(e -> accion.run());
        Tooltip.install(b, new Tooltip(tooltip));
        return b;
    }

    private HBox detalleRow(String campo, String valor) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        Label k = label(campo, 13, GRAY_TEXT, false);
        k.setMinWidth(130);
        row.getChildren().addAll(k, label(valor != null ? valor : "—", 13, "#111827", false));
        return row;
    }

    private TextField dlgField(String prompt, String val) {
        TextField f = new TextField(val);
        f.setPromptText(prompt);
        f.setPrefHeight(40);
        f.setMaxWidth(Double.MAX_VALUE);
        f.setStyle("-fx-background-color:#f5f7fb;-fx-background-radius:8;"
                + "-fx-border-radius:8;-fx-border-color:transparent;"
                + "-fx-padding:0 14;-fx-font-size:13px;");
        return f;
    }

    private PasswordField dlgPassword(String prompt) {
        PasswordField f = new PasswordField();
        f.setPromptText(prompt);
        f.setPrefHeight(40);
        f.setMaxWidth(Double.MAX_VALUE);
        f.setStyle("-fx-background-color:#f5f7fb;-fx-background-radius:8;"
                + "-fx-border-radius:8;-fx-border-color:transparent;"
                + "-fx-padding:0 14;-fx-font-size:13px;");
        return f;
    }

    private HBox fila2(javafx.scene.Node a, javafx.scene.Node b) {
        HBox row = new HBox(10);
        HBox.setHgrow(a, Priority.ALWAYS);
        HBox.setHgrow(b, Priority.ALWAYS);
        row.getChildren().addAll(a, b);
        return row;
    }

    private Label seccion(String txt) {
        Label l = label(txt, 11, "#888", true);
        VBox.setMargin(l, new Insets(8, 0, 0, 0));
        return l;
    }

    private Label label(String text, double size, String color, boolean bold) {
        Label lbl = new Label(text);
        lbl.setFont(bold ? Font.font("System", FontWeight.BOLD, size) : Font.font("System", size));
        lbl.setTextFill(Color.web(color));
        return lbl;
    }

    private void shadow(Region node) {
        node.setEffect(new DropShadow(12, 0, 2, Color.web("#0000001a")));
    }

    private void mostrarAlerta(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    // ── String helpers ─────────────────────────────────────────────
    private String nombreCompleto(Usuario u) {
        return (u.getPrimer_nombre() != null ? u.getPrimer_nombre() : "")
                + (u.getSegundo_nombre() != null && !u.getSegundo_nombre().isBlank()
                ? " " + u.getSegundo_nombre() : "");
    }

    private String apellidoCompleto(Usuario u) {
        return (u.getPrimer_apellido() != null ? u.getPrimer_apellido() : "")
                + (u.getSegundo_apellido() != null && !u.getSegundo_apellido().isBlank()
                ? " " + u.getSegundo_apellido() : "");
    }

    private String iniciales(Usuario u) {
        String n = u.getPrimer_nombre() != null && !u.getPrimer_nombre().isBlank()
                ? u.getPrimer_nombre().substring(0, 1).toUpperCase() : "?";
        String a = u.getPrimer_apellido() != null && !u.getPrimer_apellido().isBlank()
                ? u.getPrimer_apellido().substring(0, 1).toUpperCase() : "";
        return n + a;
    }

    private String direccionTexto(Usuario u) {
        if (u.getDireccion() == null) {
            return "—";
        }
        var d = u.getDireccion();
        String bar = d.getBarrio() != null ? d.getBarrio().getNombre() : "";
        return bar + " · Calle " + d.getCalle() + " Cra " + d.getCarrera();
    }

    // ── Colores ─────────────────────────────────────────────────────
    private static final String[] AVATAR_COLORS = {
        "#1565c0", "#2e7d32", "#6a1b9a", "#c62828", "#e65100",
        "#00695c", "#283593", "#4e342e", "#37474f", "#558b2f"
    };

    private String colorAvatar(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return AVATAR_COLORS[0];
        }
        return AVATAR_COLORS[Math.abs(nombre.hashCode()) % AVATAR_COLORS.length];
    }

    private String rolColor(String rol) {
        if (rol == null) {
            return GRAY_TEXT;
        }
        return switch (rol.toUpperCase()) {
            case "ADMIN" ->
                BLUE;
            case "ADMINISTRADOR_POLICIA" ->
                "#7b1fa2";
            case "RESIDENTE" ->
                GREEN;
            default ->
                GRAY_TEXT;
        };
    }

    private String rolBg(String rol) {
        if (rol == null) {
            return "#f3f4f6";
        }
        return switch (rol.toUpperCase()) {
            case "ADMIN" ->
                "#e8f0fe";
            case "ADMINISTRADOR_POLICIA" ->
                "#f3e5f5";
            case "RESIDENTE" ->
                "#e8f5e9";
            default ->
                "#f3f4f6";
        };
    }

    private String estadoColor(EstadoUsuario estado) {
        if (estado == null) {
            return GRAY_TEXT;
        }
        return switch (estado) {
            case ACTIVO ->
                GREEN;
            case INACTIVO ->
                RED;
            case SUSPENDIDO ->
                ORANGE;
        };
    }

    private String estadoBg(EstadoUsuario estado) {
        if (estado == null) {
            return "#f3f4f6";
        }
        return switch (estado) {
            case ACTIVO ->
                "#e8f5e9";
            case INACTIVO ->
                RED_LIGHT;
            case SUSPENDIDO ->
                "#fff8e1";
        };
    }
}
