package com.patex.zombie.opds;


import com.google.common.util.concurrent.MoreExecutors;
import com.patex.zombie.model.User;
import com.patex.zombie.opds.entity.ExtLibrary;
import com.patex.zombie.opds.entity.SubscriptionEntity;
import com.patex.zombie.opds.entity.SubscriptionRepository;
import com.patex.zombie.opds.service.ExtLibDownloadService;
import com.patex.zombie.opds.service.ExtLibSubscriptionService;
import com.patex.zombie.service.ExecutorCreator;
import com.patex.zombie.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
@Disabled
public class ExtLibSubscriptionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepo;

    @Mock
    private ExtLibDownloadService downloadService;

    @Mock
    private UserService userService;

    private ExtLibSubscriptionService subscriptionService;

    @Mock
    private ExecutorCreator executorCreator;
    private User user;
    private String url;

    @BeforeEach
    public void setUp() {
        user = new User();
        Mockito.when(userService.getCurrentUser()).thenReturn(user);
        Mockito.when(executorCreator.createExecutor(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(MoreExecutors.newDirectExecutorService());
        subscriptionService = new ExtLibSubscriptionService(subscriptionRepo, downloadService,
                userService, executorCreator);

        Mockito.when(subscriptionRepo.save(ArgumentMatchers.any())).then(invocation -> invocation.getArguments()[0]);

        url = "uri";
    }

    @Test
    @Disabled
    public void shouldAddAndCheckSubscription() {

        ExtLibrary library = new ExtLibrary();
        subscriptionService.addSubscription(library, url);
        ArgumentCaptor<SubscriptionEntity> argument = ArgumentCaptor.forClass(SubscriptionEntity.class);
        Mockito.verify(subscriptionRepo).save(argument.capture());
        assertEquals(library, argument.getValue().getExtLibrary());
        assertEquals(url, argument.getValue().getLink());
        assertEquals(user, argument.getValue().getUser());
        Mockito.verify(downloadService).downloadAll(library, url, user.getUsername());
    }


    @Test
    public void shouldNotAddIfExists() {
        ExtLibrary library = new ExtLibrary();
        Mockito.when(subscriptionRepo.findFirstByExtLibraryAndLink(library, url)).thenReturn(Optional.of(new SubscriptionEntity()));
        subscriptionService.addSubscription(library, url);
        Mockito.verify(subscriptionRepo, Mockito.never()).save(ArgumentMatchers.any());
        verifyNoInteractions(downloadService);
    }

    @Test
    public void shouldDeleteSubscription() {
        long id = 42L;
        subscriptionService.deleteSubscription(id);
        Mockito.verify(subscriptionRepo).deleteById(id);
    }

    @Test
    public void shouldCheckSubscriptions() {
        ExtLibrary library = new ExtLibrary();
        SubscriptionEntity subscription = new SubscriptionEntity();
        subscription.setLink(url);
        subscription.setExtLibrary(library);
        subscription.setUser(user.getUsername());

        Mockito.when(subscriptionRepo.findAllByExtLibrary(library)).thenReturn(Collections.singletonList(subscription));

        subscriptionService.checkSubscriptions(library);

        Mockito.verify(downloadService).downloadAll(library, url, user.getUsername());
        verifyNoMoreInteractions(downloadService);

    }
}
