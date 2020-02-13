///*
// * Copyright (C) 2019 Cristian Frasinaru
// */
//package ro.uaic.info.mdvsp.old;
//
//import ilog.concert.IloException;
//import ilog.concert.IloNumExpr;
//import ilog.concert.IloIntVar;
//import ilog.cplex.IloCplex;
//import java.io.FileNotFoundException;
//import java.io.PrintWriter;
//import java.util.Collections;
//import java.util.List;
//import java.util.stream.Collectors;
//import ro.uaic.info.mdvsp.Model;
//import ro.uaic.info.mdvsp.Tour;
//
///**
// * Save-Load bad tours.
// *
// * @author Cristian Frăsinaru
// */
//public class ModelBadTours extends Model {
//
//    IloIntVar x[][];
//    IloCplex cp;
//    private static int step = 0;
//    private List<Tour> oldBadTours;
//
//    public ModelBadTours(String filename) {
//        super(filename);
//    }
//
//    /**
//     *
//     * @param name
//     * @param m depots
//     * @param n trips
//     */
//    public ModelBadTours(String name, int m, int n) {
//        super(name, m, n);
//    }
//
//    private String repo() {
//        return "d:/java/MDVSP/input/" + name + "_bad.txt";
//    }
//
//    private void loadBadTours() throws IloException {
//        oldBadTours = Tour.load(repo());
//        System.out.println("Loaded " + oldBadTours.size() + " bad tours");
//        for (Tour t : oldBadTours) {
//            IloNumExpr path = cp.constant(0);
//            for (int k = 0; k < t.size() - 1; k++) {
//                int i = t.get(k);
//                int j = t.get(k + 1);
//                path = cp.sum(path, x[i][j]);
//            }
//            cp.addLe(path, t.size() - 2);
//        }
//    }
//
//    private void saveBadTours() {
//        if (oldBadTours == null) {
//            return;
//        }
//        List<Tour> newBadTours = getTours().stream().filter(t -> t.isBad()).collect(Collectors.toList());
//        System.out.println("Saving " + newBadTours.size() + " new bad tours");
//        oldBadTours.addAll(newBadTours);
//        Collections.sort(oldBadTours, Tour::compareByTrips);
//        try (PrintWriter pw = new PrintWriter(repo())) {
//            oldBadTours.forEach(t -> pw.println(t.toString(" ")));
//        } catch (FileNotFoundException e) {
//            System.err.println(e);
//        }
//    }
//
//    @Override
//    public void solve() throws IloException {
//        //createGraph();
//        this.cp = new IloCplex();
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
//        cp.addMinimize(expr);
//        loadBadTours();
//
//        //cp.use(new BadTourElimination());
//        if (cp.solve()) {
//            System.out.println("Solution found: " + cp.getObjValue());
//            extractSolution();
//            getTours();
//            saveBadTours();
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
//
//    /*
//    private class BadTourElimination extends IloCplex.LazyConstraintCallback {
//
//        @Override
//        protected void main() throws IloException {
//            step++;
//            if (step >= 10) {
//                return;
//            }
//            System.out.println("LazyContraints, step=" + step);
//            double val[][] = new double[n + m][n + m];
//            for (int i = 0; i < m + n; i++) {
//                for (int j = 0; j < m + n; j++) {
//                    val[i][j] = this.getValue(x[i][j]);
//                }
//            }
//            getTours(val);
//
//            for (Tour tour : tours) {
//                int d0 = tour.get(0);
//                int d1 = tour.get(tour.size() - 1);
//                if (d0 < m && d1 < m && d0 != d1) {
//                    //bad tour
//                    IloNumExpr path = cp.constant(0);
//                    for (int k = 0; k < tour.size() - 1; k++) {
//                        int i = tour.get(k);
//                        int j = tour.get(k + 1);
//                        path = cp.sum(path, x[i][j]);
//                    }
//                    cp.addLe(path, tour.size() - 2);
//                }
//            };
//        }
//    }
//     */
//}
