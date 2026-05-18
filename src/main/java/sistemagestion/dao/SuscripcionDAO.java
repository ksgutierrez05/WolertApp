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
import sistemagestion.model.EstadoSuscripcion;
import sistemagestion.model.Suscripcion;
import sistemagestion.model.TipoAlerta;
import sistemagestion.model.Usuario;

/**
 *
 * @author Lenovo
 */
public class SuscripcionDAO {

    private Connection con;

    public SuscripcionDAO() throws SQLException {
        this.con = ConexionDB.getInstancia().getConexion();
    }

    public boolean insertar(Suscripcion s) {

        String sql = "{call pkg_suscripciones.pr_insertar_suscripcion(?, ?, ?, ?, ?)}";

        try (CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, s.getUsuario().getId_usuario());
            cs.setInt(2, s.getTipoalerta().getId_tipoalerta());

            if (s.getComuna() != null) {
                cs.setInt(3, s.getComuna().getId_comuna());
            } else {
                cs.setNull(3, java.sql.Types.NUMERIC);
            }

            if (s.getBarrio() != null) {
                cs.setInt(4, s.getBarrio().getId_barrio());
            } else {
                cs.setNull(4, java.sql.Types.NUMERIC);
            }

            cs.setString(5, s.getEstado().name());

            cs.execute();
            return true;

        } catch (SQLException e) {
            return false;
        }
    }

    public List<Suscripcion> listar() {

        List<Suscripcion> lista = new ArrayList<>();
        String sql = "{call pkg_suscripciones.pr_listar_suscripciones(?)}";

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

    private Suscripcion mapear(ResultSet rs) throws SQLException {

        Suscripcion s = new Suscripcion();

        s.setId_suscripcion(rs.getInt("ID_SUSCRIPCION"));
        s.setUsuario(mapUsuario(rs));
        s.setTipoalerta(mapTipoAlerta(rs));
        s.setComuna(mapComuna(rs));
        s.setBarrio(mapBarrio(rs));
        s.setEstado(mapEstado(rs));

        return s;
    }

    private Usuario mapUsuario(ResultSet rs) throws SQLException {

        Usuario u = new Usuario();
        u.setId_usuario(rs.getInt("ID_USUARIO"));

        return u;
    }

    private TipoAlerta mapTipoAlerta(ResultSet rs) throws SQLException {

        TipoAlerta t = new TipoAlerta();
        t.setId_tipoalerta(rs.getInt("ID_TIPO_ALERTA"));

        return t;
    }

    private Comuna mapComuna(ResultSet rs) throws SQLException {

        int id = rs.getInt("ID_COMUNA");

        if (rs.wasNull()) {
            return null;
        }

        Comuna c = new Comuna();
        c.setId_comuna(id);

        return c;
    }

    private Barrio mapBarrio(ResultSet rs) throws SQLException {

        int id = rs.getInt("ID_BARRIO");

        if (rs.wasNull()) {
            return null;
        }

        Barrio b = new Barrio();
        b.setId_barrio(id);

        return b;
    }

    private EstadoSuscripcion mapEstado(ResultSet rs) throws SQLException {

        return EstadoSuscripcion.valueOf(rs.getString("ESTADO"));
    }
}
