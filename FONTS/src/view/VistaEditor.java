package view;

import domini.controller.CtrlPresentacion;
import domini.model.board.Board;
import domini.model.cell.Cell;
import domini.model.cell.Position;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class VistaEditor extends JPanel {

    private static final Color BG_PANEL  = new Color(0x12121E);
    private static final Color BG_HEADER = new Color(0x1A1A2E);
    private static final Color BG_FOOTER = new Color(0x16213E);
    private static final Color C_GOLD    = new Color(0xFBBF24);
    private static final Color C_SUBTEXT = new Color(0x6B7280);
    private static final Color C_SEP     = new Color(0x2D2D4E);

    private final CtrlPresentacion ctrl;
    private final BoardPanel boardPanel = new BoardPanel();
    private Board currentBoard;

    private final JLabel lblStatus  = new JLabel(" ", SwingConstants.LEFT);

    private final JButton btnVoid    = mkBtn("⬡  Void",       new Color(0x374151));
    private final JButton btnClear   = mkBtn("✕  Limpiar",    new Color(0x374151));
    private final JButton btnAnalyze = mkBtn("⚙  Validar",    new Color(0x1D4ED8));
    private final JButton btnSave    = mkBtn("↑  Guardar",    new Color(0x065F46));
    private final JButton btnExport  = mkBtn("↓  Exportar",   new Color(0x6D28D9));
    private final JButton btnBack    = mkBtn("←  Salir",      new Color(0x4B1C1C));

    public VistaEditor(CtrlPresentacion ctrl) {
        this.ctrl = ctrl;
        setLayout(new BorderLayout());
        setBackground(BG_PANEL);
        add(buildHeader(), BorderLayout.NORTH);
        add(boardPanel,    BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
        wireButtons();
    }

    // ── Header ────────────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setBackground(BG_HEADER);
        hdr.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, C_SEP),
            BorderFactory.createEmptyBorder(14, 22, 14, 22)));

        JLabel title = new JLabel("Editor de Puzzle");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(C_GOLD);
        hdr.add(title, BorderLayout.WEST);

        JLabel hint = new JLabel(
            "Clic en celda  ·  escribe número + Enter para fijar  ·  Backspace borra");
        hint.setFont(new Font("SansSerif", Font.PLAIN, 12));
        hint.setForeground(C_SUBTEXT);
        hdr.add(hint, BorderLayout.EAST);

        return hdr;
    }

    // ── Footer ────────────────────────────────────────────────────────────────

    private JPanel buildFooter() {
        JPanel footer = new JPanel();
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setBackground(BG_FOOTER);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, C_SEP));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 14));
        btnRow.setBackground(BG_FOOTER);
        btnRow.add(btnVoid);
        btnRow.add(btnClear);
        btnRow.add(sep());
        btnRow.add(btnAnalyze);
        btnRow.add(sep());
        btnRow.add(btnSave);
        btnRow.add(btnExport);
        btnRow.add(sep());
        btnRow.add(btnBack);
        footer.add(btnRow);

        JPanel statusRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 22, 0));
        statusRow.setBackground(BG_FOOTER);
        statusRow.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        lblStatus.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblStatus.setForeground(C_SUBTEXT);
        statusRow.add(lblStatus);
        footer.add(statusRow);

        return footer;
    }

    private static JSeparator sep() {
        JSeparator s = new JSeparator(SwingConstants.VERTICAL);
        s.setPreferredSize(new Dimension(1, 30));
        s.setForeground(new Color(0x374151));
        return s;
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public void iniciarEditor(Board board) {
        this.currentBoard = board;
        int maxN = board.getRows() * board.getCols();
        boardPanel.setEditorMode(true);
        boardPanel.setBoard(board, maxN);
        boardPanel.setOnConfirm(this::onConfirm);
        boardPanel.setOnSelect(pos -> {
            Cell cell = currentBoard.getCell(pos);
            String kind = (cell != null && cell.isVoid()) ? "VOID" : "editable";
            lblStatus.setText("Celda (" + pos.row() + ", " + pos.col() + ") — " + kind);
        });
        lblStatus.setText("Haz clic en una celda para editarla.");
        SwingUtilities.invokeLater(boardPanel::requestFocusInWindow);
    }

    // ── Callbacks ─────────────────────────────────────────────────────────────

    private void onConfirm(Position pos, int val) {
        Cell cell = currentBoard.getCell(pos);
        if (cell == null || cell.isVoid()) return;
        if (val == 0) {
            cell.setAsEmpty();
            lblStatus.setText("Celda (" + pos.row() + ", " + pos.col() + ") limpiada.");
        } else {
            cell.setFixedValue(val);
            lblStatus.setText("Pista  " + val + "  fijada en (" + pos.row() + ", " + pos.col() + ").");
        }
        boardPanel.refresh();
    }

    // ── Button listeners ──────────────────────────────────────────────────────

    private void wireButtons() {
        btnVoid.addActionListener(e    -> actionVoid());
        btnClear.addActionListener(e   -> actionClear());
        btnAnalyze.addActionListener(e -> actionAnalyze());
        btnSave.addActionListener(e    -> actionSave());
        btnExport.addActionListener(e  -> actionExport());
        btnBack.addActionListener(e    -> ctrl.mostrarMenuPrincipal());
    }

    private void actionVoid() {
        Position sel = boardPanel.getSelected();
        if (sel == null) { lblStatus.setText("Selecciona primero una celda."); return; }
        Cell cell = currentBoard.getCell(sel);
        if (cell == null) return;
        cell.setVoid(!cell.isVoid());
        boardPanel.refresh();
        lblStatus.setText("Celda (" + sel.row() + ", " + sel.col() + ")  "
            + (cell.isVoid() ? "marcada VOID." : "restaurada como editable."));
    }

    private void actionClear() {
        Position sel = boardPanel.getSelected();
        if (sel == null) { lblStatus.setText("Selecciona primero una celda."); return; }
        Cell cell = currentBoard.getCell(sel);
        if (cell == null || cell.isVoid()) { lblStatus.setText("Esa celda no es editable."); return; }
        cell.setAsEmpty();
        boardPanel.refresh();
        lblStatus.setText("Celda (" + sel.row() + ", " + sel.col() + ") limpiada.");
    }

    private void actionAnalyze() {
        if (currentBoard == null) return;
        int[] result = ctrl.validarEditor(currentBoard);
        int count = result[0];
        long ms   = result[1];
        String t  = "  [" + ctrl.formatTime(ms) + "]";
        String msg;
        if (count == 0) {
            msg = "El tablero NO tiene solución." + t;
            lblStatus.setText(msg);
            JOptionPane.showMessageDialog(ctrl.getFrame(), msg, "Sin solución", JOptionPane.WARNING_MESSAGE);
        } else if (count == 1) {
            msg = "Hidato válido — solución única." + t;
            lblStatus.setText(msg);
            int r = JOptionPane.showConfirmDialog(ctrl.getFrame(),
                msg + "\n\n¿Mostrar la solución?", "Válido",
                JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
            if (r == JOptionPane.YES_OPTION) {
                Board sol = ctrl.resolverEditor(currentBoard);
                if (sol != null) {
                    currentBoard = sol;
                    boardPanel.setBoard(sol, sol.getRows() * sol.getCols());
                    lblStatus.setText("Solución mostrada.");
                }
            }
        } else {
            msg = "Múltiples soluciones — añade más pistas." + t;
            lblStatus.setText(msg);
            JOptionPane.showMessageDialog(ctrl.getFrame(), msg, "Ambiguo", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void actionSave() {
        String name = JOptionPane.showInputDialog(ctrl.getFrame(),
            "Nombre del archivo (sin extensión):", "Guardar partida",
            JOptionPane.QUESTION_MESSAGE);
        if (name == null || name.isBlank()) return;
        String err = ctrl.guardarEditorComoPartida(currentBoard, name.trim());
        if (err != null) JOptionPane.showMessageDialog(ctrl.getFrame(), "Error: " + err,
            "Error", JOptionPane.ERROR_MESSAGE);
        else lblStatus.setText("Guardado como  " + name.trim() + ".hidato");
    }

    private void actionExport() {
        String name = JOptionPane.showInputDialog(ctrl.getFrame(),
            "Nombre del archivo (sin extensión):", "Exportar",
            JOptionPane.QUESTION_MESSAGE);
        if (name == null || name.isBlank()) return;
        String err = ctrl.exportarEditor(currentBoard, name.trim());
        if (err != null) JOptionPane.showMessageDialog(ctrl.getFrame(), "Error: " + err,
            "Error", JOptionPane.ERROR_MESSAGE);
        else lblStatus.setText("Exportado como  " + name.trim() + ".txt");
    }

    // ── Button factory ────────────────────────────────────────────────────────

    private static JButton mkBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(new Color(0xE5E7EB));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(9, 20, 9, 20));
        Color hover = bg.brighter();
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { b.setBackground(hover); }
            @Override public void mouseExited(MouseEvent e)  { b.setBackground(bg); }
        });
        return b;
    }
}
