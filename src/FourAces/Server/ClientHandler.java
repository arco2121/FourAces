package FourAces.Server;

import Common.FACP;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler extends Thread {

    private final int id;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    private final GameCenter game;

    public ClientHandler(int id, Socket socket, GameCenter game) throws Exception {
        this.id = id;
        this.game = game;
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
    }

    public void send(FACP.CommonMessage msg) {
        try {
            out.writeObject(msg);
        } catch (Exception ignored) {}
    }

    @Override
    public void run() {
        try {
            while (true) {
                FACP.CommonMessage msg = (FACP.CommonMessage) in.readObject();
                game.handleMessage(id, msg);
            }
        } catch (Exception e) {
            game.handleMessage(id, new FACP.CommonMessage(FACP.ActionType.END));
        }
    }
}
