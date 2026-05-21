/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.view;

import javafx.scene.image.Image;
import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.util.Duration;

public class LoginApp {

    private AnchorPane root;
    private VBox loginForm;
    private VBox registerForm;
    private StackPane overlayPanel;
    private boolean signUpMode = false;
    private static final double PANEL_W = 500;
    private static final double EXTRA = 40;
    private static final double PANEL_H = 650;
    private static final double RADIUS = 50;

    // VIEW PRINCIPAL
    public Parent getView() {

        root = new AnchorPane();
        root.setPrefSize(1000, PANEL_H);
        root.setStyle("-fx-background-color: white;");

        // Clip para que el panel extra no se vea fuera de la ventana
        Rectangle rootClip = new Rectangle(1000, PANEL_H);
        root.setClip(rootClip);

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
        // Posicionamos el panel con el offset negativo para que
        // el lado izquierdo extra quede oculto fuera de la ventana
        overlayPanel.setLayoutX(-EXTRA);
        overlayPanel.setLayoutY(0);

        root.getChildren().addAll(loginForm, registerForm, overlayPanel);

        return root;
    }

    // PANEL AZUL
    private StackPane createOverlayPanel() {

        double totalW = PANEL_W + EXTRA * 2;

        StackPane panel = new StackPane();
        panel.setPrefSize(totalW, PANEL_H);

        Rectangle bg = new Rectangle(totalW, PANEL_H);
        bg.setArcWidth(RADIUS * 2);
        bg.setArcHeight(RADIUS * 2);
        bg.setFill(new LinearGradient(
                0, 0, 1, 1, true,
                CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#16283d")),
                new Stop(1, Color.web("#223f63"))
        ));

        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        content.setTranslateY(-30);

        Circle circle = new Circle(55);
        circle.setFill(Color.rgb(255, 255, 255, 0.2));
        System.out.println(getClass().getResource("/LogoWolertAPP.png"));
        System.out.println(getClass().getResource("/Resourcess/LogoWolertAPP.png"));
        System.out.println(
                getClass().getResource("/Resourcess/LogoWolertAPP.png")
        );

        Image logo = new Image(
                getClass()
                        .getResource("/LogoWolertAPP.png")
                        .toExternalForm()
        );
        ImageView icon = new ImageView(logo);

        icon.setFitWidth(220);
        icon.setFitHeight(220);

        icon.setPreserveRatio(true);

        StackPane iconPane = new StackPane(circle, icon);

        Label title = new Label("WolertApp");
        title.setStyle("""
                -fx-font-size: 34px;
                -fx-font-weight: bold;
                -fx-text-fill: white;
                """);
        VBox.setMargin(title, new Insets(-50, 0, 0, 0));

        Label text = new Label("""
                Sistema de Alertas Comunitarias
                
                Seguridad para tu barrio
                y notificaciones en tiempo real
                """);
        text.setStyle("""
                -fx-font-size: 15px;
                -fx-text-fill: white;
                -fx-text-alignment: center;
                """);
        VBox.setMargin(text, new Insets(0, 0, 20, 0));

        Button switchBtn = new Button("REGISTRARSE");
        switchBtn.setStyle("""
                -fx-background-color: white;
                -fx-border-color: white;
                -fx-border-width: 2;
                -fx-text-fill:  #16283d;
                -fx-font-size: 14px;
                -fx-font-weight: bold;
                -fx-background-radius: 30;
                -fx-border-radius: 30;
                -fx-padding: 10 35;
                -fx-cursor: hand;
                """);

        switchBtn.setOnAction(e -> {
            if (!signUpMode) {
                animateToRegister(switchBtn);
            } else {
                animateToLogin(switchBtn);
            }
            signUpMode = !signUpMode;
        });

        content.getChildren().addAll(iconPane, title, text, switchBtn);
        panel.getChildren().addAll(bg, content);

        return panel;
    }

