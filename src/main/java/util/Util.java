package util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A class containing useful helper methods
 */
public class Util {
    /**
     * Puts given collection into sorted list
     */
    public static <T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c) {
        List<T> list = new ArrayList<>(c);
        java.util.Collections.sort(list);
        return list;
    }

    public static double roundToOneDecimal (double value) {
        int scale = (int) Math.pow(10, 1);
        return (double) Math.round(value * scale) / scale;
    }
}
