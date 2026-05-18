/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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

    public void insertar(Usuario u) throws SQLException {

        String sql = "{CALL PKG_USUARIOS.pr_insertar_usuario(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}";

        try (CallableStatement cs = con.prepareCall(sql)) {

            cs.setNull(1, Types.NUMERIC);
            cs.setString(2, u.getPrimer_nombre());
            cs.setString(3, u.getSegundo_nombre());
            cs.setString(4, u.getPrimer_apellido());
            cs.setString(5, u.getSegundo_apellido());
            cs.setString(6, u.getIdentificacion());   // cedula
            cs.setString(7, u.getTelefono());
            cs.setString(8, u.getCorreo());            // email
            cs.setString(9, u.getUsername());
            cs.setString(10, u.getPassword());
            cs.setString(11, EstadoUsuario.ACTIVO.name()); // activo
            cs.setInt(12, rolToId(u.getRol())); // id_rol 

            if (u.getDireccion() != null && u.getDireccion().getBarrio() != null) {
                cs.setInt(13, u.getDireccion().getBarrio().getId_barrio());
            } else {
                cs.setNull(13, Types.NUMERIC);
            }
            cs.setNull(14, Types.VARCHAR); // calle
            cs.setNull(15, Types.VARCHAR); // carrera
            cs.setNull(16, Types.VARCHAR); // etapa
            cs.setNull(17, Types.VARCHAR); // manzana
            cs.setNull(18, Types.VARCHAR); // casa
            cs.execute();

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
        String sql = "SELECT u.ID_USUARIO, u.PRIMER_NOMBRE, u.SEGUNDO_NOMBRE, "
                + "u.PRIMER_APELLIDO, u.SEGUNDO_APELLIDO, u.CEDULA, u.TELEFONO, "
                + "u.EMAIL, u.USERNAME, u.PASSWORD, u.ACTIVO, r.NOMBRE AS ROL_NOMBRE "
                + "FROM USUARIOS u JOIN ROLES_USUARIO r ON u.ID_ROL = r.ID_ROL "
                + "WHERE u.USERNAME = ? AND u.PASSWORD = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapear(rs);
            }
        }
        return null;
    }

    public Usuario buscarPorId(int id) throws SQLException {
        String sql = "SELECT u.ID_USUARIO, u.PRIMER_NOMBRE, u.SEGUNDO_NOMBRE, "
                + "u.PRIMER_APELLIDO, u.SEGUNDO_APELLIDO, u.CEDULA, u.TELEFONO, "
                + "u.EMAIL, u.USERNAME, u.PASSWORD, u.ACTIVO, r.NOMBRE AS ROL_NOMBRE "
                + "FROM USUARIOS u JOIN ROLES_USUARIO r ON u.ID_ROL = r.ID_ROL "
                + "WHERE u.ID_USUARIO = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapear(rs);
            }
        }
        return null;
    }

    public List<Usuario> listarTodos() throws SQLException {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT u.ID_USUARIO, u.PRIMER_NOMBRE, u.SEGUNDO_NOMBRE, "
                + "u.PRIMER_APELLIDO, u.SEGUNDO_APELLIDO, u.CEDULA, u.TELEFONO, "
                + "u.EMAIL, u.USERNAME, u.PASSWORD, u.ACTIVO, r.NOMBRE AS ROL_NOMBRE "
                + "FROM USUARIOS u JOIN ROLES_USUARIO r ON u.ID_ROL = r.ID_ROL "
                + "ORDER BY u.PRIMER_APELLIDO";
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
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
        u.setRol(RolUsuario.valueOf(rs.getString("ROL_NOMBRE")));
        return u;
    }

    private int rolToId(RolUsuario rol) {
        switch (rol) {
            case ADMINISTRADOR:
                return 1;
            case ADMINISTRADOR_POLICIA:
                return 2;
            case POLICIA:
                return 3;
            default:
                return 4; // CIUDADANO
        }
    }
}
