package com.company;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Stack;

public class Graph {
    ArrayList<GraphNode> nodeList;
    ArrayList<GraphEdge> edgeList;

    public Graph() {
        this.nodeList = new ArrayList<GraphNode>();
        this.edgeList = new ArrayList<GraphEdge>();
    }

    // Persistence: get Websites from storage and put links in storage
    public void persistEdges() throws IOException {
        FileOutputStream f = new FileOutputStream(new File("edges.txt"));
        ObjectOutputStream o = new ObjectOutputStream(f);

        o.writeObject(edgeList);

        o.close();
        f.close();
    }

    //Get links from storage
    public void getEdges() throws IOException, ClassNotFoundException {
        FileInputStream f = new FileInputStream(new File("edges.txt"));
        ObjectInputStream o = new ObjectInputStream(f);

        ArrayList<GraphEdge> ge = (ArrayList<GraphEdge>) o.readObject();
        this.edgeList = ge;

        o.close();
        f.close();
    }


    // Graph Functionality Stuff

    public GraphNode findHighestParent(GraphNode i){
    // find the subset of both vertices of every edge
        // If both subsets are the same, then there is cycle in the graph
        // a utility function to find the subset of an element i


        // you have two nodes and you want to go back until you hit their roots
        // so you want to go to their parents until you get to their final parents
        // if theyre the same theyre in the same set
        if(i.parent == null || i == i.parent){
            return i;
        } else {
            return findHighestParent(i.parent);
        }
    }

    public void Union(GraphNode x, GraphNode y){
       GraphNode xnew = findHighestParent(x);
       GraphNode ynew = findHighestParent(y);

        if(xnew.nodeName.equals(ynew.nodeName)){
            return;
        } else {
            xnew = ynew;
            ynew = xnew;
            ynew.parent = xnew;
        }
    }

    public int countCycles(){
        System.out.println("counting...");
        int numCycles = 0;
        ArrayList<GraphNode> anotherNodeList = nodeList;

        ArrayList<String> alreadyRemembered = new ArrayList<>();
        
        // hash set of visited nodes and queue of union nodes
        for(int i=0; i<anotherNodeList.size() -2; i++){
            GraphNode insertx = anotherNodeList.get(i);
            GraphNode inserty = anotherNodeList.get(i+1);
            GraphNode x = findHighestParent(insertx);
            GraphNode y = findHighestParent(inserty);


            if(!x.nodeName.equals(y.nodeName)){
                String insertedvalue = x.nodeName + y.nodeName;
                String anotherinsertedvalue = y.nodeName + x.nodeName;

                if(!alreadyRemembered.contains(insertedvalue) && !alreadyRemembered.contains(anotherinsertedvalue)) {
                    numCycles++;
                    Union(x, y);
                    alreadyRemembered.add(insertedvalue);
                    alreadyRemembered.add(anotherinsertedvalue);
                }
            }

        }

        return numCycles;
    }

    public static double getDistBetweenTwoNodes(GraphNode target, GraphNode potentialParent){
        double distance = 0;

        GraphNode parent = target.parent;
        while(parent != null){
            distance = distance + (target.nodeData.similarity - parent.nodeData.similarity);

            if(parent.nodeData.getNodeName().equals(potentialParent.nodeName)){
                return distance;
            }

            if(parent.parent.nodeName.equals(parent.nodeName)){
                break;
            }
            parent = parent.parent;
        }
        
     return Double.POSITIVE_INFINITY;


    }


