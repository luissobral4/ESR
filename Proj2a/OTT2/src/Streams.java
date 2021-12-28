import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantLock;

public class Streams {
    private HashMap<Integer, TreeSet<Integer>> streams;
    private ReentrantLock lStream = new ReentrantLock();
    private HashMap<Integer, TreeSet<String>> streamIP;
    private ReentrantLock lIP = new ReentrantLock();

    public Streams() {
        this.streams = new HashMap<>();
        this.lStream = new ReentrantLock();
        this.streamIP = new HashMap<>();
        this.lIP = new ReentrantLock();
    }

    public void addStream(int streamID,int roteID,String ipDest){
        lStream.lock();
        streams.put(streamID,new TreeSet<>(Collections.singleton(roteID)));
        lStream.unlock();
        lIP.lock();
        streamIP.put(streamID,new TreeSet<>(Collections.singleton(ipDest)));
        lIP.unlock();
    }

    public void addStreamRote(int streamID,int roteID,String ipDest){
        lStream.lock();
        streams.get(streamID).add(roteID);
        lStream.unlock();

        lIP.lock();
        streamIP.get(streamID).add(ipDest);
        lIP.unlock();
    }

    public boolean currentStream(int streamID){
        lStream.lock();
        boolean curr = streams.containsKey(streamID);
        lStream.unlock();

        return curr;
    }

    public boolean containsRote(int streamID,int roteID){
        lStream.lock();
        boolean curr = streams.get(streamID).contains(roteID);
        lStream.unlock();

        return curr;
    }

    public ArrayList<String> getStreamDest(int streamID){
        lIP.lock();
        ArrayList<String> ips = new ArrayList<>(streamIP.get(streamID));
        lIP.unlock();

        return ips;
    }
}
