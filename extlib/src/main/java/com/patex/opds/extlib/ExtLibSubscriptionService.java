package com.patex.opds.extlib;

import com.patex.LibException;
import com.patex.opds.entities.ExtLibrary;
import com.patex.opds.entities.Subscription;
import com.patex.opds.entities.SubscriptionRepository;
import com.patex.opds.service.UserService;
import com.patex.utils.ExecutorCreator;
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

    private final  ExecutorService executor;


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
        if (!find(library, uri).isPresent()) {
            Subscription saved = subscriptionRepo.save(new Subscription(library, uri, userService.getCurrentUser()));
            executor.execute(() -> checkSubscription(library, saved));
        }
    }

    public void deleteSubscription(Long id) throws LibException {
        subscriptionRepo.deleteById(id);
    }

    private void checkSubscription(ExtLibrary library, Subscription subscription) {
        downloadService.downloadAll(library, subscription.getLink(), subscription.getUserId());
    }

    public void checkSubscriptions(ExtLibrary library) {
        Collection<Subscription> subscriptions = subscriptionRepo.findAllByExtLibrary(library);
        subscriptions.forEach(subscription ->
                downloadService.downloadAll(library, subscription.getLink(), subscription.getUserId()));
    }

    public Optional<Subscription> find(ExtLibrary library, String url) {
        return subscriptionRepo.findFirstByExtLibraryAndLink(library, url);
    }
}
