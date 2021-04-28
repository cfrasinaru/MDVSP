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
package ro.uaic.info.mdvsp.gen;

import java.io.IOException;
import ro.uaic.info.mdvsp.Instance;
import ro.uaic.info.mdvsp.Model;
import ro.uaic.info.mdvsp.Solution;
import ro.uaic.info.mdvsp.Tour;
import ro.uaic.info.mdvsp.gurobi.Model3D;
import ro.uaic.info.mdvsp.gurobi.ModelRelaxed;
import ro.uaic.info.mdvsp.repair.RepairModel;

/**
 * Generates a random MDVSP instance, in a greedy manner. The optimum is known.
 *
 *
 *
 * @author Cristian Frăsinaru
 */
public class GridBasedGenerator extends Generator {

    private final int nbRows;
    private final int nbCols;
    private int best; //the optimum solution value

    public GridBasedGenerator(int nbDepots, int nbRows, int nbCols) {
        super(nbDepots, nbRows * nbCols);
        this.nbRows = nbRows;
        this.nbCols = nbCols;
    }

    /**
     *
     * @return
     */
    @Override
    public Instance build() {
        init();
        this.best = 0;

        int dx = 100; //minTripCost + rand.nextInt(maxTripCost - minTripCost);
        int dy = dx;
        //int dy = minTripCost + rand.nextInt(maxTripCost - minTripCost);
        //create the cost matrix
        for (int i = m; i < m + n - 1; i++) {
            int y0 = (i - m) / nbCols;
            int x0 = (i - m) % nbCols;
            for (int j = i + 1; j < m + n; j++) {
                int y1 = (j - m) / nbCols;
                int x1 = (j - m) % nbCols;
                if (!((x1 == x0 && y1 == y0 + 1) || (x1 == x0 + 1 && y1 == y0))) {
                    continue;
                }
                cost[i][j] = (int) Math.sqrt(1.0d * dx * dx * (x0 - x1) * (x0 - x1) + 1.0d * dy * dy * (y0 - y1) * (y0 - y1));
            }
        }

        //pull out
        int maxpo = minDepotCost;
        for (int i = 0; i < m; i++) {
            for (int j = m; j < m + n; j++) {
                if (cost[i][j] < 0) {
                    //cost[i][j] = minDepotCost + rand.nextInt(maxDepotCost - maxpo);
                    cost[i][j] = 1000;
                }
            }
        }
        //pull in
        int maxpi = minDepotCost;
        for (int j = m; j < m + n; j++) {
            for (int i = 0; i < m; i++) {
                if (cost[j][i] < 0) {
                    //cost[j][i] = maxpi + rand.nextInt(maxDepotCost - maxpi);
                    cost[j][i] = 1000;
                }
            }
        }

        //create the best tours
        for (int row = 0; row < nbRows; row++) {
            Tour tour = new Tour();
            int depot = rand.nextInt(m);
            tour.add(depot);
            for (int col = 0; col < nbCols; col++) {
                int trip = m + row * nbCols + col;
                tour.add(trip);
            }
            tour.add(depot);
            tours.add(tour);
        }

        best = computeCost();
        computeNbVehicles();
        Instance instance = createInstance();
        instance.setKnownOptimum(best);
        return instance;
    }

    public static void main(String args[]) throws IOException {
        var gen = new GridBasedGenerator(4, 30, 20)
                .tripCosts(0, 1000)
                .depotCosts(5000, 6000);
        System.out.println("Generate");
        Instance inst = gen.build();
        inst.write(0);
        System.out.println(inst.getKnownOptimum());

        System.out.println("Solve exact");
        Model pb = new Model3D(inst);
        pb.solve();
        Solution sol = pb.getSolution();
        System.out.println(sol.totalCost());
        System.out.println("Tours:" + sol.getTours().size());
        for (Tour t : sol.getTours()) {
            //System.out.println(t);
        }

        System.out.println("Solve heuristic");
        pb = new RepairModel(new ModelRelaxed(inst));
        pb.solve();
        System.out.println("\t" + pb.getSolution().totalCost());

    }

}
