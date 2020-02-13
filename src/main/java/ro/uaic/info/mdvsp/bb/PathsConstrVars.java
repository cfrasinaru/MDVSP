package ro.uaic.info.mdvsp.bb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class PathsConstrVars {
	public ArrayList<ArrayList<Integer>> pathConstr_list = new ArrayList<ArrayList<Integer>>();
	public HashMap<Arc, ArrayList<ArrayList<Integer>>> arcPaths_list = new HashMap<Arc, ArrayList<ArrayList<Integer>>>();
	public ArrayList<Arc> var2BSet_0 = new ArrayList<Arc>();
	public ArrayList<Arc> var2BSet_1 = new ArrayList<Arc>();

	public PathsConstrVars(HashMap<Arc, ArrayList<ArrayList<Integer>>> arc_list,
			ArrayList<ArrayList<Integer>> path_list, ArrayList<Arc> var_list0, ArrayList<Arc> var_list1) {
		this.pathConstr_list = path_list;// the path list for generating constraints
		this.arcPaths_list = arc_list;// a collection of paths that contains a certain arc
		this.var2BSet_0 = var_list0;// variables to be set to 0
		this.var2BSet_1 = var_list1;// variables to be set to 1
	}

	public PathsConstrVars(HashMap<Arc, ArrayList<ArrayList<Integer>>> arc_list,
			ArrayList<ArrayList<Integer>> path_list, ArrayList<Arc> var_list0, ArrayList<Arc> var_list1, int i, int j) {
				//ArrayList<ArrayList<Integer>> pathConstraints_list = stack_pathConstr_vars2BeSet.pathConstraints_list;
		Iterator<ArrayList<Integer>> iterator_path = path_list.iterator();
		int from, to, found = 0;

		while (iterator_path.hasNext()) {
			ArrayList<Integer> path = (ArrayList<Integer>) iterator_path.next();
			if (path != null) {
				found = 0;
				from = path.get(0);
				for (int h = 1; h < path.size(); h++) {
					to = path.get(h);
					if(from == i && to == j) {
						found = 1;
						iterator_path.remove();
						break;
					}
					from = to;
				}
				//iterator_path.remove(); // avoids a ConcurrentModificationException
			}
		}
		this.pathConstr_list = path_list;// the path list for generating constraints
		this.arcPaths_list = arc_list;// a collection of paths that contains a certain arc
		this.var2BSet_0 = var_list0;// variables to be set to 0
		this.var2BSet_1 = var_list1;// variables to be set to 1
	}

	public PathsConstrVars() {
	}
}
