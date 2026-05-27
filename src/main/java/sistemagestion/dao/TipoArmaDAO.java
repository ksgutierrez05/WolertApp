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
import sistemagestion.model.TipoArma;

/**
 *
 * @author Lenovo
 */
public class TipoArmaDAO {

    private Connection con() throws SQLException {
        return ConexionDB.getInstancia().getConexion();
    }

    public TipoArmaDAO() throws SQLException {
      
    }

    // pr_insertar_tipo_arma(nombre)
    public boolean insertar(String nombre) {
        String sql = "{call pkg_catalogos.pr_insertar_tipo_arma(?)}";
        try (CallableStatement cs = con().prepareCall(sql)) {
            cs.setString(1, nombre);
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.out.println("Error insertar tipo arma: " + e.getMessage());
            return false;
        }
    }

    // pr_actualizar_tipo_arma(nombre_actual, nombre_nuevo)
    public boolean actualizar(String nombreActual, String nombreNuevo) {
        String sql = "{call pkg_catalogos.pr_actualizar_tipo_arma(?, ?)}";
        try (CallableStatement cs = con().prepareCall(sql)) {
            cs.setString(1, nombreActual);
            cs.setString(2, nombreNuevo);
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.out.println("Error actualizar tipo arma: " + e.getMessage());
            return false;
        }
    }

    // pr_eliminar_tipo_arma(nombre)
    public boolean eliminar(String nombre) {
        String sql = "{call pkg_catalogos.pr_eliminar_tipo_arma(?)}";
        try (CallableStatement cs = con().prepareCall(sql)) {
            cs.setString(1, nombre);
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.out.println("Error eliminar tipo arma: " + e.getMessage());
            return false;
        }
    }

    // pr_listar_tipos_arma(cursor OUT) — retorna solo NOMBRE
    public List<TipoArma> listar() {
        List<TipoArma> lista = new ArrayList<>();
        String sql = "{call pkg_catalogos.pr_listar_tipos_arma(?)}";
        try (CallableStatement cs = con().prepareCall(sql)) {
            cs.registerOutParameter(1, OracleTypes.CURSOR);
            cs.execute();
            ResultSet rs = (ResultSet) cs.getObject(1);
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error listar tipos arma: " + e.getMessage());
        }
        return lista;
    }

    // pr_buscar_tipo_arma — búsqueda parcial
    public List<TipoArma> buscar(String texto) {
        List<TipoArma> lista = new ArrayList<>();
        String sql = "{call pkg_catalogos.pr_buscar_tipo_arma(?, ?)}";
        try (CallableStatement cs = con().prepareCall(sql)) {
            cs.setString(1, texto);
            cs.registerOutParameter(2, OracleTypes.CURSOR);
            cs.execute();
            ResultSet rs = (ResultSet) cs.getObject(2);
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error buscar tipo arma: " + e.getMessage());
        }
        return lista;
    }

    // pr_buscar_tipo_arma_exacto — coincidencia exacta
    public List<TipoArma> buscarExacto(String texto) {
        List<TipoArma> lista = new ArrayList<>();
        String sql = "{call pkg_catalogos.pr_buscar_tipo_arma_exacto(?, ?)}";
        try (CallableStatement cs = con().prepareCall(sql)) {
            cs.setString(1, texto);
            cs.registerOutParameter(2, OracleTypes.CURSOR);
            cs.execute();
            ResultSet rs = (ResultSet) cs.getObject(2);
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error buscar tipo arma exacto: " + e.getMessage());
        }
        return lista;
    }

    // cursor retorna solo NOMBRE — sin ID_TIPO_ARMA
    private TipoArma mapear(ResultSet rs) throws SQLException {
        TipoArma t = new TipoArma();
        t.setNombre(rs.getString("NOMBRE"));
        return t;
    }
}
