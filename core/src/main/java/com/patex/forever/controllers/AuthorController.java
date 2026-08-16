package com.patex.forever.controllers;

import com.patex.forever.model.Author;
import com.patex.forever.service.AuthorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import static com.patex.forever.service.UserService.ADMIN_AUTHORITY;

@Controller
@RequestMapping("/author")
public class AuthorController {

    private static final Logger log = LoggerFactory.getLogger(AuthorController.class);

    @Autowired
    private AuthorService authorService;


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

    @RequestMapping(value = "/merge", method = RequestMethod.POST)
    @Secured(ADMIN_AUTHORITY)
    @ResponseBody
    public Author mergeAuthors(@AuthenticationPrincipal UserDetails user, @RequestParam("id") Long... ids) {
        return authorService.mergeAuthors(user, ids);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    @Secured(ADMIN_AUTHORITY)
    public
    @ResponseBody
    Author updateAuthor(@PathVariable(value = "id") long id, @RequestBody Author author) {
        return authorService.updateAuthor(id, author);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @Secured(ADMIN_AUTHORITY)
    public
    @ResponseBody
    void deleteAuthor(@PathVariable(value = "id") long id) {
        authorService.deleteAuthor(id);
    }
}
