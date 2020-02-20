/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ro.uaic.info.mdvsp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Cristian Frăsinaru
 */
public class Tour extends ArrayList<Integer> implements Comparable<Tour> {

    private int id;

    public Tour() {
        id = 0;
    }

    public Tour(int id) {
        this.id = id;
    }

    public Tour(List<Integer> other) {
        super(other);
        this.id = 0;
    }

    public Tour(List<Integer> other, int id) {
        super(other);
        this.id = id;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    public int getCost(Model model) {
        int cost = 0;
        int prev = get(0);
        for (int j = 1; j < size(); j++) {
            int next = get(j);
            cost += model.cost[prev][next];
            prev = next;
        }
        return cost;
    }

    public static List<Tour> load(String filename) {
        List<Tour> tours = new ArrayList<>();
        try {
            Path path = Paths.get(filename);
            for (String line : Files.readAllLines(path)) {
                if (line.isBlank()) {
                    continue;
                }
                Tour tour = new Tour();
                String x[] = line.split("\\s+");
                for (int i = 0; i < x.length; i++) {
                    tour.add(Integer.parseInt(x[i]));
                }
                tours.add(tour);
            }
        } catch (IOException ex) {
            System.err.println("No bad tours file...");
        }
        return tours;
    }

    public int first() {
        return get(0);
    }

    public int second() {
        return get(1);
    }

    public int last() {
        return get(size() - 1);
    }

    public int lastButOne() {
        return get(size() - 2);
    }

    public boolean isBad() {
        return first() != last();
    }

    @Override
    public String toString() {
        return toString("->");
    }

    /**
     *
     * @param delimiter
     * @return
     */
    public String toString(String delimiter) {
        StringBuilder sb = new StringBuilder();
        sb.append(get(0));
        for (int i = 1; i < size(); i++) {            
            sb.append(delimiter).append(get(i));
        }
        //sb.append(" [").append(id).append("]");
        return sb.toString();
    }

    @Override
    public int compareTo(Tour other) {
        if (this.id != 0 && other.id != 0) {
            return this.id - other.id;
        }
        int n = this.size();
        if (n > other.size()) {
            n = other.size();
        }
        for (int i = 0; i < n; i++) {
            if (this.get(i) < other.get(i)) {
                return -1;
            }
            if (this.get(i) > other.get(i)) {
                return -1;
            }
        }
        return this.size() - other.size();
    }

    public static int compareByTrips(Tour t0, Tour t1) {
        int n = t0.size();
        if (n > t1.size()) {
            n = t1.size();
        }
        for (int i = 0; i < n; i++) {
            if (t0.get(i) < t1.get(i)) {
                return -1;
            }
            if (t0.get(i) > t1.get(i)) {
                return 1;
            }
        }
        return t0.size() - t1.size();
    }

    @Override
    public boolean equals(Object o) {
        Tour other = (Tour) o;
        if (this.id == 0 || other.id == 0) {
            return compareByTrips(this, other) == 0;
        }
        return this.id == other.id;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

}
