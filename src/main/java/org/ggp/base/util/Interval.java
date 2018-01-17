package org.ggp.base.util;

/**
 * This class represents an interval.
 * NOTE: this class assumes that the value "infinity" cannot be specified for the interval, so when the interval is closed
 * it doesn't check if the extreme is infinity (or -infinity).
 * @author c.sironi
 *
 */
public class Interval {

	private double leftExtreme;

	private double rightExtreme;

	// True if left extreme is included in the interval
	private boolean leftClosed;

	// True if right extreme is included in the interval
	private boolean rightClosed;

	public Interval(double leftExtreme, double rightExtreme, boolean leftClosed, boolean rightClosed) {

		if(leftExtreme < rightExtreme || (leftExtreme == rightExtreme && leftClosed && rightClosed)){
			this.leftExtreme = leftExtreme;
			this.rightExtreme = rightExtreme;
			this.leftClosed = leftClosed;
			this.rightClosed = rightClosed;
		}else{
			throw new RuntimeException("Infeasible interval: " + (leftClosed ? "[" : "(") + leftExtreme + ";" + rightExtreme + (rightClosed ? "]" : ")"));
		}

	}

	public double getLeftExtreme(){
		return this.leftExtreme;
	}

	public double getRightExtreme(){
		return this.rightExtreme;
	}

	public boolean isLeftClosed(){
		return this.leftClosed;
	}

	public boolean isRightClosed(){
		return this.isRightClosed();
	}

	public boolean contains(double value){
		return ((value > this.leftExtreme || (this.leftClosed && value == this.leftExtreme)) &&
				(value < this.rightExtreme || (this.rightClosed && value == this.rightExtreme)));
	}

	@Override
	public String toString() {
		String interval = "";

		if(this.leftClosed) {
			interval += "[";
		}else {
			interval += "(";
		}

		interval += (this.leftExtreme + "," + this.rightExtreme);

		if(this.rightClosed) {
			interval += "]";
		}else {
			interval += ")";
		}

		return interval;
	}

}
