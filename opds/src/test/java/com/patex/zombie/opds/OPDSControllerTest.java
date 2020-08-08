package com.patex.zombie.opds;

import com.patex.zombie.model.AggrResult;
import com.patex.zombie.service.AuthorService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;

import java.io.IOException;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {TestApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OPDSControllerTest {

    @LocalServerPort
    int randomServerPort;

    private HttpTestClient httpClient;

    @Autowired
    private AuthorService authorService;

    @BeforeEach
    public void setUp()  {
        httpClient = new HttpTestClient("http://localhost:" + randomServerPort);
    }

    @Test
    public void shouldHaveSpace() throws IOException {
        httpClient.get("/opds/authorsindex?prefix=testAuthor+", String.class);
        verify(authorService).getAuthorsCount("testAuthor ");
    }

    @Test
    public void shouldHavePlus() throws IOException {
        httpClient.get("/opds/authorsindex?prefix=testAuthor%2B", String.class);
        verify(authorService).getAuthorsCount("testAuthor+");
    }


    @Test
    public void shouldEncodeSpace() throws IOException {
        when(authorService.getAuthorsCount("testAuthor")).
                thenReturn(singletonList(new AggrResultTestImpl("testAuthor", 10)));

        String s = httpClient.get("/opds/authorsindex?prefix=testAuthor", String.class);

        assertTrue(s.contains("testAuthor+"));
    }


    @Test
    public void shouldEncodePlus() throws IOException {
        when(authorService.getAuthorsCount("testAuthor")).
                thenReturn(singletonList(new AggrResultTestImpl("testAuthor+", 10)));

        String s = httpClient.get("/opds/authorsindex?prefix=testAuthor", String.class);

        assertTrue(s.contains("testAuthor%2B"));
    }

    @Getter
    @RequiredArgsConstructor
    private static class AggrResultTestImpl implements AggrResult {
        private final String prefix;
        private final long result;

    }
}
