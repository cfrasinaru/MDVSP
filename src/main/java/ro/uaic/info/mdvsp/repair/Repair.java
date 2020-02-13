package ro.uaic.info.mdvsp.repair;

import java.util.List;
import ro.uaic.info.mdvsp.Tour;

/**
 *
 * @author Cristian Frăsinaru
 */
public interface Repair {

    boolean isValid();

    int getValue();

    List<Tour> getRepairedTours();
}
