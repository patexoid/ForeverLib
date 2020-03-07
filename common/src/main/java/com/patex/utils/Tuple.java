package com.patex.utils;

/**
 * Created by Alexey on 30.07.2017.
 */
public class Tuple<F, S> {
    public final F _1;
    public final S _2;

    public Tuple(F _1, S _2) {
        this._1 = _1;
        this._2 = _2;
    }

    public F _1() {
        return _1;
    }

    public S _2() {
        return _2;
    }
}
