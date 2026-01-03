package FourAces.Client;

import Common.FACP;
import Common.Utility;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Objects;

import static Common.Utility.Version;
import static Common.Utility.outer;

public class ClientHandler {

    public static final FACP.Role role = FACP.Role.CLIENT;
    public static final int PORT = 5000;
    public static final String HOST = "localhost";

    public static void main(String args[]) {

        String name = "FourAcesUser:" + (Math.random() * (100000 + 1) - 1);
        outer.println("FourAces " + role + "\tv" + Version);

        if(args.length > 2) {
            outer.println("\nNo valid params inserted, using default");
        } else {
            try{
                name = args[0];
            } catch(Exception e){
                outer.println("\nNo valid params inserted, using default");
            }
        }
        boolean auto = !Objects.equals(args[1], "");

        try(Socket socket = new Socket(HOST, PORT)) {

            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            FACP.CommonMessage connect = new FACP.CommonMessage(FACP.ActionType.CONNECT, ClientHandler.role);
            connect.setParam("name", name);
            if(Utility.securityOn)
                connect.lock(Utility.globalPassword);
            out.writeObject(connect);

            Core core = new Core(name, out, auto);
            InputFromServer input = new InputFromServer(core);
            input.start();
            Runtime.getRuntime().addShutdownHook(new Thread (() -> {
                FACP.CommonMessage end = new FACP.CommonMessage(FACP.ActionType.END, ClientHandler.role);
                core.send(end);
                outer.println("\nYou left the game");
                System.exit(0);
            }));
            while(true) {
                FACP.CommonMessage message = (FACP.CommonMessage) in.readObject();
                if(message.isLocked()) {
                    boolean check = message.unLock(Utility.globalPassword);
                    if(!check) throw new Exception("Cannot unlock the message from the Client");
                }
                core.handle(message);
            }

        } catch (Exception e) {
            outer.println("Error: " + e.getMessage());
        }
    }
}