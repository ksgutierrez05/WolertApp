/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.service;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import sistemagestion.model.AtencionAlerta;
import sistemagestion.util.Validador;

/**
 *
 * @author Maria Cristina
 */
public class AtencionAlertaService {
 
    private AtencionAlertaDAO atencioalertaDAO;
 
    public AtencionAlertaService() throws SQLException {
        atencioalertaDAO = new AtencionAlertaDAO();
    }
 
    public boolean insertar(AtencionAlerta a) {
 
        Validador.validarObjeto(a);
        Validador.validarObjeto(a.getAlerta());
        Validador.validarObjeto(a.getUnidad());
        Validador.validarEnum(a.getEstado());
        Validador.validarCampoVacio(a.getDescripcion());
 
        // Validar que la alerta no esté cancelada ni resuelta
        Validador.validarAlertaActiva(a.getAlerta());
 
        // Validar que la unidad esté operativa
        Validador.validarUnidadOperativa(a.getUnidad());
 
        LocalDateTime fecha = (a.getFechaatencion() != null)
                ? a.getFechaatencion()
                : LocalDateTime.now();
 
        return atencioalertaDAO.insertar(
                a.getAlerta().getId_alerta(),
                a.getUnidad().getId_unidad(),
                a.getEstado().name(),
                fecha,
                a.getDescripcion(),
                a.getTipoarma() != null ? a.getTipoarma().getId_tipoarma() : null,
                a.getMediotransporte() != null ? a.getMediotransporte().getId_mediotransporte() : null,
                a.getObservacion()
        );
    }
 
    public AtencionAlerta buscarPorId(int id) {
 
        if (id <= 0) {
            throw new IllegalArgumentException();
        }
 
        return atencioalertaDAO.buscarPorId(id);
    }
 
    public List<AtencionAlerta> listar() {
        return atencioalertaDAO.listar();
    }
 
    public boolean actualizar(AtencionAlerta a) {
 
        Validador.validarObjeto(a);
        Validador.validarObjeto(a.getAlerta());
        Validador.validarObjeto(a.getUnidad());
        Validador.validarEnum(a.getEstado());
        Validador.validarCampoVacio(a.getDescripcion());
 
        return atencioalertaDAO.actualizar(a);
    }
 
    public boolean actualizarEstado(int id, String estado) {
 
        if (id <= 0) {
            throw new IllegalArgumentException();
        }
 
        Validador.validarCampoVacio(estado);
 
        return atencioalertaDAO.actualizarEstado(id, estado);
    }
 
    public boolean eliminar(int id) {
 
        if (id <= 0) {
            throw new IllegalArgumentException();
        }
 
        return atencioalertaDAO.eliminar(id);
    }
 
    public List<AtencionAlerta> listarPorAlerta(int idAlerta) {
 
        if (idAlerta <= 0) {
            throw new IllegalArgumentException();
        }
 
        return atencioalertaDAO.listarPorAlerta(idAlerta);
    }
}