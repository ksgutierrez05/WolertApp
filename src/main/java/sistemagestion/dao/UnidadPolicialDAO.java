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
import sistemagestion.model.Barrio;
import sistemagestion.model.Comuna;
import sistemagestion.model.EstadoUnidadPolicial;
import sistemagestion.model.UnidadPolicial;

/**
 *
 * @author Lenovo
 */
public class UnidadPolicialDAO {

    private Connection con;

    public UnidadPolicialDAO() throws SQLException {
        this.con = ConexionDB.getInstancia().getConexion();
    }

    public boolean insertar(String nombre, String estado, int idBarrio) {

        String sql = "{call pkg_unidades_policiales.pr_insertar_unidad(?, ?, ?)}";

        try (CallableStatement cs = con.prepareCall(sql)) {

            cs.setString(1, nombre);
            cs.setString(2, estado);
            cs.setInt(3, idBarrio);

            cs.execute();
            return true;

        } catch (SQLException e) {
            return false;
        }
    }

    public UnidadPolicial buscarPorId(int id) {
        String sql = "{call pkg_unidades_policiales.pr_consultar_unidad(?, ?, ?, ?)}";
        try (CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt(1, id);
            cs.registerOutParameter(2, Types.VARCHAR); // p_nombre
            cs.registerOutParameter(3, Types.VARCHAR); // p_estado
            cs.registerOutParameter(4, Types.NUMERIC); // p_id_barrio
            cs.execute();

            String nombre = cs.getString(2);
            if (nombre == null) {
                return null;
            }

            UnidadPolicial u = new UnidadPolicial();
            u.setId_unidad(id);
            u.setNombre(nombre);
            u.setEstado(EstadoUnidadPolicial.valueOf(cs.getString(3)));
            Barrio b = new Barrio();
            b.setId_barrio(cs.getInt(4));
            u.setBarrio(b);
            return u;
        } catch (SQLException e) {
            return null;
        }
    }

    public List<UnidadPolicial> listar() {
        List<UnidadPolicial> lista = new ArrayList<>();
        String sql = "{call pkg_unidades_policiales.pr_listar_unidades(?)}";
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

    public boolean actualizar(UnidadPolicial u) {
        String sql = "{call pkg_unidades_policiales.pr_actualizar_unidad(?, ?, ?, ?)}";
        try (CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt(1, u.getId_unidad());
            cs.setString(2, u.getNombre());
            cs.setString(3, u.getEstado().name());
            cs.setInt(4, u.getBarrio().getId_barrio());
            cs.execute();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean eliminar(int id) {
        String sql = "{call pkg_unidades_policiales.pr_eliminar_unidad(?)}";
        try (CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt(1, id);
            cs.execute();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    private UnidadPolicial mapear(ResultSet rs) throws SQLException {
        UnidadPolicial u = new UnidadPolicial();
        u.setId_unidad(rs.getInt("ID_UNIDAD"));
        u.setNombre(rs.getString("NOMBRE"));
        u.setEstado(EstadoUnidadPolicial.valueOf(rs.getString("ESTADO")));

        Comuna com = new Comuna();
        com.setId_comuna(rs.getInt("ID_COMUNA"));
        com.setNombre(rs.getString("NOMBRE_COMUNA"));

        Barrio b = new Barrio();
        b.setId_barrio(rs.getInt("ID_BARRIO"));
        b.setNombre(rs.getString("NOMBRE_BARRIO"));
        b.setComuna(com);

        u.setBarrio(b);
        return u;
    }
}
