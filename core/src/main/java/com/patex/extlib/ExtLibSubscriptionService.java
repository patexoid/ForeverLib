package com.patex.extlib;

import com.patex.LibException;
import com.patex.entities.ExtLibrary;
import com.patex.entities.Subscription;
import com.patex.entities.SubscriptionRepository;
import com.patex.service.ZUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutorService;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ExtLibSubscriptionService {

    @Autowired
    private SubscriptionRepository subscriptionRepo;

    @Autowired
    private ExtLibDownloadService downloadService;

    @Autowired
    private ZUserService userService;

    private ExecutorService executor = new DelegatingSecurityContextExecutorService(
            Executors.newCachedThreadPool(r -> {
                AtomicInteger count = new AtomicInteger();
                Thread thread = new Thread(r);
                thread.setName("ExtLibSubscriptionService-" + count.incrementAndGet());
                thread.setDaemon(true);
                return thread;
            }));

    public void addSubscription(ExtLibrary library, String uri) throws LibException {
        if (library.getSubscriptions().stream().
                noneMatch(s -> uri.equals(s.getLink()))) {
            Subscription saved = subscriptionRepo.save(new Subscription(library, uri, userService.getCurrentUser()));
            library.getSubscriptions().add(saved);
            executor.execute(() -> checkSubscription(library, saved));
        }
    }

    public void deleteSubscription(Long id) throws LibException {
        subscriptionRepo.delete(id);
    }

    private void checkSubscription(ExtLibrary library, Subscription subscription) {
        downloadService.downloadAll(library, subscription.getLink(), subscription.getUser());
    }

    public void checkSubscriptions(ExtLibrary library) {
        library.getSubscriptions().forEach(subscription ->
                downloadService.downloadAll(library, subscription.getLink(), subscription.getUser()));
    }
}
