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
import sistemagestion.model.EstadoPolicia;
import sistemagestion.model.EstadoUsuario;
import sistemagestion.model.Policia;
import sistemagestion.model.RolUsuario;
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

    public boolean insertar(
            String cedulaUsuario,
            String nombreUnidad,
            String placa,
            String rango,
            String estado
    ) {
        String sql = "{call pkg_usuarios.pr_insertar_policia(?, ?, ?, ?, ?)}";
        try (CallableStatement cs = con.prepareCall(sql)) {
            cs.setString(1, cedulaUsuario);
            cs.setString(2, nombreUnidad);
            cs.setString(3, placa);
            cs.setString(4, rango);
            cs.setString(5, estado);
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.out.println("Error insertar policia: " + e.getMessage());
            return false;
        }
    }

    public boolean actualizar(
            String cedulaUsuario,
            String nombreUnidad,
            String placa,
            String rango,
            String estado
    ) {
        String sql = "{call pkg_usuarios.pr_actualizar_policia(?, ?, ?, ?, ?)}";
        try (CallableStatement cs = con.prepareCall(sql)) {
            cs.setString(1, cedulaUsuario);
            cs.setString(2, nombreUnidad);
            cs.setString(3, placa);
            cs.setString(4, rango);
            cs.setString(5, estado);
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.out.println("Error actualizar policia: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminar(String cedulaUsuario) {
        String sql = "{call pkg_usuarios.pr_eliminar_policia(?)}";
        try (CallableStatement cs = con.prepareCall(sql)) {
            cs.setString(1, cedulaUsuario);
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.out.println("Error eliminar policia: " + e.getMessage());
            return false;
        }
    }

    public boolean verificarPlaca(String username, String placa) {
        String sql = "SELECT COUNT(*) FROM policias p " +
                     "JOIN usuarios u ON p.id_usuario = u.id_usuario " +
                     "WHERE u.username = ? AND UPPER(p.placa) = UPPER(?)";
        try (java.sql.PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, placa);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.out.println("Error verificar placa: " + e.getMessage());
        }
        return false;
    }

    public List<Policia> listar() {
        List<Policia> lista = new ArrayList<>();
        String sql = "{call pkg_usuarios.pr_listar_policias(?)}";
        try (CallableStatement cs = con.prepareCall(sql)) {
            cs.registerOutParameter(1, OracleTypes.CURSOR);
            cs.execute();
            ResultSet rs = (ResultSet) cs.getObject(1);
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error listar policias: " + e.getMessage());
        }
        return lista;
    }

    public List<Policia> buscar(String texto) {
        List<Policia> lista = new ArrayList<>();
        String sql = "{call pkg_usuarios.pr_buscar_policia(?, ?)}";
        try (CallableStatement cs = con.prepareCall(sql)) {
            cs.setString(1, texto);
            cs.registerOutParameter(2, OracleTypes.CURSOR);
            cs.execute();
            ResultSet rs = (ResultSet) cs.getObject(2);
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error buscar policia: " + e.getMessage());
        }
        return lista;
    }

    public List<Policia> buscarExacto(String texto) {
        List<Policia> lista = new ArrayList<>();
        String sql = "{call pkg_usuarios.pr_buscar_policia_exacto(?, ?)}";
        try (CallableStatement cs = con.prepareCall(sql)) {
            cs.setString(1, texto);
            cs.registerOutParameter(2, OracleTypes.CURSOR);
            cs.execute();
            ResultSet rs = (ResultSet) cs.getObject(2);
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error buscar policia exacto: " + e.getMessage());
        }
        return lista;
    }

    // cursor retorna campos separados 
    private Policia mapear(ResultSet rs) throws SQLException {
        Policia p = new Policia();

        // campos heredados de Persona
        p.setPrimer_nombre(rs.getString("PRIMER_NOMBRE"));
        p.setSegundo_nombre(rs.getString("SEGUNDO_NOMBRE"));
        p.setPrimer_apellido(rs.getString("PRIMER_APELLIDO"));
        p.setSegundo_apellido(rs.getString("SEGUNDO_APELLIDO"));
        p.setIdentificacion(rs.getString("CEDULA"));
        p.setTelefono(rs.getString("TELEFONO"));
        p.setCorreo(rs.getString("EMAIL"));
        p.setUsername(rs.getString("USERNAME"));
        p.setEstado(EstadoUsuario.valueOf(rs.getString("ACTIVO")));

        RolUsuario r = new RolUsuario();
        r.setNombre(rs.getString("ROL"));
        p.setRol(r);

        // campos propios de Policia
        p.setPlaca(rs.getString("PLACA"));
        p.setRango(rs.getString("RANGO"));
        p.setEstadopolicial(EstadoPolicia.valueOf(rs.getString("ESTADO_POLICIA")));

        UnidadPolicial u = new UnidadPolicial();
        u.setNombre(rs.getString("NOMBRE_UNIDAD"));
        p.setUnidadpolicial(u);

        return p;
    }
}