package com.patex.utils.shingle;

import com.patex.utils.Tuple;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Alexey on 16.07.2017.
 */
public class ShingleComparsion {


    public <T> Set<T> findSimilar(T primary, Collection<T> secondaries, Function<T, Shingleable> mapFunc) {
        Shingler primaryShingler = new Shingler(mapFunc.apply(primary));
        return secondaries.stream().
                map(t -> new Tuple<>(t, new Shingler(mapFunc.apply(t)))).
                filter(tuple -> primaryShingler.isSimilar(tuple._2)).
                map(Tuple::_1).
                collect(Collectors.toSet());
    }

}
