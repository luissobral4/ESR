package OverlayController;

import Util.Address;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class NodeListener implements Runnable{
    private DataAccessControl dac;


    public NodeListener(DataAccessControl dac){
        this.dac = dac;
    }



    @Override
    public void run() {
        try{
            ServerSocket server = new ServerSocket(5555);

            while(true){
                System.out.println("Node Listener: Waiting connection...");
                Socket client = server.accept();
                System.out.println("Node Listener: Node at " + client.getInetAddress().getHostAddress() + ":" + client.getPort() + " connected!");
                DataOutputStream out = new DataOutputStream(client.getOutputStream());

                String ip = client.getInetAddress().getHostAddress();
                int port = client.getPort();
                int nodeId = dac.nodeAdd(new Address(ip,port));


                Thread nc = new Thread(new NodeHandler(nodeId, dac, out));
                nc.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
