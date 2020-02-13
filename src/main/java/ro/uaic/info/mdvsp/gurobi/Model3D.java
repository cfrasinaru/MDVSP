/*
 * Copyright (C) 2019 Cristian Frasinaru
 */
package ro.uaic.info.mdvsp.gurobi;

import gurobi.GRB;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBVar;
import java.io.IOException;
import ro.uaic.info.mdvsp.Model;
import ro.uaic.info.mdvsp.Solution;

/**
 * 3D.
 *
 * @author Cristian Frăsinaru
 */
public class Model3D extends AbstractModel {

    GRBVar x[][][];

    public Model3D(String filename) throws IOException {
        super(filename);
    }

    public Model3D(Model other) {
        super(other);
    }

    @Override
    protected void createVariables() throws GRBException {
        // Create variables
        this.x = new GRBVar[m][n + m][n + m];

        //create variables
        for (int k = 0; k < m; k++) {
            for (int i = 0; i < n + m; i++) {
                for (int j = 0; j < n + m; j++) {
                    if (cost[i][j] >= 0 && (i >= m || i == k) && (j >= m || j == k)) {
                        x[k][i][j] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "x[" + k + "," + i + "," + j + "]");
                    }
                }
            }
        }
    }

    @Override
    protected void initVariables() throws GRBException {
        int ns = initialSolutions.size();
        if (ns == 0) {
            return;
        }        
        model.set(GRB.IntAttr.NumStart, ns);
        for (int q = 0; q < ns; q++) {
            Solution sol = initialSolutions.get(q);
            model.set(GRB.IntParam.StartNumber, q);
            for (int i = 0; i < n + m; i++) {
                for (int j = 0; j < n + m; j++) {
                    int k = sol.getDepot(i);
                    if (x[k][i][j] != null) {
                        x[k][i][j].set(GRB.DoubleAttr.Start, sol.get(i, j));
                    }
                }
            }
        }
    }

    @Override
    protected void createConstraints() throws GRBException {
        //(2) each task is executed exactly once by a vehicle
        for (int i = m; i < n + m; i++) {
            GRBLinExpr sum = new GRBLinExpr();
            for (int j = 0; j < n + m; j++) {
                for (int k = 0; k < m; k++) {
                    if (x[k][i][j] != null) {
                        sum.addTerm(1, x[k][i][j]);
                    }
                }
            }
            model.addConstr(sum, GRB.EQUAL, 1, "eachTaskOnce" + i);
        }

        //(3) limit the number of vehicles that can be used from each depot
        for (int k = 0; k < m; k++) {
            GRBLinExpr sum = new GRBLinExpr();
            for (int j = m; j < n + m; j++) {
                if (x[k][k][j] != null) {
                    sum.addTerm(1, x[k][k][j]);
                }
            }
            model.addConstr(sum, GRB.LESS_EQUAL, nbVehicles[k], "depotVehicleLimit" + k);
        }

        //(4) flow conservation constraints which define a multiple-path structure for each depot
        for (int i = 0; i < n + m; i++) {
            for (int k = 0; k < m; k++) {
                GRBLinExpr sum1 = new GRBLinExpr();
                GRBLinExpr sum2 = new GRBLinExpr();
                for (int j = 0; j < n + m; j++) {
                    if (x[k][i][j] != null) {
                        sum1.addTerm(1, x[k][i][j]);
                    }
                    if (x[k][j][i] != null) {
                        sum2.addTerm(1, x[k][j][i]);
                    }
                }
                model.addConstr(sum1, GRB.EQUAL, sum2, "flowConservation" + k + "_" + i);
            }
        }
    }

    @Override
    protected void createObjective() throws GRBException {
        // Set objective: minimize overall cost
        GRBLinExpr expr = new GRBLinExpr();
        for (int i = 0; i < n + m; i++) {
            for (int j = 0; j < n + m; j++) {
                for (int k = 0; k < m; k++) {
                    if (x[k][i][j] != null) {
                        expr.addTerm(cost[i][j], x[k][i][j]);
                    }
                }
            }
        }
        model.setObjective(expr, GRB.MINIMIZE);
    }

    @Override
    protected void extractSolution() {
        Solution sol = new Solution(this);
        try {
            for (int k = 0; k < m; k++) {
                for (int i = 0; i < n + m; i++) {
                    for (int j = 0; j < n + m; j++) {
                        if (x[k][i][j] != null && x[k][i][j].get(GRB.DoubleAttr.X) > tolerance) {
                            sol.set(i, j, 1);
                        }
                    }
                }
            }
            solutions.add(sol);
        } catch (GRBException e) {
            System.err.println(e);
        }
    }

    @Override
    protected void extractSolutions() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
