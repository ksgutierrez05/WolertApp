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
        private Connection con;

    public AsignacionUnidadDAO() throws SQLException {
        this.con = ConexionDB.getInstancia().getConexion();
    }

    public boolean insertar(AsignacionUnidad a) {
        String sql = "{call pkg_asignaciones_unidad.pr_insertar_asignacion(?, ?, ?, ?)}";
        try (CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt(1, a.getAlerta().getId_alerta());
            cs.setInt(2, a.getUnidadpolicial().getId_unidad());
            cs.setString(3, a.getObservacion());
            cs.setTimestamp(4, Timestamp.valueOf(a.getFechahoraasignacion()));
            cs.execute();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public AsignacionUnidad buscarPorId(int idAsignacion) {
        String sql = "{call pkg_asignaciones_unidad.pr_consultar_asignacion(?, ?)}";
        try (CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt(1, idAsignacion);
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

    public List<AsignacionUnidad> listar() {
        List<AsignacionUnidad> lista = new ArrayList<>();
        String sql = "{call pkg_asignaciones_unidad.pr_listar_asignaciones(?)}";
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

    public boolean actualizar(AsignacionUnidad a) {
        String sql = "{call pkg_asignaciones_unidad.pr_actualizar_asignacion(?, ?, ?, ?, ?)}";
        try (CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt(1, a.getId_asignacion());
            cs.setInt(2, a.getAlerta().getId_alerta());
            cs.setInt(3, a.getUnidadpolicial().getId_unidad());
            cs.setString(4, a.getObservacion());
            cs.setTimestamp(5, Timestamp.valueOf(a.getFechahoraasignacion()));
            cs.execute();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean eliminar(int idAsignacion) {
        String sql = "{call pkg_asignaciones_unidad.pr_eliminar_asignacion(?)}";
        try (CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt(1, idAsignacion);
            cs.execute();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    private AsignacionUnidad mapear(ResultSet rs) throws SQLException {

        AsignacionUnidad a = new AsignacionUnidad();

        a.setId_asignacion(rs.getInt("ID_ASIGNACION"));

        UnidadPolicial u = new UnidadPolicial();
        u.setId_unidad(rs.getInt("ID_UNIDAD"));
        u.setNombre(rs.getString("NOMBRE_UNIDAD"));
        a.setUnidadpolicial(u);

    
        Alerta al = new Alerta();
        al.setId_alerta(rs.getInt("ID_ALERTA"));
        a.setAlerta(al);
        a.setObservacion(rs.getString("OBSERVACION"));
        a.setFechahoraasignacion(
            rs.getTimestamp("FECHAHORAASIGNACION").toLocalDateTime()
        );

        return a;
    }
}
