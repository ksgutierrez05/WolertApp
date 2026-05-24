/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.service;

import java.sql.SQLException;
import java.util.List;
import sistemagestion.dao.UsuarioDAO;
import sistemagestion.model.Usuario;
import sistemagestion.util.Validador;

/**
 *
 * @author Maria Cristina
 */
public class UsuarioService {

    private UsuarioDAO usuarioDAO;

    public UsuarioService() throws SQLException {

        usuarioDAO = new UsuarioDAO();
    }

   
    public boolean insertar(Usuario u) {

        
        Validador.validarObjeto(u);

       
        Validador.validarCampoVacio(
                u.getPrimer_nombre());

        Validador.validarCampoVacio(
                u.getPrimer_apellido());

        Validador.validarIdentificacion(
                u.getIdentificacion());

        Validador.validarTelefono(
                u.getTelefono());

        Validador.validarCorreo(
                u.getCorreo());

        Validador.validarUsername(
                u.getUsername());

        Validador.validarPassword(
                u.getPassword());

        Validador.validarEnum(
                u.getEstado());

        Validador.validarObjeto(
                u.getRol());

        return usuarioDAO.insertar(

                u.getPrimer_nombre(),

                u.getSegundo_nombre(),

                u.getPrimer_apellido(),

                u.getSegundo_apellido(),

                u.getIdentificacion(),

                u.getTelefono(),

                u.getCorreo(),

                u.getUsername(),

                u.getPassword(),

                u.getEstado().name(),

                u.getRol().getIdRol(),

                u.getDireccion() != null
                && u.getDireccion().getBarrio() != null
                        ? u.getDireccion()
                                .getBarrio()
                                .getId_barrio()
                        : null
        );
    }

    public void actualizar(Usuario u)
            throws SQLException {

        Validador.validarObjeto(u);

        Validador.validarCampoVacio(
                u.getPrimer_nombre());

        Validador.validarCampoVacio(
                u.getPrimer_apellido());

        Validador.validarTelefono(
                u.getTelefono());

        Validador.validarCorreo(
                u.getCorreo());

        Validador.validarUsername(
                u.getUsername());

        Validador.validarPassword(
                u.getPassword());

        usuarioDAO.actualizar(u);
    }

    
    public void eliminar(int id)
            throws SQLException {

        if (id <= 0) {

            throw new IllegalArgumentException();
        }

        usuarioDAO.eliminar(id);
    }

    public boolean existePorCedula(
            String cedula)
            throws SQLException {

        Validador.validarIdentificacion(
                cedula);

        return usuarioDAO.existePorCedula(
                cedula);
    }

    
    public Usuario login(
            String username,
            String password)
            throws SQLException {

        Validador.validarUsername(
                username);

        Validador.validarPassword(
                password);

        return usuarioDAO.login(
                username,
                password);
    }

   
    public Usuario buscarPorId(int id)
            throws SQLException {

        if (id <= 0) {

            throw new IllegalArgumentException();
        }

        return usuarioDAO.buscarPorId(id);
    }

    
    public List<Usuario> listarTodos()
            throws SQLException {

        return usuarioDAO.listarTodos();
    }
}
