/*
 * Copyright (C) 2020 Cristian Frasinaru
 */
package ro.uaic.info.mdvsp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author Cristian Frăsinaru
 */
public class Solution {
    
    private final Model model;
    private final int n; //trips
    private final int m; //depots
    private final int[][] cost;
    
    private final int x[][];
    private List<Tour> tours;

    /**
     * Creates an empty solution.
     *
     * @param model
     */
    public Solution(Model model) {
        this.model = model;
        this.n = model.nbTrips();
        this.m = model.nbDepots();
        this.cost = model.getCost();
        this.x = new int[m + n][m + n];
    }

    /**
     *
     * @param i
     * @param j
     * @param value
     */
    public void set(int i, int j, int value) {
        x[i][j] = value;
    }

    /**
     *
     * @param i
     * @param j
     * @return
     */
    public int get(int i, int j) {
        return x[i][j];
    }

    /**
     *
     * @return
     */
    public List<Tour> getTours() {
        if (tours == null) {
            createTours();
        }
        return tours;
    }

    /**
     *
     * @return
     */
    public List<Tour> getBadTours() {
        return getTours().stream().filter(t -> t.isBad()).collect(Collectors.toList());
    }

    /**
     *
     * @return
     */
    public int[][] getMatrix() {
        return x;
    }

    /**
     *
     * @return
     */
    public int totalCost() {
        return totalCost(x);
    }

    /**
     *
     * @param s
     * @return
     */
    public int totalCost(int s[][]) {
        int total = 0;
        for (int i = 0; i < n + m; i++) {
            for (int j = 0; j < n + m; j++) {
                total += cost[i][j] * s[i][j];
            }
        }
        return total;
    }
    
    private void createTours() {
        this.tours = new ArrayList<>();
        boolean visited[] = new boolean[n + m];
        Arrays.fill(visited, 0, n + m, false);
        for (int i = 0; i < n + m; i++) {
            for (int j = 0; j < n + m; j++) {
                if (x[i][j] == 0 || visited[i]) {
                    continue;
                }
                if (i >= m) {
                    visited[i] = true;
                }
                Tour tour = new Tour();
                tours.add(tour);
                tour.add(i);
                int next = j;
                while (next >= 0 && !visited[next]) {
                    if (next >= m) {
                        visited[next] = true;
                    }
                    tour.add(next);
                    next = findNext(next);
                }
            }
        }
    }
    
    private int findNext(int i) {
        if (i < m) {
            return -1;
        }
        for (int j = 0; j < n + m; j++) {
            if (x[i][j] > 0) {
                return j;
            }
        }
        return -1;
    }

    /**
     *
     * @return
     */
    public int[] getUsedVehicles() {
        int[] used = new int[m];
        for (int i = 0; i < m; i++) {
            used[i] = 0;
            for (int j = m; j < m + n; j++) {
                if (x[i][j] > 0) {
                    used[i]++;
                }
            }
        }
        return used;
    }

    /**
     *
     * @param i
     * @return
     */
    public int getDepot(int i) {
        for (Tour t : getTours()) {
            if (t.contains(i)) {
                return t.first();
            }
        }
        throw new RuntimeException("No depot for trip " + i);
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        return toursToString();
    }

    /**
     *
     * @return
     */
    public String toursToString() {
        getTours();
        StringBuilder sb = new StringBuilder();
        for (Tour tour : getTours()) {
            sb.append(tourToString(tour)).append("\n");
        }
        return sb.toString();
    }
    
    private String tourToString(Tour tour) {
        StringBuilder sb = new StringBuilder();
        sb.append(tour.get(0));
        for (int i = 1; i < tour.size(); i++) {
            int t0 = tour.get(i - 1);
            int t1 = tour.get(i);
            sb.append("--(").append(cost[t0][t1]).append(")-->").append(t1);
        }
        //sb.append(" [").append(id).append("]");
        return sb.toString();
    }

    /**
     *
     * @return
     */
    public String matrixToString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n + m; i++) {
            for (int j = 0; j < n + m; j++) {
                if (x[i][j] > 0) {
                    sb.append("x[").append(i).append("][").append(j).append("]=").append(x[i][j]);
                    sb.append("\n");
                }
            }
        }
        return sb.toString();
    }
    
    public String usedVehiclesToString() {
        int nbv[] = getUsedVehicles();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < m; i++) {
            sb.append("Depot ").append(i).append(": ").append(nbv[i]).append(" vehicles").append("\n");
        }
        return sb.toString();
    }

    /**
     * Verifies if the solution is correct.
     *
     * @throws InvalidSolutionException
     */
    public void check() throws InvalidSolutionException {
        //from each depot, max nbVehicles
        for (int i = 0; i < m; i++) {
            int nv = model.nbVehicles(i);
            int rowSum = rowSum(i);
            int colSum = colSum(i);
            if (rowSum > nv) {
                throw new InvalidSolutionException("Too many vehicles starting from depot " + i + ": " + rowSum);
            }
            if (colSum > nv) {
                throw new InvalidSolutionException("Too many vehicles returning at depot " + i + ": " + colSum);
            }
            if (rowSum != colSum) {
                throw new InvalidSolutionException("Different number of starting and returning vehicles at depot "
                        + i + ": " + rowSum + " <> " + colSum);
            }
        }
        //each trip is saturated
        for (int i = m; i < m + n; i++) {
            if (rowSum(i) != 1) {
                throw new InvalidSolutionException("Invalid row count for trip " + i + ": " + rowSum(i));
            }
            if (colSum(i) != 1) {
                throw new InvalidSolutionException("Invalid col count for trip " + i + ": " + colSum(i));
            }
        }
        //all tours start and end in the same depot
        for (Tour t : getTours()) {
            if (t.isBad()) {
                throw new InvalidSolutionException("Bad tour " + t);
            }
        }
        
    }
    
    private int rowSum(int i) {
        int sum = 0;
        for (int j = 0; j < m + n; j++) {
            sum += x[i][j];
        }
        return sum;
    }
    
    private int colSum(int i) {
        int sum = 0;
        for (int j = 0; j < m + n; j++) {
            sum += x[j][i];
        }
        return sum;
    }
    
}
