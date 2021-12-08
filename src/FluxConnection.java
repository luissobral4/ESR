import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;


public class FluxConnection implements Runnable{
    private int fluxID; //Id do fluxo que esta thread trata
    private Socket socketIn; //Socket de receção de dados
    private DataInputStream inpStream; //Stream de receção de dados
    private Map<int,Map<int,InetAddress>> fluxTable; //Tabela de mapeamento de fluxos deste nodo
    private ReentrantLock tableLock; //Lock para gerir concorrencias no acesso à tabela
    private Map<int,InetAddress> portIP; //Mapeia a porta ao adress para criar uma socket, funciona como uma lista dos próximos nodos do fluxo
    private Map<Socket,DataOutputStream> socketDOS; //Mapeia a socket de ligação a um dos próximos nodos do fluxo com a respetiva stream de escrita


    public FluxConnection(int fluxID,
                          InetAddress ipIn,
                          int portIn,
                          Map<int,InetAddress> portIP,
                          Map<int,Map<int,InetAddress>> fluxTable,
                          ReentrantLock tableLock) throws IOException {
        this.fluxID = fluxID;
        this.socketIn = new Socket(ipIn, portIn);
        this.inpStream = new DataInputStream(socketIn.getInputStream());
        this.socketDOS = new HashMap<>();
        this.portIP = portIP;

        connectToPeers(portIP);

        this.fluxTable = fluxTable;
        this.tableLock = tableLock;
    }


    //Cria as conexões com todos os próximos nodos para qual o fluxo vai navegar
    public void connectToPeers(Map<int,InetAddress> portIP) throws IOException {
        for(Map.Entry<int,InetAddress> ent : portIP.entrySet()){
            Socket socketOut = new Socket(ent.getValue(), ent.getKey());
            DataOutputStream outStream = new DataOutputStream(socketOut.getOutputStream());
            socketDOS.put(socketOut,outStream);
        }
    }

    public void stopAllConnections() throws IOException {
        socketIn.close(); //Fecha as streams sozinho
        for (Map.Entry<Socket, DataOutputStream> ent : socketDOS.entrySet()) {
            ent.getKey().close();
        }

    }

    public void updateSocketDOS() throws IOException {
        tableLock.lock();
        Map<int,InetAddress> newPeers = fluxTable.get(fluxID);
        if(!portIP.equals(newPeers)){
            for (Map.Entry<Socket, DataOutputStream> ent : socketDOS.entrySet()) {
                ent.getKey().close();
            }
            connectToPeers(newPeers);
        }
        tableLock.unlock();
    }


    public void run(){
        int read = 0;
        while(true) {
            try {
                int count = inpStream.available();
                byte[] dp = new byte[count];
                if (!(read == inpStream.read(dp))) break;
                if(dp[0] == 0){
                    for (Map.Entry<Socket, DataOutputStream> ent : socketDOS.entrySet()) {
                        ent.getValue().write(dp);
                    }
                    stopAllConnections();
                    tableLock.lock();
                    fluxTable.remove(fluxID);
                    tableLock.unlock();
                    break;
                }else if(dp[0] == 1) {
                    updateSocketDOS();
                    for (Map.Entry<Socket, DataOutputStream> ent : socketDOS.entrySet()) {
                        ent.getValue().write(dp);
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}

