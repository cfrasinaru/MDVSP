package ro.uaic.info.mdvsp.bb;

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
import ro.uaic.info.mdvsp.Config;
import ro.uaic.info.mdvsp.Main;
import ro.uaic.info.mdvsp.Model;
import ro.uaic.info.mdvsp.Solution;
//import gurobi.GRB.StringParam;

public class Model_b_heuristic extends Model {

    public int n, m, inst;
    public int[] next = new int[n + m];
    public double[][] c = new double[n + m][n + m];
    public double[][] sol = new double[n + m][n + m];
    public GRBEnv env;
    public GRBModel model;
    public static final int[][] instances = {{1289114, 2516247, 3830912, 1292411, 2422112, 3500160},
    {1241618, 2413393, 3559176, 1276919, 2524293, 3802650},
    {1283811, 2452905, 3649757, 1304251, 2556313, 3605094},
    {1258634, 2490812, 3406815, 1277838, 2478393, 3515802},
    {1317077, 2519191, 3567122, 1276010, 2498388, 3704953}}; // new int[5][6];
    public static final double precision_a = 1e-9;
    public static final double precision_b = 1e-15;
    public static final double precision_c = 1e-5;
    
    /**
     *
     * @param filename
     */
    public Model_b_heuristic(String filename) throws IOException {
        super(filename);
    }

    /**
     *
     * @param other
     */
    public Model_b_heuristic(Model other) {
        super(other);
    }

