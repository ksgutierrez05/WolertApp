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
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import sistemagestion.model.Barrio;
import sistemagestion.model.Direccion;
import sistemagestion.model.Usuario;
import sistemagestion.service.BarrioService;
import sistemagestion.service.UsuarioService;

/**
 * Diálogo para editar el perfil del usuario.
 *
 * Principio GRASP aplicado — Alta Cohesión: Solo conoce y gestiona los campos
 * de perfil personal. No sabe nada de suscripciones ni de la vista principal.
 *
 * Principio GRASP aplicado — Information Expert: La validación de cada campo
 * está aquí porque es quien conoce qué campos son obligatorios y cómo
 * actualizarlos.
 */
public class EditarPerfilDialog extends BaseDialog {

    private final Usuario usuarioActual;
    private final UsuarioService usuarioService;

    // Campos de texto
    private TextField fNombre, fApellido, fTelefono, fCorreo;
    private TextField fCalle, fCarrera, fEtapa, fManzana, fCasa;

    // Componente buscador de barrio (Pure Fabrication reutilizable)
    private BarrioBuscadorComponent buscadorBarrio;

    public EditarPerfilDialog(
            Usuario usuarioActual,
            BarrioService barrioService,
            UsuarioService usuarioService,
            Runnable onSuccess) {

        super(
                "Editar perfil",
                "Actualiza tu información personal",
                "\uf304",
                onSuccess
        );

        this.usuarioActual = usuarioActual;
        this.usuarioService = usuarioService;

        List<Barrio> barrios = cargarBarrios(barrioService);

        Barrio barrioActual
                = usuarioActual != null
                && usuarioActual.getDireccion() != null
                ? usuarioActual.getDireccion().getBarrio()
                : null;

        this.buscadorBarrio
                = new BarrioBuscadorComponent(
                        barrios,
                        barrioActual
                );

        inicializarDialogo(
                "Editar perfil",
                "Actualiza tu información personal",
                "\uf304"
        );
    }

    @Override
    protected void buildContenido(VBox contenido) {
        if (usuarioActual == null) {
            mostrarError("Usuario no disponible");
            return;
        }
        Direccion dir = usuarioActual != null ? usuarioActual.getDireccion() : null;

        fNombre = modernField("Primer nombre", val(usuarioActual.getPrimer_nombre()));
        fApellido = modernField("Primer apellido", val(usuarioActual.getPrimer_apellido()));
        fTelefono = modernField("Teléfono", val(usuarioActual.getTelefono()));
        fCorreo = modernField("Correo", val(usuarioActual.getCorreo()));

        fCalle = modernField("Calle", dir(dir, dir != null ? dir.getCalle() : null));
        fCarrera = modernField("Carrera", dir(dir, dir != null ? dir.getCarrera() : null));
        fEtapa = modernField("Etapa", dir(dir, dir != null ? dir.getEtapa() : null));
        fManzana = modernField("Manzana", dir(dir, dir != null ? dir.getManzana() : null));
        fCasa = modernField("Casa", dir(dir, dir != null ? dir.getCasa() : null));

        contenido.getChildren().addAll(
                MiCuentaUIFactory.groupLabel("NOMBRE"),
                fNombre, fApellido,
                MiCuentaUIFactory.groupLabel("CONTACTO"),
                fTelefono, fCorreo,
                MiCuentaUIFactory.groupLabel("DIRECCIÓN"),
                fCalle, fCarrera, fEtapa, fManzana, fCasa,
                MiCuentaUIFactory.groupLabel("BARRIO"),
                buscadorBarrio.getSearchBox()
        );
    }

    @Override
    protected void onGuardar() {
        mostrarAviso("");
        try {
            // Actualizar campos personales si no están vacíos
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

            // Validar barrio seleccionado
            Barrio barrioSel = buscadorBarrio.getBarrioSeleccionado();
            if (barrioSel == null) {
                mostrarAviso("⚠️ Selecciona un barrio de la lista.");
                return;
            }

            // Actualizar dirección
            Direccion dir = usuarioActual.getDireccion() != null
                    ? usuarioActual.getDireccion() : new Direccion();
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
            dir.setBarrio(barrioSel);
            usuarioActual.setDireccion(dir);

            if (usuarioService != null) {
                usuarioService.actualizar(usuarioActual);
                cerrarConExito();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            mostrarError("Error: " + ex.getMessage());
        }
    }

    @Override
    protected String getBotonGuardarTexto() {
        return "Guardar cambios";
    }

    @Override
    protected int getScrollHeight() {
        return 370;
    }

    // ── Helpers privados ──────────────────────────────────────────────────────
    private List<Barrio> cargarBarrios(BarrioService barrioService) {
        try {
            return barrioService != null ? barrioService.listar() : List.of();
        } catch (Exception e) {
            return List.of();
        }
    }

    private TextField modernField(String prompt, String value) {
        TextField f = new TextField(value != null ? value : "");
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

    private String val(String v) {
        return v != null && !v.isBlank() ? v : "";
    }

    private String dir(Direccion d, String v) {
        return v != null ? v : "";
    }
}
