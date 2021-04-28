/*
 * Copyright (C) 2021 Faculty of Computer Science Iasi, Romania
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
 * Adds new trips between consecutive trips of a tour.
 *
 * The new trips have no connections to other trips outside the tour.
 *
 * @author Cristian Frăsinaru
 */
public class InsertionGenerator extends Generator {

    private static final int n0 = 100;

    public InsertionGenerator(int nbDepots, int nbTrips) {
        super(nbDepots, nbTrips);
    }

    @Override
    public Instance build() {
        init();
        //create a smaller manageable instance
        var gen = new RandomGenerator(m, n0)
                .avgTourSize(avgTourSize)
                .tripCosts(minTripCost, maxTripCost)
                .depotCosts(minDepotCost, maxDepotCost);
        Instance inst = gen.build();
        inst.setMaxVehicles();

        //solve it
        Model model = new Model3D(inst);
        model.solve();
        Solution sol = model.getSolution();

        //copy the costs
        for (int i = 0; i < m + n0; i++) {
            for (int j = 0; j < m + n0; j++) {
                cost[i][j] = inst.cost(i, j);
            }
        }

        //get the optimal tours
        for (Tour t0 : sol.getTours()) {
            tours.add(t0);
        }

        insert0();
        createGraph();
        computeNbVehicles();

        Instance instance = createInstance();
        instance.setKnownOptimum(computeCost());
        return instance;
    }

    private void insert0() {
        //Insert new trips along the solution tours
        int other = m + n0; //incrementing towards m + n
        int tsize = tours.size();
        while (other < m + n) {
            //pick a random tour
            int i = rand.nextInt(tsize);
            Tour t = tours.get(i);
            //pick a random position 
            int j = 1 + rand.nextInt(t.size() - 2);
            //insert other between trip and next
            int trip = t.get(j);
            int next = t.get(j + 1);
            t.add(j + 1, other);
            cost[other][next] = cost[trip][next];
            cost[trip][other] = minTripCost + rand.nextInt(maxTripCost - minTripCost);
            other++;
        }
        insertDepotEdges();
    }

    private void insert1() {
        //Insert new trips along the solution tours
        int k = (n / n0) - 1;
        int other = m + n0; //incrementing towards m + n
        for (Tour t : tours) {
            int i = 1;
            while (i < t.size() - 1) {
                int trip = t.get(i);
                int next = t.get(i + 1);
                for (int j = 0; j < k; j++) {
                    t.add(i + 1, other);
                    cost[other][next] = cost[trip][next];
                    cost[trip][other] = minTripCost + rand.nextInt(maxTripCost - minTripCost);
                    other++;                    
                }
                i += 2;
            }
        }
        insertDepotEdges();
    }

    private void insertDepotEdges() {
        for (int j = m; j < m + n; j++) {
            for (int i = 0; i < m; i++) {
                if (cost[j][i] < 0) {
                    cost[j][i] = maxDepotCost + rand.nextInt(maxDepotCost - minDepotCost);
                }
                if (cost[i][j] < 0) {
                    cost[i][j] = maxDepotCost + rand.nextInt(maxDepotCost - minDepotCost);
                }
            }
        }
    }

    public static void main(String args[]) throws IOException {
        var gen = new InsertionGenerator(8, 400)
                .avgTourSize(4)
                .tripCosts(0, 1000)
                .depotCosts(5000, 6000);
        System.out.println("Generate");
        Instance inst = gen.build();
        inst.write(0);
        System.out.println("\t" + inst.getKnownOptimum());

        System.out.println("Exact");
        Model pb = new Model3D(inst);
        pb.solve();
        Solution sol = pb.getSolution();
        System.out.println("\t" + sol.totalCost());

        System.out.println("Heuristic");
        pb = new RepairModel(new ModelRelaxed(inst));
        pb.solve();
        sol = pb.getSolution();
        System.out.println("\t" + sol.totalCost());
        
        if (false) {
            System.out.println("-------------------------------------------------");
            System.out.println("Generated tours");
            for (Tour t : gen.tours) {
                System.out.println(t.toDetailedString(gen.cost));
            }

            System.out.println("-------------------------------------------------");
            System.out.println("Computed tours");
            for (Tour t : sol.getTours()) {
                System.out.println(t.toDetailedString(gen.cost));
            }
        }

    }

}