    // ANIMACIÓN A REGISTRO 
    private void animateToRegister(Button switchBtn) {

        registerForm.setVisible(true);
        registerForm.setManaged(true);
        registerForm.setOpacity(0);
        registerForm.setTranslateX(80);

        FadeTransition fadeOutLogin
                = new FadeTransition(Duration.millis(250), loginForm);
        fadeOutLogin.setToValue(0);
        fadeOutLogin.setInterpolator(Interpolator.EASE_IN);
        fadeOutLogin.setOnFinished(e -> {
            loginForm.setVisible(false);
            loginForm.setManaged(false);
        });

        TranslateTransition movePanel
                = new TranslateTransition(Duration.millis(700), overlayPanel);
        movePanel.setToX(PANEL_W);
        movePanel.setInterpolator(Interpolator.SPLINE(0.16, 1.0, 0.3, 1.0));

        FadeTransition fadeInRegister
                = new FadeTransition(Duration.millis(500), registerForm);
        fadeInRegister.setFromValue(0);
        fadeInRegister.setToValue(1);
        fadeInRegister.setDelay(Duration.millis(200));

        TranslateTransition registerSlide
                = new TranslateTransition(Duration.millis(500), registerForm);
        registerSlide.setFromX(80);
        registerSlide.setToX(0);
        registerSlide.setDelay(Duration.millis(200));
        registerSlide.setInterpolator(Interpolator.SPLINE(0.16, 1.0, 0.3, 1.0));

        ScaleTransition scaleRegister = createScale(registerForm, 0.97, 1.0);
        scaleRegister.setDelay(Duration.millis(200));

        // Botón sincronizado con la aparición del formulario
        FadeTransition fadeOutBtn = new FadeTransition(Duration.millis(150), switchBtn);
        fadeOutBtn.setToValue(0);
        //fadeOutBtn.setDelay(Duration.millis(200));
        fadeOutBtn.setOnFinished(e -> {
            switchBtn.setText("INICIAR SESIÓN");
            FadeTransition fadeInBtn = new FadeTransition(Duration.millis(200), switchBtn);
            fadeInBtn.setFromValue(0);
            fadeInBtn.setToValue(1);
            fadeInBtn.play();
        });

        new ParallelTransition(
                fadeOutLogin,
                movePanel,
                fadeInRegister,
                registerSlide,
                scaleRegister,
                fadeOutBtn
        ).play();
    }

    // ANIMACIÓN A LOGIN 
    private void animateToLogin(Button switchBtn) {

        loginForm.setVisible(true);
        loginForm.setManaged(true);
        loginForm.setOpacity(0);
        loginForm.setTranslateX(-80);

        FadeTransition fadeOutRegister
                = new FadeTransition(Duration.millis(250), registerForm);
        fadeOutRegister.setToValue(0);
        fadeOutRegister.setInterpolator(Interpolator.EASE_IN);
        fadeOutRegister.setOnFinished(e -> {
            registerForm.setVisible(false);
            registerForm.setManaged(false);
        });

        TranslateTransition movePanel
                = new TranslateTransition(Duration.millis(700), overlayPanel);
        movePanel.setToX(0);
        movePanel.setInterpolator(Interpolator.SPLINE(0.16, 1.0, 0.3, 1.0));

        FadeTransition fadeInLogin
                = new FadeTransition(Duration.millis(500), loginForm);
        fadeInLogin.setFromValue(0);
        fadeInLogin.setToValue(1);
        fadeInLogin.setDelay(Duration.millis(200));

        TranslateTransition loginSlide
                = new TranslateTransition(Duration.millis(500), loginForm);
        loginSlide.setFromX(-80);
        loginSlide.setToX(0);
        loginSlide.setDelay(Duration.millis(200));
        loginSlide.setInterpolator(Interpolator.SPLINE(0.16, 1.0, 0.3, 1.0));

        ScaleTransition scaleLogin = createScale(loginForm, 0.97, 1.0);
        scaleLogin.setDelay(Duration.millis(200));

        // Botón sincronizado con la aparición del formulario
        FadeTransition fadeOutBtn = new FadeTransition(Duration.millis(150), switchBtn);
        fadeOutBtn.setToValue(0);
        //fadeOutBtn.setDelay(Duration.millis(200));
        fadeOutBtn.setOnFinished(e -> {
            switchBtn.setText("REGISTRARSE");
            FadeTransition fadeInBtn = new FadeTransition(Duration.millis(200), switchBtn);
            fadeInBtn.setFromValue(0);
            fadeInBtn.setToValue(1);
            fadeInBtn.play();
        });

        new ParallelTransition(
                fadeOutRegister,
                movePanel,
                fadeInLogin,
                loginSlide,
                scaleLogin,
                fadeOutBtn
        ).play();
    }

