///*
// * Copyright (C) 2019 Cristian Frasinaru
// */
//package ro.uaic.info.mdvsp.old;
//
//import ilog.concert.IloException;
//import java.io.FileNotFoundException;
//import org.jgrapht.Graphs;
//import org.jgrapht.graph.DefaultWeightedEdge;
//import ro.uaic.info.mdvsp.Model;
//
///**
// * Least expensive trip.
// *
// * @author Cristian Frăsinaru
// */
//public class ModelGreedy1 extends Model {
//
//    private boolean visited[];
//
//    public ModelGreedy1(String name) {
//        super(name);
//    }
//
//    @Override
//    public void solve() throws IloException, FileNotFoundException {
//        createGraph();
//
//        visited = new boolean[n + m];
//        /*
//        var edges = new ArrayList<>(graph.edgeSet());
//        List<DefaultWeightedEdge> sorted = edges.stream()
//                .sorted((e0, e1) -> (int) (graph.getEdgeWeight(e0) - graph.getEdgeWeight(e1)))
//                .collect(Collectors.toList());
//         */
//        int trips = 0;
//        int i = -1, start = -1;
//        while (trips < n) {
//            DefaultWeightedEdge e = null;
//            if (i < 0) {
//                e = minDepot();
//                i = graph.getEdgeSource(e);
//                start = i;
//            } else {
//                e = min(start, i);
//            }
//            int j = graph.getEdgeTarget(e);
//            sol[i][j] = 1;
//            if (j >= m) {
//                visited[j] = true;
//                trips++;
//                i = j;
//                if (trips == n) {
//                    sol[j][start] = 1;
//                }
//            } else {
//                i = -1;
//            }
//        }
//
//        getTours();
//        printTours();
//
//    }
//
//    private DefaultWeightedEdge min(int start, int i) {
//        double minCost = Double.MAX_VALUE;
//        DefaultWeightedEdge minEdge = null;
//        for (var j : Graphs.successorListOf(graph, i)) {
//            if (visited[j] || (j < m && j != start)) {
//                continue;
//            }
//            var edge = graph.getEdge(i, j);
//            double edgeCost = graph.getEdgeWeight(edge);
//
//            if (minCost > edgeCost) {
//                minCost = edgeCost;
//                minEdge = edge;
//            }
//        }
//        return minEdge;
//    }
//
//    private DefaultWeightedEdge minDepot() {
//        double minCost = Double.MAX_VALUE;
//        DefaultWeightedEdge minEdge = null;
//        for (int i = 0; i < m; i++) {
//            var edge = min(i, i);
//            double edgeCost = graph.getEdgeWeight(edge);
//            if (minCost > edgeCost) {
//                minCost = edgeCost;
//                minEdge = edge;
//            }
//        }
//        return minEdge;
//    }
//
//    @Override
//    protected void extractSolution() {
//        //the solution is already built
//    }
//
//}
