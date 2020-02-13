///*
// * Copyright (C) 2019 Cristian Frasinaru
// */
//package ro.uaic.info.mdvsp.old;
//
//import ilog.concert.IloException;
//import ilog.concert.IloIntExpr;
//import ilog.concert.IloIntVar;
//import ilog.concert.IloNumExpr;
//import ilog.concert.IloNumVar;
//import ilog.cplex.IloCplex;
//import ro.uaic.info.mdvsp.Model;
//
///**
// *
// * @author Cristian Frăsinaru
// */
//public class ModelTest extends Model {
//
//    IloNumVar x[][];
//    IloNumVar[][] d;
//    IloCplex cp;
//
//    public ModelTest(String filename) {
//        super(filename);
//    }
//
//    /**
//     *
//     * @param name
//     * @param m depots
//     * @param n trips
//     */
//    public ModelTest(String name, int m, int n) {
//        super(name, m, n);
//    }
//
//    @Override
//    public void solve() throws IloException {
//        //createGraph();
//        this.cp = new IloCplex();
//        this.x = new IloIntVar[n + m][n + m];
//        this.d = new IloIntVar[n + m][m];
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
//            for (int k = 0; k < m; k++) {
//                if (i < m) {
//                    if (i == k) {
//                        d[i][i] = cp.intVar(1, 1);
//                    } else {
//                        d[i][i] = cp.intVar(0, 0);
//                    }
//                }
//                d[i][k] = cp.intVar(0, 1);
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
//        //each trip is served by exactly obe depot
//        for (int i = m; i < n + m; i++) {
//            cp.addEq(cp.sum(d[i]), 1);
//        }
//
//        for (int i = 0; i < n + m; i++) {
//            for (int j = 0; j < n + m; j++) {
//                for (int k = 0; k < m; k++) {
//                    IloNumExpr sum = cp.constant(0);
//                    sum = cp.sum(sum, x[i][j]);
//                    sum = cp.sum(sum, d[i][k]);
//                    sum = cp.sum(sum, d[j][k]);
//                    //cp.add(cp.not(cp.eq(sum, 2))); //???
//                    
//                    //cp.add(cp.ifThen(cp.eq(x[i][j], 1), cp.eq(d[i], d[j])));
//                    //cp.addGe(x[i][j], cp.sum(d[i][j], 1));
//                    //xij=1 => forall k dik = djk
//                }
//            }
//        }
//
//        IloNumExpr expr = cp.constant(0);
//        for (int i = 0; i < n + m; i++) {
//            for (int j = 0; j < n + m; j++) {
//                expr = cp.sum(expr, cp.prod(x[i][j], cost[i][j]));
//            }
//        }
//        cp.addMinimize(expr);
//
//        if (cp.solve()) {
//            System.out.println("Solution found: " + cp.getObjValue());
//            extractSolution();
//            getTours();
//        }
//        cp.end();
//    }
//
//    @Override
//    protected void extractSolution() {
//        try {
//            for (int i = 0; i < n + m; i++) {
//                for (int j = 0; j < n + m; j++) {
//                    sol[i][j] = cp.getValue(x[i][j]) < tolerance ? 0 : 1;
//                }
//            }
//        } catch (IloException ex) {
//            System.err.println(ex);
//        }
//    }
//}
