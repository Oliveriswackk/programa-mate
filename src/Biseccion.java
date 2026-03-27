import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

public class Biseccion {

    public static class Iteracion {
        public final int n;
        public final double a, b, c, fa, fb, fc, error;

        public Iteracion(int n, double a, double b, double c, double fa, double fb, double fc, double error) {
            this.n = n; this.a = a; this.b = b; this.c = c;
            this.fa = fa; this.fb = fb; this.fc = fc; this.error = error;
        }
    }

    public static List<Iteracion> biseccion(double a, double b, double tol, DoubleUnaryOperator f, int maxIter) {
        List<Iteracion> iteraciones = new ArrayList<>();
        double fa = f.applyAsDouble(a);
        double fb = f.applyAsDouble(b);

        if (fa * fb > 0) throw new IllegalArgumentException("No hay cambio de signo en [" + a + "," + b + "]");

        double c = a;
        double error = Double.MAX_VALUE;
        int iter = 0;

        while (error > tol && iter < maxIter) {
            c = (a + b) / 2.0;
            double fc = f.applyAsDouble(c);
            error = (iter == 0) ? Math.abs(b - a) : Math.abs(b - a) / 2.0;

            iteraciones.add(new Iteracion(iter + 1, a, b, c, fa, fb, fc, error));

            if (fa * fc < 0) { b = c; fb = fc; } else { a = c; fa = fc; }
            iter++;
        }
        return iteraciones;
    }

    public static void mostrarIteraciones(List<Iteracion> iteraciones) {
        System.out.println("\nIteración |      a      |      b      |      c      |     f(c)     |   error");
        System.out.println("-------------------------------------------------------------------------------");
        for (Iteracion it : iteraciones) {
            System.out.printf("%9d | %11.6f | %11.6f | %11.6f | %12.6e | %9.6e\n",
                    it.n, it.a, it.b, it.c, it.fc, it.error);
        }
    }
}