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
import sistemagestion.model.TipoAlerta;

/**
 *
 * @author Lenovo
 */
public class TipoAlertaDAO {

    private Connection con() throws SQLException {
        return ConexionDB.getInstancia().getConexion();
    }

    public TipoAlertaDAO() throws SQLException {
         
    }

    public boolean insertar(String nombre) {
        String sql = "{call pkg_catalogos.pr_insertar_tipo_alerta(?)}";
        try (CallableStatement cs = con().prepareCall(sql)) {
            cs.setString(1, nombre);
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.out.println("Error insertar tipo alerta: " + e.getMessage());
            return false;
        }
    }

    public boolean actualizar(String nombreActual, String nombreNuevo) {
        String sql = "{call pkg_catalogos.pr_actualizar_tipo_alerta(?, ?)}";
        try (CallableStatement cs = con().prepareCall(sql)) {
            cs.setString(1, nombreActual);
            cs.setString(2, nombreNuevo);
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.out.println("Error actualizar tipo alerta: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminar(String nombre) {
        String sql = "{call pkg_catalogos.pr_eliminar_tipo_alerta(?)}";
        try (CallableStatement cs = con().prepareCall(sql)) {
            cs.setString(1, nombre);
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.out.println("Error eliminar tipo alerta: " + e.getMessage());
            return false;
        }
    }

    
    public List<TipoAlerta> listar() {
        List<TipoAlerta> lista = new ArrayList<>();
        String sql = "{call pkg_catalogos.pr_listar_tipos_alerta(?)}";
        try (CallableStatement cs = con().prepareCall(sql)) {
            cs.registerOutParameter(1, OracleTypes.CURSOR);
            cs.execute();
            ResultSet rs = (ResultSet) cs.getObject(1);
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error listar tipos alerta: " + e.getMessage());
        }
        return lista;
    }

  
    public List<TipoAlerta> buscar(String texto) {
        List<TipoAlerta> lista = new ArrayList<>();
        String sql = "{call pkg_catalogos.pr_buscar_tipo_alerta(?, ?)}";
        try (CallableStatement cs = con().prepareCall(sql)) {
            cs.setString(1, texto);
            cs.registerOutParameter(2, OracleTypes.CURSOR);
            cs.execute();
            ResultSet rs = (ResultSet) cs.getObject(2);
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error buscar tipo alerta: " + e.getMessage());
        }
        return lista;
    }

   
    public List<TipoAlerta> buscarExacto(String texto) {
        List<TipoAlerta> lista = new ArrayList<>();
        String sql = "{call pkg_catalogos.pr_buscar_tipo_alerta_exacto(?, ?)}";
        try (CallableStatement cs = con().prepareCall(sql)) {
            cs.setString(1, texto);
            cs.registerOutParameter(2, OracleTypes.CURSOR);
            cs.execute();
            ResultSet rs = (ResultSet) cs.getObject(2);
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error buscar tipo alerta exacto: " + e.getMessage());
        }
        return lista;
    }

    
    private TipoAlerta mapear(ResultSet rs) throws SQLException {
        TipoAlerta t = new TipoAlerta();
        t.setId_tipoalerta(rs.getInt("ID_TIPO_ALERTA"));
        t.setNombre(rs.getString("NOMBRE"));
        return t;
    }
}