/*
 * Copyright (C) 2020 Cristian Frasinaru
 */
package ro.uaic.info.mdvsp.flow;

import java.io.FileNotFoundException;
import java.io.IOException;
import org.jgrapht.alg.flow.mincost.CapacityScalingMinimumCostFlow;
import org.jgrapht.graph.DefaultWeightedEdge;
import ro.uaic.info.mdvsp.Model;
import ro.uaic.info.mdvsp.Solution;

/**
 *
 * @author Cristian Frăsinaru
 */
public class ModelMinCostFlow extends Model {

    public ModelMinCostFlow(String name, int m, int n) {
        super(name, m, n);
    }

    public ModelMinCostFlow(String name) throws IOException {
        super(name);
    }

    @Override
    public void _solve() throws FileNotFoundException {
        //cost(depot, same depot) = 0
        for (int i = 0; i < m; i++) {
            cost[i][i] = 0;
        }
        CapacityScalingMinimumCostFlow<Node, DefaultWeightedEdge> alg = new CapacityScalingMinimumCostFlow<>();
        var pb = new MinCostFlowMDVSP(this);
        var flow = alg.getMinimumCostFlow(pb);
        System.out.println("min flow cost = " + flow.getCost());

        var map = flow.getFlowMap();
        Solution sol = new Solution(this);
        for (var e : map.keySet()) {
            if (flow.getFlow(e) == 1) {
                //System.out.println(e + " = " + flow.getFlow(e));
                Node u = pb.getGraph().getEdgeSource(e);
                Node v = pb.getGraph().getEdgeTarget(e);
                if (u.getId() != v.getId()) {
                    sol.set(u.getId(), v.getId(), 1);
                }
            }
        }

        //getTours();
        //printTours();
    }

}
