package com.patex.extlib;

import com.patex.LibException;
import com.patex.entities.ExtLibrary;
import com.patex.entities.ExtLibraryRepository;
import com.patex.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Alexey on 12.02.2017.
 */
@Component
public class ExtLibFactory {

    @Autowired
    private ExtLibraryRepository extLibRepo;

    @Autowired
    private BookService bookService;

    private final Map<Long, ExtLib> extLibMap = new HashMap<>();

    ExtLib getExtLib(Long id) throws LibException {
        ExtLib extLib = extLibMap.get(id);
        if (extLib == null) {
            synchronized (extLibMap) {
                extLib = extLibMap.get(id);
                if (extLib == null) {
                    ExtLibrary extLibrary = extLibRepo.findOne(id);
                    if (extLibrary == null) {
                        throw new LibException("External Lib unknown id:" + id);
                    }
                    extLib = new ExtLib(extLibrary, bookService);
                    extLibMap.put(id, extLib);
                }
            }
        }
        return extLib;
    }

}
