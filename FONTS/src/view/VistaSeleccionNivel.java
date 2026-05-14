package view;

import domini.controller.CtrlPresentacion;
import domini.model.level.Difficulty;
import domini.model.level.Level;
import domini.model.level.LevelCatalog;
import domini.model.user.LevelProgress;
import domini.model.user.User;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class VistaSeleccionNivel extends JPanel {

    private final CtrlPresentacion ctrl;
    private final JTabbedPane tabs = new JTabbedPane();

    public VistaSeleccionNivel(CtrlPresentacion ctrl) {
        this.ctrl = ctrl;
        setLayout(new BorderLayout());
        setBackground(new Color(0x2B2D30));
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        JLabel title = new JLabel("Selecciona un nivel", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(14, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        tabs.setBackground(new Color(0x3C3F41));
        tabs.setForeground(Color.WHITE);
        add(tabs, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottom.setBackground(new Color(0x2B2D30));
        JButton back = styledButton("← Volver", new Color(0x888888));
        back.addActionListener(e -> ctrl.mostrarMenuPrincipal());
        bottom.add(back);
        add(bottom, BorderLayout.SOUTH);
    }

    public void actualizar() {
        tabs.removeAll();
        LevelCatalog catalog = ctrl.getCatalog();
        User user = ctrl.getCurrentUser();
        Map<String, LevelProgress> progress = user.getAllLevelProgress();

        Difficulty[] diffs = {Difficulty.EASY, Difficulty.MEDIUM, Difficulty.HARD};
        Color[] tabColors   = {new Color(0x6AA84F), new Color(0xE8A010), new Color(0xE74C3C)};

        for (int di = 0; di < diffs.length; di++) {
            Difficulty d = diffs[di];
            List<Level> levels = catalog.getLevelsByDifficulty(d);
            if (levels.isEmpty()) continue;

            String[] cols = {"#", "Nombre", "Estrellas", "Mejor tiempo"};
            Object[][] data = new Object[levels.size()][4];
            for (int i = 0; i < levels.size(); i++) {
                Level l = levels.get(i);
                LevelProgress lp = progress.getOrDefault(l.getLevelId(), new LevelProgress());
                data[i][0] = i + 1;
                data[i][1] = l.getDisplayName();
                data[i][2] = stars(lp.getBestStars());
                data[i][3] = lp.isCompleted()
                    ? ctrl.formatTime(lp.getBestTimeMillis())
                    : "—";
            }

            DefaultTableModel model = new DefaultTableModel(data, cols) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            JTable table = new JTable(model);
            styleTable(table);

            JButton playBtn = styledButton("Jugar nivel seleccionado", tabColors[di]);
            final List<Level> lvls = levels;
            playBtn.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row < 0) {
                    JOptionPane.showMessageDialog(ctrl.getFrame(),
                        "Selecciona un nivel de la lista.", "Sin selección",
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }
                ctrl.jugarNivel(lvls.get(row));
            });

            JPanel panel = new JPanel(new BorderLayout(0, 8));
            panel.setBackground(new Color(0x3C3F41));
            panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            panel.add(new JScrollPane(table), BorderLayout.CENTER);
            JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            btnRow.setBackground(new Color(0x3C3F41));
            btnRow.add(playBtn);
            panel.add(btnRow, BorderLayout.SOUTH);

            tabs.addTab(d.getDisplayName(), panel);
        }
    }

    private void styleTable(JTable t) {
        t.setBackground(new Color(0x4A4D50));
        t.setForeground(Color.WHITE);
        t.setGridColor(new Color(0x666666));
        t.setSelectionBackground(new Color(0x5294E2));
        t.setSelectionForeground(Color.WHITE);
        t.setRowHeight(28);
        t.setFont(new Font("SansSerif", Font.PLAIN, 13));
        t.getTableHeader().setBackground(new Color(0x3C3F41));
        t.getTableHeader().setForeground(Color.WHITE);
        t.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        t.setFillsViewportHeight(true);
        // Column widths
        int[] widths = {30, 120, 80, 100};
        for (int i = 0; i < widths.length; i++)
            t.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
    }

    private String stars(int n) {
        return "★".repeat(n) + "☆".repeat(Math.max(0, 3 - n));
    }

    private JButton styledButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
}
