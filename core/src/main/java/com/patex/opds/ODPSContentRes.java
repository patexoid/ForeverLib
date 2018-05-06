package com.patex.opds;

import com.patex.service.Resources;

import java.util.Locale;

public class ODPSContentRes extends OPDSContent {


    private final Object[] objs;

    public ODPSContentRes(String value, Object... objs) {
        super(value);
        this.objs = objs;
    }

    public ODPSContentRes(String type, String value, String src, Object... objs) {
        super(type, value, src);
        this.objs = objs;
    }

    @Override
    public String getValue(Resources res, Locale locale) {
        return res.get(locale,getValue(),objs);
    }

    public Object[] getObjs() {
        return objs;
    }
}
