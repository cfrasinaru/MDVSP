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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import ro.uaic.info.mdvsp.Config;
import ro.uaic.info.mdvsp.InvalidSolutionException;
import ro.uaic.info.mdvsp.Model;
import ro.uaic.info.mdvsp.Solution;
import ro.uaic.info.mdvsp.Submodel;
import ro.uaic.info.mdvsp.Tour;

/**
 *
 * @author Cristian Frăsinaru
 */
public class MetaheuristicModel extends Model {

    private final Model other;

    public MetaheuristicModel(Model other) throws IOException {
        super(other);
        this.other = other;
    }

    @Override
    protected void _solve() throws Exception {
        long t0 = System.currentTimeMillis();
        //solve the original problem
        other.solve();

        Random rand = new Random();
        Solution currentSol = other.getSolution();

        int step = 0;
        int failedSteps = 0;
        int maxFailedSteps = Config.getInt("maxFailedSteps", 10);
        int poolTours = Config.getInt("minPoolTours", 50);
        int maxPoolTours = Config.getInt("maxPoolTours", 100);
        int solImprovement = 0;

        do {
            if (Config.isOutputEnabled()) {
                int val = currentSol.totalCost();
                double pOpt = ((double) val - knownOptimum) / knownOptimum;
                System.out.printf("%d: %.5f \tpoolTours=%d  \trunningTime=%d %n", (step++), pOpt, poolTours, System.currentTimeMillis() - t0);
            }            
            t0 = System.currentTimeMillis();
            
            //improve
            List<Tour> tours = currentSol.getTours();

            //select a pool of random tours
            double prob = (double) poolTours / tours.size();
            List<Tour> randomTours = new ArrayList<>();
            for (Tour t : tours) {
                if (rand.nextDouble() < prob) {
                    randomTours.add(t);
                }
            }

            //create a subproblem only wih the selected tours
            Submodel sub = new Submodel(this, randomTours);

            //create an exact solution of the submodel
            Model3D exact = new Model3D(sub);
            exact.setZeroMipGap(false);
            exact.solve();

            //adjust the original solution with the subproblem solution
            List<Tour> improvedTours = new ArrayList<>();
            for (Tour t : exact.getSolution().getTours()) {
                improvedTours.add(sub.getMappedTour(t));
            }
            tours.removeAll(randomTours);
            tours.addAll(improvedTours);

            Solution newSol = new Solution(this, tours);
            solImprovement = newSol.totalCost() - currentSol.totalCost();
            currentSol = newSol;

            if (solImprovement == 0) {
                failedSteps++;
                if (failedSteps % 10 == 0 && poolTours < maxPoolTours) {
                    poolTours += 5;
                }
            } else {
                failedSteps = 0;
            }
                        
        } while (!isTimeLimitReached() && (solImprovement > 0 || failedSteps < maxFailedSteps));

        solutions.add(currentSol);
    }

}
