package com.patex.controllers;

import com.patex.LibException;
import com.patex.extlib.ExtLibFeed;
import com.patex.extlib.ExtLibService;
import com.patex.extlib.LinkMapper;
import com.patex.opds.OPDSEntryBuilder;
import com.patex.opds.RootProvider;
import com.patex.opds.converters.OPDSEntry;
import com.patex.opds.converters.OPDSLink;
import com.patex.opds.latest.SaveLatest;
import com.patex.service.Resources;
import com.patex.service.ZUserService;
import com.patex.utils.LinkUtils;
import com.patex.utils.Res;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.patex.controllers.OPDSController.*;
import static com.patex.service.ZUserService.*;

/**
 *
 */
@Controller
@RequestMapping(OPDSExtLibController.OPDS_EXT_LIB)
public class OPDSExtLibController implements RootProvider {

    private static final String EXT_LIB = "extLib";
    static final String OPDS_EXT_LIB = "/" + PREFIX + "/" + EXT_LIB;


    private final List<OPDSEntry> rootEntries = Collections.singletonList(
            new OPDSEntryBuilder("root:libraries", null, new Res("opds.extlib.libraries")).
                    addLink(OPDS_EXT_LIB, OPDSLink.OPDS_CATALOG).build());

    @Autowired
    private ExtLibService extLibService;

    @Autowired
    private OPDSController opdsController;

    @Autowired
    private ZUserService userService;

    @Autowired
    private Resources resources;

    @PostConstruct
    public void setUp() {
        opdsController.addRootPrivider(this);
    }

    @Override
    public List<OPDSEntry> getRoot() {
        return rootEntries;
    }

    @RequestMapping(produces = APPLICATION_ATOM_XML)
    public ModelAndView getExtLibraries() {
        return createMav(new Res("opds.extlib.libraries"), extLibService.getRoot(OPDS_EXT_LIB));
    }

    @SaveLatest
    @RequestMapping(value = "{id}", produces = APPLICATION_ATOM_XML)
    public ModelAndView getExtLibData(@PathVariable(value = "id") long id,
                                      @RequestParam(required = false) Map<String, String> requestParams) throws LibException {
        ExtLibFeed extLibFeed = extLibService.getDataForLibrary(id, requestParams);
        extLibFeed = extLibFeed.updateWithPrefix(OPDS_EXT_LIB + "/" + id + "/");
        return createMav(new Res("first.value", extLibFeed.getTitle()), extLibFeed.getEntries());
    }

    @RequestMapping(value = "{id}/download")
    @Secured(USER)
    public String downloadBook(@PathVariable(value = "id") long id,
                               @RequestParam(name = ExtLibService.REQUEST_P_NAME) String url,
                               @RequestParam(name = ExtLibService.PARAM_TYPE) String type)
            throws LibException {

        String redirect = extLibService.downloadBook(id, url, type);
        return "redirect:" + redirect;
    }


    @RequestMapping(value = "{id}/action/{action}")
    @Secured(USER)
    public String actionExtLibData(@PathVariable(value = "id") long id,
                                   @PathVariable(value = "action") String action,
                                   @RequestParam Map<String, String> requestParams,
                                   @RequestParam("uri") String uri)
            throws LibException {
        extLibService.actionExtLibData(id, action, requestParams);
        //referer header is not supported by some clients make redirect url manually
        return "redirect:" + LinkUtils.makeURL(OPDS_EXT_LIB, id, LinkMapper.mapToUri("?", uri));
    }

    @RequestMapping(value = "runSubcriptionTask")
    @Secured(ADMIN_AUTHORITY)
    public @ResponseBody
    String runSubcriptionTask() throws LibException {
        extLibService.checkSubscriptions();
        return resources.get(userService.getUserLocale(), "opds.extlib.subscription.task.in.progress");
    }
}