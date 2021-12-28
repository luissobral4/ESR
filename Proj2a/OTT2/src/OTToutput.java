import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.io.DataOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class OTToutput implements Runnable{
    private BlockingQueue queue;
    private HashMap<Integer,int[]> routesMap;
    private ReentrantLock l;
    private String packet;
    private HashMap<Integer,String> connMap;
    private Streams streams;

    public OTToutput(BlockingQueue queue,HashMap<Integer,int[]> routesMap,ReentrantLock l,String packet,HashMap<Integer,String> connMap,Streams streams){
        this.queue = queue;
        this.routesMap = routesMap;
        this.l = l;
        this.packet = packet;
        this.connMap = connMap;
        this.streams = streams;
    }

    @Override
    public void run() {
        try {
            String[] headers = packet.split(":",3);
            String ip = headers[0];
            String type = headers[1];
            String data = headers[2];

            //Socket socket = new Socket(ip, 8080);
            //DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            Socket socket = null;
            DataOutputStream out = null;

            //definir vizinhos
            if(type.equals("2")) {
                String[] c = data.split("-");

                for(String a:c){
                    String[] node = a.split("/");
                    connMap.put(Integer.valueOf(node[0]),node[1]);
                }System.out.println("DEBUG connections  "+ connMap.toString());
            }
            //definir rota
            else if(type.equals("4")) {
                System.out.println("DEBUG ROTA "+ data);
                String [] route = data.split(":",2);
                String routeID = route[0];
                String [] nodes = route[1].split("-",4);
                int[] arr = new int[1];
                int next = -1;

                if(nodes.length == 2){
                    arr = new int[2];
                    arr[0] = Integer.valueOf(nodes[0]);
                    arr[1] = Integer.valueOf(nodes[1]);

                }
                if(nodes.length > 2){
                    next = Integer.valueOf(nodes[2]);
                    arr = new int[3];
                    arr[0] = Integer.valueOf(nodes[0]);
                    arr[1] = Integer.valueOf(nodes[1]);
                    arr[2] = Integer.valueOf(nodes[2]);
                    String[] nextRoute = route[1].split("-",2);
                    socket = new Socket(connMap.get(next), 8080);
                    //socket = new Socket("127.0.0.1", 5555);
                    out = new DataOutputStream(socket.getOutputStream());
                    out.writeUTF("4:"+routeID+":"+nextRoute[1]);
                    out.flush();
                    socket.close();
                }
                l.lock();
                routesMap.put(Integer.valueOf(routeID),arr);
                l.unlock();
            }
            //pedido de stream para o servidor
            else if(type.equals("5")) {
                int routeID = Integer.valueOf(data);
                int newNode = -1;
                l.lock();
                if(routesMap.containsKey(routeID))
                    newNode = routesMap.get(routeID)[0];
                l.unlock();
                System.out.println("DEBUG "+ newNode);

                if(newNode != -1){
                    socket = new Socket(connMap.get(newNode), 8080);
                    out = new DataOutputStream(socket.getOutputStream());
                    out.writeUTF("5:"+routeID);
                    out.flush();
                    socket.close();
                    System.out.println("PACKET TYPE [5] SEND");
                }
            }
            //transmissao stream
            else if(type.equals("6")) {
                String[] d = data.split(":",2);
                int routeID = Integer.valueOf(d[1]);
                int streamID = Integer.valueOf(d[0]);
                int newNode = -1;
                l.lock();
                if(routesMap.containsKey(routeID) && routesMap.get(routeID).length > 2)
                    newNode = routesMap.get(routeID)[2];
                l.unlock();

                if(newNode != -1){
                    boolean currentStream =  streams.currentStream(streamID);
                    boolean route = false;
                    String newIp = connMap.get(newNode);

                    if(!currentStream) {
                        streams.addStream(streamID, routeID, newIp);
                        Thread t = new Thread(new OTTVideo(streams, streamID));
                        t.start();
                    }
                    else {
                        route = streams.containsRote(streamID,routeID);
                        if(!route)
                            streams.addStreamRote(streamID,routeID,newIp);
                    }

                    if(!route) {
                        socket = new Socket(connMap.get(newNode), 8080);
                        out = new DataOutputStream(socket.getOutputStream());
                        out.writeUTF("6:" + data);
                        out.flush();
                        socket.close();
                        System.out.println("PACKET TYPE [6]");
                    }
                } else{
                    Thread t = new Thread(new ClientVideo());
                    t.start();
                }
                    //System.out.println(d[1]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
