package com.patex.forever.service;

import com.patex.forever.Application;
import com.patex.forever.entities.AuthorEntity;
import com.patex.forever.entities.AuthorRepository;
import com.patex.forever.messaging.TelegramMessenger;
import com.patex.forever.model.AggrResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
        classes = Application.class)
@ExtendWith(SpringExtension.class)
@Disabled
public class AuthorServiceImplTest {

    @MockBean
    StorageService storageService;
    @MockBean
    DirWatcherService dirWatcherService;
    @MockBean
    TelegramMessenger telegramMessenger;
    @MockBean
    RabbitDuplicateHandler rabbitDuplicateHandler;
    @Autowired
    private AuthorServiceImpl authorService;
    @Autowired
    private AuthorRepository controller;

    @AfterEach
    public void tearDown() throws Exception {
        controller.deleteAll();
    }

    @Test
    public void shouldReturnLatestAuthorsCount1() {
        controller.save(new AuthorEntity("abcd1"));
        List<AggrResult> authorsCount = authorService.getAuthorsCount("","ua");
        assertEquals("abcd1",authorsCount.get(0).getPrefix());
        assertEquals(1,authorsCount.get(0).getResult());
    }
    @Test
    public void shouldReturnLatestAuthorsCount2() {
        controller.save(new AuthorEntity("abcd1"));
        controller.save(new AuthorEntity("abcd2"));
        controller.save(new AuthorEntity("abcd3"));
        controller.save(new AuthorEntity("abcdd4"));
        controller.save(new AuthorEntity("abcdd6"));
        controller.save(new AuthorEntity("abcdd7"));
        List<AggrResult> authorsCount = authorService.getAuthorsCount("","ua");
        authorsCount.forEach(aggrResult -> System.out.println(aggrResult.getPrefix() + " " + aggrResult.getResult()));
        assertEquals("abcd1",authorsCount.get(0).getPrefix());
        assertEquals(1,authorsCount.get(0).getResult());
        assertEquals("abcd2",authorsCount.get(1).getPrefix());
        assertEquals(1,authorsCount.get(1).getResult());
        assertEquals("abcd3",authorsCount.get(2).getPrefix());
        assertEquals(1,authorsCount.get(2).getResult());
        assertEquals("abcdd",authorsCount.get(3).getPrefix());
        assertEquals(3,authorsCount.get(3).getResult());
    }
}
