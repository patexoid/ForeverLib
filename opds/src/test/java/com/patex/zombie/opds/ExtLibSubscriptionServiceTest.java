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
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(MockitoJUnitRunner.class)
@Ignore
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

    @Before
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
    @Ignore
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
        verifyZeroInteractions(downloadService);
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
