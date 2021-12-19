package OverlayController;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class RequestListener implements Runnable{
    DataAccessControl dac;

    public RequestListener(DataAccessControl dac){
        this.dac = dac;
    }

    public int parseReq(String str){
        return Integer.parseInt(str);
    }


    @Override
    public void run() {
        try {
            ServerSocket server = new ServerSocket(5555);
            while(dac.isAlive()){
                Socket client = server.accept();
                DataInputStream in = new DataInputStream(client.getInputStream());



                String ip = client.getInetAddress().toString();
                int port = client.getPort();
                String read = in.readUTF();

                Request req = new Request(parseReq(read),ip,port);


                Thread fh = new Thread(new RequestHandler(dac,req));
                fh.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
