package com.patex.controllers;

import com.patex.BookUploadInfo;
import com.patex.LibException;
import com.patex.entities.BookEntity;
import com.patex.service.AdminService;
import com.patex.service.BookService;
import com.patex.service.DuplicateHandler;
import com.patex.service.ZUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.patex.service.ZUserService.ADMIN_AUTHORITY;
import static com.patex.service.ZUserService.USER;

@Controller
@RequestMapping("/book")
public class BookController {

   private static final Logger log = LoggerFactory.getLogger(BookController.class);

    @Autowired
    private BookService bookService;

    @Autowired
    private AdminService adminService;


    @Autowired
    private DuplicateHandler duplicateHandler;

    @Autowired
    private ZUserService userService;

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public @ResponseBody
    BookEntity getBook(@PathVariable(value = "id") long id) {
        return bookService.getBook(id);
    }

    @RequestMapping(method = RequestMethod.GET)
    public @ResponseBody
    Page<BookEntity> getBooks(Pageable pageable) {
        return bookService.getBooks(pageable);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/upload")
    @Secured(USER)
    public @ResponseBody
    List<BookUploadInfo> handleFileUpload(@RequestParam("file") MultipartFile[] files)
            throws LibException {

        return Arrays.stream(files).map(file -> {
                    try {
                        BookEntity book = bookService.uploadBook(file.getOriginalFilename(), file.getInputStream(),
                                userService.getCurrentUser());
                        return new BookUploadInfo(book.getId(), file.getOriginalFilename(), BookUploadInfo.Status.Success);
                    } catch (AccessDeniedException e) {
                        throw e;
                    } catch (Exception e) {
                        log.error("unable to parse {}", file.getOriginalFilename());
                        log.warn(e.getMessage(), e);
                        return new BookUploadInfo(-1, file.getOriginalFilename(), BookUploadInfo.Status.Failed);
                    }
                }
        ).collect(Collectors.toList());
    }

    @RequestMapping(method = RequestMethod.POST)
    public @ResponseBody
    BookEntity updateBook(@RequestBody BookEntity book) throws LibException {
        return bookService.updateBook(book);
    }

    @RequestMapping(value = "/loadFile/{id}", method = RequestMethod.GET)
    public ResponseEntity<InputStreamResource> downloadBook(@PathVariable("id") int bookId) throws LibException {

        BookEntity book = bookService.getBook(bookId);
        InputStream inputStream = bookService.getBookInputStream(book);
        HttpHeaders respHeaders = new HttpHeaders();
        respHeaders.setContentLength(book.getFileResource().getSize());
        respHeaders.setContentDispositionFormData("attachment", book.getFileName());
        InputStreamResource isr = new InputStreamResource(inputStream);
        return new ResponseEntity<>(isr, respHeaders, HttpStatus.OK);
    }

    @RequestMapping(value = "/cover/{id}", method = RequestMethod.GET)
    public ResponseEntity<InputStreamResource> getCover(@PathVariable("id") int bookId) throws LibException {
        BookEntity book = bookService.getBook(bookId);
        if(book.getCover()!=null) {
            InputStream inputStream = bookService.getBookCoverInputStream(book);
            HttpHeaders respHeaders = new HttpHeaders();
            respHeaders.setContentLength(book.getCover().getSize());
            respHeaders.add("Content-Type", book.getCover().getType());
            InputStreamResource isr = new InputStreamResource(inputStream);
            return new ResponseEntity<>(isr, respHeaders, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(value = "/waitForDuplicateCheck", method = RequestMethod.GET)
    @Secured(ADMIN_AUTHORITY)
    public @ResponseBody
    String waitForDuplicateCheck() {
        duplicateHandler.waitForFinish();
        return "success";
    }

    @RequestMapping(value = "/duplicateCheckForExisted", method = RequestMethod.GET)
    @Secured(ADMIN_AUTHORITY)
    public @ResponseBody
    String duplicateCheckForExisted() {
        adminService.publisEventForExistingBooks(userService.getCurrentUser());
        duplicateHandler.waitForFinish();
        return "success";
    }

    @RequestMapping(value = "/duplicateCheckForAuthor/{authorId}", method = RequestMethod.GET)
    @Secured(ADMIN_AUTHORITY)
    public @ResponseBody
    String duplicateCheckForAuthorExisted(@PathVariable("authorId") Long authorId) {
        adminService.checkDuplicatesForAuthor(userService.getCurrentUser(), authorId);
        duplicateHandler.waitForFinish();
        return "success";
    }


    @RequestMapping(value = "/updateCovers", method = RequestMethod.GET)
    @Secured(ADMIN_AUTHORITY)
    public @ResponseBody
    String updateCovers() {
        adminService.updateCovers();
        return "success";
    }

}