///*
// * Copyright (C) 2019 Cristian Frasinaru
// */
//package ro.uaic.info.mdvsp.old;
//
//import ilog.concert.IloException;
//import java.io.FileNotFoundException;
//import localsolver.LSExpression;
//import localsolver.LSModel;
//import localsolver.LocalSolver;
//import ro.uaic.info.mdvsp.Model;
//
///**
// *
// * @author Cristian Frăsinaru
// */
//public class ModelLocalSolver2 extends Model {
//
//    private LocalSolver localsolver;
//
//    LSExpression[][] x;
//    LSExpression[] d;
//
//    public ModelLocalSolver2(String filename) {
//        super(filename);
//    }
//
//    public ModelLocalSolver2(String name, int m, int n) {
//        super(name, m, n);
//    }
//
//    @Override
//    public void solve() throws IloException, FileNotFoundException {
//        localsolver = new LocalSolver();
//        // Declares the optimization model. 
//        LSModel model = localsolver.getModel();
//        this.x = new LSExpression[n + m][n + m];
//        this.d = new LSExpression[n];
//
//        //create variables
//        for (int i = 0; i < n + m; i++) {
//            for (int j = 0; j < n + m; j++) {
//                if (cost[i][j] == -1) {
//                    x[i][j] = model.boolVar();
//                    model.constraint(model.eq(x[i][j], 0));
//                } else {
//                    x[i][j] = model.boolVar();
//                }
//            }
//        }
//
//        //limit the number of vehicles that can be used from each depot
//        for (int k = 0; k < m; k++) {
//            LSExpression sum = model.sum();
//            for (int j = m; j < n + m; j++) {
//                sum.addOperand(x[k][j]);
//            }
//            model.constraint(model.leq(sum, nbVehicles[k]));
//        }
//
//        //flow conservation constraints
//        for (int i = 0; i < n + m; i++) {
//            LSExpression sum1 = model.sum();
//            LSExpression sum2 = model.sum();
//            for (int j = 0; j < n + m; j++) {
//                sum1.addOperand(x[i][j]);
//                sum2.addOperand(x[j][i]);
//            }
//            if (i < m) {
//                model.constraint(model.eq(sum1, sum2));
//            } else {
//                model.constraint(model.eq(sum1, 1));
//                model.constraint(model.eq(sum2, 1));
//            }
//        }
//
//        //objective function
//        LSExpression expr = model.sum();
//        for (int i = 0; i < n + m; i++) {
//            for (int j = 0; j < n + m; j++) {
//                expr.addOperand(model.prod(x[i][j], cost[i][j]));
//            }
//        }
//
//        model.minimize(expr);
//        model.close();
//
//        localsolver.getParam().setTimeLimit(60);
//        localsolver.solve();
//
//        System.out.println("Solution found: " + expr.getIntValue());
//        extractSolution();
//        //printSolution();
//        //printUsedVehicles();
//        //getTours();
//        //printTours();
//
//    }
//
//    @Override
//    protected void extractSolution() {
//        for (int i = 0; i < n + m; i++) {
//            for (int j = 0; j < n + m; j++) {
//                sol[i][j] = x[i][j].getIntValue() != 0 ? 1 : 0;
//            }
//        }
//    }
//
//}
