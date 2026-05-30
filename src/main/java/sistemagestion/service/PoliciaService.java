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
                p.getIdentificacion(),
                p.getUnidadpolicial().getNombre(),
                p.getPlaca(),
                p.getRango(),
                p.getEstadopolicial().name()
        );
    }

    public boolean actualizar(Policia p) {

        Validador.validarObjeto(p);
        Validador.validarObjeto(p.getUnidadpolicial());
        Validador.validarCampoVacio(p.getPlaca());
        Validador.validarCampoVacio(p.getRango());
        Validador.validarEnum(p.getEstadopolicial());

        return policiaDAO.actualizar(
                p.getIdentificacion(),
                p.getUnidadpolicial().getNombre(),
                p.getPlaca(),
                p.getRango(),
                p.getEstadopolicial().name()
        );
    }

    public boolean eliminar(String cedulaUsuario) {

        Validador.validarCampoVacio(cedulaUsuario);

        return policiaDAO.eliminar(cedulaUsuario);
    }

    public List<Policia> listar() {
        return policiaDAO.listar();
    }
}