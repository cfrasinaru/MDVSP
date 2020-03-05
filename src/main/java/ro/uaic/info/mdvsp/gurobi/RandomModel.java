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
public class RandomModel extends ModelRelaxed {

    public RandomModel(String filename) throws IOException {
        super(filename);
    }

    @Override
    protected void optimize() throws GRBException {
        Random rand = new Random();

        int nbSteps = 100;
        double prob = 5.0 / this.nbArcs();

        for (int step = 1; step <= nbSteps; step++) {
            System.out.println("Step " + step);
            
            model.optimize();
            extractSolution();

            for (int i = 0; i < n + m; i++) {
                for (int j = 0; j < n + m; j++) {
                    if (x[i][j] == null) {
                        continue;
                    }
                    x[i][j].set(GRB.DoubleAttr.LB, 0);
                    if (rand.nextDouble() < prob) {
                        x[i][j].set(GRB.DoubleAttr.LB, 1);
                    }
                }
            }
            model.update();                    

        }
    }

}
