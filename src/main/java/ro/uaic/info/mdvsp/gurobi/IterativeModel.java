/*
 * Copyright (C) 2020 Faculty of Computer Science Iasi, Romania
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
package ro.uaic.info.mdvsp.gurobi;

import gurobi.GRB;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import ro.uaic.info.mdvsp.Solution;
import ro.uaic.info.mdvsp.Tour;

/**
 * Solve the relaxed problem - Create constraints based on bad tours - Repeat.
 *
 * @author Cristian Frăsinaru
 */
public class IterativeModel extends ModelRelaxed {

    public IterativeModel(String filename) throws IOException {
        super(filename);
    }

    @Override
    protected void optimize() throws GRBException {
        List<Tour> allBadTours = new ArrayList<>();
        initVariables();
        Random rand = new Random();
        int nbIterations = 100;
        for (int i = 1; i <= nbIterations; i++) {
            System.out.println("Iteration " + i);
            model.optimize();

            //store solution
            Solution sol = extractSolution();

            //pick some bad tours
            List<Tour> badTours = sol.getBadTours();

            for (int q = 1; q <= 1; q++) {
                Tour t = badTours.get(rand.nextInt(badTours.size()));
                allBadTours.add(t);
                //add aditional constraints
                //System.out.println("bad tour: " + t + ", size=" + t.size());
                int ts = t.size() - 1;
                //ts = ts / 2;
                GRBLinExpr sum = new GRBLinExpr();
                for (int j = 0; j < ts; j++) {
                    sum.addTerm(1, x[t.get(j)][t.get(j + 1)]);
                }
                model.addConstr(sum, GRB.LESS_EQUAL, ts - 1, "badTour" + i);
            }
        }
        allBadTours.stream().sorted().forEach(System.out::println);
    }

}
