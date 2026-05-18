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
import sistemagestion.model.Barrio;
import sistemagestion.model.Direccion;
import sistemagestion.model.EstadoAlerta;
import sistemagestion.model.MedioTransporte;
import sistemagestion.model.TipoAlerta;
import sistemagestion.model.TipoArma;
import sistemagestion.model.Usuario;

/**
 *
 * @author Lenovo
 */
public class AlertaDAO {

    private Connection con;

    public AlertaDAO() throws SQLException {
        this.con = ConexionDB.getInstancia().getConexion();
    }

    public boolean insertar(
            int idUsuario,
            int idTipoAlerta,
            int idBarrio,
            Integer idTipoArma,
            Integer idMedioTransporte,
            String estado,
            String sector,
            String manzana,
            String casa,
            String calle,
            String carrera,
            String referencia,
            String descripcion,
            LocalDateTime fechaHora
    ) {

        String sql = "{call pkg_alertas.pr_insertar_alerta(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}";

        try (CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, idUsuario);
            cs.setInt(2, idTipoAlerta);
            cs.setInt(3, idBarrio);

            if (idTipoArma != null) {
                cs.setInt(4, idTipoArma);
            } else {
                cs.setNull(4, java.sql.Types.NUMERIC);
            }

            if (idMedioTransporte != null) {
                cs.setInt(5, idMedioTransporte);
            } else {
                cs.setNull(5, java.sql.Types.NUMERIC);
            }

            cs.setString(6, estado);

            cs.setString(7, sector);
            cs.setString(8, manzana);
            cs.setString(9, casa);
            cs.setString(10, calle);
            cs.setString(11, carrera);
            cs.setString(12, referencia);

            cs.setString(13, descripcion);

            cs.setTimestamp(14, Timestamp.valueOf(fechaHora));

            cs.execute();
            return true;

        } catch (SQLException e) {
            return false;
        }
    }

    public Alerta buscarPorId(int idAlerta) {
        String sql = "{call pkg_alertas.pr_consultar_alerta(?, ?)}";
        try (CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt(1, idAlerta);
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

    public List<Alerta> listar() {
        List<Alerta> lista = new ArrayList<>();
        String sql = "{call pkg_alertas.pr_listar_alertas(?)}";
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

    public boolean actualizarEstado(int idAlerta, String estado) {
        String sql = "{call pkg_alertas.pr_actualizar_estado(?, ?)}";
        try (CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt(1, idAlerta);
            cs.setString(2, estado);
            cs.execute();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean eliminar(int idAlerta) {
        String sql = "{call pkg_alertas.pr_eliminar_alerta(?)}";
        try (CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt(1, idAlerta);
            cs.execute();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    private Alerta mapear(ResultSet rs) throws SQLException {

        Alerta a = new Alerta();

        a.setId_alerta(rs.getInt("ID_ALERTA"));
        a.setDescripcion(rs.getString("DESCRIPCION"));

        a.setUsuario(mapUsuario(rs));
        a.setTipoalerta(mapTipoAlerta(rs));
        a.setBarrio(mapBarrio(rs));
        a.setTipoarma(mapTipoArma(rs));
        a.setMediotransporte(mapMedio(rs));
        a.setEstado(mapEstado(rs));
        a.setDireccion(mapDireccion(rs));

        a.setFechaHora(
                rs.getTimestamp("FECHA").toLocalDateTime()
        );

        return a;
    }

    private Usuario mapUsuario(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId_usuario(rs.getInt("ID_USUARIO"));
        return u;
    }

    private TipoAlerta mapTipoAlerta(ResultSet rs) throws SQLException {
        TipoAlerta ta = new TipoAlerta();
        ta.setId_tipoalerta(rs.getInt("ID_TIPO_ALERTA"));
        return ta;
    }

    private Barrio mapBarrio(ResultSet rs) throws SQLException {
        Barrio b = new Barrio();
        b.setId_barrio(rs.getInt("ID_BARRIO"));
        return b;
    }

    private TipoArma mapTipoArma(ResultSet rs) throws SQLException {

        int id = rs.getInt("ID_TIPO_ARMA");
        if (rs.wasNull()) {
            return null;
        }

        TipoArma ar = new TipoArma();
        ar.setId_tipoarma(id);
        return ar;
    }

    private MedioTransporte mapMedio(ResultSet rs) throws SQLException {

        int id = rs.getInt("ID_MEDIO_TRANSPORTE");
        if (rs.wasNull()) {
            return null;
        }

        MedioTransporte m = new MedioTransporte();
        m.setId_mediotransporte(id);
        return m;
    }

    private Direccion mapDireccion(ResultSet rs) throws SQLException {

        Direccion d = new Direccion();
        d.setCalle(rs.getString("CALLE"));
        d.setCarrera(rs.getString("CARRERA"));
        d.setReferencia(rs.getString("REFERENCIA"));

        return d;
    }

    private EstadoAlerta mapEstado(ResultSet rs) throws SQLException {

        return EstadoAlerta.valueOf(rs.getString("ESTADO"));
    }

}
