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
//public class ModelLocalSolver1 extends Model {
//
//    private LocalSolver localsolver;
//
//    LSExpression[][][] x;
//
//    public ModelLocalSolver1(String filename) {
//        super(filename);
//    }
//
//    public ModelLocalSolver1(String name, int m, int n) {
//        super(name, m, n);
//    }
//
//    @Override
//    public void solve() throws IloException, FileNotFoundException {
//        localsolver = new LocalSolver();
//        // Declares the optimization model. 
//        LSModel model = localsolver.getModel();
//        this.x = new LSExpression[m][n + m][n + m];
//
//        //create variables
//        for (int k = 0; k < m; k++) {
//            for (int i = 0; i < n + m; i++) {
//                for (int j = 0; j < n + m; j++) {
//                    if (cost[i][j] == -1 || (i < m && i != k) || (j < m && j != k)) {
//                        x[k][i][j] = model.boolVar(); //0
//                        model.constraint(model.eq(x[k][i][j], 0));
//                    } else {
//                        x[k][i][j] = model.boolVar();
//                    }
//                }
//            }
//        }
//
//        //constraints
//        //(2) each task is executed exactly once by a vehicle
//        for (int i = m; i < n + m; i++) {
//            LSExpression sum = model.sum();
//            for (int j = 0; j < n + m; j++) {
//                for (int k = 0; k < m; k++) {
//                    sum.addOperand(x[k][i][j]);
//                }
//            }
//            model.constraint(model.eq(sum, 1));
//        }
//
//        //(3) limit the number of vehicles that can be used from each depot
//        for (int k = 0; k < m; k++) {
//            LSExpression sum = model.sum();
//            for (int j = m; j < n + m; j++) {
//                sum.addOperand(x[k][k][j]);
//            }
//            model.constraint(model.leq(sum, nbVehicles[k]));
//        }
//
//        //(4) flow conservation constraints which define a multiple-path structure for each depot
//        for (int i = 0; i < n + m; i++) {
//            for (int k = 0; k < m; k++) {
//                LSExpression sum1 = model.sum();
//                LSExpression sum2 = model.sum();
//                for (int j = 0; j < n + m; j++) {
//                    sum1.addOperand(x[k][i][j]);
//                    sum2.addOperand(x[k][j][i]);
//                }
//                model.constraint(model.eq(sum1, sum2));
//            }
//        }
//
//        //objective function
//        LSExpression expr = model.sum();
//        for (int i = 0; i < n + m; i++) {
//            for (int j = 0; j < n + m; j++) {
//                for (int k = 0; k < m; k++) {
//                    expr.addOperand(model.prod(x[k][i][j], cost[i][j]));                    
//                }
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
//        printSolution();
//        printUsedVehicles();
//        getTours();
//        printTours();
//
//    }
//
//    @Override
//    protected void extractSolution() {
//        for (int k = 0; k < m; k++) {
//            for (int i = 0; i < n + m; i++) {
//                for (int j = 0; j < n + m; j++) {
//                    sol[i][j] = x[k][i][j].getIntValue() != 0 ? 1 : 0;
//                }
//            }
//        }
//    }
//
//}
