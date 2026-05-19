/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import oracle.jdbc.OracleTypes;
import sistemagestion.model.EstadoPolicia;
import sistemagestion.model.EstadoUsuario;
import sistemagestion.model.Policia;
import sistemagestion.model.UnidadPolicial;

/**
 *
 * @author Lenovo
 */
public class PoliciaDAO {

    private Connection con;

    public PoliciaDAO() throws SQLException {
        this.con = ConexionDB.getInstancia().getConexion();
    }

    public boolean insertar(int idUsuario, int idUnidad, String placa, String rango, String estado) {

        String sql = "{call pkg_policias.pr_insertar_policia(?, ?, ?, ?, ?)}";

        try (CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, idUsuario);
            cs.setInt(2, idUnidad);
            cs.setString(3, placa);
            cs.setString(4, rango);
            cs.setString(5, estado);

            cs.execute();
            return true;

        } catch (SQLException e) {
            return false;
        }
    }

    public Policia buscarPorId(int idUsuario) {
        String sql = "{call pkg_policias.pr_consultar_policia(?, ?, ?, ?, ?)}";
        try (CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt(1, idUsuario);                      // id_usuario
            cs.registerOutParameter(2, Types.NUMERIC);    // p_id_unidad
            cs.registerOutParameter(3, Types.VARCHAR);    // p_placa
            cs.registerOutParameter(4, Types.VARCHAR);    // p_rango
            cs.registerOutParameter(5, Types.VARCHAR);    // p_estado
            cs.execute();

            String placa = cs.getString(3);
            if (placa == null) {
                return null;
            }

            Policia p = new Policia();
            p.setId_policia(idUsuario);
            p.setPlaca(placa);
            p.setRango(cs.getString(4));
            p.setEstadopolicial(EstadoPolicia.valueOf(cs.getString(5)));
            UnidadPolicial u = new UnidadPolicial();
            u.setId_unidad(cs.getInt(2));
            p.setUnidadpolicial(u);
            return p;
        } catch (SQLException e) {
            return null;
        }
    }

    public List<Policia> listar() {
        List<Policia> lista = new ArrayList<>();
        String sql = "{call pkg_policias.pr_listar_policias(?)}";
        try (CallableStatement cs = con.prepareCall(sql)) {
            cs.registerOutParameter(1, OracleTypes.CURSOR);
            cs.execute();
            ResultSet rs = (ResultSet) cs.getObject(1);
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {

        }
        return lista;
    }

    public boolean actualizar(Policia p) {
        String sql = "{call pkg_policias.pr_actualizar_policia(?, ?, ?, ?, ?)}";
        try (CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt(1, p.getId_policia());
            cs.setInt(2, p.getUnidadpolicial().getId_unidad());
            cs.setString(3, p.getPlaca());
            cs.setString(4, p.getRango());
            cs.setString(5, p.getEstadopolicial().name());
            cs.execute();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean eliminar(int idUsuario) {
        String sql = "{call pkg_policias.pr_eliminar_policia(?)}";
        try (CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt(1, idUsuario);
            cs.execute();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    private Policia mapear(ResultSet rs) throws SQLException {
        Policia p = new Policia();
        p.setId_policia(rs.getInt("ID_USUARIO"));
        p.setPrimer_nombre(rs.getString("PRIMER_NOMBRE"));
        p.setSegundo_nombre(rs.getString("SEGUNDO_NOMBRE"));
        p.setPrimer_apellido(rs.getString("PRIMER_APELLIDO"));
        p.setSegundo_apellido(rs.getString("SEGUNDO_APELLIDO"));
        p.setIdentificacion(rs.getString("CEDULA"));
        p.setTelefono(rs.getString("TELEFONO"));
        p.setCorreo(rs.getString("EMAIL"));
        p.setUsername(rs.getString("USERNAME"));
        p.setPassword(rs.getString("PASSWORD"));
        p.setEstado(EstadoUsuario.valueOf(rs.getString("ACTIVO")));
        p.setPlaca(rs.getString("PLACA"));
        p.setRango(rs.getString("RANGO"));
        p.setEstadopolicial(EstadoPolicia.valueOf(rs.getString("ESTADO_POLICIA")));
        UnidadPolicial u = new UnidadPolicial();
        u.setId_unidad(rs.getInt("ID_UNIDAD"));
        p.setUnidadpolicial(u);
        return p;
    }
}
