import java.util.List;
import java.util.Scanner;
import java.util.function.DoubleUnaryOperator;
import javax.swing.*;

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

        System.out.print("Ingresa tolerancia (ej. 1e-6): ");
        double tol = scanner.nextDouble();

        System.out.print("Ingresa número máximo de iteraciones: ");
        int maxIter = scanner.nextInt();

        try {
            DoubleUnaryOperator f = Funcion.crearFuncion(expresion);
            List<Biseccion.Iteracion> iteraciones = Biseccion.biseccion(a, b, tol, f, maxIter);

            Biseccion.mostrarIteraciones(iteraciones);

            SwingUtilities.invokeLater(() -> {
                JFrame frame = new JFrame("Bisección - " + expresion);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(900, 600);

                GraficoPanel graficoPanel = new GraficoPanel(iteraciones, f, expresion);
                frame.add(graficoPanel);

                frame.setVisible(true);
            });

        } catch (IllegalArgumentException ex) {
            System.err.println("Error: " + ex.getMessage());
        } finally {
            scanner.close();
        }

        System.out.println("Programa terminado.");
    }
}