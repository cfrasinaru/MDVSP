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
package ro.uaic.info.mdvsp;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author Cristian Frăsinaru
 */
public class Submodel extends Model {

    private final Model model;
    private final List<Tour> tours;
    private int mapping[]; //mappin[index in submodel] = index in original
    private int inverse[]; //mappin[index in original] = index in submodel

    /**
     *
     * @param model
     * @param tours
     */
    public Submodel(Model model, List<Tour> tours) {
        this.name = model.name + "_sub";
        this.model = model;
        this.tours = tours;
        init();
    }

    private void init() {
        List<Integer> nodes = tours.stream().flatMap(t -> t.stream()).distinct().sorted().collect(Collectors.toList());

        //compute the number of depots
        this.m = 0;
        int k = 0;
        while (k < nodes.size() && nodes.get(k++) < model.nbDepots()) {
            this.m++;
        }

        //compute the number of trips
        this.n = nodes.size() - this.m;

        //create the mapping
        mapping = new int[this.m + this.n];
        inverse = new int[model.m + model.n];
        for (int i = 0; i < m + n; i++) {
            int j = nodes.get(i);
            mapping[i] = j;
            inverse[j] = i;
        }

        //create the nbVehicles array
        this.nbVehicles = new int[m];
        for (Tour t : tours) {
            nbVehicles[inverse[t.get(0)]]++;
        }

        //create the cost matrix
        this.cost = new int[m + n][m + n];
        for (int i = 0; i < m + n; i++) {
            for (int j = 0; j < m + n; j++) {
                cost[i][j] = model.cost[mapping[i]][mapping[j]];
            }
        }        
    }

    /**
     *
     * @return
     */
    public Model getModel() {
        return model;
    }

    /**
     *
     * @return
     */
    public List<Tour> getTours() {
        return tours;
    }

    /**
     * 
     * @param t
     * @return 
     */
    public Tour getMappedTour(Tour t) {
        Tour tour = new Tour();
        for (int i : t) {
            tour.add(mapping[i]);
        }        
        return tour;
    }

    @Override
    protected void _solve() throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
