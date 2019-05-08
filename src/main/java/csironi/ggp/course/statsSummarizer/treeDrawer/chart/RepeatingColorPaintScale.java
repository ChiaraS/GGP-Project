package csironi.ggp.course.statsSummarizer.treeDrawer.chart;

import java.awt.Color;
import java.awt.Paint;

public class RepeatingColorPaintScale implements TreePaintScale {

	private final int lowerBound;
    private final int upperBound;

    private final Paint[] colors;

    private final Color selectedActionColor;

    /**
     *
     * @param lowerBound
     * @param upperBound
     * @param samplesPerScale number of samples that I want to get out of the scale.
     */
    public RepeatingColorPaintScale(double lowerBound, double upperBound, int samplesPerScale) {
        this.lowerBound = (int) Math.floor(lowerBound);
        this.upperBound = (int) Math.floor(upperBound);

        this.colors = new Color[samplesPerScale];

        ExtendedColorPaintScale colorScale = new ExtendedColorPaintScale(1, samplesPerScale);

        for(int i = 1; i <= samplesPerScale; i++) {
        	this.colors[i-1] = colorScale.getPaint(i);
        }

        this.selectedActionColor = colorScale.getSelectedActionColor();
    }

    @Override
    public double getLowerBound() {
        return lowerBound;
    }

    @Override
    public double getUpperBound() {
        return upperBound;
    }

	@Override
	public Paint getPaint(double value) {
		int intValue = (int) Math.floor(value);

		return this.colors[(intValue-this.lowerBound)%this.colors.length];
	}

	@Override
	public Color getSelectedActionColor() {
		return this.selectedActionColor;
	}

}
