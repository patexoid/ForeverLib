package com.patex.zombie.opds.service;

import com.patex.LibException;
import com.patex.service.ZUserService;
import com.patex.utils.ExecutorCreator;
import com.patex.zombie.opds.entity.ExtLibrary;
import com.patex.zombie.opds.entity.Subscription;
import com.patex.zombie.opds.entity.SubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

@Service
public class ExtLibSubscriptionService {

    private static final Logger log = LoggerFactory.getLogger(ExtLibSubscriptionService.class);

    private final SubscriptionRepository subscriptionRepo;

    private final ExtLibDownloadService downloadService;

    private final ZUserService userService;

    private final ExecutorService executor;


    public ExtLibSubscriptionService(SubscriptionRepository subscriptionRepo,
                                     ExtLibDownloadService downloadService,
                                     ZUserService userService,
                                     ExecutorCreator executorCreator) {
        this.subscriptionRepo = subscriptionRepo;
        this.downloadService = downloadService;
        this.userService = userService;
        executor = executorCreator.createExecutor("ExtLibSubscriptionService", log);
    }

    public void addSubscription(ExtLibrary library, String uri) throws LibException {
        if (find(library, uri).isEmpty()) {
            Subscription saved = subscriptionRepo.save(new Subscription(library, uri, userService.getCurrentUser()));
            executor.execute(() -> checkSubscription(library, saved));
        }
    }

    public void deleteSubscription(Long id) throws LibException {
        subscriptionRepo.deleteById(id);
    }

    private void checkSubscription(ExtLibrary library, Subscription subscription) {
        downloadService.downloadAll(library, subscription.getLink(), subscription.getUser());
    }

    public void checkSubscriptions(ExtLibrary library) {
        Collection<Subscription> subscriptions = subscriptionRepo.findAllByExtLibrary(library);
        subscriptions.forEach(subscription ->
                downloadService.downloadAll(library, subscription.getLink(), subscription.getUser()));
    }

    public Optional<Subscription> find(ExtLibrary library, String url) {
        return subscriptionRepo.findFirstByExtLibraryAndLink(library, url);
    }
}
