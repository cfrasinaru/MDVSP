/*
 * Copyright (C) 2022 Faculty of Computer Science Iasi, Romania
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
package ro.uaic.info.mdvsp.rad;

import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

/**
 *
 * @author Cristian Frăsinaru
 */
public class ColoringProblem {

    private final Timetable timetable;
    private final int n; //nb of vertices
    private final int k; //nb of colors
    private final Graph<Integer, DefaultEdge> graph;
    //
    private GRBVar x[][];
    private GRBEnv env;
    private GRBModel model;

    public ColoringProblem(Timetable timetable, int k) {
        this.timetable = timetable;
        this.n = timetable.tours.size();
        this.k = k;
        this.graph = new SimpleGraph<>(DefaultEdge.class);
        for (int i = 0; i < n; i++) {
            graph.addVertex(i);
        }
        for (int i = 0; i < n - 1; i++) {
            var t0 = timetable.tours.get(i);
            for (int j = i + 1; j < n; j++) {
                var t1 = timetable.tours.get(j);
                if (t0.intersects(t1)) {
                    graph.addEdge(i, j);
                    //System.out.println("Bad");
                    //System.out.println("\t" + t0);
                    //System.out.println("\t" + t1);
                }
            }
        }
    }

    public List<MultiTour> solve() {
        try {
            env = new GRBEnv(true);
            env.set(GRB.IntParam.OutputFlag, 0);
            env.start();

            model = new GRBModel(env);
            model.set(GRB.DoubleParam.MIPGapAbs, 0);
            model.set(GRB.DoubleParam.MIPGap, 0);
            //model.set(GRB.DoubleParam.TimeLimit, timeLimit);
            model.set(GRB.IntParam.Method, GRB.METHOD_AUTO);

            createModel();

            // Optimize model
            model.optimize();

            List<MultiTour> schedule = null;
            if (model.get(GRB.IntAttr.Status) == GRB.Status.OPTIMAL) {
                // Get the solution
                System.out.println("Coloring ok!");
                schedule = createSchedule();
            } else if (model.get(GRB.IntAttr.Status) == GRB.Status.TIME_LIMIT) {
                System.out.println("...time limit");
            }

            model.dispose();
            env.dispose();
            return schedule;
        } catch (GRBException ex) {
            System.out.println(ex);
            return null;
        }
    }

    private void createModel() throws GRBException {
        x = new GRBVar[n][k];
        for (int u = 0; u < n; u++) {
            for (int j = 0; j < k; j++) {
                x[u][j] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "x[" + u + ", " + j + "]");
            }
        }

        //each node must have a color
        for (int u = 0; u < n; u++) {
            GRBLinExpr sum = new GRBLinExpr();
            for (int j = 0; j < k; j++) {
                sum.addTerm(1, x[u][j]);
            }
            model.addConstr(sum, GRB.EQUAL, 1, "color_" + u);
        }

        //two adjacent nodes cannot have the same color
        for (DefaultEdge e : graph.edgeSet()) {
            int u = graph.getEdgeSource(e);
            int v = graph.getEdgeTarget(e);
            for (int j = 0; j < k; j++) {
                GRBLinExpr sum = new GRBLinExpr();
                sum.addTerm(1, x[u][j]);
                sum.addTerm(1, x[v][j]);
                model.addConstr(sum, GRB.LESS_EQUAL, 1, "diffcolor_" + u + "," + v + " - " + j);
            }
        }
    }

    private List<MultiTour> createSchedule() {
        try {
            List<MultiTour> schedule = new ArrayList<>();
            for (int j = 0; j < k; j++) {
                //j=color=vehicle
                var mt = new MultiTour(j + 1);
                schedule.add(mt);
                for (int i = 0; i < n; i++) {
                    if (x[i][j].get(GRB.DoubleAttr.X) > .00001) {
                        //tour i is going to vehicle j
                        mt.add(timetable.tours.get(i));
                    }
                }
            }
            return schedule;
        } catch (GRBException ex) {
            Logger.getLogger(ColoringProblem.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
