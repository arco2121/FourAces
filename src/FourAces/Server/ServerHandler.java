package FourAces.Server;

import Common.FACP;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

import static Common.Utility.*;

public class ServerHandler {

    public static final FACP.Role role = FACP.Role.SERVER;
    public static final int PORT = 5000;
    public static final FACP.ComunicationType medium =  FACP.ComunicationType.UDP;
    public static int rows = 5;
    public static int columns = 6;
    public static char circle = 'O';
    public static char cross = 'X';

    public static void main(String[] args) {

        outer.println("\nFourAces " + role + "\tv" + Version + "\tMethod: " + medium + "\n");

        /**
         * Param 1 => Rows
         * Param 2 => Columns
         * Param 3 => Circle symbol
         * Param 4 => Cross symbol
         */
        if(args.length < 4) {
            outer.println("\nNo valid params inserted, using default");
        } else {
            try {
                rows = Math.max(Integer.parseInt(args[0]), 5);
                columns = Math.max(Integer.parseInt(args[1]), 6);
                circle = args[2].charAt(0);
                cross = args[3].charAt(0);
            } catch (Exception e) {
                outer.println("\nNo valid params inserted, using default");
            }
        }

        switch (medium) {
            case TCP -> processTCP();
            case UDP -> processUDP();
        }
    }

    public static void processTCP() {

        try (ServerSocket server = new ServerSocket(PORT)) {

            outer.print("\nListening for clients");
            for(int i = 0; i < 3; i++) {
                Thread.sleep(500);
                outer.print(".");
            }
            outer.print("\n\n");
            CoreTCP game = new CoreTCP(rows, columns, circle, cross);
            Socket s1 = server.accept();
            ClientHandlerTCP client1 = new ClientHandlerTCP(0, s1, game);
            outer.println("Client number 1 connected\n");
            Socket s2 = server.accept();
            ClientHandlerTCP client2 = new ClientHandlerTCP(1, s2, game);
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

    public static void  processUDP() {

        try(DatagramSocket socket = new DatagramSocket(PORT)) {

            outer.print("\nListening for clients");
            for(int i = 0; i < 3; i++) {
                Thread.sleep(500);
                outer.print(".");
            }

            Map<SocketAddress, ClientHandlerUDP> players = new HashMap<>();
            CoreUDP game = new CoreUDP(rows, columns, circle, cross);
            byte[] buf = new byte[MAX_BYTE];
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    FACP.CommonMessage end = new FACP.CommonMessage(FACP.ActionType.END, role);
                    if(securityOn) end.lock(globalPassword);
                    outer.println("The game was ended");
                    for (ClientHandlerUDP one : players.values())
                        one.transmit(end, socket);
                } catch (Exception ignored) {}
            }));

            while (true) {

                DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length);
                socket.receive(datagramPacket);
                ByteArrayInputStream data = new ByteArrayInputStream(datagramPacket.getData(), 0, datagramPacket.getLength());
                FACP.CommonMessage message = (FACP.CommonMessage) new ObjectInputStream(data).readObject();
                SocketAddress addr = datagramPacket.getSocketAddress();
                if(message.isLocked()) {
                    boolean check = message.unLock(globalPassword);
                    if(check) {
                        throw new Exception("Cannot unlock the message");
                    }
                }

                switch (message.getAction()) {

                    case CONNECT -> {
                        if (players.size() >= 2 || players.containsKey(addr)) continue;
                        int id = players.size();
                        ClientHandlerUDP pl = new ClientHandlerUDP(id, addr, socket);
                        players.put(addr, pl);
                    }

                    case MOVE -> {
                        if (!players.containsKey(addr)) continue;
                        int id = (int) message.getParam("id");
                        int seq = (int) message.getParam("seq");
                        int col = (int) message.getParam("col");
                        ClientHandlerUDP pl = players.get(addr);
                        if (seq <= pl.lastSeq || id != game.getTurn()) continue;
                        pl.lastSeq = seq;
                        if (!game.applyMove(col)) continue;
                        FACP.CommonMessage state = game.buildState(pl);
                        FACP.ActionType action = state.getAction();
                        int winner = (int) state.getParam("winner");
                        if (securityOn) state.lock(globalPassword);
                        for (ClientHandlerUDP one : players.values())
                        {
                            if(action == FACP.ActionType.UPDATE || action == FACP.ActionType.END)
                                one.transmit(state, socket);
                            else if(action == FACP.ActionType.END_WIN && winner != one.id)
                                one.transmit(new FACP.CommonMessage(FACP.ActionType.END_LOST, role, globalPassword, securityOn), socket);
                            else if(action == FACP.ActionType.END_WIN)
                                one.transmit(new FACP.CommonMessage(FACP.ActionType.END_WIN, role, globalPassword, securityOn), socket);
                        }
                    }

                    case RESYNC -> {
                        if (players.containsKey(addr)) {
                            FACP.CommonMessage state = new FACP.CommonMessage(FACP.ActionType.RESYNC, role);
                            state.setParam("board", game.getBoard());
                            if (securityOn) state.lock(globalPassword);
                            for (ClientHandlerUDP one : players.values())
                                one.transmit(state, socket);
                        }
                    }
                }
            }

        } catch (Exception e) {
            outer.println("\nServer error:\t" + e.getMessage());
        }
    }
}