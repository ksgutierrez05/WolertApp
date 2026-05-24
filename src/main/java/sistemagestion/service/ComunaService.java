/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.service;

import java.sql.SQLException;
import java.util.List;
import sistemagestion.dao.ComunaDAO;
import sistemagestion.model.Comuna;
import sistemagestion.util.Validador;

/**
 *
 * @author Maria Cristina
 */
public class ComunaService {

    private ComunaDAO dao;

    public ComunaService() throws SQLException {
        dao = new ComunaDAO();
    }

    public void insertar(Comuna c) throws SQLException {

        Validador.validarObjeto(c);
        Validador.validarCampoVacio(c.getNombre());

        dao.insertar(
        c.getNombre()
    );
    }

    public Comuna buscarPorId(int id) throws SQLException {

        if (id <= 0) {
            throw new IllegalArgumentException();
        }

        return dao.buscarPorId(id);
    }

    public List<Comuna> listar() throws SQLException {
        return dao.listar();
    }

    public void actualizar(Comuna c) throws SQLException {

        Validador.validarObjeto(c);
        Validador.validarCampoVacio(c.getNombre());

        dao.actualizar(c);
    }

    public void eliminar(int id) throws SQLException {

        if (id <= 0) {
            throw new IllegalArgumentException();
        }

        dao.eliminar(id);
    }
}
