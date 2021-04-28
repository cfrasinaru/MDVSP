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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import ro.uaic.info.mdvsp.Instance;
import ro.uaic.info.mdvsp.Tour;

/**
 * Generates a random MDVSP createInstance.
 *
 *
 *
 * @author Cristian Frăsinaru
 */
public abstract class Generator {

    protected final int m;
    protected final int n;

    protected int avgTourSize; //trip nodes
    protected double density; //how many neighbors has
    protected int minTripCost, maxTripCost;
    protected int minDepotCost, maxDepotCost;

    protected int[] nbVehicles;
    protected int[][] cost;

    protected SimpleDirectedWeightedGraph<Integer, DefaultWeightedEdge> graph;
    protected final Random rand = new Random();

    protected List<Tour> tours; //optimum tours

    public Generator(int nbDepots, int nbTrips) {
        this.m = nbDepots;
        this.n = nbTrips;

        avgTourSize = 4; //excluding depots
        density = 1;
        minTripCost = 0;
        maxTripCost = 1000;
        minDepotCost = 5000;
        maxDepotCost = 6000;
    }

    /**
     *
     * @param min
     * @param max
     * @return
     */
    public Generator tripCosts(int min, int max) {
        this.minTripCost = min;
        this.maxTripCost = max;
        return this;
    }

    /**
     *
     * @param min
     * @param max
     * @return
     */
    public Generator depotCosts(int min, int max) {
        this.minDepotCost = min;
        this.maxDepotCost = max;
        return this;
    }

    /**
     *
     * @param avgTourSize
     * @return
     */
    public Generator avgTourSize(int avgTourSize) {
        this.avgTourSize = avgTourSize;
        return this;
    }

    /**
     *
     * @param density
     * @return
     */
    public Generator density(double density) {
        this.density = density;
        return this;
    }

    protected int randGauss(int mean) {
        int number;
        do {
            number = mean + (int) (Math.round(rand.nextGaussian()));
        } while (number <= 0);
        return number;
    }

    public abstract Instance build();

    protected Instance createInstance() {
        return new Instance(this.getClass().getSimpleName(), m, n, nbVehicles, cost);
    }

    /**
     *
     */
    protected void init() {
        this.cost = new int[m + n][m + n];
        for (int i = 0; i < m + n; i++) {
            for (int j = 0; j < m + n; j++) {
                cost[i][j] = -1;
            }
        }
        this.nbVehicles = new int[m];
        for (int i = 0; i < m; i++) {
            this.nbVehicles[i] = 0;
        }
        this.graph = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        for (int i = 0; i < n + m; i++) {
            graph.addVertex(i);
        }
        this.tours = new ArrayList<>();
    }

    protected List<Integer> shuffledTrips() {
        //Create a global shuffled list of trip numbers
        var shuffledTrips = IntStream.range(m, m + n).boxed().collect(Collectors.toList());
        Collections.shuffle(shuffledTrips);
        return shuffledTrips;
    }

    protected List<Integer> shuffledDepots() {
        var shuffledDepots = IntStream.range(0, m).boxed().collect(Collectors.toList());
        Collections.shuffle(shuffledDepots);
        return shuffledDepots;
    }

    /**
     * Pick a depot D. Create a close path D -> D, having a gaussian random
     * length, and random costs.
     *
     * @param size
     * @param trips
     * @return
     */
    protected Tour createTour(int size, Queue<Integer> trips) {
        Tour tour = new Tour();

        //Pick a depot
        int depot = rand.nextInt(m);
        tour.add(depot);
        Integer u = -1, v = -1;
        for (int i = 0; i < size; i++) {
            v = trips.poll();
            if (v == null) {
                break;
            }
            tour.add(v);
            if (u != -1) {
                //add uv edge
                var uv = graph.addEdge(u, v);
                cost[u][v] = minTripCost >= maxTripCost ? maxTripCost : minTripCost + rand.nextInt(maxTripCost - minTripCost);
                graph.setEdgeWeight(uv, cost[u][v]);
            } else {
                //v is the first node, add the pull out edge
                var dv = graph.addEdge(depot, v);
                cost[depot][v] = minDepotCost >= maxDepotCost ? maxDepotCost : minDepotCost + rand.nextInt(maxDepotCost - minDepotCost);
                graph.setEdgeWeight(dv, cost[depot][v]);
            }
            u = v;
        }
        //u is now the last node, add the pull in edge to d
        tour.add(depot);
        var ud = graph.addEdge(u, depot);
        cost[u][depot] = minDepotCost >= maxDepotCost ? maxDepotCost : minDepotCost + rand.nextInt(maxDepotCost - minDepotCost);
        graph.setEdgeWeight(ud, cost[u][depot]);
        return tour;
    }

    /**
     *
     * @param trip
     * @return
     */
    protected Tour getTour(int trip) {
        for (Tour t : tours) {
            if (t.contains(trip)) {
                return t;
            }
        }
        return null;
    }

    /**
     * Creates a random ordered list of trips (based on the underlying DAG).
     *
     * @return
     */
    protected List<Integer> orderedTrips() {
        //create an ordering of the trips (respecting the tours)
        double[] order = new double[m + n]; //[0,1)
        for (Tour tour : tours) {
            //int k = rand.nextInt(n - tour.size());
            double prev = 0;
            for (int trip : tour) {
                //order[trip] = ++k; //each trip gets an order number
                order[trip] = prev + Math.random() * (1 - prev);
                prev = order[trip];
            }
        }
        //create a sorted list of trips, based on their order number
        var orderedTrips = IntStream.range(m, m + n).boxed()
                .sorted((t1, t2) -> (int) Math.signum(order[t1] - order[t2])).collect(Collectors.toList());
        return orderedTrips;
    }

