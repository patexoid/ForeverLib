package com.patex.controllers;

import com.patex.entities.AuthorEntity;
import com.patex.service.AuthorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/author")
public class AuthorController {

   private static final Logger log = LoggerFactory.getLogger(AuthorController.class);

    @Autowired
    private AuthorService authorService;


    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public
    @ResponseBody
    AuthorEntity getAuthor(@PathVariable(value = "id") long id) {
        return authorService.getAuthor(id);
    }

    @RequestMapping(method = RequestMethod.GET)
    public
    @ResponseBody
    Page<AuthorEntity> getAuthors(Pageable pageable, @RequestParam(required = false) String prefix) {
        log.trace("prefix:{} page:{} of {}",prefix,pageable.getPageNumber(),pageable.getPageSize());
        return authorService.getAuthor(pageable, prefix);
    }

}
