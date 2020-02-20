/*
 * Copyright (C) 2020 Cristian Frasinaru
 */
package ro.uaic.info.mdvsp.gurobi;

import java.io.IOException;
import ro.uaic.info.mdvsp.Model;
import ro.uaic.info.mdvsp.Solution;
import ro.uaic.info.mdvsp.repair.BipartiteMatchingRepair;

/**
 *
 * @author Cristian Frăsinaru
 */
public class ModelRepairSimple extends Model {

    public ModelRepairSimple(String filename) throws IOException {
        super(filename);
    }

    @Override
    protected void _solve() throws Exception {
        //solve the relaxed problem
        Model relax = new ModelRelaxed(this);        
        relax.solve();

        //repair
        Solution sol1 = new BipartiteMatchingRepair(relax, 1).getSolution();
        Solution sol2 = new BipartiteMatchingRepair(relax, 2).getSolution();

        solutions.add(sol1.totalCost() < sol2.totalCost() ? sol1 : sol2);
    }

}
