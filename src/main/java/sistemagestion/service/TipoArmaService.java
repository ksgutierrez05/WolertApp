/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.service;

import java.sql.SQLException;
import java.util.List;
import sistemagestion.dao.TipoArmaDAO;
import sistemagestion.model.TipoArma;
import sistemagestion.util.Validador;

/**
 *
 * @author Maria Cristina
 */
public class TipoArmaService {

    private TipoArmaDAO dao;

    public TipoArmaService() throws SQLException {
        dao = new TipoArmaDAO();
    }

    public void insertar(TipoArma t) throws SQLException {

        Validador.validarObjeto(t);
        Validador.validarCampoVacio(t.getNombre());

        dao.insertar(
                t.getNombre()
        );
    }

    public void actualizar(TipoArma t) throws SQLException {

        Validador.validarObjeto(t);
        Validador.validarCampoVacio(t.getNombre());

        dao.actualizar(
                t.getNombre(),
                t.getNombre()
        );
    }

    public List<TipoArma> listar() throws SQLException {
        return dao.listar();
    }

    public void eliminar(String nombre) throws SQLException {

        Validador.validarCampoVacio(nombre);

        dao.eliminar(nombre);
    }
}