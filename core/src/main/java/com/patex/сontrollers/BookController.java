package com.patex.сontrollers;

import com.patex.BookUploadInfo;
import com.patex.LibException;
import com.patex.entities.Book;
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
@RequestMapping("/book")
public class BookController {

    private static Logger log = LoggerFactory.getLogger(BookController.class);

    @Autowired
    BookService bookService;


    @RequestMapping(value = "/{id}" , method = RequestMethod.GET)
    public @ResponseBody Book getBook(@PathVariable(value = "id") long id) {
        return bookService.getBook(id);
    }

    @RequestMapping(method = RequestMethod.GET)
    public @ResponseBody
    Page<Book> getBooks(Pageable pageable) {
        return bookService.getBooks(pageable);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/upload")
    public @ResponseBody List<BookUploadInfo> handleFileUpload(@RequestParam("file") MultipartFile[] files)
            throws LibException, IOException {

        return Arrays.stream(files).map( file->{
                    try {
                        Book book = bookService.uploadBook(file.getOriginalFilename(), file.getInputStream());
                        return new BookUploadInfo(book.getId(),file.getOriginalFilename(), BookUploadInfo.Status.Success);
                    } catch (Exception e) {
                        log.error("unable to parse {}",file.getOriginalFilename());
                        log.warn(e.getMessage(),e);
                        return new BookUploadInfo(-1,file.getOriginalFilename(), BookUploadInfo.Status.Failed);
                    }
                }
        ).collect(Collectors.toList());
    }

    @RequestMapping(method = RequestMethod.POST)
    public @ResponseBody Book updateBook(@RequestBody Book book) throws LibException {
        return bookService.updateBook(book);
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