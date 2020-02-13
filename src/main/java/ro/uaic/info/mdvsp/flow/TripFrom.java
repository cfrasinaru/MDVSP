/*
 * Copyright (C) 2020 Cristian Frasinaru
 */

package ro.uaic.info.mdvsp.flow;

/**
 *
 * @author Cristian Frăsinaru
 */
public class TripFrom extends Node {
    int trip;

    public TripFrom(int trip) {
        super(trip);
    }

    @Override
    public String toString() {
        return "from trip:" + id;
    }
    
}
