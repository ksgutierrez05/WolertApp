/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.service;

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

    public RolUsuarioService() {
        dao = new RolUsuarioDAO();
    }

    public boolean insertar(RolUsuario r) {

        Validador.validarObjeto(r);
        Validador.validarCampoVacio(r.getNombre());

        return dao.insertar(r.getIdRol(), r.getNombre());
    }

    public RolUsuario buscarPorId(int idRol) {

        if (idRol <= 0) {
            throw new IllegalArgumentException();
        }

        return dao.buscarPorId(idRol);
    }

    public List<RolUsuario> listar() {
        return dao.listar();
    }

    public boolean actualizar(RolUsuario r) {

        Validador.validarObjeto(r);
        Validador.validarCampoVacio(r.getNombre());

        return dao.actualizar(r.getIdRol(), r.getNombre());
    }

    public boolean eliminar(int idRol) {

        if (idRol <= 0) {
            throw new IllegalArgumentException();
        }

        return dao.eliminar(idRol);
    }
}