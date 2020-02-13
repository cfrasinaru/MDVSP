/*
 * Copyright (C) 2020 Cristian Frasinaru
 */

package ro.uaic.info.mdvsp.flow;

/**
 *
 * @author Cristian Frăsinaru
 */
public class TripTo extends Node {

    public TripTo(int trip) {
        super(trip);
    }

    @Override
    public String toString() {
        return "to trip:" + id;
    }    

}
