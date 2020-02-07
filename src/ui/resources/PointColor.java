package ui.resources;

import java.awt.*;

/**
 * All the colors a point in the t-SNE plot can be
 */
public final class PointColor {
    public final static Color[] COLORS = {new Color(0.204f, 0.471f, 0.635f), new Color(0.886f, 0.518f, 0.224f),
                                          new Color(0.239f, 0.592f, 0.286f), new Color(0.753f, 0.278f, 0.282f),
                                          new Color(0.545f, 0.451f, 0.702f), new Color(0.478f, 0.337f, 0.318f),
                                          new Color(0.800f, 0.510f, 0.718f), new Color(0.576f, 0.584f, 0.596f),
                                          new Color(0.631f, 0.659f, 0.271f), new Color(0.169f, 0.690f, 0.722f),
                                          new Color(0.659f, 0.753f, 0.859f), new Color(0.925f, 0.710f, 0.522f),
                                          new Color(0.576f, 0.812f, 0.596f), new Color(0.957f, 0.627f, 0.616f),
                                          new Color(0.725f, 0.675f, 0.784f), new Color(0.706f, 0.592f, 0.576f),
                                          new Color(0.933f, 0.714f, 0.800f), new Color(0.804f, 0.808f, 0.817f),
                                          new Color(0.784f, 0.804f, 0.584f), new Color(0.596f, 0.820f, 0.847f)};
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
