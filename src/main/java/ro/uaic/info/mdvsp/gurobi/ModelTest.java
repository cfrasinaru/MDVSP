/*
 * Copyright (C) 2020 Cristian Frasinaru
 */
package ro.uaic.info.mdvsp.gurobi;

import gurobi.GRB;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import java.io.IOException;
import ro.uaic.info.mdvsp.Model;

/**
 *
 * @author Cristian Frăsinaru
 */
public class ModelTest extends ModelRelaxed {

    private int y[];

    public ModelTest(String filename) throws IOException {
        super(filename);
    }

    public ModelTest(Model other) {
        super(other);
    }
    
    private void init() {
        y = new int[n]; //y[i] = cel mai apropiat depot de i

        for (int i = 0; i < n; i++) {
            int minCost = Integer.MAX_VALUE;
            int minDepot = -1;
            for (int j = 0; j < m; j++) {
                if (cost[i + m][j] > 0 && cost[i + m][j] < minCost) {
                    minDepot = j;
                    minCost = cost[i + m][j];
                }
            }
            y[i] = minDepot;
        }
        /*
        createGraph();
        ShortestPathAlgorithm alg = new BellmanFordShortestPath(graph);
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < m; j++) {
                GraphPath<Integer, DefaultWeightedEdge> path = alg.getPath(i, j);                
                //System.out.println("sp(" + i + "," + j + ")=" + path.getWeight() + "\t" + path );
            }
        }*/
 /*
        ShortestPathAlgorithm fw = new FloydWarshallShortestPaths(graph);
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < m; j++) {
                System.out.print(i + "->" + j + " ");
                GraphPath<Integer, DefaultWeightedEdge> path = fw.getPath(i, j);
                //System.out.println("sp(" + i + "," + j + ")=" + path.getWeight() + "\t" + path );
            }
        }*/
    }

    @Override
    protected void createObjective() throws GRBException {
        init();
        double factor = 1_000_000;
        GRBLinExpr expr = new GRBLinExpr();
        for (int i = 0; i < n + m; i++) {
            for (int j = 0; j < n + m; j++) {
                if (x[i][j] != null) {
                    if (i >= m && j < m) {
                        expr.addTerm(cost[i][j] + (cost[i][j] - cost[i][y[i - m]]) * factor, x[i][j]);
                    } else {
                        expr.addTerm(cost[i][j], x[i][j]);
                    }
                }
            }
        }
        model.setObjective(expr, GRB.MINIMIZE);
    }

}
