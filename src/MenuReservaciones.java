import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MenuReservaciones {

    public static void mostrar() {
        int op;
        do {
            System.out.println("\n===== RESERVACIONES DE SALONES =====");
            System.out.println("1. Ver ocupación de un salón en un día");
            System.out.println("2. Ver salones libres en un horario");
            System.out.println("3. Hacer reservación puntual");
            System.out.println("4. Cancelar reservación puntual");
            System.out.println("5. Cancelar reservaciones en un intervalo de fechas");
            System.out.println("0. Volver al menú principal");

            op = Utils.leerEntero("Elige una opción: ");

            switch (op) {
                case 1 -> verOcupacionSalonDia();
                case 2 -> verSalonesLibresEnHorario();
                case 3 -> hacerReservacionPuntual();
                case 4 -> cancelarReservacionPuntual();
                case 5 -> cancelarReservacionesIntervalo();
                case 0 -> {}
                default -> System.out.println("Opción no válida.");
            }
        } while (op != 0);
    }

    // ====== 1) Ver ocupación de un salón en un día ======
    private static void verOcupacionSalonDia() {
        String idSalon = Utils.leerLinea("ID del salón (ej. IA104): ").trim();
        String fechaStr = Utils.leerLinea("Fecha (YYYY-MM-DD): ").trim();

        String sql = "SELECT NOMBRE, FECHAHORA, DURACION " +
                "FROM RESERVACIONES " +
                "WHERE IDSALON = ? AND DATE(FECHAHORA) = ? " +
                "ORDER BY FECHAHORA";

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, idSalon);
            stmt.setString(2, fechaStr);

            ResultSet rs = stmt.executeQuery();

            System.out.println("\n--- Ocupación del salón " + idSalon + " el " + fechaStr + " ---");
            boolean hay = false;
            while (rs.next()) {
                hay = true;
                String nombre = rs.getString("NOMBRE");
                java.sql.Timestamp fh = rs.getTimestamp("FECHAHORA");
                int dur = rs.getInt("DURACION");
                System.out.println("- " + fh.toString() + " (" + dur + " min) reservado por " + nombre);
            }

            if (!hay) {
                System.out.println("No hay reservaciones ese día.");
            }

        } catch (SQLException e) {
            System.err.println("Error al consultar ocupación: " + e.getMessage());
        }
    }

    // ====== 2) Ver salones libres en un horario ======
    private static void verSalonesLibresEnHorario() {
        String fechaStr = Utils.leerLinea("Fecha (YYYY-MM-DD): ").trim();
        int hora = Utils.leerEntero("Hora (0-23): ");
        int minuto = Utils.leerEntero("Minuto (0-59): ");
        int duracion = Utils.leerEntero("Duración en minutos: ");

        String iniStr = fechaStr + String.format(" %02d:%02d:00", hora, minuto);

        String sql = "SELECT S.IDSALON, S.CAPACIDAD, S.TIPO " +
                "FROM SALONES S " +
                "WHERE NOT EXISTS ( " +
                "  SELECT 1 FROM RESERVACIONES R " +
                "  WHERE R.IDSALON = S.IDSALON " +
                "    AND R.FECHAHORA < DATE_ADD(?, INTERVAL ? MINUTE) " +
                "    AND DATE_ADD(R.FECHAHORA, INTERVAL R.DURACION MINUTE) > ? " +
                ") " +
                "ORDER BY S.IDSALON";

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            java.sql.Timestamp inicio = java.sql.Timestamp.valueOf(iniStr);

            stmt.setTimestamp(1, inicio);
            stmt.setInt(2, duracion);
            stmt.setTimestamp(3, inicio);

            ResultSet rs = stmt.executeQuery();

            System.out.println("\n--- Salones libres el " + iniStr + " por " + duracion + " minutos ---");
            boolean hay = false;
            while (rs.next()) {
                hay = true;
                String id = rs.getString("IDSALON");
                int cap = rs.getInt("CAPACIDAD");
                String tipo = rs.getString("TIPO");
                System.out.println(id + " (capacidad " + cap + ", tipo " + tipo + ")");
            }

            if (!hay) {
                System.out.println("No hay salones libres en ese horario.");
            }

        } catch (SQLException e) {
            System.err.println("Error al consultar salones libres: " + e.getMessage());
        }
    }

    // ====== 3) Hacer reservación puntual ======
    private static void hacerReservacionPuntual() {
        String idSalon = Utils.leerLinea("ID del salón (ej. IA104): ").trim();
        String nombre = Utils.leerLinea("Nombre de quien reserva: ").trim();
        String fechaStr = Utils.leerLinea("Fecha (YYYY-MM-DD): ").trim();
        int hora = Utils.leerEntero("Hora (0-23): ");
        int minuto = Utils.leerEntero("Minuto (0-59): ");
        int duracion = Utils.leerEntero("Duración en minutos: ");

        String iniStr = fechaStr + String.format(" %02d:%02d:00", hora, minuto);
        java.sql.Timestamp inicio = java.sql.Timestamp.valueOf(iniStr);

        try (Connection conn = ConexionBD.getConnection()) {

            // 1) Verificar que no haya choque
            String sqlCheck = "SELECT 1 FROM RESERVACIONES R " +
                    "WHERE R.IDSALON = ? " +
                    "  AND R.FECHAHORA < DATE_ADD(?, INTERVAL ? MINUTE) " +
                    "  AND DATE_ADD(R.FECHAHORA, INTERVAL R.DURACION MINUTE) > ?";

            try (PreparedStatement stChk = conn.prepareStatement(sqlCheck)) {
                stChk.setString(1, idSalon);
                stChk.setTimestamp(2, inicio);
                stChk.setInt(3, duracion);
                stChk.setTimestamp(4, inicio);

                ResultSet rs = stChk.executeQuery();
                if (rs.next()) {
                    System.out.println("El salón ya está reservado en ese horario. No se puede reservar.");
                    return;
                }
            }

            // 2) Insertar reservación
            String sqlIns = "INSERT INTO RESERVACIONES (IDSALON, NOMBRE, FECHAHORA, DURACION) " +
                    "VALUES (?, ?, ?, ?)";

            try (PreparedStatement stIns = conn.prepareStatement(sqlIns)) {
                stIns.setString(1, idSalon);
                stIns.setString(2, nombre);
                stIns.setTimestamp(3, inicio);
                stIns.setInt(4, duracion);

                int filas = stIns.executeUpdate();
                if (filas > 0) {
                    System.out.println("Reservación realizada correctamente.");
                } else {
                    System.out.println("No se pudo realizar la reservación.");
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al hacer la reservación: " + e.getMessage());
        }
    }

    // ====== 4) Cancelar reservación puntual ======
    private static void cancelarReservacionPuntual() {
        String idSalon = Utils.leerLinea("ID del salón: ").trim();
        String fechaHoraStr = Utils.leerLinea("Fecha y hora exacta de la reservación (YYYY-MM-DD HH:MM:SS): ").trim();

        String sql = "DELETE FROM RESERVACIONES WHERE IDSALON = ? AND FECHAHORA = ?";

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            java.sql.Timestamp fh = java.sql.Timestamp.valueOf(fechaHoraStr);

            stmt.setString(1, idSalon);
            stmt.setTimestamp(2, fh);

            int filas = stmt.executeUpdate();
            if (filas > 0) {
                System.out.println("Reservación cancelada correctamente.");
            } else {
                System.out.println("No se encontró una reservación con esos datos.");
            }

        } catch (SQLException e) {
            System.err.println("Error al cancelar reservación: " + e.getMessage());
        }
    }

    // ====== 5) Cancelar reservaciones en intervalo de fechas ======
    private static void cancelarReservacionesIntervalo() {
        String idSalon = Utils.leerLinea("ID del salón (o * para todos): ").trim();
        String iniStr = Utils.leerLinea("Fecha/hora inicio (YYYY-MM-DD HH:MM:SS): ").trim();
        String finStr = Utils.leerLinea("Fecha/hora fin (YYYY-MM-DD HH:MM:SS): ").trim();

        String sql;
        boolean filtrarSalon = !idSalon.equals("*");

        if (filtrarSalon) {
            sql = "DELETE FROM RESERVACIONES " +
                    "WHERE IDSALON = ? " +
                    "  AND FECHAHORA >= ? " +
                    "  AND FECHAHORA <= ?";
        } else {
            sql = "DELETE FROM RESERVACIONES " +
                    "WHERE FECHAHORA >= ? " +
                    "  AND FECHAHORA <= ?";
        }

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            java.sql.Timestamp ini = java.sql.Timestamp.valueOf(iniStr);
            java.sql.Timestamp fin = java.sql.Timestamp.valueOf(finStr);

            int index = 1;
            if (filtrarSalon) {
                stmt.setString(index++, idSalon);
            }
            stmt.setTimestamp(index++, ini);
            stmt.setTimestamp(index, fin);

            int filas = stmt.executeUpdate();
            System.out.println("Reservaciones canceladas: " + filas);

        } catch (SQLException e) {
            System.err.println("Error al cancelar reservaciones: " + e.getMessage());
        }
    }
}

