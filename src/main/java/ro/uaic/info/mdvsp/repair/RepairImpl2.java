/*
 * Copyright (C) 2019 Cristian Frasinaru
 */
package ro.uaic.info.mdvsp.repair;

import java.util.ArrayList;
import java.util.List;
import ro.uaic.info.mdvsp.Tour;

/**
 * Repair two tours
 * @author Cristian Frăsinaru
 */
public class RepairImpl2 implements Repair {

    private final Tour tour1;
    private final Tour tour2;
    private final int pos1;
    private final int pos2;
    private final int value;

    /**
     *
     * @param tour1
     * @param tour2
     * @param pos1
     * @param pos2
     * @param value
     */
    public RepairImpl2(Tour tour1, Tour tour2, int pos1, int pos2, int value) {
        this.tour1 = tour1;
        this.tour2 = tour2;
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.value = value;
    }

    /**
     * @return the tour1
     */
    public Tour getTour1() {
        return tour1;
    }

    /**
     * @return the tour2
     */
    public Tour getTour2() {
        return tour2;
    }

    /**
     * @return the pos1
     */
    public int getPos1() {
        return pos1;
    }

    /**
     * @return the pos2
     */
    public int getPos2() {
        return pos2;
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

    public Tour gerRepairedTour1() {
        Tour t = new Tour();
        for (int i = 0; i <= pos1; i++) {
            t.add(tour1.get(i));
        }
        for (int i = pos2; i < tour2.size(); i++) {
            t.add(tour2.get(i));
        }
        return t;
    }

    public Tour gerRepairedTour2() {
        Tour t = new Tour();
        for (int i = 0; i < pos2; i++) {
            t.add(tour2.get(i));
        }
        for (int i = pos1 + 1; i < tour1.size(); i++) {
            t.add(tour1.get(i));
        }
        return t;
    }

    @Override
    public List<Tour> getRepairedTours() {
        List<Tour> tours = new ArrayList<>();
        tours.add(gerRepairedTour1());
        tours.add(gerRepairedTour2());
        return tours;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(tour1).append(" --- ").append(tour2).
                append(", pos1 = ").append(tour1.get(pos1)).
                append(", pos2 = ").append(tour2.get(pos2)).
                append(", repair = ").append(value).
                append("\n\t").append(gerRepairedTour1()).
                append("\n\t").append(gerRepairedTour2());

        return sb.toString();
    }

}
