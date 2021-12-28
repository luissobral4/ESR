import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Server {
    public static void main(String[] args) throws IOException, InterruptedException {
        Graph g = Graph.importGraph( "/home/core/Desktop/Proj2/OTT2/src/teste.txt");
        //Graph g = Graph.importGraph( "/Users/luissobral/OTT/src/teste2.txt");
        HashMap<Integer,int[]> routes = new HashMap<>();
        ReentrantLock l = new ReentrantLock();
        BlockingQueue queue = new ArrayBlockingQueue(1024);
        //HashMap<Integer,TreeSet<String>> streams = new HashMap<>();
        //ReentrantLock lStream = new ReentrantLock();
        Streams streams = new Streams();

        Thread ch = new Thread(new ConnectionHandler(queue));
        ch.start();

        //TimeUnit.SECONDS.sleep(10);

        while(true){
            if(queue.isEmpty())
                TimeUnit.SECONDS.sleep(1);
            else {
                String packet = (String) queue.take();
                Thread tqr = new Thread(new QueueResponder(queue,g,routes,l,packet,streams));
                tqr.start();
            }
        }
    }

/*
        for(String s:connections.values()){
            Thread t = new Thread(new ServerRequest(s));
            t.start();
        }

        while(true);

        //Thread urh = new Thread(new UpdateRoutesHandler(sr,g));
        //urh.start();

        //int serverNode = g.getNode(args[0]);
        //HashMap<Integer,String> connections = g.getServerCon(serverNode);

        //System.out.println("Server node:"+serverNode +"  IP node:" + args[0] + "adj="+connections.toString());


    }*/
}
