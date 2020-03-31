package ro.uaic.info.mdvsp.bb2;

public class subPathVars {

	private final int from;
	private final int to;

	/**
	 * Initializes a directed edge from vertex {@code v} to vertex {@code w} with
	 * the given {@code weight}.
	 * 
	 * @param v the tail vertex
	 * @param w the head vertex
	 * @throws IllegalArgumentException if either {@code v} or {@code w} is a
	 *                                  negative integer
	 * @throws IllegalArgumentException if {@code weight} is {@code NaN}
	 */
	public subPathVars(int from, int to) {

		this.from = from;
		this.to = to;

	}

	/**
	 * Returns the tail vertex of the directed edge.
	 * 
	 * @return the tail vertex of the directed edge
	 */
	public int from() {
		return from;
	}

	/**
	 * Returns the head vertex of the directed edge.
	 * 
	 * @return the head vertex of the directed edge
	 */
	public int to() {
		return to;
	}

	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		subPathVars other = (subPathVars) obj;
		return this.from==other.from &&  this.to==other.to;
	}
	/**
	 * Returns a string representation of the directed edge.
	 * 
	 * @return a string representation of the directed edge
	 */
	public String toString() {
		return from + "->" + to;
	}

	/**
	 * Unit tests the {@code DirectedEdge} data type.
	 *
	 * @param args the command-line arguments
	 */
	public static void main(String[] args) {
		//aax_SubPathCorrected e = new aax_SubPathCorrected(12, 34, 5203.78, 5304.32);
		//StdOut.println(e);
	}
}