    //TODO: change all distance metrics to abs value
    public Stack<GraphNode> dijkstra(GraphNode source, GraphNode target){
        System.out.println("dijkstra");
        Stack<GraphNode> finishedPath = new Stack<GraphNode>();

        //distances
         HashMap<GraphNode, Double> dist = new HashMap<GraphNode, Double>();
        //all nodes satisfying condition
        HashMap<GraphNode, GraphNode> prev = new HashMap<GraphNode, GraphNode>();

        // initialize queue
        PriorityQueue<GraphNode> Q = new PriorityQueue<>(GraphNode.weightCompare);

        //prev.put(source, null); // undefined
        Q.add(source);
        dist.put(source, 0.0);
        prev.put(source, null);
        source.visited = true;
        // dist.put(gn, gn.nodeData.similarity);


        // vertices are nodes and edges are edges
       for(GraphNode gn: nodeList) {
            if (!gn.nodeName.equals(source.nodeName)) {
                dist.put(gn, Double.POSITIVE_INFINITY); // unknown distance
                prev.put(gn, null); // undefined
                // dist.put(gn, gn.nodeData.similarity);
            }
        }


        // only put the source in to start
        // mark it and if youve visited it before stop doing that

        //GraphNode previous = null;
            while(!Q.isEmpty()){
                GraphNode u = Q.poll();
                // prev.put(u, u.parent);

                if(u.nodeName.equals(target.nodeName)){
                    // push it onto a stack
                    // retrieve all from prev hashmap
                    // while osme node isnt null se tto predecesoor and continue

                    System.out.println("Building path");
                    finishedPath.push(u);

                   // u = previous;
                    while(u != null){
                       GraphNode pred = prev.get(u);
                       finishedPath.push(pred);
                       u = pred;
                    }

                    return finishedPath;

                }

                for(GraphNode v: u.neighbors){
                    System.out.println("Checking neighbors " + u.nodeData.getNodeName());
                    double alt = 0.0;
                    alt = v.nodeData.similarity + (getDistBetweenTwoNodes(target, u));
                    if(u.nodeData.getNodeName() == "Main_Page"){
                        System.out.println("I get stuck here");
                    }
                            if(alt < dist.get(v) && !v.visited){
                                dist.put(v, alt);
                                prev.put(v, u); // u and v or v and u?
                                v.visited = true;
                   //             previous = v;
                                Q.add(v);
                            }
                }
            }

        return null;

    }

    public ArrayList<Website> collectAllNodes(){
        // for similarity metrics

        ArrayList<Website> websiteList = new ArrayList<>();
        for(GraphNode gn: nodeList){
            websiteList.add(gn.nodeData);
        }
        return websiteList;
    }

    public HashMap<Website, Integer> getNodes(){
        HashMap<Website, Integer> allnodes = new HashMap<>();
        for(int i=0; i<nodeList.size(); i++){
            allnodes.put(nodeList.get(i).nodeData, i);
        }
        return allnodes;
    }

    private boolean inNodeList(String node){
        for(GraphNode gn: nodeList){
            if(gn.nodeName.equals(node)){
                return true;
            }
        }
        return false;
    }

    private ArrayList<Website> getStoredSites() throws Exception {
        ArrayList<Website> allWebsites = new ArrayList<>();

        File checkDir = new File(System.getProperty("user.dir"));
        File[] allDir = checkDir.listFiles();

        System.out.println("Checking our storage");
        for (File dir : allDir) {
            if (dir.isFile() && dir.toString().endsWith(".txt") && !dir.toString().endsWith("startURL.txt")) {
                System.out.println("Adding from storage: " + dir.toString());
            }
        }
        return allWebsites;
    }

    private ArrayList<Website> getStartSites() throws IOException {
        System.out.println("Building graph getting start sites");
        String filePath = System.getProperty("user.dir") + File.separator + "startURL.txt";
        BufferedReader Buff = new BufferedReader(new FileReader(filePath));

        String line = "";
        ArrayList<Website> allWebsites = new ArrayList<Website>();

        while ((line = Buff.readLine()) != null) {
            System.out.println(line);
            Website w = new Website(line);
            w.getContents();
            w.getConnectedSites();
            w.setWords();
            w.makePersistent();
            allWebsites.add(w);
            GraphNode gn = new GraphNode(w);
           //nodeList.add(gn);
        }

        return allWebsites;
    }

