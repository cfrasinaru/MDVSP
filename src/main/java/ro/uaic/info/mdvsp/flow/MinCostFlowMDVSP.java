/*
 * Copyright (C) 2020 Cristian Frasinaru
 */
package ro.uaic.info.mdvsp.flow;

import java.util.function.Function;
import org.jgrapht.Graph;
import org.jgrapht.alg.flow.mincost.MinimumCostFlowProblem;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import ro.uaic.info.mdvsp.Model;

/**
 *
 * @author Cristian Frăsinaru
 */
public class MinCostFlowMDVSP implements MinimumCostFlowProblem<Node, DefaultWeightedEdge> {

    Model model;
    int n, m;
    int cost[][];
    SimpleDirectedWeightedGraph<Node, DefaultWeightedEdge> graph;

    public MinCostFlowMDVSP(Model model) {
        this.model = model;
        n = model.nbTrips();
        m = model.nbDepots();

        int[][] modelCost = model.getCost();
        cost = new int[m + n][m + n];
        for (int i = 0; i < m + n; i++) {
            for (int j = 0; j < m + n; j++) {
                int c = modelCost[i][j];
                cost[i][j] = c == -1 ? 2_000_000 : c;
            }
        }
        createGraph();
    }

    private void createGraph() {
        graph = new SimpleDirectedWeightedGraph(DefaultWeightedEdge.class);

        //add depots as sources
        Source sourceMap[] = new Source[m];
        for (int i = 0; i < m; i++) {
            Source source = new Source(i);
            sourceMap[i] = source;
            graph.addVertex(source);
        }

        //add depots as sinks
        Sink sinkMap[] = new Sink[m];
        for (int i = 0; i < m; i++) {
            Sink sink = new Sink(i);
            sinkMap[i] = sink;
            graph.addVertex(sink);
        }

        //add trips - for each trip create two nodes
        TripTo tripToMap[] = new TripTo[n];
        TripFrom tripFromMap[] = new TripFrom[n];
        for (int i = m; i < n + m; i++) {
            TripFrom from = new TripFrom(i);
            tripFromMap[i - m] = from;
            TripTo to = new TripTo(i);
            tripToMap[i - m] = to;
            graph.addVertex(from);
            graph.addVertex(to);
            var e = graph.addEdge(from, to);
            graph.setEdgeWeight(e, 0); //cost of the trip expanded edge
        }

        //add edges
        //sources - sinks
        for (int i = 0; i < m; i++) {
            Node u = sourceMap[i];
            for (int j = 0; j < m; j++) {
                if (cost[i][j] < 0) {
                    continue;
                }
                Node v = sinkMap[j];
                var e = graph.addEdge(u, v);
                graph.setEdgeWeight(e, cost[i][j]);
            }
        }

        //sources - tripFrom
        for (int i = 0; i < m; i++) {
            Node u = sourceMap[i];
            for (int j = m; j < m + n; j++) {
                if (cost[i][j] < 0) {
                    continue;
                }
                Node v = tripFromMap[j - m];
                var e = graph.addEdge(u, v);
                graph.setEdgeWeight(e, cost[i][j]);
            }
        }

        //tripTo - sinks
        for (int i = 0; i < m; i++) {
            Node u = sinkMap[i];
            for (int j = m; j < m + n; j++) {
                if (cost[j][i] < 0) {
                    continue;
                }
                Node v = tripToMap[j - m];
                var e = graph.addEdge(v, u);
                graph.setEdgeWeight(e, cost[j][i]);
            }
        }

        //trips  i-->j   tripTo[i] --> tripFrom[j]
        for (int i = m; i < m + n; i++) {
            Node u = tripToMap[i - m];
            for (int j = m; j < m + n; j++) {
                if (cost[i][j] < 0) {
                    continue;
                }
                Node v = tripFromMap[j - m];
                var e = graph.addEdge(u, v);
                graph.setEdgeWeight(e, cost[i][j]);
            }
        }

    }

    @Override
    public Graph<Node, DefaultWeightedEdge> getGraph() {
        return graph;
    }

    private Integer nodeSupply(Node u) {
        if (u instanceof Source) {
            Source node = (Source) u;
            return model.nbVehicles(node.id);
        }
        if (u instanceof Sink) {
            Sink node = (Sink) u;
            return -model.nbVehicles(node.id);
        }
        return 0;
    }

    @Override
    public Function<Node, Integer> getNodeSupply() {
        return this::nodeSupply;
    }

    private Integer lowerBound(DefaultWeightedEdge e) {
        Node u = graph.getEdgeSource(e);
        Node v = graph.getEdgeTarget(e);
        if (u instanceof TripFrom && v instanceof TripTo) {
            return 1;
        }
        return 0;
    }

    private Integer upperBound(DefaultWeightedEdge e) {
        Node u = graph.getEdgeSource(e);
        Node v = graph.getEdgeTarget(e);
        if (u instanceof Source && v instanceof Sink) {
            return 1_000_000;
        }
        return 1;
    }

    @Override
    public Function<DefaultWeightedEdge, Integer> getArcCapacityLowerBounds() {
        return this::lowerBound;
    }

    @Override
    public Function<DefaultWeightedEdge, Integer> getArcCapacityUpperBounds() {
        return this::upperBound;
    }

    public static void main(String args[]) {
        //CapacityScalingMinimumCostFlow<Integer, DefaultWeightedEdge> alg = new CapacityScalingMinimumCostFlow<>();
        //var sol = alg.getMinimumCostFlow(new MinCostFlowMDVSP());
        //System.out.println(sol.getCost());
    }

}
