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
import sistemagestion.model.TipoArma;

/**
 *
 * @author Lenovo
 */
public class TipoArmaDAO {

    private Connection con;

    public TipoArmaDAO() throws SQLException {
        this.con = ConexionDB.getInstancia().getConexion();
    }

    public boolean insertar(TipoArma t) {

        String sql = "{ call pkg_tipos_arma.pr_insertar_tipo_arma(?) }";

        try (CallableStatement cs = con.prepareCall(sql)) {

            cs.setString(1, t.getNombre());
            cs.execute();
            return true;

        } catch (SQLException e) {
            return false;
        }
    }

    public TipoArma buscarPorId(int id) {

        String sql = "{ ? = call pkg_tipos_arma.fn_consultar_tipo_arma(?) }";

        try (CallableStatement cs = con.prepareCall(sql)) {

            cs.registerOutParameter(1, Types.VARCHAR);
            cs.setInt(2, id);

            cs.execute();

            String nombre = cs.getString(1);

            if (nombre == null) return null;

            TipoArma t = new TipoArma();
            t.setId_tipoarma(id);
            t.setNombre(nombre);

            return t;

        } catch (SQLException e) {
            return null;
        }
    }

    public List<TipoArma> listarTodos() {

        List<TipoArma> lista = new ArrayList<>();

        String sql = "{ call pkg_tipos_arma.pr_listar_tipos_arma(?) }";

        try (CallableStatement cs = con.prepareCall(sql)) {

            cs.registerOutParameter(1, OracleTypes.CURSOR);
            cs.execute();

            ResultSet rs = (ResultSet) cs.getObject(1);

            while (rs.next()) {
                lista.add(mapear(rs));
            }

        } catch (SQLException e) {
            return new ArrayList<>();
        }

        return lista;
    }

    public boolean actualizar(TipoArma t) {

        String sql = "{ call pkg_tipos_arma.pr_actualizar_tipo_arma(?, ?) }";

        try (CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, t.getId_tipoarma());
            cs.setString(2, t.getNombre());

            cs.execute();
            return true;

        } catch (SQLException e) {
            return false;
        }
    }

    public boolean eliminar(int id) {

        String sql = "{ call pkg_tipos_arma.pr_eliminar_tipo_arma(?) }";

        try (CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, id);
            cs.execute();
            return true;

        } catch (SQLException e) {
            return false;
        }
    }

    private TipoArma mapear(ResultSet rs) throws SQLException {

        TipoArma t = new TipoArma();
        t.setId_tipoarma(rs.getInt("ID_TIPO_ARMA"));
        t.setNombre(rs.getString("NOMBRE"));

        return t;
    }
}
