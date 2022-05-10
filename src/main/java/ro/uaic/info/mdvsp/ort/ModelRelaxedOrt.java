package ro.uaic.info.mdvsp.ort;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;
import java.io.IOException;
import ro.uaic.info.mdvsp.Instance;
import ro.uaic.info.mdvsp.Model;
import ro.uaic.info.mdvsp.Solution;

public class ModelRelaxedOrt extends Model {

    private static final double INFINITY = java.lang.Double.POSITIVE_INFINITY;
    private MPSolver solver;
    private MPObjective objective;
    private MPVariable x[][];

    public ModelRelaxedOrt(String filename) throws IOException {
        super(filename);
    }

    public ModelRelaxedOrt(Model other) {
        super(other);
    }

    public ModelRelaxedOrt(Instance instance) {
        super(instance);
    }

    @Override
    protected void _solve() {
        start();
        createVariables();
        createConstraints();
        createObjective();
        optimize();
    }

    protected void start() {
        Loader.loadNativeLibraries();
        this.solver = MPSolver.createSolver("GLOP");
        //solver.setNumThreads(8);
    }

    protected void createVariables() {
        // Create variables
        this.x = new MPVariable[n + m][n + m];

        for (int i = 0; i < n + m; i++) {
            for (int j = 0; j < n + m; j++) {
                if (cost[i][j] >= 0) {
                    x[i][j] = solver.makeIntVar(0, 1, "x[" + i + "," + j + "]");
                    //x[i][j] = solver.makeNumVar(0, 1, "x[" + i + "," + j + "]");
                }
            }
        }
    }

    protected void createConstraints() {

        //Constraints: in each trip enters one vehicle, exits one vehicle
        for (int i = m; i < n + m; i++) {
            MPConstraint rowSum = solver.makeConstraint(1, 1, "tripRowSum" + i);
            MPConstraint colSum = solver.makeConstraint(1, 1, "tripColSum" + i);
            for (int j = 0; j < n + m; j++) {
                if (x[i][j] != null) {
                    rowSum.setCoefficient(x[i][j], 1);
                }
                if (x[j][i] != null) {
                    colSum.setCoefficient(x[j][i], 1);
                }
            }
        }

        //Constraints: from each depots goes at most nbVehicles
        for (int i = 0; i < m; i++) {
            MPConstraint rowSum = solver.makeConstraint(-INFINITY, nbVehicles[i], "depotVehicles" + i);
            for (int j = 0; j < n + m; j++) {
                if (x[i][j] != null) {
                    rowSum.setCoefficient(x[i][j], 1);
                }
            }
        }

        //Constraints: depots exits = depot entries
        for (int i = 0; i < m; i++) {
            MPConstraint depotSum = solver.makeConstraint(0, 0, "depotSum" + i);
            for (int j = 0; j < n + m; j++) {
                if (x[i][j] != null) {
                    depotSum.setCoefficient(x[i][j], 1);
                }
                if (x[j][i] != null) {
                    depotSum.setCoefficient(x[j][i], -1);
                }
            }
        }

    }

    protected void createObjective() {
        this.objective = solver.objective();
        for (int i = 0; i < n + m; i++) {
            for (int j = 0; j < n + m; j++) {
                if (x[i][j] != null) {
                    objective.setCoefficient(x[i][j], cost[i][j]);
                }
            }
        }
        objective.setMinimization();
    }

    protected void optimize() {
        MPSolver.ResultStatus resultStatus = solver.solve();

        // [START print_solution]
        if (resultStatus == MPSolver.ResultStatus.OPTIMAL) {
            System.out.println("Solution:");
            System.out.println("Objective value = " + objective.value());
            extractSolution();
        } else {
            System.err.println("The problem does not have an optimal solution!");
        }

        System.out.println("Problem solved in " + solver.wallTime() + " milliseconds");
        System.out.println("Problem solved in " + solver.iterations() + " iterations");
    }

    protected Solution extractSolution() {
        Solution sol = new Solution(this);
        for (int i = 0; i < n + m; i++) {
            for (int j = 0; j < n + m; j++) {
                if (x[i][j] != null) {
                    sol.set(i, j, x[i][j].solutionValue() < tolerance ? 0 : 1);
                } else {
                    sol.set(i, j, 0);
                }
            }
        }
        solutions.add(sol);
        return sol;
    }

}
