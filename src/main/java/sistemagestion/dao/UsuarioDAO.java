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
import sistemagestion.model.EstadoUsuario;
import sistemagestion.model.RolUsuario;
import sistemagestion.model.Usuario;

/**
 *
 * @author Lenovo
 */
public class UsuarioDAO {

    private Connection con;

    public UsuarioDAO() throws SQLException {
        this.con = ConexionDB.getInstancia().getConexion();
    }

    public boolean insertar(
            String primerNombre, String segundoNombre,
            String primerApellido, String segundoApellido,
            String cedula, String telefono,
            String correo, String username,
            String password, String nombreRol,
            String nombreBarrio, String calle,
            String carrera, String etapa,
            String manzana, String casa
    ) {
        String sql = "{call pkg_usuarios.pr_insertar_usuario(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}";
        try (CallableStatement cs = con.prepareCall(sql)) {
            cs.setString(1, primerNombre);
            cs.setString(2, segundoNombre);
            cs.setString(3, primerApellido);
            cs.setString(4, segundoApellido);
            cs.setString(5, cedula);
            cs.setString(6, telefono);
            cs.setString(7, correo);
            cs.setString(8, username);
            cs.setString(9, password);
            cs.setString(10, nombreRol);
            cs.setString(11, nombreBarrio);
            cs.setString(12, calle);
            cs.setString(13, carrera);
            cs.setString(14, etapa);
            cs.setString(15, manzana);
            cs.setString(16, casa);

            cs.execute();
            return true;
        } catch (SQLException e) {
            System.out.println("Error insertar usuario: " + e.getMessage());
            return false;
        }
    }

    public boolean actualizar(
            String username,
            String primerNombre, String segundoNombre,
            String primerApellido, String segundoApellido,
            String telefono, String correo,
            String password, String nombreRol,
            String nombreBarrio, String calle,
            String carrera, String etapa,
            String manzana, String casa
    ) {
        String sql = "{call pkg_usuarios.pr_actualizar_usuario(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}";
        try (CallableStatement cs = con.prepareCall(sql)) {
            cs.setString(1, username);
            cs.setString(2, primerNombre);
            cs.setString(3, segundoNombre);
            cs.setString(4, primerApellido);
            cs.setString(5, segundoApellido);
            cs.setString(6, telefono);
            cs.setString(7, correo);
            cs.setString(8, password);
            cs.setString(9, nombreRol);
            cs.setString(10, nombreBarrio);
            cs.setString(11, calle);
            cs.setString(12, carrera);
            cs.setString(13, etapa);
            cs.setString(14, manzana);
            cs.setString(15, casa);
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.out.println("Error actualizar usuario: " + e.getMessage());
            return false;
        }
    }

    // pr_eliminar_usuario 
    public boolean eliminar(String cedula) {
        String sql = "{call pkg_usuarios.pr_eliminar_usuario(?)}";
        try (CallableStatement cs = con.prepareCall(sql)) {
            cs.setString(1, cedula);
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.out.println("Error eliminar usuario: " + e.getMessage());
            return false;
        }
    }

    // pr_login_usuario
    public Usuario login(String username, String password) throws SQLException {
        String sql = "{call pkg_usuarios.pr_login_usuario(?, ?, ?)}";
        try (CallableStatement cs = con.prepareCall(sql)) {
            cs.setString(1, username);
            cs.setString(2, password);
            cs.registerOutParameter(3, OracleTypes.CURSOR);
            cs.execute();
            ResultSet rs = (ResultSet) cs.getObject(3);
            if (rs.next()) {
                return mapear(rs);
            }
        }
        return null;
    }

    // pr_consultar_usuario recibe cedula
    public Usuario buscarPorCedula(String cedula) throws SQLException {
        String sql = "{call pkg_usuarios.pr_consultar_usuario(?, ?)}";
        try (CallableStatement cs = con.prepareCall(sql)) {
            cs.setString(1, cedula);
            cs.registerOutParameter(2, OracleTypes.CURSOR);
            cs.execute();
            ResultSet rs = (ResultSet) cs.getObject(2);
            if (rs.next()) {
                return mapearParcial(rs);
            }
        }
        return null;
    }

