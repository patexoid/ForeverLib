package com.patex.entities;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

/**
 * Created by Alexey on 12.03.2016.
 */

@Repository
public interface SubscriptionRepository extends CrudRepository<Subscription, Long> {

    Page<Subscription> findAll(Pageable pageable);

    Optional<Subscription> findFirstByExtLibraryAndLink(ExtLibrary library, String link);

    Collection<Subscription> findAllByExtLibrary(ExtLibrary library);
}
