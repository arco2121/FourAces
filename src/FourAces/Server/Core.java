package FourAces.Server;

import Common.FACP;

public class GameCenter {

    private final char[][] board;
    private final char[] symbols;
    private final ClientHandler[] players;
    private int turn;

    public GameCenter(int rows, int columns, char circle, char cross) {
        board = new char[rows][columns];
        symbols = new char[]{circle, cross};
        players = new ClientHandler[2];
        turn = 0;
    }

    public synchronized void addPlayer(int id, ClientHandler handler) {
        players[id] = handler;
    }

    public synchronized void handleMessage(int id, FACP.CommonMessage msg) {
        if (id != turn) return;

        if (msg.getAction() == FACP.ActionType.END) {
            players[1 - id].send(new FACP.CommonMessage(FACP.ActionType.END_WIN));
            return;
        }

        if (msg.getAction() == FACP.ActionType.MOVE) {
            int col = (int) msg.getParam("column");
            int row = drop(col, symbols[id]);

            if (row == -1) {
                players[id].send(new FACP.CommonMessage(FACP.ActionType.INVALID));
                return;
            }
            FACP.CommonMessage update = new FACP.CommonMessage(FACP.ActionType.UPDATE);
            update.setParam("board", board);
            update.lock();
            players[0].send(update);
            players[1].send(update);
            if (checkVertical(row, col, symbols[id]) || checkHorizontal(row, col, symbols[id])) {
                players[id].send(new FACP.CommonMessage(FACP.ActionType.END_WIN));
                players[1 - id].send(new FACP.CommonMessage(FACP.ActionType.END_LOST));
                return;
            }
            turn = 1 - turn;
            players[turn].send(new FACP.CommonMessage(FACP.ActionType.CHANGE_TURN));
            players[1 - turn].send(new FACP.CommonMessage(FACP.ActionType.WAIT));
        }
    }

    private int drop(int col, char symbol) {
        if (col < 0 || col >= board[0].length) return -1;
        for (int r = board.length - 1; r >= 0; r--) {
            if (board[r][col] == '\0') {
                board[r][col] = symbol;
                return r;
            }
        }
        return -1;
    }

    private boolean checkVertical(int r, int c, char s) {
        int count = 0;
        for (int i = r; i < board.length; i++) {
            if (board[i][c] == s) count++;
            else break;
        }
        return count >= 4;
    }

    private boolean checkHorizontal(int r, int c, char s) {
        int count = 1;
        for (int i = c - 1; i >= 0 && board[r][i] == s; i--) count++;
        for (int i = c + 1; i < board[0].length && board[r][i] == s; i++) count++;
        return count >= 4;
    }
}