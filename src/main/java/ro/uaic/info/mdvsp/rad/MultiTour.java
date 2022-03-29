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
import java.util.ArrayList;

/**
 *
 * @author Cristian Frăsinaru
 */
public class MultiTour extends ArrayList<SimpleTour> {

    private final int vehicle;
    private LocalTime startTime;
    private LocalTime endTime;

    public MultiTour(int vehicle) {
        super();
        this.vehicle = vehicle;        
    }

    
    @Override
    public boolean add(SimpleTour e) {
        if (!super.add(e)) {
            return false;
        }
        if (endTime != null) {
            if (e.getStartTime().isBefore(endTime)) {
                throw new IllegalArgumentException("Invalid simple tour: " + e);
            }
        }
        if (startTime == null) {
            startTime = e.getStartTime();
        }
        endTime = e.getEndTime();
        return true;
    }

    public int getVehicle() {
        return vehicle;
    }
    
    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Vehicle #").append(vehicle);
        sb.append(" [").append(startTime).append("-").append(endTime).append("]").append("\n");
        for (SimpleTour t : this) {
            sb.append("\t").append(t).append("\n");
        }
        return sb.toString();
    }

}
