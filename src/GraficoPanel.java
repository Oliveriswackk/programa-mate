import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

public class GraficoPanel extends JPanel {

    private final List<Biseccion.Iteracion> iteraciones;
    private final DoubleUnaryOperator f;
    private final String expresion;

    // Rango visible del plano (en coordenadas matemáticas)
    private double viewMinX, viewMaxX, viewMinY, viewMaxY;

    // Para pan con arrastrar
    private Point dragStart = null;
    private double dragMinX, dragMaxX, dragMinY, dragMaxY;

    public GraficoPanel(List<Biseccion.Iteracion> iteraciones,
                        DoubleUnaryOperator f,
                        String expresion) {
        this.iteraciones = iteraciones;
        this.f           = f;
        this.expresion   = expresion;

        setBackground(Color.WHITE);
        initViewRange();
        setupMouseListeners();
    }

    // ── Calcula el rango inicial a partir de los datos ────────────────────────
    private void initViewRange() {
        if (iteraciones.isEmpty()) {
            viewMinX = -1; viewMaxX = 1;
            viewMinY = -1; viewMaxY = 1;
            return;
        }
        double minX = iteraciones.stream().mapToDouble(it -> it.a).min().orElse(0);
        double maxX = iteraciones.stream().mapToDouble(it -> it.b).max().orElse(1);

        // Evaluar f en una muestra fina para obtener rango Y real
        int samples = 400;
        double minY =  Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        for (int i = 0; i <= samples; i++) {
            double x = minX + i * (maxX - minX) / samples;
            try {
                double y = f.applyAsDouble(x);
                if (Double.isFinite(y)) {
                    minY = Math.min(minY, y);
                    maxY = Math.max(maxY, y);
                }
            } catch (Exception ignored) {}
        }
        if (minY == Double.MAX_VALUE) { minY = -1; maxY = 1; }

        double padX = 0.15 * (maxX - minX == 0 ? 1 : maxX - minX);
        double padY = 0.15 * (maxY - minY == 0 ? 1 : maxY - minY);

        viewMinX = minX - padX;
        viewMaxX = maxX + padX;
        viewMinY = minY - padY;
        viewMaxY = maxY + padY;

        // Asegurar que el 0 sea visible en Y si está cerca
        if (viewMinY > -0.2 * (viewMaxY - viewMinY)) viewMinY = -0.2 * (viewMaxY - viewMinY);
        if (viewMaxY <  0.2 * (viewMaxY - viewMinY)) viewMaxY =  0.2 * (viewMaxY - viewMinY);
    }

