package com.patex.api;


import com.patex.BookUploadInfo;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;


public class UploadIT {

    private HttpTestClient httpClient;

    @Before
    public void setUp() {
        httpClient = new HttpTestClient("http://localhost:8080");
    }

    @Test
    public void uploadFile() throws IOException {
        Map<String, InputStream> files = new HashMap<>();
        String fileName = "parserTest.fb2";
        putBook(files, fileName);

        ResponseEntity<List<BookUploadInfo>> response = httpClient.uploadFiles("book/upload",
                "file",files, new ParameterizedTypeReference<List<BookUploadInfo>>() {
        });

        assertThat(response.getStatusCode(), Matchers.equalTo(HttpStatus.OK));
        assertThat(response.getBody(), hasSize(1));
        BookUploadInfo bookInfo = response.getBody().get(0);
        assertThat(bookInfo.getFileName(), Matchers.equalTo(fileName));
        assertThat(bookInfo.getStatus(), Matchers.equalTo(BookUploadInfo.Status.Success));
    }

    private void putBook(Map<String, InputStream> files, String fileName) {
        files.put(fileName,getClass().getResourceAsStream("/"+fileName));
    }

    @Test
    public void uploadFiles() throws IOException {

        Map<String, InputStream> files = new HashMap<>();
        putBook(files, "parserTest.fb2");
        putBook(files, "parserTest1.fb2");
        putBook(files, "parserTest2.fb2");
        putBook(files, "parserTest4.fb2");
        ResponseEntity<List<BookUploadInfo>> response = httpClient.uploadFiles("book/upload",
                "file",files, new ParameterizedTypeReference<List<BookUploadInfo>>() {
                });

        assertThat(response.getStatusCode(), Matchers.equalTo(HttpStatus.OK));
        assertThat(response.getBody(), hasSize(4));
        Assert.assertTrue(response.getBody().stream().
                allMatch(info ->info.getStatus().equals(BookUploadInfo.Status.Success) ));
            }

}

