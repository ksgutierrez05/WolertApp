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

    private UsuarioDAO dao;

    public UsuarioService() throws SQLException {
        dao = new UsuarioDAO();
    }

    public void insertar(Usuario u) throws SQLException {

        Validador.validarObjeto(u);

        Validador.validarCampoVacio(u.getPrimer_nombre());
        Validador.validarCampoVacio(u.getPrimer_apellido());

        Validador.validarIdentificacion(u.getIdentificacion());
        Validador.validarTelefono(u.getTelefono());
        Validador.validarCorreo(u.getCorreo());
        Validador.validarUsername(u.getUsername());
        Validador.validarPassword(u.getPassword());

        Validador.validarEnum(u.getEstado());
        Validador.validarObjeto(u.getRol());

        dao.insertar(
                u.getPrimer_nombre(),
                u.getSegundo_nombre(),
                u.getPrimer_apellido(),
                u.getSegundo_apellido(),
                u.getIdentificacion(),
                u.getTelefono(),
                u.getCorreo(),
                u.getUsername(),
                u.getPassword(),
                u.getRol().getNombre(),
                u.getDireccion().getBarrio().getNombre(),
                u.getDireccion().getCalle(),
                u.getDireccion().getCarrera(),
                u.getDireccion().getEtapa(),
                u.getDireccion().getManzana(),
                u.getDireccion().getCasa()
        );
    }

    public void actualizar(Usuario u) throws SQLException {

        Validador.validarObjeto(u);

        Validador.validarCampoVacio(u.getPrimer_nombre());
        Validador.validarCampoVacio(u.getPrimer_apellido());

        Validador.validarTelefono(u.getTelefono());
        Validador.validarCorreo(u.getCorreo());
        Validador.validarUsername(u.getUsername());
        Validador.validarPassword(u.getPassword());

        dao.actualizar(
                u.getUsername(),
                u.getPrimer_nombre(),
                u.getSegundo_nombre(),
                u.getPrimer_apellido(),
                u.getSegundo_apellido(),
                u.getTelefono(),
                u.getCorreo(),
                u.getPassword(),
                u.getRol().getNombre(),
                u.getDireccion().getBarrio().getNombre(),
                u.getDireccion().getCalle(),
                u.getDireccion().getCarrera(),
                u.getDireccion().getEtapa(),
                u.getDireccion().getManzana(),
                u.getDireccion().getCasa()
        );
    }

    public Usuario login(String username, String password) throws SQLException {
        Validador.validarUsername(username);
        Validador.validarCampoVacio(password);
        return dao.login(username, password);
    }

    public Usuario buscarPorCedula(String cedula) throws SQLException {

        Validador.validarIdentificacion(cedula);

        return dao.buscarPorCedula(cedula);
    }

    public List<Usuario> listar() throws SQLException {
        return dao.listarTodos();
    }

    public void eliminar(String cedula) throws SQLException {

        Validador.validarIdentificacion(cedula);

        dao.eliminar(cedula);
    }
}
