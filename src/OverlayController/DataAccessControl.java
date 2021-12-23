package OverlayController;

import Util.Address;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class DataAccessControl {
    private HashMap<Integer,HashMap<Integer, ArrayList<Address>>> nodeFluxMap; // {nodeId = {fluxId = [output1,output2,...]}}
    private ReentrantLock nFluxMapLock;
    private AtomicBoolean tableUpdated;

    private HashMap<Integer,Address> nodeAddressMap;
    private ReentrantLock nAddressMLock;

    private ReentrantLock wul;
    private HashMap<Integer,ReentrantLock> waitUpdateLocks;
    private HashMap<Integer,Condition> waitUpdateConds;

    private ReentrantLock wauc;
    private HashMap<Integer,ReentrantLock> locksWaitAllUpdtdCli;
    private HashMap<Integer,Condition> condsWaitAllUpdtdCli;

    private ReentrantLock waun;
    private HashMap<Integer,ReentrantLock> locksWaitAllUpdtdNode;
    private HashMap<Integer,Condition> condsWaitAllUpdtdNode;


    private int nNodesUpdated;
    private int nNodesConnected;
    private boolean alive;
    private Address firstNode;
    private HashMap<Integer,String> fluxFilePath;
    private Graph g;


    public DataAccessControl(){
        String inputFile = "D:\\Coisas\\Aulinhas\\ESR\\src\\Util\\teste.txt";
        this.nodeFluxMap = new HashMap<>();
        this.tableUpdated = new AtomicBoolean(false);
        this.nFluxMapLock = new ReentrantLock();

        this.wul = new ReentrantLock();
        this.waitUpdateLocks = new HashMap<>();
        this.waitUpdateConds = new HashMap<>();

        this.wauc = new ReentrantLock();
        this.locksWaitAllUpdtdCli = new HashMap<>();
        this.condsWaitAllUpdtdCli = new HashMap<>();

        this.waun = new ReentrantLock();
        this.locksWaitAllUpdtdNode = new HashMap<>();
        this.condsWaitAllUpdtdNode = new HashMap<>();


        this.nodeAddressMap = new HashMap<>();
        this.nAddressMLock = new ReentrantLock();

        this.nNodesUpdated = 0;
        this.nNodesConnected = 0;
        this.alive = true;
        this.g = Graph.importGraph(inputFile);
        this.firstNode = g.getFirstNodeAddress();
    }

    public static byte[] serialize(HashMap<Integer, ArrayList<Address>> obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }

    public HashMap<Integer, Address> getNodeAddressMap() {
        HashMap<Integer, Address> ret;
        this.nAddressMLock.lock();
        ret = nodeAddressMap;
        this.nAddressMLock.unlock();
        return ret;
    }



    public String getFluxFilePath(int fluxId){
        return fluxFilePath.get(fluxId);
    }

    public String getFirstNodeIp(){
        return firstNode.getIp();
    }

    public int getFirstNodePort(){
        return firstNode.getPort();
    }

    public boolean hasFlux(int fluxId){
        boolean ret = false;
        nFluxMapLock.lock();
        for(HashMap<Integer, ArrayList<Address>> h : nodeFluxMap.values()){
            if(h.containsKey(fluxId)) {
                ret = true;
                break;
            }
        }
        nFluxMapLock.unlock();
        return ret;
    }


    public boolean nodeUpdated(int nodeId){
        boolean last = false;
        nNodesUpdated++;
        System.out.println("Node["+nodeId+"] thread: New table sent "+nNodesUpdated+"/"+nNodesConnected+"!");
        if(nNodesUpdated == nNodesConnected){
            last = true;
            tableUpdated.set(false);
            for(Integer i : locksWaitAllUpdtdNode.keySet()){
                locksWaitAllUpdtdNode.get(i).lock();
                condsWaitAllUpdtdNode.get(i).signal();
                locksWaitAllUpdtdNode.get(i).unlock();
            }

            for(Integer i : locksWaitAllUpdtdCli.keySet()){
                locksWaitAllUpdtdCli.get(i).lock();
                condsWaitAllUpdtdCli.get(i).signal();
                locksWaitAllUpdtdCli.get(i).unlock();
            }
        }else{
            locksWaitAllUpdtdNode.put(nodeId,new ReentrantLock());
            condsWaitAllUpdtdNode.put(nodeId,locksWaitAllUpdtdNode.get(nodeId).newCondition());
        }
        return last;
    }



    public void updateTable(Request req) throws InterruptedException {
        int target = g.getClientNode(new Address(req.getIp(),req.getPort()));
        int[] overlayNodes = g.getNodesFromIps(this.getNodeAddressMap(),req.getIp());
        System.out.println(Arrays.toString(overlayNodes));
        int[] bestPath = g.getOverlayShortestPath(overlayNodes,target);

        nFluxMapLock.lock();
        int n = bestPath.length;
        for(int i = 0; i < n-1; i++){
            if (!nodeFluxMap.get(bestPath[i]).containsKey(req.getFluxId())) {
                nodeFluxMap.get(bestPath[i]).put(req.getFluxId(), new ArrayList<>());
            }
            nodeFluxMap.get(bestPath[i]).get(req.getFluxId()).add(new Address(g.nodeGetIp(bestPath[i+1]),7777));

        }

        nNodesUpdated = 0;
        tableUpdated.set(true);
        this.locksWaitAllUpdtdCli = new HashMap<>();
        this.condsWaitAllUpdtdCli = new HashMap<>();

        this.locksWaitAllUpdtdNode = new HashMap<>();
        this.condsWaitAllUpdtdNode = new HashMap<>();

        System.out.println("Request Handler: New update (flux "+req.getFluxId()+"), signaling all node threads...");

        System.out.println("\n\n\nncc: "+waitUpdateLocks.toString());
        for(Integer key : waitUpdateLocks.keySet()){
            System.out.println("\n\n\nsignalling");
            waitUpdateLocks.get(key).lock();
            waitUpdateConds.get(key).signal();
            waitUpdateLocks.get(key).unlock();
        }
        System.out.println("Request Handler: signaled all node threads to wake!");

        nFluxMapLock.unlock();
    }




    public void waitTableUpdate(int nodeId) throws InterruptedException {
        while(!tableUpdated.get()){
            waitUpdateLocks.get(nodeId).lock();
            System.out.println("Node["+nodeId+"] thread: Waiting for table update...");
            waitUpdateConds.get(nodeId).await();
            waitUpdateLocks.get(nodeId).unlock();
        }
        System.out.println("Node["+nodeId+"] thread: Table update notification!");
    }

    public void waitAllUpdatedClient(int clientId) throws InterruptedException {
        wauc.lock();
        locksWaitAllUpdtdCli.put(clientId,new ReentrantLock());
        condsWaitAllUpdtdCli.put(clientId,locksWaitAllUpdtdCli.get(clientId).newCondition());
        wauc.unlock();
        while(tableUpdated.get()){
            locksWaitAllUpdtdCli.get(clientId).lock();
            System.out.println("Client["+clientId+"] thread: Waiting for all node threads to send current packet...");
            condsWaitAllUpdtdCli.get(clientId).await();
            locksWaitAllUpdtdCli.get(clientId).unlock();
        }
        System.out.println("Client["+clientId+"] thread: All nodes updated. Resuming!");

    }

    public void waitAllUpdatedNode(int nodeId) throws InterruptedException {
        while(!(nNodesUpdated == nNodesConnected)){
            locksWaitAllUpdtdNode.get(nodeId).lock();
            System.out.println("Node["+nodeId+"] thread: Waiting for all node threads to send current packet...");
            condsWaitAllUpdtdNode.get(nodeId).await();
            locksWaitAllUpdtdNode.get(nodeId).unlock();
        }
        System.out.println("Node["+nodeId+"] thread: All nodes updated. Resuming!");

    }

    public void nodeRemove(int nodeId){
        nAddressMLock.lock();
        nodeAddressMap.remove(nodeId);
        nAddressMLock.unlock();
        nFluxMapLock.lock();
        nodeFluxMap.remove(nodeId);
        waitUpdateLocks.remove(nodeId);
        waitUpdateConds.remove(nodeId);
        nNodesConnected--;
        nFluxMapLock.unlock();
        System.out.println("Node disconnected ("+nodeId+")\nTotal nodes connected: "+nodeAddressMap.toString());
    }

    public int nodeAdd(Address adr){
        int nodeId = g.getOverlayNode(adr);
        nAddressMLock.lock();
        getNodeAddressMap().put(nodeId,adr);
        nAddressMLock.unlock();
        nFluxMapLock.lock();
        nodeFluxMap.put(nodeId,new HashMap<>());
        waitUpdateLocks.put(nodeId, new ReentrantLock());
        waitUpdateConds.put(nodeId, waitUpdateLocks.get(nodeId).newCondition());
        nNodesConnected++;
        nFluxMapLock.unlock();
        System.out.println("Node connected from "+adr.toString()+" node("+nodeId+")\nTotal nodes connected: "+nodeAddressMap.toString());
        return nodeId;
    }


    public byte[] getByteArrayTable(int nodeId) throws IOException {
        nFluxMapLock.lock();
        System.out.println("\n\n\nnfm: "+nodeFluxMap.toString());
        byte[] ret = serialize(nodeFluxMap.get(nodeId));
        nFluxMapLock.unlock();
        return ret;
    }

    public boolean isAlive() {
        return alive;
    }
}
