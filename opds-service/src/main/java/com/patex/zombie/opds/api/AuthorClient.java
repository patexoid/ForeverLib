package com.patex.zombie.opds.api;

import com.patex.model.AggrResult;
import com.patex.model.Author;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@FeignClient(name = "core")
@RequestMapping("/author")
public interface AuthorClient {

    @RequestMapping(value = "/count/{prefix}", method = RequestMethod.GET)
    List<AggrResult> getCount(String prefix);

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    Author getCount(long id);

    @RequestMapping(method = RequestMethod.GET)
    Page<Author> getAuthors(@RequestParam(required = false) String prefix);

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    Author getAuthor(long id);
}
