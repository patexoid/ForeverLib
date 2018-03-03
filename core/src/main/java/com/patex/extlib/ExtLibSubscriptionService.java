package com.patex.extlib;

import com.patex.LibException;
import com.patex.entities.ExtLibrary;
import com.patex.entities.Subscription;
import com.patex.entities.SubscriptionRepository;
import com.patex.service.ZUserService;
import com.patex.utils.ExecutorCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;

@Service
public class ExtLibSubscriptionService {

    private static Logger log = LoggerFactory.getLogger(ExtLibSubscriptionService.class);

    private SubscriptionRepository subscriptionRepo;

    private ExtLibDownloadService downloadService;

    private ZUserService userService;

    private ExecutorService executor = ExecutorCreator.createExecutor("ExtLibSubscriptionService", log);


    public ExtLibSubscriptionService(SubscriptionRepository subscriptionRepo, ExtLibDownloadService downloadService,
                                     ZUserService userService) {
        this.subscriptionRepo = subscriptionRepo;
        this.downloadService = downloadService;
        this.userService = userService;
    }

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
