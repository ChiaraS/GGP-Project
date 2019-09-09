package csironi.ggp.course.statsSummarizer.paramVisits;

/**
 * This class groups all statistics about the visits of a certain parameter tuple
 * (which could also be a single parameter)
 *
 * @author C.Sironi
 *
 */
public class TupleVisitsStats {

	private String tupleValue;

	private int visits;

	private double visitsPercentage;

	public TupleVisitsStats(String tupleValue, int visits) {

		this.tupleValue = tupleValue;

		this.visits = visits;

	}

	public String getTuple() {
		return tupleValue;
	}

	public int getVisits() {
		return visits;
	}

	public double getVisitsPercentage() {
		return visitsPercentage;
	}

	public void computeVisitsPercentage(double totalVisits) {

		if(totalVisits == 0.0) {
			this.visitsPercentage = 0;
		}else {
			this.visitsPercentage = (((double)this.visits)/totalVisits) * 100.0;
		}

	}


}
