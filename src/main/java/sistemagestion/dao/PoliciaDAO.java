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

    private Connection con() throws SQLException {
        return ConexionDB.getInstancia().getConexion();
    }

    public PoliciaDAO() throws SQLException {

    }

    public boolean insertarCompleto(
            String cedula,
            String primerNombre, String segundoNombre,
            String primerApellido, String segundoApellido,
            String telefono, String email,
            String username, String password,
            String nombreRol,
            String nombreUnidad,
            String placa, String rango, String estado
    ) {
        // Paso 1: crear usuario
        String sqlUsuario = "{call pkg_usuarios.pr_insertar_usuario(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}";
        try (CallableStatement cs = con().prepareCall(sqlUsuario)) {
            cs.setString(1, primerNombre);
            cs.setString(2, segundoNombre);
            cs.setString(3, primerApellido);
            cs.setString(4, segundoApellido);
            cs.setString(5, cedula);
            cs.setString(6, telefono);
            cs.setString(7, email);
            cs.setString(8, username);
            cs.setString(9, password);
            cs.setString(10, nombreRol);
            cs.setString(11, null); // barrio opcional
            cs.setString(12, null); // calle
            cs.setString(13, null); // carrera
            cs.setString(14, null); // etapa
            cs.setString(15, null); // manzana
            cs.setString(16, null); // casa
            cs.execute();
        } catch (SQLException e) {
            System.out.println("Error creando usuario base: " + e.getMessage());
            return false;
        }

        String sqlPolicia = "{call pkg_usuarios.pr_insertar_policia(?,?,?,?,?)}";
        try (CallableStatement cs = con().prepareCall(sqlPolicia)) {
            System.out.println("Insertando policia - cedula: [" + cedula + "] unidad: [" + nombreUnidad + "] placa: [" + placa + "] rango: [" + rango + "] estado: [" + estado + "]");
            cs.setString(1, cedula);
            cs.setString(2, nombreUnidad);
            cs.setString(3, placa);
            cs.setString(4, rango);
            cs.setString(5, estado);
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.out.println("Error creando policia: " + e.getMessage());
            try {
                eliminarUsuarioBase(cedula);
            } catch (Exception ignored) {
            }
            return false;
        }
    }

    private void eliminarUsuarioBase(String cedula) throws SQLException {
        String sql = "{call pkg_usuarios.pr_eliminar_usuario(?)}";
        try (CallableStatement cs = con().prepareCall(sql)) {
            cs.setString(1, cedula);
            cs.execute();
        }
    }

    public boolean actualizar(
            String cedulaUsuario,
            String primerNombre, String segundoNombre,
            String primerApellido, String segundoApellido,
            String telefono, String email,
            String username,
            String nombreUnidad,
            String placa,
            String rango,
            String estado
    ) {
        // Paso 1: actualizar datos personales en USUARIOS
        String sqlUsuario = "{call pkg_usuarios.pr_actualizar_usuario(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}";
        try (CallableStatement cs = con().prepareCall(sqlUsuario)) {
            cs.setString(1, username);
            cs.setString(2, primerNombre);
            cs.setString(3, segundoNombre);
            cs.setString(4, primerApellido);
            cs.setString(5, segundoApellido);
            cs.setString(6, telefono);
            cs.setString(7, email);
            cs.setString(8, null); // password sin cambio
            cs.setString(9, "POLICIA"); // rol
            cs.setString(10, null); // barrio
            cs.setString(11, null); // calle
            cs.setString(12, null); // carrera
            cs.setString(13, null); // etapa
            cs.setString(14, null); // manzana
            cs.setString(15, null); // casa
            cs.execute();
        } catch (SQLException e) {
            System.out.println("Error actualizando usuario: " + e.getMessage());
            return false;
        }

        // Paso 2: actualizar datos policiales en POLICIAS
        String sqlPolicia = "{call pkg_usuarios.pr_actualizar_policia(?, ?, ?, ?, ?)}";
        try (CallableStatement cs = con().prepareCall(sqlPolicia)) {
            cs.setString(1, cedulaUsuario);
            cs.setString(2, nombreUnidad);
            cs.setString(3, placa);
            cs.setString(4, rango);
            cs.setString(5, estado);
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.out.println("Error actualizando policia: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminar(String cedulaUsuario) {
        String sql = "{call pkg_usuarios.pr_eliminar_policia(?)}";
        try (CallableStatement cs = con().prepareCall(sql)) {
            cs.setString(1, cedulaUsuario);
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.out.println("Error eliminar policia: " + e.getMessage());
            return false;
        }
    }

    public boolean verificarPlaca(String username, String placa) {
        String sql = "SELECT COUNT(*) FROM policias p "
                + "JOIN usuarios u ON p.id_usuario = u.id_usuario "
                + "WHERE u.username = ? AND UPPER(p.placa) = UPPER(?)";
        try (java.sql.PreparedStatement ps = con().prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, placa);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println("Error verificar placa: " + e.getMessage());
        }
        return false;
    }

    public List<Policia> listar() {
        List<Policia> lista = new ArrayList<>();
        String sql = "{call pkg_usuarios.pr_listar_policias(?)}";
        try (CallableStatement cs = con().prepareCall(sql)) {
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
        try (CallableStatement cs = con().prepareCall(sql)) {
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
        try (CallableStatement cs = con().prepareCall(sql)) {
            cs.setString(1, texto);
            cs.registerOutParameter(2, OracleTypes.CURSOR);
            cs.execute();
            ResultSet rs = (ResultSet) cs.getObject(2);
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error listar policias: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }

    private Policia mapear(ResultSet rs) throws SQLException {
        Policia p = new Policia();

        // campos heredados de Persona
        p.setId_policia(rs.getInt("ID_USUARIO"));
        p.setPrimer_nombre(rs.getString("PRIMER_NOMBRE"));
        p.setSegundo_nombre(rs.getString("SEGUNDO_NOMBRE"));
        p.setPrimer_apellido(rs.getString("PRIMER_APELLIDO"));
        p.setSegundo_apellido(rs.getString("SEGUNDO_APELLIDO"));
        p.setIdentificacion(rs.getString("CEDULA"));
        p.setTelefono(rs.getString("TELEFONO"));
        p.setCorreo(rs.getString("EMAIL"));
        p.setUsername(rs.getString("USERNAME"));

        // ACTIVO puede ser null → protegemos
        String activo = rs.getString("ACTIVO");
        if (activo != null) {
            try {
                p.setEstado(EstadoUsuario.valueOf(activo.trim().toUpperCase()));
            } catch (IllegalArgumentException ignored) {
            }
        }

        // ROL puede ser null
        String rolStr = rs.getString("ROL");
        if (rolStr != null && !rolStr.isBlank()) {
            RolUsuario r = new RolUsuario();
            r.setNombre(rolStr);
            p.setRol(r);
        }

        // campos propios de Policia
        p.setPlaca(rs.getString("PLACA"));
        p.setRango(rs.getString("RANGO"));

        // ESTADO_POLICIA puede ser null → protegemos
        String estadoP = rs.getString("EST_POLICIA");
        System.out.println(">>> EST_POLICIA raw = [" + estadoP + "]");
        if (estadoP != null) {
            try {
                p.setEstadopolicial(EstadoPolicia.valueOf(estadoP.trim().toUpperCase()));
            } catch (IllegalArgumentException e) {
                System.out.println("EstadoPolicia desconocido: " + estadoP);
            }
        }

        // NOMBRE_UNIDAD puede ser null
        // ASÍ DEBE QUEDAR
        String nomUnidad = rs.getString("NOMBRE_UNIDAD");
        int idUnidad = 0;
        try {
            idUnidad = rs.getInt("ID_UNIDAD");
            System.out.println(">>> ID_UNIDAD leído: " + idUnidad);
        } catch (SQLException e) {
            System.out.println(">>> ERROR leyendo ID_UNIDAD: " + e.getMessage());
        }

        UnidadPolicial u = new UnidadPolicial();
        u.setId_unidad(idUnidad);
        u.setNombre(nomUnidad != null ? nomUnidad : "—");
        p.setUnidadpolicial(u);

        return p;
    }

}