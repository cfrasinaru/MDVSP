/*
 * Copyright (C) 2020 Cristian Frasinaru
 */
package ro.uaic.info.mdvsp.gurobi;

import java.io.IOException;
import ro.uaic.info.mdvsp.Model;
import ro.uaic.info.mdvsp.Solution;
import ro.uaic.info.mdvsp.bb.Model_b_heuristic;
import ro.uaic.info.mdvsp.repair.BipartiteMatchingRepair;

/**
 *
 * @author Cristian Frăsinaru
 */
public class ModelRepairBadTours extends Model {

    public ModelRepairBadTours(String filename) throws IOException {
        super(filename);
    }

    @Override
    protected void _solve() throws Exception {
        //solve the relaxed problem
        Model relax = new Model_b_heuristic(dataFile);
        relax.solve();

        //repair
        Solution sol1 = new BipartiteMatchingRepair(relax, 1).getSolution();
        Solution sol2 = new BipartiteMatchingRepair(relax, 2).getSolution();

        solutions.add(sol1.totalCost() < sol2.totalCost() ? sol1 : sol2);
    }

}
