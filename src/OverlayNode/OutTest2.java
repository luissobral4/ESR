package OverlayNode;

import Util.Address;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class OutTest2 {

    public static byte[] serialize(HashMap<Integer, ArrayList<Address>> obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }
    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(6667);

        System.out.println("Waiting connection...");
        Socket client = server.accept();
        System.out.println("Connected!");
        while(true) {

            DataInputStream in = new DataInputStream(client.getInputStream());


            byte[] packet = new byte[2048];
            int read = 0;
            if((read = in.read(packet)) < 0) break;
            byte[] trimmedPacket = new byte[read];
            System.arraycopy(packet, 0, trimmedPacket, 0, read);
            System.out.println(Arrays.toString(trimmedPacket));
        }
    }
}
