package csironi.ggp.course.statsSummarizer.treeDrawer.chart;

import java.awt.Color;
import java.awt.Paint;

public class RepeatedDarkColorPaintScale implements TreePaintScale {

	private final int lowerBound;
    private final int upperBound;

    private final Color[] darkColorScaleColors;

    public RepeatedDarkColorPaintScale(double lowerBound, double upperBound) {
        this.lowerBound = (int) Math.floor(lowerBound);
        this.upperBound = (int) Math.floor(upperBound);

        this.darkColorScaleColors = new Color[15];
        this.darkColorScaleColors[0] = new Color(53, 64, 115);
        this.darkColorScaleColors[1] = new Color(118, 53, 119);
        this.darkColorScaleColors[2] = new Color(172, 27, 88);
        this.darkColorScaleColors[3] = new Color(192, 46, 29);
        this.darkColorScaleColors[4] = new Color(217, 78, 31);
        this.darkColorScaleColors[5] = new Color(241, 108, 32);
        this.darkColorScaleColors[6] = new Color(239, 139, 44);
        this.darkColorScaleColors[7] = new Color(236, 170, 56);
        this.darkColorScaleColors[8] = new Color(235, 200, 68);
        this.darkColorScaleColors[9] = new Color(162, 184, 108);
        this.darkColorScaleColors[10] = new Color(92, 167, 147);
        this.darkColorScaleColors[11] = new Color(19, 149, 186);
        this.darkColorScaleColors[12] = new Color(17, 120, 153);
        this.darkColorScaleColors[13] = new Color(15, 91, 120);
        this.darkColorScaleColors[14] = new Color(13, 60, 85);

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

		return this.darkColorScaleColors[(intValue-this.lowerBound)%this.darkColorScaleColors.length];
	}

	@Override
	public Color getSelectedActionColor() {
		return new Color(0,0,0);
	}

}
