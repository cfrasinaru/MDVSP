///*
// * Copyright (C) 2020 Faculty of Computer Science Iasi, Romania
// *
// * This program is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with this program.  If not, see <http://www.gnu.org/licenses/>.
// */
//package ro.uaic.info.mdvsp.gen;
//
//import java.io.PrintWriter;
//import java.util.ArrayList;
//import java.util.LinkedList;
//import java.util.List;
//import ro.uaic.info.mdvsp.Instance;
//import ro.uaic.info.mdvsp.Tour;
//
///**
// * Generates a random MDVSP instance. The optimum is known.
// *
// * @author Cristian Frăsinaru
// *
// */
//@Deprecated
//public class ShortestPathsGenerator extends AbstractGenerator {
//
//    private int best; //the optimum solution value
//    private List<Tour> tours; //the optimum tours
//    private final Tour[] parent;
//
//    public ShortestPathsGenerator(int nbDepots, int nbTrips) {
//        super(nbDepots, nbTrips);
//        parent = new Tour[nbDepots + nbTrips];
//        //parent[i] = the tour containing the trip i;
//    }
//
//    /**
//     *
//     */
//    @Override
//    public Instance build() {
//        init();
//        this.best = 0;
//        this.tours = new ArrayList<>();
//
//        //Create a global shuffled queue of trip numbers
//        var trips = new LinkedList<>(shuffledTrips());
//
//        //Create random tours representing the solution
//        int maxTourCost = 0;
//        while (!trips.isEmpty()) {
//            int tourSize = randGauss(avgTourSize);
//            Tour tour = createTour(tourSize, trips);
//            tours.add(tour);
//            //
//            int tourCost = tour.getCost(cost);
//            if (maxTourCost < tourCost) {
//                maxTourCost = tourCost;
//            }
//            //
//            for (int i = 1; i < tour.size() - 1; i++) {
//                parent[tour.get(i)] = tour;
//            }
//        }
//        best = computeCost(tours);
//        computeNbVehicles(tours);
//
//        //create an ordering of the trips (respecting the tours)
//        var orderedTrips = orderedTrips(tours);
//        int[] pos = orderedTripsPos(orderedTrips); //pos[i] is between 0 and n-1
//
//        //for each trip, add dummy edges to another trips
//        //making sure not to create a better tour.
//        for (Tour tour : tours) {
//            for (int i = 1; i < tour.size() - 2; i++) {
//                int trip = tour.get(i);
//                int next = tour.get(i + 1);
//                for (int j = pos[trip] + 1; j < n; j++) {
//                    int other = orderedTrips.get(j);
//                    if (other == next) {
//                        continue;
//                    }
//
//                    var e = graph.addEdge(trip, other);
//                    Tour temp = createTour(tour, trip, other);
//                    cost[trip][other] = 0;
//                    int opt = maxTourCost - temp.getCost(cost);
//                    System.out.println("newTour cost=" + temp.getCost(cost));
//                    System.out.println("maxTourCost=" + maxTourCost);
//                    System.out.println("\t" + opt);
//                    if (opt < minTripCost) {
//                        opt = minTripCost;
//                    } else if (opt >= maxTripCost) {
//                        opt = -1;
//                    }
//                    cost[trip][other] = opt == -1 ? -1 : opt + rand.nextInt(maxTripCost - opt);
//                    graph.setEdgeWeight(e, cost[trip][other]);
//                }
//            }
//        }
//
//        //pull out
//        int maxpo = maxPullOut();
//        for (int i = 0; i < m; i++) {
//            for (int j = m; j < m + n; j++) {
//                if (cost[i][j] < 0) {
//                    var e = graph.addEdge(i, j);
//                    cost[i][j] = maxpo + rand.nextInt(maxDepotCost - maxpo);
//                    graph.setEdgeWeight(e, cost[i][j]);
//                }
//            }
//        }
//        //pull in
//        int maxpi = maxPullIn();
//        for (int j = m; j < m + n; j++) {
//            for (int i = 0; i < m; i++) {
//                if (cost[j][i] < 0) {
//                    var e = graph.addEdge(j, i);
//                    cost[j][i] = maxpi + rand.nextInt(maxDepotCost - maxpi);
//                    graph.setEdgeWeight(e, cost[j][i]);
//                }
//            }
//        }
//        return new Instance("", m, n, nbVehicles, cost);
//    }
//
//    //trip1 is from tour1, trip2 is from tour2
//    private Tour createTour(Tour tour1, int trip1, int trip2) {
//        Tour tour = new Tour();
//        for (int i = 0; i <= tour1.indexOf(trip1); i++) {
//            tour.add(tour1.get(i));
//        }
//        Tour tour2 = parent[trip2];
//        for (int i = tour2.indexOf(trip2); i < tour2.size(); i++) {
//            tour.add(tour2.get(i));
//        }
//        return tour;
//    }
//
//    private int maxPullOut() {
//        int max = -1;
//        for (Tour t : tours) {
//            int c = cost[t.first()][t.second()];
//            if (c > max) {
//                max = c;
//            }
//        }
//        return max;
//    }
//
//    private int maxPullIn() {
//        int max = -1;
//        for (Tour t : tours) {
//            int c = cost[t.lastButOne()][t.last()];
//            if (c > max) {
//                max = c;
//            }
//        }
//        return max;
//    }
//
//    @Override
//    protected void writeExtrasToFile(PrintWriter out) {
//        out.println("best: " + best);
//        for (Tour t : tours) {
//            out.println(t);
//        }
//    }
//
//    public static void main(String args[]) {
//        var gen = new ShortestPathsGenerator(4, 500)
//                .avgTourSize(4)
//                .tripCosts(0, 1000)
//                .depotCosts(5000, 6000);
//        gen.build();
//        gen.writeToFile(0);
//    }
//
//}
