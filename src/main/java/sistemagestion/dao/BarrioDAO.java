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

    private Connection con() throws SQLException {
        return ConexionDB.getInstancia().getConexion();
    }

    public BarrioDAO() throws SQLException {
         
    }

    public boolean insertar(String nombre, String nombreComuna) {
        String sql = "{call pkg_catalogos.pr_insertar_barrio(?, ?)}";
        try (CallableStatement cs = con().prepareCall(sql)) {
            cs.setString(1, nombre);
            cs.setString(2, nombreComuna);
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.out.println("Error insertar barrio: " + e.getMessage());
            return false;
        }
    }

    public boolean actualizar(String nombreActual, String nombreNuevo) {
        String sql = "{call pkg_catalogos.pr_actualizar_barrio(?, ?)}";
        try (CallableStatement cs = con().prepareCall(sql)) {
            cs.setString(1, nombreActual);
            cs.setString(2, nombreNuevo);
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.out.println("Error actualizar barrio: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminar(String nombre) {
        String sql = "{call pkg_catalogos.pr_eliminar_barrio(?)}";
        try (CallableStatement cs = con().prepareCall(sql)) {
            cs.setString(1, nombre);
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.out.println("Error eliminar barrio: " + e.getMessage());
            return false;
        }
    }

    public List<Barrio> listar() {
        List<Barrio> lista = new ArrayList<>();
        String sql = "{call pkg_catalogos.pr_listar_barrios(?)}";
        try (CallableStatement cs = con().prepareCall(sql)) {
            cs.registerOutParameter(1, OracleTypes.CURSOR);
            cs.execute();
            ResultSet rs = (ResultSet) cs.getObject(1);
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error listar barrios: " + e.getMessage());
        }
        return lista;
    }

    public List<Barrio> listarPorComuna(String nombreComuna) {
        List<Barrio> lista = new ArrayList<>();
        String sql = "{call pkg_catalogos.pr_buscar_barrios_por_comuna(?, ?)}";
        try (CallableStatement cs = con().prepareCall(sql)) {
            cs.setString(1, nombreComuna);
            cs.registerOutParameter(2, OracleTypes.CURSOR);
            cs.execute();
            ResultSet rs = (ResultSet) cs.getObject(2);
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error listar barrios por comuna: " + e.getMessage());
        }
        return lista;
    }

    public List<Barrio> buscar(String texto) {
        List<Barrio> lista = new ArrayList<>();
        String sql = "{call pkg_catalogos.pr_buscar_barrio(?, ?)}";
        try (CallableStatement cs = con().prepareCall(sql)) {
            cs.setString(1, texto);
            cs.registerOutParameter(2, OracleTypes.CURSOR);
            cs.execute();
            ResultSet rs = (ResultSet) cs.getObject(2);
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error buscar barrio: " + e.getMessage());
        }
        return lista;
    }

    public List<Barrio> buscarExacto(String texto) {
        List<Barrio> lista = new ArrayList<>();
        String sql = "{call pkg_catalogos.pr_buscar_barrio_exacto(?, ?)}";
        try (CallableStatement cs = con().prepareCall(sql)) {
            cs.setString(1, texto);
            cs.registerOutParameter(2, OracleTypes.CURSOR);
            cs.execute();
            ResultSet rs = (ResultSet) cs.getObject(2);
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error buscar barrio exacto: " + e.getMessage());
        }
        return lista;
    }

    private Barrio mapear(ResultSet rs) throws SQLException {
        Barrio b = new Barrio();
        b.setNombre(rs.getString("BARRIO"));
        Comuna c = new Comuna();
        c.setNombre(rs.getString("COMUNA"));
        b.setComuna(c);
        return b;
    }
}
