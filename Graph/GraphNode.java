package com.company;

import java.util.ArrayList;
import java.util.Comparator;

public class GraphNode {
    Website nodeData;
    ArrayList<GraphEdge> nodeEdges;
    String nodeName;
    GraphNode parent;
    ArrayList<GraphNode> neighbors;
    boolean visited;

    public GraphNode(Website data){
        this.nodeData = data;
        nodeEdges = new ArrayList<>();
        neighbors = new ArrayList<>();
        nodeName = data.getNodeName();
        parent = null;
        visited = false;
    }

    public void addEdge(GraphEdge ge){
        this.nodeEdges.add(ge);
    }

    public static Comparator<GraphNode> weightCompare = new Comparator<GraphNode>() {
        @Override
        public int compare(GraphNode o1, GraphNode o2) {
           return (int) (o1.nodeData.similarity - o2.nodeData.similarity);
        }
    };

    public void addNeighbor(GraphNode ge){
        neighbors.add(ge);
    }

}
