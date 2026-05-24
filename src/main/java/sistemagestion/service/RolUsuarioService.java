/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.service;

import java.sql.SQLException;
import java.util.List;
import sistemagestion.dao.RolUsuarioDAO;
import sistemagestion.model.RolUsuario;
import sistemagestion.util.Validador;

/**
 *
 * @author Maria Cristina
 */
public class RolUsuarioService {

    private RolUsuarioDAO dao;

    public RolUsuarioService() throws SQLException {
        dao = new RolUsuarioDAO();
    }

    public boolean insertar(RolUsuario r) {

        Validador.validarObjeto(r);
        Validador.validarCampoVacio(r.getNombre());

        return dao.insertar(
                r.getNombre()
        );
    }

    public List<RolUsuario> listar() {
        return dao.listar();
    }

    public boolean actualizar(RolUsuario r) {

        Validador.validarObjeto(r);
        Validador.validarCampoVacio(r.getNombre());

        return dao.actualizar(
                r.getNombre(),
                r.getNombre()
        );
    }

    public boolean eliminar(String nombre) {

        Validador.validarCampoVacio(nombre);

        return dao.eliminar(nombre);
    }
}
