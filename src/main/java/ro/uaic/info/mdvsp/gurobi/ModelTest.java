/*
 * Copyright (C) 2020 Cristian Frasinaru
 */
package ro.uaic.info.mdvsp.gurobi;

import java.io.IOException;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.BellmanFordShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;

/**
 *
 * @author Cristian Frăsinaru
 */
public class ModelTest extends ModelRelaxed {

    public ModelTest(String filename) throws IOException {
        super(filename);
        init();
    }

    private void init() {
        createGraph();
        ShortestPathAlgorithm alg = new BellmanFordShortestPath(graph);
        for (int i = 0; i < m; i++) {
            for (int j = m; j < m + n; j++) {
                GraphPath<Integer, DefaultWeightedEdge> path = alg.getPath(i, j);                
                System.out.println("sp(" + i + "," + j + ")=" + path.getWeight() + "\t" + path );
            }
        }
    }


}
