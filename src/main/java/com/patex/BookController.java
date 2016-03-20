package com.patex;

import com.patex.entities.Book;
import com.patex.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Controller
@RequestMapping("/book")
public class BookController {

    @Autowired
    BookService bookService;


    @RequestMapping(value = "/{id}" , method = RequestMethod.GET)
    public
    @ResponseBody
    Book getAuthor(@PathVariable(value = "id") long id) {
        return bookService.getBook(id);
    }


    @RequestMapping(method = RequestMethod.POST, value = "/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile[] files) throws LibException, IOException {
        for (MultipartFile file : files) {
            Book book = bookService.uploadBook(file.getOriginalFilename(), file.getInputStream());
        }
        return "redirect:1";
    }

    @RequestMapping(value = "/loadFile/{id}", method = RequestMethod.GET)
    public ResponseEntity<InputStreamResource> downloadBook(@PathVariable("id") int bookId) throws LibException{

        Book book = bookService.getBook(bookId);
        InputStream inputStream= bookService.getBookInputStream(book);
        HttpHeaders respHeaders = new HttpHeaders();
        respHeaders.setContentLength(bookId);
        respHeaders.setContentDispositionFormData("attachment", book.getFileName());
        InputStreamResource isr = new InputStreamResource(inputStream);
        return new ResponseEntity<>(isr, respHeaders, HttpStatus.OK);
    }
}