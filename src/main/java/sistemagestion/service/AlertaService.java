/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.service;

import java.sql.SQLException;
import java.util.List;
import sistemagestion.dao.AlertaDAO;
import sistemagestion.model.Alerta;
import sistemagestion.util.Validador;

public class AlertaService {

    private AlertaDAO alertaDAO;

    public AlertaService() throws SQLException {
        alertaDAO = new AlertaDAO();
    }

    public boolean insertar(Alerta a) {

        Validador.validarObjeto(a);

        Validador.validarObjeto(a.getUsuario());
        Validador.validarObjeto(a.getTipoalerta());
        Validador.validarObjeto(a.getBarrio());

        Validador.validarCampoVacio(a.getDescripcion());

        return alertaDAO.insertar(
                a.getUsuario().getUsername(),
                a.getTipoalerta().getNombre(),
                a.getBarrio().getNombre(),
                a.getTipoarma() != null
                ? a.getTipoarma().getNombre()
                : null,
                a.getMediotransporte() != null
                ? a.getMediotransporte().getNombre()
                : null,
                a.getDireccion() != null
                ? a.getDireccion().getEtapa()
                : null,
                a.getDireccion() != null
                ? a.getDireccion().getSector()
                : null,
                a.getDireccion() != null
                ? a.getDireccion().getManzana()
                : null,
                a.getDireccion() != null
                ? a.getDireccion().getCasa()
                : null,
                a.getDireccion() != null
                ? a.getDireccion().getCalle()
                : null,
                a.getDireccion() != null
                ? a.getDireccion().getCarrera()
                : null,
                a.getDireccion() != null
                ? a.getDireccion().getReferencia()
                : null,
                a.getDireccion() != null
                ? a.getDireccion().getLatitud()
                : null,
                a.getDireccion() != null
                ? a.getDireccion().getLongitud()
                : null,
                a.getDescripcion()
        );
    }

    public List<Alerta> listar() {
        return alertaDAO.listar();
    }

    public List<Alerta> listarPorUsuario(String username) {
        Validador.validarCampoVacio(username);
        return alertaDAO.listarPorUsuario(username);
    }

    public List<Alerta> listarPorBarrio(String nombreBarrio) {
        Validador.validarCampoVacio(nombreBarrio);
        return alertaDAO.listarPorBarrio(nombreBarrio);
    }

    public List<Alerta> buscarPorTipo(String tipoAlerta) {
        Validador.validarCampoVacio(tipoAlerta);
        return alertaDAO.buscarPorTipo(tipoAlerta);
    }

    public Alerta buscarPorId(int id) {

        if (id <= 0) {
            throw new IllegalArgumentException();
        }

        return alertaDAO.buscarPorId(id);
    }

    public boolean actualizarEstado(int id, String estado) {

        if (id <= 0) {
            throw new IllegalArgumentException();
        }

        Validador.validarCampoVacio(estado);

        return alertaDAO.actualizarEstado(id, estado);
    }

    public boolean eliminar(int id) {

        if (id <= 0) {
            throw new IllegalArgumentException();
        }

        return alertaDAO.eliminar(id);
    }
}
