package FourAces.Client;

import Common.FACP;
import Common.Utility;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Objects;

import static Common.ProtocolToUse.comunicationType;
import static Common.Utility.*;

public class ClientHandler {

    public static final FACP.Role role = FACP.Role.CLIENT;
    public static final int PORT = 5000;
    public static final String HOST = "localhost";
    public static String name = "FourAcesUser:" + (Math.random() * (100000 + 1) - 1);

    public static void main(String args[]) {

        outer.println("\nFourAces " + role + "\tv" + Version + "\tMethod: " + comunicationType + "\n");

        /**
         * Param 1 => Name
         * Param 2 => Use Gui (*)
         * Param 3 => Auto Mode (*)
         */
        if(args.length < 1) {
            outer.println("\nNo valid params inserted, using default");
        } else {
            try{
                name = !Objects.equals(args[0], "*") ? args[0] : "";
                if(Objects.equals(name, "")) throw new Exception("No valid name");
            } catch(Exception e){
                outer.println("\nNo valid params inserted, using default");
            }
        }

        switch (comunicationType) {
            case TCP -> processTCP(args);
            case UDP -> processUDP();
        }

    }

    public static void processTCP(String[] args) {
        boolean gui = args.length > 1 && Objects.equals(args[1], "*");
        boolean auto = args.length > 2 && Objects.equals(args[2], "*");

        try(Socket socket = new Socket(HOST, PORT)) {

            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            FACP.CommonMessage connect = new FACP.CommonMessage(FACP.ActionType.CONNECT, ClientHandler.role);
            connect.setParam("name", name);
            if(Utility.securityOn)
                connect.lock(globalPassword);
            out.writeObject(connect);

            CoreTCP core = new CoreTCP(name, out, auto);
            outer.print("\nConnected to the host, wait for the game to start");
            for(int i = 0; i < 3; i++) {
                Thread.sleep(500);
                outer.print(".");
            }
            outer.print("\n\n");
            if(!gui) {
                InputFromServerTCP input = new InputFromServerTCP(core);
                input.start();
            } else core.startGui();
            Runtime.getRuntime().addShutdownHook(new Thread (() -> {
                FACP.CommonMessage end = new FACP.CommonMessage(FACP.ActionType.END, ClientHandler.role);
                if(securityOn)
                    end.lock(globalPassword);
                core.send(end);
                outer.println("\nYou left the game");
            }));

            while(true) {
                FACP.CommonMessage message = (FACP.CommonMessage) in.readObject();
                if(message.isLocked()) {
                    boolean check = message.unLock(globalPassword);
                    if(!check) throw new Exception("Cannot unlock the message from the Client");
                }
                core.handle(message);
            }

        } catch (Exception e) {
            outer.println("\nClient error: " + e.getMessage());
            System.exit(-1);
        }
    }

    public static void processUDP() {

        try(DatagramSocket socket = new DatagramSocket(PORT)) {

            InputFromServerUDP input = new InputFromServerUDP(socket);
            FACP.CommonMessage connect = new FACP.CommonMessage(FACP.ActionType.CONNECT, ClientHandler.role);
            connect.setParam("name", name);
            if(Utility.securityOn)
                connect.lock(globalPassword);
            input.send(connect);
            CoreUDP core = new CoreUDP(0,0,null, 0, false);
            Runtime.getRuntime().addShutdownHook(new Thread (() -> {
                try {
                    FACP.CommonMessage end = new FACP.CommonMessage(FACP.ActionType.END, ClientHandler.role);
                    if (securityOn)
                        end.lock(globalPassword);
                    input.send(end);
                    outer.println("\nYou left the game");
                } catch (Exception ignored) {}
            }));

            while (true) {
                try {
                    FACP.CommonMessage message = input.receive();
                    if(message.isLocked()) {
                        boolean check = message.unLock(globalPassword);
                        if(!check) throw new Exception("Cannot unlock the message from the Client");
                    }
                    switch (message.getAction()) {

                        case START -> {
                            core = new CoreUDP((int) message.getParam("row"), (int) message.getParam("col"), name, (int) message.getParam("id"), message.getParam("id") == message.getParam("turnOf"));
                            outer.print("\nConnected to the host, wait for the game to start");
                            for(int i = 0; i < 3; i++) {
                                Thread.sleep(500);
                                outer.print(".");
                            }
                            outer.print("\n\n");
                        }

                        case UPDATE -> {
                            int seq = (int) message.getParam("seq");
                            if (seq <= core.lastState) break;
                            core.board = (char[][]) message.getParam("board");
                            core.lastState = seq;
                            int turn = (int) message.getParam("turnOf");
                            core.printBoard();
                            core.turn = (turn == core.id);
                            if (core.turn) {
                                Utility.outer.println("\nMove :");
                                String line = Utility.inner.nextLine();
                                if (line.equalsIgnoreCase("exit"))
                                    System.exit(0);
                                try {
                                    core.sendMove(Integer.parseInt(line), input);
                                } catch (Exception ignored) {}
                            }
                        }

                        case END -> {
                            outer.println("\nThe game ended");
                            System.exit(0);
                        }

                        case END_WIN -> {
                            outer.println("\nYou won game");
                            System.exit(0);
                        }

                        case END_LOST -> {
                            outer.println("\nYou lost game");
                            System.exit(0);
                        }

                        case RESYNC -> {
                            core.board = (char[][]) message.getParam("board");
                            core.printBoard();
                        }
                    }

                } catch (SocketTimeoutException e) {
                    FACP.CommonMessage sync = new FACP.CommonMessage(FACP.ActionType.RESYNC, role, globalPassword, securityOn);
                    input.send(sync);
                }
            }
        } catch (Exception e) {
            outer.println("\nClient error: " + e.getMessage());
            System.exit(-1);
        }
    }
}