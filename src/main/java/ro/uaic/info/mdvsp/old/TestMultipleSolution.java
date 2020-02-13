//package ro.uaic.info.mdvsp.old;
//
//import ilog.concert.IloException;
//import ilog.concert.IloIntVar;
//import ilog.concert.IloNumExpr;
//import ilog.cplex.IloCplex;
//
///**
// *
// * @author Cristian Frăsinaru
// */
//public class TestMultipleSolution {
//
//    public static void main(String args[]) throws IloException {
//        IloCplex cp = new IloCplex();
//        cp.setParam(IloCplex.DoubleParam.SolnPoolAGap, 0);
//        cp.setParam(IloCplex.IntParam.SolnPoolIntensity, 4);
//        cp.setParam(IloCplex.IntParam.SolnPoolReplace, 2);
//        cp.setParam(IloCplex.IntParam.PopulateLim, 1000);
//
//        int n = 10;
//        IloIntVar x[] = new IloIntVar[n];
//        for (int i = 0; i < x.length; i++) {
//            x[i] = cp.intVar(0, 1);
//        }
//        IloNumExpr rowSum = cp.sum(x);
//        cp.addEq(rowSum, 3);
//
//        
//        //IloNumExpr expr = cp.sum(x);
//        //cp.addMinimize(expr);
//                
//        cp.populate();
//
//        int nsol = cp.getSolnPoolNsolns();
//        System.out.println("solutions found " + nsol);
//        
//        for (int k = 0; k < nsol; k++) {
//            System.out.println("Solution " + k);
//            for (int i = 0; i < n; i++) {
//                System.out.println("\tx[" + i + "]=" + cp.getValue(x[i], k));
//            }
//        }
//        cp.end();
//    }
//}
