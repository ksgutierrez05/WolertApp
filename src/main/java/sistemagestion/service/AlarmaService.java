/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.service;

/**
 *
 * @author Maria Cristina
 */
import java.sql.SQLException;
import java.util.List;
import sistemagestion.dao.AlarmaDAO;
import sistemagestion.model.Alarma;
import sistemagestion.util.Validador;
 
/**
 *
 * @author Maria Cristina
 */
public class AlarmaService {
 
    private AlarmaDAO alarmaDAO;
 
    public AlarmaService() throws SQLException {
        alarmaDAO = new AlarmaDAO();
    }
 
    public boolean insertar(Alarma a) {
 
        Validador.validarObjeto(a);
        Validador.validarCampoVacio(a.getNombre());
        Validador.validarObjeto(a.getBarrio());
        Validador.validarEnum(a.getEstado());
 
        return alarmaDAO.insertar(
                a.getNombre(),
                a.getBarrio().getId_barrio(),
                a.getLatitud(),
                a.getLongitud(),
                a.getRadio_cobertura(),
                a.getEstado().name()
        );
    }
 
    public Alarma buscarPorId(int id) {
 
        if (id <= 0) {
            throw new IllegalArgumentException();
        }
 
        return alarmaDAO.buscarPorId(id);
    }
 
    public List<Alarma> listar() {
        return alarmaDAO.listar();
    }
 
    public boolean actualizar(Alarma a) {
 
        Validador.validarObjeto(a);
        Validador.validarCampoVacio(a.getNombre());
        Validador.validarObjeto(a.getBarrio());
        Validador.validarEnum(a.getEstado());
 
        return alarmaDAO.actualizar(a);
    }
 
    public boolean actualizarEstado(int id, String estado) {
 
        if (id <= 0) {
            throw new IllegalArgumentException();
        }
 
        Validador.validarCampoVacio(estado);
 
        return alarmaDAO.actualizarEstado(id, estado);
    }
 
    public boolean eliminar(int id) {
 
        if (id <= 0) {
            throw new IllegalArgumentException();
        }
 
        return alarmaDAO.eliminar(id);
    }
 
    public List<Alarma> listarPorBarrio(int idBarrio) {
 
        if (idBarrio <= 0) {
            throw new IllegalArgumentException();
        }
 
        return alarmaDAO.listarPorBarrio(idBarrio);
    }
}