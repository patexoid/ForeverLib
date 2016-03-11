package com.patex;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/book")
public class BookController {


    @RequestMapping(method=RequestMethod.GET)
    public @ResponseBody Book sayHello(@RequestParam(value="name", required=false, defaultValue="Stranger") String name) {
        return new Book("author", name);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/upload")
    public String handleFileUpload(
                                   @RequestParam("file") MultipartFile file,
                                   RedirectAttributes redirectAttributes) {


        if (!file.isEmpty()) {
            try {

                redirectAttributes.addFlashAttribute("message",
                        "You successfully uploaded "  + "!");
            }
            catch (Exception e) {
                redirectAttributes.addFlashAttribute("message",
                        "You failed to upload "  + " => " + e.getMessage());
            }
        }
        else {
            redirectAttributes.addFlashAttribute("message",
                    "You failed to upload " + " because the file was empty");
        }

        return "redirect:";
    }
}