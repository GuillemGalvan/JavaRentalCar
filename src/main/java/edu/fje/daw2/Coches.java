package edu.fje.daw2;

import java.sql.*;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Coches {
    //Sirve para comprobar que lo que metas en el imput sea correcto
    private static final Pattern MATRICULA_PATTERN = Pattern.compile("^[0-9A-Z]{4}[A-Z]{3}$");
    private static final Pattern COLOR_PATTERN = Pattern.compile("^[a-zA-Z]+$");

    public static void listarCoches() {
        Connection conn = ConexionSQL.conectar();
        if (conn != null) {
            try {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM cotxes");

                System.out.println("\n--- LISTA DE COCHES ---");
                while (rs.next()) {
                    System.out.println("Matrícula: " + rs.getString("matricula"));
                    System.out.println("Marca: " + rs.getString("marca"));
                    System.out.println("Modelo: " + rs.getString("model"));
                    System.out.println("Color: " + rs.getString("color"));
                    System.out.println("Tipo: " + rs.getString("tipus"));
                    System.out.println("---------------------------");
                }

                rs.close();
                stmt.close();
                conn.close();
            } catch (SQLException e) {
                System.out.println("Error en la consulta: " + e.getMessage());
            }
        }
    }

    public static void agregarCoche(Scanner scanner) {
        Connection conn = ConexionSQL.conectar();
        if (conn != null) {
            try {
                String matricula, bastidor, marca, modelo, color, tipus, combustible;
                int places, portes;

                do {
                    System.out.print("Matrícula (ej: 1234ABC): ");
                    matricula = scanner.nextLine().toUpperCase();
                } while (!MATRICULA_PATTERN.matcher(matricula).matches());

                do {
                    System.out.print("Bastidor (17 caracteres): ");
                    bastidor = scanner.nextLine().toUpperCase();
                    if (bastidor.length() != 17) {
                        System.out.println("Error: El bastidor debe tener exactamente 17 caracteres.");
                    }
                } while (bastidor.length() != 17);

                System.out.print("Marca: ");
                marca = scanner.nextLine();

                System.out.print("Modelo: ");
                modelo = scanner.nextLine();

                do {
                    System.out.print("Color (solo letras): ");
                    color = scanner.nextLine();
                } while (!COLOR_PATTERN.matcher(color).matches());

                do {
                    System.out.print("Tipo (SUV, Sedán, Compacto, etc.): ");
                    tipus = scanner.nextLine();
                } while (tipus.isEmpty());

                while (true) {
                    System.out.print("Número de plazas: ");
                    String placesInput = scanner.nextLine();
                    try {
                        places = Integer.parseInt(placesInput);
                        if (places > 0) break;
                        System.out.println("Error: El número de plazas debe ser mayor que 0.");
                    } catch (NumberFormatException e) {
                        System.out.println("Error: Introduce un número válido para las plazas.");
                    }
                }

                while (true) {
                    System.out.print("Número de puertas: ");
                    String portesInput = scanner.nextLine();
                    try {
                        portes = Integer.parseInt(portesInput);
                        if (portes > 0) break;
                        System.out.println("Error: El número de puertas debe ser mayor que 0.");
                    } catch (NumberFormatException e) {
                        System.out.println("Error: Introduce un número válido para las puertas.");
                    }
                }

                while (true) {
                    System.out.print("Combustible (gasolina/dièsel/elèctric): ");
                    combustible = scanner.nextLine().toLowerCase();
                    if (combustible.equals("gasolina") || combustible.equals("dièsel") || combustible.equals("elèctric")) {
                        break;
                    }
                    System.out.println("Error: Debes introducir 'gasolina', 'dièsel' o 'elèctric'.");
                }

                String checkQuery = "SELECT COUNT(*) FROM cotxes WHERE matricula = ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
                checkStmt.setString(1, matricula);
                ResultSet rs = checkStmt.executeQuery();
                rs.next();
                if (rs.getInt(1) > 0) {
                    System.out.println("Error: Ya existe un coche con esa matrícula.");
                    rs.close();
                    checkStmt.close();
                    conn.close();
                    return;
                }
                rs.close();
                checkStmt.close();

                String query = "INSERT INTO cotxes (matricula, bastidor, marca, model, color, tipus, places, portes, combustible) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, matricula);
                stmt.setString(2, bastidor);
                stmt.setString(3, marca);
                stmt.setString(4, modelo);
                stmt.setString(5, color);
                stmt.setString(6, tipus);
                stmt.setInt(7, places);
                stmt.setInt(8, portes);
                stmt.setString(9, combustible);

                int filas = stmt.executeUpdate();
                System.out.println(filas > 0 ? "Coche agregado correctamente." : "Error al agregar coche.");

                stmt.close();
                conn.close();
            } catch (SQLException e) {
                System.out.println("Error: " + e.getMessage());
            }
        } else {
            System.out.println("Error: No se pudo conectar a la base de datos.");
        }
    }



    public static void eliminarCoche(Scanner scanner) {
        Connection conn = ConexionSQL.conectar();
        if (conn != null) {
            try {
                System.out.print("Introduce la matrícula del coche a eliminar: ");
                String matricula = scanner.nextLine().toUpperCase();

                String query = "DELETE FROM cotxes WHERE matricula = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, matricula);

                int filas = stmt.executeUpdate();
                System.out.println(filas > 0 ? "Coche eliminado correctamente." : "No se encontró ningún coche con esa matrícula.");

                stmt.close();
                conn.close();
            } catch (SQLException e) {
                System.out.println("Error al eliminar coche: " + e.getMessage());
            }
        }
    }

    public static void modificarCoche(Scanner scanner) {
        Connection conn = ConexionSQL.conectar();
        if (conn != null) {
            try {
                System.out.print("Introduce la matrícula del coche a modificar: ");
                String matricula = scanner.nextLine().toUpperCase();

                String checkQuery = "SELECT * FROM cotxes WHERE matricula = ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
                checkStmt.setString(1, matricula);
                ResultSet rs = checkStmt.executeQuery();

                if (!rs.next()) {
                    System.out.println("No se encontró ningún coche con esa matrícula.");
                    return;
                }

                System.out.print("Nueva marca (deja en blanco para no cambiar): ");
                String nuevaMarca = scanner.nextLine();

                System.out.print("Nuevo modelo (deja en blanco para no cambiar): ");
                String nuevoModelo = scanner.nextLine();

                System.out.print("Nuevo color (deja en blanco para no cambiar): ");
                String nuevoColor = scanner.nextLine();
                if (!nuevoColor.isEmpty() && !COLOR_PATTERN.matcher(nuevoColor).matches()) {
                    System.out.println("Error: El color solo puede contener letras.");
                    return;
                }

                System.out.print("Nuevo tipo (deja en blanco para no cambiar): ");
                String nuevoTipo = scanner.nextLine();

                String updateQuery = "UPDATE cotxes SET marca = ?, model = ?, color = ?, tipus = ? WHERE matricula = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                updateStmt.setString(1, nuevaMarca.isEmpty() ? rs.getString("marca") : nuevaMarca);
                updateStmt.setString(2, nuevoModelo.isEmpty() ? rs.getString("model") : nuevoModelo);
                updateStmt.setString(3, nuevoColor.isEmpty() ? rs.getString("color") : nuevoColor);
                updateStmt.setString(4, nuevoTipo.isEmpty() ? rs.getString("tipus") : nuevoTipo);
                updateStmt.setString(5, matricula);

                int filas = updateStmt.executeUpdate();
                System.out.println(filas > 0 ? "Coche actualizado correctamente." : "No se pudo modificar el coche.");

                rs.close();
                checkStmt.close();
                updateStmt.close();
                conn.close();
            } catch (SQLException e) {
                System.out.println("Error al modificar coche: " + e.getMessage());
            }
        }
    }


    public static void buscarCochePorMatricula(Scanner scanner) {
        Connection conn = ConexionSQL.conectar();
        if (conn != null) {
            try {
                System.out.print("Introduce la matrícula del coche: ");
                String matricula = scanner.nextLine().toUpperCase();

                String query = "SELECT * FROM cotxes WHERE matricula = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, matricula);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    System.out.println("\n--- COCHE ENCONTRADO ---");
                    System.out.println("Matrícula: " + rs.getString("matricula"));
                    System.out.println("Marca: " + rs.getString("marca"));
                    System.out.println("Modelo: " + rs.getString("model"));
                    System.out.println("Color: " + rs.getString("color"));
                    System.out.println("Tipo: " + rs.getString("tipus"));
                    System.out.println("---------------------------");
                } else {
                    System.out.println("No se encontró ningún coche con esa matrícula.");
                }

                rs.close();
                stmt.close();
                conn.close();
            } catch (SQLException e) {
                System.out.println("Error en la búsqueda: " + e.getMessage());
            }
        }
    }

    public static void buscarCochePorTipo(Scanner scanner) {
        Connection conn = ConexionSQL.conectar();
        if (conn != null) {
            try {
                System.out.print("Introduce el tipo de coche (ej. SUV, Sedán): ");
                String tipo = scanner.nextLine();

                String query = "SELECT * FROM cotxes WHERE tipus = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, tipo);
                ResultSet rs = stmt.executeQuery();

                System.out.println("\n--- COCHES ENCONTRADOS ---");
                boolean encontrado = false;
                while (rs.next()) {
                    encontrado = true;
                    System.out.println("Matrícula: " + rs.getString("matricula"));
                    System.out.println("Marca: " + rs.getString("marca"));
                    System.out.println("Modelo: " + rs.getString("model"));
                    System.out.println("Color: " + rs.getString("color"));
                    System.out.println("---------------------------");
                }

                if (!encontrado) {
                    System.out.println("No se encontraron coches de ese tipo.");
                }

                rs.close();
                stmt.close();
                conn.close();
            } catch (SQLException e) {
                System.out.println("Error en la búsqueda: " + e.getMessage());
            }
        }
    }

    public static void buscarCochePorMarca(Scanner scanner) {
        Connection conn = ConexionSQL.conectar();
        if (conn != null) {
            try {
                System.out.print("Introduce la marca del coche: ");
                String marca = scanner.nextLine();

                String query = "SELECT * FROM cotxes WHERE marca = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, marca);
                ResultSet rs = stmt.executeQuery();

                System.out.println("\n--- COCHES ENCONTRADOS ---");
                boolean encontrado = false;
                while (rs.next()) {
                    encontrado = true;
                    System.out.println("Matrícula: " + rs.getString("matricula"));
                    System.out.println("Modelo: " + rs.getString("model"));
                    System.out.println("Color: " + rs.getString("color"));
                    System.out.println("---------------------------");
                }

                if (!encontrado) {
                    System.out.println("No se encontraron coches de esa marca.");
                }

                rs.close();
                stmt.close();
                conn.close();
            } catch (SQLException e) {
                System.out.println("Error en la búsqueda: " + e.getMessage());
            }
        }
    }
}
