package ro.uaic.info.mdvsp.bb2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Stack;

import edu.princeton.cs.algs4.BellmanFordSP;
import edu.princeton.cs.algs4.DirectedEdge;
import edu.princeton.cs.algs4.EdgeWeightedDigraph;
import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;
import gurobi.GRB.DoubleParam;
import gurobi.GRB.IntParam;
import gurobi.GRB.StringAttr;
//import gurobi.GRB.StringParam;

public class c_heuristic {

    public static int n, m, inst;
    public static int[] next = new int[n + m];
    public static double[][] c = new double[n + m][n + m];
    public static double[][] sol = new double[n + m][n + m];
    public static GRBEnv env;
    public static GRBModel model;
    public static int[][] instances = {{1289114, 2516247, 3830912, 1292411, 2422112, 3500160},
    {1241618, 2413393, 3559176, 1276919, 2524293, 3802650},
    {1283811, 2452905, 3649757, 1304251, 2556313, 3605094},
    {1258634, 2490812, 3406815, 1277838, 2478393, 3515802},
    {1317077, 2519191, 3567122, 1276010, 2498388, 3704953}}; // new int[5][6];
    public static double precision_a = 1e-9;
    public static double precision_b = 1e-15;
    public static double precision_c = 1e-5;

    public static String convert(String dataFile, String vtype) {
        inst = Integer.parseInt(dataFile.substring(dataFile.length() - 5, dataFile.length() - 4));
        // System.out.println("inst = " + inst);
        File file = new File("../data/" + dataFile);
        String lp_fileName = "lp_" + dataFile.substring(0, dataFile.length() - 3) + "lp";
        File lp_file = new File("../data/results/" + lp_fileName);
        BufferedReader reader = null;
        BufferedWriter writer = null;
        String text = null;
        String[] nors = null;
        double[] r = new double[n + m];

        try {
            if (!lp_file.exists()) {
                lp_file.createNewFile();
            }
            FileWriter fw = new FileWriter(lp_file);
            writer = new BufferedWriter(fw);
            reader = new BufferedReader(new FileReader(file));
            if ((text = reader.readLine()) != null) {
                nors = text.split("\t", 0);
            }
            m = Integer.parseInt(nors[0]);// the number of depots
            n = Integer.parseInt(nors[1]);// the number of trips

            if (m != nors.length - 2) {
                return "-1";
            }
            System.out.println(m + " depots & " + n + " trips.");
            r = new double[n + m];
            for (int i = 0; i < m; i++) {
                r[i] = Double.parseDouble(nors[i + 2]);// the capacities of depots
            }
            for (int i = m; i < m + n; i++) {
                r[i] = 1;
            }
            c = new double[n + m][n + m];
            for (int i = 0; i < n + m; i++) {
                for (int j = 0; j < n + m; j++) {
                    c[i][j] = -1.0;
                }
            }
            for (int i = 0; i < m; i++) {
                if ((text = reader.readLine()) != null) {
                    nors = text.split("\t", 0);
                    for (int j = 0; j < m; j++) {
                        c[i][j] = -1;
                    }
                    c[i][i] = 0;
                    for (int j = m; j < n + m; j++) {
                        c[i][j] = Double.parseDouble(nors[j]);
                    }
                }
            }
            for (int i = m; i < n + m; i++) {
                if ((text = reader.readLine()) != null) {
                    nors = text.split("\t", 0);
                    for (int j = 0; j < n + m; j++) {
                        c[i][j] = Double.parseDouble(nors[j]);
                    }
                    c[i][i] = -1.0;
                }
            }
            writer.write("Minimize ");
            writer.newLine();
            writer.write(c[0][0] + " x0y0");
            for (int j = 1; j < n + m; j++) {
                if (c[0][j] >= 0.0) {
                    writer.write(" + " + c[0][j] + " x0y" + j);
                }
            }
            for (int i = 1; i < n + m; i++) {
                for (int j = 0; j < n + m; j++) {
                    if (c[i][j] >= 0.0) {
                        writer.write(" + " + c[i][j] + " x" + i + "y" + j);
                    }
                }
            }

            writer.newLine();
            writer.write("Subject To ");

            for (int i = 0; i < n + m; i++) {
                writer.newLine();
                writer.write("ca" + i + ": ");
                for (int j = 0; j < n + m; j++) {
                    if (c[j][i] >= 0.0) {
                        writer.write(" + " + 1 + " x" + j + "y" + i);
                    }
                }
                writer.write(" = " + r[i]);
            }
            for (int i = 0; i < n + m; i++) {
                writer.newLine();
                writer.write("cb" + i + ": ");
                for (int j = 0; j < n + m; j++) {
                    if (c[i][j] >= 0.0) {
                        writer.write(" + " + 1 + " x" + i + "y" + j);
                    }
                }
                writer.write(" = " + r[i]);
            }

            writer.newLine();
            writer.write("Bounds ");
            for (int i = 0; i < n + m; i++) {
                for (int j = 0; j < n + m; j++) {
                    if (c[i][j] >= 0.0) {
                        writer.newLine();
                        writer.write("0.0 <= x" + i + "y" + j);
                    }
                }
            }
            if (vtype == "Integers") {
                writer.newLine();
                writer.write("Integers ");

                for (int i = 0; i < n + m; i++) {
                    for (int j = 0; j < n + m; j++) {
                        if (c[i][j] >= 0.0) {
                            writer.newLine();
                            writer.write(" x" + i + "y" + j);
                        }
                    }
                }
            }
            writer.newLine();
            writer.write("End ");
            writer.newLine();
            System.out.println("End writing to file");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
            }
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (Exception ex) {
                System.out.println("Error in closing the BufferedWriter" + ex);
            }
        }

