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
            String primerNombre,
            String segundoNombre,
            String primerApellido,
            String segundoApellido,
            String identificacion,
            String telefono,
            String correo,
            String username,
            String password,
            String estado,
            int idRol,
            Integer idBarrio
    ) {

        String sql = "{CALL PKG_USUARIOS.pr_insertar_usuario(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}";

        try (CallableStatement cs = con.prepareCall(sql)) {

            cs.setNull(1, Types.NUMERIC);

            cs.setString(2, primerNombre);
            cs.setString(3, segundoNombre);
            cs.setString(4, primerApellido);
            cs.setString(5, segundoApellido);
            cs.setString(6, identificacion);
            cs.setString(7, telefono);
            cs.setString(8, correo);
            cs.setString(9, username);
            cs.setString(10, password);

            cs.setString(11, estado);

            cs.setInt(12, idRol);

            if (idBarrio != null) {
                cs.setInt(13, idBarrio);
            } else {
                cs.setNull(13, Types.NUMERIC);
            }

            cs.setNull(14, Types.VARCHAR);
            cs.setNull(15, Types.VARCHAR);
            cs.setNull(16, Types.VARCHAR);
            cs.setNull(17, Types.VARCHAR);
            cs.setNull(18, Types.VARCHAR);

            cs.execute();
            return true;

        } catch (SQLException e) {
            return false;
        }
    }

    public void actualizar(Usuario u) throws SQLException {
        String sql = "{CALL PKG_USUARIOS.pr_actualizar_usuario(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}";
        try (CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt(1, u.getId_usuario());
            cs.setString(2, u.getPrimer_nombre());
            cs.setString(3, u.getSegundo_nombre());
            cs.setString(4, u.getPrimer_apellido());
            cs.setString(5, u.getSegundo_apellido());
            cs.setString(6, u.getTelefono());
            cs.setString(7, u.getCorreo());
            cs.setString(8, u.getUsername());
            cs.setString(9, u.getPassword());
            cs.setString(10, u.getEstado().name());
            cs.setInt(11, 4); // id_rol

            if (u.getDireccion() != null && u.getDireccion().getBarrio() != null) {
                cs.setInt(12, u.getDireccion().getBarrio().getId_barrio());
            } else {
                cs.setNull(12, Types.NUMERIC);
            }

            cs.setNull(13, Types.VARCHAR); // calle
            cs.setNull(14, Types.VARCHAR); // carrera
            cs.setNull(15, Types.VARCHAR); // etapa
            cs.setNull(16, Types.VARCHAR); // manzana
            cs.setNull(17, Types.VARCHAR); // casa
            cs.execute();
        }
    }

    public void eliminar(int id) throws SQLException {
        try (CallableStatement cs = con.prepareCall("{CALL PKG_USUARIOS.pr_eliminar_usuario(?)}")) {
            cs.setInt(1, id);
            cs.execute();
        }
    }

    public boolean existePorCedula(String cedula) throws SQLException {
        try (CallableStatement cs = con.prepareCall("{? = CALL PKG_USUARIOS.fx_usuario_existe(?)}")) {
            cs.registerOutParameter(1, Types.NUMERIC);
            cs.setString(2, cedula);
            cs.execute();
            return cs.getInt(1) > 0;
        }
    }

    public Usuario login(String username, String password) throws SQLException {

        String sql = "{call pkg_usuarios.prc_login_usuario(?, ?, ?)}";

        try (CallableStatement cs = con.prepareCall(sql)) {

            cs.setString(1, username);
            cs.setString(2, password);

            cs.registerOutParameter(3, oracle.jdbc.OracleTypes.CURSOR);

            cs.execute();

            ResultSet rs = (ResultSet) cs.getObject(3);

            if (rs.next()) {
                return mapear(rs);
            }

        }

        return null;
    }

    public Usuario buscarPorId(int id) throws SQLException {

        String sql = "{call pkg_usuarios.prc_consultar_usuario(?, ?)}";

        try (CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, id);
            cs.registerOutParameter(2, oracle.jdbc.OracleTypes.CURSOR);

            cs.execute();

            ResultSet rs = (ResultSet) cs.getObject(2);

            if (rs.next()) {
                return mapear(rs);
            }

        }

        return null;
    }

    public List<Usuario> listarTodos() throws SQLException {

        List<Usuario> lista = new ArrayList<>();

        String sql = "{call pkg_usuarios.prc_listar_usuarios(?)}";

        try (CallableStatement cs = con.prepareCall(sql)) {

            cs.registerOutParameter(1, oracle.jdbc.OracleTypes.CURSOR);

            cs.execute();

            ResultSet rs = (ResultSet) cs.getObject(1);

            while (rs.next()) {
                lista.add(mapear(rs));
            }

        }

        return lista;
    }

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

}
