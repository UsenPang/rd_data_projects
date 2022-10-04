package utils;

import java.util.Collection;
import java.util.Collections;

public class CollectUtil {
    public static boolean isEmpty(Collection collection) {
        return collection == null || collection.size() <= 0;
    }
}
