package FourAces.Client;

import Common.FACP;
import Common.Utility;

import javax.swing.*;
import java.io.ObjectOutputStream;

public class CoreTCP {

    private char[][] board;
    private char symbol;
    public String name;
    private String opponent;
    private boolean myTurn;
    private final ObjectOutputStream out;
    private final boolean auto;
    private CoreView gui = null;

    public CoreTCP(String name, ObjectOutputStream out, boolean auto) {
        this.out = out;
        this.auto = auto;
        this.name = name;
    }

    public synchronized void handle(FACP.CommonMessage msg) throws Exception {

        if(msg.isLocked()) {
            boolean check = msg.unLock(Utility.globalPassword);
            if(!check) throw new Exception("Cannot unlock message from Server");
        }

        switch (msg.getAction()) {

            case START -> {
                symbol = (char) msg.getParam("symbol");
                if(gui != null) SwingUtilities.invokeLater(() -> gui.startBoard());
            }
            case UPDATE -> {
                board = (char[][]) msg.getParam("board");
                opponent = (String) msg.getParam("opponent");
                if(gui != null) SwingUtilities.invokeLater(() -> gui.updateBoard(board)); else printBoard();
            }
            case CHANGE_TURN -> {
                myTurn = true;
                if (auto) makeAutoMove();
            }
            case WAIT -> myTurn = false;
            case INVALID -> {
                if (gui != null) SwingUtilities.invokeLater(() -> gui.notValidMove());
                else Utility.outer.println("Move not valid");
            }
            case END_WIN -> exit("You won");
            case END_LOST -> exit("You lost");
            case END -> exit("Game terminated");

        }
    }

    public synchronized void sendMove(int col) {
        if (!myTurn) return;
        FACP.CommonMessage move = new FACP.CommonMessage(FACP.ActionType.MOVE, ClientHandler.role);
        move.setParam("column", col);
        move.setParam("symbol", symbol);
        if(Utility.securityOn)
            move.lock(Utility.globalPassword);
        send(move);
        myTurn = false;
    }

    private void makeAutoMove() {
        int col = (int) (Math.random() * board[0].length);
        sendMove(col);
    }

    public void send(FACP.CommonMessage msg) {
        try {
            out.writeObject(msg);
        } catch (Exception ignored) {}
    }

    private void printBoard() {
        Utility.outer.println("Board :\n");
        for (char[] r : board) {
            for (char c : r)
                Utility.outer.print(c == '\0' ? '.' : c);
            Utility.outer.println();
        }
        Utility.outer.println("\nTurn of " + (myTurn ? name : opponent));
    }

    private void exit(String msg) {
        Utility.outer.println(msg);
        System.exit(0);
    }

    public void startGui() {
        if(gui == null)
            SwingUtilities.invokeLater(() -> gui = new CoreView(this));
    }

    public boolean isMyTurn() { return myTurn; }
}
