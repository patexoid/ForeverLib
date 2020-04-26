package com.patex.zombie.opds;

import com.patex.zombie.model.BookUploadInfo;
import com.patex.zombie.model.User;
import fb2Generator.Fb2Creator;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.event.annotation.BeforeTestClass;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 *
 */
public class OpdsIT {

    private HttpTestClient httpClient;

    @BeforeTestClass
    public void setUp() throws IOException {
        httpClient = new HttpTestClient("http://localhost:8080");
        httpClient.setCreds("testUser","simplePassword");
        try {
            httpClient.get("user/current", User.class);
        } catch (HttpClientErrorException e) {
            httpClient.setCreds(null,null);
            httpClient.post("user/create", "{\"username\":\"testUser\", \"password\":\"simplePassword\"}",
                    MediaType.APPLICATION_JSON, User.class);
        }
        httpClient.setCreds("testUser","simplePassword");
    }

    @Test
    public void testLatestForAnonim() throws IOException {
        httpClient.setCreds(null, null);
        try {
            httpClient.get("opds/latest",String.class);
            fail("HttpClientErrorException should be thrown");
        } catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode(),equalTo(HttpStatus.UNAUTHORIZED));
            //expected
        } catch (Exception e) {
            fail("HttpClientErrorException instead of "+e.getMessage()+"should be thrown");
        }

    }

    @Test
    public void testLatestForUser() throws IOException, InterruptedException {
        httpClient.setCreds("testUser","simplePassword");
        Map<String, InputStream> files = new HashMap<>();
        String firstName = randomAlphanumeric(10);
        String middleName = randomAlphanumeric(10);
        String lastName = randomAlphanumeric(10);
        files.put(randomAlphanumeric(10)+".fb2", new Fb2Creator(randomAlphanumeric(10)).
                addAuthor(firstName, middleName, lastName).
                getFbook());
        httpClient.uploadFiles("book/upload",
                "file", files, new ParameterizedTypeReference<List<BookUploadInfo>>() {
                });

        String value = httpClient.get("opds/authorsindex/"+lastName, String.class);
        Thread.sleep(1000);
        try {
            String value2 = httpClient.get("opds/latest", String.class);
            assertThat(value2,containsString(lastName));
            assertEquals(value2,value);
        } catch (HttpClientErrorException e) {
            fail(e.getMessage());
        }

    }
}
