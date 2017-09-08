package com.patex.extlib;

import com.patex.LibException;
import com.patex.entities.ZUser;
import com.patex.opds.OPDSEntryI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 */


@Service
public class ExtLibService {

    private static Logger log = LoggerFactory.getLogger(ExtLibService.class);


    @Autowired
    private ExtLibFactory extLibFactory;

    public ExtLibFeed getDataForLibrary(String prefix, Long libId, Map<String, String> requestParams) throws LibException {
        ExtLibFeed data = extLibFactory.getExtLib(libId).getExtLibFeed(requestParams);
        return data.updateWithPrefix(prefix + "/" + libId + "/");
    }

    public List<OPDSEntryI> getRoot(String prefix) {
        return extLibFactory.getAll().stream().
                map(extLib -> new ExtLibOPDSEntry(extLib.getRootEntry(), prefix+"/"+extLib.getExtLibId()))
                .collect(Collectors.toList());
    }


    public String actionExtLibData(long libId, String action, Map<String, String> params, ZUser user) throws LibException {
        log.trace("{} libid:{} params:{}", action, libId, params);
        ExtLib extLib = extLibFactory.getExtLib(libId);
        return extLib.action(action, params, user);
    }

    @Scheduled(cron = "0 00 12 * * *")
    public void checkSubscriptions() {
        extLibFactory.getAll().forEach(ExtLib::checkSubscriptions);

    }
}
