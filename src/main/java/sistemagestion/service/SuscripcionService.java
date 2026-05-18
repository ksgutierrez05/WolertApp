/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.service;

import java.sql.SQLException;
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
                s.getUsuario().getId_usuario(),
                s.getTipoalerta().getId_tipoalerta(),
                s.getComuna() != null ? s.getComuna().getId_comuna() : null,
                s.getBarrio() != null ? s.getBarrio().getId_barrio() : null,
                s.getEstado().name()
        );
    }

    public List<Suscripcion> listar() {
        return dao.listar();
    }
}
