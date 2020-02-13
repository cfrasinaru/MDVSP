///*
// * Copyright (C) 2019 Cristian Frasinaru
// */
//package ro.uaic.info.mdvsp.old;
//
//import ilog.concert.IloException;
//import ilog.concert.IloIntVar;
//import ilog.cplex.IloCplex;
//import ro.uaic.info.mdvsp.Model;
//
///**
// * Duplicate all depots, so each one contains 1 vehicle. FAIL.
// *
// * @author Cristian Frăsinaru
// */
//public class ModelDuplicateDepots extends Model {
//
//    int m1; //nb of duplicated depots
//    int cost1[][];
//
//    IloIntVar next[][];
//    IloIntVar prev[][];
//    IloCplex cp;
//
//    public ModelDuplicateDepots(String filename) {
//        super(filename);
//    }
//
//    /**
//     *
//     * @param name
//     * @param m depots
//     * @param n trips
//     */
//    public ModelDuplicateDepots(String name, int m, int n) {
//        super(name, m, n);
//    }
//
//    @Override
//    public void solve() throws IloException {
//
//        m1 = nbVehicles();
//
//        cost1 = new int[m1 + n][m1 + n];
//
//        //copy depot costs
//        int k = 0;
//        for (int i = 0; i < m; i++) {
//            for (int j = 0; j < nbVehicles[i]; j++) {
//                cost1[k][j] = cost[i - m1 + m][j - m1 + m];
//            }
//        }
//
//        //copy trip costs
//        cost1 = new int[m1 + n][m1 + n];
//        for (int i = m; i < m + n; i++) {
//            for (int j = m1; j < m1 + n; j++) {
//                cost1[i][j] = cost[i - m1 + m][j - m1 + m];
//            }
//        }
//
//        getTours();
//        printTours();
//    }
//
//    private int original(int depot) {
//        int i = 0;
//        while (depot >= nbVehicles[i]) {
//            i++;
//        }
//        return i;
//    }
//
//    @Override
//    protected void extractSolution() {
//    }
//
//}
