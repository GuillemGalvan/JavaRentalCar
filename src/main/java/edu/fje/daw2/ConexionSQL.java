package edu.fje.daw2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Esta clase se encarga de establecer la conexión con la base de datos MySQL utilizando JDBC.
 * Es una API de Java que permite conectar aplicaciones Java con bases de datos como MySQL.
 */
public class ConexionSQL {
    private static final String URL = "jdbc:mysql://localhost:3306/havana_rental"; // Usamos la base de datos "havana_rental_cars"
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    public static Connection conectar() {
        try {
            // No es necesario usar Class.forName en JDBC 4 o superior, siempre y cuando el conector esté en el classpath
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.out.println("Error en la conexión: " + e.getMessage());
            return null;
        }
    }
}
