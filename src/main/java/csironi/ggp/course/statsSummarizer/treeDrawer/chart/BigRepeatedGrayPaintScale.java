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

        this.grayScaleColors = new Color[23];
        this.grayScaleColors[0] = new Color(0, 0, 0, 235);
        this.grayScaleColors[1] = new Color(0, 0, 0, 225);
        this.grayScaleColors[2] = new Color(0, 0, 0, 215);
        this.grayScaleColors[3] = new Color(0, 0, 0, 205);
        this.grayScaleColors[4] = new Color(0, 0, 0, 195);
        this.grayScaleColors[5] = new Color(0, 0, 0, 185);
        this.grayScaleColors[6] = new Color(0, 0, 0, 175);
        this.grayScaleColors[7] = new Color(0, 0, 0, 165);
        this.grayScaleColors[8] = new Color(0, 0, 0, 155);
        this.grayScaleColors[9] = new Color(0, 0, 0, 145);
        this.grayScaleColors[10] = new Color(0, 0, 0, 135);
        this.grayScaleColors[11] = new Color(0, 0, 0, 125);
        this.grayScaleColors[12] = new Color(0, 0, 0, 115);
        this.grayScaleColors[13] = new Color(0, 0, 0, 105);
        this.grayScaleColors[14] = new Color(0, 0, 0, 95);
        this.grayScaleColors[15] = new Color(0, 0, 0, 85);
        this.grayScaleColors[16] = new Color(0, 0, 0, 75);
        this.grayScaleColors[17] = new Color(0, 0, 0, 65);
        this.grayScaleColors[18] = new Color(0, 0, 0, 55);
        this.grayScaleColors[19] = new Color(0, 0, 0, 45);
        this.grayScaleColors[20] = new Color(0, 0, 0, 35);
        this.grayScaleColors[21] = new Color(0, 0, 0, 25);
        this.grayScaleColors[22] = new Color(0, 0, 0, 15);

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
