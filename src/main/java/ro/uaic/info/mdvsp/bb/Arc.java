package ro.uaic.info.mdvsp.bb;

public class Arc {
	public int from;
	public int to;

	public Arc(int from, int to) {

		this.from = from;
		this.to = to;
	}

	public Arc() {
	}

	public String toString() {
		return from + "->" + to;
	}
}
