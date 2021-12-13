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
    private Map<int,Map<int,InetAddress>> fluxTable; //Tabela de mapeamento de fluxos
    private ReentrantLock tableLock; //Lock para gerir concorrencias no acesso à tabela
    private Map<int,InetAddress> portIP; //Mapeia a porta ao ip para criar uma socket, funciona como uma lista dos próximos nodos do fluxo
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

        this.fluxTable = fluxTable;
        this.tableLock = tableLock;
    }





    @Override
    public void run(){

    }
}

