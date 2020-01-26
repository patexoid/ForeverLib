package com.patex.opds.controller;

import com.patex.zombie.core.entities.ExtLibrary;
import com.patex.zombie.core.service.ExtLibraryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/extLibrary")
public class ExtLibraryController {

    private final ExtLibraryService service;

    @Autowired
    public ExtLibraryController(ExtLibraryService service) {
        this.service = service;
    }

    @RequestMapping(method = RequestMethod.GET)
    public
    @ResponseBody
    Page<ExtLibrary> getAuthors(Pageable pageable) {
        return service.findAll(pageable);
    }

    public ExtLibrary save(ExtLibrary entity) {
        return service.save(entity);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public
    @ResponseBody ExtLibrary findOne(Long id) {
        return service.findOne(id);
    }
}
