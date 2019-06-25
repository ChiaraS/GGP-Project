package csironi.ggp.course.experiments.propnet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SingleValueDoubleStats implements SingleValueStats{

	/**
	 * The constant for the t-distribution.
	 * TODO: keep a range of constants so that the correct one can be picked
	 * depending on the actual number of samples in this class.
	 */
	private double tconstant;

	/**
	 * List containing all the samples.
	 */
	private List<Double> values;

	/**
	 * Average of the scores.
	 */
	private double valuesSum;

	/**
	 * Maximum inserted value.
	 */
	private double maxValue;

	/**
	 * Minimum inserted value.
	 */
	private double minValue;

	/**
	 *
	 */
	public SingleValueDoubleStats() {

		this.tconstant = 1.96;
		this.values = new ArrayList<Double>();
		this.valuesSum = 0;
		this.maxValue = -Double.MAX_VALUE;
		this.minValue = Double.MAX_VALUE;

	}

	public void addValue(double value){

		this.values.add(value);

		this.valuesSum += value;
		if(value > this.maxValue){
			this.maxValue = value;
		}
		if(value < this.minValue){
			this.minValue = value;
		}
	}

	public List<Double> getValues(){
		return this.values;
	}

	@Override
	public int getNumSamples(){
		return this.values.size();
	}

	@Override
	public double getAvgValue(){
		if(this.values.isEmpty()){
			return -1;
		}

		return (this.valuesSum)/(this.toDoubleWithCheck(this.values.size()));
	}

	@Override
	public double getMaxValue(){
		return this.maxValue;
	}

	@Override
	public double getMinValue(){
		return this.minValue;
	}

	@Override
	public double getValuesStandardDeviation(){
		if(this.values.isEmpty()){
			return -1;
		}
		double squaredSum = 0.0;
		double avgValue = this.getAvgValue();
		for(Double value : this.values){
			double difference = (value.doubleValue() - avgValue);
			squaredSum += (difference * difference);
		}
		return Math.sqrt(squaredSum/(this.toDoubleWithCheck(this.values.size()-1)));
	}

	@Override
	public double getValuesSEM(){
		double standardDev = this.getValuesStandardDeviation();
		if(standardDev == -1){
			return -1;
		}
		return (standardDev / Math.sqrt(this.toDoubleWithCheck(this.values.size())));
	}

	@Override
	public double getMedian(){

		ArrayList<Double> valuesCopy = new ArrayList<Double>(this.values);
		Collections.sort(valuesCopy);

		int index = valuesCopy.size() / 2; // Rounded to lower closest integer

		if(valuesCopy.size()%2 == 1){
			return valuesCopy.get(index).doubleValue();
		}else{
			return (valuesCopy.get(index-1) + valuesCopy.get(index)) / 2.0;
		}
	}

	/**
	 * Attention! This method assumes there are enough samples to use the t-distribution constant equal to 1.96.
	 * @return
	 */
	@Override
	public double get95ConfidenceInterval(){
		return (this.getValuesSEM() * this.tconstant);
	}

	private double toDoubleWithCheck(long longValue){

		double doubleValue = (double)longValue;

		long newLongValue = (long)doubleValue;

		if(newLongValue != longValue){
			System.out.println("Loss of precision: " + longValue + " -> " + newLongValue);
		}

		return doubleValue;

	}

	@Override
	public boolean isEmpty(){
		return this.values.isEmpty();
	}

	public double getTotalSum(){
		return this.valuesSum;
	}

}
