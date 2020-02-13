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
        ModelRelaxed relax = new ModelRelaxed(this);
        //Model relax = new Model_b_heuristic(dataFile);
        Solution relaxedSol = relax.solve();
        
        //repair
        Solution repairedSol = new BipartiteMatchingRepair(relax).getSolution();
        solutions.add(repairedSol);
                
        /*
        Model3D exact = new Model3D(this);
        exact.addInitialSolution(repairedSol);
        exact.setTimeLimit(60);
        Solution exactSol = exact.solve();
        solutions.add(exactSol);
        */
    }

}
