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
import sistemagestion.model.TipoAlerta;

/**
 *
 * @author Lenovo
 */
public class TipoAlertaDAO {

    private Connection con;

    public TipoAlertaDAO() throws SQLException {
        this.con = ConexionDB.getInstancia().getConexion();
    }

    public boolean insertar(String nombre) {

        String sql = "{call pkg_tipos_alerta.pr_insertar_tipo_alerta(?)}";

        try (CallableStatement cs = con.prepareCall(sql)) {

            cs.setString(1, nombre);

            cs.execute();
            return true;

        } catch (SQLException e) {
            return false;
        }
    }

    public TipoAlerta buscarPorId(int id) {
        String sql = "{ call pkg_tipos_alerta.pr_consultar_tipo_alerta(?, ?) }";

        try (CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, id);
            cs.registerOutParameter(2, Types.VARCHAR);

            cs.execute();

            String nombre = cs.getString(2);

            if (nombre == null) {
                return null;
            }

            TipoAlerta t = new TipoAlerta();
            t.setId_tipoalerta(id);
            t.setNombre(nombre);

            return t;

        } catch (SQLException e) {
            return null;
        }
    }

    public List<TipoAlerta> listarTodos() {
        List<TipoAlerta> lista = new ArrayList<>();

        String sql = "{ call pkg_tipos_alerta.pr_listar_tipos_alerta(?) }";

        try (CallableStatement cs = con.prepareCall(sql)) {

            cs.registerOutParameter(1, oracle.jdbc.OracleTypes.CURSOR);
            cs.execute();

            try (ResultSet rs = (ResultSet) cs.getObject(1)) {
                while (rs.next()) {
                    TipoAlerta t = new TipoAlerta();
                    t.setId_tipoalerta(rs.getInt("ID_TIPO_ALERTA"));
                    t.setNombre(rs.getString("NOMBRE"));
                    lista.add(t);
                }
            }

        } catch (SQLException e) {
            return new ArrayList<>();
        }

        return lista;
    }

    public boolean actualizar(TipoAlerta t) {
        String sql = "{ call pkg_tipos_alerta.pr_actualizar_tipo_alerta(?, ?) }";

        try (CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, t.getId_tipoalerta());
            cs.setString(2, t.getNombre());

            cs.execute();
            return true;

        } catch (SQLException e) {
            return false;
        }
    }

    public boolean eliminar(int id) {
        String sql = "{ call pkg_tipos_alerta.pr_eliminar_tipo_alerta(?) }";

        try (CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, id);
            cs.execute();

            return true;

        } catch (SQLException e) {
            return false;
        }
    }
}
