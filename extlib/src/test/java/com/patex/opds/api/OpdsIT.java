package com.patex.opds.api;

import com.patex.BookUploadInfo;
import com.patex.entities.ZUser;
import fb2Generator.Fb2Creator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;

/**
 *
 */
public class OpdsIT {

    private HttpTestClient httpClient;

    @Before
    public void setUp() throws IOException {
        httpClient = new HttpTestClient("http://localhost:8080");
        httpClient.setCreds("testUser","simplePassword");
        try {
            httpClient.get("user/current", ZUser.class);
        } catch (HttpClientErrorException e) {
            httpClient.setCreds(null,null);
            httpClient.post("user/create", "{\"username\":\"testUser\", \"password\":\"simplePassword\"}",
                    MediaType.APPLICATION_JSON, ZUser.class);
        }
        httpClient.setCreds("testUser","simplePassword");
    }

    @Test
    public void testLatestForAnonim() throws IOException {
        httpClient.setCreds(null, null);
        try {
            httpClient.get("opds/latest",String.class);
            Assert.fail("HttpClientErrorException should be thrown");
        } catch (HttpClientErrorException e) {
            Assert.assertThat(e.getStatusCode(),equalTo(HttpStatus.UNAUTHORIZED));
            //expected
        } catch (Exception e) {
            Assert.fail("HttpClientErrorException instead of "+e.getMessage()+"should be thrown");
        }

    }

    @Test
    public void testLatestForUser() throws IOException, InterruptedException {
        httpClient.setCreds("testUser","simplePassword");
        Map<String, InputStream> files = new HashMap<>();
        String firstName = RandomStringUtils.randomAlphanumeric(10);
        String middleName = RandomStringUtils.randomAlphanumeric(10);
        String lastName = RandomStringUtils.randomAlphanumeric(10);
        files.put(RandomStringUtils.randomAlphanumeric(10)+".fb2", new Fb2Creator(RandomStringUtils.randomAlphanumeric(10)).
                addAuthor(firstName, middleName, lastName).
                getFbook());
        httpClient.uploadFiles("book/upload",
                "file", files, new ParameterizedTypeReference<List<BookUploadInfo>>() {
                });

        String value = httpClient.get("opds/authorsindex/"+lastName, String.class);
        Thread.sleep(1000);
        try {
            String value2 = httpClient.get("opds/latest", String.class);
            Assert.assertThat(value2,containsString(lastName));
            Assert.assertEquals(value2,value);
        } catch (HttpClientErrorException e) {
            Assert.fail(e.getMessage());
        }

    }
}
