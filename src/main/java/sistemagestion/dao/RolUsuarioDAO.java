
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

    public RolUsuarioDAO() throws SQLException {
        this.con = ConexionDB.getInstancia().getConexion();
    }

    public boolean insertar(String nombre) {
        String sql = "{call pkg_usuarios.pr_insertar_rol(?)}";
        try (CallableStatement cs = con.prepareCall(sql)) {
            cs.setString(1, nombre);
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.out.println("Error insertar rol: " + e.getMessage());
            return false;
        }
    }

    public boolean actualizar(String nombreActual, String nombreNuevo) {
        String sql = "{call pkg_usuarios.pr_actualizar_rol(?, ?)}";
        try (CallableStatement cs = con.prepareCall(sql)) {
            cs.setString(1, nombreActual);
            cs.setString(2, nombreNuevo);
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.out.println("Error actualizar rol: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminar(String nombre) {
        String sql = "{call pkg_usuarios.pr_eliminar_rol(?)}";
        try (CallableStatement cs = con.prepareCall(sql)) {
            cs.setString(1, nombre);
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.out.println("Error eliminar rol: " + e.getMessage());
            return false;
        }
    }

    public List<RolUsuario> listar() {
        List<RolUsuario> lista = new ArrayList<>();
        String sql = "{call pkg_usuarios.pr_listar_roles(?)}";
        try (CallableStatement cs = con.prepareCall(sql)) {
            cs.registerOutParameter(1, OracleTypes.CURSOR);
            cs.execute();
            ResultSet rs = (ResultSet) cs.getObject(1);
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error listar roles: " + e.getMessage());
        }
        return lista;
    }

    public List<RolUsuario> buscar(String texto) {
        List<RolUsuario> lista = new ArrayList<>();
        String sql = "{call pkg_usuarios.pr_buscar_rol(?, ?)}";
        try (CallableStatement cs = con.prepareCall(sql)) {
            cs.setString(1, texto);
            cs.registerOutParameter(2, OracleTypes.CURSOR);
            cs.execute();
            ResultSet rs = (ResultSet) cs.getObject(2);
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error buscar rol: " + e.getMessage());
        }
        return lista;
    }

    public List<RolUsuario> buscarExacto(String texto) {
        List<RolUsuario> lista = new ArrayList<>();
        String sql = "{call pkg_usuarios.pr_buscar_rol_exacto(?, ?)}";
        try (CallableStatement cs = con.prepareCall(sql)) {
            cs.setString(1, texto);
            cs.registerOutParameter(2, OracleTypes.CURSOR);
            cs.execute();
            ResultSet rs = (ResultSet) cs.getObject(2);
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error buscar rol exacto: " + e.getMessage());
        }
        return lista;
    }

    private RolUsuario mapear(ResultSet rs) throws SQLException {
        RolUsuario r = new RolUsuario();
        r.setNombre(rs.getString("NOMBRE"));
        return r;
    }
}
