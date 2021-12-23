import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ConnectionHandler implements Runnable{
    private BlockingQueue queue;

    public ConnectionHandler(BlockingQueue queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        ServerSocket server = null;
        try {
            server = new ServerSocket(8080);
            System.out.println("[Server] - LISTENING!");

            while (true) {

                Socket client = server.accept();

                String ip = client.getInetAddress().toString();
                String [] split = ip.split("/",2);
                ip = split[1];
                DataInputStream in = new DataInputStream(client.getInputStream());

                String s = in.readUTF();
                String [] packet = s.split(":");

                System.out.println("PACKET TYPE ["+packet[0]+"] RECEIVED  IP "+ip);
                queue.put(ip+":"+s);
                client.close();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }


}
