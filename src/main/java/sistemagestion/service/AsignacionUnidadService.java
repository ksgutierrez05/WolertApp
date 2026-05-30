/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.service;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import sistemagestion.dao.AsignacionUnidadDAO;
import sistemagestion.model.AsignacionUnidad;
import sistemagestion.util.Validador;

/**
 *
 * @author Maria Cristina
 */
public class AsignacionUnidadService {

    private AsignacionUnidadDAO asignacionDAO;

    public AsignacionUnidadService() throws SQLException {
        asignacionDAO = new AsignacionUnidadDAO();
    }

    public boolean insertar(AsignacionUnidad a) {

        Validador.validarObjeto(a);
        Validador.validarObjeto(a.getAlerta());
        Validador.validarObjeto(a.getUnidadpolicial());
        Validador.validarCampoVacio(a.getObservacion());

        LocalDateTime fecha = (a.getFechahoraasignacion() != null)
                ? a.getFechahoraasignacion()
                : LocalDateTime.now();

        return asignacionDAO.insertar(
                a.getAlerta().getId_alerta(),
                a.getUnidadpolicial().getNombre(),
                a.getObservacion(),
                fecha
        );
    }

    public boolean asignarUnidadCercana(int idAlerta) {
        if (idAlerta <= 0) {
            throw new IllegalArgumentException("ID de alerta inválido.");
        }
        return asignacionDAO.asignarUnidadCercana(idAlerta);
    }

    public boolean actualizar(AsignacionUnidad a) {

        Validador.validarObjeto(a);
        Validador.validarObjeto(a.getAlerta());
        Validador.validarObjeto(a.getUnidadpolicial());

        if (a.getId_asignacion() <= 0) {
            throw new IllegalArgumentException();
        }

        Validador.validarCampoVacio(a.getObservacion());

        LocalDateTime fecha = (a.getFechahoraasignacion() != null)
                ? a.getFechahoraasignacion()
                : LocalDateTime.now();

        return asignacionDAO.actualizar(
                a.getId_asignacion(),
                a.getAlerta().getId_alerta(),
                a.getUnidadpolicial().getNombre(),
                a.getObservacion(),
                fecha
        );
    }

    public List<AsignacionUnidad> listar() {
        return asignacionDAO.listar();
    }

    public boolean eliminar(int id) {

        if (id <= 0) {
            throw new IllegalArgumentException();
        }

        return asignacionDAO.eliminar(id);
    }
}
