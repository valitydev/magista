package dev.vality.magista.query;


/**
 * Created by vpankrashkin on 03.08.16.
 */
public interface FunctionQuery<T, CT> extends Query<T, CT> {

    String getName();
}
