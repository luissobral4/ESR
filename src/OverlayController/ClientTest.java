package OverlayController;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class ClientTest {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("127.0.0.1",6666);
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        out.writeUTF("0");
        ServerSocket server = new ServerSocket(7777);
        Socket conn = server.accept();
        DataInputStream in = new DataInputStream(conn.getInputStream());
        int read = 0;
        byte[] buff = new byte[1024];
        while((read = in.read(buff))!=0){
            byte[] trimmedPacket = new byte[read];
            System.arraycopy(buff, 0, trimmedPacket, 0, read);
            System.out.println("Received: "+ Arrays.toString(trimmedPacket));
        }
    }
}
