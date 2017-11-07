package com.patex.utils;


public class Res implements Comparable<Res>{

    private final String key;
    private final Object[] objs;


    public Res(String key, Object... objs) {
        this.key = key;
        this.objs = objs;
    }

    public String getKey() {
        return key;
    }

    public Object[] getObjs() {
        return objs;
    }


    public int compareTo(Res other) {
        return key.compareTo(other.key);
    }
}
