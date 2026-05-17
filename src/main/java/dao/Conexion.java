/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author Lenovo
 */
public class Conexion {

    private static Connection cn;

    private static final String URL
            = "jdbc:oracle:thin:@localhost:1521/xepdb1";

    private static final String USER = "usrwolertapp";
    private static final String PASS = "usrwolertapp";

    public static Connection getConexion() {
        try {
            if (cn == null || cn.isClosed()) {
                Class.forName("oracle.jdbc.driver.OracleDriver");
                cn = DriverManager.getConnection(URL, USER, PASS);
                System.out.println("Conexión exitosa a Oracle");
            }
        } catch (ClassNotFoundException e) {
            System.out.println("Driver no encontrado: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Error de conexión: " + e.getMessage());
        }
        return cn;
    }

    public static void cerrarConexion() {
        try {
            if (cn != null && !cn.isClosed()) {
                cn.close();
                System.out.println("Conexión cerrada");
            }
        } catch (SQLException e) {
            System.out.println("Error al cerrar: " + e.getMessage());
        }
    }
}
