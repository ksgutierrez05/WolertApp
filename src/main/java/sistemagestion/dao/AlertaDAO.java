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
import sistemagestion.model.Alarma;
import sistemagestion.model.Alerta;
import sistemagestion.model.Barrio;
import sistemagestion.model.Comuna;
import sistemagestion.model.Direccion;
import sistemagestion.model.EstadoAlarma;
import sistemagestion.model.EstadoAlerta;
import sistemagestion.model.MedioTransporte;
import sistemagestion.model.TipoAlerta;
import sistemagestion.model.TipoArma;
import sistemagestion.model.Usuario;

/**
 *
 * @author Lenovo
 */
public class AlertaDAO {

    private Connection con() throws SQLException {
        return ConexionDB.getInstancia().getConexion();
    }

    public AlertaDAO() throws SQLException {
       
    }

    public boolean insertar(
            String username,
            String tipoAlerta,
            String nombreBarrio,
            String tipoArma,
            String medioTransporte,
            String etapa,
            String sector,
            String manzana,
            String casa,
            String calle,
            String carrera,
            String referencia,
            Double latitud,
            Double longitud,
            String descripcion
    ) {
        String sql = "{call pkg_alertas.pr_insertar_alerta(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}";
        try (CallableStatement cs = con().prepareCall(sql)) {
            cs.setString(1, username);
            cs.setString(2, tipoAlerta);
            cs.setString(3, nombreBarrio);
            cs.setString(4, tipoArma);
            cs.setString(5, medioTransporte);
            cs.setString(6, etapa);
            cs.setString(7, sector);
            cs.setString(8, manzana);
            cs.setString(9, casa);
            cs.setString(10, calle);
            cs.setString(11, carrera);
            cs.setString(12, referencia);
            if (latitud != null) cs.setDouble(13, latitud);
            else cs.setNull(13, java.sql.Types.NUMERIC);
            if (longitud != null) cs.setDouble(14, longitud);
            else cs.setNull(14, java.sql.Types.NUMERIC);
            cs.setString(15, descripcion);
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.out.println("Error insertar alerta: " + e.getMessage());
            return false;
        }
    }

    public Alerta buscarPorId(int idAlerta) {
        String sql = "{call pkg_alertas.pr_consultar_alerta(?, ?)}";
        try (CallableStatement cs = con().prepareCall(sql)) {
            cs.setInt(1, idAlerta);
            cs.registerOutParameter(2, OracleTypes.CURSOR);
            cs.execute();
            ResultSet rs = (ResultSet) cs.getObject(2);
            if (rs.next()) return mapear(rs);
        } catch (SQLException e) {
            System.out.println("Error buscar alerta: " + e.getMessage());
        }
        return null;
    }

    public List<Alerta> listar() {
        List<Alerta> lista = new ArrayList<>();
        String sql = "{call pkg_alertas.pr_listar_alertas(?)}";
        try (CallableStatement cs = con().prepareCall(sql)) {
            cs.registerOutParameter(1, OracleTypes.CURSOR);
            cs.execute();
            ResultSet rs = (ResultSet) cs.getObject(1);
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            System.out.println("Error listar alertas: " + e.getMessage());
        }
        return lista;
    }

    public List<Alerta> listarPorUsuario(String username) {
        List<Alerta> lista = new ArrayList<>();
        String sql = "{call pkg_alertas.pr_listar_alertas_por_usuario(?, ?)}";
        try (CallableStatement cs = con().prepareCall(sql)) {
            cs.setString(1, username);
            cs.registerOutParameter(2, OracleTypes.CURSOR);
            cs.execute();
            ResultSet rs = (ResultSet) cs.getObject(2);
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            System.out.println("Error listar alertas por usuario: " + e.getMessage());
        }
        return lista;
    }

    public List<Alerta> listarPorBarrio(String nombreBarrio) {
        List<Alerta> lista = new ArrayList<>();
        String sql = "{call pkg_alertas.pr_listar_alertas_por_barrio(?, ?)}";
        try (CallableStatement cs = con().prepareCall(sql)) {
            cs.setString(1, nombreBarrio);
            cs.registerOutParameter(2, OracleTypes.CURSOR);
            cs.execute();
            ResultSet rs = (ResultSet) cs.getObject(2);
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            System.out.println("Error listar alertas por barrio: " + e.getMessage());
        }
        return lista;
    }

    public List<Alerta> buscarPorTipo(String tipoAlerta) {
        List<Alerta> lista = new ArrayList<>();
        String sql = "{call pkg_alertas.pr_buscar_alerta_por_tipo(?, ?)}";
        try (CallableStatement cs = con().prepareCall(sql)) {
            cs.setString(1, tipoAlerta);
            cs.registerOutParameter(2, OracleTypes.CURSOR);
            cs.execute();
            ResultSet rs = (ResultSet) cs.getObject(2);
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            System.out.println("Error buscar alerta por tipo: " + e.getMessage());
        }
        return lista;
    }

