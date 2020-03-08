package com.patex.zombie.opds;


import com.google.common.util.concurrent.MoreExecutors;
import com.patex.entities.ZUser;
import com.patex.service.ZUserService;
import com.patex.utils.ExecutorCreator;
import com.patex.zombie.opds.entity.ExtLibrary;
import com.patex.zombie.opds.entity.Subscription;
import com.patex.zombie.opds.entity.SubscriptionRepository;
import com.patex.zombie.opds.service.ExtLibDownloadService;
import com.patex.zombie.opds.service.ExtLibSubscriptionService;
import org.junit.Before;
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
public class ExtLibSubscriptionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepo;

    @Mock
    private ExtLibDownloadService downloadService;

    @Mock
    private ZUserService userService;

    private ExtLibSubscriptionService subscriptionService;

    @Mock
    private ExecutorCreator executorCreator;
    private ZUser user;
    private String url;

    @Before
    public void setUp() {
        user = new ZUser();
        Mockito.when(userService.getCurrentUser()).thenReturn(user);
        Mockito.when(executorCreator.createExecutor(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(MoreExecutors.newDirectExecutorService());
        subscriptionService = new ExtLibSubscriptionService(subscriptionRepo, downloadService,
                userService, executorCreator);

        Mockito.when(subscriptionRepo.save(ArgumentMatchers.any())).then(invocation -> invocation.getArguments()[0]);

        url = "uri";
    }

    @Test
    public void shouldAddAndCheckSubscription() {

        ExtLibrary library = new ExtLibrary();
        subscriptionService.addSubscription(library, url);
        ArgumentCaptor<Subscription> argument = ArgumentCaptor.forClass(Subscription.class);
        Mockito.verify(subscriptionRepo).save(argument.capture());
        assertEquals(library, argument.getValue().getExtLibrary());
        assertEquals(url, argument.getValue().getLink());
        assertEquals(user, argument.getValue().getUser());
        Mockito.verify(downloadService).downloadAll(library, url, user);
    }


    @Test
    public void shouldNotAddIfExists() {
        ExtLibrary library = new ExtLibrary();
        Mockito.when(subscriptionRepo.findFirstByExtLibraryAndLink(library, url)).thenReturn(Optional.of(new Subscription()));
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
        Subscription subscription = new Subscription();
        subscription.setLink(url);
        subscription.setExtLibrary(library);
        subscription.setUser(user);

        Mockito.when(subscriptionRepo.findAllByExtLibrary(library)).thenReturn(Collections.singletonList(subscription));

        subscriptionService.checkSubscriptions(library);

        Mockito.verify(downloadService).downloadAll(library, url, user);
        verifyNoMoreInteractions(downloadService);

    }
}
