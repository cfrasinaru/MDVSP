/*
 * Copyright (C) 2019 Cristian Frasinaru
 */
package ro.uaic.info.mdvsp;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Cristian Frăsinaru
 */
public class ProblemIO {

//    public static Problem read(String filename) throws IOException {
//        Path path = Paths.get(filename);
//        List<String> lines = Files.readAllLines(path);
//        String firstLine[] = lines.get(0).split("\t");
//        int m = Integer.parseInt(firstLine[0]);
//        int n = Integer.parseInt(firstLine[1]);
//        Problem pb = new Problem(path.toFile().getName(), m, n);
//        for (int j = 0; j < m; j++) {
//            pb.nbVehicles[j] = Integer.parseInt(firstLine[j + 2]);
//        }
//        for (int i = 0; i < n + m; i++) {
//            String line[] = lines.get(i + 1).split("\t");
//            for (int j = 0; j < n + m; j++) {
//                pb.cost[i][j] = Integer.parseInt(line[j]);
//                /*
//                if (pb.cost[i][j] == -1) {
//                    pb.cost[i][j] = Integer.MAX_VALUE;
//                }*/
//            }
//        }
//        return pb;
//    }

    
    /**
     *
     * @param filename
     * @return
     * @throws IOException
     */
    public static void transform(String filename) throws IOException {

        Path path = Paths.get(filename);
        List<String> lines = Files.readAllLines(path);
        String sep = " ";
        String firstLine[] = lines.get(0).split(sep);
        int m = Integer.parseInt(firstLine[0]); //depots
        int n = Integer.parseInt(firstLine[1]); //trips
        int l = Integer.parseInt(firstLine[2]); //locations

        int nbVehicles[] = new int[m];
        int startTime[] = new int[n];
        int startLocation[] = new int[n];
        int endTime[] = new int[n];
        int endLocation[] = new int[n];
        int travelTime[][] = new int[l][l];

        String secondLine[] = lines.get(1).split(sep);
        for (int j = 0; j < m; j++) {
            nbVehicles[j] = Integer.parseInt(secondLine[j]);
        }

        //trip informations
        for (int i = 0; i < n; i++) {
            String line[] = lines.get(i + 2).split(sep);
            startLocation[i] = Integer.parseInt(line[0]);
            startTime[i] = Integer.parseInt(line[1]);
            endLocation[i] = Integer.parseInt(line[2]);
            endTime[i] = Integer.parseInt(line[3]);
        }

        //matrix travel
        for (int i = 0; i < l; i++) {
            String line[] = lines.get(i + n + 2).split(sep);
            for (int j = 0; j < l; j++) {
                travelTime[i][j] = Integer.parseInt(line[j]);
            }
        }

        int pos = filename.length() - 5;
        int q = Integer.parseInt(filename.substring(pos, pos + 1));

        sep = "\t";
        PrintWriter out = new PrintWriter(new FileWriter("input2/inventory/m" + m + "n" + n + "s" + (q - 1) + ".inp"));
        StringBuilder line = new StringBuilder();
        line.append(m).append(sep).append(n);
        for (int i = 0; i < m; i++) {
            line.append(sep).append(nbVehicles[i]);
        }
        out.println(line);

        int cost = -1;
        for (int i = 0; i < m + n; i++) {
            line = new StringBuilder();
            for (int j = 0; j < m + n; j++) {
                if (i < m && j < m) {
                    //depot - depot
                    cost = -1;
                } else if (i < m && j >= m) {
                    //pull out: depot i -> trip j
                    cost = 5000 + 10 * travelTime[i][startLocation[j - m]];
                } else if (i >= m && j < m) {
                    //pull in: trip i -> depot j
                    cost = 5000 + 10 * travelTime[endLocation[i - m]][j];
                } else {
                    //trip i -trip j 
                    if (startTime[j - m] < endTime[i - m] + travelTime[endLocation[i - m]][startLocation[j - m]]) {
                        cost = -1;
                    } else {
                        cost = 8 * travelTime[endLocation[i - m]][startLocation[j - m]] + 2 * (startTime[j - m] - endTime[i - m]);
                    }
                }
                if (line.length() > 0) {
                    line.append(sep);
                }
                line.append(cost);
            }
            out.println(line);
        }

        out.close();
    }

    public static void main(String args[]) {
        int m0 = 8;
        int m1 = 16; //+4
        int n0 = 1500;
        int n1 = 3000; //+500
        int q0 = 1;
        int q1 = 5;
        try {
            for (int m = m0; m <= m1; m += 4) {
                for (int n = n0; n <= n1; n += 500) {
                    for (int q = q0; q <= q1; q++) {
                        transform("d:/java/MDVSP/input2/RN-" + m + "-" + n + "-0" + q + ".dat");
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(ProblemIO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
