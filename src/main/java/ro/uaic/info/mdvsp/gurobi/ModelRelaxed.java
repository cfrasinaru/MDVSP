package ro.uaic.info.mdvsp.gurobi;

import gurobi.GRB;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import java.io.IOException;
import ro.uaic.info.mdvsp.Model;

public class ModelRelaxed extends AbstractModel2D {

    public ModelRelaxed(String filename) throws IOException {
        super(filename);
    }

    public ModelRelaxed(Model other) {
        super(other);
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
        //Constraints: in each depot returns at most nbVehicles (nope)
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
    }

}
