import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class OverlayNode {
    private Map<int, Map<int, InetAddress>> fluxTable;
    private ReentrantLock tableLock; //Lock para gerir concorrencias no acesso à tabela





    public static void main(String[] args) {
        Thread controllerThread = new Thread

        while(true){

        }
    }
}
