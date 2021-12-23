package OverlayNode;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class FluxConnectionInput implements Runnable {
    private boolean debug = true;
    private int fluxID; //Id do fluxo que esta thread trata
    private TableUpdatesControl tableUpdtCtrl;
    private byte[] currPacket; //Pacote atual
    private FluxControl fluxCtrl; //Objeto de controlo do fluxo
    private AtomicBoolean running;

    public FluxConnectionInput(int fluxID,
                               TableUpdatesControl tableUpdtCtrl,
                               FluxControl fluxCtrl,
                               AtomicBoolean running) throws IOException {
        this.fluxID = fluxID;
        this.tableUpdtCtrl = tableUpdtCtrl;
        currPacket = new byte[8];
        this.fluxCtrl = fluxCtrl;
        this.running = running;
    }


    @Override
    public void run() {
        ServerSocket server = null;
        try {
            server = new ServerSocket(4444+fluxID);
            DataInputStream inpStream;
            Socket client = server.accept();
            inpStream = new DataInputStream(client.getInputStream());

            if(debug) System.out.println("Flux[" + fluxID + "] - Input node connected: " + client.getInetAddress().getHostAddress());



            fluxCtrl.waitConnections();
            byte[] trimmedPacket = {1,2,3,4,5,6};
            while(trimmedPacket.length != 1 && running.get()){
                currPacket = new byte[16384];
                int read = 0;
                System.out.println("Flux[" + fluxID + "] - Input thread: Waiting for new packet...");
                if((read = inpStream.read(currPacket)) < 0) break;
                trimmedPacket = new byte[read];
                System.arraycopy(currPacket, 0, trimmedPacket, 0, read);
                if(debug) System.out.println("Flux[" + fluxID + "] - Input read: " + Arrays.toString(trimmedPacket));
                fluxCtrl.setCurrentPacket(trimmedPacket);
                fluxCtrl.waitAllSent();
            }
            if(trimmedPacket.length == 1){
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