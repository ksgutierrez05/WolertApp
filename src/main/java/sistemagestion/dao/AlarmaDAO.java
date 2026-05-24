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
import sistemagestion.model.Alarma;
import sistemagestion.model.Barrio;
import sistemagestion.model.Comuna;
import sistemagestion.model.EstadoAlarma;

/**
 *
 * @author Lenovo
 */
public class AlarmaDAO {

    private Connection con;

    public AlarmaDAO() throws SQLException {
        this.con = ConexionDB.getInstancia().getConexion();
    }

    public boolean insertar(
            String nombre,
            String nombreBarrio,
            double latitud,
            double longitud,
            double radioCobertura,
            String estado
    ) {
        String sql = "{call pkg_alertas.pr_insertar_alarma(?, ?, ?, ?, ?, ?)}";
        try (CallableStatement cs = con.prepareCall(sql)) {
            cs.setString(1, nombre);
            cs.setString(2, nombreBarrio);
            cs.setDouble(3, latitud);
            cs.setDouble(4, longitud);
            cs.setDouble(5, radioCobertura);
            cs.setString(6, estado);
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.out.println("Error insertar alarma: " + e.getMessage());
            return false;
        }
    }

   
    public boolean actualizar(
            int idAlarma,
            String nombre,
            String nombreBarrio,
            double latitud,
            double longitud,
            double radioCobertura,
            String estado
    ) {
        String sql = "{call pkg_alertas.pr_actualizar_alarma(?, ?, ?, ?, ?, ?, ?)}";
        try (CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt(1, idAlarma);
            cs.setString(2, nombre);
            cs.setString(3, nombreBarrio);
            cs.setDouble(4, latitud);
            cs.setDouble(5, longitud);
            cs.setDouble(6, radioCobertura);
            cs.setString(7, estado);
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.out.println("Error actualizar alarma: " + e.getMessage());
            return false;
        }
    }

   
    public boolean eliminar(int idAlarma) {
        String sql = "{call pkg_alertas.pr_eliminar_alarma(?)}";
        try (CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt(1, idAlarma);
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.out.println("Error eliminar alarma: " + e.getMessage());
            return false;
        }
    }

    
    public Alarma buscarPorId(int idAlarma) {
        String sql = "{call pkg_alertas.pr_consultar_alarma(?, ?)}";
        try (CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt(1, idAlarma);
            cs.registerOutParameter(2, OracleTypes.CURSOR);
            cs.execute();
            ResultSet rs = (ResultSet) cs.getObject(2);
            if (rs.next()) return mapear(rs);
        } catch (SQLException e) {
            System.out.println("Error buscar alarma: " + e.getMessage());
        }
        return null;
    }

    
    public List<Alarma> listar() {
        List<Alarma> lista = new ArrayList<>();
        String sql = "{call pkg_alertas.pr_listar_alarmas(?)}";
        try (CallableStatement cs = con.prepareCall(sql)) {
            cs.registerOutParameter(1, OracleTypes.CURSOR);
            cs.execute();
            ResultSet rs = (ResultSet) cs.getObject(1);
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error listar alarmas: " + e.getMessage());
        }
        return lista;
    }

    // cursor de vw_alarmas retorna:
    // ID_ALARMA, NOMBRE, BARRIO, COMUNA, LATITUD, LONGITUD, RADIO_COBERTURA, ESTADO
    private Alarma mapear(ResultSet rs) throws SQLException {
        Alarma a = new Alarma();
        a.setId_alarma(rs.getInt("ID_ALARMA"));
        a.setNombre(rs.getString("NOMBRE"));
        a.setLatitud(rs.getDouble("LATITUD"));
        a.setLongitud(rs.getDouble("LONGITUD"));
        a.setRadio_cobertura(rs.getDouble("RADIO_COBERTURA"));
        a.setEstado(EstadoAlarma.valueOf(rs.getString("ESTADO")));

        Barrio b = new Barrio();
        b.setNombre(rs.getString("BARRIO"));

        Comuna c = new Comuna();
        c.setNombre(rs.getString("COMUNA"));
        b.setComuna(c);

        a.setBarrio(b);
        return a;
    }
}
