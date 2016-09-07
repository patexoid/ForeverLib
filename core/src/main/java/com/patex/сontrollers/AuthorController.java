package com.patex.сontrollers;

import com.patex.BookUploadInfo;
import com.patex.LibException;
import com.patex.entities.Author;
import com.patex.entities.Book;
import com.patex.service.AuthorService;
import com.patex.service.BookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/author")
public class AuthorController {

    private static Logger log = LoggerFactory.getLogger(AuthorController.class);

    @Autowired
    AuthorService authorService;


    @RequestMapping(value = "/{id}" , method = RequestMethod.GET)
    public @ResponseBody
    Author getAuthor(@PathVariable(value = "id") long id) {
        return authorService.getAuthor(id);
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(method = RequestMethod.GET)
    public @ResponseBody
    Page<Author> getAuthors(Pageable pageable) {
        return authorService.getAuthor(pageable);
    }

}