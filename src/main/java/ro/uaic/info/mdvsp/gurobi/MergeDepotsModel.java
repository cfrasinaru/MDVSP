/*
 * Copyright (C) 2019 Cristian Frasinaru
 */
package ro.uaic.info.mdvsp.gurobi;

import gurobi.GRBException;
import java.io.IOException;
import ro.uaic.info.mdvsp.Solution;
import ro.uaic.info.mdvsp.Tour;

/**
 * Replace all depots with a single one. Solve. Repair.
 *
 * @author Cristian Frăsinaru
 */
public class MergeDepotsModel extends ModelRelaxed {

    private int count[];
    private int[] originalVehicles;

    public MergeDepotsModel(String filename) throws IOException {
        super(filename);
    }


    @Override
    public void _solve() throws GRBException {

        // Put all vehicles in depot 0. 
        this.originalVehicles = new int[m];
        System.arraycopy(nbVehicles, 0, originalVehicles, 0, m);
        this.nbVehicles[0] = this.nbVehicles(); //total
        for (int i = 1; i < m; i++) {
            this.nbVehicles[i] = 0;
        }

        // Solve the altered model. All tours will start and end in depot 0.
        super._solve();

        // Restore the number of vehicles
        nbVehicles = originalVehicles;

        for (Solution sol : solutions) {
            count = new int[m];
            for (Tour t : sol.getTours()) {
                int firstTrip = t.second();
                int lastTrip = t.lastButOne();
                int depot = bestDepot(firstTrip, lastTrip);
                
                //clear the first and the last arc
                sol.set(0, firstTrip, 0);
                sol.set(lastTrip, 0 ,0);
                
                //add arcs from and to the best depot
                sol.set(depot, firstTrip, 1);
                sol.set(lastTrip, depot, 1);
                
                //increment the number of used vehicles in the depot
                count[depot]++;
            }
            sol.clearTours(); //force tours reconstruction
        }        
    }

    private int bestDepot(int from, int to) {
        double minValue = Double.MAX_VALUE;
        int bestDepot = -1;
        for (int depot = 0; depot < m; depot++) {
            if (count[depot] >= nbVehicles(depot)) {
                continue;
            }
            double value = cost[depot][from] + cost[to][depot];
            if (minValue > value) {
                value = minValue;
                bestDepot = depot;
            }
        }
        return bestDepot;
    }

}
