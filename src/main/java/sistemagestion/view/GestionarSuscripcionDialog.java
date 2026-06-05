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
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import sistemagestion.model.*;
import sistemagestion.service.BarrioService;
import sistemagestion.service.SuscripcionService;

/**
 * Diálogo para gestionar (crear/actualizar) suscripciones.
 *
 * Principio GRASP aplicado — Alta Cohesión: Solo gestiona la lógica de
 * suscripción: zona, barrio, comuna. No sabe nada de edición de perfil ni de la
 * vista principal.
 *
 * Principio GRASP aplicado — Information Expert: Quien conoce las suscripciones
 * existentes decide si insertar o actualizar.
 */
public class GestionarSuscripcionDialog extends BaseDialog {

    private final Usuario usuarioActual;
    private final List<Suscripcion> actuales;
    private final SuscripcionService suscripcionService;
    private final BarrioService barrioService;

    // Controles de zona
    private RadioButton rbBarrio, rbComuna, rbGeneral;
    private ToggleGroup tgZona;

    // Buscador de barrio (Pure Fabrication reutilizable)
    private BarrioBuscadorComponent buscadorBarrio;

    // Combo de comuna
    private ComboBox<String> cmbComuna;

    // Todos los barrios cargados
    private List<Barrio> todosBarrios;

    public GestionarSuscripcionDialog(Usuario usuarioActual,
            List<Suscripcion> actuales,
            SuscripcionService suscripcionService,
            BarrioService barrioService,
            Runnable onSuccess) {

        super(
                "Gestionar suscripción",
                "Recibirás alertas de la zona elegida",
                "\uf0f3",
                onSuccess
        );

        this.usuarioActual = usuarioActual;
        this.actuales = actuales;
        this.suscripcionService = suscripcionService;
        this.barrioService = barrioService;

        inicializarDialogo(
                "Gestionar suscripción",
                "Recibirás alertas de la zona elegida",
                "\uf0f3"
        );
    }

    @Override
    protected void buildContenido(VBox contenido) {
        if (usuarioActual == null) {
            mostrarError("Usuario no disponible");
            return;
        }
        todosBarrios = cargarBarrios();

        // RadioButtons de zona
        tgZona = new ToggleGroup();
        rbBarrio = styledRadio("Por barrio", tgZona, true);
        rbComuna = styledRadio("Por comuna", tgZona, false);
        rbGeneral = styledRadio("General (toda la ciudad)", tgZona, false);
        VBox zonaBox = new VBox(6, rbBarrio, rbComuna, rbGeneral);

        // Buscador de barrio — precargar con el barrio del usuario
        Barrio barrioActual = usuarioActual.getDireccion() != null
                ? usuarioActual.getDireccion().getBarrio() : null;
        buscadorBarrio = new BarrioBuscadorComponent(todosBarrios, barrioActual);

        // ComboBox de comunas
        cmbComuna = buildCmbComuna();

        // Listener que muestra/oculta controles según la zona elegida
        tgZona.selectedToggleProperty().addListener((obs, o, n) -> {
            boolean esBarrio = n == rbBarrio;
            boolean esComuna = n == rbComuna;
            buscadorBarrio.getSearchBox().setVisible(esBarrio);
            buscadorBarrio.getSearchBox().setManaged(esBarrio);
            cmbComuna.setVisible(esComuna);
            cmbComuna.setManaged(esComuna);
            if (!esBarrio) {
                buscadorBarrio.ocultarPopup();
            }
        });

        contenido.getChildren().addAll(
                MiCuentaUIFactory.groupLabel("ZONA DE SUSCRIPCIÓN"),
                zonaBox,
                MiCuentaUIFactory.groupLabel("BARRIO"),
                buscadorBarrio.getSearchBox(),
                cmbComuna
        );
    }

