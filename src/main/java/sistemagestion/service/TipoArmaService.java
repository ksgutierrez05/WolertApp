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

    public boolean insertar(TipoArma t) {

        Validador.validarObjeto(t);
        Validador.validarCampoVacio(t.getNombre());

        return dao.insertar(t);
    }

    public TipoArma buscarPorId(int id) {

        if (id <= 0) {
            throw new IllegalArgumentException();
        }

        return dao.buscarPorId(id);
    }

    public List<TipoArma> listarTodos() {
        return dao.listarTodos();
    }

    public boolean actualizar(TipoArma t) {

        Validador.validarObjeto(t);
        Validador.validarCampoVacio(t.getNombre());

        return dao.actualizar(t);
    }

    public boolean eliminar(int id) {

        if (id <= 0) {
            throw new IllegalArgumentException();
        }

        return dao.eliminar(id);
    }
}
