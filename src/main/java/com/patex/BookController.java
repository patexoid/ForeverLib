package com.patex;

import com.patex.entities.Author;
import com.patex.entities.Book;
import com.patex.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.websocket.server.PathParam;
import java.io.IOException;

@Controller
@RequestMapping("/book")
public class BookController {

    @Autowired
    BookService bookService;

    @RequestMapping(method = RequestMethod.GET)
    public
    @ResponseBody
    Book sayHello(@RequestParam(value = "name", required = false, defaultValue = "Stranger") String name) {
        return new Book(new Author("author"), name);
    }

    @RequestMapping(value = "/{id}" , method = RequestMethod.GET)
    public
    @ResponseBody
    Book sayHello(@PathVariable(value = "id") long id) {
        return bookService.getBook(id);
    }


    @RequestMapping(method = RequestMethod.POST, value = "/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile[] files) throws LibException, IOException {
        for (MultipartFile file : files) {
            Book book = bookService.saveBook(file.getOriginalFilename(), file.getInputStream());
        }
        return "redirect:1";
    }
}