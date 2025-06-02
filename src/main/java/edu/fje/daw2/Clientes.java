package edu.fje.daw2;

import java.sql.*;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Clientes {
    //Sirve para comprobar que lo que metas en el imput sea correcto
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern DNI_PATTERN = Pattern.compile("^[0-9]{8}[A-Za-z]$");
    private static final Pattern TELEFONO_PATTERN = Pattern.compile("^[0-9]+$");

    public static void listarClientes() {
        Connection conn = ConexionSQL.conectar();
        if (conn != null) {
            try {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM clientes");

                System.out.println("\n--- LISTA DE CLIENTES ---");
                while (rs.next()) {
                    mostrarCliente(rs);
                }

                rs.close();
                stmt.close();
                conn.close();
            } catch (SQLException e) {
                System.out.println("Error en la consulta: " + e.getMessage());
            }
        }
    }

    public static void agregarCliente(Scanner scanner) {
        Connection conn = ConexionSQL.conectar();
        if (conn != null) {
            try {
                String dni, nombre, apellidos, telefono, email, direccion, permiso;
                int edad;

                do {
                    System.out.print("DNI (8 números + 1 letra): ");
                    dni = scanner.nextLine();
                } while (!DNI_PATTERN.matcher(dni).matches());

                System.out.print("Nombre: ");
                nombre = scanner.nextLine();

                System.out.print("Apellidos: ");
                apellidos = scanner.nextLine();

                while (true) {
                    System.out.print("Edad (mínimo 18): ");
                    String edadInput = scanner.nextLine();
                    try {
                        edad = Integer.parseInt(edadInput);
                        if (edad >= 18) break;
                        System.out.println("Error: La edad debe ser mayor o igual a 18.");
                    } catch (NumberFormatException e) {
                        System.out.println("Error: Introduce un número válido.");
                    }
                }

                do {
                    System.out.print("Teléfono (solo números): ");
                    telefono = scanner.nextLine();
                } while (!TELEFONO_PATTERN.matcher(telefono).matches());

                do {
                    System.out.print("Correo (formato válido): ");
                    email = scanner.nextLine();
                } while (!EMAIL_PATTERN.matcher(email).matches());

                System.out.print("Dirección: ");
                direccion = scanner.nextLine();

                do {
                    System.out.print("Permiso de Conducir: ");
                    permiso = scanner.nextLine();
                } while (permiso.isEmpty());

                String query = "INSERT INTO clientes (dni, nom, cognoms, edat, telefon, adreça, email, permis_conduccio) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, dni);
                stmt.setString(2, nombre);
                stmt.setString(3, apellidos);
                stmt.setInt(4, edad);
                stmt.setString(5, telefono);
                stmt.setString(6, direccion);
                stmt.setString(7, email);
                stmt.setString(8, permiso);

                int filas = stmt.executeUpdate();
                System.out.println(filas > 0 ? "Cliente agregado correctamente." : "Error al agregar cliente.");

                stmt.close();
                conn.close();
            } catch (SQLException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    public static void modificarCliente(Scanner scanner) {
        Connection conn = ConexionSQL.conectar();
        if (conn != null) {
            try {
                System.out.print("Introduce el DNI del cliente a modificar: ");
                System.out.print("Dejalo vacio para mantenerlo igual ");
                String dni = scanner.nextLine();

                String checkQuery = "SELECT * FROM clientes WHERE dni = ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
                checkStmt.setString(1, dni);
                ResultSet rs = checkStmt.executeQuery();

                if (!rs.next()) {
                    System.out.println("No se encontró ningún cliente con ese DNI.");
                    return;
                }


                System.out.print("Nuevo nombre: ");
                String nuevoNombre = scanner.nextLine();

                System.out.print("Nuevos apellidos: ");
                String nuevosApellidos = scanner.nextLine();

                System.out.print("Nueva edad: ");
                String entradaEdad = scanner.nextLine();
                Integer nuevaEdad = null;
                if (!entradaEdad.isEmpty()) {
                    try {
                        nuevaEdad = Integer.parseInt(entradaEdad);
                        if (nuevaEdad < 18) {
                            System.out.println("Error: la edad debe ser mayor o igual a 18.");
                            return;
                        }
                    } catch (NumberFormatException ex) {
                        System.out.println("Error: edad no válida.");
                        return;
                    }
                }

                System.out.print("Nuevo teléfono: ");
                String nuevoTelefono = scanner.nextLine();
                if (!nuevoTelefono.isEmpty() && !TELEFONO_PATTERN.matcher(nuevoTelefono).matches()) {
                    System.out.println("Error: El teléfono solo puede contener números.");
                    return;
                }

                System.out.print("Nuevo correo: ");
                String nuevoEmail = scanner.nextLine();
                if (!nuevoEmail.isEmpty() && !EMAIL_PATTERN.matcher(nuevoEmail).matches()) {
                    System.out.println("Error: correo no válido.");
                    return;
                }

                System.out.print("Nueva dirección: ");
                String nuevaDireccion = scanner.nextLine();

                System.out.print("Nuevo permiso de conducción: ");
                String nuevoPermiso = scanner.nextLine();


                String updateQuery = "UPDATE clientes SET nom = ?, cognoms = ?, edat = ?, telefon = ?, email = ?, adreça = ?, permis_conduccio = ? WHERE dni = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateQuery);


                updateStmt.setString(1, nuevoNombre.isEmpty() ? rs.getString("nom") : nuevoNombre);
                updateStmt.setString(2, nuevosApellidos.isEmpty() ? rs.getString("cognoms") : nuevosApellidos);
                updateStmt.setInt(3, nuevaEdad == null ? rs.getInt("edat") : nuevaEdad);
                updateStmt.setString(4, nuevoTelefono.isEmpty() ? rs.getString("telefon") : nuevoTelefono);
                updateStmt.setString(5, nuevoEmail.isEmpty() ? rs.getString("email") : nuevoEmail);
                updateStmt.setString(6, nuevaDireccion.isEmpty() ? rs.getString("adreça") : nuevaDireccion);
                updateStmt.setString(7, nuevoPermiso.isEmpty() ? rs.getString("permis_conduccio") : nuevoPermiso);
                updateStmt.setString(8, dni);

                int filas = updateStmt.executeUpdate();
                System.out.println(filas > 0 ? "Cliente modificado correctamente." : "No se pudo modificar el cliente.");


                rs.close();
                checkStmt.close();
                updateStmt.close();
                conn.close();
            } catch (SQLException e) {
                System.out.println("Error al modificar cliente: " + e.getMessage());
            }
        }
    }



    public static void eliminarCliente(Scanner scanner) {
        Connection conn = ConexionSQL.conectar();
        if (conn != null) {
            try {
                System.out.print("DNI del cliente a eliminar: ");
                String dni = scanner.nextLine();

                String query = "DELETE FROM clientes WHERE dni = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, dni);

                int filas = stmt.executeUpdate();
                System.out.println(filas > 0 ? "Cliente eliminado correctamente." : "No se encontró el cliente.");

                stmt.close();
                conn.close();
            } catch (SQLException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    public static void buscarCliente(Scanner scanner) {
        Connection conn = ConexionSQL.conectar();
        if (conn != null) {
            try {
                System.out.print("Introduce el DNI del cliente: ");
                String dni = scanner.nextLine();

                String query = "SELECT * FROM clientes WHERE dni = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, dni);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    mostrarCliente(rs);
                } else {
                    System.out.println("No se encontró ningún cliente con ese DNI.");
                }

                rs.close();
                stmt.close();
                conn.close();
            } catch (SQLException e) {
                System.out.println("Error en la búsqueda: " + e.getMessage());
            }
        }
    }

    private static void mostrarCliente(ResultSet rs) throws SQLException {
        System.out.println("\n--- CLIENTE ENCONTRADO ---");
        System.out.println("DNI: " + rs.getString("dni"));
        System.out.println("Nombre: " + rs.getString("nom"));
        System.out.println("Apellidos: " + rs.getString("cognoms"));
        System.out.println("Edad: " + rs.getInt("edat"));
        System.out.println("Teléfono: " + rs.getString("telefon"));
        System.out.println("Correo: " + rs.getString("email"));
        System.out.println("Dirección: " + rs.getString("adreça"));
        System.out.println("Permiso de Conducir: " + rs.getString("permis_conduccio"));
        System.out.println("---------------------------");
    }
}
