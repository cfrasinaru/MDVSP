/*
 * Copyright (C) 2019 Cristian Frasinaru
 */
package ro.uaic.info.mdvsp.greedy;

import java.io.IOException;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import ro.uaic.info.mdvsp.Model;
import ro.uaic.info.mdvsp.Solution;

/**
 * Least expensive available trip.
 *
 * @author Cristian Frăsinaru
 */
public class GreedyModel1 extends Model {

    private boolean visited[];
    private int vehicles[]; //number of used vehicles

    public GreedyModel1(String name) throws IOException {
        super(name);
    }

    @Override
    public void _solve() {
        Solution sol = new Solution(this);
        createGraph();

        visited = new boolean[n + m];
        vehicles = new int[m];

        /*
        var edges = new ArrayList<>(graph.edgeSet());
        List<DefaultWeightedEdge> sorted = edges.stream()
                .sorted((e0, e1) -> (int) (graph.getEdgeWeight(e0) - graph.getEdgeWeight(e1)))
                .collect(Collectors.toList());
         */
        int trips = 0;
        int i = -1, start = -1;
        while (trips < n) {
            DefaultWeightedEdge e = null;
            if (i < 0) {
                e = minDepot();
                i = graph.getEdgeSource(e);
                start = i;
                vehicles[start]++;
            } else {
                e = min(start, i);
            }
            int j = graph.getEdgeTarget(e);
            sol.set(i, j, 1);
            if (j >= m) {
                visited[j] = true;                
                trips++;
                i = j;
                if (trips == n) {
                    sol.set(j, start, 1);
                }
            } else {
                i = -1;
            }
        }

        solutions.add(sol);
    }

    /*
     * Returns the minimum cost edge (i-j), where j is not visited
     * and j is either a trip or the depot start.
     */
    private DefaultWeightedEdge min(int start, int i) {
        double minCost = Double.MAX_VALUE;
        DefaultWeightedEdge minEdge = null;
        for (var j : Graphs.successorListOf(graph, i)) {
            if (visited[j] || (j < m && j != start)) {
                continue;
            }
            var edge = graph.getEdge(i, j);
            double edgeCost = graph.getEdgeWeight(edge);

            if (minCost > edgeCost) {
                minCost = edgeCost;
                minEdge = edge;
            }
        }
        return minEdge;
    }

    /*
     *  Returns the minimum edge from a NOT FULL depot to a NOT VISITED trip    
     */
    private DefaultWeightedEdge minDepot() {
        double minCost = Double.MAX_VALUE;
        DefaultWeightedEdge minEdge = null;
        for (int i = 0; i < m; i++) {
            if (vehicles[i] == nbVehicles(i)) {
                continue;
            }
            var edge = min(i, i);
            double edgeCost = graph.getEdgeWeight(edge);
            if (minCost > edgeCost) {
                minCost = edgeCost;
                minEdge = edge;
            }
        }
        return minEdge;
    }

}
