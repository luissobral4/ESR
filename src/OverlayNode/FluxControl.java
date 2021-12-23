package OverlayNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class FluxControl {
    private boolean debug;
    private int fluxID;

    private ReentrantLock lockPacket;
    private byte[] currentPacket;

    private ReentrantLock lockConn;
    private Condition allConnected;
    private int nOutputsConnected;
    private ReentrantLock nOutputsConnectedLock;

    private ReentrantLock lockAllSent;
    private Condition condAllSent;

    private HashMap<Integer,ReentrantLock> waitMsgLocks;
    private HashMap<Integer,Condition> waitMsgConds;

    private int nPacketsSent;
    private HashMap<Integer,ReentrantLock> locksWaitAllSentOut;
    private HashMap<Integer,Condition> condsWaitAllSentOut;
    private AtomicBoolean newPacket;
    
    /**
    * Método construtor
    */
    public FluxControl(boolean debug, int nOutputs,int fluxID){
        this.debug = debug;
        this.fluxID = fluxID;
        this.lockConn = new ReentrantLock();
        this.allConnected = this.lockConn.newCondition();
        this.nOutputsConnected = 0;
        this.nOutputsConnectedLock = new ReentrantLock();
        this.nPacketsSent = 0;
        this.lockPacket = new ReentrantLock();
        this.currentPacket = new byte[1024];
        this.lockAllSent = new ReentrantLock();
        this.condAllSent = lockAllSent.newCondition();
        this.waitMsgLocks = new HashMap<>();
        this.waitMsgConds = new HashMap<>();
        this.newPacket = new AtomicBoolean(false);
    }

    public void removeOut(){
        nOutputsConnectedLock.lock();
        nOutputsConnected--;
        nOutputsConnectedLock.unlock();
    }


    public void waitConnections() throws InterruptedException {
        while(nOutputsConnected < 1){
            this.lockConn.lock();
            if(debug) System.out.println("Flux["+fluxID+"] Input thread: waiting for a connection to be set...");
            allConnected.await();
            this.lockConn.unlock();
        }
        if(debug) System.out.println("Flux["+fluxID+"] Input thread: At least one connection set!");
    }

    public void outputConnected(String ip, int port, int outId) {
        this.lockConn.lock();
        this.waitMsgLocks.put(outId,new ReentrantLock());
        this.waitMsgConds.put(outId,waitMsgLocks.get(outId).newCondition());
        nOutputsConnectedLock.lock();
        this.nOutputsConnected++;
        nOutputsConnectedLock.unlock();
        allConnected.signal();
        this.lockConn.unlock();
        if(debug) System.out.println("Flux["+ fluxID +"] Output thread("+ip+":"+port+"): Connected!");
    }

    public byte[] getCurrentPacket() {
        this.lockPacket.lock();
        byte[] curr = currentPacket;
        this.lockPacket.unlock();
        return curr;
    }


    public void setCurrentPacket(byte[] currentPacket) throws InterruptedException {
        lockPacket.lock();
        nPacketsSent = 0;
        this.currentPacket = currentPacket;
        newPacket.set(true);
        if(debug) System.out.println("Flux["+fluxID+"] Input thread: New packet received, waking up all output threads...");
        locksWaitAllSentOut = new HashMap<>();
        condsWaitAllSentOut = new HashMap<>();
        for(Integer key : waitMsgConds.keySet()){
            waitMsgLocks.get(key).lock();
            waitMsgConds.get(key).signal();
            waitMsgLocks.get(key).unlock();
        }
        if(debug) System.out.println("Flux["+fluxID+"] Input thread: signaled all output threads wake!");
        this.lockPacket.unlock();
    }


    public boolean packetSent(String ip, int port, int outId) {
        boolean last = false;
        lockAllSent.lock();
        nPacketsSent++;
        if(debug) System.out.println("Flux["+fluxID+"] Output thread("+ip+":"+port+"): Packet sent "+nPacketsSent+"/"+nOutputsConnected+"!");
        if(nPacketsSent == nOutputsConnected){
            last = true;
            newPacket.set(false);
            for(Integer i : locksWaitAllSentOut.keySet()){
                locksWaitAllSentOut.get(i).lock();
                condsWaitAllSentOut.get(i).signal();
                locksWaitAllSentOut.get(i).unlock();

            }
            condAllSent.signal();
        }else{
            locksWaitAllSentOut.put(outId,new ReentrantLock());
            condsWaitAllSentOut.put(outId, locksWaitAllSentOut.get(outId).newCondition());
        }
        lockAllSent.unlock();
        return last;
    }


    public void waitNewPacket(String ip, int port,int outId) throws InterruptedException {
        while((!newPacket.get())){
            waitMsgLocks.get(outId).lock();
            if(debug) System.out.println("Flux["+fluxID+"] Output thread("+ip+":"+port+"): Waiting for new packet...");
            waitMsgConds.get(outId).await();
            waitMsgLocks.get(outId).unlock();
        }
        if(debug) System.out.println("Flux["+fluxID+"] Output thread("+ip+":"+port+"): Notified of new packet!");
    }

    public void waitAllSent() throws InterruptedException {
        while(!(nPacketsSent == nOutputsConnected)){
            lockAllSent.lock();
            if(debug) System.out.println("Flux["+fluxID+"] Input thread: Waiting for all output threads to send current packet...");
            condAllSent.await();
            lockAllSent.unlock();
        }
        if(debug) System.out.println("Flux["+fluxID+"] Input thread: All packets sent. Resuming!");

    }

    public void waitAllSentOut(String ip, int port, int outId) throws InterruptedException {
        while(!(nPacketsSent == nOutputsConnected)){
            locksWaitAllSentOut.get(outId).lock();
            if(debug) System.out.println("Flux["+fluxID+"] Output thread("+ip+":"+port+") waiting for all output threads to send current packet...");
            condsWaitAllSentOut.get(outId).await();
            locksWaitAllSentOut.get(outId).unlock();
        }
    }
}