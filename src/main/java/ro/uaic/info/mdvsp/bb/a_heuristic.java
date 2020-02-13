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
//import gurobi.GRB.StringParam;

public class a_heuristic {
	public static int n, m, inst;
	public static int[] next = new int[n + m];
	public static double[][] c = new double[n + m][n + m];
	public static double[][] sol = new double[n + m][n + m];
	public static GRBEnv env;
	public static GRBModel model;
	public static int[][] instances = { { 1289114, 2516247, 3830912, 1292411, 2422112, 3500160 },
			{ 1241618, 2413393, 3559176, 1276919, 2524293, 3802650 },
			{ 1283811, 2452905, 3649757, 1304251, 2556313, 3605094 },
			{ 1258634, 2490812, 3406815, 1277838, 2478393, 3515802 },
			{ 1317077, 2519191, 3567122, 1276010, 2498388, 3704953 } }; // new int[5][6];
	public static double precision_a = 1e-9;
	public static double precision_b = 1e-15;

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
			for (int i = 0; i < n + m; i++)
				for (int j = 0; j < n + m; j++)
					c[i][j] = -1.0;
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
				if (c[0][j] >= 0.0)
					writer.write(" + " + c[0][j] + " x0y" + j);
			}
			for (int i = 1; i < n + m; i++) {
				for (int j = 0; j < n + m; j++) {
					if (c[i][j] >= 0.0)
						writer.write(" + " + c[i][j] + " x" + i + "y" + j);
				}
			}

			writer.newLine();
			writer.write("Subject To ");

			for (int i = 0; i < n + m; i++) {
				writer.newLine();
				writer.write("ca" + i + ": ");
				for (int j = 0; j < n + m; j++) {
					if (c[j][i] >= 0.0)
						writer.write(" + " + 1 + " x" + j + "y" + i);
				}
				writer.write(" = " + r[i]);
			}
			for (int i = 0; i < n + m; i++) {
				writer.newLine();
				writer.write("cb" + i + ": ");
				for (int j = 0; j < n + m; j++) {
					if (c[i][j] >= 0.0)
						writer.write(" + " + 1 + " x" + i + "y" + j);
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
				if (writer != null)
					writer.close();
			} catch (Exception ex) {
				System.out.println("Error in closing the BufferedWriter" + ex);
			}
		}

		return lp_fileName;
	}

	public static ArrayList<subPathVars>[] cloneArrayList(ArrayList<subPathVars>[] corrected_list) {
		ArrayList<subPathVars>[] new_corrected_list = (ArrayList<subPathVars>[]) new ArrayList[m * m + m];
		for (int i = 0; i < corrected_list.length; i++) {
			if (corrected_list[i] != null && corrected_list[i].size() > 0)
				new_corrected_list[i] = new ArrayList<subPathVars>(corrected_list[i]);
			else
				new_corrected_list[i] = new ArrayList<subPathVars>();
		}
		return new_corrected_list;
	}

	public static void Bellman_Digraph1() throws GRBException {
		GRBVar[] variabile = model.getVars();
		int stop = 1, i, j, k = 0;
		String nume = "";
		double valoare = 0, current_optimum = 0.0;
		System.out.println("Bellman in");
		while (stop > 0) {
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
			stop = BellmanFMSolveAlgEff(graph, 0);
			model.optimize();
			System.out.println("k = " + (k++) + " stop = " + stop + " Obj = " + model.get(GRB.DoubleAttr.ObjVal));
			if (Math.abs(current_optimum - model.get(GRB.DoubleAttr.ObjVal)) < 1.0e-8)
				return;
			current_optimum = model.get(GRB.DoubleAttr.ObjVal);
		}
		System.out.println("Bellman out");
	}

	// Paths_arcPaths

	public static void Bellman_Digraph() throws GRBException {
		GRBVar[] variabile = model.getVars();
		String nume = "";
		List<Integer>[] path_list = new ArrayList[m];
		int stop = 1, i, j;
		double valoare = 0, current_optimum = 0.0;

		System.out.println("Bellman in");
		long start = System.nanoTime();
		while (stop > 0) {
			// k++;
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
			stop = BellmanFMSolveAlgEff(graph, 0);
			// System.out.println(stop + " drumuri");
			start = System.nanoTime();
			model.optimize();
			// System.out.println("Exec_time = " + (System.nanoTime() - start) * 1e-9);
			// System.out.print(" | " + (k++) + " stop = " + stop + " Obj = " +
			// model.get(GRB.DoubleAttr.ObjVal));
			if (Math.abs(current_optimum - model.get(GRB.DoubleAttr.ObjVal)) < 1.0e-8) {
				System.out.println("Bellman out");
				current_optimum = model.get(GRB.DoubleAttr.ObjVal);
				System.out.println("current_optimum = " + current_optimum);
				return;
			}
			current_optimum = model.get(GRB.DoubleAttr.ObjVal);
			System.out.println("current_optimum = " + current_optimum);
		}

	}

	public static int BellmanFMSolveAlgEff(EdgeWeightedDigraph graph, int i_start) throws GRBException {
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
			for (int i = 0; i < m; i++)
				for (int j = 0; j < m; j++) {
					out = false;
					if (path_list[i] != null && path_list[j] != null && i != j) {
						if (path_list[i].size() < path_list[j].size()) {
							for (int h = 0; h < path_list[i].size(); h++)
								if (path_list[i].get(h) != path_list[j].get(h)) {
									out = true;
									break;
								}
							if (!out)
								path_list[j] = null;
						} else if (path_list[j].size() < path_list[i].size()) {
							for (int h = 0; h < path_list[j].size(); h++)
								if (path_list[j].get(h).intValue() != path_list[i].get(h).intValue()) {
									out = true;
									break;
								}
							if (!out)
								path_list[i] = null;
						}
					}
				}
			for (int i = 0; i < m; i++)
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
						constrLength++;
						vare = model.getVarByName("x" + from + "y" + to);
						expr.addTerm(1.0, vare);
						from = to;
					}

					model.addConstr(expr, GRB.LESS_EQUAL, constrLength - 1, "");
				}
			// System.out.println("ff");
		}
		return y;
	}

	public static double solRepairWithNewConstraints(String dataFile, ArrayList<subPathVars>[] corrections_list,
			int[][] graph) {
		double sum = 0;
		int graph_dim = 0, v = -1, last_vertex = -1, vertex = -1;
		int[] degrees = new int[m];
		boolean[] marked = new boolean[m];
		ArrayList<Integer> path = new ArrayList<Integer>();

		for (int i = 0; i < m; i++)
			for (int j = 0; j < m; j++)
				if (graph[i][j] != 0) {
					graph_dim += graph[i][j];
					degrees[i] += graph[i][j];
				}
		for (int i = 0; i < m; i++)
			marked[i] = false;
		while (graph_dim > 0) {
			last_vertex = -1;
			vertex = (new Random()).nextInt(m);
			while (marked[vertex] == true)
				vertex = (new Random()).nextInt(m);
			Stack<Integer> stiva = new Stack<Integer>(); // dfs:
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
				while (v != last_vertex) {
					k--;
					graph[v][vertex]--;
					degrees[v]--;
					if (degrees[v] == 0)
						marked[v] = true;
					else
						marked[v] = false;
					graph_dim--;
					vertex = v;
					v = path.get(k);
				}
				graph[v][vertex]--;
				degrees[v]--;
				if (degrees[v] == 0)
					marked[v] = true;
				else
					marked[v] = false;
				graph_dim--;
				k--;
				while (k != -1) {
					marked[path.get(k)] = false;
					path.remove(k);
					k--;
				}
				// sum += cycleCorrections(corrections_list, path);// corrections along the
				// cycle
				sum += cycleCorrectionsAndNewConstraints(corrections_list, path);// corrections along the cycle
			}
		}
		return sum;
	}

	public static double solRepair(String dataFile, ArrayList<subPathVars> vars,
			ArrayList<subPathVars>[] corrections_list, int[][] graph, int repairType) {
		double sum = 0;
		int graph_dim = 0, v = -1, last_vertex = -1, vertex = -1;
		int[] degrees = new int[m];
		boolean[] marked = new boolean[m];
		ArrayList<Integer> path = new ArrayList<Integer>();

		for (int i = 0; i < m; i++)
			for (int j = 0; j < m; j++)
				if (graph[i][j] != 0) {
					graph_dim += graph[i][j];
					degrees[i] += graph[i][j];
				}
		for (int i = 0; i < m; i++)
			marked[i] = false;
		while (graph_dim > 0) {
			last_vertex = -1;
			vertex = (new Random()).nextInt(m);
			while (marked[vertex] == true)
				vertex = (new Random()).nextInt(m);
			// System.out.println("Chosen vertex = " + vertex);
			Stack<Integer> stiva = new Stack<Integer>(); // dfs:
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
				while (v != last_vertex) {
					k--;
					graph[v][vertex]--;
					degrees[v]--;
					if (degrees[v] == 0)
						marked[v] = true;
					else
						marked[v] = false;
					graph_dim--;
					vertex = v;
					v = path.get(k);
				}
				graph[v][vertex]--;
				degrees[v]--;
				if (degrees[v] == 0)
					marked[v] = true;
				else
					marked[v] = false;
				graph_dim--;
				k--;
				while (k != -1) {
					marked[path.get(k)] = false;
					path.remove(k);
					k--;
				}
				if (repairType == 1)
					sum += cycleCorrections1(corrections_list, path);// corrections along the
				// cycle

				if (repairType == 2)
					sum += cycleCorrections2(vars, corrections_list, path);// corrections along the
				// cycle

				if (repairType == 3)
					sum += cycleCorrections3(vars, corrections_list, path);// corrections along the
				// cycle

				if (repairType == 30)
					sum += cycleCorrectionsSimplified3(vars, corrections_list, path);// corrections along the
				// cycle

				if (repairType == 33)
					sum += cycleCorrections33(corrections_list, path);// corrections along the
				// cycle

				if (repairType == 4)
					sum += cycleCorrections4(corrections_list, path);// corrections along the
				// cycle

				if (repairType == 5)
					sum += cycleCorrections5(corrections_list, path);// corrections along the
				// cycle
				// sum += cycleCorrectionsAndNewConstraints(corrections_list, path);//
				// corrections along the cycle
			}
		}
		return sum;
	}

	public static double solRepair1(String dataFile, ArrayList<subPathVars>[] corrections_list, int[][] graph) {
		double sum = 0;
		int graph_dim = 0, v = -1, last_vertex = -1, vertex = -1;
		int[] degrees = new int[m];
		boolean[] marked = new boolean[m];
		ArrayList<Integer> path = new ArrayList<Integer>();

		for (int i = 0; i < m; i++)
			for (int j = 0; j < m; j++)
				if (graph[i][j] != 0) {
					graph_dim += graph[i][j];
					degrees[i] += graph[i][j];
				}
		for (int i = 0; i < m; i++)
			marked[i] = false;
		while (graph_dim > 0) {
			last_vertex = -1;
			vertex = (new Random()).nextInt(m);
			while (marked[vertex] == true)
				vertex = (new Random()).nextInt(m);
			Stack<Integer> stiva = new Stack<Integer>(); // dfs:
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
				while (v != last_vertex) {
					k--;
					graph[v][vertex]--;
					degrees[v]--;
					if (degrees[v] == 0)
						marked[v] = true;
					else
						marked[v] = false;
					graph_dim--;
					vertex = v;
					v = path.get(k);
				}
				graph[v][vertex]--;
				degrees[v]--;
				if (degrees[v] == 0)
					marked[v] = true;
				else
					marked[v] = false;
				graph_dim--;
				k--;
				while (k != -1) {
					marked[path.get(k)] = false;
					path.remove(k);
					k--;
				}
				sum += cycleCorrections1(corrections_list, path);// corrections along the
				// cycle
				// sum += cycleCorrectionsAndNewConstraints(corrections_list, path);//
				// corrections along the cycle
			}
		}
		return sum;
	}

	public static double solRepair2(String dataFile, ArrayList<subPathVars>[] corrections_list, int[][] graph) {
		double sum = 0;
		int graph_dim = 0, v = -1, last_vertex = -1, vertex = -1;
		int[] degrees = new int[m];
		boolean[] marked = new boolean[m];
		ArrayList<Integer> path = new ArrayList<Integer>();

		for (int i = 0; i < m; i++)
			for (int j = 0; j < m; j++)
				if (graph[i][j] != 0) {
					graph_dim += graph[i][j];
					degrees[i] += graph[i][j];
				}
		for (int i = 0; i < m; i++)
			marked[i] = false;
		while (graph_dim > 0) {
			last_vertex = -1;
			vertex = (new Random()).nextInt(m);
			while (marked[vertex] == true)
				vertex = (new Random()).nextInt(m);
			Stack<Integer> stiva = new Stack<Integer>(); // dfs:
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
				while (v != last_vertex) {
					k--;
					graph[v][vertex]--;
					degrees[v]--;
					if (degrees[v] == 0)
						marked[v] = true;
					else
						marked[v] = false;
					graph_dim--;
					vertex = v;
					v = path.get(k);
				}
				graph[v][vertex]--;
				degrees[v]--;
				if (degrees[v] == 0)
					marked[v] = true;
				else
					marked[v] = false;
				graph_dim--;
				k--;
				while (k != -1) {
					marked[path.get(k)] = false;
					path.remove(k);
					k--;
				}

				sum += cycleCorrections3(null, corrections_list, path);
				// corrections along the cycle
			}
		}
		return sum;
	}

	public static void addCycleConstraint(int i, int j, ArrayList<subPathVars>[] corrections_list) throws GRBException {
		int s = i, t, length;
		GRBLinExpr expr = new GRBLinExpr();
		GRBVar var;

		// System.out.println(i);
		t = corrections_list[m * i + j].get(0).from();
		var = model.getVarByName("x" + s + "y" + t);
		expr.addTerm(1.0, var);
		length = 1;
		// System.out.println(t);
		while (next[t] != -1) {
			s = t;
			length++;
			t = next[t];
			var = model.getVarByName("x" + s + "y" + t);
			expr.addTerm(1.0, var);
			// System.out.println(t);
		}
		model.addConstr(expr, GRB.LESS_EQUAL, length - 1, "");
	}

	public static void addAllCyclesConstraints1(ArrayList<subPathVars>[] corrections_list) throws GRBException {
		int s, t, length = 0;
		GRBLinExpr expr = new GRBLinExpr();
		GRBVar[] variabile = new GRBVar[n + 2];
		double[] coeffs = new double[n + 2];

		for (int i = 0; i < m; i++)
			for (int j = 0; j < m; j++) {
				if (corrections_list[m * i + j] != null) {
					for (int p = 0; p < corrections_list[m * i + j].size(); p++) {
						expr = new GRBLinExpr();
						length = 0;
						variabile = new GRBVar[n + 2];
						coeffs = new double[n + 2];
						s = i;
						t = corrections_list[m * i + j].get(p).from();
						variabile[length] = model.getVarByName("x" + s + "y" + t);
						coeffs[length] = 1.0;
						while (next[t] != -1) {
							length++;
							s = t;
							t = next[t];
							variabile[length] = model.getVarByName("x" + s + "y" + t);
							coeffs[length] = 1.0;
						}
						expr.addTerms(coeffs, variabile, 0, length);
						model.addConstr(expr, GRB.LESS_EQUAL, length, "");
					}
				}
			}
	}

	public static void addAllCyclesConstraints(ArrayList<subPathVars>[] corrections_list) throws GRBException {
		int s, t, length;
		GRBLinExpr expr = new GRBLinExpr();
		GRBVar var;

		System.out.println("Ok");
		for (int i = 0; i < m; i++)
			for (int j = 0; j < m; j++) {
				if (corrections_list[m * i + j] != null) {
					// GRBVar[] variabile = new GRBVar[corrections_list[m * i + j].size()];
					for (int p = 0; p < corrections_list[m * i + j].size(); p++) {
						// System.out.println("Ok1");
						s = i;
						t = corrections_list[m * i + j].get(p).from();
						var = model.getVarByName("x" + s + "y" + t);
						// System.out.println("varx[" + s + ", " + t + "] = " +
						// var.get(GRB.DoubleAttr.X));
						// System.out.println("Ok2");
						expr.addTerm(1.0, var);
						length = 1;
						// System.out.println(t);
						while (next[t] != -1) {
							s = t;
							length++;
							t = next[t];
							var = model.getVarByName("x" + s + "y" + t);
							expr.addTerm(1.0, var);
							// System.out.println(t);
						}
						model.addConstr(expr, GRB.LESS_EQUAL, length - 1, "");
						// System.out.println("expresie = " + expr.toString());
					}
				}
			}
	}

	public static double cycleCorrectionsAndNewConstraints(ArrayList<subPathVars>[] corrections_list,
			ArrayList<Integer> path) {
		double sum1 = 0, sum2 = 0;
		int i, j, trip;

		i = path.get(path.size() - 1);
		j = path.get(0);
		try {
			addCycleConstraint(i, j, corrections_list);
		} catch (GRBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (int p = 0; p <= path.size() - 2; p++) {
			trip = corrections_list[m * i + j].get(0).to();
			// there is a path i, corrections_list[m * i + j].get(0).from(),...,
			// corrections_list[m * i + j].get(0).to(), j
			if (c[trip][i] < 0)
				System.out.println("Negativ +: " + c[trip][i]);
			if (c[trip][j] < 0)
				System.out.println("Negativ -: " + c[trip][j]);

			sum1 += c[trip][i] - c[trip][j];
			i = path.get(p);
			j = path.get(p + 1);
		}
		trip = corrections_list[m * i + j].get(0).to();
		if (c[trip][i] < 0)
			System.out.println("Negativ +: " + c[trip][i]);
		if (c[trip][j] < 0)
			System.out.println("Negativ -: " + c[trip][j]);
		sum1 += c[trip][i] - c[trip][j];
		/* */
		i = path.get(path.size() - 1);
		j = path.get(0);
		for (int p = 0; p <= path.size() - 2; p++) {
			trip = corrections_list[m * i + j].get(0).from();
			if (c[j][trip] < 0)
				System.out.println("Negativ +: " + c[j][trip]);
			if (c[i][trip] < 0)
				System.out.println("Negativ -: " + c[i][trip]);
			sum2 += c[j][trip] - c[i][trip];
			i = path.get(p);
			j = path.get(p + 1);
		}
		trip = corrections_list[m * i + j].get(0).from();
		if (c[j][trip] < 0)
			System.out.println("Negativ +: " + c[j][trip]);
		if (c[i][corrections_list[m * i + j].get(0).from()] < 0)
			System.out.println("Negativ -: " + c[i][trip]);
		sum2 += c[j][trip] - c[i][trip];

		if (sum1 < sum2) {
			i = path.get(path.size() - 1);
			j = path.get(0);
			for (int p = 0; p <= path.size() - 2; p++) {
				corrections_list[m * i + j].remove(0);
				i = path.get(p);
				j = path.get(p + 1);
			}
			corrections_list[m * i + j].remove(0);
			return sum1;
		}
		i = path.get(path.size() - 1);
		j = path.get(0);
		for (int p = 0; p <= path.size() - 2; p++) {
			corrections_list[m * i + j].remove(0);
			i = path.get(p);
			j = path.get(p + 1);
		}
		corrections_list[m * i + j].remove(0);
		return sum2;
	}

	public static double cycleCorrections1(ArrayList<subPathVars>[] corrections_list, ArrayList<Integer> path) {
		// THE ORIGINAL (SIMPLEST) REPAIR METHOD
		double sum1 = 0, sum2 = 0;
		int i, j, trip;

		// if (path.size() > 2)
		// System.out.println(" path size = " + path.size());
		i = path.get(path.size() - 1);
		j = path.get(0);
		for (int p = 0; p <= path.size() - 2; p++) {
			trip = corrections_list[m * i + j].get(0).to();
			// there is a path i, corrections_list[m * i + j].get(0).from(),...,
			// corrections_list[m * i + j].get(0).to(), j
			if (c[trip][i] < 0)
				System.out.println("Negativ +: " + c[trip][i]);
			if (c[trip][j] < 0)
				System.out.println("Negativ -: " + c[trip][j]);
			sum1 += c[trip][i] - c[trip][j];
			i = path.get(p);
			j = path.get(p + 1);
		}
		trip = corrections_list[m * i + j].get(0).to();
		if (c[trip][i] < 0)
			System.out.println("Negativ +: " + c[trip][i]);
		if (c[trip][j] < 0)
			System.out.println("Negativ -: " + c[trip][j]);
		sum1 += c[trip][i] - c[trip][j];
		/* */
		i = path.get(path.size() - 1);
		j = path.get(0);
		for (int p = 0; p <= path.size() - 2; p++) {
			trip = corrections_list[m * i + j].get(0).from();
			if (c[j][trip] < 0)
				System.out.println("Negativ +: " + c[j][trip]);
			if (c[i][trip] < 0)
				System.out.println("Negativ -: " + c[i][trip]);
			sum2 += c[j][trip] - c[i][trip];
			i = path.get(p);
			j = path.get(p + 1);
		}
		trip = corrections_list[m * i + j].get(0).from();
		if (c[j][trip] < 0)
			System.out.println("Negativ +: " + c[j][trip]);
		if (c[i][trip] < 0)
			System.out.println("Negativ -: " + c[i][trip]);
		sum2 += c[j][trip] - c[i][trip];

		i = path.get(path.size() - 1);
		j = path.get(0);
		for (int p = 0; p <= path.size() - 2; p++) {
			corrections_list[m * i + j].remove(0);
			i = path.get(p);
			j = path.get(p + 1);
		}
		corrections_list[m * i + j].remove(0);

		if (sum1 < sum2)
			return sum1;

		/*
		 * i = path.get(path.size() - 1); j = path.get(0); for (int p = 0; p <=
		 * path.size() - 2; p++) { corrections_list[m * i + j].remove(0); i =
		 * path.get(p); j = path.get(p + 1); } corrections_list[m * i + j].remove(0);
		 */
		return sum2;
	}

	public static double cycleCorrections2(ArrayList<subPathVars> vars, ArrayList<subPathVars>[] corrections_list,
			ArrayList<Integer> path) {
		// A REPAIR METHOD THAT TAKES EACH PATH AND DECIDE THE BEST WAY TO REPAIR IT
		double sum = 0.0, diff1 = 0.0, diff2 = 0.0;
		int i, j, trip1, trip2;
		subPathVars var = null;

		i = path.get(path.size() - 1);
		j = path.get(0);
		for (int p = 0; p <= path.size() - 2; p++) {
			trip1 = corrections_list[m * i + j].get(0).to();
			trip2 = corrections_list[m * i + j].get(0).from();
			// there is a path i, corrections_list[m * i + j].get(0).from(),...,
			// corrections_list[m * i + j].get(0).to(), j
			if (c[trip1][i] < 0 || c[trip1][j] < 0)
				System.out.println("Negativ +: " + c[trip1][i] + " Negativ -: " + c[trip1][j]);
			diff1 = c[trip1][i] - c[trip1][j];

			if (c[j][trip2] < 0 || c[i][trip2] < 0)
				System.out.println("Negativ +: " + c[j][trip2] + " Negativ -: " + c[i][trip2]);
			diff2 = c[j][trip2] - c[i][trip2];

			if (diff1 < diff2) {
				sum += diff1;
				var = new subPathVars(trip1, j);
				vars.remove(var);
				var = new subPathVars(trip1, i);
				vars.add(var);
			} else {
				sum += diff2;
				var = new subPathVars(i, trip2);
				vars.remove(var);
				var = new subPathVars(j, trip2);
				vars.add(var);
			}
			i = path.get(p);
			j = path.get(p + 1);
		}

		trip1 = corrections_list[m * i + j].get(0).to();
		if (c[trip1][i] < 0 || c[trip1][j] < 0)
			System.out.println("Negativ +: " + c[trip1][i] + " Negativ -: " + c[trip1][j]);
		diff1 = c[trip1][i] - c[trip1][j];

		trip2 = corrections_list[m * i + j].get(0).from();
		if (c[j][trip2] < 0 || c[i][trip2] < 0)
			System.out.println("Negativ +: " + c[j][trip2] + " Negativ -: " + c[i][trip2]);
		diff2 = c[j][trip2] - c[i][trip2];

		if (diff1 < diff2) {
			sum += diff1;
			var = new subPathVars(trip1, j);
			vars.remove(var);
			var = new subPathVars(trip1, i);
			vars.add(var);
		} else {
			sum += diff2;
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

	public static double cycleCorrections3(ArrayList<subPathVars> vars, ArrayList<subPathVars>[] corrections_list,
			ArrayList<Integer> path) {
		// twisting cycles
		double sum1 = 0.0, sum2 = 0.0, sum = 0.0, diff1 = 0.0, diff2 = 0.0, bound = 1e20, gain = 0.0;
		int i, j, k, h, current_trip = -1, trip, trip_in, trip_out, trip1, trip2, path_len;
		boolean flag = false;

		path_len = path.size();
		h = 0;
		i = path.get(path.size() - 1);
		j = path.get(h);
		k = path.get(h + 1);

		trip1 = corrections_list[m * i + j].get(0).to();
		trip_in = j;
		trip_out = corrections_list[m * j + k].get(0).from();

		while (path_len > 2) {
			while (trip_out != -1) {
				if (c[trip1][trip_out] > 0.0 && c[trip_in][j] > 0.0) {
					gain = c[trip1][trip_out] - c[trip1][j] + c[trip_in][j] - c[trip_in][trip_out];
					if (gain < bound) {
						flag = true;
						current_trip = trip_out;
						bound = gain;
					}
				} // else
					// System.out.println("Neg:" + c[trip1][trip_out] + "||Neg:" + c[trip_in][j]);
				trip_in = trip_out;
				trip_out = next[trip_in];
			}
			// System.out.println("Flag = " + flag);
			if (flag) {
				sum += bound;
				// next[trip1] = next[current_trip]; // optional;
				path_len--;
				flag = false;
				bound = 1e20;
				h++;
				if (trip_out >= m)
					trip1 = corrections_list[m * j + k].get(0).to();
				j = path.get(h);
				k = path.get(h + 1);
				trip_in = j;
				trip_out = corrections_list[m * j + k].get(0).from();
			} else {
				System.out.println("Flag = " + flag);
				break;
			}
		}

		// path_len == 1 (in fact is a length 2 cycle or a pair o symmetric arcs):

		while (trip_out != -1) {
			if (c[trip1][trip_out] > 0.0 && c[trip_in][j] > 0.0) {
				gain = c[trip1][trip_out] - c[trip1][j] + c[trip_in][j] - c[trip_in][trip_out];
				if (gain < bound) {
					flag = true;
					current_trip = trip_out;
					bound = gain;
				}
			} // else
				// System.out.println("Neg:" + c[trip1][trip_out] + "||Neg:" + c[trip_in][j]);
			trip_in = trip_out;
			trip_out = next[trip_in];
		}
		if (flag) {
			sum += bound;
			// next[trip1] = next[current_trip]; // optional;
		} else
			System.out.println("Flag = " + flag);

		i = path.get(path.size() - 1);
		j = path.get(0);
		for (int p = 0; p <= path.size() - 2; p++) {
			trip1 = corrections_list[m * i + j].get(0).to();
			trip2 = corrections_list[m * i + j].get(0).from();
			// there is a path i, corrections_list[m * i + j].get(0).from(),...,
			// corrections_list[m * i + j].get(0).to(), j
			if (c[trip1][i] < 0 || c[trip1][j] < 0)
				System.out.println("Negativ +: " + c[trip1][i] + " Negativ -: " + c[trip1][j]);
			diff1 = c[trip1][i] - c[trip1][j];

			if (c[j][trip2] < 0 || c[i][trip2] < 0)
				System.out.println("Negativ +: " + c[j][trip2] + " Negativ -: " + c[i][trip2]);
			diff2 = c[j][trip2] - c[i][trip2];

			if (diff1 < diff2)
				sum1 += diff1;
			else
				sum1 += diff2;
			i = path.get(p);
			j = path.get(p + 1);
		}

		trip1 = corrections_list[m * i + j].get(0).to();
		if (c[trip1][i] < 0 || c[trip1][j] < 0)
			System.out.println("Negativ +: " + c[trip1][i] + " Negativ -: " + c[trip1][j]);
		diff1 = c[trip1][i] - c[trip1][j];

		trip2 = corrections_list[m * i + j].get(0).from();
		if (c[j][trip2] < 0 || c[i][trip2] < 0)
			System.out.println("Negativ +: " + c[j][trip2] + " Negativ -: " + c[i][trip2]);
		diff2 = c[j][trip2] - c[i][trip2];

		if (diff1 < diff2)
			sum1 += diff1;
		else
			sum1 += diff2;

		/* */
		i = path.get(path.size() - 1);
		j = path.get(0);
		for (int p = 0; p <= path.size() - 2; p++) {
			corrections_list[m * i + j].remove(0);
			i = path.get(p);
			j = path.get(p + 1);
		}
		corrections_list[m * i + j].remove(0);

		// System.out.println("sum = " + sum + " || sum1 = " + sum1);
		return sum;
	}

	public static double cycleCorrectionsSimplified3(ArrayList<subPathVars> vars,
			ArrayList<subPathVars>[] corrections_list, ArrayList<Integer> path) {
		// twisting cycles
		double sum = 0.0, bound = 1e20, gain = 0.0;
		int i, j, k, h, current_trip_in = -1, current_trip_out = -1, trip_in, trip_out, trip1, trip2, path_len;
		boolean flag = false;
		subPathVars var = null;

		path_len = path.size();
		h = 0;
		i = path.get(path.size() - 1);
		j = path.get(h);
		k = path.get(h + 1);

		trip1 = corrections_list[m * i + j].get(0).to();
		trip_in = j;
		trip_out = corrections_list[m * j + k].get(0).from();

		while (path_len > 2) {
			while (trip_out != -1) {
				if (c[trip1][trip_out] > 0.0 && c[trip_in][j] > 0.0) {
					gain = c[trip1][trip_out] - c[trip1][j] + c[trip_in][j] - c[trip_in][trip_out];
					if (gain < bound) {
						flag = true;
						current_trip_out = trip_out;
						current_trip_in = trip_in;
						bound = gain;
					}
				} // else
					// System.out.println("Neg:" + c[trip1][trip_out] + "||Neg:" + c[trip_in][j]);
				trip_in = trip_out;
				trip_out = next[trip_in];
			}
			// System.out.println("Flag = " + flag);
			if (flag) {
				sum += bound;
				var = new subPathVars(current_trip_in, current_trip_out);
				vars.remove(var);
				var = new subPathVars(trip1, j);
				vars.remove(var);
				var = new subPathVars(trip1, current_trip_out);
				vars.add(var);
				var = new subPathVars(current_trip_in, j);
				vars.add(var);
				// next[trip1] = next[current_trip]; // optional;
				path_len--;
				flag = false;
				bound = 1e20;
				h++;
				if (trip_out >= m)
					trip1 = corrections_list[m * j + k].get(0).to();
				j = path.get(h);
				k = path.get(h + 1);
				trip_in = j;
				trip_out = corrections_list[m * j + k].get(0).from();
			} else {
				System.out.println("Flag = " + flag);
				break;
			}
		}

		// path_len == 1 (in fact is a length 2 cycle or a pair o symmetric arcs):

		while (trip_out != -1) {
			if (c[trip1][trip_out] > 0.0 && c[trip_in][j] > 0.0) {
				gain = c[trip1][trip_out] - c[trip1][j] + c[trip_in][j] - c[trip_in][trip_out];
				if (gain < bound) {
					flag = true;
					current_trip_out = trip_out;
					current_trip_in = trip_in;
					bound = gain;
				}
			} // else
				// System.out.println("Neg:" + c[trip1][trip_out] + "||Neg:" + c[trip_in][j]);
			trip_in = trip_out;
			trip_out = next[trip_in];
		}
		if (flag) {
			sum += bound;
			// next[trip1] = next[current_trip]; // optional;
		} else
			System.out.println("Flag = " + flag);

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

	public static double cycleCorrections33(ArrayList<subPathVars>[] corrections_list, ArrayList<Integer> path) {
		// twisting cycles
		double sum = 0.0, bound = 1e20, gain = 0.0;
		int index, i, j, k, h, current_trip = -1, trip_in, trip_out, trip1, path_len;
		boolean flag = false;
		int[] paths = new int[path.size()];

		path_len = path.size();
		h = 0;
		i = path.get(path.size() - 1);
		j = path.get(h);
		k = path.get(h + 1);

		// System.out.println("Random 0 = " + (new Random()).nextInt(1));
		index = corrections_list[m * i + j].size();
		if (index > 0)
			index = (new Random()).nextInt(corrections_list[m * i + j].size());
		else
			index = 0;
		paths[path_len - 1] = index;

		index = corrections_list[m * j + k].size();
		if (index > 0)
			index = (new Random()).nextInt(corrections_list[m * j + k].size());
		else
			index = 0;
		paths[h] = index;

		trip1 = corrections_list[m * i + j].get(paths[path_len - 1]).to();
		trip_in = j;
		trip_out = corrections_list[m * j + k].get(paths[h]).from();

		while (path_len > 2) {
			while (trip_out != -1) {
				if (c[trip1][trip_out] > 0.0 && c[trip_in][j] > 0.0) {
					gain = c[trip1][trip_out] - c[trip1][j] + c[trip_in][j] - c[trip_in][trip_out];
					if (gain < bound) {
						flag = true;
						current_trip = trip_out;
						bound = gain;
					}
				} // else
					// System.out.println("Neg:" + c[trip1][trip_out] + "||Neg:" + c[trip_in][j]);
				trip_in = trip_out;
				trip_out = next[trip_in];
			}
			// System.out.println("Flag = " + flag);
			if (flag) {
				sum += bound;
				// next[trip1] = next[current_trip]; // optional;
				path_len--;
				flag = false;
				bound = 1e20;
				if (current_trip >= m)
					trip1 = corrections_list[m * j + k].get(paths[h]).to();
				h++;
				j = path.get(h);
				k = path.get(h + 1);
				trip_in = j;

				index = corrections_list[m * j + k].size();
				if (index > 0)
					index = (new Random()).nextInt(corrections_list[m * j + k].size());
				else
					index = 0;
				paths[h] = index;
				trip_out = corrections_list[m * j + k].get(paths[h]).from();
			} else {
				System.out.println("Flag = " + flag);
				break;
			}
		}

		// path_len == 2 (in fact is a length 2 cycle or a pair o symmetric arcs):

		while (trip_out != -1) {
			if (c[trip1][trip_out] > 0.0 && c[trip_in][j] > 0.0) {
				gain = c[trip1][trip_out] - c[trip1][j] + c[trip_in][j] - c[trip_in][trip_out];
				if (gain < bound) {
					flag = true;
					current_trip = trip_out;
					bound = gain;
				}
			} // else
				// System.out.println("Neg:" + c[trip1][trip_out] + "||Neg:" + c[trip_in][j]);
			trip_in = trip_out;
			trip_out = next[trip_in];
		}
		if (flag) {
			sum += bound;
			// next[trip1] = next[current_trip]; // optional;
		} else
			System.out.println("Flag = " + flag);

		// for (int s = 0; s < path.size(); s++)
		// System.out.println(" paths[" + s + "] = " + paths[s]);

		/*
		 * path_len = path.size(); i = path.get(path.size() - 1); // path.get(0); j =
		 * path.get(0);// path.get(1); index = paths[path_len - 1];
		 * 
		 * for (int p = 0; p <= path_len - 2; p++) { trip1 = corrections_list[m * i +
		 * j].get(index).to(); trip2 = corrections_list[m * i + j].get(index).from(); if
		 * (c[trip1][i] < 0 || c[trip1][j] < 0) System.out.println("Negativ +: " +
		 * c[trip1][i] + " Negativ -: " + c[trip1][j]); diff1 = c[trip1][i] -
		 * c[trip1][j]; if (c[j][trip2] < 0 || c[i][trip2] < 0)
		 * System.out.println("Negativ +: " + c[j][trip2] + " Negativ -: " +
		 * c[i][trip2]); diff2 = c[j][trip2] - c[i][trip2]; if (diff1 < diff2) sum1 +=
		 * diff1; else sum1 += diff2; i = path.get(p); j = path.get(p + 1); index =
		 * paths[p]; }
		 * 
		 * trip1 = corrections_list[m * i + j].get(index).to(); trip2 =
		 * corrections_list[m * i + j].get(index).from(); if (c[trip1][i] < 0 ||
		 * c[trip1][j] < 0) System.out.println("Negativ +: " + c[trip1][i] +
		 * " Negativ -: " + c[trip1][j]); diff1 = c[trip1][i] - c[trip1][j]; if
		 * (c[j][trip2] < 0 || c[i][trip2] < 0) System.out.println("Negativ +: " +
		 * c[j][trip2] + " Negativ -: " + c[i][trip2]); diff2 = c[j][trip2] -
		 * c[i][trip2]; if (diff1 < diff2) sum1 += diff1; else sum1 += diff2;
		 */
		/* */
		i = path.get(path_len - 1);
		j = path.get(0);
		index = paths[path_len - 1];
		for (int p = 0; p <= path_len - 2; p++) {
			corrections_list[m * i + j].remove(index);
			i = path.get(p);
			j = path.get(p + 1);
			index = paths[p];
		}
		corrections_list[m * i + j].remove(index);

		// System.out.println("sum = " + sum + " || sum1 = " + sum1);
		return sum;
	}

	public static double cycleCorrections4(ArrayList<subPathVars>[] corrections_list, ArrayList<Integer> path) {
		// twisting cycles
		double sum1 = 0.0, sum = 0.0, diff1 = 0.0, diff2 = 0.0, bound = 1e20, gain = 0.0;
		int index, i, j, k, h, current_trip = -1, trip_in, trip_out, trip1, trip2, path_len;
		boolean flag = false;
		int[] paths = new int[path.size()];

		path_len = path.size();
		h = 0;
		i = path.get(path.size() - 1);
		j = path.get(h);
		k = path.get(h + 1);

		System.out.println("Random 0 = " + (new Random()).nextInt(1));
		index = corrections_list[m * i + j].size();
		if (index > 0)
			index = (new Random()).nextInt(corrections_list[m * i + j].size());
		else
			index = 0;
		paths[path_len - 1] = index;

		index = corrections_list[m * j + k].size();
		if (index > 0)
			index = (new Random()).nextInt(corrections_list[m * j + k].size());
		else
			index = 0;
		paths[h] = index;

		trip1 = corrections_list[m * i + j].get(paths[path_len - 1]).to();
		trip_in = j;
		trip_out = corrections_list[m * j + k].get(paths[h]).from();

		while (path_len > 2) {
			while (trip_out != -1) {
				if (c[trip1][trip_out] > 0.0 && c[trip_in][j] > 0.0) {
					gain = c[trip1][trip_out] - c[trip1][j] + c[trip_in][j] - c[trip_in][trip_out];
					if (gain < bound) {
						flag = true;
						current_trip = trip_out;
						bound = gain;
					}
				} else
					System.out.println("Neg:" + c[trip1][trip_out] + "||Neg:" + c[trip_in][j]);
				trip_in = trip_out;
				trip_out = next[trip_in];
			}
			System.out.println("Flag = " + flag);
			if (flag) {
				sum += bound;
				// next[trip1] = next[current_trip]; // optional;
				path_len--;
				flag = false;
				bound = 1e20;
				if (current_trip >= m)
					trip1 = corrections_list[m * j + k].get(paths[h]).to();
				h++;
				j = path.get(h);
				k = path.get(h + 1);
				trip_in = j;

				index = corrections_list[m * j + k].size();
				if (index > 0)
					index = (new Random()).nextInt(corrections_list[m * j + k].size());
				else
					index = 0;
				paths[h] = index;
				trip_out = corrections_list[m * j + k].get(paths[h]).from();
			} else {
				System.out.println("Flag = " + flag);
				break;
			}
		}

		// path_len == 2 (in fact is a length 2 cycle or a pair o symmetric arcs):

		while (trip_out != -1) {
			if (c[trip1][trip_out] > 0.0 && c[trip_in][j] > 0.0) {
				gain = c[trip1][trip_out] - c[trip1][j] + c[trip_in][j] - c[trip_in][trip_out];
				if (gain < bound) {
					flag = true;
					current_trip = trip_out;
					bound = gain;
				}
			} else
				System.out.println("Neg:" + c[trip1][trip_out] + "||Neg:" + c[trip_in][j]);
			trip_in = trip_out;
			trip_out = next[trip_in];
		}
		if (flag) {
			sum += bound;
			// next[trip1] = next[current_trip]; // optional;
		} else
			System.out.println("Flag = " + flag);

		for (int s = 0; s < path.size(); s++)
			System.out.println(" paths[" + s + "] = " + paths[s]);

		path_len = path.size();
		i = path.get(path.size() - 1); // path.get(0);
		j = path.get(0);// path.get(1);
		index = paths[path_len - 1];

		for (int p = 0; p <= path_len - 2; p++) {
			trip1 = corrections_list[m * i + j].get(index).to();
			trip2 = corrections_list[m * i + j].get(index).from();
			if (c[trip1][i] < 0 || c[trip1][j] < 0)
				System.out.println("Negativ +: " + c[trip1][i] + " Negativ -: " + c[trip1][j]);
			diff1 = c[trip1][i] - c[trip1][j];
			if (c[j][trip2] < 0 || c[i][trip2] < 0)
				System.out.println("Negativ +: " + c[j][trip2] + " Negativ -: " + c[i][trip2]);
			diff2 = c[j][trip2] - c[i][trip2];
			if (diff1 < diff2)
				sum1 += diff1;
			else
				sum1 += diff2;
			i = path.get(p);
			j = path.get(p + 1);
			index = paths[p];
		}

		trip1 = corrections_list[m * i + j].get(index).to();
		trip2 = corrections_list[m * i + j].get(index).from();
		if (c[trip1][i] < 0 || c[trip1][j] < 0)
			System.out.println("Negativ +: " + c[trip1][i] + " Negativ -: " + c[trip1][j]);
		diff1 = c[trip1][i] - c[trip1][j];
		if (c[j][trip2] < 0 || c[i][trip2] < 0)
			System.out.println("Negativ +: " + c[j][trip2] + " Negativ -: " + c[i][trip2]);
		diff2 = c[j][trip2] - c[i][trip2];
		if (diff1 < diff2)
			sum1 += diff1;
		else
			sum1 += diff2;

		/* */
		i = path.get(path_len - 1);
		j = path.get(0);
		index = paths[path_len - 1];
		for (int p = 0; p <= path_len - 2; p++) {
			corrections_list[m * i + j].remove(index);
			i = path.get(p);
			j = path.get(p + 1);
			index = paths[p];
		}
		corrections_list[m * i + j].remove(index);

		System.out.println("sum = " + sum + " || sum1 = " + sum1);
		return sum;
	}

	public static double cycleCorrections5(ArrayList<subPathVars>[] corrections_list, ArrayList<Integer> path) {
		// twisting cycles, UNFINISHED
		double sum = 0.0, diff1 = 0.0, diff2 = 0.0;
		int i, j, k, trip1, trip2;
		// boolean flag = true;

		i = path.get(path.size() - 1);
		j = path.get(0);
		k = path.get(path.size() - 2);

		trip1 = corrections_list[m * i + j].get(0).to();
		trip2 = corrections_list[m * j + k].get(0).from();

		while (next[trip2] != -1) {
			trip2 = next[trip2];
			if (c[trip1][trip2] > 0.0) {

			}
		}

		for (int p = 0; p <= path.size() - 2; p++) {
			trip1 = corrections_list[m * i + j].get(0).to();
			trip2 = corrections_list[m * i + j].get(0).from();
			// there is a path i, corrections_list[m * i + j].get(0).from(),...,
			// corrections_list[m * i + j].get(0).to(), j
			if (c[trip1][i] < 0 || c[trip1][j] < 0)
				System.out.println("Negativ +: " + c[trip1][i] + " Negativ -: " + c[trip1][j]);
			diff1 = c[trip1][i] - c[trip1][j];

			if (c[j][trip2] < 0 || c[i][trip2] < 0)
				System.out.println("Negativ +: " + c[j][trip2] + " Negativ -: " + c[i][trip2]);
			diff2 = c[j][trip2] - c[i][trip2];

			if (diff1 < diff2)
				sum += diff1;
			else
				sum += diff2;
			i = path.get(p);
			j = path.get(p + 1);
		}

		trip1 = corrections_list[m * i + j].get(0).to();
		if (c[trip1][i] < 0 || c[trip1][j] < 0)
			System.out.println("Negativ +: " + c[trip1][i] + " Negativ -: " + c[trip1][j]);
		diff1 = c[trip1][i] - c[trip1][j];

		trip2 = corrections_list[m * i + j].get(0).from();
		if (c[j][trip2] < 0 || c[i][trip2] < 0)
			System.out.println("Negativ +: " + c[j][trip2] + " Negativ -: " + c[i][trip2]);
		diff2 = c[j][trip2] - c[i][trip2];

		if (diff1 < diff2)
			sum += diff1;
		else
			sum += diff2;

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

	public static double cycleCorrections6(ArrayList<subPathVars>[] corrections_list, ArrayList<Integer> path) {
		// JUST FOR COMPARATION PURPOSES (CORRECTIONS 2 AND 3 METHODS)
		double sum1 = 0.0, sum2 = 0.0, sum = 0.0, diff1 = 0.0, diff2 = 0.0;
		int i, j, trip, trip1, trip2;

		i = path.get(path.size() - 1);
		j = path.get(0);
		for (int p = 0; p <= path.size() - 2; p++) {
			trip1 = corrections_list[m * i + j].get(0).to();
			trip2 = corrections_list[m * i + j].get(0).from();
			// there is a path i, corrections_list[m * i + j].get(0).from(),...,
			// corrections_list[m * i + j].get(0).to(), j
			if (c[trip1][i] < 0 || c[trip1][j] < 0)
				System.out.println("Negativ +: " + c[trip1][i] + " Negativ -: " + c[trip1][j]);
			diff1 = c[trip1][i] - c[trip1][j];

			if (c[j][trip2] < 0 || c[i][trip2] < 0)
				System.out.println("Negativ +: " + c[j][trip2] + " Negativ -: " + c[i][trip2]);
			diff2 = c[j][trip2] - c[i][trip2];

			if (diff1 < diff2)
				sum += diff1;
			else
				sum += diff2;
			i = path.get(p);
			j = path.get(p + 1);
		}

		trip1 = corrections_list[m * i + j].get(0).to();
		if (c[trip1][i] < 0 || c[trip1][j] < 0)
			System.out.println("Negativ +: " + c[trip1][i] + " Negativ -: " + c[trip1][j]);
		diff1 = c[trip1][i] - c[trip1][j];

		trip2 = corrections_list[m * i + j].get(0).from();
		if (c[j][trip2] < 0 || c[i][trip2] < 0)
			System.out.println("Negativ +: " + c[j][trip2] + " Negativ -: " + c[i][trip2]);
		diff2 = c[j][trip2] - c[i][trip2];

		if (diff1 < diff2)
			sum += diff1;
		else
			sum += diff2;

		/* */

		i = path.get(path.size() - 1);
		j = path.get(0);
		for (int p = 0; p <= path.size() - 2; p++) {
			trip = corrections_list[m * i + j].get(0).to();
			// there is a path i, corrections_list[m * i + j].get(0).from(),...,
			// corrections_list[m * i + j].get(0).to(), j
			if (c[trip][i] < 0 || c[trip][j] < 0)
				System.out.println("Negativ +: " + c[trip][i] + " Negativ -: " + c[trip][j]);
			sum1 += c[trip][i] - c[trip][j];
			i = path.get(p);
			j = path.get(p + 1);
		}
		trip = corrections_list[m * i + j].get(0).to();
		if (c[trip][i] < 0 || c[trip][j] < 0)
			System.out.println("Negativ +: " + c[trip][i] + " Negativ -: " + c[trip][j]);
		sum1 += c[trip][i] - c[trip][j];
		/* */
		i = path.get(path.size() - 1);
		j = path.get(0);
		for (int p = 0; p <= path.size() - 2; p++) {
			trip = corrections_list[m * i + j].get(0).from();
			if (c[j][trip] < 0 || c[i][trip] < 0)
				System.out.println("Negativ +: " + c[j][trip] + " Negativ -: " + c[i][trip]);
			sum2 += c[j][trip] - c[i][trip];
			i = path.get(p);
			j = path.get(p + 1);
		}
		trip = corrections_list[m * i + j].get(0).from();
		if (c[j][trip] < 0 || c[i][trip] < 0)
			System.out.println("Negativ +: " + c[j][trip] + " Negativ -: " + c[i][trip]);
		sum2 += c[j][trip] - c[i][trip];

		System.out.println(" sum1 = " + sum1 + " || sum2 = " + sum2 + " || sum = " + sum);
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
					if (c[h][k] < 0)
						System.out.println(" negativ: " + c[h][k]);
					suma += valoare * c[h][k];
				}
			}
			System.out.println();
			System.out.println("suma = " + suma + " optim = " + model.get(GRB.DoubleAttr.ObjVal));
		} catch (

		GRBException e) {
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
			if (i == j || Math.abs(valoare) > precision_b || Math.abs(1.0 - valoare) > precision_b) {
				return variabile[k];
			}
		}
		return null;
	}

	public static void addIntegerConstraints() throws GRBException {
		GRBVar[] variabile = model.getVars();
		for (int p = 0; p < variabile.length; p++) {
			variabile[p].set(GRB.CharAttr.VType, 'I');
		}
		model.update();
	}

	public static void addContinuousConstraints() throws GRBException {
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

		for (int h = 0; h < n + m; h++)
			next[h] = -1;

		for (int h = 0; h < m; h++)
			s_list[h] = new ArrayList<Integer>();

		for (int k = 0; k < vars.length; k++) {
			nume = vars[k].get(StringAttr.VarName);
			val = vars[k].get(GRB.DoubleAttr.X);
			if (val > 0) {
				i = Integer.valueOf(nume.substring(nume.indexOf('x') + 1, nume.indexOf('y')));
				j = Integer.valueOf(nume.substring(nume.indexOf('y') + 1));
				if (i != j) {
					if (i < m)
						s_list[i].add(j);
					else
						next[i] = j;
				}
			}
		}

		for (int h = 0; h < m; h++)
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
		model.update();
		return out;
	}

	public static void showSubtours(GRBModel model, GRBVar[] vars, int m, int n) throws GRBException {

		double val = 0;
		int i = 0, j = 0, from = -1, to = -1;
		int[] next = new int[n + m];
		String nume = "";
		List<Integer>[] s_list = new ArrayList[m];

		for (int h = 0; h < n + m; h++)
			next[h] = -1;

		for (int h = 0; h < m; h++)
			s_list[h] = new ArrayList<Integer>();

		for (int k = 0; k < vars.length; k++) {
			nume = vars[k].get(StringAttr.VarName);
			val = vars[k].get(GRB.DoubleAttr.X);
			if (val > 0) {
				i = Integer.valueOf(nume.substring(nume.indexOf('x') + 1, nume.indexOf('y')));
				j = Integer.valueOf(nume.substring(nume.indexOf('y') + 1));
				if (i != j) {
					if (i < m)
						s_list[i].add(j);
					else
						next[i] = j;
				}
			}
		}
		for (int h = 0; h < m; h++)
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

	public static ArrayList<subPathVars>[] repairSubtours() throws GRBException {
		GRBVar[] vars = model.getVars();
		next = new int[n + m];
		double val = 0;
		int i = 0, j = 0, from = -1, to = -1, to1 = -1;
		int[][] graph = new int[m][m];
		String nume = "";
		ArrayList<subPathVars>[] corrections_list = (ArrayList<subPathVars>[]) new ArrayList[m * m + m];
		List<Integer>[] s_list = new ArrayList[m];

		for (int h = 0; h < n + m; h++)
			next[h] = -1;
		for (int h = 0; h < m; h++)
			s_list[h] = new ArrayList<Integer>();
		for (int k = 0; k < vars.length; k++) {
			nume = vars[k].get(StringAttr.VarName);
			val = vars[k].get(GRB.DoubleAttr.X);
			// System.out.println("val = "+ val);
			if (val > 0) {
				i = Integer.valueOf(nume.substring(nume.indexOf('x') + 1, nume.indexOf('y')));
				j = Integer.valueOf(nume.substring(nume.indexOf('y') + 1));
				if (i != j) {
					if (i < m)
						s_list[i].add(j);
					else
						next[i] = j;
				}
			}
		}

		for (int h = 0; h < m; h++)
			if (!s_list[h].isEmpty()) {
				for (int p = 0; p < s_list[h].size(); p++) {
					from = h;
					to = s_list[h].get(p);
					while (next[to] != -1)
						to = next[to];
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
		return corrections_list;
	}

	public static int[][] graphSubtours() throws GRBException {
		GRBVar[] vars = model.getVars();
		double val = 0;
		int i = 0, j = 0, from = -1, to = -1, constrLength;
		int[] next = new int[n + m];
		int[][] graph = new int[m][m];
		String nume = "";
		List<Integer>[] s_list = new ArrayList[m];

		for (int h = 0; h < n + m; h++)
			next[h] = -1;

		for (int h = 0; h < m; h++)
			s_list[h] = new ArrayList<Integer>();

		for (int k = 0; k < vars.length; k++) {
			nume = vars[k].get(StringAttr.VarName);
			val = vars[k].get(GRB.DoubleAttr.X);
			if (val > 0) {
				i = Integer.valueOf(nume.substring(nume.indexOf('x') + 1, nume.indexOf('y')));
				j = Integer.valueOf(nume.substring(nume.indexOf('y') + 1));
				if (i != j) {
					if (i < m)
						s_list[i].add(j);
					else
						next[i] = j;
				}
			}
		}
		for (int h = 0; h < m; h++)
			if (!s_list[h].isEmpty()) {
				for (int p = 0; p < s_list[h].size(); p++) {
					from = h;
					to = s_list[h].get(p);
					while (next[to] != -1) {
						to = next[to];
					}
					if (h != to)
						graph[h][to]++;
				}
			}
		return graph;
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
			if (varr[j].get(GRB.DoubleAttr.X) != 0) {
				nume = varr[j].get(StringAttr.VarName);
				h = Integer.valueOf(nume.substring(nume.indexOf('x') + 1, nume.indexOf('y')));
				k = Integer.valueOf(nume.substring(nume.indexOf('y') + 1));
				var = new subPathVars(h, k);
				verify_out[h]++;
				verify_in[k]++;
				list.add(var);
			}
		}

		for (int j = 0; j < n + m; j++)
			if (verify_in[j] != verify_out[j]) {
				System.out.println(" *** SOLUTIE GRESITA *** ");
				System.out.println("in = " + verify_in[j]);
				System.out.println("out = " + verify_out[j]);
			}
		System.out.println(" *** SOLUTIE CORECTA *** ");
		return list;
	}

	public static void solutionToFile(String dataFile) throws GRBException {
		if (dataFile == "-1") {
			System.out.println("Usage: input filename");
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

	public static void heuristic1(String dataFile, double gap) {
		// Repair the MIP solution in the most simple way
		// gap is the MIP gap absolute value
		if (dataFile == "-1") {
			System.out.println("Usage: input filename");
			System.exit(1);
		}
		double opt = 0, corr_opt = 0, kn_opt = 0;
		String dataFileInt1 = "Int1_" + dataFile;
		String lpDataFile = convert(dataFile, "");
		kn_opt = instances[inst][(int) (0.75 * m - 3.0 + n / 500.0 - 1.0)];
		ArrayList<subPathVars>[] corrected_list;
		try {
			env = new GRBEnv();
			model = new GRBModel(env, "../data/results/" + lpDataFile);
			model.update();
			// model.set(StringParam., newval);
			model.set(IntParam.OutputFlag, 0);
			model.optimize();
			Bellman_Digraph();
			solutionToFile(dataFile);

			addIntegerConstraints();
			if (gap == 0.0)
				model.set(DoubleParam.MIPGapAbs, gap);
			model.set(IntParam.OutputFlag, 0);
			model.optimize();
			corrected_list = repairSubtours();
			opt = model.get(GRB.DoubleAttr.ObjVal);
			solutionToFile(dataFileInt1);
			verify();
			corr_opt = opt + solRepair1(dataFile, corrected_list, graphSubtours());
			System.out.println("found=" + corr_opt + "/known=" + kn_opt + "/rel_error=" + (corr_opt - kn_opt) / kn_opt);
		} catch (GRBException e) {
			System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
		}
	}

	public static void heuristic2(String dataFile, double gap) {
		// Repair the MIP solution in the most simple way
		if (dataFile == "-1") {
			System.out.println("Usage: input filename");
			System.exit(1);
		}
		double opt = 0, corr_opt = 0, kn_opt = 0;
		String dataFileInt1 = "Int1_" + dataFile;
		String lpDataFile = convert(dataFile, "");
		kn_opt = instances[inst][(int) (0.75 * m - 3.0 + n / 500.0 - 1.0)];
		ArrayList<subPathVars>[] corrected_list;
		try {
			env = new GRBEnv();
			model = new GRBModel(env, "../data/results/" + lpDataFile);
			model.update();
			// model.set(StringParam., newval);
			model.set(IntParam.OutputFlag, 0);
			model.optimize();
			Bellman_Digraph();
			solutionToFile(dataFile);

			addIntegerConstraints();
			if (gap == 0.0)
				model.set(DoubleParam.MIPGapAbs, 0);
			model.set(IntParam.OutputFlag, 0);
			model.optimize();
			corrected_list = repairSubtours();
			opt = model.get(GRB.DoubleAttr.ObjVal);
			solutionToFile(dataFileInt1);
			verify();
			corr_opt = opt + solRepair2(dataFile, corrected_list, graphSubtours());
			System.out.println("found=" + corr_opt + "/known=" + kn_opt + "/rel_error=" + (corr_opt - kn_opt) / kn_opt);
		} catch (GRBException e) {
			System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
		}
	}

	public static void heuristicCombined(String dataFile) {
		// After repairing an solution add the constraints corresponding to the wrong
		// subtours to the problem and solve again the MIP
		if (dataFile == "-1") {
			System.out.println("Usage: input filename");
			System.exit(1);
		}
		double opt = 0, corr_opt = 0, kn_opt = 0;
		String dataFileInt1 = "Int1_" + dataFile;
		String lpDataFile = convert(dataFile, "");
		kn_opt = instances[inst][(int) (0.75 * m - 3.0 + n / 500.0 - 1.0)];
		ArrayList<subPathVars>[] corrected_list;
		try {
			env = new GRBEnv();
			model = new GRBModel(env, "../data/results/" + lpDataFile);
			model.update();
			// model.set(StringParam., newval);
			model.set(IntParam.OutputFlag, 0);
			model.optimize();
			Bellman_Digraph();
			solutionToFile(dataFile);

			System.out.println();
			System.out.println("****************** First optimization *****************");
			System.out.println();
			addIntegerConstraints();
			// model.set(DoubleParam.MIPGapAbs, 0);
			model.set(IntParam.OutputFlag, 0);
			model.optimize();
			corrected_list = repairSubtours();
			opt = model.get(GRB.DoubleAttr.ObjVal);
			solutionToFile(dataFileInt1);
			verify();
			corr_opt = opt + solRepairWithNewConstraints(dataFile, corrected_list, graphSubtours());
			System.out.println("found=" + corr_opt + "/known=" + kn_opt + "/rel_error=" + (corr_opt - kn_opt) / kn_opt);

			System.out.println();
			System.out.println("****************** Second optimization *****************");
			System.out.println();
			addIntegerConstraints();
			// model.set(DoubleParam.MIPGapAbs, 0);
			model.set(IntParam.OutputFlag, 0);
			model.optimize();
			corrected_list = repairSubtours();
			opt = model.get(GRB.DoubleAttr.ObjVal);
			solutionToFile(dataFileInt1);
			verify();
			corr_opt = opt + solRepairWithNewConstraints(dataFile, corrected_list, graphSubtours());
			System.out.println("found=" + corr_opt + "/known=" + kn_opt + "/rel_error=" + (corr_opt - kn_opt) / kn_opt);
		} catch (GRBException e) {
			System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
		}
	}

	public static void simple_heuristic(String dataFile, double gap, int repairType) {
		// Repair the MIP solution in the most simple way
		if (dataFile == "-1") {
			System.out.println("Usage: input filename");
			System.exit(1);
		}
		double opt = 0, corr_opt = 0, kn_opt = 0;
		String dataFileInt1 = "Int1_" + dataFile;
		String lpDataFile = convert(dataFile, "");
		kn_opt = instances[inst][(int) (0.75 * m - 3.0 + n / 500.0 - 1.0)];
		ArrayList<subPathVars>[] corrected_list;
		try {
			env = new GRBEnv();
			model = new GRBModel(env, "../data/results/" + lpDataFile);
			model.update();
			// model.set(StringParam., newval);
			if (gap == 0.0)
				model.set(DoubleParam.MIPGapAbs, 0);
			addIntegerConstraints();
			model.set(IntParam.OutputFlag, 0);
			model.optimize();
			Bellman_Digraph();
			solutionToFile(dataFile);

			corrected_list = repairSubtours();
			opt = model.get(GRB.DoubleAttr.ObjVal);
			solutionToFile(dataFileInt1);
			verify();
			checkValidity();

			corr_opt = opt + solRepair(dataFile, checkValidity(), corrected_list, graphSubtours(), repairType);
			System.out.println("found=" + corr_opt + "/known=" + kn_opt + "/rel_error=" + (corr_opt - kn_opt) / kn_opt);
		} catch (GRBException e) {
			System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
		}
	}

	public static void double_heuristic(String dataFile, double gap, int repairType) {
		// After repairing an solution add the constraints corresponding to the wrong
		// subtours to the problem and solve again the MIP
		if (dataFile == "-1") {
			System.out.println("Usage: input filename");
			System.exit(1);
		}
		double opt = 0, corr_opt = 0, kn_opt = 0;
		String dataFileInt1 = "Int1_" + dataFile;
		String lpDataFile = convert(dataFile, "");
		kn_opt = instances[inst][(int) (0.75 * m - 3.0 + n / 500.0 - 1.0)];
		ArrayList<subPathVars>[] corrected_list;
		try {
			env = new GRBEnv();
			model = new GRBModel(env, "../data/results/" + lpDataFile);
			model.update();
			// model.set(StringParam., newval);
			model.set(IntParam.OutputFlag, 0);
			model.optimize();
			Bellman_Digraph();
			solutionToFile(dataFile);

			System.out.println();
			System.out.println("****************** First optimization *****************");
			System.out.println();

			addIntegerConstraints();
			model.update();
			if (gap == 0.0)
				model.set(DoubleParam.MIPGapAbs, 0);
			model.set(IntParam.OutputFlag, 0);

			model.optimize();
			opt = model.get(GRB.DoubleAttr.ObjVal);
			System.out.println("Optimum2 = " + opt);
			solutionToFile(dataFileInt1);
			verify();

			checkValidity();

			// ArrayList<aax_SubPathVars>[] corrections_list0 =
			// cloneArrayList(corrections_list, m);
			corrected_list = repairSubtours();
			addAllCyclesConstraints1(corrected_list);

			corr_opt = opt + solRepair(dataFile, null, corrected_list, graphSubtours(), repairType);
			System.out.println("found=" + corr_opt + "/known=" + kn_opt + "/rel_error=" + (corr_opt - kn_opt) / kn_opt);

			System.out.println();
			System.out.println("****************** Second optimization *****************");
			System.out.println();

			addIntegerConstraints();
			model.update();
			if (gap == 0.0)
				model.set(DoubleParam.MIPGapAbs, 0);
			model.set(IntParam.OutputFlag, 0);
			model.optimize();

			corrected_list = repairSubtours();
			opt = model.get(GRB.DoubleAttr.ObjVal);
			System.out.println("Optimum2 = " + opt);
			solutionToFile(dataFileInt1);
			verify();

			checkValidity();

			corr_opt = opt + solRepair(dataFile, null, corrected_list, graphSubtours(), repairType);
			System.out.println("found=" + corr_opt + "/known=" + kn_opt + "/rel_error=" + (corr_opt - kn_opt) / kn_opt);
		} catch (GRBException e) {
			System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
		}
	}

	public static void heuristic(String dataFile, double gap, int repairType) {
		// Repair the MIP solution in the most simple way
		
		if (dataFile == "-1") {
			System.out.println("Usage: input filename");
			System.exit(1);
		}
		Map nn = new HashMap<Arc, ArrayList<Integer>>();
		double opt = 0, opt1 = 0, corr_opt = 0, kn_opt = 0;
		String dataFileInt1 = "Int1_" + dataFile;
		String lpDataFile = convert(dataFile, "");
		long start = System.nanoTime();
		kn_opt = instances[inst][(int) (0.75 * m - 3.0 + n / 500.0 - 1.0)];
		ArrayList<subPathVars>[] corrected_list;
		try {
			env = new GRBEnv();
			model = new GRBModel(env, "../data/results/" + lpDataFile);
			// addContinuousConstraints();
			model.update();
			// model.set(StringParam., newval);
			model.set(IntParam.OutputFlag, 0);
			addIntegerConstraints();
			model.optimize();
			System.out.println("First Optimum" + model.get(GRB.DoubleAttr.ObjVal));
			
			System.out.println("Execution time1 = " + (System.nanoTime() - start) * 1e-9);
			
			
			//model = new GRBModel(env, "../data/results/" + lpDataFile);
			model.set(IntParam.OutputFlag, 0);
			Bellman_Digraph();
			// solutionToFile(dataFile);

			// addIntegerConstraints();
			model.update();
			if (gap == 0.0)
				model.set(DoubleParam.MIPGapAbs, 0);
			model.set(IntParam.OutputFlag, 0);
			model.optimize();
			opt1 = model.get(GRB.DoubleAttr.ObjVal);
			System.out.println("opt1 (relaxed) = " + opt1);

			addIntegerConstraints();
			model.update();
			if (gap == 0.0)
				model.set(DoubleParam.MIPGapAbs, 0);
			model.set(IntParam.OutputFlag, 0);
			model.optimize();
			corrected_list = repairSubtours();
			opt = model.get(GRB.DoubleAttr.ObjVal);
			System.out.println("opt (milp) = " + opt);
			solutionToFile(dataFileInt1);
			// 3830912
			verify();
			addIntegerConstraints();
			model = new GRBModel(env, "../data/results/" + lpDataFile);
			model.set(IntParam.OutputFlag, 0);
			corr_opt = opt + solRepair(dataFile, checkValidity(), corrected_list, graphSubtours(), repairType);
			System.out.println("opt (corrected) = " + opt);
			System.out.println("found=" + corr_opt + "/known=" + kn_opt + "/rel_error=" + (corr_opt - kn_opt) / kn_opt);
		} catch (GRBException e) {
			System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
		}
	}

	public static void _heuristic(String dataFile, double gap, int repairType) {
		// Repair the MIP solution in the most simple way
		if (dataFile == "-1") {
			System.out.println("Usage: input filename");
			System.exit(1);
		}
		Map nn = new HashMap<Arc, ArrayList<Integer>>();
		double opt = 0, opt1 = 0, corr_opt = 0, kn_opt = 0;
		String dataFileInt1 = "Int1_" + dataFile;
		String lpDataFile = convert(dataFile, "");
		kn_opt = instances[inst][(int) (0.75 * m - 3.0 + n / 500.0 - 1.0)];
		ArrayList<subPathVars>[] corrected_list;

		try {
			env = new GRBEnv();
			model = new GRBModel(env, "../data/results/" + lpDataFile);
			// addContinuousConstraints();
			model.update();
			// model.set(StringParam., newval);
			model.set(IntParam.OutputFlag, 0);
			model.optimize();
			// Bellman_Digraph();
			Bellman_Digraph();
			// solutionToFile(dataFile);

			// addIntegerConstraints();
			model.update();
			if (gap == 0.0)
				model.set(DoubleParam.MIPGapAbs, 0);
			model.set(IntParam.OutputFlag, 0);
			model.optimize();
			opt1 = model.get(GRB.DoubleAttr.ObjVal);
			System.out.println("opt1 (relaxed) = " + opt1);

			addIntegerConstraints();
			model.update();
			if (gap == 0.0)
				model.set(DoubleParam.MIPGapAbs, 0);
			model.set(IntParam.OutputFlag, 0);
			model.optimize();
			corrected_list = repairSubtours();
			opt = model.get(GRB.DoubleAttr.ObjVal);
			System.out.println("opt (milp) = " + opt);
			solutionToFile(dataFileInt1);
			// 3830912
			verify();

			corr_opt = opt + solRepair(dataFile, checkValidity(), corrected_list, graphSubtours(), repairType);
			System.out.println("opt (corrected) = " + opt);
			System.out.println("found=" + corr_opt + "/known=" + kn_opt + "/rel_error=" + (corr_opt - kn_opt) / kn_opt);
		} catch (GRBException e) {
			System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
		}
	}

	public static void b_b(String dataFile, double gap, int repairType) {
		// Perform a Branch&Bound algorithm
		if (dataFile == "-1") {
			System.out.println("Usage: input filename");
			System.exit(1);
		}
		int n_max = 20, nr_problems = 1, i, j;
		double opt = 0, best_opt = 0, kn_opt = 0;
		String var_name, dataFileInt1 = "Int1_" + dataFile;
		String lpDataFile = convert(dataFile, "");
		kn_opt = instances[inst][(int) (0.75 * m - 3.0 + n / 500.0 - 1.0)];
		ArrayList<subPathVars>[] corrected_list;
		boolean found = false;
		try {
			Stack<GRBModel> stiva = new Stack<GRBModel>();
			env = new GRBEnv();
			GRBVar branch_var;
			GRBModel current_model, best_model = new GRBModel(env);
			//addIntegerConstraints();
			model = new GRBModel(env, "../data/results/" + lpDataFile);
			//model.set(IntParam.OutputFlag, 0);

			// solving the root problem:
			model.optimize();// solved with the equivalent column generation method
			//System.out.println("First Optimum" + model.get(GRB.DoubleAttr.ObjVal));
			// ArrayList<ArrayList<Integer>> list = Bellman_Digraph();
			solutionToFile(dataFile + "_a");

			// find an upper bound for the optimum in the MILP problem (minimization):
			addIntegerConstraints();
			model.update();
			if (gap == 0.0)
				model.set(DoubleParam.MIPGapAbs, 0);
			model.optimize();
			corrected_list = repairSubtours();
			solutionToFile(dataFileInt1);
			opt = model.get(GRB.DoubleAttr.ObjVal);
			best_opt = opt + solRepair(dataFile, checkValidity(), corrected_list, graphSubtours(), repairType);
			System.out.println("found=" + best_opt + "/known=" + kn_opt + "/rel_error=" + (best_opt - kn_opt) / kn_opt);
			// TODO: save this integer solution
			verify();
			System.out.println("Stiva");

			current_model = model;
			addContinuousConstraints();
			model.update();
			stiva.add(model);

			while (!stiva.isEmpty() && nr_problems < n_max) {
				current_model = stiva.pop();
				System.out.println("nr_problems = " + nr_problems);
				current_model.optimize(); // Bellman_Digraph(current_model, m, n); //
				System.out.println("proaddIntegerConstraints();\n" + 
						"			model = new GRBModel(env, \"../data/results/\" + lpDataFile);\n" + 
						"			model.set(IntParam.OutputFlag, 0);bl. " + nr_problems + "|optim = " + current_model.get(GRB.DoubleAttr.ObjVal));
				nr_problems++;
				System.out.println("O problema din stiva ");
				int optimstatus = current_model.get(GRB.IntAttr.Status);
				if (optimstatus != GRB.Status.INFEASIBLE && optimstatus != GRB.Status.UNBOUNDED
						&& (best_opt > current_model.get(GRB.DoubleAttr.ObjVal))) {
					branch_var = checkIntegrality(current_model);
					if (branch_var != null) {
						var_name = branch_var.get(StringAttr.VarName);
						i = Integer.valueOf(var_name.substring(var_name.indexOf('x') + 1, var_name.indexOf('y')));
						j = Integer.valueOf(var_name.substring(var_name.indexOf('y') + 1));
						GRBModel new_model1 = new GRBModel(current_model); // new_model1.update();
						new_model1.getVarByName(branch_var.get(StringAttr.VarName)).set(GRB.DoubleAttr.LB, 1.0);
						stiva.add(new_model1);
						GRBModel new_model2 = new GRBModel(current_model); // new_model2.update();
						new_model2.getVarByName(branch_var.get(StringAttr.VarName)).set(GRB.DoubleAttr.UB, 0.0);
						stiva.add(new_model2);
					} else {
						System.out.println("Fathoming by integrality");
						found = true;
						best_model = current_model;
						best_opt = current_model.get(GRB.DoubleAttr.ObjVal);
					}
				}
			}

		} catch (GRBException e) {
			System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
		}
	}

	public static void main(String[] args) {
		long start = System.nanoTime();
		// heuristic(args[0], Double.valueOf(args[1]), Integer.valueOf(args[2]));
		// System.out.println("Execution time = " + (System.nanoTime() - start) * 1e-9);
		// start = System.nanoTime();
		// simple_heuristic(args[0], Double.valueOf(args[1]), Integer.valueOf(args[2]));
		// double_heuristic("m8n1000s1.inp", 0.0, 3);
		heuristic("m8n1500s0.inp", 0.0, 2);
		// b_b("m4n500s0.inp", 0.0, 2);
		System.out.println("Execution time1 = " + (System.nanoTime() - start) * 1e-9);
		start = System.nanoTime();
		// heuristic("m8n500s3.inp", 0.0, 30);
		// simple_heuristic("m8n500s0.inp", 0.0, 33);
		System.out.println("Execution time2 = " + (System.nanoTime() - start) * 1e-9);
	}
}
