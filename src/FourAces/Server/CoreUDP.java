package FourAces.Server;

import Common.FACP;

import static Common.Utility.globalPassword;
import static Common.Utility.securityOn;
import static FourAces.Server.ServerHandler.role;

public class CoreUDP {
    private final char[][] board;
    private final char[] symbols;
    private int turn = 0;
    private int stateSeq = 0;
    private boolean finished = false;
    private int winner = -1;

    public CoreUDP(int r, int c, char s1, char s2) {
        board = new char[r][c];
        symbols = new char[]{s1, s2};
    }

    public synchronized boolean applyMove(int col) {
        if (finished) return false;
        for (int r = board.length - 1; r >= 0; r--) {
            if (board[r][col] == '\0') {
                board[r][col] = symbols[turn];
                if (check(r, col, symbols[turn])) {
                    finished = true;
                    winner = turn;
                } else if (isFull()) {
                    finished = true;
                    winner = -1;
                }
                turn = 1 - turn;
                stateSeq++;
                return true;
            }
        }
        return false;
    }

    private boolean check(int r, int c, char s) {
        int cnt = 0;
        for (int i = r; i < board.length; i++) {
            if (board[i][c] == s) cnt++; else break;
            if (cnt == 4) return true;
        }
        cnt = 1;
        for (int i = c-1; i>=0 && board[r][i]==s; i--) cnt++;
        for (int i = c+1; i<board[0].length && board[r][i]==s; i++) cnt++;
        if (cnt>=4) return true;

        return false;
    }

    private boolean isFull() {
        for (int c = 0; c < board[0].length; c++)
            if (board[0][c] == '\0') return false;
        return true;
    }

    public synchronized FACP.CommonMessage buildState(ClientHandlerUDP who) {
        FACP.CommonMessage status;
        if (!finished) {
            status = emitBoard(FACP.ActionType.UPDATE);
        }
        else if (winner == -1) status = new FACP.CommonMessage(FACP.ActionType.END, role);
        else {
            status = new FACP.CommonMessage(FACP.ActionType.END_WIN, role);
            status.setParam("winner", who.id);
        }
        status.setParam("turnOf", turn);
        status.setParam("seq", stateSeq);
        if(securityOn) status.lock(globalPassword);
        return status;
    }

    public synchronized FACP.CommonMessage emitBoard(FACP.ActionType type) {
        FACP.CommonMessage status = new FACP.CommonMessage(type, role);
        status.setParam("board", board);
        if(securityOn) status.lock(globalPassword);
        return status;
    }
    public int getTurn() { return turn; }
    public char getSymbol(int id) { return symbols[id]; }
}
