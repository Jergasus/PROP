package view;

import domini.controller.CtrlPresentacion;
import domini.model.ranking.RankingEntry;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class VistaRanking extends JPanel {

    private final CtrlPresentacion ctrl;
    private final DefaultTableModel tableModel;
    private final JTable table;

    public VistaRanking(CtrlPresentacion ctrl) {
        this.ctrl = ctrl;
        setLayout(new BorderLayout(0, 8));
        setBackground(new Color(0x2B2D30));
        setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        String[] cols = {"#", "Jugador", "Puntos"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        styleTable(table);

        JLabel title = new JLabel("Ranking Global", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(new Color(0xE8A010));
        add(title, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(new Color(0x4A4D50));
        add(scroll, BorderLayout.CENTER);

        JButton back = styledButton("← Volver", new Color(0x888888));
        back.addActionListener(e -> ctrl.mostrarMenuPrincipal());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottom.setBackground(new Color(0x2B2D30));
        bottom.add(back);
        add(bottom, BorderLayout.SOUTH);
    }

    public void actualizar() {
        tableModel.setRowCount(0);
        List<RankingEntry> entries = ctrl.getRanking();
        int rank = 1;
        for (RankingEntry e : entries)
            tableModel.addRow(new Object[]{rank++, e.getUsername(), e.getTotalPoints() + " pts"});
    }

    private void styleTable(JTable t) {
        t.setBackground(new Color(0x4A4D50));
        t.setForeground(Color.WHITE);
        t.setGridColor(new Color(0x666666));
        t.setSelectionBackground(new Color(0x5294E2));
        t.setSelectionForeground(Color.WHITE);
        t.setRowHeight(30);
        t.setFont(new Font("SansSerif", Font.PLAIN, 14));
        t.getTableHeader().setBackground(new Color(0x3C3F41));
        t.getTableHeader().setForeground(Color.WHITE);
        t.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        t.setFillsViewportHeight(true);
        int[] widths = {40, 200, 100};
        for (int i = 0; i < widths.length; i++)
            t.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
    }

    private JButton styledButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setFocusPainted(false); b.setBorderPainted(false); b.setOpaque(true);
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
}
