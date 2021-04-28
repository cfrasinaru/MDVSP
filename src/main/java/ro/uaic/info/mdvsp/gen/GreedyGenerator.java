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
import java.util.LinkedList;
import ro.uaic.info.mdvsp.Instance;
import ro.uaic.info.mdvsp.Model;
import ro.uaic.info.mdvsp.gurobi.Model3D;

/**
 * Generates a random MDVSP instance, in a greedy manner. The optimum is known.
 *
 *
 *
 * @author Cristian Frăsinaru
 */
public class GreedyGenerator extends Generator {
    
    private int best; //the optimum solution value

    public GreedyGenerator(int nbDepots, int nbTrips) {
        super(nbDepots, nbTrips);
    }

    /**
     *
     * @return
     */
    @Override
    public Instance build() {
        init();
        this.best = 0;

        //Create a global shuffled queue of trip numbers
        var trips = new LinkedList<>(shuffledTrips());

        //Create random tours representing the solution
        while (!trips.isEmpty()) {
            int tourSize = randGauss(avgTourSize);
            tours.add(createTour(tourSize, trips));
        }
        addRandomEdges(true);
        best = computeCost();
        computeNbVehicles();
        Instance instance = createInstance();
        instance.setKnownOptimum(best);
        return instance;
    }
        
    public static void main(String args[]) throws IOException {
        var gen = new GreedyGenerator(4, 200)
                .avgTourSize(4)
                .tripCosts(0, 1000)
                .depotCosts(5000, 6000);
        System.out.println("Generate");
        Instance inst = gen.build();
        inst.write(0);
        System.out.println(inst.getKnownOptimum());
        
        System.out.println("Solve");
        Model pb = new Model3D(inst);
        pb.solve();
        System.out.println(pb.getSolution().totalCost());
    }
    
}
