/*
 * Copyright (C) 2021 Faculty of Computer Science Iasi, Romania
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

/**
 *
 * @author Cristian Frăsinaru
 */
public class Instance {

    protected String dataFile;

    protected String name;
    protected int m; //depots
    protected int n; //trips
    protected int nbVehicles[];
    protected int cost[][];
    protected int knownOptimum = -1;

    protected SimpleDirectedWeightedGraph<Integer, DefaultWeightedEdge> graph;

    protected Instance() {
    }

    /**
     *
     * @param filename
     * @throws java.io.IOException
     */
    public Instance(String filename) throws IOException {
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
    public Instance(String name, int m, int n) {
        init(name, m, n);
    }

    /**
     *
     * @param other
     */
    public Instance(Instance other) {
        this.name = other.name;
        this.n = other.n;
        this.m = other.m;
        this.nbVehicles = other.nbVehicles;
        this.cost = other.cost;
    }

    public Instance(String name, int m, int n, int[] nbVehicles, int[][] cost) {
        this.name = name;
        this.n = n;
        this.m = m;
        this.nbVehicles = nbVehicles;
        this.cost = cost;
    }

    private void init(String name, int m, int n) {
        this.name = name;
        this.m = m;
        this.n = n;
        this.nbVehicles = new int[m];
        this.cost = new int[m + n][m + n];
    }

    /**
     * 
     * @param name 
     */
    public void setName(String name) {
        this.name = name;
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
    public int countVehicles(int depot) {
        return nbVehicles[depot];
    }

    /**
     *
     * @return total number of vehicles
     */
    public int countVehicles() {
        int total = 0;
        for (int i = 0; i < m; i++) {
            total += nbVehicles[i];
        }
        return total;
    }

    /**
     *
     * @return
     */
    public int[] getNbVehicles() {
        return nbVehicles;
    }

    /**
     *
     * @param nbVehicles
     */
    public void setNbVehicles(int[] nbVehicles) {
        this.nbVehicles = nbVehicles;
    }

    /**
     *
     */
    public void setMaxVehicles() {
        for (int i = 0; i < m; i++) {
            nbVehicles[i] = n;
        }
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
     * @param i
     * @param j
     * @return
     */
    public int cost(int i, int j) {
        return cost[i][j];
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
     * Creates a graph without depots. This a acyclic (DAG).
     *
     * @return
     */
    protected DirectedAcyclicGraph createReducedGraph() {
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
        //System.out.print("Creating graph...");
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

    /**
     * m4n500s0
     *
     * @param index
     * @throws IOException
     */
    public void write(int index) throws IOException {
        String fileName = Config.getDataPath() + "m" + m + "n" + n + "s" + index + ".inp";
        write(fileName);
    }

    /**
     *
     * @param filename
     * @throws IOException
     */
    public void write(String filename) throws IOException {
        try (PrintWriter out = new PrintWriter(filename)) {
            out.print(toString());
        }
    }

    @Override
    public String toString() {
        String eol = System.lineSeparator();
        String tab = "\t";
        StringBuilder out = new StringBuilder();
        out.append(m).append(tab).append(n);
        for (int i = 0; i < m; i++) {
            out.append(tab).append(nbVehicles[i]);
        }
        out.append(tab).append(knownOptimum);
        for (int i = 0; i < n + m; i++) {
            out.append(eol);
            for (int j = 0; j < n + m; j++) {
                if (j > 0) {
                    out.append(tab);
                }
                out.append(cost[i][j]);
            }
        }
        out.append(eol).append(knownOptimum);
        return out.toString();
    }

}
