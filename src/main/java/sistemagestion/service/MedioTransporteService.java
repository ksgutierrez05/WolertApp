/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.service;

import java.sql.SQLException;
import java.util.List;
import sistemagestion.dao.MedioTransporteDAO;
import sistemagestion.model.MedioTransporte;
import sistemagestion.util.Validador;

/**
 *
 * @author Maria Cristina
 */
public class MedioTransporteService {

    private MedioTransporteDAO dao;

    public MedioTransporteService() throws SQLException {
        dao = new MedioTransporteDAO();
    }

    public boolean insertar(MedioTransporte m) {

        Validador.validarObjeto(m);
        Validador.validarCampoVacio(m.getNombre());

        return dao.insertar(m);
    }

    public MedioTransporte buscarPorId(int id) {

        if (id <= 0) {
            throw new IllegalArgumentException();
        }

        return dao.buscarPorId(id);
    }

    public List<MedioTransporte> listar() {
        return dao.listar();
    }

    public boolean actualizar(MedioTransporte m) {

        Validador.validarObjeto(m);
        Validador.validarCampoVacio(m.getNombre());

        return dao.actualizar(m);
    }

    public boolean eliminar(int id) {

        if (id <= 0) {
            throw new IllegalArgumentException();
        }

        return dao.eliminar(id);
    }
}
