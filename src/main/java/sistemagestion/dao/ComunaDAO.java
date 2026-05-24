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
import sistemagestion.model.Comuna;

/**
 *
 * @author Lenovo
 */
public class ComunaDAO {

    private Connection con() throws SQLException {
        return ConexionDB.getInstancia().getConexion();
    }

    public ComunaDAO() throws SQLException {
        
    }

  
    public boolean insertar(String nombre) {
        String sql = "{call pkg_catalogos.pr_insertar_comuna(?)}";
        try (CallableStatement cs = con().prepareCall(sql)) {
            cs.setString(1, nombre);
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.out.println("Error insertar comuna: " + e.getMessage());
            return false;
        }
    }

    public boolean actualizar(String nombreActual, String nombreNuevo) {
        String sql = "{call pkg_catalogos.pr_actualizar_comuna(?, ?)}";
        try (CallableStatement cs = con().prepareCall(sql)) {
            cs.setString(1, nombreActual);
            cs.setString(2, nombreNuevo);
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.out.println("Error actualizar comuna: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminar(String nombre) {
        String sql = "{call pkg_catalogos.pr_eliminar_comuna(?)}";
        try (CallableStatement cs = con().prepareCall(sql)) {
            cs.setString(1, nombre);
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.out.println("Error eliminar comuna: " + e.getMessage());
            return false;
        }
    }

    public List<Comuna> listar() {
        List<Comuna> lista = new ArrayList<>();
        String sql = "{call pkg_catalogos.pr_listar_comunas(?)}";
        try (CallableStatement cs = con().prepareCall(sql)) {
            cs.registerOutParameter(1, OracleTypes.CURSOR);
            cs.execute();
            ResultSet rs = (ResultSet) cs.getObject(1);
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error listar comunas: " + e.getMessage());
        }
        return lista;
    }

    public List<Comuna> buscar(String texto) {
        List<Comuna> lista = new ArrayList<>();
        String sql = "{call pkg_catalogos.pr_buscar_comuna(?, ?)}";
        try (CallableStatement cs = con().prepareCall(sql)) {
            cs.setString(1, texto);
            cs.registerOutParameter(2, OracleTypes.CURSOR);
            cs.execute();
            ResultSet rs = (ResultSet) cs.getObject(2);
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error buscar comuna: " + e.getMessage());
        }
        return lista;
    }

    public List<Comuna> buscarExacto(String texto) {
        List<Comuna> lista = new ArrayList<>();
        String sql = "{call pkg_catalogos.pr_buscar_comuna_exacto(?, ?)}";
        try (CallableStatement cs = con().prepareCall(sql)) {
            cs.setString(1, texto);
            cs.registerOutParameter(2, OracleTypes.CURSOR);
            cs.execute();
            ResultSet rs = (ResultSet) cs.getObject(2);
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error buscar comuna exacto: " + e.getMessage());
        }
        return lista;
    }

    private Comuna mapear(ResultSet rs) throws SQLException {
        Comuna c = new Comuna();
        c.setNombre(rs.getString("NOMBRE"));
        return c;
    }
}