    // pr_listar_usuarios 
    public List<Usuario> listarTodos() throws SQLException {
        List<Usuario> lista = new ArrayList<>();
        String sql = "{call pkg_usuarios.pr_listar_usuarios(?)}";
        try (CallableStatement cs = con.prepareCall(sql)) {
            cs.registerOutParameter(1, OracleTypes.CURSOR);
            cs.execute();
            ResultSet rs = (ResultSet) cs.getObject(1);
            while (rs.next()) {
                lista.add(mapearParcial(rs));
            }
        }
        return lista;
    }

    // pr_buscar_usuario 
    public List<Usuario> buscar(String texto) throws SQLException {
        List<Usuario> lista = new ArrayList<>();
        String sql = "{call pkg_usuarios.pr_buscar_usuario(?, ?)}";
        try (CallableStatement cs = con.prepareCall(sql)) {
            cs.setString(1, texto);
            cs.registerOutParameter(2, OracleTypes.CURSOR);
            cs.execute();
            ResultSet rs = (ResultSet) cs.getObject(2);
            while (rs.next()) {
                lista.add(mapearParcial(rs));
            }
        }
        return lista;
    }

    // pr_buscar_usuario_exacto 
    public List<Usuario> buscarExacto(String texto) throws SQLException {
        List<Usuario> lista = new ArrayList<>();
        String sql = "{call pkg_usuarios.pr_buscar_usuario_exacto(?, ?)}";
        try (CallableStatement cs = con.prepareCall(sql)) {
            cs.setString(1, texto);
            cs.registerOutParameter(2, OracleTypes.CURSOR);
            cs.execute();
            ResultSet rs = (ResultSet) cs.getObject(2);
            while (rs.next()) {
                lista.add(mapearParcial(rs));
            }
        }
        return lista;
    }

    public boolean inactivar(String cedula) {
        String sql = "{call pkg_usuarios.pr_inactivar_usuario(?)}";
        try (CallableStatement cs = con.prepareCall(sql)) {
            cs.setString(1, cedula);
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.out.println("Error inactivar usuario: " + e.getMessage());
            return false;
        }
    }

    public boolean activar(String cedula) {
        String sql = "{call pkg_usuarios.pr_activar_usuario(?)}";
        try (CallableStatement cs = con.prepareCall(sql)) {
            cs.setString(1, cedula);
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.out.println("Error activar usuario: " + e.getMessage());
            return false;
        }
    }

    // Para login 
    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId_usuario(rs.getInt("ID_USUARIO"));
        u.setPrimer_nombre(rs.getString("PRIMER_NOMBRE"));
        u.setSegundo_nombre(rs.getString("SEGUNDO_NOMBRE"));
        u.setPrimer_apellido(rs.getString("PRIMER_APELLIDO"));
        u.setSegundo_apellido(rs.getString("SEGUNDO_APELLIDO"));
        u.setIdentificacion(rs.getString("CEDULA"));
        u.setTelefono(rs.getString("TELEFONO"));
        u.setCorreo(rs.getString("EMAIL"));
        u.setUsername(rs.getString("USERNAME"));
        u.setPassword(rs.getString("PASSWORD"));
        u.setEstado(EstadoUsuario.valueOf(rs.getString("ACTIVO")));
        RolUsuario r = new RolUsuario();
        r.setIdRol(rs.getInt("ID_ROL"));
        r.setNombre(rs.getString("ROL_NOMBRE"));
        u.setRol(r);
        return u;
    }

    // Para listar, consultar y buscar
    private Usuario mapearParcial(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setPrimer_nombre(rs.getString("PRIMER_NOMBRE"));
        u.setSegundo_nombre(rs.getString("SEGUNDO_NOMBRE"));
        u.setPrimer_apellido(rs.getString("PRIMER_APELLIDO"));
        u.setSegundo_apellido(rs.getString("SEGUNDO_APELLIDO"));
        u.setIdentificacion(rs.getString("CEDULA"));
        u.setTelefono(rs.getString("TELEFONO"));
        u.setCorreo(rs.getString("EMAIL"));
        u.setUsername(rs.getString("USERNAME"));
        u.setEstado(EstadoUsuario.valueOf(rs.getString("ACTIVO")));
        RolUsuario r = new RolUsuario();
        r.setNombre(rs.getString("ROL"));
        u.setRol(r);
        return u;
    }
}
