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
        ServerSocket server;
        try {
            server = new ServerSocket(8888);
            DataInputStream inpStream;
            Socket client = server.accept();
            inpStream = new DataInputStream(client.getInputStream());

            if(debug) System.out.println("Flux[" + fluxID + "] - Previous node connected: "
                                         + client.getInetAddress().getHostAddress());

            fluxCtrl.waitConnections();
            while(currPacket[0] != 0 && running.get()){
                int count = inpStream.available();
                currPacket = new byte[count];
                int read = 0;
                if(!(read == inpStream.read(currPacket))) break;
                fluxCtrl.setCurrentPacket(currPacket);
            }
            if(currPacket[0] == 0){
                if(debug) System.out.println("Flux[" + fluxID + "] - End of stream on input thread!");
                tableUpdtCtrl.tableRemove(fluxID);
            }
            server.close();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}