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
    private int component = 0;

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

    /**
     * @return the component
     */
    public int getComponent() {
        return component;
    }

    /**
     * @param component the component to set
     */
    public void setComponent(int component) {
        this.component = component;
    }

    /**
     *
     * @param model
     * @return
     */
    public int getCost(Model model) {
        return getCost(model.cost);
    }

    /**
     *
     * @param costMatrix
     * @return
     */
    public int getCost(int[][] costMatrix) {
        int cost = 0;
        int prev = get(0);
        for (int j = 1; j < size(); j++) {
            int next = get(j);
            cost += costMatrix[prev][next];
            prev = next;
        }
        return cost;
    }

    /**
     * 
     * @param costMatrix
     * @return 
     */
    public int pullOutCost(int[][] costMatrix) {
        return costMatrix[first()][second()];
    }

    /**
     * 
     * @param costMatrix
     * @return 
     */
    public int pullInCost(int[][] costMatrix) {
        return costMatrix[lastButOne()][last()];
    }
    
    /**
     *
     * @param amount
     */
    public void incrementTrips(int amount) {
        for (int i = 1; i < size() - 1; i++) {
            set(i, get(i) + amount);
        }
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

    //trip1 is from tour1, trip2 is from tour2
    public static Tour combine(Tour tour1, Tour tour2, int trip1, int trip2) {
        Tour tour = new Tour();
        for (int i = 0; i <= tour1.indexOf(trip1); i++) {
            tour.add(tour1.get(i));
        }
        for (int i = tour2.indexOf(trip2); i < tour2.size(); i++) {
            tour.add(tour2.get(i));
        }
        return tour;
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

    public String toDetailedString(int cost[][]) {
        StringBuilder sb = new StringBuilder();
        sb.append(get(0));
        for (int i = 1; i < size(); i++) {
            int t0 = get(i - 1);
            int t1 = get(i);
            sb.append(" --(").append(cost[t0][t1]).append(")--> ").append(t1);
        }
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
                return 1;
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
        int hash = super.hashCode();
        hash = 19 * hash + this.id;
        hash = 19 * hash + this.component;
        return hash;
    }

}