    // ── Listeners: zoom (scroll) y pan (arrastrar) ────────────────────────────
    private void setupMouseListeners() {

        // Zoom con rueda del mouse
        addMouseWheelListener(e -> {
            double factor = e.getWheelRotation() > 0 ? 1.15 : 1.0 / 1.15;
            // Zoom centrado en la posición del mouse
            double mx = pixToMathX(e.getX());
            double my = pixToMathY(e.getY());
            viewMinX = mx + (viewMinX - mx) * factor;
            viewMaxX = mx + (viewMaxX - mx) * factor;
            viewMinY = my + (viewMinY - my) * factor;
            viewMaxY = my + (viewMaxY - my) * factor;
            repaint();
        });

        // Pan con arrastrar
        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                dragStart = e.getPoint();
                dragMinX = viewMinX; dragMaxX = viewMaxX;
                dragMinY = viewMinY; dragMaxY = viewMaxY;
            }
            @Override public void mouseReleased(MouseEvent e) { dragStart = null; }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseDragged(MouseEvent e) {
                if (dragStart == null) return;
                double dx = (e.getX() - dragStart.x) * (dragMaxX - dragMinX) / getWidth();
                double dy = (e.getY() - dragStart.y) * (dragMaxY - dragMinY) / getHeight();
                viewMinX = dragMinX - dx;
                viewMaxX = dragMaxX - dx;
                viewMinY = dragMinY + dy;
                viewMaxY = dragMaxY + dy;
                repaint();
            }
            @Override public void mouseMoved(MouseEvent e) {}
        });

        // Doble clic → resetear zoom
        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { initViewRange(); repaint(); }
            }
        });

        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    }

    // ── Conversión píxel ↔ coordenada matemática ──────────────────────────────
    private int mathToPixX(double x) {
        return (int) ((x - viewMinX) / (viewMaxX - viewMinX) * getWidth());
    }
    private int mathToPixY(double y) {
        return (int) ((viewMaxY - y) / (viewMaxY - viewMinY) * getHeight());
    }
    private double pixToMathX(int px) {
        return viewMinX + px * (viewMaxX - viewMinX) / getWidth();
    }
    private double pixToMathY(int py) {
        return viewMaxY - py * (viewMaxY - viewMinY) / getHeight();
    }

    // ── Pintado principal ─────────────────────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();

        drawGrid(g, w, h);
        drawAxes(g, w, h);
        drawCurve(g, w, h);
        drawBisectionPoints(g, w, h);
        drawTitle(g);
        drawHint(g, w, h);
    }

    // ── Grid ──────────────────────────────────────────────────────────────────
    private void drawGrid(Graphics2D g, int w, int h) {
        g.setColor(new Color(220, 220, 220));
        g.setStroke(new BasicStroke(0.5f));

        double[] ticks = niceTicksFor(viewMinX, viewMaxX, 10);
        for (double t : ticks) {
            int px = mathToPixX(t);
            g.drawLine(px, 0, px, h);
        }
        ticks = niceTicksFor(viewMinY, viewMaxY, 8);
        for (double t : ticks) {
            int py = mathToPixY(t);
            g.drawLine(0, py, w, py);
        }
    }

    // ── Ejes con etiquetas ────────────────────────────────────────────────────
    private void drawAxes(Graphics2D g, int w, int h) {
        g.setColor(new Color(60, 60, 60));
        g.setStroke(new BasicStroke(1.5f));
        Font tickFont = new Font("Monospaced", Font.PLAIN, 10);
        g.setFont(tickFont);
        FontMetrics fm = g.getFontMetrics();

        // Eje X
        int y0 = mathToPixY(0);
        y0 = Math.max(2, Math.min(h - 15, y0));
        g.drawLine(0, y0, w, y0);
        // flecha
        g.fillPolygon(new int[]{w, w-8, w-8}, new int[]{y0, y0-4, y0+4}, 3);

        // Eje Y
        int x0 = mathToPixX(0);
        x0 = Math.max(35, Math.min(w - 2, x0));
        g.drawLine(x0, 0, x0, h);
        g.fillPolygon(new int[]{x0, x0-4, x0+4}, new int[]{0, 8, 8}, 3);

        // Etiquetas X
        g.setColor(new Color(80, 80, 80));
        double[] ticksX = niceTicksFor(viewMinX, viewMaxX, 10);
        for (double t : ticksX) {
            int px = mathToPixX(t);
            g.setColor(new Color(60, 60, 60));
            g.drawLine(px, y0 - 4, px, y0 + 4);
            String lbl = formatTick(t);
            int sw = fm.stringWidth(lbl);
            int ty = Math.min(h - 3, y0 + 15);
            g.drawString(lbl, px - sw / 2, ty);
        }

        // Etiquetas Y
        double[] ticksY = niceTicksFor(viewMinY, viewMaxY, 8);
        for (double t : ticksY) {
            if (Math.abs(t) < 1e-12) continue; // evitar duplicar "0"
            int py = mathToPixY(t);
            g.drawLine(x0 - 4, py, x0 + 4, py);
            String lbl = formatTick(t);
            int sw = fm.stringWidth(lbl);
            int tx = Math.max(2, x0 - sw - 6);
            g.drawString(lbl, tx, py + fm.getAscent() / 2 - 1);
        }

        // "0" en origen
        g.drawString("0", Math.max(2, x0 - fm.stringWidth("0") - 4),
                Math.min(h - 3, y0 + fm.getAscent()));
    }

    // ── Curva f(x) ────────────────────────────────────────────────────────────
    private void drawCurve(Graphics2D g, int w, int h) {
        g.setColor(new Color(30, 100, 200));
        g.setStroke(new BasicStroke(2f));

        GeneralPath path = new GeneralPath();
        boolean started = false;
        int samples = w * 2;
        for (int i = 0; i <= samples; i++) {
            double x = viewMinX + i * (viewMaxX - viewMinX) / samples;
            double y;
            try { y = f.applyAsDouble(x); }
            catch (Exception e) { started = false; continue; }
            if (!Double.isFinite(y)) { started = false; continue; }

            float px = (float) mathToPixX(x);
            float py = (float) mathToPixY(y);
            if (!started) { path.moveTo(px, py); started = true; }
            else            path.lineTo(px, py);
        }
        g.draw(path);
    }

    // ── Puntos de bisección ───────────────────────────────────────────────────
    private void drawBisectionPoints(Graphics2D g, int w, int h) {
        int n = iteraciones.size();
        for (int i = 0; i < n; i++) {
            Biseccion.Iteracion it = iteraciones.get(i);
            double x = it.c;
            double y = f.applyAsDouble(x);

            int px = mathToPixX(x);
            int py = mathToPixY(y);

            // Color: el último punto en naranja, los demás azul oscuro
            boolean isLast = (i == n - 1);
            g.setColor(isLast ? new Color(220, 80, 0) : new Color(0, 60, 160));
            g.setStroke(new BasicStroke(1.5f));
            int r = isLast ? 6 : 4;
            g.fillOval(px - r, py - r, r * 2, r * 2);
            g.setColor(Color.WHITE);
            g.drawOval(px - r, py - r, r * 2, r * 2);

            // Línea vertical desde eje X al punto
            g.setColor(new Color(150, 150, 150, 100));
            g.setStroke(new BasicStroke(0.8f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                    1f, new float[]{3, 3}, 0));
            g.drawLine(px, mathToPixY(0), px, py);
        }

        // Líneas conectando los puntos sucesivos
        g.setColor(new Color(0, 80, 180, 180));
        g.setStroke(new BasicStroke(1.2f));
        for (int i = 1; i < n; i++) {
            Biseccion.Iteracion prev = iteraciones.get(i - 1);
            Biseccion.Iteracion curr = iteraciones.get(i);
            g.drawLine(mathToPixX(prev.c), mathToPixY(f.applyAsDouble(prev.c)),
                       mathToPixX(curr.c), mathToPixY(f.applyAsDouble(curr.c)));
        }
    }

    // ── Título ────────────────────────────────────────────────────────────────
    private void drawTitle(Graphics2D g) {
        g.setFont(new Font("SansSerif", Font.BOLD, 13));
        g.setColor(new Color(40, 40, 40));
        g.drawString("f(x) = " + expresion, 12, 20);
    }

    // ── Hint de zoom/pan ──────────────────────────────────────────────────────
    private void drawHint(Graphics2D g, int w, int h) {
        g.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g.setColor(new Color(140, 140, 140));
        String hint = "Scroll: zoom  |  Arrastrar: pan  |  Doble clic: reset";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(hint, w - fm.stringWidth(hint) - 8, h - 6);
    }

    // ── Utilidad: ticks "bonitos" ─────────────────────────────────────────────
    private double[] niceTicksFor(double min, double max, int target) {
        if (max <= min) return new double[0];
        double range = max - min;
        double rawStep = range / target;
        double mag = Math.pow(10, Math.floor(Math.log10(rawStep)));
        double[] nices = {1, 2, 2.5, 5, 10};
        double step = mag;
        for (double n : nices) {
            if (n * mag >= rawStep) { step = n * mag; break; }
        }
        int count = (int)((max - min) / step) + 2;
        double start = Math.ceil(min / step) * step;
        double[] result = new double[count];
        int k = 0;
        for (double t = start; t <= max + 1e-9 * step && k < count; t += step) {
            result[k++] = Math.round(t / step) * step;
        }
        double[] trimmed = new double[k];
        System.arraycopy(result, 0, trimmed, 0, k);
        return trimmed;
    }

    private String formatTick(double v) {
        if (v == 0) return "0";
        if (Math.abs(v) >= 1000 || (Math.abs(v) < 0.01 && v != 0))
            return String.format("%.1e", v);
        if (v == Math.floor(v)) return String.valueOf((int) v);
        return String.format("%.3g", v);
    }
}