import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class FluxConnectionOutput implements Runnable {
    private FluxControl fluxCtrl;
    private Map.Entry<int, String> ent;
    private boolean debug;
    private int fluxID;
    private AtomicBoolean running;

    public FluxConnectionOutput(FluxControl fluxCtrl, Map.Entry<int, String> ent, boolean debug, int fluxID, AtomicBoolean running){
        this.fluxCtrl = fluxCtrl;
        this.ent = ent;
        this.debug = debug;
        this.fluxID = fluxID;
        this.running = running;
    }

    @Override
    public void run() {
        boolean notConnected = true;
        DataOutputStream outStream = null;
        Socket clientSocket = null;
        while (notConnected && running.get()) {
            try {
                clientSocket = new Socket(ent.getValue(), ent.getKey());
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
            while(fluxCtrl.getCurrentPacket()[0] != 0 && running.get()) {
                outStream.write(fluxCtrl.getCurrentPacket());
                fluxCtrl.packetSent();
            }
            if (fluxCtrl.getCurrentPacket()[0] == 0) {
                clientSocket.close();
                if(debug) System.out.println("Flux[" + fluxID + "] - End of stream on Output thread!");
            }else{
                clientSocket.close();
                if(debug) System.out.println("Flux[" + fluxID + "] - Output thread killed!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}