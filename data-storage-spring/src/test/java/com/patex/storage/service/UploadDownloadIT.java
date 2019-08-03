package com.patex.storage.service;

import com.patex.storage.Application;
import com.patex.storage.model.UploadResponse;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Application.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class UploadDownloadIT {

    @LocalServerPort
    int randomPort;

    @Test
    public void shouldDownloadUpload() throws Exception {
        HttpTestClient testClient=new HttpTestClient("http://localhost:"+randomPort);
        byte[] data = {1, 2, 3};
        Map<String, InputStream> files = Map.of("testFileName.txt", new ByteArrayInputStream(data));
        ResponseEntity<List<UploadResponse>> uploadResponseEntity =
                testClient.uploadFiles("data/upload/test", "file", files, new ParameterizedTypeReference<>() {
        });
        assertEquals(HttpStatus.OK, uploadResponseEntity.getStatusCode());
        UploadResponse uploadResponse = uploadResponseEntity.getBody().get(0);
        assertEquals(UploadResponse.Status.Success, uploadResponse.getStatus());
        assertEquals("testFileName.txt",uploadResponse.getId());

        ResponseEntity<byte[]> downloadResponseEntity = testClient.get("/data/download/test/testFileName.txt",
                null, ParameterizedTypeReference.forType(byte[].class));
        assertEquals(HttpStatus.OK, downloadResponseEntity.getStatusCode());
        assertArrayEquals(data,downloadResponseEntity.getBody());
    }
}
