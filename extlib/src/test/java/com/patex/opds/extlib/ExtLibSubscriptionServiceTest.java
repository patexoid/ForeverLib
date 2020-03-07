package com.patex.opds.extlib;


import com.google.common.util.concurrent.MoreExecutors;
import com.patex.opds.entities.ExtLibrary;
import com.patex.opds.entities.Subscription;
import com.patex.opds.entities.SubscriptionRepository;
import com.patex.opds.service.UserService;
import com.patex.utils.ExecutorCreator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
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
    private String user;
    private String url;

    @Before
    public void setUp() {
        user = "new ZUser()";
        when(userService.getCurrentUser()).thenReturn(user);
        when(executorCreator.createExecutor(any(), any())).thenReturn(MoreExecutors.newDirectExecutorService());
        subscriptionService = new ExtLibSubscriptionService(subscriptionRepo, downloadService,
                userService, executorCreator);

        when(subscriptionRepo.save(any())).then(invocation -> invocation.getArguments()[0]);

        url = "uri";
    }

    @Test
    public void shouldAddAndCheckSubscription() {

        ExtLibrary library = new ExtLibrary();
        subscriptionService.addSubscription(library, url);
        ArgumentCaptor<Subscription> argument = ArgumentCaptor.forClass(Subscription.class);
        verify(subscriptionRepo).save(argument.capture());
        assertEquals(library, argument.getValue().getExtLibrary());
        assertEquals(url, argument.getValue().getLink());
        assertEquals(user, argument.getValue().getUserId());
        verify(downloadService).downloadAll(library, url, user);
    }


    @Test
    public void shouldNotAddIfExists() {
        ExtLibrary library = new ExtLibrary();
        when(subscriptionRepo.findFirstByExtLibraryAndLink(library, url)).thenReturn(Optional.of(new Subscription()));
        subscriptionService.addSubscription(library, url);
        verify(subscriptionRepo,never()).save(any());
        verifyZeroInteractions(downloadService);
    }

    @Test
    public void shouldDeleteSubscription() {
        long id = 42L;
        subscriptionService.deleteSubscription(id);
        verify(subscriptionRepo).deleteById(id);
    }

    @Test
    public void shouldCheckSubscriptions() {
        ExtLibrary library = new ExtLibrary();
        Subscription subscription = new Subscription();
        subscription.setLink(url);
        subscription.setExtLibrary(library);
        subscription.setUserId(user);

        when(subscriptionRepo.findAllByExtLibrary(library)).thenReturn(Collections.singletonList(subscription));

        subscriptionService.checkSubscriptions(library);

        verify(downloadService).downloadAll(library, url, user);
        verifyNoMoreInteractions(downloadService);

    }
}
