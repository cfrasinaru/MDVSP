package ro.uaic.info.mdvsp.bb2;

import edu.princeton.cs.algs4.DirectedEdge;
import edu.princeton.cs.algs4.EdgeWeightedDigraph;
import edu.princeton.cs.algs4.EdgeWeightedDirectedCycle;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.Queue;
import edu.princeton.cs.algs4.Stack;
import edu.princeton.cs.algs4.StdOut;

/**
 * This is a modified version of the Bellman Ford Moore algorithm 
 * using the assumption that the digraph doesn't have negative cycles.
 * For optimization purposes we cancel the validations also.
 * @author Olariu E. F.
 */

/**
 * The {@code BellmanFordSP} class represents a data type for solving the
 * single-source shortest paths problem in edge-weighted digraphs with no
 * negative cycles. The edge weights can be positive, negative, or zero. This
 * class finds either a shortest path from the source vertex <em>s</em> to every
 * other vertex or a negative cycle reachable from the source vertex.
 * <p>
 * This implementation uses the Bellman-Ford-Moore algorithm. The constructor
 * takes time proportional to <em>V</em> (<em>V</em> + <em>E</em>) in the worst
 * case, where <em>V</em> is the number of vertices and <em>E</em> is the number
 * of edges. Each call to {@code distTo(int)} and {@code hasPathTo(int)},
 * {@code hasNegativeCycle} takes constant time; each call to
 * {@code pathTo(int)} and {@code negativeCycle()} takes time proportional to
 * length of the path returned.
 * <p>
 * For additional documentation, see
 * <a href="https://algs4.cs.princeton.edu/44sp">Section 4.4</a> of
 * <i>Algorithms, 4th Edition</i> by Robert Sedgewick and Kevin Wayne.
 *
 * @author Robert Sedgewick
 * @author Kevin Wayne
 */
public class BellmanNew {
	private double[] distTo; // distTo[v] = distance of shortest s->v path
	private DirectedEdge[] edgeTo; // edgeTo[v] = last edge on shortest s->v path
	private boolean[] onQueue; // onQueue[v] = is v currently on the queue?
	private Queue<Integer> queue; // queue of vertices to relax

	/**
	 * Computes a shortest paths tree from {@code s} to every other vertex in the
	 * edge-weighted digraph {@code G}.
	 * 
	 * @param G the acyclic digraph
	 * @param s the source vertex
	 * @throws IllegalArgumentException unless {@code 0 <= s < V}
	 */
	public BellmanNew(EdgeWeightedDigraph G, int s) {
		distTo = new double[G.V()];
		edgeTo = new DirectedEdge[G.V()];
		onQueue = new boolean[G.V()];
		for (int v = 0; v < G.V(); v++)
			distTo[v] = Double.POSITIVE_INFINITY;
		distTo[s] = 0.0;

		// Bellman-Ford algorithm
		queue = new Queue<Integer>();
		queue.enqueue(s);
		onQueue[s] = true;
		while (!queue.isEmpty()) {
			int v = queue.dequeue();
			onQueue[v] = false;
			relax(G, v);
		}
	}

	// relax vertex v and put other endpoints on queue if changed
	private void relax(EdgeWeightedDigraph G, int v) {
		for (DirectedEdge e : G.adj(v)) {
			int w = e.to();
			if (distTo[w] > distTo[v] + e.weight()) {
				distTo[w] = distTo[v] + e.weight();
				edgeTo[w] = e;
				if (!onQueue[w]) {
					queue.enqueue(w);
					onQueue[w] = true;
				}
			}
		}
	}

	private void relax1(EdgeWeightedDigraph G, int v) {
		double weigth_e, dist_v = distTo[v];
		int w;
		for (DirectedEdge e : G.adj(v)) {
			w = e.to();
			weigth_e = e.weight();
			if (distTo[w] > dist_v + weigth_e) {
				distTo[w] = dist_v + weigth_e;
				edgeTo[w] = e;
				if (!onQueue[w]) {
					queue.enqueue(w);
					onQueue[w] = true;
				}
			}
		}
	}

	/**
	 * Is there a path from the source {@code s} to vertex {@code v}?
	 * 
	 * @param v the destination vertex
	 * @return {@code true} if there is a path from the source vertex {@code s} to
	 *         vertex {@code v}, and {@code false} otherwise
	 * @throws IllegalArgumentException unless {@code 0 <= v < V}
	 */
	public boolean hasPathTo(int v) {
		// validateVertex(v);
		return distTo[v] < Double.POSITIVE_INFINITY;
	}

	/**
	 * Returns a shortest path from the source {@code s} to vertex {@code v}.
	 * 
	 * @param v the destination vertex
	 * @return a shortest path from the source {@code s} to vertex {@code v} as an
	 *         iterable of edges, and {@code null} if no such path
	 * @throws UnsupportedOperationException if there is a negative cost cycle
	 *                                       reachable from the source vertex
	 *                                       {@code s}
	 * @throws IllegalArgumentException      unless {@code 0 <= v < V}
	 */
	public Iterable<DirectedEdge> pathTo(int v) {
		// validateVertex(v);
		// if (hasNegativeCycle())
		// throw new UnsupportedOperationException("Negative cost cycle exists");
		if (!hasPathTo(v))
			return null;
		Stack<DirectedEdge> path = new Stack<DirectedEdge>();
		for (DirectedEdge e = edgeTo[v]; e != null; e = edgeTo[e.from()]) {
			path.push(e);
		}
		return path;
	}

	/**
	 * Returns the length of a shortest path from the source vertex {@code s} to
	 * vertex {@code v}.
	 * 
	 * @param v the destination vertex
	 * @return the length of a shortest path from the source vertex {@code s} to
	 *         vertex {@code v}; {@code Double.POSITIVE_INFINITY} if no such path
	 * @throws UnsupportedOperationException if there is a negative cost cycle
	 *                                       reachable from the source vertex
	 *                                       {@code s}
	 * @throws IllegalArgumentException      unless {@code 0 <= v < V}
	 */
	public double distTo(int v) {
		// validateVertex(v);
		return distTo[v];
	}

	public static void main(String[] args) {
		In in = new In(args[0]);
		int s = Integer.parseInt(args[1]);
		EdgeWeightedDigraph G = new EdgeWeightedDigraph(in);

		BellmanNew sp = new BellmanNew(G, s);

	}

}

/******************************************************************************
 * Copyright 2002-2018, Robert Sedgewick and Kevin Wayne.
 *
 * This file is part of algs4.jar, which accompanies the textbook
 *
 * Algorithms, 4th edition by Robert Sedgewick and Kevin Wayne, Addison-Wesley
 * Professional, 2011, ISBN 0-321-57351-X. http://algs4.cs.princeton.edu
 *
 *
 * algs4.jar is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * algs4.jar is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * algs4.jar. If not, see http://www.gnu.org/licenses.
 ******************************************************************************/
