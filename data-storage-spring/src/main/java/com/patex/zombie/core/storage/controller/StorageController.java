package com.patex.zombie.core.storage.controller;

import com.patex.LibException;
import com.patex.jwt.JwtTokenUtil;
import com.patex.model.UploadResponse;
import com.patex.zombie.core.storage.service.DataHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/data")
@RequiredArgsConstructor
@Slf4j
public class StorageController {

    private final DataHandler dataHandler;

    @RequestMapping(method = RequestMethod.POST, value = "/upload/{bucket}")
    @Secured(JwtTokenUtil.FILE_UPLOAD)
    public @ResponseBody
    List<UploadResponse> handleFileUpload(@PathVariable String bucket,
                                          @RequestParam("file") MultipartFile[] files) throws LibException {

        return Arrays.stream(files).map(file -> {
                    try {
                        return UploadResponse.success(dataHandler.save(file, bucket));
                    } catch (LibException e) {
                        log.error(e.getMessage(), e);
                        return UploadResponse.error(e.getMessage());
                    }
                }
        ).collect(Collectors.toList());
    }

    @RequestMapping(value = "/download/{bucket}/{id}", method = RequestMethod.GET)
    @Secured(JwtTokenUtil.FILE_DOWNLOAD)
    public ResponseEntity<InputStreamResource> downloadBook(@PathVariable String bucket,
                                                            @PathVariable String id) throws LibException {
        return dataHandler.load(bucket, id).map(fileData -> {
            HttpHeaders respHeaders = new HttpHeaders();
            respHeaders.setContentLength(fileData.getSize());
            respHeaders.setContentDispositionFormData("attachment", id);
            InputStreamResource isr = new InputStreamResource(fileData.getInputStream());
            return new ResponseEntity<>(isr, respHeaders, HttpStatus.OK);
        }).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
