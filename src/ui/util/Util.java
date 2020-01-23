package ui.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Util {
    public static
    <T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c) {
        List<T> list = new ArrayList<>(c);
        java.util.Collections.sort(list);
        return list;
    }
}
