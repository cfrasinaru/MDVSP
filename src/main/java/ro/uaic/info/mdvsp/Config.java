/*
 * Copyright (C) 2019 Cristian Frasinaru
 */
package ro.uaic.info.mdvsp;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 *
 * @author Cristian Frăsinaru
 */
public class Config {

    private static Properties props;
    private static final Map<String, Object> MAP = new HashMap<>();

    private static String solve;
    private static Integer depotsMin;
    private static Integer depotsMax;
    private static Integer tripsMin;
    private static Integer tripsMax;
    private static Integer instanceMin;
    private static Integer instanceMax;

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

    private static Object get(String key, Object defaultValue) {
        Object value = MAP.get(key);
        if (value == null) {
            String str = props.getProperty(key);
            if (str == null) {
                value = defaultValue;
            } else {
                if (defaultValue instanceof String) {
                    value = str;
                } else if (defaultValue instanceof Boolean) {
                    value = Boolean.parseBoolean(str);
                } else if (defaultValue instanceof Integer) {
                    value = Integer.parseInt(str);
                } else if (defaultValue instanceof Double) {
                    value = Double.parseDouble(str);
                }
            }
            MAP.put(key, value);
        }        
        return value;
    }

    public static String getString(String key, String defaultValue) {
        return (String) get(key, defaultValue);
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        return (Boolean) get(key, defaultValue);
    }

    public static int getInt(String key, int defaultValue) {
        return (Integer) get(key, defaultValue);
    }

    public static double getDouble(String key, double defaultValue) {
        return (Double) get(key, defaultValue);
    }

    public static String getDataPath() {
        return getString("data", "../MDVSP-data/");
    }

    public static String getAlgorithm() {
        return getString("algorithm", "simple");
    }

    public static boolean isOutputEnabled() {
        return getBoolean("output", false);
    }

    public static int getPoolSolutions() {
        return getInt("poolSolutions", 1);
    }

    public static int getTimeLimit() {
        return getInt("timeLimit", 5 * 60);
    }

    public static double getClusterFactor() {
        return getDouble("clusterFactor", 0.0);
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
    
    public static String asString() {
        return props.toString();
    }

}
