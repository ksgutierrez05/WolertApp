/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.service;

import java.sql.SQLException;
import java.util.List;
import sistemagestion.dao.SuscripcionDAO;
import sistemagestion.model.Suscripcion;
import sistemagestion.util.Validador;

/**
 *
 * @author Maria Cristina
 */
public class SuscripcionService {

    private SuscripcionDAO dao;

    public SuscripcionService() throws SQLException {
        dao = new SuscripcionDAO();
    }

    public boolean insertar(Suscripcion s) {

        Validador.validarObjeto(s);
        Validador.validarObjeto(s.getUsuario());
        Validador.validarObjeto(s.getTipoalerta());
        Validador.validarEnum(s.getEstado());

        return dao.insertar(
                s.getUsuario().getIdentificacion(),
                s.getTipoalerta().getNombre(),
                s.getComuna() != null ? s.getComuna().getNombre() : null,
                s.getBarrio() != null ? s.getBarrio().getNombre() : null,
                s.getEstado().name()
        );
    }

    public List<Suscripcion> listar() {
        return dao.listar();
    }

    public boolean actualizar(Suscripcion s) {

        Validador.validarObjeto(s);
        Validador.validarObjeto(s.getUsuario());
        Validador.validarObjeto(s.getTipoalerta());
        Validador.validarEnum(s.getEstado());

        return dao.actualizar(
                s.getId_suscripcion(),
                s.getUsuario().getIdentificacion(),
                s.getTipoalerta().getNombre(),
                s.getComuna() != null ? s.getComuna().getNombre() : null,
                s.getBarrio() != null ? s.getBarrio().getNombre() : null,
                s.getEstado().name()
        );
    }

    public boolean eliminar(int id) {

        if (id <= 0) {
            throw new IllegalArgumentException();
        }

        return dao.eliminar(id);
    }
}
