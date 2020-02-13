/*
 * Copyright (C) 2019 Cristian Frasinaru
 */
package ro.uaic.info.mdvsp.repair;

import java.util.ArrayList;
import java.util.List;
import ro.uaic.info.mdvsp.Tour;

/**
 * Self repair - target variant
 *
 * @author Cristian Frăsinaru
 */
public class RepairImpl1b implements Repair {

    private final Tour tour;
    private final int value;
    private final int pos;

    public RepairImpl1b(Tour tour, int value) {
        this.tour = tour;
        this.pos = 1;
        this.value = value;
    }

    /**
     * @return the tour
     */
    public Tour getTour() {
        return tour;
    }

    /**
     * @return the pos
     */
    public int getPos() {
        return pos;
    }

    /**
     * @return the value
     */
    @Override
    public int getValue() {
        return value;
    }

    @Override
    public boolean isValid() {
        return value < Integer.MAX_VALUE;
    }

    public Tour getRepairedTour() {
        Tour t = new Tour();
        t.add(tour.last());
        for (int i = 1; i < tour.size() - 1; i++) {
            t.add(tour.get(i));
        }
        t.add(tour.last());
        return t;
    }

    @Override
    public List<Tour> getRepairedTours() {
        List<Tour> tours = new ArrayList<>();
        tours.add(getRepairedTour());
        return tours;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(tour).
                append(", pos = ").append(tour.get(pos)).
                append(", repair = ").append(value).
                append("\n\t").append(getRepairedTour());
        return sb.toString();
    }

}
