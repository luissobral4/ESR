package OverlayNode;

import Util.Address;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class Test {

    public static byte[] serialize(HashMap<Integer, ArrayList<Address>> obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }
    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(5555);

        System.out.println("Waiting connection...");
        Socket client = server.accept();

        System.out.println("Connected!");
        DataOutputStream out = new DataOutputStream(client.getOutputStream());

        HashMap<Integer, ArrayList<Address>> map = new HashMap<>();
        ArrayList<Address> adr = new ArrayList<>();
        adr.add(new Address("127.0.0.1",6666));
        map.put(0,adr);
        byte[] packet = serialize(map);
        out.write(packet);

        while(true);
    }
}