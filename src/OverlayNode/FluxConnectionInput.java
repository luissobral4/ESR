package OverlayNode;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

public class FluxConnectionInput implements Runnable {
    private boolean debug = false;
    private int fluxID; //Id do fluxo que esta thread trata
    private TableUpdatesControl tableUpdtCtrl;
    private byte[] currPacket; //Pacote atual
    private FluxControl fluxCtrl; //Objeto de controlo do fluxo
    private AtomicBoolean running;

    public FluxConnectionInput(int fluxID,
                               TableUpdatesControl tableUpdtCtrl,
                               AtomicBoolean running) throws IOException {
        this.fluxID = fluxID;
        this.tableUpdtCtrl = tableUpdtCtrl;
        currPacket = new byte[1];
        currPacket[0] = 1;
        this.running = running;
    }


    @Override
    public void run() {
        ServerSocket server = null;
        try {
            server = new ServerSocket(1111+fluxID);
            DataInputStream inpStream;
            Socket client = server.accept();
            inpStream = new DataInputStream(client.getInputStream());

            if(debug) System.out.println("Flux[" + fluxID + "] - Previous node connected: "
                                         + client.getInetAddress().getHostAddress());

            fluxCtrl.waitConnections();
            while(currPacket.length != 1 && running.get()){
                currPacket = new byte[16384];
                int read = 0;
                if((read = inpStream.read(currPacket)) < 0) break;
                byte[] trimmedPacket = new byte[read];
                System.arraycopy(currPacket, 0, trimmedPacket, 0, read);
                fluxCtrl.setCurrentPacket(trimmedPacket);
            }
            if(currPacket.length == 1){
                if(debug) System.out.println("Flux[" + fluxID + "] - End of stream on input thread!");
                tableUpdtCtrl.tableRemove(fluxID);
            }
            server.close();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();

            System.out.println("EXCEPTION FLUXCONNIN");
        }
        try {
            assert server != null;
            server.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}