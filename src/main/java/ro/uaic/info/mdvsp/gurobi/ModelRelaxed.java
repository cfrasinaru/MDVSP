package ro.uaic.info.mdvsp.gurobi;

import gurobi.GRB;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import java.io.IOException;
import ro.uaic.info.mdvsp.Config;
import ro.uaic.info.mdvsp.Model;

public class ModelRelaxed extends AbstractModel2D {

    private int y[]; //y[i] = the closest depot for trip i
    
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

    private void computeNearestDepots() {
        y = new int[n]; //y[i] = the nearest depot for trip i
        for (int i = 0; i < n; i++) {
            int minCost = Integer.MAX_VALUE;
            int minDepot = -1;
            for (int j = 0; j < m; j++) {
                if (cost[i + m][j] > 0 && cost[i + m][j] < minCost) {
                    minDepot = j;
                    minCost = cost[i + m][j];
                }
            }
            y[i] = minDepot;
        }
    }
    
    @Override
    protected void createObjective() throws GRBException {
        double factor = Config.getClusterFactor();
        computeNearestDepots();
        GRBLinExpr expr = new GRBLinExpr();
        for (int i = 0; i < n + m; i++) {
            for (int j = 0; j < n + m; j++) {
                if (x[i][j] != null) {
                    if (i >= m && j < m) {
                        expr.addTerm(cost[i][j] + (cost[i][j] - cost[i][y[i - m]]) * factor, x[i][j]);
                    } else {
                        expr.addTerm(cost[i][j], x[i][j]);
                    }
                }
            }
        }
        model.setObjective(expr, GRB.MINIMIZE);
    }
    
}
