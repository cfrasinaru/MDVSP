/*
 * Copyright (C) 2020 Cristian Frasinaru
 */
package ro.uaic.info.mdvsp.flow;

/**
 *
 * @author Cristian Frăsinaru
 */
public class Source extends Node {

    public Source(int depot) {
        super(depot);
    }

    @Override
    public String toString() {
        return "source:" + id;
    }
        
}
