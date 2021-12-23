package OverlayNode;

import Util.Address;

import java.io.*;
import java.lang.reflect.Array;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class OverlayNode{

    public static HashMap<Integer, ArrayList<Address>> deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return (HashMap<Integer, ArrayList<Address>>) is.readObject();
    }

    public static void runFluxes(TableUpdatesControl tableUpdtCtrl,HashMap<Integer, ArrayList<Address>> old) throws IOException {
        for(Map.Entry<Integer, ArrayList<Address>> ent : tableUpdtCtrl.getFluxTableSet()){
            if(!old.containsKey(ent.getKey())) {
                Thread flux = new Thread(new FluxConnection(ent.getKey(), ent.getValue().size(), tableUpdtCtrl));
                flux.start();
            }
        }
    }


    public static void main(String[] args) {
        try {
 //           Socket socket = new Socket(args[0],Integer.parseInt(args[1]));
            Socket socket = new Socket("127.0.0.1",5555);
            DataInputStream in = new DataInputStream(socket.getInputStream());
            TableUpdatesControl tableUpdtCtrl = new TableUpdatesControl();
            System.out.println("Here!");

            byte[] currPacket = new byte[16384];
            int read = 0;
            if((read = in.read(currPacket)) < 0) return;
            byte[] trimmedPacket = new byte[read];
            System.arraycopy(currPacket, 0, trimmedPacket, 0, read);

            HashMap<Integer, ArrayList<Address>> aux = tableUpdtCtrl.getFluxTable();
            tableUpdtCtrl.setFluxTable(deserialize(trimmedPacket));
            runFluxes(tableUpdtCtrl,aux);
            while(true) {
                System.out.println("Main thread: Waiting for message...");
                read = in.read(currPacket);
                if(read < 5) break;
                trimmedPacket = new byte[read];
                System.arraycopy(currPacket, 0, trimmedPacket, 0, read);

                aux = tableUpdtCtrl.getFluxTable();
                tableUpdtCtrl.setFluxTable(deserialize(trimmedPacket));
                runFluxes(tableUpdtCtrl,aux);
                tableUpdtCtrl.signalTableUpdate();

                System.out.println("Main thread: Received " + tableUpdtCtrl.getFluxTable().toString());

            }
        } catch (IOException | ClassNotFoundException e){
            e.printStackTrace();
            System.out.println("EXCEPTION OVERLAY NODE");
        }
    }
}
