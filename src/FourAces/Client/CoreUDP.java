package FourAces.Client;

import Common.FACP;
import Common.Utility;

import java.io.IOException;
import java.net.DatagramSocket;

import static Common.Utility.globalPassword;
import static Common.Utility.securityOn;
import static FourAces.Client.ClientHandler.role;

public class CoreUDP {

    private int moveCount = 0;
    public boolean turn = false;
    public int lastState = -1;
    public char[][] board;
    private String name;
    public final int id;

    public CoreUDP(int rows, int col, String name, int id, boolean begin) {
        board = new char[rows][col];
        this.name = name;
        this.id = id;
        turn = begin;
    }

    public void sendMove(int col, InputFromServerUDP sender) throws Exception {
        FACP.CommonMessage move = new FACP.CommonMessage(FACP.ActionType.MOVE, role);
        move.setParam("col", col);
        if(securityOn) move.lock(globalPassword);
        sender.send(move);
    }

    public void printBoard() {
        Utility.outer.println("Board :\n");
        for (char[] r : board) {
            for (char c : r)
                Utility.outer.print(c == '\0' ? '.' : c);
            Utility.outer.println();
        }
        Utility.outer.println("\nTurn of " + (turn ? name : "Opponent"));
    }
}
