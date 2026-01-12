package FourAces.Client;

import Common.Utility;

import javax.swing.*;
import java.awt.*;

public class CoreView extends JFrame {

    private final BoardView board;
    private final JPanel defaultP;

    public CoreView(CoreTCP core) {
        setTitle("FourAces " + ClientHandler.role + "\tv" + Utility.Version + "\t" + core.name);
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        defaultP = new JPanel();
        defaultP.setLayout(new BorderLayout());
        JLabel waitingLabel = new JLabel("Waiting for the game to start...", SwingConstants.CENTER);
        defaultP.add(waitingLabel, BorderLayout.CENTER);
        board = new BoardView(core);
        setVisible(true);
    }

    public void updateBoard(char[][] matrix) {
        board.setBoard(matrix);
    }

    public void startBoard() {
        defaultP.removeAll();
        defaultP.add(board, BorderLayout.CENTER);
        defaultP.revalidate();
        defaultP.repaint();
    }

    public void notValidMove() {
        JOptionPane.showMessageDialog(
                this,
                "Move not valid",
                "FourAces " + ClientHandler.role + "\tv" + Utility.Version,
                JOptionPane.WARNING_MESSAGE
        );
    }
}
