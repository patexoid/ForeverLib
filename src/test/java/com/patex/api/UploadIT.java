package com.patex.api;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;


public class UploadIT {

    private HttpTestClient httpClient;

    @Before
    public void setUo() {
        httpClient = new HttpTestClient("http://localhost:8080");
    }

    @Test
    public void uploadFile() throws IOException {

        HttpEntity entity = MultipartEntityBuilder.create().addBinaryBody("parserTest.fb2",
                getClass().getResourceAsStream("/parserTest.fb2")).
                build();
        HttpResponse response = httpClient.makePost("book/upload", entity);

        Assert.assertThat(response.getStatusLine().getStatusCode(), Matchers.equalTo(200));

    }
}

