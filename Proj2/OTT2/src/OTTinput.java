import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.io.DataOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class OTTinput implements Runnable{
    private BlockingQueue queue;

    public OTTinput(BlockingQueue queue){
        this.queue = queue;
    }

    @Override
    public void run() {
        try {
            //ServerSocket ott = new ServerSocket(8080);
            ServerSocket ott = new ServerSocket(6666);
            System.out.println("[Node] - LISTENING!");

            while(true){
                Socket client = ott.accept();

                String ip = client.getInetAddress().toString();
                String [] split = ip.split("/",2);
                ip = split[1];

                DataInputStream in = new DataInputStream(client.getInputStream());
                String packet = in.readUTF();
                String [] headers = packet.split(":");

                queue.put(ip+":"+packet);
                System.out.println("PACKET TYPE ["+headers[0]+"] RECEIVED     IP "+ip);
                client.close();


            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}

