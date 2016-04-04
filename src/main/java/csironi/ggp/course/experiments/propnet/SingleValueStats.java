package csironi.ggp.course.experiments.propnet;

public interface SingleValueStats {

	public int getNumSamples();

	public double getAvgValue();

	public double getMaxValue();

	public double getMinValue();

	public double getValuesStandardDeviation();

	public double getValuesSEM();

	/**
	 * Attention! This method assumes there are enough samples to use the t-distribution constant equal to 1.96.
	 * @return
	 */
	public double get95ConfidenceInterval();

	public boolean isEmpty();

}
