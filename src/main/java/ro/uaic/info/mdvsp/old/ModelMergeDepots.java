///*
// * Copyright (C) 2019 Cristian Frasinaru
// */
//package ro.uaic.info.mdvsp.old;
//
//import ilog.concert.IloException;
//import ilog.concert.IloIntVar;
//import ilog.cplex.IloCplex;
//import java.util.List;
//import org.jgrapht.graph.DefaultEdge;
//import org.jgrapht.graph.DirectedAcyclicGraph;
//import ro.uaic.info.mdvsp.Model;
//import ro.uaic.info.mdvsp.Tour;
//
///**
// * Replace all depots with a single one. Solve. Repair.
// *
// * @author Cristian Frăsinaru
// */
//public class ModelMergeDepots extends Model {
//
//    IloIntVar x[][];
//    IloCplex cp;
//    DirectedAcyclicGraph<Integer, DefaultEdge> dag;
//    private int count[];
//
//    public ModelMergeDepots(String filename) {
//        super(filename);
//    }
//
//    /**
//     *
//     * @param name
//     * @param m depots
//     * @param n trips
//     */
//    public ModelMergeDepots(String name, int m, int n) {
//        super(name, m, n);
//    }
//
//    @Override
//    public void solve() throws IloException {
//        dag = createReducedGraph();
//        ModelReduced red = new ModelReduced(name, 1, n);
//        red.nbVehicles[0] = this.nbVehicles(); //total
//        for (int i = 1; i < n + 1; i++) {
//            red.cost[0][i] = 5000;
//            red.cost[i][0] = 5000;
//            for (int j = 1; j < n + 1; j++) {
//                red.cost[i][j] = this.cost[i + m - 1][j + m - 1];
//            }
//        }
//
//        red.solve();
//
//        for (int i = 1; i < n + 1; i++) {
//            for (int j = 1; j < n + 1; j++) {
//                this.sol[i + m - 1][j + m - 1] = red.sol[i][j];
//            }
//        }
//
//        count = new int[m];
//        List<Tour> redTours = red.getTours();
//        for (Tour t : redTours) {
//            int i = t.second();
//            int j = t.lastButOne();
//            int depot = bestDepot(i, j);
//            this.sol[depot][i + m - 1] = 1;
//            this.sol[j + m - 1][depot] = 1;
//            count[depot]++;
//        }
//        getTours();
//        printTours();
//    }
//
//    @Override
//    protected void getSolution() {
//    }
//
//    private int bestDepot(int from, int to) {
//        double minValue = Double.MAX_VALUE;
//        int bestDepot = -1;
//        for (int depot = 0; depot < m; depot++) {
//            if (count[depot] >= nbVehicles(depot)) {
//                continue;
//            }
//            double value = cost[depot][from] + cost[to][depot];
//            if (minValue > value) {
//                value = minValue;
//                bestDepot = depot;
//            }
//        }
//        return bestDepot;
//    }
//
//}
