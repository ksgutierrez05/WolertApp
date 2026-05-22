
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
import sistemagestion.model.RolUsuario;

/**
 *
 * @author Lenovo
 */
public class RolUsuarioDAO {
    
    private Connection con;

    public boolean insertar(int idRol, String nombre) {

        String sql = "{call pkg_roles.prc_insertar_rol(?, ?)}";

        try (CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, idRol);
            cs.setString(2, nombre);

            cs.execute();
            return true;

        } catch (SQLException e) {
            return false;
        }
    }

    public RolUsuario buscarPorId(int idRol) {

        String sql = "{call pkg_roles.prc_consultar_rol(?)}";

        try (CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, idRol);
            cs.registerOutParameter(2, OracleTypes.CURSOR);

            cs.execute();

            ResultSet rs = (ResultSet) cs.getObject(2);

            if (rs.next()) {
                return mapear(rs);
            }

        } catch (SQLException e) {
            return null;
        }

        return null;
    }

    public List<RolUsuario> listar() {

        List<RolUsuario> lista = new ArrayList<>();

        String sql = "{call pkg_roles.prc_listar_roles(?)}";

        try (CallableStatement cs = con.prepareCall(sql)) {

            cs.registerOutParameter(1, OracleTypes.CURSOR);
            cs.execute();

            ResultSet rs = (ResultSet) cs.getObject(1);

            while (rs.next()) {
                lista.add(mapear(rs));
            }

        } catch (SQLException e) {
            return lista;
        }

        return lista;
    }

    public boolean actualizar(int idRol, String nombre) {

        String sql = "{call pkg_roles.prc_actualizar_rol(?, ?)}";

        try (CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, idRol);
            cs.setString(2, nombre);

            cs.execute();
            return true;

        } catch (SQLException e) {
            return false;
        }
    }

    public boolean eliminar(int idRol) {

        String sql = "{call pkg_roles.prc_eliminar_rol(?)}";

        try (CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, idRol);

            cs.execute();
            return true;

        } catch (SQLException e) {
            return false;
        }
    }

    private RolUsuario mapear(ResultSet rs) throws SQLException {

        RolUsuario r = new RolUsuario();

        r.setIdRol(rs.getInt("ID_ROL"));
        r.setNombre(rs.getString("NOMBRE"));

        return r;
    }
}

