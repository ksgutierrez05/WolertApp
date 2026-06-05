/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.view;

/**
 *
 * @author Maria Cristina
 */


import javafx.scene.Node;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Registro de rutas de navegación: texto → constructor de vista.
 *
 * Patrón GRASP: Controller — desacopla la lógica de navegación del sidebar.
 *               Pure Fabrication — no es dominio, mejora la estructura.
 * Principio SOLID: OCP — agregar una nueva ruta NO modifica esta clase
 *                  ni AdministradorApp/UsuarioApp; solo se registra un entry.
 *                  SRP — única responsabilidad: mapear texto a vistas.
 */
public class NavigationRegistry {

    /** Map preserva el orden de inserción para construir el sidebar en orden. */
    private final Map<String, Supplier<Node>> routes = new LinkedHashMap<>();

    /**
     * Registra una entrada de navegación.
     *
     * @param label   Texto visible en el sidebar.
     * @param builder Supplier que construye la vista al navegar.
     */
    public NavigationRegistry register(String label, Supplier<Node> builder) {
        routes.put(label, builder);
        return this;
    }

    /**
     * Retorna la vista para el label dado, o null si no existe.
     */
    public Node navigate(String label) {
        Supplier<Node> builder = routes.get(label);
        return builder != null ? builder.get() : null;
    }

    /**
     * Devuelve todos los labels registrados (para construir el nav).
     */
    public java.util.Set<String> getLabels() {
        return routes.keySet();
    }
}
