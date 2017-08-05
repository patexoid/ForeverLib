package com.patex.utils.shingle;

import com.patex.utils.Tuple;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;

/**
 * Created by Alexey on 16.07.2017.
 */
public class ShingleComparsion {


    public <T> Optional<T> findSimilar(T primary, Collection<T> secondaries, Function<T, Shingleable> mapFunc) {
        Shingler primaryShingler = new Shingler(mapFunc.apply(primary));
        return secondaries.stream().
                map(t -> new Tuple<>(t, new Shingler(mapFunc.apply(t)))).
                filter(tuple -> primaryShingler.isSimilar(tuple._2)).
                map(Tuple::_1).findFirst();
    }

}
