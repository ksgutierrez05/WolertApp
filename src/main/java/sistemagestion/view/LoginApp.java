/*
     * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
     * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.view;

import java.sql.SQLException;
import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import sistemagestion.model.Direccion;
import sistemagestion.model.EstadoUsuario;
import sistemagestion.model.RolUsuario;
import sistemagestion.model.Usuario;
import sistemagestion.service.UsuarioService;
import sistemagestion.service.ComunaService;
import sistemagestion.service.BarrioService;
import sistemagestion.model.Comuna;
import sistemagestion.model.Barrio;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class LoginApp {

    private static final double PANEL_W = 500;
    private static final double PANEL_H = 650;
    private static final double EXTRA = 40;
    private static final double RADIUS = 50;

    private AnchorPane root;
    private VBox loginForm;
    private VBox registerForm;
    private StackPane overlayPanel;
    private boolean signUpMode = false;
    private UsuarioService usuarioService;
    private ComunaService comunaService;
    private BarrioService barrioService;

    private TextField loginUsername;
    private PasswordField loginPassword;
    private Label loginMsg;

    private TextField regPrimerNombre, regSegundoNombre;
    private TextField regPrimerApellido, regSegundoApellido;
    private TextField regCedula, regTelefono, regEmail;
    private TextField regUsername;
    private PasswordField regPassword;
    private ComboBox<String> regComuna;
    private ComboBox<String> regBarrio;
    private TextField regCalle, regCarrera;
    private TextField regEtapa, regManzana, regCasa;
    private Label regMsg;

    // ─────────────────────────────────────────────────────────────────────────
    public Parent getView() {
        try {
            usuarioService = new UsuarioService();
        } catch (SQLException ex) {
            throw new RuntimeException("Error UsuarioService: " + ex.getMessage(), ex);
        }
        try {
            comunaService = new ComunaService();
        } catch (SQLException ex) {
            throw new RuntimeException("Error ComunaService: " + ex.getMessage(), ex);
        }
        try {
            barrioService = new BarrioService();
        } catch (SQLException ex) {
            throw new RuntimeException("Error BarrioService: " + ex.getMessage(), ex);
        }

        root = new AnchorPane();
        root.setPrefSize(1000, PANEL_H);
        root.setStyle("-fx-background-color: white;");
        root.setClip(new Rectangle(1000, PANEL_H));

        loginForm = createLoginForm();
        loginForm.setLayoutX(600);
        loginForm.setLayoutY(170);

        registerForm = createRegisterForm();
        registerForm.setLayoutX(10);
        registerForm.setLayoutY(10);
        registerForm.setVisible(false);
        registerForm.setManaged(false);
        registerForm.setOpacity(0);

        overlayPanel = createOverlayPanel();
        overlayPanel.setLayoutX(-EXTRA);
        overlayPanel.setLayoutY(0);

        root.getChildren().addAll(loginForm, registerForm, overlayPanel);
        return root;
    }

    // ════════════════════════════════════════════════════════════════════════
    // REDIRECCIÓN POR ROL
    // ════════════════════════════════════════════════════════════════════════
    private void redirigirSegunRol(Usuario u, Stage stage) {
        String rol = u.getRol() != null ? u.getRol().getNombre().toUpperCase().trim() : "";
        switch (rol) {
            case "ADMIN":
                AdministradorApp admin = new AdministradorApp();
                admin.show(stage);
                break;
            case "CIUDADANO":
                UsuarioApp ciudadano = new UsuarioApp(u);
                ciudadano.show(stage);
                break;

            case "POLICIA":
                PoliciaApp policia = new PoliciaApp(u);
                policia.show(stage);
                break;

            case "ADMIN_POLICIA":
                AdministradorPoliciaApp adminview = new AdministradorPoliciaApp(u);
                adminview.show(stage);
                break;

            default:
                setMsg(loginMsg,
                        "Tu cuenta no tiene acceso configurado. Contacta al administrador.",
                        false);
                loginPassword.clear();
                loginUsername.requestFocus();
                break;

        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // PANEL AZUL
    // ════════════════════════════════════════════════════════════════════════
    private StackPane createOverlayPanel() {
        double totalW = PANEL_W + EXTRA * 2;

        Rectangle bg = new Rectangle(totalW, PANEL_H);
        bg.setArcWidth(RADIUS * 2);
        bg.setArcHeight(RADIUS * 2);
        bg.setFill(new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#16283d")),
                new Stop(1, Color.web("#223f63"))));

        Circle circle = new Circle(55);
        circle.setFill(Color.rgb(255, 255, 255, 0.2));

        Image logo = new Image(getClass().getResource("/LogoWolertAPP.png").toExternalForm());
        ImageView icon = new ImageView(logo);
        icon.setFitWidth(220);
        icon.setFitHeight(220);
        icon.setPreserveRatio(true);

        Label title = new Label("WolertApp");
        title.setStyle("-fx-font-size: 34px; -fx-font-weight: bold; -fx-text-fill: white;");
        VBox.setMargin(title, new Insets(-50, 0, 0, 0));

        Label text = new Label(
                "Sistema de Alertas Comunitarias\n\n"
                + "Seguridad para tu barrio\ny notificaciones en tiempo real");
        text.setStyle("-fx-font-size: 15px; -fx-text-fill: white; -fx-text-alignment: center;");
        VBox.setMargin(text, new Insets(0, 0, 20, 0));

        Button switchBtn = new Button("REGISTRARSE");
        switchBtn.setStyle("""
                    -fx-background-color: white; -fx-border-color: white; -fx-border-width: 2;
                    -fx-text-fill: #16283d; -fx-font-size: 14px; -fx-font-weight: bold;
                    -fx-background-radius: 30; -fx-border-radius: 30;
                    -fx-padding: 10 35; -fx-cursor: hand;
                    """);
        switchBtn.setOnAction(e -> {
            if (!signUpMode) {
                animateToRegister(switchBtn);
            } else {
                animateToLogin(switchBtn);
            }
            signUpMode = !signUpMode;
        });

        VBox content = new VBox(20, new StackPane(circle, icon), title, text, switchBtn);
        content.setAlignment(Pos.CENTER);
        content.setTranslateY(-30);

        StackPane panel = new StackPane(bg, content);
        panel.setPrefSize(totalW, PANEL_H);
        return panel;
    }

    // ════════════════════════════════════════════════════════════════════════
    // FORMULARIO LOGIN
    // ════════════════════════════════════════════════════════════════════════
    private VBox createLoginForm() {
        loginUsername = createField("👤 Usuario");
        loginPassword = createPassword("🔒 Contraseña");
        loginMsg = new Label();
        Button btnLogin = createButton("LOGIN");

        loginUsername.setOnAction(e -> loginPassword.requestFocus());
        loginPassword.setOnAction(e -> btnLogin.fire());

        btnLogin.setOnAction(e -> {
            if (loginUsername.getText().isBlank() || loginPassword.getText().isBlank()) {
                setMsg(loginMsg, "Completa todos los campos", false);
                return;
            }
            try {
                Usuario u = usuarioService.login(
                        loginUsername.getText().trim(),
                        loginPassword.getText().trim());

                if (u != null) {
                    setMsg(loginMsg, "¡Bienvenido, " + u.getPrimer_nombre() + "!", true);
                    PauseTransition pausa = new PauseTransition(Duration.millis(800));
                    pausa.setOnFinished(ev -> {
                        Stage stage = (Stage) loginUsername.getScene().getWindow();
                        redirigirSegunRol(u, stage);
                    });
                    pausa.play();
                } else {
                    setMsg(loginMsg, "Usuario o contraseña incorrectos", false);
                    loginPassword.clear();
                    loginPassword.requestFocus();
                }
            } catch (Exception ex) {
                setMsg(loginMsg, "Error de conexión. Intenta de nuevo.", false);
                ex.printStackTrace();
            }
        });

        Label title = new Label("Iniciar Sesión");
        title.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #444;");

        VBox form = new VBox(15, title, loginUsername, loginPassword, btnLogin, loginMsg);
        form.setAlignment(Pos.CENTER);
        form.setPadding(new Insets(40));
        form.setPrefWidth(350);
        return form;
    }

    // ════════════════════════════════════════════════════════════════════════
    // FORMULARIO REGISTRO
    // ════════════════════════════════════════════════════════════════════════
    private VBox createRegisterForm() {

        regPrimerNombre = modernField("Primer Nombre *");
        regSegundoNombre = modernField("Segundo Nombre");
        regPrimerApellido = modernField("Primer Apellido *");
        regSegundoApellido = modernField("Segundo Apellido");
        regCedula = modernField("Cédula *");
        regTelefono = modernField("Teléfono * (10 dígitos)");
        regEmail = modernField("Correo Electrónico *");
        regUsername = modernField("Username * (mín. 4 caracteres)");
        regPassword = modernPassword("Contraseña * (mín. 8, 1 mayúscula, 1 número)");

        regComuna = new ComboBox<>();
        regComuna.setPromptText("Seleccione una comuna");
        regComuna.setMaxWidth(Double.MAX_VALUE);
        regComuna.setPrefHeight(50);
        regComuna.setStyle("-fx-background-color:#f5f7fb;-fx-background-radius:25;"
                + "-fx-border-radius:25;-fx-border-color:transparent;-fx-font-size:13px;");
        try {
            List<Comuna> comunas = comunaService.listar();
            ObservableList<String> nombres = FXCollections.observableArrayList();
            for (Comuna c : comunas) {
                nombres.add(c.getNombre());
            }
            regComuna.setItems(nombres);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        regBarrio = new ComboBox<>();
        regBarrio.setPromptText("Seleccione un barrio");
        regBarrio.setMaxWidth(Double.MAX_VALUE);
        regBarrio.setPrefHeight(50);
        regBarrio.setStyle("-fx-background-color: #f5f7fb; -fx-background-radius: 25;"
                + "-fx-border-radius: 25; -fx-border-color: transparent;"
                + "-fx-font-size: 13px;");
        regBarrio.setDisable(true);

        regComuna.setOnAction(e -> {
            String comunaSeleccionada = regComuna.getValue();
            if (comunaSeleccionada == null) {
                return;
            }
            regBarrio.getItems().clear();
            regBarrio.setDisable(true);
            try {
                List<Barrio> barrios = barrioService.listar();
                ObservableList<String> nombresBarrios = FXCollections.observableArrayList();
                for (Barrio b : barrios) {
                    if (b.getComuna() != null
                            && comunaSeleccionada.equals(b.getComuna().getNombre())) {
                        nombresBarrios.add(b.getNombre());
                    }
                }
                regBarrio.setItems(nombresBarrios);
                regBarrio.setDisable(nombresBarrios.isEmpty());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        regCalle = modernField("Calle");
        regCarrera = modernField("Carrera");
        regEtapa = modernField("Etapa");
        regManzana = modernField("Manzana");
        regCasa = modernField("Casa");
        regMsg = new Label();
        regMsg.setStyle("-fx-font-size:12px;");
        regMsg.setWrapText(true);
        regMsg.setMaxWidth(390);

        ScrollPane scroll = buildScroll();

        chain(regPrimerNombre, regSegundoNombre, regPrimerApellido, regSegundoApellido,
                regCedula, regTelefono, regEmail, regUsername);
        regPassword.setOnAction(e -> {
            regBarrio.requestFocus();
            animateScroll(scroll, 0.90);
        });
        chain(regCalle, regCarrera, regEtapa, regManzana, regCasa);

        Button btnRegister = new Button("CREAR CUENTA");
        btnRegister.setPrefWidth(250);
        String btnNormal = "-fx-background-color: linear-gradient(to right, #16283d, #1f3a56);"
                + "-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;"
                + "-fx-background-radius: 30; -fx-padding: 14 20; -fx-cursor: hand;";
        String btnHover = "-fx-background-color: linear-gradient(to right, #0f1c2b, #16283d);"
                + "-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;"
                + "-fx-background-radius: 30; -fx-padding: 14 20; -fx-cursor: hand;";
        btnRegister.setStyle(btnNormal);
        btnRegister.setOnMouseEntered(e -> btnRegister.setStyle(btnHover));
        btnRegister.setOnMouseExited(e -> btnRegister.setStyle(btnNormal));
        regCasa.setOnAction(e -> btnRegister.fire());

        btnRegister.setOnAction(e -> {
            // ── Obligatorios personales ───────────────────────────────────
            StringBuilder errores = new StringBuilder();

            if (regPrimerNombre.getText().isBlank()) {
                highlight(regPrimerNombre);
                errores.append("• El primer nombre es obligatorio.\n");
            }
            if (regPrimerApellido.getText().isBlank()) {
                highlight(regPrimerApellido);
                errores.append("• El primer apellido es obligatorio.\n");
            }
            if (regCedula.getText().isBlank()) {
                highlight(regCedula);
                errores.append("• La cédula es obligatoria.\n");
            } else if (!regCedula.getText().matches("\\d+")) {
                highlight(regCedula);
                errores.append("• La cédula solo debe contener números.\n");
            }
            if (regTelefono.getText().isBlank()) {
                highlight(regTelefono);
                errores.append("• El teléfono es obligatorio.\n");
            } else if (!regTelefono.getText().matches("\\d{10}")) {
                highlight(regTelefono);
                errores.append("• El teléfono debe tener exactamente 10 dígitos.\n");
            }
            if (regEmail.getText().isBlank()) {
                highlight(regEmail);
                errores.append("• El correo electrónico es obligatorio.\n");
            } else if (!regEmail.getText().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                highlight(regEmail);
                errores.append("• El correo electrónico no es válido.\n");
            }
            if (regUsername.getText().isBlank()) {
                highlight(regUsername);
                errores.append("• El username es obligatorio.\n");
            } else if (regUsername.getText().length() < 4) {
                highlight(regUsername);
                errores.append("• El username debe tener al menos 4 caracteres.\n");
            }
            if (regPassword.getText().isBlank()) {
                highlight(regPassword);
                errores.append("• La contraseña es obligatoria.\n");
            } else if (regPassword.getText().length() < 8) {
                highlight(regPassword);
                errores.append("• La contraseña debe tener al menos 8 caracteres.\n");
            } else if (!regPassword.getText().matches("^(?=.*[A-Z])(?=.*\\d).+$")) {
                highlight(regPassword);
                errores.append("• La contraseña debe tener al menos 1 mayúscula y 1 número.\n");
            }
            if (regBarrio.getValue() == null) {
                regBarrio.setStyle("-fx-background-color:#f5f7fb;-fx-background-radius:25;"
                        + "-fx-border-radius:25;-fx-border-color:#e53935;-fx-border-width:1.5;-fx-font-size:13px;");
                errores.append("• Debes seleccionar un barrio.\n");
            }

            // Si hay errores mostrarlos todos
            if (errores.length() > 0) {
                setMsg(regMsg, errores.toString().trim(), false);
                return;
            }

            // Todo OK → enviar
            clearHighlights();
            try {
                usuarioService.insertar(buildUsuario());
                setMsg(regMsg, "Usuario registrado correctamente.", true);
                limpiarRegistro();
            } catch (Exception ex) {
                setMsg(regMsg, "Error al registrar. Verifica los datos e intenta de nuevo.", false);
                ex.printStackTrace();
            }
        });

        Label title = new Label("Crear Cuenta");
        title.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: #444;");
        Label subtitle = new Label("Completa la información para registrarte");
        subtitle.setStyle("-fx-text-fill: #888; -fx-font-size: 13px;");

        VBox form = new VBox(14,
                title, subtitle, new Separator(),
                sectionTitle("DATOS PERSONALES"),
                modernRow(regPrimerNombre, regSegundoNombre),
                modernRow(regPrimerApellido, regSegundoApellido),
                modernRow(regCedula, regTelefono),
                regEmail,
                sectionTitle("CUENTA"),
                regUsername, regPassword,
                sectionTitle("DIRECCIÓN"),
                regComuna, regBarrio,
                modernRow(regCalle, regCarrera),
                modernRow(regEtapa, regManzana),
                regCasa,
                new Separator(),
                btnRegister, regMsg
        );
        form.setPadding(new Insets(40, 30, 60, 30));
        form.setAlignment(Pos.TOP_CENTER);
        form.setStyle("-fx-background-color: white;");

        scroll.setContent(form);

        VBox wrapper = new VBox(scroll);
        wrapper.setAlignment(Pos.CENTER);
        return wrapper;
    }

    // Poner borde rojo en campo inválido
    private void highlight(Control field) {
        String base = "-fx-background-color:#f5f7fb;-fx-background-radius:25;"
                + "-fx-border-radius:25;-fx-border-width:1.5;-fx-padding:0 20;-fx-font-size:13px;";
        field.setStyle(base + "-fx-border-color:#e53935;");
        // Quitar el rojo cuando el usuario empiece a escribir
        if (field instanceof TextField tf) {
            tf.textProperty().addListener((obs, o, n) -> tf.setStyle(base + "-fx-border-color:transparent;"));
        } else if (field instanceof PasswordField pf) {
            pf.textProperty().addListener((obs, o, n) -> pf.setStyle(base + "-fx-border-color:transparent;"));
        }
    }

    // Limpiar todos los highlights
    private void clearHighlights() {
        String base = "-fx-background-color:#f5f7fb;-fx-background-radius:25;"
                + "-fx-border-radius:25;-fx-border-color:transparent;"
                + "-fx-padding:0 20;-fx-font-size:13px;";
        for (TextField tf : new TextField[]{
            regPrimerNombre, regSegundoNombre, regPrimerApellido, regSegundoApellido,
            regCedula, regTelefono, regEmail, regUsername,
            regCalle, regCarrera, regEtapa, regManzana, regCasa
        }) {
            tf.setStyle(base);
        }
        regPassword.setStyle(base);
        regBarrio.setStyle("-fx-background-color:#f5f7fb;-fx-background-radius:25;"
                + "-fx-border-radius:25;-fx-border-color:transparent;-fx-font-size:13px;");
    }

    // ════════════════════════════════════════════════════════════════════════
    // ANIMACIONES
    // ════════════════════════════════════════════════════════════════════════
    private void animateToRegister(Button switchBtn) {
        limpiarRegistro();
        clearHighlights();
        registerForm.setVisible(true);
        registerForm.setManaged(true);
        registerForm.setOpacity(0);
        registerForm.setTranslateX(80);

        FadeTransition fadeOutLogin = fade(loginForm, 1, 0, 250);
        fadeOutLogin.setOnFinished(e -> {
            loginForm.setVisible(false);
            loginForm.setManaged(false);
        });

        new ParallelTransition(
                fadeOutLogin,
                translate(overlayPanel, PANEL_W, 700),
                fade(registerForm, 0, 1, 500, 200),
                translate(registerForm, 0, 500, 200),
                createScale(registerForm, 0.97, 1.0, 200),
                switchLabel(switchBtn, "INICIAR SESIÓN")
        ).play();
    }

    private void animateToLogin(Button switchBtn) {
        limpiarLogin();
        loginForm.setVisible(true);
        loginForm.setManaged(true);
        loginForm.setOpacity(0);
        loginForm.setTranslateX(-80);

        FadeTransition fadeOutRegister = fade(registerForm, 1, 0, 250);
        fadeOutRegister.setOnFinished(e -> {
            registerForm.setVisible(false);
            registerForm.setManaged(false);
        });

        new ParallelTransition(
                fadeOutRegister,
                translate(overlayPanel, 0, 700),
                fade(loginForm, 0, 1, 500, 200),
                translate(loginForm, 0, 500, 200),
                createScale(loginForm, 0.97, 1.0, 200),
                switchLabel(switchBtn, "REGISTRARSE")
        ).play();
    }

    // ════════════════════════════════════════════════════════════════════════
    // LIMPIEZA
    // ════════════════════════════════════════════════════════════════════════
    private void limpiarLogin() {
        loginUsername.clear();
        loginPassword.clear();
        loginMsg.setText("");
    }

    private void limpiarRegistro() {
        for (TextField tf : new TextField[]{
            regPrimerNombre, regSegundoNombre, regPrimerApellido, regSegundoApellido,
            regCedula, regTelefono, regEmail, regUsername,
            regCalle, regCarrera, regEtapa, regManzana, regCasa
        }) {
            tf.clear();
        }
        regPassword.clear();
        regComuna.setValue(null);
        regBarrio.getItems().clear();
        regBarrio.setValue(null);
        regBarrio.setDisable(true);
        regMsg.setText("");

        clearHighlights();
    }

    // ════════════════════════════════════════════════════════════════════════
    // HELPERS DE UI
    // ════════════════════════════════════════════════════════════════════════
    private TextField createField(String prompt) {
        TextField f = new TextField();
        f.setPromptText(prompt);
        f.setMaxWidth(Double.MAX_VALUE);
        f.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 30;"
                + "-fx-border-radius: 30; -fx-padding: 14 20; -fx-font-size: 14px;");
        return f;
    }

    private PasswordField createPassword(String prompt) {
        PasswordField f = new PasswordField();
        f.setPromptText(prompt);
        f.setMaxWidth(Double.MAX_VALUE);
        f.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 30;"
                + "-fx-border-radius: 30; -fx-padding: 14 20; -fx-font-size: 14px;");
        return f;
    }

    private Button createButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: #16283d; -fx-text-fill: white;"
                + "-fx-font-size: 14px; -fx-font-weight: bold;"
                + "-fx-background-radius: 30; -fx-padding: 12 40; -fx-cursor: hand;");
        return btn;
    }

    private TextField modernField(String prompt) {
        TextField f = new TextField();
        f.setPromptText(prompt);
        f.setPrefHeight(50);
        f.setStyle("-fx-background-color: #f5f7fb; -fx-background-radius: 25;"
                + "-fx-border-radius: 25; -fx-border-color: transparent;"
                + "-fx-padding: 0 20; -fx-font-size: 13px;");
        return f;
    }

    private PasswordField modernPassword(String prompt) {
        PasswordField f = new PasswordField();
        f.setPromptText(prompt);
        f.setPrefHeight(50);
        f.setStyle("-fx-background-color: #f5f7fb; -fx-background-radius: 25;"
                + "-fx-border-radius: 25; -fx-border-color: transparent;"
                + "-fx-padding: 0 20; -fx-font-size: 13px;");
        return f;
    }

    private Label sectionTitle(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #888;");
        return l;
    }

    private HBox modernRow(Node a, Node b) {
        HBox row = new HBox(12, a, b);
        row.setAlignment(Pos.CENTER);
        HBox.setHgrow(a, Priority.ALWAYS);
        HBox.setHgrow(b, Priority.ALWAYS);
        return row;
    }

    private ScrollPane buildScroll() {
        ScrollPane scroll = new ScrollPane();
        scroll.setPrefSize(450, 600);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: white; -fx-background: white;"
                + "-fx-border-color: transparent; -fx-padding: 0;");
        scroll.skinProperty().addListener((obs, ov, nv) -> {
            scroll.lookupAll(".scroll-bar").forEach(n -> n.setStyle(
                    "-fx-background-color: transparent; -fx-padding: 0; -fx-pref-width: 6;"));
            scroll.lookupAll(".track").forEach(n -> n.setStyle(
                    "-fx-background-color: transparent; -fx-border-color: transparent;"));
            scroll.lookupAll(".thumb").forEach(n -> n.setStyle(
                    "-fx-background-color: rgba(120,120,120,0.45); -fx-background-radius: 8;"));
            scroll.lookupAll(".increment-button, .decrement-button").forEach(n
                    -> n.setStyle("-fx-padding: 0; -fx-opacity: 0;"));
            scroll.lookupAll(".increment-arrow, .decrement-arrow").forEach(n
                    -> n.setStyle("-fx-shape: ''; -fx-padding: 0;"));
        });
        return scroll;
    }

    private void setMsg(Label lbl, String msg, boolean ok) {
        lbl.setStyle("-fx-text-fill: " + (ok ? "#27ae60" : "#e74c3c") + "; -fx-font-size: 13px;");
        lbl.setText(msg);
    }

    private void chain(TextField... fields) {
        for (int i = 0; i < fields.length - 1; i++) {
            final TextField next = fields[i + 1];
            fields[i].setOnAction(e -> next.requestFocus());
        }
    }

    private void animateScroll(ScrollPane scroll, double target) {
        new Timeline(new KeyFrame(Duration.millis(300),
                new KeyValue(scroll.vvalueProperty(), target))).play();
    }

    private Usuario buildUsuario() {
        Usuario u = new Usuario();
        u.setPrimer_nombre(regPrimerNombre.getText());
        u.setSegundo_nombre(regSegundoNombre.getText());
        u.setPrimer_apellido(regPrimerApellido.getText());
        u.setSegundo_apellido(regSegundoApellido.getText());
        u.setIdentificacion(regCedula.getText());
        u.setTelefono(regTelefono.getText());
        u.setCorreo(regEmail.getText());
        u.setUsername(regUsername.getText());
        u.setPassword(regPassword.getText());
        u.setEstado(EstadoUsuario.ACTIVO);

        RolUsuario rol = new RolUsuario();
        rol.setNombre("CIUDADANO");
        u.setRol(rol);

        Barrio b = new Barrio();
        b.setNombre(regBarrio.getValue() != null ? regBarrio.getValue() : null);

        Direccion d = new Direccion();
        d.setBarrio(b);
        d.setCalle(regCalle.getText().isBlank() ? null : regCalle.getText());
        d.setCarrera(regCarrera.getText().isBlank() ? null : regCarrera.getText());
        d.setEtapa(regEtapa.getText().isBlank() ? null : regEtapa.getText());
        d.setManzana(regManzana.getText().isBlank() ? null : regManzana.getText());
        d.setCasa(regCasa.getText().isBlank() ? null : regCasa.getText());
        u.setDireccion(d);

        return u;
    }

    // ════════════════════════════════════════════════════════════════════════
    // HELPERS DE ANIMACIÓN
    // ════════════════════════════════════════════════════════════════════════
    private FadeTransition fade(Node node, double from, double to, int ms) {
        FadeTransition ft = new FadeTransition(Duration.millis(ms), node);
        ft.setFromValue(from);
        ft.setToValue(to);
        ft.setInterpolator(Interpolator.EASE_IN);
        return ft;
    }

    private FadeTransition fade(Node node, double from, double to, int ms, int delayMs) {
        FadeTransition ft = fade(node, from, to, ms);
        ft.setDelay(Duration.millis(delayMs));
        return ft;
    }

    private TranslateTransition translate(Node node, double toX, int ms) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(ms), node);
        tt.setToX(toX);
        tt.setInterpolator(Interpolator.SPLINE(0.16, 1.0, 0.3, 1.0));
        return tt;
    }

    private TranslateTransition translate(Node node, double toX, int ms, int delayMs) {
        TranslateTransition tt = translate(node, toX, ms);
        tt.setDelay(Duration.millis(delayMs));
        return tt;
    }

    private ScaleTransition createScale(Node node, double from, double to, int delayMs) {
        ScaleTransition st = new ScaleTransition(Duration.seconds(0.7), node);
        st.setFromX(from);
        st.setFromY(from);
        st.setToX(to);
        st.setToY(to);
        st.setInterpolator(Interpolator.EASE_BOTH);
        st.setDelay(Duration.millis(delayMs));
        return st;
    }

    private FadeTransition switchLabel(Button btn, String newText) {
        FadeTransition out = new FadeTransition(Duration.millis(150), btn);
        out.setToValue(0);
        out.setOnFinished(e -> {
            btn.setText(newText);
            FadeTransition in = new FadeTransition(Duration.millis(200), btn);
            in.setFromValue(0);
            in.setToValue(1);
            in.play();
        });
        return out;
    }
}
