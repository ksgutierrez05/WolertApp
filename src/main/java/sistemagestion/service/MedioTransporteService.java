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

    private MedioTransporteDAO medioDAO;

    public MedioTransporteService() throws SQLException {
        medioDAO = new MedioTransporteDAO();
    }

    public boolean insertar(MedioTransporte m) {

        Validador.validarObjeto(m);
        Validador.validarCampoVacio(m.getNombre());

        return medioDAO.insertar(
                m.getNombre()
        );
    }

    public List<MedioTransporte> listar() {
        return medioDAO.listar();
    }

    public boolean actualizar(MedioTransporte m) {

        Validador.validarObjeto(m);
        Validador.validarCampoVacio(m.getNombre());

        return medioDAO.actualizar(
                m.getNombre(),
                m.getNombre()
        );
    }

    public boolean eliminar(String nombre) {

        Validador.validarCampoVacio(nombre);

        return medioDAO.eliminar(nombre);
    }
}