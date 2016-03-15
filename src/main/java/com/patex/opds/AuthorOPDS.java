package com.patex.opds;

import com.patex.service.AuthorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Created by potekhio on 15-Mar-16.
 */
@Controller
@RequestMapping("opds/author")
public class AuthorOPDS {

  @Autowired
  AuthorService authorService;

  @RequestMapping(value="/{id}", produces ="application/atom+xml")
  public ModelAndView getAuthor(@PathVariable("id") long authorID) {
    ModelAndView mav = new ModelAndView();
    mav.setViewName(AuthorOpdsView.OPDS_AUTHOR_VIEW);
    mav.addObject("author",authorService.getAuthor(authorID));
    return mav;
  }

  @RequestMapping(value="search/{start}", produces ="application/atom+xml")
  public ModelAndView getAuthors(@PathVariable("start") String start) {
    ModelAndView mav = new ModelAndView();
    mav.setViewName(AuthorsOpdsView.OPDS_AUTHORS_VIEW);
    mav.addObject(AuthorsOpdsView.LIST,authorService.findByName(start, new PageRequest(0,20)).getContent());
    return mav;
  }
}
