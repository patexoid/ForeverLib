package com.patex.zombie.opds.entity;

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
public interface SubscriptionRepository extends CrudRepository<SubscriptionEntity, Long> {

    Page<SubscriptionEntity> findAll(Pageable pageable);

    Optional<SubscriptionEntity> findFirstByExtLibraryAndLink(ExtLibrary library, String link);

    Collection<SubscriptionEntity> findAllByExtLibrary(ExtLibrary library);
}
