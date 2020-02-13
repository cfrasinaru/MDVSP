package ro.uaic.info.mdvsp.bb;

import java.util.ArrayList;
import java.util.HashMap;

public class Paths_arcPaths {
	public ArrayList<ArrayList<Integer>> path_list;
	public HashMap<Arc, ArrayList<ArrayList<Integer>>> arc_list;

	// ArrayList<ArrayList<Integer>> list = new ArrayList<ArrayList<Integer>>();
	// Map arc_list = new HashMap<arc, ArrayList<ArrayList<Integer>>>();

	public Paths_arcPaths(ArrayList<ArrayList<Integer>> list, HashMap<Arc, ArrayList<ArrayList<Integer>>> arc_list) {
		this.path_list = list;
		this.arc_list = arc_list;
	}

	public Paths_arcPaths() {
	}

	public Paths_arcPaths clonePaths_arcPaths(Paths_arcPaths inputPair) {
		Paths_arcPaths resultedPair = new Paths_arcPaths((ArrayList<ArrayList<Integer>>) inputPair.path_list.clone(),
				(HashMap<Arc, ArrayList<ArrayList<Integer>>>) inputPair.arc_list.clone());
		return resultedPair;

	}
	public Paths_arcPaths clonePaths_arcPaths() {
		Paths_arcPaths resultedPair = new Paths_arcPaths((ArrayList<ArrayList<Integer>>) this.path_list.clone(),
				(HashMap<Arc, ArrayList<ArrayList<Integer>>>) this.arc_list.clone());
		return resultedPair;

	}
}