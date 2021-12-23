package OverlayController;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ClientListener implements Runnable{
    DataAccessControl dac;

    public ClientListener(DataAccessControl dac){
        this.dac = dac;
    }

    public int parseReq(String str){
        return Integer.parseInt(str);
    }


    @Override
    public void run() {
        try {
            ServerSocket server = new ServerSocket(6666);
            int clientId = 0;
            while(dac.isAlive()){
                System.out.println("Request Listener: Waiting connection...");
                Socket client = server.accept();
                DataInputStream in = new DataInputStream(client.getInputStream());
                DataOutputStream out = new DataOutputStream(client.getOutputStream());


                String ip = client.getInetAddress().getHostAddress();
                int port = client.getPort();
                System.out.println("Request Listener: Client at "+ip+":"+port+" connected!");

                out.write(dac.getFluxFileTable());

                String read = in.readUTF();

                Request req = new Request(parseReq(read),ip,port);

                Thread fh = new Thread(new RequestHandler(dac,req,clientId));
                fh.start();
                clientId++;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
