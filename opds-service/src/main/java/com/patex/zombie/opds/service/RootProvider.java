package com.patex.zombie.opds.service;

import com.patex.zombie.opds.controller.OPDSController;
import com.patex.opds.OPDSEntry;
import com.patex.opds.OPDSLink;
import com.patex.utils.LinkUtils;
import lombok.Getter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Component
public class RootProvider {

    private List<RootInfo> roots = new ArrayList<>();

    @PostConstruct
    public void setUp() {
        registerRoot("latest", () -> true);
        registerRoot("newBooks", () -> true);
        registerRoot("authors", () -> true);
    }

    public List<OPDSEntry> getRootEntries() {
        return roots.stream().map(RootInfo::getRoot).collect(Collectors.toList());
    }

    public void registerRoot(String root, Supplier<Boolean> exists) {
        if (roots.stream().map(RootInfo::getRootId).noneMatch(root::equals)) {
            roots.add(new RootInfo(root, exists));
        }
    }

    @Scheduled(cron = "5 * * * * *")
    public void checkRoots() {
        roots = roots.stream().filter(RootInfo::isValid).collect(Collectors.toList());
    }

    private static class RootInfo {

        @Getter
        private final String rootId;

        @Getter
        private final OPDSEntry root;

        private final Supplier<Boolean> exists;

        public RootInfo(String rootId, Supplier<Boolean> exists) {
            this.rootId = rootId;
            this.root = OPDSEntry.builder("root:" + rootId, "opds:" + rootId).
                    addLink(LinkUtils.makeURL(OPDSController.PREFIX, rootId), OPDSLink.OPDS_CATALOG).
                    build();
            this.exists = exists;
        }

        public Boolean isValid() {
            return exists.get();
        }
    }
}
