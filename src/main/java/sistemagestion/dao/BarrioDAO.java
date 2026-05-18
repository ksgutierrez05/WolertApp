/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import oracle.jdbc.OracleTypes;
import sistemagestion.model.Barrio;
import sistemagestion.model.Comuna;

/**
 *
 * @author Lenovo
 */
public class BarrioDAO {

    private Connection con;

    public BarrioDAO() throws SQLException {
        this.con = ConexionDB.getInstancia().getConexion();
    }

    public void insertar(Barrio b) throws SQLException {
        String sql = "{CALL pkg_barrios.pr_insertar_barrio(?,?)}";

        try (CallableStatement cs = con.prepareCall(sql)) {
            cs.setString(1, b.getNombre());
            cs.setInt(2, b.getComuna().getId_comuna());
            cs.execute();
        }
    }

    public void actualizar(Barrio b) throws SQLException {
        String sql = "{CALL pkg_barrios.pr_actualizar_barrio(?,?)}";

        try (CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt(1, b.getId_barrio());
            cs.setString(2, b.getNombre());
            cs.execute();
        }
    }

    public List<Barrio> listar() throws SQLException {
        List<Barrio> lista = new ArrayList<>();

        String sql = "{CALL pkg_barrios.pr_listar_barrios(?)}";

        try (CallableStatement cs = con.prepareCall(sql)) {

            cs.registerOutParameter(1, OracleTypes.CURSOR);
            cs.execute();

            try (ResultSet rs = (ResultSet) cs.getObject(1)) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }

        return lista;
    }

    public Barrio buscarPorId(int id) throws SQLException {
        String sql = "{CALL pkg_barrios.pr_consultar_barrio(?,?)}";

        try (CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, id);
            cs.registerOutParameter(2, oracle.jdbc.OracleTypes.CURSOR);

            cs.execute();

            try (ResultSet rs = (ResultSet) cs.getObject(2)) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }

        return null;
    }

    public void eliminar(int id) throws SQLException {
        String sql = "{CALL pkg_barrios.pr_eliminar_barrio(?)}";

        try (CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt(1, id);
            cs.execute();
        }
    }

    private Barrio mapear(ResultSet rs) throws SQLException {
        Barrio b = new Barrio();
        b.setId_barrio(rs.getInt("ID_BARRIO"));
        b.setNombre(rs.getString("NOMBRE"));

        Comuna c = new Comuna();
        c.setId_comuna(rs.getInt("ID_COMUNA"));
        c.setNombre(rs.getString("NOMBRE_COMUNA"));

        b.setComuna(c);

        return b;
    }

}
