/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.view;

/**
 *
 * @author Maria Cristina
 */

/**
 * Paleta de colores compartida por todas las vistas.
 *
 * GRASP — Information Expert:
 *   Única clase que conoce los valores hexadecimales de la paleta.
 *   Cualquier vista consulta aquí en lugar de duplicar literales.
 *
 * SOLID — SRP (Single Responsibility Principle):
 *   Su única razón de cambio es una decisión de diseño visual.
 */
public final class AppColors {

    private AppColors() {}

    // ── Generales ─────────────────────────────────────────────────
    public static final String WHITE        = "#ffffff";
    public static final String BG           = "#f8fafc";
    public static final String BG_USER      = "#f4f6fb";
    public static final String BORDER       = "#e5e7eb";
     public static final String AMBER        = "#f9a825";

    // ── Semáforo ──────────────────────────────────────────────────
    public static final String RED          = "#e53935";
    public static final String RED_LIGHT    = "#fff0f0";
    public static final String ORANGE       = "#fb8c00";
    public static final String ORANGE_LIGHT = "#fff8e1";
    public static final String GREEN        = "#43a047";
    public static final String GREEN_LIGHT  = "#e8f5e9";
    public static final String BLUE         = "#1565c0";
    public static final String BLUE_LIGHT   = "#e8f0fe";
    public static final String BLUE_SOFT    = "#eff6ff";

    // ── Texto ─────────────────────────────────────────────────────
    public static final String GRAY_TEXT      = "#6b7280";
    public static final String TEXT_PRIMARY   = "#111827";
    public static final String TEXT_SECONDARY = "#374151";

    // ── Estados de alerta ─────────────────────────────────────────
    public static final String PURPLE                = "#7b1fa2";
    public static final String PURPLE_LIGHT          = "#f3e5f5";
    public static final String COLOR_EN_ATENCION     = PURPLE;
    public static final String COLOR_UNIDAD_ASIGNADA = "#0288d1";
    public static final String CYAN_LIGHT            = "#e1f5fe";

    // ── Tipos de alerta ───────────────────────────────────────────
    public static final String TIPO_ROBO_TEXT      = "#b91c1c";
    public static final String TIPO_ROBO_BG        = "#fff1f2";
    public static final String TIPO_INCENDIO_TEXT  = "#c2410c";
    public static final String TIPO_INCENDIO_BG    = "#fff7ed";
    public static final String TIPO_MEDICO_TEXT    = "#15803d";
    public static final String TIPO_MEDICO_BG      = "#f0fdf4";
    public static final String TIPO_ACCIDENTE_TEXT = "#1d4ed8";
    public static final String TIPO_ACCIDENTE_BG   = "#eff6ff";
    public static final String TIPO_DEFAULT_BG     = "#f3f4f6";

    // ── Sidebar ───────────────────────────────────────────────────
    public static final String SIDEBAR_BG_START  = "#16283d";
    public static final String SIDEBAR_BG_END    = "#1f3a56";
    public static final String SIDEBAR_AVATAR_BG = "#334155";
    public static final String SIDEBAR_NAV_TEXT  = "#8899bb";
    public static final String SIDEBAR_HOVER     = "#ffffff18";
    public static final String SIDEBAR_ACTIVE    = "rgba(255,255,255,0.20)";
    
     public static final String FA_USER        = "\uf007";
    public static final String FA_USERS       = "\uf0c0";
    public static final String FA_USER_CHECK  = "\uf4fc";
    public static final String FA_USER_SLASH  = "\uf506";
    public static final String FA_BELL        = "\uf0f3";
    public static final String FA_CHART       = "\uf080";
    public static final String FA_LIST        = "\uf03a";
    public static final String FA_FILTER      = "\uf0b0";
    public static final String FA_TIMES       = "\uf00d";
    public static final String FA_SEARCH      = "\uf002";
    public static final String FA_DOWNLOAD    = "\uf019";
    public static final String FA_SHIELD      = "\uf505";
    public static final String FA_TOGGLE_ON   = "\uf205";
    public static final String FA_TOGGLE_OFF  = "\uf204";
    public static final String FA_BAN         = "\uf05e";
    public static final String FA_WARNING     = "\uf071";
    public static final String FA_ARROW_LEFT  = "\uf104";
    public static final String FA_ARROW_RIGHT = "\uf105";
}
