package ui.resources;

import java.awt.*;

public final class PointColours {
    public final static Color[] COLOURS = {Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN,
                                           Color.CYAN, Color.BLUE, Color.MAGENTA, Color.PINK,
                                           Color.BLACK, Color.DARK_GRAY, Color.GRAY, Color.LIGHT_GRAY};
    private static int currIndex = 0;

    public static Color getNextColor() {
        Color colorToReturn = COLOURS[currIndex];
        currIndex++;
        if (currIndex == COLOURS.length)
            currIndex = 0;
        return colorToReturn;
    }
}
