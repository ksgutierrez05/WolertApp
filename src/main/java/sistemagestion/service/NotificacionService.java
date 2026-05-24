/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.service;

import java.sql.SQLException;
import sistemagestion.dao.NotificacionDAO;
import sistemagestion.model.Notificacion;
import sistemagestion.util.Validador;

/**
 *
 * @author Maria Cristina
 */
public class NotificacionService {

    private NotificacionDAO dao;

    public NotificacionService() throws SQLException {
        dao = new NotificacionDAO();
    }

    public boolean insertar(Notificacion n) {

        Validador.validarObjeto(n);
        Validador.validarObjeto(n.getUsuario());
        Validador.validarObjeto(n.getAlerta());
        Validador.validarCampoVacio(n.getMensaje());

        return dao.insertar(
                n.getAlerta().getId_alerta(),
                n.getUsuario().getId_usuario(),
                n.getMensaje()
        );
    }

    public Notificacion consultar(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException();
        }
        return dao.consultar(id);
    }

    public boolean eliminar(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException();
        }
        return dao.eliminar(id);
    }
}
