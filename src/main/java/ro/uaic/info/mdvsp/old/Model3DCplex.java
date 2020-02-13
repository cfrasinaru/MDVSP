///*
// * Copyright (C) 2019 Cristian Frasinaru
// */
//package ro.uaic.info.mdvsp.old;
//
//import ro.uaic.info.mdvsp.*;
//import ilog.concert.IloException;
//import ilog.concert.IloIntVar;
//import ilog.concert.IloNumExpr;
//import ilog.concert.IloNumVar;
//import ilog.cplex.IloCplex;
//import java.io.FileNotFoundException;
//
///**
// * 3D.
// *
// * @author Cristian Frăsinaru
// */
//public class Model3DCplex extends Model {
//
//    IloNumVar x[][][];
//    IloCplex cp;
//
//    public Model3DCplex(String filename) {
//        super(filename);
//    }
//
//    public Model3DCplex(String name, int m, int n) {
//        super(name, m, n);
//    }
//
//    @Override
//    public void solve() throws IloException, FileNotFoundException {
//        //createGraph();
//        this.cp = new IloCplex();
//        //this.cp.setParam(IloCplex.DoubleParam.EpInt, 0);
//        this.cp.setParam(IloCplex.DoubleParam.EpGap, 0);
//        this.cp.setParam(IloCplex.DoubleParam.EpInt, 0);
//        //this.cp.setParam(IloCplex.IntParam.MIPEmphasis, 0);
//        //this.cp.setParam(IloCplex.DoubleParam.EpRHS, 1e-9);
//        //this.cp.setParam(IloCplex.DoubleParam.ObjULim, 1241697);
//        //cp.setOut(null);
//        this.x = new IloIntVar[m][n + m][n + m];
//
//        //create variables
//        for (int k = 0; k < m; k++) {
//            for (int i = 0; i < n + m; i++) {
//                for (int j = 0; j < n + m; j++) {
//                    if (cost[i][j] == -1 || (i < m && i != k) || (j < m && j != k)) {
//                        x[k][i][j] = cp.intVar(0, 0);
//                    } else {
//                        x[k][i][j] = cp.intVar(0, 1);
//                    }
//                }
//            }
//        }
//
//        //constraints
//        //(2) each task is executed exactly once by a vehicle
//        for (int i = m; i < n + m; i++) {
//            IloNumExpr sum = cp.constant(0);
//            for (int j = 0; j < n + m; j++) {
//                for (int k = 0; k < m; k++) {
//                    sum = cp.sum(sum, x[k][i][j]);
//                }
//            }
//            cp.addEq(sum, 1);
//        }
//
//        //(3) limit the number of vehicles that can be used from each depot
//        for (int k = 0; k < m; k++) {
//            IloNumExpr sum = cp.constant(0);
//            for (int j = m; j < n + m; j++) {
//                sum = cp.sum(sum, x[k][k][j]);
//            }
//            //cp.addLe(sum, nbVehicles[k]);
//            cp.addLe(sum, nbVehicles[k]);
//        }
//
//        //(4) flow conservation constraints which define a multiple-path structure for each depot
//        for (int i = 0; i < n + m; i++) {
//            for (int k = 0; k < m; k++) {
//                IloNumExpr sum1 = cp.constant(0);
//                IloNumExpr sum2 = cp.constant(0);
//                for (int j = 0; j < n + m; j++) {
//                    sum1 = cp.sum(sum1, x[k][i][j]);
//                    sum2 = cp.sum(sum2, x[k][j][i]);
//                }
//                cp.addEq(sum1, sum2);
//            }
//        }
//
//        //objective function
//        IloNumExpr expr = cp.constant(0);
//        for (int i = 0; i < n + m; i++) {
//            for (int j = 0; j < n + m; j++) {
//                for (int k = 0; k < m; k++) {
//                    expr = cp.sum(expr, cp.prod(x[k][i][j], cost[i][j]));
//                }
//            }
//        }
//
//        cp.addMinimize(expr);
//
//        if (cp.solve()) {
//            System.out.println("Solution found: " + cp.getObjValue());
//            extractSolution();
//            getTours();
//        }
//
//        cp.end();
//    }
//
//    @Override
//    protected void extractSolution() {
//        try {
//            for (int k = 0; k < m; k++) {
//                for (int i = 0; i < n + m; i++) {
//                    for (int j = 0; j < n + m; j++) {
//                        if (cp.getValue(x[k][i][j]) > tolerance) {
//                            sol[i][j] = 1;
//                        }
//                    }
//                }
//            }
//        } catch (IloException ex) {
//            System.err.println(ex);
//        }
//    }
//
//}
