/*
 * Copyright (C) 2019 Cristian Frasinaru
 */
package ro.uaic.info.mdvsp.greedy;

import java.io.IOException;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import ro.uaic.info.mdvsp.Model;
import ro.uaic.info.mdvsp.Solution;

/**
 * DAG longest path.
 *
 * @author Cristian Frăsinaru
 */
public class GreedyModel2 extends Model {

    LongestPath lp;
    DirectedAcyclicGraph<Integer, DefaultEdge> dag;
    private boolean visited[];

    private class Path {

        int from;
        int to;

    }

    public GreedyModel2(String name) throws IOException {
        super(name);
    }

    @Override
    public void _solve() {
        Solution sol = new Solution(this);
        //createGraph();

        dag = createReducedGraph(); //the graph without depots is a dag
        
        //ConnectivityInspector ci = new ConnectivityInspector(dag);
        //System.out.println(ci.isConnected());

        lp = new LongestPath(this, dag);
        lp.computeAll();

        /*
        GraphMeasurer gm = new GraphMeasurer(g);        
        Map<Integer, Double> map = gm.getVertexEccentricityMap();        
        for(int i=m; i<n+m; i++) {
            System.out.println(i + ": " + map.get(i));
        }*/
        /*
        System.out.print("Running Floyd-Warshall algorithm...");
        FloydWarshallShortestPaths alg = new FloydWarshallShortestPaths(graph);
        System.out.println(alg.getShortestPathsCount());
        System.out.println("Done.");
        for (int i = m; i < m + n; i++) {
            System.out.println(alg.getPath(0, i) + " = " + alg.getPathWeight(0, i));
            System.out.println("\t" + alg.getPath(i, 0) + " = " + alg.getPathWeight(i, 0));
        }*/
        int trips = 0;
        visited = new boolean[n + m];
        while (trips < n) {
            Path path = bestPath();
            int i = path.to;
            while (true) {
                visited[i] = true;
                dag.removeVertex(i);
                trips++;

                int j = lp.prev[path.from][i];
                if (j < 0) {
                    break;
                }
                sol.set(j, i, 1);
                i = j;
            }
            int depot = bestDepot(path);
            sol.set(depot, path.from, 1);
            sol.set(path.to, depot, 1);            
        }
        
        solutions.add(sol);
    }

    private Path bestPath() {
        lp.computeAll();
        double maxValue = -1;
        Path bestPath = new Path();
        for (int src : dag) {
            int v = lp.max[src];
            if (v < 0) {
                continue;
            }
            double value = lp.dist[src][v];
            /*
            if (dag.edgeSet().isEmpty()) {
                System.out.println(src + "\t" + lp.max[src] + "\t" + value);
            }*/
            if (maxValue < value) {
                maxValue = value;
                bestPath.from = src;
                bestPath.to = v;
            }
        }
        /*
        if (bestPath.from == 0) {
            System.out.println(dag);
            System.exit(0);
        }
        System.out.println("best path " + bestPath.from + " --> " + bestPath.to + ", dist=" + lp.dist[bestPath.from][bestPath.to]);
        */
        return bestPath;
    }

    private int bestDepot(Path path) {
        double minValue = Double.MAX_VALUE;
        int bestDepot = -1;
        for (int depot = 0; depot < m; depot++) {
            double value = cost[depot][path.from] + cost[path.to][depot];
            if (minValue > value) {
                value = minValue;
                bestDepot = depot;
            }
        }
        return bestDepot;
    }

    private int bestTarget(int src) {
        double maxValue = Double.MIN_VALUE;
        int maxNode = -1;
        for (int v = m; v < m + n; v++) {
            if (visited[v]) {
                continue;
            }
            if (maxValue < lp.dist[src][v]) {
                maxValue = lp.dist[src][v];
                //+ 1 / lp.cost[src][v]
                maxNode = v;
            }
        }
        return maxNode;
    }

}
