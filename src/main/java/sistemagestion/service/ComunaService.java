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

    private ComunaDAO comunaDAO;

    public ComunaService() throws SQLException {
        comunaDAO = new ComunaDAO();
    }

    public void insertar(Comuna c) throws SQLException {

        Validador.validarObjeto(c);
        Validador.validarCampoVacio(c.getNombre());

        comunaDAO.insertar(
                c.getNombre()
        );
    }

    public List<Comuna> listar() throws SQLException {
        return comunaDAO.listar();
    }

    public void actualizar(Comuna c) throws SQLException {

        Validador.validarObjeto(c);
        Validador.validarCampoVacio(c.getNombre());

        comunaDAO.actualizar(
                c.getNombre(),
                c.getNombre()
        );
    }

    public void eliminar(String nombre) throws SQLException {

        Validador.validarCampoVacio(nombre);

        comunaDAO.eliminar(nombre);
    }
}