/*
 * Copyright (C) 2019 Cristian Frasinaru
 */
package ro.uaic.info.mdvsp.repair;

import java.util.ArrayList;
import java.util.List;
import ro.uaic.info.mdvsp.Tour;

/**
 * Self repair - source variant
 *
 * @author Cristian Frăsinaru
 */
public class RepairImpl1a implements Repair {

    private final Tour tour;
    private final int value;
    private final int pos;

    public RepairImpl1a(Tour tour, int value) {
        this.tour = tour;
        this.pos = tour.size() - 2;
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
        for (int i = 0; i <= pos; i++) {
            t.add(tour.get(i));
        }
        t.add(tour.first());
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