    @Override
    protected void onGuardar() {
        try {
            Barrio barrioSel = null;
            String comunaNom = null;

            if (rbBarrio.isSelected()) {
                barrioSel = buscadorBarrio.getBarrioSeleccionado();
                if (barrioSel == null) {
                    mostrarAviso("⚠️ Selecciona un barrio de la lista.");
                    return;
                }
            } else if (rbComuna.isSelected()) {
                comunaNom = cmbComuna.getValue();
                if (comunaNom == null) {
                    mostrarAviso("⚠️ Selecciona una comuna.");
                    return;
                }
            }

            Suscripcion suscripcion = buildSuscripcion(barrioSel, comunaNom);
            boolean ok = persistirSuscripcion(suscripcion);

            if (ok) {
                buscadorBarrio.ocultarPopup();
                cerrarConExito();
            } else {
                mostrarError("✘ No se pudo guardar.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            mostrarError("✘ Error: " + ex.getMessage());
        }
    }

    /**
     * Information Expert: quien conoce las suscripciones existentes decide si
     * hacer insert o update.
     */
    private boolean persistirSuscripcion(Suscripcion suscripcion) {
        java.util.Optional<Suscripcion> existente = actuales.stream().findFirst();
        if (existente.isPresent()) {
            suscripcion.setId_suscripcion(existente.get().getId_suscripcion());
            return suscripcionService.actualizar(suscripcion);
        }
        return suscripcionService.insertar(suscripcion);
    }

    private Suscripcion buildSuscripcion(Barrio barrioSel, String comunaNom) {
        Suscripcion s = new Suscripcion();
        s.setUsuario(usuarioActual);
        TipoAlerta ta = new TipoAlerta();
        ta.setNombre("GENERAL");
        s.setTipoalerta(ta);
        s.setEstado(EstadoSuscripcion.ACTIVA);
        if (barrioSel != null) {
            s.setBarrio(barrioSel);
        }
        if (comunaNom != null) {
            resolverComuna(s, comunaNom);
        }
        return s;
    }

    private void resolverComuna(Suscripcion s, String comunaNom) {
        try {
            todosBarrios.stream()
                    .filter(b -> b.getComuna() != null && comunaNom.equals(b.getComuna().getNombre()))
                    .map(Barrio::getComuna)
                    .findFirst()
                    .ifPresent(s::setComuna);
        } catch (Exception ignored) {
        }
    }

    // ── Helpers de construcción UI ────────────────────────────────────────────
    private ComboBox<String> buildCmbComuna() {
        ComboBox<String> cmb = new ComboBox<>();
        cmb.setPromptText("Selecciona la comuna");
        cmb.setMaxWidth(Double.MAX_VALUE);
        cmb.setPrefHeight(50);
        cmb.setStyle("-fx-background-color:#f5f7fb;-fx-border-color:transparent;"
                + "-fx-border-radius:25;-fx-background-radius:25;-fx-font-size:13px;");
        cmb.setVisible(false);
        cmb.setManaged(false);

        cmb.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(item);
                boolean sel = item.equals(getListView().getSelectionModel().getSelectedItem());
                setStyle(sel
                        ? "-fx-background-color:linear-gradient(to right,#16283d,#1f3a56);"
                        + "-fx-text-fill:white;-fx-font-size:13px;-fx-padding:10 20;"
                        : "-fx-background-color:transparent;-fx-text-fill:#374151;"
                        + "-fx-font-size:13px;-fx-padding:10 20;");
                setOnMouseEntered(e -> {
                    if (!item.equals(getListView().getSelectionModel().getSelectedItem())) {
                        setStyle("-fx-background-color:#f0f4ff;-fx-text-fill:#16283d;-fx-font-size:13px;-fx-padding:10 20;");
                    }
                });
                setOnMouseExited(e -> setStyle(item.equals(getListView().getSelectionModel().getSelectedItem())
                        ? "-fx-background-color:linear-gradient(to right,#16283d,#1f3a56);-fx-text-fill:white;-fx-font-size:13px;-fx-padding:10 20;"
                        : "-fx-background-color:transparent;-fx-text-fill:#374151;-fx-font-size:13px;-fx-padding:10 20;"));
            }
        });
        cmb.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Selecciona la comuna" : item);
                setStyle("-fx-background-color:transparent;-fx-text-fill:"
                        + (item != null ? "#374151" : "#9ca3af") + ";-fx-font-size:13px;-fx-padding:0 6;");
            }
        });

        todosBarrios.stream()
                .filter(b -> b.getComuna() != null && b.getComuna().getNombre() != null)
                .map(b -> b.getComuna().getNombre()).distinct().sorted()
                .forEach(cmb.getItems()::add);
        return cmb;
    }

    private RadioButton styledRadio(String texto, ToggleGroup tg, boolean selected) {
        RadioButton rb = new RadioButton(texto);
        rb.setToggleGroup(tg);
        rb.setSelected(selected);
        rb.setFont(javafx.scene.text.Font.font("System", javafx.scene.text.FontWeight.BOLD, 13));
        rb.setTextFill(javafx.scene.paint.Color.web(selected ? "#1f3a56" : "#6b7280"));
        rb.setPadding(new javafx.geometry.Insets(10, 20, 10, 20));
        rb.setMaxWidth(Double.MAX_VALUE);
        rb.setPrefHeight(46);
        rb.setGraphic(null);

        String base = "-fx-background-color:#f5f7fb;-fx-background-radius:25;"
                + "-fx-border-color:transparent;-fx-border-radius:25;";
        String activo = "-fx-background-color:#eef2ff;-fx-background-radius:25;"
                + "-fx-border-color:#1f3a56;-fx-border-radius:25;-fx-border-width:1.5;";

        rb.setStyle(selected ? activo : base);

        rb.skinProperty().addListener((obs, o, n) -> {
            if (n == null) {
                return;
            }
            rb.lookupAll(".radio").forEach(node -> node.setStyle("-fx-padding:0;-fx-background-color:transparent;-fx-border-color:transparent;"));
            rb.lookupAll(".dot").forEach(node -> node.setStyle("-fx-background-color:transparent;"));
        });
        rb.selectedProperty().addListener((obs, o, n) -> {
            rb.setStyle(n ? activo : base);
            rb.setTextFill(javafx.scene.paint.Color.web(n ? "#1f3a56" : "#6b7280"));
            rb.lookupAll(".radio").forEach(node -> node.setStyle("-fx-padding:0;-fx-background-color:transparent;-fx-border-color:transparent;"));
            rb.lookupAll(".dot").forEach(node -> node.setStyle("-fx-background-color:transparent;"));
        });
        rb.setOnMouseEntered(e -> {
            if (!rb.isSelected()) {
                rb.setStyle("-fx-background-color:#eef2ff;-fx-background-radius:25;-fx-border-color:transparent;-fx-border-radius:25;");
            }
        });
        rb.setOnMouseExited(e -> {
            if (!rb.isSelected()) {
                rb.setStyle(base);
            }
        });
        return rb;
    }

    private List<Barrio> cargarBarrios() {
        try {
            return barrioService != null ? barrioService.listar() : List.of();
        } catch (Exception e) {
            return List.of();
        }
    }

    @Override
    protected String getBotonGuardarTexto() {
        return "Guardar suscripción";
    }

    @Override
    protected int getScrollHeight() {
        return 340;
    }

    @Override
    protected int getHeight() {
        return 520;
    }
}
