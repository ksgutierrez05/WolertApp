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

/**
 * Configuración inmutable del sidebar para una vista concreta.
 *
 * Patrón GRASP: Information Expert — agrupa la información que necesita
 *               SidebarBuilder para construir el sidebar de cualquier rol.
 * Principio SOLID: SRP — solo transporta configuración, sin lógica UI.
 */
public record SidebarConfig(
        String appSubtitle,
        String userName,
        String userSubInfo,
        List<NavEntry> entries,
        Runnable onLogout
) {
    /**
     * Entrada de navegación: ícono FA + texto + acción al hacer clic.
     */
    public record NavEntry(String icon, String text, Runnable action) {}
}