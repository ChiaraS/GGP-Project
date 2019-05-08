package csironi.ggp.course.statsSummarizer.treeDrawer;

public class Edge {

	private double x1;
	private double x2;
	private double y1;
	private double y2;

	public Edge(double x1, double x2, double y1, double y2) {

		this.x1 = x1;
		this.x2 = x2;
		this.y1 = y1;
		this.y2 = y2;

	}

	public double getX1() {
		return this.x1;
	}

	public double getX2() {
		return this.x2;
	}

	public double getY1() {
		return this.y1;
	}

	public double getY2() {
		return this.y2;
	}

	@Override
	public boolean equals(Object other) {

		if(other == null) {
			return false;
		}

		if(!(other instanceof Edge)) {
			return false;
		}

		Edge otheredge = (Edge)other;

		return (this.x1 == otheredge.getX1() && this.x2 == otheredge.getX2() && this.y1 == otheredge.getY1() && this.y2 == otheredge.getY2());
	}

	@Override
	public String toString() {
		return "" + this.x1 + " " + this.x2 + " " + this.y1 + " " + this.y2;
	}

}
