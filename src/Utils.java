import java.util.Scanner;

public class Utils {

    private static final Scanner scanner = new Scanner(System.in);

    public static int leerEntero(String mensaje) {
        System.out.print(mensaje);
        while (!scanner.hasNextInt()) {
            System.out.print("Introduce un número válido: ");
            scanner.next();
        }
        int val = scanner.nextInt();
        scanner.nextLine(); // limpia salto de línea
        return val;
    }

    public static String leerLinea(String mensaje) {
        System.out.print(mensaje);
        return scanner.nextLine();
    }
}
