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

public class QueueResponder implements Runnable{
    private BlockingQueue queue;
    private Graph g;
    private HashMap<Integer,int[]> routesMap;
    private ReentrantLock l;
    private String packet;

    public QueueResponder(BlockingQueue queue,Graph g,HashMap<Integer,int[]> routes,ReentrantLock l,String packet){
        this.queue = queue;
        this.g = g;
        this.routesMap = routes;
        this.l = l;
        this.packet = packet;
    }

    @Override
    public void run() {
        try {
            System.out.println("PACKET "+packet);
            String[] headers = packet.split(":",3);
            String ip = headers[0];
            String type = headers[1];
            String data = headers[2];

            Socket socket = new Socket(ip, 8080);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            //pedir conexao
            if(type.equals("1")) {
                String adj = "";
                boolean clientNode = g.isCliente(ip);
                if (clientNode) {
                    //calcular vizinos
                    int id = g.getClient(ip);
                    adj = g.getConnections(id);

                    //guardar rota
                    int[] route = g.getOverlayShortestPath(g.getOTT(),id);
                    //String route = g.getRoute(route);
                    int routeID = routesMap.size()+1;
                    l.lock();
                    routesMap.put(routeID,route);
                    l.unlock();
                    clientNode = g.isCliente(route[1]);
                    String ip1 = "";
                    if(clientNode)
                      ip1 = g.cliGetIP(route[1]);
                    else
                      ip1 = g.nodeGetIP(route[1]);

                    queue.put(ip1+":3:"+routeID);
                } else
                    adj = g.getConnections(g.getNode(ip));

                out.writeUTF("2:"+adj);
                out.flush();
                //definir rota
            } else if(type.equals("3")){
                int roteID = Integer.valueOf(data);
                l.lock();
                String route = g.routeToString(routesMap.get(roteID));
                l.unlock();
                System.out.println("DEBUG ROTA "+route);
                out.writeUTF("4:"+data+":"+route);
                out.flush();
            } else if(type.equals("5")){
                  System.out.println("DEBUG PEDIDO STREAM!");
            }
        } catch (IOException | InterruptedException e) {
        e.printStackTrace();
      }
    }
}