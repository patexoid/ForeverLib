package com.patex.extlib;

import com.patex.LibException;
import com.patex.entities.ExtLibrary;
import com.patex.entities.ExtLibraryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 *
 */


@Service
public class ExtLibService {

    private static Logger log = LoggerFactory.getLogger(ExtLibService.class);

    @Autowired
    private ExtLibraryRepository extLibRepo;

    @Autowired
    private ExtLibFactory extLibFactory;

    public ExtLibFeed getDataForLibrary(String prefix, Long libId, Map<String, String> requestParams) throws LibException {
        ExtLibFeed data = extLibFactory.getExtLib(libId).getExtLibFeed(requestParams);
        data.getEntries().forEach(entry -> entry.getLinks().
                forEach(link -> link.setHref(prefix + "/" + libId + "/" + link.getHref())));//TODO FIXIT
        return data;
    }

    public Iterable<ExtLibrary> findAll() {
        return extLibRepo.findAll();
    }


    public String actionExtLibData(long libId, String action, Map<String, String> params) throws LibException {
        log.trace("{} libid:{} params:{}", action, libId, params);
        ExtLib extLib = extLibFactory.getExtLib(libId);
        return extLib.action(action, params);
    }
}
