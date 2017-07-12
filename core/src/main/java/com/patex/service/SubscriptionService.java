package com.patex.service;

import com.patex.entities.Subscription;
import com.patex.entities.SubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;

import static com.patex.service.ZUserService.USER;

/**
 * Created by Alexey on 05.07.2017.
 */
@Service
public class SubscriptionService {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Secured(USER)
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
