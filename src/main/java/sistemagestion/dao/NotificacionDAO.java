/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import sistemagestion.model.Alerta;
import sistemagestion.model.EstadoNotificacion;
import sistemagestion.model.Notificacion;
import sistemagestion.model.Policia;
import sistemagestion.model.Usuario;

/**
 *
 * @author Lenovo
 */
public class NotificacionDAO {

private Connection con;

    public NotificacionDAO() throws SQLException {
        this.con = ConexionDB.getInstancia().getConexion();
    }

    public boolean insertar(
            int idAlerta,
            int idUsuario,
            String mensaje
    ) {

        String sql = "{call pkg_notificaciones.pr_insertar_notificacion(?, ?, ?)}";

        try (CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, idAlerta);
            cs.setInt(2, idUsuario);
            cs.setString(3, mensaje);

            cs.execute();
            return true;

        } catch (SQLException e) {
            return false;
        }
    }

    public Notificacion consultar(int idNotificacion) {

        String sql = "{call pkg_notificaciones.pr_consultar_notificacion(?)}";

        try (CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, idNotificacion);
            cs.registerOutParameter(2, oracle.jdbc.OracleTypes.CURSOR);

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

    public boolean eliminar(int idNotificacion) {

        String sql = "{call pkg_notificaciones.pr_eliminar_notificacion(?)}";

        try (CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, idNotificacion);
            cs.execute();

            return true;

        } catch (SQLException e) {
            return false;
        }
    }

    private Notificacion mapear(ResultSet rs) throws SQLException {

        Notificacion n = new Notificacion();

        n.setId_notificacion(rs.getInt("ID_NOTIFICACION"));
        n.setMensaje(rs.getString("MENSAJE"));
        n.setCorreodestinatario(rs.getString("CORREO_DESTINATARIO"));

        n.setFechahora(rs.getTimestamp("FECHA").toLocalDateTime());

        n.setUsuario(mapUsuario(rs));
        n.setAlerta(mapAlerta(rs));
        n.setPolicia(mapPolicia(rs));
        n.setEstado(mapEstado(rs));

        return n;
    }

    private Usuario mapUsuario(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId_usuario(rs.getInt("ID_USUARIO"));
        return u;
    }

    private Alerta mapAlerta(ResultSet rs) throws SQLException {
        Alerta a = new Alerta();
        a.setId_alerta(rs.getInt("ID_ALERTA"));
        return a;
    }

    private Policia mapPolicia(ResultSet rs) throws SQLException {
        Policia p = new Policia();
        p.setId_policia(rs.getInt("ID_POLICIA"));
        return p;
    }

    private EstadoNotificacion mapEstado(ResultSet rs) throws SQLException {
        return EstadoNotificacion.valueOf(rs.getString("ESTADO"));
    }
}