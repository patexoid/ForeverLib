package com.patex.forever.opds.controller;

import com.patex.forever.opds.model.OPDSEntry;

import java.util.List;

public interface RootProvider {

    List<OPDSEntry> getRoot();
}
