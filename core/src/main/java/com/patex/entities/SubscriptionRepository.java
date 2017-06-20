package com.patex.entities;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by Alexey on 12.03.2016.
 */

@Repository
public interface SubscriptionRepository extends CrudRepository<Subscription, Long> {
}
