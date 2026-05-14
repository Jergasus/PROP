package view;

import domini.model.board.Board;
import domini.model.cell.Cell;
import domini.model.cell.CellShape;
import domini.model.cell.Position;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class BoardPanel extends JPanel {

    // ── Palette ────────────────────────────────────────────────────────────
    static final Color BG          = new Color(0x12121E);
    private static final Color C_EMPTY     = new Color(0xF4F4FB);
    private static final Color C_FIXED_BG  = new Color(0x2563EB);
    private static final Color C_FILLED_BG = new Color(0x16A34A);
    private static final Color C_SELECTED  = new Color(0xFACC15);
    private static final Color C_HOVER     = new Color(0xDDDDF5);
    private static final Color C_VOID      = new Color(0x1E1E30);
    private static final Color C_BORDER    = new Color(0x3A3A5C);
    private static final Color C_TYPING    = new Color(0xF97316);
    private static final Color C_TXT_DARK  = new Color(0x1A1A2E);
    private static final Color C_TXT_LIGHT = Color.WHITE;

    private static final int MARGIN      = 24;
    private static final int GAP         = 4;
    private static final int MAX_CS      = 82;
    private static final int MIN_CS      = 28;
    private static final int ARC         = 12;

    // ── State ──────────────────────────────────────────────────────────────
    private Board board;
    private int maxNumber;
    private Position selected;
    private Position hovered;
    private String typingBuffer = "";
    private boolean editorMode;

    // Callbacks
    private BiConsumer<Position, Integer> onConfirm;
    private Consumer<Position> onSelect;

    // ── Constructor ────────────────────────────────────────────────────────
    public BoardPanel() {
        setBackground(BG);
        setFocusable(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e)  { handleClick(e.getX(), e.getY()); }
            @Override public void mouseExited(MouseEvent e)   { hovered = null; repaint(); }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                Position p = pixelToCell(e.getX(), e.getY());
                if (!same(p, hovered)) { hovered = p; repaint(); }
            }
        });
        addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) { handleKey(e); }
        });
    }

    // ── Public API ─────────────────────────────────────────────────────────
    public void setBoard(Board b, int maxN) {
        this.board = b;
        this.maxNumber = maxN;
        selected = null; hovered = null; typingBuffer = "";
        revalidate(); repaint();
    }

    public void setOnConfirm(BiConsumer<Position, Integer> cb) { this.onConfirm = cb; }
    public void setOnSelect(Consumer<Position> cb)             { this.onSelect  = cb; }
    public void setEditorMode(boolean v)   { this.editorMode = v; }
    public Position getSelected()          { return selected; }
    public void clearSelection()           { selected = null; typingBuffer = ""; repaint(); }
    public void refresh()                  { typingBuffer = ""; repaint(); }

    // ── Preferred size (scales with content) ───────────────────────────────
    @Override
    public Dimension getPreferredSize() {
        if (board == null) return new Dimension(500, 500);
        int cs = 70;
        int cols = board.getCols(), rows = board.getRows();
        int w = cols * (cs + GAP) - GAP + 2 * MARGIN;
        int h = rows * (cs + GAP) - GAP + 2 * MARGIN;
        return new Dimension(Math.max(w, 400), Math.max(h, 400));
    }

    // ── Painting ───────────────────────────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        if (board == null) return;
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int cs   = computeCellSize();
        int rows = board.getRows(), cols = board.getCols();
        CellShape shape = board.getCellShape();

        // Board pixel dimensions
        int[] dim = boardPixelDim(cs, rows, cols, shape);
        int ox = (getWidth()  - dim[0]) / 2;
        int oy = (getHeight() - dim[1]) / 2;

        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                drawCell(g, board.getCell(r, c), cs, ox, oy, shape);
    }

    private int[] boardPixelDim(int cs, int rows, int cols, CellShape shape) {
        if (shape == CellShape.HEXAGON) {
            double hw = Math.sqrt(3) * cs;
            int w = (int)(cols * hw + hw / 2);
            int h = (int)(rows * 1.5 * cs + cs);
            return new int[]{w, h};
        } else if (shape == CellShape.TRIANGLE) {
            return new int[]{cols * (cs + GAP), rows * (cs + GAP)};
        } else {
            return new int[]{cols * (cs + GAP) - GAP, rows * (cs + GAP) - GAP};
        }
    }

    private void drawCell(Graphics2D g, Cell cell, int cs, int ox, int oy, CellShape shape) {
        if (cell == null) return;
        int r = cell.getPosition().row(), c = cell.getPosition().col();
        boolean isSel = same(cell.getPosition(), selected);
        boolean isHov = same(cell.getPosition(), hovered) && !isSel;

        Color fill = resolveColor(cell, isSel, isHov);

        switch (shape) {
            case SQUARE   -> drawSquare(g, cell, r, c, cs, ox, oy, fill, isSel);
            case HEXAGON  -> drawHex(g, cell, r, c, cs, ox, oy, fill);
            case TRIANGLE -> drawTri(g, cell, r, c, cs, ox, oy, fill);
        }
    }

    // ── Square ─────────────────────────────────────────────────────────────
    private void drawSquare(Graphics2D g, Cell cell, int r, int c, int cs,
                             int ox, int oy, Color fill, boolean selected) {
        int x = ox + c * (cs + GAP);
        int y = oy + r * (cs + GAP);

        if (cell.isVoid()) {
            g.setColor(fill);
            g.fill(new RoundRectangle2D.Float(x, y, cs, cs, ARC, ARC));
            return;
        }

        // Shadow
        g.setColor(new Color(0, 0, 0, 60));
        g.fill(new RoundRectangle2D.Float(x + 2, y + 3, cs, cs, ARC, ARC));

        // Fill
        g.setColor(fill);
        g.fill(new RoundRectangle2D.Float(x, y, cs, cs, ARC, ARC));

        // Subtle border
        g.setColor(selected ? C_SELECTED.darker() : C_BORDER);
        g.setStroke(new BasicStroke(selected ? 2.5f : 1f));
        g.draw(new RoundRectangle2D.Float(x, y, cs, cs, ARC, ARC));
        g.setStroke(new BasicStroke(1f));

        drawLabel(g, cell, x + cs / 2, y + cs / 2, cs, fill);
    }

    // ── Hexagon ────────────────────────────────────────────────────────────
    private void drawHex(Graphics2D g, Cell cell, int r, int c, int cs,
                          int ox, int oy, Color fill) {
        double hw = Math.sqrt(3) * cs;
        int cx = (int)(ox + c * hw + (r % 2 == 1 ? hw / 2 : 0) + hw / 2);
        int cy = (int)(oy + r * 1.5 * cs + cs);

        int[] xs = new int[6], ys = new int[6];
        for (int i = 0; i < 6; i++) {
            double a = Math.toRadians(60 * i - 30);
            xs[i] = (int)(cx + cs * Math.cos(a));
            ys[i] = (int)(cy + cs * Math.sin(a));
        }
        Polygon hex = new Polygon(xs, ys, 6);

        if (cell.isVoid()) { g.setColor(fill); g.fillPolygon(hex); return; }

        g.setColor(new Color(0, 0, 0, 50));
        g.translate(2, 3); g.fillPolygon(hex); g.translate(-2, -3);

        g.setColor(fill);
        g.fillPolygon(hex);
        g.setColor(C_BORDER);
        g.drawPolygon(hex);

        drawLabel(g, cell, cx, cy, cs, fill);
    }

    // ── Triangle ───────────────────────────────────────────────────────────
    private void drawTri(Graphics2D g, Cell cell, int r, int c, int cs,
                          int ox, int oy, Color fill) {
        boolean up = (r + c) % 2 == 0;
        int x0   = ox + c * (cs + GAP);
        int yTop = oy + r * (cs + GAP);
        int yBot = yTop + cs;
        int xMid = x0 + cs / 2;

        int[] xs = {x0, x0 + cs, xMid};
        int[] ys = up ? new int[]{yBot, yBot, yTop} : new int[]{yTop, yTop, yBot};
        Polygon tri = new Polygon(xs, ys, 3);

        if (cell.isVoid()) { g.setColor(fill); g.fillPolygon(tri); return; }

        g.setColor(fill);
        g.fillPolygon(tri);
        g.setColor(C_BORDER);
        g.drawPolygon(tri);

        int labelY = up ? yBot - cs / 3 : yTop + cs / 3;
        drawLabel(g, cell, xMid, labelY, cs, fill);
    }

    // ── Label ──────────────────────────────────────────────────────────────
    private void drawLabel(Graphics2D g, Cell cell, int cx, int cy, int cs, Color bg) {
        if (cell.isVoid()) return;

        String text;
        Color  textColor;
        Font   font = new Font("SansSerif", Font.BOLD, labelFontSize(cs));

        boolean isSel = same(cell.getPosition(), selected);

        if (isSel && !typingBuffer.isEmpty()) {
            text      = typingBuffer;
            textColor = C_TXT_DARK;
            font      = new Font("SansSerif", Font.BOLD | Font.ITALIC, labelFontSize(cs));
        } else if (cell.getValue() != 0) {
            text      = String.valueOf(cell.getValue());
            textColor = cell.isFixed() ? C_TXT_LIGHT : C_TXT_LIGHT;
        } else {
            return;
        }

        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        int tx = cx - fm.stringWidth(text) / 2;
        int ty = cy + fm.getAscent() / 2 - 1;

        // Subtle text shadow for light bg cells
        if (bg.equals(C_EMPTY) || bg.equals(C_HOVER) || bg.equals(C_SELECTED)) {
            textColor = C_TXT_DARK;
        }
        // Typing buffer is always dark orange
        if (isSel && !typingBuffer.isEmpty()) textColor = C_TYPING;

        g.setColor(textColor);
        g.drawString(text, tx, ty);
    }

    private int labelFontSize(int cs) {
        if (cs >= 60) return 20;
        if (cs >= 44) return 16;
        if (cs >= 32) return 13;
        return 11;
    }

    // ── Color resolution ───────────────────────────────────────────────────
    private Color resolveColor(Cell cell, boolean sel, boolean hov) {
        if (cell.isVoid())  return (sel && editorMode) ? new Color(0x3A3A5C) : C_VOID;
        if (sel)            return C_SELECTED;
        if (hov)            return C_HOVER;
        if (cell.isFixed()) return C_FIXED_BG;
        if (cell.getValue() != 0) return C_FILLED_BG;
        return C_EMPTY;
    }

    // ── Adaptive cell size ─────────────────────────────────────────────────
    private int computeCellSize() {
        if (board == null) return 60;
        int rows = board.getRows(), cols = board.getCols();
        int availW = getWidth()  - 2 * MARGIN;
        int availH = getHeight() - 2 * MARGIN;
        int cs;
        if (board.getCellShape() == CellShape.HEXAGON) {
            int rW = (int)(availW / (cols * Math.sqrt(3) + Math.sqrt(3) / 2));
            int rH = (int)(availH / (rows * 1.5 + 0.5));
            cs = Math.min(rW, rH);
        } else if (board.getCellShape() == CellShape.TRIANGLE) {
            cs = Math.min(availW / Math.max(cols, 1), availH / Math.max(rows, 1));
        } else {
            int csW = (availW + GAP) / Math.max(cols, 1) - GAP;
            int csH = (availH + GAP) / Math.max(rows, 1) - GAP;
            cs = Math.min(csW, csH);
        }
        return Math.max(MIN_CS, Math.min(MAX_CS, cs));
    }

    // ── Click → cell ───────────────────────────────────────────────────────
    private void handleClick(int px, int py) {
        requestFocusInWindow();
        Position pos = pixelToCell(px, py);
        if (pos == null) { selected = null; typingBuffer = ""; repaint(); return; }
        Cell cell = board.getCell(pos);
        if (cell == null) { selected = null; typingBuffer = ""; repaint(); return; }
        if (cell.isVoid()) {
            if (editorMode) {
                selected = pos; typingBuffer = "";
                if (onSelect != null) onSelect.accept(pos);
                repaint();
            } else {
                selected = null; typingBuffer = ""; repaint();
            }
            return;
        }
        if (!editorMode && cell.isFixed()) { return; }
        if (!same(pos, selected)) typingBuffer = "";
        selected = pos;
        if (onSelect != null) onSelect.accept(pos);
        repaint();
    }

    private Position pixelToCell(int px, int py) {
        if (board == null) return null;
        int cs   = computeCellSize();
        int rows = board.getRows(), cols = board.getCols();
        CellShape shape = board.getCellShape();
        int[] dim = boardPixelDim(cs, rows, cols, shape);
        int ox  = (getWidth()  - dim[0]) / 2;
        int oy  = (getHeight() - dim[1]) / 2;

        if (shape == CellShape.SQUARE) {
            int c = (px - ox) / (cs + GAP);
            int r = (py - oy) / (cs + GAP);
            if (r >= 0 && r < rows && c >= 0 && c < cols) return new Position(r, c);
            return null;
        }
        // Hex / Triangle: nearest center
        double bestDist = Double.MAX_VALUE;
        Position best = null;
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++) {
                double[] ctr = cellCenter(r, c, cs, ox, oy, shape);
                double d = Math.hypot(px - ctr[0], py - ctr[1]);
                if (d < bestDist) { bestDist = d; best = new Position(r, c); }
            }
        double thresh = shape == CellShape.HEXAGON ? cs * 1.1 : cs * 0.65;
        return bestDist <= thresh ? best : null;
    }

    private double[] cellCenter(int r, int c, int cs, int ox, int oy, CellShape shape) {
        if (shape == CellShape.HEXAGON) {
            double hw = Math.sqrt(3) * cs;
            double cx = ox + c * hw + (r % 2 == 1 ? hw / 2 : 0) + hw / 2;
            double cy = oy + r * 1.5 * cs + cs;
            return new double[]{cx, cy};
        } else {
            boolean up = (r + c) % 2 == 0;
            double cx = ox + c * (cs + GAP) + cs / 2.0;
            double cy = up ? oy + r * (cs + GAP) + cs * 2.0/3 : oy + r * (cs + GAP) + cs / 3.0;
            return new double[]{cx, cy};
        }
    }

    // ── Keyboard ───────────────────────────────────────────────────────────
    private void handleKey(KeyEvent e) {
        if (selected == null || board == null) return;
        Cell selCell = board.getCell(selected);
        if (selCell == null || selCell.isVoid()) return;
        int key = e.getKeyCode();

        if (isDigit(key)) {
            int d = digitValue(key);
            // Prevent leading zeros
            if (d == 0 && typingBuffer.isEmpty()) return;
            typingBuffer += d;
            if (maxNumber < 10) confirmBuffer();
            else repaint();
            return;
        }
        switch (key) {
            case KeyEvent.VK_ENTER, KeyEvent.VK_TAB    -> confirmBuffer();
            case KeyEvent.VK_BACK_SPACE, KeyEvent.VK_DELETE -> {
                if (!typingBuffer.isEmpty()) { typingBuffer = typingBuffer.substring(0, typingBuffer.length()-1); repaint(); }
                else if (onConfirm != null)  { onConfirm.accept(selected, 0); }
            }
            case KeyEvent.VK_ESCAPE -> { selected = null; typingBuffer = ""; repaint(); }
            case KeyEvent.VK_UP    -> navigate(-1,  0);
            case KeyEvent.VK_DOWN  -> navigate( 1,  0);
            case KeyEvent.VK_LEFT  -> navigate( 0, -1);
            case KeyEvent.VK_RIGHT -> navigate( 0,  1);
        }
    }

    private void confirmBuffer() {
        if (typingBuffer.isEmpty() || selected == null) return;
        try {
            int val = Integer.parseInt(typingBuffer);
            if (onConfirm != null) onConfirm.accept(selected, val);
        } catch (NumberFormatException ignored) {}
        typingBuffer = "";
        repaint();
    }

    private void navigate(int dr, int dc) {
        if (selected == null) return;
        int nr = selected.row() + dr, nc = selected.col() + dc;
        if (nr < 0 || nr >= board.getRows() || nc < 0 || nc >= board.getCols()) return;
        Cell cell = board.getCell(nr, nc);
        if (cell == null || cell.isVoid() || (!editorMode && cell.isFixed())) return;
        selected = new Position(nr, nc);
        typingBuffer = "";
        if (onSelect != null) onSelect.accept(selected);
        repaint();
    }

    private boolean isDigit(int key) {
        return (key >= KeyEvent.VK_0 && key <= KeyEvent.VK_9) ||
               (key >= KeyEvent.VK_NUMPAD0 && key <= KeyEvent.VK_NUMPAD9);
    }
    private int digitValue(int key) {
        return key >= KeyEvent.VK_NUMPAD0 ? key - KeyEvent.VK_NUMPAD0 : key - KeyEvent.VK_0;
    }
    private boolean same(Position a, Position b) {
        if (a == null || b == null) return false;
        return a.row() == b.row() && a.col() == b.col();
    }
}
