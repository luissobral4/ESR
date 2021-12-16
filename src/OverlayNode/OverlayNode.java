package OverlayNode;

import Util.Address;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OverlayNode{

    public static HashMap<Integer, ArrayList<Address>> deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return (HashMap<Integer, ArrayList<Address>>) is.readObject();
    }

    public static void runFluxes(TableUpdatesControl tableUpdtCtrl) throws IOException {
        for(Map.Entry<Integer, ArrayList<Address>> ent : tableUpdtCtrl.getFluxTableSet()){
            Thread flux = new Thread(new FluxConnection(ent.getKey(),ent.getValue().size(),tableUpdtCtrl));
            flux.start();
        }
    }


    public static void main(String[] args) {
        try {
 //           Socket socket = new Socket(args[0],Integer.parseInt(args[1]));
            Socket socket = new Socket("127.0.0.1",5555);
            DataInputStream in = new DataInputStream(socket.getInputStream());
            TableUpdatesControl tableUpdtCtrl = new TableUpdatesControl();
            System.out.println("Here!");

            /*in.read(packet);
            HashMap<Integer, HashMap<Integer,String>> read = convertToTable(packet);
            ;
            runFluxes(tableUpdtCtrl);*/
            boolean first = true;
            while(true) {
                System.out.println("[Main] - Waiting for message...");

                byte[] currPacket = new byte[1024];
                int read = in.read(currPacket);
                System.out.println("Raw: " + currPacket);
                if(read < 5) break;
                byte[] trimmedPacket = new byte[read];
                System.arraycopy(currPacket, 0, trimmedPacket, 0, read);
                tableUpdtCtrl.setFluxTable(deserialize(trimmedPacket));

                System.out.println("[Main] - Received " + tableUpdtCtrl.getFluxTable().toString());
                /*if(first){
                    runFluxes(tableUpdtCtrl);
                    first = false;
                }
                else tableUpdtCtrl.signalTableUpdate();*/

            }
        } catch (IOException | ClassNotFoundException e){
            e.printStackTrace();
        }
    }
}
