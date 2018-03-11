package com.patex.service;

import com.patex.entities.Subscription;
import com.patex.entities.SubscriptionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Created by Alexey on 05.07.2017.
 */
@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    public Subscription save(Subscription entity) {
        return subscriptionRepository.save(entity);
    }

    public Subscription findOne(Long aLong) {
        return subscriptionRepository.findOne(aLong);
    }

    public Page<Subscription> findAll(Pageable pageable) {
        return subscriptionRepository.findAll(pageable);
    }
}
