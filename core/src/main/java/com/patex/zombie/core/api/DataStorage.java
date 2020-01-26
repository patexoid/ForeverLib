package com.patex.zombie.core.api;

import com.patex.model.UploadResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@FeignClient(name = "data-storage")
@Profile("feignStorage")
public interface DataStorage {

    @RequestMapping(value = {"/data/upload/{path}"}, consumes = {"multipart/form-data"}, method = RequestMethod.POST)
    List<UploadResponse> upload(@RequestHeader(HttpHeaders.AUTHORIZATION) String header,
                                @PathVariable String path, @RequestPart(name = "file") MultipartFile file);

    @RequestMapping(value = "/data/download/{bucket}/{id}", method = RequestMethod.GET)
    ResponseEntity<InputStreamResource> downloadBook(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String header,
            @PathVariable String bucket, @PathVariable String id);
}
