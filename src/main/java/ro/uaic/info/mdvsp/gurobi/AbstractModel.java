package ro.uaic.info.mdvsp.gurobi;

import ro.uaic.info.mdvsp.*;
import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBModel;
import java.io.IOException;

/**
 * Abstract Gurobi model.
 *
 * @author Cristian Frăsinaru
 */
public abstract class AbstractModel extends Model {

    protected GRBEnv env;
    protected GRBModel model;

    public AbstractModel(String filename) throws IOException {
        super(filename);
    }

    public AbstractModel(Model other) {
        super(other);
    }

    protected void start() throws GRBException {
        // Create empty environment, set options, and start
        env = new GRBEnv(true);
        env.set(GRB.StringParam.LogFile, "mdvsp.log");
        env.set(GRB.IntParam.OutputFlag, outputEnabled ? 1 : 0);
        env.start();

        this.model = new GRBModel(env);

        //integrality
        model.set(GRB.DoubleParam.MIPGapAbs, 0);
        model.set(GRB.DoubleParam.MIPGap, 0);

        //multiple solutions
        if (poolSolutions > 1) {
            model.set(GRB.DoubleParam.PoolGap, Config.getDouble("poolGap", 0.1));
            model.set(GRB.IntParam.PoolSolutions, poolSolutions);
            model.set(GRB.IntParam.PoolSearchMode, Config.getInt("poolSerchMode", 1)); //2 is better but slooow
        }

        //time
        if (timeLimit > 0) {
            model.set(GRB.DoubleParam.TimeLimit, timeLimit);
        }
    }

    protected void dispose() throws GRBException {
        // Dispose of model and environment
        model.dispose();
        env.dispose();
    }

    protected abstract void createVariables() throws GRBException;

    protected abstract void createConstraints() throws GRBException;

    protected abstract void createObjective() throws GRBException;

    protected void initVariables() throws GRBException {
    }

    /**
     *
     * @throws GRBException
     */
    protected void optimize() throws GRBException {

        //set the initial solutions, if any
        initVariables();

        // Optimize model
        model.optimize();

        // Get the solution
        if (outputEnabled) {
            System.out.println("Objective Value: " + model.get(GRB.DoubleAttr.ObjVal));
        }

        if (poolSolutions == 1) {
            extractSolution();
        } else {
            int ns = model.get(GRB.IntAttr.SolCount);
            if (outputEnabled) {
                System.out.println("Solutions found: " + ns);
            }
            extractSolutions();
        }
    }

    @Override
    protected void _solve() throws GRBException {
        start();
        createVariables();
        createConstraints();
        createObjective();
        optimize();
    }

    protected abstract Solution extractSolution();

    protected abstract void extractSolutions();

}
