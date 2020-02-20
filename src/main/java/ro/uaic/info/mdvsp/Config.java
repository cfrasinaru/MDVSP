/*
 * Copyright (C) 2019 Cristian Frasinaru
 */
package ro.uaic.info.mdvsp;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author Cristian Frăsinaru
 */
public class Config {

    private static String dataPath;
    private static String algorithm;
    private static Properties props;
    private static Boolean outputEnabled;
    private static Integer populateLimit;
    private static Integer timeLimit;

    private static String solve;
    private static Integer depotsMin;
    private static Integer depotsMax;
    private static Integer tripsMin;
    private static Integer tripsMax;
    private static Integer instanceMin;
    private static Integer instanceMax;
    
    private static Double clusterFactor;

    static {
        init();
    }

    private Config() {
    }

    private static void init() {
        try {
            props = new Properties();
            props.load(new FileInputStream("config.properties"));
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }

    /**
     *
     * @return
     */
    public static String getDataPath() {
        if (dataPath == null) {
            dataPath = props.getProperty("data", "../MDVSP-data/");
        }
        return dataPath;
    }
    
    /**
     *
     * @return
     */
    public static String getAlgorithm() {
        if (algorithm == null) {
            algorithm = props.getProperty("algorithm", "simple");
        }
        return algorithm;
    }

    /**
     *
     * @return
     */
    public static boolean isOutputEnabled() {
        if (outputEnabled == null) {
            outputEnabled = Boolean.valueOf(props.getProperty("output", "false"));
        }
        return outputEnabled;
    }

    /**
     *
     * @return
     */
    public static int getPopulateLimit() {
        if (populateLimit == null) {
            populateLimit = Integer.parseInt(props.getProperty("populateLimit", "1"));
        }
        return populateLimit;
    }

    /**
     *
     * @return
     */
    public static int getTimeLimit() {
        if (timeLimit == null) {
            timeLimit = Integer.parseInt(props.getProperty("timeLimit", "5*60"));
        }
        return timeLimit;
    }
    
    /**
     *
     * @return
     */
    public static double getClusterFactor() {
        if (clusterFactor == null) {
            clusterFactor = Double.parseDouble(props.getProperty("clusterFactor", "0"));
        }
        return clusterFactor;
    }
    

    public static int getDepotsMin() {
        if (solve == null) {
            getSolve();
        }
        return depotsMin;
    }

    public static int getDepotsMax() {
        if (solve == null) {
            getSolve();
        }
        return depotsMax;
    }

    public static int getTripsMin() {
        if (solve == null) {
            getSolve();
        }
        return tripsMin;
    }

    public static int getTripsMax() {
        if (solve == null) {
            getSolve();
        }
        return tripsMax;
    }

    public static int getInstanceMin() {
        if (solve == null) {
            getSolve();
        }
        return instanceMin;
    }

    public static int getInstanceMax() {
        if (solve == null) {
            getSolve();
        }
        return instanceMax;
    }

    private static String getSolve() {
        if (solve == null) {
            solve = props.getProperty("solve");
        }
        String[] tokens = solve.split(",");
        depotsMin = Integer.parseInt(tokens[0]);
        depotsMax = Integer.parseInt(tokens[1]);
        tripsMin = Integer.parseInt(tokens[2]);
        tripsMax = Integer.parseInt(tokens[3]);
        instanceMin = Integer.parseInt(tokens[4]);
        instanceMax = Integer.parseInt(tokens[5]);
        return solve;

    }

}
