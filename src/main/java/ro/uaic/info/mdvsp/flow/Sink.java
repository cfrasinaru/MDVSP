/*
 * Copyright (C) 2020 Cristian Frasinaru
 */

package ro.uaic.info.mdvsp.flow;

/**
 *
 * @author Cristian Frăsinaru
 */
public class Sink extends Node {

    public Sink(int depot) {
        super(depot);
    }

    @Override
    public String toString() {
        return "sink:" + id;
    }
    
}
