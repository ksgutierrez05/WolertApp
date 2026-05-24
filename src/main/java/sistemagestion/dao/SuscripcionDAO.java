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
import sistemagestion.model.Barrio;
import sistemagestion.model.Comuna;
import sistemagestion.model.EstadoSuscripcion;
import sistemagestion.model.Suscripcion;
import sistemagestion.model.TipoAlerta;
import sistemagestion.model.Usuario;

/**
 *
 * @author Lenovo
 */
public class SuscripcionDAO {

    private Connection con;

    public SuscripcionDAO() throws SQLException {
        this.con = ConexionDB.getInstancia().getConexion();
    }

    public boolean insertar(
            String cedulaUsuario,
            String tipoAlerta,
            String nombreComuna,  
            String nombreBarrio,   
            String estado
    ) {
        String sql = "{call pkg_alertas.pr_insertar_suscripcion(?, ?, ?, ?, ?)}";
        try (CallableStatement cs = con.prepareCall(sql)) {
            cs.setString(1, cedulaUsuario);
            cs.setString(2, tipoAlerta);
            cs.setString(3, nombreComuna);
            cs.setString(4, nombreBarrio);
            cs.setString(5, estado);
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.out.println("Error insertar suscripcion: " + e.getMessage());
            return false;
        }
    }

    public boolean actualizar(
            int idSuscripcion,
            String cedulaUsuario,
            String tipoAlerta,
            String nombreComuna,   
            String nombreBarrio,  
            String estado
    ) {
        String sql = "{call pkg_alertas.pr_actualizar_suscripcion(?, ?, ?, ?, ?, ?)}";
        try (CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt(1, idSuscripcion);
            cs.setString(2, cedulaUsuario);
            cs.setString(3, tipoAlerta);
            cs.setString(4, nombreComuna);
            cs.setString(5, nombreBarrio);
            cs.setString(6, estado);
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.out.println("Error actualizar suscripcion: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminar(int idSuscripcion) {
        String sql = "{call pkg_alertas.pr_eliminar_suscripcion(?)}";
        try (CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt(1, idSuscripcion);
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.out.println("Error eliminar suscripcion: " + e.getMessage());
            return false;
        }
    }


    public List<Suscripcion> listar() {
        List<Suscripcion> lista = new ArrayList<>();
        String sql = "{call pkg_alertas.pr_listar_suscripciones(?)}";
        try (CallableStatement cs = con.prepareCall(sql)) {
            cs.registerOutParameter(1, OracleTypes.CURSOR);
            cs.execute();
            ResultSet rs = (ResultSet) cs.getObject(1);
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error listar suscripciones: " + e.getMessage());
        }
        return lista;
    }

    // vw_suscripciones retorna:
    // ID_SUSCRIPCION, ESTADO, NOMBRE_USUARIO, CEDULA, TIPO_ALERTA, COMUNA, BARRIO
    private Suscripcion mapear(ResultSet rs) throws SQLException {
        Suscripcion s = new Suscripcion();
        s.setId_suscripcion(rs.getInt("ID_SUSCRIPCION"));
        s.setEstado(EstadoSuscripcion.valueOf(rs.getString("ESTADO")));

        Usuario u = new Usuario();
        u.setPrimer_nombre(rs.getString("NOMBRE_USUARIO"));
        u.setIdentificacion(rs.getString("CEDULA"));
        s.setUsuario(u);

        TipoAlerta t = new TipoAlerta();
        t.setNombre(rs.getString("TIPO_ALERTA"));
        s.setTipoalerta(t);

        String nombreComuna = rs.getString("COMUNA");
        if (nombreComuna != null) {
            Comuna c = new Comuna();
            c.setNombre(nombreComuna);
            s.setComuna(c);
        }

        
        String nombreBarrio = rs.getString("BARRIO");
        if (nombreBarrio != null) {
            Barrio b = new Barrio();
            b.setNombre(nombreBarrio);
            s.setBarrio(b);
        }

        return s;
    }
}