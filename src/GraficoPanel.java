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

    private double viewMinX, viewMaxX, viewMinY, viewMaxY;

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

    // ================= RANGO INICIAL =================
    private void initViewRange() {
        if (iteraciones.isEmpty()) {
            viewMinX = -1; viewMaxX = 1;
            viewMinY = -1; viewMaxY = 1;
            return;
        }

        double minX = iteraciones.stream().mapToDouble(it -> it.a).min().orElse(0);
        double maxX = iteraciones.stream().mapToDouble(it -> it.b).max().orElse(1);

        int samples = 400;
        double minY = Double.MAX_VALUE;
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

        if (minY == Double.MAX_VALUE) {
            minY = -1;
            maxY = 1;
        }

        double padX = 0.15 * (maxX - minX == 0 ? 1 : maxX - minX);
        double padY = 0.15 * (maxY - minY == 0 ? 1 : maxY - minY);

        viewMinX = minX - padX;
        viewMaxX = maxX + padX;
        viewMinY = minY - padY;
        viewMaxY = maxY + padY;
    }

    // ================= FORMATO DE NÚMEROS =================
    private String formatTick(double v) {
        if (Math.abs(v) < 1e-10) return "0";

        if (Math.abs(v) >= 1000 || Math.abs(v) < 0.001) {
            return String.format("%.1e", v);
        }

        if (v == Math.floor(v)) {
            return String.valueOf((int) v);
        }

        return String.format("%.2f", v);
    }

    // ================= EVENTOS =================
    private void setupMouseListeners() {

        addMouseWheelListener(e -> {
            double factor = e.getWheelRotation() > 0 ? 1.15 : 1 / 1.15;
            double mx = pixToMathX(e.getX());
            double my = pixToMathY(e.getY());

            viewMinX = mx + (viewMinX - mx) * factor;
            viewMaxX = mx + (viewMaxX - mx) * factor;
            viewMinY = my + (viewMinY - my) * factor;
            viewMaxY = my + (viewMaxY - my) * factor;

            repaint();
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dragStart = e.getPoint();
                dragMinX = viewMinX;
                dragMaxX = viewMaxX;
                dragMinY = viewMinY;
                dragMaxY = viewMaxY;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                dragStart = null;
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    initViewRange();
                    repaint();
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragStart == null) return;

                double dx = (e.getX() - dragStart.x) * (dragMaxX - dragMinX) / getWidth();
                double dy = (e.getY() - dragStart.y) * (dragMaxY - dragMinY) / getHeight();

                viewMinX = dragMinX - dx;
                viewMaxX = dragMaxX - dx;
                viewMinY = dragMinY + dy;
                viewMaxY = dragMaxY + dy;

                repaint();
            }
        });

        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    }

    // ================= CONVERSIONES =================
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

    // ================= PINTADO =================
    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;

        int w = getWidth();
        int h = getHeight();

        drawGrid(g, w, h);
        drawAxes(g, w, h);
        drawCurve(g, w, h);
        drawBisectionPoints(g);
        drawTitle(g);
    }

    private void drawGrid(Graphics2D g, int w, int h) {
        g.setColor(Color.LIGHT_GRAY);

        for (double t : niceTicksFor(viewMinX, viewMaxX, 10)) {
            int px = mathToPixX(t);
            g.drawLine(px, 0, px, h);
        }

        for (double t : niceTicksFor(viewMinY, viewMaxY, 8)) {
            int py = mathToPixY(t);
            g.drawLine(0, py, w, py);
        }
    }

    private void drawAxes(Graphics2D g, int w, int h) {
        g.setColor(Color.BLACK);
        g.setFont(new Font("Monospaced", Font.PLAIN, 11));
        FontMetrics fm = g.getFontMetrics();

        int y0 = mathToPixY(0);
        int x0 = mathToPixX(0);

        if (y0 < 0 || y0 > h) y0 = h - 20;
        if (x0 < 0 || x0 > w) x0 = 40;

        g.drawLine(0, y0, w, y0);
        g.drawLine(x0, 0, x0, h);

        // Ticks X
        for (double t : niceTicksFor(viewMinX, viewMaxX, 10)) {
            int px = mathToPixX(t);
            if (px < 0 || px > w) continue;

            g.drawLine(px, y0 - 3, px, y0 + 3);

            String lbl = formatTick(t);
            int sw = fm.stringWidth(lbl);
            g.drawString(lbl, px - sw / 2, y0 + 15);
        }

        // Ticks Y
        for (double t : niceTicksFor(viewMinY, viewMaxY, 8)) {
            int py = mathToPixY(t);
            if (py < 0 || py > h) continue;

            g.drawLine(x0 - 3, py, x0 + 3, py);

            String lbl = formatTick(t);
            int sw = fm.stringWidth(lbl);
            g.drawString(lbl, x0 - sw - 5, py + fm.getAscent() / 2);
        }
    }

    private void drawCurve(Graphics2D g, int w, int h) {
        g.setColor(Color.BLUE);
        GeneralPath path = new GeneralPath();

        boolean started = false;

        for (int i = 0; i <= w * 2; i++) {
            double x = viewMinX + i * (viewMaxX - viewMinX) / (w * 2);
            double y = f.applyAsDouble(x);

            if (!Double.isFinite(y)) {
                started = false;
                continue;
            }

            int px = mathToPixX(x);
            int py = mathToPixY(y);

            if (!started) {
                path.moveTo(px, py);
                started = true;
            } else {
                path.lineTo(px, py);
            }
        }

        g.draw(path);
    }

    private void drawBisectionPoints(Graphics2D g) {
        for (Biseccion.Iteracion it : iteraciones) {
            double x = it.c;
            double y = f.applyAsDouble(x);

            int px = mathToPixX(x);
            int py = mathToPixY(y);

            boolean isRoot = Math.abs(it.fc) < 1e-6;

            if (isRoot) {
                g.setColor(Color.RED);
                g.fillOval(px - 6, py - 6, 12, 12);

                g.setColor(Color.BLACK);
                g.drawString(String.format("x≈%.6f", x), px + 8, py - 8);
            } else {
                g.setColor(Color.BLUE);
                g.fillOval(px - 3, py - 3, 6, 6);
            }
        }
    }

    private void drawTitle(Graphics2D g) {
        g.setColor(Color.BLACK);
        g.drawString("f(x) = " + expresion, 10, 20);
    }

    private double[] niceTicksFor(double min, double max, int n) {
        double step = (max - min) / n;
        double[] ticks = new double[n + 1];

        for (int i = 0; i <= n; i++) {
            ticks[i] = min + i * step;
        }

        return ticks;
    }
}