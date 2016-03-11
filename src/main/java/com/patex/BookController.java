package com.patex;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/book")
public class BookController {


    @RequestMapping(method=RequestMethod.GET)
    public @ResponseBody Book sayHello(@RequestParam(value="name", required=false, defaultValue="Stranger") String name) {
        return new Book("author", name);
    }

}