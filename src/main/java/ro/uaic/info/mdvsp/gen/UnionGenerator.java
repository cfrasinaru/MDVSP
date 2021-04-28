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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import ro.uaic.info.mdvsp.Instance;
import ro.uaic.info.mdvsp.Model;
import ro.uaic.info.mdvsp.Solution;
import ro.uaic.info.mdvsp.Tour;
import ro.uaic.info.mdvsp.gurobi.Model3D;

/**
 * Union of two random generated instances.
 *
 * The union may be disjoint [or full-join NOT].
 *
 *
 *
 * @author Cristian Frăsinaru
 */
public class UnionGenerator extends Generator {
    
    private int comps = 0;
    private static int n0 = 100;

    public UnionGenerator(int nbDepots, int nbTrips) {
        super(nbDepots, nbTrips);
    }

    /**
     *
     * @return
     */
    @Override
    public Instance build() {
        init();
        this.comps = n / n0;
        var gen = new RandomGenerator(m, n0)
                .avgTourSize(avgTourSize)
                .tripCosts(minTripCost, maxTripCost)
                .depotCosts(minDepotCost, maxDepotCost);

        for (int comp = 0; comp < comps; comp++) {
            Instance inst = gen.build();
            inst.setMaxVehicles();

            for (int i = 0; i < m; i++) {
                for (int j = m; j < n0 + m; j++) {
                    cost[i][j + comp * n0] = inst.cost(i, j);
                    cost[j + comp * n0][i] = inst.cost(j, i);
                }
            }

            for (int i = m; i < n0 + m; i++) {
                for (int j = m; j < n0 + m; j++) {
                    cost[i + comp * n0][j + comp * n0] = inst.cost(i, j);
                }
            }

            Model model = new Model3D(inst);
            model.solve();
            Solution sol = model.getSolution();

            for (Tour t0 : sol.getTours()) {
                t0.setComponent(comp);
                t0.incrementTrips(n0 * comp);
                tours.add(t0);
            }
        }

        createGraph(); //recreate the graph;

        //joinRandomTours();
        //addRandomEdges();
        //joinAllTrips();
        computeNbVehicles();

        Instance instance = createInstance();
        instance.setKnownOptimum(computeCost());
        return instance;
    }

    /*
    Full join between components, with the same cost.
     */
    private void joinAllTrips() {
        int x = minTripCost + rand.nextInt(maxTripCost - minTripCost);
        for (int i = m; i < n0 + m; i++) {
            for (int j = m + n0; j < 2 * n0 + m; j++) {
                cost[i][j] = x;
            }
        }
    }

    /*
    Only works if the depot-trip-depot costs are all equal
     */
    private void joinRandomTours() {
        //pick two random tours t0 and t1 in differenct components
        //the shorter tours should have priority
        //connect the last trip from t0 with the first trip of t1  
        List<Tour> sorted0 = new ArrayList<>(tours);
        List<Tour> sorted1 = new ArrayList<>(tours);
        Collections.sort(sorted0, (t0, t1) -> t0.pullOutCost(cost) - t1.pullOutCost(cost));
        Collections.sort(sorted1, (t0, t1) -> t0.pullInCost(cost) - t1.pullInCost(cost));

        int pos[] = orderedTripsPos(orderedTrips());
        List<Tour> remove = new ArrayList<>();
        List<Tour> insert = new ArrayList<>();
        start:
        for (Tour t0 : sorted0) {
            if (remove.contains(t0)) {
                continue;
            }
            for (Tour t1 : sorted1) {
                if (t0 == t1 || t0.getComponent() >= t1.getComponent() || t0.first() != t1.first() || remove.contains(t1)) {
                    continue;
                }
                int trip = t0.lastButOne();
                int other = t1.second();
                if (pos[trip] >= pos[other]) {
                    //continue;
                }
                remove.add(t0);
                remove.add(t1);
                Tour tt = Tour.combine(t0, t1, trip, other);
                insert.add(tt);
                //
                cost[trip][other] = minTripCost + rand.nextInt(maxTripCost - minTripCost);
                //cost[trip][other] = minTripCost;                
                //best = best - cost[trip][t0.last()] - cost[t1.first()][other] + cost[trip][other];                
                //cost[trip][t0.last()] = minDepotCost;
                //cost[t1.first()][other] = minDepotCost;

                /*
                System.out.println("edge " + trip + "--" + other + " \t= " + cost[trip][other]);
                System.out.println("pullIn " + trip + "--" + t0.last() + " \t= " + cost[trip][t0.last()]);
                System.out.println("pullOut " + t1.first() + "--" + other + " \t= " + cost[t1.first()][other]);
                 */
                break;
            }
        }
        tours.removeAll(remove);
        tours.addAll(insert);
    }

