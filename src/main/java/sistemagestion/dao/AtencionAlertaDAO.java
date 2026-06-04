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
import sistemagestion.model.Alerta;
import sistemagestion.model.AtencionAlerta;
import sistemagestion.model.EstadoAlerta;
import sistemagestion.model.EstadoAtencionAlerta;
import sistemagestion.model.MedioTransporte;
import sistemagestion.model.Policia;
import sistemagestion.model.TipoArma;
import sistemagestion.model.UnidadPolicial;

/**
 *
 * @author Lenovo
 */
public class AtencionAlertaDAO {

    private Connection con() throws SQLException {
        return ConexionDB.getInstancia().getConexion();
    }

    public AtencionAlertaDAO() throws SQLException {

    }

    public boolean insertar(
            int idAlerta,
            String nombreUnidad,
            String estadoFinal,
            String descripcion,
            String tipoArma,
            String medioTransporte,
            String observacion,
            int idPolicia
    ) {
        String sql = "{call pkg_alertas.pr_insertar_atencion(?, ?, ?, ?, ?, ?, ?, ?)}"; // ← 8 parámetros
        try (CallableStatement cs = con().prepareCall(sql)) {
            cs.setInt(1, idAlerta);
            cs.setString(2, nombreUnidad);
            cs.setString(3, estadoFinal);
            cs.setString(4, descripcion);
            cs.setString(5, tipoArma);
            cs.setString(6, medioTransporte);
            cs.setString(7, observacion);
            if (idPolicia > 0) {
                cs.setInt(8, idPolicia);
            } else {
                cs.setNull(8, java.sql.Types.INTEGER);
            }
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.out.println("=== ERROR DAO ===");
            System.out.println("Mensaje: " + e.getMessage());
            System.out.println("Codigo: " + e.getErrorCode());
            e.printStackTrace();

            return false;

        }
    }

    public boolean actualizar(
            int idAtencion,
            String estadoFinal,
            String descripcion,
            String tipoArma,
            String medioTransporte,
            String observacion
    ) {
        String sql = "{call pkg_alertas.pr_actualizar_atencion(?, ?, ?, ?, ?, ?)}";
        try (CallableStatement cs = con().prepareCall(sql)) {
            cs.setInt(1, idAtencion);
            cs.setString(2, estadoFinal);
            cs.setString(3, descripcion);
            cs.setString(4, tipoArma);
            cs.setString(5, medioTransporte);
            cs.setString(6, observacion);
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.out.println("Error actualizar atencion: " + e.getMessage());
            return false;
        }
    }

    // pr_eliminar_atencion
    public boolean eliminar(int idAtencion) {
        String sql = "{call pkg_alertas.pr_eliminar_atencion(?)}";
        try (CallableStatement cs = con().prepareCall(sql)) {
            cs.setInt(1, idAtencion);
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.out.println("Error eliminar atencion: " + e.getMessage());
            return false;
        }
    }

    // pr_consultar_atencion
    public AtencionAlerta buscarPorId(int idAtencion) {
        String sql = "{call pkg_alertas.pr_consultar_atencion(?, ?)}";
        try (CallableStatement cs = con().prepareCall(sql)) {
            cs.setInt(1, idAtencion);
            cs.registerOutParameter(2, OracleTypes.CURSOR);
            cs.execute();
            ResultSet rs = (ResultSet) cs.getObject(2);
            if (rs.next()) {
                return mapear(rs);
            }
        } catch (SQLException e) {
            System.out.println("Error buscar atencion: " + e.getMessage());
        }
        return null;
    }

    public List<AtencionAlerta> listar() {
        List<AtencionAlerta> lista = new ArrayList<>();
        String sql = "{call pkg_alertas.pr_listar_atenciones(?)}";
        try (CallableStatement cs = con().prepareCall(sql)) {
            cs.registerOutParameter(1, OracleTypes.CURSOR);
            cs.execute();
            ResultSet rs = (ResultSet) cs.getObject(1);
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error listar atenciones: " + e.getMessage());
        }
        return lista;
    }

