/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import oracle.jdbc.OracleTypes;
import sistemagestion.model.Comuna;

/**
 *
 * @author Lenovo
 */
public class ComunaDAO {

    private Connection con;

    public ComunaDAO() throws SQLException {
        this.con = ConexionDB.getInstancia().getConexion();
    }

    public boolean insertar(String nombre) {

        String sql = "{CALL pkg_comunas.pr_insertar_comuna(?)}";

        try (CallableStatement cs = con.prepareCall(sql)) {

            cs.setString(1, nombre);

            cs.execute();
            return true;

        } catch (SQLException e) {
            return false;
        }
    }

    public Comuna buscarPorId(int id) throws SQLException {
        String sql = "{? = CALL pkg_comunas.fn_consultar_comuna(?)}";

        try (CallableStatement cs = con.prepareCall(sql)) {

            cs.registerOutParameter(1, Types.VARCHAR);
            cs.setInt(2, id);
            cs.execute();

            String nombre = cs.getString(1);

            if (nombre != null) {
                Comuna c = new Comuna();
                c.setId_comuna(id);
                c.setNombre(nombre);
                return c;
            }
        }

        return null;
    }

    public List<Comuna> listar() throws SQLException {
        List<Comuna> lista = new ArrayList<>();

        String sql = "{CALL pkg_comunas.pr_listar_comunas(?)}";

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

    public void actualizar(Comuna c) throws SQLException {
        String sql = "{CALL pkg_comunas.pr_actualizar_comuna(?,?)}";

        try (CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt(1, c.getId_comuna());
            cs.setString(2, c.getNombre());
            cs.execute();
        }
    }

    public void eliminar(int id) throws SQLException {
        String sql = "{CALL pkg_comunas.pr_eliminar_comuna(?)}";

        try (CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt(1, id);
            cs.execute();
        }
    }

    private Comuna mapear(ResultSet rs) throws SQLException {
        Comuna c = new Comuna();
        c.setId_comuna(rs.getInt("ID_COMUNA"));
        c.setNombre(rs.getString("NOMBRE"));
        return c;
    }
}
