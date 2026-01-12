package FourAces.Client;

import Common.FACP;
import Common.Utility;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class InputFromServerUDP {

    private final DatagramSocket socket;

    public InputFromServerUDP(DatagramSocket socket) {
        this.socket = socket;
    }

    public void send(FACP.CommonMessage msg) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream output = new ObjectOutputStream(out);
        output.writeObject(msg);
        output.flush();
        byte[] data = out.toByteArray();
        DatagramPacket pac = new DatagramPacket(data, data.length, socket.getInetAddress(), socket.getPort());
        socket.send(pac);
    }

    public FACP.CommonMessage receive() throws Exception {
        byte[] buffer = new byte[Utility.MAX_BYTE];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);
        ByteArrayInputStream bais = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
        ObjectInputStream ois = new ObjectInputStream(bais);
        return  (FACP.CommonMessage) ois.readObject();
    }
}
