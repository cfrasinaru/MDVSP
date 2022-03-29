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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    static final int DEPOT_TIME = 0;
    final List<Integer> depots = new ArrayList<>();
    final List<Trip> trips = new ArrayList<>();
    final List<Movement> movements = new ArrayList<>();
    final List<PullOut> pullOuts = new ArrayList<>();
    final List<PullIn> pullIns = new ArrayList<>();
    final Set<Integer> routes = new HashSet<>();
    final Set<Integer> locations = new HashSet<>();

    final String directory;
    //
    final Map<Integer, Integer> depotMap = new HashMap<>();
    final Map<Trip, Integer> tripMap = new HashMap<>();
    int[][] cost;

    public Timetable(String directory) {
        this.directory = directory;
    }

    public void loadTrips(String filename) throws IOException {
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
            if (!depots.contains(p.getDepot())) {
                depots.add(p.getDepot());
            }
        }
    }

    public void loadPullIns(String filename) throws IOException {
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
            if (!depots.contains(p.getDepot())) {
                depots.add(p.getDepot());
            }
        }
    }

    public void solve() throws IOException, InvalidDataException {
        StringBuilder errors = new StringBuilder();
        int m = depots.size();
        int n = trips.size();

        //index depots
        for (int i = 0; i < m; i++) {
            depotMap.put(depots.get(i), i);
        }

        //check and index trips
        for (int i = 0; i < n; i++) {
            Trip t = trips.get(i);
            if (t.getStartTime().isAfter(t.getEndTime())) {
                errors.append("\n").append("Invalid trip times: ").append(t);
            }
            if (t.getStartLoc() == t.getEndLoc()) {
                errors.append("\n").append("Dubious trip: ").append(t);
            }
            tripMap.put(t, i + m);
        }

        //check movements
        for (Movement move : movements) {
            if (!locations.contains(move.getStartLoc())) {
                errors.append("\n").append("Invalid movement start location: ").append(move.getEndLoc());
            }
            if (!locations.contains(move.getEndLoc())) {
                errors.append("\n").append("Invalid movement end location: ").append(move.getEndLoc());
            }
        }

        if (errors.length() > 0) {
            //throw new InvalidDataException(errors.toString());
            //System.out.println(errors);
        }

        System.out.println("Depots: " + m);
        System.out.println("Trips: " + n);
        System.out.println("Locations: " + locations.size());
        System.out.println("Routes: " + routes.size());
        /*
        for (int l : locations) {
            System.out.println("Location " + l);
            System.out.println("\tOut:" + pullOuts.stream().filter(p -> p.getStartLoc() == l).collect(Collectors.toList()));
            System.out.println("\tIn:" + pullIns.stream().filter(p -> p.getEndLoc() == l).collect(Collectors.toList()));
        }*/

        int[] nbVehicles = new int[m];
        for (int i = 0; i < m; i++) {
            nbVehicles[i] = n;
        }
        cost = new int[n + m][n + m];
        for (int i = 0; i < m + n; i++) {
            for (int j = 0; j < m + n; j++) {
                cost[i][j] = -1;
                if ((i < m && j >= m) || (i >= m && j < m)) {
                    //cost[i][j] = 9999;
                }
            }
        }

        //pull out costs
        for (PullOut po : pullOuts) {
            for (Trip trip : trips) {
                if (po.getStartLoc() == trip.getStartLoc()) {
                    cost[depotMap.get(po.getDepot())][tripMap.get(trip)] = po.getDuration();
                }
            }
        }
        //pull out: trip-uri care contin depot
        for (int depot : depots) {
            for (Trip t : trips) {
                if (t.getStartLoc() == depot) {
                    cost[depotMap.get(depot)][tripMap.get(t)] = 0; //t.getDuration();
                }
            }
        }

        //pull in costs
        for (PullIn pi : pullIns) {
            for (Trip trip : trips) {
                if (pi.getEndLoc() == trip.getEndLoc()) {
                    cost[tripMap.get(trip)][depotMap.get(pi.getDepot())] = pi.getDuration();
                }
            }
        }
        //pull-in: trip-uri care contin depot
        for (int depot : depots) {
            for (Trip t : trips) {
                if (t.getEndLoc() == depot) {
                    cost[tripMap.get(t)][depotMap.get(depot)] = 0; //t.getDuration();
                }
            }
        }

        //trip-trip costs
        for (int i = m; i < m + n; i++) {
            Trip t0 = trips.get(i - m);
            for (int j = m; j < m + n; j++) {
                Trip t1 = trips.get(j - m);
                //arc de la t0 la t1 daca: t1 este dupa t0, sunt in pereche sau in movements
                if (t0.getEndTime().isAfter(t1.getStartTime())) {
                    continue;
                }
                int duration = (int) Duration.between(t0.getEndTime(), t1.getStartTime()).toMinutes();
                if (t0.getEndLoc() == t1.getStartLoc()) {
                    cost[i][j] = duration;
                    continue;
                }
                Movement move = movements.stream()
                        .filter(mv -> mv.getStartLoc() == t0.getEndLoc() && mv.getEndLoc() == t1.getStartLoc())
                        .findAny().orElse(null);
                if (move != null) {
                    if (!t0.getEndTime().plusMinutes(move.getDuration()).isAfter(t1.getStartTime())) {
                        cost[i][j] = move.getDuration();
                    }
                }
            }
        }

        //
        Instance instance = new Instance("demo", m, n, nbVehicles, cost);
        instance.write("d:/java/MDVSP/demo.inp");

        Model model = new RepairModel(new ModelRelaxed(instance));
        var graph = model.getGraph();
        for (int v : graph.vertexSet()) {
            if (v < m) {
                continue;
            }
            if (graph.inDegreeOf(v) == 0) {
                System.out.println("Indegree 0: " + trips.get(v - m));
            }
            if (graph.outDegreeOf(v) == 0) {
                System.out.println("Outdegree 0: " + trips.get(v - m));
            }
        }
        //DirectedAcyclicGraph dag = model.createReducedGraph();

        model.setOutputEnabled(true);
        Solution sol = model.solve();
        if (sol != null) {
            System.out.println("Model is feasible. Optimum: " + sol.totalCost() + " min");
            System.out.println("Initial tours: " + sol.getTours().size());
            List<SimpleTour> tours = new ArrayList<>();
            for (Tour t : sol.getTours()) {
                var st = new SimpleTour(this, t);
                tours.add(st);
                //System.out.println(st);
                //System.out.println(st.startTime + "-" + st.endTime);
            }
            var schedule = createSchedule(tours);
            System.out.println("Vehicles: " + schedule.size());
            for(MultiTour mt : schedule) {
                System.out.println(mt);
            }
        } 
    }

    private List<MultiTour> createSchedule(List<SimpleTour> tours) {
        List<MultiTour> schedule = new ArrayList<>();
        List<SimpleTour> candidates = tours.stream().sorted().collect(Collectors.toList());
        
        int vehicle = 1;
        MultiTour multi = new MultiTour(vehicle);
        LocalTime minStartTime = null;
        while (!candidates.isEmpty()) {
            SimpleTour t = findTour(minStartTime, candidates);
            if (t != null) {
                multi.add(t);
                candidates.remove(t);
                minStartTime = t.getEndTime();
            } else {
                schedule.add(multi);
                multi = new MultiTour(++vehicle);
                minStartTime = null;
            }
        }
        return schedule;
    }

    private SimpleTour findTour(LocalTime minStartTime, List<SimpleTour> candidates) {
        if (minStartTime == null) {
            return candidates.get(0);
        }
        for (SimpleTour t : candidates) {
            if (t.getStartTime().isAfter(minStartTime.plusMinutes(DEPOT_TIME))) {
                return t;
            }
        }
        return null;
    }

    public static void main(String args[]) {
        Timetable tt = new Timetable("d:/java/MDVSP/input/");
        try {
            tt.loadTrips("TimeTable.csv");
            tt.loadMovements("Movements.csv");
            tt.loadPullOuts("PullOuts.csv");
            tt.loadPullIns("PullIns.csv");
            tt.solve();
        } catch (IOException | InvalidDataException e) {
            System.err.println(e);
        }
    }
}
