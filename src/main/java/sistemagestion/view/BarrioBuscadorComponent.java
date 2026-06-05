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
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import sistemagestion.model.Barrio;

/**
 * Componente visual reutilizable: buscador de barrio con popup de resultados.
 *
 * Principio GRASP aplicado — Pure Fabrication:
 *   No representa ningún concepto del dominio; es un widget de UI
 *   que encapsula la lógica de búsqueda + popup para ser reutilizado
 *   tanto en EditarPerfilDialog como en GestionarSuscripcionDialog
 *   sin duplicar ~60 líneas de código.
 *
 * Principio GRASP aplicado — Alta Cohesión:
 *   Solo sabe buscar barrios y mantener cuál fue seleccionado.
 *   No sabe nada de usuarios, suscripciones ni de persistencia.
 */
public class BarrioBuscadorComponent {

    private final List<Barrio>       todosBarrios;
    private final HBox               searchBox;
    private final TextField          editorBarrio;
    private final ListView<Barrio>   listaBarrios;
    private final javafx.stage.Popup popup;
    private final VBox               popupContent;

    private Barrio barrioSeleccionado;

    public BarrioBuscadorComponent(List<Barrio> todosBarrios, Barrio barrioInicial) {
        this.todosBarrios = todosBarrios;

        // ── SearchBox ─────────────────────────────────────────────────────────
        searchBox = new HBox(8);
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

        editorBarrio = new TextField();
        editorBarrio.setPromptText("Buscar tu barrio...");
        editorBarrio.setStyle(
                "-fx-background-color:transparent;"
                + "-fx-border-color:transparent;"
                + "-fx-font-size:13px;"
                + "-fx-text-fill:#111827;");
        editorBarrio.setPrefHeight(42);
        HBox.setHgrow(editorBarrio, Priority.ALWAYS);
        searchBox.getChildren().addAll(searchIcon, editorBarrio);

        // ── ListView del popup ────────────────────────────────────────────────
        listaBarrios = new ListView<>();
        listaBarrios.setPrefHeight(160);
        listaBarrios.setStyle(
                "-fx-background-color:white;-fx-border-color:#e2e8f0;"
                + "-fx-border-radius:12;-fx-background-radius:12;-fx-font-size:13px;");
        listaBarrios.setCellFactory(lv -> new ListCell<Barrio>() {
            @Override protected void updateItem(Barrio item, boolean empty) {
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

        // ── Popup ─────────────────────────────────────────────────────────────
        popup = new javafx.stage.Popup();
        popup.setAutoHide(true);
        popup.setHideOnEscape(true);
        popupContent = new VBox(listaBarrios);
        popupContent.setStyle(
                "-fx-background-color:white;-fx-background-radius:12;"
                + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.15),12,0,0,4);");
        popup.getContent().add(popupContent);

        // ── Precargar barrio inicial ──────────────────────────────────────────
        if (barrioInicial != null) {
            String nombre = barrioInicial.getNombre();
            todosBarrios.stream()
                    .filter(b -> b.getNombre().equalsIgnoreCase(nombre))
                    .findFirst()
                    .ifPresent(b -> {
                        barrioSeleccionado = b;
                        editorBarrio.setText(b.getNombre());
                    });
        }

        // ── Listeners ─────────────────────────────────────────────────────────
        editorBarrio.textProperty().addListener((obs, oldVal, newVal) -> onTextoCambiado(newVal));
        listaBarrios.setOnMouseClicked(e -> seleccionarItem());
        editorBarrio.setOnKeyReleased(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE)       popup.hide();
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER && !listaBarrios.getItems().isEmpty())
                seleccionarPrimero();
        });
    }

    // ── API pública ───────────────────────────────────────────────────────────

    /** Devuelve el HBox del buscador para incluirlo en un layout. */
    public HBox getSearchBox() { return searchBox; }

    /** Devuelve el barrio que el usuario seleccionó, o null si no hay ninguno. */
    public Barrio getBarrioSeleccionado() { return barrioSeleccionado; }

    /** Oculta el popup (llamar antes de cerrar el diálogo). */
    public void ocultarPopup() { popup.hide(); }

    // ── Lógica interna ────────────────────────────────────────────────────────

    private void onTextoCambiado(String newVal) {
        // Si el texto coincide con el barrio ya seleccionado, no buscar de nuevo
        if (barrioSeleccionado != null && barrioSeleccionado.getNombre().equals(newVal)) return;
        barrioSeleccionado = null;

        String busq = newVal == null ? "" : newVal.toLowerCase();
        List<Barrio> filtrados = todosBarrios.stream()
                .filter(b -> busq.isBlank() || b.getNombre().toLowerCase().contains(busq))
                .toList();
        listaBarrios.getItems().setAll(filtrados);

        if (!filtrados.isEmpty() && !busq.isBlank() && searchBox.getScene() != null) {
            javafx.geometry.Bounds bounds = searchBox.localToScreen(searchBox.getBoundsInLocal());
            if (bounds != null) {
                popup.show(searchBox, bounds.getMinX(), bounds.getMaxY() + 2);
                popupContent.setPrefWidth(bounds.getWidth());
                listaBarrios.setPrefWidth(bounds.getWidth());
            }
        } else {
            popup.hide();
        }
    }

    private void seleccionarItem() {
        Barrio sel = listaBarrios.getSelectionModel().getSelectedItem();
        if (sel != null) confirmarSeleccion(sel);
    }

    private void seleccionarPrimero() {
        if (!listaBarrios.getItems().isEmpty())
            confirmarSeleccion(listaBarrios.getItems().get(0));
    }

    private void confirmarSeleccion(Barrio barrio) {
        barrioSeleccionado = barrio;
        editorBarrio.setText(barrio.getNombre());
        popup.hide();
    }
}
