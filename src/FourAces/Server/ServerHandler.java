package FourAces.Server;

import Common.FACP;

import java.net.ServerSocket;
import java.net.Socket;

import static Common.Utility.*;

public class ServerHandler {

    public static final FACP.Role role = FACP.Role.SERVER;
    public static final int PORT = 5000;

    public static void main(String[] args) {

        int rows = 5;
        int columns = 6;
        char circle = 'O';
        char cross = 'X';
        outer.println("\nFourAces " + role + "\tv" + Version + "\n");

        /**
         * Param 1 => Rows
         * Param 2 => Columns
         * Param 3 => Circle symbol
         * Param 4 => Cross symbol
         */
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

            outer.print("\nListening for clients");
            for(int i = 0; i < 3; i++) {
                Thread.sleep(500);
                outer.print(".");
            }
            outer.print("\n\n");
            Core game = new Core(rows, columns, circle, cross);
            Socket s1 = server.accept();
            ClientHandler client1 = new ClientHandler(0, s1, game);
            outer.println("Client number 1 connected\n");
            Socket s2 = server.accept();
            ClientHandler client2 = new ClientHandler(1, s2, game);
            outer.println("Client number 2 connected\n");
            game.addPlayer(0, client1);
            game.addPlayer(1, client2);
            client1.start();
            client2.start();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    FACP.CommonMessage end = new FACP.CommonMessage(FACP.ActionType.END, role);
                    if(securityOn) end.lock(globalPassword);
                    outer.println("The game was ended");
                    game.broadcast(end);
                } catch (Exception ignored) {}
            }));

        } catch (Exception e) {
            outer.println("\nServer error:\t" + e.getMessage());
        }
    }
}