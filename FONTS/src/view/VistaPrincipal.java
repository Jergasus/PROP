package view;

import domini.controller.CtrlPresentacion;
import domini.model.adjacency.*;
import domini.model.cell.CellShape;
import domini.model.user.User;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

public class VistaPrincipal extends JPanel {

    private final CtrlPresentacion ctrl;
    private final JLabel lblUserInfo = new JLabel();

    public VistaPrincipal(CtrlPresentacion ctrl) {
        this.ctrl = ctrl;
        setLayout(new BorderLayout());
        setBackground(new Color(0x2B2D30));
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0x3C3F41));
        header.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));
        JLabel title = new JLabel("HIDATO");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(new Color(0x5294E2));
        lblUserInfo.setForeground(new Color(0xAAAAAA));
        lblUserInfo.setFont(new Font("SansSerif", Font.PLAIN, 13));
        header.add(title, BorderLayout.WEST);
        header.add(lblUserInfo, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Menu buttons
        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(new Color(0x2B2D30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.gridx = 0;

        String[][] items = {
            {"Hidatos Pregenerados",  "0x5294E2"},
            {"Partida Aleatoria",     "0x6AA84F"},
            {"Editor de Hidato",      "0xE8A010"},
            {"Cargar Partida",        "0x9B59B6"},
            {"Ver Ranking",           "0xE74C3C"},
            {"Cerrar sesión",         "0x888888"},
        };

        for (int i = 0; i < items.length; i++) {
            JButton btn = menuButton(items[i][0], Color.decode(items[i][1]));
            gbc.gridy = i;
            center.add(btn, gbc);
            final int idx = i;
            btn.addActionListener(e -> actionMenu(idx));
        }

        add(center, BorderLayout.CENTER);
    }

    private void actionMenu(int idx) {
        switch (idx) {
            case 0 -> ctrl.mostrarSeleccionNivel();
            case 1 -> ctrl.mostrarConfigAleatoria();
            case 2 -> abrirEditor();
            case 3 -> cargarPartida();
            case 4 -> ctrl.mostrarRanking();
            case 5 -> { ctrl.actualizarRanking(); ctrl.mostrarLogin(); }
        }
    }

    private void abrirEditor() {
        // Ask for shape and size, then open editor
        String[] shapes = {"Cuadrado", "Hexágono", "Triángulo"};
        String shapeStr = (String) JOptionPane.showInputDialog(
            ctrl.getFrame(), "Forma de celdas:", "Editor – Forma",
            JOptionPane.QUESTION_MESSAGE, null, shapes, shapes[0]);
        if (shapeStr == null) return;

        String sizeStr = JOptionPane.showInputDialog(ctrl.getFrame(),
            "Tamaño del lado (ej. 5):", "Editor – Tamaño",
            JOptionPane.QUESTION_MESSAGE);
        if (sizeStr == null) return;
        int size = 5;
        try { size = Integer.parseInt(sizeStr.trim()); } catch (NumberFormatException ignored) {}
        size = Math.max(3, Math.min(size, 12));

        CellShape shape;
        AdjacencyStrategy strategy;
        if (shapeStr.startsWith("H")) {
            shape    = CellShape.HEXAGON;
            strategy = new HexagonalAdjacencyStrategy();
        } else if (shapeStr.startsWith("T")) {
            shape    = CellShape.TRIANGLE;
            strategy = new TriangleAdjacencyStrategy();
        } else {
            shape = CellShape.SQUARE;
            String[] adjs = {"Ortogonal (4)", "Completa (8)"};
            String adjStr = (String) JOptionPane.showInputDialog(
                ctrl.getFrame(), "Adyacencia:", "Editor – Adyacencia",
                JOptionPane.QUESTION_MESSAGE, null, adjs, adjs[0]);
            strategy = (adjStr != null && adjStr.startsWith("C"))
                ? new SquareFullAdjacencyStrategy()
                : new SquareAdjacencyStrategy();
        }

        ctrl.mostrarEditor(ctrl.crearTableroEditor(shape, strategy, size));
    }

    private void cargarPartida() {
        JFileChooser fc = new JFileChooser(".");
        fc.setFileFilter(new FileNameExtensionFilter("Partidas Hidato (*.hidato)", "hidato"));
        if (fc.showOpenDialog(ctrl.getFrame()) != JFileChooser.APPROVE_OPTION) return;
        File f = fc.getSelectedFile();
        String err = ctrl.cargarPartida(f.getAbsolutePath());
        if (err != null) {
            JOptionPane.showMessageDialog(ctrl.getFrame(), "Error: " + err, "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            ctrl.mostrarJuego();
        }
    }

    public void actualizar() {
        User u = ctrl.getCurrentUser();
        if (u != null) lblUserInfo.setText(u.getUsername() + "  •  " + u.getTotalPoints() + " pts");
    }

    private JButton menuButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setFont(new Font("SansSerif", Font.BOLD, 15));
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setPreferredSize(new Dimension(320, 52));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
}
