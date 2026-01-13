package FourAces.Server;

import Common.FACP;
import Common.Utility;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;

public class ClientHandlerUDP {
    public final int id;
    public final SocketAddress address;
    public int lastSeq = -1;

    public ClientHandlerUDP(int id, int col, int row, int turn, SocketAddress address, DatagramSocket socket) throws IOException {
        this.id = id;
        this.address = address;
        FACP.CommonMessage start = new FACP.CommonMessage(FACP.ActionType.START, ServerHandler.role);
        start.setParam("id", id);
        start.setParam("col", col);
        start.setParam("row", row);
        start.setParam("turnOf", turn);
        if(Utility.securityOn)
            start.lock(Utility.globalPassword);
        transmit(start, socket);
    }

    public void transmit(FACP.CommonMessage msg, DatagramSocket socket) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream output = new ObjectOutputStream(out);
        output.writeObject(msg);
        output.flush();
        byte[] data = out.toByteArray();
        DatagramPacket pac = new DatagramPacket(data, data.length, address);
        socket.send(pac);
    }
}
