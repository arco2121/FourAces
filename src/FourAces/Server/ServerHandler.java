package FourAces.Server;

import Common.FACP;

import java.net.ServerSocket;
import java.net.Socket;

import static Common.Utility.Version;
import static Common.Utility.outer;

public class Handler {

    public static final FACP.Role role = FACP.Role.SERVER;
    public static final int PORT = 5000;

    public static void main(String[] args) {

        int rows = 5;
        int columns = 6;
        char circle = 'O';
        char cross = 'X';
        outer.println("FourAces " + role + "\tv" + Version);

        if(args.length < 4) {
            outer.println("\nNo valid params inserted, using default");
        } else {
            try{
                rows = Math.max(Integer.parseInt(args[0]), 5);
                columns = Math.max(Integer.parseInt(args[1]), 6);
                circle = args[2].charAt(0);
                cross = args[3].charAt(0);
            } catch(Exception e){
                outer.println("\nNo valid params inserted, using default");
            }
        }

        try (ServerSocket server = new ServerSocket(PORT)) {

            Socket s1 = server.accept();
            Socket s2 = server.accept();
            GameCenter game = new GameCenter(rows, columns, circle, cross);

            ClientHandler client1 = new ClientHandler(0, s1, game);
            ClientHandler client2 = new ClientHandler(1, s2, game);
            game.addPlayer(0, client1);
            game.addPlayer(1, client2);
            client1.start();
            client2.start();

        } catch (Exception e) {
            outer.println("\nServer Error:\t" + e.getMessage());
        }
    }
}