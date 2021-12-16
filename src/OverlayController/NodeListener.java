package OverlayController;

import Util.Address;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class NodeListener implements Runnable{
    private DataAccessControl dac;


    public NodeListener(DataAccessControl dac){
        this.dac = dac = new DataAccessControl();
    }



    @Override
    public void run() {
        try{
            ServerSocket server = new ServerSocket(5555);

            while(true){
                System.out.println("[OverlayController.NodeListener] - Waiting connection...");
                Socket client = server.accept();
                System.out.println("[OverlayController.NodeListener] - Node at " + client.getInetAddress() + ":" + client.getPort() + " connected!");
                DataOutputStream out = new DataOutputStream(client.getOutputStream());

                String ip = client.getInetAddress().toString();
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
