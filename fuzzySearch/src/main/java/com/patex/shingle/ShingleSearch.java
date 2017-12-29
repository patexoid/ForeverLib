package com.patex.shingle;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class ShingleSearch<T,ID> {

    private final Function<T, Collection<T>> preSearch;
    private final ShingleMatcher<T,ID> shingleMatcher;

    public ShingleSearch(Function<T, Collection<T>> preSearch,
                         Function<T, Shingleable> mapFunc,
                         Function<T, ID> idFunc, int coef, int cacheSize
    ) {
        this.preSearch = preSearch;
        shingleMatcher=new ShingleMatcher<>(mapFunc, idFunc, coef, cacheSize);
    }

    public Optional<T> findSimilar(T t) {
        return findSimilarStream(t).findFirst();
    }

    public Stream<T> findSimilarStream(T t) {
        return preSearch.apply(t).stream().filter(obj -> shingleMatcher.isSimilar(obj, t));
    }


    public void invalidate(T secondary) {
        shingleMatcher.invalidate(secondary);
    }

    public void setStorage(ShingleCacheStorage<T> storage) {
        shingleMatcher.setStorage(storage);
    }
}
