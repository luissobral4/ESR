import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.io.DataOutputStream;
import java.util.HashMap;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.Scanner;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class OTTRequests implements Runnable{
    private BlockingQueue queue;
    private HashMap<Integer,int[]> routesMap;
    private ReentrantLock l;

    public OTTRequests(BlockingQueue queue,HashMap<Integer,int[]> routesMap,ReentrantLock l){
        this.queue = queue;
        this.routesMap = routesMap;
        this.l = l;
    }

    public static int lerInt(String m,int min, int max){
        Scanner s = new Scanner(System.in);
        int n = -1;

        do{
            System.out.print(m);
            try {
                String line = s.nextLine();
                n = Integer.parseInt(line);
            } catch (NumberFormatException nfe) {
                System.out.println(nfe.getMessage());
                n = -1;
            }
        } while (n < min || n > max);

        return n;
    }

    @Override
    public void run() {
        try {
            while(true){
                System.out.println("1. Pedir Stream");
                int command = lerInt(">>",1,1);
                int roteID = -1;
                l.lock();
                if(routesMap.size() > 0){
                    Set<Integer> s = routesMap.keySet();
                    for(int i:s)
                        roteID = i;
                }
                l.unlock();

                if(roteID != -1){
                    queue.put("ip:5:"+roteID);

                } else
                    System.out.println("SEM ROTAS DISPONIVEIS!");

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}