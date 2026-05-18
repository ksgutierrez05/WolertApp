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

    public boolean insertar(UnidadPolicial u) {

        Validador.validarObjeto(u);
        Validador.validarCampoVacio(u.getNombre());
        Validador.validarEnum(u.getEstado());
        Validador.validarObjeto(u.getBarrio());

        return dao.insertar(u);
    }

    public UnidadPolicial buscarPorId(int id) {
        if (id <= 0) throw new IllegalArgumentException();
        return dao.buscarPorId(id);
    }

    public List<UnidadPolicial> listar() {
        return dao.listar();
    }

    public boolean actualizar(UnidadPolicial u) {

        Validador.validarObjeto(u);
        Validador.validarCampoVacio(u.getNombre());
        Validador.validarEnum(u.getEstado());
        Validador.validarObjeto(u.getBarrio());

        return dao.actualizar(u);
    }

    public boolean eliminar(int id) {
        if (id <= 0) throw new IllegalArgumentException();
        return dao.eliminar(id);
    }
}
