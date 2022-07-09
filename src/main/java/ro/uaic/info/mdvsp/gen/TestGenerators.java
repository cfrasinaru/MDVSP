/*
 * Copyright (C) 2021 Faculty of Computer Science Iasi, Romania
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ro.uaic.info.mdvsp.gen;

import java.io.IOException;
import ro.uaic.info.mdvsp.Instance;
import ro.uaic.info.mdvsp.Solution;
import ro.uaic.info.mdvsp.gurobi.Model3D;
import ro.uaic.info.mdvsp.gurobi.ModelRelaxed;
import static ro.uaic.info.mdvsp.gen.GeneratorType.*;
import ro.uaic.info.mdvsp.repair.RepairModel;

/**
 *
 * @author Cristian Frăsinaru
 */
public class TestGenerators {

    public static void main(String args[]) throws IOException {
        var app = new TestGenerators();
        //app.testRandom();
        //app.test(UNION);        
        app.test(UNION); 
    }

    private void test(GeneratorType type) throws IOException {
        long t0, t1;
        int ms[] = {2, 4, 8, 16};
        //int ns[] = {100, 200, 500, 1000, 1500, 3000, 5000, 10_000};
        int ns[] = {100, 200, 500, 1000};        
        //int ns[] = {500};
        int ks = 5;
        for (int m : ms) {
            for (int n : ns) {
                for (int k = 0; k < ks; k++) {
                    Generator gen;
                    switch (type) {
                        case RANDOM:
                            gen = new RandomGenerator(m, n);
                            break;
                        case GREEDY:
                            gen = new GreedyGenerator(m, n);
                            break;
                        case UNION:
                            gen = new UnionGenerator(m, n);
                            break;
                        case INSERTION:
                            gen = new InsertionGenerator(m, n);
                            break;
                        default:
                            throw new IllegalArgumentException("Unknown algorithm: " + type);
                    }
                    gen.avgTourSize(5)
                            .tripCosts(0, 1000)
                            .depotCosts(5000, 6000);
                    Instance inst = gen.build();
                    inst.setName(type.name().toLowerCase() + "_" + "m" + m + "n" + n + "s" + k);
                    inst.write("d:/java/MDVSP-Data/union/" + inst.getName() + ".inp");

                    /*
                    t0 = System.currentTimeMillis();
                    var aprox = new RepairModel(new ModelRelaxed(inst));
                    Solution aproxSol = aprox.solve();
                    t1 = System.currentTimeMillis();
                    long aproxTime = (t1 - t0) / 1000;

                    int z0 = inst.getKnownOptimum();
                    int z = aproxSol.totalCost();
                    double error = 100.0 * (z - z0) / z0;

                    //time in seconds
                    System.out.println(inst.getName()
                            + " \t& " + z0
                            + " \t& " + z
                            + " \t& " + String.format("%.2f\\%%", error)
                            + " \t& " + aproxTime
                            + " \\\\"
                    );*/
                }
            }
        }
    }

    private void testExact() throws IOException {
        long t0, t1;
        int ms[] = {4};
        //int ns[] = {100, 200, 500, 1000, 1500, 3000, 5000, 10_000};
        //int ns[] = {100, 200, 500, 1000};        
        int ns[] = {100};
        int ks = 1;
        for (int m : ms) {
            for (int n : ns) {
                for (int k = 1; k <= ks; k++) {
                    var gen = new RandomGenerator(m, n)
                            .avgTourSize(5)
                            .tripCosts(0, 1000)
                            .depotCosts(5000, 6000);
                    Instance inst = gen.build();
                    //inst.write(0);            

                    t0 = System.currentTimeMillis();
                    var exact = new Model3D(inst);
                    Solution exactSol = exact.solve();
                    t1 = System.currentTimeMillis();
                    long exactTime = t1 - t0;

                    t0 = System.currentTimeMillis();
                    var aprox = new RepairModel(new ModelRelaxed(inst));
                    Solution aproxSol = aprox.solve();
                    t1 = System.currentTimeMillis();
                    long aproxTime = t1 - t0;

                    int z0 = exactSol.totalCost();
                    int z = aproxSol.totalCost();
                    double error = 100.0 * (z - z0) / z0;

                    //time in ms
                    System.out.println("m" + m + "n" + n + "s" + k
                            //+ " & " + inst.getKnownOptimum()
                            + " \t& " + z0
                            + " \t& " + z
                            + " \t& " + String.format("%.2f\\%%", error)
                            + " \t& " + exactTime
                            + " \t& " + aproxTime
                            + " \\\\"
                    );
                }
            }
        }
    }

}
