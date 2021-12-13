import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class FluxConnectionInput implements Runnable {
    private boolean debug = false;
    private int fluxID; //Id do fluxo que esta thread trata
    private HashMap<int, HashMap<int, InetAddress>> fluxTable; //Tabela de mapeamento de fluxos
    private ReentrantLock tableLock; //Lock para gerir concorrencias no acesso à tabela
    private byte[] currPacket; //Pacote atual
    private FluxControl fluxCtrl; //Objeto de controlo do fluxo
    private boolean kill;

    public FluxConnectionInput(int fluxID,
                               HashMap<int, HashMap<int, InetAddress>> fluxTable,
                               ReentrantLock tableLock,boolean kill) throws IOException {
        this.fluxID = fluxID;


        this.fluxTable = fluxTable;
        this.tableLock = tableLock;
        currPacket = new byte[1];
        currPacket[0] = 1;
        this.kill = kill;
    }

    public void setKill(boolean kill){
        this.kill = kill;
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
            while(currPacket[0] != 0 && !kill){
                int count = inpStream.available();
                currPacket = new byte[count];
                int read = 0;
                if(!(read == inpStream.read(currPacket))) break;
                fluxCtrl.setCurrentPacket(currPacket);
            }
            if(currPacket[0] == 0){
                if(debug) System.out.println("Flux[" + fluxID + "] - End of stream on input thread!");
                tableLock.lock();
                fluxTable.remove(fluxID);
                tableLock.unlock();
            }
            server.close();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
