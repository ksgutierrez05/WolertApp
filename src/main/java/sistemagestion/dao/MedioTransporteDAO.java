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
import sistemagestion.dao.ConexionDB;
import sistemagestion.model.MedioTransporte;

/**
 *
 * @author Lenovo
 */
public class MedioTransporteDAO {

    private Connection con() throws SQLException {
        return ConexionDB.getInstancia().getConexion();
    }

    public MedioTransporteDAO() throws SQLException {
       
    }

    public boolean insertar(String nombre) {
        String sql = "{call pkg_catalogos.pr_insertar_medio(?)}";
        try (CallableStatement cs = con().prepareCall(sql)) {
            cs.setString(1, nombre);
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.out.println("Error insertar medio transporte: " + e.getMessage());
            return false;
        }
    }

    public boolean actualizar(String nombreActual, String nombreNuevo) {
        String sql = "{call pkg_catalogos.pr_actualizar_medio(?, ?)}";
        try (CallableStatement cs = con().prepareCall(sql)) {
            cs.setString(1, nombreActual);
            cs.setString(2, nombreNuevo);
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.out.println("Error actualizar medio transporte: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminar(String nombre) {
        String sql = "{call pkg_catalogos.pr_eliminar_medio(?)}";
        try (CallableStatement cs = con().prepareCall(sql)) {
            cs.setString(1, nombre);
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.out.println("Error eliminar medio transporte: " + e.getMessage());
            return false;
        }
    }

    public List<MedioTransporte> listar() {
        List<MedioTransporte> lista = new ArrayList<>();
        String sql = "{call pkg_catalogos.pr_listar_medios(?)}";
        try (CallableStatement cs = con().prepareCall(sql)) {
            cs.registerOutParameter(1, OracleTypes.CURSOR);
            cs.execute();
            ResultSet rs = (ResultSet) cs.getObject(1);
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error listar medios transporte: " + e.getMessage());
        }
        return lista;
    }

    public List<MedioTransporte> buscar(String texto) {
        List<MedioTransporte> lista = new ArrayList<>();
        String sql = "{call pkg_catalogos.pr_buscar_medio(?, ?)}";
        try (CallableStatement cs = con().prepareCall(sql)) {
            cs.setString(1, texto);
            cs.registerOutParameter(2, OracleTypes.CURSOR);
            cs.execute();
            ResultSet rs = (ResultSet) cs.getObject(2);
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error buscar medio transporte: " + e.getMessage());
        }
        return lista;
    }

    public List<MedioTransporte> buscarExacto(String texto) {
        List<MedioTransporte> lista = new ArrayList<>();
        String sql = "{call pkg_catalogos.pr_buscar_medio_exacto(?, ?)}";
        try (CallableStatement cs = con().prepareCall(sql)) {
            cs.setString(1, texto);
            cs.registerOutParameter(2, OracleTypes.CURSOR);
            cs.execute();
            ResultSet rs = (ResultSet) cs.getObject(2);
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error buscar medio transporte exacto: " + e.getMessage());
        }
        return lista;
    }
    
    private MedioTransporte mapear(ResultSet rs) throws SQLException {
        MedioTransporte m = new MedioTransporte();
        m.setNombre(rs.getString("NOMBRE"));
        return m;
    }
}
