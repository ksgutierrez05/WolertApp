/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import oracle.jdbc.OracleTypes;
import sistemagestion.model.Alerta;
import sistemagestion.model.AsignacionUnidad;
import sistemagestion.model.UnidadPolicial;

/**
 *
 * @author Lenovo
 */
public class AsignacionUnidadDAO {

    private Connection con() throws SQLException {
        return ConexionDB.getInstancia().getConexion();
    }

    public AsignacionUnidadDAO() throws SQLException {

    }

    public boolean insertar(
            int idAlerta,
            String nombreUnidad,
            String observacion,
            LocalDateTime fechaHora
    ) {
        String sql = "{call pkg_alertas.pr_insertar_asignacion(?, ?, ?, ?)}";
        try (CallableStatement cs = con().prepareCall(sql)) {
            cs.setInt(1, idAlerta);
            cs.setString(2, nombreUnidad);
            cs.setString(3, observacion);
            cs.setTimestamp(4, Timestamp.valueOf(fechaHora));
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.out.println("Error insertar asignacion: " + e.getMessage());
            return false;
        }
    }

    public boolean actualizar(
            int idAsignacion,
            int idAlerta,
            String nombreUnidad,
            String observacion,
            LocalDateTime fechaHora
    ) {
        String sql = "{call pkg_alertas.pr_actualizar_asignacion(?, ?, ?, ?, ?)}";
        try (CallableStatement cs = con().prepareCall(sql)) {
            cs.setInt(1, idAsignacion);
            cs.setInt(2, idAlerta);
            cs.setString(3, nombreUnidad);
            cs.setString(4, observacion);
            cs.setTimestamp(5, Timestamp.valueOf(fechaHora));
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.out.println("Error actualizar asignacion: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminar(int idAsignacion) {
        String sql = "{call pkg_alertas.pr_eliminar_asignacion(?)}";
        try (CallableStatement cs = con().prepareCall(sql)) {
            cs.setInt(1, idAsignacion);
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.out.println("Error eliminar asignacion: " + e.getMessage());
            return false;
        }
    }

    public List<AsignacionUnidad> listar() {
        List<AsignacionUnidad> lista = new ArrayList<>();
        String sql = "{call pkg_alertas.pr_listar_asignaciones(?)}";
        try (CallableStatement cs = con().prepareCall(sql)) {
            cs.registerOutParameter(1, OracleTypes.CURSOR);
            cs.execute();
            ResultSet rs = (ResultSet) cs.getObject(1);
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error listar asignaciones: " + e.getMessage());
        }
        return lista;
    }

    public boolean asignarUnidadCercana(int idAlerta) {
        String sql = "{CALL pr_asignar_unidad_cercana(?)}";
        try (CallableStatement cs = con().prepareCall(sql)) {
            cs.setInt(1, idAlerta);
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al ejecutar procedure: " + e.getMessage());
            return false;
        }
    }

    // vw_asignaciones retorna
    private AsignacionUnidad mapear(ResultSet rs) throws SQLException {
        AsignacionUnidad a = new AsignacionUnidad();
        a.setId_asignacion(rs.getInt("ID_ASIGNACION"));
        a.setObservacion(rs.getString("OBSERVACION"));
        a.setFechahoraasignacion(
                rs.getTimestamp("FECHA").toLocalDateTime()
        );

        // alerta — la vista retorna estado y descripcion
        Alerta al = new Alerta();
        al.setDescripcion(rs.getString("DESCRIPCION_ALERTA"));
        a.setAlerta(al);

        // unidad — la vista retorna nombre
        UnidadPolicial u = new UnidadPolicial();
        u.setNombre(rs.getString("NOMBRE_UNIDAD"));
        a.setUnidadpolicial(u);

        return a;
    }
}
