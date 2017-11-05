package com.patex.opds;

import com.patex.opds.converters.OPDSEntryI;

import java.util.List;

public interface RootProvider {

    List<OPDSEntryI> getRoot();
}
