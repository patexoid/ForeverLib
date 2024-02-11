package com.patex.forever.opds;

import com.patex.forever.messaging.MessengerService;
import com.patex.forever.model.User;
import com.patex.forever.opds.entity.ExtLibraryRepository;
import com.patex.forever.opds.entity.SavedBookRepository;
import com.patex.forever.opds.entity.SubscriptionRepository;
import com.patex.forever.service.AuthorService;
import com.patex.forever.service.BookService;
import com.patex.forever.service.Resources;
import com.patex.forever.service.SequenceService;
import com.patex.forever.service.UserService;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class},
scanBasePackages = {"com.patex.forever.opds", "com.patex.forever.service"})
@EnableSpringDataWebSupport
public class TestApplication {

    @MockBean
    ExtLibraryRepository extLibraryRepository;

    @MockBean
    AuthorService authorService;

    @MockBean
    BookService bookService;

    @MockBean
    SequenceService sequenceService;



    @Bean
    @Primary
    public Resources res(){
        Resources resources= mock(Resources.class);
        when(resources.get(any(),any(),any())).then(invocationOnMock -> invocationOnMock.getArguments()[1]);
        return resources;
    }

    @Bean
    public UserService userService(){
        UserService userService=mock(UserService.class);
        User user = new User("current","pass",null);
        when(userService.getCurrentUser()).thenReturn(user);
        return userService;

    };

    @MockBean
    SavedBookRepository savedBookRepository;

    @MockBean
    MessengerService messengerService;

    @MockBean
    SubscriptionRepository subscriptionRepository;
}
