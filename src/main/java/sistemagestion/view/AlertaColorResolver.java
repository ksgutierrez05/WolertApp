/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.view;

/**
 *
 * @author Maria Cristina
 */


import sistemagestion.model.Alerta;
import sistemagestion.model.EstadoAlerta;


/**
 * Resuelve el color y el ícono visual de una alerta según su estado y tipo.
 *
 * Patrón GRASP : Information Expert — centraliza el conocimiento de
 *                representación visual de EstadoAlerta.
 * Principio SOLID: SRP — solo resuelve representación visual de alertas.
 *                  OCP — si EstadoAlerta crece, solo se toca este resolver.
 *
 * Nota de mejora futura: si EstadoAlerta es un enum propio del proyecto,
 * lo ideal es mover getColor() y getIcon() como métodos del enum directamente.
 */
public final class AlertaColorResolver {

    private AlertaColorResolver() {
    }

    /**
     * Devuelve el color hex correspondiente al estado dado.
     */
    public static String getColor(EstadoAlerta estado) {
        if (estado == null) {
            return AppColors.GRAY_TEXT;
        }
        return switch (estado) {
            case PENDIENTE ->
                AppColors.ORANGE;
            case RECIBIDA ->
                AppColors.BLUE;
            case EN_ATENCION ->
                AppColors.COLOR_EN_ATENCION;
            case UNIDAD_ASIGNADA ->
                AppColors.COLOR_UNIDAD_ASIGNADA;
            case RESUELTA ->
                AppColors.GREEN;
            case CANCELADA ->
                AppColors.GRAY_TEXT;
        };
    }

    /**
     * Devuelve el emoji/ícono que representa el tipo de alerta.
     */
    public static String getIcon(Alerta alerta) {
        if (alerta.getTipoalerta() == null) {
            return "🚨";
        }
        String tipo = alerta.getTipoalerta().getNombre().toUpperCase();
        if (tipo.contains("ROB")) {
            return "🦹";
        }
        if (tipo.contains("PELEA") || tipo.contains("VIOLENCIA")) {
            return "⚔";
        }
        if (tipo.contains("ANIMAL")) {
            return "🐕";
        }
        if (tipo.contains("LUZ") || tipo.contains("INFRA")) {
            return "💡";
        }
        return "🚨";
    }

    /**
     * Devuelve el ícono Font Awesome para usar en labels según el tipo.
     */
    public static String getFaIcon(String tipoUpper) {
        return switch (tipoUpper) {
            case "ROBO", "HURTO" ->
                "\uf505";
            case "ACCIDENTE" ->
                "\uf071";
            case "INCENDIO" ->
                "\uf46a";
            case "ANIMAL" ->
                "\uf6d3";
            case "VANDALISMO" ->
                "\uf6e3";
            case "SOSPECHOSO", "PERSONA" ->
                "\uf007";
            default ->
                "\uf0f3";
        };
    }

    /**
     * Color del fondo del círculo-ícono según tipo.
     */
    public static String getCircleBg(String tipoUpper) {
        return switch (tipoUpper) {
            case "ROBO", "HURTO", "SOSPECHOSO", "PERSONA" ->
                "#fce4ec";
            case "ANIMAL" ->
                "#fff8e1";
            case "ACCIDENTE" ->
                "#fff3e0";
            case "VANDALISMO" ->
                "#e8eaf6";
            default ->
                "#e3f2fd";
        };
    }
}
