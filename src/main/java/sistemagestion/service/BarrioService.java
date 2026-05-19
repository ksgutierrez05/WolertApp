/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.service;

import java.sql.SQLException;
import java.util.List;
import sistemagestion.dao.BarrioDAO;
import sistemagestion.model.Barrio;
import sistemagestion.util.Validador;

/**
 *
 * @author Maria Cristina
 */
public class BarrioService {

    private BarrioDAO dao;

    public BarrioService() throws SQLException {
        dao = new BarrioDAO();
    }

    public void insertar(Barrio b) throws SQLException {

        Validador.validarObjeto(b);
        Validador.validarCampoVacio(b.getNombre());
        Validador.validarObjeto(b.getComuna());

         dao.insertar(
        b.getNombre(),
        b.getComuna().getId_comuna()
       );
    }

    public void actualizar(Barrio b) throws SQLException {

        Validador.validarObjeto(b);
        Validador.validarCampoVacio(b.getNombre());

        dao.actualizar(b);
    }

    public List<Barrio> listar() throws SQLException {
        return dao.listar();
    }

    public Barrio buscarPorId(int id) throws SQLException {

        if (id <= 0) {
            throw new IllegalArgumentException();
        }

        return dao.buscarPorId(id);
    }

    public void eliminar(int id) throws SQLException {

        if (id <= 0) {
            throw new IllegalArgumentException();
        }

        dao.eliminar(id);
    }
}