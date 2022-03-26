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

import java.time.Duration;
import java.time.LocalTime;

/**
 *
 * @author Cristian Frăsinaru
 */
public class Trip {

    private int route;
    private LocalTime startTime;
    private int startLoc;
    private LocalTime endTime;
    private int endLoc;
    private int index;

    public int getRoute() {
        return route;
    }

    public void setRoute(int route) {
        this.route = route;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public int getStartLoc() {
        return startLoc;
    }

    public void setStartLoc(int startLoc) {
        this.startLoc = startLoc;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public int getEndLoc() {
        return endLoc;
    }

    public void setEndLoc(int endLoc) {
        this.endLoc = endLoc;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getDuration() {
        return (int) Duration.between(startTime, endTime).toMinutes();
    }

    @Override
    public String toString() {
        return "(" + route + "," + startTime + "," + startLoc + "," + endTime + "," + endLoc + ")";
    }

}
