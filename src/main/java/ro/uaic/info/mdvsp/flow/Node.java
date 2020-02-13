package ro.uaic.info.mdvsp.flow;

/**
 *
 * @author Cristian Frăsinaru
 */
public class Node {

    protected int id;
    protected double demand = 0;

    public Node(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    /**
     * @return the demand
     */
    public double getDemand() {
        return demand;
    }

    /**
     * @param demand the demand to set
     */
    public void setDemand(double demand) {
        this.demand = demand;
    }

}
