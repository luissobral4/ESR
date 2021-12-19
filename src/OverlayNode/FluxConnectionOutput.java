package OverlayNode;

import Util.Address;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class FluxConnectionOutput implements Runnable {
    private FluxControl fluxCtrl;
    private Address adr;
    private boolean debug;
    private int fluxID;
    private AtomicBoolean running;
    private int outId;

    public FluxConnectionOutput(FluxControl fluxCtrl, Address adr, boolean debug, int fluxID, AtomicBoolean running, int outId){
        this.fluxCtrl = fluxCtrl;
        this.adr = adr;
        this.debug = debug;
        this.fluxID = fluxID;
        this.running = running;
        this.outId = outId;
    }

    @Override
    public void run() {
        boolean notConnected = true;
        DataOutputStream outStream = null;
        Socket clientSocket = null;
        while(notConnected) {
            try {
                clientSocket = new Socket(adr.getIp(), adr.getPort());
                outStream = new DataOutputStream(clientSocket.getOutputStream());
                fluxCtrl.outputConnected();
                notConnected = false;
            } catch (IOException e) {
                if(debug) System.out.println("Flux[" + fluxID + "] - Connect failed, waiting and trying again!");
                try {
                    Thread.sleep(2000);//2 seconds
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        }

        try {
            assert outStream != null;
            while(fluxCtrl.getCurrentPacket().length != 1 && running.get()) {
                fluxCtrl.waitNewPacket(outId);
                outStream.write(fluxCtrl.getCurrentPacket());
                fluxCtrl.packetSent();
            }
            if (fluxCtrl.getCurrentPacket().length == 1) {
                clientSocket.close();
                if(debug) System.out.println("Flux[" + fluxID + "] - End of stream on Output thread!");
            }else{
                clientSocket.close();
                if(debug) System.out.println("Flux[" + fluxID + "] - Output thread killed!");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            System.out.println("EXCEPTION FLUXCONNOUT");
        }
    }
}