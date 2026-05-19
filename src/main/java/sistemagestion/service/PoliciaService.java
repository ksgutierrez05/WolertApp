/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.service;

import java.sql.SQLException;
import java.util.List;
import sistemagestion.dao.PoliciaDAO;
import sistemagestion.model.Policia;
import sistemagestion.util.Validador;

/**
 *
 * @author Maria Cristina
 */
public class PoliciaService {

    private PoliciaDAO policiaDAO;

    public PoliciaService() throws SQLException {
        policiaDAO = new PoliciaDAO();
    }

    public boolean insertar(Policia p) {

        Validador.validarObjeto(p);

        Validador.validarObjeto(p.getUnidadpolicial());

        Validador.validarCampoVacio(p.getPlaca());

        Validador.validarCampoVacio(p.getRango());

        Validador.validarEnum(p.getEstadopolicial());

        return policiaDAO.insertar(
            p.getId_policia(),
            p.getUnidadpolicial().getId_unidad(),
            p.getPlaca(),
            p.getRango(),
            p.getEstadopolicial().name()
    );
    }

    public void actualizar(Policia p)
            throws SQLException {

        Validador.validarObjeto(p);

        Validador.validarObjeto(p.getUnidadpolicial());

        Validador.validarCampoVacio(p.getPlaca());

        Validador.validarCampoVacio(p.getRango());

        Validador.validarEnum(p.getEstadopolicial());

        policiaDAO.actualizar(p);
    }

    public void eliminar(int idUsuario)
            throws SQLException {

        if (idUsuario <= 0) {

            throw new IllegalArgumentException();
        }

        policiaDAO.eliminar(idUsuario);
    }

    public Policia buscarPorId(int idUsuario)
            throws SQLException {

        if (idUsuario <= 0) {

            throw new IllegalArgumentException();
        }

        return policiaDAO.buscarPorId(idUsuario);
    }

    public List<Policia> listar()
            throws SQLException {

        return policiaDAO.listar();
    }
}
