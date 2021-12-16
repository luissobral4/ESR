package OverlayController;

import Util.Address;
import Graph;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class DataAccessControl {
    private HashMap<Integer,HashMap<Integer, ArrayList<Address>>> nodeFluxMap;
    private ReentrantLock nFluxMLock;
    private Condition nFluxMUpdate;
    private boolean tableUpdated;

    private HashMap<Integer,Address> nodeAddressMap;
    private ReentrantLock nAddressMLock;

    private ArrayList<Integer> fluxList;
    private ReentrantLock fluxListLock;

    private boolean alive;
    private int currNodeId;
    private Address firstNode;
    private HashMap<Integer,String> fluxFilePath;
    private Graph g;


    public DataAccessControl(){
        String inputFile = "D:\\Coisas\\Aulinhas\\ESR\\src\\Util\\teste.txt";
        this.nodeFluxMap = new HashMap<>();
        this.nFluxMLock = new ReentrantLock();
        this.nFluxMUpdate = this.nFluxMLock.newCondition();
        this.tableUpdated = false;

        this.nodeAddressMap = new HashMap<>();
        this.nAddressMLock = new ReentrantLock();

        this.fluxList = new ArrayList<>();
        this.fluxListLock = new ReentrantLock();

        this.alive = true;
        this.currNodeId = 1;
        this.g = Graph.importGraph(inputFile);
    }

    public static byte[] serialize(HashMap<Integer, ArrayList<Address>> obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
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
        fluxListLock.lock();
        boolean ret = fluxList.contains(fluxId);
        fluxListLock.unlock();
        return ret;
    }

    public void waitTableUpdate() throws InterruptedException {
        while(!tableUpdated){
            nFluxMUpdate.await();
        }
        tableUpdated = false;
    }


    public void updateTable(Request req){
        int target = g.getClientNode(new Address(req.getIp(),req.getPort()));
        int[] overlayNodes = g.getNodesFromIps(nodeAddressMap);

        int[] bestPath = g.getOverlayShortestPath(overlayNodes,target);

        nFluxMLock.lock();
        int n = bestPath.length;
        for(int i = 0; i < n-1; i++){
            if (!nodeFluxMap.get(bestPath[i]).containsKey(req.getFluxId())) {
                nodeFluxMap.get(bestPath[i]).put(req.getFluxId(), new ArrayList<>());
            }
            nodeFluxMap.get(bestPath[i]).get(req.getFluxId()).add(g.nodeGetAddress(bestPath[i+1]));

        }
        nFluxMLock.unlock();

        tableUpdated = true;
        nFluxMUpdate.signalAll();
    }

    public void nodeRemove(int nodeId){
        nAddressMLock.lock();
        nodeAddressMap.remove(nodeId);
        nAddressMLock.unlock();
        nFluxMLock.lock();
        nodeFluxMap.remove(nodeId);
        nFluxMLock.unlock();
    }

    public int nodeAdd(Address adr){
        int nodeId = g.getOverlayNode(adr);
        nAddressMLock.lock();
        nodeAddressMap.put(nodeId,adr);
        nAddressMLock.unlock();
        nFluxMLock.lock();
        nodeFluxMap.put(nodeId,new HashMap<>());
        nFluxMLock.unlock();
        return nodeId;
    }


    public byte[] getByteArrayTable(int nodeId) throws IOException {
        nFluxMLock.lock();
        byte[] ret = serialize(nodeFluxMap.get(nodeId));
        nFluxMLock.unlock();
        return ret;
    }

    public boolean isAlive() {
        return alive;
    }
}
