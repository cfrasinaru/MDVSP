/*
 * Copyright (C) 2019 Cristian Frasinaru
 */
package ro.uaic.info.mdvsp;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Cristian Frăsinaru
 */
public class Tools {

    public static double round(double number, int prec) {
        double factor = Math.pow(10, prec);
        return (double) (Math.round(number * factor)) / factor;
    }

    /**
     *
     * @param a
     */
    public static void printMatrix(int[][] a) {
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[i].length; j++) {
                System.out.print(a[i][j] + "\t");
            }
            System.out.println();
        }
    }

    /*
    public static List<Tour>[][] createTourMatrix(Model model) {
        int m = model.nbDepots();
        List<Tour>[][] tourMatrix = new ArrayList[m][m];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < m; j++) {
                tourMatrix[i][j] = new ArrayList<>();
            }
        }
        for (Tour tour : model.getTours()) {
            int i = tour.first();
            int j = tour.last();
            tourMatrix[i][j].add(tour);
        }
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < m; j++) {
                System.out.print(tourMatrix[i][j].size() + "\t");
            }
            System.out.println();
        }
        return tourMatrix;
    }*/

    /*
    public static int repair(Model model) {
        List<Tour> modelTours = model.getTours();
        List<Tour> tours = modelTours.stream().filter(t -> t.isBad()).collect(Collectors.toList());
        Map<Tour, Tour> map = new HashMap<>();
        Set<Tour> part1 = new HashSet<>();
        Set<Tour> part2 = new HashSet<>();
        SimpleWeightedGraph<Tour, DefaultWeightedEdge> graph
                = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

        int k = tours.size();
        for (int i = 0; i < k; i++) {
            Tour t1 = tours.get(i);
            t1.setId(i + 1);
            Tour t2 = new Tour(tours.get(i), -t1.getId());
            part1.add(t1);
            part2.add(t2);
            graph.addVertex(t1);
            graph.addVertex(t2);
            map.put(t1, t2);
            map.put(t2, t1);
        }

        for (int i = 0; i < k; i++) {
            Tour t1 = tours.get(i);
            for (int j = 0; j < k; j++) {
                Tour t2 = tours.get(j);
                Repair repair = model.repair(t1, t2);
                DefaultWeightedEdge e = graph.addEdge(t1, map.get(t2));
                graph.setEdgeWeight(e, repair.getValue());
            }
        }

        List<Tour> repairedTours = modelTours.stream().filter(t -> !t.isBad()).collect(Collectors.toList());
        int total = model.totalCost();
        KuhnMunkresMinimalWeightBipartitePerfectMatching alg
                = new KuhnMunkresMinimalWeightBipartitePerfectMatching(graph, part1, part2);
        Matching match = alg.getMatching();
        int q = 0;
        for (var obj : match.getEdges()) {
            DefaultWeightedEdge e = (DefaultWeightedEdge) obj;
            total += graph.getEdgeWeight(e);
            //
            Tour t1 = graph.getEdgeSource(e);
            Tour t2 = graph.getEdgeTarget(e);
            if (Math.abs(t1.getId()) == Math.abs(t2.getId())) {
                q++;
            }
            Repair repair = model.repair(t1, t2);
            repairedTours.add(repair.getRepairedTours().get(0));
            repair.getRepairedTours().size();
        }
        //model.tours = repairedTours;
        return total;
    }
    */

    public static void tourAnalyzer(List<Tour> tours) {
        System.out.println("Count: " + tours.size());
        System.out.println("Max: " + tours.stream().mapToInt(t -> t.size()).max());
        System.out.println("Min: " + tours.stream().mapToInt(t -> t.size()).min());
        System.out.println("Average: " + tours.stream().mapToInt(t -> t.size()).average());
    }

    public static void main(String args[]) {
        for (int i = 0; i < 255; i++) {
            System.out.println((char) i);
        }
    }
}
