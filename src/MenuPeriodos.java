import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MenuPeriodos {

    public static void mostrar() {
        int opcion;
        do {
            System.out.println("\n===== GESTIÓN DE PERÍODOS =====");
            System.out.println("1. Ver todos los períodos");
            System.out.println("2. Agregar período");
            System.out.println("0. Volver al menú principal");

            opcion = Utils.leerEntero("Elige una opción: ");

            switch (opcion) {
                case 1 -> verTodosLosPeriodos();
                case 2 -> agregarPeriodo();
                case 0 -> {}
                default -> System.out.println("Opción no válida.");
            }
        } while (opcion != 0);
    }

    private static void verTodosLosPeriodos() {
        String sql = "SELECT TITULO, FECHAINICIO, FECHAFIN FROM PERIODOS ORDER BY FECHAINICIO";

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            System.out.println("\n--- Lista de períodos ---");
            System.out.printf("%-20s  %-12s  %-12s%n", "TITULO", "INICIO", "FIN");
            System.out.println("----------------------------------------------------");

            boolean hay = false;
            while (rs.next()) {
                hay = true;
                String titulo = rs.getString("TITULO");
                java.sql.Date ini = rs.getDate("FECHAINICIO");
                java.sql.Date fin = rs.getDate("FECHAFIN");
                System.out.printf("%-20s  %-12s  %-12s%n", titulo, ini.toString(), fin.toString());
            }

            if (!hay) {
                System.out.println("No hay períodos registrados.");
            }

        } catch (SQLException e) {
            System.err.println("Error al consultar períodos: " + e.getMessage());
        }
    }

    private static void agregarPeriodo() {
        String titulo = Utils.leerLinea("Título del período (ej. PRIMAVERA-25): ").trim();
        String iniStr = Utils.leerLinea("Fecha de inicio (YYYY-MM-DD): ").trim();
        String finStr = Utils.leerLinea("Fecha de fin (YYYY-MM-DD): ").trim();

        String sql = "INSERT INTO PERIODOS (TITULO, FECHAINICIO, FECHAFIN) VALUES (?, ?, ?)";

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, titulo);
            stmt.setDate(2, java.sql.Date.valueOf(iniStr));
            stmt.setDate(3, java.sql.Date.valueOf(finStr));

            int filas = stmt.executeUpdate();
            if (filas > 0) {
                System.out.println("Período agregado correctamente.");
            } else {
                System.out.println("No se pudo agregar el período.");
            }

        } catch (SQLException e) {
            System.err.println("Error al agregar período: " + e.getMessage());
        }
    }
}
