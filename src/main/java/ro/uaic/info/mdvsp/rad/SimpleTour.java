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

import java.time.LocalTime;
import ro.uaic.info.mdvsp.Tour;

/**
 * depot - trips - depot.
 *
 * @author Cristian Frăsinaru
 */
public class SimpleTour implements Comparable<SimpleTour> {

    private final Timetable timetable;
    private final Tour tour;
    private int depot;
    private LocalTime startTime;
    private LocalTime endTime;

    public SimpleTour(Timetable timetable, Tour tour) {
        this.timetable = timetable;
        this.tour = tour;
        this.depot = timetable.depots.get(tour.get(0));
        int k = tour.size();
        int j = tour.get(k - 2);
        this.startTime = trip(tour.get(1)).getStartTime().minusMinutes(timetable.dur[tour.get(0)][tour.get(1)]);
        this.endTime = trip(tour.get(k - 2)).getEndTime().plusMinutes(timetable.dur[tour.get(k - 2)][tour.get(k - 1)]);
        if (endTime.getHour() == 0) {
            endTime = LocalTime.of(23, 59, 59);
        }
    }

    //i is an index insider the tour (at 0 and size-1 are depots)
    private Trip trip(int i) {
        int m = timetable.depots.size();
        return timetable.trips.get(i - m);
    }

    /**
     * 
     * @param other
     * @return 
     */
    public boolean intersects(SimpleTour other) {
        LocalTime start0 = this.getStartTime();
        LocalTime end0 = this.getEndTime();
        LocalTime start1 = other.getStartTime();
        LocalTime end1 = other.getEndTime();
        //if start0 between start1 and end1
        if (start0.compareTo(start1) >= 0 && start0.compareTo(end1) < 0) {
            return true;
        }
        //if start1 between start0 and end0
        if (start1.compareTo(start0) >= 0 && start1.compareTo(end0) < 0) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        int m = timetable.depots.size();
        int k = tour.size();
        int[][] dur = timetable.dur;
        int t0, t1;
        sb.append("[").append(startTime).append(" - ").append(endTime).append("]: ");
        sb.append("Depot_").append(depot);
        for (int i = 1; i < k - 1; i++) {
            t0 = tour.get(i - 1);
            t1 = tour.get(i);
            Trip trip0 = i > 1 ? trip(t0) : null;
            Trip trip1 = trip(t1);
            if (trip0 == null) {
                sb.append(" --{").append(dur[t0][t1]).append(" min}--> ").append(trip1);
            } else {
                if (trip0.getEndLoc() == trip1.getStartLoc()) {
                    sb.append(" --{").append(dur[t0][t1]).append(" min}--> ").append(trip1);
                } else {
                    sb.append(" --**{").append(dur[t0][t1]).append(" min}**--> ").append(trip1);
                }
            }
        }
        t0 = tour.get(k - 2);
        t1 = tour.get(k - 1);
        sb.append(" --{").append(dur[t0][t1]).append(" min}--> Depot_").append(depot);
        return sb.toString();
    }

    public int getDepot() {
        return depot;
    }

    public void setDepot(int depot) {
        this.depot = depot;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public int getMainRoute() {
        Trip first = trip(tour.get(1));
        return first.getRoute();
    }

    @Override
    public int compareTo(SimpleTour o) {
        /*
        int ret = this.getMainRoute() - o.getMainRoute();
        if (ret != 0) {
            return ret;
        }*/
        return this.startTime.compareTo(o.startTime);
    }

}
