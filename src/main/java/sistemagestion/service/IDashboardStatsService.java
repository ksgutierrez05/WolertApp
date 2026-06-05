/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.service;

/**
 *
 * @author Maria Cristina
 */

/**
 * Contrato para los servicios que calculan estadísticas del dashboard.
 *
 * Principio SOLID: DIP — las vistas dependen de esta abstracción,
 *                  no de implementaciones concretas.
 *                  ISP — interfaz específica por rol (admin / usuario).
 */
public interface IDashboardStatsService {

    /** Total de alertas activas (PENDIENTE, RECIBIDA, EN_ATENCION, UNIDAD_ASIGNADA). */
    long getAlertasActivas();

    /** Alertas en atención activa (EN_ATENCION, UNIDAD_ASIGNADA). */
    long getIncidentes();

    /** Total histórico de alertas resueltas. */
    long getAlertasResueltas();
}