package com.patex.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(name = "data-storage")
@Profile("feignStorage")
public interface DataStorage {

    @RequestMapping(value = {"/data/upload/{path}"}, consumes = {"multipart/form-data"})
    String upload(@PathVariable String path, @RequestPart(name = "file") MultipartFile file);
}
