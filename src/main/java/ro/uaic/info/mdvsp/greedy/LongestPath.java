package ro.uaic.info.mdvsp.greedy;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import ro.uaic.info.mdvsp.Model;

/**
 *
 * @author Cristian Frăsinaru
 */
public class LongestPath {

    private final Model model;
    private final int n;
    private final int m;
    private final DirectedAcyclicGraph<Integer, DefaultEdge> dag;

    int[][] dist;
    double cost[][];
    int[][] prev;
    int[] max;

    public LongestPath(Model model, DirectedAcyclicGraph<Integer, DefaultEdge> dag) {
        this.model = model;
        this.n = model.nbTrips();
        this.m = model.nbDepots();
        this.dag = dag;

        dist = new int[n + m][n + m];
        cost = new double[n + m][n + m];
        prev = new int[n + m][n + m];
        max = new int[n + m];

    }

    public void computeAll() {
        for (int src : dag) {
            compute(src);
        }
    }

    public void compute(int src) {
        for (int i = m; i < m + n; i++) {
            dist[src][i] = Integer.MIN_VALUE;
            cost[src][i] = Double.MAX_VALUE;
            prev[src][i] = -1;
        }
        dist[src][src] = 0;
        cost[src][src] = 0;
        max[src] = src;
        for (int u : dag) {            
            for (int v : Graphs.successorListOf(dag, u)) {
                if (dist[src][v] < dist[src][u] + 1) {
                    prev[src][v] = u;
                    dist[src][v] = dist[src][u] + 1;
                    cost[src][v] = cost[src][u] + model.getCost()[u][v];
                    max[src] = v;
                }
            }
        }
    }
}
