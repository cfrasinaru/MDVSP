/*
 * Copyright (C) 2019 Cristian Frasinaru
 */
package ro.uaic.info.mdvsp.gurobi;

import gurobi.GRB;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBVar;
import java.io.IOException;

/**
 * 3D.
 *
 * @author Cristian Frăsinaru
 */
public class Model2D extends AbstractModel2D {

    private GRBVar y[][];

    public Model2D(String filename) throws IOException {
        super(filename);
    }

    @Override
    protected void createVariables() throws GRBException {
        super.createVariables();

        this.y = new GRBVar[n][m];
        for (int i = 0; i < n; i++) {
            for (int k = 0; k < m; k++) {
                y[i][k] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "y[" + i + "," + k + "]");
            }
        }
    }

    @Override
    protected void createConstraints() throws GRBException {
        //Constraints: in each trip enters one vehicle, exits one vehicle
        for (int i = m; i < n + m; i++) {
            GRBLinExpr rowSum = new GRBLinExpr();
            GRBLinExpr colSum = new GRBLinExpr();
            for (int j = 0; j < n + m; j++) {
                if (x[i][j] != null) {
                    rowSum.addTerm(1, x[i][j]);
                }
                if (x[j][i] != null) {
                    colSum.addTerm(1, x[j][i]);
                }
            }
            model.addConstr(rowSum, GRB.EQUAL, 1, "tripRowSum" + i);
            model.addConstr(colSum, GRB.EQUAL, 1, "tripColSum" + i);
        }

        //Constraints: from each depots goes at most nbVehicles
        //Constraints: in each depot returns as many vehicles as departed
        for (int i = 0; i < m; i++) {
            GRBLinExpr rowSum = new GRBLinExpr();
            GRBLinExpr colSum = new GRBLinExpr();
            for (int j = 0; j < n + m; j++) {
                if (x[i][j] != null) {
                    rowSum.addTerm(1, x[i][j]);
                }
                if (x[j][i] != null) {
                    colSum.addTerm(1, x[j][i]);
                }
            }
            model.addConstr(rowSum, GRB.LESS_EQUAL, nbVehicles[i], "depotRowSum" + i);
            model.addConstr(colSum, GRB.EQUAL, rowSum, "depotColSum" + i);
        }

        //y constraints
        for (int i = 0; i < n; i++) {
            for (int k = 0; k < m; k++) {
                model.addConstr(x[k][m + i], GRB.LESS_EQUAL, y[i][k], "depotStartLink" + i + "_" + k);
                model.addConstr(x[m + i][k], GRB.LESS_EQUAL, y[i][k], "depotEndink" + i + "_" + k);
            }
        }

        //y constraints
        for (int i = 0; i < n; i++) {
            GRBLinExpr sum = new GRBLinExpr();
            for (int k = 0; k < m; k++) {
                sum.addTerm(1, y[i][k]);
            }
            model.addConstr(sum, GRB.EQUAL, 1, "uniqueDepotFor" + i);
        }

        //y constraints
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (x[m + i][m + j] == null) {
                    continue;
                }
                for (int k = 0; k < m; k++) {
                    GRBLinExpr sum = new GRBLinExpr();
                    sum.addTerm(1, y[i][k]);
                    sum.addTerm(1, x[m + i][m + j]);
                    sum.addTerm(-1, y[j][k]);
                    model.addConstr(sum, GRB.LESS_EQUAL, 1, "sync" + i + "_" + j + "_" + k);
                }
            }
        }
    }

}
