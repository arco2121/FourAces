package FourAces.Client;

import Common.FACP;
import Common.Utility;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Objects;

import static Common.Utility.*;

public class ClientHandler {

    public static final FACP.Role role = FACP.Role.CLIENT;
    public static final int PORT = 5000;
    public static final String HOST = "localhost";

    public static void main(String args[]) {

        String name = "FourAcesUser:" + (Math.random() * (100000 + 1) - 1);
        outer.println("\nFourAces " + role + "\tv" + Version + "\n");

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
        boolean gui = args.length > 1 ? Objects.equals(args[1], "*") : false;
        boolean auto = args.length > 2 ? Objects.equals(args[2], "*") : false;

        try(Socket socket = new Socket(HOST, PORT)) {

            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            FACP.CommonMessage connect = new FACP.CommonMessage(FACP.ActionType.CONNECT, ClientHandler.role);
            connect.setParam("name", name);
            if(Utility.securityOn)
                connect.lock(globalPassword);
            out.writeObject(connect);

            Core core = new Core(name, out, auto);
            outer.print("\nConnected to the host, wait for the game to start");
            for(int i = 0; i < 3; i++) {
                Thread.sleep(500);
                outer.print(".");
            }
            outer.print("\n\n");
            if(!gui) {
                InputFromServer input = new InputFromServer(core);
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
}