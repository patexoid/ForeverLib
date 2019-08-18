package com.patex.opds.extlib;

import com.patex.entities.ExtLibrary;
import com.patex.utils.Tuple;
import org.springframework.stereotype.Component;

@Component
public class ExtLibScopeStorage {

    private final ThreadLocal<Tuple<ExtLibrary, Integer>> scopeTL = new ThreadLocal<>();

    public ExtLibrary getCurrentExtLib() {
        return scopeTL.get()._1;
    }

    public void setScope(ExtLibrary extLibrary) {
        Tuple<ExtLibrary, Integer> scopeDesc = scopeTL.get();
        if (scopeDesc == null) {
            scopeDesc = new Tuple<>(extLibrary, 1);
        } else {
            scopeDesc = new Tuple<>(extLibrary, scopeDesc._2 + 1);
        }
        scopeTL.set(scopeDesc);
    }

    public void clearScope() {
        Tuple<ExtLibrary, Integer> scopeDesc = scopeTL.get();
        if (scopeDesc._2 == 0) {
            scopeTL.remove();
        } else {
            scopeTL.set(new Tuple<>(scopeDesc._1, scopeDesc._2 - 1));
        }
    }
}
