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
import sistemagestion.dao.ConexionDB;
import sistemagestion.model.MedioTransporte;

/**
 *
 * @author Lenovo
 */
public class MedioTransporteDAO {

    private Connection con;

    public MedioTransporteDAO() throws SQLException {
        this.con = ConexionDB.getInstancia().getConexion();
    }


    public boolean insertar(MedioTransporte m) {
        String sql = "{call pkg_medios_transporte.pr_insertar_medio(?)}";

        try (CallableStatement cs = con.prepareCall(sql)) {
            cs.setString(1, m.getNombre());
            cs.execute();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public MedioTransporte buscarPorId(int id) {
        String sql = "{call pkg_medios_transporte.pr_consultar_medio(?, ?)}";

        try (CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, id);
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
    
    public List<MedioTransporte> listar() {
        List<MedioTransporte> lista = new ArrayList<>();

        String sql = "{call pkg_medios_transporte.pr_listar_medios(?)}";

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

    public boolean actualizar(MedioTransporte m) {
        String sql = "{call pkg_medios_transporte.pr_actualizar_medio(?, ?)}";

        try (CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt(1, m.getId_mediotransporte());
            cs.setString(2, m.getNombre());
            cs.execute();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean eliminar(int id) {
        String sql = "{call pkg_medios_transporte.pr_eliminar_medio(?)}";

        try (CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt(1, id);
            cs.execute();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    private MedioTransporte mapear(ResultSet rs) throws SQLException {
        MedioTransporte m = new MedioTransporte();
        m.setId_mediotransporte(rs.getInt("ID_MEDIO_TRANSPORTE"));
        m.setNombre(rs.getString("NOMBRE"));
        return m;
    }
}