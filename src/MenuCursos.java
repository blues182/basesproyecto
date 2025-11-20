import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MenuCursos {

    public static void mostrar() {
        int opcion;
        do {
            System.out.println("\n===== GESTIÓN DE CURSOS =====");
            System.out.println("1. Ver todos los cursos");
            System.out.println("2. Agregar curso");
            System.out.println("0. Volver al menú principal");

            opcion = Utils.leerEntero("Elige una opción: ");

            switch (opcion) {
                case 1 -> verTodosLosCursos();
                case 2 -> agregarCurso();
                case 0 -> {}
                default -> System.out.println("Opción no válida.");
            }
        } while (opcion != 0);
    }

    private static void verTodosLosCursos() {
        String sql = "SELECT CLAVE, SECC, TITULO, PROF FROM CURSOS ORDER BY CLAVE, SECC";

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            System.out.println("\n--- Lista de cursos ---");
            System.out.printf("%-10s  %-4s  %-30s  %-20s%n", "CLAVE", "SECC", "TITULO", "PROFESOR");
            System.out.println("---------------------------------------------------------------------");

            boolean hay = false;
            while (rs.next()) {
                hay = true;
                String clave = rs.getString("CLAVE");
                int secc = rs.getInt("SECC");
                String titulo = rs.getString("TITULO");
                String prof = rs.getString("PROF");
                System.out.printf("%-10s  %-4d  %-30s  %-20s%n", clave, secc, titulo, prof);
            }

            if (!hay) {
                System.out.println("No hay cursos registrados.");
            }

        } catch (SQLException e) {
            System.err.println("Error al consultar cursos: " + e.getMessage());
        }
    }

    private static void agregarCurso() {
        String clave = Utils.leerLinea("Clave del curso (ej. LIS-2082): ").trim();
        int secc = Utils.leerEntero("Sección (ej. 1): ");
        String titulo = Utils.leerLinea("Título del curso: ").trim();
        String prof = Utils.leerLinea("Nombre del profesor: ").trim();

        String sql = "INSERT INTO CURSOS (CLAVE, SECC, TITULO, PROF) VALUES (?, ?, ?, ?)";

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, clave);
            stmt.setInt(2, secc);
            stmt.setString(3, titulo);
            stmt.setString(4, prof);

            int filas = stmt.executeUpdate();
            if (filas > 0) {
                System.out.println("Curso agregado correctamente.");
            } else {
                System.out.println("No se pudo agregar el curso.");
            }

        } catch (SQLException e) {
            System.err.println("Error al agregar curso: " + e.getMessage());
        }
    }
}