    public String convert(String dataFile, String vtype) {
        inst = Integer.parseInt(dataFile.substring(dataFile.length() - 5, dataFile.length() - 4));
        File file = new File(this.dataFile);
        String lp_fileName = "lp_" + name + ".lp";
        File lp_file = new File(Config.getDataPath() + "results/" + lp_fileName);
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
            if (outputEnabled) {
                System.out.println(m + " depots & " + n + " trips.");
            }
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
            if (outputEnabled) {
                System.out.println("End writing to file");
            }

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

    public ArrayList<subPathVars>[] cloneArrayList(ArrayList<subPathVars>[] corrected_list) {
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

    public Paths_arcPaths _Bellman_Digraph() throws GRBException {
        GRBVar[] variabile = model.getVars();
        String nume = "";
        List<Integer>[] path_list = new ArrayList[m];
        int stop = 1, i, j;
        double valoare = 0, current_optimum = 0.0;
        ArrayList<ArrayList<Integer>> list = new ArrayList<ArrayList<Integer>>();
        HashMap<Arc, ArrayList<ArrayList<Integer>>> arc_list = new HashMap<Arc, ArrayList<ArrayList<Integer>>>();
        Paths_arcPaths returned_pair = new Paths_arcPaths();

        if (outputEnabled) {
            System.out.println("Bellman in");
        }
        long start1 = 0, aux1 = System.nanoTime(), start2 = 0, aux2 = System.nanoTime();
        while (stop > 0) {
            // k++;
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
            stop = _BellmanFMSolveAlgEff(graph, list, arc_list, 0);
            aux1 = System.nanoTime() - aux1;
            start1 += aux1;
            if (outputEnabled) {
                System.out.println("Bellman time = " + aux1 * 1e-9);
            }
            aux2 = System.nanoTime();
            model.optimize();
            aux2 = System.nanoTime() - aux2;
            start2 += aux2;
            if (outputEnabled) {
                System.out.println("Gurobi time = " + aux2 * 1e-9);
            }
            // System.out.println("Exec_time = " + (System.nanoTime() - start) * 1e-9);
            // System.out.print(" | " + (k++) + " stop = " + stop + " Obj = " +
            // model.get(GRB.DoubleAttr.ObjVal));
            if (Math.abs(current_optimum - model.get(GRB.DoubleAttr.ObjVal)) < 1.0e-8) {
                if (outputEnabled) {
                    System.out.println("Bellman out");
                }
                current_optimum = model.get(GRB.DoubleAttr.ObjVal);
                if (outputEnabled) {
                    System.out.println("current_optimum = " + current_optimum);
                }
                returned_pair = new Paths_arcPaths(list, arc_list);
                if (outputEnabled) {
                    System.out.println("Bellman final time = " + start1 * 1e-9 + " Gurobi final time = " + start2 * 1e-9);
                }
                return returned_pair;
            }
        }
        return returned_pair;
    }

    public void _Bellman_Digraph1() throws GRBException {// for the simple heuristic
        GRBVar[] variabile = model.getVars();
        String nume = "";
        int stop = 1, i, j;
        double valoare = 0, current_optimum = 0.0, new_optimum = 0;
        ArrayList<ArrayList<Integer>> list = new ArrayList<>();
        HashMap<Arc, ArrayList<ArrayList<Integer>>> arc_list = new HashMap<>();

        if (outputEnabled) {
            System.out.println("Bellman in");
        }
        long start1 = 0, aux1 = System.nanoTime(), start2 = 0, aux2 = System.nanoTime();
        while (stop > 0) {
            // k++;
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
            stop = _BellmanFMSolveAlgEff(graph, list, arc_list, 0);
            aux1 = System.nanoTime() - aux1;
            start1 += aux1;
            if (outputEnabled) {
                System.out.println("Bellman time = " + aux1 * 1e-9);
            }
            aux2 = System.nanoTime();
            model.optimize();
            aux2 = System.nanoTime() - aux2;
            start2 += aux2;
            if (outputEnabled) {
                System.out.println("Gurobi time = " + aux2 * 1e-9);
            }

            new_optimum = model.get(GRB.DoubleAttr.ObjVal);
            if (outputEnabled) {
                System.out.println();
                System.out.println("current optimum = " + current_optimum + "|| new optimum = " + new_optimum);
                System.out.println();
            }
            if (Math.abs(current_optimum - new_optimum) < 1.0e-8) {
                if (outputEnabled) {
                    System.out.println("Bellman out");
                }
                current_optimum = new_optimum;
                // System.out.println("current_optimum = " + current_optimum);
                if (outputEnabled) {
                    System.out.println("Bellman final time = " + start1 * 1e-9 + " Gurobi final time = " + start2 * 1e-9);
                }
                return;
            }
            current_optimum = new_optimum;
        }
        if (outputEnabled) {
            System.out.println("Bellman final time = " + start1 * 1e-9 + " Gurobi final time = " + start2 * 1e-9);
        }
        return;
    }

    public int _BellmanFMSolveAlgEff(EdgeWeightedDigraph graph, ArrayList<ArrayList<Integer>> list, Map arc_list,
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

    public Paths_arcPaths _new_Bellman_Digraph(GRBModel model) throws GRBException {
        GRBVar[] variabile = model.getVars();
        String nume = "";
        List<Integer>[] path_list = new ArrayList[m];
        int stop = 1, i, j, k = 0;
        double valoare = 0, current_optimum = 0.0;
        ArrayList<ArrayList<Integer>> list = new ArrayList<ArrayList<Integer>>();
        HashMap<Arc, ArrayList<ArrayList<Integer>>> arc_list = new HashMap<Arc, ArrayList<ArrayList<Integer>>>();
        Paths_arcPaths returned_pair = new Paths_arcPaths();

        if (outputEnabled) {
            System.out.println("Bellman in");
        }
        long start = System.nanoTime();
        while (stop > 0) {
            k++;
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
            start = System.nanoTime();
            stop = _new_BellmanFMSolveAlgEff(model, graph, list, arc_list, 0);
            if (outputEnabled) {
                System.out.println("Bellman exec_time = " + (System.nanoTime() - start) * 1e-9);
            }
            // System.out.println(stop + " drumuri");
            System.nanoTime();
            model.optimize();
            if (outputEnabled) {
                System.out.println("Gurobi exec_time = " + (System.nanoTime() - start) * 1e-9);
            }
            // System.out.print(" | " + (k++) + " stop = " + stop + " Obj = " +
            // model.get(GRB.DoubleAttr.ObjVal));
            if (Math.abs(current_optimum - model.get(GRB.DoubleAttr.ObjVal)) < 1.0e-8) {
                if (outputEnabled) {
                    System.out.println("Bellman out");
                }
                current_optimum = model.get(GRB.DoubleAttr.ObjVal);
                // System.out.println("current_optimum = " + current_optimum);
                returned_pair = new Paths_arcPaths(list, arc_list);
                return returned_pair;
            }
            current_optimum = model.get(GRB.DoubleAttr.ObjVal);
            // System.out.println("current_optimum = " + current_optimum);
        }
        return returned_pair;
    }

    public int _new_BellmanFMSolveAlgEff(GRBModel model, EdgeWeightedDigraph graph,
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

    public double solRepair(String dataFile, ArrayList<subPathVars> vars,
            ArrayList<subPathVars>[] corrections_list, int[][] graph, int repairType) {
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

                if (repairType == 20) {
                    sum += cycleCorrections2_0(vars, corrections_list, path);// corrections along the
                }
            }
        }
        return sum;
    }

    public void printReturned_pair(Paths_arcPaths returned_pair) {
        Iterator it1 = returned_pair.arc_list.entrySet().iterator();
        while (it1.hasNext()) {
            Map.Entry pair = (Map.Entry) it1.next();
            if (outputEnabled) {
                System.out.println(pair.getKey() + " = " + pair.getValue());
            }
            it1.remove(); // avoids a ConcurrentModificationException
        }

        if (outputEnabled) {
            System.out.println("**********************************");
        }
        Iterator<ArrayList<Integer>> it2 = returned_pair.path_list.iterator();
        while (it2.hasNext()) {
            ArrayList<Integer> path = (ArrayList<Integer>) it2.next();
            if (outputEnabled) {
                System.out.println(path.toString());
            }
            it2.remove(); // avoids a ConcurrentModificationException
        }
    }

    public double cycleCorrections2_0(ArrayList<subPathVars> vars, ArrayList<subPathVars>[] corrections_list,
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
                if (outputEnabled) {
                    System.out.println("Negativ +: " + c[trip1][i] + " Negativ -: " + c[trip1][j]);
                }
            }
            sum1 += c[trip1][i] - c[trip1][j];
            i = path.get(p);
            j = path.get(p + 1);
        }

        trip1 = corrections_list[m * i + j].get(0).to();
        if (c[trip1][i] < 0 || c[trip1][j] < 0) {
            if (outputEnabled) {
                System.out.println("Negativ +: " + c[trip1][i] + " Negativ -: " + c[trip1][j]);
            }
        }
        sum1 += c[trip1][i] - c[trip1][j];

        // second type of repair
        i = path.get(path.size() - 1);
        j = path.get(0);
        for (int p = 0; p <= path.size() - 2; p++) {
            trip2 = corrections_list[m * i + j].get(0).from();
            if (c[j][trip2] < 0 || c[i][trip2] < 0) {
                if (outputEnabled) {
                    System.out.println("Negativ +: " + c[j][trip2] + " Negativ -: " + c[i][trip2]);
                }
            }
            sum2 += c[j][trip2] - c[i][trip2];
            i = path.get(p);
            j = path.get(p + 1);
        }

        trip2 = corrections_list[m * i + j].get(0).from();
        if (c[j][trip2] < 0 || c[i][trip2] < 0) {
            if (outputEnabled) {
                System.out.println("Negativ +: " + c[j][trip2] + " Negativ -: " + c[i][trip2]);
            }
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

    public double[][] verify(GRBModel model) {
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
                        if (outputEnabled) {
                            System.out.println(" negativ: " + c[h][k]);
                        }
                    }
                    suma += valoare * c[h][k];
                }
            }
            if (outputEnabled) {
                System.out.println();
                System.out.println("suma = " + suma + " optim = " + model.get(GRB.DoubleAttr.ObjVal));
            }
        } catch (GRBException e) {
            System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
        }
        return c;
    }

    public double[][] verify() {
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
                    if (outputEnabled) {
                        if (c[h][k] < 0) {
                            System.out.println(" negativ: " + c[h][k]);
                        }
                    }
                    suma += valoare * c[h][k];
                }
            }
            if (outputEnabled) {
                System.out.println();
                System.out.println("suma = " + suma + " optim = " + model.get(GRB.DoubleAttr.ObjVal));
            }
        } catch (GRBException e) {
            System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
        }
        return c;
    }

    public GRBVar checkIntegrality(GRBModel model) throws GRBException {

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

    public void addIntegerConstraints() throws GRBException {
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

    public void addIntegerConstraints(GRBModel model) throws GRBException {
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

    public void addContinuousConstraints() throws GRBException {
        GRBVar[] variabile = model.getVars();
        for (int p = 0; p < variabile.length; p++) {
            variabile[p].set(GRB.CharAttr.VType, GRB.CONTINUOUS);
        }
        model.update();
    }

    public void addContinuousConstraints(GRBModel model) throws GRBException {
        GRBVar[] variabile = model.getVars();
        for (int p = 0; p < variabile.length; p++) {
            variabile[p].set(GRB.CharAttr.VType, GRB.CONTINUOUS);
        }
        model.update();
    }

    public boolean displaySubtours(GRBModel model, GRBVar[] vars, int m, int n) throws GRBException {

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

    public void showSubtours(GRBModel model, GRBVar[] vars, int m, int n) throws GRBException {

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
                if (outputEnabled) {
                    System.out.println("Subtour:");
                }
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
                    if (outputEnabled) {
                        System.out.println();
                    }
                }
            }
        }
    }

    public ArrayList<subPathVars>[] repairSubtours(GRBModel model) throws GRBException {
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

    public ArrayList<subPathVars>[] repairSubtours() throws GRBException {
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

    public int[][] graphSubtours(GRBModel model) throws GRBException {
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

    public int[][] graphSubtours() throws GRBException {
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

    public ArrayList<subPathVars> checkValidity(GRBModel model) throws GRBException {
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
                if (outputEnabled) {
                    System.out.println("tip: " + varr[j].get(GRB.CharAttr.VType) + "variabila " + h + ", " + k
                            + ", valoare = " + varr[j].get(GRB.DoubleAttr.X));
                }
                var = new subPathVars(h, k);
                verify_out[h]++;
                verify_in[k]++;
                list.add(var);
            }
        }

        if (outputEnabled) {
            for (int j = 0; j < n + m; j++) {
                if (verify_in[j] != verify_out[j]) {
                    System.out.println(" *** SOLUTIE GRESITA *** ");
                    System.out.println("vertex " + j + " in = " + verify_in[j]);
                    System.out.println("vertex " + j + "out = " + verify_out[j]);
                }
            }
            System.out.println(" *** SOLUTIE CORECTA *** ");
        }
        return list;
    }

    public ArrayList<subPathVars> checkValidity() throws GRBException {
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

        if (outputEnabled) {
            for (int j = 0; j < n + m; j++) {
                if (verify_in[j] != verify_out[j]) {
                    System.out.println(" *** SOLUTIE GRESITA *** ");
                    System.out.println("vertex " + j + " in = " + verify_in[j]);
                    System.out.println("vertex " + j + "out = " + verify_out[j]);
                }
            }
            System.out.println(" *** SOLUTIE CORECTA *** ");
        }
        return list;
    }

    public void solutionToFile(String dataFile) throws GRBException {
        if (dataFile == "-1") {
            System.out.println("Usage: input filenamebranchBound(\"m8n1000s4.inp\", 10, 0.0, 2);");
            System.exit(1);
        }
        int h, k;
        ///////////
        String lp_sol_fileName = Config.getDataPath() + "results/sol_" + name + ".txt";
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

    public GRBModel _buildCurrentModel(GRBModel model, PathsConstrVars _pvc_Obj) throws GRBException {
        GRBModel current_model = new GRBModel(model);
        Arc arc = new Arc();
        if (_pvc_Obj != null) {
            GRBLinExpr expr = new GRBLinExpr();
            GRBVar _var2BSet = null, var_to_constr = null;
            int from, to, constrLength;

            // set the variables:
            if (_pvc_Obj.var2BSet_0 != null) {
                Iterator<Arc> iterator_arc = _pvc_Obj.var2BSet_0.iterator();
                while (iterator_arc.hasNext()) {
                    arc = (Arc) iterator_arc.next();
                    _var2BSet = current_model.getVarByName("x" + arc.from + "y" + arc.to);
                    current_model.getVarByName(_var2BSet.get(StringAttr.VarName)).set(GRB.DoubleAttr.UB, 0.0);
                    current_model.getVarByName(_var2BSet.get(StringAttr.VarName)).set(GRB.DoubleAttr.LB, 0.0);
                    /*
					 * expr = new GRBLinExpr(); expr.addTerm(1.0, _var2BSet);
					 * current_model.addConstr(expr, GRB.LESS_EQUAL, 0.0, "");
                     */
                    // System.out.println("Variabila setata la 0: " + arc.toString());
                    // iterator_arc.remove(); // avoids a ConcurrentModificationException
                }
            }
            // var_list = new ArrayList<Arc>(stack_pathConstr_vars2BeSet.var2BeSet_list_1);
            if (_pvc_Obj.var2BSet_1 != null) {
                Iterator<Arc> iterator_arc = _pvc_Obj.var2BSet_1.iterator();
                while (iterator_arc.hasNext()) {
                    arc = (Arc) iterator_arc.next();
                    _var2BSet = current_model.getVarByName("x" + arc.from + "y" + arc.to);
                    current_model.getVarByName(_var2BSet.get(StringAttr.VarName)).set(GRB.DoubleAttr.LB, 1.0);
                    current_model.getVarByName(_var2BSet.get(StringAttr.VarName)).set(GRB.DoubleAttr.UB, 1.0);
                    /*
					 * expr = new GRBLinExpr(); expr.addTerm(1.0, _var2BSet);
					 * current_model.addConstr(expr, GRB.GREATER_EQUAL, 1.0, "");
                     */
                    // System.out.println("Variabila setata la 1: " + arc.toString());
                    // iterator_arc.remove(); // avoids a ConcurrentModificationException
                }
            }
            // add the constraints:
            ArrayList<ArrayList<Integer>> pathConstraints_list = _pvc_Obj.pathConstr_list;

            if (_pvc_Obj.pathConstr_list != null) {
                Iterator<ArrayList<Integer>> iterator_path = _pvc_Obj.pathConstr_list.iterator();

                while (iterator_path.hasNext()) {
                    ArrayList<Integer> path = (ArrayList<Integer>) iterator_path.next();
                    if (path != null) {
                        expr = new GRBLinExpr();
                        constrLength = 0;
                        from = path.get(0);
                        for (int h = 1; h < path.size(); h++) {
                            to = path.get(h);
                            constrLength++;
                            var_to_constr = current_model.getVarByName("x" + from + "y" + to);
                            expr.addTerm(1.0, var_to_constr);
                            from = to;
                        }
                        // current_model.getVarByName(_var2BSet.get(StringAttr.VarName)).set(GRB.DoubleAttr.LB,
                        // 1.0);
                        // iterator_path.remove(); // avoids a ConcurrentModificationException
                        current_model.addConstr(expr, GRB.LESS_EQUAL, constrLength - 1, "");
                    }

                }
            }
        }
        current_model.update();
        return current_model;

    }

    public void branchBound(String dataFile, int n_max, double gap, int repairType) {
        // Perform a Branch&Bound algorithm
        if (dataFile == "-1") {
            System.out.println("Usage: input filename");
            System.exit(1);
        }
        int nr_problems = 1, i, j;
        double opt_min = 1e20, opt = 0, best_int_opt = 0, kn_opt = 0, current_opt = 0.0;
        String var_name, dataFileInt1 = "Int1_" + dataFile;
        String lpDataFile = convert(dataFile, "");
        kn_opt = instances[inst][(int) (0.75 * m - 3.0 + n / 500.0 - 1.0)];
        ArrayList<subPathVars>[] correctd_list;
        Paths_arcPaths bell_ret;

        boolean found = false;
        try {
            Stack<PathsConstrVars> stiva = new Stack<PathsConstrVars>();
            env = new GRBEnv();
            GRBVar branch_var;
            GRBModel root_model, current_model = new GRBModel(env), best_model = new GRBModel(env),
                    best_int_model = new GRBModel(env);
            model = new GRBModel(env, Config.getDataPath() + "results/" + lpDataFile);
            model.set(IntParam.OutputFlag, 0);
            model.update();
            root_model = new GRBModel(model);

            // solving the root problem:
            model.optimize();// solved with the equivalent column generation method:
            solutionToFile(dataFile + "_a");
            bell_ret = _Bellman_Digraph();

            // printReturned_pair(returned_pair);
            model.update();
            if (gap == 0.0) {
                model.set(DoubleParam.MIPGapAbs, 0);
                model.set(GRB.DoubleParam.MIPGap, 0);
            }
            model.set(IntParam.OutputFlag, 0);
            model.optimize();

            // find an upper bound for the optimum in the MILP problem (minimization):
            addIntegerConstraints();
            model.update();
            if (gap == 0.0) {
                model.set(DoubleParam.MIPGapAbs, 0);
                model.set(DoubleParam.MIPGap, 0);
            }
            model.optimize();
            correctd_list = repairSubtours();
            solutionToFile(dataFileInt1);
            opt = model.get(GRB.DoubleAttr.ObjVal);
            best_int_opt = opt + solRepair(dataFile, checkValidity(), correctd_list, graphSubtours(), repairType);
            if (outputEnabled) {
                System.out.println(
                        "found=" + best_int_opt + "/known=" + kn_opt + "/rel_error=" + (best_int_opt - kn_opt) / kn_opt);
            }
            // TODO: save this integer solution
            verify();

            PathsConstrVars _pcvObj = new PathsConstrVars(null, null, null, null);
            stiva.add(_pcvObj);

            while (!stiva.isEmpty() && nr_problems < n_max && !found) {
                _pcvObj = stiva.pop();

                // build the model for the current problem in the stack:
                current_model = _buildCurrentModel(root_model, _pcvObj);
                addContinuousConstraints(current_model);
                current_model.update();
                current_model.optimize(); // Bellman_Digraph(current_model, m, n); //
                current_opt = current_model.get(GRB.DoubleAttr.ObjVal);
                if (outputEnabled) {
                    System.out.println("Current_opt before = " + current_opt);
                }
                bell_ret = _new_Bellman_Digraph(current_model);
                current_opt = current_model.get(GRB.DoubleAttr.ObjVal);
                if (current_opt < opt_min) {
                    opt_min = current_opt;
                    best_model = new GRBModel(current_model);
                }
                _pcvObj = new PathsConstrVars(bell_ret.arc_list, bell_ret.path_list, _pcvObj.var2BSet_0,
                        _pcvObj.var2BSet_1);

                if (outputEnabled) {
                    System.out.println("probl. " + nr_problems + "|optim = " + current_opt);
                }
                nr_problems++;
                int optStat = current_model.get(GRB.IntAttr.Status);
                if (optStat != GRB.Status.INFEASIBLE && optStat != GRB.Status.UNBOUNDED
                        && (best_int_opt > current_opt)) {
                    branch_var = checkIntegrality(current_model);
                    if (branch_var != null) {
                        var_name = branch_var.get(StringAttr.VarName);
                        i = Integer.valueOf(var_name.substring(var_name.indexOf('x') + 1, var_name.indexOf('y')));
                        j = Integer.valueOf(var_name.substring(var_name.indexOf('y') + 1));
                        System.out.println("i = " + i + "j = " + j + ", val = " + branch_var.get(GRB.DoubleAttr.X));

                        if (nr_problems % 2 == 0) {
                            // the first problem is added to the stack:
                            ArrayList<Arc> var_list_1;
                            if (_pcvObj.var2BSet_1 != null) {
                                var_list_1 = new ArrayList<Arc>(_pcvObj.var2BSet_1);
                            } else {
                                var_list_1 = new ArrayList<Arc>();
                            }
                            var_list_1.add(new Arc(i, j));
                            PathsConstrVars stack_pathConstr_vars2BeSet_1 = new PathsConstrVars(bell_ret.arc_list,
                                    bell_ret.path_list, _pcvObj.var2BSet_0, var_list_1);
                            stiva.add(stack_pathConstr_vars2BeSet_1);

                            // the second problem is added to the stack:
                            ArrayList<Arc> var_list_0;
                            if (_pcvObj.var2BSet_0 != null) {
                                var_list_0 = new ArrayList<Arc>(_pcvObj.var2BSet_0);
                            } else {
                                var_list_0 = new ArrayList<Arc>();
                            }
                            var_list_0.add(new Arc(i, j));
                            PathsConstrVars stack_pathConstr_vars2BeSet_0 = new PathsConstrVars(bell_ret.arc_list,
                                    bell_ret.path_list, var_list_0, _pcvObj.var2BSet_1, i, j);
                            stiva.add(stack_pathConstr_vars2BeSet_0);
                        } else {
                            // the second problem is added to the stack:
                            ArrayList<Arc> var_list_0;
                            if (_pcvObj.var2BSet_0 != null) {
                                var_list_0 = new ArrayList<Arc>(_pcvObj.var2BSet_0);
                            } else {
                                var_list_0 = new ArrayList<Arc>();
                            }
                            var_list_0.add(new Arc(i, j));
                            PathsConstrVars stack_pathConstr_vars2BeSet_0 = new PathsConstrVars(bell_ret.arc_list,
                                    bell_ret.path_list, var_list_0, _pcvObj.var2BSet_1, i, j);
                            stiva.add(stack_pathConstr_vars2BeSet_0);

                            // the first problem is added to the stack:
                            ArrayList<Arc> var_list_1;
                            if (_pcvObj.var2BSet_1 != null) {
                                var_list_1 = new ArrayList<Arc>(_pcvObj.var2BSet_1);
                            } else {
                                var_list_1 = new ArrayList<Arc>();
                            }
                            var_list_1.add(new Arc(i, j));
                            PathsConstrVars stack_pathConstr_vars2BeSet_1 = new PathsConstrVars(bell_ret.arc_list,
                                    bell_ret.path_list, _pcvObj.var2BSet_0, var_list_1);
                            stiva.add(stack_pathConstr_vars2BeSet_1);
                        }
                    } else {
                        System.out.println("Fathomed by integrality");
                        found = true;
                        best_int_model = current_model;
                        best_int_opt = current_model.get(GRB.DoubleAttr.ObjVal);
                    }
                }
            }

            System.out.println("Final integer solution");
            addIntegerConstraints(current_model);
            current_model.update();
            if (gap == 0.0) {
                current_model.set(DoubleParam.MIPGapAbs, 0);
            }
            current_model.optimize();
            correctd_list = repairSubtours(current_model);
            // solutionToFile(dataFileInt1);
            System.out.println();
            opt = current_model.get(GRB.DoubleAttr.ObjVal);
            best_int_opt = opt + solRepair(dataFile, checkValidity(current_model), correctd_list,
                    graphSubtours(current_model), repairType);
            verify(current_model);
            if (outputEnabled) {
                System.out.println(
                        "found=" + best_int_opt + "/known=" + kn_opt + "/rel_error=" + (best_int_opt - kn_opt) / kn_opt);
            }
        } catch (GRBException e) {
            System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
        }
    }

    public void branchBound1(String dataFile, int n_max, double gap, int repairType) {
        // Perform a Branch&Bound algorithm
        if (dataFile == "-1") {
            System.out.println("Usage: input filename");
            System.exit(1);
        }
        int nr_problems = 1, i, j;
        double opt_min = 1e20, opt = 0, best_int_opt = 0, kn_opt = 0, current_opt = 0.0;
        String var_name, dataFileInt1 = "Int1_" + dataFile;
        String lpDataFile = convert(dataFile, "");
        kn_opt = instances[inst][(int) (0.75 * m - 3.0 + n / 500.0 - 1.0)];
        ArrayList<subPathVars>[] correctd_list;
        Paths_arcPaths bell_ret;

        boolean found = false;
        try {
            Stack<PathsConstrVars> stiva = new Stack<PathsConstrVars>();
            env = new GRBEnv();
            GRBVar branch_var;
            GRBModel root_model, current_model = new GRBModel(env), best_model = new GRBModel(env),
                    best_int_model = new GRBModel(env);
            model = new GRBModel(env, Config.getDataPath() + "results/" + lpDataFile);
            model.set(IntParam.OutputFlag, 0);
            model.update();
            root_model = new GRBModel(model);

            // solving the root problem:
            model.optimize();// solved with the equivalent column generation method:
            solutionToFile(dataFile + "_a");
            bell_ret = _Bellman_Digraph();

            // printReturned_pair(returned_pair);
            model.update();
            if (gap == 0.0) {
                model.set(DoubleParam.MIPGapAbs, 0);
            }
            model.set(IntParam.OutputFlag, 0);
            model.optimize();

            // find an upper bound for the optimum in the MILP problem (minimization):
            addIntegerConstraints();
            model.update();
            if (gap == 0.0) {
                model.set(DoubleParam.MIPGapAbs, 0);
            }
            model.optimize();
            correctd_list = repairSubtours();
            solutionToFile(dataFileInt1);
            opt = model.get(GRB.DoubleAttr.ObjVal);
            best_int_opt = opt + solRepair(dataFile, checkValidity(), correctd_list, graphSubtours(), repairType);
            System.out.println(
                    "found=" + best_int_opt + "/known=" + kn_opt + "/rel_error=" + (best_int_opt - kn_opt) / kn_opt);
            // TODO: save this integer solution
            verify();

            PathsConstrVars _pcvObj = new PathsConstrVars(null, null, null, null);
            stiva.add(_pcvObj);

            while (!stiva.isEmpty() && nr_problems < n_max && !found) {
                _pcvObj = stiva.pop();

                // build the model for the current problem in the stack:
                current_model = _buildCurrentModel(root_model, _pcvObj);
                addContinuousConstraints(current_model);
                current_model.update();
                current_model.optimize(); // Bellman_Digraph(current_model, m, n); //
                current_opt = current_model.get(GRB.DoubleAttr.ObjVal);
                if (outputEnabled) {
                    System.out.println("Current_opt before = " + current_opt);
                }
                bell_ret = _new_Bellman_Digraph(current_model);
                current_opt = current_model.get(GRB.DoubleAttr.ObjVal);
                if (current_opt < opt_min) {
                    opt_min = current_opt;
                    best_model = new GRBModel(current_model);
                }
                _pcvObj = new PathsConstrVars(bell_ret.arc_list, bell_ret.path_list, _pcvObj.var2BSet_0,
                        _pcvObj.var2BSet_1);

                if (outputEnabled) {
                    System.out.println("probl. " + nr_problems + "|optim = " + current_opt);
                }
                nr_problems++;
                int optStat = current_model.get(GRB.IntAttr.Status);
                if (optStat != GRB.Status.INFEASIBLE && optStat != GRB.Status.UNBOUNDED
                        && (best_int_opt > current_opt)) {
                    branch_var = checkIntegrality(current_model);
                    if (branch_var != null) {
                        var_name = branch_var.get(StringAttr.VarName);
                        i = Integer.valueOf(var_name.substring(var_name.indexOf('x') + 1, var_name.indexOf('y')));
                        j = Integer.valueOf(var_name.substring(var_name.indexOf('y') + 1));
                        System.out.println("i = " + i + "j = " + j + ", val = " + branch_var.get(GRB.DoubleAttr.X));

                        if (nr_problems % 2 == 0) {
                            // the first problem is added to the stack:
                            ArrayList<Arc> var_list_1;
                            if (_pcvObj.var2BSet_1 != null) {
                                var_list_1 = new ArrayList<Arc>(_pcvObj.var2BSet_1);
                            } else {
                                var_list_1 = new ArrayList<Arc>();
                            }
                            var_list_1.add(new Arc(i, j));
                            PathsConstrVars stack_pathConstr_vars2BeSet_1 = new PathsConstrVars(bell_ret.arc_list,
                                    bell_ret.path_list, _pcvObj.var2BSet_0, var_list_1);
                            stiva.add(stack_pathConstr_vars2BeSet_1);

                            // the second problem is added to the stack:
                            ArrayList<Arc> var_list_0;
                            if (_pcvObj.var2BSet_0 != null) {
                                var_list_0 = new ArrayList<Arc>(_pcvObj.var2BSet_0);
                            } else {
                                var_list_0 = new ArrayList<Arc>();
                            }
                            var_list_0.add(new Arc(i, j));
                            PathsConstrVars stack_pathConstr_vars2BeSet_0 = new PathsConstrVars(bell_ret.arc_list,
                                    bell_ret.path_list, var_list_0, _pcvObj.var2BSet_1, i, j);
                            stiva.add(stack_pathConstr_vars2BeSet_0);
                        } else {
                            // the second problem is added to the stack:
                            ArrayList<Arc> var_list_0;
                            if (_pcvObj.var2BSet_0 != null) {
                                var_list_0 = new ArrayList<Arc>(_pcvObj.var2BSet_0);
                            } else {
                                var_list_0 = new ArrayList<Arc>();
                            }
                            var_list_0.add(new Arc(i, j));
                            PathsConstrVars stack_pathConstr_vars2BeSet_0 = new PathsConstrVars(bell_ret.arc_list,
                                    bell_ret.path_list, var_list_0, _pcvObj.var2BSet_1, i, j);
                            stiva.add(stack_pathConstr_vars2BeSet_0);

                            // the first problem is added to the stack:
                            ArrayList<Arc> var_list_1;
                            if (_pcvObj.var2BSet_1 != null) {
                                var_list_1 = new ArrayList<Arc>(_pcvObj.var2BSet_1);
                            } else {
                                var_list_1 = new ArrayList<Arc>();
                            }
                            var_list_1.add(new Arc(i, j));
                            PathsConstrVars stack_pathConstr_vars2BeSet_1 = new PathsConstrVars(bell_ret.arc_list,
                                    bell_ret.path_list, _pcvObj.var2BSet_0, var_list_1);
                            stiva.add(stack_pathConstr_vars2BeSet_1);
                        }
                    } else {
                        System.out.println("Fathomed by integrality");
                        found = true;
                        best_int_model = current_model;
                        best_int_opt = current_model.get(GRB.DoubleAttr.ObjVal);
                    }
                }
            }

            System.out.println("Final integer solution");
            addIntegerConstraints(current_model);
            current_model.update();
            if (gap == 0.0) {
                current_model.set(DoubleParam.MIPGapAbs, 0);
            }
            current_model.optimize();
            correctd_list = repairSubtours(current_model);
            // solutionToFile(dataFileInt1);
            System.out.println();
            opt = current_model.get(GRB.DoubleAttr.ObjVal);
            best_int_opt = opt + solRepair(dataFile, checkValidity(current_model), correctd_list,
                    graphSubtours(current_model), repairType);
            verify(current_model);
            System.out.println(
                    "found=" + best_int_opt + "/known=" + kn_opt + "/rel_error=" + (best_int_opt - kn_opt) / kn_opt);
        } catch (GRBException e) {
            System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
        }
    }

    private void repairHeuristic1(int n_max, double gap, int repairType) {
        // Perform a Branch&Bound algorithm
        double opt = 0, best_int_opt = 0, kn_opt = 0;
        String dataFileInt1 = "Int1_" + dataFile;
        String lpDataFile = convert(dataFile, "");
        kn_opt = instances[inst][(int) (0.75 * m - 3.0 + n / 500.0 - 1.0)];
        ArrayList<subPathVars>[] correctd_list;
        try {
            env = new GRBEnv();
            env.set(IntParam.OutputFlag, 0);
            model = new GRBModel(env, Config.getDataPath() + "results/" + lpDataFile);
            model.set(IntParam.OutputFlag, 0);
            model.update();

            // solving the relaxed problem:
            model.optimize();
            solutionToFile(dataFile + "_a");

            // solved with the equivalent column generation method:
            _Bellman_Digraph1();
            model.update();
            if (gap == 0.0) {
                model.set(DoubleParam.MIPGapAbs, 0);
                model.set(GRB.DoubleParam.MIPGap, 0);
            }
            model.set(IntParam.OutputFlag, 0);
            //model.optimize();

            // solve the integer problem:
            // model.set(IntParam.OutputFlag, 1);
            long start = System.nanoTime();
            addIntegerConstraints();
            model.update();
            if (gap == 0.0) {
                model.set(DoubleParam.MIPGapAbs, 0);
                model.set(GRB.DoubleParam.MIPGap, 0);
            }
            model.optimize();
            if (outputEnabled) {
                System.out.println("MILP optimization time = " + (System.nanoTime() - start) * 1e-9);
            }
            //-----------------------STOP
            
            
            /*
            start = System.nanoTime();
            correctd_list = repairSubtours();
            //solutionToFile(dataFileInt1);
            opt = model.get(GRB.DoubleAttr.ObjVal);
            best_int_opt = opt + solRepair(dataFile, checkValidity(), correctd_list, graphSubtours(), repairType);
            if (outputEnabled) {
                System.out.println(
                        "found=" + best_int_opt + "/known=" + kn_opt + "/rel_error=" + (best_int_opt - kn_opt) / kn_opt);
                // TODO: save this integer solution
                System.out.println("Repairing time = " + (System.nanoTime() - start) * 1e-9);
            }
            verify();
            */
            
            //----------------
            extractSolution();
            //----------------
        } catch (GRBException e) {
            System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
        }
    }

    /**
     * Extracts the solution from gurobi model variables
     */
    protected void extractSolution() {
        Solution solution = new Solution(this);
        try {
            GRBVar[] varr = model.getVars();
            int nr_vars = model.get(GRB.IntAttr.NumVars);

            for (int j = 0; j < nr_vars; j++) {
                String nume = varr[j].get(StringAttr.VarName);
                double valoare = varr[j].get(GRB.DoubleAttr.X);
                if (valoare > tolerance) {
                    int h = Integer.valueOf(nume.substring(nume.indexOf('x') + 1, nume.indexOf('y')));
                    int k = Integer.valueOf(nume.substring(nume.indexOf('y') + 1));
                    solution.set(h, k, 1);
                }
            }
            solutions.add(solution);
        } catch (GRBException e) {
            System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
        }
    }

    public static void main(String[] args) throws IOException {
        long start = System.nanoTime();
        // heuristic(args[0], Double.valueOf(args[1]), Integer.valueOf(args[2]));
        // simple_heuristic(args[0], Double.valueOf(args[1]), Integer.valueOf(args[2]));
        new Model_b_heuristic(Config.getDataPath() + "m4n500s0.inp").repairHeuristic1(10, 0.0, 20);
        System.out.println("Execution time1 = " + (System.nanoTime() - start) * 1e-9);

    }

    @Override
    protected void _solve() throws Exception {
        repairHeuristic1(10, 0.0, 20);
    }

}