    public List<AtencionAlerta> listarPorAlerta(int idAlerta) {
        List<AtencionAlerta> lista = new ArrayList<>();
        String sql = "{call pkg_alertas.pr_listar_atenciones_por_alerta(?, ?)}";
        try (CallableStatement cs = con().prepareCall(sql)) {
            cs.setInt(1, idAlerta);
            cs.registerOutParameter(2, OracleTypes.CURSOR);
            cs.execute();
            ResultSet rs = (ResultSet) cs.getObject(2);
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error listar atenciones por alerta: " + e.getMessage());
        }
        return lista;
    }

    // pr_buscar_atencion_por_estado 
    public List<AtencionAlerta> buscarPorEstado(String estado) {
        List<AtencionAlerta> lista = new ArrayList<>();
        String sql = "{call pkg_alertas.pr_buscar_atencion_por_estado(?, ?)}";
        try (CallableStatement cs = con().prepareCall(sql)) {
            cs.setString(1, estado);
            cs.registerOutParameter(2, OracleTypes.CURSOR);
            cs.execute();
            ResultSet rs = (ResultSet) cs.getObject(2);
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error buscar atencion por estado: " + e.getMessage());
        }
        return lista;
    }

    // pr_buscar_atencion_por_estado_exacto 
    public List<AtencionAlerta> buscarPorEstadoExacto(String estado) {
        List<AtencionAlerta> lista = new ArrayList<>();
        String sql = "{call pkg_alertas.pr_buscar_atencion_por_estado_exacto(?, ?)}";
        try (CallableStatement cs = con().prepareCall(sql)) {
            cs.setString(1, estado);
            cs.registerOutParameter(2, OracleTypes.CURSOR);
            cs.execute();
            ResultSet rs = (ResultSet) cs.getObject(2);
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error buscar atencion exacto: " + e.getMessage());
        }
        return lista;
    }

    // cursor de vw_atenciones_completa retorna:
    // ID_ATENCION, FECHA_ATENCION, ESTADO_FINAL, DESCRIPCION, OBSERVACION,
    // ID_ALERTA, ESTADO_ALERTA, DESCRIPCION_ALERTA, FECHA_ALERTA,
    // NOMBRE_UNIDAD, ESTADO_UNIDAD, BARRIO_UNIDAD, TIPO_ARMA, MEDIO_TRANSPORTE
    private AtencionAlerta mapear(ResultSet rs) throws SQLException {
        AtencionAlerta a = new AtencionAlerta();
        a.setId_atencion(rs.getInt("ID_ATENCION"));
        a.setFechaatencion(rs.getTimestamp("FECHA_ATENCION").toLocalDateTime());
        a.setEstado(EstadoAtencionAlerta.valueOf(rs.getString("ESTADO_FINAL")));
        a.setDescripcion(rs.getString("DESCRIPCION"));
        a.setObservacion(rs.getString("OBSERVACION"));

        // alerta asociada
        Alerta al = new Alerta();
        al.setId_alerta(rs.getInt("ID_ALERTA"));
        al.setEstado(EstadoAlerta.valueOf(rs.getString("ESTADO_ALERTA")));
        a.setAlerta(al);

        // unidad que atendió
        UnidadPolicial u = new UnidadPolicial();
        u.setNombre(rs.getString("NOMBRE_UNIDAD"));
        a.setUnidad(u);

        String nombrePolicia = rs.getString("NOMBRE_POLICIA");
        if (nombrePolicia != null) {
            Policia p = new Policia();
            String[] partes = nombrePolicia.split(" ", 2);
            p.setPrimer_nombre(partes[0]);
            p.setPrimer_apellido(partes.length > 1 ? partes[1] : "");
            List<Policia> policias = new ArrayList<>();
            policias.add(p);
            u.setPolicias(policias);
        }
        a.setUnidad(u);

        // opcionales 
        String tipoArma = rs.getString("TIPO_ARMA");
        if (tipoArma != null) {
            TipoArma t = new TipoArma();
            t.setNombre(tipoArma);
            a.setTipoarma(t);
        }
        String medio = rs.getString("MEDIO_TRANSPORTE");
        if (medio != null) {
            MedioTransporte m = new MedioTransporte();
            m.setNombre(medio);
            a.setMediotransporte(m);
        }

        return a;
    }
}