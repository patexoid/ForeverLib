package com.patex.zombie.opds;

import com.patex.messaging.MessengerService;
import com.patex.zombie.model.User;
import com.patex.zombie.opds.entity.ExtLibraryRepository;
import com.patex.zombie.opds.entity.SavedBookRepository;
import com.patex.zombie.opds.entity.SubscriptionRepository;
import com.patex.zombie.service.AuthorService;
import com.patex.zombie.service.BookService;
import com.patex.zombie.service.Resources;
import com.patex.zombie.service.SequenceService;
import com.patex.zombie.service.UserService;
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
scanBasePackages = {"com.patex.zombie.opds","com.patex.zombie.service"})
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
