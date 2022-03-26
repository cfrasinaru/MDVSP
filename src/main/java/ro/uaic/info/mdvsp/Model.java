/*
 * Copyright (C) 2019 Cristian Frasinaru
 */
package ro.uaic.info.mdvsp;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.connectivity.BiconnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.graph.SimpleGraph;

/**
 *
 * @author Cristian Frăsinaru
 */
public abstract class Model {

    protected String dataFile;

    protected String name;
    protected int m; //depots
    protected int n; //trips
    protected int nbVehicles[];
    protected int cost[][];

    protected SimpleDirectedWeightedGraph<Integer, DefaultWeightedEdge> graph;
    protected List<Solution> solutions = new ArrayList<>();
    protected List<Solution> initialSolutions = new ArrayList<>();

    protected double tolerance = .00001;
    protected int poolSolutions = 1;
    protected int timeLimit = 0; //
    protected boolean outputEnabled = false;

    protected int knownOptimum = -1; //known knownOptimum

    protected long runningTime = -1;
    protected long startTime;

    protected Model() {
    }

    /**
     *
     * @param filename
     * @throws java.io.IOException
     */
    public Model(String filename) throws IOException {
        this.dataFile = filename;
        if (!filename.endsWith(".inp")) {
            this.dataFile += ".inp";
        }
        if (!filename.contains("/")) {
            this.dataFile = Config.getDataPath() + this.dataFile;
        }
        read(this.dataFile);
    }

    /**
     *
     * @param name
     * @param m depots
     * @param n trips
     */
    public Model(String name, int m, int n) {
        init(name, m, n);
    }

    /**
     *
     * @param other
     */
    public Model(Model other) {
        this.name = other.name;
        this.n = other.n;
        this.m = other.m;
        this.nbVehicles = other.nbVehicles;
        this.cost = other.cost;
        this.poolSolutions = other.poolSolutions;
        this.outputEnabled = other.outputEnabled;
    }

    /**
     *
     * @param name
     * @param m
     * @param n
     * @param nbVehicles
     * @param cost
     */
    public Model(String name, int m, int n, int[] nbVehicles, int[][] cost) {
        this.name = name;
        this.n = n;
        this.m = m;
        this.nbVehicles = nbVehicles;
        this.cost = cost;
        initParams();
    }

    /**
     *
     * @param instance
     */
    public Model(Instance instance) {
        this.name = instance.name;
        this.n = instance.n;
        this.m = instance.m;
        this.nbVehicles = instance.nbVehicles;
        this.cost = instance.cost;
        this.knownOptimum = instance.knownOptimum;
        this.dataFile = instance.dataFile;
        initParams();
    }

    private void init(String name, int m, int n) {
        this.name = name;
        this.m = m;
        this.n = n;
        this.nbVehicles = new int[m];
        this.cost = new int[m + n][m + n];
        initParams();
    }

    private void initParams() {
        setPoolSolutions(Config.getPoolSolutions());
        setOutputEnabled(Config.isOutputEnabled());
        setTimeLimit(Config.getTimeLimit());
    }

    /**
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @return
     */
    public int nbDepots() {
        return m;
    }

    /**
     *
     * @return
     */
    public int nbTrips() {
        return n;
    }

    /**
     *
     * @param depot
     * @return
     */
    public int nbVehicles(int depot) {
        return nbVehicles[depot];
    }

    /**
     *
     * @return total number of vehicles
     */
    public int nbVehicles() {
        int total = 0;
        for (int i = 0; i < m; i++) {
            total += nbVehicles[i];
        }
        return total;
    }

    /**
     *
     * @return the cost matrix.
     */
    public int[][] getCost() {
        return cost;
    }

    /**
     *
     * @return
     */
    public int nbArcs() {
        int nbArcs = 0;
        for (int i = 0; i < n + m; i++) {
            for (int j = 0; j < n + m; j++) {
                if (cost[i][j] > 0) {
                    nbArcs++;
                }
            }
        }
        return nbArcs;
    }

    /**
     * @return the poolSolutions
     */
    public int getPoolSolutions() {
        return poolSolutions;
    }

