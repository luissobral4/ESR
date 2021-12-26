import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.TimeUnit;

public class OTT {
    public static void main(String[] args) throws IOException, InterruptedException {
        HashMap<Integer,String> connMap = new HashMap<>();
        BlockingQueue queue = new ArrayBlockingQueue(1024);
        ReentrantLock l = new ReentrantLock();
        HashMap<Integer,int[]> routesMap = new HashMap<>();

        //pedir vizinhos
        //Socket socket = new Socket(args[0], 8080);
        Socket socket = new Socket("127.0.0.1", 5555);
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        out.writeUTF("1:d");
        out.flush();
        socket.close();

        Thread tin = new Thread(new OTTinput(queue));
        tin.start();

        if(args.length > 1){
            Thread tr = new Thread(new OTTRequests(queue,routesMap,l));
            tr.start();
        }

        while(true){
            if(queue.isEmpty())
                TimeUnit.SECONDS.sleep(1);
            else {
                String obj = (String) queue.take();
                Thread tout = new Thread(new OTToutput(queue,routesMap,l,obj,connMap));
                tout.start();
            }
        }
    }
}
