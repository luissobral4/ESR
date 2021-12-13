import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class ControllerConnection implements Runnable{
    private Socket socket;
    private ObjectInputStream in;
    private DataOutputStream out;
    private HashMap<int,HashMap<int,InetAddress>> fluxTable; //Tabela de mapeamento de fluxos
    private ReentrantLock tableLock; //Lock para gerir concorrencias no acesso à tabela
    private int[] currentFluxes;

    public ControllerConnection(InetAddress ip,
                                int port,
                                HashMap<int,HashMap<int,InetAddress>> fluxTable,
                                ReentrantLock tableLock) throws IOException {
        this.socket = new Socket(ip,port);
        this.in = new ObjectInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());
        this.fluxTable = fluxTable;
        this.tableLock = tableLock;
    }



    @Override
    public void run() {
        try {
            byte[] greeting = new byte[1];
            out.write(greeting);

            HashMap<int,HashMap<int,InetAddress>> read = (HashMap<int,HashMap<int,InetAddress>>) in.readObject();
            runFluxes(read);
            while(read.get(0).get(0) != InetAddress.getByName("0.0.0.0")) {

                read = (HashMap<int,HashMap<int,InetAddress>>) in.readObject();




            }
        } catch (IOException | ClassNotFoundException e){
            e.printStackTrace();
        }
    }
}
