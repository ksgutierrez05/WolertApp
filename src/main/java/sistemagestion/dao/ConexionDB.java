/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author Lenovo
 */
public class ConexionDB {

    private static Connection cn;

    private static final String URL
            = "jdbc:oracle:thin:@10.123.30.162:1521/xepdb1";

    private static final String USER = "usrwolertapp";
    private static final String PASS = "usrwolertapp";

    private static ConexionDB instancia;
    private Connection conexion;

    private ConexionDB() throws SQLException {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");

            java.util.Properties props = new java.util.Properties();
            props.setProperty("user", USER);
            props.setProperty("password", PASS);
            props.setProperty("oracle.jdbc.defaultNCharBinding", "true");
            props.setProperty("oracle.net.disableOob", "true");
            this.conexion = DriverManager.getConnection(URL, props);
            this.conexion.setAutoCommit(true);

            try (java.sql.Statement st = this.conexion.createStatement()) {
                st.execute("ALTER SESSION SET NLS_NUMERIC_CHARACTERS = '.,'");
                st.execute("ALTER SESSION SET NLS_LANGUAGE = 'SPANISH'");
                st.execute("ALTER SESSION SET NLS_TERRITORY = 'SPAIN'");

                System.out.println("NLS_NUMERIC_CHARACTERS configurado: punto decimal");
            }


        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver Oracle no encontrado. "
                    + "Agrega ojdbc11.jar a las librerías del proyecto.", e);
        }
    }

    public static ConexionDB getInstancia() throws SQLException {
        if (instancia == null || instancia.conexion.isClosed()
                || !instancia.conexion.isValid(2)) {
            instancia = new ConexionDB();
        }
        return instancia;
    }

    public Connection getConexion() {
        return conexion;
    }

    public void cerrar() throws SQLException {
        if (conexion != null && !conexion.isClosed()) {
            conexion.close();
        }
    }
}
