import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

public class Main {

    // ── Paleta rosita ─────────────────────────────────────────────────────────
    static final Color PINK_BG     = new Color(255, 228, 235);
    static final Color PINK_DARK   = new Color(219, 112, 147);
    static final Color PINK_LIGHT  = new Color(255, 210, 220);
    static final Color PINK_MID    = new Color(255, 105, 135);
    static final Color PINK_BTN    = new Color(233,  80, 120);
    static final Color PINK_HEADER = new Color(210,  70, 110);
    static final Color TEXT_DARK   = new Color( 80,  20,  40);
    static final Color TEXT_WHITE  = Color.WHITE;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) {}

            JFrame frame = new JFrame("Método de Bisección");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 800);
            frame.setLayout(new BorderLayout(0, 0));
            frame.getContentPane().setBackground(PINK_BG);

            // ── Panel de entrada ──────────────────────────────────────────────
            JPanel inputPanel = new JPanel(new GridLayout(5, 2, 8, 6));
            inputPanel.setBackground(PINK_BG);
            inputPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 0, PINK_DARK),
                    BorderFactory.createEmptyBorder(10, 14, 10, 14)));

            String[] labels   = {"f(x) =", "Extremo izq. a =", "Extremo der. b =",
                                  "Tolerancia =", "Máx iteraciones ="};
            String[] defaults = {"exp(-x)-x", "0", "3", "1e-6", "50"};
            JTextField[] fields = new JTextField[5];

            for (int i = 0; i < 5; i++) {
                JLabel lbl = new JLabel(labels[i]);
                lbl.setForeground(TEXT_DARK);
                lbl.setFont(new Font("SansSerif", Font.BOLD, 13));

                fields[i] = new JTextField(defaults[i]);
                fields[i].setBackground(Color.WHITE);
                fields[i].setForeground(TEXT_DARK);
                fields[i].setFont(new Font("Monospaced", Font.PLAIN, 13));
                fields[i].setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(PINK_DARK, 1, true),
                        BorderFactory.createEmptyBorder(3, 6, 3, 6)));
                fields[i].setCaretColor(PINK_DARK);

                inputPanel.add(lbl);
                inputPanel.add(fields[i]);
            }

            // ── Botón Calcular ────────────────────────────────────────────────
            JButton calcularBtn = new JButton(" Calcular ") {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getModel().isRollover() ? PINK_MID : PINK_BTN);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                    super.paintComponent(g);
                }
            };
            calcularBtn.setForeground(TEXT_WHITE);
            calcularBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
            calcularBtn.setPreferredSize(new Dimension(180, 34));
            calcularBtn.setContentAreaFilled(false);
            calcularBtn.setBorderPainted(false);
            calcularBtn.setFocusPainted(false);
            calcularBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
            btnPanel.setBackground(PINK_BG);
            btnPanel.add(calcularBtn);

            JPanel topWrapper = new JPanel(new BorderLayout());
            topWrapper.setBackground(PINK_BG);
            topWrapper.add(inputPanel, BorderLayout.NORTH);
            topWrapper.add(btnPanel,   BorderLayout.SOUTH);
            frame.add(topWrapper, BorderLayout.NORTH);

            // ── SplitPane ─────────────────────────────────────────────────────
            JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
            splitPane.setResizeWeight(0.65);
            splitPane.setDividerSize(7);
            splitPane.setBackground(PINK_DARK);
            frame.add(splitPane, BorderLayout.CENTER);

            // Placeholder gráfico (el GraficoPanel sigue siendo blanco)
            JPanel graficoHolder = new JPanel(new BorderLayout());
            graficoHolder.setBackground(Color.WHITE);
            JLabel placeholder = new JLabel(
                    "Ingresa los valores y presiona Calcular", SwingConstants.CENTER);
            placeholder.setForeground(new Color(180, 100, 130));
            placeholder.setFont(new Font("SansSerif", Font.ITALIC, 14));
            graficoHolder.add(placeholder, BorderLayout.CENTER);
            splitPane.setTopComponent(graficoHolder);

            // Placeholder tabla
            JScrollPane tablaScroll = new JScrollPane(new JTable());
            tablaScroll.setBackground(PINK_BG);
            tablaScroll.getViewport().setBackground(PINK_BG);
            tablaScroll.setBorder(
                    BorderFactory.createMatteBorder(2, 0, 0, 0, PINK_DARK));
            splitPane.setBottomComponent(tablaScroll);

            // ── Acción del botón ──────────────────────────────────────────────
            calcularBtn.addActionListener(e -> {
                try {
                    String expresion = fields[0].getText().trim();
                    double a       = Double.parseDouble(fields[1].getText().trim());
                    double b       = Double.parseDouble(fields[2].getText().trim());
                    double tol     = Double.parseDouble(fields[3].getText().trim());
                    int    maxIter = Integer.parseInt(fields[4].getText().trim());

                    DoubleUnaryOperator f = Funcion.crearFuncion(expresion);
                    List<Biseccion.Iteracion> iteraciones =
                            Biseccion.biseccion(a, b, tol, f, maxIter);

                    Biseccion.mostrarIteraciones(iteraciones);

                    // — Gráfico (sin tocar GraficoPanel) —
                    graficoHolder.removeAll();
                    graficoHolder.add(
                            new GraficoPanel(iteraciones, f, expresion),
                            BorderLayout.CENTER);
                    graficoHolder.revalidate();
                    graficoHolder.repaint();

                    // — Tabla rosita —
                    String[] cols = {"N", "a", "b", "c", "f(c)", "Error"};
                    DefaultTableModel model = new DefaultTableModel(cols, 0);
                    for (Biseccion.Iteracion it : iteraciones)
                        model.addRow(new Object[]{it.n, it.a, it.b, it.c, it.fc, it.error});

                    JTable tabla = new JTable(model);
                    tabla.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
                    tabla.setFont(new Font("Monospaced", Font.PLAIN, 12));
                    tabla.setRowHeight(22);
                    tabla.setBackground(Color.WHITE);
                    tabla.setForeground(TEXT_DARK);
                    tabla.setGridColor(PINK_LIGHT);
                    tabla.setSelectionBackground(PINK_DARK);
                    tabla.setSelectionForeground(TEXT_WHITE);
                    tabla.setShowGrid(true);

                    // Filas alternas
                    tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
                        @Override
                        public Component getTableCellRendererComponent(
                                JTable t, Object v, boolean sel, boolean foc,
                                int row, int col) {
                            super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                            if (sel) {
                                setBackground(PINK_DARK);
                                setForeground(TEXT_WHITE);
                            } else {
                                setBackground(row % 2 == 0 ? Color.WHITE : PINK_LIGHT);
                                setForeground(TEXT_DARK);
                            }
                            setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
                            return this;
                        }
                    });

                    // Cabecera rosita oscura
                    JTableHeader header = tabla.getTableHeader();
                    header.setBackground(PINK_HEADER);
                    header.setForeground(TEXT_WHITE);
                    header.setFont(new Font("SansSerif", Font.BOLD, 12));
                    header.setReorderingAllowed(false);
                    ((DefaultTableCellRenderer) header.getDefaultRenderer())
                            .setHorizontalAlignment(SwingConstants.CENTER);

                    tablaScroll.setViewportView(tabla);
                    tablaScroll.getViewport().setBackground(Color.WHITE);
                    tablaScroll.revalidate();
                    tablaScroll.repaint();

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage());
                }
            });

            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}