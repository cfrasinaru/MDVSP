/*
 * Copyright (C) 2020 Cristian Frasinaru
 */
package ro.uaic.info.mdvsp.repair;

import java.io.IOException;
import ro.uaic.info.mdvsp.Model;
import ro.uaic.info.mdvsp.Solution;

/**
 *
 * @author Cristian Frăsinaru
 */
public class RepairModel extends Model {

    private final Model other;

    public RepairModel(Model other) throws IOException {
        super(other);
        this.other = other;
    }

    @Override
    protected void _solve() throws Exception {
        //solve the original problem
        other.solve();

        //repair
        Solution sol1 = new BipartiteMatchingRepair(other, 1).getSolution();
        Solution sol2 = new BipartiteMatchingRepair(other, 2).getSolution();

        Solution sol = sol1.totalCost() < sol2.totalCost() ? sol1 : sol2;
        solutions.add(sol);
        
        /*
        Solution rep1 = new BipartiteMatchingRepair(this, 1).repair(sol);
        Solution rep2 = new BipartiteMatchingRepair(this, 1).repair(sol);        
        solutions.add(rep1.totalCost() < rep2.totalCost() ? rep1 : rep2);
        */
    }

}
