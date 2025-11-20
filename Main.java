import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        int opcion;
        do {
            System.out.println("\n===== ADMINISTRACIÓN DE HORARIOS Y SALONES =====");
            System.out.println("1. Ver todos los salones");
            System.out.println("2. Agregar salón");
            System.out.println("0. Salir");
            System.out.print("Elige una opción: ");
            opcion = leerEntero();

            switch (opcion) {
                case 1:
                    verTodosLosSalones();
                    break;
                case 2:
                    agregarSalon();
                    break;
                case 0:
                    System.out.println("Saliendo...");
                    break;
                default:
                    System.out.println("Opción no válida.");
            }
        } while (opcion != 0);
    }


    private static void verTodosLosSalones() {
        String sql = "SELECT IDSALON, CAPACIDAD, TIPO FROM SALONES ORDER BY IDSALON";

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            System.out.println("\n--- Lista de salones ---");
            System.out.printf("%-8s  %-10s  %-3s%n", "IDSALON", "CAPACIDAD", "TIPO");
            System.out.println("---------------------------------");

            boolean hay = false;
            while (rs.next()) {
                hay = true;
                String id = rs.getString("IDSALON");
                int cap = rs.getInt("CAPACIDAD");
                String tipo = rs.getString("TIPO");
                System.out.printf("%-8s  %-10d  %-3s%n", id, cap, tipo);
            }

            if (!hay) {
                System.out.println("No hay salones registrados.");
            }

        } catch (SQLException e) {
            System.err.println("Error al consultar salones: " + e.getMessage());
        }
    }


    private static void agregarSalon() {
        System.out.print("ID del salón (ej. IA104): ");
        String id = scanner.nextLine().trim();

        System.out.print("Capacidad: ");
        int capacidad = leerEntero();

        System.out.print("Tipo (C = curso, SC = sala cómputo, A = auditorio): ");
        String tipo = scanner.nextLine().trim().toUpperCase();

        String sql = "INSERT INTO SALONES (IDSALON, CAPACIDAD, TIPO) VALUES (?, ?, ?)";

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            stmt.setInt(2, capacidad);
            stmt.setString(3, tipo);

            int filas = stmt.executeUpdate();
            if (filas > 0) {
                System.out.println("Salón agregado correctamente.");
            } else {
                System.out.println("No se pudo agregar el salón.");
            }

        } catch (SQLException e) {
            System.err.println("Error al agregar salón: " + e.getMessage());
        }
    }

    private static int leerEntero() {
        while (!scanner.hasNextInt()) {
            System.out.print("Introduce un número válido: ");
            scanner.next();
        }
        int val = scanner.nextInt();
        scanner.nextLine();
        return val;
    }
}
