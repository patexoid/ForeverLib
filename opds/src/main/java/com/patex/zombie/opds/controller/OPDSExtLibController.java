package com.patex.zombie.opds.controller;

import com.patex.zombie.LibException;
import com.patex.zombie.LinkUtils;
import com.patex.zombie.model.Res;
import com.patex.zombie.opds.model.ExtLibFeed;
import com.patex.zombie.opds.service.ExtLibService;
import com.patex.zombie.opds.model.converter.LinkMapper;
import com.patex.zombie.opds.model.OPDSEntryBuilder;
import com.patex.zombie.opds.model.OPDSEntry;
import com.patex.zombie.opds.model.OPDSLink;
import com.patex.zombie.opds.controller.latest.SaveLatest;
import com.patex.zombie.service.Resources;
import com.patex.zombie.service.UserService;
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

import static com.patex.zombie.opds.controller.OPDSController.*;

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
    private UserService userService;

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
    @Secured(UserService.USER)
    public String downloadBook(@PathVariable(value = "id") long id,
                               @RequestParam(name = ExtLibService.REQUEST_P_NAME) String url,
                               @RequestParam(name = ExtLibService.PARAM_TYPE) String type)
            throws LibException {

        String redirect = extLibService.downloadBook(id, url, type);
        return "redirect:" + redirect;
    }


    @RequestMapping(value = "{id}/action/{action}")
    @Secured(UserService.USER)
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
    @Secured(UserService.ADMIN_AUTHORITY)
    public @ResponseBody
    String runSubcriptionTask() throws LibException {
        extLibService.checkSubscriptions();
        return resources.get(userService.getUserLocale(), "opds.extlib.subscription.task.in.progress");
    }
}