    private void extendGraph(ArrayList<GraphNode> toExtend, int requiredDepth) throws IOException {
        ArrayList<GraphNode> childrenSites = new ArrayList<>();

        for (GraphNode child : toExtend) {

            if (nodeList.size() >= requiredDepth) {
                break;
            }

            for (Website deeperChild : child.nodeData.connectedSites) {
                System.out.println("extending by snail check: " + deeperChild.URL);
                if(!inNodeList(deeperChild.getNodeName())){

                    deeperChild.getContents();
                    deeperChild.getConnectedSites();
                    deeperChild.setWords();
                    deeperChild.makePersistent();
                    System.out.println("Adding website: " + deeperChild.URL);

                    GraphNode deeperChildNode = new GraphNode(deeperChild);
                    nodeList.add(deeperChildNode);
                    childrenSites.add(deeperChildNode);

                    deeperChildNode.parent = child;
                    GraphEdge newEdge = new GraphEdge(child, deeperChildNode);
                    edgeList.add(newEdge);

                    //deeperChildNode.addEdge(newEdge);
                    deeperChildNode.addNeighbor(child);
                    child.addEdge(newEdge);
                    child.addNeighbor(deeperChildNode);

                }

                if (nodeList.size() >= requiredDepth) {
                    break;
                }
            }
        }

        if (nodeList.size() < requiredDepth) {
            extendGraph(childrenSites, requiredDepth);
        }
    }

    public void buildGraph(int requiredVert) throws Exception {
        ArrayList<Website> startSites = getStartSites();
        ArrayList<GraphNode> childrenSites = new ArrayList<>();
        for (Website w : startSites) {
            System.out.println("Master this: " + w.URL);

            GraphNode node = new GraphNode(w);
            nodeList.add(node);

            if (nodeList.size() >= requiredVert) {
                break;
            }

            for (Website child : w.connectedSites) {
                System.out.println("Looking at this: " + child.URL);
                if (!inNodeList(child.getNodeName())) {
                    child.getContents();
                    child.getConnectedSites();
                    child.setWords();
                    child.makePersistent();

                    System.out.println("Adding website: " + child.URL);
                    GraphNode childNode = new GraphNode(child);
                    childNode.parent = node;
                    nodeList.add(childNode);
                    childrenSites.add(childNode);

                    GraphEdge newEdge = new GraphEdge(node, childNode);
                    edgeList.add(newEdge);
                    node.addEdge(newEdge);
                    node.addNeighbor(childNode);

                    childNode.addNeighbor(node);

                }

                if (nodeList.size() >= requiredVert) {
                    break;
                }
            }
        }

        if (nodeList.size() < requiredVert) {
            extendGraph(childrenSites, requiredVert);
        }
    }

    public void drawGraph(ArrayList<GraphEdge> path) {
        for(GraphEdge ge: path){
            double weight = ge.source.nodeData.similarity - ge.sink.nodeData.similarity;
            System.out.println("(" + ge.source.nodeName + "==>" + ge.sink.nodeName + ")" + " weight: " + weight);
        }
    }

    public String drawGraph(Stack<GraphNode> path, GraphNode source){
        String output = "";

        if(path == null){
            return "There is no path between these nodes";
        } else {
            while(!path.isEmpty()){
                GraphNode next = path.pop();
                if(next != null){
                    output = output + next.nodeName + "=>";
                }
            }
        }

        return output;
    }

    public void updateWithSimilarities(){
        ArrayList<Website> allWebsites = collectAllNodes();
        ArrayList<String> allwords = Website.getAllWords(allWebsites);
        ArrayList<String> allheadings = Website.getAllHeadings(allWebsites);

        Website arbitraryFirst = nodeList.get(0).nodeData;

        for(GraphNode gn: nodeList) {
            gn.nodeData.similarity(arbitraryFirst, allWebsites, allwords, allheadings);
        }

    }

}
