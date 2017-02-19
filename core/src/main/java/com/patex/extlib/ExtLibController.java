package com.patex.extlib;

import com.patex.LibException;
import com.patex.opds.OPDSController2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.patex.opds.OPDSController2.*;

/**
 *
 */
@Controller
@RequestMapping(ExtLibController.OPDS_EXT_LIB)
public class ExtLibController {

    private static final String EXT_LIB = "extLib";
    static final String OPDS_EXT_LIB = "/" + PREFIX + "/" + EXT_LIB;
    @Autowired
    private ExtLibService extLibService;

    @Autowired
    private OPDSController2 opdsController2;

    @PostConstruct
    public void setUp() {
        opdsController2.addRoot(createEntry("root:libraries", "Библиотеки", OPDS_EXT_LIB));
    }

    @RequestMapping(produces = APPLICATION_ATOM_XML)
    public ModelAndView getExtLibraries() {
        return createMav("Библиотеки", extLibService.findAll(), extLibraries ->
                StreamSupport.stream(extLibService.findAll().spliterator(), false).map(extLib ->
                        createEntry("" + extLib.getId(), extLib.getName(), makeURL(OPDS_EXT_LIB, extLib.getId()))
                ).collect(Collectors.toList()));
    }

    @RequestMapping(value = "{id}", produces = APPLICATION_ATOM_XML)
    public ModelAndView getExtLibData(@PathVariable(value = "id") long id,
                                      @RequestParam(required = false) Map<String, String> requestParams) throws LibException {

        ExtLibFeed extLibFeed = extLibService.getDataForLibrary(OPDS_EXT_LIB, id, requestParams);
        return createMav(extLibFeed.getTitle(), extLibFeed.getEntries());
    }

    @RequestMapping(value = "{id}/{action}", produces = APPLICATION_ATOM_XML)
    public String actionExtLibData(@PathVariable(value = "id") long id,
                                   @PathVariable(value = "action") String action,
                                   @RequestParam(required = false) Map<String, String> requestParams) throws LibException {

        String redirect = extLibService.actionExtLibData(id, action, requestParams);
        return "redirect:" + redirect;
    }


}
