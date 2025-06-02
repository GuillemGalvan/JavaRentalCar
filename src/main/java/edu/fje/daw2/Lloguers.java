package edu.fje.daw2;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Lloguers {
    private static final Pattern DNI_PATTERN = Pattern.compile("^[0-9]{8}[A-Za-z]$");
    private static final Pattern MATRICULA_PATTERN = Pattern.compile("^[0-9A-Z]{4}[A-Z]{3}$");
    private static final Pattern SI_NO_PATTERN = Pattern.compile("^[SsNn]$");

    // En la clase Clientes.java
    public static void listarClientesDisponibles() {
        Connection conn = ConexionSQL.conectar();
        if (conn != null) {
            try {
                String query = "SELECT dni, nom, cognoms FROM clientes";
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query);

                System.out.println("\n--- CLIENTS DISPONIBLES ---");
                while (rs.next()) {
                    System.out.println("DNI: " + rs.getString("dni") +
                            " | Nom: " + rs.getString("nom") +
                            " " + rs.getString("cognoms"));
                }

                rs.close();
                stmt.close();
                conn.close();
            } catch (SQLException e) {
                System.out.println("Error en la consulta: " + e.getMessage());
            }
        }
    }

    // En la clase Coches.java
    public static void listarCochesDisponibles() {
        Connection conn = ConexionSQL.conectar();
        if (conn != null) {
            try {
                String query = "SELECT matricula, marca, model FROM cotxes " +
                        "WHERE matricula NOT IN " +
                        "(SELECT matricula_cotxe FROM lloguers WHERE finalitzat = FALSE)";

                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query);

                System.out.println("\n--- COTXES DISPONIBLES ---");
                while (rs.next()) {
                    System.out.println("Matrícula: " + rs.getString("matricula") +
                            " | Marca: " + rs.getString("marca") +
                            " | Model: " + rs.getString("model"));
                }

                rs.close();
                stmt.close();
                conn.close();
            } catch (SQLException e) {
                System.out.println("Error en la consulta: " + e.getMessage());
            }
        }
    }

    public static void registrarLloguer(Scanner scanner) {
        Connection conn = ConexionSQL.conectar();
        if (conn != null) {
            try {
                // Mostrar clientes disponibles (DNI y nombre)
                System.out.println("\n--- CLIENTES DISPONIBLES PARA LLOGUER ---");
                String queryClientes = "SELECT dni, nom, cognoms FROM clientes";
                Statement stmtClientes = conn.createStatement();
                ResultSet rsClientes = stmtClientes.executeQuery(queryClientes);

                while (rsClientes.next()) {
                    System.out.println("DNI: " + rsClientes.getString("dni") +
                            " | Nombre: " + rsClientes.getString("nom") +
                            " " + rsClientes.getString("cognoms"));
                }
                rsClientes.close();
                stmtClientes.close();

                // Selección del cliente
                String dniClient;
                do {
                    System.out.print("\nIntroduce el DNI del cliente: ");
                    dniClient = scanner.nextLine();
                    if (!DNI_PATTERN.matcher(dniClient).matches()) {
                        System.out.println("Error: Formato de DNI incorrecto. Debe tener 8 números y una letra.");
                    }
                } while (!DNI_PATTERN.matcher(dniClient).matches());

                if (!existeClient(conn, dniClient)) {
                    System.out.println("Error: No existe ningún cliente con ese DNI.");
                    conn.close();
                    return;
                }

                // Mostrar coches disponibles (matrícula y modelo)
                System.out.println("\n--- COCHES DISPONIBLES PARA LLOGUER ---");
                String queryCoches = "SELECT matricula, marca, model FROM cotxes " +
                        "WHERE matricula NOT IN " +
                        "(SELECT matricula_cotxe FROM lloguers WHERE finalitzat = FALSE)";
                Statement stmtCoches = conn.createStatement();
                ResultSet rsCoches = stmtCoches.executeQuery(queryCoches);

                while (rsCoches.next()) {
                    System.out.println("Matrícula: " + rsCoches.getString("matricula") +
                            " | Modelo: " + rsCoches.getString("marca") +
                            " " + rsCoches.getString("model"));
                }
                rsCoches.close();
                stmtCoches.close();

                // Selección del coche
                String matricula;
                do {
                    System.out.print("\nIntroduce la matrícula del coche: ");
                    matricula = scanner.nextLine().toUpperCase();
                    if (!MATRICULA_PATTERN.matcher(matricula).matches()) {
                        System.out.println("Error: Formato de matrícula incorrecto. Debe ser 4 números + 3 letras.");
                    }
                } while (!MATRICULA_PATTERN.matcher(matricula).matches());

                if (!existeCotxe(conn, matricula)) {
                    System.out.println("Error: No existe ningún coche con esa matrícula.");
                    conn.close();
                    return;
                }

                if (!cotxeDisponible(conn, matricula)) {
                    System.out.println("Error: El coche no está disponible para alquilar.");
                    conn.close();
                    return;
                }

                // Fecha de inicio
                LocalDate dataPrestec = null;
                boolean fechaValida = false;
                while (!fechaValida) {
                    try {
                        System.out.print("\nFecha de inicio del lloguer (YYYY-MM-DD): ");
                        dataPrestec = LocalDate.parse(scanner.nextLine());
                        fechaValida = true;
                    } catch (DateTimeParseException e) {
                        System.out.println("Error: Formato de fecha incorrecto. Usa YYYY-MM-DD.");
                    }
                }

                // Días de alquiler
                int diesLloguer = 0;
                boolean diesValids = false;
                while (!diesValids) {
                    System.out.print("Número de días de alquiler: ");
                    try {
                        diesLloguer = Integer.parseInt(scanner.nextLine());
                        if (diesLloguer <= 0) {
                            System.out.println("Error: El número de días debe ser mayor que 0.");
                        } else {
                            diesValids = true;
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Error: Introduce un número válido.");
                    }
                }

                // Precio por día
                double preuDia = 0;
                boolean preuValid = false;
                while (!preuValid) {
                    System.out.print("Precio por día (€): ");
                    try {
                        preuDia = Double.parseDouble(scanner.nextLine());
                        if (preuDia <= 0) {
                            System.out.println("Error: El precio por día debe ser mayor que 0.");
                        } else {
                            preuValid = true;
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Error: Introduce un número válido.");
                    }
                }

                // Lugar de devolución
                String llocDevolucio;
                do {
                    System.out.print("Lugar de devolución: ");
                    llocDevolucio = scanner.nextLine();
                    if (llocDevolucio.isEmpty()) {
                        System.out.println("Error: El lugar de devolución no puede estar vacío.");
                    }
                } while (llocDevolucio.isEmpty());

                // Estado del depósito
                String dipositInput;
                do {
                    System.out.print("¿El depósito estará lleno en la devolución? (S/N): ");
                    dipositInput = scanner.nextLine();
                    if (!SI_NO_PATTERN.matcher(dipositInput).matches()) {
                        System.out.println("Error: Introduce 'S' para Sí o 'N' para No.");
                    }
                } while (!SI_NO_PATTERN.matcher(dipositInput).matches());
                boolean dipositPle = dipositInput.equalsIgnoreCase("S");

                // Tipo de seguro
                String asseguranca;
                do {
                    System.out.print("Tipo de seguro (1. Con franquicia / 2. Sin franquicia): ");
                    String input = scanner.nextLine();
                    if (input.equals("1")) {
                        asseguranca = "amb franquícia";
                        break;
                    } else if (input.equals("2")) {
                        asseguranca = "sense franquícia";
                        break;
                    } else {
                        System.out.println("Error: Introduce 1 o 2.");
                    }
                } while (true);

                // Calcular precio total
                double preuTotal = diesLloguer * preuDia;

                // Insertar en base de datos
                String insertQuery = "INSERT INTO lloguers (dni_client, matricula_cotxe, data_prestec, dies, " +
                        "preu_per_dia, lloc_devolucio, diposit_ple, asseguranca) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

                PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
                insertStmt.setString(1, dniClient);
                insertStmt.setString(2, matricula);
                insertStmt.setDate(3, Date.valueOf(dataPrestec));
                insertStmt.setInt(4, diesLloguer);
                insertStmt.setDouble(5, preuDia);
                insertStmt.setString(6, llocDevolucio);
                insertStmt.setBoolean(7, dipositPle);
                insertStmt.setString(8, asseguranca);

                int filasAfectadas = insertStmt.executeUpdate();
                if (filasAfectadas > 0) {
                    System.out.println("\n--- LLOGUER REGISTRADO CORRECTAMENTE ---");
                    System.out.println("Cliente DNI: " + dniClient);
                    System.out.println("Vehículo: " + matricula);
                    System.out.println("Fecha inicio: " + dataPrestec);
                    System.out.println("Fecha devolución: " + dataPrestec.plusDays(diesLloguer));
                    System.out.println("Días contratados: " + diesLloguer);
                    System.out.println("Precio por día: " + preuDia + "€");
                    System.out.println("Precio total: " + preuTotal + "€");
                    System.out.println("Lugar devolución: " + llocDevolucio);
                    System.out.println("Depósito lleno: " + (dipositPle ? "Sí" : "No"));
                    System.out.println("Tipo seguro: " + asseguranca);
                } else {
                    System.out.println("Error al registrar el lloguer.");
                }

                insertStmt.close();
                conn.close();
            } catch (SQLException e) {
                System.out.println("Error de base de datos: " + e.getMessage());
            }
        }
    }


    public static void llistarLloguersActius() {
        Connection conn = ConexionSQL.conectar();
        if (conn != null) {
            try {
                String query = "SELECT l.*, c.nom, c.cognoms, c.telefon, co.marca, co.model " +
                        "FROM lloguers l " +
                        "JOIN clientes c ON l.dni_client = c.dni " +
                        "JOIN cotxes co ON l.matricula_cotxe = co.matricula " +
                        "WHERE l.finalitzat = FALSE OR l.finalitzat IS NULL";

                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query);

                System.out.println("\n--- LLOGUERS ACTIUS ---");
                boolean hiHaLloguers = false;

                while (rs.next()) {
                    hiHaLloguers = true;
                    System.out.println("ID Lloguer: " + rs.getInt("id"));
                    System.out.println("Client: " + rs.getString("nom") + " " + rs.getString("cognoms"));
                    System.out.println("Telèfon: " + rs.getString("telefon"));
                    System.out.println("Cotxe: " + rs.getString("marca") + " " + rs.getString("model") +
                            " (Matrícula: " + rs.getString("matricula_cotxe") + ")");
                    System.out.println("Data inici: " + rs.getDate("data_prestec"));
                    System.out.println("Dies contractats: " + rs.getInt("dies"));
                    System.out.println("Preu per dia: " + rs.getDouble("preu_per_dia") + "€");
                    System.out.println("Data devolució prevista: " +
                            rs.getDate("data_prestec").toLocalDate().plusDays(rs.getInt("dies")));
                    System.out.println("Lloc devolució: " + rs.getString("lloc_devolucio"));
                    System.out.println("Assegurança: " + rs.getString("asseguranca"));
                    System.out.println("----------------------------------------");
                }

                if (!hiHaLloguers) {
                    System.out.println("No hi ha lloguers actius en aquest moment.");
                }

                rs.close();
                stmt.close();
                conn.close();
            } catch (SQLException e) {
                System.out.println("Error en la consulta: " + e.getMessage());
            }
        }
    }


    public static void registrarDevolucio(Scanner scanner) {
        Connection conn = ConexionSQL.conectar();
        if (conn != null) {
            try {
                // Mostrar lloguers activos pendientes de devolución
                System.out.println("\n--- LLOGUERS ACTIUS PENDENTS DE DEVOLUCIÓ ---");
                String queryActius = "SELECT l.id, c.nom, c.cognoms, co.matricula, co.marca, co.model " +
                        "FROM lloguers l " +
                        "JOIN clientes c ON l.dni_client = c.dni " +
                        "JOIN cotxes co ON l.matricula_cotxe = co.matricula " +
                        "WHERE l.finalitzat = FALSE";

                Statement stmtActius = conn.createStatement();
                ResultSet rsActius = stmtActius.executeQuery(queryActius);

                boolean hayActivos = false;
                while (rsActius.next()) {
                    hayActivos = true;
                    System.out.println("ID: " + rsActius.getInt("id") +
                            " | Client: " + rsActius.getString("nom") + " " + rsActius.getString("cognoms") +
                            " | Cotxe: " + rsActius.getString("marca") + " " + rsActius.getString("model") +
                            " (" + rsActius.getString("matricula") + ")");
                }

                if (!hayActivos) {
                    System.out.println("No hi ha lloguers actius pendents de devolució.");
                    conn.close();
                    return;
                }

                rsActius.close();
                stmtActius.close();

                // Selección del lloguer a devolver
                int idLloguer = 0;
                boolean idValid = false;
                while (!idValid) {
                    System.out.print("\nIntrodueix l'ID del lloguer a devolver: ");
                    try {
                        idLloguer = Integer.parseInt(scanner.nextLine());
                        if (idLloguer <= 0) {
                            System.out.println("Error: L'ID ha de ser un número positiu.");
                        } else {
                            idValid = true;
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Error: Introdueix un número vàlid.");
                    }
                }

                // Verificar que el lloguer existe y no está ya devuelto
                String queryVerificacion = "SELECT l.*, c.nom, c.cognoms, co.matricula, co.marca, co.model " +
                        "FROM lloguers l " +
                        "JOIN clientes c ON l.dni_client = c.dni " +
                        "JOIN cotxes co ON l.matricula_cotxe = co.matricula " +
                        "WHERE l.id = ?";

                PreparedStatement stmtVerificacion = conn.prepareStatement(queryVerificacion);
                stmtVerificacion.setInt(1, idLloguer);
                ResultSet rsVerificacion = stmtVerificacion.executeQuery();

                if (!rsVerificacion.next()) {
                    System.out.println("Error: No existeix cap lloguer amb aquest ID.");
                    conn.close();
                    return;
                }

                if (rsVerificacion.getBoolean("finalitzat")) {
                    System.out.println("Error: Aquest lloguer ja ha estat devolt.");
                    conn.close();
                    return;
                }

                // Mostrar información del lloguer seleccionado
                System.out.println("\n--- INFORMACIÓ DEL LLOGUER ---");
                System.out.println("ID: " + rsVerificacion.getInt("id"));
                System.out.println("Client: " + rsVerificacion.getString("nom") + " " + rsVerificacion.getString("cognoms"));
                System.out.println("Cotxe: " + rsVerificacion.getString("marca") + " " +
                        rsVerificacion.getString("model") + " (" + rsVerificacion.getString("matricula") + ")");
                System.out.println("Data inici: " + rsVerificacion.getDate("data_prestec"));
                System.out.println("Data devolució prevista: " +
                        rsVerificacion.getDate("data_prestec").toLocalDate().plusDays(rsVerificacion.getInt("dies")));
                System.out.println("Lloc devolució: " + rsVerificacion.getString("lloc_devolucio"));

                // Obtener datos para el cálculo
                LocalDate dataPrestec = rsVerificacion.getDate("data_prestec").toLocalDate();
                int diesContractats = rsVerificacion.getInt("dies");
                double preuDia = rsVerificacion.getDouble("preu_per_dia");
                boolean dipositPrevist = rsVerificacion.getBoolean("diposit_ple");
                String llocDevolucio = rsVerificacion.getString("lloc_devolucio");

                rsVerificacion.close();
                stmtVerificacion.close();

                // Solicitar fecha real de devolución
                LocalDate dataDevolucioReal = null;
                boolean fechaValida = false;
                while (!fechaValida) {
                    try {
                        System.out.print("\nData real de devolució (YYYY-MM-DD): ");
                        dataDevolucioReal = LocalDate.parse(scanner.nextLine());
                        fechaValida = true;
                    } catch (DateTimeParseException e) {
                        System.out.println("Error: Format de data incorrecte. Utilitza YYYY-MM-DD.");
                    }
                }

                // Solicitar estado del depósito
                String dipositInput;
                do {
                    System.out.print("El dipòsit està ple? (S/N): ");
                    dipositInput = scanner.nextLine();
                    if (!SI_NO_PATTERN.matcher(dipositInput).matches()) {
                        System.out.println("Error: Introdueix 'S' per Sí o 'N' per No.");
                    }
                } while (!SI_NO_PATTERN.matcher(dipositInput).matches());
                boolean dipositPleDevolucio = dipositInput.equalsIgnoreCase("S");

                // Cálculo de penalizaciones
                LocalDate dataDevolucioPrevista = dataPrestec.plusDays(diesContractats);
                long diesRetard = ChronoUnit.DAYS.between(dataDevolucioPrevista, dataDevolucioReal);
                diesRetard = diesRetard > 0 ? diesRetard : 0;

                double penalitzacioRetard = diesRetard * (preuDia * 0.20);
                double penalitzacioDiposit = (!dipositPleDevolucio && dipositPrevist) ? 50.00 : 0.00;
                double penalitzacioTotal = penalitzacioRetard + penalitzacioDiposit;

                // Mostrar resumen
                System.out.println("\n--- RESUM DE DEVOLUCIÓ ---");
                System.out.println("ID Lloguer: " + idLloguer);
                System.out.println("Lloc de devolució: " + llocDevolucio);
                System.out.println("Data devolució prevista: " + dataDevolucioPrevista);
                System.out.println("Data devolució real: " + dataDevolucioReal);
                System.out.println("Dies de retard: " + (diesRetard > 0 ? diesRetard : "Cap"));
                System.out.println("Penalització per retard: " + penalitzacioRetard + "€");
                System.out.println("Penalització per dipòsit: " + penalitzacioDiposit + "€");
                System.out.println("TOTAL PENALITZACIÓ: " + penalitzacioTotal + "€");

                // Actualizar la base de datos
                String updateQuery = "UPDATE lloguers SET " +
                        "data_devolucio_real = ?, " +
                        "diposit_ple_devolucio = ?, " +
                        "penalitzacio = ?, " +
                        "finalitzat = TRUE " +
                        "WHERE id = ?";

                PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                updateStmt.setDate(1, Date.valueOf(dataDevolucioReal));
                updateStmt.setBoolean(2, dipositPleDevolucio);
                updateStmt.setDouble(3, penalitzacioTotal);
                updateStmt.setInt(4, idLloguer);

                int filesAfectades = updateStmt.executeUpdate();
                if (filesAfectades > 0) {
                    System.out.println("\nDevolució registrada correctament.");
                    System.out.println("Total a pagar en concepte de penalització: " + penalitzacioTotal + "€");
                } else {
                    System.out.println("Error al registrar la devolució.");
                }

                updateStmt.close();
                conn.close();
            } catch (SQLException e) {
                System.out.println("Error de base de dades: " + e.getMessage());
            }
        }
    }

    public static void llistarHistoricLloguers() {
        Connection conn = ConexionSQL.conectar();
        if (conn != null) {
            try {
                String query = "SELECT l.*, c.nom, c.cognoms, co.marca, co.model " +
                        "FROM lloguers l " +
                        "JOIN clientes c ON l.dni_client = c.dni " +
                        "JOIN cotxes co ON l.matricula_cotxe = co.matricula " +
                        "WHERE l.finalitzat = TRUE";

                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query);

                System.out.println("\n--- HISTÒRIC DE LLOGUERS ---");
                boolean hiHaLloguers = false;

                while (rs.next()) {
                    hiHaLloguers = true;
                    System.out.println("ID Lloguer: " + rs.getInt("id"));
                    System.out.println("Client: " + rs.getString("nom") + " " + rs.getString("cognoms"));
                    System.out.println("Cotxe: " + rs.getString("marca") + " " + rs.getString("model"));
                    System.out.println("Data inici: " + rs.getDate("data_prestec"));
                    System.out.println("Data devolució: " + rs.getDate("data_devolucio_real"));
                    System.out.println("Dies contractats: " + rs.getInt("dies"));
                    System.out.println("Preu per dia: " + rs.getDouble("preu_per_dia") + "€");
                    System.out.println("Penalització: " + rs.getDouble("penalitzacio") + "€");
                    System.out.println("Dipòsit ple en devolució: " +
                            (rs.getBoolean("diposit_ple_devolucio") ? "Sí" : "No"));
                    System.out.println("----------------------------------------");
                }

                if (!hiHaLloguers) {
                    System.out.println("No hi ha lloguers finals en l'històric.");
                }

                rs.close();
                stmt.close();
                conn.close();
            } catch (SQLException e) {
                System.out.println("Error en la consulta: " + e.getMessage());
            }
        }
    }

    // Métodos auxiliares (sin cambios)
    private static boolean existeClient(Connection conn, String dni) throws SQLException {
        String query = "SELECT COUNT(*) FROM clientes WHERE dni = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, dni);
        ResultSet rs = stmt.executeQuery();
        rs.next();
        boolean existe = rs.getInt(1) > 0;
        rs.close();
        stmt.close();
        return existe;
    }

    private static boolean existeCotxe(Connection conn, String matricula) throws SQLException {
        String query = "SELECT COUNT(*) FROM cotxes WHERE matricula = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, matricula);
        ResultSet rs = stmt.executeQuery();
        rs.next();
        boolean existe = rs.getInt(1) > 0;
        rs.close();
        stmt.close();
        return existe;
    }

    private static boolean cotxeDisponible(Connection conn, String matricula) throws SQLException {
        String query = "SELECT COUNT(*) FROM lloguers WHERE matricula_cotxe = ? AND finalitzat = FALSE";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, matricula);
        ResultSet rs = stmt.executeQuery();
        rs.next();
        boolean disponible = rs.getInt(1) == 0;
        rs.close();
        stmt.close();
        return disponible;
    }

    private static boolean lloguerExisteix(Connection conn, int idLloguer) throws SQLException {
        String query = "SELECT COUNT(*) FROM lloguers WHERE id = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, idLloguer);
        ResultSet rs = stmt.executeQuery();
        rs.next();
        boolean existe = rs.getInt(1) > 0;
        rs.close();
        stmt.close();
        return existe;
    }

    private static boolean lloguerJaDevold(Connection conn, int idLloguer) throws SQLException {
        String query = "SELECT finalitzat FROM lloguers WHERE id = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, idLloguer);
        ResultSet rs = stmt.executeQuery();
        rs.next();
        boolean devold = rs.getBoolean("finalitzat");
        rs.close();
        stmt.close();
        return devold;
    }
}