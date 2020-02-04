package ui.resources;

import java.awt.*;

/**
 * All the colors a point in the t-SNE plot can be
 */
public final class PointColor {
    public final static Color[] COLORS = {new Color(0.894f, 0.102f, 0.120f), new Color(0.216f, 0.494f, 0.722f),
                                          new Color(0.302f, 0.686f, 0.290f), new Color(0.596f, 0.306f, 0.639f),
                                          new Color(1.000f, 0.498f, 0.000f), new Color(1.000f, 1.000f, 0.200f),
                                          new Color(0.651f, 0.337f, 0.157f), new Color(0.969f, 0.506f, 0.749f),
                                          new Color(0.600f, 0.600f, 0.600f), new Color(0.553f, 0.867f, 0.780f),
                                          new Color(0.745f, 0.729f, 0.855f), new Color(0.984f, 0.502f, 0.447f),
                                          new Color(0.502f, 0.694f, 0.827f), new Color(0.992f, 0.706f, 0.384f),
                                          new Color(0.702f, 0.871f, 0.412f), new Color(0.988f, 0.804f, 0.898f),
                                          new Color(0.851f, 0.851f, 0.851f)};
    private static int currIndex = 0;

    /**
     * @return a color for a point in the t-SNE plot
     */
    public static Color getColor() {
        Color colorToReturn = COLORS[currIndex];
        currIndex++;
        if (currIndex == COLORS.length)
            currIndex = 0;
        return colorToReturn;
    }
}
