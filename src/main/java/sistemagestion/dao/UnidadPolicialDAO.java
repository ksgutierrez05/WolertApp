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
import sistemagestion.model.EstadoUnidadPolicial;
import sistemagestion.model.UnidadPolicial;

/**
 *
 * @author Lenovo
 */
public class UnidadPolicialDAO {

    private Connection con() throws SQLException {
        return ConexionDB.getInstancia().getConexion();
    }

    public UnidadPolicialDAO() throws SQLException {

    }

    // pr_insertar_unidad(nombre, estado, nombre_barrio)
    public boolean insertar(String nombre, String estado, String nombreBarrio,
            double latitud, double longitud) {
        String sql = "{call pkg_alertas.pr_insertar_unidad(?, ?, ?, ?, ?)}";
        try (CallableStatement cs = con().prepareCall(sql)) {
            cs.setString(1, nombre);
            cs.setString(2, estado);
            cs.setString(3, nombreBarrio);
            cs.setDouble(4, latitud);
            cs.setDouble(5, longitud);
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.out.println("Error insertar unidad: " + e.getMessage());
            return false;
        }
    }

    // pr_actualizar_unidad(nombre_actual, nombre_nuevo, estado, nombre_barrio)
    public boolean actualizar(
            String nombreActual,
            String nombreNuevo,
            String estado,
            String nombreBarrio
    ) {
        String sql = "{call pkg_alertas.pr_actualizar_unidad(?, ?, ?, ?)}";
        try (CallableStatement cs = con().prepareCall(sql)) {
            cs.setString(1, nombreActual);
            cs.setString(2, nombreNuevo);
            cs.setString(3, estado);
            cs.setString(4, nombreBarrio);
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.out.println("Error actualizar unidad: " + e.getMessage());
            return false;
        }
    }

    // pr_eliminar_unidad
    public boolean eliminar(String nombre) {
        String sql = "{call pkg_alertas.pr_eliminar_unidad(?)}";
        try (CallableStatement cs = con().prepareCall(sql)) {
            cs.setString(1, nombre);
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.out.println("Error eliminar unidad: " + e.getMessage());
            return false;
        }
    }

    // pr_listar_unidades
    public List<UnidadPolicial> listar() {
        List<UnidadPolicial> lista = new ArrayList<>();
        String sql = "{call pkg_alertas.pr_listar_unidades(?)}";
        try (CallableStatement cs = con().prepareCall(sql)) {
            cs.registerOutParameter(1, OracleTypes.CURSOR);
            cs.execute();
            ResultSet rs = (ResultSet) cs.getObject(1);
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error listar unidades: " + e.getMessage());
        }
        return lista;
    }

    // pr_buscar_unidad — búsqueda parcial por nombre o barrio
    public List<UnidadPolicial> buscar(String texto) {
        List<UnidadPolicial> lista = new ArrayList<>();
        String sql = "{call pkg_alertas.pr_buscar_unidad(?, ?)}";
        try (CallableStatement cs = con().prepareCall(sql)) {
            cs.setString(1, texto);
            cs.registerOutParameter(2, OracleTypes.CURSOR);
            cs.execute();
            ResultSet rs = (ResultSet) cs.getObject(2);
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error buscar unidad: " + e.getMessage());
        }
        return lista;
    }

    // pr_buscar_unidad_exacto 
    public List<UnidadPolicial> buscarExacto(String texto) {
        List<UnidadPolicial> lista = new ArrayList<>();
        String sql = "{call pkg_alertas.pr_buscar_unidad_exacto(?, ?)}";
        try (CallableStatement cs = con().prepareCall(sql)) {
            cs.setString(1, texto);
            cs.registerOutParameter(2, OracleTypes.CURSOR);
            cs.execute();
            ResultSet rs = (ResultSet) cs.getObject(2);
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error buscar unidad exacto: " + e.getMessage());
        }
        return lista;
    }

    // cursor retorna: NOMBRE, ESTADO, BARRIO, COMUNA
    private UnidadPolicial mapear(ResultSet rs) throws SQLException {
        UnidadPolicial u = new UnidadPolicial();
        u.setNombre(rs.getString("NOMBRE"));
        u.setEstado(EstadoUnidadPolicial.valueOf(rs.getString("ESTADO")));
        u.setLatitud(rs.getDouble("LATITUD"));
        u.setLongitud(rs.getDouble("LONGITUD"));

        Comuna c = new Comuna();
        c.setNombre(rs.getString("COMUNA"));

        Barrio b = new Barrio();
        b.setNombre(rs.getString("BARRIO"));
        b.setComuna(c);

        u.setBarrio(b);
        return u;
    }
}
