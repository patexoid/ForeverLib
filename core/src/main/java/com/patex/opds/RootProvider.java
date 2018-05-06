package com.patex.opds;

import com.patex.opds.converters.OPDSEntry;

import java.util.List;

public interface RootProvider {

    List<OPDSEntry> getRoot();
}
