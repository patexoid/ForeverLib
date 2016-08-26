package com.patex.api;


import com.patex.BookUploadInfo;
import org.hamcrest.Matchers;
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
        String bookFileName = "parserTest.fb2";
        files.put(bookFileName,
                getClass().getResourceAsStream("/parserTest.fb2"));
        ResponseEntity<List<BookUploadInfo>> response = httpClient.uploadFiles("book/upload", "file",files, new ParameterizedTypeReference<List<BookUploadInfo>>() {
        });

        assertThat(response.getStatusCode(), Matchers.equalTo(HttpStatus.OK));
        assertThat(response.getBody(), hasSize(1));
        BookUploadInfo bookInfo = response.getBody().get(0);
        assertThat(bookInfo.getFileName(), Matchers.equalTo(bookFileName));
        assertThat(bookInfo.getStatus(), Matchers.equalTo(BookUploadInfo.Status.Success));
    }
}

