package OverlayNode;

import java.util.ArrayList;
import java.util.HashMap;
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
    private HashMap<Integer,ReentrantLock> locks;
    private HashMap<Integer,Condition> conds;
    private boolean newPacket;
    
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
        this.currentPacket = new byte[1024];
        this.locks = new HashMap<>();
        this.conds = new HashMap<>();
        for (int i = 0; i < nOutputs; i++){
            locks.put(i,new ReentrantLock());
            conds.put(i,locks.get(i).newCondition());
        }
        this.newPacket = false;
    }

    public void setCurrentPacket(byte[] currentPacket) throws InterruptedException {
        this.currentPacket = currentPacket;
        this.newPacket = true;
        for(Integer key : conds.keySet()){
            locks.get(key).lock();
            conds.get(key).signal();
            locks.get(key).unlock();
        }

        this.newPacket = false;

        while(!(nPacketsSent == nOutputs)){
            this.lock.lock();
            allSent.await();
            this.lock.unlock();
        }
        nPacketsSent = 0;
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
        this.lock.lock();
        this.nOutputsConnected++;
        allConnected.signal();
        this.lock.unlock();
    }

    public void packetSent() {
        this.lock.lock();
        nPacketsSent++;
        allSent.signal();
        this.lock.unlock();
    }

    public void waitNewPacket(int outId) throws InterruptedException {
        while(!newPacket) {
            this.locks.get(outId).lock();
            this.conds.get(outId).await();
            this.locks.get(outId).unlock();
        }
    }
}