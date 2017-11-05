package com.patex.messaging;

import com.patex.service.Resources;

import java.util.Locale;

public class ZMessage {

    private final String mesage;
    private final Object[] objs;

    public ZMessage(String mesage, Object... objs) {
        this.mesage = mesage;
        this.objs = objs;
    }

    public String getMessage(Resources res, Locale loc){
        return res.get(loc, mesage,objs);
    }
}
