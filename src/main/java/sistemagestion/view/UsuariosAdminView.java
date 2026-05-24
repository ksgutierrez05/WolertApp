/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.view;

/**
 *
 * @author Maria Cristina
 */
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */


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

import sistemagestion.model.RolUsuario;
import sistemagestion.model.Usuario;
import sistemagestion.service.RolUsuarioService;
import sistemagestion.service.UsuarioService;

/**
 * Vista de Gestión de Usuarios para el Panel Administrativo.
 * Se integra en AdministradorApp mediante: root.setCenter(new UsuariosAdminView().getView())
 *
 * @author Maria Cristina
 */
public class UsuariosAdminView {

    // ── Paleta idéntica a AdministradorApp ────────────────────────
    private static final String WHITE     = "#ffffff";
    private static final String BG        = "#f4f6fb";
    private static final String RED       = "#e53935";
    private static final String RED_LIGHT = "#fff0f0";
    private static final String ORANGE    = "#fb8c00";
    private static final String GREEN     = "#43a047";
    private static final String BLUE      = "#1565c0";
    private static final String GRAY_TEXT = "#6b7280";
    private static final String BORDER    = "#e5e7eb";

    // ── Servicios ─────────────────────────────────────────────────
    private UsuarioService    usuarioService;
    private RolUsuarioService rolService;

    // ── Estado de la tabla ────────────────────────────────────────
    private ObservableList<Usuario> todosLosUsuarios = FXCollections.observableArrayList();
    private ObservableList<Usuario> usuariosFiltrados = FXCollections.observableArrayList();

    // ── Paginación ────────────────────────────────────────────────
    private static final int FILAS_POR_PAGINA = 8;
    private int paginaActual = 1;

    // ── Referencias UI ────────────────────────────────────────────
    private VBox tablaContainer;
    private Label lblMostrando;
    private HBox paginacionBox;
    private TextField campoBusqueda;
    private ComboBox<String> filtroEstado;
    private ComboBox<String> filtroRol;

    // ── Stats labels (para actualizar en vivo) ────────────────────
    private Label lblTotalVal;
    private Label lblActivosVal;
    private Label lblInactivosVal;
    private Label lblRolesVal;

    // =========================================================================
    // Constructor
    // =========================================================================
    public UsuariosAdminView() {
        try {
            usuarioService = new UsuarioService();
            rolService     = new RolUsuarioService();
        } catch (SQLException e) {
            mostrarAlerta("Error de conexión", e.getMessage());
        }
    }

    // =========================================================================
    // ENTRY POINT — llama AdministradorApp con: root.setCenter(new UsuariosAdminView().getView())
    // =========================================================================
    public ScrollPane getView() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color: " + BG + ";");

        content.getChildren().addAll(
                buildTopBar(),
                buildStatsRow(),
                buildToolbar(),
                buildTabla(),
                buildPaginacion()
        );

        // Cargar datos reales
        cargarUsuarios();

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: " + BG + "; -fx-background: " + BG + ";");
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

        Button btnNuevo = new Button("+ Nuevo usuario");
        btnNuevo.setStyle("""
                -fx-background-color: #1565c0;
                -fx-text-fill: white;
                -fx-font-size: 13px;
                -fx-font-weight: bold;
                -fx-background-radius: 8;
                -fx-padding: 10 18;
                -fx-cursor: hand;
                """);
        btnNuevo.setOnMouseEntered(e -> btnNuevo.setStyle("""
                -fx-background-color: #0d47a1;
                -fx-text-fill: white;
                -fx-font-size: 13px;
                -fx-font-weight: bold;
                -fx-background-radius: 8;
                -fx-padding: 10 18;
                -fx-cursor: hand;
                """));
        btnNuevo.setOnMouseExited(e -> btnNuevo.setStyle("""
                -fx-background-color: #1565c0;
                -fx-text-fill: white;
                -fx-font-size: 13px;
                -fx-font-weight: bold;
                -fx-background-radius: 8;
                -fx-padding: 10 18;
                -fx-cursor: hand;
                """));
        btnNuevo.setOnAction(e -> abrirDialogoCrear());

