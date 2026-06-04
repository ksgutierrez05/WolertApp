/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.service;

import java.sql.SQLException;
import java.util.List;
import sistemagestion.dao.AtencionAlertaDAO;
import sistemagestion.model.AtencionAlerta;
import sistemagestion.util.Validador;

/**
 *
 * @author Maria Cristina
 */
public class AtencionAlertaService {

    private AtencionAlertaDAO atencionDAO;

    public AtencionAlertaService() throws SQLException {
        atencionDAO = new AtencionAlertaDAO();
    }

    public boolean insertar(AtencionAlerta a, int Policia) {
        Validador.validarObjeto(a);
        Validador.validarObjeto(a.getAlerta());
        Validador.validarObjeto(a.getUnidad());
        Validador.validarCampoVacio(a.getUnidad().getNombre());
        Validador.validarEnum(a.getEstado());
        Validador.validarCampoVacio(a.getDescripcion());

        String tipoArma = null;
        String medioTransporte = null;
        if (a.getTipoarma() != null) {
            tipoArma = a.getTipoarma().getNombre();
        }
        if (a.getMediotransporte() != null) {
            medioTransporte = a.getMediotransporte().getNombre();
        }

        int idPolicia = Policia;

        System.out.println("Parametro Policia = " + Policia);
        System.out.println("a.getPolicia() = " + a.getPolicia());

        return atencionDAO.insertar(
                a.getAlerta().getId_alerta(),
                a.getUnidad().getNombre(),
                a.getEstado().name(),
                a.getDescripcion(),
                tipoArma,
                medioTransporte,
                a.getObservacion(),
                idPolicia
        );
    }

    public boolean actualizar(AtencionAlerta a) {

        Validador.validarObjeto(a);

        if (a.getId_atencion() <= 0) {
            throw new IllegalArgumentException();
        }

        Validador.validarEnum(a.getEstado());
        Validador.validarCampoVacio(a.getDescripcion());

        String tipoArma = null;
        String medioTransporte = null;

        if (a.getTipoarma() != null) {
            tipoArma = a.getTipoarma().getNombre();
        }

        if (a.getMediotransporte() != null) {
            medioTransporte = a.getMediotransporte().getNombre();
        }

        return atencionDAO.actualizar(
                a.getId_atencion(),
                a.getEstado().name(),
                a.getDescripcion(),
                tipoArma,
                medioTransporte,
                a.getObservacion()
        );
    }

    public boolean eliminar(int id) {

        if (id <= 0) {
            throw new IllegalArgumentException();
        }

        return atencionDAO.eliminar(id);
    }

    public AtencionAlerta buscarPorId(int id) {

        if (id <= 0) {
            throw new IllegalArgumentException();
        }

        return atencionDAO.buscarPorId(id);
    }

    public List<AtencionAlerta> listar() {
        return atencionDAO.listar();
    }

    public List<AtencionAlerta> listarPorAlerta(int idAlerta) {

        if (idAlerta <= 0) {
            throw new IllegalArgumentException();
        }

        return atencionDAO.listarPorAlerta(idAlerta);
    }

    public List<AtencionAlerta> buscarPorEstado(String estado) {

        Validador.validarCampoVacio(estado);

        return atencionDAO.buscarPorEstado(estado);
    }

    public List<AtencionAlerta> buscarPorEstadoExacto(String estado) {

        Validador.validarCampoVacio(estado);

        return atencionDAO.buscarPorEstadoExacto(estado);
    }

}
