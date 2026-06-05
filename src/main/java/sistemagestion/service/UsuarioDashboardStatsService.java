/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.service;

/**
 *
 * @author Maria Cristina
 */
import sistemagestion.model.EstadoAlerta;
import sistemagestion.model.Usuario;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Calcula las estadísticas del dashboard del usuario normal.
 *
 * Patrón GRASP: Information Expert — extrae la lógica de cálculo que antes
 * vivía dentro de buildStatCards() en UsuarioApp. Principio SOLID: SRP — única
 * responsabilidad: estadísticas del usuario. DIP — recibe AlertaService por
 * constructor.
 */
public class UsuarioDashboardStatsService implements IDashboardStatsService {

    private final AlertaService alertaService;
    private final Usuario usuario;

    public UsuarioDashboardStatsService(AlertaService alertaService, Usuario usuario) {
        this.alertaService = alertaService;
        this.usuario = usuario;
    }

    /**
     * Incidentes del mes actual del usuario.
     */
    public long getIncidentesMes() {
        return misAlertas().stream()
                .filter(a -> a.getFechaHora() != null
                && a.getFechaHora().getMonth() == LocalDateTime.now().getMonth()
                && a.getFechaHora().getYear() == LocalDateTime.now().getYear())
                .count();
    }

    /**
     * Alertas pendientes o en atención del usuario.
     */
    @Override
    public long getAlertasActivas() {
        return misAlertas().stream()
                .filter(a -> a.getEstado() == EstadoAlerta.PENDIENTE
                || a.getEstado() == EstadoAlerta.EN_ATENCION)
                .count();
    }

    @Override
    public long getIncidentes() {
        return getIncidentesMes();
    }

    @Override
    public long getAlertasResueltas() {
        return misAlertas().stream()
                .filter(a -> a.getEstado() == EstadoAlerta.RESUELTA)
                .count();
    }

    /**
     * Cantidad de usuarios distintos que reportaron alertas en el mismo barrio
     * del usuario actual.
     */
    public long getVecinosActivos() {
        if (usuario.getDireccion() == null
                || usuario.getDireccion().getBarrio() == null) {
            return 0;
        }
        String miBarrio = usuario.getDireccion().getBarrio().getNombre();
        try {
            return alertaService.listar().stream()
                    .filter(a -> a.getBarrio() != null
                    && miBarrio.equalsIgnoreCase(a.getBarrio().getNombre()))
                    .map(a -> a.getUsuario() != null ? a.getUsuario().getUsername() : "")
                    .distinct()
                    .count();
        } catch (Exception e) {
            return 0;
        }
    }

    // ── Privado ───────────────────────────────────────────────────
    private List<sistemagestion.model.Alerta> misAlertas() {
        try {
            return alertaService.listar().stream()
                    .filter(a -> usuario.getUsername().equals(
                    a.getUsuario() != null ? a.getUsuario().getUsername() : ""))
                    .toList();
        } catch (Exception e) {
            return List.of();
        }
    }
}
