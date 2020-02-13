package ro.uaic.info.mdvsp.gurobi;

import ro.uaic.info.mdvsp.*;
import gurobi.GRB;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBVar;
import java.io.IOException;

public abstract class AbstractModel2D extends AbstractModel {
    
    protected GRBVar x[][];
    
    public AbstractModel2D(String filename) throws IOException {
        super(filename);
    }
    
    public AbstractModel2D(Model other) {
        super(other);
    }
    
    @Override
    protected void createVariables() throws GRBException {
        // Create variables
        this.x = new GRBVar[n + m][n + m];
        
        for (int i = 0; i < n + m; i++) {
            for (int j = 0; j < n + m; j++) {
                if (cost[i][j] >= 0) {
                    x[i][j] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "x[" + i + "," + j + "]");
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
        for (int k = 0; k < ns; k++) {
            Solution sol = initialSolutions.get(k);
            model.set(GRB.IntParam.StartNumber, k);            
            for (int i = 0; i < n + m; i++) {
                for (int j = 0; j < n + m; j++) {
                    if (x[i][j] != null) {
                        x[i][j].set(GRB.DoubleAttr.Start, sol.get(i, j));
                    }
                }
            }
        }
    }
    
    @Override
    protected abstract void createConstraints() throws GRBException;
    
    @Override
    protected void createObjective() throws GRBException {
        // Set objective: minimize overall cost
        GRBLinExpr expr = new GRBLinExpr();
        for (int i = 0; i < n + m; i++) {
            for (int j = 0; j < n + m; j++) {
                if (x[i][j] != null) {
                    expr.addTerm(cost[i][j], x[i][j]);
                }
            }
        }
        model.setObjective(expr, GRB.MINIMIZE);
    }
    
    @Override
    protected void extractSolution() {
        try {
            Solution sol = new Solution(this);
            for (int i = 0; i < n + m; i++) {
                for (int j = 0; j < n + m; j++) {
                    if (x[i][j] != null) {
                        sol.set(i, j, x[i][j].get(GRB.DoubleAttr.X) < tolerance ? 0 : 1);
                    } else {
                        sol.set(i, j, 0);
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
        try {
            int solCount = model.get(GRB.IntAttr.SolCount);
            for (int k = 0; k < solCount; k++) {
                model.set(GRB.IntParam.SolutionNumber, k);
                Solution sol = new Solution(this);
                for (int i = 0; i < n + m; i++) {
                    for (int j = 0; j < n + m; j++) {
                        if (x[i][j] != null) {
                            sol.set(i, j, x[i][j].get(GRB.DoubleAttr.Xn) < tolerance ? 0 : 1);
                        } else {
                            sol.set(i, j, 0);
                        }
                    }
                }
                solutions.add(sol);
            }
        } catch (GRBException e) {
            System.err.println(e);
        }
    }
    
}
