/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.view;

/**
 *
 * @author Maria Cristina
 */


import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Fábrica de componentes UI reutilizables.
 *
 * Patrón GRASP : Pure Fabrication — no representa un concepto del dominio,
 *                existe solo para mejorar cohesión y eliminar duplicación.
 * Principio SOLID: SRP — única responsabilidad: crear widgets básicos.
 *                  OCP — nuevos helpers se agregan sin modificar los existentes.
 */
public final class UIFactory {

    private UIFactory() {}

    /** Crea un Label con fuente, color y peso configurables. */
    public static Label label(String text, double size, String color, boolean bold) {
        Label lbl = new Label(text);
        lbl.setFont(bold
                ? Font.font("System", FontWeight.BOLD, size)
                : Font.font("System", size));
        lbl.setTextFill(Color.web(color));
        return lbl;
    }

    /** Línea separadora horizontal de 1 px. */
    public static Region separator() {
        Region sep = new Region();
        sep.setPrefHeight(AppDimensions.SEPARATOR_HEIGHT);
        sep.setStyle("-fx-background-color: " + AppColors.BORDER + ";");
        return sep;
    }

    /** Aplica sombra suave a cualquier Region. */
    public static void shadow(Region node) {
        DropShadow shadow = new DropShadow();
        shadow.setRadius(15);
        shadow.setOffsetY(3);
        shadow.setColor(Color.rgb(15, 23, 42, 0.08));
        node.setEffect(shadow);
    }

    /** Sombra ligeramente más pronunciada para uso en UsuarioApp. */
    public static void shadowMd(Region node) {
        DropShadow shadow = new DropShadow();
        shadow.setRadius(18);
        shadow.setOffsetY(4);
        shadow.setColor(Color.rgb(15, 23, 42, 0.10));
        node.setEffect(shadow);
    }
}