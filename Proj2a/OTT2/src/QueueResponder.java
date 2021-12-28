import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.io.DataOutputStream;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class QueueResponder implements Runnable{
    private BlockingQueue queue;
    private Graph g;
    private HashMap<Integer,int[]> routesMap;
    private ReentrantLock l;
    private Streams streams;
    private String packet;

    public QueueResponder(BlockingQueue queue,Graph g,HashMap<Integer,int[]> routes,ReentrantLock l,String packet, Streams streams){
        this.queue = queue;
        this.g = g;
        this.routesMap = routes;
        this.l = l;
        this.packet = packet;
        this.streams = streams;
    }

    @Override
    public void run() {
        try {
            System.out.println("PACKET "+packet);
            String[] headers = packet.split(":",3);
            String ip = headers[0];
            String type = headers[1];
            String data = headers[2];

            Socket socket = null;
            DataOutputStream out = null;

            //pedir conexao
            if(type.equals("1")) {
                socket = new Socket(ip, 8080);
                //socket =  new Socket("127.0.0.1", 6666);
                out = new DataOutputStream(socket.getOutputStream());
                int id;

                String adj = "";
                boolean clientNode = g.isCliente(ip);
                if (clientNode) {
                    //calcular vizinos
                    id = g.getClient(ip);
                    adj = g.getConnections(id);

                } else {
                    id = g.getNode(ip);
                    adj = g.getConnections(id);
                }
                g.addActive(id);

                out.writeUTF("2:"+adj);
                out.flush();
                socket.close();
             //definir rota
            } else if(type.equals("3")){
                //guardar rota
                int routeID = g.getClient(ip);//routesMap.size()+1;
                int[] route = g.getOverlayShortestPath(g.getOTTActive(),routeID);

                l.lock();
                  routesMap.put(routeID,route);
                l.unlock();
                boolean clientNode = g.isCliente(route[1]);
                String ip2 = "";
                if(clientNode)
                    ip2 = g.cliGetIP(route[1]);
                else
                    ip2 = g.nodeGetIP(route[1]);

                //queue.put(ip1+":3:"+routeID);
                socket = new Socket(ip2, 8080);
                out = new DataOutputStream(socket.getOutputStream());

                String routeS = g.routeToString(route);
                System.out.println("DEBUG ROTA "+route);
                out.writeUTF("4:"+routeID+":"+routeS);
                out.flush();
                socket.close();
             //responder a pedido de stream
            } else if(type.equals("5")){
                String[] streamR = data.split(":",2);
                System.out.println("DEBUG PEDIDO STREAM!");
                int routeID = Integer.valueOf(streamR[1]);
                int streamID = Integer.valueOf(streamR[0]);
                boolean currentStream =  streams.currentStream(streamID);
                boolean rote = false;

                if(!currentStream) {
                    streams.addStream(streamID, routeID, ip);
                    Thread t = new Thread(new ServerVideo(streams, streamID));
                    t.start();
                }
                else {
                    rote = streams.containsRote(streamID,routeID);
                    if(!rote)
                        streams.addStreamRote(streamID,routeID,ip);
                }

                if(!rote) {
                    socket = new Socket(ip, 8080);
                    out = new DataOutputStream(socket.getOutputStream());
                    out.writeUTF("6:" + data);
                    out.flush();
                    socket.close();
                } else System.out.println("ROTE EXISTS");
            }
        } catch (IOException e){//| InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
