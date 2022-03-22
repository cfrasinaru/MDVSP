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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import ro.uaic.info.mdvsp.Instance;
import ro.uaic.info.mdvsp.Model;
import ro.uaic.info.mdvsp.Solution;
import ro.uaic.info.mdvsp.Tour;
import ro.uaic.info.mdvsp.gurobi.ModelRelaxed;
import ro.uaic.info.mdvsp.repair.RepairModel;

/**
 *
 * @author Cristian Frăsinaru
 */
public class Timetable {

    private List<Trip> trips;
    private List<Movement> movements;
    private List<PullOut> pullOuts;
    private List<PullIn> pullIns;
    private Set<Integer> routes;
    private Set<Integer> locations;

    private final String directory;
    //
    private List<Integer> locIndex;
    private List<Integer> depotIndex = new ArrayList<>();

    public Timetable(String directory) {
        this.directory = directory;
    }

    public void loadTrips(String filename) throws IOException {
        trips = new ArrayList<>();
        routes = new HashSet<>();
        locations = new HashSet<>();
        List<String> lines = Files.readAllLines(Paths.get(directory + filename));
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }
            List<String> tokens = CSVUtils.parseLine(line);
            Trip t = new Trip();
            t.setRoute(Integer.parseInt(tokens.get(0)));
            t.setStartTime(LocalTime.parse(tokens.get(1)));
            t.setStartLoc(Integer.parseInt(tokens.get(2)));
            t.setEndTime(LocalTime.parse(tokens.get(3)));
            t.setEndLoc(Integer.parseInt(tokens.get(4)));
            trips.add(t);
            routes.add(t.getRoute());
            locations.add(t.getStartLoc());
            locations.add(t.getEndLoc());
        }
    }

    public void loadMovements(String filename) throws IOException {
        movements = new ArrayList<>();
        List<String> lines = Files.readAllLines(Paths.get(directory + filename));
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }
            List<String> tokens = CSVUtils.parseLine(line);
            Movement m = new Movement();
            m.setStartLoc(Integer.parseInt(tokens.get(0)));
            m.setEndLoc(Integer.parseInt(tokens.get(1)));
            m.setDuration(Integer.parseInt(tokens.get(2)));
            movements.add(m);
        }
    }

    public void loadPullOuts(String filename) throws IOException {
        pullOuts = new ArrayList<>();
        List<String> lines = Files.readAllLines(Paths.get(directory + filename));
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }
            List<String> tokens = CSVUtils.parseLine(line);
            PullOut p = new PullOut();
            p.setDepot(Integer.parseInt(tokens.get(0)));
            p.setStartLoc(Integer.parseInt(tokens.get(1)));
            p.setDuration(Integer.parseInt(tokens.get(2)));
            pullOuts.add(p);
        }
    }

    public void loadPullIns(String filename) throws IOException {
        pullIns = new ArrayList<>();
        List<String> lines = Files.readAllLines(Paths.get(directory + filename));
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }
            List<String> tokens = CSVUtils.parseLine(line);
            PullIn p = new PullIn();
            p.setEndLoc(Integer.parseInt(tokens.get(0)));
            p.setDepot(Integer.parseInt(tokens.get(1)));
            p.setDuration(Integer.parseInt(tokens.get(2)));
            pullIns.add(p);
        }
    }

    public void solve() throws IOException, InvalidDataException {
        StringBuilder errors = new StringBuilder();
        //find out locations
        locIndex = new ArrayList<>();
        int loc = 0;
        Map<Integer, Integer> locMap = new TreeMap<>();
        for (Trip t : trips) {
            if (t.getStartTime().isAfter(t.getEndTime())) {
                errors.append("Invalid trip times: ").append(t).append("\n");
            }
            if (!locMap.containsKey(t.getStartLoc())) {
                locIndex.add(t.getStartLoc());
                locMap.put(t.getStartLoc(), loc);
                loc++;
            }
            if (!locMap.containsKey(t.getEndLoc())) {
                locIndex.add(t.getEndLoc());
                locMap.put(t.getEndLoc(), loc);
                loc++;
            }
        }

        //find out depots
        int dep = 0;
        depotIndex = new ArrayList<>();
        Map<Integer, Integer> depotMap = new TreeMap<>();
        for (PullOut p : pullOuts) {
            if (!locMap.containsKey(p.getStartLoc())) {
                errors.append("\n").append("Invalid pull-out start location: ").append(p.getStartLoc());
            }
            if (!depotMap.containsKey(p.getDepot())) {
                depotMap.put(p.getDepot(), dep);
                depotIndex.add(p.getDepot());
                dep++;
            }
        }
        for (PullIn p : pullIns) {
            if (!locMap.containsKey(p.getEndLoc())) {
                errors.append("\n").append("Invalid pull-in end location: ").append(p.getEndLoc());
                continue;
            }
            if (!depotMap.containsKey(p.getDepot())) {
                depotMap.put(p.getDepot(), dep);
                depotIndex.add(p.getDepot());
                dep++;
            }
        }

        //check movements
        for (Movement move : movements) {
            if (!locMap.containsKey(move.getStartLoc())) {
                errors.append("\n").append("Invalid movement start location: ").append(move.getEndLoc());
            }
            if (!locMap.containsKey(move.getEndLoc())) {
                errors.append("\n").append("Invalid movement end location: ").append(move.getEndLoc());
            }
        }
        if (errors.length() > 0) {
            //throw new InvalidDataException(errors.toString());
            System.out.println(errors);
        }

        System.out.println("Depots: " + dep);
        System.out.println("Locations: " + loc);
        System.out.println("Trips: " + trips.size());
        System.out.println("Routes: " + routes.size());
        for (int l : locations) {
            System.out.println("Location " + l);
            System.out.println("\tOut:" + pullOuts.stream().filter(p -> p.getStartLoc() == l).collect(Collectors.toList()));
            System.out.println("\tIn:" + pullIns.stream().filter(p -> p.getEndLoc() == l).collect(Collectors.toList()));
        }

        int m = dep;
        int n = trips.size();
        int[] nbVehicles = new int[m];
        for (int i = 0; i < m; i++) {
            nbVehicles[i] = n;
        }
        int[][] cost = new int[n + m][n + m];
        for (int i = 0; i < m + n; i++) {
            for (int j = 0; j < m + n; j++) {
                cost[i][j] = -1;
            }
        }

        //pull out costs
        for (int i = 0; i < m; i++) {
            int depot = depotIndex.get(i);
            for (int j = m; j < m + n; j++) {
                Trip trip = trips.get(j - m);
                PullOut po = pullOuts.stream()
                        .filter(p -> p.getDepot() == depot && p.getStartLoc() == trip.getStartLoc())
                        .findAny().orElse(null);
                cost[i][j] = (po == null ? -1 : 5000 + po.getDuration());
            }
        }
        //pull in costs
        for (int j = m; j < m + n; j++) {
            Trip trip = trips.get(j - m);
            for (int i = 0; i < m; i++) {
                int depot = depotIndex.get(i);
                PullIn pi = pullIns.stream()
                        .filter(p -> p.getEndLoc() == trip.getEndLoc() && p.getDepot() == depot)
                        .findAny().orElse(null);
                cost[j][i] = (pi == null ? -1 : 5000 + pi.getDuration());
            }
        }
        //trip-trip costs
        for (int i = m; i < m + n; i++) {
            Trip t0 = trips.get(i - m);
            for (int j = m; j < m + n; j++) {
                Trip t1 = trips.get(j - m);
                //arc de la t0 la t1 daca: t1 este dupa t0, sunt in pereche sau in movements
                int duration = (int) Duration.between(t0.getEndTime(), t1.getStartTime()).toMinutes();
                if (duration < 0) {
                    continue;
                }
                if (t0.getEndLoc() == t1.getStartLoc()) {
                    cost[i][j] = duration;
                    continue;
                }
                Movement move = movements.stream()
                        .filter(mv -> mv.getStartLoc() == t0.getEndLoc() && mv.getEndLoc() == t1.getStartLoc())
                        .findAny().orElse(null);
                if (move != null) {
                    cost[i][j] = move.getDuration();
                    continue;
                }
                //cost[i][j] = 1000;
            }
        }
        //movements
        /*
        for(Movement move : movements) {
            Trip t0 = trips.get(i - m);
            Trip t1 = trips.get(i - m);
        }*/

        //
        Instance instance = new Instance("demo", m, n, nbVehicles, cost);
        instance.write("d:/java/MDVSP/demo.inp");

        Model model = new RepairModel(new ModelRelaxed(instance));
        model.setOutputEnabled(true);
        try {
            Solution sol = model.solve();
            if (sol != null) {
                //System.out.println(sol.toursToStringSimple());
                System.out.println("Tours: " + sol.getTours().size());
                for (Tour t : sol.getTours()) {
                    printTour(t);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void printTour(Tour tour) {
        StringBuilder sb = new StringBuilder();
        int m = depotIndex.size();
        int k = tour.size();
        sb.append("Depot_").append(depotIndex.get(tour.get(0)));
        for (int i = 1; i < k - 1; i++) {
            int t0 = tour.get(i - 1);
            int t1 = tour.get(i);
            Trip trip0 = i > 1 ? trips.get(t0 - m) : null;
            Trip trip1 = trips.get(t1 - m);
            if (trip0 == null || trip0.getEndLoc() == trip1.getStartLoc()) {
                sb.append(" --> (").append(trip1).append(")");
            } else {
                sb.append(" -***-> (").append(trip1).append(")");
            }
        }
        sb.append(" --> Depot_").append(depotIndex.get(tour.get(k - 1)));
        System.out.println(sb.toString());

    }

    public static void main(String args[]) {
        Timetable tt = new Timetable("d:/java/MDVSP/input/");
        try {
            tt.loadTrips("TimeTable-simple.csv");
            tt.loadMovements("Movements.csv");
            tt.loadPullOuts("PullOuts.csv");
            tt.loadPullIns("PullIns.csv");
            tt.solve();
        } catch (IOException | InvalidDataException e) {
            System.err.println(e);
        }
    }
}
