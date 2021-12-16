package OverlayController;

import Util.Address;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Graph {
    private HashMap<Address,Integer> nodeIpMap;
    private HashMap<Address,Integer> clientNodeMap;
    private int serverNode;
    private ArrayList<ArrayList<Integer>> graph;
    private int[][] shortestPathMatrix;
    private int ver;

    public Graph(int vertices) {
        this.nodeIpMap = new HashMap<>();
        this.clientNodeMap = new HashMap<>();
        this.ver = vertices;
        graph = new ArrayList<>();

        for (int i = 0; i < vertices; i++)
            graph.add(new ArrayList<>());


    }

    public Address nodeGetAddress(int node){
        Address adr = null;
        for(Map.Entry<Address, Integer> ent : nodeIpMap.entrySet()){
            if(ent.getValue() == node){
                adr = ent.getKey();
            }
        }
        return adr;
    }

    public int[] getNodesFromIps(HashMap<Integer,Address> map){
        Collection<Address> values = map.values();

        int[] arrOfNodes = new int[values.size()];

        int i = 0;
        for(Address val : values){
            arrOfNodes[i] = nodeIpMap.get(val);
        }

        return arrOfNodes;
    }

    public void setShortestPathMatrix(int[][] shortestPathMatrix) {
        this.shortestPathMatrix = shortestPathMatrix;
    }

    public int getVer() {
        return ver;
    }

    public HashMap<Address,Integer> getNodeIpMap() {
        return nodeIpMap;
    }

    public HashMap<Address, Integer> getClientNodeMap() {
        return clientNodeMap;
    }

    public void nodeIpMapPut(int value, Address key) {
        this.nodeIpMap.put(key, value);
    }

    public void clientNodeMapPut(Address key, int value) {
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
            int mode = 0;
            while ((line = br.readLine()) != null) {
                if (line.contains("-")) {
                    mode++;
                } else if (mode == 0) {
                    String[] spl1 = line.split(" ");
                    String[] spl2 = spl1[1].split(":");
                    g.nodeIpMapPut(Integer.parseInt(spl1[0]), new Address(spl2[0],Integer.parseInt(spl2[1])));
                } else if (mode == 3) {
                    g.setServerNode(Integer.parseInt(line));
                } else {
                    String[] spl1 = line.split(" ");
                    String[] spl2 = spl1[1].split(",");
                    for (String n : spl2) {
                        if (mode == 1) {
                            g.addEdge(Integer.parseInt(n), Integer.parseInt(spl1[0]));
                        } else {
                            assert g != null;
                            String[] spl3 = n.split(":");
                            g.clientNodeMapPut(new Address(spl3[0],Integer.parseInt(spl3[1])), Integer.parseInt(spl1[0]));
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
                "\n\nver: " + ver;
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
        System.out.println("Distance to " + to + ": " + to.minDistance);
        int[] path = Dijkstra.getShortestPathTo(to);
        System.out.println("Path: " + Arrays.toString(path));

        return path;
    }

    public int getClientNode(Address adr) {
        return clientNodeMap.get(adr);
    }

    public Integer getOverlayNode(Address adr) {
        return nodeIpMap.get(adr);
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




    /* Driver Code
    public static void main(String[] args) {
        String inputFile = "D:\\Coisas\\Aulinhas\\ESR\\src\\Util\\teste.txt";
        Graph g = importGraph(inputFile);
        assert g != null;
        System.out.println(g.toString());
        int[] aux = new int[]{0,1,2,3,5,6,8,11,10};
        g.getOverlayShortestPath(aux, 10);

    }*/
}