    /*
    Does not work.
     */
    private void addRandomEdges() {
        for (Tour tour : tours) {
            for (int i = 1; i < tour.size() - 2; i++) {
                int trip = tour.get(i);
                int next = tour.get(i + 1);
                //int opt = cost[trip][next];
                int opt = maxNextTripCost(trip);
                int step = (trip - m) / 100;
                for (int other = m + n0 * (step + 1); other < m + n; other++) {
                    Tour tt = getTour(other);
                    if (other == tt.second() || other == tt.lastButOne()) {
                        continue;
                    }
                    Tour qq = Tour.combine(tour, tt, trip, other);
                    if (qq.getCost(cost) < tour.getCost(cost)) {
                        continue;
                    }
                    if (Math.random() < 0.5) {
                        //continue;
                    }
                    var e = graph.addEdge(trip, other);
                    cost[trip][other] = opt + rand.nextInt(maxTripCost - opt);
                    //cost[trip][other] = maxTripCost;
                    graph.setEdgeWeight(e, cost[trip][other]);
                }
            }
        }
    }

    public static void main(String args[]) throws IOException {
        n0 = 100;
        var gen = new UnionGenerator(4, 200)
                .avgTourSize(4)
                .tripCosts(0, 1000)
                .depotCosts(5000, 6000);
        System.out.println("Generate");
        Instance inst = gen.build();
        inst.write(0);
        System.out.println("Known optimum \t= " + inst.getKnownOptimum());
        System.out.println("tours=" + gen.tours.size());

        System.out.println("Solve exact");
        Model pb = new Model3D(inst);
        pb.solve();
        Solution sol = pb.getSolution();
        System.out.println("\t" + sol.totalCost());
        System.out.println(inst.getKnownOptimum() == sol.totalCost() ? "\tBINGO!" : "\tNOPE...");
        System.out.println("tours=" + sol.getTours().size());

        int bprim[] = {0, 0};
        for (Tour t : sol.getTours()) {
            bprim[(t.second() - gen.m) / n0] += gen.cost[t.first()][t.second()];
            bprim[(t.lastButOne() - gen.m) / n0] += gen.cost[t.lastButOne()][t.last()];
            for (int i = 1; i < t.size() - 2; i++) {
                if ((t.get(i) - gen.m) / n0 == (t.get(i + 1) - gen.m) / n0) {
                    bprim[(t.get(i) - gen.m) / n0] += gen.cost[t.get(i)][t.get(i + 1)];
                } else {
                    if (t.get(i) > gen.m && t.get(i + 1) > gen.m) {
                        //System.out.println("avoided " + t.get(i) + "--" + t.get(i + 1));
                    }
                }
            }
        }
        System.out.println("bprim0 = " + bprim[0]);
        System.out.println("bprim1 = " + bprim[1]);

        System.out.println();
        if (inst.getKnownOptimum() != sol.totalCost()) {
            System.out.println("Generated tours");
            for (Tour t : gen.tours) {
                System.out.println(t);
            }
            System.out.println("-------------------------------------------------");
            System.out.println("Computed tours");
            for (Tour t : sol.getTours()) {
                System.out.println(t);
            }

        }
        /*
        for (Tour t : sol.getTours()) {
            if (!gen.tours.contains(t)) {
                System.out.println(t + "\t" + t.getCost(gen.cost));
            }
            for (int i = 1; i < t.size() - 2; i++) {
                if ((t.get(i) - gen.m) / 100 != (t.get(i + 1) - gen.m) / 100) {
                    System.out.println("??????????????????");
                    System.out.println("\t" + t);
                    break;
                }
            }
        }
        System.out.println("-------------------------------------------------");
        for (Tour t : gen.tours) {
            if (!sol.getTours().contains(t)) {
                System.out.println(t + "\t" + t.getCost(gen.cost));
            }
        }
         */
 /*
        System.out.println("Solve heuristic");
        pb = new RepairModel(new ModelRelaxed(inst));
        pb.solve();
        System.out.println("\t" + pb.getSolution().totalCost());
         */
    }

}
