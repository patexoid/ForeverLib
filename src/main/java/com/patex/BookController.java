package com.patex;

import com.patex.entities.Book;
import com.patex.service.BookService;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/book")
public class BookController {

    @Autowired
    BookService bookService;


    @RequestMapping(value = "/{id}" , method = RequestMethod.GET)
    public @ResponseBody Book getBook(@PathVariable(value = "id") long id) {
        return bookService.getBook(id);
    }

    @RequestMapping(method = RequestMethod.GET)
    public @ResponseBody
    Page<Book> getBookS(Pageable pageable) {
        return bookService.getBooks(pageable);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/upload")
    public @ResponseBody List<BookUploadInfo> handleFileUpload(@RequestParam("file") MultipartFile[] files)
            throws LibException, IOException {

        return Arrays.stream(files).map( file->{
                    try {
                        bookService.uploadBook(file.getOriginalFilename(), file.getInputStream());
                        return new BookUploadInfo(file.getOriginalFilename(), BookUploadInfo.Status.Success);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return new BookUploadInfo(file.getOriginalFilename(), BookUploadInfo.Status.Failed);
                    }
        }
        ).collect(Collectors.toList());
    }

    @RequestMapping(value = "/loadFile/{id}", method = RequestMethod.GET)
    public ResponseEntity<InputStreamResource> downloadBook(@PathVariable("id") int bookId) throws LibException{

        Book book = bookService.getBook(bookId);
        InputStream inputStream= bookService.getBookInputStream(book);
        HttpHeaders respHeaders = new HttpHeaders();
        respHeaders.setContentLength(book.getSize() );
        respHeaders.setContentDispositionFormData("attachment", book.getFileName());
        InputStreamResource isr = new InputStreamResource(inputStream);
        return new ResponseEntity<>(isr, respHeaders, HttpStatus.OK);
    }
}