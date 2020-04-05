package com.patex.controllers;

import com.patex.service.AuthorServiceImpl;
import com.patex.zombie.model.Author;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/author")
public class AuthorController {

    private static final Logger log = LoggerFactory.getLogger(AuthorController.class);

    @Autowired
    private AuthorServiceImpl authorService;


    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public
    @ResponseBody
    Author getAuthor(@PathVariable(value = "id") long id) {
        return authorService.getAuthor(id);
    }

    @RequestMapping(method = RequestMethod.GET)
    public
    @ResponseBody
    Page<Author> getAuthors(Pageable pageable, @RequestParam(required = false) String prefix) {
        log.trace("prefix:{} page:{} of {}", prefix, pageable.getPageNumber(), pageable.getPageSize());
        return authorService.getAuthor(pageable, prefix);
    }

}
