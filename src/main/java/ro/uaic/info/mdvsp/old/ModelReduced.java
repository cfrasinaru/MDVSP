///*
// * Copyright (C) 2019 Cristian Frasinaru
// */
//package ro.uaic.info.mdvsp.old;
//
//import ilog.concert.IloException;
//import ilog.concert.IloNumExpr;
//import ilog.concert.IloIntVar;
//import ilog.cplex.IloCplex;
//import ro.uaic.info.mdvsp.Model;
//
///**
// * Withour loop constraints.
// *
// * @author Cristian Frăsinaru
// */
//public class ModelReduced extends Model {
//
//    private IloIntVar x[][];
//    private IloCplex cp;
//    double opt = 1289114.0;
//    double red = 1287475.0;
//
//    public ModelReduced(String filename) {
//        super(filename);
//    }
//
//    /**
//     *
//     * @param name
//     * @param m depots
//     * @param n trips
//     */
//    public ModelReduced(String name, int m, int n) {
//        super(name, m, n);
//    }
//
//    @Override
//    public void solve() throws IloException {
//
//        this.cp = new IloCplex();
//        if (populateLimit > 1) {
//            cp.setParam(IloCplex.DoubleParam.EpGap, 0.01);
//            cp.setParam(IloCplex.DoubleParam.SolnPoolAGap, 0);
//            cp.setParam(IloCplex.IntParam.SolnPoolIntensity, 4);
//            cp.setParam(IloCplex.IntParam.SolnPoolReplace, 2);
//            cp.setParam(IloCplex.IntParam.PopulateLim, populateLimit);
//        }
//        cp.setParam(IloCplex.DoubleParam.TiLim, 300); //5 min
//
//        this.x = new IloIntVar[n + m][n + m];
//
//        for (int i = 0; i < n + m; i++) {
//            for (int j = 0; j < n + m; j++) {
//                if (cost[i][j] == -1) {
//                    x[i][j] = cp.intVar(0, 0);
//                } else {
//                    x[i][j] = cp.intVar(0, 1);
//                }
//            }
//        }
//
//        for (int i = 0; i < n + m; i++) {
//            IloNumExpr rowSum = cp.sum(x[i]);
//            IloNumExpr colSum = cp.constant(0);
//            for (int j = 0; j < n + m; j++) {
//                colSum = cp.sum(colSum, x[j][i]);
//            }
//            if (i >= m) {
//                cp.addEq(rowSum, 1); //rowSum
//                cp.addEq(colSum, 1);
//            } else {
//                cp.addEq(colSum, rowSum);
//                cp.addLe(rowSum, nbVehicles[i]);
//            }
//        }
//
//        IloNumExpr expr = cp.constant(0);
//        for (int i = 0; i < n + m; i++) {
//            for (int j = 0; j < n + m; j++) {
//                expr = cp.sum(expr, cp.prod(x[i][j], cost[i][j]));
//            }
//        }
//        //1299055.0
//        //cp.addLe(expr, red * 1.001);
//
//        cp.addMinimize(expr);
//
//        boolean solved;
//        if (populateLimit == 1) {
//            solved = cp.solve();
//            System.out.println("Solution found: " + cp.getObjValue());
//            getSolution();
//            getTours();
//            //printTours();
//            //printSolution();
//        } else {
//            solved = cp.populate();
//            nbSolutions = cp.getSolnPoolNsolns();
//            System.out.println("Number of solution found: " + nbSolutions);
//            getSolutions();
//            repair();
//        }
//
//        //if (solved) {
//        //    getTours();
//        //    System.out.println(tours.size());
//        //}
//        cp.end();
//    }
//
//    private void repair() {
//        int[][] bestSol = null;
//        double bestVal = Double.MAX_VALUE;
//        for (int k = 0; k < nbSolutions; k++) {
//            int s[][] = solutions.get(k);
//            double rep = repair(s);
//            if (rep < bestVal) {
//                bestSol = s;
//                bestVal = rep;
//            }
//        }
//        System.out.println(this.totalCost(bestSol) + " -> " + bestVal + " = " + ((bestVal - opt) / opt));
//
//        int maxCount = -1;
//        for (int k = 0; k < nbSolutions; k++) {
//            int count = 0;
//            int s[][] = solutions.get(k);
//            for (int i = 0; i < n + m; i++) {
//                for (int j = 0; j < n + m; j++) {
//                    if (s[i][j] != bestSol[i][j]) {
//                        count++;
//                    }
//                }
//            }
//            System.out.println("\tcount=" + count);
//            if (count > maxCount) {
//                maxCount = count;
//            }
//        }
//        System.out.println("max differences " + maxCount);
//    }
//
//    @Override
//    protected void getSolution() {
//        try {
//            for (int i = 0; i < n + m; i++) {
//                for (int j = 0; j < n + m; j++) {
//                    sol[i][j] = cp.getValue(x[i][j]) < tolerance ? 0 : 1;
//                }
//            }
//            solutions.add(sol);
//        } catch (IloException ex) {
//            System.err.println(ex);
//        }
//    }
//
//    @Override
//    protected void getSolutions() {
//        try {
//            for (int k = 0; k < nbSolutions; k++) {
//                int s[][] = new int[n + m][n + m];
//                for (int i = 0; i < n + m; i++) {
//                    for (int j = 0; j < n + m; j++) {
//                        s[i][j] = cp.getValue(x[i][j], k) < tolerance ? 0 : 1;
//                    }
//                }
//                solutions.add(s);
//            }
//        } catch (IloException ex) {
//            System.err.println(ex);
//        }
//    }
//
//}
