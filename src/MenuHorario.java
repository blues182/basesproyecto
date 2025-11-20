import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MenuHorario {

    public static void mostrar() {
        int opcion;
        do {
            System.out.println("\n===== GESTIÓN DE HORARIO =====");
            System.out.println("1. Ver horario por período");
            System.out.println("2. Agregar entrada al horario");
            System.out.println("3. Eliminar curso y su horario (TRANSACCIÓN)");
            System.out.println("0. Volver al menú principal");

            opcion = Utils.leerEntero("Elige una opción: ");

            switch (opcion) {
                case 1 -> verHorarioPorPeriodo();
                case 2 -> agregarEntradaHorario();
                case 3 -> eliminarCursoYHorarioTransaccion();
                case 0 -> {}
                default -> System.out.println("Opción no válida.");
            }
        } while (opcion != 0);
    }

    private static void verHorarioPorPeriodo() {
        String periodo = Utils.leerLinea("Título del período (ej. PRIMAVERA-25): ").trim();

        String sql = "SELECT H.CLAVE, H.SECC, H.DIASEM, H.HORA, H.MINUTO, H.DURACION, " +
                "H.SEMESTRE, H.IDSALON, C.TITULO " +
                "FROM HORARIO H " +
                "JOIN CURSOS C ON H.CLAVE = C.CLAVE AND H.SECC = C.SECC " +
                "WHERE H.PERIODO = ? " +
                "ORDER BY H.DIASEM, H.HORA, H.MINUTO";

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, periodo);
            ResultSet rs = stmt.executeQuery();

            System.out.println("\n--- Horario para el período " + periodo + " ---");
            System.out.printf("%-10s %-4s %-3s %-5s %-8s %-8s %-8s %-30s%n",
                    "CLAVE", "SECC", "DIA", "HORA", "MIN", "DUR", "SALON", "TITULO");
            System.out.println("-------------------------------------------------------------------------------");

            boolean hay = false;
            while (rs.next()) {
                hay = true;
                String clave = rs.getString("CLAVE");
                int secc = rs.getInt("SECC");
                int dia = rs.getInt("DIASEM");
                int hora = rs.getInt("HORA");
                int min = rs.getInt("MINUTO");
                int dur = rs.getInt("DURACION");
                String salon = rs.getString("IDSALON");
                int sem = rs.getInt("SEMESTRE");
                String titulo = rs.getString("TITULO");

                System.out.printf("%-10s %-4d %-3d %02d:%02d %-8d %-8s %-30s%n",
                        clave, secc, dia, hora, min, dur, salon, titulo + " (Sem " + sem + ")");
            }

            if (!hay) {
                System.out.println("No hay entradas de horario para ese período.");
            }

        } catch (SQLException e) {
            System.err.println("Error al consultar horario: " + e.getMessage());
        }
    }

    private static void agregarEntradaHorario() {
        String clave = Utils.leerLinea("Clave del curso (ej. LIS-2082): ").trim();
        int secc = Utils.leerEntero("Sección: ");
        int dia = Utils.leerEntero("Día de la semana (1=Lunes ... 7=Domingo): ");
        int hora = Utils.leerEntero("Hora (0-23): ");
        int minuto = Utils.leerEntero("Minuto (0-59): ");
        int duracion = Utils.leerEntero("Duración en minutos: ");
        String periodo = Utils.leerLinea("Período (ej. PRIMAVERA-25): ").trim();
        int semestre = Utils.leerEntero("Semestre (1-9): ");
        String idSalon = Utils.leerLinea("ID del salón (ej. IA104): ").trim();

        String sql = "INSERT INTO HORARIO " +
                "(CLAVE, SECC, DIASEM, HORA, MINUTO, DURACION, PERIODO, SEMESTRE, IDSALON) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, clave);
            stmt.setInt(2, secc);
            stmt.setInt(3, dia);
            stmt.setInt(4, hora);
            stmt.setInt(5, minuto);
            stmt.setInt(6, duracion);
            stmt.setString(7, periodo);
            stmt.setInt(8, semestre);
            stmt.setString(9, idSalon);

            int filas = stmt.executeUpdate();
            if (filas > 0) {
                System.out.println("Entrada de horario agregada correctamente.");
            } else {
                System.out.println("No se pudo agregar la entrada de horario.");
            }

        } catch (SQLException e) {
            System.err.println("Error al agregar entrada de horario: " + e.getMessage());
        }
    }

    private static void eliminarCursoYHorarioTransaccion() {
        String clave = Utils.leerLinea("Clave del curso a eliminar: ").trim();
        int secc = Utils.leerEntero("Sección del curso a eliminar: ");

        try (Connection conn = ConexionBD.getConnection()) {
            conn.setAutoCommit(false);

            try {
                String sqlHorario = "DELETE FROM HORARIO WHERE CLAVE = ? AND SECC = ?";
                try (PreparedStatement stmtHor = conn.prepareStatement(sqlHorario)) {
                    stmtHor.setString(1, clave);
                    stmtHor.setInt(2, secc);
                    int filasHor = stmtHor.executeUpdate();
                    System.out.println("Entradas de horario eliminadas: " + filasHor);
                }

                String sqlCurso = "DELETE FROM CURSOS WHERE CLAVE = ? AND SECC = ?";
                int filasCurso;
                try (PreparedStatement stmtCur = conn.prepareStatement(sqlCurso)) {
                    stmtCur.setString(1, clave);
                    stmtCur.setInt(2, secc);
                    filasCurso = stmtCur.executeUpdate();
                }

                if (filasCurso == 0) {
                    System.out.println("No se encontró el curso. Se cancela la operación.");
                    conn.rollback();
                } else {
                    conn.commit();
                    System.out.println("Curso y su horario eliminados correctamente (TRANSACCIÓN OK).");
                }

            } catch (SQLException e) {
                System.err.println("Error durante la transacción: " + e.getMessage());
                conn.rollback();
                System.out.println("Se hizo ROLLBACK. No se aplicaron cambios.");
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            System.err.println("Error al conectar para transacción: " + e.getMessage());
        }
    }
}
