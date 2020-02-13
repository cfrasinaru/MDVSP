///*
// * Copyright (C) 2019 Cristian Frasinaru
// */
//package ro.uaic.info.mdvsp;
//
//import ilog.concert.IloException;
//import ilog.concert.IloNumExpr;
//import ilog.concert.IloNumVar;
//import ilog.cplex.IloCplex;
//import java.io.FileNotFoundException;
//import java.io.PrintWriter;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import org.jgrapht.Graph;
//import org.jgrapht.Graphs;
//import org.jgrapht.graph.DefaultDirectedGraph;
//import org.jgrapht.graph.DefaultEdge;
//
///**
// *
// * @author Cristian Frăsinaru
// */
//public class Problem {
//
//    String name;
//    final int m; //depots
//    final int n; //trips
//    final int nbVehicles[];
//    int totalVehicles = 0;
//    final int cost[][];
//    Graph<Integer, DefaultEdge> graph;
//    IloNumVar x[][];
//    IloNumVar[] d, t;
//    IloCplex cp;
//    List<Tour> tours = new ArrayList<>();
//    List<Tour> forbidden = new ArrayList<>();
//    private static final int STEPS = 1;
//
//    /**
//     *
//     * @param name
//     * @param m depots
//     * @param n trips
//     */
//    public Problem(String name, int m, int n) {
//        this.m = m;
//        this.n = n;
//        this.nbVehicles = new int[m];
//        this.cost = new int[m + n][m + n];
//    }
//
//    public void solve() throws IloException, FileNotFoundException {
//        String repo = "d:/java/MDVSP/badTours.txt";
//
//        this.forbidden = Tour.load(repo);
//        //this.forbidden = findAllPaths();
//        PrintWriter pw = new PrintWriter(repo);
//        for (Tour bad : forbidden) {
//            int d0 = bad.get(0);
//            int d1 = bad.get(bad.size() - 1);
//            if (d0 < m && d1 < m && d0 != d1) {
//                pw.println(bad);
//            }
//        }
//        pw.flush();
//        System.out.println("Forbidden tours: " + forbidden.size());
//        for (int step = 0; step < STEPS; step++) {
//            System.out.println("Step " + step);
//            solve2();
//            int bad = 0;
//            for (Tour tour : tours) {
//                int d0 = tour.get(0);
//                int d1 = tour.get(tour.size() - 1);
//                if (d0 < m && d1 < m && d0 != d1) {
//                    bad++;
//                    forbidden.add(tour);
//                    pw.println(tour);
//                }
//            };
//            pw.flush();
//            System.out.println("----------------------------------------------------------------------");
//            System.out.println("Bad tours: " + bad + " / " + tours.size());
//            //System.out.println("Forbidden tours: " + forbidden);
//            System.out.println("----------------------------------------------------------------------");
//            if (bad == 0) {
//                break;
//            }
//        }
//        printTours();
//        //findAllPaths();
//        pw.close();
//    }
//
//    private void solve2() throws IloException {
//        //createGraph();
//        this.cp = new IloCplex();
//        //cp.setOut(null);
//        this.x = new IloNumVar[n + m][n + m];
//        this.d = new IloNumVar[n + m];
//        this.t = new IloNumVar[n + m];
//
//        for (int i = 0; i < n + m; i++) {
//            if (i < m) {
//                //depot
//                d[i] = cp.intVar(i, i);
//                //t[i] = cp.intVar(0, 0);
//            } else {
//                //trip
//                d[i] = cp.intVar(0, m - 1);
//                //t[i] = cp.intVar(1, n - 1);
//            }
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
//                //cp.add(cp.eq(cp.prod(cp.diff(d[i], d[j]), x[i][j]), 0));
//                //cp.add(cp.eq( cp.prod(cp.diff(d[i], d[j]), x[i][j]), cp.constant(0)));
//                //cp.add(cp.ifThen(cp.eq(x[i][j], 1), cp.eq(d[i], d[j])));
//                /*
//                cp.add(
//                        cp.ifThen(cp.eq(x[i][j], 1),
//                                cp.or(
//                                        cp.eq(t[j], 0),
//                                        cp.eq(t[j], cp.sum(t[i], 1))
//                                )
//                        )
//                );
//                 */
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
//        for (Tour t : forbidden) {
//            IloNumExpr path = cp.constant(0);
//            for (int k = 0; k < t.size() - 1; k++) {
//                int i = t.get(k);
//                int j = t.get(k + 1);
//                path = cp.sum(path, x[i][j]);
//            }
//            cp.addLe(path, t.size() - 2);
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
//            getTours();
//            //printSolution();
//            //printUsedVehicles();
//            //printTours();
//        }
//
//        cp.end();
//    }
//
//    private void createGraph() {
//        graph = new DefaultDirectedGraph<>(DefaultEdge.class);
//        for (int i = 0; i < n + m; i++) {
//            graph.addVertex(i);
//        }
//        for (int i = 0; i < n + m; i++) {
//            for (int j = 0; j < n + m; j++) {
//                if (cost[i][j] > 0) {
//                    graph.addEdge(i, j);
//                }
//            }
//        }
//        /*
//        JGraphXAdapter<Integer, DefaultEdge> graphAdapter = new JGraphXAdapter<>(graph);
//        mxIGraphLayout layout = new mxCircleLayout(graphAdapter);
//        layout.execute(graphAdapter.getDefaultParent());
//        BufferedImage image = mxCellRenderer.createBufferedImage(graphAdapter, null, 2, Color.WHITE, true, null);
//        File imgFile = new File("d:/java/MDVSP/graph.png");
//        try {
//            ImageIO.write(image, "PNG", imgFile);
//        } catch (IOException ex) {
//            Logger.getLogger(Problem.class.getName()).log(Level.SEVERE, null, ex);
//        }*/
//    }
//
//    private List<Tour> findAllPaths() {
//        createGraph();
//        List<Tour> list = new ArrayList<>();
//        boolean visited[] = new boolean[n + m];
//        Arrays.fill(visited, 0, n + m, false);
//        Tour current = new Tour();
//        findAllPathsRec(0, 1, current, list, visited);
//        /*
//        for (int i = 0; i < m - 1; i++) {
//            for (int j = i + 1; j < m; j++) {
//                findAllPathsRec(i, j, current, list, visited);
//            }
//        }
//         */
//        return list;
//    }
//
//    private void findAllPathsRec(int from, int to, Tour current, List<Tour> list, boolean visited[]) {
//        visited[from] = true;
//        current.add(from);
//        for (int i : Graphs.neighborSetOf(graph, from)) {
//            if (i == to) {
//                current.add(to);
//                list.add(new Tour(current));
//                current.remove((Integer) to);
//            } else {
//                if (i < m || visited[i]) {
//                    continue;
//                }
//                findAllPathsRec(i, to, current, list, visited);
//            }
//        }
//        visited[from] = false;
//        current.remove((Integer) from);
//    }
//
//    private void printSolution() throws IloException {
//        for (int i = 0; i < n + m; i++) {
//            for (int j = 0; j < n + m; j++) {
//                if (cp.getValue(x[i][j]) == 1) {
//                    System.out.println("x[" + i + "][" + j + "]=" + cp.getValue(x[i][j]));
//                    //System.out.println("\td[" + i + "]= " + cp.getValue(d[i]) + ", d[" + j + "]=" + cp.getValue(d[j]));
//                    //System.out.println("\tt[" + i + "]= " + cp.getValue(t[i]) + ", t[" + j + "]=" + cp.getValue(t[j]));
//                }
//            }
//        }
//    }
//
//    private void printUsedVehicles() throws IloException {
//        for (int i = 0; i < m; i++) {
//            int used = 0;
//            for (int j = m; j < m + n; j++) {
//                if (cp.getValue(x[i][j]) == 1) {
//                    used++;
//                }
//            }
//            System.out.println("Depot " + i + ": " + used + " vehicles");
//        }
//    }
//
//    private void printTours() throws IloException {
//        for (Tour tour : tours) {
//            System.out.print(tour.get(0));
//            for (int i = 1; i < tour.size(); i++) {
//                System.out.print("->" + tour.get(i));
//            }
//            System.out.println();
//        }
//    }
//
//    private void getTours() throws IloException {
//        tours = new ArrayList<>();
//        boolean visited[] = new boolean[n + m];
//        Arrays.fill(visited, 0, n + m, false);
//        for (int i = 0; i < n + m; i++) {
//            for (int j = 0; j < n + m; j++) {
//                if (cp.getValue(x[i][j]) == 0 || visited[i]) {
//                    continue;
//                }
//                if (i >= m) {
//                    visited[i] = true;
//                }
//                Tour tour = new Tour();
//                tours.add(tour);
//                tour.add(i);
//                int next = j;
//                while (next >= 0 && !visited[next]) {
//                    if (next >= m) {
//                        visited[next] = true;
//                    }
//                    tour.add(next);
//                    next = findNext(next);
//                }
//            }
//        }
//    }
//
//    private int findNext(int i) throws IloException {
//        if (i < m) {
//            return -1;
//        }
//        for (int j = 0; j < n + m; j++) {
//            if (cp.getValue(x[i][j]) == 1) {
//                return j;
//            }
//        }
//        return -1;
//    }
//
//    @Override
//    public String toString() {
//        StringBuilder sb = new StringBuilder();
//        sb.append(name).append(":").append("nbDepots=").append(m).append(", nbTrips=").append(n);
//        return sb.toString();
//    }
//
//}
