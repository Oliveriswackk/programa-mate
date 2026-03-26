import java.util.ArrayList;
import java.util.List;

public class Biseccion {

    public static class Iteracion {
        public final int n;
        public final double a;
        public final double b;
        public final double c;
        public final double fa;
        public final double fb;
        public final double fc;
        public final double error;

        public Iteracion(int n, double a, double b, double c, double fa, double fb, double fc, double error) {
            this.n = n;
            this.a = a;
            this.b = b;
            this.c = c;
            this.fa = fa;
            this.fb = fb;
            this.fc = fc;
            this.error = error;
        }
    }

    public static List<Iteracion> biseccion(double a, double b, double tol, int maxIter, java.util.function.DoubleUnaryOperator f) {
        List<Iteracion> iteraciones = new ArrayList<>();

        double fa = f.applyAsDouble(a);
        double fb = f.applyAsDouble(b);

        if (fa * fb > 0) {
            throw new IllegalArgumentException("No hay cambio de signo en el intervalo [" + a + ", " + b + "]");
        }

        double c = a;
        double error = Double.MAX_VALUE;

        for (int i = 1; i <= maxIter; i++) {
            c = (a + b) / 2.0;
            double fc = f.applyAsDouble(c);

            if (i == 1) {
                error = Math.abs(b - a);
            } else {
                error = Math.abs(b - a) / 2.0;
            }

            iteraciones.add(new Iteracion(i, a, b, c, fa, fb, fc, error));

            if (Math.abs(fc) < tol || error <= tol) {
                break;
            }

            if (fa * fc < 0) {
                b = c;
                fb = fc;
            } else {
                a = c;
                fa = fc;
            }
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

    public static void graficar(List<Iteracion> iteraciones, java.util.function.DoubleUnaryOperator f, String expresion) {
        if (iteraciones.isEmpty()) {
            System.out.println("No hay datos para graficar.");
            return;
        }

        double minX = iteraciones.stream().mapToDouble(it -> it.a).min().orElse(0);
        double maxX = iteraciones.stream().mapToDouble(it -> it.b).max().orElse(0);
        double rango = maxX - minX;
        minX -= rango * 0.1;
        maxX += rango * 0.1;

        int width = 80;
        int rows = 20;

        double[] xs = new double[rows];
        double[] ys = new double[rows];

        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < rows; i++) {
            xs[i] = minX + (maxX - minX) * i / (rows - 1);
            ys[i] = f.applyAsDouble(xs[i]);
            if (ys[i] < minY) minY = ys[i];
            if (ys[i] > maxY) maxY = ys[i];
        }

        System.out.println("\nGráfico aproximado de " + expresion);
        for (int i = 0; i < rows; i++) {
            int col = (int) ((ys[i] - minY) / (maxY - minY) * (width - 1));
            if (col < 0) col = 0;
            if (col >= width) col = width - 1;

            StringBuilder line = new StringBuilder(" ");
            for (int j = 0; j < width; j++) {
                if (j == col) {
                    line.append('*');
                } else if (j == width / 2) {
                    line.append('|');
                } else {
                    line.append(' ');
                }
            }

            System.out.printf("x=%7.4f y=%8.4f %s\n", xs[i], ys[i], line.toString());
        }

        double raizEstim = iteraciones.get(iteraciones.size() - 1).c;
        System.out.printf("\nRaíz estimada: %.8f\n", raizEstim);
    }
}