        return lp_fileName;
    }

    public static ArrayList<subPathVars>[] cloneArrayList(ArrayList<subPathVars>[] corrected_list) {
        ArrayList<subPathVars>[] new_corrected_list = (ArrayList<subPathVars>[]) new ArrayList[m * m + m];
        for (int i = 0; i < corrected_list.length; i++) {
            if (corrected_list[i] != null && corrected_list[i].size() > 0) {
                new_corrected_list[i] = new ArrayList<subPathVars>(corrected_list[i]);
            } else {
                new_corrected_list[i] = new ArrayList<subPathVars>();
            }
        }
        return new_corrected_list;
    }

    public static void bellmanDigraph1() throws GRBException {// for the simple heuristic
        GRBVar[] variabile = model.getVars();
        String nume = "";
        int stop = 1, i, j;
        double valoare = 0, current_optimum = 0.0, new_optimum = 0;
        ArrayList<ArrayList<Integer>> list = new ArrayList<ArrayList<Integer>>();
        HashMap<Arc, ArrayList<ArrayList<Integer>>> arc_list = new HashMap<Arc, ArrayList<ArrayList<Integer>>>();

        System.out.println("Bellman in");
        long start1 = 0, aux1 = System.nanoTime(), start2 = 0, aux2 = System.nanoTime();
        int k = 0;
        while (stop > 0) {
            k++;
            aux1 = System.nanoTime();
            EdgeWeightedDigraph graph = new EdgeWeightedDigraph(n + m);
            DirectedEdge e;
            for (int p = 0; p < variabile.length; p++) {
                nume = variabile[p].get(GRB.StringAttr.VarName);
                valoare = 1.0 - variabile[p].get(GRB.DoubleAttr.X);
                // System.out.println("valoare = "+ variabile[p].get(GRB.DoubleAttr.X));
                i = Integer.valueOf(nume.substring(nume.indexOf('x') + 1, nume.indexOf('y')));
                j = Integer.valueOf(nume.substring(nume.indexOf('y') + 1));
                if (i != j) {
                    e = new DirectedEdge(i, j, Math.abs(valoare));
                    graph.addEdge(e);
                }
            }
            stop = bellmanAlg1(graph, list, arc_list, 0);
            aux1 = System.nanoTime() - aux1;
            System.out.println("Bellman time = " + aux1 * 1e-9);
            start1 += aux1;
            aux2 = System.nanoTime();
            model.optimize();
            aux2 = System.nanoTime() - aux2;
            start2 += aux2;
            // System.out.println("Gurobi time = " + aux2 * 1e-9);

            new_optimum = model.get(GRB.DoubleAttr.ObjVal);
            // System.out.println();
            // System.out.println("current optimum = " + current_optimum + "|| new optimum =
            // " + new_optimum);
            // System.out.println();
            if (Math.abs(current_optimum - new_optimum) < 1.0e-8) {
                System.out.println("Bellman out");
                current_optimum = new_optimum;
                // System.out.println("current_optimum = " + current_optimum);
                System.out.println("k = " + k + " |Bellman final time = " + start1 * 1e-9 + " Gurobi final time = "
                        + start2 * 1e-9);
                return;
            }
            current_optimum = new_optimum;
        }
        System.out.println(
                "k = " + k + " | Bellman final time = " + start1 * 1e-9 + " Gurobi final time = " + start2 * 1e-9);
        return;
    }

    public static int bellmanAlg1(EdgeWeightedDigraph graph, ArrayList<ArrayList<Integer>> list, Map arc_list,
            int i_start) throws GRBException {
        int y = 0, constrLength = 0, from, to = -1, path_index = 0;
        boolean out = false;
        double dist = 0;
        Arc arc = new Arc();
        List<Integer>[] path_list = new ArrayList[m];
        LinkedList<Arc> _arc_path_list = new LinkedList<Arc>();
        ArrayList<ArrayList<Integer>> _paths = new ArrayList<ArrayList<Integer>>();
        BellmanFordSP bellman;
        GRBLinExpr expr;
        GRBVar vare;

        for (int s = i_start; s < m; s++) {
            path_list = new ArrayList[m];
            path_index = 0;
            bellman = new BellmanFordSP(graph, s);
            expr = new GRBLinExpr();
            vare = null;
            for (int i = 0; i < m; i++) {
                if (i != s) {
                    dist = bellman.distTo(i);
                    if ((dist < Double.POSITIVE_INFINITY) && ((dist < 1.0) && (1.0 - dist > 1e-15))) {
                        // System.out.println("Drum eligibil = " + dist);
                        path_index++;
                        path_list[path_index] = new ArrayList<Integer>();
                        path_list[path_index].add(s);
                        Iterator<DirectedEdge> iterator = bellman.pathTo(i).iterator();
                        while (iterator.hasNext()) {
                            int tto = iterator.next().to();
                            path_list[path_index].add(tto);
                        }
                    }
                }
            }
            for (int i = 0; i < m; i++) {
                for (int j = 0; j < m; j++) {
                    out = false;
                    if (path_list[i] != null && path_list[j] != null && i != j) {
                        if (path_list[i].size() < path_list[j].size()) {
                            for (int h = 0; h < path_list[i].size(); h++) {
                                if (path_list[i].get(h) != path_list[j].get(h)) {
                                    out = true;
                                    break;
                                }
                            }
                            if (!out) {
                                path_list[j] = null;
                            }
                        } else if (path_list[j].size() < path_list[i].size()) {
                            for (int h = 0; h < path_list[j].size(); h++) {
                                if (path_list[j].get(h).intValue() != path_list[i].get(h).intValue()) {
                                    out = true;
                                    break;
                                }
                            }
                            if (!out) {
                                path_list[i] = null;
                            }
                        }
                    }
                }
            }
            for (int i = 0; i < m; i++) {
                if (path_list[i] != null) {
                    _arc_path_list = new LinkedList<Arc>();
                    ArrayList<Integer> n_path = new ArrayList<Integer>();
                    // n_path =
                    y++;
                    constrLength = 0;
                    expr = new GRBLinExpr();
                    vare = null;
                    from = s;
                    n_path.add(s);
                    for (int h = 1; h < path_list[i].size(); h++) {
                        to = path_list[i].get(h);
                        arc = new Arc(from, to);
                        // _arc_path_list.add(new Arc(from, to));
                        if (arc_list.containsKey(arc)) {
                            _paths = (ArrayList<ArrayList<Integer>>) arc_list.get(arc);
                            _paths.add(n_path);
                            arc_list.put(arc, _paths);
                        } else {
                            _paths = new ArrayList<ArrayList<Integer>>();
                            _paths.add(n_path);
                            arc_list.put(arc, _paths);
                        }
                        n_path.add(to);
                        constrLength++;
                        vare = model.getVarByName("x" + from + "y" + to);
                        expr.addTerm(1.0, vare);
                        from = to;
                    }
                    list.add(n_path);

                    model.addConstr(expr, GRB.LESS_EQUAL, constrLength - 1, "");
                }
            }
            // System.out.println("ff");
        }
        return y;
    }

    public static void bellmanDigraph2New() throws GRBException {// for the simple heuristic
        GRBVar[] variabile = model.getVars();
        String nume = "";
        int stop = 1, i, j;
        double valoare = 0, current_optimum = 0.0, new_optimum = 0;
        ArrayList<ArrayList<Integer>> list = new ArrayList<ArrayList<Integer>>();
        HashMap<Arc, ArrayList<ArrayList<Integer>>> arc_list = new HashMap<Arc, ArrayList<ArrayList<Integer>>>();

        System.out.println("Bellman in");
        long start1 = 0, aux1 = System.nanoTime(), start2 = 0, aux2 = System.nanoTime();
        int k = 0;
        while (stop > 0) {
            k++;
            aux1 = System.nanoTime();
            EdgeWeightedDigraph graph = new EdgeWeightedDigraph(n + m);
            DirectedEdge e;
            for (int p = 0; p < variabile.length; p++) {
                nume = variabile[p].get(GRB.StringAttr.VarName);
                valoare = 1.0 - variabile[p].get(GRB.DoubleAttr.X);
                i = Integer.valueOf(nume.substring(nume.indexOf('x') + 1, nume.indexOf('y')));
                j = Integer.valueOf(nume.substring(nume.indexOf('y') + 1));
                if (i != j) {
                    e = new DirectedEdge(i, j, Math.abs(valoare));
                    graph.addEdge(e);
                }
            }
            stop = bellmanAlg2New(graph, list, arc_list, 0);
            aux1 = System.nanoTime() - aux1;
            start1 += aux1;
            aux2 = System.nanoTime();
            model.optimize();
            aux2 = System.nanoTime() - aux2;
            start2 += aux2;
            new_optimum = model.get(GRB.DoubleAttr.ObjVal);
            if (Math.abs(current_optimum - new_optimum) < 1.0e-8) {
                System.out.println("Bellman out");
                current_optimum = new_optimum;
                System.out.println("k = " + k + " |Bellman final time = " + start1 * 1e-9 + " Gurobi final time = "
                        + start2 * 1e-9);
                return;
            }
            current_optimum = new_optimum;
        }
        System.out.println(
                "k = " + k + " | Bellman final time = " + start1 * 1e-9 + " Gurobi final time = " + start2 * 1e-9);
        return;
    }

    public static int bellmanAlg2New(EdgeWeightedDigraph graph, ArrayList<ArrayList<Integer>> list, Map arc_list,
            int i_start) throws GRBException {
        int y = 0, constrLength = 0, from, to = -1, path_index = 0;
        boolean out = false;
        double dist = 0;
        Arc arc = new Arc();
        List<Integer>[] path_list = new ArrayList[m];
        LinkedList<Arc> _arc_path_list = new LinkedList<Arc>();
        ArrayList<ArrayList<Integer>> _paths = new ArrayList<ArrayList<Integer>>();
        BellmanNew bellman;
        GRBLinExpr expr;
        GRBVar vare;

        for (int s = i_start; s < m; s++) {
            path_list = new ArrayList[m];
            path_index = 0;
            bellman = new BellmanNew(graph, s);
            expr = new GRBLinExpr();
            vare = null;
            for (int i = 0; i < m; i++) {
                if (i != s) {
                    dist = bellman.distTo(i);
                    if ((dist < Double.POSITIVE_INFINITY) && ((dist < 1.0) && (1.0 - dist > 1e-15))) {
                        // System.out.println("Drum eligibil = " + dist);
                        path_index++;
                        path_list[path_index] = new ArrayList<Integer>();
                        path_list[path_index].add(s);
                        Iterator<DirectedEdge> iterator = bellman.pathTo(i).iterator();
                        while (iterator.hasNext()) {
                            int tto = iterator.next().to();
                            path_list[path_index].add(tto);
                        }
                    }
                }
            }
            for (int i = 0; i < m; i++) {
                for (int j = 0; j < m; j++) {
                    out = false;
                    if (path_list[i] != null && path_list[j] != null && i != j) {
                        if (path_list[i].size() < path_list[j].size()) {
                            for (int h = 0; h < path_list[i].size(); h++) {
                                if (path_list[i].get(h) != path_list[j].get(h)) {
                                    out = true;
                                    break;
                                }
                            }
                            if (!out) {
                                path_list[j] = null;
                            }
                        } else if (path_list[j].size() < path_list[i].size()) {
                            for (int h = 0; h < path_list[j].size(); h++) {
                                if (path_list[j].get(h).intValue() != path_list[i].get(h).intValue()) {
                                    out = true;
                                    break;
                                }
                            }
                            if (!out) {
                                path_list[i] = null;
                            }
                        }
                    }
                }
            }
            for (int i = 0; i < m; i++) {
                if (path_list[i] != null) {
                    _arc_path_list = new LinkedList<Arc>();
                    ArrayList<Integer> n_path = new ArrayList<Integer>();
                    // n_path =
                    y++;
                    constrLength = 0;
                    expr = new GRBLinExpr();
                    vare = null;
                    from = s;
                    n_path.add(s);
                    for (int h = 1; h < path_list[i].size(); h++) {
                        to = path_list[i].get(h);
                        arc = new Arc(from, to);
                        // _arc_path_list.add(new Arc(from, to));
                        if (arc_list.containsKey(arc)) {
                            _paths = (ArrayList<ArrayList<Integer>>) arc_list.get(arc);
                            _paths.add(n_path);
                            arc_list.put(arc, _paths);
                        } else {
                            _paths = new ArrayList<ArrayList<Integer>>();
                            _paths.add(n_path);
                            arc_list.put(arc, _paths);
                        }
                        n_path.add(to);
                        constrLength++;
                        vare = model.getVarByName("x" + from + "y" + to);
                        expr.addTerm(1.0, vare);
                        from = to;
                    }
                    list.add(n_path);

                    model.addConstr(expr, GRB.LESS_EQUAL, constrLength - 1, "");
                }
            }
            // System.out.println("ff");
        }
        return y;
    }

    public static int _new_BellmanFMSolveAlgEff(GRBModel model, EdgeWeightedDigraph graph,
            ArrayList<ArrayList<Integer>> list, Map arc_list, int i_start) throws GRBException {
        int y = 0, constrLength = 0, from, to = -1, path_index = 0;
        boolean out = false;
        double dist = 0;
        Arc arc = new Arc();
        List<Integer>[] path_list = new ArrayList[m];
        LinkedList<Arc> _arc_path_list = new LinkedList<Arc>();
        ArrayList<ArrayList<Integer>> _paths = new ArrayList<ArrayList<Integer>>();
        BellmanFordSP bellman;
        GRBLinExpr expr;
        GRBVar vare;

        for (int s = i_start; s < m; s++) {
            path_list = new ArrayList[m];
            path_index = 0;
            bellman = new BellmanFordSP(graph, s);
            expr = new GRBLinExpr();
            vare = null;
            for (int i = 0; i < m; i++) {
                if (i != s) {
                    dist = bellman.distTo(i);
                    if ((dist < Double.POSITIVE_INFINITY) && ((dist < 1.0) && (1.0 - dist > 1e-15))) {
                        // System.out.println("Drum eligibil = " + dist);
                        path_index++;
                        path_list[path_index] = new ArrayList<Integer>();
                        path_list[path_index].add(s);
                        Iterator<DirectedEdge> iterator = bellman.pathTo(i).iterator();
                        while (iterator.hasNext()) {
                            int tto = iterator.next().to();
                            path_list[path_index].add(tto);
                        }
                    }
                }
            }
            for (int i = 0; i < m; i++) {
                for (int j = 0; j < m; j++) {
                    out = false;
                    if (path_list[i] != null && path_list[j] != null && i != j) {
                        if (path_list[i].size() < path_list[j].size()) {
                            for (int h = 0; h < path_list[i].size(); h++) {
                                if (path_list[i].get(h) != path_list[j].get(h)) {
                                    out = true;
                                    break;
                                }
                            }
                            if (!out) {
                                path_list[j] = null;
                            }
                        } else if (path_list[j].size() < path_list[i].size()) {
                            for (int h = 0; h < path_list[j].size(); h++) {
                                if (path_list[j].get(h).intValue() != path_list[i].get(h).intValue()) {
                                    out = true;
                                    break;
                                }
                            }
                            if (!out) {
                                path_list[i] = null;
                            }
                        }
                    }
                }
            }
            for (int i = 0; i < m; i++) {
                if (path_list[i] != null) {
                    _arc_path_list = new LinkedList<Arc>();
                    ArrayList<Integer> n_path = new ArrayList<Integer>();
                    // n_path =
                    y++;
                    constrLength = 0;
                    expr = new GRBLinExpr();
                    vare = null;
                    from = s;
                    n_path.add(s);
                    for (int h = 1; h < path_list[i].size(); h++) {
                        to = path_list[i].get(h);
                        arc = new Arc(from, to);
                        // _arc_path_list.add(new Arc(from, to));
                        if (arc_list.containsKey(arc)) {
                            _paths = (ArrayList<ArrayList<Integer>>) arc_list.get(arc);
                            _paths.add(n_path);
                            arc_list.put(arc, _paths);
                        } else {
                            _paths = new ArrayList<ArrayList<Integer>>();
                            _paths.add(n_path);
                            arc_list.put(arc, _paths);
                        }
                        n_path.add(to);
                        constrLength++;
                        vare = model.getVarByName("x" + from + "y" + to);
                        expr.addTerm(1.0, vare);
                        from = to;
                    }
                    list.add(n_path);

                    model.addConstr(expr, GRB.LESS_EQUAL, constrLength - 1, "");
                }
            }
            // System.out.println("ff");
        }
        return y;
    }

    public static double solRepair(String dataFile, ArrayList<subPathVars> vars,
            ArrayList<subPathVars>[] corrections_list, int[][] graph) {
        double sum = 0;
        int graph_dim = 0, v = -1, last_vertex = -1, vertex = -1;
        int[] degrees = new int[m];
        boolean[] marked = new boolean[m];
        ArrayList<Integer> path = new ArrayList<Integer>();

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < m; j++) {
                if (graph[i][j] != 0) {
                    graph_dim += graph[i][j];
                    degrees[i] += graph[i][j];
                }
            }
        }
        for (int i = 0; i < m; i++) {
            marked[i] = false;
        }
        while (graph_dim > 0) {
            last_vertex = -1;
            vertex = (new Random()).nextInt(m);
            while (marked[vertex] == true) {
                vertex = (new Random()).nextInt(m);
            }
            // System.out.println("Chosen vertex = " + vertex);
            Stack<Integer> stiva = new Stack<Integer>(); // dfs for finding a cycle:
            path = new ArrayList<Integer>();
            path.add(vertex);
            stiva.push(vertex);
            marked[vertex] = true;
            while (stiva.size() != 0) {
                vertex = stiva.pop();
                for (int h = 0; h < m; h++) {
                    if (graph[vertex][h] > 0) {
                        if (marked[h]) {
                            last_vertex = h;
                        } else {
                            path.add(h);
                            stiva.push(h);
                            marked[h] = true;
                        }
                        break;
                    }
                }
            }
            if (last_vertex != -1 && path.size() > 0) {
                v = vertex;
                vertex = last_vertex;
                int k = path.size() - 1;
                // System.out.println("Path size = " + path.size());
                // System.out.println("path: " + path.toString());
                if (k == 0) {
                    path.size();
                    // System.out.println("Path size = " + path.size());
                }
                while (v != last_vertex) {
                    k--;
                    graph[v][vertex]--;
                    degrees[v]--;
                    if (degrees[v] == 0) {
                        marked[v] = true;
                    } else {
                        marked[v] = false;
                    }
                    graph_dim--;
                    vertex = v;
                    v = path.get(k);
                }
                graph[v][vertex]--;
                degrees[v]--;
                if (degrees[v] == 0) {
                    marked[v] = true;
                } else {
                    marked[v] = false;
                }
                graph_dim--;
                k--;
                while (k != -1) {
                    marked[path.get(k)] = false;
                    path.remove(k);
                    k--;
                }

                sum += cycleCorrections(vars, corrections_list, path);// corrections along the
            }
        }
        return sum;
    }

    public static double cycleCorrections(ArrayList<subPathVars> vars, ArrayList<subPathVars>[] corrections_list,
            ArrayList<Integer> path) {
        // A REPAIR METHOD THAT TAKES EACH PATH AND DECIDE THE BEST WAY TO REPAIR IT
        double sum1 = 0.0, sum2 = 0.0, sum = 0.0, diff1 = 0.0, diff2 = 0.0;
        int i, j, trip1, trip2;
        subPathVars var = null;

        // first type of repair
        i = path.get(path.size() - 1);
        j = path.get(0);
        for (int p = 0; p <= path.size() - 2; p++) {
            trip1 = corrections_list[m * i + j].get(0).to();
            if (c[trip1][i] < 0 || c[trip1][j] < 0) {
                System.out.println("Negativ +: " + c[trip1][i] + " Negativ -: " + c[trip1][j]);
            }
            sum1 += c[trip1][i] - c[trip1][j];
            i = path.get(p);
            j = path.get(p + 1);
        }

        trip1 = corrections_list[m * i + j].get(0).to();
        if (c[trip1][i] < 0 || c[trip1][j] < 0) {
            System.out.println("Negativ +: " + c[trip1][i] + " Negativ -: " + c[trip1][j]);
        }
        sum1 += c[trip1][i] - c[trip1][j];

        // second type of repair
        i = path.get(path.size() - 1);
        j = path.get(0);
        for (int p = 0; p <= path.size() - 2; p++) {
            trip2 = corrections_list[m * i + j].get(0).from();
            if (c[j][trip2] < 0 || c[i][trip2] < 0) {
                System.out.println("Negativ +: " + c[j][trip2] + " Negativ -: " + c[i][trip2]);
            }
            sum2 += c[j][trip2] - c[i][trip2];
            i = path.get(p);
            j = path.get(p + 1);
        }

        trip2 = corrections_list[m * i + j].get(0).from();
        if (c[j][trip2] < 0 || c[i][trip2] < 0) {
            System.out.println("Negativ +: " + c[j][trip2] + " Negativ -: " + c[i][trip2]);
        }
        sum2 += c[j][trip2] - c[i][trip2];

        if (sum1 < sum2) {
            sum = sum1;
            i = path.get(path.size() - 1);
            j = path.get(0);
            for (int p = 0; p <= path.size() - 2; p++) {
                trip1 = corrections_list[m * i + j].get(0).to();
                var = new subPathVars(trip1, j);
                vars.remove(var);
                var = new subPathVars(trip1, i);
                vars.add(var);
                i = path.get(p);
                j = path.get(p + 1);
            }
            trip1 = corrections_list[m * i + j].get(0).to();
            var = new subPathVars(trip1, j);
            vars.remove(var);
            var = new subPathVars(trip1, i);
            vars.add(var);
        } else {
            sum = sum2;
            i = path.get(path.size() - 1);
            j = path.get(0);
            for (int p = 0; p <= path.size() - 2; p++) {
                trip2 = corrections_list[m * i + j].get(0).from();
                var = new subPathVars(i, trip2);
                vars.remove(var);
                var = new subPathVars(j, trip2);
                vars.add(var);
                i = path.get(p);
                j = path.get(p + 1);
            }
            trip2 = corrections_list[m * i + j].get(0).from();
            var = new subPathVars(i, trip2);
            vars.remove(var);
            var = new subPathVars(j, trip2);
            vars.add(var);

        }
        /* */
        i = path.get(path.size() - 1);
        j = path.get(0);
        for (int p = 0; p <= path.size() - 2; p++) {
            corrections_list[m * i + j].remove(0);
            i = path.get(p);
            j = path.get(p + 1);
        }
        corrections_list[m * i + j].remove(0);

        return sum;
    }

    public static double[][] verify(GRBModel model) {
        int h, k;
        double valoare, suma = 0.0;
        String nume;

        try {
            GRBVar[] varr = model.getVars();
            int nr_vars = model.get(GRB.IntAttr.NumVars);

            for (int j = 0; j < nr_vars; j++) {
                nume = varr[j].get(StringAttr.VarName);
                valoare = varr[j].get(GRB.DoubleAttr.X);
                if (varr[j].get(GRB.DoubleAttr.X) != 0) {
                    h = Integer.valueOf(nume.substring(nume.indexOf('x') + 1, nume.indexOf('y')));
                    k = Integer.valueOf(nume.substring(nume.indexOf('y') + 1));
                    if (c[h][k] < 0) {
                        System.out.println(" negativ: " + c[h][k]);
                    }
                    suma += valoare * c[h][k];
                }
            }
            System.out.println();
            System.out.println("suma = " + suma + " optim = " + model.get(GRB.DoubleAttr.ObjVal));
        } catch (GRBException e) {
            e.printStackTrace();
            System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
        }
        return c;
    }

    public static double[][] verify() {
        int h, k;
        double valoare, suma = 0.0;
        String nume;

        try {
            GRBVar[] varr = model.getVars();
            int nr_vars = model.get(GRB.IntAttr.NumVars);

            for (int j = 0; j < nr_vars; j++) {
                nume = varr[j].get(StringAttr.VarName);
                valoare = varr[j].get(GRB.DoubleAttr.X);
                if (varr[j].get(GRB.DoubleAttr.X) != 0) {
                    h = Integer.valueOf(nume.substring(nume.indexOf('x') + 1, nume.indexOf('y')));
                    k = Integer.valueOf(nume.substring(nume.indexOf('y') + 1));
                    if (c[h][k] < 0) {
                        System.out.println(" negativ: " + c[h][k]);
                    }
                    suma += valoare * c[h][k];
                }
            }
            System.out.println();
            System.out.println("suma = " + suma + " optim = " + model.get(GRB.DoubleAttr.ObjVal));
        } catch (GRBException e) {
            e.printStackTrace();
            System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
        }
        return c;
    }

    public static GRBVar checkIntegrality(GRBModel model) throws GRBException {

        double valoare;
        String nume;
        int i, j;
        GRBVar[] variabile = model.getVars();
        for (int k = 0; k < variabile.length; k++) {
            valoare = variabile[k].get(GRB.DoubleAttr.X);
            nume = variabile[k].get(StringAttr.VarName);
            i = Integer.valueOf(nume.substring(nume.indexOf('x') + 1, nume.indexOf('y')));
            j = Integer.valueOf(nume.substring(nume.indexOf('y') + 1));
            if (i != j && ((valoare < 0.5 && Math.abs(valoare) > precision_b)
                    || (valoare > 0.5 && Math.abs(1.0 - valoare) > precision_b))) {
                return variabile[k];
            }
        }
        return null;
    }

    public static void addIntegerConstraints() throws GRBException {
        GRBVar[] variabile = model.getVars();
        GRBVar var;
        for (int p = 0; p < variabile.length; p++) {
            variabile[p].set(GRB.CharAttr.VType, GRB.BINARY);
        }
        for (int i = 0; i < m; i++) {
            var = model.getVarByName("x" + i + "y" + i);
            var.set(GRB.CharAttr.VType, GRB.INTEGER);
        }
        model.update();
        // for (int p = 0; p < variabile.length; p++)
        // System.out.println("Type: " + variabile[p].get(GRB.CharAttr.VType));
    }

    public static void addIntegerConstraints(GRBModel model) throws GRBException {
        GRBVar[] variabile = model.getVars();
        GRBVar var;
        for (int p = 0; p < variabile.length; p++) {
            variabile[p].set(GRB.CharAttr.VType, GRB.BINARY);
        }
        for (int i = 0; i < m; i++) {
            var = model.getVarByName("x" + i + "y" + i);
            var.set(GRB.CharAttr.VType, GRB.INTEGER);
        }
        model.update();
        // for (int p = 0; p < variabile.length; p++)
        // System.out.println("Type: " + variabile[p].get(GRB.CharAttr.VType));

        // model.update();
    }

    public static void addContinuousConstraints() throws GRBException {
        GRBVar[] variabile = model.getVars();
        for (int p = 0; p < variabile.length; p++) {
            variabile[p].set(GRB.CharAttr.VType, GRB.CONTINUOUS);
        }
        model.update();
    }

    public static void addContinuousConstraints(GRBModel model) throws GRBException {
        GRBVar[] variabile = model.getVars();
        for (int p = 0; p < variabile.length; p++) {
            variabile[p].set(GRB.CharAttr.VType, GRB.CONTINUOUS);
        }
        model.update();
    }

    public static boolean displaySubtours(GRBModel model, GRBVar[] vars, int m, int n) throws GRBException {

        boolean out = false;
        double val = 0;
        int i = 0, j = 0, from = -1, to = -1, constrLength;
        int[] next = new int[n + m];
        String nume = "";
        List<Integer>[] s_list = new ArrayList[m];

        for (int h = 0; h < n + m; h++) {
            next[h] = -1;
        }

        for (int h = 0; h < m; h++) {
            s_list[h] = new ArrayList<Integer>();
        }

        for (int k = 0; k < vars.length; k++) {
            nume = vars[k].get(StringAttr.VarName);
            val = vars[k].get(GRB.DoubleAttr.X);
            if (val > 0) {
                i = Integer.valueOf(nume.substring(nume.indexOf('x') + 1, nume.indexOf('y')));
                j = Integer.valueOf(nume.substring(nume.indexOf('y') + 1));
                if (i != j) {
                    if (i < m) {
                        s_list[i].add(j);
                    } else {
                        next[i] = j;
                    }
                }
            }
        }

        for (int h = 0; h < m; h++) {
            if (!s_list[h].isEmpty()) {
                // System.out.println("Subtour:");
                for (int p = 0; p < s_list[h].size(); p++) {
                    from = h;
                    to = s_list[h].get(p);
                    // System.out.print(from + "->");
                    // System.out.print(to + "->");
                    while (next[to] != -1) {
                        from = to;
                        to = next[to];
                        // System.out.print(from + "->");
                        // System.out.print(to + "->");
                    }
                    if (to != h) {
                        out = true;
                        GRBLinExpr expr = new GRBLinExpr();
                        GRBVar vare;
                        constrLength = 0;
                        vare = null;
                        from = h;
                        to = s_list[h].get(p);
                        while (next[to] != -1) {
                            constrLength++;
                            vare = model.getVarByName("x" + from + "y" + to);
                            expr.addTerm(1.0, vare);
                            from = to;
                            to = next[to];
                        }
                        // System.out.println("Constrangere adaugata");
                        model.addConstr(expr, GRB.LESS_EQUAL, constrLength - 1, "");
                    }
                    // System.out.println();
                }
            }
        }
        model.update();
        return out;
    }

    public static void showSubtours(GRBModel model, GRBVar[] vars, int m, int n) throws GRBException {

        double val = 0;
        int i = 0, j = 0, from = -1, to = -1;
        int[] next = new int[n + m];
        String nume = "";
        List<Integer>[] s_list = new ArrayList[m];

        for (int h = 0; h < n + m; h++) {
            next[h] = -1;
        }

        for (int h = 0; h < m; h++) {
            s_list[h] = new ArrayList<Integer>();
        }

        for (int k = 0; k < vars.length; k++) {
            nume = vars[k].get(StringAttr.VarName);
            val = vars[k].get(GRB.DoubleAttr.X);
            if (val > 0) {
                i = Integer.valueOf(nume.substring(nume.indexOf('x') + 1, nume.indexOf('y')));
                j = Integer.valueOf(nume.substring(nume.indexOf('y') + 1));
                if (i != j) {
                    if (i < m) {
                        s_list[i].add(j);
                    } else {
                        next[i] = j;
                    }
                }
            }
        }
        for (int h = 0; h < m; h++) {
            if (!s_list[h].isEmpty()) {
                System.out.println("Subtour:");
                for (int p = 0; p < s_list[h].size(); p++) {
                    from = h;
                    to = s_list[h].get(p);
                    System.out.print(from + "->");
                    System.out.print(to + "->");
                    while (next[to] != -1) {
                        from = to;
                        to = next[to];
                        // System.out.print(from + "->");
                        System.out.print(to + "->");
                    }
                    System.out.println();
                }
            }
        }
    }

    public static ArrayList<subPathVars>[] repairSubtours(GRBModel model) throws GRBException {
        GRBVar[] vars = model.getVars();
        next = new int[n + m];
        double val = 0;
        int i = 0, j = 0, from = -1, to = -1, to1 = -1;
        int[][] graph = new int[m][m];
        String nume = "";
        ArrayList<subPathVars>[] corrections_list = (ArrayList<subPathVars>[]) new ArrayList[m * m + m];
        List<Integer>[] s_list = new ArrayList[m];

        for (int h = 0; h < n + m; h++) {
            next[h] = -1;
        }
        for (int h = 0; h < m; h++) {
            s_list[h] = new ArrayList<Integer>();
        }
        for (int k = 0; k < vars.length; k++) {
            nume = vars[k].get(StringAttr.VarName);
            val = vars[k].get(GRB.DoubleAttr.X);
            // System.out.println("val = "+ val);
            if (val > 0) {
                i = Integer.valueOf(nume.substring(nume.indexOf('x') + 1, nume.indexOf('y')));
                j = Integer.valueOf(nume.substring(nume.indexOf('y') + 1));
                if (i != j) {
                    if (i < m) {
                        s_list[i].add(j);
                    } else {
                        next[i] = j;
                    }
                }
            }
        }

        for (int h = 0; h < m; h++) {
            if (!s_list[h].isEmpty()) {
                for (int p = 0; p < s_list[h].size(); p++) {
                    from = h;
                    to = s_list[h].get(p);
                    while (next[to] != -1) {
                        to = next[to];
                    }
                    if (to != h) {
                        from = h;
                        to1 = s_list[h].get(p);
                        to = s_list[h].get(p);
                        while (next[to] != -1) {
                            from = to;
                            to = next[to];
                        }
                        if (corrections_list[m * h + to] == null) {
                            corrections_list[m * h + to] = new ArrayList<subPathVars>();
                        }
                        corrections_list[m * h + to].add(new subPathVars(to1, from));
                        graph[h][to]++;
                    } else {
                        // s_list[h].remove(p);
                        // System.out.println(" removed");
                    }
                }
            }
        }
        return corrections_list;
    }

    public static ArrayList<subPathVars>[] repairSubtours() throws GRBException {
        GRBVar[] vars = model.getVars();
        next = new int[n + m];
        double val = 0;
        int i = 0, j = 0, from = -1, to = -1, to1 = -1;
        int[][] graph = new int[m][m];
        String nume = "";
        ArrayList<subPathVars>[] corrections_list = (ArrayList<subPathVars>[]) new ArrayList[m * m + m];
        List<Integer>[] s_list = new ArrayList[m];

        for (int h = 0; h < n + m; h++) {
            next[h] = -1;
        }
        for (int h = 0; h < m; h++) {
            s_list[h] = new ArrayList<Integer>();
        }
        for (int k = 0; k < vars.length; k++) {
            nume = vars[k].get(StringAttr.VarName);
            val = vars[k].get(GRB.DoubleAttr.X);
            // System.out.println("val = "+ val);
            if (Math.abs(val) > precision_c) {
                i = Integer.valueOf(nume.substring(nume.indexOf('x') + 1, nume.indexOf('y')));
                j = Integer.valueOf(nume.substring(nume.indexOf('y') + 1));
                if (i != j) {
                    if (i < m) {
                        s_list[i].add(j);
                    } else {
                        next[i] = j;
                    }
                }
            }
        }

        for (int h = 0; h < m; h++) {
            if (!s_list[h].isEmpty()) {
                for (int p = 0; p < s_list[h].size(); p++) {
                    from = h;
                    to = s_list[h].get(p);
                    while (next[to] != -1) {
                        to = next[to];
                    }
                    if (to != h) {
                        from = h;
                        to1 = s_list[h].get(p);
                        to = s_list[h].get(p);
                        while (next[to] != -1) {
                            from = to;
                            to = next[to];
                        }
                        if (corrections_list[m * h + to] == null) {
                            corrections_list[m * h + to] = new ArrayList<subPathVars>();
                        }
                        corrections_list[m * h + to].add(new subPathVars(to1, from));
                        graph[h][to]++;
                    } else {
                        // s_list[h].remove(p);
                        // System.out.println(" removed");
                    }
                }
            }
        }
        return corrections_list;
    }

    public static int[][] graphSubtours(GRBModel model) throws GRBException {
        GRBVar[] vars = model.getVars();
        double val = 0;
        int i = 0, j = 0, from = -1, to = -1, constrLength;
        int[] next = new int[n + m];
        int[][] graph = new int[m][m];
        String nume = "";
        List<Integer>[] s_list = new ArrayList[m];

        for (int h = 0; h < n + m; h++) {
            next[h] = -1;
        }

        for (int h = 0; h < m; h++) {
            s_list[h] = new ArrayList<Integer>();
        }

        for (int k = 0; k < vars.length; k++) {
            nume = vars[k].get(StringAttr.VarName);
            val = vars[k].get(GRB.DoubleAttr.X);
            if (Math.abs(val) > precision_c) {
                i = Integer.valueOf(nume.substring(nume.indexOf('x') + 1, nume.indexOf('y')));
                j = Integer.valueOf(nume.substring(nume.indexOf('y') + 1));
                if (i != j) {
                    if (i < m) {
                        s_list[i].add(j);
                    } else {
                        next[i] = j;
                    }
                }
            }
        }
        for (int h = 0; h < m; h++) {
            if (!s_list[h].isEmpty()) {
                for (int p = 0; p < s_list[h].size(); p++) {
                    from = h;
                    to = s_list[h].get(p);
                    while (next[to] != -1) {
                        to = next[to];
                    }
                    if (h != to) {
                        graph[h][to]++;
                    }
                }
            }
        }
        return graph;
    }

    public static int[][] graphSubtours() throws GRBException {
        GRBVar[] vars = model.getVars();
        double val = 0;
        int i = 0, j = 0, from = -1, to = -1, constrLength;
        int[] next = new int[n + m];
        int[][] graph = new int[m][m];
        String nume = "";
        List<Integer>[] s_list = new ArrayList[m];

        for (int h = 0; h < n + m; h++) {
            next[h] = -1;
        }

        for (int h = 0; h < m; h++) {
            s_list[h] = new ArrayList<Integer>();
        }

        for (int k = 0; k < vars.length; k++) {
            nume = vars[k].get(StringAttr.VarName);
            val = vars[k].get(GRB.DoubleAttr.X);
            if (Math.abs(val) > precision_c) {
                i = Integer.valueOf(nume.substring(nume.indexOf('x') + 1, nume.indexOf('y')));
                j = Integer.valueOf(nume.substring(nume.indexOf('y') + 1));
                if (i != j) {
                    if (i < m) {
                        s_list[i].add(j);
                    } else {
                        next[i] = j;
                    }
                }
            }
        }
        for (int h = 0; h < m; h++) {
            if (!s_list[h].isEmpty()) {
                for (int p = 0; p < s_list[h].size(); p++) {
                    from = h;
                    to = s_list[h].get(p);
                    while (next[to] != -1) {
                        to = next[to];
                    }
                    if (h != to) {
                        graph[h][to]++;
                    }
                }
            }
        }
        return graph;
    }

    public static ArrayList<subPathVars> checkValidity(GRBModel model) throws GRBException {
        int h, k, nr_vars;
        String nume;
        int[] verify_in = new int[n + m];
        int[] verify_out = new int[n + m];
        ArrayList<subPathVars> list = new ArrayList<subPathVars>();
        subPathVars var;
        GRBVar[] varr = model.getVars();

        nr_vars = model.get(GRB.IntAttr.NumVars);

        for (int j = 0; j < n + m; j++) {
            verify_in[j] = 0;
            verify_out[j] = 0;
        }

        for (int j = 0; j < nr_vars; j++) {
            if (Math.abs(varr[j].get(GRB.DoubleAttr.X)) > precision_c) {
                nume = varr[j].get(StringAttr.VarName);

                h = Integer.valueOf(nume.substring(nume.indexOf('x') + 1, nume.indexOf('y')));
                k = Integer.valueOf(nume.substring(nume.indexOf('y') + 1));
                System.out.println("tip: " + varr[j].get(GRB.CharAttr.VType) + "variabila " + h + ", " + k
                        + ", valoare = " + varr[j].get(GRB.DoubleAttr.X));
                var = new subPathVars(h, k);
                verify_out[h]++;
                verify_in[k]++;
                list.add(var);
            }
        }

        for (int j = 0; j < n + m; j++) {
            if (verify_in[j] != verify_out[j]) {
                System.out.println(" *** SOLUTIE GRESITA *** ");
                System.out.println("vertex " + j + " in = " + verify_in[j]);
                System.out.println("vertex " + j + "out = " + verify_out[j]);
            }
        }
        System.out.println(" *** SOLUTIE CORECTA *** ");
        return list;
    }

    public static ArrayList<subPathVars> checkValidity() throws GRBException {
        int h, k, nr_vars;
        String nume;
        int[] verify_in = new int[n + m];
        int[] verify_out = new int[n + m];
        ArrayList<subPathVars> list = new ArrayList<subPathVars>();
        subPathVars var;
        GRBVar[] varr = model.getVars();

        nr_vars = model.get(GRB.IntAttr.NumVars);

        for (int j = 0; j < n + m; j++) {
            verify_in[j] = 0;
            verify_out[j] = 0;
        }

        for (int j = 0; j < nr_vars; j++) {
            if (Math.abs(varr[j].get(GRB.DoubleAttr.X)) > precision_c) {
                nume = varr[j].get(StringAttr.VarName);
                h = Integer.valueOf(nume.substring(nume.indexOf('x') + 1, nume.indexOf('y')));
                k = Integer.valueOf(nume.substring(nume.indexOf('y') + 1));
                // System.out.println("variabila " + h + ", " + k + ", valoare = " +
                // varr[j].get(GRB.DoubleAttr.X));
                var = new subPathVars(h, k);
                verify_out[h]++;
                verify_in[k]++;
                list.add(var);
            }
        }

        for (int j = 0; j < n + m; j++) {
            if (verify_in[j] != verify_out[j]) {
                System.out.println(" *** SOLUTIE GRESITA *** ");
                System.out.println("vertex " + j + " in = " + verify_in[j]);
                System.out.println("vertex " + j + "out = " + verify_out[j]);
            }
        }
        System.out.println(" *** SOLUTIE CORECTA *** ");
        return list;
    }

    public static void solutionToFile(String dataFile) throws GRBException {
        if (dataFile == "-1") {
            System.out.println("Usage: input filenamebranchBound(\"m8n1000s4.inp\", 10, 0.0, 2);");
            System.exit(1);
        }
        int h, k;
        ///////////
        String lp_sol_fileName = "../data/results/sol_" + dataFile.substring(0, dataFile.length() - 3) + "txt";
        String nume;
        File lp_sol_file = new File(lp_sol_fileName);
        BufferedWriter writer = null;
        try {
            if (!lp_sol_file.exists()) {
                lp_sol_file.createNewFile();
            }
            FileWriter fw = new FileWriter(lp_sol_file);
            writer = new BufferedWriter(fw);

            GRBVar[] varr = model.getVars();
            int nr_vars = model.get(GRB.IntAttr.NumVars);
            for (int j = 0; j < nr_vars; j++) {
                if (varr[j].get(GRB.DoubleAttr.X) > 0) {
                    // writer.write(varr[j].get(StringAttr.VarName) + " = " +
                    // varr[j].get(GRB.DoubleAttr.X));
                    nume = varr[j].get(StringAttr.VarName);
                    h = Integer.valueOf(nume.substring(nume.indexOf('x') + 1, nume.indexOf('y')));
                    k = Integer.valueOf(nume.substring(nume.indexOf('y') + 1));
                    writer.write(h + "\t" + k + "\t" + varr[j].get(GRB.DoubleAttr.X));
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            System.out.println("Error code: " + e.getMessage());
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
            }
        }
    }

    public static void repairHeuristicBellman1(String dataFile, double gap) {
        // Bellman Princeton's algorithm
        if (dataFile == "-1") {
            System.out.println("Usage: input filename");
            System.exit(1);
        }
        double opt = 0, best_int_opt = 0, kn_opt = 0;
        String dataFileInt1 = "Int1_" + dataFile;
        String lpDataFile = convert(dataFile, "");
        kn_opt = instances[inst][(int) (0.75 * m - 3.0 + n / 500.0 - 1.0)];
        ArrayList<subPathVars>[] correctd_list;

        try {
            env = new GRBEnv();
            model = new GRBModel(env, "../data/results/" + lpDataFile);
            model.set(IntParam.OutputFlag, 0);
            model.update();

            // solving the relaxed problem:
            model.optimize();
            solutionToFile(dataFile + "_a");

            // solved with the equivalent column generation method:
            bellmanDigraph1();
            model.update();
            if (gap == 0.0) {
                model.set(DoubleParam.MIPGapAbs, 0);
            }
            model.set(IntParam.OutputFlag, 0);
            model.optimize();

            // solve the integer problem:
            // model.set(IntParam.OutputFlag, 1);
            long start = System.nanoTime();
            addIntegerConstraints();
            model.update();
            if (gap == 0.0) {
                model.set(DoubleParam.MIPGapAbs, 0);
            }
            model.optimize();
            System.out.println("MILP optimization time = " + (System.nanoTime() - start) * 1e-9);
            start = System.nanoTime();
            correctd_list = repairSubtours();
            solutionToFile(dataFileInt1);
            opt = model.get(GRB.DoubleAttr.ObjVal);
            best_int_opt = opt + solRepair(dataFile, checkValidity(), correctd_list, graphSubtours());
            System.out.println(
                    "found=" + best_int_opt + "/known=" + kn_opt + "/rel_error=" + (best_int_opt - kn_opt) / kn_opt);
            // TODO: save this integer solution
            System.out.println("Repairing time = " + (System.nanoTime() - start) * 1e-9);
            verify();
        } catch (GRBException e) {
            e.printStackTrace();
            System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
        }
    }

    public static void repairHeuristicBellman2(String dataFile, double gap, int threads) {
        // Bellman Princeton's modified algorithm
        if (dataFile == "-1") {
            System.out.println("Usage: input filename");
            System.exit(1);
        }

        double opt = 0, best_int_opt = 0, kn_opt = 0;
        String dataFileInt1 = "Int1_" + dataFile;
        String lpDataFile = convert(dataFile, "");
        // kn_opt = instances[inst][(int) (0.75 * m - 3.0 + n / 500.0 - 1.0)];
        ArrayList<subPathVars>[] correctd_list;

        try {
            env = new GRBEnv();
            model = new GRBModel(env, "../data/results/" + lpDataFile);
            model.set(IntParam.OutputFlag, 0);
            // Threads:
            model.set(GRB.IntParam.Threads, threads);
            model.update();

            // solving the relaxed problem:
            model.optimize();
            solutionToFile(dataFile + "_a");

            // solved with the equivalent column generation method:
            bellmanDigraph2New();
            model.update();
            if (gap == 0.0) {
                model.set(DoubleParam.MIPGapAbs, 0);
            }
            model.set(IntParam.OutputFlag, 0);
            model.optimize();

            // solve the integer problem:
            // model.set(IntParam.OutputFlag, 1);
            long start = System.nanoTime();
            addIntegerConstraints();
            model.update();
            if (gap == 0.0) {
                model.set(DoubleParam.MIPGapAbs, 0);
            }
            model.optimize();
            System.out.println("MILP optimization time = " + (System.nanoTime() - start) * 1e-9);
            start = System.nanoTime();
            correctd_list = repairSubtours();
            solutionToFile(dataFileInt1);
            opt = model.get(GRB.DoubleAttr.ObjVal);
            best_int_opt = opt + solRepair(dataFile, checkValidity(), correctd_list, graphSubtours());
            // System.out.println("found=" + best_int_opt + "/known=" + kn_opt +
            // "/rel_error=" + (best_int_opt - kn_opt) / kn_opt);
            System.out.println("found=" + best_int_opt);
            // TODO: save this integer solution
            System.out.println("Repairing time = " + (System.nanoTime() - start) * 1e-9);
            // verify();
        } catch (GRBException e) {
            e.printStackTrace();
            System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        long start = System.nanoTime();
        // heuristic(args[0], Double.valueOf(args[1]), Integer.valueOf(args[2]));
        // simple_heuristic(args[0], Double.valueOf(args[1]), Integer.valueOf(args[2]));
        // repairHeuristicBellman3("m4n1000s2.inp", 0.0);
        // System.out.println("Bellman execution time 3 = " + (System.nanoTime() -
        // start) * 1e-9);
        // start = System.nanoTime();
        // heuristic(args[0], Double.valueOf(args[1]), Integer.valueOf(args[2]));
        // simple_heuristic(args[0], Double.valueOf(args[1]), Integer.valueOf(args[2]));
        // repairHeuristicBellman2(args[0], Double.valueOf(args[1]),
        // Integer.valueOf(args[2]));
        repairHeuristicBellman2("m4n500s0.inp", 0.0, 8);
        System.out.println("Bellman new execution time 2 = " + (System.nanoTime() - start) * 1e-9);
        // start = System.nanoTime();
        // repairHeuristicDijkstra2("m8n1000s2.inp", 0.0);
        // System.out.println("Dijkstra new execution time 3 = " + (System.nanoTime() -
        // start) * 1e-9);
        // start = System.nanoTime();
        // repairHeuristicBellman1("m8n1500s2.inp", 0.0);
        // System.out.println("Bellman new execution time 1 = " + (System.nanoTime() -
        // start) * 1e-9);
    }
}