    // LOGIN
    private VBox createLoginForm() {

        VBox form = new VBox(15);
        form.setAlignment(Pos.CENTER);
        form.setPadding(new Insets(40));
        form.setPrefWidth(350);

        Label title = new Label("Iniciar Sesión");
        title.setStyle("""
                -fx-font-size: 32px;
                -fx-font-weight: bold;
                -fx-text-fill: #444;
                """);

        TextField username = createField("👤 Usuario");
        PasswordField password = createPassword("🔒 Contraseña");
        Button btnLogin = createButton("LOGIN");
        username.setOnAction(e -> password.requestFocus());
        password.setOnAction(e -> btnLogin.fire());
        Label lbl = new Label();

        btnLogin.setOnAction(e -> {
            if (username.getText().isEmpty() || password.getText().isEmpty()) {
                lbl.setStyle("-fx-text-fill: red;");
                lbl.setText("Completa todos los campos");
            } else {
                lbl.setStyle("-fx-text-fill: green;");
                lbl.setText("Bienvenido " + username.getText());
            }
        });

        form.getChildren().addAll(title, username, password, btnLogin, lbl);
        return form;
    }

    // REGISTRO
    private VBox createRegisterForm() {

        VBox wrapper = new VBox();
        wrapper.setAlignment(Pos.CENTER);

        ScrollPane scroll = new ScrollPane();
        scroll.setPrefSize(450, 600);
        scroll.setFitToWidth(true);

        scroll.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            scroll.lookupAll(".scroll-bar").forEach(node
                    -> node.setStyle("-fx-background-color: transparent; -fx-padding: 0; -fx-pref-width: 6;"));
            scroll.lookupAll(".track").forEach(node
                    -> node.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;"));
            scroll.lookupAll(".thumb").forEach(node
                    -> node.setStyle("-fx-background-color: rgba(120,120,120,0.45); -fx-background-radius: 8;"));
            scroll.lookupAll(".increment-button").forEach(node
                    -> node.setStyle("-fx-padding: 0; -fx-opacity: 0;"));
            scroll.lookupAll(".decrement-button").forEach(node
                    -> node.setStyle("-fx-padding: 0; -fx-opacity: 0;"));
            scroll.lookupAll(".increment-arrow").forEach(node
                    -> node.setStyle("-fx-shape: ''; -fx-padding: 0;"));
            scroll.lookupAll(".decrement-arrow").forEach(node
                    -> node.setStyle("-fx-shape: ''; -fx-padding: 0;"));
        });

        scroll.setStyle("""
                -fx-background-color: white;
                -fx-background: white;
                -fx-border-color: transparent;
                -fx-padding: 0;
                """);

        VBox form = new VBox(14);
        form.setPadding(new Insets(40, 30, 60, 30));
        form.setAlignment(Pos.TOP_CENTER);
        form.setStyle("-fx-background-color: white;");

        Label title = new Label("Crear Cuenta");
        title.setStyle("""
                -fx-font-size: 30px;
                -fx-font-weight: bold;
                -fx-text-fill: #444;
                """);

        Label subtitle = new Label("Completa la información para registrarte");
        subtitle.setStyle("-fx-text-fill: #888; -fx-font-size: 13px;");

        TextField primerNombre = modernField("Primer Nombre");
        TextField segundoNombre = modernField("Segundo Nombre");
        TextField primerApellido = modernField("Primer Apellido");
        TextField segundoApellido = modernField("Segundo Apellido");
        TextField cedula = modernField("Cédula");
        TextField telefono = modernField("Teléfono");
        TextField email = modernField("Correo Electrónico");
        TextField username = modernField("Username");
        PasswordField password = modernPassword("Contraseña");
        TextField idBarrio = modernField("ID Barrio");
        TextField calle = modernField("Calle");
        TextField carrera = modernField("Carrera");
        TextField etapa = modernField("Etapa");
        TextField manzana = modernField("Manzana");
        TextField casa = modernField("Casa");

        primerNombre.setOnAction(e -> segundoNombre.requestFocus());
        segundoNombre.setOnAction(e -> primerApellido.requestFocus());
        primerApellido.setOnAction(e -> segundoApellido.requestFocus());
        segundoApellido.setOnAction(e -> cedula.requestFocus());
        cedula.setOnAction(e -> telefono.requestFocus());
        telefono.setOnAction(e -> email.requestFocus());
        email.setOnAction(e -> username.requestFocus());
        username.setOnAction(e -> password.requestFocus());
        password.setOnAction(e -> {
            idBarrio.requestFocus();

            // Baja automáticamente hasta la sección Dirección
            Timeline scrollAnim = new Timeline(
                    new KeyFrame(
                            Duration.millis(300),
                            new KeyValue(scroll.vvalueProperty(), 0.90)
                    )
            );
            scrollAnim.play();
        });
        idBarrio.setOnAction(e -> calle.requestFocus());
        calle.setOnAction(e -> carrera.requestFocus());
        carrera.setOnAction(e -> etapa.requestFocus());
        etapa.setOnAction(e -> manzana.requestFocus());
        manzana.setOnAction(e -> casa.requestFocus());

