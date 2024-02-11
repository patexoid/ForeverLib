package com.patex.forever.opds.service;

import com.patex.forever.LibException;
import com.patex.forever.opds.entity.ExtLibrary;
import com.patex.forever.opds.entity.SubscriptionEntity;
import com.patex.forever.opds.entity.SubscriptionRepository;
import com.patex.forever.service.ExecutorCreator;
import com.patex.forever.service.UserService;
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

    private final UserService userService;

    private final ExecutorService executor;


    public ExtLibSubscriptionService(SubscriptionRepository subscriptionRepo,
                                     ExtLibDownloadService downloadService,
                                     UserService userService,
                                     ExecutorCreator executorCreator) {
        this.subscriptionRepo = subscriptionRepo;
        this.downloadService = downloadService;
        this.userService = userService;
        executor = executorCreator.createExecutor("ExtLibSubscriptionService", log);
    }

    public void addSubscription(ExtLibrary library, String uri) throws LibException {
        if (find(library, uri).isEmpty()) {
            SubscriptionEntity saved = subscriptionRepo.save(new SubscriptionEntity(library, uri, userService.getCurrentUser().getUsername()));
            executor.execute(() -> checkSubscription(library, saved));
        }
    }

    public void deleteSubscription(Long id) throws LibException {
        subscriptionRepo.deleteById(id);
    }

    private void checkSubscription(ExtLibrary library, SubscriptionEntity subscription) {
        downloadService.downloadAll(library, subscription.getLink(), subscription.getUser());
    }

    public void checkSubscriptions(ExtLibrary library) {
        Collection<SubscriptionEntity> subscriptions = subscriptionRepo.findAllByExtLibrary(library);
        subscriptions.forEach(subscription ->
                downloadService.downloadAll(library, subscription.getLink(), subscription.getUser()));
    }

    public Optional<SubscriptionEntity> find(ExtLibrary library, String url) {
        return subscriptionRepo.findFirstByExtLibraryAndLink(library, url);
    }
}
