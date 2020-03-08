package com.patex.zombie.opds.controller;

import com.patex.zombie.opds.model.OPDSEntry;

import java.util.List;

public interface RootProvider {

    List<OPDSEntry> getRoot();
}