    /**
     * @param poolSolutions the poolSolutions to set
     */
    public void setPoolSolutions(int poolSolutions) {
        this.poolSolutions = poolSolutions;
    }

    /**
     *
     * @return
     */
    public Solution getInitialSolution() {
        return initialSolutions.isEmpty() ? null : initialSolutions.get(0);
    }

    /**
     * @return the initialSolutions
     */
    public List<Solution> getInitialSolutions() {
        return initialSolutions;
    }

    /**
     * @param initialSolutions the initialSolutions to set
     */
    public void setInitialSolutions(List<Solution> initialSolutions) {
        this.initialSolutions = initialSolutions;
    }

    /**
     *
     * @param sol
     */
    public void addInitialSolution(Solution sol) {
        initialSolutions.add(sol);
    }

    /**
     * @return the timeLimit (in seconds)
     */
    public int getTimeLimit() {
        return timeLimit;
    }

    /**
     * @param timeLimit the timeLimit to set (in seconds)
     */
    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }

    /**
     * @return the outputEnabled
     */
    public boolean isOutputEnabled() {
        return outputEnabled;
    }

    /**
     *
     * @param outputEnabled the outputEnabled to set
     */
    public void setOutputEnabled(boolean outputEnabled) {
        this.outputEnabled = outputEnabled;
    }

    /**
     *
     * @return
     */
    public Solution solve() {
        if (outputEnabled) {
            System.out.println("Solving " + name);
        }
        startTime = System.currentTimeMillis();
        try {
            _solve();
        } catch (Exception ex) {
            System.err.println(ex);
        }
        runningTime = System.currentTimeMillis() - startTime;
        return getSolution();
    }

    /**
     *
     * @throws Exception
     */
    protected abstract void _solve() throws Exception;

    /**
     *
     * @return
     */
    public Solution getSolution() {
        return solutions.isEmpty() ? null : solutions.get(0);
    }

    /**
     *
     * @return
     */
    public List<Solution> getSolutions() {
        return solutions;
    }

    /**
     * @return the runningTime
     */
    public long getRunningTime() {
        if (runningTime < 0) {
            return System.currentTimeMillis() - startTime;
        }
        return runningTime;
    }

    /**
     *
     * @return
     */
    public boolean isTimeLimitReached() {
        return getRunningTime() > Config.getTimeLimit() * 1000;
    }

    /**
     *
     * @return
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * Creates a graph without depots. This a acyclic (DAG).
     *
     * @return
     */
    public  DirectedAcyclicGraph createReducedGraph() {
        DirectedAcyclicGraph g = new DirectedAcyclicGraph(DefaultEdge.class);
        for (int i = m; i < n + m; i++) {
            g.addVertex(i);
        }
        for (int i = m; i < n + m; i++) {
            for (int j = m; j < n + m; j++) {
                if (cost[i][j] >= 0) {
                    var e = g.addEdge(i, j);
                }
            }
        }
        return g;
    }

    /**
     *
     */
    protected void createGraph() {
        graph = new SimpleDirectedWeightedGraph(DefaultWeightedEdge.class);
        for (int i = 0; i < n + m; i++) {
            graph.addVertex(i);
        }
        for (int i = 0; i < n + m; i++) {
            for (int j = 0; j < n + m; j++) {
                if (cost[i][j] >= 0) {
                    DefaultWeightedEdge e = graph.addEdge(i, j);
                    graph.setEdgeWeight(e, cost[i][j]);
                }
            }
        }
    }

    public SimpleDirectedWeightedGraph<Integer, DefaultWeightedEdge> getGraph() {
        if (graph == null) {
            createGraph();
        }
        return graph;
    }

    protected SimpleGraph<Integer, DefaultEdge> createSupportGraph() {
        SimpleGraph<Integer, DefaultEdge> support = new SimpleGraph(DefaultEdge.class);
        for (int i = 0; i < n + m; i++) {
            support.addVertex(i);
        }
        for (int i = 0; i < n + m; i++) {
            for (int j = 0; j < n + m; j++) {
                if (cost[i][j] >= 0) {
                    DefaultEdge e = support.addEdge(i, j);
                }
            }
        }
        return support;
    }

    private List<Tour> findAllPaths() {
        createGraph();
        List<Tour> list = new ArrayList<>();
        boolean visited[] = new boolean[n + m];
        Arrays.fill(visited, 0, n + m, false);
        Tour current = new Tour();
        findAllPathsRec(0, 1, current, list, visited);
        /*
        for (int i = 0; i < m - 1; i++) {
            for (int j = i + 1; j < m; j++) {
                findAllPathsRec(i, j, current, list, visited);
            }
        }
         */
        return list;
    }

    private void findAllPathsRec(int from, int to, Tour current, List<Tour> list, boolean visited[]) {
        visited[from] = true;
        current.add(from);
        for (int i : Graphs.neighborSetOf(graph, from)) {
            if (i == to) {
                current.add(to);
                list.add(new Tour(current));
                current.remove((Integer) to);
            } else {
                if (i < m || visited[i]) {
                    continue;
                }
                findAllPathsRec(i, to, current, list, visited);
            }
        }
        visited[from] = false;
        current.remove((Integer) from);
    }

    private void read(String filename) throws IOException {
        Path path = Paths.get(filename);
        List<String> lines = Files.readAllLines(path);
        String firstLine[] = lines.get(0).split("\t");
        String instName = path.getFileName().toString().replaceAll(".inp", "");
        init(instName, Integer.parseInt(firstLine[0]), Integer.parseInt(firstLine[1]));

        for (int j = 0; j < m; j++) {
            nbVehicles[j] = Integer.parseInt(firstLine[j + 2]);
        }
        for (int i = 0; i < n + m; i++) {
            String line[] = lines.get(i + 1).split("\t");
            for (int j = 0; j < n + m; j++) {
                cost[i][j] = Integer.parseInt(line[j]);
            }
        }
    }

    public void write(String filename) throws IOException {
        try (PrintWriter out = new PrintWriter(filename)) {
            out.print(m + "\t" + n);
            for (int i = 0; i < m; i++) {
                out.print("\t" + nbVehicles[i]);
            }
            for (int i = 0; i < n + m; i++) {
                out.println();
                for (int j = 0; j < n + m; j++) {
                    if (j > 0) {
                        out.print("\t");
                    }
                    out.print(cost[i][j]);
                }
            }
        }
    }

    /**
     * @return the knownOptimum
     */
    public int getKnownOptimum() {
        return knownOptimum;
    }

    /**
     * @param knownOptimum the knownOptimum to set
     */
    public void setKnownOptimum(int knownOptimum) {
        this.knownOptimum = knownOptimum;
    }

    /**
     *
     * @param tour
     * @return
     */
    public int computeCost(Tour tour) {
        int c = 0;
        int sz = tour.size();
        for (int i = 0; i < sz - 1; i++) {
            c += cost[tour.get(i)][tour.get(i + 1)];
        }
        return c;
    }

    /**
     *
     * @param list
     * @return
     */
    public int computeCost(List<Tour> list) {
        int c = 0;
        c = list.stream().map(t -> computeCost(t)).reduce(c, Integer::sum);
        return c;
    }

    public void inspectConnectivity() {
        Graph<Integer, DefaultEdge> support = createSupportGraph();
        BiconnectivityInspector<Integer, DefaultEdge> ci = new BiconnectivityInspector<Integer, DefaultEdge>(support);
        boolean connected = ci.isConnected();
        System.out.println("Connected: " + connected);
        if (!connected) {
            System.out.print(ci.getConnectedComponents().size() + " connected components: ");
            for (Graph<Integer, DefaultEdge> g : ci.getConnectedComponents()) {
                System.out.print(g.vertexSet().size() + " ");
                if (g.vertexSet().size() == 1) {
                    System.out.println(g.vertexSet());
                }
            }
            System.out.println();
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(":").append("nbDepots=").append(m).append(", nbTrips=").append(n);
        return sb.toString();
    }

}
