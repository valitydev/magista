package dev.vality.magista.util;

import java.util.List;

public class TokenUtil {

    public static  <T> T getLastElement(List<T> objects) {
        return objects.get(objects.size() - 1);
    }

}
