package edu.fje.daw2;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int opcion = 0;

        do {
            System.out.println("\n--- MENÚ PRINCIPAL HAVANA RENTAL CARS ---");
            System.out.println("==========================================");

            System.out.println("1. GESTIÓ DE CLIENTS");
            System.out.println("2. GESTIÓ DE COTXES");
            System.out.println("3. GESTIÓ DE LLOGUERS");
            System.out.println("0. SORTIR");
            System.out.print("Selecciona una opció: ");

            String entrada = scanner.nextLine();

            try {
                opcion = Integer.parseInt(entrada);

                switch (opcion) {
                    case 1:
                        menuClients(scanner);
                        break;
                    case 2:
                        menuCotxes(scanner);
                        break;
                    case 3:
                        menuLloguers(scanner);
                        break;
                    case 0:
                        System.out.println("Sortint de l'aplicació...");
                        break;
                    default:
                        System.out.println("Opció no vàlida. Introdueix un número del 0 al 3.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: Has d'introduir un número.");
            }

        } while (opcion != 0);

        scanner.close();
    }

    private static void menuClients(Scanner scanner) {
        int opcio;
        do {
            System.out.println("\n--- GESTIÓ DE CLIENTS ---");
            System.out.println("1. Llistar clients");
            System.out.println("2. Afegir client");
            System.out.println("3. Eliminar client");
            System.out.println("4. Modificar client");
            System.out.println("5. Buscar client per DNI");
            System.out.println("0. Tornar al menú principal");
            System.out.print("Selecciona una opció: ");

            try {
                opcio = Integer.parseInt(scanner.nextLine());

                switch (opcio) {
                    case 1:
                        Clientes.listarClientes();
                        break;
                    case 2:
                        Clientes.agregarCliente(scanner);
                        break;
                    case 3:
                        Clientes.eliminarCliente(scanner);
                        break;
                    case 4:
                        Clientes.modificarCliente(scanner);
                        break;
                    case 5:
                        Clientes.buscarCliente(scanner);
                        break;
                    case 0:
                        break;
                    default:
                        System.out.println("Opció no vàlida. Introdueix un número del 0 al 5.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: Has d'introduir un número.");
                opcio = -1;
            }
        } while (opcio != 0);
    }

    private static void menuCotxes(Scanner scanner) {
        int opcio;
        do {
            System.out.println("\n--- GESTIÓ DE COTXES ---");
            System.out.println("1. Llistar tots els cotxes");
            System.out.println("2. Afegir cotxe");
            System.out.println("3. Eliminar cotxe");
            System.out.println("4. Modificar cotxe");
            System.out.println("5. Buscar cotxe per matrícula");
            System.out.println("6. Buscar cotxe per tipus");
            System.out.println("7. Buscar cotxe per marca");
            System.out.println("0. Tornar al menú principal");
            System.out.print("Selecciona una opció: ");

            try {
                opcio = Integer.parseInt(scanner.nextLine());

                switch (opcio) {
                    case 1:
                        Coches.listarCoches();
                        break;
                    case 2:
                        Coches.agregarCoche(scanner);
                        break;
                    case 3:
                        Coches.eliminarCoche(scanner);
                        break;
                    case 4:
                        Coches.modificarCoche(scanner);
                        break;
                    case 5:
                        Coches.buscarCochePorMatricula(scanner);
                        break;
                    case 6:
                        Coches.buscarCochePorTipo(scanner);
                        break;
                    case 7:
                        Coches.buscarCochePorMarca(scanner);
                        break;
                    case 0:
                        break;
                    default:
                        System.out.println("Opció no vàlida. Introdueix un número del 0 al 7.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: Has d'introduir un número.");
                opcio = -1;
            }
        } while (opcio != 0);
    }

    private static void menuLloguers(Scanner scanner) {
        int opcio;
        do {
            System.out.println("\n--- GESTIÓ DE LLOGUERS ---");
            System.out.println("1. Registrar nou lloguer");
            System.out.println("2. Llistar lloguers actius");
            System.out.println("3. Registrar devolució");
            System.out.println("4. Llistar històric de lloguers");
            System.out.println("0. Tornar al menú principal");
            System.out.print("Selecciona una opció: ");

            try {
                opcio = Integer.parseInt(scanner.nextLine());

                switch (opcio) {
                    case 1:
                        Lloguers.registrarLloguer(scanner);
                        break;
                    case 2:
                        Lloguers.llistarLloguersActius();
                        break;
                    case 3:
                        Lloguers.registrarDevolucio(scanner);
                        break;
                    case 4:
                        Lloguers.llistarHistoricLloguers();
                        break;
                    case 0:
                        break;
                    default:
                        System.out.println("Opció no vàlida. Introdueix un número del 0 al 4.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: Has d'introduir un número.");
                opcio = -1;
            }
        } while (opcio != 0);
    }
}