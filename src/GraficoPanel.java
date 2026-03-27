import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

public class GraficoPanel extends JPanel {
    private final List<Biseccion.Iteracion> iteraciones;
    private final DoubleUnaryOperator f;
    private final String expresion;

    public GraficoPanel(List<Biseccion.Iteracion> iteraciones, DoubleUnaryOperator f, String expresion) {
        this.iteraciones = iteraciones;
        this.f = f;
        this.expresion = expresion;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE); // fondo claro

        addTabla();
    }

    private void addTabla() {
        String[] columnas = {"N", "a", "b", "c", "f(c)", "Error"};
        DefaultTableModel model = new DefaultTableModel(columnas, 0);

        for (Biseccion.Iteracion it : iteraciones) {
            model.addRow(new Object[]{
                    it.n, it.a, it.b, it.c, it.fc, it.error
            });
        }

        JTable tabla = new JTable(model);
        tabla.setFillsViewportHeight(true);
        tabla.setBackground(new Color(255, 240, 245));
        tabla.setGridColor(new Color(255, 200, 220));
        tabla.setForeground(Color.DARK_GRAY);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JScrollPane scrollPane = new JScrollPane(tabla);
        scrollPane.setPreferredSize(new Dimension(880, 250));
        scrollPane.getViewport().setBackground(new Color(255, 245, 250));

        add(scrollPane, BorderLayout.SOUTH);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (iteraciones.isEmpty()) return;

        int width = getWidth();
        int height = Math.max(100, getHeight() - 260); // espacio para tabla

        double minX = iteraciones.stream().mapToDouble(it -> it.a).min().orElse(0);
        double maxX = iteraciones.stream().mapToDouble(it -> it.b).max().orElse(1);
        double minY = iteraciones.stream().mapToDouble(it -> f.applyAsDouble(it.c)).min().orElse(0);
        double maxY = iteraciones.stream().mapToDouble(it -> f.applyAsDouble(it.c)).max().orElse(1);

        // Agregar margen vertical
        double margin = 0.1 * (maxY - minY);
        minY -= margin;
        maxY += margin;

        // Fondo del gráfico
        g.setColor(new Color(240, 240, 240));
        g.fillRect(0, 0, width, height);

        // Dibujar puntos y línea de función
        g.setColor(Color.BLUE);
        int prevX = -1, prevY = -1;
        for (Biseccion.Iteracion it : iteraciones) {
            int xPix = (int)((it.c - minX) / (maxX - minX) * (width - 1));
            int yPix = (int)((maxY - f.applyAsDouble(it.c)) / (maxY - minY) * (height - 1));
            g.fillOval(xPix - 3, yPix - 3, 6, 6);

            if(prevX >= 0) {
                g.drawLine(prevX, prevY, xPix, yPix);
            }
            prevX = xPix;
            prevY = yPix;
        }

        // Título del gráfico
        g.setColor(Color.BLACK);
        g.setFont(new Font("Segoe UI", Font.BOLD, 16));
        g.drawString("Gráfico aproximado de " + expresion, 10, 20);

        // Ejes X y Y
        g.setColor(Color.DARK_GRAY);
        g.drawLine(0, height - 1, width, height - 1); // eje X
        g.drawLine(0, 0, 0, height);                  // eje Y
    }
}