package com.company;

import java.io.Serializable;

public class GraphEdge implements Serializable {
    GraphNode source;
    GraphNode sink;
    double weight;

    public GraphEdge(GraphNode src, GraphNode sk){
        this.source = src;
        this.sink = sk;
        weight = src.nodeData.similarity - sk.nodeData.similarity;
    }

}
