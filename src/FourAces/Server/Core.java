package FourAces.Server;

import Common.FACP;
import Common.Utility;

import java.util.ArrayList;
import java.util.List;

public class Core {

    private final char[][] board;
    private final char[] symbols;
    private final ClientHandler[] players;
    private static final List<ClientHandler> handlers = new ArrayList<>();
    private int turn;

    public Core(int rows, int columns, char circle, char cross) {
        board = new char[rows][columns];
        symbols = new char[]{circle, cross};
        players = new ClientHandler[2];
        turn = 0;
    }

    public synchronized void addPlayer(int id, ClientHandler handler) {
        players[id] = handler;
        handlers.add(handler);
        if (players[0] != null && players[1] != null) {
            FACP.CommonMessage changeTurn = new FACP.CommonMessage(FACP.ActionType.CHANGE_TURN, ServerHandler.role);
            FACP.CommonMessage waitTurn = new FACP.CommonMessage(FACP.ActionType.WAIT, ServerHandler.role);
            changeTurn.setParam("opponent", players[1].name);
            waitTurn.setParam("opponent", players[0].name);
            if(Utility.securityOn) {
                changeTurn.lock(Utility.globalPassword);
                waitTurn.lock(Utility.globalPassword);
            }
            players[0].send(changeTurn);
            players[1].send(waitTurn);
        }
    }

    public synchronized void handleMessage(int id, FACP.CommonMessage msg) {
        if (id != turn) return;

        if (msg.getAction() == FACP.ActionType.END) {
            FACP.CommonMessage end = new FACP.CommonMessage(FACP.ActionType.END, ServerHandler.role);
            if(Utility.securityOn)
                end.lock(Utility.globalPassword);
            for (ClientHandler h : players) h.send(msg);
            System.exit(0);
            return;
        }

        if (msg.getAction() == FACP.ActionType.MOVE) {
            int col = (int) msg.getParam("column");
            int row = drop(col, symbols[id]);

            if (row == -1) {
                players[id].send(new FACP.CommonMessage(FACP.ActionType.INVALID, ServerHandler.role));
                return;
            }
            FACP.CommonMessage update = new FACP.CommonMessage(FACP.ActionType.UPDATE, ServerHandler.role);
            update.setParam("board", board);
            if(Utility.securityOn)
                update.lock(Utility.globalPassword);
            players[0].send(update);
            players[1].send(update);
            if (checkVertical(row, col, symbols[id]) || checkHorizontal(row, col, symbols[id])) {
                FACP.CommonMessage mesaageWin = new FACP.CommonMessage(FACP.ActionType.END_WIN, ServerHandler.role);
                FACP.CommonMessage mesaageLost = new FACP.CommonMessage(FACP.ActionType.END_LOST, ServerHandler.role);
                if(Utility.securityOn) {
                    mesaageWin.lock(Utility.globalPassword);
                    mesaageLost.lock(Utility.globalPassword);
                }
                players[id].send(mesaageWin);
                players[1 - id].send(mesaageLost);
                return;
            }
            turn = 1 - turn;
            FACP.CommonMessage changeTurn = new FACP.CommonMessage(FACP.ActionType.CHANGE_TURN, ServerHandler.role);
            FACP.CommonMessage waitTurn = new FACP.CommonMessage(FACP.ActionType.WAIT, ServerHandler.role);
            if(Utility.securityOn) {
                changeTurn.lock(Utility.globalPassword);
                waitTurn.lock(Utility.globalPassword);
            }
            players[turn].send(changeTurn);
            players[1 - turn].send(waitTurn);
        }
    }

    public char getSymbol(int id) { return symbols[id]; }

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

    public static void broadcast(FACP.CommonMessage msg) {
        for (ClientHandler h : handlers) {
            h.send(msg);
            h.close();
        }
    }
}