    public List<Alerta> buscarPorTipoExacto(String tipoAlerta) {
        List<Alerta> lista = new ArrayList<>();
        String sql = "{call pkg_alertas.pr_buscar_alerta_por_tipo_exacto(?, ?)}";
        try (CallableStatement cs = con().prepareCall(sql)) {
            cs.setString(1, tipoAlerta);
            cs.registerOutParameter(2, OracleTypes.CURSOR);
            cs.execute();
            ResultSet rs = (ResultSet) cs.getObject(2);
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            System.out.println("Error buscar alerta por tipo exacto: " + e.getMessage());
        }
        return lista;
    }

    public List<Alerta> buscarPorDescripcion(String texto) {
        List<Alerta> lista = new ArrayList<>();
        String sql = "{call pkg_alertas.pr_buscar_alerta_por_descripcion(?, ?)}";
        try (CallableStatement cs = con().prepareCall(sql)) {
            cs.setString(1, texto);
            cs.registerOutParameter(2, OracleTypes.CURSOR);
            cs.execute();
            ResultSet rs = (ResultSet) cs.getObject(2);
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            System.out.println("Error buscar alerta por descripcion: " + e.getMessage());
        }
        return lista;
    }

    public boolean actualizarEstado(int idAlerta, String estado) {
        String sql = "{call pkg_alertas.pr_actualizar_estado(?, ?)}";
        try (CallableStatement cs = con().prepareCall(sql)) {
            cs.setInt(1, idAlerta);
            cs.setString(2, estado);
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.out.println("Error actualizar estado: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminar(int idAlerta) {
        String sql = "{call pkg_alertas.pr_eliminar_alerta(?)}";
        try (CallableStatement cs = con().prepareCall(sql)) {
            cs.setInt(1, idAlerta);
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.out.println("Error eliminar alerta: " + e.getMessage());
            return false;
        }
    }

    // vw_alertas_completa retorna:
    // ID_ALERTA, FECHA, ESTADO, DESCRIPCION, ETAPA, SECTOR, MANZANA, CASA,
    // CALLE, CARRERA, REFERENCIA, LATITUD, LONGITUD,
    // NOMBRE_USUARIO, CEDULA, TELEFONO, USERNAME,
    // TIPO_ALERTA, BARRIO, COMUNA, TIPO_ARMA, MEDIO_TRANSPORTE,
    // ID_ALARMA, NOMBRE_ALARMA, ESTADO_ALARMA
    private Alerta mapear(ResultSet rs) throws SQLException {
        Alerta a = new Alerta();

        a.setId_alerta(rs.getInt("ID_ALERTA"));
        a.setDescripcion(rs.getString("DESCRIPCION"));
        a.setFechaHora(rs.getTimestamp("FECHA").toLocalDateTime());
        a.setEstado(EstadoAlerta.valueOf(rs.getString("ESTADO")));

        // usuario
        Usuario u = new Usuario();
        u.setPrimer_nombre(rs.getString("NOMBRE_USUARIO"));
        u.setUsername(rs.getString("USERNAME"));
        u.setIdentificacion(rs.getString("CEDULA"));
        u.setTelefono(rs.getString("TELEFONO"));
        a.setUsuario(u);

        // tipo alerta
        TipoAlerta ta = new TipoAlerta();
        ta.setNombre(rs.getString("TIPO_ALERTA"));
        a.setTipoalerta(ta);

        // barrio y comuna
        Comuna c = new Comuna();
        c.setNombre(rs.getString("COMUNA"));
        Barrio b = new Barrio();
        b.setNombre(rs.getString("BARRIO"));
        b.setComuna(c);
        a.setBarrio(b);

        // tipo arma 
        String tipoArma = rs.getString("TIPO_ARMA");
        if (tipoArma != null) {
            TipoArma ar = new TipoArma();
            ar.setNombre(tipoArma);
            a.setTipoarma(ar);
        }

        // medio transporte
        String medio = rs.getString("MEDIO_TRANSPORTE");
        if (medio != null) {
            MedioTransporte m = new MedioTransporte();
            m.setNombre(medio);
            a.setMediotransporte(m);
        }

        // alarma más cercana asignada 
        int idAlarma = rs.getInt("ID_ALARMA");
        if (!rs.wasNull()) {
            Alarma al = new Alarma();
            al.setId_alarma(idAlarma);
            al.setNombre(rs.getString("NOMBRE_ALARMA"));
            al.setEstado(EstadoAlarma.valueOf(rs.getString("ESTADO_ALARMA")));
            a.setAlarma(al);
        }

        // dirección
        Direccion d = new Direccion();
        d.setEtapa(rs.getString("ETAPA"));
        d.setSector(rs.getString("SECTOR"));
        d.setManzana(rs.getString("MANZANA"));
        d.setCasa(rs.getString("CASA"));
        d.setCalle(rs.getString("CALLE"));
        d.setCarrera(rs.getString("CARRERA"));
        d.setReferencia(rs.getString("REFERENCIA"));
        d.setLatitud(rs.getDouble("LATITUD"));
        d.setLongitud(rs.getDouble("LONGITUD"));
        a.setDireccion(d);

        return a;
    }
}
