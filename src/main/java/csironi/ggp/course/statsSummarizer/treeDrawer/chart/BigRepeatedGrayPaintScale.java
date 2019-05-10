package csironi.ggp.course.statsSummarizer.treeDrawer.chart;

import java.awt.Color;
import java.awt.Paint;

public class BigRepeatedGrayPaintScale implements TreePaintScale {

	private final int lowerBound;
    private final int upperBound;

    private final Color[] grayScaleColors;

    public BigRepeatedGrayPaintScale(double lowerBound, double upperBound) {
        this.lowerBound = (int) Math.floor(lowerBound);
        this.upperBound = (int) Math.floor(upperBound);

        this.grayScaleColors = new Color[16];
        this.grayScaleColors[0] = new Color(0, 0, 0);
        this.grayScaleColors[1] = new Color(40, 40, 40);
        this.grayScaleColors[2] = new Color(60, 60, 60);
        this.grayScaleColors[3] = new Color(75, 75, 75);
        this.grayScaleColors[4] = new Color(90, 90, 90);
        this.grayScaleColors[5] = new Color(105, 105, 105);
        this.grayScaleColors[6] = new Color(120, 120, 120);
        this.grayScaleColors[7] = new Color(135, 135, 135);
        this.grayScaleColors[8] = new Color(150, 150, 150);
        this.grayScaleColors[9] = new Color(165, 165, 165);
        this.grayScaleColors[10] = new Color(180, 180, 180);
        this.grayScaleColors[11] = new Color(195, 195, 195);
        this.grayScaleColors[12] = new Color(210, 210, 210);
        this.grayScaleColors[13] = new Color(225, 225, 225);
        this.grayScaleColors[14] = new Color(235, 235, 235);
        this.grayScaleColors[15] = new Color(245, 245, 245);

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

		return this.grayScaleColors[(intValue-this.lowerBound)%this.grayScaleColors.length];
	}

	@Override
	public Color getSelectedActionColor() {
		return new Color(255,0,0);
	}
}
