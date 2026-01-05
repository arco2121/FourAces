package FourAces.Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class BoardView extends JPanel {

    private char[][] board;

    public BoardView(Core core) {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (board == null) return;
                int col = e.getX() / (getWidth() / board[0].length);
                core.sendMove(col);
            }
        });
    }

    public void setBoard(char[][] board) {
        this.board = board;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (board == null) return;

        int rows = board.length;
        int cols = board[0].length;
        int w = getWidth() / cols;
        int h = getHeight() / rows;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                g.drawRect(c * w, r * h, w, h);
                if (board[r][c] != '\0') {
                    g.drawString(
                            String.valueOf(board[r][c]),
                            c * w + w / 2,
                            r * h + h / 2
                    );
                }
            }
        }
    }
}