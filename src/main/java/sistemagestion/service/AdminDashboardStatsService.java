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

import java.util.List;

/**
 * Calcula las estadísticas del dashboard del administrador.
 *
 * Patrón GRASP: Information Expert — concentra la lógica de cálculo de métricas
 * que antes estaba dispersa en AdministradorApp. Principio SOLID: SRP — única
 * responsabilidad: calcular estadísticas admin. DIP — depende de AlertaService
 * y UsuarioService por constructor.
 */
public class AdminDashboardStatsService implements IDashboardStatsService {

    private final AlertaService alertaService;
    private final UsuarioService usuarioService;

    public AdminDashboardStatsService(AlertaService alertaService,
            UsuarioService usuarioService) {
        this.alertaService = alertaService;
        this.usuarioService = usuarioService;
    }

    /**
     * Total de usuarios registrados en el sistema.
     */
    public long getTotalUsuarios() {
        try {
            return usuarioService.listar().size();
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public long getAlertasActivas() {
        return cargarAlertas().stream()
                .filter(a -> a.getEstado() == EstadoAlerta.PENDIENTE
                || a.getEstado() == EstadoAlerta.RECIBIDA
                || a.getEstado() == EstadoAlerta.EN_ATENCION
                || a.getEstado() == EstadoAlerta.UNIDAD_ASIGNADA)
                .count();
    }

    @Override
    public long getIncidentes() {
        return cargarAlertas().stream()
                .filter(a -> a.getEstado() == EstadoAlerta.EN_ATENCION
                || a.getEstado() == EstadoAlerta.UNIDAD_ASIGNADA)
                .count();
    }

    @Override
    public long getAlertasResueltas() {
        return cargarAlertas().stream()
                .filter(a -> a.getEstado() == EstadoAlerta.RESUELTA)
                .count();
    }

    /**
     * Lista completa de alertas, retorna vacío en caso de error.
     */
    public List<sistemagestion.model.Alerta> cargarAlertas() {
        try {
            return alertaService.listar();
        } catch (Exception e) {
            return List.of();
        }
    }
}
