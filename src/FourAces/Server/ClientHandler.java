package FourAces.Server;

import Common.FACP;
import Common.Utility;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler extends Thread {

    private final int id;
    public final String name;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    private final Core game;

    public ClientHandler(int id, Socket socket, Core game) throws Exception {
        this.id = id;
        this.game = game;
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
        FACP.CommonMessage connectCheck = (FACP.CommonMessage) in.readObject();
        if(connectCheck.isLocked()) {
            boolean check = connectCheck.unLock(Utility.globalPassword);
            if(!check) throw new Exception("Cannot unlock the message from the Client");
        }
        if (connectCheck.getAction() != FACP.ActionType.CONNECT) {
            socket.close();
            throw new Exception("Invalid connection from the Client");
        }
        this.name = (String) connectCheck.getParam("opponent");
        FACP.CommonMessage start = new FACP.CommonMessage(FACP.ActionType.START, ServerHandler.role);
        start.setParam("symbol", game.getSymbol(id));
        if(Utility.securityOn)
            start.lock(Utility.globalPassword);
        out.writeObject(start);
    }

    public synchronized void send(FACP.CommonMessage msg) {
        try {
            out.writeObject(msg);
            out.flush();
        } catch (Exception ignored) {}
    }

    public void close() {
        try {
            in.close();
            out.close();
        } catch (IOException ignored) {}
    }

    @Override
    public void run() {
        try {
            while (true) {
                FACP.CommonMessage msg = (FACP.CommonMessage) in.readObject();
                if(msg.isLocked()) {
                    boolean check = msg.unLock(Utility.globalPassword);
                    if(!check) throw new Exception("Cannot unlock the message from the Client");
                }
                if (msg.getAction() == FACP.ActionType.END) {
                    send(msg);
                    close();
                    break;
                }
                game.handleMessage(id, msg);
            }
        } catch (Exception e) {
            FACP.CommonMessage end = new FACP.CommonMessage(FACP.ActionType.END, ServerHandler.role);
            if(Utility.securityOn)
                end.lock(Utility.globalPassword);
            game.handleMessage(id, end);
        }
    }
}
