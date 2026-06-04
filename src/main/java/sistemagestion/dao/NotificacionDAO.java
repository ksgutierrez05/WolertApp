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
import sistemagestion.model.EstadoNotificacion;
import sistemagestion.model.Notificacion;
import sistemagestion.model.Usuario;

/**
 *
 * @author Lenovo
 */
public class NotificacionDAO {

    private Connection con() throws SQLException {
        return ConexionDB.getInstancia().getConexion();
    }

    public NotificacionDAO() throws SQLException {

    }

    public boolean insertar(int idAlerta, String cedulaUsuario, String mensaje) {
        String sql = "{call pkg_alertas.pr_insertar_notificacion(?, ?, ?)}";
        try (CallableStatement cs = con().prepareCall(sql)) {
            cs.setInt(1, idAlerta);
            cs.setString(2, cedulaUsuario);
            cs.setString(3, mensaje);
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.out.println("Error insertar notificacion: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminar(int idNotificacion) {
        String sql = "{call pkg_alertas.pr_eliminar_notificacion(?)}";
        try (CallableStatement cs = con().prepareCall(sql)) {
            cs.setInt(1, idNotificacion);
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.out.println("Error eliminar notificacion: " + e.getMessage());
            return false;
        }
    }

    public List<Notificacion> listar() {
        List<Notificacion> lista = new ArrayList<>();
        String sql = "{call pkg_alertas.pr_listar_notificaciones(?)}";
        try (CallableStatement cs = con().prepareCall(sql)) {
            cs.registerOutParameter(1, OracleTypes.CURSOR);
            cs.execute();
            ResultSet rs = (ResultSet) cs.getObject(1);
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error listar notificaciones: " + e.getMessage());
        }
        return lista;
    }

    public List<Notificacion> listarPorUnidad(int idUnidad) {
        List<Notificacion> lista = new ArrayList<>();

        String sql = "{call pkg_alertas.pr_listar_notificaciones_unidad(?, ?)}";

        try (CallableStatement cs = con().prepareCall(sql)) {

            cs.setInt(1, idUnidad);
            cs.registerOutParameter(2, OracleTypes.CURSOR);

            cs.execute();

            ResultSet rs = (ResultSet) cs.getObject(2);

            while (rs.next()) {
                lista.add(mapear(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }

        return lista;
    }

    // vw_notificaciones retorna
    private Notificacion mapear(ResultSet rs) throws SQLException {
        Notificacion n = new Notificacion();
        n.setId_notificacion(rs.getInt("ID_NOTIFICACION"));
        n.setMensaje(rs.getString("MENSAJE"));
        n.setFechahora(rs.getTimestamp("FECHA").toLocalDateTime());
        n.setCorreodestinatario(rs.getString("EMAIL"));

        // usuario
        Usuario u = new Usuario();
        u.setPrimer_nombre(rs.getString("NOMBRE_USUARIO"));
        u.setCorreo(rs.getString("EMAIL"));
        n.setUsuario(u);

        // alerta
        Alerta a = new Alerta();
        a.setDescripcion(rs.getString("DESCRIPCION_ALERTA"));
        n.setAlerta(a);

        // estado directo desde el enum
        n.setEstado(EstadoNotificacion.valueOf(rs.getString("ENVIADA")));

        return n;
    }
}
