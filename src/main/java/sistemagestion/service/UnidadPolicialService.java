/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.service;

import java.sql.SQLException;
import java.util.List;
import sistemagestion.dao.UnidadPolicialDAO;
import sistemagestion.model.UnidadPolicial;
import sistemagestion.util.Validador;

/**
 *
 * @author Maria Cristina
 */

public class UnidadPolicialService {

    private UnidadPolicialDAO dao;

    public UnidadPolicialService() throws SQLException {
        dao = new UnidadPolicialDAO();
    }

    public void insertar(UnidadPolicial u) throws SQLException {
        Validador.validarObjeto(u);
        Validador.validarCampoVacio(u.getNombre());
        Validador.validarEnum(u.getEstado());
        Validador.validarObjeto(u.getBarrio());
        dao.insertar(
                u.getNombre(),
                u.getEstado().name(),
                u.getBarrio().getNombre(),
                u.getLatitud(),
                u.getLongitud()
        );
    }

    public void actualizar(UnidadPolicial u) throws SQLException {

        Validador.validarObjeto(u);
        Validador.validarCampoVacio(u.getNombre());
        Validador.validarEnum(u.getEstado());
        Validador.validarObjeto(u.getBarrio());

        dao.actualizar(
                u.getNombre(),
                u.getNombre(),
                u.getEstado().name(),
                u.getBarrio().getNombre(),
                u.getLatitud(),
                u.getLongitud()
        );
    }

    public List<UnidadPolicial> listar() throws SQLException {
        return dao.listar();
    }

    public void eliminar(String nombre) throws SQLException {

        Validador.validarCampoVacio(nombre);

        dao.eliminar(nombre);
    }
}