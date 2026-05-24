/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.service;

import java.sql.SQLException;
import java.util.List;
import sistemagestion.dao.TipoAlertaDAO;
import sistemagestion.model.TipoAlerta;
import sistemagestion.util.Validador;

/**
 *
 * @author Maria Cristina
 */
public class TipoAlertaService {

    private TipoAlertaDAO dao;

    public TipoAlertaService() throws SQLException {
        dao = new TipoAlertaDAO();
    }

    public boolean insertar(TipoAlerta t) {

        Validador.validarObjeto(t);
        Validador.validarCampoVacio(t.getNombre());

        return dao.insertar(
                t.getNombre()
        );
    }

    public List<TipoAlerta> listar() {
        return dao.listar();
    }

    public boolean actualizar(TipoAlerta t) {

        Validador.validarObjeto(t);
        Validador.validarCampoVacio(t.getNombre());

        return dao.actualizar(
                t.getNombre(),
                t.getNombre()
        );
    }

    public boolean eliminar(String nombre) {

        Validador.validarCampoVacio(nombre);

        return dao.eliminar(nombre);
    }
}