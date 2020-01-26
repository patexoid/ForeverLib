package com.patex.zombie.core.controllers;

import com.patex.zombie.core.mapper.AuthorMapper;
import com.patex.model.AggrResult;
import com.patex.model.Author;
import com.patex.zombie.core.service.AuthorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/author")
public class AuthorController {

    private static final Logger log = LoggerFactory.getLogger(AuthorController.class);

    private final AuthorService authorService;

    private final AuthorMapper mapper;

    public AuthorController(AuthorService authorService, AuthorMapper mapper) {
        this.authorService = authorService;
        this.mapper = mapper;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public
    @ResponseBody
    Author getAuthor(@PathVariable(value = "id") long id) {
        return mapper.toDto(authorService.getAuthor(id));
    }

    @RequestMapping(method = RequestMethod.GET)
    public
    @ResponseBody
    Page<Author> getAuthors(Pageable pageable, @RequestParam(required = false) String prefix) {
        log.trace("prefix:{} page:{} of {}", prefix, pageable.getPageNumber(), pageable.getPageSize());
        return authorService.getAuthor(pageable, prefix).map(mapper::toDto);
    }

    @RequestMapping(value = "/count/{prefix}", method = RequestMethod.GET)
    public @ResponseBody
    List<AggrResult> getCount(@PathVariable String prefix) {
        return authorService.getAuthorsCount(prefix);
    }

}
