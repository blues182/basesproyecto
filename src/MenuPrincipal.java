public class MenuPrincipal {

    public static void mostrar() {
        int opcion;
        do {
            System.out.println("\n===== ADMINISTRACIÓN DE HORARIOS Y SALONES =====");
            System.out.println("1. Gestión de salones");
            System.out.println("2. Gestión de cursos");
            System.out.println("3. Gestión de períodos");
            System.out.println("4. Gestión de horario");
            System.out.println("5. Gestión de reservaciones");
            System.out.println("0. Salir");

            opcion = Utils.leerEntero("Elige una opción: ");

            switch (opcion) {
                case 1 -> MenuSalones.mostrar();
                case 2 -> MenuCursos.mostrar();
                case 3 -> MenuPeriodos.mostrar();
                case 4 -> MenuHorario.mostrar();
                case 5 -> MenuReservaciones.mostrar();
                case 0 -> System.out.println("Saliendo...");
                default -> System.out.println("Opción no válida.");
            }
        } while (opcion != 0);
    }
}
