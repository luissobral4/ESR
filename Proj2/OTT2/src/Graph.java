import java.io.BufferedReader;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Graph {
    private HashMap<String,Integer> nodeIpMap;
    private HashMap<String,Integer> clientNodeMap;
    private int serverNode;
    private ArrayList<ArrayList<Integer>> graph;
    private int[][] shortestPathMatrix;
    private HashMap<Integer,ArrayList<Integer>> nodeConnetions;
    private int ver;

    public Graph(int vertices) {
        this.nodeIpMap = new HashMap<>();
        this.clientNodeMap = new HashMap<>();
        this.ver = vertices;
        graph = new ArrayList<>();
        nodeConnetions = new HashMap<>();

        for (int i = 0; i < vertices; i++)
            graph.add(new ArrayList<>());
    }

    public String getConnections(int node) {
        String r = "";
        ArrayList<Integer> l = nodeConnetions.get(node);

        if (l.size() == 0)
            return  "-1";
        String ip;

        for(int dest:l) {
            ip = nodeGetIP(dest);
            if (ip.equals(""))
                ip = cliGetIP(dest);
            r = r + (String.valueOf(dest)).concat("/").concat(ip).concat("-");
        }

        return r.substring(0,r.length()-1);
    }

    public String routeToString(int [] route) {
        int size = route.length;
        String r = "";

        for(int i = 0;i < size - 1;i++)
            r = r + route[i]+"-";

        r = r + route[size - 1];

        return r;
    }


    public HashMap<Integer,String> getServerCon(int id)  {
        ArrayList<Integer> l = nodeConnetions.get(id);
        HashMap<Integer,String> map = new HashMap<>();
        String ip;

        for(int n:l) {
            ip = nodeGetIP(n);
            if (ip.equals(""))
                ip = cliGetIP(n);
            map.put(n,ip);
        }

        return map;
    }

    public boolean isCliente(String ip) {
        for (String k:clientNodeMap.keySet())
            if(k.equals(ip))
                return true;
        return false;
    }

    public boolean isCliente(int id) {
        for (int k:clientNodeMap.values())
            if(k == id)
                return true;
        return false;
    }

    public String nodeGetIP(int node){
        String adr = "";
        for(Map.Entry<String, Integer> ent : nodeIpMap.entrySet()){
            if(ent.getValue() == node){
                adr = ent.getKey();
            }
        }
        return adr;
    }

    public String cliGetIP(int node){
        String adr = null;
        for(Map.Entry<String, Integer> ent : clientNodeMap.entrySet()){
            if(ent.getValue() == node){
                adr = ent.getKey();
            }
        }
        return adr;
    }

    public int getClient(String ip) {
        for(Map.Entry<String, Integer> ent : clientNodeMap.entrySet()){
            if(ip.equals(ent.getKey()))
                return ent.getValue();
        }
        return -1;
    }

    public int getNode(String ip) {
        for(Map.Entry<String, Integer> ent : nodeIpMap.entrySet()){
            if(ip.equals(ent.getKey()))
                return ent.getValue();
        }
        return -1;
    }

    public int[] getOTT(){
        int i = 0;
        int[] l = new int[clientNodeMap.size()+nodeIpMap.size()];

        for(int v:nodeIpMap.values())
            l[i++] = v;

        for(int v:clientNodeMap.values())
            l[i++] = v;

        return l;
    }

    public void setShortestPathMatrix(int[][] shortestPathMatrix) {
        this.shortestPathMatrix = shortestPathMatrix;
    }

    public int getVer() {
        return ver;
    }

    public void nodeIpMapPut(int value, String key) {
        this.nodeIpMap.put(key, value);
    }

    public void clientNodeMapPut(String key, int value) {
        this.clientNodeMap.put(key, value);
    }

    public void setServerNode(int serverNode) {
        this.serverNode = serverNode;
    }

    public void addEdge(int u, int v) {
        graph.get(u).add(v);
        graph.get(v).add(u);
    }

    static void floydAlg(int[][] w, int n) {
        for (int k = 0; k < n; k++)
            for (int i = 0; i < n; i++)
                for (int j = 0; j < n; j++)
                    w[i][j] = Math.min(w[i][j], w[i][k] + w[k][j]);
    }

    public int[][] listToMatrix() {
        int[][] aux = new int[ver][ver];
        for (int i = 0; i < ver; i++) {
            for (int j = 0; j < ver; j++) {
                if (graph.get(i).contains(j)) {
                    aux[i][j] = 1;
                } else if (i == j) {
                    aux[i][j] = 0;
                } else {
                    aux[i][j] = 1000000;
                }
            }
        }

        return aux;
    }

    public static Graph importGraph(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line = br.readLine();
            Graph g = new Graph(Integer.parseInt(line));
            int mode = 0,i = 0;
            while ((line = br.readLine()) != null) {
                if (line.contains("-")) {
                    mode++;
                } else if (mode == 0) {
                    String[] spl1 = line.split(" ");
                    g.nodeIpMapPut(Integer.parseInt(spl1[0]), spl1[1]);
                } else if (mode == 3) {
                    g.setServerNode(Integer.parseInt(line));
                } else {
                    String[] spl1 = line.split(" ");
                    if (mode == 2) {
                        assert g != null;
                        g.clientNodeMapPut(spl1[1], Integer.parseInt(spl1[0]));
                    } else {
                        String[] spl2 = spl1[1].split(",");
                        i = Integer.parseInt(spl1[0]);
                        g.nodeConnetions.put(i, new ArrayList<>());
                        for (String n : spl2) {
                            g.addEdge(Integer.parseInt(n), i);
                            g.nodeConnetions.get(i).add(Integer.parseInt(n));
                        }
                    }
                }
            }
            int[][] mtrx = g.listToMatrix();
            floydAlg(mtrx, g.getVer());
            g.setShortestPathMatrix(mtrx);
            return g;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    String matrixToString(int[][] m, int n) {
        StringBuilder aux = new StringBuilder();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                aux.append(m[i][j]);
                aux.append("  ");
            }
            aux.append("\n");
        }
        return aux.toString();
    }

    @Override
    public String toString() {
        return "Graph:" +
                "\n\nnodeIpMap: \n" + nodeIpMap +
                "\n\nclientNodeMap: \n" + clientNodeMap +
                "\n\nserverNode: \n" + serverNode +
                "\n\ngraph: \n" + graph +
                "\n\nshortestPathMatrix: \n" + matrixToString(shortestPathMatrix, ver) +
                "\n\nver: " + ver + "\n\nadj: " + nodeConnetions;
    }

    public int[] getOverlayShortestPath(int[] overlayNodes, int toi){
        int v = overlayNodes.length;
        Vertex from = null;
        Vertex to = null;
        ArrayList<Vertex> vl = new ArrayList<>();
        for(int i = 0; i < v; i++){
            Vertex aux = new Vertex(overlayNodes[i]);
            if(overlayNodes[i] == serverNode){
                from = aux;
            }
            if(overlayNodes[i] == toi){
                to = aux;
            }
            vl.add(aux);
        }

        for(int i = 0; i < v; i++){
            Edge[] el = new Edge[v-1];
            int s = 0;
            for(int j = 0; j < v; j++){
                if(j == i) s = 1;
                if(j != i) el[j-s] = new Edge(vl.get(j),Math.pow(shortestPathMatrix[vl.get(j).getNode()][vl.get(i).getNode()],2));
            }
            vl.get(i).adjacencies = el.clone();
        }

        assert to != null;
        assert from != null;
        Dijkstra.computePaths(from);
        //System.out.println("Distance to " + to + ": " + to.minDistance);
        int[] path = Dijkstra.getShortestPathTo(to);
        //System.out.println("Path: " + Arrays.toString(path));

        return path;
    }

    class Vertex implements Comparable<Vertex>{
        public final int node;
        public Edge[] adjacencies;
        public double minDistance = Double.POSITIVE_INFINITY;
        public Vertex previous;

        public Vertex(int n){
            node = n;
        }

        public int getNode() {
            return node;
        }

        public String toString(){
            return "" + node;
        }

        public int compareTo(Vertex other){
            return Double.compare(minDistance, other.minDistance);
        }

    }

    class Edge{
        public final Vertex target;
        public final double weight;

        public Edge(Vertex argTarget, double argWeight){
            target = argTarget;
            weight = argWeight;
        }
    }

    static class Dijkstra{
        public static void computePaths(Vertex source){
            source.minDistance = 0.;
            PriorityQueue<Vertex> vertexQueue = new PriorityQueue<Vertex>();
            vertexQueue.add(source);

            while (!vertexQueue.isEmpty()) {
                Vertex u = vertexQueue.poll();

                // Visit each edge exiting u
                for (Edge e : u.adjacencies)
                {
                    Vertex v = e.target;
                    double weight = e.weight;
                    double distanceThroughU = u.minDistance + weight;
                    if (distanceThroughU < v.minDistance) {
                        vertexQueue.remove(v);

                        v.minDistance = distanceThroughU ;
                        v.previous = u;
                        vertexQueue.add(v);
                    }
                }
            }
        }

        public static int[] getShortestPathTo(Vertex target){
            List<Vertex> path = new ArrayList<Vertex>();
            for (Vertex vertex = target; vertex != null; vertex = vertex.previous)
                path.add(vertex);

            Collections.reverse(path);

            int[] ret = new int[path.size()];
            for(int i = 0; i < path.size(); i++){
                ret[i] = path.get(i).getNode();
            }
            return ret;
        }
    }


    // Driver Code
    public static void main(String[] args) {
        String inputFile = "/Users/luissobral/Desktop/LEI/4ano/ESR/ESR/src/Util/t1.txt";
        Graph g = importGraph(inputFile);
        assert g != null;
        System.out.println(g.toString());
        int[] aux = new int[]{0,1,2,3,4,5};
        System.out.println(g.getOverlayShortestPath(aux, 5));

    }
}
