package com.patex.opds.controller;

import com.patex.LibException;
import com.patex.jwt.JwtTokenUtil;
import com.patex.opds.OPDSEntry;
import com.patex.opds.OPDSLink;
import com.patex.opds.controller.latest.SaveLatest;
import com.patex.opds.extlib.ExtLibFeed;
import com.patex.opds.extlib.ExtLibService;
import com.patex.opds.extlib.LinkMapper;
import com.patex.opds.service.UserService;
import com.patex.utils.LinkUtils;
import com.patex.utils.Res;
import com.patex.utils.Resources;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collections;
import java.util.List;
import java.util.Map;


/**
 *
 */
@Controller
@RequestMapping(OPDSExtLibController.OPDS_EXT_LIB)
public class OPDSExtLibController {
    static final String APPLICATION_ATOM_XML = "application/atom+xml;charset=UTF-8";
    private static final String EXT_LIB = "extLib";
    static final String OPDS_EXT_LIB = EXT_LIB;


    private final List<OPDSEntry> rootEntries = Collections.singletonList(
            OPDSEntry.builder("root:libraries", "opds.extlib.libraries").
                    addLink(OPDS_EXT_LIB, OPDSLink.OPDS_CATALOG).build());

    private ExtLibService extLibService;

    private UserService userService;

    private Resources resources;

    public OPDSExtLibController(ExtLibService extLibService,
                                UserService userService, Resources resources) {
        this.extLibService = extLibService;
        this.userService = userService;
        this.resources = resources;
    }

    @RequestMapping(produces = APPLICATION_ATOM_XML)
    public List<OPDSEntry> getExtLibraries() {
//        return createMav(new Res("opds.extlib.libraries"), extLibService.getRoot(OPDS_EXT_LIB));
        return  extLibService.getRoot(OPDS_EXT_LIB);
    }

    @SaveLatest
    @RequestMapping(value = "{id}", produces = APPLICATION_ATOM_XML)
    public  ExtLibFeed getExtLibData(@PathVariable(value = "id") long id,
                                      @RequestParam(required = false) Map<String, String> requestParams) throws LibException {
        ExtLibFeed extLibFeed = extLibService.getDataForLibrary(id, requestParams);
        extLibFeed = extLibFeed.updateWithPrefix(OPDS_EXT_LIB + "/" + id + "/");
        return extLibFeed;
//        return createMav(new Res("first.value", extLibFeed.getTitle()), extLibFeed.getEntries());
    }

    @RequestMapping(value = "{id}/download")
    @Secured(JwtTokenUtil.USER)
    public String downloadBook(@PathVariable(value = "id") long id,
                               @RequestParam(name = ExtLibService.REQUEST_P_NAME) String url,
                               @RequestParam(name = ExtLibService.PARAM_TYPE) String type)
            throws LibException {

        String redirect = extLibService.downloadBook(id, url, type);
        return "redirect:" + redirect;
    }


    @RequestMapping(value = "{id}/action/{action}")
    @Secured(JwtTokenUtil.USER)
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
    @Secured(JwtTokenUtil.ADMIN_AUTHORITY)
    public @ResponseBody
    String runSubcriptionTask() throws LibException {
        extLibService.checkSubscriptions();
        return resources.get(userService.getUserLocale(null), "opds.extlib.subscription.task.in.progress");
    }
}