        right.getChildren().add(btnNuevo);
        bar.getChildren().addAll(titles, right);
        return bar;
    }

    // =========================================================================
    // STATS ROW  (4 tarjetas)
    // =========================================================================
    private HBox buildStatsRow() {
        HBox row = new HBox(16);

        lblTotalVal    = boldNum("0");
        lblActivosVal  = boldNum("0");
        lblInactivosVal= boldNum("0");
        lblRolesVal    = boldNum("4");

        row.getChildren().addAll(
                statCard("👥", "#e8f0fe", BLUE,   "Total registrados",        lblTotalVal, "↑ +24 este mes",        BLUE),
                statCard("✅", "#e8f5e9", GREEN,  "Usuarios ACTIVOS",         lblActivosVal, "+18 este mes",        GREEN),
                statCard("🚨", RED_LIGHT, RED,    "Inactivos / Suspendidos",  lblInactivosVal, "↑ 5% este mes",     RED),
                statCard("🏠", "#f3e5f5", "#7b1fa2","Roles configurados",     lblRolesVal, "Admin · Policía · Usuario", GRAY_TEXT)
        );
        return row;
    }

    private Label boldNum(String val) {
        Label l = new Label(val);
        l.setFont(Font.font("System", FontWeight.BOLD, 32));
        l.setTextFill(Color.web("#111827"));
        return l;
    }

    private VBox statCard(String icon, String bgIcon, String iconColor,
                          String title, Label valueLabel, String sub, String subColor) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: " + WHITE + "; -fx-background-radius: 12;");
        HBox.setHgrow(card, Priority.ALWAYS);
        shadow(card);

        HBox top = new HBox(12);
        top.setAlignment(Pos.CENTER_LEFT);
        StackPane iconBox = new StackPane();
        Rectangle iconBg = new Rectangle(44, 44);
        iconBg.setArcWidth(10); iconBg.setArcHeight(10);
        iconBg.setFill(Color.web(bgIcon));
        Label iconLbl = label(icon, 20, iconColor, false);
        iconBox.getChildren().addAll(iconBg, iconLbl);
        top.getChildren().addAll(iconBox, label(title, 12, GRAY_TEXT, false));

        card.getChildren().addAll(top, valueLabel, label(sub, 11, subColor, false));
        return card;
    }

    // =========================================================================
    // TOOLBAR  (búsqueda + filtros)
    // =========================================================================
    private HBox buildToolbar() {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(16));
        bar.setStyle("-fx-background-color: " + WHITE + "; -fx-background-radius: 12;");
        shadow(bar);

        // Búsqueda
        campoBusqueda = new TextField();
        campoBusqueda.setPromptText("🔍  Buscar por nombre, cédula o username...");
        campoBusqueda.setPrefHeight(40);
        campoBusqueda.setStyle("""
                -fx-background-color: #f5f7fb;
                -fx-background-radius: 8;
                -fx-border-radius: 8;
                -fx-border-color: transparent;
                -fx-padding: 0 14;
                -fx-font-size: 13px;
                """);
        HBox.setHgrow(campoBusqueda, Priority.ALWAYS);
        campoBusqueda.textProperty().addListener((obs, o, n) -> filtrarYMostrar());

        // Filtro Estado
        filtroEstado = new ComboBox<>();
        filtroEstado.getItems().addAll("Estado: Todos", "ACTIVO", "INACTIVO", "SUSPENDIDO");
        filtroEstado.setValue("Estado: Todos");
        filtroEstado.setPrefHeight(40);
        filtroEstado.setStyle("-fx-background-radius: 8; -fx-font-size: 13px;");
        filtroEstado.setOnAction(e -> filtrarYMostrar());

        // Filtro Rol
        filtroRol = new ComboBox<>();
        filtroRol.getItems().add("Rol: Todos");
        filtroRol.setValue("Rol: Todos");
        filtroRol.setPrefHeight(40);
        filtroRol.setStyle("-fx-background-radius: 8; -fx-font-size: 13px;");
        filtroRol.setOnAction(e -> filtrarYMostrar());

        // Cargar roles disponibles
        cargarFiltroRoles();

        bar.getChildren().addAll(campoBusqueda, filtroEstado, filtroRol);
        return bar;
    }

    // =========================================================================
    // TABLA
    // =========================================================================
    private VBox buildTabla() {
        VBox card = new VBox(0);
        card.setStyle("-fx-background-color: " + WHITE + "; -fx-background-radius: 12;");
        shadow(card);

        // Encabezado
        HBox header = new HBox();
        header.setPadding(new Insets(14, 16, 14, 16));
        header.setStyle("-fx-border-color: transparent transparent " + BORDER + " transparent; -fx-border-width: 0 0 1 0;");
        header.getChildren().addAll(
                colHeader("Usuario",               0.22),
                colHeader("Identificación",        0.12),
                colHeader("Correo electrónico",    0.20),
                colHeader("Teléfono",              0.12),
                colHeader("Username",              0.10),
                colHeader("Rol",                   0.08),
                colHeader("Estado",                0.09),
                colHeader("Acciones",              0.07)
        );
        card.getChildren().add(header);

        // Contenedor de filas (se actualiza con paginación)
        tablaContainer = new VBox(0);
        card.getChildren().add(tablaContainer);

        // Footer con info de paginación
        lblMostrando = label("Mostrando 0 – 0 de 0 usuarios", 12, GRAY_TEXT, false);
        HBox footer = new HBox();
        footer.setPadding(new Insets(12, 16, 12, 16));
        footer.setStyle("-fx-border-color: " + BORDER + " transparent transparent transparent; -fx-border-width: 1 0 0 0;");
        footer.getChildren().add(lblMostrando);
        card.getChildren().add(footer);

        return card;
    }

    private Label colHeader(String text, double pct) {
        Label l = label(text, 12, GRAY_TEXT, false);
        l.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(l, Priority.ALWAYS);
        l.setMinWidth(0);
        // Peso proporcional vía maxWidth no aplica directo; usamos region con grow
        return l;
    }

    // =========================================================================
    // PAGINACIÓN
    // =========================================================================
    private HBox buildPaginacion() {
        paginacionBox = new HBox(6);
        paginacionBox.setAlignment(Pos.CENTER_RIGHT);
        actualizarPaginacion();
        return paginacionBox;
    }

    // =========================================================================
    // CARGA Y FILTRADO
    // =========================================================================
    private void cargarUsuarios() {
        if (usuarioService == null) return;
        try {
            List<Usuario> lista = usuarioService.listar();
            todosLosUsuarios.setAll(lista);
            filtrarYMostrar();
            actualizarStats();
        } catch (SQLException e) {
            mostrarAlerta("Error al cargar usuarios", e.getMessage());
        }
    }

    private void cargarFiltroRoles() {
        if (rolService == null) return;
        try {
            List<RolUsuario> roles = rolService.listar();
            roles.forEach(r -> filtroRol.getItems().add(r.getNombre()));
        } catch (Exception e) {
            // Si falla, dejamos solo "Rol: Todos"
        }
    }

    private void filtrarYMostrar() {
        String busqueda = campoBusqueda.getText().toLowerCase().trim();
        String estado   = filtroEstado.getValue();
        String rol      = filtroRol.getValue();

        List<Usuario> filtrados = todosLosUsuarios.stream()
                .filter(u -> {
                    boolean matchBusq = busqueda.isEmpty()
                            || nombreCompleto(u).toLowerCase().contains(busqueda)
                            || u.getIdentificacion().toLowerCase().contains(busqueda)
                            || u.getUsername().toLowerCase().contains(busqueda);
                    boolean matchEstado = estado == null || estado.startsWith("Estado")
                            || (u.getEstado() != null && u.getEstado().name().equalsIgnoreCase(estado));
                    boolean matchRol = rol == null || rol.startsWith("Rol")
                            || (u.getRol() != null && u.getRol().getNombre().equalsIgnoreCase(rol));
                    return matchBusq && matchEstado && matchRol;
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
            Label vacio = label("No se encontraron usuarios", 14, GRAY_TEXT, false);
            VBox.setMargin(vacio, new Insets(30));
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
    // FILA DE TABLA
    // =========================================================================
    private HBox buildFila(Usuario u, boolean par) {
        HBox fila = new HBox();
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.setPadding(new Insets(12, 16, 12, 16));
        fila.setStyle("-fx-background-color: " + (par ? WHITE : "#fafbfd") + ";"
                + "-fx-border-color: transparent transparent " + BORDER + " transparent;"
                + "-fx-border-width: 0 0 1 0;");
        fila.setOnMouseEntered(e -> fila.setStyle("""
                -fx-background-color: #EEF2FF;
                -fx-border-color: transparent transparent #e5e7eb transparent;
                -fx-border-width: 0 0 1 0;
                -fx-cursor: hand;
                """));
        fila.setOnMouseExited(e -> fila.setStyle("-fx-background-color: " + (par ? WHITE : "#fafbfd") + ";"
                + "-fx-border-color: transparent transparent " + BORDER + " transparent;"
                + "-fx-border-width: 0 0 1 0;"));

        // Celda: Avatar + nombre
        HBox celdaUsuario = new HBox(10);
        celdaUsuario.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(celdaUsuario, Priority.ALWAYS);

        Circle avatar = new Circle(18, Color.web(colorAvatar(u.getPrimer_nombre())));
        Label avatarLbl = label(iniciales(u), 11, WHITE, true);
        StackPane avatarBox = new StackPane(avatar, avatarLbl);

        VBox nombreBox = new VBox(1);
        nombreBox.getChildren().addAll(
                label(nombreCompleto(u), 13, "#111827", true),
                label(apellidoCompleto(u), 11, GRAY_TEXT, false)
        );
        celdaUsuario.getChildren().addAll(avatarBox, nombreBox);

        // Celdas simples
        Label cedula   = celda(u.getIdentificacion());
        Label correo   = celda(u.getCorreo() != null ? u.getCorreo() : "—");
        Label telefono = celda(u.getTelefono() != null ? u.getTelefono() : "—");
        Label username = celda(u.getUsername());

        // Rol badge
        String rolNombre = u.getRol() != null ? u.getRol().getNombre() : "—";
        Label rolLbl = label(rolNombre, 11, rolColor(rolNombre), true);
        rolLbl.setPadding(new Insets(3, 8, 3, 8));
        rolLbl.setStyle("-fx-background-color: " + rolBg(rolNombre) + ";"
                + "-fx-background-radius: 20;");
        HBox rolBox = new HBox(rolLbl);
        rolBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(rolBox, Priority.ALWAYS);

        // Estado badge
        String estadoStr = u.getEstado() != null ? u.getEstado().name() : "—";
        Label estadoLbl = label(estadoStr, 11, estadoColor(estadoStr), true);
        estadoLbl.setPadding(new Insets(3, 8, 3, 8));
        estadoLbl.setStyle("-fx-background-color: " + estadoBg(estadoStr) + ";"
                + "-fx-background-radius: 20;");
        HBox estadoBox = new HBox(estadoLbl);
        estadoBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(estadoBox, Priority.ALWAYS);

        // Acciones
        HBox acciones = new HBox(6);
        acciones.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(acciones, Priority.ALWAYS);
        acciones.getChildren().addAll(
                btnAccion("👁", "#0284c7", () -> abrirDialogoVer(u)),
                btnAccion("✏", "#f59e0b", () -> abrirDialogoEditar(u)),
                btnAccion("🗑", "#ef4444", () -> confirmarEliminar(u))
        );

        fila.getChildren().addAll(
                celdaUsuario, cedula, correo, telefono,
                username, rolBox, estadoBox, acciones
        );
        return fila;
    }

    // =========================================================================
    // DIALOGO VER USUARIO
    // =========================================================================
    private void abrirDialogoVer(Usuario u) {
        Dialog<Void> dlg = new Dialog<>();
        dlg.setTitle("Detalle de Usuario");
        dlg.setHeaderText(null);

        VBox content = new VBox(14);
        content.setPadding(new Insets(20));
        content.setPrefWidth(420);

        // Avatar grande
        Circle avatar = new Circle(35, Color.web(colorAvatar(u.getPrimer_nombre())));
        Label avatarLbl = label(iniciales(u), 22, WHITE, true);
        StackPane avatarBox = new StackPane(avatar, avatarLbl);

        Label nombre = new Label(nombreCompleto(u) + " " + apellidoCompleto(u));
        nombre.setFont(Font.font("System", FontWeight.BOLD, 18));
        nombre.setTextFill(Color.web("#111827"));

        VBox header = new VBox(6);
        header.setAlignment(Pos.CENTER);
        header.getChildren().addAll(avatarBox, nombre);

        content.getChildren().addAll(
                header,
                new Separator(),
                detalleRow("📋 Cédula",     u.getIdentificacion()),
                detalleRow("📞 Teléfono",   u.getTelefono() != null ? u.getTelefono() : "—"),
                detalleRow("📧 Correo",     u.getCorreo() != null ? u.getCorreo() : "—"),
                detalleRow("👤 Username",   u.getUsername()),
                detalleRow("🔐 Rol",        u.getRol() != null ? u.getRol().getNombre() : "—"),
                detalleRow("📍 Dirección",  direccionTexto(u))
        );

        dlg.getDialogPane().setContent(content);
        dlg.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dlg.showAndWait();
    }

    // =========================================================================
    // DIALOGO CREAR USUARIO
    // =========================================================================
    private void abrirDialogoCrear() {
        Dialog<ButtonType> dlg = crearDialogoFormulario(null);
        dlg.setTitle("Nuevo Usuario");
        dlg.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) cargarUsuarios();
        });
    }

    // =========================================================================
    // DIALOGO EDITAR USUARIO
    // =========================================================================
    private void abrirDialogoEditar(Usuario u) {
        Dialog<ButtonType> dlg = crearDialogoFormulario(u);
        dlg.setTitle("Editar Usuario — " + u.getUsername());
        dlg.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) cargarUsuarios();
        });
    }

    /**
     * Formulario reutilizable para crear/editar.
     * Si u == null → modo creación; si u != null → modo edición (precarga campos).
     */
    private Dialog<ButtonType> crearDialogoFormulario(Usuario u) {
        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setHeaderText(null);

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setPrefSize(480, 520);
        scroll.setStyle("-fx-background: white; -fx-background-color: white;");

        VBox form = new VBox(12);
        form.setPadding(new Insets(20));
        form.setStyle("-fx-background-color: white;");

        // Campos
        TextField fPrimerNombre    = dlgField("Primer Nombre *",   u != null ? u.getPrimer_nombre()    : "");
        TextField fSegundoNombre   = dlgField("Segundo Nombre",    u != null ? u.getSegundo_nombre()   : "");
        TextField fPrimerApellido  = dlgField("Primer Apellido *", u != null ? u.getPrimer_apellido()  : "");
        TextField fSegundoApellido = dlgField("Segundo Apellido",  u != null ? u.getSegundo_apellido() : "");
        TextField fCedula          = dlgField("Cédula *",          u != null ? u.getIdentificacion()   : "");
        TextField fTelefono        = dlgField("Teléfono",          u != null ? u.getTelefono()         : "");
        TextField fCorreo          = dlgField("Correo",            u != null ? u.getCorreo()           : "");
        TextField fUsername        = dlgField("Username *",        u != null ? u.getUsername()         : "");
        PasswordField fPassword    = dlgPassword("Contraseña" + (u != null ? " (dejar vacío = no cambiar)" : " *"));

        // Modo edición: cédula no editable
        if (u != null) fCedula.setDisable(true);

        // ComboBox Rol — solo admin puede asignar
        ComboBox<String> cmbRol = new ComboBox<>();
        cmbRol.setPromptText("Seleccionar Rol *");
        cmbRol.setPrefHeight(40);
        cmbRol.setMaxWidth(Double.MAX_VALUE);
        cmbRol.setStyle("-fx-background-radius: 8; -fx-font-size: 13px;");
        if (rolService != null) {
            try {
                rolService.listar().forEach(r -> cmbRol.getItems().add(r.getNombre()));
            } catch (Exception ignored) {}
        }
        if (u != null && u.getRol() != null) cmbRol.setValue(u.getRol().getNombre());

        // ComboBox Estado (solo en edición)
        ComboBox<String> cmbEstado = new ComboBox<>();
        cmbEstado.getItems().addAll("ACTIVO", "INACTIVO", "SUSPENDIDO");
        cmbEstado.setPrefHeight(40);
        cmbEstado.setMaxWidth(Double.MAX_VALUE);
        cmbEstado.setStyle("-fx-background-radius: 8; -fx-font-size: 13px;");
        if (u != null && u.getEstado() != null) cmbEstado.setValue(u.getEstado().name());

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
                seccion("ROL Y ESTADO"),
                cmbRol,
                u != null ? cmbEstado : new Label(""),
                lblError
        );

        scroll.setContent(form);
        dlg.getDialogPane().setContent(scroll);
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Personalizar botón OK
        Button btnOk = (Button) dlg.getDialogPane().lookupButton(ButtonType.OK);
        btnOk.setText(u == null ? "Crear Usuario" : "Guardar Cambios");
        btnOk.setStyle("-fx-background-color: #1565c0; -fx-text-fill: white; "
                + "-fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 16;");

        // Validación antes de cerrar
        btnOk.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            lblError.setText("");

            if (fPrimerNombre.getText().isBlank() || fPrimerApellido.getText().isBlank()
                    || fUsername.getText().isBlank()
                    || (u == null && fPassword.getText().isBlank())
                    || cmbRol.getValue() == null) {
                lblError.setText("Completa los campos obligatorios (*)");
                ev.consume();
                return;
            }

            try {
                if (u == null) {
                    // CREAR
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
                    RolUsuario rol = new RolUsuario();
                    rol.setNombre(cmbRol.getValue());
                    nuevo.setRol(rol);
                    usuarioService.insertar(nuevo);
                } else {
                    // EDITAR
                    u.setPrimer_nombre(fPrimerNombre.getText().trim());
                    u.setSegundo_nombre(fSegundoNombre.getText().trim());
                    u.setPrimer_apellido(fPrimerApellido.getText().trim());
                    u.setSegundo_apellido(fSegundoApellido.getText().trim());
                    u.setTelefono(fTelefono.getText().trim());
                    u.setCorreo(fCorreo.getText().trim());
                    u.setUsername(fUsername.getText().trim());
                    if (!fPassword.getText().isBlank()) u.setPassword(fPassword.getText());
                    RolUsuario rol = new RolUsuario();
                    rol.setNombre(cmbRol.getValue());
                    u.setRol(rol);
                    usuarioService.actualizar(u);
                }
            } catch (IllegalArgumentException ex) {
                lblError.setText(ex.getMessage());
                ev.consume();
            } catch (SQLException ex) {
                lblError.setText("Error BD: " + ex.getMessage());
                ev.consume();
            }
        });

        return dlg;
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
    // PAGINACIÓN — render de botones
    // =========================================================================
    private void actualizarPaginacion() {
        if (paginacionBox == null) return;
        paginacionBox.getChildren().clear();

        int total = usuariosFiltrados.size();
        int totalPaginas = (int) Math.ceil((double) total / FILAS_POR_PAGINA);
        if (totalPaginas <= 1) return;

        paginacionBox.getChildren().add(btnPag("‹", paginaActual > 1,
                () -> { paginaActual--; renderizarPagina(); }));

        int inicio = Math.max(1, paginaActual - 2);
        int fin    = Math.min(totalPaginas, paginaActual + 2);

        if (inicio > 1) {
            paginacionBox.getChildren().addAll(btnPag("1", true, () -> { paginaActual = 1; renderizarPagina(); }),
                    label("...", 13, GRAY_TEXT, false));
        }
        for (int i = inicio; i <= fin; i++) {
            final int pg = i;
            paginacionBox.getChildren().add(btnPag(String.valueOf(i), true,
                    () -> { paginaActual = pg; renderizarPagina(); }));
        }
        if (fin < totalPaginas) {
            paginacionBox.getChildren().addAll(label("...", 13, GRAY_TEXT, false),
                    btnPag(String.valueOf(totalPaginas), true,
                            () -> { paginaActual = totalPaginas; renderizarPagina(); }));
        }

        paginacionBox.getChildren().add(btnPag("›", paginaActual < totalPaginas,
                () -> { paginaActual++; renderizarPagina(); }));
    }

    private Button btnPag(String txt, boolean enabled, Runnable accion) {
        Button b = new Button(txt);
        b.setDisable(!enabled);
        boolean esActual = txt.equals(String.valueOf(paginaActual));
        b.setStyle("-fx-background-color: " + (esActual ? BLUE : WHITE) + ";"
                + "-fx-text-fill: " + (esActual ? WHITE : "#374151") + ";"
                + "-fx-background-radius: 6; -fx-padding: 6 11; -fx-cursor: hand;"
                + "-fx-font-size: 13px; -fx-border-color: " + BORDER + "; -fx-border-radius: 6;");
        b.setOnAction(e -> accion.run());
        return b;
    }

    // =========================================================================
    // STATS — actualizar contadores
    // =========================================================================
    private void actualizarStats() {
        long total   = todosLosUsuarios.size();
        long activos = todosLosUsuarios.stream()
                .filter(u -> u.getEstado() != null && "ACTIVO".equalsIgnoreCase(u.getEstado().name()))
                .count();
        long inactivos = total - activos;

        lblTotalVal.setText(String.valueOf(total));
        lblActivosVal.setText(String.valueOf(activos));
        lblInactivosVal.setText(String.valueOf(inactivos));

        if (rolService != null) {
            try { lblRolesVal.setText(String.valueOf(rolService.listar().size())); }
            catch (Exception ignored) {}
        }
    }

    // =========================================================================
    // HELPERS UI
    // =========================================================================
    private Label celda(String txt) {
        Label l = label(txt != null ? txt : "—", 13, "#374151", false);
        l.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(l, Priority.ALWAYS);
        l.setEllipsisString("…");
        l.setMaxWidth(160);
        return l;
    }

    private Button btnAccion(String icon, String color, Runnable accion) {
        Button b = new Button(icon);
        b.setStyle("-fx-background-color: transparent; -fx-font-size: 14px; "
                + "-fx-cursor: hand; -fx-padding: 4 6;");
        b.setOnMouseEntered(e -> b.setStyle("-fx-background-color: #f0f0f0; "
                + "-fx-font-size: 14px; -fx-cursor: hand; -fx-background-radius: 6; -fx-padding: 4 6;"));
        b.setOnMouseExited(e  -> b.setStyle("-fx-background-color: transparent; "
                + "-fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 4 6;"));
        b.setOnAction(e -> accion.run());
        return b;
    }

    private HBox detalleRow(String campo, String valor) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        Label k = label(campo, 13, GRAY_TEXT, false);
        k.setMinWidth(130);
        Label v = label(valor != null ? valor : "—", 13, "#111827", false);
        row.getChildren().addAll(k, v);
        return row;
    }

    private TextField dlgField(String prompt, String val) {
        TextField f = new TextField(val);
        f.setPromptText(prompt);
        f.setPrefHeight(40);
        f.setMaxWidth(Double.MAX_VALUE);
        f.setStyle("-fx-background-color: #f5f7fb; -fx-background-radius: 8; "
                + "-fx-border-radius: 8; -fx-border-color: transparent; "
                + "-fx-padding: 0 14; -fx-font-size: 13px;");
        return f;
    }

    private PasswordField dlgPassword(String prompt) {
        PasswordField f = new PasswordField();
        f.setPromptText(prompt);
        f.setPrefHeight(40);
        f.setMaxWidth(Double.MAX_VALUE);
        f.setStyle("-fx-background-color: #f5f7fb; -fx-background-radius: 8; "
                + "-fx-border-radius: 8; -fx-border-color: transparent; "
                + "-fx-padding: 0 14; -fx-font-size: 13px;");
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
        lbl.setFont(bold
                ? Font.font("System", FontWeight.BOLD, size)
                : Font.font("System", size));
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

    // ── Texto helpers ─────────────────────────────────────────────
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
        if (u.getDireccion() == null) return "—";
        var d = u.getDireccion();
        String barrio = d.getBarrio() != null ? d.getBarrio().getNombre() : "";
        return barrio + " · Calle " + d.getCalle() + " Cra " + d.getCarrera();
    }

    // ── Colores dinámicos ─────────────────────────────────────────
    private static final String[] AVATAR_COLORS = {
        "#1565c0","#2e7d32","#6a1b9a","#c62828","#e65100",
        "#00695c","#283593","#4e342e","#37474f","#558b2f"
    };

    private String colorAvatar(String nombre) {
        if (nombre == null || nombre.isBlank()) return AVATAR_COLORS[0];
        return AVATAR_COLORS[Math.abs(nombre.hashCode()) % AVATAR_COLORS.length];
    }

    private String rolColor(String rol) {
        if (rol == null) return GRAY_TEXT;
        return switch (rol.toUpperCase()) {
            case "ADMIN"      -> BLUE;
            case "POLICIA"    -> "#7b1fa2";
            case "RESIDENTE"  -> GREEN;
            default           -> GRAY_TEXT;
        };
    }

    private String rolBg(String rol) {
        if (rol == null) return "#f3f4f6";
        return switch (rol.toUpperCase()) {
            case "ADMIN"     -> "#e8f0fe";
            case "POLICIA"   -> "#f3e5f5";
            case "RESIDENTE" -> "#e8f5e9";
            default          -> "#f3f4f6";
        };
    }

    private String estadoColor(String estado) {
        if (estado == null) return GRAY_TEXT;
        return switch (estado.toUpperCase()) {
            case "ACTIVO"      -> GREEN;
            case "SUSPENDIDO"  -> ORANGE;
            case "INACTIVO"    -> RED;
            default            -> GRAY_TEXT;
        };
    }

    private String estadoBg(String estado) {
        if (estado == null) return "#f3f4f6";
        return switch (estado.toUpperCase()) {
            case "ACTIVO"     -> "#e8f5e9";
            case "SUSPENDIDO" -> "#fff8e1";
            case "INACTIVO"   -> RED_LIGHT;
            default           -> "#f3f4f6";
        };
    }
}
