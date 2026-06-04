/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.service;

import java.sql.SQLException;
import java.util.List;
import sistemagestion.dao.NotificacionDAO;
import sistemagestion.model.Notificacion;
import sistemagestion.util.Validador;

/**
 *
 * @author Maria Cristina
 */
public class NotificacionService {

    private NotificacionDAO notificacionDAO;

    public NotificacionService() throws SQLException {
        notificacionDAO = new NotificacionDAO();
    }

    public boolean insertar(Notificacion n) {
        Validador.validarObjeto(n);
        Validador.validarObjeto(n.getUsuario());
        Validador.validarObjeto(n.getAlerta());
        Validador.validarCampoVacio(n.getMensaje());

        boolean guardado = notificacionDAO.insertar(
                n.getAlerta().getId_alerta(),
                n.getUsuario().getIdentificacion(),
                n.getMensaje()
        );

        if (guardado) {
            NotificacionEmailSender.enviar(n);
        }

        return guardado;
    }

    public List<Notificacion> listarPorUnidad(int idUnidad) {
        return notificacionDAO.listarPorUnidad(idUnidad);
    }

    public List<Notificacion> listar() {
        return notificacionDAO.listar();
    }

    public boolean eliminar(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException();
        }
        return notificacionDAO.eliminar(id);
    }
}
