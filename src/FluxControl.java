import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class FluxControl {
    private ReentrantLock lock;
    private Condition allConnected;
    private Condition allSent;
    private boolean inputConnected;
    private int nOutputs;
    private int nOutputsConnected;
    private int nPacketsSent;
    private byte[] currentPacket;
    
    /**
    * Método construtor
    */
    public FluxControl(int nOutputs){
        this.lock = new ReentrantLock();
        this.allSent = this.lock.newCondition();
        this.allConnected = this.lock.newCondition();
        this.inputConnected = false;
        this.nOutputs = nOutputs;
        this.nOutputsConnected = 0;
        this.nPacketsSent = 0;
    }

    public void setCurrentPacket(byte[] currentPacket) throws InterruptedException {
        nPacketsSent = 0;
        this.currentPacket = currentPacket;

        while(!(nPacketsSent == nOutputs)){
            this.lock.lock();
            allSent.await();
            this.lock.unlock();
        }
    }

    public byte[] getCurrentPacket() {
        return currentPacket;
    }


    public void waitConnections() throws InterruptedException {
        inputConnected = true;
        while(!(nOutputsConnected == nOutputs && inputConnected)){
            this.lock.lock();
            allConnected.await();
            this.lock.unlock();
        }
    }

    public void outputConnected() {
        this.nOutputsConnected++;
        allConnected.signalAll();
    }

    public void packetSent() {
        nPacketsSent++;
        allSent.signalAll();
    }
}