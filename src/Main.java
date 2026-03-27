import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Método de la Bisección");
        System.out.println("Ingresa función f(x). Ejemplos: x^3-4*x-9, cos(x)-x, exp(-x)-x");
        System.out.print("f(x) = ");
        String expresion = scanner.nextLine();

        System.out.print("Ingresa extremo izquierdo a: ");
        double a = scanner.nextDouble();

        System.out.print("Ingresa extremo derecho b: ");
        double b = scanner.nextDouble();

        System.out.print("Ingresa tolerancia (ej. 0.0004): ");
        double tol = scanner.nextDouble();

        try {
            java.util.function.DoubleUnaryOperator f = Funcion.crearFuncion(expresion);
            List<Biseccion.Iteracion> iteraciones = Biseccion.biseccion(a, b, tol, f);

            Biseccion.mostrarIteraciones(iteraciones);
            Biseccion.graficar(iteraciones, f, expresion);

            if (!iteraciones.isEmpty()) {
                System.out.printf("\nRaíz final calculada: %.10f\n", iteraciones.get(iteraciones.size() - 1).c);
                System.out.println("Error estimado: " + iteraciones.get(iteraciones.size() - 1).error);
            }
        } catch (IllegalArgumentException ex) {
            System.err.println("Error: " + ex.getMessage());
        } finally {
            scanner.close();
        }

        System.out.println("Programa terminado.");
    }
}
