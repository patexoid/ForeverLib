package com.patex.сontrollers;

import com.patex.entities.Author;
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

    private static Logger log = LoggerFactory.getLogger(AuthorController.class);

    @Autowired
    AuthorService authorService;


    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public
    @ResponseBody
    Author getAuthor(@PathVariable(value = "id") long id) {
        return authorService.getAuthors(id);
    }

    @RequestMapping(method = RequestMethod.GET)
    public
    @ResponseBody
    Page<Author> getAuthors(Pageable pageable, @RequestParam(required = false) String prefix) {
        return authorService.getAuthors(pageable, prefix);
    }

}
