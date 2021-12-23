package OverlayNode;

import Util.Address;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
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
                fluxCtrl.outputConnected(adr.getIp(), adr.getPort(),outId);
                notConnected = false;
            } catch (IOException e) {
                if(debug) System.out.println("Flux[" + fluxID + "] - Output connection to " + adr.getIp() + ":" + adr.getPort() + " failed, waiting and trying again!");
                try {
                    Thread.sleep(2000);//2 seconds
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        }

        try {
            while(fluxCtrl.getCurrentPacket().length != 1 && running.get()) {
                fluxCtrl.waitNewPacket(adr.getIp(),adr.getPort(),outId);
                byte[] currPacket = fluxCtrl.getCurrentPacket();
                outStream.write(currPacket);
                outStream.flush();
                if(debug) System.out.println("Flux[" + fluxID + "] - Wrote:" + Arrays.toString(currPacket) + "\nTo " + adr.getIp() + ":" + adr.getPort());
                boolean last = fluxCtrl.packetSent(adr.getIp(),adr.getPort(),outId);
                if(!last) fluxCtrl.waitAllSentOut(adr.getIp(),adr.getPort(),outId);
            }
            if (fluxCtrl.getCurrentPacket().length == 1) {
                clientSocket.close();
                if(debug) System.out.println("Flux[" + fluxID + "] - End of stream on Output thread!");
            }else{
                clientSocket.close();
                if(debug) System.out.println("Flux[" + fluxID + "] - Output thread killed!");
            }
        } catch (IOException | InterruptedException e) {
            fluxCtrl.removeOut();
            e.printStackTrace();
            System.out.println("EXCEPTION FLUXCONNOUT");
        }
        fluxCtrl.removeOut();
    }
}