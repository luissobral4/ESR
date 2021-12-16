import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TTTT {
    public static void main(String[] args) throws IOException {
        ServerSocket sv = new ServerSocket(5678);

        System.out.println("Waiting...");
        Socket client = sv.accept();
        DataInputStream in = new DataInputStream(client.getInputStream());

        String str = in.readUTF();

        System.out.println("Received: " + str);



    }
}
