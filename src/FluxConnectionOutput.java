import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class FluxConnectionOutput implements Runnable {
    private boolean debug = false;
    private int fluxID; //Id do fluxo que esta thread trata
    private HashMap<int, HashMap<int, InetAddress>> fluxTable; //Tabela de mapeamento de fluxos
    private ReentrantLock tableLock; //Lock para gerir concorrencias no acesso à tabela
    private FluxControl fluxCtrl; //Objeto de controlo do fluxo
    private TableUpdatesControl tableUpdtCtrl; //Objeto de controlo dos updates da tabela de fluxos
    private boolean kill,

    public FluxConnectionOutput(int fluxID,
                               HashMap<int, HashMap<int, InetAddress>> fluxTable,
                               ReentrantLock tableLock, FluxControl fluxCtrl) throws IOException {
        this.fluxID = fluxID;


        this.fluxTable = fluxTable;
        this.tableLock = tableLock;
        this.fluxCtrl = fluxCtrl;
        this.kill = false;
    }


    @Override
    public void run() {
        tableLock.lock();
        HashMap<int,HashMap<int, InetAddress>> tableAux = (HashMap<int,HashMap<int, InetAddress>>) fluxTable.clone();


        Thread inThread = new Thread(new FluxConnectionInput(fluxID,fluxTable,tableLock,kill));
        inThread.start();

        for (Map.Entry<int, InetAddress> ent : fluxTable.get(fluxID).entrySet()) {
             Thread outThread = new Thread(new FluxConnectionOutputThread(fluxCtrl, ent, debug, fluxID,kill));
             outThread.start();
        }
        while(fluxTable.containsKey(fluxID)){
            try {
                tableUpdtCtrl.tableUpdated();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(!fluxTable.get(fluxID).equals(tableAux.get(fluxID))){
                tableAux = (HashMap<int, HashMap<int, InetAddress>>) fluxTable.clone();


            }
        }
    }
}


class FluxConnectionOutputThread implements Runnable {
    private FluxControl fluxCtrl;
    private Map.Entry<int, InetAddress> ent;
    private boolean debug;
    private int fluxID;

    public FluxConnectionOutputThread(FluxControl fluxCtrl, Map.Entry<int, InetAddress> ent, boolean debug, int fluxID){
        this.fluxCtrl = fluxCtrl;
        this.ent = ent;
        this.debug = debug;
        this.fluxID = fluxID;
    }

    @Override
    public void run() {
        boolean notConnected = true;
        DataOutputStream outStream = null;
        Socket clientSocket = null;
        while (notConnected) {
            try {
                clientSocket = new Socket(ent.getValue(), ent.getKey());
                outStream = new DataOutputStream(clientSocket.getOutputStream());
                fluxCtrl.outputConnected();
                notConnected = false;
            } catch (IOException e) {
                if(debug) System.out.println("Flux[" + fluxID + "] - Connect failed, waiting and trying again!");
                try {
                    Thread.sleep(2000);//2 seconds
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        }

        try {
            while(fluxCtrl.getCurrentPacket()[0] != 0) {
                outStream.write(fluxCtrl.getCurrentPacket());
                fluxCtrl.packetSent();
            }
            if (fluxCtrl.getCurrentPacket()[0] == 0) {
                clientSocket.close();
                if(debug) System.out.println("Flux[" + fluxID + "] - End of stream on Output thread!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}