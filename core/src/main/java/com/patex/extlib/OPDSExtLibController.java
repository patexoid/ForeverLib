package com.patex.extlib;

import com.patex.LibException;
import com.patex.opds.OPDSController;
import com.patex.opds.SaveLatest;
import com.patex.service.ZUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.PostConstruct;
import java.util.Map;

import static com.patex.opds.OPDSController.*;
import static com.patex.service.ZUserService.USER;

/**
 *
 */
@Controller
@RequestMapping(OPDSExtLibController.OPDS_EXT_LIB)
public class OPDSExtLibController {

    private static final String EXT_LIB = "extLib";
    static final String OPDS_EXT_LIB = "/" + PREFIX + "/" + EXT_LIB;

    @Autowired
    private ExtLibService extLibService;

    @Autowired
    private OPDSController opdsController;


    @Autowired
    private ZUserService userService;

    @PostConstruct
    public void setUp() {
        opdsController.addRoot(createEntry("root:libraries", "Библиотеки", OPDS_EXT_LIB));
    }

    @RequestMapping(produces = APPLICATION_ATOM_XML)
    public ModelAndView getExtLibraries() {
        return createMav("Библиотеки", extLibService.getRoot(OPDS_EXT_LIB));
    }

    @SaveLatest
    @RequestMapping(value = "{id}", produces = APPLICATION_ATOM_XML)
    public ModelAndView getExtLibData(@PathVariable(value = "id") long id,
                                      @RequestParam(required = false) Map<String, String> requestParams) throws LibException {
        ExtLibFeed extLibFeed = extLibService.getDataForLibrary(OPDS_EXT_LIB, id, requestParams);
        return createMav(extLibFeed.getTitle(), extLibFeed.getEntries());
    }

    @RequestMapping(value = "{id}/action/{action}")
    @Secured(USER)
    public String actionExtLibData(@PathVariable(value = "id") long id,
                                   @PathVariable(value = "action") String action,
                                   @RequestParam(required = false) Map<String, String> requestParams) throws LibException {

        String redirect = extLibService.actionExtLibData(id, action, requestParams, userService.getCurrentUser());
        return "redirect:" + redirect;
    }


}
