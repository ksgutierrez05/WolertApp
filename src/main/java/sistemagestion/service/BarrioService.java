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

    private BarrioDAO barrioDAO;

    public BarrioService() throws SQLException {
        barrioDAO = new BarrioDAO();
    }

    public void insertar(Barrio b) throws SQLException {

        Validador.validarObjeto(b);
        Validador.validarCampoVacio(b.getNombre());
        Validador.validarObjeto(b.getComuna());

        barrioDAO.insertar(
                b.getNombre(),
                b.getComuna().getNombre()
        );
    }

    public void actualizar(Barrio b) throws SQLException {

        Validador.validarObjeto(b);
        Validador.validarCampoVacio(b.getNombre());

        barrioDAO.actualizar(
                b.getNombre(),
                b.getNombre()
        );
    }

    public List<Barrio> listar() throws SQLException {
        return barrioDAO.listar();
    }

    public void eliminar(String nombre) throws SQLException {

        Validador.validarCampoVacio(nombre);

        barrioDAO.eliminar(nombre);
    }
}