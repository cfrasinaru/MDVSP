package ro.uaic.info.mdvsp;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import ro.uaic.info.mdvsp.gurobi.*;

/**
 *
 * @author Cristian Frăsinaru
 */
public class Main {

    public static final String PATH = "../MDVSP-data/";

    private final Map<String, Integer> best = new HashMap<>();

    private PrintWriter solWriter;
    private PrintWriter resWriter;
    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd.MM.yyyy HH:mm");

    /*
    static Map<String, Integer> best = Stream.of(new Object[][]{
        {"m4n500s0", 1_289_114},
    }).collect(Collectors.toMap(data -> (String) data[0], data -> (Integer) data[1]));
     */
    public static void main(String args[]) throws Exception {
        Main app = new Main();
        app.init();
        app.run();
    }

    private void init() {
        try {
            Properties props = new Properties();
            props.load(new FileInputStream("best.properties"));
            for (String key : props.stringPropertyNames()) {
                best.put(key, Integer.parseInt(props.getProperty(key)));
            }
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }

    private void run() {
        int m0 = Config.getDepotsMin();
        int m1 = Config.getDepotsMax();
        int n0 = Config.getTripsMin();
        int n1 = Config.getTripsMax();
        int k0 = Config.getInstanceMin();
        int k1 = Config.getInstanceMax();
        try {
            solWriter = new PrintWriter(new FileWriter(PATH + "/solutions.txt", true));
            resWriter = new PrintWriter(new FileWriter(PATH + "/results.txt", true));
            resWriter.println("Instance \t Best known \t Our value \t Percent \t Running time \t Date");
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
                
        for (int i = m0; i <= m1; i += 4) {
            for (int j = n0; j <= n1; j += 500) {
                for (int k = k0; k <= k1; k++) {
                    run("m" + i + "n" + j + "s" + k);
                }
            }
        }

        solWriter.println();
        solWriter.close();
        
        resWriter.println();
        resWriter.close();        
    }

    private void run(String name) {
        try {
            //Model pb = new Model2D(name);
            //Model pb = new Model3D(name);
            //Model pb = new ModelMinCostFlow(name);
            //Model pb = new ModelRelaxed(name);
            //Model pb = new ModelRepairSimple(name);
            //Model pb = new Model_b_heuristic(name);
            
            Model pb = getModel(name);
            if (pb == null) {
                return;
            }
            pb.setPopulateLimit(Config.getPopulateLimit());
            pb.setOutputEnabled(Config.isOutputEnabled());
            pb.setTimeLimit(Config.getTimeLimit());

            Solution sol = pb.solve();
            if (sol != null) {
                sol.check();
            }
            writeSolution(pb);
            writeResult(pb);
            
        } catch (InvalidSolutionException e) {
            System.err.println(">>>>>>>>>> Bad solution!\n" + e.getMessage());
        }
    }
    
    private Model getModel(String name) {
        try {
            switch(Config.getAlgorithm()) {
                case "simple":
                    return new ModelRepairSimple(name);
                case "bad-tours":
                    return new ModelRepairBadTours(name);
                case "exact":
                    return new Model3D(name);
            }
            throw new IllegalArgumentException("Invalid algorithm: " + name);
        } catch (IOException e) {
            System.err.println("No data file for: " + name + "\n" + e.getMessage());
            return null;
        }
    }

    private void writeSolution(Model pb) {
        Solution sol = pb.getSolution();
        solWriter.println("----------");
        solWriter.println(pb.getName());
        solWriter.println(pb.getClass().getSimpleName());
        solWriter.println(sol.totalCost());
        solWriter.println(SDF.format(new Date()));
        solWriter.println("----------");
        solWriter.println(sol);
        solWriter.println();
        solWriter.flush();
    }

    private void writeResult(Model pb) {
        Solution sol = pb.getSolution();
        int opt = best.get(pb.getName());
        int val = sol.totalCost();
        double pOpt = ((double) val - opt) / opt;

        //name best value percent runningTime(ms) dateTime
        String str = String.format("%.10s \t %,d \t %,d \t %.5f \t %10d \t %s \t %s",
                pb.getName(), opt, val, pOpt, pb.getRunningTime(), SDF.format(new Date()), pb.getClass().getSimpleName());
        resWriter.println(str);
        resWriter.flush();
        
        System.out.println(str);
    }

}