        Label lbl = new Label();
        lbl.setStyle("-fx-font-size: 13px;");

        Button btnRegister = new Button("CREAR CUENTA");

        casa.setOnAction(e -> btnRegister.fire());
        btnRegister.setPrefWidth(250);

        String btnNormal = """
                -fx-background-color: linear-gradient(to right, #16283d, #1f3a56);
                -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;
                -fx-background-radius: 30; -fx-padding: 14 20; -fx-cursor: hand;
                """;
        String btnHover = """
                -fx-background-color: linear-gradient(to right, #0f1c2b, #16283d);
                -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;
                -fx-background-radius: 30; -fx-padding: 14 20; -fx-cursor: hand;
                """;

        btnRegister.setStyle(btnNormal);
        btnRegister.setOnMouseEntered(e -> btnRegister.setStyle(btnHover));
        btnRegister.setOnMouseExited(e -> btnRegister.setStyle(btnNormal));
        btnRegister.setOnAction(e -> {
            if (primerNombre.getText().isEmpty() || primerApellido.getText().isEmpty()
                    || cedula.getText().isEmpty() || username.getText().isEmpty()
                    || password.getText().isEmpty()) {
                lbl.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 13px;");
                lbl.setText("Completa los campos obligatorios");
            } else {
                lbl.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 13px;");
                lbl.setText("Usuario registrado correctamente");
            }
        });

        form.getChildren().addAll(
                title, subtitle, new Separator(),
                sectionTitle("DATOS PERSONALES"),
                modernRow(primerNombre, segundoNombre),
                modernRow(primerApellido, segundoApellido),
                modernRow(cedula, telefono), email,
                sectionTitle("CUENTA"), username, password,
                sectionTitle("DIRECCIÓN"), idBarrio,
                modernRow(calle, carrera), modernRow(etapa, manzana),
                casa, new Separator(), btnRegister, lbl
        );

        scroll.setContent(form);
        wrapper.getChildren().add(scroll);
        return wrapper;
    }

    // HELPERS
    private TextField modernField(String prompt) {
        TextField f = new TextField();
        f.setPromptText(prompt);
        f.setPrefHeight(50);
        f.setStyle("-fx-background-color: #f5f7fb; -fx-background-radius: 25; -fx-border-radius: 25; -fx-border-color: transparent; -fx-padding: 0 20; -fx-font-size: 13px;");
        return f;
    }

    private PasswordField modernPassword(String prompt) {
        PasswordField f = new PasswordField();
        f.setPromptText(prompt);
        f.setPrefHeight(50);
        f.setStyle("-fx-background-color: #f5f7fb; -fx-background-radius: 25; -fx-border-radius: 25; -fx-border-color: transparent; -fx-padding: 0 20; -fx-font-size: 13px;");
        return f;
    }

    private Label sectionTitle(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #888;");
        return l;
    }

    private HBox modernRow(javafx.scene.Node a, javafx.scene.Node b) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER);
        HBox.setHgrow(a, Priority.ALWAYS);
        HBox.setHgrow(b, Priority.ALWAYS);
        row.getChildren().addAll(a, b);
        return row;
    }

    private TextField createField(String prompt) {
        TextField f = new TextField();
        f.setPromptText(prompt);
        f.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 30; -fx-border-radius: 30; -fx-padding: 14 20; -fx-font-size: 14px;");
        f.setMaxWidth(Double.MAX_VALUE);
        return f;
    }

    private PasswordField createPassword(String prompt) {
        PasswordField f = new PasswordField();
        f.setPromptText(prompt);
        f.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 30; -fx-border-radius: 30; -fx-padding: 14 20; -fx-font-size: 14px;");
        f.setMaxWidth(Double.MAX_VALUE);
        return f;
    }

    private Button createButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: #16283d; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 30; -fx-padding: 12 40; -fx-cursor: hand;");
        return btn;
    }

    private ScaleTransition createScale(Node node, double from, double to) {

        ScaleTransition scale = new ScaleTransition(
                Duration.seconds(0.7),
                node
        );

        scale.setFromX(from);
        scale.setFromY(from);

        scale.setToX(to);
        scale.setToY(to);

        scale.setInterpolator(Interpolator.EASE_BOTH);

        return scale;
    }
}
