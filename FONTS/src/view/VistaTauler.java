package view;

import domini.controller.CtrlPresentacion;
import domini.model.board.Board;
import domini.model.cell.Position;
import domini.model.game.Game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class VistaTauler extends JPanel {

    private static final Color BG_PANEL  = new Color(0x12121E);
    private static final Color BG_HEADER = new Color(0x1A1A2E);
    private static final Color BG_FOOTER = new Color(0x16213E);
    private static final Color C_TIMER   = new Color(0xFACC15);
    private static final Color C_SUBTEXT = new Color(0x6B7280);
    private static final Color C_SEP     = new Color(0x2D2D4E);

    private final CtrlPresentacion ctrl;
    private final BoardPanel boardPanel = new BoardPanel();

    private final JLabel lblTimer  = new JLabel("00:00", SwingConstants.RIGHT);
    private final JLabel lblInfo   = new JLabel("", SwingConstants.LEFT);
    private final JLabel lblStatus = new JLabel(" ", SwingConstants.LEFT);

    private final JButton btnUndo = mkBtn("↩  Deshacer", new Color(0x374151));
    private final JButton btnRedo = mkBtn("↪  Rehacer",  new Color(0x374151));
    private final JButton btnHint = mkBtn("★  Pista",    new Color(0xB45309));
    private final JButton btnGive = mkBtn("✕  Rendirse", new Color(0x991B1B));
    private final JButton btnSave = mkBtn("↑  Guardar",  new Color(0x065F46));

    private javax.swing.Timer swingTimer;
    private long startTime;
    private long accumulatedTime;
    private Game currentGame;

    public VistaTauler(CtrlPresentacion ctrl) {
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
        JPanel hdr = new JPanel(new BorderLayout(0, 0));
        hdr.setBackground(BG_HEADER);
        hdr.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, C_SEP),
            BorderFactory.createEmptyBorder(14, 22, 14, 22)));

        lblInfo.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lblInfo.setForeground(C_SUBTEXT);
        hdr.add(lblInfo, BorderLayout.WEST);

        lblTimer.setFont(new Font("Monospaced", Font.BOLD, 38));
        lblTimer.setForeground(C_TIMER);
        lblTimer.setPreferredSize(new Dimension(130, 48));
        hdr.add(lblTimer, BorderLayout.EAST);

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
        btnRow.add(btnUndo);
        btnRow.add(btnRedo);
        btnRow.add(sep());
        btnRow.add(btnHint);
        btnRow.add(btnGive);
        btnRow.add(sep());
        btnRow.add(btnSave);
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

    public void iniciarPartida(Game game, long accumulated) {
        this.currentGame     = game;
        this.accumulatedTime = accumulated;
        this.startTime       = System.currentTimeMillis();

        boardPanel.setEditorMode(false);
        boardPanel.setBoard(game.getBoard(), game.getMaxNumber());
        boardPanel.setOnConfirm(this::onConfirm);
        boardPanel.setOnSelect(pos ->
            lblStatus.setText("Celda seleccionada — escribe un número y pulsa Enter"));

        lblInfo.setText("Completa los números del  1 → " + game.getMaxNumber());
        updateTimer();

        if (swingTimer != null) swingTimer.stop();
        swingTimer = new javax.swing.Timer(500, e -> updateTimer());
        swingTimer.start();

        SwingUtilities.invokeLater(boardPanel::requestFocusInWindow);
    }

    // ── Callbacks ─────────────────────────────────────────────────────────────

    private void onConfirm(Position pos, int val) {
        boolean ok = ctrl.hacerMovimiento(pos.row(), pos.col(), val);
        boardPanel.refresh();
        if (!ok) {
            lblStatus.setText("Movimiento inválido — ese número no puede ir aquí.");
            return;
        }
        lblStatus.setText(val == 0
            ? "Celda borrada."
            : "✓  " + val + " colocado en (" + pos.row() + ", " + pos.col() + ").");
        if (ctrl.juegoTerminado()) terminar();
    }

    private void terminar() {
        if (swingTimer != null) swingTimer.stop();
        long elapsed = elapsedMillis();
        ctrl.completarNivel(elapsed);
        int stars = ctrl.getStarsForTime(elapsed);
        showWinDialog(elapsed, stars);
        ctrl.mostrarMenuPrincipal();
    }

    private void showWinDialog(long elapsed, int stars) {
        String filled = "★".repeat(stars) + "☆".repeat(3 - stars);
        long s = elapsed / 1000, m = s / 60; s %= 60;
        JOptionPane.showMessageDialog(ctrl.getFrame(),
            "<html><center>"
            + "<p style='font-size:18px; margin:4px 0'><b>¡Completado!</b></p>"
            + "<p style='font-size:32px; margin:6px 0'>" + filled + "</p>"
            + "<p style='font-size:14px'>Tiempo: <b>" + String.format("%02d:%02d", m, s) + "</b></p>"
            + "</center></html>",
            "¡Hidato resuelto!", JOptionPane.INFORMATION_MESSAGE);
    }

    // ── Button listeners ──────────────────────────────────────────────────────

    private void wireButtons() {
        btnUndo.addActionListener(e -> {
            if (ctrl.deshacer()) { boardPanel.refresh(); lblStatus.setText("Movimiento deshecho."); }
            else                 { lblStatus.setText("No hay movimientos para deshacer."); }
        });
        btnRedo.addActionListener(e -> {
            if (ctrl.rehacer()) { boardPanel.refresh(); lblStatus.setText("Movimiento rehecho."); }
            else                { lblStatus.setText("No hay movimientos para rehacer."); }
        });
        btnHint.addActionListener(e -> {
            String hint = ctrl.pedirPista();
            boardPanel.refresh();
            lblStatus.setText(hint);
        });
        btnGive.addActionListener(e -> actionSurrender());
        btnSave.addActionListener(e -> actionSave());
    }

    private void actionSurrender() {
        int r = JOptionPane.showConfirmDialog(ctrl.getFrame(),
            "¿Seguro que quieres rendirte?  Se mostrará la solución.",
            "Rendirse", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (r != JOptionPane.YES_OPTION) return;
        if (swingTimer != null) swingTimer.stop();
        Board solution = ctrl.rendirse();
        if (solution != null)
            boardPanel.setBoard(solution, currentGame != null ? currentGame.getMaxNumber() : 0);
        lblStatus.setText("Te has rendido. Aquí está la solución.");
        disableControls();
    }

    private void actionSave() {
        if (swingTimer != null) swingTimer.stop();
        String name = JOptionPane.showInputDialog(ctrl.getFrame(),
            "Nombre del archivo (sin extensión):", "Guardar partida",
            JOptionPane.QUESTION_MESSAGE);
        if (name == null) { if (swingTimer != null) swingTimer.start(); return; }
        String err = ctrl.guardarPartida(name.trim(), elapsedMillis());
        if (err != null) {
            JOptionPane.showMessageDialog(ctrl.getFrame(), "Error al guardar: " + err,
                "Error", JOptionPane.ERROR_MESSAGE);
            if (swingTimer != null) swingTimer.start();
        } else {
            JOptionPane.showMessageDialog(ctrl.getFrame(),
                "Partida guardada como  " + (name.isBlank() ? "saved_game" : name) + ".hidato");
            ctrl.mostrarMenuPrincipal();
        }
    }

    private void disableControls() {
        for (JButton b : new JButton[]{btnUndo, btnRedo, btnHint, btnSave})
            b.setEnabled(false);
    }

    // ── Timer ─────────────────────────────────────────────────────────────────

    private void updateTimer() {
        long secs = elapsedMillis() / 1000;
        lblTimer.setText(String.format("%02d:%02d", secs / 60, secs % 60));
    }

    private long elapsedMillis() {
        return accumulatedTime + (System.currentTimeMillis() - startTime);
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
