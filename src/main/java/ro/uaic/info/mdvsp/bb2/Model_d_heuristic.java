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

import edu.princeton.cs.algs4.AcyclicSP;
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
import ro.uaic.info.mdvsp.Model;
import ro.uaic.info.mdvsp.Solution;

public class Model_d_heuristic extends Model {

    public int n, m, inst;
    public int[] next = new int[n + m];
    public double[][] c = new double[n + m][n + m];
    public double[][] sol = new double[n + m][n + m];
    public GRBEnv env;
    public GRBModel model;
    public double precision_a = 1e-9;
    public double precision_b = 1e-15;
    public double precision_c = 1e-5;

    private int algorithm = 0;

    /**
     *
     * @param filename
     * @param algorithm
     * @throws java.io.IOException
     */
    public Model_d_heuristic(String filename, int algorithm) throws IOException {
        //0=  Bellman, 1 = DagSP, 2 = DagSPNew
        super(filename);
        this.algorithm = algorithm;
    }

    /**
     *
     * @param other
     */
    public Model_d_heuristic(Model other) {
        super(other);
    }

    public String convert(String dataFile, String vtype) {
        File file = new File(dataFile);
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
            if ("Integers".equals(vtype)) {
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
                e.printStackTrace();
                System.err.println(e);
            }
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                System.out.println("Error in closing the BufferedWriter" + ex);
            }
        }

        return lp_fileName;
    }

    public void dagDigraphNew() throws GRBException {// for the simple heuristic
        GRBVar[] variabile = model.getVars();
        String nume = "";
        int stop = 1, i, j;
        double valoare = 0, current_optimum = 0.0, new_optimum = 0;
        ArrayList<ArrayList<Integer>> list = new ArrayList<ArrayList<Integer>>();
        HashMap<Arc, ArrayList<ArrayList<Integer>>> arc_list = new HashMap<Arc, ArrayList<ArrayList<Integer>>>();

        if (outputEnabled) {
            System.out.println("New_Dag in");
        }
        long start1 = 0, aux1 = System.nanoTime(), start2 = 0, aux2 = System.nanoTime();
        int k = 0;
        while (stop > 0) {
            k++;
            aux1 = System.nanoTime();
            EdgeWeightedDigraph graph = new EdgeWeightedDigraph(n + 2 * m);
            DirectedEdge e;
            for (int p = 0; p < variabile.length; p++) {
                nume = variabile[p].get(GRB.StringAttr.VarName);
                valoare = 1.0 - variabile[p].get(GRB.DoubleAttr.X);
                i = Integer.valueOf(nume.substring(nume.indexOf('x') + 1, nume.indexOf('y')));
                j = Integer.valueOf(nume.substring(nume.indexOf('y') + 1));
                if (i != j) {
                    if (j < m) {
                        e = new DirectedEdge(i, n + m + j, Math.abs(valoare));
                    } else {
                        e = new DirectedEdge(i, j, Math.abs(valoare));
                    }
                    graph.addEdge(e);
                }

            }
            stop = dagSPAlgNew(graph, list, arc_list, 0);
            aux1 = System.nanoTime() - aux1;
            start1 += aux1;
            aux2 = System.nanoTime();
            model.optimize();
            aux2 = System.nanoTime() - aux2;
            start2 += aux2;
            new_optimum = model.get(GRB.DoubleAttr.ObjVal);
            if (Math.abs(current_optimum - new_optimum) < 1.0e-8) {
                current_optimum = new_optimum;
                if (outputEnabled) {
                    System.out.format(" - New_Dag out " + "| k = %d | New_Dag_time = %.3f | Gur_time = %.3f", k,
                            start1 * 1e-9, start2 * 1e-9);
                }
                return;
            }
            current_optimum = new_optimum;
        }
        if (outputEnabled) {
            System.out.format(" - New_Dag out " + "| k = %d | New_Dag_time = %.3f | Gur_time = %.3f", k, start1 * 1e-9,
                    start2 * 1e-9);
        }
        return;
    }

    public int dagSPAlgNew(EdgeWeightedDigraph graph, ArrayList<ArrayList<Integer>> list, Map arc_list,
            int i_start) throws GRBException {
        int y = 0, constrLength = 0, from, to = -1, path_index = 0;
        boolean out = false;
        double dist = 0;
        Arc arc = new Arc();
        List<Integer>[] path_list = new ArrayList[m];
        LinkedList<Arc> _arc_path_list = new LinkedList<Arc>();
        ArrayList<ArrayList<Integer>> _paths = new ArrayList<ArrayList<Integer>>();
        AcyclicSPNew dag;
        // BellmanNew bellman;
        GRBLinExpr expr;
        GRBVar vare;

        for (int s = i_start; s < m; s++) {
            path_list = new ArrayList[m];
            path_index = 0;
            dag = new AcyclicSPNew(graph, s);
            expr = new GRBLinExpr();
            vare = null;
            for (int i = 0; i < m; i++) {
                if (i != s) {
                    dist = dag.distTo(n + m + i);
                    if ((dist < Double.POSITIVE_INFINITY) && ((dist < 1.0) && (1.0 - dist > 1e-15))) {
                        // if (outputEnabled) System.out.println("Drum eligibil = " + dist);
                        path_index++;
                        path_list[path_index] = new ArrayList<Integer>();
                        path_list[path_index].add(s);
                        Iterator<DirectedEdge> iterator = dag.pathTo(n + m + i).iterator();
                        int tto;
                        while (iterator.hasNext()) {
                            tto = iterator.next().to();
                            path_list[path_index].add(tto);
                        }
                        path_list[path_index].remove(path_list[path_index].size() - 1);
                        path_list[path_index].add(i);
                        // if (outputEnabled) System.out.println("kk");
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
            // if (outputEnabled) System.out.println("ff");
        }
        return y;
    }

    public void dagDigraph() throws GRBException {// for the simple heuristic
        GRBVar[] variabile = model.getVars();
        String nume = "";
        int stop = 1, i, j;
        double valoare = 0, current_optimum = 0.0, new_optimum = 0;
        ArrayList<ArrayList<Integer>> list = new ArrayList<ArrayList<Integer>>();
        HashMap<Arc, ArrayList<ArrayList<Integer>>> arc_list = new HashMap<Arc, ArrayList<ArrayList<Integer>>>();

        if (outputEnabled) {
            System.out.println("Dag in");
        }
        long start1 = 0, aux1 = System.nanoTime(), start2 = 0, aux2 = System.nanoTime();
        int k = 0;
        while (stop > 0) {
            k++;
            aux1 = System.nanoTime();
            EdgeWeightedDigraph graph = new EdgeWeightedDigraph(n + 2 * m);
            DirectedEdge e;
            for (int p = 0; p < variabile.length; p++) {
                nume = variabile[p].get(GRB.StringAttr.VarName);
                valoare = 1.0 - variabile[p].get(GRB.DoubleAttr.X);
                i = Integer.valueOf(nume.substring(nume.indexOf('x') + 1, nume.indexOf('y')));
                j = Integer.valueOf(nume.substring(nume.indexOf('y') + 1));
                if (i != j) {
                    if (j < m) {
                        e = new DirectedEdge(i, n + m + j, Math.abs(valoare));
                    } else {
                        e = new DirectedEdge(i, j, Math.abs(valoare));
                    }
                    graph.addEdge(e);
                }

            }
            stop = dagSPAlg(graph, list, arc_list, 0);
            aux1 = System.nanoTime() - aux1;
            start1 += aux1;
            aux2 = System.nanoTime();
            model.optimize();
            aux2 = System.nanoTime() - aux2;
            start2 += aux2;
            new_optimum = model.get(GRB.DoubleAttr.ObjVal);
            if (Math.abs(current_optimum - new_optimum) < 1.0e-8) {
                current_optimum = new_optimum;
                if (outputEnabled) {
                    System.out.format(" - Dag out " + "| k = %d | Dag_time = %.3f | Gur_time = %.3f", k, start1 * 1e-9,
                            start2 * 1e-9);
                }
                return;
            }
            current_optimum = new_optimum;
        }
        if (outputEnabled) {
            System.out.format(" - Dag out " + "| k = %d | Dag_time = %.3f | Gur_time = %.3f", k, start1 * 1e-9,
                    start2 * 1e-9);
        }
        return;
    }

    public int dagSPAlg(EdgeWeightedDigraph graph, ArrayList<ArrayList<Integer>> list, Map arc_list, int i_start)
            throws GRBException {
        int y = 0, constrLength = 0, from, to = -1, path_index = 0;
        boolean out = false;
        double dist = 0;
        Arc arc = new Arc();
        List<Integer>[] path_list = new ArrayList[m];
        LinkedList<Arc> _arc_path_list = new LinkedList<Arc>();
        ArrayList<ArrayList<Integer>> _paths = new ArrayList<ArrayList<Integer>>();
        AcyclicSP dag;
        // BellmanNew bellman;
        GRBLinExpr expr;
        GRBVar vare;

        for (int s = i_start; s < m; s++) {
            path_list = new ArrayList[m];
            path_index = 0;
            dag = new AcyclicSP(graph, s);
            expr = new GRBLinExpr();
            vare = null;
            for (int i = 0; i < m; i++) {
                if (i != s) {
                    dist = dag.distTo(n + m + i);
                    if ((dist < Double.POSITIVE_INFINITY) && ((dist < 1.0) && (1.0 - dist > 1e-15))) {
                        // if (outputEnabled) System.out.println("Drum eligibil = " + dist);
                        path_index++;
                        path_list[path_index] = new ArrayList<Integer>();
                        path_list[path_index].add(s);
                        Iterator<DirectedEdge> iterator = dag.pathTo(n + m + i).iterator();
                        int tto;
                        while (iterator.hasNext()) {
                            tto = iterator.next().to();
                            path_list[path_index].add(tto);
                        }
                        path_list[path_index].remove(path_list[path_index].size() - 1);
                        path_list[path_index].add(i);
                        // if (outputEnabled) System.out.println("kk");
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
            // if (outputEnabled) System.out.println("ff");
        }
        return y;
    }

    public void bellmanDigraph2New() throws GRBException {// for the simple heuristic
        GRBVar[] variabile = model.getVars();
        String nume = "";
        int stop = 1, i, j;
        double valoare = 0, current_optimum = 0.0, new_optimum = 0;
        ArrayList<ArrayList<Integer>> list = new ArrayList<ArrayList<Integer>>();
        HashMap<Arc, ArrayList<ArrayList<Integer>>> arc_list = new HashMap<Arc, ArrayList<ArrayList<Integer>>>();

        if (outputEnabled) {
            System.out.println("Bell in");
        }
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
                current_optimum = new_optimum;
                if (outputEnabled) {
                    System.out.format(" - Bell out " + "| k = %d | Bell_time = %.3f | Gur_time = %.3f", k, start1 * 1e-9,
                            start2 * 1e-9);
                }
                return;
            }
            current_optimum = new_optimum;
        }
        if (outputEnabled) {
            System.out.format(" - Bell out " + "| k = %d | Bell_time = %.3f | Gur_time = %.3f", k, start1 * 1e-9,
                    start2 * 1e-9);
        }
        return;
    }

    public int bellmanAlg2New(EdgeWeightedDigraph graph, ArrayList<ArrayList<Integer>> list, Map arc_list,
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
                        // if (outputEnabled) System.out.println("Drum eligibil = " + dist);
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
            // if (outputEnabled) System.out.println("ff");
        }
        return y;
    }

    public double solRepair(String dataFile, ArrayList<subPathVars> vars,
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
            // if (outputEnabled) System.out.println("Chosen vertex = " + vertex);
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
                // if (outputEnabled) System.out.println("Path size = " + path.size());
                // if (outputEnabled) System.out.println("path: " + path.toString());
                if (k == 0) {
                    path.size();
                    // if (outputEnabled) System.out.println("Path size = " + path.size());
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

    public double cycleCorrections(ArrayList<subPathVars> vars, ArrayList<subPathVars>[] corrections_list,
            ArrayList<Integer> path) {
        // A REPAIR METHOD THAT TAKES EACH PATH AND DECIDE THE BEST WAY TO REPAIR IT
        double sum1 = 0.0, sum2 = 0.0, sum = 0.0;
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
            e.printStackTrace();
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
    }

    public void addContinuousConstraints() throws GRBException {
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
                // if (outputEnabled) System.out.println("Subtour:");
                for (int p = 0; p < s_list[h].size(); p++) {
                    from = h;
                    to = s_list[h].get(p);
                    while (next[to] != -1) {
                        from = to;
                        to = next[to];
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
                        // if (outputEnabled) System.out.println("Constrangere adaugata");
                        model.addConstr(expr, GRB.LESS_EQUAL, constrLength - 1, "");
                    }
                    // if (outputEnabled) System.out.println();
                }
            }
        }
        model.update();
        return out;
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
            // if (outputEnabled) System.out.println("val = "+ val);
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
                        // if (outputEnabled) System.out.println(" removed");
                    }
                }
            }
        }
        return corrections_list;
    }

    public int[][] graphSubtours() throws GRBException {
        GRBVar[] vars = model.getVars();
        double val = 0;
        int i = 0, j = 0, from = -1, to = -1;
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
                var = new subPathVars(h, k);
                verify_out[h]++;
                verify_in[k]++;
                list.add(var);
            }
        }

        for (int j = 0; j < n + m; j++) {
            if (verify_in[j] != verify_out[j]) {
                if (outputEnabled) {
                    System.out.println(" *** SOLUTIE GRESITA *** ");
                }
                if (outputEnabled) {
                    System.out.println("vertex " + j + " in = " + verify_in[j]);
                }
                if (outputEnabled) {
                    System.out.println("vertex " + j + "out = " + verify_out[j]);
                }
            }
        }
        if (outputEnabled) {
            System.out.println();
        }
        if (outputEnabled) {
            System.out.println(" *** SOLUTIE CORECTA *** ");
        }
        return list;
    }

    public void solutionToFile(String dataFile) throws GRBException {
        if (dataFile == "-1") {
            if (outputEnabled) {
                System.out.println("Usage: input filenamebranchBound(\"m8n1000s4.inp\", 10, 0.0, 2);");
            }
            System.exit(1);
        }
        int h, k;

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
                    nume = varr[j].get(StringAttr.VarName);
                    h = Integer.valueOf(nume.substring(nume.indexOf('x') + 1, nume.indexOf('y')));
                    k = Integer.valueOf(nume.substring(nume.indexOf('y') + 1));
                    writer.write(h + "\t" + k + "\t" + varr[j].get(GRB.DoubleAttr.X));
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error code: " + e.getMessage());
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println(e);
            }
        }
    }

    public void repairHeuristicBellman(String dataFile, double gap, int threads, int method) {
        // Bellman Princeton's modified algorithm
        if (dataFile == "-1") {
            if (outputEnabled) {
                System.out.println("Usage: input filename");
            }
            System.exit(1);
        }

        double opt = 0, best_int_opt = 0, kn_opt = knownOptimum;
        String dataFileInt1 = "Int1_" + dataFile;
        String lpDataFile = convert(dataFile, "");
        ArrayList<subPathVars>[] correctd_list;

        try {
            long start1 = System.nanoTime();
            env = new GRBEnv();
            model = new GRBModel(env, Config.getDataPath() + "results/" + lpDataFile);
            model.set(IntParam.OutputFlag, 0);
            model.set(GRB.IntParam.Threads, threads);
            model.set(GRB.IntParam.Method, method);
            model.update();

            // solving the relaxed problem:
            model.optimize();
            solutionToFile(dataFile + "_a");

            // solved with the equivalent column generation method:
            long start2 = System.nanoTime();
            bellmanDigraph2New();
            if (outputEnabled) {
                System.out.println();
                System.out.println("+++++++++++++++++++++++++++");
            }
            if (outputEnabled) {
                System.out.format("Bellman time =  %.3f", (System.nanoTime() - start2) * 1e-9,
                        (System.nanoTime() - start1) * 1e-9);
                System.out.println();
                System.out.println("+++++++++++++++++++++++++++");
            }
            model.update();
            if (gap == 0.0) {
                model.set(DoubleParam.MIPGapAbs, 0);
            }
            model.set(IntParam.OutputFlag, 0);
            model.optimize();

            // solve the integer problem:
            long start = System.nanoTime();
            addIntegerConstraints();
            model.update();
            if (gap == 0.0) {
                model.set(DoubleParam.MIPGapAbs, 0);
            }
            model.optimize();
            if (outputEnabled) {
                System.out.println();
                System.out.format("MILP optimization time = %.3f", (System.nanoTime() - start) * 1e-9);
            }
            start = System.nanoTime();
            correctd_list = repairSubtours();
            solutionToFile(dataFileInt1);
            opt = model.get(GRB.DoubleAttr.ObjVal);
            best_int_opt = opt + solRepair(dataFile, checkValidity(), correctd_list, graphSubtours());
            // TODO: save this integer solution
            if (outputEnabled) {
                System.out.format("found = %d | known = %d | rel_error= %.3f", (int) best_int_opt, (int) kn_opt,
                        (best_int_opt - kn_opt) / kn_opt);
                System.out.println();
                System.out.format("Repairing time =  %.3f | Overall time =  %.3f", (System.nanoTime() - start) * 1e-9,
                        (System.nanoTime() - start1) * 1e-9);
            }
            // verify();
            //----------------
            extractSolution();
            //----------------            
        } catch (GRBException e) {
            e.printStackTrace();
            System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void repairHeuristicDagSP(String dataFile, double gap, int threads, int method) {
        // Bellman Princeton's modified algorithm
        if (dataFile == "-1") {
            if (outputEnabled) {
                System.out.println("Usage: input filename");
            }
            System.exit(1);
        }
        double opt = 0, best_int_opt = 0, kn_opt = knownOptimum;
        String dataFileInt1 = "Int1_" + dataFile;
        String lpDataFile = convert(dataFile, "");
        ArrayList<subPathVars>[] correctd_list;

        try {
            long start1 = System.nanoTime();
            env = new GRBEnv();
            model = new GRBModel(env, Config.getDataPath() + "results/" + lpDataFile);
            model.set(IntParam.OutputFlag, 0);
            model.set(GRB.IntParam.Threads, threads);
            model.set(GRB.IntParam.Method, method);
            model.update();

            // solving the relaxed problem:
            model.optimize();
            solutionToFile(dataFile + "_a");

            // solved with the equivalent column generation method:
            long start2 = System.nanoTime();
            dagDigraph();
            if (outputEnabled) {
                System.out.println();
                System.out.println("+++++++++++++++++++++++++++");
                System.out.format("DAG time =  %.3f", (System.nanoTime() - start2) * 1e-9,
                        (System.nanoTime() - start1) * 1e-9);
                System.out.println();
                System.out.println("+++++++++++++++++++++++++++");
            }
            model.update();
            if (gap == 0.0) {
                model.set(DoubleParam.MIPGapAbs, 0);
            }
            model.set(IntParam.OutputFlag, 0);
            model.optimize();

            // solve the integer problem:
            long start = System.nanoTime();
            addIntegerConstraints();
            model.update();
            if (gap == 0.0) {
                model.set(DoubleParam.MIPGapAbs, 0);
            }
            model.optimize();
            if (outputEnabled) {
                System.out.println();
                System.out.format("MILP optimization time = %.3f", (System.nanoTime() - start) * 1e-9);
            }
            start = System.nanoTime();
            correctd_list = repairSubtours();
            solutionToFile(dataFileInt1);
            opt = model.get(GRB.DoubleAttr.ObjVal);
            best_int_opt = opt + solRepair(dataFile, checkValidity(), correctd_list, graphSubtours());
            if (outputEnabled) {
                System.out.format("found = %d | known = %d | rel_error= %.3f", (int) best_int_opt, (int) kn_opt,
                        (best_int_opt - kn_opt) / kn_opt);
            }
            // TODO: save this integer solution
            if (outputEnabled) {
                System.out.println();
                System.out.format("Repairing time =  %.3f | Overall time =  %.3f", (System.nanoTime() - start) * 1e-9,
                        (System.nanoTime() - start1) * 1e-9);
            }
            // verify();
            //----------------
            extractSolution();
            //----------------            
        } catch (GRBException e) {
            e.printStackTrace();
            System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
        }
    }

    public void repairHeuristicDagSPNew(String dataFile, double gap, int threads, int method) {
        // Bellman Princeton's modified algorithm
        if (dataFile == "-1") {
            if (outputEnabled) {
                System.out.println("Usage: input filename");
            }
            System.exit(1);
        }
        double opt = 0, best_int_opt = 0, kn_opt = knownOptimum;
        String dataFileInt1 = "Int1_" + dataFile;
        String lpDataFile = convert(dataFile, "");
        ArrayList<subPathVars>[] correctd_list;

        try {
            long start1 = System.nanoTime();
            env = new GRBEnv();
            model = new GRBModel(env, Config.getDataPath() + "results/" + lpDataFile);
            model.set(IntParam.OutputFlag, 0);
            model.set(GRB.IntParam.Threads, threads);
            model.set(GRB.IntParam.Method, method);
            model.update();

            // solving the relaxed problem:
            model.optimize();
            solutionToFile(dataFile + "_a");

            // solved with the equivalent column generation method:
            long start2 = System.nanoTime();
            dagDigraphNew();
            if (outputEnabled) {
                System.out.println();
            }
            if (outputEnabled) {
                System.out.println("+++++++++++++++++++++++++++");
                System.out.format("New DAG time =  %.3f", (System.nanoTime() - start2) * 1e-9,
                        (System.nanoTime() - start1) * 1e-9);
                System.out.println();
                System.out.println("+++++++++++++++++++++++++++");
            }
            model.update();
            if (gap == 0.0) {
                model.set(DoubleParam.MIPGapAbs, 0);
            }
            model.set(IntParam.OutputFlag, 0);
            model.optimize();

            // solve the integer problem:
            long start = System.nanoTime();
            addIntegerConstraints();
            model.update();
            if (gap == 0.0) {
                model.set(DoubleParam.MIPGapAbs, 0);
            }
            model.optimize();
            if (outputEnabled) {
                System.out.println();
                System.out.format("MILP optimization time = %.3f", (System.nanoTime() - start) * 1e-9);
            }
            start = System.nanoTime();
            correctd_list = repairSubtours();
            solutionToFile(dataFileInt1);
            opt = model.get(GRB.DoubleAttr.ObjVal);

            // Aici se face repararea solutiei si trebuie inlocuita cu cealalta reparare:
            best_int_opt = opt + solRepair(dataFile, checkValidity(), correctd_list, graphSubtours());
            if (outputEnabled) {
                System.out.format("found = %d | known = %d | rel_error= %.3f", (int) best_int_opt, (int) kn_opt,
                        (best_int_opt - kn_opt) / kn_opt);
            }
            // TODO: save this integer solution
            if (outputEnabled) {
                System.out.println();
                System.out.format("Repairing time =  %.3f | Overall time =  %.3f", (System.nanoTime() - start) * 1e-9,
                        (System.nanoTime() - start1) * 1e-9);
            }
            // verify();
            //----------------
            extractSolution();
            //----------------            
        } catch (GRBException e) {
            e.printStackTrace();
            System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
        }
    }

    public static void main(String[] args) throws IOException {
        long start = System.nanoTime();
        // setting the method to solve: -1=automatic, 0=primal simplex, 1=dual simplex,
        // 2=barrier,
        // 3=concurrent, 4=deterministic concurrent,5=deterministic concurrent simplex.
        // repairHeuristicBellman2(args[0], Double.valueOf(args[1]));
        System.out.println();
        System.out.println("------------------------------------------");
        System.out.println();
        // repairHeuristicBellman(args[0], Double.valueOf(args[1]),
        // Integer.valueOf(args[2]), Integer.valueOf(args[3]));
        Model_d_heuristic app = new Model_d_heuristic("m8n1500s1.inp", 0);
        app.repairHeuristicBellman("m8n1500s1.inp", 0.0, 0, -1);
        System.out.println();
        System.out.println("------------------------------------------");
        System.out.println();
        System.out.println("Bellman time = " + (System.nanoTime() - start) * 1e-9);
        start = System.nanoTime();
        // repairHeuristicDagSP(args[0], Double.valueOf(args[1]),
        // Integer.valueOf(args[2]), Integer.valueOf(args[3]));
        app.repairHeuristicDagSP("m8n1500s1.inp", 0.0, 0, -1);
        System.out.println();
        System.out.println("------------------------------------------");
        System.out.println();
        System.out.println("Dag time = " + (System.nanoTime() - start) * 1e-9);
        start = System.nanoTime();
        app.repairHeuristicDagSPNew("m8n1500s1.inp", 0.0, 0, -1);
        // repairHeuristicDagSPNew(args[0], Double.valueOf(args[1]),
        // Integer.valueOf(args[2]), Integer.valueOf(args[3]));
        System.out.println();
        System.out.println("NewDag time = " + (System.nanoTime() - start) * 1e-9);
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
            e.printStackTrace();
            System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
        }
    }

    @Override
    protected void _solve() throws Exception {        
        try {
            switch (algorithm) {
                case 1:
                    repairHeuristicDagSP(dataFile, 0.0, 0, -1);
                    break;
                case 2:
                    repairHeuristicDagSPNew(dataFile, 0.0, 0, -1);
                    break;
                default:
                    repairHeuristicBellman(dataFile, 0.0, 0, -1);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
