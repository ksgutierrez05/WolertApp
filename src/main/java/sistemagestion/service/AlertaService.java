/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.service;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import sistemagestion.dao.AlertaDAO;
import sistemagestion.model.Alerta;
import sistemagestion.util.Validador;

/**
 *
 * @author Maria Cristina
 */
public class AlertaService {

    private AlertaDAO alertaDAO;

    public AlertaService() throws SQLException {
        alertaDAO = new AlertaDAO();
    }

    public boolean insertar(Alerta a) {

        Validador.validarObjeto(a);
        Validador.validarCampoVacio(a.getDescripcion());
        Validador.validarObjeto(a.getUsuario());
        Validador.validarObjeto(a.getTipoalerta());
        Validador.validarObjeto(a.getBarrio());
        Validador.validarEnum(a.getEstado());

        LocalDateTime fecha = (a.getFechaHora() != null)
                ? a.getFechaHora()
                : LocalDateTime.now();

        return alertaDAO.insertar(
                a.getUsuario().getId_usuario(),
                a.getTipoalerta().getId_tipoalerta(),
                a.getBarrio().getId_barrio(),
                a.getTipoarma() != null ? a.getTipoarma().getId_tipoarma() : null,
                a.getMediotransporte() != null ? a.getMediotransporte().getId_mediotransporte() : null,
                a.getEstado().name(),
                a.getDireccion() != null ? a.getDireccion().getSector() : null,
                a.getDireccion() != null ? a.getDireccion().getManzana() : null,
                a.getDireccion() != null ? a.getDireccion().getCasa() : null,
                a.getDireccion() != null ? a.getDireccion().getCalle() : null,
                a.getDireccion() != null ? a.getDireccion().getCarrera() : null,
                a.getDireccion() != null ? a.getDireccion().getReferencia() : null,
                a.getDescripcion(),
                fecha
        );
    }

    public List<Alerta> listar() {
        return alertaDAO.listar();
    }

    public Alerta buscarPorId(int id) {
        if (id <= 0) throw new IllegalArgumentException();
        return alertaDAO.buscarPorId(id);
    }

    public boolean actualizarEstado(int id, String estado) {
        Validador.validarCampoVacio(estado);
        return alertaDAO.actualizarEstado(id, estado);
    }

    public boolean eliminar(int id) {
        if (id <= 0) throw new IllegalArgumentException();
        return alertaDAO.eliminar(id);
    }
}