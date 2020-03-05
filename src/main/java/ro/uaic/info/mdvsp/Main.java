package ro.uaic.info.mdvsp;

import ro.uaic.info.mdvsp.repair.RepairModel;
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
import ro.uaic.info.mdvsp.bb.Model_b_heuristic;
import ro.uaic.info.mdvsp.greedy.GreedyModel2;
import ro.uaic.info.mdvsp.gurobi.*;

/**
 *
 * @author Cristian Frăsinaru
 */
public class Main {

    private static final Map<String, Integer> BEST = new HashMap<>();

    private PrintWriter solWriter;
    private PrintWriter resWriter;
    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd.MM.yyyy HH:mm");

    /*
    static Map<String, Integer> BEST = Stream.of(new Object[][]{
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
                BEST.put(key, Integer.parseInt(props.getProperty(key)));
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
            String path = Config.getDataPath();
            solWriter = new PrintWriter(new FileWriter(path + "/solutions.txt", true));
            resWriter = new PrintWriter(new FileWriter(path + "/results.txt", true));
            resWriter.println("----------------------------------------------------------------------");
            resWriter.println("Session: " + new java.util.Date());
            resWriter.println(Config.asString());
            resWriter.println("----------------------------------------------------------------------");
            resWriter.printf("%-15s %-15s %-15s %-15s %-15s %-20s %-20s %n", "Instance", "Best known", "Our value", "Percent", "Running time", "Date", "Algorithm");
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
            //Model pb = new Model_b_heuristic(name);
            //Model pb = new GreedyModel1(name);
            //Model pb = new GreedyModel2(name);
            //Model pb = new IterativeModel(name);
            //Model pb = new RepairModel(new ModelRelaxed(name));
            //Model pb = new RepairModel(new IterativeModel(name));
            //Model pb = new RepairModel(new RandomModel(name));
            //Model pb = new MergeDepotsModel(name);

            Model pb = getModel(name);
            if (pb == null) {
                return;
            }
            pb.setKnownOptimum(BEST.get(name));
            pb.setPoolSolutions(Config.getPoolSolutions());
            pb.setOutputEnabled(Config.isOutputEnabled());
            pb.setTimeLimit(Config.getTimeLimit());

            Solution sol = pb.solve();
            if (sol != null && true) {
                sol.check();
            }
            writeSolution(pb);
            writeResult(pb);

        } catch (InvalidSolutionException e) {
            System.err.println(">>>>>>>>>> Bad solution!\n" + e.getMessage());
        } catch (IOException e) {
            System.err.println("No data file for: " + name + "\n" + e.getMessage());
        }
    }

    private Model getModel(String name) throws IOException {
        switch (Config.getAlgorithm()) {
            case "simple":
                return new RepairModel(new ModelRelaxed(name));
            case "bad-tours":
                return new RepairModel(new Model_b_heuristic(name));
            case "exact":
                return new Model3D(name);
        }
        throw new IllegalArgumentException("Invalid algorithm: " + name);
    }

    private void writeSolution(Model pb) {
        Solution sol = pb.getSolution();
        if (sol == null) {
            return;
        }
        solWriter.println("----------");
        solWriter.println(pb.getName());
        solWriter.println(pb.getClass().getSimpleName());
        solWriter.println(sol.totalCost());
        solWriter.println(SDF.format(new Date()));
        solWriter.println("----------");
        solWriter.println(sol.toursToStringDetailed());
        solWriter.println();
        solWriter.flush();

        //System.out.println("Bad tours: " + sol.getBadTours().size() + "/" + sol.getTours().size());
        //System.out.println(sol.toursToStringDetailed());
        //sol.getBadTours().stream().filter(t -> t.size() <= 5).forEach(System.out::println);
    }

    private void writeResult(Model pb) {
        Solution sol = pb.getSolution();
        if (sol == null) {
            return;
        }
        int opt = BEST.get(pb.getName());
        int val = sol.totalCost();
        double pOpt = ((double) val - opt) / opt;

        //name BEST value percent runningTime(ms) dateTime
        String str = String.format("%-15s %-,15d %-,15d %-15.5f %-15d %-20s %-20s",
                pb.getName(), opt, val, pOpt, pb.getRunningTime(), SDF.format(new Date()), pb.getClass().getSimpleName());
        resWriter.println(str);
        resWriter.flush();

        System.out.println(str);
    }

}
