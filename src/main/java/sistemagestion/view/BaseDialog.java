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
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Modality;

/**
 * Clase base para los diálogos modales de MiCuenta.
 *
 * Principio GRASP aplicado — Polimorfismo + Alta Cohesión: Centraliza la
 * estructura visual común (header con icono, scroll, footer con botones) para
 * que cada subclase solo implemente buildContenido() con su lógica particular.
 *
 * Evita duplicar ~80 líneas de código de estructura entre EditarPerfilDialog y
 * GestionarSuscripcionDialog.
 */
public abstract class BaseDialog {

    protected final Stage stage;
    protected final Runnable onSuccess;

    // Componentes de estructura compartida
    protected ScrollPane scrollContenido;
    protected Label lblResultado;

    protected BaseDialog(String titulo, String subtitulo, String iconFA, Runnable onSuccess) {
        this.onSuccess = onSuccess;
        this.stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UNDECORATED);

    }

    protected void inicializarDialogo(
            String titulo,
            String subtitulo,
            String iconFA) {

        stage.setScene(
                buildScene(titulo, subtitulo, iconFA)
        );
    }

    // ── Template Method: estructura fija, contenido variable ─────────────────
    private Scene buildScene(String titulo, String subtitulo, String iconFA) {
        VBox root = new VBox(buildHeader(titulo, subtitulo, iconFA), buildScroll(), buildFooter());
        root.setStyle("-fx-background-color:white;");
        StackPane wrapper = new StackPane(root);
        wrapper.setStyle(
                "-fx-background-color:white;"
                + "-fx-background-radius:16;"
                + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.20),30,0,0,8);");
        Scene scene = new Scene(wrapper, getWidth(), getHeight());
        scene.setFill(Color.TRANSPARENT);
        return scene;
    }

    // ── Header común ─────────────────────────────────────────────────────────
    private HBox buildHeader(String titulo, String subtitulo, String iconFA) {
        HBox header = new HBox(12);
        header.setPadding(new Insets(20, 24, 16, 24));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color:white;-fx-border-color:#f1f5f9;-fx-border-width:0 0 1 0;");

        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(38, 38);
        iconBox.setStyle("-fx-background-color:linear-gradient(to bottom right,#16283d,#1f3a56);-fx-background-radius:10;");
        Label iconLbl = new Label(iconFA);
        iconLbl.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';-fx-font-size:16px;-fx-text-fill:white;");
        iconBox.getChildren().add(iconLbl);

        VBox titleBox = new VBox(2);
        Label titleLbl = new Label(titulo);
        titleLbl.setFont(Font.font("System", FontWeight.BOLD, 16));
        titleLbl.setTextFill(Color.web("#0f172a"));
        Label subLbl = new Label(subtitulo);
        subLbl.setFont(Font.font("System", 11));
        subLbl.setTextFill(Color.web("#94a3b8"));
        titleBox.getChildren().addAll(titleLbl, subLbl);

        Button btnX = new Button("✕");
        btnX.setStyle("-fx-background-color:transparent;-fx-text-fill:#94a3b8;-fx-font-size:16px;-fx-cursor:hand;-fx-padding:0;");
        btnX.setOnMouseEntered(e -> btnX.setStyle("-fx-background-color:transparent;-fx-text-fill:#e53935;-fx-font-size:16px;-fx-cursor:hand;-fx-padding:0;"));
        btnX.setOnMouseExited(e -> btnX.setStyle("-fx-background-color:transparent;-fx-text-fill:#94a3b8;-fx-font-size:16px;-fx-cursor:hand;-fx-padding:0;"));
        btnX.setOnAction(e -> onCerrar());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(iconBox, titleBox, spacer, btnX);
        return header;
    }

    // ── Scroll con contenido de la subclase ───────────────────────────────────
    private ScrollPane buildScroll() {
        lblResultado = new Label("");
        lblResultado.setFont(Font.font("System", FontWeight.BOLD, 12));
        lblResultado.setWrapText(true);

        VBox contenido = new VBox(6);
        contenido.setPadding(new Insets(16, 24, 20, 24));
        contenido.setStyle("-fx-background-color:white;");
        buildContenido(contenido);           // cada subclase llena esto
        contenido.getChildren().add(lblResultado);

        scrollContenido = new ScrollPane(contenido);
        scrollContenido.setFitToWidth(true);
        scrollContenido.setPrefHeight(getScrollHeight());
        scrollContenido.setStyle("-fx-background:white;-fx-background-color:white;-fx-border-color:transparent;");
        scrollContenido.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        return scrollContenido;
    }

    // ── Footer común ─────────────────────────────────────────────────────────
    private HBox buildFooter() {
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
        btnCancelar.setOnAction(e -> onCerrar());

        Button btnGuardar = new Button(getBotonGuardarTexto());
        btnGuardar.setPrefHeight(40);
        btnGuardar.setPrefWidth(180);
        btnGuardar.setStyle("-fx-background-color:linear-gradient(to bottom right,#16283d,#1f3a56);-fx-text-fill:white;-fx-font-size:13px;-fx-font-weight:bold;-fx-background-radius:10;-fx-cursor:hand;-fx-effect:dropshadow(gaussian,rgba(22,40,61,0.35),10,0,0,3);");
        btnGuardar.setOnMouseEntered(e -> btnGuardar.setStyle("-fx-background-color:linear-gradient(to bottom right,#1f3a56,#2a4f72);-fx-text-fill:white;-fx-font-size:13px;-fx-font-weight:bold;-fx-background-radius:10;-fx-cursor:hand;-fx-effect:dropshadow(gaussian,rgba(22,40,61,0.5),14,0,0,5);"));
        btnGuardar.setOnMouseExited(e -> btnGuardar.setStyle("-fx-background-color:linear-gradient(to bottom right,#16283d,#1f3a56);-fx-text-fill:white;-fx-font-size:13px;-fx-font-weight:bold;-fx-background-radius:10;-fx-cursor:hand;-fx-effect:dropshadow(gaussian,rgba(22,40,61,0.35),10,0,0,3);"));
        btnGuardar.setOnAction(e -> onGuardar());

        footer.getChildren().addAll(btnCancelar, btnGuardar);
        return footer;
    }

    // ── Métodos de ciclo de vida ──────────────────────────────────────────────
    public void show() {
        stage.show();
    }

    protected void onCerrar() {
        stage.close();
    }

    protected void cerrarConExito() {
        stage.close();
        if (onSuccess != null) {
            onSuccess.run();
        }
    }

    protected void mostrarError(String msg) {
        lblResultado.setText(msg);
        lblResultado.setTextFill(Color.web("#e53935"));
    }

    protected void mostrarAviso(String msg) {
        lblResultado.setText(msg);
        lblResultado.setTextFill(Color.web("#fb8c00"));
    }

    // ── Métodos abstractos que cada diálogo implementa ───────────────────────
    /**
     * Agrega los campos/controles específicos al VBox de contenido.
     */
    protected abstract void buildContenido(VBox contenido);

    /**
     * Lógica de guardado específica de cada diálogo.
     */
    protected abstract void onGuardar();

    /**
     * Texto del botón principal (ej. "Guardar cambios", "Guardar suscripción").
     */
    protected abstract String getBotonGuardarTexto();

    /**
     * Ancho de la ventana.
     */
    protected int getWidth() {
        return 480;
    }

    /**
     * Alto total de la ventana.
     */
    protected int getHeight() {
        return 520;
    }

    /**
     * Alto del área de scroll.
     */
    protected int getScrollHeight() {
        return 370;
    }
}
