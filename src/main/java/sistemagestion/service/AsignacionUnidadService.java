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

    private AsignacionUnidadDAO dao;

    public AsignacionUnidadService() throws SQLException {
        dao = new AsignacionUnidadDAO();
    }

    public boolean insertar(AsignacionUnidad a) {

        Validador.validarObjeto(a);
        Validador.validarObjeto(a.getAlerta());
        Validador.validarObjeto(a.getUnidadpolicial());
        Validador.validarCampoVacio(a.getObservacion());

        LocalDateTime fecha = (a.getFechahoraasignacion() != null)
                ? a.getFechahoraasignacion()
                : LocalDateTime.now();

        return dao.insertar(
                a.getAlerta().getId_alerta(),
                a.getUnidadpolicial().getId_unidad(),
                a.getObservacion(),
                fecha
        );
    }

    public List<AsignacionUnidad> listar() {
        return dao.listar();
    }

    public AsignacionUnidad buscarPorId(int id) {
        if (id <= 0) throw new IllegalArgumentException();
        return dao.buscarPorId(id);
    }

    public boolean actualizar(AsignacionUnidad a) {
        Validador.validarObjeto(a);
        return dao.actualizar(a);
    }

    public boolean eliminar(int id) {
        if (id <= 0) throw new IllegalArgumentException();
        return dao.eliminar(id);
    }
}