    /**
     * Creates an array containing the position of each trip in the ordered list
     * of trips. DAG ordering.
     *
     * @param orderedTrips
     * @return
     */
    protected int[] orderedTripsPos(List<Integer> orderedTrips) {
        //position of each trip in the ordered list of trips
        int[] pos = new int[m + n];
        for (int i = 0; i < orderedTrips.size(); i++) {
            pos[orderedTrips.get(i)] = i; //pos is between 0 and n-1
        }
        return pos;
    }

    protected int maxPullOut() {
        int max = -1;
        for (Tour t : tours) {
            int c = cost[t.first()][t.second()];
            if (c > max) {
                max = c;
            }
        }
        return max;
    }

    protected int maxPullIn() {
        int max = -1;
        for (Tour t : tours) {
            int c = cost[t.lastButOne()][t.last()];
            if (c > max) {
                max = c;
            }
        }
        return max;
    }

    protected void addRandomEdges(boolean preserveOptimum) {
        //create an ordering of the trips (respecting the tours)
        var orderedTrips = orderedTrips();
        int[] pos = orderedTripsPos(orderedTrips);//pos[i] is between 0 and n-1

        //for each trip, add dummy edges to another trips
        //more expensive than the one in the tour
        for (Tour tour : tours) {
            for (int i = 1; i < tour.size() - 2; i++) {
                int trip = tour.get(i);
                int next = tour.get(i + 1);
                int opt = preserveOptimum ? cost[trip][next] : minTripCost;
                for (int j = pos[trip] + 1; j < n; j++) {
                    int other = orderedTrips.get(j);
                    if (other == next || cost[trip][other] >= 0) {
                        continue;
                    }
                    var e = graph.addEdge(trip, other);
                    cost[trip][other] = opt + rand.nextInt(maxTripCost - opt);
                    graph.setEdgeWeight(e, cost[trip][other]);
                }
            }
        }

        //pull out
        int maxpo = preserveOptimum ? maxPullOut() : minDepotCost;
        for (int i = 0; i < m; i++) {
            for (int j = m; j < m + n; j++) {
                if (cost[i][j] < 0) {
                    var e = graph.addEdge(i, j);
                    cost[i][j] = maxpo >= maxDepotCost ? maxDepotCost : maxpo + rand.nextInt(maxDepotCost - maxpo);
                    //cost[i][j] = maxDepotCost;
                    graph.setEdgeWeight(e, cost[i][j]);
                }
            }
        }
        //pull in
        int maxpi = preserveOptimum ? maxPullIn() : minDepotCost;
        for (int j = m; j < m + n; j++) {
            for (int i = 0; i < m; i++) {
                if (cost[j][i] < 0) {
                    var e = graph.addEdge(j, i);
                    cost[j][i] = maxpi >= maxDepotCost ? maxDepotCost : maxpi + rand.nextInt(maxDepotCost - maxpi);
                    //cost[j][i] = maxDepotCost;
                    graph.setEdgeWeight(e, cost[j][i]);
                }
            }
        }

    }

    /**
     * Re-create the graph
     */
    protected void createGraph() {
        graph = new SimpleDirectedWeightedGraph(DefaultWeightedEdge.class);
        for (int i = 0; i < n + m; i++) {
            graph.addVertex(i);
        }
        for (int i = 0; i < n + m; i++) {
            for (int j = 0; j < n + m; j++) {
                if (cost[i][j] >= 0) {
                    DefaultWeightedEdge e = graph.addEdge(i, j);
                    graph.setEdgeWeight(e, cost[i][j]);
                }
            }
        }
    }

    /**
     *
     */
    protected void computeNbVehicles() {
        for (Tour tour : tours) {
            nbVehicles[tour.first()] += 1;
        }
    }

    /**
     *
     * @return
     */
    protected int computeCost() {
        int total = 0;
        for (Tour tour : tours) {
            total += tour.getCost(cost);
        }
        return total;
    }

    /**
     *
     * @param trip
     * @return
     */
    protected int maxNextTripCost(int trip) {
        int max = minTripCost;
        for (int i = m; i < m + n; i++) {
            if (trip == i) {
                continue;
            }
            if (cost[trip][i] > max) {
                max = cost[trip][i];
            }
        }
        return max;
    }

    /**
     * @return the minTripCost
     */
    public int getMinTripCost() {
        return minTripCost;
    }

    /**
     * @param minTripCost the minTripCost to set
     */
    public void setMinTripCost(int minTripCost) {
        this.minTripCost = minTripCost;
    }

    /**
     * @return the maxTripCost
     */
    public int getMaxTripCost() {
        return maxTripCost;
    }

    /**
     * @param maxTripCost the maxTripCost to set
     */
    public void setMaxTripCost(int maxTripCost) {
        this.maxTripCost = maxTripCost;
    }

    /**
     * @return the minDepotCost
     */
    public int getMinDepotCost() {
        return minDepotCost;
    }

    /**
     * @param minDepotCost the minDepotCost to set
     */
    public void setMinDepotCost(int minDepotCost) {
        this.minDepotCost = minDepotCost;
    }

    /**
     * @return the maxDepotCost
     */
    public int getMaxDepotCost() {
        return maxDepotCost;
    }

    /**
     * @param maxDepotCost the maxDepotCost to set
     */
    public void setMaxDepotCost(int maxDepotCost) {
        this.maxDepotCost = maxDepotCost;
    }

}
