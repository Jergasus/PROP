package view;

import domini.controller.CtrlPresentacion;
import domini.model.board.Board;
import domini.model.cell.CellShape;

import javax.swing.*;
import java.awt.*;

public class VistaConfigAleatoria extends JPanel {

    private final CtrlPresentacion ctrl;

    private final JRadioButton rbSquare   = radio("Cuadrado");
    private final JRadioButton rbHex      = radio("Hexágono");
    private final JRadioButton rbTriangle = radio("Triángulo");

    private final JRadioButton rbOrtho = radio("Ortogonal (4 vecinos)");
    private final JRadioButton rbFull  = radio("Completa (8 vecinos)");

    private final JSpinner spnSize  = new JSpinner(new SpinnerNumberModel(5, 3, 12, 1));
    private final JSlider  sldDiff  = new JSlider(0, 100, 40);
    private final JSpinner spnVoids = new JSpinner(new SpinnerNumberModel(0, 0, 20, 1));
    private final JLabel   lblDiff  = new JLabel("40%");
    private final JLabel   lblStatus = new JLabel(" ", SwingConstants.CENTER);

    public VistaConfigAleatoria(CtrlPresentacion ctrl) {
        this.ctrl = ctrl;
        setLayout(new BorderLayout(0, 8));
        setBackground(new Color(0x2B2D30));
        setBorder(BorderFactory.createEmptyBorder(16, 30, 16, 30));
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        JLabel title = new JLabel("Partida Aleatoria", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(new Color(0x6AA84F));
        add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(new Color(0x3C3F41));
        form.setBorder(BorderFactory.createEmptyBorder(14, 24, 14, 24));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;

        // Shape
        ButtonGroup bgShape = new ButtonGroup();
        bgShape.add(rbSquare); bgShape.add(rbHex); bgShape.add(rbTriangle);
        rbSquare.setSelected(true);
        JPanel shapePanel = darkPanel();
        shapePanel.add(rbSquare); shapePanel.add(rbHex); shapePanel.add(rbTriangle);
        addRow(form, g, 0, sectionLabel("Forma:"), shapePanel);

        // Adjacency
        ButtonGroup bgAdj = new ButtonGroup();
        bgAdj.add(rbOrtho); bgAdj.add(rbFull);
        rbOrtho.setSelected(true);
        JPanel adjPanel = darkPanel();
        adjPanel.add(rbOrtho); adjPanel.add(rbFull);
        addRow(form, g, 1, sectionLabel("Adyacencia:"), adjPanel);

        // Disable adjacency for non-square shapes
        rbHex.addActionListener(e     -> { rbOrtho.setSelected(true); setAdjEnabled(false); });
        rbTriangle.addActionListener(e -> { rbOrtho.setSelected(true); setAdjEnabled(false); });
        rbSquare.addActionListener(e  -> setAdjEnabled(true));

        // Size
        styleSpinner(spnSize);
        addRow(form, g, 2, sectionLabel("Tamaño (lado):"), spnSize);

        // Difficulty
        sldDiff.setBackground(new Color(0x3C3F41));
        sldDiff.setForeground(Color.WHITE);
        sldDiff.addChangeListener(e -> lblDiff.setText(sldDiff.getValue() + "%"));
        lblDiff.setForeground(Color.WHITE);
        JPanel diffRow = darkPanel();
        diffRow.add(sldDiff); diffRow.add(lblDiff);
        addRow(form, g, 3, sectionLabel("Dificultad:"), diffRow);

        // Voids
        styleSpinner(spnVoids);
        addRow(form, g, 4, sectionLabel("Celdas vacías:"), spnVoids);

        add(form, BorderLayout.CENTER);

        // Bottom
        JButton btnGen  = actionButton("Generar y jugar", new Color(0x6AA84F));
        JButton btnBack = actionButton("← Volver",        new Color(0x888888));
        btnGen.addActionListener(e  -> actionGenerar());
        btnBack.addActionListener(e -> ctrl.mostrarMenuPrincipal());

        lblStatus.setForeground(new Color(0xFF6B6B));
        lblStatus.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JPanel bottom = new JPanel();
        bottom.setBackground(new Color(0x2B2D30));
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        JPanel btnRow = new JPanel(new FlowLayout());
        btnRow.setBackground(new Color(0x2B2D30));
        btnRow.add(btnGen); btnRow.add(btnBack);
        bottom.add(btnRow);
        bottom.add(lblStatus);
        add(bottom, BorderLayout.SOUTH);
    }

    private void actionGenerar() {
        CellShape shape;
        if (rbHex.isSelected())      shape = CellShape.HEXAGON;
        else if (rbTriangle.isSelected()) shape = CellShape.TRIANGLE;
        else                         shape = CellShape.SQUARE;

        boolean fullAdj = rbFull.isSelected();
        int size        = (Integer) spnSize.getValue();
        double diff     = sldDiff.getValue() / 100.0;
        int voids       = (Integer) spnVoids.getValue();

        lblStatus.setText("Generando...");
        // Run in background thread to keep EDT responsive
        new SwingWorker<Board, Void>() {
            @Override protected Board doInBackground() {
                return ctrl.generarPartidaAleatoria(shape, fullAdj, size, diff, voids);
            }
            @Override protected void done() {
                try {
                    Board board = get();
                    if (board == null) {
                        lblStatus.setText("No se pudo generar. Prueba otros parámetros.");
                    } else {
                        lblStatus.setText(" ");
                        ctrl.jugarPartidaAleatoria(board);
                    }
                } catch (Exception ex) {
                    lblStatus.setText("Error: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void setAdjEnabled(boolean enabled) {
        rbOrtho.setEnabled(enabled);
        rbFull.setEnabled(enabled);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private static void addRow(JPanel p, GridBagConstraints g, int row,
                                JComponent label, JComponent field) {
        g.gridx = 0; g.gridy = row; g.weightx = 0;
        p.add(label, g);
        g.gridx = 1; g.weightx = 1;
        p.add(field, g);
    }

    private static JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(Color.WHITE);
        l.setFont(new Font("SansSerif", Font.BOLD, 13));
        return l;
    }

    private static JPanel darkPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        p.setBackground(new Color(0x3C3F41));
        return p;
    }

    private static JRadioButton radio(String text) {
        JRadioButton rb = new JRadioButton(text);
        rb.setBackground(new Color(0x3C3F41));
        rb.setForeground(Color.WHITE);
        rb.setFont(new Font("SansSerif", Font.PLAIN, 13));
        return rb;
    }

    private static JButton actionButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setFocusPainted(false); b.setBorderPainted(false); b.setOpaque(true);
        b.setFont(new Font("SansSerif", Font.BOLD, 14));
        b.setPreferredSize(new Dimension(200, 42));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private static void styleSpinner(JSpinner s) {
        JComponent editor = s.getEditor();
        if (editor instanceof JSpinner.DefaultEditor de) {
            de.getTextField().setBackground(new Color(0x4A4D50));
            de.getTextField().setForeground(Color.WHITE);
            de.getTextField().setCaretColor(Color.WHITE);
        }
    }
}
